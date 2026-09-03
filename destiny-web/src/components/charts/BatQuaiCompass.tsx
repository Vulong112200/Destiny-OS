import type { LabelRegistries } from "@/lib/types";
import { labelOf } from "@/lib/labels";
import {
  COMPASS_ORDER,
  DIRECTION_ANGLE,
  DIRECTION_SHORT_VI,
  isAuspicious,
  type CompassDirection,
} from "@/lib/batTrach";

/**
 * Tám hướng Bát Trạch trên một hoa la bàn.
 *
 * <p>Cùng dữ liệu với bảng "Hướng / Du niên" ngay bên dưới, nhưng Bát Trạch là
 * một phát biểu về **phương hướng**, và một bảng hai cột thì không nói được
 * điều đó: người đọc phải tự dựng lại hình trong đầu để thấy bốn hướng hợp của
 * mình nằm liền nhau. Ở đây thì nhìn phát ra ngay.
 *
 * <p>Bảng vẫn được giữ, không bị thay thế — nó đọc được bằng trình đọc màn
 * hình và copy được, hai việc mà một hình SVG làm kém.
 *
 * <p><strong>Bắc ở trên</strong>, và có ghi rõ trong chú giải. Sơ đồ cổ truyền
 * hay để Nam ở trên; đây là lựa chọn quy ước chứ không phải mặc định hiển
 * nhiên, nên không được để người đọc tự đoán.
 */
export function BatQuaiCompass({
  directions,
  facingDirection,
  labels,
}: {
  /** Bản đồ hướng → quan hệ Du Niên, từ `FENGSHUI_BAT_TRACH_DIRECTIONS`. */
  directions: Record<string, unknown>;
  /** Hướng đang xét, nếu người dùng có nhập. */
  facingDirection?: string | null;
  labels: LabelRegistries;
}) {
  const size = 320;
  const c = size / 2;
  const rOuter = 148;
  const rInner = 58;

  return (
    <figure className="my-2">
      <figcaption className="mb-2 text-sm font-medium text-slate-700">
        Tám hướng theo cung phi của bạn
      </figcaption>

      <div className="flex flex-wrap items-center justify-center gap-6">
        <svg
          viewBox={`0 0 ${size} ${size}`}
          className="h-72 w-72 shrink-0"
          role="img"
          aria-label="Hoa la bàn tám hướng, tô màu theo du niên cát hoặc hung"
        >
          {COMPASS_ORDER.map((dir) => {
            const relation = directions[dir];
            const good = isAuspicious(relation);
            const isFacing = facingDirection === dir;
            const mid = DIRECTION_ANGLE[dir as CompassDirection];
            return (
              <g key={dir}>
                <path
                  d={sector(c, c, rInner, rOuter, mid - 22.5, mid + 22.5)}
                  fill={good ? "var(--color-cat-soft, #d1fae5)" : "var(--color-hung-soft, #ffe4e6)"}
                  stroke={good ? "var(--color-cat, #047857)" : "var(--color-hung, #be123c)"}
                  strokeWidth={isFacing ? 2.5 : 0.8}
                  opacity={isFacing ? 1 : 0.9}
                />
                <SectorText
                  cx={c}
                  cy={c}
                  angle={mid}
                  radius={(rInner + rOuter) / 2}
                  primary={DIRECTION_SHORT_VI[dir as CompassDirection]}
                  secondary={shortRelation(labelOf(labels, "BatTrachRelation", relation, ""))}
                  good={good}
                />
              </g>
            );
          })}

          {/* Kim chỉ hướng đang xét. */}
          {facingDirection && DIRECTION_ANGLE[facingDirection as CompassDirection] !== undefined && (
            <Needle
              cx={c}
              cy={c}
              angle={DIRECTION_ANGLE[facingDirection as CompassDirection]}
              radius={rOuter + 6}
            />
          )}

          <circle cx={c} cy={c} r={rInner} fill="#ffffff" stroke="#cbd5e1" strokeWidth="1" />
          <text
            x={c}
            y={c - 6}
            textAnchor="middle"
            className="fill-slate-500"
            style={{ fontSize: 11 }}
          >
            Bắc ở trên
          </text>
          <text
            x={c}
            y={c + 12}
            textAnchor="middle"
            className="fill-slate-700"
            style={{ fontSize: 13, fontWeight: 600 }}
          >
            {facingDirection
              ? DIRECTION_SHORT_VI[facingDirection as CompassDirection]
              : "chưa nhập hướng"}
          </text>
        </svg>

        <dl className="min-w-[10rem] space-y-1.5 text-xs">
          <div className="flex items-center gap-2">
            <span
              className="inline-block h-3 w-3 rounded-sm"
              style={{
                background: "var(--color-cat-soft, #d1fae5)",
                border: "1px solid var(--color-cat, #047857)",
              }}
            />
            <span className="text-slate-700">Hướng thuộc nhóm cát</span>
          </div>
          <div className="flex items-center gap-2">
            <span
              className="inline-block h-3 w-3 rounded-sm"
              style={{
                background: "var(--color-hung-soft, #ffe4e6)",
                border: "1px solid var(--color-hung, #be123c)",
              }}
            />
            <span className="text-slate-700">Hướng thuộc nhóm hung</span>
          </div>
          {facingDirection && (
            <div className="flex items-center gap-2">
              <span className="inline-block h-3 w-3 rounded-full bg-slate-900" />
              <span className="text-slate-700">Hướng bạn đang xét</span>
            </div>
          )}
          <p className="pt-1 text-[11px] leading-relaxed text-slate-500">
            Đơn vị: mỗi cung là một hướng 45 độ. Thứ tự là thứ tự la bàn, không phải thứ tự tốt
            xấu — xếp hạng nằm ở tên du niên.
          </p>
        </dl>
      </div>

      <p className="mt-1 text-[11px] text-slate-400">
        Phương pháp: Bát Trạch (Bát Biến Du Niên), bảng hướng suy từ quy tắc đổi hào — xem mục
        nghiên cứu R7 trong tài liệu dự án. Đây là tín hiệu theo truyền thống, không phải một
        khẳng định đã được khoa học kiểm chứng.
      </p>
    </figure>
  );
}

/** Bỏ phần xếp hạng trong ngoặc để nhãn vừa một cung 45 độ. */
function shortRelation(labelVi: string): string {
  const paren = labelVi.indexOf(" (");
  return paren === -1 ? labelVi : labelVi.slice(0, paren);
}

function SectorText({
  cx,
  cy,
  angle,
  radius,
  primary,
  secondary,
  good,
}: {
  cx: number;
  cy: number;
  angle: number;
  radius: number;
  primary: string;
  secondary: string;
  good: boolean;
}) {
  const [x, y] = polar(cx, cy, radius, angle);
  return (
    <g>
      <text
        x={x}
        y={y - 4}
        textAnchor="middle"
        className="fill-slate-700"
        style={{ fontSize: 11, fontWeight: 600 }}
      >
        {primary}
      </text>
      <text
        x={x}
        y={y + 9}
        textAnchor="middle"
        style={{
          fontSize: 10,
          fill: good ? "var(--color-cat, #047857)" : "var(--color-hung, #be123c)",
        }}
      >
        {secondary}
      </text>
    </g>
  );
}

function Needle({ cx, cy, angle, radius }: { cx: number; cy: number; angle: number; radius: number }) {
  const [x, y] = polar(cx, cy, radius, angle);
  return (
    <g>
      <line x1={cx} y1={cy} x2={x} y2={y} stroke="#0f172a" strokeWidth="2" />
      <circle cx={x} cy={y} r="5" fill="#0f172a" />
      <circle cx={cx} cy={cy} r="3.5" fill="#0f172a" />
    </g>
  );
}

/** 0 độ = lên trên, tăng theo chiều kim đồng hồ — đúng cách đọc la bàn. */
function polar(cx: number, cy: number, r: number, angleDeg: number): [number, number] {
  const rad = ((angleDeg - 90) * Math.PI) / 180;
  return [cx + r * Math.cos(rad), cy + r * Math.sin(rad)];
}

function sector(
  cx: number,
  cy: number,
  rInner: number,
  rOuter: number,
  fromDeg: number,
  toDeg: number,
): string {
  const [x1, y1] = polar(cx, cy, rOuter, fromDeg);
  const [x2, y2] = polar(cx, cy, rOuter, toDeg);
  const [x3, y3] = polar(cx, cy, rInner, toDeg);
  const [x4, y4] = polar(cx, cy, rInner, fromDeg);
  return [
    `M ${x1} ${y1}`,
    `A ${rOuter} ${rOuter} 0 0 1 ${x2} ${y2}`,
    `L ${x3} ${y3}`,
    `A ${rInner} ${rInner} 0 0 0 ${x4} ${y4}`,
    "Z",
  ].join(" ");
}
