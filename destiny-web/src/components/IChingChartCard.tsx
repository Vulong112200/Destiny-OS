import type { EvidenceDto, LabelRegistries } from "@/lib/types";
import { BlockedSectionList } from "./BlockedSectionList";
import { changedLineValues, HexagramSvg } from "./HexagramSvg";

/**
 * Renders an I Ching / Mai Hoa hexagram casting as hard data, reconstructed
 * from `IChingEngine`'s evidence — same approach and same reasons as
 * `AstrologyChartCard`: what's shown here cannot drift from the audit trail.
 *
 * Casting only, same as the other chart-half components — the blocked
 * section at the bottom (which line's text to read) is the reading, and it
 * is absent on purpose (R12's interpretive half).
 */

const METHOD_LABELS: Record<string, string> = {
  THREE_COINS: "Tam Tiền (rút xu)",
  YARROW: "Thi Thảo (cỏ thi)",
  MAI_HOA_NUMBER: "Mai Hoa — theo Số",
  MAI_HOA_TIME: "Mai Hoa — theo Năm Tháng Ngày Giờ",
};

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

  const movingPositions = Array.isArray(moving?.positions)
    ? (moving!.positions as unknown[]).filter((p): p is number => typeof p === "number")
    : [];
  const drawnLinesAsStrings = Array.isArray(drawnLines?.lines)
    ? (drawnLines!.lines as unknown[]).filter((l): l is string => typeof l === "string")
    : [];

  return (
    <section className="space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div>
        <h2 className="text-lg font-semibold text-slate-900">☰ Kinh Dịch — Quẻ gieo được</h2>
        <p className="mt-1 text-xs text-slate-500">
          Dữ liệu gieo quẻ tất định. Đây là <span className="font-medium">quẻ</span>, chưa phải
          lời đoán — phần luận giải xem ở cuối mục này.
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

      <BlockedSectionList items={blocked} />
    </section>
  );
}
