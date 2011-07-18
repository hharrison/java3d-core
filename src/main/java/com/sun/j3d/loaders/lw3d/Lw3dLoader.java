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


import com.sun.j3d.loaders.*;
import java.awt.Component;
import java.io.*;
import java.util.Vector;
import java.util.Enumeration;
import java.net.URL;
import java.net.MalformedURLException;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;


/**
 * This class implements the Loader API and allows users to load
 * Lightwave 3D scene files.  In order to load properly, the object
 * files referred to in the scene files and the image files referred
 * to by the object files must all be specified with path and filenames
 * that are valid with respect to the directory in which the application
 * is being executed.
 */

public class Lw3dLoader extends TextfileParser implements Loader {

    Vector           objectList;
    Vector           lightList;
    BranchGroup      sceneGroupNode;
    Color3f          ambientColor;
    LwsCamera        camera = null;
    LwsFog           fog = null;
    LwsBackground    background = null;
    int              loadFlags = 0;
    int              loadBehaviors = 0;
    Vector           sceneBehaviors;
    SceneBase        scene = null;
    String           basePath = null;
    String           internalBasePath = null;
    URL              baseUrl = null;
    String           internalBaseUrl = null;  // store url base as String
    static final int FILE_TYPE_NONE = 0;
    static final int FILE_TYPE_URL = 1;
    static final int FILE_TYPE_FILENAME = 2;
    static final int FILE_TYPE_READER = 4;
    int              fileType = FILE_TYPE_NONE;
    
    /**
     * Default constructor.  Sets up default values for some variables.
     */
    public Lw3dLoader() {

	ambientColor = new Color3f(0f, 0f, 0f);
	objectList = new Vector();
	lightList = new Vector();
	debugPrinter.setValidOutput(0x0);

    }

    /**
     * This constructor takes a flags word that specifies which types of
     * scenefile items should be loaded into the scene.  The possible
     * values are specified in the com.sun.j3d.loaders.Loader class.
     */
    public Lw3dLoader(int flags) {

	this();
	loadFlags = flags;
        loadBehaviors = (loadFlags & Loader.LOAD_BEHAVIOR_NODES);

    }
    
    /**
     * This method loads the named file and returns the Scene
     * containing the scene.  Any data files referenced by the Reader
     * should be located in the same place as the named file; otherwise,
     * users should specify an alternate base path with the setBaseUrl(URL)
     * method.
     */
    public Scene load(URL url) throws FileNotFoundException, 
                           IncorrectFormatException, ParsingErrorException {

	fileType = FILE_TYPE_URL;
	setInternalBaseUrl(url);
	InputStreamReader reader;
	try {
	    reader = new InputStreamReader(
		new BufferedInputStream(url.openStream()));
	}
	catch (IOException e) {
	  throw new FileNotFoundException(e.getMessage());
	}
	Scene returnScene = load(reader);
	fileType = FILE_TYPE_NONE;
	return returnScene;
    }
    
    /**
     * This method loads the named file and returns the Scene
     * containing the scene.  Any data files referenced by this
     * file should be located in the same place as the named file;
     * otherwise users should specify an alternate base path with
     * the setBasePath(String) method.
     */
    public Scene load(String fileName) throws FileNotFoundException, 
                          IncorrectFormatException, ParsingErrorException {

	fileType = FILE_TYPE_FILENAME;
	setInternalBasePath(fileName);
	Reader reader = new BufferedReader(new FileReader(fileName));
	Scene returnScene = load(reader);
	fileType = FILE_TYPE_NONE;
	return returnScene;
    }
	
    /**
     * This method loads the Reader and returns the Scene
     * containing the scene.  Any data files referenced by the Reader should
     * be located in the user's current working directory.
     */
    public Scene load(Reader reader) throws FileNotFoundException, 
                            IncorrectFormatException, ParsingErrorException {

	if (fileType == FILE_TYPE_NONE)
	    fileType = FILE_TYPE_READER;
	StreamTokenizer tokenizer = new StreamTokenizer(reader);
	setupTokenizer(tokenizer);
	
	getAndCheckString(tokenizer, "LWSC");
	getNumber(tokenizer);
	getAndCheckString(tokenizer, "FirstFrame");
	int firstFrame = (int)getNumber(tokenizer);
	getAndCheckString(tokenizer, "LastFrame");
	int finalFrame = (int)getNumber(tokenizer);
	skipUntilString(tokenizer, "FramesPerSecond");
	double fps = getNumber(tokenizer);
	float totalTime = (float)(finalFrame - firstFrame)/(float)fps;
	boolean done = false;
	while (!done) {
	    int token;
	    try {
		token = tokenizer.nextToken();
	    }
	    catch (IOException e) {
		throw new ParsingErrorException(e.getMessage());
	    }
	    switch (tokenizer.ttype) {
	    case StreamTokenizer.TT_EOF:
		done = true;
		break;
	    case StreamTokenizer.TT_WORD:
		debugOutputLn(VALUES, "  String = " + tokenizer.sval);
		if (tokenizer.sval.equals("AddNullObject")) {
		    LwsObject obj =
			new LwsObject(tokenizer, false,
				      firstFrame,
				      finalFrame, totalTime,
				      this,
				      debugPrinter.getValidOutput());
		    obj.createJava3dObject(null, loadBehaviors);
		    objectList.addElement(obj);
		}
		else if (tokenizer.sval.equals("LoadObject")) {
		    String filename = getString(tokenizer);
		    tokenizer.pushBack();  // push filename token back
		    debugOutputLn(TIME, "loading " + filename + " at " +
			    System.currentTimeMillis());
		    LwsObject obj = new LwsObject(tokenizer, true,
						  firstFrame, 
						  finalFrame, totalTime,
						  this,
						  debugPrinter.getValidOutput());
		    debugOutputLn(TIME, "done loading at " +
			    System.currentTimeMillis());
		    LwsObject cloneObject = null;
		    for (Enumeration e = objectList.elements() ;
			 e.hasMoreElements() ;) {
			LwsObject tmpObj = (LwsObject)e.nextElement();
			if (tmpObj.fileName != null &&
			    tmpObj.fileName.equals(filename)) {
			    cloneObject = tmpObj;
			    break;
			}
		    }
		    obj.createJava3dObject(cloneObject, loadBehaviors);
		    objectList.addElement(obj);
		}
		else if (tokenizer.sval.equals("AmbientColor")) {
		    ambientColor.x = (float)getNumber(tokenizer)/255f;
		    ambientColor.y = (float)getNumber(tokenizer)/255f;
		    ambientColor.z = (float)getNumber(tokenizer)/255f;
		}
		else if (tokenizer.sval.equals("AmbIntensity")) {
		    // TODO: must be able to handle envelopes here
		    float intensity = (float)getNumber(tokenizer);
		    ambientColor.x *= intensity;
		    ambientColor.y *= intensity;
		    ambientColor.z *= intensity;
		}
		else if (tokenizer.sval.equals("AddLight")) {
		    LwsLight light =
			new LwsLight(tokenizer,
				     finalFrame, totalTime,
				     debugPrinter.getValidOutput());
		    light.createJava3dObject(loadBehaviors);
		    lightList.addElement(light);
		}
		else if (tokenizer.sval.equals("ShowCamera")) {
		    camera = new LwsCamera(tokenizer, firstFrame,
					   finalFrame, totalTime,
					   debugPrinter.getValidOutput());
		    camera.createJava3dObject(loadBehaviors);
		}
		else if (tokenizer.sval.equals("FogType")) {
		    int fogType = (int)getNumber(tokenizer);
		    if (fogType != 0) {
			fog = new LwsFog(tokenizer,
					 debugPrinter.getValidOutput());
			fog.createJava3dObject();
		    }
		}
		else if (tokenizer.sval.equals("SolidBackdrop")) {
		    background =
			new LwsBackground(tokenizer,
					  debugPrinter.getValidOutput());
		    background.createJava3dObject();
		}
		break;
	    default:
		debugOutputLn(VALUES, "  Unknown ttype, token = " +
				   tokenizer.ttype + ", " + token);
		break;
	    }
	}
	
	// Set up scene groups and parent objects appropriately
	sceneGroupNode = new BranchGroup();
        sceneBehaviors = new Vector();
	parentObjects();
	constructScene();
	
	return scene;

    }


    /**
     * This method creates the Scene (actually SceneBase) data structure
     * and adds all appropriate items to it.  This is the data structure
     * that the user will get back from the load() call and inquire to
     * get data from the scene.
     */
    void constructScene() {

	// Construct Scene data structure
	scene = new SceneBase();

	if ((loadFlags & Loader.LOAD_LIGHT_NODES) != 0) {
	    addLights();
	    addAmbient();
	}

	if ((loadFlags & Loader.LOAD_FOG_NODES) != 0)
	    addFog();

	if ((loadFlags & Loader.LOAD_BACKGROUND_NODES) != 0)
	    addBackground();

	if ((loadFlags & Loader.LOAD_VIEW_GROUPS) != 0)
	    addCamera();

        if (loadBehaviors != 0)
            addBehaviors();

	scene.setSceneGroup(sceneGroupNode);
	
	// now add named objects to the scenes name table
	for (Enumeration e = objectList.elements(); e.hasMoreElements() ;) {
	    
	    LwsObject obj = (LwsObject)e.nextElement();
	    if (obj.fileName != null)
		scene.addNamedObject(obj.fileName,(Object)obj.getObjectNode());
	    else if (obj.objName != null)
		scene.addNamedObject(obj.objName,(Object)obj.getObjectNode());
				    
	}
    }	    


    /**
     * Creates a url for internal use.  This method is not currently
     * used (url's are ignored for this loader; only filenames work)
     */
    void setInternalBaseUrl(URL url) {
// 	System.out.println("setInternalBaseUrl url = " + url);
	java.util.StringTokenizer stok =
	    new java.util.StringTokenizer(url.toString(),
// 					  java.io.File.separator);
					  "\\/");
	int tocount = stok.countTokens()-1;
	StringBuffer sb = new StringBuffer(80);
	for(int ji = 0; ji < tocount ; ji++) {
	    String a = stok.nextToken();
	    if((ji == 0) && //(!a.equals("file:"))) {
	       (!a.regionMatches(true, 0, "file:", 0, 5))) {
		sb.append(a);
		// urls use / on windows also
// 		sb.append(java.io.File.separator);
// 		sb.append(java.io.File.separator);
		sb.append('/');
		sb.append('/');
	    } else {
		sb.append(a);
		// urls use / on windows also
// 		sb.append( java.io.File.separator );
		sb.append('/');
	    }
	}
	internalBaseUrl = sb.toString();
// 	System.out.println("internalBaseUrl = " + internalBaseUrl);
    }

    /**
     * Standardizes the filename for use in the loader
     */
    void setInternalBasePath(String fileName) {
	java.util.StringTokenizer stok =
	    new java.util.StringTokenizer(fileName,
					  java.io.File.separator);
	int tocount = stok.countTokens()-1;
	StringBuffer sb = new StringBuffer(80);
	if (fileName!= null &&
	    fileName.startsWith(java.io.File.separator))
	    sb.append(java.io.File.separator);
	for(int ji = 0; ji < tocount ; ji++) {
	    String a = stok.nextToken();
	    sb.append(a);
	    sb.append( java.io.File.separator );
	}
	internalBasePath = sb.toString();
    }

    String getInternalBasePath() {
	return internalBasePath;
    }

    String getInternalBaseUrl() {
	return internalBaseUrl;
    }

    int getFileType() {
	return fileType;
    }
    
    /**
     * This method parents all objects in the scene appropriately.  If
     * the scen file specifies a Parent node for the object, then the
     * object is parented to that node.  If not, then the object is
     * parented to the scene's root.
     */
    void parentObjects() {
	debugOutputLn(TRACE, "parentObjects()");
	for (Enumeration e = objectList.elements(); e.hasMoreElements(); ) {
	    
	    LwsObject obj = (LwsObject)e.nextElement();
	    if (obj.getParent() != -1) {

		LwsObject parent = (LwsObject)
	 	           objectList.elementAt(obj.getParent() - 1);
		parent.addChild(obj);
		debugOutputLn(VALUES, "added child successfully");

	    } else {
	    
		if (obj.getObjectNode() != null)
		    sceneGroupNode.addChild(obj.getObjectNode());

	    }

            // Collect all behaviors
            if (loadBehaviors != 0) {
              if (!(obj.getObjectBehaviors()).isEmpty()) {
                sceneBehaviors.addAll(obj.getObjectBehaviors());

              }
	    }
        }

	debugOutputLn(LINE_TRACE, "Done with parentObjects()");

    }

    
    /**
     * This method sets the base URL name for data files
     * associated with the file passed into the load(URL) method.
     * The basePath should be null by default, which is an
     * indicator to the loader that it should look for any
     * associated files starting from the same directory as the
     * file passed into the load(URL) method.
     */
    public void setBaseUrl(URL url) {
	baseUrl = url;
    }

    /**
     * This method sets the base path to be used when searching for all
     * data files within a Lightwave scene.  
     */
    public void setBasePath(String pathName) {
	// This routine standardizes path names so that all pathnames
	// will have standard file separators, they'll end in a separator
	// character, and if the user passes in null or "" (meaning to
	// set the current directory as the base path), this will become
	// "./" (or ".\")
	basePath = pathName;
	if (basePath == null || basePath == "")
	    basePath = "." + java.io.File.separator;
	basePath = basePath.replace('/', java.io.File.separatorChar);
	basePath = basePath.replace('\\', java.io.File.separatorChar);
	if (!basePath.endsWith(java.io.File.separator))
	    basePath = basePath + java.io.File.separator;
    }

    /**
     * Returns the current base URL setting.  
     */
    public URL getBaseUrl() {
	return baseUrl;
    }

    /**
     * Returns the current base path setting.
     */
    public String getBasePath() {
	return basePath;
    }
    
    /**
     * This method sets the load flags for the file.  The flags should
     * equal 0 by default (which tells the loader to only load geometry).
     */
    public void setFlags(int flags) {
	loadFlags = flags;
    }

    /**
     * Returns the current loading flags setting.
     */
    public int getFlags() {
	return loadFlags;
    }


    
    /**
     * getObject() iterates through the objectList checking the given
     * name against the fileName and objectName of each object in turn.
     * For the filename, it carves off the pathname and just checks the
     * final name (e.g., "missile.lwo").
     * If name has []'s at the end, it will use the number inside those
     * brackets to pick which object out of an ordered set it will
     * send back (objectList is created in the order that objects
     * exist in the file, so this order should correspond to the order
     * specified by the user).  If no []'s exist, just pass back the
     * first one encountered that matches.
     */
    public TransformGroup getObject(String name) {
	debugOutputLn(TRACE, "getObject()");
	int indexNumber = -1;
	int currentObjectCount = 0;
	String subobjectName = name;
	if (name.indexOf("[") != -1) {
	    // caller wants specifically numbered subjbect; get that number
	    int bracketsIndex = name.indexOf("[");
	    subobjectName = name.substring(0, bracketsIndex);
	    String bracketsString = name.substring(bracketsIndex);
	    int bracketEndIndex = bracketsString.indexOf("]");
	    String indexString = bracketsString.substring(1, bracketEndIndex);
	    indexNumber = (new Integer(indexString)).intValue();
	}
	for (Enumeration e = objectList.elements() ;
	     e.hasMoreElements() ;) {
	    LwsObject tempObj = (LwsObject)e.nextElement();
	    debugOutputLn(VALUES, "tempObj, file, objname = " +
			       tempObj + tempObj.fileName +
			       tempObj.objName);
	    if ((tempObj.fileName != null &&
		 tempObj.fileName.indexOf(subobjectName) != -1) ||
		(tempObj.objName != null &&
		 tempObj.objName.indexOf(subobjectName) != -1)) {
		if (indexNumber < 0 ||
		       indexNumber == currentObjectCount)
		    return tempObj.getObjectNode();
		else
		    currentObjectCount++;
	    }
	}
	debugOutputLn(VALUES, " no luck - wanted " +
			   name + " returning null");
	return null;
    }


    /**
     * This method sets up the StreamTokenizer for the scene file.  Note
     * that we're not parsing numbers as numbers because the tokenizer
     * does not interpret scientific notation correctly.
     */
    void setupTokenizer(StreamTokenizer tokenizer) {
	tokenizer.resetSyntax();
	tokenizer.wordChars('a', 'z');
	tokenizer.wordChars('A', 'Z');
	tokenizer.wordChars(128 + 32, 255);
	tokenizer.whitespaceChars(0, ' ');
	tokenizer.commentChar('/');
	tokenizer.quoteChar('"');
	tokenizer.quoteChar('\'');
	tokenizer.wordChars('0', '9');
	tokenizer.wordChars('.', '.');
	tokenizer.wordChars('-', '-');
	tokenizer.wordChars('/', '/');
	tokenizer.wordChars('\\', '\\');
	tokenizer.wordChars('_', '_');
	tokenizer.wordChars('&', '&');
	tokenizer.ordinaryChar('(');
	tokenizer.ordinaryChar(')');
	tokenizer.whitespaceChars('\r', '\r');

	// add ':' as wordchar so urls will work
 	tokenizer.wordChars(':', ':');
	// add '~' as wordchar for urls
	tokenizer.wordChars('~', '~');
    }

    /**
     * Adds Ambient lighting effects to the scene
     */
    void addAmbient() {
	AmbientLight aLgt = new AmbientLight(ambientColor);
	BoundingSphere bounds =
	    new BoundingSphere(new Point3d(0.0,0.0,0.0), 100000.0);
	aLgt.setInfluencingBounds(bounds);
	sceneGroupNode.addChild(aLgt);
	// scope ambient light to the lw3d scene
	aLgt.addScope(sceneGroupNode);
	scene.addLightNode(aLgt);
    }	

    /**
     * Add any defined lights to the java3d scene
     */
    void addLights() {
	// Add lights to the scene
	for (Enumeration e1 = lightList.elements(); e1.hasMoreElements(); ) {
	   
	    debugOutputLn(LINE_TRACE, "adding light to scene group");
	    LwsLight light = (LwsLight)e1.nextElement();

	    if (light.getObjectNode() != null) {
		// scope light to the lw3d scene
		light.getLight().addScope(sceneGroupNode);
		
		if (light.getParent() != -1) {
		    LwsObject parent = (LwsObject)
			objectList.elementAt(light.getParent() - 1);
		    parent.addChild(light);
		}
		else {  // No parent - add to scene group
		    sceneGroupNode.addChild(light.getObjectNode());

		}
              
                // collect behaviors if LOAD_BEHAVIOR_NODES is set 
                if (loadBehaviors != 0) {
                  if (!(light.getObjectBehaviors()).isEmpty()) 
                      sceneBehaviors.addAll(light.getObjectBehaviors());

                }
                 
		scene.addLightNode(light.getLight());
	    }
	    else
		debugOutputLn(LINE_TRACE, "light object null?");
	}
    }

    /**
     * Adds the Camera's transform group to the scene, either by parenting
     * it to the appropriate object or by adding it to the scene root.
     * To use this camera data, users can request the camera/view data
     * for the scene and can then insert a ViewPlatform in the transform group.
     */
    void addCamera() {
	// Add camera effects to scene.
	if (camera != null) {
	    if (camera.getParent() != -1) {
		debugOutputLn(VALUES, "camera parent = " +
			      camera.getParent());
		LwsObject parent = (LwsObject)
		    objectList.elementAt(camera.getParent() - 1);
		parent.addChild(camera);
		debugOutputLn(VALUES, "added child successfully");
	    }
	    else {
		sceneGroupNode.addChild(camera.getObjectNode());

	    }

            // collect behaviors if LOAD_BEHAVIOR_NODES is set 
            if (loadBehaviors != 0) {
              if (!(camera.getObjectBehaviors()).isEmpty()) 
                   sceneBehaviors.addAll(camera.getObjectBehaviors());
            }

	    scene.addViewGroup(camera.getObjectNode());
	}
    }

    /**
     * Add appropriate fog effects to the scene
     */
    void addFog() {
	if (fog != null) {
	    Fog fogNode = fog.getObjectNode();
	    if (fogNode != null) {
		sceneGroupNode.addChild(fogNode);
		scene.addFogNode(fogNode);
	    }
	}
    }

    /**
     * Add the behaviors to the scene
     */
    void addBehaviors() {
        if (!sceneBehaviors.isEmpty()) {
           Enumeration e = sceneBehaviors.elements();
           while (e.hasMoreElements()) {
             scene.addBehaviorNode((Behavior)e.nextElement());
           }
        }
    }

    /**
     * Add appropriate background effects to the scene.  Note that the java3d
     * background may not have all of the information of the lw3d background,
     * as the loader does not currently process items such as gradients between
     * the horizon and sky colors
     */
    void addBackground() {
	if (background != null) {
	    Background bgNode = background.getObjectNode();
	    if (bgNode != null) {
		sceneGroupNode.addChild(bgNode);
		scene.addBackgroundNode(bgNode);
	    }
	}
    }

}
