package org.rosuda.REngine;

public class REXPLanguage extends REXPList {
	public REXPLanguage(RList list) { super(list); }
	public REXPLanguage(RList list, REXPList attr) { super(list, attr); }
	
	public boolean isLanguage() { return true; }
}
