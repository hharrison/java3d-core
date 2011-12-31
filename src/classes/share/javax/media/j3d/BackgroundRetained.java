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

import java.util.ArrayList;

import javax.vecmath.Color3f;


/**
 * The Background leaf node defines either a solid background color
 * or a background image that is used to fill the window at the
 * beginning of each new frame.  It also specifies an application
 * region in which this background is active.
 */
class BackgroundRetained extends LeafRetained {

    static final int COLOR_CHANGED		= 0x00001;
    static final int IMAGE_CHANGED		= 0x00002;
    static final int GEOMETRY_CHANGED		= 0x00004;
    static final int BOUNDS_CHANGED		= 0x00008;
    static final int BOUNDINGLEAF_CHANGED	= 0x00010;
    static final int IMAGE_SCALE_CHANGED        = 0x00020;
    // Background color or image.  If non-null, the image overrides the
    // color.
    Color3f		color = new Color3f(0.0f, 0.0f, 0.0f);
    ImageComponent2DRetained	image = null;
    Texture2DRetained           texture = null;

    // the image scale mode if image is used.
    int imageScaleMode = Background.SCALE_NONE;

    /**
     * The Boundary object defining the lights's application region.
     */
    Bounds applicationRegion = null;

    /**
     * The bounding leaf reference
     */
    BoundingLeafRetained boundingLeaf = null;

    /**
     * Background geometry branch group
     */
    BranchGroup geometryBranch = null;

    /**
     * The transformed value of the applicationRegion.
     */
    Bounds transformedRegion = null;

    /**
     * The state structure used for Background Geometry
     */
    SetLiveState setLiveState = null;

    /**
     * The locale of this Background node since we don't have mirror object
     *  when clearLive is called
     * locale is set to null, we still want locale to have a
     * non-null value, since renderingEnv structure may be using the
     * locale
     */
    Locale cachedLocale = null;

    // This is true when this background is referenced in an immediate mode context
    boolean inImmCtx = false;

    // list of light nodes for background geometry
    ArrayList lights = new ArrayList();

    // list of fog nodes for background geometry
    ArrayList fogs = new ArrayList();

    // a list of background geometry atoms
    ArrayList bgGeometryAtomList = new ArrayList();

    // false is background geometry atoms list has changed
    boolean bgGeometryAtomListDirty = true;

    // an array of background geometry atoms
    GeometryAtom[] bgGeometryAtoms = null;

    // Target threads to be notified when light changes
    // Note, the rendering env structure only get notified
    // when there is a bounds related change
    final static int targetThreads = J3dThread.UPDATE_RENDERING_ENVIRONMENT |
                                     J3dThread.UPDATE_RENDER;

    // Is true, if the background is viewScoped
    boolean isViewScoped = false;

    BackgroundRetained () {
        this.nodeType = NodeRetained.BACKGROUND;
	localBounds = new BoundingBox();
	((BoundingBox)localBounds).setLower( 1.0, 1.0, 1.0);
	((BoundingBox)localBounds).setUpper(-1.0,-1.0,-1.0);
    }

    /**
     * Initializes the background color to the specified color.
     * This color is used
     * if the image is null.
     * @param color the new background color
     */
    final void initColor(Color3f color) {
	this.color.set(color);
    }


    /**
     * Sets the background color to the specified color.  This color is used
     * if the image is null.
     * @param color the new background color
     */
    final void setColor(Color3f color) {
	initColor(color);
	if (source.isLive()) {
	    sendMessage(COLOR_CHANGED, new Color3f(color));
	}
    }

    /**
     * Initializes the background color to the specified color.
     * This color is used
     * if the image is null.
     * @param r the red component of the background color
     * @param g the green component of the background color
     * @param b the blue component of the background color
     */
    final void initColor(float r, float g, float b) {
	this.color.x = r;
	this.color.y = g;
	this.color.z = b;
    }



    /**
     * Sets the background color to the specified color.  This color is used
     * if the image is null.
     * @param r the red component of the background color
     * @param g the green component of the background color
     * @param b the blue component of the background color
     */
    final void setColor(float r, float g, float b) {
	setColor(new Color3f(r, g, b));
    }


    /**
     * Retrieves the background color.
     * @param color the vector that will receive the current background color
     */
    final void getColor(Color3f color) {
	color.set(this.color);
    }

    /**
     * Initialize the image scale mode to the specified mode
     * @imageScaleMode the image scale mode to the used
     */
    final void initImageScaleMode(int imageScaleMode){
	this.imageScaleMode = imageScaleMode;
    }

    /**
     * Sets the image scale mode for this Background node.
     * @param imageScaleMode the image scale mode
     */
    final void setImageScaleMode(int imageScaleMode){
	initImageScaleMode(imageScaleMode);
	if(source.isLive()){
	    sendMessage(IMAGE_SCALE_CHANGED, new Integer(imageScaleMode));
	}
    }

    /**
     * gets the image scale mode for this Background node.
     */
    final int getImageScaleMode(){
	return imageScaleMode;
    }

    /**
     * Initializes the background image to the specified image.
     * @param image new ImageCompoent2D object used as the background image
     */
    final void initImage(ImageComponent2D img) {
        int texFormat;

        if (img == null) {
            image = null;
            texture = null;
            return;
        }

        if (img.retained != image ) {
            image = (ImageComponent2DRetained) img.retained;
            image.setEnforceNonPowerOfTwoSupport(true);
            switch(image.getNumberOfComponents()) {
                case 1:
                    texFormat = Texture.INTENSITY;
                    break;
                case 2:
                    texFormat = Texture.LUMINANCE_ALPHA;
                    break;
                case 3:
                    texFormat = Texture.RGB;
                    break;
                case 4:
                    texFormat = Texture.RGBA;
                    break;
                default:
                    assert false;
                    return;
            }

            Texture2D tex2D = new Texture2D(Texture.BASE_LEVEL, texFormat,
                    img.getWidth(), img.getHeight());
            texture = (Texture2DRetained) tex2D.retained;
            // Background is special case of Raster.
            texture.setUseAsRaster(true);
            // Fix to issue 373 : ImageComponent.set(BufferedImage) ignored when used by Background
            image.addUser(texture);
            texture.initImage(0,img);
        }
    }

    /**
     * Sets the background image to the specified image.
     * @param image new ImageCompoent3D object used as the background image
     */
    final void setImage(ImageComponent2D img) {
	if (source.isLive()) {
	    if (texture != null) {
		texture.clearLive(refCount);
	    }
	}
	initImage(img);
        if (source.isLive()) {
            if (texture != null) {
                texture.setLive(inBackgroundGroup, refCount);
            }

            sendMessage(IMAGE_CHANGED,
                    (texture != null ? texture.mirror : null));

        }
    }

    /**
     * Retrieves the background image.
     * @return the current background image
     */
    final ImageComponent2D getImage() {
        return (image == null ? null :
		(ImageComponent2D)image.source);
    }

    /**
     * Initializes the background geometry branch group to the specified branch.
     * @param branch new branch group object used for background geometry
     */
    final void initGeometry(BranchGroup branch) {
        geometryBranch = branch;
    }


    /**
     * Sets the background geometry branch group to the specified branch.
     * @param branch new branch group object used for background geometry
     */
    final void setGeometry(BranchGroup branch) {
        int numMessages = 0;
        int i;

        if (source.isLive()) {
	    J3dMessage m[];
            if (geometryBranch != null)
                numMessages+=2; // REMOVE_NODES, ORDERED_GROUP_REMOVED
            if (branch != null)
                numMessages+=2; // INSERT_NODES, ORDERED_GROUP_INSERTED
            m = new J3dMessage[numMessages];
            for (i=0; i<numMessages; i++) {
                m[i] = new J3dMessage();
            }
            i = 0;
            if (geometryBranch != null) {
                clearGeometryBranch((BranchGroupRetained)geometryBranch.retained);
                m[i].threads = (J3dThread.UPDATE_RENDER |
				J3dThread.UPDATE_RENDERING_ENVIRONMENT);
                m[i].type = J3dMessage.ORDERED_GROUP_REMOVED;
                m[i].universe = universe;
                m[i].args[0] = setLiveState.ogList.toArray();
                m[i].args[1] = setLiveState.ogChildIdList.toArray();
		m[i].args[3] = setLiveState.ogCIOList.toArray();
		m[i].args[4] = setLiveState.ogCIOTableList.toArray();
                i++;

                m[i].threads = setLiveState.notifyThreads;
                m[i].type = J3dMessage.REMOVE_NODES;
                m[i].universe = universe;
                m[i].args[0] = setLiveState.nodeList.toArray();
                i++;

            }
            if (branch != null) {
                setGeometryBranch((BranchGroupRetained)branch.retained);
                m[i].threads = (J3dThread.UPDATE_RENDER |
				J3dThread.UPDATE_RENDERING_ENVIRONMENT);
                m[i].type = J3dMessage.ORDERED_GROUP_INSERTED;
                m[i].universe = universe;
                m[i].args[0] = setLiveState.ogList.toArray();
                m[i].args[1] = setLiveState.ogChildIdList.toArray();
		m[i].args[2] = setLiveState.ogOrderedIdList.toArray();
		m[i].args[3] = setLiveState.ogCIOList.toArray();
		m[i].args[4] = setLiveState.ogCIOTableList.toArray();
                i++;

                m[i].threads = setLiveState.notifyThreads;
                m[i].type = J3dMessage.INSERT_NODES;
                m[i].universe = universe;
                m[i].args[0] = setLiveState.nodeList.toArray();
            }
            VirtualUniverse.mc.processMessage(m);
	    // Free up memory
	    setLiveState.reset(null);
        }
        initGeometry(branch);
    }

    /**
     * Retrieves the background geometry branch group.
     * @return the current background geometry branch group
     */
    final BranchGroup getGeometry() {
        return geometryBranch;
    }

    /**
     * Initializes the Background's application region.
     * @param region a region that contains the Backgound's new application bounds
     */
    final void initApplicationBounds(Bounds region) {
	if (region != null) {
	    applicationRegion = (Bounds) region.clone();
	} else {
	    applicationRegion = null;
	}
    }

    /**
     * Set the Background's application region.
     * @param region a region that contains the Backgound's new application bounds
     */
    final void setApplicationBounds(Bounds region) {
	initApplicationBounds(region);
	// Don't send the message if there is a valid boundingleaf
	if (boundingLeaf == null) {
	    J3dMessage createMessage = new J3dMessage();
	    createMessage.threads = targetThreads |
		J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	    createMessage.type = J3dMessage.BACKGROUND_CHANGED;
	    createMessage.universe = universe;
	    createMessage.args[0] = this;
	    createMessage.args[1]= new Integer(BOUNDS_CHANGED);
	    if (region != null)
		createMessage.args[2] = region.clone();
	    else
		createMessage.args[2] = null;
	    VirtualUniverse.mc.processMessage(createMessage);
	}
    }

    /**
     * Get the Backgound's application region.
     * @return this Backgound's application region information
     */
    final Bounds getApplicationBounds() {
	return (applicationRegion != null ?  (Bounds) applicationRegion.clone() : null);
    }

    /**
     * Initializes the Background's application region
     * to the specified Leaf node.
     */
    void initApplicationBoundingLeaf(BoundingLeaf region) {
	if (region != null) {
	    boundingLeaf = (BoundingLeafRetained)region.retained;
	} else {
	    boundingLeaf = null;
	}
    }

    /**
     * Set the Background's application region to the specified Leaf node.
     */
    void setApplicationBoundingLeaf(BoundingLeaf region) {
	if (boundingLeaf != null)
	    boundingLeaf.mirrorBoundingLeaf.removeUser(this);

	if (region != null) {
	    boundingLeaf = (BoundingLeafRetained)region.retained;
	    boundingLeaf.mirrorBoundingLeaf.addUser(this);
	} else {
	    boundingLeaf = null;
	}
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = targetThreads |
	    J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	createMessage.type = J3dMessage.BACKGROUND_CHANGED;
	createMessage.universe = universe;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(BOUNDINGLEAF_CHANGED);
	if (boundingLeaf != null) {
	    createMessage.args[2] = boundingLeaf.mirrorBoundingLeaf;
	    createMessage.args[3] = null;
	} else {
	    createMessage.args[2] = null;
	    if (applicationRegion != null)
		createMessage.args[3] = applicationRegion.clone();
	    else
		createMessage.args[3] = null;
	}
	VirtualUniverse.mc.processMessage(createMessage);
    }


    /**
     * Get the Background's application region
     */
    BoundingLeaf getApplicationBoundingLeaf() {
	return (boundingLeaf != null ?
		(BoundingLeaf)boundingLeaf.source : null);
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

    void setGeometryBranch(BranchGroupRetained branch) {
        setLiveState.reset(locale);
        setLiveState.inBackgroundGroup = true;

        setLiveState.geometryBackground = this;
        setLiveState.currentTransforms[0] = new Transform3D[2];
        setLiveState.currentTransforms[0][0] = new Transform3D();
        setLiveState.currentTransforms[0][1] = new Transform3D();
	setLiveState.currentTransformsIndex[0] = new int[2];
	setLiveState.currentTransformsIndex[0][0] = 0;
	setLiveState.currentTransformsIndex[0][1] = 0;

	setLiveState.localToVworld = setLiveState.currentTransforms;
	setLiveState.localToVworldIndex = setLiveState.currentTransformsIndex;

        setLiveState.branchGroupPaths = new ArrayList();
        setLiveState.branchGroupPaths.add(new BranchGroupRetained[0]);

        setLiveState.orderedPaths = new ArrayList(1);
        setLiveState.orderedPaths.add(new OrderedPath());

        setLiveState.switchStates = new ArrayList(1);
        setLiveState.switchStates.add(new SwitchState(false));


	branch.setLive(setLiveState);


    }

    void clearGeometryBranch(BranchGroupRetained branch) {
        setLiveState.reset(locale);
        setLiveState.inBackgroundGroup = true;
        setLiveState.geometryBackground = this;
        branch.clearLive(setLiveState);
        branch.setParent(null);
        branch.setLocale(null);

    }

    /**
     * This setLive routine first calls the superclass's method, then
     * it adds itself to the list of lights
     */
    void setLive(SetLiveState s) {
	TransformGroupRetained[] tlist;
	int i;

	super.doSetLive(s);

        if (inImmCtx) {
           throw new IllegalSharingException(
				J3dI18N.getString("BackgroundRetained1"));
        }
        if (inBackgroundGroup) {
            throw new
              IllegalSceneGraphException(J3dI18N.getString("BackgroundRetained5"));
        }

	if (inSharedGroup) {
	    throw new
		IllegalSharingException(J3dI18N.getString("BackgroundRetained6"));
	}


        if (geometryBranch != null) {
            BranchGroupRetained branch =
                (BranchGroupRetained)geometryBranch.retained;
            if (branch.inBackgroundGroup == true)
               throw new IllegalSharingException(
				J3dI18N.getString("BackgroundRetained0"));

            if (branch.parent != null)
               throw new IllegalSharingException(
				J3dI18N.getString("BackgroundRetained3"));

            if (branch.locale != null)
               throw new IllegalSharingException(
				J3dI18N.getString("BackgroundRetained4"));

	    if (setLiveState == null) {
	   	setLiveState = new SetLiveState(universe);
		setLiveState.universe = universe;
	    }
            setGeometryBranch((BranchGroupRetained)geometryBranch.retained);
	    // add background geometry nodes to setLiveState's nodeList
            s.nodeList.addAll(setLiveState.nodeList);
	    s.notifyThreads |= setLiveState.notifyThreads;
	    s.ogList.addAll(setLiveState.ogList);
	    s.ogChildIdList.addAll(setLiveState.ogChildIdList);
	    s.ogOrderedIdList.addAll(setLiveState.ogOrderedIdList);
	    // Free up memory.
            setLiveState.reset(null);
        }

	if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
	    s.viewScopedNodeList.add(this);
	    s.scopedNodesViewList.add(s.viewLists.get(0));
	} else {
	    s.nodeList.add(this);
	}
	// System.err.println("bkg.setlive nodeList " + s.nodeList);

        // process switch leaf
        if (s.switchTargets != null && s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(this, Targets.ENV_TARGETS);
        }
        switchState = (SwitchState)s.switchStates.get(0);

	// Initialize some mirror values
	if (boundingLeaf != null) {
	    transformedRegion =
		(Bounds)boundingLeaf.mirrorBoundingLeaf.transformedRegion;
	}
	else { // Evaluate applicationRegion if not null
	    if (applicationRegion != null) {
		transformedRegion = (Bounds)applicationRegion.clone();
		transformedRegion.transform(
					applicationRegion,
					getLastLocalToVworld());
	    }
	    else {
		transformedRegion = null;
	    }

	}
	cachedLocale = s.locale;

        // add this node to the transform target
        if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(this, Targets.ENV_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
        }

	s.notifyThreads |= J3dThread.UPDATE_RENDERING_ENVIRONMENT|
	    J3dThread.UPDATE_RENDER;

	if (texture != null) {
	    texture.setLive(inBackgroundGroup, refCount);
	}
	super.markAsLive();

    }

    /**
     * This clearLive routine first calls the superclass's method, then
     * it removes itself to the list of lights
     */
    void clearLive(SetLiveState s) {
        super.clearLive(s);
	if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
	    s.viewScopedNodeList.add(this);
	    s.scopedNodesViewList.add(s.viewLists.get(0));
	} else {
	    s.nodeList.add(this);
	}
        if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(this, Targets.ENV_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
        }

        if (s.switchTargets != null && s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(this, Targets.ENV_TARGETS);
        }

        if (geometryBranch != null) {
            BranchGroupRetained branch =
                (BranchGroupRetained)geometryBranch.retained;
            clearGeometryBranch((BranchGroupRetained)geometryBranch.retained);
            // add background geometry nodes to setLiveState's nodeList
            s.nodeList.addAll(setLiveState.nodeList);
            s.ogList.addAll(setLiveState.ogList);
            s.ogChildIdList.addAll(setLiveState.ogChildIdList);
	    s.notifyThreads |= setLiveState.notifyThreads;
	    // Free up memory.
	    setLiveState.reset(null);
            lights.clear();
            fogs.clear();
        }

	if (texture != null) {
	    texture.clearLive(refCount);
	}

	s.notifyThreads |= J3dThread.UPDATE_RENDERING_ENVIRONMENT|
	    J3dThread.UPDATE_RENDER;
    }

    // The update Object function.
    synchronized void updateImmediateMirrorObject(Object[] objs) {
	int component = ((Integer)objs[1]).intValue();
	Transform3D trans;
	// If initialization

	// Bounds message only sent when boundingleaf is null
	if  ((component & BOUNDS_CHANGED) != 0) {
	    if (objs[2] != null) {
		transformedRegion = ((Bounds)((Bounds) objs[2])).copy(transformedRegion);
		transformedRegion.transform(
			(Bounds) objs[2], getCurrentLocalToVworld());
	    }
	    else {
		transformedRegion = null;
	    }
	}
	else if  ((component & BOUNDINGLEAF_CHANGED) != 0) {
	    if (objs[2] != null) {
		transformedRegion = ((BoundingLeafRetained)objs[2]).transformedRegion;
	    }
	    else { // Evaluate applicationRegion if not null
		Bounds appRegion = (Bounds)objs[3];
		if (appRegion != null) {
		    transformedRegion = appRegion.copy(transformedRegion);
		    transformedRegion.transform(
			appRegion, getCurrentLocalToVworld());
		}
		else {
		    transformedRegion = null;
		}

	    }
	}

    }

    /** Note: This routine will only be called
     * to  update the object's
     * transformed region
     */
    void updateBoundingLeaf() {
        if (boundingLeaf != null &&
		boundingLeaf.mirrorBoundingLeaf.switchState.currentSwitchOn) {
            transformedRegion =
                        boundingLeaf.mirrorBoundingLeaf.transformedRegion;
        } else { // Evaluate applicationRegion if not null
            if (applicationRegion != null) {
		transformedRegion = applicationRegion.copy(transformedRegion);
                transformedRegion.transform(
			applicationRegion, getCurrentLocalToVworld());
            } else {
                transformedRegion = null;
            }
        }
    }

    void updateImmediateTransformChange() {
        // If bounding leaf is null, tranform the bounds object
        if (boundingLeaf == null) {
            if (applicationRegion != null) {
		transformedRegion = applicationRegion.copy(transformedRegion);
                transformedRegion.transform(
			applicationRegion, getCurrentLocalToVworld());
            }
        }
    }


    final void sendMessage(int attrMask, Object attr) {
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = targetThreads;
	createMessage.universe = universe;
	createMessage.type = J3dMessage.BACKGROUND_CHANGED;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(attrMask);
	createMessage.args[2] = attr;
	VirtualUniverse.mc.processMessage(createMessage);
    }

    void addBgGeometryAtomList(GeometryAtom geomAtom) {
        bgGeometryAtomList.add(geomAtom);
        bgGeometryAtomListDirty = true;
    }

    void removeBgGeometryAtomList(GeometryAtom geomAtom) {
        bgGeometryAtomList.remove(bgGeometryAtomList.indexOf(geomAtom));
        bgGeometryAtomListDirty = true;
    }

    GeometryAtom[] getBackgroundGeometryAtoms() {
        if (bgGeometryAtomListDirty) {
            int nAtoms = bgGeometryAtomList.size();
            if (nAtoms == 0) {
                bgGeometryAtoms = null;
            } else {
                bgGeometryAtoms = new GeometryAtom[nAtoms];
                for (int i=0; i<bgGeometryAtoms.length; i++) {
                    bgGeometryAtoms[i] = (GeometryAtom)bgGeometryAtomList.get(i) ;
                }
            bgGeometryAtomListDirty = false;
           }
        }
        return(bgGeometryAtoms);
    }

    void mergeTransform(TransformGroupRetained xform) {
	super.mergeTransform(xform);
        if (applicationRegion != null) {
            applicationRegion.transform(xform.transform);
        }
    }

    // notifies the Background object that the image data in a referenced
    // ImageComponent object is changed.
    // Currently we are not making use of this information.

    void notifyImageComponentImageChanged(ImageComponentRetained image,
                                        ImageComponentUpdateInfo value) {
    }
    void getMirrorObjects(ArrayList leafList, HashKey key) {
	leafList.add(this); // No Mirror in this case
    }
}
