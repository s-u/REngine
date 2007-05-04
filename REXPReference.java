package org.rosuda.REngine;

public class REXPReference extends REXP {
	REngine eng;
	Object handle;
	
	public REXPReference(REngine eng, Object handle) {
		super();
		this.eng = eng;
		this.handle = handle;
	}
	
	public REXP resolve() {
		try {
			return eng.resolveReference(this);
		} catch(REngineException ee) {
			// FIXME: what to we do?
		}
		return null;
	}
	
	// type checks
	public boolean isString() { return resolve().isString(); }
	public boolean isNumeric() { return resolve().isNumeric(); }
	public boolean isInteger() { return resolve().isInteger(); }
	public boolean isNull() { return resolve().isNull(); }
	public boolean isFactor() { return resolve().isFactor(); }
	public boolean isList() { return resolve().isList(); }
	public boolean isLogical() { return resolve().isLogical(); }
	public boolean isEnvironment() { return resolve().isEnvironment(); }
	public boolean isLanguage() { return resolve().isLanguage(); }
	public boolean isSymbol() { return resolve().isSymbol(); }
	
	// basic accessor methods
	public String[] asStringArray() throws REXPMismatchException { return resolve().asStringArray(); }
	public int[] asIntegerArray() throws REXPMismatchException { return resolve().asIntegerArray(); }
	public double[] asDoubleArray() throws REXPMismatchException { return resolve().asDoubleArray(); }
	public RList asList() throws REXPMismatchException { return resolve().asList(); }
	
	public REXPList _attr() { return resolve()._attr(); }
	
	public Object getHandle() { return handle; }
	
	public String toString() {
		return super.toString()+"{eng="+eng+",h="+handle+"}";
	}
}
