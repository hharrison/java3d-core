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


import java.awt.Component;
import java.io.*;
import java.util.Vector;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.IncorrectFormatException;
import java.net.MalformedURLException;

import java.net.*;

/**
 * An LwsObject is passed a handle to the text file that contains the scene
 * and is responsible for parsing a particular section of that file that
 * describes a single object. This section defines the type of object
 * (either a group or some geometry specified by an Object file) and
 * some keyframe data that describes the an animation of the
 * orientation/position/scale of the object. For geometry objects, this
 * class instantiates a J3dLwoParser object to parse the binary data file.
 * For the keyframe data, the class instantiates an LwsMotion object to
 * parse and store that data.
 */

class LwsObject extends TextfileParser implements LwsPrimitive {

    // data from the file
    String         fileName;
    String         objName;
    LwsMotion      motion;
    int            parent;
    TransformGroup objectTransform;
    Vector         objectBehavior;
    Vector         shapeList = null;
    boolean        hasPivot = false;
    TransformGroup pivotTransGroup = null;

  URL urlName;
    String protocol;
  int fileType;

    /**
     * Constructor: parses object section of this scene file and
     * creates all appropriate data structures to hold the information
     * @param st StreamTokenizer for scene file
     * @param loadObject boolean specifying that object is not a lw3d Null
     * object
     * @param firstFrame int holding the first frame of the scene's animation
     * @param totalFrames int holding the total number of frames in the scene
     * @param totalTime float holding the total time of the animation
     * @param loader Lw3dLoader loader object that was created by user
     * @param debugVals in holding current debug flags
     */
    LwsObject(StreamTokenizer st, boolean loadObject,
	      int firstFrame, int totalFrames, float totalTime,
	      Lw3dLoader loader, int debugVals)
	      throws java.io.FileNotFoundException,
		  ParsingErrorException {      
	debugPrinter.setValidOutput(debugVals);
	parent = -1;

	fileType = loader.getFileType();

	try {
	    if (loadObject) {
		// If this is true, then the object is actually described
		// in an external geometry file.  Get that filename
		fileName = getString(st);
		String path = null;
		switch (loader.getFileType()) {
		case Lw3dLoader.FILE_TYPE_FILENAME:
		    // No other case is current implemented in this loader
		    path = loader.getBasePath();
		    if (path == null)
			path = loader.getInternalBasePath();
		    if (path != null) {
			// It's not sufficient to just use the base path.
			// Lightwave scene files tend to embed path names
			// to object files that are only correct if you
			// start from a certain directory.  For example, a
			// scene file in data/ may point to an object in
			// data/Objects - but in this case
			// getInternalBasePath() would be data/, so you'd
			// look for the object in data/data/Objects...
			// To attempt to overcome this confusing state of
			// affairs, let's check path/filename
			// first, then iterate all the way up the path
			// until there are no more members of path.  This
			// will ensure that we'll at least pick up data
			// files if they exist off of one of the parent
			// directories of wherever the scene file is
			// stored.
			// No, I don't really like this solution, but I don't
			// know of a better general approach for now...

			fileName = getQualifiedFilename(path, fileName);
		    }
		    break;
		case Lw3dLoader.FILE_TYPE_URL:
		  path = "";
		  URL pathUrl = loader.getBaseUrl();
		  if (pathUrl != null) {
		    path = pathUrl.toString();
		    // store the protocol
		    protocol = pathUrl.getProtocol();
		  }
		  else {
		    path = loader.getInternalBaseUrl();
		    // store the protocol
		    protocol = (new URL(path)).getProtocol();
		  }

		  urlName = getQualifiedURL(path, fileName);
		  break;
		}
	    }
	    else
		// else the object is a lw3d Null object; essentially a group
		// which contains other objects
		objName = getString(st);
	    skip(st, "ShowObject", 2);
	    debugOutputLn(LINE_TRACE,
			  "skipped showobject, about to get objectmotion");
	    getAndCheckString(st, "ObjectMotion");
	    debugOutputLn(LINE_TRACE, "got string " + st.sval);
	    // Create an LwsMotion object to parse the animation data
	    motion = new LwsMotion(st, firstFrame, totalFrames,
				   totalTime, debugVals);
	    debugOutputLn(LINE_TRACE, "got motion");
	    boolean hasParent = false; // keeps bones prim from reassigning par

	    // TODO: This isn't the greatest way to stop parsing an object
	    // (keying on the ShowOptions parameter), but it seems to be valid
	    // for the current lw3d format
	    while (!isCurrentToken(st, "ShadowOptions")) {
		if (!hasParent &&
		    isCurrentToken(st, "ParentObject")) {
		    parent = (int)getNumber(st);
		    hasParent = true;
		}
		else if (isCurrentToken(st, "PivotPoint")) {
		    // PivotPoint objects are tricky - they make the object
		    // rotate about this new point instead of the default
		    // So setup transform groups such that this happens
		    // correctly.
		    hasPivot = true;
		    float x = (float)getNumber(st);
		    float y = (float)getNumber(st);
		    float z = (float)getNumber(st);
		    Vector3f pivotPoint = new Vector3f(-x, -y, z);
		    Transform3D pivotTransform = new Transform3D();
		    pivotTransform.set(pivotPoint);
		    pivotTransGroup = new TransformGroup(pivotTransform);
		    pivotTransGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		}
		    
		else if (isCurrentToken(st, "ObjDissolve")) {
		    // Just handle it for now, don't care about value
		    EnvelopeHandler env = 
			new EnvelopeHandler(st, totalFrames, totalTime);
		}
		st.nextToken();
	    }
	    getNumber(st); // skip shadow options parameter
        debugOutputLn(LINE_TRACE, "done with LwsObject constructor");
	}
	catch (MalformedURLException e) {
	    throw new FileNotFoundException(e.getMessage());
	}
	catch (IOException e) {
	    throw new ParsingErrorException(e.getMessage());
	}
	catch (NumberFormatException e) {
	    throw new ParsingErrorException("Expected a number, got " +
		e.getMessage());
	}
    }

    /**
     * This method takes the given path and filename and checks whether
     * that file exists.  If not, it chops off the last part of pathname
     * and recurse to try again.  This has the effect of searching all the
     * way up a given pathname for the existence of the file anywhere
     * along that path.  This is somewhat of a hack to get around the
     * constraining way that Lightwave uses to define its data object
     * locations in its scene files.
     *
     * If the filename is absolute, it will use the path information from
     * the filename first, then the path information from the lws file.
     * If the file can not be found in these locations, the local directory 
     * will be searched.
     * In addition, it will look for filenames converted to lowercase to 
     * make it easier to use between Windows and Unix.
     */

  String getQualifiedFilename(String pathname, String filename) 
      throws java.io.FileNotFoundException {

    int index;
    String pathname2 = "";

    //    System.out.println ("pathname:"+pathname+" filename:"+filename);

    // Do we have an absolute filename ?
    if (filename.indexOf (File.separator) == 0) {
      if ((index = filename.lastIndexOf (File.separator)) != -1) {
	pathname2 = filename.substring (0, index+1);
	filename = filename.substring (index+1);
      }
      else {
	  return null; // something out of the ordinary happened
      }
    }
    
    // See if we can find the file
    // ---------------------------

    // Try pathname from absolute filename
    try {
	if (new File(pathname2 + filename).exists()) {
	    return (pathname2 + filename);
	}
    }
    catch (NullPointerException ex) {
	ex.printStackTrace();
    }
    // Try lowercase filename 
    if (new File(pathname2 + filename.toLowerCase()).exists()) {
      return (pathname2 + filename.toLowerCase());
    }

    // Try original pathname 
    if (new File(pathname + filename).exists()) {
      return (pathname + filename);
    }
    // Try lowercase filename 
    if (new File(pathname + filename.toLowerCase()).exists()) {
      return (pathname + filename.toLowerCase());
    }

    // Finally, let's check the local directory 
    if (new File(filename).exists()) {
      return (filename);
    }
    // Try lowercase filename 
    if (new File(filename.toLowerCase()).exists()) {
      return (filename.toLowerCase());
    }

    // Conditions that determine when we give up on the recursive search 
    if ((pathname.equals(File.separator)) || 
	(pathname == null) ||
	(pathname.equals(""))) {
	
	throw new java.io.FileNotFoundException(filename);
    }

    // Try to find the file in the upper directories 
    // Chop off the last directory from pathname and recurse 
    StringBuffer newPathName = new StringBuffer(128);
    StringTokenizer st = new StringTokenizer(pathname, File.separator);
    int tokenCount = st.countTokens() - 1;
    if (pathname.startsWith(java.io.File.separator))
      newPathName.append(File.separator);
    for (int i = 0; i < tokenCount; ++i) {
      String directory = st.nextToken();
      newPathName.append(directory);
      newPathName.append(File.separator);
    }

    String newPath = newPathName.toString();
    return getQualifiedFilename(newPath, filename);
  }

  URL getQualifiedURL(String path, String file)
      throws MalformedURLException {
      
    URL url = null;

    // try the path and the file -- this is the lightwave spec
    try {
      // create url
      url = new URL(path + file);
      // see if file exists
      url.getContent();
      // return url if no exception is thrown
      return url;
    }
    catch (IOException e) {
      // Ignore - try something different
    }

    // try a couple other options, but trying to open connections is slow,
    // so don't try as many options as getQualifiedFilename

    // try absolute path
    try {
      url = new URL(file);
      url.getContent();
    }
    catch (IOException ex) {
      // Ignore - try something different
    }

    // try the absolute path with the protocol
    try {
	url = new URL(protocol + ":" + file);
	url.getContent();
	return url;
    }
    catch (IOException ex) {
	// Nothing else to try so give up
	throw new MalformedURLException(path + file);
    }
  }
  
    /**
     * Returns parent object
     */
    int getParent() {
	return parent;
    }

    /**
     * Adds the given child to the transform of this node (its parent).
     */
    void addChild(LwsPrimitive child) {
	debugOutputLn(TRACE, "addChild()");
	if (objectTransform != null) {
	    debugOutputLn(LINE_TRACE, "objectTransform = " + objectTransform);
	    if (child.getObjectNode() != null) {
		debugOutputLn(LINE_TRACE, "child has object node");
		if (hasPivot)
		    pivotTransGroup.addChild(child.getObjectNode());
		else
		    objectTransform.addChild(child.getObjectNode());
	    }
/*
	    if (child.getObjectBehaviors() != null) {
		debugOutputLn(LINE_TRACE, "child has behaviors");
		Group bg = child.getObjectBehaviors();
		debugOutputLn(VALUES, " child behaviors = " + bg);
		// TODO: should remove intermediate group nodes
		objectBehavior.addChild(bg);
	    }
*/
	}
    }

    /**
     * Creates Java3d objects from the data stored for this object.
     * The objects created consist of: A TransformGroup that holds the
     * transform specified by the first keyframe, a Behavior that acts
     * on the TransformGroup if there are more than 1 keyframes, and
     * some geometry (created by J3dLwoParser) from an external geometry
     * file (if the object wasn't an lw3d Null object (group)).
     */
    void createJava3dObject(LwsObject cloneObject, int loadBehaviors)
	throws IncorrectFormatException, ParsingErrorException,
	    FileNotFoundException
    {
	String seqToken = new String("_sequence_");
	Matrix4d mat = new Matrix4d();
	mat.setIdentity();
	// Set the node's transform matrix according to the first frame
	// of the object's motion 
	LwsFrame firstFrame = motion.getFirstFrame();
	firstFrame.setMatrix(mat);
	Transform3D t1 = new Transform3D();
	t1.set(mat);
	objectTransform = new TransformGroup(t1);
	objectTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

	// This following bit is a hack and should probably be removed.
	// It was put in here in order to handle the "Tloop" functionality
	// of holosketch, which was needed for the 1998 Javaone conference
	// (the HighNoon demo, in particular).  Having the code here, or
	// using it, means that some object file names are tagged as special
	// because they contain the phrase "_sequence_", which tells this
	// object file reader that that object file is, in fact, a
	// sequence file.  It then creates a SequenceReader object to
	// read that file and create an animation from the objects defined
	// in that file.
	// See SequenceReader.java for more information on this.
	// By the way, a better/fuller implementation of this functionality
	// would involve investigating a standard Plug-In for Lightwave
	// that allows the writing out of sequence files from Bones data.
	// i think it would be better to base any Tloop stuff on that
	// standard than on some proprietary hack of our own.

	if (fileName != null && fileName.indexOf(seqToken) != -1) { // Tloop
	   
	    int index = fileName.indexOf(seqToken);
	    index += seqToken.length();
	    String seqFilename = fileName.substring(index);
	    int endIndex = seqFilename.indexOf(".lwo");
	    if (endIndex != -1)
		seqFilename = seqFilename.substring(0, endIndex);
	    if ((new File(seqFilename)).exists()) {
		SequenceReader sr =
		    new SequenceReader(seqFilename,
				       motion.totalTime,
				       (int)motion.totalFrames);
		sr.printLines();
		sr.createJava3dObjects(debugPrinter.getValidOutput(),
                                                         loadBehaviors);
		Group  g = sr.getObjectNode();
		if (g != null)
		    objectTransform.addChild(g);

                // Sequence reader's getObjectBehaviors creates new Vector
		objectBehavior = sr.getObjectBehaviors();

		return;
            }	
	}

	// Okay, now that that hack is out of the way, let's get on with
	// "normal" Lightwave object files.
	if (fileName != null || urlName != null) {
	    // If this object refers to an obj file, load it and create
	    // geometry from it.
	    if (cloneObject == null) {
		debugOutputLn(VALUES,
			      "About to load binary file for " + fileName);
		// Create a J3dLwoParser object to parse the geometry file
		// and create the appropriate geometry
		J3dLwoParser objParser = null;
		switch (fileType) {
		case Lw3dLoader.FILE_TYPE_FILENAME:
		  objParser =
		    new J3dLwoParser(fileName,
				     debugPrinter.getValidOutput());
		  break;
		case Lw3dLoader.FILE_TYPE_URL:
		  objParser = new J3dLwoParser(urlName,
					       debugPrinter.getValidOutput());
		  break;
		}
		objParser.createJava3dGeometry();
		// pivot points change the parent transform
		if (hasPivot) {
		    objectTransform.addChild(pivotTransGroup);
		}
		if (objParser.getJava3dShapeList() != null) {
		    shapeList = objParser.getJava3dShapeList();
		    for (Enumeration e = shapeList.elements() ;
			 e.hasMoreElements() ;) {
			if (!hasPivot || pivotTransGroup == null)
			    objectTransform.addChild((Shape3D)e.nextElement());
			else
			    pivotTransGroup.addChild((Shape3D)e.nextElement());
		    }
		}
	    }
	    else {
		// Already read that file: Clone original object
		debugOutputLn(LINE_TRACE, "Cloning shapes");
		Vector cloneShapeList = cloneObject.getShapeList();
		for (Enumeration e = cloneShapeList.elements() ;
		     e.hasMoreElements() ;) {
		    debugOutputLn(LINE_TRACE, "   shape clone");
		    Shape3D shape = (Shape3D)e.nextElement();
		    Shape3D cloneShape = (Shape3D)shape.cloneTree();
		    objectTransform.addChild(cloneShape);
		}
	    }
	}

	// Create j3d behaviors for the object's animation
        objectBehavior = new Vector();
        if (loadBehaviors != 0) {
	  motion.createJava3dBehaviors(objectTransform);
          Behavior b = motion.getBehaviors();
          if (b != null)
            objectBehavior.addElement(b);
        }
    }

    /**
     * Return list of Shape3D objects for this object file.  This is used
     * when cloning objects (if the scene file requests the same object file
     * more than once, that object will be cloned instead of recreated each
     * time).
     */
    Vector getShapeList() {
	return shapeList;
    }

    /**
     * Return the TransformGroup that holds this object file
     */
    public TransformGroup getObjectNode() {
	return objectTransform;
    }

    /**
     * Return the Group that holds this object's behaviors.  The behaviors
     * are grouped separately from the geometry so that they can be handled
     * differently by the parent application.
     */
    public Vector getObjectBehaviors()
    {
	debugOutputLn(TRACE, "getObjectBehaviors()");
	return objectBehavior;
    }


    /**
     * Utiliy function to print some of the object values.  Used in
     * debugging.
     */
    void printVals()
    {
	debugOutputLn(VALUES, "  OBJECT vals: ");
	debugOutputLn(VALUES, "   fileName = " + fileName);
	debugOutputLn(VALUES, "   objName = " + objName);
	motion.printVals();
    }
}	
