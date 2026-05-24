package com.uet.client.controllers;

import com.uet.client.core.ClientSocket;
import com.uet.domain.AuctionSummary;
import com.uet.domain.result.AutoBidResult;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller cho cửa sổ popup Auto Bid.
 *
 * Luồng sử dụng:
 *   1. AuctionListController mở popup và gọi setAuction(selectedAuction)
 *   2. Controller tải trạng thái auto-bid hiện tại từ server (nếu có)
 *   3. Người dùng nhập Max Bid + Increment → bấm "Đặt Auto Bid"
 *   4. Hoặc bấm "Huỷ Auto Bid" để dừng auto-bid đang chạy
 */
public class AutoBidController {

    @FXML private Label    lblAuctionInfo;   // Hiển thị thông tin phiên đang chọn
    @FXML private VBox     activeAutoBidBox; // Panel hiện trạng thái auto-bid đang chạy
    @FXML private Label    lblCurrentAutoBid;// Nội dung trạng thái auto-bid hiện tại
    @FXML private TextField txtMaxBid;       // Ô nhập giá tối đa
    @FXML private TextField txtIncrement;    // Ô nhập bước giá
    @FXML private Label    lblResult;        // Thông báo kết quả sau thao tác
    @FXML private Button   btnSetAutoBid;    // Nút đặt / cập nhật auto-bid
    @FXML private Button   btnCancelAutoBid; // Nút huỷ auto-bid đang chạy

    private AuctionSummary selectedAuction; // Phiên đấu giá đang được xem

    /**
     * Được gọi bởi AuctionListController SAU KHI popup được mở.
     * Điền thông tin phiên vào UI và tải trạng thái auto-bid hiện tại.
     *
     * @param auction Phiên đấu giá mà bidder muốn đặt auto-bid
     */
    public void setAuction(AuctionSummary auction) {
        this.selectedAuction = auction;

        // Hiển thị thông tin phiên lên nhãn
        lblAuctionInfo.setText(
                "📦 " + auction.getItemName()
                + "\n💰 Giá hiện tại: " + formatPrice(auction.getCurrentPrice())
                + "   |   Tối thiểu tiếp theo: " + formatPrice(auction.getMinimumNextBid()));

        // Gợi ý sẵn bước giá bằng minIncrement của phiên
        double suggestedIncrement = auction.getMinimumNextBid() - auction.getCurrentPrice();
        txtIncrement.setText(String.valueOf((long) suggestedIncrement));

        // Tải trạng thái auto-bid hiện tại (nếu bidder đã đăng ký trước đó)
        loadCurrentAutoBidStatus();
    }

    /**
     * Tải trạng thái auto-bid từ server trong background thread.
     * Nếu đang có auto-bid → hiện panel thông tin + nút Huỷ.
     */
    private void loadCurrentAutoBidStatus() {
        new Thread(() -> {
            try {
                AutoBidResult result = ClientSocket.getAutoBidStatus(selectedAuction.getAuctionId());
                Platform.runLater(() -> applyAutoBidStatus(result));
            } catch (Exception e) {
                Platform.runLater(() ->
                        setMessage("⚠️ Không thể tải trạng thái auto-bid.", false));
            }
        }).start();
    }

    /**
     * Cập nhật UI theo trạng thái auto-bid nhận được từ server.
     */
    private void applyAutoBidStatus(AutoBidResult result) {
        if (result.isHasActiveBid()) {
            // Có auto-bid đang chạy → hiện panel thông tin và nút Huỷ
            activeAutoBidBox.setVisible(true);
            activeAutoBidBox.setManaged(true);
            btnCancelAutoBid.setVisible(true);
            btnCancelAutoBid.setManaged(true);
            lblCurrentAutoBid.setText(
                    "Max Bid: " + formatPrice(result.getMaxBid())
                    + "   |   Bước giá: " + formatPrice(result.getIncrement()));
            // Điền sẵn giá trị cũ vào form (tiện cập nhật)
            txtMaxBid.setText(String.valueOf((long) result.getMaxBid()));
            txtIncrement.setText(String.valueOf((long) result.getIncrement()));
        } else {
            // Chưa có auto-bid → ẩn panel và nút Huỷ
            activeAutoBidBox.setVisible(false);
            activeAutoBidBox.setManaged(false);
            btnCancelAutoBid.setVisible(false);
            btnCancelAutoBid.setManaged(false);
        }
    }

    /**
     * Xử lý nút "Đặt Auto Bid":
     *   1. Đọc và validate tham số từ form
     *   2. Gửi yêu cầu SET_AUTO_BID lên server (trong background thread)
     *   3. Cập nhật UI theo kết quả trả về
     */
    @FXML
    private void handleSetAutoBid() {
        if (selectedAuction == null) {
            setMessage("❌ Chưa chọn phiên đấu giá.", false);
            return;
        }

        // Đọc và kiểm tra giá trị nhập vào
        double maxBid, increment;
        try {
            maxBid    = Double.parseDouble(txtMaxBid.getText().trim());
            increment = Double.parseDouble(txtIncrement.getText().trim());
        } catch (NumberFormatException e) {
            setMessage("❌ Vui lòng nhập số hợp lệ cho Giá tối đa và Bước giá.", false);
            return;
        }

        if (maxBid <= 0 || increment <= 0) {
            setMessage("❌ Giá tối đa và Bước giá phải lớn hơn 0.", false);
            return;
        }

        // Vô hiệu hoá nút trong lúc chờ phản hồi
        btnSetAutoBid.setDisable(true);
        setMessage("⏳ Đang gửi yêu cầu...", true);

        double finalMaxBid    = maxBid;
        double finalIncrement = increment;

        new Thread(() -> {
            try {
                AutoBidResult result = ClientSocket.sendSetAutoBid(
                        selectedAuction.getAuctionId(), finalMaxBid, finalIncrement);
                Platform.runLater(() -> {
                    btnSetAutoBid.setDisable(false);
                    setMessage(result.isSuccess() ? "✅ " + result.getMessage()
                                                  : "❌ " + result.getMessage(),
                               result.isSuccess());
                    if (result.isSuccess()) {
                        // Cập nhật lại trạng thái hiển thị
                        applyAutoBidStatus(result);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btnSetAutoBid.setDisable(false);
                    setMessage("❌ Lỗi kết nối: " + e.getMessage(), false);
                });
            }
        }).start();
    }

    /**
     * Xử lý nút "Huỷ Auto Bid":
     *   Gửi yêu cầu CANCEL_AUTO_BID lên server, ẩn panel auto-bid nếu thành công.
     */
    @FXML
    private void handleCancelAutoBid() {
        if (selectedAuction == null) return;

        new Thread(() -> {
            try {
                AutoBidResult result = ClientSocket.sendCancelAutoBid(selectedAuction.getAuctionId());
                Platform.runLater(() -> {
                    setMessage(result.isSuccess() ? "✅ " + result.getMessage()
                                                  : "❌ " + result.getMessage(),
                               result.isSuccess());
                    if (result.isSuccess()) {
                        // Ẩn trạng thái active và nút Huỷ
                        activeAutoBidBox.setVisible(false);
                        activeAutoBidBox.setManaged(false);
                        btnCancelAutoBid.setVisible(false);
                        btnCancelAutoBid.setManaged(false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        setMessage("❌ Lỗi kết nối: " + e.getMessage(), false));
            }
        }).start();
    }

    // ── Helper: định dạng số tiền ──

    private String formatPrice(double price) {
        return String.format("%,.0f đ", price);
    }

    private void setMessage(String msg, boolean isSuccess) {
        lblResult.setText(msg);
        lblResult.getStyleClass().removeAll("message-success", "message-error", "message-info");
        lblResult.getStyleClass().add(isSuccess ? "message-success" : "message-error");
    }
}
