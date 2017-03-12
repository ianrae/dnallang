package org.dnal.compiler.nrule;

import org.dnal.api.impl.CompilerContext;
import org.dnal.compiler.dnalgenerate.ViaFinder;
import org.dnal.core.DStructType;
import org.dnal.core.DType;
import org.dnal.core.DValue;
import org.dnal.core.ErrorType;
import org.dnal.core.NewErrorMessage;
import org.dnal.core.logger.Log;
import org.dnal.core.nrule.NRuleBase;
import org.dnal.core.nrule.NRuleContext;

public class UniqueRule extends NRuleBase {
    
    private String fieldName;
    private CompilerContext context;

    public UniqueRule(String name, String fieldName, DType type, CompilerContext context) {
        super(name);
        this.fieldName = fieldName;
        this.context = context;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    @Override
    public boolean eval(DValue dval, NRuleContext ctx) {
        DStructType structType = (DStructType) dval.getType();
        DType innerType = structType.getFields().get(fieldName);
        if (innerType == null) {
            this.addRuleFailedError(ctx, this.getRuleText());
            return false;
        }
        
        boolean pass = false;
        switch(innerType.getShape()) {
        case INTEGER:
        case LONG:
        case STRING:
            pass = checkRule(structType, ctx);
            break;
        default:
            this.addRuleFailedError(ctx, this.getRuleText() + " - can only be used on fields of type int,long, or string");
            break;
        }
        
        return pass;
    }
    

    private boolean checkRule(DStructType structType, NRuleContext ctx) {
        ViaFinder finder = new ViaFinder(context.world, context.registry, context.et);
        boolean b = finder.calculateUnique(structType, fieldName);
        Log.log(String.format("AAAAAAAAAAAx %b", b));
    	
		if (!b) {
			String s = String.format("%s: %s", this.getName(), this.getRuleText());
			ctx.addErrorWithField(ErrorType.RULEFAIL, s, fieldName);
		}
        
        return b;
    }

    @Override
    protected boolean onEval(DValue dval, NRuleContext ctx) {
        return true;
    }
}