/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.util.Enumeration;
import java.util.Vector;

/**
 * An LOD leaf node is an abstract behavior class that operates on
 * a list of Switch group nodes to select one of the children of the
 * Switch nodes.
 * The LOD class is extended to implement various selection criteria.
 */

public abstract class LOD extends Behavior {

    /**
     * Wakeup condition for all LOD nodes
     */
    WakeupOnElapsedFrames wakeupFrame = new WakeupOnElapsedFrames(0, true);


    /**
     * The LOD Node's vector of switch nodes.
     */
    Vector switches = new Vector(5);

    /**
     * Constructs and initializes an LOD node.
     */
    public LOD() {
    }

    /**
     * Appends the specified switch node to this LOD's list of switches.
     * @param switchNode the switch node to add to this LOD's list of switches
     */
    public void addSwitch(Switch switchNode) {
	switches.addElement(switchNode);
    }

    /**
     * Replaces the specified switch node with the switch node provided.
     * @param switchNode the new switch node
     * @param index which switch node to replace
     */
    public void setSwitch(Switch switchNode, int index) {
	Switch sw = getSwitch(index);
	switches.setElementAt(switchNode, index);
    }

    /**
     * Inserts the specified switch node at specified index.
     * @param switchNode the new switch node
     * @param index position to insert new switch node at
     */
    public void insertSwitch(Switch switchNode, int index) {
	switches.insertElementAt(switchNode, index);
    }

    /**
     * Removes the switch node at specified index.
     * @param index which switch node to remove
     */
    public void removeSwitch(int index) {
	Switch sw = getSwitch(index);
	switches.removeElementAt(index);
    }

    /**
     * Returns the switch node specified by the index.
     * @param index which switch node to return
     * @return the switch node at location index
     */
    public Switch getSwitch(int index) {
	return (Switch) switches.elementAt(index);
    }

    /**
     * Returns the enumeration object of all switches.
     * @return the enumeration object of all switches
     */  
    public Enumeration getAllSwitches() {
        return switches.elements();
    }

    /**
     * Returns a count of this LOD's switches.
     * @return the number of switches controlled by this LOD
     */
    public int numSwitches() {
	return switches.size();
    }


    /**
     * Retrieves the index of the specified switch node in
     * this LOD node's list of switches.
     *
     * @param switchNode the switch node to be looked up.
     * @return the index of the specified switch node;
     * returns -1 if the object is not in the list.
     *
     * @since Java 3D 1.3
     */
    public int indexOfSwitch(Switch switchNode) {
	return switches.indexOf(switchNode);
    }


    /**
     * Removes the specified switch node from this LOD node's
     * list of switches.
     * If the specified object is not in the list, the list is not modified.
     *
     * @param switchNode the switch node to be removed.
     *
     * @since Java 3D 1.3
     */
    public void removeSwitch(Switch switchNode) {
	int index = switches.indexOf(switchNode);
	if (index >= 0)
	    removeSwitch(index);
    }


    /**
     * Removes all switch nodes from this LOD node.
     *
     * @since Java 3D 1.3
     */
    public void removeAllSwitches() {
	int numSwitches = switches.size();

	// Remove in reverse order to ensure valid indices
	for (int index = numSwitches - 1; index >= 0; index--) {
	    removeSwitch(index);
	}
    }


    /**
     * Copies all LOD information from
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
    void duplicateAttributes(Node originalNode, boolean forceDuplicate) {
        super.duplicateAttributes(originalNode, forceDuplicate);
	
	LOD lod = (LOD) originalNode;

        int numSwitch = lod.numSwitches();
        for (int i = 0; i < numSwitch; i++) {
            addSwitch(lod.getSwitch(i));
        }
    }

    /**
     * Callback used to allow a node to check if any nodes referenced
     * by that node have been duplicated via a call to <code>cloneTree</code>.
     * This method is called by <code>cloneTree</code> after all nodes in
     * the sub-graph have been duplicated. The cloned Leaf node's method
     * will be called and the Leaf node can then look up any node references
     * by using the <code>getNewObjectReference</code> method found in the
     * <code>NodeReferenceTable</code> object.  If a match is found, a
     * reference to the corresponding Node in the newly cloned sub-graph
     * is returned.  If no corresponding reference is found, either a
     * DanglingReferenceException is thrown or a reference to the original
     * node is returned depending on the value of the
     * <code>allowDanglingReferences</code> parameter passed in the
     * <code>cloneTree</code> call.
     * <p>
     * NOTE: Applications should <i>not</i> call this method directly.
     * It should only be called by the cloneTree method.
     *
     * @param referenceTable a NodeReferenceTableObject that contains the
     *  <code>getNewObjectReference</code> method needed to search for
     *  new object instances.
     * @see NodeReferenceTable
     * @see Node#cloneTree
     * @see DanglingReferenceException
     */
    public void updateNodeReferences(NodeReferenceTable referenceTable) {
       int numSwitch = numSwitches();

       for (int i = 0; i < numSwitch; i++) {
	   Switch curSwitch = getSwitch(i);
	   if (curSwitch != null) {
	       setSwitch((Switch)
			 referenceTable.getNewObjectReference(curSwitch), i);
	   }
       }
    }
}
