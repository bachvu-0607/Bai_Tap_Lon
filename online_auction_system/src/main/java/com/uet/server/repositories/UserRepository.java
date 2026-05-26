package com.uet.server.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.uet.domain.UserSummary;
import com.uet.domain.entity.user.Admin;
import com.uet.domain.entity.user.Bidder;
import com.uet.domain.entity.user.Seller;
import com.uet.domain.entity.user.User;
import com.uet.server.utils.DatabaseConnection;

public class UserRepository {
    //Hàm đăng ký -> lưu lại tài khoản và trả về boolean
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
    
    //Phụ trợ hàm đăng ký check CCCD tồn tại hay chưa
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

    //Phụ trợ hàm đăng ký check số điện thoại tồn tại hay chưa
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

    //Hàm đăng nhập check tài khoản -> trả về User
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
                    String address = rs.getString("address");
                    String role = rs.getString("role");
                    double balance = rs.getDouble("balance");
                    double lockedBalance = rs.getDouble("locked_balance");
                    if (role.equals("Bidder")) {
                        loggedInUser = new Bidder(systemId, citizenId, name, phone, password, address, balance, lockedBalance);
                    } else if (role.equals("Seller")) {
                        loggedInUser = new Seller(systemId, citizenId, name, phone, password, address, balance);
                    } else if (role.equals("Admin")) {
                        loggedInUser = new Admin(systemId, citizenId, name, phone, password, address);
                    }
                }


        }catch(SQLException e){
            System.out.println("Sign in error: " + e.getMessage());
        }
        return loggedInUser;
    }

    public static Seller findSellerBySystemId(String systemId) {
        User user = findUserBySystemId(systemId);
        return user instanceof Seller ? (Seller) user : null;
    }

    public static Bidder findBidderBySystemId(String systemId) {
        User user = findUserBySystemId(systemId);
        return user instanceof Bidder ? (Bidder) user : null;
    }

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
                double balance = rs.getDouble("balance");
                double lockedBalance = rs.getDouble("locked_balance");
                if (role.equals("Bidder")) {
                    return new Bidder(systemId, citizenId, name, phone, password, address, balance, lockedBalance);
                } else if (role.equals("Seller")) {
                    return new Seller(systemId, citizenId, name, phone, password, address, balance);
                } else if (role.equals("Admin")) {
                    return new Admin(systemId, citizenId, name, phone, password, address);
                }
            }
        }catch(SQLException e){
            System.out.println("Find user error: " + e.getMessage());
        }
        return null;
    }
    // Lấy thông tin tất cả người dùng (trừ Admin) để hiển thị trong phần quản lý người dùng của Admin
    public static List<UserSummary> getAllNonAdminUsers() {
        String sql = "SELECT system_id, citizen_id, full_name, phone, address, role FROM users WHERE role != 'Admin'";
        List<UserSummary> result = new ArrayList<>();
        // Sử dụng try-with-resources để tự động đóng kết nối và statement
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
        // Duyệt qua kết quả và tạo UserSummary cho mỗi người dùng
            while (rs.next()) {
                String systemId = rs.getString("system_id");
                String citizenId = rs.getString("citizen_id");
                String name = rs.getString("full_name");
                String phone = rs.getString("phone");
                String address = rs.getString("address");
                String role = rs.getString("role");
                User user;
                if ("Bidder".equals(role)) {
                    user = new Bidder(systemId, citizenId, name, phone, "", address);
                } else {
                    user = new Seller(systemId, citizenId, name, phone, "", address);
                }
                result.add(new UserSummary(user, role));
            }
        } catch (SQLException e) {
            System.out.println("getAllNonAdminUsers error: " + e.getMessage());
        }
        return Collections.unmodifiableList(result);
    }

    public static boolean removeUserById(String systemId) {
        String sql = "DELETE FROM users WHERE system_id = ? AND role != 'Admin'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, systemId);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("removeUserById error: " + e.getMessage());
            return false;
        }
    }

    private static String generateSystemId() {
        return "USER-" + UUID.randomUUID();
    }

}
