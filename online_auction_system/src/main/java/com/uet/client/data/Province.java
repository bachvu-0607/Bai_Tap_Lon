package com.uet.client.data;

/**
 * Lớp đại diện cho một Tỉnh/Thành phố trực thuộc trung ương trong dữ liệu địa giới hành chính.
 */
public class Province {
    private String code;
    private String name;

    /**
     * Lấy mã định danh hành chính của Tỉnh/Thành phố.
     * 
     * @return Chuỗi mã tỉnh/thành.
     */
    public String getCode(){
        return this.code;
    }

    /**
     * Lấy tên Tỉnh/Thành phố sau khi đã loại bỏ khoảng trắng thừa.
     * 
     * @return Tên tỉnh/thành.
     */
    public String getName(){
        return this.name == null ? "" : name.replaceAll("\\s+", " ").trim();
    }

    /**
     * Trả về chuỗi đại diện hiển thị của Tỉnh/Thành phố (dùng cho các ComboBox UI).
     * 
     * @return Tên tỉnh/thành.
     */
    @Override
    public String toString(){
        return getName();
    }
}
