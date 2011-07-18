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

package com.sun.j3d.utils.behaviors.sensor ;

import javax.media.j3d.Shape3D ;
import javax.media.j3d.Material ;
import javax.media.j3d.Appearance ;
import javax.media.j3d.Transform3D ;
import javax.media.j3d.GeometryArray ;
import javax.media.j3d.TriangleStripArray ;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.AxisAngle4f ;
import javax.vecmath.Point3d ;
import javax.vecmath.Point3f ;
import javax.vecmath.Vector3f ;

/**
 * A Shape3D representing a beam pointing from the origin of a
 * sensor's local coordinate system to its hotspot.
 * 
 * @since Java 3D 1.3
 */
public class SensorBeamEcho extends Shape3D {
    /**
     * Creates a SensorBeamEcho.  Read and write capabilities are granted
     * for the Appearance, Material, TransparencyAttributes, and 
     * TransparencyAttributes mode and value.
     * 
     * @param hotspot location of the sensor's hotspot in the sensor's
     *  local coordinate system; this must not be (0, 0, 0)
     * @param baseWidth width of the beam in meters
     * @param enableLighting boolean indicating whether normals should be
     *  generated and lighting enabled
     * @exception IllegalArgumentException if hotspot is (0, 0, 0)
     */
    public SensorBeamEcho(Point3d hotspot, double baseWidth, 
			  boolean enableLighting) {
	super() ;

	if (hotspot.distance(new Point3d()) == 0.0)
	    throw new IllegalArgumentException
		("\nBeam echo can't have hotspot at origin") ;

	Vector3f axis = new Vector3f((float)hotspot.x,
				     (float)hotspot.y,
				     (float)hotspot.z) ;

	Vector3f axis1 = new Vector3f() ;
	axis1.normalize(axis) ;

	// Choose an arbitrary vector normal to the beam axis.
	Vector3f normal = new Vector3f(0.0f, 1.0f, 0.0f) ;
	normal.cross(axis1, normal) ;
	if (normal.lengthSquared() < 0.5f) {
	    normal.set(0.0f, 0.0f, 1.0f) ;
	    normal.cross(axis1, normal) ;
	}
	normal.normalize() ;

	// Create cap vertices and normals.
	int divisions = 18 ;
	Point3f[] cap0 = new Point3f[divisions] ;
	Point3f[] cap1 = new Point3f[divisions] ;
	Vector3f[] capNormals = new Vector3f[divisions] ;
	Vector3f cap0Normal = new Vector3f(axis1) ;
	Vector3f cap1Normal = new Vector3f(axis1) ;
	cap0Normal.negate() ;

	AxisAngle4f aa4f = new AxisAngle4f
	    (axis1, -(float)Math.PI/((float)divisions/2.0f)) ;
	Transform3D t3d = new Transform3D() ;
	t3d.set(aa4f) ;

	float halfWidth = (float)baseWidth / 2.0f ;
	for (int i = 0 ; i < divisions ; i++) {
	    capNormals[i] = new Vector3f(normal) ;
	    cap0[i] = new Point3f(normal) ;
	    cap0[i].scale(halfWidth) ;
	    cap1[i] = new Point3f(cap0[i]) ;
	    cap1[i].add(axis) ;
	    t3d.transform(normal) ;
	}

	// The beam cylinder is created with 3 triangle strips.  The first
	// strip contains the side facets (2 + 2*divisions vertices), and
	// the other two strips are the caps (divisions vertices each).
	int vertexCount = 2 + (4 * divisions) ;
	Point3f[] vertices = new Point3f[vertexCount] ;
	Vector3f[] normals = new Vector3f[vertexCount] ;

	// Side facets.
	for (int i = 0 ; i < divisions ; i++) {
	    vertices[i*2] = cap0[i] ;
	    vertices[(i*2) + 1] = cap1[i] ;

	    normals[i*2] = capNormals[i] ;
	    normals[(i*2) + 1] = capNormals[i] ;
	}

	vertices[divisions*2] = cap0[0] ;
	vertices[(divisions*2) + 1] = cap1[0] ;

	normals[divisions*2] = capNormals[0] ;
	normals[(divisions*2) + 1] = capNormals[0] ;

	// Strips for caps created by criss-crossing the interior.
	int v = (divisions+1) * 2 ;
	vertices[v] = cap0[0] ;
	normals[v++] = cap0Normal ;

	int j = 1 ;
	int k = divisions - 1 ;
	while (j <= k) {
	    vertices[v] = cap0[j++] ;
	    normals[v++] = cap0Normal ;
	    if (j > k) break ;
	    vertices[v] = cap0[k--] ;
	    normals[v++] = cap0Normal ;
	}

	vertices[v] = cap1[0] ;
	normals[v++] = cap1Normal ;

	j = 1 ;
	k = divisions - 1 ;
	while (j <= k) {
	    vertices[v] = cap1[k--] ;
	    normals[v++] = cap1Normal ;
	    if (j > k) break ;
	    vertices[v] = cap1[j++] ;
	    normals[v++] = cap1Normal ;
	}

	// Create the TriangleStripArray.
	int vertexFormat ;
	Material m = new Material() ;
	m.setCapability(Material.ALLOW_COMPONENT_READ) ;
	m.setCapability(Material.ALLOW_COMPONENT_WRITE) ;

	if (enableLighting) {
	    vertexFormat =
		GeometryArray.COORDINATES | GeometryArray.NORMALS ;
	    m.setLightingEnable(true) ;
	}
	else {
	    vertexFormat = GeometryArray.COORDINATES ;
	    m.setLightingEnable(false) ;
	}

	int[] stripCounts = new int[3] ;
	stripCounts[0] = 2 + (2 * divisions) ;
	stripCounts[1] = divisions ;
	stripCounts[2] = divisions ;

	TriangleStripArray tsa =
	    new TriangleStripArray(vertexCount,
				   vertexFormat, stripCounts) ;

	tsa.setCoordinates(0, vertices) ;
	if (enableLighting)
	    tsa.setNormals(0, normals) ;

	Appearance a = new Appearance() ;
	a.setMaterial(m) ;
	a.setCapability(Appearance.ALLOW_MATERIAL_READ) ;
	a.setCapability(Appearance.ALLOW_MATERIAL_WRITE) ;

	TransparencyAttributes ta = new TransparencyAttributes() ;
	ta.setCapability(TransparencyAttributes.ALLOW_MODE_READ) ;
	ta.setCapability(TransparencyAttributes.ALLOW_MODE_WRITE) ;
	ta.setCapability(TransparencyAttributes.ALLOW_VALUE_READ) ;
	ta.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE) ;
	ta.setCapability
	    (TransparencyAttributes.ALLOW_BLEND_FUNCTION_READ) ;
	ta.setCapability
	    (TransparencyAttributes.ALLOW_BLEND_FUNCTION_WRITE) ;

	a.setTransparencyAttributes(ta) ;
	a.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ) ;
	a.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE) ;

	setGeometry(tsa) ;
	setAppearance(a) ;

	setCapability(ALLOW_APPEARANCE_READ) ;
	setCapability(ALLOW_APPEARANCE_WRITE) ;
    }
}

