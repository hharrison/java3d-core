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

import javax.vecmath.*;

/**
 * The ShaderAttributeBinding object encapsulates a uniform attribute
 * whose value is bound to a Java&nbsp;3D system attribute. The
 * shader variable <code>attrName</code> is implicitly set to the
 * value of the corresponding Java&nbsp;3D system attribute
 * <code>j3dAttrName</code> during rendering. <code>attrName</code>
 * must be the name of a valid uniform attribute in the shader in
 * which it is used. Otherwise, the attribute name will be ignored and
 * a runtime error may be generated. <code>j3dAttrName</code> must be
 * the name of a predefined Java&nbsp;3D system attribute. An
 * IllegalArgumentException will be thrown if the specified
 * <code>j3dAttrName</code> is not one of the predefined system
 * attributes. Further, the type of the <code>j3dAttrName</code>
 * attribute must match the type of the corresponding
 * <code>attrName</code> variable in the shader in which it is
 * used. Otherwise, the shader will not be able to use the attribute
 * and a runtime error may be generated.
 */

class ShaderAttributeBindingRetained extends ShaderAttributeRetained {
    String j3dAttrName;

    ShaderAttributeBindingRetained() {
    }

    void initJ3dAttrName(String j3dAttrName) {
	this.j3dAttrName = j3dAttrName;
    }

    /**
     * Retrieves the name of the Java 3D system attribute that is bound to this
     * shader attribute.
     *
     * @return the name of the Java 3D system attribute that is bound to this
     * shader attribute
     */
    String getJ3DAttributeName() {
	return j3dAttrName;
    }

}
