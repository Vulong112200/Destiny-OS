/**
 * TypeScript mirror of destiny-api's DTOs (destiny-api/src/main/java/io/destinyos/api/dto).
 * Field names and nesting match exactly - keep this file in sync by hand
 * whenever a DTO changes, there is no shared schema generator yet.
 */

/** A technical enum value paired with its Vietnamese label. Never render `technical` alone. */
export interface LabeledValue {
  technical: string;
  labelVi: string;
}

export interface EngineOutcomeDto {
  engine: string;
  status: LabeledValue;
  timedOut: boolean;
  durationMs: number;
}

export interface EvidenceDto {
  evidenceId: string;
  engine: string;
  school: string | null;
  ruleId: string;
  ruleVersion: string;
  dimension: LabeledValue | null;
  fact: Record<string, unknown>;
  source: string | null;
}

export interface SignalDto {
  signalId: string;
  engine: string;
  school: string | null;
  dimension: LabeledValue;
  tag: string;
  polarity: LabeledValue;
  strength: LabeledValue;
  applicability: LabeledValue;
  critical: boolean;
  evidenceIds: string[];
}

export interface DimensionResultDto {
  dimension: LabeledValue;
  state: LabeledValue;
  supportingEngines: string[];
  cautionEngines: string[];
  negativeEngines: string[];
  rulesApplied: string[];
}

export interface ConflictDto {
  type: LabeledValue;
  dimension: LabeledValue | null;
  involvedEngines: string[];
  description: string;
}

export interface FusionResultDto {
  overallOutcome: LabeledValue;
  dimensions: DimensionResultDto[];
  conflicts: ConflictDto[];
  rulesApplied: string[];
  supportingSources: string[];
  cautionSources: string[];
}

/**
 * How long this result will be kept (CLAUDE.md section 7).
 *
 * `expiresAt` being null is a positive statement — "not scheduled for
 * deletion" — not a missing field, which is why the UI branches on it rather
 * than rendering an empty date.
 */
export interface RetentionDto {
  retentionClass: LabeledValue;
  /** ISO instant, or null for "never expires". */
  expiresAt: string | null;
  /** False once the result is already kept indefinitely, so the save button hides. */
  canBeSaved: boolean;
}

export interface ScenarioRunResponse {
  calculationId: string;
  scenarioId: string;
  policyDefined: boolean;
  engines: EngineOutcomeDto[];
  unavailableEngines: string[];
  evidence: EvidenceDto[];
  signals: SignalDto[];
  fusion: FusionResultDto | null;
  resultHash: string;
  retention: RetentionDto;
}

export interface MethodologyDto {
  methodologyId: string;
  displayNameVi: string;
  domain: string | null;
  version: string | null;
  status: LabeledValue | null;
  calculable: boolean;
  school: string | null;
  source: string | null;
  researchIds: string[];
  notes: string | null;
}

export interface ErrorResponse {
  code: string;
  message: string;
}

/**
 * The scenarios with a real applicability policy (ScenarioRegistry) —
 * everything except COMPATIBILITY, which stays undefined because its
 * strongest traditional evidence (Bát Tự hợp hôn, Tử Vi xem tuổi, Chiêm tinh
 * synastry) needs two charts and this system takes one. Extended from
 * {BUSINESS, DAILY_ACTION} to the full set on 2026-08-23
 * (docs/DECISION_LOG.md).
 */
export type SupportedScenarioType =
  | "BUSINESS"
  | "DAILY_ACTION"
  | "CAREER"
  | "FINANCE"
  | "RELATIONSHIP"
  | "PURCHASE"
  | "TRAVEL"
  | "PROJECT"
  | "GENERAL_DECISION";

export interface NumerologyRequestInput {
  fullName: string;
  /** ISO date, e.g. "1990-05-15". */
  birthDate: string;
}

export type TarotSpreadName = "PAST_PRESENT_FUTURE" | "CHOICE_A_B" | "SITUATION_CHALLENGE_ADVICE";

export interface TarotRequestInput {
  spread: TarotSpreadName;
  seed: number | null;
  question: string | null;
}

export interface BaziRequestInput {
  /** ISO date, e.g. "1984-02-05". */
  birthDate: string;
  /**
   * ISO local time, e.g. "07:30", or null if not known.
   *
   * Null is a real answer, not a missing field: Master Spec section 2 forbids
   * treating an unknown birth time as exact, so the backend returns the year
   * and month pillars only rather than inventing an hour. Sending "00:00" to
   * fill the shape would be the worst possible invention - midnight sits
   * inside the Gio Ty window whose 23:00 boundary rolls the day pillar over.
   */
  birthTime: string | null;
  /** "NORTH" | "SOUTH" | "UNKNOWN". Send UNKNOWN rather than guessing. */
  region: string | null;
  /** Degrees east; enables the mean-solar-time correction (R10). */
  longitude: number | null;
  /**
   * "MALE" | "FEMALE", or null if not supplied.
   *
   * Only the Đại Vận direction depends on it (R2). Null costs the luck cycles
   * and nothing else — the Tứ Trụ comes back in full, with a stated reason for
   * the missing section. Unlike Phong Thủy, where gender gates the whole
   * result, so never copy the default from there: there is no defensible
   * default here, and a guessed direction runs the entire sequence backwards
   * while looking correct.
   */
  gender: string | null;
}

export type CompassDirectionName =
  | "NORTH"
  | "NORTHEAST"
  | "EAST"
  | "SOUTHEAST"
  | "SOUTH"
  | "SOUTHWEST"
  | "WEST"
  | "NORTHWEST";

export interface FengShuiRequestInput {
  /** ISO date, e.g. "1990-08-20". */
  birthDate: string;
  /** ISO local time, or null. Only decisive within minutes of the Lập Xuân instant. */
  birthTime: string | null;
  /**
   * "MALE" or "FEMALE". Required, with no default: the male and female Kua
   * formulas differ and are not symmetric, so any default would hand half of
   * users someone else's Kua number.
   */
  gender: "MALE" | "FEMALE";
  /** "NORTH" | "SOUTH" | "UNKNOWN". */
  region: string | null;
  longitude: number | null;
  /**
   * The direction of the house or room being assessed, or null.
   *
   * Null is meaningful: Bát Trạch judges a person against a direction, so with
   * none supplied the backend returns the eight-direction profile and no signal
   * rather than inventing something to be favourable about.
   */
  facingDirection: CompassDirectionName | null;
}

/**
 * Unlike Bát Tự and Phong Thủy, every field here is required — the backend
 * task factory declines to run rather than guess a missing one. The
 * Ascendant moves roughly 1 degree every 4 minutes, so a chart built on a
 * guessed time or place would be confidently wrong, not degraded, the way
 * an hourless Bát Tự chart still is.
 *
 * `birthDate`/`birthTime` are read as Vietnam civil time on the backend
 * (`AstrologyTaskFactory`), even though `latitudeDegrees`/`longitudeDegrees`
 * can name any place on Earth - a stated limitation, not a hidden one, for a
 * user entering a foreign birthplace's own local time.
 */
export interface AstrologyRequestInput {
  /** ISO date, e.g. "1994-06-17". */
  birthDate: string;
  /** ISO local time, e.g. "01:00". Required - see the interface note above. */
  birthTime: string;
  /** Positive north, in [-90, 90]. */
  latitudeDegrees: number;
  /** Positive east, in [-180, 180]. */
  longitudeDegrees: number;
}

export interface ScenarioRunRequestInput {
  numerology: NumerologyRequestInput | null;
  tarot: TarotRequestInput | null;
  bazi: BaziRequestInput | null;
  fengShui: FengShuiRequestInput | null;
  astrology: AstrologyRequestInput | null;
}

/**
 * Vietnamese labels from `GET /api/v1/labels`, keyed by enum type then by
 * technical name — e.g. `labels.HeavenlyStem.GIAP === "Giáp"`.
 *
 * Needed because a Bát Tự chart arrives inside `EvidenceDto.fact`, which is a
 * free-form map of technical names rather than `LabeledValue` pairs. Every
 * other enum in this API travels pre-labelled; these do not, and
 * UI_UX_VIETNAMESE_SPEC section 1 still forbids showing the raw name.
 */
export type LabelRegistries = Record<string, Record<string, string>>;
