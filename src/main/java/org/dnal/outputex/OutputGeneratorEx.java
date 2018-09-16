package org.dnal.outputex;

import org.dnal.core.DListType;
import org.dnal.core.DMapType;
import org.dnal.core.DStructType;
import org.dnal.core.DType;
import org.dnal.core.DValue;

public interface OutputGeneratorEx {

	void structType(DStructType dtype, String typeName, String parentTypeName);

	void enumType(DStructType enumType, String typeName);

	void listType(DListType listType, String typeName, String elementName);

	void mapType(DMapType mapType);

	void scalarType(DType dtype, String typeName, String parentName);

	//and values
	void startStruct(String varName, String fieldName, DValue dval, DStructType structType, GeneratorContext genctx, int index);
	void endStruct(DValue dval, DStructType structType, GeneratorContext genctx);

	void startList(String varName, String fieldName, DValue dval, DListType listType, GeneratorContext genctx, int index);
	void endList(DValue dval, DListType listType, GeneratorContext genctx);
	
	void startMap(String varName, String fieldName, DValue dval, DMapType mapType, GeneratorContext genctx, int index);
	void endMap(DValue dval, DMapType mapType, GeneratorContext genctx);
	
	void listElementValue(DValue dval, GeneratorContext genctx, int index);
	void structMemberValue(String fieldName, DValue dval, GeneratorContext genctx, int index);
	void mapMemberValue(String key, DValue dval, GeneratorContext genctx, int index);
	void scalarValue(String varName, DValue dval, GeneratorContext genctx);
}