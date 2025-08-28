package com.cafe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {
	
  @Override
  public void start(Stage stage) throws Exception {
	Boot.migrate(); // DB şemasını ayağa kaldır
	
	// var yerine Parent kullan
    Parent root = FXMLLoader.load(getClass().getResource("/view/DashboardView.fxml"));
    stage.setScene(new Scene(root, 1280, 780));
    stage.setTitle("Cafe Otomasyon — Dashboard");
    
    stage.show();
    
  }
  public static void main(String[] args) { launch(args); }
}
