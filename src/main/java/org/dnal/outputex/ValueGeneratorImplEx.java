package org.dnal.outputex;

import java.util.ArrayList;
import java.util.List;

import org.dnal.compiler.parser.error.TypeInfo;
import org.dnal.core.DListType;
import org.dnal.core.DMapType;
import org.dnal.core.DStructType;
import org.dnal.core.DType;
import org.dnal.core.DValue;

public class ValueGeneratorImplEx implements ValueGeneratorEx {
    public List<String> outputL = new ArrayList<>();
	
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
		}
		return s;
	}
	private String doEnum(DValue dval, DType dtype) {
		return dval.asString();
	}
	
	@Override
	public void startStruct(String varName, String fieldName, DValue dval, DStructType structType, GeneratorContext genctx, int index) {
		if (varName != null) {
			String s = String.format("let %s %s = {", varName, structType.getName());
			outputL.add(s);
		} else {
			String comma = (index == 0) ? "" : ", ";
			if (fieldName == null) {
				String s = String.format("%s{", comma);
				appendCurrentList(s);
			} else {
				String s = String.format("%s%s:{", comma, fieldName);
				appendCurrentList(s);
			}
		}
	}
	@Override
	public void endStruct(DValue dval, DStructType structType, GeneratorContext genctx) {
		appendCurrentList("}");
	}
	@Override
	public void startList(String varName, String fieldName, DValue dval, DListType listType, GeneratorContext genctx, int index) {
		String typeName = listType.getName();
		
		if (varName != null) {
			String s = String.format("let %s %s = [", varName, typeName);
			outputL.add(s);
		} else {
			String comma = (index == 0) ? "" : ", ";
			if (fieldName == null) {
				String s = String.format("%s[", comma);
				appendCurrentList(s);
			} else {
				String s = String.format("%s%s:[", comma, fieldName);
				appendCurrentList(s);
			}
		}
	}
	
	private void appendCurrentList(String str) {
		String s = outputL.remove(outputL.size() - 1);
		outputL.add(s + str);
	}
	
	@Override
	public void endList(DValue dval, DListType listType, GeneratorContext genctx) {
		appendCurrentList("]");
	}
	@Override
	public void listElementValue(DValue dval, GeneratorContext genctx, int index) {
		String comma = (index == 0) ? "" : ", ";
		String s = String.format("%s%s", comma, this.getValueStr(dval));
		appendCurrentList(s);
	}
	@Override
	public void structMemberValue(String fieldName, DValue dval, GeneratorContext genctx, int index) {
		String comma = (index == 0) ? "" : ", ";
		String s = String.format("%s%s:%s", comma, fieldName, this.getValueStr(dval));
		appendCurrentList(s);
	}
	
	private String buildTypeName(DValue dval) {
		return buildTypeName(dval.getType());
	}
	private String buildTypeName(DType dtype) {
		String typeName = TypeInfo.parserTypeOf(dtype.getName());
		return typeName;
	}
	
	@Override
	public void scalarValue(String varName, String fieldName, DValue dval, GeneratorContext genctx) {
		String typeName = buildTypeName(dval);
		String s = String.format("let %s %s = %s", varName, typeName, this.getValueStr(dval));
		outputL.add(s);
	}
	@Override
	public void startMap(String varName, String fieldName,  DValue dval, DMapType mapType, GeneratorContext genctx, int index) {
		if (varName != null) {
			String s = String.format("let %s %s = {", varName, mapType.getName());
			outputL.add(s);
		} else {
			String comma = (index == 0) ? "" : ", ";
			if (fieldName == null) {
				String s = String.format("%s{", comma);
				appendCurrentList(s);
			} else {
				String s = String.format("%s%s:{", comma, fieldName);
				appendCurrentList(s);
			}
		}
	}
	@Override
	public void endMap(DValue dval, DMapType mapType, GeneratorContext genctx) {
		appendCurrentList("}");
	}
	@Override
	public void mapMemberValue(String key, DValue dval, GeneratorContext genctx, int index) {
		String comma = (index == 0) ? "" : ", ";
		String s = String.format("%s%s:%s", comma, key, this.getValueStr(dval));
		appendCurrentList(s);
	}
}