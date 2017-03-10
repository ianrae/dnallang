package org.dnal.api;

import java.util.List;

import org.dnal.compiler.generate.GenerateVisitor;
import org.dnal.core.NewErrorMessage;

public interface Generator {
    
     boolean generate(GenerateVisitor visitor);
     List<NewErrorMessage> getErrors();

}