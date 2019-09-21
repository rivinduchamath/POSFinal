package controller;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class login {
    public Button btnloginbutton;
    public JFXTextField txtloginName;
    public JFXPasswordField txtloginpw;
    public AnchorPane LoginForm;
    public AnchorPane root1;

    public void loginNameOnAction(ActionEvent actionEvent) {
    }

    public void loginPasswordOnAction(ActionEvent actionEvent) {
    }

    public void LoginButtonOnAction(ActionEvent actionEvent) throws IOException {
        URL resource = this.getClass().getResource("/view/MainForm.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) (this.root1.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }
}
