/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metrobank.communicationhub.exception.RestProxyException;
import com.metrobank.communicationhub.model.preferences.PreferencesResponse;
import com.metrobank.communicationhub.model.registration.RegistrationResponse;
import com.metrobank.communicationhub.model.request.CommsType;
import com.metrobank.communicationhub.model.request.MessageType;
import com.metrobank.communicationhub.service.PreferencesService;
import com.metrobank.communicationhub.service.RegisterService;
import com.metrobank.communicationhub.service.RestProxyService;
import com.mongodb.MongoExecutionTimeoutException;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.requestreply.KafkaReplyTimeoutException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Map;

import static com.metrobank.communicationhub.util.Constant.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@WebMvcTest(RestProxyController.class)
class RestProxyControllerTest {

  protected ResponseEntity result;
  @MockBean private RestProxyService restProxyService;
  @MockBean private RegisterService registerService;
  @MockBean private PreferencesService preferencesService;
  @Mock private RegistrationResponse registrationResponse;
  @Mock private PreferencesResponse preferencesResponse;
  @InjectMocks private RestProxyController restProxyController;

  @Autowired private MockMvc mockMvc;

  @DisplayName("Create Registration :: Success Request")
  @Test
  void testRegisterRequest_validRequest() throws Exception {
    when(restProxyService.sendRegisterRequest(any(), any(), any(), any(), any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(IBID_HEADER, IBID)
                .content(new ObjectMapper().writeValueAsString(REGISTRATION_REQUEST)))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().isCreated());
  }

  @DisplayName("Create Preference :: Success Request")
  @Test
  void testPreferenceRequest_validRequest() throws Exception {
    when(restProxyService.sendPreferenceRequest(any(), any(), any(), any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(PREFERENCE_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(IBID_HEADER, IBID)
                .content(new ObjectMapper().writeValueAsString(PREFERENCE_REQUEST)))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().isCreated());
  }

  @DisplayName("Create Preference :: TimeOut")
  @Test
  void testPreferenceRequest_timeout() throws Exception {
    when(restProxyService.sendPreferenceRequest(any(), any(), any(), any()))
        .thenThrow(new KafkaReplyTimeoutException("Response timed out"));
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(PREFERENCE_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(IBID_HEADER, IBID)
                .content(new ObjectMapper().writeValueAsString(PREFERENCE_REQUEST)))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.jsonPath("$.code", Is.is("Response Timeout")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.message", Is.is("Response timed out")));
  }

  @DisplayName("Create Preference :: Exception")
  @Test
  void testPreferenceRequest_exception() throws Exception {
    when(restProxyService.sendPreferenceRequest(any(), any(), any(), any()))
        .thenThrow(new RestProxyException(new Throwable()));
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(PREFERENCE_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(IBID_HEADER, IBID)
                .content(new ObjectMapper().writeValueAsString(PREFERENCE_REQUEST)))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().is5xxServerError())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.jsonPath("$.code", Is.is("Server Error")))
        .andExpect(
            MockMvcResultMatchers.jsonPath(
                "$.message", Is.is("Internal server error, please try again")));
  }

  @DisplayName("Create Registration :: Failure")
  @Test
  void testRegisterRequest_MissingCustomerIdAndIbId() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(ACCEPT, MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(REGISTRATION_REQUEST_NULL)))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.jsonPath("$.code", Is.is("Invalid field")));
  }

  @DisplayName("Create Registration-> Missing Header CustomerID & IBID  :: Failure")
  @Test
  void testRegisterRequest_InvalidRequest() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(ACCEPT, MediaType.APPLICATION_JSON)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(IBID_HEADER, IBID)
                .content(new ObjectMapper().writeValueAsString(REGISTRATION_REQUEST_NULL)))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.jsonPath("$.code", Is.is("Invalid field")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.message", Is.is("Missing Data")));
  }

  @DisplayName("Create Preference :: Failure")
  @Test
  void testPreferenceRequest_InvalidRequest() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(PREFERENCE_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(ACCEPT, MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(PREFERENCE_REQUEST_NULL)))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.jsonPath("$.code", Is.is("Invalid field")));
  }

  @DisplayName("Send Communication :: Success")
  @Test
  void testSend_validRequest() throws Exception {
    when(restProxyService.sendCommunicateRequest(any(), any(), any(), any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(SEND_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(IBID_HEADER, IBID)
                .content(new ObjectMapper().writeValueAsString(SEND_REQUEST)))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().isOk());
  }

  @DisplayName("Send Communication :: Failure")
  @Test
  void testSendRequest_InvalidRequest() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(SEND_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(ACCEPT, MediaType.APPLICATION_JSON)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(IBID_HEADER, IBID)
                .content(new ObjectMapper().writeValueAsString(NULL_SEND_REQUEST)))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.jsonPath("$.code", Is.is("Invalid field")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.message", Is.is("Missing Data")));
  }

  @DisplayName("Send Communication-> Missing Header CustomerID & IBID  :: Failure")
  @Test
  void testSendRequest_MissingCustomerIdAndIbId() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(SEND_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(ACCEPT, MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(NULL_SEND_REQUEST)))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.jsonPath("$.code", Is.is("Invalid field")));
  }

  @DisplayName("Delete Registration / De-Registration :: Success")
  @Test
  void testDeRegisterRequest_validRequest() throws Exception {
    when(restProxyService.sendRegisterRequest(any(), any(), any(), any(), any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(IBID_HEADER, IBID)
                .content(new ObjectMapper().writeValueAsString(REGISTRATION_REQUEST)))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().isCreated());
  }

  @DisplayName("Delete Registration / De-Registration :: Failure")
  @Test
  void testDeRegisterRequest_InvalidRequest() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(ACCEPT, MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(REGISTRATION_REQUEST_NULL)))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(MockMvcResultMatchers.jsonPath("$.code", Is.is("Invalid field")));
  }

  @DisplayName("Get Registration-> Missing Mandatory Header DeviceId :: Failure")
  @Test
  void testGetRegistrationRequest_missingDeviceIdHeader() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(REGISTER_URL)
                .header(IBID_HEADER, IBID)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(DEVICE_TYPE_HEADER, DEVICE_TYPE)
                .queryParam(COMMS_TYPE_QUERY_PARAM, CommsType.MOBILE_PUSH.toString())
                .content(new ObjectMapper().writeValueAsString("")))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @DisplayName("Get Registration-> Missing Mandatory Header Device Type :: Failure")
  @Test
  void testGetRegistrationRequest_missingDeviceTypeHeader() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(REGISTER_URL)
                .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(IBID_HEADER, IBID)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(DEVICE_ID_HEADER, DEVICE_ID)
                .queryParam(COMMS_TYPE_QUERY_PARAM, CommsType.MOBILE_PUSH.toString())
                .content(new ObjectMapper().writeValueAsString("")))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @DisplayName("Get Registration-> Missing Mandatory Headers CustomerID & IBID :: Failure")
  @Test
  void testGetRegistrationRequest_missingCustomerIdAndIbIdHeaders() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(REGISTER_URL)
                .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(DEVICE_ID_HEADER, DEVICE_ID)
                .header(DEVICE_TYPE_HEADER, DEVICE_TYPE)
                .queryParam(COMMS_TYPE_QUERY_PARAM, CommsType.MOBILE_PUSH.toString())
                .content(new ObjectMapper().writeValueAsString("")))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @DisplayName("Get Registration-> Argument Exception :: Failure")
  @Test
  void testGetRegistrationRequest_ArgumentException() throws Exception {
    when(registerService.getRegistration(any(), any(), any(), any(), any()))
        .thenThrow(new IllegalArgumentException("Unexpected Exception"));
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(REGISTER_URL)
                .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(IBID_HEADER, IBID)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(DEVICE_ID_HEADER, DEVICE_ID)
                .header(DEVICE_TYPE_HEADER, DEVICE_TYPE)
                .queryParam(COMMS_TYPE_QUERY_PARAM, CommsType.MOBILE_PUSH.toString())
                .content(new ObjectMapper().writeValueAsString("")))
        .andDo(print())
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("$.code", Is.is("Invalid field")));
  }

  @DisplayName("Get Registration-> CassandraReadTimeout Exception :: Failure")
  @Test
  void testGetRegistrationRequest_TimeoutException() throws Exception {
    when(registerService.getRegistration(any(), any(), any(), any(), any()))
        .thenThrow(new MongoExecutionTimeoutException(500, "timedout"));
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(REGISTER_URL)
                .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(IBID_HEADER, IBID)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(DEVICE_ID_HEADER, DEVICE_ID)
                .header(DEVICE_TYPE_HEADER, DEVICE_TYPE)
                .queryParam(COMMS_TYPE_QUERY_PARAM, CommsType.MOBILE_PUSH.toString())
                .content(new ObjectMapper().writeValueAsString("")))
        .andDo(print())
        .andExpect(status().isRequestTimeout())
        .andExpect(jsonPath("$.code", Is.is("Response Timeout")));
  }

  @DisplayName("Get Preference :: Success")
  @Test
  void testGetPreferenceRequest_validRequest() throws Exception {
    preferencesResponse =
        PreferencesResponse.builder()
            .preferences(
                List.of(
                    PreferencesResponse.Preference.builder()
                        .messageType(MessageType.PAYMENT_SEPA.name())
                        .commsPreference(Map.of("enabled", "true"))
                        .build()))
            .build();

    when(preferencesService.getPreferencesForCustomer(any(), any(), any(), any()))
        .thenReturn(preferencesResponse);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(PREFERENCE_URL)
                .header(IBID_HEADER, IBID)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(DEVICE_ID_HEADER, DEVICE_ID)
                .queryParam(COMMS_TYPE_QUERY_PARAM, CommsType.MOBILE_PUSH.toString())
                .content(new ObjectMapper().writeValueAsString("")))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(content().string(RETRIEVE_PREFERENCE_RESPONSE));
  }

  @DisplayName("Get Preference-> Missing Header DeviceId :: Failure")
  @Test
  void testGetPreferenceRequest_missingDeviceIdHeader() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(PREFERENCE_URL)
                .header(IBID_HEADER, IBID)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .queryParam(COMMS_TYPE_QUERY_PARAM, CommsType.MOBILE_PUSH.toString())
                .content(new ObjectMapper().writeValueAsString("")))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @DisplayName("Get Preference-> Missing Param CommsType :: Failure")
  @Test
  void testGetPreferenceRequest_missingCommsType() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(PREFERENCE_URL)
                .header(IBID_HEADER, IBID)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(DEVICE_ID_HEADER, DEVICE_ID)
                .content(new ObjectMapper().writeValueAsString("")))
        .andDo(print())
        .andExpect(status().is4xxClientError());
  }

  @DisplayName("Get Preference-> Missing Mandatory Header CustomerAndIbId :: Failure")
  @Test
  void testGetPreferenceRequest_missingCustomerAndIbIdHeader() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(PREFERENCE_URL)
                .queryParam(COMMS_TYPE_QUERY_PARAM, CommsType.MOBILE_PUSH.toString())
                .content(new ObjectMapper().writeValueAsString("")))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @DisplayName("Get Preference-> Missing ibId :: Failure")
  @Test
  void testGetPreferenceRequest_missingIbId() throws Exception {
    preferencesResponse =
        PreferencesResponse.builder()
            .preferences(
                List.of(
                    PreferencesResponse.Preference.builder()
                        .messageType(MessageType.PAYMENT_SEPA.name())
                        .commsPreference(Map.of("enabled", "true"))
                        .build()))
            .build();

    when(preferencesService.getPreferencesForCustomer(any(), any(), any(), any()))
        .thenReturn(preferencesResponse);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(PREFERENCE_URL)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(DEVICE_ID_HEADER, DEVICE_ID)
                .queryParam(COMMS_TYPE_QUERY_PARAM, CommsType.MOBILE_PUSH.toString())
                .content(new ObjectMapper().writeValueAsString("")))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(content().string(RETRIEVE_PREFERENCE_RESPONSE));
  }

  @DisplayName("Get Preference-> Missing customerId :: Failure")
  @Test
  void testGetPreferenceRequest_missingCustomerId() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(PREFERENCE_URL)
                .header(DEVICE_ID_HEADER, DEVICE_ID)
                .queryParam(COMMS_TYPE_QUERY_PARAM, CommsType.MOBILE_PUSH.toString())
                .content(new ObjectMapper().writeValueAsString("")))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @DisplayName("Get Preference-> Invalid Comms Type :: Failure")
  @Test
  void testGetPreferenceRequest_invalidCommsType() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(PREFERENCE_URL)
                .header(IBID_HEADER, IBID)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .queryParam(COMMS_TYPE_QUERY_PARAM, "INVALID")
                .content(new ObjectMapper().writeValueAsString("")))
        .andDo(print())
        .andExpect(status().is4xxClientError());
  }

  @DisplayName("Get Registration :: Invalid Comms Type :: Failure")
  @Test
  void testGetRegistrationRequest_invalidCommsType() throws Exception {

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(REGISTER_URL)
                .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(IBID_HEADER, IBID)
                .header(CUSTOMER_ID_HEADER, CUSTOMER_ID)
                .header(DEVICE_ID_HEADER, DEVICE_ID)
                .header(DEVICE_TYPE_HEADER, DEVICE_TYPE)
                .queryParam(COMMS_TYPE_QUERY_PARAM, "INVALID")
                .content(new ObjectMapper().writeValueAsString("")))
        .andDo(print())
        .andExpect(status().is4xxClientError());
  }
}
