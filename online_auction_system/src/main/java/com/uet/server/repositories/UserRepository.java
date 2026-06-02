package com.uet.server.repositories; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.uet.domain.entity.user.Admin;
import com.uet.domain.entity.user.Bidder;
import com.uet.domain.entity.user.Seller;
import com.uet.domain.entity.user.User;
import com.uet.server.utils.DatabaseConnection;

/**
 * Lớp Repository quản lý các hoạt động truy vấn và thao tác dữ liệu người dùng (User, Admin, Bidder, Seller)
 * trong cơ sở dữ liệu.
 */
public class UserRepository {
    
    /**
     * Thực hiện đăng ký tài khoản người dùng mới vào hệ thống.
     * 
     * @param name Họ và tên đầy đủ của người dùng.
     * @param phone Số điện thoại (đồng thời là số liên hệ).
     * @param citizenId Số Căn cước công dân (CCCD).
     * @param password Mật khẩu tài khoản đã được mã hóa hoặc thô.
     * @param address Địa chỉ nơi ở.
     * @param role Vai trò của người dùng (ví dụ: "Bidder", "Seller", "Admin").
     * @return {@code true} nếu đăng ký thành công và lưu vào cơ sở dữ liệu; {@code false} nếu thất bại.
     */
    public static boolean register(String name, String phone, String citizenId, String password, String address, String role){
        String systemId = generateSystemId();
        try(Connection conn = DatabaseConnection.getConnection()){
            String sql = "INSERT INTO users (system_id, citizen_id, full_name, phone, password, address, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, systemId);
                pstmt.setString(2, citizenId);
                pstmt.setString(3, name);
                pstmt.setString(4, phone);
                pstmt.setString(5, password);
                pstmt.setString(6, address);
                pstmt.setString(7, role);

                pstmt.executeUpdate();
                System.out.println("Regist successfully system id: " + systemId + " to system");
                return true;
            }

        }catch(SQLException e){
            System.out.println("Regist error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Kiểm tra xem số Căn cước công dân (Citizen ID) đã tồn tại trong hệ thống chưa.
     * 
     * @param citizenId Số CCCD cần kiểm tra.
     * @return {@code true} nếu đã tồn tại; {@code false} nếu chưa tồn tại hoặc xảy ra lỗi SQL.
     */
    public static boolean checkCitizenIdExisted(String citizenId){
        String sql = "SELECT 1 FROM users WHERE citizen_id = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
               
                pstmt.setString(1, citizenId);

                ResultSet rs = pstmt.executeQuery();
                return rs.next();

        }catch(SQLException e){
            //System.out.println(" " + e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra xem số điện thoại đăng ký đã tồn tại trong hệ thống chưa.
     * 
     * @param phone Số điện thoại cần kiểm tra.
     * @return {@code true} nếu đã tồn tại; {@code false} nếu chưa tồn tại hoặc xảy ra lỗi SQL.
     */
    public static boolean check_phone_existed(String phone){
        String sql = "SELECT 1 FROM users WHERE phone = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
               
                pstmt.setString(1, phone);

                ResultSet rs = pstmt.executeQuery();
                return rs.next();

        }catch(SQLException e){
            //System.out.println(" " + e.getMessage());
            return false;
        }
    }

    /**
     * Xác thực thông tin đăng nhập của người dùng.
     * Hỗ trợ đăng nhập bằng Số Căn cước công dân hoặc Số điện thoại.
     * 
     * @param username Tên đăng nhập (có thể là Số CCCD hoặc Số điện thoại).
     * @param password Mật khẩu tài khoản.
     * @return Đối tượng kế thừa từ {@link User} tương ứng với vai trò nếu xác thực thành công;
     *         {@code null} nếu thông tin đăng nhập không chính xác hoặc xảy ra lỗi.
     */
    public static User checkSignIn(String username, String password){
        User loggedInUser = null;
        String sql = "SELECT * FROM users WHERE (citizen_id = ? OR phone = ?) AND password = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
               
                pstmt.setString(1, username);
                pstmt.setString(2, username);
                pstmt.setString(3, password);

                ResultSet rs = pstmt.executeQuery();

                if(rs.next()){
                    String systemId = rs.getString("system_id");
                    String citizenId = rs.getString("citizen_id");
                    String name = rs.getString("full_name");
                    String phone = rs.getString("phone");
                    //String password = rs.getString("password"); ko cần vì có sẵn rồi
                    String address = rs.getString("address");
                    String role = rs.getString("role");
                    if (role.equals("Bidder")) {
                        loggedInUser = new Bidder(systemId, citizenId, name, phone, password, address); 
                        
                    } else if (role.equals("Seller")) {
                        loggedInUser = new Seller(systemId, citizenId, name, phone, password, address); 
                        
                    } else if (role.equals("Admin")) {
                        loggedInUser = new Admin(systemId, citizenId, name, phone, password, address); 
                    }
                }


        }catch(SQLException e){
            System.out.println("Sign in error: " + e.getMessage());
        }
        return loggedInUser;
    }

    /**
     * Tìm kiếm thông tin người bán (Seller) theo mã hệ thống (System ID).
     * 
     * @param systemId Mã định danh hệ thống của người bán.
     * @return Đối tượng {@link Seller} nếu tìm thấy và đúng vai trò; {@code null} nếu không tìm thấy.
     */
    public static Seller findSellerBySystemId(String systemId) {
        User user = findUserBySystemId(systemId);
        return user instanceof Seller ? (Seller) user : null;
    }

    /**
     * Tìm kiếm thông tin người đấu giá (Bidder) theo mã hệ thống (System ID).
     * 
     * @param systemId Mã định danh hệ thống của người đấu giá.
     * @return Đối tượng {@link Bidder} nếu tìm thấy và đúng vai trò; {@code null} nếu không tìm thấy.
     */
    public static Bidder findBidderBySystemId(String systemId) {
        User user = findUserBySystemId(systemId);
        return user instanceof Bidder ? (Bidder) user : null;
    }

    /**
     * Tìm kiếm thông tin người dùng theo mã hệ thống (System ID) từ cơ sở dữ liệu.
     * 
     * @param systemId Mã định danh hệ thống của người dùng.
     * @return Đối tượng kế thừa từ {@link User} tương ứng (Admin, Bidder, hoặc Seller);
     *         {@code null} nếu không tìm thấy hoặc có lỗi xảy ra.
     */
    private static User findUserBySystemId(String systemId) {
        String sql = "SELECT * FROM users WHERE system_id = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, systemId);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                String citizenId = rs.getString("citizen_id");
                String name = rs.getString("full_name");
                String phone = rs.getString("phone");
                String password = rs.getString("password");
                String address = rs.getString("address");
                String role = rs.getString("role");
                if (role.equals("Bidder")) {
                    return new Bidder(systemId, citizenId, name, phone, password, address);
                } else if (role.equals("Seller")) {
                    return new Seller(systemId, citizenId, name, phone, password, address);
                } else if (role.equals("Admin")) {
                    return new Admin(systemId, citizenId, name, phone, password, address);
                }
            }
        }catch(SQLException e){
            System.out.println("Find user error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Sinh tự động mã định danh hệ thống duy nhất cho người dùng mới.
     * 
     * @return Chuỗi định danh bắt đầu bằng "USER-".
     */
    private static String generateSystemId() {
        return "USER-" + UUID.randomUUID();
    }

}
