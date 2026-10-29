/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.outbound.UserRepository;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.user.User;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
class JpaUserRepository implements UserRepository {

  private final UserSpringRepository userSpringRepository;
  private final JpaMapper jpaMapper;

  @Override
  public void insert(User user) {
    userSpringRepository.saveAndFlush(jpaMapper.toJpa(user));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findById(UserId id) {
    return userSpringRepository.findAggregateById(id.value()).map(jpaMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByEmail(Email email) {
    return userSpringRepository.findAggregateByEmail(email.value()).map(jpaMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByEmail(Email email) {
    return userSpringRepository.existsByEmail(email.value());
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> findAll() {
    return userSpringRepository.findAll().stream().map(jpaMapper::toDomain).toList();
  }

  @Override
  public void deleteById(UserId id) {
    userSpringRepository.deleteById(id.value());
  }

  @Override
  public void update(User user) {
    userSpringRepository
        .findAggregateById(user.id())
        .ifPresent(
            entity -> {
              jpaMapper.fromDomain(user, entity);
              userSpringRepository.save(entity);
            });
  }
}
