import type { EvidenceDto } from "@/lib/types";

/**
 * Những mục một engine cố ý để trống vì thuật toán chưa được xác minh
 * (`BlockedSection`, ADR D7).
 *
 * <p>Ba thẻ lá số — Bát Tự, Chiêm tinh, Kinh Dịch — từng mang ba bản sao gần
 * như y hệt của khối này. Gộp lại không chỉ để bớt lặp: nó gộp cả *cách nói*
 * về việc thiếu, vốn là thứ nhạy cảm nhất trên trang này. Ba bản sao có thể
 * phân kỳ, và bản nào diễn đạt lệch đi thì sẽ nói sai về điều hệ thống biết
 * hay không biết.
 *
 * <p>Mã nghiên cứu (`R1`, `R12`) không còn nằm trong chữ của badge. Nó vẫn
 * hiện, nhưng có nhãn hẳn hoi ở dòng riêng — trước đây người dùng đọc được
 * "Cần xác minh thuật toán · R1" và không có gì trên trang nói R1 là cái gì.
 */
export function BlockedSectionList({ items }: { items: EvidenceDto[] }) {
  if (items.length === 0) return null;

  return (
    <div>
      <h3 className="text-sm font-semibold text-slate-900">
        Phần luận giải chưa được cung cấp ({items.length})
      </h3>
      <p className="mb-2 text-xs text-slate-500">
        Những phần dưới đây bị bỏ trống có chủ đích, không phải do lỗi hay thiếu dữ liệu của bạn.
      </p>
      <ul className="space-y-2">
        {items.map((item) => (
          <li key={item.evidenceId} className="rounded-md border border-amber-200 bg-amber-50 p-3">
            <div className="flex flex-wrap items-baseline gap-2">
              <span className="text-sm font-medium text-amber-900">
                {String(item.fact.displayNameVi ?? "")}
              </span>
              <span className="rounded-full bg-amber-200 px-2 py-0.5 text-xs font-medium text-amber-900">
                Cần xác minh thuật toán
              </span>
            </div>
            <p className="mt-1 text-xs text-amber-900">{String(item.fact.reasonVi ?? "")}</p>
            {asStringArray(item.fact.knownVariants).length > 0 && (
              <p className="mt-1 text-xs text-amber-800">
                Các cách làm khác nhau đang tồn tại:{" "}
                {asStringArray(item.fact.knownVariants).join(" · ")}
              </p>
            )}
            {item.fact.researchId != null && (
              <p className="mt-1 text-[11px] text-amber-700">
                Mã mục nghiên cứu trong tài liệu dự án:{" "}
                <code className="font-mono">{String(item.fact.researchId)}</code>
              </p>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

function asStringArray(value: unknown): string[] {
  return Array.isArray(value) ? value.filter((v): v is string => typeof v === "string") : [];
}
