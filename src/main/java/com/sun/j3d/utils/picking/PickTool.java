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

package com.sun.j3d.utils.picking;

import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.internal.*;

/** 
 * The base class for picking operations.
 * The picking methods will return a PickResult object for each object picked, 
 * which can then be queried to 
 * obtain more detailed information about the specific objects that were
 * picked. 
 * <p>
 * The pick mode specifies the detail level of picking before the PickResult
 * is returned:
 * <p>
 * <UL>
 * <LI> PickTool.BOUNDS - Pick using the bounds of the pickable nodes.  The 
 * PickResult returned will contain the SceneGraphPath to the picked Node.
 * </LI>
 * <LI> PickTool.GEOMETRY will pick using the geometry of the pickable nodes.
 * The PickResult returned will contain the SceneGraphPath to the picked Node.
 * Geometry nodes in the scene must have the ALLOW_INTERSECT capability set for
 * this mode.</LI>
 * <LI> PickTool.GEOMETRY_INTERSECT_INFO -is the same as GEOMETRY, but the 
 * the PickResult will also include information on each intersection
 * of the pick shape with the geometry.  The intersection information includes
 * the sub-primitive picked (that is, the point, line, triangle or quad), 
 * the closest vertex to the center of the pick shape, and 
 * the intersection's coordinate, normal, color and texture coordinates.
 * To allow this information to be generated, Shape3D and Morph nodes must have 
 * the ALLOW_GEOMETRY_READ capability set and GeometryArrays must have the 
 * ALLOW_FORMAT_READ,
 * ALLOW_COUNT_READ, and ALLOW_COORDINATE_READ capabilities set, plus the
 * ALLOW_COORDINATE_INDEX_READ capability for indexed geometry. 
 * To inquire
 * the intersection color, normal or texture coordinates 
 * the corresponding READ capability bits must be set on the GeometryArray.
 * </LI>
 * </UL>
 * <p> The utility method 
 * <A HREF="PickTool.html#setCapabilities(javax.media.j3d.Node, int)">
 * <code>PickTool.setCapabilities(Node, int)</code></A> 
 * can be used before the scene graph is
 * made live to set the 
 * capabilities of Shape3D, Morph or Geometry
 * nodes to allow picking.
 * <p>
 * A PickResult from a lower level of detail pick can be used to
 * inquire more detailed information if the capibility bits are set.  
 * This can be used to filter the PickResults
 * before the more computationally intensive intersection processing. 
 * For example,
 * the application can do a BOUNDS pick and then selectively inquire
 * intersections on some of the PickResults. This will save the effort of 
 * doing intersection computation on the other PickResults.  
 * However, inquiring the intersections from a GEOMETRY pick will make 
 * the intersection computation happen twice, use GEOMETRY_INTERSECT_INFO
 * if you want to inquire the intersection information on all the PickResults.
 * <p>
 * When using pickAllSorted or pickClosest methods, the picks 
 * will be sorted by the distance from the start point of the pick shape to 
 * the intersection point.
 * <p>
 * Morph nodes cannot be picked using the displayed geometry in 
 * GEOMETRY_INTERSECT_INFO mode due to limitations in the current Java3D core 
 * API (the current
 * geometry of the the Morph cannot be inquired).  Instead they are picked 
 * using 
 * the geometry at index 0 in the Morph, this limitation may be eliminated in a
 * future release of Java3D.
 * <p>
 * If the pick shape is a PickBounds, the pick result will contain only the
 * scene graph path, even if the mode is GEOMETRY_INTERSECT_INFO.
 */
public class PickTool {

    /* OPEN ISSUES:
       -- pickClosest() and pickAllSorted() using GEOMETRY and a non-PickRay
	  shape => unsorted picking.
       -- Need to implement Morph geometry index 0 picking.
    */

    private final boolean debug = true;
    protected boolean userDefineShape = false;

    PickShape pickShape;

    /** Used to store the BranchGroup used for picking */
    BranchGroup pickRootBG = null;
    /** Used to store the Locale used for picking */
    Locale pickRootL = null;

    /** Used to store a reference point used in determining how "close" points
        are. 
    */
    Point3d start = null;

    /* pick mode, one of BOUNDS, GEOMETRY, etc. */
    int mode = BOUNDS;

    /** Use this mode to pick by bounds and get basic information
        on the pick. 
    */
    public static final int BOUNDS = 0x200;

    /** Use this mode to pick by geometry and get basic 
	information on the pick. 
    */
    public static final int GEOMETRY = 0x100;

    /** Use this mode to pick by geometry and save 
        information about the intersections (intersected primitive,
	intersection point and closest vertex).
    */
    public static final int GEOMETRY_INTERSECT_INFO = 0x400;


  // Flags for the setCapabilities() method
  /**
   * Flag to pass to <CODE>setCapabilities(Node, int)<code> to set
   * the Node's capabilities to allow intersection tests, but not
   * inquire information about the intersections (use for GEOMETRY mode).
   * @see PickTool#setCapabilities 
   */
  public static final int INTERSECT_TEST = 0x1001;

  /**
   * Flag to pass to <CODE>setCapabilities(Node, int)<code> to set
   * the Node's capabilities to allow inquiry of the intersection
   * coordinate information. 
   * @see PickTool#setCapabilities 
   */
  public static final int INTERSECT_COORD = 0x1002;

  /**
   * Flag to pass to <CODE>setCapabilities(Node, int)<code> to set
   * the Node's capabilities to allow inquiry of all intersection
   * information.
   * @see PickTool#setCapabilities 
   */
  public static final int INTERSECT_FULL = 0x1004;

    /* ============================ METHODS ============================ */

    /** 
     * Constructor with BranchGroup to be picked.
     */
    public PickTool (BranchGroup b) {
	pickRootBG = b;
    }

    /** Returns the BranchGroup to be picked if the tool was initialized
	with a BranchGroup, null otherwise. 
      */
    public BranchGroup getBranchGroup() {
        return pickRootBG;
    }

    /** 
     * Constructor with the Locale to be picked.
     */
    public PickTool (Locale l) {
	pickRootL = l;
    }

    /** 
     * Returns the Locale to be picked if the tool was initialized with
     * a Locale, null otherwise.
     */
    public Locale getLocale () {
	return pickRootL;
    }
    

    /** 
     * @deprecated This method does nothing other than return its
     * input parameter.
     */
     public Locale setBranchGroup (Locale l) {
         return l;
    }

    /** 
     * Sets the capabilities on the Node and it's components to allow
     * picking at the specified detail level.  
     * <p> 
     * Note that by default all com.sun.j3d.utils.geometry.Primitive
     * objects with the same parameters share their geometry (e.g.,
     * you can have 50 spheres in your scene, but the geometry is
     * stored only once).  Therefore the capabilities of the geometry
     * are also shared, and once a shared node is live, the
     * capabilities cannot be changed.  To assign capabilities to
     * Primitives with the same parameters, either set the
     * capabilities before the primitive is set live, or specify the
     * Primitive.GEOMETRY_NOT_SHARED constructor parameter when
     * creating the primitive.
     * @param node The node to modify
     * @param level The capability level, must be one of INTERSECT_TEST,
     *  INTERSECT_COORD or INTERSECT_FULL
     * @throws IllegalArgumentException if Node is not a Shape3D or Morph or
     *	if the flag value is not valid.
     * @throws javax.media.j3d.RestrictedAccessException if the node is part 
     *  of a live or compiled scene graph.  */
    static public void setCapabilities(Node node, int level)  {
	if (node instanceof Morph) {
	    Morph morph = (Morph) node;
	    switch (level) {
	      case INTERSECT_FULL:
		/* intentional fallthrough */
	      case INTERSECT_COORD:
		morph.setCapability(Morph.ALLOW_GEOMETRY_ARRAY_READ);
		/* intentional fallthrough */
	      case INTERSECT_TEST:
		break;
	      default:
		throw new IllegalArgumentException("Improper level");
	    }
	    double[] weights = morph.getWeights();
	    for (int i = 0; i < weights.length; i++) {
		GeometryArray ga = morph.getGeometryArray(i);
		setCapabilities(ga, level);
	    }
	} else if (node instanceof Shape3D) {
	    Shape3D shape = (Shape3D) node;
	    switch (level) {
	      case INTERSECT_FULL:
		/* intentional fallthrough */
	      case INTERSECT_COORD:
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		/* intentional fallthrough */
	      case INTERSECT_TEST:
		break;
	      default:
		throw new IllegalArgumentException("Improper level");
	    }
	    for (int i = 0; i < shape.numGeometries(); i++) {
		Geometry geo = shape.getGeometry(i);
		if (geo instanceof GeometryArray) {
		    setCapabilities((GeometryArray)geo, level);
		} else if (geo instanceof CompressedGeometry) {
		    setCapabilities((CompressedGeometry)geo, level);
		}
	    }
	} else {
	    throw new IllegalArgumentException("Improper node type");
	}
    }

    static private void setCapabilities(GeometryArray ga, int level) {
	switch (level) {
	  case INTERSECT_FULL:
	    ga.setCapability(GeometryArray.ALLOW_COLOR_READ);
	    ga.setCapability(GeometryArray.ALLOW_NORMAL_READ);
	    ga.setCapability(GeometryArray.ALLOW_TEXCOORD_READ);
	    /* intential fallthrough */
	  case INTERSECT_COORD:
	    ga.setCapability(GeometryArray.ALLOW_COUNT_READ);
	    ga.setCapability(GeometryArray.ALLOW_FORMAT_READ);
	    ga.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
	    /* intential fallthrough */
	  case INTERSECT_TEST:
	    ga.setCapability(GeometryArray.ALLOW_INTERSECT);
	    break;
	}
	if (ga instanceof IndexedGeometryArray) {
	    setCapabilities((IndexedGeometryArray)ga, level);
	}
    }

    static private void setCapabilities(IndexedGeometryArray iga, int level) {
	switch (level) {
	  case INTERSECT_FULL:
	    iga.setCapability(IndexedGeometryArray.ALLOW_COLOR_INDEX_READ);
	    iga.setCapability(IndexedGeometryArray.ALLOW_NORMAL_INDEX_READ);
	    iga.setCapability(IndexedGeometryArray.ALLOW_TEXCOORD_INDEX_READ);
	    /* intential fallthrough */
	  case INTERSECT_COORD:
	    iga.setCapability(IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ);
	    /* intential fallthrough */
	  case INTERSECT_TEST:
	    break;
	}
    }

    static private void setCapabilities(CompressedGeometry cg, int level) {
	switch (level) {
	  case INTERSECT_FULL:
	    /* intential fallthrough */
	  case INTERSECT_COORD:
	    cg.setCapability(CompressedGeometry.ALLOW_GEOMETRY_READ);
	    /* intential fallthrough */
	  case INTERSECT_TEST:
	    cg.setCapability(CompressedGeometry.ALLOW_INTERSECT);
	    break;
	}
    }

    // Methods used to define the pick shape

    /** Sets the pick shape to a user-provided PickShape object 
      *  @param ps The pick shape to pick against.
      *  @param startPt The start point to use for distance calculations
      */
    public void setShape (PickShape ps, Point3d startPt) {
	this.pickShape = ps;
	this.start = startPt;
	userDefineShape = (ps != null);
    }

    /**  Sets the pick shape to use a user-provided Bounds object 
      *  @param bounds The bounds to pick against.
      *  @param startPt The start point to use for distance calculations
      */
    public void setShapeBounds (Bounds bounds, Point3d startPt) {
	this.pickShape = (PickShape) new PickBounds (bounds);
	this.start = startPt;
	userDefineShape = true;
    }

    /** Sets the picking detail mode.  The default is BOUNDS.
     * @param mode One of BOUNDS, GEOMETRY, GEOMETRY_INTERSECT_INFO, or 
     * @exception IllegalArgumentException if mode is not a legal value
     */
    public void setMode (int mode) {
	if ((mode != BOUNDS) && (mode != GEOMETRY) && 
	      (mode != GEOMETRY_INTERSECT_INFO)) {
	    throw new java.lang.IllegalArgumentException();
	}
	this.mode = mode;
    }

    /** Gets the picking detail mode.
     */
    public int getMode () {
	return mode;
    }

    /**  Sets the pick shape to a PickRay. 
     *   @param start The start of the ray
     *   @param dir The direction of the ray
     */
    public void setShapeRay (Point3d start, Vector3d dir) {
	this.pickShape = (PickShape) new PickRay (start, dir);
	this.start = start;
	userDefineShape = true;
    }

    /**  Sets the pick shape to a PickSegment.
	 @param start The start of the segment
p	 @param end The end of the segment
     */
    public void setShapeSegment (Point3d start, Point3d end) {
	this.pickShape = (PickShape) new PickSegment (start, end);
	this.start = start;
	userDefineShape = true;
    }

    /**  Sets the pick shape to a capped PickCylinder 
     *   @param start The start of axis of the cylinder
     *   @param end The end of the axis of the cylinder
     *   @param radius The radius of the cylinder
     */
    public void setShapeCylinderSegment (Point3d start, Point3d end, 
				   double radius) {
	this.pickShape = (PickShape) 
				new PickCylinderSegment (start, end, radius);
	this.start = start;
	userDefineShape = true;
    }

    /**  Sets the pick shape to an infinite PickCylinder.
     *   @param start The start of axis of the cylinder
     *   @param dir The direction of the axis of the cylinder
     *   @param radius The radius of the cylinder
     */
    public void setShapeCylinderRay (Point3d start, Vector3d dir, 
			       double radius) {
	this.pickShape = (PickShape) new PickCylinderRay (start, dir, radius);
	this.start = start;
	userDefineShape = true;
    }

    /** Sets the pick shape to a capped PickCone 
     *   @param start The start of axis of the cone
     *   @param end The end of the axis of the cone
     *   @param angle The angle of the cone
     */
    public void setShapeConeSegment (Point3d start, Point3d end, 
			       double angle) {
	this.pickShape = (PickShape) new PickConeSegment (start, end, angle);
	this.start = start;
	userDefineShape = true;
    }

    /**  Sets the pick shape to an infinite PickCone. 
     *   @param start The start of axis of the cone
     *   @param dir The direction of the axis of the cone
     *   @param angle The angle of the cone
     */
    public void setShapeConeRay (Point3d start, Vector3d dir, 
			   double angle) {
	this.pickShape = (PickShape) new PickConeRay (start, dir, angle);
	this.start = start;
	userDefineShape = true;
    }

    /** Returns the PickShape for this object. */
    public PickShape getPickShape () {
	return pickShape;
    }

    /** Returns the start postion used for distance measurement. */
    public Point3d getStartPosition () {
	return start;
    }

    /** Selects all the nodes that intersect the PickShape.
      @return An array of <code>PickResult</code> objects which will contain 
       information about the picked instances. <code>null</code> if nothing was 
       picked.
    */ 
    public PickResult[] pickAll () {
	PickResult[] retval = null;
	switch (mode) {
	  case BOUNDS:
	    retval =  pickAll(pickShape);
	    break;
	  case GEOMETRY:
	    retval =  pickGeomAll(pickShape);
	    break;
	  case GEOMETRY_INTERSECT_INFO:
	    retval =  pickGeomAllIntersect(pickShape);
	    break;
	  default:
	    throw new RuntimeException("Invalid pick mode");
	}
	return retval;
    }

    /** Select one of the nodes that intersect the PickShape
        @return A <code>PickResult</code> object which will contain 
         information about the picked instance. <code>null</code> if nothing 
	 was picked.
    */ 
    public PickResult pickAny () {
	PickResult retval = null;
	switch (mode) {
	  case BOUNDS:
	    retval =  pickAny(pickShape);
	    break;
	  case GEOMETRY:
	    retval =  pickGeomAny(pickShape);
	    break;
	  case GEOMETRY_INTERSECT_INFO:
	    retval =  pickGeomAnyIntersect(pickShape);
	    break;
	  default:
	    throw new RuntimeException("Invalid pick mode");
	}
	return retval;
    }

    /** Select all the nodes that intersect the 
        PickShape, returned sorted. The "closest" object will be returned first.
        See note above to see how "closest" is determined.    
	<p>
	@return An array of <code>PickResult</code> objects which will contain 
	information 
	about the picked instances. <code>null</code> if nothing was picked.
    */
    public PickResult[] pickAllSorted () {
	PickResult[] retval = null;

	// System.out.println ("PickTool.pickAllSorted.");

	switch (mode) {
	  case BOUNDS:
	      // System.out.println ("PickTool.pickAllSorted : Bounds");
	      retval =  pickAllSorted(pickShape);
	      break;
	  case GEOMETRY:
	      // System.out.println ("PickTool.pickAllSorted : Geometry");
	      // TODO - BugId 4351050.
	      // pickGeomAllSorted is broken for PickCone and PickCylinder :
	      // The current Shape3D.intersect() API doesn't return the distance for
	      // PickCone and PickCylinder.
	      // 2) TODO - BugId 4351579.
	      // pickGeomClosest is broken for multi-geometry Shape3D node :
	      // The current Shape3D.intersect() API does't return the closest intersected
	      // geometry.
	      retval =  pickGeomAllSorted(pickShape);
	      
	      break;
	  case GEOMETRY_INTERSECT_INFO:
	      // System.out.println ("PickShape " + pickShape);
	      // System.out.println ("PickTool.pickAllSorted : GEOMETRY_INTERSECT_INFO");
	      retval =  pickGeomAllSortedIntersect(pickShape);
	      break;
	default:
	    throw new RuntimeException("Invalid pick mode");
	}
	return retval;
    }

    /** Select the closest node that 
        intersects the PickShape. See note above to see how "closest" is 
	determined.
	<p>
	@return A <code>PickResult</code> object which will contain 
	information about the picked instance. <code>null</code> if nothing 
	was picked.
    */
    public PickResult pickClosest () {
	PickResult retval = null;
	switch (mode) {
	  case BOUNDS:
	    retval =  pickClosest(pickShape);
	    break;
	  case GEOMETRY:
	      // System.out.println("pickCloset -- Geometry based picking");
	      // 1) TODO - BugId 4351050.
	      // pickGeomClosest is broken for PickCone and PickCylinder :
	      // The current Shape3D.intersect() API doesn't return the distance for
	      // PickCone and PickCylinder.
	      // 2) TODO - BugId 4351579.
	      // pickGeomClosest is broken for multi-geometry Shape3D node :
	      // The current Shape3D.intersect() API does't return the closest intersected
	      // geometry.
	      retval =  pickGeomClosest(pickShape);
	      
	      break;
	case GEOMETRY_INTERSECT_INFO:
	    // System.out.println ("PickShape " + pickShape);
	    // System.out.println ("PickTool.pickClosest : GEOMETRY_INTERSECT_INFO");
	    retval =  pickGeomClosestIntersect(pickShape);
	    break;
	  default:
	    throw new RuntimeException("Invalid pick mode");
	}
	return retval;
    }

    private PickResult[] pickAll (PickShape pickShape) {
	PickResult[] pr = null;
	SceneGraphPath[] sgp = null;

	if (pickRootBG != null) {
	    sgp = pickRootBG.pickAll (pickShape);
	} else if (pickRootL != null) {
	    sgp = pickRootL.pickAll (pickShape);
	}
	if (sgp == null) return null; // no match

	// Create PickResult array
	pr = new PickResult [sgp.length];
	for (int i=0;i<sgp.length;i++) {
	    pr[i] = new PickResult (sgp[i], pickShape);
	}
	return pr;
    }

    private PickResult[] pickAllSorted (PickShape pickShape) {
	PickResult[] pr = null;
	SceneGraphPath[] sgp = null;

	if (pickRootBG != null) {
	    sgp = pickRootBG.pickAllSorted (pickShape);
	} else if (pickRootL != null) {
	    sgp = pickRootL.pickAllSorted (pickShape);
	}
	if (sgp == null) return null; // no match

	// Create PickResult array
	pr = new PickResult [sgp.length];
	for (int i=0;i<sgp.length;i++) {
	    pr[i] = new PickResult (sgp[i], pickShape);
	}
	return pr;
    }

    private PickResult pickAny (PickShape pickShape) {
	PickResult pr = null;
	SceneGraphPath sgp = null;

	if (pickRootBG != null) {
	    sgp = pickRootBG.pickAny (pickShape);
	} else if (pickRootL != null) {
	    sgp = pickRootL.pickAny (pickShape);
	}
	if (sgp == null) return null; // no match

	// Create PickResult object
	pr = new PickResult (sgp, pickShape);
	return pr;
    }

    private PickResult pickClosest (PickShape pickShape) {
	PickResult pr = null;
	SceneGraphPath sgp = null;

	if (pickRootBG != null) {
	    sgp = pickRootBG.pickClosest (pickShape);
	} else if (pickRootL != null) {
	    sgp = pickRootL.pickClosest (pickShape);
	}
	if (sgp == null) return null; // no match

	// Create PickResult object
	pr = new PickResult (sgp, pickShape);
	return pr;
    }

    // ================================================================
    // GEOMETRY METHODS
    // ================================================================

    private PickResult[] pickGeomAll (PickShape pickShape) {
	SceneGraphPath[] sgp = null;
	Node obj[] = null;
	int i, cnt=0;

	// First pass
	if (pickRootBG != null) {
	    sgp = pickRootBG.pickAll(pickShape);
	} else if (pickRootL != null) {
	    sgp = pickRootL.pickAll(pickShape);
	}
	if (sgp == null) return null; // no match

	// Second pass, check to see if geometries intersected
	boolean found[] = new boolean [sgp.length];

	obj = new Node [sgp.length];
	PickResult[] pr = new PickResult[sgp.length];
	for (i=0; i<sgp.length; i++) {
	    obj[i] = sgp[i].getObject();
	    pr[i] = new PickResult (sgp[i], pickShape);

	    if (obj[i] instanceof Shape3D) {
		found[i] = ((Shape3D) obj[i]).intersect(sgp[i], pickShape);
	    } else if (obj[i] instanceof Morph) {
		found[i] = ((Morph) obj[i]).intersect(sgp[i], pickShape); 
	    }
	    if (found[i] == true) cnt++;	
	}

	if (cnt == 0) return null; // no match

	PickResult[] newpr = new PickResult[cnt];
	cnt = 0; // reset for reuse.
	for(i=0; i<sgp.length; i++) {
	    if (found[i] == true)
	    pr[cnt++] = pr[i];
	}

	return pr;
    }

    private PickResult[] pickGeomAllSorted(PickShape pickShape) {
	SceneGraphPath[] sgp = null;
	Node[] obj = null;
	int i, cnt=0;
	double[] dist = new double[1];
	
	// First pass
	if (pickRootBG != null) {
	    sgp = pickRootBG.pickAll(pickShape);
	} else if (pickRootL != null) {
	    sgp = pickRootL.pickAll(pickShape);
	}
	if (sgp == null) return null; // no match

	/*
	  System.out.println ("PickTool.pickGeomAllSorted: bounds " +
	  "picking found "+sgp.length+" nodes");
	  */
	// Second pass, check to see if geometries intersected
	boolean[] found = new boolean [sgp.length];
	double[] distArr = new double[sgp.length];
	obj = new Node [sgp.length];
	PickResult[] pr = new PickResult [sgp.length];
	
	for (i=0; i<sgp.length; i++) {
	    obj[i] = sgp[i].getObject();
	    pr[i] = new PickResult (sgp[i], pickShape);
	    if (obj[i] instanceof Shape3D) {
		found[i] = ((Shape3D)obj[i]).intersect(sgp[i], pickShape,
						       dist);
		distArr[i] = dist[0];		
	    } else if (obj[i] instanceof Morph) {
		found[i] = ((Morph)obj[i]).intersect(sgp[i], pickShape,
						     dist);
		distArr[i] = dist[0];
	    }
	    if (found[i] == true) cnt++;	
	}
	if (cnt == 0) return null; // no match

	PickResult[] npr = new PickResult [cnt];
	double[] distance = new double [cnt];
	cnt = 0; // reset for reuse.
	for(i=0; i<sgp.length; i++) {
	    if (found[i] == true) {
		distance[cnt] = distArr[i];
		npr[cnt++] = pr[i];
	    }
	}
	if (cnt > 1) { 
	    return sortPickResults (npr, distance);
	} else { // Don't have to sort if only one item
	    return npr;
	}
    }

    private PickResult pickGeomAny (PickShape pickShape) {
	Node obj = null;
	int i;
	SceneGraphPath[] sgpa = null;

	if (pickRootBG != null) {
	    sgpa = pickRootBG.pickAll(pickShape);
	} else if (pickRootL != null) {
	    sgpa = pickRootL.pickAll(pickShape);
	}

	if (sgpa == null) return null; // no match

	for(i=0; i<sgpa.length; i++) {
	    obj = sgpa[i].getObject();
	    PickResult pr = new PickResult(sgpa[i], pickShape);
	    if(obj instanceof Shape3D) {
		if(((Shape3D) obj).intersect(sgpa[i], pickShape)) {
		    return pr;
		}
	    } else if (obj instanceof Morph) {
		if(((Morph) obj).intersect(sgpa[i], pickShape)){
		    return pr;
		}
	    }
	}

	return null;
    }

    private PickResult pickGeomClosest(PickShape pickShape) {
	// System.out.println("pickGeomCloset -- Geometry based picking");
	PickResult[] pr = pickGeomAllSorted(pickShape);
	if (pr == null) {
	    return null;
	} else {
	    return pr[0];
	}
    }

    // ================================================================
    // NEW METHODS, return additional information
    // ================================================================

    private PickResult[] pickGeomAllIntersect(PickShape pickShape) {
	SceneGraphPath[] sgp = null;
	Node obj[] = null;
	int i, cnt=0;

	// First pass
	if (pickRootBG != null) {
	    sgp = pickRootBG.pickAll(pickShape);
	} else if (pickRootL != null) {
	    sgp = pickRootL.pickAll(pickShape);
	}
	if (sgp == null) return null; // no match

	// Second pass, check to see if geometries intersected
	boolean found[] = new boolean [sgp.length];

	PickResult[] pr = new PickResult[sgp.length];
	for (i=0; i<sgp.length; i++) {
	    pr[i] = new PickResult (sgp[i], pickShape);
	    if (pr[i].numIntersections() > 0) {
		found[i] = true;
		cnt++;
	    }
	}

	if (cnt == 0) return null; // no match

	PickResult[] newpr = new PickResult[cnt];
	cnt = 0; // reset for reuse.
	for(i=0; i<sgp.length; i++) {
	    if(found[i] == true)
	    pr[cnt++] = pr[i];
	}

	return pr;
    }

    private PickResult[] pickGeomAllSortedIntersect (PickShape pickShape) {
	SceneGraphPath[] sgp = null;
	Node[] obj = null;
	int i, cnt=0;
	double[] dist = new double[1];

	// First pass
	if (pickRootBG != null) {
	    sgp = pickRootBG.pickAll(pickShape);
	} else if (pickRootL != null) {
	    sgp = pickRootL.pickAll(pickShape);
	}
	if (sgp == null) return null; // no match


	// System.out.println ("PickTool.pickGeomAllSortedIntersect: bounds " +
	// " picking found "+sgp.length+" nodes");
		
	
	// Second pass, check to see if geometries intersected
	boolean[] found = new boolean[sgp.length];
	double[] distArr = new double[sgp.length];
	
	PickResult[] pr = new PickResult[sgp.length];
	for (i=0; i<sgp.length; i++) {
 	    pr[i] = new PickResult(sgp[i], pickShape);
	    int numIntersection = pr[i].numIntersections();
	    if (numIntersection > 0) {
		// System.out.println ("numIntersection " + numIntersection);
		found[i] = true;
		double minDist;
		double tempDist;

		int minIndex;
		boolean needToSwap = false;
		minDist = pr[i].getIntersection(0).getDistance();
		minIndex = 0;
		for(int j=1; j<numIntersection; j++) {
		    // System.out.println ("Distance " + pr[i].getIntersection(j).getDistance());
		    //System.out.println ("Geom Index " + pr[i].getIntersection(j).getGeometryArrayIndex());
		    tempDist = pr[i].getIntersection(j).getDistance();
		    if(minDist > tempDist) {
			minDist = tempDist;
			minIndex = j;    
			needToSwap = true;
		    }
		}
		
		//Swap if necc.
		if(needToSwap) {
		    // System.out.println ("Swap is needed");
		    PickIntersection pi0 = pr[i].getIntersection(0);
		    PickIntersection piMin = pr[i].getIntersection(minIndex);
		    pr[i].intersections.set(0, piMin);
		    pr[i].intersections.set(minIndex, pi0);
		}
		
		distArr[i] = pr[i].getIntersection(0).getDistance();
		cnt++;
	    }
	}
	
	
	// System.out.println ("PickTool.pickGeomAllSortedIntersect: geometry intersect check "
	// + " cnt " + cnt);
	  
	
	if (cnt == 0) return null; // no match

	PickResult[] npr = new PickResult[cnt];
	double[] distance = new double[cnt];
	cnt = 0; // reset for reuse.
	for(i=0; i<sgp.length; i++) {
	    if(found[i] == true) {
		distance[cnt] = distArr[i];
		npr[cnt++] = pr[i];
	    }
	}
	
	if (cnt > 1) {
	    return sortPickResults (npr, distance);
	} else { // Don't have to sort if only one item
	    return npr;
	}
    }
    
    private PickResult pickGeomClosestIntersect(PickShape pickShape) {
	PickResult[] pr = pickGeomAllSortedIntersect(pickShape);
	/*
	  System.out.println ("PickTool.pickGeomClosestIntersect: pr.length "
	  + pr.length);
	  for(int i=0;i<pr.length;i++) {
	  System.out.println ("pr["+i+"] " + pr[i]);
	  }
	  */

	if (pr == null) {
	    return null;
	} else {
	    return pr[0];
	}
    }
    private PickResult pickGeomAnyIntersect(PickShape pickShape) { 
	Node obj = null;
	int i;
	SceneGraphPath[] sgpa = null;

	if (pickRootBG != null) {
	    sgpa = pickRootBG.pickAll(pickShape);
	} else if (pickRootL != null) {
	    sgpa = pickRootL.pickAll(pickShape);
	}
	if (sgpa == null) return null; // no match
	for(i=0; i<sgpa.length; i++) {
	    PickResult pr = new PickResult(sgpa[i], pickShape);
	    pr.setFirstIntersectOnly(true);
	    if (pr.numIntersections() > 0) {
		return pr;
	    }
	}

	return null;
    }

    // ================================================================
    // Sort Methods
    // ================================================================
    private PickResult[] sortPickResults (PickResult[] pr, double[] dist) {
	int[] pos = new int [pr.length];
	PickResult[] prsorted = new PickResult [pr.length];
	
	// Initialize position array
	for (int i=0; i<pr.length; i++) {
	    pos[i]=i;
	}
	// Do sort
	quicksort (0, dist.length-1, dist, pos);

	// Create new array
	for (int i=0; i<pr.length; i++) {
	    prsorted[i] = pr[pos[i]];
	}
	return prsorted;
    }
  
    private final void quicksort( int l, int r, double[] dist, 
			    int[] pos) {
	int p,i,j;
	double tmp,k;

	i = l;
	j = r;
	k = dist[(l+r) / 2];
	do {
	    while (dist[i]<k) i++;
	    while (k<dist[j]) j--;
	    if (i<=j) {
		tmp = dist[i];
		dist[i] =dist[j];
		dist[j] = tmp;

		p=pos[i];
		pos[i]=pos[j];
		pos[j]=p;
		i++;
		j--;
	    }
	} while (i<=j);

	if (l<j) quicksort(l, j, dist, pos);
	if (l<r) quicksort(i, r, dist, pos);
    }

} // PickTool





