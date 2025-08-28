package com.cafe.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity @Table(name="category")
@Data
public class Category {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false) private String name;
  @Column(nullable=false) private Integer sort = 0;
}
