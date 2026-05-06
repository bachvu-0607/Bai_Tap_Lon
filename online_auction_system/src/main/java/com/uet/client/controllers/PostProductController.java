package com.uet.client.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.uet.client.core.ClientSocket;
import com.uet.domain.result.ProductPostResult;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PostProductController {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private Button btn_Post;
    @FXML
    private ChoiceBox<String> cb_Role;
    @FXML
    private Label lbl_Error;
    @FXML
    private TextField txt_ImageLink;
    @FXML
    private TextField txt_Name;
    @FXML
    private TextField txt_Description;
    @FXML
    private TextField txt_OpeningPrice;
    @FXML
    private TextField txt_MinIncrement;
    @FXML
    private TextField txt_StartTime;
    @FXML
    private TextField txt_EndTime;

    @FXML
    private void initialize() {
        if (cb_Role != null) {
            cb_Role.getItems().setAll("Electronics", "Art", "Vehicle");
            cb_Role.setValue("Electronics");
        }
    }

    @FXML
    private void handlePost() {
        String productType = this.cb_Role.getValue();
        String productName = this.txt_Name.getText().trim();
        String description = this.txt_Description.getText().trim();
        String openingPriceText = this.txt_OpeningPrice.getText().trim();
        String minIncrementText = this.txt_MinIncrement.getText().trim();
        String startTimeText = this.txt_StartTime.getText().trim();
        String endTimeText = this.txt_EndTime.getText().trim();
        String imageLink = this.txt_ImageLink.getText().trim();

        if(productName.isBlank() || description.isBlank() || openingPriceText.isBlank()
                || minIncrementText.isBlank() || startTimeText.isBlank() || endTimeText.isBlank()){
            System.out.println("Seller not give enough info about their product");
            lbl_Error.setText("Please fill all information about your product");
            return;
        }

        double openingPrice;
        try {
            openingPrice = Double.parseDouble(openingPriceText);
        } catch (NumberFormatException e) {
            lbl_Error.setText("Opening price must be a number.");
            return;
        }

        if (openingPrice <= 0) {
            lbl_Error.setText("Opening price must be greater than 0.");
            return;
        }

        double minIncrement;
        try {
            minIncrement = Double.parseDouble(minIncrementText);
        } catch (NumberFormatException e) {
            lbl_Error.setText("Minimum increment must be a number.");
            return;
        }

        if (minIncrement <= 0) {
            lbl_Error.setText("Minimum increment must be greater than 0.");
            return;
        }

        LocalDateTime startTime;
        LocalDateTime endTime;
        try {
            startTime = LocalDateTime.parse(startTimeText, DATE_TIME_FORMAT);
            endTime = LocalDateTime.parse(endTimeText, DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            lbl_Error.setText("Time format must be yyyy-MM-dd HH:mm.");
            return;
        }

        if (!endTime.isAfter(startTime)) {
            lbl_Error.setText("End time must be after start time.");
            return;
        }

        try {
            ProductPostResult result = ClientSocket.postProduct(
                    productType,
                    productName,
                    description,
                    openingPrice,
                    minIncrement,
                    startTime,
                    endTime,
                    imageLink);
            lbl_Error.setText(result.getMessage());
            if (result.isSuccess()) {
                txt_Name.clear();
                txt_Description.clear();
                txt_OpeningPrice.clear();
                txt_MinIncrement.clear();
                txt_StartTime.clear();
                txt_EndTime.clear();
                txt_ImageLink.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            lbl_Error.setText("Cannot post product: " + e.getMessage());
        }
    }
}
