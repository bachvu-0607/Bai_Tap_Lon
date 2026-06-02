package com.uet.domain.result;

import java.io.Serializable;

/**
 * Lớp DTO trả về kết quả (Result) từ Server cho Client sau khi thực hiện thao tác
 * liên quan đến quản lý phiên đấu giá (Ví dụ: Admin duyệt/từ chối phiên đấu giá).
 */
public class AuctionActionResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Cờ đánh dấu thao tác thành công (true) hay thất bại (false) */
    private final boolean success;
    
    /** Thông điệp chi tiết trả về cho Client (Ví dụ: "Duyệt thành công", hoặc thông báo lỗi) */
    private final String message;

    /**
     * Khởi tạo đối tượng kết quả ẩn (chỉ được gọi qua các phương thức tĩnh).
     * 
     * @param success Trạng thái thành công.
     * @param message Thông điệp kèm theo.
     */
    private AuctionActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Tạo một kết quả báo cáo thao tác đã thành công.
     * 
     * @param message Lời nhắn thành công.
     * @return Đối tượng AuctionActionResult đại diện cho thành công.
     */
    public static AuctionActionResult success(String message) {
        return new AuctionActionResult(true, message);
    }

    /**
     * Tạo một kết quả báo cáo thao tác đã thất bại.
     * 
     * @param message Thông báo lỗi nguyên nhân thất bại.
     * @return Đối tượng AuctionActionResult đại diện cho thất bại.
     */
    public static AuctionActionResult failed(String message) {
        return new AuctionActionResult(false, message);
    }

    /**
     * Kiểm tra xem thao tác có thành công hay không.
     * 
     * @return true nếu thành công.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Lấy thông điệp chi tiết trả về từ Server.
     * 
     * @return Chuỗi thông điệp.
     */
    public String getMessage() {
        return message;
    }
}
