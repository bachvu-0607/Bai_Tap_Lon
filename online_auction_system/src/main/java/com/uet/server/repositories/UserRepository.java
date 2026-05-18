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

    private static String generateSystemId() {
        return "USER-" + UUID.randomUUID();
    }

}
