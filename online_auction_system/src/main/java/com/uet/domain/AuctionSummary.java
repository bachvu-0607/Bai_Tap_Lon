package com.uet.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.uet.domain.entity.auction.Auction;

/**
 * Lớp đại diện cho dữ liệu tóm tắt của một phiên đấu giá (Data Transfer Object - DTO).
 * Được sử dụng để truyền tải thông tin cơ bản của phiên đấu giá từ Server về Client,
 * giúp giảm thiểu băng thông mạng so với việc gửi toàn bộ đối tượng Auction phức tạp.
 */
public class AuctionSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String itemName;
    private final String description;
    private final String category;
    private final String sellerName;
    private final String currentWinnerName;
    private final String imageLink;
    private final double currentPrice;
    private final double minIncrement;
    private final double minimumNextBid;
    private final String status;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    /**
     * Khởi tạo một đối tượng AuctionSummary từ một đối tượng Auction gốc.
     * Trích xuất và lưu trữ các thông tin cần thiết để hiển thị trên giao diện.
     * 
     * @param auction Đối tượng phiên đấu giá gốc cần được tóm tắt.
     */
    public AuctionSummary(Auction auction) {
        this.auctionId = auction.getId();
        this.itemName = auction.getItem().getName();
        this.description = auction.getItem().getDescription();
        this.category = auction.getItem().getCategory();
        this.sellerName = auction.getSeller().getName();
        this.currentWinnerName = auction.getWinner() == null ? "-" : auction.getWinner().getName();
        this.imageLink = auction.getItem().getImageLink();
        this.currentPrice = auction.getCurrentMaxPrice();
        this.minIncrement = auction.getMinIncrement();
        this.minimumNextBid = auction.getMinimumNextBid();
        this.status = auction.getStatus().name();
        this.startTime = auction.getStartTime();
        this.endTime = auction.getEndTime();
    }

    /**
     * Lấy mã định danh duy nhất của phiên đấu giá.
     * 
     * @return Mã định danh của phiên đấu giá.
     */
    public String getAuctionId() { return auctionId; }

    /**
     * Lấy tên của sản phẩm đang được đấu giá.
     * 
     * @return Tên sản phẩm.
     */
    public String getItemName() { return itemName; }

    /**
     * Lấy mô tả chi tiết về sản phẩm.
     * 
     * @return Chuỗi mô tả sản phẩm.
     */
    public String getDescription() { return description; }

    /**
     * Lấy danh mục của sản phẩm (ví dụ: Điện tử, Nghệ thuật, Phương tiện).
     * 
     * @return Tên danh mục.
     */
    public String getCategory() { return category; }

    /**
     * Lấy tên của người bán (Seller) đăng bán sản phẩm.
     * 
     * @return Tên người bán.
     */
    public String getSellerName() { return sellerName; }

    /**
     * Lấy tên của người đang trả giá cao nhất (Winner).
     * 
     * @return Tên người chiến thắng hiện tại, hoặc "-" nếu chưa có ai đặt giá.
     */
    public String getCurrentWinnerName() { return currentWinnerName; }

    /**
     * Lấy đường dẫn (URL hoặc path) đến hình ảnh của sản phẩm.
     * 
     * @return Đường dẫn hình ảnh.
     */
    public String getImageLink() { return imageLink; }

    /**
     * Lấy giá cao nhất hiện tại của phiên đấu giá.
     * Nếu chưa có ai đặt, đây sẽ là giá khởi điểm.
     * 
     * @return Giá cao nhất hiện tại.
     */
    public double getCurrentPrice() { return currentPrice; }

    /**
     * Lấy bước giá tối thiểu (số tiền tối thiểu phải cộng thêm cho mỗi lần đặt giá mới).
     * 
     * @return Bước giá tối thiểu.
     */
    public double getMinIncrement() { return minIncrement; }

    /**
     * Lấy giá trị tối thiểu hợp lệ cho lượt đặt giá tiếp theo 
     * (thường bằng giá hiện tại cộng với bước giá).
     * 
     * @return Giá tối thiểu cho lượt đặt tiếp theo.
     */
    public double getMinimumNextBid() { return minimumNextBid; }

    /**
     * Lấy trạng thái hiện tại của phiên đấu giá dưới dạng chuỗi (ví dụ: "OPEN", "RUNNING").
     * 
     * @return Chuỗi đại diện cho trạng thái.
     */
    public String getStatus() { return status; }

    /**
     * Lấy thời điểm bắt đầu của phiên đấu giá.
     * 
     * @return Thời điểm bắt đầu.
     */
    public LocalDateTime getStartTime() { return startTime; }

    /**
     * Lấy thời điểm kết thúc dự kiến của phiên đấu giá.
     * 
     * @return Thời điểm kết thúc.
     */
    public LocalDateTime getEndTime() { return endTime; }
}
