import type {
  ErrorResponse,
  LabelRegistries,
  MethodologyDto,
  NarrativeResponseDto,
  RetentionDto,
  ScenarioRunRequestInput,
  ScenarioRunResponse,
  SupportedScenarioType,
} from "./types";

import { pushLog } from "./logBuffer";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

/**
 * `request()` chạy ở cả hai phía: trình duyệt (form Trung tâm quyết định) và
 * máy chủ Next (các trang RSC). Ghi rõ phía nào, vì trang nhật ký chỉ thấy
 * trực tiếp phía trình duyệt.
 */
const ON_SERVER = typeof window === "undefined";

/**
 * Ghi một mục nhật ký, và với phía máy chủ thì in thêm ra stdout của Next.
 *
 * <p>Trước đây file này không ghi gì cả và nuốt lỗi ở bốn chỗ, nên một lần
 * `fetchLabels` hỏng làm cả trang kết quả tụt xuống hiển thị enum thô mà không
 * để lại dấu vết nào.
 */
function log(entry: Parameters<typeof pushLog>[0]) {
  if (ON_SERVER) {
    const { level, message, ...rest } = entry;
    const line = `[destiny-web] ${level}: ${message} ${JSON.stringify(rest)}`;
    if (level === "error") console.error(line);
    else if (level === "warn") console.warn(line);
    else console.log(line);
    return;
  }
  pushLog(entry);
}

/**
 * Hạn chờ mặc định cho mọi lệnh gọi API — đủ rộng cho một lần chạy kịch bản
 * (nhiều engine chạy song song, harness backend giới hạn 5s mỗi engine) nhưng
 * hữu hạn.
 *
 * Trước đây `request()` không có `AbortSignal` nào cả, nghĩa là hạn chờ thực tế
 * là hạn chờ của runtime — có thể là vài phút, hoặc không bao giờ. Một fetch
 * treo trong server component không hiện ra như lỗi: nó hiện ra như một trang
 * không bao giờ tải xong, đúng kiểu suy giảm im lặng mà ADR D8 khiến ta dễ
 * bỏ qua.
 */
const DEFAULT_TIMEOUT_MS = 20_000;

/**
 * Hạn chờ riêng, dài hơn, cho lệnh gọi narrative.
 *
 * Đây là lệnh gọi duy nhất có thể mất hàng chục giây một cách hợp lệ: backend
 * đi hết chuỗi model OpenRouter, và bản thân nó cũng có deadline tổng
 * (`destiny.ai.openrouter.total-deadline-ms`, mặc định 45s + tối đa một lần
 * gọi HTTP nữa đang dở). Giá trị ở đây
 * phải **lớn hơn** deadline tổng của backend, để khi mọi model đều thất bại
 * thì backend là bên kết thúc trước và người dùng nhận được lý do fallback
 * thật (`fallbackReason`) thay vì một lần huỷ phía client không nói lên
 * điều gì.
 */
const NARRATIVE_TIMEOUT_MS = 75_000;

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly body: ErrorResponse | null,
  ) {
    super(body?.message ?? `Yêu cầu tới API thất bại (HTTP ${status}).`);
  }
}

async function request<T>(
  path: string,
  init?: RequestInit,
  timeoutMs: number = DEFAULT_TIMEOUT_MS,
): Promise<T> {
  const method = init?.method ?? "GET";
  const startedAt = Date.now();
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers: {
        "Content-Type": "application/json",
        ...init?.headers,
      },
      // Người gọi tự truyền `signal` thì tôn trọng lựa chọn đó; nếu không thì
      // áp hạn chờ để không lệnh gọi nào treo vô hạn.
      signal: init?.signal ?? AbortSignal.timeout(timeoutMs),
      cache: "no-store",
    });
  } catch (error) {
    const durationMs = Date.now() - startedAt;
    // Hạn chờ được quy về `504` dạng ApiError chứ không để `TimeoutError` thô
    // rò lên UI: mọi nơi trong web đã biết cách xử lý ApiError, và thông điệp
    // tiếng Việt là thứ người dùng có thể đọc được (CLAUDE.md §9).
    if (error instanceof DOMException && error.name === "TimeoutError") {
      const message = `Hệ thống tính toán không phản hồi trong ${Math.round(timeoutMs / 1000)} giây.`;
      log({
        level: "error",
        origin: ON_SERVER ? "server" : "client",
        kind: "api",
        method,
        url: path,
        durationMs,
        code: "REQUEST_TIMEOUT",
        message,
        detail: { timeoutMs },
      });
      throw new ApiError(504, { code: "REQUEST_TIMEOUT", message });
    }
    // `next build` thăm dò mỗi route bằng cách thử render tĩnh, và `cache:
    // "no-store"` ở trên khiến Next ném DynamicServerError để đánh dấu route
    // là động. Đó là hoạt động bình thường của khung, không phải sự cố — ghi
    // nó thành "error" sẽ làm bản build in ra hai dòng đỏ vô hại và dạy người
    // đọc bỏ qua nhật ký.
    if (isNextDynamicUsage(error)) {
      throw error;
    }
    log({
      level: "error",
      origin: ON_SERVER ? "server" : "client",
      kind: "error",
      method,
      url: path,
      durationMs,
      code: error instanceof Error ? error.name : undefined,
      message:
        error instanceof Error
          ? `Lệnh gọi thất bại trước khi có phản hồi: ${error.message}`
          : "Lệnh gọi thất bại trước khi có phản hồi.",
    });
    throw error;
  }

  const durationMs = Date.now() - startedAt;

  if (!response.ok) {
    let body: ErrorResponse | null = null;
    try {
      body = (await response.json()) as ErrorResponse;
    } catch {
      // Thân phản hồi không phải JSON. Trước đây trường hợp này bị nuốt hoàn
      // toàn và ApiError lùi về một câu chung chung, nên một lỗi hạ tầng (proxy
      // trả HTML) trông y hệt một lỗi nghiệp vụ.
      log({
        level: "warn",
        origin: ON_SERVER ? "server" : "client",
        kind: "api",
        method,
        url: path,
        status: response.status,
        durationMs,
        message: "Máy chủ trả lỗi nhưng thân phản hồi không phải JSON.",
      });
    }
    log({
      level: "error",
      origin: ON_SERVER ? "server" : "client",
      kind: "api",
      method,
      url: path,
      status: response.status,
      durationMs,
      code: body?.code,
      message: body?.message ?? `Yêu cầu tới API thất bại (HTTP ${response.status}).`,
    });
    throw new ApiError(response.status, body);
  }

  const parsed = (await response.json()) as T;
  log({
    level: "info",
    origin: ON_SERVER ? "server" : "client",
    kind: "api",
    method,
    url: path,
    status: response.status,
    durationMs,
    message: "Thành công.",
    calculationId:
      typeof parsed === "object" && parsed !== null && "calculationId" in parsed
        ? String((parsed as { calculationId?: unknown }).calculationId ?? "")
        : undefined,
  });
  return parsed;
}

/** Next dùng ngoại lệ này làm tín hiệu nội bộ, không phải để báo hỏng. */
function isNextDynamicUsage(error: unknown): boolean {
  if (typeof error !== "object" || error === null) return false;
  const digest = (error as { digest?: unknown }).digest;
  return typeof digest === "string" && digest.startsWith("DYNAMIC_SERVER_USAGE");
}

export function listMethodologies(): Promise<MethodologyDto[]> {
  return request<MethodologyDto[]>("/api/v1/methodologies");
}

/**
 * Every Vietnamese label, for rendering enum names that arrive inside
 * free-form evidence facts (a Bát Tự chart, today).
 *
 * Returns an empty registry rather than throwing when the call fails: a
 * missing label table should degrade a chart to its technical names, not blank
 * the whole result page a user is waiting on.
 */
export async function fetchLabels(): Promise<LabelRegistries> {
  try {
    return await request<LabelRegistries>("/api/v1/labels");
  } catch {
    // Hành vi không đổi - vẫn suy giảm chứ không làm trắng trang. Cái đổi là
    // nó không còn im lặng: đây là thất bại duy nhất tự tay tạo ra vi phạm §9
    // trên khắp trang kết quả (mọi enum hiện ra dưới dạng tên kỹ thuật), và
    // trước đây không có gì ở đâu nói vì sao.
    log({
      level: "warn",
      origin: ON_SERVER ? "server" : "client",
      kind: "api",
      url: "/api/v1/labels",
      message:
        "Không tải được bảng nhãn tiếng Việt; trang kết quả sẽ hiển thị tên kỹ thuật thay cho nhãn Việt.",
    });
    return {};
  }
}

export function runScenario(
  scenarioType: SupportedScenarioType,
  input: ScenarioRunRequestInput,
): Promise<ScenarioRunResponse> {
  return request<ScenarioRunResponse>(`/api/v1/scenarios/${scenarioType.toLowerCase()}`, {
    method: "POST",
    body: JSON.stringify(input),
  });
}

/**
 * Keeps a calculation indefinitely (retention class `USER_SAVED`).
 *
 * Unlike `findCalculation`, a 404 here is allowed to throw: the user pressed a
 * button and is owed an error message, whereas a missing calculation on a page
 * load is a state the page can render honestly.
 */
export function saveCalculation(calculationId: string): Promise<RetentionDto> {
  return request<RetentionDto>(
    `/api/v1/calculations/${encodeURIComponent(calculationId)}/save`,
    { method: "POST" },
  );
}

/** Returns `null` on a 404 (unknown calculation id) rather than throwing - callers render "not found" honestly. */
export async function findCalculation(calculationId: string): Promise<ScenarioRunResponse | null> {
  try {
    return await request<ScenarioRunResponse>(
      `/api/v1/calculations/${encodeURIComponent(calculationId)}`,
    );
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      return null;
    }
    throw error;
  }
}

function narrativePath(calculationId: string): string {
  return `/api/v1/calculations/${encodeURIComponent(calculationId)}/narrative`;
}

/** Nguồn diễn giải là bản dự phòng phi-AI (enum `NarrativeSource.FALLBACK` ở `destiny-ai`). */
export function isFallbackNarrative(narrative: NarrativeResponseDto): boolean {
  return narrative.source.technical === "FALLBACK";
}

/**
 * Lấy phần diễn giải đã sinh của một lần tính nếu có, và sinh nó qua
 * `POST .../narrative` khi cần. Trả `null` thay vì throw khi thất bại
 * (không có lần tính, API chết) — phần diễn giải là lời bình về dữ liệu cứng
 * (CLAUDE.md §9), không bao giờ là lý do làm trắng trang kết quả.
 *
 * <p>**Vì sao một bản FALLBACK đã lưu vẫn phải POST lại.** `NarrativeRecorder`
 * ở backend là *upsert* và nó ghi cả kết quả fallback. Trước đây hàm này chỉ
 * POST khi `GET` trả `404`, nên bất kỳ lần tính nào từng sinh diễn giải trong
 * lúc AI đang tắt sẽ **vĩnh viễn** được phục vụ bản fallback đó: bật AI lên
 * sau cũng không thay đổi gì, và không có lỗi nào ở đâu để lần ra. `FALLBACK`
 * là một trạng thái *tạm thời* theo bản chất (AI tắt, chưa có API key, model
 * đang bị giới hạn tần suất, phản hồi sai định dạng), nên nó phải được coi là
 * "chưa có diễn giải AI" chứ không phải "đã có kết quả cuối".
 *
 * <p>Khi POST lại thất bại, hàm trả về **bản fallback đã có** chứ không phải
 * `null`. Bản fallback vẫn là báo cáo dựng từ đúng dữ liệu tính toán và vẫn
 * đọc được; đánh mất nó để đổi lấy một panel trống là làm người dùng tệ hơn
 * so với trước khi sửa.
 */
export async function getOrGenerateNarrative(
  calculationId: string,
): Promise<NarrativeResponseDto | null> {
  const path = narrativePath(calculationId);
  let existing: NarrativeResponseDto | null = null;
  try {
    existing = await request<NarrativeResponseDto>(path, undefined, NARRATIVE_TIMEOUT_MS);
    if (!isFallbackNarrative(existing)) {
      return existing;
    }
  } catch (error) {
    if (!(error instanceof ApiError) || error.status !== 404) {
      log({
        level: "warn",
        origin: ON_SERVER ? "server" : "client",
        kind: "api",
        url: path,
        message: "Không đọc được phần diễn giải đã lưu; bỏ qua và không sinh lại.",
      });
      return null;
    }
  }
  try {
    return await request<NarrativeResponseDto>(
      path,
      { method: "POST" },
      NARRATIVE_TIMEOUT_MS,
    );
  } catch {
    log({
      level: "warn",
      origin: ON_SERVER ? "server" : "client",
      kind: "api",
      url: path,
      message: "Sinh diễn giải thất bại; giữ nguyên bản dự phòng đã có nếu có.",
    });
    return existing;
  }
}

/**
 * Bắt backend sinh lại phần diễn giải (nút "Tạo lại phần diễn giải").
 *
 * <p>Khác `getOrGenerateNarrative`: ở đây lỗi được **throw** ra, vì người dùng
 * vừa bấm một nút và xứng đáng được biết là nó không thành công — trong khi
 * cùng một thất bại lúc tải trang chỉ nên làm phần diễn giải suy giảm.
 */
export function regenerateNarrative(calculationId: string): Promise<NarrativeResponseDto> {
  return request<NarrativeResponseDto>(
    narrativePath(calculationId),
    { method: "POST" },
    NARRATIVE_TIMEOUT_MS,
  );
}
