/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.service;

import com.metrobank.communicationhub.common.model.MobilePushRegistration;
import com.metrobank.communicationhub.common.repository.MobilePushRegistrationRepository;
import com.metrobank.communicationhub.common.service.CustomerIdIbIdMappingService;
import com.metrobank.communicationhub.model.registration.RegistrationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

  private static final String DEVICE_TYPE = "Android";
  private static final String DEVICE_TOKEN = "abd234fe";
  private static final String COMMS_TYPE = "MOBILE_PUSH";
  private static final String INVALID_COMMS_TYPE = "BROADCAST";
  private static final String REGISTRATION_STATUS = "Active";
  private static final Integer CUSTOMER_ID = 12345;
  private static final String IBID = "123456789012";
  MobilePushRegistration registration;
  RegistrationResponse registrationResponse;
  @Mock private MobilePushRegistrationRepository mobilePushRegistrationRepository;
  @Mock private CustomerIdIbIdMappingService customerIdIbIdMappingService;
  @InjectMocks private RegisterService registerService;

  @DisplayName("Successfully retrieved the registration details with Customer Id")
  @Test
  void getRegistrationWithCustomerId() {

    Given:
    {
      registration =
          MobilePushRegistration.builder()
              .commsType(COMMS_TYPE)
              .customerId(CUSTOMER_ID)
              .deviceType(DEVICE_TYPE)
              .deviceId(DEVICE_TOKEN)
              .status(REGISTRATION_STATUS)
              .build();

      List<MobilePushRegistration> registrationDetailList = List.of(registration);

      when(mobilePushRegistrationRepository.findAllByCustomerIdAndCommsTypeAndStatus(
              any(), any(), any()))
          .thenReturn(registrationDetailList);
    }
    When:
    {
      registrationResponse =
          registerService.getRegistration(null, CUSTOMER_ID, DEVICE_TOKEN, DEVICE_TYPE, COMMS_TYPE);
    }
    Then:
    {
      assertNotNull(registrationResponse);
      assertEquals(
          registration.getCommsType(),
          registrationResponse.getRegistrationDetails().get(0).getCommsType());
      assertEquals(
          registration.getStatus(),
          registrationResponse.getRegistrationDetails().get(0).getStatus());
      assertEquals(
          registration.getDeviceId(),
          registrationResponse.getRegistrationDetails().get(0).getDeviceToken());
      assertEquals(
          registration.getDeviceType(),
          registrationResponse.getRegistrationDetails().get(0).getDeviceType());
      assertNull(registrationResponse.getRegistrationDetails().get(0).getMobileNo());
      assertNull(registrationResponse.getRegistrationDetails().get(0).getEmail());
    }
  }

  @DisplayName("Successfully retrieved the registration details with IBId")
  @Test
  void getRegistrationWithIbId() {

    Given:
    {
      Optional<Integer> ibIdCustomerIdMappingOpt = Optional.of(CUSTOMER_ID);
      when(customerIdIbIdMappingService.getCustomerIdFromIbId(IBID.toString()))
          .thenReturn(ibIdCustomerIdMappingOpt);

      registration =
          MobilePushRegistration.builder()
              .customerId(ibIdCustomerIdMappingOpt.get())
              .commsType(COMMS_TYPE)
              .deviceId(DEVICE_TOKEN)
              .deviceType(DEVICE_TYPE)
              .status(REGISTRATION_STATUS)
              .build();
      List<MobilePushRegistration> registrationDetailList = List.of(registration);
      when(mobilePushRegistrationRepository.findAllByCustomerIdAndCommsTypeAndStatus(
              any(), any(), any()))
          .thenReturn(registrationDetailList);
    }
    When:
    {
      registrationResponse =
          registerService.getRegistration(IBID, null, DEVICE_TOKEN, DEVICE_TYPE, COMMS_TYPE);
    }
    Then:
    {
      assertNotNull(registrationResponse.getRegistrationDetails());
      assertEquals(
          registration.getStatus(),
          registrationResponse.getRegistrationDetails().get(0).getStatus());
    }
  }

  @DisplayName("Successfully retrieved the registration details with Customer Id & IBID")
  @Test
  void getRegistrationWithCustomerIdAndIbId() {

    Given:
    {
      registration =
          MobilePushRegistration.builder()
              .customerId(CUSTOMER_ID)
              .deviceType(DEVICE_TYPE)
              .deviceId(DEVICE_TOKEN)
              .commsType(COMMS_TYPE)
              .status(REGISTRATION_STATUS)
              .build();

      List<MobilePushRegistration> registrationDetailList = List.of(registration);
      when(mobilePushRegistrationRepository.findAllByCustomerIdAndCommsTypeAndStatus(
              any(), any(), any()))
          .thenReturn(registrationDetailList);
    }
    When:
    {
      registrationResponse =
          registerService.getRegistration(IBID, CUSTOMER_ID, DEVICE_TOKEN, DEVICE_TYPE, COMMS_TYPE);
    }
    Then:
    {
      assertNotNull(registrationResponse.getRegistrationDetails());
      assertEquals(
          registration.getStatus(),
          registrationResponse.getRegistrationDetails().get(0).getStatus());
    }
  }

  @DisplayName("Failed to retrieve the registration")
  @Test
  void getRegistrationWithoutCustomerId() {

    Given:
    {
      registration =
          MobilePushRegistration.builder()
              .customerId(CUSTOMER_ID)
              .deviceId(DEVICE_TOKEN)
              .deviceType(DEVICE_TYPE)
              .commsType(COMMS_TYPE)
              .build();
    }
    When:
    {
      registrationResponse =
          registerService.getRegistration(IBID, null, DEVICE_TOKEN, DEVICE_TYPE, COMMS_TYPE);
    }
    Then:
    {
      assertNull(registrationResponse.getRegistrationDetails());
    }
  }

  @DisplayName("Failed retrieving the registration - Device Token doesn't match")
  @Test
  void getRegistrationInvalidDeviceToken() {
    Given:
    {
      registration =
          MobilePushRegistration.builder()
              .customerId(CUSTOMER_ID)
              .deviceType(DEVICE_TYPE)
              .deviceId(DEVICE_TOKEN)
              .commsType(COMMS_TYPE)
              .status(REGISTRATION_STATUS)
              .build();

      List<MobilePushRegistration> registrationDetailList = List.of(registration);
      when(mobilePushRegistrationRepository.findAllByCustomerIdAndCommsTypeAndStatus(
              any(), any(), any()))
          .thenReturn(registrationDetailList);
    }
    When:
    {
      registrationResponse =
          registerService.getRegistration(null, CUSTOMER_ID, "1234", DEVICE_TYPE, COMMS_TYPE);
    }
    Then:
    {
      assertEquals(0, registrationResponse.getRegistrationDetails().size());
    }
  }

  @DisplayName("Failed retrieving the registration - Device Type doesn't match")
  @Test
  void getRegistrationInvalidDeviceType() {
    Given:
    {
      registration =
          MobilePushRegistration.builder()
              .customerId(CUSTOMER_ID)
              .deviceType(DEVICE_TYPE)
              .deviceId(DEVICE_TOKEN)
              .commsType(COMMS_TYPE)
              .status(REGISTRATION_STATUS)
              .build();

      List<MobilePushRegistration> registrationDetailList = List.of(registration);
      when(mobilePushRegistrationRepository.findAllByCustomerIdAndCommsTypeAndStatus(
              any(), any(), any()))
          .thenReturn(registrationDetailList);
    }
    When:
    {
      registrationResponse =
          registerService.getRegistration(null, CUSTOMER_ID, DEVICE_TOKEN, "IOS", COMMS_TYPE);
    }
    Then:
    {
      assertEquals(0, registrationResponse.getRegistrationDetails().size());
    }
  }

  @DisplayName("Failed retrieving the registration with invalid communication type")
  @Test
  void getRegistrationWithInvalidCommType() {

    Given:
    {
      registration =
          MobilePushRegistration.builder()
              .customerId(CUSTOMER_ID)
              .deviceType(DEVICE_TYPE)
              .deviceId(DEVICE_TOKEN)
              .commsType(COMMS_TYPE)
              .build();
    }
    When:
    {
      registrationResponse =
          registerService.getRegistration(
              null, CUSTOMER_ID, DEVICE_TOKEN, DEVICE_TYPE, INVALID_COMMS_TYPE);
    }
    Then:
    {
      assertNull(registrationResponse.getRegistrationDetails());
    }
  }
}
