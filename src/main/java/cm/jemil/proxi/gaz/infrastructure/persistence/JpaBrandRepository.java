/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import cm.jemil.proxi.gaz.application.outbound.BrandRepository;
import cm.jemil.proxi.gaz.domain.brand.Brand;
import cm.jemil.proxi.gaz.domain.brand.BrandAlreadyExistsException;
import cm.jemil.proxi.gaz.domain.brand.BrandId;
import cm.jemil.proxi.gaz.domain.brand.BrandName;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
class JpaBrandRepository implements BrandRepository {

  static final String UNIQUE_NAME_INDEX = "uq_brands_name_lower";

  private final BrandSpringRepository brandSpringRepository;

  /**
   * Flushes immediately so a concurrent duplicate surfaces here, as a domain exception, rather than
   * at commit time.
   */
  @Override
  public void insert(Brand brand) {
    try {
      brandSpringRepository.saveAndFlush(toJpa(brand));
    } catch (DataIntegrityViolationException e) {
      if (violates(e, UNIQUE_NAME_INDEX)) {
        throw new BrandAlreadyExistsException();
      }
      throw e;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByName(BrandName name) {
    return brandSpringRepository.existsByNameKey(name.key());
  }

  @Override
  @Transactional(readOnly = true)
  public List<Brand> findAll(@Nullable Boolean active) {
    var rows =
        active == null
            ? brandSpringRepository.findAllSorted()
            : brandSpringRepository.findAllSorted(active);
    return rows.stream().map(JpaBrandRepository::toDomain).toList();
  }

  private static boolean violates(Throwable error, String constraint) {
    for (var cause = error; cause != null; cause = cause.getCause()) {
      var message = cause.getMessage();
      if (message != null && message.toLowerCase(Locale.ROOT).contains(constraint)) {
        return true;
      }
    }
    return false;
  }

  private static BrandJpa toJpa(Brand brand) {
    var row = new BrandJpa();
    row.setId(brand.id().value());
    row.setName(brand.name().value());
    row.setActive(brand.active());
    row.setCreatedAt(brand.createdAt().value());
    return row;
  }

  private static Brand toDomain(BrandJpa row) {
    return new Brand(
        BrandId.from(row.getId()),
        BrandName.from(row.getName()),
        row.isActive(),
        CreatedAt.from(row.getCreatedAt()));
  }
}
