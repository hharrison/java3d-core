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

package com.sun.j3d.loaders.objectfile;

import java.io.StreamTokenizer;
import java.io.IOException;
import java.io.Reader;
import com.sun.j3d.loaders.ParsingErrorException;

class ObjectFileParser extends StreamTokenizer {

    private static final char BACKSLASH = '\\';


    /**
     * setup
     *
     *    Sets up StreamTokenizer for reading ViewPoint .obj file format.
     */
    void setup() {
	resetSyntax();
	eolIsSignificant(true);
	lowerCaseMode(true);

	// All printable ascii characters
	wordChars('!', '~');

	// Comment from ! to end of line
	commentChar('!');

	whitespaceChars(' ', ' ');
	whitespaceChars('\n', '\n');
	whitespaceChars('\r', '\r');
	whitespaceChars('\t', '\t');

	// These characters returned as tokens
	ordinaryChar('#');
	ordinaryChar('/');
	ordinaryChar(BACKSLASH);
    } // End of setup


    /**
     * getToken
     *
     *	Gets the next token from the stream.  Puts one of the four
     *	constants (TT_WORD, TT_NUMBER, TT_EOL, or TT_EOF) or the token value
     *	for single character tokens into ttype.  Handles backslash
     *	continuation of lines.
     */
    void getToken() throws ParsingErrorException {
	int t;
	boolean done = false;

	try {
	    do {
		t = nextToken();
		if (t == BACKSLASH) {
		    t = nextToken();
		    if (ttype != TT_EOL) done = true;
		} else done = true;
	    } while (!done);
	}
	catch (IOException e) {
	    throw new ParsingErrorException(
		"IO error on line " + lineno() + ": " + e.getMessage());
	}
    } // End of getToken


    void printToken() {
	switch (ttype) {
	case TT_EOL:
	    System.out.println("Token EOL");
	    break;
	case TT_EOF:
	    System.out.println("Token EOF");
	    break;
	case TT_WORD:
	    System.out.println("Token TT_WORD: " + sval);
	    break;
	case '/':
	    System.out.println("Token /");
	    break;
	case BACKSLASH:
	    System.out.println("Token " + BACKSLASH);
	    break;
	case '#':
	    System.out.println("Token #");
	    break;
	}
    } // end of printToken


    /**
     * skipToNextLine
     *
     *	Skips all tokens on the rest of this line.  Doesn't do anything if
     *	We're already at the end of a line
     */
    void skipToNextLine() throws ParsingErrorException {
	while (ttype != TT_EOL && ttype != -1 /* issue 587*/) {
	    getToken();
	}
    } // end of skipToNextLine


    /**
     * getNumber
     *
     *	Gets a number from the stream.  Note that we don't recognize
     *	numbers in the tokenizer automatically because numbers might be in
     *	scientific notation, which isn't processed correctly by 
     *	StreamTokenizer.  The number is returned in nval.
     */
    void getNumber() throws ParsingErrorException {
	int t;

	try {
	    getToken();
	    if (ttype != TT_WORD)
		throw new ParsingErrorException("Expected number on line " + lineno());
	    nval =  (Double.valueOf(sval)).doubleValue();
	}
	catch (NumberFormatException e) {
	    throw new ParsingErrorException(e.getMessage());
	}
    } // end of getNumber


    // ObjectFileParser constructor
    ObjectFileParser(Reader r) {
	super(r);
	setup();
    } // end of ObjectFileParser

} // End of file ObjectFileParser.java
