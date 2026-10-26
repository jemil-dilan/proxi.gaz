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
