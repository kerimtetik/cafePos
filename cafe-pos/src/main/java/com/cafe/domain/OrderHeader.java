package com.cafe.domain;

import com.cafe.domain.enums.OrderStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="order_header")
public class OrderHeader {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false) @JoinColumn(name="table_id")
  private CafeTable table;

  @Enumerated(EnumType.STRING) @Column(nullable=false)
  private OrderStatus status = OrderStatus.OPEN;

  @Column(name="created_at", nullable=false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name="closed_at") private LocalDateTime closedAt;

  @Column(nullable=false) private String channel = "DINEIN";

  // getters/setters
  public Long getId() { return id; }
  public CafeTable getTable() { return table; }
  public void setTable(CafeTable table) { this.table = table; }
  public OrderStatus getStatus() { return status; }
  public void setStatus(OrderStatus status) { this.status = status; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public LocalDateTime getClosedAt() { return closedAt; }
  public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
  public String getChannel() { return channel; }
  public void setChannel(String channel) { this.channel = channel; }
}
