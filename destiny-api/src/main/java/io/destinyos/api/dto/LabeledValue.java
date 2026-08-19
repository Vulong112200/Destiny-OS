package io.destinyos.api.dto;

/**
 * A technical enum value paired with its Vietnamese label.
 *
 * <p>UI_UX_VIETNAMESE_SPEC section 1: a bare technical enum must never
 * appear alone in a user-facing surface. Every enum-valued field in this
 * API's responses is wrapped in this type instead of being emitted as a
 * plain string, so a client cannot render "MAJOR_CONFLICT" without also
 * having "Mâu thuẫn đáng chú ý" available in the same payload. The
 * technical value stays present too — section 1 explicitly permits it for
 * tooltips and technical-detail views.
 *
 * <p>Deliberately takes the resolved label as a plain {@code String}
 * argument rather than a {@code Function<E, String>} labeler reference:
 * {@code VietnameseLabels::of} is an overloaded method reference, and
 * Java's generic inference cannot resolve both "which overload" and "what
 * is E" from the same call when E is itself the method's own type
 * variable — every call site failed to compile with "cannot infer
 * type-variable(s) E" until this was simplified to a direct value.
 *
 * @param technical the enum constant name, e.g. {@code "MAJOR_CONFLICT"}
 * @param labelVi   the Vietnamese label, e.g. {@code "Mâu thuẫn đáng chú ý"}
 */
public record LabeledValue(String technical, String labelVi) {

    public static LabeledValue of(Enum<?> value, String labelVi) {
        return new LabeledValue(value.name(), labelVi);
    }

    /**
     * Null-safe variant: returns {@code null} when {@code value} is
     * {@code null}, without ever evaluating {@code labelSupplier} in that
     * case. Takes a supplier rather than an already-resolved label
     * specifically so a caller can write
     * {@code ofNullable(x, () -> VietnameseLabels.of(x))} — with a plain
     * {@code String} parameter, Java evaluates it eagerly, which would call
     * {@code VietnameseLabels.of(null)} (and have it throw, per its own
     * null-rejection contract) before this method got a chance to check.
     */
    public static LabeledValue ofNullable(Enum<?> value, java.util.function.Supplier<String> labelSupplier) {
        return value == null ? null : new LabeledValue(value.name(), labelSupplier.get());
    }
}
