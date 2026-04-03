package com.uet.server.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // Tên file database
    private static final String URL = "jdbc:sqlite:auction_system.db";

    public static Connection getConnection() throws SQLException {
        // DriverManager sẽ dùng chuỗi URL trên để mở đường ống vào file .db
        return DriverManager.getConnection(URL);
    }

    public static void createTableUsers(){
        String sql = "CREATE TABLE IF NOT EXISTS users ("
            + "stt INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "ID TEXT NOT NULL UNIQUE," // Dùng txt_ID lưu vào đây
            + "password TEXT NOT NULL,"
            + "role TEXT NOT NULL,"
            + "full_name TEXT,"  
            + "phone TEXT NOT NULL UNIQUE,"       
            + "address TEXT"      
            + ");";

        try (Connection conn = DatabaseConnection.getConnection(); 
            Statement stmt = conn.createStatement()){

            stmt.execute(sql);
            System.out.println("Create table user successfully!");

        }catch(SQLException e){
            System.out.println("Creat table error");
        }
    }
}