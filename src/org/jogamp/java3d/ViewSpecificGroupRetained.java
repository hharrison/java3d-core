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

package org.jogamp.java3d;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The ViewSpecificGroup node retained object.
 */

class ViewSpecificGroupRetained extends GroupRetained {

ArrayList<View> apiViewList = new ArrayList<View>();

    // Used by leaf objects particularly GAs
    // Updated in a MT Safe manner and also used by RenderBin
    ArrayList<ArrayList<View>> cachedViewList = new ArrayList<ArrayList<View>>();

   // The object that contains the dynamic HashKey - a string type object
    // Used in scoping
    HashKey tempKey = new HashKey(250);

// ArrayList of Integer indices
ArrayList<ArrayList<View>> parentLists = new ArrayList<ArrayList<View>>();

    static final int SET_VIEW       = 0x1;
    static final int ADD_VIEW       = 0x2;
    static final int REMOVE_VIEW    = 0x4;

    // Construct retained object
    ViewSpecificGroupRetained() {
          this.nodeType = NodeRetained.VIEWSPECIFICGROUP;
	  viewLists = new ArrayList<ArrayList<View>>();
    }

    void addView(View view) {
	int i;
	Integer mtype = new Integer(ADD_VIEW);

	apiViewList.add(view);
	if (source.isLive() && view != null) {
	    // Gather all affected leaf nodes and send a message to
	    // RenderingEnv and RenderBin
	    if (inSharedGroup) {
		for (int k = 0; k < localToVworldKeys.length; k++) {
		    ArrayList<View> parentList = parentLists.get(k);
		    // If the parentList contains this view or if this is the
		    // first VSG then ..
		    if (parentList == null || parentList.contains(view)) {
			Object[] objAry = new Object[4];
			ArrayList addVsgList = new ArrayList();
			ArrayList addLeafList = new ArrayList();
			int[] addKeyList = new int[10];

			HashKey key = localToVworldKeys[k];
			addVsgList.add(this);
			addKeyList[0] = k;
			objAry[0] = view;
			objAry[1] = addVsgList;
			objAry[2] = addLeafList;
			/*
			for (int n = 0; n < addLeafList.size(); n++) {
			    System.err.println("Shared:n = "+n+" addLeafList = "+addLeafList.get(n));
			}
			*/
		      objAry[3] = super.processViewSpecificInfo(ADD_VIEW,
						      key, view,
						      addVsgList, addKeyList, addLeafList);
			J3dMessage message = new J3dMessage();
			message.type = J3dMessage.VIEWSPECIFICGROUP_CHANGED;
			message.threads = (J3dThread.UPDATE_RENDERING_ENVIRONMENT|
					   J3dThread.UPDATE_RENDER |
					   J3dThread.UPDATE_SOUND|
					   J3dThread.SOUND_SCHEDULER);
			message.universe = universe;
			message.args[0] = mtype;
			message.args[1] = objAry;
			VirtualUniverse.mc.processMessage(message);
		    }
		}

	    }
	    else {
			ArrayList<View> parentList = parentLists.get(0);

		// If the parentList contains this view or if this is the
		// first VSG then ..
		if (parentList == null || parentList.contains(view)) {
		    Object[] objAry = new Object[4];
		    ArrayList addVsgList = new ArrayList();
		    ArrayList addLeafList = new ArrayList();
		    int[] addKeyList = new int[10];

		    objAry[0] = view;
		    objAry[1] = addVsgList;
		    objAry[2] = addLeafList;

		    addVsgList.add(this);
		    addKeyList[0] = 0;
		    tempKey.reset();
		    objAry[3] = super.processViewSpecificInfo(ADD_VIEW,
						  tempKey, view,
						  addVsgList, addKeyList, addLeafList);

		    /*
		      for (int n = 0; n < addLeafList.size(); n++) {
		      System.err.println("n = "+n+" addLeafList = "+addLeafList.get(n));
		      }
		      */

		    J3dMessage message = new J3dMessage();
		    message.type = J3dMessage.VIEWSPECIFICGROUP_CHANGED;
		    message.threads = (J3dThread.UPDATE_RENDERING_ENVIRONMENT|
				       J3dThread.UPDATE_RENDER |
				       J3dThread.UPDATE_SOUND|
				       J3dThread.SOUND_SCHEDULER);
		    message.universe = universe;
		    message.args[0] = mtype;
		    message.args[1] = objAry;
		    VirtualUniverse.mc.processMessage(message);
		}
	    }


	}
    }


void setView(View view, int index) {
	View oldView =  apiViewList.get(index);
	Integer mtype = new Integer(SET_VIEW);

	if (oldView == view)
	    return;

	apiViewList.set(index, view);
	if (source.isLive()) {
	    // Gather all affected leaf nodes and send a message to
	    // RenderingEnv and RenderBin
	    if (inSharedGroup) {
		for (int k = 0; k < localToVworldKeys.length; k++) {
			ArrayList<View> parentList = parentLists.get(k);
		    Object[] objAry = new Object[8];
		    ArrayList addVsgList = new ArrayList();
		    ArrayList removeVsgList = new ArrayList();
		    ArrayList addLeafList = new ArrayList();
		    ArrayList removeLeafList = new ArrayList();
		    int[] addKeyList = new int[10];
		    int[] removeKeyList = new int[10];

		    objAry[0] = view;
		    objAry[1] = addVsgList ;
		    objAry[2] = addLeafList;
		    objAry[4] = oldView;
		    objAry[5] = removeVsgList;
		    objAry[6] = removeLeafList;

		    HashKey key = localToVworldKeys[k];
		    if (oldView != null && (parentList == null || parentList.contains(oldView))) {
			removeVsgList.add(this);
			removeKeyList[0] = k;
			objAry[7] = super.processViewSpecificInfo(REMOVE_VIEW, key,
						      oldView, removeVsgList, removeKeyList, removeLeafList);
		    }

		    if (view != null && (parentList == null || parentList.contains(view))) {
			addVsgList.add(this);
			addKeyList[0] = k;
			objAry[3] = super.processViewSpecificInfo(ADD_VIEW, key,
						      view, addVsgList, addKeyList, addLeafList);
		    }
		    J3dMessage message = new J3dMessage();
		    message.type = J3dMessage.VIEWSPECIFICGROUP_CHANGED;
		    message.threads = (J3dThread.UPDATE_RENDERING_ENVIRONMENT|
				       J3dThread.UPDATE_RENDER |
				       J3dThread.UPDATE_SOUND|
				       J3dThread.SOUND_SCHEDULER);
		    message.universe = universe;
		    message.args[0] = mtype;
		    message.args[1] = objAry;
		    VirtualUniverse.mc.processMessage(message);
		}

	    }
	    else {
		ArrayList<View> parentList = parentLists.get(0);
		Object[] objAry = new Object[8];
		ArrayList addVsgList = new ArrayList();
		ArrayList removeVsgList = new ArrayList();
		ArrayList addLeafList = new ArrayList();
		ArrayList removeLeafList = new ArrayList();
		int[] addKeyList = new int[10];
		int[] removeKeyList = new int[10];

		objAry[0] = view;
		objAry[1] = addVsgList ;
		objAry[2] = addLeafList;
		objAry[4] = oldView;
		objAry[5] = removeVsgList;
		objAry[6] = removeLeafList;



		tempKey.reset();
		if (oldView != null && (parentList == null || parentList.contains(oldView))) {
		    removeVsgList.add(this);
		    removeKeyList[0] = 0;
		    objAry[7] = super.processViewSpecificInfo(REMOVE_VIEW, tempKey,
						  oldView, removeVsgList, removeKeyList, removeLeafList);
		}
		if (view != null && (parentList == null || parentList.contains(view))) {
		    tempKey.reset();
		    addVsgList.add(this);
		    addKeyList[0] = 0;
		    objAry[3] =  super.processViewSpecificInfo(ADD_VIEW, tempKey,
					      view, addVsgList, addKeyList, addLeafList);
		}
		J3dMessage message = new J3dMessage();
		message.type = J3dMessage.VIEWSPECIFICGROUP_CHANGED;
		message.threads = (J3dThread.UPDATE_RENDERING_ENVIRONMENT|
				   J3dThread.UPDATE_RENDER |
				   J3dThread.UPDATE_SOUND|
				   J3dThread.SOUND_SCHEDULER);

		message.universe = universe;
		message.args[0] = mtype;
		message.args[1] = objAry;
		VirtualUniverse.mc.processMessage(message);
	    }


	}

    }

    @Override
    int[] processViewSpecificInfo(int mode, HashKey key, View v, ArrayList vsgList, int[] keyList, ArrayList leaflist) {
	int hkIndex = 0;
	Integer hashInt = null;
	int[] newKeyList = null;
	// Get the intersection of the viewList with this view,

	if (source.isLive()) {
	    if (inSharedGroup) {
		hkIndex = key.equals(localToVworldKeys, 0, localToVworldKeys.length);
	    }

	    if (mode == ADD_VIEW) {
		ArrayList<View> parentList = parentLists.get(hkIndex);
		parentList.add(v);
	    }
	    else if (mode == REMOVE_VIEW) {
		ArrayList<View> parentList = parentLists.get(hkIndex);
		parentList.remove(v);
	    }
	    if(apiViewList.contains(v)) {
	    //	    System.err.println("processViewSpecificInfo, this = "+this+" key = "+key);
		vsgList.add(this);
		if (keyList.length< vsgList.size()) {
		    //		    System.err.println("====> allocating new array");
		    newKeyList = new int[keyList.length+20];
		    System.arraycopy(keyList, 0, newKeyList, 0, keyList.length);
		    keyList = newKeyList;
		}
		if (mode == ADD_VIEW) {
		    if (inSharedGroup) {
			keyList[vsgList.size()-1] = hkIndex;

		    }
		    else {
			keyList[vsgList.size()-1] = 0;
		    }
		}
		else if (mode == REMOVE_VIEW) {
		    if (inSharedGroup) {
			keyList[vsgList.size()-1] = hkIndex;
		    }
		    else {
			keyList[vsgList.size()-1] = 0;
		    }
		}
		return super.processViewSpecificInfo(mode, key, v, vsgList, keyList, leaflist);
	    }
	}
	return keyList;
    }

View getView(int index) {
	return apiViewList.get(index);
}

    void insertView(View view, int index) {
	int i;
	Integer mtype = new Integer(ADD_VIEW);

	apiViewList.add(index, view);
	if (source.isLive() && view != null) {
	    // Gather all affected leaf nodes and send a message to
	    // RenderingEnv and RenderBin
	    if (inSharedGroup) {
		for (int k = 0; k < localToVworldKeys.length; k++) {
			ArrayList<View> parentList = parentLists.get(k);
		    // If the parentList contains this view or if this is the
		    // first VSG then ..
		    if (parentList == null || parentList.contains(view)) {
			Object[] objAry = new Object[4];
			ArrayList addVsgList = new ArrayList();
			ArrayList addLeafList = new ArrayList();
			int[] addKeyList = new int[10];

			HashKey key = localToVworldKeys[k];
			addVsgList.add(this);
			addKeyList[0] = k;
			objAry[0] = view;
			objAry[1] = addVsgList;
			objAry[2] = addLeafList;
			/*
			for (int n = 0; n < addLeafList.size(); n++) {
			    System.err.println("Shared:n = "+n+" addLeafList = "+addLeafList.get(n));
			}
			*/
		      objAry[3] = super.processViewSpecificInfo(ADD_VIEW,
						      key, view,
						      addVsgList, addKeyList, addLeafList);
			J3dMessage message = new J3dMessage();
			message.type = J3dMessage.VIEWSPECIFICGROUP_CHANGED;
			message.threads = (J3dThread.UPDATE_RENDERING_ENVIRONMENT|
					   J3dThread.UPDATE_RENDER |
					   J3dThread.UPDATE_SOUND|
					   J3dThread.SOUND_SCHEDULER);
			message.universe = universe;
			message.args[0] = mtype;
			message.args[1] = objAry;
			VirtualUniverse.mc.processMessage(message);
		    }
		}

	    }
	    else {
	    	ArrayList<View> parentList = parentLists.get(0);

		// If the parentList contains this view or if this is the
		// first VSG then ..
		if (parentList == null || parentList.contains(view)) {
		    Object[] objAry = new Object[4];
		    ArrayList addVsgList = new ArrayList();
		    ArrayList addLeafList = new ArrayList();
		    int[] addKeyList = new int[10];

		    objAry[0] = view;
		    objAry[1] = addVsgList;
		    objAry[2] = addLeafList;

		    addVsgList.add(this);
		    addKeyList[0] = 0;
		    tempKey.reset();
		    objAry[3] = super.processViewSpecificInfo(ADD_VIEW,
						  tempKey, view,
						  addVsgList, addKeyList, addLeafList);

		    /*
		      for (int n = 0; n < addLeafList.size(); n++) {
		      System.err.println("n = "+n+" addLeafList = "+addLeafList.get(n));
		      }
		      */

		    J3dMessage message = new J3dMessage();
		    message.type = J3dMessage.VIEWSPECIFICGROUP_CHANGED;
		    message.threads = (J3dThread.UPDATE_RENDERING_ENVIRONMENT|
				       J3dThread.UPDATE_RENDER |
				       J3dThread.UPDATE_SOUND|
				       J3dThread.SOUND_SCHEDULER);
		    message.universe = universe;
		    message.args[0] = mtype;
		    message.args[1] = objAry;
		    VirtualUniverse.mc.processMessage(message);
		}
	    }


	}
    }

    void removeView(int index) {
	int i;
	View v = apiViewList.remove(index);
	if (source.isLive() && v != null) {
	    // Gather all affected leaf nodes and send a message to
	    // RenderingEnv and RenderBin
	    if (inSharedGroup) {
		for (int k = 0; k < localToVworldKeys.length; k++) {
			ArrayList<View> parentList = parentLists.get(k);
		    // If the parentList contains this view or if this is the
		    // first VSG then ..
		    if (parentList == null || parentList.contains(v)) {
			Object[] objAry = new Object[4];
			ArrayList removeVsgList = new ArrayList();
			ArrayList removeLeafList = new ArrayList();
			int[] removeKeyList = new int[10];

			objAry[0] = v;
			objAry[1] = removeVsgList;
			objAry[2] = removeLeafList;
			HashKey key = localToVworldKeys[k];

			removeVsgList.add(this);
			removeKeyList[0] = k;

			objAry[3] =  super.processViewSpecificInfo(REMOVE_VIEW,
						      key,v,
						      removeVsgList, removeKeyList, removeLeafList);


			J3dMessage message = new J3dMessage();
			message.type = J3dMessage.VIEWSPECIFICGROUP_CHANGED;
			message.threads = (J3dThread.UPDATE_RENDERING_ENVIRONMENT|
					   J3dThread.UPDATE_RENDER |
					   J3dThread.UPDATE_SOUND|
					   J3dThread.SOUND_SCHEDULER);
			message.universe = universe;
			message.args[0] = new Integer(REMOVE_VIEW);
			message.args[1] = objAry;
			VirtualUniverse.mc.processMessage(message);
		    }
		}

	    }
	    else {
	    	ArrayList<View> parentList = parentLists.get(0);

		// If the parentList contains this view or if this is the
		// first VSG then ..
		if (parentList == null || parentList.contains(v)) {
		    Object[] objAry = new Object[4];
		    ArrayList removeVsgList = new ArrayList();
		    ArrayList removeLeafList = new ArrayList();
		    int[] removeKeyList = new int[10];

		    objAry[0] = v;
		    objAry[1] = removeVsgList;
		    objAry[2] = removeLeafList;
		    removeVsgList.add(this);
		    removeKeyList[0] = 0;

		    tempKey.reset();
		    objAry[3] = super.processViewSpecificInfo(REMOVE_VIEW,
						  tempKey, v,
						  removeVsgList, removeKeyList, removeLeafList);

		    /*
		      for (int n = 0; n < removeKeyList.size(); n++) {
		      System.err.println("n = "+n+" keyValue = "+removeKeyList.get(n));
		      }
		      */
		    J3dMessage message = new J3dMessage();
		    message.type = J3dMessage.VIEWSPECIFICGROUP_CHANGED;
		    message.threads = (J3dThread.UPDATE_RENDERING_ENVIRONMENT|
				       J3dThread.UPDATE_RENDER |
				       J3dThread.UPDATE_SOUND|
				       J3dThread.SOUND_SCHEDULER);
		    message.universe = universe;
		    message.args[0] = new Integer(REMOVE_VIEW);
		    message.args[1] = objAry;
		    VirtualUniverse.mc.processMessage(message);
		}
	    }

	}
    }

Enumeration<View> getAllViews() {
	Vector<View> viewList = new Vector<View>(apiViewList.size());
	for (int i = 0; i < apiViewList.size(); i++) {
		viewList.add(apiViewList.get(i));
	}
	return viewList.elements();
}

    int numViews() {
	return apiViewList.size();
    }

    int indexOfView(View view) {
	return apiViewList.indexOf(view);
    }

    void removeView(View view) {
	removeView(apiViewList.indexOf(view));
    }

    void removeAllViews() {
	int size = apiViewList.size();
	for (int i = 0; i < size; i++) {
	    removeView(0);
	}
    }

    @Override
    void compile(CompileState compState) {
        super.compile(compState);

        // don't remove this group node
        mergeFlag = SceneGraphObjectRetained.DONT_MERGE;

	// XXXX: complete this
    }

    @Override
    void setLive(SetLiveState s) {
        if (inBackgroundGroup) {
            throw new
               IllegalSceneGraphException(J3dI18N.getString("ViewSpecificGroup3"));
        }

	s.inViewSpecificGroup = true;
	ArrayList<ArrayList<View>> savedViewList = s.viewLists;
	if (s.changedViewGroup == null) {
	    s.changedViewGroup = new ArrayList<ViewSpecificGroupRetained>();
	    s.changedViewList = new ArrayList<ArrayList<View>>();
	    s.keyList = new int[10];
	    s.viewScopedNodeList = new ArrayList<NodeRetained>();
	    s.scopedNodesViewList = new ArrayList<ArrayList<View>>();
	}
	super.setLive(s);
        s.viewLists = savedViewList;

    }

    @Override
    void clearLive(SetLiveState s) {
	ArrayList<ArrayList<View>> savedViewList = s.viewLists;
	if (s.changedViewGroup == null) {
	    s.changedViewGroup = new ArrayList<ViewSpecificGroupRetained>();
	    s.changedViewList = new ArrayList<ArrayList<View>>();
	    s.keyList = new int[10];
	    s.viewScopedNodeList = new ArrayList<NodeRetained>();
	    s.scopedNodesViewList = new ArrayList<ArrayList<View>>();
	}
	// XXXX: This is a hack since removeNodeData is called before
	// children are clearLives
	int[] tempIndex = null;
	// Don't keep the indices if everything will be cleared
	if (inSharedGroup && (s.keys.length != localToVworld.length)) {
	    tempIndex = new int[s.keys.length];
	    for (int i = 0; i < s.keys.length; i++) {
		tempIndex[i] = s.keys[i].equals(localToVworldKeys, 0, localToVworldKeys.length);
	    }
	}
	super.clearLive(s);
	// Do this after children clearlive since part of the viewLists may get cleared
	// during removeNodeData

	// If the last SharedGroup is being cleared
        if((!inSharedGroup) || (localToVworld == null)) {
	    viewLists.clear();
	}
	else {
	    // Must be in reverse, to preserve right indexing.
	    for (int i = tempIndex.length-1; i >= 0 ; i--) {
		if(tempIndex[i] >= 0) {
		    viewLists.remove(tempIndex[i]);
		}
	    }
	}
	s.viewLists = savedViewList;
    }


    @Override
    void removeNodeData(SetLiveState s) {
        if((!inSharedGroup) || (s.keys.length == localToVworld.length)) {
	    s.changedViewGroup.add(this);
	    // Remove everything ..
	    int size = s.changedViewGroup.size();
	    if (s.keyList.length < size) {
		int[] newKeyList = new int[s.keyList.length+20];
		System.arraycopy(s.keyList, 0, newKeyList, 0, s.keyList.length);
		s.keyList = newKeyList;
		//		System.err.println("====> RemovedNodeData: Allocating Non-shared");
	    }
	    s.keyList[size -1] = -1;
	    parentLists.clear();
	}
	// A path of the shared group is removed
	else {
	    int i, index;
	    int size = s.changedViewGroup.size();
	    if (s.keyList.length < size+1+s.keys.length) {
		int[] newKeyList = new int[s.keyList.length+s.keys.length+20];
		System.arraycopy(s.keyList, 0, newKeyList, 0, s.keyList.length);
		s.keyList = newKeyList;
		//		System.err.println("====> RemovedNodeData: Allocating Shared");
	    }
	    // Must be in reverse, to preserve right indexing.
	    for (i = s.keys.length-1; i >= 0; i--) {
		index = s.keys[i].equals(localToVworldKeys, 0, localToVworldKeys.length);
		if(index >= 0) {
		    s.changedViewGroup.add(this);
		    s.keyList[s.changedViewGroup.size() -1] = index;
		    parentLists.remove(index);
		}
	    }
	}
	s.viewLists =viewLists;
        super.removeNodeData(s);
    }

void updateCachedInformation(int component, View view, int index) {
	ArrayList<View> list = cachedViewList.get(index);

	/*
	System.err.println("updateCachedInformation v = "+this+" index = "+index+" list = "+list+" cachedViewList.size() = "+cachedViewList.size());
	for (int k = 0; k < cachedViewList.size(); k++) {
	    System.err.println("v = "+this+" k = "+k+" v.cachedViewList.get(k) = "+cachedViewList.get(k));
	}
	*/
	if ((component & ADD_VIEW) != 0) {
	    list.add(view);
	}
	else if ((component & REMOVE_VIEW) != 0) {
	    list.remove(view);
	}
	/*
	System.err.println("After updateCachedInformation v = "+this+" index = "+index+" list = "+list+" cachedViewList.size() = "+cachedViewList.size());
	for (int k = 0; k < cachedViewList.size(); k++) {
	    System.err.println("v = "+this+" k = "+k+" v.cachedViewList.get(k) = "+cachedViewList.get(k));
	}
	*/

    }

    @Override
    void setNodeData(SetLiveState s) {
        super.setNodeData(s);
        if (!inSharedGroup) {
	    int size = s.changedViewGroup.size();
	    if (s.keyList.length < size+1) {
		int[] newKeyList = new int[s.keyList.length+20];
		System.arraycopy(s.keyList, 0, newKeyList, 0, s.keyList.length);
		s.keyList = newKeyList;
		//		System.err.println("====> setNodeData: Allocating Non-shared");
	    }
            setAuxData(s, 0, 0);
        } else {
            // For inSharedGroup case.
            int j, hkIndex;

	    int size = s.changedViewGroup.size();
	    if (s.keyList.length < size+1+s.keys.length) {
		int[] newKeyList = new int[s.keyList.length+s.keys.length+20];
		System.arraycopy(s.keyList, 0, newKeyList, 0, s.keyList.length);
		s.keyList = newKeyList;
		//		System.err.println("====> setNodeData: Allocating Shared");
	    }

	    for(j=0; j<s.keys.length; j++) {
                hkIndex = s.keys[j].equals(localToVworldKeys, 0,
                                                localToVworldKeys.length);

                if(hkIndex >= 0) {
                    setAuxData(s, j, hkIndex);

                } else {
                    MasterControl.getCoreLogger().severe("Can't Find matching hashKey in setNodeData.");
                }
            }
        }
	// Make the VSG's viewLists as the relavant one for its children
	s.viewLists = viewLists;

    }

    @Override
    void setAuxData(SetLiveState s, int index, int hkIndex) {
	ArrayList<View> vl = new ArrayList<View>();
	ArrayList<View> parentList = null;
	int size = apiViewList.size();
	if (s.viewLists != null) {
	    //	    System.err.println("=====> VSG: = "+this+" hkIndex = "+hkIndex+" s.viewLists = "+s.viewLists);
	    parentList = s.viewLists.get(hkIndex);
	    if (parentList != null) {
			for (int i = 0; i < size; i++) {
				View v = apiViewList.get(i);
				// Get the intersection of the parentlist and this vsg's api list
				for (int j = 0; j < parentList.size(); j++) {
					if (v == parentList.get(j)) {
						vl.add(v);
						break;
					}
				}
			}
	    }
	    else {
		// Only include the non null ones in the apiViewList
		for (int i = 0; i < size; i++) {
				View v = apiViewList.get(i);
				if (v != null) {
					vl.add(v);
				}
		}
	    }
	}
	else {
	    // Only include the non null ones in the apiViewList
	    for (int i = 0; i < size; i++) {
			View v = apiViewList.get(i);
			if (v != null) {
				vl.add(v);
			}
	    }
	}
	if (parentList != null) {
	    parentLists.add(hkIndex, new ArrayList<View>(parentList));
	}
	else {
	    parentLists.add(hkIndex, null);
	}

	viewLists.add(hkIndex,vl);
	s.changedViewGroup.add(this);
	s.changedViewList.add(vl);
	if (localToVworldKeys != null) {
	    s.keyList[s.changedViewGroup.size() -1] = hkIndex;
	}
	else {
	    s.keyList[s.changedViewGroup.size() -1] = 0;
	}



    }

}
