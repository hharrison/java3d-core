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

/*
New Base level methods:

ISSUE :
        How about PickPoint and PickSegment ?

DONE :
	PickShape generatePickRay(int x, int y)

	SceneGraphPath[] pickAll(int x, int y)
	SceneGraphPath[] pickAllSorted(int x, int y)
	SceneGraphPath   pickAny(int x, int y)
	SceneGraphPath   pickClosest(int x, int y)

	Node getPickedNode(SceneGraphPath, flag)
	Node getPickedNode(SceneGraphPath, flag, int count)
	       where flag can be any combo of:
		       Group, Morph, Primitive, Shape3D,
		       TransformGroup, Switch


TODO :
	SceneGraphPath[] pickGeomAll(int x, int y)
        SceneGraphPath[] pickGeomAllSorted(int x, int y)
        SceneGraphPath   pickGeomAny(int x, int y)
	SceneGraphPath   pickGeomClosest(int x, int y)

	bool intersect(SceneGraphPath, PickShape)


        Eventually:
        	getClosestVtx(ScenGraphPath, PickShape)


Misc:
	Mouse should stay on top of object it is dragging

	*/

package com.sun.j3d.utils.behaviors.picking;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.media.j3d.*;
import com.sun.j3d.utils.geometry.Primitive;
import javax.vecmath.*;

/*
 * Contains methods to aid in picking.  A PickObject is created
 * for a given Canvas3D and a BranchGroup.  SceneGraphObjects
 * under the specified BranchGroup can then be checked to determine
 * if they have been picked.
 */

/**
 * @deprecated As of Java 3D version 1.2, replaced by
 * <code>com.sun.j3d.utils.picking.PickCanvas</code>
 *
 * @see com.sun.j3d.utils.picking.PickCanvas
 */

public class PickObject extends Object {

  // Have to rethink what to support. Is this complete.
  
  /**
   * A flag to indicate to the pickNode method to return a
   * <code>Shape3D</code> node from 
   * a given <code>SceneGraphPath</code>. 
   *
   * @see PickObject#pickNode 
   */
  public static final int SHAPE3D = 0x1;

  /**
   * A flag to indicate to the pickNode method to return a
   * <code>Morph</code> node from 
   * a given <code>SceneGraphPath</code>. 
   *
   * @see PickObject#pickNode 
   */
  public static final int MORPH = 0x2;

  /**
   * A flag to indicate to the pickNode method to return a
   * <code>Primitive</code> node 
   * from a given <code>SceneGraphPath</code>. 
   *
   * @see PickObject#pickNode 
   */
  public static final int PRIMITIVE = 0x4;

  /**
   * A flag to indicate to the pickNode method to return a
   * <code>Link</code> node from 
   * a given <code>SceneGraphPath</code>. 
   *
   * @see PickObject#pickNode 
   */
  public static final int LINK = 0x8;

  /**
   * A flag to indicate to the pickNode method to return a
   * <code>Group</code> node from 
   * a given <code>SceneGraphPath</code>. 
   *
   * @see PickObject#pickNode 
   */
  public static final int GROUP = 0x10;
  
  /**
   * A flag to indicate to the pickNode method to return a
   * <code>TransformGroup</code> 
   * node from a given <code>SceneGraphPath</code>. 
   *
   * @see PickObject#pickNode 
   */
  public static final int TRANSFORM_GROUP = 0x20;
 
  /**
   * A flag to indicate to the pickNode method to return a
   * <code>BranchGroup</code> 
   * node from a given <code>SceneGraphPath</code>. 
   *
   * @see PickObject#pickNode 
   */
  public static final int BRANCH_GROUP = 0x40;

  /**
   * A flag to indicate to the pickNode method to return a
   * <code>Switch</code> node from 
   * a given <code>SceneGraphPath</code>. 
   *
   * @see PickObject#pickNode 
   */
  public static final int SWITCH = 0x80;


  /**
   * Set this flag if you want to pick by geometry.
   */
  public static final int USE_GEOMETRY = 0x100;


  /**
   * Set this flag if you want to pick by bounds.
   */
  public static final int USE_BOUNDS = 0x200;
  
  BranchGroup pickRoot;
  Canvas3D canvas;
  Point3d origin = new Point3d();
  Vector3d direction = new Vector3d();
  PickRay pickRay = new PickRay();
  SceneGraphPath sceneGraphPath = null;  
  SceneGraphPath sceneGraphPathArr[] = null;  
  int pickBy;    // To pick by Bounds or Geometry.

  static final boolean debug = false;

  /**
   * Creates a PickObject.
   * @param c Current J3D canvas.
   * @param root The portion of the scenegraph for which picking is to occur
   * on.  It has to be a <code>BranchGroup</code>.
   *
   * @see BranchGroup
   * @see Canvas3D
   */ 
  public PickObject(Canvas3D c, BranchGroup root)
    {
      pickRoot = root;
      canvas = c;      
    }

  /**
   * Creates a PickRay that starts at the viewer position and points into
   * the scene in the direction of (xpos, ypos) specified in window space.
   *
   * @param xpos The value along the x-axis.
   * @param ypos The value along the y-axis.
   * @return A PickShape object that is the constructed PickRay.
   */ 
  public PickShape generatePickRay(int xpos, int ypos)
    {
            
      Transform3D motion=new Transform3D();
      Point3d eyePosn = new Point3d();
      Point3d mousePosn = new Point3d();
      Vector3d mouseVec=new Vector3d();
      
      canvas.getCenterEyeInImagePlate(eyePosn);
      canvas.getPixelLocationInImagePlate(xpos,ypos,mousePosn);
      if (canvas.getView().getProjectionPolicy() ==
                                View.PARALLEL_PROJECTION) {
          // Correct for the parallel projection: keep the eye's z
          // coordinate, but make x,y be the same as the mouse, this
          // simulates the eye being at "infinity"
	  eyePosn.x = mousePosn.x;
	  eyePosn.y = mousePosn.y;
      }

      canvas.getImagePlateToVworld(motion);

      if (debug) {
	System.out.println("mouse position " + xpos + " " + ypos);
	System.out.println("before, mouse " + mousePosn + " eye " + eyePosn);
      }
      
      motion.transform(eyePosn);
      motion.transform(mousePosn);
      mouseVec.sub(mousePosn, eyePosn);
      mouseVec.normalize();
      
      if (debug) {
	System.out.println(motion + "\n");
	System.out.println("after, mouse " + mousePosn + " eye " + eyePosn + 
			   " mouseVec " + mouseVec);
      }

      pickRay.set(eyePosn, mouseVec);
            
      return (PickShape) pickRay;      
      
    }
  
  /**
   * Returns an array referencing all the items that are pickable below the
   * <code>BranchGroup</code> (specified in the PickObject constructor) that
   * intersect with a ray that starts at the 
   * viewer position and points into the scene in the direction of (xpos, ypos)
   * specified in window space. The resultant array is unordered.
   *
   * @param xpos The value along the x-axis.
   * @param ypos The value along the y-axis.
   * @return The array of SceneGraphPath objects that contain Objects that
   *  were picked
   * If no pickable object is found <code>null</code> is returned..
   *
   * @see SceneGraphPath
   */  
  public SceneGraphPath[] pickAll(int xpos, int ypos)
    {
      pickRay = (PickRay) generatePickRay(xpos, ypos);
      sceneGraphPathArr = pickRoot.pickAll(pickRay);
      return sceneGraphPathArr;
    }
  
  /**
   * Returns a sorted array of references to all the Pickable items below the
   * <code>BranchGroup</code> (specified in the PickObject constructor) that
   * intersect with the ray that starts at the viewer 
   * position and points into the scene in the direction of (xpos, ypos) 
   * in the window space. 
   * Element [0] references the item closest to viewer.
   *
   * @param xpos The value along the x-axis.
   * @param ypos The value along the y-axis.
   * @return A sorted arrayof SceneGraphPath objects that contain Objects that
   * were picked.  The array is sorted from closest to farthest from the
   * viewer
   * If no pickable object is found <code>null</code> is returned..
   *
   * @see SceneGraphPath
   */
  public SceneGraphPath[] pickAllSorted(int xpos, int ypos)
    {
      pickRay = (PickRay) generatePickRay(xpos, ypos);
      sceneGraphPathArr = pickRoot.pickAllSorted(pickRay);
      return sceneGraphPathArr;
    }

  /**
   * Returns a reference to any item that is Pickable below the specified
   * <code>BranchGroup</code> (specified in the PickObject constructor) which
   *  intersects with the ray that starts at the viewer 
   * position and points into the scene in the direction of (xpos, ypos) in 
   * window space.
   *
   * @param xpos The value along the x-axis.
   * @param ypos The value along the y-axis.
   * @return A SceneGraphPath of an object that was picked.  This is not
   * guarenteed to return the same result for multiple picks
   * If no pickable object is found <code>null</code> is returned..
   *
   * @see SceneGraphPath
   */
  public SceneGraphPath pickAny(int xpos, int ypos)
    {
      pickRay = (PickRay) generatePickRay(xpos, ypos);
      sceneGraphPath = pickRoot.pickAny(pickRay);
      return sceneGraphPath;
    }

  /**
   * Returns a reference to the item that is closest to the viewer and is
   * Pickable below the <code>BranchGroup</code> (specified in the PickObject
   * constructor) which intersects with the ray that starts at 
   * the viewer position and points into the scene in the direction of
   * (xpos, ypos) in the window space.
   *
   * @param xpos The value along the x-axis.
   * @param ypos The value along the y-axis.
   * @return A SceneGraphPath which contains the closest pickable object.
   * If no pickable object is found, <code>null</code> is returned.
   *
   * @see SceneGraphPath
   */
  public SceneGraphPath pickClosest(int xpos, int ypos)
    {
      pickRay = (PickRay) generatePickRay(xpos, ypos);
      sceneGraphPath = pickRoot.pickClosest(pickRay);
      return sceneGraphPath;
    }


  /**
   * Returns an array referencing all the items that are pickable below the
   * <code>BranchGroup</code> (specified in the PickObject constructor) that
   * intersect with a ray that starts at the 
   * viewer position and points into the scene in the direction of (xpos, ypos)
   * specified in window space. The resultant array is unordered.
   *
   * @param xpos The value along the x-axis.
   * @param ypos The value along the y-axis.
   * @param flag Specifys picking by Geometry or Bounds.
   * @return The array of SceneGraphPath objects that contain Objects that
   *  were picked
   * If no pickable object is found <code>null</code> is returned..
   *
   * @see SceneGraphPath
   */  
  public SceneGraphPath[] pickAll(int xpos, int ypos, int flag)
    {

      if(flag == USE_BOUNDS) {
	return pickAll(xpos, ypos);
      }
      else if(flag == USE_GEOMETRY) {   
	return pickGeomAll(xpos, ypos);
      }
      else 
	return null;
    }
  
  /**
   * Returns a sorted array of references to all the Pickable items below the
   * <code>BranchGroup</code> (specified in the PickObject constructor) that
   * intersect with the ray that starts at the viewer 
   * position and points into the scene in the direction of (xpos, ypos) 
   * in the window space. 
   * Element [0] references the item closest to viewer.
   *
   * @param xpos The value along the x-axis.
   * @param ypos The value along the y-axis.
   * @param flag Specifys picking by Geometry or Bounds.
   * @return A sorted arrayof SceneGraphPath objects that contain Objects that
   * were picked.  The array is sorted from closest to farthest from the
   * viewer
   * If no pickable object is found <code>null</code> is returned..
   *
   * @see SceneGraphPath
   */
  public SceneGraphPath[] pickAllSorted(int xpos, int ypos, int flag)
    {

      if(flag == USE_BOUNDS) {
	return pickAllSorted(xpos, ypos);
      }
      else if(flag == USE_GEOMETRY) {   
	return pickGeomAllSorted(xpos, ypos);
      }
      else 
	return null;

    }

  /**
   * Returns a reference to any item that is Pickable below the specified
   * <code>BranchGroup</code> (specified in the PickObject constructor) which
   *  intersects with the ray that starts at the viewer 
   * position and points into the scene in the direction of (xpos, ypos) in 
   * window space.
   *
   * @param xpos The value along the x-axis.
   * @param ypos The value along the y-axis.
   * @param flag Specifys picking by Geometry or Bounds.
   * @return A SceneGraphPath of an object that was picked.  This is not
   * guarenteed to return the same result for multiple picks
   * If no pickable object is found <code>null</code> is returned..
   *
   * @see SceneGraphPath
   */
  public SceneGraphPath pickAny(int xpos, int ypos, int flag)
    {

      if(flag == USE_BOUNDS) {
	return pickAny(xpos, ypos);
      }
      else if(flag == USE_GEOMETRY) {   
	return pickGeomAny(xpos, ypos);
      }
      else 
	return null;
    }

  /**
   * Returns a reference to the item that is closest to the viewer and is
   * Pickable below the <code>BranchGroup</code> (specified in the PickObject
   * constructor) which intersects with the ray that starts at 
   * the viewer position and points into the scene in the direction of
   * (xpos, ypos) in the window space.
   *
   * @param xpos The value along the x-axis.
   * @param ypos The value along the y-axis.
   * @param flag Specifys picking by Geometry or Bounds.
   * @return A SceneGraphPath which contains the closest pickable object.
   * If no pickable object is found, <code>null</code> is returned.
   *
   * @see SceneGraphPath
   */
  public SceneGraphPath pickClosest(int xpos, int ypos, int flag)
    {

      if(flag == USE_BOUNDS) {
	return pickClosest(xpos, ypos);
      }
      else if(flag == USE_GEOMETRY) {   
	return pickGeomClosest(xpos, ypos);
      }
      else 
	return null;
    }

  private SceneGraphPath[] pickGeomAll(int xpos, int ypos)
    {
      Node obj;
      int i, cnt=0;

      pickRay = (PickRay) generatePickRay(xpos, ypos);
      sceneGraphPathArr = pickRoot.pickAll(pickRay);
      
      if(sceneGraphPathArr == null)
	return null;
      
      boolean found[] = new boolean[sceneGraphPathArr.length];

      for(i=0; i<sceneGraphPathArr.length; i++) {
        obj = sceneGraphPathArr[i].getObject();
	if(obj instanceof Shape3D) {
	  found[i] = ((Shape3D) obj).intersect(sceneGraphPathArr[i], 
					       (PickShape) pickRay);	  
	} else if(obj instanceof Morph) {
	  found[i] = ((Morph) obj).intersect(sceneGraphPathArr[i], 
					     (PickShape) pickRay); 
	}
	if(found[i] == true)
	  cnt++;	
      }
      
      if(cnt == 0)
	return null;

      SceneGraphPath newSceneGraphPathArr[] = new SceneGraphPath[cnt];

      cnt = 0; // reset for reuse.
      for(i=0; i<sceneGraphPathArr.length; i++) {
	if(found[i] == true)
	  newSceneGraphPathArr[cnt++] = sceneGraphPathArr[i];
      }
      
      return newSceneGraphPathArr;
    }

  private double distance[];

  private SceneGraphPath[] pickGeomAllSorted(int xpos, int ypos)
    {
      Node obj;
      int i, cnt=0;
      double dist[] = new double[1];

      // System.out.print("In pickGeomAllSorted\n");
      pickRay = (PickRay) generatePickRay(xpos, ypos);
      sceneGraphPathArr = pickRoot.pickAll(pickRay);
      
      if(sceneGraphPathArr == null)
	return null;
      
      boolean found[] = new boolean[sceneGraphPathArr.length];
      double distArr[] = new double[sceneGraphPathArr.length];

      for(i=0; i<sceneGraphPathArr.length; i++) {
        obj = sceneGraphPathArr[i].getObject();
	if(obj instanceof Shape3D) {
	  found[i] = ((Shape3D) obj).intersect(sceneGraphPathArr[i], 
					       pickRay, dist);
	  distArr[i] = dist[0];
	} else if(obj instanceof Morph) {
	  found[i] = ((Morph) obj).intersect(sceneGraphPathArr[i], 
					     pickRay, dist);
	  distArr[i] = dist[0];
	}
	if(found[i] == true)
	  cnt++;	
      }
      
      if(cnt == 0)
	return null;

      SceneGraphPath newSceneGraphPathArr[] = new SceneGraphPath[cnt];
      distance = new double[cnt];

      cnt = 0; // reset for reuse.
      for(i=0; i<sceneGraphPathArr.length; i++) {
	if(found[i] == true) {
	  newSceneGraphPathArr[cnt] = sceneGraphPathArr[i];
	  distance[cnt++] = distArr[i];
	}
      }
      
      return sort(newSceneGraphPathArr);
    }

  
  private SceneGraphPath pickGeomClosest(int xpos, int ypos)
    {
      SceneGraphPath sgpArr[] = pickGeomAllSorted(xpos, ypos);
      
      if (sgpArr == null)
	return null;
      
      return sgpArr[0];    
    }


  private SceneGraphPath pickGeomAny(int xpos, int ypos)
    {
      Node obj;
      int i;

      pickRay = (PickRay) generatePickRay(xpos, ypos);
      sceneGraphPathArr = pickRoot.pickAll(pickRay);
      for(i=0; i<sceneGraphPathArr.length; i++) {
        obj = sceneGraphPathArr[i].getObject();
	if(obj instanceof Shape3D) {
	  if(((Shape3D) obj).intersect(sceneGraphPathArr[i],(PickShape) pickRay))
	    return sceneGraphPathArr[i];
	} else if(obj instanceof Morph) {
	  if(((Morph) obj).intersect(sceneGraphPathArr[i],(PickShape) pickRay))
	    return sceneGraphPathArr[i];
	}
      }
      
      return null;
    }


  /**
   * Sort the elements in sgpArr and
   * return the sorted list in SceneGraphPath
   *
   * Sorts on the distance but also adjusts an array of positions
   * this allows the sort to operate on small data elements rather
   * than the possibly large SceneGraphPath
   *
   * Initial implementation is a Quick Sort
   */
  
  private int position[];
  
  private SceneGraphPath[] sort(SceneGraphPath sgpArr[]) {
    
    if (sgpArr == null) 
      return null;
    
    SceneGraphPath sorted[] = new SceneGraphPath[sgpArr.length];
    position = new int[sgpArr.length];
        	
    for(int i=0; i<sgpArr.length; i++) {
      position[i]=i;
    }
    
    
    /*
      System.out.println("Before Sort :");
      for(int i=0; i<distance.length; i++) {
      System.out.println("pos " + position[i] +" dist "+ distance[i] + 
      " sgp "+ sgpArr[i]);
      }
      */

    quicksort( 0, distance.length-1 );
    
    for(int i=0; i<distance.length; i++) {
      sorted[i]= sgpArr[position[i]];
    }

    /*    
	  System.out.println("\nAfter Sort :");
	  for(int i=0; i<distance.length; i++) {
	  System.out.println("pos " + position[i] +" dist "+ distance[i] + 
	  " sorted sgp "+ sorted[i]);
	  }
	  */

    return sorted;
  }
  
  
  private final void quicksort( int l, int r ) {
    int p,i,j;
    double tmp,k;
    
    i = l;
    j = r;
    k = distance[(l+r) / 2];
    do {
      while (distance[i]<k) i++;
      while (k<distance[j]) j--;
      if (i<=j) {
	tmp = distance[i];
	distance[i] =distance[j];
	distance[j] = tmp;
	
	p=position[i];
	position[i]=position[j];
	position[j]=p;
	i++;
	j--;
      }
    } while (i<=j);
    
    if (l<j) quicksort(l,j);
    if (l<r) quicksort(i,r);
  }


  /**
   * Returns a reference to a Pickable Node that
   * is of the specified type
   * that is contained in the specified SceneGraphPath.
   * If more than one node of the same type is encountered, the node
   * closest to the terminal node of SceneGraphPath will be returned.
   *
   * @param sgPath the SceneGraphPath to be traversed.
   * @param flags the Node types interested in picking.
   * @return the first occurrence of the specified Node type
   * starting from the terminal node of SceneGraphPath. 
   * If no pickable object is found of the  specifed types, 
   * <code>null</code> is returned.
   */
  public Node pickNode(SceneGraphPath sgPath, int flags)
    {
      
      if (sgPath != null) {	
	Node pickedNode = sgPath.getObject();
	
	if ((pickedNode instanceof Shape3D) && ((flags & SHAPE3D) != 0)){
	  if (debug) System.out.println("Shape3D found");
	  return pickedNode;
	} 
	else if ((pickedNode instanceof Morph) && ((flags & MORPH) != 0)){
	  if (debug) System.out.println("Morph found"); 
	  return pickedNode;
	}
	else {	  
	  for (int j=sgPath.nodeCount()-1; j>=0; j--){
	    pickedNode = sgPath.getNode(j); 
	    if (debug) System.out.println("looking at node " + pickedNode);
	    
	    if ((pickedNode instanceof Primitive) &&
		((flags & PRIMITIVE) != 0)){
	      if (debug) System.out.println("Primitive found");
	      return pickedNode;
	    }
	    else if ((pickedNode instanceof Link) && ((flags & LINK) != 0)){
	      if (debug) System.out.println("Link found");
	      return pickedNode;
	    }
	    else if ((pickedNode instanceof Switch) && ((flags & SWITCH) != 0)){
	      if (debug) System.out.println("Switch found");
	      return pickedNode;
	    }
	    else if ((pickedNode instanceof TransformGroup) &&
		     ((flags & TRANSFORM_GROUP) != 0)){
	      if (debug) System.out.println("xform group found");
	      return pickedNode;
	    }
	    else if ((pickedNode instanceof BranchGroup) &&
                     ((flags & BRANCH_GROUP) != 0)){
	      if (debug) System.out.println("Branch group found");
	      return pickedNode;
	    }
	    else if ((pickedNode instanceof Group) && ((flags & GROUP) != 0)){
	      if (debug) System.out.println("Group found");
	      return pickedNode;
	    }	     
	  }
	  
	  if (pickedNode == null)
	    if (debug) System.out.println("ERROR: null SceneGraphPath");
	}

      }
      
      return null;
      
    }


  /**
   * Returns a reference to a Pickable Node that
   * is of the specified type
   * that is contained in the specified SceneGraphPath.
   * The Node returned is the nth <code>occurrence</code>
   * of a Node that is of the specified type.
   *
   * @param sgPath the SceneGraphPath to be traversed.
   * @param flags the Node types interested.
   * @param occurrence the occurrence of a Node that
   * matches the specified type to return.  An <code>occurrence</code> of
   * 1 means to return the first occurrence of that object type (the object
   * closest to the Locale).
   * @return the nth <code>occurrence</code> of a Node
   * of type <code>flags</code>, starting from the Locale. If no pickable object is 
   * found, <code>null</code> is returned.
   */  
  public Node pickNode(SceneGraphPath sgPath, int flags, int occurrence)
    {
      int curCnt=0;
      
      if (sgPath != null) {	
	Node pickedNode = sgPath.getObject();
	
	// Shape3D and Morph are leaf nodes and have no children. It doesn't
        // make sense to do occurrence check here. We'll just return it for now.
	if ((pickedNode instanceof Shape3D) && ((flags & SHAPE3D) != 0)){
	  if (debug) System.out.println("Shape3D found");
	  return pickedNode;
	} else if ((pickedNode instanceof Morph) && ((flags & MORPH) != 0)){
	  if (debug) System.out.println("Morph found"); 
	  return pickedNode;
	}
	else {	  
	  for (int j = 0; j < sgPath.nodeCount(); j++){
	    pickedNode = sgPath.getNode(j);
	    if (debug) System.out.println("looking at node " + pickedNode);
	    
	    if ((pickedNode instanceof Group) && ((flags & GROUP) != 0)){
	      if (debug) System.out.println("Group found"); 
	      curCnt++;
	      if(curCnt == occurrence)
		return pickedNode;
	    }
	    else if ((pickedNode instanceof BranchGroup) &&
                    ((flags & BRANCH_GROUP) != 0)){
	      if (debug) System.out.println("Branch group found");
	      curCnt++;
	      if(curCnt == occurrence)
		return pickedNode;	      
	    }
	    else if ((pickedNode instanceof TransformGroup) &&
                    ((flags & TRANSFORM_GROUP) != 0)){
	      if (debug) System.out.println("xform group found");
	      curCnt++;
	      if(curCnt == occurrence)
		return pickedNode;
	    }
	    else if ((pickedNode instanceof Primitive) &&
                    ((flags & PRIMITIVE) != 0)){
	      if (debug) System.out.println("Primitive found");
	      curCnt++;
	      if(curCnt == occurrence)
		return pickedNode;
	    }
	    else if ((pickedNode instanceof Link) && ((flags & LINK) != 0)){
	      if (debug) System.out.println("Link found");
	      curCnt++;	
	      if(curCnt == occurrence)
		return pickedNode;
	    }
	  }
	  
	  if (pickedNode == null)
	    if (debug) System.out.println("ERROR: null SceneGraphPath");
	}

      }
      
      return null;
      
    }

}

