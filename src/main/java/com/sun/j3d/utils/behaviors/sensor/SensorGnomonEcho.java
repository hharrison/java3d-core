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
import javax.media.j3d.TriangleArray ;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point3f ;
import javax.vecmath.Vector3f ;

/**
 * A Shape3D representing a gnomon pointing along each coordinate
 * axis.  The base of the gnomon is a cube, and the coordinate axes are
 * represented by pyramids attached to each face of the cube.
 * 
 * @since Java 3D 1.3
 */
public class SensorGnomonEcho extends Shape3D {
    /**
     * Constructs a SensorGnomonEcho.  Read and write capabilities are
     * granted for the Appearance, Material, TransparencyAttributes,
     * and TransparencyAttributes mode and value.
     * 
     * @param transform translation and/or rotation to apply to the gnomon
     *  geometry; this should be the position and orientation of the sensor
     *  hotspot in the sensor's local coordinate system
     * @param baseWidth width of each edge of the base cube in meters
     * @param axisLength distance in meters from the gnomon center to
     *  the apex of the pyramid attached to each face of the base cube
     * @param enableLighting boolean indicating whether normals should be
     *  generated and lighting enabled
     */
    public SensorGnomonEcho(Transform3D transform,
			    double baseWidth,
			    double axisLength,
			    boolean enableLighting) {
	super() ;

	int FRONT  = 0 ;
	int BACK   = 1 ;
	int LEFT   = 2 ;
	int RIGHT  = 3 ;
	int TOP    = 4 ;
	int BOTTOM = 5 ;
	Point3f[] axes = new Point3f[6] ;
	float length = (float)axisLength ;

	axes[FRONT]  = new Point3f(0f, 0f,  length) ; 
	axes[BACK]   = new Point3f(0f, 0f, -length) ;
	axes[LEFT]   = new Point3f(-length, 0f, 0f) ;
	axes[RIGHT]  = new Point3f( length, 0f, 0f) ;
	axes[TOP]    = new Point3f(0f,  length, 0f) ;
	axes[BOTTOM] = new Point3f(0f, -length, 0f) ;

	if (transform != null)
	    for (int i = FRONT ; i <= BOTTOM ; i++)
		transform.transform(axes[i]) ;

	float offset = (float)baseWidth / 2.0f ;
	Point3f[][] cube = new Point3f[6][4] ;

	cube[FRONT][0]  = new Point3f(-offset, -offset,  offset) ;
	cube[FRONT][1]  = new Point3f( offset, -offset,  offset) ;
	cube[FRONT][2]  = new Point3f( offset,  offset,  offset) ;
	cube[FRONT][3]  = new Point3f(-offset,  offset,  offset) ;

	cube[BACK][0]   = new Point3f( offset, -offset, -offset) ;
	cube[BACK][1]   = new Point3f(-offset, -offset, -offset) ;
	cube[BACK][2]   = new Point3f(-offset,  offset, -offset) ;
	cube[BACK][3]   = new Point3f( offset,  offset, -offset) ;

	if (transform != null)
	    for (int i = FRONT ; i <= BACK ; i++)
		for (int j = 0 ; j < 4 ; j++)
		    transform.transform(cube[i][j]) ;

	cube[LEFT][0]   = cube[BACK][1] ;
	cube[LEFT][1]   = cube[FRONT][0] ;
	cube[LEFT][2]   = cube[FRONT][3] ;
	cube[LEFT][3]   = cube[BACK][2] ;

	cube[RIGHT][0]  = cube[FRONT][1] ;
	cube[RIGHT][1]  = cube[BACK][0] ;
	cube[RIGHT][2]  = cube[BACK][3] ;
	cube[RIGHT][3]  = cube[FRONT][2] ;

	cube[TOP][0]    = cube[FRONT][3] ;
	cube[TOP][1]    = cube[FRONT][2] ;
	cube[TOP][2]    = cube[BACK][3] ;
	cube[TOP][3]    = cube[BACK][2] ;

	cube[BOTTOM][0] = cube[BACK][1] ;
	cube[BOTTOM][1] = cube[BACK][0] ;
	cube[BOTTOM][2] = cube[FRONT][1] ;
	cube[BOTTOM][3] = cube[FRONT][0] ;

	int v = 0 ;
	Point3f[] vertices = new Point3f[72] ;

	for (int i = 0 ; i < 6 ; i++) {
	    vertices[v++] = cube[i][0] ;
	    vertices[v++] = cube[i][1] ;
	    vertices[v++] = axes[i] ;
	    vertices[v++] = cube[i][1] ;
	    vertices[v++] = cube[i][2] ;
	    vertices[v++] = axes[i] ;
	    vertices[v++] = cube[i][2] ;
	    vertices[v++] = cube[i][3] ;
	    vertices[v++] = axes[i] ;
	    vertices[v++] = cube[i][3] ;
	    vertices[v++] = cube[i][0] ;
	    vertices[v++] = axes[i] ;
	}
        
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

	TriangleArray ta = new TriangleArray(72, vertexFormat) ;
	ta.setCoordinates(0, vertices) ;

	if (enableLighting) {
	    Vector3f v0 = new Vector3f() ;
	    Vector3f v1 = new Vector3f() ;
	    Vector3f[] normals = new Vector3f[72] ;

	    for (int i = 0 ; i < 72 ; i += 3) {
		v0.sub(vertices[i+1], vertices[i]) ;
		v1.sub(vertices[i+2], vertices[i]) ;

		Vector3f n = new Vector3f() ;
		n.cross(v0, v1) ;
		n.normalize() ;

		normals[i] = n ;
		normals[i+1] = n ;
		normals[i+2] = n ;
	    }
	    ta.setNormals(0, normals) ;
	}

	Appearance a = new Appearance() ;
	a.setMaterial(m) ;
	a.setCapability(Appearance.ALLOW_MATERIAL_READ) ;
	a.setCapability(Appearance.ALLOW_MATERIAL_WRITE) ;

	TransparencyAttributes tra = new TransparencyAttributes() ;
	tra.setCapability(TransparencyAttributes.ALLOW_MODE_READ) ;
	tra.setCapability(TransparencyAttributes.ALLOW_MODE_WRITE) ;
	tra.setCapability(TransparencyAttributes.ALLOW_VALUE_READ) ;
	tra.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE) ;
	ta.setCapability
	    (TransparencyAttributes.ALLOW_BLEND_FUNCTION_READ) ;
	ta.setCapability
	    (TransparencyAttributes.ALLOW_BLEND_FUNCTION_WRITE) ;

	a.setTransparencyAttributes(tra) ;
	a.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ) ;
	a.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE) ;

	setGeometry(ta) ;
	setAppearance(a) ;

	setCapability(ALLOW_APPEARANCE_READ) ;
	setCapability(ALLOW_APPEARANCE_WRITE) ;
    }
}

