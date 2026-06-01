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
            Path classPath = Paths
                    .get(DatabaseConnection.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path projectRoot = Files.isDirectory(classPath)
                    ? classPath.getParent().getParent()
                    : classPath.getParent();
            return projectRoot.resolve("auction_system.db").toString();
        } catch (Exception e) {
            return "auction_system.db";
        }
    }

    public static void createTableUsers() {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
            + "stt INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "system_id TEXT UNIQUE,"
            + "citizen_id TEXT UNIQUE,"
            + "password TEXT NOT NULL,"
            + "role TEXT NOT NULL,"
            + "full_name TEXT,"
            + "phone TEXT NOT NULL UNIQUE,"
            + "address TEXT,"
            + "balance REAL DEFAULT 0,"
            + "locked_balance REAL DEFAULT 0"
            + ");";

        try (Connection conn = getConnection();
            Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            addColumnIfMissing(stmt, "users", "balance", "REAL DEFAULT 0");
            addColumnIfMissing(stmt, "users", "locked_balance", "REAL DEFAULT 0");
            System.out.println("Create table user successfully!");

        } catch (SQLException e) {
            System.out.println("Creat table error: " + e.getMessage());
        }
    }

    public static void createWalletTransactionsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS wallet_transactions ("
            + "id TEXT PRIMARY KEY,"
            + "user_id TEXT NOT NULL,"
            + "type TEXT NOT NULL,"
            + "amount REAL NOT NULL,"
            + "description TEXT,"
            + "created_at TEXT NOT NULL,"
            + "FOREIGN KEY(user_id) REFERENCES users(system_id)"
            + ");";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Create wallet_transactions table successfully!");
        } catch (SQLException e) {
            System.out.println("Create wallet_transactions error: " + e.getMessage());
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

    /**
     * Tạo bảng auto_bids để lưu các đăng ký đấu giá tự động của Bidder.
     *
     * Cột is_active = 1 → đang hoạt động, 0 → đã huỷ hoặc phiên đã kết thúc.
     * Bảng này chủ yếu dùng để ghi log; logic auto-bid chính chạy trong bộ nhớ (AuctionManager).
     */
    public static void createAutoBidsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS auto_bids ("
            + "id TEXT PRIMARY KEY,"
            + "auction_id TEXT NOT NULL,"
            + "bidder_id TEXT NOT NULL,"
            + "max_bid REAL NOT NULL,"
            + "increment REAL NOT NULL,"
            + "registered_at TEXT NOT NULL,"
            + "is_active INTEGER NOT NULL DEFAULT 1,"
            + "FOREIGN KEY(auction_id) REFERENCES auctions(id),"
            + "FOREIGN KEY(bidder_id) REFERENCES users(system_id)"
            + ");";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Create auto_bids table successfully!");
        } catch (SQLException e) {
            System.out.println("Create auto_bids error: " + e.getMessage());
        }
    }

    private static void addColumnIfMissing(Statement stmt, String tableName, String columnName, String columnType) {
        try {
            stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
        } catch (SQLException ignored) {
            // SQLite throws duplicate column name when the schema is already up to date.
        }
    }

}
