package com.cafe.ui;

import com.cafe.domain.*;
import com.cafe.domain.enums.*;
import com.cafe.persistence.JPAUtil;
import jakarta.persistence.EntityManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class POSController {

  @FXML private ListView<CafeTable> tableList;
  @FXML private TableView<Product> productTable;
  @FXML private TableColumn<Product,String> pName;
  @FXML private TableColumn<Product,String> pPrice;

  @FXML private TableView<OrderItem> cartTable;
  @FXML private TableColumn<OrderItem,String> cProd;
  @FXML private TableColumn<OrderItem,String> cQty;
  @FXML private TableColumn<OrderItem,String> cTotal;

  @FXML private Label lblTotal;

  private OrderHeader currentOrder;
  private final List<OrderItem> cart = new ArrayList<>();

  @FXML
  private void initialize() {
    // ürün tablo kolonları
    pName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
    pPrice.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPrice().toString()));

    // sepet kolonları
    cProd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProduct().getName()));
    cQty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQty())));
    cTotal.setCellValueFactory(c -> new SimpleStringProperty(
        c.getValue().getUnitPrice().multiply(new BigDecimal(c.getValue().getQty())).toString()
    ));

    loadTables();
    loadProducts();
  }

  private void loadTables() {
    EntityManager em = JPAUtil.emf().createEntityManager();
    try {
      var list = em.createQuery("select t from CafeTable t order by t.number", CafeTable.class).getResultList();
      tableList.setItems(FXCollections.observableArrayList(list));
      tableList.setCellFactory(v -> new ListCell<>() {
        @Override protected void updateItem(CafeTable item, boolean empty) {
          super.updateItem(item, empty);
          setText(empty || item==null ? null : "Masa " + item.getNumber() + " (" + item.getStatus() + ")");
        }
      });
    } finally { em.close(); }
  }

  private void loadProducts() {
    EntityManager em = JPAUtil.emf().createEntityManager();
    try {
      var list = em.createQuery("select p from Product p where p.isActive=true order by p.name", Product.class)
                   .getResultList();
      productTable.setItems(FXCollections.observableArrayList(list));
    } finally { em.close(); }
  }

  @FXML
  private void onNewOrder() {
    CafeTable t = tableList.getSelectionModel().getSelectedItem();
    if (t == null) { alert("Lütfen bir masa seçin."); return; }

    EntityManager em = JPAUtil.emf().createEntityManager();
    em.getTransaction().begin();
    try {
      // masayı OCCUPIED yap
      t = em.merge(t);
      t.setStatus(TableStatus.OCCUPIED);

      // açık sipariş var mı?
      var open = em.createQuery(
          "select o from OrderHeader o where o.table = :t and o.status <> :closed and o.status <> :canceled",
          OrderHeader.class)
          .setParameter("t", t)
          .setParameter("closed", OrderStatus.CLOSED)
          .setParameter("canceled", OrderStatus.CANCELED)
          .getResultStream().findFirst().orElse(null);

      if (open == null) {
        currentOrder = new OrderHeader();
        currentOrder.setTable(t);
        currentOrder.setStatus(OrderStatus.OPEN);
        em.persist(currentOrder);
      } else {
        currentOrder = open;
      }
      em.getTransaction().commit();

      cart.clear();
      refreshCart();
    } catch (Exception ex) {
      em.getTransaction().rollback();
      ex.printStackTrace();
      alert("Sipariş açılamadı.");
    } finally { em.close(); }
  }

  @FXML
  private void onAddToCart() {
    if (currentOrder == null) { alert("Önce bir masa için 'Yeni Sipariş' oluşturun."); return; }
    Product p = productTable.getSelectionModel().getSelectedItem();
    if (p == null) { alert("Lütfen bir ürün seçin."); return; }

    // Sepete ekle (şimdilik memory’de, 'Gönder' deyince DB'ye yazacağız)
    OrderItem it = new OrderItem();
    it.setOrder(currentOrder);
    it.setProduct(p);
    it.setQty(1);
    it.setUnitPrice(p.getPrice());
    it.setStatus(OrderItemStatus.NEW);
    cart.add(it);
    refreshCart();
  }

  @FXML
  private void onSendToKitchen() {
    if (currentOrder == null) { alert("Açık sipariş yok."); return; }
    if (cart.isEmpty()) { alert("Sepet boş."); return; }

    EntityManager em = JPAUtil.emf().createEntityManager();
    em.getTransaction().begin();
    try {
      // fresh order ref
      OrderHeader o = em.find(OrderHeader.class, currentOrder.getId());
      for (OrderItem it : cart) {
        OrderItem entity = new OrderItem();
        entity.setOrder(o);
        entity.setProduct(em.getReference(Product.class, it.getProduct().getId()));
        entity.setQty(it.getQty());
        entity.setUnitPrice(it.getUnitPrice());
        entity.setNote(it.getNote());
        entity.setStatus(OrderItemStatus.NEW);
        em.persist(entity);
      }
      o.setStatus(OrderStatus.IN_KITCHEN);
      em.getTransaction().commit();

      cart.clear();
      refreshCart();
      loadTables();
      alert("Sipariş KDS'ye gönderildi.");
    } catch (Exception ex) {
      em.getTransaction().rollback();
      ex.printStackTrace();
      alert("Gönderilemedi.");
    } finally { em.close(); }
  }

  private void refreshCart() {
    cartTable.setItems(FXCollections.observableArrayList(cart));
    BigDecimal total = cart.stream()
        .map(i -> i.getUnitPrice().multiply(new BigDecimal(i.getQty())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    lblTotal.setText(total.toString());
  }

  private void alert(String msg) {
    new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
  }
}
