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
import java.util.*;

class TransparentRenderingInfo extends Object {
    // For DepthSortedTransparency, rm is the rendermolecule
    // that this rInfo is part of
    // For non depth sorted transparency, rm is one of the rendermolecules
    // in the textureBin that is rendered, all renderMolecules under
    // rm.textureBin's will be rendered
    RenderMolecule rm;
    RenderAtomListInfo rInfo;
    TransparentRenderingInfo prev;
    TransparentRenderingInfo next;
    double zVal; // Used in DepthSorted Transparency
    // TODO: Add Dirty info

    /**
     * update state before rendering transparent objects 
     */
    boolean updateState(Canvas3D cv) {

	TextureBin textureBin = rm.textureBin;
	AttributeBin attributeBin = textureBin.attributeBin;

	// Get a collection to check if switch is on

	RenderMolecule rm = textureBin.transparentRMList ;

	// Optimization to skip updating Attributes if
	// all switch are off. Note that switch condition
	// is check again in rm.render().
	while (rm != null) {
	    if (rm.isSwitchOn()) {
		break;
	    }
	    if (rm.next != null) {
		rm = rm.next;
	    } else {
		rm = rm.nextMap;
	    }
	}
	
	if (rm == null) {
	    return false;
	}

	if (cv.environmentSet != attributeBin.environmentSet) {
		
	    boolean visible = (attributeBin.definingRenderingAttributes == null ||
				   attributeBin.definingRenderingAttributes.visible);

	    if ( (attributeBin.environmentSet.renderBin.view.viewCache.visibilityPolicy
		  == View.VISIBILITY_DRAW_VISIBLE && !visible) ||
		 (attributeBin.environmentSet.renderBin.view.viewCache.visibilityPolicy
		  == View.VISIBILITY_DRAW_INVISIBLE && visible)) {
		return false;
	    }
	    attributeBin.environmentSet.lightBin.updateAttributes(cv);
	    attributeBin.environmentSet.updateAttributes(cv);
	    attributeBin.updateAttributes(cv);
	}
	else {
	    if (cv.attributeBin != attributeBin) {
		boolean visible = (attributeBin.definingRenderingAttributes == null ||
				   attributeBin.definingRenderingAttributes.visible);

		if ( (attributeBin.environmentSet.renderBin.view.viewCache.visibilityPolicy
		      == View.VISIBILITY_DRAW_VISIBLE && !visible) ||
		     (attributeBin.environmentSet.renderBin.view.viewCache.visibilityPolicy
		      == View.VISIBILITY_DRAW_INVISIBLE && visible)) {
		    return false;
		}
		attributeBin.updateAttributes(cv);
	    } 
	}
	return true;
    }

    void render(Canvas3D cv) {
	if (updateState(cv)) {
	    rm.textureBin.render(cv, rm.textureBin.transparentRMList);
	}
    }


    void sortRender(Canvas3D cv) {
	if (updateState(cv)) {
	    rm.textureBin.render(cv, this);
	}
    }
}
