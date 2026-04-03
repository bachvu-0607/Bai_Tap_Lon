package com.uet.client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainClient extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {

            ClientSocket.connect();
            // CHÚ Ý: Đường dẫn trỏ tới file FXML của ông. 
            // Nếu file fxml để trong thư mục resources, nhớ có dấu "/" ở đầu
            Parent root = FXMLLoader.load(getClass().getResource("/com/uet/views/SignIn.fxml")); 
            // Thiết lập cửa sổ hiển thị
            Scene scene = new Scene(root);
            primaryStage.setTitle("Phần mềm Đấu giá trực tuyến - Đăng nhập");
            primaryStage.setScene(scene);
            
            // Show giao diện lên
            primaryStage.show();

        } catch (Exception e) {
            System.out.println("Lỗi không load được file FXML! Ông kiểm tra lại đường dẫn nhé.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Hàm này sẽ đánh thức JavaFX và gọi hàm start() ở trên
        launch(args);
    }
}