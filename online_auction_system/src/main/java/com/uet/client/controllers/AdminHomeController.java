package com.uet.client.controllers;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.uet.client.core.ClientSocket;
import com.uet.client.utils.MessageHelper;
import com.uet.client.utils.SceneManager;
import com.uet.domain.summary.AuctionSummary;
import com.uet.domain.summary.UserSummary;
import com.uet.domain.event.ServerEventType;
import com.uet.domain.result.AuctionActionResult;
import com.uet.domain.result.UserActionResult;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

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
    private TableView<UserSummary> tblUsers;
    @FXML
    private TableColumn<UserSummary, String> colUserId;
    @FXML
    private TableColumn<UserSummary, String> colUserName;
    @FXML
    private TableColumn<UserSummary, String> colUserPhone;
    @FXML
    private TableColumn<UserSummary, String> colUserRole;
    @FXML
    private TableColumn<UserSummary, String> colUserStatus;
    @FXML
    private Button btnApprove;
    @FXML
    private Button btnReject;
    @FXML
    private Button btnRefresh;
    @FXML
    private Button btnBanUser;
    @FXML
    private Hyperlink hplSignOut;
    @FXML
    private Label lblMessage;

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
        colUserId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSystemId()));
        colUserName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        colUserPhone.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPhone()));
        colUserRole.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRole()));
        colUserStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatusText()));
        colUserStatus.setCellFactory(column -> new StatusBadgeCell<>());
        tblUsers.setPlaceholder(new Label("No users available."));
        loadPendingAuctions();
        loadUsers();

        ClientSocket.addEventListener(event -> {
            if (event.getType() == ServerEventType.AUCTION_UPDATED) {
                Platform.runLater(() -> loadPendingAuctions());
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadPendingAuctions();
        loadUsers();
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

    @FXML
    private void handleBanUser() {
        UserSummary selectedUser = tblUsers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            MessageHelper.error(lblMessage, "Please select a user first.");
            return;
        }

        btnBanUser.setDisable(true);
        MessageHelper.info(lblMessage, "Banning user...");

        Task<UserActionResult> banTask = new Task<>() {
            @Override
            protected UserActionResult call() throws Exception {
                return ClientSocket.banUser(selectedUser.getSystemId());
            }
        };

        banTask.setOnSucceeded(event -> {
            UserActionResult result = banTask.getValue();
            if (result.isSuccess()) {
                MessageHelper.success(lblMessage, result.getMessage());
            } else {
                MessageHelper.error(lblMessage, result.getMessage());
            }
            loadUsers();
            loadPendingAuctions();
            btnBanUser.setDisable(false);
        });

        banTask.setOnFailed(event -> {
            Throwable error = banTask.getException();
            MessageHelper.error(lblMessage, "Cannot ban user: " + error.getMessage());
            btnBanUser.setDisable(false);
        });

        Thread banThread = new Thread(banTask, "ban-user-request");
        banThread.setDaemon(true);
        banThread.start();
    }

    private void handleAuctionAction(boolean approve) {
        AuctionSummary selectedAuction = tblPendingAuctions.getSelectionModel().getSelectedItem();
        if (selectedAuction == null) {
            MessageHelper.error(lblMessage, "Please select an auction first.");
            return;
        }

        try {
            AuctionActionResult result = approve
                    ? ClientSocket.approveAuction(selectedAuction.getAuctionId())
                    : ClientSocket.rejectAuction(selectedAuction.getAuctionId());
            if (result.isSuccess()) {
                MessageHelper.success(lblMessage, result.getMessage());
            } else {
                MessageHelper.error(lblMessage, result.getMessage());
            }
            loadPendingAuctions();
        } catch (Exception e) {
            MessageHelper.error(lblMessage, "Cannot update auction: " + e.getMessage());
        }
    }

    private void loadPendingAuctions() {
        try {
            List<AuctionSummary> auctions = ClientSocket.getPendingAuctionList();
            tblPendingAuctions.setItems(FXCollections.observableArrayList(auctions));
            MessageHelper.info(lblMessage, "Loaded " + auctions.size() + " pending auctions.");
        } catch (Exception e) {
            MessageHelper.error(lblMessage, "Cannot load pending auctions: " + e.getMessage());
        }
    }

    private void loadUsers() {
        try {
            List<UserSummary> users = ClientSocket.getUserList();
            tblUsers.setItems(FXCollections.observableArrayList(users));
        } catch (Exception e) {
            MessageHelper.error(lblMessage, "Cannot load users: " + e.getMessage());
        }
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
                case "RUNNING" -> "auction-running";
                case "OPEN" -> "auction-open";
                case "PENDING_APPROVAL" -> "auction-pending";
                case "FINISHED" -> "auction-finished";
                case "PAID" -> "auction-paid";
                case "CANCELED" -> "auction-canceled";
                case "REJECTED" -> "auction-rejected";
                case "ACTIVE" -> "account-active";
                case "BANNED" -> "account-banned";
                default -> "auction-open";
            };
        }
    }
}
