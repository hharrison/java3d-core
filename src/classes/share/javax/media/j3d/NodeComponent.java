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
import java.util.Hashtable;

/**
 * NodeComponent is a common superclass for all scene graph node
 * component objects such as: Geometry, Appearance, Material, Texture, etc.
 */
public abstract class NodeComponent extends SceneGraphObject {

    // This is use for cloneTree only, set to false after the operation
    boolean forceDuplicate = false; 
    /**
     * Constructs a NodeComponent object with default parameters.
     * The default values are as follows:
     * <ul>
     * duplicate on clone tree : false<br>
     * </ul>
     */
    public NodeComponent() {
    }

  /**
   * Sets this node's duplicateOnCloneTree value.  The
   * <i>duplicateOnCloneTree</i> value is used to determine if NodeComponent
   * objects are to be duplicated or referenced during a
   * <code>cloneTree</code> operation. A value of <code>true</code> means
   *  that this NodeComponent object should be duplicated, while a value
   *  of <code>false</code> indicates that this NodeComponent object's
   *  reference will be copied into the newly cloned object.  This value
   *  can be overriden via the <code>forceDuplicate</code> parameter of
   *  the <code>cloneTree</code> method.
   * @param duplicate the value to set.
   * @see Node#cloneTree
   */
    public void setDuplicateOnCloneTree(boolean duplicate) {
	((NodeComponentRetained)retained).setDuplicateOnCloneTree(duplicate);
    }

  /**
   * Returns this node's duplicateOnCloneTree value. The
   * <i>duplicateOnCloneTree</i> value is used to determine if NodeComponent
   * objects are to be duplicated or referenced during a
   * <code>cloneTree</code> operation. A value of <code>true</code> means
   *  that this NodeComponent object should be duplicated, while a value
   *  of <code>false</code> indicates that this NodeComponent object's
   *  reference will be copied into the newly cloned object.  This value
   *  can be overriden via the <code>forceDuplicate</code> parameter of
   *  the <code>cloneTree</code> method.
   * @return the value of this node's duplicateOnCloneTree
   * @see Node#cloneTree
   */
    public boolean getDuplicateOnCloneTree() {
	return ((NodeComponentRetained)retained).getDuplicateOnCloneTree();
    }

  /**
   * @deprecated As of Java 3D version 1.2, replaced by
   * <code>cloneNodeComponent(boolean forceDuplicate)</code>
   */
    public NodeComponent cloneNodeComponent() {
	throw new RuntimeException(J3dI18N.getString("NodeComponent0"));
    }


    /**
     * NOTE: Applications should <i>not</i> call this method directly.
     * It should only be called by the cloneNode method.
     *
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>duplicateNodeComponent(NodeComponent
     * originalNodeComponent, boolean forceDuplicate)</code>
     */
    public void duplicateNodeComponent(NodeComponent originalNodeComponent) {
        duplicateAttributes(originalNodeComponent, 
			    originalNodeComponent.forceDuplicate);
    }

    /**
     * Copies all node information from <code>originalNodeComponent</code> into
     * the current node component.  This method is called from subclass of
     * <code>duplicateNodeComponent</code> method which is, in turn, called by the
     * <code>cloneNodeComponent</code> method. 
     *
     * For any <i>NodeComponent</i> objects
     * contained by the object being duplicated, each <i>NodeComponent</i>
     * object's <code>duplicateOnCloneTree</code> value is used to determine
     * whether the <i>NodeComponent<i> should be duplicated in the new node
     * or if just a reference to the current node should be placed in the
     * new node.  This flag can be overridden by setting the
     * <code>forceDuplicate</code> parameter in the <code>cloneTree</code>
     * method to <code>true</code>.
     *
     * @param originalNodeComponent the original node component to duplicate.
     */
    final void checkDuplicateNodeComponent(
				   NodeComponent originalNodeComponent) {

	if (originalNodeComponent.nodeHashtable != null) {
	    duplicateAttributes(originalNodeComponent,
				originalNodeComponent.forceDuplicate);
	} else {
	    //  user call cloneNodeComponent() or duplicateNodeComponent() 
	    // directly instead of via cloneTree()
	    originalNodeComponent.nodeHashtable = new Hashtable();
	    duplicateAttributes(originalNodeComponent, 
				originalNodeComponent.forceDuplicate);
	    originalNodeComponent.nodeHashtable = null;
	}
    }

  /**
   * Copies all node information from <code>originalNodeComponent</code>
   * into the current node.  This method is called from the
   * <code>cloneNodeComponent</code> method which is, in turn, called
   * by the <code>cloneNode</code> method.
   * <br>
   * NOTE: Applications should <i>not</i> call this method directly.
   * It should only be called by the cloneNode method.
   *
   * @param originalNodeComponent the node to duplicate.
   * @param forceDuplicate when set to <code>true</code>, causes the
   *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
   *  <code>false</code>, the value of each node's
   *  <code>duplicateOnCloneTree</code> variable determines whether
   *  NodeComponent data is duplicated or copied.
   *
   * @exception RestrictedAccessException if forceDuplicate is set and
   *  this object is part of a compiled scenegraph
   *
   * @see NodeComponent#cloneNodeComponent
   * @see Node#cloneNode
   * @see Node#cloneTree
   *
   * @since Java 3D 1.2
   */
    public void duplicateNodeComponent(NodeComponent originalNodeComponent,
				       boolean forceDuplicate) {
        originalNodeComponent.forceDuplicate = forceDuplicate;
	try {
	    duplicateNodeComponent(originalNodeComponent);
	} catch (RuntimeException e) {
	    originalNodeComponent.forceDuplicate = false;
	    throw e;
	}
	originalNodeComponent.forceDuplicate = false;
    }

  /**
   * Used to create a new instance of a NodeComponent object.  This
   * routine is called  by <code>cloneNode</code> to duplicate the 
   * current node. <br>
   * 
   * <code>cloneNodeComponent</code> should be overridden by any user
   * subclassed <i>NodeComponent</i> objects. All subclasses must have their
   * <code>cloneNodeComponent</code>
   * method consist of the following lines:
   * <P><blockquote><pre>
   *     public NodeComponent cloneNodeComponent(boolean forceDuplicate) {
   *         UserNodeComponent unc = new UserNodeComponent();
   *         unc.duplicateNodeComponent(this, forceDuplicate);
   *         return unc;
   *     }
   * </pre></blockquote>
   *
   * @param forceDuplicate when set to <code>true</code>, causes the
   *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
   *  <code>false</code>, the value of each node's
   *  <code>duplicateOnCloneTree</code> variable determines whether
   *  NodeComponent data is duplicated or copied.
   *   
   * @exception RestrictedAccessException if forceDuplicate is set and
   *  this object is part of a compiled scenegraph
   *
   * @see NodeComponent#duplicateNodeComponent
   * @see Node#cloneNode
   * @see Node#cloneTree
   *
   * @since Java 3D 1.2
   */
    public NodeComponent cloneNodeComponent(boolean forceDuplicate) {
	// For backward compatibility !
	//
	// If user did not overwrite this procedure, it will fall back
	// to call cloneNodeComponent() 
	// So for core API, 
	// don't implement cloneNodeComponent(boolean forceDuplicate)
	// otherwise this prcedure will not call and the user
	// cloneNodeComponent() will not invoke.
        NodeComponent nc; 
	this.forceDuplicate = forceDuplicate;
	try {
	    nc = cloneNodeComponent();
	} catch (RuntimeException e) {
	    this.forceDuplicate = false;
	    throw e;
	}
	this.forceDuplicate = false;
	return nc;
    }


    /**
     * Copies all NodeComponent information from
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
     * @see Group#cloneNode
     * @see Node#duplicateNode
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    void duplicateAttributes(NodeComponent originalNode,
			     boolean forceDuplicate) {

        if (forceDuplicate && originalNode.isCompiled()) {
            throw new RestrictedAccessException(
                J3dI18N.getString("NodeComponent1"));
        }

        super.duplicateSceneGraphObject(originalNode);
	setDuplicateOnCloneTree(originalNode.getDuplicateOnCloneTree());
    }

    /**
     * Creates the retained mode NodeComponentRetained object that this
     * NodeComponent object will point to.
    */
    void createRetained() {
        this.retained = new NodeComponentRetained();
	this.retained.setSource(this);
    }

    /** 
     *  This function is called from getNodeComponent() to see if any of
     *  the sub-NodeComponents  duplicateOnCloneTree flag is true. 
     *  If it is the case, current NodeComponent needs to 
     *  duplicate also even though current duplicateOnCloneTree flag is false. 
     *  This should be overwrite by NodeComponent which contains sub-NodeComponent.
     */
    boolean duplicateChild() {
        return getDuplicateOnCloneTree();
    }
}
