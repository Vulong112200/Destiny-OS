package io.destinyos.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the two-message chat prompt from AI_NARRATIVE_SPEC.md section 4
 * (system contract) and section 2 (input shape, serialized as the user
 * message).
 *
 * <p>The caller MUST pass an already-{@link NarrativePruner#prune pruned}
 * {@link NarrativeInput} - this class only renders, it does not prune.
 */
public final class NarrativePromptBuilder {

    /**
     * AI_NARRATIVE_SPEC.md section 4. Every "Khong duoc" line is a direct
     * enforcement of CLAUDE.md Rule B (AI is a narrative layer, never a
     * calculation engine) - this text is the actual mechanism, not
     * documentation of the rule, which is why it is quoted rather than
     * paraphrased and why nothing may be removed from it.
     *
     * <p>The "Cau hoi cua nguoi dung" block was added (and AI_NARRATIVE_SPEC.md
     * section 4 updated to match) once the user's question finally reached this
     * layer at all. It is additive: every prohibition above it is untouched.
     * The block exists because a model handed a question and a pile of signals
     * will otherwise answer the pile - producing a fluent general reading that
     * never addresses what was asked, which is what this system did for as long
     * as the question was being dropped upstream.
     *
     * <p>The final instruction in that block is the important one. "Say the
     * data cannot speak to this" is a legitimate, required answer here, exactly
     * as "Neu du lieu thieu, phai noi thieu du lieu" already is. A model that
     * generalises to fill the gap is fabricating (Rule C) in the one place a
     * reader is least equipped to detect it.
     *
     * <p>The "mang cac CHUOI thuan" line near the end is not redundant with the
     * example object above it, however obvious it looks. Measured against real
     * free models: they mirror the <em>input</em> payload's shape back at us,
     * returning {@code "conflicts": [{"type": ..., "dimension": ...,
     * "involvedEngines": [...], "description": ...}]} - which is exactly the
     * structure this class serializes into the user message. {@link
     * NarrativeResponse} declares those four fields as {@code List<String>},
     * so Jackson rejects the object form and a perfectly good narrative is
     * discarded as MALFORMED_JSON. Naming the failure explicitly ("not an
     * object with type/dimension/description") is what fixed it on the models
     * that were otherwise well-behaved; a bare example was not enough.
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

            Câu hỏi của người dùng:
            - trường "question" là câu hỏi cụ thể người dùng đã đặt; trường "scenario.focus" là hướng quan tâm họ đã chọn;
            - phải trả lời thẳng vào câu hỏi đó, không viết một bài luận chung chung về kịch bản;
            - mọi nhận định phải bám vào các tín hiệu trong "signals" và phần "meaning" đi kèm từng tín hiệu;
            - trường "meaning" là nội dung đã được soạn sẵn và kiểm duyệt trong hệ thống: được trích dẫn, tóm tắt, diễn giải lại, nhưng không được viết thêm ý nghĩa mới cho bất kỳ lá bài, con số hay tín hiệu nào;
            - tín hiệu không có "meaning" thì chỉ được nói ở mức phân loại (chiều, tính chất, mức độ), không được tự đặt ý nghĩa cho nó;
            - nếu câu hỏi hỏi về điều mà dữ liệu được cung cấp không nói tới, phải nói rõ là dữ liệu hiện có không trả lời được điều đó, thay vì nói chung chung cho có.

            Ngôn ngữ: tiếng Việt.
            Giọng: khách quan, dễ hiểu, không hù dọa.

            Trả lời CHỈ BẰNG một đối tượng JSON hợp lệ, đúng định dạng:
            {"summary": "...", "keySignals": ["..."], "conflicts": ["..."], "cautions": ["..."], "reflectionQuestions": ["..."]}
            Bốn trường keySignals, conflicts, cautions, reflectionQuestions BẮT BUỘC là mảng các CHUỖI thuần, không phải mảng đối tượng.
            Mỗi phần tử là một câu tiếng Việt hoàn chỉnh, không phải một object có "type"/"dimension"/"description".
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
        // First key in the object, ahead of the spec's original seven: it is
        // what the whole response is supposed to be about, and burying it after
        // a hardData block invites the model to answer the data instead.
        payload.put("question", input.question());

        // LinkedHashMap, not Map.of: focus is legitimately absent most of the
        // time and Map.of throws on a null value. An explicit "focus": null is
        // also the more honest thing to send than a silently missing key - the
        // model is told the user chose no particular angle, rather than left to
        // infer it.
        Map<String, Object> scenario = new LinkedHashMap<>();
        scenario.put("name", input.scenarioNameVi());
        scenario.put("focus", input.focusLabel());
        payload.put("scenario", scenario);

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
        // The engine's own authored text (R11/R8), not a summary of it. Sent
        // as null when the engine authored none - see NarrativeSignalItem's
        // Javadoc for why an absence must stay an absence here.
        map.put("title", signal.title());
        map.put("meaning", signal.meaning());
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
