/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
public class PreferenceRequest {

  @JsonProperty("commsType")
  @NotNull(message = "Missing Data")
  private CommsType commsType;

  @JsonProperty("messageType")
  @NotNull(message = "Missing Data")
  private MessageType messageType;

  @JsonProperty("deviceId")
  private String deviceId;

  @JsonProperty("preferences")
  @NotNull(message = "Missing Data")
  private List<Preference> preferences;

  @Data
  @Builder
  public static class Preference {
    @JsonProperty("key")
    private String key;

    @JsonProperty("value")
    private String value;
  }
}
