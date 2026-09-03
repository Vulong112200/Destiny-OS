/**
 * Bộ đệm nhật ký phía trình duyệt.
 *
 * <p>Trước khi có file này, toàn bộ `src/` **không có một lệnh `console` nào**,
 * và có bốn chỗ nuốt lỗi im lặng. Hậu quả cụ thể: `fetchLabels()` thất bại thì
 * trả về `{}` và trang kết quả âm thầm tụt xuống hiển thị tên enum thô — đúng
 * cái lỗi tiếng Việt mà dự án cấm — mà không có gì ở đâu nói vì sao. Còn một
 * lần hết hạn chờ thì hiện đúng một câu, vứt mất cả mã HTTP lẫn mã lỗi.
 *
 * <p><strong>Không bao giờ ghi dữ liệu cá nhân.</strong> Không họ tên, không
 * ngày/giờ sinh, không tọa độ, không câu hỏi. Với một lần chạy kịch bản thì chỉ
 * ghi *tên những hệ* được bật, không ghi nội dung. Đây là chỗ dễ vô tình biến
 * một trang chẩn đoán thành một kho dữ liệu ngày sinh.
 *
 * <p>Dùng `sessionStorage` chứ không phải `localStorage`: dự án có chính sách
 * lưu trữ có thời hạn (CLAUDE.md §7), và một nhật ký sống lâu hơn cái tab là
 * thứ không ai yêu cầu.
 */

export type LogLevel = "info" | "warn" | "error";

export interface LogEntry {
  id: string;
  at: string;
  level: LogLevel;
  origin: "client" | "server";
  kind: "api" | "action" | "error";
  method?: string;
  url?: string;
  status?: number;
  durationMs?: number;
  /** Mã lỗi từ `ErrorResponse.code`, ví dụ `REQUEST_TIMEOUT`. */
  code?: string;
  message: string;
  detail?: Record<string, unknown>;
  calculationId?: string;
}

const CAP = 300;
const STORAGE_KEY = "destiny-os:nhat-ky";

let buffer: LogEntry[] = [];
let loaded = false;
const listeners = new Set<() => void>();
let seq = 0;

function isBrowser(): boolean {
  return typeof window !== "undefined";
}

function load() {
  if (loaded || !isBrowser()) return;
  loaded = true;
  try {
    const raw = window.sessionStorage.getItem(STORAGE_KEY);
    if (raw) buffer = JSON.parse(raw) as LogEntry[];
  } catch {
    // Trình duyệt chặn sessionStorage, hoặc nội dung hỏng. Bắt đầu lại từ
    // trống - một nhật ký không đọc được không được phép làm sập trang.
    buffer = [];
  }
}

function persist() {
  if (!isBrowser()) return;
  try {
    window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(buffer));
  } catch {
    // Hết dung lượng hoặc bị chặn. Bộ đệm trong bộ nhớ vẫn dùng được.
  }
}

function emit() {
  listeners.forEach((fn) => fn());
}

export function pushLog(entry: Omit<LogEntry, "id" | "at"> & { at?: string }): void {
  load();
  seq += 1;
  const full: LogEntry = {
    ...entry,
    id: `${Date.now().toString(36)}-${seq}`,
    at: entry.at ?? new Date().toISOString(),
  };
  buffer = [...buffer, full].slice(-CAP);
  persist();
  emit();
}

/** Thêm nhiều mục cùng lúc — dùng cho log sinh ra ở phía máy chủ Next. */
export function pushLogs(entries: LogEntry[]): void {
  if (entries.length === 0) return;
  load();
  const known = new Set(buffer.map((e) => e.id));
  const fresh = entries.filter((e) => !known.has(e.id));
  if (fresh.length === 0) return;
  buffer = [...buffer, ...fresh].slice(-CAP);
  persist();
  emit();
}

export function readLog(): LogEntry[] {
  load();
  return buffer;
}

export function clearLog(): void {
  buffer = [];
  persist();
  emit();
}

export function subscribeLog(fn: () => void): () => void {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

/** Mốc thời gian dạng ngắn cho bảng: `14:03:27`. */
export function shortTime(iso: string): string {
  try {
    return new Date(iso).toLocaleTimeString("vi-VN", { hour12: false });
  } catch {
    return iso;
  }
}

/**
 * Toàn bộ nhật ký thành một khối văn bản dán được vào khung chat.
 *
 * <p>Đây là lý do trang này tồn tại: chủ dự án muốn tự xem và copy gửi đi.
 * Nên định dạng phải đọc được bằng mắt, không phải JSON.
 */
export function formatLogForChat(entries: LogEntry[]): string {
  const header = [
    "=== NHẬT KÝ DESTINY OS ===",
    `Thời điểm xuất: ${new Date().toISOString()}`,
    `Địa chỉ API: ${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}`,
    isBrowser() ? `Trình duyệt: ${window.navigator.userAgent}` : "Trình duyệt: (không rõ)",
    `Số mục: ${entries.length}`,
    "",
  ];

  const lines = entries.map((e) => {
    const parts = [
      shortTime(e.at),
      e.level.toUpperCase().padEnd(5),
      e.origin === "server" ? "[máy chủ]" : "[trình duyệt]",
    ];
    if (e.method) parts.push(e.method);
    if (e.url) parts.push(e.url);
    if (e.status !== undefined) parts.push(`HTTP ${e.status}`);
    if (e.durationMs !== undefined) parts.push(`${Math.round(e.durationMs)}ms`);
    if (e.code) parts.push(`mã ${e.code}`);
    let line = parts.join("  ") + `\n    ${e.message}`;
    if (e.calculationId) line += `\n    mã lần tính: ${e.calculationId}`;
    if (e.detail && Object.keys(e.detail).length > 0) {
      line += `\n    chi tiết: ${JSON.stringify(e.detail)}`;
    }
    return line;
  });

  return [...header, ...lines, "", "=== HẾT ==="].join("\n");
}
