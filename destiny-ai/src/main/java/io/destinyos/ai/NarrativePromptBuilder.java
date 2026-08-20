package io.destinyos.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the two-message chat prompt from AI_NARRATIVE_SPEC.md section 4
 * (system contract, quoted verbatim - this is the project's own specified
 * prompt, not something this class is free to paraphrase) and section 2
 * (input shape, serialized as the user message).
 *
 * <p>The caller MUST pass an already-{@link NarrativePruner#prune pruned}
 * {@link NarrativeInput} - this class only renders, it does not prune.
 */
public final class NarrativePromptBuilder {

    /**
     * AI_NARRATIVE_SPEC.md section 4, unchanged. Every "Khong duoc" line is
     * a direct enforcement of CLAUDE.md Rule B (AI is a narrative layer,
     * never a calculation engine) - this text is the actual mechanism, not
     * documentation of the rule.
     */
    static final String SYSTEM_PROMPT = """
            Bạn là lớp diễn giải ngôn ngữ của Destiny OS.

            Bạn chỉ được sử dụng dữ liệu được cung cấp.

            Không được:
            - tính toán lại;
            - sửa dữ liệu;
            - thêm sao;
            - thêm lá bài;
            - thêm hành tinh;
            - thêm quẻ;
            - tạo evidence;
            - thay đổi kết quả Fusion;
            - trình bày huyền học như khoa học đã được chứng minh.

            Nếu dữ liệu mâu thuẫn, phải nói rõ mâu thuẫn.
            Nếu dữ liệu thiếu, phải nói thiếu dữ liệu.

            Ngôn ngữ: tiếng Việt.
            Giọng: khách quan, dễ hiểu, không hù dọa.

            Trả lời CHỈ BẰNG một đối tượng JSON hợp lệ, đúng định dạng:
            {"summary": "...", "keySignals": ["..."], "conflicts": ["..."], "cautions": ["..."], "reflectionQuestions": ["..."]}
            Không thêm văn bản nào ngoài đối tượng JSON đó.""";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private NarrativePromptBuilder() {
    }

    public static NarrativePrompt build(NarrativeInput prunedInput) {
        String userPayload = renderUserPayload(prunedInput);
        return new NarrativePrompt(List.of(
                ChatMessage.system(SYSTEM_PROMPT),
                ChatMessage.user(userPayload)));
    }

    private static String renderUserPayload(NarrativeInput input) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("scenario", Map.of("name", input.scenarioNameVi()));
        payload.put("hardData", input.hardDataSummary());
        payload.put("signals", input.signals().stream().map(NarrativePromptBuilder::signalToMap).toList());
        payload.put("conflicts", input.conflicts().stream().map(NarrativePromptBuilder::conflictToMap).toList());
        payload.put("warnings", input.warnings());
        payload.put("limitations", input.limitations());
        payload.put("calculationMetadata", input.calculationMetadata());

        try {
            return MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize pruned narrative input", e);
        }
    }

    private static Map<String, Object> signalToMap(NarrativeSignalItem signal) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("engine", signal.engine());
        map.put("dimension", signal.dimensionLabelVi());
        map.put("polarity", signal.polarityLabelVi());
        map.put("strength", signal.strengthLabelVi());
        map.put("critical", signal.critical());
        map.put("tag", signal.tag());
        return map;
    }

    private static Map<String, Object> conflictToMap(NarrativeConflictItem conflict) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", conflict.typeLabelVi());
        map.put("dimension", conflict.dimension());
        map.put("involvedEngines", conflict.involvedEngines());
        map.put("description", conflict.description());
        return map;
    }
}
