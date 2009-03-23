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
	
	public Object getHandle() { return handle; }
	
	public REXP get(String name, boolean resolve) throws REngineException {
		try {
			return eng.get(name, this, resolve);
		} catch (REXPMismatchException e) { // this should never happen because this is always guaranteed to be REXPEnv
			throw(new REngineException(eng, "REXPMismatchException:"+e+" in get()"));
		}
	}

	public REXP get(String name) throws REngineException {
		return get(name, true);
	}
	
	public void assign(String name, REXP value) throws REngineException, REXPMismatchException {
		eng.assign(name, value, this);
	}
	
	public REXP parent(boolean resolve) throws REngineException {
		try {
			return eng.getParentEnvironment(this, resolve);
		} catch (REXPMismatchException e) { // this should never happen because this is always guaranteed to be REXPEnv
			throw(new REngineException(eng, "REXPMismatchException:"+e+" in parent()"));
		}
	}

	public REXPEnvironment parent() throws REngineException {
		return (REXPEnvironment) parent(true);
	}
}
