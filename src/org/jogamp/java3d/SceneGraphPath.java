/*
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

package org.jogamp.java3d;

import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point4d;

/**
 * A SceneGraphPath object represents the path from a Locale to a
 * terminal node in the scene graph.  This path consists of a Locale, a
 * terminal node, and an array of internal nodes that are in the path
 * from the Locale to the terminal node.  The terminal node may be
 * either a Leaf node or a Group node.  A valid SceneGraphPath must
 * uniquely identify a specific instance of the terminal node.  For
 * nodes that are not under a SharedGroup, the minimal SceneGraphPath
 * consists of the Locale and the terminal node itself.  For nodes that
 * are under a SharedGroup, the minimal SceneGraphPath consists of the
 * Locale, the terminal node, and a list of all Link nodes in the path
 * from the Locale to the terminal node.  A SceneGraphPath may optionally
 * contain other interior nodes that are in the path.
 * A SceneGraphPath is verified for correctness and uniqueness when
 * it is sent as an argument to other methods of Java 3D.
 * <p>
 * In the array of internal nodes, the node at index 0 is the node
 * closest to the Locale.  The indices increase along the path to the
 * terminal node, with the node at index length-1 being the node closest
 * to the terminal node.  The array of nodes does not contain either the
 * Locale (which is not a node) or the terminal node.
 * <p>
 * When a SceneGraphPath is returned from the picking or collision
 * methods of Java 3D, it will also contain the value of the
 * LocalToVworld transform of the terminal node that was in effect at
 * the time the pick or collision occurred.
 * Note that ENABLE_PICK_REPORTING and ENABLE_COLLISION_REPORTING are
 * disabled by default.  This means that the picking and collision
 * methods will return the minimal SceneGraphPath by default.
 *
 * @see Node#ENABLE_PICK_REPORTING
 * @see Node#ENABLE_COLLISION_REPORTING
 * @see BranchGroup#pickAll
 * @see BranchGroup#pickAllSorted
 * @see BranchGroup#pickClosest
 * @see BranchGroup#pickAny
 */

public class SceneGraphPath {

    Locale root = null;
    Node[] interior = null;
    Node item = null;
    Transform3D transform = new Transform3D();

    // Intersect Point for item when picked
    Point3d intersectPoint = new Point3d();

    double pickDistance;                    // distance to pick location

    /**
     * Constructs a SceneGraphPath object with default parameters.
     * The default values are as follows:
     * <ul>
     * root : null<br>
     * object : null<br>
     * list of (interior) nodes : null<br>
     * transform : identity<br>
     * </ul>
     */
    public SceneGraphPath() {
	// Just use defaults
    }

    /**
     * Constructs a new SceneGraphPath object.
     * @param root the Locale object of this path
     * @param object the terminal node of this path
     */
    public SceneGraphPath(Locale root, Node  object) {

        this.item = object;
	this.root = root;
    }

    /**
     * Constructs a new SceneGraphPath object.
     * @param root the Locale object of this path
     * @param nodes an array of node objects in the path from
     * the Locale to the terminal node
     * @param object the terminal node of this path
     */
    public SceneGraphPath(Locale root, Node nodes[], Node  object) {

        this.item = object;
	this.root = root;
	this.interior = new Node[nodes.length];
	for (int i = 0; i < nodes.length; i++)
	    this.interior[i] = nodes[i];
    }


    /**
     * Constructs a new SceneGraphPath object
     * @param sgp  the SceneGraphPath to copy from
     */
    SceneGraphPath(SceneGraphPath sgp) {
	set(sgp);
    }

    /**
     * Sets this path's values to that of the specified path.
     * @param newPath the SceneGraphPath to copy
     */
    public final void set(SceneGraphPath newPath) {
	this.root = newPath.root;
	this.item = newPath.item;
	this.transform.set(newPath.transform);
	if(newPath.interior != null && newPath.interior.length > 0) {
	    interior = new Node[newPath.interior.length];
	    for (int i = 0; i < interior.length; i++)
		this.interior[i] = newPath.interior[i];
	}
	else
	    interior = null;
    }

    /**
     * Sets this path's Locale to the specified Locale.
     * @param newLocale The new Locale
     */
    public final void setLocale(Locale newLocale) {
	root = newLocale;
    }

    /**
     * Sets this path's terminal node to the specified node object.
     * @param object the new terminal node
     */
    public final void setObject(Node  object) {
	    this.item = object;
    }

    /**
     * Sets this path's node objects to the specified node objects.
     * @param nodes an array of node objects in the path from
     * the Locale to the terminal node
     */
    public final void setNodes(Node nodes[]) {

	if(nodes != null && nodes.length > 0) {
	    interior = new Node[nodes.length];
	    for (int i = 0; i < nodes.length; i++)
		this.interior[i] = nodes[i];
	}
	else
	    interior = null;
    }

    /**
     * Replaces the node at the specified index with newNode.
     * @param index the index of the node to replace
     * @param newNode the new node
     * @exception NullPointerException if the node array pointer is null.
     *
     */
    public final void setNode(int index, Node newNode) {
      if(interior == null)
	throw new NullPointerException(J3dI18N.getString("SceneGraphPath0"));

      interior[index] = newNode;
    }

    /**
     * Sets the transform component of this SceneGraphPath to the value of
     * the passed transform.
     * @param trans the transform to be copied. trans should be the
     * localToVworld matrix of this SceneGraphPath object.
     */
     public final void setTransform(Transform3D trans) {
	 transform.set(trans);
     }

    /**
      *  Returns a copy of the transform associated with this SceneGraphPath;
      *  returns null if there is no transform associated.
      *  If this SceneGraphPath was returned by a Java 3D picking or
      *  collision method, the local coordinate to virtual world
      *  coordinate transform for this scene graph object at the
      *  time of the pick or collision is recorded.
      *  @return the local to VWorld transform
      */
    public final Transform3D getTransform() {
	return new Transform3D(transform);
    }

    /**
     * Retrieves the path's Locale
     * @return this path's Locale
     */
    public final Locale getLocale() {
	return this.root;
    }

    /**
     * Retrieves the path's terminal node object.
     * @return the terminal node
     */
    public final Node getObject() {
	return this.item;
    }

    /**
     * Retrieves the number of nodes in this path.  The number of nodes
     * does not include the Locale or the terminal node object itself.
     * @return a count of the number of nodes in this path
     */
    public final int nodeCount() {
        if(interior == null)
	  return 0;
	return interior.length;
    }

    /**
     * Retrieves the node at the specified index.
     * @param index the index specifying which node to retrieve
     * @return the specified node
     */
    public final Node getNode(int index) {
      if(interior == null)
	throw new
          ArrayIndexOutOfBoundsException(J3dI18N.getString("SceneGraphPath1"));
	return interior[index];
    }

    /**
     * Returns true if all of the data members of path testPath are
     * equal to the corresponding data members in this SceneGraphPath and
     * if the values of the transforms is equal.
     * @param testPath the path we will compare this object's path against.
     * @return  true or false
     */
    public boolean equals(SceneGraphPath testPath) {
	boolean result = true;
        try {

	  if(testPath == null || root != testPath.root || item != testPath.item)
	    return false;

	  result = transform.equals(testPath.transform);

	  if(result == false)
	    return false;

	  if(interior == null || testPath.interior == null) {
	    if(interior != testPath.interior)
	      return false;
	    else
	      result = (root == testPath.root && item == testPath.item);

	  } else  {
	    if (interior.length == testPath.interior.length) {
	      for (int i = 0; i < interior.length; i++)
		if (interior[i] !=  testPath.interior[i]) {
		    return false;
		}
	    }
	    else
		return false;
	  }

        }
        catch (NullPointerException e2) {return false;}

        return result;
    }

    /**
     * Returns true if the Object o1 is of type SceneGraphPath and all of the
     * data members of o1 are equal to the corresponding data members in
     * this SceneGraphPath  and if the values of the transforms is equal.
     * @param o1 the object we will compare this SceneGraphPath's path against.
     * @return  true or false
     */
    @Override
    public boolean equals(Object o1) {
	boolean result = true;

        try {
	   SceneGraphPath testPath = (SceneGraphPath)o1;
           if(testPath == null || root != testPath.root || item != testPath.item)
	     return false;

	   result = transform.equals(testPath.transform);

	   if(result == false)
	     return false;

	   if(interior == null || testPath.interior == null) {
             if(interior != testPath.interior)
	       return false;
             else
	       result = (root == testPath.root && item == testPath.item);

           } else  {
	     if (interior.length == testPath.interior.length) {
	       for (int i = 0; i < interior.length; i++)
		 if (interior[i] !=  testPath.interior[i]) {
		     return false;
		 }
	     }
	     else
		 return false;
           }

 	   return result;
        }
        catch (NullPointerException e2) {return false;}
        catch (ClassCastException   e1) {return false;}
    }


    /**
     * Returns a hash number based on the data values in this
     * object. Two different SceneGraphPath objects with identical data
     * values (ie, returns true for trans.equals(SceneGraphPath) ) will
     * return the same hash number.  Two Paths with different data members
     * may return the same hash value, although this is not likely.
     * @return the integer hash value
     */
    @Override
    public int hashCode() {
       HashKey key = new HashKey(250);
       // NOTE: Needed to add interior != null because this method is called
       // by object.toString() when interior is null.
       if(interior != null && item != null) {
             for(int i=0; i<interior.length; i++) {
                 key.append(LinkRetained.plus).append( item.toString() );
             }
       }
       return( key.hashCode() + transform.hashCode() );
    }

   /**
     * Determines whether two SceneGraphPath objects represent the same
     * path in the scene graph; either object might include a different
     * subset of internal nodes; only the internal link nodes, the Locale,
     * and the Node itself are compared.  The paths are not validated for
     * correctness or uniqueness.
     * @param testPath  the SceneGraphPath to be compared to this SceneGraphPath
     * @return true or false
     */
    public final boolean isSamePath(SceneGraphPath testPath) {
         int count=0, i;

         if(testPath == null || testPath.item != this.item || root != testPath.root)
             return false;

         if(interior != null && testPath.interior != null) {
                for(i=0 ; i<interior.length ; i++)  {
                     if(interior[i] instanceof Link)  {
                         // found Link in this, now check for matching in testPath
                         while(count < testPath.interior.length) {
                             if(testPath.interior[count] instanceof Link) {
                                  if(testPath.interior[count] != interior[i]) {
				      return false;
                                  }
                                  count++;
                                  break;
                             }
                             count++;
                             // if true, this had an extra Link
                             if(count == testPath.interior.length)
				 return false;
                         }
                     }
                }
                // make sure testPath doesn't have any extra Links
                while(count < testPath.interior.length) {
                    if(testPath.interior[count] instanceof Link)
			return false;
                    count++;
                }
         } else if(interior != testPath.interior)  // ==> they are not both null
	          return false;

         return true;
    }

    /**
     * Returns a string representation of this object;
     * the string contains the class names of all Nodes in the SceneGraphPath,
     * the toString() method of any associated user data provided by
     * SceneGraphObject.getUserData(), and also prints out the transform,
     * if it is not null.
     *  @return String representation of this object
     */
    @Override
    public String toString() {

	StringBuffer str = new StringBuffer();
	Object obj;

	if(root == null && interior == null && item == null)
	    return (super.toString());

	if(root != null)
	    str.append(root + " : ");

	if(interior != null) {
	    for(int i=0; i<interior.length; i++) {

		str.append( interior[i].getClass().getName());
		obj = interior[i].getUserData();
		if(obj == null)
		    str.append(" : ");
		else
		    str.append(", " + obj + " : ");
	    }
	}

	if(item != null) {
	    //	       str.append( item + ", "+ item.getUserData() + "--"+intersectPoint );
	    str.append( item.getClass().getName() );
	    obj = item.getUserData();
	    if(obj != null)
		str.append(", " + obj);

	    try {
		if (item.getClass().getName().equals("org.jogamp.java3d.Shape3D"))
		    str.append( ((Shape3D)item).getGeometry() );
	    }
	    catch( CapabilityNotSetException e) {}
	}

	str.append("\n" + "LocalToVworld Transform:\n" + transform);

	return new String(str);
    }

    /**
     * Determine if this SceneGraphPath is unique and valid
     * The graph don't have to be live for this checking.
     * Set Locale when it is null.
     * Only the essential link node which led to the Locale
     * is validated.
     */
    boolean validate() {
	NodeRetained node = (NodeRetained) item.retained;

	Locale locale = node.locale;

	if (root != null) {
	    if (item.isLive()) {
		if (locale != root) {
		    return false;
		}
	    }
	} else {
	    root = locale;
	}

	int idx =  (interior == null ? 0: interior.length);

	do {
	    if (node instanceof SharedGroupRetained) {
		if (interior == null)
		    return false;
		while (--idx > 0) {
		    if (((SharedGroupRetained)node).parents.contains(interior[idx].retained)) {
			break;
		    }
		}
		if (idx < 0) {
		    return false;
		}
		node = (NodeRetained) interior[idx].retained;
	    } else {
		node = node.parent;
	    }
	} while (node != null);

	return true;
    }


    // return key of this path or null is not in SharedGroup
    void getHashKey(HashKey key) {
	if (interior != null) {
	    key.reset();
	    key.append(root.nodeId);
	    for(int i=0; i<interior.length; i++) {
		Node node = interior[i];

		if (!node.isLive()) {
		    throw new RuntimeException(J3dI18N.getString("SceneGraphPath3"));
		}

		NodeRetained nodeR = (NodeRetained) node.retained;
		if (nodeR.nodeType == NodeRetained.LINK) {
		    key.append("+").append(nodeR.nodeId);
		}
	    }
	}
    }

    /**
     * Determines whether this SceneGraphPath is unique and valid.  The
     * verification determines that all of the nodes are live, that the
     * specified path is unique, that the correct Locale is specified, and
     * that there is a Node specified.
     */
    boolean validate(HashKey key) {

	int i;

	// verify that there is at least a Locale and Node specified
	if( root == null )
	    throw new IllegalArgumentException(J3dI18N.getString("SceneGraphPath2"));

	if( item == null )
	    throw new IllegalArgumentException(J3dI18N.getString("SceneGraphPath10"));

	// verify liveness
	if( !item.isLive() )
	    throw new IllegalArgumentException(J3dI18N.getString("SceneGraphPath3"));

	try {
	    getHashKey(key);
	} catch (RuntimeException ex) {
	    throw new IllegalArgumentException(ex.getMessage());
	}

	// The rest of the code verifies uniqueness; it traverses the retained
	// hierarchy of the scene graph.  This could be problematic later in
	// when certain compile mode optimizations are added. */

	NodeRetained bottomNR, currentNR, nextNR=null;
	Node currentNode;
	int count = 0;

	// Need to traverse the retained hierarchy on a live scene graph
	//  from bottom to top
	//
	// bottomNR = last verified node; as nodes are verified, bottomNR
	//            moves up the scen graph
	// nextNR = Next node that the user has specified after bottomNR
	// currentNR = current node; is changing as it covers all the
	//             nodes from bottomNR to nextNR

	// If the parent of a NodeRetained is null, we know that the parent
	// is either a BranchGroupRetained at the top of a scene graph or
	// it is a SharedGroupRetained, potentially with multiple parents.

	bottomNR = (NodeRetained)(item.retained);

	if(interior != null) {
	    for(i=interior.length-1; i >=0  ; i--) {
		nextNR = (NodeRetained)(interior[i].retained);
		currentNR = bottomNR.parent;
		if(currentNR == null  &&  bottomNR instanceof SharedGroupRetained) {
		    if(((SharedGroupRetained)(bottomNR)).parents.contains(nextNR) )
			currentNR = nextNR;
		    else
			throw new IllegalArgumentException(J3dI18N.getString("SceneGraphPath5"));

		}

		while(currentNR != nextNR) {
		    if(currentNR == null) {
			throw new IllegalArgumentException(J3dI18N.getString("SceneGraphPath11"));
		    }

		    if(currentNR instanceof SharedGroupRetained) {
			if(((SharedGroupRetained)
			    (currentNR)).parents.contains(nextNR) )
			    currentNR = nextNR;
			else
			    throw new IllegalArgumentException(J3dI18N.getString("SceneGraphPath5"));

		    } else {
			currentNR = currentNR.parent;
		    }
		}
		bottomNR = currentNR;
	    }
	}

	//  Now go from bottomNR to Locale
	currentNR = bottomNR.parent;
	if(currentNR == null && bottomNR instanceof SharedGroupRetained) {
	    throw new IllegalArgumentException(J3dI18N.getString("SceneGraphPath5"));
	}

	while(currentNR != null) {
	    if(currentNR instanceof LinkRetained) {
		throw new IllegalArgumentException(J3dI18N.getString("SceneGraphPath5"));
	    }

	    bottomNR = currentNR;
	    currentNR = currentNR.parent;
	    if(currentNR == null && bottomNR instanceof SharedGroupRetained) {
		throw new IllegalArgumentException(J3dI18N.getString("SceneGraphPath5"));
	    }
	}

	// get the real BranchGroup from the BranchGroupRetained
	currentNode = (Node)(bottomNR.source);
	// now bottomNR should be a BranchGroup -- should try an assert here
	if(!root.branchGroups.contains(currentNode)) {
	    throw new IllegalArgumentException(J3dI18N.getString("SceneGraphPath9"));
	}

	return true;
    }

    /**
     * Returns the distance from the intersectPoint for item and
     * origin.
     */
    double getDistanceFrom( Point3d origin ) {
	return intersectPoint.distance(origin);
    }

    /**
     * Returns the distance of the pick
     */
    double getDistance() {
	return pickDistance;
    }

    final void setIntersectPoint( Point3d point ) {
	intersectPoint.set(point);
    }

    final void setIntersectPointDis( Point4d pickLocation ) {
	//	System.err.println( "setIntersectPointDis pickLocation= "+pickLocation);
	intersectPoint.x = pickLocation.x;
	intersectPoint.y = pickLocation.y;
	intersectPoint.z = pickLocation.z;
	pickDistance = pickLocation.w;
    }

    final Point3d getIntersectPoint() {
	return intersectPoint;
    }
}
