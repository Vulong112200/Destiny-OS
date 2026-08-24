import type { EvidenceDto, LabelRegistries } from "@/lib/types";

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

  return (
    <section className="space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div>
        <h2 className="text-lg font-semibold text-slate-900">Kinh Dịch — Quẻ gieo được</h2>
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

      {drawnLines && Array.isArray(drawnLines.lines) && (
        <div>
          <h3 className="text-sm font-semibold text-slate-900">6 hào (dưới lên trên)</h3>
          <ol className="mt-1 flex flex-col-reverse gap-1 text-sm text-slate-700">
            {(drawnLines.lines as unknown[]).map((line, i) => (
              <li key={i} className="flex gap-2">
                <span className="w-6 text-xs text-slate-400">#{i + 1}</span>
                <span>{label(labels, "LineValue", line)}</span>
              </li>
            ))}
          </ol>
        </div>
      )}

      {blocked.length > 0 && (
        <div>
          <h3 className="text-sm font-semibold text-slate-900">
            Phần luận giải chưa được cung cấp ({blocked.length})
          </h3>
          <p className="mb-2 text-xs text-slate-500">
            Những phần dưới đây bị bỏ trống có chủ đích, không phải do lỗi hay thiếu dữ liệu của
            bạn.
          </p>
          <ul className="space-y-2">
            {blocked.map((item) => (
              <li key={item.evidenceId} className="rounded-md border border-amber-200 bg-amber-50 p-3">
                <div className="flex flex-wrap items-baseline gap-2">
                  <span className="text-sm font-medium text-amber-900">
                    {String(item.fact.displayNameVi ?? "")}
                  </span>
                  <span
                    title={`Mục nghiên cứu ${String(item.fact.researchId ?? "")}`}
                    className="rounded-full bg-amber-200 px-2 py-0.5 text-xs font-medium text-amber-900"
                  >
                    Cần xác minh thuật toán · {String(item.fact.researchId ?? "")}
                  </span>
                </div>
                <p className="mt-1 text-xs text-amber-900">{String(item.fact.reasonVi ?? "")}</p>
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  );
}
