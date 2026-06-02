package com.uet.domain.entity.user;

import com.uet.domain.exceptions.InvalidAdminActionException;

/**
 * Lớp đại diện cho Quản trị viên (Admin) trong hệ thống.
 * Kế thừa từ lớp User và có thêm các đặc quyền quản trị như khóa tài khoản người dùng khác.
 */
public class Admin extends User{

    /**
     * Khởi tạo một tài khoản Admin mới (ID tự động sinh).
     * 
     * @param citizenId Số Căn cước công dân.
     * @param name Họ và tên đầy đủ.
     * @param phoneNumber Số điện thoại.
     * @param password Mật khẩu.
     * @param address Địa chỉ.
     */
    public Admin(String citizenId, String name, String phoneNumber, String password, String address) {
        super(citizenId, name, phoneNumber, password, address);
    }

    /**
     * Khởi tạo một tài khoản Admin với ID đã có sẵn (dùng khi khôi phục từ cơ sở dữ liệu).
     * 
     * @param id Mã định danh duy nhất.
     * @param citizenId Số Căn cước công dân.
     * @param name Họ và tên đầy đủ.
     * @param phoneNumber Số điện thoại.
     * @param password Mật khẩu.
     * @param address Địa chỉ.
     */
    public Admin(String id, String citizenId, String name, String phoneNumber, String password, String address) {
        super(id, citizenId, name, phoneNumber, password, address);
    }


    /**
     * Thực hiện khóa (ban) một tài khoản người dùng trong hệ thống.
     * Không cho phép khóa tài khoản của một Admin khác.
     * 
     * @param user Đối tượng người dùng cần bị khóa.
     * @throws InvalidAdminActionException Nếu cố gắng khóa một Admin khác hoặc tài khoản đã bị khóa từ trước.
     */
    public void banUser(User user) throws InvalidAdminActionException {
        //Không cho phép Admin tự khóa mình hoặc khóa Admin khác
        if (user instanceof Admin) {
            throw new InvalidAdminActionException("Lỗi quyền hạn: Không thể khóa tài khoản của một Quản trị viên khác!");
        }
        
        //Tài khoản đã bị khóa từ trước
        if (!user.isActive()) {
            throw new InvalidAdminActionException("Tài khoản của người dùng này đã bị khóa từ trước!");
        }

        user.setActive(false);
    }

    /**
     * Trả về chuỗi mô tả thông tin cơ bản của Admin.
     * 
     * @return Chuỗi String chứa thông tin của Quản trị viên.
     */
    @Override
    public String toString() {
        return "Admin: " + getUserName() + " (ID: " + getId() + ") - Quyền hạn: Quản lý hệ thống";
    }

}
