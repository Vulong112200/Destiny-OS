/**
 * Mặt lưng lá bài.
 *
 * <p>Trước đây mỗi lá tự vẽ một hình chữ nhật, một vòng tròn và một dấu cộng
 * bằng vàng — ba nét, đủ để nói "đây là mặt sau" và không đủ để trông giống
 * một lá bài. Còn màn chọn lá thì không có mặt lưng nào cả: 78 ô xám trống.
 *
 * <p>Hình được định nghĩa **một lần** trong `<symbol>` và mỗi lá chỉ tham chiếu
 * bằng `<use>`. Điều đó có ý nghĩa thật ở đây: màn trải bài hiện 78 lá cùng
 * lúc, và 78 cây SVG nội tuyến là thứ khiến trình duyệt bò ra.
 */

export const TAROT_BACK_SYMBOL_ID = "tarot-card-back";

/** Đặt đúng một lần ở gốc của bất kỳ màn nào có mặt lưng bài. */
export function TarotBackDefs() {
  return (
    <svg width="0" height="0" aria-hidden className="absolute">
      <defs>
        <symbol id={TAROT_BACK_SYMBOL_ID} viewBox="0 0 100 160">
          <rect width="100" height="160" rx="7" fill="var(--color-la-nen, #0f172a)" />
          <rect
            x="4.5"
            y="4.5"
            width="91"
            height="151"
            rx="4.5"
            fill="none"
            stroke="var(--color-la-vien, #d4af37)"
            strokeWidth="1.2"
            opacity="0.9"
          />
          <rect
            x="9"
            y="9"
            width="82"
            height="142"
            rx="3"
            fill="none"
            stroke="var(--color-la-vien, #d4af37)"
            strokeWidth="0.5"
            opacity="0.55"
          />
          {/* Hoa thị 8 cánh ở giữa. */}
          <g
            fill="none"
            stroke="var(--color-la-vien, #d4af37)"
            strokeWidth="0.8"
            opacity="0.85"
          >
            <circle cx="50" cy="80" r="26" />
            <circle cx="50" cy="80" r="19" opacity="0.7" />
            <circle cx="50" cy="80" r="6" />
            {Array.from({ length: 8 }, (_, i) => {
              const a = (i * Math.PI) / 4;
              return (
                <line
                  key={i}
                  x1={50 + Math.cos(a) * 6}
                  y1={80 + Math.sin(a) * 6}
                  x2={50 + Math.cos(a) * 26}
                  y2={80 + Math.sin(a) * 26}
                />
              );
            })}
            {Array.from({ length: 8 }, (_, i) => {
              const a = (i * Math.PI) / 4 + Math.PI / 8;
              return (
                <circle key={`d${i}`} cx={50 + Math.cos(a) * 22.5} cy={80 + Math.sin(a) * 22.5} r="1.6" fill="var(--color-la-vien, #d4af37)" stroke="none" />
              );
            })}
          </g>
          {/* Bốn góc. */}
          {[
            [17, 25],
            [83, 25],
            [17, 135],
            [83, 135],
          ].map(([cx, cy]) => (
            <circle
              key={`${cx}-${cy}`}
              cx={cx}
              cy={cy}
              r="4"
              fill="none"
              stroke="var(--color-la-vien, #d4af37)"
              strokeWidth="0.7"
              opacity="0.75"
            />
          ))}
        </symbol>
      </defs>
    </svg>
  );
}

/** Một mặt lưng. Cần `<TarotBackDefs/>` có mặt đâu đó trên trang. */
export function TarotCardBack({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 100 160" className={className} aria-hidden preserveAspectRatio="none">
      <use href={`#${TAROT_BACK_SYMBOL_ID}`} />
    </svg>
  );
}
