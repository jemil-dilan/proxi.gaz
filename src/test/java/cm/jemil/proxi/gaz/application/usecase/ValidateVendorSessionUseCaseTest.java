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
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.vendor.InvalidVendorSessionException;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorInactiveException;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import cm.jemil.proxi.gaz.domain.vendor.VendorSession;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionToken;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionTokenHash;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidateVendorSessionUseCaseTest {
  @Mock private VendorSessionRepository repository;
  @Mock private VendorSessionTokenHasher tokenHasher;
  @Mock private VendorRepository vendorRepository;
  @InjectMocks private ValidateVendorSessionUseCase objectUnderTest;

  @Test
  void execute_shouldReturnVendorForStoredTokenHash() {
    var vendorId = VendorId.generate();
    var token = new VendorSessionToken("opaque-secret");
    var hash = new VendorSessionTokenHash("sha256-hash");
    var session = VendorSession.of(vendorId, hash, CreatedAt.from(Instant.EPOCH));
    var vendor = mock(Vendor.class);
    when(vendor.getName()).thenReturn(VendorName.from("Test Vendor"));
    when(vendor.isActive()).thenReturn(true);
    when(tokenHasher.hash(token)).thenReturn(hash);
    when(repository.findByTokenHash(hash)).thenReturn(session);
    when(vendorRepository.findById(vendorId)).thenReturn(vendor);

    var result = objectUnderTest.execute(token.value());

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
    String rawToken = token.value();
    when(tokenHasher.hash(token)).thenReturn(hash);
    when(repository.findByTokenHash(hash)).thenThrow(new InvalidVendorSessionException());

    assertThatThrownBy(() -> objectUnderTest.execute(rawToken))
        .isInstanceOf(InvalidVendorSessionException.class);
  }

  @Test
  void execute_shouldRejectBlankTokenAsUnauthorizedSession() {
    assertThatThrownBy(() -> objectUnderTest.execute(" "))
        .isInstanceOf(InvalidVendorSessionException.class);
  }

  @Test
  void execute_shouldRejectInactiveVendor() {
    var vendorId = VendorId.generate();
    var token = new VendorSessionToken("opaque-secret");
    var hash = new VendorSessionTokenHash("sha256-hash");
    String rawToken = token.value();
    var session = VendorSession.of(vendorId, hash, CreatedAt.from(Instant.EPOCH));
    var vendor = mock(Vendor.class);
    when(vendor.isActive()).thenReturn(false);
    when(tokenHasher.hash(token)).thenReturn(hash);
    when(repository.findByTokenHash(hash)).thenReturn(session);
    when(vendorRepository.findById(vendorId)).thenReturn(vendor);

    assertThatThrownBy(() -> objectUnderTest.execute(rawToken))
        .isInstanceOf(VendorInactiveException.class);
  }
}
