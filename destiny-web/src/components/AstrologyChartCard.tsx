import type { EvidenceDto, LabelRegistries } from "@/lib/types";

/**
 * Renders the Western astrology chart (Sun, Midheaven, Ascendant, Whole Sign
 * houses) as hard data, reconstructed from `WesternAstrologyEngine`'s
 * evidence — same approach and same reasons as {@code BaziChartCard}: the
 * chart shown here cannot drift from the audit trail, at the cost of reading
 * technical names (`ARIES`, `HOUSE_1`) through `labels`.
 *
 * Chart construction only, same as Bát Tự's chart half — the blocked
 * sections at the bottom are the interpretation, and they are absent on
 * purpose (R5/R6).
 */

const POINT_ORDER = ["SUN", "MIDHEAVEN", "ASCENDANT"] as const;
const POINT_LABELS: Record<(typeof POINT_ORDER)[number], string> = {
  SUN: "Mặt Trời",
  MIDHEAVEN: "Thiên Đỉnh (MC)",
  ASCENDANT: "Cung Mọc (Ascendant)",
};

const HOUSE_ORDER = Array.from({ length: 12 }, (_, i) => `HOUSE_${i + 1}`);

function label(labels: LabelRegistries, type: string, key: unknown): string {
  if (typeof key !== "string") return "—";
  return labels[type]?.[key] ?? key;
}

function factOf(evidence: EvidenceDto[], ruleId: string): Record<string, unknown> | null {
  return evidence.find((e) => e.ruleId === ruleId)?.fact ?? null;
}

function asStringArray(value: unknown): string[] {
  return Array.isArray(value) ? value.filter((v): v is string => typeof v === "string") : [];
}

function degreesText(fact: Record<string, unknown>): string {
  const value = Number(fact.degreesIntoSign ?? NaN);
  return Number.isFinite(value) ? `${value.toFixed(2)}°` : "—";
}

export function AstrologyChartCard({
  evidence,
  labels,
}: {
  evidence: EvidenceDto[];
  labels: LabelRegistries;
}) {
  const astroEvidence = evidence.filter((e) => e.engine === "WESTERN_ASTROLOGY");
  if (astroEvidence.length === 0) {
    return null;
  }

  const points = POINT_ORDER.map((name) => ({
    name,
    fact: factOf(astroEvidence, `ASTROLOGY_${name}`),
  })).filter((p) => p.fact !== null);

  const houses = factOf(astroEvidence, "ASTROLOGY_WHOLE_SIGN_HOUSES");
  const frame = factOf(astroEvidence, "ASTROLOGY_FRAME");
  const blocked = astroEvidence.filter((e) => e.ruleId.startsWith("ASTROLOGY_BLOCKED_"));

  return (
    <section className="space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div>
        <h2 className="text-lg font-semibold text-slate-900">✨ Lá số Chiêm tinh phương Tây</h2>
        <p className="mt-1 text-xs text-slate-500">
          Dữ liệu tính toán tất định (Mặt Trời, góc chiếu, hệ nhà). Đây là{" "}
          <span className="font-medium">lá số</span>, chưa phải lời luận giải — phần luận giải xem
          ở cuối mục này.
        </p>
      </div>

      {frame && (
        <p className="rounded-md bg-slate-50 px-3 py-2 text-xs text-slate-600">
          Hệ hoàng đạo: <span className="font-medium">Nhiệt đới (Tropical)</span> · Hệ nhà:{" "}
          <span className="font-medium">Whole Sign</span> · Độ nghiêng hoàng đạo:{" "}
          <span className="font-medium tabular-nums">
            {Number(frame.obliquityDegrees ?? NaN).toFixed(4)}°
          </span>
        </p>
      )}

      <div className="overflow-x-auto">
        <table className="w-full min-w-[28rem] border-collapse text-sm">
          <caption className="sr-only">Vị trí Mặt Trời, Thiên Đỉnh và Cung Mọc trên hoàng đạo</caption>
          <thead>
            <tr className="text-left text-xs uppercase tracking-wide text-slate-500">
              <th scope="col" className="w-40 py-2 font-medium">
                &nbsp;
              </th>
              <th scope="col" className="py-2 font-medium">
                Cung hoàng đạo
              </th>
              <th scope="col" className="py-2 font-medium">
                Độ trong cung
              </th>
              <th scope="col" className="py-2 font-medium">
                Nhà (Whole Sign)
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {points.map(({ name, fact }) => (
              <tr key={name}>
                <th scope="row" className="py-2 text-left text-xs font-medium text-slate-500">
                  {POINT_LABELS[name]}
                </th>
                <td className="py-2 text-base font-semibold text-slate-900">
                  {label(labels, "ZodiacSign", fact!.sign)}
                </td>
                <td className="py-2 tabular-nums text-slate-700">{degreesText(fact!)}</td>
                <td className="py-2 text-slate-700">
                  {fact!.house ? label(labels, "AstrologicalHouse", fact!.house) : "—"}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {houses && (
        <div>
          <h3 className="text-sm font-semibold text-slate-900">12 nhà (Whole Sign)</h3>
          <p className="mb-2 text-xs text-slate-500">
            Mỗi nhà là một cung hoàng đạo trọn vẹn, bắt đầu từ cung của Cung Mọc — không cần chia
            theo thời gian nên đúng ở mọi vĩ độ, kể cả vùng cực.
          </p>
          <div className="grid grid-cols-2 gap-x-6 gap-y-1 text-sm sm:grid-cols-3">
            {HOUSE_ORDER.map((house) => (
              <div key={house} className="flex justify-between gap-2 border-b border-slate-100 py-1">
                <dt className="text-slate-500">{label(labels, "AstrologicalHouse", house)}</dt>
                <dd className="font-medium text-slate-900">
                  {label(labels, "ZodiacSign", houses[house])}
                </dd>
              </div>
            ))}
          </div>
        </div>
      )}

      {blocked.length > 0 && (
        <div>
          <h3 className="text-sm font-semibold text-slate-900">
            Phần luận giải chưa được cung cấp ({blocked.length})
          </h3>
          <p className="mb-2 text-xs text-slate-500">
            Những phần dưới đây bị bỏ trống có chủ đích, không phải do lỗi hay thiếu dữ liệu của
            bạn.
          </p>
          <ul className="space-y-2">
            {blocked.map((item) => (
              <li key={item.evidenceId} className="rounded-md border border-amber-200 bg-amber-50 p-3">
                <div className="flex flex-wrap items-baseline gap-2">
                  <span className="text-sm font-medium text-amber-900">
                    {String(item.fact.displayNameVi ?? "")}
                  </span>
                  <span
                    title={`Mục nghiên cứu ${String(item.fact.researchId ?? "")}`}
                    className="rounded-full bg-amber-200 px-2 py-0.5 text-xs font-medium text-amber-900"
                  >
                    Cần xác minh thuật toán · {String(item.fact.researchId ?? "")}
                  </span>
                </div>
                <p className="mt-1 text-xs text-amber-900">{String(item.fact.reasonVi ?? "")}</p>
                {asStringArray(item.fact.knownVariants).length > 0 && (
                  <p className="mt-1 text-xs text-amber-800">
                    Các cách làm khác nhau đang tồn tại:{" "}
                    {asStringArray(item.fact.knownVariants).join(" · ")}
                  </p>
                )}
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  );
}
