package org.rosuda.REngine;

public abstract class REXP {
    public REXP(String[] s) {};
    public REXP(int[] i) {};
    public REXP(double[] d) {};
    
    public abstract String[] asStringArray();
    public abstract int[] asIntArray();
    public abstract double[] asDoubleArray();
    public abstract REXP[] asREXPArray();

    /* the following methods are convenience methods
       based on the above abstract methods */
    public REXP(String s) { this(new String[]{s}); }
    public REXP(int i) { this(new int[]{i}); }
    public REXP(double d) { this(new double[]{d}); }
    
    /* the following methods are implemented on the basis of arrays
       the real implementation should replace those by more efficient
       implementations where possible */
    public String asString() {
	String[] s = asStringArray();
	return (s==null||s.length<1)?null:s[0];
    }
}
