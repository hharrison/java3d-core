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

package com.sun.j3d.utils.universe ;

import java.awt.event.* ;
import java.io.* ;
import java.lang.Integer ;
import java.lang.Boolean ;
import java.util.* ;
import javax.vecmath.* ;
import javax.media.j3d.* ;

class ConfigSexpression {

    private ArrayList elements = new ArrayList() ;

    private void syntaxError(StreamTokenizer st, String file, String s) {
	System.out.println(s + ":\nat line " + st.lineno() + " in " + file) ;
	print() ; System.out.print("\n\n") ;
    }

    private int myNextToken(StreamTokenizer st, String file) {
	int tok = 0 ;
	try {
	    tok = st.nextToken() ;
	}
	catch (IOException e) {
	    throw new RuntimeException(e + "\nwhile reading " + file) ;
	}
	return tok ;
    }


    Object parseAndEval(ConfigContainer configContainer,
			StreamTokenizer st, int level) {
	int tok ;
	String s ;
	String file = configContainer.currentFileName ;

	//
	// First tokenize the character stream and add the tokens to the
	// elements array.
	//
	elements.clear() ;

	// Look for an open paren to start this sexp.
	while (true) {
	    tok = myNextToken(st, file) ;

	    if (tok == StreamTokenizer.TT_EOF)
		return Boolean.FALSE ;

	    if (tok == ')')
		syntaxError(st, file, "Premature closing parenthesis") ;

	    if (tok == '(')
		break ;
	}

	// Add elements until a close paren for this sexp is found.
	for (int i = 0 ; true ; i++) {
	    tok = myNextToken(st, file) ;

	    if (tok == StreamTokenizer.TT_EOF) {
		syntaxError(st, file, "Missing closing parenthesis") ;
		break ;
	    }

	    // An open paren starts a new embedded sexp.  Put the paren back,
	    // evaluate the sexp, and add the result to the elements list.
	    if (tok == '(') {
		st.pushBack() ;
		ConfigSexpression cs = new ConfigSexpression() ;
		elements.add(cs.parseAndEval(configContainer, st, level+1)) ;
		continue ;
	    }

	    // A close paren finishes the scan.
	    if (tok == ')')
		break ;
	    
	    // Check for too many arguments.
	    if (i >= 20)
		syntaxError(st, file, "Too many arguments") ;

	    // Check for numeric argument.
	    if (tok == StreamTokenizer.TT_NUMBER) {
		elements.add(new Double(st.nval)) ;
		continue ;
	    }

	    // Anything other than a word or a quoted string is an error.
	    if (tok != StreamTokenizer.TT_WORD && tok != '"' && tok != '\'') {
		String badToken = String.valueOf((char)tok) ;
		elements.add(badToken) ;  // so bad token prints out
		syntaxError(st, file, "Invalid token \"" + badToken +
			    "\" must be enclosed in quotes") ;
		continue ;
	    }

	    // Scan the token for Java property substitution syntax ${...}.
	    s = scanJavaProperties(st, file, st.sval) ;
	    if (s == null) continue ;

	    if (s.equalsIgnoreCase("true"))
		// Replace "true" or "True" with the Boolean equivalent.
		elements.add(new Boolean(true)) ;

	    else if (s.equalsIgnoreCase("false"))
		// Replace "false" or "False" with the Boolean equivalent.
		elements.add(new Boolean(false)) ;

	    else
		// Add the token as a string element.
		elements.add(s) ;
	}


	//
	// Now evaluate elements.
	//
	if (elements.size() == 0)
	    syntaxError(st, file, "Null command") ;

	// If the first argument is a string, then this sexp must be
	// a top-level command or a built-in, and needs to be evaluated.
	if (elements.get(0) instanceof String) {
	    try {
		if (level == 0) {
		    configContainer.evaluateCommand(elements, st.lineno()) ;

		    // Continue parsing top-level commands.
		    return Boolean.TRUE ;
		}
		else {
		    // Evaluate built-in and return result to next level up.
		    return evaluateBuiltIn
			(configContainer, elements, st.lineno()) ;
		}
	    }
	    catch (IllegalArgumentException e) {
		syntaxError(st, file, e.getMessage()) ;
		if (level == 0)
		    // Command ignored: continue parsing.
		    return Boolean.TRUE ;
		else
		    // Function ignored: return sexp to next level up so error
		    // processing can print it out in context of command.
		    return this ;
	    }
	}

	// If the first argument isn't a string, and we are at level 0,
	// this is a syntax error.
	if (level == 0)
	    syntaxError(st, file, "Malformed top-level command name") ;

	// If the first argument is a number, then we must have
	// either a 2D, 3D, or 4D numeric vector.
	if (elements.get(0) instanceof Double) {
	    if (elements.size() == 1)
		syntaxError(st, file, "Can't have single-element vector") ;
	    
	    // Point2D
	    if (elements.size() == 2) {
		if (!(elements.get(1) instanceof Double))
		    syntaxError(st, file, "Both elements must be numbers") ;

		return new Point2d(((Double)elements.get(0)).doubleValue(),
				   ((Double)elements.get(1)).doubleValue()) ;
	    }

	    // Point3d
	    if (elements.size() == 3) {
		if (!(elements.get(1) instanceof Double) ||
		    !(elements.get(2) instanceof Double))
		    syntaxError(st, file, "All elements must be numbers") ;

		return new Point3d(((Double)elements.get(0)).doubleValue(),
				   ((Double)elements.get(1)).doubleValue(),
				   ((Double)elements.get(2)).doubleValue()) ;
	    }

	    // Point4D
	    if (elements.size() == 4) {
		if (!(elements.get(1) instanceof Double) ||
		    !(elements.get(2) instanceof Double) ||
		    !(elements.get(3) instanceof Double))
		    syntaxError(st, file, "All elements must be numbers") ;

		return new Point4d(((Double)elements.get(0)).doubleValue(),
				   ((Double)elements.get(1)).doubleValue(),
				   ((Double)elements.get(2)).doubleValue(),
				   ((Double)elements.get(3)).doubleValue()) ;
	    }

	    // Anything else is an error.
	    syntaxError(st, file, "Too many vector elements") ;
	}

	// If the first argument is a Point3d, then we should be a Matrix3d.
	if (elements.get(0) instanceof Point3d) {
	    if (elements.size() != 3)
		syntaxError(st, file, "Matrix must have three rows") ;

	    if (!(elements.get(1) instanceof Point3d) ||
		!(elements.get(2) instanceof Point3d))
		syntaxError(st, file, "All rows must have three elements") ;

	    return new Matrix3d(((Point3d)elements.get(0)).x,
				((Point3d)elements.get(0)).y,
				((Point3d)elements.get(0)).z,
				((Point3d)elements.get(1)).x,
				((Point3d)elements.get(1)).y,
				((Point3d)elements.get(1)).z,
				((Point3d)elements.get(2)).x,
				((Point3d)elements.get(2)).y,
				((Point3d)elements.get(2)).z) ;
	}

	// If the first argument is a Point4d, then we should be a Matrix4d.
	if (elements.get(0) instanceof Point4d) {
	    if (elements.size() == 3) {
		if (!(elements.get(1) instanceof Point4d) ||
		    !(elements.get(2) instanceof Point4d))
		    syntaxError(st, file, "All rows must have four elements") ;

		return new Matrix4d(((Point4d)elements.get(0)).x,
				    ((Point4d)elements.get(0)).y,
				    ((Point4d)elements.get(0)).z,
				    ((Point4d)elements.get(0)).w,
				    ((Point4d)elements.get(1)).x,
				    ((Point4d)elements.get(1)).y,
				    ((Point4d)elements.get(1)).z,
				    ((Point4d)elements.get(1)).w,
				    ((Point4d)elements.get(2)).x,
				    ((Point4d)elements.get(2)).y,
				    ((Point4d)elements.get(2)).z,
				    ((Point4d)elements.get(2)).w,
				    0.0, 0.0, 0.0, 1.0) ;
	    }
	    else if (elements.size() != 4)
		syntaxError(st, file, "Matrix must have three or four rows") ;

	    if (!(elements.get(1) instanceof Point4d) ||
		!(elements.get(2) instanceof Point4d) ||
		!(elements.get(3) instanceof Point4d))
		syntaxError(st, file, "All rows must have four elements") ;

	    return new Matrix4d(((Point4d)elements.get(0)).x,
				((Point4d)elements.get(0)).y,
				((Point4d)elements.get(0)).z,
				((Point4d)elements.get(0)).w,
				((Point4d)elements.get(1)).x,
				((Point4d)elements.get(1)).y,
				((Point4d)elements.get(1)).z,
				((Point4d)elements.get(1)).w,
				((Point4d)elements.get(2)).x,
				((Point4d)elements.get(2)).y,
				((Point4d)elements.get(2)).z,
				((Point4d)elements.get(2)).w,
				((Point4d)elements.get(3)).x,
				((Point4d)elements.get(3)).y,
				((Point4d)elements.get(3)).z,
				((Point4d)elements.get(3)).w) ;
	}

	// Anything else is an error.
	syntaxError(st, file, "Syntax error") ;
	return null ;
    }

    /**
     * Scan for Java properties in the specified string.  Nested properties are
     * not supported.
     *
     * @param st stream tokenizer in use
     * @param f current file name
     * @param s string containing non-nested Java properties possibly
     *  interspersed with arbitrary text.
     * @return scanned string with Java properties replaced with values
     */
    private String scanJavaProperties(StreamTokenizer st, String f, String s) {
	int open = s.indexOf("${") ;
	if (open == -1) return s ;

	int close = 0 ;
	StringBuffer buf = new StringBuffer() ;
	while (open != -1) {
	    buf.append(s.substring(close, open)) ;
	    close = s.indexOf('}', open) ;
	    if (close == -1) {
		elements.add(s) ;  // so that the bad element prints out
		syntaxError(st, f, "Java property substitution syntax error") ;
		return null ;
	    }

	    String property = s.substring(open + 2, close) ;
	    String value = ConfigCommand.evaluateJavaProperty(property) ;
	    if (value == null) {
		elements.add(s) ;  // so that the bad element prints out
		syntaxError(st, f, "Java property \"" + property +
			    "\" has a null value") ;
		return null ;
	    }

	    buf.append(value) ;
	    open = s.indexOf("${", close) ;
	    close++ ;
	}

	buf.append(s.substring(close)) ;
	return buf.toString() ;
    }

    /**
     * This method gets called from the s-expression parser to evaluate a
     * built-in command.
     * 
     * @param elements tokenized list of sexp elements
     * @return object representing result of evaluation
     */
    private Object evaluateBuiltIn(ConfigContainer configContainer,
				   ArrayList elements, int lineNumber) {
	int argc ;
	String functionName ;

	argc = elements.size() ;
	functionName = (String)elements.get(0) ;

	if (functionName.equals("Rotate")) {
	    return makeRotate(elements) ;
	}
	else if (functionName.equals("Translate")) {
	    return makeTranslate(elements) ;
	}
	else if (functionName.equals("RotateTranslate") ||
		 functionName.equals("TranslateRotate") ||
		 functionName.equals("Concatenate")) {

	    return concatenate(elements) ;
	}
	else if (functionName.equals("BoundingSphere")) {
	    return makeBoundingSphere(elements) ;
	}
	else {
	    // This built-in can't be evaluated immediately or contains an
	    // unknown command.  Create a ConfigCommand for later evaluation.
	    return new ConfigCommand
		(elements, configContainer.currentFileName, lineNumber) ;
	}
    }

    /**
     * Processes the built-in command (Translate x y z).
     * 
     * @param elements ArrayList containing Doubles wrapping x, y, and z
     * translation components at indices 1, 2, and 3 respectively
     * 
     * @return matrix that translates by the given x, y, and z components
     */
    private Matrix4d makeTranslate(ArrayList elements) {
	if (elements.size() != 4) {
	    throw new IllegalArgumentException
		("Incorrect number of arguments to Translate") ;
	}

	if (!(elements.get(1) instanceof Double) ||
	    !(elements.get(2) instanceof Double) ||
	    !(elements.get(3) instanceof Double)) {
	    throw new IllegalArgumentException
		("All arguments to Translate must be numbers") ;
	}

	Matrix4d m4d = new Matrix4d() ;
	m4d.set(new Vector3d(((Double)elements.get(1)).doubleValue(),
			     ((Double)elements.get(2)).doubleValue(),
			     ((Double)elements.get(3)).doubleValue())) ;

	return m4d ;
    }

    /**
     * Processes the (Rotate x y z) built-in command.
     * 
     * @param elements ArrayList containing Doubles wrapping x, y, and z Euler
     * angles at indices 1, 2, and 3 respectively
     * 
     * @return matrix that rotates by the given Euler angles around static X,
     * Y, and Z basis vectors: first about X, then Y, and then Z
     * 
     * @see Transform3D#setEuler()
     */
    private Matrix4d makeRotate(ArrayList elements) {
	if (elements.size() != 4) {
	    throw new IllegalArgumentException
		("Incorrect number of arguments to Rotate") ;
	}

	if (!(elements.get(1) instanceof Double) ||
	    !(elements.get(2) instanceof Double) ||
	    !(elements.get(3) instanceof Double)) {
	    throw new IllegalArgumentException
		("All arguments to Rotate must be numbers") ;
	}

	double x = Math.toRadians(((Double)elements.get(1)).doubleValue()) ;
	double y = Math.toRadians(((Double)elements.get(2)).doubleValue()) ;
	double z = Math.toRadians(((Double)elements.get(3)).doubleValue()) ;

	Transform3D t3d = new Transform3D() ;
	t3d.setEuler(new Vector3d(x, y, z)) ;

	Matrix4d m4d = new Matrix4d() ;
	t3d.get(m4d) ;

	return m4d ;
    }

    /**
     * Processes the (RotateTranslate m1 m2), (TranslateRotate m1 m2), and
     * (Concatenate m1 m2) built-in commands. Although these do exactly the
     * same thing, using the appropriate command is recommended in order to
     * explicitly describe the sequence of transforms and their intent.
     * 
     * @param elements ArrayList containing Matrix4d objects m1 and m2 at
     * indices 1 and 2 respectively
     * 
     * @return matrix that concatenates m1 and m2 in that order: if a point is
     * transformed by the resulting matrix, then in effect the points are
     * first transformed by m1 and then m2
     */
    private Matrix4d concatenate(ArrayList elements) {
	String functionName = (String)elements.get(0) ;

	if (elements.size() != 3) {
	    throw new IllegalArgumentException
		("Incorrect number of arguments to " + functionName) ;
	}

	if (!(elements.get(1) instanceof Matrix4d) ||
	    !(elements.get(2) instanceof Matrix4d)) {
	    throw new IllegalArgumentException
		("Both arguments to " + functionName + " must be Matrix4d") ;
	}

	// Multiply the matrices in the order such that the result, when
	// transforming a 3D point, will apply the transform represented by
	// the 1st matrix and then apply the transform represented by the 2nd
	// matrix.
	Matrix4d m4d = new Matrix4d((Matrix4d)elements.get(2)) ;
	m4d.mul((Matrix4d)elements.get(1)) ;

	return m4d ;
    }

    /**
     * Processes the built-in command (BoundingSphere center radius).
     * This is used when configuring behaviors.
     * 
     * @param elements ArrayList containing Point3d at index 1 for the sphere
     * center and Double at index 2 wrapping the sphere radius, or the String
     * "infinite" at index 2.
     * 
     * @return BoundingSphere with the given center and radius
     */
    private BoundingSphere makeBoundingSphere(ArrayList elements) {
	if (elements.size() != 3) {
	    throw new IllegalArgumentException
		("Incorrect number of arguments to BoundingSphere") ;
	}

	if (! (elements.get(1) instanceof Point3d) ||
	    ! (elements.get(2) instanceof Double ||
	       elements.get(2) instanceof String))
	    throw new IllegalArgumentException
		("BoundingSphere needs a Point3d center " +
		 "followed by a Double radius or the String \"infinite\"") ;

	double r ;
	if (elements.get(2) instanceof Double)
	    r = ((Double)elements.get(2)).doubleValue() ;
	else
	    r = Double.POSITIVE_INFINITY ;

	return new BoundingSphere((Point3d)elements.get(1), r) ;
    }

    void print() {
	System.out.print("(") ;
	int argc = elements.size() ;
	for (int i = 0 ; i < argc ; i++) {
	    if (elements.get(i) instanceof Matrix3d) {
		String[] rows = ConfigCommand.formatMatrixRows
		    ((Matrix3d)elements.get(i)) ;
		System.out.println("\n ((" + rows[0] + ")") ;
		System.out.println("  (" + rows[1] + ")") ;
		System.out.print("  (" + rows[2] + "))") ;
		if (i != (argc - 1)) System.out.println() ;
	    }
	    else if (elements.get(i) instanceof Matrix4d) {
		String[] rows = ConfigCommand.formatMatrixRows
		    ((Matrix4d)elements.get(i)) ;
		System.out.println("\n ((" + rows[0] + ")") ;
		System.out.println("  (" + rows[1] + ")") ;
		System.out.println("  (" + rows[2] + ")") ;
		System.out.print("  (" + rows[3] + "))") ;
		if (i != (argc - 1)) System.out.println() ;
	    }
	    else if (elements.get(i) instanceof ConfigSexpression) {
		if (i > 0) System.out.print(" ") ;
		((ConfigSexpression)elements.get(i)).print() ;
		if (i != (argc - 1)) System.out.println() ;
	    }
	    else if (elements.get(i) instanceof ConfigCommand) {
		if (i > 0) System.out.print(" ") ;
		System.out.print(elements.get(i).toString()) ;
		if (i != (argc - 1)) System.out.println() ;
	    }
	    else {
		if (i > 0) System.out.print(" ") ;
		System.out.print(elements.get(i).toString()) ;
	    }
	}
	System.out.print(")") ;
    }
}
