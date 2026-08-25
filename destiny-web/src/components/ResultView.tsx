import type { LabelRegistries, NarrativeResponseDto, ScenarioRunResponse } from "@/lib/types";
import { AstrologyChartCard } from "./AstrologyChartCard";
import { BaziChartCard } from "./BaziChartCard";
import { BatTrachCard } from "./BatTrachCard";
import { IChingChartCard } from "./IChingChartCard";
import { LabeledBadge } from "./LabeledBadge";
import { NumerologyResultCard } from "./NumerologyResultCard";
import { RetentionNotice } from "./RetentionNotice";
import { TarotResultCard } from "./TarotResultCard";

/**
 * Renders one calculation's full explainability record, in the order
 * UI_UX_VIETNAMESE_SPEC section 4 requires (Fusion conclusion first, hard
 * data before any AI text) and following section 3's named flow for what
 * appears below the fold.
 *
 * `labels` is optional so every existing caller keeps working; without it the
 * Bát Tự chart renders technical names rather than disappearing, which is the
 * right failure mode for a label table that could not be fetched.
 */
export function ResultView({
  result,
  labels = {},
  narrative = null,
}: {
  result: ScenarioRunResponse;
  labels?: LabelRegistries;
  narrative?: NarrativeResponseDto | null;
}) {
  return (
    <div className="space-y-8">
      {/*
        First, because it is about whether this result will still be here
        tomorrow - which outranks anything it says (CLAUDE.md section 7).
        Guarded for robustness against an older cached response shape.
      */}
      {result.retention && (
        <RetentionNotice
          calculationId={result.calculationId}
          retention={result.retention}
        />
      )}

      {!result.policyDefined && (
        <p className="rounded-md bg-slate-100 px-4 py-3 text-sm text-slate-700">
          Chủ đề này chưa có chính sách áp dụng hệ thống cụ thể — chưa hệ thống nào được chạy, để
          tránh suy đoán một chính sách chưa được xác định.
        </p>
      )}

      {result.fusion && (
        <section className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="mb-2 text-sm font-medium text-slate-500">🎯 Kết luận tổng hợp</h2>
          <div className="text-2xl">
            <LabeledBadge value={result.fusion.overallOutcome} />
          </div>
        </section>
      )}

      {/*
        Commentary on the hard data above, never a replacement for it
        (CLAUDE.md section 9) - a visually distinct panel (indigo, not the
        slate/white hard-data cards) so it never reads as another fact.
        `source`/`fallbackReason` are shown, not hidden: a deterministic
        fallback is a legitimate, honestly-labeled answer (ADR D8), not a
        degraded one to apologize for.
      */}
      {narrative && (
        <section className="rounded-xl border border-indigo-200 bg-indigo-50 p-6 shadow-sm">
          <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
            <h2 className="text-sm font-medium text-indigo-700">📝 Tổng kết bằng lời văn</h2>
            <span className="text-xs text-indigo-500" title={narrative.fallbackReason.technical}>
              Nguồn diễn giải: <LabeledBadge value={narrative.source} />
            </span>
          </div>
          <p className="text-slate-800">{narrative.summary}</p>

          {narrative.keySignals.length > 0 && (
            <div className="mt-4">
              <h3 className="text-xs font-semibold uppercase tracking-wide text-indigo-700">
                Tín hiệu chính
              </h3>
              <ul className="mt-1 list-inside list-disc space-y-1 text-sm text-slate-700">
                {narrative.keySignals.map((item, i) => (
                  <li key={i}>{item}</li>
                ))}
              </ul>
            </div>
          )}

          {narrative.conflicts.length > 0 && (
            <div className="mt-4">
              <h3 className="text-xs font-semibold uppercase tracking-wide text-indigo-700">
                Mâu thuẫn cần lưu ý
              </h3>
              <ul className="mt-1 list-inside list-disc space-y-1 text-sm text-slate-700">
                {narrative.conflicts.map((item, i) => (
                  <li key={i}>{item}</li>
                ))}
              </ul>
            </div>
          )}

          {narrative.cautions.length > 0 && (
            <div className="mt-4">
              <h3 className="text-xs font-semibold uppercase tracking-wide text-indigo-700">Cảnh báo</h3>
              <ul className="mt-1 list-inside list-disc space-y-1 text-sm text-slate-700">
                {narrative.cautions.map((item, i) => (
                  <li key={i}>{item}</li>
                ))}
              </ul>
            </div>
          )}

          {narrative.reflectionQuestions.length > 0 && (
            <div className="mt-4">
              <h3 className="text-xs font-semibold uppercase tracking-wide text-indigo-700">
                Câu hỏi để tự suy ngẫm
              </h3>
              <ul className="mt-1 list-inside list-disc space-y-1 text-sm text-slate-700">
                {narrative.reflectionQuestions.map((item, i) => (
                  <li key={i}>{item}</li>
                ))}
              </ul>
            </div>
          )}

          <p className="mt-4 text-xs text-indigo-500">
            Phần này là diễn giải, không phải dữ liệu tính toán — mọi số liệu/lá số/quẻ ở các mục
            dưới đây mới là dữ liệu thật, không đổi theo lời văn này.
          </p>
        </section>
      )}

      {/*
        Hard data, shown before the aggregate discussion and independently of
        any AI narrative (CLAUDE.md section 9). Renders nothing when the run
        had no Bát Tự engine, so it costs an absent section rather than an
        empty one.
      */}
      <BaziChartCard evidence={result.evidence} labels={labels} />

      {/* Same contract: renders nothing when Bát Trạch did not take part. */}
      <BatTrachCard evidence={result.evidence} labels={labels} />

      {/* Same contract: renders nothing when Western astrology did not take part. */}
      <AstrologyChartCard evidence={result.evidence} labels={labels} />

      {/* Same contract: renders nothing when I Ching did not take part. */}
      <IChingChartCard evidence={result.evidence} labels={labels} />

      {/* Same contract: renders nothing when Tarot did not take part. */}
      <TarotResultCard evidence={result.evidence} />

      {/* Same contract: renders nothing when Numerology did not take part. */}
      <NumerologyResultCard evidence={result.evidence} labels={labels} />

      <section>
        <h2 className="mb-3 text-lg font-semibold text-slate-900">Nguồn đóng góp</h2>
        <div className="grid gap-3 sm:grid-cols-2">
          {result.engines.map((engine) => (
            <div
              key={engine.engine}
              className="flex items-center justify-between rounded-lg border border-slate-200 px-4 py-3"
            >
              <span className="font-medium text-slate-800">{engine.engine}</span>
              <LabeledBadge value={engine.status} />
            </div>
          ))}
        </div>
        {result.unavailableEngines.length > 0 && (
          <p className="mt-2 text-sm text-slate-500">
            Hệ thống không áp dụng cho lần chạy này: {result.unavailableEngines.join(", ")}
          </p>
        )}
      </section>

      {result.fusion && result.fusion.dimensions.length > 0 && (
        <section>
          <h2 className="mb-3 text-lg font-semibold text-slate-900">Điểm đồng thuận &amp; mâu thuẫn</h2>
          <div className="space-y-3">
            {result.fusion.dimensions.map((dim) => (
              <div key={dim.dimension.technical} className="rounded-lg border border-slate-200 p-4">
                <div className="mb-2 flex items-center justify-between">
                  <span className="font-medium text-slate-800">{dim.dimension.labelVi}</span>
                  <LabeledBadge value={dim.state} />
                </div>
                <dl className="grid grid-cols-3 gap-2 text-xs text-slate-600">
                  <div>
                    <dt className="font-semibold">Ủng hộ</dt>
                    <dd>{dim.supportingEngines.join(", ") || "—"}</dd>
                  </div>
                  <div>
                    <dt className="font-semibold">Thận trọng</dt>
                    <dd>{dim.cautionEngines.join(", ") || "—"}</dd>
                  </div>
                  <div>
                    <dt className="font-semibold">Không thuận lợi</dt>
                    <dd>{dim.negativeEngines.join(", ") || "—"}</dd>
                  </div>
                </dl>
              </div>
            ))}
          </div>
        </section>
      )}

      {result.fusion && result.fusion.conflicts.length > 0 && (
        <section>
          <h2 className="mb-3 text-lg font-semibold text-slate-900">Điểm khác biệt giữa các phương pháp</h2>
          <div className="space-y-3">
            {result.fusion.conflicts.map((conflict, i) => (
              <div key={i} className="rounded-lg border border-rose-200 bg-rose-50 p-4">
                <div className="mb-1 flex items-center gap-2">
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

      <details className="rounded-lg border border-slate-200 p-4">
        <summary className="cursor-pointer text-sm font-semibold text-slate-900">
          Vì sao có kết quả này? Xem toàn bộ bằng chứng ({result.evidence.length})
        </summary>
        <div className="mt-4 space-y-3">
          {result.evidence.map((ev) => (
            <div key={ev.evidenceId} className="rounded-md bg-slate-50 p-3 text-sm">
              <div className="mb-1 flex flex-wrap items-center gap-2 text-xs text-slate-500">
                <span className="font-medium text-slate-700">{ev.engine}</span>
                {ev.school && <span>· {ev.school}</span>}
                {ev.dimension && <LabeledBadge value={ev.dimension} />}
                <span>· quy tắc {ev.ruleId} (v{ev.ruleVersion})</span>
              </div>
              <pre className="overflow-x-auto text-xs text-slate-600">
                {JSON.stringify(ev.fact, null, 2)}
              </pre>
              {ev.source && <p className="mt-1 text-xs text-slate-400">Nguồn: {ev.source}</p>}
            </div>
          ))}
        </div>
      </details>

      {result.signals.length === 0 && result.engines.length > 0 && (
        <p className="rounded-md bg-slate-100 px-4 py-3 text-sm text-slate-700">
          Lần chạy này không có tín hiệu nào để tổng hợp. Bát Tự hiện chỉ lập lá số (dữ liệu thật)
          mà chưa phát sinh tín hiệu, vì mọi tín hiệu Bát Tự đều cần phần luận giải còn đang chờ
          xác minh; Bát Trạch chỉ phát sinh tín hiệu khi bạn nhập hướng nhà/phòng để đối chiếu.
          Muốn có kết luận tổng hợp, hãy nhập hướng, hoặc thêm Thần số học hoặc Tarot.
        </p>
      )}

      {result.signals.length > 0 && (
        <details className="rounded-lg border border-slate-200 p-4">
          <summary className="cursor-pointer text-sm font-semibold text-slate-900">
            Toàn bộ tín hiệu ({result.signals.length})
          </summary>
          <div className="mt-4 grid gap-2 sm:grid-cols-2">
            {result.signals.map((sig) => (
              <div key={sig.signalId} className="rounded-md bg-slate-50 p-3 text-xs">
                <div className="mb-1 flex flex-wrap items-center gap-1.5">
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

      <section className="rounded-lg bg-slate-50 p-4 text-xs text-slate-500">
        <p>Mã lần tính: <span className="font-mono">{result.calculationId}</span></p>
        <p>Mã xác thực kết quả: <span className="font-mono">{result.resultHash}</span></p>
        {result.fusion && result.fusion.rulesApplied.length > 0 && (
          <p>Quy tắc tổng hợp đã áp dụng: {result.fusion.rulesApplied.join(", ")}</p>
        )}
        <p className="mt-2">
          Lưu mã lần tính này để tra cứu lại sau tại mục{" "}
          <span className="font-medium">Lịch sử</span>.
        </p>
      </section>
    </div>
  );
}
