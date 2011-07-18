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

/**
 * Cone is a geometry primitive defined with a radius and a height.
 * It is a capped cone centered at the origin with its central axis
 * aligned along the Y-axis. The center of the cone is defined to be
 * the center of its bounding box (rather than its centroid).
 * <p>
 * If the GENERATE_TEXTURE_COORDS flag is set, the texture coordinates
 * are generated such that the texture gets mapped onto the cone similarly
 * to how it gets mapped onto a cylinder, the difference being the top
 * cap is nonexistent.
 * <p>
 * By default all primitives with the same parameters share their
 * geometry (e.g., you can have 50 shperes in your scene, but the
 * geometry is stored only once). A change to one primitive will
 * effect all shared nodes.  Another implication of this
 * implementation is that the capabilities of the geometry are shared,
 * and once one of the shared nodes is live, the capabilities cannot
 * be set.  Use the GEOMETRY_NOT_SHARED flag if you do not wish to
 * share geometry among primitives with the same parameters.
 */
public class Cone extends Primitive {
  float radius, height;
  int xdivisions, ydivisions;
  static final int MID_REZ_DIV_X = 15;
  static final int MID_REZ_DIV_Y = 1;

  /**
   * Designates the body of the cone.  Used by <code>getShape</code>.
   *
   * @see Cone#getShape
   */
  public static final int BODY = 0;

  /**
   * Designates the end-cap of the cone.  Used by <code>getShape</code>.
   *
   * @see Cone#getShape
   */
  public static final int CAP = 1;

  /**  
   *   Constructs a default Cone of radius of 1.0 and height
   *   of 2.0. Resolution defaults to 15 divisions along X and axis
   *   and 1 along the Y axis.  Normals are generated, texture 
   *   coordinates are not.
   */
  public Cone(){
    this(1.0f, 2.0f, GENERATE_NORMALS, MID_REZ_DIV_X, MID_REZ_DIV_Y, null);
  }

  /**
   *
   *   Constructs a default Cone of a given radius and height.  Normals
   *   are generated, texture coordinates are not.
   *   @param radius Radius
   *   @param height Height
   */
  public Cone (float radius, float height)
  {
    this(radius, height, GENERATE_NORMALS, MID_REZ_DIV_X, MID_REZ_DIV_Y, null);
  }

  /**
   *
   *   Constructs a default cone of a given radius, height,
   *   and appearance. Normals are generated, texture coordinates are not.
   *   @param radius Radius
   *   @param height Height
   *   @param ap Appearance
   *
   * @since Java 3D 1.2.1
   */
  public Cone (float radius, float height, Appearance ap)
  {
    this(radius, height, GENERATE_NORMALS, MID_REZ_DIV_X, MID_REZ_DIV_Y, ap);
  }

  /**
   *
   *   Constructs a default cone of a given radius, height,
   *   primitive flags, and appearance.
   *   @param radius Radius
   *   @param height Height
   *   @param primflags Primitive flags
   *   @param ap Appearance
   */
  public Cone (float radius, float height, int primflags, Appearance ap)
  {
    this(radius, height, primflags, MID_REZ_DIV_X, MID_REZ_DIV_Y, ap);
  }

  /**
   * Obtains the Shape3D node associated with one of the parts of the
   * cone (the body or the cap). This allows users to modify the appearance
   * or geometry of individual parts. 
   * @param partId The part to return (BODY or CAP).
   * @return The Shape3D object associated with the partId.  If an
   * invalid partId is passed in, null is returned.
   */
  public Shape3D getShape(int partId){
      if (partId > CAP || partId < BODY) return null;
      return (Shape3D)getChild(partId);
  }


  /**
   * Sets appearance of the cone. This will set each part of the
   *  cone (cap & body) to the same appearance. To set each
   *  part's appearance separately, use getShape(partId) to get the
   *  individual shape and call shape.setAppearance(ap).
   */
  public void setAppearance(Appearance ap){
      ((Shape3D)getChild(BODY)).setAppearance(ap);
      ((Shape3D)getChild(CAP)).setAppearance(ap);
  }

    /**
     * Gets the appearance of the specified part of the cone.
     *
     * @param partId identifier for a given subpart of the cone
     *
     * @return The appearance object associated with the partID.  If an
     * invalid partId is passed in, null is returned.
     *
     * @since Java 3D 1.2.1
     */
    public Appearance getAppearance(int partId) {
	if (partId > CAP || partId < BODY) return null;
	return getShape(partId).getAppearance();
    }


  /**  
   *   Constructs a customized Cone of a given radius, height, flags,
   *   resolution (X and Y dimensions), and appearance. The 
   *   resolution is defined in terms of number of subdivisions
   *   along the object's X axis (width) and Y axis (height). More divisions
   *   lead to finer tesselated objects.
   *   <p>
   *   If appearance is null, the default white appearance will be used.
   *   @param radius Radius
   *   @param height Height
   *   @param xdivision Number of divisions along X direction.
   *   @param ydivision Number of divisions along the height of cone.
   *   @param primflags flags
   *   @param ap Appearance
   */

  public Cone(float radius, float height, int primflags,
	      int xdivision, int ydivision,
	      Appearance ap)
  {
    super();

    Shape3D shape[] = new Shape3D[2];
    this.radius = radius;
    this.height = height;
    xdivisions = xdivision;
    ydivisions = ydivision;
    flags = primflags;
    boolean outside = (flags & GENERATE_NORMALS_INWARD) == 0;
    boolean texCoordYUp = (flags & GENERATE_TEXTURE_COORDS_Y_UP) != 0;
    Quadrics q = new Quadrics();
    GeomBuffer gbuf = null;

    GeomBuffer cache = getCachedGeometry(Primitive.CONE,
					 radius, 0.0f, height,
					 xdivision, ydivision, primflags);
    if (cache != null){
// 	System.out.println("using cached geometry");
	shape[BODY] = new Shape3D(cache.getComputedGeometry());
	numVerts += cache.getNumVerts();
	numTris += cache.getNumTris();
    }
    else {
      // the body of the cone consists of the top of the cone and if 
      // ydivisions is greater than 1, the body of the cone.
	gbuf = q.coneTop((double)(height/2.0 - height/ydivisions),
			 (double)(radius/ydivisions), height/ydivisions,
			 xdivisions, 1.0-1.0/(double)ydivisions,
			 outside, texCoordYUp);
      shape[BODY] = new Shape3D(gbuf.getGeom(flags));
      numVerts += gbuf.getNumVerts();
      numTris += gbuf.getNumTris();
      if ((primflags & Primitive.GEOMETRY_NOT_SHARED) == 0) {
	  cacheGeometry(Primitive.CONE,
			radius, 0.0f, height,
			xdivision, ydivision, primflags, gbuf);
      }
    }
      
    // only need to add a body if the ydivisions is greater than 1
    if (ydivisions > 1) {
	cache = getCachedGeometry(Primitive.CONE_DIVISIONS, radius, 0.0f,
				  height, xdivision, ydivision, primflags);
	if (cache != null) {
// 	    System.out.println("using cached divisions");
	    shape[BODY].addGeometry(cache.getComputedGeometry());
	    numVerts += cache.getNumVerts();
	    numTris += cache.getNumTris();
	}
	else {
	    gbuf = q.coneBody(-(double)(height/2.0),
			      (double)(height/2.0-height/ydivisions),
			      (double)radius, (double)(radius/ydivisions),
			      xdivisions, ydivisions-1, 1.0/(double)ydivisions,
			      outside, texCoordYUp);
	    shape[BODY].addGeometry(gbuf.getGeom(flags));
	    numVerts += gbuf.getNumVerts();
	    numTris += gbuf.getNumTris();
	    if ((primflags & Primitive.GEOMETRY_NOT_SHARED) == 0) {
		 cacheGeometry(Primitive.CONE_DIVISIONS, radius, 0.0f, height,
			       xdivision, ydivision, primflags, gbuf);
	    }
	}
    }
      
    if ((flags & ENABLE_APPEARANCE_MODIFY) != 0) {
	(shape[BODY]).setCapability(Shape3D.ALLOW_APPEARANCE_READ);
	(shape[BODY]).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
    }

    if ((flags & ENABLE_GEOMETRY_PICKING) != 0) {
        (shape[BODY]).setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    }
    
    this.addChild(shape[BODY]);

    // Create bottom cap.
    cache = getCachedGeometry(Primitive.BOTTOM_DISK, radius,
			      radius, -height/2.0f, xdivision, xdivision,
			      primflags);
    if (cache != null) {
// 	System.out.println("using cached bottom");
	shape[CAP] = new Shape3D(cache.getComputedGeometry());
	numVerts += cache.getNumVerts();
	numTris += cache.getNumTris();
    }
    else {
	gbuf = q.disk((double)radius, xdivision, -(double)height/2.0,
		      !outside, texCoordYUp);
	shape[CAP] = new Shape3D(gbuf.getGeom(flags));
	numVerts += gbuf.getNumVerts();
	numTris += gbuf.getNumTris();
	if ((primflags & Primitive.GEOMETRY_NOT_SHARED) == 0) {
	    cacheGeometry(Primitive.BOTTOM_DISK, radius, radius, -height/2.0f,
			  xdivision, xdivision, primflags, gbuf);
	}
    }

    if ((flags & ENABLE_APPEARANCE_MODIFY) != 0) {
	(shape[CAP]).setCapability(Shape3D.ALLOW_APPEARANCE_READ);
	(shape[CAP]).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
    }

    if ((flags & ENABLE_GEOMETRY_PICKING) != 0) {
        (shape[CAP]).setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    }

//     Transform3D t2 = new Transform3D();

    // Flip it to match up the texture coords.
/* This causes the bottom not to match up for odd xdivision values
    objectMat = new Matrix4d();
    objectMat.setIdentity();
    rotMat = new Matrix4d();
    rotMat.setIdentity();
    rotMat.rotZ(Math.PI);
    objectMat.mul(objectMat, rotMat);
    t2.set(objectMat);
*/
    
    this.addChild(shape[CAP]);
 
    if (ap == null){
      setAppearance();
    }
    else setAppearance(ap);
  }

    /**
     * Used to create a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * <code>cloneNode</code> should be overridden by any user subclassed
     * objects.  All subclasses must have their <code>cloneNode</code>
     * method consist of the following lines:
     * <P><blockquote><pre>
     *     public Node cloneNode(boolean forceDuplicate) {
     *         UserSubClass usc = new UserSubClass();
     *         usc.duplicateNode(this, forceDuplicate);
     *         return usc;
     *     }
     * </pre></blockquote>
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see Node#duplicateNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneNode(boolean forceDuplicate) {
        Cone c = new Cone(radius, height, flags, xdivisions,
                          ydivisions, getAppearance());
        c.duplicateNode(this, forceDuplicate);
        return c;
    }

    /**
     * Copies all node information from <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.
     * <P>
     * For any <i>NodeComponent</i> objects
     * contained by the object being duplicated, each <i>NodeComponent</i>
     * object's <code>duplicateOnCloneTree</code> value is used to determine
     * whether the <i>NodeComponent</i> should be duplicated in the new node
     * or if just a reference to the current node should be placed in the
     * new node.  This flag can be overridden by setting the
     * <code>forceDuplicate</code> parameter in the <code>cloneTree</code>
     * method to <code>true</code>.
     *
     * @param originalNode the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public void duplicateNode(Node originalNode, boolean forceDuplicate) {
        super.duplicateNode(originalNode, forceDuplicate);
    }

    /**
     * Returns the radius of the cone
     *
     * @since Java 3D 1.2.1
     */
    public float getRadius() {
	return radius;
    }

    /**
     * Returns the height of the cone
     *
     * @since Java 3D 1.2.1
     */
    public float getHeight() {
	return height;
    }

    /**
     * Returns the number divisions along the X direction
     *
     * @since Java 3D 1.2.1
     */
    public int getXdivisions() {
	return xdivisions;
    }

    /**
     * Returns the number of divisions along the height of the cone
     *
     * @since Java 3D 1.2.1
     */
    public int getYdivisions() {
	return ydivisions;
    }

}
