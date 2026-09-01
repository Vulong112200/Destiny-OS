import { Suspense } from "react";
import Link from "next/link";
import { fetchLabels, findCalculation } from "@/lib/api";
import { asSupportedScenario } from "@/lib/scenarioMeta";
import { NarrativePanel, NarrativePanelSkeleton } from "@/components/NarrativePanel";
import { ResultHero } from "@/components/ResultHero";
import { ResultSidebar } from "@/components/ResultSidebar";
import { ResultView } from "@/components/ResultView";

/**
 * Hạn thực thi của route này, tính bằng giây.
 *
 * <p>`NarrativePanel` là async server component nên nó chạy **trên server
 * của Next**, và hạn mặc định của một serverless function thường là 10–15
 * giây. Chuỗi model của backend cộng deadline tổng của nó có thể mất tới ~60
 * giây một cách hợp lệ, nên nếu không khai báo gì thì trên môi trường deploy
 * phần diễn giải bị cắt trước khi backend kịp trả lời — mỗi lần, và không
 * kèm lỗi nào nhìn thấy được.
 *
 * <p>90 giây = hạn chờ client cho narrative (75s, đã bao trọn deadline tổng
 * 45s của backend) + chỗ dư cho cold start. Đặt lớn hơn hạn chờ trong
 * `lib/api.ts` là có chủ ý: bên
 * nào có hạn chờ ngắn nhất mới là bên nên kết thúc trước, và ta muốn đó là
 * lệnh gọi API — nó biết vì sao nó thất bại — chứ không phải nền tảng, thứ
 * chỉ biết trả về 504.
 */
export const maxDuration = 90;

export default async function ResultPage({
  params,
}: {
  params: Promise<{ calculationId: string }>;
}) {
  const { calculationId } = await params;
  // Both are independent and both are fast, so pay for one round trip rather
  // than two. The narrative is deliberately NOT awaited here - it streams in
  // separately below, because it is the one call that can take tens of
  // seconds (provider model fallback chain) and nothing on this page should
  // wait on it.
  const [result, labels] = await Promise.all([
    findCalculation(calculationId),
    fetchLabels(),
  ]);

  if (!result) {
    return (
      <div className="mx-auto max-w-2xl space-y-4 py-12 text-center">
        <h1 className="text-2xl font-bold">Không tìm thấy lần tính này</h1>
        <p className="text-slate-600">
          Mã lần tính <span className="font-mono">{calculationId}</span> không tồn tại. Không có
          kết quả nào được tạo ra để thay thế.
        </p>
        <Link href="/trung-tam-quyet-dinh" className="font-medium underline">
          Quay lại Trung tâm quyết định
        </Link>
      </div>
    );
  }

  const scenario = asSupportedScenario(result.scenarioId);

  // Only offer a jump link to a section that will actually render, so the
  // sidebar never points at an anchor that does not exist on this result.
  // "dien-giai" is unconditional: the narrative slot always renders something
  // at that id - the panel, its skeleton, or an honest "could not generate".
  const sections = [
    { id: "tra-loi", label: "Trả lời cho điều bạn hỏi" },
    { id: "dien-giai", label: "Tổng kết bằng lời văn" },
    ...(result.fusion && result.fusion.conflicts.length > 0
      ? [{ id: "mau-thuan", label: "Điểm khác biệt" }]
      : []),
    { id: "du-lieu", label: "Dữ liệu tính toán" },
    { id: "bang-chung", label: "Vì sao có kết quả này?" },
  ];

  return (
    <div className="space-y-6">
      <ResultHero result={result} scenario={scenario} />
      {/*
        Two columns from `lg` up: the reading in the main column, and the
        reference material (retention, engines, ids, jump links) in a sticky
        rail beside it. Below `lg` the rail stacks under the content, which is
        the right order on a phone - the answer before the housekeeping.
      */}
      <div className="grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_20rem] xl:gap-8">
        <div className="min-w-0">
          <ResultView
            result={result}
            labels={labels}
            narrativeSlot={
              <Suspense fallback={<NarrativePanelSkeleton />}>
                <NarrativePanel calculationId={calculationId} />
              </Suspense>
            }
          />
        </div>
        <ResultSidebar result={result} sections={sections} />
      </div>
    </div>
  );
}
