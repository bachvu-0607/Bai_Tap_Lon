package com.uet.server.repositories; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.management.relation.Role;
import javax.naming.spi.DirStateFactory;
import javax.sql.rowset.CachedRowSet;

import com.uet.server.utils.DatabaseConnection;

public class UserRepository {
    //Hàm đăng ký -> lưu lại tài khoản và trả về boolean
    public static boolean register(String name, String phone, String ID, String password, String address, String role){
        String sql = "INSERT INTO users (full_name, phone, ID, password, address, role) VALUES (?, ?, ?, ?, ?, ?)";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
                pstmt.setString(1, name);
                pstmt.setString(2, phone);
                pstmt.setString(3, ID);
                pstmt.setString(4, password);
                pstmt.setString(5, address);
                pstmt.setString(6, role);

                pstmt.executeUpdate();
                System.out.println("Regist successfully id: " + ID + "to system");
                return true;

        }catch(SQLException e){
            System.out.println("Regist error: " + e.getMessage());
            return false;
        }
    }
    
     //Phụ trợ hàm đăng ký check CCCD tồn tại hay chưa
    public static boolean check_ID_existed(String ID){
        String sql = "SELECT 1 FROM users WHERE ID = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
               
                pstmt.setString(1, ID);

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

    //Hàm đăng nhập check tài khoản -> trả về role
    public static String checkSignIn(String username, String password){
        String sql = "SELECT role FROM users WHERE (ID = ? OR phone = ?) AND password = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
               
                pstmt.setString(1, username);
                pstmt.setString(2, username);
                pstmt.setString(3, password);

                ResultSet rs = pstmt.executeQuery();

                if(rs.next()) return rs.getString("role");


        }catch(SQLException e){
            System.out.println("Sign in error: " + e.getMessage());
        }
        return null;
    }
}