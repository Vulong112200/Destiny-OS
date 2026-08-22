import type { EvidenceDto, LabelRegistries } from "@/lib/types";

/**
 * Renders the Bát Trạch (Phong Thủy) profile as hard data, reconstructed from
 * the evidence `FengShuiKuaEngine` emits.
 *
 * <p>Same approach as `BaziChartCard`, for the same reason: a table built from
 * the evidence record cannot drift away from what the audit trail says. The
 * cost is that facts hold technical names, which is what `labels` is for.
 *
 * <p>Three states, and the middle one is the interesting one:
 * - the two year conventions agree → Kua, group, eight directions;
 * - they disagree → both Kua numbers, no direction table, and why;
 * - a facing direction was assessed → that assessment, shown first.
 */

const COMPASS_ORDER = [
  "NORTH",
  "NORTHEAST",
  "EAST",
  "SOUTHEAST",
  "SOUTH",
  "SOUTHWEST",
  "WEST",
  "NORTHWEST",
] as const;

/** Relations the tradition classes as cát. Used only for tone, never to re-rank. */
const AUSPICIOUS = new Set(["SINH_KHI", "DIEN_NIEN", "THIEN_Y", "PHUC_VI"]);

function label(labels: LabelRegistries, type: string, key: unknown): string {
  if (typeof key !== "string") return "—";
  return labels[type]?.[key] ?? key;
}

function factOf(evidence: EvidenceDto[], ruleId: string): Record<string, unknown> | null {
  return evidence.find((e) => e.ruleId === ruleId)?.fact ?? null;
}

export function BatTrachCard({
  evidence,
  labels,
}: {
  evidence: EvidenceDto[];
  labels: LabelRegistries;
}) {
  const own = evidence.filter((e) => e.engine === "FENGSHUI_KUA");
  if (own.length === 0) {
    return null;
  }

  const kua = factOf(own, "FENGSHUI_KUA_NUMBER");
  const directions = factOf(own, "FENGSHUI_BAT_TRACH_DIRECTIONS");
  const facing = factOf(own, "FENGSHUI_FACING_ASSESSMENT");
  const agree = kua?.boundaryConventionsAgree === true;

  return (
    <section className="space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div>
        <h2 className="text-lg font-semibold text-slate-900">Phong Thủy — Bát Trạch</h2>
        <p className="mt-1 text-xs text-slate-500">
          Cung phi và tám hướng theo Bát Biến Du Niên. Chỉ dùng phái{" "}
          <span className="font-medium">Bát Trạch</span> — không trộn Phi Tinh hay Huyền Không.
        </p>
      </div>

      {!agree && kua && (
        <div className="rounded-md bg-amber-50 px-3 py-2 text-xs text-amber-900">
          <p className="font-medium">Hai quy ước ranh giới năm cho ra cung phi khác nhau</p>
          <p className="mt-1">
            Theo <span className="font-medium">Lập Xuân</span> (cách cổ điển): cung{" "}
            <span className="font-medium">{label(labels, "Trigram", kua.trigram)}</span> (số{" "}
            {String(kua.kuaNumber)}), tính theo năm {String(kua.lapXuanYear)}. Theo{" "}
            <span className="font-medium">Tết</span> (phổ biến ở Việt Nam): cung{" "}
            <span className="font-medium">{label(labels, "Trigram", kua.trigramByTet)}</span> (số{" "}
            {String(kua.kuaNumberByTet)}), tính theo năm {String(kua.tetYear)}.
          </p>
          <p className="mt-1">
            Chưa có nguồn nào phân định giữa hai quy ước, nên hệ thống không tự chọn giúp bạn,
            không đưa ra bảng tám hướng, và không góp tín hiệu nào vào kết luận tổng hợp.
          </p>
        </div>
      )}

      {agree && kua && (
        <p className="rounded-md bg-slate-50 px-3 py-2 text-xs text-slate-600">
          Cung phi:{" "}
          <span className="font-medium text-slate-900">
            {label(labels, "Trigram", kua.trigram)}
          </span>{" "}
          (số {String(kua.kuaNumber)}, {label(labels, "FiveElement", kua.element)}) ·{" "}
          <span className="font-medium">{label(labels, "TrigramGroup", kua.group)}</span> · tính
          theo năm {String(kua.lapXuanYear)}, hai quy ước Lập Xuân và Tết trùng nhau
        </p>
      )}

      {facing && (
        <div
          className={`rounded-md border px-3 py-2 text-sm ${
            facing.auspicious === true
              ? "border-emerald-200 bg-emerald-50 text-emerald-900"
              : "border-rose-200 bg-rose-50 text-rose-900"
          }`}
        >
          <p>
            Hướng đang xét:{" "}
            <span className="font-medium">
              {label(labels, "CompassDirection", facing.facingDirection)}
            </span>{" "}
            →{" "}
            <span className="font-medium">
              {label(labels, "BatTrachRelation", facing.relation)}
            </span>
          </p>
        </div>
      )}

      {directions && (
        <div className="overflow-x-auto">
          <table className="w-full min-w-[26rem] border-collapse text-sm">
            <caption className="sr-only">
              Tám hướng theo cung phi: mỗi hướng ứng với một du niên tốt hoặc xấu
            </caption>
            <thead>
              <tr className="text-left text-xs uppercase tracking-wide text-slate-500">
                <th scope="col" className="w-28 py-2 font-medium">
                  Hướng
                </th>
                <th scope="col" className="py-2 font-medium">
                  Du niên
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {COMPASS_ORDER.map((direction) => {
                const relation = directions[direction];
                const good = typeof relation === "string" && AUSPICIOUS.has(relation);
                return (
                  <tr key={direction}>
                    <th
                      scope="row"
                      className="py-2 text-left text-sm font-medium text-slate-700"
                    >
                      {label(labels, "CompassDirection", direction)}
                    </th>
                    <td className="py-2">
                      <span
                        className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-sm font-medium ${
                          good
                            ? "bg-emerald-100 text-emerald-800"
                            : "bg-rose-100 text-rose-800"
                        }`}
                      >
                        {label(labels, "BatTrachRelation", relation)}
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
          <p className="mt-2 text-xs text-slate-500">
            Bốn hướng cát và bốn hướng hung, luôn nằm trong nhóm Đông tứ / Tây tứ của bạn. Thứ tự
            trong bảng là theo la bàn, không phải theo mức tốt — xếp hạng là việc của người đọc.
          </p>
        </div>
      )}

      {!facing && agree && (
        <p className="text-xs text-slate-500">
          Chưa có hướng nhà/phòng để đối chiếu, nên chưa đánh giá được hướng cụ thể nào và Bát
          Trạch chưa góp tín hiệu vào kết luận tổng hợp. Nhập hướng ở Trung tâm quyết định để có
          phần đánh giá này.
        </p>
      )}
    </section>
  );
}
