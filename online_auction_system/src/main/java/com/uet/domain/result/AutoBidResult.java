package com.uet.domain.result;

import java.io.Serializable;

/**
 * Kết quả server trả về sau mỗi thao tác liên quan đến auto-bid:
 *   - Đặt auto-bid   (SET_AUTO_BID)
 *   - Huỷ auto-bid   (CANCEL_AUTO_BID)
 *   - Lấy trạng thái (GET_AUTO_BID)
 *
 * hasActiveBid = true  → bidder đang có auto-bid chạy cho phiên này
 * hasActiveBid = false → bidder chưa đăng ký hoặc đã huỷ
 */
public class AutoBidResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;       // Thao tác thành công hay không
    private final String  message;       // Thông báo hiển thị cho người dùng
    private final boolean hasActiveBid;  // Bidder có auto-bid đang chạy cho phiên này không
    private final double  maxBid;        // Giá tối đa đang được đặt (0 nếu không có)
    private final double  increment;     // Bước giá đang được đặt   (0 nếu không có)

    private AutoBidResult(boolean success, String message,
                          boolean hasActiveBid, double maxBid, double increment) {
        this.success      = success;
        this.message      = message;
        this.hasActiveBid = hasActiveBid;
        this.maxBid       = maxBid;
        this.increment    = increment;
    }

    /** Thành công — kèm thông tin auto-bid hiện tại */
    public static AutoBidResult success(String message, boolean hasActiveBid, double maxBid, double increment) {
        return new AutoBidResult(true, message, hasActiveBid, maxBid, increment);
    }

    /** Thất bại — không kèm thông tin auto-bid */
    public static AutoBidResult failed(String message) {
        return new AutoBidResult(false, message, false, 0, 0);
    }

    public boolean isSuccess()      { return success; }
    public String  getMessage()     { return message; }
    public boolean isHasActiveBid() { return hasActiveBid; }
    public double  getMaxBid()      { return maxBid; }
    public double  getIncrement()   { return increment; }
}
