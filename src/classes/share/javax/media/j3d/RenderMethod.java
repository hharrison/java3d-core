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
 * The RenderMethod interface is used to create various ways to render
 * different geometries.
 */

interface RenderMethod {

    /**
     * The actual rendering code for this RenderMethod
     */
    abstract boolean render(RenderMolecule rm, Canvas3D cv,
			 RenderAtomListInfo ra, int dirtyBits);
}
