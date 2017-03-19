package org.dnal.compiler.parser.ast;

import org.codehaus.jparsec.Token;

public class ViewMemberExp implements Exp {
	
	public IdentExp left;
	public IdentExp leftType;
	public IdentExp right;
	public ViewDirection direction;

	public ViewMemberExp(IdentExp left, IdentExp type, Token tok, IdentExp right) {
		this.left = left;
		this.leftType = type;
		this.right = right;
		String tokStr = getTokString(tok);
		this.direction = (tokStr.equals("->")) ? ViewDirection.OUTBOUND : ViewDirection.INBOUND;
	}
	private String getTokString(Token tok) {
		if (tok == null) {
			return "";
		} else {
			return tok.toString();
		}
	}
	
	public String strValue() {
		return left.val;
	}
}