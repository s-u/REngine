package org.rosuda.REngine.Rserve.protocol;

// JRclient library - client interface to Rserve, see http://www.rosuda.org/Rserve/
// Copyright (C) 2004 Simon Urbanek
// --- for licensing information see LICENSE file in the original JRclient distribution ---

import java.util.*;
import java.io.*;
import java.net.*;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.protocol.REXPFactory;
import org.rosuda.REngine.REXP;

/** This class encapsulates the QAP1 protocol used by Rserv.
    it is independent of the underying protocol(s), therefore RTalk
    can be used over any transport layer
    <p>
    The current implementation supports long (0.3+/0102) data format only
    up to 32-bit and only for incoming packets.
    <p>
    @version $Id$
*/
public class RTalk {
    public static final int DT_INT=1;
    public static final int DT_CHAR=2;
    public static final int DT_DOUBLE=3;
    public static final int DT_STRING=4;
    public static final int DT_BYTESTREAM=5;
    public static final int DT_SEXP=10;
    public static final int DT_ARRAY=11;

    /** this is a flag saying that the contents is large (>0xfffff0) and hence uses 56-bit length field */
    public static final int DT_LARGE=64;

    public static final int CMD_login=0x001;
    public static final int CMD_voidEval=0x002;
    public static final int CMD_eval=0x003;
    public static final int CMD_shutdown=0x004;

    /** OCAP call, only valid in OCAP mode, since Rserve 1.8 */
    public static final int CMD_OCcall = 0x00f;
    /** not a command but rather payload sent by the server in OCAP mode */
    public static final int CMD_OCinit = 0x434f7352; /* RsOC in ASCII */

    public static final int CMD_openFile=0x010;
    public static final int CMD_createFile=0x011;
    public static final int CMD_closeFile=0x012;
    public static final int CMD_readFile=0x013;
    public static final int CMD_writeFile=0x014;
    public static final int CMD_removeFile=0x015;
    public static final int CMD_setSEXP=0x020;
    public static final int CMD_assignSEXP=0x021;
    
    public static final int CMD_setBufferSize=0x081;
    public static final int CMD_setEncoding=0x082;

    public static final int CMD_detachSession=0x030;
    public static final int CMD_detachedVoidEval=0x031;
    public static final int CMD_attachSession=0x032;

    // control commands since 0.6-0
    public static final int CMD_ctrlEval=0x42;
    public static final int CMD_ctrlSource=0x45;
    public static final int CMD_ctrlShutdown=0x44; 
    
    // errors as returned by Rserve
    public static final int ERR_auth_failed=0x41;
    public static final int ERR_conn_broken=0x42;
    public static final int ERR_inv_cmd=0x43;
    public static final int ERR_inv_par=0x44;
    public static final int ERR_Rerror=0x45;
    public static final int ERR_IOerror=0x46;
    public static final int ERR_not_open=0x47;
    public static final int ERR_access_denied=0x48;
    public static final int ERR_unsupported_cmd=0x49;
    public static final int ERR_unknown_cmd=0x4a;
    public static final int ERR_data_overflow=0x4b;
    public static final int ERR_object_too_big=0x4c;
    public static final int ERR_out_of_mem=0x4d;
    public static final int ERR_ctrl_closed=0x4e;
    public static final int ERR_session_busy=0x50;
    public static final int ERR_detach_failed=0x51;

    /** since 1.7 */
    public static final int ERR_disabled = 0x61;
    public static final int ERR_unavailable = 0x62;
    public static final int ERR_cryptError = 0x63;
    public static final int ERR_securityClose = 0x64;

    public static final int CMD_RESP = 0x10000; /* response bit */
    public static final int RESP_OK  = 0x10001;
    public static final int RESP_ERR = 0x10002;

    public static final int CMD_OOB  = 0x20000; /* OOB command, low 12 bits are app-specific */
    public static final int OOB_SEND = 0x21000;
    public static final int OOB_MSG  = 0x22000;

    InputStream is;
    OutputStream os;
    
    /** constructor; parameters specify the streams
	@param sis socket input stream
	@param sos socket output stream */

    public RTalk(InputStream sis, OutputStream sos) {
	is=sis; os=sos;
    }

    /** writes bit-wise int to a byte buffer at specified position in Intel-endian form
	@param v value to be written
	@param buf buffer
	@param o offset in the buffer to start at. An int takes always 4 bytes */
    public static void setInt(int v, byte[] buf, int o) {
	buf[o]=(byte)(v&255); o++;
	buf[o]=(byte)((v&0xff00)>>8); o++;
	buf[o]=(byte)((v&0xff0000)>>16); o++;
	buf[o]=(byte)((v&0xff000000)>>24);
    }

    /** writes cmd/resp/type byte + 3/7 bytes len into a byte buffer at specified offset.
	@param ty type/cmd/resp byte
	@param len length
	@param buf buffer
	@param o offset
        @return offset in buf just after the header. Please note that since Rserve 0.3 the header can be either 4 or 8 bytes long, depending on the len parameter.
        */
    public static int setHdr(int ty, int len, byte[] buf, int o) {
        buf[o]=(byte)((ty&255)|((len>0xfffff0)?DT_LARGE:0)); o++;
	buf[o]=(byte)(len&255); o++;
	buf[o]=(byte)((len&0xff00)>>8); o++;
	buf[o]=(byte)((len&0xff0000)>>16); o++;
        if (len>0xfffff0) { // for large data we need to set the next 4 bytes as well
            buf[o]=(byte)((len&0xff000000)>>24); o++;
            buf[o]=0; o++; // since len is int, we get 32-bits only
            buf[o]=0; o++;
            buf[o]=0; o++;
        }
        return o;
    }

    /** creates a new header according to the type and length of the parameter
        @param ty type/cmd/resp byte
        @param len length */        
    public static byte[] newHdr(int ty, int len) {
        byte[] hdr=new byte[(len>0xfffff0)?8:4];
        setHdr(ty,len,hdr,0);
        return hdr;
    }
    
    /** converts bit-wise stored int in Intel-endian form into Java int
	@param buf buffer containg the representation
	@param o offset where to start (4 bytes will be used)
	@return the int value. no bounds checking is done so you need to
	        make sure that the buffer is big enough */
    public static int getInt(byte[] buf, int o) {
	return ((buf[o]&255)|((buf[o+1]&255)<<8)|((buf[o+2]&255)<<16)|((buf[o+3]&255)<<24));
    }

    /** converts bit-wise stored length from a header. "long" format is supported up to 32-bit
	@param buf buffer
	@param o offset of the header (length is at o+1)
	@return length */
    public static int getLen(byte[] buf, int o) {

        return
        ((buf[o]&64)>0)? // "long" format; still - we support 32-bit only
        ((buf[o+1]&255)|((buf[o+2]&255)<<8)|((buf[o+3]&255)<<16)|((buf[o+4]&255)<<24))
        :
        ((buf[o+1]&255)|((buf[o+2]&255)<<8)|((buf[o+3]&255)<<16));
    }

    /** converts bit-wise Intel-endian format into long
	@param buf buffer
	@param o offset (8 bytes will be used)
	@return long value */
    public static long getLong(byte[] buf, int o) {
	long low=((long)getInt(buf,o))&0xffffffffL;
	long hi=((long)getInt(buf,o+4))&0xffffffffL;
	hi<<=32; hi|=low;
	return hi;
    }

    public static void setLong(long l, byte[] buf, int o) {
	setInt((int)(l&0xffffffffL),buf,o);
	setInt((int)(l>>32),buf,o+4);
    }

    /** sends a request with no attached parameters
	@param cmd command
	@return returned packet or <code>null</code> if something went wrong */
    public RPacket request(int cmd) {
        byte[] d = new byte[0];
        return request(cmd,d);
    }

    /** sends a request with attached parameters
        @param cmd command
        @param cont contents - parameters
        @return returned packet or <code>null</code> if something went wrong */
    public RPacket request(int cmd, byte[] cont) {
        return request(cmd,null,cont,0,(cont==null)?0:cont.length);
    }

    /** read the response. Return <code>null</code> on error (FIXME: need to use exceptions) */
    public RPacket response() { return response(null); }

    /** read the response. If header is <code>null</code> then is it read from the socket otherwise
	the provided header is used instead of reading it. Note that if provided, the
	header aray must have at least 16 bytes and it must at most contain one message */
    public RPacket response(byte[] header) {
	try {
	    if (header == null) {
		header = new byte[16];
		int n = is.read(header);
		if (n != 16)
		    return null;
	    }
	    int rep = getInt(header, 0);
	    int rl  = getInt(header, 4);
	    if (rl > 0) {
		byte[] ct = new byte[rl];
                int n = 0;
		if (header.length > 16) {
		    n = header.length - 16;
		    System.arraycopy(header, 16, ct, 0, n);
		}
                while (n < rl) {
                    int rd = is.read(ct, n, rl - n);
                    n += rd;
                }
		return new RPacket(rep, ct);
	    }
	    return new RPacket(rep, null);
	} catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /** sends a request with attached prefix and  parameters. Both prefix and cont can be <code>null</code>. Effectively <code>request(a,b,null)</code> and <code>request(a,null,b)</code> are equivalent.
	@param cmd command - a special command of -1 prevents request from sending anything
        @param prefix - this content is sent *before* cont. It is provided to save memory copy operations where a small header precedes a large data chunk (usually prefix conatins the parameter header and cont contains the actual data).
        @param cont contents
        @param offset offset in cont where to start sending (if <0 then 0 is assumed, if >cont.length then no cont is sent)
        @param len number of bytes in cont to send (it is clipped to the length of cont if necessary)
	@return returned packet or <code>null</code> if something went wrong */
    public RPacket request(int cmd, byte[] prefix, byte[] cont, int offset, int len) {
        if (cont!=null) {
            if (offset>=cont.length) { cont=null; len=0; }
            else if (len>cont.length-offset) len=cont.length-offset;
        }
        if (offset<0) offset=0;
        if (len<0) len=0;
        int contlen=(cont==null)?0:len;
        if (prefix!=null && prefix.length>0) contlen+=prefix.length;
	byte[] hdr=new byte[16];
	setInt(cmd,hdr,0);
	setInt(contlen,hdr,4);
	for(int i=8;i<16;i++) hdr[i]=0;
	try {
	    if (cmd!=-1) {
		os.write(hdr);
		if (prefix!=null && prefix.length>0)
		    os.write(prefix);
		if (cont!=null && cont.length>0)
		    os.write(cont,offset,len);
	    }
	    return response();
	} catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /** sends a request with one string parameter attached
	@param cmd command
	@param par parameter - length and DT_STRING will be prepended
	@return returned packet or <code>null</code> if something went wrong */
    public RPacket request(int cmd, String par) {
	try {
            byte[] b=par.getBytes(RConnection.transferCharset);
            int sl=b.length+1;
            if ((sl&3)>0) sl=(sl&0xfffffc)+4; // make sure the length is divisible by 4
	    byte[] rq=new byte[sl+5];
            int i;
	    for(i=0;i<b.length;i++)
                rq[i+4]=b[i];
            while (i<sl) { // pad with 0
                rq[i+4]=0; i++;
            };
	    setHdr(DT_STRING,sl,rq,0);
	    return request(cmd,rq);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }

    /** sends a request with one REXP (single <code>DT_SEXP</code> payload)
	@param cmd command
	@param object REXP to send
	@return returned packet or <code>null</code> if something went wrong */
    public RPacket request(int cmd, REXP object) {
	try {
	    REXPFactory r = new REXPFactory(object);
	    int rl = r.getBinaryLength();
	    byte[] rq = new byte[rl + ((rl > 0xfffff0) ? 8 : 4)];
	    setHdr(RTalk.DT_SEXP, rl, rq, 0);
	    r.getBinaryRepresentation(rq, (rl > 0xfffff0) ? 8 : 4);
	    return request(cmd, rq);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }

    /** sends a request with one string parameter attached
        @param cmd command
        @param par parameter of the type DT_INT
        @return returned packet or <code>null</code> if something went wrong */
    public RPacket request(int cmd, int par) {
	try {
	    byte[] rq=new byte[8];
	    setInt(par,rq,4);
	    setHdr(DT_INT,4,rq,0);
	    return request(cmd,rq);
	} catch (Exception e) {
	};
	return null;
    }
}
