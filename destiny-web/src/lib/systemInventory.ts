/**
 * Chín hệ thống trong đặc tả, và hệ nào đang thật sự chạy được.
 *
 * <p>**Bản sao chép tay** của `DESTINY_OS_MASTER_SPECIFICATION.md` §0/§1, cùng
 * kiểu với `scenarioMeta.ts` và `types.ts`. `GET /api/v1/methodologies` không
 * trả về engine id, nên không có cách nào ghép registry với engine hoàn toàn tự
 * động; sửa đúng gốc là thêm trường `engineId` vào `MethodologyDto`.
 *
 * <p>Tồn tại vì ba con số khác nhau đang cùng đúng và không chỗ nào nói ra:
 * đặc tả có **9** hệ, registry có **18** mục (tách phần lập lá số và phần luận
 * giải), code có **6** engine. Người dùng đếm được số ô chọn trên form và kết
 * luận rằng đó là tất cả những gì hệ thống có.
 */

export interface SystemEntry {
  id: string;
  nameVi: string;
  domain: "EASTERN" | "WESTERN" | "OTHER";
  /** `null` nghĩa là chưa có engine nào tồn tại trong code. */
  engineId: string | null;
  /** Các mục trong registry phương pháp ứng với hệ này. */
  methodologyIds: string[];
  /** Có ô chọn ở Trung tâm quyết định hay không. */
  formToggle: boolean;
  /** Một dòng nói thật về trạng thái. */
  stateVi: string;
}

export const SYSTEM_INVENTORY: SystemEntry[] = [
  {
    id: "BAZI",
    nameVi: "Bát Tự (Tứ Trụ)",
    domain: "EASTERN",
    engineId: "BAZI",
    methodologyIds: ["BAZI_TUBINH_CHART", "BAZI_DAY_MASTER_STRENGTH_TVH", "BAZI"],
    formToggle: true,
    stateVi:
      "Lập được lá số Tứ Trụ, Thập Thần, Tàng Can và Đại Vận. Phần luận giải (Dụng Thần) còn chờ xác minh trường phái, nên hệ này chưa phát tín hiệu vào kết luận tổng hợp.",
  },
  {
    id: "ZIWEI",
    nameVi: "Tử Vi Đẩu Số",
    domain: "EASTERN",
    engineId: null,
    methodologyIds: ["ZIWEI"],
    formToggle: false,
    stateVi:
      "Chưa có engine. Cách an sao còn khác nhau giữa các trường phái ở mức không thể chọn bừa, và đây là mục nghiên cứu nghiêm trọng cuối cùng còn mở.",
  },
  {
    id: "FENGSHUI_KUA",
    nameVi: "Phong Thủy — Bát Trạch (Cung Phi)",
    domain: "EASTERN",
    engineId: "FENGSHUI_KUA",
    methodologyIds: ["FENGSHUI_KUA"],
    formToggle: true,
    stateVi:
      "Chạy đầy đủ và có phát tín hiệu khi bạn nhập hướng nhà hoặc hướng phòng. Phần ứng dụng theo từng phòng và vật dụng (hướng ngủ, hướng bàn làm việc, hướng cửa chính) chưa có nguồn được xác minh nên chưa làm.",
  },
  {
    id: "WESTERN_ASTROLOGY",
    nameVi: "Chiêm tinh học phương Tây",
    domain: "WESTERN",
    engineId: "WESTERN_ASTROLOGY",
    methodologyIds: ["WESTERN_ASTROLOGY_CHART_ANGLES", "WESTERN_ASTROLOGY"],
    formToggle: true,
    stateVi:
      "Tính được Mặt Trời, Thiên Đỉnh, Cung Mọc và 12 nhà. Mặt Trăng, bảy hành tinh còn lại và các góc chiếu chưa được tính — được khai báo rõ trên lá số chứ không bị bỏ qua âm thầm.",
  },
  {
    id: "NUMEROLOGY_PYTHAGOREAN",
    nameVi: "Thần số học (Pythagoras)",
    domain: "OTHER",
    engineId: "NUMEROLOGY_PYTHAGOREAN",
    methodologyIds: ["NUMEROLOGY_PYTHAGOREAN", "NUMEROLOGY_CHALDEAN"],
    formToggle: true,
    stateVi:
      "Chạy đầy đủ với 5 chỉ số và nội dung diễn giải đã soạn. Hệ Chaldean chưa làm vì chưa có cách quy đổi chữ cái tiếng Việt được xác minh.",
  },
  {
    id: "TAROT",
    nameVi: "Tarot (Rider-Waite-Smith)",
    domain: "OTHER",
    engineId: "TAROT",
    methodologyIds: ["TAROT_RWS"],
    formToggle: true,
    stateVi: "Chạy đầy đủ: 6 kiểu trải bài và nội dung diễn giải đủ 78 lá.",
  },
  {
    id: "ICHING",
    nameVi: "Kinh Dịch",
    domain: "EASTERN",
    engineId: "ICHING",
    methodologyIds: [
      "ICHING_HEXAGRAM_CASTING",
      "ICHING_HEXAGRAM_JUDGMENT_NGOTATTO",
      "ICHING_CAT_HUNG_LEXICAL",
      "ICHING_HAO_LAM_CHU_NGUYENHIENLE",
      "ICHING",
    ],
    formToggle: true,
    stateVi:
      "Gieo được quẻ bằng 3 cách, có đủ 64 quẻ từ và 386 hào từ, và đã phát tín hiệu cát/hung. Còn thiếu quy tắc chọn lời chính khi có nhiều hào động.",
  },
  {
    id: "MAIHOA",
    nameVi: "Mai Hoa Dịch Số",
    domain: "EASTERN",
    engineId: null,
    methodologyIds: ["MAIHOA"],
    formToggle: false,
    stateVi:
      "Phần gieo quẻ đã có, nằm chung trong Kinh Dịch. Phần luận Thể/Dụng riêng của Mai Hoa thì chưa — đó mới là thứ làm nên môn này.",
  },
  {
    id: "QIMEN",
    nameVi: "Kỳ Môn Độn Giáp",
    domain: "EASTERN",
    engineId: null,
    methodologyIds: ["QIMEN"],
    formToggle: false,
    stateVi: "Cố ý nằm ngoài phạm vi dự án. Không phải thiếu sót, là một quyết định đã ghi lại.",
  },
];

/** Ba hệ chưa có engine — hiện ra có tên, thay vì biến mất như không tồn tại. */
export const UNBUILT_SYSTEMS = SYSTEM_INVENTORY.filter((s) => s.engineId === null);

export const RUNNABLE_SYSTEMS = SYSTEM_INVENTORY.filter((s) => s.engineId !== null);
