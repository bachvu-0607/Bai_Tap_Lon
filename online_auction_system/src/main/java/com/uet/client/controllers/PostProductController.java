package com.uet.client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;

import java.net.URL;
import java.util.ResourceBundle;

public class PostProductController implements Initializable {
    @FXML
    private Button btnPostProduct;
    @FXML
    private TextField txtProductName;
    @FXML
    private TextArea txtProductDescription;
    @FXML
    private TextField txtStartingPrice;
    @FXML
    private TextField txtBidIncrement;
    @FXML
    private TextField startHour;
    @FXML
    private TextField startMinute;
    @FXML
    private TextField endHour;
    @FXML
    private TextField endMinute;
    @FXML 
    private ChoiceBox<String> categoryBox;
    private String[] categories = {"Electronics", "Art","Vehicles"};
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryBox.getItems().addAll(categories);
        categoryBox.setValue(categories[0]);
    }
    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private void handlePostProduct() {
        String productName = txtProductName.getText();
        String productDescription = txtProductDescription.getText();
        String startingPrice = txtStartingPrice.getText();
        String bidIncrement = txtBidIncrement.getText();
        String category = categoryBox.getValue();
        String startDateStr = startDate.getValue().toString();
        String endDateStr = endDate.getValue().toString();
        String startTimeStr = startHour.getText() + ":" + startMinute.getText();
        String endTimeStr = endHour.getText() + ":" + endMinute.getText();
    }
    
}
