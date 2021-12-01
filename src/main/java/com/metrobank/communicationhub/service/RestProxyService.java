/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.service;

import com.metrobank.communicationhub.common.service.CustomerIdIbIdMappingService;
import com.metrobank.communicationhub.exception.RestProxyUnprocessableEntityException;
import com.metrobank.communicationhub.model.request.PreferenceRequest;
import com.metrobank.communicationhub.model.request.RegistrationRequest;
import com.metrobank.communicationhub.model.request.SendRequest;
import com.metrobank.communicationhub.schema.CommsPreference;
import com.metrobank.communicationhub.schema.Preference;
import com.metrobank.communicationhub.schema.RegistrationAvroRequest;
import com.metrobank.communicationhub.schema.SendAvroRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Optional;
import java.util.stream.Collectors;

import static com.metrobank.communicationhub.util.CommsHubConstants.*;

@Service
@Log4j2
@RequiredArgsConstructor
public class RestProxyService implements RestProxy {

  private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;
  private final CustomerIdIbIdMappingService customerIdIbIdMappingService;

  @Value("${topic.register.request}")
  protected String registerTopic;

  @Value("${topic.send.request}")
  protected String sendTopic;

  @Value("${topic.preference.request}")
  protected String preferenceTopic;

  @Override
  public ResponseEntity sendRegisterRequest(
      final String ibId,
      Integer customerId,
      final RegistrationRequest request,
      final String uuid,
      final HttpMethod requestMethod) {

    log.info("RestProxyService :: register/de-register :: received request : {}", request);

    // If customerId not present in request get it from the mapping table
    if (customerId == null) {
      customerId = getCustomerIdFromIbId(ibId);
    }

    final RegistrationAvroRequest avroRequest =
        buildRegistrationRequest(customerId, request, requestMethod);

    if (ibId != null) {
      avroRequest.setIbId(ibId);
    }

    ProducerRecord<String, SpecificRecordBase> registrationRecord =
        new ProducerRecord<>(registerTopic, null, uuid, avroRequest);
    registrationRecord
        .headers()
        .add(
            PROCESS_NAME,
            requestMethod.toString().equals("POST")
                ? REGISTRATION_PROCESS_NAME.getBytes()
                : DEREGISTRATION_PROCESS_NAME.getBytes());
    registrationRecord
        .headers()
        .add(
            ACTIVTY_NAME,
            requestMethod.toString().equals("POST")
                ? REGISTRATION_ACTIVITY_NAME.getBytes()
                : DEREGISTRATION_ACTIVITY_NAME.getBytes());

    ListenableFuture<SendResult<String, SpecificRecordBase>> future =
        kafkaTemplate.send(registrationRecord);

    future.addCallback(
        new ListenableFutureCallback<>() {
          @Override
          public void onSuccess(SendResult<String, SpecificRecordBase> result) {
            log.info(
                "Sent Registration request with offset:{}", result.getRecordMetadata().offset());
          }

          @Override
          public void onFailure(Throwable ex) {
            log.error("Error sending Registration request: {}", ex.getMessage());
          }
        });

    return new ResponseEntity<>(
        (requestMethod == HttpMethod.POST) ? HttpStatus.CREATED : HttpStatus.NO_CONTENT);
  }

  @Override
  public ResponseEntity sendCommunicateRequest(
      final String ibId, Integer customerId, final SendRequest request, final String uuid) {

    log.info("RestProxyService :: SendCommunication :: received request : {}", request);

    // If customerId not present in request get it from the mapping table
    if (customerId == null) {
      customerId = getCustomerIdFromIbId(ibId);
    }

    final SendAvroRequest avroRequest = buildSendAvroRequest(customerId, request);

    ProducerRecord<String, SpecificRecordBase> sendRecord =
        new ProducerRecord<>(sendTopic, null, uuid, avroRequest);
    sendRecord.headers().add(PROCESS_NAME, ROUTING_PROCESS_NAME.getBytes());
    sendRecord.headers().add(ACTIVTY_NAME, ROUTING_ACTIVITY_NAME.getBytes());

    final ListenableFuture<SendResult<String, SpecificRecordBase>> future =
        kafkaTemplate.send(sendRecord);

    future.addCallback(
        new ListenableFutureCallback<>() {
          @Override
          public void onSuccess(SendResult<String, SpecificRecordBase> result) {
            log.info("Sent Comms request with offset:{}", result.getRecordMetadata().offset());
          }

          @Override
          public void onFailure(Throwable ex) {
            log.error("Error sending Comms request: {}", ex.getMessage());
          }
        });

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity sendPreferenceRequest(
      final String ibId, Integer customerId, final PreferenceRequest request, final String uuid) {

    log.info("RestProxyService :: SendPreference :: received request : {}", request);

    // If customerId not present in request get it from the mapping table
    if (customerId == null) {
      customerId = getCustomerIdFromIbId(ibId);
    }

    final CommsPreference avroRequest = buildPreferenceRequest(ibId, customerId, request);

    ProducerRecord<String, SpecificRecordBase> preferenceRecord =
        new ProducerRecord<>(preferenceTopic, null, uuid, avroRequest);
    preferenceRecord.headers().add(PROCESS_NAME, PREFERENCE_PROCESS_NAME.getBytes());
    preferenceRecord.headers().add(ACTIVTY_NAME, PREFERENCE_ACTIVITY_NAME.getBytes());

    final ListenableFuture<SendResult<String, SpecificRecordBase>> future =
        kafkaTemplate.send(preferenceRecord);

    future.addCallback(
        new ListenableFutureCallback<>() {
          @Override
          public void onSuccess(SendResult<String, SpecificRecordBase> result) {
            log.info("Sent Preference request with offset:{}", result.getRecordMetadata().offset());
          }

          @Override
          public void onFailure(Throwable ex) {
            log.error("Error sending Preference request: {}", ex.getMessage());
          }
        });

    return new ResponseEntity<>(HttpStatus.OK);
  }

  private RegistrationAvroRequest buildRegistrationRequest(
      Integer customerId, RegistrationRequest request, HttpMethod requestMethod) {
    return RegistrationAvroRequest.newBuilder()
        .setMethod(requestMethod.toString())
        .setCommsType(request.getCommsType().toString())
        .setDeviceType(request.getDeviceType().toString())
        .setCustomerId(customerId)
        .setToken(request.getToken())
        .setEmail(request.getEmail())
        .setMobileNo(request.getMobileNo())
        .build();
  }

  private SendAvroRequest buildSendAvroRequest(Integer customerId, SendRequest request) {
    return SendAvroRequest.newBuilder()
        .setCommsType(request.getCommsType().toString())
        .setCustomerId(customerId)
        .setMessageType(request.getMessageType().toString())
        .setEmail(request.getEmail())
        .setMobileNo(request.getMobileNo())
        .setTemplateId(request.getTemplateId())
        .setTemplateParams(request.getTemplateParams())
        .build();
  }

  private CommsPreference buildPreferenceRequest(
      String ibId, Integer customerId, PreferenceRequest request) {
    return CommsPreference.newBuilder()
        .setCommsType(request.getCommsType().toString())
        .setCustomerId(customerId)
        .setMessageType(request.getMessageType().toString())
        .setIbId(ibId)
        .setDeviceId(request.getDeviceId())
        .setMethod(HttpMethod.POST.toString())
        .setPreferences(
            request.getPreferences().stream()
                .map(p -> Preference.newBuilder().setKey(p.getKey()).setValue(p.getValue()).build())
                .collect(Collectors.toList()))
        .build();
  }

  private Integer getCustomerIdFromIbId(String ibId) {
    Optional<Integer> customerIdFromIbId;
    customerIdFromIbId = customerIdIbIdMappingService.getCustomerIdFromIbId(ibId);
    if (customerIdFromIbId.isEmpty()) {
      log.error("Invalid IbId: {}", ibId);
      throw new RestProxyUnprocessableEntityException("Bad Request");
    } else {
      return customerIdFromIbId.get();
    }
  }
}
