package com.uet.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AdminHomeController {
     @FXML
    private Label adminHome;

    @FXML
    private Label auctionActionLabel;

    @FXML
    private TableView<?> auctionsTable;

    @FXML
    private TableColumn<?, ?> colActive;

    @FXML
    private TableColumn<?, ?> colAuctionBids;

    @FXML
    private TableColumn<?, ?> colAuctionEnd;

    @FXML
    private TableColumn<?, ?> colAuctionItem;

    @FXML
    private TableColumn<?, ?> colAuctionPrice;

    @FXML
    private TableColumn<?, ?> colAuctionSeller;

    @FXML
    private TableColumn<?, ?> colAuctionStatus;

    @FXML
    private TableColumn<?, ?> colEmail;

    @FXML
    private TableColumn<?, ?> colFullName;

    @FXML
    private TableColumn<?, ?> colRole;

    @FXML
    private TableColumn<?, ?> colUsername;

    @FXML
    private Label statusBarAdmin;

    @FXML
    private Label userActionLabel;

    @FXML
    private TableView<?> usersTable;

    @FXML
    void handleCancelAuction(ActionEvent event) {

    }

    @FXML
    void handleDeactivateUser(ActionEvent event) {

    }

    @FXML
    void loadAuctions(ActionEvent event) {

    }

    @FXML
    void loadUsers(ActionEvent event) {

    }

    
}
