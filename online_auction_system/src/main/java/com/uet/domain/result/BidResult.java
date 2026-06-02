package com.uet.domain.result;

import java.io.Serializable;

/**
 * Lớp DTO trả về kết quả cho Client sau khi thực hiện thao tác trả giá (Bid).
 */
public class BidResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Cờ trạng thái: Đặt giá thành công hay không */
    private final boolean success;
    
    /** Thông điệp chi tiết (Ví dụ: "Bạn đang dẫn đầu" hoặc thông báo lỗi) */
    private final String message;

    /**
     * Khởi tạo kết quả đặt giá.
     */
    private BidResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Phản hồi đặt giá hợp lệ và đã được ghi nhận.
     * 
     * @param message Thông điệp thành công.
     * @return Đối tượng BidResult mang cờ true.
     */
    public static BidResult success(String message) {
        return new BidResult(true, message);
    }

    /**
     * Phản hồi đặt giá bị từ chối (Ví dụ: Giá đặt quá thấp, tài khoản hết tiền).
     * 
     * @param message Nguyên nhân thất bại.
     * @return Đối tượng BidResult mang cờ false.
     */
    public static BidResult failed(String message) {
        return new BidResult(false, message);
    }

    /** @return Trạng thái đặt giá (true = thành công). */
    public boolean isSuccess() { return success; }

    /** @return Nội dung phản hồi. */
    public String getMessage() { return message; }
}
