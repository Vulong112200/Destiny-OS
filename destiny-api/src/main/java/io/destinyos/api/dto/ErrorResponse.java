package io.destinyos.api.dto;

/** A plain, safe error body — never a stack trace (CLAUDE.md section 40). */
public record ErrorResponse(String code, String message) {
}
