package com.uet.client.controllers;

import com.uet.client.utils.SceneManager;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

/**
 * Bộ điều khiển (Controller) cho màn hình chính giới thiệu hệ thống (Main View/Landing Page).
 * Cung cấp liên kết dẫn tới Đăng ký, Đăng nhập và các hiệu ứng cuộn trang động.
 */
public class MainViewController {
    @FXML
    private Hyperlink hpl_Register;
    @FXML
    private Hyperlink hpl_SignIn;
    @FXML
    private ScrollPane mainScrollPane;

    /**
     * Mở màn hình đăng ký tài khoản.
     */
    @FXML
    private void handleOpenRegisterLink() {
        SceneManager.switchScene(hpl_Register, "/com/uet/views/Register.fxml", "Register", 600, 400);
    }

    /**
     * Mở màn hình đăng nhập tài khoản.
     */
    @FXML
    private void handleOpenSignInLink() {
        SceneManager.switchScene(hpl_SignIn, "/com/uet/views/SignIn.fxml", "Sign In", 600, 400);
    }

    /**
     * Chuyển hướng người dùng tới xem danh sách phiên đấu giá (yêu cầu đăng nhập trước).
     */
    @FXML
    private void handleOpenAuctionList() {
        SceneManager.switchScene(hpl_SignIn != null ? hpl_SignIn : hpl_Register, "/com/uet/views/SignIn.fxml", "Sign In", 600, 400);
    }

    /**
     * Cuộn mượt màn hình ScrollPane hiện tại xuống phần thông tin liên hệ (Contact) ở cuối trang.
     */
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
