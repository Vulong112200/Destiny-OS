"use client";

import { useEffect, useRef } from "react";
import { LazyMotion, domAnimation, m, useReducedMotion } from "framer-motion";
import { TarotCard, TarotTile } from "@/components/TarotCard";
import { TarotBackDefs } from "@/components/icons/TarotCardBack";
import { tarotCardNameVi } from "@/lib/tarotCards";
import { CARD_RATIO, spreadGeometry } from "@/lib/tarotLayout";
import type { TarotSpreadName } from "@/lib/types";

export interface StageDraw {
  evidenceId: string;
  cardId: string;
  cardName: string;
  orientation: string;
  positionLabel?: string;
  positionHasMeaning: boolean;
  deckSlot?: number;
  selectionMode?: string;
}

/**
 * Các lá đã rút, đặt đúng bố cục của kiểu trải.
 *
 * <p>Trước đây đây là một `flex flex-wrap justify-center gap-6` — mười lá của
 * Thập tự Celtic xuống dòng thành một lưới, nên hình chữ thập và cây gậy, tức
 * là toàn bộ ý nghĩa của bố cục ấy, biến mất. Móng ngựa cũng thành một hàng
 * thẳng.
 *
 * <p><strong>Vị trí là CSS, chuyển động là transform — không trộn hai thứ.</strong>
 * Bản trước đặt `left`/`top` trong `style` nhưng lại khai `left: "4%", top: "50%"`
 * trong `initial` của framer-motion mà không khai lại trong `animate`. Framer
 * giữ nguyên giá trị nó đã ghi khi `animate` không nhắc tới thuộc tính đó, nên
 * cả mười lá đứng vĩnh viễn ở `left: 4%, top: 50%`: toàn bộ bàn bài dồn về một
 * điểm, mọi nhãn đè lên nhau. Nay `left`/`top` chỉ nằm ở một `div` định vị
 * thuần CSS mà framer không chạm vào, còn hiệu ứng vào bàn chỉ dùng
 * opacity/scale/y trên một `div` con.
 *
 * <p>Toàn bộ phần chuyển động là trang trí: dữ liệu đã có đủ trước khi bất kỳ
 * chuyển động nào xảy ra, nên khi người dùng tắt hiệu ứng thì các lá hiện thẳng
 * tại chỗ và không mất gì.
 */
export function TarotSpreadStage({
  spread,
  draws,
  revealed,
  onReveal,
  revealAllToken,
}: {
  spread: TarotSpreadName;
  draws: StageDraw[];
  revealed: Set<string>;
  onReveal: (evidenceId: string) => void;
  /**
   * Tăng lên mỗi lần bấm "Lật tất cả". Dùng làm tín hiệu để chạy chuỗi lật có
   * độ trễ, thay vì lật sạch trong một khung hình.
   */
  revealAllToken: number;
}) {
  const reduce = useReducedMotion();
  const { slots, aspect, cardWidth } = spreadGeometry(spread, draws.length);
  const timers = useRef<number[]>([]);

  useEffect(() => {
    if (revealAllToken === 0) return;
    timers.current.forEach(window.clearTimeout);
    timers.current = [];
    const step = reduce ? 0 : 170;
    draws.forEach((d, i) => {
      if (revealed.has(d.evidenceId)) return;
      timers.current.push(window.setTimeout(() => onReveal(d.evidenceId), i * step));
    });
    return () => {
      timers.current.forEach(window.clearTimeout);
      timers.current = [];
    };
    // Chỉ chạy khi người dùng bấm "Lật tất cả". Thêm `revealed` vào đây sẽ
    // khiến chuỗi tự khởi động lại sau mỗi lá được lật.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [revealAllToken]);

  // Kiểu trải tự do cố ý không có hình dạng: nó tồn tại để từ chối gán ý nghĩa
  // cho vị trí, nên xếp nó thành một hình gì đó sẽ ngầm nói ngược lại.
  if (slots === null || slots.length < draws.length) {
    return (
      <LazyMotion features={domAnimation} strict>
        <TarotBackDefs />
        <div className="flex flex-wrap justify-center gap-6">
          {draws.map((d, i) => (
            <Entering key={d.evidenceId} index={i} reduce={Boolean(reduce)}>
              <TarotCard
                cardId={d.cardId}
                cardName={d.cardName}
                orientation={d.orientation}
                positionLabel={d.positionLabel}
                positionHasMeaning={d.positionHasMeaning}
                deckSlot={d.deckSlot}
                selectionMode={d.selectionMode}
                revealed={revealed.has(d.evidenceId)}
                onReveal={() => onReveal(d.evidenceId)}
              />
            </Entering>
          ))}
        </div>
      </LazyMotion>
    );
  }

  /*
    Kích thước lá tính theo bề ngang THẬT của khung chứa (container query
    `cqw`), không theo breakpoint viewport: khung bị chặn ở `max-w-3xl` nên từ
    viewport ~1024px trở lên, cỡ theo breakpoint vẫn tăng dù khung không lớn
    thêm. `cardWidth` là tỉ lệ đã tính khớp với bố cục (xem `SpreadGeometry`),
    nên tỉ lệ đó giữ nguyên ở mọi bề ngang khung.
  */
  const cardSize = {
    width: `${(cardWidth * 100).toFixed(3)}cqw`,
    height: `${(cardWidth * CARD_RATIO * 100).toFixed(3)}cqw`,
  };

  return (
    <LazyMotion features={domAnimation} strict>
      <TarotBackDefs />
      <div
        className="@container relative mx-auto w-full max-w-3xl"
        style={{ aspectRatio: String(aspect) }}
      >
        {draws.map((d, i) => {
          const slot = slots[i];
          return (
            <div
              key={d.evidenceId}
              // Chỉ định vị. Framer không được ghi vào `div` này.
              className="absolute -translate-x-1/2 -translate-y-1/2"
              style={{
                left: `${slot.x * 100}%`,
                top: `${slot.y * 100}%`,
                zIndex: revealed.has(d.evidenceId) ? 30 + slot.z : 10 + slot.z,
              }}
            >
              <Entering index={i} reduce={Boolean(reduce)}>
                <TarotTile
                  cardId={d.cardId}
                  cardName={d.cardName}
                  orientation={d.orientation}
                  revealed={revealed.has(d.evidenceId)}
                  onReveal={() => onReveal(d.evidenceId)}
                  rotate={slot.rotate}
                  size={cardSize}
                  badge={i + 1}
                  positionLabel={d.positionLabel}
                />
              </Entering>
            </div>
          );
        })}
      </div>

      <SpreadLegend draws={draws} revealed={revealed} />
    </LazyMotion>
  );
}

/**
 * Chú giải: số thứ tự → vị trí trong kiểu trải → lá đã lật.
 *
 * <p>Đây là chỗ chữ nghĩa của bàn bài dời về. Nhãn vị trí không thể nằm sát lá
 * trong bố cục có vị trí: lá 1 và lá 2 của Thập tự Celtic cố ý chồng lên nhau,
 * nên nhãn của chúng cũng chồng lên nhau, và lá 2 xoay 90° thì nhãn xoay theo.
 * Ở đây thì không lá nào che lá nào, và người đọc thấy đủ mười vị trí ngay từ
 * lúc chưa lật lá nào — điều bàn bài cũ không làm được.
 */
function SpreadLegend({
  draws,
  revealed,
}: {
  draws: StageDraw[];
  revealed: Set<string>;
}) {
  return (
    <ol className="mx-auto mt-5 grid max-w-3xl gap-x-8 gap-y-1.5 text-xs sm:grid-cols-2">
      {draws.map((d, i) => {
        const daLat = revealed.has(d.evidenceId);
        return (
          <li key={d.evidenceId} className="flex items-baseline gap-2">
            <span
              aria-hidden
              className="mt-0.5 flex h-4 w-4 shrink-0 items-center justify-center rounded-full bg-slate-200 text-[10px] font-semibold text-slate-700"
            >
              {i + 1}
            </span>
            <span className="min-w-0">
              <span
                className={
                  d.positionHasMeaning
                    ? "font-medium text-slate-700"
                    : "text-slate-500"
                }
              >
                {d.positionLabel ?? `Lá ${i + 1}`}
              </span>
              {daLat ? (
                <>
                  <span className="text-slate-400"> — </span>
                  <span className="text-slate-800">
                    {tarotCardNameVi(d.cardId, d.cardName)}
                  </span>
                  <span className="text-slate-500">
                    {d.orientation === "REVERSED" ? " (ngược)" : " (xuôi)"}
                  </span>
                  {d.selectionMode === "PICKED_BY_QUERENT" && d.deckSlot !== undefined && (
                    <span className="text-indigo-600"> · bạn chọn ô {d.deckSlot}</span>
                  )}
                </>
              ) : (
                <span className="text-slate-400"> — chưa lật</span>
              )}
            </span>
          </li>
        );
      })}
    </ol>
  );
}

function Entering({
  index,
  reduce,
  children,
}: {
  index: number;
  reduce: boolean;
  children: React.ReactNode;
}) {
  return (
    <m.div
      initial={reduce ? false : { opacity: 0, y: 18, scale: 0.9 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      transition={reduce ? { duration: 0 } : { type: "spring", stiffness: 240, damping: 24, delay: index * 0.08 }}
    >
      {children}
    </m.div>
  );
}
