/*
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
 */

package org.jogamp.java3d;
import java.util.Arrays;

/**
 * A strongly type unorder array list.
 * The operation add(Object o) & remove(int i) take O(1) time.
 * The class is designed to optimize speed. So many reductance
 * procedures call and range check as found in ArrayList are
 * removed.
 *
 * <p>
 * Use the following code to iterate through an array.
 *
 * <pre>
 *  UnorderList  list = new UnorderList(YourClass.class);
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
 * 1) The array return is a copied of internal array.<br>
 * 2) Don't use arr.length , use list.arraySize();<br>
 * 3) No need to do casting for individual element as in
 *    ArrayList.<br>
 * 4) UnorderList is thread safe.
 * </ul>
 */

class UnorderList implements Cloneable, java.io.Serializable  {

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer.
     *
     * It is non-private to enable compiler do inlining for get(),
     * set(), remove() when -O flag turn on.
     */
    transient Object elementData[];

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


    /**
     * Constructs an empty list with the specified initial capacity.
     * and the class data Type
     *
     * @param   initialCapacity   the initial capacity of the list.
     * @param   componentType     class type of element in the list.
     */
     UnorderList(int initialCapacity, Class componentType) {
	this.componentType = componentType;
	this.elementData = (Object[])java.lang.reflect.Array.newInstance(
					 componentType, initialCapacity);
    }

    /**
     * Constructs an empty list.
     * @param   componentType     class type of element in the list.
     */
    UnorderList(Class componentType) {
	this(10, componentType);
    }


    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param   initialCapacity   the initial capacity of the list.
     */
    UnorderList(int initialCapacity) {
	this(initialCapacity, Object.class);
    }


    /**
     * Constructs an empty list.
     * componentType default to Object.
     */
    UnorderList() {
	this(10, Object.class);
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
    synchronized final boolean contains(Object o) {
	if (o != null) { // common case first
	    for (int i=size-1; i >= 0; i--)
		if (o.equals(elementData[i]))
		    return true;
	} else {
	    for (int i=size-1; i >= 0; i--)
		if (elementData[i]==null)
		    return true;
	}
	return false;

    }


    /**
     * Add Object into the list if it is not already exists.
     *
     * @param  o an object to add into the list
     * @return true if object successfully add, false if duplicate found
     */
    synchronized final boolean addUnique(Object o) {
	if (!contains(o)) {
	    add(o);
	    return true;
	}
	return false;
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
    synchronized final int indexOf(Object o) {
	if (o != null) { // common case first
	    for (int i=size-1; i >= 0; i--)
		if (o.equals(elementData[i]))
		    return i;
	} else {
	    for (int i=size-1; i >= 0; i--)
		if (elementData[i]==null)
		    return i;
	}
	return -1;
    }

    /**
     * Returns a shallow copy of this <tt>ArrayList</tt> instance.  (The
     * elements themselves are not copied.)
     *
     * @return  a clone of this <tt>ArrayList</tt> instance.
     */
    @Override
    synchronized protected final Object clone() {
	try {
	    UnorderList v = (UnorderList)super.clone();
	    v.elementData =  (Object[])java.lang.reflect.Array.newInstance(
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
    synchronized final Object[] toArray(Object startElement) {
	int idx = indexOf(startElement);
	if (idx < 0) {
	    return  (Object[])java.lang.reflect.Array.newInstance(componentType, 0);
	}

	int s = size - idx;
	Object data[] = (Object[])java.lang.reflect.Array.newInstance(componentType, s);
	System.arraycopy(elementData, idx, data, 0, s);
	return data;
    }

    // copy element to objs and clear the array
    synchronized final void toArrayAndClear(Object[] objs) {
	System.arraycopy(elementData, 0, objs, 0, size);
	Arrays.fill(elementData, 0, size, null);
	size = 0;
	isDirty = true;
    }


    /**
     * Trims the capacity of this <tt>ArrayList</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>ArrayList</tt> instance.
     */
    synchronized final void trimToSize() {
	if (elementData.length > size) {
	    Object oldData[] = elementData;
	    elementData = (Object[])java.lang.reflect.Array.newInstance(
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
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @throws    IndexOutOfBoundsException if index out of range
     *		  <tt>(index &lt; 0 || index &gt;= size())</tt>.
     */
    synchronized final void set(int index, Object element) {
	elementData[index] = element;
	isDirty = true;
    }

    /**
     * Appends the specified element to the end of this list.
     * It is the user responsible to ensure that the element add is of
     * the same type as array componentType.
     *
     * @param o element to be appended to this list.
     */
     synchronized final void add(Object o) {
	if (elementData.length == size) {
	    Object oldData[] = elementData;
	    elementData = (Object[])java.lang.reflect.Array.newInstance(
						 componentType,
						 (size << 1));
	    System.arraycopy(oldData, 0, elementData, 0, size);

	}
	elementData[size++] = o;
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
	elementData[index] = elementData[--size];
	elementData[size] = null;
	isDirty = true;
	/*
	if ((cloneData != null) && (index < cloneData.length)) {
	    cloneData[index] = null; // for gc
	}
	*/
    }


    /**
     * Removes the element at the specified position in this list.
     * The order is keep.
     *
     * @param index the index of the element to removed.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    synchronized final void removeOrdered(int index) {
	size--;
	if (index < size) {
	    System.arraycopy(elementData, index+1,
			     elementData, index, size-index);

	}
	// gc for last element
	elementData[size] = null;
	isDirty = true;
    }


   /**
     * Removes the element at the last position in this list.
     * @return    The element remove
     * @throws    IndexOutOfBoundsException if array is empty
     */
    synchronized final Object removeLastElement() {
	Object elm = elementData[--size];
	elementData[size] = null;
	isDirty = true;
	/*
	if ((cloneData != null) && (size < cloneData.length)) {
	    cloneData[size] = null; // for gc
	}
	*/
	return elm;
    }


    // Shift element of array from positin idx to position 0
    // Note that idx < size,  otherwise ArrayIndexOutOfBoundsException
    // throws. The element remove are copy to objs.
    synchronized final void shift(Object objs[], int idx) {
	int oldsize = size;

	System.arraycopy(elementData, 0, objs, 0, idx);
	size -= idx;
	if (size > 0) {
	    System.arraycopy(elementData, idx, elementData, 0, size);
	}
	Arrays.fill(elementData, size, oldsize, null);
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
    synchronized final boolean remove(Object o) {
	size--;
	if (o != null) {
	    for (int i=size; i >= 0; i--) {
		if (o.equals(elementData[i])) {
		    elementData[i] = elementData[size];
		    elementData[size] = null;
		    /*
		    if ((cloneData != null) && (i < cloneData.length)) {
			cloneData[i] = null; // for gc
		    }
		    */
		    isDirty = true;
		    return true;
		}
	    }
	} else {
	    for (int i=size; i >= 0; i--)
		if (elementData[i]==null) {
		    elementData[i] = elementData[size];
		    elementData[size] = null;
		    /*
		    if ((cloneData != null) && (i < cloneData.length)) {
			cloneData[i] = null; // for gc
		    }
		    */
		    isDirty = true;
		    return true;
		}
	}
	size++;  // fail to remove
	return false;
    }


    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    synchronized final void clear() {
	if (size > 0) {
	    Arrays.fill(elementData, 0, size, null);
	    size = 0;
	    isDirty = true;
	}
    }

    synchronized final void clearMirror() {
	if (cloneData != null) {
	    Arrays.fill(cloneData, 0, cloneData.length, null);
	}
	cloneSize = 0;
	isDirty = true;
    }

    final Class getComponentType() {
	return componentType;
    }

    @Override
    synchronized public String toString() {
	StringBuffer sb = new StringBuffer("Size = " + size + "\n[");
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
	sb.append("]\n");
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
	elementData = (Object[])java.lang.reflect.Array.newInstance(
					    componentType, arrayLength);

	// Read in all elements in the proper order.
	for (int i=0; i<size; i++)
            elementData[i] = s.readObject();
    }
}
