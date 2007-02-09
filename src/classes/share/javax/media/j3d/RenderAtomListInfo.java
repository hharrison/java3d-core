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
/**
 * Information per geometry in the renderAtom, there are several
 * of these per RenderAtom, one per geometry in GeometryAtom
 */
class RenderAtomListInfo extends Object {
    // RenderAtom that its a part of

    RenderAtom renderAtom = null; 

    // Specific geometry index in the GeometryAtom geometryArr list that
    // corresponds to this RenderAtomListInfo
    int index;
    
    // Prev and next pointer
    RenderAtomListInfo next = null;
    RenderAtomListInfo prev = null;

    // Which bucket in the renderMolecule that it falls info
    int groupType = 0;

    // Used only for Text3D
    // background geometry rendering
    Transform3D infLocalToVworld = null;
    Transform3D localToVworld = null;


    GeometryRetained geometry() {
	return renderAtom.geometryAtom.geometryArray[index];
    }
}
