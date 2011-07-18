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

package com.sun.j3d.utils.geometry;

import com.sun.j3d.utils.geometry.*;
import java.io.*;
import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.math.*;

/** 
 * GeomBuffer allows OpenGL-like input of geometry data. It outputs 
 * Java 3D geometry array objects. This utility is to simplify porting
 * of OpenGL programs to Java 3D. 
 *<p>
 *    Here is a sample code that use this utility to create some quads.
 *  <P><blockquote><pre>
 * 
 *     GeomBuffer gbuf = new GeomBuffer(100);
 *     gbuf.begin(GeomBuffer.QUADS);
 *
 *     for (int i = 0; i < 5; i++){
 *       gbuf.normal3d(0.0, 1.0, 0.0);
 *       gbuf.vertex3d(1.0, 1.0, 0.0);
 * 
 *       gbuf.normal3d(0.0, 1.0, 0.0);
 *       gbuf.vertex3d(0.0, 1.0, 0.0);
 * 
 *       gbuf.normal3d(0.0, 1.0, 0.0);
 *       gbuf.vertex3d(0.0, 0.0, 0.0);
 * 
 *       gbuf.normal3d(0.0, 1.0, 0.0);
 *       gbuf.vertex3d(1.0, 0.0, 0.0);
 *     }
 *     gbuf.end();
 *     Shape3D shape = new Shape3D(gbuf.getGeom(GeomBuffer.GENERATE_NORMALS));
 * </pre></blockquote>
 *     Notice, that you only need to specify some upperbound on the number of 
 *     points you'll use at the beginning (100 in this case). 
 * <p>
 *     Currently, you are limited to one primitive type per geom buffer. Future
 *     versions will add support for mixed primitive types.
 *
 **/

class GeomBuffer extends Object{

  //Supported Primitives
  static final int QUAD_STRIP = 0x01;
  static final int TRIANGLES = 0x02;
  static final int QUADS = 0x04;
  static final int TRIANGLE_FAN = 0x10;
  static final int TRIANGLE_STRIP = 0x20;  

  private int flags;

  Point3f[] pts = null;
  Vector3f[] normals = null;
  TexCoord2f[] tcoords = null;
  int currVertCnt;
  int currPrimCnt;
  int[] currPrimType = null,
    currPrimStartVertex = null,
    currPrimEndVertex = null;
  GeometryArray geometry;
  int numVerts = 0;
  int numTris = 0;
  int numTexUnit = 1;
  int texCoordSetMap[] = null;


  static final int debug = 0;

  /** Creates a geometry buffer of given number of vertices 
   * @param numVerts total number of vertices to allocate by this buffer. 
   *        This is an upper bound estimate.
   */
  GeomBuffer(int numVerts, int numTexUnit)
  {
    this.numTexUnit = numTexUnit;
    pts = new Point3f[numVerts];
    normals = new Vector3f[numVerts];
    tcoords = new TexCoord2f[numVerts];
    // max primitives is numV/3
    currPrimType = new int[numVerts/3];
    currPrimStartVertex = new int[numVerts/3];
    currPrimEndVertex = new int[numVerts/3];
    currVertCnt = 0;
    currPrimCnt = 0;

    texCoordSetMap = new int[numTexUnit];
    for (int i = 0; i < numTexUnit; i++) 
	texCoordSetMap[i] = 0;

  }

  GeomBuffer(int numVerts) 
  {
    this(numVerts, 1);
  }

  /*
   * Returns a Java 3D geometry array from the geometry buffer. You need to
   * call begin, vertex3d, end, etc. before calling this, of course.
   *
   * @param format vertex format.
   */
  
  GeometryArray getGeom(int format)
  {
    GeometryArray obj = null;
    flags = format;

    numTris = 0;

    //Switch based on first primitive.
    switch (currPrimType[0]){
    case TRIANGLES:
      obj = processTriangles();
      break;
    case QUADS:
      obj = processQuads();
      break;
    case QUAD_STRIP:
    case TRIANGLE_STRIP:
      obj = processQuadStrips();
      break;
    case TRIANGLE_FAN:
      obj = processTriangleFan();
      break;
    }
    if ((obj != null) && ((flags & Primitive.ENABLE_GEOMETRY_PICKING) != 0)) {
      obj.setCapability(Geometry.ALLOW_INTERSECT);
      obj.setCapability(GeometryArray.ALLOW_FORMAT_READ);
      obj.setCapability(GeometryArray.ALLOW_COUNT_READ);
      obj.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
    }
    return obj;
  }


  /** 
   * Begins a new primitive given the primitive type.
   *
   * @param prim the primitive type (listed above).
   *
   **/

  void begin(int prim)
  {
    if (debug >= 1) System.out.println("quad");
    currPrimType[currPrimCnt] = prim;
    currPrimStartVertex[currPrimCnt] = currVertCnt;
  }


  /** 
   * End of primitive.
   *
   *
   **/
  void end()
  {
    if (debug >= 1) System.out.println("end");
    currPrimEndVertex[currPrimCnt] = currVertCnt;
    currPrimCnt++;
  }
  
  void vertex3d(double x, double y, double z)
  {

    if (debug >= 2) System.out.println("v " + x + " " +
				       y + " " +  
				       z);
    pts[currVertCnt] = new Point3f((float)x, (float)y, (float)z);
    currVertCnt++;
  }
  
  void normal3d(double x, double y, double z)
  {
    if (debug >= 2) System.out.println("n " + x + " " +
				       y + " " + z);
    double sum = x*x+y*y+z*z;
    if (Math.abs(sum - 1.0) > 0.001){
      if (debug >= 2) System.out.println("normalizing");
      double root = Math.sqrt(sum);
      if (root > 0.000001) {
	x /= root; 
	y /= root; 
	z /= root;
      } else {
	y = z = 0.0; x = 1.0;
      }
    }
    normals[currVertCnt] = new Vector3f((float)x, (float)y, (float)z);
  }
  
  void texCoord2d(double s, double t)
  {
    if (debug >= 2) System.out.println("t " + 
				       s + " " +
				       t);
    tcoords[currVertCnt] = new TexCoord2f((float)s, (float)t);
  }
  
  // Return a reference to the texture coordinates of this geom buffer.
  TexCoord2f[] getTexCoords() {
      return tcoords;
  }
  
  /**
   * Returns the Java 3D geometry gotten from calling getGeom.
   *
   **/

  GeometryArray getComputedGeometry()
  {
    return geometry;
  }
    
  int getNumTris()
  { 
    return numTris;
  }

  int getNumVerts()
  { 
    return numVerts;
  }
  

  private GeometryArray processQuadStrips()
  {
    GeometryArray obj = null;
    int i;
    int totalVerts = 0;

    // Calculate how many vertices needed to hold all of the individual quads
    int stripCounts[] = new int[currPrimCnt];
    for (i = 0; i < currPrimCnt; i++){
      stripCounts[i] = currPrimEndVertex[i] - currPrimStartVertex[i];
      totalVerts += stripCounts[i];
    }

    if (debug >= 1) System.out.println("totalVerts " + totalVerts);

    int tsaFlags = TriangleStripArray.COORDINATES;
    if ((flags & Primitive.GENERATE_NORMALS) != 0)
	tsaFlags |= TriangleStripArray.NORMALS;
    if ((flags & Primitive.GENERATE_TEXTURE_COORDS) != 0)
	 tsaFlags |= TriangleStripArray.TEXTURE_COORDINATE_2;

    // Create GeometryArray to pass back
    obj = new TriangleStripArray(totalVerts, tsaFlags, 
			1, texCoordSetMap, stripCounts);

    // Allocate space to store new vertex info
    Point3f[] newpts = new Point3f[totalVerts];
    Vector3f[] newnormals = new Vector3f[totalVerts];
    TexCoord2f[] newtcoords = new TexCoord2f[totalVerts];
    int currVert = 0;
    
    // Repeat for each Quad Strip
    for (i = 0; i < currPrimCnt; i++){
      // Output order for these quad arrays same as java 3d triangle strips
      for (int j = currPrimStartVertex[i] ; j < currPrimEndVertex[i] ; j++){
	outVertex(newpts, newnormals, newtcoords, currVert++,
		  pts, normals, tcoords, j);
      }

    }

    numVerts = currVert;
    numTris += totalVerts - currPrimCnt * 2;

    obj.setCoordinates(0, newpts);
    if ((flags & Primitive.GENERATE_NORMALS) != 0)
      obj.setNormals(0, newnormals);
    if ((flags & Primitive.GENERATE_TEXTURE_COORDS) != 0)
      obj.setTextureCoordinates(0, 0, newtcoords);

    geometry = obj;
    return obj;
  }

  private GeometryArray processQuads()
  {
    GeometryArray obj = null;
    int i;
    int totalVerts = 0;

    for (i = 0; i < currPrimCnt; i++){
      totalVerts += currPrimEndVertex[i] - currPrimStartVertex[i];
    }

    if (debug >= 1) System.out.println("totalVerts " + totalVerts);

    if (((flags & Primitive.GENERATE_NORMALS) != 0) &&
	((flags & Primitive.GENERATE_TEXTURE_COORDS) != 0)){
      obj = new QuadArray(totalVerts,
			  QuadArray.COORDINATES | 
			  QuadArray.NORMALS |
			  QuadArray.TEXTURE_COORDINATE_2, 
			  1, texCoordSetMap);
    }
    else 
      if (((flags & Primitive.GENERATE_NORMALS) == 0) &&
	  ((flags & Primitive.GENERATE_TEXTURE_COORDS) != 0)){
	obj = new QuadArray(totalVerts,
			    QuadArray.COORDINATES | 
			    QuadArray.TEXTURE_COORDINATE_2, 
			    1, texCoordSetMap);
      }
    else 
      if (((flags & Primitive.GENERATE_NORMALS) != 0) &&
	  ((flags & Primitive.GENERATE_TEXTURE_COORDS) == 0)){
	obj = new QuadArray(totalVerts,
			    QuadArray.COORDINATES | 
			    QuadArray.NORMALS);
      }
      else {
	obj = new QuadArray(totalVerts,
			    QuadArray.COORDINATES);
      }

    Point3f[] newpts = new Point3f[totalVerts];
    Vector3f[] newnormals = new Vector3f[totalVerts];
    TexCoord2f[] newtcoords = new TexCoord2f[totalVerts];
    int currVert = 0;

    if (debug > 1) System.out.println("total prims " + currPrimCnt);

    for (i = 0; i < currPrimCnt; i++){
      if (debug > 1) System.out.println("start " + currPrimStartVertex[i] + 
					" end " + currPrimEndVertex[i]);
      for (int j = currPrimStartVertex[i]; j < currPrimEndVertex[i] - 3;j+=4){
	outVertex(newpts, newnormals, newtcoords, currVert++,
		  pts, normals, tcoords, j);
	outVertex(newpts, newnormals, newtcoords, currVert++,
		  pts, normals, tcoords, j + 1);
	outVertex(newpts, newnormals, newtcoords, currVert++,
		  pts, normals, tcoords, j + 2);
	outVertex(newpts, newnormals, newtcoords, currVert++,
		  pts, normals, tcoords, j + 3);
	numTris += 2;
      }
    }
    numVerts = currVert;

    obj.setCoordinates(0, newpts);
    if ((flags & Primitive.GENERATE_NORMALS) != 0)
      obj.setNormals(0, newnormals);
    if ((flags & Primitive.GENERATE_TEXTURE_COORDS) != 0)
      obj.setTextureCoordinates(0, 0, newtcoords);

    geometry = obj;
    return obj;
  }

  private GeometryArray processTriangles()
  {
    GeometryArray obj = null;
    int i;
    int totalVerts = 0;

    for (i = 0; i < currPrimCnt; i++){
      totalVerts += currPrimEndVertex[i] - currPrimStartVertex[i];
    }

    if (debug >= 1) System.out.println("totalVerts " + totalVerts);

    if (((flags & Primitive.GENERATE_NORMALS) != 0) &&
	((flags & Primitive.GENERATE_TEXTURE_COORDS) != 0)){
      obj = new TriangleArray(totalVerts,
			  TriangleArray.COORDINATES | 
			  TriangleArray.NORMALS |
			  TriangleArray.TEXTURE_COORDINATE_2,
			  1, texCoordSetMap);
    }
    else 
      if (((flags & Primitive.GENERATE_NORMALS) == 0) &&
	  ((flags & Primitive.GENERATE_TEXTURE_COORDS) != 0)){
	obj = new TriangleArray(totalVerts,
			    TriangleArray.COORDINATES | 
			    TriangleArray.TEXTURE_COORDINATE_2,
			  1, texCoordSetMap);
      }
    else 
      if (((flags & Primitive.GENERATE_NORMALS) != 0) &&
	  ((flags & Primitive.GENERATE_TEXTURE_COORDS) == 0)){
	obj = new TriangleArray(totalVerts,
			    TriangleArray.COORDINATES | 
			    TriangleArray.NORMALS);
      }
      else {
	obj = new TriangleArray(totalVerts,
			    TriangleArray.COORDINATES);
      }

    Point3f[] newpts = new Point3f[totalVerts];
    Vector3f[] newnormals = new Vector3f[totalVerts];
    TexCoord2f[] newtcoords = new TexCoord2f[totalVerts];
    int currVert = 0;
    
    for (i = 0; i < currPrimCnt; i++){
      for (int j = currPrimStartVertex[i]; j < currPrimEndVertex[i] - 2;j+=3){
	outVertex(newpts, newnormals, newtcoords, currVert++,
		  pts, normals, tcoords, j);
	outVertex(newpts, newnormals, newtcoords, currVert++,
		  pts, normals, tcoords, j + 1);
	outVertex(newpts, newnormals, newtcoords, currVert++,
		  pts, normals, tcoords, j + 2);
	numTris += 1;
      }
    }
    numVerts = currVert;

    obj.setCoordinates(0, newpts);
    if ((flags & Primitive.GENERATE_NORMALS) != 0)
      obj.setNormals(0, newnormals);
    if ((flags & Primitive.GENERATE_TEXTURE_COORDS) != 0)
      obj.setTextureCoordinates(0, 0, newtcoords);

    geometry = obj;
    return obj;
  }


  private GeometryArray processTriangleFan() {
    if (debug > 0) System.out.println("processTriangleFan");

    GeometryArray obj = null;
    int i;
    int totalVerts = 0;

    int stripCounts[] = new int[currPrimCnt];

    // figure out how many vertices we need to hold the individual fans
    for (i = 0; i < currPrimCnt; i++) {
      stripCounts[i] = currPrimEndVertex[i] - currPrimStartVertex[i];
      totalVerts += stripCounts[i];
    }

    // figure out what flags we need
    int tfFlags = TriangleFanArray.COORDINATES;
    if ((flags & Primitive.GENERATE_NORMALS) != 0) {
      tfFlags |= TriangleFanArray.NORMALS;
    }
    if ((flags & Primitive.GENERATE_TEXTURE_COORDS) != 0) {
      tfFlags |= TriangleFanArray.TEXTURE_COORDINATE_2;
    }

    // create the TriangleFanArray
    obj = new TriangleFanArray(totalVerts, tfFlags, 1, texCoordSetMap,
			       stripCounts);

    // allocate space for vertex info
    Point3f[] newpts = new Point3f[totalVerts];
    Vector3f[] newnormals = new Vector3f[totalVerts];
    TexCoord2f[] newtcoords = new TexCoord2f[totalVerts];

    int currVert = 0;

    // repeat for each fan
    for (i = 0; i < currPrimCnt; i++) {
      for (int j = currPrimStartVertex[i]; j < currPrimEndVertex[i]; j++) {
	outVertex(newpts, newnormals, newtcoords, currVert++, pts, 
		  normals, tcoords, j);
      }
    }

    for (i = 0; i < newpts.length; i++) {
      if (debug > 1) System.out.println("i = " + i + " " + newpts[i]);
    }

    numVerts = currVert;
    numTris = totalVerts - currPrimCnt * 2;    

    // set the coordinates on the GeometryArray
    obj.setCoordinates(0, newpts);

    // set the normals and tex coords if necessary
    if ((flags & Primitive.GENERATE_NORMALS) != 0) {
      obj.setNormals(0, newnormals);
    }
    if ((flags & Primitive.GENERATE_TEXTURE_COORDS) != 0) {
      obj.setTextureCoordinates(0, 0, newtcoords);
    }
    geometry = obj;
    return obj;
  }



  void outVertex(Point3f[] dpts, Vector3f[] dnormals, TexCoord2f[] dtcoords,
		 int dloc,
		 Point3f[] spts, Vector3f[] snormals, TexCoord2f[] stcoords,
		 int sloc)
  {
    if (debug >= 1) System.out.println("v " + spts[sloc].x + " " +
				       spts[sloc].y + " " + 
				       spts[sloc].z);

    // PSP: Do we really need new points here?

    dpts[dloc] = new Point3f(spts[sloc]);

    if ((flags & Primitive.GENERATE_NORMALS) != 0){
      dnormals[dloc] = new Vector3f(snormals[sloc]);
    }
    if ((flags & Primitive.GENERATE_TEXTURE_COORDS) != 0){
      if (debug >= 2) System.out.println("final out tcoord");
      dtcoords[dloc] = new TexCoord2f(stcoords[sloc]);
    }
  }
}

