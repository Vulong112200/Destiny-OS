"use client";

import { useEffect, useRef } from "react";
import { LazyMotion, domAnimation, m, useReducedMotion } from "framer-motion";
import { TarotCard } from "@/components/TarotCard";
import { TarotBackDefs } from "@/components/icons/TarotCardBack";
import { spreadAspect, spreadLayout } from "@/lib/tarotLayout";
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
 * <p>Các lá bay ra từ một chồng bài ở mép trái rồi về đúng ô của mình, sau đó
 * lật lần lượt chứ không lật cùng lúc. Toàn bộ phần này là trang trí: dữ liệu
 * đã có đủ trước khi bất kỳ chuyển động nào xảy ra, nên khi người dùng tắt
 * hiệu ứng thì các lá hiện thẳng tại chỗ và không mất gì.
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
  const layout = spreadLayout(spread, draws.length);
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
  if (layout === null || layout.length < draws.length) {
    return (
      <LazyMotion features={domAnimation} strict>
        <TarotBackDefs />
        <div className="flex flex-wrap justify-center gap-6">
          {draws.map((d, i) => (
            <Entering key={d.evidenceId} index={i} reduce={Boolean(reduce)}>
              <Card draw={d} revealed={revealed.has(d.evidenceId)} onReveal={onReveal} />
            </Entering>
          ))}
        </div>
      </LazyMotion>
    );
  }

  return (
    <LazyMotion features={domAnimation} strict>
      <TarotBackDefs />
      <div
        className="@container relative mx-auto w-full max-w-3xl"
        style={{ aspectRatio: String(spreadAspect(spread)) }}
      >
        {draws.map((d, i) => {
          const slot = layout[i];
          return (
            <m.div
              key={d.evidenceId}
              className="absolute origin-center"
              style={{
                left: `${slot.x * 100}%`,
                top: `${slot.y * 100}%`,
                zIndex: revealed.has(d.evidenceId) ? 30 + slot.z : 10 + slot.z,
              }}
              initial={
                reduce
                  ? false
                  : { x: "-50%", y: "-50%", opacity: 0, scale: 0.85, rotate: 0, left: "4%", top: "50%" }
              }
              animate={{
                x: "-50%",
                y: "-50%",
                opacity: 1,
                scale: 1,
                rotate: slot.rotate,
              }}
              transition={
                reduce
                  ? { duration: 0 }
                  : { type: "spring", stiffness: 220, damping: 24, delay: i * 0.09 }
              }
            >
              {/*
                Tỉ lệ phải theo bề rộng THẬT của khung chứa (container query
                cqw), không theo breakpoint viewport — khung chứa bị chặn cứng
                ở max-w-3xl (768px) nên từ viewport ~1024px trở lên, scale kiểu
                cũ (sm/md/lg) vẫn tăng dù khung không lớn thêm, làm các lá đè
                lên nhau. 0.62 là tỉ lệ tham chiếu đã tính khớp với bố cục ở
                đúng 768px; nhân theo cqw giữ tỉ lệ đó không đổi ở mọi kích
                thước khung, kể cả khi hẹp hơn 768px.
              */}
              <div className="origin-center [scale:calc(100cqw/768px*0.62)]">
                <Card draw={d} revealed={revealed.has(d.evidenceId)} onReveal={onReveal} />
              </div>
            </m.div>
          );
        })}
      </div>
    </LazyMotion>
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

function Card({
  draw,
  revealed,
  onReveal,
}: {
  draw: StageDraw;
  revealed: boolean;
  onReveal: (evidenceId: string) => void;
}) {
  return (
    <TarotCard
      cardId={draw.cardId}
      cardName={draw.cardName}
      orientation={draw.orientation}
      positionLabel={draw.positionLabel}
      positionHasMeaning={draw.positionHasMeaning}
      deckSlot={draw.deckSlot}
      selectionMode={draw.selectionMode}
      revealed={revealed}
      onReveal={() => onReveal(draw.evidenceId)}
    />
  );
}
