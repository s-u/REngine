// REngine - generic Java/R API
//
// Copyright (C) 2007 Simon Urbanek
// --- for licensing information see LICENSE file in the distribution ---
//
//  REXPMismatch.java
//
//  Created by Simon Urbanek on 2007/05/03
//
//  $Id: REngineException.java 2555 2006-06-21 20:36:42Z urbaneks $
//

package org.rosuda.REngine;

public class REXPMismatchException extends Exception {
    public REXPMismatchException(REXP x, String access) {
        super("attempt to access "+x.getClass().getName()+" as "+access);
    }
}
