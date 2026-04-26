package com.uet.client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;

import com.uet.server.utils.SceneManager;

public class PostProductController implements Initializable {
    @FXML
    private Button btnPostProduct;
    @FXML
    private TextField txtProductName;
    @FXML
    private TextArea txtProductDescription;
    @FXML
    private TextField txtStartingPrice;
    @FXML
    private TextField txtBidIncrement;
    @FXML
    private TextField startHour;
    @FXML
    private TextField startMinute;
    @FXML
    private Label lbBackSite;
    @FXML
    private TextField endHour;
    @FXML
    private TextField endMinute;
    @FXML 
    private ChoiceBox<String> categoryBox;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryBox.getItems().addAll("Electronics", "Art","Vehicles");
        categoryBox.setValue("Electronics");
    }
    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private void handlePostProduct() {
        try {
            // 1. Lấy dữ liệu cơ bản dạng String trước
            String productName = txtProductName.getText();
            String productDescription = txtProductDescription.getText();
            String priceStr = txtStartingPrice.getText();
            String bidStr = txtBidIncrement.getText();
            String startHourStr = startHour.getText();
            String startMinuteStr = startMinute.getText();
            String endHourStr = endHour.getText();
            String endMinuteStr = endMinute.getText();
            
            LocalDate startDateValue = startDate.getValue();
            LocalDate endDateValue = endDate.getValue();
            String category = categoryBox.getValue();

            // 2. Validate kiểm tra rỗng TRƯỚC KHI ép kiểu
            if (productName.isBlank() || productDescription.isBlank() || priceStr.isBlank() || bidStr.isBlank() || 
                startHourStr.isBlank() || startMinuteStr.isBlank() || endHourStr.isBlank() || endMinuteStr.isBlank() ||
                startDateValue == null || endDateValue == null || category == null) {
                showError("Vui lòng điền đầy đủ thông tin!");
                return;
            }

            // 3. Ép kiểu dữ liệu (Nếu đến đây thì chắc chắn các ô không rỗng)
            double startingPrice = Double.parseDouble(priceStr);
            double bidIncrement = Double.parseDouble(bidStr);
            int startHourValue = Integer.parseInt(startHourStr);
            int startMinuteValue = Integer.parseInt(startMinuteStr);
            int endHourValue = Integer.parseInt(endHourStr);
            int endMinuteValue = Integer.parseInt(endMinuteStr);

            // 4. Tạo Object Thời gian
            LocalDateTime finalStartDateTime = LocalDateTime.of(startDateValue, LocalTime.of(startHourValue, startMinuteValue));
            LocalDateTime finalEndDateTime = LocalDateTime.of(endDateValue, LocalTime.of(endHourValue, endMinuteValue));

            // 5. Kiểm tra Business Logic (Số âm, Thời gian hợp lệ...)
            String validationError = validateBusinessLogic(startingPrice, bidIncrement, startHourValue, startMinuteValue, 
                                                           endHourValue, endMinuteValue, finalStartDateTime, finalEndDateTime);
            if (validationError != null) {
                showError(validationError); // Hiển thị lỗi nếu có
                return;                     // Dừng tiến trình
            }

            // 6. GỬI DỮ LIỆU LÊN SERVER (Gắn OOP vào đây: Tạo đối tượng Product rồi gọi Service)
            System.out.println("Thời gian chốt phiên đấu giá sẽ là: " + finalEndDateTime);
            // ProductService.postProduct(new Product(...));

            // 7. Hoàn tất
            showAlert("Thành công", "Sản phẩm của bạn đã được đăng thành công!");
            SellerHomeController.getInstance().loadView("Home");

        } catch (NumberFormatException e) {
            showError("Giá tiền, Giờ và Phút phải là số hợp lệ!");
        } catch (DateTimeException e) { // Bắt lỗi tạo thời gian sai (VD: 25h 61p)
            showError("Định dạng giờ phút không tồn tại!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi hệ thống", "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    // --- CÁC HÀM HỖ TRỢ ĐƯỢC TÁCH RA ---
    /**
     * Hàm này chỉ chịu trách nhiệm kiểm tra logic nghiệp vụ.
     */
    private String validateBusinessLogic(double price, double bid, int sHour, int sMin, int eHour, int eMin, 
                                         LocalDateTime start, LocalDateTime end) {
        if (price < 0 || bid < 0) return "Giá khởi điểm và bước giá phải là số dương!";
        
        if (sHour < 0 || sHour > 23 || sMin < 0 || sMin > 59 || eHour < 0 || eHour > 23 || eMin < 0 || eMin > 59) {
            return "Giờ phải từ 0-23 và phút phải từ 0-59!";
        }
        if (start.isBefore(LocalDateTime.now())) return "Thời gian bắt đầu không được ở trong quá khứ!";
        if (end.isBefore(LocalDateTime.now())) return "Thời gian kết thúc không được ở trong quá khứ!";
        if (start.isAfter(end) || start.isEqual(end)) return "Thời gian bắt đầu phải trước thời gian kết thúc!";
        
        return null; // Không có lỗi
    }
    // Viết gọn lại hàm báo lỗi 
    private void showError(String message) {
        System.out.println("Lỗi nhập liệu: " + message);
        showAlert("Lỗi nhập liệu", message);
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (title.contains("Lỗi")) {
             alert.setAlertType(Alert.AlertType.ERROR); // Đổi icon sang báo lỗi đỏ nếu là lỗi
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // @FXML
    // private void handleBack() {
    //     System.out.println("Quay về trang chủ...");
    //     SellerHomeController.getInstance().loadView("Home");
    // }
}
