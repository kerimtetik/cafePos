package com.cafe.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;         // <-- EKLE
import javafx.scene.Scene;          // <-- (istersen import et, fully-qualified da olur)
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
  @FXML private TextField username;
  @FXML private PasswordField pin;

  @FXML
  private void onLogin(ActionEvent e) {
    try {
      Stage stage = (Stage) ((javafx.scene.Node)e.getSource()).getScene().getWindow();

      // var yerine Parent kullan
      Parent root = FXMLLoader.load(getClass().getResource("/view/TablePlanView.fxml"));
      stage.setScene(new Scene(root, 1200, 700));
      stage.setTitle("Table Plan");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
