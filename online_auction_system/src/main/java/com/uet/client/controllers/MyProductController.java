package com.uet.client.controllers;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.uet.client.core.ClientSocket;
import com.uet.client.utils.MessageHelper;
import com.uet.domain.AuctionSummary;
import com.uet.domain.event.ServerEventType;

import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class MyProductController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private Hyperlink hpl_PostProduct;
    @FXML
    private Button btnRefresh;
    @FXML
    private Label lblMessage;
    @FXML
    private TableView<AuctionSummary> tblProducts;
    @FXML
    private TableColumn<AuctionSummary, String> colProduct;
    @FXML
    private TableColumn<AuctionSummary, String> colDescription;
    @FXML
    private TableColumn<AuctionSummary, String> colCategory;
    @FXML
    private TableColumn<AuctionSummary, Number> colCurrentPrice;
    @FXML
    private TableColumn<AuctionSummary, Number> colMinBid;
    @FXML
    private TableColumn<AuctionSummary, String> colStatus;
    @FXML
    private TableColumn<AuctionSummary, String> colStartTime;
    @FXML
    private TableColumn<AuctionSummary, String> colEndTime;
    @FXML
    private TableColumn<AuctionSummary, String> colWinner;
    @FXML
    private VBox vbox_OrderList;

    @FXML
    private void initialize() {
        colProduct.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getItemName()));
        colDescription.setCellValueFactory(cell -> new SimpleStringProperty(formatDescription(cell.getValue().getDescription())));
        colCategory.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory()));
        colCurrentPrice.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getCurrentPrice()));
        colMinBid.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getMinimumNextBid()));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));
        colStatus.setCellFactory(column -> new StatusBadgeCell<>());
        colStartTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStartTime().format(TIME_FORMAT)));
        colEndTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEndTime().format(TIME_FORMAT)));
        colWinner.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCurrentWinnerName()));
        tblProducts.setPlaceholder(new Label("No product posted yet."));
        loadSellerProducts();

        ClientSocket.setEventListener(event -> {
            if (event.getType() == ServerEventType.AUCTION_UPDATED) {
                Platform.runLater(() -> loadSellerProducts());
            }
        });
    }

    @FXML
    private void handleOpenPostProduct(ActionEvent event) {
        try {
            Node node = FXMLLoader.load(getClass().getResource("/com/uet/views/PostProduct.fxml"));
            vbox_OrderList.getChildren().setAll(node);
            tblProducts.setManaged(false);
            tblProducts.setVisible(false);
            vbox_OrderList.setManaged(true);
            vbox_OrderList.setVisible(true);
            MessageHelper.info(lblMessage, "Posting a new product.");
        } catch (IOException e) {
            e.printStackTrace();
            MessageHelper.error(lblMessage, "Cannot open post product form.");
        }
    }

    @FXML
    private void handleRefresh() {
        vbox_OrderList.setManaged(false);
        vbox_OrderList.setVisible(false);
        tblProducts.setManaged(true);
        tblProducts.setVisible(true);
        loadSellerProducts();
    }

    private void loadSellerProducts() {
        try {
            List<AuctionSummary> products = ClientSocket.getSellerProductList();
            tblProducts.setItems(FXCollections.observableArrayList(products));
            MessageHelper.info(lblMessage, "Loaded " + products.size() + " products.");
        } catch (Exception e) {
            MessageHelper.error(lblMessage, "Cannot load products: " + e.getMessage());
        }
    }

    private String formatDescription(String description) {
        if (description == null || description.isBlank()) {
            return "-";
        }
        return description;
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
                default -> "auction-open";
            };
        }
    }
}
