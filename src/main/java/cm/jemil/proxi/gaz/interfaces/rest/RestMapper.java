/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import cm.jemil.proxi.gaz.application.usecase.CreateDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.CreateVendorUseCase;
import cm.jemil.proxi.gaz.application.usecase.LoginAdminUseCase;
import cm.jemil.proxi.gaz.application.usecase.LoginAdminUseCase.Response;
import cm.jemil.proxi.gaz.application.usecase.UpdateDemoUseCase.Command;
import cm.jemil.proxi.gaz.application.usecase.UpdateVendorUseCase;
import cm.jemil.proxi.gaz.application.views.DemoView;
import cm.jemil.proxi.gaz.application.views.DemoView.DemoView1;
import cm.jemil.proxi.gaz.application.views.DemoView.DemoView2;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminLoginDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminProfileDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.CreationResponseDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.DemoCreateDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.DemoDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.DemoUpdateDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.LoginResponseDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.VendorDetailsDTO;
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

  default CreateVendorUseCase.Command toCreateVendorCommand(VendorDetailsDTO dto) {
    return new CreateVendorUseCase.Command(
        dto.getName(),
        dto.getPhone(),
        dto.getLatitude(),
        dto.getLongitude(),
        dto.getAddressText(),
        dto.getIsStation(),
        dto.getDeliveryFeeType(),
        dto.getDeliveryFeeAmount(),
        dto.getIsActive());
  }

  default UpdateVendorUseCase.Command toUpdateVendorCommand(UUID vendorId, VendorDetailsDTO dto) {
    return new UpdateVendorUseCase.Command(
        vendorId,
        dto.getName(),
        dto.getPhone(),
        dto.getLatitude(),
        dto.getLongitude(),
        dto.getAddressText(),
        dto.getIsStation(),
        dto.getDeliveryFeeType(),
        dto.getDeliveryFeeAmount(),
        dto.getIsActive());
  }

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "email.value", source = "email")
  @Mapping(target = "password", source = "password")
  LoginAdminUseCase.Command toCommand(AdminLoginDTO request);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "accessToken", source = "accessToken")
  @Mapping(target = "tokenType", expression = "java(LoginResponseDTO.TokenTypeEnum.BEARER)")
  @Mapping(target = "expiresIn", source = "expiresInSeconds")
  LoginResponseDTO fromDomain(Response response);

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

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "demoId", source = "demoId")
  @Mapping(target = "name", source = "dto.name")
  @Mapping(target = "description", source = "dto.description")
  Command toCommand(UUID demoId, DemoUpdateDTO dto);
}
