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

import java.io.*;
import com.sun.j3d.loaders.ParsingErrorException;

/**
 * This class is a superclass for most of the Lws* Scene-file parsing
 * classes. It provides some debugging utilities, as well as utilities for
 * reading the types of data common to this loader.
 */

class TextfileParser {

    // class variables 
    static int WORD = StreamTokenizer.TT_WORD;
    static int NUMBER = StreamTokenizer.TT_NUMBER;
    int currentLevel = 3;
    final static int TRACE = DebugOutput.TRACE, VALUES = DebugOutput.VALUES;
    final static int MISC = DebugOutput.MISC, LINE_TRACE = DebugOutput.LINE_TRACE;
    final static int NONE = DebugOutput.NONE, EXCEPTION = DebugOutput.EXCEPTION;
    final static int TIME = DebugOutput.TIME;
    protected DebugOutput debugPrinter;
    char lineSeparatorChar = 0;

    TextfileParser() {
	debugPrinter = new DebugOutput(EXCEPTION);
	String lineSeparator = System.getProperty("line.separator");
	lineSeparatorChar = lineSeparator.charAt(0);
	debugOutputLn(VALUES, "lineSeparatorChar = " + (int)lineSeparatorChar);
    }

    
    protected void debugOutputLn(int outputType, String theOutput) {
	if (theOutput.equals(""))
	    debugPrinter.println(outputType, theOutput);
	else {
	    debugPrinter.println(outputType,
				 getClass().getName() + "::" + theOutput);
	}
    }

    protected void debugOutput(int outputType, String theOutput) {
	debugPrinter.print(outputType, theOutput);
    }

    /**
     * Utility method to advance the tokenizer until we see the given
     * string.  This is used to skip by various parameters that we 
     * currently ignore in the loader.
     */
    void skipUntilString(StreamTokenizer st, String theString)
	throws ParsingErrorException {
	boolean done = false;
	try {
	    while (!done) {
		st.nextToken();
		if (st.ttype == WORD &&
		    st.sval.equals(theString))
		    done = true;
	    }
	}
	catch (IOException e) {
	    throw new ParsingErrorException(e.getMessage());
	}
    }


    /**
     * Returns number from the tokenizer.  Note that we don't recognize
     * numbers in the tokenizer automatically because numbers might be in
     * scientific notation, which isn't processed correctly by 
     * StreamTokenizer
     */
    double getNumber(StreamTokenizer st)
	throws ParsingErrorException, NumberFormatException {
	try {
	    int token = st.nextToken();
	}
	catch (IOException e) {
	    throw new ParsingErrorException(e.getMessage());
	}
	checkType(st, WORD);
	return ((Double.valueOf(st.sval)).doubleValue());
    }

    /**
     * Returns String from the tokenizer
     */
    String getString(StreamTokenizer st) throws ParsingErrorException {
	try {
	    st.nextToken();
	}
	catch (IOException e) {
	    throw new ParsingErrorException(e.getMessage());
	}
	checkType(st, WORD);
	return (st.sval);
    }

    /**
     * Returns a "name" from the stream.  This is different from simply a
     * String because the name could contain whitespace characters 
     * (such as "object 1" or "objectname (sequence)") that would confuse
     * the string parser.  So we just grab all characters until EOL and
     * concatenate them together to form the name
     */ 
    String getName(StreamTokenizer st) throws ParsingErrorException {
	String theName = "";
	st.ordinaryChar(lineSeparatorChar);
	st.ordinaryChar('\n');
	st.ordinaryChar('\r');
	try {
	    st.nextToken();
	    while (st.ttype != lineSeparatorChar &&
		   st.ttype != '\r' &&
		   st.ttype != '\n') {
		if (st.ttype != '(' &&
		    st.ttype != ')')
		    theName += st.sval;
		st.nextToken();
	    }
	}
	catch (IOException e) {
	    throw new ParsingErrorException(e.getMessage());
	}
	st.whitespaceChars(lineSeparatorChar, lineSeparatorChar);
	st.whitespaceChars('\n', '\n');
	st.whitespaceChars('\r', '\r');
	debugOutputLn(VALUES, "name = " + theName);
	return theName;
    }

    /**
     * Gets the next token and ensures that it is the string we were 
     * expecting to see
     */
    void getAndCheckString(StreamTokenizer st, String expectedValue)
	throws ParsingErrorException {
	try {
	    st.nextToken();
	    checkString(st, expectedValue);
	}
	catch (IOException e) {
	    throw new ParsingErrorException(e.getMessage());
	}
    }

    /**
     * Error checking routine - makes sure the current token is the string
     * we were expecting
     */
    void checkString(StreamTokenizer st, String theString) throws
	ParsingErrorException {
	if (!(st.ttype == StreamTokenizer.TT_WORD) ||
	    !st.sval.equals(theString))
	    throw new ParsingErrorException(
		"Bad String Token (wanted " + theString + ", got " + st.sval +
		": " + st.toString());
    }

    /**
     * Error checking routine - makes sure the current token is of the right
     * type
     */
    void checkType(StreamTokenizer st, int theType)
	throws ParsingErrorException {
	if (!(st.ttype == theType))
	    throw new ParsingErrorException(
		"Bad Type Token, Expected " + theType + " and received" +
		 st.ttype);
    }

    /**
     * Utility routine - gets next token, checks it against our expectation,
     * then skips a given number of tokens.  This can be used to parse 
     * through (and ignore) certain parameter/value sets in the files
     */
    void skip(StreamTokenizer st, String tokenString, int skipVals)
	throws ParsingErrorException {
	try {
	    st.nextToken();
	    checkString(st, tokenString);
	    for (int i = 0; i < skipVals; ++i) {
		st.nextToken();
	    }
	}
	catch (IOException e) {
	    throw new ParsingErrorException(e.getMessage());
	}
    }
	
    /**
     * Utility method- used to check whether the current token is equal
     * to the given string
     */
    boolean isCurrentToken(StreamTokenizer st, String tokenString) {
	if (st.ttype == WORD)
	    return (st.sval.equals(tokenString));
	return false;
    }
}	
