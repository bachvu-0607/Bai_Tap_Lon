package com.uet.client.controllers;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.uet.client.core.ClientSocket;
import com.uet.client.data.AddressDataLoader;
import com.uet.client.data.Commune;
import com.uet.client.data.Province;
import com.uet.client.utils.SceneManager;
import com.uet.domain.result.AuthenticationResult;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Bộ điều khiển (Controller) cho màn hình Đăng ký (Register).
 * Xử lý giao diện cho phép nhập liệu thông tin tài khoản mới, tải địa giới hành chính động
 * (tỉnh thành, xã phường) và kiểm tra tính hợp lệ dữ liệu (mật khẩu mạnh, số điện thoại, số CCCD).
 */
public class RegisterController implements Initializable {

    @FXML
    private Button btn_Register;
    @FXML
    private TextField txt_CitizenId;
    @FXML
    private TextField txt_Name;
    @FXML
    private PasswordField txt_Password;
    @FXML
    private TextField txt_PhoneNumber;
    @FXML
    private ComboBox<String> cb_Role;
    private static final String[] ROLES = {"Bidder", "Seller"};
    @FXML
    private Label lbl_Error;
    @FXML
    private ComboBox<Province> cb_Province;
    @FXML
    private ComboBox<Commune> cb_Commune;
    @FXML
    private TextField txt_AddressDetail;

    /**
     * Khởi tạo giao diện Đăng ký.
     * Cấu hình các ComboBox cho vai trò người dùng, tải dữ liệu tỉnh/thành từ tệp JSON
     * và lắng nghe sự thay đổi của tỉnh thành để cập nhật danh sách xã/phường tương ứng.
     * 
     * @param location URL vị trí file FXML.
     * @param resources Tài nguyên bản địa hóa.
     */
    @Override  //Đây là thanh ngăn xếp Roles
    public void initialize(URL location, ResourceBundle resources) {
        // Hàm này tự chạy khi giao diện được load lên
        cb_Role.getItems().addAll(ROLES);
        cb_Role.setPromptText("Select role");
        cb_Role.setVisibleRowCount(4);

        AddressDataLoader loader = new AddressDataLoader();

        List<Province> provinces = loader.getProvinces();
        List<Commune> communes = loader.getCommunes();
        cb_Province.getItems().addAll(provinces);
        cb_Province.setPromptText("Select province/city");
        cb_Province.setVisibleRowCount(6);
        cb_Commune.setDisable(true);
        cb_Commune.setPromptText("Select commune/ward");
        cb_Commune.setVisibleRowCount(6);

        cb_Province.getSelectionModel().selectedItemProperty().addListener((observable, oldProvince, selectedProvince) -> {
            cb_Commune.getItems().clear();
            cb_Commune.setValue(null);

            //Nếu chưa chọn tỉnh thì khóa xã/phường lại.
            if (selectedProvince == null) {
                cb_Commune.setDisable(true);
                return;
            }

            for (Commune commune : communes) {
                if (selectedProvince.getCode().equals(commune.getProvinceCode())) {
                    cb_Commune.getItems().add(commune);
                }
            }

            boolean hasCommuneData = !cb_Commune.getItems().isEmpty();
            cb_Commune.setDisable(!hasCommuneData);
            if (!hasCommuneData) {
                lbl_Error.setText("No commune/ward data for selected province.");
            } else {
                lbl_Error.setText("");
            }
        });
    }

    /**
     * Xử lý khi người dùng nhấn nút Đăng ký.
     * Kiểm tra toàn bộ dữ liệu nhập vào (Name, Password, CitizenId, Phone, Address, Role)
     * và gửi yêu cầu đăng ký tài khoản mới lên Server.
     */
    @FXML
    private void handleRegister(){
        String txt_name = this.txt_Name.getText().trim();
        String txt_phone = this.txt_PhoneNumber.getText().trim();
        String txt_citizenId = this.txt_CitizenId.getText().trim();
        String txt_password = this.txt_Password.getText();
        
        Province province = cb_Province.getValue();
        Commune commune = cb_Commune.getValue();
        String detail = txt_AddressDetail.getText();

        String txt_role = this.cb_Role.getValue(); 
        
        if (txt_name.isBlank()) {
            lbl_Error.setText("Please fill in your name!");
            return;
        }
        if(!validatePassword(txt_password)) return;
        if(!validateCitizenId(txt_citizenId)) return;
        if(!validatePhone(txt_phone)) return;
        String txt_address = validateAddress(province, commune, detail);
        if(txt_address.equals("ERROR")) return;

        //Theo database ko được để trống mấy cái dưới đây
        if(txt_role == null || txt_role.isBlank()){
            System.out.println("Client not fill in all the info");
            lbl_Error.setText("Please fill in all the information!");
            return;
        }


        try {
            AuthenticationResult result = ClientSocket.sendRegister(txt_name, txt_phone, txt_citizenId, txt_password, txt_address, txt_role);
            if(AuthenticationResult.EXISTED_CITIZEN_ID.equals(result.getErrorCode())){ 
                lbl_Error.setText("This citizen ID has already been registered!");
                return; // Dừng lại luôn, không chạy code đăng ký bên dưới nữa
            }
            else if(AuthenticationResult.EXIST_PHONE.equals(result.getErrorCode())){
                lbl_Error.setText("This phone number has already been registered!");
                return; // Dừng lại luôn, không chạy code đăng ký bên dưới nữa
            }
            else if(AuthenticationResult.SERVER_ERROR.equals(result.getErrorCode())){
                lbl_Error.setText("Server error. Check server console.");
                return;
            }
            else if(result.isSuccess()){ // đăng ký thành công chuyển giao diện sang đăng nhập
                SceneManager.switchScene(btn_Register, "/com/uet/views/SignIn.fxml", "Sign In", 600, 400); 
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connect to server error");
            lbl_Error.setText("Connect to server error: " + e.getMessage()); 
        }
    }

    /**
     * Kiểm tra tính hợp lệ của mật khẩu. Mật khẩu hợp lệ phải chứa ít nhất một chữ hoa,
     * một chữ thường và một chữ số.
     * 
     * @param password Mật khẩu cần kiểm tra.
     * @return {@code true} nếu hợp lệ; {@code false} nếu không hợp lệ.
     */
    private boolean validatePassword(String password){
        if (password == null || password.isBlank()) {
            lbl_Error.setText("Please fill the password");
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);

            if (Character.isUpperCase(c)) {
                hasUpper = true;
            }

            if (Character.isLowerCase(c)) {
                hasLower = true;
            }

            if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasUpper) {
            lbl_Error.setText("password must have one upper case character");
            return false;
        }

        if (!hasLower) {
            lbl_Error.setText("password must have lower case character");
            return false;
        }

        if (!hasDigit) {
            lbl_Error.setText("password must have number character");
            return false;
        }
        
        return true;
    } 
    
    /**
     * Kiểm tra tính hợp lệ của số điện thoại. Số điện thoại phải gồm đúng 10 chữ số.
     * 
     * @param phone Số điện thoại cần kiểm tra.
     * @return {@code true} nếu hợp lệ; {@code false} nếu không hợp lệ.
     */
    private boolean validatePhone(String phone){
        if(phone == null || phone.isBlank()){
            lbl_Error.setText("Please fill in your phone number!");
            return false;
        }
        if(!phone.matches("\\d{10}")){
            lbl_Error.setText("Phone number must contain exactly 10 digits");
            return false;
        }
        return true;
    }

    /**
     * Kiểm tra tính hợp lệ của số Căn cước công dân (Citizen ID).
     * Số CCCD hợp lệ phải chứa đúng 12 chữ số.
     * 
     * @param id Số CCCD cần kiểm tra.
     * @return {@code true} nếu hợp lệ; {@code false} nếu không hợp lệ.
     */
    private boolean validateCitizenId(String id){
        if(id == null || id.isBlank()){
            lbl_Error.setText("Please fill in your citizen ID!");
            return false;
        }
        if(!id.matches("\\d{12}")){
            lbl_Error.setText("Citizen ID must contain exactly 12 digits");
            return false;
        }
        return true;
    }

    /**
     * Hợp nhất và định dạng chuỗi địa chỉ đầy đủ từ thông tin chi tiết nhà riêng, xã phường và tỉnh thành.
     * 
     * @param province Đối tượng Tỉnh/Thành phố được chọn.
     * @param commune Đối tượng Xã/Phường được chọn.
     * @param detail Chi tiết số nhà, ngõ/đường.
     * @return Chuỗi địa chỉ hoàn chỉnh, hoặc "ERROR" nếu điền thiếu thông tin.
     */
    private String validateAddress(Province province, Commune commune, String detail){
        if(province == null || commune == null || detail == null || detail.isBlank()){
            System.out.println("Client not fill in their address");
            lbl_Error.setText("Please fill in your address!");
            return "ERROR";
        }
        StringBuilder address = new StringBuilder();

        address.append(detail.trim());
        address.append(", ");
        address.append(commune.getName());
        address.append(", ");
        address.append(province.getName());

        return address.toString();
    }
}
