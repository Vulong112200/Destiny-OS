import type { Metadata } from "next";
import Link from "next/link";
import { Geist, Geist_Mono } from "next/font/google";
import { Disclaimer } from "@/components/Disclaimer";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Destiny OS",
  description: "Nền tảng tính toán huyền học tất định và hỗ trợ ra quyết định theo kịch bản.",
};

// UI_UX_VIETNAMESE_SPEC section 2's 13-item nav. Only 3 are backed by a
// real API today (see docs/UI_UX_VIETNAMESE_SPEC.md and this session's
// frontend plan) - the rest are named honestly as "Sắp ra mắt" rather than
// linked to a page with nothing real behind it.
const NAV_ITEMS: { href: string; label: string }[] = [
  { href: "/", label: "Tổng quan" },
  { href: "/trung-tam-quyet-dinh", label: "Trung tâm quyết định" },
  { href: "/lich-su", label: "Lịch sử" },
];

const COMING_SOON_ITEMS = [
  "Hồ sơ",
  "Phương Đông",
  "Phương Tây",
  "Chu kỳ",
  "Dòng thời gian",
  "Tương hợp",
  "Tarot",
  "Kinh Dịch",
  "Báo cáo",
  "Cài đặt",
];

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html
      lang="vi"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="flex min-h-full flex-col bg-slate-50 text-slate-900">
        <header className="border-b border-slate-200 bg-white">
          <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4">
            <Link href="/" className="text-lg font-bold">
              Destiny OS
            </Link>
            <nav className="flex flex-wrap items-center gap-1">
              {NAV_ITEMS.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className="rounded-md px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
                >
                  {item.label}
                </Link>
              ))}
              {COMING_SOON_ITEMS.map((label) => (
                <span
                  key={label}
                  title="Chưa có hệ thống hỗ trợ phía sau — chưa xây dựng để tránh giao diện trông như hoạt động nhưng thực chất không có gì"
                  className="rounded-md px-3 py-2 text-sm text-slate-400"
                >
                  {label} <span className="text-xs">(sắp ra mắt)</span>
                </span>
              ))}
            </nav>
          </div>
        </header>
        <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-8">{children}</main>
        <footer className="border-t border-slate-200 bg-white">
          <Disclaimer />
        </footer>
      </body>
    </html>
  );
}
