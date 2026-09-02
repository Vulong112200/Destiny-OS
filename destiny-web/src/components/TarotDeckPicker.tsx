"use client";

import { TAROT_DECK_SIZE } from "@/lib/types";

/**
 * Bộ 78 lá úp mặt để người xem tự chọn.
 *
 * <p>Vì sao có màn này: chủ dự án phê rằng để máy tự bốc thì không đúng với
 * cách bói bài thật, và họ đúng — trong một lượt bói thật, người xem là người
 * chọn lá.
 *
 * <p>Điều phải nói cho đúng, không được nói quá: bộ bài vẫn được **xào từ seed**
 * ở backend, nên khi bạn bấm vào ô 47 thì **không ai biết ô 47 là lá gì**, kể cả
 * bạn. Mức ngẫu nhiên y hệt cách máy tự bốc — thứ khác đi là **ai đã thực hiện
 * lựa chọn**, và kết quả sẽ ghi rõ lá đã đến bằng cách nào. Đây cũng **không
 * phải** cách gọi tên một lá: chọn ô 47 không có nghĩa là chỉ định lá nào.
 *
 * <p>Ô đã chọn hiển thị **thứ tự bốc** (1, 2, 3…) chứ không phải số ô, vì thứ tự
 * mới là thứ quyết định lá nào rơi vào vị trí nào của spread.
 */
export function TarotDeckPicker({
  cardsNeeded,
  picked,
  onChange,
  disabled = false,
}: {
  cardsNeeded: number;
  picked: number[];
  onChange: (next: number[]) => void;
  disabled?: boolean;
}) {
  const full = picked.length >= cardsNeeded;

  function toggle(slot: number) {
    if (disabled) return;
    if (picked.includes(slot)) {
      onChange(picked.filter((s) => s !== slot));
      return;
    }
    // Không âm thầm đẩy lá cũ ra khi đã đủ: người dùng vừa bấm một nút và cần
    // biết vì sao không có gì xảy ra, nên nút bị vô hiệu hóa kèm dòng nhắc ở
    // dưới thay vì lặng lẽ thay lá.
    if (full) return;
    onChange([...picked, slot]);
  }

  return (
    <div className="rounded-lg border border-slate-200 bg-slate-50 p-3">
      <div className="mb-2 flex flex-wrap items-center justify-between gap-2">
        <span className="text-xs text-slate-600">
          Đã chọn <span className="font-medium text-slate-900">{picked.length}</span>/{cardsNeeded} lá
        </span>
        {picked.length > 0 && (
          <button
            type="button"
            onClick={() => onChange([])}
            disabled={disabled}
            className="text-xs text-slate-600 underline underline-offset-2 hover:text-slate-900 disabled:opacity-50"
          >
            Bỏ chọn hết
          </button>
        )}
      </div>

      <div className="max-h-64 overflow-y-auto rounded-md bg-white p-2">
        <div className="grid grid-cols-8 gap-1 sm:grid-cols-10 md:grid-cols-13">
          {Array.from({ length: TAROT_DECK_SIZE }, (_, i) => i + 1).map((slot) => {
            const order = picked.indexOf(slot);
            const isPicked = order >= 0;
            return (
              <button
                key={slot}
                type="button"
                onClick={() => toggle(slot)}
                disabled={disabled || (full && !isPicked)}
                aria-pressed={isPicked}
                aria-label={
                  isPicked ? `Ô ${slot}, đã chọn, lá thứ ${order + 1}` : `Ô ${slot}, chưa chọn`
                }
                title={isPicked ? `Lá thứ ${order + 1}` : `Ô ${slot}`}
                className={
                  "flex h-9 items-center justify-center rounded border text-[11px] font-medium transition " +
                  (isPicked
                    ? "border-indigo-500 bg-indigo-600 text-white"
                    : "border-slate-300 bg-slate-100 text-slate-400 hover:border-indigo-300 hover:bg-indigo-50 disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:border-slate-300 disabled:hover:bg-slate-100")
                }
              >
                {isPicked ? order + 1 : " "}
              </button>
            );
          })}
        </div>
      </div>

      <p className="mt-2 text-[11px] leading-relaxed text-slate-500">
        Bộ bài đã được xào trước khi úp, nên <strong>không ai biết ô nào là lá nào</strong> — kể cả
        bạn và kể cả hệ thống, cho tới khi lật. Mức ngẫu nhiên đúng bằng khi để hệ thống tự bốc;
        khác biệt duy nhất là <strong>bạn</strong> là người chọn, và kết quả sẽ ghi rõ điều đó.
        {full && " Đã đủ số lá — bỏ chọn một ô nếu muốn đổi."}
      </p>
    </div>
  );
}
