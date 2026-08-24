package io.destinyos.engines.bazi;

import io.destinyos.calendar.EarthlyBranch;
import io.destinyos.calendar.FiveElement;
import io.destinyos.calendar.HeavenlyStem;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Thiệu Vĩ Hoa &amp; Trần Viên's point-scoring method for Day Master strength
 * (R3), resolved 2026-08-24. Source: "Dự đoán theo Tứ trụ", Chương 11 §II
 * (pp.331-356). Verified by Claude Opus —
 * {@code docs/research_drafts/VERIFICATION_OPUS_R3.md} — against five of
 * seven worked examples (two, including the chapter's own first example,
 * were found to contain arithmetic errors and must not be used as fixtures).
 * The six points the source left ambiguous (four found before implementation,
 * two more found while getting the golden tests to pass exactly) are decided
 * in {@code docs/DECISION_LOG.md} and referenced by number (1-6) below.
 *
 * <p><strong>What this class deliberately does not attempt</strong> (ADR D7 —
 * declines rather than guesses):
 * <ul>
 *   <li><strong>Special-pattern charts (cách cục đặc biệt).</strong> The
 *       source's own formula excludes them (p.331), and its section III
 *       (pp.356-376, which would identify them) was not read. Every result
 *       from this class is only as good as the assumption that the chart is
 *       an ordinary one — callers must present it with that caveat.</li>
 *   <li><strong>An unmitigated Lục Xung (six-clash).</strong> The source's
 *       exact fractional-loss table for this case ("BẢNG TRA CHI THÁNG LÀM
 *       TỔN THẤT", p.345, a 12x8 grid) was not transcribed during research.
 *       {@link #resolve} returns {@link Optional#empty()} for a chart
 *       containing a genuine, unmitigated clash between two branches
 *       (Rule C: no fabricated fraction) rather than silently reusing the
 *       degrees table for an unrelated case.</li>
 *   <li><strong>Thiên can ngũ hợp's phu tòng thê hóa / thê tòng phu hóa
 *       asymmetric sub-rules</strong> (the source's rules 5-6, p.337) —
 *       transformation success for a stem pair is decided here only by the
 *       narrower "dẫn hóa" test of whether the resultant element is the
 *       month branch's own ruling element (see {@link #danHoa}), which is
 *       what every worked example this class was checked against actually
 *       uses — none exercises a genuinely successful stem transform to
 *       calibrate a broader test against. A stem pair whose transformation
 *       genuinely depends on the asymmetric rule is treated as failing to
 *       transform, which understates rather than overstates any resulting
 *       strength — the same "decline toward the conservative case" this
 *       class uses for Lục Xung.</li>
 * </ul>
 */
final class DayMasterStrengthResolver {

    /** Bumps whenever a rule choice in this class's own logic changes (the six DECISION_LOG.md decisions). */
    static final String RULE_VERSION = "1.0";

    private DayMasterStrengthResolver() {
    }

    /** The five Thiên Can Ngũ Hợp pairs and what they transform into. Universal, no school variance found. */
    private record NguHopPair(HeavenlyStem a, HeavenlyStem b, FiveElement element) {
    }

    private static final java.util.List<NguHopPair> NGU_HOP = java.util.List.of(
            new NguHopPair(HeavenlyStem.GIAP, HeavenlyStem.KY, FiveElement.EARTH),
            new NguHopPair(HeavenlyStem.AT, HeavenlyStem.CANH, FiveElement.METAL),
            new NguHopPair(HeavenlyStem.BINH, HeavenlyStem.TAN, FiveElement.WATER),
            new NguHopPair(HeavenlyStem.DINH, HeavenlyStem.NHAM, FiveElement.WOOD),
            new NguHopPair(HeavenlyStem.MAU, HeavenlyStem.QUY, FiveElement.FIRE)
    );

    private static Optional<FiveElement> nguHopElement(HeavenlyStem a, HeavenlyStem b) {
        for (NguHopPair p : NGU_HOP) {
            if ((p.a == a && p.b == b) || (p.a == b && p.b == a)) {
                return Optional.of(p.element);
            }
        }
        return Optional.empty();
    }

    /**
     * @param year, month, day, hour the four pillars, Day Master taken from
     *              {@code day}'s stem
     * @return empty if this chart cannot be scored by this method — an
     *         unmitigated Lục Xung was found (see class Javadoc)
     */
    static Optional<DayMasterStrength> resolve(BaziPillar year, BaziPillar month,
                                               BaziPillar day, BaziPillar hour) {
        HeavenlyStem[] stems = { year.stem(), month.stem(), day.stem(), hour.stem() };
        EarthlyBranch[] branches = { year.branch(), month.branch(), day.branch(), hour.branch() };

        BranchResolution br = resolveBranches(branches, stems);
        if (br.unmitigatedXung) {
            return Optional.empty();
        }

        // --- Stems: cho dua (support) base ---
        Set<FiveElement> supportingElements = effectiveBranchElements(branches, br);
        int[] stemDegree = new int[4];
        boolean[] stemHasSupport = new boolean[4];
        for (int i = 0; i < 4; i++) {
            HeavenlyStem s = stems[i];
            boolean supported = supportingElements.contains(s.element())
                    || supportingElements.contains(s.element().generatedBy());
            stemHasSupport[i] = supported;
            stemDegree[i] = supported ? 36 : 9;
        }

        // --- Stems: Ngu Hop among adjacent pairs, with tranh hop handling ---
        boolean[] stemInAttemptedCombination = new boolean[4];
        FiveElement[] stemFinalElement = new FiveElement[4];
        for (int i = 0; i < 4; i++) {
            stemFinalElement[i] = stems[i].element();
        }
        resolveStemCombinations(stems, branches, br, stemDegree, stemHasSupport,
                stemInAttemptedCombination, stemFinalElement);

        // --- Stems: Tuong khac (mutual control), skipping combined stems ---
        for (int i = 0; i < 4; i++) {
            if (stemInAttemptedCombination[i]) continue;
            for (int j = 0; j < 4; j++) {
                if (i == j || stemInAttemptedCombination[j]) continue;
                if (stemFinalElement[i].controls() == stemFinalElement[j]) {
                    // i controls j - can i still exert force?
                    if (stemDegree[i] <= 18) continue; // hu phu / already-weak cannot control
                    int distance = Math.abs(i - j);
                    int loss = switch (distance) {
                        case 1 -> 12; // khac gan: -1/3 of 36
                        case 2 -> 6;  // khac cach: -1/6 of 36
                        default -> 0; // khac xa: no loss
                    };
                    stemDegree[j] = Math.max(0, stemDegree[j] - loss);
                }
            }
        }

        // --- Stems: same-pillar branch influence (5 cases), using the branch's OWN natural element ---
        for (int i = 0; i < 4; i++) {
            FiveElement stemEl = stemFinalElement[i];
            FiveElement branchEl = branches[i].element();
            if (branchEl == stemEl || branchEl.generates() == stemEl) {
                // unchanged (sinh phu, or same element)
            } else if (stemEl.generates() == branchEl) {
                stemDegree[i] -= 6; // stem drains into its own branch (xi hoi)
            } else if (stemEl.controls() == branchEl) {
                stemDegree[i] -= 12; // stem controls its own branch
            } else if (branchEl.controls() == stemEl) {
                stemDegree[i] -= 18; // branch controls its own stem
            }
            stemDegree[i] = Math.max(0, stemDegree[i]);
        }

        // --- Branches: degree contributions (groups lumped once, others individually) ---
        Map<FiveElement, Integer> totals = new EnumMap<>(FiveElement.class);
        for (FiveElement e : FiveElement.values()) totals.put(e, 0);
        for (int i = 0; i < 4; i++) {
            totals.merge(stemFinalElement[i], stemDegree[i], Integer::sum);
        }

        Set<Integer> lumped = new HashSet<>();
        for (BranchGroup g : br.groups) {
            if (!g.transformed) continue;
            totals.merge(g.element, g.lumpDegree, Integer::sum);
            lumped.addAll(g.positions);
        }
        for (int i = 0; i < 4; i++) {
            if (lumped.contains(i)) continue;
            Map<FiveElement, Integer> branchDegrees = br.khuBi.contains(i)
                    ? khuBiDegrees(branches[i])
                    : BranchDegreeTable.elementDegrees(branches[i]);
            // Truc dinh modifier: the SAME-PILLAR stem only (p.341: "duoc
            // thien can CUNG TRU sinh phu" for the boost; the penalty rule
            // is described the same way - see DayMasterStrengthResolverTest
            // for the Vi du 6 case this distinguishes: two identical Than
            // branches get different outcomes because only one sits under a
            // stem that actually controls it). Decision 2 drops the >=18
            // gate the source states for the echo clause - not applied
            // consistently in the source's own worked examples.
            //
            // The boost fires only for a same-element same-pillar stem (Vi
            // du 1's Mui/Ky and Vi du 5's Mui/Ky, both p.341's "tuong dong
            // ngu hanh" case), not a stem that merely GENERATES its branch:
            // Vi du 6's Dinh Mui pillar has Dinh(Fire) generating Mui(Earth)
            // same-pillar, and the book's own step-by-step (p.350 steps 6-7)
            // lists only Than and Mao as receiving a branch modifier there -
            // Mui gets none. A stem generating its branch already has its
            // own separate, already-applied effect: the stem itself loses 6
            // degrees (xi hoi, p.341 rule 3, applied above) - it does not
            // also boost the branch.
            //
            // "Ban than dia chi khong gap hop" (p.341 rule 2, a precondition
            // for the boost only - the penalty rule 3 on p.342 states no such
            // precondition) means specifically LUC HOP, not any combination
            // attempt: Vi du 5 p.350 step 5 has Tuat (in an attempted, failed
            // Luc Hop with Mao) explicitly keep its plain ban khi with no
            // boost, while step 7 has Mui (in an attempted, failed Ban Tam
            // Hop with the same Mao) receive the +6 boost anyway - confirmed
            // by the example's own final Tho total (137), which is exactly
            // ban-khi-only-Tuat + boosted-Mui and off by 6 (a second boosted
            // branch) or by -6 (no boosted branch) under either alternative.
            FiveElement principalEl = BranchDegreeTable.principal(branches[i]).element();
            FiveElement samePillarStemEl = stemFinalElement[i];
            boolean branchInLucHop = br.inLucHop.contains(i);
            Map<FiveElement, Integer> adjusted = new EnumMap<>(branchDegrees);
            boolean boosts = samePillarStemEl == principalEl;
            boolean penalises = samePillarStemEl.controls() == principalEl;
            if (boosts && !branchInLucHop) {
                adjusted.merge(principalEl, 6, Integer::sum);
            } else if (penalises) {
                adjusted.merge(principalEl, -6, (a, b) -> Math.max(0, a + b));
            }
            adjusted.forEach((el, deg) -> totals.merge(el, deg, Integer::sum));
        }

        // --- Seasonal command (chi thang nam lenh): only two of the five
        // elements move. The season's own element gets x6/5; the single
        // element the season CONTROLS (khac xuat) gets x4/5; the other three
        // (sinh xuat, sinh nhap, khac nhap) are untouched. Confirmed against
        // three primary-source result tables (p.347 Vi du 1: Kim nam lenh ->
        // Moc, the element Kim controls, is the only one penalised, Hoa/Tho/
        // Thuy unchanged; p.348 Vi du 3: Moc nam lenh -> Tho, the element Moc
        // controls, is the only one penalised) - an earlier draft penalised
        // all four non-season elements uniformly, which p.347/348 rule out.
        FiveElement seasonal = br.monthTransformedTo != null
                ? br.monthTransformedTo
                : branches[1].element();
        FiveElement seasonalControls = seasonal.controls();
        Map<FiveElement, Integer> seasoned = new EnumMap<>(FiveElement.class);
        for (FiveElement e : FiveElement.values()) {
            int base = totals.get(e);
            int adjustedVal;
            if (e == seasonal) {
                adjustedVal = (12 * base + 5) / 10;   // x 6/5, round to nearest
            } else if (e == seasonalControls) {
                adjustedVal = (8 * base + 5) / 10;    // x 4/5, round to nearest
            } else {
                adjustedVal = base;
            }
            seasoned.put(e, Math.max(0, adjustedVal));
        }

        HeavenlyStem dayMaster = stems[2];
        FiveElement dmElement = dayMaster.element();
        int total = seasoned.values().stream().mapToInt(Integer::intValue).sum();
        int ownSide = seasoned.get(dmElement) + seasoned.get(dmElement.generatedBy());
        boolean vuong = 5L * ownSide >= 2L * total;

        return Optional.of(new DayMasterStrength(vuong, seasoned, ownSide, total, seasonal));
    }

    /** Khử bì: only the branch's own bản khí (principal stem's element/degree) survives. */
    private static Map<FiveElement, Integer> khuBiDegrees(EarthlyBranch branch) {
        HeavenlyStem principal = BranchDegreeTable.principal(branch);
        int degree = BranchDegreeTable.of(branch).get(principal);
        Map<FiveElement, Integer> m = new EnumMap<>(FiveElement.class);
        m.put(principal.element(), degree);
        return m;
    }

    /** Every element present across all 4 branches' currently-effective hidden stems, post branch resolution. */
    private static Set<FiveElement> effectiveBranchElements(EarthlyBranch[] branches, BranchResolution br) {
        Set<FiveElement> result = new HashSet<>();
        Set<Integer> lumped = new HashSet<>();
        for (BranchGroup g : br.groups) {
            if (!g.transformed) continue;
            result.add(g.element);
            lumped.addAll(g.positions);
        }
        for (int i = 0; i < 4; i++) {
            if (lumped.contains(i)) continue;
            if (br.khuBi.contains(i)) {
                result.add(BranchDegreeTable.principal(branches[i]).element());
            } else {
                result.addAll(BranchDegreeTable.elementDegrees(branches[i]).keySet());
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    // Stem Ngu Hop resolution
    // ------------------------------------------------------------------

    private static void resolveStemCombinations(HeavenlyStem[] stems, EarthlyBranch[] branches,
                                                 BranchResolution br, int[] stemDegree,
                                                 boolean[] stemHasSupport,
                                                 boolean[] stemInAttemptedCombination,
                                                 FiveElement[] stemFinalElement) {
        boolean[][] adjacentPairIsNguHop = new boolean[4][4];
        FiveElement[][] pairElement = new FiveElement[4][4];
        for (int i = 0; i < 3; i++) {
            int j = i + 1;
            Optional<FiveElement> el = nguHopElement(stems[i], stems[j]);
            if (el.isPresent()) {
                adjacentPairIsNguHop[i][j] = true;
                pairElement[i][j] = el.get();
            }
        }

        // Tranh hop: two overlapping adjacent pairs sharing a stem.
        boolean tranhHop01_12 = adjacentPairIsNguHop[0][1] && adjacentPairIsNguHop[1][2];
        boolean tranhHop12_23 = adjacentPairIsNguHop[1][2] && adjacentPairIsNguHop[2][3];

        if (tranhHop01_12) {
            applyTranhHop(new int[]{0, 1, 2}, stemDegree, stemInAttemptedCombination);
        }
        if (tranhHop12_23) {
            applyTranhHop(new int[]{1, 2, 3}, stemDegree, stemInAttemptedCombination);
        }

        for (int i = 0; i < 3; i++) {
            int j = i + 1;
            if (!adjacentPairIsNguHop[i][j]) continue;
            if (stemInAttemptedCombination[i] || stemInAttemptedCombination[j]) continue; // already handled by tranh hop

            // Kep khac: a hu-phu stem controlled from its OUTSIDE neighbor
            // too (i.e. pinched between its would-be hop partner and its far
            // neighbor, both controlling it) does not attempt the hop at all
            // - Vi du 5 step 3 (p.349): Quy thuy is adjacent to Mau (its ngu
            // hop partner, Mau-Quy hoa Hoa) but the book never invokes "ngu
            // hop khong hoa" for this pair the way it does for every other
            // failed pair in the other worked examples - it goes straight to
            // "quy thuy hu phu, con bi mau, ky KEP KHAC nen tinh la khong",
            // i.e. controlled from BOTH sides down to zero. Falling through
            // to the ordinary cho-dua (9) + tuong khac path below reproduces
            // that 0 exactly (two separate -12 losses, each clamped at 0);
            // a lone-sided control (Vi du 7's Quy-Mau pair, only its hop
            // partner controls it) is unaffected and still attempts the hop.
            boolean iHuPhu = !stemHasSupport[i];
            boolean jHuPhu = !stemHasSupport[j];
            if (iHuPhu != jHuPhu) {
                int weakPos = iHuPhu ? i : j;
                int outsideNeighbor = weakPos == i ? i - 1 : j + 1;
                if (outsideNeighbor >= 0 && outsideNeighbor <= 3) {
                    FiveElement weakEl = stems[weakPos].element();
                    if (stems[outsideNeighbor].element().controls() == weakEl) {
                        continue;
                    }
                }
            }

            stemInAttemptedCombination[i] = true;
            stemInAttemptedCombination[j] = true;
            FiveElement resultElement = pairElement[i][j];

            boolean bothHuPhu = !stemHasSupport[i] && !stemHasSupport[j];
            if (bothHuPhu) {
                // "hai can deu hu phu thi khong xem la hop ma xem la hu phu" - unchanged at 9 each.
                continue;
            }
            // "Ngu hop co mot can hu phu van xem la hop nen khong tinh la hu
            // phu": when only ONE side lacks its own cho dua, BOTH stems
            // still start from the full 36 base for the combination math
            // below - the attempted hop itself exempts the weaker side from
            // the hu-phu fallback (Vi du 7 step 1: At khong co cho dua rieng
            // nhung van tinh tu 36, khong phai tu 9).
            stemDegree[i] = 36;
            stemDegree[j] = 36;

            boolean hoaSucceeds = danHoa(resultElement, stems, branches, br, i, j);
            if (hoaSucceeds) {
                stemDegree[i] = 60;
                stemDegree[j] = 60;
                stemFinalElement[i] = resultElement;
                stemFinalElement[j] = resultElement;
            } else {
                // "hop ma khong hoa duoc xem la khac gan": the losing side (per
                // sinh-khac between the two stems' own elements) drops 1/3.
                FiveElement ei = stems[i].element();
                FiveElement ej = stems[j].element();
                if (ei.controls() == ej) {
                    stemDegree[j] = Math.max(0, stemDegree[j] - stemDegree[j] / 3);
                } else if (ej.controls() == ei) {
                    stemDegree[i] = Math.max(0, stemDegree[i] - stemDegree[i] / 3);
                }
                // If neither controls the other, no sinh-khac relation exists
                // between them and the "treat as khac gan" rule does not
                // apply to either side - both keep their cho-dua degree.
            }
        }
    }

    private static void applyTranhHop(int[] positions, int[] stemDegree, boolean[] stemInAttemptedCombination) {
        for (int p : positions) {
            stemInAttemptedCombination[p] = true;
            stemDegree[p] = Math.max(0, stemDegree[p] - stemDegree[p] / 3);
        }
    }

    /**
     * Narrowed dẫn hóa test for a stem pair (decision: only the month
     * branch's own ruling element counts — see class Javadoc for why rules
     * 5-6 are skipped entirely). This is narrower than the source's stated
     * rules 1-4, which also credit a hidden stem or another visible stem
     * present anywhere in the chart; that fuller test produced false
     * positives when checked against Ví dụ 6 and 7 (both stem pairs there
     * fail to transform, per the book, despite the resultant elements
     * appearing as minor hidden stems and as another visible stem
     * respectively), and neither example — nor any other read for this
     * research pass — exercises a genuinely *successful* stem transform to
     * calibrate a broader rule against. Erring toward "does not transform"
     * degrades toward the conservative case (an unmerged pair scored by
     * treat-as-khắc) rather than fabricating a transform Rule C would not
     * license, the same posture this class takes for Lục Xung.
     */
    private static boolean danHoa(FiveElement resultElement, HeavenlyStem[] stems,
                                  EarthlyBranch[] branches, BranchResolution br, int i, int j) {
        FiveElement monthRuling = br.monthTransformedTo != null
                ? br.monthTransformedTo : branches[1].element();
        return monthRuling == resultElement;
    }

    // ------------------------------------------------------------------
    // Branch combination resolution (Tam Hoi / Tam Hop / Ban Tam Hop / Luc Hop / Luc Xung)
    // ------------------------------------------------------------------

    private record BranchGroup(Set<Integer> positions, FiveElement element, int lumpDegree, boolean transformed) {
    }

    private record BranchResolution(
            java.util.List<BranchGroup> groups,
            Set<Integer> khuBi,
            Set<Integer> inAnyRelation,
            Set<Integer> inLucHop,
            boolean unmitigatedXung,
            FiveElement monthTransformedTo
    ) {
    }

    private static BranchResolution resolveBranches(EarthlyBranch[] branches, HeavenlyStem[] stems) {
        java.util.List<BranchGroup> groups = new java.util.ArrayList<>();
        Set<Integer> consumed = new HashSet<>();
        Set<Integer> khuBi = new HashSet<>();
        Set<Integer> inAnyRelation = new HashSet<>();
        // Luc Hop specifically (not Tam Hoi/Tam Hop/Ban Tam Hop) gates the
        // truc dinh BOOST below - see its call site for why (Vi du 5 steps
        // 5 & 7, p.350: Tuat, in an attempted Luc Hop, keeps its plain ban
        // khi with no boost; Mui, in an attempted Ban Tam Hop, gets the
        // +6 boost despite that attempt also failing to hoa).
        Set<Integer> inLucHop = new HashSet<>();

        // Tam Hoi (precedence 1): full 3 distinct members, positions assigned
        // by backtracking so a repeated branch value (e.g. two Mao pillars in
        // the same chart) does not falsely satisfy a trio missing its third
        // member - see matchAll's Javadoc.
        for (Set<EarthlyBranch> group : BranchRelations.TAM_HOI) {
            Optional<Set<Integer>> positions = matchAll(branches, group, consumed);
            if (positions.isPresent()) {
                FiveElement element = BranchRelations.tamHoiElement(group);
                boolean hoa = anyStemHas(stems, element);
                groups.add(new BranchGroup(positions.get(), element, 72, hoa));
                if (!hoa) khuBi.addAll(positions.get());
                consumed.addAll(positions.get());
                inAnyRelation.addAll(positions.get());
            }
        }
        // Tam Hop (precedence 2): full 3 distinct members.
        for (Set<EarthlyBranch> group : BranchRelations.TAM_HOP) {
            Optional<Set<Integer>> positions = matchAll(branches, group, consumed);
            if (positions.isPresent()) {
                FiveElement element = BranchRelations.tamHopElement(group);
                boolean hoa = anyStemHas(stems, element);
                groups.add(new BranchGroup(positions.get(), element, 60, hoa));
                if (!hoa) khuBi.addAll(positions.get());
                consumed.addAll(positions.get());
                inAnyRelation.addAll(positions.get());
            }
        }
        // Ban Tam Hop (precedence 3): exactly 2 of the 3 members, and those
        // two ADJACENT - tried over every 2-of-3 subset and every position
        // assignment, so a duplicated branch (again, two Mao pillars) can
        // pair with whichever of its own occurrences is actually adjacent.
        for (Set<EarthlyBranch> group : BranchRelations.TAM_HOP) {
            Optional<Set<Integer>> positions = matchAnyTwoAdjacent(branches, group, consumed);
            if (positions.isPresent()) {
                FiveElement element = BranchRelations.tamHopElement(group);
                boolean hoa = anyStemHas(stems, element);
                groups.add(new BranchGroup(positions.get(), element, 40, hoa));
                if (!hoa) khuBi.addAll(positions.get());
                consumed.addAll(positions.get());
                inAnyRelation.addAll(positions.get());
            }
        }
        // Luc Hop (adjacent pairs only, not already consumed). Detected in
        // two passes: first find every valid adjacent pairing without
        // consuming anything, then check for a shared branch contested by
        // two pairings at once ("tranh hop" between branches - e.g. two Dau
        // pillars both adjacent to one Thin, Vi du 7) before committing any
        // of them, since a contested pairing transforms nothing and reverts
        // all three positions to khu bi rather than forming a group.
        Map<Integer, BranchRelations.LucHopPair> pairAt = new java.util.HashMap<>();
        for (int i = 0; i < 3; i++) {
            int j = i + 1;
            if (consumed.contains(i) || consumed.contains(j)) continue;
            for (BranchRelations.LucHopPair pair : BranchRelations.LUC_HOP) {
                if (pair.matches(branches[i], branches[j])) {
                    pairAt.put(i, pair);
                    break;
                }
            }
        }
        Set<Integer> contested = new HashSet<>();
        for (int i : pairAt.keySet()) {
            if (pairAt.containsKey(i - 1)) {
                // position i is the shared branch between pair (i-1,i) and (i,i+1)
                contested.add(i - 1);
                contested.add(i);
                contested.add(i + 1);
            }
        }
        if (!contested.isEmpty()) {
            khuBi.addAll(contested);
            inAnyRelation.addAll(contested);
            inLucHop.addAll(contested);
            consumed.addAll(contested);
        }
        for (Map.Entry<Integer, BranchRelations.LucHopPair> e : pairAt.entrySet()) {
            int i = e.getKey();
            int j = i + 1;
            if (contested.contains(i)) continue;
            BranchRelations.LucHopPair pair = e.getValue();
            Set<Integer> positions = Set.of(i, j);
            boolean hoa = anyStemHas(stems, pair.element());
            groups.add(new BranchGroup(positions, pair.element(), 36, hoa));
            if (!hoa) khuBi.addAll(positions);
            consumed.addAll(positions);
            inAnyRelation.addAll(positions);
            inLucHop.addAll(positions);
        }
        // Luc Xung (adjacent pairs only, exempt if either branch already in a relation - "tham hop quen xung")
        boolean unmitigatedXung = false;
        for (int i = 0; i < 3; i++) {
            int j = i + 1;
            if (inAnyRelation.contains(i) || inAnyRelation.contains(j)) continue;
            if (BranchRelations.isLucXung(branches[i], branches[j])) {
                unmitigatedXung = true;
            }
        }

        FiveElement monthTransformedTo = null;
        for (BranchGroup g : groups) {
            if (g.transformed && g.positions.contains(1)) {
                monthTransformedTo = g.element;
            }
        }

        return new BranchResolution(groups, khuBi, inAnyRelation, inLucHop, unmitigatedXung, monthTransformedTo);
    }

    /**
     * Finds a set of distinct, unconsumed positions covering every member of
     * {@code group} exactly once, by backtracking. Needed because a chart can
     * repeat a branch value across pillars (e.g. Ví dụ 5 has Mão at both the
     * year and day positions) — naive "does this position's branch belong to
     * the group" membership would let one repeated branch masquerade as two
     * different required members of a trio whose real third member is
     * absent. Returns empty if no complete assignment exists.
     */
    private static Optional<Set<Integer>> matchAll(EarthlyBranch[] branches, Set<EarthlyBranch> group,
                                                    Set<Integer> consumed) {
        return assign(new java.util.ArrayList<>(group), 0, branches, consumed, new HashSet<>());
    }

    private static Optional<Set<Integer>> assign(java.util.List<EarthlyBranch> members, int idx,
                                                  EarthlyBranch[] branches, Set<Integer> consumed,
                                                  Set<Integer> used) {
        if (idx == members.size()) {
            return Optional.of(Set.copyOf(used));
        }
        EarthlyBranch want = members.get(idx);
        for (int i = 0; i < 4; i++) {
            if (consumed.contains(i) || used.contains(i) || branches[i] != want) continue;
            used.add(i);
            Optional<Set<Integer>> result = assign(members, idx + 1, branches, consumed, used);
            if (result.isPresent()) return result;
            used.remove(i);
        }
        return Optional.empty();
    }

    /**
     * Bán Tam Hợp: try every 2-of-3 subset of the trio, and for each, every
     * position assignment (again allowing for a repeated branch value), and
     * accept the first pairing whose two positions are adjacent.
     */
    private static Optional<Set<Integer>> matchAnyTwoAdjacent(EarthlyBranch[] branches, Set<EarthlyBranch> group,
                                                               Set<Integer> consumed) {
        java.util.List<EarthlyBranch> members = new java.util.ArrayList<>(group);
        for (int skip = 0; skip < members.size(); skip++) {
            java.util.List<EarthlyBranch> pair = new java.util.ArrayList<>(members);
            pair.remove(skip);
            Optional<Set<Integer>> assigned = assignAnyAdjacent(pair, branches, consumed);
            if (assigned.isPresent()) return assigned;
        }
        return Optional.empty();
    }

    private static Optional<Set<Integer>> assignAnyAdjacent(java.util.List<EarthlyBranch> pair,
                                                             EarthlyBranch[] branches, Set<Integer> consumed) {
        EarthlyBranch first = pair.get(0);
        EarthlyBranch second = pair.get(1);
        for (int i = 0; i < 4; i++) {
            if (consumed.contains(i) || branches[i] != first) continue;
            for (int j = 0; j < 4; j++) {
                if (j == i || consumed.contains(j) || branches[j] != second) continue;
                if (Math.abs(i - j) == 1) {
                    return Optional.of(Set.of(i, j));
                }
            }
        }
        return Optional.empty();
    }

    private static boolean anyStemHas(HeavenlyStem[] stems, FiveElement element) {
        for (HeavenlyStem s : stems) {
            if (s.element() == element) return true;
        }
        return false;
    }
}
