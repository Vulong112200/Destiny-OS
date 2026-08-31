import Link from "next/link";
import type { ScenarioRunResponse, SupportedScenarioType } from "@/lib/types";
import { SCENARIO_META } from "@/lib/scenarioMeta";
import { LabeledBadge } from "./LabeledBadge";

/**
 * The top of a result: what was asked, and the one-line verdict.
 *
 * <p>The page used to open with `<h1>Kết quả — CAREER</h1>` — a raw enum name,
 * which UI_UX_VIETNAMESE_SPEC §1 forbids on its own — followed immediately by
 * a retention notice and an engine list, so the first thing a user saw after
 * asking a question was housekeeping. Fusion's conclusion, which §4 ranks
 * first, sat below all of it.
 *
 * <p>Restating the question here is not decoration. A result is a permanent,
 * shareable link; opened a week later, or by someone the link was sent to,
 * "Cần thận trọng" means nothing without the question it answers.
 */
export function ResultHero({
  result,
  scenario,
}: {
  result: ScenarioRunResponse;
  scenario: SupportedScenarioType | null;
}) {
  const meta = scenario ? SCENARIO_META[scenario] : null;
  const context = result.context ?? null;
  const scenarioLabel = meta?.labelVi ?? result.scenarioId;

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm lg:p-8">
      <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <span className="rounded-full bg-slate-900 px-3 py-1 text-sm font-medium text-white">
              {scenarioLabel}
            </span>
            {context?.focusLabel && (
              <span className="rounded-full border border-slate-300 px-3 py-1 text-sm text-slate-700">
                {context.focusLabel}
              </span>
            )}
          </div>

          {context?.question ? (
            <blockquote className="mt-4 border-l-4 border-slate-300 pl-4">
              <p className="text-xl leading-snug font-medium text-slate-900 lg:text-2xl">
                “{context.question}”
              </p>
            </blockquote>
          ) : (
            <h1 className="mt-4 text-xl font-bold text-slate-900 lg:text-2xl">
              Kết quả — {scenarioLabel}
            </h1>
          )}

          {meta && (
            <p className="mt-2 text-sm text-slate-500">{meta.blurb}</p>
          )}
        </div>

        {/*
          The verdict, given the visual weight UI_UX_VIETNAMESE_SPEC §4 asks
          for by ranking it first — but placed beside the question rather than
          above it, so the two are read together.
        */}
        {result.fusion && (
          <div className="shrink-0 rounded-xl bg-slate-50 px-6 py-5 lg:min-w-56 lg:text-center">
            <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
              Kết luận tổng hợp
            </p>
            <div className="mt-2 text-xl">
              <LabeledBadge value={result.fusion.overallOutcome} />
            </div>
            <p className="mt-3 text-xs text-slate-500">
              Tổng hợp từ {result.engines.length} hệ · {result.signals.length} tín hiệu
            </p>
          </div>
        )}
      </div>

      {!result.policyDefined && (
        <p className="mt-5 rounded-md bg-slate-100 px-4 py-3 text-sm text-slate-700">
          Chủ đề này chưa có chính sách áp dụng hệ thống cụ thể — chưa hệ thống nào được chạy, để
          tránh suy đoán một chính sách chưa được xác định.
        </p>
      )}

      <div className="mt-5 flex flex-wrap gap-3 border-t border-slate-100 pt-4 text-sm">
        <Link
          href="/trung-tam-quyet-dinh"
          className="font-medium text-slate-700 underline underline-offset-4 hover:text-slate-900"
        >
          Hỏi một câu khác
        </Link>
      </div>
    </section>
  );
}
