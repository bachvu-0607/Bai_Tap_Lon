package com.uet.server.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;


public class SceneManager {

    //Chuyển giao diện
    public static void switchScene(Node currentNode, String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) currentNode.getScene().getWindow();
            Scene scene = new Scene(root, width, height);
            
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Không load được file FXML: " + fxmlPath);
        }
    }
}