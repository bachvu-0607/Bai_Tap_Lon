package com.uet.client.controllers;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.uet.client.core.ClientSocket;
import com.uet.client.utils.SceneManager;
import com.uet.domain.AuctionSummary;
import com.uet.domain.UserSummary;
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
import javafx.scene.layout.VBox;

public class AdminHomeController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // --- Auction section ---
    @FXML private VBox sectionAuctions;
    @FXML private TableView<AuctionSummary> tblPendingAuctions;
    @FXML private TableColumn<AuctionSummary, String> colItem;
    @FXML private TableColumn<AuctionSummary, String> colCategory;
    @FXML private TableColumn<AuctionSummary, String> colSeller;
    @FXML private TableColumn<AuctionSummary, Number> colOpeningPrice;
    @FXML private TableColumn<AuctionSummary, String> colStatus;
    @FXML private TableColumn<AuctionSummary, String> colEndTime;
    @FXML private Button btnApprove;
    @FXML private Button btnReject;
    @FXML private Button btnRefresh;
    @FXML private Label lblMessage;

    // --- User section ---
    @FXML private VBox sectionUsers;
    @FXML private TableView<UserSummary> tblUsers;
    @FXML private TableColumn<UserSummary, String> colUserName;
    @FXML private TableColumn<UserSummary, String> colUserPhone;
    @FXML private TableColumn<UserSummary, String> colUserCitizenId;
    @FXML private TableColumn<UserSummary, String> colUserRole;
    @FXML private TableColumn<UserSummary, String> colUserAddress;
    @FXML private Button btnRemoveUser;
    @FXML private Button btnReloadUsers;
    @FXML private Label lblUserMessage;

    // --- Nav ---
    @FXML private Hyperlink hplAuctions;
    @FXML private Hyperlink hplUsers;
    @FXML private Hyperlink hplSignOut;

    @FXML
    private void initialize() {
        setupAuctionTable();
        setupUserTable();
        loadPendingAuctions();

        ClientSocket.setEventListener(event -> {
            if (event.getType() == ServerEventType.AUCTION_UPDATED) {
                Platform.runLater(() -> {
                    if (sectionAuctions.isVisible()) {
                        loadPendingAuctions();
                    }
                });
            }
        });
    }

    // ================================================================
    // Navigation
    // ================================================================

    @FXML
    private void showAuctions() {
        sectionAuctions.setVisible(true);
        sectionAuctions.setManaged(true);
        sectionUsers.setVisible(false);
        sectionUsers.setManaged(false);
        loadPendingAuctions();
    }

    @FXML
    private void showUsers() {
        sectionAuctions.setVisible(false);
        sectionAuctions.setManaged(false);
        sectionUsers.setVisible(true);
        sectionUsers.setManaged(true);
        loadUsers();
    }

    // ================================================================
    // Auction Approval
    // ================================================================

    private void setupAuctionTable() {
        colItem.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getItemName()));
        colCategory.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory()));
        colSeller.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSellerName()));
        colOpeningPrice.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getCurrentPrice()));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));
        colStatus.setCellFactory(column -> new StatusBadgeCell<>());
        colEndTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEndTime().format(TIME_FORMAT)));
        tblPendingAuctions.setPlaceholder(new Label("No pending auctions."));
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
        AuctionSummary selected = tblPendingAuctions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setMessage(lblMessage, "Please select an auction first.", "message-error");
            return;
        }
        try {
            AuctionActionResult result = approve
                    ? ClientSocket.approveAuction(selected.getAuctionId())
                    : ClientSocket.rejectAuction(selected.getAuctionId());
            if (result.isSuccess()) {
                setMessage(lblMessage, result.getMessage(), "message-success");
            } else {
                setMessage(lblMessage, result.getMessage(), "message-error");
            }
            loadPendingAuctions();
        } catch (Exception e) {
            setMessage(lblMessage, "Cannot update auction: " + e.getMessage(), "message-error");
        }
    }

    private void loadPendingAuctions() {
        try {
            List<AuctionSummary> auctions = ClientSocket.getPendingAuctionList();
            tblPendingAuctions.setItems(FXCollections.observableArrayList(auctions));
            setMessage(lblMessage, "Loaded " + auctions.size() + " pending auctions.", "message-info");
        } catch (Exception e) {
            setMessage(lblMessage, "Cannot load pending auctions: " + e.getMessage(), "message-error");
        }
    }

    // ================================================================
    // User Management
    // ================================================================

    private void setupUserTable() {
        colUserName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        colUserPhone.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPhone()));
        colUserCitizenId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCitizenId()));
        colUserRole.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRole()));
        colUserRole.setCellFactory(column -> new RoleBadgeCell<>());
        colUserAddress.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAddress()));
        tblUsers.setPlaceholder(new Label("No users found."));
    }

    @FXML
    private void handleRemoveUser() {
        UserSummary selected = tblUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setMessage(lblUserMessage, "Please select a user first.", "message-error");
            return;
        }
        try {
            AuctionActionResult result = ClientSocket.removeUser(selected.getSystemId());
            if (result.isSuccess()) {
                setMessage(lblUserMessage, result.getMessage(), "message-success");
            } else {
                setMessage(lblUserMessage, result.getMessage(), "message-error");
            }
            loadUsers();
        } catch (Exception e) {
            setMessage(lblUserMessage, "Cannot remove user: " + e.getMessage(), "message-error");
        }
    }

    @FXML
    private void handleReloadUsers() {
        loadUsers();
    }

    private void loadUsers() {
        try {
            List<UserSummary> users = ClientSocket.getUserList();
            tblUsers.setItems(FXCollections.observableArrayList(users));
            setMessage(lblUserMessage, "Loaded " + users.size() + " users.", "message-info");
        } catch (Exception e) {
            setMessage(lblUserMessage, "Cannot load users: " + e.getMessage(), "message-error");
        }
    }

    // ================================================================
    // Helpers
    // ================================================================

    private void setMessage(Label label, String message, String styleClass) {
        label.setText(message);
        label.getStyleClass().removeAll("message-info", "message-success", "message-error");
        label.getStyleClass().add(styleClass);
    }

    private static class StatusBadgeCell<T> extends TableCell<T, String> {
        @Override
        protected void updateItem(String status, boolean empty) {
            super.updateItem(status, empty);
            if (empty || status == null) { setGraphic(null); setText(null); return; }
            Label badge = new Label(status);
            badge.getStyleClass().addAll("status-badge", statusStyleClass(status));
            setGraphic(badge);
            setText(null);
        }

        private static String statusStyleClass(String status) {
            return switch (status) {
                case "RUNNING" -> "auction-running";
                case "OPEN" -> "auction-open";
                case "PENDING_APPROVAL" -> "auction-pending";
                case "FINISHED" -> "auction-finished";
                case "PAID" -> "auction-paid";
                case "CANCELED" -> "auction-canceled";
                case "REJECTED" -> "auction-rejected";
                default -> "auction-open";
            };
        }
    }

    private static class RoleBadgeCell<T> extends TableCell<T, String> {
        @Override
        protected void updateItem(String role, boolean empty) {
            super.updateItem(role, empty);
            if (empty || role == null) { setGraphic(null); setText(null); return; }
            Label badge = new Label(role);
            badge.getStyleClass().addAll("status-badge", roleStyleClass(role));
            setGraphic(badge);
            setText(null);
        }

        private static String roleStyleClass(String role) {
            return switch (role) {
                case "Bidder" -> "role-bidder";
                case "Seller" -> "role-seller";
                default -> "role-bidder";
            };
        }
    }
}
