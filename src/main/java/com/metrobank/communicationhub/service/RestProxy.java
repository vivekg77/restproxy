/**
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.communicationhub.service;

import com.metrobank.communicationhub.model.request.PreferenceRequest;
import com.metrobank.communicationhub.model.request.RegistrationRequest;
import com.metrobank.communicationhub.model.request.SendRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface RestProxy {
    ResponseEntity<HttpStatus> sendRegisterRequest(
            final String ibId, final Integer customerId, final RegistrationRequest request, final String uuid, final HttpMethod requestMethod);

    ResponseEntity<HttpStatus> sendCommunicateRequest(
            final String ibId, final Integer customerId, final SendRequest request, final String uuid);

    ResponseEntity<HttpStatus> sendPreferenceRequest(final String ibId, final Integer customerId, final PreferenceRequest request, final String uuid);
}
