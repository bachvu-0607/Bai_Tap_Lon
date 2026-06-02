package com.uet.domain.entity.item;

import com.uet.domain.entity.Entity;
import com.uet.domain.enums.ItemStatus;

/**
 * Lớp trừu tượng đại diện cho một Vật phẩm/Sản phẩm (Item) tham gia đấu giá.
 * Lớp này lưu trữ các thông tin chung của sản phẩm như tên, giá khởi điểm, trạng thái và hình ảnh.
 */
public abstract class Item extends Entity {

    private String name;
    private String description;
    private String imageLink;
    private double startingPrice;
    private ItemStatus status;


    /**
     * Khởi tạo một vật phẩm mới với trạng thái ban đầu là Sẵn sàng (AVAILABLE).
     */
    public Item() {
        super();
        this.status = ItemStatus.AVAILABLE;
    }

    /**
     * Khởi tạo một vật phẩm với tên và giá khởi điểm.
     * 
     * @param name Tên sản phẩm.
     * @param startingPrice Giá khởi điểm.
     */
    public Item(String name, double startingPrice) {
        super();
        this.name = name;
        this.description = "";
        this.imageLink = "";
        this.startingPrice = startingPrice;
        this.status = ItemStatus.AVAILABLE;
    }

    /**
     * Khởi tạo một vật phẩm bằng ID có sẵn, tên và giá khởi điểm (dùng khi khôi phục từ Database).
     * 
     * @param id Mã định danh duy nhất của sản phẩm.
     * @param name Tên sản phẩm.
     * @param startingPrice Giá khởi điểm.
     */
    public Item(String id, String name, double startingPrice) {
        this(id, name, "", startingPrice);
    }

    /**
     * Khởi tạo một vật phẩm đầy đủ thông tin bằng ID có sẵn (khôi phục từ Database).
     * 
     * @param id Mã định danh duy nhất.
     * @param name Tên sản phẩm.
     * @param description Mô tả chi tiết sản phẩm.
     * @param startingPrice Giá khởi điểm.
     */
    public Item(String id, String name, String description, double startingPrice) {
        super(id);
        this.name = name;
        this.description = description;
        this.imageLink = "";
        this.startingPrice = startingPrice;
        this.status = ItemStatus.AVAILABLE;
    }

    /**
     * Lấy danh mục của sản phẩm. Các lớp kế thừa sẽ định nghĩa thể loại riêng của nó.
     * 
     * @return Tên danh mục (ví dụ: "Điện tử", "Phương tiện").
     */
    public abstract String getCategory();

    /**
     * Lấy thông tin mô tả chi tiết của sản phẩm.
     * 
     * @return Chuỗi mô tả.
     */
    public String getDescription() { return this.description; }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu khởi tạo sản phẩm.
     * Một sản phẩm hợp lệ cần có ID, tên, trạng thái và giá khởi điểm dương.
     * 
     * @return true nếu dữ liệu hợp lệ, false nếu có trường thiếu/sai.
     */
    public boolean isValid() {
        return getId() != null
                && !getId().isBlank()
                && name != null
                && !name.isBlank()
                && startingPrice > 0
                && status != null;
    }

    /**
     * Lấy tên của sản phẩm.
     * 
     * @return Tên sản phẩm.
     */
    public String getName() { return this.name; }
    
    /**
     * Gán hoặc thay đổi tên sản phẩm.
     * 
     * @param name Tên mới.
     */
    public void setName(String name) { this.name = name; }
    
    /**
     * Gán nội dung mô tả chi tiết sản phẩm.
     * 
     * @param description Nội dung mô tả.
     */
    public void setDescription(String description) { this.description = description; }
    
    /**
     * Lấy đường dẫn liên kết hình ảnh minh họa sản phẩm.
     * 
     * @return Đường dẫn hình ảnh.
     */
    public String getImageLink() { return this.imageLink; }
    
    /**
     * Gán đường dẫn hình ảnh cho sản phẩm.
     * 
     * @param imageLink Đường dẫn URL ảnh (nếu null sẽ chuyển thành chuỗi rỗng).
     */
    public void setImageLink(String imageLink) { this.imageLink = imageLink == null ? "" : imageLink; }
    
    /**
     * Lấy giá khởi điểm ban đầu của sản phẩm.
     * 
     * @return Giá khởi điểm.
     */
    public double getStartingPrice() { return this.startingPrice; }
    
    /**
     * Lấy trạng thái hiện hành của sản phẩm.
     * 
     * @return Trạng thái (AVAILABLE, IN_AUCTION, SOLD, v.v.).
     */
    public ItemStatus getStatus() { return this.status; }
    
    /**
     * Cập nhật trạng thái của sản phẩm theo quá trình đấu giá.
     * 
     * @param status Trạng thái mới.
     */
    public void setStatus(ItemStatus status) { this.status = status; }

    /**
     * Cung cấp một chuỗi mô tả chung nhất cho vật phẩm.
     * 
     * @return Chuỗi thông tin tổng hợp.
     */
    @Override
    public String toString() {
        return "Sản phẩm: " + name + " | Giá khởi điểm: " + startingPrice + "$";
    }
}
