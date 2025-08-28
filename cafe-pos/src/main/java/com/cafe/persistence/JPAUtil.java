package com.cafe.persistence;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JPAUtil {
  private static final EntityManagerFactory EMF =
      Persistence.createEntityManagerFactory("cafePU");

  private JPAUtil() {}
  public static EntityManagerFactory emf() { return EMF; }
  public static void close() { EMF.close(); }
}
