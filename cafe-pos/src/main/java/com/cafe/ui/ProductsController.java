package com.cafe.ui;

import com.cafe.domain.Product;
import com.cafe.repo.ProductRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ProductsController {
  @FXML private TableView<Product> table;
  @FXML private TableColumn<Product, String> colName;
  @FXML private TableColumn<Product, String> colPrice;
  @FXML private TableColumn<Product, String> colCategory;

  private final ProductRepository repo = new ProductRepository();

  @FXML
  private void initialize() {
    colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
    colPrice.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPrice().toString()));
    colCategory.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
        c.getValue().getCategory() != null ? c.getValue().getCategory().getName() : "-"));
    load();
  }

  @FXML
  private void onRefresh() { load(); }

  private void load() {
    table.setItems(FXCollections.observableArrayList(repo.findAllActive()));
  }
}
