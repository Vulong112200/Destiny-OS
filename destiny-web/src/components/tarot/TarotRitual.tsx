"use client";

import { useCallback, useMemo, useState } from "react";
import { LazyMotion, domAnimation, m, useReducedMotion } from "framer-motion";
import { RotateCcw, Shuffle } from "lucide-react";
import { TAROT_DECK_SIZE } from "@/lib/types";
import { TarotBackDefs, TarotCardBack } from "@/components/icons/TarotCardBack";

/**
 * Chọn lá: xào bài, trải bài úp, rồi nhặt.
 *
 * <p>Thay cho `TarotDeckPicker`, vốn là 78 ô vuông xám cao 36px trong một hộp
 * cuộn — ô chưa chọn render đúng một ký tự khoảng trắng. Nó đúng về mặt dữ
 * liệu và không giống một lượt bói bài ở bất kỳ điểm nào.
 *
 * <p><strong>Hợp đồng với backend không đổi một chữ.</strong> `picked` vẫn là
 * các ô 1-based trong bộ 78 lá đã xào, không trùng nhau, và form vẫn chỉ gửi
 * đi khi đủ số lá. Bộ bài được xào từ seed ở backend, nên bấm vào lá thứ 47
 * trong quạt không nói gì về việc lá đó là gì — mức ngẫu nhiên y hệt khi để
 * máy tự bốc. Thứ khác đi là ai đã chọn, và kết quả ghi rõ điều đó.
 *
 * <p>Ba màn: idle (bộ bài úp + nút Xào bài) → shuffling (~1,1s, bỏ qua nếu
 * người dùng đã tắt hiệu ứng ở hệ điều hành) → spread (78 lá úp trong ba cung).
 */

type Phase = "idle" | "shuffling" | "spread";

/** Ba cung, mỗi cung 26 lá. */
const ROWS = 3;
const PER_ROW = TAROT_DECK_SIZE / ROWS;

export function TarotRitual({
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
  const [phase, setPhase] = useState<Phase>(picked.length > 0 ? "spread" : "idle");
  const reduce = useReducedMotion();
  const full = picked.length >= cardsNeeded;

  const toggle = useCallback(
    (slot: number) => {
      if (disabled) return;
      if (picked.includes(slot)) {
        onChange(picked.filter((s) => s !== slot));
        return;
      }
      // Không âm thầm đẩy lá cũ ra khi đã đủ: người dùng vừa bấm một lá và cần
      // biết vì sao không có gì xảy ra.
      if (picked.length >= cardsNeeded) return;
      onChange([...picked, slot]);
    },
    [disabled, picked, onChange, cardsNeeded],
  );

  function startShuffle() {
    if (reduce) {
      setPhase("spread");
      return;
    }
    setPhase("shuffling");
    window.setTimeout(() => setPhase("spread"), 1100);
  }

  const rows = useMemo(
    () =>
      Array.from({ length: ROWS }, (_, r) =>
        Array.from({ length: PER_ROW }, (_, i) => r * PER_ROW + i + 1),
      ),
    [],
  );

  return (
    <LazyMotion features={domAnimation} strict>
      <div className="rounded-lg border border-slate-200 bg-gradient-to-b from-slate-50 to-white p-3">
        <TarotBackDefs />

        {phase === "idle" && <IdleDeck onShuffle={startShuffle} disabled={disabled} />}
        {phase === "shuffling" && <ShufflingDeck />}

        {phase === "spread" && (
          <>
            <div className="mb-2 flex flex-wrap items-center justify-between gap-2">
              <span className="text-xs text-slate-600">
                Đã chọn <span className="font-medium text-slate-900">{picked.length}</span>/
                {cardsNeeded} lá
              </span>
              <div className="flex items-center gap-3">
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
                <button
                  type="button"
                  onClick={() => {
                    onChange([]);
                    setPhase("idle");
                  }}
                  disabled={disabled}
                  className="inline-flex items-center gap-1 text-xs text-slate-600 underline underline-offset-2 hover:text-slate-900 disabled:opacity-50"
                >
                  <RotateCcw aria-hidden className="h-3 w-3" />
                  Xào lại
                </button>
              </div>
            </div>

            <div className="space-y-2 rounded-md bg-slate-900/5 px-2 py-4">
              {rows.map((row, rowIndex) => (
                <div
                  key={rowIndex}
                  className="flex justify-center pr-[26%]"
                  role="group"
                  aria-label={`Cung bài ${rowIndex + 1} trên ${ROWS}`}
                >
                  {row.map((slot, i) => {
                    const order = picked.indexOf(slot);
                    const isPicked = order >= 0;
                    return (
                      <FannedCard
                        key={slot}
                        slot={slot}
                        indexInRow={i}
                        order={order}
                        isPicked={isPicked}
                        disabled={disabled || (full && !isPicked)}
                        reduce={Boolean(reduce)}
                        onPick={() => toggle(slot)}
                      />
                    );
                  })}
                </div>
              ))}
            </div>

            {picked.length > 0 && (
              <PickedTray picked={picked} onRemove={toggle} disabled={disabled} />
            )}

            <p className="mt-3 text-[11px] leading-relaxed text-slate-500">
              Bộ bài đã được xào trước khi úp, nên{" "}
              <strong>không ai biết lá nào nằm ở đâu</strong> — kể cả bạn và kể cả hệ thống, cho
              tới khi lật. Mức ngẫu nhiên đúng bằng khi để hệ thống tự bốc; khác biệt duy nhất là{" "}
              <strong>bạn</strong> là người chọn, và kết quả sẽ ghi rõ điều đó.
              {full ? " Đã đủ số lá — bỏ chọn một lá nếu muốn đổi." : ""}
            </p>
          </>
        )}
      </div>
    </LazyMotion>
  );
}

/** Bộ bài úp, chưa xào. */
function IdleDeck({ onShuffle, disabled }: { onShuffle: () => void; disabled: boolean }) {
  return (
    <div className="flex flex-col items-center gap-4 py-6">
      <div className="relative h-40 w-26">
        {[0, 1, 2, 3, 4].map((i) => (
          <div
            key={i}
            className="absolute inset-0"
            style={{ transform: `translate(${i * 2}px, ${i * -2}px) rotate(${i * 1.4 - 2.8}deg)` }}
          >
            <TarotCardBack className="h-40 w-26 rounded-lg shadow-md" />
          </div>
        ))}
      </div>
      <button
        type="button"
        onClick={onShuffle}
        disabled={disabled}
        className="inline-flex items-center gap-2 rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white transition hover:bg-slate-700 disabled:opacity-50"
      >
        <Shuffle aria-hidden className="h-4 w-4" />
        Xào bài
      </button>
      <p className="max-w-md text-center text-[11px] leading-relaxed text-slate-500">
        Bấm để xào bộ 78 lá rồi trải úp. Bạn sẽ tự chọn lá — hệ thống không biết trước lá nào nằm
        ở đâu, và bạn cũng vậy.
      </p>
    </div>
  );
}

/** Màn xào. Thuần trang trí, khoảng 1,1 giây, và không chạy nếu đã tắt hiệu ứng. */
function ShufflingDeck() {
  return (
    <div className="flex flex-col items-center gap-4 py-6" role="status" aria-live="polite">
      <div className="relative h-40 w-26">
        {[0, 1, 2, 3, 4].map((i) => (
          <m.div
            key={i}
            className="absolute inset-0"
            initial={{ x: 0, y: 0, rotate: 0 }}
            animate={{
              x: [0, i % 2 === 0 ? -46 : 46, 0, i % 2 === 0 ? 34 : -34, 0],
              y: [0, -6 * i, 0, -3 * i, 0],
              rotate: [0, i % 2 === 0 ? -9 : 9, 0, i % 2 === 0 ? 5 : -5, 0],
            }}
            transition={{ duration: 1.05, times: [0, 0.25, 0.5, 0.75, 1], ease: "easeInOut" }}
          >
            <TarotCardBack className="h-40 w-26 rounded-lg shadow-md" />
          </m.div>
        ))}
      </div>
      <p className="text-sm text-slate-600">Đang xào bài…</p>
    </div>
  );
}

/**
 * Một lá trong quạt.
 *
 * <p>Các lá chồng lên nhau khoảng 52% bề ngang và nghiêng dần theo vị trí
 * trong cung — dáng một tay chia bài trải ra mặt bàn.
 *
 * <p>Ba cung 26 lá thay vì một quạt 78 lá là lựa chọn có lý do: ở 78 lá thì
 * mỗi lá chỉ được khoảng 2 độ, và vùng bấm chồng lên nhau tới mức không dùng
 * nổi dưới 900px bề ngang.
 */
function FannedCard({
  slot,
  indexInRow,
  order,
  isPicked,
  disabled,
  reduce,
  onPick,
}: {
  slot: number;
  indexInRow: number;
  order: number;
  isPicked: boolean;
  disabled: boolean;
  reduce: boolean;
  onPick: () => void;
}) {
  const tilt = (indexInRow - (PER_ROW - 1) / 2) * 0.9;
  const lift = -Math.sin((Math.PI * indexInRow) / (PER_ROW - 1)) * 8;

  return (
    <m.button
      type="button"
      onClick={onPick}
      disabled={disabled}
      aria-pressed={isPicked}
      aria-label={isPicked ? `Lá ở ô ${slot}, đã chọn, thứ ${order + 1}` : `Lá ở ô ${slot}`}
      title={isPicked ? `Lá thứ ${order + 1}` : `Ô ${slot}`}
      className="relative -mr-[6.5%] w-[clamp(1.5rem,3.2vw,2.4rem)] shrink-0 rounded focus:z-30 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-500 disabled:cursor-not-allowed"
      style={{ rotate: `${tilt}deg`, zIndex: isPicked ? 25 : indexInRow }}
      initial={reduce ? false : { opacity: 0, y: 24 }}
      animate={{ opacity: disabled && !isPicked ? 0.4 : 1, y: isPicked ? lift - 14 : lift }}
      transition={
        reduce
          ? { duration: 0 }
          : { type: "spring", stiffness: 300, damping: 26, delay: indexInRow * 0.012 }
      }
      whileHover={disabled ? undefined : { y: lift - 10 }}
    >
      <TarotCardBack
        className={`aspect-[100/160] w-full rounded shadow-sm ${
          isPicked ? "ring-2 ring-indigo-500" : "ring-1 ring-black/10"
        }`}
      />
      {isPicked && (
        <span className="absolute inset-x-0 -top-2 mx-auto flex h-4 w-4 items-center justify-center rounded-full bg-indigo-600 text-[10px] font-semibold text-white">
          {order + 1}
        </span>
      )}
    </m.button>
  );
}

/**
 * Các lá đã chọn, theo thứ tự bốc.
 *
 * <p>Thứ tự mới là thứ quyết định lá nào rơi vào vị trí nào của kiểu trải —
 * không phải số ô. Hiển thị nó ra để lựa chọn của người dùng không biến mất
 * vào giữa một quạt 78 lá.
 */
function PickedTray({
  picked,
  onRemove,
  disabled,
}: {
  picked: number[];
  onRemove: (slot: number) => void;
  disabled: boolean;
}) {
  return (
    <div className="mt-3 rounded-md border border-slate-200 bg-white p-2">
      <p className="mb-2 text-[11px] font-medium text-slate-600">
        Đã chọn, theo thứ tự bốc — thứ tự này quyết định lá nào vào vị trí nào của kiểu trải
      </p>
      <ul className="flex flex-wrap gap-2">
        {picked.map((slot, i) => (
          <li key={slot}>
            <button
              type="button"
              onClick={() => onRemove(slot)}
              disabled={disabled}
              title={`Bỏ chọn lá thứ ${i + 1} (ô ${slot})`}
              className="relative block w-12 rounded transition hover:-translate-y-0.5 disabled:opacity-50"
            >
              <TarotCardBack className="aspect-[100/160] w-full rounded shadow ring-2 ring-indigo-500" />
              <span className="absolute inset-x-0 -top-1.5 mx-auto flex h-4 w-4 items-center justify-center rounded-full bg-indigo-600 text-[10px] font-semibold text-white">
                {i + 1}
              </span>
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
