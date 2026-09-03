"use client";

import { LazyMotion, domAnimation, m, useReducedMotion } from "framer-motion";
import type { EvidenceDto, LabelRegistries } from "@/lib/types";
import { BlockedSectionList } from "./BlockedSectionList";
import { changedLineValues, HexagramSvg } from "./HexagramSvg";

/**
 * Renders an I Ching / Mai Hoa hexagram casting as hard data, reconstructed
 * from `IChingEngine`'s evidence — same approach and same reasons as
 * `AstrologyChartCard`: what's shown here cannot drift from the audit trail.
 *
 * <p>Quẻ từ (卦辭) và hào từ (爻辭) — R24/R25, đóng 2026-08-31 — nay là nội
 * dung thật (`ICHING_JUDGMENT_ORIGINAL`/`_CHANGED`, `ICHING_LINE_JUDGMENT_*`),
 * nên được hiển thị trực tiếp ở đây thay vì chỉ nằm trong evidence thô. Phần
 * còn bị chặn duy nhất (`BlockedSectionList` ở cuối) là *chọn lời hào nào làm
 * câu trả lời chính* khi nhiều hào cùng động — không phải bản thân lời hào.
 */

const METHOD_LABELS: Record<string, string> = {
  THREE_COINS: "Tam Tiền (rút xu)",
  YARROW: "Thi Thảo (cỏ thi)",
  MAI_HOA_NUMBER: "Mai Hoa — theo Số",
  MAI_HOA_TIME: "Mai Hoa — theo Năm Tháng Ngày Giờ",
};

interface JudgmentFact {
  hanTu?: unknown;
  hanViet?: unknown;
  nghia?: unknown;
  sourcePage?: unknown;
  note?: unknown;
  label?: unknown;
}

/** Một khối lời quẻ/lời hào: Hán văn, phiên âm Hán Việt, và nghĩa tiếng Việt. */
function JudgmentBlock({
  heading,
  fact,
  index,
}: {
  heading: string;
  fact: JudgmentFact;
  index: number;
}) {
  const reduce = useReducedMotion();
  return (
    <m.div
      initial={reduce ? false : { opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={reduce ? { duration: 0 } : { duration: 0.35, delay: index * 0.08 }}
      className="rounded-md border border-slate-200 bg-slate-50 p-3"
    >
      <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">{heading}</p>
      {fact.hanTu != null && (
        <p className="mt-1 font-serif text-base text-slate-800">{String(fact.hanTu)}</p>
      )}
      {fact.hanViet != null && (
        <p className="text-sm italic text-slate-600">{String(fact.hanViet)}</p>
      )}
      {fact.nghia != null && <p className="mt-1 text-sm text-slate-800">{String(fact.nghia)}</p>}
      {(fact.sourcePage != null || fact.note != null) && (
        <p className="mt-1 text-[11px] text-slate-400">
          {fact.sourcePage != null && <>Ngô Tất Tố, tr.{String(fact.sourcePage)}</>}
          {fact.sourcePage != null && fact.note != null && " · "}
          {fact.note != null && String(fact.note)}
        </p>
      )}
    </m.div>
  );
}

function label(labels: LabelRegistries, type: string, key: unknown): string {
  if (typeof key !== "string") return "—";
  return labels[type]?.[key] ?? key;
}

function factOf(evidence: EvidenceDto[], ruleId: string): Record<string, unknown> | null {
  return evidence.find((e) => e.ruleId === ruleId)?.fact ?? null;
}

function hexagramText(labels: LabelRegistries, fact: Record<string, unknown> | null): string {
  if (!fact) return "—";
  const upper = label(labels, "IChingTrigram", fact.upperTrigram);
  const lower = label(labels, "IChingTrigram", fact.lowerTrigram);
  return `Quẻ ${String(fact.number ?? "?")} — ${upper} trên, ${lower} dưới`;
}

export function IChingChartCard({
  evidence,
  labels,
}: {
  evidence: EvidenceDto[];
  labels: LabelRegistries;
}) {
  const ichingEvidence = evidence.filter((e) => e.engine === "ICHING");
  if (ichingEvidence.length === 0) {
    return null;
  }

  const cast = factOf(ichingEvidence, "ICHING_CAST");
  const original = factOf(ichingEvidence, "ICHING_ORIGINAL_HEXAGRAM");
  const changed = factOf(ichingEvidence, "ICHING_CHANGED_HEXAGRAM");
  const moving = factOf(ichingEvidence, "ICHING_MOVING_LINES");
  const drawnLines = factOf(ichingEvidence, "ICHING_DRAWN_LINES");
  const blocked = ichingEvidence.filter((e) => e.ruleId.startsWith("ICHING_BLOCKED_"));
  const judgmentOriginal = factOf(ichingEvidence, "ICHING_JUDGMENT_ORIGINAL");
  const judgmentChanged = factOf(ichingEvidence, "ICHING_JUDGMENT_CHANGED");
  const lineJudgments = ichingEvidence
    .filter((e) => e.ruleId.startsWith("ICHING_LINE_JUDGMENT_"))
    .map((e) => e.fact)
    .sort((a, b) => Number(a.position ?? 0) - Number(b.position ?? 0));
  const haoLamChu = factOf(ichingEvidence, "ICHING_HAO_LAM_CHU");

  const movingPositions = Array.isArray(moving?.positions)
    ? (moving!.positions as unknown[]).filter((p): p is number => typeof p === "number")
    : [];
  const drawnLinesAsStrings = Array.isArray(drawnLines?.lines)
    ? (drawnLines!.lines as unknown[]).filter((l): l is string => typeof l === "string")
    : [];

  return (
    <LazyMotion features={domAnimation} strict>
      <section className="space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div>
        <h2 className="text-lg font-semibold text-slate-900">☰ Kinh Dịch — Quẻ gieo được</h2>
        <p className="mt-1 text-xs text-slate-500">
          Dữ liệu gieo quẻ tất định, kèm lời quẻ (卦辭) và lời hào (爻辭) nguyên văn — theo bản
          dịch Ngô Tất Tố, đối chiếu Hán văn với zh.wikisource.org.
        </p>
      </div>

      {cast && (
        <p className="rounded-md bg-slate-50 px-3 py-2 text-xs text-slate-600">
          Phương pháp: <span className="font-medium">{METHOD_LABELS[String(cast.method)] ?? String(cast.method)}</span>
          {cast.seed != null && (
            <>
              {" "}
              · Hạt giống ngẫu nhiên:{" "}
              <span className="font-mono">{String(cast.seed)}</span>
            </>
          )}
        </p>
      )}

      {drawnLines && Array.isArray(drawnLines.lines) && (
        <div className="flex flex-wrap items-start gap-8">
          <div className="flex flex-col items-center gap-2">
            <HexagramSvg lines={drawnLinesAsStrings} />
            <span className="text-xs font-medium text-slate-600">Quẻ gốc (bản quái)</span>
          </div>
          {changed && (
            <div className="flex flex-col items-center gap-2">
              <HexagramSvg lines={changedLineValues(drawnLinesAsStrings)} />
              <span className="text-xs font-medium text-slate-600">Quẻ biến (chi quái)</span>
            </div>
          )}
          <p className="max-w-xs text-xs text-slate-500">
            Nét liền = Dương, nét đứt = Âm. Hào màu nâu có chấm giữa là hào động — hào đó đổi
            cực (Dương↔Âm) để tạo thành quẻ biến bên cạnh.
          </p>
        </div>
      )}

      <dl className="grid grid-cols-1 gap-x-6 gap-y-2 text-sm sm:grid-cols-2">
        <div className="flex flex-col gap-1">
          <dt className="text-xs uppercase tracking-wide text-slate-500">Quẻ gốc (bản quái)</dt>
          <dd className="font-medium text-slate-900">{hexagramText(labels, original)}</dd>
        </div>
        {changed && (
          <div className="flex flex-col gap-1">
            <dt className="text-xs uppercase tracking-wide text-slate-500">Quẻ biến (chi quái)</dt>
            <dd className="font-medium text-slate-900">{hexagramText(labels, changed)}</dd>
          </div>
        )}
      </dl>

      <p className="text-sm text-slate-700">
        Hào động:{" "}
        {movingPositions.length > 0 ? (
          <span className="font-medium">{movingPositions.join(", ")}</span>
        ) : (
          <span className="text-slate-500">không có hào nào động</span>
        )}
      </p>

      {(judgmentOriginal || judgmentChanged) && (
        <div className="space-y-2 border-t border-slate-100 pt-4">
          <h3 className="text-sm font-semibold text-slate-900">Lời quẻ (卦辭)</h3>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {judgmentOriginal && (
              <JudgmentBlock heading="Quẻ gốc" fact={judgmentOriginal} index={0} />
            )}
            {judgmentChanged && (
              <JudgmentBlock heading="Quẻ biến" fact={judgmentChanged} index={1} />
            )}
          </div>
        </div>
      )}

      {lineJudgments.length > 0 && (
        <div className="space-y-2 border-t border-slate-100 pt-4">
          <h3 className="text-sm font-semibold text-slate-900">Lời hào động (爻辭)</h3>
          {lineJudgments.length > 1 && (
            <p className="text-xs text-amber-700">
              Nhiều hào cùng động — engine chưa xác định lời hào nào là câu trả lời chính (xem mục
              &quot;chưa được cung cấp&quot; bên dưới), nên cả {lineJudgments.length} lời đều được
              hiển thị ngang nhau.
            </p>
          )}
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {lineJudgments.map((fact, i) => (
              <JudgmentBlock
                key={i}
                heading={typeof fact.label === "string" ? fact.label : `Hào ${String(fact.position)}`}
                fact={fact}
                index={i}
              />
            ))}
          </div>
        </div>
      )}

      {haoLamChu && (
        <div className="space-y-1 border-t border-slate-100 pt-4">
          <h3 className="text-sm font-semibold text-slate-900">Hào làm chủ</h3>
          <p className="text-sm text-slate-700">
            Hào {String(haoLamChu.position)} ({haoLamChu.isYang ? "Dương" : "Âm"}) — theo quy tắc{" "}
            <span className="italic">{String(haoLamChu.rule)}</span>
          </p>
          <p className="text-xs text-amber-700">{String(haoLamChu.neutralityNoteVi)}</p>
          {haoLamChu.sourceNamedExceptionVi != null && (
            <p className="text-xs text-amber-700">{String(haoLamChu.sourceNamedExceptionVi)}</p>
          )}
        </div>
      )}

      <BlockedSectionList items={blocked} />
      </section>
    </LazyMotion>
  );
}
