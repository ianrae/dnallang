package org.dnal.compiler.core.generator;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.dnal.compiler.core.BaseTest;
import org.dnal.compiler.dnalgenerate.ASTToDNALGenerator;
import org.dnal.compiler.generate.DNALGeneratePhase;
import org.dnal.compiler.generate.DNALValueVisitor;
import org.dnal.compiler.parser.FullParser;
import org.dnal.compiler.parser.ast.Exp;
import org.dnal.core.DTypeRegistry;
import org.dnal.core.repository.World;
import org.junit.Test;

public class DNALValueGeneratorTests extends BaseTest {
	
	@Test
	public void test() {
	    chkGen("type Foo boolean end let x Foo = false",  "let x Foo = false|", 2);
	    chkGen("let x int = 44",  "let x int = 44|");
	    chkGen("let x long = 555666",  "let x long = 555666|");
	    chkGen("let x number = 3.14",  "let x number = 3.14|");
	    chkGen("let x string = 'abc def'",  "let x string = 'abc def'|");
		chkGen("let x date = '2017'",  "let x date = 1483246800000|");
		chkGen("let x list<int> = [44, 45]",  "let x list<int> = [44, 45]|");
	}
    
    @Test
    public void test1a() {
        chkGen("type Foo enum { RED, BLUE } end let x Foo = RED",  "let x Foo = RED|", 2);
    }

    @Test
    public void test1b() {
        chkGen("type Foo struct { name string, age int } end let x Foo = { 'amy', 33 }",  "let x Foo = {name:'amy', age:33}|", 2);
    }

    @Test
    public void test2() {
        chkGen("let x list<int> = [44, 45]", "let x list<int> = [44, 45]|");
        chkGen("type Z list<int> end let x list<Z> = [[44, 45],[50, 51]]",  "let x list<Z> = [[44, 45], [50, 51]]|", 2);
    }
    @Test
    public void test3() {
        chkGen("type Z struct { x int, y int } end let x Z = { 15, 20 }", "let x Z = {x:15, y:20}|", 2);
        String s = "let x Z = {{a:100, b:101}, y:20}|";
        chkGen("type Inner struct { a int, b int } end type Z struct { x Inner, y int } end let x Z = { { 100, 101 }, 20 }", s, 3);
    }
    @Test
    public void test4() {
        String s = "let x Z = {[15, 16], y:20}|";
        chkGen("type L list<int> end type Z struct { x L, y int } end let x Z = { [15,16], 20 }", s, 3);
//        String s = "{'x':{'a':100,'b':101},'y':20}|";
//        chkGen("type Inner struct { a int b int } end type Z struct { x Inner y int } end let x Z = { { 100, 101 }, 20 }", s, 3);
    }

    //------------------
	private void chkGen(String input, String expectedOutput) {
		chkGen(input, expectedOutput, 1);
	}
	
	private void chkGen(String input, String expectedOutput, int expectedSize) {
		ASTToDNALGenerator dnalGenerator = parseAndGenDVals(input, expectedSize);

		World world = getContext().world;
        DTypeRegistry registry = getContext().registry;
		DNALGeneratePhase phase = new DNALGeneratePhase(getContext().et, registry, world);
		DNALValueVisitor visitor = new DNALValueVisitor();
		boolean b = phase.generate(visitor);
		assertEquals(true, b);
		String output = flatten(visitor.outputL);
		log("output: " + output);
		
		assertEquals(expectedOutput, output);
	}

	private ASTToDNALGenerator parseAndGenDVals(String input, int expectedSize) {
		log("doing: " + input);
		List<Exp> list = FullParser.fullParse(input);
		assertEquals(expectedSize, list.size());

		ASTToDNALGenerator generator = createASTGenerator();
		boolean b = generator.generate(list);
		assertEquals(true, b);
		return generator;
	}

	private String flatten(List<String> L) {
		StringBuffer sb = new StringBuffer();
		for(String s: L) {
			sb.append(s);
			sb.append("|");
		}
		return sb.toString();
	}


	private void log(String s) {
		System.out.println(s);
	}
}