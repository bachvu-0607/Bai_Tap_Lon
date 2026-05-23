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

public class WalletRepository {

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

    public static void saveTransaction(String userId, String type, double amount, String description) {
        String sql = "INSERT INTO wallet_transactions (id, user_id, type, amount, description, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
            pstmt.setString(2, userId);
            pstmt.setString(3, type);
            pstmt.setDouble(4, amount);
            pstmt.setString(5, description);
            pstmt.setString(6, LocalDateTime.now().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Save transaction error: " + e.getMessage());
        }
    }

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
