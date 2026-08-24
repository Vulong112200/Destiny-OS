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
 * Purely a drawing of the cast hexagram — no line/hexagram meaning text
 * here or anywhere it is used; that stays R12-blocked (CLAUDE.md Rule C).
 */
export function HexagramSvg({ lines, className }: { lines: string[]; className?: string }) {
  const height = lines.length * ROW_HEIGHT - GAP;

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
          <g key={i}>
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
              <circle cx={BAR_WIDTH / 2} cy={y + BAR_HEIGHT / 2} r={2.5} fill="#fef3c7" />
            )}
          </g>
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
