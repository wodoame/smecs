package com.smecs.controller;

import com.smecs.model.User;
import com.smecs.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainLayoutController {

    @FXML
    private TabPane mainTabPane;

    @FXML
    private Tab adminTab;

    @FXML
    private Button authButton;

    @FXML
    private Label userLabel;

    @FXML
    public void initialize() {
        // Initial UI update
        updateUI();
    }

    private void updateUI() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        boolean isLoggedIn = SessionManager.getInstance().isLoggedIn();

        if (isLoggedIn) {
            authButton.setText("Logout");
            userLabel.setText("User: " + currentUser.getUsername());

            if (SessionManager.getInstance().isAdmin()) {
                if (!mainTabPane.getTabs().contains(adminTab)) {
                    mainTabPane.getTabs().add(adminTab);
                }
            } else {
                mainTabPane.getTabs().remove(adminTab);
            }
        } else {
            authButton.setText("Login");
            userLabel.setText("");
            mainTabPane.getTabs().remove(adminTab);
        }
    }

    @FXML
    private void handleAuthAction(ActionEvent event) {
        if (SessionManager.getInstance().isLoggedIn()) {
            SessionManager.getInstance().logout();
            updateUI();
        } else {
            openLoginDialog();
        }
    }

    private void openLoginDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login_view.fxml"));
            Parent root = loader.load();

            LoginController loginController = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Login");
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            try {
                java.net.URL cssUrl = getClass().getResource("/css/styles.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
            } catch (Exception e) {
                // Ignore if css not found
                System.out.println("CSS not found: " + e.getMessage());
            }
            dialogStage.setScene(scene);

            loginController.setDialogMode(dialogStage, user -> {
                updateUI();
            });

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
