/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.service;

import com.metrobank.communicationhub.common.service.CustomerIdIbIdMappingService;
import com.metrobank.communicationhub.exception.RestProxyUnprocessableEntityException;
import com.metrobank.communicationhub.schema.CommsPreference;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Optional;

import static com.metrobank.communicationhub.util.Constant.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestProxyServiceTest {
  @Mock private KafkaTemplate kafkaTemplate;
  @Mock private CustomerIdIbIdMappingService customerIdIbIdMappingService;
  @InjectMocks private RestProxyService restProxyService;
  private String correlationId;

  @BeforeEach
  void setUp() {
    this.restProxyService = new RestProxyService(kafkaTemplate, customerIdIbIdMappingService);
    this.restProxyService.preferenceTopic = "preferenceTopic";
    this.restProxyService.registerTopic = "registrationTopic";
    this.restProxyService.sendTopic = "sendTopic";
  }

  @DisplayName("Register Success")
  @Test
  void sendRegisterRequest_Created() {
    ResponseEntity responseEntity;
    String uuid;
    Given:
    {
      uuid = "1234567";
      ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
      Mockito.when(kafkaTemplate.send((ProducerRecord) any())).thenReturn(future);

      SendResult<String, Object> sendResult = mock(SendResult.class);
    }
    When:
    {
      responseEntity =
          restProxyService.sendRegisterRequest(
              IBID, CUSTOMER_ID, REGISTRATION_REQUEST, uuid, HttpMethod.POST);
    }
    Then:
    {
      assertNotNull(responseEntity);
      assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }
  }

  @DisplayName("Register Success when CustomerId is retrieved from IBID")
  @Test
  void sendRegisterRequestWithCustomerIdMissing_Created() {
    ResponseEntity responseEntity;
    String uuid;
    Given:
    {
      uuid = "1234567";
      ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
      Mockito.when(kafkaTemplate.send((ProducerRecord) any())).thenReturn(future);
      SendResult<String, Object> sendResult = mock(SendResult.class);
      RecordMetadata recordMetadata =
          new RecordMetadata(new TopicPartition("REGISTER", 0), 0, 0L, 0L, 0L, 0, 0);
      given(sendResult.getRecordMetadata()).willReturn(recordMetadata);

      Optional<Integer> ibIdCustomerIdMappingOpt = Optional.of(CUSTOMER_ID);
      when(customerIdIbIdMappingService.getCustomerIdFromIbId(IBID.toString()))
          .thenReturn(ibIdCustomerIdMappingOpt);
      doAnswer(
              invocationOnMock -> {
                ListenableFutureCallback listenableFutureCallback = invocationOnMock.getArgument(0);
                listenableFutureCallback.onSuccess(sendResult);
                assertEquals(0, sendResult.getRecordMetadata().offset());
                assertEquals(0, sendResult.getRecordMetadata().partition());
                return null;
              })
          .when(future)
          .addCallback(any(ListenableFutureCallback.class));
    }
    When:
    {
      responseEntity =
          restProxyService.sendRegisterRequest(
              IBID, null, REGISTRATION_REQUEST, uuid, HttpMethod.POST);
    }
    Then:
    {
      assertNotNull(responseEntity);
      assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }
  }

  @DisplayName("Register Failed")
  @Test
  void sendRegisterRequest_Failed() {
    ResponseEntity responseEntity;
    String uuid;
    Given:
    {
      uuid = "1234567";
      ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
      Mockito.when(kafkaTemplate.send((ProducerRecord) any())).thenReturn(future);
      SendResult<String, Object> sendResult = mock(SendResult.class);
      doAnswer(
              invocationOnMock -> {
                ListenableFutureCallback listenableFutureCallback = invocationOnMock.getArgument(0);
                listenableFutureCallback.onFailure(new Exception());
                return null;
              })
          .when(future)
          .addCallback(any(ListenableFutureCallback.class));
    }
    When:
    {
      responseEntity =
          restProxyService.sendRegisterRequest(
              IBID, CUSTOMER_ID, REGISTRATION_REQUEST, uuid, HttpMethod.POST);
    }
    Then:
    {
      assertNotNull(responseEntity);
      assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }
  }

  @DisplayName("Register Failed when invalid IBID passed and CustomerId missing")
  @Test
  void sendRegisterRequestInvalidIbId_Failed() {
    String uuid = "1234567";
    Assertions.assertThrows(
        RestProxyUnprocessableEntityException.class,
        () -> {
          restProxyService.sendRegisterRequest(
              IBID, null, REGISTRATION_REQUEST, uuid, HttpMethod.POST);
        });
  }

  @DisplayName("Registration with null IBID Failed")
  @Test
  void sendRegisterRequestWithNullIBID_Failed() {
    ResponseEntity responseEntity;
    String uuid;
    Given:
    {
      uuid = "1234567";
      ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
      Mockito.when(kafkaTemplate.send((ProducerRecord) any())).thenReturn(future);
      SendResult<String, Object> sendResult = mock(SendResult.class);
      doAnswer(
              invocationOnMock -> {
                ListenableFutureCallback listenableFutureCallback = invocationOnMock.getArgument(0);
                listenableFutureCallback.onFailure(new Exception());
                return null;
              })
          .when(future)
          .addCallback(any(ListenableFutureCallback.class));
    }
    When:
    {
      responseEntity =
          restProxyService.sendRegisterRequest(
              null, CUSTOMER_ID, REGISTRATION_REQUEST, uuid, HttpMethod.POST);
    }
    Then:
    {
      assertNotNull(responseEntity);
      assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }
  }

  @DisplayName("Delete Register/De-Registration Success")
  @Test
  void deRegisterRequest_Success() {
    ResponseEntity responseEntity;
    String uuid;
    Given:
    {
      uuid = "1234567";
      ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
      Mockito.when(kafkaTemplate.send((ProducerRecord) any())).thenReturn(future);
      SendResult<String, Object> sendResult = mock(SendResult.class);
      RecordMetadata recordMetadata =
          new RecordMetadata(new TopicPartition("REGISTER", 0), 0, 0L, 0L, 0L, 0, 0);
      given(sendResult.getRecordMetadata()).willReturn(recordMetadata);
      doAnswer(
              invocationOnMock -> {
                ListenableFutureCallback listenableFutureCallback = invocationOnMock.getArgument(0);
                listenableFutureCallback.onSuccess(sendResult);
                assertEquals(0, sendResult.getRecordMetadata().offset());
                assertEquals(0, sendResult.getRecordMetadata().partition());
                return null;
              })
          .when(future)
          .addCallback(any(ListenableFutureCallback.class));
    }
    When:
    {
      responseEntity =
          restProxyService.sendRegisterRequest(
              IBID, CUSTOMER_ID, REGISTRATION_REQUEST, uuid, HttpMethod.DELETE);
    }
    Then:
    {
      assertNotNull(responseEntity);
      assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }
  }

  @DisplayName("Delete Register/De-Registration Failed")
  @Test
  void deRegisterRequest_Failed() {
    ResponseEntity responseEntity;
    String uuid;
    Given:
    {
      uuid = "1234567";
      ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
      Mockito.when(kafkaTemplate.send((ProducerRecord) any())).thenReturn(future);
      SendResult<String, Object> sendResult = mock(SendResult.class);
      doAnswer(
              invocationOnMock -> {
                ListenableFutureCallback listenableFutureCallback = invocationOnMock.getArgument(0);
                listenableFutureCallback.onFailure(new Exception());
                return null;
              })
          .when(future)
          .addCallback(any(ListenableFutureCallback.class));
    }
    When:
    {
      responseEntity =
          restProxyService.sendRegisterRequest(
              IBID, CUSTOMER_ID, REGISTRATION_REQUEST, uuid, HttpMethod.DELETE);
    }
    Then:
    {
      assertNotNull(responseEntity);
      assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }
  }

  @DisplayName("Delete Register/De-Registration with null IBID Failed")
  @Test
  void deRegisterRequestWithNullIBID_Failed() {
    ResponseEntity responseEntity;
    String uuid;
    Given:
    {
      uuid = "1234567";
      ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
      Mockito.when(kafkaTemplate.send((ProducerRecord) any())).thenReturn(future);
      SendResult<String, Object> sendResult = mock(SendResult.class);
      doAnswer(
              invocationOnMock -> {
                ListenableFutureCallback listenableFutureCallback = invocationOnMock.getArgument(0);
                listenableFutureCallback.onFailure(new Exception());
                return null;
              })
          .when(future)
          .addCallback(any(ListenableFutureCallback.class));
    }
    When:
    {
      responseEntity =
          restProxyService.sendRegisterRequest(
              null, CUSTOMER_ID, REGISTRATION_REQUEST, uuid, HttpMethod.DELETE);
    }
    Then:
    {
      assertNotNull(responseEntity);
      assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }
  }

  @DisplayName("Send Success")
  @Test
  void sendCommunicateRequest_Success() {
    ResponseEntity responseEntity;
    String uuid;
    Given:
    {
      uuid = "1234567";
      ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
      Mockito.when(kafkaTemplate.send((ProducerRecord) any())).thenReturn(future);
      SendResult<String, Object> sendResult = mock(SendResult.class);
      RecordMetadata recordMetadata =
          new RecordMetadata(new TopicPartition("SEND", 0), 0, 0L, 0L, 0L, 0, 0);
      given(sendResult.getRecordMetadata()).willReturn(recordMetadata);
      doAnswer(
              invocationOnMock -> {
                ListenableFutureCallback listenableFutureCallback = invocationOnMock.getArgument(0);
                listenableFutureCallback.onSuccess(sendResult);
                assertEquals(0, sendResult.getRecordMetadata().offset());
                assertEquals(0, sendResult.getRecordMetadata().partition());
                return null;
              })
          .when(future)
          .addCallback(any(ListenableFutureCallback.class));
    }
    When:
    {
      responseEntity =
          restProxyService.sendCommunicateRequest(IBID, CUSTOMER_ID, SEND_REQUEST, uuid);
    }
    Then:
    {
      assertNotNull(responseEntity);
      assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
  }

  @DisplayName("Send Success when CustomerId is retrieved from IBID")
  @Test
  void sendCommunicateWithCustomerIdMissing_Success() {
    ResponseEntity responseEntity;
    String uuid;
    Given:
    {
      uuid = "1234567";
      ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
      Mockito.when(kafkaTemplate.send((ProducerRecord) any())).thenReturn(future);
      SendResult<String, Object> sendResult = mock(SendResult.class);
      RecordMetadata recordMetadata =
          new RecordMetadata(new TopicPartition("SEND", 0), 0, 0L, 0L, 0L, 0, 0);
      given(sendResult.getRecordMetadata()).willReturn(recordMetadata);

      Optional<Integer> ibIdCustomerIdMappingOpt = Optional.of(CUSTOMER_ID);
      when(customerIdIbIdMappingService.getCustomerIdFromIbId(IBID.toString()))
          .thenReturn(ibIdCustomerIdMappingOpt);
      doAnswer(
              invocationOnMock -> {
                ListenableFutureCallback listenableFutureCallback = invocationOnMock.getArgument(0);
                listenableFutureCallback.onSuccess(sendResult);
                assertEquals(0, sendResult.getRecordMetadata().offset());
                assertEquals(0, sendResult.getRecordMetadata().partition());
                return null;
              })
          .when(future)
          .addCallback(any(ListenableFutureCallback.class));
    }
    When:
    {
      responseEntity = restProxyService.sendCommunicateRequest(IBID, null, SEND_REQUEST, uuid);
    }
    Then:
    {
      assertNotNull(responseEntity);
      assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
  }

  @DisplayName("Send Failed")
  @Test
  void sendCommunicateRequest_Failed() {
    ResponseEntity responseEntity;
    String uuid;
    Given:
    {
      uuid = "1234567";
      ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
      Mockito.when(kafkaTemplate.send((ProducerRecord) any())).thenReturn(future);
      doAnswer(
              invocationOnMock -> {
                ListenableFutureCallback listenableFutureCallback = invocationOnMock.getArgument(0);
                listenableFutureCallback.onFailure(new Exception());
                return null;
              })
          .when(future)
          .addCallback(any(ListenableFutureCallback.class));
    }
    When:
    {
      responseEntity =
          restProxyService.sendCommunicateRequest(IBID, CUSTOMER_ID, SEND_REQUEST, uuid);
    }
    Then:
    {
      assertNotNull(responseEntity);
      assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
  }

  @DisplayName("Save Preference Success")
  @Test
  void savePreferenceRequest_Success() throws Exception {
    ResponseEntity responseEntity;
    String uuid;
    Given:
    {
      uuid = "1234567";
      ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
      Mockito.when(kafkaTemplate.send((ProducerRecord) any())).thenReturn(future);
      SendResult<String, Object> sendResult = mock(SendResult.class);
      RecordMetadata recordMetadata =
          new RecordMetadata(new TopicPartition("topic", 0), 0, 0L, 0L, 0L, 0, 0);
      given(sendResult.getRecordMetadata()).willReturn(recordMetadata);
      doAnswer(
              invocationOnMock -> {
                ListenableFutureCallback listenableFutureCallback = invocationOnMock.getArgument(0);
                listenableFutureCallback.onSuccess(sendResult);
                assertEquals(0, sendResult.getRecordMetadata().offset());
                assertEquals(0, sendResult.getRecordMetadata().partition());
                return null;
              })
          .when(future)
          .addCallback(any(ListenableFutureCallback.class));
    }
    When:
    {
      responseEntity =
          restProxyService.sendPreferenceRequest(IBID, CUSTOMER_ID, PREFERENCE_REQUEST, uuid);
    }
    Then:
    {
      assertNotNull(responseEntity);
      assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
  }

  @DisplayName("Save Preference Success when CustomerId is retrieved from IBID")
  @Test
  void savePreferenceWithCustomerIdMissing_Success() throws Exception {
    ResponseEntity responseEntity;
    String uuid;
    Given:
    {
      uuid = "1234567";
      RequestReplyFuture<String, CommsPreference, CommsPreference> future =
          mock(RequestReplyFuture.class);
      ConsumerRecord<String, CommsPreference> consumerRecord =
          new ConsumerRecord<>("topic", 0, 123L, uuid, COMMS_PREFERENCE);
      Mockito.when(kafkaTemplate.send((ProducerRecord) any())).thenReturn(future);
      Optional<Integer> ibIdCustomerIdMappingOpt = Optional.of(CUSTOMER_ID);

      when(customerIdIbIdMappingService.getCustomerIdFromIbId(IBID.toString()))
          .thenReturn(ibIdCustomerIdMappingOpt);
    }
    When:
    {
      responseEntity = restProxyService.sendPreferenceRequest(IBID, null, PREFERENCE_REQUEST, uuid);
    }
    Then:
    {
      assertNotNull(responseEntity);
      assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
  }

  @DisplayName("Save Preference Failed")
  @Test
  void savePreferenceRequest_Failed() {
    ResponseEntity responseEntity;
    String uuid;
    Given:
    {
      uuid = "1234567";
      ListenableFuture<SendResult<String, Object>> future = mock(ListenableFuture.class);
      Mockito.when(kafkaTemplate.send((ProducerRecord) any())).thenReturn(future);
      doAnswer(
              invocationOnMock -> {
                ListenableFutureCallback listenableFutureCallback = invocationOnMock.getArgument(0);
                listenableFutureCallback.onFailure(new Exception());
                return null;
              })
          .when(future)
          .addCallback(any(ListenableFutureCallback.class));
    }
    When:
    {
      responseEntity =
          restProxyService.sendPreferenceRequest(IBID, CUSTOMER_ID, PREFERENCE_REQUEST, uuid);
    }
    Then:
    {
      assertNotNull(responseEntity);
      assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
  }
}
