/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * A strongly type indexed unorder set.
 * All operations remove(IndexedObject, ListType), add(IndexedObject, ListType),
 * contains(IndexedObject, ListType) etc. take O(1) time.
 * The class is designed to optimize speed. So many reductance
 * procedures call and range check as found in ArrayList are 
 * removed.
 *
 * <p>
 * Use the following code to iterate through an array.
 *
 * <pre>
 *  IndexedUnorderSet  IUset =
 *      new IndexedUnorderSet(YourClass.class, listType);
 *  // add element here
 *
 *  YourClass[] arr = (YourClass []) IUset.toArray();
 *  int size = IUset.arraySize();
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
 * 2) Don't use arr.length , use IUset.arraySize();<br>
 * 3) IndexedObject contains an array of listIndex, the number of
 * array elements depends on the number of different types of 
 * IndexedUnorderSet that use it.<br>
 * 4) No need to do casting for individual element as in ArrayList.<br>
 * 5) IndexedUnorderSet is thread safe.<br>
 * 6) Object implement this interface MUST initialize the index to -1.<br>
 * </ul>
 *
 * <p>
 * Limitation:
 * <ul>
 *       1) Order of IndexedObject in list is not important<br>
 *       2) Can't modify the clone() copy.<br>
 *       3) IndexedObject can't be null<br>
 * </ul>
 */

class IndexedUnorderSet implements Cloneable, java.io.Serializable  {

    // XXXX: set to false when release
    final static boolean debug = false;

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer.
     *
     * It is non-private to enable compiler do inlining for get(),
     * set(), remove() when -O flag turn on.
     */
    transient IndexedObject elementData[];

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
     IndexedUnorderSet(int initialCapacity, Class componentType,
		       int listType, VirtualUniverse univ) {
	this.componentType = componentType;
	this.elementData = (IndexedObject[])java.lang.reflect.Array.newInstance(
						componentType, initialCapacity);
	this.listType = listType;
	this.univ = univ;
    }

    /**
     * Constructs an empty list.
     * @param   componentType     class type of element in the list.
     */
     IndexedUnorderSet(Class componentType, int listType,
		       VirtualUniverse univ) {
	this(10, componentType, listType, univ);
     }


    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param   initialCapacity   the initial capacity of the list.
     */
     IndexedUnorderSet(int initialCapacity, int listType,
		       VirtualUniverse univ) {
	 this(initialCapacity, IndexedObject.class, listType, univ);
     }

    /**
     * Constructs an empty list.
     * @param listType default to Object.
     */
     IndexedUnorderSet(int listType, VirtualUniverse univ) {
	this(10, IndexedObject.class, listType, univ);
    }

    /**
     * Initialize all indexes to -1
     */
    final static void init(IndexedObject obj, int len) {
	obj.listIdx = new int[3][];

	obj.listIdx[0] = new int[len];
	obj.listIdx[1] = new int[len];
	obj.listIdx[2] = new int[1];

	for (int i=0; i < len; i++) {
	    obj.listIdx[0][i] = -1;
	    obj.listIdx[1][i] = -1;
	}
	
	// Just want to set both RenderMolecule idx
	// and BehaviorRetained idx to 0 by default
	// It is OK without the following lines
	if (obj instanceof SceneGraphObjectRetained) {
	    // setlive() will change this back to 0
	    obj.listIdx[2][0] = 1;
	} else {
	    obj.listIdx[2][0] = 0;
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
    synchronized final boolean contains(IndexedObject o) {
	return (o.listIdx[o.getIdxUsed(univ)][listType] >= 0);
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
    synchronized final int indexOf(IndexedObject o) {
	return o.listIdx[o.getIdxUsed(univ)][listType];
    }

    /**
     * Returns a shallow copy of this <tt>ArrayList</tt> instance.  (The
     * elements themselves are not copied.)
     *
     * @return  a clone of this <tt>ArrayList</tt> instance.
     */
    synchronized protected final Object clone() {
	try { 
	    IndexedUnorderSet v = (IndexedUnorderSet)super.clone();
	    v.elementData =  (IndexedObject[])java.lang.reflect.Array.newInstance(
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
    synchronized final Object[] toArray(IndexedObject startElement) {
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
	    elementData = (IndexedObject[])java.lang.reflect.Array.newInstance(
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
     synchronized final void set(int index, IndexedObject o) {
	IndexedObject oldElm = elementData[index];
	if (oldElm != null) {
	    oldElm.listIdx[oldElm.getIdxUsed(univ)][listType] = -1;
	}
	elementData[index] = o;
	
	int univIdx = o.getIdxUsed(univ);

	if (debug) {
	    if (o.listIdx[univIdx][listType] != -1) {
		System.err.println("Illegal use of UnorderIndexedList idx in set " + 
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
    synchronized final void add(IndexedObject o) {
	 
	if (elementData.length == size) {
	    IndexedObject oldData[] = elementData;
	    elementData = (IndexedObject[])java.lang.reflect.Array.newInstance(
						 componentType,
						 (size << 1));
	    System.arraycopy(oldData, 0, elementData, 0, size);

	}

	int univIdx = o.getIdxUsed(univ);

	if (debug) {
	    int idx = o.listIdx[univIdx][listType];
	    if (idx >= 0) {
		if (elementData[idx] != o) {
		    System.err.println("Illegal use of UnorderIndexedList idx in add " + idx);
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
	IndexedObject elm = elementData[index];

	int univIdx = elm.getIdxUsed(univ);

	if (debug) {
	    if (elm.listIdx[univIdx][listType] != index) {
		System.err.println("Inconsistent idx in remove, expect " + index + " actual " + elm.listIdx[univIdx][listType]);
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
	IndexedObject elm = elementData[--size];
	elementData[size] = null;
	elm.listIdx[elm.getIdxUsed(univ)][listType] = -1;
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
    synchronized final boolean remove(IndexedObject o) {
	int univIdx = o.getIdxUsed(univ);
	int idx = o.listIdx[univIdx][listType];

	if (idx >= 0) {
	    if (debug) {
		if (o != elementData[idx]) {
		    System.err.println(this + " Illegal use of UnorderIndexedList in remove expect " + o + " actual " + elementData[idx] + " idx = " + idx);
		    Thread.dumpStack();
		}
	    }
	    // Object in the container
	    size--;
	    if (idx != size) {
		IndexedObject elm = elementData[size];
		elementData[idx] = elm;
		elm.listIdx[elm.getIdxUsed(univ)][listType] = idx;
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
	IndexedObject o;
	for (int i = size-1; i >= 0; i--) {
	    o = elementData[i];
	    o.listIdx[o.getIdxUsed(univ)][listType] = -1;
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

    /*
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
    */


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
	elementData = (IndexedObject[])java.lang.reflect.Array.newInstance(
						   componentType, arrayLength);

	// Read in all elements in the proper order.
	for (int i=0; i<size; i++)
            elementData[i] = (IndexedObject) s.readObject();
    }
}
