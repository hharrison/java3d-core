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

import java.lang.reflect.* ;
import java.util.List ;
import java.util.ArrayList ;

/**
 * Base class for all configuration objects.  A ConfigObject processes
 * configuration parameters for a target object, which is instantiated after
 * the configuration file is parsed.  The ConfigObject then applies its
 * configuration properties to the target object.<p>
 * 
 * Generic base implementations are provided for the initialize(),
 * setProperty(), and processProperties() methods.  These implementations
 * assume target objects that are unknown and thus instantiated via
 * introspection.  Property names are assumed to be method names that take an
 * array of Objects as a parameter; they are invoked through introspection as
 * well.<p>
 *
 * Most ConfigObjects target concrete Java 3D core classes, so these
 * implementations are usually overridden to instantiate those objects and
 * call their methods directly.
 */
class ConfigObject {
    /**
     * The base name of this object, derived from the configuration command
     * which created it.  This is constructed by stripping off the leading
     * "New" prefix or the trailing "Attribute", "Property", or "Alias" suffix
     * of the command name.  The name of the ConfigObject subclass which
     * handles the command is derived by adding "Config" as a prefix to the
     * base name.
     */
    String baseName = null ;
    
    /**
     * The instance name of this object, as specified in the configuration
     * file.
     */
    String instanceName = null ;
    
    /**
     * The corresponding target object which this ConfigObject is configuring.
     */
    Object targetObject = null ;

    /**
     * The name of the target class this object is configuring.
     */
    String targetClassName = null ;

    /**
     * The Class object for the target.
     */
    Class targetClass = null ;

    /**
     * Configurable properties gathered by this object, represented by the
     * ConfigCommands that set them.
     */
    List properties = new ArrayList() ;

    /**
     * The ConfigContainer in which this ConfigObject is contained.
     */
    ConfigContainer configContainer = null ;
     
    /**
     * The command that created this class.
     */
    ConfigCommand creatingCommand = null ;

    /**
     * If true, this object is an alias to another.
     */
    boolean isAlias = false ;

    /**
     * If isAlias is true, this references the original object.
     */
    ConfigObject original = null ;

    /**
     * List of alias Strings for this object if it's not an alias itself.
     */
    List aliases = new ArrayList() ;
    
    protected ClassLoader classLoader;

    /**
     * @param classLoader the ClassLoader to use when loading the implementation
     * class for this object
     */
    void setClassLoader( ClassLoader classLoader ) {
        this.classLoader = classLoader;
    }
    
    /**
     * The base initialize() implementation.  This takes a ConfigCommand with
     * three arguments:  the command name, the instance name, and the name of
     * the target class this ConfigObject is configuring.  The command in the
     * configuration file should have the form:<p>
     *
     * (New{configType} {instanceName} {className})<p>
     * 
     * For example, (NewDevice tracker com.sun.j3d.input.LogitechTracker) will
     * first cause ConfigDevice to be instantiated, which will then be
     * initialized with this method.  After all the properties are collected,
     * ConfigDevice will instantiate com.sun.j3d.input.LogitechTracker,
     * evaluate its properties, and allow references to it in the
     * configuration file by the name "tracker".<p>
     *
     * It's assumed the target class will be instantiated through
     * introspection and its properties set through introspection as well.
     * Most config objects (ConfigScreen, ConfigView, ConfigViewPlatform,
     * ConfigPhysicalBody, and ConfigPhysicalEnvironment) target a concrete
     * core Java 3D class and will instantiate them directly, so they override
     * this method.
     * 
     * @param c the command that created this ConfigObject
     */
    protected void initialize(ConfigCommand c) {
	if (c.argc != 3) {
	    syntaxError("Wrong number of arguments to " + c.commandName) ;
	}

	if (!isName(c.argv[1])) {
	    syntaxError("The first argument to " + c.commandName +
			" must be the instance name") ;
	}

	if (!isName(c.argv[2])) {
	    syntaxError("The second argument to " + c.commandName +
			" must be the class name") ;
	}

	targetClassName = (String)c.argv[2] ;
    }
    
    /**
     * The base setProperty() implementation.  This implementation assumes the
     * property needs to be set by introspection on the property name as a
     * method that accepts an array of Objects.  That is, the command in the
     * configuration file is of the form:<p>
     *
     * ({type}Property {instance name} {method name} {arg0} ... {argn})<p>
     * 
     * For example, (DeviceProperty tracker SerialPort "/dev/ttya") will
     * invoke the method named "SerialPort" in the object referenced by
     * "tracker" with an array of 1 Object containing the String
     * "/dev/ttya".<p> 
     * 
     * The property is stored as the original ConfigCommand and is evaluated
     * after the configuration file has been parsed.  It is overridden by
     * subclasses that instantiate concrete core Java 3D classes with known
     * method names.
     * 
     * @param c the command that invoked this method
     */
    protected void setProperty(ConfigCommand c) {
	if (c.argc < 4) {
	    syntaxError("Wrong number of arguments to " + c.commandName) ;
	}

	if (!isName(c.argv[1])) {
	    syntaxError("The first argument to " + c.commandName +
			" must be the instance name") ;
	}

	if (!isName(c.argv[2])) {
	    syntaxError("The second argument to " + c.commandName +
			" must be the property name") ;
	}

	properties.add(c) ;
    }

    /**
     * Instantiates the target object.
     */
    protected Object createTargetObject() {
	if (targetClassName == null)
	    return null ;

	targetClass = getClassForName(creatingCommand, targetClassName) ;
	targetObject = getNewInstance(creatingCommand, targetClass) ;

	return targetObject ;
    }

    /**
     * Return the class for the specified class name string.  
     *
     * @param className the name of the class
     * @return the object representing the class
     */
    protected Class getClassForName(ConfigCommand cmd, String className) {
	try {
	    // Use the system class loader.  If the Java 3D jar files are
	    // installed directly in the JVM's lib/ext directory, then the
	    // default class loader won't find user classes outside ext.
            //
            // From 1.3.2 we use the classLoader supplied to this object,
            // normally this will be the system class loader, but for webstart
            // apps the user can supply another class loader.
	    return Class.forName(className, true,
				 classLoader) ;
	}
	catch (ClassNotFoundException e) {
	    throw new IllegalArgumentException
		(errorMessage(cmd, "Class \"" + className + "\" not found")) ;
	}
    }

    /**
     * Return an instance of the class specified by the given class object.
     * 
     * @param objectClass the object representing the class
     * @return a new instance of the class
     */
    protected Object getNewInstance(ConfigCommand cmd, Class objectClass) {
	try {
	    return objectClass.newInstance() ;
	}
	catch (IllegalAccessException e) {
	    throw new IllegalArgumentException
		(errorMessage(cmd, "Illegal access to object class")) ;
	}
	catch (InstantiationException e) {
	    throw new IllegalArgumentException
		(errorMessage(cmd, "Instantiation error for object class")) ;
	}
    }

    /**
     * Evaluate properties for the the given class instance.  The property
     * names are used as the names of methods to be invoked by the instance.
     * Each such method takes an array of Objects as its only parameter.  The
     * array will contain Objects corresponding to the property values.
     */
    protected void processProperties() {
	evaluateProperties(this.targetClass,
			   this.targetObject, this.properties) ;

	// Potentially holds a lot of references, and not needed anymore.
	this.properties.clear() ;
    }

    /**
     * Evaluate properties for the the given class instance.  
     *
     * @param objectClass the class object representing the given class
     * @param objectInstance the class instance whose methods will be invoked
     * @param properties list of property setting commands
     */
    protected void evaluateProperties(Class objectClass,
				      Object objectInstance,
				      List properties) {

	// Holds the single parameter passed to the class instance methods.
	Object[] parameters = new Object[1] ;

	// Specifies the class of the single method parameter.  
	Class[] parameterTypes = new Class[1] ;

	// All property methods use Object[] as their single parameter, which
	// happens to be the same type as the parameters variable above.
	parameterTypes[0] = parameters.getClass() ;

	// Loop through all property commands and invoke the appropriate
	// method for each one.  Property commands are of the form:
	// ({configClass}Property {instanceName} {methodName} {arg} ...)
	for (int i = 0 ; i < properties.size() ; i++) {
	    ConfigCommand cmd = (ConfigCommand)properties.get(i) ;
	    String methodName = (String)cmd.argv[2] ;
	    Object[] argv = new Object[cmd.argc - 3] ;

	    for (int a = 0 ; a < argv.length ; a++) {
		argv[a] = cmd.argv[a + 3] ;
		if (argv[a] instanceof ConfigCommand) {
		    // Evaluate a delayed built-in command.
		    ConfigCommand bcmd = (ConfigCommand)argv[a] ;
		    argv[a] = configContainer.evaluateBuiltIn(bcmd) ;
		}
	    }

	    parameters[0] = argv ;

	    try {
		Method objectMethod = 
		    objectClass.getMethod(methodName, parameterTypes) ;

		objectMethod.invoke(objectInstance, parameters) ;
	    }
	    catch (NoSuchMethodException e) {
		throw new IllegalArgumentException
		    (errorMessage
		     (cmd, "Unknown property \"" + methodName + "\"")) ;
	    }
	    catch (IllegalAccessException e) {
		throw new IllegalArgumentException
		    (errorMessage
		     (cmd, "Illegal access to \"" + methodName + "\"")) ;
	    }
	    catch (InvocationTargetException e) {
		throw new IllegalArgumentException
		    (errorMessage(cmd, e.getTargetException().getMessage())) ;
	    }
	}
    }

    /**
     * Throws an IllegalArgumentException with the specified description.
     * This is caught by the parser which prints out error diagnostics and
     * continues parsing if it can.
     *
     * @param s string describing the syntax error
     */
    protected void syntaxError(String s) {
	throw new IllegalArgumentException(s) ;
    }

    /**
     * Constructs an error message from the given string and file information
     * from the given command.
     */
    static String errorMessage(ConfigCommand cmd, String s) {
	return
	    s + "\nat line " + cmd.lineNumber +
	    " in " + cmd.fileName + "\n" + cmd;
    }

    /**
     * Check if the argument is a name string.
     * 
     * @param o the object to be checked
     * @return true if the object is an instance of String
     */
    protected boolean isName(Object o) {
	return (o instanceof String) ;
    }
}
