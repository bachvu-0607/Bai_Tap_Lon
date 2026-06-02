package com.uet.domain.entity.user;

import com.uet.domain.contract.Biddable;
import com.uet.domain.contract.Payable;
import com.uet.domain.exceptions.*;

/**
 * Lớp đại diện cho Người tham gia đấu giá (Bidder) trong hệ thống.
 * Kế thừa User và triển khai Payable (quản lý ví tiền) cùng Biddable (thực hiện nghiệp vụ đấu giá).
 * Bidder có khả năng nạp tiền, đặt giá, tạm giữ/hoàn trả tiền khi đấu giá, và hỗ trợ đấu giá tự động.
 */
public class Bidder extends User implements Payable, Biddable {
    
    /**
     * Tổng số dư hiện tại của người dùng.
     */
    private double balance;

    /**
     * Số tiền đang bị tạm giữ khi người dùng đang dẫn đầu một hoặc nhiều phiên đấu giá.
     * Số tiền này không được phép sử dụng cho các giao dịch khác.
     */
    private double lockedBalance;

    /**
     * Mức giá tối đa mà người dùng cho phép hệ thống tự động đặt giá hộ (Auto Bid).
     */
    private double maxBidLimit;
    
    /**
     * Cờ đánh dấu tính năng đấu giá tự động có đang được bật hay không.
     */
    private boolean autoBidEnabled;

    /**
     * Khởi tạo tài khoản Người mua mới (ID tự động sinh, số dư ban đầu được tặng mặc định).
     * 
     * @param citizenId Số Căn cước công dân.
     * @param name Họ và tên.
     * @param phoneNumber Số điện thoại.
     * @param password Mật khẩu.
     * @param address Địa chỉ.
     */
    public Bidder(String citizenId, String name, String phoneNumber, String password, String address) {
        super(citizenId, name, phoneNumber, password, address);
        initializeBidder();
    }

    /**
     * Khởi tạo tài khoản Người mua từ dữ liệu có sẵn (khôi phục từ cơ sở dữ liệu).
     * 
     * @param id Mã định danh duy nhất.
     * @param citizenId Số Căn cước công dân.
     * @param name Họ và tên.
     * @param phoneNumber Số điện thoại.
     * @param password Mật khẩu.
     * @param address Địa chỉ.
     */
    public Bidder(String id, String citizenId, String name, String phoneNumber, String password, String address) {
        super(id, citizenId, name, phoneNumber, password, address);
        initializeBidder();
    }

    /**
     * Phương thức khởi tạo các thông số ví mặc định cho Bidder mới.
     * Được cấp sẵn 1.000.000$ làm số vốn ban đầu (dành cho mục đích thử nghiệm).
     */
    private void initializeBidder() {
        this.balance = 1000000;
        this.lockedBalance = 0;
        this.maxBidLimit = 0;
        this.autoBidEnabled = false;
    }

    /**
     * Kích hoạt tính năng Đấu giá tự động (Auto Bid).
     * Khi tính năng này bật, hệ thống sẽ tự động nâng giá thay người dùng nếu bị người khác vượt mức.
     * 
     * @param maxBidLimit Giới hạn số tiền lớn nhất được phép tự động đặt.
     * @throws InsufficientBalanceException Nếu giới hạn tự động vượt quá số dư khả dụng thực tế.
     * @throws InvalidTransactionException Nếu giới hạn cấu hình nhỏ hơn hoặc bằng 0.
     */
    public void enableAutoBid(double maxBidLimit) throws InsufficientBalanceException, InvalidTransactionException {
        if (maxBidLimit <= 0) {
            throw new InvalidTransactionException("Giới hạn giá tự động đặt phải lớn hơn 0!");
        }
        if (maxBidLimit > getAvailableBalance()) { 
            throw new InsufficientBalanceException("Giới hạn tự động đặt giá không được vượt quá số dư khả dụng!");
        }
        this.maxBidLimit = maxBidLimit;
        this.autoBidEnabled = true;
    }
    
    /**
     * Hủy bỏ tính năng Đấu giá tự động.
     * Giới hạn giá tự động sẽ được đặt về 0.
     */
    public void disableAutoBid() {
        this.autoBidEnabled = false;
        this.maxBidLimit = 0;
    }

    /**
     * Nạp thêm tiền vào ví để tăng số dư.
     * 
     * @param amount Số tiền cần nạp.
     * @throws InvalidDepositException Nếu số tiền nạp không hợp lệ (ví dụ <= 0).
     */
    @Override
    public void deposit(double amount) throws InvalidDepositException{
        if(amount > 0){
            this.balance += amount;
        }
        else{
            throw new InvalidDepositException("Số tiền nạp phải lớn hơn 0");
        }
    }

    /**
     * Kiểm tra khả năng chi trả.
     * Xem số dư khả dụng (số dư tổng trừ đi tiền đang bị khóa) có đủ để thanh toán một khoản tiền hay không.
     * 
     * @param amount Số tiền cần kiểm tra.
     * @return true nếu có thể chi trả, false nếu không.
     */
    @Override
    public boolean canAfford(double amount) {
        return getAvailableBalance() >= amount;
    }

    /**
     * Tạm khóa một khoản tiền khi người dùng vừa đặt giá và trở thành người dẫn đầu.
     * Số tiền này sẽ được gỡ khi bị người khác trả giá cao hơn, hoặc trừ hẳn khi phiên kết thúc thành công.
     * 
     * @param amount Số tiền muốn tạm giữ.
     * @throws InvalidTransactionException Nếu số tiền tạm giữ <= 0.
     * @throws InsufficientBalanceException Nếu số dư khả dụng không đủ để tạm giữ khoản này.
     */
    @Override
    public void lockFunds(double amount) throws InvalidTransactionException, InsufficientBalanceException {
        if (amount <= 0) {
            throw new InvalidTransactionException("Số tiền cần tạm giữ phải lớn hơn 0!");
        }
        if (amount > getAvailableBalance()) {
            throw new InsufficientBalanceException("Số dư khả dụng không đủ để tạm giữ!");
        }
        this.lockedBalance += amount;
    }

    /**
     * Hoàn trả lại số tiền đã tạm giữ khi có người khác đặt giá cao hơn.
     * Giúp người dùng có lại tiền khả dụng để tiếp tục tham gia đấu giá hoặc dùng cho phiên khác.
     * 
     * @param amount Số tiền muốn hoàn trả.
     * @throws InvalidTransactionException Nếu số tiền hoàn trả <= 0.
     * @throws InsufficientBalanceException Nếu số tiền yêu cầu hoàn trả vượt mức đang bị khóa.
     */
    @Override
    public void unlockFunds(double amount) throws InvalidTransactionException, InsufficientBalanceException {
        // Không cho phép âm tiền
        if (amount <= 0) {
            throw new InvalidTransactionException("Số tiền cần hoàn trả phải lớn hơn 0!");
        }
        
        // Không cho phép hoàn trả tiền khi không có số dư bị tạm giữ
        if (this.lockedBalance <= 0) {
            throw new InsufficientBalanceException("Không có số tiền nào đang bị tạm giữ!");
        }

        // Không cho phép hoàn trả nhiều hơn số tiền đang bị tạm giữ
        if (amount > this.lockedBalance) {
            throw new InsufficientBalanceException("Số tiền hoàn trả không được vượt quá số tiền đang bị tạm giữ!");
        }

        this.lockedBalance -= amount;

    }

    /**
     * Thực hiện trừ tiền vĩnh viễn cả trong tổng số dư lẫn quỹ bị khóa khi phiên đấu giá 
     * chuyển sang trạng thái đã thanh toán (PAID) do người dùng giành chiến thắng.
     * 
     * @param amount Số tiền thực tế thanh toán cho sản phẩm.
     * @throws InvalidTransactionException Nếu số tiền thanh toán <= 0.
     * @throws InsufficientBalanceException Nếu khoản tiền thanh toán vượt quỹ bị khóa.
     */
    @Override
    public void commitPayment(double amount) throws InvalidTransactionException, InsufficientBalanceException {
        if (amount <= 0) {
            throw new InvalidTransactionException("Số tiền thanh toán phải lớn hơn 0!");
        }
        if (amount > this.lockedBalance) {
            throw new InsufficientBalanceException("Số tiền thanh toán không được vượt quá số tiền đã tạm giữ!");
        }
        this.balance -= amount;
        this.lockedBalance -= amount;
    }


    /**
     * Lấy số tiền khả dụng (Tổng quỹ - Tiền đang bị tạm giữ).
     * 
     * @return Số tiền có thể sử dụng ngay.
     */
    @Override
    public double getAvailableBalance() { return balance - lockedBalance; }

    /**
     * Lấy tổng quỹ trong ví (Bao gồm cả tiền đang bị tạm giữ).
     * 
     * @return Tổng số tiền.
     */
    @Override
    public double getBalance() { return balance; }
    
    /**
     * Lấy số lượng tiền hiện đang bị tạm giữ (lock).
     * 
     * @return Số tiền bị khóa.
     */
    public double getLockedBalance() { return lockedBalance; }
    
    /**
     * Lấy mức giới hạn cao nhất mà tính năng tự động đấu giá được phép sử dụng.
     * 
     * @return Giới hạn đấu giá tự động.
     */
    public double getMaxBidLimit() { return maxBidLimit; }
    
    /**
     * Thay đổi giới hạn tối đa cho tự động đấu giá.
     * 
     * @param maxBidLimit Mức giới hạn mới (phải lớn hơn 0 và không quá tổng quỹ).
     * @throws InvalidTransactionException Nếu giới hạn <= 0.
     * @throws InsufficientBalanceException Nếu vượt quá số tiền trong ví.
     */
    public void setMaxBidLimit(double maxBidLimit) throws InvalidTransactionException, InsufficientBalanceException {
        if(maxBidLimit <= 0) {
            throw new InvalidTransactionException("Giới hạn giá tự động đặt phải lớn hơn 0!");
        }
        if(maxBidLimit > this.balance){
            throw new InsufficientBalanceException("Giới hạn giá tự động đặt không được vượt quá số dư khả dụng!");
        }   
        this.maxBidLimit = maxBidLimit;
    }

    /**
     * Kiểm tra xem tính năng tự động đấu giá có đang chạy hay không.
     * 
     * @return true nếu đang bật, false nếu tắt.
     */
    public boolean isAutoBidEnabled() { return autoBidEnabled; }

    /**
     * Trả về chuỗi mô tả ngắn gọn đối tượng Người tham gia đấu giá.
     * 
     * @return Thông tin chuỗi.
     */
    @Override
    public String toString() {
        return "Bidder: " + getUserName() + 
               " (ID: " + getId() + ") - " +
                "Balance: " + balance + "$";
    }
    
}
