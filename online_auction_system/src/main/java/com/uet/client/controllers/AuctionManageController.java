package com.uet.client.controllers;

import com.uet.models.Seller;
import com.uet.server.utils.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AuctionManageController {
    @FXML
    private Label lbl_BackSite;
    @FXML
    private TableView productTable;
    @FXML
    private Button btn_AddProduct;
    @FXML
    private Button btn_EditProduct;
    @FXML
    private Button btn_RemoveProduct;
    @FXML
    private TableColumn col_Id;
    @FXML
    private TableColumn col_Name;
    @FXML
    private TableColumn col_OpeningPrice;
    @FXML
    private TableColumn col_CurrentPrice;
    @FXML
    private TableColumn col_OpeningTime;
    @FXML
    private TableColumn col_EndingTime;
    @FXML
    private TableColumn col_AuctionStatus;
    @FXML
    private void addProductButton(){
        SellerHomeController.getInstance().loadView("PostProduct");
    }
    @FXML
    private void editProductButton(){
        SellerHomeController.getInstance().loadView("EditProduct");
    }
    @FXML
    private void removeProductButton(){
        SellerHomeController.getInstance().loadView("RemoveProduct");
    }
}
