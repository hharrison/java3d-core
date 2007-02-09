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

import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.IllegalComponentStateException;
import javax.vecmath.*;

/**
 * The CanvasViewCache class is used to cache all data, both API data
 * and derived data, that is dependent on the Canvas3D or Screen3D.
 * The final view and projection matrices are stored here.
 */

class CanvasViewCache extends Object {
    // Used for debugging only
    private static Object debugLock = new Object();

    // The canvas associated with this canvas view cache
    private Canvas3D canvas;

    // Mask that indicates this CanvasViewCache view dependence info. has changed,
    // and CanvasViewCache may need to recompute the final view matries.
    int cvcDirtyMask = 0;

    // The screen view cache associated with this canvas view cache
    private ScreenViewCache screenViewCache;

    // The view cache associated with this canvas view cache
    private ViewCache viewCache;

    // *************
    // API/INPUT DATA
    // *************

    // The position and size of the canvas (in pixels)
    private int awtCanvasX;
    private int awtCanvasY;
    private int awtCanvasWidth;
    private int awtCanvasHeight;

    // The current RenderBin used for rendering during the frame
    // associated with this snapshot.
    private RenderBin renderBin;

    // Flag indicating whether or not stereo will be used.  Computed by
    // Canvas3D as: useStereo = stereoEnable && stereoAvailable
    private boolean useStereo;

    // Current monoscopic view policy from canvas
    private int monoscopicViewPolicy;

    // The manual positions of the left and right eyes in image-plate
    // coordinates.
    // Note that these values are only used in non-head-tracked mode
    // when the view's window eyepoint policy is one of RELATIVE_TO_SCREEN
    // or RELATIVE_TO_WINDOW.
    private Point3d leftManualEyeInImagePlate = new Point3d();
    private Point3d rightManualEyeInImagePlate = new Point3d();

    // *************
    // DERIVED DATA
    // *************

    // The width and height of the screen in meters (from ScreenViewCache)
    double physicalScreenWidth;
    double physicalScreenHeight;

    // The width and height of the screen in pixels (from ScreenViewCache)
    int screenWidth;
    int screenHeight;

    // Meters per pixel in the X and Y dimension (from ScreenViewCache)
    double metersPerPixelX;
    double metersPerPixelY;

    // The position and size of the canvas (in pixels)
    private int canvasX;
    private int canvasY;
    private int canvasWidth;
    private int canvasHeight;

    // Either the Canvas' or the View's monoscopicViewPolicy
    private int effectiveMonoscopicViewPolicy;

    // The current cached projection transforms.
    private Transform3D leftProjection = new Transform3D();
    private Transform3D rightProjection = new Transform3D();
    private Transform3D infLeftProjection = new Transform3D();
    private Transform3D infRightProjection = new Transform3D();

    // The current cached viewing transforms.
    private Transform3D leftVpcToEc = new Transform3D();
    private Transform3D rightVpcToEc = new Transform3D();
    private Transform3D infLeftVpcToEc = new Transform3D();
    private Transform3D infRightVpcToEc = new Transform3D();

    // The current cached inverse viewing transforms.
    private Transform3D leftEcToVpc = new Transform3D();
    private Transform3D rightEcToVpc = new Transform3D();
    private Transform3D infLeftEcToVpc = new Transform3D();
    private Transform3D infRightEcToVpc = new Transform3D();

    // Arrays of Vector4d objects that represent the plane equations for
    // the 6 planes in the viewing frustum in ViewPlatform coordinates.
    private Vector4d[] leftFrustumPlanes = new Vector4d[6];
    private Vector4d[] rightFrustumPlanes = new Vector4d[6];

    // Arrays of Vector4d objects that represent the volume of viewing frustum
    private Point4d leftFrustumPoints[] = new Point4d[8];
    private Point4d rightFrustumPoints[] = new Point4d[8];

    // Calibration matrix from Screen object for HMD mode using
    // non-field-sequential stereo

    private Transform3D headTrackerToLeftImagePlate = new Transform3D();
    private Transform3D headTrackerToRightImagePlate = new Transform3D();

    // Head tracked version of eye in imageplate
    private Point3d leftTrackedEyeInImagePlate = new Point3d();
    private Point3d rightTrackedEyeInImagePlate = new Point3d();

    // Derived version of eye in image plate coordinates
    private Point3d leftEyeInImagePlate = new Point3d();
    private Point3d rightEyeInImagePlate = new Point3d();
    private Point3d centerEyeInImagePlate = new Point3d();

    // Derived version of nominalEyeOffsetFromNominalScreen
    private double nominalEyeOffset;

    // Physical window position,size and center (in image plate coordinates)
    private double physicalWindowXLeft;
    private double physicalWindowYBottom;
    private double physicalWindowXRight;
    private double physicalWindowYTop;
    private double physicalWindowWidth;
    private double physicalWindowHeight;
    private Point3d physicalWindowCenter = new Point3d();

    // Screen scale value from viewCache or from screen size.
    private double screenScale;

    // Window scale value that compensates for window size if
    // the window resize policy is PHYSICAL_WORLD.
    private double windowScale;

    // ViewPlatform scale that takes coordinates from view platform
    // coordinates and scales them to physical coordinates
    private double viewPlatformScale;

    // Various derived transforms

    private Transform3D leftCcToVworld = new Transform3D();
    private Transform3D rightCcToVworld = new Transform3D();

    private Transform3D coexistenceToLeftPlate = new Transform3D();
    private Transform3D coexistenceToRightPlate = new Transform3D();

    private Transform3D vpcToCoexistence = new Transform3D();

    private Transform3D vpcToLeftPlate = new Transform3D();
    private Transform3D vpcToRightPlate = new Transform3D();
    private Transform3D leftPlateToVpc = new Transform3D();
    private Transform3D rightPlateToVpc = new Transform3D();
    private Transform3D vworldToLeftPlate = new Transform3D();
    private Transform3D lastVworldToLeftPlate = new Transform3D();
    private Transform3D vworldToRightPlate = new Transform3D();
    private Transform3D leftPlateToVworld = new Transform3D();
    private Transform3D rightPlateToVworld = new Transform3D();
    private Transform3D headToLeftImagePlate = new Transform3D();
    private Transform3D headToRightImagePlate = new Transform3D();

    private Transform3D vworldToTrackerBase = new Transform3D();
    private Transform3D tempTrans = new Transform3D();
    private Transform3D headToVworld = new Transform3D();
    private Vector3d coexistenceCenter = new Vector3d();

    // scale for transformimg clip and fog distances
    private double vworldToCoexistenceScale;
    private double infVworldToCoexistenceScale;

    //
    // Temporary matrices and vectors, so we dont generate garbage
    //
    private Transform3D tMat1 = new Transform3D();
    private Transform3D tMat2 = new Transform3D();
    private Vector3d tVec1 = new Vector3d();
    private Vector3d tVec2 = new Vector3d();
    private Vector3d tVec3 = new Vector3d();
    private Point3d tPnt1 = new Point3d();
    private Point3d tPnt2 = new Point3d();

    private Matrix4d tMatrix = new Matrix4d();

    /**
     * The view platform transforms.
     */
    private Transform3D vworldToVpc = new Transform3D();
    private Transform3D vpcToVworld = new Transform3D();
    private Transform3D infVworldToVpc = new Transform3D();

    // This flag is used to remember the last time doInfinite flag
    // is true or not.
    // If this cache is updated twice, the first time in RenderBin
    // updateViewCache() and the second time in Renderer with
    // geometryBackground. The first time will reset the vcDirtyMask
    // to 0 so that geometry background will not get updated the
    // second time doComputeDerivedData() is invoked when view change.
    private boolean lastDoInfinite = false;
    private boolean updateLastTime = false;

    void getCanvasPositionAndSize() {
	if(J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2) {
	    System.out.println("Get canvas position and size");
	    System.out.println("Before");
	    System.out.println("Canvas pos = (" + awtCanvasX + ", " +
			       awtCanvasY + "), size = " + awtCanvasWidth +
			       "x" + awtCanvasHeight);
	    System.out.println("After");
	}
	awtCanvasX = canvas.newPosition.x;
	awtCanvasY = canvas.newPosition.y;
	awtCanvasWidth = canvas.newSize.width;
	awtCanvasHeight = canvas.newSize.height;

	// The following works around problem when awt creates 0-size
	// window at startup
	if ((awtCanvasWidth <= 0) || (awtCanvasHeight <= 0)) {
	    awtCanvasWidth = 1;
	    awtCanvasHeight = 1;
	}

	if (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_1) {
	    System.out.println("Canvas pos = (" + awtCanvasX + ", " +
			       awtCanvasY + "), size = " + awtCanvasWidth +
			       "x" + awtCanvasHeight);
	}
    }

    void computefrustumBBox(BoundingBox frustumBBox) {
	int i;

	for(i = 0; i < leftFrustumPoints.length; i++) {
	    if(frustumBBox.lower.x > leftFrustumPoints[i].x)
		frustumBBox.lower.x = leftFrustumPoints[i].x;
	    if(frustumBBox.lower.y > leftFrustumPoints[i].y)
		frustumBBox.lower.y = leftFrustumPoints[i].y;
	    if(frustumBBox.lower.z > leftFrustumPoints[i].z)
		frustumBBox.lower.z = leftFrustumPoints[i].z;

	    if(frustumBBox.upper.x < leftFrustumPoints[i].x)
		frustumBBox.upper.x = leftFrustumPoints[i].x;
	    if(frustumBBox.upper.y < leftFrustumPoints[i].y)
		frustumBBox.upper.y = leftFrustumPoints[i].y;
	    if(frustumBBox.upper.z < leftFrustumPoints[i].z)
		frustumBBox.upper.z = leftFrustumPoints[i].z;
	}

	if(useStereo) {

	    for(i = 0; i< rightFrustumPoints.length; i++) {
		if(frustumBBox.lower.x > rightFrustumPoints[i].x)
		    frustumBBox.lower.x = rightFrustumPoints[i].x;
		if(frustumBBox.lower.y > rightFrustumPoints[i].y)
		    frustumBBox.lower.y = rightFrustumPoints[i].y;
		if(frustumBBox.lower.z > rightFrustumPoints[i].z)
		    frustumBBox.lower.z = rightFrustumPoints[i].z;

		if(frustumBBox.upper.x < rightFrustumPoints[i].x)
		    frustumBBox.upper.x = rightFrustumPoints[i].x;
		if(frustumBBox.upper.y < rightFrustumPoints[i].y)
		    frustumBBox.upper.y = rightFrustumPoints[i].y;
		if(frustumBBox.upper.z < rightFrustumPoints[i].z)
		    frustumBBox.upper.z = rightFrustumPoints[i].z;
	    }

	}
    }


    void copyComputedCanvasViewCache(CanvasViewCache cvc, boolean doInfinite) {
	// For performance reason, only data needed by renderer are copied.
	// useStereo,
	// canvasWidth,
	// canvasHeight,
	// leftProjection,
	// rightProjection,
	// leftVpcToEc,
	// rightVpcToEc,
	// leftFrustumPlanes,
	// rightFrustumPlanes,
	// vpcToVworld,
	// vworldToVpc.

	cvc.useStereo = useStereo;
	cvc.canvasWidth = canvasWidth;
	cvc.canvasHeight = canvasHeight;
	cvc.leftProjection.set(leftProjection);
	cvc.rightProjection.set(rightProjection);
	cvc.leftVpcToEc.set(leftVpcToEc) ;
	cvc.rightVpcToEc.set(rightVpcToEc) ;

	cvc.vpcToVworld = vpcToVworld;
	cvc.vworldToVpc.set(vworldToVpc);

        if (doInfinite) {
            cvc.infLeftProjection.set(infLeftProjection);
            cvc.infRightProjection.set(infRightProjection);
            cvc.infLeftVpcToEc.set(infLeftVpcToEc) ;
            cvc.infRightVpcToEc.set(infRightVpcToEc) ;
            cvc.infVworldToVpc.set(infVworldToVpc);
        }

	for (int i = 0; i < leftFrustumPlanes.length; i++) {
	    cvc.leftFrustumPlanes[i].x = leftFrustumPlanes[i].x;
	    cvc.leftFrustumPlanes[i].y = leftFrustumPlanes[i].y;
	    cvc.leftFrustumPlanes[i].z = leftFrustumPlanes[i].z;
	    cvc.leftFrustumPlanes[i].w = leftFrustumPlanes[i].w;

	    cvc.rightFrustumPlanes[i].x = rightFrustumPlanes[i].x;
	    cvc.rightFrustumPlanes[i].y = rightFrustumPlanes[i].y;
	    cvc.rightFrustumPlanes[i].z = rightFrustumPlanes[i].z;
	    cvc.rightFrustumPlanes[i].w = rightFrustumPlanes[i].w;
	}
    }


    /**
     * Take snapshot of all per-canvas API parameters and input values.
     * NOTE: This is probably not needed, but we'll do it for symmetry
     * with the ScreenViewCache and ViewCache objects.
     */
    synchronized void snapshot(boolean computeFrustum) {
        // Issue 109 : determine the the correct index to use -- either the
        // Renderer or RenderBin
        int dirtyIndex = computeFrustum ?
            Canvas3D.RENDER_BIN_DIRTY_IDX : Canvas3D.RENDERER_DIRTY_IDX;

        synchronized (canvas.dirtyMaskLock) {
            // Issue 109 : read/clear the dirty bits for the correct index
            cvcDirtyMask = canvas.cvDirtyMask[dirtyIndex];
            canvas.cvDirtyMask[dirtyIndex] = 0;
        }

        useStereo = canvas.useStereo;
	monoscopicViewPolicy = canvas.monoscopicViewPolicy;
	leftManualEyeInImagePlate.set(canvas.leftManualEyeInImagePlate);
	rightManualEyeInImagePlate.set(canvas.rightManualEyeInImagePlate);

	if(( cvcDirtyMask & Canvas3D.MOVED_OR_RESIZED_DIRTY) != 0) {
	    getCanvasPositionAndSize();
	}

	renderBin = canvas.view.renderBin;

    }

    /**
     * Compute derived data using the snapshot of the per-canvas,
     * per-screen and per-view data.
     */
    synchronized void computeDerivedData(boolean currentFlag,
	CanvasViewCache cvc, BoundingBox frustumBBox, boolean doInfinite) {

	if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_1)) {
	    synchronized(debugLock) {
		System.out.println("------------------------------");
		doComputeDerivedData(currentFlag,cvc,frustumBBox,doInfinite);
	    }
	}
	else {
	    doComputeDerivedData(currentFlag,cvc,frustumBBox,doInfinite);
	}
    }

    /**
     * Compute derived data using the snapshot of the per-canvas,
     * per-screen and per-view data.  Caller must synchronize before
     * calling this method.
     */
    private void doComputeDerivedData(boolean currentFlag,
	CanvasViewCache cvc, BoundingBox frustumBBox, boolean doInfinite) {

        // Issue 109 : determine the the correct index to use -- either the
        // Renderer or RenderBin
        int dirtyIndex = (frustumBBox != null) ?
            Canvas3D.RENDER_BIN_DIRTY_IDX : Canvas3D.RENDERER_DIRTY_IDX;
        int scrvcDirtyMask;
        
        // Issue 109 : read/clear the dirty bits for the correct index
        synchronized (screenViewCache) {
            scrvcDirtyMask = screenViewCache.scrvcDirtyMask[dirtyIndex];
            // reset screen view dirty mask if canvas is offScreen. Note:
            // there is only one canvas per offscreen, so it is ok to
            // do the reset here.
            if (canvas.offScreen) {
                screenViewCache.scrvcDirtyMask[dirtyIndex] = 0;
            }
        }

        if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
	    if(cvcDirtyMask != 0)
		System.out.println("cvcDirtyMask : " +  cvcDirtyMask);

	    if(scrvcDirtyMask != 0)
		System.out.println("scrvcDirtyMask : "+ scrvcDirtyMask);

	    if(viewCache.vcDirtyMask != 0)
		System.out.println("vcDirtyMask : " +  viewCache.vcDirtyMask);
	}


	// NOTE: This fix is only fixing the symptoms, but not the
	// root of the bug.  We shouldn't have to check for null here.
	if(viewCache.vpRetained == null) {
	     System.out.println("CanvasViewCache : Error! viewCache.vpRetained is null");
	    return;
	}

	// This flag is use to force a computation when a ViewPlatformTransform
	// is detected. No sync. needed. We're doing a read of t/f.
	// XXXX: Peeking at the dirty flag is a hack. Need to revisit this.
	boolean vprNotDirty = (viewCache.vpRetained.vprDirtyMask == 0);

        // Issue 131: If not manual, it has to be considered as an onscreen canvas.
	if(!canvas.manualRendering &&
	   (vprNotDirty) &&
	   (cvcDirtyMask == 0) &&
	   (scrvcDirtyMask == 0) &&
	   (viewCache.vcDirtyMask == 0) &&
	    !(updateLastTime && (doInfinite != lastDoInfinite))) {
	    if(frustumBBox != null)
		computefrustumBBox(frustumBBox);

	    // Copy the computed data into cvc.
	    if(cvc != null) {
		copyComputedCanvasViewCache(cvc, doInfinite);
	    }
	    lastDoInfinite = doInfinite;
	    updateLastTime = false;
	    return;
	}

	lastDoInfinite = doInfinite;
	updateLastTime = true;

	if(currentFlag) {
	    vpcToVworld.set(viewCache.vpRetained.getCurrentLocalToVworld(null));
	}
	else {
	    vpcToVworld.set(viewCache.vpRetained.getLastLocalToVworld(null));
	}

	// System.out.println("vpcToVworld is \n" + vpcToVworld);

        try {
	    vworldToVpc.invert(vpcToVworld);
	}
	catch (SingularMatrixException e) {
	    vworldToVpc.setIdentity();
	    //System.out.println("SingularMatrixException encountered when doing vworldToVpc invert");
	}
        if (doInfinite) {
            vworldToVpc.getRotation(infVworldToVpc);
	}

	// Compute global flags
	if (monoscopicViewPolicy == View.CYCLOPEAN_EYE_VIEW)
	    effectiveMonoscopicViewPolicy = viewCache.monoscopicViewPolicy;
	else
	    effectiveMonoscopicViewPolicy = monoscopicViewPolicy;

	// Recompute info about current canvas window
	computeCanvasInfo();

	// Compute coexistence center (in plate coordinates)
	computeCoexistenceCenter();

	// Get Eye position in image-plate coordinates
	cacheEyePosition();

	// Compute VPC to COE and COE to PLATE transforms
	computeVpcToCoexistence();
	computeCoexistenceToPlate();

	// Compute view and projection matrices
	computeView(doInfinite);


	computePlateToVworld();

	if (!currentFlag) {
	    // save the result for use in RasterRetained computeWinCoord
	    lastVworldToLeftPlate.set(vworldToLeftPlate);
	}
	computeHeadToVworld();

	if (frustumBBox != null)
	    computefrustumBBox(frustumBBox);

	// Issue 109: cvc should *always* be null
        assert cvc == null;
	if(cvc != null)
	    copyComputedCanvasViewCache(cvc, doInfinite);

	canvas.canvasDirty |= Canvas3D.VIEW_MATRIX_DIRTY;

	if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_1)) {
	    // Print some data :
	    System.out.println("useStereo = " + useStereo);
	    System.out.println("leftProjection:\n" + leftProjection);
	    System.out.println("rightProjection:\n " + rightProjection);
	    System.out.println("leftVpcToEc:\n" + leftVpcToEc);
	    System.out.println("rightVpcToEc:\n" + rightVpcToEc);
	    System.out.println("vpcToVworld:\n" + vpcToVworld);
	    System.out.println("vworldToVpc:\n" + vworldToVpc);

	    if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
		int i;
		for (i = 0; i < leftFrustumPlanes.length; i++) {
		    System.out.println("leftFrustumPlanes " + i + " is " +
				       leftFrustumPlanes[i]);
		}

		for (i = 0; i < rightFrustumPlanes.length; i++) {
		    System.out.println("rightFrustumPlanes " + i + " is " +
				       rightFrustumPlanes[i]);
		}
	    }
	}

    }

    private void computeCanvasInfo() {
	// Copy the screen width and height info into derived parameters
	physicalScreenWidth = screenViewCache.physicalScreenWidth;
	physicalScreenHeight = screenViewCache.physicalScreenHeight;

	screenWidth = screenViewCache.screenWidth;
	screenHeight = screenViewCache.screenHeight;

	metersPerPixelX = screenViewCache.metersPerPixelX;
	metersPerPixelY = screenViewCache.metersPerPixelY;

	// If a multi-screen virtual device (e.g. Xinerama) is being used,
	// then awtCanvasX and awtCanvasY are relative to the origin of that
	// virtual screen.  Subtract the origin of the physical screen to
	// compute the origin in physical (image plate) coordinates.
	Rectangle screenBounds = canvas.graphicsConfiguration.getBounds();
	canvasX = awtCanvasX - screenBounds.x;
	canvasY = awtCanvasY - screenBounds.y;

	// Use awtCanvasWidth and awtCanvasHeight as reported.
	canvasWidth = awtCanvasWidth;
	canvasHeight = awtCanvasHeight;

	// Convert the window system ``pixel'' coordinate location and size
	// of the window into physical units (meters) and coordinate system.

	// Window width and Height in meters
	physicalWindowWidth = canvasWidth * metersPerPixelX;
	physicalWindowHeight = canvasHeight * metersPerPixelY;

	// Compute the 4 corners of the window in physical units
	physicalWindowXLeft = metersPerPixelX *
	    (double) canvasX;
	physicalWindowYBottom = metersPerPixelY *
	    (double)(screenHeight - canvasHeight - canvasY);

	physicalWindowXRight = physicalWindowXLeft + physicalWindowWidth;
	physicalWindowYTop = physicalWindowYBottom + physicalWindowHeight;

	//  Cache the physical location of the center of the window
	physicalWindowCenter.x =
	    physicalWindowXLeft + physicalWindowWidth / 2.0;
	physicalWindowCenter.y =
	    physicalWindowYBottom + physicalWindowHeight / 2.0;
	physicalWindowCenter.z = 0.0;

	if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
	    System.out.println("Canvas pos = (" + awtCanvasX + ", " +
			       awtCanvasY + "), size = " + awtCanvasWidth +
			       "x" + awtCanvasHeight);

	    System.out.println("Window LL corner (in plate coordinates): " +
		"(" + physicalWindowXLeft + "," + physicalWindowYBottom + ")");

	    System.out.println("Window size (in plate coordinates): " +
		"(" + physicalWindowWidth + "," + physicalWindowHeight + ")");

	    System.out.println("Window center (in plate coordinates): " +
			       physicalWindowCenter);

	    System.out.println();
	}

	// Compute the view platform scale.  This combines
	// the screen scale and the window scale.
	computeViewPlatformScale();

	if (!viewCache.compatibilityModeEnable &&
	    viewCache.viewPolicy == View.HMD_VIEW) {
	    if (!useStereo) {
		switch(effectiveMonoscopicViewPolicy) {
		case View.CYCLOPEAN_EYE_VIEW:
		    if(J3dDebug.devPhase) {
			System.out.println("CanvasViewCache : Should never reach here.\n" +
					   "HMD_VIEW with CYCLOPEAN_EYE_VIEW is not allowed");
		    }
		    break;

		case View.LEFT_EYE_VIEW:
		    headTrackerToLeftImagePlate.set(screenViewCache.
						    headTrackerToLeftImagePlate);
		    break;

		case View.RIGHT_EYE_VIEW:
 		    headTrackerToLeftImagePlate.set(screenViewCache.
						    headTrackerToRightImagePlate);
		    break;
		}
	    }
	    else {
		headTrackerToLeftImagePlate.set(screenViewCache.
						headTrackerToLeftImagePlate);

		headTrackerToRightImagePlate.set(screenViewCache.
						 headTrackerToRightImagePlate);
 	    }

	}
    }

    // Routine to compute the center of coexistence coordinates in
    // imageplate coordinates.  Also compute the scale from Vpc
    private void computeViewPlatformScale() {
	windowScale = screenScale = 1.0;

	if (!viewCache.compatibilityModeEnable) {
	    switch (viewCache.screenScalePolicy) {
	    case View.SCALE_SCREEN_SIZE:
		screenScale = physicalScreenWidth / 2.0;
		break;
	    case View.SCALE_EXPLICIT:
		screenScale = viewCache.screenScale;
		break;
	    }

	    if (viewCache.windowResizePolicy == View.PHYSICAL_WORLD) {
		windowScale = physicalWindowWidth / physicalScreenWidth;
	    }
	}

	viewPlatformScale = windowScale * screenScale;
	if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
	    System.out.println("viewCache.windowResizePolicy = " +
			       viewCache.windowResizePolicy);
	    System.out.println("physicalWindowWidth = " + physicalWindowWidth);
	    System.out.println("physicalScreenWidth = " + physicalScreenWidth);
	    System.out.println("windowScale = " + windowScale);
	    System.out.println("screenScale = " + screenScale);
	    System.out.println("viewPlatformScale = " + viewPlatformScale);
	}
    }

    private void cacheEyePosFixedField() {
	if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_1))
	    System.out.println("cacheEyePosFixedField:");

	// y is always the window center
	rightEyeInImagePlate.y =
	    leftEyeInImagePlate.y =
	    physicalWindowCenter.y;

	if (!useStereo) {
	    switch(effectiveMonoscopicViewPolicy) {
	    case View.CYCLOPEAN_EYE_VIEW:
		leftEyeInImagePlate.x = physicalWindowCenter.x;
		break;

	    case View.LEFT_EYE_VIEW:
		leftEyeInImagePlate.x =
		    physicalWindowCenter.x + viewCache.leftEyePosInHead.x;
		break;

	    case View.RIGHT_EYE_VIEW:
		leftEyeInImagePlate.x =
		    physicalWindowCenter.x + viewCache.rightEyePosInHead.x;
		break;
	    }

	    // Set right as well just in case
	    rightEyeInImagePlate.x = leftEyeInImagePlate.x;
	}
	else {
	    leftEyeInImagePlate.x =
		physicalWindowCenter.x + viewCache.leftEyePosInHead.x;

	    rightEyeInImagePlate.x =
		physicalWindowCenter.x + viewCache.rightEyePosInHead.x;
	}

	//
	// Derive the z distance by constraining the field of view of the
	// window width to be constant.
	//
	rightEyeInImagePlate.z =
	    leftEyeInImagePlate.z =
	    physicalWindowWidth /
	    (2.0 * Math.tan(viewCache.fieldOfView / 2.0));

        // Denote that eyes-in-ImagePlate fields have changed so that
	// these new values can be sent to the AudioDevice
        if (this.viewCache.view.soundScheduler != null)
            this.viewCache.view.soundScheduler.setListenerFlag(
                 SoundScheduler.EYE_POSITIONS_CHANGED);
    }

    /**
     *  Case of view eye position contrainted to center of window, but
     *  with z distance from plate eye pos.
     */
    private void cacheEyePosWindowRelative() {

	if ((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_1))
	    System.out.println("cacheEyePosWindowRelative:");

	// y is always the window center
	rightEyeInImagePlate.y =
	    leftEyeInImagePlate.y =
	    physicalWindowCenter.y;

	// z is always from the existing eye pos
	rightEyeInImagePlate.z =
	    leftEyeInImagePlate.z =
	    leftManualEyeInImagePlate.z;

	if (!useStereo) {

	    switch(effectiveMonoscopicViewPolicy) {

	    case View.CYCLOPEAN_EYE_VIEW:
		leftEyeInImagePlate.x =
		    physicalWindowCenter.x;
		break;

	    case View.LEFT_EYE_VIEW:
		leftEyeInImagePlate.x =
		    physicalWindowCenter.x +
		    viewCache.leftEyePosInHead.x;
		break;

	    case View.RIGHT_EYE_VIEW:
		leftEyeInImagePlate.x =
		    physicalWindowCenter.x +
		    viewCache.rightEyePosInHead.x;
		    break;

	    }

	    // Set right as well just in case
	    rightEyeInImagePlate.x =
		leftEyeInImagePlate.x;

	}
	else {

	    leftEyeInImagePlate.x =
		physicalWindowCenter.x +
		viewCache.leftEyePosInHead.x;

	    rightEyeInImagePlate.x =
		physicalWindowCenter.x +
		viewCache.rightEyePosInHead.x;

	    // Right z gets its own value
	    rightEyeInImagePlate.z =
		rightManualEyeInImagePlate.z;
	}

	// Denote that eyes-in-ImagePlate fields have changed so that
	// these new values can be sent to the AudioDevice
        if (this.viewCache.view.soundScheduler != null)
            this.viewCache.view.soundScheduler.setListenerFlag(
                 SoundScheduler.EYE_POSITIONS_CHANGED);
    }

    /**
     * Common routine used when head tracking and when using manual
     * relative_to_screen eyepoint policy.
     */
    private void cacheEyePosScreenRelative(Point3d leftEye, Point3d rightEye) {
	if ((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_1))
	    System.out.println("cacheEyePosScreenRelative:");

	if (!useStereo) {
	    switch(effectiveMonoscopicViewPolicy) {

	    case View.CYCLOPEAN_EYE_VIEW:
		leftEyeInImagePlate.x = (leftEye.x + rightEye.x) / 2.0;
		leftEyeInImagePlate.y = (leftEye.y + rightEye.y) / 2.0;
		leftEyeInImagePlate.z = (leftEye.z + rightEye.z) / 2.0;
		break;

	    case View.LEFT_EYE_VIEW:
		leftEyeInImagePlate.set(leftEye);
		break;

	    case View.RIGHT_EYE_VIEW:
		leftEyeInImagePlate.set(rightEye);
		break;

	    }

	    // Set right as well just in case
	    rightEyeInImagePlate.set(leftEyeInImagePlate);
	}
	else {
	    leftEyeInImagePlate.set(leftEye);
	    rightEyeInImagePlate.set(rightEye);
	}

	// Denote that eyes-in-ImagePlate fields have changed so that
	// these new values can be sent to the AudioDevice
        if (this.viewCache.view.soundScheduler != null)
            this.viewCache.view.soundScheduler.setListenerFlag(
                 SoundScheduler.EYE_POSITIONS_CHANGED);
    }

    private void cacheEyePosCoexistenceRelative(Point3d leftManualEyeInCoexistence,
						Point3d rightManualEyeInCoexistence) {

	tPnt1.set(leftManualEyeInCoexistence);
	viewCache.coexistenceToTrackerBase.transform(tPnt1);
	screenViewCache.trackerBaseToImagePlate.transform(tPnt1);
	tPnt1.add(coexistenceCenter);

	tPnt2.set(rightManualEyeInCoexistence);
	viewCache.coexistenceToTrackerBase.transform(tPnt2);
	screenViewCache.trackerBaseToImagePlate.transform(tPnt2);
	tPnt2.add(coexistenceCenter);

	cacheEyePosScreenRelative(tPnt1, tPnt2);

    }

    /**
     * Compute the head-tracked eye position for the right and
     * left eyes.
     */
    private void computeTrackedEyePosition() {
	if ((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
	    System.out.println("computeTrackedEyePosition:");
	    System.out.println("viewCache.headTrackerToTrackerBase:");
	    System.out.println(viewCache.headTrackerToTrackerBase);

	    System.out.println("viewCache.headToHeadTracker:");
	    System.out.println(viewCache.headToHeadTracker);
	}

	if (viewCache.viewPolicy != View.HMD_VIEW) {
	    if ((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
		System.out.println("screenViewCache.trackerBaseToImagePlate:");
		System.out.println(screenViewCache.trackerBaseToImagePlate);
	    }

	    headToLeftImagePlate.set(coexistenceCenter);
	    headToLeftImagePlate.mul(screenViewCache.trackerBaseToImagePlate);
	    headToLeftImagePlate.mul(viewCache.headTrackerToTrackerBase);
	    headToLeftImagePlate.mul(viewCache.headToHeadTracker);

	    headToLeftImagePlate.transform(viewCache.leftEyePosInHead,
					   leftTrackedEyeInImagePlate);

	    headToLeftImagePlate.transform(viewCache.rightEyePosInHead,
					   rightTrackedEyeInImagePlate);
	}
	else {
	    if ((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
		System.out.println("headTrackerToLeftImagePlate:");
		System.out.println(headTrackerToLeftImagePlate);
	    }

	    headToLeftImagePlate.mul(headTrackerToLeftImagePlate,
				     viewCache.headToHeadTracker);

	    headToLeftImagePlate.transform(viewCache.leftEyePosInHead,
					   leftTrackedEyeInImagePlate);

	    if(useStereo) {
		headToRightImagePlate.mul(headTrackerToRightImagePlate,
					  viewCache.headToHeadTracker);

		headToRightImagePlate.transform(viewCache.rightEyePosInHead,
						rightTrackedEyeInImagePlate);
	    }
	    else { // HMD_VIEW with no stereo.
		headToLeftImagePlate.transform(viewCache.rightEyePosInHead,
					       rightTrackedEyeInImagePlate);
	    }

	}

	if ((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
	    System.out.println("headToLeftImagePlate:");
	    System.out.println(headToLeftImagePlate);
	    System.out.println("headToRightImagePlate:");
	    System.out.println(headToRightImagePlate);

	}
    }

    /**
     * Routine to cache the current eye position in image plate
     * coordinates.
     */
    private void cacheEyePosition() {
	if (viewCache.compatibilityModeEnable) {
	    // XXXX: Compute compatibility mode eye position in ImagePlate???
	    cacheEyePosScreenRelative(leftManualEyeInImagePlate,
				      rightManualEyeInImagePlate);
	}
	else if (viewCache.getDoHeadTracking()) {
	    computeTrackedEyePosition();
	    cacheEyePosScreenRelative(leftTrackedEyeInImagePlate,
				      rightTrackedEyeInImagePlate);
	}
	else {
	    switch (viewCache.windowEyepointPolicy) {

	    case View.RELATIVE_TO_FIELD_OF_VIEW:
		cacheEyePosFixedField();
		break;

	    case View.RELATIVE_TO_WINDOW:
		cacheEyePosWindowRelative();
		break;

	    case View.RELATIVE_TO_SCREEN:
		cacheEyePosScreenRelative(leftManualEyeInImagePlate,
					  rightManualEyeInImagePlate);
		break;

	    case View.RELATIVE_TO_COEXISTENCE:
 		cacheEyePosCoexistenceRelative(viewCache.leftManualEyeInCoexistence,
					       viewCache.rightManualEyeInCoexistence);
		break;
	    }
	}

	// Compute center eye
	centerEyeInImagePlate.add(leftEyeInImagePlate, rightEyeInImagePlate);
	centerEyeInImagePlate.scale(0.5);

	// Compute derived value of nominalEyeOffsetFromNominalScreen
	if (viewCache.windowEyepointPolicy == View.RELATIVE_TO_FIELD_OF_VIEW)
	    nominalEyeOffset = centerEyeInImagePlate.z;
	else
	    nominalEyeOffset = viewCache.nominalEyeOffsetFromNominalScreen;

	if ((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_1)) {
	    System.out.println("leftEyeInImagePlate = " +
			       leftEyeInImagePlate);
	    System.out.println("rightEyeInImagePlate = " +
			       rightEyeInImagePlate);
	    System.out.println("centerEyeInImagePlate = " +
			       centerEyeInImagePlate);
	    System.out.println("nominalEyeOffset = " +
			       nominalEyeOffset);
	    System.out.println();
	}
    }

    private void computePlateToVworld() {
	if (viewCache.compatibilityModeEnable) {
	    // XXXX: implement this correctly for compat mode
	    leftPlateToVworld.setIdentity();
	    vworldToLeftPlate.setIdentity();
	}
	else {
	    try {
		leftPlateToVpc.invert(vpcToLeftPlate);
	    }
	    catch (SingularMatrixException e) {
		leftPlateToVpc.setIdentity();
		/*
		  System.out.println("SingularMatrixException encountered when doing" +
		  " leftPlateToVpc invert");
		  */
	    }

	    leftPlateToVworld.mul(vpcToVworld, leftPlateToVpc);
	    vworldToLeftPlate.mul(vpcToLeftPlate, vworldToVpc);

	    if(useStereo) {
		try {
		    rightPlateToVpc.invert(vpcToRightPlate);
		}
		catch (SingularMatrixException e) {
		    rightPlateToVpc.setIdentity();
		    /*
		      System.out.println("SingularMatrixException encountered when doing" +
		      " rightPlateToVpc invert");
		      */
		}

		rightPlateToVworld.mul(vpcToVworld, rightPlateToVpc);
		vworldToRightPlate.mul(vpcToRightPlate, vworldToVpc);

	    }

	    if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
		System.out.println("vpcToVworld:");
		System.out.println(vpcToVworld);
		System.out.println("vpcToLeftPlate:");
		System.out.println(vpcToLeftPlate);
		if(useStereo) {
		    System.out.println("vpcToRightPlate:");
		    System.out.println(vpcToRightPlate);

		}

	    }
	}

	// Denote that eyes-in-ImagePlate fields have changed so that
	// these new values can be sent to the AudioDevice
        if (this.viewCache.view.soundScheduler != null)
            this.viewCache.view.soundScheduler.setListenerFlag(
                 SoundScheduler.IMAGE_PLATE_TO_VWORLD_CHANGED);
    }


    private void computeHeadToVworld() {
        // Concatenate headToLeftImagePlate with leftPlateToVworld

	if (viewCache.compatibilityModeEnable) {
	    // XXXX: implement this correctly for compat mode
	    headToVworld.setIdentity();
	}
	else {
	    headToVworld.mul(leftPlateToVworld, headToLeftImagePlate);

	    if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
		System.out.println("leftPlateToVworld:");
		System.out.println(leftPlateToVworld);
		System.out.println("headToLeftImagePlate:");
		System.out.println(headToLeftImagePlate);
		System.out.println("...gives -> headToVworld:");
		System.out.println(headToVworld);
	    }
	}

	// Denote that eyes-in-ImagePlate fields have changed so that
	// these new values can be sent to the AudioDevice
        if (this.viewCache.view.soundScheduler != null)
            this.viewCache.view.soundScheduler.setListenerFlag(
                 SoundScheduler.HEAD_TO_VWORLD_CHANGED);
    }

    private void computeVpcToCoexistence() {
	// Create a transform with the view platform to coexistence scale
	tMat1.set(viewPlatformScale);

	// XXXX: Is this really correct to ignore HMD?

	if (viewCache.viewPolicy != View.HMD_VIEW) {
	    switch (viewCache.coexistenceCenterInPworldPolicy) {
	    case View.NOMINAL_SCREEN :
		switch (viewCache.viewAttachPolicy) {
		case View.NOMINAL_SCREEN:
		    tMat2.setIdentity();
		    break;
		case View.NOMINAL_HEAD:
		    tVec1.set(0.0, 0.0, nominalEyeOffset);
		    tMat2.set(tVec1);
		    break;
		case View.NOMINAL_FEET:
		    tVec1.set(0.0, -viewCache.nominalEyeHeightFromGround,
			      nominalEyeOffset);
		    tMat2.set(tVec1);
		    break;
		}

		break;
	    case View.NOMINAL_HEAD :
		switch (viewCache.viewAttachPolicy) {
		case View.NOMINAL_SCREEN:
		    tVec1.set(0.0, 0.0, -nominalEyeOffset);
		    tMat2.set(tVec1);
		    break;
		case View.NOMINAL_HEAD:
		    tMat2.setIdentity();
		    break;
		case View.NOMINAL_FEET:
		    tVec1.set(0.0, -viewCache.nominalEyeHeightFromGround,
			      0.0);
		    tMat2.set(tVec1);
		    break;
		}
		break;
	      case View.NOMINAL_FEET:
		switch (viewCache.viewAttachPolicy) {
		case View.NOMINAL_SCREEN:
		    tVec1.set(0.0,
			      viewCache.nominalEyeHeightFromGround, -nominalEyeOffset);
		    tMat2.set(tVec1);
		    break;
		case View.NOMINAL_HEAD:
		    tVec1.set(0.0, viewCache.nominalEyeHeightFromGround,
			      0.0);
		    tMat2.set(tVec1);

		    break;
		case View.NOMINAL_FEET:
		    tMat2.setIdentity();
		    break;
		}
		break;
	    }

	    vpcToCoexistence.mul(tMat2, tMat1);
	}
	else {
	    vpcToCoexistence.set(tMat1);
	}

	if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
	    System.out.println("vpcToCoexistence:");
	    System.out.println(vpcToCoexistence);
	}
    }

    private void computeCoexistenceCenter() {

	if ((!viewCache.compatibilityModeEnable) &&
	    (viewCache.viewPolicy != View.HMD_VIEW) &&
	    (viewCache.coexistenceCenteringEnable) &&
	    (viewCache.coexistenceCenterInPworldPolicy == View.NOMINAL_SCREEN)) {

	    // Compute the coexistence center in image plate coordinates

	    // Image plate cordinates have their orgin in the lower
	    // left hand corner of the CRT visiable raster.
	    // The nominal coexistence center is at the *center* of
	    // targeted area: either the window or screen, depending
	    // on policy.
	    if (viewCache.windowMovementPolicy == View.VIRTUAL_WORLD) {
		coexistenceCenter.x = physicalScreenWidth / 2.0;
		coexistenceCenter.y = physicalScreenHeight / 2.0;
		coexistenceCenter.z = 0.0;
	    }
	    else { // windowMovementPolicy == PHYSICAL_WORLD
		coexistenceCenter.x = physicalWindowCenter.x;
		coexistenceCenter.y = physicalWindowCenter.y;
		coexistenceCenter.z = 0.0;
	    }
	}
	else {
	    coexistenceCenter.set(0.0, 0.0, 0.0);
	}

	if(J3dDebug.devPhase) {
	    if (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_1) {
		System.out.println("coexistenceCenter = " + coexistenceCenter);
	    }
	}
    }

    private void computeCoexistenceToPlate() {
	if (viewCache.compatibilityModeEnable) {
	    // XXXX: implement this correctly
	    coexistenceToLeftPlate.setIdentity();
	    return;
	}

	if (viewCache.viewPolicy != View.HMD_VIEW) {
	    coexistenceToLeftPlate.set(coexistenceCenter);
	    coexistenceToLeftPlate.mul(screenViewCache.trackerBaseToImagePlate);
	    coexistenceToLeftPlate.mul(viewCache.coexistenceToTrackerBase);

	    if(useStereo) {
		coexistenceToRightPlate.set(coexistenceToLeftPlate);
	    }
	}
	else {
	    coexistenceToLeftPlate.mul(headTrackerToLeftImagePlate,
				       viewCache.trackerBaseToHeadTracker);
	    coexistenceToLeftPlate.mul(viewCache.coexistenceToTrackerBase);

	    if(useStereo) {
		coexistenceToRightPlate.mul(headTrackerToRightImagePlate,
					    viewCache.trackerBaseToHeadTracker);
		coexistenceToRightPlate.mul(viewCache.coexistenceToTrackerBase);
	    }
	}

	if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
	    System.out.println("coexistenceToLeftPlate:");
	    System.out.println(coexistenceToLeftPlate);
	    if(useStereo) {
		System.out.println("coexistenceToRightPlate:");
		System.out.println(coexistenceToRightPlate);

	    }
	}
    }

    /**
     * Computes the viewing matrices.
     *
     * computeView computes the following:
     *
     * <ul>
     * left (& right) eye viewing matrices (only left is valid for mono view)
     * </ul>
     *
     * This call works for both fixed screen and HMD displays.
     */
    private void computeView(boolean doInfinite) {
	int		i,j;
	int		backClipPolicy;
	double		Fl, Fr, B, scale, backClipDistance;

        // compute scale used for transforming clip and fog distances
        vworldToCoexistenceScale = vworldToVpc.getDistanceScale()
                * vpcToCoexistence.getDistanceScale();
        if(doInfinite) {
            infVworldToCoexistenceScale = infVworldToVpc.getDistanceScale()
                * vpcToCoexistence.getDistanceScale();
        }

	if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
	    System.out.println("vworldToCoexistenceScale = " +
			       vworldToCoexistenceScale);
	}

        // compute coexistenceToVworld transform -- dirty bit candidate!!
        tempTrans.mul(viewCache.coexistenceToTrackerBase, vpcToCoexistence);
        vworldToTrackerBase.mul(tempTrans, vworldToVpc);

	// If we are in compatibility mode, compute the view and
	// projection matrices accordingly
	if (viewCache.compatibilityModeEnable) {
	    leftProjection.set(viewCache.compatLeftProjection);
	    leftVpcToEc.set(viewCache.compatVpcToEc);

	    if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_1)) {
		System.out.println("Left projection and view matrices");
		System.out.println("ecToCc (leftProjection) :");
		System.out.println(leftProjection);
		System.out.println("vpcToEc:");
		System.out.println(leftVpcToEc);
	    }

	    computeFrustumPlanes(leftProjection, leftVpcToEc,
				 leftFrustumPlanes, leftFrustumPoints,
                                 leftCcToVworld);

	    if(useStereo) {
		rightProjection.set(viewCache.compatRightProjection);
		rightVpcToEc.set(viewCache.compatVpcToEc);

		if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_1)) {
		    System.out.println("Right projection and view matrices");
		    System.out.println("ecToCc:");
		    System.out.println("vpcToEc:");
		    System.out.println(rightVpcToEc);
		}

		computeFrustumPlanes(rightProjection, rightVpcToEc,
				     rightFrustumPlanes, rightFrustumPoints,
                                     rightCcToVworld);
	    }

	    return;
	}

	//
	//  The clipping plane distances are set from the internal policy.
	//
	//  Note that the plane distance follows the standard Z axis
	//  convention, e.g. negative numbers further away.
	//  Note that for policy from eye, the distance is negative in
	//  the direction of z in front of the eye.
	//  Note that for policy from screen, the distance is negative for
	//  locations behind the screen, and positive in front.
	//
	//  The distance attributes are measured either in physical (plate)
	//  units, or vworld units.
	//

	// Compute scale factor for front clip plane computation
	if (viewCache.frontClipPolicy == View.VIRTUAL_EYE ||
	    viewCache.frontClipPolicy == View.VIRTUAL_SCREEN) {
            scale = vworldToCoexistenceScale;
	}
	else {
	    scale = windowScale;
	}

	// Set left and right front clipping plane distances.
	if(viewCache.frontClipPolicy == View.PHYSICAL_EYE ||
	   viewCache.frontClipPolicy == View.VIRTUAL_EYE) {
	    Fl = leftEyeInImagePlate.z +
		scale * -viewCache.frontClipDistance;
	    Fr = rightEyeInImagePlate.z +
		scale * -viewCache.frontClipDistance;
	}
	else {
	    Fl = scale * -viewCache.frontClipDistance;
	    Fr = scale * -viewCache.frontClipDistance;
	}

        // if there is an active clip node, use it and ignore the view's
        // backclip
        if ((renderBin != null) && (renderBin.backClipActive)) {
            backClipPolicy = View.VIRTUAL_EYE;
            backClipDistance = renderBin.backClipDistanceInVworld;
        } else {
            backClipPolicy = viewCache.backClipPolicy;
            backClipDistance = viewCache.backClipDistance;
        }

	// Compute scale factor for rear clip plane computation
	if (backClipPolicy == View.VIRTUAL_EYE ||
	    backClipPolicy == View.VIRTUAL_SCREEN) {
            scale = vworldToCoexistenceScale;
	}
	else {
	    scale = windowScale;
	}

	// Set left and right rear clipping plane distnaces.
	if(backClipPolicy == View.PHYSICAL_EYE ||
	   backClipPolicy == View.VIRTUAL_EYE) {
	    // Yes, left for both left and right rear.
	    B = leftEyeInImagePlate.z +
		scale * -backClipDistance;
	}
	else {
	    B = scale * -backClipDistance;
	}

	// XXXX: Can optimize for HMD case.
	if (true /*viewCache.viewPolicy != View.HMD_VIEW*/) {

	    // Call buildProjView to build the projection and view matrices.

	    if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
		System.out.println("Left projection and view matrices");
		System.out.println("Fl " + Fl + " B " + B);
		System.out.println("leftEyeInImagePlate\n" + leftEyeInImagePlate);
		System.out.println("Before : leftProjection\n" + leftProjection);
		System.out.println("Before leftVpcToEc\n" + leftVpcToEc);
	    }

	    buildProjView(leftEyeInImagePlate, coexistenceToLeftPlate,
			  vpcToLeftPlate, Fl, B, leftProjection, leftVpcToEc, false);


	    if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
		System.out.println("After : leftProjection\n" + leftProjection);
		System.out.println("After leftVpcToEc\n" + leftVpcToEc);
	    }

	    computeFrustumPlanes(leftProjection, leftVpcToEc,
				 leftFrustumPlanes, leftFrustumPoints,
                                 leftCcToVworld);

	    if(useStereo) {
		if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2))
		    System.out.println("Right projection and view matrices");

		buildProjView(rightEyeInImagePlate, coexistenceToRightPlate,
			      vpcToRightPlate, Fr, B, rightProjection,
			      rightVpcToEc, false);

		computeFrustumPlanes(rightProjection, rightVpcToEc,
				     rightFrustumPlanes, rightFrustumPoints,
                                     rightCcToVworld);
	    }

	    //
	    // Now to compute the left (& right) eye (and infinite)
	    // viewing matrices.
	    if(doInfinite) {
		// Call buildProjView separately for infinite view
		buildProjView(leftEyeInImagePlate, coexistenceToLeftPlate,
			      vpcToLeftPlate, leftEyeInImagePlate.z - 0.05,
			      leftEyeInImagePlate.z - 1.5,
			      infLeftProjection, infLeftVpcToEc, true);

		if(useStereo) {
		    buildProjView(rightEyeInImagePlate, coexistenceToRightPlate,
				  vpcToRightPlate, rightEyeInImagePlate.z - 0.05,
				  rightEyeInImagePlate.z - 1.5,
				  infRightProjection, infRightVpcToEc, true);

		}
	    }
	}
	// XXXX: The following code has never been ported
//      else {
//	    Point3d cen_eye;
//
//	    // HMD case.  Just concatenate the approprate matrices together.
//	    // Additional work just for now
//
//	    compute_lr_plate_to_cc( &cen_eye, Fl, B, 0, &vb, 0);
//
//	    if(useStereo) {
//		mat_mul_dpt(&right_eye_pos_in_head,
//			    head_to_right_plate, &cen_eye);
//		compute_lr_plate_to_cc( &cen_eye, Fr, B,
//				       1, &vb, 0);
//	    }
//
// 	    //  Make sure that coexistence_to_plate is current.
// 	    //  (It is usually constant for fixed plates, always varies for HMDs.)
// 	    //  For HMD case, computes finial matrices that will be used.
// 	    //
// 	    computeCoexistenceToPlate();
//	}

    }

    /**
     * Debugging routine to analyze the projection matrix.
     */
    private void analyzeProjection(Transform3D p, double xMax) {
	if (viewCache.projectionPolicy == View.PARALLEL_PROJECTION)
	    System.out.println("PARALLEL_PROJECTION =");
	else
	    System.out.println("PERSPECTIVE_PROJECTION =");

	System.out.println(p);

	double projectionPlaneZ = ((p.mat[0] * xMax + p.mat[3] - p.mat[15]) /
				   (p.mat[14] - p.mat[2]));

	System.out.println("projection plane at z = " + projectionPlaneZ);
    }

    /**
     *  buildProjView creates a projection and viewing matrix.
     *
     *  Inputs:
     *     ep :		    eye point, in plate coordinates
     * coe2Plate :          matrix from coexistence to image plate.
     *     F, B :	    front, back clipping planes, in plate coordinates
     *     doInfinite :	    flag to indicate ``at infinity'' view desired
     *
     *  Output:
     *   vpc2Plate :       matric from vpc to image plate.
     *     ecToCc :	    projection matrix from Eye Coordinates (EC)
     *			    to Clipping Coordinates (CC)
     *     vpcToEc :        view matrix from ViewPlatform Coordinates (VPC)
     *			    to Eye Coordinates (EC)
     */
    private void buildProjView(Point3d		ep,
			       Transform3D      coe2Plate,
			       Transform3D      vpc2Plate,
			       double		F,
			       double		B,
			       Transform3D	ecToCc,
			       Transform3D	vpcToEc,
			       boolean		doInfinite) {

	// Lx,Ly Hx,Hy will be adjusted window boundaries
	double		Lx, Hx, Ly, Hy;
	Lx = physicalWindowXLeft; Hx = physicalWindowXRight;
	Ly = physicalWindowYBottom; Hy = physicalWindowYTop;

	ecToCc.setIdentity();


	// XXXX: we have no concept of glass correction in the Java 3D API
	//
	// Correction in apparent 3D position of window due to glass/CRT
	// and spherical/cylinderical curvarure of CRT.
	// This boils down to producing modified values of Lx Ly Hx Hy
	// and is different for hot spot vs. window center corrections.
	//
	/* XXXX:
	double		cx, cy;
	if(viewPolicy != HMD_VIEW && enable_crt_glass_correction) {
	    if (correction_point == CORRECTION_POINT_WINDOW_CENTER) {
		correct_crt( ep, Lx, Ly, &cx, &cy); Lx = cx; Ly = cy;
		correct_crt( ep, Hx, Hy, &cx, &cy); Hx = cx; Hy = cy;
	    }
	    else {  // must be hot spot correction
		// Not real code yet, for now just do same as above.
		correct_crt( ep, Lx, Ly, &cx, &cy); Lx = cx; Ly = cy;
		correct_crt( ep, Hx, Hy, &cx, &cy); Hx = cx; Hy = cy;
	    }
	}
	*/

	if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
	    System.out.println("ep = " + ep);
	    System.out.println("Lx = " + Lx + ", Hx = " + Hx);
	    System.out.println("Ly = " + Ly + ", Hy = " + Hy);
	    System.out.println("F = " + F + ", B = " + B);
	}

	// Compute the proper projection equation.  Note that we
	// do this in two steps: first we generate ImagePlateToCc,
	// then we translate this by EcToPlate, resulting in a
	// projection from EctoCc.
	//
	// A more efficient (and more accurate) approach would be to
	// modify the equations below to directly project from EcToCc.

	if (viewCache.projectionPolicy == View.PARALLEL_PROJECTION) {
	    double inv_dx, inv_dy, inv_dz;
	    inv_dx = 1.0 / (Hx - Lx);
	    inv_dy = 1.0 / (Hy - Ly);
	    inv_dz = 1.0 / (F - B);

	    ecToCc.mat[0] = 2.0 * inv_dx;
	    ecToCc.mat[3] = -(Hx + Lx) * inv_dx;
	    ecToCc.mat[5] = 2.0 * inv_dy;
	    ecToCc.mat[7] = -(Hy + Ly) * inv_dy;
	    ecToCc.mat[10] = 2.0 * inv_dz;
	    ecToCc.mat[11] = -(F + B) * inv_dz;
	}
	else {
	    double sxy, rzb, inv_dx, inv_dy;

	    inv_dx = 1.0 / (Hx - Lx);
	    inv_dy = 1.0 / (Hy - Ly);
	    rzb = 1.0/(ep.z - B);
	    sxy = ep.z*rzb;

	    ecToCc.mat[0] = sxy*2.0*inv_dx;
	    ecToCc.mat[5] = sxy*2.0*inv_dy;

	    ecToCc.mat[2] = rzb*(Hx+Lx - 2.0*ep.x)*inv_dx;
	    ecToCc.mat[6] = rzb*(Hy+Ly - 2.0*ep.y)*inv_dy;
	    ecToCc.mat[10] = rzb*(B+F-2*ep.z)/(B-F);
	    ecToCc.mat[14] = -rzb;

	    ecToCc.mat[3] = sxy*(-Hx-Lx)*inv_dx;
	    ecToCc.mat[7] = sxy*(-Hy-Ly)*inv_dy;
	    ecToCc.mat[11] = rzb*(B - ep.z - B*(B+F - 2*ep.z)/(B-F));
	    ecToCc.mat[15] = sxy;
	}

	// Since we set the matrix elements ourselves, we need to set the
	// type field.  A value of 0 means a non-affine matrix.
	ecToCc.setOrthoDirtyBit();

	// EC to ImagePlate matrix is a simple translation.
	tVec1.set(ep.x, ep.y, ep.z);
	tMat1.set(tVec1);
	ecToCc.mul(tMat1);

	if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
	    System.out.println("ecToCc:");
	    analyzeProjection(ecToCc, Hx);
	}

	if(!doInfinite) {
	    // View matrix is:
	    //  [plateToEc] [coexistence_to_plate] [vpc_to_coexistence]
	    //  where vpc_to_coexistence includes the viewPlatformScale

	    // First compute ViewPlatform to Plate
	    vpc2Plate.mul(coe2Plate, vpcToCoexistence);

	    // ImagePlate to EC matrix is a simple translation.
	    tVec1.set(-ep.x, -ep.y, -ep.z);
	    tMat1.set(tVec1);
	    vpcToEc.mul(tMat1, vpc2Plate);

	    if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
		System.out.println("vpcToEc:");
		System.out.println(vpcToEc);
	    }
	}
	else {
	    // Final infinite composite is:
	    //   [coexistence_to_eye] [vpc_to_coexistence (vom)]
	    //   (does vworld_to_coe_scale_factor get used here??? )
	    //
	    // The method is to relocate the coexistence org centered on
	    // the eye rather than the window center (via coexistence_to_eye).
	    // Computationaly simpler simplifed equation form may exist.

	    // coexistence to eye is a simple translation.
/*
	    tVec1.set(ep.x, ep.y, ep.z);
	    tMat1.set(tVec1);
	    vpcToEc.mul(tMat1, vpcToCoexistence);
	    // First compute ViewPlatform to Plate
	    vpcToPlate.mul(coexistenceToPlatevpcToPlate, vpcToCoexistence);
*/

	    // ImagePlate to EC matrix is a simple translation.
	    tVec1.set(-ep.x, -ep.y, -ep.z);
	    tMat1.set(tVec1);
	    tMat1.mul(tMat1, vpc2Plate);
	    tMat1.getRotation(vpcToEc); // use only rotation component of transform

	}

    }

    /**
     * Compute the plane equations for the frustum in ViewPlatform
     * coordinates, plus its viewing frustum points.  ccToVworld will
     * be cached - used by Canavs3D.getInverseVworldProjection().
     */
    private void computeFrustumPlanes(Transform3D ecToCc,
				      Transform3D vpcToEc,
				      Vector4d [] frustumPlanes,
				      Point4d [] frustumPoints,
                                      Transform3D ccToVworld) {

	// Compute the inverse of the Vworld to Cc transform.  This
	// gives us the Cc to Vworld transform.
	tMat2.mul(ecToCc, vpcToEc);
	ccToVworld.mul(tMat2, vworldToVpc);
	// System.out.println("ccToVworld = " + ccToVworld);
	try {
	    ccToVworld.invert();
	}
	catch (SingularMatrixException e) {
	    ccToVworld.setIdentity();
	    // System.out.println("SingularMatrixException encountered when doing invert in computeFrustumPlanes");
	}

	if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_2)) {
	    Transform3D t = new Transform3D();
	    t.mul(ecToCc, vpcToEc);
	    t.mul(vworldToVpc);
	    System.out.println("\nvworldToCc = " + t);
	    System.out.println("ccToVworld = " + ccToVworld);
	    t.mul(ccToVworld);
	    System.out.println("vworldToCc * ccToVworld = " + t);
	}

	// Transform the 8 corners of the viewing frustum into Vpc
	frustumPoints[0].set(-1.0, -1.0,  1.0, 1.0);  // lower-left-front
	frustumPoints[1].set(-1.0,  1.0,  1.0, 1.0);  // upper-left-front
	frustumPoints[2].set( 1.0,  1.0,  1.0, 1.0);  // upper-right-front
	frustumPoints[3].set( 1.0, -1.0,  1.0, 1.0);  // lower-right-front
	frustumPoints[4].set(-1.0, -1.0, -1.0, 1.0);  // lower-left-back
	frustumPoints[5].set(-1.0,  1.0, -1.0, 1.0);  // upper-left-back
	frustumPoints[6].set( 1.0,  1.0, -1.0, 1.0);  // upper-right-back
	frustumPoints[7].set( 1.0, -1.0, -1.0, 1.0);  // lower-right-back

	ccToVworld.get(tMatrix);
	int i;
	for (i = 0; i < frustumPoints.length; i++) {
	    tMatrix.transform(frustumPoints[i]);
	    double w_inv = 1.0 / frustumPoints[i].w;
	    frustumPoints[i].x *= w_inv;
	    frustumPoints[i].y *= w_inv;
	    frustumPoints[i].z *= w_inv;
	}

	// Now compute the 6 plane equations
	// left
	computePlaneEq(frustumPoints[0], frustumPoints[4],
		       frustumPoints[5], frustumPoints[1],
		       frustumPlanes[0]);

	// right
	computePlaneEq(frustumPoints[3], frustumPoints[2],
		       frustumPoints[6], frustumPoints[7],
		       frustumPlanes[1]);

	// top
	computePlaneEq(frustumPoints[1], frustumPoints[5],
		       frustumPoints[6], frustumPoints[2],
		       frustumPlanes[2]);

	// bottom
	computePlaneEq(frustumPoints[0], frustumPoints[3],
		       frustumPoints[7], frustumPoints[4],
		       frustumPlanes[3]);

	// front
	computePlaneEq(frustumPoints[0], frustumPoints[1],
		       frustumPoints[2], frustumPoints[3],
		       frustumPlanes[4]);

	// back
	computePlaneEq(frustumPoints[4], frustumPoints[7],
		       frustumPoints[6], frustumPoints[5],
		       frustumPlanes[5]);

	//System.out.println("left plane = "   + frustumPlanes[0]);
	//System.out.println("right plane = "  + frustumPlanes[1]);
	//System.out.println("top plane = "    + frustumPlanes[2]);
	//System.out.println("bottom plane = " + frustumPlanes[3]);
	//System.out.println("front plane = "  + frustumPlanes[4]);
	//System.out.println("back plane = "   + frustumPlanes[5]);
    }

    private void computePlaneEq(Point4d p1, Point4d p2, Point4d p3, Point4d p4,
				Vector4d planeEq) {
	tVec1.x = p3.x - p1.x;
	tVec1.y = p3.y - p1.y;
	tVec1.z = p3.z - p1.z;

	tVec2.x = p2.x - p1.x;
	tVec2.y = p2.y - p1.y;
	tVec2.z = p2.z - p1.z;

	tVec3.cross(tVec2, tVec1);
	tVec3.normalize();
	planeEq.x = tVec3.x;
	planeEq.y = tVec3.y;
	planeEq.z = tVec3.z;
	planeEq.w = -(planeEq.x * p1.x + planeEq.y * p1.y + planeEq.z * p1.z);
    }

    // Get methods for returning derived data values.
    // Eventually, these get functions will cause some of the parameters
    // to be lazily evaluated.
    //
    // NOTE: in the case of Transform3D, and Tuple objects, a reference
    // to the actual derived data is returned.  In these cases, the caller
    // must ensure that the returned data is not modified.
    //
    // NOTE: the snapshot and computeDerivedData methods are synchronized.
    // Callers of the following methods that can run asynchronously with
    // the renderer must call these methods and copy the data from within
    // a synchronized block on the canvas view cache object.

    int getCanvasX() {
	return canvasX;
    }

    int getCanvasY() {
	return canvasY;
    }

    int getCanvasWidth() {
	return canvasWidth;
    }

    int getCanvasHeight() {
	return canvasHeight;
    }

    double getPhysicalWindowWidth() {
	return physicalWindowWidth;
    }

    double getPhysicalWindowHeight() {
	return physicalWindowHeight;
    }

    boolean getUseStereo() {
	return useStereo;
    }

    Transform3D getLeftProjection() {
	return leftProjection;
    }

    Transform3D getRightProjection() {
	return rightProjection;
    }

    Transform3D getLeftVpcToEc() {
	return leftVpcToEc;
    }

    Transform3D getRightVpcToEc() {
	return rightVpcToEc;
    }

    Transform3D getLeftEcToVpc() {
	return leftEcToVpc;
    }

    Transform3D getRightEcToVpc() {
	return rightEcToVpc;
    }

    Transform3D getInfLeftProjection() {
	return infLeftProjection;
    }

    Transform3D getInfRightProjection() {
	return infLeftProjection;
    }

    Transform3D getInfLeftVpcToEc() {
	return infLeftVpcToEc;
    }

    Transform3D getInfRightVpcToEc() {
	return infRightVpcToEc;
    }

    Transform3D getInfLeftEcToVpc() {
	return infLeftEcToVpc;
    }

    Transform3D getInfgRightEcToVpc() {
	return infRightEcToVpc;
    }

    Transform3D getInfVworldToVpc() {
        return infVworldToVpc;
    }

    Transform3D getLeftCcToVworld() {
        return leftCcToVworld;
    }

    Transform3D getRightCcToVworld() {
        return rightCcToVworld;
    }

    Transform3D getImagePlateToVworld() {
	// XXXX: Document -- This will return the transform of left plate.
	return leftPlateToVworld;
    }



    Transform3D getLastVworldToImagePlate() {
	// XXXX: Document -- This will return the transform of left plate.
	return lastVworldToLeftPlate;

    }

    Transform3D getVworldToImagePlate() {
	// XXXX: Document -- This will return the transform of left plate.
	return vworldToLeftPlate;
    }

    Transform3D getVworldToTrackerBase() {
        return vworldToTrackerBase;
    }

    double getVworldToCoexistenceScale() {
        return vworldToCoexistenceScale;
    }

    double getInfVworldToCoexistenceScale() {
        return infVworldToCoexistenceScale;
    }

    Point3d getLeftEyeInImagePlate() {
	return leftEyeInImagePlate;
    }

    Point3d getRightEyeInImagePlate() {
	return rightEyeInImagePlate;
    }

    Point3d getCenterEyeInImagePlate() {
	return centerEyeInImagePlate;
    }

    Transform3D getHeadToVworld() {
	return headToVworld;
    }

    Transform3D getVpcToVworld() {
	return vpcToVworld;
    }

    Transform3D getVworldToVpc() {
	return vworldToVpc;
    }


    // Transform the specified X point in AWT window-relative coordinates
    // to image plate coordinates
    double getWindowXInImagePlate(double x) {
	double xScreen = x + (double)canvasX;
	return metersPerPixelX * xScreen;
    }

    // Transform the specified Y point in AWT window-relative coordinates
    // to image plate coordinates
    double getWindowYInImagePlate(double y) {
	double yScreen = y + (double)canvasY;
	return metersPerPixelY * ((double)(screenHeight - 1) - yScreen);
    }

    Vector4d[] getLeftFrustumPlanesInVworld() {
	return leftFrustumPlanes;
    }

    Vector4d[] getRightFrustumPlanesInVworld() {
	return rightFrustumPlanes;
    }


    void getPixelLocationInImagePlate(double x, double y, double z,
				      Point3d imagePlatePoint) {

	double screenx = (x + canvasX)*metersPerPixelX;
	double screeny = (screenHeight - 1 - canvasY - y)*metersPerPixelY;

	if ((viewCache.projectionPolicy == View.PERSPECTIVE_PROJECTION) &&
	    (centerEyeInImagePlate.z != 0)) {
	    double zScale = 1.0 - z/centerEyeInImagePlate.z;
	    imagePlatePoint.x = (screenx - centerEyeInImagePlate.x)*zScale
		                + centerEyeInImagePlate.x;
	    imagePlatePoint.y = (screeny - centerEyeInImagePlate.y)*zScale
		                + centerEyeInImagePlate.y;
	} else {
	    imagePlatePoint.x = screenx;
	    imagePlatePoint.y = screeny;
	}
	imagePlatePoint.z = z;
    }

    /**
     * Projects the specified point from image plate coordinates
     * into AWT pixel coordinates.
     */
    void getPixelLocationFromImagePlate(Point3d imagePlatePoint,
					Point2d pixelLocation) {

	double screenX, screenY;

	if(viewCache.projectionPolicy == View.PERSPECTIVE_PROJECTION) {
	    // get the vector from centerEyeInImagePlate to imagePlatePoint
            tVec1.sub(imagePlatePoint, centerEyeInImagePlate);

            // Scale this vector to make it end at the projection plane.
            // Scale is ratio :
            //     eye->imagePlate Plane dist  / eye->imagePlatePt dist
            // eye dist to plane is eyePos.z (eye is in +z space)
            // image->eye dist is -tVec1.z (image->eye is in -z dir)
            //System.out.println("eye dist = " + (centerEyeInImagePlate.z));
            //System.out.println("image dist = " + (-tVec1.z));
	    if (tVec1.z != 0) {
		double zScale = centerEyeInImagePlate.z / (-tVec1.z);
		screenX = centerEyeInImagePlate.x + tVec1.x * zScale;
		screenY = centerEyeInImagePlate.y + tVec1.y * zScale;

	    } else {
		screenX = imagePlatePoint.x;
		screenY = imagePlatePoint.y;
	    }

        } else {
            screenX = imagePlatePoint.x;
            screenY = imagePlatePoint.y;
        }

	//System.out.println("screenX = " + screenX + " screenY = " + screenY);
        // Note: screenPt is in image plate coords, at z=0

        // Transform from image plate coords to screen coords
        pixelLocation.x = (screenX / screenViewCache.metersPerPixelX) - canvasX;
        pixelLocation.y = screenViewCache.screenHeight - 1 -
	    (screenY / screenViewCache.metersPerPixelY) - canvasY;
        //System.out.println("pixelLocation = " + pixelLocation);
    }

    /**
     * Constructs and initializes a CanvasViewCache object.
     * Note that the canvas, screen, screenCache, view, and
     * viewCache parameters are all fixed at construction time
     * and must be non-null.
     */
    CanvasViewCache(Canvas3D canvas,
		    ScreenViewCache screenViewCache,
		    ViewCache viewCache) {

	this.canvas = canvas;
	this.screenViewCache = screenViewCache;
	this.viewCache = viewCache;

        // Set up the initial plane equations
	int i;
	for (i = 0; i < leftFrustumPlanes.length; i++) {
	    leftFrustumPlanes[i] = new Vector4d();
	    rightFrustumPlanes[i] = new Vector4d();
	}

	for (i = 0; i < leftFrustumPoints.length; i++) {
	    leftFrustumPoints[i] = new Point4d();
	    rightFrustumPoints[i] = new Point4d();
	}

       // canvas is null in Renderer copyOfCvCache
	if (canvas != null) {
	    leftEyeInImagePlate.set(canvas.leftManualEyeInImagePlate);
	    rightEyeInImagePlate.set(canvas.rightManualEyeInImagePlate);
	    centerEyeInImagePlate.add(leftEyeInImagePlate,
				      rightEyeInImagePlate);
	    centerEyeInImagePlate.scale(0.5);
	}

	if((J3dDebug.devPhase) && (J3dDebug.canvasViewCache >= J3dDebug.LEVEL_1))
	    System.out.println("Constructed a CanvasViewCache");
    }

    synchronized void setCanvas(Canvas3D c) {
	canvas = c;
    }

    synchronized void setScreenViewCache(ScreenViewCache svc) {
	screenViewCache = svc;
    }

    synchronized void setViewCache(ViewCache vc) {
	viewCache = vc;
    }
}
