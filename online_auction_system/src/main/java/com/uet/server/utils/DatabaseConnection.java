package com.uet.server.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // Tên file database
    private static final String URL = "jdbc:sqlite:" + resolveDatabasePath();

    public static Connection getConnection() throws SQLException {
        // DriverManager sẽ dùng chuỗi URL trên để mở đường ống vào file .db
        return DriverManager.getConnection(URL);
    }

    private static String resolveDatabasePath() {
        try {
            Path classPath = Paths.get(DatabaseConnection.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path projectRoot = Files.isDirectory(classPath)
                    ? classPath.getParent().getParent()
                    : classPath.getParent();
            return projectRoot.resolve("auction_system.db").toString();
        } catch (Exception e) {
            return "auction_system.db";
        }
    }

    public static void createTableUsers(){
        String sql = "CREATE TABLE IF NOT EXISTS users ("
            + "stt INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "system_id TEXT UNIQUE,"
            + "citizen_id TEXT UNIQUE,"
            + "password TEXT NOT NULL,"
            + "role TEXT NOT NULL,"
            + "full_name TEXT,"  
            + "phone TEXT NOT NULL UNIQUE,"       
            + "address TEXT,"
            + "balance REAL NOT NULL DEFAULT 0,"
            + "locked_balance REAL NOT NULL DEFAULT 0,"
            + "is_banned INTEGER NOT NULL DEFAULT 0"
            + ");";

        try (Connection conn = getConnection(); 
            Statement stmt = conn.createStatement()){

            stmt.execute(sql);
            boolean addedBalanceColumn = addColumnIfMissing(stmt, "users", "balance", "REAL NOT NULL DEFAULT 0");
            addColumnIfMissing(stmt, "users", "locked_balance", "REAL NOT NULL DEFAULT 0");
            addColumnIfMissing(stmt, "users", "is_banned", "INTEGER NOT NULL DEFAULT 0");
            if (addedBalanceColumn) {
                stmt.executeUpdate("UPDATE users SET balance = 1000000 WHERE role = 'Bidder' AND balance = 0");
            }
            System.out.println("Create table user successfully!");

        }catch(SQLException e){
            System.out.println("Creat table error: " + e.getMessage());
        }
    }

    public static void createAuctionTables() {
        String createItems = "CREATE TABLE IF NOT EXISTS items ("
            + "id TEXT PRIMARY KEY,"
            + "name TEXT NOT NULL,"
            + "category TEXT NOT NULL,"
            + "description TEXT,"
            + "starting_price REAL NOT NULL,"
            + "status TEXT NOT NULL,"
            + "image_link TEXT"
            + ");";

        String createAuctions = "CREATE TABLE IF NOT EXISTS auctions ("
            + "id TEXT PRIMARY KEY,"
            + "item_id TEXT NOT NULL,"
            + "seller_id TEXT NOT NULL,"
            + "start_time TEXT NOT NULL,"
            + "end_time TEXT NOT NULL,"
            + "current_price REAL NOT NULL,"
            + "min_increment REAL NOT NULL,"
            + "status TEXT NOT NULL,"
            + "winner_id TEXT,"
            + "FOREIGN KEY(item_id) REFERENCES items(id),"
            + "FOREIGN KEY(seller_id) REFERENCES users(system_id),"
            + "FOREIGN KEY(winner_id) REFERENCES users(system_id)"
            + ");";

        String createBids = "CREATE TABLE IF NOT EXISTS bids ("
            + "id TEXT PRIMARY KEY,"
            + "auction_id TEXT NOT NULL,"
            + "bidder_id TEXT NOT NULL,"
            + "amount REAL NOT NULL,"
            + "bid_time TEXT NOT NULL,"
            + "status TEXT NOT NULL,"
            + "FOREIGN KEY(auction_id) REFERENCES auctions(id),"
            + "FOREIGN KEY(bidder_id) REFERENCES users(system_id)"
            + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createItems);
            addColumnIfMissing(stmt, "items", "image_link", "TEXT");
            stmt.execute(createAuctions);
            stmt.execute(createBids);
            System.out.println("Create auction tables successfully!");
        } catch (SQLException e) {
            System.out.println("Create auction tables error: " + e.getMessage());
        }
    }

    private static boolean addColumnIfMissing(Statement stmt, String tableName, String columnName, String columnType) {
        try {
            stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
            return true;
        } catch (SQLException ignored) {
            // SQLite throws duplicate column name when the schema is already up to date.
            return false;
        }
    }
}
