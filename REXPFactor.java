package org.rosuda.REngine;

public class REXPFactor extends REXPInteger {
	private String[] levels;
	private RFactor factor;
	
	public REXPFactor(int[] ids, String[] levels) {
		super(ids);
		this.levels = (levels==null)?(new String[0]):levels;
		factor = new RFactor(this.payload, this.levels, false);
		attr = new REXPList(
							new RList(
									  new REXP[] {
										  new REXPString(this.levels), new REXPString("factor")
									  }, new String[] { "levels", "class" }));
	}

	public REXPFactor(int[] ids, String[] levels, REXPList attr) {
		super(ids, attr);
		this.levels = (levels==null)?(new String[0]):levels;
		factor = new RFactor(this.payload, this.levels, false);
	}
	
	public REXPFactor(RFactor factor) {
		super(factor.asIntegers());
		this.factor = factor;
		this.levels = factor.levels();
		attr = new REXPList(
							new RList(
									  new REXP[] {
										  new REXPString(this.levels), new REXPString("factor")
									  }, new String[] { "levels", "class" }));
	}
	
	public REXPFactor(RFactor factor, REXPList attr) {
		super(factor.asIntegers(), attr);
		this.factor = factor;
		this.levels = factor.levels();
	}

	public boolean isFactor() { return true; }

	public RFactor asFactor() {
		return factor;
	}

	public String[] asStrings() {
		return factor.asStrings();
	}
}
