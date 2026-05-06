package com.uet.client.controllers;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;

public class MyProductController {
    @FXML
    private Hyperlink hpl_PostProduct;
    @FXML
    private VBox vbox_OrderList;

    @FXML
    private void handleOpenPostProduct(ActionEvent event) {
        try {
            Node node = FXMLLoader.load(getClass().getResource("/com/uet/views/PostProduct.fxml"));
            vbox_OrderList.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
