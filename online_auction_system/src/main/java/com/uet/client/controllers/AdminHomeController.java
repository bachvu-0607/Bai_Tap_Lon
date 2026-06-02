package com.uet.client.controllers;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.uet.client.core.ClientSocket;
import com.uet.client.utils.SceneManager;
import com.uet.domain.AuctionSummary;
import com.uet.domain.event.ServerEventType;
import com.uet.domain.result.AuctionActionResult;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Bộ điều khiển (Controller) trang chủ dành cho Quản trị viên (Admin Home).
 * Quản lý giao diện phê duyệt/từ chối các phiên đấu giá mới do Seller đăng tải,
 * cập nhật danh sách chờ duyệt thời gian thực thông qua kết nối Socket.
 */
public class AdminHomeController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private TableView<AuctionSummary> tblPendingAuctions;
    @FXML
    private TableColumn<AuctionSummary, String> colItem;
    @FXML
    private TableColumn<AuctionSummary, String> colCategory;
    @FXML
    private TableColumn<AuctionSummary, String> colSeller;
    @FXML
    private TableColumn<AuctionSummary, Number> colOpeningPrice;
    @FXML
    private TableColumn<AuctionSummary, String> colStatus;
    @FXML
    private TableColumn<AuctionSummary, String> colEndTime;
    @FXML
    private Button btnApprove;
    @FXML
    private Button btnReject;
    @FXML
    private Button btnRefresh;
    @FXML
    private Hyperlink hplSignOut;
    @FXML
    private Label lblMessage;

    /**
     * Khởi tạo giao diện Admin Home.
     * Liên kết các cột của bảng TableView với các trường dữ liệu tương ứng trong {@link AuctionSummary},
     * tải dữ liệu phiên đấu giá chờ duyệt và đăng ký sự kiện lắng nghe cập nhật realtime từ Server.
     */
    @FXML
    private void initialize() {
        colItem.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getItemName()));
        colCategory.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory()));
        colSeller.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSellerName()));
        colOpeningPrice.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getCurrentPrice()));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));
        colStatus.setCellFactory(column -> new StatusBadgeCell<>());
        colEndTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEndTime().format(TIME_FORMAT)));
        tblPendingAuctions.setPlaceholder(new Label("No pending auctions."));
        loadPendingAuctions();

        ClientSocket.setEventListener(event -> {
            if (event.getType() == ServerEventType.AUCTION_UPDATED) {
                Platform.runLater(() -> loadPendingAuctions());
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadPendingAuctions();
    }

    @FXML
    private void handleApprove() {
        handleAuctionAction(true);
    }

    @FXML
    private void handleReject() {
        handleAuctionAction(false);
    }

    @FXML
    private void handleSignOut() {
        ClientSocket.sendDisconnect();
        SceneManager.switchScene(hplSignOut, "/com/uet/views/SignIn.fxml", "Sign In", 600, 400);
    }

    private void handleAuctionAction(boolean approve) {
        AuctionSummary selectedAuction = tblPendingAuctions.getSelectionModel().getSelectedItem();
        if (selectedAuction == null) {
            setErrorMessage("Please select an auction first.");
            return;
        }

        try {
            AuctionActionResult result = approve
                    ? ClientSocket.approveAuction(selectedAuction.getAuctionId())
                    : ClientSocket.rejectAuction(selectedAuction.getAuctionId());
            if (result.isSuccess()) {
                setSuccessMessage(result.getMessage());
            } else {
                setErrorMessage(result.getMessage());
            }
            loadPendingAuctions();
        } catch (Exception e) {
            setErrorMessage("Cannot update auction: " + e.getMessage());
        }
    }

    private void loadPendingAuctions() {
        try {
            List<AuctionSummary> auctions = ClientSocket.getPendingAuctionList();
            tblPendingAuctions.setItems(FXCollections.observableArrayList(auctions));
            setInfoMessage("Loaded " + auctions.size() + " pending auctions.");
        } catch (Exception e) {
            setErrorMessage("Cannot load pending auctions: " + e.getMessage());
        }
    }

    private void setInfoMessage(String message) {
        setMessage(message, "message-info");
    }

    private void setSuccessMessage(String message) {
        setMessage(message, "message-success");
    }

    private void setErrorMessage(String message) {
        setMessage(message, "message-error");
    }

    private void setMessage(String message, String styleClass) {
        lblMessage.setText(message);
        lblMessage.getStyleClass().removeAll("message-info", "message-success", "message-error");
        lblMessage.getStyleClass().add(styleClass);
    }

    private static class StatusBadgeCell<T> extends TableCell<T, String> {
        @Override
        protected void updateItem(String status, boolean empty) {
            super.updateItem(status, empty);
            if (empty || status == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            Label badge = new Label(status);
            badge.getStyleClass().addAll("status-badge", statusStyleClass(status));
            setGraphic(badge);
            setText(null);
        }

        private static String statusStyleClass(String status) {
            return switch (status) {
                case "RUNNING" -> "status-running";
                case "OPEN" -> "status-open";
                case "PENDING_APPROVAL" -> "status-pending";
                case "FINISHED" -> "status-finished";
                case "PAID" -> "status-paid";
                case "CANCELED" -> "status-canceled";
                case "REJECTED" -> "status-rejected";
                default -> "status-open";
            };
        }
    }
}
