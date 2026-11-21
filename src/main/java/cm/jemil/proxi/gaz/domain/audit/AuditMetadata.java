/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.audit;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Details of an audited action. Only allowlisted keys and value types are accepted, so credentials,
 * session tokens, hashes or personal values can never reach the audit log by accident.
 */
public record AuditMetadata(Map<String, Object> values) {

  static final String IS_ACTIVE = "isActive";
  static final String PREVIOUS_ACTIVE = "previousActive";
  static final String CHANGED_FIELDS = "changedFields";

  /** Vendor profile fields an update may report as changed (names only, never values). */
  public static final Set<String> VENDOR_FIELDS =
      Set.of(
          "name",
          "phone",
          "location",
          "addressText",
          "isStation",
          "deliveryFeeType",
          "deliveryFeeAmount");

  public AuditMetadata {
    if (values == null) {
      throw new InvalidDomainValueException("audit metadata must not be null");
    }
    values.forEach(AuditMetadata::validate);
    values = Map.copyOf(values);
  }

  public static AuditMetadata of(Map<String, Object> values) {
    return new AuditMetadata(values);
  }

  public static AuditMetadata empty() {
    return new AuditMetadata(Map.of());
  }

  public static AuditMetadata created(boolean isActive) {
    return new AuditMetadata(Map.of(IS_ACTIVE, isActive));
  }

  public static AuditMetadata statusChanged(boolean previousActive) {
    return new AuditMetadata(Map.of(PREVIOUS_ACTIVE, previousActive));
  }

  public static AuditMetadata updated(List<String> changedFields) {
    var values = new LinkedHashMap<String, Object>();
    values.put(CHANGED_FIELDS, List.copyOf(changedFields));
    return new AuditMetadata(values);
  }

  private static void validate(String key, Object value) {
    var valid =
        switch (key) {
          case IS_ACTIVE, PREVIOUS_ACTIVE -> value instanceof Boolean;
          case CHANGED_FIELDS ->
              value instanceof List<?> fields
                  && fields.stream()
                      .allMatch(
                          field -> field instanceof String name && VENDOR_FIELDS.contains(name));
          case null, default -> false;
        };
    if (!valid) {
      throw new InvalidDomainValueException("audit metadata entry not allowed: " + key);
    }
  }
}
