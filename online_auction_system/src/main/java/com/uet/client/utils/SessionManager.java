package com.uet.client.utils;

import com.uet.domain.entity.user.User;

/**
 * Bộ quản lý phiên làm việc (Session Manager) của phía Client.
 * Lưu trữ thông tin người dùng đang đăng nhập hiện tại trong bộ nhớ ứng dụng.
 */
public class SessionManager {
    /**
     * Thông tin đối tượng người dùng đăng nhập hiện tại trong toàn bộ ứng dụng Client.
     */
    public static User currentUser;

    /**
     * Xóa thông tin phiên đăng nhập hiện tại (khi đăng xuất).
     */
    public static void clearSession() {
        currentUser = null;
    }
    
    /**
     * Kiểm tra xem hiện tại đã có người dùng nào đăng nhập ứng dụng chưa.
     * 
     * @return {@code true} nếu đã đăng nhập; {@code false} nếu chưa.
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
