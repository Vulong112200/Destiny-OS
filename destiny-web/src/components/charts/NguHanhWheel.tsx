import type { LabelRegistries } from "@/lib/types";
import { labelOf } from "@/lib/labels";

/**
 * Số lần xuất hiện của mỗi hành, vẽ trên ngũ giác Ngũ Hành.
 *
 * <p>Năm đỉnh xếp theo vòng tương sinh (Mộc → Hỏa → Thổ → Kim → Thủy → Mộc),
 * nên cạnh ngoài của ngũ giác chính là quan hệ sinh, còn các đường chéo bên
 * trong chính là quan hệ khắc. Đó là lý do dùng hình này thay vì một biểu đồ
 * cột: cấu trúc của Ngũ Hành nằm ở quan hệ giữa các hành, và biểu đồ cột vứt
 * bỏ đúng phần đó.
 *
 * <p><strong>Mỗi nhóm đếm một vòng riêng.</strong> `BaziEngine` ghi rõ trong
 * chính dữ liệu rằng ba nhóm (Thiên Can, Địa Chi, Tàng Can) là số đếm thô và
 * <em>không được cộng gộp</em> — các trường phái không thống nhất tính cái gì.
 * Gộp ba nhóm vào một vòng cho đẹp sẽ là dựng lại bằng hình đúng cái phép cộng
 * mà dữ liệu vừa từ chối.
 *
 * <p>Đơn vị là <strong>số lần xuất hiện</strong>, không phải phần trăm và
 * không phải cường độ. Cường độ Ngũ Hành là việc của mục nghiên cứu R3, hiện
 * chưa mở.
 */

interface ElementNode {
  key: string;
  angle: number;
  color: string;
  soft: string;
}

/** Thứ tự tương sinh, bắt đầu từ Mộc ở đỉnh trên. */
const NODES: ElementNode[] = [
  { key: "WOOD", angle: 0, color: "var(--color-moc, #15803d)", soft: "var(--color-moc-soft, #dcfce7)" },
  { key: "FIRE", angle: 72, color: "var(--color-hoa, #b91c1c)", soft: "var(--color-hoa-soft, #fee2e2)" },
  { key: "EARTH", angle: 144, color: "var(--color-tho, #a16207)", soft: "var(--color-tho-soft, #fef3c7)" },
  { key: "METAL", angle: 216, color: "var(--color-kim, #57534e)", soft: "var(--color-kim-soft, #f5f5f4)" },
  { key: "WATER", angle: 288, color: "var(--color-thuy, #1d4ed8)", soft: "var(--color-thuy-soft, #dbeafe)" },
];

export function NguHanhWheel({
  title,
  counts,
  labels,
}: {
  title: string;
  counts: Record<string, unknown>;
  labels: LabelRegistries;
}) {
  const size = 180;
  const c = size / 2;
  const rRing = 62;
  const values = NODES.map((n) => toCount(counts[n.key]));
  const max = Math.max(1, ...values);

  return (
    <figure className="flex flex-col items-center">
      <figcaption className="mb-1 text-center text-xs font-medium text-slate-700">
        {title}
      </figcaption>
      <svg
        viewBox={`0 0 ${size} ${size}`}
        className="h-44 w-44"
        role="img"
        aria-label={`${title}: ${NODES.map(
          (n, i) => `${labelOf(labels, "FiveElement", n.key, n.key)} ${values[i]}`,
        ).join(", ")}`}
      >
        {/* Khắc: các đường chéo trong ngũ giác. */}
        {NODES.map((n, i) => {
          const target = NODES[(i + 2) % NODES.length];
          const [x1, y1] = polar(c, c, rRing, n.angle);
          const [x2, y2] = polar(c, c, rRing, target.angle);
          return (
            <line
              key={`khac-${n.key}`}
              x1={x1}
              y1={y1}
              x2={x2}
              y2={y2}
              stroke="#cbd5e1"
              strokeWidth="0.8"
              strokeDasharray="3 3"
            />
          );
        })}

        {/* Sinh: cạnh ngoài. */}
        <polygon
          points={NODES.map((n) => polar(c, c, rRing, n.angle).join(",")).join(" ")}
          fill="none"
          stroke="#94a3b8"
          strokeWidth="1"
        />

        {NODES.map((n, i) => {
          const [x, y] = polar(c, c, rRing, n.angle);
          const count = values[i];
          // Diện tích tỉ lệ với số đếm, nên bán kính lấy căn - một hình tròn
          // bán kính gấp đôi trông gấp bốn, và mắt đọc theo diện tích.
          const r = 8 + 13 * Math.sqrt(count / max);
          return (
            <g key={n.key}>
              <circle
                cx={x}
                cy={y}
                r={count === 0 ? 8 : r}
                fill={count === 0 ? "#f8fafc" : n.soft}
                stroke={n.color}
                strokeWidth={count === 0 ? 1 : 1.5}
                strokeDasharray={count === 0 ? "2 2" : undefined}
              />
              <text
                x={x}
                y={y + 4}
                textAnchor="middle"
                style={{ fontSize: 12, fontWeight: 600, fill: n.color }}
              >
                {count}
              </text>
              <text
                x={polar(c, c, rRing + 24, n.angle)[0]}
                y={polar(c, c, rRing + 24, n.angle)[1] + 4}
                textAnchor="middle"
                className="fill-slate-600"
                style={{ fontSize: 10 }}
              >
                {labelOf(labels, "FiveElement", n.key, n.key)}
              </text>
            </g>
          );
        })}
      </svg>
    </figure>
  );
}

function toCount(value: unknown): number {
  return typeof value === "number" && Number.isFinite(value) ? value : 0;
}

/** 0 độ = đỉnh trên, tăng theo chiều kim đồng hồ. */
function polar(cx: number, cy: number, r: number, angleDeg: number): [number, number] {
  const rad = ((angleDeg - 90) * Math.PI) / 180;
  return [cx + r * Math.cos(rad), cy + r * Math.sin(rad)];
}
