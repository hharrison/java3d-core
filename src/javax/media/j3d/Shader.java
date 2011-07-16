/*
 * $RCSfile$
 *
 * Copyright 2004-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * The Shader object is the abstract base class for programmable
 * shader code. Currently, only text-based source code shaders are
 * supported, so the only subclass of Shader is SourceCodeShader. We
 * leave open the possibility for binary (object code) shaders in the
 * future.
 *
 * <p>
 * Each instance of a Shader object allows an application to specify
 * the source code used in programming the Graphics Pipeline Unit
 * (GPU) of the graphics accelerator. A Shader object is constructed
 * with modes that specify the <i>shading language</i> and the
 * <i>shader type</i>.
 *
 * <p>
 * The shading language specifies the language and runtime environment
 * used to program the GPU. The currently defined shading languages
 * are GLSL (also known as the OpenGL 2.0 shading language) and
 * Cg. Note that not all shading languages are supported on all
 * platforms. It is up to the application or utility to query whether
 * a particular shading language is supported before using it. The
 * value of the <code>shadingLanguage</code> mode is one of:
 * <code>SHADING_LANGUAGE_GLSL</code> or
 * <code>SHADING_LANGUAGE_CG</code>.
 *
 *<p>
 * The shader type specifies whether the shader is a <i>vertex
 * shader</i> or a <i>fragment shader</i>. A vertex shader replaces
 * the fixed-function graphics pipeline for vertex operations
 * (transformation and lighting). A fragment shader replaces the
 * fixed-function graphics pipeline for fragment shading operations
 * (texture mapping, texture application, coloring, shading, and so
 * forth). The value of the <code>shaderType</code> mode is one of:
 * <code>SHADER_TYPE_VERTEX</code> or
 * <code>SHADER_TYPE_FRAGMENT</code>.
 *
 * <p>
 * Both the shading language and shader type are immutable modes of
 * the Shader object.
 *
 * <p>
 * NOTE: Applications should <i>not</i> extend this class.
 *
 * @see ShaderProgram
 * @see Canvas3D#isShadingLanguageSupported
 *
 * @since Java 3D 1.4
 */

public abstract class Shader extends NodeComponent {


    /**
     * This constant indicates the GLSL shading language. It is one
     * of the possible values of the shadingLanguage parameter.
     */
    public static final int SHADING_LANGUAGE_GLSL = 1;

    /**
     * This constant indicates the Cg shading language. It is one
     * of the possible values of the shadingLanguage parameter.
     */
    public static final int SHADING_LANGUAGE_CG = 2;


    /**
     * This constant indicates that the shader type is a vertex
     * shader.  It is one of the possible values of the shaderType
     * parameter.
     */
    public static final int SHADER_TYPE_VERTEX = 1;

    /**
     * This constant indicates that the shader type is a fragment
     * shader.  It is one of the possible values of the shaderType
     * parameter.
     */
    public static final int SHADER_TYPE_FRAGMENT = 2;


    /**
     * Not a public constructor, for internal use
     */
    Shader() {
    }

    /**
     * Package scope constructor so it can't be subclassed by classes
     * outside the javax.media.j3d package.
     */
    Shader(int shadingLanguage, int shaderType) {
	((ShaderRetained)this.retained).initializeShader(shadingLanguage, shaderType);
    }

    /**
     * Returns the shading language of this shader.
     *
     * @return the  shading language of this shader, one of:
     * <code>SHADING_LANGUAGE_GLSL</code> or
     * <code>SHADING_LANGUAGE_CG</code>.
     */
    public int getShadingLanguage() {
	return ((ShaderRetained)this.retained).getShadingLanguage();
    }

    /**
     * Returns the type of this shader.
     *
     * @return the shader type, one of:
     * <code>SHADER_TYPE_VERTEX</code> or
     * <code>SHADER_TYPE_FRAGMENT</code>.
     */
    public int getShaderType() {
	return ((ShaderRetained)this.retained).getShaderType();
    }
}

