import type { LabelRegistries } from "@/lib/types";
import { factKeyLabelVi } from "@/lib/factKeys";
import { tarotPositionLabelVi } from "@/lib/tarotCards";

/**
 * Một `EvidenceDto.fact` được trình bày để đọc được, với bản JSON thô giữ
 * nguyên bên dưới.
 *
 * <p>Trang kết quả trước đây đổ thẳng `JSON.stringify(fact, null, 2)` vào một
 * thẻ `<pre>`. Khối đó **không bị bỏ đi** ở đây, và cố ý như vậy: mục "Vì sao
 * có kết quả này?" là dấu vết kiểm toán mà `CLAUDE.md` §12 yêu cầu, và cũng là
 * thứ người dùng sẽ copy khi cần báo lỗi. Nó chỉ bị hạ xuống một lớp, sau khi
 * cùng dữ liệu ấy đã được trình bày bằng tiếng Việt ở trên.
 */
export function RawFactDetails({
  fact,
  labels,
}: {
  fact: Record<string, unknown>;
  labels?: LabelRegistries;
}) {
  const entries = Object.entries(fact);
  if (entries.length === 0) {
    return <p className="text-xs text-slate-500">Mục bằng chứng này không kèm dữ liệu chi tiết.</p>;
  }

  return (
    <div className="space-y-2">
      <dl className="grid grid-cols-[minmax(7rem,auto)_1fr] gap-x-3 gap-y-1 text-xs">
        {entries.map(([key, value]) => (
          <div key={key} className="contents">
            <dt className="text-slate-500" title={key}>
              {keyLabel(key, labels)}
            </dt>
            <dd className="min-w-0 break-words text-slate-700">
              <FactValue value={value} labels={labels} contextKey={key} />
            </dd>
          </div>
        ))}
      </dl>

      <details className="text-xs">
        <summary className="cursor-pointer text-slate-400 hover:text-slate-600">
          Dữ liệu thô (dành cho kiểm tra kỹ thuật)
        </summary>
        <pre className="mt-1 max-h-64 overflow-auto rounded bg-slate-100 p-2 text-[11px] text-slate-600">
          {JSON.stringify(fact, null, 2)}
        </pre>
      </details>
    </div>
  );
}

/**
 * Một giá trị trong `fact`.
 *
 * <p>Chuỗi được tra qua bảng nhãn trước khi hiển thị, vì rất nhiều giá trị ở
 * đây là tên enum (`GIAP`, `SINH_KHI`, `ARIES`) — đúng loại chữ mà §9 cấm để
 * trần. Không tra được thì giữ nguyên, không biến thành gạch ngang.
 */
/**
 * Nhãn cho một khóa.
 *
 * <p>Không phải khóa nào cũng là tên trường: bản đồ 12 nhà có khóa là chính
 * tên enum (`HOUSE_11`), và bản đồ tám hướng có khóa là `NORTH`, `SOUTHEAST`.
 * Nên sau khi tra từ điển tên trường thì phải thử tiếp bảng nhãn enum, không
 * thì người dùng đọc được `HOUSE_11` ngay bên cạnh `Bạch Dương`.
 */
function keyLabel(key: string, labels?: LabelRegistries): string {
  const known = factKeyLabelVi(key);
  if (known !== key) return known;
  return translateEnumish(key, labels);
}

function FactValue({
  value,
  labels,
  contextKey,
}: {
  value: unknown;
  labels?: LabelRegistries;
  /** Tên khóa chứa giá trị này, để chọn đúng cách dịch. */
  contextKey?: string;
}) {
  if (value === null || value === undefined) {
    return <span className="text-slate-400">—</span>;
  }
  if (typeof value === "boolean") {
    return <>{value ? "Có" : "Không"}</>;
  }
  if (typeof value === "number") {
    return <>{value}</>;
  }
  if (typeof value === "string") {
    return <>{translateValue(value, labels, contextKey)}</>;
  }
  if (Array.isArray(value)) {
    if (value.length === 0) return <span className="text-slate-400">(trống)</span>;
    if (value.every((v) => typeof v === "string" || typeof v === "number")) {
      return <>{value.map((v) => translateValue(String(v), labels, contextKey)).join(" · ")}</>;
    }
    return (
      <ul className="space-y-1">
        {value.map((v, i) => (
          <li key={i} className="rounded bg-slate-50 p-1.5">
            {typeof v === "object" && v !== null ? (
              <RawFactDetails fact={v as Record<string, unknown>} labels={labels} />
            ) : (
              translateValue(String(v), labels, contextKey)
            )}
          </li>
        ))}
      </ul>
    );
  }
  if (typeof value === "object") {
    return (
      <div className="rounded bg-slate-50 p-1.5">
        <RawFactDetails fact={value as Record<string, unknown>} labels={labels} />
      </div>
    );
  }
  return <>{String(value)}</>;
}

/**
 * Vài enum chưa có bảng nhãn ở backend.
 *
 * <p>`TarotOrientation`, cách gieo quẻ và cách lá bài được chọn đều không nằm
 * trong `GET /api/v1/labels`, nên chúng lọt ra dưới dạng `REVERSED`,
 * `THREE_COINS`, `TOP_OF_DECK`. Sửa đúng gốc là thêm registry ở
 * `VietnameseLabels.java`; tới lúc đó bảng nhỏ này lấp chỗ trống, và phải
 * được xoá đi khi backend có nhãn thật.
 */
const LOCAL_VALUE_VI: Record<string, string> = {
  UPRIGHT: "Xuôi",
  REVERSED: "Ngược",
  TOP_OF_DECK: "Lấy từ trên bộ đã xào xuống",
  PICKED_BY_QUERENT: "Người xem tự chọn",
  THREE_COINS: "Gieo ba đồng tiền",
  YARROW: "Cỏ thi",
  MAI_HOA_NUMBER: "Mai Hoa theo số",
  MAI_HOA_TIME: "Mai Hoa theo giờ",
  MAJOR: "Ẩn chính (Major Arcana)",
  MINOR: "Ẩn phụ (Minor Arcana)",
  MALE: "Nam",
  FEMALE: "Nữ",
  NORTH: "Bắc",
  SOUTH: "Nam",
  UNKNOWN: "Không rõ",
};

function translateValue(value: string, labels?: LabelRegistries, contextKey?: string): string {
  const local = LOCAL_VALUE_VI[value];
  if (local) return local;

  // Bảng nhãn của backend trước, vì khóa `position` không chỉ thuộc về Tarot:
  // một trụ Bát Tự cũng dùng nó, với giá trị YEAR/MONTH/DAY/HOUR nằm trong
  // registry `PillarPosition`. Thử bảng vị trí bài trước sẽ dịch "Trụ Năm"
  // thành một vị trí trải bài không tồn tại.
  const fromRegistry = translateEnumish(value, labels);
  if (fromRegistry !== value) return fromRegistry;

  if (contextKey === "position" || contextKey === "positions") {
    return tarotPositionLabelVi(value);
  }
  return value;
}

/**
 * Tra một chuỗi qua **mọi** registry nhãn.
 *
 * <p>`fact` không nói khóa nào thuộc enum nào, nên không thể tra đúng một
 * registry như những chỗ khác trong web. Chỉ thử với chuỗi trông như tên enum
 * (VIẾT_HOA_GẠCH_DƯỚI) để một câu tiếng Việt hay một ngày ISO không bao giờ bị
 * đem đi đối chiếu nhầm.
 */
function translateEnumish(value: string, labels?: LabelRegistries): string {
  if (!labels || !/^[A-Z][A-Z0-9_]*$/.test(value)) return value;
  for (const registry of Object.values(labels)) {
    const hit = registry?.[value];
    if (hit) return hit;
  }
  return value;
}
