package com.uet.client.controllers;

import com.uet.client.core.ClientSocket;
import com.uet.client.utils.MessageHelper;
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
    
    @FXML
    private void handleSignIn(){
        String txt_password = this.txt_Password.getText();
        String txt_username = this.txt_Username.getText();

        if(txt_password.isBlank() || txt_username.isBlank()){
            System.out.println("Client not fill in all the info");
            MessageHelper.error(lbl_Error, "Please fill in all the information!");
            return;
        }
        
        try{
            AuthenticationResult response  = ClientSocket.sendSignIn(txt_username, txt_password);
            if(!response.isSuccess()){
                if(AuthenticationResult.ALREADY_LOGGED_IN.equals(response.getErrorCode())){
                    System.out.println("This account has already signed in");
                    MessageHelper.error(lbl_Error, "This account has already signed in");
                    return;
                }

                System.out.println("Wrong username or password");
                MessageHelper.error(lbl_Error, "Wrong username or password");
                return;
            }

            User loggedInUser = response.getUser();
            if (loggedInUser != null) {

                SessionManager.currentUser = loggedInUser;
                System.out.println("Sign in successfully! Hello: " + loggedInUser.getName());
                MessageHelper.clear(lbl_Error);

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
            MessageHelper.error(lbl_Error, "Connect to server error");
        }
        
    }

    @FXML
    private void handleOpenRegisterLink(){
        SceneManager.switchScene(hpl_Register, "/com/uet/views/Register.fxml", "Register", 600, 400);
    }
}
