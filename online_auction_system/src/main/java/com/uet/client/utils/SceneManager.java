package com.uet.client.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;


/**
 * Bộ quản lý chuyển cảnh (Scene Manager) cho giao diện người dùng JavaFX.
 * Hỗ trợ chuyển đổi nhanh chóng giữa các màn hình bằng cách nạp tệp FXML và cập nhật Stage hiện tại.
 */
public class SceneManager {

    /**
     * Thực hiện chuyển đổi màn hình (Scene) hiện tại sang màn hình mới.
     * 
     * @param currentNode Node hiện tại đang hiển thị trên giao diện (dùng để xác định Stage).
     * @param fxmlPath Đường dẫn đến tệp FXML của giao diện mới (ví dụ: "/com/uet/views/Home.fxml").
     * @param title Tiêu đề hiển thị cho cửa sổ mới.
     * @param width Chiều rộng của cửa sổ mới.
     * @param height Chiều cao của cửa sổ mới.
     */
    public static void switchScene(Node currentNode, String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) currentNode.getScene().getWindow();
            Scene scene = new Scene(root, width, height);
            
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Không load được file FXML: " + fxmlPath);
        }
    }
}
