"use client";

import Image from "next/image";
import { useState } from "react";
import { tarotCardImagePath, tarotCardNameVi } from "@/lib/tarotCards";
import { TarotCardBack } from "@/components/icons/TarotCardBack";

/**
 * One drawn card, rendered as a flippable tile: a card back until `revealed`,
 * then the real Rider-Waite-Smith scan (public domain, see `lib/tarotCards.ts`)
 * rotated 180° for a reversed draw — the conventional way Tarot readers show a
 * reversal, rather than a text badge alone.
 *
 * If the bundled image is missing for some card (a partial asset fetch),
 * `onError` swaps to a plain symbol + name tile — never a broken-image icon
 * or a blank space where a card should be.
 *
 * <p>Mặt lưng dùng chung `TarotCardBack`, nên lá bài lúc chờ lật trông giống
 * hệt lá bài trong màn chọn ở Trung tâm quyết định. Trước đây mỗi nơi tự vẽ
 * một mặt lưng khác nhau — hoặc, ở màn chọn, không vẽ gì cả.
 */
export function TarotCard({
  cardId,
  cardName,
  orientation,
  positionLabel,
  positionHasMeaning = true,
  deckSlot,
  selectionMode,
  revealed,
  onReveal,
}: {
  cardId: string;
  cardName: string;
  orientation: string;
  positionLabel?: string;
  /**
   * Kiểu trải tự do không gán ý nghĩa cho vị trí, và `fact.positionHasMeaning`
   * nói đúng điều đó. Khi false, nhãn vị trí hiện nhạt và không viết hoa — nó
   * là số thứ tự, không phải một tuyên bố.
   */
  positionHasMeaning?: boolean;
  /** Ô trong bộ đã xào mà lá này đến từ đó. */
  deckSlot?: number;
  /** "PICKED_BY_QUERENT" hoặc "TOP_OF_DECK". */
  selectionMode?: string;
  revealed: boolean;
  onReveal?: () => void;
}) {
  const [imageFailed, setImageFailed] = useState(false);
  const nameVi = tarotCardNameVi(cardId, cardName);
  const isReversed = orientation === "REVERSED";
  const pickedByQuerent = selectionMode === "PICKED_BY_QUERENT";

  return (
    <div className="flex flex-col items-center gap-2">
      {positionLabel && (
        <span
          className={
            positionHasMeaning
              ? "text-xs font-medium uppercase tracking-wide text-slate-500"
              : "text-xs text-slate-400"
          }
        >
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
          <div className="absolute inset-0 overflow-hidden rounded-lg [backface-visibility:hidden]">
            <TarotCardBack className="h-full w-full" />
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
          {/*
            "Lá bạn đã chọn" là một câu chỉ được nói khi nó đúng. `selectionMode`
            và `deckSlot` đã nằm sẵn trong evidence từ lúc engine ghi ra — trang
            kết quả chỉ chưa bao giờ đọc tới, nên một người tự chọn lá và một
            người để máy bốc nhìn thấy y hệt nhau.
          */}
          {pickedByQuerent && deckSlot !== undefined && (
            <p className="text-[11px] text-indigo-600">Lá bạn đã chọn (ô {deckSlot})</p>
          )}
        </div>
      )}
    </div>
  );
}
