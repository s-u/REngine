package org.rosuda.REngine;

public class REXPDouble extends REXPVector {
	private double[] payload;
	
	public REXPDouble(double[] load) {
		super();
		payload=(load==null)?new double[0]:load;
	}

	public REXPDouble(double[] load, REXPList attr) {
		super(attr);
		payload=(load==null)?new double[0]:load;
	}
	
	public int length() { return payload.length; }

	public boolean isNumeric() { return true; }

	public double[] asDoubleArray() { return payload; }

	public int[] asIntegerArray() {
		int[] a = new int[payload.length];
		int i = 0;
		while (i < payload.length) { a[i] = (int) payload[i]; i++; }
		return a;
	}

	public String[] asStringArray() {
		String[] s = new String[payload.length];
		int i = 0;
		while (i < payload.length) { s[i] = ""+payload[i]; i++; }
		return s;
	}
	
	public boolean[] isNA() {
		boolean a[] = new boolean[payload.length];
		int i = 0; // FIXME: not quite true, will flag NaN as NA as well
		while (i < a.length) { a[i] = Double.isNaN(payload[i]); i++; }
		return a;
	}	
}
