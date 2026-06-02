package com.uet.domain.contract;

import com.uet.domain.exceptions.InsufficientBalanceException;
import com.uet.domain.exceptions.InvalidTransactionException;

/**
 * Interface định nghĩa các hành vi cho những đối tượng có khả năng tham gia đặt giá (Bidder).
 * Bao gồm các phương thức quản lý tài chính liên quan đến việc đặt cọc, hoàn trả, 
 * và thanh toán khi tham gia các phiên đấu giá.
 */
public interface Biddable {
    
    /**
     * Tạm giữ một khoản tiền khi người dùng đặt giá thành công và trở thành người dẫn đầu.
     * Số tiền này không thể sử dụng cho các phiên đấu giá khác cho đến khi được hoàn trả.
     * 
     * @param amount Số tiền cần tạm giữ (phải lớn hơn 0).
     * @throws InvalidTransactionException Nếu số tiền yêu cầu tạm giữ không hợp lệ (ví dụ <= 0).
     * @throws InsufficientBalanceException Nếu số dư khả dụng không đủ để tạm giữ.
     */
    void lockFunds(double amount) throws InvalidTransactionException, InsufficientBalanceException;
    
    /**
     * Hoàn trả lại số tiền đã tạm giữ trước đó khi người dùng bị người khác trả giá cao hơn (Outbid).
     * Số tiền hoàn trả sẽ được cộng ngược lại vào số dư khả dụng.
     * 
     * @param amount Số tiền cần hoàn trả (phải lớn hơn 0).
     * @throws InvalidTransactionException Nếu số tiền yêu cầu hoàn trả không hợp lệ hoặc lớn hơn tổng số tiền đang bị khóa.
     * @throws InsufficientBalanceException Nếu số tiền khóa hiện tại nhỏ hơn số lượng muốn mở.
     */
    void unlockFunds(double amount) throws InvalidTransactionException, InsufficientBalanceException;
    
    /**
     * Trừ vĩnh viễn số tiền đã tạm giữ khi người dùng chiến thắng phiên đấu giá 
     * và hệ thống tiến hành thanh toán cho sản phẩm.
     * 
     * @param amount Số tiền cần thanh toán thực tế (thường bằng với số tiền đã tạm giữ).
     * @throws InvalidTransactionException Nếu số tiền yêu cầu thanh toán không hợp lệ (ví dụ <= 0).
     * @throws InsufficientBalanceException Nếu số tiền đang tạm giữ không đủ để thanh toán.
     */
    void commitPayment(double amount) throws InvalidTransactionException, InsufficientBalanceException;
    
    /**
     * Kiểm tra xem đối tượng có đủ số dư khả dụng để chi trả cho một khoản tiền nhất định hay không.
     * Khả dụng ở đây nghĩa là số dư thực tế trừ đi phần tiền đang bị khóa (tạm giữ).
     * 
     * @param amount Số tiền cần kiểm tra.
     * @return {@code true} nếu số dư khả dụng lớn hơn hoặc bằng {@code amount}; ngược lại trả về {@code false}.
     */
    boolean canAfford(double amount);
}
