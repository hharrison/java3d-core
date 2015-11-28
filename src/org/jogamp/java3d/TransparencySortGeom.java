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

/**
 * An interface that allows sorting the rendering order of transparent geometry.
 *
 * @since Java 3D 1.6
 */
public interface TransparencySortGeom {

/**
 * Returns the Geometry for this object.
 * @return geometry for this object
 */
public Geometry getGeometry();

/**
 * Returns the distance squared of this object to the viewer.
 * @return distancesquared to viewer
 */
public double getDistanceSquared();

/**
 * Returns the LocalToVWorld transform for this object
 * @param localToVW variable in which transform will be returned
 */
public void getLocalToVWorld(Transform3D localToVW);

/**
 * Returns the Shape3D being rendered using this geometry.
 * @return the Shape3D being rendered using this geometry
 */
public Shape3D getShape3D();

}
