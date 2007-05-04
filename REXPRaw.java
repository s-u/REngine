package org.rosuda.REngine;

public class REXPRaw extends REXPVector {
	private byte[] payload;
	
	public REXPRaw(byte[] load) {
		super();
		payload=(load==null)?new byte[0]:load;
	}

	public REXPRaw(byte[] load, REXPList attr) {
		super(attr);
		payload=(load==null)?new byte[0]:load;
	}
	
	public int length() { return payload.length; }

	public boolean isRaw() { return true; }

	public byte[] asBytes() { return payload; }
}
