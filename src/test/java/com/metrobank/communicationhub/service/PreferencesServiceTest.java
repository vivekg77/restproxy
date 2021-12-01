/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.service;

import com.metrobank.communicationhub.common.model.MobilePushPreference;
import com.metrobank.communicationhub.common.model.MobilePushPreference.PreferenceRec;
import com.metrobank.communicationhub.common.repository.MobilePushPreferencesRepository;
import com.metrobank.communicationhub.common.service.CustomerIdIbIdMappingService;
import com.metrobank.communicationhub.model.preferences.PreferencesResponse;
import com.metrobank.communicationhub.model.request.CommsType;
import com.metrobank.communicationhub.model.request.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.metrobank.communicationhub.util.Constant.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreferencesServiceTest {
  @Mock private MobilePushPreferencesRepository mobilePushPreferencesRepository;
  @Mock private CustomerIdIbIdMappingService customerIdIbIdMappingService;

  @InjectMocks private PreferencesService preferencesService;

  @DisplayName("Get preference using IbId")
  @Test
  void getPreferencesForCustomerWithIbId() {
    PreferencesResponse preferences;
    Given:
    {
      when(customerIdIbIdMappingService.getCustomerIdFromIbId(IBID.toString()))
          .thenReturn(Optional.of(CUSTOMER_ID));
      when(mobilePushPreferencesRepository.findAllByCustomerIdAndCommsType(any(), any()))
          .thenReturn(
              List.of(
                  MobilePushPreference.builder()
                      .commsType(CommsType.MOBILE_PUSH.name())
                      .preferences(List.of(PreferenceRec.builder().k("enabled").v("true").build()))
                      .mdInsertDtTm(LocalDateTime.now())
                      .customerId(CUSTOMER_ID)
                      .deviceId(DEVICE_ID)
                      .messageType(MessageType.PAYMENT_SEPA.name())
                      .build()));
    }
    When:
    {
      preferences =
          preferencesService.getPreferencesForCustomer(
              IBID, null, DEVICE_ID, COMMS_TYPE_QUERY_PARAM);
    }
    Then:
    {
      assertNotNull(preferences);
      assertEquals(
          MessageType.PAYMENT_SEPA.name(), preferences.getPreferences().get(0).getMessageType());
      assertEquals("true", preferences.getPreferences().get(0).getCommsPreference().get("enabled"));
    }
  }

  @DisplayName("Get preference using Invalid IbId")
  @Test
  void getPreferencesForCustomerWithInvalidIbId() {
    PreferencesResponse preferences;
    Given:
    {
      when(customerIdIbIdMappingService.getCustomerIdFromIbId(IBID.toString()))
          .thenReturn(Optional.empty());
    }
    When:
    {
      preferences =
          preferencesService.getPreferencesForCustomer(
              IBID, null, DEVICE_ID, COMMS_TYPE_QUERY_PARAM);
    }
    Then:
    {
      assertNotNull(preferences);
      assertNull(preferences.getPreferences());
    }
  }

  @DisplayName("Get preference using CustomerId")
  @Test
  void getPreferencesForCustomerWithCustomerId() {
    PreferencesResponse preferences;
    Given:
    {
      when(mobilePushPreferencesRepository.findAllByCustomerIdAndCommsType(any(), any()))
          .thenReturn(
              List.of(
                  MobilePushPreference.builder()
                      .commsType(CommsType.MOBILE_PUSH.name())
                      .preferences(List.of(PreferenceRec.builder().k("enabled").v("true").build()))
                      .mdInsertDtTm(LocalDateTime.now())
                      .customerId(CUSTOMER_ID)
                      .deviceId(DEVICE_ID)
                      .messageType(MessageType.PAYMENT_SEPA.name())
                      .build()));
    }
    When:
    {
      preferences =
          preferencesService.getPreferencesForCustomer(
              null, CUSTOMER_ID, DEVICE_ID, COMMS_TYPE_QUERY_PARAM);
    }
    Then:
    {
      assertNotNull(preferences);
      assertEquals(
          MessageType.PAYMENT_SEPA.name(), preferences.getPreferences().get(0).getMessageType());
      assertEquals("true", preferences.getPreferences().get(0).getCommsPreference().get("enabled"));
    }
  }
}
