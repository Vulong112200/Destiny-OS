/**
 * Tên tiếng Việt cho các khóa trong `EvidenceDto.fact`.
 *
 * <p>`fact` là một map tự do do từng engine tự đặt tên khóa, nên nó không đi
 * kèm nhãn như mọi trường khác của API. Trang kết quả trước đây giải quyết
 * bằng cách đổ thẳng `JSON.stringify(fact, null, 2)` ra màn hình — đúng dữ
 * liệu, nhưng là một khối tiếng Anh viết hoa lạc lõng giữa một trang tiếng
 * Việt, và `UI_UX_VIETNAMESE_SPEC.md` §1 cấm chính điều đó.
 *
 * <p>Khóa nào chưa có ở đây thì hiện nguyên tên kỹ thuật, không phải khoảng
 * trắng — mất nhãn phải làm trang suy giảm, không được làm nó nói dối.
 */
const FACT_KEY_VI: Record<string, string> = {
  // Chung
  note: "Ghi chú",
  source: "Nguồn",
  sourcePage: "Trang nguồn",
  description: "Mô tả",
  displayNameVi: "Tên hiển thị",
  reasonVi: "Lý do",
  researchId: "Mã nghiên cứu",
  sectionId: "Mã mục",
  knownVariants: "Các dị bản đã biết",
  value: "Giá trị",
  type: "Loại",
  label: "Nhãn",
  number: "Số",
  ordinal: "Thứ tự",
  text: "Nội dung",
  keywords: "Từ khóa",
  meaning: "Ý nghĩa",

  // Tarot
  cardId: "Mã lá bài",
  cardName: "Tên lá bài",
  arcana: "Bộ bài",
  suit: "Chất bài",
  orientation: "Chiều lá",
  // Dùng chung: Tarot gọi đây là vị trí trong trải bài, Bát Tự gọi là vị
  // trí trụ (Năm/Tháng/Ngày/Giờ). Nhãn phải trung tính cho cả hai.
  position: "Vị trí",
  positionHasMeaning: "Vị trí có ý nghĩa riêng",
  deckSlot: "Ô trong bộ đã xào",
  selectionMode: "Cách chọn lá",
  uprightKeywords: "Từ khóa khi xuôi",
  reversedKeywords: "Từ khóa khi ngược",
  career: "Sự nghiệp",
  finance: "Tài chính",
  relationship: "Quan hệ",
  decision: "Quyết định",
  general: "Tổng quát",

  // Thần số học
  isMasterNumber: "Là Số Bậc Thầy",
  normalizedName: "Tên đã chuẩn hóa",

  // Bát Tự
  stem: "Thiên Can",
  branch: "Địa Chi",
  stems: "Các Thiên Can",
  branches: "Các Địa Chi",
  stemElement: "Ngũ Hành của Can",
  branchElement: "Ngũ Hành của Chi",
  stemPolarity: "Âm Dương của Can",
  stemTenGod: "Thập Thần của Can",
  hiddenStems: "Tàng Can",
  hiddenStemTenGods: "Thập Thần của Tàng Can",
  hiddenStemRoleOrderingDisputed: "Thứ tự vai Tàng Can còn tranh luận",
  pillars: "Tứ Trụ",
  element: "Ngũ Hành",
  elementDegrees: "Số lần xuất hiện theo hành",
  totalDegrees: "Tổng số lần đếm",
  seasonalElement: "Hành theo mùa",
  ownSideDegrees: "Số lần cùng phe",
  vuong: "Vượng",
  startAgeYears: "Tuổi bắt đầu (năm)",
  startAgeMonths: "Tuổi bắt đầu (tháng)",
  startAgeDays: "Tuổi bắt đầu (ngày)",
  startDate: "Ngày bắt đầu",
  solarTermAtBirth: "Tiết khí lúc sinh",
  solarMonthBranch: "Chi tháng theo tiết khí",
  solarMonthIndex: "Thứ tự tháng theo tiết khí",
  localSolarDateTime: "Giờ mặt trời địa phương",
  hasHourPrecision: "Có giờ sinh chính xác",
  distanceDays: "Khoảng cách (ngày)",
  distanceHours: "Khoảng cách (giờ)",
  boundaryTerm: "Tiết khí ranh giới",
  boundaryInstant: "Thời điểm ranh giới",

  // Phong Thủy
  kuaNumber: "Số Cung Phi",
  trigram: "Quái",
  group: "Nhóm trạch",
  direction: "Hướng",
  facingDirection: "Hướng đang xét",
  relation: "Du Niên",
  auspicious: "Thuộc nhóm cát",
  yearBoundary: "Quy ước ranh giới năm",
  boundaryConventionsAgree: "Hai quy ước năm khớp nhau",
  lapXuanYear: "Năm theo Lập Xuân",
  tetYear: "Năm theo Tết",
  baziYear: "Năm theo Bát Tự",

  // Chiêm tinh
  sign: "Cung hoàng đạo",
  house: "Nhà",
  houseSystem: "Hệ chia nhà",
  zodiacSystem: "Hệ hoàng đạo",
  degreesIntoSign: "Độ trong cung",
  eclipticLongitudeDegrees: "Kinh độ hoàng đạo",
  obliquityDegrees: "Độ nghiêng hoàng đạo",
  ramcDegrees: "RAMC",
  positions: "Các vị trí",

  // Kinh Dịch
  hexagramNumber: "Số quẻ",
  upperTrigram: "Thượng quái",
  lowerTrigram: "Hạ quái",
  lines: "Các hào",
  method: "Cách gieo",
  seed: "Hạt giống ngẫu nhiên",
  hanTu: "Hán tự",
  hanViet: "Hán Việt",
  nghia: "Nghĩa",
  chineseName: "Tên chữ Hán",
  rank: "Bậc",
};

export function factKeyLabelVi(key: string): string {
  return FACT_KEY_VI[key] ?? key;
}
