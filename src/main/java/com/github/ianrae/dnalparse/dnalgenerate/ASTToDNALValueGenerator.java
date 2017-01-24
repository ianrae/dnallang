package com.github.ianrae.dnalparse.dnalgenerate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dval.DListType;
import org.dval.DStructHelper;
import org.dval.DStructType;
import org.dval.DType;
import org.dval.DTypeRegistry;
import org.dval.DValue;
import org.dval.DValueImpl;
import org.dval.DValueProxy;
import org.dval.ErrorMessage;
import org.dval.Shape;
import org.dval.TypePair;
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
import org.dval.logger.Log;
import org.dval.repository.MyWorld;

import com.github.ianrae.dnalparse.impl.CompilerContext;
import com.github.ianrae.dnalparse.parser.DNALDocument;
import com.github.ianrae.dnalparse.parser.ast.BooleanExp;
import com.github.ianrae.dnalparse.parser.ast.EnumMemberExp;
import com.github.ianrae.dnalparse.parser.ast.Exp;
import com.github.ianrae.dnalparse.parser.ast.FullAssignmentExp;
import com.github.ianrae.dnalparse.parser.ast.FullEnumTypeExp;
import com.github.ianrae.dnalparse.parser.ast.FullListTypeExp;
import com.github.ianrae.dnalparse.parser.ast.FullStructTypeExp;
import com.github.ianrae.dnalparse.parser.ast.FullTypeExp;
import com.github.ianrae.dnalparse.parser.ast.IdentExp;
import com.github.ianrae.dnalparse.parser.ast.IntegerExp;
import com.github.ianrae.dnalparse.parser.ast.ListAssignExp;
import com.github.ianrae.dnalparse.parser.ast.LongExp;
import com.github.ianrae.dnalparse.parser.ast.NumberExp;
import com.github.ianrae.dnalparse.parser.ast.StringExp;
import com.github.ianrae.dnalparse.parser.ast.StructAssignExp;
import com.github.ianrae.dnalparse.parser.ast.StructMemberAssignExp;
import com.github.ianrae.dnalparse.parser.ast.ViaExp;
import com.github.ianrae.dnalparse.parser.error.ErrorTrackingBase;
import com.github.ianrae.dnalparse.parser.error.TypeInfo;

public class ASTToDNALValueGenerator extends ErrorTrackingBase  {
    protected MyWorld world;
    protected DTypeRegistry registry;
    private BuilderFactory factory;
    private List<ErrorMessage> valErrorList;
    private PackageHelper packageHelper;
    private ViaFinder viaFinder;
    private ViaHelper viaHelper;
    private StructBuilder currentStructBuilder;
    private boolean useProxyDVals = false;

    public ASTToDNALValueGenerator(MyWorld world, CompilerContext context, DNALDocument doc, DTypeRegistry registry, PackageHelper packageHelper) {
        super(doc, context.et);
        this.world = world;
        this.registry = registry;
        this.valErrorList = new ArrayList<>();
        factory = new BuilderFactory(registry, valErrorList);
        this.packageHelper = packageHelper;
        this.viaFinder = new ViaFinder(world, registry, context.et);
        this.viaHelper = new ViaHelper();
        this.useProxyDVals = context.compilerOptions.isUseProxyDValues();
    }

    //error if returns null
    public DValue buildTopLevelValue(FullAssignmentExp assignExp) {
        DValue dval = buildValue(assignExp);
        
        //top-level objects should be proxies
        dval = maybeGenProxy(dval);
        return dval;
    }
    
    private DValue maybeGenProxy(DValue dval) {
        if (dval != null && this.useProxyDVals) {
            return new DValueProxy(dval);
        } else {
            return dval;
        }
    }
    
    
    private DValue buildValue(FullAssignmentExp assignExp) {
        String varName = assignExp.var.name();

        Log.debugLog("value: " + varName);
        if (assignExp.isListVar()) {
            if (assignExp.value instanceof ListAssignExp) {
                return buildListValue((ListAssignExp)assignExp.value, assignExp.type.name());
            } else if (assignExp.value instanceof ViaExp) {
                return buildViaListValue(assignExp, (ViaExp) assignExp.value, assignExp.type.name());
            } else {
                this.addError2s("let %s does not contain list %s", assignExp.var.name(), "");
                return null; //??
            }
        }


        if (assignExp.value instanceof StructAssignExp) {
            return buildStructValue((StructAssignExp)assignExp.value, assignExp.type.name());
        } else if (assignExp.value instanceof ListAssignExp) {
            return buildListValue((ListAssignExp)assignExp.value, assignExp.type.name());
        } else if (assignExp.value instanceof ViaExp) {
            ViaExp via = (ViaExp) assignExp.value;
            return handleVia(assignExp, via);
        } else {
            DValue resultVal = null;

            String shape = this.doc.getShape(assignExp.type);
            DType dtype = getddType(assignExp.type);
            switch(TypeInfo.typeOf(shape)) {
            case INT:
            {
                IntBuilder builder = factory.createIntegerBuilder(dtype);
                IntegerExp tmp = (IntegerExp) resolveRHS(assignExp);
                resultVal = builder.buildFrom(tmp.val); 
            }
            break;
            case LONG:
            {
                LongBuilder builder = factory.createLongBuilder(dtype);
                Exp tmp = resolveRHS(assignExp);
                Long nVal = 0L;
                if (tmp instanceof LongExp) {
                    nVal = ((LongExp) tmp).val;
                } else if (tmp instanceof IntegerExp) {
                    nVal = Long.valueOf(((IntegerExp)tmp).val.longValue());
                } else {
                    this.addError2s("var '%s': long var not a long or int %s", assignExp.var.name(), "");
                }
                resultVal = builder.buildFrom(nVal); 
            }
            break;
            case NUMBER:
            {
                NumberBuilder builder = factory.createNumberBuilder(dtype);
                NumberExp tmp = (NumberExp) resolveRHS(assignExp);
                resultVal = builder.buildFrom(tmp.val); 
            }
            break;
            case DATE:
            {
                resultVal = handleDate(assignExp, dtype);
                if (resultVal == null) {
                    this.addError2s("var '%s': date value not number or string %s", assignExp.var.name(), "");
                }
            }
            break;
            case BOOLEAN:
            {
                BooleanBuilder builder = factory.createBooleanBuilder(dtype);
                BooleanExp tmp = (BooleanExp) resolveRHS(assignExp);
                resultVal = builder.buildFrom(tmp.val);
            }
            break;
            case STRING:
            {
                StringBuilder builder = factory.createStringBuilder(dtype);
                StringExp tmp = (StringExp) resolveRHS(assignExp);
                resultVal = builder.buildFromString(tmp.val); 
            }
            break;
            case ENUM:
            {
                IdentExp tmp = (IdentExp) assignExp.value;
                FullTypeExp fullType = doc.findType(assignExp.type.name());
                boolean found = false;
                FullEnumTypeExp fste = (FullEnumTypeExp) fullType;
                for (EnumMemberExp vv: fste.members.list) {
                    if (vv.var.name().equals(tmp.name())) {
                        found = true;
                    }
                }

                if (! found) {
                    this.addError2s("enum %s does not contain %s", fullType.var.name(), tmp.name());
                    return null;
                }

                EnumBuilder builder = factory.createEnumBuilder(dtype);
                resultVal = builder.buildFromString(tmp.name()); 
            }
            break;
            //				case LIST:
            //					break;

            default:
                addError2s("var '%s' - unknown shape '%s'", varName, shape);
                break;
            }

            return resultVal;
        }
    }

    private DValue handleDate(FullAssignmentExp assignExp, DType dtype) {
        DateBuilder builder = factory.createDateBuilder(dtype);
        Exp tmp = resolveRHS(assignExp);
        if (tmp instanceof LongExp) {
            LongExp longExp = (LongExp) tmp;
            Date dt = new Date(longExp.val);
            return builder.buildFrom(dt);
        } else if (tmp instanceof IntegerExp) {
            IntegerExp intExp = (IntegerExp) tmp;
            Date dt = new Date(Long.valueOf(intExp.val));
            return builder.buildFrom(dt);
        } else if (tmp instanceof StringExp) {
            StringExp strExp = (StringExp) tmp;
            Date dt = DateFormatParser.parse(strExp.val);
            if (dt == null) {
                this.addError2s("var '%s': unsupported date value '%s'", assignExp.var.name(), strExp.val);
                return null;
            } else {
                return builder.buildFrom(dt);
            }
        } else {
            this.addError2s("var '%s': date value not number or string %s", assignExp.var.name(), "");
            return null;
        }
    }
    private DValue buildViaListValue(FullAssignmentExp assignExp, ViaExp viaExp, String typeName) {
        List<DValue> list = findViaMatches(assignExp, viaExp);
        if (list == null) {
            addError2s("Dcan't resolve via: %s: %s", viaExp.fieldExp.val, viaExp.valueExp.strValue());
            return null;
        }

        List<DValue> list2 = new ArrayList<>();
        for(DValue dval: list) {
            String idField = viaExp.fieldExp.name();
            DStructHelper helper = new DStructHelper(dval);
            DValue inner = helper.getField(idField);
            list2.add(inner);
        }

        DListType dtype = (DListType) packageHelper.findRegisteredType(typeName);
        ListBuilder builder = factory.createListBuilder(dtype);

        for(DValue element: list2) {
            builder.addElement(element);
        }

        DValue dval = builder.finish();
        if (dval == null) {
            //propogate
            for(ErrorMessage err: builder.getValidationErrors()) {
                String errType = err.getErrorType().name();
                addError2s("validation error: %s: %s", errType, err.getMessage());
            }

            addError2s("var '%s' - struct value builder failed %s", typeName, "unknown reason");
        }

        return dval;
    }
    private DValue buildListValue(ListAssignExp listExp, String typeName) {
        DListType dtype = (DListType) packageHelper.findRegisteredType(typeName);
        ListBuilder builder = factory.createListBuilder(dtype);
        String fieldType = dtype.getElementType().getName();
        fieldType = TypeInfo.parserTypeOf(fieldType);

        int index = 0;
        FullTypeExp fullType = doc.findType(typeName);
        if (fullType == null) {
            for(Exp exp: listExp.list) {

                if (exp instanceof ViaExp) {
                    ViaExp via = (ViaExp) exp;
                    viaHelper.adjustTypeIfNeeded(via, dtype);
                    FullAssignmentExp tmp = createFAE(String.format("list[%d]", index), fieldType, exp);
                    DValue element = this.buildValue(tmp);
                    if (element != null) {
                        if (element.getType() == null) { 
                            //hack!
                            if (via.extraViaExp != null) {
                                for(DValue gg: element.asList()) {
                                    DStructHelper h3 = new DStructHelper(gg);
                                    DValue j3 = h3.getField(via.extraViaExp.fieldExp.name());
                                    builder.addElement(maybeGenProxy(j3));
                                }
                            } else {
                                for(DValue gg: element.asList()) {
                                    builder.addElement(maybeGenProxy(gg));
                                }
                            }
                        } else {
                            builder.addElement(maybeGenProxy(element));
                        }
                    }
                } else {
                    FullAssignmentExp tmp = createFAE(String.format("list[%d]", index), fieldType, exp);
                    DValue element = this.buildValue(tmp);
                    if (element != null) {
                        builder.addElement(maybeGenProxy(element));
                    }
                }

                index++;
            }
        } else {
            FullListTypeExp fste = (FullListTypeExp) fullType;
            String xfieldType = fste.getListElementType();

            for(Exp exp: listExp.list) {
                FullAssignmentExp tmp = createFAE("listel?", xfieldType, exp);
                DValue element = this.buildValue(tmp);
                builder.addElement(maybeGenProxy(element));
                index++;
            }
        }

        DValue dval = builder.finish();
        if (dval == null) {
            //propogate
            for(ErrorMessage err: builder.getValidationErrors()) {
                String errType = err.getErrorType().name();
                addError2s("validation error: %s: %s", errType, err.getMessage());
            }

            addError2s("var '%s' - struct value builder failed %s", typeName, "unknown reason");
        }

        return dval;
    }

    private FullAssignmentExp createFAE(String varName, String typeName, Exp exp) {
        FullAssignmentExp assignExp = new FullAssignmentExp(new IdentExp(varName),
                new IdentExp(typeName), 
                exp);

        if (exp instanceof IdentExp) {
            IdentExp tmp = (IdentExp) exp;
            FullAssignmentExp referencedValue = this.doc.findValue(tmp.name());
            assignExp.type = referencedValue.type;
            assignExp.value = resolveRHS(referencedValue);
        }
        return assignExp;
    }

    private Exp resolveRHS(FullAssignmentExp typeExp) {
        if (typeExp == null) {
            this.addError2s("no type in resolveRHS", "", "");
            return null;
        }
        
        if (typeExp.value instanceof IntegerExp){
            IntegerExp tmp = (IntegerExp) typeExp.value;
            return tmp;
        }else if (typeExp.value instanceof StringExp){
            StringExp tmp = (StringExp) typeExp.value;
            return tmp;
        }else if (typeExp.value instanceof BooleanExp){
            BooleanExp tmp = (BooleanExp) typeExp.value;
            return tmp;
        }else if (typeExp.value instanceof NumberExp){
            NumberExp tmp = (NumberExp) typeExp.value;
            return tmp;
        }else if (typeExp.value instanceof LongExp){
            LongExp tmp = (LongExp) typeExp.value;
            return tmp;
        } else if (typeExp.value instanceof IdentExp) {
            IdentExp tmp = (IdentExp) typeExp.value;
            FullAssignmentExp referencedValue = this.doc.findValue(tmp.name());
            return resolveRHS(referencedValue); //recursion
        } else if (typeExp.value instanceof StructMemberAssignExp) {
            StructMemberAssignExp tmp = (StructMemberAssignExp) typeExp.value;
            FullAssignmentExp fake = new FullAssignmentExp(tmp.var, new IdentExp("?fakeType"), tmp.value);
            return resolveRHS(fake);
        } else if (typeExp.value instanceof StructAssignExp) {
            StructAssignExp tmp = (StructAssignExp) typeExp.value;
            return tmp;
        } else if (typeExp.value instanceof ViaExp) {
            ViaExp via = (ViaExp) typeExp.value;
            FullAssignmentExp fake = new FullAssignmentExp(via.fieldExp, new IdentExp("?fakeType"), via.valueExp);
            return resolveRHS(fake);
        } else {
            return null;
        }
    }

    private DValue handleVia(FullAssignmentExp assignExp, ViaExp via) {
        if (via.valueExp instanceof IdentExp) {
            if (currentStructBuilder != null) {
                //!!the via field must be before the via.  fix later!!
                DValue ff = currentStructBuilder.getAlreadyBuiltField(via.valueExp.strValue());
                if (ff != null) {
                    via.valueExp = new StringExp(ff.asString());
                }
            }
        }

        List<DValue> list = findViaMatches(assignExp, via);
        if (list == null || list.size() == 0) {
            String ss = (via.valueExp == null) ? "" : via.valueExp.strValue();
            addError2s("Acan't resolve via: %s: %s", via.fieldExp.val, ss);
            return null;
        } else if (list.size() > 1) {
            if (assignExp.var.val.startsWith("list[")) {
                DValue hack = new DValueImpl(null, list);
                return hack;
            }
            Integer n = list.size();
            addError2s("B can't resolve via: %s: %s matches", via.fieldExp.val, n.toString());
            return null;
        } else {
            DValue dval = list.get(0);
            return dval;
        }
    }

    private List<DValue> findViaMatches(FullAssignmentExp assignExp, ViaExp via) {
        DType tmp = packageHelper.findRegisteredType(via.typeExp.name());
        DStructType dtype = null;
        if (tmp instanceof DStructType) {
            dtype = (DStructType) tmp;
        } else {
            this.addError("not a struct type", assignExp);
            return null;
        }
        DType inner = dtype.getFields().get(via.fieldExp.name());
        if (inner == null) {
            this.addError("couldn't file field", assignExp);
            return null;
        }
        
        if (inner.isShape(Shape.ENUM)) {
        } else {
            via.valueExp = resolveRHS(assignExp);
        }
        List<DValue> list = viaFinder.findMatches(via);
        return list;
    }

    private DType getddType(IdentExp type) {
        if (TypeInfo.isPrimitiveType(type)) {
            String shapeTypeName = TypeInfo.toShapeType(type.name());
            return registry.getType(shapeTypeName); //eg. INTEGER_SHAPE
        } else {
            return packageHelper.findRegisteredType(type.name());
        }
    }

    private DValue buildStructValue(StructAssignExp structExp, String typeName) {
        DStructType dtype = (DStructType) packageHelper.findRegisteredType(typeName);
        StructBuilder builder = factory.createStructBuilder(dtype);
        this.currentStructBuilder = builder;

        int index = 0;
        FullTypeExp fullType = doc.findType(typeName);
        FullStructTypeExp fste = (FullStructTypeExp) fullType;
        List<TypePair> pairList = builder.getAllFields();

        for(Exp exp: structExp.list) {
            //the values must be in same order as members in the type

            //			StructMemberExp sme = fste.members.list.get(index);
            //			String fieldName = sme.var.name();
            //			String fieldType = sme.type.name();
            if (index >= pairList.size()) {
                addError2s("missing field: %s: %s", new Integer(index).toString(), exp.strValue());
            } else {
                String fieldName = pairList.get(index).name;
                String fieldType = TypeInfo.parserTypeOf(pairList.get(index).type.getName());

                ViaExp via = null;
                boolean isNull = false;
                if (exp instanceof ViaExp) {
                    via = (ViaExp) exp;
                    viaHelper.adjustTypeIfNeeded((ViaExp)exp, dtype, fieldName);
                } else if (exp instanceof ListAssignExp) {
                    viaHelper.adjustListTypeIfNeeded((ListAssignExp)exp, dtype, fieldName);
                } else if (exp instanceof IdentExp) {
                    isNull = exp.strValue().equals("null");
                } else if (exp instanceof StructMemberAssignExp) {
                    StructMemberAssignExp smae = (StructMemberAssignExp) exp;
                    if (smae.value.strValue().equals("null")) {
                        isNull = true;
                    }
                }

                if (isNull) {
                    if (dtype.fieldIsOptional(fieldName)) {
                        DValue member = null;
                        builder.addField(fieldName, member);
                    } else {
                        addError2s("can't assign null unless field is optional: %s%s", fieldName, "");
                    }
                } else {
                    FullAssignmentExp tmp = new FullAssignmentExp(new IdentExp(fieldName),
                            new IdentExp(fieldType), 
                            exp);
                    DValue member = this.buildValue(tmp);
                    
                    if (via != null && via.extraViaExp != null) {
                        DStructHelper h3 = new DStructHelper(member);
                        DValue j3 = h3.getField(via.extraViaExp.fieldExp.name());
                        builder.addField(fieldName, j3);
                    } else {
                        builder.addField(fieldName, member);
                    }
                }
                //if is StructMemberAssignExp then use name to find struct member
                //				FullAssignmentExp fullExp = null; //new FullAssignmentExp(varname, typename, val);
            }
            index++;
        }

        DValue dval = builder.finish();
        if (dval == null) {
            //propogate
            for(ErrorMessage err: builder.getValidationErrors()) {
                String errType = err.getErrorType().name();
                addError2s("validation error: %s: %s", errType, err.getMessage());
            }

            addError2s("var '%s' - struct value builder failed %s", typeName, "unknown reason");
        }

        this.currentStructBuilder = null; //reset
        
        //all structs should be proxy
        dval = maybeGenProxy(dval);
        return dval;
    }
}