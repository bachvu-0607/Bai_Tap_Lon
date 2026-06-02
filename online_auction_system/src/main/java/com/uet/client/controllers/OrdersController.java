package com.uet.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Bộ điều khiển (Controller) quản lý danh sách đơn hàng (Orders).
 * Hiện tại đang ở dạng khung chờ hoàn thiện (Placeholder).
 */
public class OrdersController {
    @FXML
    private VBox vbox_OrderList;

    /**
     * Khởi tạo giao diện danh sách đơn hàng.
     */
    @FXML
    private void initialize() {
        if (vbox_OrderList != null) {
            vbox_OrderList.getChildren().setAll(new Label("Order history is not implemented yet."));
        }
    }
}
