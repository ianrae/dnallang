package org.dnal.core.builder;

import java.util.List;

import org.dnal.core.DType;
import org.dnal.core.DValue;
import org.dnal.core.NewErrorMessage;
import org.dnal.core.xbuilder.XBooleanValueBuilder;

public class BooleanBuilder extends Builder {
    private XBooleanValueBuilder builder;
    
    public BooleanBuilder(DType type, List<NewErrorMessage> valErrorList) {
        super(valErrorList);
        builder = new XBooleanValueBuilder(type);
    }

    public DValue buildFromString(String input) {
        builder.buildFromString(input);
        wasSuccessful = builder.finish();
        valErrorList.addAll(builder.getValidationErrors());
        return builder.getDValue();
    }
    public DValue buildFrom(Boolean b) {
        builder.buildFrom(b);
        wasSuccessful = builder.finish();
        valErrorList.addAll(builder.getValidationErrors());
        return builder.getDValue();
    }

}