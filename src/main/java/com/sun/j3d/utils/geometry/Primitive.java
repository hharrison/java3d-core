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
 * Base class for all Java 3D primitives. By default all primitives
 * with the same parameters share their geometry (e.g., you can have 50
 * shperes in your scene, but the geometry is stored only once). A
 * change to one primitive will effect all shared nodes.  Another
 * implication of this implementation is that the capabilities of the
 * geometry are shared, and once one of the shared nodes is live, the
 * capabilities cannot be set.  Use the GEOMETRY_NOT_SHARED flag if
 * you do not wish to share geometry among primitives with the same
 * parameters. 
 */

public abstract class Primitive extends Group {
  /**
   * Specifies that normals are generated along with the positions. 
   */
  public static final int GENERATE_NORMALS =  0x01;

  /**
   * Specifies that texture coordinates are generated along with the
   * positions. 
   */
  public static final int GENERATE_TEXTURE_COORDS = 0x02;

  /**
   * Specifies that normals are to be flipped along the surface.
   */
  public static final int GENERATE_NORMALS_INWARD = 0x04;

  /**
   * Specifies that texture coordinates are to be Y up.
   *
   * @since Java 3D 1.5.1
   */
  // Fix to Issue 411. Java 3D prefers images used for texture mapping to be Y-up   
  public static final int GENERATE_TEXTURE_COORDS_Y_UP = 0x08;
  
  
  /** 
   * Specifies that the geometry being created will not be shared by
   * another scene graph node. By default all primitives created with
   * the same parameters share their geometry (e.g., you can have 50
   * spheres in your scene, but the geometry is stored only once). A
   * change to one primitive will effect all shared nodes.  You
   * specify this flag if you do not wish to share any geometry among
   * primitives of the same parameters.  */
  public static final int GEOMETRY_NOT_SHARED = 0x10;

  /**
   * Specifies that the ALLOW_INTERSECT
   * capability bit should be set on the generated geometry.
   * This allows the object
   * to be picked using Geometry based picking.
   */
  public static final int ENABLE_GEOMETRY_PICKING = 0x20;

  /**
   * Specifies that the ALLOW_APPEARANCE_READ and 
   * ALLOW_APPEARANCE_WRITE bits are to be set on the generated
   * geometry's Shape3D nodes.
   */
  public static final int ENABLE_APPEARANCE_MODIFY = 0x40;

  static final int SPHERE = 0x01;
  static final int CYLINDER = 0x02;
  static final int CONE = 0x04;
  static final int BOX = 0x08;

    // used for cached geometries of Cone and Cylinder
    static final int TOP_DISK = 0x10;
    static final int BOTTOM_DISK = 0x20;
    static final int CONE_DIVISIONS = 0x40;

  int numTris = 0;
  int numVerts = 0;

  /**
   * Primitive flags.
   */
  int flags;

  
  /**
   * Constructs a default primitive.
   */
  public Primitive()
  {
    flags = 0;
    setCapability(ENABLE_PICK_REPORTING);
    setCapability(ALLOW_CHILDREN_READ);
  }
       
    /**
     * Returns the total number of triangles in this primitive.
     * @return the total number of triangles in this primitive
     */
    public int getNumTriangles() {
	return numTris;
    }

    /**
     * @deprecated The number of triangles is an immutable attribute.
     */
    public void setNumTriangles(int num) {
	System.err.println("Warning: setNumTriangles has no effect");
    }

    /**
     * Returns the total number of vertices in this primitive.
     * @return the total number of vertices in this primitive
     */
    public int getNumVertices() {
	return numVerts;
    }

    /**
     * @deprecated The number of vertices is an immutable attribute.
     */
    public void setNumVertices(int num) {
	System.err.println("Warning: setNumVertices has no effect");
    }

  /** Returns the flags of primitive (generate normal, textures, caching, etc).
   */
  public int getPrimitiveFlags()
  {
    return flags;
  }

  /**
   * @deprecated The primitive flags must be set at construction time
   * via one of the subclass constructors.
   */
  public void setPrimitiveFlags(int fl) {
      System.err.println("Warning: setPrimitiveFlags has no effect");
  }

  /** Obtains a shape node of a subpart of the primitive.
   * @param partid identifier for a given subpart of the primitive.
   */
  public abstract Shape3D getShape(int partid);

  /** Gets the appearance of the primitive (defaults to first subpart).
   */
  public Appearance getAppearance(){
    return getShape(0).getAppearance();
  }

    /**
     * Gets the appearance of the specified part of the primitive.
     *
     * @param partId identifier for a given subpart of the primitive
     *
     * @return The appearance object associated with the partID.  If an
     * invalid partId is passed in, null is returned.
     *
     * @since Java 3D 1.2.1
     */
    public abstract Appearance getAppearance(int partId);

  /** Sets the appearance of a subpart given a partid.
   */

  public void setAppearance(int partid, Appearance ap)
  {
    getShape(partid).setAppearance(ap);
  }

  /** Sets the main appearance of the primitive (all subparts) to 
   *  same appearance.
   */
  public abstract void setAppearance(Appearance ap);

  
  /** Sets the main appearance of the primitive (all subparts) to 
   *  a default white appearance.
   */
  public void setAppearance(){

    Color3f aColor  = new Color3f(0.1f, 0.1f, 0.1f);
    Color3f eColor  = new Color3f(0.0f, 0.0f, 0.0f);
    Color3f dColor  = new Color3f(0.6f, 0.6f, 0.6f);
    Color3f sColor  = new Color3f(1.0f, 1.0f, 1.0f);

    Material m = new Material(aColor, eColor, dColor, sColor, 100.0f);
    Appearance a = new Appearance();
    m.setLightingEnable(true);
    a.setMaterial(m);
    setAppearance(a);
  }

  static Hashtable geomCache = new Hashtable(); 

  String strfloat(float x)
  {
    return (new Float(x)).toString();
  }

  protected void cacheGeometry(int kind, float a, float b, 
			    float c, int d, int e, int flags, 
			    GeomBuffer geo)
  {
    String key = new String(kind+strfloat(a)+strfloat(b)+
			    strfloat(c)+d+e+flags);
    geomCache.put(key, geo);
  }

  protected GeomBuffer getCachedGeometry(int kind, float a, float b, float c,
				      int d, int e, int flags)
  {
    String key = new String(kind+strfloat(a)+strfloat(b)+
			    strfloat(c)+d+e+flags);
    Object cache =  geomCache.get(key);

    return((GeomBuffer) cache);
  }
  
  /**
   * Clear the shared geometry cache for all Primitive types.
   * Existing Shapes with shared geometry will continue to share 
   * the geometry. New Primitives will create new shared geometry.
   *
   * @since Java 3D 1.3.2
   */
  public static void clearGeometryCache() {
    geomCache.clear();
  }
}

