"use client";

import Image from "next/image";
import { useState } from "react";
import { tarotCardImagePath, tarotCardNameVi } from "@/lib/tarotCards";

/**
 * One drawn card, rendered as a flippable tile: a plain back face until
 * `revealed`, then the real Rider-Waite-Smith scan (public domain, see
 * `lib/tarotCards.ts`) rotated 180° for a reversed draw — the conventional
 * way Tarot readers show a reversal, rather than a text badge alone.
 *
 * If the bundled image is missing for some card (a partial asset fetch),
 * `onError` swaps to a plain symbol + name tile — never a broken-image icon
 * or a blank space where a card should be.
 */
export function TarotCard({
  cardId,
  cardName,
  orientation,
  positionLabel,
  revealed,
  onReveal,
}: {
  cardId: string;
  cardName: string;
  orientation: string;
  positionLabel?: string;
  revealed: boolean;
  onReveal?: () => void;
}) {
  const [imageFailed, setImageFailed] = useState(false);
  const nameVi = tarotCardNameVi(cardId, cardName);
  const isReversed = orientation === "REVERSED";

  return (
    <div className="flex flex-col items-center gap-2">
      {positionLabel && (
        <span className="text-xs font-medium uppercase tracking-wide text-slate-500">
          {positionLabel}
        </span>
      )}
      <button
        type="button"
        onClick={onReveal}
        disabled={revealed || !onReveal}
        className="group [perspective:1000px]"
        aria-label={revealed ? nameVi : "Lật lá bài"}
      >
        <div
          className={`relative h-56 w-32 rounded-lg shadow-md transition-transform duration-500 [transform-style:preserve-3d] ${
            revealed ? "[transform:rotateY(180deg)]" : ""
          } ${!revealed && onReveal ? "cursor-pointer group-hover:-translate-y-1" : ""}`}
        >
          {/* Back face - shown before reveal */}
          <div className="absolute inset-0 flex items-center justify-center rounded-lg border-2 border-amber-300 bg-slate-900 [backface-visibility:hidden]">
            <svg viewBox="0 0 100 160" className="h-4/5 w-4/5 opacity-70">
              <rect x="4" y="4" width="92" height="152" rx="6" fill="none" stroke="#d4af37" strokeWidth="2" />
              <circle cx="50" cy="80" r="28" fill="none" stroke="#d4af37" strokeWidth="1.5" />
              <path d="M50 40 L50 120 M22 80 L78 80" stroke="#d4af37" strokeWidth="1" />
            </svg>
          </div>

          {/* Front face - the real card, shown after reveal */}
          <div
            className={`absolute inset-0 overflow-hidden rounded-lg border border-slate-200 bg-white [backface-visibility:hidden] [transform:rotateY(180deg)] ${
              isReversed ? "[&>span]:rotate-180" : ""
            }`}
          >
            {!imageFailed ? (
              <span className="relative block h-full w-full">
                <Image
                  src={tarotCardImagePath(cardId)}
                  alt={nameVi}
                  fill
                  sizes="128px"
                  className="object-cover"
                  onError={() => setImageFailed(true)}
                />
              </span>
            ) : (
              <div className="flex h-full flex-col items-center justify-center gap-2 bg-slate-50 p-2 text-center">
                <span className="text-3xl">🃏</span>
                <span className="text-xs font-medium text-slate-700">{nameVi}</span>
              </div>
            )}
          </div>
        </div>
      </button>
      {revealed && (
        <div className="text-center">
          <p className="text-sm font-semibold text-slate-900">{nameVi}</p>
          <p className="text-xs text-slate-500">{isReversed ? "Ngược" : "Xuôi"}</p>
        </div>
      )}
    </div>
  );
}
