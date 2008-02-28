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
 *
 * <p>
 * Following is the list of predefined Java&nbsp;3D system attributes:<br>
 *
 * <ul>
 * <font color="#ff0000"><i>TODO: replace the following with
 * the real system attributes table</i></font><br>
 * <table BORDER=1 CELLSPACING=2 CELLPADDING=2>
 * <tr>
 * <td><b>Name</b></td>
 * <td><b>Type</b></td>
 * <td><b>Description</b></td>
 * </tr>
 * <tr>
 * <td><code>something</code></td>
 * <td>Float</td>
 * <td>This is something (of course)</td>
 * </tr>
 * <tr>
 * <td><code>somethingElse</code></td>
 * <td>Tuple3f</td>
 * <td>This is something else</td>
 * </tr>
 * </table>
 * </ul>
 *
 * <p>
 * Depending on the shading language (and profile) being used, several
 * Java 3D state attributes are automatically made available to the
 * shader program as pre-defined uniform attributes. The application
 * doesn't need to do anything to pass these attributes in to the
 * shader program. The implementation of each shader language (e.g.,
 * Cg, GLSL) defines its own mapping from Java 3D attribute to uniform
 * variable name.
 *
 * <p>
 * A list of these attributes for each shader language can be found in
 * the concrete subclass of ShaderProgram for that shader language.
 *
 * <p>
 * <font color="#ff0000"><i>NOTE: This class is not yet
 * implemented.</i></font><br>
 *
 * @see ShaderAttributeSet
 * @see ShaderProgram
 *
 * @since Java 3D 1.4
 */

public class ShaderAttributeBinding extends ShaderAttribute {

    /**
     * Constructs a new ShaderAttributeBinding from the specified
     * <code>(attrName,&nbsp;j3dAttrName)</code> pair.
     *
     * @param attrName the name of the shader attribute to be added
     * @param j3dAttrName the name of the Java&nbsp;3D attribute
     * to bind to the shader attribute
     *
     * @exception UnsupportedOperationException this class is not
     * yet implemented
     *
     * @exception NullPointerException if attrName or j3dAttrName is null
     *
     * @exception IllegalArgumentException if j3dAttrName is not the name
     * of a valid predefined Java&nbsp;3D system attribute
     */
    public ShaderAttributeBinding(String attrName, String j3dAttrName) {
	super(attrName);
	((ShaderAttributeBindingRetained)this.retained).initJ3dAttrName(j3dAttrName);
	// TODO: implement this class
	throw new UnsupportedOperationException(J3dI18N.getString("ShaderAttributeBinding0"));
    }

    /**
     * Retrieves the name of the Java 3D system attribute that is bound to this
     * shader attribute.
     *
     * @return the name of the Java 3D system attribute that is bound to this
     * shader attribute
     */
    public String getJ3DAttributeName() {
 	return ((ShaderAttributeBindingRetained)this.retained).getJ3DAttributeName();
    }

    /**
     * Creates a retained mode ShaderAttributeBindingRetained object that this
     * ShaderAttributeBinding component object will point to.
     */
    void createRetained() {
	this.retained = new ShaderAttributeBindingRetained();
	this.retained.setSource(this);
    }

}
