package io.destinyos.api.controller;

import io.destinyos.i18n.VietnameseLabels;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code Label} API group: every Vietnamese label the UI might need, keyed by
 * enum type and technical name.
 *
 * <p><strong>Why this endpoint exists.</strong> Until Phase 8a, every enum that
 * reached the browser travelled inside a {@code LabeledValue} — technical name
 * and Vietnamese label paired at the point of mapping, so the frontend never
 * needed a lookup table. A Bát Tự chart breaks that: it arrives as
 * {@code Evidence.fact} maps holding raw names like {@code GIAP} and
 * {@code TY_KIEN}, because {@code Evidence} is defined as a structured finding
 * and explicitly not prose. Something has to translate them, and the two
 * alternatives were both worse — putting Vietnamese strings into engine output
 * (display text in the layer that must stay free of it) or duplicating the
 * label table in TypeScript (two registries that drift, with
 * {@code LabelCoverageTest} guarding only one of them).
 *
 * <p>UI_UX_VIETNAMESE_SPEC §1 forbids a bare technical enum reaching a user.
 * This endpoint is what makes that achievable for free-form fact data.
 */
@RestController
@RequestMapping("/api/v1/labels")
public class LabelController {

    @GetMapping
    public Map<String, Map<String, String>> all() {
        // Static data derived from a compile-time registry: no domain
        // calculation happens here, so the "controllers stay thin" rule
        // (CLAUDE.md §3) is satisfied without an intervening service that
        // would only forward the call.
        return VietnameseLabels.asStringRegistries();
    }
}
