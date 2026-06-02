package com.uet.domain.entity.item;

/**
 * Lớp đại diện cho sản phẩm là Tác phẩm Nghệ thuật (Art).
 * Kế thừa từ Item, bổ sung các đặc tính đặc thù của tranh/tác phẩm nghệ thuật
 * như họa sĩ, năm sáng tác và chất liệu.
 */
public class Art extends Item {
    
    /** Họa sĩ/Nghệ sĩ sáng tác tác phẩm */
    private String artist;
    
    /** Năm sáng tác */
    private int yearCreated;
    
    /** Chất liệu sáng tác (Ví dụ: Sơn dầu, Acrylic, Màu nước...) */
    private String medium; 

    /**
     * Khởi tạo một tác phẩm nghệ thuật mới chỉ với tên và giá khởi điểm.
     * Các thuộc tính riêng sẽ nhận giá trị mặc định.
     * 
     * @param name Tên tác phẩm.
     * @param startingPrice Giá khởi điểm.
     */
    public Art(String name, double startingPrice) {
        super(name, startingPrice);
        this.artist = "Không rõ";
        this.yearCreated = 0;
        this.medium = "Không rõ";
    }

    /**
     * Khởi tạo một tác phẩm nghệ thuật mới với đầy đủ thông tin.
     * 
     * @param name Tên tác phẩm.
     * @param startingPrice Giá khởi điểm.
     * @param artist Tên nghệ sĩ/họa sĩ.
     * @param yearCreated Năm ra đời.
     * @param medium Chất liệu.
     */
    public Art(String name, double startingPrice, String artist, int yearCreated, String medium) {
        super(name, startingPrice);
        this.artist = artist;
        this.yearCreated = yearCreated;
        this.medium = medium;
    }

    /**
     * Khởi tạo tác phẩm với ID biết trước (Dùng khi lấy từ DB) và giá trị mặc định.
     * 
     * @param id Mã định danh sản phẩm.
     * @param name Tên tác phẩm.
     * @param startingPrice Giá khởi điểm.
     */
    public Art(String id, String name, double startingPrice) {
        super(id, name, startingPrice);
        this.artist = "Không rõ";
        this.yearCreated = 0;
        this.medium = "Không rõ";
    }

    /**
     * Khởi tạo tác phẩm đầy đủ thông tin với ID biết trước (Dùng khi lấy từ DB).
     * 
     * @param id Mã định danh sản phẩm.
     * @param name Tên tác phẩm.
     * @param startingPrice Giá khởi điểm.
     * @param artist Tên nghệ sĩ/họa sĩ.
     * @param yearCreated Năm ra đời.
     * @param medium Chất liệu.
     */
    public Art(String id, String name, double startingPrice, String artist, int yearCreated, String medium) {
        super(id, name, startingPrice);
        this.artist = artist;
        this.yearCreated = yearCreated;
        this.medium = medium;
    }

    /**
     * Lấy thể loại/danh mục của sản phẩm.
     * 
     * @return Chuỗi cố định "Nghệ thuật".
     */
    @Override
    public String getCategory() {
        return "Nghệ thuật";
    }

    /**
     * Lấy mô tả về sản phẩm.
     * Nếu không có mô tả tuỳ chỉnh nào được thiết lập, sẽ tự động sinh ra chuỗi mô tả 
     * ghép từ thông tin nghệ sĩ, năm ra đời và chất liệu.
     * 
     * @return Chuỗi mô tả chi tiết sản phẩm.
     */
    @Override
    public String getDescription() {
        if (super.getDescription() != null && !super.getDescription().isBlank()) {
            return super.getDescription();
        }
        return "Tác phẩm '" + getName() + "' của " + artist + " (" + yearCreated + ") - " + medium;
    }

    /**
     * Sinh ra chuỗi tóm tắt thông tin của tác phẩm nghệ thuật.
     * 
     * @return Chuỗi tóm tắt kèm giá khởi điểm.
     */
    @Override
    public String toString() {
        return getDescription() + " | Giá khởi điểm: " + getStartingPrice() + "$";
    }

    /**
     * Lấy tên tác giả của tác phẩm.
     * 
     * @return Tên nghệ sĩ/họa sĩ.
     */
    public String getArtist() { return artist; }
    
    /**
     * Lấy năm hoàn thành tác phẩm.
     * 
     * @return Năm sáng tác.
     */
    public int getYearCreated() { return yearCreated; }
    
    /**
     * Lấy thông tin chất liệu tạo nên tác phẩm.
     * 
     * @return Chất liệu sử dụng.
     */
    public String getMedium() { return medium; }
}
