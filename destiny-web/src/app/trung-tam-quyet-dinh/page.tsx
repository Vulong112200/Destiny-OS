import { DecisionCenterForm } from "@/components/DecisionCenterForm";

export default function DecisionCenterPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Trung tâm quyết định</h1>
        <p className="mt-1 max-w-2xl text-slate-600">
          Chọn chủ đề, nói rõ bạn đang phân vân điều gì, rồi bật những hệ bạn muốn dùng. Mỗi hệ
          chỉ trả lời trong phạm vi nó thật sự tính được.
        </p>
      </div>
      <DecisionCenterForm />
    </div>
  );
}
