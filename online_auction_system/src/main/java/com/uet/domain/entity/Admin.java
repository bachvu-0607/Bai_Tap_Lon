package com.uet.domain.entity;
import java.util.ArrayList;
import com.uet.domain.enums.AuctionStatus;
import com.uet.domain.exceptions.InvalidAdminActionException;

public class Admin extends User{

    public Admin(String userName, String password, String id) {
        super(userName, password, id);
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


    //Buộc hủy một phiên đấu giá.
    public void forceCancelAuction(Auction auction) throws InvalidAdminActionException {
        //Không thể hủy phiên đã thanh toán hoặc đã kết thúc bình thường
        if (auction.getStatus() == AuctionStatus.PAID || auction.getStatus() == AuctionStatus.FINISHED) {
            throw new InvalidAdminActionException("Không thể hủy! Phiên đấu giá này đã kết thúc hoặc đã được thanh toán.");
        }
        
        //Phiên đã bị hủy từ trước
        if (auction.getStatus() == AuctionStatus.CANCELED) {
            throw new InvalidAdminActionException("Phiên đấu giá này đã bị hủy từ trước rồi!");
        }

        //Thực hiện hủy phiên 
        auction.setStatus(AuctionStatus.CANCELED);
    }

    @Override
    public String toString() {
        return "Admin: " + UserName + " (ID: " + id + ") - Quyền hạn: Quản lý hệ thống";
    }

}