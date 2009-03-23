// JRIEngine - REngine-based interface to JRI
// Copyright(c) 2009 Simon Urbanek
//
// Currently it uses low-level calls from org.rosuda.JRI.Rengine, but
// all REXP representations are created based on the org.rosuda.REngine API

package org.rosuda.REngine.JRI;

import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.Mutex;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.REngine.*;

/** <code>JRIEngine</code> is a <code>REngine</code> implementation using JRI (Java/R Interface).
 <p>
 Note that at most one JRI instance can exist in a given JVM process, because R does not support multiple threads. <code>JRIEngine</code> itself is thread-safe, so it is possible to invoke its methods from any thread. However, this is achieved by serializing all entries into R, so be aware of possible deadlock conditions if your R code calls back into Java (<code>JRIEngine</code> is re-entrant from the same thread so deadlock issues can arise only with multiple threads inteacting thorugh R). */
public class JRIEngine extends REngine {
	// internal R types as defined in Rinternals.h
	static final int NILSXP = 0; /* nil = NULL */
	static final int SYMSXP = 1; /* symbols */
	static final int LISTSXP = 2; /* lists of dotted pairs */
	static final int CLOSXP = 3; /* closures */
	static final int ENVSXP = 4; /* environments */
	static final int PROMSXP = 5; /* promises: [un]evaluated closure arguments */
	static final int LANGSXP = 6; /* language constructs */
	static final int SPECIALSXP = 7; /* special forms */
	static final int BUILTINSXP = 8; /* builtin non-special forms */
	static final int CHARSXP = 9; /* "scalar" string type (internal only) */
	static final int LGLSXP = 10; /* logical vectors */
	static final int INTSXP = 13; /* integer vectors */
	static final int REALSXP = 14; /* real variables */
	static final int CPLXSXP = 15; /* complex variables */
	static final int STRSXP = 16; /* string vectors */
	static final int DOTSXP = 17; /* dot-dot-dot object */
	static final int ANYSXP = 18; /* make "any" args work */
	static final int VECSXP = 19; /* generic vectors */
	static final int EXPRSXP = 20; /* expressions vectors */
	static final int BCODESXP = 21; /* byte code */
	static final int EXTPTRSXP = 22; /* external pointer */
	static final int WEAKREFSXP = 23; /* weak reference */
	static final int RAWSXP = 24; /* raw bytes */
	static final int S4SXP = 25; /* S4 object */
	
	/** currently running <code>JRIEngine</code> - there can be only one and we store it here. Essentially if it is <code>null</code> then R was not initialized. */
	static JRIEngine jriEngine = null;
	
	/** reference to the underlying low-level JRI (RNI) engine */
	Rengine rni = null;
	
	/** event loop callabcks associated with this engine. (Currently callbacks are not supported yet) */
	RMainLoopCallbacks callbackObject = null;
	
	/** mutex synchronizing access to R through JRIEngine.<p> NOTE: only access through this class is synchronized. Any other access (e.g. using RNI directly) is NOT. */
	Mutex rniMutex = null;
	
	// cached pointers of special objects in R
	long R_UnboundValue, R_NilValue;
	
	/** special, global references */
	public REXPReference globalEnv, emptyEnv, baseEnv, nullValue;
	
	/** factory method called by <code>engineForClass</code> 
	 @return new or current engine (new if there is none, current otherwise since R allows only one engine at any time) */
	public static REngine createEngine() throws REngineException {
		// there can only be one JRI engine in a process
		if (jriEngine == null)
			jriEngine = new JRIEngine();
		return jriEngine;
	}
	
	/** deafault constructor - this constructor is also used via <code>createEngine</code> factory call and implies --no-save R argument */
	public JRIEngine() throws REngineException {
		this(new String[] { "--no-save" });
	}
	
	public JRIEngine(String args[]) throws REngineException {
		// the default modus operandi is without event loop and with --no-save option
		rniMutex = new Mutex();
		rni = new Rengine(args, false, null);
		if (!rni.waitForR())
			throw(new REngineException(this, "Unable to initialize R"));
		if (rni.rniGetVersion() < 0x109)
			throw(new REngineException(this, "R JRI engine is too old - RNI API 1.9 (JRI 0.5) or newer is required"));
		globalEnv = new REXPReference(this, new Long(rni.rniSpecialObject(Rengine.SO_GlobalEnv)));
		nullValue = new REXPReference(this, new Long(R_NilValue = rni.rniSpecialObject(Rengine.SO_NilValue)));
		emptyEnv = new REXPReference(this, new Long(rni.rniSpecialObject(Rengine.SO_EmptyEnv)));
		baseEnv = new REXPReference(this, new Long(rni.rniSpecialObject(Rengine.SO_BaseEnv)));
		R_UnboundValue = rni.rniSpecialObject(Rengine.SO_UnboundValue);
	}
	
	public REXP parse(String text, boolean resolve) throws REngineException {
		REXP ref = null;
		boolean obtainedLock = rniMutex.safeLock();
		try {
			long pr = rni.rniParse(text, 1);
			if (pr == 0) throw(new REngineException(this, "Parse error"));
			rni.rniPreserve(pr);
			ref = new REXPReference(this, new Long(pr));
			if (resolve)
				try { ref = resolveReference(ref); } catch (REXPMismatchException me) { };
		} finally {
			if (obtainedLock)
				rniMutex.unlock();
		}
		return ref;
	}

	public REXP eval(REXP what, REXP where, boolean resolve) throws REngineException, REXPMismatchException {
		REXP ref = null;
		long rho = 0;
		if (where != null && !where.isReference()) throw(new REXPMismatchException(where, "reference (environment)"));
		if (where != null) rho = ((Long)((REXPReference)where).getHandle()).longValue();
		if (what == null) throw(new REngineException(this, "null object to evaluate"));
		if (!what.isReference()) throw(new REXPMismatchException(where, "reference (expression)"));
		boolean obtainedLock = rniMutex.safeLock();
		try {
			long pr = rni.rniEval(((Long)((REXPReference)what).getHandle()).longValue(), rho);
			if (pr == -1) throw(new REngineException(this, "Eval error (invalid input)"));
			rni.rniPreserve(pr);
			ref = new REXPReference(this, new Long(pr));
			if (resolve)
				ref = resolveReference(ref);
		} finally {
			if (obtainedLock)
				rniMutex.unlock();
		}
		return ref;
	}

	public void assign(String symbol, REXP value, REXP env) throws REngineException, REXPMismatchException {
		long rho = 0;
		if (env != null && !env.isReference()) throw(new REXPMismatchException(env, "reference (environment)"));
		if (env != null) rho = ((Long)((REXPReference)env).getHandle()).longValue();
		if (value == null) value = nullValue;
		if (!value.isReference())
			value = createReference(value); // if value is not a reference, we have to create one
		boolean obtainedLock = rniMutex.safeLock();
		try {
			rni.rniAssign(symbol, ((Long)((REXPReference)value).getHandle()).longValue(), rho);
		} finally {
			if (obtainedLock)
				rniMutex.unlock();
		}
	}
	
	public REXP get(String symbol, REXP env, boolean resolve) throws REngineException, REXPMismatchException {
		REXP ref = null;
		long rho = 0;
		if (env != null && !env.isReference()) throw(new REXPMismatchException(env, "reference (environment)"));
		if (env != null) rho = ((Long)((REXPReference)env).getHandle()).longValue();
		boolean obtainedLock = rniMutex.safeLock();
		try {
			long pr = rni.rniFindVar(symbol, rho);
			if (pr == R_UnboundValue || pr == 0) return null;
			rni.rniPreserve(pr);
			ref = new REXPReference(this, new Long(pr));
			if (resolve)
				try { ref = resolveReference(ref); } catch (REXPMismatchException me) { };
		} finally {
			if (obtainedLock)
				rniMutex.unlock();
		}
		return ref;
	}

	public REXP resolveReference(REXP ref) throws REngineException, REXPMismatchException {
		REXP res = null;
		if (ref == null) throw(new REngineException(this, "resolveReference called on NULL input"));
		if (!ref.isReference()) throw(new REXPMismatchException(ref, "reference"));
		long ptr = ((Long)((REXPReference)ref).getHandle()).longValue();
		if (ptr == 0) return new REXPNull();
		return resolvePointer(ptr);
	}

	/** this is the actual implementation of <code>resolveReference</code> but it works directly on the long pointers to be more efficient when performing recursive de-referencing */
	REXP resolvePointer(long ptr) throws REngineException, REXPMismatchException {
		if (ptr == 0) return nullValue;
		REXP res = null;
		boolean obtainedLock = rniMutex.safeLock();
		try {
			int xt = rni.rniExpType(ptr);
			String an[] = rni.rniGetAttrNames(ptr);
			REXPList attrs = null;
			if (an != null && an.length > 0) { // are there attributes? Then we need to resolve them first
				RList attl = new RList();
				for (int i = 0; i < an.length; i++) {
					long aptr = rni.rniGetAttr(ptr, an[i]);
					if (aptr != 0 && aptr != R_NilValue) {
						REXP av = resolvePointer(aptr);
						if (av != null && av != nullValue)
							attl.put(an[i], av);
					}
				}
				if (attl.size() > 0)
					attrs = new REXPList(attl);
			}
			switch (xt) {
				case NILSXP:
					return nullValue;
					
				case STRSXP:
					String[] s = rni.rniGetStringArray(ptr);
					res = new REXPString(s, attrs);
					break;
					
				case INTSXP:
					if (rni.rniInherits(ptr, "factor")) {
						long levx = rni.rniGetAttr(ptr, "levels");
						if (levx != 0) {
							String[] levels = null;
							// we're using low-lever calls here (FIXME?)
							int rlt = rni.rniExpType(levx);
							if (rlt == STRSXP) {
								levels = rni.rniGetStringArray(levx);
								int[] ids = rni.rniGetIntArray(ptr);
								res = new REXPFactor(ids, levels, attrs);
							}
						}
					}
					// if it's not a factor, then we use int[] instead
					if (res == null)
						res = new REXPInteger(rni.rniGetIntArray(ptr), attrs);
					break;
					
				case REALSXP:
					res = new REXPDouble(rni.rniGetDoubleArray(ptr), attrs);
					break;
					
				case LGLSXP:
				{
					int ba[] = rni.rniGetBoolArrayI(ptr);
					byte b[] = new byte[ba.length];
					for (int i = 0; i < ba.length; i++) b[i] = (ba[i] == 0 || ba[i] == 1) ? (byte) ba[i] : 2;
					res = new REXPLogical(b, attrs);
				}
					break;
					
				case VECSXP:
				{
					long l[] = rni.rniGetVector(ptr);
					REXP rl[] = new REXP[l.length];
					long na = rni.rniGetAttr(ptr, "names");
					String[] names = null;
					if (na != 0 && rni.rniExpType(na) == STRSXP)
						names = rni.rniGetStringArray(na);
					for (int i = 0; i < l.length; i++)
						rl[i] = resolvePointer(l[i]);
					RList list = (names == null) ? new RList(rl) : new RList(rl, names);
					res = new REXPGenericVector(list, attrs);
				}
					break;
					
				case RAWSXP:
					res = new REXPRaw(rni.rniGetRawArray(ptr), attrs);
					break;
					
				case LISTSXP:
				case LANGSXP:
				{
					RList l = new RList();
					// we need to plow through the list iteratively - the recursion occurs at the value level
					long cdr = ptr;
					while (cdr != 0 && cdr != R_NilValue) {
						long car = rni.rniCAR(cdr);
						long tag = rni.rniTAG(cdr);
						String name = null;
						if (rni.rniExpType(tag) == SYMSXP)
							name = rni.rniGetSymbolName(tag);
						REXP val = resolvePointer(car);
						if (name == null) l.add(val); else l.put(name, val);
						cdr = rni.rniCDR(cdr);
					}
					res = (xt == LANGSXP) ? new REXPLanguage(l, attrs) : new REXPList(l, attrs);
				}
					break;
					
				case SYMSXP:
					res = new REXPSymbol(rni.rniGetSymbolName(ptr));
					break;
			}
		} finally {
			if (obtainedLock)
				rniMutex.unlock();
		}
		return res;
	}

	
	public REXP createReference(REXP value) throws REngineException, REXPMismatchException {
		if (value == null) throw(new REngineException(this, "createReference from a NULL value"));
		if (value.isReference()) return value;
		long ptr = createReferencePointer(value);
		if (ptr == 0) return null;
		return new REXPReference(this, new Long(ptr));
	}
	
	long createReferencePointer(REXP value) throws REngineException, REXPMismatchException {
		boolean obtainedLock = rniMutex.safeLock();
		int upp = 0;
		try {
			long ptr = 0;
			if (value.isNull()) // NULL cannot have attributes, hence get out right away
				return R_NilValue;
			else if (value.isLogical())
				ptr = rni.rniPutBoolArrayI(value.asIntegers());
			else if (value.isInteger())
				ptr = rni.rniPutIntArray(value.asIntegers());
			else if (value.isRaw())
				ptr = rni.rniPutRawArray(value.asBytes());
			else if (value.isNumeric())
				ptr = rni.rniPutDoubleArray(value.asDoubles());
			else if (value.isString())
				ptr = rni.rniPutStringArray(value.asStrings());
			else if (value.isPairList()) { // LISTSXP / LANGSXP
				boolean lang = value.isLanguage();
				RList rl = value.asList();
				ptr = R_NilValue;
				int j = rl.size();
				if (j == 0)
					ptr = rni.rniCons(R_NilValue, 0, 0, lang);
				else
					// we are in a somewhat unfortunate situation because we cannot append to the list (RNI has no rniSetCDR!) so we have to use Preserve and bulild the list backwards which may be a bit slower ...
					for (int i = j - 1; i >= 0; i--) {
						REXP v = rl.at(i);
						String n = rl.keyAt(i);
						long sn = 0;
						if (n != null) sn = rni.rniInstallSymbol(n);
						long vptr = createReferencePointer(v);
						if (vptr == 0) vptr = R_NilValue;
						long ent = rni.rniCons(vptr, ptr, sn, lang);
						rni.rniPreserve(ent); // preserve current head
						rni.rniRelease(ptr); // release previous head (since it's part of the new one already)
						ptr = ent;
					}
			} else if (value.isList()) { // VECSXP
				int init_upp = upp;
				RList rl = value.asList();
				long xl[] = new long[rl.size()];
				for (int i = 0; i < xl.length; i++) {
					REXP rv = rl.at(i);
					if (rv == null || rv.isNull())
						xl[i] = R_NilValue;
					else {
						long lv = createReferencePointer(rv);
						if (lv != 0 && lv != R_NilValue) {
							rni.rniProtect(lv);
							upp++;
						} else lv = R_NilValue;
						xl[i] = lv;
					}
				}
				ptr = rni.rniPutVector(xl);
				if (init_upp > upp) {
					rni.rniUnprotect(upp - init_upp);
					upp = init_upp;
				}
			} else if (value.isSymbol())
				return rni.rniInstallSymbol(value.asString()); // symbols need no attribute handling, hence get out right away
			if (ptr == R_NilValue)
				return ptr;
			if (ptr != 0) {
				REXPList att = value._attr();
				if (att == null || !att.isPairList()) return ptr; // no valid attributes? the we're done
				RList al = att.asList();
				if (al == null || al.size() < 1 || !al.isNamed()) return ptr; // again - no valid list, get out
				rni.rniProtect(ptr); // symbols and other exotic creatures are already out by now, so it's ok to protect
				upp++;
				for (int i = 0; i < al.size(); i++) {
					REXP v = al.at(i);
					String n = al.keyAt(i);
					if (n != null) {
						long vptr = createReferencePointer(v);
						if (vptr != 0 && vptr != R_NilValue)
							rni.rniSetAttr(ptr, n, vptr);
					}
				}
				return ptr;
			}
		} finally {
			if (upp > 0)
				rni.rniUnprotect(upp);
			if (obtainedLock)
				rniMutex.unlock();
		}
		// we fall thgough here if the object cannot be handled or something went wrong
		return 0;
	}
	
	public void finalizeReference(REXP ref) throws REngineException, REXPMismatchException {
		if (ref != null && ref.isReference()) {
			long ptr = ((Long)((REXPReference)ref).getHandle()).longValue();
			boolean obtainedLock = rniMutex.safeLock();
			try {
				rni.rniRelease(ptr);
			} finally {
				if (obtainedLock)
					rniMutex.unlock();
			}
		}
	}

	public REXP getParentEnvironment(REXP env, boolean resolve) throws REngineException, REXPMismatchException {
		REXP ref = null;
		long rho = 0;
		if (env != null && !env.isReference()) throw(new REXPMismatchException(env, "reference (environment)"));
		if (env != null) rho = ((Long)((REXPReference)env).getHandle()).longValue();
		boolean obtainedLock = rniMutex.safeLock();
		try {
			long pr = rni.rniParentEnv(rho);
			if (pr == 0 || pr == R_NilValue) return null; // this should never happen, really
			ref = new REXPReference(this, new Long(pr));
			if (resolve)
				ref = resolveReference(ref);
		} finally {
			if (obtainedLock)
				rniMutex.unlock();
		}
		return ref;
	}

	public REXP newEnvironment(REXP parent, boolean resolve) throws REngineException {
		throw(new REngineException(this, "unsupported"));
	}

	public boolean close() {
		if (rni == null) return false;
		rni.end();
		return true;
	}

	public boolean supportsReferences() { return true; }
	public boolean supportsEnvironemnts() { return true; }
	// public boolean supportsREPL() { return true; }

}