package com.uet.client.controllers;

import com.uet.client.core.ClientSocket;
import com.uet.client.utils.SessionManager;
import com.uet.domain.result.AuthenticationResult;
import com.uet.domain.entity.user.Admin;
import com.uet.domain.entity.user.Bidder;
import com.uet.domain.entity.user.Seller;
import com.uet.domain.entity.user.User;
import com.uet.client.utils.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Bộ điều khiển (Controller) cho màn hình Đăng nhập (SignIn).
 * Quản lý các sự kiện nhập liệu tài khoản, gửi yêu cầu đăng nhập lên Server qua Socket và điều hướng giao diện phù hợp với vai trò của người dùng (Admin, Bidder, Seller).
 */
public class SignInController {

    @FXML
    private Button btn_SignIn;

    @FXML
    private Label lbl_Error;

    @FXML
    private Hyperlink hpl_MoveToForgotPassWord;

    @FXML
    private Hyperlink hpl_Register;

    @FXML
    private PasswordField txt_Password;

    @FXML
    private TextField txt_Username;
    
    /**
     * Xử lý sự kiện khi người dùng bấm nút Đăng nhập.
     * Kiểm tra tính hợp lệ của dữ liệu nhập, gửi yêu cầu xác thực tới Server
     * và chuyển hướng giao diện tương ứng theo vai trò.
     */
    @FXML
    private void handleSignIn(){
        String txt_password = this.txt_Password.getText();
        String txt_username = this.txt_Username.getText();

        if(txt_password.isBlank() || txt_username.isBlank()){
            System.out.println("Client not fill in all the info");
            lbl_Error.setText("Please fill in all the information!");
            return;
        }
        
        try{
            AuthenticationResult response  = ClientSocket.sendSignIn(txt_username, txt_password);
            if(!response.isSuccess()){
                if(AuthenticationResult.ALREADY_LOGGED_IN.equals(response.getErrorCode())){
                    System.out.println("This account has already signed in");
                    lbl_Error.setText("This account has already signed in"); // Hiện chữ đỏ lên màn hình
                    return;
                }

                System.out.println("Wrong username or password");
                lbl_Error.setText("Wrong username or password"); // Hiện chữ đỏ lên màn hình
                return;
            }

            User loggedInUser = response.getUser();
            if (loggedInUser != null) {

                SessionManager.currentUser = loggedInUser;
                System.out.println("Sign in successfully! Hello: " + loggedInUser.getName());
                lbl_Error.setText(""); 

                // Chuyển giao diện sang giao diện phù hợp với từng đối tượng
                if(loggedInUser instanceof Bidder){
                    SceneManager.switchScene(btn_SignIn, "/com/uet/views/BidderHome.fxml", "Bidder View", 1000, 600);
                } 
                else if(loggedInUser instanceof Seller){
                    SceneManager.switchScene(btn_SignIn, "/com/uet/views/SellerHome.fxml", "Seller View", 1000, 600);
                } 
                else if(loggedInUser instanceof Admin){
                    SceneManager.switchScene(btn_SignIn, "/com/uet/views/AdminHome.fxml", "Admin View", 1000, 600);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Connect to server error");
            lbl_Error.setText("Connect to server error"); 
        }
        
    }

    /**
     * Chuyển hướng người dùng sang màn hình Đăng ký tài khoản mới khi nhấp vào liên kết.
     */
    @FXML
    private void handleOpenRegisterLink(){
        SceneManager.switchScene(hpl_Register, "/com/uet/views/Register.fxml", "Register", 600, 400);
    }
}
