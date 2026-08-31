import type { ReactNode } from "react";
import type { LabelRegistries, ScenarioRunResponse } from "@/lib/types";
import { asSupportedScenario } from "@/lib/scenarioMeta";
import { AstrologyChartCard } from "./AstrologyChartCard";
import { BaziChartCard } from "./BaziChartCard";
import { BatTrachCard } from "./BatTrachCard";
import { IChingChartCard } from "./IChingChartCard";
import { LabeledBadge } from "./LabeledBadge";
import { NumerologyResultCard } from "./NumerologyResultCard";
import { ScenarioAnswer } from "./ScenarioAnswer";
import { TarotResultCard } from "./TarotResultCard";

/**
 * One calculation's full explainability record.
 *
 * <p>Ordering follows UI_UX_VIETNAMESE_SPEC §4's priority list, read as the
 * ranking it is rather than a literal top-to-bottom sequence. Fusion's
 * conclusion is first (in `ResultHero`, above this component); the traditional
 * interpretation assembled from authored evidence (§5) comes before the AI
 * summary (§6), so the reading a user acts on is the sourced one and the AI
 * panel is visibly commentary on top of it — §4's "Không để AI text che hard
 * data", enforced by position as well as by styling. Retention, the engine
 * list (§4.2) and the trace ids live in `ResultSidebar`, beside this column
 * rather than interrupting it.
 *
 * <p>`labels` is optional so every existing caller keeps working; without it
 * the Bát Tự chart renders technical names rather than disappearing, which is
 * the right failure mode for a label table that could not be fetched.
 *
 * <p>`narrativeSlot` is a node rather than data so the caller can stream it in
 * behind its own `<Suspense>` boundary — the narrative can take up to
 * `MAX_ATTEMPTS × chain length × timeout` on the provider's model fallback
 * chain, and none of the hard data below should wait on that. See
 * `NarrativePanel`.
 */
export function ResultView({
  result,
  labels = {},
  narrativeSlot = null,
}: {
  result: ScenarioRunResponse;
  labels?: LabelRegistries;
  narrativeSlot?: ReactNode;
}) {
  const scenario = asSupportedScenario(result.scenarioId);

  return (
    <div className="space-y-8">
      <ScenarioAnswer result={result} scenario={scenario} />

      {narrativeSlot}

      {result.fusion && result.fusion.conflicts.length > 0 && (
        <section id="mau-thuan" className="scroll-mt-20">
          <h2 className="mb-3 text-lg font-semibold text-slate-900">
            Điểm khác biệt giữa các phương pháp
          </h2>
          <div className="grid gap-3 xl:grid-cols-2">
            {result.fusion.conflicts.map((conflict, i) => (
              <div key={i} className="rounded-lg border border-rose-200 bg-rose-50 p-4">
                <div className="mb-1 flex flex-wrap items-center gap-2">
                  <LabeledBadge value={conflict.type} />
                  {conflict.dimension && <LabeledBadge value={conflict.dimension} />}
                </div>
                <p className="text-sm text-slate-700">{conflict.description}</p>
                <p className="mt-1 text-xs text-slate-500">
                  Liên quan: {conflict.involvedEngines.join(", ")}
                </p>
              </div>
            ))}
          </div>
        </section>
      )}

      {/*
        Hard data, shown independently of any AI narrative (CLAUDE.md §9).
        Each card renders nothing when its engine did not take part, so this
        costs an absent section rather than an empty one. Laid out as a grid
        because these are reference panels a user compares against each other
        - stacking them was most of the scrolling this page used to demand.
      */}
      <section id="du-lieu" className="scroll-mt-20 space-y-4">
        <h2 className="text-lg font-semibold text-slate-900">Dữ liệu tính toán</h2>
        {/* Bát Tự is a wide four-pillar table; it gets its own full-width row. */}
        <BaziChartCard evidence={result.evidence} labels={labels} />
        <TarotResultCard evidence={result.evidence} scenarioId={result.scenarioId} />
        <div className="grid gap-4 xl:grid-cols-2">
          <NumerologyResultCard evidence={result.evidence} labels={labels} />
          <BatTrachCard evidence={result.evidence} labels={labels} />
          <AstrologyChartCard evidence={result.evidence} labels={labels} />
          <IChingChartCard evidence={result.evidence} labels={labels} />
        </div>
      </section>

      {result.signals.length === 0 && result.engines.length > 0 && (
        <p className="rounded-md bg-slate-100 px-4 py-3 text-sm text-slate-700">
          Lần chạy này không có tín hiệu nào để tổng hợp. Bát Tự hiện chỉ lập lá số (dữ liệu thật)
          mà chưa phát sinh tín hiệu, vì mọi tín hiệu Bát Tự đều cần phần luận giải còn đang chờ
          xác minh; Bát Trạch chỉ phát sinh tín hiệu khi bạn nhập hướng nhà/phòng để đối chiếu.
          Muốn có kết luận tổng hợp, hãy nhập hướng, hoặc thêm Thần số học hoặc Tarot.
        </p>
      )}

      <section id="bang-chung" className="scroll-mt-20 space-y-3">
        <h2 className="text-lg font-semibold text-slate-900">Vì sao có kết quả này?</h2>
        <details className="rounded-lg border border-slate-200 bg-white p-4">
          <summary className="cursor-pointer text-sm font-semibold text-slate-900">
            Toàn bộ bằng chứng ({result.evidence.length})
          </summary>
          <div className="mt-4 grid gap-3 xl:grid-cols-2">
            {result.evidence.map((ev) => (
              <div key={ev.evidenceId} className="rounded-md bg-slate-50 p-3 text-sm">
                <div className="mb-1 flex flex-wrap items-center gap-2 text-xs text-slate-500">
                  <span className="font-medium text-slate-700">{ev.engine}</span>
                  {ev.school && <span>· {ev.school}</span>}
                  {ev.dimension && <LabeledBadge value={ev.dimension} />}
                  <span>· quy tắc {ev.ruleId} (v{ev.ruleVersion})</span>
                </div>
                <pre className="max-h-64 overflow-auto text-xs text-slate-600">
                  {JSON.stringify(ev.fact, null, 2)}
                </pre>
                {ev.source && <p className="mt-1 text-xs text-slate-400">Nguồn: {ev.source}</p>}
              </div>
            ))}
          </div>
        </details>

        {result.signals.length > 0 && (
          <details className="rounded-lg border border-slate-200 bg-white p-4">
            <summary className="cursor-pointer text-sm font-semibold text-slate-900">
              Toàn bộ tín hiệu ({result.signals.length})
            </summary>
            <div className="mt-4 grid gap-2 sm:grid-cols-2 xl:grid-cols-3">
              {result.signals.map((sig) => (
                <div key={sig.signalId} className="rounded-md bg-slate-50 p-3 text-xs">
                  <div className="flex flex-wrap items-center gap-1.5">
                    <span className="font-medium text-slate-700">{sig.engine}</span>
                    <LabeledBadge value={sig.dimension} />
                    <LabeledBadge value={sig.polarity} />
                    <LabeledBadge value={sig.strength} />
                    {sig.critical && (
                      <span className="rounded-full bg-rose-600 px-2 py-0.5 font-medium text-white">
                        Quan trọng
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </details>
        )}
      </section>
    </div>
  );
}
