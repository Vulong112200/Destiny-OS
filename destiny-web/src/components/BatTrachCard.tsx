import type { EvidenceDto, LabelRegistries } from "@/lib/types";
import { BatQuaiCompass } from "./charts/BatQuaiCompass";
import { BlockedSectionList } from "./BlockedSectionList";
import { AUSPICIOUS_RELATIONS, COMPASS_ORDER } from "@/lib/batTrach";

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
  const meanings = factOf(own, "FENGSHUI_RELATION_MEANINGS");
  const blocked = own.filter((e) => e.ruleId.startsWith("FENGSHUI_BLOCKED_"));
  const agree = kua?.boundaryConventionsAgree === true;

  return (
    <section className="space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div>
        <h2 className="text-lg font-semibold text-slate-900">🧭 Phong Thủy — Bát Trạch</h2>
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
        <BatQuaiCompass
          directions={directions}
          facingDirection={typeof facing?.facingDirection === "string" ? facing.facingDirection : null}
          labels={labels}
        />
      )}

      {directions && (
        <details className="rounded-md border border-slate-200 bg-slate-50 p-3">
          <summary className="cursor-pointer text-xs font-medium text-slate-600">
            Xem dạng bảng
          </summary>
          <div className="mt-3 overflow-x-auto">
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
                const good = typeof relation === "string" && AUSPICIOUS_RELATIONS.has(relation);
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
        </details>
      )}

      {meanings && (
        <RelationMeanings fact={meanings} labels={labels} facing={facing} />
      )}

      <BlockedSectionList items={blocked} />

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

/**
 * Mỗi du niên nghĩa là gì, và nó nói về khía cạnh nào của đời sống.
 *
 * <p>Đây là phần thẻ này thiếu suốt: bảng hướng đã có, badge màu đã có, nhưng
 * không một chữ nào nói Sinh Khí là cái gì — nên nó không khác một trang bói
 * toán tra được trong năm phút. Nội dung lấy từ `BatTrachRelationMeanings` ở
 * backend, không phải viết ở đây, để lời văn và tín hiệu không thể lệch nhau.
 */
function RelationMeanings({
  fact,
  labels,
  facing,
}: {
  fact: Record<string, unknown>;
  labels: LabelRegistries;
  facing: Record<string, unknown> | null;
}) {
  const map = fact.relationMeanings;
  if (typeof map !== "object" || map === null) return null;

  const entries = Object.entries(map as Record<string, unknown>);
  const facingRelation = typeof facing?.relation === "string" ? facing.relation : null;

  // Hướng đang xét lên đầu: đó là câu trả lời cho câu hỏi người dùng vừa đặt.
  entries.sort((a, b) => {
    if (a[0] === facingRelation) return -1;
    if (b[0] === facingRelation) return 1;
    const aGood = AUSPICIOUS_RELATIONS.has(a[0]) ? 0 : 1;
    const bGood = AUSPICIOUS_RELATIONS.has(b[0]) ? 0 : 1;
    return aGood - bGood;
  });

  return (
    <div>
      <h3 className="text-sm font-semibold text-slate-900">Mỗi du niên nói về điều gì</h3>
      <p className="mb-2 text-xs text-slate-500">
        Ý nghĩa khái quát của từng quan hệ, kèm những khía cạnh đời sống mà truyền thống gắn với
        nó. Đây là đặc điểm của quan hệ, không phải lời đoán dành riêng cho bạn.
      </p>
      <ul className="grid gap-2 sm:grid-cols-2">
        {entries.map(([relation, value]) => {
          const m = value as { natureVi?: string; tendencyVi?: string; domainsVi?: unknown };
          const good = AUSPICIOUS_RELATIONS.has(relation);
          const isFacing = relation === facingRelation;
          const domains = Array.isArray(m.domainsVi)
            ? m.domainsVi.filter((d): d is string => typeof d === "string")
            : [];
          return (
            <li
              key={relation}
              className={`rounded-md border p-3 ${
                isFacing
                  ? "border-slate-400 bg-white shadow-sm"
                  : good
                    ? "border-emerald-200 bg-emerald-50/50"
                    : "border-rose-200 bg-rose-50/50"
              }`}
            >
              <div className="flex flex-wrap items-baseline gap-2">
                <span className="text-sm font-medium text-slate-900">
                  {label(labels, "BatTrachRelation", relation)}
                </span>
                {isFacing && (
                  <span className="rounded-full bg-slate-900 px-2 py-0.5 text-[10px] font-medium text-white">
                    hướng bạn đang xét
                  </span>
                )}
              </div>
              {m.natureVi && <p className="mt-0.5 text-xs text-slate-500">{m.natureVi}</p>}
              {m.tendencyVi && (
                <p className="mt-1 text-sm leading-relaxed text-slate-700">{m.tendencyVi}</p>
              )}
              {domains.length > 0 && (
                <p className="mt-1.5 text-xs text-slate-600">
                  Khía cạnh liên quan: <span className="font-medium">{domains.join(" · ")}</span>
                </p>
              )}
            </li>
          );
        })}
      </ul>
      {typeof fact.sourceNoteVi === "string" && (
        <p className="mt-2 text-[11px] text-slate-400">{fact.sourceNoteVi}</p>
      )}
    </div>
  );
}
