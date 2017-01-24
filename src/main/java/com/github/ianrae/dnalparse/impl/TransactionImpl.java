package com.github.ianrae.dnalparse.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jparsec.functors.Pair;
import org.dval.DListType;
import org.dval.DStructType;
import org.dval.DType;
import org.dval.DTypeRegistry;
import org.dval.DValue;
import org.dval.ErrorMessage;
import org.dval.builder.BooleanBuilder;
import org.dval.builder.BuilderFactory;
import org.dval.builder.DateBuilder;
import org.dval.builder.EnumBuilder;
import org.dval.builder.IntBuilder;
import org.dval.builder.ListBuilder;
import org.dval.builder.LongBuilder;
import org.dval.builder.NumberBuilder;
import org.dval.builder.StringBuilder;
import org.dval.builder.StructBuilder;
import org.dval.repository.MyWorld;

import com.github.ianrae.dnalparse.DValueLoader;
import com.github.ianrae.dnalparse.Transaction;
import com.github.ianrae.dnalparse.WorldException;
import com.github.ianrae.dnalparse.dnalgenerate.CustomRuleFactory;
import com.github.ianrae.dnalparse.impl.CompilerContext;
import com.github.ianrae.dnalparse.nrule.StandardRuleFactory;
import com.github.ianrae.dnalparse.validate.ValidationPhase;

public class TransactionImpl implements Transaction {
    protected List<ErrorMessage> errorList = new ArrayList<>();
    private DTypeRegistry registry;
    private MyWorld world;
    private CustomRuleFactory crf;
    private List<Pair<String, DValue>> pendingL = new ArrayList<>();
    private BuilderFactory factory;
    private CompilerContext context;
    private Map<Class<?>, DValueLoader<?>> loaderRegistry;

    public TransactionImpl(DTypeRegistry registry, MyWorld world, CompilerContext context, Map<Class<?>, DValueLoader<?>> loaderRegistry) {
        this.world = world;
        this.registry = registry;
        StandardRuleFactory standard = new StandardRuleFactory();
        this.crf = standard.createFactory();
        this.factory = new BuilderFactory(registry, errorList);
        this.context = context;
        this.loaderRegistry = loaderRegistry;
    }

    @Override
    public void add(String name, DValue dval) {
        if (dval == null || name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name or dval were null");
        }
        pendingL.add(new Pair<String, DValue>(name, dval));
    }

    //eventually add update and remove which will be handled using event log approach
    @Override
    public boolean commit() {
        //validate
        for(Pair<String,DValue> pair: pendingL) {
            //            String name = pair.a;
            DValue dval = pair.b;
            if (! validateSingleValue(dval)) {
                return false;
            }
        }

        //everything is valid, so add to world
        for(Pair<String,DValue> pair: pendingL) {
            String name = pair.a;
            DValue dval = pair.b;
            //add all sub-vals
            AddObserver observer = new AddObserver(world);
            observer.observe(dval);

            world.addTopLevelValue(name, dval);
        }
        return true;
    }

    private boolean validateSingleValue(DValue dval) {
        ValidationPhase validator = new ValidationPhase(world, context.et);

        boolean b = validator.validateDValue(dval, dval.getType());
        return b;
    }

    @Override
    public List<ErrorMessage> getValErrorList() {
        return errorList;
    }

    @Override
    public DValue createFromBean(Object bean) throws WorldException {
        if (bean == null) {
            throw new WorldException("null passed to createFromBean()");
        }
        
        @SuppressWarnings("unchecked")
        DValueLoader<?> loader = (DValueLoader<?>) loaderRegistry.get(bean.getClass());
        if (loader == null) {
            throw new WorldException(String.format("bean class '%s' not registered. Use loadRegister()", bean.getClass().getSimpleName()));
        }
        
        DValue dval = loader.createDValue(bean);
        return dval;
    }
    
    

    //-------------- builder ------------------------
    @Override
    public DType getType(String typeName) {
        DType structType = registry.getType(typeName);
        return structType;
    }
    @Override
    public DListType getListType(String typeName) {
        DListType listType = (DListType) registry.getType(typeName);
        return listType;
    }
    @Override
    public DStructType getStructType(String typeName) {
        DStructType structType = (DStructType) registry.getType(typeName);
        return structType;
    }

    @Override
    public StructBuilder createStructBuilder(DStructType structType) {
        return factory.createStructBuilder(structType);
    }
    @Override
    public StructBuilder createStructBuilder(String typeName) {
        return factory.createStructBuilder(typeName);
    }

    @Override
    public IntBuilder createIntBuilder() {
        return factory.createIntegerBuilder();
    }
    @Override
    public IntBuilder createIntBuilder(String typeName) {
        return factory.createIntegerBuilder(typeName);
    }
    @Override
    public IntBuilder createIntBuilder(DType type) {
        return factory.createIntegerBuilder(type);
    }

    @Override
    public LongBuilder createLongBuilder() {
        return factory.createLongBuilder();
    }
    @Override
    public LongBuilder createLongBuilder(String typeName) {
        return factory.createLongBuilder(typeName);
    }
    @Override
    public LongBuilder createLongBuilder(DType type) {
        return factory.createLongBuilder(type);
    }

    @Override
    public BooleanBuilder createBooleanBuilder() {
        return factory.createBooleanBuilder();        
    }
    @Override
    public BooleanBuilder createBooleanBuilder(String typeName) {
        return factory.createBooleanBuilder(typeName);
    }
    @Override
    public BooleanBuilder createBooleanBuilder(DType type) {
        return factory.createBooleanBuilder(type);
    }

    @Override
    public NumberBuilder createNumberBuilder() {
        return factory.createNumberBuilder();
    }
    @Override
    public NumberBuilder createNumberBuilder(String typeName) {
        return factory.createNumberBuilder(typeName);
    }
    @Override
    public NumberBuilder createNumberBuilder(DType type) {
        return factory.createNumberBuilder(type);
    }

    @Override
    public DateBuilder createDateBuilder() {
        return factory.createDateBuilder();
    }
    @Override
    public DateBuilder createDateBuilder(String typeName) {
        return factory.createDateBuilder(typeName);
    }
    @Override
    public DateBuilder createDateBuilder(DType type) {
        return factory.createDateBuilder(type);
    }

    @Override
    public StringBuilder createStringBuilder() {
        return factory.createStringBuilder();
    }
    @Override
    public StringBuilder createStringBuilder(String typeName) {
        return factory.createStringBuilder(typeName);
    }
    @Override
    public StringBuilder createStringBuilder(DType type) {
        return factory.createStringBuilder(type);
    }

    @Override
    public EnumBuilder createEnumBuilder(String typeName) {
        return factory.createEnumBuilder(typeName);
    }
    @Override
    public EnumBuilder createEnumBuilder(DType type) {
        return factory.createEnumBuilder(type);
    }

    //for list builder the list elements should be built with lower-level DValueBuilder, assembled into the list
    //then added to the world all at once. DValueBuilders all should use same BuffereingWorldAdder
    @Override
    public ListBuilder createListBuilder(String typeName) {
        return factory.createListBuilder(typeName);
    }
    @Override
    public ListBuilder createListBuilder(DListType type) {
        return factory.createListBuilder(type);
    }

}
