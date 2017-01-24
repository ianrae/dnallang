package com.github.ianrae.dnalparse.codegen.java;

import java.util.ArrayList;
import java.util.List;

import org.dval.DListType;
import org.dval.DType;
import org.dval.nrule.NRule;

import com.github.ianrae.dnalc.ConfigFileOptions;
import com.github.ianrae.dnalparse.generate.GenerateVisitor;

public class JavaCodeGen extends CodeGenBase {
    public List<GenerateVisitor> list = new ArrayList<>();
    
    public JavaCodeGen(ConfigFileOptions options) {
        super(options);
        list.add(new InterfaceCodeGen(options));
        list.add(new ImmutableBeanCodeGen(options));
        list.add(new BeanCodeGen(options));
    }
    
    @Override
    public void startType(String name, DType dtype) throws Exception {
        for(GenerateVisitor visitor: list) {
            visitor.startType(name, dtype);
        }
    }

    @Override
    public void startListType(String name, DListType type) throws Exception {
        for(GenerateVisitor visitor: list) {
            visitor.startListType(name, type);
        }
    }


    @Override
    public void endType(String name, DType type) throws Exception {
        for(GenerateVisitor visitor: list) {
            visitor.endType(name, type);
        }
    }

    @Override
    public void startMember(String name, DType type) throws Exception {
        for(GenerateVisitor visitor: list) {
            visitor.startMember(name, type);
        }
    }

    @Override
    public void endMember(String name, DType s) throws Exception {
        for(GenerateVisitor visitor: list) {
            visitor.endMember(name, s);
        }
    }

    @Override
    public void rule(String ruleText, NRule rule) throws Exception {
        for(GenerateVisitor visitor: list) {
            visitor.rule(ruleText, rule);
        }
    }

    @Override
    public void finish() throws Exception {
        for(GenerateVisitor visitor: list) {
            visitor.finish();
        }
    }
}