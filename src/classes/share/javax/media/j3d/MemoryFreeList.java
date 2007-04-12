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

import java.util.*;

/**
 * Class for storing various free lists.  This class must be
 * synchronized because different threads may try to access the lists.
 */
class MemoryFreeList {

    // never go smaller than the initial capacity
    ArrayList elementData = null;
    int size = 0;
    int currBlockSize = 10;
    Object[] currBlock = null;
    int currBlockIndex = 0;
    int spaceUsed = 0;
    int numBlocks = 0;
    int capacity = 0;
    int minBlockSize = 0;
    boolean justShrunk = false;
    int initcap = 10;
    
    // the minimum size since the last shrink
    int minSize = 0;

    Class c = null;

    MemoryFreeList(String className) {
	this(className, 10);
    }

    MemoryFreeList(String className, int initialCapacity) {
	if (initialCapacity < 0) {
	    throw new IllegalArgumentException ("Illegal Capacity: " +
						initialCapacity);
	}

	try {
	    c = Class.forName(className);
	}
	catch (Exception e) {
 	    System.err.println(e);
	}
	
	initcap = initialCapacity;
	currBlockSize = initialCapacity;
	minBlockSize = currBlockSize;
	elementData = new ArrayList();
	// add the first block of memory to the arraylist
	currBlock = new Object[currBlockSize];
	elementData.add(currBlock);
	numBlocks++;
	capacity += currBlockSize;
    }

    /*
    MemoryFreeList(String className, Collection collection) { 
	try { 
	    c = Class.forName(className); 
	}  
	catch (Exception e) { 
// 	    System.err.println(e);
	} 

	size = collection.size(); 
	initcap = size; 
	currBlockSize = size; 
	minBlockSize = currBlockSize; 
	elementData = new ArrayList(); 
	currBlock = new Object[currBlockSize]; 
	collection.toArray(currBlock); 
	elementData.add(currBlock); 
	numBlocks++; 
	capacity += currBlockSize; 
	spaceUsed = size; 
    }
    */

    synchronized int size() {
	return size;
    }


    synchronized boolean add(Object o) {
	if (justShrunk) {
	    // empty some space out in the current block instead of
	    // adding this message
	    if ((currBlockSize/2) < spaceUsed) {
		size -= (spaceUsed - (currBlockSize/2));
		spaceUsed = (currBlockSize/2);
		Arrays.fill(currBlock, spaceUsed, currBlockSize-1, null);
	    }
	    justShrunk = false;
	    return false;
	}
	else {
	ensureCapacity(size+1);

	// check to see if the whole block is used and if so, reset the
	// current block
// 	System.err.println("spaceUsed = " + spaceUsed + " currBlockSize = " +
// 			   currBlockSize + " currBlockIndex = " +
// 			   currBlockIndex + " currBlock = " + currBlock);
	if ((currBlockIndex == -1) || (spaceUsed >= currBlockSize)) {
	    currBlockIndex++;
	    currBlock = (Object[])elementData.get(currBlockIndex);
	    currBlockSize = currBlock.length;
	    spaceUsed = 0;
	}
	int index = spaceUsed++;
	currBlock[index] = o;
	size++;
	
	return true;
	}
    }

    protected synchronized Object removeLastElement() {
//   	System.err.println("removeLastElement: size = " + size);
	int index = --spaceUsed;
// 	System.err.println("index = " + index);
	Object elm = currBlock[index];
	currBlock[index] = null;
	size--;

	// see if this block is empty now, and if it is set the previous
	// block to the current block
	if (spaceUsed == 0) {
	    currBlockIndex--;
	    if (currBlockIndex < 0) {
		currBlock = null;
		currBlockSize = 0;
	    }
	    else {
		currBlock = (Object[])elementData.get(currBlockIndex);
		currBlockSize = currBlock.length;
	    }
	    spaceUsed = currBlockSize;
	}

	return elm;
    }


    synchronized void shrink() {
//  	System.err.println("shrink size = " + size + " minSize = " +
//  			   minSize);
	if ((minSize > minBlockSize) && (numBlocks > 1)) {
	    justShrunk = true;
	    
//  	    System.err.println("removing a block");
// 	    Runtime r = Runtime.getRuntime();
// 	    r.gc();
// 	    System.err.println("numBlocks = " + numBlocks + " size = " + size);
// 	    System.err.println("free memory before shrink: " + r.freeMemory());
	    
	    // remove the last block
	    Object[] block = (Object[])elementData.remove(numBlocks-1);
	    numBlocks--;
	    capacity -= block.length;

	    // we only need to do this if the block removed was the current
	    // block.  otherwise we just removed a null block.
	    if (numBlocks == currBlockIndex) {
		size -= spaceUsed;
		// set the current block to the last one
		currBlockIndex = numBlocks-1;
		currBlock = (Object[])elementData.get(currBlockIndex);
		currBlockSize = currBlock.length;

		spaceUsed = currBlockSize;
		
	    }
	    
// 	    r.gc();
// 	    System.err.println("free memory after  shrink: " + r.freeMemory());
// 	    System.err.println("numBlocks = " + numBlocks + " size = " + size);
	}
	else {
	    justShrunk = false;
	}
	minSize = size;
    }

    synchronized void ensureCapacity(int minCapacity) {
// 	System.err.println("ensureCapacity: size = " + size + " capacity: " +
// 			   elementData.length);
// 	System.err.println("minCapacity = " + minCapacity + " capacity = "
// 			   + capacity);
	
	if (minCapacity > capacity) {
// 	    System.err.println("adding a block: numBlocks = " + numBlocks);
	    int lastBlockSize =
		((Object[])elementData.get(numBlocks-1)).length;
	    int prevBlockSize = 0;
	    if (numBlocks > 1) {
		prevBlockSize =
		((Object[])elementData.get(numBlocks-2)).length;
	    }
	    currBlockSize = lastBlockSize + prevBlockSize;
	    currBlock = new Object[currBlockSize];
	    elementData.add(currBlock);
	    numBlocks++;
	    currBlockIndex++;
	    capacity += currBlockSize;
	    // there is nothing used in this block yet
	    spaceUsed = 0;
	}
    }

    synchronized void rangeCheck(int index) {
	if (index >= size || index < 0) {
	    throw new IndexOutOfBoundsException("Index: " + index +
						", Size: " + size);
	}
    }

    public synchronized void clear() {
// 	System.err.println("clear");
	elementData.clear();

	// put an empty block in
	currBlockSize = initcap;
	minBlockSize = currBlockSize;
	currBlock = new Object[currBlockSize];
	elementData.add(currBlock);
	numBlocks = 1;
	capacity = currBlockSize;
	spaceUsed = 0;
	size = 0;
	currBlockIndex = 0;
	justShrunk = false;
    }

    synchronized Object getObject() {
	if (size > 0) {
	    return removeLastElement();
	}
	else {
	    try {
		return c.newInstance();
	    }
	    catch (Exception e) {
		System.err.println(e);
		return null;
	    }
	}
    }
	    
}

