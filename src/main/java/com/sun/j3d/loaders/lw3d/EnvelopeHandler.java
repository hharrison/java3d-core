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

import java.io.StreamTokenizer;
import java.io.IOException;
import java.lang.reflect.Constructor;
import com.sun.j3d.loaders.ParsingErrorException;
import java.lang.ClassNotFoundException;
import java.lang.InstantiationException;
import java.lang.IllegalAccessException;
import java.lang.reflect.InvocationTargetException;


/**
 * This class is used in implementing Envelope objects (of which there
 * is currently only one - LightIntensity).
 * The class is called whenever the parser has encountered
 * a token which could have an envelope description.  If the
 * token simply has a numeric value, this value is stored.
 * If, instead, there is an envelope, then the class creates
 * the envelope class and parses that information.
 */

class EnvelopeHandler extends TextfileParser {

    float theValue = 0;
    boolean hasValue = false;
    boolean hasEnvelope = true;
    LwsEnvelope theEnvelope = null;

	/**
	* Constructor: This constructor is used if there is no existing
	* implementation for this type of envelope.  The real constructor
	* is called with the generic LwsEnvelope class name, which will
	* allow s to parse and ignore the envelope data
	*/
    EnvelopeHandler(StreamTokenizer st,
        int totalFrames, float totalTime) {
        this(st, totalFrames, totalTime,
	     "com.sun.j3d.utils.loaders.lw3d.LwsEnvelope");
    }

	/**
	* Constructor: This constructor is called with the name of a class
	* that can handle the encountered envelope.  This is done so that this
	* EnvelopeHandler class can generically call the given class to handle
	* the envelope, whether it results in parsing/ignoring the data or 
	* in actually using the data
	*/
    EnvelopeHandler(StreamTokenizer st,
			   int totalFrames,
			   float totalTime,
			   String envClassName) throws ParsingErrorException {
	try {
	    theValue = (float)getNumber(st);
            hasValue = true;
	}
	catch (NumberFormatException e) {
	    if (st.ttype == '(') {
		st.pushBack();
		// This code creates a new instance for the given class name
		try {
		    Class envClass = Class.forName(envClassName);
		    Constructor constructors[] = envClass.getConstructors();
		    Constructor con = constructors[0];
		    Object args[] = new Object[3];
		    args[0] = (Object)st;
		    args[1] = (Object)(new Integer(totalFrames));
		    args[2] = (Object)(new Float(totalTime));
		    try {
			theEnvelope = (LwsEnvelope)con.newInstance(args);
			hasEnvelope = true;
		    }
		    catch (InstantiationException e3) {
			// Ignore
		    }
		    catch (IllegalAccessException e3) {
			// Ignore
		    }
		    catch (InvocationTargetException e3) {
			// Ignore
		    }
		}
		catch (ClassNotFoundException e2) {
		    // Ignore
		}
	    }
	}
    }
}
