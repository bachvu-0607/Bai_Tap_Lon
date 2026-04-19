package com.uet.client.controllers;

import java.io.IOException;

import com.uet.server.utils.SceneManager;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SellerHomeController {

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
    @FXML
    private Hyperlink hpl_PostProduct; // Khai báo thêm biến này nếu bạn muốn can thiệp UI sau này

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
    void handleOpenPostProduct(ActionEvent event) throws IOException {
        // javafx.scene.Parent root = FXMLLoader.load(getClass().getResource("/com/uet/views/PostProduct.fxml"));
        // javafx.stage.Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        // javafx.scene.Scene scene = new Scene(root);
        // stage.setScene(scene);
        // stage.show();
         SceneManager.switchScene(hpl_PostProduct, "/com/uet/views/PostProduct.fxml", "Post Product", 600, 500);
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
