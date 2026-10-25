/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

class PostgresVendorRepository implements VendorRepository {
  private final JdbcTemplate jdbc;
  private final JpaMapper mapper;

  PostgresVendorRepository(JdbcTemplate jdbc, JpaMapper mapper) {
    this.jdbc = jdbc;
    this.mapper = mapper;
  }

  @Override
  public void insert(Vendor vendor) {
    var row = mapper.toPersistence(vendor);
    jdbc.update(
        """
        INSERT INTO t_vendors (c_id, c_name, c_phone, c_address_text, c_is_station,
          c_delivery_fee_type, c_delivery_fee_amount, c_is_active, c_location)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography)
        """,
        row.id(),
        row.name(),
        row.phone(),
        row.addressText(),
        row.station(),
        row.deliveryFeeType(),
        row.deliveryFeeAmount(),
        row.active(),
        row.longitude(),
        row.latitude());
  }

  @Override
  public Optional<Vendor> findById(VendorId vendorId) {
    return jdbc
        .query(
            """
            SELECT c_id, c_name, c_phone, ST_Y(c_location::geometry) AS latitude,
              ST_X(c_location::geometry) AS longitude, c_address_text, c_is_station,
              c_delivery_fee_type, c_delivery_fee_amount, c_is_active
            FROM t_vendors WHERE c_id = ?
            """,
            (rs, rowNum) -> mapper.toDomain(read(rs)),
            vendorId.value())
        .stream()
        .findFirst();
  }

  @Override
  public void update(Vendor vendor) {
    var row = mapper.toPersistence(vendor);
    int updated =
        jdbc.update(
            """
            UPDATE t_vendors SET c_name = ?, c_phone = ?, c_address_text = ?, c_is_station = ?,
              c_delivery_fee_type = ?, c_delivery_fee_amount = ?, c_is_active = ?,
              c_location = ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography
            WHERE c_id = ?
            """,
            row.name(),
            row.phone(),
            row.addressText(),
            row.station(),
            row.deliveryFeeType(),
            row.deliveryFeeAmount(),
            row.active(),
            row.longitude(),
            row.latitude(),
            row.id());
    if (updated == 0) throw new VendorNotFoundException();
  }

  private static VendorPersistenceModel read(ResultSet rs) throws SQLException {
    return new VendorPersistenceModel(
        rs.getObject("c_id", UUID.class),
        rs.getString("c_name"),
        rs.getString("c_phone"),
        rs.getDouble("latitude"),
        rs.getDouble("longitude"),
        rs.getString("c_address_text"),
        rs.getBoolean("c_is_station"),
        rs.getString("c_delivery_fee_type"),
        (Integer) rs.getObject("c_delivery_fee_amount"),
        rs.getBoolean("c_is_active"));
  }
}
