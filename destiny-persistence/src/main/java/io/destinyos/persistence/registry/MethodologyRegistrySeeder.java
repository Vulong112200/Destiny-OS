package io.destinyos.persistence.registry;

import io.destinyos.engine.MethodologyStatus;
import java.util.List;
import java.util.Set;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registers the methodologies named in the specification with their real,
 * current status (ADR D7).
 *
 * <p><strong>Every status and research reference below must trace back to
 * {@code docs/RESEARCH_BLOCKERS.md}.</strong> This class is not the place to
 * decide a methodology's status - it is the place that records the decision
 * already made there. If the register changes, this seeder must change to
 * match it; it must never drift ahead of it.
 *
 * <p>Deliberately absent from this list:
 * <ul>
 *   <li>The sexagenary cycle - pure arithmetic, not a user-selectable school,
 *       so it is not a registry entry (it has no Dụng Thần-style dispute to
 *       register a status against).</li>
 *   <li>Fusion, Applicability, Scenario - core infrastructure, not a
 *       metaphysical methodology a user selects.</li>
 *   <li>The Chinese UTC+8 calendar comparison methodology (ADR D3 permits
 *       it, does not commit to it) - not yet scheduled in any phase, so
 *       registering it now would be speculative.</li>
 * </ul>
 */
@Component
public class MethodologyRegistrySeeder {

    private final MethodologyRegistryService registry;

    public MethodologyRegistrySeeder(MethodologyRegistryService registry) {
        this.registry = registry;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedOnStartup() {
        seed();
    }

    /** Idempotent: entries already present are left untouched. */
    @Transactional
    public void seed() {
        for (Entry entry : ENTRIES) {
            if (registry.allVersions(entry.methodologyId).isEmpty()) {
                registry.register(entry.methodologyId, entry.displayNameVi, entry.domain,
                        "1.0", entry.status, entry.school, entry.source, entry.researchIds,
                        entry.notes);
            }
        }
    }

    private record Entry(String methodologyId, String displayNameVi, String domain,
                         MethodologyStatus status, String school, String source,
                         Set<String> researchIds, String notes) {
    }

    // school/source are null for every entry below except TAROT_RWS.
    // CONTENT_REQUIRED and PRODUCTION_READY are the two statuses under which
    // MethodologyStatus.mayCalculate() is true, and TAROT_RWS is the one
    // entry at that status - Tarot's algorithm (RWS deck structure, seeded
    // shuffle) is fully specified even though its meaning content is not.
    // Getting this wrong here is exactly the mistake the
    // MethodologyVersionEntity constructor guard exists to catch: an
    // earlier draft of this seeder left TAROT_RWS's school/source blank and
    // registration failed loudly at startup, as it should have.
    private static final List<Entry> ENTRIES = List.of(

            new Entry("NUMEROLOGY_PYTHAGOREAN", "Thần số học - Pythagoras", "WESTERN",
                    MethodologyStatus.DECISION_REQUIRED, null, null,
                    Set.of("R8"),
                    "Letter-to-number mapping is standard and fixed. What is "
                            + "outstanding is the Vietnamese-orthography normalization "
                            + "policy (diacritics, đ, reduction order) - RESEARCH_GAPS.md "
                            + "worked example flags the reduction-order question "
                            + "specifically. A decision, not open research."),

            new Entry("NUMEROLOGY_CHALDEAN", "Thần số học - Chaldea", "WESTERN",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R8"),
                    "No standard Chaldean letter-value mapping exists for "
                            + "Vietnamese orthography. Constructing one would be "
                            + "inventing a methodology, which CLAUDE.md Rule C forbids."),

            new Entry("TAROT_RWS", "Tarot - Rider-Waite-Smith", "WESTERN",
                    MethodologyStatus.CONTENT_REQUIRED,
                    "Rider-Waite-Smith (RWS) - 78 cards: 22 Major Arcana, 56 Minor Arcana",
                    "DESTINY_OS_MASTER_SPECIFICATION.md section 17",
                    Set.of("R11"),
                    "Deck structure, seeded shuffle and orientation rule are fully "
                            + "specified. Only the 78-card Vietnamese meaning corpus is "
                            + "outstanding, and that is content authorship, not "
                            + "algorithm - this is why CONTENT_REQUIRED (not "
                            + "RESEARCH_REQUIRED) is the correct status: the engine may "
                            + "ship."),

            new Entry("CALENDAR_VN_TRADITIONAL", "Lịch Việt Nam truyền thống", "EASTERN",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R9", "R10", "R14", "R15", "R16", "R17"),
                    "Six open items per ADR D3 (Calendar Authority Rule): solar term "
                            + "dataset sourcing (R9), giờ Tý/solar-time policy (R10), "
                            + "historical timezone rules by date and region especially "
                            + "1955-1975 (R14), new moon computation and its "
                            + "localisation meridian (R15), leap month insertion rule "
                            + "(R16), and region model granularity (R17). This is the "
                            + "project's critical path."),

            new Entry("BAZI", "Bát Tự - Tứ Trụ", "EASTERN",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R1", "R2", "R3"),
                    "Dụng Thần/Hỷ Thần/Kỵ Thần school selection (R1), Đại Vận start "
                            + "age and direction (R2), and Day Master strength "
                            + "assessment (R3) are all unresolved. Also depends "
                            + "transitively on CALENDAR_VN_TRADITIONAL for month "
                            + "boundaries."),

            new Entry("ZIWEI", "Tử Vi Đẩu Số", "EASTERN",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R4"),
                    "No authoritative an sao rulebook selected. Master Spec section "
                            + "14 explicitly forbids placeholder star placement."),

            new Entry("WESTERN_ASTROLOGY", "Chiêm tinh học phương Tây", "WESTERN",
                    MethodologyStatus.DECISION_REQUIRED, null, null,
                    Set.of("R5", "R6"),
                    "Ephemeris source and its licence (R5 - Swiss Ephemeris is "
                            + "AGPL-or-commercial) and zodiac/house-system/aspect-orb "
                            + "policy (R6) both need a recorded decision before "
                            + "Phase 11."),

            new Entry("FENGSHUI_KUA", "Phong Thủy - Số Cung Phi (Kua)", "EASTERN",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R7"),
                    "Female Kua formula, year-boundary policy (Lập Xuân vs Tết vs "
                            + "1 January), and the gendered '5' special case all vary "
                            + "by school and are unresolved."),

            new Entry("ICHING", "Kinh Dịch", "EASTERN",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R12"),
                    "Three Coins, Yarrow, Number and Time methods each need their "
                            + "own algorithmVersion and changing-line specification."),

            new Entry("MAIHOA", "Mai Hoa Dịch Số", "EASTERN",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R12"),
                    "Number extraction, timestamp conversion, and trigram/hexagram "
                            + "derivation are not yet specified."),

            new Entry("QIMEN", "Kỳ Môn Độn Giáp", "EASTERN",
                    MethodologyStatus.OUT_OF_SCOPE, null, null,
                    Set.of("R13"),
                    "METHODOLOGY_RESEARCH_REGISTER.md section 9: not to be "
                            + "implemented without a full rule specification, which "
                            + "does not exist in this repository.")
    );
}
