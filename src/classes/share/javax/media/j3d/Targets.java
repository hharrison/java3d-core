/*
 * Copyright 2001-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
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

    ArrayList<NnuId>[] targetList = new ArrayList[MAX_NODELIST];

    void addNode(NnuId node, int targetType) {
	if(targetList[targetType] == null)
	    targetList[targetType] = new ArrayList<NnuId>(1);

	targetList[targetType].add(node);
    }

    void removeNode(int index, int targetType) {
        if(targetList[targetType] != null) {
            targetList[targetType].remove(index);
	}
    }


    void addNodes(ArrayList<NnuId> nodeList, int targetType) {
	if(targetList[targetType] == null)
	    targetList[targetType] = new ArrayList<NnuId>(1);

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
		NnuIdManager.sort(cachedTargets.targetArr[i]);
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
