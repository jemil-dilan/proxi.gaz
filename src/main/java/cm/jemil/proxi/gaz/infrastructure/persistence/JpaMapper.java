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
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import org.mapstruct.*;

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

  default AdminUser toDomain(AdminUserJpa adminUser) {
    return AdminUser.reconstitute(
        UserId.from(adminUser.getId()),
        Email.from(adminUser.getEmail()),
        PasswordHash.from(adminUser.getPasswordHash()),
        CreatedAt.from(adminUser.getCreatedAt()));
  }

  default Demo toDomain(DemoJpa value) {
    return Demo.reconstitute(
        DemoId.from(value.getId()),
        DemoName.from(value.getName()),
        DemoReference.from(value.getReference()),
        DemoDescription.from(value.getDescription()));
  }
}
