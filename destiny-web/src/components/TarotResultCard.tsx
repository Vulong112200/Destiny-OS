"use client";

import { useState } from "react";
import type { EvidenceDto } from "@/lib/types";
import { tarotCardNameVi, tarotPositionLabelVi } from "@/lib/tarotCards";
import { TarotCard } from "./TarotCard";

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
export function TarotResultCard({ evidence }: { evidence: EvidenceDto[] }) {
  const draws = evidence.filter((e) => e.engine === "TAROT" && e.ruleId === "TAROT_SEEDED_DRAW");
  const [revealed, setRevealed] = useState<Set<string>>(new Set());

  if (draws.length === 0) {
    return null;
  }

  function reveal(evidenceId: string) {
    setRevealed((prev) => new Set(prev).add(evidenceId));
  }

  function revealAll() {
    setRevealed(new Set(draws.map((d) => d.evidenceId)));
  }

  const allRevealed = revealed.size >= draws.length;

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
      <div className="flex flex-wrap justify-center gap-6">
        {draws.map((ev) => {
          const fact = ev.fact as {
            position?: string;
            cardId?: string;
            cardName?: string;
            orientation?: string;
            meaning?: TarotMeaning;
          };
          const isRevealed = revealed.has(ev.evidenceId);
          return (
            <TarotCard
              key={ev.evidenceId}
              cardId={String(fact.cardId ?? "")}
              cardName={String(fact.cardName ?? "")}
              orientation={String(fact.orientation ?? "UPRIGHT")}
              positionLabel={fact.position ? tarotPositionLabelVi(fact.position) : undefined}
              revealed={isRevealed}
              onReveal={() => reveal(ev.evidenceId)}
            />
          );
        })}
      </div>

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
            const entries = MEANING_LABELS.map(({ key, label }) => ({
              label,
              text: meaning[key],
            })).filter((e) => typeof e.text === "string" && e.text.trim() !== "");
            if (entries.length === 0) return null;
            return (
              <div key={ev.evidenceId}>
                <h3 className="text-sm font-semibold text-slate-900">
                  {fact.position ? `${tarotPositionLabelVi(fact.position)} — ` : ""}
                  {nameVi}
                </h3>
                <dl className="mt-1 space-y-1">
                  {entries.map(({ label, text }) => (
                    <div key={label} className="text-sm text-slate-700">
                      <dt className="inline font-medium text-slate-500">{label}: </dt>
                      <dd className="inline">{text}</dd>
                    </div>
                  ))}
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
