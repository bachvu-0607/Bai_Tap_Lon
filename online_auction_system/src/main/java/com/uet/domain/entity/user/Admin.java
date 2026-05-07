package com.uet.domain.entity.user;

import com.uet.domain.exceptions.InvalidAdminActionException;

public class Admin extends User{

    public Admin(String citizenId, String name, String phoneNumber, String password, String address) {
        super(citizenId, name, phoneNumber, password, address);
    }

    public Admin(String id, String citizenId, String name, String phoneNumber, String password, String address) {
        super(id, citizenId, name, phoneNumber, password, address);
    }


    //Khoá tài khoản người dùng
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

    @Override
    public String toString() {
        return "Admin: " + getUserName() + " (ID: " + getId() + ") - Quyền hạn: Quản lý hệ thống";
    }

}
