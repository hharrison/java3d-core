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

package com.sun.j3d.utils.universe ;

import java.lang.reflect.* ;
import java.util.ArrayList ;
import javax.vecmath.Matrix4d ;
import javax.media.j3d.Bounds ;
import javax.media.j3d.Canvas3D ;
import javax.media.j3d.Sensor ;
import javax.media.j3d.Transform3D ;
import com.sun.j3d.utils.behaviors.vp.ViewPlatformBehavior ;

class ConfigViewPlatformBehavior extends ConfigObject {

    // All known configurable properties.
    private Transform3D homeTransform = null ;
    private Bounds schedulingBounds = null ;
    private int schedulingInterval = -1 ;

    /**
     * The corresponding ViewPlatformBehavior instance.
     */
    ViewPlatformBehavior viewPlatformBehavior ;

    /**
     * Processes properties for this object.  Handles commands of the form:<p>
     * (ViewPlatformBehaviorProperty {instanceName} {attrName} {arg} ...)
     * 
     * @param command the command that invoked this method
     */
    protected void setProperty(ConfigCommand cmd) {

	int argc = cmd.argc ;
	Object[] argv = cmd.argv ;

	if (argc < 4) {
	    syntaxError("Wrong number of arguments to " + cmd.commandName) ;
	}

	if (! isName(argv[2])) {
	    syntaxError("The second argument to " + cmd.commandName +
			" must be a property name");
	}

	String attribute = (String)argv[2] ;
	if (attribute.equals("HomeTransform")) {
	    if (! (argv[3] instanceof Matrix4d)) {
		syntaxError("HomeTransform must be a Matrix4d") ;
	    }
	    homeTransform = new Transform3D((Matrix4d)argv[3]) ;
	}
	else if (attribute.equals("SchedulingBounds")) {
	    if (! (argv[3] instanceof Bounds)) {
		syntaxError("SchedulingBounds must be an instance of Bounds") ;
	    }
	    schedulingBounds = (Bounds)argv[3] ;
	}
	else if (attribute.equals("SchedulingInterval")) {
	    if (! (argv[3] instanceof Double)) {
		syntaxError("SchedulingInterval must be a priority (number)") ;
	    }
	    schedulingInterval = ((Double)argv[3]).intValue() ;
	}
	else {
	    // It's not any of the pre-defined attributes.  Add it to the
	    // properties list for the behavior instance itself to evaluate.
	    properties.add(cmd) ;
	}
    }

    /**
     * Instantiate a ViewPlatformBehavior of the given class name.<p>
     * 
     * NOTE: All ConfigView and ConfigSensor objects must be processed before
     * calling this method.
     *
     * @return the configured ViewPlatformBehavior, or null if error
     */
    ViewPlatformBehavior createViewPlatformBehavior() {

	viewPlatformBehavior = (ViewPlatformBehavior)createTargetObject() ;

	// Set known attributes.
	if (homeTransform != null)
	    viewPlatformBehavior.setHomeTransform(homeTransform) ;

	if (schedulingBounds != null)
	    viewPlatformBehavior.setSchedulingBounds(schedulingBounds) ;

	if (schedulingInterval != -1)
	    viewPlatformBehavior.setSchedulingInterval(schedulingInterval) ;

	// Unknown properties in the concrete instance are evaluated later.
	return viewPlatformBehavior ;
    }
}
