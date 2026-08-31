import { getOrGenerateNarrative } from "@/lib/api";
import { LabeledBadge } from "./LabeledBadge";

/**
 * The AI/fallback narrative, fetched independently of the rest of the page.
 *
 * <p>This is its own async component so it can be streamed in behind a
 * `<Suspense>` boundary instead of being awaited alongside the calculation.
 * The reason is a measured one: the OpenRouter provider walks a model
 * fallback chain, and its worst case is `MAX_ATTEMPTS × chain length ×
 * timeout-ms` — 100s with the shipped two-model chain at a 25s timeout. The
 * result page used to `Promise.all` this with the calculation itself, so a
 * slow or exhausted free model held back the hard data too.
 *
 * <p>That inversion was also backwards on principle. The narrative is
 * commentary on the hard data and never a replacement for it (CLAUDE.md §9);
 * it is the one part of this page that may legitimately arrive late, be
 * degraded, or be a deterministic fallback. Making everything else wait for
 * it gave the least authoritative section the most scheduling power.
 */
export async function NarrativePanel({ calculationId }: { calculationId: string }) {
  const narrative = await getOrGenerateNarrative(calculationId);

  if (!narrative) {
    return (
      <section
        id="dien-giai"
        className="scroll-mt-20 rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-600"
      >
        <h2 className="mb-1 text-sm font-medium text-slate-700">📝 Tổng kết bằng lời văn</h2>
        <p>
          Không tạo được phần tổng kết bằng lời cho lần tính này. Mọi dữ liệu tính toán bên dưới
          vẫn đầy đủ và không phụ thuộc vào phần này.
        </p>
      </section>
    );
  }

  return (
    <section
      id="dien-giai"
      className="scroll-mt-20 rounded-xl border border-indigo-200 bg-indigo-50 p-6 shadow-sm"
    >
      <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
        <h2 className="text-sm font-medium text-indigo-700">📝 Tổng kết bằng lời văn</h2>
        <span className="text-xs text-indigo-500" title={narrative.fallbackReason.technical}>
          Nguồn diễn giải: <LabeledBadge value={narrative.source} />
          {narrative.model && <span className="ml-1">· {narrative.model}</span>}
        </span>
      </div>
      <p className="max-w-prose text-slate-800">{narrative.summary}</p>

      <div className="mt-4 grid gap-4 md:grid-cols-2">
        <NarrativeList title="Tín hiệu chính" items={narrative.keySignals} />
        <NarrativeList title="Mâu thuẫn cần lưu ý" items={narrative.conflicts} />
        <NarrativeList title="Cảnh báo" items={narrative.cautions} />
        <NarrativeList title="Câu hỏi để tự suy ngẫm" items={narrative.reflectionQuestions} />
      </div>

      <p className="mt-4 text-xs text-indigo-500">
        Phần này là diễn giải, không phải dữ liệu tính toán — mọi số liệu/lá số/quẻ ở các mục
        dưới đây mới là dữ liệu thật, không đổi theo lời văn này.
      </p>
    </section>
  );
}

/** Placeholder shown while the narrative is still being generated. */
export function NarrativePanelSkeleton() {
  return (
    <section
      id="dien-giai"
      className="scroll-mt-20 rounded-xl border border-indigo-200 bg-indigo-50 p-6"
      aria-busy="true"
    >
      <h2 className="text-sm font-medium text-indigo-700">📝 Tổng kết bằng lời văn</h2>
      <p className="mt-2 text-sm text-indigo-600">
        Đang soạn phần tổng kết… Dữ liệu tính toán bên dưới đã sẵn sàng, bạn có thể đọc ngay
        mà không cần chờ mục này.
      </p>
      <div className="mt-4 space-y-2">
        <div className="h-3 w-full animate-pulse rounded bg-indigo-100" />
        <div className="h-3 w-11/12 animate-pulse rounded bg-indigo-100" />
        <div className="h-3 w-4/6 animate-pulse rounded bg-indigo-100" />
      </div>
    </section>
  );
}

function NarrativeList({ title, items }: { title: string; items: string[] }) {
  if (items.length === 0) return null;
  return (
    <div>
      <h3 className="text-xs font-semibold uppercase tracking-wide text-indigo-700">{title}</h3>
      <ul className="mt-1 list-inside list-disc space-y-1 text-sm text-slate-700">
        {items.map((item, i) => (
          <li key={i}>{item}</li>
        ))}
      </ul>
    </div>
  );
}
