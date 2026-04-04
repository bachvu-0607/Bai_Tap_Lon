package com.uet.client.controllers;
//import com.uet.models.User;
//import com.uet.models.Seller;
//import com.uet.models.Bidder;
//import com.uet.models.Admin;
import com.uet.server.utils.SceneManager;
import com.uet.client.core.ClientSocket;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.IIOException;



import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController implements Initializable {

    @FXML
    private Button btn_Register;
    @FXML
    private TextField txt_Address;
    @FXML
    private TextField txt_ID;
    @FXML
    private TextField txt_Name;
    @FXML
    private PasswordField txt_Password;
    @FXML
    private TextField txt_PhoneNumber;
    @FXML
    private ChoiceBox <String> cb_Role;
    @FXML
    private String[] roles = {"Bidder", "Seller", "Admin"};

    @FXML
    private Label lbl_Error;
    
    @Override  //Đây là thanh ngăn xếp Roles
    public void initialize(URL location, ResourceBundle resources) {
        // Hàm này tự chạy khi giao diện được load lên
        cb_Role.getItems().addAll(roles);
        cb_Role.setValue("Role");
    }

    @FXML
    private void handleRegister(){
        String txt_name = this.txt_Name.getText();
        String txt_phone = this.txt_PhoneNumber.getText();
        String txt_ID = this.txt_ID.getText();
        String txt_password = this.txt_Password.getText();
        String txt_address = this.txt_Address.getText();
        String txt_role = this.cb_Role.getValue(); 


        //Theo database ko được để trống mấy cái dưới đây
        if(txt_password.isBlank() || txt_phone.isBlank() || txt_ID.isBlank() || txt_role.isBlank()){
            System.out.println("Client not fill in all the info");
            lbl_Error.setText("Please fill in all the information!");
            return;
        }

        try {
            String state = ClientSocket.sendRegister(txt_name, txt_phone, txt_ID, txt_password, txt_address, txt_role);
            if(state.equals("EXISTED_ID")){ 
                lbl_Error.setText("This ID has already been registered!");
                return; // Dừng lại luôn, không chạy code đăng ký bên dưới nữa
            }
            else if(state.equals("EXIST_PHONE")){
                lbl_Error.setText("This phone number has already been registered!");
                return; // Dừng lại luôn, không chạy code đăng ký bên dưới nữa
            }
            else if(state.equals("SERVER_ERROR")){
                throw new Exception("Lỗi server rồi!");
            }
            else{ // đăng ký thành công chuyển giao diện sang đăng nhập
                SceneManager.switchScene(btn_Register, "/com/uet/views/SignIn.fxml", "Sign In", 600, 400); 
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connect to server error");
            lbl_Error.setText("Connect to server error"); 
        }
    }
}
