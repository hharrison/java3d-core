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

import java.util.ArrayList;
import javax.vecmath.*;

/**
 * A GeometryAtom is the smallest object representing Geometry.
 */

class GeometryAtom extends Object implements BHLeafInterface, NnuId {
    
    /**
     * Array of geometry components of this geometry atom
     */
    // The first index of geometryArr should always be 0, unless geometryArr contains
    // multiple Text3Ds.
    GeometryRetained[] geometryArray = null;
    
    /**
     * Array of transforms used only for Text3d.
     */
    Transform3D[] lastLocalTransformArray = null;


    /** 
     * The locale that this geometry atom is attatched to.  This is only non-null
     * if this instance is directly linked into a locale.
     */
    Locale locale = null;

    /**
     * The mirror Shape3DRetained for this GeometryAtom.
     */
    Shape3DRetained source = null;
  
    /**
     * The BHLeafNode for this GeometryAtom.
     */
    BHLeafNode bhLeafNode = null;  
  
    // true if alpha channel is editable
    boolean alphaEditable;
    
    // true if this ga is visible. Default is true.
    boolean visible = true;
    
    /**
     * This is the original geometry type from which this atom came
     */
    int geoType = -1;

    /**
     * The list of RenderAtoms for this GeometryAtom
     */
    RenderAtom[] renderAtoms = new RenderAtom[0];

    // Id use for quick search.
    int nnuId;

    Point3d[] centroid = null;
    boolean centroidIsDirty = true;
    Object lockObj = new Object();


    GeometryAtom() {
	// Get a not necessary unique Id.
	nnuId = NnuIdManager.getId();
    }

    public int getId() {
	return nnuId;
    }

    public int equal(NnuId obj) {
	int keyId = obj.getId();
	if(nnuId < keyId) {
	    return -1;
	}
	else if(nnuId > keyId) {
	    return 1;
	}
	else { // Found it!
	    return 0;
	}
    }
    
    public BoundingBox computeBoundingHull() {
	/*
	  System.out.println("Bounds is " + source.vwcBounds); 
	  for(int i=0; i<geometryArray.length; i++) {
	  System.out.println( i + " geoBounds " +
	  geometryArray[i].geoBounds);
	  }
	  */
	
	return source.vwcBounds;
    }

    // This method is use by picking and collision queries.
    public boolean isEnable() {
	return ((source.vwcBounds != null) && 
		(source.vwcBounds.isEmpty() == false) &&
		(source.switchState.currentSwitchOn));
    }

    // This method is use by visibility query.
    public boolean isEnable(int vis) {
	if((source.vwcBounds != null) && (source.vwcBounds.isEmpty() == false) &&
	   (source.switchState.currentSwitchOn)) {
	    switch(vis) {
	    case View.VISIBILITY_DRAW_VISIBLE:
		return visible;
	    case View.VISIBILITY_DRAW_INVISIBLE:
		return (!visible);
	    case View.VISIBILITY_DRAW_ALL:
		return true;
	    }
	}
	return false;
    }

    public Locale getLocale2() {
	return locale;
    }

    
    /**
     * Gets a RenderAtom for the given viewIndex.
     * If it doesn't exist, it creates one.
     */
    RenderAtom getRenderAtom(View view) {
	RenderAtom ra;
	int index;
	// If renderAtom is not scoped to this view, don't even
	// bother creating the renderAtom

	synchronized (renderAtoms) {
	    index = view.viewIndex;
	    if (index >= renderAtoms.length) {

		// If creating a new RenderAtom, but this ga is not scoped
		// to this view, then just return ..
		if (source.viewList != null &&
		    !source.viewList.contains(view))
		    return null;
		RenderAtom[] newList = new RenderAtom[index+1];
		for (int i = 0; i < renderAtoms.length; i++) {
		    newList[i] = renderAtoms[i];
		}
		ra = new RenderAtom();
		newList[index] = ra;
		newList[index].geometryAtom = this;
		
		// Allocate space based on number of geometry in the list
		ra.rListInfo = new RenderAtomListInfo[geometryArray.length];
		if (geoType != GeometryRetained.GEO_TYPE_TEXT3D) {
		    for (int j = 0; j < ra.rListInfo.length; j++) {
			ra.rListInfo[j] = new RenderAtomListInfo();
			ra.rListInfo[j].renderAtom = ra;
			ra.rListInfo[j].index = j;
		    }
		}
		else {
		    for (int j = 0; j < ra.rListInfo.length; j++) {
			ra.rListInfo[j] = new RenderAtomListInfo();
			ra.rListInfo[j].renderAtom = ra;
			ra.rListInfo[j].index = j;
			ra.rListInfo[j].localToVworld = VirtualUniverse.mc.getTransform3D(null);
		    }
		}

		// Note this must be the last line in synchronized.
		// Otherwise the lock is changed to newList and
		// another thread can come in modified. This cause
		// NullPointerException in
		// renderAtoms[index].geometryAtom = this;
		// which I encounter.
		renderAtoms = newList;
	    } else {
		if (renderAtoms[index] == null) {
		    // If creating a new RenderAtom, but this ga is not scoped
		    // to this view, then just return ..
		    if (source.viewList != null &&
			!source.viewList.contains(view))
			return null;

		    ra = new RenderAtom();
		    renderAtoms[index] = ra;
		    renderAtoms[index].geometryAtom = this;
		    // Allocate space based on number of geometry in the list
		    ra.rListInfo = new RenderAtomListInfo[geometryArray.length];
		    if (geoType != GeometryRetained.GEO_TYPE_TEXT3D) {
			for (int j = 0; j < ra.rListInfo.length; j++) {
			    ra.rListInfo[j] = new RenderAtomListInfo();
			    ra.rListInfo[j].renderAtom = ra;
			    ra.rListInfo[j].index = j;
			}
		    }
		    else {
			for (int j = 0; j < ra.rListInfo.length; j++) {
			    ra.rListInfo[j] = new RenderAtomListInfo();
			    ra.rListInfo[j].renderAtom = ra;
			    ra.rListInfo[j].index = j;
			    ra.rListInfo[j].localToVworld = VirtualUniverse.mc.getTransform3D(null);
			}
		    }
		}
	    }
	}
	    
	return (renderAtoms[index]);
    }
    // If the renderAtom is transparent, then make sure that the
    // value is up-to-date

    void updateCentroid() {
	// New for 1.3.2
	// If the sortShape3DBounds flag is set, the bounds of the
	// Shape3D node will be used in place of the computed
	// GeometryArray bounds for transparency sorting for those
	// Shape3D nodes whose boundsAutoCompute attribute is set to
	// false.
	if (VirtualUniverse.mc.sortShape3DBounds &&
	    !source.boundsAutoCompute) {

	    synchronized(lockObj) {
		if (centroid == null) {
		    centroid = new Point3d[geometryArray.length];
		    for (int j = 0; j < centroid.length; j++) {
			centroid[j] = new Point3d(source.localBounds.getCenter());
			source.getCurrentLocalToVworld(0).transform(centroid[j]);
		    }
                }
		else {
		    for (int j = 0; j < centroid.length; j++) {
			centroid[j].set(source.localBounds.getCenter());
			source.getCurrentLocalToVworld(0).transform(centroid[j]);
		    }
		}
	    }

	    return;
	}
	// End of new for 1.3.2
	
	synchronized(lockObj) {
	    for (int j = 0; j < geometryArray.length; j++) {
		if (geometryArray[j] == null)
		    continue;
		synchronized(geometryArray[j].centroid) {
		    if (geometryArray[j].recompCentroid) {
			geometryArray[j].computeCentroid();
			geometryArray[j].recompCentroid = false;
		    }
		}
	    }
	    if (centroidIsDirty) {
		if (centroid == null) {
		    centroid = new Point3d[geometryArray.length];
		    for (int j = 0; j < centroid.length; j++) {
			if (geometryArray[j] == null)
			    continue;
			centroid[j] = new Point3d(geometryArray[j].centroid);
			source.getCurrentLocalToVworld(0).transform(centroid[j]);
		    }
		}
		else {
		    for (int j = 0; j < centroid.length; j++) {
			if (geometryArray[j] == null)
			    continue;
			centroid[j].set(geometryArray[j].centroid);
			source.getCurrentLocalToVworld(0).transform(centroid[j]);
		    }
		}
		centroidIsDirty = false;
	    }
	}
    }

}
