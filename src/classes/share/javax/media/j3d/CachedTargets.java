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

class CachedTargets {
    // cached targets, used by J3d threads
    
    // 0 - Data type is GeometryAtom.
    // 1 - Data type is Light, Fog, Background, ModelClip, AlternateAppearance,
    //                  Clip
    // 2 - Data type is BehaviorRetained.
    // 3 - Data type is Sound or Soundscape
    // 4 - Data type is ViewPlatformRetained.
    // 5 - Data type is BoundingLeafRetained.
    // 6 - Data type is GroupRetained.

    // Order of index is as above.
    // The handling of BoundingLeaf isn't optimize. Target threads should be 
    // more specific.

    static String typeString[] = {
	"GEO_TARGETS",
	"ENV_TARGETS",
	"BEH_TARGETS",
	"SND_TARGETS",
	"VPF_TARGETS",
	"BLN_TARGETS",
	"GRP_TARGETS",
    };

    static int updateTargetThreads[] = {
	// GEO
        J3dThread.UPDATE_TRANSFORM | J3dThread.UPDATE_RENDER | 
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
        J3dThread.UPDATE_TRANSFORM | J3dThread.UPDATE_GEOMETRY
    };

    
    NnuId targetArr[][] = new NnuId[Targets.MAX_NODELIST][];
 
    int computeTargetThreads() {
	int targetThreads = 0;
	
	for (int i=0; i < Targets.MAX_NODELIST; i++) {
	    if (targetArr[i] != null) {
		targetThreads |= updateTargetThreads[i];
            }
        }
	return targetThreads;
    }

    void copy( CachedTargets ct ) {

        for(int i=0; i<Targets.MAX_NODELIST; i++) {
            targetArr[i] = ct.targetArr[i];
        }
    }

    void replace(NnuId oldObj, NnuId newObj, int type) {

        NnuId[] newArr = new NnuId[targetArr[type].length];
        System.arraycopy(targetArr[type], 0, newArr,
                         0, targetArr[type].length);
        targetArr[type] = newArr;
        NnuIdManager.replace((NnuId)oldObj, (NnuId)newObj, 
				(NnuId[])targetArr[type]);
    }

    void dump() {
        for(int i=0; i<Targets.MAX_NODELIST; i++) {
	    if (targetArr[i] != null) {
		System.out.println("  " + typeString[i]);
        	for(int j=0; j<targetArr[i].length; j++) {
		    System.out.println("  " + targetArr[i][j]);
	        }
	    }
        }
    }

}
