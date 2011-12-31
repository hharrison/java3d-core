/*
 * $RCSfile$
 *
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import javax.vecmath.Color3f;

/**
 * An ambient light source object. Ambient light is that light
 * that seems to come from all directions. The AmbientLight object
 * has the same attributes as a Light node, including color,
 * influencing bounds, scopes, and
 * a flag indicating whether this light source is on or off.
 * Ambient reflections do not depend on the orientation or
 * position of a surface.
 * Ambient light has only an ambient reflection component.
 * It does not have diffuse or specular reflection components.
 * <p>
 * For more information on Java 3D lighting, see the class description
 * for Light.
 * <p>
 */

public class AmbientLight extends Light {
    /**
     * Constructs and initializes an ambient light using default parameters.
     */
    public AmbientLight() {
    }


    /**
     * Constructs and initializes an ambient light using the specified
     * parameters.
     * @param color the color of the light source.
     */
    public AmbientLight(Color3f color) {
	super(color);
    }


    /**
     * Constructs and initializes an ambient light using the specified
     * parameters.
     * @param lightOn flag indicating whether this light is on or off.
     * @param color the color of the light source.
     */
    public AmbientLight(boolean lightOn, Color3f color) {
	super(lightOn, color);
    }

    /**
     * Creates the retained mode AmbientLightRetained object that this
     * AmbientLight component object will point to.
     */
    void createRetained() {
        this.retained = new AmbientLightRetained();
        this.retained.setSource(this);
    }

    /**
     * Used to create a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see Node#duplicateNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneNode(boolean forceDuplicate) {
        AmbientLight a = new AmbientLight();
        a.duplicateNode(this, forceDuplicate);
        return a;
    }

}
