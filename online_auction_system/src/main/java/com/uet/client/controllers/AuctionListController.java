package com.uet.client.controllers;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class AuctionListController {
    
    @FXML
    private VBox auctionDetailBox;

    @FXML
    private TableView<?> auctionTable;

    @FXML
    private TextField autoBidIncrement;

    @FXML
    private TextField autoBidMax;

    @FXML
    private TextField bidAmount;

    @FXML
    private VBox bidSection;

    @FXML
    private Label bidStatusLabel;

    @FXML
    private TableColumn<?, String> colBidCount;

    @FXML
    private TableColumn<?, String> colCurrentPrice;

    @FXML
    private TableColumn<?, String> colEndTime;

    @FXML
    private TableColumn<?, String> colItemName;

    @FXML
    private TableColumn<?, String> colItemType;

    @FXML
    private TableColumn<?, String> colStatus;

    @FXML
    private ComboBox<String> statusFilter;
    private final ObservableList<?> AllAuctions = FXCollections.observableArrayList();

    @FXML
    void handleCancelAutoBid(ActionEvent event) {

    }

    @FXML
    void handlePlaceBid(ActionEvent event) {

    }

    @FXML
    void handleRefresh(ActionEvent event) {

    }

    @FXML
    void handleSetAutoBid(ActionEvent event) {

    }
    // private void setupTable(){
    //     // Cấu hình các cột của bảng ở đây
    //     colItemName.setCellValueFactory(...);
    //     colCurrentPrice.setCellValueFactory(...);
    //     // Cấu hình các cột khác tương tự


    //     //Đổi màu cho cột dựa vào trạng thái
    //     auctionTable.setRowFactory(tv -> new TableRow<>() {
    //         @Override
    //         protected void updateItem(Auction item, boolean empty) {
    //             super.updateItem(item, empty);
    //             if (empty || item == null) {
    //                 setStyle("");
    //             } else {
    //                 setStyle(switch (item.getStatus()) {
    //                     case RUNNING -> "-fx-background-color: #0d2b0d;";
    //                     case FINISHED -> "-fx-background-color: #2b1a0d;";
    //                     case CANCELED -> "-fx-background-color: #2b0d0d;";
    //                     default -> "";
    //                 });
    //             }
    //         }
    //     });
    // }
    // Thiết kế lọc theo trạng thái
    @FXML
    private void setupFilter(){
        statusFilter.getItems().addAll("Tất cả","OPEN","RUNNING", "FINISHED", "PAID", "CANCELED");
        statusFilter.setValue("Tất cả");
      //   statusFilter.setOnAction(e -> applyFilter());
    }
    // private void applyFilter(){
    //     String filter = statusFilter.getValue();
    //     // Lọc dữ liệu dựa trên selectedStatus và cập nhật auctionTable
    //     if (filter==null || filter.equals("Tất cả")) {
    //         // Hiển thị tất cả
    //         auctionTable.setItems(AllAuctions);
    //     } else {
    //         // Lọc theo trạng thái
    //         ObservableList<Auction> filtered = FXCollections.observableArrayList(
    //                 AllAuctions.filtered(a -> a.getStatus().name().equals(filter)));
    //         auctionTable.setItems(filtered);
    //     }
    // }
}
