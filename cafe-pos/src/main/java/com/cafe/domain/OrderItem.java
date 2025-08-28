package com.cafe.domain;

import com.cafe.domain.enums.OrderItemStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="order_item")
public class OrderItem {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false) @JoinColumn(name="order_id")
  private OrderHeader order;

  @ManyToOne(optional=false) @JoinColumn(name="product_id")
  private Product product;

  @Column(nullable=false) private Integer qty = 1;
  @Column(name="unit_price", nullable=false) private BigDecimal unitPrice;
  @Column(length=200) private String note;

  @Enumerated(EnumType.STRING) @Column(nullable=false)
  private OrderItemStatus status = OrderItemStatus.NEW;

  // getters/setters
  public Long getId() { return id; }
  public OrderHeader getOrder() { return order; }
  public void setOrder(OrderHeader order) { this.order = order; }
  public Product getProduct() { return product; }
  public void setProduct(Product product) { this.product = product; }
  public Integer getQty() { return qty; }
  public void setQty(Integer qty) { this.qty = qty; }
  public BigDecimal getUnitPrice() { return unitPrice; }
  public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
  public OrderItemStatus getStatus() { return status; }
  public void setStatus(OrderItemStatus status) { this.status = status; }
}
