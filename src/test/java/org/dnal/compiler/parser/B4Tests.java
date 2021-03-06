package org.dnal.compiler.parser;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.dnal.compiler.parser.ast.BooleanExp;
import org.dnal.compiler.parser.ast.ComparisonOrRuleExp;
import org.dnal.compiler.parser.ast.ComparisonRuleExp;
import org.dnal.compiler.parser.ast.CustomRule;
import org.dnal.compiler.parser.ast.Exp;
import org.dnal.compiler.parser.ast.FullTypeExp;
import org.dnal.compiler.parser.ast.IdentExp;
import org.dnal.compiler.parser.ast.IntegerExp;
import org.dnal.compiler.parser.ast.IsaRuleExp;
import org.dnal.compiler.parser.ast.RangeExp;
import org.dnal.compiler.parser.ast.RuleExp;
import org.dnal.compiler.parser.ast.StringExp;
import org.junit.Test;

public class B4Tests {

	@Test
	public void test1() {
		chkRuleOperand("34", "34");
		chkRuleOperand("-34.5", "-34.5");
		chkRuleOperand("false", "false");
		chkRuleOperand("true", "true");
	}
	@Test
	public void test1a() {
		BooleanExp exp = (BooleanExp) parseRuleOperand("true");
		assertEquals(true, exp.val);
		exp = (BooleanExp) parseRuleOperand("false");
		assertEquals(false, exp.val);
	}
	

	//ruleFn
	@Test
	public void test10() {
		CustomRule exp =  parseRuleFn("a()");
		assertEquals("a", exp.ruleName);
		assertEquals(0, exp.argL.size());
		assertEquals(null, exp.fieldName);
		assertEquals(true, exp.polarity);
	}
	@Test
	public void test10a() {
		CustomRule exp = parseRuleFn("!a()");
		assertEquals("a", exp.ruleName);
		assertEquals(0, exp.argL.size());
		assertEquals(null, exp.fieldName);
		assertEquals(false, exp.polarity);
	}
	@Test
	public void test11() {
		CustomRule exp = parseRuleFn("x.a()");
		assertEquals("a", exp.ruleName);
		assertEquals(0, exp.argL.size());
		assertEquals("x", exp.fieldName);
		assertEquals(true, exp.polarity);
	}
	@Test
	public void test11a() {
		CustomRule exp = parseRuleFn("!x.a()");
		assertEquals("a", exp.ruleName);
		assertEquals(0, exp.argL.size());
		assertEquals("x", exp.fieldName);
		assertEquals(false, exp.polarity);
	}
	
	@Test
	public void test12() {
		CustomRule exp = parseRuleFn("x.a(3,'ab',false)");
		assertEquals("a", exp.ruleName);
		assertEquals(3, exp.argL.size());
		IntegerExp iexp = (IntegerExp) exp.argL.get(0);
		assertEquals(3, iexp.val.intValue());
		StringExp sexp = (StringExp) exp.argL.get(1);
		assertEquals("ab", sexp.val);
		
		assertEquals("x", exp.fieldName);
		assertEquals(true, exp.polarity);
	}
	@Test
	public void test12a() {
		CustomRule exp = parseRuleFn("!x.a(3,'ab',false)");
		assertEquals("a", exp.ruleName);
		assertEquals(3, exp.argL.size());
		IntegerExp iexp = (IntegerExp) exp.argL.get(0);
		assertEquals(3, iexp.val.intValue());
		StringExp sexp = (StringExp) exp.argL.get(1);
		assertEquals("ab", sexp.val);
		assertEquals("x", exp.fieldName);
		assertEquals(false, exp.polarity);
	}
	
	//ruleExpr
	@Test
	public void test20() {
		CustomRule exp =  (CustomRule) parseRuleExpr("a()");
		assertEquals("a", exp.ruleName);
		assertEquals(0, exp.argL.size());
		assertEquals(null, exp.fieldName);
		assertEquals(true, exp.polarity);
	}
	@Test
	public void test20a() {
		ComparisonRuleExp exp =  (ComparisonRuleExp) parseRuleExpr("x < 5");
		assertEquals("< 5", exp.strValue());
		assertEquals(5, exp.val.intValue());
		assertEquals("x", exp.optionalArg.strValue());
	}
	@Test
	public void test20b() {
		ComparisonRuleExp exp =  (ComparisonRuleExp) parseRuleExpr("x < 5.6");
		assertEquals("< 5.60000", exp.strValue());
		assertEquals(5.6, exp.zval, 0.001);
		assertEquals("x", exp.optionalArg.strValue());
	}
	@Test
	public void test20c() {
		ComparisonRuleExp exp =  (ComparisonRuleExp) parseRuleExpr("x < 'ab'");
		assertEquals("< ab", exp.strValue());
		assertEquals("ab", exp.strVal);
		assertEquals("x", exp.optionalArg.strValue());
	}
	@Test
	public void test20d() {
		ComparisonRuleExp exp =  (ComparisonRuleExp) parseRuleExpr("x < z");
		assertEquals("< z", exp.strValue());
		assertEquals("z", exp.identVal);
		assertEquals("x", exp.optionalArg.strValue());
	}
	
	@Test
	public void test20e() {
		ComparisonRuleExp exp =  (ComparisonRuleExp) parseRuleExpr("x.len() == 3");
		assertEquals("== 3", exp.strValue());
		assertEquals(3, exp.val.intValue());
		CustomRule cr = (CustomRule) exp.optionalArg;
		assertEquals("len", cr.ruleName);
		assertEquals("x", cr.fieldName);
	}
	
	
	@Test
	public void test21a() {
		ComparisonRuleExp exp =  (ComparisonRuleExp) parseRuleExpr("< 5");
		assertEquals("< 5", exp.strValue());
		assertEquals(5, exp.val.intValue());
		assertEquals(null, exp.optionalArg);
	}
	@Test
	public void test21c() {
		ComparisonRuleExp exp =  (ComparisonRuleExp) parseRuleExpr("< 'ab'");
		assertEquals("< ab", exp.strValue());
		assertEquals("ab", exp.strVal);
		assertEquals(null, exp.optionalArg);
	}
	@Test
	public void test21d() {
		ComparisonRuleExp exp =  (ComparisonRuleExp) parseRuleExpr("< z");
		assertEquals("< z", exp.strValue());
		assertEquals("z", exp.identVal);
		assertEquals(null, exp.optionalArg);
	}
	
	@Test
	public void test22() {
		ComparisonOrRuleExp exp =  (ComparisonOrRuleExp) parseRuleExpr("x < 5 or z < 10");
		assertEquals("< 5 or < 10", exp.strValue());
		assertEquals(5, exp.exp1.val.intValue());
		assertEquals("x", exp.exp1.optionalArg.strValue());
		assertEquals(10, exp.exp2.val.intValue());
		assertEquals("z", exp.exp2.optionalArg.strValue());
	}
	@Test
	public void test22a() {
		ComparisonOrRuleExp exp =  (ComparisonOrRuleExp) parseRuleExpr("< 5 or < 10");
		assertEquals("< 5 or < 10", exp.strValue());
		assertEquals(5, exp.exp1.val.intValue());
		assertEquals(null, exp.exp1.optionalArg);
		assertEquals(10, exp.exp2.val.intValue());
		assertEquals(null, exp.exp2.optionalArg);
	}
	
	@Test
	public void test23() {
		ComparisonOrRuleExp exp =  (ComparisonOrRuleExp) parseRuleExpr("x.a() < 5 or z < 10");
		assertEquals("< 5 or < 10", exp.strValue());
		assertEquals(5, exp.exp1.val.intValue());
		CustomRule cr = (CustomRule) exp.exp1.optionalArg;
		assertEquals("x", cr.fieldName);
		assertEquals("a", cr.ruleName);
		assertEquals(0, cr.argL.size());
		
		assertEquals(10, exp.exp2.val.intValue());
		IdentExp iexp = (IdentExp) exp.exp2.optionalArg;
		assertEquals("z", iexp.val);
	}
//	@Test
//	public void test23a() {
//		ComparisonOrRuleExp exp =  (ComparisonOrRuleExp) parseRuleExpr("z < x.a() or < 10");
//		assertEquals("< 5 or < 10", exp.strValue());
//		assertEquals(5, exp.exp1.val.intValue());
//		assertEquals(null, exp.exp1.optionalArg);
//		assertEquals(10, exp.exp2.val.intValue());
//		assertEquals(null, exp.exp2.optionalArg);
//	}

	@Test
	public void test30() {
		List<RuleExp> list = parseRuleMany("< 5, < 10");
		assertEquals(2, list.size());
		ComparisonRuleExp exp1 = (ComparisonRuleExp) list.get(0);
		assertEquals(5, exp1.val.intValue());
		assertEquals(null, exp1.optionalArg);
	}
	
	
	//--range
    @Test
    public void test40() {
        String src = "15..20";
        Exp exp = RuleParser.ruleRange().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
        RangeExp rexp = (RangeExp)exp;
        assertEquals(15, rexp.from.intValue());
        assertEquals(20, rexp.to.intValue());
    }
    @Test
    public void test40a() {
        String src = "15 ..20";
        Exp exp = RuleParser.ruleSpaceRange().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
        RangeExp rexp = (RangeExp)exp;
        assertEquals(15, rexp.from.intValue());
        assertEquals(20, rexp.to.intValue());
    }
    
	@Test
	public void test41() {
		FullTypeExp ax = (FullTypeExp) FullParser.parse02("type X int myrule(15..20) end");
		assertEquals("X", ax.var.val);
		assertEquals("int", ax.type.val);
		assertEquals(1, ax.ruleList.size());
		CustomRule rule = (CustomRule) ax.ruleList.get(0);
		assertEquals("myrule", rule.ruleName);
		RangeExp exp = (RangeExp) rule.argL.get(0);
		assertEquals("15..20", exp.strValue());
	}
	
  @Test
  public void test80() {
      FullTypeExp ax = (FullTypeExp) FullParser.parse02("type X int z isa Product.id end");
      assertEquals("X", ax.var.val);
      assertEquals("int", ax.type.val);
      assertEquals(1, ax.ruleList.size());
      IsaRuleExp rule = (IsaRuleExp) ax.ruleList.get(0);
      assertEquals("z", rule.fieldName);
      assertEquals("Product.id", rule.val);
  }
  @Test
  public void test81() {
      FullTypeExp ax = (FullTypeExp) FullParser.parse02("type X int isa Product.id end");
      assertEquals("X", ax.var.val);
      assertEquals("int", ax.type.val);
      assertEquals(1, ax.ruleList.size());
      IsaRuleExp rule = (IsaRuleExp) ax.ruleList.get(0);
      assertEquals(null, rule.fieldName);
      assertEquals("Product.id", rule.val);
  }
  
    
	
	
	
	//--helpers
	private void chkRuleOperand(String src, String expected) {
		Exp ax =  parseRuleOperand(src);
		assertEquals(expected, ax.strValue());
	}
	private Exp parseRuleOperand(String src) {
		Exp ax =  RuleParser.ruleOperand().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		return ax;
	}
	private CustomRule parseRuleFn(String src) {
		CustomRule ax =  RuleParser.ruleFn().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		return ax;
	}
	private RuleExp parseRuleExpr(String src) {
		RuleExp ax =  RuleParser.ruleExpr().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		return ax;
	}
	private List<RuleExp> parseRuleMany(String src) {
		List<RuleExp> ax =  RuleParser.ruleMany().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		return ax;
	}
}
