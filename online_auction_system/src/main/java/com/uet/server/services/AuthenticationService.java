package com.uet.server.services;

import com.uet.domain.result.AuthenticationResult;
import com.uet.domain.entity.user.User;
import com.uet.server.repositories.UserRepository;

/**
 * Dịch vụ xử lý xác thực người dùng trong hệ thống (Đăng nhập, Đăng ký, Đăng xuất).
 * Tương tác với {@link UserRepository} để truy vấn dữ liệu và {@link AuctionManager} để quản lý trạng thái online.
 */
public class AuthenticationService {
    private final AuctionManager auctionManager = AuctionManager.getInstance();

    /**
     * Thực hiện xác thực đăng nhập người dùng.
     * Kiểm tra thông tin tài khoản và đảm bảo người dùng chưa đăng nhập ở nơi khác.
     * 
     * @param username Tên đăng nhập (Số CCCD hoặc Số điện thoại).
     * @param password Mật khẩu tài khoản.
     * @return Đối tượng {@link AuthenticationResult} chứa thông tin kết quả xác thực.
     */
    public AuthenticationResult login(String username, String password) {
        User user = UserRepository.checkSignIn(username, password);
        if (user == null) {
            return AuthenticationResult.failed(AuthenticationResult.INVALID_CREDENTIALS);
        }

        boolean canLogin = auctionManager.SignIn(user.getId());
        if (!canLogin) {
            return AuthenticationResult.failed(AuthenticationResult.ALREADY_LOGGED_IN);
        }

        return AuthenticationResult.success(user);
    }

    /**
     * Thực hiện đăng ký tài khoản người dùng mới.
     * Kiểm tra trùng lặp Số CCCD hoặc Số điện thoại trước khi lưu vào DB.
     * 
     * @param name Họ và tên đầy đủ.
     * @param phone Số điện thoại.
     * @param citizenId Số Căn cước công dân.
     * @param password Mật khẩu.
     * @param address Địa chỉ.
     * @param role Vai trò ("Bidder" hoặc "Seller").
     * @return Đối tượng {@link AuthenticationResult} đại diện kết quả đăng ký.
     */
    public AuthenticationResult register(String name, String phone, String citizenId, String password, String address, String role) {
        if (UserRepository.checkCitizenIdExisted(citizenId)) {
            return AuthenticationResult.failed(AuthenticationResult.EXISTED_CITIZEN_ID);
        }

        if (UserRepository.check_phone_existed(phone)) {
            return AuthenticationResult.failed(AuthenticationResult.EXIST_PHONE);
        }

        boolean success = UserRepository.register(name, phone, citizenId, password, address, role);
        return success ? AuthenticationResult.success() : AuthenticationResult.failed(AuthenticationResult.SERVER_ERROR);
    }

    /**
     * Đăng xuất người dùng ra khỏi hệ thống, giải phóng trạng thái online.
     * 
     * @param username Mã định danh hệ thống (System ID) của người dùng cần đăng xuất.
     */
    public void logout(String username) {
        auctionManager.removeUser(username);
    }
}
