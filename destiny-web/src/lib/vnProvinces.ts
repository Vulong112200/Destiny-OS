/**
 * The 34 provincial-level units of Vietnam (28 provinces + 6 municipalities)
 * after the 2025 merger (Nghị quyết Quốc hội, hiệu lực 01/07/2025), each with
 * an approximate latitude/longitude for its administrative center.
 *
 * This exists so a birth-place picker can offer "chọn tỉnh/thành" instead of
 * asking for exact coordinates (most people do not know the lat/long of the
 * hospital they were born in). `formerProvinces` lists the pre-2025 province
 * names folded into each unit, purely so someone searching by the province
 * they remember (e.g. "Sóc Trăng") still finds the right modern entry
 * (now Cần Thơ).
 *
 * These are objective geographic facts (administrative-center coordinates),
 * not metaphysical content — CLAUDE.md's Rule A/C research-gate does not
 * apply here. Precision is city-level, not hospital-level: the UI must say so
 * (a birth place picked this way is accurate to within a province, which is
 * a coarser error than the birth-time uncertainty most users already accept
 * elsewhere in this system).
 */
export interface VnProvince {
  id: string;
  /** Current (post-2025) name, as shown in the picker. */
  name: string;
  /** Pre-2025 province/city names now folded into this unit, for search only. */
  formerProvinces: string[];
  /** Degrees north. */
  latitude: number;
  /** Degrees east. */
  longitude: number;
}

export const VN_PROVINCES: VnProvince[] = [
  { id: "ha_noi", name: "Hà Nội", formerProvinces: [], latitude: 21.0285, longitude: 105.8542 },
  { id: "ho_chi_minh", name: "Thành phố Hồ Chí Minh", formerProvinces: ["Bà Rịa - Vũng Tàu", "Bình Dương"], latitude: 10.7769, longitude: 106.7009 },
  { id: "hai_phong", name: "Hải Phòng", formerProvinces: ["Hải Dương"], latitude: 20.8449, longitude: 106.6881 },
  { id: "da_nang", name: "Đà Nẵng", formerProvinces: ["Quảng Nam"], latitude: 16.0544, longitude: 108.2022 },
  { id: "hue", name: "Huế", formerProvinces: [], latitude: 16.4637, longitude: 107.5909 },
  { id: "can_tho", name: "Cần Thơ", formerProvinces: ["Sóc Trăng", "Hậu Giang"], latitude: 10.0452, longitude: 105.7469 },
  { id: "dong_nai", name: "Đồng Nai", formerProvinces: ["Bình Phước"], latitude: 10.9574, longitude: 106.8426 },
  { id: "cao_bang", name: "Cao Bằng", formerProvinces: [], latitude: 22.6667, longitude: 106.25 },
  { id: "lang_son", name: "Lạng Sơn", formerProvinces: [], latitude: 21.8537, longitude: 106.761 },
  { id: "phu_tho", name: "Phú Thọ", formerProvinces: ["Vĩnh Phúc", "Hòa Bình"], latitude: 21.3227, longitude: 105.4023 },
  { id: "quang_ninh", name: "Quảng Ninh", formerProvinces: [], latitude: 20.95, longitude: 107.0833 },
  { id: "thai_nguyen", name: "Thái Nguyên", formerProvinces: ["Bắc Kạn"], latitude: 21.5942, longitude: 105.848 },
  { id: "tuyen_quang", name: "Tuyên Quang", formerProvinces: ["Hà Giang"], latitude: 21.8236, longitude: 105.2144 },
  { id: "dien_bien", name: "Điện Biên", formerProvinces: [], latitude: 21.3856, longitude: 103.0169 },
  { id: "lai_chau", name: "Lai Châu", formerProvinces: [], latitude: 22.386, longitude: 103.4707 },
  { id: "lao_cai", name: "Lào Cai", formerProvinces: ["Yên Bái"], latitude: 21.7167, longitude: 104.9 },
  { id: "son_la", name: "Sơn La", formerProvinces: [], latitude: 21.3256, longitude: 103.9188 },
  { id: "bac_ninh", name: "Bắc Ninh", formerProvinces: ["Bắc Giang"], latitude: 21.2731, longitude: 106.1946 },
  { id: "hung_yen", name: "Hưng Yên", formerProvinces: ["Thái Bình"], latitude: 20.6464, longitude: 106.0511 },
  { id: "ninh_binh", name: "Ninh Bình", formerProvinces: ["Hà Nam", "Nam Định"], latitude: 20.2506, longitude: 105.9744 },
  { id: "ha_tinh", name: "Hà Tĩnh", formerProvinces: [], latitude: 18.3428, longitude: 105.9057 },
  { id: "nghe_an", name: "Nghệ An", formerProvinces: [], latitude: 18.6796, longitude: 105.6813 },
  { id: "quang_tri", name: "Quảng Trị", formerProvinces: ["Quảng Bình"], latitude: 17.4675, longitude: 106.6222 },
  { id: "thanh_hoa", name: "Thanh Hóa", formerProvinces: [], latitude: 19.8067, longitude: 105.7852 },
  { id: "dak_lak", name: "Đắk Lắk", formerProvinces: ["Phú Yên"], latitude: 12.6667, longitude: 108.05 },
  { id: "gia_lai", name: "Gia Lai", formerProvinces: ["Bình Định"], latitude: 13.783, longitude: 109.2196 },
  { id: "khanh_hoa", name: "Khánh Hòa", formerProvinces: ["Ninh Thuận"], latitude: 12.2451, longitude: 109.1943 },
  { id: "lam_dong", name: "Lâm Đồng", formerProvinces: ["Đắk Nông", "Bình Thuận"], latitude: 11.9404, longitude: 108.4583 },
  { id: "quang_ngai", name: "Quảng Ngãi", formerProvinces: ["Kon Tum"], latitude: 15.1214, longitude: 108.8044 },
  { id: "tay_ninh", name: "Tây Ninh", formerProvinces: ["Long An"], latitude: 10.5333, longitude: 106.4167 },
  { id: "an_giang", name: "An Giang", formerProvinces: ["Kiên Giang"], latitude: 10.0125, longitude: 105.0808 },
  { id: "ca_mau", name: "Cà Mau", formerProvinces: ["Bạc Liêu"], latitude: 9.1769, longitude: 105.15 },
  { id: "dong_thap", name: "Đồng Tháp", formerProvinces: ["Tiền Giang"], latitude: 10.36, longitude: 106.36 },
  { id: "vinh_long", name: "Vĩnh Long", formerProvinces: ["Trà Vinh", "Bến Tre"], latitude: 10.2537, longitude: 105.9722 },
];

/** Case/diacritics-loose search over both the current name and folded-in former province names. */
export function searchVnProvinces(query: string): VnProvince[] {
  const q = normalize(query);
  if (q === "") return VN_PROVINCES;
  return VN_PROVINCES.filter(
    (p) => normalize(p.name).includes(q) || p.formerProvinces.some((f) => normalize(f).includes(q)),
  );
}

function normalize(s: string): string {
  return s
    .toLowerCase()
    .normalize("NFD")
    .replace(/[̀-ͯ]/g, "")
    .replace(/đ/g, "d");
}

export function findVnProvince(id: string): VnProvince | null {
  return VN_PROVINCES.find((p) => p.id === id) ?? null;
}
