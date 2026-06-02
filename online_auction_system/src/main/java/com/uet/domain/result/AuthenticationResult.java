package com.uet.domain.result;

import java.io.Serializable;
import com.uet.domain.entity.user.User;

/**
 * Lớp DTO trả về kết quả (Result) từ Server cho Client sau khi thực hiện thao tác
 * xác thực (Đăng ký / Đăng nhập).
 */
public class AuthenticationResult implements Serializable {
    private static final long serialVersionUID = 1L;

    // Các hằng số định nghĩa mã lỗi chuẩn để Client dễ dàng kiểm tra.
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String ALREADY_LOGGED_IN = "ALREADY_LOGGED_IN";
    public static final String EXISTED_CITIZEN_ID = "EXISTED_CITIZEN_ID";
    public static final String EXIST_PHONE = "EXIST_PHONE";
    public static final String SERVER_ERROR = "SERVER_ERROR";

    /** Trạng thái xác thực thành công hay không */
    private final boolean success;
    
    /** Thông tin người dùng đăng nhập thành công (sẽ gửi về để Client lưu phiên làm việc) */
    private final User user;
    
    /** Mã lỗi nếu việc xác thực thất bại */
    private final String errorCode;

    /**
     * Hàm khởi tạo kết quả xác thực ẩn.
     */
    private AuthenticationResult(boolean success, User user, String errorCode) {
        this.success = success;
        this.user = user;
        this.errorCode = errorCode;
    }

    /**
     * Tạo kết quả đăng nhập thành công, có kèm thông tin User.
     * 
     * @param user Thông tin tài khoản đăng nhập.
     * @return Đối tượng AuthenticationResult mang cờ true.
     */
    public static AuthenticationResult success(User user) {
        return new AuthenticationResult(true, user, null);
    }

    /**
     * Tạo kết quả xác thực thành công nhưng không cần gửi lại thông tin User (Thường dùng cho Đăng ký).
     * 
     * @return Đối tượng AuthenticationResult mang cờ true.
     */
    public static AuthenticationResult success() {
        return new AuthenticationResult(true, null, null);
    }

    /**
     * Tạo kết quả xác thực thất bại.
     * 
     * @param errorCode Mã lỗi nguyên nhân.
     * @return Đối tượng AuthenticationResult mang cờ false và mã lỗi.
     */
    public static AuthenticationResult failed(String errorCode) {
        return new AuthenticationResult(false, null, errorCode);
    }

    /** @return true nếu đăng nhập/đăng ký thành công. */
    public boolean isSuccess() { return success; }

    /** @return Đối tượng User nếu thành công, null nếu thất bại. */
    public User getUser() { return user; }

    /** @return Mã lỗi giải thích lý do thất bại. */
    public String getErrorCode() { return errorCode; }
}
