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

import javax.vecmath.Point3d ;
import javax.media.j3d.Sensor ;

class ConfigSensor extends ConfigObject {

    // The index of this sensor in the associated InputDevice.
    private int sensorIndex ;

    // The ConfigDevice which creates the associated InputDevice.
    private ConfigDevice configDevice ;

    // All configurable attributes.
    private Point3d hotspot = null ;
    private int predictor = -1 ;
    private int predictionPolicy = -1 ;
    private int sensorReadCount = -1 ;

    /**
     * The corresponding Java 3D core Sensor instance.  This is created by the
     * associated InputDevice.
     */
    Sensor j3dSensor ;

    /**
     * Handles the command
     * (NewSensor {instanceName} {inputDeviceName} {indexInInputDevice})
     * 
     * @param command the command that invoked this method
     */
    protected void initialize(ConfigCommand command) {

	int argc = command.argc ;
	Object[] argv = command.argv ;

	// Check that arg[1] and arg[2] are strings, arg[3] a number
	if (argc != 4) {
	    syntaxError("Incorrect number of arguments to " +
			command.commandName) ;
	}

	if (!isName(argv[2])) {
	    syntaxError("The second argument to " + command.commandName +
			" must be the device name") ;
	}

	if (!(argv[3] instanceof Double)) {
	    syntaxError("The third argument to " + command.commandName +
			" must be a sensor index") ;
	}

	sensorIndex = ((Double)argv[3]).intValue() ;
	configDevice = (ConfigDevice)configContainer.findConfigObject
	    ("Device", (String)argv[2]) ;
    }

    /**
     * Handles the commands
     * (SensorAttribute {instanceName} {attributeName} {attributeValue}) and
     * (SensorProperty  {instanceName} {attributeName} {attributeValue}).
     * 
     * @param command the command that invoked this method
     */
    protected void setProperty(ConfigCommand command) {

	int argc = command.argc ;
	Object[] argv = command.argv ;
	String attribute ;

	// Check that arg[1] and arg[2] are strings
	if (argc != 4) {
	    syntaxError("Incorrect number of arguments to " +
			command.commandName) ;
	}

	if (! isName(argv[1])) {
	    syntaxError("The first argument to " + command.commandName +
			" must be the instance name") ;
	}

	if (! isName(argv[2])) {
	    syntaxError("The second argument to " + command.commandName +
			" must be a property name") ;
	}

	attribute = (String)argv[2] ;
	if (attribute.equals("Hotspot")) {
	    if (! (argv[3] instanceof Point3d)) {
		syntaxError("Hotspot must be a 3D point") ;
	    }
	    hotspot = (Point3d)argv[3] ;
	}
	/*
	 * Questionable attributes.  Commented out for now.
	else if (attribute.equals("Predictor")) {
	    if (! isName(argv[3])) {
		syntaxError("Predictor must be a name") ;
	    }
	    
	    String predictorName = (String)argv[3] ;
	    if (predictorName.equals("PREDICT_NONE")) {
		predictor = Sensor.PREDICT_NONE ;
	    }
	    else if (predictorName.equals("PREDICT_NEXT_FRAME_TIME")) {
		predictor = Sensor.PREDICT_NEXT_FRAME_TIME ;
	    }
	    else {
		syntaxError("Predictor must be either PREDICT_NONE " +
			    "or PREDICT_NEXT_FRAME_TIME") ;
	    }
	}
	else if (attribute.equals("PredictionPolicy")) {
	    if (! isName(argv[3])) {
		syntaxError("PredictionPolicy must be a name") ;
	    }
	    
	    String predictionPolicyName = (String)argv[3] ;
	    if (predictionPolicyName.equals("NO_PREDICTOR")) {
		predictionPolicy = Sensor.NO_PREDICTOR ;
	    }
	    else if (predictionPolicyName.equals("HEAD_PREDICTOR")) {
		predictionPolicy = Sensor.HEAD_PREDICTOR ;
	    }
	    else if (predictionPolicyName.equals("HAND_PREDICTOR")) {
		predictionPolicy = Sensor.HAND_PREDICTOR ;
	    }
	    else {
		syntaxError("PredictionPolicy must be either NO_PREDICTOR, " +
			    "HEAD_PREDICTOR, or HAND_PREDICTOR") ;
	    }
	}
	else if (attribute.equals("SensorReadCount")) {
	    if (! (argv[3] instanceof Double)) {
		syntaxError("SensorReadCount must be a number") ;
	    }
	    sensorReadCount = ((Double)argv[3]).intValue() ;
	}
	*/
	else {
	    syntaxError("Unknown " + command.commandName +
			" \"" + attribute + "\"") ;
	}
    }

    /**
     * This method is called after all InputDevice implementations have been
     * instantiated and initialized.  All the specified attributes for this
     * sensor are set in the corresponding Java3D core Sensor instantiated by
     * the associated InputDevice.
     */
    void configureSensor() {
	j3dSensor = configDevice.j3dInputDevice.getSensor(sensorIndex) ;

	if (hotspot != null)
	    j3dSensor.setHotspot(hotspot) ;

	if (predictor != -1)
	    j3dSensor.setPredictor(predictor) ;

	if (predictionPolicy != -1)
	    j3dSensor.setPredictionPolicy(predictionPolicy) ;

	if (sensorReadCount != -1)
	    j3dSensor.setSensorReadCount(sensorReadCount) ;
    }
}
