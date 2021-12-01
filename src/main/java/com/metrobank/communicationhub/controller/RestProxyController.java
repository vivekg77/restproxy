/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.controller;

import com.metrobank.communicationhub.exception.RestProxyUnprocessableEntityException;
import com.metrobank.communicationhub.model.Error;
import com.metrobank.communicationhub.model.preferences.PreferencesResponse;
import com.metrobank.communicationhub.model.registration.RegistrationResponse;
import com.metrobank.communicationhub.model.request.*;
import com.metrobank.communicationhub.service.PreferencesService;
import com.metrobank.communicationhub.service.RegisterService;
import com.metrobank.communicationhub.service.RestProxyService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.metrobank.communicationhub.util.CommsHubConstants.BAD_REQUEST_CUSTOMERID_IBID_MISSING;
import static com.metrobank.communicationhub.util.CommsHubConstants.BAD_REQUEST_ERROR_MESSAGE;
import static java.lang.String.format;
import static java.util.Objects.isNull;

@RestController
@RequiredArgsConstructor
@Log4j2
@Api(tags = {"Communication Hub"})
@SwaggerDefinition(
    tags = {
      @Tag(
          name = "Communication Hub REST Proxy Controller",
          description = "Communication Hub REST Proxy Operations")
    })
@RequestMapping(value = "comms/v1")
public class RestProxyController {
  private final RestProxyService restProxyService;
  private final RegisterService registerService;
  private final PreferencesService preferencesService;

  @ApiOperation(value = "Process Registration request", consumes = "application/json")
  @ApiResponses({
    @ApiResponse(code = 201, message = "CREATED"),
    @ApiResponse(code = 400, message = "BAD_REQUEST", response = Error.class)
  })
  @PostMapping(value = "register", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> register(
      @RequestHeader(value = "x-ibid", required = false) String ibId,
      @RequestHeader(value = "x-customer-id", required = false) Integer customerId,
      @RequestAttribute("generatedId") String uuid,
      @RequestBody @Valid final RegistrationRequest request,
      final BindingResult bindingResult) {

    if (customerId == null && ibId == null) {
      throw new RestProxyUnprocessableEntityException(BAD_REQUEST_CUSTOMERID_IBID_MISSING);
    }

    validateRequest(bindingResult);

    log.info(
        "RestProxyController :: Register Device :: CustomerId: {}, IBID : {}, ReceivedRequest : {}",
        customerId,
        ibId,
        request);

    return restProxyService.sendRegisterRequest(ibId, customerId, request, uuid, HttpMethod.POST);
  }

  @ApiOperation(value = "Process DeRegistration request", consumes = "application/json")
  @ApiResponses({
    @ApiResponse(code = 204, message = "NO_CONTENT"),
    @ApiResponse(code = 400, message = "BAD_REQUEST", response = Error.class)
  })
  @DeleteMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> deregister(
      @RequestHeader(value = "x-ibid", required = false) String ibId,
      @RequestHeader(value = "x-customer-id", required = false) Integer customerId,
      @RequestAttribute("generatedId") String uuid,
      @RequestBody @Valid final RegistrationRequest request,
      final BindingResult bindingResult) {

    if (customerId == null && ibId == null) {
      throw new RestProxyUnprocessableEntityException(BAD_REQUEST_CUSTOMERID_IBID_MISSING);
    }

    validateRequest(bindingResult);

    log.info(
        "RestProxyController :: DeRegister Device :: CustomerId: {}, IBID : {}, ReceivedRequest : {}",
        customerId,
        ibId,
        request);

    return restProxyService.sendRegisterRequest(ibId, customerId, request, uuid, HttpMethod.DELETE);
  }

  @ApiOperation(value = "Process Communication request", consumes = "application/json")
  @ApiResponses({
    @ApiResponse(code = 200, message = "SUCCESS"),
    @ApiResponse(code = 400, message = "BAD_REQUEST", response = Error.class)
  })
  @PostMapping(value = "send", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> send(
      @RequestHeader(value = "x-ibid", required = false) String ibId,
      @RequestHeader(value = "x-customer-id", required = false) Integer customerId,
      @RequestAttribute("generatedId") String uuid,
      @RequestBody @Valid final SendRequest request,
      final BindingResult bindingResult) {

    if (customerId == null && ibId == null) {
      throw new RestProxyUnprocessableEntityException(BAD_REQUEST_CUSTOMERID_IBID_MISSING);
    }

    validateRequest(bindingResult);

    log.info(
        "RestProxyController :: Send Communication :: CustomerId: {}, IBID : {}, ReceivedRequest : {}",
        customerId,
        ibId,
        request);

    return restProxyService.sendCommunicateRequest(ibId, customerId, request, uuid);
  }

  @ApiOperation(value = "Save Preference request", consumes = "application/json")
  @ApiResponses({
    @ApiResponse(code = 200, message = "SUCCESS"),
    @ApiResponse(code = 400, message = "BAD_REQUEST", response = Error.class)
  })
  @PostMapping(
      value = "preference",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PreferenceRequest> savePreference(
      @RequestHeader(value = "x-ibid", required = false) String ibId,
      @RequestHeader(value = "x-customer-id", required = false) Integer customerId,
      @RequestAttribute("generatedId") String uuid,
      @RequestBody @Valid final PreferenceRequest request,
      final BindingResult bindingResult) {

    if (customerId == null && ibId == null) {
      throw new RestProxyUnprocessableEntityException(BAD_REQUEST_CUSTOMERID_IBID_MISSING);
    }

    validateRequest(bindingResult);

    log.info(
        "RestProxyController :: Save Preferences :: CustomerId: {}, IBID : {}, ReceivedRequest : {}",
        customerId,
        ibId,
        request);

    return restProxyService.sendPreferenceRequest(ibId, customerId, request, uuid);
  }

  @ApiOperation(value = "Process get registration request", produces = "application/json")
  @ApiResponses({
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 400, message = "BAD_REQUEST", response = Error.class)
  })
  @GetMapping(value = "register", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<RegistrationResponse> getRegistration(
      @RequestHeader(value = "x-device-id", required = false) String deviceId,
      @RequestHeader(value = "x-device-type", required = false) DeviceType deviceType,
      @RequestHeader(value = "x-ibid", required = false) String ibId,
      @RequestHeader(value = "x-customer-id", required = false) Integer customerId,
      @RequestParam("comms-type") CommsType commsType) {

    if (customerId == null && ibId == null) {
      throw new RestProxyUnprocessableEntityException(BAD_REQUEST_CUSTOMERID_IBID_MISSING);
    }
    if (commsType.equals(CommsType.MOBILE_PUSH)
        && (StringUtils.isEmpty(deviceId) || isNull(deviceType))) {
      throw new RestProxyUnprocessableEntityException(BAD_REQUEST_ERROR_MESSAGE);
    }

    log.info(
        "RestProxyController :: GetRegistration :: CustomerId: {}, IBID : {}, DeviceToken: {}, DeviceType: {}, CommsType: {}",
        customerId,
        ibId,
        deviceId,
        deviceType,
        commsType);

    return new ResponseEntity<>(
        registerService.getRegistration(
            ibId, customerId, deviceId, deviceType.name(), commsType.name()),
        HttpStatus.OK);
  }

  @ApiOperation(value = "Process get preferences request", produces = "application/json")
  @ApiResponses({
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 400, message = "BAD_REQUEST", response = Error.class)
  })
  @GetMapping(value = "preference", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PreferencesResponse> getPreference(
      @RequestHeader(value = "x-device-id", required = false) String deviceId,
      @RequestHeader(value = "x-ibid", required = false) String ibId,
      @RequestHeader(value = "x-customer-id", required = false) Integer customerId,
      @RequestParam("comms-type") CommsType commsType) {

    if (customerId == null && ibId == null) {
      throw new RestProxyUnprocessableEntityException(BAD_REQUEST_CUSTOMERID_IBID_MISSING);
    }

    if (commsType.equals(CommsType.MOBILE_PUSH) && StringUtils.isEmpty(deviceId)) {
      throw new RestProxyUnprocessableEntityException(BAD_REQUEST_ERROR_MESSAGE);
    }

    log.info(
        "RestProxyController :: GetPreferences :: CustomerId: {}, IBID : {}, DeviceToken: {}, CommsType: {}",
        customerId,
        ibId,
        deviceId,
        commsType);

    return new ResponseEntity<>(
        preferencesService.getPreferencesForCustomer(ibId, customerId, deviceId, commsType.name()),
        HttpStatus.OK);
  }

  private void validateRequest(BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      final FieldError error = (FieldError) bindingResult.getAllErrors().get(0);
      final String rejectedMessage = format("Invalid: %s", error.getField());
      log.warn("Bad request {}", rejectedMessage);
      throw new RestProxyUnprocessableEntityException(error.getDefaultMessage());
    }
  }
}
