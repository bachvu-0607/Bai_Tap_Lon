package com.uet.client.controllers;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.uet.client.core.ClientSocket;
import com.uet.client.utils.SessionManager;
import com.uet.domain.WalletTransaction;
import com.uet.domain.entity.user.Bidder;
import com.uet.domain.result.WalletResult;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class WalletController {

    @FXML private Label lbl_totalBalance;
    @FXML private Label lbl_availableBalance;
    @FXML private Label lbl_lockedBalance;
    @FXML private VBox vbox_lockedCard;

    @FXML private TextField txf_depositAmount;
    @FXML private Button btn_deposit;
    @FXML private Label lbl_message;

    @FXML private TableView<WalletTransaction> tbl_transactions;
    @FXML private TableColumn<WalletTransaction, String> col_date;
    @FXML private TableColumn<WalletTransaction, String> col_type;
    @FXML private TableColumn<WalletTransaction, Double> col_amount;
    @FXML private TableColumn<WalletTransaction, String> col_description;

    private static final NumberFormat VND = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        boolean isBidder = SessionManager.currentUser instanceof Bidder;
        if (vbox_lockedCard != null) {
            vbox_lockedCard.setManaged(isBidder);
            vbox_lockedCard.setVisible(isBidder);
        }

        setupTable();
        loadWalletInfo();
    }

    private void setupTable() {
        col_date.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        col_date.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                try {
                    LocalDateTime dt = LocalDateTime.parse(item);
                    setText(dt.format(DATE_FMT));
                } catch (Exception e) {
                    setText(item);
                }
            }
        });

        col_type.setCellValueFactory(new PropertyValueFactory<>("type"));
        col_type.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); getStyleClass().removeAll("message-success", "message-error", "message-info"); return; }
                getStyleClass().removeAll("message-success", "message-error", "message-info");
                switch (item) {
                    case "DEPOSIT"     -> { setText("Nạp tiền");       getStyleClass().add("message-success"); }
                    case "BID_LOCK"    -> { setText("Tạm giữ");        getStyleClass().add("message-error"); }
                    case "BID_UNLOCK"  -> { setText("Hoàn tiền");      getStyleClass().add("message-success"); }
                    case "PAYMENT"     -> { setText("Thanh toán");     getStyleClass().add("message-error"); }
                    case "SALE_INCOME" -> { setText("Thu từ đấu giá"); getStyleClass().add("message-success"); }
                    default            -> { setText(item);              getStyleClass().add("message-info"); }
                }
            }
        });

        col_amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        col_amount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(VND.format(item) + " đ");
            }
        });

        col_description.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void loadWalletInfo() {
        setLoading(true);
        new Thread(() -> {
            try {
                WalletResult result = ClientSocket.getWalletInfo();
                Platform.runLater(() -> {
                    setLoading(false);
                    if (result.isSuccess()) {
                        updateBalanceDisplay(result);
                        updateTransactionTable(result.getTransactions());
                    } else {
                        showMessage(result.getMessage(), false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showMessage("Lỗi kết nối server: " + e.getMessage(), false);
                });
            }
        }).start();
    }

    @FXML
    private void handleDeposit() {
        String raw = txf_depositAmount.getText().trim();
        if (raw.isEmpty()) {
            showMessage("Vui lòng nhập số tiền cần nạp.", false);
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(raw.replace(",", "").replace(".", ""));
        } catch (NumberFormatException e) {
            showMessage("Số tiền không hợp lệ.", false);
            return;
        }
        if (amount <= 0) {
            showMessage("Số tiền phải lớn hơn 0.", false);
            return;
        }

        btn_deposit.setDisable(true);
        lbl_message.setText("");

        new Thread(() -> {
            try {
                WalletResult result = ClientSocket.sendDeposit(amount);
                Platform.runLater(() -> {
                    btn_deposit.setDisable(false);
                    if (result.isSuccess()) {
                        txf_depositAmount.clear();
                        updateBalanceDisplay(result);
                        updateTransactionTable(result.getTransactions());
                        showMessage("Nạp tiền thành công: +" + VND.format(amount) + " đ", true);
                    } else {
                        showMessage(result.getMessage(), false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btn_deposit.setDisable(false);
                    showMessage("Lỗi: " + e.getMessage(), false);
                });
            }
        }).start();
    }

    private void updateBalanceDisplay(WalletResult result) {
        lbl_totalBalance.setText(VND.format(result.getBalance()) + " đ");
        lbl_availableBalance.setText(VND.format(result.getAvailableBalance()) + " đ");
        if (lbl_lockedBalance != null) {
            lbl_lockedBalance.setText(VND.format(result.getLockedBalance()) + " đ");
        }
    }

    private void updateTransactionTable(List<WalletTransaction> transactions) {
        tbl_transactions.getItems().setAll(transactions);
    }

    private void showMessage(String msg, boolean success) {
        lbl_message.setText(msg);
        lbl_message.getStyleClass().removeAll("message-success", "message-error");
        lbl_message.getStyleClass().add(success ? "message-success" : "message-error");
    }

    private void setLoading(boolean loading) {
        btn_deposit.setDisable(loading);
        if (loading) {
            lbl_totalBalance.setText("...");
            lbl_availableBalance.setText("...");
            if (lbl_lockedBalance != null) lbl_lockedBalance.setText("...");
        }
    }
}
