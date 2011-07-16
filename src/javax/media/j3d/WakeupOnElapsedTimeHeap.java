/*
 * $RCSfile$
 *
 * Copyright 1999-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * A Binary heap to store WakeupOnElapsedTime. It is arranged so that the 
 * smallest triggeredTime of the wakeup object is put at the top of the heap.
 * Add/deletion takes O(log n) time. 
 * For better performance we can consider to use Fibonacci Heaps.
 *
 */
class WakeupOnElapsedTimeHeap implements Cloneable {

    // entry 0 is not used so index can be calculated more efficiently
    WakeupOnElapsedTime data[]; 
    int size = 0;

    /**
     * Construct heap with user-defined capacity
     */
    WakeupOnElapsedTimeHeap(int initCapacity) {
	data = new WakeupOnElapsedTime[initCapacity+1];
    }

    /**
     * Construct heap of default capacity 10
     */
    WakeupOnElapsedTimeHeap() {
	this(10);
    }

    /**
     * Return size of heap
     */
    final int size() {
	return size;
    }

    /**
     * Return true if heap is empty
     */
    final boolean isEmpty() {
	return (size == 0);
    }

    /**
     * Get the minimum element from the heap.
     * User has to make sure that size > 0 before it is called.
     */
    final WakeupOnElapsedTime getMin() {
	return data[1]; 
    }
    

    /**
     * Insert the key into the heap
     */
    final void insert(WakeupOnElapsedTime key) {
	if (data.length == size + 1) {
	    WakeupOnElapsedTime oldData[] = data;
	    data = new WakeupOnElapsedTime[oldData.length << 1];
	    System.arraycopy(oldData, 0, data, 0, oldData.length);
	}

	int i = ++size;

	int parentIdx = i >> 1;
	WakeupOnElapsedTime parentKey = data[parentIdx];
	long time = key.triggeredTime;

	while ((i > 1) && (parentKey.triggeredTime > time)) {
	    data[i] = parentKey;
	    i = parentIdx;
	    parentIdx >>= 1;
	    parentKey = data[parentIdx];
	}
	data[i] = key;
    }

    /**
     * Extract wakeup condition belongs to behav from the heap.
     * Return true if wakeup is found.
     */
    final void extract(BehaviorRetained behav) {
	for (int i=1; i <= size; i++) {
	    if (data[i].behav == behav) {
		extract(i);
	    }
	}
    }

    /**
     * Extract wakeup from the heap.
     * Return true if wakeup is found.
     */
    final boolean extract(WakeupOnElapsedTime wakeup) {
	for (int i=1; i <= size; i++) {
	    if (data[i] == wakeup) {
		extract(i);
		return true;
	    }
	}
	return false;
    }

    /**
     * Extract the minimum value from the heap.
     * User has to make sure that size > 0 before it is called.
     */
    final WakeupOnElapsedTime extractMin() {
	return extract(1);
    }

    /**
     * Extract the ith value from the heap.
     * User has to make sure that i <= size before it is called.
     */
    final WakeupOnElapsedTime extract(int i) {
	WakeupOnElapsedTime min = data[i];
	WakeupOnElapsedTime temp;
	int l, r;
	int smallest;
	data[i] = data[size];
	data[size] = null; // for gc
	size--;


	do {
	    l = i << 1;
	    r = l+1;

	    if ((l <= size) && 
		(data[l].triggeredTime < data[i].triggeredTime)) {
		smallest = l;
	    } else {
		smallest = i;
	    }
	    if ((r <= size) && 
		(data[r].triggeredTime < data[smallest].triggeredTime)) {
		smallest = r;
	    }
	    if (smallest == i) {
		break;
	    }
	    temp = data[smallest];
	    data[smallest] = data[i];
	    data[i] = temp;
	    i = smallest;
	} while (true);
	
	return min;
    }


    /***
     * Trims the capacity of this instance to be the
     * list's current size.  
     */
    final void trimToSize() {
	if (data.length > size+1) {
	    WakeupOnElapsedTime oldData[] = data;
	    data = new WakeupOnElapsedTime[size+1];
	    System.arraycopy(oldData, 0, data, 0, data.length);
	}
    }

    /**
     * Clone this heap
     */
    protected final Object clone() {
	try { 
	    WakeupOnElapsedTimeHeap heap = (WakeupOnElapsedTimeHeap)super.clone();
	    heap.data =  new WakeupOnElapsedTime[size+1];
	    System.arraycopy(data, 0, heap.data, 0, size+1);
	    return heap;
	} catch (CloneNotSupportedException e) { 
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}

    }
    

    public String toString() {
	StringBuffer sb = new StringBuffer("[ ");
	
	if (size > 0) {
	    sb.append(data[1]);
	}

	for (int i=2; i <= size; i++) {
	    sb.append("," + data[i]);
	}
	sb.append(" ]");
	return sb.toString();
    }



}
