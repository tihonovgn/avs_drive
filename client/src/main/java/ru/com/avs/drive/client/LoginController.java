package ru.com.avs.drive.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginController {

    @FXML
    private Text actiontarget;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    public void handleLoginFieldAction(ActionEvent actionEvent) {
        passwordField.requestFocus();
    }

    private Map<String, String> authData = new HashMap<>();

    @FXML
    protected void handleSubmitButtonAction(ActionEvent event) {
        authData.put("login", loginField.getText());
        authData.put("password", passwordField.getText());
        if(loginField.getText().equals("admin") && passwordField.getText().equals("admin")){
            showMainWindow();
            Stage stage = (Stage) loginField.getScene().getWindow();
            stage.close();
        }else{
            actiontarget.setText("Login or password are wrong.");
        }

    }

    private void showMainWindow() {
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            root = loader.load();
            MainController controller = loader.getController();
            controller.setAuthData(authData);
            controller.refreshServerFileList();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Scene scene = new Scene(root, 800, 600);
        Stage stage = new Stage();
        stage.setTitle("AVS Drive");
        stage.setScene(scene);
        stage.show();
    }
}