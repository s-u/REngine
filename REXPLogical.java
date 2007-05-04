package org.rosuda.REngine;

public class REXPLogical extends REXPVector {
	private byte[] payload;
	
	public final byte NA = 2;
	
	public REXPLogical(byte[] load) {
		super();
		payload=(load==null)?new byte[0]:load;
	}

	public REXPLogical(byte[] load, REXPList attr) {
		super(attr);
		payload=(load==null)?new byte[0]:load;
	}
	
	public REXPLogical(boolean[] load, REXPList attr) {
		super(attr);
		if (load==null) {
			payload=new byte[0];
		} else {
			payload=new byte[load.length];
			int i = 0;
			while (i < load.length) {
				payload[i] = (byte) (load[i]?1:0);
				i++;
			}
		}
	}

	public int length() { return payload.length; }

	public boolean isLogical() { return true; }

	public int[] asIntegers() {
		int[] a = new int[payload.length];
		int i = 0;
		while (i < payload.length) { a[i] = (int) payload[i]; i++; }
		return a;
	}

	public byte[] asBytes() { return payload; }

	public String[] asStrings() {
		String[] s = new String[payload.length];
		int i = 0;
		while (i < payload.length) { s[i] = (payload[i]==0)?"false":((payload[i]==1)?"true":null); i++; }
		return s;
	}
	
	public boolean[] isTrue() {
		boolean a[] = new boolean[payload.length];
		int i = 0;
		while (i < a.length) { a[i] = (payload[i]==1); i++; }
		return a;
	}

	public boolean[] isFalse() {
		boolean a[] = new boolean[payload.length];
		int i = 0;
		while (i < a.length) { a[i] = (payload[i]==0); i++; }
		return a;
	}

	public boolean[] isNA() {
		boolean a[] = new boolean[payload.length];
		int i = 0;
		while (i < a.length) { a[i] = (payload[i]==2); i++; }
		return a;
	}
}
