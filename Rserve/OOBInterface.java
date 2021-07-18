package org.rosuda.REngine.Rserve;

import  org.rosuda.REngine.REXP;

public interface OOBInterface {
    public void oobSend(int code, REXP message);
    public REXP oobMessage(int code, REXP message);
}
