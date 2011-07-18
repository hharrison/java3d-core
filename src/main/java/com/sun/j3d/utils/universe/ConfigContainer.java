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

import java.io.* ;
import java.util.* ;
import java.net.URL ;
import java.net.MalformedURLException ;
import javax.media.j3d.* ;
import com.sun.j3d.utils.behaviors.vp.ViewPlatformBehavior ;

/**
 * Loads a Java 3D configuration file and creates a container of named objects
 * that will effect the viewing configuration specified in the file.  These
 * can include Viewers, ViewingPlatforms, ViewPlatformBehaviors, InputDevices,
 * Sensors, and other objects.<p>
 * 
 * Clients can construct the view side of a scene graph by retrieving these
 * objects using the accessor methods provided by this class.  This could
 * involve as little as just attaching ViewingPlatforms to a Locale, depending
 * upon how completely the viewing configuration is specified in the file.
 * The ConfiguredUniverse class is an example of a ConfigContainer client and
 * how it can be used.<p>
 *
 * ConfigContainer can be useful for clients other than ConfiguredUniverse.
 * InputDevice and ViewPlatformBehavior configuration is fully supported, so a
 * given Java 3D installation can provide configuration files to an
 * application that will allow it to fully utilize whatever site-specific
 * devices and behaviors are available.  The configuration mechanism can be
 * extended for any target object through the use of the
 * <code>NewObject</code> and <code>ObjectProperty</code> configuration
 * commands.
 *
 * @see ConfiguredUniverse
 * @see <a href="doc-files/config-syntax.html">
 *      The Java 3D Configuration File</a>
 * @see <a href="doc-files/config-examples.html">
 *      Example Configuration Files</a>
 *
 * @since Java 3D 1.3.1
 */
public class ConfigContainer {
    //
    // The configuration object database is implemented with a HashMap which
    // maps their class names to ArrayList objects which contain the actual
    // instances.  The latter are used since the instances of a given class
    // must be evaluated in the order in which they were created.
    // LinkedHashMap is available in JDK 1.4 but currently this code must run
    // under JDK 1.3.1 as well.
    // 
    private Map baseNameMap = new HashMap() ;

    // Map containing named canvases for each view.
    private Map viewCanvasMap = new HashMap() ;

    // Read-only Maps for the public interface to the configuration database.
    private ReadOnlyMap bodyMap = null ;
    private ReadOnlyMap environmentMap = null ;
    private ReadOnlyMap viewerMap = null ;
    private ReadOnlyMap deviceMap = null ;
    private ReadOnlyMap sensorMap = null ;
    private ReadOnlyMap behaviorMap = null ;
    private ReadOnlyMap platformMap = null ;
    private ReadOnlyMap genericObjectMap = null ;

    // Read-only Sets for the public interface to the configuration database.
    private ReadOnlySet bodies = null ;
    private ReadOnlySet environments = null ;
    private ReadOnlySet viewers = null ;
    private ReadOnlySet devices = null ;
    private ReadOnlySet sensors = null ;
    private ReadOnlySet behaviors = null ;
    private ReadOnlySet platforms = null ;
    private ReadOnlySet genericObjects = null ;

    // The number of TransformGroups to include in ViewingPlatforms.
    private int transformCount = 1 ;

    // The visibility status of Viewer AWT components.
    private boolean setVisible = false ;
    
    private ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    /**
     * The name of the file this ConfigContainer is currently loading.
     */
    String currentFileName = null ;

    /**
     * Creates a new ConfigContainer and loads the configuration file at the
     * specified URL.  All ViewingPlatform instances are created with a single
     * TransformGroup and all Viewer components are initially invisible.
     *
     * @param userConfig URL of the configuration file to load
     */
    public ConfigContainer(URL userConfig) {
	this(userConfig, false, 1, true) ;
    }

    /**
     * Creates a new ConfigContainer and loads the configuration file at the
     * specified URL.  All ViewingPlatform instances are created with a single
     * TransformGroup and all Viewer components are initially invisible.
     *
     * @param userConfig URL of the configuration file to load
     * @param classLoader the class loader to use to load classes specified
     * in the config file.
     */
    public ConfigContainer(URL userConfig, ClassLoader classLoader) {
	this(userConfig, false, 1, true, classLoader) ;
    }

    /**
     * Creates a new ConfigContainer and loads the configuration file at the
     * specified URL.  Any ViewingPlatform instantiated by the configuration
     * file will be created with the specified number of transforms.  Viewer
     * components may be set initially visible or invisible with the
     * <code>setVisible</code> flag.
     *
     * @param userConfig URL of the configuration file to load
     * @param setVisible if true, <code>setVisible(true)</code> is called on
     *  all Viewers
     * @param transformCount number of transforms to be included in any
     *  ViewingPlatform created; must be greater than 0
     */
    public ConfigContainer(URL userConfig,
			   boolean setVisible, int transformCount) {

	this(userConfig, setVisible, transformCount, true) ;
    }
	
    /**
     * Creates a new ConfigContainer and loads the configuration file at the
     * specified URL.  Any ViewingPlatform instantiated by the configuration
     * file will be created with the specified number of transforms.  Viewer
     * components may be set initially visible or invisible with the
     * <code>setVisible</code> flag.
     *
     * @param userConfig URL of the configuration file to load
     * @param setVisible if true, <code>setVisible(true)</code> is called on
     *  all Viewers
     * @param transformCount number of transforms to be included in any
     *  ViewingPlatform created; must be greater than 0
     * @param classLoader the class loader to use to load classes specified
     * in the config file.
     */
    public ConfigContainer(URL userConfig,
			   boolean setVisible, int transformCount,
                           ClassLoader classLoader) {

	this(userConfig, setVisible, transformCount, true, classLoader) ;
    }

    /**
     * Package-scoped constructor for ConfigContainer.  This provides an
     * additional flag, <code>attachBehaviors</code>, which indicates whether
     * or not ViewPlatformBehaviors should be attached to the ViewingPlatforms
     * specified for them.<p>
     *
     * Normally the flag should be true.  However, when instantiated by
     * ConfiguredUniverse, this flag is set false so that ConfiguredUniverse
     * can set a reference to itself in the ViewingPlatform before attaching
     * the behavior.  This provides backwards compatibility to behaviors that
     * access the ConfiguredUniverse instance from a call to
     * <code>setViewingPlatform</code> in order to look up the actual Sensor,
     * Viewer, Behavior, etc., instances associated with the names provided
     * them from the configuration file.<p>
     * 
     * The preferred methods to retrieve instances of specific objects defined
     * in the configuration file are to either 1) get the ConfiguredUniverse
     * instance when the behavior's <code>initialize</code> method is called,
     * or to 2) define properties that accept object instances directly, and
     * then use the newer Device, Sensor, ViewPlatform, etc., built-in
     * commands in the configuration file.  These built-ins will return an
     * object instance from a name.
     *
     * @param userConfig URL of the configuration file to load
     * @param setVisible if true, <code>setVisible(true)</code> is called on
     *  all Viewers
     * @param transformCount number of transforms to be included in any
     *  ViewingPlatform created; must be greater than 0
     * @param attachBehaviors if true, attach ViewPlatformBehaviors to the
     *  appropriate ViewingPlatforms
     */
    ConfigContainer(URL userConfig, boolean setVisible,
		    int transformCount, boolean attachBehaviors) {

	if (transformCount < 1)
	    throw new IllegalArgumentException
		("transformCount must be greater than 0") ;

	loadConfig(userConfig) ;
	processConfig(setVisible, transformCount, attachBehaviors) ;
    }
	
    /**
     * Package scoped constructor that adds the ability to set the ClassLoader
     * which will be used to load any app specific classes specified in the
     * configuration file. By default SystemClassLoader is used.
     */
    ConfigContainer(URL userConfig, boolean setVisible,
		    int transformCount, boolean attachBehaviors,
                    ClassLoader classLoader) {
        this(userConfig, setVisible, transformCount, attachBehaviors);
        this.classLoader = classLoader;
    }

   /**
     * Open, parse, and load the contents of a configuration file.
     * 
     * @param userConfig location of the configuration file
     */
    private void loadConfig(URL userConfig) {
	InputStream inputStream = null ;
	StreamTokenizer streamTokenizer = null ;
	String lastFileName = currentFileName ;
	
	currentFileName = userConfig.toString() ;
	try {
	    inputStream = userConfig.openStream() ;
	    Reader r = new BufferedReader(new InputStreamReader(inputStream)) ;
	    streamTokenizer = new StreamTokenizer(r) ;
	}
	catch (IOException e) {
	    throw new IllegalArgumentException(
                    e + "\nUnable to open " + currentFileName) ;
	}

	//
	// Set up syntax tables for the tokenizer.
	// 
	// It would be nice to allow '/' as a word constituent for URL strings
	// and Unix paths, but then the scanner won't ignore "//" and "/* */"
	// comment style syntax.  Treating '/' as an ordinary character will
	// allow comments to work, but then '/' becomes a single token which
	// has to be concatenated with subsequent tokens to reconstruct the
	// original word string.
	//
	// It is cleaner to just require quoting for forward slashes.  '/'
	// should still be treated as an ordinary character however, so that a
	// non-quoted URL string or Unix path will be treated as a syntax
	// error instead of a comment.
	// 
	streamTokenizer.ordinaryChar('/') ;
	streamTokenizer.wordChars('_', '_') ;
	streamTokenizer.wordChars('$', '$') ; // for ${...} Java property
	streamTokenizer.wordChars('{', '}') ; // substitution in word tokens
	streamTokenizer.slashSlashComments(true) ;
	streamTokenizer.slashStarComments(true) ;

	// Create an s-expression parser to use for all top-level (0) commands.
	ConfigSexpression sexp = new ConfigSexpression() ;

	// Loop through all top-level commands.  Boolean.FALSE is returned
	// after the last one is evaluated.
	while (sexp.parseAndEval(this, streamTokenizer, 0) != Boolean.FALSE) ;

	// Close the input stream.
	try {
	    inputStream.close() ;
	}
	catch (IOException e) {
	    throw new IllegalArgumentException(
                    e + "\nUnable to close " + currentFileName) ;
	}

	// Restore current file name.
	currentFileName = lastFileName ;
    }

    /**
     * This method gets called from the s-expression parser to process a
     * configuration command.
     *
     * @param elements tokenized list of sexp elements
     * @param lineNumber command line number
     */
    void evaluateCommand(ArrayList elements, int lineNumber) {
	ConfigObject co ;
	ConfigCommand cmd ;

	// Create a command object.
	cmd = new ConfigCommand(elements, currentFileName, lineNumber) ;

	// Process the command according to its type.
	switch (cmd.type) {
	case ConfigCommand.CREATE:
	    co = createConfigObject(cmd) ;
	    addConfigObject(co) ;
	    break ;
	case ConfigCommand.ALIAS:
	    co = createConfigAlias(cmd) ;
	    addConfigObject(co) ;
	    break ;
	case ConfigCommand.PROPERTY:
	    co = findConfigObject(cmd.baseName, cmd.instanceName) ;
	    co.setProperty(cmd) ;
	    break ;
	case ConfigCommand.INCLUDE:
	    if (! (cmd.argv[1] instanceof String)) {
		throw new IllegalArgumentException
		    ("Include file must be a URL string") ;
	    }
	    URL url = null ;
	    String urlString = (String)cmd.argv[1] ;
	    try {
		url = new URL(urlString) ;
	    }
	    catch (MalformedURLException e) {
		throw new IllegalArgumentException(e.toString()) ;
	    }
	    loadConfig(url) ;
	    break ;
	case ConfigCommand.IGNORE:
	    break ;
	default:
	    throw new IllegalArgumentException
		("Unknown command \"" + cmd.commandName + "\"") ;
	}
    }

    /**
     * Instantiates and initializes an object that extends the ConfigObject
     * base class.  The class name of the object is derived from the
     * command, which is of the following form:<p>
     *
     * (New{baseName} {instanceName} ... [Alias {aliasName}])<p>
     *
     * The first two command elements and the optional trailing Alias syntax
     * are processed here, at which point the subclass implementation of
     * initialize() is called.  Subclasses must override initialize() if they
     * need to process more than what is processed by default here.
     * 
     * @param cmd configuration command that creates a new ConfigObject
     */
    private ConfigObject createConfigObject(ConfigCommand cmd) {
	Class objectClass = null ;
	ConfigObject configObject = null ;

	// Instantatiate the ConfigObject if possible.  This is not the target
	// object, but an object that will gather configuration properties,
	// instantiate the target object, and then apply the configuration
	// properties to it.
	try {
	    objectClass = Class.forName("com.sun.j3d.utils.universe.Config" +
					cmd.baseName) ;
	}
	catch (ClassNotFoundException e) {
	    throw new IllegalArgumentException
		("\"" + cmd.baseName + "\"" +
		 " is not a configurable object; ignoring command") ;
	}
	try {
	    configObject = (ConfigObject)(objectClass.newInstance()) ;
	}
	catch (IllegalAccessException e) {
	    System.out.println(e) ;
	    throw new IllegalArgumentException("Ignoring command") ;
	}
	catch (InstantiationException e) {
	    System.out.println(e) ;
	    throw new IllegalArgumentException("Ignoring command") ;
	}

	// Process an Alias keyword if present.  This option is available for
	// all New commands so it is processed here.  The Alias keyword must
	// be the penultimate command element, followed by a String.
	for (int i = 2 ; i < cmd.argc ; i++) {
	    if (cmd.argv[i] instanceof String &&
		((String)cmd.argv[i]).equals("Alias")) {
		if (i == (cmd.argc - 2) && cmd.argv[i+1] instanceof String) {
		    addConfigObject(new ConfigAlias(cmd.baseName,
						    (String)cmd.argv[i+1],
						    configObject)) ;
		    cmd.argc -= 2 ;
		}
		else {
		    throw new IllegalArgumentException
			("The alias name must be a string and " +
			 "must be the last command argument") ;
		}
	    }
	}

	// Initialize common fields.
	configObject.baseName = cmd.baseName ;
	configObject.instanceName = cmd.instanceName ;
	configObject.creatingCommand = cmd ;
	configObject.configContainer = this ;

	// Initialize specific fields and return the ConfigObject.
        configObject.setClassLoader(classLoader);
	configObject.initialize(cmd) ;
	return configObject ;
    }

    /**
     * Instantiate and initialize a ConfigObject base class containing alias
     * information.  The command is of the form:<p>
     *
     * ({baseName}Alias {aliasName} {originalName})
     * 
     * @param cmd configuration command that creates a new alias
     * @return the new ConfigObject with alias information
     */
    private ConfigObject createConfigAlias(ConfigCommand cmd) {
	ConfigObject original ;

	if (cmd.argc != 3 || ! (cmd.argv[2] instanceof String))
	    throw new IllegalArgumentException
		("Command \"" + cmd.commandName +
		 "\" requires an instance name as second argument") ;
	    
	original = findConfigObject(cmd.baseName, (String)cmd.argv[2]) ;
	return new ConfigAlias(cmd.baseName, cmd.instanceName, original) ;
    }
    
    /**
     * A class that does nothing but reference another ConfigObject.  Once
     * created, the alias name can be used in all commands that would accept
     * the original name.  A lookup of the alias name will always return the
     * original instance.
     */
    private static class ConfigAlias extends ConfigObject {
	ConfigAlias(String baseName, String instanceName, ConfigObject targ) {
	    this.baseName = baseName ;
	    this.instanceName = instanceName ;
	    this.isAlias = true ;
	    this.original = targ ;
	    targ.aliases.add(instanceName) ;
	}
    }

    /**
     * Adds the specified ConfigObject instance into this container using the
     * given ConfigCommand's base name and instance name.
     * 
     * @param object the ConfigObject instance to add into the database
     */
    private void addConfigObject(ConfigObject object) {
	ArrayList instances ;

	instances = (ArrayList)baseNameMap.get(object.baseName) ;
	if (instances == null) {
	    instances = new ArrayList() ;
	    baseNameMap.put(object.baseName, instances) ;
	}

	// Disallow duplicate instance names.
	for (int i = 0 ; i < instances.size() ; i++) {
	    ConfigObject co = (ConfigObject)instances.get(i) ;
	    if (co.instanceName.equals(object.instanceName)) {
		// Don't confuse anybody using Window.
		String base = object.baseName ;
		if (base.equals("Screen")) base = "Screen or Window" ;
		throw new IllegalArgumentException
		    ("Duplicate " + base + " instance name \"" +
		     object.instanceName + "\" ignored") ;
	    }
	}

	instances.add(object) ;
    }

    /**
     * Finds a config object matching the given base name and the instance
     * name.  If an alias is found, then its original is returned. If the
     * object is not found, an IllegalArgumentException is thrown.<p>
     *
     * @param basename base name of the config object
     * @param instanceName name associated with this config object instance
     * @return the found ConfigObject
     */
    ConfigObject findConfigObject(String baseName, String instanceName) {
	ArrayList instances ;
	ConfigObject configObject ;

	instances = (ArrayList)baseNameMap.get(baseName) ;
	if (instances != null) {
	    for (int i = 0 ; i < instances.size() ; i++) {
		configObject = (ConfigObject)instances.get(i) ;

		if (configObject.instanceName.equals(instanceName)) {
		    if (configObject.isAlias)
			return configObject.original ;
		    else
			return configObject ;
		}
	    }
	}

	// Throw an error, but don't confuse anybody using Window.
	if (baseName.equals("Screen")) baseName = "Screen or Window" ;
	throw new IllegalArgumentException
	    (baseName + " \"" + instanceName + "\" not found") ;
    }

    /**
     * Find instances of config objects with the given base name.
     * This is the same as <code>findConfigObjects(baseName, true)</code>.
     * Aliases are filtered out so that all returned instances are unique.
     *
     * @param baseName base name of desired config object class
     * @return ArrayList of config object instances of the desired base
     *  class, or null if instances of the base class don't exist
     */
    Collection findConfigObjects(String baseName) {
	return findConfigObjects(baseName, true) ;
    }


    /**
     * Find instances of config objects with the given base name.
     *
     * @param baseName base name of desired config object class
     * @param filterAlias if true, aliases are filtered out so that all
     *  returned instances are unique
     * @return ArrayList of config object instances of the desired base
     *  class, or null if instances of the base class don't exist
     */
    Collection findConfigObjects(String baseName, boolean filterAlias) {
	ArrayList instances ;

	instances = (ArrayList)baseNameMap.get(baseName) ;
	if (instances == null || instances.size() == 0) {
	    return null ; // This is not an error.
	}

	if (filterAlias) {
	    ArrayList output = new ArrayList() ;
	    for (int i = 0 ; i < instances.size() ; i++) {
		ConfigObject configObject = (ConfigObject)instances.get(i) ;

		if (! configObject.isAlias) {
		    output.add(configObject) ;
		}
	    }
	    return output ;
	}
	else {
	    return instances ;
	}
    }

    /**
     * Returns the ConfigObject associated with the name in the given
     * ConfigCommand.  This is used for evaluating retained built-in commands
     * after the config file has already been parsed.  The parser won't catch
     * any of the exceptions generated by this method, so the error messages
     * are wrapped accordingly.
     * 
     * @param basename base name of the config object
     * @param cmd command containing the name in argv[1]
     * @return the found ConfigObject
     */
    private ConfigObject findConfigObject(String baseName, ConfigCommand cmd) {
	if (cmd.argc != 2 || !(cmd.argv[1] instanceof String))
	    throw new IllegalArgumentException
		(ConfigObject.errorMessage
		 (cmd, "Parameter must be a single string")) ;
	try {
	    return findConfigObject(baseName, (String)cmd.argv[1]) ;
	}
	catch (IllegalArgumentException e) {
	    throw new IllegalArgumentException
		(ConfigObject.errorMessage(cmd, e.getMessage())) ;
	}
    }

    /**
     * This method gets called from a ConfigObject to evaluate a retained
     * built-in command nested within a property command.  These are commands
     * that can't be evaluated until the entire config file is parsed.
     * 
     * @param cmd the built-in command
     * @return object representing result of evaluation
     */
    Object evaluateBuiltIn(ConfigCommand cmd) {
	int argc = cmd.argc ;
	Object[] argv = cmd.argv ;

	if (cmd.commandName.equals("ConfigContainer")) {
	    // return a reference to this ConfigContainer
	    return this ;
	}
	else if (cmd.commandName.equals("Canvas3D")) {
	    // Look for canvases in the screen database.
	    return ((ConfigScreen)findConfigObject("Screen", cmd)).j3dCanvas ;
	}
	else if (baseNameMap.get(cmd.commandName) != null) {
	    // Handle commands of the form ({objectType} name) that return the
	    // object associated with the name.
	    return findConfigObject(cmd.commandName, cmd).targetObject ;
	}
	else {
	    // So far no other retained built-in commands.
	    throw new IllegalArgumentException
		(ConfigObject.errorMessage(cmd, "Unknown built-in command \"" +
					   cmd.commandName + "\"")) ;
	}
    }

    /**
     * Process the configuration after parsing the configuration file.
     * Note: the processing order of the various config objects is
     * significant.
     *
     * @param setVisible true if Viewer components should be visible
     * @param transformCount number of TransformGroups with which
     *  ViewingPlatforms should be created
     * @param attachBehaviors true if behaviors should be attached to
     *  ViewingPlatforms
     */
    private void processConfig(boolean setVisible,
			       int transformCount, boolean attachBehaviors) {

	Collection c, s, pe, vp ;
	this.setVisible = setVisible ;
	this.transformCount = transformCount ;

	c = findConfigObjects("PhysicalBody") ;
	if (c != null) {
	    processPhysicalBodies(c) ;
	}

	pe = findConfigObjects("PhysicalEnvironment") ;
	if (pe != null) {
	    processPhysicalEnvironments(pe) ;
	}

	c = findConfigObjects("View") ;
	if (c != null) {
	    processViews(c, setVisible) ;
	}

	c = findConfigObjects("Device") ;
	s = findConfigObjects("Sensor") ;
	if (c != null) {
	    processDevices(c, s, pe) ;
	}

	vp = findConfigObjects("ViewPlatform") ;
	if (vp != null) {
	    processViewPlatforms(vp, transformCount) ;
	}

	c = findConfigObjects("ViewPlatformBehavior") ;
	if (c != null) {
	    processViewPlatformBehaviors(c, vp, attachBehaviors) ;
	}

	c = findConfigObjects("Object") ;
	if (c != null) {
	    processGenericObjects(c) ;
	}
    }

    // Process config physical environments into Java 3D physical
    // environments.  
    private void processPhysicalEnvironments(Collection c) {
	Iterator i = c.iterator() ;
	while (i.hasNext()) {
	    ConfigPhysicalEnvironment e = (ConfigPhysicalEnvironment)i.next() ;
	    e.targetObject = e.createJ3dPhysicalEnvironment() ;
	}
    }

    // Process config physical bodys into Java 3D physical bodies.
    private void processPhysicalBodies(Collection c) {
	Iterator i = c.iterator() ;
	while (i.hasNext()) {
	    ConfigPhysicalBody b = (ConfigPhysicalBody)i.next() ;
	    b.targetObject = b.createJ3dPhysicalBody() ;
	}
    }

    // Process config views into Java 3D Views and then create Viewer objects
    // for them.  This should only be called after all physical bodies and
    // physical environments have been processed.
    private void processViews(Collection c, boolean setVisible) {
	Iterator i = c.iterator() ;
	while (i.hasNext()) {
	    ConfigView v = (ConfigView)i.next() ;
	    v.targetObject = v.createViewer(setVisible) ;
	}
    }

    // Process config devices into Java 3D input devices.  This should be done
    // only after all views have been processed, as some InputDevice
    // implementations require the AWT components associated with a view.
    private void processDevices(Collection c, Collection s, Collection p) {
	ConfigDevice cd = null ;
	Iterator i = c.iterator() ;
	while (i.hasNext()) {
	    cd = (ConfigDevice)i.next() ;
	    cd.targetObject = cd.createInputDevice() ;
	}

	// Process device properties only after all InputDevices have been
	// instantiated.  Some InputDevice properties require references
	// to other InputDevice implementations.
	i = c.iterator() ;
	while (i.hasNext()) ((ConfigDevice)i.next()).processProperties() ;

	// Initialize the devices only after all have been instantiated, as
	// some InputDevices implementations are slaved to the first one
	// created and will not initialize otherwise (e.g. LogitechTracker).
	i = c.iterator() ;
	while (i.hasNext()) {
	    cd = (ConfigDevice)i.next() ;
	    if (! cd.j3dInputDevice.initialize())
		throw new RuntimeException
		    (cd.errorMessage(cd.creatingCommand,
				     "could not initialize device \"" + 
				     cd.instanceName + "\"")) ;
	}
	
	// An InputDevice implementation will have created all its Sensors by
	// the time initialize() returns.  Retrieve and configure them here.
	if (s != null) {
	    i = s.iterator() ;
	    while (i.hasNext()) {
		ConfigSensor cs = (ConfigSensor)i.next() ;
		cs.configureSensor() ;
		cs.targetObject = cs.j3dSensor ;
	    }
	}

	// Iterate through the PhysicalEnvironments and process the devices.
	if (p != null) {
	    i = p.iterator() ;
	    while (i.hasNext())
		((ConfigPhysicalEnvironment)i.next()).processDevices() ;
	}
    }

    // Process config view platforms into Java 3D viewing platforms.
    private void processViewPlatforms(Collection c, int numTransforms) {
	Iterator i = c.iterator() ;
	while (i.hasNext()) {
	    ConfigViewPlatform cvp = (ConfigViewPlatform)i.next() ;
	    cvp.targetObject = cvp.createViewingPlatform(numTransforms) ;
	}
    }

    // Process the configured view platform behaviors.
    private void processViewPlatformBehaviors(Collection behaviors,
					      Collection viewPlatforms,
					      boolean attach) {
	Iterator i = behaviors.iterator() ;
	while (i.hasNext()) {
	    ConfigViewPlatformBehavior b =
		(ConfigViewPlatformBehavior)i.next() ;
	    b.targetObject = b.createViewPlatformBehavior() ;
	}

	// Process properties only after all behaviors are instantiated.
	i = behaviors.iterator() ;
	while (i.hasNext())
	    ((ConfigViewPlatformBehavior)i.next()).processProperties() ;

	// Attach behaviors to platforms after properties processed.
	if (attach && viewPlatforms != null) {
	    i = viewPlatforms.iterator() ;
	    while (i.hasNext())
		((ConfigViewPlatform)i.next()).processBehavior() ;
	}
    }

    // Process generic objects. 
    private void processGenericObjects(Collection objects) {
	Iterator i = objects.iterator() ;
	while (i.hasNext()) {
	    ConfigObject o = (ConfigObject)i.next() ;
	    o.targetObject = o.createTargetObject() ;
	}

	// Process properties only after all target objects are instantiated.
	i = objects.iterator() ;
	while (i.hasNext()) ((ConfigObject)i.next()).processProperties() ;
    }

    // Returns a read-only Set containing all unique Java 3D objects of the
    // specified base class.
    private ReadOnlySet createSet(String baseName) {
	Collection c = findConfigObjects(baseName, true) ;
	if (c == null || c.size() == 0)
	    return null ;

	Iterator i = c.iterator() ;
	ArrayList l = new ArrayList() ;
	while (i.hasNext()) l.add(((ConfigObject)i.next()).targetObject) ;

	return new ReadOnlySet(l) ;
    }

    // Returns a read-only Map that maps all names in the specified base
    // class, including aliases, to their corresponding Java 3D objects.
    private ReadOnlyMap createMap(String baseName) {
	Collection c = findConfigObjects(baseName, false) ;
	if (c == null || c.size() == 0)
	    return null ;

	Iterator i = c.iterator() ;
	HashMap m = new HashMap() ;
	while (i.hasNext()) {
	    ConfigObject co = (ConfigObject)i.next() ;
	    if (co.isAlias)
		m.put(co.instanceName, co.original.targetObject) ;
	    else
		m.put(co.instanceName, co.targetObject) ;
	}

	return new ReadOnlyMap(m) ;
    }

    /**
     * Returns a read-only Set of all configured PhysicalBody instances in the
     * order they were defined in the configuration file.
     *
     * PhysicalBody instances are created with the following command:<p>
     * <blockquote>
     * (NewPhysicalBody <i>&lt;instance name&gt;</i>
     * [Alias <i>&lt;alias name&gt;</i>])
     * </blockquote>
     * 
     * The PhysicalBody is configured through the following command:<p>
     * <blockquote>
     * (PhysicalBodyProperty <i>&lt;instance name&gt;
     * &lt;property name&gt; &lt;property value&gt;</i>)
     * </blockquote>
     * 
     * @return read-only Set of all unique instances, or null
     */
    public Set getPhysicalBodies() {
	if (bodies != null) return bodies ;
	bodies = createSet("PhysicalBody") ;
	return bodies ;
    }

    /**
     * Returns a read-only Map that maps PhysicalBody names to instances.
     * Names may be aliases and if so will map to the original instances.
     * 
     * @return read-only Map from names to PhysicalBody instances, or null if
     *  no instances
     */
    public Map getNamedPhysicalBodies() {
	if (bodyMap != null) return bodyMap ;
	bodyMap = createMap("PhysicalBody") ;
	return bodyMap ;
    }

    /**
     * Returns a read-only Set of all configured PhysicalEnvironment instances
     * in the order they were defined in the configuration file.<p>
     * 
     * PhysicalEnvironment instances are created with the following command:<p>
     * <blockquote>
     * (NewPhysicalEnvironment <i>&lt;instance name&gt;</i>
     * [Alias <i>&lt;alias name&gt;</i>])
     * </blockquote>
     * 
     * The PhysicalEnvironment is configured through the following command:<p>
     * <blockquote>
     * (PhysicalEnvironmentProperty <i>&lt;instance name&gt;
     * &lt;property name&gt; &lt;property value&gt;</i>)
     * </blockquote>
     * 
     * @return read-only Set of all unique instances, or null
     */
    public Set getPhysicalEnvironments() {
	if (environments != null) return environments ;
	environments = createSet("PhysicalEnvironment") ;
	return environments ;
    }

    /**
     * Returns a read-only Map that maps PhysicalEnvironment names to
     * instances.  Names may be aliases and if so will map to the original
     * instances.
     * 
     * @return read-only Map from names to PhysicalEnvironment instances, or
     *  null if no instances
     */
    public Map getNamedPhysicalEnvironments() {
	if (environmentMap != null) return environmentMap ;
	environmentMap = createMap("PhysicalEnvironment") ;
	return environmentMap ;
    }

    /**
     * Returns a read-only Set of all configured Viewer instances in the order
     * they were defined in the configuration file.  The Viewers will have
     * incorporated any PhysicalEnvironment and PhysicalBody objects specfied
     * for them in the configuration file, and will be attached to any
     * ViewingPlatforms specified for them.<p>
     * 
     * Viewer instances are created with the following command:<p>
     * <blockquote>
     * (NewView <i>&lt;instance name&gt;</i> [Alias <i>&lt;alias name&gt;</i>])
     * </blockquote>
     * 
     * The Viewer is configured through the following command:<p>
     * <blockquote>
     * (ViewProperty <i>&lt;instance name&gt;
     * &lt;property name&gt; &lt;property value&gt;</i>)
     * </blockquote>
     *
     * @return read-only Set of all unique instances, or null
     */
    public Set getViewers() {
	if (viewers != null) return viewers ;
	viewers = createSet("View") ;
	return viewers ;
    }

    /**
     * Returns a read-only Map that maps Viewer names to instances.
     * Names may be aliases and if so will map to the original instances.
     * The Viewers will have incorporated any PhysicalEnvironment and
     * PhysicalBody objects specfied for them in the configuration file, and
     * will be attached to any ViewingPlatforms specified for them.<p>
     * 
     * @return read-only Map from names to Viewer instances, or
     *  null if no instances
     */
    public Map getNamedViewers() {
	if (viewerMap != null) return viewerMap ;
	viewerMap = createMap("View") ;
	return viewerMap ;
    }

    /**
     * Returns a read-only Set of all configured InputDevice instances in the
     * order they were defined in the configuration file.  All InputDevice
     * instances in the set are initialized and registered with any
     * PhysicalEnvironments that reference them.<p>
     *
     * InputDevice instances are created with the following command:<p>
     * <blockquote>
     * (NewDevice <i>&lt;instanceName&gt; &lt;className&gt;</i>
     * [Alias <i>&lt;alias name&gt;</i>])
     * </blockquote>
     * 
     * <i>className</i> must be the fully-qualified name of a class that
     * implements the InputDevice interface.  The implementation
     * must provide a parameterless constructor.<p>
     *
     * The InputDevice is configured through the DeviceProperty command:<p>
     * <blockquote>
     * (DeviceProperty <i>&lt;instanceName&gt; &lt;propertyName&gt;
     *  &lt;arg0&gt; ... &lt;argn&gt;</i>)
     * </blockquote>
     * 
     * <i>propertyName</i> must be the name of a input device method that
     * takes an array of Objects as its only parameter; the array is populated
     * with the values of <i>arg0</i> through <i>argn</i> when the method is
     * invoked to set the property.  These additional requirements for
     * configurable input devices can usually be fulfilled by extending or
     * wrapping available InputDevice implementations.
     * 
     * @return read-only Set of all unique instances, or null
     */
    public Set getInputDevices() {
	if (devices != null) return devices ;
	devices = createSet("Device") ;
	return devices ;
    }

    /**
     * Returns a read-only Map that maps InputDevice names to instances.
     * Names may be aliases and if so will map to the original instances.  All
     * InputDevice instances in the map are initialized and registered with
     * any PhysicalEnvironments that reference them.
     * 
     * @return read-only Map from names to InputDevice instances, or
     *  null if no instances
     * @see #getInputDevices
     */
    public Map getNamedInputDevices() {
	if (deviceMap != null) return deviceMap ;
	deviceMap = createMap("Device") ;
	return deviceMap ;
    }

    /**
     * Returns a read-only Set of all configured Sensor instances in the order
     * they were defined in the configuration file.  The associated
     * InputDevices are all initialized and registered with any
     * PhysicalEnvironments that reference them.<p>
     * 
     * Sensor instances are named with the following command:<p>
     * <blockquote>
     * (NewSensor <i>&lt;instance name&gt; &lt;device name&gt; 
     * &lt;sensor index&gt;</i> [Alias <i>&lt;alias name&gt;</i>])
     * </blockquote>
     * 
     * <i>device name</i> is the instance name of a previously defined
     * InputDevice, and <i>sensor index</i> is the index of the Sensor to be
     * bound to <i>instance name</i>.  The InputDevice implementation is
     * responsible for creating its own Sensor objects, so this command does
     * not create any new instances.<p>
     *
     * The Sensor is configured through the SensorProperty command:<p>
     * <blockquote>
     * (SensorProperty <i>&lt;instance name&gt; &lt;property name&gt; 
     * &lt;property value&gt;</i>)
     * </blockquote>
     * 
     * With the sole exception of the Sensor assigned to the head tracker,
     * none of the Sensors defined in the configuration file are placed into
     * the Sensor array maintained by a PhysicalEnvironment.  
     *
     * @return read-only Set of all unique instances, or null
     */
    public Set getSensors() {
	if (sensors != null) return sensors ;
	sensors = createSet("Sensor") ;
	return sensors ;
    }

    /**
     * Returns a read-only Map that maps Sensor names to instances.  Names may
     * be aliases and if so will map to the original instances.  The
     * associated InputDevices are all initialized and registered with any
     * PhysicalEnvironments that reference them.<p>
     * 
     * With the sole exception of the Sensor assigned to the head tracker,
     * none of the Sensors defined in the configuration file are placed into
     * the Sensor array maintained by a PhysicalEnvironment.  
     * 
     * @return read-only Map from names to Sensor instances, or
     *  null if no instances
     */
    public Map getNamedSensors() {
	if (sensorMap != null) return sensorMap ;
	sensorMap = createMap("Sensor") ;
	return sensorMap ;
    }

    /**
     * Returns a read-only Set of all configured ViewingPlatform instances in
     * the order they were defined in the configuration file.  The
     * ConfigContainer class itself does not attach the ViewingPlatform
     * instances to any scengraph components or universe Locales; they are not
     * "live" until made so by a separate client such as ConfiguredUniverse.
     *
     * ViewingPlatform instances are created with the following command:<p>
     * <blockquote>
     * (NewViewPlatform <i>&lt;instance name&gt;</i>
     * [Alias <i>&lt;alias name&gt;</i>])
     * </blockquote>
     * 
     * The ViewingPlatform is configured through the following command:<p>
     * <blockquote>
     * (ViewPlatformProperty <i>&lt;instance name&gt; &lt;property name&gt;
     * &lt;property value&gt;</i>)
     * </blockquote>
     *
     * @return read-only Set of all unique instances, or null
     */
    public Set getViewingPlatforms() {
	if (platforms != null) return platforms ;
	platforms = createSet("ViewPlatform") ;
	return platforms ;
    }

    /**
     * Returns a read-only Map that maps ViewingPlatform names to instances.
     * Names may be aliases and if so will map to the original instances.  The
     * ConfigContainer class itself does not attach the ViewingPlatform
     * instances to any scengraph components or universe Locales; they are not
     * "live" until made so by a separate client such as ConfiguredUniverse.
     * 
     * @return read-only Map from names to ViewingPlatform instances, or
     *  null if no instances
     */
    public Map getNamedViewingPlatforms() {
	if (platformMap != null) return platformMap ;
	platformMap = createMap("ViewPlatform") ;
	return platformMap ;
    }

    /**
     * Returns a read-only Set of all configured ViewPlatformBehavior
     * instances in the order they were defined in the configuration file.<p>
     * 
     * The behaviors are attached to any ViewingPlatforms that specified them;
     * that is, the <code>setViewPlatformBehavior</code> and
     * <code>setViewingPlatform</code> methods of ViewingPlatform and
     * ViewPlatformBehavior have been called if appropriate.  However, a
     * behavior's <code>initialize</code> method is not called until the
     * ViewingPlatform to which it is attached is made live.<p>
     *
     * ViewPlatformBehavior instances are created by the following command:<p>
     * <blockquote>
     * (NewViewPlatformBehavior <i>&lt;instanceName&gt; &lt;className&gt;</i>)
     * </blockquote>
     * 
     * <i>className</i> must be the fully qualified name of a concrete class
     * that extends the abstract ViewPlatformBehavior class.  The
     * implementation must provide a parameterless constructor.<p>
     *
     * The behavior is configured using ViewPlatformBehaviorProperty:<p>
     * <blockquote>
     * (ViewPlatformBehaviorProperty <i>&lt;instanceName&gt;
     *  &lt;propertyName&gt; &lt;arg0&gt; ... &lt;argn&gt;</i>)
     * </blockquote>
     * 
     * ViewPlatformBehavior subclasses inherit a number of pre-defined
     * properties that can be directly specified with the <i>propertyName</i>
     * string; see the configuration file documentation for details.<p>
     *
     * Concrete ViewPlatformBehavior instances can also define their own
     * unique properties.  In those cases, <i>propertyName</i> must be the
     * name of a behavior method that takes an array of Objects as its only
     * parameter; the array is populated with the values of <i>arg0</i>
     * through <i>argn</i> when the method is invoked to set the property.
     * These additional requirements for configurable behaviors can usually be
     * fulfilled by extending or wrapping available ViewPlatformBehavior
     * subclasses.
     *
     * @return read-only Set of all unique instances, or null
     */
    public Set getViewPlatformBehaviors() {
	if (behaviors != null) return behaviors ;
	behaviors = createSet("ViewPlatformBehavior") ;
	return behaviors ;
    }

    /**
     * Returns a read-only Map that maps ViewPlatformBehavior names to
     * instances.  Names may be aliases and if so will map to the original
     * instances.<p>
     * 
     * The behaviors are attached to any ViewingPlatforms that specified them;
     * that is, the <code>setViewPlatformBehavior</code> and
     * <code>setViewingPlatform</code> methods of ViewingPlatform and
     * ViewPlatformBehavior have been called if appropriate.  However, a
     * behavior's <code>initialize</code> method is not called until the
     * ViewingPlatform to which it is attached is made live.<p>
     * 
     * @return read-only Map from names to ViewPlatformBehavior instances, or
     *  null if no instances
     * @see #getViewPlatformBehaviors
     */
    public Map getNamedViewPlatformBehaviors() {
	if (behaviorMap != null) return behaviorMap ;
	behaviorMap = createMap("ViewPlatformBehavior") ;
	return behaviorMap ;
    }

    /**
     * Returns a read-only Map containing the named Canvas3D instances used by
     * the specified Viewer.  Names may be aliases and if so will map to the
     * original instances.  The set of unique Canvas3D instances used by a
     * Viewer may be obtained by calling the Viewer's accessor methods
     * directly.<p>
     * 
     * A named Canvas3D is created and added to a Viewer whenever any of the
     * following configuration commands are used:<p>
     * <blockquote>
     * (ViewProperty <i>&lt;view&gt;</i> Screen <i>&lt;screenName&gt;</i>)<br>
     * (ViewProperty <i>&lt;view&gt;</i> Window <i>&lt;windowName&gt;</i>)
     * </blockquote>
     * 
     * <i>view</i> is the name of a Viewer created with the NewView command.
     * The <i>screenName</i> and <i>windowName</i> parameters of the above
     * commands are the keys to use when looking up the associated Canvas3D
     * instances in the Map returned by this method.  <b>Note:</b> the
     * NewScreen and NewWindow commands do <i>not</i> create Canvas3D
     * instances themselves; they are created only by the above configuration
     * commands.
     *
     * @param viewName the name of the Viewer
     * @return read-only Map containing the Viewer's named Canvas3D instances
     */
    public Map getNamedCanvases(String viewName) {
	Map m = (Map)viewCanvasMap.get(viewName) ;
	if (m != null) return m ;

	m = new HashMap() ;
	ConfigView cv = (ConfigView)findConfigObject("View", viewName) ;
	Iterator i = cv.screens.iterator() ;
	while (i.hasNext()) {
	    ConfigScreen cs = (ConfigScreen)i.next() ;
	    m.put(cs.instanceName, cs.j3dCanvas) ;

	    // The aliases list contains all alias strings for the canvas. 
	    Iterator j = cs.aliases.iterator() ;
	    while (j.hasNext()) m.put(j.next(), cs.j3dCanvas) ;
	}
	m = new ReadOnlyMap(m) ;
	viewCanvasMap.put(viewName, m) ;
	return m ;
    }

    /**
     * Returns a read-only Set of all generic configuration object
     * instances in the order they were defined in the configuration file.<p>
     *
     * Generic object instances are created with the following command:<p>
     * <blockquote>
     * (NewObject <i>&lt;instanceName&gt; &lt;className&gt;</i>)
     * </blockquote>
     * 
     * <i>className</i> must be the fully-qualified name of a class that
     * provides a parameterless constructor.<p>
     *
     * The object is configured through the ObjectProperty command:<p>
     * <blockquote>
     * (ObjectProperty <i>&lt;instanceName&gt; &lt;propertyName&gt;
     *  &lt;arg0&gt; ... &lt;argn&gt;</i>)
     * </blockquote>
     * 
     * <i>propertyName</i> must be the name of a method provided by object
     * <i>instanceName</i>.  It must take an array of Objects as its only
     * parameter; the array is populated with the values of <i>arg0</i>
     * through <i>argn</i> when the method is invoked to set the property.
     * These additional requirements for configurable objects can usually be
     * fulfilled by extending or wrapping available object classes.
     *
     * @return read-only Set of all unique instances, or null
     */
    public Set getGenericObjects() {
	if (genericObjects != null) return genericObjects ;
	genericObjects = createSet("Object") ;
	return genericObjects ;
    }

    /**
     * Returns a read-only Map that maps generic object names to
     * instances.  Names may be aliases and if so will map to the original
     * instances.
     * 
     * @return read-only Map from names to generic object instances, or
     *  null if no instances
     * @see #getGenericObjects
     */
    public Map getNamedGenericObjects() {
	if (genericObjectMap != null) return genericObjectMap ;
	genericObjectMap = createMap("Object") ;
	return genericObjectMap ;
    }

    /**
     * Returns the number of TransformGroups with which ViewingPlatforms
     * should be created.  This is useful for clients that wish to provide a
     * default ViewingPlatform if the configuration file doesn't specify one.
     *
     * @return the number of TransformGroups
     */
    public int getViewPlatformTransformCount() {
	return transformCount ;
    }

    /**
     * Returns whether Viewers should be created with their AWT components
     * initially visible or invisible.  This is useful for clients that wish
     * to provide a default Viewer if the configuration file doesn't specify
     * one. 
     *
     * @return true if Viewer components should be initially visible; false
     *  otherwise 
     */ 
    public boolean getViewerVisibility() {
	return setVisible ;
    }

    /**
     * Release memory references used by this ConfigContainer.  All Sets and
     * Maps obtained from this ConfigContainer are cleared.
     */
    public void clear() {
	// Clear baseNameList.
	Iterator i = baseNameMap.values().iterator() ;
	while (i.hasNext()) ((Collection)i.next()).clear() ;
	baseNameMap.clear() ;

	// Clear viewCanvasMap.
	i = viewCanvasMap.values().iterator() ;
	while (i.hasNext()) ((ReadOnlyMap)i.next()).map.clear() ;
	viewCanvasMap.clear() ;

	// Release reference to file name.
	currentFileName = null ;

	// Clear and release sets.
	if (bodies != null) {
	    bodies.collection.clear() ;
	    bodies = null ;
	}
	if (environments != null) {
	    environments.collection.clear() ;
	    environments = null ;
	}
	if (devices != null) {
	    devices.collection.clear() ;
	    devices = null ;
	}
	if (sensors != null) {
	    sensors.collection.clear() ;
	    sensors = null ;
	}
	if (behaviors != null) {
	    behaviors.collection.clear() ;
	    behaviors = null ;
	}
	if (platforms != null) {
	    platforms.collection.clear() ;
	    platforms = null ;
	}
	if (viewers != null) {
	    viewers.collection.clear() ;
	    viewers = null ;
	}
	if (genericObjects != null) {
	    genericObjects.collection.clear() ;
	    genericObjects = null ;
	}

	// Clear and release maps.
	if (bodyMap != null) {
	    bodyMap.map.clear() ;
	    bodyMap = null ;
	}
	if (environmentMap != null) {
	    environmentMap.map.clear() ;
	    environmentMap = null ;
	}
	if (deviceMap != null) {
	    deviceMap.map.clear() ;
	    deviceMap = null ;
	}
	if (sensorMap != null) {
	    sensorMap.map.clear() ;
	    sensorMap = null ;
	}
	if (behaviorMap != null) {
	    behaviorMap.map.clear() ;
	    behaviorMap = null ;
	}
	if (platformMap != null) {
	    platformMap.map.clear() ;
	    platformMap = null ;
	}
	if (viewerMap != null) {
	    viewerMap.map.clear() ;
	    viewerMap = null ;
	}
	if (genericObjectMap != null) {
	    genericObjectMap.map.clear() ;
	    genericObjectMap = null ;
	}
	
    }

    /**
     * Returns the config file URL based on system properties.  The current
     * implementation of this method parses the j3d.configURL property as a
     * URL string.  For example, the following command line would specify that
     * the config file is taken from the file "j3dconfig" in the current
     * directory:
     * <ul>
     * <code>java -Dj3d.configURL=file:j3dconfig ...</code>
     * </ul>
     *
     * @return the URL of the config file; null is returned if no valid
     *  URL is defined by the system properties
     */
    public static URL getConfigURL() {
	return getConfigURL(null) ;
    }

    /**
     * Returns the config file URL based on system properties.  The current
     * implementation of this method parses the j3d.configURL property as a
     * URL string.  For example, the following command line would specify that
     * the config file is taken from the file "j3dconfig" in the current
     * directory:
     * <ul>
     * <code>java -Dj3d.configURL=file:j3dconfig ...</code>
     * </ul>
     *
     * @param defaultURLString the default string used to construct
     *  the URL if the appropriate system properties are not defined
     * @return the URL of the config file; null is returned if no
     *  valid URL is defined either by the system properties or the
     *  default URL string
     */
    public static URL getConfigURL(String defaultURLString) {
	URL url = null ;
	String urlString = null ;
	final String defProp = defaultURLString ;

        urlString = (String)java.security.AccessController.doPrivileged
	    (new java.security.PrivilegedAction() {
		public Object run() {
		    return System.getProperty("j3d.configURL", defProp) ;
		}
	    }) ;

	if (urlString == null) {
	    return null ;
	}
	try {
	    url = new URL(urlString) ;
	}
	catch(MalformedURLException e) {
	    System.out.println(e) ;
	    return null ;
	}
	return url ;
    }

    // A general purpose read-only Map backed by a HashMap.
    private static class ReadOnlyMap extends AbstractMap {
	HashMap map ;
	private Set entrySet = null ;

	ReadOnlyMap(Map map) {
	    this.map = new HashMap(map) ;
	}

	// overridden for efficiency
	public Object get(Object key) {
	    return map.get(key) ;
	}

	// overridden for efficiency
	public boolean containsKey(Object key) {
	    return map.containsKey(key) ;
	}

	// overridden for efficiency
	public boolean containsValue(Object value) {
	    return map.containsValue(value) ;
	}

	public Set entrySet() {
	    if (entrySet == null)
		entrySet = new ReadOnlySet(map.entrySet()) ;

	    return entrySet ;
	}
    }

    // A general purpose read-only Set backed by a Collection containing
    // unique objects.
    private static class ReadOnlySet extends AbstractSet {
	Collection collection = null ;

	ReadOnlySet(Collection c) {
	    this.collection = c ;
	}

	public int size() {
	    return collection.size() ;
	}

	public Iterator iterator() {
	    return new ReadOnlyIterator(collection.iterator()) ;
	}
    }

    // A general purpose read-only Iterator backed by another Iterator.
    private static class ReadOnlyIterator implements Iterator {
	private Iterator i ;

	ReadOnlyIterator(Iterator i) {
	    this.i = i ;
	}

	public boolean hasNext() {
	    return i.hasNext() ;
	}

	public Object next() {
	    return i.next() ;
	}

	public void remove() {
	    throw new UnsupportedOperationException() ;
	}
    }
}
