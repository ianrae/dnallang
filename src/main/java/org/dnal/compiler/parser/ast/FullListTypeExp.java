package org.dnal.compiler.parser.ast;

import java.util.List;

public class FullListTypeExp extends FullTypeExp {
	public IdentExp elementType; //list<int>

	public FullListTypeExp(int pos, IdentExp varname, IdentExp typename, IdentExp elementType, List<RuleExp> ruleList) {
		super(pos, varname, typename, ruleList);
		this.elementType = elementType;
	}
	@Override
	public String strValue() {
		return super.strValue();
	}
	
	public String getListElementType() {
		String s = elementType.name();
		int pos = s.indexOf('<');
		int pos2 = s.lastIndexOf('>');
		String elType = s.substring(pos + 1, pos2);
		return elType;
	}
}