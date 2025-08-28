package com.cafe.ui;

import com.cafe.domain.*;
import com.cafe.domain.enums.OrderStatus;
import com.cafe.domain.enums.TableStatus;
import com.cafe.persistence.JPAUtil;
import jakarta.persistence.EntityManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TablePlanController {

  @FXML private Label lblTime;
  @FXML private Label lblSelectedTable;
  @FXML private ListView<String> orderList;
  @FXML private Label lblTotal;
  @FXML private TabPane tabPane;
  @FXML private AnchorPane frontArea;
  @FXML private AnchorPane backArea;
  @FXML private AnchorPane terraceArea;

  // durum
  private CafeTable selectedTable;
  private OrderHeader currentOrder;
  private List<OrderItem> currentItems;
  private LocalDateTime orderOpenedAt;

  @FXML
  private void initialize() {
    ensureSeedData();  // hiç masa yoksa örnek ekle
    loadTables();
    startTimer();
  }

  // --- MASALARI YÜKLE ---
  private void loadTables() {
    frontArea.getChildren().clear(); // basit: hepsini ön tarafa koyuyoruz
    EntityManager em = JPAUtil.emf().createEntityManager();
    try {
      var tables = em.createQuery("select t from CafeTable t order by t.number", CafeTable.class).getResultList();
      double x = 20, y = 20; // basit yerleşim
      for (CafeTable t : tables) {
        Button btn = new Button("Masa " + t.getNumber());
        btn.setPrefSize(80, 50);
        btn.setLayoutX(x);
        btn.setLayoutY(y);

        // renk
        switch (t.getStatus()) {
          case EMPTY -> btn.setStyle("-fx-background-color: lightgreen;");
          case OCCUPIED -> btn.setStyle("-fx-background-color: lightcoral;");
          case BILL -> btn.setStyle("-fx-background-color: gold;");
        }

        btn.setOnAction(e -> openTable(t.getId())); // ID ile aç

        frontArea.getChildren().add(btn);
        x += 100;
        if (x > 380) { x = 20; y += 80; }
      }
    } finally { em.close(); }
  }

  // --- MASA AÇ (seç veya oluştur) ---
  private void openTable(Long tableId) {
    EntityManager em = JPAUtil.emf().createEntityManager();
    em.getTransaction().begin();
    try {
      selectedTable = em.find(CafeTable.class, tableId);

      // masayı OCCUPIED yap (boşsa)
      if (selectedTable.getStatus() == TableStatus.EMPTY) {
        selectedTable.setStatus(TableStatus.OCCUPIED);
      }

      // açık sipariş var mı?
      currentOrder = em.createQuery("""
          select o from OrderHeader o
          where o.table = :t and o.status <> :closed and o.status <> :canceled
          order by o.createdAt desc
          """, OrderHeader.class)
          .setParameter("t", selectedTable)
          .setParameter("closed", OrderStatus.CLOSED)
          .setParameter("canceled", OrderStatus.CANCELED)
          .setMaxResults(1)
          .getResultStream().findFirst().orElse(null);

      if (currentOrder == null) {
        currentOrder = new OrderHeader();
        currentOrder.setTable(selectedTable);
        currentOrder.setStatus(OrderStatus.OPEN);
        em.persist(currentOrder);
      }

      em.getTransaction().commit();
    } catch (Exception ex) {
      em.getTransaction().rollback();
      ex.printStackTrace();
      alert("Masa açılamadı.");
      return;
    } finally { em.close(); }

    lblSelectedTable.setText("Seçili masa: " + selectedTable.getNumber());
    orderOpenedAt = currentOrder.getCreatedAt();
    reloadOrderItems();
    loadTables(); // renk güncellensin
  }
//ÜST BAR: Yeni masa ekle (en basit hali)
@FXML
private void onAddTable() {
 TextInputDialog d = new TextInputDialog();
 d.setTitle("Masa Ekle");
 d.setHeaderText("Yeni masa numarası:");
 var r = d.showAndWait();
 if (r.isEmpty()) return;

 int number;
 try { number = Integer.parseInt(r.get().trim()); }
 catch (Exception e) { alert("Geçersiz numara."); return; }

 var em = JPAUtil.emf().createEntityManager();
 em.getTransaction().begin();
 try {
   var exists = em.createQuery("select count(t) from CafeTable t where t.number=:n", Long.class)
                  .setParameter("n", number).getSingleResult();
   if (exists > 0) { alert("Bu numarada masa var."); em.getTransaction().rollback(); return; }

   CafeTable t = new CafeTable();
   t.setNumber(number);
   t.setCapacity(2);
   t.setStatus(TableStatus.EMPTY);
   em.persist(t);
   em.getTransaction().commit();
   loadTables();   // ekranda yenile
 } catch (Exception ex) {
   em.getTransaction().rollback(); ex.printStackTrace(); alert("Masa eklenemedi.");
 } finally { em.close(); }
}

//ÜST BAR: Ürün kataloğuna hızlı ekleme (basit dialog)
@FXML
private void onAddProduct() {
 Dialog<Product> dlg = new Dialog<>();
 dlg.setTitle("Ürün Ekle");
 dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

 TextField tfName = new TextField();
 TextField tfPrice = new TextField();
 tfName.setPromptText("Ad");
 tfPrice.setPromptText("Fiyat (ör. 65.00)");
 GridPane gp = new GridPane(); gp.setHgap(8); gp.setVgap(8);
 gp.addRow(0, new Label("Ad:"), tfName);
 gp.addRow(1, new Label("Fiyat:"), tfPrice);
 dlg.getDialogPane().setContent(gp);

 dlg.setResultConverter(bt -> bt == ButtonType.OK ? new Product() : null);
 var res = dlg.showAndWait();
 if (res.isEmpty()) return;

 String name = tfName.getText().trim();
 String priceStr = tfPrice.getText().trim();
 if (name.isEmpty() || priceStr.isEmpty()) { alert("Ad ve fiyat zorunlu."); return; }

 java.math.BigDecimal price;
 try { price = new java.math.BigDecimal(priceStr.replace(',', '.')); }
 catch (Exception e) { alert("Fiyat geçersiz."); return; }

 var em = JPAUtil.emf().createEntityManager();
 em.getTransaction().begin();
 try {
   // "Genel" kategorisi yoksa oluştur
   Category cat = em.createQuery("select c from Category c where c.name=:n", Category.class)
                    .setParameter("n", "Genel").getResultStream().findFirst().orElse(null);
   if (cat == null) {
     cat = new Category(); cat.setName("Genel"); cat.setSort(0); em.persist(cat);
   }

   Product p = new Product();
   p.setName(name); p.setPrice(price); p.setCategory(cat);
   em.persist(p);
   em.getTransaction().commit();

   // herhangi bir ürün listesi açık ise yenilemek istersen burada çağırabilirsin
   alert("Ürün eklendi: " + p.getName());
 } catch (Exception ex) {
   em.getTransaction().rollback(); ex.printStackTrace(); alert("Ürün eklenemedi.");
 } finally { em.close(); }
}


  // --- SİPARİŞ KALEMLERİNİ YENİLE ---
  private void reloadOrderItems() {
    if (currentOrder == null) { orderList.getItems().clear(); lblTotal.setText("0.00 TL"); return; }

    EntityManager em = JPAUtil.emf().createEntityManager();
    try {
      currentItems = em.createQuery("""
          select i from OrderItem i
          where i.order = :o and i.status <> com.cafe.domain.enums.OrderItemStatus.CANCELED
          order by i.id
          """, OrderItem.class)
          .setParameter("o", em.getReference(OrderHeader.class, currentOrder.getId()))
          .getResultList();

      orderList.getItems().setAll(currentItems.stream()
          .map(i -> "%s x%d  (%.2f)".formatted(
              i.getProduct().getName(), i.getQty(),
              i.getUnitPrice().multiply(new BigDecimal(i.getQty()))
          ))
          .collect(Collectors.toList()));

      BigDecimal total = currentItems.stream()
          .map(i -> i.getUnitPrice().multiply(new BigDecimal(i.getQty())))
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      lblTotal.setText("%.2f TL".formatted(total));
    } finally { em.close(); }
  }

  // --- ÜRÜN EKLE ---
  @FXML
  private void onAddProductToTable() {
    if (currentOrder == null) { alert("Önce bir masa seçin."); return; }

    // ürün seçtir
    EntityManager em = JPAUtil.emf().createEntityManager();
    List<Product> products = em.createQuery(
        "select p from Product p where p.isActive = true order by p.name", Product.class
    ).getResultList();
    em.close();

    if (products.isEmpty()) {
      alert("Aktif ürün yok.");
      return;
    }

    // Varsayılan seçim olarak ilk ürünü veriyoruz
    ChoiceDialog<Product> dlg = new ChoiceDialog<>(products.get(0), products);
    dlg.setTitle("Ürün Seç");
    dlg.setHeaderText("Eklenecek ürünü seçin");
    dlg.setContentText("Ürün:");

    // setConverter KULLANMA — gerek yok, toString() yeterli
    var opt = dlg.showAndWait();
    if (opt.isEmpty()) return;

    Product p = opt.get();


    // DB'ye ekle (aynı ürün varsa sadece qty++ yapalım)
    EntityManager em2 = JPAUtil.emf().createEntityManager();
    em2.getTransaction().begin();
    try {
      OrderHeader o = em2.find(OrderHeader.class, currentOrder.getId());

      OrderItem existing = em2.createQuery("""
          select i from OrderItem i where i.order = :o and i.product.id = :pid
          """, OrderItem.class)
          .setParameter("o", o)
          .setParameter("pid", p.getId())
          .getResultStream().findFirst().orElse(null);

      if (existing != null) {
        existing.setQty(existing.getQty()+1);
      } else {
        OrderItem it = new OrderItem();
        it.setOrder(o);
        it.setProduct(em2.getReference(Product.class, p.getId()));
        it.setQty(1);
        it.setUnitPrice(p.getPrice());
        em2.persist(it);
      }

      // masayı OCCUPIED yap
      CafeTable t = o.getTable();
      if (t.getStatus() == TableStatus.EMPTY) t.setStatus(TableStatus.OCCUPIED);

      em2.getTransaction().commit();
    } catch (Exception ex) {
      em2.getTransaction().rollback();
      ex.printStackTrace();
      alert("Ürün eklenemedi.");
    } finally { em2.close(); }

    reloadOrderItems();
    loadTables();
  }

  // --- ADET ARTIR/AZALT/SİL ---
  @FXML private void onIncreaseQty() { adjustQty(+1); }
  @FXML private void onDecreaseQty() { adjustQty(-1); }
  @FXML private void onDeleteItem()  { deleteSelected(); }

  private void adjustQty(int delta) {
    int idx = orderList.getSelectionModel().getSelectedIndex();
    if (idx < 0 || currentItems == null || idx >= currentItems.size()) { alert("Listeden bir öğe seçin."); return; }
    OrderItem item = currentItems.get(idx);

    EntityManager em = JPAUtil.emf().createEntityManager();
    em.getTransaction().begin();
    try {
      OrderItem e = em.find(OrderItem.class, item.getId());
      int newQty = e.getQty() + delta;
      if (newQty <= 0) {
        em.remove(e);
      } else {
        e.setQty(newQty);
      }
      em.getTransaction().commit();
    } catch (Exception ex) {
      em.getTransaction().rollback();
      ex.printStackTrace();
      alert("Güncellenemedi.");
    } finally { em.close(); }

    reloadOrderItems();
  }

  private void deleteSelected() {
    int idx = orderList.getSelectionModel().getSelectedIndex();
    if (idx < 0 || currentItems == null || idx >= currentItems.size()) { alert("Listeden bir öğe seçin."); return; }
    OrderItem item = currentItems.get(idx);

    EntityManager em = JPAUtil.emf().createEntityManager();
    em.getTransaction().begin();
    try {
      OrderItem e = em.find(OrderItem.class, item.getId());
      em.remove(e);
      em.getTransaction().commit();
    } catch (Exception ex) {
      em.getTransaction().rollback();
      ex.printStackTrace();
      alert("Silinemedi.");
    } finally { em.close(); }

    reloadOrderItems();
  }

  // --- YAZDIR (basit) ---
  @FXML
  private void onPrint() {
    if (currentOrder == null) { alert("Önce bir masa seçin."); return; }
    String text = "Masa " + selectedTable.getNumber() + "\n" +
        String.join("\n", orderList.getItems()) + "\n\nToplam: " + lblTotal.getText();

    TextArea ta = new TextArea(text);
    ta.setWrapText(true);

    var job = javafx.print.PrinterJob.createPrinterJob();
    if (job != null && job.showPrintDialog(orderList.getScene().getWindow())) {
      boolean ok = job.printPage(ta);
      if (ok) job.endJob();
    }
  }

  // --- yardımcılar ---
  private void alert(String msg) {
    new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
  }

  private void ensureSeedData() {
    EntityManager em = JPAUtil.emf().createEntityManager();
    em.getTransaction().begin();
    try {
      long count = em.createQuery("select count(t) from CafeTable t", Long.class).getSingleResult();
      if (count == 0) {
        for (int i=1;i<=6;i++) {
          CafeTable t = new CafeTable();
          t.setNumber(i); t.setCapacity(i<=2 ? 2 : 4);
          t.setStatus(TableStatus.EMPTY);
          em.persist(t);
        }
      }
      em.getTransaction().commit();
    } catch (Exception ex) {
      em.getTransaction().rollback();
      ex.printStackTrace();
    } finally { em.close(); }
  }

  private void startTimer() {
    // çok basit süre göstergesi: seçili siparişin geçen süresi
    javafx.animation.Timeline tl = new javafx.animation.Timeline(
        new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
          if (currentOrder == null) { lblTime.setText("00:00"); return; }
          Duration d = Duration.between(currentOrder.getCreatedAt(), LocalDateTime.now());
          long min = d.toMinutes(); long sec = d.minusMinutes(min).getSeconds();
          lblTime.setText(String.format("%02d:%02d", min, sec));
        })
    );
    tl.setCycleCount(javafx.animation.Animation.INDEFINITE);
    tl.play();
  }
}
