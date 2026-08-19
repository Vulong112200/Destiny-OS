import Link from "next/link";
import { findCalculation } from "@/lib/api";
import { ResultView } from "@/components/ResultView";

export default async function ResultPage({
  params,
}: {
  params: Promise<{ calculationId: string }>;
}) {
  const { calculationId } = await params;
  const result = await findCalculation(calculationId);

  if (!result) {
    return (
      <div className="mx-auto max-w-2xl space-y-4 text-center">
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

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Kết quả — {result.scenarioId}</h1>
      </div>
      <ResultView result={result} />
    </div>
  );
}
