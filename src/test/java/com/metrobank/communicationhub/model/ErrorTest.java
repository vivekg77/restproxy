package com.metrobank.communicationhub.model;

import com.metrobank.communicationhub.util.pojoUtility.PojoTestUtility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class ErrorTest {
    @Test
    @DisplayName("should access Error class fields with getter and setter")
    void shouldAccessErrorMappingFieldsProperly() {

        PojoTestUtility.validateAccessors(Error.class);
    }

}