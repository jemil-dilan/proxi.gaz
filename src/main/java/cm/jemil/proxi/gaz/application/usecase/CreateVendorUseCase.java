/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.VendorOnboardingRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenGenerator;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenHasher;
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.user.User;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import cm.jemil.proxi.gaz.domain.vendor.VendorSession;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
public class CreateVendorUseCase {

  private final VendorOnboardingRepository onboardingRepository;
  private final VendorSessionTokenGenerator tokenGenerator;
  private final VendorSessionTokenHasher tokenHasher;
  private final Clock clock;

  public Response execute(Command command) {
    var createdAt = CreatedAt.from(clock.now());
    var user = User.createVendor(command.email(), createdAt);
    var vendor =
        Vendor.of(
            user.getId(),
            command.name(),
            command.phone(),
            command.location(),
            command.addressText(),
            command.isStation(),
            command.deliveryFeeType(),
            command.deliveryFeeAmount(),
            command.isActive());
    var rawToken = tokenGenerator.generate();
    var session = VendorSession.of(vendor.getId(), tokenHasher.hash(rawToken), createdAt);

    onboardingRepository.insert(user, vendor, session);
    return new Response(vendor.getId(), rawToken.value());
  }

  public record Command(
      @Nullable Email email,
      VendorName name,
      PhoneNumber phone,
      GeoPoint location,
      AddressText addressText,
      Boolean isStation,
      DeliveryFeeType deliveryFeeType,
      Money deliveryFeeAmount,
      Boolean isActive) {}

  public record Response(VendorId vendorId, String sessionToken) {}
}
