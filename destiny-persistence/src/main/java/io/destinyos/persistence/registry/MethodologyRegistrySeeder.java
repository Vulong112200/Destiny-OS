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

    /**
     * Idempotent per (methodologyId, version): a version already present is
     * left untouched, but a code change that bumps {@code entry.version()}
     * (e.g. promoting a status once real content/algorithm work lands) adds
     * a new version row even on an already-seeded, persistent database
     * (Supabase, not just the in-memory H2 test stand-in) - the same
     * "a version bump, not a silent change" discipline this project applies
     * everywhere else. Caught in practice: promoting TAROT_RWS,
     * NUMEROLOGY_PYTHAGOREAN and CALENDAR_VN_TRADITIONAL to
     * PRODUCTION_READY in code silently had no effect on a real Supabase
     * instance already seeded at "1.0" earlier in the same session, because
     * the previous check only asked "does any version exist" - fixed by
     * versioning the seed data itself.
     */
    @Transactional
    public void seed() {
        for (Entry entry : ENTRIES) {
            boolean versionAlreadySeeded = registry.allVersions(entry.methodologyId).stream()
                    .anyMatch(v -> v.version().equals(entry.version));
            if (!versionAlreadySeeded) {
                registry.register(entry.methodologyId, entry.displayNameVi, entry.domain,
                        entry.version, entry.status, entry.school, entry.source, entry.researchIds,
                        entry.notes);
            }
        }
    }

    private record Entry(String methodologyId, String displayNameVi, String domain,
                         String version, MethodologyStatus status, String school, String source,
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
                    "1.1",
                    MethodologyStatus.PRODUCTION_READY,
                    "Pythagorean",
                    "Standard A-Z letter table (converging across all sources checked); "
                            + "Vietnamese normalization policy (Unicode NFD + explicit đ/Đ "
                            + "substitution) and Life Path per-component reduction order "
                            + "sourced and recorded in RESEARCH_BLOCKERS.md R8, 2026-08-18. "
                            + "Interpretive meaning content grounded in the standard, "
                            + "widely-converged Pythagorean numerology corpus (65 entries: "
                            + "5 number types x values 1-9/11/22/33), authored 2026-08-19.",
                    Set.of("R8"),
                    "Algorithm implemented and golden-tested (destiny-engine-numerology) "
                            + "against independently sourced worked examples: Life Path for "
                            + "1990-03-15 = 1; day 29 preserves master number 11; Expression "
                            + "for 'John Doe' = 8. Vietnamese interpretive content for every "
                            + "(type, value) pair now authored (NumerologyNumberMeanings) - "
                            + "the engine emits one real signal per computed number, "
                            + "dimension OTHER, polarity authored per pair since the same "
                            + "number reads differently by type. Y is simplified to "
                            + "always-consonant for Soul Urge/Personality pending a source "
                            + "for Vietnamese-specific treatment - a labelled "
                            + "simplification, not a researched rule. Chaldean remains "
                            + "RESEARCH_REQUIRED separately: no source for a "
                            + "Vietnamese-orthography mapping exists."),

            new Entry("NUMEROLOGY_CHALDEAN", "Thần số học - Chaldea", "WESTERN",
                    "1.0",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R8"),
                    "No standard Chaldean letter-value mapping exists for "
                            + "Vietnamese orthography. Constructing one would be "
                            + "inventing a methodology, which CLAUDE.md Rule C forbids."),

            new Entry("TAROT_RWS", "Tarot - Rider-Waite-Smith", "WESTERN",
                    "1.1",
                    MethodologyStatus.PRODUCTION_READY,
                    "Rider-Waite-Smith (RWS) - 78 cards: 22 Major Arcana, 56 Minor Arcana",
                    "DESTINY_OS_MASTER_SPECIFICATION.md section 17; interpretive content "
                            + "grounded in the standard Rider-Waite-Smith tradition (A.E. "
                            + "Waite, 1910, Pictorial Key to the Tarot, and the consistent "
                            + "derivative corpus since), authored 2026-08-19 "
                            + "(TarotCardMeanings, contentVersion 1.0)",
                    Set.of("R11"),
                    "Deck structure, seeded shuffle and orientation rule are fully "
                            + "specified. All 78 cards now carry authored Vietnamese "
                            + "meaning content (upright/reversed keywords, polarity, and "
                            + "career/finance/relationship/decision/general text) - the "
                            + "engine emits up to 5 real signals per drawn card. Polarity "
                            + "is authored once per orientation, not per dimension, and "
                            + "strength follows arcana/rank (Major -> STRONG, court -> "
                            + "MEDIUM, numbered Minor -> WEAK) - both documented "
                            + "simplifications, not fabrications."),

            new Entry("CALENDAR_VN_TRADITIONAL", "Lịch Việt Nam truyền thống", "EASTERN",
                    "1.1",
                    MethodologyStatus.PRODUCTION_READY,
                    "Vietnamese lunisolar calendar, 105 degrees East meridian, "
                            + "no-zhongqi leap-month rule",
                    "Jean Meeus, Astronomical Algorithms (1998) - low-precision solar "
                            + "longitude and new-moon series; cross-checked byte-for-byte "
                            + "against two independent implementations (vanng822/amlich "
                            + "JS port and Vietnamese Wikipedia's \"Mo dun:Am lich\" Lua "
                            + "module) and against Ho Ngoc Duc's own published worked "
                            + "tables (xemamlich.uhm.vn/calrules_en.html, 1983-1986, "
                            + "second-precision) plus four named Vietnamese/Chinese "
                            + "divergence years (1985, 2007, 2030, 2053). Historical "
                            + "timezone table per Cong Bao Viet Nam gazette citations "
                            + "(R14a). Gio Ty 23:00 boundary and solar time policy per "
                            + "owner decision R10, 2026-08-19.",
                    Set.of("R14b", "R17"),
                    "destiny-calendar: astronomical core (new moon, solar longitude, "
                            + "solar terms, leap month) and Can Chi Year/Month/Day/Hour "
                            + "arithmetic implemented and golden-tested against 86 "
                            + "assertions, including an exhaustive 1900-2100 Tet scan and "
                            + "every date in Ho Ngoc Duc's published worked tables - none "
                            + "generated from this project's own code (CLAUDE.md section "
                            + "32). R14b (North/South geographic boundary, no source found "
                            + "across three research rounds) and R17 (region granularity, "
                            + "depends on R14b) remain genuinely open: a birth in an "
                            + "affected (date, region) resolves to RESEARCH_REQUIRED with "
                            + "no Can Chi fabricated, never a silent default - this is a "
                            + "per-calculation limitation, not a reason to withhold "
                            + "PRODUCTION_READY from the cases this methodology does cover, "
                            + "the same model TAROT_RWS and NUMEROLOGY_PYTHAGOREAN already "
                            + "use for their own content gaps (R11/R8)."),

            new Entry("BAZI", "Bát Tự - Tứ Trụ", "EASTERN",
                    "1.0",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R1", "R2", "R3"),
                    "Dụng Thần/Hỷ Thần/Kỵ Thần school selection (R1), Đại Vận start "
                            + "age and direction (R2), and Day Master strength "
                            + "assessment (R3) are all unresolved. Also depends "
                            + "transitively on CALENDAR_VN_TRADITIONAL for month "
                            + "boundaries."),

            new Entry("ZIWEI", "Tử Vi Đẩu Số", "EASTERN",
                    "1.0",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R4"),
                    "No authoritative an sao rulebook selected. Master Spec section "
                            + "14 explicitly forbids placeholder star placement."),

            new Entry("WESTERN_ASTROLOGY", "Chiêm tinh học phương Tây", "WESTERN",
                    "1.0",
                    MethodologyStatus.DECISION_REQUIRED, null, null,
                    Set.of("R5", "R6"),
                    "Ephemeris source and its licence (R5 - Swiss Ephemeris is "
                            + "AGPL-or-commercial) and zodiac/house-system/aspect-orb "
                            + "policy (R6) both need a recorded decision before "
                            + "Phase 11."),

            new Entry("FENGSHUI_KUA", "Phong Thủy - Số Cung Phi (Kua)", "EASTERN",
                    "1.0",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R7"),
                    "Female Kua formula, year-boundary policy (Lập Xuân vs Tết vs "
                            + "1 January), and the gendered '5' special case all vary "
                            + "by school and are unresolved."),

            new Entry("ICHING", "Kinh Dịch", "EASTERN",
                    "1.0",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R12"),
                    "Three Coins, Yarrow, Number and Time methods each need their "
                            + "own algorithmVersion and changing-line specification."),

            new Entry("MAIHOA", "Mai Hoa Dịch Số", "EASTERN",
                    "1.0",
                    MethodologyStatus.RESEARCH_REQUIRED, null, null,
                    Set.of("R12"),
                    "Number extraction, timestamp conversion, and trigram/hexagram "
                            + "derivation are not yet specified."),

            new Entry("QIMEN", "Kỳ Môn Độn Giáp", "EASTERN",
                    "1.0",
                    MethodologyStatus.OUT_OF_SCOPE, null, null,
                    Set.of("R13"),
                    "METHODOLOGY_RESEARCH_REGISTER.md section 9: not to be "
                            + "implemented without a full rule specification, which "
                            + "does not exist in this repository.")
    );
}
