package com.uet.client.controllers;

import java.io.IOException;

//import com.uet.models.User;

import com.uet.client.core.ClientSocket;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import com.uet.server.repositories.UserRepository;
import com.uet.server.utils.SceneManager;
//import com.uet.utils.SceneManager;
//import com.uet.utils.UserSession;

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
            lbl_Error.setText("Please fill in all the information!");
            return;
        }
        
        try{
            String role  = ClientSocket.sendSignIn(txt_username, txt_password);
            if(role == null){
                System.out.println("Wrong username or password");
                lbl_Error.setText("Wrong username or password"); // Hiện chữ đỏ lên màn hình
            }else if(role.equals("ALREADY_LOGGED_IN")){
                System.out.println("This account has already signed up");
                lbl_Error.setText("This account has already signed up"); // Hiện chữ đỏ lên màn hình
            }else{
                System.out.println("Sign in successfully! Role: " + role);
                lbl_Error.setText(""); 

                // Chuyển giao diện sang giao diện phù hợp với từng đối tượng
                if(role.equals("Bidder")){
                    //SceneManager.switchScene(btn_SignIn, "/com/uet/views/BidderHome.fxml", "Bidder View", 1000, 600);
                } 
                else if(role.equals("Seller")){
                    //SceneManager.switchScene(btn_SignIn, "/com/uet/views/SellerHome.fxml", "Seller View", 1000, 600);
                } 
                else if(role.equals("Admin")){
                    //SceneManager.switchScene(btn_SignIn, "/com/uet/views/AdminHome.fxml", "Admin View", 1000, 600);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Connect to server error");
            lbl_Error.setText("Connect to server error"); 
        }
        
    }

    @FXML
    private void handleOpenRegisterLink(){
        SceneManager.switchScene(hpl_Register, "/com/uet/views/Register.fxml", "Register", 600, 400);
    }
}