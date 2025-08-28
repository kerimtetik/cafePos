package com.cafe.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity @Table(name="product")
@Data
public class Product {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false) private String name;
  @Column(nullable=false) private BigDecimal price;

  @ManyToOne(optional=false) @JoinColumn(name="category_id")
  private Category category;

  @Column(name="is_active", nullable=false)
  private Boolean isActive = true;
  
  @Override
  public String toString() {
    return name + " (" + price + " TL)";
  }
}
