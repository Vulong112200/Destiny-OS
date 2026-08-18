package io.destinyos.persistence.calculation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Persists {@code Map<String, Object>} (e.g. {@link io.destinyos.core.evidence.Evidence#fact()})
 * as a TEXT column containing JSON.
 *
 * <p>TEXT rather than a native JSON/JSONB column type — see
 * destiny-persistence's pom.xml — so the same migration and entity work
 * unchanged on both PostgreSQL and the H2 compatibility mode used for local
 * verification.
 */
@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<LinkedHashMap<String, Object>> TYPE = new TypeReference<>() { };

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize fact map to JSON", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Map.of();
        }
        try {
            return MAPPER.readValue(dbData, TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not deserialize fact JSON: " + dbData, e);
        }
    }
}
