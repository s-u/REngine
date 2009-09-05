package org.rosuda.REngine ;

/**
 * Exception thrown when an error occurs during eval. This 
 * class is a placeholder and should be extended when more information
 * can be extracted from R (call stack, etc ... )
 */
public class REngineEvalException extends REngineException {
	
	/**
	 * Value returned by the rniEval native method when the input passed to eval
	 * is invalid
	 */ 
	public static final int INVALID_INPUT = -1 ;
	
	/**
	 * Value returned by the rniEval native method when an error occured during 
	 * eval (stop, ...)
	 */
	public static final int ERROR = -2 ;  

	/**
	 * Constructor. 
	 */
	public REngineEvalException( REngine eng, String message ){
		super( eng, message ); 
	}
	
}
