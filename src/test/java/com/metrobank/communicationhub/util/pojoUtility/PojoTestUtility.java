package com.metrobank.communicationhub.util.pojoUtility;

import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.*;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class PojoTestUtility {
    private static final Validator ACCESSOR_VALIDATOR = ValidatorBuilder.create()
            .with(new GetterTester())
            .with(new SetterTester())
            .with(new GetterMustExistRule())
            .with(new SetterMustExistRule())
            .with(new TestClassMustBeProperlyNamedRule())
            .with(new NoStaticExceptFinalRule())
            .with(new NoFieldShadowingRule())
            .build();

    public static void validateAccessors(final Class<?> pojoClassToTest) {
        ACCESSOR_VALIDATOR.validate(PojoClassFactory.getPojoClass(pojoClassToTest));
    }
}
