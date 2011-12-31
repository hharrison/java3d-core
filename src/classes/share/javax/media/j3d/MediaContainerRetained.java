/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.io.InputStream;
import java.net.URL;

/**
 * The MediaContainerRetained object defines all rendering state that can
 * be set as a component object of a retained Soundscape node.
 */
class MediaContainerRetained extends NodeComponentRetained {
     /**
      *  Gain Scale Factor applied to source with this attribute
      */
     boolean    cached = true;

     /**
      *  URL string that references the sound data
      */
     URL           url = null;
     String        urlString = null;
     InputStream   inputStream = null;


    /**
     * Set Cached flag
     * @param state flag denoting sound data is cached by app within node
     */
    void setCacheEnable(boolean state) {
        this.cached = state;
        // changing this AFTER sound data attached to node is ignored
	// notifyUsers();
    }

    /**
     * Retrieve Attrribute Gain (amplitude)
     * @return gain amplitude scale factor
     */
    boolean getCacheEnable() {
        return this.cached;
    }

    /**
     * Set URL object that references the sound data
     * @param url URL object that references the sound data
     */
    void setURLObject(URL url) {
         setURLObject(url, true);
    }

    /**
     * Set URL object that references the sound data
     * @param url URL object that references the sound data
     * @param forceLoad ensures that message about change is sent to scheduler
     */
    void setURLObject(URL url, boolean forceLoad) {
        // can NOT set URL object field unless the other related fields are null
        if (url != null) {
            if (urlString != null || inputStream != null)
                throw new IllegalArgumentException(J3dI18N.getString("MediaContainer5"));
            // Test if url object is valid by openning it
            try  {
                InputStream stream;
                stream = url.openStream();
                stream.close();
            }
            catch (Exception e) {
                throw new SoundException(javax.media.j3d.J3dI18N.getString("MediaContainer0"));
            }
        }
        this.url = url;
 	// notifyUsers();
        // avoid re-loading SAME MediaContainer when duplicateAttrib calls
        if (forceLoad)
           dispatchMessage();
    }

    /**
     * Set URL path that references the sound data
     * @param path string of URL that references the sound data
     */
    void setURLString(String path) {
        setURLString(path, true);
    }

    /**
     * Set URL path that references the sound data
     * @param path string of URL that references the sound data
     * @param forceLoad ensures that message about change is sent to scheduler
     */
    void setURLString(String path, boolean forceLoad) {
        // can NOT set string field unless the other related fields are null
        if (path != null) {
            if (this.url != null || inputStream != null)
                throw new IllegalArgumentException(J3dI18N.getString("MediaContainer5"));
            // Test if path string is valid URL by trying to generate a URL
            // and then openning it
            try  {
               URL url = new URL(path);
               InputStream stream;
               stream = url.openStream();
               stream.close();
            }
            catch (Exception e) {
                throw new SoundException(javax.media.j3d.J3dI18N.getString("MediaContainer0"));
            }
        }
        this.urlString = path;
	// notifyUsers();
        // avoid re-loading SAME MediaContainer when duplicateAttrib calls
        if (forceLoad)
           dispatchMessage();
    }

    /**
     * Set input stream reference to sound data
     * @param stream InputStream that references the sound data
     * @param forceLoad ensures that message about change is sent to scheduler
     */
    void setInputStream(InputStream stream) {
        setInputStream(stream, true);
    }

    /**
     * Set input stream reference to sound data
     * @param stream InputStream that references the sound data
     */
    void setInputStream(InputStream stream, boolean forceLoad) {
        // XXXX: AudioDevice not intellegent enough to process InputStreams yet
        // can NOT set stream field unless the other related fields are null
        if (stream != null) {
            if (url != null || urlString != null)
                throw new IllegalArgumentException(J3dI18N.getString("MediaContainer5"));
        }
        this.inputStream = stream;
	// notifyUsers();
        // avoid re-loading SAME MediaContainer when duplicateAttrib calls
        if (forceLoad)
           dispatchMessage();
    }

    /**
     * Retrieve URL String
     * @return URL string that references the sound data
     */
    String getURLString() {
        return this.urlString;
    }

    /**
     * Retrieve URL objects
     * @return URL object that references the sound data
     */
    URL getURLObject() {
        return this.url;
    }

    /**
     * Retrieve InputData
     * @return InputString that references the sound data
     */
    InputStream getInputStream() {
        return this.inputStream;
    }

    /**
     * Dispatch a message about a media container change
     */
    void dispatchMessage() {
        // Send message including a integer argumentD
        J3dMessage createMessage = new J3dMessage();
        createMessage.threads = J3dThread.SOUND_SCHEDULER;
        createMessage.type = J3dMessage.MEDIA_CONTAINER_CHANGED;
        createMessage.universe = null;
        createMessage.args[0] = this;
        createMessage.args[1]= new Integer(SoundRetained.SOUND_DATA_DIRTY_BIT);
        createMessage.args[2]= new Integer(users.size());
        createMessage.args[3] = users;
        VirtualUniverse.mc.processMessage(createMessage);
    }
}
