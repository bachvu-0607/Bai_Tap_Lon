package com.uet.domain.entity.user;

import com.uet.domain.contract.Payable;
import com.uet.domain.exceptions.InvalidDepositException;

/**
 * Lớp đại diện cho Người bán (Seller) trong hệ thống đấu giá.
 * Kế thừa từ User và thực thi Payable để quản lý ví tiền (nhận doanh thu từ việc bán sản phẩm).
 */
public class Seller extends User implements Payable {
    
    /**
     * Số dư tài khoản hiện tại của người bán.
     */
    private double balance;

    /**
     * Khởi tạo tài khoản Người bán mới (ID tự động sinh, số dư ban đầu là 0).
     * 
     * @param citizenId Số Căn cước công dân.
     * @param name Họ và tên.
     * @param phoneNumber Số điện thoại.
     * @param password Mật khẩu.
     * @param address Địa chỉ.
     */
    public Seller(String citizenId, String name, String phoneNumber, String password, String address) {
        super(citizenId, name, phoneNumber, password, address);
        this.balance = 0;
    }

    /**
     * Khởi tạo tài khoản Người bán từ dữ liệu có sẵn (khôi phục từ cơ sở dữ liệu).
     * 
     * @param id Mã định danh duy nhất.
     * @param citizenId Số Căn cước công dân.
     * @param name Họ và tên.
     * @param phoneNumber Số điện thoại.
     * @param password Mật khẩu.
     * @param address Địa chỉ.
     */
    public Seller(String id, String citizenId, String name, String phoneNumber, String password, String address) {
        super(id, citizenId, name, phoneNumber, password, address);
        this.balance = 0;
    }

    /**
     * Thực hiện nạp tiền vào ví của Người bán (ví dụ: khi nhận doanh thu từ một phiên đấu giá kết thúc thành công).
     * 
     * @param amount Số tiền nạp vào (phải > 0).
     * @throws InvalidDepositException Nếu số tiền muốn nạp <= 0.
     */
    @Override
    public void deposit(double amount) throws InvalidDepositException {
        if (amount <= 0) {
            throw new InvalidDepositException("Số tiền nạp phải lớn hơn 0!");
        }
        this.balance += amount;
    }

    /**
     * Lấy tổng số dư hiện tại trong ví của Người bán.
     * 
     * @return Số tiền trong ví.
     */
    @Override
    public double getBalance() { return this.balance; }

    /**
     * Lấy số tiền khả dụng. Đối với Seller, toàn bộ số dư đều là khả dụng
     * do Seller không đi đấu giá nên không bị khóa tiền cọc.
     * 
     * @return Số tiền khả dụng.
     */
    @Override
    public double getAvailableBalance() { return this.balance; }

    /**
     * Trả về chuỗi mô tả thông tin cơ bản của Người bán bao gồm số dư.
     * 
     * @return Chuỗi String mô tả đối tượng Seller.
     */
    @Override
    public String toString() {
        return "Seller: " + getUserName() + " (ID: " + getId() + ") - Balance: " + balance + "$";
    }
}
