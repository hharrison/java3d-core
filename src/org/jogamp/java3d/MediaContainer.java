/*
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

package org.jogamp.java3d;

import java.io.InputStream;
import java.net.URL;

/**
 * The MediaContainer object defines all sound data: cached state flag, and
 * associated sound media. Currently this references the sound media in
 * one of three forms: URL String, URL object, or InputStream object.
 * In future releases media data will include references to Java Media
 * Player objects.
 * Only one type of sound media data specified using
 * <code>setURLString</code>, <code>setURLObject</code>,
 * or <code>setInputStream</code> may be
 * non-null (or they may all be null).  An attempt to set more
 * than one of these attributes to a non-null reference will
 * result in an exception being thrown.  If all sound media data
 * references are null, there is no sound associated with this
 * MediaContainer and Sound nodes referencing this object cannot
 * be played.
 */
public class MediaContainer extends NodeComponent {
     /**
      * For MediaContainer component objects, specifies that this object
      * allows the reading of its cached flag.
      */
     public static final int
    ALLOW_CACHE_READ = CapabilityBits.MEDIA_CONTAINER_ALLOW_CACHE_READ;

     /**
      * For MediaContainer component objects, specifies that this object
      * allows the writing of its cached flag.
      */
     public static final int
    ALLOW_CACHE_WRITE = CapabilityBits.MEDIA_CONTAINER_ALLOW_CACHE_WRITE;

     /**
      * For MediaContainer component objects, specifies that this object
      * allows the reading of it's sound data.
      */
     public static final int
    ALLOW_URL_READ = CapabilityBits.MEDIA_CONTAINER_ALLOW_URL_READ;

     /**
      * For MediaContainer component objects, specifies that this object
      * allows the writing of it's URL path.
      */
     public static final int
    ALLOW_URL_WRITE = CapabilityBits.MEDIA_CONTAINER_ALLOW_URL_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_CACHE_READ,
        ALLOW_URL_READ
    };

    /**
     * Constructs a MediaContainer object with default parameters.
     * The default values are as follows:
     * <ul>
     * URL String data : null<br>
     * URL object data : null<br>
     * InputStream data : null<br>
     * cache enable : true<br>
     * </ul>
     */
    public MediaContainer() {
         // Just use default values
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs and initializes a MediaContainer object using specified
     * parameters.
     * @param path string of URL path containing sound data
     * @exception SoundException if the URL is not valid or cannot be opened
     */
    public MediaContainer(String path) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((MediaContainerRetained)this.retained).setURLString(path);
    }

    /**
     * Constructs and initializes a MediaContainer object using specified
     * parameters.
     * @param url URL path containing sound data
     * @exception SoundException if the URL is not valid or cannot be opened
     */
    public MediaContainer(URL url) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((MediaContainerRetained)this.retained).setURLObject(url);
    }

    /**
     * Constructs and initializes a MediaContainer object using specified
     * parameters.
     * @param stream input stream containing sound data
     *
     * @since Java 3D 1.2
     */
    public MediaContainer(InputStream stream) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((MediaContainerRetained)this.retained).setInputStream(stream);
    }

    /**
     * Creates the retained mode MediaContainerRetained object that this
     * component object will point to.
     */
    @Override
    void createRetained() {
        this.retained = new MediaContainerRetained();
        this.retained.setSource(this);
    }

    /**
     * Set Cache Enable state flag.
     * Allows the writing of sound data explicitly into the MediaContainer
     * rather than just referencing a JavaMedia container.
     * @param flag boolean denoting if sound data is cached in this instance
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setCacheEnable(boolean flag)  {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_CACHE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("MediaContainer1"));

	((MediaContainerRetained)this.retained).setCacheEnable(flag);
    }

    /**
     * Retrieve Cache Enable state flag.
     * @return flag denoting is sound data is non-cached or cached
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public boolean getCacheEnable() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_CACHE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("MediaContainer2"));

	return ((MediaContainerRetained)this.retained).getCacheEnable();
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>setURLString</code>
     */
    public void setURL(String path) {
        if (isLiveOrCompiled()) {
            if(!this.getCapability(ALLOW_URL_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("MediaContainer3"));
        }

        ((MediaContainerRetained)this.retained).setURLString(path);
    }
    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>setURLObject</code>
     */
    public void setURL(URL url) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_URL_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("MediaContainer3"));
	((MediaContainerRetained)this.retained).setURLObject(url);
    }

    /**
     * Set URL String.
     * @param path string of URL containing sound data
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception SoundException if the URL is not valid or cannot be opened
     * @exception IllegalArgumentException if the specified sound data is
     * non-null and any other sound data reference is also non-null.
     * @since Java 3D 1.2
     */
    public void setURLString(String path) {
        if (isLiveOrCompiled()) {
            if(!this.getCapability(ALLOW_URL_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("MediaContainer3"));
        }
        ((MediaContainerRetained)this.retained).setURLString(path);
    }

    /**
     * Set URL Object.
     * @param url URL object containing sound data
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception SoundException if the URL is not valid or cannot be opened
     * @exception IllegalArgumentException if the specified sound data is
     * non-null and any other sound data reference is also non-null.
     * @since Java 3D 1.2
     */
    public void setURLObject(URL url) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_URL_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("MediaContainer3"));
	((MediaContainerRetained)this.retained).setURLObject(url);
    }

    /**
     * Set Input Stream.
     * @param stream input stream object containing sound data
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception SoundException if InputStream is bad
     * @exception IllegalArgumentException if the specified sound data is
     * non-null and any other sound data reference is also non-null.
     * @since Java 3D 1.2
     */
    public void setInputStream(InputStream stream) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_URL_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("MediaContainer3"));
	((MediaContainerRetained)this.retained).setInputStream(stream);
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>getURLString</code>
     */
    public String getURL() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_URL_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("MediaContainer4"));
	return ((MediaContainerRetained)this.retained).getURLString();
    }

    /**
     * Retrieve URL String.
     * @return string of URL containing sound data
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.2
     */
    public String getURLString() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_URL_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("MediaContainer4"));
	return ((MediaContainerRetained)this.retained).getURLString();
    }

    /**
     * Retrieve URL Object.
     * @return URL containing sound data
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.2
     */
    public URL getURLObject() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_URL_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("MediaContainer4"));
	return ((MediaContainerRetained)this.retained).getURLObject();
    }

    /**
     * Retrieve Input Stream.
     * @return reference to input stream containing sound data
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.2
     */
    public InputStream getInputStream() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_URL_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("MediaContainer4"));
	return ((MediaContainerRetained)this.retained).getInputStream();
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced with
     * <code>cloneNodeComponent(boolean forceDuplicate)</code>
     */
    @Override
    public NodeComponent cloneNodeComponent() {
        MediaContainer mc = new MediaContainer();
        mc.duplicateNodeComponent(this);
        return mc;
    }


   /**
     * Copies all MediaContainer information from
     * <code>originalNodeComponent</code> into
     * the current node.  This method is called from the
     * <code>cloneNodeComponent</code> method and <code>duplicateNodeComponent</code>
     * method which is, in turn, called by the
     * <code>cloneTree</code> method.<P>
     *
     * @param originalNodeComponent the original node component to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node component's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @exception RestrictedAccessException if this object is part of a live
     *  or compiled scenegraph.
     *
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    @Override
    void duplicateAttributes(NodeComponent originalNodeComponent,
			     boolean forceDuplicate) {

	super.duplicateAttributes(originalNodeComponent, forceDuplicate);

	MediaContainerRetained mc = (MediaContainerRetained)
	    originalNodeComponent.retained;
	MediaContainerRetained rt = (MediaContainerRetained) retained;
	rt.setCacheEnable(mc.getCacheEnable());
	rt.setURLString(mc.getURLString(), false);
	rt.setURLObject(mc.getURLObject(), false);
	rt.setInputStream(mc.getInputStream(), false);
    }
}
