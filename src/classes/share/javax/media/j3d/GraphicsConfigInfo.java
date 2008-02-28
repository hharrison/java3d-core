/*
 * $RCSfile$
 *
 * Copyright 2005-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Container for the GraphicsTemplate3D and other private data about a selected
 * GraphicsConfiguration. An instance of this class is created along with an
 * instance of GrahpicsConfiguration, whenever getBestConfiguration is called.
 */
class GraphicsConfigInfo {
    private GraphicsConfigTemplate3D graphicsConfigTemplate3D = null;
    private Object privateData = null;

    GraphicsConfigInfo(GraphicsConfigTemplate3D graphicsConfigTemplate3D) {
        setGraphicsConfigTemplate3D(graphicsConfigTemplate3D);
    }

    GraphicsConfigTemplate3D getGraphicsConfigTemplate3D() {
        return graphicsConfigTemplate3D;
    }

    void setGraphicsConfigTemplate3D(GraphicsConfigTemplate3D graphicsConfigTemplate3D) {
        this.graphicsConfigTemplate3D = graphicsConfigTemplate3D;
    }

    Object getPrivateData() {
        return privateData;
    }

    void setPrivateData(Object privateData) {
        this.privateData = privateData;
    }

}
