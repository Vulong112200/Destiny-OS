import type { EvidenceDto, LabelRegistries, NumerologyNumberFact } from "@/lib/types";
import { LabeledBadge } from "./LabeledBadge";

/**
 * Renders the five Pythagorean numbers `NumerologyEngine` computes, plus the
 * already-authored Vietnamese interpretation for each
 * (`NumerologyNumberMeanings.java`) that `NumerologyEngine.buildEvidence` now
 * exposes in `evidence.fact.meaning`.
 *
 * Mirrors `TarotResultCard`'s contract for the same underlying situation
 * (hand-authored static text, not AI-generated, not computed at runtime):
 * hard data (the number) shown before the reading, a visually distinct
 * "Giải thích" panel, and a source-citation line at the end. Renders nothing
 * when Numerology did not take part in this run, matching every other engine
 * card in `ResultView`.
 */

const NUMBER_ORDER = [
  "NUMEROLOGY_LIFE_PATH",
  "NUMEROLOGY_EXPRESSION",
  "NUMEROLOGY_SOUL_URGE",
  "NUMEROLOGY_PERSONALITY",
  "NUMEROLOGY_BIRTHDAY",
] as const;

/**
 * Vietnamese names for each number type. A name TRANSLATION (standard
 * vocabulary already converged in Vietnamese numerology writing), not
 * interpretive content — the same distinction `tarotCards.ts` draws for card
 * names versus `TarotCardMeaning` text. The interpretation itself always
 * comes from `evidence.fact.meaning`, never authored here.
 */
const NUMBER_LABELS_VI: Record<string, string> = {
  NUMEROLOGY_LIFE_PATH: "Số Chủ Đạo (Life Path)",
  NUMEROLOGY_EXPRESSION: "Số Sứ Mệnh (Expression)",
  NUMEROLOGY_SOUL_URGE: "Số Linh Hồn (Soul Urge)",
  NUMEROLOGY_PERSONALITY: "Số Nhân Cách (Personality)",
  NUMEROLOGY_BIRTHDAY: "Số Ngày Sinh (Birthday)",
};

function label(labels: LabelRegistries, type: string, key: unknown): string {
  if (typeof key !== "string") return "—";
  return labels[type]?.[key] ?? key;
}

export function NumerologyResultCard({
  evidence,
  labels,
}: {
  evidence: EvidenceDto[];
  labels: LabelRegistries;
}) {
  const numbers = NUMBER_ORDER.map((ruleId) => ({
    ruleId,
    ev: evidence.find((e) => e.engine === "NUMEROLOGY_PYTHAGOREAN" && e.ruleId === ruleId),
  })).filter((n): n is { ruleId: (typeof NUMBER_ORDER)[number]; ev: EvidenceDto } => n.ev !== undefined);

  if (numbers.length === 0) {
    return null;
  }

  return (
    <section className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div>
        <h2 className="text-lg font-semibold text-slate-900">🔢 Thần số học (Pythagoras)</h2>
        <p className="mt-1 text-xs text-slate-500">
          Dữ liệu tính toán tất định từ tên và ngày sinh. Phần luận giải cho từng con số ở ngay
          dưới mỗi số.
        </p>
      </div>

      <div className="mt-4 space-y-5">
        {numbers.map(({ ruleId, ev }) => {
          const fact = ev.fact as unknown as NumerologyNumberFact;
          const meaning = fact.meaning;
          const polarity = meaning
            ? { technical: meaning.polarity, labelVi: label(labels, "Polarity", meaning.polarity) }
            : null;

          return (
            <div key={ev.evidenceId} className="border-t border-slate-100 pt-4 first:border-t-0 first:pt-0">
              <div className="flex flex-wrap items-baseline justify-between gap-2">
                <h3 className="text-sm font-semibold text-slate-900">{NUMBER_LABELS_VI[ruleId]}</h3>
                <div className="flex items-center gap-2">
                  {fact.isMasterNumber && (
                    <span
                      title="Số Bậc Thầy (Master Number) — không rút gọn về 1 chữ số"
                      className="rounded-full bg-amber-100 px-2 py-0.5 text-xs font-medium text-amber-800"
                    >
                      Số Bậc Thầy
                    </span>
                  )}
                  <span className="text-2xl font-bold text-slate-900">{fact.value}</span>
                </div>
              </div>

              {fact.normalizedName && (
                <p className="mt-0.5 text-xs text-slate-400">
                  Tính từ tên đã chuẩn hoá: {fact.normalizedName}
                </p>
              )}

              {meaning && (
                <div className="mt-2 rounded-lg border border-teal-200 bg-teal-50 p-3">
                  <div className="mb-1.5 flex flex-wrap items-center justify-between gap-2">
                    <h4 className="text-xs font-semibold uppercase tracking-wide text-teal-700">
                      Giải thích
                    </h4>
                    {polarity && <LabeledBadge value={polarity} />}
                  </div>
                  <p className="text-sm text-slate-800">{meaning.text}</p>
                  {meaning.keywords.length > 0 && (
                    <p className="mt-2 text-xs text-teal-800">
                      Từ khoá: {meaning.keywords.join(" · ")}
                    </p>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>

      <p className="mt-5 border-t border-slate-100 pt-3 text-xs text-slate-400">
        Ý nghĩa theo hệ thống Thần số học Pythagoras — kho tàng diễn giải hội tụ theo các nguồn
        tiêu chuẩn (không dùng Chaldean, vì chưa có cách quy đổi chữ cái tiếng Việt được xác minh —
        xem mục nghiên cứu R8) — đây là ý nghĩa con số, không phải AI viết riêng cho bạn.
      </p>
    </section>
  );
}
