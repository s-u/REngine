package org.rosuda.REngine;

public abstract class REXPVector extends REXP {
    public REXPVector() { super(); }
    public REXPVector(REXPList attr) {
	super(attr);
    }

	public abstract int length();
	public boolean isVector() { return true; }
	
	public String toString() {
		return super.toString()+"["+length()+"]";
	}
	
	public String toDebugString() {
		return super.toDebugString()+"["+length()+"]";
	}
}
