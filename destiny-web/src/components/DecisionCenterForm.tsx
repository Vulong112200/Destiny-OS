"use client";

import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { ApiError, runScenario } from "@/lib/api";
import { findVnProvince, VN_PROVINCES } from "@/lib/vnProvinces";
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

const SPREAD_OPTIONS: { value: TarotSpreadName; label: string; cardCount: number }[] = [
  { value: "PAST_PRESENT_FUTURE", label: "Quá khứ – Hiện tại – Tương lai", cardCount: 3 },
  { value: "CHOICE_A_B", label: "Lựa chọn A – B", cardCount: 2 },
  { value: "SITUATION_CHALLENGE_ADVICE", label: "Tình huống – Thử thách – Lời khuyên", cardCount: 3 },
];

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
  const [scenarioType, setScenarioType] = useState<SupportedScenarioType>("BUSINESS");

  // Vietnamese labels from ScenarioRegistry's own displayNameVi — kept in
  // sync by hand rather than fetched, since the set of *supported* scenarios
  // is a frontend decision (which ones get a dedicated input form below),
  // not something GET /api/v1/methodologies exposes.
  const SCENARIO_LABELS: Record<SupportedScenarioType, string> = {
    BUSINESS: "Mở rộng kinh doanh",
    DAILY_ACTION: "Hôm nay nên làm gì",
    CAREER: "Sự nghiệp",
    FINANCE: "Tài chính",
    RELATIONSHIP: "Quan hệ",
    PURCHASE: "Mua sắm",
    TRAVEL: "Di chuyển",
    PROJECT: "Dự án",
    GENERAL_DECISION: "Quyết định chung",
  };

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

  const hasNumerology = fullName.trim() !== "" && birthDate !== "";

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
        "Cần ít nhất một hệ thống: nhập họ tên + ngày sinh (có Thần số học), bật rút bài Tarot, bật Bát Tự, bật Bát Trạch, bật Chiêm tinh phương Tây, hoặc bật Kinh Dịch.",
      );
      return;
    }

    setSubmitting(true);
    try {
      const result = await runScenario(scenarioType, {
        numerology: hasNumerology ? { fullName: fullName.trim(), birthDate } : null,
        tarot: useTarot ? { spread, seed: null, question: question.trim() || null } : null,
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
      setError(err instanceof ApiError ? err.message : "Không thể kết nối tới hệ thống tính toán.");
    } finally {
      setSubmitting(false);
    }
  }

  const showRegion = useMemo(() => useBazi || useFengShui, [useBazi, useFengShui]);
  const needsPreciseLocation = useAstrology;

  return (
    <form onSubmit={handleSubmit} className="space-y-8">
      <fieldset className="space-y-2">
        <legend className="text-sm font-semibold text-slate-900">1. Bạn đang muốn xem điều gì?</legend>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
          {(Object.keys(SCENARIO_LABELS) as SupportedScenarioType[]).map((type) => (
            <label
              key={type}
              className={`cursor-pointer rounded-lg border px-4 py-3 text-center text-sm ${
                scenarioType === type
                  ? "border-slate-900 bg-slate-900 text-white"
                  : "border-slate-200 text-slate-700 hover:border-slate-400"
              }`}
            >
              <input
                type="radio"
                name="scenarioType"
                value={type}
                checked={scenarioType === type}
                onChange={() => setScenarioType(type)}
                className="sr-only"
              />
              {SCENARIO_LABELS[type]}
            </label>
          ))}
        </div>
        <p className="text-xs text-slate-500">
          9 chủ đề đã có chính sách áp dụng hệ thống thật. Riêng &quot;Tương hợp&quot;
          (so hai lá số trước khi cưới/hợp tác) chưa có ở đây — hệ thống hiện chỉ nhận một
          lá số mỗi lượt tính, còn thực hành truyền thống mạnh nhất cho tương hợp lại cần hai.
        </p>
      </fieldset>

      <fieldset className="space-y-3 rounded-lg border border-slate-200 p-4">
        <legend className="px-1 text-sm font-semibold text-slate-900">2. Thông tin cá nhân</legend>
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

        {hasNumerology && (
          <p className="rounded-md bg-emerald-50 px-3 py-2 text-xs text-emerald-900">
            Đủ họ tên + ngày sinh — Thần số học sẽ tự động chạy cùng lần tính này.
          </p>
        )}
      </fieldset>

      <fieldset className="space-y-3 rounded-lg border border-slate-200 p-4">
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
                    {opt.label} ({opt.cardCount} lá)
                  </option>
                ))}
              </select>
            </label>
            <label className="block text-sm">
              <span className="mb-1 block text-slate-600">Câu hỏi / bối cảnh (tùy chọn)</span>
              <textarea
                value={question}
                onChange={(e) => setQuestion(e.target.value)}
                rows={2}
                placeholder="Tôi có nên mở rộng kinh doanh không?"
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
              />
            </label>
          </div>
        )}
      </fieldset>

      <fieldset className="space-y-3 rounded-lg border border-slate-200 p-4">
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

      <fieldset className="space-y-3 rounded-lg border border-slate-200 p-4">
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
            <p className="rounded-md bg-slate-50 px-3 py-2 text-xs text-slate-600">
              Dùng ngày sinh, giới tính và vùng sinh ở mục Thông tin cá nhân để tính cung phi.
              Nhập thêm <span className="font-medium">hướng nhà/phòng</span> dưới đây thì hệ
              thống mới đánh giá được hướng đó và góp tín hiệu vào kết luận tổng hợp.
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

      <fieldset className="space-y-3 rounded-lg border border-slate-200 p-4">
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

      <fieldset className="space-y-3 rounded-lg border border-slate-200 p-4">
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

      {error && (
        <p role="alert" className="rounded-md bg-rose-50 px-4 py-3 text-sm text-rose-800">
          {error}
        </p>
      )}

      <button
        type="submit"
        disabled={submitting}
        className="w-full rounded-md bg-slate-900 px-4 py-3 text-sm font-semibold text-white hover:bg-slate-700 disabled:opacity-50"
      >
        {submitting ? "Đang tính toán…" : "Tính toán"}
      </button>
    </form>
  );
}
