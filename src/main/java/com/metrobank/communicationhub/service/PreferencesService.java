/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.service;

import com.metrobank.communicationhub.common.model.MobilePushPreference;
import com.metrobank.communicationhub.common.model.MobilePushPreference.PreferenceRec;
import com.metrobank.communicationhub.common.repository.MobilePushPreferencesRepository;
import com.metrobank.communicationhub.common.service.CustomerIdIbIdMappingService;
import com.metrobank.communicationhub.model.preferences.PreferencesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Log4j2
public class PreferencesService {
  private final MobilePushPreferencesRepository mobilePushPreferencesRepository;
  private final CustomerIdIbIdMappingService customerIdIbIdMappingService;

  public PreferencesResponse getPreferencesForCustomer(
      final String ibId, Integer customerId, final String deviceId, final String commsType) {
    Optional<Integer> customerIdFromIbId;
    // If customerId not present in request get it from the mapping table
    if (customerId == null) {
      customerIdFromIbId = customerIdIbIdMappingService.getCustomerIdFromIbId(ibId);
      if (customerIdFromIbId.isEmpty()) {
        log.error("Invalid IbId: {}", ibId);
        return PreferencesResponse.builder().build();
      } else {
        customerId = customerIdFromIbId.get();
      }
    }

    List<MobilePushPreference> preferenceList =
        mobilePushPreferencesRepository
            .findAllByCustomerIdAndCommsType(customerId, commsType)
            .stream()
            .filter(p -> p.getDeviceId().equals(deviceId))
            .collect(Collectors.toList());
    return PreferencesResponse.builder()
        .preferences(
            preferenceList.stream()
                .map(
                    p ->
                        PreferencesResponse.Preference.builder()
                            .messageType(p.getMessageType())
                            .commsPreference(
                                p.getPreferences().stream()
                                    .collect(toMap(PreferenceRec::getK, PreferenceRec::getV)))
                            .build())
                .collect(Collectors.toList()))
        .build();
  }
}
