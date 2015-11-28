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

import java.util.Enumeration;

import org.jogamp.vecmath.Point3f;

/**
 * This class defines a distance-based LOD behavior node that operates on
 * a Switch group node to select one of the children of that Switch node
 * based on the distance of this LOD node from the viewer.
 * An array of <i>n</i> monotonically increasing distance values is
 * specified, such that distances[0] is associated with the highest level of
 * detail and distances[<i>n</i>-1] is associated with the lowest level of
 * detail.  Based on the actual distance from the viewer to
 * this DistanceLOD node, these <i>n</i>
 * distance values [0, <i>n</i>-1] select from among <i>n</i>+1
 * levels of detail [0, <i>n</i>].  If <i>d</i> is the distance from
 * the viewer to the LOD node, then the equation for determining
 * which level of detail (child of the Switch node) is selected is:
 * <p>
 * <ul>
 *     0, if <i>d</i> <= distances[0]
 * <br>
 *     <i>i</i>, if distances[<i>i</i>-1] < <i>d</i> <= distances[<i>i</i>]
 * <br>
 *     <i>n</i>, if d > distances[<i>n</i>-1]
 * </ul>
 * <p>
 * Note that both the position and the array of distances are
 * specified in the local coordinate system of this node.
 */
public class DistanceLOD extends LOD {

    private double distances[];
    private Point3f position = new Point3f(0.0f, 0.0f, 0.0f);

    // variables for processStimulus
    private Point3f center = new Point3f();
    private Point3f viewPosition = new Point3f();

    /**
     * Constructs and initializes a DistanceLOD node with default values.
     * Note that the default constructor creates a DistanceLOD object with
     * a single distance value set to 0.0 and is, therefore, not useful.
     */
    public DistanceLOD() {
	distances = new double[1];
	distances[0] = 0.0;
    }

    /**
     * Constructs and initializes a DistanceLOD node with the specified
     * array of distances and a default position of (0,0,0).
     * @param distances an array of values representing LOD cutoff distances
     */
    public DistanceLOD(float[] distances) {
	this.distances = new double[distances.length];

	for(int i=0;i<distances.length;i++) {
 	   this.distances[i] = (double)distances[i];
        }
    }

    /**
     * Constructs and initializes a DistanceLOD node with the specified
     * array of distances and the specified position.
     * @param distances an array of values representing LOD cutoff distances
     * @param position the position of this LOD node
     */
    public DistanceLOD(float[] distances, Point3f position) {
	this.distances = new double[distances.length];

	for(int i=0;i<distances.length;i++) {
 	   this.distances[i] = (double)distances[i];
        }
	this.position.set(position);
    }

    /**
     * Sets the position of this LOD node.  This position is specified in
     * the local coordinates of this node, and is
     * the position from which the distance to the viewer is computed.
     * @param position the new position
     */
    public void setPosition(Point3f position) {
	if (((NodeRetained)retained).staticTransform != null) {
	    ((NodeRetained)retained).staticTransform.transform.transform(
					position, this.position);
	} else {
	    this.position.set(position);
	}
    }

    /**
     * Retrieves the current position of this LOD node.  This position is
     * in the local coordinates of this node.
     * @param position the object that will receive the current position
     */
    public void getPosition(Point3f position) {
        if (((NodeRetained)retained).staticTransform != null) {
            Transform3D invTransform =
                ((NodeRetained)retained).staticTransform.getInvTransform();
            invTransform.transform(this.position, position);
        } else {
            position.set(this.position);
        }
    }

    /**
     * Returns a count of the number of LOD distance cut-off parameters.
     * Note that the number of levels of detail (children of the Switch node)
     * is one greater than the number of distance values.
     * @return a count of the LOD cut-off distances
     */
    public int numDistances() {
	return distances.length;
    }

    /**
     * Returns a particular LOD cut-off distance.
     * @param whichDistance an index specifying which LOD distance to return
     * @return the cut-off distance value associated with the index provided
     */
    public double getDistance(int whichDistance) {
	return distances[whichDistance];
    }

    /**
     * Sets a particular LOD cut-off distance.
     * @param whichDistance an index specifying which LOD distance to modify
     * @param distance the cut-off distance associated with the index provided
     */
    public void setDistance(int whichDistance, double distance) {
	     distances[whichDistance] = distance;
    }

    /**
     * Initialize method that sets up initial wakeup criteria.
     */
    @Override
    public void initialize() {
	// Insert wakeup condition into queue
	wakeupOn(wakeupFrame);
    }

    /**
     * Process stimulus method that computes appropriate level of detail.
     * @param criteria an enumeration of the criteria that caused the
     * stimulus
     */
    @Override
    public void processStimulus(Enumeration criteria) {


	// compute distance in virtual world
	View v = this.getView();
	if( v == null ) {
	    wakeupOn(wakeupFrame);
	    return;
	}

	ViewPlatform vp = v.getViewPlatform();
	if (vp == null) {
	    return;
	}

	// Handle stimulus
	double viewDistance = 0.0;
	int nSwitches,i,index=0;

	Transform3D localToWorldTrans = new Transform3D();

        localToWorldTrans.set(((NodeRetained)this.retained).getCurrentLocalToVworld());


	//  DistanceLOD's location in virutal world
	localToWorldTrans.transform( position, center);


	viewPosition.x = (float)((ViewPlatformRetained)vp.retained).schedSphere.center.x;
	viewPosition.y = (float)((ViewPlatformRetained)vp.retained).schedSphere.center.y;
	viewPosition.z = (float)((ViewPlatformRetained)vp.retained).schedSphere.center.z;
	viewDistance = center.distance( viewPosition);


	// convert distance into local coordinates
	viewDistance = viewDistance/localToWorldTrans.getDistanceScale();

	nSwitches = numSwitches();

	index = distances.length; // viewDistance > distances[n-1]

	if( viewDistance <= distances[0] ) {
	    index = 0;
	} else {
	    for (i=1; i < distances.length; i++) {
		if ((viewDistance > distances[i-1]) &&
		    (viewDistance <= distances[i])) {
		    index = i;
		    break;
		}
	    }
	}

	for(i=nSwitches-1; i>=0; i--) {
	    Switch sw = getSwitch(i);
	    // Optimize, this behavior is passive
	    // Note that we skip the capability check for getWhichChild()
	    if (((SwitchRetained) sw.retained).getWhichChild() !=
		index) {
		sw.setWhichChild(index);
	    }
	}
	// Insert wakeup condition into queue
	wakeupOn(wakeupFrame);

    }

    /**
     * Used to create a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see Node#duplicateNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    @Override
    public Node cloneNode(boolean forceDuplicate) {
        DistanceLOD d = new DistanceLOD();
        d.duplicateNode(this, forceDuplicate);
        return d;
    }


   /**
     * Copies all DistanceLOD information from
     * <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.<P>
     *
     * @param originalNode the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @exception RestrictedAccessException if this object is part of a live
     *  or compiled scenegraph.
     *
     * @see Node#duplicateNode
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    @Override
    void duplicateAttributes(Node originalNode, boolean forceDuplicate) {
        super.duplicateAttributes(originalNode, forceDuplicate);

	DistanceLOD lod = (DistanceLOD) originalNode;

        int numD = lod.numDistances();

	// No API available to set the size of this array after initialize
        this.distances = new double[numD];

        for (int i = 0; i < numD; i++)
            setDistance(i, lod.getDistance(i));

        Point3f p = new Point3f();
        lod.getPosition(p);
        setPosition(p);
    }

    void mergeTransform(TransformGroupRetained xform) {
	xform.transform.transform(position, position);
    }
}
