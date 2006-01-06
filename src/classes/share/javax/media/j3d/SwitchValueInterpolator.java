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

/**
 * SwitchValueInterpolator behavior.  This class defines a 
 * behavior that modifies the selected child of the target
 * switch node by linearly interpolating between a pair of
 * specified child index values (using the value generated
 * by the specified Alpha object).
 */

public class SwitchValueInterpolator extends Interpolator {

    Switch target;
    int firstSwitchIndex;
    int lastSwitchIndex;
    int childCount;

    // We can't use a boolean flag since it is possible 
    // that after alpha change, this procedure only run
    // once at alpha.finish(). So the best way is to
    // detect alpha value change.
    private float prevAlphaValue = Float.NaN;
    private WakeupCriterion passiveWakeupCriterion = 
	(WakeupCriterion) new WakeupOnElapsedFrames(0, true);

    // non-public, default constructor used by cloneNode
    SwitchValueInterpolator() {
    }

    /**
     * Constructs a SwitchValueInterpolator behavior that varies its target 
     * Switch node's child index between 0 and <i>n</i>-1, where <i>n</i>
     * is the number of children in the target Switch node.
     * @param alpha the alpha object for this interpolator
     * @param target the Switch node affected by this interpolator
     */
    public SwitchValueInterpolator(Alpha alpha,
				   Switch target) {

	super(alpha);

	this.target = target;
	firstSwitchIndex = 0;
	childCount = target.numChildren();
	lastSwitchIndex = childCount - 1;
	
    }

    /**
     * Constructs a SwitchValueInterpolator behavior that varies its target 
     * Switch node's child index between the two values provided.
     * @param alpha the alpha object for this interpolator
     * @param target the Switch node affected by this interpolator
     * @param firstChildIndex the index of first child in the Switch node to
     * select
     * @param lastChildIndex the index of last child in the Switch node to
     * select
     */
    public SwitchValueInterpolator(Alpha alpha,
				   Switch target,
				   int firstChildIndex,
				   int lastChildIndex) {

	super(alpha);

	this.target = target;
	firstSwitchIndex = firstChildIndex;
	lastSwitchIndex = lastChildIndex;
	computeChildCount();
    }
    
    /**
      * This method sets the firstChildIndex for this interpolator.
      * @param firstIndex the new index for the first child
      */
    public void setFirstChildIndex(int firstIndex) {
	firstSwitchIndex = firstIndex;
	computeChildCount();
    }

    /**
      * This method retrieves this interpolator's firstChildIndex.
      * @return the interpolator's firstChildIndex
      */
    public int getFirstChildIndex() {
	return this.firstSwitchIndex;
    }

    /**
      * This method sets the lastChildIndex for this interpolator.
      * @param lastIndex the new index for the last child
      */
    public void setLastChildIndex(int lastIndex) {
	lastSwitchIndex = lastIndex;
	computeChildCount();
    }

    /**
      * This method retrieves this interpolator's lastSwitchIndex.
      * @return the interpolator's maximum scale value
      */
    public int getLastChildIndex() {
	return this.lastSwitchIndex;
    }

    /**
      * This method sets the target for this interpolator.
      * @param target the target Switch node
      */
    public void setTarget(Switch target) {
	this.target = target;
    }

    /**
      * This method retrieves this interpolator's target Switch node
      * reference.
      * @return the interpolator's target Switch node
      */
    public Switch getTarget() {
	return target;
    }

    // The SwitchValueInterpolator's initialize routine uses the default 
    // initialization routine.

    /**
     * This method is invoked by the behavior scheduler every frame.  
     * It maps the alpha value that corresponds to the current time
     * into a child index value and updates the specified Switch node
     * with this new child index value.
     * @param criteria an enumeration of the criteria that triggered
     * this stimulus
     */
    public void processStimulus(Enumeration criteria) {
	// Handle stimulus
	WakeupCriterion criterion = passiveWakeupCriterion;

	if (alpha != null) {
	    float value = alpha.value();

	    if (value != prevAlphaValue) {
		int child;

		if (lastSwitchIndex > firstSwitchIndex) {
		    child = (int)(firstSwitchIndex +
				  (int)(value * (childCount-1) + 0.49999999999f));
		} else {
		    child = (int)(firstSwitchIndex - 
				  (int)(value * (childCount-1) + 0.49999999999f));
		}
		target.setWhichChild(child);
		prevAlphaValue = value;
	    }
	    if (!alpha.finished() && !alpha.isPaused()) {
		criterion = defaultWakeupCriterion;
	    }
	}
	wakeupOn(criterion);
    }


    /**
     * calculate the number of the child to manage for this switch node 
     */
    final private void computeChildCount() {
	if (lastSwitchIndex >= firstSwitchIndex) {	
	    childCount = lastSwitchIndex - firstSwitchIndex + 1;
	} else {
	    childCount = firstSwitchIndex - lastSwitchIndex + 1;	    
	}
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
    public Node cloneNode(boolean forceDuplicate) {
        SwitchValueInterpolator svi = new SwitchValueInterpolator();
        svi.duplicateNode(this, forceDuplicate);
        return svi;
    }


   /**
     * Copies all SwitchValueInterpolator information from
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

	SwitchValueInterpolator si = 
	    (SwitchValueInterpolator) originalNode;

        setFirstChildIndex(si.getFirstChildIndex());
        setLastChildIndex(si.getLastChildIndex());
	// this reference will be updated in updateNodeReferences()
        setTarget(si.getTarget());
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
        super.updateNodeReferences(referenceTable);

        // check Switch
        Node n = getTarget();

        if (n != null) {
            setTarget((Switch) referenceTable.getNewObjectReference(n));
        }
    }
}
