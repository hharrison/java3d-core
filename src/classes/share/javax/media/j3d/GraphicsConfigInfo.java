/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
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
