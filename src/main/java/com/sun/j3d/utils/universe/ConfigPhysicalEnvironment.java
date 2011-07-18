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

import javax.media.j3d.* ;
import javax.vecmath.* ;
import java.util.* ;

class ConfigPhysicalEnvironment extends ConfigObject {

    /**
     * The corresponding J3D core PhysicalEnvironment instance.
     */
    PhysicalEnvironment j3dPhysicalEnvironment = null ;

    /**
     * The coexistence to tracker base matrix.
     */
    Matrix4d coexistenceToTrackerBase = null ;

    // All other configurable attributes.
    private ConfigSensor headTracker = null ;
    private ArrayList inputDevices = new ArrayList() ;
    private int coexistenceCenterInPworldPolicy = View.NOMINAL_SCREEN ;

    /**
     * Overrides initialize() to do nothing.
     */
    protected void initialize(ConfigCommand command) {
    }

    /**
     * Handles the commands
     * (PhysicalEnvironmentAttribute {instance} {attrName} {attrValue}) and
     * (PhysicalEnvironmentProperty  {instance} {attrName} {attrValue}).
     * 
     * @param command the command that invoked this method
     */
    protected void setProperty(ConfigCommand command) {
	Object val ;
	Object[] argv = command.argv ;
	int argc = command.argc ;
	String sval, prop ;

	if (argc != 4) {
	    syntaxError("Incorrect number of arguments to " +
			command.commandName) ;
	}

	if (!isName(argv[1])) {
	    syntaxError("The first argument to " + command.commandName + 
			" must be a name") ;
	}

	if (!isName(argv[2])) {
	    syntaxError("The second argument to " + command.commandName +
			" must be a property name") ;
	}

	prop = (String)argv[2] ;
	val = argv[3] ;

	if (prop.equals("CoexistenceCenterInPworldPolicy")) {
	    if (!(val instanceof String))
		syntaxError("CoexistenceCenterInPworldPolicy must be string") ;

	    sval = (String)val ;
	    if (sval.equals("NOMINAL_HEAD"))
		coexistenceCenterInPworldPolicy = View.NOMINAL_HEAD ;
	    else if (sval.equals("NOMINAL_SCREEN"))
		coexistenceCenterInPworldPolicy = View.NOMINAL_SCREEN ;
	    else if (sval.equals("NOMINAL_FEET"))
		coexistenceCenterInPworldPolicy = View.NOMINAL_FEET ;
	    else
		syntaxError("Illegal value " + sval +
			    " for CoexistenceCenterInPworldPolicy") ;
	}
	else if (prop.equals("CoexistenceToTrackerBase")) {
	    if (val instanceof Matrix4d)
		coexistenceToTrackerBase = (Matrix4d)val ;
	    else
		syntaxError("CoexistenceToTrackerBase must be a Matrix4d") ;
	}
	else if (prop.equals("InputDevice")) {
	    if (!(val instanceof String))
		syntaxError("InputDevice must be a name") ;

	    sval = (String)val ;
	    inputDevices.add(configContainer.findConfigObject("Device", sval));
	}
	else if (prop.equals("HeadTracker")) {
	    if (!(val instanceof String))
		syntaxError("HeadTracker must be a Sensor name") ;

	    sval = (String)val ;
	    headTracker = 
		(ConfigSensor)configContainer.findConfigObject("Sensor", sval);
	}
	else {
	    syntaxError("Unknown " + command.commandName +
			" \"" + prop + "\"") ;
	}
    }

    /**
     * Create a core Java 3D PhysicalEnvironment instance using the attributes
     * gathered by this object.
     */
    PhysicalEnvironment createJ3dPhysicalEnvironment() {
	j3dPhysicalEnvironment = new PhysicalEnvironment() ;

	j3dPhysicalEnvironment.setCoexistenceCenterInPworldPolicy
	    (coexistenceCenterInPworldPolicy) ;
	    
	if (coexistenceToTrackerBase != null)
	    j3dPhysicalEnvironment.setCoexistenceToTrackerBase
		(new Transform3D(coexistenceToTrackerBase)) ;

	return j3dPhysicalEnvironment ;
    }

    /**
     * Process the devices associated with the PhysicalEnvironment.
     */
    void processDevices() {
	for (int j = 0; j < inputDevices.size(); j++) {
	    ConfigDevice configDevice = (ConfigDevice)inputDevices.get(j) ;
	    InputDevice device = configDevice.j3dInputDevice ;
	    j3dPhysicalEnvironment.addInputDevice(device) ;
	}

	if (headTracker != null) {
	    j3dPhysicalEnvironment.setHeadIndex(0) ;
	    j3dPhysicalEnvironment.setSensor(0, headTracker.j3dSensor) ;
	}
    }
}
