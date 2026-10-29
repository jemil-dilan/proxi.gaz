/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import cm.jemil.proxi.gaz.application.usecase.CreateDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.CreateVendorUseCase;
import cm.jemil.proxi.gaz.application.usecase.LoginAdminUseCase;
import cm.jemil.proxi.gaz.application.usecase.UpdateDemoUseCase.Command;
import cm.jemil.proxi.gaz.application.usecase.UpdateVendorUseCase;
import cm.jemil.proxi.gaz.application.views.DemoView;
import cm.jemil.proxi.gaz.application.views.DemoView.DemoView1;
import cm.jemil.proxi.gaz.application.views.DemoView.DemoView2;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminLoginDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminProfileDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.CreateVendorDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.CreationResponseDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.DemoCreateDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.DemoDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.DemoUpdateDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.LoginResponseDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorOnboardingResponseDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorUpdateDTO;
import java.math.BigDecimal;
import java.util.UUID;
import org.mapstruct.BeanMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface RestMapper {

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "email", source = "email")
  @Mapping(target = "name.value", source = "name")
  @Mapping(target = "phone.value", source = "phone")
  @Mapping(target = "location.latitude", source = "latitude")
  @Mapping(target = "location.longitude", source = "longitude")
  @Mapping(target = "addressText.value", source = "address")
  @Mapping(target = "isStation", source = "isStation")
  @Mapping(target = "deliveryFeeType", source = "deliveryFeeType")
  @Mapping(target = "deliveryFeeAmount", source = "deliveryFeeAmount")
  @Mapping(target = "isActive", source = "isActive")
  CreateVendorUseCase.Command toCommand(CreateVendorDTO createVendorDTO);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "vendorId.value", source = "vendorId")
  @Mapping(target = "name.value", source = "createVendorDTO.name")
  @Mapping(target = "phone.value", source = "createVendorDTO.phone")
  @Mapping(target = "geoPoint.latitude", source = "createVendorDTO.latitude")
  @Mapping(target = "geoPoint.longitude", source = "createVendorDTO.longitude")
  @Mapping(target = "addressText.value", source = "createVendorDTO.addressText")
  @Mapping(target = "isStation", source = "createVendorDTO.isStation")
  @Mapping(target = "deliveryFeeType", source = "createVendorDTO.deliveryFeeType")
  @Mapping(target = "deliveryFeeAmount", source = "createVendorDTO.deliveryFeeAmount")
  @Mapping(target = "isActive", source = "createVendorDTO.isActive")
  UpdateVendorUseCase.Command toCommand(UUID vendorId, VendorUpdateDTO createVendorDTO);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "vendorId", source = "vendorId.value")
  @Mapping(target = "sessionToken", source = "sessionToken")
  VendorOnboardingResponseDTO fromDomain(CreateVendorUseCase.Response response);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "email.value", source = "email")
  @Mapping(target = "password", source = "password")
  LoginAdminUseCase.Command toCommand(AdminLoginDTO request);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "accessToken", source = "accessToken")
  @Mapping(target = "tokenType", expression = "java(LoginResponseDTO.TokenTypeEnum.BEARER)")
  @Mapping(target = "expiresIn", source = "expiresInSeconds")
  LoginResponseDTO fromDomain(LoginAdminUseCase.Response response);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "userId.value")
  @Mapping(target = "email", source = "email.value")
  AdminProfileDTO fromDomain(UserId userId, Email email);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "name")
  @Mapping(target = "description")
  CreateDemoUseCase.Command toCommand(DemoCreateDTO demoCreateDTO);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "newId", source = "value")
  CreationResponseDTO fromDomain(UUID value);

  default CreationResponseDTO fromDomain(UserId value) {
    return fromDomain(value.value());
  }

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "demoId")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "reference", source = "reference")
  DemoDTO fromDomain(DemoView1 value);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "demoId")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "reference", source = "reference")
  @Mapping(target = "description", source = "description")
  DemoDTO fromDomain(DemoView2 value);

  default DemoDTO fromDomain(DemoView demoView) {
    return switch (demoView) {
      case DemoView1 value -> fromDomain(value);
      case DemoView2 value -> fromDomain(value);
      default -> null;
    };
  }

  default Email toEmail(String email) {
    return email == null ? null : Email.from(email);
  }

  default DeliveryFeeType toDeliveryFeeType(String deliveryFeeType) {
    return deliveryFeeType == null ? null : DeliveryFeeType.from(deliveryFeeType);
  }

  default Money toDeliveryFeeAmount(BigDecimal deliveryFeeAmount) {
    return deliveryFeeAmount == null ? null : Money.fcfa(deliveryFeeAmount.intValue());
  }

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "demoId", source = "demoId")
  @Mapping(target = "name", source = "dto.name")
  @Mapping(target = "description", source = "dto.description")
  Command toCommand(UUID demoId, DemoUpdateDTO dto);
}
