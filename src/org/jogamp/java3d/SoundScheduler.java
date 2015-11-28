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

import java.awt.AWTEvent;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

/**
 * This structure parallels the RenderBin structure and
 * is used for sounds
 */
class SoundScheduler extends J3dStructure {

    /**
     * The View that owns this SoundScheduler
     */
    View view = null;

    /**
     * This boolean tells the thread to suspend itself.
     * This is true ONLY when everythings ready to render using run loop
     */
    boolean ready = false;

    /**
     * The ViewPlatform that is associated with this SoundScheduler
     */
    ViewPlatformRetained viewPlatform = null;

    /**
     * The GraphicContext3D that we are currently unning in.
     */
    GraphicsContext3D graphicsCtx = null;

    /**
     * Maintain what reference to the last AuralAttributes found active
     * was so that only if there was a change do we need to reset these
     * parameters in the AudioDevice3D.
     */
    AuralAttributesRetained lastAA = null;

    /**
     * Since AuralAttribute gain scale factor is multipled with sound's
     * initialGain scale factor, any change in AuralAttrib gain scale
     * factor should force an update of all active sounds' gains
     * Also, change in AuralAttributes should force a sound update
     * even if no other sound field changes occurred.
     */
    boolean resetAA = true;

    /**
     * Audio Device
     */
    AudioDevice     audioDevice = null;
    AudioDevice3D   audioDevice3D = null;
    AudioDevice3DL2 audioDevice3DL2 = null;
    int           totalChannels = 0;

    /**
     * Array of SoundScapeRetained nodes that intersect the viewPlatform
     * This list is a subset of the soundscapes array, and is used when
     * selecting the closest Soundscape.
     * Maintained as an expandable array.
     */
    SoundscapeRetained[] intersectedSoundscapes = new SoundscapeRetained[32];

    /**
     * Array of Bounds nodes for the corresponding intersectedSoundscapes
     * array.  This array is used when selecting the closest Soundscape.
     * This list is used when selecting the closest Soundscape.
     * Maintained as an expandable array.
     */
    Bounds[] intersectedRegions = new Bounds[32];

    /**
     * Reference to last processed region within run().
     * Maintained to avoid re-transforming this bounds.
     */
    Bounds    region = null;

    /**
     * An array of prioritized sounds currently playing "live" sounds.
     * This prioritized sound list is NO longer re-create instead sounds
     * are insert, shuffled or removed as messages are processed.
     */
    // XXXX: (Enhancement) should have a seperate list for
    //       background sound and a list for positional sounds
    ArrayList<SoundSchedulerAtom> prioritizedSounds = new ArrayList<SoundSchedulerAtom>();

    /**
     * Current number of scene graph sound nodes in the universe
     */
    int  nRetainedSounds = -1;   // none calculated yet

    /**
     * Current number of immediate mode sound nodes in the universe
     */
    int  nImmedSounds = -1;      // none calculated yet

    /**
     * Current active (selected) attribute node in the sceneGraph
     */
    AuralAttributesRetained aaRetained = null;

    // variables for processing transform messages
    boolean transformMsg = false;
    UpdateTargets targets = null;


    /**
     * Current active (selected) attribute node in the sceneGraph
     */
    AuralAttributesRetained aaImmed = null;

    // Dirty flags for fields and parameters that are unique to the
    // Sound Scheduler or the Audio Device
    // Any listener (body) and/or view transform changes processed in
    // CanvasViewCache force one of these flags to be set.
    static final int EAR_POSITIONS_CHANGED         =  0x0001;
    static final int EYE_POSITIONS_CHANGED         =  0x0002;
    static final int IMAGE_PLATE_TO_VWORLD_CHANGED =  0x0004;
    static final int HEAD_TO_VWORLD_CHANGED        =  0x0008;
    static final int LISTENER_CHANGED              =  0x000F;// all of the above
    private int        listenerUpdated = LISTENER_CHANGED;

    /**
     * Temporary flag that's denotes that a positional sound was processed
     * in the current loop of renderChange().
     */
    private boolean    positionalSoundUpdated = false;

    /**
     * Temporary flag that's denotes that some field auralAttribute was changed
     */
    private boolean    auralAttribsChanged = true;  // force processing 1st x

    private boolean     stallThread = false;

    int lastEventReceived = WindowEvent.WINDOW_CLOSED;

    /**
     * Constructs a new SoundScheduler
     */
    SoundScheduler(VirtualUniverse u, View v) {
	super(u, J3dThread.SOUND_SCHEDULER);

	// Assertion check view & universe
	if (v == null) {
	    System.err.println("WARNING: SoundScheduler constructed with null view");
	}
	if (u == null) {
	    System.err.println("WARNING: SoundScheduler constructed with null universe");
	}

	universe = u;
	view = v;
	reset();
    }


    // NOTE: processMessage only called with updatethread.active true
    @Override
    void processMessages(long referenceTime) {
	J3dMessage[] messages = getMessages(referenceTime);
	int nMsg = getNumMessage();
	J3dMessage m;
	int nSounds;

	if (nMsg > 0) {
	    for (int i=0; i < nMsg; i++) {
		m = messages[i];

		switch (m.type) {
		case J3dMessage.INSERT_NODES:
		    insertNodes(m);
		    break;
		case J3dMessage.REMOVE_NODES:
		    removeNodes(m);
		    break;
		case J3dMessage.SOUND_ATTRIB_CHANGED:
		    changeNodeAttrib(m);
		    break;
		case J3dMessage.SOUND_STATE_CHANGED:
		    changeNodeState(m);
		    break;
		case J3dMessage.BOUNDINGLEAF_CHANGED:
		    processBoundingLeafChanged(m);
		    break;
		case J3dMessage.SOUNDSCAPE_CHANGED:
		    SoundscapeRetained ss = (SoundscapeRetained)m.args[0];
		    if (universe.soundStructure.isSoundscapeScopedToView(ss, view)) {
			auralAttribsChanged = true;
			changeNodeAttrib(m);
		    }
		    break;
		case J3dMessage.AURALATTRIBUTES_CHANGED:
		    auralAttribsChanged = true;
		    changeNodeAttrib(m);
		    break;
		case J3dMessage.MEDIA_CONTAINER_CHANGED:
		    changeNodeAttrib(m);
		    break;
		case J3dMessage.TRANSFORM_CHANGED:
		    transformMsg = true;
		    auralAttribsChanged = true;
		    break;
		case J3dMessage.RENDER_IMMEDIATE:
		    processImmediateNodes(m.args, referenceTime);
		    break;
		case J3dMessage.VIEWSPECIFICGROUP_CHANGED:
		    processViewSpecificGroupChanged(m);
		    break;
		case J3dMessage.UPDATE_VIEW:
		    if (debugFlag)
			debugPrint(".processMessage() UPDATE_VIEW");
		    // NOTE: can only rely on seeing UPDATE_VIEW when canvas [re]Created
		    //      AND when view deactivated...
		    // NOTE:
		    //      temp work-around
		    //      calling prioritizeSounds() wipes out old atom fields
		    // QUESTION: prioritizedSound is NEVER empty - why if size is 0 can
		    //      .isEmpty return anything but TRUE???
		    //
		    if (prioritizedSounds.isEmpty()) {
			nSounds = prioritizeSounds();
		    }
		    break;
		case J3dMessage.SWITCH_CHANGED:
		    if (debugFlag)
			debugPrint(".processMessage() " +
				   "SWITCH_CHANGED ignored");
		    break;
		}  // switch
		m.decRefcount();
	    }  // for
	    if (transformMsg) {
		targets = universe.transformStructure.getTargetList();
		updateTransformChange(targets, referenceTime);
		transformMsg = false;
		targets = null;
	    }
	    Arrays.fill(messages, 0, nMsg, null);
	}

	// Call renderChanges within try/catch so errors won't kill
	// the SoundScheduler.
	try {
	    renderChanges();
	}
	catch (RuntimeException e) {
	    System.err.println("Exception occurred " +
			       "during Sound rendering:");
	    e.printStackTrace();
	}
        catch (Error e) {
            // Issue 264 - catch Error
	    System.err.println("Error occurred " +
			       "during Sound rendering:");
	    e.printStackTrace();
        }

	// what if the user/app makes no change to scenegraph?
	// must still re-render after retest for sound complete
	// calculate which sound will finished first and set a
	// wait time to this shortest time so that scheduler is
	// re-entered to process sound complete.

	long  waitTime = shortestTimeToFinish();

	if (waitTime == 0L) {
	    // come right back
	    if (debugFlag)
		debugPrint(".processMessage calls sendRunMessage " +
			   "for immediate processing");
	    VirtualUniverse.mc.sendRunMessage(universe,
					      J3dThread.SOUND_SCHEDULER);
	}
	else if (waitTime > 0L) {
	    // Use TimerThread to send message with sounds complete.
	    // This uses waitForElapse time to sleep for at least the duration
	    // returned by shortestTimeToFinish method.
	    if (debugFlag)
		debugPrint(".processMessage calls sendRunMessage " +
			   "with wait time = " + waitTime );
	    // QUESTION (ISSUE): even when this is set to a large time
	    //     processMessage is reentered immediately.
	    //     Why is timer thread not waiting??
	    VirtualUniverse.mc.sendRunMessage(waitTime, view,
					      J3dThread.SOUND_SCHEDULER);
	}
    }

    void insertNodes(J3dMessage m) {
	Object[] nodes = (Object[])m.args[0];
	ArrayList<NodeRetained> viewScopedNodes = (ArrayList<NodeRetained>)m.args[3];
	ArrayList<ArrayList<View>> scopedNodesViewList = (ArrayList<ArrayList<View>>)m.args[4];

	for (int i=0; i<nodes.length; i++) {
	    Object node = nodes[i];
	    if (node instanceof SoundRetained) {
		nRetainedSounds++;
		// insert sound node into sound scheduler's prioritized list
		addSound((SoundRetained) node);
	    }
	    else if (node instanceof SoundscapeRetained) {
		auralAttribsChanged = true;
	    }
	    else if (node instanceof AuralAttributesRetained) {
		auralAttribsChanged = true;
	    }
	    else if (node instanceof ViewPlatformRetained) {
		// XXXX: don't support multiple viewPlatforms per scheduler
		/*
		// useful for resetting VP ??
		addViewPlatform((ViewPlatformRetained) node);
		*/
		if (debugFlag) {
		    debugPrint(".insertNodes() viewPlatformRetained not supported yet");
		}
	    }
	}

	// Handle ViewScoped Nodes
	if (viewScopedNodes != null) {
	    int size = viewScopedNodes.size();
	    for (int i = 0; i < size; i++) {
		NodeRetained node = viewScopedNodes.get(i);
		ArrayList<View> vl = scopedNodesViewList.get(i);
		// If the node object is scoped to this view, then ..
		if (vl.contains(view)) {
		    if (node instanceof SoundRetained) {
			nRetainedSounds++;
			// insert sound node into sound scheduler's prioritized list
			addSound((SoundRetained) node);
		    }
		    else if (node instanceof SoundscapeRetained) {
			auralAttribsChanged = true;
		    }
		}
	    }
	}
    }

    /**
     * Add sound to sounds list.
     */
    void addSound(SoundRetained sound) {
	if (sound == null)
	    return;
	if (debugFlag)
	    debugPrint(".addSound()");
	synchronized (prioritizedSounds) {
	    addPrioritizedSound(sound);
	}
    } // end addSound


    /**
     * Node removed from tree
     */
    @Override
    void removeNodes(J3dMessage m) {
	Object[] nodes = (Object[])m.args[0];
	ArrayList<NodeRetained> viewScopedNodes = (ArrayList<NodeRetained>)m.args[3];
	ArrayList<ArrayList<View>> scopedNodesViewList = (ArrayList<ArrayList<View>>)m.args[4];

	for (int i=0; i<nodes.length; i++) {
	    Object node = nodes[i];
	    if (node instanceof SoundRetained) {
		// sound is deactivated but NOT deleted
		// incase sound is reattached

// QUESTION: what's the difference in messages between really deleting
//     a node and just deactivating it.
/*
//
// can't delete atom in case it's reactivitated
//
			nRetainedSounds--;
			deleteSound((SoundRetained) node);
//
// until there's a difference in messages between detaching and deleting
// a sound node, sound is stopped and atom enable state changed but atom
// is NOT deleted from list.
*/
		SoundSchedulerAtom soundAtom = null;
		for (int arrIndx=1; ;arrIndx++) {
		    soundAtom = findSoundAtom((SoundRetained)node,
					      arrIndx);
		    if (soundAtom == null)
			break;
		    stopSound(soundAtom, false);
		}
	    }
	    else if (node instanceof SoundscapeRetained) {
		auralAttribsChanged = true;
	    }
	}
	// Handle ViewScoped Nodes
	if (viewScopedNodes != null) {
	    int size = viewScopedNodes.size();
	    for (int i = 0; i < size; i++) {
		NodeRetained node = viewScopedNodes.get(i);
		ArrayList<View> vl = scopedNodesViewList.get(i);
		// If the node object is scoped to this view, then ..
		if (vl.contains(view)) {
		    if (node instanceof SoundRetained) {
			SoundSchedulerAtom soundAtom = null;
			for (int arrIndx=1; ;arrIndx++) {
			    soundAtom = findSoundAtom((SoundRetained)node,
						      arrIndx);
			    if (soundAtom == null)
				break;
			    stopSound(soundAtom, false);
			}
		    }
		    else if (node instanceof SoundscapeRetained) {
			auralAttribsChanged = true;
		    }
		}
	    }
	}

    }


    // deletes all instances of the sound nodes from the priority list
    void deleteSound(SoundRetained sound) {
	if (sound != null)
	    return;
	if (debugFlag)
	    debugPrint(".deleteSound()");
	synchronized (prioritizedSounds) {
	    if (!prioritizedSounds.isEmpty()) {
		// find sound in list and remove it
		int arrSize = prioritizedSounds.size();
		for (int index=0; index<arrSize; index++) {
		    SoundSchedulerAtom soundAtom = prioritizedSounds.get(index);
		    // QUESTION: which???
		    if (soundAtom.sound == sound ||
			soundAtom.sound.sgSound == sound) {
			stopSound(soundAtom, false);
			prioritizedSounds.remove(index);
		    }
		}
	    }
	}
    }


    void changeNodeAttrib(J3dMessage m) {
	Object node = m.args[0];
	Object value = m.args[1];
	int    attribDirty = ((Integer)value).intValue();
	if (debugFlag)
	    debugPrint(".changeNodeAttrib:");

	if (node instanceof SoundRetained &&
	    universe.soundStructure.isSoundScopedToView(node, view)) {

	    this.setAttribsDirtyFlag((SoundRetained)node, attribDirty);
	    if (debugFlag)
		debugPrint("         Sound node dirty bit = " + attribDirty);
	    if ((attribDirty & SoundRetained.PRIORITY_DIRTY_BIT) > 0) {
		shuffleSound((SoundRetained) node);
	    }
	    if ((attribDirty & SoundRetained.SOUND_DATA_DIRTY_BIT) >0) {
		if (debugFlag)
		    debugPrint(".changeNodeAttrib " +
                               "SOUND_DATA_DIRTY_BIT calls loadSound");
		loadSound((SoundRetained) node, true);
	    }
	    if ((attribDirty & SoundRetained.MUTE_DIRTY_BIT) > 0) {
		if (debugFlag)
		    debugPrint("         MuteDirtyBit is on");
		muteSound((SoundRetained) node);
	    }
	    if ((attribDirty & SoundRetained.PAUSE_DIRTY_BIT) > 0) {
		if (debugFlag)
		    debugPrint("         PauseDirtyBit is on");
		pauseSound((SoundRetained) node);
	    }
	}
	else if (node instanceof SoundscapeRetained  &&
		 universe.soundStructure.isSoundscapeScopedToView(node, view)) {
	    auralAttribsChanged = true;
	}
	else if (node instanceof AuralAttributesRetained) {
	    auralAttribsChanged = true;
	}
	else if (node instanceof MediaContainerRetained) {
	    int listSize = ((Integer)m.args[2]).intValue();
	    ArrayList userList = (ArrayList)m.args[3];
	    for (int i = 0; i < listSize; i++) {
		SoundRetained sound = (SoundRetained)userList.get(i);
		if (sound != null) {
		    loadSound(sound, true);
		    if (debugFlag)
			debugPrint(".changeNodeAttrib " +
				   "MEDIA_CONTAINER_CHANGE calls loadSound");
		}
	    }
	}
    }


    void changeNodeState(J3dMessage m) {
	Object node = m.args[0];
	Object value = m.args[1];
	if (debugFlag)
	    debugPrint(".changeNodeState:");
	if (node instanceof SoundRetained && universe.soundStructure.isSoundScopedToView(node, view)) {
	    int stateDirty = ((Integer)value).intValue();
	    setStateDirtyFlag((SoundRetained)node, stateDirty);
	    if (debugFlag)
		debugPrint("         Sound node dirty bit = "+stateDirty);
	    if ((stateDirty & SoundRetained.LIVE_DIRTY_BIT) > 0) {
		if (debugFlag)
		    debugPrint(".changeNodeState LIVE_DIRTY_BIT " +
			       "calls loadSound");
		loadSound((SoundRetained) node, false);
	    }
	    if ((stateDirty & SoundRetained.ENABLE_DIRTY_BIT) > 0) {
		if (debugFlag)
		    debugPrint("         EnableDirtyBit is on");
		if (((Boolean) m.args[4]).booleanValue()) {
		    enableSound((SoundRetained) node);
		} else {
		    SoundSchedulerAtom soundAtom;
		    SoundRetained soundRetained = (SoundRetained) node;
		    for (int i=prioritizedSounds.size()-1; i >=0; i--) {
			soundAtom = prioritizedSounds.get(i);
			if (soundAtom.sound.sgSound == soundRetained) {
			    // ignore soundRetained.release
			    // flag which is not implement
			    turnOff(soundAtom);
                            // Fix to Issue 431.
                            soundAtom.enable(soundRetained.enable);
			}
		    }
		}
	    }
	}
    }

    void shuffleSound(SoundRetained sound) {
	// Find sound atom that references this sound node and
	// reinsert it into prioritized sound list by removing atom for
	// this sound from priority list, then re-add it.
	// Assumes priority has really changed since a message is not sent
	// to the scheduler if the 'new' priority value isn't different.
	deleteSound(sound);  // remove atom for this sound
	addSound(sound);     // then re-insert it back into list in new position
    }


    void loadSound(SoundRetained sound, boolean forceReload) {
	// find sound atom that references this sound node
	// QUESTION: "node" probably not mirror node?
	SoundSchedulerAtom soundAtom = null;
	for (int i=1; ;i++) {
	    soundAtom = findSoundAtom(sound, i);
	    if (soundAtom == null)
		break;
	    MediaContainer mediaContainer = sound.getSoundData();
	    if (forceReload ||
		soundAtom.loadStatus != SoundRetained.LOAD_COMPLETE) {
		if (debugFlag)
		    debugPrint(": not LOAD_COMPLETE - try attaching");
		attachSoundData(soundAtom, mediaContainer, forceReload);
	    }
	}
    }


    void enableSound(SoundRetained sound) {
	if (debugFlag)
	    debugPrint(".enableSound " + sound );
	// find sound atom that references this sound node
	SoundSchedulerAtom soundAtom = null;
	for (int i=1; ;i++) {
	    soundAtom = findSoundAtom(sound, i);
	    if (soundAtom == null)
		break;
	    // Set atom enabled field based on current Sound node
	    // enable boolean flag
	    soundAtom.enable(sound.enable);
	}
    }


    void muteSound(SoundRetained sound) {
	// make mute pending
	// mute -> MAKE-SILENT
	// unmute -> MAKE-AUDIBLE
	if (debugFlag)
	    debugPrint(".muteSound " + sound );
	// find sound atom that references this sound node
	SoundSchedulerAtom soundAtom = null;
	for (int i=1; ;i++) {
	    soundAtom = findSoundAtom(sound, i);
	    if (soundAtom == null)
		break;
	    // Set atom mute field based on node current
	    // mute boolean flag
	    soundAtom.mute(sound.mute);
	}
    }

    void pauseSound(SoundRetained sound) {
        // make pause pending
        // Pause is a separate action
        // When resumed it has to reset its state
        //     PAUSE_AUDIBLE
        //     PAUSE_SILENT
        //     RESUME_AUDIBLE
        //     RESUME_SILENT
        // to whatever it was before
	if (debugFlag)
	    debugPrint(".pauseSound " + sound );
	// find sound atom that references this sound node
	SoundSchedulerAtom soundAtom = null;
	for (int i=1; ;i++) {
	    soundAtom = findSoundAtom(sound, i);
	    if (soundAtom == null)
		break;
	    // Set atom pause field based on node's current
	    // pause boolean flag
	    soundAtom.pause(sound.pause);
	}
    }

    void processImmediateNodes(Object[] args, long referenceTime) {
	Object command = args[0];
	Object newNode = args[1];
	Object oldNode = args[2];
	Sound oldSound = (Sound)oldNode;
	Sound newSound = (Sound)newNode;
	int action = ((Integer)command).intValue();
	if (debugFlag)
	    debugPrint(".processImmediateNodes() - action = " +
		       action);
	switch (action) {
	case GraphicsContext3D.ADD_SOUND :
	case GraphicsContext3D.INSERT_SOUND :
	    addSound((SoundRetained)newSound.retained);
	    nImmedSounds++;
	    break;
	case GraphicsContext3D.REMOVE_SOUND :
	    deleteSound((SoundRetained)oldSound.retained);
	    nImmedSounds--;
	    break;
	case GraphicsContext3D.SET_SOUND :
	    deleteSound((SoundRetained)oldSound.retained);
	    addSound((SoundRetained)newSound.retained);
	    break;
	}
    }


    void updateTransformChange(UpdateTargets targets, long referenceTime) {
	// node.updateTransformChange() called immediately rather than
	// waiting for updateObject to be called and process xformChangeList
	// which apprears to only happen when sound started...

	UnorderList arrList = targets.targetList[Targets.SND_TARGETS];
	if (arrList != null) {
	    int j,i;
	    Object nodes[], nodesArr[];
	    int size = arrList.size();
	    nodesArr = arrList.toArray(false);

	    for (j = 0; j<size; j++) {
		nodes = (Object[])nodesArr[j];


		for (i = 0; i < nodes.length; i++) {
		    if (nodes[i] instanceof ConeSoundRetained && universe.soundStructure.isSoundScopedToView(nodes[i], view)) {
			ConeSoundRetained cnSndNode =
			    (ConeSoundRetained)nodes[i];
			synchronized (cnSndNode) {
			    cnSndNode.updateTransformChange();
			}
			// set XFORM_DIRTY_BIT in corresponding atom
			setStateDirtyFlag((SoundRetained)nodes[i],
					  SoundRetained.XFORM_DIRTY_BIT);
		    } else if (nodes[i] instanceof PointSoundRetained && universe.soundStructure.isSoundScopedToView(nodes[i], view)) {
			PointSoundRetained ptSndNode =
			    (PointSoundRetained)nodes[i];
			synchronized (ptSndNode) {
			    ptSndNode.updateTransformChange();
			}
			// set XFORM_DIRTY_BIT in corresponding atom
			setStateDirtyFlag((SoundRetained)nodes[i],
					  SoundRetained.XFORM_DIRTY_BIT);
		    } else if (nodes[i] instanceof SoundscapeRetained && universe.soundStructure.isSoundscapeScopedToView(nodes[i], view)) {
			SoundscapeRetained sndScapeNode =
			    (SoundscapeRetained)nodes[i];
			synchronized (sndScapeNode) {
			    sndScapeNode.updateTransformChange();
			}
		    }
		}
	    }
	}
    }


    void updateTransformedFields(SoundRetained mirSound) {
	if (mirSound instanceof ConeSoundRetained && universe.soundStructure.isSoundScopedToView(mirSound, view)) {
	    ConeSoundRetained cnSndNode = (ConeSoundRetained)mirSound;
	    synchronized (cnSndNode) {
		cnSndNode.updateTransformChange();
	    }
	}
	else if (mirSound instanceof PointSoundRetained && universe.soundStructure.isSoundScopedToView(mirSound, view)) {
	    PointSoundRetained ptSndNode = (PointSoundRetained)mirSound;
	    synchronized (ptSndNode) {
		ptSndNode.updateTransformChange();
	    }
	}
    }


    void activate() {
	updateThread.active = true;
	if (debugFlag)
	    debugPrint(".activate(): calls sendRunMessage for immediate processing");
	VirtualUniverse.mc.sendRunMessage(universe,
					  J3dThread.SOUND_SCHEDULER);
	// renderChanges() called indirectly thru processMessage now
    }


    // deactivate scheduler only if it state has such that it can not perform
    // sound processing
    void deactivate() {
	if (debugFlag)
	    debugPrint(".deactivate()");
	//

	// XXXX: The following code is clearly erroneous.
	// The indendation, along with the 2nd test of
	// "if (debugFlag)" in the else clause, suggests that
	// the intent was to make the else clause apply to
	// "if (checkState())".  However, the else clause
	// actually applies to the first "if (debugFlag)".
	// This is a textbook example of why one should
	// *ALWAYS* enclose the target of an "if", "while", or
	// "else" in curly braces -- even when the target
	// consists of a single statement.
	//
	// The upshot of this, is that the else clause is
	// unconditionally executed, since "debugFlag" is a
	// static final constant that is set to false for
	// production builds.  We won't fix it now, because
	// The SoundScheduler may actually be relying on the
	// fact that all sounds are unconditionally
	// deactivated when this method is called, and we
	// don't want to introduce a new bug.
	//
	if (checkState())
	    if (debugFlag)
		debugPrint("  checkState returns true");
	else {
	    if (debugFlag)
		debugPrint("  checkState returns false; deactive scheduler");
	    // Call deactivateAllSounds within try/catch so
	    // errors won't kill the SoundScheduler.
	    try {
		deactivateAllSounds();
	    }
	    catch (RuntimeException e) {
		System.err.println("Exception occurred " +
				   "during sound deactivation:");
		e.printStackTrace();
	    }
	    catch (Error e) {
                // Issue 264 - catch Error
		System.err.println("Error occurred " +
				   "during sound deactivation:");
		e.printStackTrace();
	    }
	    updateThread.active = false;
	}
    }


    // Check the ready state and return true if ready
    boolean checkState() {
	boolean runState = false;
	if (stallThread) {
	    if (debugFlag)
		debugPrint(" checkState stallThread true");
	    runState = false;
	}

	if (ready) {
	    if (debugFlag)
		debugPrint(" checkState ready to run");
	    runState = true;
	}
	else {
	    // previous not ready, force reset call to see if everything
	    // ready now or not
	    reset();
	    if (ready) {
		if (debugFlag)
		    debugPrint(" checkState Now ready to run");
		runState = true;
	    }
	    else {
		if (debugFlag) {
		    debugPrint(" checkState NOT ready to run");
		}
		runState = false;
	    }
	}
	return runState;
    }

    synchronized void reset() {
	// Return quickly if universe, view, physical env, or audio
	// device are null
	if (universe == null ||
	    view == null ||
	    view.physicalEnvironment == null ||
	    view.physicalEnvironment.audioDevice == null) {

	    audioDevice = null;
	    ready = false;
	    return;
	}

	// Set AudioDevice
	audioDevice = view.physicalEnvironment.audioDevice;

	// Get viewPlatform; if it is null or not live, we can't render
	ViewPlatform vp = view.getViewPlatform();
	if (vp == null || vp.retained == null) {
	    // System.err.println("    vp is null");
	    viewPlatform = null;
	    ready = false;
	    return;
	}

	viewPlatform = (ViewPlatformRetained)vp.retained;
	if (!vp.isLive()) {
	    ready = false;
	    return;
	}

	// XXXX: Does not support multiple canvases per view, thus
	//      multiple GraphicsContext3Ds
	// QUESTION: what does that mean for sound -
	//      being applied to only ONE graphics context?
	// GET FIRST Canvas
	Canvas3D canvas = view.getFirstCanvas();
	if (canvas != null) {
	    graphicsCtx = canvas.getGraphicsContext3D();
	}

	// now the render loop can be run successfully
	audioDevice3DL2 = null;
	audioDevice3D = null;
	if (audioDevice instanceof AudioDevice3DL2) {
	    audioDevice3DL2 = (AudioDevice3DL2)audioDevice;
        }
	if (audioDevice instanceof AudioDevice3D) {
	    audioDevice3D = (AudioDevice3D)audioDevice;
            if (debugFlag)
	        debugPrint(".reset: audioDevice3D.setView");
	    audioDevice3D.setView(view);
	    totalChannels = audioDevice.getTotalChannels();
	    if (debugFlag)
	        debugPrint(" audioDevice3D.getTotalChannels returned " +
                                   totalChannels);
	}
	else {
	    if (internalErrors)
	        debugPrint(": AudioDevice implementation not supported");
	    totalChannels = 0;
	}

        if (totalChannels == 0) {
	    ready = false;
            return;
        }

        ready = true;
        // since audio device is ready; set enable flag for continuous
        // calculating userHead-to-VirtualWorld transform
        view.setUserHeadToVworldEnable(true);
	return;
    }


    void receiveAWTEvent(AWTEvent evt) {
	int eventId = evt.getID();
	if (debugFlag)
	    debugPrint(".receiveAWTEvent " + eventId);
	if (ready && eventId == WindowEvent.WINDOW_ICONIFIED) {
	    lastEventReceived = eventId;
	}
	else if (ready &&
		 (lastEventReceived == WindowEvent.WINDOW_ICONIFIED &&
		  eventId == WindowEvent.WINDOW_DEICONIFIED) ) {
	    lastEventReceived = eventId;
	    // used to notify
	}
    }


    /**
     * The main loop for the Sound Scheduler.
     */
    void renderChanges() {
	int nSounds = 0;
	int totalChannelsUsed = 0;
	int nPrioritizedSound = 0;
	int numActiveSounds = 0;
	if (debugFlag)
	    debugPrint(" renderChanges begun");
	// XXXX: BUG?? should wait if audioDevice is NULL or nSounds = 0
	//          when a new sound is added or deleted from list, or
	//          when the audioDevice is set into PhysicalEnvironment

	if (!checkState()) {
	    if (debugFlag)
		debugPrint(".workToDo()  checkState failed");
	    return;
	}

	/*
	synchronized (prioritizedSounds) {
	*/
	    nPrioritizedSound = prioritizedSounds.size();
	    if (debugFlag)
		debugPrint(" nPrioritizedSound = " + nPrioritizedSound);
	    if (nPrioritizedSound == 0)
		return;

	    if (auralAttribsChanged) {
		// Find closest active aural attributes in scene graph
		int nIntersected = findActiveSoundscapes();
		if (nIntersected > 0) {
		    if (debugFlag)
			debugPrint("        "+ nIntersected +
				   " active SoundScapes found");
		    // XXXX: (Performance) calling clone everytime, even
		    //     though closest AA has NOT changed, is expensive
		    aaRetained = (AuralAttributesRetained)
			(findClosestAAttribs(nIntersected)).clone();
		}
		else {
		    if (debugFlag) debugPrint(" NO active SoundScapes found");
		}
	    }
	    if (nPrioritizedSound > 0) {
		calcSchedulingAction();
		muteSilentSounds();

		// short term flag set within performActions->update()
		positionalSoundUpdated = false;

		// if listener parameters changed re-set View parameters
		if (testListenerFlag()) {
		    if (debugFlag)
			debugPrint(" audioDevice3D.setView");
		    audioDevice3D.setView(view);
		}

		numActiveSounds = performActions();

		if (positionalSoundUpdated) {
		    // if performActions updated at least one positional sound
		    // was processed so the listener/view changes were processed,
		    // thus we can clear the SoundScheduler dirtyFlag, otherwise
		    // leave the flag dirty until a positional sound is updated
		    clearListenerFlag();  // clears listenerUpdated flag
		}
	    }
	/*
	}
	*/
    }


    /**
     * Prioritize all sounds associated with SoundScheduler (view)
     * This only need be done once when scheduler is initialized since
     * the priority list is updated when:
     *     a) PRIORITY_DIRTY_BIT in soundDirty field set; or
     *     b) sound added or removed from live array list
     */
    int prioritizeSounds() {
	int size;
	synchronized (prioritizedSounds) {
	    if (!prioritizedSounds.isEmpty()) {
		prioritizedSounds.clear();
	    }
	    // XXXX: sync soundStructure sound list
	    UnorderList retainedSounds = universe.soundStructure.getSoundList(view);
	    // QUESTION: what is in this sound list??
	    //          mirror node or actual node???
	    nRetainedSounds = 0;
	    nImmedSounds = 0;
	    if (debugFlag)
		debugPrint(" prioritizeSound , num retained sounds" +
			   retainedSounds.size());
	    for (int i=0; i<retainedSounds.size(); i++) {
		addPrioritizedSound((SoundRetained)retainedSounds.get(i));
		nRetainedSounds++;
	    }
	    // XXXX: sync canvases
		Enumeration<Canvas3D> canvases = view.getAllCanvas3Ds();
	    while (canvases.hasMoreElements()) {
			Canvas3D canvas = canvases.nextElement();
		GraphicsContext3D graphicsContext = canvas.getGraphicsContext3D();
		Enumeration nonretainedSounds = graphicsContext.getAllSounds();
		while (nonretainedSounds.hasMoreElements()) {
		    if (debugFlag)
			debugPrint(" prioritizeSound , get non-retained sound");
		    Sound sound = (Sound)nonretainedSounds.nextElement();
		    if (sound == null)  {
			if (debugFlag)
			    debugPrint(" prioritizeSound , sound element is null");
			// QUESTION: why should I have to do this?
			continue;
		    }
		    addPrioritizedSound((SoundRetained)sound.retained);
		    nImmedSounds++;
		}
	    }
	    if (debugFlag)
		debugPrint(" prioritizeSound , num of processed retained sounds" +
			   nRetainedSounds);
	    debugPrint(" prioritizeSound , num of processed non-retained sounds" +
		       nImmedSounds);
	    size = prioritizedSounds.size();
	} // sync
	return  size;
    }


    // methods that call this should synchronize prioritizedSounds
    void addPrioritizedSound(SoundRetained mirSound) {
	SoundRetained sound = mirSound.sgSound;
	if (sound == null) {  // this mirSound is a nonretained sound
	    // pad the "child" sg sound pointer with itself
	    mirSound.sgSound = mirSound;
	    sound = mirSound;
	    if (debugFlag)
		debugPrint(":addPritorizedSound() sound NULL");
	}
	boolean addAtom = false;
	// see if this atom is in the list already
	// covers the case where the node was detached or unswitched but NOT
	// deleted (so sample already loaded
	// QUESTION: is above logic correct???
	SoundSchedulerAtom atom = null;
	atom = findSoundAtom(mirSound, 1);  // look thru list for 1st instance
	if (atom == null) {
	    atom = new SoundSchedulerAtom();
	    atom.soundScheduler = this;  // save scheduler atom is associated with
	    addAtom = true;
	}

	// update fields in atom based on sound nodes state
	atom.sound = mirSound; // new mirror sound
	updateTransformedFields(mirSound);

	if ( !addAtom ) {
	    return;
	}

	// if this atom being added then set the enable state
	atom.enable(sound.enable);

	if (prioritizedSounds.isEmpty()) {
	    // List is currently empty, so just add it
	    // insert into empty list of prioritizedSounds
	    prioritizedSounds.add(atom);
	    if (debugFlag)
		debugPrint(":addPritorizedSound() inset sound " +
			   mirSound + " into empty priority list");
	}
	else {
	    // something's in the proirity list already
	    // Since list is not empty insert sound into list.
	    //
	    // Order is highest to lowest priority values, and
	    // for sounds with equal priority values, sound
	    // inserted first get in list given higher priority.
	    SoundRetained jSound;
	    SoundSchedulerAtom jAtom;
	    int   j;
	    int   jsounds = (prioritizedSounds.size() - 1);
	    float soundPriority = sound.priority;
	    for (j=jsounds; j>=0; j--) {
		jAtom = prioritizedSounds.get(j);
		jSound = jAtom.sound;
		if (debugFlag)
		    debugPrint(": priority of sound " + jSound.sgSound +
			       " element " + (j+1) + " of prioritized list");
		if (soundPriority <= jSound.sgSound.priority) {
		    if (j==jsounds) {
			// last element's priority is larger than
			// current sound's priority, so add this
			// sound to the end of the list
			prioritizedSounds.add(atom);
			if (debugFlag)
			    debugPrint(": insert sound at list bottom");
			break;
		    }
		    else {
			if (debugFlag)
			    debugPrint(
				       ": insert sound as list element " +
				       (j+1));
			prioritizedSounds.add(j+1, atom);
			break;
		    }
		}
	    } // for loop
	    if (j < 0) { // insert at the top of the list
		if (debugFlag)
		    debugPrint(": insert sound at top of priority list");
		prioritizedSounds.add(0, atom);
	    }
	}  // else list not empty
    }


    /**
     * Process active Soundscapes (if there are any) and intersect these
     * soundscapes with the viewPlatform.
     *
     * Returns the number of soundscapes that intesect with
     * view volume.
     */
    int findActiveSoundscapes() {
	int                nSscapes = 0;
	int                nSelectedSScapes = 0;
	SoundscapeRetained ss = null;
	SoundscapeRetained lss = null;
	boolean            intersected = false;
	int                nUnivSscapes = 0;
	UnorderList        soundScapes = null;

	// Make a copy of references to the soundscapes in the universe
	// that are both switch on and have non-null (transformed) regions,
	// don't bother testing for intersection with view.
	if (universe == null) {
	    if (debugFlag)
		debugPrint(".findActiveSoundscapes() univ=null");
	    return 0;
	}
	soundScapes = universe.soundStructure.getSoundscapeList(view);
	if (soundScapes == null) {
	    if (debugFlag)
		debugPrint(".findActiveSoundscapes() soundScapes null");
	    return 0;
	}

	synchronized (soundScapes) {
	    nUnivSscapes = soundScapes.size;
	    if (nUnivSscapes == 0) {
		if (debugFlag)
		    debugPrint(
			       ".findActiveSoundscapes() soundScapes size=0");
		return 0;
	    }

	    // increase arrays lengths by increments of 32 elements
	    if (intersectedRegions.length < nSscapes) {
		intersectedRegions = new Bounds[nSscapes + 32];
	    }
	    if (intersectedSoundscapes.length < nSscapes) {
		intersectedSoundscapes = new SoundscapeRetained[nSscapes + 32];
	    }

	    // nSscapes is incremented for every Soundscape found
	    if (debugFlag)
		debugPrint(".findActiveSoundscapes() nUnivSscapes="+
			   nUnivSscapes);
	    nSelectedSScapes = 0;
	    for (int k=0; k<nUnivSscapes; k++) {
		lss = (SoundscapeRetained)soundScapes.get(k);

		//  Find soundscapes that intersect the view platform region
		if (lss.transformedRegion == null ) {
		    continue;
		}
		if ((region instanceof BoundingSphere &&
		     lss.transformedRegion instanceof BoundingSphere) ||
		    (region instanceof BoundingBox &&
		     lss.transformedRegion instanceof BoundingBox) ||
		    (region instanceof BoundingPolytope &&
		     lss.transformedRegion instanceof BoundingPolytope) ) {
		    lss.transformedRegion.getWithLock(region);
		    if (debugFlag)
			debugPrint(" get tranformed region " + region );
		} else {
		    region = (Bounds)lss.transformedRegion.clone();
		    if (debugFlag)
			debugPrint(" clone tranformed region " + region );
		}
		if (region!=null && viewPlatform.schedSphere.intersect(region)){
		    intersectedRegions[nSelectedSScapes] = (Bounds)region.clone();
		    // test View Platform intersection(lss))
		    intersectedSoundscapes[nSelectedSScapes] = lss;
		    if (debugFlag)
			debugPrint(" store sscape " + lss +
				   " and region " + region + " into array");
		    nSelectedSScapes++;
		    if (debugFlag)
			debugPrint(": region of intersection for "+
				   "soundscape "+k+" found at "+J3dClock.currentTimeMillis());
		} else {
		    if (debugFlag)
			debugPrint(
				   ": region of intersection for soundscape "+
				   k + " not found at "+ J3dClock.currentTimeMillis());
		}
	    }
	}
	return(nSelectedSScapes);
    }


    AuralAttributesRetained findClosestAAttribs(int nSelectedSScapes) {
	AuralAttributes    aa = null;
	SoundscapeRetained ss = null;
	boolean            intersected = false;

	// Get auralAttributes from closest (non-null) soundscape
	ss = null;
	if (nSelectedSScapes == 1)
	    ss = intersectedSoundscapes[0];
	else if (nSelectedSScapes > 1) {
	    Bounds closestRegions;
	    closestRegions = viewPlatform.schedSphere.closestIntersection(
									  intersectedRegions);
	    for (int j=0; j < intersectedRegions.length; j++) {
		if (debugFlag)
		    debugPrint("        element " + j +
			       " in intersectedSoundsscapes is " + intersectedRegions[j]);
		if (intersectedRegions[j] == closestRegions) {
		    ss = intersectedSoundscapes[j];
		    if (debugFlag)
			debugPrint("        element " + j + " is closest");
		    break;
		}
	    }
	}

	if (ss != null) {
	    if (debugFlag)
		debugPrint("        closest SoundScape found is " + ss);
	    aa = ss.getAuralAttributes();
	    if (aa != null) {
		if (debugFlag)
		    debugPrint(": AuralAttribute for " +
			       "soundscape is NOT null");
	    } else {
		if (debugFlag)
		    debugPrint(": AuralAttribute for " +
			       "soundscape " + ss + " is NULL");
	    }
	}
	else {
	    if (debugFlag)
		debugPrint(": AuralAttribute is null " +
			   "since soundscape is NULL");
	}

	if (debugFlag)
	    debugPrint(
		       "        auralAttrib for closest SoundScape found is " + aa);
	return ((AuralAttributesRetained)aa.retained);
    }

    /**
     *  Send current aural attributes to audio device
     *
     *  Note that a AA's dirtyFlag is clear only after parameters are sent to
     *  audio device.
     */
    void updateAuralAttribs(AuralAttributesRetained attribs) {
        if (auralAttribsChanged) {
	    if (attribs != null) {
	        synchronized (attribs) {
/*
		// XXXX: remove use of aaDirty from AuralAttrib node
		if ((attribs != lastAA) || attribs.aaDirty)
*/
		    if (debugFlag) {
			debugPrint("     set real updateAuralAttribs because");
		    }

		    // Send current aural attributes to audio device
		    // Assumes that aural attribute parameter is NOT null.
		    audioDevice3D.setRolloff(attribs.rolloff);
		    if (debugFlag)
			debugPrint("     rolloff " + attribs.rolloff);

		    // Distance filter parameters
		    int arraySize = attribs.getDistanceFilterLength();
		    if ((attribs.filterType ==
			  AuralAttributesRetained.NO_FILTERING) ||
			       arraySize == 0 ) {
		        audioDevice3D.setDistanceFilter(
					   attribs.NO_FILTERING, null, null);
			if (debugFlag)
			    debugPrint("     no filtering");
		    }
		    else {
			Point2f[] attenuation = new Point2f[arraySize];
			for (int i=0; i< arraySize; i++)
			    attenuation[i] = new Point2f();
			attribs.getDistanceFilter(attenuation);
			double[] distance = new double[arraySize];
			float[] cutoff = new float[arraySize];
			for (int i=0; i< arraySize; i++)  {
			    distance[i] = attenuation[i].x;
			    cutoff[i] = attenuation[i].y;
			}
			audioDevice3D.setDistanceFilter(attribs.filterType,
				       distance, cutoff);
			if (debugFlag) {
			    debugPrint("     filtering parameters: " +
			       " distance, cutoff arrays");
			    for (int jj=0; jj<arraySize; jj++)
			       debugPrint(
			    "        " + distance[jj]+", "+cutoff[jj]);
			}
		    }
		    audioDevice3D.setFrequencyScaleFactor(
				       attribs.frequencyScaleFactor);
		    if (debugFlag)
			debugPrint("     freq.scale " +
				    attribs.frequencyScaleFactor);
		    audioDevice3D.setVelocityScaleFactor(
			       attribs.velocityScaleFactor);
		    if (debugFlag)
			debugPrint("     velocity scale " +
					attribs.velocityScaleFactor);
		    audioDevice3D.setReflectionCoefficient(
					attribs.reflectionCoefficient);
                    if (audioDevice3DL2 != null) {
		        audioDevice3DL2.setReverbCoefficient(
                                        attribs.reverbCoefficient);
		        audioDevice3DL2.setDecayFilter(attribs.decayFilter);
		        audioDevice3DL2.setDiffusion(attribs.diffusion);
		        audioDevice3DL2.setDensity(attribs.density);
	 	        if (debugFlag) {
			    debugPrint("     reflect coeff " +
                                        attribs.reflectionCoefficient);
			    debugPrint("     reverb coeff " +
                                        attribs.reverbCoefficient);
			    debugPrint("     decay filter " +
                                        attribs.decayFilter);
			    debugPrint("     diffusion " +
                                        attribs.diffusion);
			    debugPrint("     density " +
                                        attribs.density);
                        }
                    }

                    // Precidence for determining Reverb Delay
                    // 1) If ReverbBounds set, bounds used to calculate delay.
                    //    Default ReverbBounds is null meaning it's not defined.
                    // 2) If ReverbBounds is null, ReverbDelay used
                    //    (implitic default value is defined as 40ms)

		    // If Bounds null, use Reverb Delay
		    // else calculate Reverb Delay from Bounds
		    Bounds reverbVolume = attribs.reverbBounds;
		    if (reverbVolume == null) {
			audioDevice3D.setReverbDelay(attribs.reverbDelay);
			if (debugFlag)
			    debugPrint(
				   "     reverbDelay " + attribs.reverbDelay);
		    }
		    else {
			float  reverbDelay;
			if (reverbVolume instanceof BoundingSphere) {
			    // worst (slowest) case is if sound in center of
			    // bounding sphere
			    reverbDelay = (float)
				((2.0*((BoundingSphere)reverbVolume).radius)/
				 AuralAttributesRetained.SPEED_OF_SOUND);
			}
			else {
			    // create a bounding sphere the surrounds the bounding
			    // object then calcalate the worst case as above
			    BoundingSphere tempSphere =
				new BoundingSphere(reverbVolume);
			    reverbDelay = (float)
				((2.0 * tempSphere.radius)/
				AuralAttributesRetained.SPEED_OF_SOUND);
			}
			audioDevice3D.setReverbDelay(reverbDelay);
			if (debugFlag)
			    debugPrint("     reverbDelay " + reverbDelay);
		    }

                    // Audio device makes calculations for reverb decay
                    // using API's precidence where positive reverb order
                    // is used to clamp reverb decay time.
                    // Because of that the values are just passed along.
		    audioDevice3D.setReverbOrder(attribs.reverbOrder);
                    if (audioDevice3DL2 != null)
		        audioDevice3DL2.setDecayTime(attribs.decayTime);
		} // sync attribs
	        resetAA = true;
	    }  // attribs not null

	    else if (lastAA != null) {
	        // Even when there are no active auralAttributes, still check
	        // if a set of auralAttributes was passed to the AudioDevice,
	        // thus is now inactive and must be cleared out (set to
	        // default values).
                // Gain scale factor of 1.0 implicit by default -  this value
                // passed to AudioDevice3D as part of InitialScaleFactor value.
	        if (debugFlag)
			        debugPrint(": set updateDefaultAAs");
	        audioDevice3D.setRolloff(1.0f);
	        audioDevice3D.setReflectionCoefficient(0.0f);
	        audioDevice3D.setReverbDelay(40.0f);
	        audioDevice3D.setReverbOrder(0);
	        // Distance filter parameters
	        audioDevice3D.setDistanceFilter(
			AuralAttributesRetained.NO_FILTERING, null, null);
	        audioDevice3D.setFrequencyScaleFactor(1.0f);
	        audioDevice3D.setVelocityScaleFactor(0.0f);
                if (audioDevice3DL2 != null) {
	            audioDevice3DL2.setReverbCoefficient(0.0f);
	            audioDevice3DL2.setReflectionDelay(20.0f);
		    audioDevice3DL2.setDecayTime(1000.0f);
		    audioDevice3DL2.setDecayFilter(5000.0f);
		    audioDevice3DL2.setDiffusion(1.0f);
		    audioDevice3DL2.setDensity(1.0f);
                }

		// Any change in AuralAttrib should force an update of all
		// active sounds as passed to AudioDevice3D.
	        resetAA = true;
	    }
	    else  {
                if (debugFlag)
	        debugPrint(" updateAuralAttribs: none set or cleared");
	    }
/*
            attribs.aaDirty = false;
*/
	    lastAA = attribs;
	    auralAttribsChanged = false;
        }
    }


    void processSoundAtom(SoundSchedulerAtom soundAtom) {
	SoundRetained mirSound = soundAtom.sound;
	SoundRetained sound = mirSound.sgSound;

	// Sounds that have finished playing are not put into list
	if ((soundAtom.status == SoundSchedulerAtom.SOUND_COMPLETE) &&
	    (soundAtom.enabled != SoundSchedulerAtom.PENDING_ON )) {
	    // XXXX:/QUESTION test for immediate mode (?)

	    // Unless the sound has been re-started, there's no need
	    // to process sound the finished playing the last time thru
	    // the run() loop
	    return;  // don't need to process unless re-started
	}

	// Sound must be loaded if it hasn't been successfully loaded already
	if (soundAtom.loadStatus != SoundRetained.LOAD_COMPLETE) {
	    // should be OK for soundData to be NULL - this will unclear
	    if (debugFlag)
		debugPrint(": LOAD_PENDING - try attaching");
	    attachSoundData(soundAtom, sound.soundData, false);
	}

	// if the loadStatus is STILL NOT COMPLETE, wait to schedule
	if (soundAtom.loadStatus != SoundRetained.LOAD_COMPLETE) {
	    if (debugFlag)
		debugPrint(": not LOAD_COMPLETE yet, so bail");
	    // if sound is still not loaded, or loaded with null
	    return;  // don't process this sound now
	}

	if (resetAA)  { // explicitly force update of gain for sound
	    soundAtom.setAttribsDirtyFlag(SoundRetained.INITIAL_GAIN_DIRTY_BIT);
	}

	// Determine if sound is "active"
	//    Immediate mode sounds are always active.
	//    Sounds (both background and positional) are active only when their
	//      scheduling region intersects the viewPlatform.
	boolean intersected = false;
	if (!updateThread.active) {
	    region = null;
	    intersected = false; // force sound to be made inactive
	    if (debugFlag)
		debugPrint(": updateThread NOT active");
	}
	else if (sound.getInImmCtx()) {
	    region = null;
	    intersected = true;
	    if (debugFlag)
		debugPrint(": sound.getInImmCtx TRUE");
	}
	else {
	    if ( sound.schedulingRegion != null &&
		 mirSound.transformedRegion != null ) {
		// QUESTION: shouldn't mirror sound transformedRegion be
		//        set to null when sound node's region set null?
		if ((region instanceof BoundingSphere &&
		     mirSound.transformedRegion instanceof BoundingSphere) ||
		    (region instanceof BoundingBox &&
		     mirSound.transformedRegion instanceof BoundingBox) ||
		    (region instanceof BoundingPolytope &&
		     mirSound.transformedRegion instanceof BoundingPolytope)){
		    mirSound.transformedRegion.getWithLock(region);
		} else {
		    region = (Bounds)mirSound.transformedRegion.clone();
		}
	    } else {
		region = null;
	    }

	    if (region != null) {
		if (debugFlag)
		    debugPrint(": region is " + region );
		intersected = viewPlatform.schedSphere.intersect(region);
		if (debugFlag)
		    debugPrint("    intersection with viewPlatform is " +
			       intersected );
	    }
	    else {
		intersected = false;
		if (debugFlag)
		    debugPrint(": region is null, " +
			       "so region does NOT intersect viewPlatform");
	    }
	}

	// Get scheduling action based on sound state (flags, status)
	// Sound must be unmuted or pending unmuting and
	// either immediate mode node, or if retained
	// then intersecting active region and switch state must be on.
	if (debugFlag) {
	    debugPrint(":      intersected = " + intersected);
	    debugPrint(":      switchState = " + mirSound.switchState);
	    if (mirSound.switchState != null)
		debugPrint(":      switchOn = " + mirSound.switchState.currentSwitchOn);
	    debugPrint(":      soundAtom.muted = " + soundAtom.muted);
	}
	if ( (sound.getInImmCtx() ||
	      (intersected &&
	       mirSound.switchState != null &&
	       mirSound.switchState.currentSwitchOn) ) &&
	     (soundAtom.muted == soundAtom.UNMUTED ||
	      soundAtom.muted == soundAtom.PENDING_UNMUTE) ) {
	    if (debugFlag)
		debugPrint(": calcActiveSchedAction");
	    soundAtom.schedulingAction = soundAtom.calcActiveSchedAction();
	}
	else {
	    if (debugFlag)
		debugPrint(": calcInactiveSchedAction");
	    soundAtom.schedulingAction = soundAtom.calcInactiveSchedAction();
	}

	if (debugFlag) {
	    debugPrint(": scheduling action calculated " +
		       "as "+ soundAtom.schedulingAction);
	    debugPrint("    dirtyFlag test of LISTENER_CHANGED " +
		       testListenerFlag() );
	}

	// If state has not changed but parameters have, set
	// action to UPDATE.
	// This test includes checking that SoundScheduler dirtyFlag
	// set when Eye, Ear or ImagePlate-to-Vworld Xform changed
	// even for non-BackgroundSounds
	if ((soundAtom.schedulingAction == SoundSchedulerAtom.LEAVE_AUDIBLE) &&
	    (soundAtom.testDirtyFlags() || (testListenerFlag() &&
					    !(sound instanceof BackgroundSoundRetained)))) {
	    if (debugFlag) {
		if (testListenerFlag()) {
		    debugPrint(" testListenerFlag = " +
			       testListenerFlag());
		}
		debugPrint(": action changed from " +
			   "as LEAVE_AUDIBLE to UPDATE");
	    }
	    soundAtom.schedulingAction = SoundSchedulerAtom.UPDATE;
	}

	// Update prioritized list of sounds whose scheduling action
	// demands processing...

	// Ensure sound are not stopped while looping thru prioritized sounds
	switch (soundAtom.schedulingAction) {
	case SoundSchedulerAtom.TURN_OFF:
	    soundAtom.status = SoundSchedulerAtom.SOUND_OFF;
	    turnOff(soundAtom); // Stop sound that need to be turned off
	    if (debugFlag)
		debugPrint(": sound " + soundAtom.sampleId +
			   " action OFF results in call to stop");
	    soundAtom.schedulingAction = SoundSchedulerAtom.LEAVE_OFF;
	    break;

	case SoundSchedulerAtom.MAKE_AUDIBLE:
	case SoundSchedulerAtom.MAKE_SILENT:
	case SoundSchedulerAtom.LEAVE_AUDIBLE:
	case SoundSchedulerAtom.LEAVE_SILENT:
	case SoundSchedulerAtom.PAUSE_AUDIBLE:
	case SoundSchedulerAtom.PAUSE_SILENT:
	case SoundSchedulerAtom.RESUME_AUDIBLE:
	case SoundSchedulerAtom.RESUME_SILENT:
	case SoundSchedulerAtom.UPDATE:

	    // Test for sound finishing playing since the last
	    // thru the run() loop.
	    //
	    // test current time against endTime of sound to determine
	    // if sound is Completely finished playing
	    long currentTime = J3dClock.currentTimeMillis();
	    if (soundAtom.endTime>0 && soundAtom.endTime<=currentTime) {
		// sound's completed playing, force action
		soundAtom.schedulingAction = SoundSchedulerAtom.COMPLETE;
		if (debugFlag)
		    debugPrint(": sample complete;"+
			       " endTime = " + soundAtom.endTime +
			       ", currentTime = " + currentTime +
			       " so turned off");
		soundAtom.status = SoundSchedulerAtom.SOUND_COMPLETE;
		turnOff(soundAtom); // Stop sound in device that are complete
		if (debugFlag)
		    debugPrint(": sound "+soundAtom.sampleId+
			       " action COMPLETE results in call to stop");
	    }
	    break;

	case SoundSchedulerAtom.RESTART_AUDIBLE:
	case SoundSchedulerAtom.START_AUDIBLE:
	case SoundSchedulerAtom.RESTART_SILENT:
	case SoundSchedulerAtom.START_SILENT:
	    break;

	default:      // includes COMPLETE, DO_NOTHING
	    soundAtom.schedulingAction = SoundSchedulerAtom.DO_NOTHING;
	    break;
	} // switch

	if (debugFlag)
	    debugPrint(": final scheduling action " +
		       "set to " + soundAtom.schedulingAction);
    }


    /**
     * Determine scheduling action for each live sound
     */
    int calcSchedulingAction() {
	// Temp variables
	SoundRetained sound;
	SoundRetained mirSound;
	SoundSchedulerAtom soundAtom;
	SoundRetained jSound;
	int           nSounds = 0;
	boolean       processSound;
	// number of sounds to process including scene graph and immediate nodes
	int           numSoundsToProcess = 0;

	if (universe == null) {
	    if (debugFlag)
		debugPrint(
			   ": calcSchedulingAction: univ NULL");
	    return 0;
	}
	if (universe.soundStructure == null) {
	    if (debugFlag)
		debugPrint(
			   ": calcSchedulingAction: soundStructure NULL");
	    return 0;
	}

	// List of prioritized "live" sounds taken from universe list of sounds.
	// Maintained as an expandable array - start out with a small number of
	// elements for this array then grow the list larger if necessary...
	synchronized (prioritizedSounds) {
	    nSounds = prioritizedSounds.size();
	    if (debugFlag)
		debugPrint(
			   ": calcSchedulingAction: soundsList size = " +
			   nSounds);

	    // (Large) Loop over all switched on sounds and conditionally put
	    // these into a order prioritized list of sound.
	    // Try throw out as many sounds as we can:
	    //     Sounds finished playing (reached end before stopped)
	    //     Sounds still yet to be loaded
	    //     Positional sounds whose regions don't intersect view
	    //     Sound to be stopped
	    // Those sounds remaining are inserted into a prioritized list

	    for (int i=0; i<nSounds; i++) {
		soundAtom = prioritizedSounds.get(i);
		mirSound = soundAtom.sound;
		sound = mirSound.sgSound;
		if (debugFlag) {
		    debugPrint(" calcSchedulingAction: sound at " + sound);
		    printAtomState(soundAtom);
		}
		// First make a list of switched on live sounds
		// make sure to process turned-off sounds even if they are
		// NOT active
		processSound = false;
		if ( (!sound.source.isLive() &&
		      !sound.getInImmCtx() ) ) {
		    if (debugFlag) {
			debugPrint(" calcSchedulingAction sound " +
				   sound + " is NOT Live");
			if (mirSound.source != null) {
			    if (mirSound.source.isLive()!=sound.source.isLive())
				debugPrint(
					   " !=!=!=  sound.isLive != mirSound");
			}
		    }
		    if (soundAtom.playing ||
			(soundAtom.enabled == SoundSchedulerAtom.ON)) {
			soundAtom.setEnableState(SoundSchedulerAtom.PENDING_OFF);
			processSound = true;
			if (debugFlag)
			    debugPrint(" calcSchedulingAction !isLive: " +
				       "sound playing or ON, so set PENDING_OFF");
		    }
		    else if (soundAtom.enabled==SoundSchedulerAtom.PENDING_OFF){
			processSound = true;
			if (debugFlag)
			    debugPrint(" calcSchedulingAction !isLive: " +
				       "sound == PENDING_OFF so process");
		    }
		    else if (soundAtom.enabled==SoundSchedulerAtom.PENDING_ON) {
			soundAtom.setEnableState(SoundSchedulerAtom.OFF);
			if (debugFlag)
			    debugPrint(" calcSchedulingAction !isLive: " +
				       "sound == PENDING_ON so set OFF");
		    }
		}
		else {  // live and switched on retained node or
		    // non-retained (immediate mode) node
		    processSound = true;
		}

		if (processSound) {
		    numSoundsToProcess++;
		    if (debugFlag) {
			debugPrint(".testListenerFlag = " +
				   testListenerFlag() + "....");
			debugPrint("       contents of live sound " +
				   soundAtom.sampleId + " before processing," );
			debugPrint(" >>>>>>sound using sgSound at " + sound);
			printAtomState(soundAtom);
		    }
		    processSoundAtom(soundAtom);
		} // end of process sound
		else {
		    soundAtom.schedulingAction = SoundSchedulerAtom.DO_NOTHING;
		} // end of not process sound

	    } // end loop over all sound in soundList
	} // sync

	if (debugFlag) {
	    if (numSoundsToProcess > 0)
		debugPrint(": number of liveSounds = " + numSoundsToProcess);
	    else
		debugPrint(": number of liveSounds <= 0");
	}

	return numSoundsToProcess;
    }


    /**
     * Mute sounds that are to be played silently.
     *
     * Not all the sound in the prioritized enabled sound list
     * may be able to be played.  Due to low priority, some sounds
     * must be muted/silenced (if such an action frees up channel
     * resources) to make way for sounds with higher priority.
     * For each sound in priority list:
     *     For sounds whose actions are X_SILENT:
     *         Mute sounds to be silenced
     *         Add the number of channels used by this muted sound to
     *                 current total number of channels used
     *     For all remaining sounds (with actions other than above)
     *         The number of channels that 'would be used' to play
     *                 potentially audible sounds is compared with
     *                 the number left on the device:
     *         If this sound would use more channels than available
     *             Change it's X_AUDIBLE action to X_SILENT
     *             Mute sounds to be silenced
     *         Add the number of channels used by this sound, muted
     *                 or not, to current total number of channels used
     *
     * NOTE: requests for sounds to play beyond channel capability of
     * the audio device do NOT throw an exception when more sounds are
     * started than can be played.  Rather the unplayable sounds are
     * muted.  It is up to the AudioDevice3D implementation to determine
     * how muted/silent sounds are implememted (playing with gain zero
     * and thus using up channel resources, or stop and restarted with
     * correct offset when inactivated then re-actived.
     */
    void muteSilentSounds() {
	// Temp variables
	SoundRetained sound;
	SoundRetained mirSound;
	int           totalChannelsUsed = 0;
	SoundSchedulerAtom soundAtom;
	int           nAtoms;
	synchronized (prioritizedSounds) {
	    nAtoms = prioritizedSounds.size();
	    if (debugFlag)
		debugPrint(".muteSilentSounds(): Loop over prioritizedSounds list, " +
			   "size = " + nAtoms);
	    for (int i=0; i<nAtoms; i++) {
		soundAtom = prioritizedSounds.get(i);
		mirSound = (SoundRetained)soundAtom.sound;
		sound = mirSound.sgSound;
		int sampleId   = soundAtom.sampleId;
		int status     = soundAtom.status;

		if (debugFlag) {
		    debugPrint(":       contents of current sound " +
			       soundAtom.sampleId + " before switch on sAction" );
		    printAtomState(soundAtom);
		}

		if (soundAtom.status == SoundSchedulerAtom.SOUND_COMPLETE) {
		    continue;
		}
		if (soundAtom.schedulingAction == SoundSchedulerAtom.DO_NOTHING) {
		    continue;
		}
		if (sampleId == SoundRetained.NULL_SOUND) {
		    // skip it until next time thru calcSchedulingAction
		    continue;
		}
		if ( (soundAtom.schedulingAction == SoundSchedulerAtom.MAKE_SILENT) ||
		     (soundAtom.schedulingAction == SoundSchedulerAtom.RESTART_SILENT) ||
		     (soundAtom.schedulingAction == SoundSchedulerAtom.LEAVE_SILENT) ||
		     (soundAtom.schedulingAction == SoundSchedulerAtom.START_SILENT) ) {
		    // Mute sounds that are not already silent
		    if (status != SoundSchedulerAtom.SOUND_SILENT) {
			// old status is not already muted/silent
			audioDevice3D.muteSample(sampleId);
			if (debugFlag)
			    debugPrint(": sound " + sampleId +
				       " action is x_SILENT, sound muted");
		    }
		    // now that the exact muting state is known get the actual
		    // number of channels used by this sound and add to total
		    int numberChannels =
			audioDevice3D.getNumberOfChannelsUsed(sampleId);
		    soundAtom.numberChannels = numberChannels; // used in audio device
		    totalChannelsUsed += numberChannels;  // could return zero
		} //  scheduling is for silent sound

		else {
		    // First, test to see if the sound can play as unmuted.
		    int numberChannels =
			audioDevice3D.getNumberOfChannelsUsed(sampleId, false);
		    // Mute sounds that have too low priority
		    if ((totalChannelsUsed+numberChannels)>totalChannels) {
			if ((soundAtom.schedulingAction == SoundSchedulerAtom.MAKE_AUDIBLE) ||
			    (soundAtom.schedulingAction == SoundSchedulerAtom.LEAVE_AUDIBLE)) {
			    soundAtom.schedulingAction = SoundSchedulerAtom.MAKE_SILENT;
			}
			else if (soundAtom.schedulingAction == SoundSchedulerAtom.RESTART_AUDIBLE)
			    soundAtom.schedulingAction = SoundSchedulerAtom.RESTART_SILENT;
			else if (soundAtom.schedulingAction == SoundSchedulerAtom.START_AUDIBLE)
			    soundAtom.schedulingAction = SoundSchedulerAtom.START_SILENT;
			else if (soundAtom.schedulingAction == SoundSchedulerAtom.PAUSE_AUDIBLE)
			    soundAtom.schedulingAction = SoundSchedulerAtom.PAUSE_SILENT;
			else if (soundAtom.schedulingAction == SoundSchedulerAtom.RESUME_AUDIBLE)
			    soundAtom.schedulingAction = SoundSchedulerAtom.RESUME_SILENT;
			audioDevice3D.muteSample(sampleId);
			if (debugFlag) {
			    debugPrint(": sound " + sampleId +
				       "number of channels needed is " +
				       numberChannels);
			    debugPrint(": sound " + sampleId +
				       " action is x_AUDIBLE but " +
				       "not enough channels free (" +
				       (totalChannels - totalChannelsUsed) +
				       ") so, sound muted");
			}
		    }
		    // sound has enough channels to play
		    else if (status != SoundSchedulerAtom.SOUND_AUDIBLE) {
			// old status is not already unmuted/audible
			audioDevice3D.unmuteSample(sampleId);
			if (debugFlag)
			    debugPrint(": sound " + sampleId +
				       " action is x_AUDIBLE and channels free so, " +
				       "sound unmuted");
		    }
		    // now that the exact muting state is known (re-)get actual
		    // number of channels used by this sound and add to total
		    numberChannels =
			audioDevice3D.getNumberOfChannelsUsed(sampleId);
		    soundAtom.numberChannels = numberChannels; // used in audio device
		    totalChannelsUsed += numberChannels;
		} //  otherwise, scheduling is for potentally audible sound
		// No sound in list should have action TURN_ or LEAVE_OFF
	    } // of for loop over sounds in list
	}
    }


    void muteSilentSound(SoundSchedulerAtom soundAtom) {
	// Temp variables
	SoundRetained sound;
	SoundRetained mirSound;
	mirSound = (SoundRetained)soundAtom.sound;
	sound = mirSound.sgSound;
	int sampleId   = soundAtom.sampleId;
	int status     = soundAtom.status;

	if (status == SoundSchedulerAtom.SOUND_COMPLETE) {
	    return;
	}
	if (sampleId == SoundRetained.NULL_SOUND) {
	    return;
	}
	if (debugFlag) {
	    debugPrint(":       contents of current sound " +
		       soundAtom.sampleId + " before switch on sAction" );
	    printAtomState(soundAtom);
	}

	if ( (soundAtom.schedulingAction == SoundSchedulerAtom.MAKE_SILENT) ||
	     (soundAtom.schedulingAction == SoundSchedulerAtom.RESTART_SILENT) ||
	     (soundAtom.schedulingAction == SoundSchedulerAtom.LEAVE_SILENT) ||
	     (soundAtom.schedulingAction == SoundSchedulerAtom.START_SILENT) ) {
	    // Mute sounds that are not already silent
	    if (status != SoundSchedulerAtom.SOUND_SILENT) {
		// old status is not already muted/silent
		audioDevice3D.muteSample(sampleId);
		if (debugFlag)
		    debugPrint(": sound " + sampleId +
			       " action is x_SILENT, sound muted");
	    }
	}  //  scheduling is for silent sound
    }

    /**
     * Determine amount of time before next playing sound will be
     * is complete.
     *
     * find the atom that has the least amount of time before is
     * finished playing and return this time
     * @return length of time in millisecond until the next active sound
     * will be complete.  Returns -1 if no sounds are playing (or all are
     * complete).
     */
    long  shortestTimeToFinish() {
	long currentTime = J3dClock.currentTimeMillis();
	long shortestTime = -1L;
	SoundSchedulerAtom soundAtom;
	synchronized (prioritizedSounds) {
	    int nAtoms = prioritizedSounds.size();
	    for (int i=0; i<nAtoms; i++) {
		soundAtom = prioritizedSounds.get(i);
		if (soundAtom.status == SoundSchedulerAtom.SOUND_OFF ||
		    soundAtom.status == SoundSchedulerAtom.SOUND_COMPLETE )
		    continue;
		long endTime = soundAtom.endTime;
		if (endTime < 0)
		    // skip sounds that are to play infinitely (until stopped)
		    continue;
		if (debugFlag) {
		    if (endTime == 0)
			debugPrint(".shortestTimeToFinish: " +
				   "Internal Error - endTime 0 while sound playing");
		}
		// for all playing sounds (audible and silent) find how much
		// time is left before the sound completed playing
		long timeLeft = endTime - currentTime;
		if (debugFlag)
		    debugPrint(
			       "              shortestTimeToFinish timeLeft = " +
			       timeLeft);
		if (timeLeft < 0L) {
		    // normalize completed sounds; force to zero
		    // so no waiting occurs before scheduler re-entered
		    timeLeft = 0L;
		}
		if (shortestTime < 0L) {
		    // save first atom's time as shortest
		    shortestTime = timeLeft;
		}
		else if (timeLeft < shortestTime) {
		    shortestTime = timeLeft;
		}
	    }
	}
	if (debugFlag)
	    debugPrint(".shortestTimeToFinish returns " + shortestTime );
	return shortestTime;
    }


    /**
     * Perform the scheduling action for each prioritized sound.
     *
     * Now, finally, the scheduling action value reflects what is
     * requested and what is physically posible to perform.
     * So, for each sound in list of prioritized enabled sounds,
     * start/update sounds that are REALLY supposed to be either
     * playing audibly or playing silently.
     * @return number of active (audible and silent) sounds
     */
    int  performActions()  {
	// Temp variables
	SoundRetained sound;
	SoundRetained mirSound;
	int nAtoms;
	SoundSchedulerAtom soundAtom;
	AuralAttributesRetained attribs;
	int numActiveSounds = 0;
	int sampleId;

	synchronized (prioritizedSounds) {
	    nAtoms = prioritizedSounds.size();
	    for (int i=0; i<nAtoms; i++) {
		// XXXX: (Enhancement) Get all sound node fields here
		//          and store locally for performance
		soundAtom = prioritizedSounds.get(i);
		mirSound = soundAtom.sound;
		sound = mirSound.sgSound;
		sampleId = soundAtom.sampleId;

		if (sampleId == SoundRetained.NULL_SOUND) {
		    // skip it until next time thru calcSchedulingAction
		    continue;
		}

		// Two flags denoting that AuralAttributes have be changed and thus
		// sounds have to potentially be rendered are maintained and set in
		// updateAuralAttribs().
		resetAA = false;

		// check to see if aural attributes changed and have to be updated
		// must be done before list of sound processed so that Aural Attributes
		// that affect Sound fields can be set in AudioDevice
		// XXXX: this is not effient if auralAttribs always the same
		if (sound.getInImmCtx()) {
		    if (graphicsCtx !=null && graphicsCtx.auralAttributes !=null) {
			aaImmed = (AuralAttributesRetained)
			    (graphicsCtx.auralAttributes.retained);
			attribs = aaImmed;
		    }
		    else  {
			attribs = null;
		    }
		}
		else {
		    attribs = aaRetained;
		}
		updateAuralAttribs(attribs);

		if (debugFlag) {
		    debugPrint(":       contents of current sound " +
			       sampleId + " before start/update " );
		    printAtomState(soundAtom);
		}

		switch (soundAtom.schedulingAction) {
		case SoundSchedulerAtom.RESTART_AUDIBLE:
		    // stop sound first then fall thru to re-start
		    turnOff(soundAtom);
		case SoundSchedulerAtom.START_AUDIBLE:
		    // Pause and Resume related actions are checked when sound
		    // is to be started or restarted
		    if (soundAtom.paused == soundAtom.PENDING_PAUSE)
			pause(soundAtom);
		    if (soundAtom.paused == soundAtom.PENDING_UNPAUSE)
			unpause(soundAtom);
		    if (soundAtom.paused == soundAtom.UNPAUSED) {
			// if its unpaused, start audible sound
			soundAtom.status = SoundSchedulerAtom.SOUND_AUDIBLE;
			render(true, soundAtom, attribs);
		    }
		    else {  // sound paused
			soundAtom.status = SoundSchedulerAtom.SOUND_PAUSED;
			// start it after when the sound is not paused
			soundAtom.setEnableState(SoundSchedulerAtom.PENDING_ON);
		    }
		    numActiveSounds++;
		    break;

		case SoundSchedulerAtom.RESTART_SILENT:
		    // stop sound first then fall thru to re-start
		    turnOff(soundAtom);
		case SoundSchedulerAtom.START_SILENT:
		    // Pause and Resume related actions are checked when sound
		    // is to be started or restarted
		    if (soundAtom.paused == soundAtom.PENDING_PAUSE)
			pause(soundAtom);
		    if (soundAtom.paused == soundAtom.PENDING_UNPAUSE)
			unpause(soundAtom);
		    if (soundAtom.paused == soundAtom.UNPAUSED) {
			// if the sound is unpaused, start silent sound
			soundAtom.status = SoundSchedulerAtom.SOUND_SILENT;
			render(true, soundAtom, attribs);
		    }
		    else {  // sound paused
			soundAtom.status = SoundSchedulerAtom.SOUND_PAUSED;
			// start it after when the sound is not paused
			soundAtom.setEnableState(SoundSchedulerAtom.PENDING_ON);
		    }
		    numActiveSounds++;
		    break;

		case SoundSchedulerAtom.RESUME_AUDIBLE:
		    // pause then fall thru set make audible
		    unpause(soundAtom);
		case SoundSchedulerAtom.MAKE_AUDIBLE:
		    // change status to audible then update sound
		    soundAtom.status = SoundSchedulerAtom.SOUND_AUDIBLE;
		    render(false, soundAtom, attribs);
		    numActiveSounds++;
		    break;
		case SoundSchedulerAtom.RESUME_SILENT:
		    // pause then fall thru set make silent
		    unpause(soundAtom);
		case SoundSchedulerAtom.MAKE_SILENT:
		    // change status to silent AFTER calling render so
		    // that a currently audible sound will be muted.
		    // XXXX: why set status AFTER??
		    render(false, soundAtom, attribs);
		    soundAtom.status = SoundSchedulerAtom.SOUND_SILENT;
		    numActiveSounds++;
		    break;

		case SoundSchedulerAtom.PAUSE_AUDIBLE:
		case SoundSchedulerAtom.PAUSE_SILENT:
		    pause(soundAtom);
		    soundAtom.status = SoundSchedulerAtom.SOUND_PAUSED;
		    numActiveSounds++;
		    break;

		case SoundSchedulerAtom.UPDATE:
		    render(false, soundAtom, attribs);
		    numActiveSounds++;
		    break;

		case SoundSchedulerAtom.LEAVE_AUDIBLE:
		case SoundSchedulerAtom.LEAVE_SILENT:
		case SoundSchedulerAtom.LEAVE_PAUSED:
		    if (resetAA || soundAtom.testDirtyFlags())
			render(false, soundAtom, attribs);
		    if (debugFlag)
			debugPrint(": LEAVE_AUDIBLE or _SILENT " +
				   "seen");
		    numActiveSounds++;
		    break;

		case SoundSchedulerAtom.TURN_OFF:
		    turnOff(soundAtom);
		    break;

		case SoundSchedulerAtom.LEAVE_OFF:
		case SoundSchedulerAtom.COMPLETE:
		case SoundSchedulerAtom.DO_NOTHING:
		    break;

		default:
		    if (internalErrors)
			debugPrint(": Internal Error"+
				   " unknown action");
		    break;
		}
		// Clear atom state and attrib dirty flags
		soundAtom.clearStateDirtyFlag();
		soundAtom.clearAttribsDirtyFlag();

	    } // for sounds in priority list
	}

	// Now that aural attribute change forced each processed sounds
	// to be updated, clear this special reset aural attrubute flag
	resetAA = false;

	return numActiveSounds;
    }


    /**
     * render (start or update) the oscillator associated with this sound
     */
    void render(boolean startFlag,
		SoundSchedulerAtom soundAtom,
		AuralAttributesRetained attribs) {

	SoundRetained mirrorSound  = soundAtom.sound;
	SoundRetained sound  = mirrorSound.sgSound;
	if (debugFlag)
	    debugPrint(".render " + sound);

	if ( soundAtom.sampleId == SoundRetained.NULL_SOUND ||
	     soundAtom.soundData == null ) {
	    if (internalErrors)
		debugPrint(".render - Internal Error: " +
			   "null sample data");
	    return;
	}

	int index = soundAtom.sampleId;

	//  Depending on Mute and/or pause flags, set sound parameters
	if (startFlag) {
	    if ( (sound instanceof PointSoundRetained) ||
		 (sound instanceof ConeSoundRetained) ) {
		updateXformedParams(true, soundAtom);
	    }
	    updateSoundParams(true, soundAtom, attribs);
	    start(soundAtom);
	}
	else {
	    if (soundAtom.status == SoundSchedulerAtom.SOUND_AUDIBLE) {
		if ( (sound instanceof PointSoundRetained) ||
		     (sound instanceof ConeSoundRetained) ) {
		    updateXformedParams(false, soundAtom);
		}
		updateSoundParams(false, soundAtom, attribs);
		update(soundAtom);
	    }
	}  // if sound Audible
    } // render


    /**
     * Start the sample associated with this sound
     * Do everything necessary to start the sound:
     *     set start time
     *     the oscillator associated with this sound
     */
    void start(SoundSchedulerAtom soundAtom) {
	SoundRetained sound = soundAtom.sound.sgSound;
	int index = soundAtom.sampleId;
	int startStatus = -1;
	if (index != SoundRetained.NULL_SOUND &&
	    (startStatus = audioDevice3D.startSample(index)) >= 0) {
	    if (debugFlag)
		debugPrint(".start: " + index );
	    soundAtom.playing = true;
	    soundAtom.startTime = audioDevice3D.getStartTime(index);
	    soundAtom.calculateEndTime();
	    if (debugFlag)
		debugPrint(".start: begintime = " +
			   soundAtom.startTime + ", endtime " + soundAtom.endTime);
	}
	else {   // error returned by audio device when trying to start
	    soundAtom.startTime = 0;
	    soundAtom.endTime = 0;
	    soundAtom.playing = false;
	    if (debugFlag) {
		debugPrint(".start: error " + startStatus +
			   " returned by audioDevice3D.startSample(" + index
			   + ")" );
		debugPrint(
			   "                       start/endTime set to zero");
	    }
	}
    }


    /**
     * Exlicitly update the sound parameters associated with a sample
     */
    void update(SoundSchedulerAtom soundAtom) {
	int index = soundAtom.sampleId;

	if (index == SoundRetained.NULL_SOUND) {
	    return;
	}
	SoundRetained sound = soundAtom.sound;
	audioDevice3D.updateSample(index);
	if (debugFlag) {
	    debugPrint(".update: " + index );
	}
	soundAtom.calculateEndTime();
	if (sound instanceof PointSoundRetained ||
	    sound instanceof ConeSoundRetained) {
	    positionalSoundUpdated = true;
	}
    }


    /**
     * stop playing one specific sound node
     *
     * If setPending flag true, sound is stopped but enable state
     * is set to pending-on so that it is restarted.
     */
    void stopSound(SoundSchedulerAtom soundAtom, boolean setPending) {
	if (audioDevice3D == null)
	    return;

	if (debugFlag)
	    debugPrint(":stopSound(" + soundAtom +
		       "), enabled = " + soundAtom.enabled);
	switch (soundAtom.enabled) {
	case SoundSchedulerAtom.ON:
	    if (setPending)
		soundAtom.setEnableState(SoundSchedulerAtom.PENDING_ON);
	    else
		soundAtom.setEnableState(SoundSchedulerAtom.SOUND_OFF);
	    break;
	case SoundSchedulerAtom.PENDING_OFF:
	    soundAtom.setEnableState(SoundSchedulerAtom.SOUND_OFF);
	    break;
	case SoundSchedulerAtom.PENDING_ON:
	    if (!setPending)
		// Pending sounds to be stop from playing later
		soundAtom.setEnableState(SoundSchedulerAtom.SOUND_OFF);
	    break;
	default:
	    break;
	}
	soundAtom.status = SoundSchedulerAtom.SOUND_OFF;
	turnOff(soundAtom);
    }

    /**
     * Deactive all playing sounds
     * If the sound is continuous thendSilence it but leave it playing
     * otherwise stop sound
     */
    synchronized void deactivateAllSounds() {
	SoundRetained sound;
	SoundRetained mirSound;
	SoundSchedulerAtom soundAtom;

	if (audioDevice3D == null)
	    return;

	if (debugFlag)
	    debugPrint(".deactivateAllSounds");

	// sync this method from interrupting run() while loop
	synchronized (prioritizedSounds) {
	    if (prioritizedSounds != null) {
		int nAtoms = prioritizedSounds.size();
		if (debugFlag)
		    debugPrint("silenceAll " + nAtoms + " Sounds");
		for (int i=0; i<nAtoms; i++) {
		    // XXXX: (Enhancement) Get all sound node fields here
		    //          and store locally for performance
		    soundAtom = prioritizedSounds.get(i);
		    mirSound = soundAtom.sound;
		    sound = mirSound.sgSound;
		    if (sound.continuous) {
			// make playing sound silent
			if (debugFlag)
			    debugPrint("deactivateAll atomScheduling " +
				       "before calcInactiveSchedAction" +
				       soundAtom.schedulingAction);
			soundAtom.schedulingAction =
			    soundAtom.calcInactiveSchedAction();
			if (debugFlag)
			    debugPrint("deactivateAll atomScheduling " +
				       "after calcInactiveSchedAction" +
				       soundAtom.schedulingAction);
			// perform muting of sound
			muteSilentSound(soundAtom);  // mark sound as silence
		    }
		    else  {
			// stop playing sound but make pending on
			stopSound(soundAtom, true); // force pendingOn TRUE
			soundAtom.schedulingAction = soundAtom.LEAVE_OFF;
			if (debugFlag)
			    debugPrint("deactivateAll atomScheduling " +
				       "forced to TURN_OFF, set pending On");
		    }

		} // for sounds in priority list
	    }
	}
	performActions();
    }


    /**
     * Pause all activity playing sounds
     */
    synchronized void pauseAllSounds() {
	SoundRetained sound;
	SoundRetained mirSound;

	if (audioDevice3D == null)
	    return;

	stallThread = true;
	if (debugFlag)
	    debugPrint(".pauseAll stallThread set to true");

	// sync this method from interrupting run() while loop
	synchronized (prioritizedSounds) {
	    if (prioritizedSounds != null) {
		int nAtoms = prioritizedSounds.size();
		if (debugFlag)
		    debugPrint(":pauseAll " + nAtoms + " Sounds");
		for (int i=0; i<nAtoms; i++) {
		    // XXXX: (Enhancement) Get all sound node fields here
		    //          and store locally for performance
		    SoundSchedulerAtom soundAtom = prioritizedSounds.get(i);
		    mirSound = soundAtom.sound;
		    sound = mirSound.sgSound;

		    switch (soundAtom.enabled) {
		    case SoundSchedulerAtom.ON:
		    case SoundSchedulerAtom.PENDING_OFF:
			pause(soundAtom);
			if (debugFlag)
			    debugPrint(
				       ".pauseAllSounds PAUSE sound " + sound);
			break;
		    default:
			break;
		    }
		} // for sounds in priority list
	    }
	}
    }


    /**
     * Resume playing all paused active sounds
     */
    synchronized void resumeAllSounds() {
	SoundRetained sound;
	SoundRetained mirSound;

	if (audioDevice3D == null)
	    return;

	if (debugFlag)
	    debugPrint(".resumeAll stallThread set to true");

	// sync this method from interrupting run() while loop
	synchronized (prioritizedSounds) {
	    if (prioritizedSounds != null) {
		int nAtoms = prioritizedSounds.size();
		if (debugFlag)
		    debugPrint(": resumeAll " + nAtoms + " Sounds ");

		for (int i=0; i<nAtoms; i++) {
		    // XXXX: (Enhancement) Get all sound node fields here
		    //          and store locally for performance
		    SoundSchedulerAtom soundAtom  = prioritizedSounds.get(i);
		    mirSound = soundAtom.sound;
		    sound = mirSound.sgSound;

		    switch (soundAtom.enabled) {
		    case SoundSchedulerAtom.ON:
		    case SoundSchedulerAtom.PENDING_OFF:
			unpause(soundAtom);
			if (debugFlag)
			    debugPrint(".resumeAll - sound = " + sound);
			break;
		    default:
			break;
		    }
		} // for sounds in priority list
	    }
	}
	stallThread = false;
    }

    /**
     * Stop all activity playing sounds
     */
    synchronized void stopAllSounds() {
	stopAllSounds(false);
    }

    synchronized void stopAllSounds(boolean setPlayingSoundsPending) {
	// QUESTION: how can I assure that all sounds on device
	//     are stopped before thread paused/shutdown
	if (debugFlag)
	    debugPrint(".stopAllSounds entered");
	SoundRetained sound;
	SoundRetained mirSound;

	if (audioDevice3D == null)
	    return;

	if (lastEventReceived == WindowEvent.WINDOW_ICONIFIED) {
	    return;  // leave sounds playing
	}

	// sync this method from interrupting run() while loop
	synchronized (prioritizedSounds) {
	    if (prioritizedSounds != null) {
		int nAtoms = prioritizedSounds.size();
		if (debugFlag)
		    debugPrint(": stopAll " + nAtoms + " Sounds ");

		for (int i=0; i<nAtoms; i++) {
		    // XXXX: (Enhancement) Get all sound node fields here
		    //          and store locally for performance
		    SoundSchedulerAtom soundAtom = prioritizedSounds.get(i);
		    if (debugFlag)
			debugPrint("      stop(" + soundAtom + ")");
		    // stop playing Sound  - optionally set pending enabled
		    stopSound(soundAtom, setPlayingSoundsPending);
		} // for sounds in priority list

		// QUESTION: - removed the code that empties out prioritized
		//      sound atom list.  Are there cases when core calling
		//      StopAllSounds expects sounds to be cleared??
	    }
	}
	if (debugFlag)
	    debugPrint(".stopAllSounds exited");
    }

	    // XXXX: Mute All Sounds, complementary to Stop All Sounds
	    //     "should return from run loop - but simply WAIT until sounds
	    //     are unmuted. " ???


    /**
     * pause the sample associated with this sound
     */
    void pause(SoundSchedulerAtom soundAtom) {
	if (soundAtom.sampleId == SoundRetained.NULL_SOUND)
	    return;
	// Ensure sound are not modified while looping thru prioritized sounds
	if (debugFlag)
	    debugPrint(".pause");
	audioDevice3D.pauseSample(soundAtom.sampleId);
	soundAtom.setPauseState(soundAtom.PAUSED);
    }

    void unpause(SoundSchedulerAtom soundAtom) {
	if (soundAtom.sampleId == SoundRetained.NULL_SOUND)
	    return;
	if (debugFlag)
	    debugPrint(".unpause");
	audioDevice3D.unpauseSample(soundAtom.sampleId);
	soundAtom.setPauseState(soundAtom.UNPAUSED);
    }

    /**
     * stop the sample associated with this sound
     */
    void turnOff(SoundSchedulerAtom soundAtom) {
	// Ensure sound are not stopped while looping thru prioritized sounds
	if (soundAtom.sampleId == SoundRetained.NULL_SOUND)
	    return;
	if (debugFlag)
	    debugPrint(".turnOff");
	if (audioDevice3D.stopSample(soundAtom.sampleId) < 0) {
	    if (internalErrors) {
		debugPrint("Internal Error: stop sample error");
	    }
	}
	soundAtom.playing = false;
	soundAtom.startTime = 0;
	soundAtom.endTime = 0;

    }


    /**
     * Update VirtualWorld local transform, sound position and direction.
     *
     * This is done dynamically from PointSoundRetained as these fields
     * are updated (when soundAtom.status is AUDIBLE or SILENT), or by this.
     * render() method when sound is started (sound.enabled is true).
     *
     * This method should only be called if mirror sound is a Point or
     * ConeSound.
     *
     * Important: pre-transformed position and direction sent to AudioDevice.
     */
    void updateXformedParams(boolean updateAll, SoundSchedulerAtom soundAtom) {
	PointSoundRetained mirrorPtSound  = (PointSoundRetained)soundAtom.sound;
	PointSoundRetained ptSound = (PointSoundRetained)mirrorPtSound.sgSound;
	int index = soundAtom.sampleId;
	if (index == SoundRetained.NULL_SOUND)
	    return;
	PointSoundRetained ps = (PointSoundRetained)mirrorPtSound;

	// Set Transform

	/*
	// XXXX: per sound tranforms can now be passed to AudioDevice
	//     modify and execute the code below

	//     MoveAppBoundingLeaf > ~/Current/MoveAppBoundingLeaf.outted,
	//     instead transformed position and direction
	//     points/vectors will be passed to AudioDevice directly.

	//     vvvvvvvvvvvvvvvvvvvvvvvvvvv
		if (updateAll || soundAtom.testDirtyFlag(SoundRetained.XFORM_DIRTY_BIT){
		    Transform3D xform    = new Transform3D();
		    ps.trans.getWithLock(xform);
		    if (debugFlag) {
			debugPrint(".updateXformedParams " +
				   "setVworldXfrm for ps @ " + ps + ":");
			debugPrint("           xformPosition " +
				    ps.xformPosition.x + ", " +
				    ps.xformPosition.y + ", " +
				    ps.xformPosition.z );
			debugPrint("           column-major transform ");
			debugPrint("               " +
				    xform.mat[0]+", "  + xform.mat[1]+", "+
				    xform.mat[2]+", "  + xform.mat[3]);
			debugPrint("               " +
				    xform.mat[4]+", "  + xform.mat[5]+", "+
				    xform.mat[6]+", "  + xform.mat[7]);
			debugPrint("               " +
				    xform.mat[8]+", "  + xform.mat[9]+", "+
				    xform.mat[10]+", " + xform.mat[11]);
			debugPrint("               " +
				    xform.mat[12]+", " + xform.mat[13]+", "+
				    xform.mat[14]+", " + xform.mat[15]);
		    }
		    audioDevice3D.setVworldXfrm(index, xform);
		    soundAtom.clearStateDirtyFlag( SoundRetained.XFORM_DIRTY_BIT);
	// XXXX: make sure position and direction are already transformed and stored
	//     into xformXxxxxxx fields.
		}
	//      ^^^^^^^^^^^^^^^^^^^^^
	*/

	// Set Position
	if (updateAll || testListenerFlag() ||
	    soundAtom.testDirtyFlag(soundAtom.attribsDirty,
				    SoundRetained.POSITION_DIRTY_BIT) ||
	    soundAtom.testDirtyFlag(soundAtom.stateDirty,
				    SoundRetained.XFORM_DIRTY_BIT)   )
	    {
		Point3f     xformLocation = new Point3f();
		mirrorPtSound.getXformPosition(xformLocation);
		Point3d positionD = new Point3d(xformLocation);
		if (debugFlag)
		    debugPrint("xform'd Position: ("+positionD.x+", "+
			       positionD.y+", "+ positionD.z+")" );
		audioDevice3D.setPosition(index, positionD);
	    }

	// Set Direction
	if (mirrorPtSound instanceof ConeSoundRetained) {
	    ConeSoundRetained cn = (ConeSoundRetained)mirrorPtSound;
	    ConeSoundRetained cnSound = (ConeSoundRetained)mirrorPtSound.sgSound;
	    if (updateAll ||
		// XXXX: test for XFORM_DIRTY only in for 1.2
		soundAtom.testDirtyFlag(soundAtom.attribsDirty,
					(SoundRetained.DIRECTION_DIRTY_BIT |
					 SoundRetained.XFORM_DIRTY_BIT) ) ) {

		Vector3f    xformDirection = new Vector3f();
		cn.getXformDirection(xformDirection);
		Vector3d directionD = new Vector3d(xformDirection);
		audioDevice3D.setDirection(index, directionD);
	    }
	}
    }


    void updateSoundParams(boolean updateAll, SoundSchedulerAtom soundAtom,
			   AuralAttributesRetained attribs) {

	SoundRetained mirrorSound = soundAtom.sound;
	SoundRetained sound  = mirrorSound.sgSound;
	int index = soundAtom.sampleId;
	int arraySize;

	if (index == SoundRetained.NULL_SOUND)
	    return;
	if (debugFlag)
	    debugPrint(".updateSoundParams(dirytFlags=" +
		       soundAtom.attribsDirty + ", " + soundAtom.stateDirty + ")");

	// since the sound is audible, make sure that the parameter for
	// this sound are up-to-date.
	if (updateAll || soundAtom.testDirtyFlag(
		soundAtom.attribsDirty, SoundRetained.INITIAL_GAIN_DIRTY_BIT)) {

	    if (attribs != null) {
		audioDevice3D.setSampleGain(index,
			(sound.initialGain * attribs.attributeGain));
	    }
	    else  {
		audioDevice3D.setSampleGain(index, sound.initialGain);
	    }
	}

	if (updateAll || soundAtom.testDirtyFlag(
		soundAtom.attribsDirty, SoundRetained.LOOP_COUNT_DIRTY_BIT)) {
	    if (debugFlag)
		debugPrint(" audioDevice.setLoop(" + sound.loopCount +
			   ") called");
	    audioDevice3D.setLoop(index, sound.loopCount);
	}

	if (updateAll || soundAtom.testDirtyFlag(
		soundAtom.attribsDirty, SoundRetained.RATE_DIRTY_BIT)) {
	    if (audioDevice3DL2 != null) {
		if (debugFlag)
		    debugPrint(" audioDevice.setRateScaleFactor(" +
			       sound.rate + ") called");
		audioDevice3DL2.setRateScaleFactor(index, sound.rate);
	    }
	}

	if (updateAll || soundAtom.testDirtyFlag(
		soundAtom.attribsDirty, SoundRetained.DISTANCE_GAIN_DIRTY_BIT)){
	    if (sound instanceof ConeSoundRetained) {
		ConeSoundRetained cnSound = (ConeSoundRetained)sound;

		// set distance attenuation
		arraySize = cnSound.getDistanceGainLength();
		if (arraySize == 0) {
		    // send default
		    audioDevice3D.setDistanceGain(index, null, null, null, null);
		}
		else {
		    Point2f[] attenuation = new Point2f[arraySize];
		    Point2f[] backAttenuation = new Point2f[arraySize];
		    for (int i=0; i< arraySize; i++) {
			attenuation[i] = new Point2f();
			backAttenuation[i] = new Point2f();
		    }
		    cnSound.getDistanceGain(attenuation, backAttenuation);
		    double[] frontDistance = new double[arraySize];
		    float[] frontGain = new float[arraySize];
		    double[] backDistance = new double[arraySize];
		    float[] backGain = new float[arraySize];
		    for (int i=0; i< arraySize; i++) {
			frontDistance[i] = attenuation[i].x;
			frontGain[i] = attenuation[i].y;
			backDistance[i] = backAttenuation[i].x;
			backGain[i] = backAttenuation[i].y;
		    }
		    audioDevice3D.setDistanceGain(index,
			    frontDistance, frontGain, backDistance, backGain);
		}
	    }  // ConeSound distanceGain
	    else if (sound instanceof PointSoundRetained) {
		PointSoundRetained ptSound = (PointSoundRetained)sound;

		// set distance attenuation
		arraySize = ptSound.getDistanceGainLength();
		if (arraySize == 0) {
		    // send default
		    audioDevice3D.setDistanceGain(index, null, null, null, null);
		}
		else {
		    Point2f[] attenuation = new Point2f[arraySize];
		    for (int i=0; i< arraySize; i++)
			attenuation[i] = new Point2f();
		    ptSound.getDistanceGain(attenuation);
		    double[] frontDistance = new double[arraySize];
		    float[] frontGain = new float[arraySize];
		    for (int i=0; i< arraySize; i++) {
			frontDistance[i] = attenuation[i].x;
			frontGain[i] = attenuation[i].y;
		    }
		    audioDevice3D.setDistanceGain(index, frontDistance,
			  frontGain, null, null);
		}
	    }  // PointSound distanceGain
	}

	if ((sound instanceof ConeSoundRetained) &&
	    (updateAll || soundAtom.testDirtyFlag(soundAtom.attribsDirty,
		    SoundRetained.ANGULAR_ATTENUATION_DIRTY_BIT)) ) {

	    // set angular attenuation
	    ConeSoundRetained cnSound = (ConeSoundRetained)sound;
	    arraySize = cnSound.getAngularAttenuationLength();
	    if (arraySize == 0) {
		// send default
		double[] angle = new double[2];
		float[] scaleFactor = new float[2];
		angle[0] = 0.0;
		angle[1] = (Math.PI)/2.0;
		scaleFactor[0] = 1.0f;
		scaleFactor[1] = 0.0f;
		audioDevice3D.setAngularAttenuation(index,
						    cnSound.NO_FILTERING,
						    angle, scaleFactor, null);
	    }
	    else {
		Point3f[] attenuation = new Point3f[arraySize];
		for (int i=0; i< arraySize; i++) {
		    attenuation[i] = new Point3f();
		}
		cnSound.getAngularAttenuation(attenuation);
		double[] angle = new double[arraySize];
		float[] scaleFactor = new float[arraySize];
		float[] cutoff = new float[arraySize];
		for (int i=0; i< arraySize; i++) {
		    angle[i] = attenuation[i].x;
		    scaleFactor[i] = attenuation[i].y;
		    cutoff[i] = attenuation[i].z;
		}
		audioDevice3D.setAngularAttenuation(index,
						    cnSound.filterType,
						    angle, scaleFactor, cutoff);
	    }
	}
    }


    /**
     * Check (and set if necessary) AudioDevice3D field
     */
    boolean checkAudioDevice3D() {
	if (universe != null) {
	    if (universe.currentView != null)
		if (universe.currentView.physicalEnvironment != null) {
		    audioDevice = universe.currentView.physicalEnvironment.audioDevice;
		    if (audioDevice != null)  {
			if (audioDevice instanceof AudioDevice3DL2) {
			    audioDevice3DL2 = (AudioDevice3DL2)audioDevice;
			}
			if (audioDevice instanceof AudioDevice3D) {
			    audioDevice3D = (AudioDevice3D)audioDevice;
			}
			else  { // audioDevice is only an instance of AudioDevice
			    if (internalErrors)
				debugPrint("AudioDevice implementation not supported");
			    // audioDevice3D should already be null
			}
		    }
		    else {
			// if audioDevice is null, clear extended class fields
			audioDevice3DL2 = null;
			audioDevice3D = null;
		    }
		}
	}
	if (audioDevice3D == null)
	    return false;

	if (audioDevice3D.getTotalChannels() == 0)
	    return false;  // can not render sounds on AudioEngine that has no channels

	return true;
    }


    /**
     * Clears the fields associated with sample data for this sound.
     * Assumes soundAtom is non-null, and that non-null atom
     * would have non-null sound field.
     */
    void clearSoundData(SoundSchedulerAtom soundAtom) {
	if (checkAudioDevice3D() &&
	    soundAtom.sampleId != SoundRetained.NULL_SOUND) {
	    stopSound(soundAtom, false); // force stop of playing sound
	    // Unload sound data from AudioDevice
	    audioDevice3D.clearSound(soundAtom.sampleId);
	}

	soundAtom.sampleId = SoundRetained.NULL_SOUND;
	// set load state into atom
	soundAtom.loadStatus = SoundRetained.LOAD_NULL;
	// NOTE: setting node load status not 1-to-1 w/actual load;
	// this is incorrect
	SoundRetained sound = soundAtom.sound;
	soundAtom.loadStatus = SoundRetained.LOAD_NULL;
	soundAtom.soundData = null;
	sound.changeAtomList(soundAtom, SoundRetained.LOAD_NULL);
    }


    /**
     * Attempts to load sound data for a particular sound source onto
     * the chosen/initialized audio device
     * If this called, it is assumed that SoundRetained.audioDevice is
     * NOT null.
     * If an error in loading occurs (an exception is caught,...)
     * an error is printed out to stderr - an exception is not thrown.
     * @param soundData descrition of sound source data
     */
    // QUESTION: should this method be synchronized?
    void attachSoundData(SoundSchedulerAtom soundAtom,
			 MediaContainer soundData, boolean forceReload) {

	if (!forceReload && (soundAtom.soundData == soundData)) {
	    return;
	}
	SoundRetained sound = soundAtom.sound.sgSound;
	if (!checkAudioDevice3D()) {
	    if (debugFlag)
		debugPrint(".attachSoundData audioDevice3D null");
	    soundAtom.loadStatus = SoundRetained.LOAD_PENDING;
	    sound.changeAtomList(soundAtom, SoundRetained.LOAD_PENDING);
	    return;
	}
	if (soundAtom.soundData != null) {
	    // clear sound data field for view specific atom NOT sound node
	    clearSoundData(soundAtom);
	    if (soundData == null)  {
		if (debugFlag)
		    debugPrint(".attachSoundData with null soundData");
		return;
	    }
	}

	URL url = ((MediaContainerRetained)sound.soundData.retained).url;
	String path = ((MediaContainerRetained)sound.soundData.retained).urlString;
	InputStream stream = ((MediaContainerRetained)sound.soundData.retained).inputStream;
	if (url == null && path == null && stream == null) {
	    if (debugFlag)
		debugPrint(".attachSoundData with null soundData");
	    // clear non-null sample associated with this soundData
	    if (soundAtom.sampleId != SoundRetained.NULL_SOUND) {
		clearSoundData(soundAtom);
	    }
	    return;
	}

	int id;
	if (sound instanceof ConeSoundRetained)
	    sound.soundType = AudioDevice3D.CONE_SOUND;
	else if (sound instanceof PointSoundRetained)
	    sound.soundType = AudioDevice3D.POINT_SOUND;
	else
	    sound.soundType = AudioDevice3D.BACKGROUND_SOUND;
	if (debugFlag) {
	    debugPrint(".attachSoundData soundType = " + sound.soundType);
	    debugPrint(".attachSoundData this is = " + sound);
	}

	// Clone the MediaContainer associated with this node and
	// set the capability bits for this clone to allow access to
	// all fields; this copy is passed to the audioDevice.
	// As the fields of the MediaContainer expands, this code must
	// be appended.
	MediaContainer cloneMediaContainer = new MediaContainer();
	cloneMediaContainer.duplicateAttributes(soundData, true);
	cloneMediaContainer.setCapability(MediaContainer.ALLOW_CACHE_READ);
	cloneMediaContainer.setCapability(MediaContainer.ALLOW_URL_READ);

	id = audioDevice3D.prepareSound(sound.soundType, cloneMediaContainer);
	if (debugFlag)
	    debugPrint(".attachSoundData prepareSound returned " + id);

	if (id == SoundRetained.NULL_SOUND) {
	    soundAtom.loadStatus = SoundRetained.LOAD_FAILED;
	    // NOTE: setting node load status not 1-to-1 with actual load;
	    // this is incorrect
	    sound.changeAtomList(soundAtom, SoundRetained.LOAD_FAILED);
	    //System.err.println(path + ": "+ J3dI18N.getString("SoundRetained1"));
	}
	else {
	    if (debugFlag)
		debugPrint(".attachSoundData - sampleId set");
	    soundAtom.sampleId = id;

	    // For now loopLength=sampleLength, loop points not supported
	    long duration = audioDevice3D.getSampleDuration(id);
	    soundAtom.sampleLength = duration;
	    soundAtom.loopLength = soundAtom.sampleLength;

	    // XXXX: for most this will be 0 but not all
	    soundAtom.loopStartOffset = 0;
	    soundAtom.attackLength = 0;    // portion of sample before loop section
	    soundAtom.releaseLength = 0;   // portion of sample after loop section
	    soundAtom.loadStatus = SoundRetained.LOAD_COMPLETE;
	    soundAtom.soundData = soundData;
	    sound.changeAtomList(soundAtom, SoundRetained.LOAD_COMPLETE);
	    if (debugFlag)
		debugPrint(" attachSoundData; index = "+soundAtom.sampleId);
	}
    }


    SoundSchedulerAtom findSoundAtom(SoundRetained node, int nthInstance) {
	// find nth sound atom in the list of prioritized sounds that
	// references this sound node
	// nthInstance=1 would look for first instance
	if (node == null)
	    return null;
	SoundSchedulerAtom returnAtom = null;
	synchronized (prioritizedSounds) {
	    if (!prioritizedSounds.isEmpty()) {
		SoundSchedulerAtom soundAtom = null;
		int atomFound = 0;
		// find sound in list and remove it
		int arrSize = prioritizedSounds.size();
		for (int index=0; index<arrSize; index++) {
		    soundAtom = prioritizedSounds.get(index);
		    if (soundAtom.sound == null)
			continue;
		    // soundAtom.sound is address of mirror sound not org node
		    if (soundAtom.sound.sgSound == node) {
			atomFound++;
			// orginal app node pass into method
			// QUESTION: is mirror node still correct?
			// XXXX: ensure only mirror nodes passed into method
			if (atomFound == nthInstance) {
			    returnAtom = soundAtom;
			    break;
			}
		    }
		    else if (soundAtom.sound.sgSound == node.sgSound)  {
			atomFound++;
			// store potentially new mirror sound into soundAtom
			soundAtom.sound = node;
			if (atomFound == nthInstance) {
			    returnAtom = soundAtom;
			    break;
			}
		    }
		}
	    }
	}
	return returnAtom;
    }


    /**
     * 'Dirty' flag == listenerUpdated flag
     * The ambiguous name 'dirtyFlag' is a legacy from when there was only a
     * single dirty flag set by Core yet tested and cleared in this scheduler.
     * These methods specifically set/test/clear the local listenerUpdated flag.
     */
    // Called by CanvasViewCache when listener parameter(s) changes
    void setListenerFlag(int flag) {
	listenerUpdated |= flag;
    }

    void clearListenerFlag() {
	listenerUpdated = 0x0;
    }

    boolean testListenerFlag() {
	// Test if any bits are on
	if (listenerUpdated > 0)
	    return true;
	else
	    return false;
    }

    /**
     * set dirty flags associated with SoundSchedulerAtom
     */
    void setAttribsDirtyFlag(SoundRetained node, int dirtyFlag) {
	if (debugFlag)
	    debugPrint(".setAttribsDirtyFlag " + node );
	// find sound atom that references this sound node
	SoundSchedulerAtom soundAtom = null;
	for (int i=1; ;i++) {
	    soundAtom = findSoundAtom(node, i);
	    if (soundAtom == null)
		break;
	    soundAtom.setAttribsDirtyFlag(dirtyFlag);
	}
    }

    void setStateDirtyFlag(SoundRetained node, int dirtyFlag) {
	if (debugFlag)
	    debugPrint(".setStateDirtyFlag " + node );
	// find sound atom that references this sound node
	SoundSchedulerAtom soundAtom = null;
	for (int i=1; ;i++) {
	    soundAtom = findSoundAtom(node, i);
	    if (soundAtom == null)
		break;
	    soundAtom.setStateDirtyFlag(dirtyFlag);
	}
    }


    void printAtomState(SoundSchedulerAtom atom) {
	SoundRetained sound = atom.sound.sgSound;
	debugPrint("                  this atom = " + atom + "       ");
	debugPrint("                 references sound = " + sound + "       ");
	debugPrint("                 enabled " + atom.enabled);
	debugPrint("                 status " + atom.status);
	debugPrint("                 activated " + atom.activated);
	debugPrint("                 released " + sound.release);
	debugPrint("                 continuous " + sound.continuous);
	debugPrint("                 scheduling " + atom.schedulingAction);
    }

    // Debug print mechanism for Sound nodes

    static final boolean debugFlag = false;
    static final boolean internalErrors = false;

    void debugPrint(String message) {
	if (debugFlag)
	    System.err.println("SS."+message);
    }

    void processViewSpecificGroupChanged(J3dMessage m) {
	int component = ((Integer)m.args[0]).intValue();
	Object[] objAry = (Object[])m.args[1];
	if (((component & ViewSpecificGroupRetained.ADD_VIEW) != 0) ||
	    ((component & ViewSpecificGroupRetained.SET_VIEW) != 0)) {
	    int i;
	    Object obj;
	    View v = (View)objAry[0];
	    ArrayList leafList = (ArrayList)objAry[2];
	    // View being added is this view
	    if (v == view) {
		int size = leafList.size();
		for (i = 0; i < size; i++) {
		    obj =  leafList.get(i);
		    if (obj instanceof SoundRetained) {
			nRetainedSounds++;
			addSound((SoundRetained) obj);
		    }
		    else if (obj instanceof SoundscapeRetained) {
			auralAttribsChanged = true;
		    }
		}

	    }

	}
	if (((component & ViewSpecificGroupRetained.REMOVE_VIEW) != 0)||
	    ((component & ViewSpecificGroupRetained.SET_VIEW) != 0)) {
	    int i;
	    Object obj;
	    ArrayList leafList;
	    View v;

	    if ((component & ViewSpecificGroupRetained.REMOVE_VIEW) != 0) {
		v = (View)objAry[0];
		leafList = (ArrayList)objAry[2];
	    }
	    else {
		v = (View)objAry[4];
		leafList = (ArrayList)objAry[6];
	    }
	    if (v == view) {
		int size = leafList.size();
		for (i = 0; i < size; i++) {
		    obj =  leafList.get(i);
		    if (obj instanceof SoundRetained) {
			SoundSchedulerAtom soundAtom = null;
			for (int arrIndx=1; ;arrIndx++) {
			    soundAtom = findSoundAtom((SoundRetained)obj,
							      arrIndx);
			    if (soundAtom == null)
				break;
			    stopSound(soundAtom, false);
			}
		    }
		    else if (obj instanceof SoundscapeRetained) {
			auralAttribsChanged = true;
		    }
		}
	    }
	}

    }

    void processBoundingLeafChanged(J3dMessage m) {
	// Notify all users of this bounding leaf, it may
	// result in the re-evaluation of the lights/fogs/backgrounds
	Object[] users = (Object[])(m.args[3]);
	int i;

	for (i = 0; i < users.length; i++) {
	    LeafRetained leaf = (LeafRetained)users[i];
	    if  (leaf instanceof SoundRetained && universe.soundStructure.isSoundScopedToView(leaf, view)) {
		auralAttribsChanged = true;
	    }
	    else if (leaf instanceof SoundscapeRetained && universe.soundStructure.isSoundscapeScopedToView(leaf, view)){
		auralAttribsChanged = true;
	    }
	}
    }

    @Override
    void cleanup() {
	// clean up any messages that are queued up, since they are
	// irrelevant
	//	clearMessages();
    }
}
