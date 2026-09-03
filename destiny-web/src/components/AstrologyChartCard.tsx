import type { EvidenceDto, LabelRegistries } from "@/lib/types";
import { BlockedSectionList } from "./BlockedSectionList";

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
  MIDHEAVEN: "Thiên Đỉnh",
  ASCENDANT: "Cung Mọc",
};

const HOUSE_ORDER = Array.from({ length: 12 }, (_, i) => `HOUSE_${i + 1}`);

function label(labels: LabelRegistries, type: string, key: unknown): string {
  if (typeof key !== "string") return "—";
  return labels[type]?.[key] ?? key;
}

function factOf(evidence: EvidenceDto[], ruleId: string): Record<string, unknown> | null {
  return evidence.find((e) => e.ruleId === ruleId)?.fact ?? null;
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
  const houseThemes = factOf(astroEvidence, "ASTROLOGY_HOUSE_THEMES");
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
        {/*
          Nêu một lần ở đây thay vì gắn "(MC)" và "(Ascendant)" vào từng nhãn.
          Người đọc tài liệu chiêm tinh nước ngoài cần hai ký hiệu này, nhưng
          chúng không phải là tên của điểm — chúng là chú thích.
        */}
        <p className="mt-2 text-[11px] text-slate-400">
          Ký hiệu thường gặp trong tài liệu nước ngoài: Thiên Đỉnh là MC (Midheaven), Cung Mọc là
          AC (Ascendant).
        </p>
      </div>

      <PointMeanings points={points} />

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

      {houseThemes && <HouseThemes fact={houseThemes} labels={labels} houses={houses} />}

      <BlockedSectionList items={blocked} />
    </section>
  );
}

/**
 * Từ khóa của cung, và ý nghĩa của chính điểm đó.
 *
 * <p>Nội dung soạn sẵn ở `AstrologyMeanings` phía backend, đi ra qua evidence.
 * Trước đây thẻ này chỉ có tọa độ: Mặt Trời ở Kim Ngưu 23,94 độ, nhà 12 — đúng
 * và không nói gì cả.
 *
 * <p>Thiên Đỉnh có từ khóa cung nhưng **không** có đoạn nói về chính nó, và
 * chỗ đó ghi rõ là chưa soạn chứ không bịa một câu cho đủ hình thức.
 */
function PointMeanings({
  points,
}: {
  points: { name: string; fact: Record<string, unknown> | null }[];
}) {
  const withMeaning = points.filter((p) => p.fact !== null);
  if (withMeaning.length === 0) return null;

  return (
    <div>
      <h3 className="text-sm font-semibold text-slate-900">Ba điểm này nói về điều gì</h3>
      <ul className="mt-2 space-y-2">
        {withMeaning.map(({ name, fact }) => {
          const keywords = Array.isArray(fact!.signKeywordsVi)
            ? (fact!.signKeywordsVi as unknown[]).filter((k): k is string => typeof k === "string")
            : [];
          const pointMeaning =
            typeof fact!.pointMeaningVi === "string" ? fact!.pointMeaningVi : null;
          return (
            <li key={name} className="rounded-md border border-slate-200 bg-slate-50 p-3">
              <p className="text-sm font-medium text-slate-900">{POINT_LABELS[name as (typeof POINT_ORDER)[number]] ?? name}</p>
              {pointMeaning ? (
                <p className="mt-1 text-sm leading-relaxed text-slate-700">{pointMeaning}</p>
              ) : (
                <p className="mt-1 text-xs text-slate-500">
                  Chưa có đoạn diễn giải riêng cho điểm này — phần nội dung đã soạn và qua rà soát
                  chỉ bao gồm Mặt Trời và Cung Mọc. Bỏ trống có chủ đích, không phải lỗi.
                </p>
              )}
              {keywords.length > 0 && (
                <p className="mt-1.5 text-xs text-slate-600">
                  Từ khóa của cung: <span className="font-medium">{keywords.join(" · ")}</span>
                </p>
              )}
            </li>
          );
        })}
      </ul>
    </div>
  );
}

/** Chủ đề của mười hai Nhà, kèm cung tương ứng trong lá số này. */
function HouseThemes({
  fact,
  labels,
  houses,
}: {
  fact: Record<string, unknown>;
  labels: LabelRegistries;
  houses: Record<string, unknown> | null;
}) {
  const themes = fact.houseThemesVi;
  if (typeof themes !== "object" || themes === null) return null;

  return (
    <div>
      <h3 className="text-sm font-semibold text-slate-900">Mười hai Nhà nói về điều gì</h3>
      {typeof fact.houseSystemNoteVi === "string" && (
        <p className="mb-2 text-xs text-slate-500">{fact.houseSystemNoteVi}</p>
      )}
      <ul className="grid gap-1.5 sm:grid-cols-2">
        {HOUSE_ORDER.map((house) => {
          const theme = (themes as Record<string, unknown>)[house];
          if (typeof theme !== "string") return null;
          const sign = houses?.[house];
          return (
            <li key={house} className="rounded-md border border-slate-200 bg-white p-2.5">
              <div className="flex flex-wrap items-baseline gap-2">
                <span className="text-sm font-medium text-slate-900">
                  {label(labels, "AstrologicalHouse", house)}
                </span>
                {typeof sign === "string" && (
                  <span className="text-xs text-slate-500">
                    {label(labels, "ZodiacSign", sign)}
                  </span>
                )}
              </div>
              <p className="mt-0.5 text-xs leading-relaxed text-slate-700">{theme}</p>
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
