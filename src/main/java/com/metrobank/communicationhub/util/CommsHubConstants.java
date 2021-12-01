/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.util;

public class CommsHubConstants {

  public static final String BAD_REQUEST_ERROR_CODE = "Invalid field";
  public static final String BAD_REQUEST_ERROR_MESSAGE = "Invalid request";
  public static final String BAD_REQUEST_CUSTOMERID_IBID_MISSING = "Missing customerId or IbId";
  public static final String RESPONSE_TIMEOUT_ERROR_CODE = "Response Timeout";
  public static final String INTERNAL_SERVER_ERROR_CODE = "Server Error";
  public static final String RESPONSE_TIMEOUT_ERROR_MESSAGE = "Response timed out";
  public static final String INTERNAL_SERVER_ERROR_MESSAGE =
      "Internal server error, please try again";
  public static final String REGISTRATION_STATUS = "ACTIVE";
  public static final String PROCESS_NAME = "x-process-name";
  public static final String ACTIVTY_NAME = "x-activity-name";
  public static final String REGISTRATION_PROCESS_NAME = "mobile-push-registration";
  public static final String DEREGISTRATION_PROCESS_NAME = "mobile-push-deregistration";
  public static final String REGISTRATION_ACTIVITY_NAME = "COMMS-REGISTRATION-STARTED";
  public static final String DEREGISTRATION_ACTIVITY_NAME = "COMMS-DEREGISTRATION-STARTED";
  public static final String ROUTING_PROCESS_NAME = "mobile-push-send";
  public static final String ROUTING_ACTIVITY_NAME = "COMMS-ROUTER-ROUTING-STARTED";
  public static final String PREFERENCE_ACTIVITY_NAME = "COMMS-PREF-STARTED";
  public static final String PREFERENCE_PROCESS_NAME = "mobile-push-set-preference";



  private CommsHubConstants() {}
}
