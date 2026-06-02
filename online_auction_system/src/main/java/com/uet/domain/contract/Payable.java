package com.uet.domain.contract;

import com.uet.domain.exceptions.InvalidDepositException;

/**
 * Interface định nghĩa các hành vi cơ bản cho những đối tượng có khả năng quản lý tài chính,
 * bao gồm việc sở hữu ví tiền, nạp tiền và truy vấn số dư.
 * Các lớp như Bidder, Seller đều kế thừa interface này để quản lý số dư của mình.
 */
public interface Payable {
    
    /**
     * Thực hiện nạp thêm tiền vào ví của người dùng.
     * Số dư sẽ được cộng dồn với số tiền vừa nạp.
     * 
     * @param amount Số tiền muốn nạp vào ví (bắt buộc phải lớn hơn 0).
     * @throws InvalidDepositException Nếu số tiền nạp vào nhỏ hơn hoặc bằng 0.
     */
    void deposit(double amount) throws InvalidDepositException;
    
    /**
     * Lấy tổng số dư hiện tại trong ví của người dùng.
     * Lưu ý: Đối với Bidder, số dư này bao gồm cả phần tiền đang bị tạm khóa (nếu có).
     * 
     * @return Tổng số tiền hiện có trong ví.
     */
    double getBalance();
    
    /**
     * Lấy số tiền khả dụng mà người dùng có thể sử dụng ngay lập tức cho các giao dịch mới.
     * Số tiền khả dụng = Tổng số tiền trong ví - Số tiền đang bị khóa (tạm giữ trong các phiên đấu giá).
     * Đối với Seller, số dư khả dụng thường bằng tổng số dư.
     * 
     * @return Số tiền khả dụng thực tế.
     */
    double getAvailableBalance();
}
