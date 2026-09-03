import type { LabelRegistries, ScenarioRunResponse } from "@/lib/types";
import { fusionRuleNames } from "@/lib/labels";
import { EngineName, EngineNameList } from "./EngineName";
import { LabeledBadge } from "./LabeledBadge";
import { RetentionNotice } from "./RetentionNotice";

/**
 * Everything about a result that is *not* the reading: how long it will be
 * kept, which engines ran, and the identifiers needed to look it up again.
 *
 * <p>This exists to take those three blocks out of the vertical flow. They
 * used to sit between the reading and the evidence trail, so a user comparing
 * a chart against its evidence scrolled past the retention notice and the
 * engine list every time. None of it is less important — retention still
 * comes first inside this column, per CLAUDE.md section 7 — it is simply
 * reference material, and reference material belongs beside the content it
 * refers to rather than interrupting it.
 */
export function ResultSidebar({
  result,
  sections,
  labels,
}: {
  result: ScenarioRunResponse;
  /** Anchor targets rendered as a jump list; `id` must match a section's DOM id. */
  sections: { id: string; label: string }[];
  /**
   * Vietnamese labels from `GET /api/v1/labels`.
   *
   * <p>This component did not take them at all, which is why it was the single
   * worst §9 offender on the page: it rendered every engine id raw, so the
   * "Hệ thống đã chạy" list read `TAROT`, `NUMEROLOGY_PYTHAGOREAN`,
   * `FENGSHUI_KUA`. The page already fetched the labels and handed them to
   * `ResultView`; they simply never reached the sidebar beside it.
   */
  labels?: LabelRegistries;
}) {
  const ranCount = result.engines.length;

  return (
    <aside className="space-y-4 lg:sticky lg:top-20 lg:self-start">
      {result.retention && (
        <RetentionNotice
          calculationId={result.calculationId}
          retention={result.retention}
        />
      )}

      {sections.length > 0 && (
        <nav className="rounded-xl border border-slate-200 bg-white p-4">
          <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
            Trong trang này
          </h2>
          <ul className="space-y-0.5 text-sm">
            {sections.map((s) => (
              <li key={s.id}>
                <a
                  href={`#${s.id}`}
                  className="block rounded-md px-2 py-1.5 text-slate-700 hover:bg-slate-100 hover:text-slate-900"
                >
                  {s.label}
                </a>
              </li>
            ))}
          </ul>
        </nav>
      )}

      <section className="rounded-xl border border-slate-200 bg-white p-4">
        <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
          Hệ thống đã chạy ({ranCount})
        </h2>
        <ul className="space-y-1.5">
          {result.engines.map((engine) => (
            <li key={engine.engine} className="flex items-center justify-between gap-2 text-sm">
              <EngineName id={engine.engine} labels={labels} className="text-slate-800" />
              <LabeledBadge value={engine.status} />
            </li>
          ))}
        </ul>
        {result.unavailableEngines.length > 0 && (
          <p className="mt-3 border-t border-slate-100 pt-2 text-xs text-slate-500">
            Không áp dụng cho lần chạy này:{" "}
            <EngineNameList
              ids={result.unavailableEngines}
              labels={labels}
              className="text-slate-600"
            />
          </p>
        )}
      </section>

      <section className="rounded-xl border border-slate-200 bg-slate-50 p-4 text-xs text-slate-500">
        <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
          Truy vết
        </h2>
        <dl className="space-y-2">
          <div>
            <dt className="text-slate-500">Mã lần tính</dt>
            <dd className="mt-0.5 font-mono text-[11px] break-all text-slate-700">
              {result.calculationId}
            </dd>
          </div>
          <div>
            <dt className="text-slate-500">Mã xác thực kết quả</dt>
            <dd className="mt-0.5 font-mono text-[11px] break-all text-slate-700">
              {result.resultHash}
            </dd>
          </div>
          {result.fusion && result.fusion.rulesApplied.length > 0 && (
            <div>
              <dt className="text-slate-500">Quy tắc tổng hợp đã áp dụng</dt>
              <dd className="mt-0.5 text-slate-700">
                {fusionRuleNames(result.fusion.rulesApplied)}
              </dd>
            </div>
          )}
        </dl>
        <p className="mt-3 border-t border-slate-200 pt-2">
          Lưu mã lần tính để tra cứu lại ở mục <span className="font-medium">Lịch sử</span>.
        </p>
      </section>
    </aside>
  );
}
