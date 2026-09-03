"use client";

import { useState } from "react";
import type { EvidenceDto } from "@/lib/types";
import { tarotCardNameVi, tarotPositionLabelVi } from "@/lib/tarotCards";
import { asSupportedScenario, tarotMeaningKeyFor } from "@/lib/scenarioMeta";
import { TarotSpreadStage } from "./tarot/TarotSpreadStage";
import type { StageDraw } from "./tarot/TarotSpreadStage";
import { inferSpread } from "@/lib/tarotLayout";

interface TarotMeaning {
  uprightKeywords?: string[];
  reversedKeywords?: string[];
  career?: string | null;
  finance?: string | null;
  relationship?: string | null;
  decision?: string | null;
  general?: string | null;
}

const MEANING_LABELS: { key: keyof TarotMeaning; label: string }[] = [
  { key: "general", label: "Ý nghĩa chung" },
  { key: "decision", label: "Cho quyết định này" },
  { key: "career", label: "Sự nghiệp" },
  { key: "finance", label: "Tài chính" },
  { key: "relationship", label: "Quan hệ" },
];

/**
 * Orders a card's authored meanings so the one matching the question comes
 * first and is marked as the one that applies.
 *
 * `TarotCardMeaning` authors five dimension-specific texts and `TarotEngine`
 * ships all five; nothing server-side picks one, so this card used to print
 * all five in a fixed order. For someone who asked about sự nghiệp that meant
 * the career reading appeared third, in the same styling as the relationship
 * reading they did not ask for - which is most of why the page read as
 * generic. The pick is presentation only: no meaning is hidden, and the
 * others stay visible below.
 */
function orderedMeanings(meaning: TarotMeaning, scenarioId: string) {
  const primaryKey = tarotMeaningKeyFor(asSupportedScenario(scenarioId));
  const entries = MEANING_LABELS.map(({ key, label }) => ({
    key,
    label,
    text: meaning[key],
    primary: key === primaryKey,
  })).filter(
    (e): e is { key: keyof TarotMeaning; label: string; text: string; primary: boolean } =>
      typeof e.text === "string" && e.text.trim() !== "",
  );
  return [...entries].sort((a, b) => Number(b.primary) - Number(a.primary));
}

/**
 * Renders a Tarot draw as an actual "rút bài": every card starts face-down
 * and the user clicks (or uses "Lật tất cả") to reveal it, rather than the
 * spread appearing already face-up the instant the page loads. The draw
 * itself already happened, deterministically, server-side at submit time
 * (seed + Fisher-Yates, `TarotEngine`) - this is a client-side re-enactment
 * of that draw for the person looking at it, not a second draw.
 *
 * Renders nothing when Tarot did not take part in this run, matching every
 * other engine card in `ResultView`.
 */
export function TarotResultCard({
  evidence,
  scenarioId = "",
}: {
  evidence: EvidenceDto[];
  /** Chosen scenario, so the matching authored meaning leads. Empty = unfocused. */
  scenarioId?: string;
}) {
  const draws = evidence.filter((e) => e.engine === "TAROT" && e.ruleId === "TAROT_SEEDED_DRAW");
  const [revealed, setRevealed] = useState<Set<string>>(new Set());
  const [revealAllToken, setRevealAllToken] = useState(0);

  if (draws.length === 0) {
    return null;
  }

  function reveal(evidenceId: string) {
    setRevealed((prev) => new Set(prev).add(evidenceId));
  }

  // Tăng một token thay vì lật sạch ngay: sân khấu dùng nó để chạy chuỗi lật
  // có độ trễ, nên mười lá của Thập tự Celtic mở ra lần lượt chứ không cùng
  // một khung hình.
  function revealAll() {
    setRevealAllToken((n) => n + 1);
  }

  const allRevealed = revealed.size >= draws.length;

  const stageDraws: (StageDraw & { rawPosition: string })[] = draws.map((ev) => {
    const fact = ev.fact as {
      position?: string;
      positionHasMeaning?: boolean;
      deckSlot?: number;
      selectionMode?: string;
      cardId?: string;
      cardName?: string;
      orientation?: string;
    };
    const rawPosition = String(fact.position ?? "");
    return {
      evidenceId: ev.evidenceId,
      cardId: String(fact.cardId ?? ""),
      cardName: String(fact.cardName ?? ""),
      orientation: String(fact.orientation ?? "UPRIGHT"),
      positionLabel: rawPosition ? tarotPositionLabelVi(rawPosition) : undefined,
      positionHasMeaning: fact.positionHasMeaning !== false,
      deckSlot: typeof fact.deckSlot === "number" ? fact.deckSlot : undefined,
      selectionMode: fact.selectionMode,
      rawPosition,
    };
  });

  return (
    <section className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
        <h2 className="text-lg font-semibold text-slate-900">🃏 Tarot — lá bài đã rút</h2>
        {!allRevealed && (
          <button
            type="button"
            onClick={revealAll}
            className="rounded-md border border-slate-300 px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50"
          >
            Lật tất cả
          </button>
        )}
      </div>
      <p className="mb-4 text-xs text-slate-500">
        Lá bài đã được rút (thuật toán tất định, có seed) ngay khi bạn bấm Tính toán. Bấm vào từng
        lá (hoặc &quot;Lật tất cả&quot;) để xem — đây chỉ là diễn lại phần rút bài cho bạn thấy,
        không phải rút lại.
      </p>
      <TarotSpreadStage
        spread={inferSpread(stageDraws.map((d) => d.rawPosition)) ?? "FREE_FORM"}
        draws={stageDraws}
        revealed={revealed}
        onReveal={reveal}
        revealAllToken={revealAllToken}
      />

      {allRevealed && (
        <div className="mt-6 space-y-4 border-t border-slate-100 pt-4">
          {draws.map((ev) => {
            const fact = ev.fact as {
              position?: string;
              cardId?: string;
              cardName?: string;
              meaning?: TarotMeaning;
            };
            const meaning = fact.meaning;
            if (!meaning) return null;
            const nameVi = tarotCardNameVi(String(fact.cardId ?? ""), String(fact.cardName ?? ""));
            const entries = orderedMeanings(meaning, scenarioId);
            if (entries.length === 0) return null;
            return (
              <div key={ev.evidenceId}>
                <h3 className="text-sm font-semibold text-slate-900">
                  {fact.position ? `${tarotPositionLabelVi(fact.position)} — ` : ""}
                  {nameVi}
                </h3>
                <dl className="mt-2 space-y-2">
                  {entries.map(({ label, text, primary }) =>
                    primary ? (
                      <div
                        key={label}
                        className="rounded-md border border-slate-300 bg-slate-50 p-3 text-sm text-slate-800"
                      >
                        <dt className="mb-0.5 text-xs font-semibold uppercase tracking-wide text-slate-500">
                          {label} — ứng với câu hỏi của bạn
                        </dt>
                        <dd>{text}</dd>
                      </div>
                    ) : (
                      <div key={label} className="px-1 text-sm text-slate-600">
                        <dt className="inline font-medium text-slate-500">{label}: </dt>
                        <dd className="inline">{text}</dd>
                      </div>
                    ),
                  )}
                </dl>
              </div>
            );
          })}
          <p className="text-xs text-slate-400">
            Ý nghĩa theo truyền thống diễn giải Rider-Waite-Smith chuẩn (A.E. Waite, 1910, và
            khối tài liệu diễn giải đồng thuận từ đó) — đây là ý nghĩa lá bài, không phải AI viết
            riêng cho bạn.
          </p>
        </div>
      )}
    </section>
  );
}
