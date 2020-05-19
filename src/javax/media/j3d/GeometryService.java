/*
 * Copyright (c) 2020 JogAmp Community. All rights reserved.
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
 */
package javax.media.j3d;

import java.util.ArrayList;

import javax.vecmath.Point3f;

/**
 * A service interface for certain geometric operations that are not available
 * in core Java 3D.
 * <p>
 * In particular, the {@code j3d-core-utils} project provides additional
 * functionality under a different license, which is needed in some
 * circumstances by core Java 3D. Thus, historically, these two projects have
 * been co-dependent. This interface breaks the circular dependency by using
 * Java's service discovery mechanism: if {@code j3d-core-utils} is present on
 * the classpath, its {@code GeometryServiceImpl} will provide the functionality
 * defined here. Or if not (i.e., no suitable {@code GeometryService}
 * implementation can be discovered and instantiated}), then the Java3D core
 * will fail as gracefully as possible.
 * </p>
 * 
 * @see Font3D#triangulateGlyphs
 */
public interface GeometryService {

	/**
	 * Loops through each island, calling triangulator once per island. Combines
	 * triangle data for all islands together in one object.
	 * 
	 * @param islandCounts TODO
	 * @param outVerts TODO
	 * @param contourCounts TODO
	 * @param triangData TODO
	 * @return total vertex count of the combined array
	 */
	int triangulateIslands(int[][] islandCounts, Point3f[][] outVerts,
		int[] contourCounts, ArrayList<GeometryArray> triangData);

}
