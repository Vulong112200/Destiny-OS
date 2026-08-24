import type { EvidenceDto, LabelRegistries } from "@/lib/types";

/**
 * Renders the Bát Tự Tứ Trụ chart as hard data, reconstructed from the
 * evidence records `BaziEngine` emits.
 *
 * <p>This component reads `EvidenceDto.fact` rather than a dedicated DTO on
 * purpose. `Evidence` is the project's explainability record — the thing the
 * "Vì sao có kết quả này?" panel is built from — so a chart rendered from
 * evidence is a chart that cannot drift away from what the audit trail says.
 * The cost is that facts hold technical names (`GIAP`, `TY_KIEN`), which is
 * what `labels` is for.
 *
 * <p>Everything shown here is deterministic hard data, displayed independently
 * of any AI narrative (CLAUDE.md section 9). Nothing here is a reading: the
 * blocked sections at the bottom are the reading, and they are absent on
 * purpose.
 */

const PILLAR_ORDER = ["YEAR", "MONTH", "DAY", "HOUR"] as const;
const ELEMENT_ORDER = ["WOOD", "FIRE", "EARTH", "METAL", "WATER"] as const;

/** Falls back to the technical name so a missing label degrades, never blanks. */
function label(labels: LabelRegistries, type: string, key: unknown): string {
  if (typeof key !== "string") return "—";
  return labels[type]?.[key] ?? key;
}

function factOf(evidence: EvidenceDto[], ruleId: string): Record<string, unknown> | null {
  return evidence.find((e) => e.ruleId === ruleId)?.fact ?? null;
}

function asStringArray(value: unknown): string[] {
  return Array.isArray(value) ? value.filter((v): v is string => typeof v === "string") : [];
}

function asCounts(value: unknown): Record<string, number> {
  if (value === null || typeof value !== "object") return {};
  return value as Record<string, number>;
}

type LuckPillarFact = {
  ordinal: number;
  stem: string;
  branch: string;
  startAgeYears: number;
  startDate: string;
};

function asLuckPillars(value: unknown): LuckPillarFact[] {
  return Array.isArray(value) ? (value as LuckPillarFact[]) : [];
}

/**
 * "8 năm 4 tháng", dropping the parts that are zero.
 *
 * The engine reports years, months and days because the tradition's own
 * conversion produces all three; showing "8 tuổi" alone would round away a
 * distinction the calculation actually made.
 */
function startAgeText(fact: Record<string, unknown>): string {
  const parts: string[] = [];
  const years = Number(fact.startAgeYears ?? 0);
  const months = Number(fact.startAgeMonths ?? 0);
  const days = Number(fact.startAgeDays ?? 0);
  if (years > 0) parts.push(`${years} năm`);
  if (months > 0) parts.push(`${months} tháng`);
  if (days > 0) parts.push(`${days} ngày`);
  return parts.length > 0 ? parts.join(" ") : "ngay từ khi sinh";
}

export function BaziChartCard({
  evidence,
  labels,
}: {
  evidence: EvidenceDto[];
  labels: LabelRegistries;
}) {
  const baziEvidence = evidence.filter((e) => e.engine === "BAZI");
  if (baziEvidence.length === 0) {
    return null;
  }

  const boundary = factOf(baziEvidence, "BAZI_BOUNDARY");
  const tally = factOf(baziEvidence, "BAZI_ELEMENT_TALLY");
  const pillars = PILLAR_ORDER.map((position) => ({
    position,
    fact: factOf(baziEvidence, `BAZI_PILLAR_${position}`),
  })).filter((p) => p.fact !== null);

  const blocked = baziEvidence.filter((e) => e.ruleId.startsWith("BAZI_BLOCKED_"));
  const hasHourPrecision = boundary?.hasHourPrecision === true;
  const luck = factOf(baziEvidence, "BAZI_LUCK_CYCLES");
  const dayMasterStrength = factOf(baziEvidence, "BAZI_DAY_MASTER_STRENGTH");
  const dayMasterStrengthSchool = baziEvidence.find(
    (e) => e.ruleId === "BAZI_DAY_MASTER_STRENGTH",
  )?.school;

  return (
    <section className="space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div>
        <h2 className="text-lg font-semibold text-slate-900">Lá số Tứ Trụ (Bát Tự)</h2>
        <p className="mt-1 text-xs text-slate-500">
          Dữ liệu tính toán tất định. Đây là <span className="font-medium">lá số</span>, chưa phải
          lời luận giải — phần luận giải xem ở cuối mục này.
        </p>
      </div>

      {boundary && (
        <p className="rounded-md bg-slate-50 px-3 py-2 text-xs text-slate-600">
          Năm Bát Tự: <span className="font-medium">{String(boundary.baziYear)}</span> ·{" "}
          {label(labels, "BaziYearBoundary", boundary.yearBoundary)} · Tiết Khí lúc sinh:{" "}
          <span className="font-medium">{label(labels, "SolarTerm", boundary.solarTermAtBirth)}</span>{" "}
          · Tháng theo Tiết Khí:{" "}
          <span className="font-medium">
            {label(labels, "EarthlyBranch", boundary.solarMonthBranch)}
          </span>
        </p>
      )}

      {!hasHourPrecision && (
        <p className="rounded-md bg-amber-50 px-3 py-2 text-xs text-amber-900">
          Không có giờ sinh chính xác nên chỉ lập được Trụ Năm và Trụ Tháng. Không có Nhật Chủ, do
          đó cũng không có phần Thập Thần — hệ thống không tự đặt một giờ sinh giả.
        </p>
      )}

      <div className="overflow-x-auto">
        <table className="w-full min-w-[32rem] border-collapse text-sm">
          <caption className="sr-only">
            Bảng Tứ Trụ: mỗi cột là một trụ, gồm Thiên Can, Địa Chi, Ngũ Hành, Tàng Can và Thập Thần
          </caption>
          <thead>
            <tr className="text-left text-xs uppercase tracking-wide text-slate-500">
              <th scope="col" className="w-28 py-2 font-medium">
                &nbsp;
              </th>
              {pillars.map(({ position }) => (
                <th key={position} scope="col" className="py-2 font-medium">
                  {label(labels, "PillarPosition", position)}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            <tr>
              <th scope="row" className="py-2 text-left text-xs font-medium text-slate-500">
                Thiên Can
              </th>
              {pillars.map(({ position, fact }) => (
                <td key={position} className="py-2 text-base font-semibold text-slate-900">
                  {label(labels, "HeavenlyStem", fact!.stem)}
                  {position === "DAY" && (
                    <span className="ml-2 rounded-full bg-slate-900 px-2 py-0.5 text-xs font-medium text-white">
                      Nhật Chủ
                    </span>
                  )}
                </td>
              ))}
            </tr>
            <tr>
              <th scope="row" className="py-2 text-left text-xs font-medium text-slate-500">
                Địa Chi
              </th>
              {pillars.map(({ position, fact }) => (
                <td key={position} className="py-2 text-base font-semibold text-slate-900">
                  {label(labels, "EarthlyBranch", fact!.branch)}
                </td>
              ))}
            </tr>
            <tr>
              <th scope="row" className="py-2 text-left text-xs font-medium text-slate-500">
                Ngũ Hành (Can · Chi)
              </th>
              {pillars.map(({ position, fact }) => (
                <td key={position} className="py-2 text-slate-700">
                  {label(labels, "FiveElement", fact!.stemElement)}
                  <span className="text-slate-400"> · </span>
                  {label(labels, "FiveElement", fact!.branchElement)}
                  <span className="ml-1 text-xs text-slate-500">
                    ({label(labels, "YinYang", fact!.stemPolarity)})
                  </span>
                </td>
              ))}
            </tr>
            <tr>
              <th scope="row" className="py-2 text-left text-xs font-medium text-slate-500">
                Tàng Can
              </th>
              {pillars.map(({ position, fact }) => (
                <td key={position} className="py-2 text-slate-700">
                  {asStringArray(fact!.hiddenStems)
                    .map((stem) => label(labels, "HeavenlyStem", stem))
                    .join(", ")}
                  {fact!.hiddenStemRoleOrderingDisputed === true && (
                    <span
                      title="Hai nguồn tham chiếu xếp thứ tự trung khí / dư khí khác nhau cho địa chi này. Hệ thống ghi nhận cả bộ can ẩn nhưng không xếp vai, và không dùng thứ tự này vào bất kỳ phép tính nào."
                      className="ml-1 cursor-help text-xs text-amber-700"
                    >
                      (thứ tự vai: nguồn chưa thống nhất)
                    </span>
                  )}
                </td>
              ))}
            </tr>
            {hasHourPrecision && (
              <tr>
                <th scope="row" className="py-2 text-left text-xs font-medium text-slate-500">
                  Thập Thần (theo Can)
                </th>
                {pillars.map(({ position, fact }) => (
                  <td key={position} className="py-2 text-slate-700">
                    {position === "DAY"
                      ? "— (chính là Nhật Chủ)"
                      : label(labels, "TenGod", fact!.stemTenGod)}
                  </td>
                ))}
              </tr>
            )}
            {hasHourPrecision && (
              <tr>
                <th scope="row" className="py-2 text-left text-xs font-medium text-slate-500">
                  Thập Thần (Tàng Can)
                </th>
                {pillars.map(({ position, fact }) => (
                  <td key={position} className="py-2 text-xs text-slate-600">
                    {asStringArray(fact!.hiddenStemTenGods)
                      .map((god) => label(labels, "TenGod", god))
                      .join(", ") || "—"}
                  </td>
                ))}
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {tally && (
        <div>
          <h3 className="text-sm font-semibold text-slate-900">Số đếm Ngũ Hành</h3>
          <p className="mb-2 text-xs text-slate-500">
            Ba nhóm đếm riêng, cố tình <span className="font-medium">không</span> cộng gộp: các
            trường phái không thống nhất đếm cái gì, nên một con số tổng sẽ là chọn giúp bạn một
            trường phái. Đây là số đếm thô, không phải đánh giá cường độ Ngũ Hành (R3).
          </p>
          <div className="overflow-x-auto">
            <table className="w-full min-w-[28rem] border-collapse text-sm">
              <thead>
                <tr className="text-left text-xs uppercase tracking-wide text-slate-500">
                  <th scope="col" className="w-40 py-1.5 font-medium">
                    Nhóm
                  </th>
                  {ELEMENT_ORDER.map((element) => (
                    <th key={element} scope="col" className="py-1.5 font-medium">
                      {label(labels, "FiveElement", element)}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {(
                  [
                    ["stems", "Theo Thiên Can"],
                    ["branches", "Theo Địa Chi"],
                    ["hiddenStems", "Theo Tàng Can"],
                  ] as const
                ).map(([key, groupLabel]) => {
                  const counts = asCounts(tally[key]);
                  return (
                    <tr key={key}>
                      <th scope="row" className="py-1.5 text-left text-xs font-medium text-slate-600">
                        {groupLabel}
                      </th>
                      {ELEMENT_ORDER.map((element) => (
                        <td key={element} className="py-1.5 tabular-nums text-slate-800">
                          {counts[element] ?? 0}
                        </td>
                      ))}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {luck && (
        <div>
          <h3 className="text-sm font-semibold text-slate-900">Đại Vận (các vận 10 năm)</h3>
          <p className="mb-2 text-xs text-slate-500">
            Chuỗi vận và tuổi khởi vận là <span className="font-medium">dữ liệu lập được</span>.
            Một vận <span className="font-medium">tốt hay xấu</span> thì chưa — điều đó cần Dụng
            Thần (R1) và cường độ Nhật Chủ (R3), nên bảng dưới đây cố tình không có cột đánh giá.
          </p>

          <dl className="mb-3 grid grid-cols-1 gap-x-6 gap-y-1 text-sm sm:grid-cols-2">
            <div className="flex gap-2">
              <dt className="text-slate-500">Chiều vận</dt>
              <dd className="font-medium text-slate-900">
                {label(labels, "LuckCycleDirection", luck.direction)}
              </dd>
            </div>
            <div className="flex gap-2">
              <dt className="text-slate-500">Khởi vận lúc</dt>
              <dd className="font-medium text-slate-900">{startAgeText(luck)}</dd>
            </div>
            <div className="flex gap-2">
              <dt className="text-slate-500">Đếm tới tiết</dt>
              <dd className="text-slate-800">{label(labels, "SolarTerm", luck.boundaryTerm)}</dd>
            </div>
            <div className="flex gap-2">
              <dt className="text-slate-500">Khoảng cách</dt>
              {/* The day count is what every Bát Tự text states, so it is the
                  one number a reader can check this against by hand. */}
              <dd className="tabular-nums text-slate-800">
                {String(luck.distanceDays ?? "—")} ngày {String(luck.distanceHours ?? 0)} giờ
              </dd>
            </div>
          </dl>

          <div className="overflow-x-auto">
            <table className="w-full min-w-[30rem] border-collapse text-sm">
              <thead>
                <tr className="text-left text-xs uppercase tracking-wide text-slate-500">
                  <th scope="col" className="w-16 py-1.5 font-medium">
                    Vận
                  </th>
                  <th scope="col" className="py-1.5 font-medium">
                    Thiên Can
                  </th>
                  <th scope="col" className="py-1.5 font-medium">
                    Địa Chi
                  </th>
                  <th scope="col" className="py-1.5 font-medium">
                    Từ tuổi
                  </th>
                  <th scope="col" className="py-1.5 font-medium">
                    Từ ngày
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {asLuckPillars(luck.pillars).map((pillar) => (
                  <tr key={pillar.ordinal}>
                    <th scope="row" className="py-1.5 text-left text-xs font-medium text-slate-600">
                      {pillar.ordinal}
                    </th>
                    <td className="py-1.5 text-slate-800">
                      {label(labels, "HeavenlyStem", pillar.stem)}
                    </td>
                    <td className="py-1.5 text-slate-800">
                      {label(labels, "EarthlyBranch", pillar.branch)}
                    </td>
                    <td className="py-1.5 tabular-nums text-slate-800">{pillar.startAgeYears}</td>
                    <td className="py-1.5 tabular-nums text-slate-600">{pillar.startDate}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {dayMasterStrength && (
        <div>
          <h3 className="text-sm font-semibold text-slate-900">Cường độ Nhật Chủ</h3>
          <p className="mb-2 text-xs text-slate-500">
            Theo <span className="font-medium">{dayMasterStrengthSchool ?? "một trường phái"}</span>{" "}
            — kết quả của <span className="font-medium">một trường phái cụ thể</span>, không phải
            sự đồng thuận chung giữa các trường phái Bát Tự (xem mục &ldquo;Cường độ Nhật Chủ&rdquo;
            trong phần luận giải chưa cung cấp bên dưới). Giả định lá số thuộc dạng bình thường —
            các cách cục đặc biệt (tòng cách…) chưa được hệ thống này nhận diện.
          </p>

          <div className="mb-3 flex flex-wrap items-center gap-2">
            <span
              className={`rounded-full px-3 py-1 text-sm font-semibold ${
                dayMasterStrength.vuong === true
                  ? "bg-emerald-100 text-emerald-900"
                  : "bg-slate-200 text-slate-800"
              }`}
            >
              {dayMasterStrength.vuong === true ? "Vượng (thân cường)" : "Yếu (thân nhược)"}
            </span>
            <span className="text-xs text-slate-500">
              Phe mình {String(dayMasterStrength.ownSideDegrees)} /{" "}
              {String(dayMasterStrength.totalDegrees)} độ ({" "}
              {dayMasterStrength.totalDegrees
                ? Math.round(
                    (Number(dayMasterStrength.ownSideDegrees) /
                      Number(dayMasterStrength.totalDegrees)) *
                      1000,
                  ) / 10
                : 0}
              % · ngưỡng 40%)
            </span>
            <span className="text-xs text-slate-500">
              Nắm lệnh: {label(labels, "FiveElement", dayMasterStrength.seasonalElement)}
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full min-w-[28rem] border-collapse text-sm">
              <thead>
                <tr className="text-left text-xs uppercase tracking-wide text-slate-500">
                  {ELEMENT_ORDER.map((element) => (
                    <th key={element} scope="col" className="py-1.5 font-medium">
                      {label(labels, "FiveElement", element)}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                <tr>
                  {ELEMENT_ORDER.map((element) => (
                    <td key={element} className="py-1.5 tabular-nums text-slate-800">
                      {asCounts(dayMasterStrength.elementDegrees)[element] ?? 0}°
                    </td>
                  ))}
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      )}

      {blocked.length > 0 && (
        <div>
          <h3 className="text-sm font-semibold text-slate-900">
            Phần luận giải chưa được cung cấp ({blocked.length})
          </h3>
          <p className="mb-2 text-xs text-slate-500">
            Những phần dưới đây bị bỏ trống có chủ đích, không phải do lỗi hay thiếu dữ liệu của
            bạn.
          </p>
          <ul className="space-y-2">
            {blocked.map((item) => (
              <li key={item.evidenceId} className="rounded-md border border-amber-200 bg-amber-50 p-3">
                <div className="flex flex-wrap items-baseline gap-2">
                  <span className="text-sm font-medium text-amber-900">
                    {String(item.fact.displayNameVi ?? "")}
                  </span>
                  <span
                    title={`Mục nghiên cứu ${String(item.fact.researchId ?? "")}`}
                    className="rounded-full bg-amber-200 px-2 py-0.5 text-xs font-medium text-amber-900"
                  >
                    Cần xác minh thuật toán · {String(item.fact.researchId ?? "")}
                  </span>
                </div>
                <p className="mt-1 text-xs text-amber-900">{String(item.fact.reasonVi ?? "")}</p>
                {asStringArray(item.fact.knownVariants).length > 0 && (
                  <p className="mt-1 text-xs text-amber-800">
                    Các cách làm khác nhau đang tồn tại:{" "}
                    {asStringArray(item.fact.knownVariants).join(" · ")}
                  </p>
                )}
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  );
}
