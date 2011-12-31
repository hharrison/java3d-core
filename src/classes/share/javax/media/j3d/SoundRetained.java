/*
 * $RCSfile$
 *
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.ArrayList;



/**
 * SoundRetained is an abstract class that contains instance varables common
 * to all retained sounds.
 */

abstract class SoundRetained extends LeafRetained
{

   /**
    * Null Sound identifier denotes sound is not created or initialized
    */
    static final int NULL_SOUND = -1;

    /**
     *  sound data associated with sound source
     */
    MediaContainer  soundData = null;

    /**
     *  Overall Scale Factor applied to sound.
     */
    float     initialGain = 1.0f;  // Valid values are >= 0.0.

    /**
     *  Number of times sound is looped/repeated during play
     */
    int       loopCount = 0;  //  Range from 0 to POSITIVE_INFINITY(-1)

    /**
     *  Switch for turning sound on or off while the sound is "active"
     */
    boolean   enable = false;

    /**
     * Type of release when sound is disabled.
     *     If true, sound plays thru to end of sample before disabled
     *     Otherwise, sound is disabled immediately.
     */
    boolean   release = false;

    /**
     * Flag denoting if sound silently continues playing when it's deactivated.
     */
    boolean   continuous = false;

    /**
     * Flag denoting if sound is explicitly muted, so that if begins playing
     * it will be played silently.
     */
    boolean   mute = false;

    /**
     * Flag denoting if sound is paused from playing - waiting to be resumed
     */
    boolean   pause = false;

    /**
     * Sound priority ranking value.
     * Valid values are 0.0 to 1.0
     */
    float     priority = 1.0f;

    /**
     * Rate Scale Factor applied to sounds playback sample rate in Hertz.
     * Valid values are 0.0 to 1.0
     */
    float     rate = 1.0f;

    /**
     * The Boundary object defining the sound's scheduling region.
     */
    Bounds    schedulingRegion = null;

    /**
     * The bounding leaf reference
     */
    BoundingLeafRetained boundingLeaf = null;

    /**
     * The transformed bounds from either schedulingRegion or boundingLeaf
     */
    Bounds transformedRegion = null;

    // Dirty bit flags used to pass change as part of message, and are
    // acclummuated/stored in SoundSchedulerAtoms.
    // These flags are grouped into two catagories:
    // attribsDirty for sound node fields
    // stateDirty for changes to sound state not reflected by sound fields.

    // Attributes Dirty bit flags
    // This bitmask is set when sound node attribute is changed by the user.
    static final int SOUND_DATA_DIRTY_BIT          = 0x0001;
    static final int INITIAL_GAIN_DIRTY_BIT        = 0x0002;
    static final int LOOP_COUNT_DIRTY_BIT          = 0x0004;
    static final int BOUNDS_DIRTY_BIT              = 0x0008;
    static final int BOUNDING_LEAF_DIRTY_BIT       = 0x0010;
    static final int PRIORITY_DIRTY_BIT            = 0x0020;
    static final int POSITION_DIRTY_BIT            = 0x0040;
    static final int DISTANCE_GAIN_DIRTY_BIT       = 0x0080;
    static final int BACK_DISTANCE_GAIN_DIRTY_BIT  = 0x0100;
    static final int DIRECTION_DIRTY_BIT           = 0x0200;
    static final int ANGULAR_ATTENUATION_DIRTY_BIT = 0x0400;
    static final int RATE_DIRTY_BIT                = 0x0800;

    static final int BOUNDS_CHANGED                =
                         BOUNDS_DIRTY_BIT      | BOUNDING_LEAF_DIRTY_BIT;

    static final int ATTRIBUTE_DIRTY_BITS          =
                         SOUND_DATA_DIRTY_BIT  | INITIAL_GAIN_DIRTY_BIT  |
                         LOOP_COUNT_DIRTY_BIT  | PRIORITY_DIRTY_BIT    |
                         RATE_DIRTY_BIT;

    static final int POSITIONAL_DIRTY_BITS         =
                         ATTRIBUTE_DIRTY_BITS  |
                         POSITION_DIRTY_BIT    | DISTANCE_GAIN_DIRTY_BIT;

    static final int DIRECTIONAL_DIRTY_BITS        =
                         POSITIONAL_DIRTY_BITS | BACK_DISTANCE_GAIN_DIRTY_BIT |
                         DIRECTION_DIRTY_BIT   | ANGULAR_ATTENUATION_DIRTY_BIT;

    // All attribute bits that are specifically set or cleared for any node */
    static final int ALL_ATTIBS_DIRTY_BITS         = 0x0FFF;

    // State Dirty bit flags
    // This bitmask is set when scene graph state is changed.
    static final int LIVE_DIRTY_BIT                = 0x0001;
    static final int IMMEDIATE_MODE_DIRTY_BIT      = 0x0002;
    static final int LOAD_SOUND_DIRTY_BIT          = 0x0004;
    static final int RELEASE_DIRTY_BIT             = 0x0008;
    static final int CONTINUOUS_DIRTY_BIT          = 0x0010;
    static final int ENABLE_DIRTY_BIT              = 0x0020;
    static final int MUTE_DIRTY_BIT                = 0x0040;
    static final int PAUSE_DIRTY_BIT               = 0x0080;
    static final int XFORM_DIRTY_BIT               = 0x8000;

    // All attribute bits that are specifically set or cleared for any node */
    static final int ALL_STATE_DIRTY_BITS          = 0x80FF;

    // The type of sound node: Background, Point, Cone
    int soundType = NULL_SOUND;

    // A back reference to the scene graph sound, when this is a mirror sound
    SoundRetained sgSound = null;

    // A HashKey for sounds in a shared group
    HashKey key = null;

    // An array of mirror sounds, one for each instance of this sound in a
    // shared group.  Entry 0 is the only one valid if we are not in a shared
    // group.
    SoundRetained[] mirrorSounds = new SoundRetained[1];

    // The number of valid sounds in mirrorSounds
    int numMirrorSounds = 0;

    /**
     * Array of references to sound scheduler atoms associated with this node.
     * For each view that a sound node is associated with a sound scheduler
     * atom is created and maintained
     */
    // for a particular view that are playing either audibly or silently.
    private SoundSchedulerAtom[] loadedAtoms = new SoundSchedulerAtom[1];
    private int                  atomCount    = 0;

    /**
     * This is true when this sound is referenced in an immediate mode context
     */
    boolean   inImmCtx = false;

    /**
     * Load Sound Data Status
     */
    static final int LOAD_COMPLETE = 2;
    // load requested but could not be performed due because sound not live
    static final int LOAD_PENDING = 1;
    static final int LOAD_NULL = 0;
    static final int LOAD_FAILED = -1;
    int       loadStatus = LOAD_NULL;
    long      duration = Sound.DURATION_UNKNOWN;

    // Static initializer for SoundRetained class
    static {
	VirtualUniverse.loadLibraries();
    }

    // Target threads to be notified when sound changes
    static final int targetThreads = J3dThread.UPDATE_SOUND |
                                     J3dThread.SOUND_SCHEDULER;

    // Is true, if the mirror light is viewScoped
    boolean isViewScoped = false;


    /**
     * Dispatch a message about a sound attribute change
     */
    void dispatchAttribChange(int dirtyBit, Object argument) {
        // Send message including a integer argument
        J3dMessage createMessage = new J3dMessage();
        createMessage.threads = J3dThread.UPDATE_SOUND |
                                J3dThread.SOUND_SCHEDULER;
        createMessage.type = J3dMessage.SOUND_ATTRIB_CHANGED;
        createMessage.universe = universe;
        createMessage.args[0] = this;
        createMessage.args[1]= new Integer(dirtyBit);
        if (inSharedGroup)
                createMessage.args[2] = new Integer(numMirrorSounds);
        else
                createMessage.args[2] = new Integer(1);
        createMessage.args[3] = mirrorSounds.clone();
        createMessage.args[4] = argument;
        if (debugFlag)
	    debugPrint("dispatchAttribChange with " + dirtyBit);
        VirtualUniverse.mc.processMessage(createMessage);
    }

    /**
     * Dispatch a message about a sound state change
     */
    void dispatchStateChange(int dirtyBit, Object argument) {
        // Send message including a integer argument
        J3dMessage createMessage = new J3dMessage();
        createMessage.threads = J3dThread.UPDATE_SOUND |
                                J3dThread.SOUND_SCHEDULER;
        createMessage.type = J3dMessage.SOUND_STATE_CHANGED;
        createMessage.universe = universe;
        createMessage.args[0] = this;
        createMessage.args[1]= new Integer(dirtyBit);
        if (inSharedGroup)
                createMessage.args[2] = new Integer(numMirrorSounds);
        else
                createMessage.args[2] = new Integer(1);
        createMessage.args[3] = mirrorSounds.clone();
        createMessage.args[4] = argument;
        if (debugFlag)
	    debugPrint("dispatchStateChange with " + dirtyBit);
        VirtualUniverse.mc.processMessage(createMessage);
    }

    /**
     * Assign value into sound data field
     * @param soundData description of sound source data
     */
    void setSoundDataState(MediaContainer soundData) {
        this.soundData = soundData;
    }

    /**
     * Associates sound data with this sound source node
     * Attempt to load sound
     * @param soundData descrition of sound source data
     */
    void setSoundData(MediaContainer soundData) {
        // if resetting soundData to the same value don't bother doing anything
        if (this.soundData == soundData) {
            return;
        }

        if (this.soundData != null) {
            // this sound node had older sound data; clear it out
	    ((MediaContainerRetained)this.soundData.retained).removeUser(this);
        }

	if (source != null && source.isLive()) {
	    if (this.soundData != null) {
		((MediaContainerRetained)this.soundData.retained).clearLive(refCount);
	    }

	    if (soundData != null) {
		((MediaContainerRetained)soundData.retained).setLive(inBackgroundGroup, refCount);
	        ((MediaContainerRetained)soundData.retained).addUser(this);
	    }
	}

        this.soundData = soundData;
        dispatchAttribChange(SOUND_DATA_DIRTY_BIT, soundData);

	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Retrieves sound data associated with this sound source node
     * @return sound source data container
     */
    MediaContainer getSoundData() {
        return ( this.soundData );
    }


    /**
     * Set the gain scale factor applied to this sound
     * @param amplitude gain scale factor
     */
    void setInitialGain(float scaleFactor) {
        if (scaleFactor < 0.0f)
            this.initialGain = 0.0f;
        else
	    this.initialGain = scaleFactor;

        dispatchAttribChange(INITIAL_GAIN_DIRTY_BIT, (new Float(scaleFactor)));
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }
    /**
     * Get the overall gain (applied to the sound data associated with source).
     * @return overall gain of sound source
     */
    float getInitialGain() {
        return (float) this.initialGain;
    }


    /**
     * Sets the sound's loop count
     * @param loopCount number of times sound is looped during play
     */
    void setLoop(int loopCount) {
        if (loopCount < -1)
            this.loopCount = -1;
        else
	    this.loopCount = (int) loopCount;
        if (debugFlag)
            debugPrint("setLoopCount called with " + this.loopCount);

        dispatchAttribChange(LOOP_COUNT_DIRTY_BIT, (new Integer(loopCount)));
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Retrieves the loop count
     * @return loop count for data associated with sound
     */
    int getLoop() {
        return (int) this.loopCount;
    }

    /**
     * Enable or disable the release flag for this sound source
     * @param state flag denoting release sound before stopping
     */
    void setReleaseEnable(boolean state) {
        this.release = state;
        dispatchAttribChange(RELEASE_DIRTY_BIT, (state ? Boolean.TRUE: Boolean.FALSE));
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Retrieves release flag for sound associated with this source node
     * @return sound's release flag
     */
    boolean getReleaseEnable() {
        return (boolean) this.release;
    }

    /**
     * Enable or disable continuous play flag
     * @param state denotes if sound continues playing silently when deactivated
     */
    void setContinuousEnable(boolean state) {
        this.continuous = state;
        dispatchAttribChange(CONTINUOUS_DIRTY_BIT, (state ? Boolean.TRUE: Boolean.FALSE));
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Retrieves sound's continuous play flag
     * @return flag denoting if deactivated sound silently continues playing
     */
    boolean getContinuousEnable() {
        return (boolean) this.continuous;
    }

    /**
     * Sets the flag denotine sound enabled/disabled and sends a message
     * for the following to be done:
     *   If state is true:
     *     if sound is not playing, sound is started.
     *     if sound is playing, sound is stopped, then re-started.
     *   If state is false:
     *     if sound is playing, sound is stopped
     * @param state true or false to enable or disable the sound
     */
     void setEnable(boolean state) {
        enable = state;
	// QUESTION: Is this still valid code?
	if (source != null && source.isLive()) {
	        notifySceneGraphChanged(false);
	}
        dispatchStateChange(ENABLE_DIRTY_BIT, (new Boolean(enable)));
    }

    /**
     * Retrieves sound's enabled flag
     * @return sound enabled flag
     */
    boolean getEnable() {
        return enable;
    }

    /**
     * Set the Sound's scheduling region.
     * @param region a region that contains the Sound's new scheduling region
     */
    void setSchedulingBounds(Bounds region) {
        if (region != null) {
            schedulingRegion = (Bounds) region.clone();
	    if (staticTransform != null) {
		schedulingRegion.transform(staticTransform.transform);
	    }
            // QUESTION: Clone into transformedRegion IS required.  Why?
            transformedRegion = (Bounds) schedulingRegion.clone();
            if (debugFlag)
		debugPrint("setSchedulingBounds for a non-null region");
        }
        else {
	    schedulingRegion = null;
            // QUESTION: Is transformedRegion of node (not mirror node)
            //           even looked at???
	    transformedRegion = null;
            if (debugFlag)
		debugPrint("setSchedulingBounds for a NULL region");
        }
        // XXXX: test that this works - could not new Bounds() since
        //       Bounds is an abstract class and can't be instantiated
        dispatchAttribChange(BOUNDS_DIRTY_BIT, region);
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Get the Sound's scheduling region.
     * @return this Sound's scheduling region information
     */
    Bounds getSchedulingBounds() {
	Bounds b = null;

	if (this.schedulingRegion != null) {
            b = (Bounds) schedulingRegion.clone();
            if (staticTransform != null) {
                Transform3D invTransform = staticTransform.getInvTransform();
                b.transform(invTransform);
            }
	}
	return b;
    }

    /**
     * Set the Sound's scheduling region to the specified Leaf node.
     */
    void setSchedulingBoundingLeaf(BoundingLeaf region) {
        int i;
        int numSnds = numMirrorSounds;
        if (numMirrorSounds == 0)
            numSnds = 1;

        if ((boundingLeaf != null) &&
          (source != null && source.isLive())) {
            // Remove the mirror lights as users of the original bounding leaf
            for (i = 0; i < numSnds; i++) {
                boundingLeaf.mirrorBoundingLeaf.removeUser(mirrorSounds[i]);
            }
        }

	if (region != null) {
	    boundingLeaf = (BoundingLeafRetained)region.retained;
            // Add all mirror sounds as user of this bounding leaf
	    if (source != null && source.isLive()) {
                for (i = 0; i < numSnds; i++) {
                    boundingLeaf.mirrorBoundingLeaf.addUser(mirrorSounds[i]);
                }
            }
	} else {
	    boundingLeaf = null;
	}
        // XXXX: since BoundingLeaf constructor only takes Bounds
        //       test if region passed into dispatchAttribChange correctly.
        dispatchAttribChange(BOUNDING_LEAF_DIRTY_BIT, region);
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Get the Sound's scheduling region
     */
    BoundingLeaf getSchedulingBoundingLeaf() {
	if (boundingLeaf != null) {
	    return((BoundingLeaf)boundingLeaf.source);
	} else {
	    return null;
	}
    }

    // The update Object function.
    synchronized void updateMirrorObject(Object[] objs) {
        Transform3D trans = null;
        int component = ((Integer)objs[1]).intValue();
        if (component == -1) { // update everything
            // object 2 contains the mirror object that needs to be
            // updated
            initMirrorObject(((SoundRetained)objs[2]));
        }

        // call the parent's mirror object update routine
        super.updateMirrorObject(objs);

    }

    void updateBoundingLeaf(long refTime) {
        // This is necessary, if for example, the region
        // changes from sphere to box.
        if (boundingLeaf != null && boundingLeaf.switchState.currentSwitchOn) {
            transformedRegion = boundingLeaf.transformedRegion;
        } else { // evaluate schedulingRegion if not null
            if (schedulingRegion != null) {
                transformedRegion = schedulingRegion.copy(transformedRegion);
                transformedRegion.transform(schedulingRegion,
                                                getLastLocalToVworld());
            } else {
                transformedRegion = null;
            }
        }
    }


    /**
     * Set sound's proirity value.
     * @param priority value used to order sound's importance for playback.
     */
    void setPriority(float rank) {
        if (rank == this.priority)
            // changing priority is expensive in the sound scheduler(s)
            // so only dispatch a message if 'new' priority value is really
            // different
            return;

        this.priority = rank;
        dispatchAttribChange(PRIORITY_DIRTY_BIT, (new Float(rank)));
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Retrieves sound's priority value.
     * @return sound priority value
     */
    float getPriority() {
        return (this.priority);
    }


    /**
     * Retrieves sound's duration in milliseconds
     * @return sound's duration, returns DURATION_UNKNOWN if duration could
     * not be queried from the audio device
     */
    long getDuration() {
        return (duration);
    }


    /**
     * Set scale factor
     * @param scaleFactor applied to sound playback rate
     */
    void setRateScaleFactor(float scaleFactor) {
        this.rate = scaleFactor;
        dispatchAttribChange(RATE_DIRTY_BIT, (new Float(scaleFactor)));
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Retrieves sound's rate scale factor
     * @return sound rate scale factor
     */
    float getRateScaleFactor() {
        return (this.rate);
    }

    void changeAtomList(SoundSchedulerAtom atom, int loadStatus) {
        if (atom == null)
            return;
        if (loadStatus == SoundRetained.LOAD_COMPLETE) {
            // atom is successfully loaded, so add this atom to array of atoms
            // associated with this sound, if not already in list
            for (int i=0; i<atomCount; i++) {
                if (atom == loadedAtoms[i])
                    return;
            }
            // add atom to list
            atomCount++;
            int currentArrayLength = loadedAtoms.length;
            if (atomCount > currentArrayLength) {
                    // expand array - replace with a larger array
                    loadedAtoms = new SoundSchedulerAtom[2*currentArrayLength];
            }
            loadedAtoms[atomCount-1] = atom;  // store reference to new atom
            // all atoms sample durations SHOULD be the same so store it in node
            this.duration = atom.sampleLength; // XXXX: refine later? in ms
        }
        else {  // atom is NOT loaded or has been unloaded; remove from list
            if (atomCount == 0)
                return;

            // remove atom from array of playing atoms if it is in list
            boolean atomFound = false;
            int i;
            for (i=0; i<atomCount; i++) {
                if (atom == loadedAtoms[i])  {
                    atomFound = true;
                    continue;
                }
            }
            if (!atomFound)
                return;

            // otherwise remove atom from list by close up list
            for (int j=i; j<atomCount; j++) {
                loadedAtoms[j] = loadedAtoms[j+1];
            }
            atomCount--;
            if (atomCount == 0)
                this.duration = Sound.DURATION_UNKNOWN; // clear sound duration
        }
    }

    /**
     * Retrieves sound's ready state for ALL active views.
     * For this node, the list of sound scheduler atoms associated with
     * each view is maintained.   The 'loaded' (=is ready) state is
     * true only if the following are true for all views/sound schedulers:
     *
     * <ul>
     * 1) the Sound node has a non-null sound data and this data has
     *    sucessfully been loaded/opened/copied/attached;<br>
     * 2) the Sound node is live;<br>
     * 3) there is at least one active View in the Universe; and<br>
     * 4) an instance of an AudioDevice is attached to the current
     *    PhysicalEnvironment.
     * </ul>
     *
     * @return true if potentially playable (audibly or silently); false otherwise
     */
    boolean isReady()  {
	// all the atoms in the atom list must be are ready for this
	// method to return true
	// if any non-null atoms are found NOT ready, return false.
        boolean atomFoundReady = true;
        for (int i=0; i<atomCount; i++) {
            SoundSchedulerAtom atom = loadedAtoms[i];
            if (atom == null || atom.soundScheduler == null)
                continue;
            else
            if (atom.loadStatus == SoundRetained.LOAD_COMPLETE) {
                atomFoundReady = true;
                continue;
            }
            else
                return false;
        }
        if (atomFoundReady) // at least on atom found ready
            return true;
        else
            // not even one atom is associated with node so none are loaded
            return false;
    }

    /**
     * Retrieves sound's ready state for a particular view.
     * For this node, the list of sound scheduler atoms associated with
     * each view is maintained.   The 'loaded' (=is ready) state is
     * true only if the following are true for the given view:
     *
     * <ul>
     * 1) the Sound node has a non-null sound data and this data has
     *    sucessfully been loaded/opened/copied/attached;<br>
     * 2) the Sound node is live;<br>
     * 3) the given View is active in the Universe; and<br>
     * 4) an instance of an AudioDevice is attached to the current
     *    PhysicalEnvironment.
     * </ul>
     *
     * @param viewRef view to test sound readiness for
     * @return true if potentially playable (audibly or silently); false otherwise
     */
    boolean isReady(View viewRef)  {
	// if an atom in the atom list that is associated with the
	// given view is found and has been loaded than return true,
	// otherwise return false.
        if (viewRef == null)
            return false;
        for (int i=0; i<atomCount; i++) {
            SoundSchedulerAtom atom = loadedAtoms[i];
            if (atom == null || atom.soundScheduler == null)
                continue;
            if (atom.soundScheduler.view == viewRef)
                if (atom.loadStatus != SoundRetained.LOAD_COMPLETE)
                    return false;
                else
                    return true;
            else // atom is not associated with given referenced view
                continue;
        }
        return false;  // sound scheduler atom for given view not found

    }

    // *******************************
    //  Play Status - isPlaying states
    // *******************************

    /**
     * Retrieves sound's playing status
     * true if potentially audible (enabled and active) on ANY audio device
     * false otherwise
     * @return sound playing flag
     */
    boolean isPlaying()  {
        for (int i=0; i<atomCount; i++) {
            SoundSchedulerAtom atom = loadedAtoms[i];
            if (atom == null || atom.soundScheduler == null)
                continue;
            if (atom.status == SoundSchedulerAtom.SOUND_AUDIBLE)
                return true;
            else
                continue; // look for at lease one atom that is playing
        }
        // not even one atom is associated with this node so none are playing
        return false;
    }

    /**
     * Retrieves sound's playing status for a particular view
     * true if potentially audible (enabled and active) on audio device
     * associated with the given view
     * false otherwise
     * @param viewRef view to test sound playing state for
     * @return sound playing flag
     */
    boolean isPlaying(View viewRef)  {
        if (viewRef == null)
            return false;
        for (int i=0; i<atomCount; i++) {
            SoundSchedulerAtom atom = loadedAtoms[i];
            if (atom == null || atom.soundScheduler == null)
                continue;
            if (atom.soundScheduler.view == viewRef) {
                if (atom.status == SoundSchedulerAtom.SOUND_AUDIBLE)
                    return true;
                else
                    return false;
            }
            else // atom is not associated with given referenced view
                continue;
        }
        return false;  // atom associated with this view not found in list
    }

    /**
     * Retrieves sound's playing silently status
     * true if enabled but not active (on any device)
     * false otherwise
     * @return sound playing flag
     */
    boolean isPlayingSilently()  {
       for (int i=0; i<atomCount; i++) {
           SoundSchedulerAtom atom = loadedAtoms[i];
           if (atom == null || atom.soundScheduler == null)
               continue;
           if (atom.status == SoundSchedulerAtom.SOUND_SILENT)
               return true;
           else
               return false;
       }
       return false;  // atom not found in list or not playing audibilly
    }

    /**
     * Retrieves sound's playing silently status for a particular view
     * true if potentially audible (enabled and active) on audio device
     * associated with the given view
     * false otherwise
     * @param viewRef view to test sound playing silently state for
     * @return sound playing flag
     */
    boolean isPlayingSilently(View viewRef)  {
        if (viewRef == null)
            return false;
        for (int i=0; i<atomCount; i++) {
            SoundSchedulerAtom atom = loadedAtoms[i];
            if (atom == null || atom.soundScheduler == null)
                continue;
            if (atom.soundScheduler.view == viewRef) {
                if (atom.status == SoundSchedulerAtom.SOUND_SILENT)
                    return true;
                else
                    return false;
            }
            else // atom is not associated with given referenced view
                continue;
        }
        return false;  // atom associated with this view not found in list
    }

    /**
     * Retrieves number of channels allocated for this sound on the primary
     * view's audio device.
     * @return number of channels used by sound across all devices
     */
    int getNumberOfChannelsUsed()  {
       // retrieves the number of channels used by the atom that is:
       //     loaded, and
       //     playing either audibily or silently
       // on the device associated with the primary view.
       View primaryView = this.universe.getCurrentView();
       if (primaryView == null)
           return 0;

       // find atom associated with primary view (VirtualUniverse currentView)
       // then return the number of channels associated with that atom
       SoundSchedulerAtom atom;
       for (int i=0; i<atomCount; i++) {
           atom = loadedAtoms[i];
           if (atom == null || atom.soundScheduler == null)
               continue;
           if (atom.soundScheduler.view == primaryView) {
                   return atom.numberChannels;
           }
       }
       return 0; // atom associated with primary view not found
    }

    /**
     * Retrieves number of channels allocated for this sound on the audio
     * devices associated with a given view.
     * @param viewRef view to test sound playing silently state for
     * @return number of channels used by this sound on a particular device
     */
    int getNumberOfChannelsUsed(View viewRef)  {
       // retrieves the number of channels used by the atom that is:
       //     loaded, and
       //     playing either audibily or silently
       // on the device associated with the given view.
       if (viewRef == null)
           return 0;
       SoundSchedulerAtom atom;
       for (int i=0; i<atomCount; i++) {
           atom = loadedAtoms[i];
           if (atom == null || atom.soundScheduler == null)
               continue;
           if (atom.soundScheduler.view == viewRef) {
                   return atom.numberChannels;
           }
       }
       return 0; // atom associated with primary view not found
    }

    /**
     * Set mute state flag.  If the sound is playing it will be set to
     * play silently
     * @param state flag
     * @since Java 3D 1.3
     */
    void setMute(boolean state) {
        this.mute = state;
        dispatchAttribChange(MUTE_DIRTY_BIT, (state ? Boolean.TRUE: Boolean.FALSE));
        if (source != null && source.isLive()) {
            notifySceneGraphChanged(false);
        }
    }

    /**
     * Retrieves sound Mute state.
     * A return value of true does not imply that the sound has
     * been started playing or is still playing silently.
     * @return mute state flag
     * @since Java 3D 1.3
     */
    boolean getMute() {
        return (boolean) this.mute;
    }

    /**
     * Set pause state flag.  If the sound is playing it will be paused
     * @param state flag
     * @since Java 3D 1.3
     */
    void setPause(boolean state) {
        this.pause = state;
        dispatchAttribChange(PAUSE_DIRTY_BIT, (state ? Boolean.TRUE: Boolean.FALSE));
        if (source != null && source.isLive()) {
            notifySceneGraphChanged(false);
        }
    }

    /**
     * Retrieves sound Pause state.
     * A return value of true does not imply that the sound has
     * been started playing auditibly or silently.
     * @return mute state flag
     * @since Java 3D 1.3
     */
    boolean getPause() {
        return (boolean) this.pause;
    }


    /**
     * This sets the immedate mode context flag
     */
    void setInImmCtx(boolean inCtx) {
        inImmCtx = inCtx;
    }

    /**
     * This gets the immedate mode context flag
     */
    boolean getInImmCtx() {
        return (inImmCtx);
    }

    /**
     * This gets the mirror sound for this sound given the key.
     */
    SoundRetained getMirrorSound(HashKey key) {
	int i;
	SoundRetained[] newSounds;

	if (inSharedGroup) {
	    for (i=0; i<numMirrorSounds; i++) {
		if (mirrorSounds[i].key.equals(key)) {
		    return(mirrorSounds[i]);
		}
	    }
	    if (numMirrorSounds == mirrorSounds.length) {
		newSounds = new SoundRetained[numMirrorSounds*2];
		for (i=0; i<numMirrorSounds; i++) {
		    newSounds[i] = mirrorSounds[i];
		}
		mirrorSounds = newSounds;
	    }
	    //	    mirrorSounds[numMirrorSounds] = (SoundRetained) this.clone();
	    mirrorSounds[numMirrorSounds] = (SoundRetained) this.clone();
	    //mirrorSounds[numMirrorSounds].key = new HashKey(key);
	    mirrorSounds[numMirrorSounds].key = key;
	    mirrorSounds[numMirrorSounds].sgSound = this;
	    return(mirrorSounds[numMirrorSounds++]);
	} else {
	    if (mirrorSounds[0] == null) {
	      //  mirrorSounds[0] = (SoundRetained) this.clone(true);
		mirrorSounds[0] = (SoundRetained) this.clone();
		mirrorSounds[0].sgSound = this;
	    }
	    return(mirrorSounds[0]);
	}
    }

    synchronized void initMirrorObject(SoundRetained ms) {
        GroupRetained group;
        Transform3D trans;
        Bounds region = null;

        ms.setSchedulingBounds(getSchedulingBounds());
        ms.setSchedulingBoundingLeaf(getSchedulingBoundingLeaf());
        ms.sgSound = sgSound;
/*
// QUESTION: these are not set in LightRetained???
        ms.key = null;
        ms.mirrorSounds = new SoundRetained[1];
        ms.numMirrorSounds = 0;
*/
        ms.inImmCtx = inImmCtx;
        ms.setSoundData(getSoundData());

// XXXX: copy ms.atoms array from this.atoms

        ms.parent = parent;
        ms.inSharedGroup = false;
        ms.locale = locale;
        ms.parent = parent;
        ms.localBounds = (Bounds)localBounds.clone();

        ms.transformedRegion = null;
        if (boundingLeaf != null) {
            if (ms.boundingLeaf != null)
                ms.boundingLeaf.removeUser(ms);
            ms.boundingLeaf = boundingLeaf.mirrorBoundingLeaf;
            // Add this mirror object as user
            ms.boundingLeaf.addUser(ms);
            ms.transformedRegion = ms.boundingLeaf.transformedRegion;
        }
        else {
            ms.boundingLeaf = null;
        }

        if (schedulingRegion != null) {
            ms.schedulingRegion = (Bounds) schedulingRegion.clone();
            // Assign region only if bounding leaf is null
            if (ms.transformedRegion == null) {
                ms.transformedRegion = (Bounds) ms.schedulingRegion.clone();
                ms.transformedRegion.transform(ms.schedulingRegion,
                                    ms.getLastLocalToVworld());
            }

        }
        else {
            ms.schedulingRegion = null;
        }
    }

    void setLive(SetLiveState s) {
        SoundRetained ms;
        int i, j;

        if (debugFlag)
            debugPrint("Sound.setLive");

	if (inImmCtx) {
            throw new
               IllegalSharingException(J3dI18N.getString("SoundRetained2"));
        }
        super.setLive(s);
        if (inBackgroundGroup) {
             throw new
                IllegalSceneGraphException(J3dI18N.getString("SoundRetained3"));
        }

        if (this.loadStatus == LOAD_PENDING) {
            if (debugFlag)
                debugPrint("Sound.setLive load Sound");
            dispatchStateChange(LOAD_SOUND_DIRTY_BIT, soundData);
        }

	if (this.soundData != null) {
	    ((MediaContainerRetained)this.soundData.retained).setLive(inBackgroundGroup, s.refCount);
	}

        if (s.inSharedGroup) {
            for (i=0; i<s.keys.length; i++) {
                ms = this.getMirrorSound(s.keys[i]);
                ms.localToVworld = new Transform3D[1][];
                ms.localToVworldIndex = new int[1][];

		j = s.keys[i].equals(localToVworldKeys, 0,
				     localToVworldKeys.length);
		if(j < 0) {
		    System.err.println("SoundRetained : Can't find hashKey");
		}

                ms.localToVworld[0] = localToVworld[j];
                ms.localToVworldIndex[0] = localToVworldIndex[j];
		// If its view Scoped, then add this list
		// to be sent to Sound Structure
		if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
		    s.viewScopedNodeList.add(ms);
		    s.scopedNodesViewList.add(s.viewLists.get(i));
		} else {
		    s.nodeList.add(ms);
		}
                // Initialization of the mirror object during the INSERT_NODE
                // message (in updateMirrorObject)
                if (s.switchTargets != null && s.switchTargets[i] != null) {
                    s.switchTargets[i].addNode(ms, Targets.SND_TARGETS);
                }
                ms.switchState = (SwitchState)s.switchStates.get(j);
                if (s.transformTargets != null &&
		    s.transformTargets[i] != null) {
                    s.transformTargets[i].addNode(ms, Targets.SND_TARGETS);
		    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
                }
            }
        } else {
            ms = this.getMirrorSound(null);
            ms.localToVworld = new Transform3D[1][];
            ms.localToVworldIndex = new int[1][];
            ms.localToVworld[0] = this.localToVworld[0];
            ms.localToVworldIndex[0] = this.localToVworldIndex[0];
	    // If its view Scoped, then add this list
	    // to be sent to Sound Structure
	    if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
		s.viewScopedNodeList.add(ms);
		s.scopedNodesViewList.add(s.viewLists.get(0));
	    } else {
		s.nodeList.add(ms);
	    }
	    // Initialization of the mirror object during the INSERT_NODE
            // message (in updateMirrorObject)
            if (s.switchTargets != null && s.switchTargets[0] != null) {
                s.switchTargets[0].addNode(ms, Targets.SND_TARGETS);
            }
            ms.switchState = (SwitchState)s.switchStates.get(0);
            if (s.transformTargets != null &&
		s.transformTargets[0] != null) {
                s.transformTargets[0].addNode(ms, Targets.SND_TARGETS);
		s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
	    }
        }
        dispatchStateChange(LIVE_DIRTY_BIT, soundData);
        s.notifyThreads |= targetThreads;
    }

    void clearLive(SetLiveState s) {
	SoundRetained ms;

	super.clearLive(s);

// XXXX: if (inSharedGroup)

        if (s.inSharedGroup) {
            for (int i=0; i<s.keys.length; i++) {
                ms = this.getMirrorSound(s.keys[i]);
                if (s.switchTargets != null &&
		    s.switchTargets[i] != null) {
                    s.switchTargets[i].addNode(ms, Targets.SND_TARGETS);
                }
                if (s.transformTargets != null && s.transformTargets[i] != null) {
                    s.transformTargets[i].addNode(ms, Targets.SND_TARGETS);
		    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
		}
		// If its view Scoped, then add this list
		// to be sent to Sound Structure
		if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
		    s.viewScopedNodeList.add(ms);
		    s.scopedNodesViewList.add(s.viewLists.get(i));
		} else {
		    s.nodeList.add(ms);
		}
            }
        } else {
            ms = this.getMirrorSound(null);
            if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
                s.switchTargets[0].addNode(ms, Targets.SND_TARGETS);
            }
            if (s.transformTargets != null &&
		s.transformTargets[0] != null) {
                s.transformTargets[0].addNode(ms, Targets.SND_TARGETS);
		s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
            }
	    // If its view Scoped, then add this list
	    // to be sent to Sound Structure
	    if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
		s.viewScopedNodeList.add(ms);
		s.scopedNodesViewList.add(s.viewLists.get(0));
	    } else {
		s.nodeList.add(ms);
	    }
	}
        s.notifyThreads |= targetThreads;

	if (this.soundData != null) {
	    ((MediaContainerRetained)this.soundData.retained).clearLive(s.refCount);
	}
    }

    void mergeTransform(TransformGroupRetained xform) {
        super.mergeTransform(xform);
        if (schedulingRegion != null) {
            schedulingRegion.transform(xform.transform);
        }
    }

/*
    // This makes passed in sound look just like this sound
// QUESTION: DOesn't appread to be called
// XXXX:      ...if so, remove...
    synchronized void update(SoundRetained sound) {
        if (debugFlag)
            debugPrint("Sound.update ******** entered ***** this = " + this +
                 ", and sound param = " + sound);

        sound.soundData = soundData;
        sound.initialGain = initialGain;
        sound.loopCount = loopCount;
        sound.release = release;
        sound.continuous = continuous;
        sound.enable = enable;  // used to be 'on'
        sound.inImmCtx = inImmCtx;

// QUESTION:
//     This line removed from 1.1.1 version; why ???
        sound.currentSwitchOn = currentSwitchOn;

// NEW:
        sound.priority = priority;

// QUESTION: With code below, no sound schedulingRegion found
//        sound.schedulingRegion = schedulingRegion;
//        sound.boundingLeaf = boundingLeaf;
// XXXX: clone of region used in Traverse code, why not here???
//        if (schedulingRegion != null)
//            sound.schedulingRegion = (Bounds)schedulingRegion.clone();
// XXXX:  BoundingLeafRetained boundingLeaf ...
//        WHAT ABOUT transformedRegion??

// XXXX: Update ALL fields
// ALL THE BELOW USED TO COMMENTED OUT vvvvvvvvvvvvvvvvvvvvvvvvvvvvv
        sound.sampleLength = sampleLength;
        sound.loopStartOffset = loopStartOffset;
        sound.loopLength = loopLength;
        sound.attackLength = attackLength;
        sound.releaseLength = releaseLength;

        sound.sgSound = sgSound;
        sound.key = key;
        sound.numMirrorSounds = numMirrorSounds;
        for (int index=0; index<numMirrorSounds; index++)
             sound.mirrorSounds = mirrorSounds;
        sound.universe = universe;
        if (universe.sounds.contains(sound) == false) {
             universe.sounds.addElement(sound);
        }
        if (debugFlag)
            debugPrint("update****************************** exited");
^^^^^^^^^^^ COMMENTED OUT
    }
*/


    // Called on mirror object
// QUESTION: doesn't transformed region need to be saved???
    void updateTransformChange() {
        // If bounding leaf is null, tranform the bounds object
        if (debugFlag)
            debugPrint("SoundRetained.updateTransformChange()");
        if (boundingLeaf == null) {
            if (schedulingRegion != null) {
                transformedRegion = schedulingRegion.copy(transformedRegion);
                transformedRegion.transform(schedulingRegion,
						getLastLocalToVworld());
            }
        }
        dispatchStateChange(XFORM_DIRTY_BIT, null);
    }

// QUESTION:
//     Clone method (from 1.1.1 version) removed!?!?!? yet LightRetained has it



    // Debug print mechanism for Sound nodes
    static final boolean debugFlag = false;
    static final boolean internalErrors = false;

    void debugPrint(String message) {
        if (debugFlag) {
            System.err.println(message);
	}
    }
    void getMirrorObjects(ArrayList leafList, HashKey key) {
	if (key == null) {
	    leafList.add(mirrorSounds[0]);
	}
	else {
	    for (int i=0; i<numMirrorSounds; i++) {
		if (mirrorSounds[i].key.equals(key)) {
		    leafList.add(mirrorSounds[i]);
		    break;
		}
	    }

	}
    }

}
