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


/**
 * An OrderCollections contains a LightBin and an ArrayList of
 * of all top level OrderedGroups under this OrderCollection
 */
class OrderedCollection extends Object implements ObjectUpdate{

    LightBin lightBin = null;

// a list of top level orderedBins under this orderedCollection
ArrayList<OrderedBin> childOrderedBins = new ArrayList<OrderedBin>();

    // LightBin used for next frame
    LightBin nextFrameLightBin = null;

    // LightBins to be added for this frame
    LightBin addLightBins = null;

    boolean onUpdateList = false;

    public void updateObject() {
	int i;
	LightBin lb;
	lightBin = nextFrameLightBin;
	if (addLightBins != null) {
	    if (lightBin != null) {
		addLightBins.prev = lightBin;
		lightBin.next = addLightBins;
	    }
	    else {
		lightBin = addLightBins;
		nextFrameLightBin = lightBin;
	    }
	}
	addLightBins = null;
	onUpdateList = false;
    }



}

