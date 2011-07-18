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

import java.text.DecimalFormat ;
import java.text.FieldPosition ;
import java.util.Collection ;
import javax.vecmath.Matrix3d ;
import javax.vecmath.Matrix4d ;

/**
 * Contains the elements which compose a configuration file command,
 * including the command name, type, and arguments.
 */
class ConfigCommand {
    /**
     * Specifies that this command creates a new ConfigObject.
     */
    static final int CREATE = 0 ;

    /**
     * Specifies that this command sets an attribute for a class known
     * to ConfiguredUniverse.  As of Java 3D 1.3.1, these commands are
     * handled the same as property commands (see PROPERTY below) and
     * this constant is no longer used.
     */
    static final int ATTRIBUTE = 1 ;

    /**
     * Specifies that this command sets a Java system property or a
     * property for a class unknown to ConfiguredUniverse.  Properties
     * for such a class are set by using the reflection API to invoke a
     * method whose name is specified in the command's argument list.
     * Such a method must accept an array of Object as its sole
     * parameter, where that array contains all the command elements
     * which appear after the method name.<p>
     *
     * As of Java 3D 1.3.1, this is handled the same as an attribute.
     * The individual setProperty() method implementations of
     * ConfigObject determine whether the method to set the property can
     * be invoked directly or through introspection.  If through
     * introspection, then the evaluation of the property must be
     * delayed until the target object is instantiated.
     */
    static final int PROPERTY = 2 ;

    /**
     * Specifies that this command creates an alias for a ConfigObject of the
     * same base name.
     */
    static final int ALIAS = 3 ;

    /**
     * Specifies that this command is a deferred built-in command that can't
     * be immediately evaluated by the parser.  Its evaluation is delayed
     * until all config objects are instantiated and their properties can be
     * evaluated.
     */
    static final int BUILTIN = 4 ;

    /**
     * Specifies that this command is an include file directive.
     */
    static final int INCLUDE = 5 ;

    /**
     * Specifes that this command is entirely processed by the
     * constructor and should be ignored by subsequent recipients.
     */
    static final int IGNORE = 6 ;

    /**
     * The type of this command, either CREATE, PROPERTY, ALIAS,
     * BUILTIN, INCLUDE, or IGNORE.
     */
    int type = -1 ;

    /**
     * The number of arguments in this command, including the command
     * name.
     */
    int argc = 0 ;

    /**
     * An array containing all of this command's arguments, including
     * the command name.
     */
    Object[] argv = null ;

    /**
     * The name of the command being invoked, which is always the first
     * argument of the command.
     */
    String commandName = null ;

    /**
     * The base name of this command, from which the name of the ConfigObject
     * subclass that processes it is derived.  This is constructed by
     * stripping off the leading "New" prefix or the trailing "Attribute",
     * "Property", or "Alias" suffix of the command name.  The name of the
     * ConfigObject subclass which handles the command is derived by adding
     * "Config" as a prefix to the base name.
     */
    String baseName = null ;

    /**
     * The instance name of the ConfigObject subclass which processes this
     * command.  Together with the base name this provides the handle by which
     * a ConfigObject can be referenced by other commands in the configuration
     * file.
     */
    String instanceName = null ;

    /**
     * The file from which this command was read.
     */
    String fileName = null ;

    /**
     * The line number from which this command was read.
     */
    int lineNumber = 0 ;

    /**
     * Constructs a ConfigCommand from configuration file command arguments.
     *
     * @param elements arguments to this command, including the command name
     * @param fileName name of the file from where the command was read
     * @param lineNumber line number where the command is found in the file
     */
    ConfigCommand(Collection elements, String fileName, int lineNumber) {
	this.fileName = fileName ;
	this.lineNumber = lineNumber ;

	argc = elements.size() ;
	argv = elements.toArray(new Object[0]) ;

	if (! (argc > 0 && (argv[0] instanceof String)))
	    throw new IllegalArgumentException("malformed command") ;

	commandName = (String)argv[0] ;

	if (commandName.startsWith("New")) {
	    type = CREATE ;
	    baseName = commandName.substring(3) ;
	    instanceName = checkName(argv[1]) ;
	}
	else if (commandName.endsWith("Property")) {
	    baseName = commandName.substring(0, commandName.length()-8) ;
	    if (baseName.equals("Java")) {
		type = IGNORE ;
		processJavaProperty(argc, argv) ;
	    }
	    else {
		type = PROPERTY ;
		instanceName = checkName(argv[1]) ;
	    }
	}
	else if (commandName.endsWith("Attribute")) {
	    // Backward compatibility.
	    type = PROPERTY ;
	    baseName = commandName.substring(0, commandName.length()-9) ;
	    instanceName = checkName(argv[1]) ;
	}
	else if (commandName.endsWith("Alias")) {
	    type = ALIAS ;
	    baseName = commandName.substring(0, commandName.length()-5) ;
	    instanceName = checkName(argv[1]) ;
	}
	else if (commandName.equals("Include")) {
	    type = INCLUDE ;
	}
	else {
	    type = BUILTIN ;
	}

	// We allow "Window" as an equivalent to "Screen".
	if (baseName != null && baseName.equals("Window"))
	    baseName = "Screen" ;
    }

    /**
     * Sets the Java property specified in the command.  If the command
     * has 3 arguments then it's an unconditional assignment.  If the
     * 3rd argument is "Default", then the property is set to the value
     * of the 4th argument only if the specified property has no
     * existing value.
     *
     * @param argc the number of arguments in the command
     * @param argv command arguments as an array of Objects; the 1st is
     *  the command name (ignored), the 2nd is the name of the Java
     *  property, the 3rd is the value to be set or the keyword
     *  "Default", and the 4th is thevalue to be set if the Java
     *  property doesn't already exist
     */
    private static void processJavaProperty(int argc, Object[] argv) {
	for (int i = 1 ; i < argc ; i++) {
	    // Check args.
	    if (argv[i] instanceof Boolean) {
		argv[i] = ((Boolean)argv[i]).toString() ;
	    }
	    else if (! (argv[i] instanceof String)) {
		throw new IllegalArgumentException
		    ("JavaProperty arguments must be Strings or Booleans") ;
	    }
	}
	if (argc == 3) {
	    // Unconditional assignment.
	    setJavaProperty((String)argv[1], (String)argv[2]) ;
	}
	else if (argc != 4) {
	    // Conditional assignment must have 4 args.
	    throw new IllegalArgumentException
		("JavaProperty must have either 2 or 3 arguments") ;
	}
	else if (! ((String)argv[2]).equals("Default")) {
	    // Penultimate arg must be "Default" keyword.
	    throw new IllegalArgumentException
		("JavaProperty 2nd argument must be \"Default\"") ;
	}
	else if (evaluateJavaProperty((String)argv[1]) == null) {
	    // Assignment only if no existing value.
	    setJavaProperty((String)argv[1], (String)argv[3]) ;
	}
    }

    /**
     * Sets the given Java system property if allowed by the security manager.
     * 
     * @param key property name
     * @param value property value
     * @return previous property value if any
     */
    static String setJavaProperty(final String key, final String value) {
	return (String)java.security.AccessController.doPrivileged
	    (new java.security.PrivilegedAction() {
		public Object run() {
		    return System.setProperty(key, value) ;
		}
   	    }) ;
    }

    /**
     * Evaluates the specified Java property string if allowed by the security
     * manager. 
     *
     * @param key string containing a Java property name
     * @return string containing the Java property valaue
     */
    static String evaluateJavaProperty(final String key) {
	return (String)java.security.AccessController.doPrivileged
	    (new java.security.PrivilegedAction() {
		public Object run() {
		    return System.getProperty(key) ;
		}
   	    }) ;
    }

    /**
     * Checks if the given object is an instance of String.
     *
     * @param o the object to be checked
     * @return the object cast to a String
     * @exception IllegalArgumentException if the object is not a String
     */
    private final String checkName(Object o) {
	if (! (o instanceof String))
	    throw new IllegalArgumentException
		("second argument to \"" + commandName + "\" must be a name") ;

	return (String)o ;
    }

    /**
     * Calls <code>formatMatrixRows(3, 3, m)</code>, where <code>m</code> is a
     * an array of doubles retrieved from the given Matrix3d.
     *
     * @param m3 matrix to be formatted
     * @return matrix rows formatted into strings
     */
    static String[] formatMatrixRows(Matrix3d m3) {
	double[] m = new double[9] ;
	m[0] = m3.m00 ;  m[1] = m3.m01 ; m[2] = m3.m02 ;
	m[3] = m3.m10 ;  m[4] = m3.m11 ; m[5] = m3.m12 ;
	m[6] = m3.m20 ;  m[7] = m3.m21 ; m[8] = m3.m22 ;

	return formatMatrixRows(3, 3, m) ;
    }

    /**
     * Calls <code>formatMatrixRows(4, 4, m)</code>, where <code>m</code> is a
     * an array of doubles retrieved from the given Matrix4d.
     *
     * @param m4 matrix to be formatted
     * @return matrix rows formatted into strings
     */
    static String[] formatMatrixRows(Matrix4d m4) {
	double[] m = new double[16] ;
	m[0]  = m4.m00 ;  m[1]  = m4.m01 ; m[2]  = m4.m02 ; m[3]  = m4.m03 ;
	m[4]  = m4.m10 ;  m[5]  = m4.m11 ; m[6]  = m4.m12 ; m[7]  = m4.m13 ;
	m[8]  = m4.m20 ;  m[9]  = m4.m21 ; m[10] = m4.m22 ; m[11] = m4.m23 ;
	m[12] = m4.m30 ;  m[13] = m4.m31 ; m[14] = m4.m32 ; m[15] = m4.m33 ;

	return formatMatrixRows(4, 4, m) ;
    }

    /**
     * Formats a matrix with fixed fractional digits and integer padding to
     * align the decimal points in columns.  Non-negative numbers print up to
     * 7 integer digits, while negative numbers print up to 6 integer digits
     * to account for the negative sign.  6 fractional digits are printed.
     *
     * @param rowCount number of rows in the matrix
     * @param colCount number of columns in the matrix
     * @param m matrix to be formatted
     * @return matrix rows formatted into strings
     */
    static String[] formatMatrixRows(int rowCount, int colCount, double[] m) {
        DecimalFormat df = new DecimalFormat("0.000000") ;
        FieldPosition fp = new FieldPosition(DecimalFormat.INTEGER_FIELD) ;
        StringBuffer sb0 = new StringBuffer() ;
        StringBuffer sb1 = new StringBuffer() ;
	String[] rows = new String[rowCount] ;

        for (int i = 0 ; i < rowCount ; i++) {
            sb0.setLength(0) ;
            for (int j = 0 ; j < colCount ; j++) {
                sb1.setLength(0) ;
                df.format(m[i*colCount+j], sb1, fp) ;
                int pad = 8 - fp.getEndIndex() ;
                for (int k = 0 ; k < pad ; k++) {
                    sb1.insert(0, " ") ;
                }
                sb0.append(sb1) ;
            }
	    rows[i] = sb0.toString() ;
        }
	return rows ;
    }

    /**
     * Returns the String representation of this command.
     * 
     * @return string representing this command
     */
    public String toString() {
	String[] lines = null ;
	StringBuffer sb = new StringBuffer("(") ;

	for (int i = 0 ; i < argc ; i++) {
	    if (argv[i] instanceof Matrix3d) {
		lines = formatMatrixRows((Matrix3d)argv[i]) ;
		sb.append("\n ((" + lines[0] + ")\n") ;
		sb.append("  (" + lines[1] + ")\n") ;
		sb.append("  (" + lines[2] + "))") ;
		if (i != (argc - 1)) sb.append("\n") ;
	    }
	    else if (argv[i] instanceof Matrix4d) {
		lines = formatMatrixRows((Matrix4d)argv[i]) ;
		sb.append("\n ((" + lines[0] + ")\n") ;
		sb.append("  (" + lines[1] + ")\n") ;
		sb.append("  (" + lines[2] + ")\n") ;
		sb.append("  (" + lines[3] + "))") ;
		if (i != (argc - 1)) sb.append("\n") ;
	    }
	    else {
		if (i > 0) sb.append(" ") ;
		sb.append(argv[i].toString()) ;
	    }
	}
	
	sb.append(")") ;
	return sb.toString() ;
    }
}
