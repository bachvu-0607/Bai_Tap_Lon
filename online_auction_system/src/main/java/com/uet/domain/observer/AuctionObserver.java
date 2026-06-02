package com.uet.domain.observer;

import com.uet.domain.entity.auction.Auction;

/**
 * Interface đại diện cho một Đối tượng quan sát (Observer) trong mô hình Observer Pattern.
 * Bất kỳ lớp nào muốn "lắng nghe" và cập nhật khi một phiên đấu giá (Auction) có sự thay đổi 
 * thì đều phải thực thi interface này.
 */
public interface AuctionObserver {
    
    /**
     * Phương thức được gọi tự động (hoặc kích hoạt thủ công) khi đối tượng bị quan sát có sự kiện mới 
     * (như đổi giá, thay đổi người thắng, hết thời gian).
     * 
     * @param auction Phiên đấu giá đã có sự thay đổi, chứa đựng thông tin cập nhật mới nhất.
     */
    void update(Auction auction);
}
