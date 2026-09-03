"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { runScenario } from "@/lib/api";
import { describeApiError } from "@/lib/labels";
import { UNBUILT_SYSTEMS } from "@/lib/systemInventory";
import { pushLog } from "@/lib/logBuffer";
import { findVnProvince, VN_PROVINCES } from "@/lib/vnProvinces";
import { SCENARIO_META } from "@/lib/scenarioMeta";
import { ScenarioPicker } from "./ScenarioPicker";
import { TarotRitual } from "./tarot/TarotRitual";
// Giá trị, không phải kiểu — nên tách khỏi khối `import type` bên dưới.
import { TAROT_MAX_CARDS } from "@/lib/types";
import type {
  CompassDirectionName,
  IChingCastingMethod,
  SupportedScenarioType,
  TarotSpreadName,
} from "@/lib/types";

const ICHING_METHOD_OPTIONS: { value: IChingCastingMethod; label: string }[] = [
  { value: "THREE_COINS", label: "Tam Tiền (rút xu, nhanh)" },
  { value: "YARROW", label: "Thi Thảo (cỏ thi, cổ điển)" },
  { value: "MAI_HOA_NUMBER", label: "Mai Hoa — theo 2 con số" },
  { value: "MAI_HOA_TIME", label: "Mai Hoa — theo thời điểm hiện tại" },
];

/**
 * `cardCount: null` nghĩa là spread không có số lá của riêng nó — chỉ `FREE_FORM`,
 * và người dùng phải tự chọn.
 */
const SPREAD_OPTIONS: {
  value: TarotSpreadName;
  label: string;
  cardCount: number | null;
  note?: string;
}[] = [
  { value: "PAST_PRESENT_FUTURE", label: "Quá khứ – Hiện tại – Tương lai", cardCount: 3 },
  { value: "CHOICE_A_B", label: "Lựa chọn A – B", cardCount: 2 },
  { value: "SITUATION_CHALLENGE_ADVICE", label: "Tình huống – Thử thách – Lời khuyên", cardCount: 3 },
  { value: "HORSESHOE_FIVE", label: "Móng ngựa", cardCount: 5 },
  { value: "CELTIC_CROSS", label: "Thập tự Celtic", cardCount: 10 },
  {
    value: "FREE_FORM",
    label: "Tự do — bạn chọn số lá",
    cardCount: null,
    note: "Không gán ý nghĩa cho vị trí nào cả.",
  },
];

function spreadMeta(value: TarotSpreadName) {
  return SPREAD_OPTIONS.find((o) => o.value === value) ?? SPREAD_OPTIONS[0];
}

/**
 * Region matters only for births between 1955 and 1975, when North and South
 * ran different UTC offsets. "Chưa rõ" is the default and a legitimate answer:
 * research item R14b found no source for the geographic boundary, so a birth in
 * that window with an unknown region comes back unresolvable — which is the
 * honest result, and better than making the user pick a side to get a number.
 */
const REGION_OPTIONS: { value: string; label: string }[] = [
  { value: "UNKNOWN", label: "Chưa rõ / không áp dụng" },
  { value: "NORTH", label: "Miền Bắc" },
  { value: "SOUTH", label: "Miền Nam" },
];

/**
 * Compass order, not best-to-worst: which direction is favourable depends on the
 * person's Kua, so any fixed ordering by desirability would be wrong for most
 * users.
 */
const DIRECTION_OPTIONS: { value: CompassDirectionName; label: string }[] = [
  { value: "NORTH", label: "Bắc" },
  { value: "NORTHEAST", label: "Đông Bắc" },
  { value: "EAST", label: "Đông" },
  { value: "SOUTHEAST", label: "Đông Nam" },
  { value: "SOUTH", label: "Nam" },
  { value: "SOUTHWEST", label: "Tây Nam" },
  { value: "WEST", label: "Tây" },
  { value: "NORTHWEST", label: "Tây Bắc" },
];

/**
 * The Decision Center intake form (UI_UX_VIETNAMESE_SPEC section 3, first
 * three steps: chọn chủ đề -> nhập câu hỏi/context -> hệ thống áp dụng).
 * Offers every {@link SupportedScenarioType} — every scenario with a real
 * applicability policy (ScenarioRegistry) except COMPATIBILITY, which stays
 * undefined because its strongest evidence needs two charts and this system
 * takes one. Presenting COMPATIBILITY here would mean a request that always
 * comes back "policyDefined: false", which does not belong on the primary
 * intake form.
 *
 * Birth info (name/date/time/place/gender/region) is entered ONCE in a
 * shared "Thông tin cá nhân" section and reused to build every engine's
 * request payload below — Thần số học, Bát Tự, Phong Thủy and Chiêm tinh
 * all describe the same person, so asking for the same four fields four
 * times was pure friction, not a real difference in what each engine needs.
 * Latitude/longitude (only Chiêm tinh and Bát Tự's solar-time correction use
 * them) come from a tỉnh/thành picker rather than raw numbers, since almost
 * nobody knows the coordinates of the hospital they were born in — an
 * "advanced" toggle still accepts exact coordinates for anyone who has them.
 */
export function DecisionCenterForm() {
  const router = useRouter();
  // Defaults to the most-asked topic rather than BUSINESS, which was first
  // only because it was one of the two scenarios that had a policy at the
  // time. All nine have had one since 2026-08-23.
  const [scenarioType, setScenarioType] = useState<SupportedScenarioType>("CAREER");
  const [focusId, setFocusId] = useState("");

  // --- Thông tin cá nhân (dùng chung cho mọi hệ) ---
  const [fullName, setFullName] = useState("");
  const [birthDate, setBirthDate] = useState("");
  const [birthTime, setBirthTime] = useState("");
  const [gender, setGender] = useState<"" | "MALE" | "FEMALE">("");
  const [region, setRegion] = useState("UNKNOWN");
  const [provinceId, setProvinceId] = useState("");
  const [useCustomCoords, setUseCustomCoords] = useState(false);
  const [customLatitude, setCustomLatitude] = useState("");
  const [customLongitude, setCustomLongitude] = useState("");

  const province = provinceId === "" ? null : findVnProvince(provinceId);
  const effectiveLatitude = useCustomCoords
    ? (customLatitude.trim() === "" ? null : Number(customLatitude))
    : province?.latitude ?? null;
  const effectiveLongitude = useCustomCoords
    ? (customLongitude.trim() === "" ? null : Number(customLongitude))
    : province?.longitude ?? null;

  // --- Từng hệ: chỉ còn field đặc thù của hệ đó ---
  const [useTarot, setUseTarot] = useState(true);
  const [spread, setSpread] = useState<TarotSpreadName>("PAST_PRESENT_FUTURE");
  const [freeFormCount, setFreeFormCount] = useState(3);
  const [pickedPositions, setPickedPositions] = useState<number[]>([]);
  const [pickCardsMyself, setPickCardsMyself] = useState(false);

  // Số lá lượt bốc này sẽ lật. FREE_FORM lấy từ người dùng, còn lại là thuộc
  // tính của spread — nên chỉ có một chỗ tính, không nhân đôi giữa form và payload.
  const tarotCardsNeeded = spreadMeta(spread).cardCount ?? freeFormCount;
  // Lifted out of the Tarot section: the question is about the whole run, and
  // burying it there meant a user who did not enable Tarot was never asked
  // what they wanted to know. UI_UX_VIETNAMESE_SPEC §3 always had it as its
  // own step in the flow.
  const [question, setQuestion] = useState("");
  const [useBazi, setUseBazi] = useState(false);
  const [useFengShui, setUseFengShui] = useState(false);
  const [fsFacing, setFsFacing] = useState<CompassDirectionName | "">("");
  const [useAstrology, setUseAstrology] = useState(false);
  const [useIChing, setUseIChing] = useState(false);
  const [ichingMethod, setIChingMethod] = useState<IChingCastingMethod>("THREE_COINS");
  const [ichingUpperNumber, setIChingUpperNumber] = useState("");
  const [ichingLowerNumber, setIChingLowerNumber] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /**
   * Thần số học từng là hệ duy nhất **không có ô chọn**: nó tự bật khi người
   * dùng nhập đủ họ tên và ngày sinh. Đúng về hành vi, nhưng nó khiến mục "Hệ
   * thống áp dụng" chỉ đếm được 5 ô trong khi backend có 6 engine — người dùng
   * không có cách nào biết hệ thứ sáu tồn tại, chứ đừng nói tắt nó đi.
   */
  const [useNumerology, setUseNumerology] = useState(true);
  const numerologyDataReady = fullName.trim() !== "" && birthDate !== "";
  const hasNumerology = useNumerology && numerologyDataReady;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);

    const hasBazi = useBazi && birthDate !== "";
    const hasFengShui = useFengShui && birthDate !== "" && gender !== "";
    const hasAstrology =
      useAstrology && birthDate !== "" && birthTime !== "" && effectiveLatitude !== null && effectiveLongitude !== null;

    if (useBazi && birthDate === "") {
      setError("Bát Tự cần ngày sinh (ở mục Thông tin cá nhân). Giờ sinh có thể để trống.");
      return;
    }
    if (useFengShui && (birthDate === "" || gender === "")) {
      setError("Bát Trạch cần ngày sinh và giới tính (ở mục Thông tin cá nhân) để tính cung phi.");
      return;
    }
    if (useAstrology && (birthDate === "" || birthTime === "")) {
      setError(
        "Chiêm tinh phương Tây cần đủ ngày sinh và giờ sinh chính xác (ở mục Thông tin cá nhân) — không thể lập lá số nếu thiếu.",
      );
      return;
    }
    if (useAstrology && (effectiveLatitude === null || effectiveLongitude === null)) {
      setError(
        "Chiêm tinh phương Tây cần nơi sinh — chọn tỉnh/thành ở mục Thông tin cá nhân, hoặc nhập tọa độ chính xác.",
      );
      return;
    }
    if (useAstrology && (!Number.isFinite(effectiveLatitude) || !Number.isFinite(effectiveLongitude))) {
      setError("Tọa độ chính xác phải là số, ví dụ 10.8 và 106.7.");
      return;
    }
    const hasIChingNumbers = ichingUpperNumber.trim() !== "" && ichingLowerNumber.trim() !== "";
    if (useIChing && ichingMethod === "MAI_HOA_NUMBER" && !hasIChingNumbers) {
      setError("Mai Hoa theo Số cần đủ 2 con số — không nhận 1 số duy nhất.");
      return;
    }
    const hasIChing = useIChing;
    if (!hasNumerology && !useTarot && !hasBazi && !hasFengShui && !hasAstrology && !hasIChing) {
      setError(
        "Cần bật ít nhất một hệ thống ở mục 4, và điền đủ dữ liệu hệ đó cần.",
      );
      return;
    }

    setSubmitting(true);
    try {
      const focus = SCENARIO_META[scenarioType].focuses.find((f) => f.id === focusId) ?? null;
      const trimmedQuestion = question.trim();
      const result = await runScenario(scenarioType, {
        context: {
          question: trimmedQuestion === "" ? null : trimmedQuestion,
          focusId: focus?.id ?? null,
          focusLabel: focus?.label ?? null,
        },
        numerology: hasNumerology ? { fullName: fullName.trim(), birthDate } : null,
        // Still sent on the Tarot payload as well: `TarotRequest.question`
        // predates the request-level context and other callers may rely on it.
        tarot: useTarot
          ? {
              spread,
              seed: null,
              question: trimmedQuestion || null,
              // Chỉ FREE_FORM cần số lá; gửi kèm cho spread khác thì backend bỏ
              // qua, nhưng gửi null cho đúng ý nghĩa.
              cardCount: spread === "FREE_FORM" ? freeFormCount : null,
              // Chỉ gửi khi người dùng thực sự đã chọn đủ. Gửi một danh sách
              // thiếu sẽ bị backend từ chối kèm lý do — đúng, nhưng ở đây ta
              // biết trước nên không bắt họ đi một vòng lỗi.
              pickedPositions:
                pickCardsMyself && pickedPositions.length === tarotCardsNeeded
                  ? pickedPositions
                  : null,
            }
          : null,
        bazi: hasBazi
          ? {
              birthDate,
              // Empty means "not known", never a stand-in hour: the backend
              // returns two pillars and says so, rather than treating an
              // unknown time as exact (Master Spec section 2).
              birthTime: birthTime === "" ? null : birthTime,
              region,
              longitude: effectiveLongitude,
              // Same rule as the hour: empty means "not supplied", and the
              // backend omits Đại Vận with a stated reason rather than
              // picking a direction.
              gender: gender === "" ? null : gender,
            }
          : null,
        fengShui: hasFengShui
          ? {
              birthDate,
              birthTime: null,
              gender: gender as "MALE" | "FEMALE",
              region,
              longitude: effectiveLongitude,
              // Empty means "no direction to assess" - the backend then returns
              // the eight-direction profile and no signal, rather than judging
              // a direction the user never gave.
              facingDirection: fsFacing === "" ? null : fsFacing,
            }
          : null,
        astrology: hasAstrology
          ? {
              birthDate,
              birthTime,
              latitudeDegrees: effectiveLatitude as number,
              longitudeDegrees: effectiveLongitude as number,
            }
          : null,
        iching: hasIChing
          ? {
              method: ichingMethod,
              seed: null,
              upperNumber: ichingMethod === "MAI_HOA_NUMBER" ? Number(ichingUpperNumber) : null,
              lowerNumber: ichingMethod === "MAI_HOA_NUMBER" ? Number(ichingLowerNumber) : null,
            }
          : null,
      });
      router.push(`/ket-qua/${result.calculationId}`);
    } catch (err) {
      // Trước đây chỗ này là `err instanceof ApiError ? err.message : "..."`,
      // vứt sạch cả mã HTTP lẫn mã lỗi. Nên một lần hết hạn chờ hiện ra đúng
      // một câu, không có gì để lần ra, và người dùng không có cách nào báo
      // lại cho ai chuyện gì đã xảy ra.
      const { message, code, status } = describeApiError(err);
      pushLog({
        level: "error",
        origin: "client",
        kind: "action",
        message,
        code: code ?? undefined,
        status: status ?? undefined,
        detail: {
          scenarioType,
          // Chỉ tên các hệ được bật. Không bao giờ là nội dung người dùng nhập.
          enginesRequested: [
            hasNumerology ? "NUMEROLOGY_PYTHAGOREAN" : null,
            useTarot ? "TAROT" : null,
            useBazi ? "BAZI" : null,
            useFengShui ? "FENGSHUI_KUA" : null,
            useAstrology ? "WESTERN_ASTROLOGY" : null,
            useIChing ? "ICHING" : null,
          ].filter(Boolean),
        },
      });
      setError(
        [message, code ? `mã lỗi: ${code}` : null, status ? `HTTP ${status}` : null]
          .filter(Boolean)
          .join(" · "),
      );
    } finally {
      setSubmitting(false);
    }
  }

  const showRegion = useMemo(() => useBazi || useFengShui, [useBazi, useFengShui]);
  const needsPreciseLocation = useAstrology;

  return (
    <form onSubmit={handleSubmit} className="space-y-8">
      <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm lg:p-8">
        <ScenarioPicker
          scenarioType={scenarioType}
          onScenarioChange={setScenarioType}
          focusId={focusId}
          onFocusChange={setFocusId}
          question={question}
          onQuestionChange={setQuestion}
        />
      </section>

      {/*
        Everything below is *how* to compute, not *what* is being asked. Two
        columns from `lg` up: the person on the left, the systems on the right.
        These used to be six full-width fieldsets stacked one after another,
        which is what made the page feel endless - the engine toggles sat a
        full screen below the birth fields they depend on, so a user fixing a
        validation error scrolled between the two repeatedly.
      */}
      <div className="grid items-start gap-6 lg:grid-cols-2">
      <fieldset className="space-y-3 rounded-lg border border-slate-200 bg-white p-4">
        <legend className="px-1 text-sm font-semibold text-slate-900">3. Thông tin cá nhân</legend>
        <p className="text-xs text-slate-500">
          Nhập một lần — dùng cho mọi hệ bạn bật dưới đây (Thần số học, Bát Tự, Bát Trạch, Chiêm
          tinh). Chỉ điền phần nào cần cho hệ bạn muốn xem; để trống những gì không biết.
        </p>
        <div className="grid gap-3 sm:grid-cols-2">
          <label className="block text-sm">
            <span className="mb-1 block text-slate-600">Họ tên đầy đủ</span>
            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="Nguyễn Văn A"
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
            />
          </label>
          <label className="block text-sm">
            <span className="mb-1 block text-slate-600">Giới tính</span>
            <select
              value={gender}
              onChange={(e) => setGender(e.target.value as "" | "MALE" | "FEMALE")}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
            >
              <option value="">— chưa chọn —</option>
              <option value="MALE">Nam</option>
              <option value="FEMALE">Nữ</option>
            </select>
            <span className="mt-1 block text-xs text-slate-500">
              Bát Trạch bắt buộc có giới tính. Bát Tự vẫn chạy được nếu để trống, chỉ thiếu Đại
              Vận — hệ thống không đoán giúp bạn.
            </span>
          </label>
          <label className="block text-sm">
            <span className="mb-1 block text-slate-600">Ngày sinh (dương lịch)</span>
            <input
              type="date"
              value={birthDate}
              onChange={(e) => setBirthDate(e.target.value)}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
            />
          </label>
          <label className="block text-sm">
            <span className="mb-1 block text-slate-600">Giờ sinh (để trống nếu không biết)</span>
            <input
              type="time"
              value={birthTime}
              onChange={(e) => setBirthTime(e.target.value)}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
            />
            <span className="mt-1 block text-xs text-slate-500">
              Bát Tự vẫn lập được lá số nếu để trống (chỉ thiếu Trụ Giờ). Chiêm tinh thì bắt buộc.
            </span>
          </label>
        </div>

        <div className="grid gap-3 sm:grid-cols-2">
          <label className="block text-sm">
            <span className="mb-1 block text-slate-600">Nơi sinh (tỉnh/thành hiện nay)</span>
            <select
              value={provinceId}
              onChange={(e) => setProvinceId(e.target.value)}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
            >
              <option value="">— chưa chọn —</option>
              {VN_PROVINCES.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                  {p.formerProvinces.length > 0 ? ` (gồm ${p.formerProvinces.join(", ")} cũ)` : ""}
                </option>
              ))}
            </select>
            <span className="mt-1 block text-xs text-slate-500">
              Không biết bệnh viện/nơi sinh ở kinh độ, vĩ độ nào? Chỉ cần chọn tỉnh/thành theo tên
              gọi hiện nay (tìm theo tên tỉnh cũ vẫn ra đúng tỉnh mới) — hệ thống tự điền tọa độ
              trung tâm tỉnh, đủ chính xác cho việc hiệu chỉnh giờ (sai số nhỏ hơn sai số giờ sinh
              thường gặp).
            </span>
          </label>
          <div className="text-sm">
            <label className="flex items-center gap-2 text-slate-600">
              <input
                type="checkbox"
                checked={useCustomCoords}
                onChange={(e) => setUseCustomCoords(e.target.checked)}
              />
              Tôi biết chính xác tọa độ nơi sinh
            </label>
            {useCustomCoords && (
              <div className="mt-2 grid grid-cols-2 gap-2">
                <input
                  type="text"
                  inputMode="decimal"
                  value={customLatitude}
                  onChange={(e) => setCustomLatitude(e.target.value)}
                  placeholder="Vĩ độ, 10.8"
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                />
                <input
                  type="text"
                  inputMode="decimal"
                  value={customLongitude}
                  onChange={(e) => setCustomLongitude(e.target.value)}
                  placeholder="Kinh độ, 106.7"
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                />
              </div>
            )}
            {needsPreciseLocation && !useCustomCoords && province === null && (
              <p className="mt-2 text-xs text-amber-700">
                Chiêm tinh phương Tây cần nơi sinh — chọn tỉnh/thành ở trên, hoặc nhập tọa độ
                chính xác.
              </p>
            )}
          </div>
        </div>

        {showRegion && (
          <label className="block text-sm">
            <span className="mb-1 block text-slate-600">Vùng sinh (chỉ ảnh hưởng người sinh 1955–1975)</span>
            <select
              value={region}
              onChange={(e) => setRegion(e.target.value)}
              className="w-full max-w-xs rounded-md border border-slate-300 px-3 py-2 text-slate-900"
            >
              {REGION_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
            <span className="mt-1 block text-xs text-slate-500">
              Chỉ khác nhau khi sinh trong khoảng 1955–1975, lúc hai miền dùng múi giờ khác nhau.
              Dùng cho Bát Tự và Bát Trạch.
            </span>
          </label>
        )}

      </fieldset>

      <div className="space-y-3">
        <h2 className="px-1 text-sm font-semibold text-slate-900">4. Hệ thống áp dụng</h2>
        <p className="px-1 text-xs text-slate-500">
          Bật hệ nào thì hệ đó chạy. Mỗi hệ nói rõ nó đang cung cấp được gì và còn thiếu gì —
          không hệ nào được bật sẵn để trông cho đầy.
        </p>

      <fieldset className="space-y-3 rounded-lg border border-slate-200 bg-white p-4">
        <legend className="px-1 text-sm font-semibold text-slate-900">
          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={useNumerology}
              onChange={(e) => setUseNumerology(e.target.checked)}
            />
            Thần số học — Pythagoras
          </label>
        </legend>
        {useNumerology &&
          (numerologyDataReady ? (
            <p className="rounded-md bg-emerald-50 px-3 py-2 text-xs text-emerald-900">
              Đủ họ tên và ngày sinh — hệ này sẽ chạy cùng lần tính này. Nó chỉ cần hai thông tin
              đó, không cần giờ sinh hay nơi sinh.
            </p>
          ) : (
            <p className="rounded-md bg-amber-50 px-3 py-2 text-xs text-amber-900">
              Cần họ tên và ngày sinh ở mục <span className="font-medium">Thông tin cá nhân</span>{" "}
              phía trên. Chưa đủ thì hệ này không chạy.
            </p>
          ))}
      </fieldset>

      <fieldset className="space-y-3 rounded-lg border border-slate-200 bg-white p-4">
        <legend className="px-1 text-sm font-semibold text-slate-900">
          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={useTarot}
              onChange={(e) => setUseTarot(e.target.checked)}
            />
            Rút bài Tarot
          </label>
        </legend>
        {useTarot && (
          <div className="space-y-3">
            <label className="block text-sm">
              <span className="mb-1 block text-slate-600">Kiểu trải bài</span>
              <select
                value={spread}
                onChange={(e) => setSpread(e.target.value as TarotSpreadName)}
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
              >
                {SPREAD_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label} ({opt.cardCount === null ? "1–10 lá, bạn chọn" : `${opt.cardCount} lá`})
                  </option>
                ))}
              </select>
            </label>

            {spread === "FREE_FORM" && (
              <div className="space-y-2 rounded-md border border-slate-200 bg-white p-3">
                <label className="block text-sm">
                  <span className="mb-1 block text-slate-600">Số lá muốn bốc</span>
                  <input
                    type="number"
                    min={1}
                    max={TAROT_MAX_CARDS}
                    value={freeFormCount}
                    onChange={(e) => {
                      const next = Number(e.target.value);
                      const clamped = Math.min(TAROT_MAX_CARDS, Math.max(1, next || 1));
                      setFreeFormCount(clamped);
                      // Số lá đổi thì lựa chọn cũ không còn hợp lệ. Cắt bớt thay
                      // vì để người dùng gửi đi rồi nhận lỗi từ backend.
                      setPickedPositions((prev) => prev.slice(0, clamped));
                    }}
                    className="w-24 rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                  />
                </label>
                <p className="text-xs leading-relaxed text-slate-600">
                  Kiểu này <strong>không gán ý nghĩa cho vị trí nào</strong>. Các kiểu khác đặt tên
                  cho từng vị trí — ví dụ &ldquo;Quá khứ&rdquo; — tức là nhận rằng hệ thống biết quá
                  khứ của bạn. Nó không biết. Chọn kiểu tự do nếu bạn không muốn bố cục nói thay mình.
                </p>
              </div>
            )}

            <div className="space-y-2 rounded-md border border-slate-200 bg-white p-3">
              <label className="flex items-start gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={pickCardsMyself}
                  onChange={(e) => {
                    setPickCardsMyself(e.target.checked);
                    if (!e.target.checked) setPickedPositions([]);
                  }}
                  className="mt-0.5"
                />
                <span className="text-slate-700">
                  Tôi tự chọn lá
                  <span className="ml-1 text-slate-500">
                    (mặc định: hệ thống lấy từ trên bộ đã xào xuống)
                  </span>
                </span>
              </label>

              {pickCardsMyself && (
                <TarotRitual
                  cardsNeeded={tarotCardsNeeded}
                  picked={pickedPositions}
                  onChange={setPickedPositions}
                />
              )}

              {pickCardsMyself && pickedPositions.length !== tarotCardsNeeded && (
                <p className="text-xs text-amber-700">
                  Còn thiếu {tarotCardsNeeded - pickedPositions.length} lá. Nếu gửi bây giờ, hệ thống
                  sẽ lấy từ trên bộ xuống thay vì dùng lựa chọn của bạn.
                </p>
              )}
            </div>

            <p className="rounded-md bg-slate-50 px-3 py-2 text-xs text-slate-600">
              Tarot là hệ duy nhất hiện có bản diễn giải tiếng Việt riêng cho từng chiều (sự
              nghiệp, tài chính, quan hệ, quyết định) — nên nó là hệ đóng góp nhiều nhất vào
              phần trả lời đúng chủ đề bạn chọn.
            </p>
          </div>
        )}
      </fieldset>

      <fieldset className="space-y-3 rounded-lg border border-slate-200 bg-white p-4">
        <legend className="px-1 text-sm font-semibold text-slate-900">
          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={useBazi}
              onChange={(e) => setUseBazi(e.target.checked)}
            />
            Bát Tự — lập lá số Tứ Trụ
          </label>
        </legend>
        {useBazi && (
          <p className="rounded-md bg-amber-50 px-3 py-2 text-xs text-amber-900">
            Dùng ngày/giờ sinh, giới tính và vùng sinh ở mục Thông tin cá nhân. Hiện chỉ{" "}
            <span className="font-medium">lập lá số</span>: Tứ Trụ, Ngũ Hành, Tàng Can, Thập
            Thần, số đếm Ngũ Hành, Đại Vận nếu bạn nhập giới tính, và (khi có giờ sinh chính xác)
            cường độ Nhật Chủ theo Thiệu Vĩ Hoa — tất cả là dữ liệu tính toán tất định. Dụng Thần
            vẫn chưa được cung cấp vì các trường phái chưa thống nhất; cường độ Nhật Chủ theo
            Thiệu Vĩ Hoa cũng chỉ là kết quả của một trường phái cụ thể, không phải sự đồng thuận
            chung. Vì vậy Bát Tự chưa góp tín hiệu nào vào kết luận tổng hợp.
          </p>
        )}
      </fieldset>

      <fieldset className="space-y-3 rounded-lg border border-slate-200 bg-white p-4">
        <legend className="px-1 text-sm font-semibold text-slate-900">
          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={useFengShui}
              onChange={(e) => setUseFengShui(e.target.checked)}
            />
            Phong Thủy — Bát Trạch (cung phi &amp; hướng)
          </label>
        </legend>
        {useFengShui && (
          <div className="space-y-3">
            {/*
              Câu này trước đây nói đúng nhưng quá nhẹ, và chủ dự án đã bỏ qua ô
              hướng nhà rồi kết luận Phong Thủy "không có luận giải". Engine
              KHÔNG thiếu luận giải — nó cố ý im lặng khi chưa có hướng, vì số
              Cung Phi một mình là một hồ sơ chứ không phải một phán định. Cái
              thiếu là nói ra hệ quả, nên hệ quả nay được nói bằng màu và bằng chữ.
            */}
            <p
              className={
                "rounded-md px-3 py-2 text-xs " +
                (fsFacing === ""
                  ? "border border-amber-300 bg-amber-50 text-amber-900"
                  : "bg-slate-50 text-slate-600")
              }
            >
              {fsFacing === "" ? (
                <>
                  <span className="font-medium">Chưa chọn hướng nhà.</span> Phong Thủy sẽ chỉ trả
                  về <span className="font-medium">Cung Phi</span> của bạn và{" "}
                  <span className="font-medium">không đưa ra lời luận giải nào</span> — vì một số
                  Cung Phi tự nó chưa phải một đánh giá tốt/xấu, phải có hướng để so mới đánh giá
                  được. Chọn hướng ở ô ngay dưới nếu muốn có phần luận giải.
                </>
              ) : (
                <>
                  Dùng ngày sinh, giới tính và vùng sinh ở mục Thông tin cá nhân để tính Cung Phi,
                  rồi đối chiếu với hướng bạn chọn để đánh giá và góp tín hiệu vào kết luận tổng hợp.
                </>
              )}
            </p>
            <label className="block text-sm">
              <span className="mb-1 block text-slate-600">Hướng nhà / phòng (tùy chọn)</span>
              <select
                value={fsFacing}
                onChange={(e) => setFsFacing(e.target.value as CompassDirectionName | "")}
                className="w-full max-w-xs rounded-md border border-slate-300 px-3 py-2 text-slate-900"
              >
                <option value="">Chưa xác định</option>
                {DIRECTION_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </label>
          </div>
        )}
      </fieldset>

      <fieldset className="space-y-3 rounded-lg border border-slate-200 bg-white p-4">
        <legend className="px-1 text-sm font-semibold text-slate-900">
          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={useAstrology}
              onChange={(e) => setUseAstrology(e.target.checked)}
            />
            Chiêm tinh học phương Tây — lập lá số
          </label>
        </legend>
        {useAstrology && (
          <div className="space-y-3">
            <p className="rounded-md bg-amber-50 px-3 py-2 text-xs text-amber-900">
              Dùng ngày sinh, giờ sinh và nơi sinh ở mục Thông tin cá nhân — cần{" "}
              <span className="font-medium">đủ cả ba</span>, không thể lập lá số với giờ sinh
              hoặc nơi sinh đoán chừng: Cung Mọc dịch chuyển khoảng 1° mỗi 4 phút, nên một giờ
              sinh đoán sai sẽ cho ra kết quả sai một cách tự tin, không phải một kết quả rút
              gọn. Hiện chỉ lập được vị trí Mặt Trời, Thiên Đỉnh, Cung Mọc và 12 nhà (Whole
              Sign) — Mặt Trăng, các hành tinh khác và góc chiếu chưa được cung cấp, nên mục
              này chưa góp tín hiệu nào vào kết luận tổng hợp.
            </p>
            <p className="text-xs text-slate-500">
              Ngày và giờ sinh được đọc theo giờ dân sự Việt Nam. Nếu nơi sinh thực tế ở múi giờ
              khác, kết quả sẽ không chính xác — hệ thống hiện chưa có bộ chọn múi giờ riêng.
            </p>
          </div>
        )}
      </fieldset>

      <fieldset className="space-y-3 rounded-lg border border-slate-200 bg-white p-4">
        <legend className="px-1 text-sm font-semibold text-slate-900">
          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={useIChing}
              onChange={(e) => setUseIChing(e.target.checked)}
            />
            Kinh Dịch — gieo quẻ
          </label>
        </legend>
        {useIChing && (
          <div className="space-y-3">
            <p className="rounded-md bg-amber-50 px-3 py-2 text-xs text-amber-900">
              Hiện chỉ <span className="font-medium">gieo quẻ và xác định quẻ</span> (quẻ gốc, hào
              động, quẻ biến) — dữ liệu tính toán tất định. Lời đoán theo hào/quẻ chưa được cung
              cấp, nên mục này chưa góp tín hiệu nào vào kết luận tổng hợp.
            </p>
            <label className="block text-sm">
              <span className="mb-1 block text-slate-600">Cách gieo quẻ</span>
              <select
                value={ichingMethod}
                onChange={(e) => setIChingMethod(e.target.value as IChingCastingMethod)}
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
              >
                {ICHING_METHOD_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </label>
            {ichingMethod === "MAI_HOA_NUMBER" && (
              <div className="grid gap-3 sm:grid-cols-2">
                <label className="block text-sm">
                  <span className="mb-1 block text-slate-600">Số thứ nhất (thượng quái)</span>
                  <input
                    type="text"
                    inputMode="numeric"
                    value={ichingUpperNumber}
                    onChange={(e) => setIChingUpperNumber(e.target.value)}
                    placeholder="3"
                    className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                  />
                </label>
                <label className="block text-sm">
                  <span className="mb-1 block text-slate-600">Số thứ hai (hạ quái)</span>
                  <input
                    type="text"
                    inputMode="numeric"
                    value={ichingLowerNumber}
                    onChange={(e) => setIChingLowerNumber(e.target.value)}
                    placeholder="6"
                    className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                  />
                </label>
                <p className="text-xs text-slate-500 sm:col-span-2">
                  Cần đủ hai số — cách tách một số nhiều chữ số thành thượng/hạ quái chưa có nguồn
                  đủ tin cậy nên chưa hỗ trợ.
                </p>
              </div>
            )}
          </div>
        )}
      </fieldset>

      {/*
        Ba hệ trong đặc tả chưa có engine. Nêu tên và làm mờ thì đọc ra là "đã
        biết, chưa làm"; giấu đi thì đọc ra là "không tồn tại" — và đó chính là
        lý do chủ dự án đếm được năm hệ rồi hỏi phần còn lại đâu.
      */}
      <div className="rounded-lg border border-dashed border-slate-300 bg-slate-50 p-4">
        <h3 className="text-sm font-medium text-slate-700">Có trong đặc tả nhưng chưa chạy được</h3>
        <ul className="mt-2 space-y-1.5">
          {UNBUILT_SYSTEMS.map((sys) => (
            <li key={sys.id} className="text-xs text-slate-500">
              <span className="font-medium text-slate-600">{sys.nameVi}</span> — {sys.stateVi}
            </li>
          ))}
        </ul>
        <p className="mt-2 text-xs text-slate-500">
          Toàn bộ 9 hệ trong đặc tả và trạng thái thật của từng hệ nằm ở{" "}
          <Link href="/he-thong" className="font-medium underline underline-offset-2">
            trang Hệ thống
          </Link>
          .
        </p>
      </div>

      </div>
      </div>

      {error && (
        <div role="alert" className="rounded-md bg-rose-50 px-4 py-3 text-sm text-rose-800">
          <p>{error}</p>
          <p className="mt-1 text-xs">
            Chi tiết đầy đủ nằm ở{" "}
            <Link href="/nhat-ky" className="font-medium underline underline-offset-2">
              Nhật ký
            </Link>{" "}
            — trang đó có nút sao chép toàn bộ để gửi đi khi cần báo lỗi.
          </p>
        </div>
      )}

      {/*
        Sticky, so "Tính toán" is reachable from anywhere in the form instead
        of only from its bottom edge.
      */}
      <div className="sticky bottom-0 -mx-4 border-t border-slate-200 bg-slate-50/95 px-4 py-3 backdrop-blur sm:-mx-6 sm:px-6">
        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-md bg-slate-900 px-4 py-3 text-sm font-semibold text-white hover:bg-slate-700 disabled:opacity-50"
        >
          {submitting ? "Đang tính toán…" : "Tính toán"}
        </button>
      </div>
    </form>
  );
}
