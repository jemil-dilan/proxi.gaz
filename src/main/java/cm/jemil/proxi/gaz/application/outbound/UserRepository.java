/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.user.User;
import java.util.List;
import java.util.Optional;

/** Persistence port for the User aggregate root. */
public interface UserRepository {

  void insert(User user);

  Optional<User> findById(UserId id);

  Optional<User> findByEmail(Email email);

  boolean existsByEmail(Email email);

  List<User> findAll();

  void deleteById(UserId id);

  void update(User user);
}
