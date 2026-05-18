package com.uet.client.controllers;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.uet.client.core.ClientSocket;
import com.uet.domain.AuctionSummary;
import com.uet.domain.BidHistoryPoint;
import com.uet.domain.event.ServerEventType;
import com.uet.domain.result.BidResult;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
public class AuctionListController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter CHART_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final double DRAWER_WIDTH = 500;
    private static final double DRAWER_HIDDEN_OFFSET = 560;
    private boolean bidHistoryPanelOpen = false;

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
    private TableView<BidHistoryPoint> tblBidHistory;
    @FXML
    private TableColumn<BidHistoryPoint, String> colBidTime;
    @FXML
    private TableColumn<BidHistoryPoint, String> colBidderName;
    @FXML
    private TableColumn<BidHistoryPoint, Number> colBidAmount;
    @FXML
    private TableColumn<BidHistoryPoint, String> colBidStatus;
    @FXML
    private VBox bidHistoryPanel;
    @FXML
    private Label lblBidHistoryTitle;
    @FXML
    private Label lblBidHistoryMeta;
    @FXML
    private LineChart<String, Number> bidPriceChart;
    @FXML
    private CategoryAxis bidChartXAxis;
    @FXML
    private NumberAxis bidChartYAxis;

    @FXML
    // Hàm JavaFX tự gọi sau khi load FXML: cấu hình bảng, chart, listener chọn dòng và listener realtime.
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

        colBidTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getBidTime().format(TIME_FORMAT)));
        colBidderName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getBidderName()));
        colBidAmount.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getAmount()));
        colBidStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));
        colBidStatus.setCellFactory(column -> new StatusBadgeCell<>());
        tblBidHistory.setPlaceholder(new Label("Select an auction to view bid history."));
        bidPriceChart.setTitle("Price Movement");
        bidChartXAxis.setLabel("Bid");
        bidChartYAxis.setLabel("Price");
        bidHistoryPanel.setTranslateX(DRAWER_HIDDEN_OFFSET);
        bidHistoryPanel.setVisible(false);
        bidHistoryPanel.setMouseTransparent(true);
        
        tblAuctions.getSelectionModel().selectedItemProperty().addListener((obs, oldAuction, selectedAuction) -> {
            handleAuctionHighlighted(selectedAuction);
        });
        tblAuctions.setRowFactory(table -> createAuctionRow());
        loadAuctions();

        ClientSocket.setEventListener(event ->{
            if(event.getType() == ServerEventType.AUCTION_UPDATED){
                Platform.runLater(() -> loadAuctions());
            }
        });

        //Ko hiểu cái trên thì đọc bạn ko rút gọn bằng lamda
        /*
        ClientSocket.setEventListener(new Consumer<ServerEvent>() {
            @Override
            public void accept(ServerEvent event) {
                if (event.getType() == ServerEventType.AUCTION_UPDATED) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            loadAuctions();
                        }
                    });
                }
            }
        }); 
        */
    }

    @FXML
    // Hàm xử lý nút Refresh: tải lại danh sách auction từ server.
    private void handleRefresh() {
        loadAuctions();
    }

    @FXML
    // Hàm xử lý nút Close trong drawer: giấu panel bid history/chart.
    private void handleCloseHistory() {
        hideBidHistoryPanel();
    }

    @FXML
    // Hàm xử lý nút Place Bid: kiểm tra dòng đang chọn, đọc số tiền và gửi bid request lên server.
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
    
    // Hàm tải lại auction list từ server và giữ lại dòng đang chọn nếu auction đó vẫn còn trong danh sách.
    private void loadAuctions() {
        try {
            AuctionSummary selectedBeforeReload = tblAuctions.getSelectionModel().getSelectedItem();
            String selectedAuctionId = selectedBeforeReload == null ? null : selectedBeforeReload.getAuctionId();
            List<AuctionSummary> auctions = ClientSocket.getAuctionList();
            ObservableList<AuctionSummary> auctionItems = FXCollections.observableArrayList(auctions);
            tblAuctions.setItems(auctionItems);
            if (auctionItems.isEmpty()) {
                txtBidAmount.clear();
                updateBidHistoryView(List.of());
                hideBidHistoryPanel();
                setInfoMessage("Loaded 0 auctions. Restart server to seed demo auctions.");
            } else {
                if (selectedAuctionId == null) {
                    tblAuctions.getSelectionModel().clearSelection();
                    setInfoMessage("Loaded " + auctions.size() + " auctions.");
                    return;
                }

                auctionItems.stream()
                        .filter(auction -> auction.getAuctionId().equals(selectedAuctionId))
                        .findFirst()
                        .ifPresentOrElse(
                                auction -> {
                                    tblAuctions.getSelectionModel().select(auction);
                                    if (bidHistoryPanelOpen) {
                                        refreshAuctionDetail(auction);
                                    }
                                },
                                () -> {
                                    tblAuctions.getSelectionModel().clearSelection();
                                    hideBidHistoryPanel();
                                });
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

    // Hàm tạo từng dòng của bảng auction để tự xử lý click đơn, double click và bỏ chọn.
    private TableRow<AuctionSummary> createAuctionRow() {
        TableRow<AuctionSummary> row = new TableRow<>();
        row.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> handleAuctionRowClick(row, event));
        return row;
    }

    // Hàm xử lý thao tác chuột trên một dòng auction: click đơn để chọn/bỏ chọn, double click để mở drawer.
    private void handleAuctionRowClick(TableRow<AuctionSummary> row, MouseEvent event) {
        if (row.isEmpty()) {
            clearAuctionSelection();
            event.consume();
            return;
        }

        AuctionSummary clickedAuction = row.getItem();
        AuctionSummary selectedAuction = tblAuctions.getSelectionModel().getSelectedItem();
        boolean alreadySelected = selectedAuction != null
                && selectedAuction.getAuctionId().equals(clickedAuction.getAuctionId());

        if (event.getClickCount() == 2) {
            tblAuctions.getSelectionModel().select(clickedAuction);
            handleAuctionSelected(clickedAuction);
            event.consume();
            return;
        }

        if (event.getClickCount() == 1) {
            if (alreadySelected) {
                clearAuctionSelection();
            } else {
                tblAuctions.getSelectionModel().select(clickedAuction);
            }
            event.consume();
        }
    }

    // Hàm bỏ chọn auction hiện tại, xóa bid amount, xóa chart/history và đóng drawer.
    private void clearAuctionSelection() {
        tblAuctions.getSelectionModel().clearSelection();
        txtBidAmount.clear();
        updateBidHistoryView(List.of());
        hideBidHistoryPanel();
        setInfoMessage("No auction selected.");
    }

    // Hàm chạy khi dòng được chọn bằng click đơn: chỉ điền minimum bid và hướng dẫn double click.
    private void handleAuctionHighlighted(AuctionSummary selectedAuction) {
        if (selectedAuction == null) {
            txtBidAmount.clear();
            return;
        }

        txtBidAmount.setText(String.valueOf(selectedAuction.getMinimumNextBid()));
        setInfoMessage("Selected: " + selectedAuction.getItemName() + ". Double click to view details.");
    }

    // Hàm chạy khi double click auction: mở drawer và tải detail/history/chart của auction đó.
    private void handleAuctionSelected(AuctionSummary selectedAuction){
        if(selectedAuction == null){
            clearAuctionSelection();
            return;
        }

        txtBidAmount.setText(String.valueOf(selectedAuction.getMinimumNextBid()));
        showBidHistoryPanel();
        refreshAuctionDetail(selectedAuction);
    }

    // Hàm cập nhật phần detail bên phải: thông tin auction, bid history và chart.
    private void refreshAuctionDetail(AuctionSummary selectedAuction) {
        lblBidHistoryTitle.setText(selectedAuction.getItemName());
        lblBidHistoryMeta.setText(
                "Status: " + selectedAuction.getStatus()
                + "\nCurrent price: " + selectedAuction.getCurrentPrice()
                + "\nMinimum next bid: " + selectedAuction.getMinimumNextBid()
                + "\nCurrent winner: " + selectedAuction.getCurrentWinnerName());

        try {
            List<BidHistoryPoint> bidHistory = ClientSocket.getHistoryBidList(selectedAuction.getAuctionId());
            updateBidHistoryView(bidHistory);
            setInfoMessage("Selected: " + selectedAuction.getItemName()
                    + " | Bids: " + bidHistory.size());
        } catch (Exception e) {
            setErrorMessage("Cannot load bid history: " + e.getMessage());
        }
    }

    // Hàm đổ bid history vào bảng nhỏ và vẽ lại line chart giá theo thời gian bid.
    private void updateBidHistoryView(List<BidHistoryPoint> bidHistory) {
        tblBidHistory.setItems(FXCollections.observableArrayList(bidHistory));
        bidPriceChart.getData().clear();

        if (bidHistory == null || bidHistory.isEmpty()) {
            return;
        }

        XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
        for (int i = 0; i < bidHistory.size(); i++) {
            BidHistoryPoint point = bidHistory.get(i);
            String xLabel = (i + 1) + " - " + point.getBidTime().format(CHART_TIME_FORMAT);
            priceSeries.getData().add(new XYChart.Data<>(xLabel, point.getAmount()));
        }
        bidPriceChart.getData().add(priceSeries);
    }

    // Hàm mở drawer bid history/chart bằng animation trượt từ phải vào.
    private void showBidHistoryPanel() {
        if (!bidHistoryPanelOpen) {
            bidHistoryPanel.setTranslateX(DRAWER_HIDDEN_OFFSET);
        }
        bidHistoryPanelOpen = true;
        bidHistoryPanel.setVisible(true);
        bidHistoryPanel.setMouseTransparent(false);
        bidHistoryPanel.toFront();

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(180), bidHistoryPanel);
        slideIn.setToX(0);
        slideIn.play();
    }

    // Hàm đóng drawer bằng animation trượt sang phải và giấu hẳn panel sau khi trượt xong.
    private void hideBidHistoryPanel() {
        if (!bidHistoryPanelOpen) {
            return;
        }
        bidHistoryPanelOpen = false;

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(160), bidHistoryPanel);
        slideOut.setToX(DRAWER_HIDDEN_OFFSET);
        slideOut.setOnFinished(event -> {
            bidHistoryPanel.setMouseTransparent(true);
            bidHistoryPanel.setVisible(false);
            bidHistoryPanel.setTranslateX(DRAWER_HIDDEN_OFFSET);
        });
        slideOut.play();
    }

    // Cell tùy chỉnh để render status thành badge màu thay vì text thường.
    private static class StatusBadgeCell<T> extends TableCell<T, String> {
        @Override
        // Hàm JavaFX gọi mỗi khi cell cần render lại status.
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

        // Hàm đổi status string sang class CSS tương ứng để chọn màu badge.
        private static String statusStyleClass(String status) {
            return switch (status) {
                case "RUNNING" -> "auction-running";
                case "OPEN" -> "auction-open";
                case "PENDING_APPROVAL" -> "auction-pending";
                case "FINISHED" -> "auction-finished";
                case "PAID" -> "auction-paid";
                case "CANCELED" -> "auction-canceled";
                case "REJECTED" -> "auction-rejected";
                case "WINNING" -> "bid-winning";
                case "OUTBID" -> "bid-outbid";
                default -> "auction-open";
            };
        }
    }
}
