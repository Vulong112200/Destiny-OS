import type { SupportedScenarioType } from "./types";

/**
 * Frontend mirror of `ScenarioRegistry`'s `displayNameVi` and `dimensions`
 * (destiny-scenario/src/main/java/io/destinyos/scenario/ScenarioRegistry.java).
 *
 * <p>Kept by hand, like `types.ts`, because the API does not expose a
 * scenario's relevant dimensions: `ScenarioRunResponse` carries `scenarioId`
 * and `policyDefined` and nothing else about the policy. Until it does, the
 * result page cannot tell which of the dimensions it is rendering the user
 * actually asked about — which is exactly why every dimension used to be
 * presented with identical weight, and why a "Sự nghiệp" run showed its
 * relationship analysis in the same typeface as its career analysis.
 *
 * <p>**These sets must not drift from the Java registry.** If a scenario's
 * `Set.of(Dimension...)` changes there, change it here in the same commit.
 */

/** `Dimension` enum names (destiny-core/.../signal/Dimension.java). */
export type DimensionName =
  | "FINANCE"
  | "CAREER"
  | "RELATIONSHIP"
  | "HEALTH_REFLECTION"
  | "TIMING"
  | "TRAVEL"
  | "DECISION"
  | "HOME"
  | "DAILY"
  | "OTHER";

/**
 * One narrower intent inside a scenario.
 *
 * <p>This is a **user-intent label, not a methodology switch.** Picking "Bạn
 * đời" instead of "Yêu đương" changes no pillar, no card, no hexagram and no
 * score — the traditional systems here do not distinguish them, and inventing
 * a distinction they do not make would violate Rule A. What it changes is
 * which authored text is surfaced first and how the question is phrased back
 * to the user, both of which are presentation. The UI says so where the user
 * picks one.
 */
export interface ScenarioFocus {
  id: string;
  label: string;
  /** Prefilled into the question box as a starting point the user can rewrite. */
  questionHint: string;
}

export interface ScenarioMeta {
  labelVi: string;
  /** One line under the scenario name in the picker. */
  blurb: string;
  /**
   * The scenario's `dimensions` set from `ScenarioRegistry`. Dimensions in
   * this set are what the user asked about; everything else a run produces is
   * real data but off-topic, and is presented as such rather than hidden.
   */
  relevantDimensions: DimensionName[];
  focuses: ScenarioFocus[];
}

export const SCENARIO_META: Record<SupportedScenarioType, ScenarioMeta> = {
  CAREER: {
    labelVi: "Sự nghiệp",
    blurb: "Công việc, thăng tiến, đổi nghề, đi hay ở",
    relevantDimensions: ["CAREER", "DECISION"],
    focuses: [
      { id: "doi-viec", label: "Đổi việc / nhảy việc", questionHint: "Tôi có nên đổi sang công việc mới lúc này không?" },
      { id: "thang-tien", label: "Thăng tiến", questionHint: "Con đường thăng tiến của tôi trong giai đoạn này thế nào?" },
      { id: "doi-nghe", label: "Chuyển hướng nghề", questionHint: "Tôi đang tính chuyển hẳn sang một nghề khác — nên cân nhắc điều gì?" },
      { id: "xung-dot", label: "Mâu thuẫn nơi làm việc", questionHint: "Tôi đang gặp căng thẳng với đồng nghiệp/cấp trên, nên xử lý thế nào?" },
    ],
  },
  RELATIONSHIP: {
    labelVi: "Tình cảm & quan hệ",
    blurb: "Yêu đương, bạn đời, gia đình, người xung quanh",
    relevantDimensions: ["RELATIONSHIP"],
    focuses: [
      { id: "yeu-duong", label: "Yêu đương", questionHint: "Chuyện tình cảm hiện tại của tôi đang đi về đâu?" },
      { id: "ban-doi", label: "Bạn đời / hôn nhân", questionHint: "Tôi có nên tiến tới lâu dài với người này không?" },
      { id: "doc-than", label: "Đang độc thân", questionHint: "Giai đoạn này của tôi có thuận cho một mối quan hệ mới không?" },
      { id: "gia-dinh", label: "Gia đình", questionHint: "Quan hệ trong gia đình tôi lúc này nên lưu ý điều gì?" },
      { id: "chia-tay", label: "Rạn nứt / chia tay", questionHint: "Mối quan hệ này đang rạn nứt — tôi nên nhìn nhận thế nào?" },
    ],
  },
  FINANCE: {
    labelVi: "Tài chính",
    blurb: "Tiền bạc, thu chi, rủi ro, khoản lớn",
    relevantDimensions: ["FINANCE", "DECISION"],
    focuses: [
      { id: "khoan-lon", label: "Một khoản chi lớn", questionHint: "Tôi có nên xuống tiền cho khoản này lúc này không?" },
      { id: "no-nan", label: "Nợ / dòng tiền", questionHint: "Tình hình dòng tiền của tôi giai đoạn này nên lưu ý gì?" },
      { id: "tich-luy", label: "Tích lũy dài hạn", questionHint: "Hướng tích lũy dài hạn của tôi có gì cần điều chỉnh?" },
    ],
  },
  BUSINESS: {
    labelVi: "Mở rộng kinh doanh",
    blurb: "Mở thêm, hợp tác, tái cấu trúc",
    relevantDimensions: ["FINANCE", "CAREER", "DECISION"],
    focuses: [
      { id: "mo-rong", label: "Mở rộng quy mô", questionHint: "Đây có phải lúc để mở rộng không?" },
      { id: "hop-tac", label: "Hợp tác / góp vốn", questionHint: "Tôi nên cân nhắc gì trước khi hợp tác với đối tác này?" },
      { id: "thu-hep", label: "Thu hẹp / dừng lại", questionHint: "Tôi có nên dừng hoặc thu hẹp mảng này không?" },
    ],
  },
  PROJECT: {
    labelVi: "Dự án",
    blurb: "Bắt đầu, tiếp tục hay dừng một việc cụ thể",
    relevantDimensions: ["FINANCE", "CAREER", "DECISION"],
    focuses: [
      { id: "khoi-dong", label: "Khởi động", questionHint: "Tôi có nên bắt đầu dự án này bây giờ không?" },
      { id: "tiep-tuc", label: "Tiếp tục hay dừng", questionHint: "Dự án đang dở dang — nên tiếp tục hay dừng?" },
      { id: "nhan-su", label: "Người cùng làm", questionHint: "Tôi nên lưu ý gì về những người cùng làm dự án này?" },
    ],
  },
  PURCHASE: {
    labelVi: "Mua sắm",
    blurb: "Nhà, xe, tài sản, món lớn",
    relevantDimensions: ["HOME", "FINANCE", "DECISION"],
    focuses: [
      { id: "nha-o", label: "Nhà / đất", questionHint: "Tôi có nên mua căn/lô này không?" },
      { id: "xe", label: "Xe cộ", questionHint: "Thời điểm này có thuận để mua xe không?" },
      { id: "tai-san-khac", label: "Tài sản khác", questionHint: "Tôi nên cân nhắc gì trước khi mua món này?" },
    ],
  },
  TRAVEL: {
    labelVi: "Di chuyển",
    blurb: "Chuyến đi, chuyển nhà, chuyển nơi ở",
    relevantDimensions: ["TRAVEL", "DECISION"],
    focuses: [
      { id: "chuyen-di", label: "Một chuyến đi", questionHint: "Chuyến đi này có gì cần lưu ý không?" },
      { id: "chuyen-nha", label: "Chuyển nhà / chuyển nơi ở", questionHint: "Tôi có nên chuyển tới nơi ở mới lúc này không?" },
      { id: "xa-xu", label: "Đi xa dài hạn", questionHint: "Tôi đang tính đi xa dài hạn — nên cân nhắc điều gì?" },
    ],
  },
  DAILY_ACTION: {
    labelVi: "Hôm nay nên làm gì",
    blurb: "Một ngày cụ thể, việc nên/không nên",
    relevantDimensions: ["DAILY", "TIMING"],
    focuses: [
      { id: "viec-hom-nay", label: "Việc trong ngày", questionHint: "Hôm nay tôi nên tập trung vào điều gì?" },
      { id: "thoi-diem", label: "Chọn thời điểm", questionHint: "Thời điểm này có thuận để làm việc đó không?" },
    ],
  },
  GENERAL_DECISION: {
    labelVi: "Quyết định chung",
    blurb: "Việc không nằm gọn trong các nhóm trên",
    relevantDimensions: ["DECISION", "OTHER"],
    focuses: [
      { id: "nga-re", label: "Đang ở ngã rẽ", questionHint: "Tôi đang phân vân giữa hai hướng — nên nhìn nhận thế nào?" },
      { id: "thoi-diem-chung", label: "Có nên làm lúc này", questionHint: "Đây có phải thời điểm thích hợp không?" },
    ],
  },
};

/** Scenario picker order: the common asks first, catch-all last. */
export const SCENARIO_ORDER: SupportedScenarioType[] = [
  "CAREER",
  "RELATIONSHIP",
  "FINANCE",
  "BUSINESS",
  "PROJECT",
  "PURCHASE",
  "TRAVEL",
  "DAILY_ACTION",
  "GENERAL_DECISION",
];

/** Vietnamese labels for `Dimension`, for the rare place one is shown without a `LabeledValue`. */
export const DIMENSION_LABELS: Record<DimensionName, string> = {
  FINANCE: "Tài chính",
  CAREER: "Sự nghiệp",
  RELATIONSHIP: "Quan hệ",
  HEALTH_REFLECTION: "Sức khỏe (tự phản tư)",
  TIMING: "Thời điểm",
  TRAVEL: "Di chuyển",
  DECISION: "Quyết định",
  HOME: "Nhà cửa",
  DAILY: "Trong ngày",
  OTHER: "Khác",
};

/** True when `dimension` is one the chosen scenario actually asked about. */
export function isRelevantDimension(
  scenario: SupportedScenarioType | null,
  dimension: string,
): boolean {
  if (scenario === null) return false;
  return (SCENARIO_META[scenario]?.relevantDimensions as string[] | undefined)?.includes(dimension) ?? false;
}

/**
 * Resolves a scenario id that arrived from the API as a plain string.
 * Returns null for `COMPATIBILITY` or anything unrecognised, so callers fall
 * back to unfocused rendering rather than crashing on an unknown scenario.
 */
export function asSupportedScenario(scenarioId: string): SupportedScenarioType | null {
  return scenarioId in SCENARIO_META ? (scenarioId as SupportedScenarioType) : null;
}

/**
 * Which authored Tarot meaning matches this scenario.
 *
 * `TarotCardMeaning` authors five fields (career/finance/relationship/
 * decision/general) and `TarotEngine` ships all five in `fact.meaning`;
 * nothing server-side picks one. This is that pick, made where the scenario
 * is known.
 */
export function tarotMeaningKeyFor(
  scenario: SupportedScenarioType | null,
): "career" | "finance" | "relationship" | "decision" | "general" {
  switch (scenario) {
    case "CAREER":
      return "career";
    case "RELATIONSHIP":
      return "relationship";
    case "FINANCE":
      return "finance";
    // Business/Project/Purchase are money-and-choice shaped; their registry
    // dimension sets lead with FINANCE, so the finance reading is the closest
    // authored match rather than the generic one.
    case "BUSINESS":
    case "PROJECT":
    case "PURCHASE":
      return "finance";
    case "TRAVEL":
    case "DAILY_ACTION":
    case "GENERAL_DECISION":
      return "decision";
    default:
      return "general";
  }
}
