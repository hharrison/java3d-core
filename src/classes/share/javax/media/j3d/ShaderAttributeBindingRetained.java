/*
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
 */

package javax.media.j3d;


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
