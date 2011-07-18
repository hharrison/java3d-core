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

import java.util.ArrayList ;
import javax.media.j3d.Node ;
import javax.media.j3d.View ;
import javax.media.j3d.ViewPlatform ;
import javax.media.j3d.Transform3D ;
import javax.media.j3d.TransformGroup ;
import javax.vecmath.Matrix4d ;
import com.sun.j3d.utils.behaviors.vp.ViewPlatformBehavior ;

class ConfigViewPlatform extends ConfigObject {

    private boolean allowPolicyRead = false ;
    private boolean allowLocalToVworldRead = false ;
    private boolean nominalViewingTransform = false ;
    private Transform3D initialViewingTransform = null ;
    private ArrayList configViews = new ArrayList() ;
    private Viewer[] viewers = null ;

    /**
     * The corresponding ViewingPlatform instance.
     */
    ViewingPlatform viewingPlatform = null ;
     
    /**
     * Indicates the view attach policy specified in the configuration file.
     * If none is set, it remains -1 even though a default may be in effect.
     */
    int viewAttachPolicy = -1 ;

    /**
     * The associated ConfigViewPlatformBehavior, if any.
     */
    ConfigViewPlatformBehavior configBehavior = null ;
    
    /**
     * Overrides initialize() to do nothing.
     */
    protected void initialize(ConfigCommand command) {
    }

    /**
     * Processes attributes for this object.  Handles commands of the form:<p>
     * (ViewPlatformAttribute {instanceName} {attrName} {attrValue})<br>
     * (ViewPlatformProperty  {instanceName} {attrName} {attrValue})
     * 
     * @param command the command that invoked this method
     */
    protected void setProperty(ConfigCommand command) {

	int argc = command.argc ;
	Object[] argv = command.argv ;
	String attribute ;
	Object value ;

	if (argc != 4) {
	    syntaxError("Incorrect number of arguments to " +
			command.commandName) ;
	}

	if (!isName(argv[2])) {
	    syntaxError("The second argument to " + command.commandName +
			" must be a property name");
	}

	attribute = (String)argv[2] ;
	value = argv[3] ;

	if (attribute.equals("NominalViewingTransform")) {
	    if (! (value instanceof Boolean)) {
		syntaxError("NominalViewingTransform must be a boolean") ;
	    }
	    nominalViewingTransform = ((Boolean)value).booleanValue() ;
	}
	else if (attribute.equals("InitialViewingTransform")) {
	    if (! (value instanceof Matrix4d)) {
		syntaxError("InitialViewingTransform must be a Matrix4d") ;
	    }
	    initialViewingTransform = new Transform3D((Matrix4d)value) ;
	}
	else if (attribute.equals("ViewAttachPolicy")) {
	    if (! (value instanceof String)) {
		syntaxError("ViewAttachPolicy must be a string") ;
	    }

	    String svalue = (String)value ;

	    if (svalue.equals("NOMINAL_HEAD"))
		viewAttachPolicy = View.NOMINAL_HEAD ;
	    else if (svalue.equals("NOMINAL_SCREEN"))
		viewAttachPolicy = View.NOMINAL_SCREEN ;
	    else if (svalue.equals("NOMINAL_FEET"))
		viewAttachPolicy = View.NOMINAL_FEET ;
	    else
		syntaxError("Illegal value " +
			    svalue + " for ViewAttachPolicy") ;
	}
	else if (attribute.equals("ViewPlatformBehavior")) {
	    if (! (value instanceof String)) {
		syntaxError("ViewPlatformBehavior must be a name") ;
	    }
	    configBehavior =
		(ConfigViewPlatformBehavior)configContainer.findConfigObject
		("ViewPlatformBehavior", (String)value) ;
	}
	else if (attribute.equals("AllowPolicyRead")) {
            if (!(value instanceof Boolean)) {
                syntaxError("value for AllowPolicyRead " +
                            "must be a boolean") ;
            }
            allowPolicyRead = ((Boolean)value).booleanValue() ;
	}
	else if (attribute.equals("AllowLocalToVworldRead")) {
            if (!(value instanceof Boolean)) {
                syntaxError("value for AllowLocalToVworldRead " +
                            "must be a boolean") ;
            }
            allowLocalToVworldRead = ((Boolean)value).booleanValue() ;
	}
	else {
	    syntaxError("Unknown " + command.commandName +
			" \"" + attribute + "\"") ;
	}
    }

    /**
     * Add a ConfigView to this ConfigViewPlatform.
     */
    void addConfigView(ConfigView cv) {
	configViews.add(cv) ;
    }

    /**
     * Creates a ViewingPlatform from attributes gathered by this object.
     * 
     * @param transformCount the number of TransformGroups to attach to the
     *  ViewingPlatform
     * @return the new ViewingPlatform
     */
    ViewingPlatform createViewingPlatform(int transformCount) {

	// Get the Viewers attached to this ViewingPlatform.
	// All ConfigViews must be processed at this point.
	if (configViews.size() == 0) {
	    viewers = new Viewer[0] ;
	}
	else {
	    viewers = new Viewer[configViews.size()] ;
	    for (int i = 0 ; i < viewers.length ; i++)
		viewers[i] = ((ConfigView)configViews.get(i)).j3dViewer ;
	}

	// Create the viewing platform and get its ViewPlatform instance.
	viewingPlatform = new ViewingPlatform(transformCount) ;
	ViewPlatform vp = viewingPlatform.getViewPlatform() ;

	// Set defined policies.
	if (allowPolicyRead)
	    vp.setCapability(ViewPlatform.ALLOW_POLICY_READ) ;

	if (allowLocalToVworldRead)
	    vp.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ) ;

	if (viewAttachPolicy == -1) {
	    // Apply a default based on the eyepoint policy.
	    boolean nominalHead = true ;
	    for (int i = 0 ; i < viewers.length ; i++) {
		if (viewers[i].getView().getWindowEyepointPolicy() !=
		    View.RELATIVE_TO_FIELD_OF_VIEW) {
		    nominalHead = false ;
		    break ;
		}
	    }
	    if (nominalHead)
		vp.setViewAttachPolicy(View.NOMINAL_HEAD) ;
	    else
		vp.setViewAttachPolicy(View.NOMINAL_SCREEN) ;
	}
	else {
	    vp.setViewAttachPolicy(viewAttachPolicy) ;
	}

	// Assign the viewing platform to all viewers.
	for (int i = 0 ; i < viewers.length ; i++) {
	    viewers[i].setViewingPlatform(viewingPlatform) ;
	}

	// Apply initial viewing transforms if defined.
	if (nominalViewingTransform) {
	    viewingPlatform.setNominalViewingTransform() ;
	}

	if (initialViewingTransform != null) {
	    TransformGroup tg = viewingPlatform.getViewPlatformTransform() ;
	    tg.setTransform(initialViewingTransform) ;
	}

	return viewingPlatform ;
    }

    /**
     * Attach any ViewPlatformBehavior specified for this platform.
     */
    void processBehavior() {
	if (configBehavior != null) {
	    viewingPlatform.setViewPlatformBehavior
		(configBehavior.viewPlatformBehavior) ;
	}
    }
}
