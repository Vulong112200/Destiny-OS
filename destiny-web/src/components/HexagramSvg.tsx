"use client";

import { m, useReducedMotion } from "framer-motion";

const BAR_WIDTH = 96;
const BAR_HEIGHT = 10;
const GAP = 10;
const ROW_HEIGHT = BAR_HEIGHT + GAP;

function isYang(value: string): boolean {
  return value === "YOUNG_YANG" || value === "OLD_YANG";
}

function isMoving(value: string): boolean {
  return value === "OLD_YIN" || value === "OLD_YANG";
}

/**
 * Draws one hexagram as 6 stacked bars — solid = Dương, broken (two
 * segments) = Âm, matching the millennia-old convention rather than the
 * plain text list this replaces (`IChingChartCard.tsx`'s old rendering).
 * Moving lines (hào động, "lão" values) get a distinct colour and a small
 * centre marker, the traditional way to flag which line flips into 卦變.
 *
 * `lines[0]` is hào 1 (bottom), matching how the backend already orders
 * `ICHING_DRAWN_LINES.lines` (`IChingChartCard.tsx`'s pre-existing
 * `flex-col-reverse` rendering makes the same assumption).
 *
 * Chỉ vẽ quẻ đã gieo — bản thân component này không mang lời quẻ/lời hào.
 * Từ R24/R25 (2026-08-31), lời quẻ/lời hào đã có nội dung thật và được hiển
 * thị ở `IChingChartCard.tsx`, ngay dưới hình vẽ này, không phải trong SVG.
 * Từng hào hiện lần lượt từ hào 1 (dưới) lên hào 6 (trên) — đúng thứ tự gieo
 * quẻ cổ truyền — thay vì hiện cùng lúc; tắt hiệu ứng chuyển động thì hiện
 * thẳng tại chỗ, không mất gì.
 */
export function HexagramSvg({ lines, className }: { lines: string[]; className?: string }) {
  const height = lines.length * ROW_HEIGHT - GAP;
  const reduce = useReducedMotion();

  return (
    <svg
      viewBox={`0 0 ${BAR_WIDTH} ${height}`}
      width={BAR_WIDTH}
      height={height}
      className={className}
      role="img"
      aria-label={`Quẻ 6 hào: ${lines
        .map((l, i) => `hào ${i + 1} ${isYang(l) ? "Dương" : "Âm"}${isMoving(l) ? " động" : ""}`)
        .join(", ")}`}
    >
      {lines.map((value, i) => {
        // i=0 (hao 1) drawn at the bottom, so its y is the largest.
        const y = height - BAR_HEIGHT - i * ROW_HEIGHT;
        const yang = isYang(value);
        const moving = isMoving(value);
        const fill = moving ? "#b45309" : "#1e293b"; // amber-700 : slate-800
        return (
          <m.g
            key={i}
            initial={reduce ? false : { opacity: 0, x: -8 }}
            animate={{ opacity: 1, x: 0 }}
            transition={reduce ? { duration: 0 } : { delay: i * 0.12, duration: 0.35, ease: "easeOut" }}
          >
            {yang ? (
              <rect x={0} y={y} width={BAR_WIDTH} height={BAR_HEIGHT} rx={2} fill={fill} />
            ) : (
              <>
                <rect x={0} y={y} width={BAR_WIDTH * 0.42} height={BAR_HEIGHT} rx={2} fill={fill} />
                <rect
                  x={BAR_WIDTH * 0.58}
                  y={y}
                  width={BAR_WIDTH * 0.42}
                  height={BAR_HEIGHT}
                  rx={2}
                  fill={fill}
                />
              </>
            )}
            {moving && (
              <m.circle
                cx={BAR_WIDTH / 2}
                cy={y + BAR_HEIGHT / 2}
                r={2.5}
                fill="#fef3c7"
                animate={reduce ? undefined : { opacity: [1, 0.4, 1] }}
                transition={reduce ? undefined : { delay: i * 0.12 + 0.4, duration: 1.6, repeat: Infinity }}
              />
            )}
          </m.g>
        );
      })}
    </svg>
  );
}

/** The hexagram after moving lines flip (卦變) - display only, no new meaning. */
export function changedLineValues(lines: string[]): string[] {
  return lines.map((value) => {
    if (!isMoving(value)) return value;
    return isYang(value) ? "YOUNG_YIN" : "YOUNG_YANG";
  });
}
