/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.utils.scenegraph.transparency;

import java.util.Comparator;
import java.util.WeakHashMap;
import javax.media.j3d.View;

/**
 * This class controls the Transparency Sorting scheme used by Java 3D when
 * rendering transparent objects. By default (and in all previous versions of
 * Java 3D) objects are sorted depending on the distance from the viewer of the
 * centroid of their bounds. By supplying a different Comparator for a view using
 * the static setComparator method the user can provide their own sorting scheme.
 *
 * The Comparator provided will be called with 2 objects of class 
 * TransparencySortGeom.
 *
 * @since Java 3D 1.4
 */
public class TransparencySortController {

    // Issue 478 - use a WeakHashMap to avoid holding a reference to a View unnecessarily.
    private static WeakHashMap<View,Comparator> comparators = new WeakHashMap<View,Comparator>();

    /** 
     * Set the comparator for the specified view.
     *
     * The comparators compare method will be called with 2 objects of type
     * TransparencySortGeom and it's result should indicate which object is
     * closer to the viewer. Object1 < Object2 if it is to be considered closer
     * and rendered after.
     *
     * @param view the view to which the comparator applies
     * @param comparator the comparator to call
     */
    public static void setComparator(View view, Comparator comparator) {
        comparators.put(view, comparator);
    }
    
    /**
     * Returns the comparator for the specified view
     *
     * @return the comparator for the specified view, or null if there
     * is no comparator for the view or the view is unknown.
     */
    public static Comparator getComparator(View view) {
        return comparators.get(view);
    }
}
