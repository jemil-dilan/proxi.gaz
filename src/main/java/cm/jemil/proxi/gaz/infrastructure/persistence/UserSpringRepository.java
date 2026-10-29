/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface UserSpringRepository extends JpaRepository<UserJpa, UUID> {

  @EntityGraph(attributePaths = {"roles"})
  Optional<UserJpa> findAggregateById(UUID id);

  @EntityGraph(attributePaths = {"roles"})
  Optional<UserJpa> findAggregateByEmail(String email);

  boolean existsByEmail(String email);

  @Override
  @EntityGraph(attributePaths = {"roles"})
  @Query("select distinct user from UserJpa user")
  List<UserJpa> findAll();
}
