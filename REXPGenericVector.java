package org.rosuda.REngine;

public class REXPGenericVector extends REXPVector {
	private RList payload;
	
	public REXPGenericVector(RList list) {
		super();
		payload=(list==null)?new RList():list;
		// automatically generate 'names' attribute
		if (payload.isNamed())
			attr = new REXPList(
				new RList(new REXP[] { new REXPString(payload.keys()) },
						  new String[] { "names" }));
	}
	
	public REXPGenericVector(RList list, REXPList attr) {
		super(attr);
		payload=(list==null)?new RList():list;
	}
	
	public int length() { return payload.size(); }

	public boolean isList() { return true; }

	public boolean isRecursive() { return true; }

	public RList asList() { return payload; }
	
	public String toString() {
		return super.toString()+(asList().isNamed()?"named":"");
	}

	public String toDebugString() {
		StringBuffer sb = new StringBuffer(super.toDebugString()+"{");
		int i = 0;
		while (i < payload.size() && i < maxDebugItems) {
			if (i>0) sb.append(",\n");
			sb.append(payload.at(i).toDebugString());
			i++;
		}
		if (i < payload.size()) sb.append(",..");
		return sb.toString()+"}";
	}
}
