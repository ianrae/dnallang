package org.dnal.compiler.core;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.dnal.api.impl.CompilerContext;
import org.dnal.compiler.dnalgenerate.ASTToDNALGenerator;
import org.dnal.compiler.dnalgenerate.CustomRuleFactory;
import org.dnal.compiler.nrule.StandardRuleFactory;
import org.dnal.compiler.parser.FullParser;
import org.dnal.compiler.parser.ast.Exp;
import org.dnal.compiler.parser.error.LineLocator;
import org.dnal.compiler.validate.ValidationOptions;
import org.dnal.compiler.validate.ValidationPhase;
import org.dnal.core.DType;
import org.dnal.core.DTypeRegistry;
import org.dnal.core.DValue;
import org.dnal.core.NewErrorMessage;
import org.dnal.core.ValidationState;
import org.dnal.core.repository.World;
import org.junit.Before;

public class BaseValidationTests extends BaseTest {
    
    @Before
    public void init() {
        StandardRuleFactory rf = new StandardRuleFactory();
        crf = rf.createFactory();
    }
    
    protected void chkInvalid(DValue dval) {
        assertEquals(ValidationState.INVALID, dval.getValState());
    }


    protected void log(String s) {
        System.out.println(s);
    }
    
    protected void parseAndValidate(String input, boolean expected, String shape) {
    	parseAndValidate(input, expected, shape, ValidationOptions.VALIDATEMODE_ALL);
    }    
    protected void parseAndValidate(String input, boolean expected, String shape, int validationMode) {
        List<NewErrorMessage> errL = new ArrayList<>();
        ASTToDNALGenerator dnalGenerator = parse(errL, input, "Foo", shape, crf);
        errL.addAll(dnalGenerator.getErrL());
        World world = getContext().world;
        getContext().validateOptions.validationMode = validationMode;
        LineLocator lineLocator = null;
        
        //TODO: clean this up
        CompilerContext compilerContext = new CompilerContext(null, null, null, null,null);
        compilerContext.world = world;
        compilerContext.registry = getContext().registry;
        
        ValidationPhase validator = new ValidationPhase(compilerContext, getContext().et, getContext().validateOptions, lineLocator);
    
//      DType type = dnalGenerator.getRegistry().getType("Foo");
//      for(NRule rule: CustomRuleRegistry.getRuleRunners()) {
//          type.getRawRules().add(rule);
//      }
        boolean b = validator.validate();
        validator.dumpErrors();
        assertEquals(expected, b);
    }
    
    protected int expected = 2;
    protected boolean generateOk = true;
    protected boolean dumpWorld = true;
    protected ASTToDNALGenerator parse(List<NewErrorMessage> errL, String input, String typeName, String baseType, CustomRuleFactory crf) {
        log("doing: " + input);
        List<Exp> list = FullParser.fullParse(input);
        assertEquals(expected, list.size());
        ASTToDNALGenerator dnalGenerator = createASTGenerator();
        boolean b = dnalGenerator.generate(list);
        dnalGenerator.dumpErrors();
        assertEquals(generateOk, b);

        World world = getContext().world;
        if (dumpWorld) {
            world.dump();
        }

        DTypeRegistry registry = getContext().registry;
        DType type = registry.getType(typeName);
        assertEquals(typeName, type.getName());

        if (baseType == null) {
            assertEquals(null, type.getBaseType());
        } else {
            assertEquals(baseType, type.getBaseType().getName());
        }
        return dnalGenerator;
    }
}
