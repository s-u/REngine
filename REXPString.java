package org.rosuda.REngine;

public class REXPString extends REXPVector {
	private String[] payload;
	
	public REXPString(String load) {
		super();
		payload=new String[] { load };
	}

	public REXPString(String[] load) {
		super();
		payload=(load==null)?new String[0]:load;
	}

	public REXPString(String[] load, REXPList attr) {
		super(attr);
		payload=(load==null)?new String[0]:load;
	}
	
	public int length() { return payload.length; }

	public boolean isString() { return true; }

	public String[] asStringArray() {
		return payload;
	}
	
	public boolean[] isNA() {
		boolean a[] = new boolean[payload.length];
		int i = 0;
		while (i < a.length) { a[i] = (payload[i]==null); i++; }
		return a;
	}
}
