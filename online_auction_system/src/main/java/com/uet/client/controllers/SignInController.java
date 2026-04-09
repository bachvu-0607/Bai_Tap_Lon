package com.uet.client.controllers;

import com.uet.client.core.ClientSocket;
import com.uet.client.utils.SessionManager;
import com.uet.models.Admin;
import com.uet.models.Bidder;
import com.uet.models.Seller;
import com.uet.models.User;
import com.uet.server.utils.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
            Object response  = ClientSocket.sendSignIn(txt_username, txt_password);
            if(response == null){
                System.out.println("Wrong username or password");
                lbl_Error.setText("Wrong username or password"); // Hiện chữ đỏ lên màn hình
            
            }else if( response instanceof String && ((String) response).equals("ALREADY_LOGGED_IN")){
                System.out.println("This account has already signed in");
                lbl_Error.setText("This account has already signed in"); // Hiện chữ đỏ lên màn hình
            
            }else if (response instanceof User) {

                User loggedInUser = (User) response;

                SessionManager.currentUser = loggedInUser;
                System.out.println("Sign in successfully! Hello: " + loggedInUser.getName());
                lbl_Error.setText(""); 

                // Chuyển giao diện sang giao diện phù hợp với từng đối tượng
                if(response instanceof Bidder){
                    SceneManager.switchScene(btn_SignIn, "/com/uet/views/BidderHome.fxml", "Bidder View", 1000, 600);
                } 
                else if(response instanceof Seller){
                    //SceneManager.switchScene(btn_SignIn, "/com/uet/views/SellerHome.fxml", "Seller View", 1000, 600);
                } 
                else if(response instanceof Admin){
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