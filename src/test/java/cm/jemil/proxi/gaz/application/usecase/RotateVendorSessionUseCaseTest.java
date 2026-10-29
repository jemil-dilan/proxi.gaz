/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenGenerator;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenHasher;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionToken;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionTokenHash;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RotateVendorSessionUseCaseTest {
  private final VendorRepository vendorRepository = mock(VendorRepository.class);
  private final VendorSessionRepository sessionRepository = mock(VendorSessionRepository.class);
  private final VendorSessionTokenGenerator tokenGenerator =
      mock(VendorSessionTokenGenerator.class);
  private final VendorSessionTokenHasher tokenHasher = mock(VendorSessionTokenHasher.class);
  private final Clock clock = mock(Clock.class);
  private final RotateVendorSessionUseCase useCase =
      new RotateVendorSessionUseCase(
          vendorRepository, sessionRepository, tokenGenerator, tokenHasher, clock);

  @Test
  void execute_shouldIssueOnceAndReplaceExistingSession() {
    var vendorId = VendorId.generate();
    var token = new VendorSessionToken("new-opaque-secret");
    var tokenHash = new VendorSessionTokenHash("new-hash");
    Vendor vendor = mock(Vendor.class);

    when(vendorRepository.findById(vendorId)).thenReturn(vendor);
    when(tokenGenerator.generate()).thenReturn(token);
    when(tokenHasher.hash(token)).thenReturn(tokenHash);
    when(clock.now()).thenReturn(Instant.EPOCH);

    var returnedToken = useCase.execute(vendorId.value());

    assertThat(returnedToken).isEqualTo(token.value());
    var session = ArgumentCaptor.forClass(cm.jemil.proxi.gaz.domain.vendor.VendorSession.class);
    verify(sessionRepository)
        .replaceForVendor(org.mockito.ArgumentMatchers.eq(vendorId), session.capture());
    assertThat(session.getValue().tokenHash()).isEqualTo(tokenHash);
    assertThat(session.getValue().createdAt().value()).isEqualTo(Instant.EPOCH);
  }
}
