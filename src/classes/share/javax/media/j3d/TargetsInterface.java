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
import java.util.ArrayList;


interface TargetsInterface {

    static final int TRANSFORM_TARGETS =	0;
    static final int SWITCH_TARGETS =		1;

    // used by Switch, TransformGroup and SharedGroup
    abstract CachedTargets getCachedTargets(int type, int index, int child);
    abstract void resetCachedTargets(int type, CachedTargets[] newCt, int child);
    // used by TransformGroup and SharedGroup
    abstract int getTargetThreads(int type);
    abstract void updateCachedTargets(int type, CachedTargets[] newCt);
    abstract void computeTargetThreads(int type, CachedTargets[] newCt);
    abstract void updateTargetThreads(int type, CachedTargets[] newCt);
    abstract void propagateTargetThreads(int type, int childTargetThreads);
    abstract void copyCachedTargets(int type, CachedTargets[] newCt);

    // used by Switch and SharedGroup
    abstract ArrayList getTargetsData(int type, int index);
}
