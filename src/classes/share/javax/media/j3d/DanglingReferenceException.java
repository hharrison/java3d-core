/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * During a <code>cloneTree</code> call an updated reference was requested
 * for a node that did not get cloned.  This happens when a sub-graph is
 * duplicated via <code>cloneTree</code> and has at least one Leaf node
 * that contains a reference to a Node that has no corresponding node in
 * the cloned sub-graph. This results in two Leaf nodes wanting to share
 * access to the same Node.
 * <P>
 * If dangling references are to be allowed during the cloneTree call,
 * <code>cloneTree</code> should be called with the
 * <code>allowDanglingReferences</code> parameter set to <code>true</code>.
 * @see Node#cloneTree
 */
public class DanglingReferenceException extends RuntimeException {

    /**
     * Create the exception object with default values.
     */
    public DanglingReferenceException() {
    }

    /**
     * Create the exception object that outputs message.
     * @param str the message string to be output.
     */
    public DanglingReferenceException(String str) {
	super(str);
    }

}
