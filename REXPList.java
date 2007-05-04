package org.rosuda.REngine;

public class REXPList extends REXPVector {
	private RList payload;
	
	public REXPList(RList list) {
		super();
		payload=(list==null)?new RList():list;
	}

	public REXPList(RList list, REXPList attr) {
		super(attr);
		payload=(list==null)?new RList():list;
	}
	
	public int length() { return payload.size(); }

	public boolean isList() { return true; }
	public RList asList() { return payload; }
	
	public String toString() {
		return super.toString()+(asList().isNamed()?"named":"");
	}	
}
