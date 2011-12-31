/*
 * $RCSfile$
 *
 * Copyright 1999-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
import java.util.Arrays;
import java.util.HashMap;

import javax.vecmath.Vector3d;

/**
 * A rendering environment structure is an object that organizes lights,
 * fogs, backgrounds, clips, and model clips.
 */

class RenderingEnvironmentStructure extends J3dStructure implements ObjectUpdate {
    /**
     * The list of light nodes
     */
    ArrayList nonViewScopedLights = new ArrayList();
    HashMap viewScopedLights = new HashMap();
    int numberOfLights = 0;

    /**
     * The list of fog nodes
     */
    ArrayList nonViewScopedFogs = new ArrayList();
    HashMap viewScopedFogs = new HashMap();
    int numberOfFogs = 0;

    /**
     * The list of alternate app nodes
     */
    ArrayList nonViewScopedAltAppearances = new ArrayList();
    HashMap viewScopedAltAppearances = new HashMap();
    int numberOfAltApps = 0;

    /**
     * The list of model clip nodes
     */
    ArrayList nonViewScopedModelClips = new ArrayList();
    HashMap viewScopedModelClips = new HashMap();
    int numberOfModelClips = 0;

    /**
     * The list of background nodes
     */
    ArrayList nonViewScopedBackgrounds = new ArrayList();
    HashMap viewScopedBackgrounds = new HashMap();
    int numberOfBgs = 0;

    /**
     * The list of clip nodes
     */
    ArrayList nonViewScopedClips = new ArrayList();
    HashMap viewScopedClips = new HashMap();
    int numberOfClips = 0;

    // For closest Background selection
    BackgroundRetained[] intersectedBacks = new BackgroundRetained[1];


    // For closest Clip selection
    ClipRetained[] intersectedClips = new ClipRetained[1];

    // For closest Background, Clip, Fog selection
    Bounds[] intersectedBounds = new Bounds[1];

    Transform3D localeXform = new Transform3D();
    Vector3d localeTranslation = new Vector3d();
    Bounds localeBounds = null;

    // For closest Fog selection
    FogRetained[] intersectedFogs = new FogRetained[1];

    // for closest alternate appearance selection
    AlternateAppearanceRetained[] intersectedAltApps = new AlternateAppearanceRetained[1];

    // For closest ModelClip selection
    ModelClipRetained[] intersectedModelClips = new ModelClipRetained[1];


    // Back clip distance in V world
    double backClipDistance;

   // ArrayList of leafRetained object whose mirrorObjects
    // should be updated
    ArrayList objList = new ArrayList();

    // ArrayList of leafRetained object whose boundingleaf xform
    // should be updated
    ArrayList xformChangeList = new ArrayList();


    // freelist management of objects
    ArrayList objFreeList = new ArrayList();

    LightRetained[] retlights = new LightRetained[5];

    // variables used for processing transform messages
    boolean transformMsg = false;
    UpdateTargets targets = null;
    ArrayList blUsers = null;

    Integer ogInsert = new Integer(J3dMessage.ORDERED_GROUP_INSERTED);
    Integer ogRemove = new Integer(J3dMessage.ORDERED_GROUP_REMOVED);

    // Used to lock the intersectedBounds {used by fog, mclip etc}
    // Can used intersectedBounds itself, since this may be realloced
    Object lockObj = new Object();

    /**
     * Constructs a RenderingEnvironmentStructure object in the specified
     * virtual universe.
     */
    RenderingEnvironmentStructure(VirtualUniverse u) {
	super(u, J3dThread.UPDATE_RENDERING_ENVIRONMENT);
    }


    /**
     * Returns a object array of length 5 to save the 5 objects in the message list.
     */
    Object[] getObjectArray() {
	Object[] objs;
	int size;

	size = objFreeList.size();
	if (size == 0) {
	    objs = new Object[5];
	}
	else {
	    objs = (Object[]) objFreeList.get(size - 1);
	    objFreeList.remove(size -1);
	}
	return objs;
    }

    void addObjArrayToFreeList(Object[] objs) {
	int i;

	for (i = 0; i < objs.length; i++)
	    objs[i] = null;

	objFreeList.add(objs);
    }



    public void updateObject() {
	int i, j;
	Object[] args;
	LeafRetained leaf;
	Boolean masks;
	int size;

	size = objList.size();
	for (i = 0; i < size; i++) {
	    args = (Object[])objList.get(i);
	    leaf = (LeafRetained)args[0];
	    leaf.updateMirrorObject(args);
	    addObjArrayToFreeList(args);
	}
	objList.clear();

	size = xformChangeList.size();
	for (i = 0; i < size; i++) {
	    leaf = (LeafRetained)xformChangeList.get(i);
	    leaf.updateTransformChange();
	}
	xformChangeList.clear();

    }

    void processMessages(long referenceTime) {
	J3dMessage[] messages = getMessages(referenceTime);;
	J3dMessage m;
	int nMsg = getNumMessage();

	if (nMsg <= 0) {
	    return;
	}

	for (int i=0; i < nMsg; i++) {
	    m = messages[i];

	    switch (m.type) {
	    case J3dMessage.INSERT_NODES:
		insertNodes(m);
		break;
	    case J3dMessage.REMOVE_NODES:
		removeNodes(m);
		break;
	    case J3dMessage.LIGHT_CHANGED:
		updateLight((Object[])m.args);
		break;
	    case J3dMessage.BOUNDINGLEAF_CHANGED:
		updateBoundingLeaf((Object[])m.args);
		break;
	    case J3dMessage.FOG_CHANGED:
		updateFog((Object[])m.args);
		break;
	    case J3dMessage.ALTERNATEAPPEARANCE_CHANGED:
		updateAltApp((Object[])m.args);
		break;
	    case J3dMessage.SHAPE3D_CHANGED:
		updateShape3D((Object[])m.args);
		break;
	    case J3dMessage.ORIENTEDSHAPE3D_CHANGED:
		updateOrientedShape3D((Object[])m.args);
		break;
	    case J3dMessage.MORPH_CHANGED:
		updateMorph((Object[])m.args);
		break;
	    case J3dMessage.TRANSFORM_CHANGED:
		transformMsg = true;
		break;
	    case J3dMessage.SWITCH_CHANGED:
		processSwitchChanged(m);
		// may need to process dirty switched-on transform
		if (universe.transformStructure.getLazyUpdate()) {
		    transformMsg = true;
		}
		break;
	    case J3dMessage.MODELCLIP_CHANGED:
		updateModelClip((Object[])m.args);
		break;
	    case J3dMessage.BACKGROUND_CHANGED:
		updateBackground((Object[])m.args);
		break;
	    case J3dMessage.CLIP_CHANGED:
		updateClip((Object[])m.args);
		break;
	    case J3dMessage.ORDERED_GROUP_INSERTED:
		updateOrderedGroupInserted(m);
		break;
	    case J3dMessage.ORDERED_GROUP_REMOVED:
		updateOrderedGroupsRemoved(m);
		break;
	    case J3dMessage.VIEWSPECIFICGROUP_CHANGED:
		updateViewSpecificGroupChanged(m);
		break;
	    case J3dMessage.VIEWSPECIFICGROUP_INIT:
		initViewSpecificInfo(m);
		break;
	    case J3dMessage.VIEWSPECIFICGROUP_CLEAR:
		clearViewSpecificInfo(m);
		break;
	    }
	    m.decRefcount();
	}

	if (transformMsg) {
	    updateTransformChange();
	    transformMsg = false;
	}

	VirtualUniverse.mc.addMirrorObject(this);

	Arrays.fill(messages, 0, nMsg, null);
    }

    void updateOrderedGroupInserted(J3dMessage m) {
	Object[] ogList = (Object[])m.args[0];
	Object[] ogChildIdList = (Object[])m.args[1];
	Object[] ogOrderedIdList = (Object[])m.args[2];
	OrderedGroupRetained og;
	int index;
	Object[] objs;

	for (int n = 0; n < ogList.length; n++) {
	    og = (OrderedGroupRetained)ogList[n];
	    og.updateChildIdTableInserted(((Integer) ogChildIdList[n]).intValue(),
					  ((Integer) ogOrderedIdList[n]).intValue());
	    og.incrChildCount();
	}
    }

    void updateOrderedGroupsRemoved(J3dMessage m) {
	Object[] ogList = (Object[])m.args[0];
	Object[] ogChildIdList = (Object[])m.args[1];
	OrderedGroupRetained og;
	int index;
	Object[] objs;

	for (int n = 0; n < ogList.length; n++) {
	    og = (OrderedGroupRetained)ogList[n];
	    og.updateChildIdTableRemoved(((Integer) ogChildIdList[n]).intValue());
	    og.decrChildCount();
	}

    }
    /**
     * This processes a switch change.
     */
    void processSwitchChanged(J3dMessage m) {
        int i;
        UnorderList arrList;
        int size;
        Object[] nodes, nodesArr;
        LeafRetained leaf;

        UpdateTargets targets = (UpdateTargets)m.args[0];

        arrList = targets.targetList[Targets.BLN_TARGETS];
        if (arrList != null) {
            BoundingLeafRetained mbleaf;
            Object[] objArr = (Object[])m.args[1];
            Object[] obj;
            size = arrList.size();
            nodesArr = arrList.toArray(false);

            for (int h=0; h<size; h++) {
                nodes = (Object[])nodesArr[h];
                obj = (Object[])objArr[h];

                for (i=0; i<nodes.length; i++) {

                    Object[] users = (Object[])obj[i];
                    mbleaf = (BoundingLeafRetained)nodes[i];

                    //mbleaf.switchState.updateCurrentSwitchOn();
                    for (int j = 0; j < users.length; j++) {
                        leaf = (LeafRetained)users[j];
                        if (leaf instanceof FogRetained ||
                            leaf instanceof LightRetained ||
                            leaf instanceof ModelClipRetained ||
                            leaf instanceof ClipRetained ||
                            leaf instanceof AlternateAppearanceRetained ||
                            leaf instanceof BackgroundRetained) {
                            leaf.updateBoundingLeaf();
                        }
                    }
                }
            }
        }
    }

    void insertNodes(J3dMessage m) {
	Object[] nodes = (Object[])m.args[0];
	ArrayList viewScopedNodes = (ArrayList)m.args[3];
	ArrayList scopedNodesViewList = (ArrayList)m.args[4];
	Object n;
	int i;
	GeometryAtom ga;
	int num;
	LightRetained lt;
	FogRetained fg;
	ModelClipRetained mc;
	ArrayList list;

	for (i=0; i<nodes.length; i++) {
	    n = nodes[i];
	    if (n instanceof LightRetained) {
                lt = (LightRetained)n;
		numberOfLights++;

		// If this particulat light is not scoped, added it
		// to all the views
		if (lt.inBackgroundGroup)
		    lt.geometryBackground.lights.add(lt);
		else
		    nonViewScopedLights.add(lt);

	    } else if (n instanceof FogRetained) {
                fg = (FogRetained)n;
		numberOfFogs++;
		// If the fog is scoped to a view , then ..

		if (fg.inBackgroundGroup) {
		    fg.geometryBackground.fogs.add(fg);
		} else {
		    nonViewScopedFogs.add(fg);
		}


	    } else if (n instanceof AlternateAppearanceRetained) {
		AlternateAppearanceRetained altApp = (AlternateAppearanceRetained)n;

		numberOfAltApps++;

		nonViewScopedAltAppearances.add(n);

	    } else if (n instanceof BackgroundRetained) {
		BackgroundRetained bg = (BackgroundRetained)n;
		numberOfBgs++;

		nonViewScopedBackgrounds.add(n);

	    } else if (n instanceof ClipRetained) {
		ClipRetained cl = (ClipRetained)n;
		numberOfClips++;

		nonViewScopedClips.add(n);

	    } else if (n instanceof ModelClipRetained) {
		mc = (ModelClipRetained)n;
		numberOfModelClips++;

		nonViewScopedModelClips.add(n);

	    }

	}


	if (viewScopedNodes != null) {
	    int size = viewScopedNodes.size();
	    int vlsize;


	    for (i = 0; i < size; i++) {
		n = (NodeRetained)viewScopedNodes.get(i);
		ArrayList vl = (ArrayList) scopedNodesViewList.get(i);
		if (n instanceof LightRetained) {
		    ((LightRetained)n).isViewScoped = true;
		    numberOfLights++;
		    vlsize = vl.size();
		    for (int k = 0; k < vlsize; k++) {
			View view = (View)vl.get(k);
			if ((list = (ArrayList)viewScopedLights.get(view)) == null) {
			    list = new ArrayList();
			    viewScopedLights.put(view, list);
			}
			list.add(n);
		    }
		} else if (n instanceof FogRetained) {
		    ((FogRetained)n).isViewScoped = true;
		    numberOfFogs++;
		    vlsize = vl.size();
		    for (int k = 0; k < vlsize; k++) {
			View view = (View)vl.get(k);
			if ((list = (ArrayList)viewScopedFogs.get(view)) == null) {
			    list = new ArrayList();
			    viewScopedFogs.put(view, list);
			}
			list.add(n);
		    }
		} else if (n instanceof AlternateAppearanceRetained) {
		    ((AlternateAppearanceRetained)n).isViewScoped = true;
		    numberOfAltApps++;
		    vlsize = vl.size();
		    for (int k = 0; k < vlsize; k++) {
			View view = (View)vl.get(k);
			if ((list = (ArrayList)viewScopedAltAppearances.get(view)) == null) {
			    list = new ArrayList();
			    viewScopedAltAppearances.put(view, list);
			}
			list.add(n);
		    }
		} else if (n instanceof BackgroundRetained) {
		    ((BackgroundRetained)n).isViewScoped = true;
		    numberOfBgs++;
		    vlsize = vl.size();
		    for (int k = 0; k < vlsize; k++) {
			View view = (View)vl.get(k);
			if ((list = (ArrayList)viewScopedBackgrounds.get(view)) == null) {
			    list = new ArrayList();
			    viewScopedBackgrounds.put(view, list);
			}
			list.add(n);
		    }
		} else if (n instanceof ClipRetained) {
		    ((ClipRetained)n).isViewScoped = true;
		    numberOfClips++;
		    vlsize = vl.size();
		    for (int k = 0; k < vlsize; k++) {
			View view = (View)vl.get(k);
			if ((list = (ArrayList)viewScopedClips.get(view)) == null) {
			    list = new ArrayList();
			    viewScopedClips.put(view, list);
			}
			list.add(n);
		    }
		} else if (n instanceof ModelClipRetained) {
		    ((ModelClipRetained)n).isViewScoped = true;
		    numberOfModelClips++;
		    vlsize = vl.size();
		    for (int k = 0; k < vlsize; k++) {
			View view = (View)vl.get(k);
			if ((list = (ArrayList)viewScopedModelClips.get(view)) == null) {
			    list = new ArrayList();
			    viewScopedModelClips.put(view, list);
			}
			list.add(n);
		    }
		}
	    }

	}
	if (numberOfLights > retlights.length)
	    retlights = new LightRetained[numberOfLights];
	if (intersectedFogs.length < numberOfFogs)
	    intersectedFogs = new FogRetained[numberOfFogs];
	if (intersectedAltApps.length < numberOfAltApps)
	    intersectedAltApps = new AlternateAppearanceRetained[numberOfAltApps];
	if (intersectedBacks.length < numberOfBgs)
	    intersectedBacks = new BackgroundRetained[numberOfBgs];
	if (intersectedClips.length < numberOfClips)
	    intersectedClips = new ClipRetained[numberOfClips];
	if (intersectedModelClips.length < numberOfModelClips)
	    intersectedModelClips = new ModelClipRetained[numberOfModelClips];
    }

    void removeNodes(J3dMessage m) {
	Object[] nodes = (Object[])m.args[0];
	ArrayList viewScopedNodes = (ArrayList)m.args[3];
	ArrayList scopedNodesViewList = (ArrayList)m.args[4];
        Object n;
        int i;
	GeometryAtom ga;
	LeafRetained oldsrc = null;

	// System.err.println("RE : removeNodes message " + m);
	// System.err.println("RE : removeNodes m.args[0] " + m.args[0]);

	for (i=0; i<nodes.length; i++) {
	    n = nodes[i];
	    if (n instanceof LightRetained) {
		LightRetained lt = (LightRetained)n;
		if (lt.inBackgroundGroup) {
		    lt.geometryBackground.lights.remove(lt);
		}
		else {
		    nonViewScopedLights.remove(nonViewScopedLights.indexOf(n));
		}

		numberOfLights--;
	    } else if (n instanceof FogRetained) {
		numberOfFogs--;
                FogRetained fg = (FogRetained)n;
		if (fg.inBackgroundGroup) {
		    fg.geometryBackground.fogs.remove(fg);
		} else {
		    nonViewScopedFogs.remove(nonViewScopedFogs.indexOf(n));
		}
	    } else if (n instanceof  AlternateAppearanceRetained) {
		numberOfAltApps--;
	        nonViewScopedAltAppearances.remove(nonViewScopedAltAppearances.indexOf(n));
	    }else if (n instanceof BackgroundRetained) {
		numberOfBgs--;
	        nonViewScopedBackgrounds.remove(nonViewScopedBackgrounds.indexOf(n));
	    } else if (n instanceof ClipRetained) {
		numberOfClips--;
	        nonViewScopedClips.remove(nonViewScopedClips.indexOf(n));
	    } else if (n instanceof ModelClipRetained) {
		ModelClipRetained mc = (ModelClipRetained)n;
		numberOfModelClips--;
		nonViewScopedModelClips.remove(nonViewScopedModelClips.indexOf(n));

	    }
	    else if (n instanceof GeometryAtom) {
		ga = (GeometryAtom)n;
		// Check that we have not already cleared the mirrorobject
		// since mant geometry atoms could be generated for one
		// mirror shape
		if (ga.source != oldsrc) {
		    ga.source.clearMirrorShape();
		    oldsrc = ga.source;
		}
	    }
	    else if (n instanceof OrderedGroupRetained) {
		// Clear the orderedBins for this orderedGroup
		((OrderedGroupRetained)n).clearDerivedDataStructures();
	    }
	}
	if (viewScopedNodes != null) {
	    int size = viewScopedNodes.size();
	    int vlsize;
	    ArrayList list;
	    for (i = 0; i < size; i++) {
		n = (NodeRetained)viewScopedNodes.get(i);
		ArrayList vl = (ArrayList) scopedNodesViewList.get(i);
		if (n instanceof LightRetained) {
		    ((LightRetained)n).isViewScoped = false;
		    numberOfLights--;
		    vlsize = vl.size();
		    for (int k = 0; k < vlsize; k++) {
			View view = (View)vl.get(k);
			list = (ArrayList)viewScopedLights.get(view);
			list.remove(n);
			if (list.size() == 0) {
			    viewScopedLights.remove(view);
			}
		    }
		} else if (n instanceof FogRetained) {
		    ((FogRetained)n).isViewScoped = false;
		    numberOfFogs--;
		    vlsize = vl.size();
		    for (int k = 0; k < vlsize; k++) {
			View view = (View)vl.get(k);
			list = (ArrayList)viewScopedFogs.get(view);
			list.remove(n);
			if (list.size() == 0) {
			    viewScopedFogs.remove(view);
			}
		    }
		} else if (n instanceof AlternateAppearanceRetained) {
		    ((AlternateAppearanceRetained)n).isViewScoped = false;
		    numberOfAltApps--;
		    vlsize = vl.size();
		    for (int k = 0; k < vlsize; k++) {
			View view = (View)vl.get(k);
			list = (ArrayList)viewScopedAltAppearances.get(view);
			list.remove(n);
			if (list.size() == 0) {
			    viewScopedAltAppearances.remove(view);
			}
		    }
		} else if (n instanceof BackgroundRetained) {
		    ((BackgroundRetained)n).isViewScoped = false;
		    numberOfBgs--;
		    vlsize = vl.size();
		    for (int k = 0; k < vlsize; k++) {
			View view = (View)vl.get(k);
			list = (ArrayList)viewScopedBackgrounds.get(view);
			list.remove(n);
			if (list.size() == 0) {
			    viewScopedBackgrounds.remove(view);
			}
		    }
		} else if (n instanceof ClipRetained) {
		    ((ClipRetained)n).isViewScoped = false;
		    numberOfClips--;
		    vlsize = vl.size();
		    for (int k = 0; k < vlsize; k++) {
			View view = (View)vl.get(k);
			list = (ArrayList)viewScopedClips.get(view);
			list.remove(n);
			if (list.size() == 0) {
			    viewScopedClips.remove(view);
			}
		    }
		} else if (n instanceof ModelClipRetained) {
		    ((ModelClipRetained)n).isViewScoped = false;
		    numberOfModelClips--;
		    vlsize = vl.size();
		    for (int k = 0; k < vlsize; k++) {
			View view = (View)vl.get(k);
			list = (ArrayList)viewScopedModelClips.get(view);
			list.remove(n);
			if (list.size() == 0) {
			    viewScopedModelClips.remove(view);
			}
		    }
		}
	    }

	}
    }

    LightRetained[] getInfluencingLights(RenderAtom ra, View view) {
	LightRetained[] lightAry = null;
        ArrayList globalLights;
	int numLights;
	int i, j, n;

	// Need to lock retlights, since on a multi-processor
	// system with 2 views on a single universe, there might
	// be councurrent access
	synchronized (retlights) {
	    numLights = 0;
	    if (ra.geometryAtom.source.inBackgroundGroup) {
		globalLights = ra.geometryAtom.source.geometryBackground.lights;
		numLights = processLights(globalLights, ra, numLights);
	    } else {
		if ((globalLights = (ArrayList)viewScopedLights.get(view)) != null) {
		    numLights = processLights(globalLights, ra, numLights);
		}
		// now process the common lights
		numLights = processLights(nonViewScopedLights, ra, numLights);
	    }

	    boolean newLights = false;
	    if (ra.lights != null && ra.lights.length == numLights) {
		for (i=0; i<ra.lights.length; i++) {
		    for (j=0; j<numLights; j++) {
			if (ra.lights[i] == retlights[j]) {
			    break;
			}
		    }
		    if (j==numLights) {
			newLights = true;
			break;
		    }
		}
	    } else {
		newLights = true;
	    }
	    if (newLights) {
		lightAry = new LightRetained[numLights];
		for (i = 0; i < numLights; i++) {
		    lightAry[i] = (LightRetained)retlights[i];
		}
		return (lightAry);
	    } else {
		return(ra.lights);
	    }
	}
    }

    // Called while holding the retlights lock
    int processLights(ArrayList globalLights, RenderAtom ra, int numLights) {
	LightRetained[] shapeScopedLt;
        Bounds bounds;
	int i, j, n;
        bounds = ra.localeVwcBounds;
	int size = globalLights.size();

	if (size > 0) {
	    for (i=0; i<size; i++) {
		LightRetained light = (LightRetained)globalLights.get(i);
		//		System.err.println("vwcBounds = "+bounds);
		//		System.err.println("light.region = "+light.region);
		//		System.err.println("Intersected = "+bounds.intersect(light.region));
		//		System.err.println("");

		//		    if ((light.viewList != null && light.viewList.contains(view)) &&
		// Treat lights in background geo as having infinite bounds
		if (light.lightOn && light.switchState.currentSwitchOn &&
		    (ra.geometryAtom.source.inBackgroundGroup || bounds.intersect(light.region))){
		    // Get the mirror Shape3D node
		    n = ((Shape3DRetained)ra.geometryAtom.source).numlights;
		    shapeScopedLt = ((Shape3DRetained)ra.geometryAtom.source).lights;

		    // System.err.println("numLights per shape= "+n);
		    // scoped Fog/light is kept in the original
		    // shape3D node, what happens if this list changes
		    // while accessing them?. So, Lock.
		    if (light.isScoped) {
			for (j = 0; j < n; j++) {
			    // then check if the light is scoped to
			    // this group
			    if (light == shapeScopedLt[j]) {
				retlights[numLights++] = light;
				break;
			    }
			}
		    }
		    else {
			retlights[numLights++] = light;
		    }
		}
	    }
	}
	return numLights;

    }
    FogRetained getInfluencingFog(RenderAtom ra, View view) {
	FogRetained fog = null;
	int i, j, k, n, nfogs;
	Bounds closestBounds;
        ArrayList globalFogs;
	int numFogs;

	// Need to lock lockObj, since on a multi-processor
	// system with 2 views on a single universe, there might
	// be councurrent access
	synchronized(lockObj) {
	    nfogs = 0;
	    Bounds bounds = ra.localeVwcBounds;

	    if (intersectedBounds.length < numberOfFogs)
		intersectedBounds = new Bounds[numberOfFogs];

	    if (ra.geometryAtom.source.inBackgroundGroup) {
		globalFogs = ra.geometryAtom.source.geometryBackground.fogs;
		nfogs = processFogs(globalFogs, ra, nfogs);
		// If background, then nfogs > 1, take the first one
		if (nfogs >= 1)
		    fog = intersectedFogs[0];

	    } else {
		if ((globalFogs = (ArrayList)viewScopedFogs.get(view)) != null) {
		    nfogs = processFogs(globalFogs, ra, nfogs);
		}
		// now process the common fogs
		nfogs = processFogs(nonViewScopedFogs, ra, nfogs);


		if (nfogs == 1)
		    fog = intersectedFogs[0];

		else if (nfogs > 1) {
		    closestBounds = bounds.closestIntersection(intersectedBounds);
		    for (j= 0; j < nfogs; j++) {
			if (intersectedBounds[j] == closestBounds) {
			    fog = intersectedFogs[j];
			    break;
			}
		    }
		}
	    }
	    return (fog);
	}
    }

    // Called while holding lockObj lock
    int processFogs(ArrayList globalFogs, RenderAtom ra, int numFogs) {
	int size = globalFogs.size();
	FogRetained fog;
	int i, k, n;
        Bounds bounds = ra.localeVwcBounds;
	FogRetained[] shapeScopedFog;

	if (globalFogs.size() > 0) {
	    for (i = 0 ; i < size; i++) {
		fog = (FogRetained) globalFogs.get(i);
		// Note : There is no enable check for fog
		if (fog.region != null && fog.switchState.currentSwitchOn &&
		    (ra.geometryAtom.source.inBackgroundGroup || fog.region.intersect(bounds))) {
		    n = ((Shape3DRetained)ra.geometryAtom.source).numfogs;
		    shapeScopedFog = ((Shape3DRetained)ra.geometryAtom.source).fogs;

		    if (fog.isScoped) {
			for (k = 0; k < n; k++) {
			    // then check if the Fog is scoped to
			    // this group
			    if (fog == shapeScopedFog[k]) {
				intersectedBounds[numFogs] = fog.region;
				intersectedFogs[numFogs++] = fog;
				break;
			    }
			}
		    }
		    else {
			intersectedBounds[numFogs] = fog.region;
			intersectedFogs[numFogs++] = fog;
		    }
		}
	    }
	}
	return numFogs;
    }

    ModelClipRetained getInfluencingModelClip(RenderAtom ra, View view) {
	ModelClipRetained modelClip = null;
	int i, j, k, n, nModelClips;
	Bounds closestBounds;
        ArrayList globalModelClips;



	if (ra.geometryAtom.source.inBackgroundGroup) {
	    return null;
	}
	// Need to lock lockObj, since on a multi-processor
	// system with 2 views on a single universe, there might
	// be councurrent access
	synchronized(lockObj) {
	    Bounds bounds = ra.localeVwcBounds;
	    nModelClips = 0;
	    if (intersectedBounds.length < numberOfModelClips)
		intersectedBounds = new Bounds[numberOfModelClips];

	    if ((globalModelClips = (ArrayList)viewScopedModelClips.get(view)) != null) {
		nModelClips = processModelClips(globalModelClips, ra, nModelClips);
	    }

	    // now process the common clips
	    nModelClips = processModelClips(nonViewScopedModelClips, ra, nModelClips);





	    modelClip = null;
	    if (nModelClips == 1)
		modelClip = intersectedModelClips[0];
	    else if (nModelClips > 1) {
		closestBounds = bounds.closestIntersection(intersectedBounds);
		for (j= 0; j < nModelClips; j++) {
		    if (intersectedBounds[j] == closestBounds) {
			modelClip = intersectedModelClips[j];
			break;
		    }
		}
	    }
	    return (modelClip);
	}

    }

    int processModelClips(ArrayList globalModelClips, RenderAtom ra, int nModelClips) {
    	int size = globalModelClips.size();
	int i, k, n;
	ModelClipRetained modelClip;
        Bounds bounds = ra.localeVwcBounds;
	ModelClipRetained[] shapeScopedModelClip;

	if (size > 0) {
	    for (i = 0; i < size; i++) {
		modelClip = (ModelClipRetained) globalModelClips.get(i);
		if (modelClip.enableFlag == true &&
		    modelClip.region != null && modelClip.switchState.currentSwitchOn) {
		    if (modelClip.region.intersect(bounds) == true) {
			n = ((Shape3DRetained)ra.geometryAtom.source).numModelClips;
			shapeScopedModelClip = ((Shape3DRetained)ra.geometryAtom.source).modelClips;

			if (modelClip.isScoped) {
			    for (k = 0; k < n; k++) {
				// then check if the modelClip is scoped to
				// this group
				if (shapeScopedModelClip[k] == modelClip) {

				    intersectedBounds[nModelClips] = modelClip.region;
				    intersectedModelClips[nModelClips++] = modelClip;
				    break;
				}
			    }
			}
			else {
			    intersectedBounds[nModelClips] = modelClip.region;
			    intersectedModelClips[nModelClips++] = modelClip;
			}
		    }
		}
	    }
	}
	return nModelClips;
    }

    BackgroundRetained getApplicationBackground(BoundingSphere bounds, Locale viewLocale, View view) {
	BackgroundRetained bg = null;
	Bounds closestBounds;
	BackgroundRetained back;
	int i = 0, j = 0;
	int nbacks;
        ArrayList globalBgs;



	// Need to lock lockObj, since on a multi-processor
	// system with 2 views on a single universe, there might
	// be councurrent access
	synchronized(lockObj) {
	    nbacks = 0;
	    if (intersectedBounds.length < numberOfBgs)
		intersectedBounds = new Bounds[numberOfBgs];



	    if ((globalBgs = (ArrayList)viewScopedBackgrounds.get(view)) != null) {
		nbacks = processBgs(globalBgs,  bounds, nbacks, viewLocale);
	    }
	    nbacks = processBgs(nonViewScopedBackgrounds,  bounds, nbacks, viewLocale);

	    // If there are no intersections, set to black.
	    if (nbacks == 1) {
		bg = intersectedBacks[0];
	    } else  if (nbacks > 1) {
		closestBounds =
		    bounds.closestIntersection(intersectedBounds);
		for (j=0; j<nbacks; j++) {
		    if (intersectedBounds[j] == closestBounds) {
			bg = intersectedBacks[j];
			//System.err.println("matched " + closestBounds);
			break;
		    }
		}
	    }
	    return (bg);
	}
    }


    // Called while holding lockObj lock
    int processBgs(ArrayList globalBgs, BoundingSphere bounds, int nbacks, Locale viewLocale) {
	int size = globalBgs.size();
	int i;
	BackgroundRetained back;

	for (i=0; i<size; i++) {
	    back = (BackgroundRetained)globalBgs.get(i);
	    if (back.transformedRegion != null && back.switchState.currentSwitchOn) {
		if (back.cachedLocale != viewLocale) {
		    localeBounds = (Bounds) back.transformedRegion.clone();
		    // Translate the transformed region
		    back.cachedLocale.hiRes.difference(viewLocale.hiRes, localeTranslation);
		    localeXform.setIdentity();
		    localeXform.setTranslation(localeTranslation);
		    localeBounds.transform(localeXform);
		    if (localeBounds.intersect(bounds) == true) {
			intersectedBounds[nbacks] = localeBounds;
			intersectedBacks[nbacks++] = back;
		    }
		}
		else {
		    if (back.transformedRegion.intersect(bounds) == true) {
			intersectedBounds[nbacks] = back.transformedRegion;
			intersectedBacks[nbacks++] = back;
		    }
		}
	    }
	}
	return nbacks;
    }

    double[] backClipDistanceInVworld (BoundingSphere bounds, View view) {
	int i,j;
        int nclips;
        Bounds closestBounds;
	ClipRetained clip;
	boolean backClipActive;
	double[] backClipDistance;
	double distance;
	ArrayList globalClips;

	// Need to lock intersectedBounds, since on a multi-processor
	// system with 2 views on a single universe, there might
	// be councurrent access
	synchronized(lockObj) {
	    backClipDistance = null;
	    backClipActive = false;
	    nclips = 0;
	    distance = 0.0;
	    if (intersectedBounds.length < numberOfClips)
		intersectedBounds = new Bounds[numberOfClips];

	    if ((globalClips = (ArrayList)viewScopedClips.get(view)) != null) {
		nclips = processClips(globalClips, bounds, nclips);
	    }
	    nclips = processClips(nonViewScopedClips, bounds, nclips);




	    if (nclips == 1)  {
		distance = intersectedClips[0].backDistanceInVworld;
		backClipActive = true;
	    } else if (nclips > 1) {
		closestBounds =
		    bounds.closestIntersection(intersectedBounds);
		for (j=0; j < nclips; j++) {
		    if (intersectedBounds[j] == closestBounds) {
			distance = intersectedClips[j].backDistanceInVworld;
			backClipActive = true;
			break;
		    }
		}
	    }
	    if (backClipActive) {
		backClipDistance = new double[1];
		backClipDistance[0] = distance;
	    }
	    return (backClipDistance);
	}
    }

    int processClips(ArrayList globalClips, BoundingSphere bounds, int nclips) {
	int i;
	int size = globalClips.size();
	ClipRetained clip;

	for (i=0 ; i<size; i++) {
	    clip = (ClipRetained)globalClips.get(i);
	    if (clip.transformedRegion != null &&
                    clip.transformedRegion.intersect(bounds) == true &&
                    clip.switchState.currentSwitchOn) {
		intersectedBounds[nclips] = clip.transformedRegion;
		intersectedClips[nclips++] = clip;
	    }
	}
	return nclips;
    }


    void updateLight(Object[] args) {
	Object[] objs;
	LightRetained light = (LightRetained)args[0];
	int component = ((Integer)args[1]).intValue();
	// Store the value to be updated during object update
	// If its an ambient light, then if color changed, update immediately
	if ((component & (LightRetained.INIT_MIRROR)) != 0) {
	    light.initMirrorObject(args);
	}

	if (light instanceof AmbientLightRetained &&
	    ((component & LightRetained.COLOR_CHANGED) != 0)) {
	    light.updateImmediateMirrorObject(args);
	}
	else if ((component & (LightRetained.COLOR_CHANGED|
			       LightRetained.INIT_MIRROR |
			  PointLightRetained.POSITION_CHANGED |
			  PointLightRetained.ATTENUATION_CHANGED|
			  DirectionalLightRetained.DIRECTION_CHANGED |
			  SpotLightRetained.DIRECTION_CHANGED |
			  SpotLightRetained.CONCENTRATION_CHANGED |
			  SpotLightRetained.ANGLE_CHANGED)) != 0) {
	    objs = getObjectArray();
	    objs[0] = args[0];
	    objs[1] = args[1];
	    objs[2] = args[2];
	    objs[3] = args[3];
	    objs[4] = args[4];

	    objList.add(objs);
	}
	else if ((component & LightRetained.CLEAR_MIRROR) != 0) {
	    light.clearMirrorObject(args);
	}
	else  {
	    light.updateImmediateMirrorObject(args);
	}



    }

    void updateBackground(Object[] args) {
	BackgroundRetained bg = (BackgroundRetained)args[0];
	bg.updateImmediateMirrorObject(args);
    }

    void updateFog(Object[] args) {
	Object[] objs;
	FogRetained fog = (FogRetained)args[0];
	int component = ((Integer)args[1]).intValue();
	if ((component & FogRetained.INIT_MIRROR) != 0) {
	    fog.initMirrorObject(args);
	    // Color, distance et all should be updated when renderer
	    // is not running ..
	    objs = getObjectArray();
	    objs[0] = args[0];
	    objs[1] = args[1];
	    objs[2] = args[2];
	    objs[3] = args[3];
	    objs[4] = args[4];
	    objList.add(objs);
	}
	else if ((component & FogRetained.CLEAR_MIRROR) != 0) {
	    fog.clearMirrorObject(args);
	// Store the value to be updated during object update
	} else if ((component & (FogRetained.COLOR_CHANGED |
			  LinearFogRetained.FRONT_DISTANCE_CHANGED|
			  LinearFogRetained.BACK_DISTANCE_CHANGED |
			  ExponentialFogRetained.DENSITY_CHANGED)) != 0) {
	    objs = getObjectArray();
	    objs[0] = args[0];
	    objs[1] = args[1];
	    objs[2] = args[2];
	    objs[3] = args[3];
	    objs[4] = args[4];
	    objList.add(objs);
	}
	else {
	    fog.updateImmediateMirrorObject(args);
	}


    }

    void updateAltApp(Object[] args) {
	AlternateAppearanceRetained altApp = (AlternateAppearanceRetained)args[0];
	int component = ((Integer)args[1]).intValue();
	if ((component & AlternateAppearanceRetained.INIT_MIRROR) != 0) {
	    AlternateAppearanceRetained altapp = (AlternateAppearanceRetained)args[0];
	    altapp.initMirrorObject(args);
	}
	else if ((component & AlternateAppearanceRetained.CLEAR_MIRROR) != 0) {
	    AlternateAppearanceRetained altapp = (AlternateAppearanceRetained)args[0];
	    altapp.clearMirrorObject(args);
	}
	else {
	    altApp.updateImmediateMirrorObject(args);
	}


    }

    void updateClip(Object[] args) {
	ClipRetained clip = (ClipRetained)args[0];
	clip.updateImmediateMirrorObject(args);
    }

    void updateModelClip(Object[] args) {
	ModelClipRetained modelClip = (ModelClipRetained)args[0];
	Object[] objs;
	int component = ((Integer)args[1]).intValue();

	if ((component & ModelClipRetained.INIT_MIRROR) != 0) {
	    modelClip.initMirrorObject(args);
	}
	if ((component & ModelClipRetained.CLEAR_MIRROR) != 0) {
	    modelClip.clearMirrorObject(args);
	}
	else if ((component & (ModelClipRetained.PLANES_CHANGED |
			  ModelClipRetained.INIT_MIRROR |
			  ModelClipRetained.PLANE_CHANGED)) != 0) {
	    objs = getObjectArray();
	    objs[0] = args[0];
	    objs[1] = args[1];
	    objs[2] = args[2];
	    objs[3] = args[3];
	    objs[4] = args[4];
	    objList.add(objs);
	}
	else {
	    modelClip.updateImmediateMirrorObject(args);
	}

    }

    void updateBoundingLeaf(Object[] args) {
	BoundingLeafRetained bl = (BoundingLeafRetained)args[0];
	Object[] users = (Object[])(args[3]);
	bl.updateImmediateMirrorObject(args);
	// Now update all users of this bounding leaf object
	for (int i = 0; i < users.length; i++) {
	    LeafRetained mLeaf = (LeafRetained)users[i];
	    mLeaf.updateBoundingLeaf();
	}
    }

    void updateShape3D(Object[] args) {
	Shape3DRetained shape = (Shape3DRetained)args[0];
	shape.updateImmediateMirrorObject(args);
    }

    void updateOrientedShape3D(Object[] args) {
	OrientedShape3DRetained shape = (OrientedShape3DRetained)args[4];
	shape.updateImmediateMirrorObject(args);
    }

    void updateMorph(Object[] args) {
	MorphRetained morph = (MorphRetained)args[0];
	morph.updateImmediateMirrorObject(args);
    }


    void updateTransformChange() {
	int i,j;
 	Object[] nodes, nodesArr;
	BoundingLeafRetained bl;
	LightRetained ml;
        UnorderList arrList;
        int size;

	targets = universe.transformStructure.getTargetList();
	blUsers = universe.transformStructure.getBlUsers();

        // process misc environment nodes
        arrList = targets.targetList[Targets.ENV_TARGETS];
        if (arrList != null) {
            size = arrList.size();
            nodesArr = arrList.toArray(false);

            for (j = 0; j < size; j++) {
                nodes = (Object[])nodesArr[j];

                for (i = 0; i < nodes.length; i++) {
	    	    if (nodes[i] instanceof LightRetained) {
			ml = (LightRetained)nodes[i];
			ml.updateImmediateTransformChange();
			xformChangeList.add(nodes[i]);

	    	    } else if (nodes[i] instanceof FogRetained) {
			FogRetained mfog = (FogRetained) nodes[i];
			mfog.updateImmediateTransformChange();
			xformChangeList.add(nodes[i]);

	    	    } else if (nodes[i] instanceof AlternateAppearanceRetained){
			AlternateAppearanceRetained mAltApp =
					(AlternateAppearanceRetained) nodes[i];
			mAltApp.updateImmediateTransformChange();
			xformChangeList.add(nodes[i]);

	    	    } else if (nodes[i] instanceof BackgroundRetained) {
                	BackgroundRetained bg = (BackgroundRetained) nodes[i];
                	bg.updateImmediateTransformChange();

            	    } else if (nodes[i] instanceof ModelClipRetained) {
			ModelClipRetained mc = (ModelClipRetained) nodes[i];
			mc.updateImmediateTransformChange();
                    }
                }
            }
        }

        // process BoundingLeaf nodes
        arrList = targets.targetList[Targets.BLN_TARGETS];
        if (arrList != null) {
            size = arrList.size();
            nodesArr = arrList.toArray(false);
            for (j = 0; j < size; j++) {
                nodes = (Object[])nodesArr[j];
                for (i = 0; i < nodes.length; i++) {
                    bl = (BoundingLeafRetained)nodes[i];
                    bl.updateImmediateTransformChange();
                }
            }
        }

	// Now notify the list of all users of bounding leaves
	// to update its boundingleaf transformed region
	if (blUsers != null) {
	    for (i = 0; i < blUsers.size(); i++) {
		LeafRetained leaf = (LeafRetained) blUsers.get(i);
		leaf.updateBoundingLeaf();
	    }
	}
	targets = null;
	blUsers = null;
    }


    // The first element is TRUE, if alternate app is in effect
    // The second element return the appearance that should be used
    // Note , I can't just return null for app, then I don't know
    // if the appearance is null or if the alternate app in not
    // in effect
    Object[]  getInfluencingAppearance(RenderAtom ra, View view) {
	AlternateAppearanceRetained altApp = null;
	int i, j, k, n, nAltApp;;
	Bounds closestBounds;
        Bounds bounds;
	Object[] retVal;
	ArrayList globalAltApps;
	retVal = new Object[2];

	if (ra.geometryAtom.source.inBackgroundGroup) {
	    retVal[0] = Boolean.FALSE;
	    return retVal;
	}

	// Need to lock lockObj, since on a multi-processor
	// system with 2 views on a single universe, there might
	// be councurrent access
	synchronized(lockObj) {
	    nAltApp = 0;
	    bounds = ra.localeVwcBounds;

	    if (intersectedBounds.length < numberOfAltApps)
		intersectedBounds = new Bounds[numberOfAltApps];

	    if ((globalAltApps =(ArrayList)viewScopedAltAppearances.get(view)) != null) {
		nAltApp = processAltApps(globalAltApps, ra, nAltApp);
	    }
	    nAltApp = processAltApps(nonViewScopedAltAppearances, ra, nAltApp);


	    altApp = null;
	    if (nAltApp == 1)
		altApp = intersectedAltApps[0];
	    else if (nAltApp > 1) {
		closestBounds = bounds.closestIntersection(intersectedBounds);
		for (j= 0; j < nAltApp; j++) {
		    if (intersectedBounds[j] == closestBounds) {
			altApp = intersectedAltApps[j];
			break;
		    }
		}
	    }
	    if (altApp == null) {
		retVal[0] = Boolean.FALSE;
		return retVal;
	    } else {
		retVal[0] = Boolean.TRUE;
		retVal[1] = altApp.appearance;
		return retVal;
	    }
	}
    }

    // Called while holding lockObj lock
    int processAltApps(ArrayList globalAltApps, RenderAtom ra, int nAltApp) {
	int size = globalAltApps.size();
	AlternateAppearanceRetained altApp;
	int i, k, n;
        Bounds bounds = ra.localeVwcBounds;
	AlternateAppearanceRetained[] shapeScopedAltApp;


	if (size > 0) {
	    for (i = 0; i < size; i++) {
		altApp = (AlternateAppearanceRetained) globalAltApps.get(i);
		//		    System.err.println("altApp.region = "+altApp.region+" altApp.switchState.currentSwitchOn = "+altApp.switchState.currentSwitchOn+" intersect = "+altApp.region.intersect(ra.geometryAtom.vwcBounds));
		//		    System.err.println("altApp.isScoped = "+altApp.isScoped);
		// Note : There is no enable check for fog
		if (altApp.region != null && altApp.switchState.currentSwitchOn) {
		    if (altApp.region.intersect(bounds) == true) {
			n = ((Shape3DRetained)ra.geometryAtom.source).numAltApps;
			shapeScopedAltApp = ((Shape3DRetained)ra.geometryAtom.source).altApps;
			if (altApp.isScoped) {
			    for (k = 0; k < n; k++) {
				// then check if the light is scoped to
				// this group
				if (altApp == shapeScopedAltApp[k]) {

				    intersectedBounds[nAltApp] = altApp.region;
				    intersectedAltApps[nAltApp++] = altApp;
				    break;
				}
			    }
			}
			else {
			    intersectedBounds[nAltApp] = altApp.region;
			    intersectedAltApps[nAltApp++] = altApp;
			}
		    }
		}
	    }
	}
	return nAltApp;
    }

    void initViewSpecificInfo(J3dMessage m) {
	int[] keys = (int[])m.args[2];
	ArrayList vlists = (ArrayList)m.args[1];
	ArrayList vsgs = (ArrayList)m.args[0];
	if (vsgs != null) {
	    //	    System.err.println("===> non null Vsg");
	    int size = vsgs.size();
	    for (int i = 0; i < size; i++) {
		ViewSpecificGroupRetained v = (ViewSpecificGroupRetained)vsgs.get(i);
		ArrayList l = (ArrayList)vlists.get(i);
		int index = keys[i];
		//		System.err.println("v = "+v+" index = "+index+" l = "+l);
		v.cachedViewList.add(index, l);
		/*
		for (int k = 0; k <  v.cachedViewList.size(); k++) {
		    System.err.println("v = "+v+" k = "+k+" v.cachedViewList.get(k) = "+v.cachedViewList.get(k));
		}
		*/
	    }
	}
    }

    void clearViewSpecificInfo(J3dMessage m) {
	int[] keys = (int[])m.args[1];
	ArrayList vsgs = (ArrayList)m.args[0];
	if (vsgs != null) {
	    int size = vsgs.size();
	    for (int i = 0; i < size; i++) {
		ViewSpecificGroupRetained v = (ViewSpecificGroupRetained)vsgs.get(i);
		int index = keys[i];
		if (index == -1) {
		    int csize = v.cachedViewList.size();
		    for (int j = 0; j< csize; j++) {
			ArrayList l = (ArrayList)v.cachedViewList.get(j);
			l.clear();
		    }
		    v.cachedViewList.clear();
		}
		else {
		    ArrayList l = (ArrayList) v.cachedViewList.remove(index);
		    l.clear();
		}
	    }
	}
    }

    void updateViewSpecificGroupChanged(J3dMessage m) {
	int component = ((Integer)m.args[0]).intValue();
	Object[] objAry = (Object[])m.args[1];

	ArrayList ltList = null;
	ArrayList fogList = null;
	ArrayList mclipList = null;
	ArrayList altAppList = null;
	ArrayList bgList = null;
	ArrayList clipList = null;
	int idx;

	if (((component & ViewSpecificGroupRetained.ADD_VIEW) != 0) ||
	    ((component & ViewSpecificGroupRetained.SET_VIEW) != 0)) {
	    int i;
	    Object obj;
	    View view = (View)objAry[0];
	    ArrayList  vsgList = (ArrayList)objAry[1];
	    ArrayList leafList = (ArrayList)objAry[2];
	    int[] keyList = (int[])objAry[3];
	    int size = vsgList.size();
	    // Don't add null views

	    if (view != null) {
		for (i = 0; i < size; i++) {
		    ViewSpecificGroupRetained vsg = (ViewSpecificGroupRetained)vsgList.get(i);
		    int index = keyList[i];
		    vsg.updateCachedInformation(ViewSpecificGroupRetained.ADD_VIEW, view, index);

		}
		size = leafList.size();
		// Leaves is non-null only for the top VSG

		if (size > 0) {
		    // Now process the list of affected leaved
		    for( i = 0; i < size; i++) {
			obj = leafList.get(i);
			if (obj instanceof LightRetained) {
			    ((LightRetained)obj).isViewScoped = true;
			    numberOfLights++;
			    if (ltList == null) {
				if ((ltList = (ArrayList)viewScopedLights.get(view)) == null) {
				    ltList = new ArrayList();
				    viewScopedLights.put(view, ltList);
				}
			    }
			    ltList.add(obj);
			}
			if (obj instanceof FogRetained) {
			    ((FogRetained)obj).isViewScoped = true;
			    numberOfFogs++;
			    if (fogList == null) {
				if ((fogList= (ArrayList)viewScopedFogs.get(view)) == null) {
				    fogList = new ArrayList();
				    viewScopedFogs.put(view, fogList);
				}
			    }
			    fogList.add(obj);
			}
			if (obj instanceof ModelClipRetained) {
			    ((ModelClipRetained)obj).isViewScoped = true;
			    numberOfModelClips++;
			    if (mclipList == null) {
				if ((mclipList= (ArrayList)viewScopedModelClips.get(view)) == null) {
				    mclipList = new ArrayList();
				    viewScopedModelClips.put(view, mclipList);
				}
			    }
			    mclipList.add(obj);
			}
			if (obj instanceof AlternateAppearanceRetained) {
			    ((AlternateAppearanceRetained)obj).isViewScoped = true;
			    numberOfAltApps++;
			    if (altAppList == null) {
				if ((altAppList= (ArrayList)viewScopedAltAppearances.get(view)) == null) {
				    altAppList = new ArrayList();
				    viewScopedAltAppearances.put(view, altAppList);
				}
			    }
			    altAppList.add(obj);
			}
			if (obj instanceof ClipRetained) {
			    ((ClipRetained)obj).isViewScoped = true;
			    numberOfClips++;
			    if (clipList == null) {
				if ((clipList= (ArrayList)viewScopedClips.get(view)) == null) {
				    clipList = new ArrayList();
				    viewScopedClips.put(view, clipList);
				}
			    }
			    clipList.add(obj);
			}
			if (obj instanceof BackgroundRetained) {
			    ((BackgroundRetained)obj).isViewScoped = true;
			    numberOfBgs++;
			    if (bgList == null) {
				if ((bgList= (ArrayList)viewScopedBackgrounds.get(view)) == null) {
				    bgList = new ArrayList();
				    viewScopedBackgrounds.put(view, bgList);
				}
			    }
			    bgList.add(obj);
			}
		    }
		    if (numberOfLights > retlights.length)
			retlights = new LightRetained[numberOfLights];
		    if (intersectedFogs.length < numberOfFogs)
			intersectedFogs = new FogRetained[numberOfFogs];
		    if (intersectedAltApps.length < numberOfAltApps)
			intersectedAltApps = new AlternateAppearanceRetained[numberOfAltApps];
		    if (intersectedBacks.length < numberOfBgs)
			intersectedBacks = new BackgroundRetained[numberOfBgs];
		    if (intersectedClips.length < numberOfClips)
			intersectedClips = new ClipRetained[numberOfClips];
		    if (intersectedModelClips.length < numberOfModelClips)
			intersectedModelClips = new ModelClipRetained[numberOfModelClips];
		}
	    }
	}
	if (((component & ViewSpecificGroupRetained.REMOVE_VIEW) != 0)||
	    ((component & ViewSpecificGroupRetained.SET_VIEW) != 0)) {
	    int i;
	    Object obj;
	    ArrayList  vsgList;
	    ArrayList leafList;
	    int[] keyList;
	    View view;

	    if ((component & ViewSpecificGroupRetained.REMOVE_VIEW) != 0) {
		view = (View)objAry[0];
		vsgList = (ArrayList)objAry[1];
		leafList = (ArrayList)objAry[2];
		keyList = (int[])objAry[3];
	    }
	    else {
		view = (View)objAry[4];
		vsgList = (ArrayList)objAry[5];
		leafList = (ArrayList)objAry[6];
		keyList = (int[])objAry[7];
	    }
	    // Don't add null views
	    if (view != null) {
		int size = vsgList.size();
		for (i = 0; i < size; i++) {
		    ViewSpecificGroupRetained vsg = (ViewSpecificGroupRetained)vsgList.get(i);
		    int index = keyList[i];
		    vsg.updateCachedInformation(ViewSpecificGroupRetained.REMOVE_VIEW, view, index);

		}
		size = leafList.size();
		// Leaves is non-null only for the top VSG
		if (size > 0) {
		    // Now process the list of affected leaved
		    for( i = 0; i < size; i++) {
			obj =  leafList.get(i);
			if (obj instanceof LightRetained) {
			    ((LightRetained)obj).isViewScoped = false;
			    numberOfLights--;
			    if (ltList == null) {
				ltList = (ArrayList)viewScopedLights.get(view);
			    }
			    ltList.remove(obj);
			}
			if (obj instanceof FogRetained) {
			    ((FogRetained)obj).isViewScoped = false;
			    numberOfFogs--;
			    if (fogList == null) {
				fogList = (ArrayList)viewScopedFogs.get(view);
			    }
			    fogList.remove(obj);
			}
			if (obj instanceof ModelClipRetained) {
			    ((ModelClipRetained)obj).isViewScoped = false;
			    numberOfModelClips--;
			    if (mclipList == null) {
				mclipList = (ArrayList)viewScopedModelClips.get(view);
			    }
			    mclipList.remove(obj);
			}
			if (obj instanceof AlternateAppearanceRetained) {
			    ((AlternateAppearanceRetained)obj).isViewScoped = false;
			    numberOfAltApps--;
			    if (altAppList == null) {
				altAppList = (ArrayList)viewScopedAltAppearances.get(view);
			    }
			    altAppList.remove(obj);
			}
			if (obj instanceof ClipRetained) {
			    ((ClipRetained)obj).isViewScoped = false;
			    numberOfClips--;
			    if (clipList == null) {
				clipList = (ArrayList)viewScopedClips.get(view);
			    }
			    clipList.remove(obj);
			}
			if (obj instanceof BackgroundRetained) {
			    ((BackgroundRetained)obj).isViewScoped = false;
			    numberOfBgs++;
			    if (bgList == null) {
				bgList = (ArrayList)viewScopedBackgrounds.get(view);
			    }
			    bgList.remove(obj);
			}
		    }

		    // If there are no more lights scoped to the view,
		    // remove the mapping
		    if (ltList != null && ltList.size() == 0)
			viewScopedLights.remove(view);
		    if (fogList != null && fogList.size() == 0)
			viewScopedFogs.remove(view);
		    if (mclipList != null && mclipList.size() == 0)
			viewScopedModelClips.remove(view);
		    if (altAppList != null && altAppList.size() == 0)
			viewScopedAltAppearances.remove(view);
		    if (clipList != null && clipList.size() == 0)
			viewScopedClips.remove(view);
		    if (bgList != null && bgList.size() == 0)
			viewScopedBackgrounds.remove(view);
		}
	    }
	}

    }

    boolean isLightScopedToThisView(Object obj, View view) {
	LightRetained light = (LightRetained)obj;
	if (light.isViewScoped) {
	    ArrayList l = (ArrayList)viewScopedLights.get(view);
	    // If this is a scoped lights, but has no views then ..
	    if (l == null || !l.contains(light))
		return false;
	}
	return true;
    }

    boolean isFogScopedToThisView(Object obj, View view) {
	FogRetained fog = (FogRetained)obj;
	if (fog.isViewScoped) {
	    ArrayList l = (ArrayList)viewScopedFogs.get(view);
	    // If this is a scoped fog, but has no views then ..
	    if (l ==null || !l.contains(fog))
		return false;
	}
	return true;
    }

    boolean isAltAppScopedToThisView(Object obj, View view) {
	AlternateAppearanceRetained altApp = (AlternateAppearanceRetained)obj;
	if (altApp.isViewScoped) {
	    ArrayList l = (ArrayList)viewScopedAltAppearances.get(view);
	    // If this is a scoped altapp, but has no views then ..
	    if (l == null || !l.contains(altApp))
		return false;
	}
	return true;
    }

    boolean isBgScopedToThisView(Object obj, View view) {
	BackgroundRetained bg = (BackgroundRetained)obj;
	if (bg.isViewScoped) {
	    ArrayList l = (ArrayList)viewScopedBackgrounds.get(view);
	    // If this is a scoped bg, but has no views then ..
	    if (l == null || !l.contains(bg))
		return false;
	}
	return true;
    }

    boolean isClipScopedToThisView(Object obj, View view) {
	ClipRetained clip = (ClipRetained)obj;
	if (clip.isViewScoped) {
	    ArrayList l = (ArrayList)viewScopedClips.get(view);
	    // If this is a scoped clip, but has no views then ..
	    if (l == null || !l.contains(clip))
		return false;
	}
	return true;
    }


    boolean isMclipScopedToThisView(Object obj, View view) {
	ModelClipRetained mclip = (ModelClipRetained)obj;
	if (mclip.isViewScoped) {
	    ArrayList l = (ArrayList)viewScopedModelClips.get(view);
	    // If this is a scoped mclip, but has no views then ..
	    if (l == null || !l.contains(mclip))
		return false;
	}
	return true;
    }

    void cleanup() {}
}


