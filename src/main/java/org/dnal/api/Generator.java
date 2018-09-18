package org.dnal.api;

import java.util.List;

import org.dnal.compiler.generate.old.OldOutputGenerator;
import org.dnal.core.NewErrorMessage;

public interface Generator {
    
     boolean generate(OldOutputGenerator visitor);
     List<NewErrorMessage> getErrors();

}