package com.uet.domain.result;

import java.io.Serializable;

/**
 * Lớp DTO chứa kết quả trả về từ Server sau khi một người bán gửi yêu cầu đăng sản phẩm mới.
 */
public class ProductPostResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Cờ hiệu báo cho Client biết việc đăng bài có thành công hay không */
    private final boolean success;
    
    /** Thông điệp đính kèm (Ví dụ: "Đã nộp đơn chờ Admin duyệt") */
    private final String message;

    /**
     * Khởi tạo kết quả ẩn.
     */
    private ProductPostResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Trả về kết quả xác nhận đã gửi đơn đăng bán thành công.
     * 
     * @param message Thông điệp.
     * @return Đối tượng ProductPostResult cờ true.
     */
    public static ProductPostResult success(String message) {
        return new ProductPostResult(true, message);
    }

    /**
     * Trả về kết quả thông báo việc đăng bán bị lỗi.
     * 
     * @param message Lý do lỗi (dữ liệu sai, v.v.).
     * @return Đối tượng ProductPostResult cờ false.
     */
    public static ProductPostResult failed(String message) {
        return new ProductPostResult(false, message);
    }

    /** @return true nếu thao tác đăng thành công. */
    public boolean isSuccess() { return success; }

    /** @return Nội dung chi tiết Server phản hồi lại. */
    public String getMessage() { return message; }
}
