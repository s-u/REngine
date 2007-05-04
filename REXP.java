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
	
	// basic accessor methods
	public String[] asStringArray() throws REXPMismatchException { throw new REXPMismatchException(this, "String"); }
	public int[] asIntegerArray() throws REXPMismatchException { return null; }
	public double[] asDoubleArray() throws REXPMismatchException { return null; }
	public byte[] asByteArray() throws REXPMismatchException { return null; }
	public RList asList() throws REXPMismatchException { return null; }

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
}
