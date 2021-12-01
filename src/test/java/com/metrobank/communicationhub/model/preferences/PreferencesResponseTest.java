package com.metrobank.communicationhub.model.preferences;

import com.metrobank.communicationhub.util.pojoUtility.PojoTestUtility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PreferencesResponseTest {
  @Test
  @DisplayName("should access Preferences Response class fields with getter and setter")
  void shouldAccessPreferencesResponseFieldsProperly() {

    PojoTestUtility.validateAccessors(PreferencesResponse.class);
  }
}
