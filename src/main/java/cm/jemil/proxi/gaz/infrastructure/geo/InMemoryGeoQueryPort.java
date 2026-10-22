/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.geo;

import cm.jemil.proxi.gaz.application.outbound.GeoQueryPort;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory implementation of GeoQueryPort for testing. Does not require PostGIS and can be used in
 * unit tests. Stores vendor locations in memory and calculates approximate distances.
 */
public class InMemoryGeoQueryPort implements GeoQueryPort {

  private final Map<UUID, GeoPoint> vendorLocations = new HashMap<>();

  /**
   * Register a vendor with its geographic location. Only for test setup purposes.
   *
   * @param vendorId the vendor identifier
   * @param location the vendor's geographic location
   */
  public void registerVendor(UUID vendorId, GeoPoint location) {
    vendorLocations.put(vendorId, location);
  }

  /** Clear all registered vendors. Only for test setup purposes. */
  public void clear() {
    vendorLocations.clear();
  }

  @Override
  public List<UUID> findNearbyVendors(GeoPoint center, int radiusMeters, boolean isActive) {
    // In-memory implementation returns all vendors
    // Real PostGIS implementation will use ST_DWithin and filter by is_active
    return new ArrayList<>(vendorLocations.keySet());
  }

  @Override
  public double calculateDistanceMeters(GeoPoint pointA, GeoPoint pointB) {
    // Haversine formula for approximate distance calculation
    final double R = 6371000; // Earth radius in meters

    double lat1 = Math.toRadians(pointA.latitude());
    double lon1 = Math.toRadians(pointA.longitude());
    double lat2 = Math.toRadians(pointB.latitude());
    double lon2 = Math.toRadians(pointB.longitude());

    double dLat = lat2 - lat1;
    double dLon = lon2 - lon1;

    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
  }
}
