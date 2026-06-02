package com.uet.domain.entity.item;

/**
 * Lớp đại diện cho sản phẩm là Thiết bị Điện tử (Electronics).
 * Kế thừa từ Item, lưu giữ các thuộc tính phân loại riêng như thương hiệu và dòng sản phẩm.
 */
public class Electronics extends Item {
    
    /** Thương hiệu sản xuất thiết bị (Ví dụ: Apple, Samsung, Sony, v.v.) */
    private String brand;
    
    /** Tên phiên bản hoặc dòng sản phẩm cụ thể (Ví dụ: iPhone 15 Pro, Galaxy S23) */
    private String model;

    /**
     * Khởi tạo thiết bị điện tử mới chỉ với tên và giá. Thuộc tính riêng nhận giá trị mặc định.
     * 
     * @param name Tên thiết bị.
     * @param startingPrice Giá khởi điểm để đấu giá.
     */
    public Electronics(String name, double startingPrice) {
        super(name, startingPrice);
        this.brand = "Không rõ";
        this.model = "Không rõ";
    }

    /**
     * Khởi tạo thiết bị điện tử mới với đầy đủ các thuộc tính phân loại.
     * 
     * @param name Tên thiết bị.
     * @param startingPrice Giá khởi điểm.
     * @param brand Tên thương hiệu sản xuất.
     * @param model Tên phiên bản, kiểu dáng.
     */
    public Electronics(String name, double startingPrice, String brand, String model) {
        super(name, startingPrice);
        this.brand = brand;
        this.model = model;
    }

    /**
     * Khởi tạo thiết bị điện tử từ dữ liệu có sẵn ID (Dùng lúc khôi phục từ Database).
     * 
     * @param id Mã định danh sản phẩm.
     * @param name Tên thiết bị.
     * @param startingPrice Giá khởi điểm.
     */
    public Electronics(String id, String name, double startingPrice) {
        super(id, name, startingPrice);
        this.brand = "Không rõ";
        this.model = "Không rõ";
    }

    /**
     * Khởi tạo thiết bị điện tử đầy đủ thông tin từ cơ sở dữ liệu.
     * 
     * @param id Mã định danh duy nhất.
     * @param name Tên sản phẩm.
     * @param startingPrice Giá đấu đầu tiên.
     * @param brand Hãng sản xuất.
     * @param model Dòng sản phẩm.
     */
    public Electronics(String id, String name, double startingPrice, String brand, String model) {
        super(id, name, startingPrice);
        this.brand = brand;
        this.model = model;
    }

    /**
     * Cung cấp danh mục chuyên biệt cho sản phẩm.
     * 
     * @return Chuỗi "Điện tử".
     */
    @Override
    public String getCategory() {
        return "Điện tử";
    }

    /**
     * Lấy mô tả của thiết bị. Nếu không có phần mô tả tùy chỉnh, hệ thống sẽ tự sinh 
     * dựa vào tên thương hiệu và phiên bản.
     * 
     * @return Chuỗi nội dung mô tả sản phẩm.
     */
    @Override
    public String getDescription() {
        if (super.getDescription() != null && !super.getDescription().isBlank()) {
            return super.getDescription();
        }
        return brand + " " + model;
    }

    /**
     * Trả về thông tin tóm tắt dùng để hiển thị ngắn gọn lên màn hình.
     * 
     * @return Chuỗi thông tin.
     */
    @Override
    public String toString() {
        return "Điện tử: " + getName() +
                " (" + brand + " - " + model +
                ") | Giá khởi điểm: " + getStartingPrice() + "$";
    }

    /**
     * Lấy thương hiệu của thiết bị.
     * 
     * @return Tên thương hiệu.
     */
    public String getBrand() { return brand; }
    
    /**
     * Lấy kiểu dáng hoặc dòng máy của thiết bị.
     * 
     * @return Tên model sản phẩm.
     */
    public String getModel() { return model; }
}
