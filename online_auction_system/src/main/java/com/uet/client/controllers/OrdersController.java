package com.uet.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class OrdersController {
    @FXML
    private VBox vbox_OrderList;

    @FXML
    private void initialize() {
        if (vbox_OrderList != null) {
            vbox_OrderList.getChildren().setAll(new Label("Order history is not implemented yet."));
        }
    }
}
