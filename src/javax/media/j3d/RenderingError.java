/*
 * Copyright 2006-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.awt.GraphicsDevice;
import java.io.PrintStream;

/**
 * RenderingError is a container object that holds the details of
 * a runtime error that occurs in the Java 3D rendering system.
 * 
 * @since Java 3D 1.5
 */
public class RenderingError extends Object {
    private int errorCode = NO_ERROR;
    private String errorMessage = null;
    private String detailMessage = null;
    private GraphicsDevice graphicsDevice = null;
    private Canvas3D canvas = null;

    /**
     * Indicates that no error occurred.
     */
    public static final int NO_ERROR = 0;

    /**
     * Indicates that an unexpected rendering exception was caught by the
     * Java 3D renderer thread.
     */
    public static final int UNEXPECTED_RENDERING_ERROR = 1;

    /**
     * Indicates that an error occurred while getting the best graphics
     * configuration or while testing whether a given graphics config is
     * supported.
     */
    public static final int GRAPHICS_CONFIG_ERROR = 2;

    /**
     * Indicates that an error occurred while creating an OpenGL or D3D
     * graphics context. This can happen either when querying
     * the Canvas3D properties or when rendering.
     */
    public static final int CONTEXT_CREATION_ERROR = 3;

    /**
     * Indicates a error in creating a rendering buffer for an off-screen
     * Canvas3D.
     */
    public static final int OFF_SCREEN_BUFFER_ERROR = 4;


    /**
     * Constructs a new RenderingError object indicating no error. The
     * error code is set to <code>NO_ERROR</code>.  All other fields
     * are initialized to null, including the error message.
     */
    public RenderingError() {
    }

    /**
     * Constructs a new RenderingError object with the given error code
     * and message.  All other fields are initialized to null.
     * 
     * @param errorCode the error code for this rendering error.
     * @param errorMessage a short error message describing this
     * rendering error.
     */
    public RenderingError(int errorCode, String errorMessage) {
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
        if (graphicsDevice != null) {
            printStream.println("graphicsDevice = " + graphicsDevice);
        }
        if (canvas != null) {
            printStream.println("canvas = " + canvas);
        }

        if (detailMessage != null) {
	    printStream.println();
	    printStream.println("Detail Message");
	    printStream.println("--------------");
	    printStream.println(detailMessage);
	}
    }

    /**
     * Sets the error code for this rendering error. This represents the
     * type of error that occurred.
     *
     * @param errorCode the error code for this rendering error.
     */
    public void setErrorCode(int errorCode) {
	this.errorCode = errorCode;
    }

    /**
     * Returns the error code for this rendering error.
     *
     * @return the error code.
     */
    public int getErrorCode() {
	return errorCode;
    }

    /**
     * Sets the error message for this rendering error. This is a short
     * message describing the error, and is included as part of
     * toString().
     *
     * @param errorMessage a short error message describing this
     * rendering error.
     */
    public void setErrorMessage(String errorMessage) {
	this.errorMessage = errorMessage;
    }

    /**
     * Returns the error message for this rendering error.
     *
     * @return a short error message describing this rendering error.
     */
    public String getErrorMessage() {
	return errorMessage;
    }

    /**
     * Sets the detail message for this rendering error. This is a more
     * detailed error message that is not included as part of toString().
     *
     * @param detailMessage a detailed message describing this
     * error in more detail.
     */
    public void setDetailMessage(String detailMessage) {
	this.detailMessage = detailMessage;
    }

    /**
     * Returns the detail message for this rendering error.
     *
     * @return the detail message for this rendering error.
     */
    public String getDetailMessage() {
	return detailMessage;
    }

    /**
     * Sets the graphics device associated with this rendering error.
     *
     * @param graphicsDevice the graphics device associated with this rendering error.
     */
    public void setGraphicsDevice(GraphicsDevice graphicsDevice) {
	this.graphicsDevice = graphicsDevice;
    }

    /**
     * Returns the graphics device associated with this rendering error.
     *
     * @return the graphics device associated with this rendering error.
     */
    public GraphicsDevice getGraphicsDevice() {
	return this.graphicsDevice;
    }

    /**
     * Sets the canvas associated with this rendering error.
     *
     * @param canvas the canvas associated with this rendering error.
     */
    public void setCanvas3D(Canvas3D canvas) {
	this.canvas = canvas;
    }

    /**
     * Returns the canvas associated with this rendering error.
     *
     * @return the canvas associated with this rendering error.
     */
    public Canvas3D getCanvas3D() {
	return this.canvas;
    }


    /**
     * Returns a short string that describes this rendering error. The
     * string is composed of the textual description of the errorCode,
     * a ": ", and the errorMessage field.  If the errorMessage is
     * null then the ": " and the errorMessage are omitted.
     *
     * @return a string representation of this rendering error.
     */
    public String toString() {
	// Concatenate string representation of error code with error message
	String errorCodeStr;
	switch (errorCode) {
	case NO_ERROR:
	    errorCodeStr = "NO_ERROR";
	    break;
	case UNEXPECTED_RENDERING_ERROR:
	    errorCodeStr = "UNEXPECTED_RENDERING_ERROR";
	    break;
	case GRAPHICS_CONFIG_ERROR:
	    errorCodeStr = "GRAPHICS_CONFIG_ERROR";
	    break;
	case CONTEXT_CREATION_ERROR:
	    errorCodeStr = "CONTEXT_CREATION_ERROR";
	    break;
	case OFF_SCREEN_BUFFER_ERROR:
	    errorCodeStr = "OFF_SCREEN_BUFFER_ERROR";
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
