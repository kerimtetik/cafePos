package com.cafe.repo;

import com.cafe.domain.Product;
import com.cafe.persistence.JPAUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

public class ProductRepository {
  public List<Product> findAllActive() {
    EntityManager em = JPAUtil.emf().createEntityManager();
    try {
      return em.createQuery(
        "select p from Product p where p.isActive = true order by p.name",
        Product.class).getResultList();
    } finally {
      em.close();
    }
  }
}
