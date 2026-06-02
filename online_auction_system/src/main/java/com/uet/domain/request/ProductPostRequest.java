package com.uet.domain.request;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Lớp DTO bao gói toàn bộ dữ liệu cần thiết khi một người bán (Seller) muốn đăng bán 
 * một sản phẩm mới và tạo một phiên đấu giá tương ứng.
 */
public class ProductPostRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Loại sản phẩm (ví dụ: ELECTRONICS, ART, VEHICLE) */
    private final String productType;
    /** Tên của sản phẩm */
    private final String productName;
    /** Mô tả chi tiết thêm về sản phẩm */
    private final String description;
    /** Giá khởi điểm muốn bắt đầu phiên đấu giá */
    private final double openingPrice;
    /** Bước giá nhảy tối thiểu cho mỗi lần người dùng trả giá */
    private final double minIncrement;
    /** Thời gian dự kiến mở cửa phiên */
    private final LocalDateTime startTime;
    /** Thời gian dự kiến đóng cửa phiên */
    private final LocalDateTime endTime;
    /** Đường dẫn (URL) trỏ tới hình ảnh minh họa cho sản phẩm */
    private final String imageLink;

    /**
     * Hàm khởi tạo yêu cầu đăng bán sản phẩm mới.
     * 
     * @param productType Thể loại sản phẩm (String).
     * @param productName Tên sản phẩm.
     * @param description Mô tả bổ sung.
     * @param openingPrice Giá khởi điểm.
     * @param minIncrement Bước giá yêu cầu.
     * @param startTime Thời gian bắt đầu mở bán.
     * @param endTime Thời gian kết thúc.
     * @param imageLink Link tải hình ảnh sản phẩm.
     */
    public ProductPostRequest(String productType, String productName, String description, double openingPrice, double minIncrement,
                              LocalDateTime startTime, LocalDateTime endTime, String imageLink) {
        this.productType = productType;
        this.productName = productName;
        this.description = description;
        this.openingPrice = openingPrice;
        this.minIncrement = minIncrement;
        this.startTime = startTime;
        this.endTime = endTime;
        this.imageLink = imageLink;
    }

    /** @return Thể loại sản phẩm được yêu cầu. */
    public String getProductType() { return productType; }

    /** @return Tên hiển thị của sản phẩm. */
    public String getProductName() { return productName; }

    /** @return Nội dung mô tả tùy chỉnh. */
    public String getDescription() { return description; }

    /** @return Giá khởi điểm quy định. */
    public double getOpeningPrice() { return openingPrice; }

    /** @return Mức tăng tối thiểu giữa 2 lần đặt giá. */
    public double getMinIncrement() { return minIncrement; }

    /** @return Thời gian khai mạc phiên. */
    public LocalDateTime getStartTime() { return startTime; }

    /** @return Thời gian bế mạc phiên. */
    public LocalDateTime getEndTime() { return endTime; }

    /** @return Đường dẫn ảnh minh họa. */
    public String getImageLink() { return imageLink; }
}
