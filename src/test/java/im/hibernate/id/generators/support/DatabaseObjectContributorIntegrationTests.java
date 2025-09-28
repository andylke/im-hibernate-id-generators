package im.hibernate.id.generators.support;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.ServiceLoader;
import org.hibernate.boot.spi.AdditionalMappingContributor;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link DatabaseObjectsContributor}.
 *
 * @author Andy Lian
 */
class DatabaseObjectContributorIntegrationTests {

  @Test
  void databaseObjectsContributor_found_whenLoad() {
    ServiceLoader<AdditionalMappingContributor> loader =
        ServiceLoader.load(AdditionalMappingContributor.class);
    assertThat(
            loader.stream()
                .anyMatch(
                    contributor -> {
                      return contributor
                          .type()
                          .getName()
                          .equals(DatabaseObjectsContributor.class.getName());
                    }))
        .isTrue();
  }
}
