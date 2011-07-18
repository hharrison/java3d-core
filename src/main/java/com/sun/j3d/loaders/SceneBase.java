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

package com.sun.j3d.loaders;

import java.lang.Float;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.Light;
import javax.media.j3d.Background;
import javax.media.j3d.Fog;
import javax.media.j3d.Sound;


/**
 * This class implements the Scene interface and extends it to incorporate
 * utilities that could be used by loaders.  There should be little need
 * for future loaders to subclass this, or to implement Scene directly,
 * as the functionality of a SceneBase is fairly straightforward.  This
 * class is responsible for both the storage and retrieval of data from
 * the Scene.  The storage methods (used only by Loader writers) are all
 * of the add* routines.  The retrieval methods (used primarily by Loader
 * users) are all of the get* routines.
 */
public class SceneBase implements Scene {

    BranchGroup		sceneGroup = null;
    BranchGroup		behaviorGroup = null;
    Hashtable		namedObjects = new Hashtable();
    String		description = null;

    Vector viewVector = new Vector();
    Vector hfovVector = new Vector();
    Vector behaviorVector = new Vector();
    Vector lightVector = new Vector();
    Vector fogVector = new Vector();
    Vector backgroundVector = new Vector();
    Vector soundVector = new Vector();
    
    // Add methods

    /**
     * Sets the sceneGroup to be the group that is passed in.
     */
    public void setSceneGroup(BranchGroup scene) {
	sceneGroup = scene;
    }

    /**
     * Adds the given group to the list of view groups.
     */
    public void addViewGroup(TransformGroup tg) {
	viewVector.addElement(tg);
    }

    /**
     * Adds the given field of view value to the list of field of view values.
     */
    public void addHorizontalFOV(float hfov) {
	hfovVector.addElement(new Float(hfov));
    }

    /**
     * Adds the given behavior to a list of behaviors 
     */
    public void addBehaviorNode(Behavior b) {
	behaviorVector.addElement(b);
    }

    /**
     * Adds the given Light node to the list of lights.
     */
    public void addLightNode(Light light) {
	lightVector.addElement(light);
    }

    /**
     * Adds the given Background node to the list of backgrounds.
     */
    public void addBackgroundNode(Background background) {
	backgroundVector.addElement(background);
    }

    /**
     * Adds the given Sound node to the list of sounds.
     */
    public void addSoundNode(Sound sound) {
	soundVector.addElement(sound);
    }

    /**
     * Adds the given Fog node to the list of fog nodes.
     */
    public void addFogNode(Fog fog) {
	fogVector.addElement(fog);
    }

    /**
     * Sets the text description of the scene to the passed in String.
     */
    public void addDescription(String descriptionString) {
	description = descriptionString;
    }

    /**
     * Adds the given String/Object pair to the table of named objects.
     */
    public void addNamedObject(String name, Object object) {
	if (namedObjects.get(name) == null)
	    namedObjects.put(name, object);
	else {
	    // key already exists - append a unique integer to end of name
	    int nameIndex = 1;
	    boolean done = false;
	    while (!done) {
		// Iterate starting from "[1]" until we find a unique key
		String tempName = name + "[" + nameIndex + "]";
		if (namedObjects.get(tempName) == null) {
		    namedObjects.put(tempName, object);
		    done = true;
		}
		nameIndex++;
	    }
	}
    }
    
    /**
     * This method returns the BranchGroup containing the overall
     * scene loaded by the loader.
     */
    public BranchGroup getSceneGroup() {
	return sceneGroup;
    }


    /**
     * This method returns an array of all View Groups defined in the file.
     * A View Group is defined as a TransformGroup which contains a
     * ViewPlatform.  The TransformGroup holds the position/orientation
     * information for the given ViewPlatform and the ViewPlatform
     * holds an view-specific information, such as Field of View.
     */
    public TransformGroup[] getViewGroups() {
	if (viewVector.isEmpty())
	    return null;
	TransformGroup[] viewGroups = new TransformGroup[viewVector.size()];
	viewVector.copyInto(viewGroups);
	return viewGroups;
    }

    /**
     * This method returns an array of floats that contains the horizontal
     * field of view values for each corresponding entry in the array of
     * view groups returned by the method getViewGroups. 
     */
    public float[] getHorizontalFOVs() {
	if (hfovVector.isEmpty())
	    return null;

        int arraySize = hfovVector.size();
        float[] hfovs = new float[arraySize];
	Float[] tmpFovs = new Float[hfovVector.size()];
	hfovVector.copyInto(tmpFovs);

        // copy to array of floats and delete Floats
        for (int i=0; i<arraySize; i++) {
          hfovs[i] = tmpFovs[i].floatValue();
          tmpFovs[i] = null;
        }
	return hfovs;
    } 

    
    /**
     * This method returns an array of all Lights defined in the file.
     */
    public Light[] getLightNodes() {
	if (lightVector.isEmpty())
	    return null;
	Light[] lightNodes = new Light[lightVector.size()]; 
	lightVector.copyInto(lightNodes);
	return lightNodes;
    }
    

    /**
     * This method returns a Hashtable which contains a list of all named
     * objects in the file and their associated scene graph objects.  The
     * naming scheme for file objects is file-type dependent, but may include
     * such names as the DEF names of Vrml or filenames of subjects (as
     * in Lightwave 3D).
     */
    public Hashtable getNamedObjects() {
	return namedObjects;
    }
    

    /**
     * This method returns an array of all Background nodes defined in the
     * file.
     */
    public Background[] getBackgroundNodes() {
	if (backgroundVector.isEmpty())
	    return null;
	Background[] backgroundNodes = new Background[backgroundVector.size()];
	backgroundVector.copyInto(backgroundNodes);
	return backgroundNodes;
    }
    

    /**
     * This method returns an array of all Fog nodes defined in the
     * file.
     */
    public Fog[] getFogNodes() {
	if (fogVector.isEmpty())
	    return null;
	Fog[] fogNodes = new Fog[fogVector.size()];
	fogVector.copyInto(fogNodes);
	return fogNodes;
    }
    

    /**
     * This method returns a group containing all of the Behavior nodes
     * in the scene.
     */
    public Behavior[] getBehaviorNodes() {
	if (behaviorVector.isEmpty())
	    return null;
	Behavior[] behaviorNodes = new Behavior[behaviorVector.size()]; 
	behaviorVector.copyInto(behaviorNodes);

	return behaviorNodes;
    }
    

    /**
     * This method returns an array of all of the Sound nodes defined
     * in the file.
     */
    public Sound[] getSoundNodes() {
	if (soundVector.isEmpty())
	    return null;
	Sound[] soundNodes = new Sound[soundVector.size()];
	soundVector.copyInto(soundNodes);
	return soundNodes;
    }


    /**
     * This method returns the text description of the file.  If no
     * such description exists, this method should return null.
     */
    public String getDescription() {
	return description;
    }

}


    

    
