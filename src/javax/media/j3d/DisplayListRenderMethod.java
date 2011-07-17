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

/**
 * The RenderMethod interface is used to create various ways to render
 * different geometries.
 */

class DisplayListRenderMethod implements RenderMethod {

    /**
     * display list buffer size
     */
    final int bufferSize = 128;

    /**
     * display list buffer
     */
    int[] buffer = new int[bufferSize];

    /**
     * The actual rendering code for this RenderMethod
     */
    public boolean render(RenderMolecule rm, Canvas3D cv,
			  RenderAtomListInfo ra,
			  int dirtyBits) {
	
        if (rm.doInfinite || 
	    !VirtualUniverse.mc.viewFrustumCulling ||
	    rm.vwcBounds.intersect(cv.viewFrustum)) {
	    cv.updateState(dirtyBits);
	    cv.callDisplayList(cv.ctx, rm.displayListId,
			       rm.isNonUniformScale);
	    return true;
	}	
	return false;
    }

    public boolean renderSeparateDlists(RenderMolecule rm, 
					Canvas3D cv,
					RenderAtomListInfo r, int dirtyBits) {
	
	if (rm.doInfinite) {
	    cv.updateState(dirtyBits);
	    while (r != null) {
		cv.callDisplayList(cv.ctx, 
				   ((GeometryArrayRetained)r.geometry()).dlistId,
				   rm.isNonUniformScale);
		r = r.next;
	    }
	    
	    return true;
	}
	
	boolean isVisible = false; // True if any of the RAs is visible.	
	while (r != null) {
	    if (cv.ra == r.renderAtom) {
		if (cv.raIsVisible) {
		    cv.updateState(dirtyBits);
		    cv.callDisplayList(cv.ctx,
				       ((GeometryArrayRetained)r.geometry()).dlistId,
				       rm.isNonUniformScale);
		    isVisible = true;
		}
	    }
	    else {
		if (r.renderAtom.localeVwcBounds.intersect(cv.viewFrustum)) {
		    cv.updateState(dirtyBits);
		    cv.raIsVisible = true;
		    cv.callDisplayList(cv.ctx, 
				       ((GeometryArrayRetained)r.geometry()).dlistId,
				       rm.isNonUniformScale);
		    isVisible = true;
		}
		else {
		    cv.raIsVisible = false;
		}
		cv.ra = r.renderAtom;
	    }
	    r = r.next;
	}
	
	return isVisible;
    }
    


    public boolean renderSeparateDlistPerRinfo(RenderMolecule rm, 
					       Canvas3D cv,
					       RenderAtomListInfo r,
					       int dirtyBits) {

	if (rm.doInfinite) {
	    cv.updateState(dirtyBits);
	    while (r != null) {
		cv.callDisplayList(cv.ctx,r.renderAtom.dlistIds[r.index],
				   rm.isNonUniformScale);
		r = r.next;
	    }
	    return true;
	}
	boolean isVisible = false; // True if any of the RAs is visible.	
	while (r != null) {
	    if (cv.ra == r.renderAtom) {
		if (cv.raIsVisible) {
		    cv.updateState(dirtyBits);
		    cv.callDisplayList(cv.ctx, r.renderAtom.dlistIds[r.index],
				       rm.isNonUniformScale);
		    isVisible = true;
		}
	    }
	    else {
		if (r.renderAtom.localeVwcBounds.intersect(cv.viewFrustum)) {
		    cv.updateState(dirtyBits);
		    cv.raIsVisible = true;
		    cv.callDisplayList(cv.ctx, r.renderAtom.dlistIds[r.index],
				       rm.isNonUniformScale);
		    isVisible = true;
		}
		else {
		    cv.raIsVisible = false;
		}
		cv.ra = r.renderAtom;
	    }
	    r = r.next;
	}
	return isVisible;
	
    }


    

    void buildDisplayList(RenderMolecule rm, Canvas3D cv) {
        RenderAtomListInfo ra;
        boolean useAlpha;
        GeometryArrayRetained geo;
	useAlpha = rm.useAlpha;
	Transform3D staticTransform;
	Transform3D staticNormalTransform;

	if ((rm.primaryRenderAtomList != null) &&
                (rm.texCoordSetMapLen <= cv.maxTexCoordSets)) {

	    cv.newDisplayList(cv.ctx, rm.displayListId);

	    ra = rm.primaryRenderAtomList;

	    while (ra != null) {
		geo = (GeometryArrayRetained)ra.geometry();
		if (ra.renderAtom.geometryAtom.source.staticTransform == null) {
		    staticTransform = null;
		    staticNormalTransform = null;
		} else {
		    staticTransform = 
			ra.renderAtom.geometryAtom.source.staticTransform.transform;
		    if ((geo.vertexFormat & GeometryArray.NORMALS) != 0) {
		        staticNormalTransform = 
			    ra.renderAtom.geometryAtom.source.staticTransform.getNormalTransform();
		    } else {
			staticNormalTransform = null;
		    }
		}
		geo.buildGA(cv, ra.renderAtom, false,
			    (useAlpha &&
			     ((geo.vertexFormat & GeometryArray.COLOR) != 0)),
			    rm.alpha,
			    rm.textureBin.attributeBin.ignoreVertexColors,
			    staticTransform,
			    staticNormalTransform);
		ra = ra.next;
	    }
	    cv.endDisplayList(cv.ctx);
	}
    }

    void buildIndividualDisplayList(RenderAtomListInfo ra, Canvas3D cv, 
					Context ctx) {
	GeometryArrayRetained geo;

	geo = (GeometryArrayRetained)ra.geometry();
	if ((geo.texCoordSetMap != null) && 
		(geo.texCoordSetMap.length > cv.maxTexCoordSets)) {
	    return;
        }

	// Note, the dlistId does not change when renderer is building
	cv.newDisplayList(ctx, geo.dlistId);

	// No need to lock when it is indexed geometry since we have
	// our own copy
	// Note individual dlist is only created if alpha is not modifiable
	// so, we don't need any renderMolecule specific information
	geo.buildGA(cv, ra.renderAtom, false,
		    false,
		    1.0f,
		    false, 
		    null, null);
	cv.endDisplayList(ctx);
    }

    void buildDlistPerRinfo(RenderAtomListInfo ra, RenderMolecule rm, Canvas3D cv) {
        boolean useAlpha;
        GeometryArrayRetained geo;
	useAlpha = rm.useAlpha;
	Transform3D staticTransform;
	Transform3D staticNormalTransform;
	int id;
	
	geo = (GeometryArrayRetained)ra.geometry();
	if ((rm.primaryRenderAtomList != null) &&
                (rm.texCoordSetMapLen <= cv.maxTexCoordSets)) {

	    id = ra.renderAtom.dlistIds[ra.index];
	    cv.newDisplayList(cv.ctx, id);
	    geo = (GeometryArrayRetained)ra.geometry();
	    if (ra.renderAtom.geometryAtom.source.staticTransform == null) {
		staticTransform = null;
		staticNormalTransform = null;
	    } else {
		staticTransform = 
		    ra.renderAtom.geometryAtom.source.staticTransform.transform;
		if ((geo.vertexFormat & GeometryArray.NORMALS) != 0) {
		    staticNormalTransform = 
			ra.renderAtom.geometryAtom.source.staticTransform.getNormalTransform();
		} else {
		    staticNormalTransform = null;
		}
	    }

	    geo.buildGA(cv, ra.renderAtom, false,
			(useAlpha &&
			 ((geo.vertexFormat & GeometryArray.COLOR) != 0)),
			rm.alpha,
			rm.textureBin.attributeBin.ignoreVertexColors,
			staticTransform,
			staticNormalTransform);
	    cv.endDisplayList(cv.ctx);
	}

    }
    
}
