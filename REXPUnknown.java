package org.rosuda.REngine;

public class REXPUnknown extends REXP {
	int type;
	public REXPUnknown(int type) { super(); this.type=type; }
	public REXPUnknown(int type, REXPList attr) { super(attr); this.type=type; }
	
	public int getType() { return type; }
	
	public String toString() {
		return super.toString()+"["+type+"]";
	}
}
