/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenHasher;
import cm.jemil.proxi.gaz.domain.vendor.InvalidVendorSessionException;
import cm.jemil.proxi.gaz.domain.vendor.VendorInactiveException;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorSession;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionToken;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionTokenHash;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ValidateVendorSessionUseCaseTest {
  private final VendorSessionRepository repository = mock(VendorSessionRepository.class);
  private final VendorSessionTokenHasher tokenHasher = mock(VendorSessionTokenHasher.class);
  private final VendorRepository vendorRepository = mock(VendorRepository.class);
  private final ValidateVendorSessionUseCase useCase =
      new ValidateVendorSessionUseCase(repository, tokenHasher, vendorRepository);

  @Test
  void execute_shouldReturnVendorForStoredTokenHash() {
    var vendorId = VendorId.generate();
    var token = new VendorSessionToken("opaque-secret");
    var hash = new VendorSessionTokenHash("sha256-hash");
    var session =
        VendorSession.of(
            vendorId, hash, cm.jemil.proxi.gaz.domain.shared.CreatedAt.from(Instant.EPOCH));
    var vendor = mock(cm.jemil.proxi.gaz.domain.vendor.Vendor.class);
    when(vendor.id()).thenReturn(vendorId.value());
    when(vendor.getName()).thenReturn(cm.jemil.proxi.gaz.domain.vendor.VendorName.from("Test Vendor"));
    when(vendor.isActive()).thenReturn(true);
    when(tokenHasher.hash(token)).thenReturn(hash);
    when(repository.findByTokenHash(hash)).thenReturn(Optional.of(session));
    when(vendorRepository.findById(vendorId)).thenReturn(vendor);

    var result = useCase.execute(token.value());

    assertThat(result.vendorId()).isEqualTo(vendorId);
    assertThat(result.name()).isEqualTo("Test Vendor");
    assertThat(result.isActive()).isTrue();
    verify(repository).findByTokenHash(hash);
    verify(vendorRepository).findById(vendorId);
  }

  @Test
  void execute_shouldRejectUnknownOrRevokedToken() {
    var token = new VendorSessionToken("unknown-secret");
    var hash = new VendorSessionTokenHash("unknown-hash");
    when(tokenHasher.hash(token)).thenReturn(hash);
    when(repository.findByTokenHash(hash)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(token.value()))
        .isInstanceOf(InvalidVendorSessionException.class);
  }

  @Test
  void execute_shouldRejectBlankTokenAsUnauthorizedSession() {
    assertThatThrownBy(() -> useCase.execute(" "))
        .isInstanceOf(InvalidVendorSessionException.class);
  }

  @Test
  void execute_shouldRejectInactiveVendor() {
    var vendorId = VendorId.generate();
    var token = new VendorSessionToken("opaque-secret");
    var hash = new VendorSessionTokenHash("sha256-hash");
    var session =
        VendorSession.of(
            vendorId, hash, cm.jemil.proxi.gaz.domain.shared.CreatedAt.from(Instant.EPOCH));
    var vendor = mock(cm.jemil.proxi.gaz.domain.vendor.Vendor.class);
    when(vendor.isActive()).thenReturn(false);
    when(tokenHasher.hash(token)).thenReturn(hash);
    when(repository.findByTokenHash(hash)).thenReturn(Optional.of(session));
    when(vendorRepository.findById(vendorId)).thenReturn(vendor);

    assertThatThrownBy(() -> useCase.execute(token.value()))
        .isInstanceOf(VendorInactiveException.class);
  }
}
