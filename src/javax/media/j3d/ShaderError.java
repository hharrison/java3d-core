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

import java.io.PrintStream;

/**
 * ShaderError is a container object that holds the details of
 * a runtime error that occurs while compiling or executing a
 * programmable shader.
 *
 * @since Java 3D 1.4
 */
public class ShaderError extends Object {
    private int errorCode = NO_ERROR;
    private String errorMessage = null;
    private String detailMessage = null;
    private Canvas3D canvas = null;
    private Shape3D shape = null;
    private Geometry geometry = null;
    private ShaderAppearance shaderApp = null;
    private ShaderProgram shaderProgram = null;
    private Shader shader = null;
    private ShaderAttributeSet shaderAttributeSet = null;
    private ShaderAttribute shaderAttribute = null;

    /**
     * Indicates that no error occurred.
     */
    public static final int NO_ERROR = 0;

    /**
     * Indicates that an error occurred while compiling a shader.
     */
    public static final int COMPILE_ERROR = 1;

    /**
     * Indicates that an error occurred while linking a shader.
     */
    public static final int LINK_ERROR = 2;

    /**
     * Indicates a error in looking up a vertex attribute
     * name within a given shader program.
     */
    public static final int VERTEX_ATTRIBUTE_LOOKUP_ERROR = 3;

    /**
     * Indicates a error in looking up the location of a uniform
     * shader attribute name within a given shader program.
     */
    public static final int SHADER_ATTRIBUTE_LOOKUP_ERROR = 4;

    /**
     * Indicates a error caused by a ShaderAttribute whose name does not
     * appear in the list of shader attribute names in the corresponding
     * ShaderProgram object.
     */
    public static final int SHADER_ATTRIBUTE_NAME_NOT_SET_ERROR = 5;

    /**
     * Indicates a error in the type of the attribute versus what the shader
     * program was expecting.
     */
    public static final int SHADER_ATTRIBUTE_TYPE_ERROR = 6;

    /**
     * Indicates that the specified shading language is not supported
     * on the screen display device.
     */
    public static final int UNSUPPORTED_LANGUAGE_ERROR = 7;


    /**
     * Constructs a new ShaderError object indicating no error. The
     * error code is set to <code>NO_ERROR</code>.  All other fields
     * are initialized to null, including the error message.
     */
    public ShaderError() {
    }

    /**
     * Constructs a new ShaderError object with the given error code
     * and message.  All other fields are initialized to null.
     *
     * @param errorCode the error code for this shader error.
     *
     * @param errorMessage a short error message describing this
     * shader error.
     */
    public ShaderError(int errorCode, String errorMessage) {
	this.errorCode = errorCode;
	this.errorMessage = errorMessage;
    }

    /**
     * Prints a verbose error report to System.err. This verbose
     * output includes the error code, error message, detail message,
     * and all relevant Java 3D objects.
     */
    public void printVerbose() {
	printVerbose(System.err);
    }

    /**
     * Prints a verbose error report to the specified PrintStream.
     * This verbose output includes the error code, error message,
     * detail message, and all relevant Java 3D objects.
     *
     * @param printStream the print stream on which to print the error
     * report.
     */
    public void printVerbose(PrintStream printStream) {
	printStream.println(this);
        if (canvas != null) {
            printStream.println("canvas = " + canvas);
        }
        if (shape != null) {
            printStream.println("shape = " + shape);
        }
	if (geometry != null) {
	    printStream.println("geometry = " + geometry);
	}
	if (shaderApp != null) {
	    printStream.println("shaderApp = " + shaderApp);
	}
	if (shaderProgram != null) {
	    printStream.println("shaderProgram = " + shaderProgram);
	}
	if (shader != null) {
	    printStream.println("shader = " + shader);
	}
	if (shaderAttributeSet != null) {
	    printStream.println("shaderAttributeSet = " + shaderAttributeSet);
	}
	if (shaderAttribute != null) {
	    printStream.println("shaderAttribute = " + shaderAttribute);
	}

        if (detailMessage != null) {
	    printStream.println();
	    printStream.println("Detail Message");
	    printStream.println("--------------");
	    printStream.println(detailMessage);
	}
    }

    /**
     * Sets the error code for this shader error. This represents the
     * type of error that occurred.
     *
     * @param errorCode the error code for this shader error.
     */
    public void setErrorCode(int errorCode) {
	this.errorCode = errorCode;
    }

    /**
     * Returns the error code for this shader error.
     *
     * @return the error code.
     */
    public int getErrorCode() {
	return errorCode;
    }

    /**
     * Sets the error message for this shader error. This is a short
     * message describing the error, and is included as part of
     * toString().
     *
     * @param errorMessage a short error message describing this
     * shader error.
     */
    public void setErrorMessage(String errorMessage) {
	this.errorMessage = errorMessage;
    }

    /**
     * Returns the error message for this shader error.
     *
     * @return a short error message describing this shader error.
     */
    public String getErrorMessage() {
	return errorMessage;
    }

    /**
     * Sets the detail message for this shader error. This is a
     * detailed error message, typically produced by the shader
     * compiler, and is not included as part of toString().
     *
     * @param detailMessage a detailed message describing this shader
     * error in more detail.
     */
    public void setDetailMessage(String detailMessage) {
	this.detailMessage = detailMessage;
    }

    /**
     * Returns the detail message for this shader error.
     *
     * @return the detail message for this shader error.
     */
    public String getDetailMessage() {
	return detailMessage;
    }

    /**
     * Sets the canvas associated with this shader error.
     *
     * @param canvas the canvas associated with this shader error.
     */
    public void setCanvas3D(Canvas3D canvas) {
	this.canvas = canvas;
    }

    /**
     * Returns the canvas associated with this shader error.
     *
     * @return the canvas associated with this shader error.
     */
    public Canvas3D getCanvas3D() {
	return this.canvas;
    }

    /**
     * Sets the shape node associated with this shader error.
     *
     * @param shape the shape node associated with this shader error.
     */
    public void setShape3D(Shape3D shape) {
	this.shape = shape;
    }

    /**
     * Returns the shape node associated with this shader error.
     *
     * @return the shape node associated with this shader error.
     */
    public Shape3D getShape3D() {
	return this.shape;
    }

    /**
     * Sets the geometry associated with this shader error.
     *
     * @param geometry the geometry associated with this shader error.
     */
    public void setGeometry(Geometry geometry) {
	this.geometry = geometry;
    }

    /**
     * Returns the geometry associated with this shader error.
     *
     * @return the geometry associated with this shader error.
     */
    public Geometry getGeometry() {
	return this.geometry;
    }

    /**
     * Sets the shader appearance associated with this shader error.
     *
     * @param shaderApp the shader appearance associated with this shader error.
     */
    public void setShaderAppearance(ShaderAppearance shaderApp) {
	this.shaderApp = shaderApp;
    }

    /**
     * Returns the shader appearance associated with this shader error.
     *
     * @return the shader appearance associated with this shader error.
     */
    public ShaderAppearance getShaderAppearance() {
	return this.shaderApp;
    }

    /**
     * Sets the shader program associated with this shader error.
     *
     * @param shaderProgram the shader program associated with this shader error.
     */
    public void setShaderProgram(ShaderProgram shaderProgram) {
	this.shaderProgram = shaderProgram;
    }

    /**
     * Returns the shader program associated with this shader error.
     *
     * @return the shader program associated with this shader error.
     */
    public ShaderProgram getShaderProgram() {
	return this.shaderProgram;
    }

    /**
     * Sets the shader object associated with this shader error.
     *
     * @param shader the shader object associated with this shader error.
     */
    public void setShader(Shader shader) {
	this.shader = shader;
    }

    /**
     * Returns the shader object associated with this shader error.
     *
     * @return the shader object associated with this shader error.
     */
    public Shader getShader() {
	return this.shader;
    }

    /**
     * Sets the shader attribute set associated with this shader error.
     *
     * @param shaderAttributeSet the shader attribute set associated with this shader error.
     */
    public void setShaderAttributeSet(ShaderAttributeSet shaderAttributeSet) {
	this.shaderAttributeSet = shaderAttributeSet;
    }

    /**
     * Returns the shader attribute set associated with this shader error.
     *
     * @return the shader attribute set associated with this shader error.
     */
    public ShaderAttributeSet getShaderAttributeSet() {
	return this.shaderAttributeSet;
    }

    /**
     * Sets the shader attribute associated with this shader error.
     *
     * @param shaderAttribute the shader attribute associated with this shader error.
     */
    public void setShaderAttribute(ShaderAttribute shaderAttribute) {
	this.shaderAttribute = shaderAttribute;
    }

    /**
     * Returns the shader attribute associated with this shader error.
     *
     * @return the shader attribute associated with this shader error.
     */
    public ShaderAttribute getShaderAttribute() {
	return this.shaderAttribute;
    }


    /**
     * Returns a short string that describes this shader error. The
     * string is composed of the textual description of the errorCode,
     * a ": ", and the errorMessage field.  If the errorMessage is
     * null then the ": " and the errorMessage are omitted.
     *
     * @return a string representation of this shader error.
     */
    @Override
    public String toString() {
	// Concatenate string representation of error code with error message
	String errorCodeStr;
	switch (errorCode) {
	case NO_ERROR:
	    errorCodeStr = "NO_ERROR";
	    break;
	case COMPILE_ERROR:
	    errorCodeStr = "COMPILE_ERROR";
	    break;
	case LINK_ERROR:
	    errorCodeStr = "LINK_ERROR";
	    break;
	case VERTEX_ATTRIBUTE_LOOKUP_ERROR:
	    errorCodeStr = "VERTEX_ATTRIBUTE_LOOKUP_ERROR";
	    break;
	case SHADER_ATTRIBUTE_LOOKUP_ERROR:
	    errorCodeStr = "SHADER_ATTRIBUTE_LOOKUP_ERROR";
	    break;
        case SHADER_ATTRIBUTE_NAME_NOT_SET_ERROR:
	    errorCodeStr = "SHADER_ATTRIBUTE_NAME_NOT_SET_ERROR";
	    break;
	case SHADER_ATTRIBUTE_TYPE_ERROR:
	    errorCodeStr = "SHADER_ATTRIBUTE_TYPE_ERROR";
	    break;
	case UNSUPPORTED_LANGUAGE_ERROR:
	    errorCodeStr = "UNSUPPORTED_LANGUAGE_ERROR";
	    break;
	default:
	    errorCodeStr = "UNKNOWN ERROR CODE (" + errorCode + ")";
	}

	if (errorMessage == null) {
	    return errorCodeStr;
	}

	return errorCodeStr + ": " + errorMessage;
    }
}
