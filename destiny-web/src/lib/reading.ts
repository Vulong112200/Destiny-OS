import type { EvidenceDto, LabeledValue, ScenarioRunResponse, SignalDto } from "./types";
import { tarotMeaningKeyFor } from "./scenarioMeta";
import type { SupportedScenarioType } from "./types";
import { tarotCardNameVi, tarotPositionLabelVi } from "./tarotCards";

/**
 * One readable statement behind one signal: what the system found, in which
 * dimension, and the authored Vietnamese text that says what it means.
 *
 * <p>This exists because the result page had the interpretive text all along
 * and never showed it *as* the reading. `TarotEngine` and `NumerologyEngine`
 * both ship authored prose inside `EvidenceDto.fact.meaning`, and `SignalDto`
 * already points at the evidence it came from via `evidenceIds` — but the
 * page rendered the two separately: a Tarot card block listing all five of
 * its authored meanings undifferentiated, and, elsewhere, a dimension row
 * saying only which engines supported it. A user asking about sự nghiệp got
 * "TAROT · Sự nghiệp · Thuận lợi" with the sentence explaining why sitting in
 * a different section under a heading about something else.
 *
 * <p>Joining them here is not new interpretation — every field below is
 * copied from data the backend already produced. Nothing is generated.
 */
export interface ReadingItem {
  signalId: string;
  engine: string;
  /** `Dimension` technical name, e.g. `"CAREER"`. */
  dimension: string;
  dimensionLabel: string;
  polarity: LabeledValue;
  strength: LabeledValue;
  critical: boolean;
  /** What produced it, in the user's terms — a card name, a number, a direction. */
  title: string;
  /** The authored interpretation, or null when this engine authors none. */
  text: string | null;
  keywords: string[];
  /** Set when the engine emitted no authored prose, so the UI can say why. */
  noTextReason: string | null;
}

function asRecord(value: unknown): Record<string, unknown> | null {
  return value !== null && typeof value === "object" && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : null;
}

function asStringArray(value: unknown): string[] {
  return Array.isArray(value) ? value.filter((v): v is string => typeof v === "string") : [];
}

function nonEmptyString(value: unknown): string | null {
  return typeof value === "string" && value.trim() !== "" ? value : null;
}

/**
 * The label a user would recognise for the thing that produced a signal.
 * Falls back to the rule id rather than inventing a name.
 */
function titleFor(evidence: EvidenceDto): string {
  const fact = evidence.fact ?? {};

  if (evidence.engine === "TAROT") {
    const cardId = nonEmptyString(fact.cardId);
    const cardName = nonEmptyString(fact.cardName);
    const name = cardId ? tarotCardNameVi(cardId, cardName ?? cardId) : cardName ?? "Lá bài";
    const reversed = fact.orientation === "REVERSED";
    const position = nonEmptyString(fact.position);
    const positionLabel = position ? tarotPositionLabelVi(position) : null;
    const orientation = reversed ? " (ngược)" : "";
    return positionLabel ? `${name}${orientation} — ${positionLabel}` : `${name}${orientation}`;
  }

  if (evidence.engine === "NUMEROLOGY_PYTHAGOREAN") {
    const value = fact.value;
    const numberLabel = NUMEROLOGY_RULE_LABELS[evidence.ruleId] ?? "Chỉ số";
    return typeof value === "number" ? `${numberLabel} — số ${value}` : numberLabel;
  }

  if (evidence.engine === "FENGSHUI_KUA") {
    const kua = fact.kuaNumber;
    return typeof kua === "number" ? `Cung phi số ${kua}` : "Bát Trạch";
  }

  return evidence.ruleId;
}

const NUMEROLOGY_RULE_LABELS: Record<string, string> = {
  NUMEROLOGY_LIFE_PATH: "Số Đường Đời",
  NUMEROLOGY_EXPRESSION: "Số Sứ Mệnh",
  NUMEROLOGY_SOUL_URGE: "Số Linh Hồn",
  NUMEROLOGY_PERSONALITY: "Số Nhân Cách",
  NUMEROLOGY_BIRTHDAY: "Số Ngày Sinh",
};

/**
 * Extracts the authored text one piece of evidence carries for `dimension`.
 *
 * Tarot authors five dimension-specific texts and ships all five; the
 * dimension of the signal selects which one applies. Numerology authors one
 * text per number. Every other engine authors none today — which is reported
 * as a reason rather than as an empty string, since "this system does not yet
 * have written interpretations" and "this system found nothing to say" are
 * different statements and only the first is true.
 */
function textFor(
  evidence: EvidenceDto,
  dimension: string,
  scenario: SupportedScenarioType | null,
): { text: string | null; keywords: string[]; noTextReason: string | null } {
  const meaning = asRecord(evidence.fact?.meaning);

  if (evidence.engine === "TAROT") {
    if (meaning === null) {
      return {
        text: null,
        keywords: [],
        noTextReason: "Lá này chưa có bản diễn giải tiếng Việt được biên soạn.",
      };
    }
    // The signal's own dimension is the authoritative pick; the scenario is
    // only the tiebreak for OTHER-dimension (general) signals.
    const byDimension: Record<string, string> = {
      CAREER: "career",
      FINANCE: "finance",
      RELATIONSHIP: "relationship",
      DECISION: "decision",
    };
    const key = byDimension[dimension] ?? tarotMeaningKeyFor(scenario);
    const reversed = evidence.fact?.orientation === "REVERSED";
    return {
      text: nonEmptyString(meaning[key]) ?? nonEmptyString(meaning.general),
      keywords: asStringArray(reversed ? meaning.reversedKeywords : meaning.uprightKeywords),
      noTextReason: null,
    };
  }

  if (evidence.engine === "NUMEROLOGY_PYTHAGOREAN") {
    if (meaning === null) {
      return {
        text: null,
        keywords: [],
        noTextReason: "Cặp (chỉ số, giá trị) này chưa có nội dung được biên soạn.",
      };
    }
    return {
      text: nonEmptyString(meaning.text),
      keywords: asStringArray(meaning.keywords),
      noTextReason: null,
    };
  }

  if (evidence.engine === "FENGSHUI_KUA") {
    return {
      text: null,
      keywords: [],
      noTextReason:
        "Bát Trạch hiện phát tín hiệu từ bảng tra hướng, chưa có đoạn diễn giải được biên soạn kèm theo.",
    };
  }

  return {
    text: null,
    keywords: [],
    noTextReason: "Hệ này hiện chỉ cung cấp dữ liệu tính toán, chưa có phần luận giải.",
  };
}

/**
 * Builds the readable layer for a whole run: every signal, paired with the
 * authored text of the evidence it was derived from.
 *
 * A signal with several evidence ids takes the first one that yields text —
 * signals are one-per-meaning-field in the engines that author text, so this
 * is a single evidence record in practice, and taking the first is not a
 * silent choice between competing readings.
 */
export function buildReading(
  result: ScenarioRunResponse,
  scenario: SupportedScenarioType | null,
): ReadingItem[] {
  const byId = new Map<string, EvidenceDto>(result.evidence.map((e) => [e.evidenceId, e]));

  return result.signals.map((signal: SignalDto) => {
    const sources = signal.evidenceIds
      .map((id) => byId.get(id))
      .filter((e): e is EvidenceDto => e !== undefined);

    let title = signal.engine;
    let text: string | null = null;
    let keywords: string[] = [];
    let noTextReason: string | null = null;

    for (const evidence of sources) {
      const resolved = textFor(evidence, signal.dimension.technical, scenario);
      if (title === signal.engine) title = titleFor(evidence);
      if (resolved.text !== null) {
        text = resolved.text;
        keywords = resolved.keywords;
        noTextReason = null;
        break;
      }
      noTextReason = noTextReason ?? resolved.noTextReason;
      keywords = keywords.length > 0 ? keywords : resolved.keywords;
    }

    return {
      signalId: signal.signalId,
      engine: signal.engine,
      dimension: signal.dimension.technical,
      dimensionLabel: signal.dimension.labelVi,
      polarity: signal.polarity,
      strength: signal.strength,
      critical: signal.critical,
      title,
      text,
      keywords,
      noTextReason,
    };
  });
}

/** Groups reading items by dimension, preserving each dimension's first-seen order. */
export function groupByDimension(items: ReadingItem[]): Map<string, ReadingItem[]> {
  const grouped = new Map<string, ReadingItem[]>();
  for (const item of items) {
    const existing = grouped.get(item.dimension);
    if (existing) {
      existing.push(item);
    } else {
      grouped.set(item.dimension, [item]);
    }
  }
  return grouped;
}

/** Strongest first, so a dimension leads with the item that most drove its verdict. */
const STRENGTH_RANK: Record<string, number> = { STRONG: 0, MEDIUM: 1, WEAK: 2 };

export function sortByWeight(items: ReadingItem[]): ReadingItem[] {
  return [...items].sort((a, b) => {
    if (a.critical !== b.critical) return a.critical ? -1 : 1;
    const byStrength =
      (STRENGTH_RANK[a.strength.technical] ?? 3) - (STRENGTH_RANK[b.strength.technical] ?? 3);
    if (byStrength !== 0) return byStrength;
    // Items with authored text before items without, so a dimension does not
    // open with a row that has nothing to say.
    if ((a.text === null) !== (b.text === null)) return a.text === null ? 1 : -1;
    return 0;
  });
}
