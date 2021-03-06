package org.dnal.compiler.generate.old;

import java.util.Stack;
import java.util.StringJoiner;

import org.dnal.compiler.parser.error.TypeInfo;
import org.dnal.core.DType;
import org.dnal.core.DValue;

public class OldDNALValueVisitor extends OldValueGeneratorVisitor {
	private static class StringPair {
		public String name;
		public String typeName;
	}
	
    private Stack<OldDNALValueVisitor> genStack = new Stack<>();
    private Stack<OldDNALValueVisitor.StringPair> nameStack = new Stack<>();
	
	@Override
	public void value(String name, DValue dval, DValue parentVal) throws Exception {
		if (dval == null) {
			return;
		}
		String s = null;
		DType dtype = dval.getType();
		switch(dtype.getShape()) {
		case BOOLEAN:
			s = Boolean.valueOf(dval.asBoolean()).toString();
			break;
		case DATE:
			s = Long.valueOf(dval.asDate().getTime()).toString();
			break;
		case ENUM:
			s = dval.asString();
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
			s = String.format("'%s'", dval.asString());
			break;
		default:
			break;
		}
		
		if (s != null) {
			if (parentVal == null) {
				String typeName = TypeInfo.parserTypeOf(dtype.getName());
				String str = String.format("let %s %s = %s", name, typeName, s);
				outputL.add(str);
			} else if (parentVal.getType().isStructShape() || parentVal.getType().isMapShape()){
				OldDNALValueVisitor gen = genStack.peek();
				String str = String.format("%s:%s", name, s);
				gen.outputL.add(str);
			} else {
				OldDNALValueVisitor gen = genStack.peek();
				gen.outputL.add(s);
			}
		}
	}

	@Override
	public void startStruct(String name, DValue dval) throws Exception {
		OldDNALValueVisitor.StringPair pair = new StringPair();
		pair.name = name;
		pair.typeName = TypeInfo.parserTypeOf(dval.getType().getName());
		nameStack.push(pair);
		OldDNALValueVisitor gen = new OldDNALValueVisitor();
		genStack.push(gen);
	}

	@Override
	public void startList(String name, DValue dval) throws Exception {
		OldDNALValueVisitor.StringPair pair = new StringPair();
		pair.name = name;
		pair.typeName = TypeInfo.parserTypeOf(dval.getType().getName());
		nameStack.push(pair);
		OldDNALValueVisitor gen = new OldDNALValueVisitor();
		genStack.push(gen);
	}

	@Override
	public void endStruct(String name, DValue value) throws Exception {
		OldDNALValueVisitor gen = genStack.pop();
		OldDNALValueVisitor.StringPair pair = nameStack.pop();
		StringBuilder sb = new StringBuilder();
		int index = 0;
		for(String s: gen.outputL) {
			if (index > 0) {
				sb.append(',');
				sb.append(' ');
			}
			sb.append(s);
			index++;
		}
		
		if (genStack.isEmpty()) {
			String str = String.format("let %s %s = {%s}", pair.name, pair.typeName, sb.toString());
			outputL.add(str);
		} else {
			String str = String.format("{%s}", sb.toString());
			OldDNALValueVisitor parentgen = genStack.peek();
			parentgen.outputL.add(str);
		}
	}

	@Override
	public void endList(String name, DValue value) throws Exception {
		OldDNALValueVisitor gen = genStack.pop();
		OldDNALValueVisitor.StringPair pair = nameStack.pop();
		StringBuilder sb = new StringBuilder();
		int index = 0;
		for(String s: gen.outputL) {
			if (index > 0) {
				sb.append(',');
				sb.append(' ');
			}
			sb.append(s);
			index++;
		}
		
		if (genStack.isEmpty()) {
			String str = String.format("let %s %s = [%s]", pair.name, pair.typeName, sb.toString());
			outputL.add(str);
		} else {
			String str = String.format("[%s]", sb.toString());
			OldDNALValueVisitor parentgen = genStack.peek();
			parentgen.outputL.add(str);
		}
	}

	@Override
	public void startMap(String name, DValue value) throws Exception {
		OldDNALValueVisitor.StringPair pair = new StringPair();
		pair.name = name;
		pair.typeName = TypeInfo.parserTypeOf(value.getType().getName());
		nameStack.push(pair);
		OldDNALValueVisitor gen = new OldDNALValueVisitor();
		genStack.push(gen);
	}

	@Override
	public void endMap(String name, DValue value) throws Exception {
		OldDNALValueVisitor gen = genStack.pop();
		OldDNALValueVisitor.StringPair pair = nameStack.pop();
		StringJoiner joiner = new StringJoiner(", ");
		for(String s: gen.outputL) {
			joiner.add(s);
		}
		
		if (genStack.isEmpty()) {
			String str = String.format("let %s %s = {%s}", pair.name, pair.typeName, joiner.toString());
			outputL.add(str);
		} else {
			String str = String.format("{%s}", joiner.toString());
			OldDNALValueVisitor parentgen = genStack.peek();
			parentgen.outputL.add(str);
		}
	}
}