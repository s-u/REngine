package org.rosuda.REngine;

/** Basic class representing an object of any type in R. Each type in R in represented by a specific subclass.
 <p>
 This class defines basic accessor methods (<tt>as</tt><i>XXX</i>), type check methods (<tt>is</tt><i>XXX</i>), gives access to attributes ({@link #getAttribute}, {@link #hasAttribute}) as well as several convenience methods. If a given method is not applicable to a particular type, it will throw the {@link REXPMismatchException} exception.
 <p>This root class will throw on any accessor call and returns <code>false</code> for all type methods. This allows subclasses to override accessor and type methods selectively.
 */
public class REXP {
	/** attribute list. This attribute should never be accessed directly. */
	protected REXPList attr;

	/** public root contrsuctor, same as <tt>new REXP(null)</tt> */
	public REXP() { }
	/** public root constructor
	 @param attr attribute list object (can be <code>null</code> */
	public REXP(REXPList attr) { this.attr=attr; }

	// type checks
	public boolean isString() { return false; }
	public boolean isNumeric() { return false; }
	public boolean isInteger() { return false; }
	public boolean isNull() { return false; }
	public boolean isFactor() { return false; }
	public boolean isList() { return false; }
	public boolean isPairList() { return false; }
	public boolean isLogical() { return false; }
	public boolean isEnvironment() { return false; }
	public boolean isLanguage() { return false; }
	public boolean isExpression() { return false; }
	public boolean isSymbol() { return false; }
	public boolean isVector() { return false; }
	public boolean isRaw() { return false; }
	public boolean isComplex() { return false; }
	public boolean isRecursive() { return false; }
	public boolean isReference() { return false; }

	// basic accessor methods
	/** returns the contents as an array of Strings (if supported by the represented object) */
	public String[] asStrings() throws REXPMismatchException { throw new REXPMismatchException(this, "String"); }
	/** returns the contents as an array of integers (if supported by the represented object) */
	public int[] asIntegers() throws REXPMismatchException { throw new REXPMismatchException(this, "int"); }
	/** returns the contents as an array of doubles (if supported by the represented object) */
	public double[] asDoubles() throws REXPMismatchException { throw new REXPMismatchException(this, "double"); }
	/** returns the contents as an array of bytes (if supported by the represented object) */
	public byte[] asBytes() throws REXPMismatchException { throw new REXPMismatchException(this, "byte"); }
	/** returns the contents as a (named) list (if supported by the represented object) */
	public RList asList() throws REXPMismatchException { throw new REXPMismatchException(this, "list"); }
	/** returns the contents as a factor (if supported by the represented object) */
	public RFactor asFactor() throws REXPMismatchException { throw new REXPMismatchException(this, "factor"); }

	/** returns the length of a vector object. Note that we use R semantics here, i.e. a matrix will have a length of <i>m * n</i> since it is represented by a single vector (see {@link #dim} for retrieving matrix and multidimentional-array dimensions).
	 * @return length (number of elements) in a vector object
	 * @throws REXPMismatchException if this is not a vector object */
	public int length() throws REXPMismatchException { throw new REXPMismatchException(this, "vector"); }

	// convenience accessor methods
	/** convenience method corresponding to <code>asIntegers()[0]</code> */
	public int asInteger() throws REXPMismatchException { int[] i = asIntegers(); return i[0]; }
	/** convenience method corresponding to <code>asDoubles()[0]</code> */
	public double asDouble() throws REXPMismatchException { double[] d = asDoubles(); return d[0]; }
	/** convenience method corresponding to <code>asStrings()[0]</code> */
	public String asString() throws REXPMismatchException { String[] s = asStrings(); return s[0]; }
	
	// methods common to all REXPs
	
	/** retrieve an attribute of the given name from this object
	 * @param name attribute name
	 * @return attribute value or <code>null</code> if the attribute does not exist */
	public REXP getAttribute(String name) {
		final REXPList a = _attr();
		if (a==null || !a.isList()) return null;
		return a.asList().at(name);
	}
	
	/** checks whether this obejct has a given attribute
	 * @param name attribute name
	 * @return <code>true</code> if the attribute exists, <code>false</code> otherwise */
	public boolean hasAttribute(String name) {
		final REXPList a = _attr();
		return (a!=null && a.isList() && a.asList().at(name)!=null);
	}
	
	
	// helper methods common to all REXPs
	
	/** returns dimensions of the object (as determined by the "<code>dim</code>" attribute)
	 * @return an array of integers with corresponding dimensions or <code>null</code> if the object has no dimension attribute */
	public int[] dim() {
		try {
			return hasAttribute("dim")?_attr().asList().at("dim").asIntegers():null;
		} catch (REXPMismatchException me) {
		}
		return null;
	}
	
	/** determines whether this object inherits from a given class in tha same fashion as the <code>inherits()</code> function in R does (i.e. ignoring S4 inheritance)
	 * @param klass class name
	 * @return <code>true</code> if this object is of the class <code>klass</code>, <code>false</code> otherwise */
	public boolean inherits(String klass) {
		if (!hasAttribute("class")) return false;
		try {
			String c[] = getAttribute("class").asStrings();
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

	/** this method allows a limited access to object's attributes. {@link #getAttribute} should be used instead to access specific attributes. Note that the {@link #attr} attribute should never be used directly incase the REXP implements a lazy access (e.g. via a reference)
	    @return list of attributes or <code>null</code> if the object has no attributes
	 */
	public REXPList _attr() { return attr; }
	
	public String toString() {
		return super.toString()+((attr!=null)?"+":"");
	}
	
	/** returns representation that it useful for debugging (e.g. it includes attributes) */
	public String toDebugString() {
		return (attr!=null)?(("<"+attr.toDebugString()+">")+super.toString()):super.toString();
	}
	
	//======= complex convenience methods
	/** returns the content of the REXP as a matrix of doubles (2D-array: m[rows][cols]). This is the same form as used by popular math packages for Java, such as 
		JAMA. This means that following leads to desired results:<br>
        <code>Matrix m=new Matrix(c.eval("matrix(c(1,2,3,4,5,6),2,3)").asDoubleMatrix());</code>
        @return 2D array of doubles in the form double[rows][cols] or <code>null</code> if the contents is no 2-dimensional matrix of doubles */
    public double[][] asDoubleMatrix() throws REXPMismatchException {
		double[] ct = asDoubles();
        REXP dim = getAttribute("dim");
        if (dim==null) throw new REXPMismatchException(this, "matrix (dim attribute missing)");
        int[] ds = dim.asIntegers();
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
	/** creates a data frame object from a list object using integer row names
	 *  @param l a (named) list of vectors ({@link REXPVector} subclasses), each element corresponds to a column and all elements must have the same length
	 *  @return a data frame object
	 *  @throws REXPMismatchException if the list is empty or any of the elements is not a vector */
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
	
	/** specifies how many items of a vector or list will be displayed in {@link #toDebugString} */
	public static int maxDebugItems = 32;
}
