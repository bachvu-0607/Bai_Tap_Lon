package com.uet.client.utils;

import com.uet.models.User;

public class SessionManager {
    // Biến static này giữ object User sống mãi trong suốt vòng đời của App Client
    public static User currentUser;

    // Hàm tiện ích để đăng xuất
    public static void clearSession() {
        currentUser = null;
    }
    
    // Hàm kiểm tra xem đã đăng nhập chưa
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}