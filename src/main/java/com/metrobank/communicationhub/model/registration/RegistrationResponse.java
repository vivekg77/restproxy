/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.model.registration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Pojo for the registration details. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class RegistrationResponse {

  @JsonProperty("registrations")
  public List<RegistrationDetails> registrationDetails;

  @Data
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class RegistrationDetails {

    @JsonProperty("commsType")
    public String commsType;

    @JsonProperty("deviceType")
    public String deviceType;

    @JsonProperty("email")
    public String email;

    @JsonProperty("mobileNo")
    public String mobileNo;

    @JsonProperty("deviceToken")
    public String deviceToken;

    @JsonProperty("status")
    public String status;
  }
}
