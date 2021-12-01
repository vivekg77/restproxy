/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.util;

import com.metrobank.communicationhub.common.model.MobilePushRegistration;
import com.metrobank.communicationhub.model.registration.RegistrationResponse;
import com.metrobank.communicationhub.model.registration.RegistrationResponse.RegistrationDetails;

import java.util.List;
import java.util.stream.Collectors;

/** Description: Utility class to handle common functionality of get registration. */
public class RegistrationResponseUtils {

  private RegistrationResponseUtils() {}

  /**
   * Description: Populates Registration Details Response
   *
   * @param registrations Output from table
   * @param registrationResponse final output for registration
   */
  public static void populateRegistrationDetails(
      List<MobilePushRegistration> registrations, RegistrationResponse registrationResponse) {
    registrationResponse.setRegistrationDetails(
        registrations.stream()
            .map(
                p ->
                    RegistrationDetails.builder()
                        .commsType(p.getCommsType())
                        .deviceToken(p.getDeviceId())
                        .deviceType(p.getDeviceType())
                        .status(p.getStatus())
                        .build())
            .collect(Collectors.toList()));
  }
}
