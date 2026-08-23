"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { ApiError, runScenario } from "@/lib/api";
import type {
  CompassDirectionName,
  SupportedScenarioType,
  TarotSpreadName,
} from "@/lib/types";

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

const REGION_OPTIONS: { value: string; label: string }[] = [
  { value: "UNKNOWN", label: "Chưa rõ / không áp dụng" },
  { value: "NORTH", label: "Miền Bắc" },
  { value: "SOUTH", label: "Miền Nam" },
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
  const [fullName, setFullName] = useState("");
  const [birthDate, setBirthDate] = useState("");
  const [useTarot, setUseTarot] = useState(true);
  const [spread, setSpread] = useState<TarotSpreadName>("PAST_PRESENT_FUTURE");
  const [question, setQuestion] = useState("");
  const [useBazi, setUseBazi] = useState(false);
  const [baziBirthDate, setBaziBirthDate] = useState("");
  const [baziBirthTime, setBaziBirthTime] = useState("");
  const [baziRegion, setBaziRegion] = useState("UNKNOWN");
  const [baziLongitude, setBaziLongitude] = useState("");
  // Empty on purpose, and never defaulted to a value: gender decides the Đại
  // Vận direction, and a guessed direction produces a full sequence that is
  // wrong from its first period while looking exactly like a correct one.
  const [baziGender, setBaziGender] = useState<"" | "MALE" | "FEMALE">("");
  const [useFengShui, setUseFengShui] = useState(false);
  const [fsBirthDate, setFsBirthDate] = useState("");
  const [fsGender, setFsGender] = useState<"MALE" | "FEMALE">("MALE");
  const [fsRegion, setFsRegion] = useState("UNKNOWN");
  const [fsFacing, setFsFacing] = useState<CompassDirectionName | "">("");
  const [useAstrology, setUseAstrology] = useState(false);
  const [astroBirthDate, setAstroBirthDate] = useState("");
  const [astroBirthTime, setAstroBirthTime] = useState("");
  const [astroLatitude, setAstroLatitude] = useState("");
  const [astroLongitude, setAstroLongitude] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);

    const hasNumerology = fullName.trim() !== "" && birthDate !== "";
    const hasBazi = useBazi && baziBirthDate !== "";
    const hasFengShui = useFengShui && fsBirthDate !== "";
    const hasAstrology =
      useAstrology &&
      astroBirthDate !== "" &&
      astroBirthTime !== "" &&
      astroLatitude.trim() !== "" &&
      astroLongitude.trim() !== "";
    if (useBazi && baziBirthDate === "") {
      setError("Bát Tự cần ngày sinh. Giờ sinh có thể để trống — khi đó chỉ lập được Trụ Năm và Trụ Tháng.");
      return;
    }
    if (useFengShui && fsBirthDate === "") {
      setError("Bát Trạch cần ngày sinh và giới tính để tính cung phi.");
      return;
    }
    if (
      useAstrology &&
      (astroBirthDate === "" || astroBirthTime === "" || astroLatitude.trim() === "" || astroLongitude.trim() === "")
    ) {
      setError(
        "Chiêm tinh phương Tây cần đủ ngày sinh, giờ sinh chính xác và tọa độ nơi sinh — không thể lập lá số nếu thiếu một trong các mục này.",
      );
      return;
    }
    if (!hasNumerology && !useTarot && !hasBazi && !hasFengShui && !hasAstrology) {
      setError(
        "Cần ít nhất một hệ thống: nhập họ tên + ngày sinh, bật rút bài Tarot, bật Bát Tự, bật Bát Trạch, hoặc bật Chiêm tinh phương Tây.",
      );
      return;
    }

    const longitude = baziLongitude.trim() === "" ? null : Number(baziLongitude);
    if (longitude !== null && !Number.isFinite(longitude)) {
      setError("Kinh độ phải là một số, ví dụ 106.7. Để trống nếu không biết.");
      return;
    }

    const astroLat = hasAstrology ? Number(astroLatitude) : null;
    const astroLon = hasAstrology ? Number(astroLongitude) : null;
    if (hasAstrology && (!Number.isFinite(astroLat) || !Number.isFinite(astroLon))) {
      setError("Vĩ độ và kinh độ nơi sinh (chiêm tinh) phải là số, ví dụ 10.8 và 106.7.");
      return;
    }

    setSubmitting(true);
    try {
      const result = await runScenario(scenarioType, {
        numerology: hasNumerology ? { fullName: fullName.trim(), birthDate } : null,
        tarot: useTarot ? { spread, seed: null, question: question.trim() || null } : null,
        bazi: hasBazi
          ? {
              birthDate: baziBirthDate,
              // Empty means "not known", never a stand-in hour: the backend
              // returns two pillars and says so, rather than treating an
              // unknown time as exact (Master Spec section 2).
              birthTime: baziBirthTime === "" ? null : baziBirthTime,
              region: baziRegion,
              longitude,
              // Same rule as the hour: empty means "not supplied", and the
              // backend omits Đại Vận with a stated reason rather than
              // picking a direction.
              gender: baziGender === "" ? null : baziGender,
            }
          : null,
        fengShui: hasFengShui
          ? {
              birthDate: fsBirthDate,
              birthTime: null,
              gender: fsGender,
              region: fsRegion,
              longitude: null,
              // Empty means "no direction to assess" - the backend then returns
              // the eight-direction profile and no signal, rather than judging
              // a direction the user never gave.
              facingDirection: fsFacing === "" ? null : fsFacing,
            }
          : null,
        astrology: hasAstrology
          ? {
              birthDate: astroBirthDate,
              birthTime: astroBirthTime,
              latitudeDegrees: astroLat as number,
              longitudeDegrees: astroLon as number,
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
        <legend className="px-1 text-sm font-semibold text-slate-900">Thần số học (tùy chọn)</legend>
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
            <span className="mb-1 block text-slate-600">Ngày sinh</span>
            <input
              type="date"
              value={birthDate}
              onChange={(e) => setBirthDate(e.target.value)}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
            />
          </label>
        </div>
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
              <span className="mb-1 block text-slate-600">2. Câu hỏi / bối cảnh (tùy chọn)</span>
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
          <div className="space-y-3">
            <p className="rounded-md bg-amber-50 px-3 py-2 text-xs text-amber-900">
              Hiện chỉ <span className="font-medium">lập lá số</span>: Tứ Trụ, Ngũ Hành, Tàng Can,
              Thập Thần, số đếm Ngũ Hành, và <span className="font-medium">Đại Vận</span> nếu bạn
              nhập giới tính — tất cả là dữ liệu tính toán tất định. Phần luận giải (Dụng Thần,
              cường độ Nhật Chủ) chưa được cung cấp vì các trường phái chưa thống nhất và hệ thống
              không tự chọn giúp bạn. Vì vậy Bát Tự chưa góp tín hiệu nào vào kết luận tổng hợp —
              kể cả Đại Vận, vì một vận chỉ tốt hay xấu khi đã có Dụng Thần.
            </p>
            <div className="grid gap-3 sm:grid-cols-2">
              <label className="block text-sm">
                <span className="mb-1 block text-slate-600">Ngày sinh (dương lịch)</span>
                <input
                  type="date"
                  value={baziBirthDate}
                  onChange={(e) => setBaziBirthDate(e.target.value)}
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                />
              </label>
              <label className="block text-sm">
                <span className="mb-1 block text-slate-600">Giờ sinh (để trống nếu không biết)</span>
                <input
                  type="time"
                  value={baziBirthTime}
                  onChange={(e) => setBaziBirthTime(e.target.value)}
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                />
                <span className="mt-1 block text-xs text-slate-500">
                  Không biết giờ thì để trống — hệ thống sẽ chỉ lập Trụ Năm và Trụ Tháng, không
                  đặt giờ giả.
                </span>
              </label>
              <label className="block text-sm">
                <span className="mb-1 block text-slate-600">Vùng sinh</span>
                <select
                  value={baziRegion}
                  onChange={(e) => setBaziRegion(e.target.value)}
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                >
                  {REGION_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
                <span className="mt-1 block text-xs text-slate-500">
                  Chỉ ảnh hưởng với người sinh trong khoảng 1955–1975, khi hai miền dùng múi giờ
                  khác nhau.
                </span>
              </label>
              <label className="block text-sm">
                <span className="mb-1 block text-slate-600">Kinh độ nơi sinh (tùy chọn)</span>
                <input
                  type="text"
                  inputMode="decimal"
                  value={baziLongitude}
                  onChange={(e) => setBaziLongitude(e.target.value)}
                  placeholder="106.7"
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                />
                <span className="mt-1 block text-xs text-slate-500">
                  Có kinh độ thì giờ sinh được hiệu chỉnh về giờ mặt trời — chỉ quan trọng khi giờ
                  sinh sát ranh giới canh giờ.
                </span>
              </label>
              <label className="block text-sm">
                <span className="mb-1 block text-slate-600">Giới tính (để có Đại Vận)</span>
                <select
                  value={baziGender}
                  onChange={(e) => setBaziGender(e.target.value as "" | "MALE" | "FEMALE")}
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                >
                  {/* No preselected value: see the state declaration. */}
                  <option value="">— chưa chọn —</option>
                  <option value="MALE">Nam</option>
                  <option value="FEMALE">Nữ</option>
                </select>
                <span className="mt-1 block text-xs text-slate-500">
                  Chiều Đại Vận (thuận hay nghịch) phụ thuộc giới tính kết hợp âm dương can năm.
                  Để trống thì lá số Tứ Trụ vẫn đầy đủ, chỉ không có phần Đại Vận — hệ thống
                  không đoán giúp bạn.
                </span>
              </label>
            </div>
          </div>
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
              Tính cung phi và tám hướng theo Bát Biến Du Niên. Nhập thêm{" "}
              <span className="font-medium">hướng nhà/phòng</span> thì hệ thống mới đánh giá được
              hướng đó và góp tín hiệu vào kết luận tổng hợp — Bát Trạch xét{" "}
              <span className="font-medium">quan hệ giữa người và một hướng</span>, nên không có
              hướng thì không có gì để đánh giá.
            </p>
            <div className="grid gap-3 sm:grid-cols-2">
              <label className="block text-sm">
                <span className="mb-1 block text-slate-600">Ngày sinh (dương lịch)</span>
                <input
                  type="date"
                  value={fsBirthDate}
                  onChange={(e) => setFsBirthDate(e.target.value)}
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                />
              </label>
              <label className="block text-sm">
                <span className="mb-1 block text-slate-600">Giới tính</span>
                <select
                  value={fsGender}
                  onChange={(e) => setFsGender(e.target.value as "MALE" | "FEMALE")}
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                >
                  <option value="MALE">Nam</option>
                  <option value="FEMALE">Nữ</option>
                </select>
                <span className="mt-1 block text-xs text-slate-500">
                  Bắt buộc: công thức cung phi cho nam và nữ khác nhau và không đối xứng, nên
                  không có giá trị mặc định nào trung lập.
                </span>
              </label>
              <label className="block text-sm">
                <span className="mb-1 block text-slate-600">Hướng nhà / phòng (tùy chọn)</span>
                <select
                  value={fsFacing}
                  onChange={(e) => setFsFacing(e.target.value as CompassDirectionName | "")}
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                >
                  <option value="">Chưa xác định</option>
                  {DIRECTION_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
              </label>
              <label className="block text-sm">
                <span className="mb-1 block text-slate-600">Vùng sinh</span>
                <select
                  value={fsRegion}
                  onChange={(e) => setFsRegion(e.target.value)}
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                >
                  {REGION_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
              </label>
            </div>
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
              Cần <span className="font-medium">đủ cả bốn mục</span> dưới đây — không thể lập lá số
              với giờ sinh hoặc tọa độ đoán chừng: Cung Mọc dịch chuyển khoảng 1° mỗi 4 phút, nên
              một giờ sinh đoán sai sẽ cho ra kết quả sai một cách tự tin, không phải một kết quả
              rút gọn. Hiện chỉ lập được vị trí Mặt Trời, Thiên Đỉnh, Cung Mọc và 12 nhà (Whole
              Sign) — Mặt Trăng, các hành tinh khác và góc chiếu chưa được cung cấp, nên mục này
              chưa góp tín hiệu nào vào kết luận tổng hợp.
            </p>
            <div className="grid gap-3 sm:grid-cols-2">
              <label className="block text-sm">
                <span className="mb-1 block text-slate-600">Ngày sinh</span>
                <input
                  type="date"
                  value={astroBirthDate}
                  onChange={(e) => setAstroBirthDate(e.target.value)}
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                />
              </label>
              <label className="block text-sm">
                <span className="mb-1 block text-slate-600">Giờ sinh (bắt buộc, càng chính xác càng tốt)</span>
                <input
                  type="time"
                  value={astroBirthTime}
                  onChange={(e) => setAstroBirthTime(e.target.value)}
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                />
              </label>
              <label className="block text-sm">
                <span className="mb-1 block text-slate-600">Vĩ độ nơi sinh</span>
                <input
                  type="text"
                  inputMode="decimal"
                  value={astroLatitude}
                  onChange={(e) => setAstroLatitude(e.target.value)}
                  placeholder="10.8 (Bắc dương)"
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                />
              </label>
              <label className="block text-sm">
                <span className="mb-1 block text-slate-600">Kinh độ nơi sinh</span>
                <input
                  type="text"
                  inputMode="decimal"
                  value={astroLongitude}
                  onChange={(e) => setAstroLongitude(e.target.value)}
                  placeholder="106.7 (Đông dương)"
                  className="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-900"
                />
              </label>
            </div>
            <p className="text-xs text-slate-500">
              Ngày và giờ sinh được đọc theo giờ dân sự Việt Nam. Nếu nơi sinh thực tế ở múi giờ
              khác, kết quả sẽ không chính xác — hệ thống hiện chưa có bộ chọn múi giờ riêng.
            </p>
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
        {submitting ? "Đang tính toán…" : "3. Tính toán"}
      </button>
    </form>
  );
}
