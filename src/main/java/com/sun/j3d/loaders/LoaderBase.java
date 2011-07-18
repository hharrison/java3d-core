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

/**
 * This class implements the Loader interface.  To use
 * a file loader would extend this class.
 */
public abstract class LoaderBase implements Loader {

    /** Stores the types of objects that the user wishes to load.*/
    protected int loadFlags = 0;

    /** Stores the baseUrl for data files associated with the URL
     * passed into load(URL).*/
    protected URL baseUrl = null;

    /** Stores the basePath for data files associated with the file
     * passed into load(String).*/
    protected String basePath = null;

    // Constructors

    /**
     * Constructs a Loader with default values for all variables.
     */
    public LoaderBase() {
    }
    
    /**
     * Constructs a Loader with the specified flags word.
     */
    public LoaderBase(int flags) {
	loadFlags = flags;
    }


    // Variable get/set methods

    /**
     * This method sets the base URL name for data files associated with
     * the file.  The baseUrl should be null by default, which is an indicator
     * to the loader that it should look for any associated files starting
     * from the same place as the URL passed into the load(URL) method.
     * Note: Users of setBaseUrl() would then use load(URL)
     * as opposed to load(String).
     */
    public void setBaseUrl(URL url) {
	baseUrl = url;
    }

    /**
     * This method sets the base path name for data files associated with
     * the file.  The basePath should be null by default, which is an indicator
     * to the loader that it should look for any associated files starting
     * from the same directory as the file passed into the load(String)
     * method.
     * Note: Users of setBasePath() would then use load(String)
     * as opposed to load(URL).
     */
    public void setBasePath(String pathName) {
	basePath = pathName;
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
    
}

    
    
    
    
	    

    
