package com.uet.server.services;

import java.time.LocalDateTime;

import com.uet.domain.entity.user.Bidder;

/**
 * Lưu thông tin một đăng ký auto-bid của Bidder cho một phiên đấu giá.
 *
 * ── Vai trò trong hệ thống ────────────────────────────────────────────────
 * AuctionManager giữ một Map<auctionId, PriorityQueue<AutoBidEntry>>.
 * Mỗi phiên đấu giá có một hàng đợi ưu tiên chứa tất cả người đang auto-bid.
 * Khi có bid mới, AuctionManager lấy các entry từ hàng đợi để biết ai cần
 * phản ứng và với giá bao nhiêu.
 *
 * ── Tại sao là PriorityQueue? ─────────────────────────────────────────────
 * PriorityQueue sắp xếp tự động theo compareTo():
 *   - maxBid cao hơn → đứng trước (ưu tiên cao hơn)
 *   - Cùng maxBid   → đăng ký trước → đứng trước (đến sớm được phục vụ trước)
 * Điều này đảm bảo người có ngân sách cao nhất luôn là người thắng cuối cùng.
 *
 * ── Class này KHÔNG cần Serializable ─────────────────────────────────────
 * Vì chỉ tồn tại phía Server, không bao giờ truyền qua socket.
 */
public class AutoBidEntry implements Comparable<AutoBidEntry> {

    private final String        bidderId;     // ID của bidder (String, dùng để so sánh và tra cứu)
    private final Bidder        bidder;       // Tham chiếu trực tiếp tới Bidder object đang online
                                             // (để gọi canAfford(), lockFunds(), v.v. mà không cần DB)
    private final double        maxBid;       // Giá tối đa bidder chấp nhận trả — hệ thống KHÔNG tự đặt vượt quá con số này
    private final double        increment;    // Bước giá mỗi lần hệ thống tự đặt (có thể khác bước giá tối thiểu của phiên)
    private final LocalDateTime registeredAt; // Thời điểm đăng ký — dùng làm tie-breaker khi maxBid bằng nhau

    /**
     * Tạo một entry auto-bid mới.
     * Thời điểm đăng ký (registeredAt) được ghi lại ngay tại đây = LocalDateTime.now().
     *
     * @param bidder    Đối tượng Bidder đang yêu cầu auto-bid
     * @param maxBid    Giá tối đa bidder sẵn sàng trả
     * @param increment Bước giá mỗi lần hệ thống tự động đặt
     */
    public AutoBidEntry(Bidder bidder, double maxBid, double increment) {
        this.bidderId     = bidder.getId();  // Lưu ID riêng để so sánh nhanh mà không cần gọi object
        this.bidder       = bidder;          // Lưu reference để gọi các method của Bidder
        this.maxBid       = maxBid;
        this.increment    = increment;
        this.registeredAt = LocalDateTime.now(); // Thời điểm đăng ký = bây giờ (server time)
    }

    /**
     * Comparator cho PriorityQueue.
     *
     * ── Lưu ý kỹ thuật: Java PriorityQueue là MIN-HEAP ────────────────────
     * Nghĩa là phần tử "nhỏ nhất" theo compareTo() sẽ đứng đầu hàng đợi.
     * Ta muốn phần tử CÓ maxBid CAO NHẤT đứng đầu → cần đảo chiều so sánh.
     *
     * ── Quy tắc trả về: ───────────────────────────────────────────────────
     *   compareTo trả về âm  → this < other → this đứng TRƯỚC trong min-heap → ưu tiên cao hơn
     *   compareTo trả về dương → this > other → this đứng SAU
     *
     * ── Ví dụ: ────────────────────────────────────────────────────────────
     *   A.maxBid = 500, B.maxBid = 300
     *   A.compareTo(B) = Double.compare(300, 500) = âm → A đứng trước → A ưu tiên cao hơn ✓
     *
     *   A.maxBid = B.maxBid = 500
     *   A đăng ký lúc 10:00, B đăng ký lúc 10:05
     *   A.compareTo(B) = A.registeredAt.compareTo(B.registeredAt) = âm → A đứng trước → A ưu tiên ✓
     */
    @Override
    public int compareTo(AutoBidEntry other) {
        // So sánh GIẢM DẦN theo maxBid: other.maxBid so với this.maxBid
        // → nếu other.maxBid > this.maxBid, kết quả dương → other "lớn hơn" → this đứng trước
        int cmp = Double.compare(other.maxBid, this.maxBid);
        if (cmp != 0) return cmp;

        // Nếu maxBid bằng nhau → so sánh TĂNG DẦN theo registeredAt (ai đăng ký trước thì ưu tiên)
        return this.registeredAt.compareTo(other.registeredAt);
    }

    // ── Getters ────────────────────────────────────────────────────────────
    public String        getBidderId()     { return bidderId; }
    public Bidder        getBidder()       { return bidder; }
    public double        getMaxBid()       { return maxBid; }
    public double        getIncrement()    { return increment; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
}
