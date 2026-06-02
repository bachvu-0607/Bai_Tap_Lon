package com.uet.server.services;

import com.uet.domain.AuctionSummary;
import com.uet.domain.entity.auction.Auction;
import com.uet.domain.event.ServerEvent;
import com.uet.domain.event.ServerEventType;
import com.uet.domain.observer.AuctionObserver;

/**
 * Bộ phát thông báo đấu giá thời gian thực.
 * Triển khai interface {@link AuctionObserver} (mẫu thiết kế Observer).
 * Lắng nghe các thay đổi trạng thái hoặc giá từ phiên đấu giá và phát tán (broadcast) sự kiện về phía các Client đang trực tuyến.
 */
public class RealtimeAuctionNotifier implements AuctionObserver{
    private final AuctionManager auctionManager;

    /**
     * Khởi tạo bộ phát thông báo thời gian thực với bộ quản lý đấu giá.
     * 
     * @param auctionManager Đối tượng quản lý đấu giá hệ thống {@link AuctionManager}.
     */
    public RealtimeAuctionNotifier(AuctionManager auctionManager){
        this.auctionManager = auctionManager;
    }

    /**
     * Nhận cập nhật từ phiên đấu giá được quan sát, chuyển đổi thành sự kiện hệ thống
     * và phát tới tất cả Client.
     * 
     * @param auction Đối tượng {@link Auction} có thay đổi.
     */
    @Override
    public void update(Auction auction){
        ServerEvent event = new ServerEvent(ServerEventType.AUCTION_UPDATED, new AuctionSummary(auction));
        auctionManager.broadcast(event);
    }
}
