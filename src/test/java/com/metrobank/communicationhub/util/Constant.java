/**
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.communicationhub.util;

import com.metrobank.communicationhub.model.request.*;
import com.metrobank.communicationhub.schema.CommsPreference;
import com.metrobank.communicationhub.schema.Preference;
import com.metrobank.communicationhub.schema.RegistrationAvroRequest;

import java.util.ArrayList;
import java.util.List;

public class Constant {
    public static final String ACCEPT = "Accept";
    public static final String REGISTER_URL = "/comms/v1/register";
    public static final String PREFERENCE_URL = "/comms/v1/preference";
    public static final String SEND_URL = "/comms/v1/send";
    public static final String IBID = "123456789012";
    public static final Integer CUSTOMER_ID = 12345;
    public static final String DEVICE_TYPE = "ANDROID";
    public static final String DEVICE_ID = "abc123ed";
    public static final String DEVICE_TOKEN = "abc123ed";
    public static final String EMAIL = "test@gmail.com";
    public static final String MOBILE_NO = "9234567890";
    public static final String STATUS = "Active";
    public static final String IBID_HEADER = "x-ibid";
    public static final String CUSTOMER_ID_HEADER = "x-customer-id";
    public static final String DEVICE_ID_HEADER = "x-device-id";
    public static final String DEVICE_TYPE_HEADER = "x-device-type";
    public static final String COMMS_TYPE_QUERY_PARAM = "comms-type";
    public static final String ENDPOINT_ARN = "arn:aws:sns:eu-west-1:672537796276/....";
    public static final RegistrationAvroRequest REGISTRATION_AVRO_REQUEST =RegistrationAvroRequest.newBuilder()
            .setMethod("POST")
            .setCommsType("MOBILE_PUSH")
            .setDeviceType("ANDROID")
            .setCustomerId(92345678)
            .setToken("12345")
            .setEmail("user@example.com")
            .setMobileNo("4234567812")
            .build();
    public static final RegistrationRequest REGISTRATION_REQUEST = RegistrationRequest
            .builder()
            .commsType(CommsType.MOBILE_PUSH)
            .deviceType(DeviceType.ANDROID)
            .token(DEVICE_ID)
            .email(EMAIL)
            .mobileNo(MOBILE_NO)
            .build();
    public static final PreferenceRequest PREFERENCE_REQUEST =
            PreferenceRequest.builder()
                    .messageType(MessageType.PAYMENT_SEPA)
                    .deviceId(DEVICE_ID)
                    .commsType(CommsType.MOBILE_PUSH)
                    .preferences(
                            new ArrayList<>(
                                    List.of(
                                            PreferenceRequest.Preference.builder().key("enabled").value("true").build())))
                    .build();
    public static final CommsPreference COMMS_PREFERENCE =
            CommsPreference.newBuilder()
                    .setDeviceId(DEVICE_ID)
                    .setIbId(IBID)
                    .setCustomerId(CUSTOMER_ID)
                    .setCommsType(CommsType.MOBILE_PUSH.toString())
                    .setMessageType(MessageType.PAYMENT_SEPA.toString())
                    .setPreferences(
                            new ArrayList<>(
                                    List.of(Preference.newBuilder().setKey("enabled").setValue("true").build())))
                    .build();

  public static final RegistrationRequest REGISTRATION_REQUEST_NULL =
      RegistrationRequest.builder().build();
    public static final PreferenceRequest PREFERENCE_REQUEST_NULL =
            PreferenceRequest.builder().build();
    public static final SendRequest SEND_REQUEST = SendRequest
            .builder()
            .commsType(CommsType.MOBILE_PUSH)
            .messageType(MessageType.PAYMENT_SEPA)
            .email(EMAIL)
            .mobileNo(MOBILE_NO)
            .templateId("abc12")
            .templateParams(List.of("foo", "bar"))
            .build();

    public static final SendRequest NULL_SEND_REQUEST = SendRequest.builder().build();

  public static final String RETRIEVE_PREFERENCE_RESPONSE =
      "{\"preferences\":[{\"messageType\":\"PAYMENT_SEPA\",\"preference\":{\"enabled\":\"true\"}}]}";
}
