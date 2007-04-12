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

class Targets {

    static final int MAX_NODELIST = 7;

    static final int GEO_TARGETS = 0; // For geometryAtoms.
    static final int ENV_TARGETS = 1; // For enviroment nodes.
    static final int BEH_TARGETS = 2; // For behavior nodes.
    static final int SND_TARGETS = 3; // For sound nodes.
    static final int VPF_TARGETS = 4; // For viewPlatform nodes.
    static final int BLN_TARGETS = 5; // For boundingLeaf nodes.
    static final int GRP_TARGETS = 6; // For group nodes.

    ArrayList[] targetList = new ArrayList[MAX_NODELIST];
    
    void addNode(NnuId node, int targetType) {
	if(targetList[targetType] == null)
	    targetList[targetType] = new ArrayList(1);
	
	targetList[targetType].add(node);
    }

    void addNodeArray(NnuId[] nodeArr, int targetType) {
	if(targetList[targetType] == null)
	    targetList[targetType] = new ArrayList(1);
	
	targetList[targetType].add(nodeArr);
    }


    void removeNode(int index, int targetType) {
        if(targetList[targetType] != null) {
            targetList[targetType].remove(index);
	}
    }


    void addNodes(ArrayList nodeList, int targetType) {
	if(targetList[targetType] == null)
	    targetList[targetType] = new ArrayList(1);
	
	targetList[targetType].addAll(nodeList);
    }
    
    
    void clearNodes() {	
	for(int i=0; i<MAX_NODELIST; i++) {
	    if (targetList[i] != null) {
	        targetList[i].clear();
	    }
	}
    }

    CachedTargets snapShotInit() {

	CachedTargets cachedTargets = new CachedTargets();


	for(int i=0; i<MAX_NODELIST; i++) {
	    if(targetList[i] != null) {
		int size = targetList[i].size();
		NnuId[] nArr = new NnuId[size];
		targetList[i].toArray(nArr);
		cachedTargets.targetArr[i] = nArr;
		// System.err.println("Before sort : ");
		// NnuIdManager.printIds(cachedTargets.targetArr[i]);
		NnuIdManager.sort((NnuId[])cachedTargets.targetArr[i]);
		// System.err.println("After sort : ");
		// NnuIdManager.printIds(cachedTargets.targetArr[i]);
	    } else {
		cachedTargets.targetArr[i] = null;
	    }
	}
	
	clearNodes();
	
	return cachedTargets;
    }

    
    CachedTargets snapShotAdd(CachedTargets cachedTargets) {
	
	int i, size;
	
	CachedTargets newCachedTargets = new CachedTargets();
	
	for(i=0; i<MAX_NODELIST; i++) {	    
	    if((targetList[i] != null) && (cachedTargets.targetArr[i] == null)) {
		size = targetList[i].size();
		NnuId[] nArr = new NnuId[size];
		targetList[i].toArray(nArr);
		newCachedTargets.targetArr[i] = nArr;		
		NnuIdManager.sort(newCachedTargets.targetArr[i]);

	    }
	    else if((targetList[i] != null) && (cachedTargets.targetArr[i] != null)) {

		size = targetList[i].size();
		NnuId[] targetArr = new NnuId[size];
		targetList[i].toArray(targetArr);
		NnuIdManager.sort(targetArr);
		newCachedTargets.targetArr[i] =
		    NnuIdManager.merge(cachedTargets.targetArr[i], targetArr);
		
	    }
	    else if((targetList[i] == null) && (cachedTargets.targetArr[i] != null)) {
		newCachedTargets.targetArr[i] = cachedTargets.targetArr[i];
	    }
	}
	
	clearNodes();

	return 	newCachedTargets;

    }


    CachedTargets snapShotRemove(CachedTargets cachedTargets) {
	
	int i, size;
	NnuId[] targetArr;
	

	CachedTargets newCachedTargets = new CachedTargets();

	for(i=0; i<MAX_NODELIST; i++) {
	    
	    if((targetList[i] != null) && (cachedTargets.targetArr[i] != null)) {
		size = targetList[i].size();
		targetArr = new NnuId[size];
		targetList[i].toArray(targetArr);
		NnuIdManager.sort(targetArr);
		newCachedTargets.targetArr[i] =
		    NnuIdManager.delete(cachedTargets.targetArr[i], targetArr);
		
	    }
	    else if((targetList[i] == null) && (cachedTargets.targetArr[i] != null)) {
		newCachedTargets.targetArr[i] = cachedTargets.targetArr[i];
		
	    }
	    else if((targetList[i] != null) && (cachedTargets.targetArr[i] == null)) {
		System.err.println("You can't remove something that isn't there");
	    }

	}
	
	clearNodes();

	return 	newCachedTargets;
	
    }

    boolean isEmpty() {
	boolean empty = true;
	
        for (int i=0; i < MAX_NODELIST; i++) {
            if (targetList[i] != null) {
                empty = false;
		break;
            }
        }
	return empty;
    }

    void addCachedTargets(CachedTargets cachedTargets) {
	for(int i=0; i<MAX_NODELIST; i++) {
	    if (cachedTargets.targetArr[i] != null ) {
	        addNodeArray(cachedTargets.targetArr[i], i);
	    }
        }
    }

    void dump() {
        for(int i=0; i<Targets.MAX_NODELIST; i++) {
            if (targetList[i] != null) {
                System.err.println("  " + CachedTargets.typeString[i]);
                for(int j=0; j<targetList[i].size(); j++) {
                    System.err.println("  " + targetList[i].get(j));
                }
            }
        }
    }
}
