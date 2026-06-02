package com.uet.client.core;
import com.uet.client.utils.SessionManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Lớp khởi chạy chính (Main Client Entry point) cho ứng dụng giao diện người dùng JavaFX.
 * Thực hiện kết nối Socket tới Server, nạp màn hình Đăng nhập FXML ban đầu và quản lý việc tắt ứng dụng sạch sẽ.
 */
public class MainClient extends Application {

    /**
     * Điểm bắt đầu chu kỳ sống của ứng dụng JavaFX.
     * Thiết lập kết nối socket TCP ban đầu, tải giao diện Đăng nhập và bắt sự kiện đóng cửa sổ để giải phóng tài nguyên.
     * 
     * @param primaryStage Cửa sổ chính được cấp bởi JavaFX runtime.
     */
    @Override
    public void start(Stage primaryStage) {
        try {

            try{
                ClientSocket.connect();
            }catch(Exception e){
                System.out.println("Không thể kết nối tới Server! Ông kiểm tra lại xem Server đã chạy chưa nhé.");
                e.printStackTrace();
                return;
            }
            // CHÚ Ý: Đường dẫn trỏ tới file FXML của ông. 
            // Nếu file fxml để trong thư mục resources, nhớ có dấu "/" ở đầu
            Parent root = FXMLLoader.load(getClass().getResource("/com/uet/views/SignIn.fxml")); 
            // Thiết lập cửa sổ hiển thị
            Scene scene = new Scene(root);
            primaryStage.setTitle("Phần mềm Đấu giá trực tuyến - Đăng nhập");
            primaryStage.setScene(scene);
            
            // Bắt sự kiện khi người dùng đóng cửa sổ
            primaryStage.setOnCloseRequest(event -> {
                if (SessionManager.currentUser != null) {
                    System.out.println(SessionManager.currentUser.getName() + " is exiting the application...");
                    
                    // hàm gửi tín hiệu Logout/Disconnect lên Server.
                    ClientSocket.sendDisconnect();
                }
                
                //Lệnh tắt hoàn toàn các luồng ngầm của giao diện JavaFX
                Platform.exit();
                // 3. Ép tắt hoàn toàn chương trình (đề phòng socket hoặc thread nào đó vẫn treo)
                System.exit(0);
            });

            // Show giao diện lên
            primaryStage.show();

        } catch (Exception e) {
            System.out.println("Can load FXML file or connect to server. Please check your connection and try again.");
            e.printStackTrace();
        }
    }

    /**
     * Phương thức main đánh thức luồng khởi chạy của JavaFX.
     * 
     * @param args Các tham số dòng lệnh.
     */
    public static void main(String[] args) {
        // Hàm này sẽ đánh thức JavaFX và gọi hàm start() ở trên
        launch(args);
    }
}