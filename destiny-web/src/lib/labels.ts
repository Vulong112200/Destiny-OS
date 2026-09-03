import type { ErrorResponse, LabelRegistries } from "./types";
import { ApiError } from "./api";

/**
 * Bảng nhãn tiếng Việt và cách đọc chúng.
 *
 * <p>Mọi thứ ở đây tồn tại vì một lý do: `docs/UI_UX_VIETNAMESE_SPEC.md` §1.1
 * nói rõ rằng **nhãn Việt đã có mà không được render là vi phạm §9**, ngang với
 * việc không có nhãn. Backend đã phục vụ đủ bảng nhãn qua `GET /api/v1/labels`
 * từ lâu; frontend nhận về rồi vứt đi ở gần chục chỗ, nên người dùng đọc được
 * `NUMEROLOGY_PYTHAGOREAN` và `FENGSHUI_KUA` trên trang kết quả.
 *
 * <p>Bốn component từng giữ bốn bản sao y hệt của `label()`. Giờ chỉ còn một.
 */

/** Nhãn Việt cho một giá trị enum, hoặc chính tên kỹ thuật nếu chưa có nhãn. */
export function labelOf(
  labels: LabelRegistries | undefined,
  type: string,
  key: unknown,
  fallback = "—",
): string {
  if (typeof key !== "string") return fallback;
  return labels?.[type]?.[key] ?? key;
}

/** Tên tiếng Việt của một engine, ví dụ `NUMEROLOGY_PYTHAGOREAN` → "Thần số học (Pythagorean)". */
export function engineName(engineId: string, labels?: LabelRegistries): string {
  return labels?.Engine?.[engineId] ?? engineId;
}

/**
 * Danh sách tên engine đã dịch, nối bằng dấu phẩy.
 *
 * <p>Thay cho `ids.join(", ")`, vốn là dạng vi phạm phổ biến nhất: nó trông
 * vô hại trong code và in ra một chuỗi enum thô cho người dùng.
 */
export function engineNames(
  ids: readonly string[],
  labels?: LabelRegistries,
  empty = "—",
): string {
  if (ids.length === 0) return empty;
  return ids.map((id) => engineName(id, labels)).join(", ");
}

/**
 * Tên tiếng Việt của các quy tắc tổng hợp `R1`–`R8`.
 *
 * <p>**Đây là bản sao chép tay** của `docs/FUSION_ENGINE_SPEC.md` §"Rules".
 * Backend trả `rulesApplied` dưới dạng mã trần và chưa có registry nhãn cho
 * chúng, nên trang kết quả hiển thị đúng chữ "R2, R5" — thứ không nói gì với
 * cả người dùng lẫn lập trình viên. Sửa đúng gốc là thêm registry `FusionRule`
 * vào `VietnameseLabels` (hoặc đổi `rulesApplied` thành `LabeledValue`); tới
 * lúc đó bảng này phải được cập nhật cùng commit với phía Java.
 */
const FUSION_RULE_VI: Record<string, string> = {
  R1: "Chưa đủ bằng chứng",
  R2: "Đồng thuận ủng hộ",
  R3: "Đồng thuận thận trọng",
  R4: "Vừa ủng hộ vừa thận trọng",
  R5: "Có tín hiệu quan trọng",
  R6: "Tín hiệu trái chiều",
  R7: "Mâu thuẫn lớn",
  R8: "Mâu thuẫn giữa các phương pháp",
};

/** Ví dụ: `R2` → "Đồng thuận ủng hộ (R2)". Giữ lại mã để đối chiếu với tài liệu. */
export function fusionRuleName(ruleId: string): string {
  const vi = FUSION_RULE_VI[ruleId];
  return vi ? `${vi} (${ruleId})` : ruleId;
}

export function fusionRuleNames(ruleIds: readonly string[], empty = "—"): string {
  if (ruleIds.length === 0) return empty;
  return ruleIds.map(fusionRuleName).join(", ");
}

/**
 * Bóc một lỗi API thành thứ có thể vừa hiển thị vừa ghi vào nhật ký.
 *
 * <p>`DecisionCenterForm` từng làm `err instanceof ApiError ? err.message : "..."`,
 * vứt sạch `status` và mã `code`. Nên một lần hết hạn chờ hiện ra đúng một câu
 * tiếng Việt, không mã lỗi, không mã HTTP, không cách nào lần ra.
 */
export function describeApiError(error: unknown): {
  message: string;
  code: string | null;
  status: number | null;
} {
  if (error instanceof ApiError) {
    const body: ErrorResponse | null = error.body;
    return {
      message: error.message,
      code: body?.code ?? null,
      status: error.status,
    };
  }
  if (error instanceof Error) {
    return { message: `Không thể kết nối tới hệ thống tính toán. (${error.name})`, code: error.name, status: null };
  }
  return { message: "Không thể kết nối tới hệ thống tính toán.", code: null, status: null };
}
