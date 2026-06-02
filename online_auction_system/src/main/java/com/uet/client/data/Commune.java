package com.uet.client.data;

/**
 * Lớp đại diện cho một Phường/Xã/Thị trấn trực thuộc một quận/huyện/thị xã trong dữ liệu địa giới hành chính.
 */
public class Commune {
    private String code;
    private String name;
    private String provinceCode;

    /**
     * Lấy mã định danh hành chính của Phường/Xã.
     * 
     * @return Chuỗi mã phường/xã.
     */
    public String getCode() {
        return code;
    }

    /**
     * Lấy tên Phường/Xã sau khi đã loại bỏ khoảng trắng thừa.
     * 
     * @return Tên phường/xã.
     */
    public String getName() {
        return name == null ? "" : name.replaceAll("\\s+", " ").trim();
    }

    /**
     * Lấy mã định danh tỉnh/thành phố quản lý phường/xã này.
     * 
     * @return Mã tỉnh/thành quản lý.
     */
    public String getProvinceCode() {
        return provinceCode;
    }

    /**
     * Trả về chuỗi đại diện hiển thị của Phường/Xã (dùng cho các ComboBox UI).
     * 
     * @return Tên phường/xã.
     */
    @Override
    public String toString() {
        return getName();
    }
}
