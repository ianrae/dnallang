package org.dnal.outputex;

import org.dnal.core.DListType;
import org.dnal.core.DMapType;
import org.dnal.core.DStructType;
import org.dnal.core.DValue;

public interface ValueGeneratorEx {

	/**
	 * 
	 * @param varName   can be null. if non-null then is a top-level var
	 * @param fieldName can be null. if non-null then varName is null. 
	 * @param dval
	 * @param structType
	 * @param genctx
	 * @param index
	 */
	void startStruct(String varName, String fieldName, DValue dval, DStructType structType, GeneratorContext genctx, int index);
	void endStruct(DValue dval, DStructType structType, GeneratorContext genctx);

	/**
	 * 
	 * @param varName   can be null. if non-null then is a top-level var
	 * @param fieldName can be null. if non-null then varName is null. 
	 * @param dval
	 * @param structType
	 * @param genctx
	 * @param index
	 */
	void startList(String varName, String fieldName, DValue dval, DListType listType, GeneratorContext genctx, int index);
	void endList(DValue dval, DListType listType, GeneratorContext genctx);
	
	/**
	 * 
	 * @param varName   can be null. if non-null then is a top-level var
	 * @param fieldName can be null. if non-null then varName is null. 
	 * @param dval
	 * @param structType
	 * @param genctx
	 * @param index
	 */
	void startMap(String varName, String fieldName, DValue dval, DMapType mapType, GeneratorContext genctx, int index);
	void endMap(DValue dval, DMapType mapType, GeneratorContext genctx);
	
	void listElementValue(DValue dval, GeneratorContext genctx, int index);
	
	/**
	 * 
	 * @param fieldName never-null
	 * @param dval
	 * @param genctx
	 * @param index
	 */
	void structMemberValue(String fieldName, DValue dval, GeneratorContext genctx, int index);
	void mapMemberValue(String key, DValue dval, GeneratorContext genctx, int index);
	
	/**
	 * 
	 * @param varName   never-null
	 * @param dval
	 * @param genctx
	 */
	void scalarValue(String varName, DValue dval, GeneratorContext genctx);
}