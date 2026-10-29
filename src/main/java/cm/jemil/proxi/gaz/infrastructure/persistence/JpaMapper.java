/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.domain.demo.Demo;
import cm.jemil.proxi.gaz.domain.demo.DemoDescription;
import cm.jemil.proxi.gaz.domain.demo.DemoId;
import cm.jemil.proxi.gaz.domain.demo.DemoName;
import cm.jemil.proxi.gaz.domain.demo.DemoReference;
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.user.User;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import cm.jemil.proxi.gaz.domain.vendor.VendorSession;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionId;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionTokenHash;
import java.util.Optional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface JpaMapper {

  GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id.value")
  @Mapping(target = "email", source = "email.value")
  @Mapping(target = "passwordHash", source = "passwordHash.value")
  @Mapping(target = "createdAt", source = "createdAt.value")
  @Mapping(target = "roles", source = "roles")
  UserJpa toJpa(User user);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "email", source = "email.value")
  @Mapping(target = "passwordHash", source = "passwordHash.value")
  @Mapping(target = "roles", ignore = true)
  void fromDomain(User user, @MappingTarget UserJpa target);

  @AfterMapping
  default void syncRoles(User user, @MappingTarget UserJpa target) {
    target.getRoles().clear();
    target.getRoles().addAll(user.roles());
  }

  default User toDomain(UserJpa entity) {
    return User.reconstitute(
        UserId.from(entity.getId()),
        entity.getRoles(),
        Optional.ofNullable(entity.getEmail()).map(Email::from).orElse(null),
        Optional.ofNullable(entity.getPasswordHash()).map(PasswordHash::from).orElse(null),
        CreatedAt.from(entity.getCreatedAt()));
  }

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id.value")
  @Mapping(target = "userId", source = "userId.value")
  @Mapping(target = "name", source = "name.value")
  @Mapping(target = "phone", source = "phone.value")
  @Mapping(target = "addressText", source = "addressText.value")
  @Mapping(target = "location", source = "location")
  @Mapping(target = "station", source = "station")
  @Mapping(target = "deliveryFeeType", source = "deliveryFeeType.value")
  @Mapping(target = "deliveryFeeAmount", source = "deliveryFeeAmount.amountFcfa")
  @Mapping(target = "active", source = "active")
  VendorJpa toJpa(Vendor vendor);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id.value")
  @Mapping(target = "userId", source = "userId.value")
  @Mapping(target = "name", source = "name.value")
  @Mapping(target = "phone", source = "phone.value")
  @Mapping(target = "addressText", source = "addressText.value")
  @Mapping(target = "location", source = "location")
  @Mapping(target = "station", source = "station")
  @Mapping(target = "deliveryFeeType", source = "deliveryFeeType.value")
  @Mapping(target = "deliveryFeeAmount", source = "deliveryFeeAmount.amountFcfa")
  @Mapping(target = "active", source = "active")
  void fromDomain(Vendor vendor, @MappingTarget VendorJpa target);

  default Vendor toDomain(VendorJpa entity) {
    Point location = entity.getLocation();
    return Vendor.reconstitute(
        VendorId.from(entity.getId()),
        UserId.from(entity.getUserId()),
        VendorName.from(entity.getName()),
        PhoneNumber.from(entity.getPhone()),
        GeoPoint.of(location.getY(), location.getX()),
        AddressText.fromOptional(entity.getAddressText()).orElse(null),
        entity.isStation(),
        DeliveryFeeType.fromOptional(entity.getDeliveryFeeType()).orElse(null),
        Optional.ofNullable(entity.getDeliveryFeeAmount()).map(Money::fcfa).orElse(null),
        entity.isActive());
  }

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id.value")
  @Mapping(target = "vendorId", source = "vendorId.value")
  @Mapping(target = "tokenHash", source = "tokenHash.value")
  @Mapping(target = "createdAt", source = "createdAt.value")
  VendorSessionJpa toJpa(VendorSession session);

  default VendorSession toDomain(VendorSessionJpa entity) {
    return new VendorSession(
        VendorSessionId.from(entity.getId()),
        VendorId.from(entity.getVendorId()),
        new VendorSessionTokenHash(entity.getTokenHash()),
        CreatedAt.from(entity.getCreatedAt()));
  }

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id.value")
  @Mapping(target = "name", source = "name.value")
  @Mapping(target = "reference", source = "reference.value")
  @Mapping(target = "description", source = "description.value")
  DemoJpa fromDomain(Demo demo);

  default Demo toDomain(DemoJpa value) {
    return Demo.reconstitute(
        DemoId.from(value.getId()),
        DemoName.from(value.getName()),
        DemoReference.from(value.getReference()),
        DemoDescription.from(value.getDescription()));
  }

  default Point toPoint(GeoPoint value) {
    return GEOMETRY_FACTORY.createPoint(new Coordinate(value.longitude(), value.latitude()));
  }
}
