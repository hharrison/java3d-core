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

import java.net.URL;
import java.io.Reader;
import java.io.FileNotFoundException;

/**
 * The Loader interface is used to specify the location
 * and elements of a file format to load. 
 * The interface is used to give loaders of various
 * file formats a common public interface.  Ideally
 * the Scene interface will be implemented to give
 * the user a consistent interface to extract the
 * data.
 *
 * @see com.sun.j3d.loaders.Scene
 */
public interface Loader {

    // These are the values to be used in constructing the
    // load flags for the loader.  Users should OR the selected
    // values together to construct an aggregate flag integer
    // (see the setFlags() method).  Users wishing to load all
    // data in a file should use the LOAD_ALL specifier.
    
    /** This flag enables the loading of light objects into the scene.*/
    public static final int LOAD_LIGHT_NODES		= 1;

    /** This flag enables the loading of fog objects into the scene.*/
    public static final int LOAD_FOG_NODES		= 2;

    /** This flag enables the loading of background objects into the scene.*/
    public static final int LOAD_BACKGROUND_NODES	= 4;

    /** This flag enables the loading of behaviors into the scene.*/
    public static final int LOAD_BEHAVIOR_NODES		= 8;

    /** This flag enables the loading of view (camera) objects into
     * the scene.*/
    public static final int LOAD_VIEW_GROUPS		= 16;

    /** This flag enables the loading of sound objects into the scene.*/
    public static final int LOAD_SOUND_NODES		= 32;

    /** This flag enables the loading of all objects into the scene.*/
    public static final int LOAD_ALL			= 0xffffffff;


    // Loading methods

    /**
     * This method loads the named file and returns the Scene
     * containing the scene.  Any data files referenced by this
     * file should be located in the same place as the named file;
     * otherwise users should specify an alternate base path with
     * the setBasePath(String) method.
     */
    public Scene load(String fileName) throws FileNotFoundException,
	IncorrectFormatException, ParsingErrorException;

    /**
     * This method loads the named file and returns the Scene
     * containing the scene.  Any data files referenced by the Reader
     * should be located in the same place as the named file; otherwise,
     * users should specify an alternate base path with the setBaseUrl(URL)
     * method.
     */
    public Scene load(URL url) throws FileNotFoundException,
	IncorrectFormatException, ParsingErrorException;

    /**
     * This method loads the Reader and returns the Scene
     * containing the scene.  Any data files referenced by the Reader should
     * be located in the user's current working directory.
     */
    public Scene load(Reader reader)
	throws FileNotFoundException, IncorrectFormatException,
	    ParsingErrorException;
    

    // Variable get/set methods

    /**
     * This method sets the base URL name for data files associated with
     * the file passed into the load(URL) method.
     * The basePath should be null by default, which is an indicator
     * to the loader that it should look for any associated files starting
     * from the same directory as the file passed into the load(URL) method.
     */
    public void setBaseUrl(URL url);

    /**
     * This method sets the base path name for data files associated with
     * the file passed into the load(String) method.
     * The basePath should be null by default, which is an indicator
     * to the loader that it should look for any associated files starting
     * from the same directory as the file passed into the load(String)
     * method.
     */
    public void setBasePath(String pathName);

    /**
     * Returns the current base URL setting.  By default this is null,
     * implying the loader should look for associated files starting
     * from the same directory as the file passed into the load(URL) method.
     */
    public URL getBaseUrl();

    /**
     * Returns the current base path setting.  By default this is null,
     * implying the loader should look for associated files starting
     * from the same directory as the file passed into the load(String)
     * method.
     */
    public String getBasePath();
    
    /**
     * This method sets the load flags for the file.  The flags should
     * equal 0 by default (which tells the loader to only load geometry).
     * To enable the loading of any particular scene elements, pass
     * in a logical OR of the LOAD values specified above.
     */
    public void setFlags(int flags);

    /**
     * Returns the current loading flags setting.
     */
    public int getFlags();
}



