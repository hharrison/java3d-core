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

package com.sun.j3d.utils.pickfast;

import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.geometry.Primitive;

/**
 * Holds information about an intersection of a PickShape with a Node 
 * as part of a PickInfo.IntersectionInfo. Information about
 * the intersected geometry, intersected primitive, intersection point, and 
 * closest vertex can be inquired.  
 * <p>
 * The intersected primitive indicates which primitive out of the GeometryArray
 * was intersected (where the primitive is a point, line, triangle or quad, 
 * not a
 * <code>com.sun.j3d.utils.geometry.Primitive)</code>.  
 * For example, the intersection would indicate which triangle out of a 
 * triangle strip was intersected.
 * The methods which return primitive data will have one value if the primitive 
 * is
 * a point, two values if the primitive is a line, three values if the primitive
 * is a triangle and four values if the primitive is quad.
 * <p>
 * The primitive's VWorld coordinates are saved when then intersection is 
 * calculated.  The local coordinates, normal, color and texture coordinates
 * for the primitive can also be inquired if they are present and readable.
 * <p>
 * The intersection point is the location on the primitive which intersects the
 * pick shape closest to the center of the pick shape. The intersection point's
 * location in VWorld coordinates is saved when the intersection is calculated.
 * The local coordinates, normal, color and texture coordiantes of at the
 * intersection can be interpolated if they are present and readable.
 * <p>
 * The closest vertex is the vertex of the primitive closest to the intersection
 * point.  The vertex index, VWorld coordinates and local coordinates of the 
 * closest vertex can be inquired.  The normal, color and texture coordinate
 * of the closest vertex can be inquired from the geometry array:
 * <p><blockquote><pre>
 *      Vector3f getNormal(PickIntersection pi, int vertexIndex) {
 *          int index;
 *          Vector3d normal = new Vector3f();
 *          GeometryArray ga = pickIntersection.getGeometryArray();
 *          if (pickIntersection.geometryIsIndexed()) {
 *              index = ga.getNormalIndex(vertexIndex);
 *          } else {
 *              index = vertexIndex;
 *          }
 *          ga.getNormal(index, normal);
 *          return normal;
 *      }
 * </pre></blockquote>
 * <p>
 * The color, normal
 * and texture coordinate information for the intersected primitive and the 
 * intersection point
 * can be inquired
 * the geometry includes them and the corresponding READ capibility bits are 
 * set.
 */

public class PickIntersection {

    
    /* The intersection point */
    // Point3d getIntersectionPoint()
    
    /* Distance between start point of pickShape and intersection point */
    // double getDistance()
    
    /* The vertex indices of the intersected primitive in the geometry */   
    // int[] getVertexIndices()

    /*************************/

    /** Weight factors for interpolation, values correspond to vertex indices,
     * sum == 1
     */
    private double[] interpWeights;

    private static final boolean debug = false;

    // Axis constants
    private static final int X_AXIS = 1;
    private static final int Y_AXIS = 2;
    private static final int Z_AXIS = 3;

    // Tolerance for numerical stability
    static final double TOL = 1.0e-5;

    /* The references to the intersectionInfo object */
    private PickInfo.IntersectionInfo iInfo = null;
    private Transform3D l2vw = null;
    private Geometry geometry = null;
    private boolean geometryIsIndexed = false;
    private double distance;

    private boolean hasColors;
    private boolean hasNormals;
    private boolean hasTexCoords;

    // Primitive
    /* indices for the different data types */
    private int[] primitiveCoordinateIndices;
    private int[] primitiveNormalIndices;
    private int[] primitiveColorIndices;
    private int[] primitiveTexCoordIndices;
 
    /** Indices of the intersected primitive */
    private int[] primitiveVertexIndices = null;

   /* Local coordinates of the intersected primitive */
    private Point3d[] primitiveCoordinates = null;

    /** VWorld coordinates of intersected primitive */
    private Point3d[] 	primitiveCoordinatesVW = null;

    /* Normals of the intersected primitive */
    private Vector3f[] primitiveNormals = null;

    /* Colors of the intersected primitive */
    private Color4f[] primitiveColors = null;

    /* TextureCoordinates of the intersected primitive */
    private TexCoord3f[] primitiveTexCoords = null;

    // Intersection point
    /** VWorld Coordinates of the intersection point */
    private Point3d pointCoordinatesVW = null;

    /** Local Coordinates of the intersection point */
    private Point3d pointCoordinates = null;

    /** Normal at the intersection point */
    private Vector3f pointNormal = null;

    /** Color at the intersection point */
    private Color4f pointColor = null;

    /** TexCoord at the intersection point */
    private TexCoord3f pointTexCoord = null;

    // Closest Vertex
    /** Index of the closest vertex */
    private int closestVertexIndex = -1;

    /** Coordinates of the closest vertex */
    private Point3d closestVertexCoordinates = null;

    /** Coordinates of the closest vertex (World coordinates) */  
    private Point3d closestVertexCoordinatesVW = null;    

    /* ===================   METHODS  ======================= */

    /** 
     * Constructor 
     * @param intersectionInfo The IntersectionInfo this intersection is part of.
     */
    public PickIntersection (Transform3D localToVWorld,
			     PickInfo.IntersectionInfo intersectionInfo) {

	// Should check and throw NPE if the following is null. 
	// localToVWorld can't be null.
	l2vw = localToVWorld;
	// intersectionInfo can't be null.
	iInfo = intersectionInfo;
	// geometry can't be null.
	geometry = iInfo.getGeometry();

	pointCoordinates = iInfo.getIntersectionPoint();
	distance = iInfo.getDistance();
	primitiveVertexIndices = iInfo.getVertexIndices();

	if (geometry instanceof GeometryArray) {
	    
	    int vertexFormat = ((GeometryArray)geometry).getVertexFormat();
	    hasColors = (0 != (vertexFormat &
			       (GeometryArray.COLOR_3 | GeometryArray.COLOR_4)));
	    hasNormals = (0 != (vertexFormat & GeometryArray.NORMALS));
	    hasTexCoords = (0 != (vertexFormat &
				  (GeometryArray.TEXTURE_COORDINATE_2 |
				   GeometryArray.TEXTURE_COORDINATE_3)));
	   
	    if (geometry instanceof IndexedGeometryArray) {
		geometryIsIndexed = true;
	    }
	}
    }
    
    /** 
     * Returns true if the geometry is indexed 
     *
     */
    public boolean geometryIsIndexed() {
	return geometryIsIndexed;
    }

    /** 
     * Get coordinates of closest vertex (local) 
     * @return the coordinates of the vertex closest to the intersection point
     *
     */
    public Point3d getClosestVertexCoordinates() {
	// System.out.println("PI.closestVertexCoordinates " + closestVertexCoordinates);
	GeometryArray geom = (GeometryArray) geometry;

	if (closestVertexCoordinates == null) {
	    int vertexIndex = getClosestVertexIndex();
	    int vformat = geom.getVertexFormat();
	    int val;
	    
	    int[] indices = getPrimitiveCoordinateIndices();
	    if ((vformat & GeometryArray.BY_REFERENCE) == 0) {
		closestVertexCoordinates = new Point3d();
		geom.getCoordinate(indices[vertexIndex], closestVertexCoordinates);
		// System.out.println("PI.closestVertexCoordinates " +
// 				   closestVertexCoordinates + " vertexIndex " +
// 				   vertexIndex);
	    }
	    else {
		if ((vformat & GeometryArray.INTERLEAVED) == 0) {
		    double[] doubleData = geom.getCoordRefDouble();
		    // If data was set as float then ..
		    if (doubleData == null) {
			float[] floatData = geom.getCoordRefFloat();
 			if (floatData == null) {
			    throw new UnsupportedOperationException("Deprecated : BY_REF - p3f and p3d");
			}
			else {
			    val = indices[vertexIndex] * 3; // for x,y,z
			    closestVertexCoordinates = new Point3d(floatData[val], 
								   floatData[val+1], 
								   floatData[val+2]);
			}
		    }
		    else {
			val = indices[vertexIndex] * 3; // for x,y,z
			closestVertexCoordinates = new Point3d(doubleData[val], 
							       doubleData[val+1], 
							       doubleData[val+2]);
		    }
		}
		else {
		    float[] floatData = geom.getInterleavedVertices();
		    int offset = getInterleavedVertexOffset(geom);
		    int stride = offset + 3; // for the vertices .
		    val = stride * indices[vertexIndex]+offset;
		    closestVertexCoordinates = new Point3d(floatData[val], 
							   floatData[val+1], 
							   floatData[val+2]);
		}
	    }
	}
	
	return closestVertexCoordinates;
    }

    /**
     * Get coordinates of closest vertex (world) 
     * @return the coordinates of the vertex closest to the intersection point
     *
     */
    public Point3d getClosestVertexCoordinatesVW() {
	if (closestVertexCoordinatesVW == null) {
	    int vertexIndex = getClosestVertexIndex();
	    Point3d[] coordinatesVW = getPrimitiveCoordinatesVW();
	    closestVertexCoordinatesVW = coordinatesVW[vertexIndex];
	}
	return closestVertexCoordinatesVW;
    }

    /** 
     * Get index of closest vertex 
     * @return the index of the closest vertex
     */
    public int getClosestVertexIndex() { 
	if (closestVertexIndex == -1) {
	    double maxDist = Double.MAX_VALUE;
	    double curDist = Double.MAX_VALUE;
	    int closestIndex = -1;
	    primitiveCoordinates = getPrimitiveCoordinates();

	    assert(primitiveCoordinates != null);

// 	    System.out.println("PI.getClosestVertexIndex : primitiveCoordinates.length " +
// 			       primitiveCoordinates.length);
	    
	    for (int i=0;i<primitiveCoordinates.length;i++) {
		curDist = pointCoordinates.distance (primitiveCoordinates[i]);
		
// 		System.out.println("pointCoordinates " + pointCoordinates);
// 		System.out.println("primitiveCoordinates[" + i + "] " +
// 				   primitiveCoordinates[i]);
// 		System.out.println("curDist " + curDist);
		
		if (curDist < maxDist) {
		    closestIndex = i;
		    maxDist = curDist;
		}
	    }
	    closestVertexIndex = closestIndex;
	}
	return closestVertexIndex;
    }
    
    /** 
     * Get the distance from the PickShape start point to the intersection point
     * @return the distance to the intersection point, if available.
     */
    public double getDistance() {
	return distance;
    }


    /**
     * Returns the color of the intersection point. Returns null if the geometry
     * does not contain colors.  If the geometry was defined with
     * GeometryArray.COLOR_3, the 'w' component of the color will initialized to 
     * 1.0
     * @return color at the intersection point.  
     */
    public Color4f getPointColor() {
	if (hasColors && (pointColor == null)) {
	    double[] weights = getInterpWeights();
	    Color4f[] colors = getPrimitiveColors();
	    pointColor = new Color4f();
	    for (int i = 0; i < weights.length; i++) {
		pointColor.x += (float) weights[i] * colors[i].x;
		pointColor.y += (float) weights[i] * colors[i].y;
		pointColor.z += (float) weights[i] * colors[i].z;
		pointColor.w += (float) weights[i] * colors[i].w;
	    }
	}
	return pointColor;
    }


    /**
     * Returns the coordinates of the intersection point (local coordinates),
     * if available.
     * @return coordinates of the intersection point
     */
    public Point3d getPointCoordinates() {
	return pointCoordinates;
    }
    
    /**
     * Returns the coordinates of the intersection point (world coordinates), 
     * if available.
     * @return coordinates of the point
     */
    public Point3d getPointCoordinatesVW() {

	if (pointCoordinatesVW != null) {
	    return pointCoordinatesVW;
	}

	pointCoordinatesVW = new Point3d();
	
	pointCoordinatesVW.x = pointCoordinates.x;
	pointCoordinatesVW.y = pointCoordinates.y;
	pointCoordinatesVW.z = pointCoordinates.z;

	l2vw.transform(pointCoordinatesVW);
	return pointCoordinatesVW;
    }

    /**
     * Returns the normal of the intersection point. Returns null if the geometry
     * does not contain normals.
     * @return normal at the intersection point.
     */
    public Vector3f getPointNormal() {
	if (hasNormals && (pointNormal == null)) {
	    double[] weights = getInterpWeights();
	    Vector3f[] normals = getPrimitiveNormals();
	    pointNormal = new Vector3f();
	    for (int i = 0; i < weights.length; i++) {
		pointNormal.x += (float) weights[i] * normals[i].x;
		pointNormal.y += (float) weights[i] * normals[i].y;
		pointNormal.z += (float) weights[i] * normals[i].z;
	    }
	}
	return pointNormal;
    }

    /**
     * Returns the texture coordinate of the intersection point at the specifed 
     * index in the specified texture coordinate set.
     * Returns null if the geometry
     * does not contain texture coordinates.  If the geometry was defined with
     * GeometryArray.TEXTURE_COORDINATE_3, the 'z' component of the texture
     * coordinate will initialized to 0.0
     * @return texture coordinate at the intersection point.  
     */
    public TexCoord3f getPointTextureCoordinate(int index) {
	if (hasTexCoords && (pointTexCoord == null)) {
	    double[] weights = getInterpWeights();
	    TexCoord3f[] texCoords = getPrimitiveTexCoords(index);
	    pointTexCoord = new TexCoord3f();
	    for (int i = 0; i < weights.length; i++) {
		pointTexCoord.x += (float) weights[i] * texCoords[i].x;
		pointTexCoord.y += (float) weights[i] * texCoords[i].y;
		pointTexCoord.z += (float) weights[i] * texCoords[i].z;
	    }
	}
	return pointTexCoord;
    }

    /** 
     * Get the color indices for the intersected primitive.  For a non-indexed
     * primitive, this will be the same as the primitive vertex indices
     * If the geometry array does not contain colors this will return null.
     * @return an array indices
     */
    public int[] getPrimitiveColorIndices() {
	if (hasColors && (primitiveColorIndices == null)) {
	    if (geometryIsIndexed()) {
		primitiveColorIndices = 
		    new int[primitiveVertexIndices.length];
		for (int i = 0; i < primitiveVertexIndices.length; i++) {
		    primitiveColorIndices[i] =  
			((IndexedGeometryArray)(geometry)).getColorIndex(primitiveVertexIndices[i]);
		}
	    } else {
		primitiveColorIndices = primitiveVertexIndices;
	    }
	}
	return primitiveColorIndices;
    }

    /** 
     * Get the colors of the intersected primitive.  This will return null if
     * the primitive does not contain colors.  If the geometry was defined
     * using GeometryArray.COLOR_3, the 'w' value of the color will be set to 1.0.
     * @return an array of Point3d's for the primitive that was intersected
     */
    public Color4f[] getPrimitiveColors() {
// 	System.out.println("PI.getPrimitiveColors " + primitiveColors);	
	GeometryArray geom = (GeometryArray) geometry;

	if (hasColors && (primitiveColors == null)) {
	    primitiveColors = new Color4f[primitiveVertexIndices.length];
	    int[] indices = getPrimitiveColorIndices();
	    int vformat = geom.getVertexFormat();
	    if ((vformat & GeometryArray.BY_REFERENCE) == 0) {
		if ((vformat & GeometryArray.COLOR_4) == 
		    GeometryArray.COLOR_4) {
		    for (int i = 0; i < indices.length; i++) {
			primitiveColors[i] = new Color4f();
			geom.getColor(indices[i], primitiveColors[i]);
		    }
		} else {
		    Color3f color = new Color3f();
		    for (int i = 0; i < indices.length; i++) {
			primitiveColors[i] = new Color4f();
			geom.getColor(indices[i], color);
			primitiveColors[i].x = color.x;
			primitiveColors[i].y = color.y;
			primitiveColors[i].z = color.z;
			primitiveColors[i].w = 1.0f;
		    }
		}
	    }
	    else {
		if ((vformat & GeometryArray.INTERLEAVED) == 0) {
		    float[] floatData = geom.getColorRefFloat();
		    // If data was set as float then ..
		    if (floatData == null) {
			byte[] byteData = geom.getColorRefByte();
			if (byteData == null) {
			    throw new UnsupportedOperationException("Deprecated : BY_REF - c3b and c3f");
			}
			else {
			    // Could be color3 or color4
			    int val;
			    if ((vformat & GeometryArray.COLOR_4) == 
				GeometryArray.COLOR_4) {
				for (int i = 0; i < indices.length; i++) {
				    val = indices[i] << 2; // for color4f
				    primitiveColors[i] = new Color4f(byteData[val],
								     byteData[val+1],
								     byteData[val+2],
								     byteData[val+3]);

				}
			    }
			    else {
				for (int i = 0; i < indices.length; i++) {
				    val = indices[i] * 3; // for color3f
				    primitiveColors[i] = new Color4f(byteData[val],
								     byteData[val+1],
								     byteData[val+2], 
								     1.0f);

				}
			    }
			}
		    }
		    else {
			// Could be color3 or color4
			int val;
			if ((vformat & GeometryArray.COLOR_4) == 
			    GeometryArray.COLOR_4) {
			    for (int i = 0; i < indices.length; i++) {
				val = indices[i] << 2; // for color4f
				primitiveColors[i] = new Color4f(floatData[val],
								 floatData[val+1],
								 floatData[val+2],
								 floatData[val+3]);
			    }
			}
			else {
			    for (int i = 0; i < indices.length; i++) {
				val = indices[i] * 3; // for color3f
				primitiveColors[i] = new Color4f(floatData[val],
								 floatData[val+1],
								 floatData[val+2],
								 1.0f);

			    }
			}
		    }

		}
		else {
		    float[] floatData = geom.getInterleavedVertices();
		    int offset = getInterleavedColorOffset(geom);
		    int stride = getInterleavedStride(geom); 
		    for (int i = 0; i < indices.length; i++) {
			int val = stride * indices[i]+offset;
			if ((vformat & GeometryArray.COLOR_4) == 
			    GeometryArray.COLOR_4) {
			    primitiveColors[i] = new Color4f(floatData[val],
							     floatData[val+1],
							     floatData[val+2],
							     floatData[val+3]);
			}
			else {
			    primitiveColors[i] = new Color4f(floatData[val],
							     floatData[val+1],
							     floatData[val+2],
							     1.0f);
			}
		    }
		}
	    }
	}
	return primitiveColors;
    }

    /** 
     * Get the coordinates indices for the intersected primitive.  For a non-indexed
     * primitive, this will be the same as the primitive vertex indices
     * @return an array indices
     */
    public int[] getPrimitiveCoordinateIndices() {
	if (primitiveCoordinateIndices == null) {
	    if (geometryIsIndexed()) {
		primitiveCoordinateIndices = 
		    new int[primitiveVertexIndices.length];
		for (int i = 0; i < primitiveVertexIndices.length; i++) {
		    primitiveCoordinateIndices[i] =  
			((IndexedGeometryArray)(geometry)).getCoordinateIndex(primitiveVertexIndices[i]);
		}
	    } else {
		primitiveCoordinateIndices = primitiveVertexIndices;
	    }
	}
	return primitiveCoordinateIndices;
    }

    /** 
     * Get the local coordinates intersected primitive 
     * @return an array of Point3d's for the primitive that was intersected
     */
    public Point3d[] getPrimitiveCoordinates() {
// 	System.out.println("PI.getPrimitiveCoordinates " + primitiveCoordinates);	
	GeometryArray geom = (GeometryArray) geometry;

	if (primitiveCoordinates == null) {
	    primitiveCoordinates = new Point3d[primitiveVertexIndices.length];
	    int[] indices = getPrimitiveCoordinateIndices();
	    int vformat = geom.getVertexFormat();
	    int val;
	    
// 	    System.out.println("---- indices.length - " + indices.length);
	    if ((vformat & GeometryArray.BY_REFERENCE) == 0) {
		for (int i = 0; i < indices.length; i++) {
		    primitiveCoordinates[i] = new Point3d();
		    // System.out.println("PickIntersection : indices["+i+"] = " + indices[i]);
		    geom.getCoordinate(indices[i], primitiveCoordinates[i]);
		}
	    }
	    else {
		if ((vformat & GeometryArray.INTERLEAVED) == 0) {
		    double[] doubleData = geom.getCoordRefDouble();
		    // If data was set as float then ..
		    if (doubleData == null) {
			float[] floatData = geom.getCoordRefFloat();
			if (floatData == null) {
			    throw new UnsupportedOperationException("Deprecated : BY_REF - c3f and c3d");		
			}
			else {
			    for (int i = 0; i < indices.length; i++) {
				val = indices[i] * 3;
				primitiveCoordinates[i] = new Point3d(floatData[val], 
								      floatData[val+1], 
								      floatData[val+2]);
			    }
			}
		    }
		    else {
			for (int i = 0; i < indices.length; i++) {
			    val = indices[i] * 3;
			    primitiveCoordinates[i] = new Point3d(doubleData[val], 
								  doubleData[val+1], 
								  doubleData[val+2]);
			}			
		    }
		}
		else {
		    float[] floatData = geom.getInterleavedVertices();
		    int offset = getInterleavedVertexOffset(geom);
		    int stride = offset + 3; // for the vertices .
		    for (int i = 0; i < indices.length; i++) {
			val = stride * indices[i]+offset;
			primitiveCoordinates[i] = new Point3d(floatData[val], 
							      floatData[val+1], 
							      floatData[val+2]);
		    }
		}
	    }

	}
	return primitiveCoordinates;
    }    

    /** 
     * Get VWorld coordinates of the intersected primitive 
     * @return an array of Point3d's for the primitive that was picked
     */
    public Point3d[] getPrimitiveCoordinatesVW() {

// 	System.out.println("PI.getPrimitiveCoordinatesVW " + primitiveCoordinatesVW);	

	if (primitiveCoordinatesVW == null) {
	    // We need to call getPrimitiveCoordinates first.
	    Point3d[] coords = getPrimitiveCoordinates();

	    primitiveCoordinatesVW = new Point3d[coords.length];
	    for (int i = 0; i < coords.length; i++) {
		primitiveCoordinatesVW[i] = new Point3d();

		primitiveCoordinatesVW[i].x = coords[i].x;
		primitiveCoordinatesVW[i].y = coords[i].y;
		primitiveCoordinatesVW[i].z = coords[i].z;

		l2vw.transform(primitiveCoordinatesVW[i]);
	    }
	}
	return primitiveCoordinatesVW;
    }
    
    /** 
     * Get the normal indices for the intersected primitive.  For a non-indexed
     * primitive, this will be the same as the primitive vertex indices
     * If the geometry array does not contain normals this will return null
     *	@return an array indices
     */
    public int[] getPrimitiveNormalIndices() {
	if (hasNormals && (primitiveNormalIndices == null)) {
	    if (geometryIsIndexed()) {
		primitiveNormalIndices = 
		    new int[primitiveVertexIndices.length];
		for (int i = 0; i < primitiveVertexIndices.length; i++) {
		    primitiveNormalIndices[i] = 
			((IndexedGeometryArray)(geometry)).getNormalIndex(primitiveVertexIndices[i]);
		}
	    } else {
		primitiveNormalIndices = primitiveVertexIndices;
	    }
	}
	return primitiveNormalIndices;
    }

    /** 
     * Get the normals of the intersected primitive.  This will return null if
     * the primitive does not contain normals.
     * @return an array of Point3d's for the primitive that was intersected
     */
    public Vector3f[] getPrimitiveNormals() {
// 	System.out.println("PI.getPrimitiveNormals " + primitiveNormals);	
	GeometryArray geom = (GeometryArray) geometry;

	if (hasNormals && (primitiveNormals == null)) {
	    primitiveNormals = new Vector3f[primitiveVertexIndices.length];
	    int[] indices = getPrimitiveNormalIndices();
	    int vformat = geom.getVertexFormat();
	    int val;

	    if ((vformat & GeometryArray.BY_REFERENCE) == 0) {
		for (int i = 0; i < indices.length; i++) {
		    primitiveNormals[i] = new Vector3f();
		    geom.getNormal(indices[i], primitiveNormals[i]);
		}
	    }
	    else {
		if ((vformat & GeometryArray.INTERLEAVED) == 0) {
		    float[] floatNormals = geom.getNormalRefFloat();
		    if (floatNormals != null) {
			for (int i = 0; i < indices.length; i++) {
			    val = indices[i] * 3;
			    primitiveNormals[i] = new Vector3f(floatNormals[val],
							       floatNormals[val+1],
							       floatNormals[val+2]);
			}
		    }
		    else {
			throw new UnsupportedOperationException("Deprecated : BY_REF - n3f");
		    }
		}
		else {
		    float[] floatData = geom.getInterleavedVertices();
		    int offset = getInterleavedColorOffset(geom);
		    int stride = getInterleavedStride(geom);
		    for (int i = 0; i < indices.length; i++) {
			val = stride * indices[i]+offset;
			primitiveNormals[i] = new Vector3f(floatData[val],floatData[val+1],floatData[val+2]);

		    }
		}
	    }
	}
	return primitiveNormals;
    }


    /** 
     * Get the texture coordinate indices for the intersected primitive at the specifed 
     * index in the specified texture coordinate set.  For a   non-indexed
     * primitive, this will be the same as the primitive vertex indices
     * If the geometry array does not contain texture coordinates, this will 
     * return null.
     * @return an array indices
     */
    public int[] getPrimitiveTexCoordIndices(int index) {
	if (hasTexCoords && (primitiveTexCoordIndices == null)) {
	    if (geometryIsIndexed()) {
		primitiveTexCoordIndices = 
		    new int[primitiveVertexIndices.length];
		for (int i = 0; i < primitiveVertexIndices.length; i++) {
		    primitiveTexCoordIndices[i] =  
			((IndexedGeometryArray)(geometry)).getTextureCoordinateIndex(index, primitiveVertexIndices[i]);
		}
	    } else {
		primitiveTexCoordIndices = primitiveVertexIndices;
	    }
	}
	return primitiveTexCoordIndices;
    }

    /** 
     * Get the texture coordinates of the intersected primitive at the specifed 
     * index in the specified texture coordinate set.
     * null if the primitive does not contain texture coordinates.  
     * If the geometry was defined
     * using GeometryArray.TEXTURE_COORDINATE_2, the 'z' value of the texture
     * coordinate will be set to 0.0.
     * @return an array of TexCoord3f's for the primitive that was intersected
     */
    public TexCoord3f[] getPrimitiveTexCoords (int index) {
// 	System.out.println("PI.getPrimitiveTexCoords " + primitiveTexCoords);	
	GeometryArray geom = (GeometryArray) geometry;

	if (primitiveTexCoords == null) {
	    primitiveTexCoords = new TexCoord3f[primitiveVertexIndices.length];
	    int[] indices = getPrimitiveTexCoordIndices(index);
	    int vformat = geom.getVertexFormat();
	    if ((vformat & GeometryArray.BY_REFERENCE) == 0) {
		for (int i = 0; i < indices.length; i++) {
		    primitiveTexCoords[i] = new TexCoord3f();
		    geom.getTextureCoordinate(index, indices[i], primitiveTexCoords[i]);
		}
	    }
	    else {
		if ((vformat & GeometryArray.INTERLEAVED) == 0) {
		    int val;
		    float[] floatTexCoords = geom.getTexCoordRefFloat(index);
		    if (floatTexCoords != null) {
			if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) == GeometryArray.TEXTURE_COORDINATE_2) {
			    for (int i = 0; i < indices.length; i++) {
				val = indices[i] << 1; // t2f
				primitiveTexCoords[i] = new TexCoord3f(floatTexCoords[val],
								       floatTexCoords[val+1],
								       0.0f);
			    }
			}
			else {
			    for (int i = 0; i < indices.length; i++) {
				val = indices[i] * 3; // t3f
				primitiveTexCoords[i] = new TexCoord3f(floatTexCoords[val],
								       floatTexCoords[val+1],
								       floatTexCoords[val+2]);
			    }
			}
		    }
		    else {
			throw new UnsupportedOperationException("Deprecated : BY_REF - t2f and t3f");
		    }
		}
		else {
		    float[] floatData = geom.getInterleavedVertices();
		    int stride = getInterleavedStride(geom);
		    int offset;
		    // Get the correct tex coord set
		    if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) ==
			GeometryArray.TEXTURE_COORDINATE_2) {
			offset = index << 1;
		    }
		    else {
			offset = index * 3;
		    }
		    for (int i = 0; i < indices.length; i++) {
			int val = stride * indices[i];
			if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) == 
			    GeometryArray.TEXTURE_COORDINATE_2) {
			    primitiveTexCoords[i] = 
				new TexCoord3f(floatData[val+offset],
					       floatData[val+1+offset],
					       0.0f);
			}
			else {
			    primitiveTexCoords[i] = 
				new TexCoord3f(floatData[val+offset],
					       floatData[val+1+offset],
					       floatData[val+2+offset]);
			}
		    }
		}
	    }
	}
	return primitiveTexCoords;
    }

    /** 
     * Get vertex indices of the intersected primitive
     * @return an array which contains the list of indices
     */
    public int [] getPrimitiveVertexIndices() {
	return primitiveVertexIndices;
    }

    /**
     * Gets the IntersectionInfo this intersection is part of.
     */
    public PickInfo.IntersectionInfo getIntersectionInfo() {
	return iInfo;
    }

    /**
     * String representation of this object
     */ 
    public String toString() {
	String rt = new String ("PickIntersection: ");
	rt += " IntersectionInfo = "+ iInfo + "\n";
	rt += " geometry = "+ geometry + "\n";
	if (distance != -1) rt += " dist:"+distance + "\n";
	if (pointCoordinates != null) rt += " pt:" + pointCoordinates + "\n";
	if (pointCoordinatesVW != null) rt += " ptVW:" + pointCoordinatesVW + "\n";

	if (primitiveCoordinateIndices != null) {
	    rt += " prim coordinate ind:" + "\n";
	    for (int i=0;i<primitiveCoordinateIndices.length;i++) {
		rt += " "+primitiveCoordinateIndices[i] + "\n";
	    }
	}

	if (primitiveColorIndices != null) {
	    rt += " prim color ind:" + "\n";
	    for (int i=0;i<primitiveColorIndices.length;i++) {
		rt += " "+primitiveColorIndices[i] + "\n";
	    }
	}

	if (primitiveNormalIndices != null) {
	    rt += " prim normal ind:" + "\n";
	    for (int i=0;i<primitiveNormalIndices.length;i++) {
		rt += " "+primitiveNormalIndices[i] + "\n";
	    }
	}

	if (primitiveTexCoordIndices != null) {
	    rt += " prim texture ind:" + "\n";
	    for (int i=0;i<primitiveTexCoordIndices.length;i++) {
		rt += " "+primitiveTexCoordIndices[i] + "\n";
	    }
	}
    
	if (closestVertexCoordinates != null) {
	    rt += " clos. vert:" + closestVertexCoordinates + "\n";
	}

	if (closestVertexCoordinatesVW != null) {
	    rt += " clos. vert:" + closestVertexCoordinatesVW + "\n";
	}

	if (closestVertexIndex != -1) {
	    rt += " clos. vert. ind.:" + closestVertexIndex + "\n";
	}
	return rt;
    }



    /******************** Helper methods ***************************************/

    int getInterleavedVertexOffset(GeometryArray geo) {
	int offset = 0;
	int vformat = geo.getVertexFormat();
	if ((vformat & GeometryArray.COLOR_3) == GeometryArray.COLOR_3) {
	    offset += 3;
	} else if ((vformat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4){
	    offset += 4;
	}
	if ((vformat & GeometryArray.NORMALS) != 0)
	    offset += 3;
	if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) == GeometryArray.TEXTURE_COORDINATE_2) {
	    offset += 2 *  geo.getTexCoordSetCount();
	}
	else if ((vformat & GeometryArray.TEXTURE_COORDINATE_3) == GeometryArray.TEXTURE_COORDINATE_3) {
	    offset += 3 * geo.getTexCoordSetCount();
	}

	return offset;
    }

    int getInterleavedStride(GeometryArray geo) {
	int offset = 3; // Add 3 for vertices
	int vformat = geo.getVertexFormat();
	if ((vformat & GeometryArray.COLOR_3) == GeometryArray.COLOR_3) {
	    offset += 3;
	} else if ((vformat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4){
	    offset += 4;
	}
	if ((vformat & GeometryArray.NORMALS) != 0)
	    offset += 3;
	if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) == GeometryArray.TEXTURE_COORDINATE_2) {
	    offset += 2 * geo.getTexCoordSetCount();
	}
	else if ((vformat & GeometryArray.TEXTURE_COORDINATE_3) == GeometryArray.TEXTURE_COORDINATE_3) {
	    offset += 3 * geo.getTexCoordSetCount();
	}
	
	return offset;
    }

    int getInterleavedColorOffset(GeometryArray geo) {
	int offset = 0;
	int vformat = geo.getVertexFormat();
	if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) == GeometryArray.TEXTURE_COORDINATE_2) {
	    offset += 2 * geo.getTexCoordSetCount();
	}
	else if ((vformat & GeometryArray.TEXTURE_COORDINATE_3) == GeometryArray.TEXTURE_COORDINATE_3) {
	    offset += 3 * geo.getTexCoordSetCount();
	}

	return offset;
    }


    /* ================================================================== */
    /*      Utility code for interpolating intersection point data        */
    /* ================================================================== */

    /* absolute value */
    double 
    abs(double value) {
	if (value < 0.0) {
	    return -value;
	} else {
	    return value;
	}
    }

    /* return the axis corresponding to the largest component of delta */
    int 
    maxAxis(Vector3d delta) {
	int axis = X_AXIS;
	double max = abs(delta.x);
	if (abs(delta.y) > max) {
	    axis = Y_AXIS;
	    max = abs(delta.y);
	}
	if (abs(delta.z) > max) {
	    axis = Z_AXIS;
	}
	return axis;
    }

    /* Triangle interpolation. Basic idea:  
     * Map the verticies of the triangle to the form:
     *
     * L--------R
     *  \      /
     * IL+--P-+IR
     *    \  /
     *    Base
     *
       where P is the intersection point Base, L and R and the triangle
       points.  IL and IR are the projections if P along the Base-L and Base-R
       edges using an axis:

       IL =  leftFactor * L + (1- leftFactor) * Base
       IR = rightFactor * R + (1-rightFactor) * Base

       then find the interp factor, midFactor, for P between IL and IR.  If 
       this is outside the range 0->1 then we have the wrong triangle of a 
       quad and we return false.  
    
       Else, the weighting is:

       IP = midFactor * IL + (1 - midFactor) * IR;

       Solving for weights for the formula:
       IP = BaseWeight * Base + LeftWeight * L + RightWeight * R;
       We get:
       BaseWeight = 1 - midFactor * leftFactor 
       - rightFactor + midFactor * rightFactor;
       LeftWeight = midFactor * leftFactor;
       RightWeight = righFactor - midFactor * rightFactor;
       As a check, note that the sum of the weights is 1.0.
       */

    boolean
    interpTriangle(int index0, int index1, int index2, Point3d[] coords, 
		   Point3d intPt) {

	// find the longest edge, we'll use that to pick the axis */
	Vector3d delta0 = new Vector3d();
	Vector3d delta1 = new Vector3d();
	Vector3d delta2 = new Vector3d();
	delta0.sub(coords[index1], coords[index0]);
	delta1.sub(coords[index2], coords[index0]);
	delta2.sub(coords[index2], coords[index1]);
	double len0 = delta0.lengthSquared();
	double len1 = delta1.lengthSquared();
	double len2 = delta2.lengthSquared();
	Vector3d longest = delta0;
	double maxLen = len0;
	if (len1 > maxLen) {
	    longest = delta1;
	    maxLen = len1;
	}
	if (len2 > maxLen) {
	    longest = delta2;
	}
	int mainAxis = maxAxis(longest);

	/*
	  System.out.println("index0 = " + index0 + " index1 = " + index1 +
	  " index2 = " + index2);
	  
	  System.out.println("coords[index0] = " + coords[index0]);
	  System.out.println("coords[index1] = " + coords[index1]);
	  System.out.println("coords[index2] = " + coords[index2]);
	  System.out.println("intPt = " + intPt);
	  
	  System.out.println("delta0 = " + delta0 + " len0 " + len0);
	  System.out.println("delta1 = " + delta1 + " len1 " + len1);
	  System.out.println("delta2 = " + delta2 + " len2 " + len2);
	  */
	
	/* now project the intersection point along the axis onto the edges */
	double[] factor = new double[3]; 
	/* the factor is for the projection opposide the vertex 0 = 1->2, etc*/
	factor[0] = 
	    getInterpFactorForBase(intPt, coords[index1], coords[index2], mainAxis);
	factor[1] = 
	    getInterpFactorForBase(intPt, coords[index2], coords[index0], mainAxis);
	factor[2] = 
	    getInterpFactorForBase(intPt, coords[index0], coords[index1], mainAxis);
	
	if (debug) {
	    System.out.println("intPt  = " + intPt);
	    switch(mainAxis) {
	    case X_AXIS:
		System.out.println("mainAxis = X_AXIS");
		break;
	    case Y_AXIS:
		System.out.println("mainAxis = Y_AXIS");
		break;
	    case Z_AXIS:
		System.out.println("mainAxis = Z_AXIS");
		break;
	    }
	    System.out.println("factor[0] =  " + factor[0]);
	    System.out.println("factor[1] =  " + factor[1]);
	    System.out.println("factor[2] =  " + factor[2]);
	}

	/* Find the factor that is out of range, it will tell us which
	 * vertex to use for base
	 */
	int base, left, right;
	double leftFactor, rightFactor;
	if ((factor[0] < 0.0) || (factor[0] > 1.0)) {
	    base  = index0;
	    right = index1;
	    left  = index2;
	    rightFactor = factor[2];
	    leftFactor  = 1.0 - factor[1];
	    if (debug) {
		System.out.println("base 0, rightFactor = " + rightFactor +
				   " leftFactor = " + leftFactor);
	    }
	} else if ((factor[1] < 0.0) || (factor[1] > 1.0)) {
	    base  = index1;
	    right = index2;
	    left  = index0;
	    rightFactor = factor[0];
	    leftFactor  = 1.0 - factor[2];
	    if (debug) {
		System.out.println("base 1, rightFactor = " + rightFactor +
				   " leftFactor = " + leftFactor);
	    }
	} else {
	    base  = index2;
	    right = index0;
	    left  = index1;
	    rightFactor = factor[1];
	    leftFactor  = 1.0 - factor[0];
	    if (debug) {
		System.out.println("base 2, rightFactor = " + rightFactor +
				   " leftFactor = " + leftFactor);
	    }
	}
	if (debug) {
	    System.out.println("base  = " + coords[base]);
	    System.out.println("left  = " + coords[left]);
	    System.out.println("right = " + coords[right]);
	}
	/* find iLeft and iRight */
	Point3d iLeft = new Point3d(leftFactor * coords[left].x +
				    (1.0-leftFactor)*coords[base].x,
				    leftFactor * coords[left].y +
				    (1.0-leftFactor)*coords[base].y,
				    leftFactor * coords[left].z +
				    (1.0-leftFactor)*coords[base].z);

	Point3d iRight = new Point3d(rightFactor * coords[right].x +
				     (1.0-rightFactor)*coords[base].x,
				     rightFactor * coords[right].y +
				     (1.0-rightFactor)*coords[base].y,
				     rightFactor * coords[right].z +
				     (1.0-rightFactor)*coords[base].z);

	if (debug) {
	    System.out.println("iLeft  = " + iLeft);
	    System.out.println("iRight  = " + iRight);
	}

	/* now find an axis and solve for midFactor */
	delta0.sub(iLeft, iRight);
	int midAxis = maxAxis(delta0);
	double midFactor = getInterpFactor(intPt, iRight, iLeft, midAxis);

	if (debug) {
	    switch(midAxis) {
	    case X_AXIS:
		System.out.println("midAxis = X_AXIS");
		break;
	    case Y_AXIS:
		System.out.println("midAxis = Y_AXIS");
		break;
	    case Z_AXIS:
		System.out.println("midAxis = Z_AXIS");
		break;
	    }
	    System.out.println("midFactor = " + midFactor);
	}

	if (midFactor < 0.0) {
	    // System.out.println("midFactor = " + midFactor);
	    if ((midFactor + TOL) >= 0.0) {
		// System.out.println("In Tol case : midFactor = " + midFactor);
		midFactor = 0.0;
	    }
	    else {
		/* int point is outside triangle */
		return false;
	    }
	}
	else if (midFactor > 1.0) {
	    // System.out.println("midFactor = " + midFactor);
	    if ((midFactor-TOL) <= 1.0) {
		// System.out.println("In Tol case : midFactor = " + midFactor);
		midFactor = 1.0;
	    }
	    else {
		/* int point is outside triangle */
		return false;
	    }
	}

	// Assign the weights
	interpWeights[base]  = 1.0 - midFactor * leftFactor -
	    rightFactor + midFactor * rightFactor;
	interpWeights[left]  = midFactor * leftFactor;
	interpWeights[right] = rightFactor - midFactor * rightFactor;
	return true;

    }

    /* Get the interpolation weights for each of the verticies of the 
     * primitive.
     */
    double[] getInterpWeights() {

	Point3d 	pt = getPointCoordinates();
	Point3d[] 	coordinates = getPrimitiveCoordinates();
	double 		factor;
	int		axis;

	if (interpWeights != null) {
	    return interpWeights;
	}

	interpWeights = new double[coordinates.length];

	// Interpolate
	switch (coordinates.length) {
	case 1:
	    // Nothing to interpolate
	    interpWeights[0] = 1.0;
	    break;
	case 2: // edge
	    Vector3d delta = new Vector3d();
	    delta.sub (coordinates[1], coordinates[0]);
	    axis = maxAxis(delta);
	    factor = getInterpFactor (pt, coordinates[1], coordinates[0], axis);
	    interpWeights[0] = factor;
	    interpWeights[1] = 1.0 - factor;
	    break;
	case 3: // triangle
	    if (!interpTriangle(0, 1, 2, coordinates, pt)) {
		throw new RuntimeException ("Interp point outside triangle");
	    } 
	    break;
	case 4: // quad
	    if (!interpTriangle(0, 1, 2, coordinates, pt)) {
		if (!interpTriangle(0, 2, 3, coordinates, pt)) {
		    throw new RuntimeException ("Interp point outside quad");
		}
	    }
	    break;
	default:
	    throw new RuntimeException ("Unexpected number of points.");
	}
	return interpWeights;
    }

    /** 
      Calculate the interpolation factor for point p by projecting it along
      an axis (x,y,z) onto the edge between p1 and p2.  If the result is 
      in the 0->1 range, point is between p1 and p2 (0 = point is at p1, 
      1 => point is at p2).
      */
    private static float getInterpFactor (Point3d p, Point3d p1, Point3d p2,
					  int axis) {
	float t;
	switch (axis) {
	case X_AXIS:
	    if (p1.x == p2.x)
		//t = Float.MAX_VALUE; // TODO: should be 0?
		t = 0.0f;
	    else
		t = (float) ((p1.x - p.x) / (p1.x - p2.x));
	    break;
	case Y_AXIS:
	    if (p1.y == p2.y)
		// t = Float.MAX_VALUE;
		t = 0.0f;
	    else
		t = (float) ((p1.y - p.y) / (p1.y - p2.y));
	    break;
	case Z_AXIS:
	    if (p1.z == p2.z)
		// t = Float.MAX_VALUE;
		t = 0.0f;
	    else
		t = (float)((p1.z - p.z) / (p1.z - p2.z));
	    break;
	default:
	    throw new RuntimeException ("invalid axis parameter "+axis+" (must be 0-2)"); 
	}
	return t;
    }

    /** 
      Calculate the interpolation factor for point p by projecting it along
      an axis (x,y,z) onto the edge between p1 and p2.  If the result is 
      in the 0->1 range, point is between p1 and p2 (0 = point is at p1, 
      1 => point is at p2).
      return MAX_VALUE if component of vertices are the same.
      */
    private static float getInterpFactorForBase (Point3d p, Point3d p1, Point3d p2,
						 int axis) {
	float t;
	switch (axis) {
	case X_AXIS:
	    if (p1.x == p2.x)
		t = Float.MAX_VALUE;
	    else
		t = (float) ((p1.x - p.x) / (p1.x - p2.x));
	    break;
	case Y_AXIS:
	    if (p1.y == p2.y)
		t = Float.MAX_VALUE;
	    else
		t = (float) ((p1.y - p.y) / (p1.y - p2.y));
	    break;
	case Z_AXIS:
	    if (p1.z == p2.z)
		t = Float.MAX_VALUE;
	    else
		t = (float)((p1.z - p.z) / (p1.z - p2.z));
	    break;
	default:
	    throw new RuntimeException ("invalid axis parameter "+axis+" (must be 0-2)"); 
	}
	return t;
    }

    
} // PickIntersection
