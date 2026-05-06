package com.uet.client.controllers;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.uet.client.core.ClientSocket;
import com.uet.domain.AuctionSummary;
import com.uet.domain.result.BidResult;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AuctionListController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private TableView<AuctionSummary> tblAuctions;
    @FXML
    private TableColumn<AuctionSummary, String> colItem;
    @FXML
    private TableColumn<AuctionSummary, String> colCategory;
    @FXML
    private TableColumn<AuctionSummary, String> colSeller;
    @FXML
    private TableColumn<AuctionSummary, String> colCurrentWinner;
    @FXML
    private TableColumn<AuctionSummary, Number> colCurrentPrice;
    @FXML
    private TableColumn<AuctionSummary, Number> colMinimumBid;
    @FXML
    private TableColumn<AuctionSummary, String> colStatus;
    @FXML
    private TableColumn<AuctionSummary, String> colEndTime;
    @FXML
    private TextField txtBidAmount;
    @FXML
    private Button btnBid;
    @FXML
    private Button btnRefresh;
    @FXML
    private Label lblMessage;

    @FXML
    private void initialize() {
        colItem.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getItemName()));
        colCategory.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory()));
        colSeller.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSellerName()));
        colCurrentWinner.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCurrentWinnerName()));
        colCurrentPrice.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getCurrentPrice()));
        colMinimumBid.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getMinimumNextBid()));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));
        colStatus.setCellFactory(column -> new StatusBadgeCell<>());
        colEndTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEndTime().format(TIME_FORMAT)));
        tblAuctions.setPlaceholder(new Label("No auctions available. Restart server or press Refresh."));
        tblAuctions.getSelectionModel().selectedItemProperty().addListener((obs, oldAuction, selectedAuction) -> {
            if (selectedAuction != null) {
                txtBidAmount.setText(String.valueOf(selectedAuction.getMinimumNextBid()));
                setInfoMessage("Selected: " + selectedAuction.getItemName());
            }
        });
        loadAuctions();
    }

    @FXML
    private void handleRefresh() {
        loadAuctions();
    }

    @FXML
    private void handleBid() {
        if (tblAuctions.getItems().isEmpty()) {
            setErrorMessage("No auction is available to bid.");
            return;
        }

        AuctionSummary selectedAuction = tblAuctions.getSelectionModel().getSelectedItem();
        if (selectedAuction == null) {
            setErrorMessage("Please click an auction row first.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(txtBidAmount.getText().trim());
        } catch (NumberFormatException e) {
            setErrorMessage("Bid amount must be a number.");
            return;
        }

        try {
            BidResult result = ClientSocket.sendBid(selectedAuction.getAuctionId(), amount);
            if (result.isSuccess()) {
                setSuccessMessage(result.getMessage());
            } else {
                setErrorMessage(result.getMessage());
            }
            if (result.isSuccess()) {
                txtBidAmount.clear();
                loadAuctions();
            }
        } catch (Exception e) {
            setErrorMessage("Cannot place bid: " + e.getMessage());
        }
    }

    private void loadAuctions() {
        try {
            List<AuctionSummary> auctions = ClientSocket.getAuctionList();
            ObservableList<AuctionSummary> auctionItems = FXCollections.observableArrayList(auctions);
            tblAuctions.setItems(auctionItems);
            if (auctionItems.isEmpty()) {
                txtBidAmount.clear();
                setInfoMessage("Loaded 0 auctions. Restart server to seed demo auctions.");
            } else {
                tblAuctions.getSelectionModel().selectFirst();
                setInfoMessage("Loaded " + auctions.size() + " auctions.");
            }
        } catch (Exception e) {
            setErrorMessage("Cannot load auctions: " + e.getMessage());
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
