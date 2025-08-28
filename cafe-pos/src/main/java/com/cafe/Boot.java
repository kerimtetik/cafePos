package com.cafe;

import org.flywaydb.core.Flyway;

public final class Boot {
  private Boot() {}

  public static void migrate() {
    Flyway.configure()
        .dataSource("jdbc:h2:file:./data/cafe-db;AUTO_SERVER=TRUE", "sa", "")
        .locations("classpath:db/migration")
        .load()
        .migrate();
  }
}
