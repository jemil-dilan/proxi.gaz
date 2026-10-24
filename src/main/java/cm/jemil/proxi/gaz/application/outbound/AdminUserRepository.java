/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.domain.admin.AdminUser;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import java.util.List;
import java.util.Optional;

public interface AdminUserRepository {

  void insert(AdminUser adminUser);

  Optional<AdminUser> findById(UserId id);

  Optional<AdminUser> findByEmail(Email email);

  boolean existsByEmail(Email email);

  List<AdminUser> findAll();

  void deleteById(UserId id);

  void update(AdminUser adminUser);
}
