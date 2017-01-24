package com.github.ianrae.dnalparse;

import static org.junit.Assert.*;

import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class StringTemplateTests {
    private static final String CODEGEN_DIR = "./src/main/resources/test/codegen/";

    @Test
    public void test() {
        ST hello = new ST("Hello, <name>");
        hello.add("name", "World");
        System.out.println(hello.render());    
    }
    
    @Test
    public void test2() {
     // Load the file
        final STGroup stGroup = new STGroupFile(CODEGEN_DIR + "file1.stg");

        // Pick the correct template
        final ST templateExample = stGroup.getInstanceOf("templateExample");

        // Pass on values to use when rendering
        templateExample.add("param", "Hello World");

        templateExample.addAggr("items.{ firstName ,lastName, id }", "Ter", "Parr", 99); // add() uses varargs
        templateExample.addAggr("items.{firstName, lastName ,id}", "Tom", "Burns", 34);
        
        // Render
        final String render = templateExample.render();

        // Print
        System.out.println(render);
    }

}
