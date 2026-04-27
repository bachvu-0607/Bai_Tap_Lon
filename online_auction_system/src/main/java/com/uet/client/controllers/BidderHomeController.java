package com.uet.client.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.uet.server.utils.SceneManager;
import com.uet.client.utils.SessionManager;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class BidderHomeController implements Initializable {

    @FXML
    private StackPane ContentArea;

    @FXML
    private Hyperlink hpl_AuctionList;

    @FXML
    private Hyperlink hpl_Contacts;

    @FXML
    private Hyperlink hpl_Home;

    @FXML
    private Hyperlink hpl_Orders;

    @FXML
    private Hyperlink hpl_Wallet;
    
    @FXML
    private Hyperlink hpl_SignOut;

    @FXML
    private ImageView img_Logo;

    @FXML
    private ImageView img_Slogan;

    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    private Label txtRole;

    @FXML
    private Label txt_Hotline;

    @FXML
    private Label txt_LocateBranchOffice;

    @FXML
    private Label txt_LocateHeadOffice;

    @FXML
    private Label txt_LogoCompany;

    @FXML
    private Label txt_MainEmail;

    @FXML
    private Label txt_NameCompany;

    @FXML
    private Label txt_OtherMail;

    @FXML
    private Label txt_News;

    @FXML
    private Label txt_Announcements;

    @FXML
    private TextField txtf_FindProduct;
    // Tạo một biến tĩnh (static) để lưu lại chính nó
    private static BidderHomeController instance;

    public void initialize(URL location, ResourceBundle resources) {
        // Gán instance bằng chính object này khi khởi tạo
        instance = this; 
        
        loadView("Home");
    }
    @FXML
    public void initialize() {
        // Ktra xem có ai đang đăng nhập k
        if (SessionManager.currentUser != null) {
            txtRole.setText("Xin chào, " + SessionManager.currentUser.getName() + "!");
        }
    }

    private void loadView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/uet/views/" + fxmlFileName + ".fxml"));
            Node node = loader.load();
            ContentArea.getChildren().clear();

            // 3. Nhét cái trang mới vào giữa
            ContentArea.getChildren().add(node);

        } catch (IOException e) {
            System.out.println("Error loading: " + fxmlFileName);
            e.printStackTrace(); // In lỗi ra để dễ bắt bệnh nếu sai đường dẫn
        }
    }
    @FXML
    void handleBackHome(ActionEvent event){
        loadView("Home");
    }

    @FXML
    void handleOpenAuctionList(ActionEvent event) {
        loadView("AuctionList");
    }

    @FXML
    void handleOpenOrders(ActionEvent event) {
        loadView("Orders");
    }

    @FXML
    void handleOpenWallet(ActionEvent event) {

    }

    @FXML
    private void scrollToContact() {
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(mainScrollPane.vvalueProperty(), 1.0);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(400), keyValue);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }
    
    @FXML
    private void handleOpenMainView(){
        //SceneManager.switchScene(hpl_SignOut,"/com/uet/views/MainView.fxml", "Main View", 600, 400);
    }

}
