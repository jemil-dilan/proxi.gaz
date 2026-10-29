/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.VendorOnboardingRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenGenerator;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenHasher;
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.user.User;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import cm.jemil.proxi.gaz.domain.vendor.VendorSession;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionToken;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionTokenHash;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CreateVendorUseCaseTest {

  private final VendorOnboardingRepository onboardingRepository =
      mock(VendorOnboardingRepository.class);
  private final VendorSessionTokenGenerator tokenGenerator =
      mock(VendorSessionTokenGenerator.class);
  private final VendorSessionTokenHasher tokenHasher = mock(VendorSessionTokenHasher.class);
  private final Clock clock = mock(Clock.class);
  private final CreateVendorUseCase useCase =
      new CreateVendorUseCase(onboardingRepository, tokenGenerator, tokenHasher, clock);

  @Test
  void execute_shouldInsertUserVendorAndHashedSessionAndReturnRawTokenOnce() {
    when(clock.now()).thenReturn(Instant.EPOCH);
    var token = new VendorSessionToken("one-time-secret");
    var hash = new VendorSessionTokenHash("sha256-hash");
    when(tokenGenerator.generate()).thenReturn(token);
    when(tokenHasher.hash(token)).thenReturn(hash);

    var result = useCase.execute(command(null));

    var userCaptor = ArgumentCaptor.forClass(User.class);
    var vendorCaptor = ArgumentCaptor.forClass(Vendor.class);
    var sessionCaptor = ArgumentCaptor.forClass(VendorSession.class);
    verify(onboardingRepository)
        .insert(userCaptor.capture(), vendorCaptor.capture(), sessionCaptor.capture());

    var user = userCaptor.getValue();
    var vendor = vendorCaptor.getValue();
    var session = sessionCaptor.getValue();
    assertThat(user.isVendor()).isTrue();
    assertThat(user.email()).isEmpty();
    assertThat(vendor.getUserId()).isEqualTo(user.getId());
    assertThat(session.vendorId()).isEqualTo(vendor.getId());
    assertThat(session.tokenHash()).isEqualTo(hash);
    assertThat(result.vendorId()).isEqualTo(vendor.getId());
    assertThat(result.sessionToken()).isEqualTo(token.value());
  }

  @Test
  void execute_shouldAttachOptionalEmailToTheUserAccount() {
    when(clock.now()).thenReturn(Instant.EPOCH);
    var token = new VendorSessionToken("one-time-secret");
    when(tokenGenerator.generate()).thenReturn(token);
    when(tokenHasher.hash(token)).thenReturn(new VendorSessionTokenHash("hash"));
    var email = Email.from("vendor@example.test");

    useCase.execute(command(email));

    var userCaptor = ArgumentCaptor.forClass(User.class);
    verify(onboardingRepository)
        .insert(userCaptor.capture(), any(Vendor.class), any(VendorSession.class));
    assertThat(userCaptor.getValue().email()).contains(email);
  }

  private CreateVendorUseCase.Command command(Email email) {
    return new CreateVendorUseCase.Command(
        email,
        VendorName.from("Vendor"),
        PhoneNumber.from("+237699002001"),
        GeoPoint.of(4.05, 9.70),
        AddressText.from("Akwa"),
        false,
        DeliveryFeeType.from("flat"),
        Money.fcfa(1200),
        true);
  }
}
