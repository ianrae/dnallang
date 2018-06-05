package org.dnal.compiler.core.generator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang.StringUtils;
import org.dnal.compiler.core.BaseTest;
import org.dnal.compiler.dnalgenerate.ASTToDNALGenerator;
import org.dnal.compiler.et.XErrorTracker;
import org.dnal.compiler.parser.FullParser;
import org.dnal.compiler.parser.ast.Exp;
import org.dnal.compiler.parser.error.ErrorTrackingBase;
import org.dnal.compiler.parser.error.TypeInfo;
import org.dnal.core.DListType;
import org.dnal.core.DMapType;
import org.dnal.core.DStructType;
import org.dnal.core.DType;
import org.dnal.core.DTypeRegistry;
import org.dnal.core.DValue;
import org.dnal.core.Shape;
import org.dnal.core.nrule.NRule;
import org.dnal.core.repository.World;
import org.junit.Test;

public class NewGeneratorTests extends BaseTest {
	
	public static class NewOutputGenerator {
	    public List<String> outputL = new ArrayList<>();
		public boolean generateTypes = false;
		public boolean generateValues = false;
		
		public void structType(DStructType dtype) {
			if (!generateTypes) {
				return;
			}
		}
		public void enumType(DStructType enumType) {
			if (!generateTypes) {
				return;
			}
		}
		public void listType(DListType listType) {
			if (!generateTypes) {
				return;
			}
			// TODO Auto-generated method stub
			
		}
		public void mapType(DMapType mapType) {
			if (!generateTypes) {
				return;
			}
			// TODO Auto-generated method stub
			
		}
		public void scalarType(DType dtype) {
			if (!generateTypes) {
				return;
			}
			String typeName = TypeInfo.parserTypeOf(dtype.getName());
			String parentName = TypeInfo.parserTypeOf(dtype.getBaseType().getName());
			String rulesStr = getRuleStr(dtype);
			if (! StringUtils.isEmpty(rulesStr)) {
				rulesStr = String.format(" %s ", rulesStr);
			}
			String s = String.format("type %s %s%send", typeName, parentName, rulesStr);
			outputL.add(s);
		}
		private String getRuleStr(DType dtype) {
			StringJoiner joiner = new StringJoiner(" ");
            for(NRule rule: dtype.getRawRules()) {
                String ruleText = rule.getRuleText();
                joiner.add(ruleText); 
            }

            return joiner.toString();
		}
		public void topLevelValue(String varName, DValue dval) {
			if (!generateValues) {
				return;
			}
			
			String typeName = TypeInfo.parserTypeOf(dval.getType().getName());
			String valueStr = getValueStr(dval);
			String s = String.format("let %s %s = %s", varName, typeName, valueStr);
			
			outputL.add(s);
		}
		private String getValueStr(DValue dval) {
			if (dval.getObject() == null) {
				return "null";
			}
			
			String s = null;
			DType dtype = dval.getType();
			if (dtype.isScalarShape()) {
				switch (dval.getType().getShape()) {
					case BOOLEAN:
						s = Boolean.valueOf(dval.asBoolean()).toString();
						break;
					case DATE:
						s = Long.valueOf(dval.asDate().getTime()).toString(); //??use sdf formatter??
						break;
					case INTEGER:
						s = Integer.valueOf(dval.asInt()).toString();
						break;
					case LONG:
						s = Long.valueOf(dval.asLong()).toString();
						break;
					case NUMBER:
						s = Double.valueOf(dval.asNumber()).toString();
						break;
					case STRING:
						//add code to use either ' or "!!
						s = String.format("'%s'", dval.asString());
						break;
					case ENUM:
						s = doEnum(dval, dtype);
					default:
						break;
				}
			} else if (dtype.isListShape()) {
				DListType listType = (DListType) dtype;
				s = doList(dval, listType);
			} else if (dtype.isStructShape()) {
				DStructType structType = (DStructType) dtype;
				s = doStruct(dval, structType);
			} else if (dtype.isMapShape()) {
				DMapType mapType = (DMapType) dtype;
				s = doMap(dval, mapType);
			}
			return s;
		}
		private String doMap(DValue dval, DMapType mapType) {
			StringJoiner joiner = new StringJoiner(", ");
			//!!should fields be in alpha order?
			for(String fieldName: dval.asMap().keySet()) {
				DValue inner = dval.asMap().get(fieldName);
				String s = getValueStr(inner);
				String fieldStr = String.format("%s:%s", fieldName, s);
				joiner.add(fieldStr);
			}
			return String.format("{%s}", joiner.toString());
		}
		private String doStruct(DValue dval, DStructType structType) {
			StringJoiner joiner = new StringJoiner(", ");
			//!!should fields be in alpha order?
			for(String fieldName: dval.asStruct().getFieldNames()) {
				DValue inner = dval.asStruct().getField(fieldName);
				String s = getValueStr(inner);
				String fieldStr = String.format("%s:%s", fieldName, s);
				joiner.add(fieldStr);
				
			}
			return String.format("{%s}", joiner.toString());
		}
		private String doEnum(DValue dval, DType dtype) {
			DStructType enumType = (DStructType) dtype;
			return dval.asString();
		}
		private String doList(DValue dval, DListType listType) {
			StringJoiner joiner = new StringJoiner(", ");
			for(DValue inner: dval.asList()) {
				String s = getValueStr(inner);
				joiner.add(s);
				
			}
			return String.format("[%s]", joiner.toString());
		}
	}
	
	public static class NewDNALGeneratePhase extends ErrorTrackingBase {
	    private DTypeRegistry registry;
	    private World world;

	    public NewDNALGeneratePhase(XErrorTracker et, DTypeRegistry registry, World world) {
	        super(et);
	        this.registry = registry;
	        this.world = world;
	    }
	    
	    public boolean generate(NewOutputGenerator visitor) {
	        boolean b = false;
	        try {
	            b = doGenerate(visitor);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return b;
	    }

	    public boolean doGenerate(NewOutputGenerator visitor) throws Exception {
	        List<DType> orderedTypeList = registry.getOrderedList();

	        for(DType dtype: orderedTypeList) {
	            if (TypeInfo.isBuiltIntype(dtype.getName())) {
	                continue;
	            }
	            
	            if (dtype.isStructShape()) {
	                DStructType fste = (DStructType) dtype;
	                visitor.structType(fste);
//	                visitor.startStructType(dtype.getName(), fste);
//	                //!!fix to be ordered
//	                for(String fieldName: fste.orderedList()) {
//	                    DType field = fste.getFields().get(fieldName);
//	                    visitor.structMember(fieldName, field);
//	                }
	            } else if (dtype.isShape(Shape.ENUM)) {  
	                DStructType structType = (DStructType) dtype;
	                visitor.enumType(structType);
//	                visitor.startEnumType(dtype.getName(), structType);
//	                for(String key: structType.orderedList()) {
//	                    DType elType = structType.getFields().get(key);
//	                    visitor.enumMember(key, elType);
//	                }
	            } else if (dtype instanceof DListType) {
	                DListType listType = (DListType) dtype;
	                visitor.listType(listType);
	            } else if (dtype instanceof DMapType) {
	            	DMapType mapType = (DMapType) dtype;
	                visitor.mapType(mapType);
	            } else {
	                visitor.scalarType(dtype);
	            }

//	            int index = 0;
//	            for(NRule rule: dtype.getRawRules()) {
//	                String ruleText = rule.getRuleText();
//	                visitor.rule(index++, ruleText, rule); //fix later!! need ruleText
//	            }

//	            visitor.endType(dtype.getName(), dtype);
	        }

	        List<String> orderedValueList = world.getOrderedList();
	        for(String valueName: orderedValueList) {
	            DValue dval = world.findTopLevelValue(valueName);
//	            doval(visitor, 0, valueName, dval, null);
	            visitor.topLevelValue(valueName, dval);
	        }


	        return areNoErrors();
	    }

//	    private void doval(OutputGenerator visitor, int indent, String valueName, DValue dval, DValue parentVal) throws Exception {
//
//	        if (dval == null) {
//	            //optional field
//	            visitor.value(valueName, null, parentVal);
//	        } else if (dval.getType().isStructShape()) {
//	            visitor.startStruct(valueName, dval);
//	            
//	            DStructHelper helper = new DStructHelper(dval);
//
//	            int index = 0;
//	            DStructType structType = (DStructType) dval.getType();
//	            for(String fieldName : structType.orderedList()) {
//	                DValue inner = helper.getField(fieldName);
//	                doval(visitor, indent+1, fieldName, inner, dval); //!recursion!
//	                index++;
//	            }
//	            visitor.endStruct(valueName, dval);
//	        } else if (dval.getType().isListShape()) {
//	            visitor.startList(valueName, dval);
//	            List<DValue> elementL = dval.asList();
//
//	            int index = 0;
//	            for(DValue el: elementL) {
//	                doval(visitor, indent+1, "", el, dval); //!recursion!
//	                index++;
//	            }
//	            visitor.endList(valueName, dval);
//	        } else if (dval.getType().isMapShape()) {
//	            visitor.startMap(valueName, dval);
//	            Map<String,DValue> map = dval.asMap();
//
//	            int index = 0;
//	            for(String key: map.keySet()) {
//	            	DValue el = map.get(key);            	
//	                doval(visitor, indent+1, key, el, dval); //!recursion!
//	                index++;
//	            }
//	            visitor.endMap(valueName, dval);
//	        } else {
////	          String shape = this.doc.getShape(valueExp.type);
////	          boolean isScalar = TypeInfo.isScalarType(new IdentExp(shape));
//	            visitor.value(valueName, dval, parentVal);
//	        }
//	    }
	}	
	
	
	
	@Test
	public void test() {
	    chkGen("type Foo boolean end let x Foo = false",  "let x Foo = false|", 2);
	    chkGen("let x boolean = true",  "let x boolean = true|");
	    chkGen("let x int = 44",  "let x int = 44|");
	    chkGen("let x long = 555666",  "let x long = 555666|");
	    chkGen("let x number = 3.14",  "let x number = 3.14|");
	    chkGen("let x string = 'abc def'",  "let x string = 'abc def'|");
		chkGen("let x date = '2017'",  "let x date = 1483246800000|");
	}
	@Test
	public void testList() {
		chkGen("let x list<int> = [44, 45]",  "let x list<int> = [44, 45]|");
		chkGen("let x list<int> = []",  "let x list<int> = []|");
	}
	
    @Test
    public void test1a() {
        chkGen("type Foo enum { RED, BLUE } end let x Foo = RED",  "let x Foo = RED|", 2);
    }
    
    @Test
    public void test1b() {
        chkGen("type Foo struct { name string, age int } end let x Foo = { 'amy', 33 }",  "let x Foo = {age:33, name:'amy'}|", 2);
    }
    @Test
    public void test2() {
        chkGen("let x list<int> = [44, 45]", "let x list<int> = [44, 45]|");
        chkGen("type Z list<int> end let x list<Z> = [[44, 45],[50, 51]]",  "let x list<Z> = [[44, 45], [50, 51]]|", 2);
    }
    @Test
    public void test3() {
        chkGen("type Z struct { x int, y int } end let x Z = { 15, 20 }", "let x Z = {x:15, y:20}|", 2);
        String s = "let x Z = {x:{a:100, b:101}, y:20}|";
        chkGen("type Inner struct { a int, b int } end type Z struct { x Inner, y int } end let x Z = { { 100, 101 }, 20 }", s, 3);
    }
    @Test
    public void test4() {
        String s = "let x Z = {x:[15, 16], y:20}|";
        chkGen("type L list<int> end type Z struct { x L, y int } end let x Z = { [15,16], 20 }", s, 3);
//        String s = "{'x':{'a':100,'b':101},'y':20}|";
//        chkGen("type Inner struct { a int b int } end type Z struct { x Inner y int } end let x Z = { { 100, 101 }, 20 }", s, 3);
    }
    
	@Test
	public void testListAny() {
		String src1 = "type Person struct { x string, y string } end ";
		src1 += "let joe Person = { 'aa', 'bb' } ";
		src1 += "type AnyList list<any> end ";
		src1 += "let people AnyList = [ joe, joe ] ";
        String s = "let joe Person = {x:'aa', y:'bb'}|let people AnyList = [{x:'aa', y:'bb'}, {x:'aa', y:'bb'}]|";
        chkGen(src1, s, 4);
	}    

    @Test
    public void test5() {
        String s = "type SizeMap map<int> end let z SizeMap = { x:33, y:34 }";
        chkGen(s, "let z SizeMap = {x:33, y:34}|", 2);
    }
    
	@Test
	public void testValueParseStructAny() {
		String src1 = "type Person struct { x string, y string } end ";
		src1 += "let joe Person = { x:'aa', y:'bb' } ";
		src1 += "type SizeMap map<any> end let z SizeMap = { 'com.x':joe }";
		
        String s = "let joe Person = {x:'aa', y:'bb'}|let z SizeMap = {com.x:{x:'aa', y:'bb'}}|";
        chkGen(src1, s, 4);
	}
    
	//---- types
	@Test
	public void testTypes() {
	    chkTypeGen("type Foo boolean end let x Foo = false",  "type Foo boolean end|", 2);
	    //type Foo int >= 5 end let x Foo = 14
	    chkTypeGen("type Foo int >= 100 end",  "type Foo int >= 100 end|", 1);
	}
    
    //------------------
	private void chkGen(String input, String expectedOutput) {
		chkGen(input, expectedOutput, 1);
	}
	private void chkGen(String input, String expectedOutput, int expectedSize) {
		doChkGen(input, expectedOutput, expectedSize, false, true);
	}
	private void chkTypeGen(String input, String expectedOutput, int expectedSize) {
		doChkGen(input, expectedOutput, expectedSize, true, false);
	}	
	
	private void doChkGen(String input, String expectedOutput, int expectedSize, boolean genTypes, boolean genValues) {
		parseAndGenDVals(input, expectedSize);

		World world = getContext().world;
        DTypeRegistry registry = getContext().registry;
		NewDNALGeneratePhase phase = new NewDNALGeneratePhase(getContext().et, registry, world);
		NewOutputGenerator visitor = new NewOutputGenerator();
		visitor.generateTypes = genTypes;
		visitor.generateValues = genValues;
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
