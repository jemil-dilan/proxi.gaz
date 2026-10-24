/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.outbound.AdminUserRepository;
import cm.jemil.proxi.gaz.domain.admin.AdminUser;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
class JpaAdminUserRepository implements AdminUserRepository {

  private final AdminUserSpringRepository adminUserSpringRepository;
  private final JpaMapper jpaMapper;

  @Override
  public void insert(AdminUser adminUser) {
    adminUserSpringRepository.saveAndFlush(jpaMapper.fromDomain(adminUser));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<AdminUser> findById(UserId id) {
    return adminUserSpringRepository.findById(id.value()).map(jpaMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<AdminUser> findByEmail(Email email) {
    return adminUserSpringRepository.findByEmail(email.value()).map(jpaMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByEmail(Email email) {
    return adminUserSpringRepository.existsByEmail(email.value());
  }

  @Override
  @Transactional(readOnly = true)
  public List<AdminUser> findAll() {
    return adminUserSpringRepository.findAll().stream().map(jpaMapper::toDomain).toList();
  }

  @Override
  public void deleteById(UserId id) {
    adminUserSpringRepository.deleteById(id.value());
  }

  @Override
  public void update(AdminUser adminUser) {
    adminUserSpringRepository
        .findById(adminUser.getId().value())
        .ifPresent(
            adminUserJpa -> {
              jpaMapper.fromDomain(adminUserJpa, adminUser);
              adminUserSpringRepository.saveAndFlush(adminUserJpa);
            });
  }
}
