package com.uet.domain.request;

import java.io.Serializable;

/**
 * Lớp DTO mang theo thông tin khi người dùng yêu cầu đăng nhập (Sign In).
 */
public class SignInRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Tên đăng nhập (thường là số điện thoại đã đăng ký) */
    private final String username;
    
    /** Mật khẩu của tài khoản */
    private final String password;

    /**
     * Khởi tạo một yêu cầu đăng nhập.
     * 
     * @param username Tài khoản người dùng nhập.
     * @param password Mật khẩu người dùng nhập.
     */
    public SignInRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Lấy tên tài khoản đăng nhập.
     * 
     * @return Chuỗi tên đăng nhập.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Lấy mật khẩu đăng nhập.
     * 
     * @return Chuỗi mật khẩu.
     */
    public String getPassword() {
        return password;
    }
}
