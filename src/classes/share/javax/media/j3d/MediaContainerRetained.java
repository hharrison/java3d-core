/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.net.URL; 
import java.io.InputStream;
import java.net.MalformedURLException; 
import java.io.File;
import java.security.*;
import java.io.InputStream;

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
        // %%% TODO AudioDevice not intellegent enough to process InputStreams yet
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
        J3dMessage createMessage = VirtualUniverse.mc.getMessage();
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
