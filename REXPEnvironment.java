package org.rosuda.REngine;
// environments are like REXPReferences except that they cannot be resolved

public class REXPEnvironment extends REXP {
	REngine eng;
	Object handle;
	public REXPEnvironment(REngine eng, Object handle) {
		super();
		this.eng = eng;
		this.handle = handle;
	}
	
	public boolean isEnvironment() { return true; }
	
	Object getHandle() { return handle; }
	
	public REXP get(String name, boolean resolve) throws REngineException {
		return eng.get(name, this, resolve);
	}

	public REXP get(String name) throws REngineException {
		return get(name, true);
	}
	
	public void assign(String name, REXP value) throws REngineException {
		eng.assign(name, value, this);
	}
	
	public REXP parent(boolean resolve) throws REngineException {
		return eng.getParentEnvironment(this, resolve);
	}

	public REXPEnvironment parent() throws REngineException {
		return (REXPEnvironment) eng.getParentEnvironment(this, true);
	}
}
