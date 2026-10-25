/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import java.util.UUID;

record VendorPersistenceModel(
    UUID id,
    String name,
    String phone,
    double latitude,
    double longitude,
    String addressText,
    boolean station,
    String deliveryFeeType,
    Integer deliveryFeeAmount,
    boolean active) {}
