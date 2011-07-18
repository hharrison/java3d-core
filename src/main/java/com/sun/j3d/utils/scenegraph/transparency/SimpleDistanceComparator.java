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
import javax.media.j3d.Transform3D;

/**
 * Sample TransparencySortComparator which has the same functionality as
 * the fixed default function in Java 3D.
 *
 * @since Java 3D 1.4
 */
public class SimpleDistanceComparator implements Comparator {
    
    /** Creates a new instance of SimpleDistanceComparator */
    public SimpleDistanceComparator() {
    }

    /**
     * Compares its two arguments for order. Returns a negative integer, zero, 
     * or a positive integer as the first argument is less than (closer to the viewer), 
     * equal to, or greater than (further from the viewer) the second argument.
     *
     * The compare method will be called with 2 objects of type
     * TransparencySortGeom and it's result should indicate which object is
     * closer to the viewer. Object1 < Object2 if it is to be considered closer
     * and rendered after.
     *
     * @param o1 TransparencySortGeom object 1
     * @param o2 TransparencySortGeom object 2
     * 
     */
    public int compare(Object o1, Object o2) {
        TransparencySortGeom t1 = (TransparencySortGeom)o1;
        TransparencySortGeom t2 = (TransparencySortGeom)o2;
        
        double f = t1.getDistanceSquared()-t2.getDistanceSquared();
        if (f<0)
            return -1;
        if (f==0)
            return 0;
        
        return 1;
    }
    
}
