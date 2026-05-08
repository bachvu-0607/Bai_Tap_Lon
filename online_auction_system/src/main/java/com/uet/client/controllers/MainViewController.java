package com.uet.client.controllers;

import com.uet.client.utils.SceneManager;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

public class MainViewController {
    @FXML
    private Hyperlink hpl_Register;
    @FXML
    private Hyperlink hpl_SignIn;
    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    private void handleOpenRegisterLink() {
        SceneManager.switchScene(hpl_Register, "/com/uet/views/Register.fxml", "Register", 600, 400);
    }

    @FXML
    private void handleOpenSignInLink() {
        SceneManager.switchScene(hpl_SignIn, "/com/uet/views/SignIn.fxml", "Sign In", 600, 400);
    }

    @FXML
    private void handleOpenAuctionList() {
        SceneManager.switchScene(hpl_SignIn != null ? hpl_SignIn : hpl_Register, "/com/uet/views/SignIn.fxml", "Sign In", 600, 400);
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
}
