/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;


/**
 * The GeometryUpdater interface is used in updating geometry data
 * that is accessed by reference from a live or compiled GeometryArray
 * object.  Applications that wish to modify such data must define a
 * class that implements this interface.  An instance of that class is
 * then passed to the <code>updateData</code> method of the
 * GeometryArray object to be modified.
 *
 * @since Java 3D 1.2
 */

public interface GeometryUpdater {
    /**
     * Updates geometry data that is accessed by reference.
     * This method is called by the updateData method of a
     * GeometryArray object to effect
     * safe updates to vertex data that
     * is referenced by that object.  Applications that wish to modify
     * such data must implement this method and perform all updates
     * within it.
     * <br>
     * NOTE: Applications should <i>not</i> call this method directly.
     *
     * @param geometry the Geometry object being updated.
     * @see GeometryArray#updateData
     */
    public void updateData(Geometry geometry);
}
