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

import java.util.Hashtable;

import javax.media.j3d.Behavior;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.Light;
import javax.media.j3d.Background;
import javax.media.j3d.Fog;
import javax.media.j3d.Sound;


/**
 * The Scene interface is a set of methods used to extract
 * Java 3D scene graph information from a file loader utility.
 * The interface is used to give loaders of various
 * file formats a common public interface.
 */
public interface Scene {

    
    /**
     * This method returns the BranchGroup containing the overall
     * scene loaded by the loader.  All enabled items will be loaded
     * into this scene except for Behaviors (the Behavior group must be
     * retrieved separately so that users can choose whether and when
     * to activate behaviors).
     */
    public BranchGroup getSceneGroup();

    /**
     * This method returns an array of all View Groups defined in the file.
     * Each View Group is a TransformGroup that is already placed within
     * the scene that is returned in the getSceneGroup() call.  This
     * TransformGroup holds the position/orientation of the view
     * as defined by the file.  A user might request these references to
     * the groups in order to look at the data stored there or
     * to place ViewPlatforms within these groups and allow the
     * View to activate these ViewPlatforms so that the user would
     * see the scene from the viewpoints defined in the file.
     */
    public TransformGroup[] getViewGroups();

    /**
     * This method returns an array of floats with the horizontal field
     * of view. The entries in the array will correspond to those in the 
     * array returned by the method getViewGroups. The entries from these
     * two arrays together provide all the information needed to recreate
     * the viewing parameters associated with a scene graph.
     */
    public float[] getHorizontalFOVs();

    /**
     * This method returns an array of all Lights defined in the file.
     * If no lights are defined, null is returned.
     */
    public Light[] getLightNodes();

    /**
     * This method returns a Hashtable which contains a list of all named
     * objects in the file and their associated scene graph objects.  The
     * naming scheme for file objects is file-type dependent, but may include
     * such names as the DEF names of Vrml or filenames of objects (as
     * in Lightwave 3D).  If no named objects are defined, null is returned.
     */
    public Hashtable getNamedObjects();

    /**
     * This method returns an array of all Background nodes defined in the
     * file.  IF no Background nodes are defined, null is returned.
     */
    public Background[] getBackgroundNodes();

    /**
     * This method returns an array of all Fog nodes defined in the
     * file.  If no fog nodes are defined, null is returned.
     */
    public Fog[] getFogNodes();

    /**
     * This method returns an array of all the behavior nodes   
     * in the scene.  If no Behavior nodes are defined, null is returned.
     */
    public Behavior[] getBehaviorNodes();

    /**
     * This method returns an array of all of the Sound nodes defined
     * in the file.  If no Sound nodes are defined, null is returned.
     */
    public Sound[] getSoundNodes();

    /**
     * This method returns the text description of the file.  If no
     * such description exists, this method should return null.
     */
    public String getDescription();

}


    

    
