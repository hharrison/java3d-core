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
 * A strongly type unorder indexed array list.
 * All operations remove(WakeupCondition), add(WakeupCondition),
 * contains(WakeupCondition) etc. take O(1) time.
 * The class is designed to optimize speed. So many reductance
 * procedures call and range check as found in ArrayList are 
 * removed.
 *
 * <p>
 * Use the following code to iterate through an array.
 *
 * <pre>
 *  WakeupIndexedList  list = new WakeupIndexedList(YourClass.class);
 *  // add element here
 *
 *  YourClass[] arr = (YourClass []) list.toArray();
 *  int size = list.arraySize();
 *  for (int i=0; i < size; i++) {
 *      YourClass obj = arr[i];
 *      ....
 *  }
 * </pre>
 *
 * <p>
 * Note:
 * <ul>
 *       1) The array return is a copied of internal array.<br>
 *       2) Don't use arr.length , use list.arraySize();<br>
 *       3) No need to do casting for individual element as in
 *          ArrayList.<br>
 *       4) WakeupIndexedList is thread safe.<br>
 *       5) Object implement this interface MUST initialize the index
 *          to -1.<br>
 * </ul>
 *
 * <p>
 * Limitation:
 * <ul>
 *       - Same element can't add in two different WakeupIndexedList<br>
 *       - Order of WakeupCondition is not important<br>
 *       - Can't modify the clone() copy.<br>
 *       - Object can't be null<br>
 * </ul>
 */

class WakeupIndexedList implements Cloneable, java.io.Serializable  {

    // TODO: set to false when release
    final static boolean debug = false;

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer.
     *
     * It is non-private to enable compiler do inlining for get(),
     * set(), remove() when -O flag turn on.
     */
    transient WakeupCondition elementData[];
    
    /**
     * Clone copy of elementData return by toArray(true);
     */
    transient Object cloneData[];
    // size of the above clone objec.
    transient int cloneSize;

    transient boolean isDirty = true;

    /**
     * Component Type of individual array element entry
     */
    Class componentType;

    /**
     * The size of the ArrayList (the number of elements it contains).
     *
     * We make it non-private to enable compiler do inlining for
     * getSize() when -O flag turn on.
     */
    int size;

    int listType;

    // Current VirtualUniverse using this structure
    VirtualUniverse univ;

    /**
     * Constructs an empty list with the specified initial capacity.
     * and the class data Type
     *
     * @param   initialCapacity   the initial capacity of the list.
     * @param   componentType     class type of element in the list.
     */
    WakeupIndexedList(int initialCapacity, Class componentType,
		      int listType, VirtualUniverse univ) {
	this.componentType = componentType;
	this.elementData = (WakeupCondition[])java.lang.reflect.Array.newInstance(
						componentType, initialCapacity);
	this.listType = listType;
	this.univ = univ;
    }

    /**
     * Constructs an empty list.
     * @param   componentType     class type of element in the list.
     */
    WakeupIndexedList(Class componentType, int listType,
		      VirtualUniverse univ) {
	this(10, componentType, listType, univ);
     }


    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param   initialCapacity   the initial capacity of the list.
     */
    WakeupIndexedList(int initialCapacity, int listType,
		      VirtualUniverse univ) {
	this(initialCapacity, WakeupCondition.class, listType, univ);
    }


    /**
     * Constructs an empty list.
     * componentType default to Object.
     */
    WakeupIndexedList(int listType, VirtualUniverse univ) {
	this(10, WakeupCondition.class, listType, univ);
    }


    /**
     * Initialize all indexes to -1
     */
    final static void init(WakeupCondition obj, int len) {
	obj.listIdx = new int[2][len];

	for (int i=0; i < len; i++) {
	    obj.listIdx[0][i] = -1;
	    obj.listIdx[1][i] = -1;
	}
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return  the number of elements in this list.
     */
    final int size() {
	return size;
    }
  
    /**
     * Returns the size of entry use in toArray() number of elements 
     * in this list.
     *
     * @return  the number of elements in this list.
     */
    final int arraySize() {
	return cloneSize;
    }

    /**
     * Tests if this list has no elements.
     *
     * @return  <tt>true</tt> if this list has no elements;
     *          <tt>false</tt> otherwise.
     */
    final boolean isEmpty() {
	return size == 0;
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     *
     * @param o element whose presence in this List is to be tested.
     */
    synchronized final boolean contains(WakeupCondition o) {
	return (o.listIdx[o.behav.getIdxUsed(univ)][listType] >= 0);
    }


    /**
     * Searches for the last occurence of the given argument, testing 
     * for equality using the <tt>equals</tt> method. 
     *
     * @param   o   an object.
     * @return  the index of the first occurrence of the argument in this
     *          list; returns <tt>-1</tt> if the object is not found.
     * @see     Object#equals(Object)
     */
    synchronized final int indexOf(WakeupCondition o) {
	return o.listIdx[o.behav.getIdxUsed(univ)][listType];
    }

    /**
     * Returns a shallow copy of this <tt>ArrayList</tt> instance.  (The
     * elements themselves are not copied.)
     *
     * @return  a clone of this <tt>ArrayList</tt> instance.
     */
    synchronized protected final Object clone() {
	try { 
	    WakeupIndexedList v = (WakeupIndexedList)super.clone();
	    v.elementData =  (WakeupCondition[])java.lang.reflect.Array.newInstance(
						   componentType, size);
	    System.arraycopy(elementData, 0, v.elementData, 0, size);
	    isDirty = true; // can't use the old cloneData reference
	    return v;
	} catch (CloneNotSupportedException e) { 
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}
    }


    /**
     * Returns an array containing all of the elements in this list.
     * The size of the array may longer than the actual size. Use
     * arraySize() to retrieve the size. 
     * The array return is a copied of internal array. if copy
     * is true.
     *
     * @return an array containing all of the elements in this list
     */
    synchronized final Object[] toArray(boolean copy) {
	if (copy) {
	    if (isDirty) {
		if ((cloneData == null) || cloneData.length < size) {
		    cloneData = (Object[])java.lang.reflect.Array.newInstance(
									      componentType, size);
		}
		System.arraycopy(elementData, 0, cloneData, 0, size);
		cloneSize = size;
		isDirty = false;
	    }
	    return cloneData;
	} else {
	    cloneSize = size;
	    return elementData;
	}
 
    }

    /**
     * Returns an array containing all of the elements in this list.
     * The size of the array may longer than the actual size. Use
     * arraySize() to retrieve the size. 
     * The array return is a copied of internal array. So another
     * thread can continue add/delete the current list. However,
     * it should be noticed that two call to toArray() may return
     * the same copy.
     *
     * @return an array containing all of the elements in this list
     */
    synchronized final Object[] toArray() {
	return toArray(true);
    }


    /**
     * Returns an array containing elements starting from startElement
     * all of the elements in this list. A new array of exact size 
     * is always allocated.
     *
     * @param startElement starting element to copy 
     *
     * @return an array containing elements starting from
     *         startElement, null if element not found.
     *
     */
    synchronized final Object[] toArray(WakeupCondition startElement) {
	int idx = indexOf(startElement);
	if (idx < 0) {
	    return  (Object[])java.lang.reflect.Array.newInstance(componentType, 0);
	}

	int s = size - idx;
	Object data[] = (Object[])java.lang.reflect.Array.newInstance(componentType, s);
	System.arraycopy(elementData, idx, data, 0, s);
	return data;
    }

    /**
     * Trims the capacity of this <tt>ArrayList</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>ArrayList</tt> instance.
     */
    synchronized final void trimToSize() {
	if (elementData.length > size) {
	    Object oldData[] = elementData;
	    elementData = (WakeupCondition[])java.lang.reflect.Array.newInstance(
						 componentType,
						 size);
	    System.arraycopy(oldData, 0, elementData, 0, size);
	}
    }
   

    // Positional Access Operations

    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of element to return.
     * @return the element at the specified position in this list.
     * @throws    IndexOutOfBoundsException if index is out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    synchronized final Object get(int index) {
	return elementData[index];
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index index of element to replace.
     * @param o element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @throws    IndexOutOfBoundsException if index out of range
     *		  <tt>(index &lt; 0 || index &gt;= size())</tt>.
     */
    synchronized final void set(int index, WakeupCondition o) {

	WakeupCondition oldElm = elementData[index];
	if (oldElm != null) {
	    oldElm.listIdx[oldElm.behav.getIdxUsed(univ)][listType] = -1;
	}
	elementData[index] = o;

	int univIdx = o.behav.getIdxUsed(univ);

	if (debug) {
	    if (o.listIdx[univIdx][listType] != -1) {
		System.out.println("Illegal use of UnorderIndexedList idx in set " + 
				   o.listIdx[univIdx][listType]);
		Thread.dumpStack();
	    }
	}

	o.listIdx[univIdx][listType] = index;
	isDirty = true;
    }

    /**
     * Appends the specified element to the end of this list.
     * It is the user responsible to ensure that the element add is of
     * the same type as array componentType.
     *
     * @param o element to be appended to this list.
     */
    synchronized final void add(WakeupCondition o) {
	if (elementData.length == size) {
	    WakeupCondition oldData[] = elementData;
	    elementData = (WakeupCondition[])java.lang.reflect.Array.newInstance(
						 componentType,
						 (size << 1));
	    System.arraycopy(oldData, 0, elementData, 0, size);

	}
	
	int univIdx = o.behav.getIdxUsed(univ);
	//	System.out.println(this + " add " + o + " univ " + univIdx);
	if (debug) {
	    int idx = o.listIdx[univIdx][listType];
	    if (idx >= 0) {
		if (elementData[idx] != o) {
		    System.out.println("Illegal use of UnorderIndexedList idx in add " + idx);
		    Thread.dumpStack();
		}
	    }
	}

	int idx = size++;
	elementData[idx] = o;
	o.listIdx[univIdx][listType] = idx;
	isDirty = true;
    }

  
    /**
     * Removes the element at the specified position in this list.
     * Replace the removed element by the last one.
     *
     * @param index the index of the element to removed.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    synchronized final void remove(int index) {
	WakeupCondition elm = elementData[index];
	int univIdx = elm.behav.getIdxUsed(univ);

	if (debug) {
	    if (elm.listIdx[univIdx][listType] != index) {
		System.out.println("Inconsistent idx in remove, expect " + index + 
				   " actual " + elm.listIdx[univIdx][listType]);
		Thread.dumpStack();	    
	    } 
	}

	elm.listIdx[univIdx][listType] = -1;
	size--;
	if (index != size) {
	    elm = elementData[size];   
	    elm.listIdx[univIdx][listType] = index;
	    elementData[index] = elm;
	} 
	elementData[size] = null;
	isDirty = true;
	/*
	if ((cloneData != null) && (index < cloneData.length)) {
	    cloneData[index] = null; // for gc
	}
	*/
    }

 
   /**
     * Removes the element at the last position in this list.
     * @return    The element remove
     * @throws    IndexOutOfBoundsException if array is empty
     */
    synchronized final Object removeLastElement() {
	WakeupCondition elm = elementData[--size];
	elementData[size] = null;
	elm.listIdx[elm.behav.getIdxUsed(univ)][listType] = -1;
	isDirty = true;
	/*
	if ((cloneData != null) && (size < cloneData.length)) {
	    cloneData[size] = null; // for gc
	}
	*/
	return elm;
    }


    /**
     * Removes the specified element in this list.
     * Replace the removed element by the last one.
     *
     * @param o   the element to removed.
     * @return    true if object remove
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    synchronized final boolean remove(WakeupCondition o) {
	int univIdx = o.behav.getIdxUsed(univ);
	int idx = o.listIdx[univIdx][listType];

	//	System.out.println(this + " remove " + o + " univ " + univIdx);

	if (idx >= 0) {
	    // Object in the container
	    if (debug) {
		if (o != elementData[idx]) {
		    System.out.println(" Illegal use of UnorderIndexedList in remove expect " + o + " actual " + elementData[idx] + " idx = " + idx);
		    Thread.dumpStack();
		}
	    }
	    size--;
	    if (idx != size) {
		WakeupCondition elm = elementData[size];
		elementData[idx] = elm;
		elm.listIdx[elm.behav.getIdxUsed(univ)][listType] = idx;
	    }
	    elementData[size] = null;
	    o.listIdx[univIdx][listType] = -1;
	    isDirty = true;
	    return true;
	} 
	return false;
    }


    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    synchronized final void clear() {
	WakeupCondition o;

	for (int i = size-1; i >= 0; i--) {
	    o = elementData[i];
	    o.listIdx[o.behav.getIdxUsed(univ)][listType] = -1;
	    elementData[i] = null; 	// Let gc do its work
	}

	size = 0;
	isDirty = true;
    }

    synchronized final void clearMirror() {
	if (cloneData != null) {
	    for (int i = cloneData.length-1; i >= 0; i--) {
		// don't set index to -1 since the original
		// copy is using this.
		cloneData[i] = null; 	// Let gc do its work
	    }
	}
	cloneSize = 0;
	isDirty = true;
    }

    final Class getComponentType() {
	return componentType;
    }

    synchronized public String toString() {
	StringBuffer sb = new StringBuffer(hashCode() + " Size = " + size + "[");
	int len = size-1;
	Object obj;
	
	for (int i=0; i < size; i++) {
	    obj = elementData[i];
	    if (obj != null) {
		sb.append(elementData[i].toString());
	    } else {
		sb.append("NULL");
	    }
	    if (i != len) {
		sb.append(", ");
	    }
	}
	sb.append("]");
	return sb.toString();
    }

    /**
     * Save the state of the <tt>ArrayList</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The length of the array backing the <tt>ArrayList</tt>
     *             instance is emitted (int), followed by all of its elements
     *             (each an <tt>Object</tt>) in the proper order.
     */
    private synchronized void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException{
	// Write out element count, and any hidden stuff
	s.defaultWriteObject();

        // Write out array length
        s.writeInt(elementData.length);

	// Write out all elements in the proper order.
	for (int i=0; i<size; i++)
            s.writeObject(elementData[i]);

    }

    /**
     * Reconstitute the <tt>ArrayList</tt> instance from a stream (that is,
     * deserialize it).
     */
    private synchronized void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
	// Read in size, and any hidden stuff
	s.defaultReadObject();

        // Read in array length and allocate array
        int arrayLength = s.readInt();
	elementData = (WakeupCondition[])java.lang.reflect.Array.newInstance(
						   componentType, arrayLength);

	// Read in all elements in the proper order.
	for (int i=0; i<size; i++)
            elementData[i] = (WakeupCondition) s.readObject();
    }
}
