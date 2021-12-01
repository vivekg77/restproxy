/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.service;

import com.metrobank.communicationhub.common.model.MobilePushRegistration;
import com.metrobank.communicationhub.common.repository.MobilePushRegistrationRepository;
import com.metrobank.communicationhub.common.service.CustomerIdIbIdMappingService;
import com.metrobank.communicationhub.model.registration.RegistrationResponse;
import com.metrobank.communicationhub.model.request.CommsType;
import com.metrobank.communicationhub.util.RegistrationResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.metrobank.communicationhub.util.CommsHubConstants.REGISTRATION_STATUS;

@Service
@Log4j2
@RequiredArgsConstructor
public class RegisterService implements Register {

  private final MobilePushRegistrationRepository mobilePushRegistrationRepository;
  private final CustomerIdIbIdMappingService customerIdIbIdMappingService;

  public RegistrationResponse getRegistration(
      final String ibId,
      Integer customerId,
      final String deviceToken,
      final String deviceType,
      final String commsType) {
    final RegistrationResponse registrationResponse;
    log.info(
        "RegistrationService :: CustomerId: {}, IBID : {}, DeviceToken: {}, DeviceType: {}, CommsType: {}",
        customerId,
        ibId,
        deviceToken,
        deviceType,
        commsType);

    registrationResponse = RegistrationResponse.builder().build();
    Optional<Integer> customerIdFromIbId;

    // If customerId not present in request get it from the mapping table
    if (customerId == null) {
      customerIdFromIbId = customerIdIbIdMappingService.getCustomerIdFromIbId(ibId);
      if (customerIdFromIbId.isEmpty()) {
        log.error("Invalid IbId: {}", ibId);
        return registrationResponse;
      } else {
        customerId = customerIdFromIbId.get();
      }
    }

    if (commsType.equals(CommsType.MOBILE_PUSH.toString())) {
      List<MobilePushRegistration> registrations =
          mobilePushRegistrationRepository
              .findAllByCustomerIdAndCommsTypeAndStatus(customerId, commsType, REGISTRATION_STATUS)
              .stream()
              .filter(
                  p -> p.getDeviceId().equals(deviceToken) && p.getDeviceType().equals(deviceType))
              .collect(Collectors.toList());

      RegistrationResponseUtils.populateRegistrationDetails(registrations, registrationResponse);
    }
    return registrationResponse;
  }
}
