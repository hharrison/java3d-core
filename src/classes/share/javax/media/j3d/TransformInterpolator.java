/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
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
 * TransformInterpolator is an abstract class that extends
 * Interpolator to provide common methods used by various transform
 * related interpolator subclasses.  These include methods to set/get
 * the target of TransformGroup, and set/get transform of axis.
 *
 * @since Java 3D 1.3
 */

public abstract class TransformInterpolator extends Interpolator {
    /**
     * The TransformGroup node affected by this transformInterpolator
     */
    protected TransformGroup target = null;

    /**
     * The transform that defines the local coordinate
     */
    protected Transform3D axis = new Transform3D();

    /**
     * The inverse transform that defines the local coordinate
     */
    protected Transform3D axisInverse = new Transform3D();

    /**
     * The transform which is passed into computeTransform() when computeTransform()
     * is called implicitly from processStimulus()
     */
   private Transform3D currentTransform = new Transform3D();
  
    // We can't use a boolean flag since it is possible 
    // that after alpha change, this procedure only run
    // once at alpha.finish(). So the best way is to
    // detect alpha value change.
    private float prevAlphaValue = Float.NaN;
    private WakeupCriterion passiveWakeupCriterion = 
    (WakeupCriterion) new WakeupOnElapsedFrames(0, true);

    
    /**
     * Constructs a TransformInterpolator node with a null alpha value and
     * a null target of TransformGroup
     */
    public TransformInterpolator() {
    }

    /**
     * Constructs a trivial transform interpolator with a specified alpha,
     * a specified target and an default axis set to Identity.
     * @param alpha The alpha object for this transform Interpolator
     * @param target The target TransformGroup for this TransformInterpolator 
     */
    public TransformInterpolator(Alpha alpha, TransformGroup target) {
	super(alpha);
	this.target = target;
	axis.setIdentity();
	axisInverse.setIdentity();
    }
    /**
     * Constructs a new transform interpolator that set an specified alpha,
     * a specified targe and a specified axisOfTransform.
     * @param alpha the alpha object for this interpolator
     * @param target the transformGroup node affected by this transformInterpolator
     * @param axisOfTransform the transform that defines the local coordinate
     * system in which this interpolator operates.  
     */
    public TransformInterpolator(Alpha alpha,
				TransformGroup target,
				Transform3D axisOfTransform){

	super(alpha);
	this.target = target;
	axis.set(axisOfTransform);
	axisInverse.invert(axis);
    }
    
    /**
     * This method sets the target TransformGroup node for this 
     * interpolator.
     * @param target The target TransformGroup
     */
    public void setTarget(TransformGroup target) {
	this.target = target;
    }

    /**
     * This method retrieves this interpolator's TransformGroup
     * node reference.
     * @return the Interpolator's target TransformGroup
     */
    public TransformGroup getTarget() {
	return target;
    }

    /**
     * This method sets the axis of transform for this interpolator.
     * @param axisOfTransform the transform that defines the local coordinate
     * system in which this interpolator operates
     */ 
    public void setTransformAxis(Transform3D axisOfTransform) {
        this.axis.set(axisOfTransform);
        this.axisInverse.invert(this.axis);
    }
       
    /**
     * This method retrieves this interpolator's axis of transform.
     * @return the interpolator's axis of transform
     */ 
    public Transform3D getTransformAxis() {
        return new Transform3D(this.axis);
    }

    /**
     * Computes the new transform for this interpolator for a given
     * alpha value.
     *
     * @param alphaValue alpha value between 0.0 and 1.0
     * @param transform object that receives the computed transform for
     * the specified alpha value
     */
    public abstract void computeTransform(float alphaValue,
					  Transform3D transform);

    /**
     * This method is invoked by the behavior scheduler every frame.
     * First it gets the alpha value that corresponds to the current time.
     * Then it calls computeTransform() method to computes the transform based on this
     * alpha vaule,  and updates the specified TransformGroup node with this new transform.
     * @param criteria an enumeration of the criteria that caused the
     * stimulus
     */
    public void processStimulus(Enumeration criteria) {
	// Handle stimulus
	WakeupCriterion criterion = passiveWakeupCriterion;

	if (alpha != null) {
	    float value = alpha.value();
	    if (value != prevAlphaValue) {
		computeTransform(value, currentTransform);
		target.setTransform(currentTransform);
		prevAlphaValue = value;
	    }
	    if (!alpha.finished() && !alpha.isPaused()) {
		criterion = defaultWakeupCriterion;
	    }
	}
	wakeupOn(criterion);
    }
    
   /**
     * Copies all TransformInterpolator information from
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

	TransformInterpolator ti = (TransformInterpolator) originalNode;

        setTransformAxis(ti.getTransformAxis());
	
	// this reference will be updated in updateNodeReferences()
        setTarget(ti.getTarget());
    }

        /**
     * Callback used to allow a node to check if any scene graph objects
     * referenced
     * by that node have been duplicated via a call to <code>cloneTree</code>.
     * This method is called by <code>cloneTree</code> after all nodes in
     * the sub-graph have been duplicated. The cloned Leaf node's method
     * will be called and the Leaf node can then look up any object references
     * by using the <code>getNewObjectReference</code> method found in the
     * <code>NodeReferenceTable</code> object.  If a match is found, a
     * reference to the corresponding object in the newly cloned sub-graph
     * is returned.  If no corresponding reference is found, either a
     * DanglingReferenceException is thrown or a reference to the original
     * object is returned depending on the value of the
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

        // check TransformGroup
        Node n = getTarget();

        if (n != null) {
            setTarget((TransformGroup) referenceTable.getNewObjectReference(n));
        }
    }
}
