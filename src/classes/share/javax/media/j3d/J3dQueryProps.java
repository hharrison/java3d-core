/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
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
 * Properties object for query operations.  It is a read-only Map backed
 * up by a Hashtable.
 */
class J3dQueryProps extends AbstractMap {
    private Hashtable table;
    private Set entrySet = null;


    /**
     * Constructs a new J3dQueryProps object using the specified
     * array of keys and the specified values.  The arrays must be
     * the same size.
     */
    J3dQueryProps(String[] keys, Object[] values) {
	table = new Hashtable();
	for (int i = 0; i < keys.length; i++) {
	    table.put(keys[i], values[i]);
	}
    }

    /**
     * Gets value corresponding to specified key
     */
    public Object get(Object key) {
	return table.get(key);
    }

    /**
     * Returns true if the specified key is contained in this Map
     */
    public boolean containsKey(Object key) {
	return table.containsKey(key);
    }

    /**
     * Returns true if the specified value is contained in this Map
     */
    public boolean containsValue(Object value) {
	return table.containsValue(value);
    }

    /**
     * Returns a new Set object for the entries of this map
     */
    public Set entrySet() {
	if (entrySet == null)
	    entrySet = new EntrySet();

	return entrySet;
    }


    /**
     * Entry set class
     */
    private class EntrySet extends AbstractSet {
	private EntrySet() {
	}

	public int size() {
	    return table.size();
	}

	public Iterator iterator() {
	    return new MapIterator();
	}
    }


    /**
     * Entry set class
     */
    private class MapIterator implements Iterator {
	private Iterator i;

	private MapIterator() {
	    i = table.entrySet().iterator();
	}

	public boolean hasNext() {
	    return i.hasNext();
	}

	public Object next() {
	    return i.next();
	}

	public void remove() {
	    throw new UnsupportedOperationException();
	}
    }
}
