/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * The Screen3D Object contains all information about a particular screen.
 * All Canvas3D objects on the same physical screen (display device)
 * refer to the same Screen3D object.  Note that Screen3D has no public
 * constructors--it is obtained from the Canvas3D via the getScreen3D
 * method.
 * <p>
 * Default values for Screen3D parameters are as follows:
 * <ul>
 * physical screen width : 0.0254/90.0 * screen width (in pixels)<br>
 * physical screen height : 0.0254/90.0 * screen height (in pixels)<br>
 * tracker base to image plate transform : identity<br>
 * head tracker to left image plate transform : identity<br>
 * head tracker to right image plate transform : identity<br>
 * off-screen size : (0,0)<br>
 * </ul>
 * <P>
 * <b>Offscreen Rendering</b><P>
 * New for Java 3D 1.2, an off-screen rendering mode allows rendering
 * to a memory image, which is possibly larger than the screen. The
 * setSize and getSize methods are defined specifically for this 
 * mode. Note that the off-screen size, physical width, and physical height
 * must be set prior to rendering
 * to the associated off-screen canvas. Failure to do so will result 
 * in an exception.<P>
 * <b>Calibration Parameters</b><P>
 * The Screen3D object must be calibrated with the coexistence volume.
 * The Screen3D class provides several methods for defining the
 * calibration parameters.<P>
 * <UL>Measured Parameters<P>
 * The screen's (image plate's) physical width and height (in meters)
 * is set once, typically by a browser, calibration program, system
 * administrator, or system calibrator, not by an applet. These values
 * must be determined by measuring the display's active image width
 * and height. In the case of a head-mounted display, this should be
 * the display's apparent width and height at the focal plane. These
 * values are defined by the setPhysicalScreenWidth and
 * setPhysicalScreenHeight methods.<P>
 *
 * Head-tracker Coordinate System<P>
 * If head tracking is enabled, one of two parameters need to be specified:<P>
 * <UL><LI>If the view policy is SCREEN_VIEW, the tracker-base-to-image-plate
 * coordinate system must be specified (setTrackerBaseToImagePlate method).
 * This coordinate system must be recalibrated whenever the image
 * plate moves relative to the tracker.</LI><P>
 *
 * <LI>If the view policy is HMD_VIEW, the head-tracker-to-left-image-plate
 * and head-tracker-to-right-image-plate coordinate systems must be
 * specified (setHeadTrackerToLeftImagePlate and
 * setHeadTrackerToRightImagePlate methods).</LI><P></UL>
 * </UL><P>
 * @see Canvas3D
 * @see Canvas3D#getScreen3D
 */

public class Screen3D extends Object {
    private static final boolean debug = false;

    // Assume a default of 90 DPI: 90 pix/inch = 1/90 inch/pix =
    // 0.0254/90 meter/pix
    private static final double METERS_PER_PIXEL = 0.0254/90.0;

    // GraphicsDevice associated with this Screen3D object.  Note that
    // all on-screen Canvas3D objects that are created on the same
    // GraphicsDevice will share the same Screen3D.
    GraphicsDevice graphicsDevice;

    // Flag indicating whether this Screen3D is associated with
    // an off-screen Canvas3D or with one or more on-screen Canvas3Ds
    boolean offScreen;

    // The display connection (X11 only) and the screen ID
    long display;
    int screen;

    // The width and height of the screen in meters.
    double physicalScreenWidth;
    double physicalScreenHeight;

    // Screen size in pixels
    Dimension screenSize = new Dimension(0, 0);

    //
    // Tracker-base coordinate system to image-plate coordinate
    // system transform.  This transform
    // is typically a calibration constant.
    // This is used only in SCREEN_VIEW mode.
    //
    Transform3D trackerBaseToImagePlate = new Transform3D();

    //
    // Head-tracker coordinate system to left and right image-plate
    // coordinate system transforms.  These transforms are typically
    // calibration constants.  These are used only in HMD_VIEW mode.
    //
    Transform3D headTrackerToLeftImagePlate = new Transform3D();
    Transform3D headTrackerToRightImagePlate = new Transform3D();

    
    //  Physical screen size related field has changed.
    static final int PHYSICAL_SCREEN_SIZE_DIRTY          = 0x01;
    // Screen size field has changed.
    static final int SCREEN_SIZE_DIRTY_DIRTY             = 0x02;
    // Tracker base to image plate field has changed.
    static final int TRACKER_BASE_TO_IMAGE_PLATE_DIRTY   = 0x04;
    // Head tracker to  image plate field has changed.
    static final int HEAD_TRACKER_TO_IMAGE_PLATE_DIRTY   = 0x08;    

    // Mask that indicates this Screen3D view dependence info. has changed,
    // and CanvasViewCache may need to recompute the final view matries.
    int scrDirtyMask = (PHYSICAL_SCREEN_SIZE_DIRTY | SCREEN_SIZE_DIRTY_DIRTY
			| TRACKER_BASE_TO_IMAGE_PLATE_DIRTY
			| HEAD_TRACKER_TO_IMAGE_PLATE_DIRTY);    
    
    //
    // View cache for this screen
    //
    ScreenViewCache screenViewCache = null;

    // The renderer for this screen
    Renderer renderer = null;
    
    // Hashtable that maps a GraphicsDevice to its associated renderer
    static Hashtable deviceRendererMap = new Hashtable();

    // A count of the number of canvases associated with this screen
    int canvasCount = 0;

    // A count of the number of active View associated with this screen
    UnorderList activeViews = new UnorderList(1, View.class);

    // A list of Canvas3D Objects that refer to this
    ArrayList users = new ArrayList();

    void addActiveView(View v) {
	activeViews.addUnique(v);
    }

    void removeActiveView(View v) {
	activeViews.remove(v);
    }

    boolean activeViewEmpty() {
	return activeViews.isEmpty();
    }

    // Add a user to the list of users
    synchronized void removeUser(Canvas3D c) {
	int idx = users.indexOf(c);
	if (idx >= 0) {
	    users.remove(idx);
	}
    }

    // Add a user to the list of users
    synchronized void addUser(Canvas3D c) {
	int idx = users.indexOf(c);
	if (idx < 0) {
	    users.add(c);
	}
    }

    // Add a user to the list of users
    synchronized void notifyUsers() {
	int i;
	Canvas3D c;

	for (i=0; i<users.size(); i++) {
	    c = (Canvas3D)users.get(i);
	    c.redraw();
	}
    }

    /**
     * Retrieves the width and height (in pixels) of this Screen3D.
     *
     * @return a new Dimension object containing the width and height
     * of this Screen3D.
     */
    public Dimension getSize() {
	return new Dimension(screenSize);
    }

    /**
     * Retrieves the width and height (in pixels) of this Screen3D
     * and copies it into the specified Dimension object.
     *
     * @param rv Dimension object into which the size of
     * this Screen3D is copied.
     * If <code>rv</code> is null, a new Dimension object is allocated.
     *
     * @return <code>rv</code>
     *
     * @since Java 3D 1.2
     */
    public Dimension getSize(Dimension rv) {
	if (rv == null) {
	    return new Dimension(screenSize);
	}
	else {
	    rv.setSize(screenSize);
	    return rv;
	}
    }

    /**
     * Sets the width and height (in pixels) of this off-screen Screen3D.
     * The default size for off-screen Screen3D objects is (0,0).
     * <br>
     * NOTE: the size must be
     * set prior to rendering to the associated off-screen canvas.
     * Failure to do so will result in an exception.
     *
     * @param width the new width of this Screen3D object
     * @param height the new height of this Screen3D object
     *
     * @exception IllegalStateException if this Screen3D is not in
     * off-screen mode.
     *
     * @since Java 3D 1.2
     */
    public void setSize(int width, int height) {

	if (!offScreen)
            throw new IllegalStateException(J3dI18N.getString("Screen3D1"));
	    
	synchronized(this) {
	    screenSize.width = width;
	    screenSize.height = height;
	    scrDirtyMask |= SCREEN_SIZE_DIRTY_DIRTY;
	}
    }

    /**
     * Sets the width and height (in pixels) of this off-screen Screen3D.
     * The default size for off-screen Screen3D objects is (0,0).
     * <br>
     * NOTE: the size must be
     * set prior to rendering to the associated off-screen canvas.
     * Failure to do so will result in an exception.
     *
     * @param d the new dimension of this Screen3D object
     *
     * @exception IllegalStateException if this Screen3D is not in
     * off-screen mode.
     *
     * @since Java 3D 1.2
     */
    public void setSize(Dimension d) {
	if (!offScreen)
            throw new IllegalStateException(J3dI18N.getString("Screen3D1"));
	    
	synchronized(this) {
	    screenSize.width = d.width;
	    screenSize.height = d.height;
	    scrDirtyMask |= SCREEN_SIZE_DIRTY_DIRTY;
	}
    }

    /**
     * Sets the screen physical width in meters.  In the case of a
     * head-mounted display, this should be the apparent width
     * at the focal plane.
     * @param width the screen's physical width in meters
     */
    public void setPhysicalScreenWidth(double width) {
	synchronized(this) {
	    physicalScreenWidth = width;
	    scrDirtyMask |= PHYSICAL_SCREEN_SIZE_DIRTY;
	}
	notifyUsers();
    }

    /**
     * Retrieves the screen's physical width in meters.
     * @return the screen's physical width in meters
     */
    public double getPhysicalScreenWidth() {
	return physicalScreenWidth;
    }

    /**
     * Sets the screen physical height in meters.  In the case of a
     * head-mounted display, this should be the apparent height
     * at the focal plane.
     * @param height the screen's physical height in meters
     */
    public void setPhysicalScreenHeight(double height) {
	synchronized(this) {
	    physicalScreenHeight = height;
	    scrDirtyMask |= PHYSICAL_SCREEN_SIZE_DIRTY;
	}
	notifyUsers();
    }
    
    /**
     * Retrieves the the screen's physical height in meters.
     * @return the screen's physical height in meters
     */
    public double getPhysicalScreenHeight() {
	return physicalScreenHeight;
    }

    public String toString() {
	return "Screen3D: size = " +
	    "(" + getSize().width + " x " + getSize().height + ")" +
	    ", physical size = " +
	    "(" + getPhysicalScreenWidth() + "m x " +
	    getPhysicalScreenHeight() + "m)";
    }

    // Static initializer for Screen3D class
    static {
 	VirtualUniverse.loadLibraries();
    }

    /**
     * Construct a new Screen3D object with the specified size in pixels.
     * Note that currently, there is no AWT equivalent of screen so Java 3D
     * users need to get this through the Canvas3D object (via getScreen()) if
     * they need it.
     * @param graphicsConfiguration the AWT graphics configuration associated
     * with this Screen3D
     * @param offScreen a flag that indicates whether this Screen3D is
     * associated with an off-screen Canvas3D
     */
    Screen3D(GraphicsConfiguration graphicsConfiguration, boolean offScreen) {
	NativeScreenInfo nativeScreenInfo;

	this.offScreen = offScreen;
	this.graphicsDevice = graphicsConfiguration.getDevice(); 

	screenViewCache = new ScreenViewCache(this);
	nativeScreenInfo = new NativeScreenInfo(graphicsDevice);

	// Get the display from the native code (X11 only) and the
	// screen ID
	display = nativeScreenInfo.getDisplay();
	screen = nativeScreenInfo.getScreen();

	if (debug)
	    System.out.println("Screen3D: display " + display +
			       " screen " + screen + " hashcode " +
			       this.hashCode());

	if (!offScreen) {
	    // Store the information in this screen object
	    Rectangle bounds = graphicsConfiguration.getBounds();
	    screenSize.width = bounds.width;
	    screenSize.height = bounds.height;
	}

	// Set the default physical size based on size in pixels
	physicalScreenWidth = screenSize.width * METERS_PER_PIXEL;
	physicalScreenHeight = screenSize.height * METERS_PER_PIXEL;
    }


    /**
     * Sets the tracker-base coordinate system to image-plate coordinate
     * system transform.  This transform
     * is typically a calibration constant.
     * This is used only in SCREEN_VIEW mode.
     * @param t the new transform
     * @exception BadTransformException if the transform is not rigid
     */
    public void setTrackerBaseToImagePlate(Transform3D t) {
	synchronized(this) {
	    if (!t.isRigid()) {
		throw new BadTransformException(J3dI18N.getString("Screen3D0"));
	    }
	    trackerBaseToImagePlate.setWithLock(t);
	    scrDirtyMask |= Screen3D.TRACKER_BASE_TO_IMAGE_PLATE_DIRTY;
	}
	notifyUsers();
    }

    /**
     * Retrieves the tracker-base coordinate system to image-plate
     * coordinate system transform and copies it into the specified
     * Transform3D object.
     * @param t the object that will receive the transform
     */
    public void getTrackerBaseToImagePlate(Transform3D t) {
	t.set(trackerBaseToImagePlate);
    }

    /**
     * Sets the head-tracker coordinate system to left image-plate coordinate
     * system transform.  This transform
     * is typically a calibration constant.
     * This is used only in HMD_VIEW mode.
     * @param t the new transform
     * @exception BadTransformException if the transform is not rigid
     */
    public void setHeadTrackerToLeftImagePlate(Transform3D t) {
	synchronized(this) {
	    if (!t.isRigid()) {
		throw new BadTransformException(J3dI18N.getString("Screen3D0"));
	    }
	    headTrackerToLeftImagePlate.setWithLock(t);
	    scrDirtyMask |= Screen3D.HEAD_TRACKER_TO_IMAGE_PLATE_DIRTY; 
	}
	notifyUsers();
    }
    
    /**
     * Retrieves the head-tracker coordinate system to left image-plate
     * coordinate system transform and copies it into the specified
     * Transform3D object.
     * @param t the object that will receive the transform
     */
    public void getHeadTrackerToLeftImagePlate(Transform3D t) {
	t.set(headTrackerToLeftImagePlate);
    }

    /**
     * Sets the head-tracker coordinate system to right image-plate coordinate
     * system transform.  This transform
     * is typically a calibration constant.
     * This is used only in HMD_VIEW mode.
     * @param t the new transform
     * @exception BadTransformException if the transform is not rigid
     */
    public void setHeadTrackerToRightImagePlate(Transform3D t) {
	synchronized(this) {
	    if (!t.isRigid()) {
		throw new BadTransformException(J3dI18N.getString("Screen3D0"));
	    }
	    headTrackerToRightImagePlate.setWithLock(t);
	    scrDirtyMask |= Screen3D.HEAD_TRACKER_TO_IMAGE_PLATE_DIRTY;
	}
	notifyUsers();
    }
    
    /**
     * Retrieves the head-tracker coordinate system to right image-plate
     * coordinate system transform and copies it into the specified
     * Transform3D object.
     * @param t the object that will receive the transform
     */
    public void getHeadTrackerToRightImagePlate(Transform3D t) {
	t.set(headTrackerToRightImagePlate);
    }

    /**
     * Update the view cache associated with this screen.
     */
    void updateViewCache() {
	if (false)
	    System.out.println("Screen3D.updateViewCache()");
	synchronized(this) {
	    screenViewCache.snapshot();
	}
    }
    
    /**
     * Increment canvas count, initialize renderer if needed
     */
    synchronized void incCanvasCount() {
	canvasCount++;
    }
    
    /**
     * Decrement canvas count, kill renderer if needed
     */
    synchronized void decCanvasCount() {
	canvasCount--;
    }

}
