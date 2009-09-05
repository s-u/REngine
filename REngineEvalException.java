package org.rosuda.REngine ;

/**
 * Exception thrown when an error occurs during eval. This 
 * class is a placeholder and should be extended when more information
 * can be extracted from R (call stack, etc ... )
 */
public class REngineEvalException extends REngineException {
	
	/**
	 * Constructor. 
	 */
	public REngineEvalException( REngine eng, String message ){
		super( eng, message ); 
	}
	
}
