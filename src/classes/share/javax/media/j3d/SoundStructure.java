/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.util.*;

/**
 * A sound structure is a object that organizes Sounds and
 * soundscapes.
 * This structure parallels the RenderingEnv structure and
 * is used for sounds
 */

class SoundStructure extends J3dStructure {
    /**
     * The list of Sound nodes
     */
    UnorderList nonViewScopedSounds = new UnorderList(SoundRetained.class);
    HashMap viewScopedSounds = new HashMap();

    /**
     * The list of Soundscapes
     */
    UnorderList nonViewScopedSoundscapes = new UnorderList(SoundscapeRetained.class);
    HashMap viewScopedSoundscapes = new HashMap();

    /**
     * The list of view platforms
     */  
    UnorderList viewPlatforms = new UnorderList(ViewPlatformRetained.class);

    /**
     * A bounds used for getting a view platform scheduling BoundingSphere
     */  
    BoundingSphere tempSphere = new BoundingSphere();
    BoundingSphere vpsphere = new BoundingSphere();

    // ArrayList of leafRetained object whose mirrorObjects
    // should be updated
    ArrayList objList = new ArrayList();

    // ArrayList of leafRetained object whose boundingleaf xform
    // should be updated
    ArrayList xformChangeList = new ArrayList();

   // ArrayList of switches that have changed
    ArrayList switchChangeLeafNodes = new ArrayList();
    ArrayList switchChangeLeafMasks = new ArrayList();

    // variables for processing transform messages
    boolean transformMsg = false;
    UpdateTargets targets = null;

    /**
     * This constructor does nothing
     */
    SoundStructure(VirtualUniverse u) {
	super(u, J3dThread.UPDATE_SOUND);
        if (debugFlag)
            debugPrint("SoundStructure constructed");
    }

    void processMessages(long referenceTime) {
	J3dMessage messages[] = getMessages(referenceTime);
	int nMsg = getNumMessage();
	J3dMessage m;	

	if (nMsg <= 0) {
	    return;
	}


	for (int i=0; i < nMsg; i++) {
	    m = messages[i];

	    switch (m.type) {
	    case J3dMessage.INSERT_NODES :
		// Prioritize retained and non-retained sounds for this view
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
	    case J3dMessage.SOUNDSCAPE_CHANGED:
	    case J3dMessage.AURALATTRIBUTES_CHANGED:
		// XXXX: this needs to be changed
		changeNodeAttrib(m);
		break;
	    case J3dMessage.TRANSFORM_CHANGED:
		transformMsg = true;
		break;
	    case J3dMessage.SWITCH_CHANGED:
                // This method isn't implemented yet.
                // processSwitchChanged(m);
		// may need to process dirty switched-on transform
		if (universe.transformStructure.getLazyUpdate()) {
		    transformMsg = true;
		}
		break;
	    case J3dMessage.VIEWSPECIFICGROUP_CHANGED:
		updateViewSpecificGroupChanged(m);
		break;
	    // XXXX: case J3dMessage.BOUNDINGLEAF_CHANGED
	    }

	    /*
	    // NOTE: this should already be handled by including/ORing
	    //       SOUND_SCHEDULER in targetThread for these message types!!
	    // Dispatch a message about a sound change
	    ViewPlatformRetained vpLists[] = (ViewPlatformRetained [])
		viewPlatforms.toArray(false);
		   
	    // QUESTION: can I just use this message to pass to all the Sound Bins
	    for (int k=viewPlatforms.arraySize()- 1; k>=0; k--) {
		View[] views = vpLists[k].getViewList();
		for (int j=(views.length-1); j>=0; j--) {
		    View v = (View)(views[j]);
		    m.view = v;
		    VirtualUniverse.mc.processMessage(m);
		}
	    }
	    */
	    m.decRefcount();
	}
	if (transformMsg) {
	    targets = universe.transformStructure.getTargetList();
	    updateTransformChange(targets, referenceTime);
	    transformMsg = false;
	    targets = null;
	}
	
	Arrays.fill(messages, 0, nMsg, null);
    }

    void insertNodes(J3dMessage m) {
	Object[] nodes = (Object[])m.args[0];
	ArrayList viewScopedNodes = (ArrayList)m.args[3];
	ArrayList scopedNodesViewList = (ArrayList)m.args[4];
	Object node;

	for (int i=0; i<nodes.length; i++) {
	    node = (Object) nodes[i];
            if (node instanceof SoundRetained) {
                addNonScopedSound((SoundRetained) node);
            }
            if (node instanceof SoundscapeRetained) {
                addNonSoundscape((SoundscapeRetained) node);
            }
	}
	// Handle ViewScoped Nodes
	if (viewScopedNodes != null) {
	    int size = viewScopedNodes.size();
	    int vlsize;
	    for (int i = 0; i < size; i++) {
		node = (NodeRetained)viewScopedNodes.get(i);
		ArrayList vl = (ArrayList) scopedNodesViewList.get(i);
		int vsize = vl.size();
		if (node instanceof SoundRetained) {
		    ((SoundRetained)node).isViewScoped = true;
		    for (int k = 0; k < vsize; k++) {
			View view = (View) vl.get(k);
			addScopedSound((SoundRetained) node, view);
		    }
		}
		else if (node instanceof SoundscapeRetained) {
		    ((SoundscapeRetained)node).isViewScoped = true;
		    for (int k = 0; k < vsize; k++) {
			View view = (View) vl.get(k);
			addScopedSoundscape((SoundscapeRetained) node, view);
		    }
		}
	    }
	}
	    /*
	    // XXXX:
            if (node instanceof AuralAttributesRetained) {
            }
            else if (node instanceof ViewPlatformRetained) {
                addViewPlatform((ViewPlatformRetained) node);
            }
	    */
    }


    /**
     * Add sound to sounds list.
     */ 
    void addScopedSound(SoundRetained mirSound, View view) {
        if (debugFlag)
            debugPrint("SoundStructure.addSound()");
	ArrayList l = (ArrayList) viewScopedSounds.get(view);
        if (l == null) {
	    l = new ArrayList();
	    viewScopedSounds.put(view, l);
        }
	l.add(mirSound);
    } // end addSound()

    void addNonScopedSound(SoundRetained mirSound) {
        if (debugFlag)
            debugPrint("SoundStructure.addSound()");
        nonViewScopedSounds.add(mirSound);
    } // end addSound()

    
    void addScopedSoundscape(SoundscapeRetained soundscape, View view) {
        if (debugFlag)
            debugPrint("SoundStructure.addSoundscape()");
	ArrayList l = (ArrayList) viewScopedSoundscapes.get(view);
        if (l == null) {
	    l = new ArrayList();
	    viewScopedSoundscapes.put(view, l);
        }
	l.add(soundscape);
    }

    void addNonSoundscape(SoundscapeRetained soundscape) {
        if (debugFlag)
            debugPrint("SoundStructure.addSoundscape()");
        nonViewScopedSoundscapes.add(soundscape);
    }

    
    void removeNodes(J3dMessage m) {
	Object[] nodes = (Object[])m.args[0];
	ArrayList viewScopedNodes = (ArrayList)m.args[3];
	ArrayList scopedNodesViewList = (ArrayList)m.args[4];
	Object node;

	for (int i=0; i<nodes.length; i++) {
	    node = (Object) nodes[i];
            if (node instanceof SoundRetained) {
                deleteNonScopedSound((SoundRetained) node);
            }
            if (node instanceof SoundscapeRetained) {
                deleteNonScopedSoundscape((SoundscapeRetained) node);
            }
        }
	// Handle ViewScoped Nodes
	if (viewScopedNodes != null) {
	    int size = viewScopedNodes.size();
	    int vlsize;
	    for (int i = 0; i < size; i++) {
		node = (NodeRetained)viewScopedNodes.get(i);
		ArrayList vl = (ArrayList) scopedNodesViewList.get(i);
		// If the node object is scoped to this view, then ..
		int vsize = vl.size();

		if (node instanceof SoundRetained) {
		    ((SoundRetained)node).isViewScoped = false;
		    for (int k = 0; k < vsize; k++) {		
			View view = (View) vl.get(k);
			deleteScopedSound((SoundRetained) node, view);
		    }
		}
		else if (node instanceof SoundscapeRetained) {
		    ((SoundscapeRetained)node).isViewScoped = false;
		    for (int k = 0; k < vsize; k++) {		
			View view = (View) vl.get(k);
			deleteScopedSoundscape((SoundscapeRetained) node, view);
		    }
		}
	    }
	}
    }

    void deleteNonScopedSound(SoundRetained sound) {
        if (!nonViewScopedSounds.isEmpty()) {
            // find sound in list and remove it
            int index = nonViewScopedSounds.indexOf(sound);
            nonViewScopedSounds.remove(index);
        }
    }

    void deleteNonScopedSoundscape(SoundscapeRetained soundscape) {
        boolean error = nonViewScopedSoundscapes.remove(soundscape);
    }

    void deleteScopedSound(SoundRetained sound, View view) {
	ArrayList l = (ArrayList) viewScopedSounds.get(view);
        if (!l.isEmpty()) {
            // find sound in list and remove it
            int index = l.indexOf(sound);
            l.remove(index);
        }
	if (l.isEmpty())
	    viewScopedSounds.remove(view);
    }

    void deleteScopedSoundscape(SoundscapeRetained soundscape, View view) {
	ArrayList l = (ArrayList) viewScopedSoundscapes.get(view);
        if (!l.isEmpty()) {
            // find sound in list and remove it
            int index = l.indexOf(soundscape);
            l.remove(index);
        }
	if (l.isEmpty())
	    viewScopedSoundscapes.remove(view);

    }

    void changeNodeAttrib(J3dMessage m) {
        int attribDirty;
        Object node = m.args[0];
        Object value = m.args[1];
        if (debugFlag)
            debugPrint("SoundStructure.changeNodeAttrib:");

        if (node instanceof SoundRetained) {
            attribDirty = ((Integer)value).intValue();
            if (debugFlag)
                debugPrint("         Sound node dirty bit = " + attribDirty);
            if ((attribDirty & SoundRetained.PRIORITY_DIRTY_BIT) > 0) {
		// XXXX: shuffle in SoundScheduler
		/*
                shuffleSound((SoundRetained) node);
		*/
            }
            if ((attribDirty & SoundRetained.SOUND_DATA_DIRTY_BIT) > 0) {
                    loadSound((SoundRetained) node, true);
            }
            ((SoundRetained)node).updateMirrorObject(m.args);
        }
        if (node instanceof SoundscapeRetained) {
/*
            attribDirty = ((Integer)value).intValue();
            if (((attribDirty & SoundscapeRetained.BOUNDING_LEAF_CHANGED) != 0) ||
                ((attribDirty & SoundscapeRetained.APPLICATION_BOUNDS_CHANGED) != 0) ) {
*/
                ((SoundscapeRetained)node).updateTransformChange();
/*
            }
*/
// XXXX: have no dirty flag for soundscape, just auralAttributes...
//          what if reference to AA changes in soundscape???
        }

    }


    void changeNodeState(J3dMessage m) {
        int stateDirty;
        Object node = m.args[0];
        Object value = m.args[1];
        if (debugFlag)
            debugPrint("SoundStructure.changeNodeState:");
        if (node instanceof SoundRetained) {
            stateDirty = ((Integer)value).intValue();
            if (debugFlag)
                debugPrint("         Sound node dirty bit = " + stateDirty);
            if ((stateDirty & SoundRetained.LIVE_DIRTY_BIT) > 0) {
                    loadSound((SoundRetained) node, false);
            }
            if ((stateDirty & SoundRetained.ENABLE_DIRTY_BIT) > 0) {
                    enableSound((SoundRetained) node);
            }
            ((SoundRetained)node).updateMirrorObject(m.args);
        }
    }

    // return true if one of ViewPlatforms intersect region
    boolean intersect(Bounds region) {
        if (region == null)
            return false;
 
        ViewPlatformRetained vpLists[] = (ViewPlatformRetained [])
                                            viewPlatforms.toArray(false);
        
        for (int i=viewPlatforms.arraySize()- 1; i>=0; i--) {
            vpLists[i].schedSphere.getWithLock(tempSphere);
            if (tempSphere.intersect(region)) {
                return true;
            }
        }      
        return false;
    }

    void loadSound(SoundRetained sound, boolean forceLoad) {
// QUESTION: should not be calling into soundScheduler directly??? 
        MediaContainer mediaContainer = sound.getSoundData();
        ViewPlatformRetained vpLists[] = (ViewPlatformRetained [])
                                            viewPlatforms.toArray(false);
        
        for (int i=viewPlatforms.arraySize()- 1; i>=0; i--) {
            View[] views = vpLists[i].getViewList();
            for (int j=(views.length-1); j>=0; j--) {
                View v = (View)(views[j]);
// XXXX: Shouldn't this be done with messages??
                v.soundScheduler.loadSound(sound, forceLoad);
            }
        }        
    }

    void enableSound(SoundRetained sound) {
        ViewPlatformRetained vpLists[] = (ViewPlatformRetained [])
                                            viewPlatforms.toArray(false);
        for (int i=viewPlatforms.arraySize()- 1; i>=0; i--) {
            View[] views = vpLists[i].getViewList();
            for (int j=(views.length-1); j>=0; j--) {
                View v = (View)(views[j]);
                v.soundScheduler.enableSound(sound);
            }
        }
    } 

    void muteSound(SoundRetained sound) {
        ViewPlatformRetained vpLists[] = (ViewPlatformRetained [])
                                            viewPlatforms.toArray(false);
        for (int i=viewPlatforms.arraySize()- 1; i>=0; i--) {
            View[] views = vpLists[i].getViewList();
            for (int j=(views.length-1); j>=0; j--) {
                View v = (View)(views[j]);
                v.soundScheduler.muteSound(sound);
            }
        }
    } 
 
    void pauseSound(SoundRetained sound) {
        ViewPlatformRetained vpLists[] = (ViewPlatformRetained [])
                                            viewPlatforms.toArray(false);
        for (int i=viewPlatforms.arraySize()- 1; i>=0; i--) {
            View[] views = vpLists[i].getViewList();
            for (int j=(views.length-1); j>=0; j--) {
                View v = (View)(views[j]);
                v.soundScheduler.pauseSound(sound);
            }
        }
    }

    // Implementation be needed.
    void processSwitchChanged(J3dMessage m) {
        /*
        SoundRetained sound;
        LeafRetained leaf;
        UnorderList arrList;
        int size;
        Object[] nodes;

        UpdateTargets targets = (UpdateTargets)m.args[0];
        arrList = targets.targetList[Targets.SND_TARGETS];

        if (arrList != null) {
            size = arrList.size();
            nodes = arrList.toArray(false);

            for (int i=size-1; i>=0; i--) {
                leaf = (LeafRetained)nodes[i];
                sound = (SoundRetained) leaf;
                if (sound.switchState.currentSwitchOn) {
                    // System.out.println("SoundStructure.switch on");
                    // add To Schedule List
                } else {
                    // System.out.println("SoundStructure.switch off");
                    // remove From Schedule List
                }
            }
        }
         */
    }

// How can active flag (based on View orientataion) be set here for all Views?!? 

    UnorderList getSoundList(View view) {
	ArrayList l = (ArrayList)viewScopedSounds.get(view);
	// No sounds scoped to this view
	if (l == null)
	    return nonViewScopedSounds;
	UnorderList newS = (UnorderList) nonViewScopedSounds.clone();
	int size = l.size();
	for (int i = 0; i < size; i++) {
	    newS.add(l.get(i));
	}
	return newS;

    }
    UnorderList getSoundscapeList(View view) {
	ArrayList l = (ArrayList)viewScopedSoundscapes.get(view);
	// No sounds scoped to this view
	if (l == null)
	    return nonViewScopedSoundscapes;
	UnorderList newS = (UnorderList) nonViewScopedSoundscapes.clone();
	int size = l.size();
	for (int i = 0; i < size; i++) {
	    newS.add(l.get(i));
	}
	return newS;

    }


/*
// XXXX: how is immediate mode handled? below code taken from SoundSchedule
// Don't know how we'll process immediate mode sounds;
// Append immediate mode sounds to live sounds list
        if (graphicsCtx != null) {
            synchronized (graphicsCtx.sounds) {
                nImmedSounds = graphicsCtx.numSounds();
                numSoundsToProcess = nSounds + nImmedSounds;
                if (sounds.length < numSoundsToProcess) {
                    // increase the array length of sounds array list
                    // by added 32 elements more than universe list size
                    sounds = new SoundRetained[numSoundsToProcess + 32];
                }
                for (int i=0; i<nImmedSounds; i++) {
                    sound = (SoundRetained)((graphicsCtx.getSound(i)).retained);                    if (debugFlag) {
                        debugPrint("#=#=#=  sound at " + sound);
                        printSoundState(sound);
                    }

                    if (sound != null && sound.getInImmCtx()) {
                        // There is no 'mirror' copy of Immediate mode sounds made.
                        // Put a reference to sound node itself in .sgSound field.
                        // For most purposes (except transforms & transformed fields)
                        // Scheduler code will treat live scenegraph sounds and
                        // immediate mode sound the same way.
                        sound.sgSound = sound;
                        sounds[nSounds] = sound;
                        if (debugFlag) {
                            debugPrint("#=#=#=  sounds["+nSounds+"] at " +
                                sounds[nSounds]);
                            printSoundState(sounds[nSounds]);
                        }
                        nSounds++;
                    }
                }
            } // sync of GraphicsContext3D.sounds list
        }
        else { // graphics context not set yet, try setting it now
            Canvas3D canvas = view.getFirstCanvas();
            if (canvas != null)
                graphicsCtx = canvas.getGraphicsContext3D();
        }

        if (debugFlag) { 
            debugPrint("SoundStructure: number of sounds in scene graph = "+nRetainedSounds);
            debugPrint("SoundStructure: number of immediate mode sounds = "+nImmedSounds);
        }
*/


    void updateTransformChange(UpdateTargets targets, long referenceTime) {
        // QUESTION: how often and when should xformChangeList be processed
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

                    if (nodes[i] instanceof ConeSoundRetained) {
                	xformChangeList.add(nodes[i]);
                	ConeSoundRetained cnSndNode = 
						(ConeSoundRetained)nodes[i];
                	cnSndNode.updateTransformChange();

            	    } else if (nodes[i] instanceof PointSoundRetained) {
                	xformChangeList.add(nodes[i]);
                	PointSoundRetained ptSndNode = 
						(PointSoundRetained)nodes[i];
                	ptSndNode.updateTransformChange();

                    } else if (nodes[i] instanceof SoundRetained) {
                	xformChangeList.add(nodes[i]);
                	SoundRetained sndNode = (SoundRetained)nodes[i];
                	sndNode.updateTransformChange();

            	    } else if (nodes[i] instanceof SoundscapeRetained) {
                	xformChangeList.add(nodes[i]);
                	SoundscapeRetained sndScapeNode = 	
						(SoundscapeRetained)nodes[i];
                	sndScapeNode.updateTransformChange();

            	    } else if (nodes[i] instanceof AuralAttributesRetained) {
                	xformChangeList.add(nodes[i]);
                    }
                }
            }
        }
    }    

    // Debug print mechanism for Sound nodes
    static final boolean debugFlag = false;
    static final boolean internalErrors = false;

    void debugPrint(String message) {
        if (debugFlag) {
            System.out.println(message);
	}
    }


    boolean isSoundScopedToView(Object obj, View view) {
	SoundRetained s = (SoundRetained)obj;
	if (s.isViewScoped) {
	    ArrayList l = (ArrayList)viewScopedSounds.get(view);
	    if (!l.contains(s))
		return false;
	}
	return true;
    }

    boolean isSoundscapeScopedToView(Object obj, View view) {
	SoundscapeRetained s = (SoundscapeRetained)obj;
	if (s.isViewScoped) {
	    ArrayList l = (ArrayList)viewScopedSoundscapes.get(view);
	    if (!l.contains(s))
		return false;
	}
	return true;
    }

    void updateViewSpecificGroupChanged(J3dMessage m) {
	int component = ((Integer)m.args[0]).intValue();
	Object[] objAry = (Object[])m.args[1];

	ArrayList soundList = null;
	ArrayList soundsScapeList = null;
	    
	if (((component & ViewSpecificGroupRetained.ADD_VIEW) != 0) ||
	    ((component & ViewSpecificGroupRetained.SET_VIEW) != 0)) {
	    int i;
	    Object obj;
	    View view = (View)objAry[0];
	    ArrayList leafList = (ArrayList)objAry[2];
	    int size = leafList.size();
	    // Leaves is non-null only for the top VSG
	    if (size > 0) {
		// Now process the list of affected leaved
		for( i = 0; i < size; i++) {
		    obj =  leafList.get(i);
		    if (obj instanceof SoundRetained) {
			if (soundList == null) {
			    if ((soundList = (ArrayList)viewScopedSounds.get(view)) == null) {
				soundList = new ArrayList();
				viewScopedSounds.put(view, soundList);
			    }
			}
			soundList.add(obj);
		    }
		    else if (obj instanceof SoundscapeRetained) {
			if (soundsScapeList == null) {
			    if ((soundsScapeList = (ArrayList)viewScopedSoundscapes.get(view)) == null) {
				soundsScapeList = new ArrayList();
				viewScopedSoundscapes.put(view, soundsScapeList);
			    }
			}
			soundsScapeList.add(obj);
		    }
		}
	    }
	}
	if (((component & ViewSpecificGroupRetained.REMOVE_VIEW) != 0)||
	    ((component & ViewSpecificGroupRetained.SET_VIEW) != 0)) {
	    int i;
	    Object obj;
	    ArrayList leafList;
	    View view;

	    
	    if ((component & ViewSpecificGroupRetained.REMOVE_VIEW) != 0) {
		view = (View)objAry[0];
		leafList = (ArrayList)objAry[2];
	    }
	    else {
		view = (View)objAry[4];
		leafList = (ArrayList)objAry[6];
	    }
	    int size = leafList.size();
	    // Leaves is non-null only for the top VSG
	    if (size > 0) {
		// Now process the list of affected leaved
		for( i = 0; i < size; i++) {
		    obj =  leafList.get(i);
		    if (obj instanceof SoundRetained) {
			if (soundList == null) {
			    soundList = (ArrayList)viewScopedSounds.get(view);
			}
			soundList.remove(obj);
		    }
		    if (obj instanceof SoundscapeRetained) {
			if (soundsScapeList == null) {
			    soundsScapeList = (ArrayList)viewScopedSoundscapes.get(view);
			}
			soundsScapeList.remove(obj);
		    }
		}
		// If there are no more lights scoped to the view,
		// remove the mapping
		if (soundList != null && soundList.size() == 0)
		    viewScopedSounds.remove(view);
		if (soundsScapeList != null && soundsScapeList.size() == 0)
		    viewScopedSoundscapes.remove(view);
	    }

	}	
    
    }

    void cleanup() {}

}
