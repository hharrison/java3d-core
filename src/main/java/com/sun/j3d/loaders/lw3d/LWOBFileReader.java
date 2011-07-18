/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.loaders.lw3d;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import com.sun.j3d.loaders.ParsingErrorException;


class LWOBFileReader extends BufferedInputStream {



    // Debugging constants
    final static int TRACE = DebugOutput.TRACE;
    final static int VALUES = DebugOutput.VALUES;
    final static int MISC = DebugOutput.MISC;
    final static int LINE_TRACE = DebugOutput.LINE_TRACE;
    final static int NONE = DebugOutput.NONE;
    final static int EXCEPTION = DebugOutput.EXCEPTION;

    protected DebugOutput debugPrinter;

    protected String theFilename;

    protected int marker;



    protected void debugOutputLn(int outputType, String theOutput) {
        if (theOutput.equals(""))
            debugPrinter.println(outputType, theOutput);
        else
            debugPrinter.println(outputType,
                                 getClass().getName() + "::" + theOutput);
    } // End of debugOutputLn



    // Return a string consisting of the next 4 bytes in the file
    public String getToken() throws ParsingErrorException {
        byte tokenBuffer[] = new byte[4];
        try {
            int readResult = read(tokenBuffer, 0, 4);
            if (readResult == -1) {
                debugOutputLn(LINE_TRACE, "no token - returning null");
                return null;
            }
            return new String(tokenBuffer);
        }
        catch (IOException e) {
            debugOutputLn(EXCEPTION, "getToken: " + e);
	    throw new ParsingErrorException(e.getMessage());
        }
    }



    /**
     * Skip ahead amount bytes in the file
     */
    public void skipLength(int amount) throws ParsingErrorException {
	try {
	    skip((long)amount);
	    marker += amount;
	}
	catch (IOException e) {
	    debugOutputLn(EXCEPTION, "skipLength: " + e);
	    throw new ParsingErrorException(e.getMessage());
	}
    }



    /**
     * Read four bytes from the file and return their integer value 
     */
    public int getInt() throws ParsingErrorException {
        try {
	    int x = 0;
	    for (int i = 0 ; i < 4 ; i++) {
		int readResult = read();
		if (readResult == -1)
		    throw new ParsingErrorException("Unexpected EOF");
		x = (x << 8) | readResult;
	    }
            return x;
        }
        catch (IOException e) {
            debugOutputLn(EXCEPTION, "getInt: " + e);
	    throw new ParsingErrorException(e.getMessage());
        }
    }



    /**
     * Read four bytes from the file and return their float value
     */
    public float getFloat() throws ParsingErrorException {
	return Float.intBitsToFloat(getInt());
    } // End of getFloat



    /**
     * Returns the name of the file associated with this stream
     */
    public String getFilename() {
	return theFilename;
    } // End of getFilename



    /**
     * Returns a string read from the file.  The string is assumed to
     * end with '0'.
     */
    public String getString() throws ParsingErrorException {
      byte buf[] = new byte[512];
      try {
	  byte b;
	  int len = 0;
	  do {
	    b = (byte)read();
	    buf[len++] = b;
	  } while (b != 0);
	  // Have to read an even number of bytes
	  if (len % 2 != 0) read();
      }
      catch (IOException e) {
	  debugOutputLn(EXCEPTION, "getString: " + e);
	  throw new ParsingErrorException(e.getMessage());
      }
      return new String(buf);
    } // End of getString



    /**
     * Reads an array of xyz values.
     */
    public void getVerts(float ar[], int num) throws ParsingErrorException {
      for (int i = 0 ; i < num ; i++) {
	ar[i * 3 + 0] = getFloat();
	ar[i * 3 + 1] = getFloat();
	ar[i * 3 + 2] = -getFloat();
      }
    } // End of getVerts



    /**
     * Reads two bytes from the file and returns their integer value.
     */
    public int getShortInt() throws ParsingErrorException {
	int i = 0;
	try {
	    i = read();
	    i = (i << 8) | read();
	    // Sign extension
	    if ((i & 0x8000) != 0) i |= 0xffff0000;
	}
	catch (IOException e) {
	    debugOutputLn(EXCEPTION, "getShortInt: " + e);
	    throw new ParsingErrorException(e.getMessage());
	}
	return i;
    } // End of getShortInt



    /**
     * Returns the current position in the file
     */
    public int getMarker() {
      // protected field inherited from BufferedInputStream
      return marker;
    } // End of getMarker



    public int read() throws IOException {
      marker++;
      return super.read();
    } // End of read()



    public int read(byte[] buffer, int offset, int count) throws IOException {
      int ret = super.read(buffer, offset, count);
      if (ret != -1) marker += ret;
      return ret;
    } // End of read(byte[], int, int)



    /**
     * Constructor.
     */
    public LWOBFileReader(String filename) throws FileNotFoundException {
	super(new FileInputStream(filename));

	// Add constants on this line to get more debug output
	debugPrinter = new DebugOutput(127);

	marker = 0;
    } // End of constructor

  public LWOBFileReader(java.net.URL url) throws java.io.IOException {
    super(url.openStream());

    // add constants on this line to get more debug output
    debugPrinter = new DebugOutput(127);

    marker = 0;
  }

} // End of file LWOBFileReader
