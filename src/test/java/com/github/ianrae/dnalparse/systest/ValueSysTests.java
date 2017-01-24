package com.github.ianrae.dnalparse.systest;

import static org.junit.Assert.assertEquals;

import org.dval.DValue;
import org.junit.Test;

public class ValueSysTests extends SysTestBase {

    @Test
    public void testT200() {
        chkFail("type Foo int end let x Foo = 14 let x int = 44", 1, "value name 'x' has already been defined");
    }
    
    
}