import type { Metadata } from "next";
import { LogViewer } from "@/components/LogViewer";

export const metadata: Metadata = {
  title: "Nhật ký · Destiny OS",
  description: "Nhật ký các lệnh gọi API trong phiên làm việc này, để tự xem và sao chép khi báo lỗi.",
};

export default function NhatKyPage() {
  return (
    <div className="mx-auto w-full max-w-5xl space-y-5 px-4 py-8">
      <header className="space-y-2">
        <h1 className="text-2xl font-semibold text-slate-900">Nhật ký</h1>
        <p className="text-sm leading-relaxed text-slate-600">
          Mọi lệnh gọi tới hệ thống tính toán trong phiên này, kèm mã HTTP, thời lượng và nội dung
          lỗi đầy đủ. Bấm <span className="font-medium">Sao chép toàn bộ</span> để lấy một khối văn
          bản dán thẳng vào khung chat khi cần báo lỗi.
        </p>
      </header>

      {/*
        Nói rõ giới hạn thay vì để người đọc tưởng trang này thấy hết. Các trang
        kết quả là React Server Component, nên fetch của chúng chạy ở phía máy
        chủ Next và không đi qua bộ đệm của trình duyệt; và không có gì ở đây
        nhìn được vào log của backend Java.
      */}
      <section className="rounded-lg border border-slate-200 bg-slate-50 p-4 text-xs leading-relaxed text-slate-600">
        <h2 className="mb-1 text-xs font-semibold text-slate-700">Trang này thấy được gì</h2>
        <ul className="list-inside list-disc space-y-1">
          <li>
            <span className="font-medium">Thấy:</span> các lệnh gọi phát ra từ trình duyệt — chạy
            một lần tính ở Trung tâm quyết định, lưu kết quả, tạo lại phần diễn giải.
          </li>
          <li>
            <span className="font-medium">Không thấy:</span> các lệnh gọi do máy chủ Next thực
            hiện khi dựng trang kết quả (chúng được in ra cửa sổ chạy <code>npm run dev</code>), và
            không thấy nhật ký của backend Java.
          </li>
          <li>
            <span className="font-medium">Không ghi:</span> họ tên, ngày giờ sinh, tọa độ hay câu
            hỏi của bạn — chỉ ghi tên những hệ đã được bật.
          </li>
          <li>Nhật ký nằm trong bộ nhớ phiên của tab, đóng tab là mất.</li>
        </ul>
      </section>

      <LogViewer />
    </div>
  );
}
