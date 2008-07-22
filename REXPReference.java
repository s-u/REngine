package org.rosuda.REngine;

/** this class represents a reference (proxy) to an R object.
 <p>
 The reference semantics works by calling {@link #resolve()} (which in turn uses {@link REngine#resolveReference(REXP)} on itself) whenever any methods are accessed. The implementation is not finalized yat and may change as we approach the JRI interface which is more ameanable to reference-style access. Subclasses are free to implement more efficient implementations. */
public class REXPReference extends REXP {
	/** engine which will be used to resolve the reference */
	REngine eng;
	/** an opaque (optional) handle */
	Object handle;

	/** create an external REXP reference using given engine and handle. The handle value is just an (optional) identifier not used by the implementation directly. */
	public REXPReference(REngine eng, Object handle) {
		super();
		this.eng = eng;
		this.handle = handle;
	}
	
	/** resolve the external REXP reference into an actual REXP object. */
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
	public boolean isVector() { return resolve().isVector(); }
	public boolean isRaw() { return resolve().isRaw(); }
	public boolean isComplex() { return resolve().isComplex(); }
	public boolean isRecursive() { return resolve().isRecursive(); }

	// basic accessor methods
	public String[] asStrings() throws REXPMismatchException { return resolve().asStrings(); }
	public int[] asIntegers() throws REXPMismatchException { return resolve().asIntegers(); }
	public double[] asDoubles() throws REXPMismatchException { return resolve().asDoubles(); }
	public RList asList() throws REXPMismatchException { return resolve().asList(); }
	public RFactor asFactor() throws REXPMismatchException { return resolve().asFactor(); }

	public int length() throws REXPMismatchException { return resolve().length(); }

	public REXPList _attr() { return resolve()._attr(); }
	
	public Object getHandle() { return handle; }
	
	public String toString() {
		return super.toString()+"{eng="+eng+",h="+handle+"}";
	}
}
