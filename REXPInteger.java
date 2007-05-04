package org.rosuda.REngine;

public class REXPInteger extends REXPVector {
	private int[] payload;
	
	public static final int NA = -2147483648;

	public REXPInteger(int load) {
		super();
		payload=new int[] { load };
	}
	
	public REXPInteger(int[] load) {
		super();
		payload=(load==null)?new int[0]:load;
	}

	public REXPInteger(int[] load, REXPList attr) {
		super(attr);
		payload=(load==null)?new int[0]:load;
	}
	
	public int length() { return payload.length; }

	public boolean isInteger() { return true; }
	public boolean isNumeric() { return true; }

	public int[] asIntegers() { return payload; }

	public double[] asDoubles() {
		double[] d = new double[payload.length];
		int i = 0;
		while (i < payload.length) { d[i] = (double) payload[i]; i++; }
		return d;
	}

	public String[] asStrings() {
		String[] s = new String[payload.length];
		int i = 0;
		while (i < payload.length) { s[i] = ""+payload[i]; i++; }
		return s;
	}
	
	public boolean[] isNA() {
		boolean a[] = new boolean[payload.length];
		int i = 0;
		while (i < a.length) { a[i] = (payload[i]==NA); i++; }
		return a;
	}	
}
