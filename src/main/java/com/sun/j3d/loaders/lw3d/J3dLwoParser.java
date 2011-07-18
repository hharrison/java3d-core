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

import java.awt.Component;
import java.awt.Image;
import java.util.Enumeration;
import java.util.Vector;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Stripifier;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.loaders.IncorrectFormatException;
import java.io.FileNotFoundException;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.net.*;


/**
 * This class is responsible for turning Lightwave geometry data into
 * Java3D geometry. It is a subclass of LwoObject and calls that
 * superclass when first instantiated to parse the binary file and turn it
 * into intermediate data structures. J3dLwoParser then goes through
 * those data structures and turns them into Java3D objects. For each
 * ShapeHolder object created by the parent class, this class retrieves
 * the geometry data and associated surface data, creates the
 * appropriate Geometry object (usually IndexedTriangleFanArray,
 * unless the object is points or lines), including calculating normals for
 * the polygons and sets up the Appearance according to the surface
 * parameters (including calculating texture coordinates if necessary).
 */

class J3dLwoParser extends LwoParser {
	
    float normalCoordsArray[];
    int normalIndicesArray[];
    Shape3D objectShape;
    Color3f color, diffuseColor, specularColor, emissiveColor;
    float shininess;
    Vector objectShapeList = new Vector();
 
    /**
     * Constructor: Calls LwoObject to parse file and create data structures
     */   
    J3dLwoParser(String fileName, 
		 int debugVals) throws FileNotFoundException {
		     super(fileName, debugVals);
    }

    J3dLwoParser(URL url, int debugVals) 
	throws FileNotFoundException {
	    super(url, debugVals);
    }

    void getSurf(int length) throws FileNotFoundException {
	super.getSurf(length);
    }


    /**
     * Turns LwoObject's data structures (created from the binary geometry
     * file) into Java3d objects
     */
    void createJava3dGeometry() throws IncorrectFormatException {
	
	GeometryArray object;
	LwoTexture texture;

	for (Enumeration e = shapeList.elements();
	     e.hasMoreElements() ;) {
	    int vertexFormat = javax.media.j3d.GeometryArray.COORDINATES;
	    ShapeHolder shape = (ShapeHolder)e.nextElement();
	    debugOutputLn(LINE_TRACE, "about to create Arrays for Shape");
	    debugOutputLn(VALUES, "shape = " + shape);
	    shape.createArrays(true);
	    int vertexCount = shape.coordsArray.length/3;
	    int indexCount = 0;
	    if (shape.facetIndices != null)
		indexCount = shape.facetIndices.length;
	    debugOutputLn(VALUES, "numSurf = " + shape.numSurf);
	    // Find the right surface.  Note: surfaces are indexed by
	    // name.  So take this surf number, look up the name of that
	    // surface in surfaceNameList, then take that name and
	    // find the matching LwoSurface
	    String surfName =
		(String)surfNameList.elementAt(shape.numSurf - 1);
	    LwoSurface surf = null;
	    for (int surfNum = 0;
		 surfNum < surfaceList.size();
		 ++surfNum) {
		LwoSurface tempSurf =
		    (LwoSurface)surfaceList.elementAt(surfNum);
		String tempSurfName = tempSurf.surfName;
		if (surfName.equals(tempSurfName)) {
		    surf = tempSurf;
		    break;
		}
	    }
	    if (surf == null) {
		throw new IncorrectFormatException(
		    "bad surf for surfnum/name = " + shape.numSurf + ", " +
		    surfName);
	    }
	    debugOutputLn(VALUES, "surf = " + surf);

	    // Get the LwoTexture object (if any) for the surface
	    texture = surf.getTexture();

	    Appearance appearance = new Appearance();
	    if (shape.facetSizes[0] == 1) {
		// This case happens if the objects are points
		// Note that points are colored, not lit
		object = new 
		    javax.media.j3d.PointArray(vertexCount, vertexFormat);
		object.setCoordinates(0, shape.coordsArray);
		ColoringAttributes colorAtt =
		    new ColoringAttributes(surf.getColor(),
					   ColoringAttributes.FASTEST);
		PointAttributes pointStyle = new PointAttributes();
		pointStyle.setPointSize(1);
		
		appearance.setColoringAttributes(colorAtt);
		appearance.setPointAttributes(pointStyle);
	    }
	    else if (shape.facetSizes[0] == 2) {
		// This case happens if the objects are lines
		// Note that lines are colored, not lit
		debugOutputLn(LINE_TRACE, "Creating IndexedLineArray");
		object = new javax.media.j3d.LineArray(vertexCount,
						       vertexFormat);
		object.setCoordinates(0, shape.coordsArray);
		ColoringAttributes colorAtt =
		    new ColoringAttributes(surf.getColor(),
					   ColoringAttributes.FASTEST);
		appearance.setColoringAttributes(colorAtt);
	    }
	    else {
		// This is the case for any polygonal objects
		debugOutputLn(LINE_TRACE, "Creating IndexedTriFanArray");
				// create triFanArray
		vertexFormat |= javax.media.j3d.GeometryArray.NORMALS;
		
		debugOutputLn(LINE_TRACE, "about to process vertices/indices, facetIndices = " +
			      shape.facetIndices);
		if (shape.facetIndices != null) {
		    float[] textureCoords = null;
		    int[] textureIndices = null;
		    
		    debugOutputLn(LINE_TRACE, "setting vertexCount, normind = " + shape.normalIndices);
		    // If these are null we're going direct (non-indexed)
		    debugOutputLn(LINE_TRACE, "vtxcount, format, indcount = " +
				  vertexCount + ", " + vertexFormat +
				  ", " + indexCount);
		    if (texture != null) {
			// There's a texture here - need to create the appropriate arrays
			// and  calculate texture coordinates for the object
			vertexFormat |= GeometryArray.TEXTURE_COORDINATE_2;
			textureCoords = new float[vertexCount * 2];
			textureIndices = new int[shape.facetIndices.length];
			calculateTextureCoords(texture, shape.coordsArray,
					       shape.facetIndices,
					       textureCoords, textureIndices);
			debugOutputLn(LINE_TRACE, "textureCoords:");
			debugOutputLn(LINE_TRACE, "texture Coords, Indices.length = " + textureCoords.length + ", " + textureIndices.length);
		    }
		    debugOutputLn(LINE_TRACE, "about to create GeometryInfo");

		    // Use the GeometryInfo utility to calculate smooth normals
		    GeometryInfo gi =
			new GeometryInfo(GeometryInfo.TRIANGLE_FAN_ARRAY);
		    gi.setCoordinates(shape.coordsArray);
		    gi.setCoordinateIndices(shape.facetIndices);
		    gi.setStripCounts(shape.facetSizes);
		    if (texture != null) {
			gi.setTextureCoordinateParams(1, 2);
			gi.setTextureCoordinates(0, textureCoords);
			gi.setTextureCoordinateIndices(0, textureIndices);
		    }
		    gi.recomputeIndices();
		    NormalGenerator ng =
			new NormalGenerator(surf.getCreaseAngle());
		    ng.generateNormals(gi);
		    Stripifier st = new Stripifier();
		    st.stripify(gi);
		    object = gi.getGeometryArray(true, true, false);
		    debugOutputLn(LINE_TRACE, "done.");
		}
		else {
		    // This case is called if LwoObject did not create facet
		    // indices.  This code is not currently used because facet
		    // indices are always created for polygonal objects
		    debugOutputLn(LINE_TRACE,
				  "about to create trifanarray with " +
				  "vertexCount, facetSizes.len = " +
				  vertexCount + ", " +
				  shape.facetSizes.length);
		    object = new
			javax.media.j3d.TriangleFanArray(vertexCount,
							 vertexFormat,
							 shape.facetSizes);
		    object.setCoordinates(0, shape.coordsArray);
		    object.setNormals(0, shape.normalCoords);
		    debugOutputLn(VALUES, "passed in normalCoords, length = " +
				  shape.normalCoords.length);
		}
		debugOutputLn(LINE_TRACE, "created fan array");

		// Setup Appearance given the surface parameters
		Material material = new Material(surf.getColor(),
						 surf.getEmissiveColor(),
						 surf.getDiffuseColor(),
						 surf.getSpecularColor(),
						 surf.getShininess());
		material.setLightingEnable(true);
		appearance.setMaterial(material);
		if (surf.getTransparency() != 0f) {
		    TransparencyAttributes ta = new TransparencyAttributes();
		    ta.setTransparency(surf.getTransparency());
		    ta.setTransparencyMode(ta.BLENDED);
		    appearance.setTransparencyAttributes(ta);
		}
		if (texture != null) {
		    debugOutputLn(LINE_TRACE, "texture != null, enable texturing");
		    Texture tex = texture.getTexture();
		    tex.setEnable(true);
		    appearance.setTexture(tex);
		    TextureAttributes ta = new TextureAttributes();
		    if (texture.getType().equals("DTEX"))
			ta.setTextureMode(TextureAttributes.MODULATE);
		    else if (texture.getType().equals("CTEX"))
			ta.setTextureMode(TextureAttributes.DECAL);
		    appearance.setTextureAttributes(ta);
		}
		else {
		    debugOutputLn(LINE_TRACE, "texture == null, no texture to use");
		}
	    }
	    debugOutputLn(LINE_TRACE, "done creating object");
	   
	    // This does gc 
	    shape.nullify();

	    objectShape = new Shape3D(object);

	    // Combine the appearance and geometry
	    objectShape.setAppearance(appearance);
	    objectShapeList.addElement(objectShape);
	}
    }

    /**
     * Calculate texture coordinates for the geometry given the texture
     * map properties specified in the LwoTexture object
     */
    void calculateTextureCoords(LwoTexture texture,
				float verts[], int indices[],
				float[] textureCoords, int[] textureIndices) {

      /*
	   the actual math in these coord calculations comes directly from
	   Newtek - they posted sample code to help compute tex coords based
	   on the type of mapping:

	   Here are some simplified code fragments showing how LightWave
	   computes UV coordinates from X, Y, and Z.  If the resulting
	   UV coordinates are not in the range from 0 to 1, the
	   appropriate integer should be added to them to bring them into
	   that range (the fract function should have accomplished
	   this by subtracting the floor of each number from itself).
	   Then they can be multiplied by the width and height (in pixels)
	   of an image map to determine which pixel to look up.  The
	   texture size, center, and tiling parameters are taken right
	   off the texture control panel.


	   x -= xTextureCenter;
	   y -= yTextureCenter;
	   z -= zTextureCenter;
	   if (textureType == TT_PLANAR) {
	   s = (textureAxis == TA_X) ? z / zTextureSize + .5 :
	   x / xTextureSize + .5;
	   t = (textureAxis == TA_Y) ? -z / zTextureSize + .5 :
	   -y / yTextureSize + .5;
	   u = fract(s);
	   v = fract(t);
	   }
	   else if (type == TT_CYLINDRICAL) {
	   if (textureAxis == TA_X) {
	   xyztoh(z,x,-y,&lon);
	   t = -x / xTextureSize + .5;
	   }
	   else if (textureAxis == TA_Y) {
	   xyztoh(-x,y,z,&lon);
	   t = -y / yTextureSize + .5;
	   }
	   else {
	   xyztoh(-x,z,-y,&lon);
	   t = -z / zTextureSize + .5;
	   }
	   lon = 1.0 - lon / TWOPI;
	   if (widthTiling != 1.0)
	   lon = fract(lon) * widthTiling;
	   u = fract(lon);
	   v = fract(t);
	   }
	   else if (type == TT_SPHERICAL) {
	   if (textureAxis == TA_X)
	   xyztohp(z,x,-y,&lon,&lat);
	   else if (textureAxis == TA_Y)
	   xyztohp(-x,y,z,&lon,&lat);
	   else
	   xyztohp(-x,z,-y,&lon,&lat);
	   lon = 1.0 - lon / TWOPI;
	   lat = .5 - lat / PI;
	   if (widthTiling != 1.0)
	   lon = fract(lon) * widthTiling;
	   if (heightTiling != 1.0)
	   lat = fract(lat) * heightTiling;
	   u = fract(lon);
	   v = fract(lat);
	   }

	   support functions:

	   void xyztoh(float x,float y,float z,float *h)
	   {
	   if (x == 0.0 && z == 0.0)
	   *h = 0.0;
	   else {
	   if (z == 0.0)
	   *h = (x < 0.0) ? HALFPI : -HALFPI;
	   else if (z < 0.0)
	   *h = -atan(x / z) + PI;
	   else
	   *h = -atan(x / z);
	   }
	   }

	   void xyztohp(float x,float y,float z,float *h,float *p)
	   {
	   if (x == 0.0 && z == 0.0) {
	   *h = 0.0;
	   if (y != 0.0)
	   *p = (y < 0.0) ? -HALFPI : HALFPI;
	   else
	   *p = 0.0;
	   }
	   else {
	   if (z == 0.0)
	   *h = (x < 0.0) ? HALFPI : -HALFPI;
	   else if (z < 0.0)
	   *h = -atan(x / z) + PI;
	   else
	   *h = -atan(x / z);
	   x = sqrt(x * x + z * z);
	   if (x == 0.0)
	   *p = (y < 0.0) ? -HALFPI : HALFPI;
	   else
	   *p = atan(y / x);
	   }
	   }
      */

	debugOutputLn(TRACE, "calculateTextureCoords()");
	// Compute texture coord stuff
	float sx = 0, sz = 0, ty = 0, tz = 0;
	double s, t;
	int textureAxis = texture.getTextureAxis();
	Vector3f textureSize = texture.getTextureSize();
	Vector3f textureCenter = texture.getTextureCenter();

	String mappingType = texture.getMappingType();
	if (mappingType.startsWith("Cylindrical"))
	    calculateCylindricalTextureCoords(textureAxis, textureSize,
					      textureCenter, textureCoords,
					      textureIndices,
					      verts, indices);
	else if (mappingType.startsWith("Spherical"))
	    calculateSphericalTextureCoords(textureAxis, 
					    textureCenter, textureCoords,
					    textureIndices,
					    verts, indices);
	else if (mappingType.startsWith("Planar"))
	    calculatePlanarTextureCoords(textureAxis, textureSize,
					 textureCenter, textureCoords,
					 textureIndices,
					 verts, indices);

    }

    /** See the comments in calculateTextureCoordinates*/
    double xyztoh(float x,float y,float z) {
	if (x == 0.0 && z == 0.0)
	    return 0.0;
	else {
	    if (z == 0.0)
		return (x < 0.0) ? Math.PI/2.0 : -Math.PI/2.0;
	    else if (z < 0.0)
		return -Math.atan(x / z) + Math.PI;
	    else
		return -Math.atan(x / z);
	}
    }
	
    /** See the comments in calculateTextureCoordinates*/
    double xyztop(float x,float y,float z) {
	double p;
	
	if (x == 0.0 && z == 0.0) {
	    if (y != 0.0)
		p = (y < 0.0) ? -Math.PI/2 : Math.PI/2;
	    else
		p = 0.0;
	}
	else {
	    x = (float)Math.sqrt(x * x + z * z);
	    if (x == 0.0)
		p = (y < 0.0) ? -Math.PI/2 : Math.PI/2;
	    else
		p = Math.atan(y / x);
	}
	return p;
    }

    
    /** See the comments in calculateTextureCoordinates*/
    void calculateSphericalTextureCoords(int textureAxis,
					 Vector3f textureCenter,
					 float textureCoords[],
					 int textureIndices[],
					 float verts[], int indices[]) {
	debugOutputLn(TRACE, "calculateSphericalTextureCoords");
	double s, t;
	
	
	for (int i = 0; i < indices.length; ++i) {
	    float x = verts[3*indices[i]] - textureCenter.x;
	    float y = verts[3*indices[i]+1] - textureCenter.y;
	    float z = -(verts[3*indices[i]+2] + textureCenter.z);
	    if (textureAxis == 1){ // X Axis
		s = xyztoh(z, x, -y);
		t = xyztop(z,x,-y);
	    }
	    else if (textureAxis == 2) { // Y Axis
		s = xyztoh(-x,y,z);
		t = xyztop(-x,y,z);
	    }
	    else {			// Z Axis
		s = xyztoh(-x,z,-y);
		t = xyztop(-x,z,-y);
	    }
	    s = 1.0 - s / (2*Math.PI);
	    t = -(.5 - t / Math.PI);
	    textureCoords[indices[i]*2] = (float)s;
	    textureCoords[indices[i]*2 + 1] = (float)t;
	    textureIndices[i] = indices[i];
	}
    }
	
    /** See the comments in calculateTextureCoordinates*/
    void calculateCylindricalTextureCoords(int textureAxis,
					   Vector3f textureSize,
					   Vector3f textureCenter,
					   float textureCoords[],
					   int textureIndices[],
					   float verts[], int indices[]) {
	debugOutputLn(TRACE, "calculateCylindricalTextureCoords");
	debugOutputLn(VALUES, "axis, size, center, tc, ti, v, i = " +
		      textureAxis + ", " +
		      textureSize + ", " +
		      textureCenter + ", " +
		      textureCoords + ", " +
		      textureIndices + ", " +
		      verts + ", " +
		      indices);
	double s, t;
	
	debugOutputLn(VALUES, "Cyl Texture Coords:");
	for (int i = 0; i < indices.length; ++i) {
	    float x = verts[3*indices[i]] - textureCenter.x;
	    float y = verts[3*indices[i]+1] - textureCenter.y;
	    float z = -(verts[3*indices[i]+2] + textureCenter.z);
	    // Negate z value because we invert geom z's to swap handedness
	    if (textureAxis == 1) { // X axis
		s = xyztoh(z,x,-y);
		t = x / textureSize.x + .5;
	    }
	    else if (textureAxis == 2) { // Y Axis
		s = xyztoh(-x,y,z);
		t = y / textureSize.y + .5;
	    }
	    else {
		s = xyztoh(-x,z,-y);
		t = z / textureSize.z + .5;
	    }
	    s = 1.0 - s / (2*Math.PI);
	    textureCoords[indices[i]*2] = (float)s;
	    textureCoords[indices[i]*2 + 1] = (float)t;
	    textureIndices[i] = indices[i];
	    debugOutputLn(VALUES, "x, y, z = " +
			  x + ", " + y + ", " + z + "    " +
			  "s, t = " + s + ", " + t);
	}
    }
    
    /** See the comments in calculateTextureCoordinates*/
    void calculatePlanarTextureCoords(int textureAxis, Vector3f textureSize,
				      Vector3f textureCenter,
				      float textureCoords[],
				      int textureIndices[],
				      float verts[], int indices[]) {
	debugOutputLn(TRACE, "calculatePlanarTextureCoords");
	debugOutputLn(VALUES, "size, center, axis = " +
		      textureSize + textureCenter + ", " + textureAxis);
	float sx = 0, sz = 0, ty = 0, tz = 0;
	double s, t;

	if (textureAxis == 1) {		// X Axis
	    sz = -1.0f / textureSize.z;  // Negate because we negate z in geom
	    ty = 1.0f / textureSize.y;
	}
	else if (textureAxis == 2) {	// Y Axis
	    sx = 1.0f / textureSize.x;
	    tz = -1.0f / textureSize.z;  // Negate because we negate z in geom
	}
	else {				// Z Axis
	    sx = 1.0f / textureSize.x;
	    ty = 1.0f / textureSize.y;
	}

	debugOutputLn(VALUES, "Planar Texture Coords:");
	for (int i = 0; i < indices.length; ++i) {
	    float x = verts[3*indices[i]] - textureCenter.x;
	    float y = verts[3*indices[i]+1] - textureCenter.y;
	    float z = verts[3*indices[i]+2] + textureCenter.z;
	    s = x*sx + z*sz + .5;
	    t = y*ty + z*tz + .5;
	    textureCoords[indices[i]*2] = (float)s;
	    textureCoords[indices[i]*2 + 1] = (float)t;
	    textureIndices[i] = indices[i];
	    debugOutputLn(VALUES, "x, y, z = " +
			  x + ", " + y + ", " + z + "    " +
			  "s, t = " + s + ", " + t);
	}
    }

    
    Shape3D getJava3dShape() {
	return objectShape;
    }

    Vector getJava3dShapeList() {
	return objectShapeList;
    }

}
