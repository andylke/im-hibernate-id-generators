package im.hibernate.id.generators.standard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.id.IdentifierGenerationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Integration tests for {@link StandardSequence}.
 *
 * @author Andy Lian
 */
class StandardSequenceIntegrationTests {

  private static SessionFactory sessionFactory;

  @BeforeAll
  static void beforeAll() {
    StandardServiceRegistry serviceRegistry =
        new StandardServiceRegistryBuilder()
            .applySetting("hibernate.connection.driver_class", "org.h2.Driver")
            .applySetting(
                "hibernate.connection.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=2000")
            .applySetting("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
            .applySetting("hibernate.hbm2ddl.auto", "create")
            .build();

    sessionFactory =
        new MetadataSources(serviceRegistry)
            .addAnnotatedClasses(
                TestAscendingNonCyclingEntity.class,
                TestAscendingCyclingEntity.class,
                TestDescendingNonCyclingEntity.class,
                TestDescendingCyclingEntity.class)
            .buildMetadata()
            .buildSessionFactory();

    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();
      session
          .createNativeMutationQuery(
              "CREATE TABLE standard_sequence_integration_tests ("
                  + "name VARCHAR(100) PRIMARY KEY,"
                  + "current_value BIGINT NOT NULL,"
                  + "created_at TIMESTAMP NOT NULL,"
                  + "last_modified_at TIMESTAMP"
                  + ")")
          .executeUpdate();

      session.getTransaction().commit();
    }
  }

  @AfterAll
  static void afterAll() {
    if (sessionFactory != null) {
      sessionFactory.close();
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @Entity
  static class TestAscendingNonCyclingEntity {
    @Id
    @StandardSequence(
        sequenceTable = "standard_sequence_integration_tests",
        name = "test-ascending-non-cycling",
        initialValue = 10L,
        maxValue = 20L,
        incrementValue = 5,
        descending = false,
        cycle = false)
    private Long id;
  }

  @Test
  void persist_whenAscending_NonCycling() {
    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();

      TestAscendingNonCyclingEntity entity1 = new TestAscendingNonCyclingEntity();
      session.persist(entity1);
      assertThat(entity1.getId()).isEqualTo(10L);

      TestAscendingNonCyclingEntity entity2 = new TestAscendingNonCyclingEntity();
      session.persist(entity2);
      assertThat(entity2.getId()).isEqualTo(15L);

      TestAscendingNonCyclingEntity entity3 = new TestAscendingNonCyclingEntity();
      session.persist(entity3);
      assertThat(entity3.getId()).isEqualTo(20L);

      TestAscendingNonCyclingEntity entity4 = new TestAscendingNonCyclingEntity();
      assertThrows(IdentifierGenerationException.class, () -> session.persist(entity4));

      session.getTransaction().commit();
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @Entity
  static class TestAscendingCyclingEntity {
    @Id
    @StandardSequence(
        sequenceTable = "standard_sequence_integration_tests",
        name = "test-ascending-cycling",
        initialValue = 10L,
        maxValue = 20L,
        incrementValue = 5,
        descending = false,
        cycle = true)
    private Long id;
  }

  @Test
  void persist_whenAscending_Cycling() {
    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();

      TestAscendingCyclingEntity entity1 = new TestAscendingCyclingEntity();
      session.persist(entity1);
      assertThat(entity1.getId()).isEqualTo(10L);

      TestAscendingCyclingEntity entity2 = new TestAscendingCyclingEntity();
      session.persist(entity2);
      assertThat(entity2.getId()).isEqualTo(15L);

      TestAscendingCyclingEntity entity3 = new TestAscendingCyclingEntity();
      session.persist(entity3);
      assertThat(entity3.getId()).isEqualTo(20L);

      session.remove(entity1);

      TestAscendingCyclingEntity entity4 = new TestAscendingCyclingEntity();
      session.persist(entity4);
      assertThat(entity4.getId()).isEqualTo(10L);

      session.getTransaction().commit();
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @Entity
  static class TestDescendingNonCyclingEntity {
    @Id
    @StandardSequence(
        sequenceTable = "standard_sequence_integration_tests",
        name = "test-descending-non-cycling",
        initialValue = 10L,
        maxValue = 20L,
        incrementValue = 5,
        descending = true,
        cycle = false)
    private Long id;
  }

  @Test
  void persist_whenDescending_NonCycling() {
    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();

      TestDescendingNonCyclingEntity entity1 = new TestDescendingNonCyclingEntity();
      session.persist(entity1);
      assertThat(entity1.getId()).isEqualTo(20L);

      TestDescendingNonCyclingEntity entity2 = new TestDescendingNonCyclingEntity();
      session.persist(entity2);
      assertThat(entity2.getId()).isEqualTo(15L);

      TestDescendingNonCyclingEntity entity3 = new TestDescendingNonCyclingEntity();
      session.persist(entity3);
      assertThat(entity3.getId()).isEqualTo(10L);

      TestDescendingNonCyclingEntity entity4 = new TestDescendingNonCyclingEntity();
      assertThrows(IdentifierGenerationException.class, () -> session.persist(entity4));

      session.getTransaction().commit();
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @Entity
  static class TestDescendingCyclingEntity {
    @Id
    @StandardSequence(
        sequenceTable = "standard_sequence_integration_tests",
        name = "test-descending-cycling",
        initialValue = 10L,
        maxValue = 20L,
        incrementValue = 5,
        descending = true,
        cycle = true)
    private Long id;
  }

  @Test
  void persist_whenDescending_Cycling() {
    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();

      TestDescendingCyclingEntity entity1 = new TestDescendingCyclingEntity();
      session.persist(entity1);
      assertThat(entity1.getId()).isEqualTo(20L);

      TestDescendingCyclingEntity entity2 = new TestDescendingCyclingEntity();
      session.persist(entity2);
      assertThat(entity2.getId()).isEqualTo(15L);

      TestDescendingCyclingEntity entity3 = new TestDescendingCyclingEntity();
      session.persist(entity3);
      assertThat(entity3.getId()).isEqualTo(10L);

      session.remove(entity1);

      TestDescendingCyclingEntity entity4 = new TestDescendingCyclingEntity();
      session.persist(entity4);
      assertThat(entity4.getId()).isEqualTo(20L);

      session.getTransaction().commit();
    }
  }
}
