package org.dnal.compiler.parser;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.functors.Tuple4;
import org.dnal.compiler.parser.ast.ComparisonAndRuleExp;
import org.dnal.compiler.parser.ast.ComparisonOrRuleExp;
import org.dnal.compiler.parser.ast.ComparisonRuleExp;
import org.dnal.compiler.parser.ast.CustomRule;
import org.dnal.compiler.parser.ast.EnumExp;
import org.dnal.compiler.parser.ast.EnumMemberExp;
import org.dnal.compiler.parser.ast.Exp;
import org.dnal.compiler.parser.ast.FullEnumTypeExp;
import org.dnal.compiler.parser.ast.FullListTypeExp;
import org.dnal.compiler.parser.ast.FullStructTypeExp;
import org.dnal.compiler.parser.ast.FullTypeExp;
import org.dnal.compiler.parser.ast.IdentExp;
import org.dnal.compiler.parser.ast.IntegerExp;
import org.dnal.compiler.parser.ast.IsaRuleExp;
import org.dnal.compiler.parser.ast.ListAssignExp;
import org.dnal.compiler.parser.ast.RangeExp;
import org.dnal.compiler.parser.ast.RuleExp;
import org.dnal.compiler.parser.ast.RuleWithFieldExp;
import org.dnal.compiler.parser.ast.StructExp;
import org.dnal.compiler.parser.ast.StructMemberExp;

public class TypeParser extends ParserBase {
    
    public static Parser<IsaRuleExp> isaDecl() {
        return Parsers.sequence(VarParser.ident().optional(), term("isa"), 
                VarParser.ident().many().sepBy(term(".")), 
                (IdentExp exp, Token tok, List<List<IdentExp>> arg)
                -> new IsaRuleExp(exp, arg));
    }    
    
	/*
	 * The parser gets confused with "15..20" and sees "15.". The workaround is
	 * to accept 15. followed by another .
	 */
    public static Parser<Exp> intWithDotArg() {
        return TerminalParser.numberSyntacticParser
        .map(new org.codehaus.jparsec.functors.Map<String, IntegerExp>() {
            @Override
            public IntegerExp map(String arg) {
                if (arg != null && ! arg.endsWith(".")) {
                    throw new IllegalArgumentException("intWithDotArg: invalid " + arg);
                }
                Integer nval = Integer.parseInt(arg.substring(0, arg.length() - 1));

                return new IntegerExp(nval);
            }
        });
    }
	
//    //handles 15..20
//	public static Parser<RangeExp> ruleRange() {
//		return Parsers.sequence(intWithDotArg().followedBy(term(".")), intArg(), 
//				(Exp exp1, Exp exp2) -> new RangeExp(exp1, exp2));
//	}
//	
//	//handles 15 .. 20
//    public static Parser<RangeExp> ruleSpaceRange() {
//        return Parsers.sequence(intArg(), term(".."), intArg(), 
//                (Exp exp1, Token dotdot, Exp exp2) -> new RangeExp(exp1, exp2));
//    }
//	
//	public static Parser<Exp> ruleArg() {
//		return Parsers.or(VarParser.someNumberValueassign(), strArg(), VarParser.ident());
//	}
//	
//	public static Parser<Token> not() {
//	    return term("!").optional();
//	}
	
	

	//type x int > 0 end
	public static Parser<FullTypeExp> type01() {
		return Parsers.or(term("type")).next(Parsers.tuple(VarParser.ident(), VarParser.ident(), RuleParser.ruleMany(), VarParser.doEnd()))
				.map(new org.codehaus.jparsec.functors.Map<Tuple4<IdentExp, IdentExp, List<RuleExp>, Exp>, FullTypeExp>() {
					@Override
					public FullTypeExp map(Tuple4<IdentExp, IdentExp, List<RuleExp>, Exp> arg0) {
//						List<List<RuleExp>> ccx = arg0.c;
//						List<RuleExp> cc = new ArrayList<>();
//						for(List<RuleExp> sub: ccx) {
//							for(RuleExp re: sub) {
//								cc.add(re);
//							}
//						}

						return new FullTypeExp(arg0.a, arg0.b, arg0.c);
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
		return Parsers.sequence(term("type"), VarParser.ident(), doStruct(), structMembers(), RuleParser.ruleMany(),
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
		return Parsers.sequence(term("type"), VarParser.ident(), doEnum(), enumMembers(), RuleParser.ruleMany(),
				(Token tok, IdentExp varName, IdentExp struct, EnumExp structMembers, List<RuleExp> rules) -> 
		new FullEnumTypeExp(varName, struct, structMembers, rules)).followedBy(VarParser.doEnd());
	}
	
	public static final Parser.Reference<IdentExp> listangleRef = Parser.newReference();
	public static Parser<IdentExp> listangleinner() {
		return Parsers.or(listangleRef.lazy(), VarParser.ident());
	}
	public static Parser<IdentExp> listangle() {
		return Parsers.sequence(term("list"), term("<"), listangleinner(), term(">"),
				(Token tok1, Token tok2, IdentExp elementType, Token tok3) -> 
		new IdentExp(String.format("list<%s>", elementType.name())));
	}

	public static Parser<FullListTypeExp> typelist01() {
		return Parsers.sequence(term("type"), VarParser.ident(), 
				listangle(), RuleParser.ruleMany(),
				(Token tok, IdentExp varName, 
				 IdentExp elementType, List<RuleExp> rules) -> 
		new FullListTypeExp(varName, new IdentExp("list"), elementType, rules)).followedBy(VarParser.doEnd());
	}
	
}