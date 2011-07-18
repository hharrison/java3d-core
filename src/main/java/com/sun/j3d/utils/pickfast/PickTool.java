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

import com.sun.j3d.utils.geometry.Primitive;
import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.internal.*;

/** 
 * The base class for optimized picking operations.
 * The picking methods will return a PickInfo object for each object picked, 
 * which can then be queried to 
 * obtain more detailed information about the specific objects that were
 * picked. 
 * <p>
 * The pick mode specifies the detail level of picking before the PickInfo
 * is returned:
 * <p>
 * <UL>
 * <LI> PickInfo.PICK_BOUNDS - Pick using the only bounds of the pickable nodes. 
 * </LI>
 * <LI> PickInfo.PICK_GEOMETRY will pick using the geometry of the pickable nodes.
 * Geometry nodes in the scene must have the ALLOW_INTERSECT capability set for
 * this mode.</LI>
 * <p>
 * The pick flags specifies the content of the PickInfo(s) returned by the 
 * pick methods. This is specified as one or more individual bits that are 
 * bitwise "OR"ed together to describe the PickInfo data. The flags include :
 * <ul>
 * <code>PickInfo.SCENEGRAPHPATH</code> - request for computed SceneGraphPath.<br>    
 * <code>PickInfo.NODE</code> - request for computed intersected Node.<br>
 * <code>PickInfo.LOCAL_TO_VWORLD</code> - request for computed local to virtual world transform.<br>
 * <code>PickInfo.CLOSEST_INTERSECTION_POINT</code> - request for closest intersection point.<br>
 * <code>PickInfo.CLOSEST_DISTANCE</code> - request for the distance of closest intersection.<br>
 * <code>PickInfo.CLOSEST_GEOM_INFO</code> - request for only the closest intersection geometry information.<br>
 * <code>PickInfo.ALL_GEOM_INFO</code> - request for all intersection geometry information.<br>
 * </ul>
 * </UL>
 * <p>
 * When using pickAllSorted or pickClosest methods, the picks 
 * will be sorted by the distance from the start point of the pick shape to 
 * the intersection point.
 *
 * @see Locale#pickClosest(int,int,javax.media.j3d.PickShape)
 */
public class PickTool {


    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>
     * to return a
     * <code>Shape3D</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int TYPE_SHAPE3D = 0x1;

    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>
     * to return a
     * <code>Morph</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int TYPE_MORPH = 0x2;

    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>

     * to return a
     * <code>Primitive</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int TYPE_PRIMITIVE = 0x4;

    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>
     * to return a
     * <code>Link</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int TYPE_LINK = 0x8;

    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>
     * to return a
     * <code>Group</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int TYPE_GROUP = 0x10;
  
    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>
     * to return a
     * <code>TransformGroup</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int TYPE_TRANSFORM_GROUP = 0x20;
 
    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>
     * to return a
     * <code>BranchGroup</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int TYPE_BRANCH_GROUP = 0x40;

    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>
     * to return a
     * <code>Switch</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int TYPE_SWITCH = 0x80;


    private static final int ALL_FLAGS = 
        PickInfo.SCENEGRAPHPATH			|
        PickInfo.NODE				|
        PickInfo.LOCAL_TO_VWORLD		|
        PickInfo.CLOSEST_INTERSECTION_POINT	|
        PickInfo.CLOSEST_DISTANCE		|
        PickInfo.CLOSEST_GEOM_INFO		|
        PickInfo.ALL_GEOM_INFO;

    private final boolean debug = false;
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

    int mode = PickInfo.PICK_BOUNDS;
    int flags = PickInfo.NODE;

    /* ============================ METHODS ============================ */

    /** 
     * Constructor with BranchGroup to be picked.
     */
    public PickTool (BranchGroup b) {
	pickRootBG = b;
    }

    /** 
     * Constructor with the Locale to be picked.
     */
    public PickTool (Locale l) {
	pickRootL = l;
    }

    /** Returns the BranchGroup to be picked if the tool was initialized
	with a BranchGroup, null otherwise. 
      */
    public BranchGroup getBranchGroup() {
        return pickRootBG;
    }

    /** 
     * Returns the Locale to be picked if the tool was initialized with
     * a Locale, null otherwise.
     */
    public Locale getLocale () {
	return pickRootL;
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

    /** Sets the picking detail mode.  The default is PickInfo.PICK_BOUNDS.
     * @param mode One of PickInfo.PICK_BOUNDS or PickInfo.PICK_GEOMETRY. 
     * @exception IllegalArgumentException if mode is not a legal value
     */
    public void setMode (int mode) {
	if ((mode != PickInfo.PICK_BOUNDS) && (mode != PickInfo.PICK_GEOMETRY)) {
	    throw new java.lang.IllegalArgumentException();
	}
	this.mode = mode;
    }

    /** Gets the picking detail mode.
     */
    public int getMode () {
	return mode;
    }

    /** Sets the PickInfo content flags. The default is PickInfo.NODE.
     * @param flags specified as one or more individual bits that are 
     * bitwise "OR"ed together : 
     * <ul>
     * <code>PickInfo.SCENEGRAPHPATH</code> - request for computed SceneGraphPath.<br>    
     * <code>PickInfo.NODE</code> - request for computed intersected Node.<br>
     * <code>PickInfo.LOCAL_TO_VWORLD</code> - request for computed local to virtual world transform.<br>
     * <code>PickInfo.CLOSEST_INTERSECTION_POINT</code> - request for closest intersection point.<br>
     * <code>PickInfo.CLOSEST_DISTANCE</code> - request for the distance of closest intersection.<br>
     * <code>PickInfo.CLOSEST_GEOM_INFO</code> - request for only the closest intersection geometry information.<br>
     * <code>PickInfo.ALL_GEOM_INFO</code> - request for all intersection geometry information.<br>
     * </ul>
     * @exception IllegalArgumentException if any other bits besides the above are set.
     */
    public void setFlags (int flags) {
	if ((flags & ~ALL_FLAGS) != 0) {
	    throw new java.lang.IllegalArgumentException();
	}
	this.flags = flags;
    }

    /** Gets the PickInfo content flags.
     */
    public int getFlags () {
	return flags;
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
	 @param end The end of the segment
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
      @return An array of <code>PickInfo</code> objects which will contain 
       information about the picked instances. <code>null</code> if nothing was 
       picked.
    */ 
    public PickInfo[] pickAll () {
	PickInfo[] pickInfos = null;
	if (pickRootBG != null) {
	    pickInfos = pickRootBG.pickAll(mode, flags, pickShape);
	} else if (pickRootL != null) {
	    pickInfos = pickRootL.pickAll(mode, flags, pickShape);
	}
	return pickInfos;
    }

    /** Select one of the nodes that intersect the PickShape
        @return A <code>PickInfo</code> object which will contain 
         information about the picked instance. <code>null</code> if nothing 
	 was picked.
    */ 
    public PickInfo pickAny () {
	PickInfo pickInfo = null;
	if (pickRootBG != null) {
	    pickInfo = pickRootBG.pickAny(mode, flags, pickShape);
	} else if (pickRootL != null) {
	    pickInfo = pickRootL.pickAny(mode, flags, pickShape);
	}
	return pickInfo;
    }

    /** Select all the nodes that intersect the 
        PickShape, returned sorted. The "closest" object will be returned first.
        See note above to see how "closest" is determined.    
	<p>
	@return An array of <code>PickInfo</code> objects which will contain 
	information 
	about the picked instances. <code>null</code> if nothing was picked.
    */
    public PickInfo[] pickAllSorted () {
	PickInfo[] pickInfos = null;
	if (pickRootBG != null) {
	    pickInfos = pickRootBG.pickAllSorted(mode, flags, pickShape);
	} else if (pickRootL != null) {
	    pickInfos = pickRootL.pickAllSorted(mode, flags, pickShape);
	}
	return pickInfos;
    }

    /** Select the closest node that 
        intersects the PickShape. See note above to see how "closest" is 
	determined.
	<p>
	@return A <code>PickInfo</code> object which will contain 
	information about the picked instance. <code>null</code> if nothing 
	was picked.
    */
    public PickInfo pickClosest () {
	// System.out.println("PickTool : pickClosest ...");
	PickInfo pickInfo = null;
	if (pickRootBG != null) {
	    pickInfo = pickRootBG.pickClosest(mode, flags, pickShape);
	} else if (pickRootL != null) {
	    pickInfo = pickRootL.pickClosest(mode, flags, pickShape);
	}
	// System.out.println(" -- pickInfo is " + pickInfo);
	
	return pickInfo;
    }

    /** Get the first node of a certain type up the SceneGraphPath 
     *@param type the type of node we are interested in
     *@return a Node object
     *
     * @exception NullPointerException if pickInfo does not contain a
     * Scenegraphpath or a picked node
     */

    public Node getNode (PickInfo pickInfo, int type) {       

	// System.out.println("pickInfo is " + pickInfo);

	if (pickInfo == null) {
	    return null;
        }
	
	SceneGraphPath sgp = pickInfo.getSceneGraphPath();
	Node pickedNode = pickInfo.getNode();
	// System.out.println("sgp = " + sgp + " pickedNode = " + pickedNode);


	/* 
	 *  Do not check for null for pickNode and sgp.
	 *  Will throw NPE if pickedNode or sgp isn't set in pickInfo  
	 */
	
        if ((pickedNode instanceof Shape3D) && ((type & TYPE_SHAPE3D) != 0)){
	    if (debug) System.out.println("Shape3D found");
	    return pickedNode;
	} 
	else if ((pickedNode instanceof Morph) && ((type & TYPE_MORPH) != 0)){
	    if (debug) System.out.println("Morph found"); 
	    return pickedNode;
	}
	else {
	    for (int j=sgp.nodeCount()-1; j>=0; j--){
		Node pNode = sgp.getNode(j); 
		if (debug) System.out.println("looking at node " + pNode);
	    
		if ((pNode instanceof Primitive) &&
		    ((type & TYPE_PRIMITIVE) != 0)){
		    if (debug) System.out.println("Primitive found");
		    return pNode;
		}
		else if ((pNode instanceof Link) && ((type & TYPE_LINK) != 0)){
		    if (debug) System.out.println("Link found");
		    return pNode;
		}
		else if ((pNode instanceof Switch) && ((type & TYPE_SWITCH) != 0)){
		    if (debug) System.out.println("Switch found");
		    return pNode;
		}
		else if ((pNode instanceof TransformGroup) &&
			 ((type & TYPE_TRANSFORM_GROUP) != 0)){
		    if (debug) System.out.println("xform group found");
		    return pNode;
		}
		else if ((pNode instanceof BranchGroup) &&
			 ((type & TYPE_BRANCH_GROUP) != 0)){
		    if (debug) System.out.println("Branch group found");
		    return pNode;
		}
		else if ((pNode instanceof Group) && ((type & TYPE_GROUP) != 0)){
		    if (debug) System.out.println("Group found");
		    return pNode;
		}	     
	    }
	}
	return null; // should not be reached
    }


} // PickTool





