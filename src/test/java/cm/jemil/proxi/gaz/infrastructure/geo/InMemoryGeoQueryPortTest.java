/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.geo;

import static org.assertj.core.api.Assertions.assertThat;

import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryGeoQueryPortTest {

  private InMemoryGeoQueryPort geoQueryPort;

  @BeforeEach
  void setUp() {
    geoQueryPort = new InMemoryGeoQueryPort();
  }

  @Test
  void findNearbyVendors_ReturnsEmptyList_WhenNoVendorsRegistered() {
    GeoPoint center = GeoPoint.of(4.05, 9.7);
    List<UUID> result = geoQueryPort.findNearbyVendors(center, 5000, true);
    assertThat(result).isEmpty();
  }

  @Test
  void findNearbyVendors_ReturnsAllRegisteredVendors() {
    UUID vendor1 = UUID.randomUUID();
    UUID vendor2 = UUID.randomUUID();

    geoQueryPort.registerVendor(vendor1, GeoPoint.of(4.05, 9.7));
    geoQueryPort.registerVendor(vendor2, GeoPoint.of(4.06, 9.71));

    GeoPoint center = GeoPoint.of(4.05, 9.7);
    List<UUID> result = geoQueryPort.findNearbyVendors(center, 5000, true);

    assertThat(result).containsExactlyInAnyOrder(vendor1, vendor2);
  }

  @Test
  void findNearbyVendors_WithIsActiveFalse_ReturnsAllVendors() {
    UUID vendorId = UUID.randomUUID();
    geoQueryPort.registerVendor(vendorId, GeoPoint.of(4.05, 9.7));

    GeoPoint center = GeoPoint.of(4.05, 9.7);
    List<UUID> resultAll = geoQueryPort.findNearbyVendors(center, 5000, false);
    List<UUID> resultActive = geoQueryPort.findNearbyVendors(center, 5000, true);

    // InMemory implementation returns all vendors regardless of isActive
    // (real PostGIS implementation will filter by is_active column)
    assertThat(resultAll).contains(vendorId);
    assertThat(resultActive).contains(vendorId);
  }

  @Test
  void calculateDistanceMeters_SamePoint_ReturnsZero() {
    GeoPoint point = GeoPoint.of(4.05, 9.7);
    double distance = geoQueryPort.calculateDistanceMeters(point, point);
    assertThat(distance).isEqualTo(0.0);
  }

  @Test
  void calculateDistanceMeters_KnownDistance_ReturnsApproximateValue() {
    // Approximate distance between two points about 1 degree apart
    GeoPoint pointA = GeoPoint.of(4.0, 9.0);
    GeoPoint pointB = GeoPoint.of(4.0, 10.0);

    double distance = geoQueryPort.calculateDistanceMeters(pointA, pointB);

    // 1 degree of longitude at equator is about 111 km
    // So 1 degree should be roughly 111,000 meters
    assertThat(distance).isBetween(100000.0, 120000.0);
  }

  @Test
  void clear_RemovesAllRegisteredVendors() {
    UUID vendorId = UUID.randomUUID();
    geoQueryPort.registerVendor(vendorId, GeoPoint.of(4.05, 9.7));

    assertThat(geoQueryPort.findNearbyVendors(GeoPoint.of(4.05, 9.7), 5000, true))
        .contains(vendorId);

    geoQueryPort.clear();

    assertThat(geoQueryPort.findNearbyVendors(GeoPoint.of(4.05, 9.7), 5000, true)).isEmpty();
  }
}
