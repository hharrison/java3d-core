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

import java.awt.Window ;
import javax.media.j3d.Canvas3D ;
import javax.media.j3d.View ;
import javax.swing.JFrame ;
import javax.swing.JPanel ;
import javax.vecmath.Matrix4d ;
import javax.vecmath.Point2d ;

class ConfigScreen extends ConfigObject {

    /**
     * The index of this screen in the GraphicsDevice array returned by
     * GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().
     */
    int		frameBufferNumber ;

    /**
     * The physical width in meters of the screen area of the GraphicsDevice
     * associated with this ConfigScreen.  The default is based on a screen
     * resolution of 90 pixels/inch.
     */
    double	physicalScreenWidth = 0.0 ;

    /**
     * The physical height in meters of the screen area of the GraphicsDevice
     * associated with this ConfigScreen.  The default is based on a screen
     * resolution of 90 pixels/inch.
     */
    double	physicalScreenHeight = 0.0 ;

    /**
     * The trackerBaseToImagePlate transform of this ConfigScreen.
     * The default is the identity transform.
     */
    Matrix4d	trackerBaseToImagePlate = null ;

    /**
     * The headTrackerToLeftImagePlate transform of this ConfigScreen if HMD
     * mode is in effect.  The default is the identity transform.
     */
    Matrix4d	headTrackerToLeftImagePlate = null ;

    /**
     * The headTrackerToRightImagePlate transform of this ConfigScreen if HMD
     * mode is in effect.  The default is the identity transform.
     */
    Matrix4d	headTrackerToRightImagePlate = null ;

    /**
     * The monoscopicViewPolicy for this ConfigScreen.  The default is
     * View.CYCLOPEAN_EYE_VIEW.
     */
    int		monoscopicViewPolicy = View.CYCLOPEAN_EYE_VIEW ;

    /**
     * Boolean indicating whether a full-screen window should be created for
     * this ConfigScreen.  The default is false.
     */
    boolean	fullScreen = false ;

    /**
     * Boolean indicating whether a full-screen window with no borders should
     * be created for this ConfigScreen.  The default is false.
     */
    boolean	noBorderFullScreen = false ;

    /**
     * The width in pixels for the window to be created for this ConfigScreen
     * if a full screen window is not specified.  The default is 512.
     */
    int		windowWidthInPixels = 512 ;

    /**
     * The height in pixels for the window to be created for this ConfigScreen
     * if a full screen window is not specified.  The default is 512.
     */
    int		windowHeightInPixels = 512 ;

    /**
     * The X pixel position of the top-left corner of the window, relative to
     * the physical screen.  The default is 0.
     */
    int         windowX = 0 ;

    /**
     * The Y pixel position of the top-left corner of the window, relative to
     * the physical screen.  The default is 0.
     */
    int         windowY = 0 ;

    /**
     * The JFrame created for this ConfigScreen.  When running under JDK 1.4
     * or newer, the JFrame always contains a JPanel which contains the
     * Canvas3D.  When running under JDK 1.3.1 and using a borderless full
     * screen the JFrame will instead contain a JWindow which will contain the
     * JPanel and Canvas3D.
     */
    JFrame	j3dJFrame ;

    /**
     * The Window created for this ConfigScreen.  Under JDK 1.4 or higher this
     * is the same reference as j3dJFrame.  If a borderless full screen is
     * specified while running under JDK 1.3.1 then this is a JWindow with the
     * j3dJFrame as its parent.
     */
    Window	j3dWindow ;

    /**
     * The JPanel created for this ConfigScreen to hold the Canvas3D.
     */
    JPanel	j3dJPanel ;

    /**
     * The Canvas3D created for this ConfigScreen.
     */
    Canvas3D	j3dCanvas ;

    /**
     * Processes attributes for this object.  Handles commands of the form:<p>
     * (ScreenAttribute  {instanceName} {attrName} {attrValue})
     * (ScreenProperty   {instanceName} {attrName} {attrValue})
     * (DisplayAttribute {instanceName} {attrName} {attrValue})
     * (DisplayProperty  {instanceName} {attrName} {attrValue})
     * 
     * @param command the command that invoked this method
     */
    protected void setProperty(ConfigCommand command) {

	String attr = null ;
	Object val = null ;
	String sval = null ;

	if (command.argc != 4) {
	    syntaxError("Incorrect number of arguments to " +
			command.commandName) ;
	}

	if (!isName(command.argv[2])) {
	    syntaxError("The second argument to " + command.commandName + 
			" must be a property name") ;
	}

	attr = (String)command.argv[2] ;
	val = command.argv[3] ;

	if (attr.equals("PhysicalScreenWidth")) {
	    if (!(val instanceof Double)) {
		syntaxError("Value for PhysicalScreenWidth " +
			    "must be a number") ;
	    }
	    physicalScreenWidth = ((Double)val).doubleValue() ;
	}
	else if (attr.equals("PhysicalScreenHeight")) {
	    if (!(val instanceof Double)) {
		syntaxError("Value for PhysicalScreenHeight " +
			    "must be a number") ;
	    }
	    physicalScreenHeight = ((Double)val).doubleValue() ;
	}
	else if (attr.equals("TrackerBaseToImagePlate")) {
	    if (!(val instanceof Matrix4d)) {
		syntaxError("Value for TrackerBaseToImagePlate " +
			    "must be a 4x3 or 4x4 matrix") ;
	    }
	    trackerBaseToImagePlate = (Matrix4d)val ;
	}
	else if (attr.equals("HeadTrackerToLeftImagePlate")) {
	    if (!(val instanceof Matrix4d)) {
		syntaxError("Value for HeadTrackerToLeftImagePlate "
			    + "must be a 4x3 or 4x4 matrix") ;
	    }
	    headTrackerToLeftImagePlate = (Matrix4d)val ;
	}
	else if (attr.equals("HeadTrackerToRightImagePlate")) {
	    if (!(val instanceof Matrix4d)) {
		syntaxError("Value for HeadTrackerToRightImagePlate "
			    + "must be a 4x3 or 4x4 matrix") ;
	    }
	    headTrackerToRightImagePlate = (Matrix4d)val ;
	}
	else if (attr.equals("MonoscopicViewPolicy")) {
	    if (!(val instanceof String)) {
		syntaxError("Value for MonoscopicViewPolicy " +
			    "must be a name") ;
	    }
	    sval = (String)val ;
	    if (sval.equals("LEFT_EYE_VIEW"))
		monoscopicViewPolicy = View.LEFT_EYE_VIEW ;
	    else if (sval.equals("RIGHT_EYE_VIEW"))
		monoscopicViewPolicy = View.RIGHT_EYE_VIEW ;
	    else if (sval.equals("CYCLOPEAN_EYE_VIEW"))
		monoscopicViewPolicy = View.CYCLOPEAN_EYE_VIEW ;
	    else
		syntaxError("Invalid value for MonoscopicViewPolicy "
			    + "\"" + sval + "\"") ;
	}
	else if (attr.equals("WindowPosition")) {
	    if (! (val instanceof Point2d)) {
		syntaxError("WindowPosition must be a Point2d") ;
	    }
	    Point2d p2d = (Point2d)val ;
	    windowX = (int)p2d.x ;
	    windowY = (int)p2d.y ;
	}
	else if (attr.equals("WindowSize")) {
	    if (val instanceof Point2d) {
		fullScreen = false ;
		noBorderFullScreen = false ;

		Point2d p2d = (Point2d)val ;
		windowWidthInPixels = (int)p2d.x ;
		windowHeightInPixels = (int)p2d.y ;
	    }
	    else if (val instanceof String) {
		String s = (String)val ;

		if (s.equals("FullScreen")) {
		    fullScreen = true ;
		    noBorderFullScreen = false ;
		} else if (s.equals("NoBorderFullScreen")) {
		    fullScreen = false ;
		    noBorderFullScreen = true ;
		} else {
		    syntaxError("Value for WindowSize " +
				"must be one of\n" + "\"FullScreen\" " +
				"\"NoBorderFullScreen\" or Point2d") ;
		}
	    }
	    else {
		syntaxError("Invalid WindowSize value: " + val +
			    "\nValue for WindowSize " +
			    "must be one of\n" + "\"FullScreen\" " +
			    "\"NoBorderFullScreen\" or Point2d") ;
	    }
	}
	else {
	    syntaxError("Unknown " + command.commandName +
			" \"" + attr + "\"") ;
	}
    }

    /**
     * Initializes this object.  Handles commands of the form:<p>
     * (NewScreen {instanceName} {FrameBufferNumber}).
     * 
     * @param command the command that invoked this method
     */
    protected void initialize(ConfigCommand command) {
	if (command.argc != 3) {
	    syntaxError("Incorrect number of arguments to " +
			command.commandName) ;
	}

	if (!(command.argv[2] instanceof Double)) {
	    syntaxError("The second argument to " + command.commandName +
			" must be a GraphicsDevice index") ;
	}

	frameBufferNumber = ((Double)command.argv[2]).intValue() ;
    }
}
