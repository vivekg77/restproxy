/**
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.communicationhub.service;

import com.metrobank.communicationhub.model.registration.RegistrationResponse;

public interface Register {
    RegistrationResponse getRegistration(final String ibId, final Integer customerId, final String deviceToken, final String deviceType, final String commsType);
}