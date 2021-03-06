package org.dnal.compiler.nrule;

import org.dnal.compiler.parser.ast.Exp;
import org.dnal.compiler.parser.ast.IntegerExp;
import org.dnal.compiler.parser.ast.LongExp;
import org.dnal.core.DValue;
import org.dnal.core.nrule.NRuleContext;
import org.dnal.core.nrule.virtual.VirtualInt;


public class InRule extends Custom1RuleBase<VirtualInt> { 
	
	public InRule(String name, VirtualInt arg1) {
		super(name, arg1);
	}

	@Override
	protected boolean onEval(DValue dval, NRuleContext ctx) {
		
		boolean found = false;
		for(Exp exp: crule.argL) {
			Integer n1 = getInt(exp);
			if (arg1.val.equals(n1)) {
				found = true;
				break;
			}
		}		
		return found;
	}
	
    private Integer getInt(Exp exp) {
        if (exp instanceof LongExp) {
            LongExp longExp = (LongExp) exp;
            return longExp.val.intValue();
        } else if (exp instanceof IntegerExp) {
            IntegerExp intExp = (IntegerExp) exp;
            return intExp.val;
        } else {
            return null;
        }
    }
}