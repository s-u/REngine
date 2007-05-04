package org.rosuda.REngine;

public class REXPNull extends REXP {
	public REXPNull() { super(); }
	public REXPNull(REXPList attr) { super(attr); }
	
	public boolean isNull() { return true; }
	public boolean isList() { return true; } // NULL is a list
	public RList asList() { return new RList(); }
}
