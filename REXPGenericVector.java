package org.rosuda.REngine;

public class REXPGenericVector extends REXPVector {
	private RList payload;
	
	public REXPGenericVector(RList list) {
		super();
		payload=(list==null)?new RList():list;
	}

	public REXPGenericVector(RList list, REXPList attr) {
		super(attr);
		payload=(list==null)?new RList():list;
	}
	
	public int length() { return payload.size(); }

	public boolean isList() { return true; }
	public RList asList() { return payload; }
}
