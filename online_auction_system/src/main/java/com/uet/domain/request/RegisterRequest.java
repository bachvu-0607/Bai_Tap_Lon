package com.uet.domain.request;

import java.io.Serializable;

/**
 * Lớp DTO chứa các thông tin đăng ký (Register) tài khoản mới của người dùng.
 */
public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Tên đầy đủ của người dùng (Ví dụ: Nguyễn Văn A) */
    private final String name;
    
    /** Số điện thoại liên lạc (thường dùng làm Username đăng nhập) */
    private final String phone;
    
    /** Số Căn cước công dân / CMND (dùng để định danh duy nhất) */
    private final String citizenId;
    
    /** Mật khẩu dùng để truy cập hệ thống */
    private final String password;
    
    /** Địa chỉ sinh sống hiện tại */
    private final String address;
    
    /** Vai trò muốn đăng ký (Ví dụ: BIDDER hoặc SELLER) */
    private final String role;

    /**
     * Khởi tạo một đối tượng yêu cầu đăng ký người dùng mới.
     * 
     * @param name Tên người dùng.
     * @param phone Số điện thoại (tài khoản đăng nhập).
     * @param citizenId Số CCCD định danh.
     * @param password Mật khẩu.
     * @param address Địa chỉ.
     * @param role Vai trò mong muốn.
     */
    public RegisterRequest(String name, String phone, String citizenId, String password, String address, String role) {
        this.name = name;
        this.phone = phone;
        this.citizenId = citizenId;
        this.password = password;
        this.address = address;
        this.role = role;
    }

    /** @return Tên đầy đủ. */
    public String getName() { return name; }

    /** @return Số điện thoại đăng nhập. */
    public String getPhone() { return phone; }

    /** @return Số CCCD. */
    public String getCitizenId() { return citizenId; }

    /** @return Mật khẩu đăng ký. */
    public String getPassword() { return password; }

    /** @return Địa chỉ. */
    public String getAddress() { return address; }

    /** @return Chức vụ đăng ký mong muốn. */
    public String getRole() { return role; }
}
