/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import java.util.List;
import java.util.UUID;

/**
 * Port interface for geographic queries. All implementations MUST use PostGIS for distance and
 * proximity calculations. Results must be ranked strictly by geographic distance.
 *
 * <p>Core domain rule: Distance/proximity queries must use PostGIS (ST_DWithin, ST_Distance).
 * Ranking is strictly local.
 */
public interface GeoQueryPort {

  /**
   * Find vendors within a specified radius of a center point.
   *
   * <p>Results are ordered by distance from the center point (closest first).
   *
   * @param center the center point (typically client location), not null
   * @param radiusMeters the search radius in meters, must be positive
   * @param isActive true to filter only active vendors, false to include all vendors
   * @return list of vendor IDs matching the criteria, ordered by distance; empty list if no vendors
   *     found
   */
  List<UUID> findNearbyVendors(GeoPoint center, int radiusMeters, boolean isActive);

  /**
   * Calculate the geographic distance between two points in meters.
   *
   * <p>Uses PostGIS ST_Distance function on geography type.
   *
   * @param pointA first geographic point, not null
   * @param pointB second geographic point, not null
   * @return distance in meters between the two points
   */
  double calculateDistanceMeters(GeoPoint pointA, GeoPoint pointB);
}
