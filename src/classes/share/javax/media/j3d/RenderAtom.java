/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;
import javax.vecmath.*;

/**
 * A RenderAtom is a wrapper for a GeometryAtom in a given RenderBin.
 */

class RenderAtom extends Object implements ObjectUpdate {
    /**
     * The geometry atom of this render atom
     */
    GeometryAtom geometryAtom = null;

    /**
     * The RenderMolecule for this RenderAtom
     */
    RenderMolecule renderMolecule = null;


    /** 
     * The lights that influence this RenderAtom
     */
    LightRetained[] lights = null;

    /** 
     * The fog that influences this RenderAtom
     */
    FogRetained fog = null;

    /**
     * The model clip that influences this RenderAtom
     */
    ModelClipRetained modelClip = null;

    /**
     * The appearance that influences this RenderAtom
     */
    AppearanceRetained app = null;

    //
    // Convert all boolean to a bitmask, saves memory since
    // there are many RenderAtoms per view
    //

    /**
     * Indicates whether or not this object is in
     * the render bin.
     */
    static int IN_RENDERBIN               = 0x1;

    // True if the above localeVwcBounds is a local copy rather
    // than a reference to the bounds in shape
    static int HAS_SEPARATE_LOCALE_VWC_BOUNDS     = 0x2;

    // true if one of the geometries not going to a display list, hence
    // need to maintain a local localeVwcBounds in order to avoid
    // conflict with the vwcBounds in shape which is CURRENT
    // while the checking of localeVwcBounds in rendering time
    // should be LAST. In order words, localeVwcBounds contain
    // the last vwcBounds, whereas, shape.vwcBounds contain the
    // current vwcBounds
    //
    static int NEED_SEPARATE_LOCALE_VWC_BOUNDS    = 0x4;

    static int ON_UPDATELIST                      = 0x8;
    static int ON_LOCALE_VWC_BOUNDS_UPDATELIST    = 0x10;
    // true if comes from Oriented Shape3D
    static int IS_ORIENTED                         = 0x20;
    // true if in dirty oriented render atom list
    static int IN_DIRTY_ORIENTED_RAs               = 0x40;

    // true if in dirty depth sort position list
    static int IN_SORTED_POS_DIRTY_TRANSP_LIST     = 0x80;
    
    // A bitmask for all the bit specified above in this renderAtom
    int dirtyMask = 0;
    
    /**
     * Environment set that this renderAtom belongs to, used
     * to compare the new env set with the old one when the
     * scoping/bounds of a light/fog changes
     */
    EnvironmentSet envSet;

    /**
     * Used for non-text3d
     */
    BoundingBox localeVwcBounds = null;


    /**
     * The last time this atom was reported visible
     */
    long lastVisibleTime = -1;
    
    /**
     * Next and Previous references for the list of RenderAtoms
     * groupType is a  mask set to true if this renderAtom is part of the displaylist array
     * of the renderMolecule
     * One per geometry in the geometryArr in the geometryAtom, since
     * each geometry in the geometryAtom can end up in a different
     * atomList(primary, secondary, seperatedlist) of the renderMoceule
     */
    RenderAtomListInfo[] rListInfo;

    /**
     * Used in depthSorted transparency, once per rInfo
     */
    TransparentRenderingInfo[] parentTInfo = null;

    /**
     * Used when depth sorted transparecy is turned on
     * one dlist per rinfo
     */
    int[] dlistIds = null;

    // One per geometry in the geometryArr in the geometryAtom
    static int TEXT3D  = 0x1;
    static int DLIST   = 0x2;
    static int CG      = 0x4;
    static int OTHER   = 0x8;
    static int SEPARATE_DLIST_PER_GEO   = 0x10;
    static int VARRAY = 0x20;
    static int SEPARATE_DLIST_PER_RINFO = 0x40;
    static int PRIMARY = TEXT3D | DLIST | CG | OTHER|SEPARATE_DLIST_PER_RINFO;

    // Rendermolecule to which its currently being added
    RenderMolecule added = null;

    // Rendermolecule from which its currently being removed
    RenderMolecule removed = null;

    // non-null, if part of the add list(for the next frame) in the renderMolecule
    RenderAtom nextAdd = null;
    RenderAtom prevAdd = null;

    // non-null, if part of the remove list(for the next frame) in the renderMolecule
    RenderAtom nextRemove = null;
    RenderAtom prevRemove = null;

    RenderAtom() {
    }

    /** 
     * This sets the inRenderBin flag
     */
    synchronized void setRenderBin(boolean value) {
	if (value == false) {
	    app = null;
	    dirtyMask &= ~IN_RENDERBIN;
	    dirtyMask &= ~ON_LOCALE_VWC_BOUNDS_UPDATELIST;
	    dirtyMask &= ~ON_UPDATELIST;
	}
	else {
	    dirtyMask |= IN_RENDERBIN;
	}
	    
    }

    /**
     * This returns whether or not this atom goes into the opaque
     * light bin
     */
    boolean isOpaque() {
	AppearanceRetained app = geometryAtom.source.appearance;

        if (app == null) {
            return true;
        }

	TransparencyAttributesRetained ta = app.transparencyAttributes;

	if (!VirtualUniverse.mc.isD3D()) {
	    // D3D doesn't support line/point antialiasing
	    switch (geometryAtom.geoType) {
	    case GeometryRetained.GEO_TYPE_POINT_SET:
	    case GeometryRetained.GEO_TYPE_INDEXED_POINT_SET:
		if ((app.pointAttributes != null) &&
		    app.pointAttributes.pointAntialiasing) {
		    return false;
		}
		break;
	    case GeometryRetained.GEO_TYPE_LINE_SET:
	    case GeometryRetained.GEO_TYPE_LINE_STRIP_SET:
	    case GeometryRetained.GEO_TYPE_INDEXED_LINE_SET:
	    case GeometryRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET:
		if ((app.lineAttributes != null) &&
		    app.lineAttributes.lineAntialiasing) {
		    return false;
		}
		break;
	    case GeometryRetained.GEO_TYPE_RASTER:
	    case GeometryRetained.GEO_TYPE_COMPRESSED:
		break;
	    default:
		if (app.polygonAttributes != null) {
		    if ((app.polygonAttributes.polygonMode == 
			 PolygonAttributes.POLYGON_POINT) &&
			(app.pointAttributes != null) &&
			app.pointAttributes.pointAntialiasing) {
			return false;
		    } else if ((app.polygonAttributes.polygonMode == 
				PolygonAttributes.POLYGON_LINE) &&
			       (app.lineAttributes != null) &&
			       app.lineAttributes.lineAntialiasing) {
			return false;
		    }
		}
		break;
	    }

	    return ((ta == null) ||
		    (ta.transparencyMode == 
		     TransparencyAttributes.NONE) ||
		    (ta.transparencyMode == 
		     TransparencyAttributes.SCREEN_DOOR));
	} else {
	    return ((ta == null) ||
		    (ta.transparencyMode ==
		     TransparencyAttributes.NONE));
	}
    }

    boolean inRenderBin() {
	return ((dirtyMask & IN_RENDERBIN) != 0);
    }

    boolean hasSeparateLocaleVwcBounds() {
	return ((dirtyMask & HAS_SEPARATE_LOCALE_VWC_BOUNDS) != 0);
    }

    boolean needSeparateLocaleVwcBounds() {
	return ((dirtyMask & NEED_SEPARATE_LOCALE_VWC_BOUNDS) != 0);
    }

    boolean onUpdateList() {
	return ((dirtyMask & ON_UPDATELIST) != 0);
    }

    boolean onLocaleVwcBoundsUpdateList() {
	return ((dirtyMask & ON_LOCALE_VWC_BOUNDS_UPDATELIST) != 0);
    }	

    boolean isOriented() {
	return ((dirtyMask & IS_ORIENTED) != 0);
    }

    boolean inDepthSortList() {
	return ((dirtyMask & IN_SORTED_POS_DIRTY_TRANSP_LIST) != 0);
    }


    boolean inDirtyOrientedRAs() {
	return ((dirtyMask & IN_DIRTY_ORIENTED_RAs) != 0);
    }
    
    public void updateObject() {
	if (inRenderBin()) {
	    int lastLVWIndex = 
	      renderMolecule.localToVworldIndex[NodeRetained.LAST_LOCAL_TO_VWORLD];

	    for (int i = 0; i < rListInfo.length; i++) {
		if (rListInfo[i].geometry() == null)
		    continue;
		
		if (geometryAtom.source.inBackgroundGroup) {
		    if (rListInfo[i].infLocalToVworld == null)
			rListInfo[i].infLocalToVworld = new Transform3D();

		    // to preserve the character transformation for Text3D atoms
		    renderMolecule.localToVworld[lastLVWIndex].getRotation(
			rListInfo[i].infLocalToVworld);
		    rListInfo[i].infLocalToVworld.mul(
			geometryAtom.lastLocalTransformArray[i]);
		} else {
		    rListInfo[i].localToVworld.mul(
			renderMolecule.localeLocalToVworld[lastLVWIndex],
			geometryAtom.lastLocalTransformArray[i]);
		}
	    }
	}
	dirtyMask &= ~ON_UPDATELIST;
    }

    void updateOrientedTransform() {
	int lastLVWIndex = 
	  renderMolecule.localToVworldIndex[NodeRetained.LAST_LOCAL_TO_VWORLD];
	Transform3D orientedTransform = 
	    ((OrientedShape3DRetained)geometryAtom.source).
	    getOrientedTransform(renderMolecule.renderBin.view.viewIndex);
	for (int i = 0; i < rListInfo.length; i++) {

	    if (geometryAtom.geoType == GeometryRetained.GEO_TYPE_TEXT3D &&
		geometryAtom.lastLocalTransformArray[i] != null) {
		if (geometryAtom.source.inBackgroundGroup) {
		    if (rListInfo[i].infLocalToVworld == null)
			rListInfo[i].infLocalToVworld = new Transform3D();

		    rListInfo[i].infLocalToVworld.mul(
			renderMolecule.infLocalToVworld[lastLVWIndex],
			orientedTransform);
		    rListInfo[i].infLocalToVworld.mul(
			geometryAtom.lastLocalTransformArray[i]);
		} else {
		    rListInfo[i].localToVworld.mul(
			renderMolecule.localeLocalToVworld[lastLVWIndex],
			orientedTransform);
		    rListInfo[i].localToVworld.mul(
			geometryAtom.lastLocalTransformArray[i]);
		}
	    } else {
		if (geometryAtom.source.inBackgroundGroup) {
		    if (rListInfo[i].infLocalToVworld == null)
			rListInfo[i].infLocalToVworld = new Transform3D();

		    rListInfo[i].infLocalToVworld.mul(
			renderMolecule.infLocalToVworld[lastLVWIndex],
			orientedTransform);
		} else {
		    rListInfo[i].localToVworld.mul(
			renderMolecule.localeLocalToVworld[lastLVWIndex],
			orientedTransform);
		}
	    }
	}
    }

    // updateLocaleVwcBounds is called from RenderBin.updateObject()
    // to update the local copy of the localeVwcBounds

    void updateLocaleVwcBounds() {

	// it is possible that inRenderBin could be false because
	// the renderAtom could have been removed from RenderBin 
	// in the same frame, and removeRenderAtoms does happen
	// before updateLocaleVwcBounds
	if (inRenderBin()) {
	    // Check if the locale of this is different from the
	    // locale on which the view is,then compute the translated 
	    // localeVwcBounds
	    if (renderMolecule.renderBin.locale != geometryAtom.source.locale) {
		
		geometryAtom.source.locale.
		    hiRes.difference(renderMolecule.renderBin.locale.hiRes, 
				     renderMolecule.renderBin.localeTranslation);
		localeVwcBounds.translate(geometryAtom.source.vwcBounds, 
					  renderMolecule.renderBin.localeTranslation);
	    } else {
		localeVwcBounds.set(geometryAtom.source.vwcBounds);
	    }
	    dirtyMask &= ~ON_LOCALE_VWC_BOUNDS_UPDATELIST;
	}
    }
}
