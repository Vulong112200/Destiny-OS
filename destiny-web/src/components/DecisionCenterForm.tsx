"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { ApiError, runScenario } from "@/lib/api";
import type { SupportedScenarioType, TarotSpreadName } from "@/lib/types";

const SPREAD_OPTIONS: { value: TarotSpreadName; label: string; cardCount: number }[] = [
  { value: "PAST_PRESENT_FUTURE", label: "Quá khứ – Hiện tại – Tương lai", cardCount: 3 },
  { value: "CHOICE_A_B", label: "Lựa chọn A – B", cardCount: 2 },
  { value: "SITUATION_CHALLENGE_ADVICE", label: "Tình huống – Thử thách – Lời khuyên", cardCount: 3 },
];

/**
 * The Decision Center intake form (UI_UX_VIETNAMESE_SPEC section 3, first
 * three steps: chọn chủ đề -> nhập câu hỏi/context -> hệ thống áp dụng).
 * Only BUSINESS and DAILY_ACTION are offered - the only two scenarios with
 * a real applicability policy (ScenarioRegistry); offering the other eight
 * would mean either guessing a policy or presenting a request that always
 * comes back "policyDefined: false", neither of which belongs on the
 * primary intake form.
 */
export function DecisionCenterForm() {
  const router = useRouter();
  const [scenarioType, setScenarioType] = useState<SupportedScenarioType>("BUSINESS");
  const [fullName, setFullName] = useState("");
  const [birthDate, setBirthDate] = useState("");
  const [useTarot, setUseTarot] = useState(true);
  const [spread, setSpread] = useState<TarotSpreadName>("PAST_PRESENT_FUTURE");
  const [question, setQuestion] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);

    const hasNumerology = fullName.trim() !== "" && birthDate !== "";
    if (!hasNumerology && !useTarot) {
      setError("Cần ít nhất một hệ thống: nhập họ tên + ngày sinh, hoặc bật rút bài Tarot.");
      return;
    }

    setSubmitting(true);
    try {
      const result = await runScenario(scenarioType, {
        numerology: hasNumerology ? { fullName: fullName.trim(), birthDate } : null,
        tarot: useTarot ? { spread, seed: null, question: question.trim() || null } : null,
      });
      router.push(`/ket-qua/${result.calculationId}`);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Không thể kết nối tới hệ thống tính toán.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-8">
      <fieldset className="space-y-2">
        <legend className="text-sm font-semibold text-slate-900">1. Bạn đang muốn xem điều gì?</legend>
        <div className="flex gap-3">
          {(["BUSINESS", "DAILY_ACTION"] as const).map((type) => (
            <label
              key={type}
              className={`flex-1 cursor-pointer rounded-lg border px-4 py-3 text-sm ${
                scenarioType === type
                  ? "border-slate-900 bg-slate-900 text-white"
                  : "border-slate-200 text-slate-700 hover:border-slate-400"
              }`}
            >
              <input
                type="radio"
                name="scenarioType"
                value={type}
                checked={scenarioType === type}
                onChange={() => setScenarioType(type)}
                className="sr-only"
              />
              {type === "BUSINESS" ? "Mở rộng kinh doanh" : "Hôm nay nên làm gì"}
            </label>
          ))}
        </div>
        <p className="text-xs text-slate-500">
          Đây là 2 chủ đề duy nhất hiện có chính sách áp dụng hệ thống đầy đủ.
        </p>
      </fieldset>

      <fieldset className="space-y-3 rounded-lg border border-slate-200 p-4">
        <legend className="px-1 text-sm font-semibold text-slate-900">Thần số học (tùy chọn)</legend>
        <div className="grid gap-3 sm:grid-cols-2">
          <label className="block text-sm">
            <span className="mb-1 block text-slate-600">Họ tên đầy đủ</span>
            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="Nguyễn Văn A"
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
            />
          </label>
          <label className="block text-sm">
            <span className="mb-1 block text-slate-600">Ngày sinh</span>
            <input
              type="date"
              value={birthDate}
              onChange={(e) => setBirthDate(e.target.value)}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
            />
          </label>
        </div>
      </fieldset>

      <fieldset className="space-y-3 rounded-lg border border-slate-200 p-4">
        <legend className="px-1 text-sm font-semibold text-slate-900">
          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={useTarot}
              onChange={(e) => setUseTarot(e.target.checked)}
            />
            Rút bài Tarot
          </label>
        </legend>
        {useTarot && (
          <div className="space-y-3">
            <label className="block text-sm">
              <span className="mb-1 block text-slate-600">Kiểu trải bài</span>
              <select
                value={spread}
                onChange={(e) => setSpread(e.target.value as TarotSpreadName)}
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
              >
                {SPREAD_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label} ({opt.cardCount} lá)
                  </option>
                ))}
              </select>
            </label>
            <label className="block text-sm">
              <span className="mb-1 block text-slate-600">2. Câu hỏi / bối cảnh (tùy chọn)</span>
              <textarea
                value={question}
                onChange={(e) => setQuestion(e.target.value)}
                rows={2}
                placeholder="Tôi có nên mở rộng kinh doanh không?"
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
              />
            </label>
          </div>
        )}
      </fieldset>

      {error && (
        <p role="alert" className="rounded-md bg-rose-50 px-4 py-3 text-sm text-rose-800">
          {error}
        </p>
      )}

      <button
        type="submit"
        disabled={submitting}
        className="w-full rounded-md bg-slate-900 px-4 py-3 text-sm font-semibold text-white hover:bg-slate-700 disabled:opacity-50"
      >
        {submitting ? "Đang tính toán…" : "3. Tính toán"}
      </button>
    </form>
  );
}
