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

/**
 * The RenderingAttributes object defines common rendering attributes
 * for all primitive types. The rendering attributes are:<p>
 * <ul>
 * <li>Depth test function - used to compare the incoming (source) depth of
 * each pixel with depth of the pixel stored in frame buffer. If the test
 * passes, the pixel is written, otherwise the pixel is not written. The depth test
 * function is set with the <code>setDepthTestFunction</code>
 * method. By default, LESS_OR_EQUAL is the function used. The depth test
 * function is one of the following:</li><p>
 * <ul>
 * <li>ALWAYS - pixels are always drawn, irrespective of the depth
 * value. This effectively disables depth testing.</li><p>
 *
 * <li>NEVER - pixels are never drawn, irrespective of the depth
 * value.</li><p>
 *
 * <li>EQUAL - pixels are drawn if the incoming pixel depth is equal
 * to the stored pixel depth in the frame buffer.</li><p>
 *
 * <li>NOT_EQUAL - pixels are drawn if the incoming pixel depth is
 * not equal to the stored pixel depth in the frame buffer.</li><p>
 * 
 * <li>LESS - pixels are drawn if the incoming pixel depth is less
 * than the stored pixel depth in the frame buffer.</li><p>
 * 
 * <li>LESS_OR_EQUAL - pixels are drawn if the incoming pixel depth
 * is less than or equal to the stored pixel depth in the frame buffer.
 * This is the default setting.</li><p>
 * 
 * <li>GREATER - pixels are drawn if the incoming pixel depth is greater
 * than the stored pixel depth in the frame buffer.</li><p>
 * 
 * <li>GREATER_OR_EQUAL - pixels are drawn if the incoming pixel depth
 * is greater than or equal to the stored pixel depth in the frame buffer.</li><p>
 * </ul>
 *
 * <li>Alpha test function - used to compare the incoming (source) alpha value
 * of each pixel with the alpha test value. If the test passes, the pixel is
 * written, otherwise the pixel is not written. The alpha test
 * function is set with the <code>setAlphaTestFunction</code>
 * method. The alpha test
 * function is one of the following:</li><p>
 * <ul>
 * <li>ALWAYS - pixels are always drawn, irrespective of the alpha
 * value. This effectively disables alpha testing.
 * This is the default setting.</li><p>
 *
 * <li>NEVER - pixels are never drawn, irrespective of the alpha
 * value.</li><p>
 *
 * <li>EQUAL - pixels are drawn if the incoming pixel alpha value is equal
 * to the alpha test value.</li><p>
 *
 * <li>NOT_EQUAL - pixels are drawn if the incoming pixel alpha value is
 * not equal to the alpha test value.</li><p>
 * 
 * <li>LESS - pixels are drawn if the incoming pixel alpha value is less
 * than the alpha test value.</li><p>
 * 
 * <li>LESS_OR_EQUAL - pixels are drawn if the incoming pixel alpha value
 * is less than or equal to the alpha test value.</li><p>
 * 
 * <li>GREATER - pixels are drawn if the incoming pixel alpha value is greater
 * than the alpha test value.</li><p>
 * 
 * <li>GREATER_OR_EQUAL - pixels are drawn if the incoming pixel alpha
 * value is greater than or equal to the alpha test value.</li><p>
 * </ul>
 *
 * <li>Alpha test value - the test value used by the alpha test function.
 * This value is compared to the alpha value of each rendered pixel.
 * The alpha test value is set with the <code>setAlphaTestValue</code>
 * method. The default alpha test value is 0.0.</li><p>
 * 
 * <li>Raster operation - the raster operation function for this
 * RenderingAttributes component object. The raster operation is
 * set with the <code>setRasterOp</code> method. The raster operation
 * is enabled or disabled with the <code>setRasterOpEnable</code>
 * method. The raster operation is one of the following:</li><p>
 * <ul>
 * <li>ROP_CLEAR - DST = 0.</li>
 * <li>ROP_AND DST = SRC & DST.</li>
 * <li>ROP_AND_REVERSE DST = SRC & ~DST.</li>
 * <li>ROP_COPY - DST = SRC. This is the default operation.</li>
 * <li>ROP_AND_INVERTED - DST = ~SRC & DST.</li>
 * <li>ROP_NOOP - DST = DST.</li>
 * <li>ROP_XOR - DST = SRC ^ DST.</li>
 * <li>ROP_OR - DST = DST | SRC.</li>
 * <li>ROP_NOR - DST = ~( DST | SRC .)</li>
 * <li>ROP_EQUIV - DST = ~( DST ^ SRC .)</li>
 * <li>ROP_INVERT - DST = ~DST.</li>
 * <li>ROP_OR_REVERSE - DST = src | ~DST.</li>
 * <li>ROP_COPY_INVERTED - DST = ~SRC.</li>
 * <li>ROP_OR_INVERTED - DST = ~SRC | DST.</li>
 * <li>ROP_NAND - DST = ~(SRC & DST.)</li>
 * <li>ROP_SET - DST = 1.</li><p>
 * </ul>
 * <li>Vertex colors - vertex colors can be ignored for this
 * RenderingAttributes object. This capability is set with the
 * <code>setIgnoreVertexColors</code> method. If 
 * ignoreVertexColors is false, per-vertex colors are used, when 
 * present in the associated geometry objects, taking
 * precedence over the ColoringAttributes color and the
 * specified Material color(s). If ignoreVertexColors is true, per-vertex
 * colors are ignored. In this case, if lighting is enabled, the 
 * Material diffuse color will be used as the object color.
 * if lighting is disabled, the ColoringAttributes color is
 * used. The default value is false.</li><p>
 * 
 * <li>Visibility flag - when set, invisible objects are
 * not rendered (subject to the visibility policy for
 * the current view), but they can be picked or collided with.
 * This flag is set with the <code>setVisible</code>
 * method. By default, the visibility flag is true.</li><p>
 * 
 * <li>Depth buffer - can be enabled or disabled for this
 * RenderingAttributes component object. The 
 * <code>setDepthBufferEnable</code> method enables
 * or disabled the depth buffer. The 
 * <code>setDepthBufferWriteEnable</code> method enables or disables
 * writing the depth buffer for this object. During the transparent
 * rendering pass, this attribute can be overridden by the
 * depthBufferFreezeTransparent attribute in the View
 * object. Transparent objects include BLENDED transparent and
 * antialiased lines and points. Transparent objects do not
 * include opaque objects or primitives rendered with
 * SCREEN_DOOR transparency. By default, the depth buffer
 * is enabled and the depth buffer write is enabled.</li><p>
 *
 * <li>Stencil buffer - can be enabled or disabled for this RenderingAttributes
 * component object using the <code>setStencilEnable</code> method. If the
 * stencil buffer is disabled, the stencil operation and function are ignored.
 * If a scene graph is rendered on a Canvas3D that does not have a stencil
 * buffer, the stencil buffer will be implicitly disabled for that
 * canvas.</li><p>
 *
 * <li>Stencil write mask - mask that controls which bits of the stencil
 * buffer are written when the stencil buffer is enabled. The default value is
 * <code>~0</code> (all ones).</li><p>
 *
 * <li>Stencil operation - a set of three stencil operations performed
 * when: 1)&nbsp;the stencil test fails; 2)&nbsp;the stencil test passes, but
 * the depth test fails; or 3)&nbsp;both the stencil test and depth test pass.
 * The stencil operations are set with the <code>setStencilOp</code>
 * method. The stencil operation is one of the following:</li><p>
 * <ul>
 * <li>STENCIL_KEEP - keeps the current value (no operation performed).
 * This is the default setting.</li>
 * <li>STENCIL_ZERO - Sets the stencil buffer value to 0.</li>
 * <li>STENCIL_REPLACE - Sets the stencil buffer value to
 * <code>refValue</code>, as specified by <code>setStencilFunction</code>.</li>
 * <li>STENCIL_INCR - Increments the current stencil buffer value.</li>
 * <li>STENCIL_DECR - Decrements the current stencil buffer value.</li>
 * <li>STENCIL_INVERT - Bitwise inverts the current stencil buffer value.</li><p>
 * </ul>
 *
 * <li>Stencil test function - used to compare the stencil reference value with
 * the per-pixel stencil value stored in the frame buffer. If the test passes,
 * the pixel is written, otherwise the pixel is not written. The stencil
 * test function, reference value, and comparison mask are set with the
 * <code>setStencilFunction</code> method. The stencil comparison mask is
 * bitwise-ANDed with both the stencil reference value and the stored stencil
 * value prior to doing the comparison. The default value for the reference value
 * is 0. The default value for the comparison mask is <code>~0</code> (all ones).
 * The stencil test function is one of the following:</li><p>
 * <ul>
 * <li>ALWAYS - pixels are always drawn, irrespective of the stencil
 * value. This effectively disables stencil testing.
 * This is the default setting.</li><p>
 *
 * <li>NEVER - pixels are never drawn, irrespective of the stencil
 * value.</li><p>
 *
 * <li>EQUAL - pixels are drawn if the stencil reference value is equal
 * to the stored stencil value in the frame buffer.</li><p>
 *
 * <li>NOT_EQUAL - pixels are drawn if the stencil reference value is
 * not equal to the stored stencil value in the frame buffer.</li><p>
 * 
 * <li>LESS - pixels are drawn if the stencil reference value is less
 * than the stored stencil value in the frame buffer.</li><p>
 * 
 * <li>LESS_OR_EQUAL - pixels are drawn if the stencil reference value
 * is less than or equal to the stored stencil value in the frame buffer.</li><p>
 * 
 * <li>GREATER - pixels are drawn if the stencil reference value is greater
 * than the stored stencil value in the frame buffer.</li><p>
 * 
 * <li>GREATER_OR_EQUAL - pixels are drawn if the stencil reference value
 * is greater than or equal to the stored stencil value in the frame buffer.</li><p>
 * </ul>
 *
 * </ul>
 *
 * <p>Note: the alpha test, depth test, and stencil functions all use
 * the same enums.</p>
 *
 * @see Appearance
 */
public class RenderingAttributes extends NodeComponent {

    /**
     * Specifies that this RenderingAttributes object
     * allows reading its alpha test value component information.
     */
    public static final int
    ALLOW_ALPHA_TEST_VALUE_READ = CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_ALPHA_TEST_VALUE_READ;

    /**
     * Specifies that this RenderingAttributes object
     * allows writing its alpha test value component information.
     */
    public static final int
    ALLOW_ALPHA_TEST_VALUE_WRITE = CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_ALPHA_TEST_VALUE_WRITE;

    /**
     * Specifies that this RenderingAttributes object
     * allows reading its alpha test function component information.
     */
    public static final int
    ALLOW_ALPHA_TEST_FUNCTION_READ = CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_ALPHA_TEST_FUNCTION_READ;

    /**
     * Specifies that this RenderingAttributes object
     * allows writing its alpha test function component information.
     */
    public static final int
    ALLOW_ALPHA_TEST_FUNCTION_WRITE = CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_ALPHA_TEST_FUNCTION_WRITE;

     /**
     * Specifies that this RenderingAttributes object
     * allows reading its depth test function component information.
     *
     * @since Java 3D 1.4
     */
    public static final int
    ALLOW_DEPTH_TEST_FUNCTION_READ = CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_DEPTH_TEST_FUNCTION_READ;

    /**
     * Specifies that this RenderingAttributes object
     * allows writing its depth test function component information.
     *
     * @since Java 3D 1.4
     */
    public static final int
    ALLOW_DEPTH_TEST_FUNCTION_WRITE = CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_DEPTH_TEST_FUNCTION_WRITE;

    /**
     * Specifies that this RenderingAttributes object
     * allows reading its depth buffer enable and depth buffer write enable
     * component information.
     */
    public static final int
    ALLOW_DEPTH_ENABLE_READ = CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_DEPTH_ENABLE_READ;

    /**
     * Specifies that this RenderingAttributes object
     * allows writing its depth buffer enable and depth buffer write enable
     * component information.
     *
     * @since Java 3D 1.3
     */
    public static final int ALLOW_DEPTH_ENABLE_WRITE =
    CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_DEPTH_ENABLE_WRITE;

    /**
     * Specifies that this RenderingAttributes object
     * allows reading its visibility information.
     *
     * @since Java 3D 1.2
     */
    public static final int ALLOW_VISIBLE_READ =
    CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_VISIBLE_READ;

    /**
     * Specifies that this RenderingAttributes object
     * allows writing its visibility information.
     *
     * @since Java 3D 1.2
     */
    public static final int ALLOW_VISIBLE_WRITE =
    CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_VISIBLE_WRITE;

    /**
     * Specifies that this RenderingAttributes object
     * allows reading its ignore vertex colors information.
     *
     * @since Java 3D 1.2
     */
    public static final int ALLOW_IGNORE_VERTEX_COLORS_READ =
    CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_IGNORE_VERTEX_COLORS_READ;

    /**
     * Specifies that this RenderingAttributes object
     * allows writing its ignore vertex colors information.
     *
     * @since Java 3D 1.2
     */
    public static final int ALLOW_IGNORE_VERTEX_COLORS_WRITE =
    CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_IGNORE_VERTEX_COLORS_WRITE;

    /**
     * Specifies that this RenderingAttributes object
     * allows reading its raster operation information.
     *
     * @since Java 3D 1.2
     */
    public static final int ALLOW_RASTER_OP_READ =
    CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_RASTER_OP_READ;

    /**
     * Specifies that this RenderingAttributes object
     * allows writing its raster operation information.
     *
     * @since Java 3D 1.2
     */
    public static final int ALLOW_RASTER_OP_WRITE =
    CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_RASTER_OP_WRITE;

    /**
     * Specifies that this RenderingAttributes object allows reading
     * its stencil enable, stencil op, stencil function, and
     * stencil write mask information.
     *
     * @since Java 3D 1.4
     */
    public static final int ALLOW_STENCIL_ATTRIBUTES_READ =
    CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_STENCIL_ATTRIBUTES_READ;

    /**
     * Specifies that this RenderingAttributes object allows writing
     * its stencil enable, stencil op, stencil function, and
     * stencil write mask information.
     *
     * @since Java 3D 1.4
     */
    public static final int ALLOW_STENCIL_ATTRIBUTES_WRITE =
    CapabilityBits.RENDERING_ATTRIBUTES_ALLOW_STENCIL_ATTRIBUTES_WRITE;


    //
    // Enums for alpha test, depth test, and stencil test
    //

    /**
     * Specifies that pixels are always drawn irrespective of the
     * values being tested.
     * Can be used to specify the alpha test function, the depth test function,
     * or the stencil function.
     * This setting effectively disables alpha, depth, or stencil testing.
     *
     * @see #setAlphaTestFunction
     * @see #setDepthTestFunction
     * @see #setStencilFunction(int,int,int)
     */
    public static final int ALWAYS = 0;

    /**
     * Specifies that pixels are never drawn irrespective of the
     * values being tested.
     * Can be used to specify the alpha test function, the depth test function,
     * or the stencil function.
     *
     * @see #setAlphaTestFunction
     * @see #setDepthTestFunction
     * @see #setStencilFunction(int,int,int)
     */
    public static final int NEVER = 1;

    /**
     * Specifies that pixels are drawn if the two values being tested are equal.
     * Can be used to specify the alpha test function, the depth test function,
     * or the stencil function.
     *
     * @see #setAlphaTestFunction
     * @see #setDepthTestFunction
     * @see #setStencilFunction(int,int,int)
     */
    public static final int EQUAL = 2;

    /**
     * Specifies that pixels are drawn if the two values being tested are not equal.
     * Can be used to specify the alpha test function, the depth test function,
     * or the stencil function.
     *
     * @see #setAlphaTestFunction
     * @see #setDepthTestFunction
     * @see #setStencilFunction(int,int,int)
     */
    public static final int NOT_EQUAL = 3;

    /**
     * Specifies that pixels are drawn if the source/reference value is less 
     * than the destination/test value.
     * Can be used to specify the alpha test function, the depth test function,
     * or the stencil function.
     *
     * @see #setAlphaTestFunction
     * @see #setDepthTestFunction
     * @see #setStencilFunction(int,int,int)
     */
    public static final int LESS = 4;

    /**
     * Specifies that pixels are drawn if the source/reference value is less 
     * than or equal to the destination/test value.
     * Can be used to specify the alpha test function, the depth test function,
     * or the stencil function.
     *
     * @see #setAlphaTestFunction
     * @see #setDepthTestFunction
     * @see #setStencilFunction(int,int,int)
     */
    public static final int LESS_OR_EQUAL = 5;

    /**
     * Specifies that pixels are drawn if the source/reference value is greater 
     * than the destination/test value.
     * Can be used to specify the alpha test function, the depth test function,
     * or the stencil function.
     *
     * @see #setAlphaTestFunction
     * @see #setDepthTestFunction
     * @see #setStencilFunction(int,int,int)
     */
    public static final int GREATER = 6;

    /**
     * Specifies that pixels are drawn if the source/reference value is greater 
     * than or equal to the destination/test value.
     * Can be used to specify the alpha test function, the depth test function,
     * or the stencil function.
     *
     * @see #setAlphaTestFunction
     * @see #setDepthTestFunction
     * @see #setStencilFunction(int,int,int)
     */
    public static final int GREATER_OR_EQUAL = 7;


    //
    // Raster op enums
    //

    /**
     * Raster operation: <code>DST = 0</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.4
     */
    public static final int ROP_CLEAR = 0x0;

    /**
     * Raster operation: <code>DST = SRC & DST</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.4
     */
    public static final int ROP_AND = 0x1;

    /**
     * Raster operation: <code>DST = SRC & ~DST</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.4
     */
    public static final int ROP_AND_REVERSE = 0x2;

    /**
     * Raster operation: <code>DST = SRC</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.2
     */
    public static final int ROP_COPY = 0x3;

    /**
     * Raster operation: <code>DST = ~SRC & DST</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.4
     */
    public static final int ROP_AND_INVERTED = 0x4;
		
    /**
     * Raster operation: <code>DST = DST</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.4
     */
    public static final int ROP_NOOP = 0x5;

    /**
     * Raster operation: <code>DST = SRC ^ DST</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.2
     */
    public static final int ROP_XOR = 0x6;

    /**
     * Raster operation: <code>DST = DST | SRC</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.4
     */
    public static final int ROP_OR = 0x7;

    /**
     * Raster operation: <code>DST = ~( DST | SRC )</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.4
     */
    public static final int ROP_NOR = 0x8;

    /**
     * Raster operation: <code>DST = ~( DST ^ SRC )</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.4
     */
    public static final int ROP_EQUIV = 0x9;
		
    /**
     * Raster operation: <code>DST = ~DST</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.4
     */
    public static final int ROP_INVERT = 0xA;
		
    /**
     * Raster operation: <code>DST = src | ~DST</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.4
     */
    public static final int ROP_OR_REVERSE = 0xB;

    /**
     * Raster operation: <code>DST = ~SRC</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.4
     */
    public static final int ROP_COPY_INVERTED = 0xC;

    /**
     * Raster operation: <code>DST = ~SRC | DST</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.4
     */
    public static final int ROP_OR_INVERTED = 0xD;

    /**
     * Raster operation: <code>DST = ~(SRC & DST)</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.4
     */
    public static final int ROP_NAND = 0xE;

    /**
     * Raster operation: <code>DST = 1</code>.
     * @see #setRasterOp
     *
     * @since Java 3D 1.4
     */
    public static final int ROP_SET = 0xF;


    //
    // Stencil op enums
    //

    /**
     * Stencil operation: <code>DST = DST</code>
     * @see #setStencilOp(int,int,int)
     *
     * @since Java 3D 1.4
     */
    public static final int STENCIL_KEEP = 1;

    /**
     * Stencil operation: <code>DST = 0</code>
     * @see #setStencilOp(int,int,int)
     *
     * @since Java 3D 1.4
     */
    public static final int STENCIL_ZERO = 2;

    /**
     * Stencil operation: <code>DST = REF</code>
     * @see #setStencilOp(int,int,int)
     *
     * @since Java 3D 1.4
     */
    public static final int STENCIL_REPLACE = 3;

    /**
     * Stencil operation: <code>DST = DST + 1</code>
     * @see #setStencilOp(int,int,int)
     *
     * @since Java 3D 1.4
     */
    public static final int STENCIL_INCR = 4;

    /**
     * Stencil operation: <code>DST = DST - 1</code>
     * @see #setStencilOp(int,int,int)
     *
     * @since Java 3D 1.4
     */
    public static final int STENCIL_DECR = 5;

    /**
     * Stencil operation: <code>DST = ~DST</code>
     * @see #setStencilOp(int,int,int)
     *
     * @since Java 3D 1.4
     */
    public static final int STENCIL_INVERT = 6;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_ALPHA_TEST_FUNCTION_READ,
        ALLOW_ALPHA_TEST_VALUE_READ,
        ALLOW_DEPTH_ENABLE_READ,
        ALLOW_DEPTH_TEST_FUNCTION_READ,
        ALLOW_IGNORE_VERTEX_COLORS_READ,        
        ALLOW_RASTER_OP_READ,
        ALLOW_STENCIL_ATTRIBUTES_READ,
        ALLOW_VISIBLE_READ
    };

    /**
     * Constructs a RenderingAttributes object with default parameters.
     * The default values are as follows:
     * <ul>
     * depth buffer enable : true<br>
     * depth buffer write enable : true<br>
     * alpha test function : ALWAYS<br>
     * alpha test value : 0.0f<br>
     * visible : true<br>
     * ignore vertex colors : false<br>
     * raster operation enable : false<br>
     * raster operation : ROP_COPY<br>
     * depth test: LESS_OR_EQUAL<br>
     * stencil enable : false<br>
     * stencil write mask : ~0 (all ones)<br>
     * stencil op - failOp : STENCIL_KEEP<br>
     * stencil op - zFailOp : STENCIL_KEEP<br>
     * stencil op - zPassOp : STENCIL_KEEP<br>
     * stencil function : ALWAYS<br>
     * stencil reference value : 0<br>
     * stencil comparison mask : ~0 (all ones)
     * </ul>
     */
    public RenderingAttributes() {
	// Just use default attributes
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs a RenderingAttributes object with specified values.
     * @param depthBufferEnable a flag to turn depth buffer on/off
     * @param depthBufferWriteEnable a flag to to make depth buffer
     * read/write or read only
     * @param alphaTestValue the alpha test reference value
     * @param alphaTestFunction the function for comparing alpha values
     */
    public RenderingAttributes(boolean depthBufferEnable,
			       boolean depthBufferWriteEnable,
			       float alphaTestValue,
			       int alphaTestFunction){

	this(depthBufferEnable, depthBufferWriteEnable, alphaTestValue,
	     alphaTestFunction, true, false, false, ROP_COPY);
    }

    /**
     * Constructs a RenderingAttributes object with specified values
     * @param depthBufferEnable a flag to turn depth buffer on/off
     * @param depthBufferWriteEnable a flag to make depth buffer
     * read/write or read only
     * @param alphaTestValue the alpha test reference value
     * @param alphaTestFunction the function for comparing alpha values
     * @param visible a flag that specifies whether the object is visible
     * @param ignoreVertexColors a flag to enable or disable
     * the ignoring of per-vertex colors
     * @param rasterOpEnable a flag that specifies whether logical
     * raster operations are enabled for this RenderingAttributes object.
     * This disables all alpha blending operations.
     * @param rasterOp the logical raster operation, one of:
     * ROP_CLEAR, ROP_AND, ROP_AND_REVERSE, ROP_COPY, ROP_AND_INVERTED,
     * ROP_NOOP, ROP_XOR, ROP_OR, ROP_NOR, ROP_EQUIV, ROP_INVERT,
     * ROP_OR_REVERSE, ROP_COPY_INVERTED, ROP_OR_INVERTED, ROP_NAND or ROP_SET
     *
     * @since Java 3D 1.2
     */
    public RenderingAttributes(boolean depthBufferEnable,
			       boolean depthBufferWriteEnable,
			       float alphaTestValue,
			       int alphaTestFunction,
			       boolean visible,
			       boolean ignoreVertexColors,
			       boolean rasterOpEnable,
			       int rasterOp) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
	
	((RenderingAttributesRetained)this.retained).initDepthBufferEnable(depthBufferEnable);
	((RenderingAttributesRetained)this.retained).initDepthBufferWriteEnable(depthBufferWriteEnable);
	((RenderingAttributesRetained)this.retained).initAlphaTestValue(alphaTestValue);
	((RenderingAttributesRetained)this.retained).initAlphaTestFunction(alphaTestFunction);
	((RenderingAttributesRetained)this.retained).initVisible(visible);

	
	((RenderingAttributesRetained)this.retained).initIgnoreVertexColors(ignoreVertexColors);
	((RenderingAttributesRetained)this.retained).initRasterOpEnable(rasterOpEnable);
	((RenderingAttributesRetained)this.retained).initRasterOp(rasterOp);
    }

    /**
     * Enables or disables depth buffer mode for this RenderingAttributes
     * component object.
     *
     * @param state true or false to enable or disable depth buffer mode
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @see GraphicsConfigTemplate3D#setDepthSize
     */
    public void setDepthBufferEnable(boolean state){
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_DEPTH_ENABLE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes0"));

	if (isLive())
	    ((RenderingAttributesRetained)this.retained).setDepthBufferEnable(state);
	else
	    ((RenderingAttributesRetained)this.retained).initDepthBufferEnable(state);

    }

    /**
     * Retrieves the state of zBuffer Enable flag
     * @return true if depth buffer mode is enabled, false
     * if depth buffer mode is disabled
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public boolean getDepthBufferEnable(){
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_DEPTH_ENABLE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes1"));

	return ((RenderingAttributesRetained)this.retained).getDepthBufferEnable();
    }

    /**
     * Enables or disables writing the depth buffer for this object.
     * During the transparent rendering pass,
     * this attribute can be overridden by
     * the depthBufferFreezeTransparent attribute in the View object.
     * @param state true or false to enable or disable depth buffer Write mode
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @see View#setDepthBufferFreezeTransparent
     */
    public void setDepthBufferWriteEnable(boolean state) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_DEPTH_ENABLE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes2"));
	if (isLive())
	    ((RenderingAttributesRetained)this.retained).setDepthBufferWriteEnable(state);
	else
	    ((RenderingAttributesRetained)this.retained).initDepthBufferWriteEnable(state);
    }

    /**
     * Retrieves the state of Depth Buffer Write Enable flag.
     * @return true if depth buffer is writable, false
     * if depth buffer is read-only
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public boolean getDepthBufferWriteEnable(){
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_DEPTH_ENABLE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes3"));

	return ((RenderingAttributesRetained)this.retained).getDepthBufferWriteEnable();
    }

    /**
     * Set alpha test value used by alpha test function.  This value is
     * compared to the alpha value of each rendered pixel.
     * @param value the alpha test value
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setAlphaTestValue(float value){
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_ALPHA_TEST_VALUE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes4"));
	if (isLive())
	    ((RenderingAttributesRetained)this.retained).setAlphaTestValue(value);
	else
	    ((RenderingAttributesRetained)this.retained).initAlphaTestValue(value);

    }

    /**
     * Retrieves the alpha test value.
     * @return the alpha test value.
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public float getAlphaTestValue(){
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_ALPHA_TEST_VALUE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes5"));

	return ((RenderingAttributesRetained)this.retained).getAlphaTestValue();
    }

    /**
     * Set alpha test function. This function is used to compare
     * each incoming (source) per-pixel alpha value with the alpha test value.
     * If the test passes, the pixel is written otherwise the pixel is not
     * written.
     * @param function the new alpha test function.  One of
     * ALWAYS, NEVER, EQUAL, NOT_EQUAL, LESS, LESS_OR_EQUAL, GREATER,
     * GREATER_OR_EQUAL
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setAlphaTestFunction(int function){
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_ALPHA_TEST_FUNCTION_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes6"));

	if (isLive())
	    ((RenderingAttributesRetained)this.retained).setAlphaTestFunction(function);
	else
	    ((RenderingAttributesRetained)this.retained).initAlphaTestFunction(function);

    }

    /**
     * Retrieves current alpha test function.
     * @return the current alpha test function
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public int getAlphaTestFunction(){
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_ALPHA_TEST_FUNCTION_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes7"));

	return ((RenderingAttributesRetained)this.retained).getAlphaTestFunction();
    }

    /**
     * Sets the visibility flag for this RenderingAttributes
     * component object.  Invisible objects are not rendered (subject to
     * the visibility policy for the current view), but they can be picked
     * or collided with. The default value is true.
     * @param visible true or false to enable or disable visibility
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @see View#setVisibilityPolicy
     *
     * @since Java 3D 1.2
     */
    public void setVisible(boolean visible) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_VISIBLE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes8"));
	
	if (isLive())
	    ((RenderingAttributesRetained)this.retained).setVisible(visible);
	else
	    ((RenderingAttributesRetained)this.retained).initVisible(visible);
    }

    /**
     * Retrieves the visibility flag for this RenderingAttributes object.
     * @return true if the object is visible; false
     * if the object is invisible.
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public boolean getVisible() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_VISIBLE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes9"));
	
	return ((RenderingAttributesRetained)this.retained).getVisible();
    }

    /**
     * Sets a flag that indicates whether vertex colors are ignored
     * for this RenderingAttributes object.  If
     * <code>ignoreVertexColors</code> is false, per-vertex
     * colors are used, when present in the associated Geometry
     * objects, taking precedence over the ColoringAttributes color
     * and the specified Material color(s).  If <code>ignoreVertexColors</code>
     * is true, per-vertex colors are ignored.  In this case, if
     * lighting is enabled, the Material diffuse color will be
     * used as the object color.  If lighting is disabled, the
     * ColoringAttributes color will be used.  The default value is false.
     *
     * @param ignoreVertexColors true or false to enable or disable
     * the ignoring of per-vertex colors
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @see ColoringAttributes
     * @see Material
     *
     * @since Java 3D 1.2
     */
    public void setIgnoreVertexColors(boolean ignoreVertexColors) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_IGNORE_VERTEX_COLORS_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes12"));


	if (isLive())
	    ((RenderingAttributesRetained)this.retained).setIgnoreVertexColors(ignoreVertexColors);
	else
	    ((RenderingAttributesRetained)this.retained).initIgnoreVertexColors(ignoreVertexColors);
    }

    /**
     * Retrieves the ignoreVertexColors flag for this
     * RenderingAttributes object.
     * @return true if per-vertex colors are ignored; false
     * if per-vertex colors are used.
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public boolean getIgnoreVertexColors() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_IGNORE_VERTEX_COLORS_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes13"));


	return ((RenderingAttributesRetained)this.retained).getIgnoreVertexColors();
    }

    /**
     * Sets the rasterOp enable flag for this RenderingAttributes
     * component object.  When set to true, this enables logical
     * raster operations as specified by the setRasterOp method.
     * Enabling raster operations effectively disables alpha blending,
     * which is used for transparency and antialiasing.  Raster
     * operations, especially XOR mode, are primarily useful when
     * rendering to the front buffer in immediate mode.  Most
     * applications will not wish to enable this mode.
     *
     * @param rasterOpEnable true or false to enable or disable
     * raster operations
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @see #setRasterOp
     *
     * @since Java 3D 1.2
     */
    public void setRasterOpEnable(boolean rasterOpEnable) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_RASTER_OP_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes10"));

	if (isLive())
	    ((RenderingAttributesRetained)this.retained).setRasterOpEnable(rasterOpEnable);
	else
	    ((RenderingAttributesRetained)this.retained).initRasterOpEnable(rasterOpEnable);
    }

    /**
     * Retrieves the rasterOp enable flag for this RenderingAttributes
     * object.
     * @return true if raster operations are enabled; false
     * if raster operations are disabled.
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public boolean getRasterOpEnable() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_RASTER_OP_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes11"));


	return ((RenderingAttributesRetained)this.retained).getRasterOpEnable();
    }

    /**
     * Sets the raster operation function for this RenderingAttributes
     * component object.
     *
     * @param rasterOp the logical raster operation, one of:
     * ROP_CLEAR, ROP_AND, ROP_AND_REVERSE, ROP_COPY, ROP_AND_INVERTED,
     * ROP_NOOP, ROP_XOR, ROP_OR, ROP_NOR, ROP_EQUIV, ROP_INVERT,
     * ROP_OR_REVERSE, ROP_COPY_INVERTED, ROP_OR_INVERTED, ROP_NAND or ROP_SET.
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public void setRasterOp(int rasterOp) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_RASTER_OP_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes10"));

	if (isLive())
	    ((RenderingAttributesRetained)this.retained).setRasterOp(rasterOp);
	else
	    ((RenderingAttributesRetained)this.retained).initRasterOp(rasterOp);
    }

    /**
     * Retrieves the current raster operation for this RenderingAttributes
     * object.
     * @return one of:
     * ROP_CLEAR, ROP_AND, ROP_AND_REVERSE, ROP_COPY, ROP_AND_INVERTED,
     * ROP_NOOP, ROP_XOR, ROP_OR, ROP_NOR, ROP_EQUIV, ROP_INVERT,
     * ROP_OR_REVERSE, ROP_COPY_INVERTED, ROP_OR_INVERTED, ROP_NAND or ROP_SET
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public int getRasterOp() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_RASTER_OP_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes11"));

	return ((RenderingAttributesRetained)this.retained).getRasterOp();
    }

    /**
     * Creates a retained mode RenderingAttributesRetained object that this
     * RenderingAttributes component object will point to.
     */
    void createRetained() {
	this.retained = new RenderingAttributesRetained();
	this.retained.setSource(this);
    }


    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)  
     */
    public NodeComponent cloneNodeComponent() {
        RenderingAttributes ra = new RenderingAttributes();
        ra.duplicateNodeComponent(this);
        return ra;
    }


    /**
     * Copies all node information from <code>originalNodeComponent</code> into
     * the current node.  This method is called from the
     * <code>duplicateNode</code> method. This routine does
     * the actual duplication of all "local data" (any data defined in
     * this object). 
     *
     * @param originalNodeComponent the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    void duplicateAttributes(NodeComponent originalNodeComponent, 
			     boolean forceDuplicate) { 

	super.duplicateAttributes(originalNodeComponent, forceDuplicate);

	RenderingAttributesRetained attr = 
	    (RenderingAttributesRetained) originalNodeComponent.retained;
	RenderingAttributesRetained rt =
	    (RenderingAttributesRetained) retained;

	rt.initDepthBufferEnable(attr.getDepthBufferEnable());
	rt.initDepthBufferWriteEnable(attr.getDepthBufferWriteEnable());
	rt.initDepthTestFunction(attr.getDepthTestFunction());
	rt.initAlphaTestValue(attr.getAlphaTestValue());
	rt.initAlphaTestFunction(attr.getAlphaTestFunction());
	rt.initVisible(attr.getVisible());
	rt.initIgnoreVertexColors(attr.getIgnoreVertexColors());
	rt.initRasterOpEnable(attr.getRasterOpEnable());
	rt.initRasterOp(attr.getRasterOp());
	rt.initStencilEnable(attr.getStencilEnable());
	int[] ops = new int[3];
	attr.getStencilOp(ops);
	rt.initStencilOp(ops[0], ops[1], ops[2]);
	attr.getStencilFunction(ops);
	rt.initStencilFunction(ops[0], ops[1], ops[2]);
	rt.initStencilWriteMask(attr.getStencilWriteMask());

    }

    /**
     * Set depth test function.  This function is used to compare each
     * incoming (source) per-pixel depth test value with the stored per-pixel
     * depth value in the frame buffer.  If the test
     * passes, the pixel is written, otherwise the pixel is not
     * written.
     * @param function the new depth test function.  One of
     * ALWAYS, NEVER, EQUAL, NOT_EQUAL, LESS, LESS_OR_EQUAL, GREATER,
     * or GREATER_OR_EQUAL.
     * The default value is LESS_OR_EQUAL.
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.4
     */
    public void setDepthTestFunction(int function){
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_DEPTH_TEST_FUNCTION_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes14"));

       if (isLive())
	    ((RenderingAttributesRetained)this.retained).setDepthTestFunction(function);
	else
	    ((RenderingAttributesRetained)this.retained).initDepthTestFunction(function);
    }

    /**
     * Retrieves current depth test function.
     * @return the current depth test function
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.4
     */
    public int getDepthTestFunction(){
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_DEPTH_TEST_FUNCTION_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes15"));

	return ((RenderingAttributesRetained)this.retained).getDepthTestFunction();
    }

    /**
     * Enables or disables the stencil buffer for this RenderingAttributes
     * component object. If the stencil buffer is disabled, the
     * stencil operation and function are ignored.  If a scene graph
     * is rendered on a Canvas3D that does not have a stencil buffer,
     * the stencil buffer will be implicitly disabled for that canvas.
     *
     * @param state true or false to enable or disable stencil buffer
     * operations.
     * If this is set to false, the stencilOp and stencilFunction parameters
     * are not used.
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @see GraphicsConfigTemplate3D#setStencilSize
     *
     * @since Java 3D 1.4
     */
    public void setStencilEnable(boolean state) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_STENCIL_ATTRIBUTES_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes16"));
            }
        }

	if (isLive())
	    ((RenderingAttributesRetained)this.retained).setStencilEnable(state);
	else
	    ((RenderingAttributesRetained)this.retained).initStencilEnable(state);

    }

    /**
     * Retrieves the stencil buffer enable flag for this RenderingAttributes
     * object.
     *
     * @return true if stencil buffer operations are enabled; false
     * if stencil buffer operations are disabled.
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.4
     */
    public boolean getStencilEnable() {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_STENCIL_ATTRIBUTES_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes17"));
            }
        }

	return ((RenderingAttributesRetained)this.retained).getStencilEnable();
    }

    /**
     * Sets the stencil operations for this RenderingAttributes object to the
     * specified parameters.
     *
     * @param failOp operation performed when the stencil test fails, one of:
     * STENCIL_KEEP, STENCIL_ZERO, STENCIL_REPLACE, STENCIL_INCR, STENCIL_DECR,
     * or STENCIL_INVERT.
     *
     * @param zFailOp operation performed when the stencil test passes and the
     * depth test fails, one of:
     * STENCIL_KEEP, STENCIL_ZERO, STENCIL_REPLACE, STENCIL_INCR, STENCIL_DECR,
     * or STENCIL_INVERT.
     *
     * @param zPassOp operation performed when both the stencil test and the
     * depth test pass, one of:
     * STENCIL_KEEP, STENCIL_ZERO, STENCIL_REPLACE, STENCIL_INCR, STENCIL_DECR,
     * or STENCIL_INVERT.
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.4
     */
    public void setStencilOp(int failOp, int zFailOp, int zPassOp) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_STENCIL_ATTRIBUTES_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes16"));
            }
        }

	if (isLive())
	    ((RenderingAttributesRetained)this.retained).setStencilOp(failOp, 
								      zFailOp, 
								      zPassOp);
	else
	    ((RenderingAttributesRetained)this.retained).initStencilOp(failOp, 
								       zFailOp, 
								       zPassOp);

    }

    /**
     * Sets the stencil operations for this RenderingAttributes object to the
     * specified parameters.
     *
     * @param stencilOps an array of three integers that specifies the new
     * set of stencil operations. Element 0 of the array specifies the
     * <code>failOp</code> parameter, element 1 specifies the
     * <code>zFailOp</code> parameter, and element 2 specifies the
     * <code>zPassOp</code> parameter.
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @see #setStencilOp(int,int,int)
     *
     * @since Java 3D 1.4
     */
    public void setStencilOp(int[] stencilOps) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_STENCIL_ATTRIBUTES_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes16"));
            }
        }

	if (isLive())
	    ((RenderingAttributesRetained)this.retained).setStencilOp(stencilOps[0], 
								      stencilOps[1],
								      stencilOps[2]);
	else
	    ((RenderingAttributesRetained)this.retained).initStencilOp(stencilOps[0], 
								       stencilOps[1],
								       stencilOps[2]);
    }

    /**
     * Retrieves the current set of stencil operations, and copies them
     * into the specified array. The caller must ensure that this array
     * has been allocated with enough space to hold the results.
     *
     * @param stencilOps array that will receive the current set of
     * three stencil operations. The <code>failOp</code> parameter is copied
     * into element 0 of the array, the <code>zFailOp</code> parameter is copied
     * into element 1, and the <code>zPassOp</code> parameter is copied
     * into element 2.
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.4
     */
    public void getStencilOp(int[] stencilOps) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_STENCIL_ATTRIBUTES_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes17"));
            }
        }
	
	((RenderingAttributesRetained)this.retained).getStencilOp(stencilOps);
    }

    /**
     * Sets the stencil function, reference value, and comparison mask
     * for this RenderingAttributes object to the specified parameters.
     *
     * @param function the stencil test function, used to compare the
     * stencil reference value with the stored per-pixel
     * stencil value in the frame buffer.  If the test
     * passes, the pixel is written, otherwise the pixel is not
     * written. The stencil function is one of:
     * ALWAYS, NEVER, EQUAL, NOT_EQUAL, LESS, LESS_OR_EQUAL, GREATER,
     * or GREATER_OR_EQUAL.
     *
     * @param refValue the stencil reference value that is tested against
     * the stored per-pixel stencil value
     *
     * @param compareMask a mask that limits which bits are compared; it is
     * bitwise-ANDed with both the stencil reference value and the stored
     * per-pixel stencil value before doing the comparison.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.4
     */
    public void setStencilFunction(int function, int refValue, int compareMask) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_STENCIL_ATTRIBUTES_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes16"));
            }
        }
	if (isLive())
	    ((RenderingAttributesRetained)this.retained).setStencilFunction(function, 
									    refValue,
									    compareMask);
	else
	    ((RenderingAttributesRetained)this.retained).initStencilFunction(function, 
									     refValue,
									     compareMask);
    }

    /**
     * Sets the stencil function, reference value, and comparison mask
     * for this RenderingAttributes object to the specified parameters.
     *
     * @param params an array of three integers that specifies the new
     * stencil function, reference value, and comparison mask.
     * Element 0 of the array specifies the
     * stencil function, element 1 specifies the
     * reference value, and element 2 specifies the
     * comparison mask.
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @see #setStencilFunction(int,int,int)
     *
     * @since Java 3D 1.4
     */
    public void setStencilFunction(int[] params) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_STENCIL_ATTRIBUTES_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes16"));
            }
        }

	if (isLive())
	    ((RenderingAttributesRetained)this.retained).setStencilFunction(params[0], 
									    params[1],
									    params[2]);
	else
	    ((RenderingAttributesRetained)this.retained).initStencilFunction(params[0], 
									     params[1],
									     params[2]);

    }

    /**
     * Retrieves the stencil function, reference value, and comparison mask,
     * and copies them into the specified array. The caller must ensure
     * that this array has been allocated with enough space to hold the results.
     *
     * @param params array that will receive the current stencil function,
     * reference value, and comparison mask. The stencil function is copied
     * into element 0 of the array, the reference value is copied
     * into element 1, and the comparison mask is copied
     * into element 2.
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.4
     */
    public void getStencilFunction(int[] params) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_STENCIL_ATTRIBUTES_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes17"));
            }
        }

	((RenderingAttributesRetained)this.retained).getStencilFunction(params);
    }

    /**
     * Sets the stencil write mask for this RenderingAttributes
     * object. This mask controls which bits of the
     * stencil buffer are written.
     * The default value is <code>~0</code> (all ones).
     *
     * @param mask the new stencil write mask.
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.4
     */
    public void setStencilWriteMask(int mask) {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_STENCIL_ATTRIBUTES_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes16"));
            }
        }

	if (isLive())
	    ((RenderingAttributesRetained)this.retained).setStencilWriteMask(mask);
	else
	    ((RenderingAttributesRetained)this.retained).initStencilWriteMask(mask);
    }

    /**
     * Retrieves the current stencil write mask for this RenderingAttributes
     * object.
     *
     * @return the stencil write mask.
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.4
     */
    public int getStencilWriteMask() {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_STENCIL_ATTRIBUTES_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("RenderingAttributes17"));
            }
        }

	return ((RenderingAttributesRetained)this.retained).getStencilWriteMask();
    }

}
