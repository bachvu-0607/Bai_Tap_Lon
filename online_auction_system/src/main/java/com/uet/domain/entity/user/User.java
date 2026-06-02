package com.uet.domain.entity.user;

import com.uet.domain.entity.Entity;

/**
 * Lớp trừu tượng đại diện cho một Người dùng trong hệ thống (User).
 * Kế thừa từ lớp Entity để có các thuộc tính ID và thời gian cơ bản.
 * Lớp này chứa các thông tin cá nhân dùng chung cho mọi loại tài khoản (Admin, Seller, Bidder).
 */
public abstract class User extends Entity {
    private String userName;
    private String name;
    private String citizenId;
    private String phoneNumber;
    private String password;
    private String address;
    private boolean isActive = true;

    /**
     * Khởi tạo một người dùng mới (ID sẽ được tự động sinh).
     * 
     * @param citizenId Số Căn cước công dân.
     * @param name Họ và tên đầy đủ.
     * @param phoneNumber Số điện thoại liên hệ (đồng thời dùng làm tên đăng nhập).
     * @param password Mật khẩu tài khoản.
     * @param address Địa chỉ nơi ở.
     */
    public User(String citizenId, String name, String phoneNumber, String password, String address) {
        super();
        initializeUser(citizenId, name, phoneNumber, password, address);
    }

    /**
     * Khởi tạo một người dùng với ID đã có sẵn (thường dùng khi khôi phục từ cơ sở dữ liệu).
     * 
     * @param id Mã định danh duy nhất của người dùng.
     * @param citizenId Số Căn cước công dân.
     * @param name Họ và tên đầy đủ.
     * @param phoneNumber Số điện thoại liên hệ (đồng thời dùng làm tên đăng nhập).
     * @param password Mật khẩu tài khoản.
     * @param address Địa chỉ nơi ở.
     */
    public User(String id, String citizenId, String name, String phoneNumber, String password, String address) {
        super(id);
        initializeUser(citizenId, name, phoneNumber, password, address);
    }

    /**
     * Phương thức hỗ trợ gán giá trị khởi tạo chung cho các thuộc tính của User.
     * 
     * @param citizenId Số Căn cước công dân.
     * @param name Họ và tên đầy đủ.
     * @param phoneNumber Số điện thoại.
     * @param password Mật khẩu tài khoản.
     * @param address Địa chỉ.
     */
    private void initializeUser(String citizenId, String name, String phoneNumber, String password, String address) {
        this.userName = phoneNumber;
        this.citizenId = citizenId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.address = address;
    }
    
    /**
     * Thiết lập trạng thái hoạt động của tài khoản.
     * 
     * @param isActive {@code true} nếu tài khoản được phép hoạt động, {@code false} nếu bị khóa.
     */
    public void setActive(boolean isActive) { this.isActive = isActive; }
    
    /**
     * Kiểm tra trạng thái hoạt động của tài khoản.
     * 
     * @return {@code true} nếu đang hoạt động, {@code false} nếu đã bị khóa.
     */
    public boolean isActive() { return this.isActive; }
    
    /**
     * Lấy tên đăng nhập của người dùng (trong hệ thống này sử dụng số điện thoại làm tên đăng nhập).
     * 
     * @return Tên đăng nhập.
     */
    public String getUserName() { return this.userName; }
    
    /**
     * Lấy họ và tên của người dùng.
     * 
     * @return Họ và tên.
     */
    public String getName() { return this.name; }
    
    /**
     * Lấy số Căn cước công dân.
     * 
     * @return Số Căn cước công dân.
     */
    public String getCitizenId() { return this.citizenId; }
    
    /**
     * Lấy mật khẩu của tài khoản.
     * 
     * @return Mật khẩu đang sử dụng.
     */
    public String getPassword() { return this.password; }
    
    /**
     * Lấy số điện thoại liên lạc của người dùng.
     * 
     * @return Số điện thoại.
     */
    public String getPhoneNumber() { return this.phoneNumber; }
    
    /**
     * Lấy địa chỉ của người dùng.
     * 
     * @return Chuỗi địa chỉ.
     */
    public String getAddress() { return this.address; }
    
    /**
     * Thay đổi họ và tên của người dùng.
     * 
     * @param name Họ và tên mới.
     */
    public void setName(String name) { this.name = name; }
    
    /**
     * Thay đổi mật khẩu của tài khoản.
     * 
     * @param password Mật khẩu mới.
     */
    public void setPassword(String password) { this.password = password; }
}
