package com.uet.server.repositories; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.uet.domain.summary.UserSummary;
import com.uet.domain.entity.user.Admin;
import com.uet.domain.entity.user.Bidder;
import com.uet.domain.entity.user.Seller;
import com.uet.domain.entity.user.User;
import com.uet.server.utils.DatabaseConnection;

public class UserRepository {
    public enum RegisterStatus {
        SUCCESS,
        DUPLICATE_CITIZEN_ID,
        DUPLICATE_PHONE,
        FAILED
    }

    //Hàm đăng ký -> lưu lại tài khoản và trả về boolean
    public static RegisterStatus register(String name, String phone, String citizenId, String password, String address, String role){
        if (checkCitizenIdExisted(citizenId)) {
            return RegisterStatus.DUPLICATE_CITIZEN_ID;
        }

        if (check_phone_existed(phone)) {
            return RegisterStatus.DUPLICATE_PHONE;
        }

        String systemId = generateSystemId();
        double initialBalance = "Bidder".equals(role) ? Bidder.DEFAULT_BALANCE : 0;
        try(Connection conn = DatabaseConnection.getConnection()){
            String sql = "INSERT INTO users (system_id, citizen_id, full_name, phone, password, address, role, balance, locked_balance) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, systemId);
                pstmt.setString(2, citizenId);
                pstmt.setString(3, name);
                pstmt.setString(4, phone);
                pstmt.setString(5, password);
                pstmt.setString(6, address);
                pstmt.setString(7, role);
                pstmt.setDouble(8, initialBalance);
                pstmt.setDouble(9, 0);

                pstmt.executeUpdate();
                System.out.println("Regist successfully system id: " + systemId + " to system");
                return RegisterStatus.SUCCESS;
            }

        }catch(SQLException e){
            System.out.println("Regist error: " + e.getMessage());
            return getRegisterStatusFromSqlError(e);
        }
    }

    private static RegisterStatus getRegisterStatusFromSqlError(SQLException e) {
        String message = e.getMessage();
        if (message == null) {
            return RegisterStatus.FAILED;
        }

        if (message.contains("users.citizen_id")) {
            return RegisterStatus.DUPLICATE_CITIZEN_ID;
        }

        if (message.contains("users.phone")) {
            return RegisterStatus.DUPLICATE_PHONE;
        }

        return RegisterStatus.FAILED;
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
        String sql = "SELECT * FROM users WHERE (citizen_id = ? OR phone = ?) AND password = ? AND is_banned = 0";
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
                    loggedInUser = mapBidder(rs, systemId, citizenId, name, phone, password, address);
                        
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

    public static boolean isBannedAccount(String username, String password) {
        String sql = "SELECT 1 FROM users WHERE (citizen_id = ? OR phone = ?) AND password = ? AND is_banned = 1";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, username);
            pstmt.setString(2, username);
            pstmt.setString(3, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }catch(SQLException e){
            System.out.println("Check banned account error: " + e.getMessage());
            return false;
        }
    }

    public static List<UserSummary> findAllUserSummaries() {
        List<UserSummary> users = new ArrayList<>();
        String sql = "SELECT system_id, full_name, phone, role, is_banned FROM users ORDER BY role, full_name";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()){

            while (rs.next()) {
                users.add(new UserSummary(
                        rs.getString("system_id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("role"),
                        rs.getInt("is_banned") == 0));
            }
        }catch(SQLException e){
            System.out.println("Load users error: " + e.getMessage());
        }
        return users;
    }

    public static UserSummary findUserSummaryBySystemId(String systemId) {
        String sql = "SELECT system_id, full_name, phone, role, is_banned FROM users WHERE system_id = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, systemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new UserSummary(
                        rs.getString("system_id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("role"),
                        rs.getInt("is_banned") == 0);
            }
        }catch(SQLException e){
            System.out.println("Find user summary error: " + e.getMessage());
        }
        return null;
    }

    public static boolean banUser(String systemId) {
        String sql = "UPDATE users SET is_banned = 1 WHERE system_id = ? AND role <> 'Admin' AND is_banned = 0";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, systemId);
            return pstmt.executeUpdate() > 0;
        }catch(SQLException e){
            System.out.println("Ban user error: " + e.getMessage());
            return false;
        }
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
                    return mapBidder(rs, systemId, citizenId, name, phone, password, address);
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

    private static Bidder mapBidder(ResultSet rs, String systemId, String citizenId, String name, String phone, String password, String address) throws SQLException {
        Bidder bidder = new Bidder(systemId, citizenId, name, phone, password, address);
        bidder.restoreFunds(rs.getDouble("balance"), rs.getDouble("locked_balance"));
        return bidder;
    }

    public static void updateBidderFunds(Bidder bidder) {
        String sql = "UPDATE users SET balance = ?, locked_balance = ? WHERE system_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, bidder.getBalance());
            pstmt.setDouble(2, bidder.getLockedBalance());
            pstmt.setString(3, bidder.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Update bidder funds error: " + e.getMessage());
        }
    }

    public static void rebuildBidderLockedBalancesFromAuctions() {
        String sql = "UPDATE users "
                + "SET locked_balance = COALESCE(("
                + "SELECT SUM(current_price) FROM auctions "
                + "WHERE auctions.winner_id = users.system_id "
                + "AND auctions.status IN ('OPEN', 'RUNNING', 'FINISHED')"
                + "), 0) "
                + "WHERE role = 'Bidder'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Rebuild bidder locked balances error: " + e.getMessage());
        }
    }

    private static String generateSystemId() {
        return "USER-" + UUID.randomUUID();
    }

}
