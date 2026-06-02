package com.uet.client;

import com.uet.client.core.MainClient;

/**
 * Lớp khởi chạy (Launcher) phụ trợ cho ứng dụng Client.
 * Giúp khởi tạo JavaFX và khởi chạy ứng dụng từ MainClient.
 */
public class Launcher {
    /**
     * Phương thức main chính khởi chạy ứng dụng Client.
     * 
     * @param args Các tham số dòng lệnh.
     */
    public static void main(String[] args) {
        MainClient.main(args);
    }
}

//Đọc kĩ logic đấu giá

//ngrok, chạy trên ip tổng
//lưu vào cloud
//CI/CD (không quan trọng, chỉ cần viết thêm testing)
//gia hạn phiên sau khi có thêm bid mới, bid tự động đấu giá

//realtime update

//admin có cơ chế ban ngừoi dùng, ban trên UI
//hoàn thiện phần wallet (ví) để hiển thị lên UI

// hoàn thiện UI phần điền vào text ( ví dụ như mật khẩu, tên đăng nhập thì cần có form như nào
// có thêm thêm phần database về địa chỉ để chọn ra thành phố, tỉnh)
// thời gian thì tạo cái hộp chọn thời gian (chỉ thời gian chia hết cho 5 và có thể là phải có logic để post product
// trước một thời gian, để admin có thời gian check)
