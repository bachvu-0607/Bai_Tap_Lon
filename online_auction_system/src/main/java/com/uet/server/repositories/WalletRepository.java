package com.uet.server.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.uet.domain.WalletTransaction;
import com.uet.server.utils.DatabaseConnection;

/**
 * Lớp xử lý toàn bộ thao tác database liên quan đến ví tiền.
 * Bao gồm: cập nhật số dư, lưu lịch sử giao dịch, đọc lịch sử giao dịch.
 *
 * Các loại giao dịch (type):
 *   DEPOSIT     - Người dùng nạp tiền thủ công vào ví
 *   BID_LOCK    - Tiền bị tạm giữ khi đặt giá
 *   BID_UNLOCK  - Tiền được hoàn trả khi bị người khác đặt giá cao hơn
 *   PAYMENT     - Tiền bị trừ vĩnh viễn khi người thắng xác nhận thanh toán
 *   SALE_INCOME - Tiền được cộng vào ví seller khi phiên đấu giá kết thúc thành công
 */
public class WalletRepository {

    /**
     * Cập nhật số dư và tiền đang tạm giữ của người dùng vào database.
     * Được gọi sau mỗi thao tác thay đổi số dư: nạp tiền, đặt giá, thanh toán.
     *
     * @param userId       ID của người dùng cần cập nhật (system_id trong bảng users)
     * @param balance      Tổng số dư mới (bao gồm cả tiền đang tạm giữ)
     * @param lockedBalance Số tiền đang bị tạm giữ (chỉ áp dụng cho Bidder; Seller luôn truyền 0)
     */
    public static void updateBalance(String userId, double balance, double lockedBalance) {
        String sql = "UPDATE users SET balance = ?, locked_balance = ? WHERE system_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, balance);
            pstmt.setDouble(2, lockedBalance);
            pstmt.setString(3, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Update balance error: " + e.getMessage());
        }
    }

    /**
     * Lưu một dòng lịch sử giao dịch vào bảng wallet_transactions.
     * Mỗi lần tiền thay đổi (nạp, tạm giữ, hoàn, thanh toán) đều tạo một bản ghi ở đây.
     *
     * @param userId      ID người dùng thực hiện giao dịch
     * @param type        Loại giao dịch: DEPOSIT / BID_LOCK / BID_UNLOCK / PAYMENT / SALE_INCOME
     * @param amount      Số tiền của giao dịch (luôn dương)
     * @param description Mô tả chi tiết hiển thị cho người dùng
     */
    public static void saveTransaction(String userId, String type, double amount, String description) {
        String sql = "INSERT INTO wallet_transactions (id, user_id, type, amount, description, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Tạo ID ngẫu nhiên cho giao dịch
            pstmt.setString(1, "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
            pstmt.setString(2, userId);
            pstmt.setString(3, type);
            pstmt.setDouble(4, amount);
            pstmt.setString(5, description);
            // Lưu thời điểm hiện tại theo định dạng ISO
            pstmt.setString(6, LocalDateTime.now().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Save transaction error: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách giao dịch gần nhất của người dùng từ database.
     * Trả về tối đa 100 giao dịch, sắp xếp theo thời gian mới nhất trước.
     *
     * @param userId ID của người dùng cần xem lịch sử
     * @return Danh sách các đối tượng WalletTransaction, rỗng nếu chưa có giao dịch nào
     */
    public static List<WalletTransaction> getTransactions(String userId) {
        List<WalletTransaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM wallet_transactions WHERE user_id = ? ORDER BY created_at DESC LIMIT 100";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(new WalletTransaction(
                        rs.getString("id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Get transactions error: " + e.getMessage());
        }
        return transactions;
    }
}
