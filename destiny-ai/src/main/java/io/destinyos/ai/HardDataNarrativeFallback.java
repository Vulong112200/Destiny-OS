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
        String summary = buildSummary(prunedInput, reason);

        List<String> keySignals = prunedInput.signals().stream()
                .map(HardDataNarrativeFallback::renderSignal)
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

    /**
     * Opens with why the AI text is absent, then restates what the user asked.
     *
     * <p>Restating the question is not decoration. This is the path that
     * actually runs in production ({@code destiny.ai.enabled} defaults to
     * false, ADR D8), so for most users this text <em>is</em> the reading. A
     * reader who typed "Tôi có nên đổi việc không?" and gets back a page that
     * never mentions the question has no way to tell whether the system
     * considered it, ignored it, or lost it - and until V9 the honest answer
     * was "lost it".
     *
     * <p>The closing sentence is the part that keeps this honest. This class
     * assembles hard data by template; it cannot reason about a question, and
     * must not let the restatement imply that it did. Saying plainly that what
     * follows is the computed signals rather than an answer is the same
     * discipline Rule C applies to an unimplemented algorithm: state the
     * limitation instead of dressing around it.
     */
    private static String buildSummary(NarrativeInput input, FallbackReason reason) {
        StringBuilder summary = new StringBuilder(reason == FallbackReason.AI_DISABLED
                ? "Phần diễn giải AI đang tắt. Dưới đây là tóm tắt dựa trên dữ liệu tính toán gốc cho \""
                        + input.scenarioNameVi() + "\"."
                : "Không thể tạo phần diễn giải AI lúc này. Dữ liệu tính toán gốc vẫn được giữ nguyên "
                        + "cho \"" + input.scenarioNameVi() + "\".");

        String question = input.question();
        if (question != null) {
            summary.append(" Câu hỏi của bạn: \"").append(question).append("\".");
        }
        if (input.focusLabel() != null) {
            summary.append(" Hướng quan tâm: ").append(input.focusLabel()).append(".");
        }
        // Deliberately after the focus line rather than merged into the block
        // above: the disclaimer has to be the last thing the reader sees, and
        // it only applies when there was a question to not be answering.
        if (question != null) {
            summary.append(" Phần dưới đây là các tín hiệu hệ thống tính được, chưa phải "
                    + "câu trả lời trực tiếp cho câu hỏi đó.");
        }
        return summary.toString();
    }

    /**
     * One signal as a line of Vietnamese: which engine, which finding, how it
     * reads, and - when the engine authored one - what it is understood to
     * mean.
     *
     * <p>The categorical part ({@code "TAROT: Sự nghiệp - Thuận lợi (Mạnh)"})
     * used to be the entire line, which told a reader that something was
     * favourable without ever saying what. {@code title} and {@code meaning}
     * are the engine's own authored, research-gated text (R11/R8) that this
     * layer previously discarded; appending them adds no claim, it stops
     * throwing one away.
     *
     * <p>Both are appended only when present. A signal from an engine with no
     * authored corpus renders exactly as it always did - honestly reduced to
     * its categories rather than padded out with invented prose.
     */
    private static String renderSignal(NarrativeSignalItem signal) {
        StringBuilder line = new StringBuilder(signal.engine());
        if (signal.title() != null) {
            line.append(" — ").append(signal.title());
        }
        line.append(": ").append(signal.dimensionLabelVi())
                .append(" - ").append(signal.polarityLabelVi())
                .append(" (").append(signal.strengthLabelVi())
                .append(signal.critical() ? ", đáng chú ý" : "").append(")");
        if (signal.meaning() != null) {
            line.append(" — ").append(signal.meaning());
        }
        return line.toString();
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
