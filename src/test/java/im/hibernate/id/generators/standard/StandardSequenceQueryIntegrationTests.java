package im.hibernate.id.generators.standard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import jakarta.persistence.PessimisticLockException;
import jakarta.persistence.Tuple;

/**
 * Database integration tests for {@link StandardSequenceQuery}.
 *
 * @author Andy Lian
 */
class StandardSequenceQueryIntegrationTests {

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

    sessionFactory = new MetadataSources(serviceRegistry).buildMetadata().buildSessionFactory();

    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();
      session
          .createNativeMutationQuery(
              "CREATE TABLE standard_sequence_query_integration_tests ("
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

  @Test
  void load_returnState_whenLoadConcurrentlyWithinTimeout()
      throws InterruptedException, ExecutionException {
    StandardSequenceQuery query =
        new StandardSequenceQuery(
            "standard_sequence_query_integration_tests",
            "name",
            "current_value",
            "created_at",
            "last_modified_at",
            "load-within-timeout");

    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();
      session
          .createNativeMutationQuery(
              "INSERT INTO standard_sequence_query_integration_tests VALUES('load-within-timeout', 123, '2025-09-20T18:00:00.000', null)")
          .executeUpdate();
      session.getTransaction().commit();
    }

    try (Session session1 = sessionFactory.openSession();
        Session session2 = sessionFactory.openSession()) {

      ExecutorService executor = Executors.newFixedThreadPool(2);
      CountDownLatch latch = new CountDownLatch(1);

      Future<StandardSequenceState> future1 =
          executor.submit(
              () -> {
                latch.await();

                session1.beginTransaction();
                StandardSequenceState state =
                    query.load((SharedSessionContractImplementor) session1);

                Thread.sleep(1000);

                session1.getTransaction().commit();
                return state;
              });

      Future<StandardSequenceState> future2 =
          executor.submit(
              () -> {
                latch.await();
                Thread.sleep(100);

                session2.beginTransaction();
                StandardSequenceState state =
                    query.load((SharedSessionContractImplementor) session2);
                session2.getTransaction().commit();
                return state;
              });

      latch.countDown();

      assertThat(future1.get().getCurrentValue()).isEqualTo(123L);
      assertThat(future2.get().getCurrentValue()).isEqualTo(123L);

      executor.shutdown();
    }
  }

  @Test
  void load_throws_whenLoadConcurrentlyExceedTimeout()
      throws InterruptedException, ExecutionException {
    StandardSequenceQuery query =
        new StandardSequenceQuery(
            "standard_sequence_query_integration_tests",
            "name",
            "current_value",
            "created_at",
            "last_modified_at",
            "load-exceed-timeout");

    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();
      session
          .createNativeMutationQuery(
              "INSERT INTO standard_sequence_query_integration_tests VALUES('load-exceed-timeout', 123, '2025-09-20T18:00:00.000', null)")
          .executeUpdate();
      session.getTransaction().commit();
    }

    try (Session session1 = sessionFactory.openSession();
        Session session2 = sessionFactory.openSession()) {

      ExecutorService executor = Executors.newFixedThreadPool(2);
      CountDownLatch latch = new CountDownLatch(1);

      Future<StandardSequenceState> future1 =
          executor.submit(
              () -> {
                latch.await();

                session1.beginTransaction();
                StandardSequenceState state =
                    query.load((SharedSessionContractImplementor) session1);
                Thread.sleep(3000);
                session1.getTransaction().commit();
                return state;
              });

      Future<StandardSequenceState> future2 =
          executor.submit(
              () -> {
                latch.await();
                Thread.sleep(100);

                session2.beginTransaction();
                try {
                  return query.load((SharedSessionContractImplementor) session2);
                } finally {
                  session2.getTransaction().rollback();
                }
              });

      latch.countDown();

      assertThat(future1.get().getCurrentValue()).isEqualTo(123L);
      assertThatThrownBy(future2::get).hasCauseInstanceOf(PessimisticLockException.class);

      executor.shutdown();
    }
  }

  @Test
  void insert_successful_whenRowNotExists() {
    StandardSequenceQuery query =
        new StandardSequenceQuery(
            "standard_sequence_query_integration_tests",
            "name",
            "current_value",
            "created_at",
            "last_modified_at",
            "insert-row-not-exists");

    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();
      query.insert((SharedSessionContractImplementor) session, new StandardSequenceState(123L));
      session.getTransaction().commit();
    }

    try (Session session = sessionFactory.openSession()) {
      Tuple result =
          session
              .createNativeQuery(
                  "SELECT current_value FROM standard_sequence_query_integration_tests WHERE name='insert-row-not-exists'",
                  Tuple.class)
              .uniqueResult();
      assertThat(result).isNotNull();
      assertThat(result.get("current_value", Long.class)).isEqualTo(123L);
    }
  }

  @Test
  void insert_throws_whenRowAlreadyExist() {
    StandardSequenceQuery query =
        new StandardSequenceQuery(
            "standard_sequence_query_integration_tests",
            "name",
            "current_value",
            "created_at",
            "last_modified_at",
            "insert-row-exists");

    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();
      session
          .createNativeMutationQuery(
              "INSERT INTO standard_sequence_query_integration_tests VALUES('insert-row-exists', 123, '2025-09-20T18:00:00.000', null)")
          .executeUpdate();
      session.getTransaction().commit();
    }

    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();

      assertThrows(
          ConstraintViolationException.class,
          () ->
              query.insert(
                  (SharedSessionContractImplementor) session, new StandardSequenceState(123L)));

      session.getTransaction().rollback();
    }
  }

  @Test
  void insert_successful_whenRowExists() {
    StandardSequenceQuery query =
        new StandardSequenceQuery(
            "standard_sequence_query_integration_tests",
            "name",
            "current_value",
            "created_at",
            "last_modified_at",
            "update-row-exists");

    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();
      session
          .createNativeMutationQuery(
              "INSERT INTO standard_sequence_query_integration_tests VALUES('update-row-exists', 123, '2025-09-20T18:00:00.000', null)")
          .executeUpdate();
      session.getTransaction().commit();
    }

    StandardSequenceState state;
    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();
      state =
          query.update((SharedSessionContractImplementor) session, new StandardSequenceState(124L));
      session.getTransaction().commit();
    }

    assertThat(state)
        .isNotNull()
        .extracting(StandardSequenceState::getCurrentValue)
        .isEqualTo(124L);

    try (Session session = sessionFactory.openSession()) {
      Tuple result =
          session
              .createNativeQuery(
                  "SELECT current_value FROM standard_sequence_query_integration_tests WHERE name='update-row-exists'",
                  Tuple.class)
              .uniqueResult();
      assertThat(result).isNotNull();
      assertThat(result.get("current_value", Long.class)).isEqualTo(124L);
    }
  }
}
