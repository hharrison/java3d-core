/*
 * Copyright 2013 Harvey Harrison
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
 */
package org.jogamp.java3d;

import java.util.Comparator;
import java.util.WeakHashMap;

/**
 * A class mapping Views to a TransparencySortGeom object.
 */
public class TransparencySortMap {

private static final WeakHashMap<View, Comparator<TransparencySortGeom>> comparators = new WeakHashMap<View, Comparator<TransparencySortGeom>>();

// prevent an instance from being created.
private TransparencySortMap() {}

/**
 * Set the comparator for the specified view.
 * @param view the view to which the comparator applies
 * @param comparator the comparator to call
 */
public static void setComparator(View view, Comparator<TransparencySortGeom> comparator) {
	comparators.put(view, comparator);
}

/**
 * Returns the comparator for the specified view
 * @return the comparator for the specified view, or null if there is no
 *         comparator for the view or the view is unknown.
 */
public static Comparator<TransparencySortGeom> getComparator(View view) {
	return comparators.get(view);
}
}
