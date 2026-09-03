import type { LabelRegistries, ScenarioRunResponse, SupportedScenarioType } from "@/lib/types";
import { DIMENSION_LABELS, SCENARIO_META, isRelevantDimension } from "@/lib/scenarioMeta";
import type { DimensionName } from "@/lib/scenarioMeta";
import { buildReading, groupByDimension, sortByWeight } from "@/lib/reading";
import type { ReadingItem } from "@/lib/reading";
import { EngineName, EngineNameList } from "./EngineName";
import { LabeledBadge } from "./LabeledBadge";

/**
 * The answer to what the user actually asked, assembled from the run.
 *
 * <p>This section is the fix for a specific complaint: every dimension the
 * system produced used to be rendered identically, so a "Sự nghiệp" question
 * came back with its career analysis, its relationship analysis and its home
 * analysis in the same typeface, in registry order, none of them tied back to
 * the question. `ScenarioRegistry` has always known which dimensions a
 * scenario is about (`ScenarioDefinition.dimensions()`); that set was simply
 * never used outside the AI pruner. Here it splits the page: the dimensions
 * the user asked about are answered in full, and the rest are kept — they are
 * real results and hiding them would be dishonest — but folded away under a
 * heading that says plainly they are outside what was asked.
 *
 * <p>Nothing here re-ranks or re-scores anything. Fusion's verdict per
 * dimension is displayed exactly as Fusion produced it; the only editorial
 * act is which of them is shown open.
 */
export function ScenarioAnswer({
  result,
  scenario,
  labels,
}: {
  result: ScenarioRunResponse;
  scenario: SupportedScenarioType | null;
  /**
   * Vietnamese labels from `GET /api/v1/labels`. This component rendered five
   * separate raw engine-id lists without them - the per-signal "Nguồn:" line
   * and the Ủng hộ / Thận trọng / Không thuận footer of every dimension card.
   */
  labels?: LabelRegistries;
}) {
  const meta = scenario ? SCENARIO_META[scenario] : null;
  const reading = buildReading(result, scenario);
  const byDimension = groupByDimension(reading);

  /*
   * The API is the authority on which dimensions a scenario is about, once it
   * carries them: `scenarioMeta.ts` is a hand-kept mirror of the Java
   * registry and can drift, whereas `relevantDimensions` comes straight from
   * `ScenarioDefinition.dimensions()`. Falling back to the mirror keeps
   * results computed before the backend carried the field rendering
   * correctly, rather than showing every dimension as off-topic.
   */
  const apiRelevant =
    result.dimensions !== undefined && result.dimensions !== null && result.dimensions.length > 0
      ? result.dimensions
      : null;
  const isRelevant = (technical: string) =>
    apiRelevant !== null
      ? apiRelevant.some((d) => d.technical === technical)
      : isRelevantDimension(scenario, technical);

  const fusionDimensions = result.fusion?.dimensions ?? [];
  const relevant = fusionDimensions.filter((d) => isRelevant(d.dimension.technical));
  const offTopic = fusionDimensions.filter((d) => !isRelevant(d.dimension.technical));

  // A dimension the scenario asks about that produced no verdict at all is
  // itself an answer, and a more useful one than silence: it means no engine
  // in this run had anything to say there.
  const answeredTechnicals = new Set(relevant.map((d) => d.dimension.technical));
  const expectedDimensions: DimensionName[] =
    apiRelevant !== null
      ? apiRelevant.map((d) => d.technical as DimensionName)
      : (meta?.relevantDimensions ?? []);
  const unanswered = expectedDimensions.filter((d) => !answeredTechnicals.has(d));

  return (
    <div className="space-y-6">
      <section id="tra-loi" className="scroll-mt-20">
        <div className="mb-3 flex items-baseline justify-between gap-4">
          <h2 className="text-lg font-semibold text-slate-900">
            Trả lời cho điều bạn hỏi
          </h2>
          {relevant.length > 0 && (
            <span className="text-xs text-slate-500">
              {relevant.length} chiều liên quan trực tiếp
            </span>
          )}
        </div>

        {relevant.length === 0 && (
          <NoAnswerNotice result={result} unanswered={unanswered} />
        )}

        {relevant.length > 0 && (
          <div className="grid gap-4 xl:grid-cols-2">
            {relevant.map((dim) => (
              <DimensionAnswerCard
                key={dim.dimension.technical}
                label={dim.dimension.labelVi}
                state={dim.state}
                supportingEngines={dim.supportingEngines}
                cautionEngines={dim.cautionEngines}
                negativeEngines={dim.negativeEngines}
                items={sortByWeight(byDimension.get(dim.dimension.technical) ?? [])}
                labels={labels}
                emphasis
              />
            ))}
          </div>
        )}

        {relevant.length > 0 && unanswered.length > 0 && (
          <p className="mt-3 rounded-md bg-slate-100 px-4 py-3 text-sm text-slate-700">
            Chủ đề bạn chọn còn có chiều{" "}
            <span className="font-medium">
              {unanswered.map((d) => DIMENSION_LABELS[d]).join(", ")}
            </span>
            , nhưng lần chạy này không hệ nào phát tín hiệu ở đó — nên hệ thống không đưa ra
            nhận định, thay vì suy đoán cho đủ mục.
          </p>
        )}
      </section>

      {offTopic.length > 0 && (
        <details id="ngoai-trong-tam" className="scroll-mt-20 rounded-xl border border-slate-200 bg-white">
          <summary className="cursor-pointer px-6 py-4 text-sm font-semibold text-slate-900">
            Những chiều khác hệ thống ghi nhận ({offTopic.length}) — ngoài trọng tâm câu hỏi
          </summary>
          <div className="border-t border-slate-100 px-6 py-4">
            <p className="mb-4 text-xs text-slate-500">
              Đây là kết quả thật, không phải phần thừa: các hệ đã chạy vẫn phát tín hiệu ở những
              chiều này. Chúng được thu gọn vì nằm ngoài chủ đề bạn chọn, không phải vì kém tin cậy
              hơn.
            </p>
            <div className="grid gap-4 xl:grid-cols-2">
              {offTopic.map((dim) => (
                <DimensionAnswerCard
                  key={dim.dimension.technical}
                  label={dim.dimension.labelVi}
                  state={dim.state}
                  supportingEngines={dim.supportingEngines}
                  cautionEngines={dim.cautionEngines}
                  negativeEngines={dim.negativeEngines}
                  items={sortByWeight(byDimension.get(dim.dimension.technical) ?? [])}
                  labels={labels}
                />
              ))}
            </div>
          </div>
        </details>
      )}
    </div>
  );
}

function DimensionAnswerCard({
  label,
  state,
  supportingEngines,
  cautionEngines,
  negativeEngines,
  items,
  labels,
  emphasis = false,
}: {
  label: string;
  state: { technical: string; labelVi: string };
  supportingEngines: string[];
  cautionEngines: string[];
  negativeEngines: string[];
  items: ReadingItem[];
  labels?: LabelRegistries;
  emphasis?: boolean;
}) {
  const withText = items.filter((i) => i.text !== null);
  const withoutText = items.filter((i) => i.text === null);

  return (
    <article
      className={`flex flex-col rounded-xl border bg-white p-5 ${
        emphasis ? "border-slate-300 shadow-sm" : "border-slate-200"
      }`}
    >
      <header className="mb-3 flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 pb-3">
        <h3 className="font-semibold text-slate-900">{label}</h3>
        <LabeledBadge value={state} />
      </header>

      {withText.length > 0 && (
        <ul className="space-y-4">
          {withText.map((item) => (
            <li key={item.signalId}>
              <div className="mb-1 flex flex-wrap items-center gap-1.5">
                <span className="text-sm font-medium text-slate-900">{item.title}</span>
                <LabeledBadge value={item.polarity} />
                <LabeledBadge value={item.strength} />
                {item.critical && (
                  <span className="rounded-full bg-rose-600 px-2 py-0.5 text-xs font-medium text-white">
                    Đáng chú ý
                  </span>
                )}
              </div>
              <p className="text-sm leading-relaxed text-slate-700">{item.text}</p>
              {item.keywords.length > 0 && (
                <p className="mt-1 text-xs text-slate-500">
                  Từ khóa: {item.keywords.join(" · ")}
                </p>
              )}
              <p className="mt-1 text-[11px] text-slate-400">
                Nguồn: <EngineName id={item.engine} labels={labels} />
              </p>
            </li>
          ))}
        </ul>
      )}

      {withText.length === 0 && (
        <p className="text-sm text-slate-600">
          Kết luận ở chiều này đến từ tín hiệu tính toán, chưa có đoạn diễn giải được biên soạn
          kèm theo.
        </p>
      )}

      {withoutText.length > 0 && (
        <p className="mt-3 border-t border-slate-100 pt-2 text-xs text-slate-500">
          Còn {withoutText.length} tín hiệu nữa ở chiều này chưa có phần luận giải bằng lời (
          <EngineNameList ids={[...new Set(withoutText.map((i) => i.engine))]} labels={labels} />)
          — đã tính vào kết luận,
          nhưng chưa có nội dung được biên soạn để hiển thị.
        </p>
      )}

      <footer className="mt-4 grid grid-cols-3 gap-2 border-t border-slate-100 pt-3 text-xs text-slate-600">
        <div>
          <dt className="font-semibold text-slate-700">Ủng hộ</dt>
          <dd>
            <EngineNameList ids={supportingEngines} labels={labels} />
          </dd>
        </div>
        <div>
          <dt className="font-semibold text-slate-700">Thận trọng</dt>
          <dd>
            <EngineNameList ids={cautionEngines} labels={labels} />
          </dd>
        </div>
        <div>
          <dt className="font-semibold text-slate-700">Không thuận</dt>
          <dd>
            <EngineNameList ids={negativeEngines} labels={labels} />
          </dd>
        </div>
      </footer>
    </article>
  );
}

/**
 * Explains an empty answer in terms of *this* run rather than a generic
 * "không có dữ liệu". Which sentence applies is decidable from the response,
 * so the page says the true one instead of the vague one.
 */
function NoAnswerNotice({
  result,
  unanswered,
}: {
  result: ScenarioRunResponse;
  unanswered: DimensionName[];
}) {
  const ranEngines = result.engines.length;

  return (
    <div className="rounded-xl border border-amber-200 bg-amber-50 p-5 text-sm text-amber-900">
      <p className="font-medium">Lần chạy này chưa trả lời được đúng chủ đề bạn chọn.</p>
      {unanswered.length > 0 && (
        <p className="mt-1">
          Chưa có nhận định ở chiều{" "}
          <span className="font-medium">
            {unanswered.map((d) => DIMENSION_LABELS[d]).join(", ")}
          </span>
          .
        </p>
      )}
      <p className="mt-2">
        {ranEngines === 0
          ? "Không hệ nào chạy được với dữ liệu bạn đã nhập."
          : result.signals.length === 0
            ? "Các hệ đã chạy và cho ra dữ liệu tính toán, nhưng chưa hệ nào phát tín hiệu để tổng hợp. Bát Tự, Chiêm tinh và Kinh Dịch hiện chỉ lập lá số/quẻ — phần luận giải của chúng còn đang chờ xác minh nguồn, nên không được phép phát tín hiệu."
            : "Các tín hiệu phát sinh nằm ở những chiều khác với chủ đề bạn chọn — xem mục bên dưới."}
      </p>
      <p className="mt-2 text-amber-800">
        Muốn có nhận định đúng chủ đề: bật thêm <span className="font-medium">Tarot</span> (có
        diễn giải riêng cho từng chiều), nhập <span className="font-medium">họ tên + ngày sinh</span>{" "}
        để có Thần số học, hoặc nhập <span className="font-medium">hướng nhà/phòng</span> cho Bát
        Trạch.
      </p>
    </div>
  );
}
