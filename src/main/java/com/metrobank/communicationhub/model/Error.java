/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Error {
  @JsonProperty("code")
  private String code;

  @JsonProperty("message")
  private String message;
}
