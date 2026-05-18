package com.uet.client.controllers;

import java.io.IOException;

import com.uet.client.core.ClientSocket;
import com.uet.client.utils.SceneManager;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class SellerHomeController {
    @FXML
    private StackPane ContentArea;
    @FXML
    private Hyperlink hpl_SignOut;
    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    private void initialize() {
        loadView("Home");
    }

    private void loadView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/uet/views/" + fxmlFileName + ".fxml"));
            Node node = loader.load();
            ContentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackHome(ActionEvent event) {
        loadView("Home");
    }

    @FXML
    private void handleOpenAuctionList(ActionEvent event) {
        loadView("AuctionList");
    }

    @FXML
    private void handleOpenMyProduct(ActionEvent event) {
        loadView("MyProduct");
    }

    @FXML
    private void handleOpenWallet(ActionEvent event) {
    }

    @FXML
    private void scrollToContact() {
        if (mainScrollPane == null) {
            return;
        }
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(mainScrollPane.vvalueProperty(), 1.0);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(400), keyValue);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    @FXML
    private void handleOpenMainView() {
        ClientSocket.sendDisconnect();
        SceneManager.switchScene(hpl_SignOut, "/com/uet/views/SignIn.fxml", "Sign In", 600, 400);
    }
}
