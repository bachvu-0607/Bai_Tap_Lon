package com.uet.domain.entity.item;

/**
 * Lớp đại diện cho sản phẩm là Phương tiện di chuyển (Vehicle).
 * Kế thừa từ Item, lưu giữ các thuộc tính phân loại riêng như hãng sản xuất và năm sản xuất.
 */
public class Vehicle extends Item {
    
    /** Hãng sản xuất xe (Ví dụ: Honda, Toyota, VinFast, v.v.) */
    private String manufacturer; 
    
    /** Năm sản xuất xe */
    private int yearMade;

    /**
     * Khởi tạo phương tiện mới chỉ với tên và giá khởi điểm.
     * Thuộc tính hãng và năm sản xuất sẽ nhận giá trị mặc định.
     * 
     * @param name Tên phương tiện.
     * @param startingPrice Giá khởi điểm.
     */
    public Vehicle(String name, double startingPrice) {
        super(name, startingPrice);
        this.manufacturer = "Không rõ";
        this.yearMade = 0;
    }

    /**
     * Khởi tạo phương tiện mới với đầy đủ thông tin phân loại.
     * 
     * @param name Tên phương tiện.
     * @param startingPrice Giá khởi điểm.
     * @param manufacturer Hãng sản xuất.
     * @param yearMade Năm sản xuất.
     */
    public Vehicle(String name, double startingPrice, String manufacturer, int yearMade) {
        super(name, startingPrice);
        this.manufacturer = manufacturer;
        this.yearMade = yearMade;
    }

    /**
     * Khởi tạo phương tiện từ dữ liệu có sẵn ID (Dùng khi khôi phục từ Database).
     * 
     * @param id Mã định danh.
     * @param name Tên phương tiện.
     * @param startingPrice Giá khởi điểm.
     */
    public Vehicle(String id, String name, double startingPrice) {
        super(id, name, startingPrice);
        this.manufacturer = "Không rõ";
        this.yearMade = 0;
    }

    /**
     * Khởi tạo phương tiện đầy đủ thông tin từ dữ liệu có sẵn ID (Khôi phục từ DB).
     * 
     * @param id Mã định danh.
     * @param name Tên phương tiện.
     * @param startingPrice Giá khởi điểm.
     * @param manufacturer Hãng sản xuất.
     * @param yearMade Năm sản xuất.
     */
    public Vehicle(String id, String name, double startingPrice, String manufacturer, int yearMade) {
        super(id, name, startingPrice);
        this.manufacturer = manufacturer;
        this.yearMade = yearMade;
    }

    /**
     * Lấy thể loại/danh mục của sản phẩm.
     * 
     * @return Chuỗi cố định "Phương tiện".
     */
    @Override
    public String getCategory() {
        return "Phương tiện";
    }

    /**
     * Lấy mô tả chi tiết của phương tiện.
     * Nếu không có phần mô tả tùy chỉnh, tự động tạo mô tả dựa vào hãng và năm sản xuất.
     * 
     * @return Chuỗi nội dung mô tả.
     */
    @Override
    public String getDescription() {
        if (super.getDescription() != null && !super.getDescription().isBlank()) {
            return super.getDescription();
        }
        return manufacturer + " (" + yearMade + ")";
    }

    /**
     * Trả về thông tin tóm tắt dùng để hiển thị trên giao diện.
     * 
     * @return Chuỗi tóm tắt thông tin phương tiện.
     */
    @Override
    public String toString() {
        return "Phương tiện: " + getName() +
                " (" + manufacturer + " - " + yearMade +
                ") | Giá khởi điểm: " + getStartingPrice() + "$";
    }

    /**
     * Lấy tên hãng sản xuất phương tiện.
     * 
     * @return Hãng sản xuất.
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * Lấy năm sản xuất của phương tiện.
     * 
     * @return Năm sản xuất.
     */
    public int getYearMade() {
        return yearMade;
    }
}
