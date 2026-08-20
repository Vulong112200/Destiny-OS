package io.destinyos.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * The deterministic, non-LLM report ADR D8 requires: the system stays fully
 * usable with the LLM disabled. Every field is assembled directly from
 * {@link NarrativeInput} by template, never invented - this class adds no
 * metaphysical claim that the hard data does not already state, which is
 * exactly why it is safe to render with zero schema-validation risk (unlike
 * a real provider's free-text output).
 */
final class HardDataNarrativeFallback {

    private HardDataNarrativeFallback() {
    }

    static NarrativeResponse build(NarrativeInput prunedInput, FallbackReason reason) {
        String summary = reason == FallbackReason.AI_DISABLED
                ? "Phần diễn giải AI đang tắt. Dưới đây là tóm tắt dựa trên dữ liệu tính toán gốc cho \""
                        + prunedInput.scenarioNameVi() + "\"."
                : "Không thể tạo phần diễn giải AI lúc này. Dữ liệu tính toán gốc vẫn được giữ nguyên "
                        + "cho \"" + prunedInput.scenarioNameVi() + "\".";

        List<String> keySignals = prunedInput.signals().stream()
                .map(s -> s.engine() + ": " + s.dimensionLabelVi() + " - " + s.polarityLabelVi()
                        + " (" + s.strengthLabelVi() + (s.critical() ? ", đáng chú ý" : "") + ")")
                .toList();

        List<String> conflicts = prunedInput.conflicts().stream()
                .map(c -> c.typeLabelVi() + (c.dimension() != null ? " ở " + c.dimension() : "")
                        + ": " + c.description())
                .toList();

        List<String> cautions = new ArrayList<>(prunedInput.warnings());
        cautions.addAll(prunedInput.limitations());

        List<String> reflectionQuestions = buildReflectionQuestions(prunedInput);

        return new NarrativeResponse(summary, keySignals, conflicts, cautions, reflectionQuestions);
    }

    private static List<String> buildReflectionQuestions(NarrativeInput input) {
        List<String> questions = new ArrayList<>();
        for (NarrativeConflictItem conflict : input.conflicts()) {
            if (conflict.involvedEngines().size() >= 2) {
                questions.add("Giữa " + String.join(" và ", conflict.involvedEngines())
                        + ", bạn thấy tín hiệu nào gần với tình huống thực tế của mình hơn?");
            }
        }
        questions.add("Những điểm cần lưu ý ở trên có liên quan trực tiếp đến quyết định "
                + "bạn đang cân nhắc không?");
        return questions;
    }
}
