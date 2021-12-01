package com.metrobank.communicationhub.model.registration;

import com.metrobank.communicationhub.util.pojoUtility.PojoTestUtility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegistrationResponseTest {
  @Test
  @DisplayName("should access Registration Detail Response class fields with getter and setter")
  void shouldAccessRegistrationDetailsResponseFieldsProperly() {

    PojoTestUtility.validateAccessors(RegistrationResponse.class);
  }
}
