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

class UpdateTargets {

    static int updateSwitchThreads[] = {
	// GEO
        J3dThread.UPDATE_RENDER | J3dThread.UPDATE_RENDERING_ENVIRONMENT |
        J3dThread.UPDATE_GEOMETRY,

	// ENV
        J3dThread.UPDATE_RENDER | J3dThread.UPDATE_RENDERING_ENVIRONMENT,

	// BEH
        J3dThread.UPDATE_BEHAVIOR,

	// SND
        J3dThread.UPDATE_SOUND | J3dThread.SOUND_SCHEDULER,

	// VPF
        J3dThread.UPDATE_RENDER | J3dThread.UPDATE_BEHAVIOR |
        J3dThread.UPDATE_SOUND | J3dThread.SOUND_SCHEDULER,

	// BLN
        J3dThread.UPDATE_RENDER | J3dThread.UPDATE_RENDERING_ENVIRONMENT |
        J3dThread.UPDATE_BEHAVIOR | J3dThread.UPDATE_SOUND,

	// GRP
        0
        };


    UnorderList[] targetList = new UnorderList[Targets.MAX_NODELIST];

    int computeSwitchThreads() {
        int switchThreads = 0;

        for (int i=0; i < Targets.MAX_NODELIST; i++) {
            if (targetList[i] != null) {
                switchThreads |= updateSwitchThreads[i];
            }
        }
        return switchThreads | J3dThread.UPDATE_TRANSFORM;
    }

    void addNode(Object node, int targetType) {
        if(targetList[targetType] == null)
            targetList[targetType] = new UnorderList(1);

        targetList[targetType].add(node);
    }

    
    void addNodeArray(Object[] nodeArr, int targetType) {
	if(targetList[targetType] == null)
	    targetList[targetType] = new UnorderList(1);
	
	targetList[targetType].add(nodeArr);
    }


    void clearNodes() {	
	for(int i=0; i<Targets.MAX_NODELIST; i++) {
	    if (targetList[i] != null) {
	        targetList[i].clear();
	    }
	}
    }

    void addCachedTargets(CachedTargets cachedTargets) {
        for(int i=0; i<Targets.MAX_NODELIST; i++) {
            if (cachedTargets.targetArr[i] != null ) {
                addNodeArray(cachedTargets.targetArr[i], i);
            }
        }
    }

    void dump() {
        for(int i=0; i<Targets.MAX_NODELIST; i++) {
            if (targetList[i] != null) {
                System.out.println("  " + CachedTargets.typeString[i]);
                for(int j=0; j<targetList[i].size(); j++) {
                    System.out.println("  " + targetList[i].get(j));
                }
            }
        }
    }
}
