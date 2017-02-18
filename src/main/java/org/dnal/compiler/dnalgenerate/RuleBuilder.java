package org.dnal.compiler.dnalgenerate;

import java.util.Date;

import org.dnal.compiler.nrule.LenRule;
import org.dnal.compiler.parser.ast.ComparisonRuleExp;
import org.dnal.core.DStructType;
import org.dnal.core.DType;
import org.dnal.core.Shape;
import org.dnal.core.nrule.CompareRule;
import org.dnal.core.nrule.EqRule;
import org.dnal.core.nrule.NRule;
import org.dnal.core.nrule.virtual.StructMember;
import org.dnal.core.nrule.virtual.VirtualDataItem;
import org.dnal.core.nrule.virtual.VirtualDate;
import org.dnal.core.nrule.virtual.VirtualInt;
import org.dnal.core.nrule.virtual.VirtualLong;
import org.dnal.core.nrule.virtual.VirtualNumber;
import org.dnal.core.nrule.virtual.VirtualPseudoLen;
import org.dnal.core.nrule.virtual.VirtualString;

public class RuleBuilder {
    private DType dtype;
    
    public RuleBuilder(DType type) {
        this.dtype = type;
    }
    
    public boolean isCompatibleType(ComparisonRuleExp exp) {
        
        if (exp.val != null) {
            return dtype.isShape(Shape.INTEGER) || dtype.isShape(Shape.LONG);
        } else if (exp.zval != null) {
            return dtype.isShape(Shape.NUMBER);
        } else if (exp.strVal != null) {
            return dtype.isShape(Shape.STRING); // || dtype.isShape(Shape.ENUM);
        } else if (exp.longVal != null) {
            return dtype.isShape(Shape.LONG) || dtype.isShape(Shape.DATE);
        } else if (exp.identVal != null) {
            return dtype.isShape(Shape.ENUM);
        } else {
            return false;
        }
    }
    public boolean isCompatibleMemberType(ComparisonRuleExp exp) {
        VirtualDataItem vs = createVirtual(exp, true);
        StructMember sm = (StructMember) vs;
        
        DStructType structType = (DStructType) dtype;
        String fieldName = sm.getFieldName();
        DType elType =  structType.getFields().get(fieldName);

        if (exp.val != null) {
            return elType.isShape(Shape.INTEGER) || elType.isShape(Shape.LONG);
        } else if (exp.zval != null) {
            return elType.isShape(Shape.NUMBER);
        } else if (exp.strVal != null) {
            return elType.isShape(Shape.STRING) || elType.isShape(Shape.DATE);
        } else if (exp.longVal != null) {
            return elType.isShape(Shape.LONG) || elType.isShape(Shape.DATE);
        } else if (exp.identVal != null) {
            return elType.isShape(Shape.ENUM);
        } else {
            return false;
        }
    }

    public NRule buildCompare(ComparisonRuleExp exp, boolean isMember) {
        VirtualDataItem vs = createVirtual(exp, isMember);
        
        if (exp.val != null) {
            return doBuildIntCompare(exp, (VirtualInt)vs);
        } else if (exp.zval != null) {
            return doBuildNumberCompare(exp, (VirtualNumber)vs);
        } else if (exp.strVal != null) {
            if (dtype.isShape(Shape.STRING)) {
                return doBuildStringCompare(exp, (VirtualString)vs);
            } else {
                return doBuildDateCompare(exp, (VirtualDate)vs);
            }
        } else if (exp.longVal != null) {
            if (dtype.isShape(Shape.LONG)) {
                return doBuildLongCompare(exp, (VirtualLong) vs);
            } else {
                return doBuildDateCompare(exp, (VirtualDate)vs);
            }
        } else {
            return null; //!!
        }
    }
    
    private VirtualDataItem createVirtual(ComparisonRuleExp exp, boolean isMember) {
        VirtualDataItem vs;
        if (isMember) {
            vs = VirtualFactory.createMember(exp, dtype);
            StructMember sm = (StructMember) vs;
            sm.setFieldName(getFieldName(exp));
        } else {
            vs = VirtualFactory.create(exp, dtype);
        }
        return vs;
    }
    private VirtualPseudoLen createVirtualPseudoLen(ComparisonRuleExp exp, boolean isMember, String fieldName) {
        VirtualPseudoLen vs = VirtualFactory.createPseudoLen(exp, isMember);
        if (isMember) {
            StructMember sm = (StructMember) vs;
            sm.setFieldName(fieldName);
        }
        return vs;
    }
    
//    public NRule buildIntCompare(ComparisonRuleExp exp) {
//        VirtualInt vs = new VirtualInt();
//        return doBuildIntCompare(exp, vs);
//    }
    private NRule doBuildIntCompare(ComparisonRuleExp exp, VirtualInt vs) {
        NRule rule = new CompareRule<VirtualInt, Integer>(exp.strValue(), exp.op, vs, exp.val);
        rule.setRuleText(exp.strValue());
        return rule;
    }
    private NRule doBuildNumberCompare(ComparisonRuleExp exp, VirtualNumber vs) {
        NRule rule = new CompareRule<VirtualNumber, Double>(exp.strValue(), exp.op, vs, exp.zval);
        rule.setRuleText(exp.strValue());
        return rule;
    }
    private NRule doBuildStringCompare(ComparisonRuleExp exp, VirtualString vs) {
        NRule rule = new CompareRule<VirtualString, String>(exp.strValue(), exp.op, vs, exp.strVal);
        rule.setRuleText(exp.strValue());
        return rule;
    }
    private NRule doBuildLongCompare(ComparisonRuleExp exp, VirtualLong vs) {
        NRule rule = new CompareRule<VirtualLong, Long>(exp.strValue(), exp.op, vs, exp.longVal);
        rule.setRuleText(exp.strValue());
        return rule;
    }
    private NRule doBuildDateCompare(ComparisonRuleExp exp, VirtualDate vs) {
        Date dt = null;
        if (exp.longVal != null) {
            dt = new Date(exp.longVal);
        } else if (exp.strVal != null) {
            dt = DateFormatParser.parse(exp.strVal);
        }
        
        NRule rule = new CompareRule<VirtualDate, Date>(exp.strValue(), exp.op, vs, dt);
        rule.setRuleText(exp.strValue());
        return rule;
    }

    
    public NRule buildEq(ComparisonRuleExp exp, boolean isMember) {
        VirtualDataItem vs = createVirtual(exp, isMember);
        if (exp.val != null) {
            return doBuildIntEq(exp, (VirtualInt)vs);
        } else if (exp.zval != null) {
            return doBuildNumberEq(exp, (VirtualNumber)vs);
        } else if (exp.strVal != null) {
            if (dtype.isShape(Shape.STRING)) {
                return doBuildStringEq(exp, (VirtualString)vs);
            } else {
                return doBuildDateEq(exp, (VirtualDate)vs);
            }
        } else if (exp.longVal != null) {
            if (dtype.isShape(Shape.LONG)) {
                return doBuildLongEq(exp, (VirtualLong)vs);
            } else {
                return doBuildDateEq(exp, (VirtualDate)vs);
            }
        } else if (exp.identVal != null) {
            return doBuildEnumStringEq(exp, (VirtualString)vs);
        } else {
            return null; //!!
        }
    }
//    public NRule buildIntEq(ComparisonRuleExp exp) {
//        VirtualInt vs = new VirtualInt();
//        return doBuildIntEq(exp, vs);
//    }
    private NRule doBuildIntEq(ComparisonRuleExp exp, VirtualInt vs) {
        NRule rule = new EqRule<VirtualInt, Integer>(exp.strValue(), exp.op, vs, exp.val);
        rule.setRuleText(exp.strValue());
        return rule;
    }
    private NRule doBuildNumberEq(ComparisonRuleExp exp, VirtualNumber vs) {
        NRule rule = new EqRule<VirtualNumber, Double>(exp.strValue(), exp.op, vs, exp.zval);
        rule.setRuleText(exp.strValue());
        return rule;
    }
    private NRule doBuildStringEq(ComparisonRuleExp exp, VirtualString vs) {
        NRule rule = new EqRule<VirtualString, String>(exp.strValue(), exp.op, vs, exp.strVal);
        rule.setRuleText(exp.strValue());
        return rule;
    }
    private NRule doBuildEnumStringEq(ComparisonRuleExp exp, VirtualString vs) {
        NRule rule = new EqRule<VirtualString, String>(exp.strValue(), exp.op, vs, exp.identVal);
        rule.setRuleText(exp.strValue());
        return rule;
    }
    private NRule doBuildLongEq(ComparisonRuleExp exp, VirtualLong vs) {
        NRule rule = new EqRule<VirtualLong, Long>(exp.strValue(), exp.op, vs, exp.longVal);
        rule.setRuleText(exp.strValue());
        return rule;
    }
    private NRule doBuildDateEq(ComparisonRuleExp exp, VirtualDate vs) {
        Date dt = new Date(exp.longVal);
        NRule rule = new EqRule<VirtualDate, Date>(exp.strValue(), exp.op, vs, dt);
        rule.setRuleText(exp.strValue());
        return rule;
    }

    public LenRule buildPseudoLenCompare(ComparisonRuleExp exp, boolean isMember, String fieldName) {
        VirtualPseudoLen vs = this.createVirtualPseudoLen(exp, isMember, fieldName);
        NRule inner = doBuildIntCompare(exp, vs);
        LenRule newRule = new LenRule("len", vs);
        newRule.opRule = inner;
        newRule.setRuleText(String.format("%s %s", "len", inner.getRuleText()));
        return newRule;
    }
    public LenRule buildPseudoLenEq(ComparisonRuleExp exp, boolean isMember, String fieldName) {
        VirtualDataItem vs = this.createVirtualPseudoLen(exp, isMember, fieldName);
        NRule inner = doBuildIntEq(exp, (VirtualInt)vs);
        LenRule newRule = new LenRule("len", (VirtualInt) vs);
        newRule.setRuleText(String.format("%s %s", "len", inner.getRuleText()));
        newRule.opRule = inner;
        return newRule;
    }
    
    private String getFieldName(ComparisonRuleExp exp) {
        String fieldName = (exp.optionalArg == null) ? null : exp.optionalArg.name();
        return fieldName;
    }
}
