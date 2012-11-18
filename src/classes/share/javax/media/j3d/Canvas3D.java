/*
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.awt.AWTEvent;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.vecmath.Color3f;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector4d;


/**
 * The Canvas3D class provides a drawing canvas for 3D rendering.  It
 * is used either for on-screen rendering or off-screen rendering.
 * Canvas3D is an extension of the AWT Canvas class that users may
 * further subclass to implement additional functionality.
 * <p>
 * The Canvas3D object extends the Canvas object to include
 * 3D-related information such as the size of the canvas in pixels,
 * the Canvas3D's location, also in pixels, within a Screen3D object,
 * and whether or not the canvas has stereo enabled.
 * <p>
 * Because all Canvas3D objects contain a
 * reference to a Screen3D object and because Screen3D objects define
 * the size of a pixel in physical units, Java 3D can convert a Canvas3D
 * size in pixels to a physical world size in meters. It can also
 * determine the Canvas3D's position and orientation in the
 * physical world.
 * <p>
 * <b>On-screen Rendering vs. Off-screen Rendering</b>
 * <p>
 * The Canvas3D class is used either for on-screen rendering or
 * off-screen rendering.
 * On-screen Canvas3Ds are added to AWT or Swing Container objects
 * like any other canvas.  Java 3D automatically and continuously
 * renders to all on-screen canvases that are attached to an active
 * View object.  On-screen Canvas3Ds can be either single or double
 * buffered and they can be either stereo or monoscopic.
 * <p>
 * Off-screen Canvas3Ds must not be added to any Container.  Java 3D
 * renders to off-screen canvases in response to the
 * <code>renderOffScreenBuffer</code> method.  Off-screen Canvas3Ds
 * are single buffered.  However, on many systems, the actual
 * rendering is done to an off-screen hardware buffer or to a 3D
 * library-specific buffer and only copied to the off-screen buffer of
 * the Canvas when the rendering is complete, at "buffer swap" time.
 * Off-screen Canvas3Ds are monoscopic.
 * <p>
 * The setOffScreenBuffer method sets the off-screen buffer for this
 * Canvas3D. The specified image is written into by the Java 3D renderer.
 * The size of the specified ImageComponent determines the size, in
 * pixels, of this Canvas3D - the size inherited from Component is
 * ignored. Note that the size, physical width, and physical height of the
 * associated Screen3D must be set
 * explicitly prior to rendering. Failure to do so will result in an
 * exception.
 * <p>
 * The getOffScreenBuffer method retrieves the off-screen
 * buffer for this Canvas3D.
 * <p>
 * The renderOffScreenBuffer method schedules the rendering of a frame
 * into this Canvas3D's off-screen buffer. The rendering is done from
 * the point of view of the View object to which this Canvas3D has been
 * added. No rendering is performed if this Canvas3D object has not been
 * added to an active View. This method does not wait for the rendering
 * to actually happen. An application that wishes to know when the
 * rendering is complete must either subclass Canvas3D and
 * override the postSwap method, or call waitForOffScreenRendering.
 * <p>
 * The setOfScreenLocation methods set the location of this off-screen
 * Canvas3D.  The location is the upper-left corner of the Canvas3D
 * relative to the upper-left corner of the corresponding off-screen
 * Screen3D. The function of these methods is similar to that of
 * Component.setLocation for on-screen Canvas3D objects. The default
 * location is (0,0).
 * <p>
 * <b>Accessing and Modifying an Eye's Image Plate Position</b>
 * <p>
 * A Canvas3D object provides sophisticated applications with access
 * to the eye's position information in head-tracked, room-mounted
 * runtime environments. It also allows applications to manipulate
 * the position of an eye relative to an image plate in non-head-tracked
 * runtime environments.
 * <p>
 * The setLeftManualEyeInImagePlate and setRightManualEyeInImagePlate
 * methods set the position of the manual left and right eyes in image
 * plate coordinates. These values determine eye placement when a head
 * tracker is not in use and the application is directly controlling the
 * eye position in image plate coordinates. In head-tracked mode or
 * when the windowEyepointPolicy is RELATIVE_TO_FIELD_OF_VIEW or
 * RELATIVE_TO_COEXISTENCE, this
 * value is ignored. When the windowEyepointPolicy is RELATIVE_TO_WINDOW,
 * only the Z value is used.
 * <p>
 * The getLeftEyeInImagePlate, getRightEyeInImagePlate, and
 * getCenterEyeInImagePlate methods retrieve the actual position of the
 * left eye, right eye, and center eye in image plate coordinates and
 * copy that value into the object provided. The center eye is the
 * fictional eye half-way between the left and right eye. These three
 * values are a function of the windowEyepointPolicy, the tracking
 * enable flag, and the manual left, right, and center eye positions.
 * <p>
 * <b>Monoscopic View Policy</b>
 * <p>
 * The setMonoscopicViewPolicy and getMonoscopicViewPolicy methods
 * set and retrieve the policy regarding how Java 3D generates monoscopic
 * view. If the policy is set to View.LEFT_EYE_VIEW, the view generated
 * corresponds to the view as seen from the left eye. If set to
 * View.RIGHT_EYE_VIEW, the view generated corresponds to the view as
 * seen from the right eye. If set to View.CYCLOPEAN_EYE_VIEW, the view
 * generated corresponds to the view as seen from the "center eye," the
 * fictional eye half-way between the left and right eye. The default
 * monoscopic view policy is View.CYCLOPEAN_EYE_VIEW.
 * <p>
 * <b>Immediate Mode Rendering</b>
 * <p>
 * Pure immediate-mode rendering provides for those applications and
 * applets that do not want Java 3D to do any automatic rendering of
 * the scene graph. Such applications may not even wish to build a
 * scene graph to represent their graphical data. However, they use
 * Java 3D's attribute objects to set graphics state and Java 3D's
 * geometric objects to render geometry.
 * <p>
 * A pure immediate mode application must create a minimal set of
 * Java 3D objects before rendering. In addition to a Canvas3D object,
 * the application must create a View object, with its associated
 * PhysicalBody and PhysicalEnvironment objects, and the following
 * scene graph elements: a VirtualUniverse object, a high-resolution
 * Locale object, a BranchGroup node object, a TransformGroup node
 * object with associated transform, and a ViewPlatform
 * leaf node object that defines the position and orientation within
 * the virtual universe that generates the view.
 * <p>
 * In immediate mode, all rendering is done completely under user
 * control. It is necessary for the user to clear the 3D canvas,
 * render all geometry, and swap the buffers.  Additionally,
 * rendering the right and left eye for stereo viewing becomes the
 * sole responsibility of the application.  In pure immediate mode,
 * the user must stop the Java 3D renderer, via the
 * Canvas3D object <code>stopRenderer</code> method, prior to adding the
 * Canvas3D object to an active View object (that is, one that is
 * attached to a live ViewPlatform object).
 * <p>
 * Other Canvas3D methods related to immediate mode rendering are:
 * <p>
 * <ul>
 * <code>getGraphicsContext3D</code> retrieves the immediate-mode
 * 3D graphics context associated with this Canvas3D. It creates a
 * new graphics context if one does not already exist.
 * <p>
 * <code>getGraphics2D</code> retrieves the
 * 2D graphics object associated with this Canvas3D. It creates a
 * new 2D graphics object if one does not already exist.
 * <p>
 * <code>swap</code> synchronizes and swaps buffers on a
 * double-buffered canvas for this Canvas3D object. This method
 * should only be called if the Java 3D renderer has been stopped.
 * In the normal case, the renderer automatically swaps
 * the buffer.
 * </ul>
 *
 * <p>
 * <b>Mixed Mode Rendering</b>
 * <p>
 * Mixing immediate mode and retained or compiled-retained mode
 * requires more structure than pure immediate mode. In mixed mode,
 * the Java 3D renderer is running continuously, rendering the scene
 * graph into the canvas.
 *
 * <p>
 * Canvas3D methods related to mixed mode rendering are:
 *
 * <p>
 * <ul>
 * <code>preRender</code> called by the Java 3D rendering loop after
 * clearing the canvas and before any rendering has been done for
 * this frame.
 * <p>
 * <code>postRender</code> called by the Java 3D rendering loop after
 * completing all rendering to the canvas for this frame and before
 * the buffer swap.
 * <p>
 * <code>postSwap</code> called by the Java 3D rendering loop after
 * completing all rendering to the canvas, and all other canvases
 * associated with this view, for this frame following the
 * buffer swap.
 * <p>
 * <code>renderField</code> called by the Java 3D rendering loop
 * during the execution of the rendering loop. It is called once
 * for each field (i.e., once per frame on a mono system or once
 * each for the right eye and left eye on a two-pass stereo system.
 * </ul>
 * <p>
 * The above callback methods are called by the Java 3D rendering system
 * and should <i>not</i> be called by an application directly.
 *
 * <p>
 * The basic Java 3D <i>stereo</i> rendering loop,
 * executed for each Canvas3D, is as follows:
 * <ul><pre>
 * clear canvas (both eyes)
 * call preRender()                           // user-supplied method
 * set left eye view
 * render opaque scene graph objects
 * call renderField(FIELD_LEFT)               // user-supplied method
 * render transparent scene graph objects
 * set right eye view
 * render opaque scene graph objects again
 * call renderField(FIELD_RIGHT)              // user-supplied method
 * render transparent scene graph objects again
 * call postRender()                          // user-supplied method
 * synchronize and swap buffers
 * call postSwap()                            // user-supplied method
 * </pre></ul>
 * <p>
 * The basic Java 3D <i>monoscopic</i> rendering loop is as follows:
 * <ul><pre>
 * clear canvas
 * call preRender()                            // user-supplied method
 * set view
 * render opaque scene graph objects
 * call renderField(FIELD_ALL)                 // user-supplied method
 * render transparent scene graph objects
 * call postRender()                           // user-supplied method
 * synchronize and swap buffers
 * call postSwap()                             // user-supplied method
 * </pre></ul>
 * <p>
 * In both cases, the entire loop, beginning with clearing the canvas
 * and ending with swapping the buffers, defines a frame. The application
 * is given the opportunity to render immediate-mode geometry at any of
 * the clearly identified spots in the rendering loop. A user specifies
 * his or her own rendering methods by extending the Canvas3D class and
 * overriding the preRender, postRender, postSwap, and/or renderField
 * methods.
 * Updates to live Geometry, Texture, and ImageComponent objects
 * in the scene graph are not allowed from any of these callback
 * methods.
 *
 * <p>
 * <b>Serialization</b>
 * <p>
 * Canvas3D does <i>not</i> support serialization.  An attempt to
 * serialize a Canvas3D object will result in an
 * UnsupportedOperationException being thrown.
 *
 * <p>
 * <b>Additional Information</b>
 * <p>
 * For more information, see the
 * <a href="doc-files/intro.html">Introduction to the Java 3D API</a> and
 * <a href="doc-files/ViewModel.html">View Model</a>
 * documents.
 *
 * @see Screen3D
 * @see View
 * @see GraphicsContext3D
 */
public class Canvas3D extends Canvas {
    /**
     * Specifies the left field of a field-sequential stereo rendering loop.
     * A left field always precedes a right field.
     */
    public static final int FIELD_LEFT = 0;

    /**
     * Specifies the right field of a field-sequential stereo rendering loop.
     * A right field always follows a left field.
     */
    public static final int FIELD_RIGHT = 1;

    /**
     * Specifies a single-field rendering loop.
     */
    public static final int FIELD_ALL = 2;

    //
    // The following constants are bit masks to specify which of the node
    // components are dirty and need updates.
    //
    // Values for the geometryType field.
    static final int POLYGONATTRS_DIRTY      = 0x01;
    static final int LINEATTRS_DIRTY         = 0x02;
    static final int POINTATTRS_DIRTY        = 0x04;
    static final int MATERIAL_DIRTY          = 0x08;
    static final int TRANSPARENCYATTRS_DIRTY = 0x10;
    static final int COLORINGATTRS_DIRTY     = 0x20;

    // Values for lightbin, env set, texture, texture setting etc.
    static final int LIGHTBIN_DIRTY            = 0x40;
    static final int LIGHTENABLES_DIRTY        = 0x80;
    static final int AMBIENTLIGHT_DIRTY        = 0x100;
    static final int ATTRIBUTEBIN_DIRTY        = 0x200;
    static final int TEXTUREBIN_DIRTY          = 0x400;
    static final int TEXTUREATTRIBUTES_DIRTY   = 0x800;
    static final int RENDERMOLECULE_DIRTY      = 0x1000;
    static final int FOG_DIRTY                 = 0x2000;
    static final int MODELCLIP_DIRTY           = 0x4000;
    static final int VIEW_MATRIX_DIRTY         = 0x8000;
    // static final int SHADER_DIRTY              = 0x10000; Not ready for this yet -- JADA

    //
    // Flag that indicates whether this Canvas3D is an off-screen Canvas3D
    //
    boolean offScreen = false;

    //
    // Issue 131: Flag that indicates whether this Canvas3D is a manually
    // rendered Canvas3D (versus an automatically rendered Canvas3D).
    //
    // NOTE: manualRendering only applies to off-screen Canvas3Ds at this time.
    // We have no plans to ever change this, but if we do, it might be necessary
    // to determine which, if any, of the uses of "manualRendering" should be
    // changed to "manualRendering&&offScreen"
    //
    boolean manualRendering = false;

    // user specified offScreen Canvas location
    Point offScreenCanvasLoc;

    // user specified offScreen Canvas dimension
    Dimension offScreenCanvasSize;

    //
    // Flag that indicates whether off-screen rendering is in progress or not
    //
    volatile boolean offScreenRendering = false;

    //
    // Flag that indicates we are waiting for an off-screen buffer to be
    // created or destroyed by the Renderer.
    //
    volatile boolean offScreenBufferPending = false;

    //
    // ImageComponent used for off-screen rendering
    //
    ImageComponent2D offScreenBuffer = null;

    // flag that indicates whether this canvas will use shared context
    boolean useSharedCtx = true;

    //
    // Read-only flag that indicates whether stereo is supported for this
    // canvas.  This is always false for off-screen canvases.
    //
    boolean stereoAvailable;

    //
    // Flag to enable stereo rendering, if allowed by the
    // stereoAvailable flag.
    //
    boolean stereoEnable = true;

    //
    // This flag is set when stereo mode is both enabled and
    // available.  Code that looks at stereo mode should use this
    // flag.
    //
    boolean useStereo;

    // Indicate whether it is left or right stereo pass currently
    boolean rightStereoPass = false;

    //
    // Specifies how Java 3D generates monoscopic view
    // (LEFT_EYE_VIEW, RIGHT_EYE_VIEW, or CYCLOPEAN_EYE_VIEW).
    //
    int monoscopicViewPolicy = View.CYCLOPEAN_EYE_VIEW;

    // User requested stencil size
    int requestedStencilSize;

    // Actual stencil size return for this canvas
    int actualStencilSize;

    // True if stencil buffer is available for user
    boolean userStencilAvailable;

    // True if stencil buffer is available for system ( decal )
    boolean systemStencilAvailable;

    //
    // Read-only flag that indicates whether double buffering is supported
    // for this canvas.  This is always false for off-screen canvases.
    //
    boolean doubleBufferAvailable;

    //
    // Flag to enable double buffered rendering, if allowed by the
    // doubleBufferAvailable flag.
    //
    boolean doubleBufferEnable = true;

    //
    // This flag is set when doubleBuffering is both enabled and
    // available Code that enables or disables double buffering should
    // use this flag.
    //
    boolean useDoubleBuffer;

    //
    // Read-only flag that indicates whether scene antialiasing
    // is supported for this canvas.
    //
    boolean sceneAntialiasingAvailable;
    boolean sceneAntialiasingMultiSamplesAvailable;

    // Use to see whether antialiasing is already set
    boolean antialiasingSet = false;

    //
    // Read-only flag that indicates the size of the texture color
    // table for this canvas.  A value of 0 indicates that the texture
    // color table is not supported.
    //
    int textureColorTableSize;

    // number of active/enabled texture unit
    int numActiveTexUnit = 0;

    // index iof last enabled texture unit
    int lastActiveTexUnit = -1;

    // True if shadingLanguage is supported, otherwise false.
    boolean shadingLanguageGLSL = false;

    // Query properties
    J3dQueryProps queryProps;

    // Flag indicating a fatal rendering error of some sort
    private boolean fatalError = false;

    //
    // The positions of the manual left and right eyes in image-plate
    // coordinates.
    // By default, we will use the center of the screen for X and Y values
    // (X values are adjusted for default eye separation), and
    // 0.4572 meters (18 inches) for the Z value.
    // These match defaults elsewhere in the system.
    //
    Point3d leftManualEyeInImagePlate = new Point3d(0.142, 0.135, 0.4572);
    Point3d rightManualEyeInImagePlate = new Point3d(0.208, 0.135, 0.4572);

    //
    // View that is attached to this Canvas3D.
    //
    View view = null;

    // View waiting to be set
    View pendingView;

    //
    // View cache for this canvas and its associated view.
    //
    CanvasViewCache canvasViewCache = null;

    // Issue 109: View cache for this canvas, for computing view frustum planes
    CanvasViewCache canvasViewCacheFrustum = null;

    // Since multiple renderAtomListInfo, share the same vecBounds
    // we want to do the intersection test only once per renderAtom
    // this flag is set to true after the first intersect and set to
    // false during checkForCompaction in renderBin
    boolean raIsVisible = false;

    RenderAtom ra = null;

    // Stereo related field has changed.
    static final int STEREO_DIRTY                   = 0x01;
    // MonoscopicViewPolicy field has changed.
    static final int MONOSCOPIC_VIEW_POLICY_DIRTY   = 0x02;
    // Left/right eye in image plate field has changed.
    static final int EYE_IN_IMAGE_PLATE_DIRTY       = 0x04;
    // Canvas has moved/resized.
    static final int MOVED_OR_RESIZED_DIRTY         = 0x08;

    // Canvas Background changed (this may affect doInfinite flag)
    static final int BACKGROUND_DIRTY               = 0x10;

    // Canvas Background Image changed
    static final int BACKGROUND_IMAGE_DIRTY         = 0x20;


    // Mask that indicates this Canvas view dependence info. has changed,
    // and CanvasViewCache may need to recompute the final view matries.
    static final int VIEW_INFO_DIRTY = (STEREO_DIRTY |
					MONOSCOPIC_VIEW_POLICY_DIRTY |
					EYE_IN_IMAGE_PLATE_DIRTY |
					MOVED_OR_RESIZED_DIRTY |
					BACKGROUND_DIRTY |
					BACKGROUND_IMAGE_DIRTY);

    // Issue 163: Array of dirty bits is used because the Renderer and
    // RenderBin run asynchronously. Now that they each have a separate
    // instance of CanvasViewCache (due to the fix for Issue 109), they
    // need separate dirty bits. Array element 0 is used for the Renderer and
    // element 1 is used for the RenderBin.
    static final int RENDERER_DIRTY_IDX = 0;
    static final int RENDER_BIN_DIRTY_IDX = 1;
    int[] cvDirtyMask = new int[2];

    // This boolean informs the J3DGraphics2DImpl that the window is resized
    boolean resizeGraphics2D = true;
    //
    // This boolean allows an application to start and stop the render
    // loop on this canvas.
    //
    volatile boolean isRunning = true;

    // This is used by MasterControl only. MC relay on this in a
    // single loop to set renderer thread. During this time,
    // the isRunningStatus can't change by user thread.
    volatile boolean isRunningStatus = true;

    // This is true when the canvas is ready to be rendered into
    boolean active = false;

    // This is true when the canvas is visible
    boolean visible = false;

    // This is true if context need to recreate
    boolean ctxReset = true;

    // The Screen3D that corresponds to this Canvas3D
    Screen3D screen = null;

    // Flag to indicate that image is render completely
    // so swap is valid.
    boolean imageReady = false;


    //
    // The current fog enable state
    //
    int fogOn = 0;

    // The 3D Graphics context used for immediate mode rendering
    // into this canvas.
    GraphicsContext3D graphicsContext3D = null;
    boolean waiting = false;
    boolean swapDone = false;

    GraphicsConfiguration graphicsConfiguration;

    // The Java 3D Graphics2D object used for Java2D/AWT rendering
    // into this Canvas3D
    J3DGraphics2DImpl graphics2D = null;

    // Lock used to synchronize the creation of the 2D and 3D
    // graphics context objects
    Object gfxCreationLock = new Object();

    // The source of the currently loaded localToVWorld for this Canvas3D
    // (used to only update the model matrix when it changes)
    //    Transform3D	localToVWorldSrc = null;

    // The current vworldToEc Transform
    Transform3D vworldToEc = new Transform3D();

    // The view transform (VPC to EC) for the current eye.
    // NOTE that this is *read-only*
    Transform3D vpcToEc;

    // Opaque object representing the underlying drawable (window). This
    // is defined by the Pipeline.
    Drawable drawable = null;

    // graphicsConfigTable is a static hashtable which allows getBestConfiguration()
    // in NativeConfigTemplate3D to map a GraphicsConfiguration to the pointer
    // to the actual GLXFBConfig that glXChooseFBConfig() returns.  The Canvas3D
    // doesn't exist at the time getBestConfiguration() is called, and
    // X11GraphicsConfig neither maintains this pointer nor provides a public
    // constructor to allow Java 3D to extend it.
    static Hashtable<GraphicsConfiguration,GraphicsConfigInfo> graphicsConfigTable =
            new Hashtable<GraphicsConfiguration,GraphicsConfigInfo>();

    // The native graphics version, vendor, and renderer information
    String nativeGraphicsVersion = "<UNKNOWN>";
    String nativeGraphicsVendor = "<UNKNOWN>";
    String nativeGraphicsRenderer = "<UNKNOWN>";

    boolean firstPaintCalled = false;

    // This reflects whether or not this canvas has seen an addNotify. It is
    // forced to true for off-screen canvases
    boolean added = false;

    // Flag indicating whether addNotify has been called (so we don't process it twice).
    private boolean addNotifyCalled = false;

    // This is the id for the underlying graphics context structure.
    Context ctx = null;

    // since the ctx id can be the same as the previous one,
    // we need to keep a time stamp to differentiate the contexts with the
    // same id
    volatile long ctxTimeStamp = 0;

    // The current context setting for local eye lighting
    boolean ctxEyeLightingEnable = false;

    // This AppearanceRetained Object refelects the current state of this
    // canvas.  It is used to optimize setting of attributes at render time.
    AppearanceRetained currentAppear = new AppearanceRetained();

    // This MaterialRetained Object refelects the current state of this canvas.
    // It is used to optimize setting of attributes at render time.
    MaterialRetained currentMaterial = new MaterialRetained();

    /**
     * The object used for View Frustum Culling
     */
    CachedFrustum viewFrustum = new CachedFrustum();

    /**
     * The RenderBin bundle references used to decide what the underlying
     * context contains.
     */
    LightBin lightBin = null;
    EnvironmentSet environmentSet = null;
    AttributeBin attributeBin = null;
    ShaderBin shaderBin = null;
    RenderMolecule renderMolecule = null;
    PolygonAttributesRetained polygonAttributes = null;
    LineAttributesRetained lineAttributes = null;
    PointAttributesRetained pointAttributes = null;
    MaterialRetained material = null;
    boolean enableLighting = false;
    TransparencyAttributesRetained transparency = null;
    ColoringAttributesRetained coloringAttributes = null;
    Transform3D modelMatrix = null;
    Transform3D projTrans = null;
    TextureBin textureBin = null;


    /**
     * cached RenderBin states for lazy native states update
     */
    LightRetained lights[] = null;
    int frameCount[] = null;
    long enableMask = -1;
    FogRetained fog = null;
    ModelClipRetained modelClip = null;
    Color3f sceneAmbient = new Color3f();
    TextureUnitStateRetained[] texUnitState = null;

    /**
     * These cached values are only used in Pure Immediate and Mixed Mode rendering
     */
    TextureRetained texture = null;
    TextureAttributesRetained texAttrs = null;
    TexCoordGenerationRetained texCoordGeneration = null;
    RenderingAttributesRetained renderingAttrs = null;
    AppearanceRetained appearance = null;

    ShaderProgramRetained  shaderProgram = null;

    // only used in Mixed Mode rendering
    Object appHandle = null;

    /**
     * Set to true when any one of texture state use
     * Texture Generation linear mode. This is used for D3D
     * temporary turn displayList off and do its own coordinate
     * generation since D3D don't support it.
     *
     * TODO aces : is this still true in DX9?
     */
    boolean texLinearMode = false;

    /**
     * Dirty bit to determine if the NodeComponent needs to be re-sent
     * down to the underlying API
     */
    int canvasDirty = 0xffff;

    // True when either one of dirtyRenderMoleculeList,
    // dirtyDlistPerRinfoList, dirtyRenderAtomList size > 0
    boolean dirtyDisplayList = false;

ArrayList<RenderMolecule> dirtyRenderMoleculeList = new ArrayList<RenderMolecule>();
ArrayList<RenderAtomListInfo> dirtyRenderAtomList = new ArrayList<RenderAtomListInfo>();
// List of (Rm, rInfo) pair of individual dlists that need to be rebuilt
ArrayList<Object[]> dirtyDlistPerRinfoList = new ArrayList<Object[]>();

ArrayList<Integer> displayListResourceFreeList = new ArrayList<Integer>();
ArrayList<Integer> textureIdResourceFreeList = new ArrayList<Integer>();

    // an unique bit to identify this canvas
    int canvasBit = 0;
    // an unique number to identify this canvas : ( canvasBit = 1 << canvasId)
    int canvasId = 0;
    // Indicates whether the canvasId has been allocated
    private boolean canvasIdAlloc = false;

    // Avoid using this as lock, it cause deadlock
    Object cvLock = new Object();
    Object evaluateLock = new Object();
    Object dirtyMaskLock = new Object();

    // Use by D3D when toggle between window/fullscreen mode.
    // Note that in fullscreen mode, the width and height get
    // by canvas is smaller than expected.
    boolean fullScreenMode = false;
    int fullscreenWidth;
    int fullscreenHeight;

    // For D3D, instead of using the same variable in Renderer,
    // each canvas has to build its own displayList.
    boolean needToRebuildDisplayList = false;

    // Read-only flag that indicates whether the following texture features
    // are supported for this canvas.

    static final int TEXTURE_3D			= 0x0001;
    static final int TEXTURE_COLOR_TABLE	= 0x0002;
    static final int TEXTURE_MULTI_TEXTURE	= 0x0004;
    static final int TEXTURE_COMBINE		= 0x0008;
    static final int TEXTURE_COMBINE_DOT3	= 0x0010;
    static final int TEXTURE_COMBINE_SUBTRACT	= 0x0020;
    static final int TEXTURE_REGISTER_COMBINERS	= 0x0040;
    static final int TEXTURE_CUBE_MAP		= 0x0080;
    static final int TEXTURE_SHARPEN		= 0x0100;
    static final int TEXTURE_DETAIL		= 0x0200;
    static final int TEXTURE_FILTER4		= 0x0400;
    static final int TEXTURE_ANISOTROPIC_FILTER	= 0x0800;
    static final int TEXTURE_LOD_RANGE		= 0x1000;
    static final int TEXTURE_LOD_OFFSET		= 0x2000;
    // Use by D3D to indicate using one pass Blend mode
    // if Texture interpolation mode is support.
    static final int TEXTURE_LERP               = 0x4000;
    static final int TEXTURE_NON_POWER_OF_TWO	= 0x8000;
    static final int TEXTURE_AUTO_MIPMAP_GENERATION = 0x10000;

    int textureExtendedFeatures = 0;

    // Extensions supported by the underlying canvas
    //
    // NOTE: we should remove EXT_BGR and EXT_ABGR when the imaging code is
    // rewritten
    static final int SUN_GLOBAL_ALPHA            = 0x1;
    static final int EXT_ABGR                    = 0x2;
    static final int EXT_BGR                     = 0x4;
    static final int MULTISAMPLE                 = 0x8;

    // The following 10 variables are set by the native
    // createNewContext()/createQueryContext() methods

    // Supported Extensions
    int extensionsSupported = 0;

    // Anisotropic Filter degree
    float anisotropicDegreeMax = 1.0f;

    // Texture Boundary Width Max
    int   textureBoundaryWidthMax = 0;

    boolean multiTexAccelerated = false;

    // Max number of texture coordinate sets
    int maxTexCoordSets = 1;

    // Max number of fixed-function texture units
    int maxTextureUnits = 1;

    // Max number of fragment shader texture units
    int maxTextureImageUnits = 0;

    // Max number of vertex shader texture units
    int maxVertexTextureImageUnits = 0;

    // Max number of combined shader texture units
    int maxCombinedTextureImageUnits = 0;

    // Max number of vertex attrs (not counting coord, etc.)
    int maxVertexAttrs = 0;

    // End of variables set by createNewContext()/createQueryContext()

    // The total available number of texture units used by either the
    // fixed-function or programmable shader pipeline.
    // This is computed as: max(maxTextureUnits, maxTextureImageUnits)
    int maxAvailableTextureUnits;

    // Texture Width, Height Max
    int   textureWidthMax = 0;
    int   textureHeightMax = 0;

    // Texture3D Width, Heigh, Depth Max
    int   texture3DWidthMax = -1;
    int   texture3DHeightMax = -1;
    int   texture3DDepthMax = -1;

    // Cached position & size for CanvasViewCache.
    // We don't want to call canvas.getxx method in Renderer
    // since it will cause deadlock as removeNotify() need to get
    // component lock of Canvas also and need to wait Renderer to
    // finish before continue. So we invoke the method now in
    // CanvasViewEventCatcher.
    Point newPosition = new Point();
    Dimension newSize = new Dimension();

// Remember OGL context resources to free
// before context is destroy.
// It is used when sharedCtx = false;
ArrayList<TextureRetained> textureIDResourceTable = new ArrayList<TextureRetained>(5);

    // The following variables are used by the lazy download of
    // states code to keep track of the set of current to be update bins

    static final int LIGHTBIN_BIT	= 0x0;
    static final int ENVIRONMENTSET_BIT	= 0x1;
    static final int ATTRIBUTEBIN_BIT	= 0x2;
    static final int TEXTUREBIN_BIT	= 0x3;
    static final int RENDERMOLECULE_BIT	= 0x4;
    static final int TRANSPARENCY_BIT	= 0x5;
    static final int SHADERBIN_BIT	= 0x6;

    // bitmask to specify if the corresponding "bin" needs to be updated
    int stateUpdateMask = 0;

    // the set of current "bins" that is to be updated, the stateUpdateMask
    // specifies if each bin in this set is updated or not.
    Object curStateToUpdate[] = new Object[7];

    /**
     * The list of lights that are currently being represented in the native
     * graphics context.
     */
    LightRetained[] currentLights = null;

    /**
     * Flag to override RenderAttributes.depthBufferWriteEnable
     */
    boolean depthBufferWriteEnableOverride = false;

    /**
     * Flag to override RenderAttributes.depthBufferEnable
     */
    boolean depthBufferEnableOverride = false;

    // current state of depthBufferWriteEnable
    boolean depthBufferWriteEnable = true;

    boolean vfPlanesValid = false;

    // The event catcher for this canvas.
    EventCatcher eventCatcher;

    // The view event catcher for this canvas.
    private CanvasViewEventCatcher canvasViewEventCatcher;

    // The top-level parent window for this canvas.
    private Window windowParent;

    // Issue 458 - list of all parent containers for this canvas
    // (includes top-level parent window)
    private LinkedList<Container> containerParentList = new LinkedList<Container>();

    // flag that indicates if light has changed
    boolean lightChanged = false;

    // resource control object
    DrawingSurfaceObject drawingSurfaceObject;

    // true if context is valid for rendering
    boolean validCtx = false;

    // true if canvas is valid for rendering
    boolean validCanvas = false;

    // true if ctx changed between render and swap. In this case
    // cv.canvasDirty flag will not reset in Renderer.
    // This case happen when GraphicsContext3D invoked doClear()
    // and canvas removeNotify() called while Renderer is running
    boolean ctxChanged = false;

    // Default graphics configuration
    private static GraphicsConfiguration defaultGcfg = null;

    // Returns default graphics configuration if user passes null
    // into the Canvas3D constructor
    private static synchronized GraphicsConfiguration  defaultGraphicsConfiguration() {
        if (defaultGcfg == null) {
            GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
            defaultGcfg = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getBestConfiguration(template);
        }
        return defaultGcfg;
    }

    // Returns true if this is a valid graphics configuration, obtained
    // via a GraphicsConfigTemplate3D.
    private static boolean isValidConfig(GraphicsConfiguration gconfig) {
        // If this is a valid GraphicsConfiguration object, then it will
        // be in the graphicsConfigTable
        return graphicsConfigTable.containsKey(gconfig);
    }

    // Checks the given graphics configuration, and throws an exception if
    // the config is null or invalid.
    private static synchronized GraphicsConfiguration
            checkForValidGraphicsConfig(GraphicsConfiguration gconfig, boolean offScreen) {

        // Issue 266 - for backwards compatibility with legacy applications,
        // we will accept a null GraphicsConfiguration for an on-screen Canvas3D
        // only if the "allowNullGraphicsConfig" system property is set to true.
        if (!offScreen && VirtualUniverse.mc.allowNullGraphicsConfig) {
            if (gconfig == null) {
                // Print out warning if Canvas3D is called with a
                // null GraphicsConfiguration
                System.err.println(J3dI18N.getString("Canvas3D7"));
                System.err.println("    " + J3dI18N.getString("Canvas3D18"));

                // Use a default graphics config
                gconfig = defaultGraphicsConfiguration();
            }
        }

        // Validate input graphics config
        if (gconfig == null) {
            throw new NullPointerException(J3dI18N.getString("Canvas3D19"));
        } else if (!isValidConfig(gconfig)) {
            throw new IllegalArgumentException(J3dI18N.getString("Canvas3D17"));
        }

        return gconfig;
    }

    // Return the actual graphics config that will be used to construct
    // the AWT Canvas. This is permitted to be non-unique or null.
    private static GraphicsConfiguration getGraphicsConfig(GraphicsConfiguration gconfig) {
        return Pipeline.getPipeline().getGraphicsConfig(gconfig);
    }

    /**
     * Constructs and initializes a new Canvas3D object that Java 3D
     * can render into. The following Canvas3D attributes are initialized
     * to default values as shown:
     * <ul>
     * left manual eye in image plate : (0.142, 0.135, 0.4572)<br>
     * right manual eye in image plate : (0.208, 0.135, 0.4572)<br>
     * stereo enable : true<br>
     * double buffer enable : true<br>
     * monoscopic view policy : View.CYCLOPEAN_EYE_VIEW<br>
     * off-screen mode : false<br>
     * off-screen buffer : null<br>
     * off-screen location : (0,0)<br>
     * </ul>
     *
     * @param graphicsConfiguration a valid GraphicsConfiguration object that
     * will be used to create the canvas.  This object should not be null and
     * should be created using a GraphicsConfigTemplate3D or the
     * getPreferredConfiguration() method of the SimpleUniverse utility.  For
     * backward compatibility with earlier versions of Java 3D, a null or
     * default GraphicsConfiguration will still work when used to create a
     * Canvas3D on the default screen, but an error message will be printed.
     * A NullPointerException or IllegalArgumentException will be thrown in a
     * subsequent release.
     *
     * @exception IllegalArgumentException if the specified
     * GraphicsConfiguration does not support 3D rendering
     */
    public Canvas3D(GraphicsConfiguration graphicsConfiguration) {
	this(null, checkForValidGraphicsConfig(graphicsConfiguration, false), false);
    }

    /**
     * Constructs and initializes a new Canvas3D object that Java 3D
     * can render into.
     *
     * @param graphicsConfiguration a valid GraphicsConfiguration object
     * that will be used to create the canvas.  This must be created either
     * with a GraphicsConfigTemplate3D or by using the
     * getPreferredConfiguration() method of the SimpleUniverse utility.
     *
     * @param offScreen a flag that indicates whether this canvas is
     * an off-screen 3D rendering canvas.  Note that if offScreen
     * is set to true, this Canvas3D object cannot be used for normal
     * rendering; it should not be added to any Container object.
     *
     * @exception NullPointerException if the GraphicsConfiguration
     * is null.
     *
     * @exception IllegalArgumentException if the specified
     * GraphicsConfiguration does not support 3D rendering
     *
     * @since Java 3D 1.2
     */
    public Canvas3D(GraphicsConfiguration graphicsConfiguration, boolean offScreen) {
        this(null, checkForValidGraphicsConfig(graphicsConfiguration, offScreen), offScreen);
    }

    // Private constructor only called by the two public constructors after
    // they have validated the graphics config (and possibly constructed a new
    // default config).
    // The graphics config must be valid, unique, and non-null.
    private Canvas3D(Object dummyObj1,
            GraphicsConfiguration graphicsConfiguration,
            boolean offScreen) {
        this(dummyObj1,
                graphicsConfiguration,
                getGraphicsConfig(graphicsConfiguration),
                offScreen);
    }

    // Private constructor only called by the previous private constructor.
    // The graphicsConfiguration parameter is used by Canvas3D to lookup the
    // graphics device and graphics template. The graphicsConfiguration2
    // parameter is generated by the Pipeline from graphicsConfiguration and
    // is only used to initialize the java.awt.Canvas.
    private Canvas3D(Object dummyObj1,
            GraphicsConfiguration graphicsConfiguration,
            GraphicsConfiguration graphicsConfiguration2,
            boolean offScreen) {

	super(graphicsConfiguration2);

	this.offScreen = offScreen;
	this.graphicsConfiguration = graphicsConfiguration;

        // Issue 131: Set the autoOffScreen variable based on whether this
        // canvas3d implements the AutoOffScreenCanvas3D tagging interface.
        // Eventually, we may replace this with an actual API.
        boolean autoOffScreenCanvas3D = false;
        if (this instanceof com.sun.j3d.exp.swing.impl.AutoOffScreenCanvas3D) {
            autoOffScreenCanvas3D = true;
        }

        // Throw an illegal argument exception if an on-screen canvas is tagged
        // as an  auto-off-screen canvas
        if (autoOffScreenCanvas3D && !offScreen) {
            throw new IllegalArgumentException(J3dI18N.getString("Canvas3D25"));
        }

        // Issue 163 : Set dirty bits for both Renderer and RenderBin
        cvDirtyMask[0] = VIEW_INFO_DIRTY;
        cvDirtyMask[1] = VIEW_INFO_DIRTY;

    	GraphicsConfigInfo gcInfo = graphicsConfigTable.get(graphicsConfiguration);
        requestedStencilSize = gcInfo.getGraphicsConfigTemplate3D().getStencilSize();

	if (offScreen) {

            // Issue 131: set manual rendering flag based on whether this is
            // an auto-off-screen Canvas3D.
            manualRendering = !autoOffScreenCanvas3D;

            screen = new Screen3D(graphicsConfiguration, offScreen);

            // QUESTION: keep a list of off-screen Screen3D objects?
            // Does this list need to be grouped by GraphicsDevice?

	    synchronized(dirtyMaskLock) {
	        cvDirtyMask[0] |= MOVED_OR_RESIZED_DIRTY;
	        cvDirtyMask[1] |= MOVED_OR_RESIZED_DIRTY;
	    }

	    // this canvas will not receive the paint callback,
	    // so we need to set the necessary flags here
            firstPaintCalled = true;

            if (manualRendering) {
                // since this canvas will not receive the addNotify
                // callback from AWT, set the added flag here for
                // evaluateActive to work correctly
                added = true;
            }

            evaluateActive();

            // create the rendererStructure object
            //rendererStructure = new RendererStructure();
	    offScreenCanvasLoc = new Point(0, 0);
	    offScreenCanvasSize = new Dimension(0, 0);

            this.setLocation(offScreenCanvasLoc);
            this.setSize(offScreenCanvasSize);
	    newSize = offScreenCanvasSize;
	    newPosition = offScreenCanvasLoc;

            // Issue 131: create event catchers for auto-offScreen
            if (!manualRendering) {
                eventCatcher = new EventCatcher(this);
                canvasViewEventCatcher = new CanvasViewEventCatcher(this);
            }
        } else {

	    GraphicsDevice graphicsDevice;
	    graphicsDevice = graphicsConfiguration.getDevice();

 	    eventCatcher = new EventCatcher(this);
	    canvasViewEventCatcher = new CanvasViewEventCatcher(this);

	    synchronized(VirtualUniverse.mc.deviceScreenMap) {
			screen = VirtualUniverse.mc.deviceScreenMap.get(graphicsDevice);

		if (screen == null) {
		    screen = new Screen3D(graphicsConfiguration, offScreen);
			VirtualUniverse.mc.deviceScreenMap.put(graphicsDevice, screen);
		}
	    }

	}

        lights = new LightRetained[VirtualUniverse.mc.maxLights];
        frameCount = new int[VirtualUniverse.mc.maxLights];
	for (int i=0; i<frameCount.length;i++) {
	    frameCount[i] = -1;
	}

        // Construct the drawing surface object for this Canvas3D
        drawingSurfaceObject =
                Pipeline.getPipeline().createDrawingSurfaceObject(this);

	// Get double buffer, stereo available, scene antialiasing
	// flags from graphics config
	GraphicsConfigTemplate3D.getGraphicsConfigFeatures(this);

        useDoubleBuffer = doubleBufferEnable && doubleBufferAvailable;
        useStereo = stereoEnable && stereoAvailable;
        useSharedCtx = VirtualUniverse.mc.isSharedCtx;

        // Issue 131: assert that only an off-screen canvas can be demand-driven
        assert (!offScreen && manualRendering) == false;

        // Assert that offScreen is *not* double-buffered or stereo
        assert (offScreen && useDoubleBuffer) == false;
        assert (offScreen && useStereo) == false;
    }

    /**
     * This method overrides AWT's handleEvent class...
     */
    void sendEventToBehaviorScheduler(AWTEvent evt) {

	ViewPlatform vp;


	if ((view != null) && ((vp = view.getViewPlatform()) != null)) {
	    VirtualUniverse univ =
		((ViewPlatformRetained)(vp.retained)).universe;
	    if (univ != null) {
		univ.behaviorStructure.handleAWTEvent(evt);
	    }
	}
    }

    /**
     * Method to return whether or not the Canvas3D is recursively visible;
     * that is, whether the Canas3D is currently visible on the screen. Note
     * that we don't directly use isShowing() because that won't work for an
     * auto-offScreen Canvas3D.
     */
    private boolean isRecursivelyVisible() {
        Container parent = getParent();
        return isVisible() && parent != null && parent.isShowing();
    }

    /**
     * Method to return whether the top-level Window parent is iconified
     */
    private boolean isIconified() {
        if (windowParent instanceof Frame) {
            return (((Frame)windowParent).getExtendedState() & Frame.ICONIFIED) != 0;
        }

        return false;
    }

    // Issue 458 - evaluate this Canvas3D's visibility whenever we get a
    // Window or Component Event that could change it.
    void evaluateVisiblilty() {
        boolean nowVisible = isRecursivelyVisible() && !isIconified();

        // Only need to reevaluate and repaint if visibility has changed
        if (this.visible != nowVisible) {
            this.visible = nowVisible;
            evaluateActive();
            if (nowVisible) {
                if (view != null) {
                    view.repaint();
                }
            }
        }
    }

    /**
     * This version looks for the view and notifies it.
     */
    void redraw() {
        if ((view != null) && active && isRunning) {
	    view.repaint();
        }
    }

    /**
     * Canvas3D uses the paint callback to track when it is possible to
     * render into the canvas.  Subclasses of Canvas3D that override this
     * method need to call super.paint() in their paint method for Java 3D
     * to function properly.
     * @param g the graphics context
     */
    public void paint(Graphics g) {

	if (!firstPaintCalled && added && validCanvas &&
	    validGraphicsMode()) {

	    try {
		newSize = getSize();
		newPosition = getLocationOnScreen();
	    } catch (IllegalComponentStateException e) {
		return;
	    }

	    synchronized (drawingSurfaceObject) {
		drawingSurfaceObject.getDrawingSurfaceObjectInfo();
	    }

	    firstPaintCalled = true;
	    visible = true;
	    evaluateActive();
	}
	redraw();
    }

    // When this canvas is added to a frame, this notification gets called.  We
    // can get drawing surface information at this time.  Note: we cannot get
    // the X11 window id yet, unless it is a reset condition.
    /**
     * Canvas3D uses the addNotify callback to track when it is added
     * to a container.  Subclasses of Canvas3D that override this
     * method need to call super.addNotify() in their addNotify() method for Java 3D
     * to function properly.
     */
    public void addNotify() {
        // Return immediately if addNotify called twice with no removeNotify
        if (addNotifyCalled) {
            return;
        }
        addNotifyCalled = true;

        // Issue 131: This method is now being called by JCanvas3D for its
        // off-screen Canvas3D, so we need to handle off-screen properly here.
        // Do nothing for manually-rendered off-screen canvases
        if (manualRendering) {
            return;
        }

	Renderer rdr = null;

	if (isRunning && (screen != null)) {
	    // If there is other Canvas3D in the same screen
	    // rendering, stop it before JDK create new Canvas

	    rdr = screen.renderer;
	    if (rdr != null) {
		VirtualUniverse.mc.postRequest(MasterControl.STOP_RENDERER, rdr);
		while (!rdr.userStop) {
		    MasterControl.threadYield();
		}
	    }
	}

        // Issue 131: Don't call super for off-screen Canvas3D
        if (!offScreen) {
            super.addNotify();
        }
	screen.addUser(this);

        // Issue 458 - Add the eventCatcher as a component listener for each
        // parent container in the window hierarchy
        assert containerParentList.isEmpty();

        windowParent = null;
        Container container = this.getParent();
        while (container != null) {
            if (container instanceof Window) {
                windowParent = (Window)container;
            }
            container.addComponentListener(eventCatcher);
            container.addComponentListener(canvasViewEventCatcher);
            containerParentList.add(container);
            container = container.getParent();
        }

        this.addComponentListener(eventCatcher);
        this.addComponentListener(canvasViewEventCatcher);

        if (windowParent != null) {
            windowParent.addWindowListener(eventCatcher);
        }

	synchronized(dirtyMaskLock) {
	    cvDirtyMask[0] |= MOVED_OR_RESIZED_DIRTY;
	    cvDirtyMask[1] |= MOVED_OR_RESIZED_DIRTY;
	}

        allocateCanvasId();

	validCanvas = true;
	added = true;

        // Since we won't get a paint call for off-screen canvases, we need
        // to set the first paint and visible flags here. We also need to
        // call evaluateActive for the same reason.
        if (offScreen) {
            firstPaintCalled = true;
            visible = true;
            evaluateActive();
        }

	// In case the same canvas is removed and add back,
	// we have to change isRunningStatus back to true;
	if (isRunning && !fatalError) {
            isRunningStatus = true;
        }

        ctxTimeStamp = 0;
	if ((view != null) && (view.universe != null)) {
	    view.universe.checkForEnableEvents();
	}

	if (rdr != null) {
            // Issue 84: Send a message to MC to restart renderer
            // Note that this also obviates the need for the earlier fix to
            // issue 131 which called redraw() for auto-off-screen Canvas3Ds
            // (and this is a more robust fix)
            VirtualUniverse.mc.postRequest(MasterControl.START_RENDERER, rdr);
            while (rdr.userStop) {
                MasterControl.threadYield();
            }
	}
    }

    // When this canvas is removed a frame, this notification gets called.  We
    // need to release the native context at this time.  The underlying window
    // is about to go away.
    /**
     * Canvas3D uses the removeNotify callback to track when it is removed
     * from a container.  Subclasses of Canvas3D that override this
     * method need to call super.removeNotify() in their removeNotify()
     * method for Java 3D to function properly.
     */
    public void removeNotify() {
        // Return immediately if addNotify not called first
        if (!addNotifyCalled) {
            return;
        }
        addNotifyCalled = false;

        // Do nothing for manually-rendered off-screen canvases
        if (manualRendering) {
            return;
        }

	Renderer rdr = null;

	if (isRunning && (screen != null)) {
	    // If there is other Canvas3D in the same screen
	    // rendering, stop it before JDK create new Canvas

	    rdr = screen.renderer;
	    if (rdr != null) {
		VirtualUniverse.mc.postRequest(MasterControl.STOP_RENDERER, rdr);
		while (!rdr.userStop) {
		    MasterControl.threadYield();
		}
	    }
	}

	// Note that although renderer userStop is true,
	// MasterControl can still schedule renderer to run through
	// runMonotor(RUN_RENDERER_CLEANUP) which skip userStop
	// thread checking.
	// For non-offscreen rendering the following call will
	// block waiting until all resources is free before
	// continue

	synchronized (drawingSurfaceObject) {
	    validCtx = false;
	    validCanvas = false;
	}

	removeCtx();

        Pipeline.getPipeline().freeDrawingSurface(this, drawingSurfaceObject);

        // Clear first paint and visible flags
        firstPaintCalled = false;
	visible = false;

	screen.removeUser(this);
	evaluateActive();

        freeCanvasId();

	ra = null;
	graphicsContext3D = null;

	ctx = null;
	// must be after removeCtx() because
	// it will free graphics2D textureID
	graphics2D = null;

	super.removeNotify();

        // Release and clear.
        for (Container container : containerParentList) {
            container.removeComponentListener(eventCatcher);
            container.removeComponentListener(canvasViewEventCatcher);
        }
        containerParentList.clear();
        this.removeComponentListener(eventCatcher);
        this.removeComponentListener(canvasViewEventCatcher);

	if (eventCatcher != null) {
	    this.removeFocusListener(eventCatcher);
	    this.removeKeyListener(eventCatcher);
	    this.removeMouseListener(eventCatcher);
	    this.removeMouseMotionListener(eventCatcher);
	    this.removeMouseWheelListener(eventCatcher);
	    eventCatcher.reset();
	}

	if (windowParent != null) {
	    windowParent.removeWindowListener(eventCatcher);
	    windowParent.requestFocus();
	}

        added = false;

	if (rdr != null) {
            // Issue 84: Send a message to MC to restart renderer
            VirtualUniverse.mc.postRequest(MasterControl.START_RENDERER, rdr);
            while (rdr.userStop) {
                MasterControl.threadYield();
            }
	}

        // Fix for issue 102 removing strong reference and avoiding memory leak
        // due retention of parent container
        this.windowParent = null;
    }

    void allocateCanvasId() {
        if (!canvasIdAlloc) {
            canvasId = VirtualUniverse.mc.getCanvasId();
            canvasBit = 1 << canvasId;
            canvasIdAlloc = true;
        }
    }

    void freeCanvasId() {
        if (canvasIdAlloc) {
            VirtualUniverse.mc.freeCanvasId(canvasId);
            canvasBit = 0;
            canvasId = 0;
            canvasIdAlloc = false;
        }
    }

    // This decides if the canvas is active
    void evaluateActive() {
	// Note that no need to check for isRunning, we want
	// view register in order to create scheduler in pure immedite mode
	// Also we can't use this as lock, otherwise there is a
	// deadlock where updateViewCache get a lock of this and
        // get a lock of this component. But Container
	// remove will get a lock of this component follows by evaluateActive.

	synchronized (evaluateLock) {
	    if ((visible || manualRendering) && firstPaintCalled) {

		if (!active) {
		    active = true;
		    if (pendingView != null) {
			pendingView.evaluateActive();
		    }
		} else {
		    if ((pendingView != null) &&
			!pendingView.activeStatus) {
			pendingView.evaluateActive();
		    }
		}
	    } else {
		if (active) {
		    active = false;
		    if (view != null) {
			view.evaluateActive();
		    }
		}
	    }
	}

	if ((view != null) && (!active)) {
	    VirtualUniverse u = view.universe;
	    if ((u != null) && !u.isSceneGraphLock) {
		u.waitForMC();
	    }
	}
    }

    void setFrustumPlanes(Vector4d[] planes) {

	if(VirtualUniverse.mc.viewFrustumCulling) {
	    /* System.err.println("Canvas3D.setFrustumPlanes()"); */
	    viewFrustum.set(planes);
	}
    }


    /**
     * Retrieve the Screen3D object that this Canvas3D is attached to.
     * If this Canvas3D is an off-screen buffer, a new Screen3D object
     * is created corresponding to the off-screen buffer.
     * @return the 3D screen object that this Canvas3D is attached to
     */
    public Screen3D getScreen3D() {
	return screen;
    }

    /**
     * Get the immediate mode 3D graphics context associated with
     * this Canvas3D.  A new graphics context object is created if one does
     * not already exist.
     * @return a GraphicsContext3D object that can be used for immediate
     * mode rendering to this Canvas3D.
     */
    public GraphicsContext3D getGraphicsContext3D() {

	synchronized(gfxCreationLock) {
	    if (graphicsContext3D == null)
		graphicsContext3D = new GraphicsContext3D(this);
	}

	return graphicsContext3D;
    }

    /**
     * Get the 2D graphics object associated with
     * this Canvas3D.  A new 2D graphics object is created if one does
     * not already exist.
     *
     * @return a Graphics2D object that can be used for Java 2D
     * rendering into this Canvas3D.
     *
     * @since Java 3D 1.2
     */
    public J3DGraphics2D getGraphics2D() {
	synchronized(gfxCreationLock) {
	    if (graphics2D == null)
		graphics2D = new J3DGraphics2DImpl(this);
	}

	return graphics2D;
    }

    /**
     * This routine is called by the Java 3D rendering loop after clearing
     * the canvas and before any rendering has been done for this frame.
     * Applications that wish to perform operations in the rendering loop,
     * prior to any actual rendering may override this function.
     *
     * <p>
     * Updates to live Geometry, Texture, and ImageComponent objects
     * in the scene graph are not allowed from this method.
     *
     * <p>
     * NOTE: Applications should <i>not</i> call this method.
     */
    public void preRender() {
	// Do nothing; the user overrides this to cause some action
    }

    /**
     * This routine is called by the Java 3D rendering loop after completing
     * all rendering to the canvas for this frame and before the buffer swap.
     * Applications that wish to perform operations in the rendering loop,
     * following any actual rendering may override this function.
     *
     * <p>
     * Updates to live Geometry, Texture, and ImageComponent objects
     * in the scene graph are not allowed from this method.
     *
     * <p>
     * NOTE: Applications should <i>not</i> call this method.
     */
    public void postRender() {
	// Do nothing; the user overrides this to cause some action
    }

    /**
     * This routine is called by the Java 3D rendering loop after completing
     * all rendering to the canvas, and all other canvases associated with
     * this view, for this frame following the buffer swap.
     * Applications that wish to perform operations at the very
     * end of the rendering loop may override this function.
     * In off-screen mode, all rendering is copied to the off-screen
     * buffer before this method is called.
     *
     * <p>
     * Updates to live Geometry, Texture, and ImageComponent objects
     * in the scene graph are not allowed from this method.
     *
     * <p>
     * NOTE: Applications should <i>not</i> call this method.
     */
    public void postSwap() {
	// Do nothing; the user overrides this to cause some action
    }

    /**
     * This routine is called by the Java 3D rendering loop during the
     * execution of the rendering loop.  It is called once for each
     * field (i.e., once per frame on
     * a mono system or once each for the right eye and left eye on a
     * two-pass stereo system.  This is intended for use by applications that
     * want to mix retained/compiled-retained mode rendering with some
     * immediate mode rendering.  Applications that wish to perform
     * operations during the rendering loop, may override this
     * function.
     *
     * <p>
     * Updates to live Geometry, Texture, and ImageComponent objects
     * in the scene graph are not allowed from this method.
     *
     * <p>
     * NOTE: Applications should <i>not</i> call this method.
     * <p>
     *
     * @param fieldDesc field description, one of: FIELD_LEFT, FIELD_RIGHT or
     * FIELD_ALL.  Applications that wish to work correctly in stereo mode
     * should render the same image for both FIELD_LEFT and FIELD_RIGHT calls.
     * If Java 3D calls the renderer with FIELD_ALL then the immediate mode
     * rendering only needs to be done once.
     */
    public void renderField(int fieldDesc) {
	// Do nothing; the user overrides this to cause some action
    }

    /**
     * Stop the Java 3D renderer on this Canvas3D object.  If the
     * Java 3D renderer is currently running, the rendering will be
     * synchronized before being stopped.  No further rendering will be done
     * to this canvas by Java 3D until the renderer is started again.
     * In pure immediate mode this method should be called prior to adding
     * this canvas to an active View object.
     *
     * @exception IllegalStateException if this Canvas3D is in
     * off-screen mode.
     */
    public final void stopRenderer() {
        // Issue 131: renderer can't be stopped only if it is an offscreen,
        // manual canvas. Otherwise, it has to be seen as an onscreen canvas.
	if (manualRendering)
	    throw new IllegalStateException(J3dI18N.getString("Canvas3D14"));

	if (isRunning) {
	    VirtualUniverse.mc.postRequest(MasterControl.STOP_RENDERER, this);
	    isRunning = false;
	}
    }


    /**
     * Start the Java 3D renderer on this Canvas3D object.  If the
     * Java 3D renderer is not currently running, any rendering to other
     * Canvas3D objects sharing the same View will be synchronized before this
     * Canvas3D's renderer is (re)started.  When a Canvas3D is created, it is
     * initially marked as being started.  This means that as soon as the
     * Canvas3D is added to an active View object, the rendering loop will
     * render the scene graph to the canvas.
     */
    public final void startRenderer() {
        // Issue 260 : ignore attempt to start renderer if fatal error
        if (fatalError) {
            return;
        }

	if (!isRunning) {
	    VirtualUniverse.mc.postRequest(MasterControl.START_RENDERER, this);
	    isRunning = true;
	    redraw();
	}
    }

    /**
     * Retrieves the state of the renderer for this Canvas3D object.
     * @return the state of the renderer
     *
     * @since Java 3D 1.2
     */
    public final boolean isRendererRunning() {
	return isRunning;
    }

    // Returns the state of the fatal error flag
    boolean isFatalError() {
        return fatalError;
    }

    // Sets the fatal error flag to true; stop the renderer for this canvas
    void setFatalError() {
        fatalError = true;

	if (isRunning) {
            isRunning = false;

            if (!manualRendering) {
                VirtualUniverse.mc.postRequest(MasterControl.STOP_RENDERER, this);
            }
	}
    }


    /**
     * Retrieves a flag indicating whether this Canvas3D is an
     * off-screen canvas.
     *
     * @return <code>true</code> if this Canvas3D is an off-screen canvas;
     * <code>false</code> if this is an on-screen canvas.
     *
     * @since Java 3D 1.2
     */
    public boolean isOffScreen() {
	return offScreen;
    }


    /**
     * Sets the off-screen buffer for this Canvas3D.  The specified
     * image is written into by the Java 3D renderer.  The size of the
     * specified ImageComponent determines the size, in pixels, of
     * this Canvas3D--the size inherited from Component is ignored.
     * <p>
     * NOTE: the size, physical width, and physical height of the associated
     * Screen3D must be set explicitly prior to rendering.
     * Failure to do so will result in an exception.
     * <p>
     *
     * @param buffer the image component that will be rendered into by
     * subsequent calls to renderOffScreenBuffer. The image component must not
     * be part of a live scene graph, nor may it subsequently be made part of a
     * live scene graph while being used as an off-screen buffer; an
     * IllegalSharingException is thrown in such cases. The buffer may be null,
     * indicating that the previous off-screen buffer is released without a new
     * buffer being set.
     *
     * @exception IllegalStateException if this Canvas3D is not in
     * off-screen mode.
     *
     * @exception RestrictedAccessException if an off-screen rendering
     * is in process for this Canvas3D.
     *
     * @exception IllegalSharingException if the specified ImageComponent2D
     * is part of a live scene graph
     *
     * @exception IllegalSharingException if the specified ImageComponent2D is
     * being used by an immediate mode context, or by another Canvas3D as
     * an off-screen buffer.
     *
     * @exception IllegalArgumentException if the image class of the specified
     * ImageComponent2D is <i>not</i> ImageClass.BUFFERED_IMAGE.
     *
     * @exception IllegalArgumentException if the specified
     * ImageComponent2D is in by-reference mode and its
     * RenderedImage is null.
     *
     * @exception IllegalArgumentException if the ImageComponent2D format
     * is <i>not</i> a 3-component format (e.g., FORMAT_RGB)
     * or a 4-component format (e.g., FORMAT_RGBA).
     *
     * @see #renderOffScreenBuffer
     * @see Screen3D#setSize(int, int)
     * @see Screen3D#setSize(Dimension)
     * @see Screen3D#setPhysicalScreenWidth
     * @see Screen3D#setPhysicalScreenHeight
     *
     * @since Java 3D 1.2
     */
    public void setOffScreenBuffer(ImageComponent2D buffer) {
	int width, height;
        boolean freeCanvasId = false;

        if (!offScreen)
            throw new IllegalStateException(J3dI18N.getString("Canvas3D1"));

        if (offScreenRendering)
            throw new RestrictedAccessException(J3dI18N.getString("Canvas3D2"));

	// Check that offScreenBufferPending is not already set
	J3dDebug.doAssert(!offScreenBufferPending, "!offScreenBufferPending");

        if (offScreenBuffer != null && offScreenBuffer != buffer) {
            ImageComponent2DRetained i2dRetained =
                    (ImageComponent2DRetained)offScreenBuffer.retained;
            i2dRetained.setUsedByOffScreen(false);
        }

	if (buffer != null) {
	    ImageComponent2DRetained bufferRetained =
		(ImageComponent2DRetained)buffer.retained;

	    if (bufferRetained.byReference &&
		!(bufferRetained.getRefImage(0) instanceof BufferedImage)) {

		throw new IllegalArgumentException(J3dI18N.getString("Canvas3D15"));
	    }

	    if (bufferRetained.getNumberOfComponents() < 3 ) {
		throw new IllegalArgumentException(J3dI18N.getString("Canvas3D16"));
	    }

            if (buffer.isLive()) {
                throw new IllegalSharingException(J3dI18N.getString("Canvas3D26"));
            }

            if (bufferRetained.getInImmCtx()) {
                throw new IllegalSharingException(J3dI18N.getString("Canvas3D27"));
            }

            if (buffer != offScreenBuffer && bufferRetained.getUsedByOffScreen()) {
                throw new IllegalSharingException(J3dI18N.getString("Canvas3D28"));
            }

            bufferRetained.setUsedByOffScreen(true);

	    width = bufferRetained.width;
	    height = bufferRetained.height;

            // Issues 347, 348 - assign a canvasId for off-screen Canvas3D
            if (manualRendering) {
                sendAllocateCanvasId();
            }
        }
	else {
	    width = height = 0;

            // Issues 347, 348 - release canvasId for off-screen Canvas3D
            if (manualRendering) {
                freeCanvasId = true;
            }
        }

	if ((offScreenCanvasSize.width != width) ||
	    (offScreenCanvasSize.height != height)) {

	    if (drawable != null) {
		// Fix for Issue 18 and Issue 175
		// Will do destroyOffScreenBuffer in the Renderer thread.
		sendDestroyCtxAndOffScreenBuffer();
		drawable = null;
            }
            // Issue 396. Since context is invalid here, we should set it to null.
            ctx = null;

            // set the canvas dimension according to the buffer dimension
	    offScreenCanvasSize.setSize(width, height);
	    this.setSize(offScreenCanvasSize);

	    if (width > 0 && height > 0) {
		sendCreateOffScreenBuffer();
	    }

	}
	else if (ctx != null) {
            removeCtx();
	}

        if (freeCanvasId) {
                sendFreeCanvasId();
        }

        offScreenBuffer = buffer;

        synchronized(dirtyMaskLock) {
            cvDirtyMask[0] |= MOVED_OR_RESIZED_DIRTY;
            cvDirtyMask[1] |= MOVED_OR_RESIZED_DIRTY;
        }
    }

    /**
     * Retrieves the off-screen buffer for this Canvas3D.
     *
     * @return the current off-screen buffer for this Canvas3D.
     *
     * @exception IllegalStateException if this Canvas3D is not in
     * off-screen mode.
     *
     * @since Java 3D 1.2
     */
    public ImageComponent2D getOffScreenBuffer() {

        if (!offScreen)
            throw new IllegalStateException(J3dI18N.getString("Canvas3D1"));

        return (offScreenBuffer);
    }


    /**
     * Schedules the rendering of a frame into this Canvas3D's
     * off-screen buffer.  The rendering is done from the point of
     * view of the View object to which this Canvas3D has been added.
     * No rendering is performed if this Canvas3D object has not been
     * added to an active View.  This method does not wait for the rendering
     * to actually happen.  An application that wishes to know when
     * the rendering is complete must either subclass Canvas3D and
     * override the <code>postSwap</code> method, or call
     * <code>waitForOffScreenRendering</code>.
     *
     * @exception NullPointerException if the off-screen buffer is null.
     * @exception IllegalStateException if this Canvas3D is not in
     * off-screen mode, or if either the width or the height of
     * the associated Screen3D's size is <= 0, or if the associated
     * Screen3D's physical width or height is <= 0.
     * @exception RestrictedAccessException if an off-screen rendering
     * is already in process for this Canvas3D or if the Java 3D renderer
     * is stopped.
     *
     * @see #setOffScreenBuffer
     * @see Screen3D#setSize(int, int)
     * @see Screen3D#setSize(Dimension)
     * @see Screen3D#setPhysicalScreenWidth
     * @see Screen3D#setPhysicalScreenHeight
     * @see #waitForOffScreenRendering
     * @see #postSwap
     *
     * @since Java 3D 1.2
     */
    public void renderOffScreenBuffer() {

        if (!offScreen)
            throw new IllegalStateException(J3dI18N.getString("Canvas3D1"));

        // Issue 131: Cannot manually render to an automatic canvas.
        if (!manualRendering)
            throw new IllegalStateException(J3dI18N.getString("Canvas3D24"));

        // Issue 260 : Cannot render if we already have a fatal error
        if (fatalError) {
            throw new IllegalRenderingStateException(J3dI18N.getString("Canvas3D30"));
        }

        if (offScreenBuffer == null)
            throw new NullPointerException(J3dI18N.getString("Canvas3D10"));

	Dimension screenSize = screen.getSize();

        if (screenSize.width <= 0)
            throw new IllegalStateException(J3dI18N.getString("Canvas3D8"));

        if (screenSize.height <= 0)
            throw new IllegalStateException(J3dI18N.getString("Canvas3D9"));

        if (screen.getPhysicalScreenWidth() <= 0.0)
            throw new IllegalStateException(J3dI18N.getString("Canvas3D12"));

        if (screen.getPhysicalScreenHeight() <= 0.0)
            throw new IllegalStateException(J3dI18N.getString("Canvas3D13"));

        if (offScreenRendering)
            throw new RestrictedAccessException(J3dI18N.getString("Canvas3D2"));

	if (!isRunning)
            throw new RestrictedAccessException(J3dI18N.getString("Canvas3D11"));

	// Fix to issue 66
	if ((!active) || (pendingView == null)) {
	    /* No rendering is performed if this Canvas3D object has not been
	       added to an active View. */
	    return;
	}

        // Issue 131: moved code that determines off-screen boundary to separate
        // method that is called from the renderer

        offScreenRendering = true;

	// Fix to issue 66.
	/* This is an attempt to do the following check in one atomic operation :
	   ((view != null) && (view.inCanvasCallback)) */

	boolean inCanvasCallback = false;
	try {
	    inCanvasCallback = view.inCanvasCallback;

	} catch (NullPointerException npe) {
	    /* Do nothing here */
	}

        if (inCanvasCallback) {
	    // Here we assume that view is stable if inCanvasCallback
	    // is true. This assumption is valid among all j3d threads as
	    // all access to view is synchronized by MasterControl.
	    // Issue : user threads access to view isn't synchronize hence
	    // is model will break.
	    if (screen.renderer == null) {

		// It is possible that screen.renderer = null when this View
		// is shared by another onScreen Canvas and this callback
		// is from that Canvas. In this case it need one more
		// round before the renderer.
		screen.renderer = Screen3D.deviceRendererMap.get(screen.graphicsDevice);
		// screen.renderer may equal to null when multiple
		// screen is used and this Canvas3D is in different
		// screen sharing the same View not yet initialize.
	    }

	    // if called from render call back, send a message directly to
	    // the renderer message queue, and call renderer doWork
	    // to do the offscreen rendering now
	    if (Thread.currentThread() == screen.renderer) {

		J3dMessage createMessage = new J3dMessage();
		createMessage.threads = J3dThread.RENDER_THREAD;
		createMessage.type = J3dMessage.RENDER_OFFSCREEN;
		createMessage.universe = this.view.universe;
		createMessage.view = this.view;
		createMessage.args[0] = this;

		screen.renderer.rendererStructure.addMessage(createMessage);

		// modify the args to reflect offScreen rendering
		screen.renderer.args = new Object[4];
		screen.renderer.args[0] = new Integer(Renderer.REQUESTRENDER);
		screen.renderer.args[1] = this;
		screen.renderer.args[2] = view;
		// This extra argument 3 is needed in MasterControl to
		// test whether offscreen Rendering is used or not
		screen.renderer.args[3] = null;

		// call renderer doWork directly since we are already in
		// the renderer thread
		screen.renderer.doWork(0);
	    } else {

		// XXXX:
		// Now we are in trouble, this will cause deadlock if
		// waitForOffScreenRendering() is invoked
		  J3dMessage createMessage = new J3dMessage();
		  createMessage.threads = J3dThread.RENDER_THREAD;
		  createMessage.type = J3dMessage.RENDER_OFFSCREEN;
		  createMessage.universe = this.view.universe;
		  createMessage.view = this.view;
		  createMessage.args[0] = this;
		  screen.renderer.rendererStructure.addMessage(createMessage);
		  VirtualUniverse.mc.setWorkForRequestRenderer();
	    }

        } else if (Thread.currentThread() instanceof BehaviorScheduler) {

	    // If called from behavior scheduler, send a message directly to
	    // the renderer message queue.
	    // Note that we didn't use
	    // currentThread() == view.universe.behaviorScheduler
	    // since the caller may be another universe Behavior
	    // scheduler.
            J3dMessage createMessage = new J3dMessage();
            createMessage.threads = J3dThread.RENDER_THREAD;
            createMessage.type = J3dMessage.RENDER_OFFSCREEN;
            createMessage.universe = this.view.universe;
            createMessage.view = this.view;
            createMessage.args[0] = this;
	    screen.renderer.rendererStructure.addMessage(createMessage);
            VirtualUniverse.mc.setWorkForRequestRenderer();

 	} else {
            // send a message to renderBin
	    // Fix for issue 66 : Since view might not been set yet,
	    // we have to use pendingView instead.
            J3dMessage createMessage = new J3dMessage();
            createMessage.threads = J3dThread.UPDATE_RENDER;
            createMessage.type = J3dMessage.RENDER_OFFSCREEN;
            createMessage.universe = this.pendingView.universe;
            createMessage.view = this.pendingView;
            createMessage.args[0] = this;
	    createMessage.args[1] = offScreenBuffer;
            VirtualUniverse.mc.processMessage(createMessage);
	}
    }


    /**
     * Waits for this Canvas3D's off-screen rendering to be done.
     * This method will wait until the <code>postSwap</code> method of this
     * off-screen Canvas3D has completed.  If this Canvas3D has not
     * been added to an active view or if the renderer is stopped for this
     * Canvas3D, then this method will return
     * immediately.  This method must not be called from a render
     * callback method of an off-screen Canvas3D.
     *
     * @exception IllegalStateException if this Canvas3D is not in
     * off-screen mode, or if this method is called from a render
     * callback method of an off-screen Canvas3D.
     *
     * @see #renderOffScreenBuffer
     * @see #postSwap
     *
     * @since Java 3D 1.2
     */
    public void waitForOffScreenRendering() {

        if (!offScreen) {
            throw new IllegalStateException(J3dI18N.getString("Canvas3D1"));
        }

        if (Thread.currentThread() instanceof Renderer) {
            throw new IllegalStateException(J3dI18N.getString("Canvas3D31"));
        }

        while (offScreenRendering) {
            MasterControl.threadYield();
        }
    }


    /**
     * Sets the location of this off-screen Canvas3D.  The location is
     * the upper-left corner of the Canvas3D relative to the
     * upper-left corner of the corresponding off-screen Screen3D.
     * The function of this method is similar to that of
     * <code>Component.setLocation</code> for on-screen Canvas3D
     * objects.  The default location is (0,0).
     *
     * @param x the <i>x</i> coordinate of the upper-left corner of
     * the new location.
     * @param y the <i>y</i> coordinate of the upper-left corner of
     * the new location.
     *
     * @exception IllegalStateException if this Canvas3D is not in
     * off-screen mode.
     *
     * @since Java 3D 1.2
     */
    public void setOffScreenLocation(int x, int y) {

        if (!offScreen)
            throw new IllegalStateException(J3dI18N.getString("Canvas3D1"));

	synchronized(cvLock) {
	    offScreenCanvasLoc.setLocation(x, y);
	}
    }


    /**
     * Sets the location of this off-screen Canvas3D.  The location is
     * the upper-left corner of the Canvas3D relative to the
     * upper-left corner of the corresponding off-screen Screen3D.
     * The function of this method is similar to that of
     * <code>Component.setLocation</code> for on-screen Canvas3D
     * objects.  The default location is (0,0).
     *
     * @param p the point defining the upper-left corner of the new
     * location.
     *
     * @exception IllegalStateException if this Canvas3D is not in
     * off-screen mode.
     *
     * @since Java 3D 1.2
     */
    public void setOffScreenLocation(Point p) {

        if (!offScreen)
            throw new IllegalStateException(J3dI18N.getString("Canvas3D1"));

	synchronized(cvLock) {
	    offScreenCanvasLoc.setLocation(p);
	}
    }


    /**
     * Retrieves the location of this off-screen Canvas3D.  The
     * location is the upper-left corner of the Canvas3D relative to
     * the upper-left corner of the corresponding off-screen Screen3D.
     * The function of this method is similar to that of
     * <code>Component.getLocation</code> for on-screen Canvas3D
     * objects.
     *
     * @return a new point representing the upper-left corner of the
     * location of this off-screen Canvas3D.
     *
     * @exception IllegalStateException if this Canvas3D is not in
     * off-screen mode.
     *
     * @since Java 3D 1.2
     */
    public Point getOffScreenLocation() {
        if (!offScreen)
            throw new IllegalStateException(J3dI18N.getString("Canvas3D1"));

	return (new Point(offScreenCanvasLoc));
    }


    /**
     * Retrieves the location of this off-screen Canvas3D and stores
     * it in the specified Point object.  The location is the
     * upper-left corner of the Canvas3D relative to the upper-left
     * corner of the corresponding off-screen Screen3D.  The function
     * of this method is similar to that of
     * <code>Component.getLocation</code> for on-screen Canvas3D
     * objects. This version of <code>getOffScreenLocation</code> is
     * useful if the caller wants to avoid allocating a new Point
     * object on the heap.
     *
     * @param rv Point object into which the upper-left corner of the
     * location of this off-screen Canvas3D is copied.
     * If <code>rv</code> is null, a new Point is allocated.
     *
     * @return <code>rv</code>
     *
     * @exception IllegalStateException if this Canvas3D is not in
     * off-screen mode.
     *
     * @since Java 3D 1.2
     */
    public Point getOffScreenLocation(Point rv) {

        if (!offScreen)
            throw new IllegalStateException(J3dI18N.getString("Canvas3D1"));

	if (rv == null)
	    return (new Point(offScreenCanvasLoc));

	else {
	    rv.setLocation(offScreenCanvasLoc);
	    return rv;
	}
    }

    void endOffScreenRendering() {

        ImageComponent2DRetained icRetained = (ImageComponent2DRetained)offScreenBuffer.retained;
        boolean isByRef = icRetained.isByReference();
        ImageComponentRetained.ImageData imageData = icRetained.getImageData(false);

        if(!isByRef) {
            // If icRetained has a null image ( BufferedImage)
            if (imageData == null)  {
                assert (!isByRef);
                icRetained.createBlankImageData();
                imageData = icRetained.getImageData(false);
            }
            // Check for possible format conversion in imageData
            else {
                // Format convert imageData if format is unsupported.
                icRetained.evaluateExtensions(this);
            }
            // read the image from the offscreen buffer
            readOffScreenBuffer(ctx, icRetained.getImageFormatTypeIntValue(false),
                    icRetained.getImageDataTypeIntValue(), imageData.get(),
                    offScreenCanvasSize.width, offScreenCanvasSize.height);

        } else {
            icRetained.geomLock.getLock();
            // Create a copy of format converted image in imageData if format is unsupported.
            icRetained.evaluateExtensions(this);

            // read the image from the offscreen buffer
            readOffScreenBuffer(ctx, icRetained.getImageFormatTypeIntValue(false),
                    icRetained.getImageDataTypeIntValue(), imageData.get(),
                    offScreenCanvasSize.width, offScreenCanvasSize.height);

            // For byRef, we might have to copy buffer back into
            // the user's referenced ImageComponent2D
            if(!imageData.isDataByRef()) {
                if(icRetained.isImageTypeSupported()) {
                    icRetained.copyToRefImage(0);
                } else {
                    // This method only handle RGBA conversion.
                    icRetained.copyToRefImageWithFormatConversion(0);
                }
            }

            icRetained.geomLock.unLock();
        }
    }

    /**
     * Synchronize and swap buffers on a double buffered canvas for
     * this Canvas3D object.  This method should only be called if the
     * Java 3D renderer has been stopped.  In the normal case, the renderer
     * automatically swaps the buffer.
     * This method calls the <code>flush(true)</code> methods of the
     * associated 2D and 3D graphics contexts, if they have been allocated.
     *
     * @exception RestrictedAccessException if the Java 3D renderer is
     * running.
     * @exception IllegalStateException if this Canvas3D is in
     * off-screen mode.
     *
     * @see #stopRenderer
     * @see GraphicsContext3D#flush
     * @see J3DGraphics2D#flush
     */
    public void swap() {
	if (offScreen)
	    throw new IllegalStateException(J3dI18N.getString("Canvas3D14"));

	if (isRunning)
	    throw new RestrictedAccessException(J3dI18N.getString("Canvas3D0"));

	if (!firstPaintCalled) {
	    return;
	}

	if (view != null && graphicsContext3D != null) {
	    if ((view.universe != null) &&
		(Thread.currentThread() == view.universe.behaviorScheduler)) {
		graphicsContext3D.sendRenderMessage(false, GraphicsContext3D.SWAP, null, null);
	    } else {
		graphicsContext3D.sendRenderMessage(true, GraphicsContext3D.SWAP, null, null);
	    }
	    graphicsContext3D.runMonitor(J3dThread.WAIT);
	}
    }

    void doSwap() {

	if (firstPaintCalled && useDoubleBuffer) {
	    try {
		if (validCtx && (ctx != null) && (view != null)) {
		    synchronized (drawingSurfaceObject) {
			if (validCtx) {
			    if (!drawingSurfaceObject.renderLock()) {
				graphicsContext3D.runMonitor(J3dThread.NOTIFY);
				return;
			    }
			    this.syncRender(ctx, true);
			    swapBuffers(ctx, drawable);
			    drawingSurfaceObject.unLock();
			}
		    }
		}
	    } catch (NullPointerException ne) {
                drawingSurfaceObject.unLock();
	    }
	}
	// Increment the elapsedFrame for the behavior structure
	// to trigger any interpolators
	view.universe.behaviorStructure.incElapsedFrames();
	graphicsContext3D.runMonitor(J3dThread.NOTIFY);
    }

    /**
     * Wrapper for native createNewContext method.
     */
    Context createNewContext(Context shareCtx, boolean isSharedCtx) {
        Context retVal = createNewContext(
                this.drawable,
                shareCtx, isSharedCtx,
                this.offScreen);
        // compute the max available texture units
        maxAvailableTextureUnits = Math.max(maxTextureUnits, maxTextureImageUnits);

        return retVal;
    }

    /**
     * Make the context associated with the specified canvas current.
     */
    final void makeCtxCurrent() {
	makeCtxCurrent(ctx, drawable);
    }

    /**
     * Make the specified context current.
     */
    final void makeCtxCurrent(Context ctx) {
	makeCtxCurrent(ctx, drawable);
    }

    final void makeCtxCurrent(Context ctx, Drawable drawable) {
        if (ctx != screen.renderer.currentCtx || drawable != screen.renderer.currentDrawable) {
	    if (!drawingSurfaceObject.isLocked()) {
		drawingSurfaceObject.renderLock();
		useCtx(ctx, drawable);
		drawingSurfaceObject.unLock();
	    } else {
		useCtx(ctx, drawable);
	    }
            screen.renderer.currentCtx = ctx;
            screen.renderer.currentDrawable = drawable;
        }
    }

    // Give the pipeline a chance to release the context; the Pipeline may
    // or may not ignore this call.
    void releaseCtx() {
        if (screen.renderer.currentCtx != null) {
            boolean needLock = !drawingSurfaceObject.isLocked();
            if (needLock) {
                drawingSurfaceObject.renderLock();
            }
            if (releaseCtx(screen.renderer.currentCtx)) {
                screen.renderer.currentCtx = null;
                screen.renderer.currentDrawable = null;
            }
            if (needLock) {
                drawingSurfaceObject.unLock();
            }
        }
    }


    /**
     * Sets the position of the manual left eye in image-plate
     * coordinates.  This value determines eye placement when a head
     * tracker is not in use and the application is directly controlling
     * the eye position in image-plate coordinates.
     * In head-tracked mode or when the windowEyePointPolicy is
     * RELATIVE_TO_FIELD_OF_VIEW or RELATIVE_TO_COEXISTENCE, this value
     * is ignored.  When the
     * windowEyepointPolicy is RELATIVE_TO_WINDOW only the Z value is
     * used.
     * @param position the new manual left eye position
     */
    public void setLeftManualEyeInImagePlate(Point3d position) {

	this.leftManualEyeInImagePlate.set(position);
	synchronized(dirtyMaskLock) {
	    cvDirtyMask[0] |= EYE_IN_IMAGE_PLATE_DIRTY;
            cvDirtyMask[1] |= EYE_IN_IMAGE_PLATE_DIRTY;
	}
	redraw();
    }

    /**
     * Sets the position of the manual right eye in image-plate
     * coordinates.  This value determines eye placement when a head
     * tracker is not in use and the application is directly controlling
     * the eye position in image-plate coordinates.
     * In head-tracked mode or when the windowEyePointPolicy is
     * RELATIVE_TO_FIELD_OF_VIEW or RELATIVE_TO_COEXISTENCE, this value
     * is ignored.  When the
     * windowEyepointPolicy is RELATIVE_TO_WINDOW only the Z value is
     * used.
     * @param position the new manual right eye position
     */
    public void setRightManualEyeInImagePlate(Point3d position) {

	this.rightManualEyeInImagePlate.set(position);
	synchronized(dirtyMaskLock) {
	    cvDirtyMask[0] |= EYE_IN_IMAGE_PLATE_DIRTY;
            cvDirtyMask[1] |= EYE_IN_IMAGE_PLATE_DIRTY;
	}
	redraw();
    }

    /**
     * Retrieves the position of the user-specified, manual left eye
     * in image-plate
     * coordinates and copies that value into the object provided.
     * @param position the object that will receive the position
     */
    public void getLeftManualEyeInImagePlate(Point3d position) {
	position.set(this.leftManualEyeInImagePlate);
    }

    /**
     * Retrieves the position of the user-specified, manual right eye
     * in image-plate
     * coordinates and copies that value into the object provided.
     * @param position the object that will receive the position
     */
    public void getRightManualEyeInImagePlate(Point3d position) {
	position.set(this.rightManualEyeInImagePlate);
    }

    /**
     * Retrieves the actual position of the left eye
     * in image-plate
     * coordinates and copies that value into the object provided.
     * This value is a function of the windowEyepointPolicy, the tracking
     * enable flag, and the manual left eye position.
     * @param position the object that will receive the position
     */
    public void getLeftEyeInImagePlate(Point3d position) {
	if (canvasViewCache != null) {
	    synchronized(canvasViewCache) {
		position.set(canvasViewCache.getLeftEyeInImagePlate());
	    }
	}
	else {
	    position.set(leftManualEyeInImagePlate);
	}
    }

    /**
     * Retrieves the actual position of the right eye
     * in image-plate
     * coordinates and copies that value into the object provided.
     * This value is a function of the windowEyepointPolicy, the tracking
     * enable flag, and the manual right eye position.
     * @param position the object that will receive the position
     */
    public void getRightEyeInImagePlate(Point3d position) {
	if (canvasViewCache != null) {
	    synchronized(canvasViewCache) {
		position.set(canvasViewCache.getRightEyeInImagePlate());
	    }
	}
	else {
	    position.set(rightManualEyeInImagePlate);
	}
    }

    /**
     * Retrieves the actual position of the center eye
     * in image-plate
     * coordinates and copies that value into the object provided.
     * The center eye is the fictional eye half-way between the left and
     * right eye.
     * This value is a function of the windowEyepointPolicy, the tracking
     * enable flag, and the manual right and left eye positions.
     * @param position the object that will receive the position
     * @see #setMonoscopicViewPolicy
     */
    // XXXX: This might not make sense for field-sequential HMD.
    public void getCenterEyeInImagePlate(Point3d position) {
	if (canvasViewCache != null) {
	    synchronized(canvasViewCache) {
		position.set(canvasViewCache.getCenterEyeInImagePlate());
	    }
	}
	else {
	    Point3d cenEye = new Point3d();
	    cenEye.add(leftManualEyeInImagePlate, rightManualEyeInImagePlate);
	    cenEye.scale(0.5);
	    position.set(cenEye);
	}
    }

    /**
     * Retrieves the current ImagePlate coordinates to Virtual World
     * coordinates transform and places it into the specified object.
     * @param t the Transform3D object that will receive the
     * transform
     */
    // TODO: Document -- This will return the transform of left plate.
    public void getImagePlateToVworld(Transform3D t) {
	if (canvasViewCache != null) {
	    synchronized(canvasViewCache) {
		t.set(canvasViewCache.getImagePlateToVworld());
	    }
	}
	else {
	    t.setIdentity();
	}
    }

    /**
     * Computes the position of the specified AWT pixel value
     * in image-plate
     * coordinates and copies that value into the object provided.
     * @param x the X coordinate of the pixel relative to the upper-left
     * hand corner of the window.
     * @param y the Y coordinate of the pixel relative to the upper-left
     * hand corner of the window.
     * @param imagePlatePoint the object that will receive the position in
     * physical image plate coordinates (relative to the lower-left
     * corner of the screen).
     */
    // TODO: Document -- This transform the pixel location to the left image plate.
    public void getPixelLocationInImagePlate(int x, int y,
					     Point3d imagePlatePoint) {

	if (canvasViewCache != null) {
	    synchronized(canvasViewCache) {
		imagePlatePoint.x =
		    canvasViewCache.getWindowXInImagePlate((double)x);
		imagePlatePoint.y =
		    canvasViewCache.getWindowYInImagePlate((double)y);
		imagePlatePoint.z = 0.0;
	    }
	} else {
	    imagePlatePoint.set(0.0, 0.0, 0.0);
	}
    }


     void getPixelLocationInImagePlate(double x, double y, double z,
				       Point3d imagePlatePoint) {
	 if (canvasViewCache != null) {
	     synchronized(canvasViewCache) {
		 canvasViewCache.getPixelLocationInImagePlate(
                                       x, y, z, imagePlatePoint);
	     }
	 } else {
	     imagePlatePoint.set(0.0, 0.0, 0.0);
	 }
     }


    /**
     * Computes the position of the specified AWT pixel value
     * in image-plate
     * coordinates and copies that value into the object provided.
     * @param pixelLocation the coordinates of the pixel relative to
     * the upper-left hand corner of the window.
     * @param imagePlatePoint the object that will receive the position in
     * physical image plate coordinates (relative to the lower-left
     * corner of the screen).
     *
     * @since Java 3D 1.2
     */
    // TODO: Document -- This transform the pixel location to the left image plate.
    public void getPixelLocationInImagePlate(Point2d pixelLocation,
						   Point3d imagePlatePoint) {

	if (canvasViewCache != null) {
	    synchronized(canvasViewCache) {
		imagePlatePoint.x =
		    canvasViewCache.getWindowXInImagePlate(pixelLocation.x);
		imagePlatePoint.y =
		    canvasViewCache.getWindowYInImagePlate(pixelLocation.y);
		imagePlatePoint.z = 0.0;
	    }
	}
	else {
	    imagePlatePoint.set(0.0, 0.0, 0.0);
	}
    }


    /**
     * Projects the specified point from image plate coordinates
     * into AWT pixel coordinates.  The AWT pixel coordinates are
     * copied into the object provided.
     * @param imagePlatePoint the position in
     * physical image plate coordinates (relative to the lower-left
     * corner of the screen).
     * @param pixelLocation the object that will receive the coordinates
     * of the pixel relative to the upper-left hand corner of the window.
     *
     * @since Java 3D 1.2
     */
    // TODO: Document -- This transform the pixel location from the left image plate.
    public void getPixelLocationFromImagePlate(Point3d imagePlatePoint,
					       Point2d pixelLocation) {
 	if (canvasViewCache != null) {
 	    synchronized(canvasViewCache) {
		canvasViewCache.getPixelLocationFromImagePlate(
                           imagePlatePoint, pixelLocation);
 	    }
 	}
 	else {
 	    pixelLocation.set(0.0, 0.0);
 	}
    }

    /**
     * Copies the current Vworld projection transform for each eye
     * into the specified Transform3D objects.  This transform takes
     * points in virtual world coordinates and projects them into
     * clipping coordinates, which are in the range [-1,1] in
     * <i>X</i>, <i>Y</i>, and <i>Z</i> after clipping and perspective
     * division.
     * In monoscopic mode, the same projection transform will be
     * copied into both the right and left eye Transform3D objects.
     *
     * @param leftProjection the Transform3D object that will receive
     * a copy of the current projection transform for the left eye.
     *
     * @param rightProjection the Transform3D object that will receive
     * a copy of the current projection transform for the right eye.
     *
     * @since Java 3D 1.3
     */
    public void getVworldProjection(Transform3D leftProjection,
				    Transform3D rightProjection) {
        if (canvasViewCache != null) {
            ViewPlatformRetained viewPlatformRetained =
               (ViewPlatformRetained)view.getViewPlatform().retained;

	    synchronized(canvasViewCache) {
                leftProjection.mul(canvasViewCache.getLeftProjection(),
                        canvasViewCache.getLeftVpcToEc());
                leftProjection.mul(viewPlatformRetained.getVworldToVpc());

                // caluclate right eye if in stereo, otherwise
                // this is the same as the left eye.
                if (useStereo) {
                    rightProjection.mul(canvasViewCache.getRightProjection(),
                            canvasViewCache.getRightVpcToEc());
                    rightProjection.mul(viewPlatformRetained.getVworldToVpc());
                }
                else {
	            rightProjection.set(leftProjection);
               }
	    }
	}
        else {
 	    leftProjection.setIdentity();
 	    rightProjection.setIdentity();
	}
    }

    /**
     * Copies the inverse of the current Vworld projection transform
     * for each eye into the specified Transform3D objects.  This
     * transform takes points in clipping coordinates, which are in
     * the range [-1,1] in <i>X</i>, <i>Y</i>, and <i>Z</i> after
     * clipping and perspective division, and transforms them into
     * virtual world coordinates.
     * In monoscopic mode, the same inverse projection transform will
     * be copied into both the right and left eye Transform3D objects.
     *
     * @param leftInverseProjection the Transform3D object that will
     * receive a copy of the current inverse projection transform for
     * the left eye.
     * @param rightInverseProjection the Transform3D object that will
     * receive a copy of the current inverse projection transform for
     * the right eye.
     *
     * @since Java 3D 1.3
     */
    public void getInverseVworldProjection(Transform3D leftInverseProjection,
					   Transform3D rightInverseProjection) {
        if (canvasViewCache != null) {
            synchronized(canvasViewCache) {
                  leftInverseProjection.set(
                    canvasViewCache.getLeftCcToVworld());

                // caluclate right eye if in stereo, otherwise
                // this is the same as the left eye.
                if (useStereo) {
                  rightInverseProjection.set(
                    canvasViewCache.getRightCcToVworld());
                }
                else {
                    rightInverseProjection.set(leftInverseProjection);
                }
            }

        }
        else {
            leftInverseProjection.setIdentity();
            rightInverseProjection.setIdentity();
        }
    }


    /**
     * Retrieves the physical width of this canvas window in meters.
     * @return the physical window width in meters.
     */
    public double getPhysicalWidth() {
	double width = 0.0;

	if (canvasViewCache != null) {
	    synchronized(canvasViewCache) {
		width = canvasViewCache.getPhysicalWindowWidth();
	    }
	}

	return width;
    }

    /**
     * Retrieves the physical height of this canvas window in meters.
     * @return the physical window height in meters.
     */
    public double getPhysicalHeight() {
	double height = 0.0;

	if (canvasViewCache != null) {
	    synchronized(canvasViewCache) {
		height = canvasViewCache.getPhysicalWindowHeight();
	    }
	}

	return height;
    }

    /**
     * Retrieves the current Virtual World coordinates to ImagePlate
     * coordinates transform and places it into the specified object.
     * @param t the Transform3D object that will receive the
     * transform
     */
    // TODO: Document -- This will return the transform of left plate.
    public void getVworldToImagePlate(Transform3D t) {
	if (canvasViewCache != null) {
	    synchronized(canvasViewCache) {
		t.set(canvasViewCache.getVworldToImagePlate());
	    }
	}
	else {
	    t.setIdentity();
	}
    }

     void getLastVworldToImagePlate(Transform3D t) {
	if (canvasViewCache != null) {
	    synchronized(canvasViewCache) {
		t.set(canvasViewCache.getLastVworldToImagePlate());
	    }
	}
	else {
	    t.setIdentity();
	}
    }

    /**
     * Sets view that points to this Canvas3D.
     * @param view view object that points to this Canvas3D
     */
    void setView(View view) {
	pendingView = view;

	// We can't set View directly here in user thread since
	// other threads may using canvas.view
	// e.g. In Renderer, we use canvas3d.view.inCallBack
	// before and after postSwap(), if view change in between
	// than view.inCallBack may never reset to false.
	VirtualUniverse.mc.postRequest(MasterControl.SET_VIEW, this);
	evaluateActive();
    }

    void computeViewCache() {
	synchronized(cvLock) {
	    if (view == null) {
		canvasViewCache = null;
                canvasViewCacheFrustum = null;
	    } else {

		canvasViewCache = new CanvasViewCache(this,
						      screen.screenViewCache,
                                                      view.viewCache);
                // Issue 109 : construct a separate canvasViewCache for
                // computing view frustum
		canvasViewCacheFrustum = new CanvasViewCache(this,
						      screen.screenViewCache,
                                                      view.viewCache);
		synchronized (dirtyMaskLock) {
                    cvDirtyMask[0] = VIEW_INFO_DIRTY;
                    cvDirtyMask[1] = VIEW_INFO_DIRTY;
		}
	    }
	}
    }

    /**
     * Gets view that points to this Canvas3D.
     * @return view object that points to this Canvas3D
     */
    public View getView() {
	return pendingView;
    }

    /**
     * Returns a status flag indicating whether or not stereo
     * is available.
     * This is equivalent to:
     * <ul>
     * <code>
     * ((Boolean)queryProperties().
     * get("stereoAvailable")).
     * booleanValue()
     * </code>
     * </ul>
     *
     * @return a flag indicating whether stereo is available
     */
    public boolean getStereoAvailable() {
	return ((Boolean)queryProperties().get("stereoAvailable")).
	    booleanValue();
    }

    /**
     * Turns stereo on or off.  Note that this attribute is used
     * only when stereo is available.  Enabling stereo on a Canvas3D
     * that does not support stereo has no effect.
     * @param flag enables or disables the display of stereo
     *
     * @see #queryProperties
     */
    public void setStereoEnable(boolean flag) {
	stereoEnable = flag;
        useStereo = stereoEnable && stereoAvailable;
	synchronized(dirtyMaskLock) {
	    cvDirtyMask[0] |= STEREO_DIRTY;
            cvDirtyMask[1] |= STEREO_DIRTY;
	}
	redraw();
    }

    /**
     * Returns a status flag indicating whether or not stereo
     * is enabled.
     * @return a flag indicating whether stereo is enabled
     */
    public boolean getStereoEnable() {
	return this.stereoEnable;
    }


    /**
     * Specifies how Java 3D generates monoscopic view. If set to
     * View.LEFT_EYE_VIEW, the view generated corresponds to the view as
     * seen from the left eye. If set to View.RIGHT_EYE_VIEW, the view
     * generated corresponds to the view as seen from the right
     * eye. If set to View.CYCLOPEAN_EYE_VIEW, the view generated
     * corresponds to the view as seen from the 'center eye', the
     * fictional eye half-way between the left and right eye.  The
     * default monoscopic view policy is View.CYCLOPEAN_EYE_VIEW.
     * <p>
     * NOTE: for backward compatibility with Java 3D 1.1, if this
     * attribute is set to its default value of
     * View.CYCLOPEAN_EYE_VIEW, the monoscopic view policy in the
     * View object will be used.  An application should not use both
     * the deprecated View method and this Canvas3D method at the same
     * time.
     * @param policy one of View.LEFT_EYE_VIEW, View.RIGHT_EYE_VIEW, or
     * View.CYCLOPEAN_EYE_VIEW.
     *
     * @exception IllegalStateException if the specified
     * policy is CYCLOPEAN_EYE_VIEW, the canvas is a stereo canvas,
     * and the viewPolicy for the associated view is HMD_VIEW
     *
     * @since Java 3D 1.2
     */
    public void setMonoscopicViewPolicy(int policy) {


	if((view !=null) && (view.viewPolicy == View.HMD_VIEW) &&
	   (monoscopicViewPolicy == View.CYCLOPEAN_EYE_VIEW) &&
	   (!useStereo)) {
	    throw new
		IllegalStateException(J3dI18N.getString("View31"));
	}

	monoscopicViewPolicy = policy;
	synchronized(dirtyMaskLock) {
            cvDirtyMask[0] |= MONOSCOPIC_VIEW_POLICY_DIRTY;
            cvDirtyMask[1] |= MONOSCOPIC_VIEW_POLICY_DIRTY;
	}
	redraw();
    }


    /**
     * Returns policy on how Java 3D generates monoscopic view.
     * @return policy one of View.LEFT_EYE_VIEW, View.RIGHT_EYE_VIEW or
     * View.CYCLOPEAN_EYE_VIEW.
     *
     * @since Java 3D 1.2
     */
    public int getMonoscopicViewPolicy() {
	return this.monoscopicViewPolicy;
    }


    /**
     * Returns a status flag indicating whether or not double
     * buffering is available.
     * This is equivalent to:
     * <ul>
     * <code>
     * ((Boolean)queryProperties().
     * get("doubleBufferAvailable")).
     * booleanValue()
     * </code>
     * </ul>
     *
     * @return a flag indicating whether double buffering is available.
     */
    public boolean getDoubleBufferAvailable() {
        return ((Boolean)queryProperties().get("doubleBufferAvailable")).
	    booleanValue();
    }

    /**
     * Turns double buffering on or off.  If double buffering
     * is off, all drawing is to the front buffer and no buffer swap
     * is done between frames. It should be stressed that running
     * Java 3D with double buffering disabled is not recommended.
     * Enabling double buffering on a Canvas3D
     * that does not support double buffering has no effect.
     *
     * @param flag enables or disables double buffering.
     *
     * @see #queryProperties
     */
    public void setDoubleBufferEnable(boolean flag) {
        doubleBufferEnable = flag;
        useDoubleBuffer = doubleBufferEnable && doubleBufferAvailable;
        if (Thread.currentThread() == screen.renderer) {
           setRenderMode(ctx, FIELD_ALL, useDoubleBuffer);
        }
	redraw();
    }

    /**
     * Returns a status flag indicating whether or not double
     * buffering is enabled.
     * @return a flag indicating if double buffering is enabled.
     */
    public boolean getDoubleBufferEnable() {
	return doubleBufferEnable;
    }

    /**
     * Returns a status flag indicating whether or not scene
     * antialiasing is available.
     * This is equivalent to:
     * <ul>
     * <code>
     * ((Boolean)queryProperties().
     * get("sceneAntialiasingAvailable")).
     * booleanValue()
     * </code>
     * </ul>
     *
     * @return a flag indicating whether scene antialiasing is available.
     */
    public boolean getSceneAntialiasingAvailable() {
	return ((Boolean)queryProperties().get("sceneAntialiasingAvailable")).
	    booleanValue();
    }


    /**
     * Returns a flag indicating whether or not the specified shading
     * language is supported. A ShaderError will be generated if an
     * unsupported shading language is used.
     *
     * @param shadingLanguage the shading language being queried, one of:
     * <code>Shader.SHADING_LANGUAGE_GLSL</code> or
     * <code>Shader.SHADING_LANGUAGE_CG</code>.
     *
     * @return true if the specified shading language is supported,
     * false otherwise.
     *
     * @since Java 3D 1.4
     */
    public boolean isShadingLanguageSupported(int shadingLanguage) {
        // Call queryProperties to ensure that the shading language flags are valid
        queryProperties();

		if (shadingLanguage == Shader.SHADING_LANGUAGE_GLSL)
			return shadingLanguageGLSL;

		return false;
	}


    /**
     * Returns a read-only Map object containing key-value pairs that define
     * various properties for this Canvas3D.  All of the keys are
     * String objects.  The values are key-specific, but most will be
     * Boolean, Integer, Float, Double, or String objects.
     *
     * <p>
     * The currently defined keys are:
     *
     * <p>
     * <ul>
     * <table BORDER=1 CELLSPACING=1 CELLPADDING=1>
     * <tr>
     * <td><b>Key (String)</b></td>
     * <td><b>Value Type</b></td>
     * </tr>
     * <tr>
     * <td><code>shadingLanguageCg</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>shadingLanguageGLSL</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>doubleBufferAvailable</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>stereoAvailable</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>sceneAntialiasingAvailable</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>sceneAntialiasingNumPasses</code></td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td><code>stencilSize</code></td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td><code>texture3DAvailable</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>textureColorTableSize</code></td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td><code>textureLodRangeAvailable</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>textureLodOffsetAvailable</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>textureWidthMax</code></td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td><code>textureHeightMax</code></td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td><code>textureBoundaryWidthMax</code></td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td><code>textureEnvCombineAvailable</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>textureCombineDot3Available</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>textureCombineSubtractAvailable</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>textureCoordSetsMax</code></td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td><code>textureUnitStateMax</code></td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td><code>textureImageUnitsMax</code></td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td><code>textureImageUnitsVertexMax</code></td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td><code>textureImageUnitsCombinedMax</code></td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td><code>textureCubeMapAvailable</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>textureDetailAvailable</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>textureSharpenAvailable</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>textureFilter4Available</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>textureAnisotropicFilterDegreeMax</code></td>
     * <td>Float</td>
     * </tr>
     * <tr>
     * <td><code>textureNonPowerOfTwoAvailable</code></td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td><code>vertexAttrsMax</code></td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td><code>compressedGeometry.majorVersionNumber</code></td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td><code>compressedGeometry.minorVersionNumber</code></td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td><code>compressedGeometry.minorMinorVersionNumber</code></td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td><code>native.version</code></td>
     * <td>String</td>
     * </tr>
     * </table>
     * </ul>
     *
     * <p>
     * The descriptions of the values returned for each key are as follows:
     *
     * <p>
     * <ul>
     * <li>
     * <code>shadingLanguageCg</code>
     * <ul>
     * A Boolean indicating whether or not Cg shading Language
     * is available for this Canvas3D.
     * </ul>
     * </li>
     *
     * <li>
     * <code>shadingLanguageGLSL</code>
     * <ul>
     * A Boolean indicating whether or not GLSL shading Language
     * is available for this Canvas3D.
     * </ul>
     * </li>
     *
     * <li>
     * <code>doubleBufferAvailable</code>
     * <ul>
     * A Boolean indicating whether or not double buffering
     * is available for this Canvas3D.  This is equivalent to
     * the getDoubleBufferAvailable method.  If this flag is false,
     * the Canvas3D will be rendered in single buffer mode; requests
     * to enable double buffering will be ignored.
     * </ul>
     * </li>
     *
     * <li>
     * <code>stereoAvailable</code>
     * <ul>
     * A Boolean indicating whether or not stereo
     * is available for this Canvas3D.  This is equivalent to
     * the getStereoAvailable method.  If this flag is false,
     * the Canvas3D will be rendered in monoscopic mode; requests
     * to enable stereo will be ignored.
     * </ul>
     * </li>
     *
     * <li>
     * <code>sceneAntialiasingAvailable</code>
     * <ul>
     * A Boolean indicating whether or not scene antialiasing
     * is available for this Canvas3D.  This is equivalent to
     * the getSceneAntialiasingAvailable method.  If this flag is false,
     * requests to enable scene antialiasing will be ignored.
     * </ul>
     * </li>
     *
     * <li>
     * <code>sceneAntialiasingNumPasses</code>
     * <ul>
     * An Integer indicating the number of passes scene antialiasing
     * requires to render a single frame for this Canvas3D.
     * If this value is zero, scene antialiasing is not supported.
     * If this value is one, multisampling antialiasing is used.
     * Otherwise, the number indicates the number of rendering passes
     * needed.
     * </ul>
     * </li>
     *
     * <li>
     * <code>stencilSize</code>
     * <ul>
     * An Integer indicating the number of stencil bits that are available
     * for this Canvas3D.
     * </ul>
     * </li>
     *
     * <li>
     * <code>texture3DAvailable</code>
     * <ul>
     * A Boolean indicating whether or not 3D Texture mapping
     * is available for this Canvas3D.  If this flag is false,
     * 3D texture mapping is either not supported by the underlying
     * rendering layer or is otherwise unavailable for this
     * particular Canvas3D.  All use of 3D texture mapping will be
     * ignored in this case.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureColorTableSize</code>
     * <ul>
     * An Integer indicating the maximum size of the texture color
     * table for this Canvas3D.  If the size is 0, the texture
     * color table is either not supported by the underlying rendering
     * layer or is otherwise unavailable for this particular
     * Canvas3D.  An attempt to use a texture color table larger than
     * textureColorTableSize will be ignored; no color lookup will be
     * performed.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureLodRangeAvailable</code>
     * <ul>
     * A Boolean indicating whether or not setting only a subset of mipmap
     * levels and setting a range of texture LOD are available for this
     * Canvas3D.
     * If it indicates false, setting a subset of mipmap levels and
     * setting a texture LOD range are not supported by the underlying
     * rendering layer, and an attempt to set base level, or maximum level,
     * or minimum LOD, or maximum LOD will be ignored. In this case,
     * images for all mipmap levels must be defined for the texture to be
     * valid.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureLodOffsetAvailable</code>
     * <ul>
     * A Boolean indicating whether or not setting texture LOD offset is
     * available for this Canvas3D. If it indicates false, setting
     * texture LOD offset is not supported by the underlying rendering
     * layer, and an attempt to set the texture LOD offset will be ignored.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureWidthMax</code>
     * <ul>
     * An Integer indicating the maximum texture width supported by
     * this Canvas3D. If the width of a texture exceeds the maximum texture
     * width for a Canvas3D, then the texture will be effectively disabled
     * for that Canvas3D.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureHeightMax</code>
     * <ul>
     * An Integer indicating the maximum texture height supported by
     * this Canvas3D. If the height of a texture exceeds the maximum texture
     * height for a Canvas3D, then the texture will be effectively disabled
     * for that Canvas3D.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureBoundaryWidthMax</code>
     * <ul>
     * An Integer indicating the maximum texture boundary width
     * supported by the underlying rendering layer for this Canvas3D. If
     * the maximum supported texture boundary width is 0, then texture
     * boundary is not supported by the underlying rendering layer.
     * An attempt to specify a texture boundary width > the
     * textureBoundaryWidthMax will effectively disable the texture.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureEnvCombineAvailable</code>
     * <ul>
     * A Boolean indicating whether or not texture environment combine
     * operation is supported for this Canvas3D. If it indicates false,
     * then texture environment combine is not supported by the
     * underlying rendering layer, and an attempt to specify COMBINE
     * as the texture mode will be ignored. The texture mode in effect
     * will be REPLACE.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureCombineDot3Available</code>
     * <ul>
     * A Boolean indicating whether or not texture combine mode
     * COMBINE_DOT3 is
     * supported for this Canvas3D. If it indicates false, then
     * texture combine mode COMBINE_DOT3 is not supported by
     * the underlying rendering layer, and an attempt to specify
     * COMBINE_DOT3 as the texture combine mode will be ignored.
     * The texture combine mode in effect will be COMBINE_REPLACE.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureCombineSubtractAvailable</code>
     * <ul>
     * A Boolean indicating whether or not texture combine mode
     * COMBINE_SUBTRACT is
     * supported for this Canvas3D. If it indicates false, then
     * texture combine mode COMBINE_SUBTRACT is not supported by
     * the underlying rendering layer, and an attempt to specify
     * COMBINE_SUBTRACT as the texture combine mode will be ignored.
     * The texture combine mode in effect will be COMBINE_REPLACE.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureCoordSetsMax</code>
     * <ul>
     * An Integer indicating the maximum number of texture coordinate sets
     * supported by the underlying rendering layer.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureUnitStateMax</code>
     * <ul>
     * An Integer indicating the maximum number of fixed-function texture units
     * supported by the underlying rendering layer. If the number of
     * application-sepcified texture unit states exceeds the maximum number
     * for a Canvas3D, and the fixed-function rendering pipeline is used, then
     * the texture will be effectively disabled for that Canvas3D.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureImageUnitsMax</code>
     * <ul>
     * An Integer indicating the maximum number of texture image units
     * that can be accessed by the fragment shader when programmable shaders
     * are used.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureImageUnitsVertexMax</code>
     * <ul>
     * An Integer indicating the maximum number of texture image units
     * that can be accessed by the vertex shader when programmable shaders
     * are used.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureImageUnitsCombinedMax</code>
     * <ul>
     * An Integer indicating the combined maximum number of texture image units
     * that can be accessed by the vertex shader and the fragment shader when
     * programmable shaders are used.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureCubeMapAvailable</code>
     * <ul>
     * A Boolean indicating whether or not texture cube map is supported
     * for this Canvas3D. If it indicates false, then texture cube map
     * is not supported by the underlying rendering layer, and an attempt
     * to specify NORMAL_MAP or REFLECTION_MAP as the texture generation
     * mode will be ignored. The texture generation mode in effect will
     * be SPHERE_MAP.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureDetailAvailable</code>
     * <ul>
     * A Boolean indicating whether or not detail texture is supported
     * for this Canvas3D. If it indicates false, then detail texture is
     * not supported by the underlying rendering layer, and an attempt
     * to specify LINEAR_DETAIL, LINEAR_DETAIL_ALPHA or
     * LINEAR_DETAIL_RGB as the texture magnification filter mode will
     * be ignored. The texture magnification filter mode in effect will
     * be BASE_LEVEL_LINEAR.
     * As of Java 3D 1.5, this property is always false.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureSharpenAvailable</code>
     * <ul>
     * A Boolean indicating whether or not sharpen texture is supported
     * for this Canvas3D. If it indicates false, then sharpen texture
     * is not supported by the underlying rendering layer, and an attempt
     * to specify LINEAR_SHARPEN, LINEAR_SHARPEN_ALPHA or
     * LINEAR_SHARPEN_RGB as the texture magnification filter mode
     * will be ignored. The texture magnification filter mode in effect
     * will be BASE_LEVEL_LINEAR.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureFilter4Available</code>
     * <ul>
     * A Boolean indicating whether or not filter4 is supported for this
     * Canvas3D. If it indicates flase, then filter4 is not supported
     * by the underlying rendering layer, and an attempt to specify
     * FILTER_4 as the texture minification filter mode or texture
     * magnification filter mode will be ignored. The texture filter mode
     * in effect will be BASE_LEVEL_LINEAR.
     * </ul>
     * </li>
     *
     * <li>
     * <code>textureAnisotropicFilterDegreeMax</code>
     * <ul>
     * A Float indicating the maximum degree of anisotropic filter
     * available for this Canvas3D. If it indicates 1.0, setting
     * anisotropic filter is not supported by the underlying rendering
     * layer, and an attempt to set anisotropic filter degree will be ignored.
     * </ul>
     * </li>

     * <li>
     * <code>textureNonPowerOfTwoAvailable</code>
     * <ul>
     * A Boolean indicating whether or not texture dimensions that are
     * not powers of two are supported for
     * for this Canvas3D. If it indicates false, then textures with
     * non power of two sizes will be ignored. Set the property
     * j3d.textureEnforcePowerOfTwo to revert to the pre-1.5 behavior
     * of throwing exceptions for non power of two textures.
     * </ul>
     * </li>
     *
     * <li>
     * <code>vertexAttrsMax</code>
     * <ul>
     * An Integer indicating the maximum number of vertex attributes
     * supported by the underlying rendering layer. This is in addition to
     * the vertex coordinate (position), color, normal, and so forth.
     * </ul>
     * </li>
     *
     * <li>
     * <code>compressedGeometry.majorVersionNumber</code><br>
     * <code>compressedGeometry.minorVersionNumber</code><br>
     * <code>compressedGeometry.minorMinorVersionNumber</code>
     * <ul>
     * Integers indicating the major, minor, and minor-minor
     * version numbers, respectively, of the version of compressed
     * geometry supported by this version of Java 3D.
     * </ul>
     * </li>
     *
     * <li>
     * <code>native.version</code>
     * <ul>
     * A String indicating the version number of the native graphics
     * library.  The format of this string is defined by the native
     * library.
     * </ul>
     * </li>
     * </ul>
     *
     * @return the properties of this Canavs3D
     *
     * @since Java 3D 1.2
     */
    public final Map queryProperties() {
	if (queryProps == null) {
	    boolean createDummyCtx = false;

	    synchronized (VirtualUniverse.mc.contextCreationLock) {
		if (ctx == null) {
		    createDummyCtx = true;
		}
	    }

	    if (createDummyCtx) {
		GraphicsConfigTemplate3D.setQueryProps(this);
	    }

	    //create query Properties
	    createQueryProps();
	}

        if (fatalError) {
            throw new IllegalStateException(J3dI18N.getString("Canvas3D29"));
        }

  	return queryProps;
    }

    void createQueryContext() {
	// create a dummy context to query for support of certain
	// extensions, the context will destroy immediately
	// inside the native code after setting the various
	// fields in this object
	createQueryContext(drawable, offScreen, 1, 1);
        // compute the max available texture units
        maxAvailableTextureUnits = Math.max(maxTextureUnits, maxTextureImageUnits);
    }

    /**
     * Creates the query properties for this Canvas.
     */
    private void createQueryProps() {
	// Create lists of keys and values
	ArrayList<String> keys = new ArrayList<String>();
	ArrayList<Object> values = new ArrayList<Object>();
	int pass = 0;

	// properties not associated with graphics context
	keys.add("doubleBufferAvailable");
	values.add(new Boolean(doubleBufferAvailable));

	keys.add("stereoAvailable");
	values.add(new Boolean(stereoAvailable));

	keys.add("sceneAntialiasingAvailable");
	values.add(new Boolean(sceneAntialiasingAvailable));

	keys.add("sceneAntialiasingNumPasses");

	if (sceneAntialiasingAvailable) {
	    pass = (sceneAntialiasingMultiSamplesAvailable ?
		    1: Renderer.NUM_ACCUMULATION_SAMPLES);
	}
	values.add(new Integer(pass));

	keys.add("stencilSize");
	// Return the actual stencil size if the user owns it, otherwise
        // return 0
        if (userStencilAvailable) {
            values.add(new Integer(actualStencilSize));
        } else {
            values.add(new Integer(0));
        }

        keys.add("compressedGeometry.majorVersionNumber");
	values.add(new Integer(GeometryDecompressor.majorVersionNumber));
	keys.add("compressedGeometry.minorVersionNumber");
	values.add(new Integer(GeometryDecompressor.minorVersionNumber));
	keys.add("compressedGeometry.minorMinorVersionNumber");
	values.add(new Integer(GeometryDecompressor.minorMinorVersionNumber));

	// Properties associated with graphics context
	keys.add("texture3DAvailable");
	values.add(new Boolean((textureExtendedFeatures & TEXTURE_3D) != 0));

	keys.add("textureColorTableSize");
	values.add(new Integer(textureColorTableSize));

	keys.add("textureEnvCombineAvailable");
	values.add(new Boolean(
		(textureExtendedFeatures & TEXTURE_COMBINE) != 0));

	keys.add("textureCombineDot3Available");
	values.add(new Boolean(
		(textureExtendedFeatures & TEXTURE_COMBINE_DOT3) != 0));

	keys.add("textureCombineSubtractAvailable");
	values.add(new Boolean(
		(textureExtendedFeatures & TEXTURE_COMBINE_SUBTRACT) != 0));

	keys.add("textureCubeMapAvailable");
	values.add(new Boolean(
		(textureExtendedFeatures & TEXTURE_CUBE_MAP) != 0));

        keys.add("textureSharpenAvailable");
        values.add(new Boolean(
		(textureExtendedFeatures & TEXTURE_SHARPEN) != 0));

        keys.add("textureDetailAvailable");
        values.add(new Boolean(
		(textureExtendedFeatures & TEXTURE_DETAIL) != 0));

        keys.add("textureFilter4Available");
        values.add(new Boolean(
		(textureExtendedFeatures & TEXTURE_FILTER4) != 0));

        keys.add("textureAnisotropicFilterDegreeMax");
        values.add(new Float(anisotropicDegreeMax));

        keys.add("textureWidthMax");
        values.add(new Integer(textureWidthMax));

        keys.add("textureHeightMax");
        values.add(new Integer(textureHeightMax));

        keys.add("texture3DWidthMax");
        values.add(new Integer(texture3DWidthMax));

        keys.add("texture3DHeightMax");
        values.add(new Integer(texture3DHeightMax));

        keys.add("texture3DDepthMax");
        values.add(new Integer(texture3DDepthMax));

        keys.add("textureBoundaryWidthMax");
        values.add(new Integer(textureBoundaryWidthMax));

        keys.add("textureLodRangeAvailable");
        values.add(new Boolean(
		(textureExtendedFeatures & TEXTURE_LOD_RANGE) != 0));

        keys.add("textureLodOffsetAvailable");
        values.add(new Boolean(
		(textureExtendedFeatures & TEXTURE_LOD_OFFSET) != 0));

        keys.add("textureNonPowerOfTwoAvailable");
        values.add(new Boolean(
                (textureExtendedFeatures & TEXTURE_NON_POWER_OF_TWO) != 0));

        keys.add("textureAutoMipMapGenerationAvailable");
        values.add(new Boolean(
                (textureExtendedFeatures & TEXTURE_AUTO_MIPMAP_GENERATION) != 0));

        keys.add("textureCoordSetsMax");
        values.add(new Integer(maxTexCoordSets));

        keys.add("textureUnitStateMax");
        values.add(new Integer(maxTextureUnits));

        keys.add("textureImageUnitsMax");
        values.add(new Integer(maxTextureImageUnits));

        keys.add("textureImageUnitsVertexMax");
        values.add(new Integer(maxVertexTextureImageUnits));

        keys.add("textureImageUnitsCombinedMax");
        values.add(new Integer(maxCombinedTextureImageUnits));

        keys.add("vertexAttrsMax");
        values.add(new Integer(maxVertexAttrs));

	keys.add("shadingLanguageGLSL");
	values.add(new Boolean(shadingLanguageGLSL));

	keys.add("native.version");
	values.add(nativeGraphicsVersion);

	keys.add("native.vendor");
	values.add(nativeGraphicsVendor);

	keys.add("native.renderer");
	values.add(nativeGraphicsRenderer);

	// Now Create read-only properties object
	queryProps = new J3dQueryProps(keys, values);
    }


    /**
     * Update the view cache associated with this canvas.
     */
    void updateViewCache(boolean flag, CanvasViewCache cvc,
		BoundingBox frustumBBox, boolean doInfinite) {

        assert cvc == null;
	synchronized(cvLock) {
            if (firstPaintCalled && (canvasViewCache != null)) {
                assert canvasViewCacheFrustum != null;
                // Issue 109 : choose the appropriate cvCache
                if (frustumBBox != null) {
                    canvasViewCacheFrustum.snapshot(true);
                    canvasViewCacheFrustum.computeDerivedData(flag, null,
                            frustumBBox, doInfinite);
                } else {
                    canvasViewCache.snapshot(false);
                    canvasViewCache.computeDerivedData(flag, null,
                            null, doInfinite);
                }
            }
	}
    }

    /**
     * Set depthBufferWriteEnableOverride flag
     */
    void setDepthBufferWriteEnableOverride(boolean flag) {
	depthBufferWriteEnableOverride = flag;
    }

    /**
     * Set depthBufferEnableOverride flag
     */
    void setDepthBufferEnableOverride(boolean flag) {
        depthBufferEnableOverride = flag;
    }

    // Static initializer for Canvas3D class
    static {
	VirtualUniverse.loadLibraries();
    }


    void resetTexture(Context ctx, int texUnitIndex) {
	// D3D also need to reset texture attributes
	this.resetTextureNative(ctx, texUnitIndex);

 	if (texUnitIndex < 0) {
	    texUnitIndex = 0;
	}
	texUnitState[texUnitIndex].mirror = null;
	texUnitState[texUnitIndex].texture = null;
    }

// reset all attributes so that everything e.g. display list,
// texture will recreate again in the next frame
void resetRendering() {
	reset();

	synchronized (dirtyMaskLock) {
		cvDirtyMask[0] |= VIEW_INFO_DIRTY;
		cvDirtyMask[1] |= VIEW_INFO_DIRTY;
	}

}

    void reset() {
	int i;
	currentAppear = new AppearanceRetained();
	currentMaterial = new MaterialRetained();
	viewFrustum = new CachedFrustum();
	canvasDirty = 0xffff;
	lightBin = null;
	environmentSet = null;
	attributeBin = null;
        shaderBin = null;
	textureBin = null;
	renderMolecule = null;
	polygonAttributes = null;
	lineAttributes = null;
	pointAttributes = null;
	material = null;
	enableLighting = false;
	transparency = null;
	coloringAttributes = null;
	shaderProgram = null;
	texture = null;
	texAttrs = null;
	if (texUnitState != null) {
	    TextureUnitStateRetained tus;
	    for (i=0; i < texUnitState.length; i++) {
		tus = texUnitState[i];
		if (tus != null) {
		    tus.texAttrs = null;
		    tus.texGen = null;
		}
	    }
	}
	texCoordGeneration = null;
	renderingAttrs = null;
	appearance = null;
	appHandle = null;
	dirtyRenderMoleculeList.clear();
	displayListResourceFreeList.clear();

	dirtyDlistPerRinfoList.clear();
	textureIdResourceFreeList.clear();

	lightChanged = true;
	modelMatrix = null;
	modelClip = null;
	fog = null;
	texLinearMode = false;
	sceneAmbient = new Color3f();


	for (i=0; i< frameCount.length;i++) {
	    frameCount[i] = -1;
	}

	for (i=0; i < lights.length; i++) {
	    lights[i] = null;
	}

	if (currentLights != null) {
	    for (i=0; i < currentLights.length; i++) {
		currentLights[i] = null;
	    }
	}

	enableMask = -1;
	stateUpdateMask = 0;
	depthBufferWriteEnableOverride = false;
	depthBufferEnableOverride = false;
	depthBufferWriteEnable = true;
	vfPlanesValid = false;
	lightChanged = false;

	for (i=0; i < curStateToUpdate.length; i++) {
	    curStateToUpdate[i] = null;
	}

        // Issue 362 - need to reset display lists and ctxTimeStamp in this
        // method, so that display lists  will be recreated when canvas is
        // removed from a view and then added back into a view with another
        // canvas
	needToRebuildDisplayList = true;
	ctxTimeStamp = VirtualUniverse.mc.getContextTimeStamp();
    }


void resetImmediateRendering() {
	canvasDirty = 0xffff;
	ra = null;

	setSceneAmbient(ctx, 0.0f, 0.0f, 0.0f);
	disableFog(ctx);
	resetRenderingAttributes(ctx, false, false);

	resetTexture(ctx, -1);
	resetTexCoordGeneration(ctx);
	resetTextureAttributes(ctx);
	texUnitState[0].texAttrs = null;
	texUnitState[0].texGen = null;

	resetPolygonAttributes(ctx);
	resetLineAttributes(ctx);
	resetPointAttributes(ctx);
	resetTransparency(ctx,
			  RenderMolecule.SURFACE,
			  PolygonAttributes.POLYGON_FILL,
			  false, false);
	resetColoringAttributes(ctx,
				1.0f, 1.0f,
				1.0f, 1.0f, false);
	updateMaterial(ctx, 1.0f, 1.0f, 1.0f, 1.0f);
	resetRendering();
	makeCtxCurrent();
        synchronized (dirtyMaskLock) {
            cvDirtyMask[0] |= VIEW_INFO_DIRTY;
            cvDirtyMask[1] |= VIEW_INFO_DIRTY;
        }
	needToRebuildDisplayList = true;

	ctxTimeStamp = VirtualUniverse.mc.getContextTimeStamp();
}


    // overide Canvas.getSize()
    public Dimension getSize() {
	if (!fullScreenMode) {
	    return super.getSize();
	} else {
	    return new Dimension(fullscreenWidth, fullscreenHeight);
	}
    }

    public Dimension getSize(Dimension rv) {
	if (!fullScreenMode) {
	    return super.getSize(rv);
	} else {
	    if (rv == null) {
		return new Dimension(fullscreenWidth, fullscreenHeight);
	    } else {
		rv.setSize(fullscreenWidth, fullscreenHeight);
		return rv;
	    }
	}
    }

    public Point getLocationOnScreen() {
	if (!fullScreenMode) {
	    try {
		return super.getLocationOnScreen();
	    } catch (IllegalComponentStateException e) {}
	}
	return new Point();
    }

    public int getX() {
	if (!fullScreenMode) {
	    return super.getX();
	} else {
	    return 0;
	}
    }


    public int getY() {
	if (!fullScreenMode) {
	    return super.getY();
	} else {
	    return 0;
	}
    }

    public int getWidth() {
	if (!fullScreenMode) {
	    return super.getWidth();
	} else {
	    return screen.screenSize.width;
	}
    }

    public int getHeight() {
	if (!fullScreenMode) {
	    return super.getHeight();
	} else {
	    return screen.screenSize.height;
	}
    }

    public Point getLocation(Point rv) {
	if (!fullScreenMode) {
	    return super.getLocation(rv);
	} else {
	    if (rv != null) {
		rv.setLocation(0, 0);
		return rv;
	    } else {
		return new Point();
	    }
	}
    }

    public Point getLocation() {
	if (!fullScreenMode) {
	    return super.getLocation();
	} else {
	    return new Point();
	}
    }

    public Rectangle getBounds() {
	if (!fullScreenMode) {
	    return super.getBounds();
	} else {
	    return new Rectangle(0, 0,
				 screen.screenSize.width,
				 screen.screenSize.height);
	}
    }

    public Rectangle getBounds(Rectangle rv) {
	if (!fullScreenMode) {
	    return super.getBounds(rv);
	} else {
	    if (rv != null) {
		rv.setBounds(0, 0,
			     screen.screenSize.width,
			     screen.screenSize.height);
		return rv;
	    } else {
		return new Rectangle(0, 0,
				     screen.screenSize.width,
				     screen.screenSize.height);
	    }
	}
    }

    void setProjectionMatrix(Context ctx, Transform3D projTrans) {
       this.projTrans = projTrans;
       setProjectionMatrix(ctx, projTrans.mat);
    }

    void setModelViewMatrix(Context ctx, double[] viewMatrix, Transform3D mTrans) {
	setModelViewMatrix(ctx, viewMatrix, mTrans.mat);
	if (!useStereo) {
	    this.modelMatrix = mTrans;
	} else {
            // TODO : This seems wrong to do only for the right eye.
            // A possible approach is to invalidate the cache at begin of
            // each eye.
	    if (rightStereoPass) {
		//  Only set cache in right stereo pass, otherwise
		//  if the left stereo pass set the cache value,
		//  setModelViewMatrix() in right stereo pass will not
		//  perform in RenderMolecules.
		this.modelMatrix = mTrans;
	    }
	}
    }

    void setDepthBufferWriteEnable(boolean mode) {
        depthBufferWriteEnable = mode;
        setDepthBufferWriteEnable(ctx, mode);
    }

    void setNumActiveTexUnit(int n) {
	numActiveTexUnit = n;
    }

    int getNumActiveTexUnit() {
	return numActiveTexUnit;
    }

    void setLastActiveTexUnit(int n) {
	lastActiveTexUnit = n;
    }

    int getLastActiveTexUnit() {
	return lastActiveTexUnit;
    }

    // Create the texture state array
    void createTexUnitState() {
        texUnitState = new TextureUnitStateRetained[maxAvailableTextureUnits];
        for (int t = 0; t < maxAvailableTextureUnits; t++) {
            texUnitState[t] = new TextureUnitStateRetained();
            texUnitState[t].texture = null;
            texUnitState[t].mirror = null;
        }
    }

    boolean supportGlobalAlpha() {
	return ((extensionsSupported & SUN_GLOBAL_ALPHA) != 0);
    }

    /**
     * Enable separate specular color if it is not overriden by the
     * property j3d.disableSeparateSpecular.
     */
    void enableSeparateSpecularColor() {
        boolean enable = !VirtualUniverse.mc.disableSeparateSpecularColor;
        updateSeparateSpecularColorEnable(ctx, enable);
    }

    // Send a createOffScreenBuffer message to Renderer (via
    // MasterControl) and wait for it to be done
    private void sendCreateOffScreenBuffer() {
	// Wait for the buffer to be created unless called from
	// a Behavior or from a Rendering thread
	if (!(Thread.currentThread() instanceof BehaviorScheduler) &&
	    !(Thread.currentThread() instanceof Renderer)) {

	    offScreenBufferPending = true;
	}

	// Send message to Renderer thread to perform createOffScreenBuffer.
	VirtualUniverse.mc.sendCreateOffScreenBuffer(this);

	// Wait for off-screen buffer to be created
	while (offScreenBufferPending) {
            // Issue 364: create master control thread if needed
            VirtualUniverse.mc.createMasterControlThread();
	    MasterControl.threadYield();
	}
    }

    // Send a destroyOffScreenBuffer message to Renderer (via
    // MasterControl) and wait for it to be done
    private void sendDestroyCtxAndOffScreenBuffer() {
	// Wait for the buffer to be destroyed unless called from
	// a Behavior or from a Rendering thread
	Thread currentThread = Thread.currentThread();
	if (!(currentThread instanceof BehaviorScheduler) &&
	    !(currentThread instanceof Renderer)) {

	    offScreenBufferPending = true;
	}

	// Fix for Issue 18 and Issue 175
	// Send message to Renderer thread to perform remove Ctx and destroyOffScreenBuffer.

        VirtualUniverse.mc.sendDestroyCtxAndOffScreenBuffer(this);

	// Wait for ctx and off-screen buffer to be destroyed
        while (offScreenBufferPending) {
            // Issue 364: create master control thread if needed
            VirtualUniverse.mc.createMasterControlThread();
            MasterControl.threadYield();
        }
    }

    // Send a allocateCanvasId message to Renderer (via MasterControl) without
    // waiting for it to be done
    private void sendAllocateCanvasId() {
        // Send message to Renderer thread to allocate a canvasId
        VirtualUniverse.mc.sendAllocateCanvasId(this);
    }

    // Send a freeCanvasId message to Renderer (via MasterControl) without
    // waiting for it to be done
    private void sendFreeCanvasId() {
        // Send message to Renderer thread to free the canvasId
        VirtualUniverse.mc.sendFreeCanvasId(this);
    }

    private void removeCtx() {

	if ((screen != null) &&
	    (screen.renderer != null) &&
	    (ctx != null)) {
            VirtualUniverse.mc.postRequest(MasterControl.FREE_CONTEXT,
                    new Object[]{this,
                            Long.valueOf(0L),
                            drawable,
                            ctx});
	    // Fix for Issue 19
	    // Wait for the context to be freed unless called from
	    // a Behavior or from a Rendering thread
	    Thread currentThread = Thread.currentThread();
	    if (!(currentThread instanceof BehaviorScheduler) &&
		!(currentThread instanceof Renderer)) {
		while (ctxTimeStamp != 0) {
		    MasterControl.threadYield();
		}
            }
	    ctx = null;
	}
    }

    /**
     * Serialization of Canvas3D objects is not supported.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @since Java 3D 1.3
     */
    private void writeObject(java.io.ObjectOutputStream out)
	    throws java.io.IOException {

	throw new UnsupportedOperationException(J3dI18N.getString("Canvas3D20"));
    }

    /**
     * Serialization of Canvas3D objects is not supported.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @since Java 3D 1.3
     */
    private void readObject(java.io.ObjectInputStream in)
	    throws java.io.IOException, ClassNotFoundException {

	throw new UnsupportedOperationException(J3dI18N.getString("Canvas3D20"));
    }


    // mark that the current bin specified by the bit is already updated
    void setStateIsUpdated(int bit) {
	stateUpdateMask &= ~(1 << bit);
    }

    // mark that the bin specified by the bit needs to be updated
    void setStateToUpdate(int bit, Object bin) {
	stateUpdateMask |= 1 << bit;
	curStateToUpdate[bit] = bin;
    }

    // update LightBin, EnvironmentSet, AttributeBin & ShaderBin if neccessary
    // according to the stateUpdateMask

    static int ENV_STATE_MASK = (1 << LIGHTBIN_BIT) |
				(1 << ENVIRONMENTSET_BIT) |
	                        (1 << ATTRIBUTEBIN_BIT) |
                                (1 << SHADERBIN_BIT);

    void updateEnvState() {

	if ((stateUpdateMask & ENV_STATE_MASK) == 0)
	    return;

	if ((stateUpdateMask & (1 << LIGHTBIN_BIT)) != 0) {
	    ((LightBin)curStateToUpdate[LIGHTBIN_BIT]).updateAttributes(this);
	}

	if ((stateUpdateMask & (1 << ENVIRONMENTSET_BIT)) != 0) {
	    ((EnvironmentSet)
		curStateToUpdate[ENVIRONMENTSET_BIT]).updateAttributes(this);
	}

	if ((stateUpdateMask & (1 << ATTRIBUTEBIN_BIT)) != 0) {
	    ((AttributeBin)
		curStateToUpdate[ATTRIBUTEBIN_BIT]).updateAttributes(this);
	}

	if ((stateUpdateMask & (1 << SHADERBIN_BIT)) != 0) {
	    ((ShaderBin)
		curStateToUpdate[SHADERBIN_BIT]).updateAttributes(this);
	}


	// reset the state update mask for those environment state bits
	stateUpdateMask &= ~ENV_STATE_MASK;
    }

    /**
     * update state if neccessary according to the stateUpdatedMask
     */
    void updateState( int dirtyBits) {


	if (stateUpdateMask == 0)
	    return;

	updateEnvState();

	if ((stateUpdateMask & (1 << TEXTUREBIN_BIT)) != 0) {
	    ((TextureBin)
		curStateToUpdate[TEXTUREBIN_BIT]).updateAttributes(this);
	}

	if ((stateUpdateMask & (1 << RENDERMOLECULE_BIT)) != 0) {
	    ((RenderMolecule)
	     curStateToUpdate[RENDERMOLECULE_BIT]).updateAttributes(this,
								    dirtyBits);

	}

        if ((stateUpdateMask & (1 << TRANSPARENCY_BIT)) != 0) {
	    ((RenderMolecule)curStateToUpdate[RENDERMOLECULE_BIT]).updateTransparencyAttributes(this);
	    stateUpdateMask &= ~(1 << TRANSPARENCY_BIT);
	}

	// reset state update mask
	stateUpdateMask = 0;
    }


    // This method updates this Texture2D for raster.
    // Note : No multi-texture is not used.
    void updateTextureForRaster(Texture2DRetained texture) {

        // Setup texture and texture attributes for texture unit 0.
        Pipeline.getPipeline().updateTextureUnitState(ctx, 0, true);
        setLastActiveTexUnit(0);
        setNumActiveTexUnit(1);

        texture.updateNative(this);
        resetTexCoordGeneration(ctx);
        resetTextureAttributes(ctx);

        for(int i=1; i < maxTextureUnits; i++) {
            resetTexture(ctx, i);
        }

        // set the active texture unit back to 0
        activeTextureUnit(ctx, 0);

        // Force the next textureBin to reload.
        canvasDirty |= Canvas3D.TEXTUREBIN_DIRTY | Canvas3D.TEXTUREATTRIBUTES_DIRTY;
    }

    void restoreTextureBin() {

        // Need to check TextureBin's shaderBin for null
        // TextureBin can get clear() if there isn't any RM under it.
        if((textureBin != null) && (textureBin.shaderBin != null)) {
            textureBin.updateAttributes(this);
        }
    }

    void textureFill(RasterRetained raster, Point2d winCoord,
           float mapZ, float alpha) {

        int winWidth = canvasViewCache.getCanvasWidth();
        int winHeight = canvasViewCache.getCanvasHeight();

        int rasterImageWidth = raster.image.width;
        int rasterImageHeight = raster.image.height;

        float texMinU = 0, texMinV = 0, texMaxU = 0, texMaxV = 0;
        float mapMinX = 0, mapMinY = 0, mapMaxX = 0, mapMaxY = 0;

        Point rasterSrcOffset = new Point();
        raster.getSrcOffset(rasterSrcOffset);

        Dimension rasterSize = new Dimension();
        raster.getSize(rasterSize);

//        System.err.println("rasterImageWidth " + rasterImageWidth + " rasterImageHeight " + rasterImageHeight);
//        System.err.println("rasterSrcOffset " + rasterSrcOffset + " rasterSize " + rasterSize);

        int rasterMinX = rasterSrcOffset.x;
        int rasterMaxX = rasterSrcOffset.x + rasterSize.width;
        int rasterMinY = rasterSrcOffset.y;
        int rasterMaxY = rasterSrcOffset.y + rasterSize.height;

        if ((rasterMinX >= rasterImageWidth) || (rasterMinY >= rasterImageHeight) ||
                (rasterMaxX <= 0) || (rasterMaxY <= 0)) {
            return;
        }

        if (rasterMinX < 0) {
            rasterMinX = 0;
        }
        if (rasterMinY < 0) {
            rasterMinY = 0;
        }

        if (rasterMaxX > rasterImageWidth) {
            rasterMaxX = rasterImageWidth;
        }

        if (rasterMaxY > rasterImageHeight) {
            rasterMaxY = rasterImageHeight;
        }

        texMinU = (float) rasterMinX / (float) rasterImageWidth;
        texMaxU = (float) rasterMaxX / (float) rasterImageWidth;
        mapMinX = (float) winCoord.x / (float) winWidth;
        mapMaxX = (float) (winCoord.x + (rasterMaxX - rasterMinX)) / (float) winWidth;

        if (raster.image.isYUp()) {
            texMinV = (float) rasterMinY / (float) rasterImageHeight;
            texMaxV = (float) rasterMaxY / (float) rasterImageHeight;
        } else {
          //  System.err.println("In yUp is false case");
            texMinV = 1.0f - (float) rasterMaxY / (float) rasterImageHeight;
            texMaxV = 1.0f - (float) rasterMinY / (float) rasterImageHeight;
        }

        mapMinY = 1.0f - ((float) (winCoord.y + (rasterMaxY - rasterMinY)) / (float) winHeight);
        mapMaxY = 1.0f - ((float) winCoord.y / (float) winHeight);

        textureFillRaster(ctx, texMinU, texMaxU, texMinV, texMaxV,
                mapMinX, mapMaxX, mapMinY, mapMaxY, mapZ, alpha, raster.image.useBilinearFilter());

    }

    void textureFill(BackgroundRetained bg, int winWidth, int winHeight) {

        final int maxX = bg.image.width;
        final int maxY = bg.image.height;

//        System.err.println("maxX " + maxX + " maxY " + maxY);

        float xzoom = (float)winWidth  / maxX;
        float yzoom = (float)winHeight / maxY;
        float zoom = 0;
        float texMinU = 0, texMinV = 0, texMaxU = 0, texMaxV = 0, adjustV = 0;
        float mapMinX = 0, mapMinY = 0, mapMaxX = 0, mapMaxY = 0;
        float halfWidth = 0, halfHeight = 0;

        switch (bg.imageScaleMode) {
            case Background.SCALE_NONE:
                texMinU = 0.0f;
                texMinV = 0.0f;
                texMaxU = 1.0f;
                texMaxV = 1.0f;
                halfWidth = (float)winWidth/2.0f;
                halfHeight = (float)winHeight/2.0f;
                mapMinX = (float) ((0 - halfWidth)/halfWidth);
                mapMinY = (float) ((0 - halfHeight)/halfHeight);
                mapMaxX = (float) ((maxX - halfWidth)/halfWidth);
                mapMaxY = (float) ((maxY - halfHeight)/halfHeight);
                adjustV = ((float)winHeight - (float)maxY)/halfHeight;
                mapMinY += adjustV;
                mapMaxY += adjustV;
                break;
            case Background.SCALE_FIT_MIN:
                zoom = Math.min(xzoom, yzoom);
                texMinU = 0.0f;
                texMinV = 0.0f;
                texMaxU = 1.0f;
                texMaxV = 1.0f;
                mapMinX = -1.0f;
                mapMaxY = 1.0f;
                if (xzoom < yzoom) {
                    mapMaxX = 1.0f;
                    mapMinY = -1.0f + 2.0f * ( 1.0f - zoom * (float)maxY/(float) winHeight );
                } else {
                    mapMaxX = -1.0f + zoom * (float)maxX/winWidth * 2;
                    mapMinY = -1.0f;
                }
                break;
            case Background.SCALE_FIT_MAX:
                zoom = Math.max(xzoom, yzoom);
                mapMinX = -1.0f;
                mapMinY = -1.0f;
                mapMaxX = 1.0f;
                mapMaxY = 1.0f;
                if (xzoom < yzoom) {
                    texMinU = 0.0f;
                    texMinV = 0.0f;
                    texMaxU = (float)winWidth/maxX/zoom;
                    texMaxV = 1.0f;
                } else {
                    texMinU = 0.0f;
                    texMinV = 1.0f - (float)winHeight/maxY/zoom;
                    texMaxU = 1.0f;
                    texMaxV = 1.0f;
                }
                break;
            case Background.SCALE_FIT_ALL:
                texMinU = 0.0f;
                texMinV = 0.0f;
                texMaxU = 1.0f;
                texMaxV = 1.0f;
                mapMinX = -1.0f;
                mapMinY = -1.0f;
                mapMaxX = 1.0f;
                mapMaxY = 1.0f;
                break;
            case Background.SCALE_REPEAT:

                texMinU = 0.0f;
                texMinV = - yzoom;
                texMaxU = xzoom;
                texMaxV = 0.0f;
                mapMinX = -1.0f;
                mapMinY = -1.0f;
                mapMaxX = 1.0f;
                mapMaxY = 1.0f;
                break;
            case Background.SCALE_NONE_CENTER:
                // TODO : Why is there a zoom ?
                if(xzoom >= 1.0f){
                    texMinU = 0.0f;
                    texMaxU = 1.0f;
                    mapMinX = -(float)maxX/winWidth;
                    mapMaxX = (float)maxX/winWidth;
                } else {
                    texMinU = 0.5f - (float)winWidth/maxX/2;
                    texMaxU = 0.5f + (float)winWidth/maxX/2;
                    mapMinX = -1.0f;
                    mapMaxX = 1.0f;
                }
                if (yzoom >= 1.0f) {
                    texMinV = 0.0f;
                    texMaxV = 1.0f;
                    mapMinY = -(float)maxY/winHeight;
                    mapMaxY = (float)maxY/winHeight;
                } else {
                    texMinV = 0.5f - (float)winHeight/maxY/2;
                    texMaxV = 0.5f + (float)winHeight/maxY/2;
                    mapMinY = -1.0f;
                    mapMaxY = 1.0f;
                }
                break;
        }

//        System.err.println("Java 3D : mapMinX " + mapMinX + " mapMinY " + mapMinY +
//                           " mapMaxX " + mapMaxX + " mapMaxY " + mapMaxY);
        textureFillBackground(ctx, texMinU, texMaxU, texMinV, texMaxV,
                mapMinX, mapMaxX, mapMinY, mapMaxY, bg.image.useBilinearFilter());

    }


    void clear(BackgroundRetained bg, int winWidth, int winHeight) {

        // Issue 239 - clear stencil if requested and available
        // Note that this is a partial solution, since we eventually want an API
        // to control this.
        boolean clearStencil = VirtualUniverse.mc.stencilClear &&
                userStencilAvailable;

        clear(ctx, bg.color.x, bg.color.y, bg.color.z, clearStencil);

        // TODO : This is a bug on not mirror bg. Will fix this as a bug after 1.5 beta.
        // For now, as a workaround, we will check bg.image and bg.image.imageData not null.
        if((bg.image != null) && (bg.image.imageData != null)) {
            // setup Texture pipe.
            updateTextureForRaster(bg.texture);

            textureFill(bg, winWidth, winHeight);

            // Restore texture pipe.
            restoreTextureBin();
        }
    }

    /**
     * obj is either TextureRetained or DetailTextureImage
     * if obj is DetailTextureImage then we just clear
     * the resourceCreationMask of all the formats
     * no matter it is create or not since we don't
     * remember the format information for simplicity.
     * We don't need to check duplicate value of id in the
     * table since this procedure is invoke only when id
     * of texture is -1 one time only.
     * This is always call from Renderer thread.
     */
void addTextureResource(int id, TextureRetained obj) {
	if (id <= 0) {
	    return;
	}

	if (useSharedCtx) {
	    screen.renderer.addTextureResource(id, obj);
	} else {
	    // This will replace the previous key if exists
	    if (textureIDResourceTable.size() <= id) {
		for (int i=textureIDResourceTable.size();
		     i < id; i++) {
		    textureIDResourceTable.add(null);
		}
		textureIDResourceTable.add(obj);
	    } else {
		textureIDResourceTable.set(id, obj);
	    }

	}
    }

    // handle free resource in the FreeList
    void freeResourcesInFreeList(Context ctx) {
	Iterator<Integer> it;
	int val;

	// free resource for those canvases that
	// don't use shared ctx
	if (displayListResourceFreeList.size() > 0) {
	    for (it = displayListResourceFreeList.iterator(); it.hasNext();) {
			val = it.next().intValue();
		if (val <= 0) {
		    continue;
		}
		freeDisplayList(ctx, val);
	    }
	    displayListResourceFreeList.clear();
	}

        if (textureIdResourceFreeList.size() > 0) {
            for (it = textureIdResourceFreeList.iterator(); it.hasNext();) {
			val = it.next().intValue();
                if (val <= 0) {
                    continue;
                }
                if (val >= textureIDResourceTable.size()) {
                    System.err.println("Error in freeResourcesInFreeList : ResourceIDTableSize = " +
                            textureIDResourceTable.size() +
                            " val = " + val);
                } else {
				TextureRetained tex = textureIDResourceTable.get(val);
				if (tex != null) {
                        synchronized (tex.resourceLock) {
                            tex.resourceCreationMask &= ~canvasBit;
                            if (tex.resourceCreationMask == 0) {
                                tex.freeTextureId(val);
                            }
                        }
                    }

                    textureIDResourceTable.set(val, null);
                }
                freeTexture(ctx, val);
            }
            textureIdResourceFreeList.clear();
        }
    }

    void freeContextResources(Renderer rdr, boolean freeBackground,
			      Context ctx) {
	TextureRetained tex;

	// Just return if we don't have a valid renderer or context
	if (rdr == null || ctx == null) {
	    return;
	}

	if (freeBackground) {
	    // Dispose of Graphics2D Texture
            if (graphics2D != null) {
                graphics2D.dispose();
            }
	}

	for (int id = textureIDResourceTable.size()-1; id >= 0; id--) {
		tex = textureIDResourceTable.get(id);
		if (tex == null) {
			continue;
		}

            // Issue 403 : this assertion doesn't hold in some cases
            // TODO KCR : determine why this is the case
//            assert id == ((TextureRetained)obj).objectId;

	    freeTexture(ctx, id);
		synchronized (tex.resourceLock) {
		    tex.resourceCreationMask &= ~canvasBit;
		    if (tex.resourceCreationMask == 0) {

			tex.freeTextureId(id);
		    }
		}
	}
	textureIDResourceTable.clear();

	freeAllDisplayListResources(ctx);
    }

    void freeAllDisplayListResources(Context ctx) {
	if ((view != null) && (view.renderBin != null)) {
	    view.renderBin.freeAllDisplayListResources(this, ctx);
	    if (useSharedCtx) {
		// We need to rebuild all other Canvas3D resource
		// shared by this Canvas3D. Since we didn't
		// remember resource in Renderer but RenderBin only.
		if ((screen != null) && (screen.renderer != null)) {
		    screen.renderer.needToRebuildDisplayList = true;
		}
	    }
	}

    }


    // *****************************************************************
    // Wrappers for native methods go below here
    // *****************************************************************

    // This is the native method for creating the underlying graphics context.
    private Context createNewContext(Drawable drawable,
            Context shareCtx, boolean isSharedCtx,
            boolean offScreen) {
        return Pipeline.getPipeline().createNewContext(this, drawable,
                shareCtx, isSharedCtx,
                offScreen);
    }

    private void createQueryContext(Drawable drawable,
            boolean offScreen, int width, int height) {
        Pipeline.getPipeline().createQueryContext(this, drawable,
                offScreen, width, height);
    }

    // This is the native for creating offscreen buffer
    Drawable createOffScreenBuffer(Context ctx, int width, int height) {
        return Pipeline.getPipeline().createOffScreenBuffer(this,
                ctx, width, height);
    }

    void destroyOffScreenBuffer(Context ctx, Drawable drawable) {
        assert drawable != null;
        Pipeline.getPipeline().destroyOffScreenBuffer(this, ctx, drawable);
    }

    // This is the native for reading the image from the offscreen buffer
    private void readOffScreenBuffer(Context ctx, int format, int type, Object data, int width, int height) {
        Pipeline.getPipeline().readOffScreenBuffer(this, ctx, format, type, data, width, height);
    }

// The native method for swapBuffers
void swapBuffers(Context ctx, Drawable drawable) {
	Pipeline.getPipeline().swapBuffers(this, ctx, drawable);
}

    // -----------------------------------------------------------------------------

    // native method for setting Material when no material is present
    void updateMaterial(Context ctx, float r, float g, float b, float a) {
        Pipeline.getPipeline().updateMaterialColor(ctx, r, g, b, a);
    }

    static void destroyContext(Drawable drawable, Context ctx) {
        Pipeline.getPipeline().destroyContext(drawable, ctx);
    }

    // This is the native method for doing accumulation.
    void accum(Context ctx, float value) {
        Pipeline.getPipeline().accum(ctx, value);
    }

    // This is the native method for doing accumulation return.
    void accumReturn(Context ctx) {
        Pipeline.getPipeline().accumReturn(ctx);
    }

    // This is the native method for clearing the accumulation buffer.
    void clearAccum(Context ctx) {
        Pipeline.getPipeline().clearAccum(ctx);
    }

    // This is the native method for getting the number of lights the underlying
    // native library can support.
    int getNumCtxLights(Context ctx) {
        return Pipeline.getPipeline().getNumCtxLights(ctx);
    }

    // Native method for decal 1st child setup
    boolean decal1stChildSetup(Context ctx) {
        return Pipeline.getPipeline().decal1stChildSetup(ctx);
    }

    // Native method for decal nth child setup
    void decalNthChildSetup(Context ctx) {
        Pipeline.getPipeline().decalNthChildSetup(ctx);
    }

    // Native method for decal reset
    void decalReset(Context ctx, boolean depthBufferEnable) {
        Pipeline.getPipeline().decalReset(ctx, depthBufferEnable);
    }

    // Native method for decal reset
    void ctxUpdateEyeLightingEnable(Context ctx, boolean localEyeLightingEnable) {
        Pipeline.getPipeline().ctxUpdateEyeLightingEnable(ctx, localEyeLightingEnable);
    }

    // The following three methods are used in multi-pass case

    // native method for setting blend color
    void setBlendColor(Context ctx, float red, float green,
            float blue, float alpha) {
        Pipeline.getPipeline().setBlendColor(ctx, red, green,
                blue, alpha);
    }

    // native method for setting blend func
    void setBlendFunc(Context ctx, int src, int dst) {
        Pipeline.getPipeline().setBlendFunc(ctx, src, dst);
    }

    // native method for setting fog enable flag
    void setFogEnableFlag(Context ctx, boolean enableFlag) {
        Pipeline.getPipeline().setFogEnableFlag(ctx, enableFlag);
    }

    // Setup the full scene antialising in D3D and ogl when GL_ARB_multisamle supported
    void setFullSceneAntialiasing(Context ctx, boolean enable) {
        Pipeline.getPipeline().setFullSceneAntialiasing(ctx, enable);
    }

    void setGlobalAlpha(Context ctx, float alpha) {
        Pipeline.getPipeline().setGlobalAlpha(ctx, alpha);
    }

    // Native method to update separate specular color control
    void updateSeparateSpecularColorEnable(Context ctx, boolean control) {
        Pipeline.getPipeline().updateSeparateSpecularColorEnable(ctx, control);
    }

    // True under Solaris,
    // False under windows when display mode <= 8 bit
    private boolean validGraphicsMode() {
        return Pipeline.getPipeline().validGraphicsMode();
    }

    // native method for setting light enables
    void setLightEnables(Context ctx, long enableMask, int maxLights) {
        Pipeline.getPipeline().setLightEnables(ctx, enableMask, maxLights);
    }

    // native method for setting scene ambient
    void setSceneAmbient(Context ctx, float red, float green, float blue) {
        Pipeline.getPipeline().setSceneAmbient(ctx, red, green, blue);
    }

    // native method for disabling fog
    void disableFog(Context ctx) {
        Pipeline.getPipeline().disableFog(ctx);
    }

    // native method for disabling modelClip
    void disableModelClip(Context ctx) {
        Pipeline.getPipeline().disableModelClip(ctx);
    }

    // native method for setting default RenderingAttributes
    void resetRenderingAttributes(Context ctx,
            boolean depthBufferWriteEnableOverride,
            boolean depthBufferEnableOverride) {
        Pipeline.getPipeline().resetRenderingAttributes(ctx,
                depthBufferWriteEnableOverride,
                depthBufferEnableOverride);
    }

    // native method for setting default texture
    void resetTextureNative(Context ctx, int texUnitIndex) {
        Pipeline.getPipeline().resetTextureNative(ctx, texUnitIndex);
    }

    // native method for activating a particular texture unit
    void activeTextureUnit(Context ctx, int texUnitIndex) {
        Pipeline.getPipeline().activeTextureUnit(ctx, texUnitIndex);
    }

    // native method for setting default TexCoordGeneration
    void resetTexCoordGeneration(Context ctx) {
        Pipeline.getPipeline().resetTexCoordGeneration(ctx);
    }

    // native method for setting default TextureAttributes
    void resetTextureAttributes(Context ctx) {
        Pipeline.getPipeline().resetTextureAttributes(ctx);
    }

    // native method for setting default PolygonAttributes
    void resetPolygonAttributes(Context ctx) {
        Pipeline.getPipeline().resetPolygonAttributes(ctx);
    }

    // native method for setting default LineAttributes
    void resetLineAttributes(Context ctx) {
        Pipeline.getPipeline().resetLineAttributes(ctx);
    }

    // native method for setting default PointAttributes
    void resetPointAttributes(Context ctx) {
        Pipeline.getPipeline().resetPointAttributes(ctx);
    }

    // native method for setting default TransparencyAttributes
    void resetTransparency(Context ctx, int geometryType,
            int polygonMode, boolean lineAA,
            boolean pointAA) {
        Pipeline.getPipeline().resetTransparency(ctx, geometryType,
                polygonMode, lineAA,
                pointAA);
    }

    // native method for setting default ColoringAttributes
    void resetColoringAttributes(Context ctx,
            float r, float g,
            float b, float a,
            boolean enableLight) {
        Pipeline.getPipeline().resetColoringAttributes(ctx,
                r, g,
                b, a,
                enableLight);
    }

    /**
     *  This native method makes sure that the rendering for this canvas
     *  gets done now.
     */
    void syncRender(Context ctx, boolean wait) {
        Pipeline.getPipeline().syncRender(ctx, wait);
    }

    // The native method that sets this ctx to be the current one
    static boolean useCtx(Context ctx, Drawable drawable) {
        return Pipeline.getPipeline().useCtx(ctx, drawable);
    }

    // Give the Pipeline a chance to release the context. The return
    // value indicates whether the context was released.
    private boolean releaseCtx(Context ctx) {
        return Pipeline.getPipeline().releaseCtx(ctx);
    }

    void clear(Context ctx, float r, float g, float b, boolean clearStencil) {
        Pipeline.getPipeline().clear(ctx, r, g, b, clearStencil);
    }

    void textureFillBackground(Context ctx, float texMinU, float texMaxU, float texMinV, float texMaxV,
            float mapMinX, float mapMaxX, float mapMinY, float mapMaxY, boolean useBiliearFilter) {
        Pipeline.getPipeline().textureFillBackground(ctx, texMinU, texMaxU, texMinV, texMaxV,
                mapMinX, mapMaxX, mapMinY, mapMaxY, useBiliearFilter);
    }

    void textureFillRaster(Context ctx, float texMinU, float texMaxU, float texMinV, float texMaxV,
            float mapMinX, float mapMaxX, float mapMinY, float mapMaxY, float mapZ, float alpha, boolean useBiliearFilter)  {
        Pipeline.getPipeline().textureFillRaster(ctx, texMinU, texMaxU, texMinV, texMaxV,
                mapMinX, mapMaxX, mapMinY, mapMaxY, mapZ, alpha, useBiliearFilter);
    }

    void executeRasterDepth(Context ctx, float posX, float posY, float posZ,
            int srcOffsetX, int srcOffsetY, int rasterWidth, int rasterHeight,
            int depthWidth, int depthHeight, int depthType, Object depthData) {
        Pipeline.getPipeline().executeRasterDepth(ctx, posX, posY, posZ,
                srcOffsetX, srcOffsetY, rasterWidth, rasterHeight, depthWidth, depthHeight, depthType, depthData);
    }

    // The native method for setting the ModelView matrix.
    void setModelViewMatrix(Context ctx, double[] viewMatrix, double[] modelMatrix) {
        Pipeline.getPipeline().setModelViewMatrix(ctx, viewMatrix, modelMatrix);
    }

    // The native method for setting the Projection matrix.
    void setProjectionMatrix(Context ctx, double[] projMatrix) {
        Pipeline.getPipeline().setProjectionMatrix(ctx, projMatrix);
    }

    // The native method for setting the Viewport.
    void setViewport(Context ctx, int x, int y, int width, int height) {
        Pipeline.getPipeline().setViewport(ctx, x, y, width, height);
    }

    // used for display Lists
    void newDisplayList(Context ctx, int displayListId) {
        Pipeline.getPipeline().newDisplayList(ctx, displayListId);
    }
    void endDisplayList(Context ctx) {
        Pipeline.getPipeline().endDisplayList(ctx);
    }
    void callDisplayList(Context ctx, int id, boolean isNonUniformScale) {
        Pipeline.getPipeline().callDisplayList(ctx, id, isNonUniformScale);
    }

    static void freeDisplayList(Context ctx, int id) {
        Pipeline.getPipeline().freeDisplayList(ctx, id);
    }
    static void freeTexture(Context ctx, int id) {
        Pipeline.getPipeline().freeTexture(ctx, id);
    }

    void texturemapping(Context ctx,
            int px, int py,
            int xmin, int ymin, int xmax, int ymax,
            int texWidth, int texHeight,
            int rasWidth,
            int format, int objectId,
            byte[] image,
            int winWidth, int winHeight) {
        Pipeline.getPipeline().texturemapping(ctx,
                px, py,
                xmin, ymin, xmax, ymax,
                texWidth, texHeight,
                rasWidth,
                format, objectId,
                image,
                winWidth, winHeight);
    }

    boolean initTexturemapping(Context ctx, int texWidth,
            int texHeight, int objectId) {
        return Pipeline.getPipeline().initTexturemapping(ctx, texWidth,
                texHeight, objectId);
    }


    // Set internal render mode to one of FIELD_ALL, FIELD_LEFT or
    // FIELD_RIGHT.  Note that it is up to the caller to ensure that
    // stereo is available before setting the mode to FIELD_LEFT or
    // FIELD_RIGHT.  The boolean isTRUE for double buffered mode, FALSE
    // foe single buffering.
    void setRenderMode(Context ctx, int mode, boolean doubleBuffer) {
        Pipeline.getPipeline().setRenderMode(ctx, mode, doubleBuffer);
    }

    // Set glDepthMask.
    void setDepthBufferWriteEnable(Context ctx, boolean mode) {
        Pipeline.getPipeline().setDepthBufferWriteEnable(ctx, mode);
    }

    // Methods to get actual capabilities from Canvas3D

    boolean hasDoubleBuffer() {
	return Pipeline.getPipeline().hasDoubleBuffer(this);
    }

    boolean hasStereo() {
	return Pipeline.getPipeline().hasStereo(this);
    }

    int getStencilSize() {
	return Pipeline.getPipeline().getStencilSize(this);
    }

    boolean hasSceneAntialiasingMultisample() {
	return Pipeline.getPipeline().hasSceneAntialiasingMultisample(this);
    }

    boolean hasSceneAntialiasingAccum() {
	return Pipeline.getPipeline().hasSceneAntialiasingAccum(this);
    }

}
