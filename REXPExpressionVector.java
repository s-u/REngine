package org.rosuda.REngine;

public class REXPExpressionVector extends REXPGenericVector {
	public REXPExpressionVector(RList list) { super(list); }	
	public REXPExpressionVector(RList list, REXPList attr) { super(list, attr); }

	public boolean isExpression() { return true; }
}
