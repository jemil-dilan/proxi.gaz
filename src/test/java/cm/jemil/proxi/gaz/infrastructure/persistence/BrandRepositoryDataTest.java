/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.application.outbound.BrandRepository;
import cm.jemil.proxi.gaz.application.outbound.UnitOfWork;
import cm.jemil.proxi.gaz.application.outbound.UserRepository;
import cm.jemil.proxi.gaz.application.usecase.CreateBrandUseCase;
import cm.jemil.proxi.gaz.domain.brand.Brand;
import cm.jemil.proxi.gaz.domain.brand.BrandAlreadyExistsException;
import cm.jemil.proxi.gaz.domain.brand.BrandName;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import cm.jemil.proxi.gaz.domain.user.User;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@Tag("data")
@SpringBootTest
@ActiveProfiles("data_test")
class BrandRepositoryDataTest {

  @Autowired private BrandRepository brandRepository;
  @Autowired private UnitOfWork unitOfWork;
  @Autowired private CreateBrandUseCase createBrandUseCase;
  @Autowired private UserRepository userRepository;
  @Autowired private JdbcTemplate jdbc;

  /** Brands are never deleted and the database is shared by the data tests: unique names. */
  private static String unique(String name) {
    return name + " " + UUID.randomUUID().toString().substring(0, 8);
  }

  @Test
  void uniqueIndex_shouldRejectANameThatDiffersOnlyInCaseEvenWithoutTheUseCase() {
    var name = unique("Index Gas");
    jdbc.update("INSERT INTO t_brands (c_id, c_name) VALUES (?, ?)", UUID.randomUUID(), name);

    assertThatThrownBy(
            () ->
                jdbc.update(
                    "INSERT INTO t_brands (c_id, c_name) VALUES (?, ?)",
                    UUID.randomUUID(),
                    name.toUpperCase()))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining(JpaBrandRepository.UNIQUE_NAME_INDEX);
  }

  @Test
  void insert_shouldMapADuplicateThatSlipsPastTheExistenceCheckToTheDomainException() {
    var name = unique("Race Gas");
    insertInTransaction(Brand.create(BrandName.from(name), CreatedAt.from(Instant.now())));
    var duplicate = Brand.create(BrandName.from(name.toLowerCase()), CreatedAt.from(Instant.now()));

    assertThatThrownBy(() -> insertInTransaction(duplicate))
        .isInstanceOf(BrandAlreadyExistsException.class);
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM t_brands WHERE lower(c_name) = lower(?)",
                Integer.class,
                name))
        .isOne();
  }

  @Test
  void existsByName_shouldMatchIgnoringCaseAndSpacing() {
    var name = unique("Exists Gas");
    insertInTransaction(Brand.create(BrandName.from(name), CreatedAt.from(Instant.now())));

    assertThat(brandRepository.existsByName(BrandName.from("  " + name.toUpperCase() + " ")))
        .isTrue();
    assertThat(brandRepository.existsByName(BrandName.from(unique("Missing Gas")))).isFalse();
  }

  @Test
  void findAll_shouldSortByNameIgnoringCaseAndFilterOnActivity() {
    var suffix = UUID.randomUUID().toString().substring(0, 8);
    var bravo = "zz-" + suffix + " bravo";
    var alpha = "ZZ-" + suffix + " Alpha";
    var inactive = "zz-" + suffix + " charlie";
    insertInTransaction(Brand.create(BrandName.from(bravo), CreatedAt.from(Instant.now())));
    insertInTransaction(Brand.create(BrandName.from(alpha), CreatedAt.from(Instant.now())));
    jdbc.update(
        "INSERT INTO t_brands (c_id, c_name, c_is_active) VALUES (?, ?, false)",
        UUID.randomUUID(),
        inactive);

    var all = names(brandRepository.findAll(null), suffix);
    var active = names(brandRepository.findAll(true), suffix);
    var inactiveOnly = names(brandRepository.findAll(false), suffix);

    assertThat(all).containsExactly(alpha, bravo, inactive);
    assertThat(active).containsExactly(alpha, bravo);
    assertThat(inactiveOnly).containsExactly(inactive);
  }

  @Test
  void createBrand_shouldCommitTheBrandWithItsAuditRowAndRejectADuplicate() {
    var admin =
        User.createAdmin(
            Email.from("brand-" + UUID.randomUUID() + "@example.test"),
            PasswordHash.from("{noop}irrelevant"),
            CreatedAt.from(Instant.now()));
    userRepository.insert(admin);
    var name = unique("Audited Gas");

    var brand = createBrandUseCase.execute(admin.getId(), BrandName.from(name));

    assertThat(
            jdbc.queryForObject(
                "SELECT c_action FROM t_admin_audit_log WHERE c_target_type = 'BRAND'"
                    + " AND c_target_id = ?",
                String.class,
                brand.id().value()))
        .isEqualTo("BRAND_CREATED");
    var duplicate = BrandName.from(name.toUpperCase());
    var adminId = admin.getId();
    assertThatThrownBy(() -> createBrandUseCase.execute(adminId, duplicate))
        .isInstanceOf(BrandAlreadyExistsException.class);
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM t_brands WHERE lower(c_name) = lower(?)",
                Integer.class,
                name))
        .isOne();
  }

  private void insertInTransaction(Brand brand) {
    unitOfWork.inTransaction(
        () -> {
          brandRepository.insert(brand);
          return brand;
        });
  }

  private static java.util.List<String> names(java.util.List<Brand> brands, String suffix) {
    return brands.stream()
        .map(brand -> brand.name().value())
        .filter(name -> name.contains(suffix))
        .toList();
  }
}
