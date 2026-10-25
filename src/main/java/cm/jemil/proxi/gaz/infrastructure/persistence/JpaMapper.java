/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.domain.admin.AdminUser;
import cm.jemil.proxi.gaz.domain.demo.Demo;
import cm.jemil.proxi.gaz.domain.demo.DemoDescription;
import cm.jemil.proxi.gaz.domain.demo.DemoId;
import cm.jemil.proxi.gaz.domain.demo.DemoName;
import cm.jemil.proxi.gaz.domain.demo.DemoReference;
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorDetails;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import java.util.Optional;
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

  default VendorPersistenceModel toPersistence(Vendor vendor) {
    var d = vendor.details();
    return new VendorPersistenceModel(
        vendor.id().value(),
        d.name().value(),
        d.phone().value(),
        d.location().latitude(),
        d.location().longitude(),
        d.addressText().map(AddressText::value).orElse(null),
        d.station(),
        d.deliveryFeeType().map(DeliveryFeeType::value).orElse(null),
        d.deliveryFeeAmount().map(Money::amountFcfa).orElse(null),
        d.active());
  }

  default Vendor toDomain(VendorPersistenceModel row) {
    return Vendor.reconstitute(
        VendorId.from(row.id()),
        new VendorDetails(
            VendorName.from(row.name()),
            PhoneNumber.from(row.phone()),
            GeoPoint.of(row.latitude(), row.longitude()),
            AddressText.fromOptional(row.addressText()),
            row.station(),
            DeliveryFeeType.fromOptional(row.deliveryFeeType()),
            Optional.ofNullable(row.deliveryFeeAmount()).map(Money::fcfa),
            row.active()));
  }

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id.value")
  @Mapping(target = "name", source = "name.value")
  @Mapping(target = "reference", source = "reference.value")
  @Mapping(target = "description", source = "description.value")
  DemoJpa fromDomain(Demo demo);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id.value")
  @Mapping(target = "email", source = "email.value")
  @Mapping(target = "passwordHash", source = "passwordHash.value")
  @Mapping(target = "createdAt", source = "createdAt.value")
  AdminUserJpa fromDomain(AdminUser adminUser);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id.value")
  @Mapping(target = "email", source = "email.value")
  @Mapping(target = "passwordHash", source = "passwordHash.value")
  @Mapping(target = "createdAt", source = "createdAt.value")
  void fromDomain(@MappingTarget AdminUserJpa adminUserJpa, AdminUser adminUser);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id.value", source = "id")
  @Mapping(target = "email.value", source = "email")
  @Mapping(target = "passwordHash.value", source = "passwordHash")
  @Mapping(target = "createdAt.value", source = "createdAt")
  AdminUser toDomain(AdminUserJpa adminUser);

  default Demo toDomain(DemoJpa value) {
    return Demo.reconstitute(
        DemoId.from(value.getId()),
        DemoName.from(value.getName()),
        DemoReference.from(value.getReference()),
        DemoDescription.from(value.getDescription()));
  }
}
