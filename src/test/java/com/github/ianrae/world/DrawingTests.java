package com.github.ianrae.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.dval.DStructHelper;
import org.dval.DStructType;
import org.dval.DValue;
import org.dval.logger.ErrorMessageLogger;
import org.dval.logger.Log;
import org.dval.nrule.NRuleRunnerImpl;
import org.dval.nrule.SimpleNRuleRunner;
import org.junit.Test;

import com.github.ianrae.dnalparse.DNALCompiler;
import com.github.ianrae.dnalparse.DataSet;
import com.github.ianrae.dnalparse.Transaction;
import com.github.ianrae.dnalparse.et.XErrorTracker;
import com.github.ianrae.dnalparse.impoter.MockImportLoader;
import com.github.ianrae.dnalparse.parser.error.ErrorTrackingBase;

public class DrawingTests extends BaseWorldTest {
    
    private static final String GENERATE_DIR = "./src/main/resources/test/example/";
    
    @Test
    public void test1() {
        NRuleRunnerImpl.ruleCounter = 0;
        DataSet dataSet = load("drawing1.dnal");
        assertEquals(3, dataSet.size());
        
        Transaction trans = dataSet.createTransaction();
        DStructType shapeType = trans.getStructType("Shape");
        DStructType circType = trans.getStructType("Circle");
        assertEquals(null, shapeType.getBaseType());
        assertEquals("Shape", circType.getBaseType().getName());

        DValue val = dataSet.getValue("circle1");
        assertNotNull(val);
        DStructHelper helper = new DStructHelper(val);
        assertEquals(3, helper.getFieldNames().size());
        for(String fieldName: helper.getFieldNames()) {
            DValue el = helper.getField(fieldName);
            int x = helper.getField(fieldName).asInt();
            log(String.format("x=%d", x));
        }
        
        val = dataSet.getValue("shape1");
        assertNotNull(val);
        helper = new DStructHelper(val);
        assertEquals(2, helper.getFieldNames().size());
        for(String fieldName: helper.getFieldNames()) {
            DValue el = helper.getField(fieldName);
            int x = helper.getField(fieldName).asInt();
            log(String.format("2x=%d", x));
            
        }
        
        assertEquals(5, NRuleRunnerImpl.ruleCounter);
    }
    @Test
    public void test2() {
        NRuleRunnerImpl.ruleCounter = 0;
        DataSet dataSet = load("drawing2.dnal");
        
        Transaction trans = dataSet.createTransaction();
        DStructType shapeType = trans.getStructType("Shape");
        DStructType circType = trans.getStructType("Circle");
        assertEquals(null, shapeType.getBaseType());
        assertEquals("Shape", circType.getBaseType().getName());

        DValue val = dataSet.getValue("drawing");
        assertNotNull(val);
        List<DValue> list = val.asList();
        assertEquals(4, list.size());
        for(DValue el: list) {
            DStructHelper helper = new DStructHelper(el);
            int x = helper.getField("x").asInt();
            log(String.format("x=%d", x));
            if (el.getType().getName().equals("Circle")) {
                log("circ!");
            }
            
        }
        
        assertEquals(14, NRuleRunnerImpl.ruleCounter);
    }
    
    private DataSet load(String dnalFilename) {
        XErrorTracker.logErrors = true;
        Log.debugLogging = true;
        
        String path = GENERATE_DIR + dnalFilename;
        DNALCompiler compiler = createCompiler();
        DataSet dataSet = compiler.compile(path);
        ErrorMessageLogger.dump(compiler.getErrors());
        assertNotNull(dataSet);
        return dataSet;
    }
    
}
