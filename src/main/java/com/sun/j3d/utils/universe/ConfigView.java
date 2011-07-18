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

import java.util.* ;
import javax.media.j3d.* ;
import javax.vecmath.* ;

class ConfigView extends ConfigObject {
    /**
     * The corresponding View and Viewer instances.  These are set when
     * createJ3dView() is called after parsing the configuration file.
     */
    View j3dView = null ;
    Viewer j3dViewer = null ;

    /**
     * Set of ConfigScreen instances added to this view.
     */
    Set screens = new HashSet() ;

    /**
     * Indicates whether or not stereo viewing should be enabled for this
     * ConfigView.  This is set during parsing of the configuration file.
     */
    boolean stereoEnable = false ;
    
    /**
     * Indicates whether or not antialiasing is enabled for this ConfigView.
     * This is set during parsing of the configuration file.
     */
    boolean antialiasingEnable = false;

    /**
     * Reference to the PhysicalBody associated with this ConfigView.  This is
     * set when createJ3dView() is called after parsing the configuration
     * file.
     */
    PhysicalBody physicalBody = null ;

    /**
     * Reference to the PhysicalEnvironment associated with this ConfigView.
     * This is set when createJ3dView() is called after parsing the
     * configuration file.
     */
    PhysicalEnvironment physicalEnvironment = null ;

    // All other configurable attributes.
    private double     fieldOfView             = Math.PI/4.0 ;
    private int        backClipPolicy          = View.PHYSICAL_EYE ;
    private int        frontClipPolicy         = View.PHYSICAL_EYE ;
    private double     backClipDistance        = 10.0 ;
    private double     frontClipDistance       =  0.1 ;
    private int        screenScalePolicy       = View.SCALE_SCREEN_SIZE ;
    private double     screenScale             = 1.0 ;
    private boolean    trackingEnable          = false ;
    private int        viewPolicy              = View.SCREEN_VIEW ;
    private int        windowEyepointPolicy    = -1 ;
    private int        windowMovementPolicy    = -1 ;
    private int        windowResizePolicy      = -1 ;
    private boolean    coeCenteringEnableSet   = false ;
    private boolean    coeCenteringEnable      = false ;
    private Point3d    centerEyeInCoexistence  = null ;

    private ConfigPhysicalBody configBody = null ;
    private ConfigPhysicalEnvironment configEnv = null ;
    private ConfigViewPlatform configViewPlatform = null ;
    
    /**
     * Overrides initialize() to do nothing.
     */
    protected void initialize(ConfigCommand command) {
    }

    /**
     * Processes properties for this object.  Handles commands of the form:<p>
     * (ViewAttribute {instanceName} {attrName} {attrValue})
     * 
     * @param command the command that invoked this method
     */
    protected void setProperty(ConfigCommand command) {

        int argc = command.argc ;
        Object[] argv = command.argv ;
        String attr = null ;
        Object val = null ;
        String sval = null ;
        ConfigScreen cs = null ;

        // Check that arg[1] and arg[2] are strings
        if (argc != 4) {
            syntaxError("Incorrect number of arguments to " +
			command.commandName) ; 
        }

        if (!isName(argv[1])) {
            syntaxError("The first argument to " + command.commandName +
			" must be the instance name") ;
        }

        if (!isName(argv[2])) {
            syntaxError("The second argument to " + command.commandName +
			" must be a property name") ;
        }

        attr = (String) argv[2] ;
        val = argv[3] ;

        if (attr.equals("Screen") || attr.equals("Window")) {
            if (!(val instanceof String)) {
                syntaxError("Value for " + attr + " must be a name") ;
            }
            cs = (ConfigScreen)
                configContainer.findConfigObject("Screen", (String)val) ;

            if (!screens.add(cs)) {
		syntaxError(attr + " \"" + ((String)val) +
			    "\" has already been added to " + instanceName) ;
	    }
        }
        else if (attr.equals("ViewPlatform")) {
            if (!(val instanceof String)) {
                syntaxError("value for ViewPlatform " +
			    " must be an instance name") ;
            }
            configViewPlatform =
                (ConfigViewPlatform)configContainer.findConfigObject
                ("ViewPlatform", (String)val) ;

	    configViewPlatform.addConfigView(this) ;
        }
        else if (attr.equals("PhysicalEnvironment")) {
            if (!(val instanceof String)) {
                syntaxError("value for PhysicalEnvironment " +
                            "must be an instance name") ;
            }
            configEnv =
                (ConfigPhysicalEnvironment)configContainer.findConfigObject
                ("PhysicalEnvironment", (String)val) ;
        }
        else if (attr.equals("PhysicalBody")) {
            if (!(val instanceof String)) {
                syntaxError("value for PhysicalBody " +
                            "must be an instance name") ;
            }
            configBody = (ConfigPhysicalBody)
                configContainer.findConfigObject("PhysicalBody", (String)val) ;
        }
        else if (attr.equals("BackClipPolicy")) {
            if (!(val instanceof String)) {
                syntaxError("value for BackClipPolicy must be a string") ;
            }
            sval = (String) val ;
            if (sval.equals("PHYSICAL_EYE"))
                backClipPolicy = View.PHYSICAL_EYE ;
            else if (sval.equals("PHYSICAL_SCREEN"))
                backClipPolicy = View.PHYSICAL_SCREEN ;
            else if (sval.equals("VIRTUAL_EYE"))
                backClipPolicy = View.VIRTUAL_EYE ;
            else if (sval.equals("VIRTUAL_SCREEN"))
                backClipPolicy = View.VIRTUAL_SCREEN ;
            else
                syntaxError("Invalid value for BackClipPolicy " + sval) ;
        }
        else if (attr.equals("FrontClipPolicy")) {
            if (!(val instanceof String)) {
                syntaxError("value for FrontClipPolicy must be a string") ;
            }
            sval = (String) val ;
            if (sval.equals("PHYSICAL_EYE"))
                frontClipPolicy = View.PHYSICAL_EYE ;
            else if (sval.equals("PHYSICAL_SCREEN"))
                frontClipPolicy = View.PHYSICAL_SCREEN ;
            else if (sval.equals("VIRTUAL_EYE"))
                frontClipPolicy = View.VIRTUAL_EYE ;
            else if (sval.equals("VIRTUAL_SCREEN"))
                frontClipPolicy = View.VIRTUAL_SCREEN ;
            else
                syntaxError("Invalid value for FrontClipPolicy " + sval) ;
        }
        else if (attr.equals("ScreenScalePolicy")) {
            if (!(val instanceof String)) {
                syntaxError("value for ScreenScalePolicy must be a string") ;
            }
            sval = (String) val ;
            if (sval.equals("SCALE_SCREEN_SIZE"))
                screenScalePolicy = View.SCALE_SCREEN_SIZE ;
            else if (sval.equals("SCALE_EXPLICIT"))
                screenScalePolicy = View.SCALE_EXPLICIT ;
            else
                syntaxError("Invalid value for ScreenScalePolicy " + sval) ;
        }
        else if (attr.equals("FieldOfView")) {
            if (!(val instanceof Double)) {
                syntaxError("value for FieldOfView must be a number") ;
            }
            fieldOfView = ((Double)val).doubleValue() ;
        }
        else if (attr.equals("BackClipDistance")) {
            if (!(val instanceof Double)) {
                syntaxError("value for BackClipDistance must be a number") ;
            }
            backClipDistance = ((Double)val).doubleValue() ;
        }
        else if (attr.equals("FrontClipDistance")) {
            if (!(val instanceof Double)) {
                syntaxError("value for FrontClipDistance must be a number") ;
            }
            frontClipDistance = ((Double)val).doubleValue() ;
        }
        else if (attr.equals("ScreenScale")) {
            if (!(val instanceof Double)) {
                syntaxError("value for ScreenScale must be a number") ;
            }
            screenScale = ((Double)val).doubleValue() ;
        }
        else if (attr.equals("TrackingEnable")) {
            if (!(val instanceof Boolean)) {
                syntaxError("value for TrackingEnable must be a boolean") ;
            }
            trackingEnable = ((Boolean)val).booleanValue() ;
        }
        else if (attr.equals("CoexistenceCenteringEnable")) {
            if (!(val instanceof Boolean)) {
                syntaxError("value for CoexistenceCenteringEnable " +
                            "must be a boolean") ;
            }
            coeCenteringEnable = ((Boolean)val).booleanValue() ;
	    coeCenteringEnableSet = true ;
        }
        else if (attr.equals("ViewPolicy")) {
            if (!(val instanceof String)) {
                syntaxError("value for ViewPolicy must be a string") ;
            }
            sval = (String) val ;
            if (sval.equals("SCREEN_VIEW"))
                viewPolicy = View.SCREEN_VIEW ;
            else if (sval.equals("HMD_VIEW"))
                viewPolicy = View.HMD_VIEW ;
            else
                syntaxError("Invalid value for ViewPolicy " + sval) ;
        }
        else if (attr.equals("WindowEyepointPolicy")) {
            if (!(val instanceof String)) {
                syntaxError("value for WindowEyepointPolicy " +
                            "must be a string") ;
            }
            sval = (String) val ;
            if (sval.equals("RELATIVE_TO_SCREEN"))
                windowEyepointPolicy = View.RELATIVE_TO_SCREEN ;
            else if (sval.equals("RELATIVE_TO_COEXISTENCE"))
                windowEyepointPolicy = View.RELATIVE_TO_COEXISTENCE ;
            else if (sval.equals("RELATIVE_TO_WINDOW"))
                windowEyepointPolicy = View.RELATIVE_TO_WINDOW ;
            else if (sval.equals("RELATIVE_TO_FIELD_OF_VIEW"))
                windowEyepointPolicy = View.RELATIVE_TO_FIELD_OF_VIEW ;
            else
                syntaxError("Invalid value for WindowEyepointPolicy " + sval) ;
        }
        else if (attr.equals("WindowMovementPolicy")) {
            if (!(val instanceof String)) {
                syntaxError("value for WindowEyeMovementPolicy " +
                            "must be a string") ;
            }
            sval = (String) val ;
            if (sval.equals("VIRTUAL_WORLD"))
                windowMovementPolicy = View.VIRTUAL_WORLD ;
            else if (sval.equals("PHYSICAL_WORLD"))
                windowMovementPolicy = View.PHYSICAL_WORLD ;
            else
                syntaxError("Invalid value for WindowMovementPolicy " + sval) ;
        }
        else if (attr.equals("WindowResizePolicy")) {
            if (!(val instanceof String)) {
                syntaxError("value for WindowResizePolicy " +
                            "must be a string") ;
            }
            sval = (String) val ;
            if (sval.equals("VIRTUAL_WORLD"))
                windowResizePolicy = View.VIRTUAL_WORLD ;
            else if (sval.equals("PHYSICAL_WORLD"))
                windowResizePolicy = View.PHYSICAL_WORLD ;
            else
                syntaxError("Invalid value for WindowResizePolicy " + sval) ;
        }
        else if (attr.equals("CenterEyeInCoexistence")) {
            if (val instanceof Point3d)
                centerEyeInCoexistence = (Point3d)val ;
            else
                syntaxError("value for CenterEyeInCoexistence " +
			    "must be a Point3d") ;
        }
        else if (attr.equals("StereoEnable")) {
            if (!(val instanceof Boolean)) {
                syntaxError("value for StereoEnable must be a boolean") ;
            }
            stereoEnable = ((Boolean)val).booleanValue() ;
        }
        else if (attr.equals("AntialiasingEnable")) {
            if (!(val instanceof Boolean)) {
                syntaxError("value for AntialiasingEnable must be a boolean") ;
            }
            antialiasingEnable = ((Boolean)val).booleanValue() ;
        }
        else {
	    syntaxError("Unknown " + command.commandName +
			" \"" + attr + "\"") ;
        }
    }

    /**
     * Create a core Java 3D View instance and a utility Viewer instance using
     * the attributes gathered by this object.
     */
    protected Viewer createViewer(boolean setVisible) {
        Point3d leftEyeCoe, rightEyeCoe ;

        j3dView = new View() ;
        j3dView.setViewPolicy(viewPolicy) ;

        if (configBody == null)
            physicalBody = new PhysicalBody() ;
        else
            physicalBody = configBody.j3dPhysicalBody ;

        if (configEnv == null)
            physicalEnvironment = new PhysicalEnvironment() ;
        else
            physicalEnvironment = configEnv.j3dPhysicalEnvironment ;

        j3dView.setPhysicalBody(physicalBody) ;
        j3dView.setPhysicalEnvironment(physicalEnvironment) ;

	boolean standardDefaults = true ;
	if (coeCenteringEnableSet && !coeCenteringEnable) {
	    standardDefaults = false ;
	}
	if (configEnv != null && configEnv.coexistenceToTrackerBase != null) {
	    standardDefaults = false ;
	}
	else {
	    Iterator i = screens.iterator() ;
	    while (i.hasNext()) {
		ConfigScreen s = (ConfigScreen)i.next() ;
		if (s.trackerBaseToImagePlate != null) {
		    standardDefaults = false ;
		    break ;
		}
	    }
	}

	if (standardDefaults) {
	    // Coexistence centering has not been explicitly set false, and
	    // the tracker base to image plate and coexistence to tracker base
	    // transforms are unset, so use the standard Java 3D defaults.
	    if (windowEyepointPolicy == -1)
		windowEyepointPolicy = View.RELATIVE_TO_FIELD_OF_VIEW ;
	    if (windowMovementPolicy == -1)
		windowMovementPolicy = View.PHYSICAL_WORLD ;
	    if (windowResizePolicy == -1)
		windowResizePolicy = View.PHYSICAL_WORLD ;
	    if (!coeCenteringEnableSet)
		coeCenteringEnable = true ;
	}
	else {
	    // Use multi-screen or calibrated coexistence defaults.
	    if (windowEyepointPolicy == -1)
		windowEyepointPolicy = View.RELATIVE_TO_COEXISTENCE ;
	    if (windowMovementPolicy == -1)
		windowMovementPolicy = View.VIRTUAL_WORLD ;
	    if (windowResizePolicy == -1)
		windowResizePolicy = View.VIRTUAL_WORLD ;
	    if (!coeCenteringEnableSet)
		coeCenteringEnable = false ;
	}
	    
        j3dView.setWindowEyepointPolicy(windowEyepointPolicy) ;
        j3dView.setWindowMovementPolicy(windowMovementPolicy) ;
        j3dView.setWindowResizePolicy(windowResizePolicy) ;
	j3dView.setCoexistenceCenteringEnable(coeCenteringEnable) ;

        if (centerEyeInCoexistence == null) {
            centerEyeInCoexistence = new Point3d(0.0, 0.0, 0.4572) ;
        }

        leftEyeCoe  = new Point3d(centerEyeInCoexistence) ;
        rightEyeCoe = new Point3d(centerEyeInCoexistence) ;

        if (stereoEnable) {
            Point3d leftEyeBody =  new Point3d() ;
            Point3d rightEyeBody = new Point3d() ;
            
            physicalBody.getLeftEyePosition(leftEyeBody) ;
            physicalBody.getRightEyePosition(rightEyeBody) ;

            leftEyeCoe.add(leftEyeBody) ;
            rightEyeCoe.add(rightEyeBody) ;
        }
        
        j3dView.setLeftManualEyeInCoexistence(leftEyeCoe) ;
        j3dView.setRightManualEyeInCoexistence(rightEyeCoe) ;
        
        j3dView.setBackClipPolicy(backClipPolicy) ;
        j3dView.setFrontClipPolicy(frontClipPolicy) ;
        j3dView.setBackClipDistance(backClipDistance) ;
        j3dView.setFrontClipDistance(frontClipDistance) ;

        j3dView.setScreenScalePolicy(screenScalePolicy) ;
        j3dView.setScreenScale(screenScale) ;

        j3dView.setFieldOfView(fieldOfView) ;
        j3dView.setTrackingEnable(trackingEnable) ;
        j3dView.setSceneAntialiasingEnable(antialiasingEnable) ;

	if (screens.size() == 0) {
	    throw new IllegalStateException
		(errorMessage(creatingCommand, "View \"" + instanceName +
			      "\" has no canvases or screens")) ;
	}

	ConfigScreen[] cs = new ConfigScreen[screens.size()] ;
	screens.toArray(cs) ;

	j3dViewer = new Viewer(cs, this, setVisible) ;
	return j3dViewer ;
    }
}
