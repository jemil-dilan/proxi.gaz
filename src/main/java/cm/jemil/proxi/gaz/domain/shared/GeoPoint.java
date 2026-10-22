/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;

/** WGS84 coordinates (latitude, longitude) for PostGIS geography points. */
public record GeoPoint(double latitude, double longitude) {

  public GeoPoint {
    if (latitude < -90.0 || latitude > 90.0) {
      throw new InvalidDomainValueException("latitude must be between -90 and 90");
    }
    if (longitude < -180.0 || longitude > 180.0) {
      throw new InvalidDomainValueException("longitude must be between -180 and 180");
    }
  }

  public static GeoPoint of(double latitude, double longitude) {
    return new GeoPoint(latitude, longitude);
  }
}
