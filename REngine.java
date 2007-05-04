package org.rosuda.REngine;

import java.lang.reflect.Method;

public abstract class REngine {
    /** last created engine or <code>null</code> if there is none */
    protected static REngine lastEngine = null;
	
    /** this is the designated constructor for REngine classes. It uses reflection to call createEngine method on the given REngine class.
	@param klass fully qualified class-name of a REngine implementation
	@return REngine implementation or <code>null</code> if <code>createEngine</code> invokation failed */
    public static REngine engineForClass(String klass) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
		Class cl=Class.forName(klass);
		if (cl==null) throw(new ClassNotFoundException("can't find engine class "+klass));
		Method m=cl.getMethod("createEngine",(Class[])null);
		Object o=m.invoke(null,(Object[])null);
		return lastEngine=(REngine)o;
    }
    
    /** retrieve the last created engine
		@return last created engine or <code>null</code> if no engine was created yet */
    public static REngine getLastEngine() {
		return lastEngine;
    }

	/** parse a string into an expression vector
		@param text string to parse
		@param resolve resolve the resulting REXP or just return a reference */
	public abstract REXP parse(String text, boolean resolve) throws REngineException;
	/** evaluate an expression vector
		@param what an expression (or vector of such) to evaluate
		@param where environment to evaluate in (or <code>null</code> for global env)
		@param resolve resolve the resulting REXP or just return a reference
		@return the result of the evaluation of the last expression */
    public abstract REXP eval(REXP what, REXP where, boolean resolve) throws REngineException;
	/** assign into an environment
		@param symbol symbol name
		@param value value to assign
		@param env environment to assign to */
    public abstract void assign(String symbol, REXP value, REXP env) throws REngineException;
	/** get a value from an environment
		@param symbol symbol name
		@param env environment
		@param resolve resolve the resulting REXP or just return a reference		
		@return value */
    public abstract REXP get(String symbol, REXP env, boolean resolve) throws REngineException;

	/** fetch the contents of the given reference. The resulting REXP may never be REXPReference.
		@param ref reference to resolve
		@return resolved reference */
	public abstract REXP resolveReference(REXP ref) throws REngineException;

	public abstract REXP getParentEnvironment(REXP env, boolean resolve) throws REngineException;
	
	public abstract REXP newEnvironment(REXP parent, boolean resolve) throws REngineException;
	
    /* derived methods */
	public REXP parseAndEval(String text, REXP where, boolean resolve) throws REngineException {
		REXP p = parse(text, false);
		return eval(p, where, resolve);
	}
    public REXP parseAndEval(String cmd) throws REngineException { return parseAndEval(cmd, null, true); };
	
	//--- capabilities ---
	public boolean supportsReferences() { return false; }
	public boolean supportsEnvironemnts() { return false; }
	public boolean supportsREPL() { return false; }

	//--- convenience methods ---
	public void assign(String symbol, double[] d) throws REngineException { assign(symbol, new REXPDouble(d), null); }
	public void assign(String symbol, int[] d) throws REngineException { assign(symbol, new REXPInteger(d), null); }
	public void assign(String symbol, String[] d) throws REngineException { assign(symbol, new REXPString(d), null); }
	public void assign(String symbol, byte[] d) throws REngineException { assign(symbol, new REXPRaw(d), null); }

	public String toString() {
		return super.toString()+((lastEngine==this)?"{last}":"");
	}
}
