/*
 * $RCSfile$
 *
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * The RenderBin is a structure that optimizes rendering by doing efficient
 * state sorting of objects to be rendered.
 */

class RenderBin extends J3dStructure  implements ObjectUpdate {

/**
 * The list of RenderAtoms
 */
ArrayList<RenderAtom> renderAtoms = new ArrayList<RenderAtom>(5);

/**
 * A couple ArrayLists used during light Processing
 */
ArrayList<J3dMessage> lightMessageList = new ArrayList<J3dMessage>(5);

    // Messges retrieved when a message is sent to RenderingEnv Structure
    J3dMessage[] m;

/**
 * List of renderMolecules that are soleUser have to do a 2 pass, first update
 * values then sort based on equivalent material
 */
ArrayList<RenderMolecule> rmUpdateList = new ArrayList<RenderMolecule>();
ArrayList<AttributeBin> aBinUpdateList = new ArrayList<AttributeBin>();

    /**
     * List of ShaderBin that are soleUser that
     * needs to have its components updated @updateObject time
     */
    ArrayList sBinUpdateList = new ArrayList();

    /**
     * List of TextureBin that are soleUser that
     * needs to have its components updated @updateObject time
     */
    ArrayList tbUpdateList = new ArrayList();

    /**
     * List of Bins that are soleUser that have new renderAtom
     * added into, which requires a pre-update screening to
     * check if any of its node component changes could have been
     * missed because the changes happen when all the render atoms
     * are temporarily removed from the bin.
     */
    ArrayList updateCheckList = new ArrayList();

    /**
     * The number of lights supported by the underlying context.
     */
    int maxLights;

    /**
     * The opaque objects
     */
    LightBin opaqueBin = null;

    /**
     * OpaqueBins to be added for the next frame
     */
    LightBin addOpaqueBin = null;

    // This is a list of textureBins to be rendered, if the transpSortPolicy
    // is NONE, otherwise, if the transpSortPolicy is geometry, then
    // this is the list of renderAtoms to be rendered
    ArrayList allTransparentObjects = new ArrayList(5);

    TransparentRenderingInfo  transparentInfo;

    /**
     * List of RenderAtoms whose postion have changed - only used for
     * depth sorted transparency
     */
    ArrayList positionDirtyList = new ArrayList(5);

    /**
     * Used when ColoringAttributes is null
     */
    Color3f white = new Color3f(1.0f, 1.0f, 1.0f);

    /**
     * Used when Background is null
     */
    Color3f black = new Color3f(0.0f, 0.0f, 0.0f);

    /**
     * The backgound color data.
     */
    BackgroundRetained background = new BackgroundRetained();

    /**
     * The view platform transforms.
     */
    // used for rendering - lights and fog modelling
    Transform3D vworldToVpc = new Transform3D();

    // used for updating vpSchedSphere
    Transform3D vpcToVworld = new Transform3D();

    /**
     * Two bounding spheres to track the scheduling region of
     * the view platform.
     */
    BoundingSphere vpSchedSphereInVworld = new BoundingSphere();

    /**
     *  To cache the view frustum bounding box.
     */
    BoundingBox viewFrustumBBox = new BoundingBox();
    BoundingBox canvasFrustumBBox = new BoundingBox();

    /**
     * To ensure that vpcToVworld is valid (not null) for the first pass
     */
    boolean afterFirst = false;

    /**
     * back clip distance in vworld
     */
    double backClipDistanceInVworld;

    boolean backClipActive = false;

    /**
     * These variables control when compaction occurs
     */
    int frameCount = 0;
    int frameCountCutoff = 150;
    int notVisibleCount = 75;
    long removeCutoffTime = -1;

    /**
     * variables to process transform messages
     */
    boolean transformMsg = false;
    UpdateTargets targets = null;
    ArrayList blUsers = null;

    /**
     * The View for this render bin
     */
    View view = null;

    private Comparator transparencySortComparator = null;

    private ArrayList toBeAddedTextureResourceFreeList = new ArrayList(5);
    private ArrayList displayListResourceFreeList = new ArrayList(5);

    // a list of top level OrderedGroups
    ArrayList orderedBins = new ArrayList(5);

    // List of changed elements in the environment that needs to
    // be reloaded
    ArrayList changedLts = new ArrayList(5);
    ArrayList changedFogs = new ArrayList(5);
    ArrayList changedModelClips = new ArrayList(5);

    // Flag to indicate whether the canvas should be marked
    static int REEVALUATE_LIGHTS  =  0x1;
    static int REEVALUATE_FOG     =  0x2;
    static int REEVALUATE_MCLIP   =  0x4;
    static int REEVALUATE_ALL_ENV  = REEVALUATE_LIGHTS | REEVALUATE_FOG | REEVALUATE_MCLIP;
    int envDirty = 0;


    private boolean reEvaluateBg = true;
    private boolean reloadBgTexture = true;

    boolean reEvaluateClip = true;

    boolean reEvaluateSortMode = false;


    // list of renderMolecule
    // RenderBin will not reused in two different universe, so it is
    // safe to pass null in last parameters in new IndexedUnorderSet()
    IndexedUnorderSet renderMoleculeList =
	new IndexedUnorderSet(RenderMolecule.class,
			      RenderMolecule.RENDER_MOLECULE_LIST, null);

    // List of renderAtoms that have a shared dlist (due to geo.refCount > 1)
    // Fix for Issue 5: change this to a Set rather than a list to
    // avoid duplicates entried
    Collection sharedDList = new HashSet();

    ArrayList dirtyRenderMoleculeList = new ArrayList(5);


    /**
     * ArrayList of objects to be updated
     */
    ArrayList objUpdateList = new ArrayList(5);

    ArrayList raLocaleVwcBoundsUpdateList = new ArrayList(5);

    /**
     * remove the bins first before adding them to new ones
     */
    IndexedUnorderSet removeRenderAtomInRMList =
	new IndexedUnorderSet(RenderMolecule.class,
			      RenderMolecule.REMOVE_RENDER_ATOM_IN_RM_LIST, null);


    /**
     * list of affect OrderedGroups with childIndexOrder changed.
     */
    ArrayList ogCIOList = new ArrayList(5);

    /**
     * list of ordered bins from which orderedCollection are added/removed
     */
    ArrayList obList = new ArrayList(5);

    /**
     * Ordered Bin processing
     */
    ArrayList orderedBinsList = new ArrayList(5);
    ArrayList toBeAddedBinList = new ArrayList(5);

    /**
     * arraylist of geometry that should be locked to ensure
     * that the same snapshot of the geometry is rendered
     * across all canvases
     */
    ArrayList lockGeometryList = new ArrayList(5);


    /**
     * arraylist of dlist that will be rebuilt
     */
    ArrayList dlistLockList = new ArrayList(5);

    // Background node that contains geometry
    BackgroundRetained geometryBackground = null;

    // background geometry processing
    LightBin bgOpaqueBin = null;
    LightBin bgAddOpaqueBin = null;
    ArrayList bgOrderedBins = new ArrayList(5);
    TransparentRenderingInfo  bgTransparentInfo;


    // vworldToVpc for background geometry
    Transform3D infVworldToVpc = new Transform3D();

    // true if vpcToVworld has been modified
    boolean vpcToVworldDirty = true;

    // current active background
    BackgroundRetained currentActiveBackground = new BackgroundRetained();

    // Flag to indicate that alternate app is dirty
    boolean altAppearanceDirty = true;


    // List of node components that need special processing, due to
    // extensions
    ArrayList nodeComponentList = new ArrayList(5);


    // List of node components ***for this frame*** that need special
    // processing due to extension
    ArrayList newNodeComponentList = new ArrayList(5);
    ArrayList removeNodeComponentList = new ArrayList(5);
    ArrayList dirtyNodeComponentList = new ArrayList(5);

    ArrayList textureBinList = new ArrayList(5);

    /**
     * arraylist of refernce geometry that should be locked when transparency
     * is on, so that we can make a mirror copy of the colors safely
     */
    ArrayList dirtyReferenceGeomList = new ArrayList(5);

    // list of all Oriented RenderAtoms
    ArrayList orientedRAs = new ArrayList(5);

    // list of Oriented RenderAtoms whose orientedTransforms require update
    ArrayList dirtyOrientedRAs = new ArrayList(5);

    // Cached copy of dirty oriented RAs to be updated in MasterControl
    ArrayList cachedDirtyOrientedRAs = null;

    // list of offScreen message that
    ArrayList offScreenMessage = new ArrayList(5);

    // Vector used for locale translation
    Vector3d localeTranslation = new Vector3d();

    // Separate dlists that were added/removed in this snapshot
    private HashSet addDlist = new HashSet();
    private HashSet removeDlist = new HashSet();

    // Separate dlists per rinfo that were added/removed in this snapshot
    ArrayList addDlistPerRinfo = new ArrayList(5);
    ArrayList removeDlistPerRinfo = new ArrayList(5);

    Locale locale = null;

    // Set to true if locale changes as part of UPDATE_VIEW message
    boolean localeChanged = false;


    // Cached copy to be used by all RenderMolecules
    DisplayListRenderMethod dlistRenderMethod = null;

    // Need to query BHTree again with visibility policy change
    boolean reactivateView = false;

    /**
     * A flag indicates that the cached visible GeometryAtoms for this RenderBin might
     * be invalid.
     */
    private boolean visGAIsDirty = false;

    /**
     * A flag indicates that a visibility query to the GeometryStructure is needed.
     */
    private boolean visQuery = false;

    // Temporary dirtylist
    ArrayList dirtyList = new ArrayList(5);

    // Transaprency sort mode
    int transpSortMode = View.TRANSPARENCY_SORT_NONE;
    int cachedTranspSortMode = View.TRANSPARENCY_SORT_NONE;


    // Temporary dirtylist
    private LinkedHashSet dirtyDepthSortRenderAtom = new LinkedHashSet();
    private int numDirtyTinfo = 0;

    // Eye position in vworld
    Point3d eyeInVworld = new Point3d();
    // Number of RenderAtomListInfo in the depthSortedList
    int nElements = 0;



    /**
     * Constructs a new RenderBin
     */
    RenderBin(VirtualUniverse u, View v) {
	super(u, J3dThread.UPDATE_RENDER);
	vworldToVpc.setIdentity();
	universe = u;
	view = v;
	transpSortMode = v.transparencySortingPolicy;
	cachedTranspSortMode = v.transparencySortingPolicy;
	maxLights = VirtualUniverse.mc.maxLights;
	ViewPlatform vp = view.getViewPlatform();
	if (vp != null) {
	    locale = ((ViewPlatformRetained) (vp.retained)).locale;
	}
	dlistRenderMethod = (DisplayListRenderMethod)
	    VirtualUniverse.mc.getDisplayListRenderMethod();
    }

    /**
     * updateObject
     */
    public void updateObject() {
	int i, j, k;
	RenderAtomListInfo ra;
	LightBin tmp;
	float radius;
	BackgroundRetained bg;
	ObjectUpdate ob;
	OrderedBin orderBin;
	TextureRetained tex;
	Integer texIdObj;
	int size;

	//	System.err.println("dirtyRenderMoleculeList.size = "+dirtyRenderMoleculeList.size());
	//	System.err.println("reEvaluateBg = "+reEvaluateBg);
	//	System.err.println("reEvaluateClip = "+reEvaluateClip);
	//	System.err.println("<========+End All Cached Values===========>");
	// Add the new lightBins that have been created
	//	System.err.println("objUpdateList.size = "+objUpdateList.size());
	//	System.err.println("addOpaqueBin = "+addOpaqueBin);
	//	System.err.println("opaqueBin = "+opaqueBin);

	// List of renderMolecule from which renderAtoms have been removed
        size = removeRenderAtomInRMList.size();
	if (size > 0) {
            RenderMolecule[] rmArr = (RenderMolecule[])
		removeRenderAtomInRMList.toArray(false);
            for (i=0 ; i<size; i++) {
                rmArr[i].updateRemoveRenderAtoms();
            }
	}

	// Add any OGs that need to be added to this frame
	// List of Ordered Groups that have been removed

	size = obList.size();
	if ( size > 0) {
	    for (i = 0 ; i < size; i++) {
		orderBin = (OrderedBin)obList.get(i);
		orderBin.addRemoveOrderedCollection();
	    }
	}

	size = ogCIOList.size();
	if(size > 0) {
	    J3dMessage m;
	    for(i=0; i<size; i++) {
		m = (J3dMessage) ogCIOList.get(i);

		switch(m.type) {
		case J3dMessage.ORDERED_GROUP_TABLE_CHANGED:
		    OrderedGroupRetained og = (OrderedGroupRetained)m.args[3];
		    if(og != null) {
			og.childIndexOrder = ((int[])m.args[4]);
		    }
		    break;


		case J3dMessage.ORDERED_GROUP_INSERTED:
                case J3dMessage.ORDERED_GROUP_REMOVED:
		    if(m.args[3] != null) {
			Object[] ogArr = (Object[]) m.args[3];
			Object[] ogTableArr = (Object[]) m.args[4];
			for(j=0; j<ogArr.length; j++) {
			    if(ogArr[j] != null) {
				((OrderedGroupRetained)ogArr[j]).childIndexOrder =
				    ((int[])ogTableArr[j]);
			    }
			}
		    }

		    break;
		}
		m.decRefcount();
	    }
	}


	if (addOpaqueBin != null) {

	    if (opaqueBin != null) {
		tmp = opaqueBin;
		while (tmp.next != null) {
		    tmp = tmp.next;
		}
		addOpaqueBin.prev = tmp;
		tmp.next = addOpaqueBin;
	    }
	    else {
		opaqueBin = addOpaqueBin;
	    }
	}

        if (bgAddOpaqueBin != null) {
            if (bgOpaqueBin != null) {
		tmp = bgOpaqueBin;
		while (tmp.next != null) {
		    tmp = tmp.next;
		}
                bgAddOpaqueBin.prev = tmp;
                tmp.next = bgAddOpaqueBin;
            }
            else {
                bgOpaqueBin = bgAddOpaqueBin;
            }
        }

	size = orderedBinsList.size();
	if (size > 0 ) {

	    for (i = 0; i < size; i++) {
		ArrayList obs= (ArrayList) orderedBinsList.get(i);
		ArrayList list = (ArrayList) toBeAddedBinList.get(i);

		int lSize = list.size();
		for (j = 0; j < lSize; j++) {
		    obs.add(list.get(j));
		}
	    }
	}


	size = raLocaleVwcBoundsUpdateList.size();
        if ( size > 0) {
            RenderAtom renderAtom;
            for (i = 0; i < size; i++) {
                renderAtom = (RenderAtom)raLocaleVwcBoundsUpdateList.get(i);
                renderAtom.updateLocaleVwcBounds();
            }
        }

	if ((size = aBinUpdateList.size()) > 0) {
	    for (i = 0; i < size; i++) {
			AttributeBin abin = aBinUpdateList.get(i);
		abin.updateNodeComponent();
	    }
	}

	if ((size = sBinUpdateList.size()) > 0) {
	    for (i = 0; i < size; i++) {
		ShaderBin sbin = (ShaderBin)sBinUpdateList.get(i);
		sbin.updateNodeComponent();
	    }
	}

	// Update the sole user TextureBins.
	if (tbUpdateList.size() > 0) {
	    TextureBin tb;
	    size = tbUpdateList.size();
	    for (i = 0; i < size; i++) {
		tb = (TextureBin) tbUpdateList.get(i);
		tb.updateNodeComponent();
	    }

	    // do another pass to re-sort TextureBin based on the
	    // texture in the first texture unit state
	    for (i = 0; i < size; i++) {
		tb = (TextureBin) tbUpdateList.get(i);
		// Bug Id : 4701430 - Have to be sure tb.shaderBin is
		// not equal to null. This is a temporary fix for j3d1.3.
		if (((tb.tbFlag & TextureBin.RESORT) != 0) &&
		    (tb.shaderBin != null)) {

		    tb.shaderBin.reInsertTextureBin(tb);
		    tb.tbFlag &= ~TextureBin.RESORT;
		}
	    }
	}


	// Update the soleUser node  components first
	// This way material equivalence during insertion
	// of new RMs is based on the updated ones
	if ((size = rmUpdateList.size()) > 0) {
	    for (i = 0; i < size; i++) {
			RenderMolecule rm = rmUpdateList.get(i);

		boolean changeLists = rm.updateNodeComponent();
		// If an existing rm went from opaque to transparent or vice-versa
		// and has not been removed, then switch the RM
		if (changeLists && rm.textureBin != null) {
		    rm.textureBin.changeLists(rm);
		}
	    }
		for (i = 0; i < size; i++) {
			rmUpdateList.get(i).reEvaluateEquivalence();
		}
	}



	size = objUpdateList.size();
	if ( size > 0) {
	    for (i = 0; i < size; i++) {
		ob = (ObjectUpdate)objUpdateList.get(i);
		ob.updateObject();
	    }
	}

	size = dirtyReferenceGeomList.size();
	if ( size > 0) {
	    GeometryArrayRetained   geo;
	    Canvas3D canvases[] = view.getCanvases();

	    for (i = 0; i < size; i++) {
		geo = (GeometryArrayRetained) dirtyReferenceGeomList.get(i);
		// Evaluate the nodeComponentList for all the canvases
		geo.geomLock.getLock();
		j = 0;
		// Do the setup  only once{if necessary} for each geometry
		boolean found = false;
		while(j < canvases.length && !found) {
		    if ((canvases[j].extensionsSupported & Canvas3D.SUN_GLOBAL_ALPHA) == 0) {
			if ((geo.vertexFormat & GeometryArray.INTERLEAVED) != 0) {
			    geo.setupMirrorInterleavedColorPointer(true);
			    found = true;
			}
			else {
			    geo.setupMirrorColorPointer((geo.vertexType & GeometryArrayRetained.COLOR_DEFINED),true);
			    found = true;
			}
		    }
		    j++;
		}
		geo.geomLock.unLock();

	    }

	}

	if (reEvaluateBg) {
	    setBackground(currentActiveBackground);
	}

	size = textureBinList.size();
//System.err.println("textureBinList.size= " + size);
	if (size > 0) {
	    Canvas3D canvasList[][] = view.getCanvasList(false);
	    Canvas3D cv;
	    boolean useSharedCtx = false;
	    TextureRetained texture;

	    // do a quick check to see if there is any canvas using
	    // shared context
	    for (j = 0; j < canvasList.length && !useSharedCtx; j++) {
		cv = canvasList[j][0];
		if (cv.useSharedCtx) {
		    useSharedCtx = true;
		}
	    }

	    for (int m = 0; m <size; m++) {
		k = 0;
		TextureBin tb = (TextureBin) textureBinList.get(m);
		tb.tbFlag |= TextureBin.ON_RENDER_BIN_LIST;

		if (tb.texUnitState == null)
		    continue;

		for (i = 0; i < tb.texUnitState.length; i++) {
		    if (tb.texUnitState[i] != null &&
			tb.texUnitState[i].texture != null) {

			texture = tb.texUnitState[i].texture;

			// for all the textures in this texture bin list that
			// need to be reloaded, add the textures to the
			// corresponding resource reload list if the
			// resource uses shared context,
			// so that the texture can be reloaded up front, and
			// we don't need to do make context current for
			// each texture reload. Make Context current isn't
			// cheap.

			if (useSharedCtx) {
			    synchronized (texture.resourceLock) {
				for (j = 0; j < canvasList.length; j++) {
				    cv = canvasList[j][0];
				    if (cv.useSharedCtx &&
					cv.screen.renderer != null &&
					((cv.screen.renderer.rendererBit &
					  (texture.resourceCreationMask |
					   texture.resourceInReloadList)) == 0)) {

					cv.screen.renderer.textureReloadList.add(
										 texture);

					texture.resourceInReloadList |=
					    cv.screen.renderer.rendererBit;
				    }
				}
			    }
			}
		    }
		}
	    }
	}

	size = newNodeComponentList.size();
	if ( size > 0) {
//System.err.println("newNodeComponentlist.size= " + size);
	    Canvas3D canvases[] = view.getCanvases();
	    for (i = 0; i < size; i++) {
                // Evaluate the nodeComponentList for all the canvases
                ImageComponentRetained nc = (ImageComponentRetained)newNodeComponentList.get(i);
                if (nc.isByReference()) {
                    nc.geomLock.getLock();
                    for (j = 0; j <canvases.length; j++) {
                        // If the context is null, then the extension
                        // will be evaluated during context creation in
                        // the renderer
                        if (canvases[j].ctx != null) {
                            nc.evaluateExtensions(canvases[j]);
                        }
                    }
                    nc.geomLock.unLock();
                } else {
                    for (j = 0; j <canvases.length; j++) {
                        // If the context is null, then the extension
                        // will be evaluated during context creation in
                        // the renderer
                        if (canvases[j].ctx != null) {
                            nc.evaluateExtensions(canvases[j]);
                        }
                    }
                }
                nodeComponentList.add(nc);
            }
	}

	size = removeNodeComponentList.size();
	if ( size > 0) {
	    for (i = 0; i < size; i++) {
		nodeComponentList.remove(removeNodeComponentList.get(i));
	    }
	}


	// reevaluate dirty node component
	size = dirtyNodeComponentList.size();
	if (size > 0) {
            Canvas3D canvases[] = view.getCanvases();
            for (i = 0; i < size; i++) {
                // Evaluate the nodeComponentList for all the canvases
                ImageComponentRetained nc =
			(ImageComponentRetained)dirtyNodeComponentList.get(i);
                if (nc.isByReference()) {
                    nc.geomLock.getLock();
                    for (j = 0; j <canvases.length; j++) {
                        // If the context is null, then the extension
                        // will be evaluated during context creation in
                        // the renderer
                        if (canvases[j].ctx != null) {
                            nc.evaluateExtensions( canvases[j]);
                        }
                    }
                    nc.geomLock.unLock();
                }
                else {
		    for (j = 0; j <canvases.length; j++) {
		        // If the context is null, then the extension
		        // will be evaluated during context creation in
		        // the renderer
		        if (canvases[j].ctx != null) {
			    nc.evaluateExtensions(canvases[j]);
		        }
		    }
                }
            }
	}


	if (reEvaluateClip) {
	    double[] retVal = null;
	    if ((retVal = universe.renderingEnvironmentStructure.backClipDistanceInVworld(vpSchedSphereInVworld, view)) != null) {
		backClipDistanceInVworld = retVal[0];
		backClipActive = true;
	    }
	    else {
		backClipActive = false;
	    }
	    view.vDirtyMask |= View.CLIP_DIRTY;
	}

        // Issue 113 - multiScreen no longer used
//	multiScreen = ((view.getScreens()).length > 1);

	// renderBin is ready now, so send the offScreen message
	size = offScreenMessage.size();
	if ( size > 0) {
	    J3dMessage m;
	    for (i=size-1; i>=0; i--) {
		m = (J3dMessage) offScreenMessage.get(i);
		m.threads = J3dThread.RENDER_THREAD;
		((Canvas3D)m.args[0]).screen.renderer.rendererStructure.addMessage(m);

		// the above call will increment the reference count again
		m.decRefcount();
	    }
	}

	// called from renderBin when there are dirtyOrientedRAs
	// This routin cache the dirtyOrintedRAs to be updated
	// by mastercontrol
	if (dirtyOrientedRAs.size() > 0) {
	    // Keep a copy to be handled by mastercontrol
	    cachedDirtyOrientedRAs = (ArrayList)dirtyOrientedRAs.clone();
	}
	boolean sortAll = false;
	if (reEvaluateSortMode && transpSortMode != cachedTranspSortMode) {
	    convertTransparentRenderingStruct(transpSortMode, cachedTranspSortMode);
	    transpSortMode = cachedTranspSortMode;
	    if (transpSortMode == View.TRANSPARENCY_SORT_GEOMETRY) {
		if (transparentInfo != null){
		    sortAll = true;
		}
	    }
	}

	if (vpcToVworldDirty) {
	    vworldToVpc.invert(vpcToVworld);
	    // Have the send down the lights, so set the canvas
	    // lightbin to null
	    Canvas3D canvases[] = view.getCanvases();
	    for (i = 0; i < canvases.length; i++) {
		canvases[i].lightBin = null;
	    }
	    if (canvases.length > 0) {
		Transform3D xform;
		canvases[0].getCenterEyeInImagePlate(eyeInVworld);
		// xform is imagePlateToLocal
		xform = canvases[0].canvasViewCache.getImagePlateToVworld();
		xform.transform(eyeInVworld);
	    }
	    if (transpSortMode == View.TRANSPARENCY_SORT_GEOMETRY && transparentInfo != null) {
		//		System.err.println("sortAll 1");
		sortAll = true;
	    }
	}
	size = dirtyDepthSortRenderAtom.size();


	if (sortAll || size > 0) {
	    int tsize = allTransparentObjects.size();

	    double zVal;
	    for (i = 0; i < tsize; i++) {
		RenderAtom renderAtom = (RenderAtom)allTransparentObjects.get(i);
		for (k = 0; k < renderAtom.rListInfo.length; k++) {
		    if (renderAtom.rListInfo[k].geometry() == null)
			continue;
		    zVal = renderAtom.geometryAtom.centroid[k].distanceSquared(eyeInVworld);
		    renderAtom.parentTInfo[k].zVal = zVal;
		    renderAtom.parentTInfo[k].geometryAtom = renderAtom.geometryAtom;
		}
	    }

	    // Check to see if a majority of the transparent Objects have changed
	    // If less than 66% of all transparentStructs are dirty
	    // then, remove and insert, otherwise resort everything


	    if (size  > 0 &&  1.5f * numDirtyTinfo >  nElements) {
		// System.err.println("sortAll 3, size = "+size);
		sortAll = true;
	    }

	    if (size > 0) {
		TransparentRenderingInfo dirtyList = null, rList;
		Iterator dirtyDepthSortIterator = dirtyDepthSortRenderAtom.iterator();
		while (dirtyDepthSortIterator.hasNext()) {
		    RenderAtom renderAtom = (RenderAtom)dirtyDepthSortIterator.next();
		    if (!renderAtom.inRenderBin())
			continue;
		    renderAtom.dirtyMask &= ~RenderAtom.IN_SORTED_POS_DIRTY_TRANSP_LIST;
		    if (!sortAll) {
			dirtyList = collectDirtyTRInfo(dirtyList, renderAtom);
		    }
		}

		if (dirtyList != null) {
		    // System.err.println("====> sort Some");
		    dirtyList = depthSortAll(dirtyList);
		    // Now merge the newly sorted list with the old one
		    transparentInfo = mergeDepthSort(transparentInfo, dirtyList);
		}
	    }
	    // Sort all the transparent renderAtoms
	    if (sortAll) {
		transparentInfo = depthSortAll(transparentInfo);
	    }
	}

	// Remove entries that are found on both the add and remove lists
	if (addDlist.size() > 0 && removeDlist.size() > 0) {
	    RenderAtomListInfo arr[] = new RenderAtomListInfo[addDlist.size()];
	    arr = (RenderAtomListInfo []) addDlist.toArray(arr);
	    for (i = 0; i < arr.length; i++) {
		if (removeDlist.contains(arr[i])) {
		    addDlist.remove(arr[i]);
		    removeDlist.remove(arr[i]);
		}
	    }
	}

	if (addDlist.size() > 0 || removeDlist.size() > 0) {
	    Canvas3D canvasList[][] = view.getCanvasList(false);
	    Canvas3D cv;
	    ArrayList rlist = new ArrayList(5);

	    for (i = 0; i < canvasList.length; i++) {
		cv = canvasList[i][0];
		if (cv.useSharedCtx) {
		    // Do this only once per renderer for this view
		    if (!rlist.contains(cv.screen.renderer)) {
			rlist.add(cv.screen.renderer);
			updateDlistRendererResource(cv.screen.renderer);
		    }
		} else {
		    updateDlistCanvasResource(canvasList[i]);
		}
	    }

	}



	if (dirtyRenderMoleculeList.size() > 0 ||
	    addDlistPerRinfo.size() > 0 ||
	    removeDlistPerRinfo.size() > 0 ||
	    displayListResourceFreeList.size() > 0 ||
	    toBeAddedTextureResourceFreeList.size() > 0 ) {

	    Canvas3D canvasList[][] = view.getCanvasList(false);
	    Canvas3D cv;

	    for (i = 0; i < canvasList.length; i++) {
		cv = canvasList[i][0];
		if (cv.useSharedCtx && (cv.screen.renderer != null)) {
		    updateRendererResource(cv.screen.renderer);
		} else {
		    updateCanvasResource(canvasList[i]);
		}
	    }

	    Integer id;
	    size = displayListResourceFreeList.size();
	    for (i = 0; i < size; i++) {
		id = (Integer)displayListResourceFreeList.get(i);
		VirtualUniverse.mc.freeDisplayListId(id);
	    }

	    // lock list of dlist
	    // XXXX: Instead of copying could we keep 2 arrays
	    // and just toggle?
	    size = dirtyRenderMoleculeList.size();
	    for (i = 0; i < size; i++) {
			RenderMolecule rm = (RenderMolecule)dirtyRenderMoleculeList.get(i);
		rm.onUpdateList = 0;
		ra = rm.primaryRenderAtomList;
		while (ra != null) {
		    dlistLockList.add(ra.geometry());
		    ra = ra.next;
		}
	    }
	    size = addDlistPerRinfo.size();
	    for (i = 0; i < size; i++) {
		ra = (RenderAtomListInfo)addDlistPerRinfo.get(i);
		if (ra.geometry() != null) {
		    dlistLockList.add(ra.geometry());
		}
	    }

	}

	clearAllUpdateObjectState();
	/*
	if (opaqueBin != null) {
	    System.err.println(this + "***** Begin Dumping OpaqueBin *****");
	    dumpBin(opaqueBin);
	    System.err.println("***** End Dumping OpaqueBin *****");
	}
	*/

    }


    // Shared context case
    void updateDlistRendererResource(Renderer rdr) {
	int i;
	int size = 0;
	RenderAtomListInfo arr[];
	RenderAtomListInfo ra;

	// XXXX: there is a possible problem in the case of multiple
	// renderers (i.e., multiple screens).  Unless the
	// MasterControl sends us a separate message for each
	// renderer, we won't create a new display list for renderers
	// other than the one passed into this method.

	if (rdr == null) {
	    return;
	}

	if ((size = addDlist.size()) > 0) {
	    arr = new RenderAtomListInfo[size];
	    arr = (RenderAtomListInfo []) addDlist.toArray(arr);
	    for (i = 0; i < size; i++) {
		ra = arr[i];
		GeometryArrayRetained geo = (GeometryArrayRetained)ra.geometry();

		// First time thru this renderer or the context that
		// it is built for no longer matches the context
		// used in the renderer, create a dlist
		sharedDList.add(ra);
		geo.addDlistUser(this, ra);

		if (((geo.resourceCreationMask & rdr.rendererBit) == 0) ||
		    (geo.getDlistTimeStamp(rdr.rendererBit) !=
		     rdr.sharedCtxTimeStamp)) {
		    geo.resourceCreationMask |=  rdr.rendererBit;
		    dirtyList.add(ra);
		}
	    }
	}

	if ((size = removeDlist.size()) > 0) {
	    arr = new RenderAtomListInfo[size];
	    arr = (RenderAtomListInfo []) removeDlist.toArray(arr);
	    for (i = 0; i < size; i++) {
		ra = arr[i];
		sharedDList.remove(ra);

		GeometryArrayRetained geo = (GeometryArrayRetained)ra.geometry();
		geo.removeDlistUser(this, ra);
		//		System.err.println("========> geo.refcount = "+geo.refCount);
		// add this geometry's dlist to be freed
		if (geo.isDlistUserSetEmpty(this)) {
		    rdr.displayListResourceFreeList.add(geo.dlistObj);
		    geo.resourceCreationMask &= ~rdr.rendererBit;
		    // All Dlist on all renderer have been freed, then return dlistID
		    if (geo.resourceCreationMask == 0) {
			geo.freeDlistId();
		    }
		}
	    }
	}
	if ((size = dirtyList.size()) > 0) {
	    for (i = 0; i < size; i++) {
		ra = (RenderAtomListInfo)dirtyList.get(i);
		GeometryArrayRetained geo = (GeometryArrayRetained)ra.geometry();
		if ( (geo.resourceCreationMask & rdr.rendererBit) != 0) {
		    rdr.dirtyRenderAtomList.add(ra);
		}
	    }
	    rdr.dirtyDisplayList = true;
	    dirtyList.clear();
	}
    }

    // Non-shared context case
    void updateDlistCanvasResource(Canvas3D[] canvases)  {
	int i, j;
	Canvas3D cv;
	int size = 0;
	RenderAtomListInfo arr[];
	RenderAtomListInfo ra;

	// Add the newly added dlist to the sharedList
	if ((size = addDlist.size()) > 0) {
	    arr = new RenderAtomListInfo[size];
	    arr = (RenderAtomListInfo []) addDlist.toArray(arr);
	    for (i = 0; i <size; i++) {
		sharedDList.add(arr[i]);
		// Fix for Issue 5: add the render atom to the list of users
		// of its geometry for this RenderBin
		GeometryArrayRetained geo = (GeometryArrayRetained) arr[i].geometry();
		geo.addDlistUser(this, arr[i]);
	    }
	}

	// Remove the newly removed dlist from the sharedList
	if ((size = removeDlist.size()) > 0) {
	    arr = new RenderAtomListInfo[size];
	    arr = (RenderAtomListInfo []) removeDlist.toArray(arr);
	    for (i = 0; i < size; i++) {
		sharedDList.remove(arr[i]);
		// Fix for Issue 5: remove this render atom from the list of users
		// of its geometry for this RenderBin
		GeometryArrayRetained geo = (GeometryArrayRetained) arr[i].geometry();
		geo.removeDlistUser(this, arr[i]);
	    }
	}

	// add to the dirty list per canvas
	for (j = 0; j < canvases.length; j++) {
	    cv = canvases[j];

	    if ((size = addDlist.size()) > 0) {
		arr = new RenderAtomListInfo[size];
		arr = (RenderAtomListInfo []) addDlist.toArray(arr);
		for (i = 0; i <size; i++) {
		    ra = arr[i];
		    GeometryArrayRetained geo = (GeometryArrayRetained) ra.geometry();

		    if ((cv.ctx != null) &&
			((geo.resourceCreationMask & cv.canvasBit) == 0) ||
			(geo.getDlistTimeStamp(cv.canvasBit) !=
			 cv.ctxTimeStamp)) {
			geo.resourceCreationMask |=  cv.canvasBit;
			dirtyList.add(ra);
		    }
		}
	    }
	    if ((size = removeDlist.size()) > 0) {
		arr = new RenderAtomListInfo[size];
		arr = (RenderAtomListInfo []) removeDlist.toArray(arr);
		for (i = 0; i < size; i++) {
		    GeometryArrayRetained geo =
			(GeometryArrayRetained) arr[i].geometry();

		    // add this geometry's dlist to be freed
		    if (geo.isDlistUserSetEmpty(this)) {
			if (cv.ctx != null) {
			    canvases[j].displayListResourceFreeList.add(geo.dlistObj);
			}
			geo.resourceCreationMask &= ~canvases[j].canvasBit;
			// All Dlist on all canvases have been freed, then return dlistID
			if (geo.resourceCreationMask == 0)
			    geo.freeDlistId();
		    }
		}
	    }
	    if ((size = dirtyList.size()) > 0) {
		for (i = 0; i <size; i++) {
		    ra = (RenderAtomListInfo)dirtyList.get(i);
		    GeometryArrayRetained geo = (GeometryArrayRetained)ra.geometry();
		    if ((geo.resourceCreationMask & cv.canvasBit) != 0) {
			cv.dirtyRenderAtomList.add(ra);
		    }
		}
		cv.dirtyDisplayList = true;
		dirtyList.clear();
	    }
	}


    }

    void clearAllUpdateObjectState() {
	localeChanged = false;
	obList.clear();
	rmUpdateList.clear();
	ogCIOList.clear();
	aBinUpdateList.clear();
	sBinUpdateList.clear();
	tbUpdateList.clear();
	removeRenderAtomInRMList.clear();
	addOpaqueBin = null;
	bgAddOpaqueBin = null;
	orderedBinsList.clear();
	toBeAddedBinList.clear();
	objUpdateList.clear();
	raLocaleVwcBoundsUpdateList.clear();
	displayListResourceFreeList.clear();
	toBeAddedTextureResourceFreeList.clear();
	dirtyRenderMoleculeList.clear();
	dirtyReferenceGeomList.clear();
	reEvaluateBg = false;
        reloadBgTexture = false;
	textureBinList.clear();
	newNodeComponentList.clear();
	removeNodeComponentList.clear();
	dirtyNodeComponentList.clear();
	reEvaluateClip = false;
	vpcToVworldDirty = false;
	offScreenMessage.clear();
	addDlist.clear();
	removeDlist.clear();
	addDlistPerRinfo.clear();
	removeDlistPerRinfo.clear();
	clearDirtyOrientedRAs();
	reEvaluateSortMode = false;
	dirtyDepthSortRenderAtom.clear();
	numDirtyTinfo = 0;
    }

    void updateRendererResource(Renderer rdr) {
	RenderMolecule rm;
	TextureRetained tex;
	Integer texIdObj;

	if (rdr == null)
	    return;

	// Take care of display lists per Rinfo that should be rebuilt
	int size = addDlistPerRinfo.size();

	if (size > 0) {
	    for (int j = 0; j < size; j++) {
		RenderAtomListInfo rinfo = (RenderAtomListInfo)addDlistPerRinfo.get(j);
		if (rinfo.renderAtom.inRenderBin()) {
		    Object[] obj = new Object[2];
		    obj[0] = rinfo;
		    obj[1] = rinfo.renderAtom.renderMolecule;
		    rdr.dirtyDlistPerRinfoList.add(obj);
		}
	    }
	    rdr.dirtyDisplayList = true;
	}


	// Take care of display lists that should be rebuilt
	size = dirtyRenderMoleculeList.size();
	if (size > 0) {
	    for (int j = 0; j < size; j++) {
		rm =(RenderMolecule)dirtyRenderMoleculeList.get(j);
		rdr.dirtyRenderMoleculeList.add(rm);
	    }
	    rdr.dirtyDisplayList = true;
	}

	// Take care of texture that should be freed
	size = toBeAddedTextureResourceFreeList.size();
	int id;
	for (int j=0; j < size; j++) {
	    tex = (TextureRetained)toBeAddedTextureResourceFreeList.get(j);
	    id = tex.objectId;
	    if ((id >= rdr.textureIDResourceTable.size()) ||
		(id <= 0) ||
		(rdr.textureIDResourceTable.get(id) != tex)) {
		// tex.objectId may change by another Renderer thread,
		// need find original texID from searching
		// rdr.textureIdResourceTable
		id = rdr.textureIDResourceTable.indexOf(tex);

		if (id <= 0) {
		    continue;
		}
	    }

	    // Since multiple renderBins (in the same screen)
	    // can share a texture object, make sure that
	    // we are not duplicating what has been added
	    // by a different renderBin in the same screen
	    if ((tex.resourceCreationMask & rdr.rendererBit) != 0) {
		texIdObj = new Integer(id);
		if (!rdr.textureIdResourceFreeList.contains(texIdObj)) {
		    rdr.textureIdResourceFreeList.add(texIdObj);
		    tex.resourceCreationMask &= ~rdr.rendererBit;
		}
	    }
	}

	// Take care of display list that should be freed
	size =  displayListResourceFreeList.size();
	Integer displayListIDObj;

	for (int j=0; j <size; j++) {
	    displayListIDObj = (Integer) displayListResourceFreeList.get(j);
	    // It doesn't harm to free the same ID twice, the
	    // underlying graphics library just ignore the second request
	    rdr.displayListResourceFreeList.add(displayListIDObj);
	}
	// Take care of display list that should be freed
	size = removeDlistPerRinfo.size();
	for (int j=0; j < size; j++) {
	    RenderAtomListInfo ra = (RenderAtomListInfo)removeDlistPerRinfo.get(j);
	    rdr.displayListResourceFreeList.add(new Integer(ra.renderAtom.dlistIds[ra.index]));
	    ra.groupType = 0;
	    ra.renderAtom.dlistIds[ra.index] = -1;
	}
    }

    void updateCanvasResource(Canvas3D[] canvases)  {
	int i, j;
	RenderMolecule rm;
	TextureRetained tex;
	Integer texIdObj;

	// update dirtyRenderMoleculeList for each canvas
	for (i = 0; i < canvases.length; i++) {
	    Canvas3D cv = canvases[i];

	    // Take care of display lists per Rinfo that should be rebuilt
	    int size = addDlistPerRinfo.size();
	    if (size > 0) {
		for ( j = 0; j < size; j++) {
		    RenderAtomListInfo rinfo = (RenderAtomListInfo)addDlistPerRinfo.get(j);
		    if (rinfo.renderAtom.inRenderBin()) {
			Object[] obj = new Object[2];
			obj[0] = rinfo;
			obj[1] = rinfo.renderAtom.renderMolecule;
			cv.dirtyDlistPerRinfoList.add(obj);
		    }
		}
		cv.dirtyDisplayList = true;
	    }
	    // Take care of display lists that should be rebuilt
	    size = dirtyRenderMoleculeList.size();
	    if (size > 0) {
		for (j = 0; j < size; j++) {
		    rm = (RenderMolecule)dirtyRenderMoleculeList.get(j);
		    cv.dirtyRenderMoleculeList.add(rm);
		}
		cv.dirtyDisplayList = true;
	    }
	    // Take care of texture that should be freed
	    size = toBeAddedTextureResourceFreeList.size();
	    int id;
	    for (j=0; j < size; j++) {
		tex = (TextureRetained)toBeAddedTextureResourceFreeList.get(j);
		id = tex.objectId;
		if ((id >= cv.textureIDResourceTable.size()) ||
		    (id <= 0) ||
		    (cv.textureIDResourceTable.get(id) != tex)) {
		    // tex.objectId may change by another Renderer thread,
		    // need find original texID from searching
		    // rdr.textureIdResourceTable
		    id = cv.textureIDResourceTable.indexOf(tex);

		    if (id <= 0) {
			continue;
		    }
		}

		if ((tex.resourceCreationMask & cv.canvasBit) != 0) {
		    texIdObj = new Integer(id);
		    cv.textureIdResourceFreeList.add(texIdObj);
		    tex.resourceCreationMask &= ~cv.canvasBit;
		}
	    }
	    // Take care of display list that should be freed
	    size = displayListResourceFreeList.size();
	    for (j=0; j < size; j++) {
		cv.displayListResourceFreeList.add(displayListResourceFreeList.get(j));
	    }
	    // Take care of display list that should be freed
	    size = removeDlistPerRinfo.size();
	    for (j=0; j < size; j++) {
		RenderAtomListInfo ra = (RenderAtomListInfo)removeDlistPerRinfo.get(j);
		cv.displayListResourceFreeList.add(new Integer(ra.renderAtom.dlistIds[ra.index]));
		ra.groupType = 0;
		ra.renderAtom.dlistIds[ra.index] = -1;

	    }
	}

    }

    void processMessages(long referenceTime) {
	int i,j, index;
	Object[] nodes;
	J3dMessage messages[], m;
	int component;

	messages = getMessages(referenceTime);
	int nMsg = getNumMessage();

	if (nMsg > 0) {
	    for (i=0; i < nMsg; i++) {
		m = messages[i];
		switch (m.type) {
		case J3dMessage.INSERT_NODES:
		    insertNodes(m);
		    m.decRefcount();
		    break;
		case J3dMessage.REMOVE_NODES:
		    removeNodes(m);
		    m.decRefcount();
		    break;
		case  J3dMessage.TRANSFORM_CHANGED:
		    transformMsg = true;
		    m.decRefcount();
		    break;
		case J3dMessage.LIGHT_CHANGED:
		    // if none of the mirror lights are scoped to this view
		    // ignore this message
		    LightRetained[] mLts =(LightRetained[])m.args[3] ;
		    for (int k = 0; k < mLts.length; k++) {
			if (universe.renderingEnvironmentStructure.isLightScopedToThisView(mLts[k], view)) {
			    lightMessageList.add(m);
			    break;
			}

		    }
		    break;
		case J3dMessage.SWITCH_CHANGED:
		    visGAIsDirty = true;
		    visQuery = true;
		    processSwitchChanged(m, referenceTime);
		    // may need to process dirty switched-on transform
		    if (universe.transformStructure.getLazyUpdate()) {
			transformMsg = true;
		    }
		    m.decRefcount();
		    break;
		case J3dMessage.BACKGROUND_CHANGED:
		    BackgroundRetained bg = (BackgroundRetained)m.args[0];
		    if (universe.renderingEnvironmentStructure.isBgScopedToThisView(bg, view)) {
			reEvaluateBg = true;
                        reloadBgTexture = true;
                    }
		    m.decRefcount();
		    break;
		case J3dMessage.CLIP_CHANGED:
		    ClipRetained c = (ClipRetained)m.args[0];
		    if (universe.renderingEnvironmentStructure.isClipScopedToThisView(c, view))
			reEvaluateClip = true;
		    m.decRefcount();
		    break;
		case J3dMessage.TRANSPARENCYATTRIBUTES_CHANGED:
		    {
			NodeComponentRetained nc = (NodeComponentRetained) m.args[0];
			GeometryAtom[] gaArr = (GeometryAtom[])m.args[3];
			RenderAtom ra = null;
			int start = -1;

			// Get the first ra that is visible
			for (int k = 0; (k < gaArr.length && (start < 0)); k++) {
			    ra = gaArr[k].getRenderAtom(view);
			    if (ra== null || !ra.inRenderBin()) {
				continue;
			    }
			    else {
				start = k;
			    }
			}

			if (start >= 0) {
			    boolean restructure = (nc.mirror.changedFrequent == 0 ||
						   ra.renderMolecule.definingTransparency != nc.mirror);
			    processRenderMoleculeNodeComponentChanged(m.args,
								      RenderMolecule.TRANSPARENCY_DIRTY,
								      start, restructure);
			}
			m.decRefcount();
			break;
		    }
		case J3dMessage.POLYGONATTRIBUTES_CHANGED:
		    {
			NodeComponentRetained nc = (NodeComponentRetained) m.args[0];
			GeometryAtom[] gaArr = (GeometryAtom[])m.args[3];
			RenderAtom ra = null;
			int start = -1;

			// Get the first ra that is visible
			// Get the first ra that is visible
			for (int k = 0; (k < gaArr.length && (start < 0)); k++) {
			    ra = gaArr[k].getRenderAtom(view);
			    if (ra== null || !ra.inRenderBin()) {
				continue;
			    }
			    else {
				start = k;
			    }
			}

			if (start >= 0) {
			    boolean restructure = (nc.mirror.changedFrequent == 0 ||
						   ra.renderMolecule.definingPolygonAttributes != nc.mirror);
			    processRenderMoleculeNodeComponentChanged(m.args,
								      RenderMolecule.POLYGONATTRS_DIRTY,
								      start, restructure);
			}
			m.decRefcount();
			break;
		    }
		case J3dMessage.LINEATTRIBUTES_CHANGED:
		    {
			NodeComponentRetained nc = (NodeComponentRetained) m.args[0];
			GeometryAtom[] gaArr = (GeometryAtom[])m.args[3];
			RenderAtom ra = null;
			int start = -1;

			// Get the first ra that is visible
			// Get the first ra that is visible
			for (int k = 0; (k < gaArr.length && (start < 0)); k++) {
			    ra = gaArr[k].getRenderAtom(view);
			    if (ra== null || !ra.inRenderBin()) {
				continue;
			    }
			    else {
				start = k;
			    }
			}

			if (start >= 0) {
			    boolean restructure = (nc.mirror.changedFrequent == 0 ||
						   ra.renderMolecule.definingLineAttributes != nc.mirror);
			    processRenderMoleculeNodeComponentChanged(m.args,
								      RenderMolecule.LINEATTRS_DIRTY,
								      start, restructure);
			}
			m.decRefcount();
			break;
		    }
		case J3dMessage.POINTATTRIBUTES_CHANGED:
		    {
			NodeComponentRetained nc = (NodeComponentRetained) m.args[0];
			GeometryAtom[] gaArr = (GeometryAtom[])m.args[3];
			RenderAtom ra = null;
			int start = -1;
			// Get the first ra that is visible
			// Get the first ra that is visible
			for (int k = 0; (k < gaArr.length && (start < 0)); k++) {
			    ra = gaArr[k].getRenderAtom(view);
			    if (ra== null || !ra.inRenderBin()) {
				continue;
			    }
			    else {
				start = k;
			    }
			}

			if (start >= 0) {
			    boolean restructure = (nc.mirror.changedFrequent == 0 ||
						   ra.renderMolecule.definingPointAttributes != nc.mirror);

			    processRenderMoleculeNodeComponentChanged(m.args,
								      RenderMolecule.POINTATTRS_DIRTY,
								      start, restructure);
			}
			m.decRefcount();
			break;
		    }
		case J3dMessage.MATERIAL_CHANGED:
		    {
			NodeComponentRetained nc = (NodeComponentRetained) m.args[0];
			GeometryAtom[] gaArr = (GeometryAtom[])m.args[3];
			RenderAtom ra = null;
			int start = -1;

			// Get the first ra that is visible
			// Get the first ra that is visible
			for (int k = 0; (k < gaArr.length && (start < 0)); k++) {
			    ra = gaArr[k].getRenderAtom(view);
			    if (ra== null || !ra.inRenderBin()) {
				continue;
			    }
			    else {
				start = k;
			    }
			}

			if (start >= 0) {
			    boolean restructure = (nc.mirror.changedFrequent == 0 ||
						   ra.renderMolecule.definingMaterial != nc.mirror);
			    processRenderMoleculeNodeComponentChanged(m.args,
								      RenderMolecule.MATERIAL_DIRTY,
								      start, restructure);
			}
			m.decRefcount();
			break;
		    }
		case J3dMessage.COLORINGATTRIBUTES_CHANGED:
		    {
			NodeComponentRetained nc = (NodeComponentRetained) m.args[0];
			GeometryAtom[] gaArr = (GeometryAtom[])m.args[3];
			RenderAtom ra = null;
			int start = -1;

			// Get the first ra that is visible
			// Get the first ra that is visible
			for (int k = 0; (k < gaArr.length && (start < 0)); k++) {
			    ra = gaArr[k].getRenderAtom(view);
			    if (ra== null || !ra.inRenderBin()) {
				continue;
			    }
			    else {
				start = k;
			    }
			}

			if (start >= 0) {
			    boolean restructure = (nc.mirror.changedFrequent == 0 ||
						   ra.renderMolecule.definingColoringAttributes != nc.mirror);
			    processRenderMoleculeNodeComponentChanged(m.args,
								      RenderMolecule.COLORINGATTRS_DIRTY,
								      start, restructure);
			}
			m.decRefcount();
			break;
		    }
		case J3dMessage.TEXTUREATTRIBUTES_CHANGED:
		    processTextureAttributesChanged(
						    (NodeComponentRetained) m.args[0],
						    (GeometryAtom[])m.args[3]);
		    m.decRefcount();
		    break;
		case J3dMessage.IMAGE_COMPONENT_CHANGED:
		    addDirtyNodeComponent((NodeComponentRetained)m.args[0]);
		    m.decRefcount();
		    break;
		case J3dMessage.TEXTURE_UNIT_STATE_CHANGED:
		    processTextureUnitStateChanged(
						   (NodeComponentRetained) m.args[0],
						   (GeometryAtom[])m.args[3]);
		    m.decRefcount();
		    break;
		case J3dMessage.TEXCOORDGENERATION_CHANGED:
		    processTexCoordGenerationChanged( (NodeComponentRetained) m.args[0],
						      (GeometryAtom[])m.args[3]);
		    m.decRefcount();
		    break;
		case J3dMessage.TEXTURE_CHANGED:
		    // Texture is always in a sole user position
		    processTextureChanged((NodeComponentRetained) m.args[0],
					  (GeometryAtom[])m.args[3],
					  m.args);
		    m.decRefcount();
		    break;
     		case J3dMessage.SHADER_APPEARANCE_CHANGED:
     		case J3dMessage.SHADER_ATTRIBUTE_SET_CHANGED:
     		case J3dMessage.SHADER_ATTRIBUTE_CHANGED:
		    processShaderComponentChanged(m.args);
		    m.decRefcount();
		    break;
		case J3dMessage.RENDERINGATTRIBUTES_CHANGED:
		    processAttributeBinNodeComponentChanged(m.args);
		    component = ((Integer)m.args[1]).intValue();
		    if (component == RenderingAttributesRetained.VISIBLE) {
			visGAIsDirty = true;
			visQuery = true;
		    }
		    m.decRefcount();
		    break;
		case J3dMessage.APPEARANCE_CHANGED:
		    processAppearanceChanged(m.args);
		    m.decRefcount();
		    break;
		case J3dMessage.FOG_CHANGED:
		    FogRetained mfog =  ((FogRetained)m.args[0]).mirrorFog;
		    if (universe.renderingEnvironmentStructure.isFogScopedToThisView(mfog, view)) {
			processFogChanged(m.args);
		    }
		    m.decRefcount();
		    break;
		case J3dMessage.ALTERNATEAPPEARANCE_CHANGED:
		    AlternateAppearanceRetained maltapp =  ((AlternateAppearanceRetained)m.args[0]).mirrorAltApp;
		    if (universe.renderingEnvironmentStructure.isAltAppScopedToThisView(maltapp, view)) {
			altAppearanceDirty = true;
		    }
		    m.decRefcount();
		    break;
		case J3dMessage.MODELCLIP_CHANGED:
		    ModelClipRetained mc= ((ModelClipRetained)m.args[0]).mirrorModelClip;
		    if (universe.renderingEnvironmentStructure.isMclipScopedToThisView(mc, view)) {
			processModelClipChanged(m.args);
		    }
		    m.decRefcount();
		    break;
		case J3dMessage.BOUNDINGLEAF_CHANGED:
		    processBoundingLeafChanged(m.args,
					       referenceTime);
		    m.decRefcount();
		    break;
		case J3dMessage.SHAPE3D_CHANGED:
		    processShapeChanged(m.args, referenceTime);
		    m.decRefcount();
		    break;
		case J3dMessage.ORIENTEDSHAPE3D_CHANGED:
		    processOrientedShape3DChanged((Object[])m.args[0]);
		    m.decRefcount();
		    break;
		case J3dMessage.MORPH_CHANGED:
		    processMorphChanged(m.args, referenceTime);
		    component = ((Integer)m.args[1]).intValue();
		    if ((component & MorphRetained.GEOMETRY_CHANGED) == 0) {
			visGAIsDirty = true;
			visQuery = true;
		    }
		    m.decRefcount();
		    break;
		case J3dMessage.UPDATE_VIEW:
		    {
			View v = (View)m.args[0];
			ViewPlatform vp = v.getViewPlatform();
			int comp = ((Integer)(m.args[2])).intValue();
			int value = ((Integer)(m.args[3])).intValue();
			if (comp == View.TRANSP_SORT_POLICY_CHANGED) {
			    if (value != transpSortMode) {
				reEvaluateSortMode = true;
				cachedTranspSortMode = value;
			    }
			} else if (vp != null) {
			    if (value != transpSortMode) {
				reEvaluateSortMode = true;
				cachedTranspSortMode = value;
			    }
			    updateViewPlatform((ViewPlatformRetained)vp.retained,
					       ((Float)m.args[1]).floatValue());
			    visQuery = true;
			    // XXXX : Handle view.visibilityPolicy changed.
			    if(((View.VISIBILITY_POLICY_DIRTY != 0) &&
				(View.VISIBILITY_DRAW_ALL != view.viewCache.visibilityPolicy)) ||
			       locale != ((ViewPlatformRetained) (vp.retained)).locale) {

				for (int n = (renderAtoms.size() - 1); n>=0 ; n--) {
							removeARenderAtom(renderAtoms.get(n));
				}
				renderAtoms.clear();
				visGAIsDirty = true;
				if (locale != ((ViewPlatformRetained) (vp.retained)).locale) {
				    locale = ((ViewPlatformRetained) (vp.retained)).locale;
				    localeChanged = true;
				}
			    }
			}
			m.decRefcount();
		    }
		    break;
		case J3dMessage.UPDATE_VIEWPLATFORM:
		    updateViewPlatform((ViewPlatformRetained) m.args[0],
				       ((Float)m.args[1]).floatValue());
		    m.decRefcount();
		    break;
		case J3dMessage.TEXT3D_DATA_CHANGED:
		    processDataChanged((Object[])m.args[0],
				       (Object[])m.args[1],
				       referenceTime);
		    m.decRefcount();
		    break;
		case J3dMessage.GEOMETRY_CHANGED:
		    processGeometryChanged(m.args);
		    visGAIsDirty = true;
		    visQuery = true;
		    m.decRefcount();
		    break;

		case J3dMessage.BOUNDS_AUTO_COMPUTE_CHANGED:
		case J3dMessage.REGION_BOUND_CHANGED:
		    processGeometryAtomsChanged((Object[])m.args[0]);
		    visGAIsDirty = true;
		    visQuery = true;
		    m.decRefcount();
		    break;
		case J3dMessage.TEXT3D_TRANSFORM_CHANGED:
		    processText3DTransformChanged((Object[])m.args[0],
						  (Object[])m.args[1],
						  referenceTime);
		    visQuery = true;
		    m.decRefcount();
		    break;
		case J3dMessage.ORDERED_GROUP_INSERTED:
		    processOrderedGroupInserted(m);
		    // Do not do decRefcount() here. We'll do it in updateObject().
		    ogCIOList.add(m);
		    break;
		case J3dMessage.ORDERED_GROUP_REMOVED:
		    processOrderedGroupRemoved(m);
		    // Do not do decRefcount() here. We'll do it in updateObject().
		    ogCIOList.add(m);
		    break;
		case J3dMessage.ORDERED_GROUP_TABLE_CHANGED:
		    // Do not do decRefcount() here. We'll do it in updateObject().
		    ogCIOList.add(m);
		    break;
		case J3dMessage.RENDER_OFFSCREEN:
		    offScreenMessage.add(m);
		    break;
		case J3dMessage.VIEWSPECIFICGROUP_CHANGED:
		    processViewSpecificGroupChanged(m);
		    visQuery = true;
		    m.decRefcount();
		    break;
		default:
		    m.decRefcount();
		}
	    }

	    if (transformMsg) {
		processTransformChanged(referenceTime);
		transformMsg = false;
	    }
	    if (lightMessageList.size() > 0) {
		processLightChanged();
		lightMessageList.clear();
	    }
	    VirtualUniverse.mc.addMirrorObject(this);

	    // clear the array to prevent memory leaks
	    Arrays.fill(messages, 0, nMsg, null);
	}

	if (reEvaluateBg) {
	    currentActiveBackground = universe.renderingEnvironmentStructure.
		getApplicationBackground(vpSchedSphereInVworld, locale, view);
	}


	if (visQuery) {
	    GeometryAtom[] bgGeometryAtoms;
	    boolean allEnComp;

	    // computeViewFrustumBox in VisibilityStructure.
	    computeViewFrustumBBox(viewFrustumBBox);
	    //	     System.err.println("viewFrustumBBox = " + this);

	    ViewPlatform vp = view.getViewPlatform();
	    if (vp != null) {
		allEnComp = universe.geometryStructure.
		    getVisibleBHTrees(this, viewFrustumBBox,
				      locale, referenceTime,
				      visGAIsDirty || reactivateView || localeChanged ||
				      ((view.viewCache.vcDirtyMask &
					View.VISIBILITY_POLICY_DIRTY) != 0),
				      view.viewCache.visibilityPolicy);

		reactivateView = false;
		// process background geometry atoms
		if (currentActiveBackground != null &&
		    currentActiveBackground.geometryBranch != null) {
		    bgGeometryAtoms =
			currentActiveBackground.getBackgroundGeometryAtoms();
		    if (bgGeometryAtoms != null) {
			processBgGeometryAtoms(bgGeometryAtoms, referenceTime);
		    }
		}

		if(!allEnComp) {
		    // Increment the framecount for compaction ...
		    frameCount++;
		    if (frameCount > frameCountCutoff) {
			frameCount = 0;
			checkForCompaction();
		    }
		    else if (frameCount == notVisibleCount) {
			removeCutoffTime = referenceTime;
		    }
		}
	    }
	    // Reset dirty bits.
	    visGAIsDirty = false;
	    visQuery = false;

	}
	// Two environments are dirty
	// If lights, fog or model clip have been added/removed, then
	// reEvaluate RenderAtoms and mark the lightbin and
	// env set dirty if applicable
	if (envDirty == REEVALUATE_ALL_ENV || envDirty == 3 ||
	    envDirty > 4) {
	    reEvaluateEnv(changedLts, changedFogs, changedModelClips, true,
			  altAppearanceDirty);
	}
	else  if (envDirty == 0 && altAppearanceDirty) {
	    reEvaluateAlternateAppearance();
	}
	else {
	    if ((envDirty & REEVALUATE_LIGHTS) != 0) {
		reEvaluateLights(altAppearanceDirty);
	    }
	    else if ((envDirty & REEVALUATE_FOG) != 0)
		reEvaluateFog(changedFogs, (changedFogs.size() > 0), altAppearanceDirty);
	    else if ((envDirty & REEVALUATE_MCLIP) != 0)
		reEvaluateModelClip(changedModelClips, (changedModelClips.size() > 0), altAppearanceDirty);
	}



        // do any pre-update node component screening

        if (updateCheckList.size() > 0) {
	    int size = updateCheckList.size();
	    NodeComponentUpdate bin;
	    for (int k = 0; k < size; k++) {
		bin = (NodeComponentUpdate) updateCheckList.get(k);
		bin.updateNodeComponentCheck();
	    }
	    updateCheckList.clear();
	}


	changedLts.clear();
	changedFogs.clear();
	changedModelClips.clear();
	envDirty = 0;
	altAppearanceDirty = false;

	view.renderBinReady = true;

	VirtualUniverse.mc.sendRunMessage(view,
					  J3dThread.RENDER_THREAD);
    }


    void processSwitchChanged(J3dMessage m, long refTime) {
	int i;
	UnorderList arrList;
	int size;
	Object[] nodes, nodesArr;
	LeafRetained leaf;

	RenderingEnvironmentStructure rdrEnvStr =
	    universe.renderingEnvironmentStructure;

	UpdateTargets targets = (UpdateTargets)m.args[0];
	arrList = targets.targetList[Targets.ENV_TARGETS];

	if (arrList != null) {
	    size = arrList.size();
	    nodesArr = arrList.toArray(false);

	    for (int h=0; h<size; h++) {
		nodes = (Object[])nodesArr[h];
		for (i=0; i<nodes.length; i++) {

		    if (nodes[i] instanceof LightRetained &&
			rdrEnvStr.isLightScopedToThisView(nodes[i], view)) {
			envDirty |=  REEVALUATE_LIGHTS;
		    } else if (nodes[i] instanceof FogRetained &&
			       rdrEnvStr.isFogScopedToThisView(nodes[i], view)) {
			envDirty |=  REEVALUATE_FOG;
		    } else if (nodes[i] instanceof ModelClipRetained &&
			       rdrEnvStr.isMclipScopedToThisView(nodes[i], view)) {
			envDirty |=  REEVALUATE_MCLIP;
		    } else if (nodes[i] instanceof BackgroundRetained &&
			       rdrEnvStr.isBgScopedToThisView(nodes[i], view)) {
			reEvaluateBg = true;
		    } else if (nodes[i] instanceof ClipRetained &&
			       rdrEnvStr.isClipScopedToThisView(nodes[i], view)) {
			reEvaluateClip = true;
		    } else if (nodes[i] instanceof AlternateAppearanceRetained &&
			       rdrEnvStr.isAltAppScopedToThisView(nodes[i], view)) {
			altAppearanceDirty = true;
		    }
		}
	    }
	}

	arrList = targets.targetList[Targets.BLN_TARGETS];
	if (arrList != null) {
	    size = arrList.size();
	    nodesArr = arrList.toArray(false);
	    Object[] objArr = (Object[])m.args[1];
	    Object[] obj, users;
	    BoundingLeafRetained mbleaf;

	    for (int h=0; h<size; h++) {
		nodes = (Object[])nodesArr[h];
		obj = (Object[])objArr[h];
		for (i=0; i<nodes.length; i++) {

		    users = (Object[])obj[i];
		    mbleaf = (BoundingLeafRetained)nodes[i];
		    for (int j = 0; j < users.length; j++) {

			if (users[j] instanceof FogRetained &&
			    rdrEnvStr.isFogScopedToThisView(users[j], view)) {
			    envDirty |=  REEVALUATE_FOG;
			} else if (users[j] instanceof LightRetained &&
				   rdrEnvStr.isLightScopedToThisView(users[j], view)) {
			    envDirty |=  REEVALUATE_LIGHTS;
			} else if (users[j] instanceof ModelClipRetained &&
				   rdrEnvStr.isMclipScopedToThisView(users[j], view)) {
			    envDirty |=  REEVALUATE_MCLIP;
			} else if (users[j] instanceof
				   AlternateAppearanceRetained &&
				   rdrEnvStr.isAltAppScopedToThisView(users[j], view)) {
			    altAppearanceDirty = true;
			} else if (users[j] instanceof BackgroundRetained &&
				   rdrEnvStr.isBgScopedToThisView(users[j], view)) {
			    reEvaluateBg = true;
			} else if (users[j] instanceof ClipRetained &&
				   rdrEnvStr.isClipScopedToThisView(users[j], view)) {
			    reEvaluateClip = true;
			}
		    }
		}
	    }
	}
    }


    /**
     * Transparency/Line/point/Poly attributes is different from other renderMolecule
     * attributes since the renderatom could move from opaque bin
     * to transparent bin
     */
    void processPossibleBinChanged(Object[] args) {
	int i;
	GeometryAtom[] gaArr = (GeometryAtom[])args[3];
	for (i = 0; i < gaArr.length; i++) {
	    RenderAtom ra = gaArr[i].getRenderAtom(view);
	    if (ra== null || !ra.inRenderBin())
		continue;
	    // If renderAtom is in orderedGroup or with this
	    // change continues to be in the same higher level
	    // lightBin(transparent or opaque) then reInsert at
	    // the textureBin level, other Insert at the lightBin
	    // level
	    TextureBin tb = ra.renderMolecule.textureBin;
	    ra.renderMolecule.removeRenderAtom(ra);
	    reInsertRenderAtom(tb, ra);
	}
    }


    /**
     * This processes a materiala and other rendermolecule node comp change.
     */
    void processRenderMoleculeNodeComponentChanged(Object[] args, int mask, int start,
						   boolean restructure) {
       	int i;
	NodeComponentRetained nc = (NodeComponentRetained) args[0];
	GeometryAtom[] gaArr = (GeometryAtom[])args[3];
	for (i = start; i < gaArr.length; i++) {
	    RenderAtom ra = gaArr[i].getRenderAtom(view);
	    if (ra== null || !ra.inRenderBin())
		continue;
	    // Check if the changed renderAtom is already in
	    // a separate bin - this is to handle the case
	    // when it has been changed to frequent, then to
	    // infrequent and then to frequent again!
	    // If the bin is in soleUser case and one of the components
	    // has been changed to frequent then remove the clone
	    // and point to the mirror
	    //	    System.err.println("restructure = "+restructure+" ra.renderMolecule.soleUser ="+ra.renderMolecule.soleUser);
	    if (restructure && !ra.renderMolecule.soleUser) {
		TextureBin tb = ra.renderMolecule.textureBin;
		ra.renderMolecule.removeRenderAtom(ra);
		reInsertRenderAtom(tb, ra);
		/*
		if (nc.mirror.changedFrequent != 0) {
		    if ((ra.renderMolecule.soleUserCompDirty& RenderMolecule.ALL_DIRTY_BITS) == 0 ) {
			rmUpdateList.add(ra.renderMolecule);
		    }
		    ra.renderMolecule.soleUserCompDirty |= mask;
		}
		*/
	    }
	    else {
		if ((ra.renderMolecule.soleUserCompDirty& RenderMolecule.ALL_DIRTY_BITS) == 0 ) {
		    rmUpdateList.add(ra.renderMolecule);
		}
		ra.renderMolecule.soleUserCompDirty |= mask;
	    }
	}

    }


    void processTextureAttributesChanged(NodeComponentRetained nc,
					GeometryAtom[] gaArr) {

	RenderAtom ra = null;
	TextureBin tb;
        ShaderBin sb;
	boolean reInsertNeeded = false;

	if (nc.mirror.changedFrequent == 0) {
	    reInsertNeeded = true;
	}

	for (int k = 0; k < gaArr.length; k++) {
	    ra = gaArr[k].getRenderAtom(view);
	    if (ra== null || !ra.inRenderBin()) {
		continue;
	    }

	    tb = ra.renderMolecule.textureBin;

	    if (!reInsertNeeded) {

	        // if changedFrequent is not zero, then need
	        // to check if the node component is currently
	        // in an equivalent bin or not. If it is in an
	        // equivalent bin, then the affected ra needs to
	        // be reinserted to a bin with a soleUser
	        // TextureAttributes

		for (int t = 0; t < tb.texUnitState.length; t++) {

		    if (tb.texUnitState[t] == null) {
			continue;

		    } else if (tb.texUnitState[t].texAttrs == nc.mirror) {
			// the TextureAttributes is already in
			// a sole user position, no need to do anything;
			// can bail out now
			return;
		    }
		}
	    }

	    if ((tb.tbFlag & TextureBin.SOLE_USER) != 0) {

	        // if the TextureBin is a sole user, then
	        // no need to reInsert, just simply update the
	        // TextureAttributes references @update

	        if (tb.soleUserCompDirty == 0) {
		    tbUpdateList.add(tb);
		}

		tb.soleUserCompDirty |= TextureBin.SOLE_USER_DIRTY_TA;

	    } else {
	        sb= ra.renderMolecule.textureBin.shaderBin;
	        ra.renderMolecule.removeRenderAtom(ra);
	        reInsertTextureBin(sb, ra);
	    }
	}
    }

    void processTexCoordGenerationChanged(NodeComponentRetained nc,
					GeometryAtom[] gaArr) {

	RenderAtom ra = null;
	TextureBin tb;
        ShaderBin sb;
	boolean reInsertNeeded = false;

	if (nc.mirror.changedFrequent == 0) {
	    reInsertNeeded = true;
	}

	for (int k = 0; k < gaArr.length; k++) {
	    ra = gaArr[k].getRenderAtom(view);
	    if (ra== null || !ra.inRenderBin()) {
		continue;
	    }

	    tb = ra.renderMolecule.textureBin;

	    if (!reInsertNeeded) {

	        // if changedFrequent is not zero, then need
	        // to check if the node component is currently
	        // in an equivalent bin or not. If it is in an
	        // equivalent bin, then the affected ra needs to
	        // be reinserted to a bin with a soleUser
	        // TexCoordGeneration

		for (int t = 0; t < tb.texUnitState.length; t++) {

		    if (tb.texUnitState[t] == null) {
			continue;

		    } else if (tb.texUnitState[t].texGen == nc.mirror) {
			// the TexCoordGeneration is already in
			// a sole user position, no need to do anything;
			// can bail out now
			return;
		    }
		}
	    }

	    if ((tb.tbFlag & TextureBin.SOLE_USER) != 0) {

	        // if the TextureBin is a sole user, then
	        // no need to reInsert, just simply update the
	        // TexCoordGeneration references @update

	        if (tb.soleUserCompDirty == 0) {
		    tbUpdateList.add(tb);
		}

		tb.soleUserCompDirty |= TextureBin.SOLE_USER_DIRTY_TC;

	    } else {
	        sb= ra.renderMolecule.textureBin.shaderBin;
	        ra.renderMolecule.removeRenderAtom(ra);
	        reInsertTextureBin(sb, ra);
	    }
	}
    }


    void processTextureChanged(NodeComponentRetained nc,
				GeometryAtom[] gaArr,
				Object args[]) {

	RenderAtom ra = null;
	TextureBin tb;
        ShaderBin sb;
	boolean reInsertNeeded = false;
        int command = ((Integer)args[1]).intValue();

	switch (command) {
	case TextureRetained.ENABLE_CHANGED: {
	    for (int i = 0; i < gaArr.length; i++) {
	        ra = gaArr[i].getRenderAtom(view);

	        if (ra== null || !ra.inRenderBin())
		    continue;

	        tb = ra.renderMolecule.textureBin;

	        if (tb.soleUserCompDirty == 0) {
		    // put this texture unit state on the sole user
		    // update list if it's not already there
		    tbUpdateList.add(tb);
	        }

	        tb.soleUserCompDirty |= TextureBin.SOLE_USER_DIRTY_TEXTURE;
	    }
	    break;
	}
	case TextureRetained.IMAGE_CHANGED: {

	    TextureRetained texture = (TextureRetained)nc.mirror;
	    Object imgChangedArgs[] = (Object[])args[2];
	    int level = ((Integer)imgChangedArgs[0]).intValue();
	    int face = ((Integer)imgChangedArgs[2]).intValue();
	    ImageComponent newImage = (ImageComponent)imgChangedArgs[1];
	    ImageComponentRetained oldImage;

	    // first remove the old image from the RenderBin's
	    // node component list if necessary.
	    // Note: image reference in the texture mirror object
	    //		is not updated yet, so it's ok to reference
	    //		the mirror object for the old image reference

	    oldImage = texture.images[face][level];

            // it is possible that oldImage.source == null because
            // the mipmap could have been created by the library, and
            // hence don't have source

	    if (oldImage != null) {
	        this.removeNodeComponent(oldImage);
	    }

	    // then add the new one to the list if it is byReference or
	    // modifiable.

	    if (newImage != null ) {
	        this.addNodeComponent(newImage.retained);
	    }
	    break;
	}
	case TextureRetained.IMAGES_CHANGED: {
	    Object imgChangedArgs[] = (Object [])args[2];
	    ImageComponent images[] = (ImageComponent [])imgChangedArgs[0];
	    int face = ((Integer)imgChangedArgs[1]).intValue();
	    TextureRetained texture = (TextureRetained)nc.mirror;
	    ImageComponentRetained oldImage;

	    for (int i = 0; i < texture.maxLevels; i++) {

	        // first remove the old image from the RenderBin's
	        // node component list if necessary.
	        // Note: image reference in the texture mirror object
	        //		is not updated yet, so it's ok to reference
	        //		the mirror object for the old image reference

	        oldImage = texture.images[face][i];

                // it is possible that oldImage.source == null because
                // the mipmap could have been created by the library, and
                // hence don't have source

	        if (oldImage != null) {
	            this.removeNodeComponent(oldImage);
	        }

	        // then add the new one to the list if it is byReference
	        if (images[i] != null ) {
	            this.addNodeComponent(((ImageComponent)images[i]).retained);
	        }
	    }
	    break;
	}
	}
    }


    void processTextureUnitStateChanged(NodeComponentRetained nc,
					GeometryAtom[] gaArr) {
	RenderAtom ra = null;
	TextureBin tb;
        ShaderBin sb;
	boolean mirrorSet = false;
	boolean firstTextureBin = true;

	for (int k = 0; k < gaArr.length; k++) {
	    ra = gaArr[k].getRenderAtom(view);
	    if (ra== null || !ra.inRenderBin()) {
		continue;
	    }

	    tb = ra.renderMolecule.textureBin;

	    if (firstTextureBin) {

                for (int t = 0;
			t < tb.texUnitState.length && !mirrorSet;
			 t++) {

                    if (tb.texUnitState[t] == null) {
                        continue;

                    } else if (tb.texUnitState[t].mirror == nc.mirror) {
			mirrorSet = true;
                        firstTextureBin = false;
                    }
                }
                firstTextureBin = false;
            }

            if (mirrorSet) {

                if (tb.soleUserCompDirty == 0) {
                    tbUpdateList.add(tb);
                }

                tb.soleUserCompDirty |= TextureBin.SOLE_USER_DIRTY_TUS;

	    } else {
	        sb = ra.renderMolecule.textureBin.shaderBin;
	        ra.renderMolecule.removeRenderAtom(ra);
	        reInsertTextureBin(sb, ra);
	    }
	}
    }


    /**
     * This processes a rendering attribute change.
     */

    void processAttributeBinNodeComponentChanged(Object[] args) {
	int i;
	GeometryAtom[] gaArr = (GeometryAtom[])args[3];
	int component = ((Integer)args[1]).intValue();
	NodeComponentRetained nc = (NodeComponentRetained) args[0];

	RenderAtom ra = null;
	int start = -1;

	// Get the first ra that is visible
	for (i = 0; (i < gaArr.length && (start < 0)); i++) {
	    ra = gaArr[i].getRenderAtom(view);
	    if (ra== null || !ra.inRenderBin()) {
		continue;
	    }
	    else {
		start = i;
	    }
	}
	if (start >= 0) {
	    // Force restucture, when changedFrequent is zero OR
	    // when it is changed to changedFrequent the first time OR
	    // when the ignoreVC changedFrequent flag is set for the first time
	    // when the last one is set for the first time, we need to force
	    // any separate dlist in RMs to go thru VA
	    boolean restructure = (nc.mirror.changedFrequent == 0 ||
				   ra.renderMolecule.textureBin.attributeBin.definingRenderingAttributes != nc.mirror);

	    if (component != RenderingAttributesRetained.VISIBLE) {
		for (i = start; i < gaArr.length; i++) {
		    ra = gaArr[i].getRenderAtom(view);
		    if (ra== null || !ra.inRenderBin())
			continue;
		    if (restructure && !ra.renderMolecule.textureBin.attributeBin.soleUser) {
			EnvironmentSet e= ra.renderMolecule.textureBin.environmentSet;
			ra.renderMolecule.removeRenderAtom(ra);
			reInsertAttributeBin(e, ra);
			/*
			// If changed Frequent the first time,
			// then  the cached value
			// may not be up-to-date since the nc is
			// updated in updateObject
			// So, add it to the list so that the cached value can
			// be updated
			if (nc.mirror.changedFrequent != 0) {
			    AttributeBin aBin = ra.renderMolecule.textureBin.attributeBin;
			    if ((aBin.onUpdateList & AttributeBin.ON_CHANGED_FREQUENT_UPDATE_LIST) == 0 ) {
				aBinUpdateList.add(aBin);
				aBin.onUpdateList |= AttributeBin.ON_CHANGED_FREQUENT_UPDATE_LIST;
			    }
			}
			*/
		   }
		    else {
			AttributeBin aBin = ra.renderMolecule.textureBin.attributeBin;
			if ((aBin.onUpdateList & AttributeBin.ON_CHANGED_FREQUENT_UPDATE_LIST) == 0 ) {
			    aBinUpdateList.add(aBin);
			    aBin.onUpdateList |= AttributeBin.ON_CHANGED_FREQUENT_UPDATE_LIST;
			}
		    }
		}
	    }
	    else {
		for (i = start; i < gaArr.length; i++) {
		    ra = gaArr[i].getRenderAtom(view);

		    if (ra== null || !ra.inRenderBin())
			continue;
		    renderAtoms.remove(renderAtoms.indexOf(ra));
		    removeARenderAtom(ra);
		}
	    }
	}
    }

    /**
     * This processes a shader component change.
     */
    void processShaderComponentChanged(Object[] args) {

	// System.err.println("RenderBin : processShaderComponentChanged");

	int component = ((Integer)args[1]).intValue();
	int i;
	GeometryAtom[] gaArr = (GeometryAtom[] )args[3];
	GeometryAtom  ga;
	RenderAtom ra = null;
	/* TODO : JADA - Sole user logic is incomplete. Will disable for JavaOne */
	// Note : args[0] may be a ShaderAppearanceRetained or ShaderAttributeSetRetained
	//ShaderAppearanceRetained sApp = (ShaderAppearanceRetained) args[0];
	int start = -1;


	// Get the first ra that is visible
	for (i = 0; (i < gaArr.length && (start < 0)); i++) {
	    ra = gaArr[i].getRenderAtom(view);
	    if (ra== null || !ra.inRenderBin()) {
		continue;
	    }
	    else {
		start = i;
	    }
	}
	if (start >= 0) {

            // Issue 471 - Don't check ATTRIBUTE_VALUE_UPDATE, there is no need
            // to do anything to the shader bins when a value changes.
	    boolean spUpdate =
		((component & ShaderAppearanceRetained.SHADER_PROGRAM) != 0);
	    boolean sasUpdate =
		(((component & ShaderAppearanceRetained.SHADER_ATTRIBUTE_SET) != 0) ||
		 ((component & ShaderConstants.ATTRIBUTE_SET_PUT) != 0) ||
		 ((component & ShaderConstants.ATTRIBUTE_SET_REMOVE) != 0) ||
		 ((component & ShaderConstants.ATTRIBUTE_SET_CLEAR) != 0));

	    if (spUpdate) {
		/* TODO : JADA - Sole user logic is incomplete. Will disable for JavaOne */
		//if (false && (sApp.mirror.changedFrequent & component) != 0) {
                if(false) {
		    /*
		      System.err.println("RenderBin : Shader sole user (SHADER_PROGRAM)" +
		      ra.renderMolecule.textureBin.shaderBin);
		    */

		    ShaderBin sBin;

		    for (i = start; i < gaArr.length; i++) {
                        ra = gaArr[i].getRenderAtom(view);
                        if (ra== null || !ra.inRenderBin())
                            continue;

                        sBin = ra.renderMolecule.textureBin.shaderBin;

                        if (sBin.componentDirty == 0) {
                            sBinUpdateList.add(sBin);
                            sBin.componentDirty |= ShaderBin.SHADER_PROGRAM_DIRTY;
                        }
                    }
		} else {
		    /*
		      System.err.println("RenderBin : not soleUser (SHADER_PROGRAM)" +
		      ra.renderMolecule.textureBin.shaderBin);
		    */

		    for (i = 0; i < gaArr.length; i++) {
			ra = gaArr[i].getRenderAtom(view);
			if (ra== null || !ra.inRenderBin())
			    continue;

			AttributeBin attrBin = ra.renderMolecule.textureBin.attributeBin;
			ra.renderMolecule.removeRenderAtom(ra);
			reInsertShaderBin(attrBin, ra);
		    }
		}
	    } else if (sasUpdate) {
		/* TODO : JADA - Sole user logic is incomplete. Will disable for JavaOne */
		//if (false && (sApp.mirror.changedFrequent & component) != 0) {
                  if(false) {
                    /*
		      System.err.println("RenderBin : sole user (SHADER_ATTRIBUTE_SET)" +
		      ra.renderMolecule.textureBin.shaderBin);
		    */

		    ShaderBin sBin;

		    for (i = 0; i < gaArr.length; i++) {
			ra = gaArr[i].getRenderAtom(view);
			if (ra== null || !ra.inRenderBin())
			    continue;


			sBin = ra.renderMolecule.textureBin.shaderBin;

			if (sBin.componentDirty == 0) {
			    sBinUpdateList.add(sBin);
			    sBin.componentDirty |= ShaderBin.SHADER_ATTRIBUTE_SET_DIRTY;
			}
		    }
		} else {
		    /*
		       System.err.println("RenderBin :not soleUser (SHADER_ATTRIBUTE_SET) " +
		       ra.renderMolecule.textureBin.shaderBin);
		    */

		    for (i = 0; i < gaArr.length; i++) {
			ra = gaArr[i].getRenderAtom(view);
			if (ra== null || !ra.inRenderBin())
			    continue;

			AttributeBin attrBin = ra.renderMolecule.textureBin.attributeBin;
			ra.renderMolecule.removeRenderAtom(ra);
			reInsertShaderBin(attrBin, ra);
		    }
		}
	    }
	}

    }


    void processFogChanged(Object[] args) {
	FogRetained fog = (FogRetained)args[0];
	EnvironmentSet e;
	int component = ((Integer)args[1]).intValue();

	if ((component &(FogRetained.SCOPE_CHANGED |
			 FogRetained.BOUNDS_CHANGED |
			 FogRetained.BOUNDINGLEAF_CHANGED)) != 0){
	    envDirty |= REEVALUATE_FOG;
	}
	else {
	    UnorderList list = fog.mirrorFog.environmentSets;
	    synchronized (list) {
		EnvironmentSet envsets[] = (EnvironmentSet []) list.toArray(false);
		int size =  list.size();
		for (int i = 0; i < size; i++) {
		    e = envsets[i];
		    e.canvasDirty |= Canvas3D.FOG_DIRTY;
		    if (!e.onUpdateList) {
			objUpdateList.add(e);
			e.onUpdateList = true;
		    }
		}
	    }
	}
    }


    /**
     * This routine get called whenever a component of the appearance
     * changes
     */
    void processAppearanceChanged(Object[] args){
	int component = ((Integer)args[1]).intValue();
	int i;
	GeometryAtom[] gaArr = (GeometryAtom[] )args[3];
	GeometryAtom  ga;
	RenderAtom ra = null;
	AppearanceRetained app = (AppearanceRetained) args[0];
	int TEXTURE_STATE_CHANGED =
	    AppearanceRetained.TEXTURE_UNIT_STATE |
	    AppearanceRetained.TEXTURE |
	    AppearanceRetained.TEXTURE_ATTR |
	    AppearanceRetained.TEXCOORD_GEN ;

        int start = -1;

        // Get the first ra that is visible
        for (i = 0; (i < gaArr.length && (start < 0)); i++) {
            ra = gaArr[i].getRenderAtom(view);
            if (ra== null || !ra.inRenderBin()) {
                continue;
            }
            else {
                start = i;
            }
        }

	if (start >= 0) {

	    if ((component & TEXTURE_STATE_CHANGED) != 0) {


	        if (((app.mirror.changedFrequent & TEXTURE_STATE_CHANGED) != 0) &&
			((ra.renderMolecule.textureBin.tbFlag &
				TextureBin.SOLE_USER) != 0))  {

/*
System.err.println("renderbin. texture state changed  tb sole user " +
			ra.renderMolecule.textureBin + " tb.tbFlag= " +
			ra.renderMolecule.textureBin.tbFlag);
*/

 		    TextureBin tb;


	            for (i = start; i < gaArr.length; i++) {
		        ra = gaArr[i].getRenderAtom(view);
		        if (ra== null || !ra.inRenderBin())
		            continue;

		        tb = ra.renderMolecule.textureBin;
		        if (tb.soleUserCompDirty == 0) {
			    tbUpdateList.add(tb);
		        }

			// mark that the texture unit state ref is changed
			// also mark that the TextureBin needs to reevaluate
			// number of active textures
		        tb.soleUserCompDirty |=
				TextureBin.SOLE_USER_DIRTY_REF;
		    }
	        } else {
/*
System.err.println("renderbin. texture state changed  tb not sole user " +
			ra.renderMolecule.textureBin + " tb.tbFlag= " +
			ra.renderMolecule.textureBin.tbFlag);

System.err.println("......tb.soleUser= " +
        ((ra.renderMolecule.textureBin.tbFlag & TextureBin.SOLE_USER) != 0) +
        " app.mirror.changedFrequent= " +
        ((app.mirror.changedFrequent & TEXTURE_STATE_CHANGED) != 0));

*/

	            for (i = start; i < gaArr.length; i++) {
		        ra = gaArr[i].getRenderAtom(view);
		        if (ra== null || !ra.inRenderBin())
		            continue;
		        ShaderBin sb = ra.renderMolecule.textureBin.shaderBin;
		        ra.renderMolecule.removeRenderAtom(ra);
		        reInsertTextureBin(sb, ra);
	            }
	        }
	    } else if ((component & AppearanceRetained.RENDERING) != 0) {
		boolean visible = ((Boolean)args[4]).booleanValue();
		visGAIsDirty = true;
		visQuery = true;
		if (!visible) {
		    // remove all gaAttrs
		    for (i = start; i < gaArr.length; i++) {
			ra = gaArr[i].getRenderAtom(view);

			if (ra== null || !ra.inRenderBin())
			    continue;
			renderAtoms.remove(renderAtoms.indexOf(ra));
			removeARenderAtom(ra);
		    }
		}
		else {
		    if ((app.mirror.changedFrequent & component) != 0 &&
			ra.renderMolecule.textureBin.attributeBin.soleUser) {
			for (i = start; i < gaArr.length; i++) {
			    ra = gaArr[i].getRenderAtom(view);
			    if (ra== null || !ra.inRenderBin())
				continue;

			    AttributeBin aBin = ra.renderMolecule.textureBin.attributeBin;
			    if ((aBin.onUpdateList & AttributeBin.ON_CHANGED_FREQUENT_UPDATE_LIST) == 0 ) {
				aBinUpdateList.add(aBin);
				aBin.onUpdateList |= AttributeBin.ON_CHANGED_FREQUENT_UPDATE_LIST;
			    }

			}
		    }
		    else {
			for (i = start; i < gaArr.length; i++) {
			    ra = gaArr[i].getRenderAtom(view);
			    if (ra== null || !ra.inRenderBin())
				continue;
			    EnvironmentSet e = ra.renderMolecule.textureBin.environmentSet;
			    ra.renderMolecule.removeRenderAtom(ra);
			    reInsertAttributeBin(e, ra);
			}
		    }
		}
	    }

	    else if ((component & (AppearanceRetained.COLOR |
				   AppearanceRetained.MATERIAL|
				   AppearanceRetained.TRANSPARENCY|
				   AppearanceRetained.POLYGON |
				   AppearanceRetained.LINE|
				   AppearanceRetained.POINT)) != 0) {
		//		System.err.println("AppearanceRetained.POINT = "+AppearanceRetained.POINT);
		//		System.err.println("(app.mirror.changedFrequent & component) != 0 "+app.mirror.changedFrequent );
		//		System.err.println("ra.renderMolecule.soleUser "+ra.renderMolecule.soleUser);
		if ((app.mirror.changedFrequent & component) != 0 &&
		    ra.renderMolecule.soleUser) {
		    for (i = start; i < gaArr.length; i++) {
			ra = gaArr[i].getRenderAtom(view);
			if (ra== null || !ra.inRenderBin())
			    continue;

			if ((ra.renderMolecule.soleUserCompDirty& RenderMolecule.ALL_DIRTY_BITS) == 0 ) {
			    rmUpdateList.add(ra.renderMolecule);
			}
			ra.renderMolecule.soleUserCompDirty |= component;

		    }

		}
		else {
		    for (i = start; i < gaArr.length; i++) {
			ra = gaArr[i].getRenderAtom(view);
			if (ra== null || !ra.inRenderBin())
			    continue;
			TextureBin tb = ra.renderMolecule.textureBin;
			ra.renderMolecule.removeRenderAtom(ra);
			reInsertRenderAtom(tb, ra);

		    }
		}
	    }
	} else {
	    // Nothing is visible
	    if ((component & AppearanceRetained.RENDERING) != 0) {
		// Rendering attributes change
		visGAIsDirty = true;
		visQuery = true;
	    }
	}
    }




    void processModelClipChanged(Object[] args) {
	ModelClipRetained modelClip =
	    (ModelClipRetained)args[0];
	EnvironmentSet e;
	int component = ((Integer)args[1]).intValue();

	if ((component & (ModelClipRetained.SCOPE_CHANGED |
			  ModelClipRetained.BOUNDS_CHANGED |
			  ModelClipRetained.BOUNDINGLEAF_CHANGED)) != 0){
	    envDirty |= REEVALUATE_MCLIP;

	} else if ((component & (ModelClipRetained.ENABLE_CHANGED |
				 ModelClipRetained.ENABLES_CHANGED)) != 0) {
	    // need to render modelclip
	    if (!changedModelClips.contains(modelClip.mirrorModelClip))
		changedModelClips.add(modelClip.mirrorModelClip);

	    // need to reevaluate envset
	    envDirty |= REEVALUATE_MCLIP;

	} else {
	    UnorderList list = modelClip.mirrorModelClip.environmentSets;
	    synchronized (list) {
		EnvironmentSet envsets[] = (EnvironmentSet []) list.toArray(false);
		int size = list.size();
		for (int i = 0; i < size; i++) {
		    e = envsets[i];
		    e.canvasDirty |= Canvas3D.MODELCLIP_DIRTY;
		    if (!e.onUpdateList) {
			objUpdateList.add(e);
			e.onUpdateList = true;
		    }
		}
	    }
	}
    }


    /**
     * This routine get called whenever a region of the boundingleaf
     * changes
     */
    void processBoundingLeafChanged(Object[] args, long refTime){
	// Notify all users of this bounding leaf, it may
	// result in the re-evaluation of the lights/fogs/backgrounds
	Object[] users = (Object[])(args[3]);
	int i;

	// XXXX: Handle other object affected by bounding leaf changes
	for (i = 0; i < users.length; i++) {
	    LeafRetained leaf = (LeafRetained)users[i];
	    switch(leaf.nodeType) {
	    case NodeRetained.AMBIENTLIGHT:
	    case NodeRetained.POINTLIGHT:
	    case NodeRetained.SPOTLIGHT:
	    case NodeRetained.DIRECTIONALLIGHT:
		if (universe.renderingEnvironmentStructure.isLightScopedToThisView(leaf, view))
		    envDirty |=  REEVALUATE_LIGHTS;
		break;
	    case NodeRetained.LINEARFOG:
	    case NodeRetained.EXPONENTIALFOG:
		if (universe.renderingEnvironmentStructure.isFogScopedToThisView(leaf, view))
		    envDirty |=  REEVALUATE_FOG;
		break;
	    case NodeRetained.BACKGROUND:
		if (universe.renderingEnvironmentStructure.isBgScopedToThisView(leaf, view))
		    reEvaluateBg = true;
		break;
	    case NodeRetained.CLIP:
		if (universe.renderingEnvironmentStructure.isClipScopedToThisView(leaf, view))
		    reEvaluateClip = true;
		break;
	    case NodeRetained.MODELCLIP:
		if (universe.renderingEnvironmentStructure.isMclipScopedToThisView(leaf, view))
		    envDirty |=  REEVALUATE_MCLIP;
		break;
	    case NodeRetained.ALTERNATEAPPEARANCE:
		if (universe.renderingEnvironmentStructure.isAltAppScopedToThisView(leaf, view))		altAppearanceDirty = true;
		break;
	    default:
		break;
	    }
	}

    }

    void processOrientedShape3DChanged(Object[] gaArr) {

	RenderAtom ra;
	for (int i = 0; i < gaArr.length; i++) {
	    ra = ((GeometryAtom)gaArr[i]).getRenderAtom(view);
	    if (ra!= null && ra.inRenderBin() && !ra.inDirtyOrientedRAs()) {
		dirtyOrientedRAs.add(ra);
		ra.dirtyMask |= RenderAtom.IN_DIRTY_ORIENTED_RAs;
	    }
	}
    }


    void processShapeChanged(Object[] args, long refTime) {

	int component = ((Integer)args[1]).intValue();
	int i;
	RenderAtom ra;
	RenderAtom raNext;
	EnvironmentSet e;
	TextureBin tb;
	if ((component & Shape3DRetained.APPEARANCE_CHANGED) != 0) {
	    GeometryAtom[] gaArr = (GeometryAtom[])args[4];
	    if (gaArr.length > 0) {
		if (!gaArr[0].source.appearanceOverrideEnable) {
		    for (i =0; i < gaArr.length; i++) {
			ra = gaArr[i].getRenderAtom(view);
			if (ra == null || !ra.inRenderBin()) {
			    continue;
			}
			ra.app = ra.geometryAtom.source.appearance;
			e = ra.renderMolecule.textureBin.environmentSet;
			ra.renderMolecule.removeRenderAtom(ra);
			reInsertAttributeBin(e, ra);
		    }
		}
		else {
		    for (i =0; i < gaArr.length; i++) {
			ra = gaArr[i].getRenderAtom(view);
			if (ra == null || !ra.inRenderBin()) {
			    continue;
			}
			// if its using the alternate appearance continue ..
			if (ra.app == ra.geometryAtom.source.otherAppearance)
			    continue;
			ra.app = ra.geometryAtom.source.appearance;
			e = ra.renderMolecule.textureBin.environmentSet;
			ra.renderMolecule.removeRenderAtom(ra);
			reInsertAttributeBin(e, ra);
		    }
		}
	    }
	}
	else if ((component & Shape3DRetained.GEOMETRY_CHANGED) != 0) {
	    processDataChanged((Object[])args[2], (Object[])args[3], refTime);
	}
	else if ((component & Shape3DRetained.APPEARANCEOVERRIDE_CHANGED) != 0) {
	    AppearanceRetained app, saveApp = null;
	    Shape3DRetained saveShape = null;
	    GeometryAtom[] gaArr = (GeometryAtom[])args[4];
	    Object[] retVal;
	    for (i =0; i < gaArr.length; i++) {
		ra = gaArr[i].getRenderAtom(view);
		if (ra == null || !ra.inRenderBin())
		    continue;
		// Once shape could have many geometryAtoms, add the
		// mirrorShape as a user of an appearance only once

		if (saveShape != ra.geometryAtom.source) {
		    saveShape = ra.geometryAtom.source;
		    if (ra.geometryAtom.source.appearanceOverrideEnable) {
			retVal =universe.renderingEnvironmentStructure.getInfluencingAppearance(ra, view);
			saveShape.otherAppearance = (AppearanceRetained)retVal[1];
			if (retVal[0] == Boolean.TRUE) {
			    app = (AppearanceRetained)retVal[1];
			    if (app != null) {
				app.sgApp.addAMirrorUser(saveShape);
			    }
			}
			else {// use the default
			    app = ra.geometryAtom.source.appearance;
			}
		    }
		    else {
			// If it were using the alternate appearance
			// remove itself as the user
			if (ra.app == saveShape.otherAppearance &&
			    ra.app != null) {
			    ra.app.sgApp.removeAMirrorUser(saveShape);
			}
			app = ra.geometryAtom.source.appearance;
			saveShape.otherAppearance = null;
		    }
		    saveApp  = app;
		}
		else {
		    app = saveApp;
		}
		ra.app = app;
		e = ra.renderMolecule.textureBin.environmentSet;
		ra.renderMolecule.removeRenderAtom(ra);
		reInsertAttributeBin(e, ra);
	    }
	}

    }


    /**
     * Process a Text3D data change.  This involves removing all the
     * old geometry atoms in the list, and the creating new ones.
     */
    void processDataChanged(Object[] oldGaList,
			    Object[] newGaList, long referenceTime) {
	Shape3DRetained s, src;
	RenderAtom ra;
	RenderMolecule rm;
	int i, j;
	Transform3D trans;
	ArrayList rmChangedList = new ArrayList(5);
	GeometryRetained geo;
	GeometryAtom ga;

	for (i=0; i<oldGaList.length; i++) {
	    ga = ((GeometryAtom)oldGaList[i]);

	    // Make sure that there is atleast one geo that is non-null
	    geo = null;
	    for (int k = 0; (k < ga.geometryArray.length && geo == null); k++) {
		geo = ga.geometryArray[k];
	    }
	    if (geo == null)
		continue;


	    ra = ga.getRenderAtom(view);

	    if (ra != null && ra.inRenderBin()) {
		renderAtoms.remove(renderAtoms.indexOf(ra));
		removeARenderAtom(ra);
	    }
	}

	visQuery = true;
	visGAIsDirty = true;
    }


    void processMorphChanged(Object[] args, long refTime) {

	int component = ((Integer)args[1]).intValue();
	int i;
	RenderAtom ra;
	TextureBin tb;
	EnvironmentSet e;
	RenderAtom raNext;
	if ((component & MorphRetained.APPEARANCE_CHANGED) != 0) {
	    GeometryAtom[] gaArr = (GeometryAtom[])args[4];
	    if (gaArr.length > 0) {
		if (!gaArr[0].source.appearanceOverrideEnable) {
		    for (i =0; i < gaArr.length; i++) {
			ra = gaArr[i].getRenderAtom(view);
			if (ra == null || !ra.inRenderBin()) {
			    continue;
			}
			ra.app = ra.geometryAtom.source.appearance;
			e = ra.renderMolecule.textureBin.environmentSet;
			ra.renderMolecule.removeRenderAtom(ra);
			reInsertAttributeBin(e, ra);
		    }
		}
		else {
		    for (i =0; i < gaArr.length; i++) {
			ra = gaArr[i].getRenderAtom(view);
			if (ra == null || !ra.inRenderBin())
			    continue;

			// if its using the alternate appearance continue ..
			if (ra.app == ra.geometryAtom.source.otherAppearance)
			    continue;
			ra.app = ra.geometryAtom.source.appearance;
			e = ra.renderMolecule.textureBin.environmentSet;
			ra.renderMolecule.removeRenderAtom(ra);
			reInsertAttributeBin(e, ra);
		    }
		}
	    }
	}
	else if ((component & MorphRetained.APPEARANCEOVERRIDE_CHANGED) != 0) {
	    AppearanceRetained app, saveApp = null;
	    Shape3DRetained saveShape = null;
	    GeometryAtom[] gaArr = (GeometryAtom[])args[4];
	    Object[] retVal;

	    for (i =0; i < gaArr.length; i++) {
		ra = gaArr[i].getRenderAtom(view);
		if (ra == null || !ra.inRenderBin())
		    continue;
		// Once shape could have many geometryAtoms, add the
		// mirrorShape as a user of an appearance only once

		if (saveShape != ra.geometryAtom.source) {
		    saveShape = ra.geometryAtom.source;
		    if (ra.geometryAtom.source.appearanceOverrideEnable) {
			retVal =universe.renderingEnvironmentStructure.getInfluencingAppearance(ra, view);
			saveShape.otherAppearance = (AppearanceRetained)retVal[1];
			if (retVal[0] == Boolean.TRUE) {
			    app = (AppearanceRetained)retVal[1];
			    if (app != null) {
				app.sgApp.addAMirrorUser(saveShape);
			    }
			}
			else {// use the default
			    app = ra.geometryAtom.source.appearance;
			}
		    }
		    else {
			// If it were using the alternate appearance
			// remove itself as the user
			if (ra.app == saveShape.otherAppearance &&
			    ra.app != null) {
			    ra.app.sgApp.removeAMirrorUser(saveShape);
			}
			app = ra.geometryAtom.source.appearance;
			saveShape.otherAppearance = null;
		    }
		    saveApp  = app;
		}
		else {
		    app = saveApp;
		}
		ra.app = app;
		e = ra.renderMolecule.textureBin.environmentSet;
		ra.renderMolecule.removeRenderAtom(ra);
		reInsertAttributeBin(e, ra);
	    }
	}

    }



    /**
     * This routine gets called whenever the position of the view platform
     * has changed.
     */
    void updateViewPlatform(ViewPlatformRetained vp, float radius) {
	Transform3D trans = null;
	ViewPlatform viewP = view.getViewPlatform();
	if (viewP != null && (ViewPlatformRetained)viewP.retained == vp) {
	    vpcToVworld = vp.getCurrentLocalToVworld(null);
	    vpcToVworldDirty = true;
	    synchronized(vp) {
		vp.vprDirtyMask |= View.VPR_VIEWPLATFORM_DIRTY;
	    }

	    // vp schedSphere is already set and transform in
	    // BehaviorStructure thread which is run before
	    // RenderBin using vp.updateActivationRadius()
	    vpSchedSphereInVworld = vp.schedSphere;
	    reEvaluateBg = true;
	    reEvaluateClip = true;
	}

    }



    /**
     * This routine removes the GeometryAtoms from RenderBin
     */
    void processGeometryAtomsChanged(Object[] gaArr) {
	int i;
	RenderAtom ra;

	for (i = 0; i < gaArr.length; i++) {
	    ra = ((GeometryAtom)gaArr[i]).getRenderAtom(view);
	    if (ra != null && ra.inRenderBin()) {
		renderAtoms.remove(renderAtoms.indexOf(ra));
		removeARenderAtom(ra);
	    }
	}
    }

    /**
     * process Geometry changed, mark the display list
     * in which renderMolecule is as dirty
     */
    void processGeometryChanged(Object[] args) {

	Object[] gaList = (Object[]) args[0];

	GeometryRetained g = (GeometryRetained)args[1];
	GeometryAtom ga;

	int i;

	for (i = 0; i < gaList.length; i++) {
	    ga = ((GeometryAtom)gaList[i]);
	    RenderAtom renderAtom = ga.getRenderAtom(view);
	    if (renderAtom == null || !renderAtom.inRenderBin()) {
		continue;
	    }


	    // Add the renderMolecule to the dirty list so that
	    // display list will be recreated
	    int j = 0;
	    for ( j = 0; j < renderAtom.rListInfo.length; j++) {
		if (g == renderAtom.rListInfo[j].geometry())
		    break;
	    }
	    RenderAtomListInfo ra = (RenderAtomListInfo)renderAtom.rListInfo[j];
	    if ((ra.groupType & RenderAtom.DLIST) != 0)
		addDirtyRenderMolecule(ra.renderAtom.renderMolecule);

	    if ((ra.groupType & RenderAtom.SEPARATE_DLIST_PER_RINFO) != 0) {
		addDlistPerRinfo.add(ra);
	    }

	    if ((ra.groupType & RenderAtom.SEPARATE_DLIST_PER_GEO) != 0)
		    addGeometryDlist(ra);

	    // Raster send this message only for setImage()
            if (g instanceof RasterRetained) {
                Object[] objs = (Object[]) args[2];
                Texture2DRetained oldTex = (Texture2DRetained) objs[0];
                Texture2DRetained newTex = (Texture2DRetained) objs[1];

                RasterRetained geo = (RasterRetained)ra.geometry();
                if (oldTex != null) {
                    addTextureResourceFreeList(oldTex);
                    ImageComponentRetained oldImage = oldTex.images[0][0];
                    if (oldImage != null) {
                        removeNodeComponent(oldImage);
                    }
                }
                if (newTex != null) {
                    ImageComponentRetained newImage = newTex.images[0][0];
                    if (newImage != null) {
                        addNodeComponent(newImage);
                    }
                }
            }

	}

    }

    void addTextureBin(TextureBin tb) {
	textureBinList.add(tb);
    }


    void removeTextureBin(TextureBin tb) {
	textureBinList.remove(tb);
    }

    void addDirtyRenderMolecule(RenderMolecule rm) {
	int i;

	if ((rm.onUpdateList & RenderMolecule.IN_DIRTY_RENDERMOLECULE_LIST) == 0) {
	    if (rm.onUpdateList == 0) {
		objUpdateList.add(rm);
	    }
	    rm.onUpdateList |= RenderMolecule.IN_DIRTY_RENDERMOLECULE_LIST;
	    dirtyRenderMoleculeList.add(rm);
	}
    }



    void removeDirtyRenderMolecule(RenderMolecule rm) {
	int i;
	if ((rm.onUpdateList & RenderMolecule.IN_DIRTY_RENDERMOLECULE_LIST) != 0) {
	    rm.onUpdateList &= ~RenderMolecule.IN_DIRTY_RENDERMOLECULE_LIST;
	    if (rm.onUpdateList == 0) {
		objUpdateList.remove(rm);
	    }
	    dirtyRenderMoleculeList.remove(dirtyRenderMoleculeList.indexOf(rm));
	}
    }

    void updateDirtyDisplayLists(Canvas3D cv,
				 ArrayList rmList, ArrayList dlistPerRinfoList,
				 ArrayList raList, boolean useSharedCtx ) {
	int size, i, bitMask;
	Context ctx;
	long timeStamp;

	if (useSharedCtx) {
	    ctx = cv.screen.renderer.sharedCtx;
	    cv.makeCtxCurrent(ctx);
	    bitMask = cv.screen.renderer.rendererBit;
	    timeStamp = cv.screen.renderer.sharedCtxTimeStamp;
	} else {
	    ctx = cv.ctx;
	    bitMask = cv.canvasBit;
	    timeStamp = cv.ctxTimeStamp;
	}

	size = rmList.size();

	if (size > 0) {
	    for (i = size-1; i >= 0; i--) {
		RenderMolecule rm = (RenderMolecule)rmList.get(i);
		rm.updateDisplayList(cv);
	    }
	    rmList.clear();
	}

	size = dlistPerRinfoList.size();

	if (size > 0) {
	    for (i = size-1; i >= 0 ; i--) {
		Object[] obj = (Object[])dlistPerRinfoList.get(i);
		dlistRenderMethod.buildDlistPerRinfo((RenderAtomListInfo)obj[0], (RenderMolecule)obj[1], cv);
	    }
	    dlistPerRinfoList.clear();
	}

	size = raList.size();
	if (size > 0) {
	    RenderAtomListInfo ra;
	    GeometryArrayRetained geo;

	    for (i = size-1; i >= 0; i--) {
		ra = (RenderAtomListInfo)raList.get(i);
		geo = (GeometryArrayRetained) ra.geometry();
		geo.resourceCreationMask &= ~bitMask;
	    }

	    for (i = size-1; i >= 0; i--) {
		ra = (RenderAtomListInfo)raList.get(i);
		geo = (GeometryArrayRetained) ra.geometry();
		if ((geo.resourceCreationMask & bitMask) == 0) {
		    dlistRenderMethod.buildIndividualDisplayList(ra, cv, ctx);
		    geo.resourceCreationMask |= bitMask;
		    geo.setDlistTimeStamp(bitMask, timeStamp);
		}
	    }
	    raList.clear();
	}

	if (useSharedCtx) {
	    cv.makeCtxCurrent(cv.ctx);
	}
    }

    void removeRenderMolecule(RenderMolecule rm) {

        if ((rm.primaryMoleculeType &(RenderMolecule.DLIST_MOLECULE|RenderMolecule.SEPARATE_DLIST_PER_RINFO_MOLECULE)) != 0)
            renderMoleculeList.remove(rm);
    }

    void updateAllRenderMolecule(Canvas3D cv) {
	int i;
	int size = renderMoleculeList.size();

	if (size > 0) {
	    RenderMolecule[] rmArr = (RenderMolecule[])
		renderMoleculeList.toArray(false);
	    for (i = size-1 ; i >= 0; i--) {
		rmArr[i].updateAllPrimaryDisplayLists(cv);
	    }
	}

	size =  sharedDList.size();
	if (size > 0) {
	    RenderAtomListInfo ra;
	    GeometryArrayRetained geo;
	    RenderAtomListInfo arr[] = new RenderAtomListInfo[size];
	    arr = (RenderAtomListInfo []) sharedDList.toArray(arr);
	    int bitMask = cv.canvasBit;

	    // We need two passes to avoid extra buildDisplayList
	    // when geo are the same. The first pass clean the
	    // rendererBit. Note that we can't rely on
	    // resourceCreation since it is a force recreate.

	    for (i = size-1; i >= 0; i--) {
		geo = (GeometryArrayRetained) arr[i].geometry();
		geo.resourceCreationMask &= ~bitMask;
	    }

	    for (i = size-1; i >= 0; i--) {
		ra = arr[i];
		geo = (GeometryArrayRetained) ra.geometry();
		if ((geo.resourceCreationMask & bitMask) == 0) {
		    dlistRenderMethod.buildIndividualDisplayList(ra, cv, cv.ctx);
		    geo.resourceCreationMask |= bitMask;
		    geo.setDlistTimeStamp(bitMask, cv.ctxTimeStamp);
		}
	    }
	}
    }

    /**
     * This method is called to update all renderMolecule
     * for a shared context of a renderer
     */
    void updateAllRenderMolecule(Renderer rdr, Canvas3D cv) {
	int i;
	boolean setCtx = false;
	GeometryArrayRetained geo;
	int size = renderMoleculeList.size();

	if (size > 0) {
	    RenderMolecule[] rmArr = (RenderMolecule[])
		renderMoleculeList.toArray(false);

	    cv.makeCtxCurrent(rdr.sharedCtx);
	    setCtx = true;
	    for (i = size-1 ; i >= 0; i--) {
		rmArr[i].updateAllPrimaryDisplayLists(cv);
	    }
	}

	size =  sharedDList.size();
	if (size > 0) {
	    RenderAtomListInfo arr[] = new RenderAtomListInfo[size];
	    arr = (RenderAtomListInfo []) sharedDList.toArray(arr);
	    RenderAtomListInfo ra;

	    if (!setCtx) {
		cv.makeCtxCurrent(rdr.sharedCtx);
		setCtx = true;
	    }

	    // We need two passes to avoid extra buildDisplayList
	    // when geo are the same. The first pass clean the
	    // rendererBit.
	    int bitMask = cv.screen.renderer.rendererBit;
	    long timeStamp = cv.screen.renderer.sharedCtxTimeStamp;

	    for (i = size-1; i >= 0; i--) {
		geo = (GeometryArrayRetained) arr[i].geometry();
		geo.resourceCreationMask &= ~bitMask;
	    }

	    for (i = size-1; i >= 0; i--) {
		ra = arr[i];
		geo = (GeometryArrayRetained) ra.geometry();
		if ((geo.resourceCreationMask & bitMask) == 0) {
		    dlistRenderMethod.buildIndividualDisplayList(ra, cv,
								 rdr.sharedCtx);
		    geo.resourceCreationMask |= bitMask;
		    geo.setDlistTimeStamp(bitMask, timeStamp);
		}
	    }
	}
	if (setCtx) {
	    cv.makeCtxCurrent(cv.ctx);
	}
    }

    private void processText3DTransformChanged(Object[] list,
				       Object[] transforms,
				       long referenceTime) {
	int i, j, numShapes;
	GeometryAtom ga;
	RenderMolecule rm;
	RenderAtom ra;

	if (transforms.length != 0) {
	    numShapes = list.length;
	    for (i=0; i<numShapes; i++) {

		ga = (GeometryAtom)list[i];
		ra = ga.getRenderAtom(view);
		if (ra == null || !ra.inRenderBin()) {
		    continue;
		}
		/*
		  System.err.println("numShapes is " + numShapes +
		  " transforms.length is " + transforms.length);
		*/
		for (j=0; j<transforms.length; j++) {

		    ga.lastLocalTransformArray[j] = (Transform3D)transforms[j];




		    for(int k = 0; k < ra.rListInfo.length; k++) {
			if (ra.rListInfo[k].localToVworld == null) {
			    ra.rListInfo[k].localToVworld = new Transform3D();
			}
		    }

		    if (ra.isOriented() && !ra.inDirtyOrientedRAs()) {
			dirtyOrientedRAs.add(ra);
			ra.dirtyMask |= RenderAtom.IN_DIRTY_ORIENTED_RAs;
		    } else if (!ra.onUpdateList()) {
			ra.dirtyMask |= RenderAtom.ON_UPDATELIST;
			objUpdateList.add(ra);
		    }
		}
	    }
	}
    }


    private void processOrderedGroupRemoved(J3dMessage m) {
	int i, n;
	Object[] ogList = (Object[])m.args[0];
	Object[] ogChildIdList = (Object[])m.args[1];
	OrderedGroupRetained og;
	int index;
	int val;
	OrderedBin ob;
	OrderedChildInfo cinfo = null;

	/*
	  System.err.println("RB : processOrderedGroupRemoved message " + m);
	  System.err.println("RB : processOrderedGroupRemoved - ogList.length is " +
	  ogList.length);
	  System.err.println("RB : processOrderedGroupRemoved - obList " +
	  obList);
	*/
	for (n = 0; n < ogList.length; n++) {
	    og = (OrderedGroupRetained)ogList[n];
	    index = ((Integer)ogChildIdList[n]).intValue();

	    ob = og.getOrderedBin(view.viewIndex);
	    //	    System.err.println("Removed, index = "+index+" ob = "+ob);
	    if (ob != null) {
		// Add at the end of the childInfo, for remove we don't care about
		// the childId
		cinfo = new OrderedChildInfo(OrderedChildInfo.REMOVE, index, -1, null);
		ob.addChildInfo(cinfo);

		if (!ob.onUpdateList) {
		    obList.add(ob);
		    ob.onUpdateList = true;
		}
	    }
	}

    }


    private void processOrderedGroupInserted(J3dMessage m) {
	Object[] ogList = (Object[])m.args[0];
	Object[] ogChildIdList = (Object[])m.args[1];
	Object[] ogOrderedIdList = (Object[])m.args[2];


	OrderedGroupRetained og;;
	int index;
	int orderedId;
	OrderedBin ob;
	OrderedChildInfo cinfo;
	//	System.err.println("Inserted OG, index = "+index+" orderedId = "+orderedId+" og = "+og+" og.orderedBin = "+og.orderedBin);
	//	System.err.println("Inserted OG, orderedId = "+orderedId);
	//	System.err.println("Inserted, index = "+index+" oid = "+orderedId+" ob = "+ob);

	if(ogList == null)
	    return;

	for (int n = 0; n < ogList.length; n++) {
	    og = (OrderedGroupRetained)ogList[n];
	    index = ((Integer)ogChildIdList[n]).intValue();
	    orderedId = ((Integer)ogOrderedIdList[n]).intValue();
	    ob = og.getOrderedBin(view.viewIndex);
	    cinfo = null;


	    if (ob != null) {
		// Add at the end of the childInfo
		cinfo = new OrderedChildInfo(OrderedChildInfo.ADD, index, orderedId, null);
		ob.addChildInfo(cinfo);

		if (!ob.onUpdateList) {
		    obList.add(ob);
		    ob.onUpdateList = true;
		}
	    }
	}
    }

    private void processTransformChanged(long referenceTime) {
	int i, j, k, numRenderMolecules, n;
	Shape3DRetained s;
	RenderMolecule rm;
	RenderAtom ra;
	Transform3D trans;
	LightRetained[] lights;
	FogRetained fog;
	ModelClipRetained modelClip;
	AppearanceRetained app;
	Object[] list, nodesArr;
	UnorderList arrList;
	int size;

	targets = universe.transformStructure.getTargetList();

	// process geometry atoms
	arrList = targets.targetList[Targets.GEO_TARGETS];
	if (arrList != null) {
	    Object[] retVal;
	    size = arrList.size();
	    nodesArr = arrList.toArray(false);

	    //System.err.println("GS:");
	    for (n = 0; n < size; n++) {
		list = (Object[])nodesArr[n];

		for (i=0; i<list.length; i++) {

		    GeometryAtom ga = (GeometryAtom) list[i];
		    //System.err.println("  ga " + ga);
		    ra = ga.getRenderAtom(view);
		    if (ra == null || !ra.inRenderBin())
			continue;

		    rm = ra.renderMolecule;

		    if (rm != null && rm.renderBin == this) {

			if (ga.source.inBackgroundGroup && (rm.onUpdateList &
							    RenderMolecule.UPDATE_BACKGROUND_TRANSFORM) == 0) {
			    if (rm.onUpdateList == 0) {
				objUpdateList.add(rm);
			    }
			    rm.onUpdateList |=
				RenderMolecule.UPDATE_BACKGROUND_TRANSFORM;
			}

			lights = universe.renderingEnvironmentStructure.
			    getInfluencingLights(ra, view);
			fog = universe.renderingEnvironmentStructure.
			    getInfluencingFog(ra, view);
			modelClip = universe.renderingEnvironmentStructure.
			    getInfluencingModelClip(ra, view);

			if (ra.geometryAtom.source.appearanceOverrideEnable) {
			    retVal = universe.renderingEnvironmentStructure.getInfluencingAppearance(ra, view);
			    if (retVal[0] == Boolean.TRUE) {
				app  = (AppearanceRetained)retVal[1];
			    }
			    else {
				app = ra.geometryAtom.source.appearance;
			    }
			}
			else {
			    app = ra.geometryAtom.source.appearance;
			}
			// XXXX: Should we do a more extensive equals app?
			if (ra.envSet.equals(ra, lights, fog, modelClip) &&
			    app == ra.app) {

			    if (ra.hasSeparateLocaleVwcBounds()
				&& !ra.onLocaleVwcBoundsUpdateList()) {
				ra.dirtyMask |= ra.ON_LOCALE_VWC_BOUNDS_UPDATELIST;
				raLocaleVwcBoundsUpdateList.add(ra);
			    }

				// If the locale are different and the xform has changed
				// then we need to translate the rm's localToVworld by
				// the locale differences
			    if (locale != ga.source.locale) {
				if (rm.onUpdateList == 0) {
				    objUpdateList.add(rm);
				}
				rm.onUpdateList |= RenderMolecule.LOCALE_TRANSLATION;

			    }
			    if ((rm.primaryMoleculeType  & RenderMolecule.DLIST_MOLECULE) != 0) {
				if (rm.onUpdateList == 0) {
				    objUpdateList.add(rm);
				}
				rm.onUpdateList |= RenderMolecule.BOUNDS_RECOMPUTE_UPDATE;

			    }
				// Note that the rm LOCALE Translation update should ocuur
				// Before the ra is added to the object update list
				// It is a Text3D Molecule
			    else if ((rm.primaryMoleculeType  & RenderMolecule.TEXT3D_MOLECULE) != 0){

				if (!ra.onUpdateList()) {
				    ra.dirtyMask |= RenderAtom.ON_UPDATELIST;
				    objUpdateList.add(ra);
				}
			    }
			    if (ra.isOriented() && !ra.inDirtyOrientedRAs()) {
				dirtyOrientedRAs.add(ra);
				ra.dirtyMask |= RenderAtom.IN_DIRTY_ORIENTED_RAs;

			    }
				// If not opaque or in OG or is not a transparent bg geometry
				// and transp sort mode is sort_geometry, then ..
			    if (!ra.renderMolecule.isOpaqueOrInOG && ra.geometryAtom.source.geometryBackground == null && transpSortMode == View.TRANSPARENCY_SORT_GEOMETRY && !ra.inDepthSortList()) {
				// Do the updating of the centroid
				// when the render is running
				ra.geometryAtom.updateCentroid();
				//				System.err.println("========> adding to the dirty list .., transpSortMode = "+transpSortMode);
				if (dirtyDepthSortRenderAtom.add(ra)) {
				    numDirtyTinfo += ra.rListInfo.length;
				}
				/*
				else {
				    System.err.println("processTransformChanged: attempt to add RenderAtom already in dirty list");
				}
				*/
				ra.dirtyMask |= RenderAtom.IN_SORTED_POS_DIRTY_TRANSP_LIST;

			    }
			    continue;
			}
			// If the appearance has changed ..
			if (ra.app != app) {
			    if (ra.geometryAtom.source.appearanceOverrideEnable) {
				// If it was using the alternate appearance, then ..
				if (ra.app == ra.geometryAtom.source.otherAppearance) {
				    if (ra.app != null) {
					// remove this mirror shape from the user list
					ra.geometryAtom.source.otherAppearance.sgApp.removeAMirrorUser(ra.geometryAtom.source);
					ra.geometryAtom.source.otherAppearance = null;
				    }
				}
				// if we are using the alternate app, add the mirror
				// shape to the userlist
				if (app != ra.geometryAtom.source.appearance) {
				    // Second check is needed to prevent,
				    // the mirror shape
				    // that has multiple ra's to be added more than
				    // once
				    if (app != null && app != ra.geometryAtom.source.otherAppearance) {
					app.sgApp.addAMirrorUser(ra.geometryAtom.source);
					ra.geometryAtom.source.otherAppearance = app;
				    }
				}

			    }
			}


			// Remove the renderAtom from the current
			// renderMolecule and reinsert
			getNewEnvironment(ra, lights, fog, modelClip, app);
		    }
		}
	    }
	}

	// process misc environment nodes
	arrList = targets.targetList[Targets.ENV_TARGETS];
	if (arrList != null) {
	    size = arrList.size();
	    nodesArr = arrList.toArray(false);
	    for (n = 0; n < size; n++) {
		list = (Object[])nodesArr[n];
		for (i=0; i<list.length; i++) {

		    if (list[i] instanceof LightRetained && universe.renderingEnvironmentStructure.isLightScopedToThisView(list[i], view)) {
			if (!changedLts.contains(list[i]) )
			    changedLts.add(list[i]);
			envDirty |= REEVALUATE_LIGHTS; // mark the canvas as dirty as well
		    }
		    else if (list[i] instanceof ModelClipRetained && universe.renderingEnvironmentStructure.isMclipScopedToThisView(list[i], view)) {
			if (!changedModelClips.contains(list[i]))
			    changedModelClips.add(list[i]);
			envDirty |= REEVALUATE_MCLIP; // mark the canvas as dirty as well
		    }
		    else if (list[i] instanceof FogRetained && universe.renderingEnvironmentStructure.isFogScopedToThisView(list[i], view)) {
			if (!changedFogs.contains(list[i]))
			    changedFogs.add(list[i]);
			envDirty |= REEVALUATE_FOG; // mark the canvas as dirty as well
		    }
		    else if (list[i] instanceof AlternateAppearanceRetained && universe.renderingEnvironmentStructure.isAltAppScopedToThisView(list[i], view)) {
			altAppearanceDirty = true;
		    }
		}
	    }
	}

	// process ViewPlatform nodes
	arrList = targets.targetList[Targets.VPF_TARGETS];
	if (arrList != null) {
	    size = arrList.size();
	    nodesArr = arrList.toArray(false);
	    for (n = 0; n < size; n++) {
		list = (Object[])nodesArr[n];
		for (i=0; i<list.length; i++) {
		    float radius;
		    synchronized(list[i]) {
			radius = (float)((ViewPlatformRetained)list[i]).sphere.radius;
		    }
		    updateViewPlatform((ViewPlatformRetained)list[i], radius);
		}
	    }
	}

	targets = null;

	blUsers = universe.transformStructure.getBlUsers();
	if (blUsers != null) {
	    size = blUsers.size();
	    for (j = 0; j < size; j++) {
		LeafRetained mLeaf = (LeafRetained)blUsers.get(j);
		if (mLeaf instanceof LightRetained && universe.renderingEnvironmentStructure.isLightScopedToThisView(mLeaf, view)) {
		    envDirty |= REEVALUATE_LIGHTS;
		}
		else if (mLeaf instanceof FogRetained && universe.renderingEnvironmentStructure.isFogScopedToThisView(mLeaf, view)) {
		    envDirty |= REEVALUATE_FOG;
		}
		else if (mLeaf instanceof ModelClipRetained && universe.renderingEnvironmentStructure.isMclipScopedToThisView(mLeaf, view)) {
		    envDirty |= REEVALUATE_MCLIP;
		}
		else if (mLeaf instanceof AlternateAppearanceRetained && universe.renderingEnvironmentStructure.isAltAppScopedToThisView(mLeaf, view)) {
		    altAppearanceDirty = true;
		}
	    }
	    blUsers = null;
	}

	visQuery = true;

    }



    /**
     * This processes a LIGHT change.
     */
    private void processLightChanged() {
	int i, j, k, l, n;
	LightRetained lt;
	EnvironmentSet e;
	Object[] args;
	LightRetained[] mLts;
	int component;
	int lightSize = lightMessageList.size();

	for (n = 0; n < lightSize; n++) {
		J3dMessage msg = lightMessageList.get(n);
	    args = msg.args;
	    mLts = (LightRetained[])args[3];
	    component = ((Integer)args[1]).intValue();
	    lt = (LightRetained) args[0];


	    if ((component &(LightRetained.SCOPE_CHANGED |
			     LightRetained.BOUNDS_CHANGED |
			     LightRetained.BOUNDINGLEAF_CHANGED)) != 0){
		envDirty |= REEVALUATE_LIGHTS;
		component &= ~(LightRetained.SCOPE_CHANGED |
			       LightRetained.BOUNDS_CHANGED |
			       LightRetained.BOUNDINGLEAF_CHANGED);
	    }
	    // This is a light that is not a part of any
	    // environment set, first check if it is enabled
	    // if it is then reEvaluate all renderAtoms in the
	    // scene, otherwise do nothing
	    if (component != 0) {
		if (lt.nodeType == LightRetained.AMBIENTLIGHT) {
		    UnorderList list;
		    EnvironmentSet envsets[];
		    for (i = 0; i < mLts.length; i++) {
			LightRetained lti = mLts[i];
			list = lti.environmentSets;
			synchronized (list) {
			    int size = list.size();
			    if (size > 0) {
				envsets = (EnvironmentSet []) list.toArray(false);
				for (j = 0; j < size; j++) {
				    e = envsets[j];
				    e.canvasDirty |= Canvas3D.AMBIENTLIGHT_DIRTY;
				    if (!e.onUpdateList) {
					objUpdateList.add(e);
					e.onUpdateList = true;
				    }
				}
			    } else {
				if ((component & LightRetained.ENABLE_CHANGED) != 0) {
				    boolean value = lti.lightOn;
				    if (value) {
					if (!changedLts.contains(lti))
					    changedLts.add(lti);
					envDirty |= REEVALUATE_LIGHTS;
				    }
				}
			    }
			}
		    }
		} else {
		    for (i = 0; i < mLts.length; i++) {
			LightRetained lti = mLts[i];
                        if ((component & LightRetained.ENABLE_CHANGED) != 0) {
                            boolean value = ((Boolean)args[4]).booleanValue();
                            if (value) {
                                if (!changedLts.contains(lti))
                                    changedLts.add(lti);

                                envDirty |= REEVALUATE_LIGHTS;
                            }
                        }
			UnorderList list = lti.environmentSets;
			EnvironmentSet envsets[];
			synchronized (list) {
			    int size = list.size();
			    int lsize;
			    if (size > 0) {
				envsets = (EnvironmentSet []) list.toArray(false);
				if ((component & LightRetained.ENABLE_CHANGED) != 0) {
				    boolean value = ((Boolean)args[4]).booleanValue();
				    for (j = 0; j <size; j++) {
					e = envsets[j];
					lsize = e.lights.size();
					for (k = 0; k < lsize; k++) {
					    if (e.lights.get(k) == lti) {
						if (value == true)
						    e.enableMaskCache |= (1 << e.ltPos[k]);
						else
						    e.enableMaskCache &= ~(1 << e.ltPos[k]);
						break;
					    }
					}
					e.canvasDirty |= Canvas3D.LIGHTENABLES_DIRTY;
					if (!e.onUpdateList) {
					    objUpdateList.add(e);
					    e.onUpdateList = true;
					}
				    }
				} else {
				    for (j = 0; j < size; j++) {
					e = envsets[j];
					lsize = e.lights.size();
					for (k = 0; k < lsize; k++) {
					    if (e.lights.get(k) == lti) {
						e.lightBin.canvasDirty |= Canvas3D.LIGHTBIN_DIRTY;
						e.lightBin.lightDirtyMaskCache |= (1 << e.ltPos[k]);
						if (!e.lightBin.onUpdateList) {
						    e.lightBin.onUpdateList = true;
						    objUpdateList.add(e.lightBin);
						}
						break;
					    }
					}
				    }
				}
			    }
			} // end sync.
		    }
		}
	    }
	    msg.decRefcount();
	}

    }

    void processGeometryAtom(GeometryAtom ga, long referenceTime) {
	RenderAtom renderAtom;
	RenderMolecule rm;

	// System.err.println("+");


	GeometryRetained geo = null;
	for (int k = 0; (k < ga.geometryArray.length && geo == null); k++) {
	    geo = ga.geometryArray[k];
	}
	if (geo == null)
	    return;


	renderAtom = ga.getRenderAtom(view);

	if (renderAtom != null) {
	    renderAtom.lastVisibleTime = referenceTime;
	}

	if (renderAtom == null || renderAtom.inRenderBin()) {
	    return;
	}


	// If the geometry is all null , don't insert
	// Make sure that there is atleast one geo that is non-null

	if (renderAtom.geometryAtom.source.viewList != null) {
	    if (renderAtom.geometryAtom.source.viewList.contains(view)) {
		//		System.err.println("Inserting RenderAtom, ra = "+renderAtom);
		//		System.err.println("ga = "+renderAtom.geometryAtom+" renderAtom.geometryAtom.source.viewList = "+renderAtom.geometryAtom.source.viewList);
		rm = insertRenderAtom(renderAtom);
	    }
	}
	// No view specific scpoing
	else {
	    rm = insertRenderAtom(renderAtom);
	}

    }



    private void processBgGeometryAtoms(GeometryAtom[] nodes, long referenceTime) {
        int i;
	GeometryAtom ga;
	RenderAtom renderAtom;
	RenderMolecule rm;
	RenderAtomListInfo ra;
	GeometryRetained geo;

        for (i=0; i<nodes.length; i++) {
	    ga = nodes[i];

	    // Make sure that there is atleast one geo that is non-null
	    geo = null;
	    for (int k = 0; (k < ga.geometryArray.length && geo == null); k++) {
		geo = ga.geometryArray[k];
	    }
	    if (geo == null)
		continue;


	    renderAtom = ga.getRenderAtom(view);
	    if (renderAtom == null)
		return;

	    renderAtom.lastVisibleTime = referenceTime;
	    if (renderAtom.inRenderBin()) {
		continue;
	    }


	    // This means that the renderAtom was not visible in the last
	    // frame ,so , no contention with the renderer ...
	    rm = insertRenderAtom(renderAtom);
        }

    }

    /**
     * This method looks through the list of RenderAtoms to see if
     * compaction is needed.
     */
    private void checkForCompaction() {
	int i, numRas;
	int numDead = 0;
	int numAlive = 0;
	RenderAtom ra;

	if (!VirtualUniverse.mc.doCompaction) {
	    return;
	}

	numRas = renderAtoms.size();
	for (i=0; i<numRas; i++) {
		ra = renderAtoms.get(i);
	    // If the renderatom has not been visible for "notVisibleCount" then
	    // add it to the deadlist
	    if (ra.lastVisibleTime < removeCutoffTime) {
		numDead++;
	    }

	}
	numAlive = numRas - numDead;
	if (numAlive*2 < numDead) {
	    compact();
	}
    }

    /**
     * This sets the number of frames to render before changing the
     * removeCutoffTime
     */
    void setFrameCountCutoff(int cutoff) {
	frameCountCutoff = cutoff;
    }

    /**
     * This method stores the timestamp of the frame frameCountCuttoff
     * frames ago.  It also does compaction if it is needed.
     */
    void compact() {
	for (int i=0; i < renderAtoms.size();) {
		RenderAtom ra = renderAtoms.get(i);
	    if (ra.lastVisibleTime < removeCutoffTime) {
		renderAtoms.remove(i);
		removeARenderAtom(ra);
		continue;
	    }
	    i++;
	}

    }

    private void reEvaluateAlternateAppearance() {
	AppearanceRetained app;
	EnvironmentSet e;
	Object[] retVal;
	int sz = renderAtoms.size();

	for (int n = 0; n < sz; n++) {
		RenderAtom ra = renderAtoms.get(n);
	    if (!ra.inRenderBin() || !ra.geometryAtom.source.appearanceOverrideEnable)
		continue;

	    retVal = universe.renderingEnvironmentStructure.getInfluencingAppearance(ra, view);

	    if (retVal[0] == Boolean.TRUE) {
		app = (AppearanceRetained)retVal[1];
	    }
	    else {
		app = ra.geometryAtom.source.appearance;
	    }

	    if (app == ra.app)
		continue;

	    if (ra.geometryAtom.source.otherAppearance != app) {
		if (ra.geometryAtom.source.otherAppearance != null) {
		    ra.geometryAtom.source.otherAppearance.sgApp.removeAMirrorUser(ra.geometryAtom.source);
		}
		if (app != ra.geometryAtom.source.appearance) {
		    if (app != null) {
			app.sgApp.addAMirrorUser(ra.geometryAtom.source);
		    }
		    ra.geometryAtom.source.otherAppearance = app;
		}
		else {
		    ra.geometryAtom.source.otherAppearance = null;
		}
	    }
	    ra.app = app;
	    e = ra.envSet;
	    ra.renderMolecule.removeRenderAtom(ra);
	    reInsertAttributeBin(e, ra);
	}

    }

    private void reEvaluateAllRenderAtoms(boolean altAppDirty) {

	int sz = renderAtoms.size();

	for (int n = 0; n < sz; n++) {
	    LightRetained[] lights;
	    FogRetained newfog;
	    ModelClipRetained newModelClip;
	    AppearanceRetained app;
		RenderAtom ra = renderAtoms.get(n);
	    Object[] retVal;

	    if (!ra.inRenderBin())
		continue;

	    lights = universe.renderingEnvironmentStructure.getInfluencingLights(ra, view);
	    newfog = universe.renderingEnvironmentStructure.getInfluencingFog(ra, view);
	    newModelClip = universe.renderingEnvironmentStructure.getInfluencingModelClip(ra, view);


	    if (altAppDirty) {
		if (ra.geometryAtom.source.appearanceOverrideEnable) {
		    retVal = universe.renderingEnvironmentStructure.getInfluencingAppearance(ra, view);
		    if (retVal[0] == Boolean.TRUE) {
			app = (AppearanceRetained)retVal[1];
		    }
		    else {
			app = ra.geometryAtom.source.appearance;
		    }

		}
		else {
		    app = ra.geometryAtom.source.appearance;
		}
	    }
	    else {
		app = ra.app;
	    }

	    // If the lights/fog/model_clip of the render atom is the same
	    // as the old set of lights/fog/model_clip, then move on to the
	    // next renderAtom
	    // XXXX: Should app test for equivalent?
	    if (ra.envSet.equals(ra, lights, newfog, newModelClip) &&
		app == ra.app)
		continue;

	    if (altAppDirty && ra.geometryAtom.source.appearanceOverrideEnable) {
		if (app != ra.app) {
		    if (ra.geometryAtom.source.otherAppearance != app) {
			if (ra.geometryAtom.source.otherAppearance != null)
			    ra.geometryAtom.source.otherAppearance.sgApp.removeAMirrorUser(ra.geometryAtom.source);
			// If it is not the default appearance
			if (app != ra.geometryAtom.source.appearance) {
			    if (app != null) {
				app.sgApp.addAMirrorUser(ra.geometryAtom.source);
			    }
			    ra.geometryAtom.source.otherAppearance = app;
			}
			else {
			    ra.geometryAtom.source.otherAppearance = null;
			}
		    }
		}
	    }
	    getNewEnvironment(ra, lights, newfog, newModelClip, app);

	}
    }



    private void getNewEnvironment(RenderAtom ra, LightRetained[] lights,
			   FogRetained fog, ModelClipRetained modelClip,
			   AppearanceRetained app) {

	LightBin currentBin, lightBin;
	EnvironmentSet currentEnvSet, newBin;
	EnvironmentSet eNew = null;
	AttributeBin attributeBin;
	TextureBin textureBin;
	RenderMolecule renderMolecule;
	FogRetained newfog;
	LightBin addBin;
	OrderedCollection oc = null;
	int i;

	// Remove this renderAtom from this render Molecule
	ra.renderMolecule.removeRenderAtom(ra);

	eNew = null;
        if (ra.geometryAtom.source.geometryBackground == null) {
            if (ra.geometryAtom.source.orderedPath != null) {
                oc = findOrderedCollection(ra.geometryAtom, false);
                currentBin = oc.nextFrameLightBin;
                addBin = oc.addLightBins;
            } else {
                addBin = addOpaqueBin;
                currentBin= opaqueBin;
            }
        } else {
            if (ra.geometryAtom.source.orderedPath != null) {
                oc = findOrderedCollection(ra.geometryAtom, true);
                currentBin = oc.nextFrameLightBin;
                addBin = oc.addLightBins;
            } else {
                addBin = bgAddOpaqueBin;
                currentBin= bgOpaqueBin;
            }
        }
	lightBin = currentBin;

	while (currentBin != null && eNew == null) {

            // this test is always true for non-backgroundGeo bins
            if (currentBin.geometryBackground ==
                ra.geometryAtom.source.geometryBackground) {

	        currentEnvSet = currentBin.environmentSetList;
	        while (currentEnvSet != null) {
		    if (currentEnvSet.equals(ra, lights, fog, modelClip)) {
		        eNew = currentEnvSet;
		        break;
		    }
		    currentEnvSet = currentEnvSet.next;
	        }
	        // If envSet set is not found
	        // Check the "to-be-added" list of environmentSets for a match
	        if (eNew == null) {
		    int size = currentBin.insertEnvSet.size();
		    for (i = 0; i < size; i++) {
		        newBin = (EnvironmentSet)currentBin.insertEnvSet.get(i);
		        if (newBin.equals(ra, lights, fog, modelClip)) {
			    eNew = newBin;
			    break;
		        }
		    }
		}
	    }
	    currentBin = currentBin.next;
	}

	// Now check the to-be added lightbins
	if (eNew == null) {
	    currentBin = addBin;
	    while (currentBin != null) {

                // this test is always true for non-backgroundGeo bins
                if (currentBin.geometryBackground ==
                    ra.geometryAtom.source.geometryBackground) {

		    // Check the "to-be-added" list of environmentSets for a match
		    int size =  currentBin.insertEnvSet.size();
		    for (i = 0; i < size; i++) {
		        newBin = (EnvironmentSet)currentBin.insertEnvSet.get(i);
		        if (newBin.equals(ra, lights, fog, modelClip)) {
			    eNew = newBin;
			    break;
		        }
		    }
		}
		currentBin = currentBin.next;
	    }
	}


	if (eNew == null) {
	    // Need a new one
	    currentEnvSet = getEnvironmentSet(ra, lights, fog, modelClip);
	    // Find a lightbin that envSet fits into
	    currentBin = lightBin;
	    while (currentBin != null) {

                // the first test is always true for non-backgroundGeo bins
                if (currentBin.geometryBackground ==
		    ra.geometryAtom.source.geometryBackground &&
                    currentBin.willEnvironmentSetFit(currentEnvSet)) {

		    // there may be new lights define which needs to
		    // call native updateLight().
		    // When using existing lightBin we have to force
		    // reevaluate Light.
		    for (i=0; i < lights.length; i++) {
			if (!changedLts.contains(lights[i]))
			    changedLts.add(lights[i]);
			envDirty |=  REEVALUATE_LIGHTS;

		    }
		    break;
		}
		currentBin = currentBin.next;
	    }

	    // Now check the to-be added lightbins
	    if (currentBin == null) {
		currentBin = addBin;
		while (currentBin != null) {

                    // the first test is always true for non-backgroundGeo bins
                    if (currentBin.geometryBackground ==
                        ra.geometryAtom.source.geometryBackground &&
                        currentBin.willEnvironmentSetFit(currentEnvSet)) {

			// there may be new lights define which needs to
			// call native updateLight().
			// When using existing lightBin we have to force
			// reevaluate Light.
			for (i=0; i < lights.length; i++) {
			    if (!changedLts.contains(lights[i]))
				changedLts.add(lights[i]);
			    envDirty |=  REEVALUATE_LIGHTS;

			}
			break;
		    }
		    currentBin = currentBin.next;
		}
	    }

	    if (currentBin == null) {
		// Need a new lightbin
		currentBin = getLightBin(maxLights,
					 ra.geometryAtom.source.geometryBackground, false);
		if (addBin != null) {
		    currentBin.next = addBin;
		    addBin.prev = currentBin;
		}
		if (ra.geometryAtom.source.orderedPath != null) {
		    if (!oc.onUpdateList) {
			objUpdateList.add(oc);
			oc.onUpdateList = true;
		    }
		    oc.addLightBins = currentBin;
		} else  {
		    if (ra.geometryAtom.source.geometryBackground == null)
			addOpaqueBin = currentBin;
		    else
			bgAddOpaqueBin = currentBin;
		}

	    }
	    eNew = currentEnvSet;
	    currentBin.addEnvironmentSet(eNew, this);

	}
	ra.fog = fog;
	ra.lights = lights;
	ra.modelClip = modelClip;
	ra.app = app;
	reInsertAttributeBin(eNew, ra);

    }

    private void reInsertAttributeBin(EnvironmentSet e, RenderAtom ra) {
	AttributeBin ab;
	// Just go up to the environment and re-insert
	ab = findAttributeBin(e, ra);
	reInsertShaderBin(ab, ra);
    }

    private void reInsertShaderBin(AttributeBin ab, RenderAtom ra) {
	ShaderBin sb;

	// System.err.println("RenderBin.reInsertShaderBin() ra= " + ra);
	sb = findShaderBin(ab, ra);
	reInsertTextureBin(sb, ra);
    }

    private void reInsertTextureBin(ShaderBin sb, RenderAtom ra) {
	TextureBin tb;

	tb = findTextureBin(sb, ra);
	reInsertRenderAtom(tb, ra);
    }

    private void reInsertRenderAtom(TextureBin tb, RenderAtom ra) {
	RenderMolecule newRm;
	// Just go up to the texture bin and re-insert
	newRm = findRenderMolecule(tb, ra);
    }

    private void computeViewFrustumBBox(BoundingBox viewFrustumBBox) {
	//Initial view frustumBBox BBox
	viewFrustumBBox.lower.x = Float.POSITIVE_INFINITY;
	viewFrustumBBox.lower.y = Float.POSITIVE_INFINITY;
	viewFrustumBBox.lower.z = Float.POSITIVE_INFINITY;
	viewFrustumBBox.upper.x = Float.NEGATIVE_INFINITY;
	viewFrustumBBox.upper.y = Float.NEGATIVE_INFINITY;
	viewFrustumBBox.upper.z = Float.NEGATIVE_INFINITY;

	Canvas3D canvases[] = view.getCanvases();
	for (int i=0; i< canvases.length; i++) {
	    Canvas3D canvas = canvases[i];

	    //Initial view frustumBBox BBox
	    canvasFrustumBBox.lower.x = Float.POSITIVE_INFINITY;
	    canvasFrustumBBox.lower.y = Float.POSITIVE_INFINITY;
	    canvasFrustumBBox.lower.z = Float.POSITIVE_INFINITY;
	    canvasFrustumBBox.upper.x = Float.NEGATIVE_INFINITY;
	    canvasFrustumBBox.upper.y = Float.NEGATIVE_INFINITY;
	    canvasFrustumBBox.upper.z = Float.NEGATIVE_INFINITY;

	    canvas.updateViewCache(true, null, canvasFrustumBBox, false);

	    if(viewFrustumBBox.lower.x > canvasFrustumBBox.lower.x)
		viewFrustumBBox.lower.x = canvasFrustumBBox.lower.x;
	    if(viewFrustumBBox.lower.y > canvasFrustumBBox.lower.y)
		viewFrustumBBox.lower.y = canvasFrustumBBox.lower.y;
	    if(viewFrustumBBox.lower.z > canvasFrustumBBox.lower.z)
		viewFrustumBBox.lower.z = canvasFrustumBBox.lower.z;

	    if(viewFrustumBBox.upper.x < canvasFrustumBBox.upper.x)
		viewFrustumBBox.upper.x = canvasFrustumBBox.upper.x;
	    if(viewFrustumBBox.upper.y < canvasFrustumBBox.upper.y)
		viewFrustumBBox.upper.y = canvasFrustumBBox.upper.y;
	    if(viewFrustumBBox.upper.z < canvasFrustumBBox.upper.z)
		viewFrustumBBox.upper.z = canvasFrustumBBox.upper.z;
	}
    }



    /**
     * This inserts a RenderAtom into the appropriate bin.
     */
    private RenderMolecule insertRenderAtom(RenderAtom ra) {
	LightBin lightBin;
	EnvironmentSet environmentSet;
	AttributeBin attributeBin;
	ShaderBin shaderBin;
	TextureBin textureBin;
	RenderMolecule renderMolecule;
	OrderedCollection oc;
	AppearanceRetained app;
	Object[] retVal;
        GeometryAtom ga = ra.geometryAtom;

	//	System.err.println("insertRenderAtom ga " + ra.geometryAtom);
        // determine if a separate copy of localeVwcBounds is needed
        // based on the locale info

        if (ra.localeVwcBounds == null) {
            // Handle multiple locales
            if (!locale.hiRes.equals(ga.source.locale.hiRes)) {
                ga.source.locale.hiRes.difference(locale.hiRes,
						  localeTranslation);
                ra.localeVwcBounds = new BoundingBox();
                ra.localeVwcBounds.translate(ga.source.vwcBounds,
					     localeTranslation);
		ra.dirtyMask |= RenderAtom.HAS_SEPARATE_LOCALE_VWC_BOUNDS;
            }
            else {
		ra.dirtyMask &= ~RenderAtom.HAS_SEPARATE_LOCALE_VWC_BOUNDS;
                ra.localeVwcBounds = ga.source.vwcBounds;
            }
        }


	// If the appearance is overrideable, then get the
	// applicable appearance
	if (ga.source.appearanceOverrideEnable) {
	    retVal = universe.renderingEnvironmentStructure.getInfluencingAppearance(ra, view);
	    // If its a valid alternate appaearance
	    if (retVal[0] == Boolean.TRUE) {
		app = (AppearanceRetained)retVal[1];
		ra.app = app;
		if (ga.source.otherAppearance != app) {
		    if (ga.source.otherAppearance != null)
			ga.source.otherAppearance.sgApp.
			    removeAMirrorUser(ga.source);
		    ga.source.otherAppearance = app;
		    if (app != null)
			ra.app.sgApp.addAMirrorUser(ga.source);
		}
	    }
	    else {
		ra.app = ga.source.appearance;

	    }
	} else {
	    ra.app = ga.source.appearance;
	}
	// Call environment set, only after the appearance has been
	// determined
	environmentSet = findEnvironmentSet(ra);
	attributeBin = findAttributeBin(environmentSet, ra);

	// System.err.println("RenderBin : findShaderBin()");
	shaderBin = findShaderBin(attributeBin, ra);

	textureBin = findTextureBin(shaderBin, ra);
	renderMolecule = findRenderMolecule(textureBin, ra);
        ra.setRenderBin(true);
	renderAtoms.add(ra);

        if (ga.source instanceof OrientedShape3DRetained) {
	    // dirty initially
	    dirtyOrientedRAs.add(ra);
	    ra.dirtyMask |= RenderAtom.IN_DIRTY_ORIENTED_RAs;
	    ra.dirtyMask |= RenderAtom.IS_ORIENTED;
	    for(int k = 0; k < ra.rListInfo.length; k++) {
		if (ra.rListInfo[k].localToVworld == null) {
		    ra.rListInfo[k].localToVworld = new Transform3D();
		}
	    }
        }

        if (renderMolecule.primaryMoleculeType  ==
	    RenderMolecule.TEXT3D_MOLECULE) {
            if (!ra.onUpdateList()) {
		ra.dirtyMask |= RenderAtom.ON_UPDATELIST;
                objUpdateList.add(ra);
            }
        }

        // ra.needSeparateLocaleVwcBounds flag is determined in
        // RenderMolecule.addRenderAtom based on the render method type.
        // That's why the localeVwcBounds has to be reevaluated here again
	// If after compaction being added in, then we just need to
	// set the updated vwcBounds, there is no need to allocate
	if (ra.needSeparateLocaleVwcBounds()) {
	    if (!ra.hasSeparateLocaleVwcBounds()) {
		ra.dirtyMask |= RenderAtom.HAS_SEPARATE_LOCALE_VWC_BOUNDS;
		ra.localeVwcBounds = new BoundingBox(ga.source.vwcBounds);
		ra.dirtyMask |= ra.ON_LOCALE_VWC_BOUNDS_UPDATELIST;
		raLocaleVwcBoundsUpdateList.add(ra);
	    }
	    else {
		ra.localeVwcBounds.set(ga.source.vwcBounds);
		ra.dirtyMask |= ra.ON_LOCALE_VWC_BOUNDS_UPDATELIST;
		raLocaleVwcBoundsUpdateList.add(ra);
	    }
	}
	return (renderMolecule);
    }

    private OrderedCollection findOrderedCollection(GeometryAtom ga,
					    boolean doBackground) {
        int i, n;
        int oi; // an id which identifies a children of the orderedGroup
        int ci; // child index of the ordered group
	int index;
	ArrayList list = null;
	int val;

        OrderedGroupRetained og;
        OrderedCollection oc = null;
        ArrayList ocs;
        ArrayList parentChildOrderedBins;
        OrderedBin parentOrderedBin;
        int parentOrderedChildId;
	OrderedBin ob;
        OrderedPathElement ope;

	// Since the table has been incremented, in response to OG addition,
	// but the ordered collecyions has not been added yet, we need to
	// check what the original index into the ordered collection
	// should be
	int adjustment;

        if (doBackground) {
            parentChildOrderedBins = bgOrderedBins;
        } else {
            parentChildOrderedBins = orderedBins;
        }

        parentOrderedBin = null;
        parentOrderedChildId = -1;

        for (i=0; i<ga.source.orderedPath.pathElements.size(); i++) {
            ope = (OrderedPathElement)ga.source.orderedPath.pathElements.get(i);
            og = ope.orderedGroup;
            oi = ope.childId.intValue();

	    ob = og.getOrderedBin(view.viewIndex);
	    if (ob == null) {
		// create ordered bin tree
		ob = new OrderedBin(og.childCount, og);
		og.setOrderedBin(ob, view.viewIndex);

		index = -1;
		for (n = 0; n < orderedBinsList.size(); n++) {
		    if (parentChildOrderedBins == orderedBinsList.get(n)) {
			index = n;
			break;
		    }

		}
		if (index == -1) {
		    orderedBinsList.add(parentChildOrderedBins);
		    list = new ArrayList(5);
		    list.add(ob);
		    toBeAddedBinList.add(list);
		}
		else {
		    list = (ArrayList)toBeAddedBinList.get(index);
		    list.add(ob);
		}
	    }
            ocs = ob.orderedCollections;
	    OrderedChildInfo cinfo = ob.lastChildInfo;
	    boolean found = false;
	    // Check if a oc is already creates for this oi
	    // Start from the last child that was added and work backwards,
	    // in case the child
	    // was added and removed and then added back the same frame, we get the
	    // correct oc
	    while (cinfo != null && !found) {
		if (cinfo.type == OrderedChildInfo.ADD) {
		    if (cinfo.orderedId == oi) {
			oc = cinfo.value;
			if (oc == null) {
			    oc = new OrderedCollection();
			    cinfo.value = oc;
			}
			found = true;
		    }
		}
		cinfo = cinfo.prev;
	    }
	    // If we are in the update_view case then check the oi
	    // exists in the setOCForOI list ..
	    for (n = 0; n < ob.setOCForOI.size(); n++) {
		val = ((Integer)ob.setOCForOI.get(n)).intValue();
		if (oi == val) {
		    oc = (OrderedCollection)ob.valueOfSetOCForOI.get(n);
		    found = true;
		}
	    }
	    // The list is not going to be modified by any additions
	    // that have happened ...
	    // Then this child must exists from the previous frame, so
	    // get the location
	    if (!found) {
		// The case below happens when there have been some insert
		// ordered nodes, but update_view happens later and
		// so the earlier insert ordered nodes are not
		// seen by renderBin!
		if (og.orderedChildIdTable == null || oi >= og.orderedChildIdTable.length) {
		    // Create a new category that adds Info based only on oi
		    // which will be added to the orderedBin after the
		    // idTable reflects the correct childId for the next frame
		    ob.setOCForOI.add(new Integer(oi));
		    oc = new OrderedCollection();
		    ob.valueOfSetOCForOI.add(oc);
		    if (!ob.onUpdateList) {
			obList.add(ob);
			ob.onUpdateList = true;
		    }
		}
		else {
		    ci = og.orderedChildIdTable[oi];

		    for (n = 0; n < ob.setOCForCI.size(); n++) {
			val = ((Integer)ob.setOCForCI.get(n)).intValue();
			if (val == ci) {

			    oc=(OrderedCollection)ob.valueOfSetOCForCI.get(n);
			    if (oc == null) {
				oc = new OrderedCollection();
				ob.valueOfSetOCForCI.set(n, oc);
			    }

			    break;
			}
		    }
		    if (n == ob.setOCForCI.size()) {
			oc = (OrderedCollection)ocs.get(ci);
			if (oc == null) {
			    oc = new OrderedCollection();
			    ob.setOCForCI.add(new Integer(ci));
			    ob.valueOfSetOCForCI.add(oc);
			    if (!ob.onUpdateList) {
				obList.add(ob);
				ob.onUpdateList = true;
			    }
			}
		    }
		}
            }
            if (oc.nextFrameLightBin == null) {
                oc.nextFrameLightBin = getLightBin(maxLights,
						   ga.source.geometryBackground, false);
                oc.nextFrameLightBin.setOrderedInfo(oc);

		if (!oc.onUpdateList) {
		    objUpdateList.add(oc);
		    oc.onUpdateList = true;
		}
            }

            parentChildOrderedBins = oc.childOrderedBins;
            parentOrderedBin = ob;
            parentOrderedChildId = oi;
        }
        return (oc);
    }

    private void removeOrderedHeadLightBin(LightBin lightBin) {
        int i, k;
        int oi; // an id which identifies a children of the orderedGroup
        int ci; // child index of the ordered group
        ArrayList ocs;
        OrderedCollection oc;
        OrderedBin ob, savedOb;
	int n, val;



	oc = lightBin.orderedCollection;

	oc.lightBin = lightBin.next;
	oc.nextFrameLightBin = oc.lightBin;



	if (oc.lightBin != null) {
	    // Make this lightBin the head of the lightBin;
	    oc.lightBin.prev = null;
	    oc.lightBin.orderedCollection = oc;
	}


    }


    /**
     * This gets a new EnviornmentSet.  It creates one if there are none
     * on the freelist.
     */
    private EnvironmentSet getEnvironmentSet(RenderAtom ra, LightRetained[] lights,
            FogRetained fog, ModelClipRetained modelClip) {
        EnvironmentSet envSet;

        envSet = new EnvironmentSet(ra, lights, fog, modelClip, this);
        return (envSet);
    }

    /**
     * This finds or creates an AttributeBin for a given RenderAtom.
     */
    private AttributeBin findAttributeBin(EnvironmentSet envSet, RenderAtom ra) {
	int i;
	AttributeBin currentBin;
	RenderingAttributesRetained renderingAttributes;
	if (ra.app == null) {
	    renderingAttributes = null;
	} else {
	    renderingAttributes = ra.app.renderingAttributes;
	}

	currentBin = envSet.attributeBinList;
	while (currentBin != null) {
	    if (currentBin.equals(renderingAttributes, ra)) {
		return(currentBin);
	    }
	    currentBin = currentBin.next;
	}
	// Check the "to-be-added" list of attributeBins for a match
	for (i = 0; i < envSet.addAttributeBins.size(); i++) {
	    currentBin = (AttributeBin)envSet.addAttributeBins.get(i);
	    if (currentBin.equals(renderingAttributes, ra)) {
		return(currentBin);
	    }
	}
	currentBin = getAttributeBin(ra.app, renderingAttributes);
	envSet.addAttributeBin(currentBin, this);
	return(currentBin);
    }

    /**
     * This finds or creates an ShaderBin for a given RenderAtom.
     */
    private ShaderBin findShaderBin(AttributeBin attributeBin, RenderAtom ra) {
	int i, size;
	ShaderBin currentBin;
	ShaderAppearanceRetained sApp;

	if((ra != null) && (ra.app instanceof ShaderAppearanceRetained))
	    sApp = (ShaderAppearanceRetained)ra.app;
	else
	    sApp = null;

	currentBin = attributeBin.shaderBinList;
	while (currentBin != null) {
	    if (currentBin.equals(sApp)) {
		return currentBin;
	    }
	    currentBin = currentBin.next;
	}

	// Check the "to-be-added" list of shaderBins for a match
	size = attributeBin.addShaderBins.size();
	for (i = 0; i < size; i++) {
	    currentBin = (ShaderBin)attributeBin.addShaderBins.get(i);
	    if (currentBin.equals(sApp)) {
		return currentBin;
	    }
	}

	currentBin = getShaderBin(sApp);
	attributeBin.addShaderBin(currentBin, this, sApp);
	return currentBin;
    }

    /**
     * This finds or creates a TextureBin for a given RenderAtom.
     */
    private TextureBin findTextureBin(ShaderBin shaderBin, RenderAtom ra) {
	int i, size;
	TextureBin currentBin;
	TextureRetained texture;
	TextureUnitStateRetained texUnitState[];

	if (ra.app == null) {
	    texUnitState = null;
	} else {
	    texUnitState = ra.app.texUnitState;
	}

	currentBin = shaderBin.textureBinList;
	while (currentBin != null) {
	    if (currentBin.equals(texUnitState, ra)) {
		//System.err.println("1: Equal");
		return(currentBin);
	    }
	    currentBin = currentBin.next;
	}
	// Check the "to-be-added" list of TextureBins for a match
	size = shaderBin.addTextureBins.size();
	for (i = 0; i < size; i++) {
	    currentBin = (TextureBin)shaderBin.addTextureBins.get(i);
	    if (currentBin.equals(texUnitState, ra)) {
		//System.err.println("2: Equal");
		return(currentBin);
	    }
	}
	// get a new texture bin for this texture unit state
	currentBin = getTextureBin(texUnitState, ra.app);
	shaderBin.addTextureBin(currentBin, this, ra);
	return(currentBin);
    }

    /**
     * This finds or creates a RenderMolecule for a given RenderAtom.
     */
    private RenderMolecule findRenderMolecule(TextureBin textureBin,
				      RenderAtom ra) {

	RenderMolecule currentBin;
	PolygonAttributesRetained polygonAttributes;
	LineAttributesRetained lineAttributes;
	PointAttributesRetained pointAttributes;
	MaterialRetained  material;
	ColoringAttributesRetained coloringAttributes;
	TransparencyAttributesRetained transparencyAttributes;
	int i;
	ArrayList list;
	TextureUnitStateRetained texUnitState[];
	RenderingAttributesRetained renderingAttributes;
	HashMap rmap = null, addmap = null;

	if (ra.app == null) {
	    polygonAttributes = null;
	    lineAttributes = null;
	    pointAttributes = null;
	    material = null;
	    coloringAttributes = null;
	    transparencyAttributes = null;
	    renderingAttributes = null;
	    texUnitState = null;
	} else {
	    polygonAttributes = ra.app.polygonAttributes;
	    lineAttributes = ra.app.lineAttributes;
	    pointAttributes = ra.app.pointAttributes;
	    material = ra.app.material;
	    coloringAttributes = ra.app.coloringAttributes;
	    transparencyAttributes = ra.app.transparencyAttributes;
	    renderingAttributes = ra.app.renderingAttributes;
	    texUnitState = ra.app.texUnitState;
	}

	// Get the renderMoleculelist for this xform
	if (ra.isOpaque()) {
	    rmap = textureBin.opaqueRenderMoleculeMap;
	    addmap = textureBin.addOpaqueRMs;
	}
	else {
	    rmap = textureBin.transparentRenderMoleculeMap;
	    addmap = textureBin.addTransparentRMs;
	}
	currentBin = (RenderMolecule)rmap.get(ra.geometryAtom.source.localToVworld[0]);

	while (currentBin != null) {
	    if (currentBin.equals(ra,
				  polygonAttributes, lineAttributes,
				  pointAttributes, material,
				  coloringAttributes,
				  transparencyAttributes,
				  ra.geometryAtom.source.localToVworld[0])) {

		currentBin.addRenderAtom(ra, this);
		ra.envSet = ra.renderMolecule.textureBin.environmentSet;
		// If the locale has changed for an existing renderMolecule
		// handle the RmlocaleToVworld
		return(currentBin);
	    }
	    currentBin = currentBin.next;
	}
	// Check the "to-be-added" list of renderMolecules for a match
	if ((list = (ArrayList)addmap.get(ra.geometryAtom.source.localToVworld[0])) != null) {
	    for (i = 0; i < list.size(); i++) {
		currentBin = (RenderMolecule)list.get(i);
		if (currentBin.equals(ra,
				      polygonAttributes, lineAttributes,
				      pointAttributes, material,
				      coloringAttributes,
				      transparencyAttributes,
				      ra.geometryAtom.source.localToVworld[0])) {
		    currentBin.addRenderAtom(ra, this);
		    return(currentBin);
		}
	    }
	}


	currentBin = getRenderMolecule(ra.geometryAtom,
				       polygonAttributes,
				       lineAttributes,
				       pointAttributes,
				       material,
				       coloringAttributes,
				       transparencyAttributes,
				       renderingAttributes,
				       texUnitState,
				       ra.geometryAtom.source.localToVworld[0],
				       ra.geometryAtom.source.localToVworldIndex[0]);
	textureBin.addRenderMolecule(currentBin, this);
	currentBin.addRenderAtom(ra, this);
	return(currentBin);
    }

    /**
     * This gets a new ShaderBin.  It creates one if there are none
     * on the freelist.
     */
    private ShaderBin getShaderBin(ShaderAppearanceRetained sApp) {
        return new ShaderBin( sApp, this);
    }

    /**
     * This gets a new AttributeBin.  It creates one if there are none
     * on the freelist.
     */
    private AttributeBin getAttributeBin(AppearanceRetained app, RenderingAttributesRetained ra) {
        return new AttributeBin(app, ra, this);
    }

    /**
     * This gets a new LightBin.  It creates one if there are none
     * on the freelist.
     */
    private LightBin getLightBin(int maxLights, BackgroundRetained bg, boolean inOpaque) {
        LightBin lightBin;

        lightBin = new LightBin(maxLights, this, inOpaque);

        lightBin.geometryBackground = bg;
        return (lightBin);
    }

    /**
     * This gets a new TextureBin.  It creates one if there are none
     * on the freelist.
     */
    private TextureBin getTextureBin(TextureUnitStateRetained texUnitState[],
            AppearanceRetained app) {
        return new TextureBin(texUnitState, app, this);
    }

    /**
     * This gets a new RenderMolecule.  It creates one if there are none
     * on the freelist.
     */
    private  RenderMolecule getRenderMolecule(GeometryAtom ga,
            PolygonAttributesRetained polya,
            LineAttributesRetained linea,
            PointAttributesRetained pointa,
            MaterialRetained material,
            ColoringAttributesRetained cola,
            TransparencyAttributesRetained transa,
            RenderingAttributesRetained ra,
            TextureUnitStateRetained[] texUnits,
            Transform3D[] transform,
            int[] transformIndex) {

        return new RenderMolecule(ga, polya, linea, pointa,
                material, cola, transa, ra,
                texUnits,
                transform, transformIndex, this);
    }


    /**
     * This finds or creates an EnviornmentSet for a given RenderAtom.
     * This also deals with empty LightBin lists.
     */
    private EnvironmentSet findEnvironmentSet(RenderAtom ra) {
	LightBin currentBin, lightBin ;
	EnvironmentSet currentEnvSet, newBin;
	int i;
	LightBin addBin = null;
	OrderedCollection oc = null;

        if (ra.geometryAtom.source.geometryBackground == null) {
            if (ra.geometryAtom.source.orderedPath != null) {
    	        oc = findOrderedCollection(ra.geometryAtom, false);
	        currentBin = oc.nextFrameLightBin;
	        addBin = oc.addLightBins;
	    } else  {
	        currentBin = opaqueBin;
	        addBin = addOpaqueBin;
	    }
        } else {
            if (ra.geometryAtom.source.orderedPath != null) {
                oc = findOrderedCollection(ra.geometryAtom, true);
                currentBin = oc.nextFrameLightBin;
                addBin = oc.addLightBins;
            } else {
                currentBin = bgOpaqueBin;
                addBin = bgAddOpaqueBin;

            }
	}
	lightBin = currentBin;


	ra.lights = universe.renderingEnvironmentStructure.
	    getInfluencingLights(ra, view);
	ra.fog = universe.renderingEnvironmentStructure.
	    getInfluencingFog(ra, view);
	ra.modelClip = universe.renderingEnvironmentStructure.
	    getInfluencingModelClip(ra, view);

	while (currentBin != null) {
	    // this test is always true for non-backgroundGeo bins
            if (currentBin.geometryBackground ==
                ra.geometryAtom.source.geometryBackground) {

	        currentEnvSet = currentBin.environmentSetList;
    	        while (currentEnvSet != null) {
		    if (currentEnvSet.equals(ra, ra.lights, ra.fog, ra.modelClip)) {
			return(currentEnvSet);
		    }
		    currentEnvSet = currentEnvSet.next;
	        }
	        // Check the "to-be-added" list of environmentSets for a match
	        for (i = 0; i < currentBin.insertEnvSet.size(); i++) {
		    newBin = (EnvironmentSet)currentBin.insertEnvSet.get(i);
		    if (newBin.equals(ra, ra.lights, ra.fog, ra.modelClip)) {
		        return(newBin);
		    }
		}
	    }
	    currentBin = currentBin.next;
	}

	// Now check the to-be added lightbins
	currentBin = addBin;
	while (currentBin != null) {

	    // this following test is always true for non-backgroundGeo bins
            if (currentBin.geometryBackground ==
                ra.geometryAtom.source.geometryBackground) {

	        // Check the "to-be-added" list of environmentSets for a match
	        for (i = 0; i < currentBin.insertEnvSet.size(); i++) {
		    newBin = (EnvironmentSet)currentBin.insertEnvSet.get(i);
		    if (newBin.equals(ra, ra.lights, ra.fog, ra.modelClip)) {
		        return(newBin);
		    }
		}
	    }
	    currentBin = currentBin.next;
	}


	// Need a new one
	currentEnvSet = getEnvironmentSet(ra, ra.lights, ra.fog, ra.modelClip);
	currentBin = lightBin;

	// Find a lightbin that envSet fits into
	while (currentBin != null) {

	    // the first test is always true for non-backgroundGeo bins
            if (currentBin.geometryBackground ==
		ra.geometryAtom.source.geometryBackground &&
	        currentBin.willEnvironmentSetFit(currentEnvSet)) {
		break;
	    }
	    currentBin = currentBin.next;
	}

	// Now check the to-be added lightbins
	if (currentBin == null) {
	    currentBin = addBin;
	    while (currentBin != null) {

	        // the first test is always true for non-backgroundGeo bins
                if (currentBin.geometryBackground ==
		    ra.geometryAtom.source.geometryBackground &&
	            currentBin.willEnvironmentSetFit(currentEnvSet)) {

		    break;
		}
		currentBin = currentBin.next;
	    }
	}

	if (currentBin == null) {
	    // Need a new lightbin
	    currentBin = getLightBin(maxLights,
				     ra.geometryAtom.source.geometryBackground, false);
	    if (addBin != null) {
		currentBin.next = addBin;
		addBin.prev = currentBin;
	    }
	    if (ra.geometryAtom.source.orderedPath != null) {
		if (!oc.onUpdateList) {
		    objUpdateList.add(oc);
		    oc.onUpdateList = true;
		}
		oc.addLightBins = currentBin;
	    } else {
		if (ra.geometryAtom.source.geometryBackground == null)
		    addOpaqueBin = currentBin;
		else
		    bgAddOpaqueBin = currentBin;
	    }
	}

	currentBin.addEnvironmentSet(currentEnvSet, this);
	return (currentEnvSet);
    }

    void removeLightBin(LightBin lbin) {
	if (lbin.prev == null) { // At the head of the list

            if (lbin.orderedCollection != null)
                removeOrderedHeadLightBin(lbin);

            if (lbin.geometryBackground == null) {
                if (opaqueBin == lbin) {
                    opaqueBin = lbin.next;
                }
            } else {
                if (bgOpaqueBin == lbin) {
                    bgOpaqueBin = lbin.next;
                }
            }
	    if (lbin.next != null) {
	        lbin.next.prev = null;
	    }
	} else { // In the middle or at the end.
	    lbin.prev.next = lbin.next;
	    if (lbin.next != null) {
		lbin.next.prev = lbin.prev;
	    }
	}
	Canvas3D canvases[] = view.getCanvases();
	for (int i = 0; i < canvases.length; i++) {
	    // Mark the environmentSet cached by all the canvases as null
	    // to force to reEvaluate when it comes back from the freelist
	    // During LightBin::render(), we only check for the pointers not
	    // being the same, so we need to take care of the env set
	    // gotten from the freelist from one frame to another
	    canvases[i].lightBin = null;
	}
	lbin.prev = null;
	lbin.next = null;
    }

    void addDisplayListResourceFreeList(RenderMolecule rm) {
	displayListResourceFreeList.add(rm.displayListIdObj);
    }

    /**
     * This renders the background scene graph.
     */
    void renderBackground(Canvas3D cv) {
        LightBin currentBin;
        boolean savedDepthBufferWriteEnable;

        cv.setDepthBufferWriteEnableOverride(true);
        savedDepthBufferWriteEnable = cv.depthBufferWriteEnable;
        cv.setDepthBufferWriteEnable(false);
        // render background opaque
        currentBin = bgOpaqueBin;
        while (currentBin != null) {
            if (currentBin.geometryBackground == geometryBackground)
                currentBin.render(cv);
            currentBin = currentBin.next;
        }

        // render background ordered
        if (bgOrderedBins.size() > 0) {
            renderOrderedBins(cv, bgOrderedBins, true);
        }

	TransparentRenderingInfo tinfo = bgTransparentInfo;
	while (tinfo != null) {
	    tinfo.render(cv);
	    tinfo = tinfo.next;
	}
        cv.setDepthBufferWriteEnableOverride(false);
        cv.setDepthBufferWriteEnable(savedDepthBufferWriteEnable);
    }

    /**
     * This renders the opaque objects
     */
    void renderOpaque(Canvas3D cv) {
	LightBin currentBin = opaqueBin;
	//System.err.println("========> renderOpaque");
	while (currentBin != null) {
	    //System.err.println("====> rendering Opaque Bin ");
	    currentBin.render(cv);
	    currentBin = currentBin.next;
	}

    }

    /**
     * This renders the transparent objects
     */
    void renderTransparent(Canvas3D cv) {
        boolean savedDepthBufferWriteEnable = true;

        //System.err.println("====> renderTransparent");
	TransparentRenderingInfo tinfo = transparentInfo;
	if (tinfo != null) {
	    //System.err.println("====> rendering transparent Bin");

	    if (cv.view.depthBufferFreezeTransparent) {
		cv.setDepthBufferWriteEnableOverride(true);
		savedDepthBufferWriteEnable = cv.depthBufferWriteEnable;
		cv.setDepthBufferWriteEnable(false);
	    }

	    if (transpSortMode == View.TRANSPARENCY_SORT_NONE) {
		while (tinfo != null) {
		    tinfo.render(cv);
		    tinfo = tinfo.next;
		}
	    }
	    else if (transpSortMode == View.TRANSPARENCY_SORT_GEOMETRY) {
		while (tinfo != null ) {
		    tinfo.sortRender(cv);
		    tinfo = tinfo.next;
		}
	    }
	    if (cv.view.depthBufferFreezeTransparent) {
		cv.setDepthBufferWriteEnableOverride(false);
		cv.setDepthBufferWriteEnable(savedDepthBufferWriteEnable);
	    }
	}
    }

    /**
     * This renders the ordered objects
     */
    void renderOrdered(Canvas3D cv) {
	//	System.err.println("******renderOrdered, orderedBins.size() = "+orderedBins.size()+" RenderBin = "+this);
        if (orderedBins.size() > 0)
            renderOrderedBins(cv, orderedBins, false);
    }

    void renderOrderedBins(Canvas3D cv, ArrayList bins, boolean doInfinite) {
        int sz = bins.size();

        for (int i=0; i <sz; i++) {
            renderOrderedBin(cv,
			     (OrderedBin) bins.get(i),
			     doInfinite);
        }
    }

    void renderOrderedBin(Canvas3D cv, OrderedBin orderedBin,
			  boolean doInfinite) {
        int i, index;
        LightBin currentBin;
        OrderedCollection oc;
        boolean depthBufferEnable = true;
	OrderedGroupRetained og = orderedBin.source;
	boolean isDecal = (og instanceof DecalGroupRetained) && cv.systemStencilAvailable;
	int size = orderedBin.orderedCollections.size();

	// System.err.println("RB : orderedBin.orderedCollections.size() " + size);
        for (i=0; i<size; i++) {
	    if((og != null) && (og.childIndexOrder != null)) {
		index = og.childIndexOrder[i];
	    }
	    else {
		index = i;
	    }
	    oc = (OrderedCollection)orderedBin.orderedCollections.get(index);
            if (isDecal) {
                if (index==0) { // first child
                    cv.setDepthBufferEnableOverride(true);
                    depthBufferEnable = cv.decal1stChildSetup(cv.ctx);
                } else if (index==1) { // second child
		    // decalNthChildSetup will disable depth test
                    cv.decalNthChildSetup(cv.ctx);
                }
            }
            if (oc != null) {
                currentBin = oc.lightBin;
                while (currentBin != null) {
                    if (!doInfinite ||
                        currentBin.geometryBackground == geometryBackground) {
                        currentBin.render(cv);
		    }
                    currentBin = currentBin.next;
                }
                renderOrderedBins(cv, oc.childOrderedBins, doInfinite);
            }
        }
        if (isDecal) { // reset
            cv.decalReset(cv.ctx, depthBufferEnable);
            cv.setDepthBufferEnableOverride(false);
        }
    }


    /**
     * Sets the new background color.
     */
    void setBackground(BackgroundRetained back) {

        boolean cvDirty = false;
        BackgroundRetained oldGeomBack = geometryBackground;
        geometryBackground = null;

        if (back != null) {
            background.initColor(back.color);
            background.initImageScaleMode(back.imageScaleMode);
            background.geometryBranch = back.geometryBranch;
            if (background.geometryBranch != null) {
                geometryBackground = back;
            }
            // Release resources associated with old BG and initialize new BG
            // if the old and new BG images are different or if the
            // reloadBgTexture flag is set.
            if (background.image != back.image || reloadBgTexture) {
                if (background.image != null) {
                    assert background.texture != null;
                    addTextureResourceFreeList(background.texture);
                    removeNodeComponent(background.image);
                }
                if (back.image != null) {
                    // May need to optimize later
                    background.initImage((ImageComponent2D)back.image.source);
                    addNodeComponent(back.image);
                } else {
                    background.initImage(null);
                }
            }
            if (oldGeomBack == null) {
                cvDirty = true;
            }
        } else {
            background.initColor(black);
            background.geometryBranch = null;
            if (background.image != null) {
                assert background.texture != null;
                addTextureResourceFreeList(background.texture);
                removeNodeComponent(background.image);
            }
            background.initImage(null);
            if (oldGeomBack != null) {
                cvDirty = true;
            }
	}

	// Need to reEvaluate View cache since doInfinite
	// flag is changed in Renderer.updateViewCache()
	Canvas3D canvases[] = view.getCanvases();
	for (int i=0; i< canvases.length; i++) {
	    Canvas3D canvas = canvases[i];
            synchronized (canvas.dirtyMaskLock) {
                if(cvDirty) {
                    canvas.cvDirtyMask[0] |= Canvas3D.BACKGROUND_DIRTY;
                    canvas.cvDirtyMask[1] |= Canvas3D.BACKGROUND_DIRTY;
                }
                canvas.cvDirtyMask[0] |= Canvas3D.BACKGROUND_IMAGE_DIRTY;
                canvas.cvDirtyMask[1] |= Canvas3D.BACKGROUND_IMAGE_DIRTY;
            }
	}
    }


    void reEvaluateFog(ArrayList fogs, boolean updateDirty,
		       boolean altAppDirty) {
	EnvironmentSet e;
	FogRetained newfog;
	int i, j, n;
	AppearanceRetained app;
	Object[] retVal;

	int sz = renderAtoms.size();
	for (i = 0; i < sz; i++) {
		RenderAtom ra = renderAtoms.get(i);
	    if (!ra.inRenderBin())
		continue;

	    newfog = universe.renderingEnvironmentStructure.getInfluencingFog(ra, view);
	    // If the fog of the render atom is the same
	    // as the old fog, then move on to the
	    // next renderAtom
	    if (altAppDirty&&ra.geometryAtom.source.appearanceOverrideEnable) {
		retVal = universe.renderingEnvironmentStructure.getInfluencingAppearance(ra, view);
		if (retVal[0] == Boolean.TRUE) {
		    app = (AppearanceRetained)retVal[1];
		}
		else {
		    app = ra.geometryAtom.source.appearance;
		}

		if (app == ra.app) {
		    if (ra.envSet.fog == newfog)
			continue;
		    else {
			getNewEnvironment(ra, ra.lights, newfog, ra.modelClip, ra.app);
		    }
		}
		else {
		    if (ra.geometryAtom.source.otherAppearance != app) {
			if (ra.geometryAtom.source.otherAppearance != null)
			    ra.geometryAtom.source.otherAppearance.sgApp.removeAMirrorUser(ra.geometryAtom.source);
			if (app != ra.geometryAtom.source.appearance) {
			    if (app != null) {
				app.sgApp.addAMirrorUser(ra.geometryAtom.source);
			    }
			    ra.geometryAtom.source.otherAppearance = app;
			}
			else {
			    ra.geometryAtom.source.otherAppearance = null;
			}
		    }

		    if (ra.envSet.fog == newfog) {
			ra.app = app;
			e = ra.envSet;
			ra.renderMolecule.removeRenderAtom(ra);
			reInsertAttributeBin(e, ra);
		    }
		    else {
			getNewEnvironment(ra, ra.lights, newfog, ra.modelClip,
					  app);
		    }
		}
	    }
	    else {
		if (ra.envSet.fog == newfog)
		    continue;
		getNewEnvironment(ra, ra.lights, newfog, ra.modelClip, ra.app);
	    };
	}

	// Only done for new fogs added to the system
	if (updateDirty)
	    updateCanvasForDirtyFog(fogs);
    }


    void updateCanvasForDirtyFog(ArrayList fogs) {
	int i, j;
	EnvironmentSet e;
	UnorderList list;
	EnvironmentSet envsets[];
	int envsize;
	int sz = fogs.size();

	for (i = 0; i < sz; i++) {
	    FogRetained fog = (FogRetained)fogs.get(i);
	    list = fog.environmentSets;
	    synchronized (list) {
		envsize = list.size();
		envsets = (EnvironmentSet []) list.toArray(false);
		for (j = 0; j < envsize; j++) {
		    e = envsets[j];
		    e.canvasDirty |= Canvas3D.FOG_DIRTY;
		    if (!e.onUpdateList) {
			objUpdateList.add(e);
			e.onUpdateList = true;
		    }
		}
	    }
	}
    }

    void reEvaluateModelClip(ArrayList modelClips,
			     boolean updateDirty,
			     boolean altAppDirty) {
        EnvironmentSet e;
	ModelClipRetained newModelClip;
        int i, j, n;
	AppearanceRetained app;
	Object[] retVal;
	int sz =  renderAtoms.size();
        for (i = 0; i < sz; i++) {
		RenderAtom ra = renderAtoms.get(i);
	    if (!ra.inRenderBin())
		continue;

            newModelClip =
		universe.renderingEnvironmentStructure.getInfluencingModelClip(ra, view);

            // If the model clip of the render atom is the same
            // as the old model clip, then move on to the
            // next renderAtom
	    if (altAppDirty&&ra.geometryAtom.source.appearanceOverrideEnable) {
		retVal = universe.renderingEnvironmentStructure.getInfluencingAppearance(ra, view);
		if (retVal[0] == Boolean.TRUE) {
		    app = (AppearanceRetained)retVal[1];
		}
		else {
		    app = ra.geometryAtom.source.appearance;
		}

		if (app == ra.app) {
		    if (ra.envSet.modelClip == newModelClip)
			continue;
		    else {
			getNewEnvironment(ra, ra.lights, ra.fog,
					  ra.envSet.modelClip, ra.app);
		    }
		}
		else {
		    if (ra.geometryAtom.source.otherAppearance != app) {
			if (ra.geometryAtom.source.otherAppearance != null)
			    ra.geometryAtom.source.otherAppearance.sgApp.removeAMirrorUser(ra.geometryAtom.source);
			if (app != ra.geometryAtom.source.appearance) {
			    if (app != null) {
				app.sgApp.addAMirrorUser(ra.geometryAtom.source);
			    }
			    ra.geometryAtom.source.otherAppearance = app;
			}
			else {
			    ra.geometryAtom.source.otherAppearance = null;
			}
		    }
		    if (ra.envSet.modelClip == newModelClip) {
			ra.app = app;
			e = ra.envSet;
			ra.renderMolecule.removeRenderAtom(ra);
			reInsertAttributeBin(e, ra);
		    }
		    else {

			getNewEnvironment(ra, ra.lights, ra.fog, newModelClip,
					  app);
		    }
		}
	    }
	    else {
		if (ra.envSet.modelClip == newModelClip)
		    continue;
		getNewEnvironment(ra, ra.lights, ra.fog, newModelClip, ra.app);
	    };
        }

        // Only done for new modelClip added to the system
        if (updateDirty)
            updateCanvasForDirtyModelClip(modelClips);
    }


    void updateCanvasForDirtyModelClip(ArrayList modelClips) {
        int i, j;
        EnvironmentSet e;
        int enableMCMaskCache = 0;
	UnorderList list;
	EnvironmentSet envsets[];
	int sz =  modelClips.size();
	int envsize;

        for (i = 0; i < sz; i++) {
            ModelClipRetained modelClip = (ModelClipRetained)modelClips.get(i);

	    // evaluate the modelClip enable mask
	    enableMCMaskCache = 0;
	    for (j = 0; j < 6; j++) {
		if (modelClip.enables[j])
		    enableMCMaskCache |= 1 << j;
	    }
	    list =  modelClip.environmentSets;
	    synchronized (list) {
		envsize = list.size();
		envsets = (EnvironmentSet []) list.toArray(false);
		for (j = 0; j < envsize; j++) {
		    e = envsets[j];
		    e.canvasDirty |= Canvas3D.MODELCLIP_DIRTY;
		    e.enableMCMaskCache = enableMCMaskCache;
		    if (!e.onUpdateList) {
			objUpdateList.add(e);
			e.onUpdateList = true;
		    }
		}
            }
        }
    }

    void reEvaluateLights(boolean altAppDirty) {
	EnvironmentSet e;
	LightRetained[] lights;
	int i, n;
	AppearanceRetained app;
	Object[] retVal;
	int sz = renderAtoms.size();
	for (i = 0; i < sz; i++) {
		RenderAtom ra = renderAtoms.get(i);
	    if (!ra.inRenderBin())
		continue;

	    lights = universe.renderingEnvironmentStructure.getInfluencingLights(ra, view);
	    // If the lights of the render atom is the same
	    // as the old set of lights, then move on to the
	    // next renderAtom
	    if (altAppDirty&&ra.geometryAtom.source.appearanceOverrideEnable) {
		retVal = universe.renderingEnvironmentStructure.getInfluencingAppearance(ra, view);
		if (retVal[0] == Boolean.TRUE) {
		    app = (AppearanceRetained)retVal[1];
		}
		else {
		    app = ra.geometryAtom.source.appearance;
		}

		if (app == ra.app) {
		    if (ra.lights == lights || ra.envSet.equalLights(lights))
			continue;
		    else {
			getNewEnvironment(ra, lights, ra.fog, ra.modelClip, ra.app);
		    }
		}
		else {
		    if (ra.geometryAtom.source.otherAppearance != app) {
			if (ra.geometryAtom.source.otherAppearance != null)
			    ra.geometryAtom.source.otherAppearance.sgApp.removeAMirrorUser(ra.geometryAtom.source);
			if (app != ra.geometryAtom.source.appearance) {
			    if (app != null) {
				app.sgApp.addAMirrorUser(ra.geometryAtom.source);
			    }
			    ra.geometryAtom.source.otherAppearance = app;
			}
			else {
			    ra.geometryAtom.source.otherAppearance = null;
			}
		    }
		    if (ra.lights == lights || ra.envSet.equalLights(lights)) {
			ra.app = app;
			e = ra.envSet;
			ra.renderMolecule.removeRenderAtom(ra);
			reInsertAttributeBin(e, ra);
		    }
		    else {
			getNewEnvironment(ra, lights, ra.fog, ra.modelClip, app);
		    }
		}
	    }
	    else {
		if (ra.lights == lights || ra.envSet.equalLights(lights))
		    continue;
		getNewEnvironment(ra, lights, ra.fog, ra.modelClip, ra.app);
	    }
	}
	// Only done for new lights added to the system
	if (changedLts.size() > 0)
	    updateCanvasForDirtyLights(changedLts);

    }

    void updateCanvasForDirtyLights(ArrayList mLts) {
	int n, i, j, lmask;
	EnvironmentSet e;
	UnorderList list;
	EnvironmentSet envsets[];
	int sz = mLts.size();
	int envsize;
	int ltsize;

	for (n = 0; n < sz; n++) {
	    LightRetained lt = (LightRetained)mLts.get(n);
	    list = lt.environmentSets;
	    synchronized (list) {
		envsets = (EnvironmentSet []) list.toArray(false);
		envsize = list.size();

		if (lt.nodeType == LightRetained.AMBIENTLIGHT) {
		    for (i = 0; i < envsize; i++) {
			e = envsets[i];
			e.canvasDirty |= Canvas3D.AMBIENTLIGHT_DIRTY;
			if (!e.onUpdateList) {
			    objUpdateList.add(e);
			    e.onUpdateList = true;
			}
		    }
		} else {
		    for (i = 0; i < envsize; i++) {
			e = envsets[i];
			lmask = 0;
			ltsize = e.lights.size();
			for (j = 0; j < ltsize; j++) {
			    LightRetained curLt = (LightRetained)e.lights.get(j);
			    if (lt == curLt) {
				lmask = (1 << e.ltPos[j]);
				if (curLt.lightOn == true) {
				    e.enableMaskCache |= (1 << e.ltPos[j]);
				}
				else {
				    e.enableMaskCache &= (1 << e.ltPos[j]);
				}
				break;
			    }
			}
			e.canvasDirty |= Canvas3D.LIGHTENABLES_DIRTY;
			if (!e.onUpdateList) {
			    objUpdateList.add(e);
			    e.onUpdateList = true;
			}
			if(e.lightBin != null) {
			    e.lightBin.canvasDirty |= Canvas3D.LIGHTBIN_DIRTY;
			    e.lightBin.lightDirtyMaskCache |= lmask;
			    if (!e.lightBin.onUpdateList) {
				e.lightBin.onUpdateList = true;
				objUpdateList.add(e.lightBin);
			    }
			}
		    }
		}
	    }
	}
    }

    void addTextureResourceFreeList(TextureRetained tex) {
        toBeAddedTextureResourceFreeList.add(tex);
    }


    void reEvaluateEnv(ArrayList mLts, ArrayList fogs,
		       ArrayList modelClips,
		       boolean updateDirty,
		       boolean altAppDirty) {

	reEvaluateAllRenderAtoms(altAppDirty);

	// Done only for xform changes, not for bounding leaf change
	if (updateDirty) {
	    // Update canvases for dirty lights and fog
            if (mLts.size()> 0)
                updateCanvasForDirtyLights(mLts);
            if (fogs.size() > 0)
                updateCanvasForDirtyFog(fogs);
            if (modelClips.size() > 0)
                updateCanvasForDirtyModelClip(modelClips);
	}

    }

    void updateInfVworldToVpc() {
        vworldToVpc.getRotation(infVworldToVpc);
    }


    // Lock all geometry before rendering into the any canvas
    // in the case of display list, for each renderer,
    // release after building the display list (which happens
    // for the first canvas rendered)
    void lockGeometry() {
	GeometryRetained geo;
	int i, size;


	// Vertex array is locked for every time renderer is run
	size =  lockGeometryList.size();
	for (i = 0; i < size; i++) {
	    geo = (GeometryRetained) lockGeometryList.get(i);
	    geo.geomLock.getLock();

	}

	// dlist is locked only when they are rebuilt
	size = dlistLockList.size();
	for (i = 0; i < size ; i++) {
	    geo = (GeometryRetained) dlistLockList.get(i);
	    geo.geomLock.getLock();

	}

	// Lock all the by reference image components
	size = nodeComponentList.size();
	for (i = 0; i < size; i++) {
	    ImageComponentRetained nc = (ImageComponentRetained)nodeComponentList.get(i);
	    nc.geomLock.getLock();
	}
    }

    // Release all geometry after rendering to the last canvas
    void releaseGeometry() {
	GeometryRetained geo;
	int i, size;

	size = lockGeometryList.size();
	for (i = 0; i < size; i++) {
	    geo = (GeometryRetained) lockGeometryList.get(i);
	    geo.geomLock.unLock();
	}

	size =  dlistLockList.size();
	for (i = 0; i < size; i++) {
	    geo = (GeometryRetained) dlistLockList.get(i);
	    geo.geomLock.unLock();
	}
	// Clear the display list clear list
	dlistLockList.clear();
	// Lock all the by reference image components
	size =  nodeComponentList.size();
	for (i = 0; i < size; i++) {
	    ImageComponentRetained nc = (ImageComponentRetained)nodeComponentList.get(i);
	    nc.geomLock.unLock();
	}
    }

    void addGeometryToLockList(Object geo) {
	// just add it to the list, if its a shared geometry
	// it may be added more than once, thats OK since we
	// now have nested locks!
	lockGeometryList.add(geo);
    }

    void removeGeometryFromLockList(Object geo) {
	lockGeometryList.remove(geo);

    }


    void addDirtyReferenceGeometry(Object geo) {
	// just add it to the list, if its a shared geometry
	// it may be added more than once, thats OK since we
	// now have nested locks!
	dirtyReferenceGeomList.add(geo);
    }


    void addNodeComponent(Object nc) {
	newNodeComponentList.add(nc);
    }

    void removeNodeComponent (Object nc) {
	removeNodeComponentList.add(nc);
    }

    void addDirtyNodeComponent(Object nc) {
	dirtyNodeComponentList.add(nc);
    }


    void clearDirtyOrientedRAs() {
        int i, nRAs;
        Canvas3D cv;
        RenderAtom ra;
        OrientedShape3DRetained os;
	nRAs = dirtyOrientedRAs.size();

	// clear the dirtyMask
	for(i=0; i<nRAs; i++) {
	    ra = (RenderAtom)dirtyOrientedRAs.get(i);
	    ra.dirtyMask &= ~RenderAtom.IN_DIRTY_ORIENTED_RAs;
	}
	dirtyOrientedRAs.clear();
    }

    // Called from MasterControl when viewCache changes or if there are
    // dirtyOrientedShapes
    void updateOrientedRAs() {
        int i, nRAs;
        Canvas3D cv = null;
        RenderAtom ra;
        OrientedShape3DRetained os;

        // Issue 562 : use cached list of canvases to avoid index OOB exception
        Canvas3D[] canvases = view.getCanvases();
        if (canvases.length > 0) {
            cv = canvases[0];
        }

        if (cv != null) {
          if (view.viewCache.vcDirtyMask != 0) {
            nRAs = orientedRAs.size();

	    // Update ra's localToVworld given orientedTransform
	    // Mark Oriented shape as dirty, since multiple ra could point
	    // to the same OrientShape3D, compute the xform only once
            for(i=0; i<nRAs; i++) {
                ra = (RenderAtom)orientedRAs.get(i);
		os = (OrientedShape3DRetained)ra.geometryAtom.source;
                os.orientedTransformDirty = true;
            }
            // Update ra's localToVworld given orientedTransform
            for(i=0; i<nRAs; i++) {
                ra = (RenderAtom)orientedRAs.get(i);
		os = (OrientedShape3DRetained)ra.geometryAtom.source;
                if (os.orientedTransformDirty) {
                    os.updateOrientedTransform(cv, view.viewIndex);
                    os.orientedTransformDirty = false;
                }
                ra.updateOrientedTransform();
            }
          } else {
            nRAs = cachedDirtyOrientedRAs.size();
	    // Update ra's localToVworld given orientedTransform
	    // Mark Oriented shape as dirty, since multiple ra could point
	    // to the same OrientShape3D, compute the xform only once
            for(i=0; i<nRAs; i++) {
                ra = (RenderAtom)cachedDirtyOrientedRAs.get(i);
                os = (OrientedShape3DRetained)ra.geometryAtom.source;
                os.orientedTransformDirty = true;
            }
            // Update ra's localToVworld given orientedTransform
            for(i=0; i<nRAs; i++) {
                ra = (RenderAtom)cachedDirtyOrientedRAs.get(i);
                os = (OrientedShape3DRetained)ra.geometryAtom.source;
                if (os.orientedTransformDirty) {
                    os.updateOrientedTransform(cv, view.viewIndex);
                    os.orientedTransformDirty = false;

		}
                ra.updateOrientedTransform();
	    }
          }
        }
	cachedDirtyOrientedRAs.clear();

    }


    // This removes a renderAtom and also does the necessary changes
    // for a orientShape3D
    void removeARenderAtom(RenderAtom ra) {
	//	System.err.println("===> remove ga = "+ra.geometryAtom);
	ra.setRenderBin(false);
	ra.renderMolecule.removeRenderAtom(ra);
	if (ra.inDirtyOrientedRAs()) {
	    dirtyOrientedRAs.remove(dirtyOrientedRAs.indexOf(ra));
	    ra.dirtyMask &= ~RenderAtom.IN_DIRTY_ORIENTED_RAs;
	}
	if (ra.inDepthSortList()) {
	    dirtyDepthSortRenderAtom.remove(ra);
	    ra.dirtyMask &= ~RenderAtom.IN_SORTED_POS_DIRTY_TRANSP_LIST;
	    numDirtyTinfo -= ra.rListInfo.length;
	}

	// Assertion check in debug mode
	if (VersionInfo.isDebug && dirtyDepthSortRenderAtom.contains(ra)) {
	    System.err.println("removeARenderAtom: ERROR: RenderAtom not removed from dirty list");
	}
    }

    void removeAllRenderAtoms() {
	int i;
	J3dMessage m;
	RenderMolecule rm;
	int sz =  renderAtoms.size();

	for (i = 0; i < sz; i++) {
		RenderAtom ra = renderAtoms.get(i);
	    rm = ra.renderMolecule;
	    removeARenderAtom(ra);
	    rm.updateRemoveRenderAtoms();
	}
	renderAtoms.clear();

	clearAllUpdateObjectState();

	// Clear the arrayList that are kept from one frame to another
	renderMoleculeList.clear();
	sharedDList.clear();
	lockGeometryList.clear();
	// clear out this orderedBin's entry in the orderedGroup
	for (i = 0; i < orderedBins.size(); i++) {
	    removeOrderedBin((OrderedBin) orderedBins.get(i));
	}
	orderedBins.clear();
	bgOrderedBins.clear();
	nodeComponentList.clear();
	orientedRAs.clear();

	// clean up any messages that are queued up, since they are
	// irrelevant
	//	clearMessages();
	geometryBackground = null;
    }

    void removeOrderedBin(OrderedBin ob) {
	int i, k;
	for (i = 0; i < ob.orderedCollections.size(); i++) {
	    OrderedCollection oc = (OrderedCollection) ob.orderedCollections.get(i);
	    if (oc == null)
		continue;

	    for (k = 0; k < oc.childOrderedBins.size(); k++) {
		removeOrderedBin((OrderedBin)(oc.childOrderedBins.get(k)));
	    }
	}
	if (ob.source != null) {
	    ob.source.setOrderedBin(null, view.viewIndex);
	    ob.source = null;
	}
    }


    void removeGeometryDlist(RenderAtomListInfo ra) {
	removeDlist.add(ra);
    }


    void addGeometryDlist(RenderAtomListInfo ra) {
	addDlist.add(ra);
    }


    void dumpBin(LightBin bin) {
	LightBin obin = bin;
	while (obin != null) {
	    System.err.println("LightBin = "+obin);
	    EnvironmentSet envSet = obin.environmentSetList;
	    while (envSet != null) {
		System.err.println("   EnvSet = "+envSet);
		AttributeBin abin = envSet.attributeBinList;
		while (abin != null) {
		    System.err.println("      ABin = "+abin);
		    ShaderBin sbin = abin.shaderBinList;
		    while (sbin != null) {
			System.err.println("         SBin = "+sbin);
			TextureBin tbin = sbin.textureBinList;
			while (tbin != null) {
			    System.err.println("             Tbin = "+tbin);
			    RenderMolecule rm = tbin.opaqueRMList;
			    System.err.println("===> Begin Dumping OpaqueBin");
			    dumpRM(rm);
			    System.err.println("===> End Dumping OpaqueBin");
			    rm = tbin.transparentRMList;
			    System.err.println("===> Begin Dumping transparentBin");
			    dumpRM(rm);
			    System.err.println("===> End Dumping transparentBin");
			    tbin = tbin.next;
			}
			sbin = sbin.next;
		    }
		    abin = abin.next;
		}
		envSet = envSet.next;
	    }
	    obin = obin.next;
	}

    }

    void dumpRM(RenderMolecule rm) {
	while (rm != null) {
	    System.err.println("            rm = "+rm+" numRAs = "+rm.numRenderAtoms);
	    System.err.println("            primaryRenderAtomList = "+
			       rm.primaryRenderAtomList);
	    RenderAtomListInfo rinfo = rm.primaryRenderAtomList;
	    while (rinfo != null) {
		System.err.println("             rinfo = "+rinfo);
 		System.err.println("             rinfo.ra.localeVwcBounds = "
				   + rinfo.renderAtom.localeVwcBounds);
 		System.err.println("             rinfo.ra.ga.so.vwcBounds = "
				   + rinfo.renderAtom.geometryAtom.source.vwcBounds);
		System.err.println("             geometry = "+rinfo.geometry());

		rinfo = rinfo.next;
	    }
	    System.err.println("            separateDlistRenderAtomList = "+
			       rm.separateDlistRenderAtomList);
	    rinfo = rm.separateDlistRenderAtomList;
	    while (rinfo != null) {
		System.err.println("             rinfo = "+rinfo);
 		System.err.println("             rinfo.ra.localeVwcBounds = "
				   + rinfo.renderAtom.localeVwcBounds);
 		System.err.println("             rinfo.ra.ga.so.vwcBounds = "
				   + rinfo.renderAtom.geometryAtom.source.vwcBounds);
		System.err.println("             geometry = "+rinfo.geometry());
		rinfo = rinfo.next;
	    }
	    System.err.println("            vertexArrayRenderAtomList = "+
			       rm.vertexArrayRenderAtomList);
	    if (rm.next == null) {
		rm= rm.nextMap;
	    }
	    else {
		rm = rm.next;
	    }
	}
    }

    void removeTransparentObject (Object obj) {
	// System.err.println("&&&&&&&&&&&&removeTransparentObject r = "+obj);
	if (obj instanceof TextureBin) {
	    TextureBin tb = (TextureBin) obj;
	    if (tb.environmentSet.lightBin.geometryBackground != null) {
		TransparentRenderingInfo t = tb.parentTInfo;

		// Remove the element from the transparentInfo struct
		if (t == bgTransparentInfo) {
		    bgTransparentInfo = bgTransparentInfo.next;
		    if (bgTransparentInfo != null)
			bgTransparentInfo.prev = null;
		}
		else {
		    t.prev.next = t.next;
		    if (t.next != null)
			t.next.prev = t.prev;
		}
		t.prev = null;
		t.next = null;
		tb.parentTInfo = null;
	    }
	    else {
		int index = allTransparentObjects.indexOf(obj);
		if (index == -1) {
		    // System.err.println("==> DEBUG1: Should never come here!");
		    return;
		}
		allTransparentObjects.remove(index);

		TransparentRenderingInfo t = tb.parentTInfo;

		// Remove the element from the transparentInfo struct
		if (t == transparentInfo) {
		    transparentInfo = transparentInfo.next;
		    if (transparentInfo != null)
			transparentInfo.prev = null;
		}
		else {
		    t.prev.next = t.next;
		    if (t.next != null)
			t.next.prev = t.prev;
		}
		t.prev = null;
		t.next = null;
		tb.parentTInfo = null;
	    }

	}
	else {
	    int index = allTransparentObjects.indexOf(obj);
	    if (index == -1) {
		// System.err.println("==> DEBUG2: Should never come here!");
		return;
	    }

	    allTransparentObjects.remove(index);
	    RenderAtom r = (RenderAtom)obj;
	    for (int i = 0; i < r.parentTInfo.length; i++) {
		// Remove the element from the transparentInfo struct
		TransparentRenderingInfo t = r.parentTInfo[i];
		// This corresponds to null geometry
		if (t == null)
		    continue;

		// Remove the element from the transparentInfo struct
		if (t == transparentInfo) {
		    transparentInfo = transparentInfo.next;
		    if (transparentInfo != null)
			transparentInfo.prev = null;
		}
		else {
		    t.prev.next = t.next;
		    if (t.next != null)
			t.next.prev = t.prev;
		}
		t.prev = null;
		t.next = null;
		nElements--;
		r.parentTInfo[i] = null;
	    }
	}

    }

    void updateTransparentInfo(RenderAtom r) {
	// System.err.println("===> update transparent Info");
	for (int i = 0; i < r.parentTInfo.length; i++) {

	    if (r.parentTInfo[i] == null)
		continue;
	    /*
		    r.parentTInfo[i].lightBin = r.envSet.lightBin;
		    r.parentTInfo[i].envSet = r.envSet;
		    r.parentTInfo[i].aBin = r.renderMolecule.textureBin.attributeBin;
	    */
	    r.parentTInfo[i].rm = r.renderMolecule;
	}
    }

    void addTransparentObject (Object obj) {
	// System.err.println("&&&&&&&&&&&&addTransparentObject r = "+obj);
	if (obj instanceof TextureBin) {
	    TextureBin tb = (TextureBin) obj;
	    // Background geometry
	    if (tb.environmentSet.lightBin.geometryBackground != null) {
		bgTransparentInfo = computeDirtyAcrossTransparentBins(tb, bgTransparentInfo);
	    }
	    else {
		allTransparentObjects.add(obj);
		transparentInfo = computeDirtyAcrossTransparentBins(tb, transparentInfo);
	    }
	}
	else {
	    allTransparentObjects.add(obj);
	    RenderAtom r = (RenderAtom)obj;
	    if (r.parentTInfo == null) {
		r.parentTInfo = new TransparentRenderingInfo[r.rListInfo.length];
	    }
	    computeDirtyAcrossTransparentBins(r);
	    //	    System.err.println("update Centroid 2, ga = "+r.geometryAtom);
	    r.geometryAtom.updateCentroid();
	    if (dirtyDepthSortRenderAtom.add(r)) {
		numDirtyTinfo += r.rListInfo.length;
	    }
	    /*
	    else {
		System.err.println("addTransparentObject: attempt to add RenderAtom already in dirty list");
	    }
	    */
	    r.dirtyMask |= RenderAtom.IN_SORTED_POS_DIRTY_TRANSP_LIST;
	    // System.err.println("transparentInfo  ="+transparentInfo);
	}
    }

    TransparentRenderingInfo getTransparentInfo() {
	   return new TransparentRenderingInfo();
    }

    TransparentRenderingInfo computeDirtyAcrossTransparentBins(TextureBin tb, TransparentRenderingInfo startinfo) {
	TransparentRenderingInfo tinfo = getTransparentInfo();
	/*
	  tinfo.lightBin = tb.environmentSet.lightBin;
	  tinfo.envSet = tb.environmentSet;
	  tinfo.aBin = tb.attributeBin;
	*/
	tinfo.rm = tb.transparentRMList;
	tb.parentTInfo = tinfo;
	if (startinfo == null) {
	    startinfo = tinfo;
	    tinfo.prev = null;
	    tinfo.next = null;

	}
	else {
	    tinfo.next = startinfo;
	    startinfo.prev = tinfo;
	    startinfo = tinfo;
	}
	return startinfo;
    }
    void computeDirtyAcrossTransparentBins(RenderAtom r) {

	for (int i = 0; i < r.parentTInfo.length; i++) {
	    if (r.rListInfo[i].geometry() == null) {
		r.parentTInfo[i] = null;
		continue;
	    }
	    nElements++;
	    TransparentRenderingInfo tinfo = getTransparentInfo();
	    /*
	      tinfo.lightBin = r.envSet.lightBin;
	      tinfo.envSet = r.envSet;
	      tinfo.aBin = r.renderMolecule.textureBin.attributeBin;
	    */
	    tinfo.rm = r.renderMolecule;
	    tinfo.rInfo = r.rListInfo[i];
	    r.parentTInfo[i] = tinfo;
	    if (transparentInfo == null) {
		transparentInfo = tinfo;
		tinfo.prev = null;
		tinfo.next = null;
	    }
	    else {
		tinfo.prev = null;
		tinfo.next = transparentInfo;
		transparentInfo.prev = tinfo;
		transparentInfo = tinfo;
	    }

	}

    }

    void processRenderAtomTransparentInfo(RenderAtomListInfo rinfo, ArrayList newList) {
	while (rinfo != null) {
	    // If either the renderAtom has never been in transparent mode
	    // or if it was previously in that mode and now going back
	    // to that mode
	    if (rinfo.renderAtom.parentTInfo == null) {
		rinfo.renderAtom.parentTInfo = new TransparentRenderingInfo[rinfo.renderAtom.rListInfo.length];
		computeDirtyAcrossTransparentBins(rinfo.renderAtom);
		rinfo.renderAtom.geometryAtom.updateCentroid();
		newList.add(rinfo.renderAtom);
	    }
	    else {
		GeometryRetained geo = null;
		int i = 0;
		while (geo == null && i < rinfo.renderAtom.rListInfo.length) {
		    geo = rinfo.renderAtom.rListInfo[i].geometry();
		    i++;
		}
		// If there is atleast one non-null geometry in this renderAtom
		if (geo != null) {
		    if (rinfo.renderAtom.parentTInfo[i-1] == null) {
			computeDirtyAcrossTransparentBins(rinfo.renderAtom);
			rinfo.renderAtom.geometryAtom.updateCentroid();
			newList.add(rinfo.renderAtom);
		    }
		}
	    }
	    rinfo = rinfo.next;

	}
    }

    void convertTransparentRenderingStruct(int oldMode, int newMode) {
	int i, size;
	ArrayList newList = new ArrayList(5);
	RenderAtomListInfo rinfo;
	// Reset the transparentInfo;
	transparentInfo = null;
	if (oldMode == View.TRANSPARENCY_SORT_NONE && newMode == View.TRANSPARENCY_SORT_GEOMETRY) {
	    size = allTransparentObjects.size();

	    for (i = 0; i < size; i++) {
		TextureBin tb = (TextureBin)allTransparentObjects.get(i);
		tb.parentTInfo = null;
		RenderMolecule r = tb.transparentRMList;
		// For each renderMolecule
		while (r != null) {
		    // If this was a dlist molecule, since we will be rendering
		    // as separate dlist per rinfo, destroy the display list
		    if ((r.primaryMoleculeType &RenderMolecule.DLIST_MOLECULE) != 0) {
			//			System.err.println("&&&&&&&&& changing from dlist to dlist_per_rinfo");
			addDisplayListResourceFreeList(r);
			removeDirtyRenderMolecule(r);

			r.vwcBounds.set(null);
			r.displayListId = 0;
			r.displayListIdObj = null;
			// Change the group type for all the rlistInfo in the primaryList
			rinfo = r.primaryRenderAtomList;
			while (rinfo != null) {
			    rinfo.groupType = RenderAtom.SEPARATE_DLIST_PER_RINFO;
			    if (rinfo.renderAtom.dlistIds == null) {
				rinfo.renderAtom.dlistIds = new int[rinfo.renderAtom.rListInfo.length];

				for (int k = 0; k < rinfo.renderAtom.dlistIds.length; k++) {
				    rinfo.renderAtom.dlistIds[k] = -1;
				}
			    }
			    if (rinfo.renderAtom.dlistIds[rinfo.index] == -1) {
				rinfo.renderAtom.dlistIds[rinfo.index] = VirtualUniverse.mc.getDisplayListId().intValue();
				addDlistPerRinfo.add(rinfo);
			    }
			    rinfo = rinfo.next;
			}
			r.primaryMoleculeType = RenderMolecule.SEPARATE_DLIST_PER_RINFO_MOLECULE;
		    }
		    // Get all the renderAtoms in the list
		    processRenderAtomTransparentInfo(r.primaryRenderAtomList, newList);
		    processRenderAtomTransparentInfo(r.vertexArrayRenderAtomList, newList);
		    processRenderAtomTransparentInfo(r.separateDlistRenderAtomList, newList);
		    if (r.next == null) {
			r = r.nextMap;
		    }
		    else {
			r = r.next;
		    }
		}
	    }
	    allTransparentObjects = newList;
	}
	else if (oldMode == View.TRANSPARENCY_SORT_GEOMETRY && newMode == View.TRANSPARENCY_SORT_NONE) {
	    //	    System.err.println("oldMode = TRANSPARENCY_SORT_GEOMETRY, newMode = TRANSPARENCY_SORT_NONE");
	    size = allTransparentObjects.size();
	    for (i = 0; i < size; i++) {
		RenderAtom r= (RenderAtom)allTransparentObjects.get(i);
		r.dirtyMask &= ~RenderAtom.IN_SORTED_POS_DIRTY_TRANSP_LIST;
		for (int j = 0; j < r.parentTInfo.length; j++) {
		    // Corresponds to null geometry
		    if (r.parentTInfo[j] == null)
			continue;

		    r.parentTInfo[j] = null;
		}
		if (r.renderMolecule.textureBin.parentTInfo == null) {
		    transparentInfo = computeDirtyAcrossTransparentBins(r.renderMolecule.textureBin, transparentInfo);
		    newList.add(r.renderMolecule.textureBin);
		}
	    }
	    allTransparentObjects = newList;
	    dirtyDepthSortRenderAtom.clear();
	    numDirtyTinfo = 0;
	}
    }

    TransparentRenderingInfo mergeDepthSort(TransparentRenderingInfo oldList, TransparentRenderingInfo newList) {
	TransparentRenderingInfo input1 = oldList , input2 = newList,  nextN;
	TransparentRenderingInfo lastInput1 = oldList;
	double zval1, zval2;
	//	System.err.println("&&&&&&&&mergeDepthSort");
	/*
	  TransparentRenderingInfo t = oldList;
	  System.err.println("");
	  while (t != null) {
	  System.err.println("==> old t = "+t);
	  t = t.next;
	  }
	  System.err.println("");
	  t = newList;
	  while (t != null) {
	  System.err.println("==> new t = "+t);
	  t = t.next;
	  }
	*/

	while (input1 != null && input2 != null) {
	    lastInput1 = input1;
	    nextN = input2.next;
	    zval1 = input1.zVal;
	    zval2 = input2.zVal;
	    // Put the newList before the current one

//            System.err.print("Code path 1 ");
//            if (transparencySortComparator!=null)
//                if (zval2 > zval1 && (transparencySortComparator.compare(input2, input1)>0))
//                    System.err.println("PASS");
//                else
//                    System.err.println("FAIL");

            if ((transparencySortComparator==null && zval2 > zval1) ||
                (transparencySortComparator!=null && (transparencySortComparator.compare(input2, input1)>0))){
		//		System.err.println("===> path1");
		if (input1.prev == null) {
		    input1.prev = input2;
		    input2.prev = null;
		    input2.next = oldList;
		    oldList = input2;
		}
		else {
		    //		    System.err.println("===> path2");
		    input2.prev = input1.prev;
		    input1.prev.next = input2;
		    input2.next = input1;
		    input1.prev = input2;
		}
		input2 = nextN;
	    }
	    else {
		//		System.err.println("===> path3");
		input1 = input1.next;
	    }
	}
	if (input1 == null && input2 != null) {
	    // add at then end
	    if (lastInput1 == null) {
		oldList = input2;
		input2.prev = null;
	    }
	    else {
		lastInput1.next = input2;
		input2.prev = lastInput1;
	    }
	}
	return oldList;
    }

//    void insertDepthSort(RenderAtom r) {
//	TransparentRenderingInfo tinfo = null;
//	//	System.err.println("&&&&&&&&insertDepthSort");
//	for (int i = 0; i < r.rListInfo.length; i++) {
//	    if (r.parentTInfo[i] == null)
//		continue;
//
//	    if (transparentInfo == null) {
//		transparentInfo = r.parentTInfo[i];
//		transparentInfo.prev = null;
//		transparentInfo.next = null;
//	    }
//	    else {
//		tinfo = transparentInfo;
//		TransparentRenderingInfo prevInfo = transparentInfo;
//                if (transparencySortComparator==null)
//                    while (tinfo != null && r.parentTInfo[i].zVal < tinfo.zVal) {
//                        prevInfo = tinfo;
//                        tinfo = tinfo.next;
//                    }
//                else {
//                    System.err.println("Code Path 2 ");
//                    if (tinfo!=null && (transparencySortComparator.compare(r.parentTInfo[i], tinfo)<0)==r.parentTInfo[i].zVal < tinfo.zVal)
//                        System.err.println("PASS");
//                    else
//                        System.err.println("FAIL");
//                    while (tinfo != null && transparencySortComparator.compare(r.parentTInfo[i], tinfo)<0) {
//                        prevInfo = tinfo;
//                        tinfo = tinfo.next;
//                    }
//                }
//		r.parentTInfo[i].prev = prevInfo;
//		if (prevInfo.next != null) {
//		    prevInfo.next.prev = r.parentTInfo[i];
//		}
//		r.parentTInfo[i].next = prevInfo.next;
//		prevInfo.next = r.parentTInfo[i];
//
//	    }
//
//	}
//    }

    TransparentRenderingInfo collectDirtyTRInfo( TransparentRenderingInfo dirtyList,
						 RenderAtom r) {

	for (int i = 0; i < r.rListInfo.length; i++) {
	    TransparentRenderingInfo t = r.parentTInfo[i];
	    if (t == null)
		continue;
	    if (t == transparentInfo) {
		transparentInfo = transparentInfo.next;
		if (transparentInfo != null)
		    transparentInfo.prev = null;
	    }
	    else {
		if (t == dirtyList) {
		    // This means that the the item has already been
		    // added to the dirtyList and is at the head of
		    // the list; since we ensure no duplicate
		    // renderAtoms, this should never happen. If it
		    // does, don't try to add it again.
		    System.err.println("collectDirtyTRInfo: ERROR: t == dirtyList");
		    continue;
		}

		// assert(t.prev != null);
		t.prev.next = t.next;
		if (t.next != null)
		    t.next.prev = t.prev;
	    }
	    if (dirtyList == null) {
		dirtyList = t;
		t.prev = null;
		t.next = null;
	    } else {
		t.next = dirtyList;
		t.prev = null;
		dirtyList.prev = t;
		dirtyList = t;
	    }
	}

	return dirtyList;
    }


    TransparentRenderingInfo depthSortAll(TransparentRenderingInfo startinfo) {
        transparencySortComparator = com.sun.j3d.utils.scenegraph.transparency.TransparencySortController.getComparator(view);
	TransparentRenderingInfo tinfo, previnfo, nextinfo;
	double curZ;
	//	System.err.println("&&&&&&&&&&&depthSortAll");
	// Do insertion sort
	/*
	  tinfo = startinfo;
	  while (tinfo != null) {
	  System.err.println("Soreted tinfo= "+tinfo+" tinfo.prev = "+tinfo.prev+" tinfo.next = "+tinfo.next);
	  tinfo = tinfo.next;
	  }
	*/
	tinfo = startinfo.next;
	while (tinfo != null) {
	    //	    System.err.println("====> Doing tinfo = "+tinfo);
	    nextinfo = tinfo.next;
	    curZ = tinfo.zVal;
	    previnfo = tinfo.prev;
	    // Find the correct location for tinfo

            if (transparencySortComparator==null) {
                while (previnfo != null && previnfo.zVal < curZ) {
                    previnfo = previnfo.prev;
                }
            } else {
//                    System.err.println("Code Path 3 ");
//                    if (tinfo!=null && (transparencySortComparator.compare(previnfo, tinfo)<0)==previnfo.zVal < curZ)
//                        System.err.println("PASS");
//                    else
//                        System.err.println("FAIL");
                while (previnfo != null && transparencySortComparator.compare(previnfo,tinfo)<0) {
                    previnfo = previnfo.prev;
                }
            }

	    if (tinfo.prev != previnfo) {
		if (previnfo == null) {
		    if (tinfo.next != null) {
			tinfo.next.prev = tinfo.prev;
		    }
		    // tinfo.prev is not null
		    tinfo.prev.next = tinfo.next;
		    tinfo.next = startinfo;
		    startinfo.prev = tinfo;
		    startinfo = tinfo;
		    tinfo.prev = null;
		}
		else {
		    if (tinfo.next != null) {
			tinfo.next.prev = tinfo.prev;
		    }
		    if (tinfo.prev != null) {
			tinfo.prev.next = tinfo.next;
		    }
		    tinfo.next = previnfo.next;
		    if (previnfo.next != null)
			previnfo.next.prev = tinfo;
		    tinfo.prev = previnfo;
		    previnfo.next = tinfo;
		    //		    System.err.println("path2, tinfo.prev = "+tinfo.prev);
		    //		    System.err.println("path2, tinfo.next = "+tinfo.next);
		}

	    }
	    /*
	      TransparentRenderingInfo tmp = startinfo;
	      while (tmp != null) {
	      System.err.println("Soreted tmp= "+tmp+" tmp.prev = "+tmp.prev+" tmp.next = "+tmp.next);
	      tmp = tmp.next;
	      }
	    */

	    tinfo = nextinfo;

	}
	/*
	  tinfo = startinfo;
	  double prevZ = 0.0;
	  while (tinfo != null) {
	  tinfo.render = false;
	  curZ = ((double[])distMap.get(tinfo.rInfo.renderAtom))[tinfo.rInfo.index];
	  nextinfo = tinfo.next;
	  if (nextinfo != null) {
	  double nextZ = ((double[])distMap.get(nextinfo.rInfo.renderAtom))[tinfo.rInfo.index];
	  if (Math.abs(curZ - nextZ) < 1.0e-6 && curZ < 400) {
	  tinfo.render = true;
	  }
	  }

	  if (Math.abs(curZ - prevZ) < 1.0e-6 && curZ < 400) {
	  tinfo.render = true;
	  }

	  prevZ = curZ;
	  tinfo = tinfo.next;

	  }
	  tinfo = startinfo;
	  while (tinfo != null) {
	  System.err.println("z = "+((double[])distMap.get(tinfo.rInfo.renderAtom))[tinfo.rInfo.index]+" ga = "+tinfo.rInfo.renderAtom.geometryAtom);
	  tinfo = tinfo.next;
	  }
	  System.err.println("\n\n");
	  tinfo = startinfo;
	  while (tinfo != null) {
	  if (tinfo.render) {
	  System.err.println("same z = "+((double[])distMap.get(tinfo.rInfo.renderAtom))[tinfo.rInfo.index]+" ga = "+tinfo.rInfo.renderAtom.geometryAtom);
	  GeometryAtom ga = tinfo.rInfo.renderAtom.geometryAtom;
	  System.err.println("ga.geometryArray.length = "+ga.geometryArray.length);
	  for (int k = 0; k < ga.geometryArray.length; k++) {
	  System.err.println("geometry "+k+" = "+ga.geometryArray[k]);
	  if (ga.geometryArray[k] != null) {
	  System.err.println("    vcount = "+((GeometryArrayRetained)ga.geometryArray[k]).getVertexCount());
	  ((GeometryArrayRetained)ga.geometryArray[k]).printCoordinates();
	  }
	  }
	  }
	  tinfo = tinfo.next;
	  }
	*/
	return startinfo;
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
		    if (obj instanceof LightRetained) {
			envDirty |=  REEVALUATE_LIGHTS;
			if (!changedLts.contains(obj))
			    changedLts.add(obj);
		    }
		    else if (obj instanceof FogRetained) {
			envDirty |=  REEVALUATE_FOG;
			if (!changedFogs.contains(obj))
			    changedFogs.add(obj);
		    }
		    else if (obj instanceof AlternateAppearanceRetained) {
			altAppearanceDirty = true;

		    }
		    else if (obj instanceof ModelClipRetained) {
			envDirty |=  REEVALUATE_MCLIP;
			if (!changedModelClips.contains(obj))
			    changedModelClips.add(obj);
		    }
		    else if (obj instanceof BackgroundRetained) {
			reEvaluateBg = true;
		    }

		    else if (obj instanceof ClipRetained) {
			reEvaluateClip = true;

		    } else if (obj instanceof GeometryAtom) {
			visGAIsDirty = true;
			visQuery = true;
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
		    if (obj instanceof GeometryAtom) {
			RenderAtom ra = ((GeometryAtom)obj).getRenderAtom(view);
			if (ra != null && ra.inRenderBin()) {
			    renderAtoms.remove(renderAtoms.indexOf(ra));
			    removeARenderAtom(ra);
			}
		    }
		    else if (obj instanceof LightRetained) {
			envDirty |=  REEVALUATE_LIGHTS;
		    }
		    else if (obj instanceof FogRetained) {
			envDirty |=  REEVALUATE_FOG;
		    }
		    else if (obj instanceof AlternateAppearanceRetained) {
			altAppearanceDirty = true;

		    }
		    else if (obj instanceof ModelClipRetained) {
			envDirty |=  REEVALUATE_MCLIP;

		    }
		    else if (obj instanceof BackgroundRetained) {
			reEvaluateBg = true;
		    }

		    else if (obj instanceof ClipRetained) {
			reEvaluateClip = true;

		    }
		}
	    }
	}

    }

    void insertNodes(J3dMessage m) {
	Object nodes[];
	ArrayList viewScopedNodes = (ArrayList)m.args[3];
	ArrayList scopedNodesViewList = (ArrayList)m.args[4];
	int i, j;
	Object n;
	nodes = (Object[])m.args[0];
	for (j = 0; j < nodes.length; j++) {
    	    if (nodes[j] instanceof LightRetained) {
		envDirty |=  REEVALUATE_LIGHTS;
		if (!changedLts.contains(nodes[j]))
		    changedLts.add(nodes[j]);
	    } else if (nodes[j] instanceof FogRetained) {
		envDirty |=  REEVALUATE_FOG;
		if (!changedFogs.contains(nodes[j]))
		    changedFogs.add(nodes[j]);
	    } else if (nodes[j] instanceof BackgroundRetained) {
		// If a new background is inserted, then
		// re_evaluate to determine if this background
		// should be used
		reEvaluateBg = true;
	    } else if (nodes[j] instanceof ClipRetained) {
		reEvaluateClip = true;
	    } else if (nodes[j] instanceof ModelClipRetained) {
		envDirty |=  REEVALUATE_MCLIP;
		if (!changedModelClips.contains(nodes[j]))
		    changedModelClips.add(nodes[j]);
	    } else if (nodes[j] instanceof GeometryAtom) {
		visGAIsDirty = true;
		visQuery = true;
	    } else if (nodes[j] instanceof AlternateAppearanceRetained) {
		altAppearanceDirty = true;
	    }
	}

    // Handle ViewScoped Nodes
	if (viewScopedNodes != null) {
	    int size = viewScopedNodes.size();
	    int vlsize;
	    for (i = 0; i < size; i++) {
		n = (NodeRetained)viewScopedNodes.get(i);
		ArrayList vl = (ArrayList) scopedNodesViewList.get(i);
		// If the node object is scoped to this view, then ..
		if (vl.contains(view)) {
		    if (n instanceof LightRetained) {
			envDirty |=  REEVALUATE_LIGHTS;
			if (!changedLts.contains(n))
			    changedLts.add(n);
		    } else if (n instanceof FogRetained) {
			envDirty |=  REEVALUATE_FOG;
			if (!changedFogs.contains(n))
			    changedFogs.add(n);
		    } else if (n instanceof BackgroundRetained) {
			// If a new background is inserted, then
			// re_evaluate to determine if this backgrouns
			// should be used
			reEvaluateBg = true;
		    } else if (n instanceof ClipRetained) {
			reEvaluateClip = true;
		    } else if (n instanceof ModelClipRetained) {
			envDirty |=  REEVALUATE_MCLIP;
			if (!changedModelClips.contains(n))
			    changedModelClips.add(n);
		    } else if (n instanceof AlternateAppearanceRetained) {
			altAppearanceDirty = true;
		    }
		}
		// Note: geometryAtom is not part of viewScopedNodes
		// Its a part of orginal nodes even if scoped

	    }
	}
    }


    void removeNodes(J3dMessage m) {
        Object[] nodes;
        ArrayList viewScopedNodes = (ArrayList)m.args[3];
        ArrayList scopedNodesViewList = (ArrayList)m.args[4];
        int i, j;
        nodes = (Object[])m.args[0];
        for (int n = 0; n < nodes.length; n++) {
            if (nodes[n] instanceof GeometryAtom) {
                visGAIsDirty = true;
                visQuery = true;
                RenderAtom ra =
                        ((GeometryAtom)nodes[n]).getRenderAtom(view);
                if (ra != null && ra.inRenderBin()) {
                    renderAtoms.remove(renderAtoms.indexOf(ra));
                    removeARenderAtom(ra);
                }

                // This code segment is to handle the texture resource cleanup
                // for Raster object.
                GeometryAtom geomAtom = (GeometryAtom) nodes[n];
                if(geomAtom.geometryArray != null) {
                    for(int ii=0; ii<geomAtom.geometryArray.length; ii++) {
                        GeometryRetained geomRetained = geomAtom.geometryArray[ii];
                        if ((geomRetained != null) &&
                                (geomRetained instanceof RasterRetained )) {
                            addTextureResourceFreeList(((RasterRetained)geomRetained).texture);
                        }
                    }
                }
            } else if (nodes[n] instanceof AlternateAppearanceRetained) {
                altAppearanceDirty = true;
            } else  if (nodes[n] instanceof BackgroundRetained) {
                reEvaluateBg = true;
            } else  if (nodes[n] instanceof ClipRetained) {
                reEvaluateClip = true;
            } else if (nodes[n] instanceof ModelClipRetained) {
                envDirty |=  REEVALUATE_MCLIP;
            } else if (nodes[n] instanceof FogRetained) {
                envDirty |=  REEVALUATE_FOG;
            }
            if (nodes[n] instanceof LightRetained) {
                envDirty |=  REEVALUATE_LIGHTS;
            }
        }
        // Handle ViewScoped Nodes
        if (viewScopedNodes != null) {
            int size = viewScopedNodes.size();
            int vlsize;
            Object node;
            for (i = 0; i < size; i++) {
                node = (NodeRetained)viewScopedNodes.get(i);
                ArrayList vl = (ArrayList) scopedNodesViewList.get(i);
                // If the node object is scoped to this view, then ..
                if (vl.contains(view)) {
                    if (node instanceof LightRetained) {
                        envDirty |=  REEVALUATE_LIGHTS;
                    } else if (node instanceof FogRetained) {
                        envDirty |=  REEVALUATE_FOG;
                    } else if (node instanceof BackgroundRetained) {
                        // If a new background is inserted, then
                        // re_evaluate to determine if this backgrouns
                        // should be used
                        reEvaluateBg = true;
                    } else if (node instanceof ClipRetained) {
                        reEvaluateClip = true;
                    } else if (node instanceof ModelClipRetained) {
                        envDirty |=  REEVALUATE_MCLIP;

                    } else if (node instanceof AlternateAppearanceRetained) {
                        altAppearanceDirty = true;
                    }
                    // Note: geometryAtom is not part of viewScopedNodes
                    // Its a part of orginal nodes even if scoped
                }

            }
        }
    }

    void cleanup() {
	releaseAllDisplayListID();
	removeAllRenderAtoms();
    }


    void freeAllDisplayListResources(Canvas3D cv, Context ctx) {

        assert ctx != null;

	int i;
	int size = renderMoleculeList.size();
	Renderer rdr = cv.screen.renderer;

	if (size > 0) {
	    RenderMolecule[] rmArr = (RenderMolecule[])
		renderMoleculeList.toArray(false);

	    for (i = 0 ; i < size; i++) {
		rmArr[i].releaseAllPrimaryDisplayListResources(cv, ctx);
	    }
	}

	size =  sharedDList.size();
	if (size > 0) {
	    RenderAtomListInfo arr[] = new RenderAtomListInfo[size];
	    arr = (RenderAtomListInfo []) sharedDList.toArray(arr);

	    GeometryArrayRetained geo;
	    int mask = (cv.useSharedCtx ? rdr.rendererBit : cv.canvasBit);

	    for (i = 0; i < size; i++) {
		geo = (GeometryArrayRetained)arr[i].geometry();
		// Fix for Issue 5: free all native display lists and clear the
		// context creation bits for this canvas, but don't do anything
		// with the geo's user list.
		if (geo.dlistId > 0) {
		    // XXXX: for the shared ctx case, we really should
		    // only free the display lists if this is the last
		    // Canvas in the renderer.  However, since the
		    // display lists will be recreated, it doesn't
		    // really matter.
		    cv.freeDisplayList(ctx, geo.dlistId);
		    geo.resourceCreationMask &= ~mask;
		}
	    }
	}
    }


    // put displayListID back to MC
    void releaseAllDisplayListID() {
	int i;
	int size = renderMoleculeList.size();

	if (size > 0) {
	    RenderMolecule[] rmArr = (RenderMolecule[])
		renderMoleculeList.toArray(false);

	    for (i = 0 ; i < size; i++) {
		rmArr[i].releaseAllPrimaryDisplayListID();
	    }
	}

	size =  sharedDList.size();
	if (size > 0) {
	    RenderAtomListInfo arr[] = new RenderAtomListInfo[size];
	    arr = (RenderAtomListInfo []) sharedDList.toArray(arr);
	    GeometryArrayRetained geo;

	    for (i = 0; i < size; i++) {
		geo = (GeometryArrayRetained)arr[i].geometry();
		if (geo.resourceCreationMask == 0) {
		    geo.freeDlistId();
		}
	    }
	}
    }


    /*
    void handleFrequencyBitChanged(J3dMessage m) {
	NodeComponentRetained nc = (NodeComponentRetained)m.args[0];
	GeometryAtom[] gaArr = (GeometryAtom[])m.args[3];
	int i;
	RenderAtom ra;
	Boolean value = (Boolean)m.args[1];
	int mask = ((Integer)m.args[2]).intValue();

	// Currently, we do not handle the case of
	// going from frequent to infrequent
	if (value == Boolean.FALSE)
	    return;

	ra = null;
	// Get the first ra that is visible
	for (i = 0; i < gaArr.length; i++) {
	    ra = gaArr[i].getRenderAtom(view);
	    if (ra== null || !ra.inRenderBin())
		continue;
	}

	if (ra == null)
	    return;

	int start = i;
	// Check if the removed renderAtom is already in
	// a separate bin - this is to handle the case
	// when it has been changed to frequent, then to
	// infrequent and then to frequent again!
	if ((nc instanceof MaterialRetained && ra.renderMolecule.definingMaterial != ra.renderMolecule.material) ||
	    (nc instanceof AppearanceRetained && ((ra.renderMolecule.soleUser & AppearanceRetained.MATERIAL) == 0))) {
	    for (i = start; i < gaArr.length; i++) {
		ra = gaArr[i].getRenderAtom(view);
		if (ra== null || !ra.inRenderBin())
		    continue;

		TextureBin tb = ra.renderMolecule.textureBin;
		ra.renderMolecule.removeRenderAtom(ra);
		reInsertRenderAtom(tb, ra);
	    }
	}
	else if ((nc instanceof PolygonAttributesRetained && ra.renderMolecule.definingPolygonAttributes != ra.renderMolecule.polygonAttributes) ||
		 (nc instanceof AppearanceRetained && ((ra.renderMolecule.soleUser & AppearanceRetained.POLYGON) == 0))) {
	    // Check if the removed renderAtom is already in
	    // a separate bin - this is to handle the case
	    // when it has been changed to frequent, then to
	    // infrequent and then to frequent again!
	    for (i = start; i < gaArr.length; i++) {
		ra = gaArr[i].getRenderAtom(view);
		if (ra== null || !ra.inRenderBin())
		    continue;

		TextureBin tb = ra.renderMolecule.textureBin;
		ra.renderMolecule.removeRenderAtom(ra);
		reInsertRenderAtom(tb, ra);
	    }
	}
	else if ((nc instanceof PointAttributesRetained && ra.renderMolecule.definingPointAttributes != ra.renderMolecule.pointAttributes) ||
		 (nc instanceof AppearanceRetained && ((ra.renderMolecule.soleUser & AppearanceRetained.POINT) == 0))) {
	    // Check if the removed renderAtom is already in
	    // a separate bin - this is to handle the case
	    // when it has been changed to frequent, then to
	    // infrequent and then to frequent again!
	    for (i = start; i < gaArr.length; i++) {
		ra = gaArr[i].getRenderAtom(view);
		if (ra== null || !ra.inRenderBin())
		    continue;

		TextureBin tb = ra.renderMolecule.textureBin;
		ra.renderMolecule.removeRenderAtom(ra);
		reInsertRenderAtom(tb, ra);
	    }
	}
	else if ((nc instanceof LineAttributesRetained && ra.renderMolecule.definingLineAttributes != ra.renderMolecule.lineAttributes) ||
		 (nc instanceof AppearanceRetained && ((ra.renderMolecule.soleUser & AppearanceRetained.LINE) == 0))) {
	    // Check if the removed renderAtom is already in
	    // a separate bin - this is to handle the case
	    // when it has been changed to frequent, then to
	    // infrequent and then to frequent again!
	    for (i = start; i < gaArr.length; i++) {
		ra = gaArr[i].getRenderAtom(view);
		if (ra== null || !ra.inRenderBin())
		    continue;

		TextureBin tb = ra.renderMolecule.textureBin;
		ra.renderMolecule.removeRenderAtom(ra);
		reInsertRenderAtom(tb, ra);
	    }
	}
	else if((nc instanceof TransparencyAttributesRetained&& ra.renderMolecule.definingTransparency != ra.renderMolecule.transparency) ||
		 (nc instanceof AppearanceRetained && ((ra.renderMolecule.soleUser & AppearanceRetained.TRANSPARENCY) == 0))) {
	    // Check if the removed renderAtom is already in
	    // a separate bin - this is to handle the case
	    // when it has been changed to frequent, then to
	    // infrequent and then to frequent again!
	    for (i = start; i < gaArr.length; i++) {
		ra = gaArr[i].getRenderAtom(view);
		if (ra== null || !ra.inRenderBin())
		    continue;

		TextureBin tb = ra.renderMolecule.textureBin;
		ra.renderMolecule.removeRenderAtom(ra);
		reInsertRenderAtom(tb, ra);
	    }
	}
	else if ((nc instanceof ColoringAttributesRetained&& ra.renderMolecule.definingColoringAttributes != ra.renderMolecule.coloringAttributes) ||
		 (nc instanceof AppearanceRetained && ((ra.renderMolecule.soleUser & AppearanceRetained.COLOR) == 0))) {
	    // Check if the removed renderAtom is already in
	    // a separate bin - this is to handle the case
	    // when it has been changed to frequent, then to
	    // infrequent and then to frequent again!
	    for (i = start; i < gaArr.length; i++) {
		ra = gaArr[i].getRenderAtom(view);
		if (ra== null || !ra.inRenderBin())
		    continue;

		TextureBin tb = ra.renderMolecule.textureBin;
		ra.renderMolecule.removeRenderAtom(ra);
		reInsertRenderAtom(tb, ra);
	    }
	}
	else if ((nc instanceof RenderingAttributesRetained && ra.renderMolecule.textureBin.attributeBin.definingRenderingAttributes != ra.renderMolecule.textureBin.attributeBin.renderingAttrs) ||
		 (nc instanceof AppearanceRetained && ((ra.renderMolecule.textureBin.attributeBin.soleUser & AppearanceRetained.RENDER) == 0))) {
	    for (i = start; i < gaArr.length; i++) {
		ra = gaArr[i].getRenderAtom(view);
		if (ra== null || !ra.inRenderBin())
		    continue;

		EnvironmentSet e= ra.renderMolecule.textureBin.environmentSet;
		ra.renderMolecule.removeRenderAtom(ra);
		reInsertAttributeBin(e, ra);
	    }
	}
	else {

	    // XXXX: handle texture
	}


    }
    */

}
