package com.cafe.domain;

import com.cafe.domain.enums.TableStatus;
import jakarta.persistence.*;

@Entity @Table(name="cafe_table")
public class CafeTable {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false, unique=true) private Integer number;
  @Column(nullable=false) private Integer capacity = 2;

  @Enumerated(EnumType.STRING) @Column(nullable=false)
  private TableStatus status = TableStatus.EMPTY;

  // getters/setters
  public Long getId() { return id; }
  public Integer getNumber() { return number; }
  public void setNumber(Integer number) { this.number = number; }
  public Integer getCapacity() { return capacity; }
  public void setCapacity(Integer capacity) { this.capacity = capacity; }
  public TableStatus getStatus() { return status; }
  public void setStatus(TableStatus status) { this.status = status; }
}
