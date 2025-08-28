package com.cafe.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class DashboardController {

  @FXML private StackPane content;
  @FXML private Label lblClock;
  @FXML private Label lblStatus;
  @FXML private Label lblUser;

  @FXML
  private void initialize() {
    // saat
    Timeline t = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
      java.time.LocalDateTime now = java.time.LocalDateTime.now();
      lblClock.setText(now.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
    }));
    t.setCycleCount(Timeline.INDEFINITE);
    t.play();

    // açılışta dashboard göster
    openDashboard();
  }

  // ------- router helpers -------
  private void loadCenter(String fxmlPath) {
    try {
      Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
      content.getChildren().setAll(view);
      lblStatus.setText("Yüklendi: " + fxmlPath.substring(fxmlPath.lastIndexOf('/')+1));
    } catch (Exception ex) {
      ex.printStackTrace();
      lblStatus.setText("Hata: " + fxmlPath);
    }
  }

  // ------- nav actions -------
  @FXML private void openDashboard()  { loadCenter("/view/DashboardHome.fxml"); }
  @FXML private void openTables()     { loadCenter("/view/TablePlanView.fxml"); }
  @FXML private void openOrders()     { loadCenter("/view/OrdersView.fxml"); }
  @FXML private void openProducts()   { loadCenter("/view/ProductsView.fxml"); }
  @FXML private void openReports()    { loadCenter("/view/ReportsView.fxml"); }
  @FXML private void openCustomers()  { loadCenter("/view/CustomersView.fxml"); }
  @FXML private void openUsers()      { loadCenter("/view/UsersView.fxml"); }
  @FXML private void openSettings()   { loadCenter("/view/SettingsView.fxml"); }

  @FXML
  private void logout() {
    // Şimdilik sahte: sadece durum çubuğuna yaz.
    lblStatus.setText("Çıkış (dummy). Giriş ekranına yönlendirme eklenebilir.");
  }
}
