import { DecisionCenterForm } from "@/components/DecisionCenterForm";

export default function DecisionCenterPage() {
  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Trung tâm quyết định</h1>
        <p className="mt-1 text-slate-600">Bạn đang muốn xem điều gì?</p>
      </div>
      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <DecisionCenterForm />
      </div>
    </div>
  );
}
