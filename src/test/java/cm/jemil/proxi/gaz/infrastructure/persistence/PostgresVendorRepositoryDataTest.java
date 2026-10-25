/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorDetails;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@Tag("data")
@SpringBootTest
@ActiveProfiles("data_test")
class PostgresVendorRepositoryDataTest {
  @Autowired private VendorRepository repository;
  @Autowired private JdbcTemplate jdbc;

  @Test
  void insertFindAndUpdate_shouldPersistVendorFieldsAndPostgisCoordinates() {
    var vendor =
        Vendor.create(
            VendorDetails.from(
                "Data Vendor", "+237699002009", 4.051, 9.702, "Akwa", false, "flat", 1200, true));

    repository.insert(vendor);

    var roundTrip = repository.findById(vendor.id()).orElseThrow();
    assertThat(roundTrip.details().location().latitude()).isEqualTo(4.051);
    assertThat(roundTrip.details().location().longitude()).isEqualTo(9.702);
    assertThat(
            jdbc.queryForObject(
                "SELECT ST_SRID(c_location::geometry) FROM t_vendors WHERE c_id = ?",
                Integer.class,
                vendor.id().value()))
        .isEqualTo(4326);

    var updated =
        Vendor.reconstitute(
            vendor.id(),
            VendorDetails.from(
                "Updated Vendor", "+237699002010", 4.06, 9.71, null, true, null, null, false));
    repository.update(updated);

    assertThat(
            repository
                .findById(VendorId.from(vendor.id().value()))
                .orElseThrow()
                .details()
                .name()
                .value())
        .isEqualTo("Updated Vendor");
    assertThat(
            jdbc.queryForObject(
                "SELECT c_address_text FROM t_vendors WHERE c_id = ?",
                String.class,
                vendor.id().value()))
        .isNull();
  }
}
