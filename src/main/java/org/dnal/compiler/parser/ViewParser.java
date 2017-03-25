package org.dnal.compiler.parser;

import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Token;
import org.dnal.compiler.parser.ast.Exp;
import org.dnal.compiler.parser.ast.IdentExp;
import org.dnal.compiler.parser.ast.ViewExp;
import org.dnal.compiler.parser.ast.ViewMemberExp;

public class ViewParser extends ParserBase {
	
	public static Parser<Token> direction() {
		return Parsers.or(term("<-"), term("->")); 
	}
    
	public static Parser<ViewMemberExp> member() {
		return Parsers.sequence(VarParser.ident().many().sepBy(term(".")), direction(), VarParser.ident(), VarParser.ident(),
				(List<List<IdentExp>> left, Token tok, IdentExp right, IdentExp type) -> new ViewMemberExp(left, tok, right, type));
	}
	
	public static Parser<ViewExp> viewMembers() {
		return Parsers.between(term("{"), member().many(), term("}")).
				map(new org.codehaus.jparsec.functors.Map<List<ViewMemberExp>, ViewExp>() {
					@Override
					public ViewExp map(List<ViewMemberExp> arg0) {
						return new ViewExp(arg0);
					}
				});
	}
	
	public static Parser<ViewExp> viewHdr() {
		return Parsers.sequence(VarParser.ident(), direction(), VarParser.ident(),
				(IdentExp varType, Token tok, IdentExp varName) -> new ViewExp(varType, tok, varName));
	}
	
	public static Parser<ViewExp> outputView() {
		return Parsers.sequence(term("outview"), viewHdr(), viewMembers(), VarParser.doEnd(),
				(Token tok, ViewExp view, ViewExp view2, Exp exp) -> new ViewExp(true, view, view2));
	}
	public static Parser<ViewExp> inputView() {
		return Parsers.sequence(term("inview"), viewHdr(), viewMembers(), VarParser.doEnd(),
				(Token tok, ViewExp view, ViewExp view2, Exp exp) -> new ViewExp(false, view, view2));
	}
	public static Parser<ViewExp> view03() {
		return Parsers.or(inputView(), outputView());
	}

	
}