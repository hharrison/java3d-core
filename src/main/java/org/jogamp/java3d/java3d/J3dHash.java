/*
 * Copyright 2013 Harvey Harrison <harvey.harrison@gmail.com>
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

/**
 * A Utility class wrapping the approach used to hash double values in Java3D
 */
class J3dHash {

// prevent an instance from actually being created
private J3dHash() {}

/**
 * Mix the given double into the provided long hash.
 */
static final long mixDoubleBits(long hash, double d) {
	hash *= 31L;
	// Treat 0.0d and -0.0d the same (all zero bits)
	if (d == 0.0d)
		return hash;

	return hash + Double.doubleToLongBits(d);
}

/**
 * Return an integer hash from a long by mixing it with itself.
 */
static final int finish(long hash) {
	return (int)(hash ^ (hash >> 32));
}
}
