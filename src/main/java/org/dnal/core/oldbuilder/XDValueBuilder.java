package org.dnal.core.oldbuilder;

import java.util.ArrayList;
import java.util.List;

import org.dnal.core.DType;
import org.dnal.core.DValue;
import org.dnal.core.ErrorType;
import org.dnal.core.NewErrorMessage;

public abstract class XDValueBuilder {
	protected List<NewErrorMessage> valErrorList = new ArrayList<>();
	protected boolean finished;
	protected DValue newDVal;
	protected DType type;
	
	public abstract void buildFromString(String input);

	public boolean finish() {
		finished = true;
		onFinish();
		boolean ok = wasSuccessful();
		return ok;
	}
	
	protected abstract void onFinish();

	public boolean wasSuccessful() {
		return finished && valErrorList.isEmpty();
	}
	public List<NewErrorMessage> getValidationErrors() {
		return valErrorList;
	}
	public DValue getDValue() {
		return newDVal;
	}
	
	private void addError(NewErrorMessage err) {
	    this.valErrorList.add(err);
	}

	public void addParsingError(String msg) {
		addOldErrorMsgZ(ErrorType.PARSINGERROR, msg);
	}
	
    public void addOldErrorMsgZ(ErrorType errType, String message) {
        NewErrorMessage err = new NewErrorMessage();
        err.setErrorType(NewErrorMessage.Type.IO_ERROR); //!!
        err.setErrorName(errType.name());
        err.setFieldName("?");
        err.setMessage(message);
        err.setSrcFile("?");
        err.setTypeName("?");
        addError(err);
    	
    }
	
	protected void addNoDataError(String msg) {
		addOldErrorMsgZ(ErrorType.NODATA, msg);
	}
	protected void addWrongTypeError(String s) {
		addOldErrorMsgZ(ErrorType.WRONGTYPE, String.format("wrong type - %s", s));
	}
    protected void addNoDataError() {
    	addOldErrorMsgZ(ErrorType.NODATA, "no data");
    }
	protected void addDuplicateFieldError(String msg) {
		addOldErrorMsgZ(ErrorType.DUPLICATEFIELD, msg);
	}
	protected void addMissingFieldError(String msg) {
		addOldErrorMsgZ(ErrorType.MISSINGFIELD, msg);
	}
	protected void addUnknownFieldError(String msg) {
		addOldErrorMsgZ(ErrorType.UNKNOWNFIELD, msg);
	}
	protected void addRefError(String msg) {
		addOldErrorMsgZ(ErrorType.REFERROR, msg);
	}

	public DType getType() {
		return type;
	}
}