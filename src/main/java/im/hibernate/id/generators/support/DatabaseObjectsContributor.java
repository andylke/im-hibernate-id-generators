package im.hibernate.id.generators.support;

import org.hibernate.boot.ResourceStreamLocator;
import org.hibernate.boot.spi.AdditionalMappingContributions;
import org.hibernate.boot.spi.AdditionalMappingContributor;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import im.hibernate.id.generators.standard.StandardSequenceDatabaseObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabaseObjectsContributor implements AdditionalMappingContributor {

  @Override
  public void contribute(
      AdditionalMappingContributions contributions,
      InFlightMetadataCollector metadata,
      ResourceStreamLocator resourceStreamLocator,
      MetadataBuildingContext buildingContext) {

    var metadataCollector = buildingContext.getMetadataCollector();
    metadataCollector.addAuxiliaryDatabaseObject(new StandardSequenceDatabaseObject());

    log.debug(
        "Registered Auxiliary Database Object: {}",
        StandardSequenceDatabaseObject.class.getSimpleName());
  }
}
