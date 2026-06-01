package com.uet.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uet.client.utils.SessionManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainClient extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MainClient.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            try {
                ClientSocket.connect();
            } catch (Exception e) {
                logger.error("Không thể kết nối tới Server — hãy kiểm tra Server đã chạy chưa", e);
                return;
            }

            Parent root = FXMLLoader.load(getClass().getResource("/com/uet/views/SignIn.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setTitle("Phần mềm Đấu giá trực tuyến - Đăng nhập");
            primaryStage.setScene(scene);

            primaryStage.setOnCloseRequest(event -> {
                if (SessionManager.currentUser != null) {
                    logger.info("{} đang thoát ứng dụng...", SessionManager.currentUser.getName());
                    ClientSocket.sendDisconnect();
                }
                Platform.exit();
                System.exit(0);
            });

            primaryStage.show();
            logger.info("Ứng dụng khởi động thành công.");

        } catch (Exception e) {
            logger.error("Không thể tải giao diện FXML hoặc kết nối server", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
