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

import javax.vecmath.*;

/**
 * The ShaderAttributeRetained object encapsulates a uniform attribute for a
 * shader programs.
 */

abstract class ShaderAttributeRetained extends NodeComponentRetained {
    // A list of pre-defined bits to indicate which component
    // in this ShaderAttribute object changed.
    static final int SHADER_ATTRIBUTE_VALUE_UPDATE            = 0x001;
    
    /**
     * Name of the shader attribute (immutable)
     */
    String attrName;

    /**
     * Package scope constructor
     */
    ShaderAttributeRetained() {
    }

    void initializeAttrName(String attrName) {
	this.attrName = attrName;
    }

    /**
     * Retrieves the name of this shader attribute.
     *
     * @return the name of this shader attribute
     */
    String getAttributeName() {
	return attrName;
    }

    void initMirrorObject() {
	((ShaderAttributeObjectRetained)mirror).initializeAttrName(this.attrName);
    }

}
