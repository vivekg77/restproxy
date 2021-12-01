/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.model.preferences;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class PreferencesResponse {
  @JsonProperty("preferences")
  private List<Preference> preferences;

  @Data
  @Builder
  public static class Preference {
    @JsonProperty("messageType")
    private String messageType;

    @JsonProperty("preference")
    Map<String, String> commsPreference;
  }
}
