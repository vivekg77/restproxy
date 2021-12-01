/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Builder
public class RegistrationRequest {

  @JsonProperty("deviceToken")
  @ApiModelProperty(required = true)
  @NotNull(message = "Missing Data")
  private String token;

  @JsonProperty("deviceType")
  @ApiModelProperty(required = true)
  @NotNull(message = "Missing Data")
  private DeviceType deviceType;

  @JsonProperty("commsType")
  @ApiModelProperty(required = true)
  @NotNull(message = "Missing Data")
  private CommsType commsType;

  @JsonProperty("email")
  @Email(message = "Invalid email format")
  private String email;

  @JsonProperty("mobileNo")
  @Pattern(regexp = "^[0-9]{10}$", message = "Invalid Input")
  private String mobileNo;
}
