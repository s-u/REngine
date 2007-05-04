package org.rosuda.REngine;

public class REXP {
	protected REXPList attr;
	
	public REXP() { }
	public REXP(REXPList attr) { this.attr=attr; }

	// type checks
	public boolean isString() { return false; }
	public boolean isNumeric() { return false; }
	public boolean isInteger() { return false; }
	public boolean isNull() { return false; }
	public boolean isFactor() { return false; }
	public boolean isList() { return false; }
	public boolean isLogical() { return false; }
	public boolean isEnvironment() { return false; }
	public boolean isLanguage() { return false; }
	public boolean isSymbol() { return false; }
	public boolean isVector() { return false; }
	public boolean isRaw() { return false; }
	
	// basic accessor methods
	public String[] asStringArray() throws REXPMismatchException { throw new REXPMismatchException(this, "String"); }
	public int[] asIntegerArray() throws REXPMismatchException { throw new REXPMismatchException(this, "int"); }
	public double[] asDoubleArray() throws REXPMismatchException { throw new REXPMismatchException(this, "double"); }
	public byte[] asByteArray() throws REXPMismatchException { throw new REXPMismatchException(this, "byte"); }
	public RList asList() throws REXPMismatchException { throw new REXPMismatchException(this, "list"); }

	// convenience accessor methods
	public int asInteger() throws REXPMismatchException { int[] i = asIntegerArray(); return i[0]; }
	public double asDouble() throws REXPMismatchException { double[] d = asDoubleArray(); return d[0]; }
	public String asString() throws REXPMismatchException { String[] s = asStringArray(); return s[0]; }
	
	// methods common to all REXPs
	public REXP getAttribute(String name) {
		final REXPList a = _attr();
		if (a==null || !a.isList()) return null;
		return a.asList().at(name);
	}
	
	public boolean hasAttribute(String name) {
		final REXPList a = _attr();
		return (a!=null && a.isList() && a.asList().at(name)!=null);
	}
	
	public int[] dim() {
		try {
			return hasAttribute("dim")?_attr().asList().at("dim").asIntegerArray():null;
		} catch (REXPMismatchException me) {
		}
		return null;
	}
	
	public boolean inherits(String klass) {
		if (!hasAttribute("class")) return false;
		try {
			String c[] = getAttribute("class").asStringArray();
			if (c != null) {
				int i = 0;
				while (i < c.length) {
					if (c[i]!=null && c[i].equals(klass)) return true;
					i++;
				}
			}
		} catch (REXPMismatchException me) {
		}
		return false;
	}

	/** attr should never be used directly incase the REXP implements a lazy
		access (e.g. via a reference) */
	public REXPList _attr() { return attr; }
	
	public String toString() {
		return super.toString()+((attr!=null)?"+":"");
	}
	
	//======= complex convenience methods
	/** returns the content of the REXP as a matrix of doubles (2D-array: m[rows][cols]). This is the same form as used by popular math packages for Java, such as 
		JAMA. This means that following leads to desired results:<br>
        <code>Matrix m=new Matrix(c.eval("matrix(c(1,2,3,4,5,6),2,3)").asDoubleMatrix());</code>
        @return 2D array of doubles in the form double[rows][cols] or <code>null</code> if the contents is no 2-dimensional matrix of doubles */
    public double[][] asDoubleMatrix() throws REXPMismatchException {
		double[] ct = asDoubleArray();
        REXP dim = getAttribute("dim");
        if (dim==null) throw new REXPMismatchException(this, "matrix (dim attribute missing)");
        int[] ds = dim.asIntegerArray();
        if (ds.length!=2) throw new REXPMismatchException(this, "matrix (wrong dimensionality)");
		int m = ds[0], n = ds[1];
        double[][] r=new double[m][n];
        // R stores matrices as matrix(c(1,2,3,4),2,2) = col1:(1,2), col2:(3,4)
        // we need to copy everything, since we create 2d array from 1d array
        int i=0,k=0;
        while (i<n) {
            int j=0;
            while (j<m) {
                r[j++][i]=ct[k++];
            }
            i++;
        }
        return r;
    }
	
	
	//======= tools
	
	public static REXP createDataFrame(RList l) throws REXPMismatchException {
		if (l==null || l.size()<1) throw new REXPMismatchException(new REXPList(l), "data frame (must have dim>0)");
		if (!(l.at(0) instanceof REXPVector)) throw new REXPMismatchException(new REXPList(l), "data frame (contents must be vectors)");
		REXPVector fe = (REXPVector) l.at(0);
		return
		new REXPGenericVector(l,
							  new REXPList(
									new RList(
										   new REXP[] {
											   new REXPString("data.frame"),
											   new REXPString(l.keys()),
											   new REXPInteger(new int[] { REXPInteger.NA, -fe.length() })
										   },
										   new String[] {
											   "class",
											   "names",
											   "row.names"
										   })));
	}
}
