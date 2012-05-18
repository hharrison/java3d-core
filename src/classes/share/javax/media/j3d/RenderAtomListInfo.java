/*
 * Copyright 2000-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 */

package javax.media.j3d;
/**
 * Information per geometry in the renderAtom, there are several
 * of these per RenderAtom, one per geometry in GeometryAtom
 */
class RenderAtomListInfo extends Object {

final RenderAtom renderAtom; // RenderAtom that its a part of

// Specific geometry index in the GeometryAtom geometryArr list that
// corresponds to this RenderAtomListInfo
final int index;

    // Prev and next pointer
    RenderAtomListInfo next = null;
    RenderAtomListInfo prev = null;

    // Which bucket in the renderMolecule that it falls info
    int groupType = 0;

    // Used only for Text3D
    // background geometry rendering
    Transform3D infLocalToVworld = null;
    Transform3D localToVworld = null;

RenderAtomListInfo(RenderAtom ra, int idx) {
	renderAtom = ra;
	index = idx;
}

    GeometryRetained geometry() {
	return renderAtom.geometryAtom.geometryArray[index];
    }
}
