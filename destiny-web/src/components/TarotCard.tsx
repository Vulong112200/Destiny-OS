"use client";

import Image from "next/image";
import { useState } from "react";
import type { CSSProperties } from "react";
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
 *
 * <p><strong>Chỉ có ô bài, không có chữ.</strong> Nhãn vị trí và tên lá nằm ở
 * `TarotCard` (bố cục xếp dòng) hoặc ở phần chú giải dưới sân khấu (bố cục có
 * vị trí). Lý do: trong Thập tự Celtic, lá giao cắt xoay 90° và nằm đúng trên lá
 * 1 — nếu chữ đi kèm ô bài thì nó xoay theo thành chữ dọc, rồi đè lên nhãn của
 * lá bên dưới. Chữ phải nằm ngoài thứ bị xoay và ngoài thứ bị xếp chồng.
 */
export function TarotTile({
  cardId,
  cardName,
  orientation,
  revealed,
  onReveal,
  /** Độ nghiêng của ô bài trong bố cục. Chỉ ảnh hưởng phần hình, không xoay số. */
  rotate = 0,
  /** Kích thước ô bài. Bỏ trống thì dùng cỡ cố định `h-56 w-32`. */
  size,
  /** Số thứ tự vị trí, khớp với phần chú giải. */
  badge,
  /** Nhãn vị trí — chỉ dùng cho trình đọc màn hình và tooltip, không vẽ ra. */
  positionLabel,
}: {
  cardId: string;
  cardName: string;
  orientation: string;
  revealed: boolean;
  onReveal?: () => void;
  rotate?: number;
  size?: { width: string; height: string };
  badge?: number;
  positionLabel?: string;
}) {
  const [imageFailed, setImageFailed] = useState(false);
  const nameVi = tarotCardNameVi(cardId, cardName);
  const isReversed = orientation === "REVERSED";
  const huong = isReversed ? "ngược" : "xuôi";
  const viTri = positionLabel ? `${positionLabel} — ` : "";
  const tenDayDu = `${viTri}${nameVi} (${huong})`;

  // `rotate` là thuộc tính riêng, không phải `transform`: ô bài bên trong dùng
  // `transform: rotateY(180deg)` để lật, và hai thứ đó sẽ ghi đè nhau nếu cùng
  // nằm trên một thuộc tính.
  const tilt: CSSProperties | undefined = rotate === 0 ? undefined : { rotate: `${rotate}deg` };

  return (
    <div className="relative" style={tilt}>
      <button
        type="button"
        onClick={onReveal}
        disabled={revealed || !onReveal}
        className="group block [perspective:1000px]"
        title={revealed ? tenDayDu : positionLabel ? `Lật lá: ${positionLabel}` : "Lật lá bài"}
        aria-label={revealed ? tenDayDu : positionLabel ? `Lật lá ở vị trí ${positionLabel}` : "Lật lá bài"}
      >
        <div
          className={`relative rounded-lg shadow-md transition-transform duration-500 [transform-style:preserve-3d] ${
            size ? "" : "h-56 w-32"
          } ${revealed ? "[transform:rotateY(180deg)]" : ""} ${
            !revealed && onReveal ? "cursor-pointer group-hover:-translate-y-1" : ""
          }`}
          style={size}
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
                  sizes="160px"
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
      {badge !== undefined && (
        <span
          aria-hidden
          // Xoay ngược lại đúng bằng độ nghiêng của ô bài: con số dính ở góc lá
          // nhưng vẫn đọc theo chiều ngang, kể cả trên lá giao cắt xoay 90°.
          style={rotate === 0 ? undefined : { rotate: `${-rotate}deg` }}
          className="absolute -left-1.5 -top-1.5 flex h-5 w-5 items-center justify-center rounded-full bg-slate-900 text-[10px] font-semibold text-white shadow ring-2 ring-white"
        >
          {badge}
        </span>
      )}
    </div>
  );
}

/**
 * Ô bài kèm nhãn vị trí phía trên và tên lá phía dưới.
 *
 * <p>Chỉ dùng cho bố cục xếp dòng (kiểu trải tự do): ở đó các lá không chồng
 * lên nhau và không lá nào bị xoay, nên chữ đặt sát lá là đọc được.
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
      <TarotTile
        cardId={cardId}
        cardName={cardName}
        orientation={orientation}
        revealed={revealed}
        onReveal={onReveal}
        positionLabel={positionLabel}
      />
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
