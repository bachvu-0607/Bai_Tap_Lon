package com.uet.client.controllers;

import java.io.IOException;

import com.uet.client.core.ClientSocket;
import com.uet.client.utils.SceneManager;
import com.uet.domain.event.ServerEventType;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SellerHomeController {
    @FXML
    private StackPane ContentArea;
    @FXML
    private Hyperlink hpl_SignOut;
    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private VBox pageContent;
    @FXML
    private VBox contactFooter;
    @FXML
    private Label lblActiveUsers;

    @FXML
    private void initialize() {
         ClientSocket.setGlobalEventListener(event -> { 
            if (event.getType() == ServerEventType.ONLINE_USERS_UPDATED) {
                int onlineUsers = (int) event.getData();
                Platform.runLater(() -> updateActiveUserDisplay(onlineUsers));
            }
        });
        bindPageContentToViewport();
        loadActiveUsers();
        loadView("Home");
    }

    private void bindPageContentToViewport() {
        mainScrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) ->
                updatePageContentMinHeight(newBounds.getHeight()));
        contactFooter.heightProperty().addListener((obs, oldHeight, newHeight) ->
                updatePageContentMinHeight(mainScrollPane.getViewportBounds().getHeight()));
    }

    private void updatePageContentMinHeight(double viewportHeight) {
        double footerHeight = Math.max(contactFooter.getHeight(), 160);
        pageContent.setMinHeight(viewportHeight + footerHeight + 24);
    }

    // Sau này nhận số active user từ server thì gọi hàm này để cập nhật badge trên nav bar.
    private void updateActiveUserDisplay(int activeUserCount) {
        lblActiveUsers.setText("● " + activeUserCount + " online");
    }

    private void loadActiveUsers() {
        try {
            updateActiveUserDisplay(ClientSocket.getOnlineUsers());
        } catch (Exception e) {
            updateActiveUserDisplay(0);
        }
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
