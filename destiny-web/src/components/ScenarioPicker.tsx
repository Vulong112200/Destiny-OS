"use client";

import type { SupportedScenarioType } from "@/lib/types";
import { SCENARIO_META, SCENARIO_ORDER } from "@/lib/scenarioMeta";

/**
 * Steps 1 and 2 of UI_UX_VIETNAMESE_SPEC §3's flow ("Chọn chủ đề" then "Nhập
 * câu hỏi/context"), which the form previously collapsed into one radio grid.
 *
 * <p>Two things were wrong before. The question box existed only inside the
 * Tarot section, so a user who did not enable Tarot was never asked what they
 * wanted to know — and even when they were, the backend dropped it. And the
 * scenarios were one-word labels, so "Quan hệ" had to absorb dating, marriage
 * and family alike; a user asking about a life partner and a user asking about
 * a falling-out picked the same button and got the same page.
 *
 * <p>The focus chips fix the second without inventing metaphysics: they are a
 * **user-intent label**, used to phrase the question and to frame the result,
 * and they change no calculation. That is stated to the user rather than left
 * implied, because a UI control that looks like it narrows the reading and
 * silently does not would be the dishonest version of this.
 */
export function ScenarioPicker({
  scenarioType,
  onScenarioChange,
  focusId,
  onFocusChange,
  question,
  onQuestionChange,
}: {
  scenarioType: SupportedScenarioType;
  onScenarioChange: (value: SupportedScenarioType) => void;
  focusId: string;
  onFocusChange: (value: string) => void;
  question: string;
  onQuestionChange: (value: string) => void;
}) {
  const meta = SCENARIO_META[scenarioType];
  const activeFocus = meta.focuses.find((f) => f.id === focusId) ?? null;

  function selectScenario(next: SupportedScenarioType) {
    onScenarioChange(next);
    // A focus belongs to its scenario; carrying "Đổi việc" into "Tài chính"
    // would leave a label that no longer means anything.
    onFocusChange("");
  }

  function selectFocus(id: string) {
    const next = focusId === id ? "" : id;
    onFocusChange(next);
    // Only ever *offer* the hint - never overwrite something the user typed.
    if (next !== "" && question.trim() === "") {
      const hint = meta.focuses.find((f) => f.id === next)?.questionHint;
      if (hint) onQuestionChange(hint);
    }
  }

  return (
    <div className="space-y-6">
      <fieldset>
        <legend className="mb-3 text-base font-semibold text-slate-900">
          1. Bạn đang muốn xem điều gì?
        </legend>
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {SCENARIO_ORDER.map((type) => {
            const item = SCENARIO_META[type];
            const selected = scenarioType === type;
            return (
              <label
                key={type}
                className={`cursor-pointer rounded-xl border p-4 transition-colors ${
                  selected
                    ? "border-slate-900 bg-slate-900 text-white"
                    : "border-slate-200 bg-white text-slate-800 hover:border-slate-400"
                }`}
              >
                <input
                  type="radio"
                  name="scenarioType"
                  value={type}
                  checked={selected}
                  onChange={() => selectScenario(type)}
                  className="sr-only"
                />
                <span className="block font-medium">{item.labelVi}</span>
                <span
                  className={`mt-1 block text-xs ${selected ? "text-slate-300" : "text-slate-500"}`}
                >
                  {item.blurb}
                </span>
              </label>
            );
          })}
        </div>
        <p className="mt-3 text-xs text-slate-500">
          9 chủ đề đã có chính sách áp dụng hệ thống thật. Riêng &quot;Tương hợp&quot; (so hai lá
          số trước khi cưới/hợp tác) chưa có ở đây — hệ thống hiện chỉ nhận một lá số mỗi lượt
          tính, còn thực hành truyền thống mạnh nhất cho tương hợp lại cần hai.
        </p>
      </fieldset>

      <fieldset>
        <legend className="mb-1 text-base font-semibold text-slate-900">
          2. Cụ thể hơn một chút
        </legend>
        <p className="mb-3 text-xs text-slate-500">
          Chọn trọng tâm và viết câu hỏi của bạn. Phần này{" "}
          <span className="font-medium text-slate-700">không làm thay đổi phép tính nào</span> —
          lá số, lá bài và quẻ vẫn y hệt. Nó quyết định phần nào của kết quả được đưa lên trước và
          lời diễn giải nói về chuyện gì.
        </p>

        <div className="flex flex-wrap gap-2">
          {meta.focuses.map((focus) => {
            const selected = focusId === focus.id;
            return (
              <button
                key={focus.id}
                type="button"
                onClick={() => selectFocus(focus.id)}
                aria-pressed={selected}
                className={`rounded-full border px-4 py-1.5 text-sm transition-colors ${
                  selected
                    ? "border-slate-900 bg-slate-900 text-white"
                    : "border-slate-300 bg-white text-slate-700 hover:border-slate-500"
                }`}
              >
                {focus.label}
              </button>
            );
          })}
        </div>

        <label className="mt-4 block">
          <span className="mb-1 block text-sm text-slate-600">
            Câu hỏi của bạn{" "}
            <span className="text-slate-400">(không bắt buộc, nhưng nên có)</span>
          </span>
          <textarea
            value={question}
            onChange={(e) => onQuestionChange(e.target.value)}
            rows={3}
            maxLength={500}
            placeholder={
              activeFocus?.questionHint ?? "Ví dụ: Tôi có nên nhận lời mời công việc này không?"
            }
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
          />
          <span className="mt-1 flex justify-between text-xs text-slate-500">
            <span>
              Viết cụ thể thì phần diễn giải bám sát hơn. Câu hỏi được lưu cùng kết quả để bạn mở
              lại còn biết mình đã hỏi gì.
            </span>
            <span className="shrink-0 tabular-nums">{question.length}/500</span>
          </span>
        </label>
      </fieldset>
    </div>
  );
}
