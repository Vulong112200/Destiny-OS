import type { LabeledValue } from "@/lib/types";

const TONE_STYLES: Record<string, string> = {
  SUPPORT: "bg-emerald-100 text-emerald-800",
  POSITIVE: "bg-emerald-100 text-emerald-800",
  CONSENSUS_SUPPORT: "bg-emerald-100 text-emerald-800",
  SUCCESS: "bg-emerald-100 text-emerald-800",
  PRODUCTION_READY: "bg-emerald-100 text-emerald-800",

  CAUTION: "bg-amber-100 text-amber-800",
  CONSENSUS_CAUTION: "bg-amber-100 text-amber-800",
  SUPPORT_WITH_CAUTION: "bg-amber-100 text-amber-800",
  CAUTION_WITH_SUPPORT: "bg-amber-100 text-amber-800",
  MIXED: "bg-amber-100 text-amber-800",
  PARTIAL: "bg-amber-100 text-amber-800",
  CONTENT_REQUIRED: "bg-amber-100 text-amber-800",
  DECISION_REQUIRED: "bg-amber-100 text-amber-800",

  NEGATIVE: "bg-rose-100 text-rose-800",
  CONSENSUS_NEGATIVE: "bg-rose-100 text-rose-800",
  CONFLICT: "bg-rose-100 text-rose-800",
  MAJOR_CONFLICT: "bg-rose-100 text-rose-800",
  METHODOLOGY_CONFLICT: "bg-rose-100 text-rose-800",
  DIRECT_CONFLICT: "bg-rose-100 text-rose-800",
  SCOPE_CONFLICT: "bg-rose-100 text-rose-800",
  TEMPORAL_CONFLICT: "bg-rose-100 text-rose-800",
  INPUT_SENSITIVITY_CONFLICT: "bg-rose-100 text-rose-800",
  FAILED_FATAL: "bg-rose-100 text-rose-800",
  FAILED_RECOVERABLE: "bg-rose-100 text-rose-800",
  RESEARCH_REQUIRED: "bg-rose-100 text-rose-800",

  NEUTRAL: "bg-slate-100 text-slate-700",
  NOT_APPLICABLE: "bg-slate-100 text-slate-700",
  INSUFFICIENT_EVIDENCE: "bg-slate-100 text-slate-700",
  NOT_IMPLEMENTED: "bg-slate-100 text-slate-700",
  OUT_OF_SCOPE: "bg-slate-100 text-slate-700",
};

/**
 * Renders a {technical, labelVi} pair the one way it should ever be shown:
 * Vietnamese label visible, technical name only in the title tooltip
 * (UI_UX_VIETNAMESE_SPEC section 1 - a bare technical enum must never
 * appear alone; section 7's "technical detail" affordance is the tooltip).
 */
export function LabeledBadge({ value }: { value: LabeledValue }) {
  const toneClass = TONE_STYLES[value.technical] ?? "bg-slate-100 text-slate-700";
  return (
    <span
      title={value.technical}
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-sm font-medium ${toneClass}`}
    >
      {value.labelVi}
    </span>
  );
}
