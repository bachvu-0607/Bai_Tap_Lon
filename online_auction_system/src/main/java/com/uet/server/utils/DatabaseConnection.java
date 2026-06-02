package com.uet.server.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Quản lý kết nối cơ sở dữ liệu SQLite cho hệ thống đấu giá.
 * Cung cấp kết nối JDBC và các phương thức khởi tạo cấu trúc bảng dữ liệu ban đầu.
 */
public class DatabaseConnection {
    // Tên file database
    private static final String URL = "jdbc:sqlite:" + resolveDatabasePath();

    /**
     * Thiết lập và trả về kết nối đến cơ sở dữ liệu SQLite.
     * 
     * @return Đối tượng Connection kết nối tới SQLite.
     * @throws SQLException Nếu có lỗi xảy ra trong quá trình kết nối.
     */
    public static Connection getConnection() throws SQLException {
        // DriverManager sẽ dùng chuỗi URL trên để mở đường ống vào file .db
        return DriverManager.getConnection(URL);
    }

    /**
     * Xác định đường dẫn tuyệt đối đến tệp tin cơ sở dữ liệu (auction_system.db).
     * Đảm bảo tệp cơ sở dữ liệu được tạo ở thư mục gốc của dự án.
     * 
     * @return Chuỗi đường dẫn đến tệp cơ sở dữ liệu.
     */
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

    /**
     * Tạo bảng `users` chứa thông tin người dùng nếu bảng này chưa tồn tại.
     */
    public static void createTableUsers(){
        String sql = "CREATE TABLE IF NOT EXISTS users ("
            + "stt INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "system_id TEXT UNIQUE,"
            + "citizen_id TEXT UNIQUE,"
            + "password TEXT NOT NULL,"
            + "role TEXT NOT NULL,"
            + "full_name TEXT,"  
            + "phone TEXT NOT NULL UNIQUE,"       
            + "address TEXT"      
            + ");";

        try (Connection conn = getConnection(); 
            Statement stmt = conn.createStatement()){

            stmt.execute(sql);
            System.out.println("Create table user successfully!");

        }catch(SQLException e){
            System.out.println("Creat table error: " + e.getMessage());
        }
    }

    /**
     * Tạo các bảng liên quan đến phiên đấu giá bao gồm `items`, `auctions`, và `bids` nếu chưa tồn tại.
     * Đồng thời thực hiện nâng cấp schema bảng nếu cần thiết.
     */
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
     * Thêm cột vào bảng nếu cột đó chưa tồn tại trong cấu trúc bảng hiện tại.
     * 
     * @param stmt Đối tượng Statement để thực thi lệnh SQL.
     * @param tableName Tên bảng cần thêm cột.
     * @param columnName Tên cột cần thêm.
     * @param columnType Kiểu dữ liệu của cột cần thêm.
     */
    private static void addColumnIfMissing(Statement stmt, String tableName, String columnName, String columnType) {
        try {
            stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
        } catch (SQLException ignored) {
            // SQLite throws duplicate column name when the schema is already up to date.
        }
    }

}
