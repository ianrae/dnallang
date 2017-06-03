package org.dnal.api.bean2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.dnal.api.bean.BeanMethodCache;
import org.dnal.api.bean.DNALLoader;
import org.dnal.api.beancopier.BeanCopierContextKey;
import org.dnal.api.beancopier.BeanMethodInvoker;
import org.dnal.api.beancopier.BeanToDTypeBuilder;
import org.dnal.api.beancopier.FieldSpec;
import org.dnal.compiler.performance.PerfContinuingTimer;
import org.dnal.core.logger.Log;

public class ZBeanCopierContext {
	DNALLoader loader;
	public PerfContinuingTimer pctA = new PerfContinuingTimer();
	public PerfContinuingTimer pctB = new PerfContinuingTimer();
	public PerfContinuingTimer pctC = new PerfContinuingTimer();
	public PerfContinuingTimer pctD = new PerfContinuingTimer();
	public PerfContinuingTimer pctE = new PerfContinuingTimer();

	BeanMethodCache destSetterMethodCache;
	List<String> allDestFields;
	List<String> allSourceFields;
	BeanMethodCache destGetterMethodCache;
	private BeanMethodCache sourceGetterMethodCache;
	private BeanCopierContextKey contextKey;
	private BeanToDTypeBuilder builder;


	public ZBeanCopierContext() {
		loader = new DNALLoader();
		loader.initCompiler();
		builder = new BeanToDTypeBuilder(loader.getErrorTracker());
	}

	public boolean prepare(Object sourceObj, Object destObj, List<FieldSpec> fieldL) {
		this.contextKey = new BeanCopierContextKey(sourceObj, destObj, fieldL);
		try {
			if (! doPrepare(sourceObj, destObj, fieldL)) {
				return false;
			}
		} catch (Exception e) {
			addError("Exception:" + e.getMessage());
		}
		return ! areErrors();
	}

	private boolean doPrepare(Object sourceObj, Object destObj, List<FieldSpec> fieldL) throws Exception {
		pctE.start();
		BeanMethodInvoker finder = new BeanMethodInvoker();
		destSetterMethodCache = finder.getAllSetters(destObj.getClass());

		allDestFields = finder.getAllFields(destObj.getClass());
		allSourceFields = finder.getAllFields(sourceObj.getClass());
		List<String> destFieldList = new ArrayList<>();
		List<String> sourceFieldList = new ArrayList<>();
		//only the needed fields, and avoid duplicates
		for(FieldSpec field: fieldL) {
			if (allSourceFields.contains(field.srcField)) {
				if (! sourceFieldList.contains(field.srcField)) {
					sourceFieldList.add(field.srcField);
				}
			} else {
				addError(String.format("src can't find field '%s'", field.srcField));
			}

			if (allDestFields.contains(field.destField)) {
				if (! destFieldList.contains(field.destField)) {
					destFieldList.add(field.destField);
				}
			} else {
				addError(String.format("dest can't find field '%s'", field.destField));
			}
		}

		if (areErrors()) {
			return false;
		}

		destGetterMethodCache = finder.getGetters(destObj.getClass(), destFieldList);
		sourceGetterMethodCache = finder.getGetters(sourceObj.getClass(), sourceFieldList);

		String xName = destObj.getClass().getSimpleName();
		String dtoName = sourceObj.getClass().getSimpleName();
		String viewName =  dtoName + "View";
		String dnalGen = buildGeneratedTypes(sourceObj.getClass(), sourceFieldList, destObj.getClass(), destFieldList);
		String dnal = builder.buildDnalType(xName, destGetterMethodCache, destFieldList);
		String dnal2 = ""; //builder.buildDnalType(dtoName, sourceGetterMethodCache, sourceFieldList);
		String dnal3 = builder.buildDnalView(xName, viewName, destGetterMethodCache, sourceGetterMethodCache, destFieldList, sourceFieldList, fieldL);

		if (areErrors()) {
			return false;
		}
		
		//			XErrorTracker.logErrors = true;
		//			Log.debugLogging = true;
		String fullSource = String.format("%s\n %s\n %s\n %s\n", dnalGen, dnal, dnal2, dnal3);
		Log.log("fullsrc:" + fullSource);
		boolean b = loader.loadTypeDefinitionFromString(fullSource);
		if (! b) {
			Log.log("FAILED: " + fullSource); 
			return false;
		}
		pctE.end();

		return true;
	}
	
	private String buildGeneratedTypes(Class<?> srcClass, List<String> sourceFieldList, Class<?> destClass, List<String> destFieldList) {
		ZCreator zc = new ZCreator(loader.getErrorTracker());
		boolean b = zc.createForClass(srcClass, sourceFieldList);
		if (! b) {
			return "";
		}
		
		b = zc.createForClass(destClass, destFieldList);
		if (! b) {
			return "";
		}
		
		String s = "";
		List<FieldInfo> infos = zc.getGenList();
		for(FieldInfo finfo: infos) {
			if (finfo.isEnum) {
				s += builder.generateEnum(finfo.clazz);
			} else {
				s += "a";
			}
		}
		
		return s;
	}

	public Class<?> getListElementType(Method meth, Class<?> paramClass) {
		return builder.getListElementType(meth, paramClass);
	}

	private boolean areErrors() {
		return loader.getErrorTracker().areErrors();
	}

	private void addError(String message) {
		loader.getErrorTracker().addParsingError(message);
	}

	public boolean compareKeys(Object dto, Object x, List<FieldSpec> fieldL) {
		BeanCopierContextKey newKey = new BeanCopierContextKey(dto, x, fieldL);
		if (contextKey.equals(newKey)) {
			return true;
		} else {
			return false;
		}
	}

	public void clearErrors() {
		loader.getErrorTracker().clear();
	}
}