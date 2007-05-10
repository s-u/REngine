package org.rosuda.REngine;

// REngine
// Copyright (C) 2007 Simon Urbanek
// --- for licensing information see LICENSE file in the original distribution ---

import java.util.*;

/** representation of a factor variable. In R there is no actual xpression
    type called "factor", instead it is coded as an int vector with a list
    attribute. The parser code of REXP converts such constructs directly into
    the RFactor objects and defines an own XT_FACTOR type 
    
    @version $Id$
*/    
public class RFactor {
    int ids[];
    String levels[];

    /** create a new, empty factor var */
    public RFactor() { ids=new int[0]; levels=new String[0]; }
    
    /** create a new factor variable, based on the supplied arrays.
		@param i array if IDs (0..v.length-1)
		@param v values - cotegory names
		@param copy copy above vaules or just retain them
		*/
    public RFactor(int[] i, String[] v, boolean copy) {
		if (i==null) i = new int[0];
		if (v==null) v = new String[0];
		if (copy) {
			ids=new int[i.length]; System.arraycopy(i,0,ids,0,i.length);
			levels=new String[v.length]; System.arraycopy(v,0,levels,0,v.length);
		} else {
			ids=i; levels=v;
		}
    }

	public RFactor(int[] i, String[] v) {
		this(i, v, true);
	}
	
    /** returns the level of a given case
		@param i case number
		@return name. may throw exception if out of range */
    public String at(int i) {
		int li = ids[i];
		return (li<0||li>levels.length)?null:levels[li];
    }

	/** returns <code>true</code> if the data contain the given level index */
	public boolean contains(int li) {
		int i = 0;
		while (i < ids.length) {
			if (ids[i] == li) return true;
			i++;
		}
		return false;
	}
	
	/** return <code>true</code> if the factor contains the given level (it is NOT the same as levelIndex==-1!) */
	public boolean contains(String name) {
		int li = levelIndex(name);
		if (li<0) return false;
		int i = 0;
		while (i < ids.length) {
			if (ids[i] == li) return true;
			i++;
		}
		return false;
	}
	
	/** count the number of occurences of a given level index */
	public int count(int levelIndex) {
		int i = 0;
		int ct = 0;
		while (i < ids.length) {
			if (ids[i] == levelIndex) ct++;
			i++;
		}
		return ct;
	}
	
	/** count the number of occurences of a given level name */
	public int count(String name) {
		return count(levelIndex(name));
	}
	
	/** return an array with level counts. */
	public int[] counts() {
		int[] c = new int[levels.length];
		int i = 0;
		while (i < ids.length) {
			final int li = ids[i];
			if (li>=0 && li<levels.length)
				c[li]++;
			i++;
		}
		return c;
	}
	
	/** return the index of a given level name or -1 if it doesn't exist */
	public int levelIndex(String name) {
		if (name==null) return -1;
		int i = 0;
		while (i < levels.length) {
			if (levels[i]!=null && levels[i].equals(name)) return i;
			i++;
		}
		return -1;
	}
	
	/** return the list of levels */
	public String[] levels() {
		return levels;
	}
	
	/** return the contents as integer indices */
	public int[] asIntegers() {
		return ids;
	}
	
	/** return the level name for a given level index */
	public String levelAtIndex(int li) {
		return (li<0||li>levels.length)?null:levels[li];
	}
	
	/** return the level index for a given case */
	public int indexAt(int i) {
		return ids[i];
	}
	
	/** return the factor as an array of strings */
	public String[] asStrings() {
		String[] s = new String[ids.length];
		int i = 0;
		while (i < ids.length) {
			s[i] = at(i);
			i++;
		}
		return s;	
	}
	
    /** returns the number of cases */
    public int size() { return ids.length; }

	public String toString() {
		return super.toString()+"["+ids.length+","+levels.length+"]";
	}
	
    /** displayable representation of the factor variable
    public String toString() {
	//return "{"+((val==null)?"<null>;":("levels="+val.size()+";"))+((id==null)?"<null>":("cases="+id.size()))+"}";
	StringBuffer sb=new StringBuffer("{levels=(");
	if (val==null)
	    sb.append("null");
	else
	    for (int i=0;i<val.size();i++) {
		sb.append((i>0)?",\"":"\"");
		sb.append((String)val.elementAt(i));
		sb.append("\"");
	    };
	sb.append("),ids=(");
	if (id==null)
	    sb.append("null");
	else
	    for (int i=0;i<id.size();i++) {
		if (i>0) sb.append(",");
		sb.append((Integer)id.elementAt(i));
	    };
	sb.append(")}");
	return sb.toString();
    } */
}

