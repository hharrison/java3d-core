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

package com.sun.j3d.utils.behaviors.vp;

import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.universe.*;

/**
 * Abstract class for ViewPlatformBehaviors.  A ViewPlatformBehavior must
 * be added to the ViewingPlatform with the
 * ViewingPlatform.addViewPlatformBehavior() method.  The ViewPlatformBehavior
 * will operate on the ViewPlatform transform (the TransformGroup return by
 * ViewingPlatform.getViewPlatformTransform()).
 * @since Java 3D 1.2.1
 */
abstract public class ViewPlatformBehavior extends Behavior {

    /**
     * The ViewingPlatform for this behavior.
     */
    protected ViewingPlatform vp;

    /**
     * The target TransformGroup for this behavior.
     */
    protected TransformGroup targetTG;

    /**
     * The "home" transform for this behavior.  This is a transform used to
     * position and orient the ViewingPlatform to a known point of interest.
     *
     * @since Java 3D 1.3
     */
    protected Transform3D homeTransform = null;

    /**
     * Sets the ViewingPlatform for this behavior.  This method is called by
     * the ViewingPlatform.  If a sub-calls overrides this method, it must
     * call super.setViewingPlatform(vp).<p>
     * 
     * NOTE: Applications should <i>not</i> call this method.    
     *
     * @param vp the target ViewingPlatform for this behavior
     */
    public void setViewingPlatform(ViewingPlatform vp) {
	this.vp = vp;
        
        if (vp!=null)
	    targetTG = vp.getViewPlatformTransform();
        else
            targetTG = null;
    }

    /**
     * Returns the ViewingPlatform for this behavior
     * @return the ViewingPlatform for this behavior
     */
    public ViewingPlatform getViewingPlatform() {
	return vp;
    }

    /**
     * Copies the given Transform3D into the "home" transform, used to
     * position and reorient the ViewingPlatform to a known point of interest.
     * 
     * @param home source transform to be copied
     * @since Java 3D 1.3
     */
    public void setHomeTransform(Transform3D home) {
	if (homeTransform == null)
	    homeTransform = new Transform3D(home);
	else
	    homeTransform.set(home);
    }

    /**
     * Returns the behaviors "home" transform.
     * 
     * @param home transform to be returned
     * @since Java 3D 1.3
     */
    public void getHomeTransform(Transform3D home ) {
        home.set( homeTransform );
    }

    /**
     * Positions and reorients the ViewingPlatform to its "home" transform.
     * @since Java 3D 1.3
     */
    public void goHome() {
	if (targetTG != null && homeTransform != null)
	    targetTG.setTransform(homeTransform);
    }
}
