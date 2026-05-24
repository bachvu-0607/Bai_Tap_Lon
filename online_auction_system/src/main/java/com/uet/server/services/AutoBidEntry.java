package com.uet.server.services;

import java.time.LocalDateTime;

import com.uet.domain.entity.user.Bidder;

/**
 * Lưu thông tin một đăng ký auto-bid của Bidder cho một phiên đấu giá.
 *
 * Class này chỉ tồn tại phía Server (không cần Serializable).
 * AuctionManager giữ một Map<auctionId, PriorityQueue<AutoBidEntry>>.
 *
 * Thứ tự ưu tiên trong PriorityQueue (compareTo):
 *   1. maxBid cao hơn → ưu tiên hơn  (người sẵn sàng trả nhiều hơn thắng)
 *   2. Nếu maxBid bằng nhau → registeredAt sớm hơn → ưu tiên hơn  (đến trước được phục vụ trước)
 */
public class AutoBidEntry implements Comparable<AutoBidEntry> {

    private final String        bidderId;     // ID của bidder (dùng để so sánh)
    private final Bidder        bidder;       // Tham chiếu trực tiếp tới Bidder object trong bộ nhớ
    private final double        maxBid;       // Giá tối đa bidder chấp nhận trả
    private final double        increment;    // Bước giá mỗi lần tự động đặt
    private final LocalDateTime registeredAt; // Thời điểm đăng ký auto-bid

    public AutoBidEntry(Bidder bidder, double maxBid, double increment) {
        this.bidderId     = bidder.getId();
        this.bidder       = bidder;
        this.maxBid       = maxBid;
        this.increment    = increment;
        this.registeredAt = LocalDateTime.now();
    }

    /**
     * Comparator cho PriorityQueue (min-heap mặc định trong Java).
     * Vì PriorityQueue là min-heap, ta cần đảo chiều để maxBid CAO nhất đứng đầu.
     *
     * Quy tắc:
     *   - other.maxBid > this.maxBid → other nhỏ hơn theo so sánh → other đứng trước (ưu tiên cao hơn)
     *   - Nếu bằng nhau → this.registeredAt < other.registeredAt → this đứng trước (đăng ký sớm hơn)
     */
    @Override
    public int compareTo(AutoBidEntry other) {
        // Giảm dần theo maxBid: người sẵn sàng trả nhiều hơn đứng trước
        int cmp = Double.compare(other.maxBid, this.maxBid);
        if (cmp != 0) return cmp;
        // Tăng dần theo thời gian: ai đăng ký trước thì ưu tiên trước
        return this.registeredAt.compareTo(other.registeredAt);
    }

    public String        getBidderId()    { return bidderId; }
    public Bidder        getBidder()      { return bidder; }
    public double        getMaxBid()      { return maxBid; }
    public double        getIncrement()   { return increment; }
    public LocalDateTime getRegisteredAt(){ return registeredAt; }
}
