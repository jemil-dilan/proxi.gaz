/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.application.outbound.UserRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorOnboardingRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.user.User;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import cm.jemil.proxi.gaz.domain.vendor.VendorSession;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionTokenHash;
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
class VendorRepositoryDataTest {

  @Autowired private UserRepository userRepository;
  @Autowired private VendorRepository vendorRepository;
  @Autowired private VendorOnboardingRepository onboardingRepository;
  @Autowired private JdbcTemplate jdbc;

  @Test
  void onboarding_shouldPersistUserVendorAndHashedSessionWithPostgisCoordinates() {
    var createdAt = CreatedAt.from(Instant.now());
    var user = User.createVendor(null, createdAt);
    var vendor = vendor(user, "Data Vendor", "+237699002009", 4.051, 9.702, true);
    var session =
        VendorSession.of(
            vendor.getId(), new VendorSessionTokenHash("test-session-hash"), createdAt);
    onboardingRepository.insert(user, vendor, session);

    var roundTrip = vendorRepository.findById(vendor.getId());
    assertThat(roundTrip.getId()).isEqualTo(vendor.getId());
    assertThat(roundTrip.getId().value()).isNotEqualTo(user.getId().value());
    assertThat(roundTrip.getUserId()).isEqualTo(user.getId());
    assertThat(roundTrip.getName()).isEqualTo(VendorName.from("Data Vendor"));
    assertThat(roundTrip.getLocation().latitude()).isEqualTo(4.051);
    assertThat(roundTrip.getLocation().longitude()).isEqualTo(9.702);
    assertThat(userRepository.findById(user.getId()).orElseThrow().roles())
        .containsExactly(cm.jemil.proxi.gaz.domain.user.UserRole.VENDOR);
    assertThat(
            jdbc.queryForObject(
                "SELECT c_user_id FROM t_vendors WHERE c_id = ?",
                UUID.class,
                vendor.getId().value()))
        .isEqualTo(user.getId().value());
    assertThat(
            jdbc.queryForObject(
                "SELECT ST_SRID(c_location::geometry) FROM t_vendors WHERE c_id = ?",
                Integer.class,
                vendor.getId().value()))
        .isEqualTo(4326);
    assertThat(
            jdbc.queryForObject(
                "SELECT c_reliability_score FROM t_vendors WHERE c_id = ?",
                Integer.class,
                vendor.getId().value()))
        .isZero();
    assertThat(
            jdbc.queryForObject(
                "SELECT c_token_hash FROM t_vendor_sessions WHERE c_id = ?",
                String.class,
                session.id().value()))
        .isEqualTo("test-session-hash");
  }

  @Test
  void userEmailMayBeNullForMultipleVendorAccounts() {
    var createdAt = CreatedAt.from(Instant.now());
    var one = User.createVendor(null, createdAt);
    var oneVendor = vendor(one, "No email vendor one", "+237699002021", 4.05, 9.70, true);
    var two = User.createVendor(null, createdAt);
    var twoVendor = vendor(two, "No email vendor two", "+237699002022", 4.06, 9.71, true);
    onboardingRepository.insert(
        one,
        oneVendor,
        VendorSession.of(
            oneVendor.getId(), new VendorSessionTokenHash("null-email-token-one"), createdAt));
    onboardingRepository.insert(
        two,
        twoVendor,
        VendorSession.of(
            twoVendor.getId(), new VendorSessionTokenHash("null-email-token-two"), createdAt));

    assertThat(userRepository.findById(one.getId())).contains(one);
    assertThat(userRepository.findById(two.getId())).contains(two);
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM t_users WHERE c_id IN (?, ?) AND c_email IS NULL",
                Integer.class,
                one.getId().value(),
                two.getId().value()))
        .isEqualTo(2);
  }

  @Test
  void update_shouldChangeVendorProfileWithoutChangingItsUserAssociation() {
    var createdAt = CreatedAt.from(Instant.now());
    var user = User.createVendor(null, createdAt);
    var original = vendor(user, "Before", "+237699002010", 4.06, 9.71, true);
    onboardingRepository.insert(
        user,
        original,
        VendorSession.of(
            original.getId(), new VendorSessionTokenHash("update-test-hash"), createdAt));
    var updated =
        Vendor.reconstitute(
            original.getId(),
            original.getUserId(),
            VendorName.from("After"),
            PhoneNumber.from("+237699002011"),
            GeoPoint.of(4.07, 9.72),
            AddressText.from("Bonanjo"),
            false,
            DeliveryFeeType.from("flat"),
            Money.fcfa(1000),
            false);

    vendorRepository.update(updated);

    var roundTrip = vendorRepository.findById(original.getId());
    assertThat(roundTrip.getUserId()).isEqualTo(user.getId());
    assertThat(roundTrip.getName()).isEqualTo(VendorName.from("After"));
    assertThat(roundTrip.isActive()).isFalse();
  }

  @Test
  void onboarding_shouldRollbackUserVendorAndSessionWhenVendorInsertFails() {
    jdbc.execute(
        "ALTER TABLE t_vendors DROP CONSTRAINT IF EXISTS ck_test_vendor_onboarding_rollback");
    jdbc.execute(
        "ALTER TABLE t_vendors ADD CONSTRAINT ck_test_vendor_onboarding_rollback "
            + "CHECK (c_name <> 'rollback-test')");
    var createdAt = CreatedAt.from(Instant.now());
    var user = User.createVendor(null, createdAt);
    var vendor = vendor(user, "rollback-test", "+237699002012", 4.051, 9.702, true);
    var session =
        VendorSession.of(
            vendor.getId(), new VendorSessionTokenHash("rollback-test-hash"), createdAt);
    try {
      assertThatThrownBy(() -> onboardingRepository.insert(user, vendor, session))
          .isInstanceOf(DataIntegrityViolationException.class);
      assertThat(userRepository.findById(user.getId())).isEmpty();
      assertThat(vendorRepository.findByUserId(user.getId())).isEmpty();
      assertThat(
              jdbc.queryForObject(
                  "SELECT COUNT(*) FROM t_vendor_sessions WHERE c_id = ?",
                  Integer.class,
                  session.id().value()))
          .isZero();
    } finally {
      jdbc.execute(
          "ALTER TABLE t_vendors DROP CONSTRAINT IF EXISTS ck_test_vendor_onboarding_rollback");
    }
  }

  @Test
  void onboarding_shouldRejectReusingAnExistingSessionTokenHash() {
    var createdAt = CreatedAt.from(Instant.now());
    var firstUser = User.createVendor(null, createdAt);
    var firstVendor = vendor(firstUser, "Token owner one", "+237699002031", 4.05, 9.70, true);
    onboardingRepository.insert(
        firstUser,
        firstVendor,
        VendorSession.of(
            firstVendor.getId(), new VendorSessionTokenHash("same-token-hash"), createdAt));

    var secondUser = User.createVendor(null, createdAt);
    var secondVendor = vendor(secondUser, "Token owner two", "+237699002032", 4.06, 9.71, true);
    assertThatThrownBy(
            () ->
                onboardingRepository.insert(
                    secondUser,
                    secondVendor,
                    VendorSession.of(
                        secondVendor.getId(),
                        new VendorSessionTokenHash("same-token-hash"),
                        createdAt)))
        .isInstanceOf(DataIntegrityViolationException.class);
    assertThat(userRepository.findById(secondUser.getId())).isEmpty();
    assertThatThrownBy(() -> vendorRepository.findById(secondVendor.getId()))
        .isInstanceOf(cm.jemil.proxi.gaz.domain.vendor.VendorNotFoundException.class);
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM t_vendor_sessions WHERE c_vendor_id = ?",
                Integer.class,
                secondVendor.getId().value()))
        .isZero();
  }

  private Vendor vendor(
      User user, String name, String phone, double latitude, double longitude, boolean active) {
    return Vendor.of(
        user.getId(),
        VendorName.from(name),
        PhoneNumber.from(phone),
        GeoPoint.of(latitude, longitude),
        AddressText.from("Akwa"),
        false,
        DeliveryFeeType.from("flat"),
        Money.fcfa(1200),
        active);
  }
}
