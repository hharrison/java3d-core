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

package com.sun.j3d.loaders.lw3d;



import java.io.*;
import java.util.Vector;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.util.Enumeration;
import com.sun.j3d.loaders.ParsingErrorException;

/**
 * This class creates a light object from the data in a Scene file. It
 * instantiates an LwsMotion object to create any associated
 * animations.
 */
	
class LwsLight extends TextfileParser implements LwsPrimitive {

    // data from the file
    String           fileName;
    String           objName;
    LwsMotion        motion;
    int              parent;
    TransformGroup   objectTransform;
    Vector           objectBehavior;
    Color3f          color;
    int              type;
    Point3f          attenuation = new Point3f(1.0f, 0.0f, 0.0f);
    float            spotConeAngle = (float)(Math.PI);
    // Meta object, used for holding light and 
    LwLightObject    lwLight;  
    // light parameters
    LwsEnvelopeLightIntensity intensityEnvelope = null;
    Light            light = null;
    final static int DIRECTIONAL = 0, POINT = 1, SPOT = 2;

    /**
     * Constructor: parses stream and creates data structures for all
     * light parameters currently handled by the loader
     */    
    LwsLight(StreamTokenizer st, int totalFrames, float totalTime,
	     int debugVals) throws ParsingErrorException {

	debugPrinter.setValidOutput(debugVals);

	debugOutput(TRACE, "LwsLight()");
	color = new Color3f(1f, 1f, 1f);
	lwLight = new LwLightObject(null, 0.0f, null);
	
	parent = -1;
	debugOutputLn(LINE_TRACE, "about to get LightName");
	getAndCheckString(st, "LightName");
	debugOutputLn(LINE_TRACE, "about to get LightName value");
	objName = getName(st);
	debugOutputLn(LINE_TRACE, "got LightName");
	skip(st, "ShowLight", 2);
	debugOutputLn(LINE_TRACE, "got ShowLight");
	getAndCheckString(st, "LightMotion");
	debugOutputLn(LINE_TRACE, "got LightMotion");
	motion = new LwsMotion(st, totalFrames, totalTime);
	debugOutputLn(LINE_TRACE, "got motions");
	
	// TODO: buggy way to stop processing the light.  Should actually
	// process required/optional fields in order and stop when there's
	// no more.  However, spec says "ShadowCasing" but the files say
	// "ShadowType".
	
	while (!isCurrentToken(st, "ShowCamera") &&
	       !isCurrentToken(st, "AddLight")) {
	    // TODO:
	    // Things that we're not yet processing (and should):
	    // - EdgeAngle: for spotlights, this is the angle which
	    // contains the linear falloff toward the edge of the
	    // ConeAngle.  This doesn't directly map to J3d's
	    // "concentration" value, so it's left out for now.

	    debugOutputLn(LINE_TRACE, "currentToken = " + st.sval);
	    
	    if (isCurrentToken(st, "ParentObject")) {
		parent = (int)getNumber(st);
	    }
	    else if (isCurrentToken(st, "LightColor")) {
		color.x = (float)getNumber(st)/255f;
		color.y = (float)getNumber(st)/255f;
		color.z = (float)getNumber(st)/255f;
		lwLight.setColor(color);
	    }
	    else if (isCurrentToken(st, "LgtIntensity")) {
		// TODO: must be able to handle envelopes here
		String className = getClass().getName();
		int classIndex = className.lastIndexOf('.');
		String packageName;
		if (classIndex < 0)
		    packageName = "";
		else
		    packageName = className.substring(0, classIndex) + ".";
		EnvelopeHandler env = 
		    new EnvelopeHandler(st, totalFrames, totalTime,
			    packageName + "LwsEnvelopeLightIntensity");
		if (env.hasValue) {
		    float intensity = (float)env.theValue;
		    color.x *= intensity;
		    color.y *= intensity;
		    color.z *= intensity;
		    lwLight.setIntensity(intensity);
		}
		else {
		    intensityEnvelope = 
			    (LwsEnvelopeLightIntensity)env.theEnvelope;
		}
	    }
	    else if (isCurrentToken(st, "LightType")) {
		type = (int)getNumber(st);
	    }
	    else if (isCurrentToken(st, "Falloff")) {
		float falloff = (float)getNumber(st);
		attenuation.y = 1.0f/(1.0f - falloff) - 1.0f;
	    }
	    else if (isCurrentToken(st, "ConeAngle")) {
		spotConeAngle = (float)getNumber(st) * (float)(Math.PI / 180.0);
	    }
	    try {
		st.nextToken();
	    }
	    catch (IOException e) {
		throw new ParsingErrorException(e.getMessage());
	    }
	}
	st.pushBack();   // push "ShowCamera" or "AddLight" back on stack
    }

    int getParent() {
	return parent;
    }

    /**
     * Create Java3D objects from the data we got from the file
     */
    void createJava3dObject(int loadBehaviors) {
	Matrix4d mat = new Matrix4d();
	mat.setIdentity();
	// Set the node's transform matrix according to the first frame
	// of the object's motion
	LwsFrame firstFrame = motion.getFirstFrame();
	firstFrame.setMatrix(mat);
	debugOutputLn(VALUES, "Light transform = " + mat);
	Transform3D t1 = new Transform3D();
	t1.set(mat);
	objectTransform = new TransformGroup(t1);
	objectTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	Vector3f defaultDir = new Vector3f(0f, 0f, -1f);
	Point3f defaultPos = new Point3f(0f, 0f, 0f);
	
	switch (type) {
	case DIRECTIONAL:
	    light = new DirectionalLight(color, defaultDir);
	    break;
	case POINT:
	    light = new PointLight(color, defaultPos, attenuation);
	    break;
	case SPOT:
	    // Note: spotConeAngle in lw3d is half that of Java3d...
	    light = new SpotLight(color, defaultPos, attenuation, defaultDir,
				  2 * spotConeAngle, 0.0f);
	    break;
	default:
	    // Shouldn't get here
	    break;
	}

	light.setCapability(Light.ALLOW_COLOR_WRITE);
	if (light != null)  {
	  lwLight.setLight(light);
	  BoundingSphere bounds = 
		    new BoundingSphere(new Point3d(0.0,0.0,0.0), 100000.0);
	  light.setInfluencingBounds(bounds);
	  objectTransform.addChild(light);

	  // load behaviors if we have to
	  objectBehavior = new Vector();
	  if (loadBehaviors != 0) {
	    Behavior b;
	    b = null;
	    motion.createJava3dBehaviors(objectTransform);
	    b = motion.getBehaviors(); 
	    if (b != null)
	      objectBehavior.addElement(b);

	    if (intensityEnvelope != null) {
	      b = null;
	      intensityEnvelope.createJava3dBehaviors(lwLight);
	      b = intensityEnvelope.getBehaviors(); 
	      if (b != null)
		objectBehavior.addElement(b);
	    }
	  }
	}
    }

    public TransformGroup getObjectNode()
	{
	    return objectTransform;
	}

    Light getLight() {
	return light;
    }
    
    public Vector getObjectBehaviors()
	{
	    debugOutputLn(TRACE, "getObjectBehaviors()");
	    return objectBehavior;
	}

    
    void printVals()
	{
	    debugOutputLn(VALUES, "  LIGHT vals: ");
	    debugOutputLn(VALUES, "   objName = " + objName);
	    motion.printVals();
	}

    
}	
