package im.hibernate.id.generators.support;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.hibernate.boot.ResourceStreamLocator;
import org.hibernate.boot.spi.AdditionalMappingContributions;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import im.hibernate.id.generators.standard.StandardSequenceDatabaseObject;

/**
 * Tests for {@link DatabaseObjectsContributor}
 *
 * @author Andy Lian
 */
class DatabaseObjectContributorTests {

  private DatabaseObjectsContributor contributor;
  private AdditionalMappingContributions contributions;
  private InFlightMetadataCollector metadataCollector;
  private ResourceStreamLocator resourceStreamLocator;
  private MetadataBuildingContext buildingContext;

  @BeforeEach
  void beforeEach() {
    contributor = new DatabaseObjectsContributor();
    contributions = mock(AdditionalMappingContributions.class);
    metadataCollector = mock(InFlightMetadataCollector.class);
    resourceStreamLocator = mock(ResourceStreamLocator.class);
    buildingContext = mock(MetadataBuildingContext.class);

    when(buildingContext.getMetadataCollector()).thenReturn(metadataCollector);
  }

  @Test
  void registeredSequenceStateDatabaseObject() {
    contributor.contribute(
        contributions, metadataCollector, resourceStreamLocator, buildingContext);

    verify(metadataCollector).addAuxiliaryDatabaseObject(isA(StandardSequenceDatabaseObject.class));
  }
}
