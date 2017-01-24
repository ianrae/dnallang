package com.github.ianrae.dnalparse.parser;

import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.functors.Tuple4;

import com.github.ianrae.dnalparse.parser.ast.ComparisonAndRuleExp;
import com.github.ianrae.dnalparse.parser.ast.ComparisonOrRuleExp;
import com.github.ianrae.dnalparse.parser.ast.ComparisonRuleExp;
import com.github.ianrae.dnalparse.parser.ast.CustomRule;
import com.github.ianrae.dnalparse.parser.ast.EnumExp;
import com.github.ianrae.dnalparse.parser.ast.EnumMemberExp;
import com.github.ianrae.dnalparse.parser.ast.Exp;
import com.github.ianrae.dnalparse.parser.ast.FullEnumTypeExp;
import com.github.ianrae.dnalparse.parser.ast.FullListTypeExp;
import com.github.ianrae.dnalparse.parser.ast.FullStructTypeExp;
import com.github.ianrae.dnalparse.parser.ast.FullTypeExp;
import com.github.ianrae.dnalparse.parser.ast.IdentExp;
import com.github.ianrae.dnalparse.parser.ast.IsaRuleExp;
import com.github.ianrae.dnalparse.parser.ast.RangeExp;
import com.github.ianrae.dnalparse.parser.ast.RuleExp;
import com.github.ianrae.dnalparse.parser.ast.StructExp;
import com.github.ianrae.dnalparse.parser.ast.StructMemberExp;

public class TypeParser extends ParserBase {
    
    public static Parser<IsaRuleExp> isaDecl() {
        return Parsers.sequence(VarParser.ident().optional(), term("isa"), 
                VarParser.ident().many().sepBy(term(".")), 
                (IdentExp exp, Token tok, List<List<IdentExp>> arg)
                -> new IsaRuleExp(exp, arg));
    }    
    
    public static Parser<IdentExp> optionalRuleArg() {
        return VarParser.ident().optional();
    }
    
	public static Parser<RuleExp> rule0() {
		return Parsers.sequence(optionalRuleArg(), 
		        Parsers.or(term("<"), term(">"), term(">="), term("<="), term("=="), term("!=")), 
		        ruleArg(), 
				(IdentExp optArg, Token optok, Exp numExp) -> new ComparisonRuleExp(optArg, optok.toString(), numExp));
	}
	public static Parser<RuleExp> ruleOr() {
		return Parsers.sequence(rule0(), term("or"), rule0(), 
				(Exp exp1, Token ortok, Exp exp2) -> new ComparisonOrRuleExp((ComparisonRuleExp)exp1, (ComparisonRuleExp)exp2));
	}
	public static Parser<RuleExp> ruleAnd() {
		return Parsers.sequence(rule0(), term("and"), rule0(), 
				(Exp exp1, Token ortok, Exp exp2) -> new ComparisonAndRuleExp((ComparisonRuleExp)exp1, (ComparisonRuleExp)exp2));
	}
	
//    private static Parser<Exp> numberArg() {
//        return TerminalParser.numberSyntacticParser
//        .map(new org.codehaus.jparsec.functors.Map<String, NumberExp>() {
//            @Override
//            public NumberExp map(String arg) {
//                Double nval = Double.parseDouble(arg);
//
//                return new NumberExp(nval);
//            }
//        });
//    }
	
	public static Parser<RangeExp> ruleRange() {
		return Parsers.sequence(intArg(), term(".."), intArg(), 
				(Exp exp1, Token ortok, Exp exp2) -> new RangeExp(exp1, exp2));
	}
//    public static Parser<RangeExp> ruleRange() {
//        return Parsers.sequence(VarParser.someNumberValueassign().followedBy(term(".")), intArg(), 
//                (Exp exp1, Exp exp2) -> new RangeExp(exp1, exp2));
//    }
	
	public static Parser<Exp> ruleArg() {
		return Parsers.or(VarParser.someNumberValueassign(), strArg(), VarParser.ident());
	}
	
	public static Parser<Token> not() {
	    return term("!").optional();
	}
	
	public static Parser<CustomRule> ruleCustom01() {
		return Parsers.sequence(not(), VarParser.ident(), term("("), ruleArg().many().sepBy(term(",")), term(")"), 
				(Token notToken, IdentExp exp1, Token tok, List<List<Exp>> arg, Token tok2) -> new CustomRule(exp1.name(), arg, (notToken == null) ? null : notToken.toString()));
	}
//	public static Parser<CustomRule> ruleCustom02() {
//		return Parsers.sequence(not(), VarParser.ident(), term("("), ruleRange(), term(")"), 
//				(Token notToken, IdentExp exp1, Token tok, RangeExp range, Token tok2) -> new CustomRule(exp1.name(), range, (notToken == null) ? null : notToken.toString()));
//	}
	public static Parser<CustomRule> ruleCustom() {
//	    return Parsers.or(ruleCustom02(), ruleCustom01());
		return Parsers.or(ruleCustom01());
	}
	
	public static Parser<RuleExp> rule() {
		return Parsers.or(ruleOr(), ruleAnd(), rule0(), ruleCustom(), isaDecl());
	}

	//type x int > 0 end
	public static Parser<FullTypeExp> type01() {
		return Parsers.or(term("type")).next(Parsers.tuple(VarParser.ident(), VarParser.ident(), rule().many(), VarParser.doEnd()))
				.map(new org.codehaus.jparsec.functors.Map<Tuple4<IdentExp, IdentExp, List<RuleExp>, Exp>, FullTypeExp>() {
					@Override
					public FullTypeExp map(Tuple4<IdentExp, IdentExp, List<RuleExp>, Exp> arg0) {
						List<RuleExp>cc = arg0.c;

						return new FullTypeExp(arg0.a, arg0.b, cc);
					}
				});
	}

	public static Parser<IdentExp> termStruct() {
		return term("struct").<IdentExp>retn(new IdentExp("struct"));
	}
    public static Parser<IdentExp> doStruct() {
        return Parsers.or(VarParser.ident(), termStruct());
    }

    public static Parser<Token> optionalOptionalArg() {
        return term("optional").optional();
    }
    public static Parser<Token> optionalUniqueArg() {
        return term("unique").optional();
    }
    
	public static Parser<StructMemberExp> structMembers00() {
		return Parsers.sequence(VarParser.ident(), Parsers.or(VarParser.ident(), listangle()), optionalOptionalArg(), optionalUniqueArg(),
				(IdentExp varName, IdentExp varType, Token opt, Token unique) -> new StructMemberExp(varName, varType, opt, unique));
	}

	public static Parser<StructExp> structMembers() {
		return Parsers.between(term("{"), structMembers00().many(), term("}")).
				map(new org.codehaus.jparsec.functors.Map<List<StructMemberExp>, StructExp>() {
					@Override
					public StructExp map(List<StructMemberExp> arg0) {
						return new StructExp(arg0);
					}
				});
	}

	//type Colour struct { x int y int } end
	public static Parser<FullStructTypeExp> typestruct01() {
		return Parsers.sequence(term("type"), VarParser.ident(), doStruct(), structMembers(), rule().many(),
				(Token tok, IdentExp varName, IdentExp struct, StructExp structMembers, List<RuleExp> rules) -> 
		new FullStructTypeExp(varName, struct, structMembers, rules)).followedBy(VarParser.doEnd());
	}
	
	//-----enum---
	public static Parser<IdentExp> doEnum() {
		return term("enum").<IdentExp>retn(new IdentExp("enum"));
	}

	public static Parser<EnumMemberExp> enumMembers00() {
		return Parsers.or(VarParser.ident()).
				map(new org.codehaus.jparsec.functors.Map<IdentExp, EnumMemberExp>() {
					@Override
					public EnumMemberExp map(IdentExp arg0) {
						return new EnumMemberExp(arg0, new IdentExp("string"));
					}
				});
	}

	public static Parser<EnumExp> enumMembers() {
		return Parsers.between(term("{"), enumMembers00().many(), term("}")).
				map(new org.codehaus.jparsec.functors.Map<List<EnumMemberExp>, EnumExp>() {
					@Override
					public EnumExp map(List<EnumMemberExp> arg0) {
						return new EnumExp(arg0);
					}
				});
	}

	//type Colour struct { x int y int } end
	public static Parser<FullEnumTypeExp> typeenum01() {
		return Parsers.sequence(term("type"), VarParser.ident(), doEnum(), enumMembers(), rule().many(),
				(Token tok, IdentExp varName, IdentExp struct, EnumExp structMembers, List<RuleExp> rules) -> 
		new FullEnumTypeExp(varName, struct, structMembers, rules)).followedBy(VarParser.doEnd());
	}
	
	public static Parser<IdentExp> listangle() {
		return Parsers.sequence(term("list"), term("<"), VarParser.ident(), term(">"),
				(Token tok1, Token tok2, IdentExp elementType, Token tok3) -> 
		new IdentExp(String.format("list<%s>", elementType.name())));
	}

	public static Parser<FullListTypeExp> typelist01() {
		return Parsers.sequence(term("type"), VarParser.ident(), 
				listangle(), rule().many(),
				(Token tok, IdentExp varName, 
				 IdentExp elementType, List<RuleExp> rules) -> 
		new FullListTypeExp(varName, new IdentExp("list"), elementType, rules)).followedBy(VarParser.doEnd());
	}
	
}