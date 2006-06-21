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

    public abstract REXP eval(String cmd, boolean copy) throws REngineException;
    public abstract REXP eval(String cmd, REXP[] args) throws REngineException;

    public abstract void assign(String symbol, REXP value, REXP env) throws REngineException;
    public abstract REXP get(String symbol, REXP env) throws REngineException;

    /* derived methods */
    public REXP eval(String cmd) throws REngineException { return eval(cmd, true); };
    public void voidEval(String cmd) throws REngineException { eval(cmd, false); };
}
