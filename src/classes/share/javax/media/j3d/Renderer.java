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

/*
 * Portions of this code were derived from work done by the Blackdown
 * group (www.blackdown.org), who did the initial Linux implementation
 * of the Java 3D API.
 */

package javax.media.j3d;

import javax.vecmath.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;


class Renderer extends J3dThread {
    
    // This action causes this thread to wait
    static final int WAIT = 0;
 
    // This action causes this thread to notify the view, and then wait.
    static final int NOTIFY_AND_WAIT = 1;
 
    // This action causes this thread to be notified
    static final int NOTIFY = 2;

    // The following are DecalGroup rendering states
    static final int DECAL_NONE      = 0;
    static final int DECAL_1ST_CHILD = 1;
    static final int DECAL_NTH_CHILD = 2;
 
    // stuff for scene antialiasing
    static final int NUM_ACCUMULATION_SAMPLES = 8;

    static final float ACCUM_SAMPLES_X[] =
		{ -0.54818f,  0.56438f,  0.39462f, -0.54498f, 
	 	  -0.83790f, -0.39263f,  0.32254f,  0.84216f};

    static final float ACCUM_SAMPLES_Y[] =
		{  0.55331f, -0.53495f,  0.41540f, -0.52829f, 
	 	   0.82102f, -0.27383f,  0.09133f, -0.84399f};

    static final float accumValue =  1.0f / NUM_ACCUMULATION_SAMPLES;

    // The following are Render arguments
    static final int RENDER = 0;
    static final int SWAP   = 1;
    static final int REQUESTRENDER = 2;
    static final int REQUESTCLEANUP = 3;

    // Renderer Structure used for the messaging to the renderer
    RendererStructure rendererStructure = new RendererStructure();


    // vworldtoVpc matrix for background geometry
    Transform3D bgVworldToVpc = new Transform3D();

    long lasttime;
    long currtime;
    float numframes = 0.0f;
    static final boolean doTiming = false;

    private static int numInstances = 0;
    private int instanceNum = -1;

    // Local copy of sharedStereZBuffer flag
    boolean sharedStereoZBuffer;
    
    // This is the id for the underlying sharable graphics context
    Context sharedCtx = null;

    // since the sharedCtx id can be the same as the previous one,
    // we need to keep a time stamp to differentiate the contexts with the
    // same id
    long sharedCtxTimeStamp = 0;

    // display and drawable, used to free shared context
    private long sharedCtxDisplay = 0;
    private Drawable sharedCtxDrawable = null; 

    /**
     * This is the id of the current rendering context
     */
    Context currentCtx = null;

    /**
     * This is the id of the current rendering drawable
     */
    Drawable currentDrawable = null;

    // an unique bit to identify this renderer
    int rendererBit = 0;
    // an unique number to identify this renderer : ( rendererBit = 1 << rendererId)
    int rendererId = 0;

    // List of renderMolecules that are dirty due to additions
    // or removal of renderAtoms from their display list set
    // of renderAtoms
    ArrayList dirtyRenderMoleculeList = new ArrayList();

    // List of individual dlists that need to be rebuilt
    ArrayList dirtyRenderAtomList = new ArrayList();

    // List of (Rm, rInfo) pair of individual dlists that need to be rebuilt
    ArrayList dirtyDlistPerRinfoList = new ArrayList();

    
    // Texture and display list that should be freed
    ArrayList textureIdResourceFreeList = new ArrayList();
    ArrayList displayListResourceFreeList = new ArrayList();

    // Texture that should be reload
    ArrayList textureReloadList = new ArrayList();


    J3dMessage[] renderMessage;

    // The screen for this Renderer. Note that this renderer may share
    // by both on screen and off screen. When view unregister, we need
    // to set both reference to null.
    Screen3D onScreen;
    Screen3D offScreen;

    // full screen anti-aliasing projection matrices   
    Transform3D accumLeftProj = new Transform3D();
    Transform3D accumRightProj = new Transform3D();
    Transform3D accumInfLeftProj = new Transform3D();
    Transform3D accumInfRightProj = new Transform3D(); 
    
    // rendering messages
    J3dMessage m[];
    int nmesg = 0;

    // List of contexts created
    ArrayList<Context> listOfCtxs = new ArrayList<Context>();

    // Parallel list of canvases
    ArrayList<Canvas3D> listOfCanvases = new ArrayList<Canvas3D>();

    boolean needToRebuildDisplayList = false;
    boolean needToResendTextureDown = false;

    // True when either one of dirtyRenderMoleculeList,
    // dirtyDlistPerRinfoList, dirtyRenderAtomList size > 0
    boolean dirtyDisplayList = false;

    // Remember OGL context resources to free
    // before context is destroy.
    // It is used when sharedCtx = true;
    ArrayList textureIDResourceTable = new ArrayList(5);

    private synchronized int newInstanceNum() {
	return (++numInstances);
    }

    int getInstanceNum() {
	if (instanceNum == -1)
	    instanceNum = newInstanceNum();
	return instanceNum;
    }

    /**
     * Constructs a new Renderer 
     */
    Renderer(ThreadGroup t) {
	super(t);
	setName("J3D-Renderer-" + getInstanceNum());

	type = J3dThread.RENDER_THREAD;
	rendererId = VirtualUniverse.mc.getRendererId();
        rendererBit = (1 << rendererId);
        renderMessage = new J3dMessage[1];
    }

   
    /**
     * The main loop for the renderer.
     */
    void doWork(long referenceTime) {
	RenderAtom ra;
	RenderBin renderBin = null;
	Canvas3D cv, canvas=null;
	Object firstArg;
	View view = null;
	Color3f col;
	int stereo_mode;
	int num_stereo_passes, num_render_passes, num_accum_passes = 1;
	int pass, apass, i, j, k;
	boolean doAccum = false;
        double accumDx = 0.0f, accumDy = 0.0f;
	double accumDxFactor = 1.0f, accumDyFactor = 1.0f;

	double accumLeftX = 0.0, accumLeftY = 0.0, 
		accumRightX = 0.0, accumRightY = 0.0,
		accumInfLeftX = 0.0, accumInfLeftY = 0.0, 
		accumInfRightX = 0.0, accumInfRightY = 0.0;
	int opArg, status;
        boolean done = false;
	Transform3D t3d = null;
	
        opArg = ((Integer)args[0]).intValue();

        try {
            if (opArg == SWAP) {

                Object [] swapArray = (Object[])args[2];

                view = (View)args[3];

                for (i=0; i<swapArray.length; i++) {
                    cv = (Canvas3D) swapArray[i];
                    if (!cv.isRunning) {
                        continue;
                    }

                    doneSwap: try {

                        if (!cv.validCanvas) {
                            continue;
                        }

                        if (cv.active && (cv.ctx != null) &&
                                (cv.view != null) && (cv.imageReady)) {
                            if (cv.useDoubleBuffer) {
                                synchronized (cv.drawingSurfaceObject) {
                                    if (cv.validCtx) {
                                        if (VirtualUniverse.mc.doDsiRenderLock) {
                                            // Set doDsiLock flag for rendering based on system
                                            // property,  If we force DSI lock for swap
                                            // buffer,  we lose most of the parallelism that having
                                            // multiple renderers gives us.

                                            if (!cv.drawingSurfaceObject.renderLock()) {
                                                break doneSwap;
                                            }
                                            cv.makeCtxCurrent();
                                            cv.syncRender(cv.ctx, true);
                                            status = cv.swapBuffers(cv.ctx,
                                                    cv.screen.display,
                                                    cv.drawable);
                                            if (status != Canvas3D.NOCHANGE) {
                                                cv.resetRendering(status);
                                            }
                                            cv.drawingSurfaceObject.unLock();
                                        } else {
                                            cv.makeCtxCurrent();

                                            cv.syncRender(cv.ctx, true);
                                            status = cv.swapBuffers(cv.ctx,
                                                    cv.screen.display,
                                                    cv.drawable);
                                            if (status != Canvas3D.NOCHANGE) {
                                                cv.resetRendering(status);
                                            }

                                        }
                                    }
                                }
                            }
                            cv.view.inCanvasCallback = true;
                            try {
                                cv.postSwap();
                            } catch (RuntimeException e) {
                                System.err.println("Exception occurred during Canvas3D callback:");
                                e.printStackTrace();
                            } catch (Error e) {
                                // Issue 264 - catch Error so Renderer doesn't die
                                System.err.println("Error occurred during Canvas3D callback:");
                                e.printStackTrace();
                            }
                            // reset flag
                            cv.imageReady = false;
                            cv.view.inCanvasCallback = false;
                            // Clear canvasDirty bit ONLY when postSwap() success

                            // Set all dirty bits except environment set and lightbin
                            // they are only set dirty if the last used light bin or
                            // environment set values for this canvas change between
                            // one frame and other

                            if (!cv.ctxChanged) {
                                cv.canvasDirty = (0xffff & ~(Canvas3D.LIGHTBIN_DIRTY |
                                        Canvas3D.LIGHTENABLES_DIRTY |
                                        Canvas3D.AMBIENTLIGHT_DIRTY |
                                        Canvas3D.MODELCLIP_DIRTY |
                                        Canvas3D.VIEW_MATRIX_DIRTY |
                                        Canvas3D.FOG_DIRTY));
                                // Force reload of transform next frame
                                cv.modelMatrix = null;

                                // Force the cached renderAtom to null
                                cv.ra = null;
                            } else {
                                cv.ctxChanged = false;
                            }
                        }
                    } catch (NullPointerException ne) {
                        // Ignore NPE
                        if (VirtualUniverse.mc.doDsiRenderLock) {
                            cv.drawingSurfaceObject.unLock();
                        }
                    } catch (RuntimeException ex) {
                        ex.printStackTrace();

                        if (VirtualUniverse.mc.doDsiRenderLock) {
                            cv.drawingSurfaceObject.unLock();
                        }

                        // Issue 260 : indicate fatal error and notify error listeners
                        cv.setFatalError();
                        RenderingError err =
                                new RenderingError(RenderingError.UNEXPECTED_RENDERING_ERROR,
                                    J3dI18N.getString("Renderer0"));
                        err.setCanvas3D(cv);
                        err.setGraphicsDevice(cv.graphicsConfiguration.getDevice());
                        notifyErrorListeners(err);
                    }

                    cv.releaseCtx();
                }

                if (view != null) { // STOP_TIMER
                    // incElapsedFrames() is delay until MC:updateMirroObject
                    if (view.viewCache.getDoHeadTracking()) {
                        VirtualUniverse.mc.sendRunMessage(view,
                                J3dThread.RENDER_THREAD);
                    }
                }
                
            } else if (opArg == REQUESTCLEANUP) {
	    Integer mtype = (Integer) args[2];

	    if (mtype == MasterControl.REMOVEALLCTXS_CLEANUP) {
		 // from MasterControl when View is last views
		 removeAllCtxs();
	    } else if (mtype == MasterControl.FREECONTEXT_CLEANUP) {
		// from MasterControl freeContext(View v)
		cv = (Canvas3D) args[1];
		removeCtx(cv, cv.screen.display, cv.drawable, cv.ctx,
			  true, true, false);
	    } else if (mtype == MasterControl.RESETCANVAS_CLEANUP) {
		// from MasterControl RESET_CANVAS postRequest
		cv = (Canvas3D) args[1];
		if (cv.ctx != null) {
		    cv.makeCtxCurrent();
		}
		cv.freeContextResources(cv.screen.renderer, true, cv.ctx);
	    } else if (mtype == MasterControl.REMOVECTX_CLEANUP) { 
		// from Canvas3D removeCtx() postRequest
		Object[] obj = (Object []) args[1];
		Canvas3D c = (Canvas3D) obj[0]; 
		removeCtx(c,
			  ((Long) obj[1]).longValue(),
			  (Drawable) obj[2],
			  (Context) obj[3], 
			  false, !c.offScreen, 
			  false);		
	    } 
	    return;
	} else { // RENDER || REQUESTRENDER

            int renderType;
	    nmesg = 0;
	    int totalMessages = 0;
            if (opArg == RENDER) {
                m = renderMessage;
		m[0] = VirtualUniverse.mc.getMessage();
                // Issue 131: Set appropriate message type
                if (((Canvas3D)args[1]).offScreen) {
                    m[0].type = J3dMessage.RENDER_OFFSCREEN;
                }
                else {
                    m[0].type = J3dMessage.RENDER_RETAINED;
                }
		m[0].incRefcount();
                m[0].args[0] = args[1];
		totalMessages = 1;
            } else { // REQUESTRENDER
		m = rendererStructure.getMessages();
		totalMessages = rendererStructure.getNumMessage();
		if (totalMessages <= 0) {
		    return;
		}
	    }


	    doneRender: while (nmesg < totalMessages) {

		firstArg = m[nmesg].args[0];

		if (firstArg == null) {
		    Object secondArg =  m[nmesg].args[1];
		    if (secondArg instanceof Canvas3D) {
			// message from Canvas3Ds to destroy Context
			Integer reqType = (Integer) m[nmesg].args[2];
			Canvas3D c = (Canvas3D) secondArg;
			if (reqType == MasterControl.SET_GRAPHICSCONFIG_FEATURES) {
                          try {
			    if (c.offScreen) {
				// offScreen canvas neither supports
				// double buffering nor  stereo
				c.doubleBufferAvailable = false;
				c.stereoAvailable = false;
			    } else {
				c.doubleBufferAvailable = c.hasDoubleBuffer();
				c.stereoAvailable = c.hasStereo();
			    }

			    // Setup stencil related variables.
                            c.actualStencilSize = c.getStencilSize();
                            boolean userOwnsStencil = c.requestedStencilSize > 0;
                            
                            c.userStencilAvailable = 
                                    (userOwnsStencil && (c.actualStencilSize > 0));
                            c.systemStencilAvailable =
                                    (!userOwnsStencil && (c.actualStencilSize > 0));

                            c.sceneAntialiasingMultiSamplesAvailable =
				c.hasSceneAntialiasingMultisample();

			    if (c.sceneAntialiasingMultiSamplesAvailable) {
				c.sceneAntialiasingAvailable = true;
			    } else {
				c.sceneAntialiasingAvailable = 
				    c.hasSceneAntialiasingAccum();
			    }
                          } catch (RuntimeException ex) {
                              ex.printStackTrace();

                              // Issue 260 : indicate fatal error and notify error listeners
                              c.setFatalError();
                              RenderingError err =
                                      new RenderingError(RenderingError.GRAPHICS_CONFIG_ERROR,
                                          J3dI18N.getString("Renderer1"));
                              err.setCanvas3D(c);
                              err.setGraphicsDevice(c.graphicsConfiguration.getDevice());
                              notifyErrorListeners(err);
                          }
			    GraphicsConfigTemplate3D.runMonitor(J3dThread.NOTIFY);
			} else if (reqType == MasterControl.SET_QUERYPROPERTIES){
                            try {
                                c.createQueryContext();
                            } catch (RuntimeException ex) {
                                ex.printStackTrace();

                                // Issue 260 : indicate fatal error and notify error listeners
                                c.setFatalError();
                                RenderingError err =
                                        new RenderingError(RenderingError.CONTEXT_CREATION_ERROR,
                                            J3dI18N.getString("Renderer2"));
                                err.setCanvas3D(c);
                                err.setGraphicsDevice(c.graphicsConfiguration.getDevice());
                                notifyErrorListeners(err);
                            }
			    // currentCtx change after we create a new context
			    GraphicsConfigTemplate3D.runMonitor(J3dThread.NOTIFY);
			    currentCtx = null;
                            currentDrawable = null;
			} 
		    } else if (secondArg instanceof Integer) {
			// message from TextureRetained finalize() method
			// to free texture id
			freeTextureID(((Integer) secondArg).intValue(), (String)m[nmesg].args[2]);
		    } else if (secondArg instanceof GeometryArrayRetained) {
			// message from GeometryArrayRetained
			// clearLive() to free D3D array
			((GeometryArrayRetained) secondArg).freeD3DArray(false);
		    } else if (secondArg instanceof GraphicsConfigTemplate3D) {
			GraphicsConfigTemplate3D gct =
			    (GraphicsConfigTemplate3D) secondArg;
			Integer reqType = (Integer) m[nmesg].args[2];
                        if (reqType == MasterControl.GETBESTCONFIG) {
                            GraphicsConfiguration gcfg = null;
                            GraphicsConfiguration [] gcList = (GraphicsConfiguration []) gct.testCfg;
                            try {
                                gcfg = Pipeline.getPipeline().getBestConfiguration(gct, gcList);
                            } catch (NullPointerException npe) {
                                npe.printStackTrace();
                            } catch (RuntimeException ex) {
                                ex.printStackTrace();

                                // Issue 260 : notify error listeners
                                RenderingError err =
                                        new RenderingError(RenderingError.GRAPHICS_CONFIG_ERROR,
                                            J3dI18N.getString("Renderer3"));
                                err.setGraphicsDevice(gcList[0].getDevice());
                                notifyErrorListeners(err);
                            }

                            gct.testCfg = gcfg;
			} else if (reqType == MasterControl.ISCONFIGSUPPORT) {
                            boolean rval = false;
                            GraphicsConfiguration gc = (GraphicsConfiguration) gct.testCfg;
                            try {
                                if (Pipeline.getPipeline().isGraphicsConfigSupported(gct, gc)) {
                                    rval = true;
                                }
                            } catch (NullPointerException npe) {
                                npe.printStackTrace();
                            } catch (RuntimeException ex) {
                                ex.printStackTrace();

                                // Issue 260 : notify error listeners
                                RenderingError err =
                                        new RenderingError(RenderingError.GRAPHICS_CONFIG_ERROR,
                                            J3dI18N.getString("Renderer4"));
                                err.setGraphicsDevice(gc.getDevice());
                                notifyErrorListeners(err);
                            }

                            gct.testCfg = Boolean.valueOf(rval);
			}
			gct.runMonitor(J3dThread.NOTIFY);
		    }

		    m[nmesg++].decRefcount();
		    continue;
		}

                canvas = (Canvas3D) firstArg;

                renderType = m[nmesg].type;

		if (renderType == J3dMessage.CREATE_OFFSCREENBUFFER) {
		    // Fix for issue 18.
		    // Fix for issue 20.

                    canvas.drawable = null;
                    try {
                        canvas.drawable =
                                canvas.createOffScreenBuffer(canvas.ctx,
                                    canvas.screen.display,
                                    canvas.fbConfig,
                                    canvas.offScreenCanvasSize.width,
                                    canvas.offScreenCanvasSize.height);
                    } catch (RuntimeException ex) {
                        ex.printStackTrace();
                    }

                    if (canvas.drawable == null) {
                        // Issue 260 : indicate fatal error and notify error listeners
                        canvas.setFatalError();
                        RenderingError err =
                                new RenderingError(RenderingError.OFF_SCREEN_BUFFER_ERROR,
                                    J3dI18N.getString("Renderer5"));
                        err.setCanvas3D(canvas);
                        err.setGraphicsDevice(canvas.graphicsConfiguration.getDevice());
                        notifyErrorListeners(err);
                    }

		    canvas.offScreenBufferPending = false;
		    m[nmesg++].decRefcount();
		    continue;
		} 
                else if (renderType == J3dMessage.DESTROY_CTX_AND_OFFSCREENBUFFER) {
                    Object[] obj = m[nmesg].args;

		    // Fix for issue 175: destroy ctx & off-screen buffer
                    // Fix for issue 340: get display, drawable & ctx from msg
                    removeCtx(canvas,
                            ((Long) obj[1]).longValue(),
                            (Drawable) obj[2],
                            (Context) obj[3],
                            false, !canvas.offScreen, true);

		    canvas.offScreenBufferPending = false;
		    m[nmesg++].decRefcount();
		    continue;
		} 

                if ((canvas.view == null) || !canvas.firstPaintCalled) {
                    // This happen when the canvas just remove from the View
                    if (renderType == J3dMessage.RENDER_OFFSCREEN) {
                        canvas.offScreenRendering = false;
		    }
		    m[nmesg++].decRefcount();
                    continue;
                }

		if (!canvas.validCanvas && 
                    (renderType != J3dMessage.RENDER_OFFSCREEN)) {
		    m[nmesg++].decRefcount();
		    continue;
		}

		if (renderType == J3dMessage.RESIZE_CANVAS) {
		    canvas.d3dResize();
		    // render the image again after resize
		    VirtualUniverse.mc.sendRunMessage(canvas.view, J3dThread.RENDER_THREAD);
		    m[nmesg++].decRefcount();
		} else if (renderType == J3dMessage.TOGGLE_CANVAS) {
		    canvas.d3dToggle();
		    VirtualUniverse.mc.sendRunMessage(canvas.view, J3dThread.RENDER_THREAD);
		    m[nmesg++].decRefcount();
		} else if (renderType == J3dMessage.RENDER_IMMEDIATE) {
                    int command = ((Integer)m[nmesg].args[1]).intValue();
		    //System.out.println("command= " + command);
		    if (needToResendTextureDown) {
			VirtualUniverse.mc.resendTexTimestamp++;
			needToResendTextureDown = false;
		    }

                    if (canvas.isFatalError()) {
                        continue;
                    }

                  try {
		    if (canvas.ctx != null) {
			// ctx may not construct until doClear();
			canvas.beginScene();
		    }

                    switch (command) {
                    case GraphicsContext3D.CLEAR:
                        canvas.graphicsContext3D.doClear();
                        break;
                    case GraphicsContext3D.DRAW:
                        canvas.graphicsContext3D.doDraw(
				(Geometry)m[nmesg].args[2]);
                        break;
                    case GraphicsContext3D.SWAP:
                        canvas.doSwap();
                        break;
                    case GraphicsContext3D.READ_RASTER:
                        canvas.graphicsContext3D.doReadRaster(
				(Raster)m[nmesg].args[2]);
                        break;
		    case GraphicsContext3D.SET_APPEARANCE:
			canvas.graphicsContext3D.doSetAppearance(
				(Appearance)m[nmesg].args[2]);
			break;
		    case GraphicsContext3D.SET_BACKGROUND:
			canvas.graphicsContext3D.doSetBackground(
				(Background)m[nmesg].args[2]);
			break;
		    case GraphicsContext3D.SET_FOG:
			canvas.graphicsContext3D.doSetFog(
				(Fog)m[nmesg].args[2]);
			break;
		    case GraphicsContext3D.SET_LIGHT:
			canvas.graphicsContext3D.doSetLight(
				(Light)m[nmesg].args[2],
				((Integer)m[nmesg].args[3]).intValue());
			break;
		    case GraphicsContext3D.INSERT_LIGHT:
			canvas.graphicsContext3D.doInsertLight(
				(Light)m[nmesg].args[2],
				((Integer)m[nmesg].args[3]).intValue());
			break;
		    case GraphicsContext3D.REMOVE_LIGHT:
			canvas.graphicsContext3D.doRemoveLight(
				((Integer)m[nmesg].args[2]).intValue());
			break;
		    case GraphicsContext3D.ADD_LIGHT:
			canvas.graphicsContext3D.doAddLight(
				(Light)m[nmesg].args[2]);
			break;
		    case GraphicsContext3D.SET_HI_RES:
			canvas.graphicsContext3D.doSetHiRes(
				(HiResCoord)m[nmesg].args[2]);
			break;
		    case GraphicsContext3D.SET_MODEL_TRANSFORM:
			t3d = (Transform3D)m[nmesg].args[2]; 
			canvas.graphicsContext3D.doSetModelTransform(t3d);
			// return t3d to freelist. t3d was gotten from GraphicsContext3D
			VirtualUniverse.mc.addToTransformFreeList(t3d);
			break;
		    case GraphicsContext3D.MULTIPLY_MODEL_TRANSFORM:
                        t3d = (Transform3D)m[nmesg].args[2];
                        canvas.graphicsContext3D.doMultiplyModelTransform(t3d);
                        // return t3d to freelist. t3d was gotten from GraphicsContext3D
                        VirtualUniverse.mc.addToTransformFreeList(t3d);
                        break;
		    case GraphicsContext3D.SET_SOUND:
			canvas.graphicsContext3D.doSetSound(
				(Sound)m[nmesg].args[2],
				((Integer)m[nmesg].args[3]).intValue());
			break;
		    case GraphicsContext3D.INSERT_SOUND:
			canvas.graphicsContext3D.doInsertSound(
				(Sound)m[nmesg].args[2],
				((Integer)m[nmesg].args[3]).intValue());
			break;
		    case GraphicsContext3D.REMOVE_SOUND:
			canvas.graphicsContext3D.doRemoveSound(
				((Integer)m[nmesg].args[2]).intValue());
			break;
		    case GraphicsContext3D.ADD_SOUND:
			canvas.graphicsContext3D.doAddSound(
				(Sound)m[nmesg].args[2]);
			break;
		    case GraphicsContext3D.SET_AURAL_ATTRIBUTES:
			canvas.graphicsContext3D.doSetAuralAttributes(
				(AuralAttributes)m[nmesg].args[2]);
			break;
		    case GraphicsContext3D.SET_BUFFER_OVERRIDE:
			canvas.graphicsContext3D.doSetBufferOverride(
			    ((Boolean)m[nmesg].args[2]).booleanValue());
			break;
		    case GraphicsContext3D.SET_FRONT_BUFFER_RENDERING:
			canvas.graphicsContext3D.doSetFrontBufferRendering(
			    ((Boolean)m[nmesg].args[2]).booleanValue());
			break;
		    case GraphicsContext3D.SET_STEREO_MODE:
			canvas.graphicsContext3D.doSetStereoMode(
				((Integer)m[nmesg].args[2]).intValue());
			break;
		    case GraphicsContext3D.FLUSH:
			canvas.graphicsContext3D.doFlush(
				((Boolean)m[nmesg].args[2]).booleanValue());
			break;
		    case GraphicsContext3D.FLUSH2D:
			canvas.graphics2D.doFlush();
			break;
		    case GraphicsContext3D.DRAWANDFLUSH2D:
			Object ar[] = m[nmesg].args;
			canvas.graphics2D.doDrawAndFlushImage(
					      (BufferedImage) ar[2], 
					      ((Point) ar[3]).x,
					      ((Point) ar[3]).y,
					      (ImageObserver) ar[4]);
			break;
		    case GraphicsContext3D.SET_MODELCLIP:
			canvas.graphicsContext3D.doSetModelClip(
				(ModelClip)m[nmesg].args[2]);
			break;
                    default:
                        break;
                    }

		    if (canvas.ctx != null) {
			canvas.endScene();
		    }
                  } catch (RuntimeException ex) {
                    ex.printStackTrace();

                    // Issue 260 : indicate fatal error and notify error listeners
                    canvas.setFatalError();
                    RenderingError err =
                            new RenderingError(RenderingError.CONTEXT_CREATION_ERROR,
                                J3dI18N.getString("Renderer6"));
                    err.setCanvas3D(canvas);
                    err.setGraphicsDevice(canvas.graphicsConfiguration.getDevice());
                    notifyErrorListeners(err);
                  }

		    m[nmesg++].decRefcount();
		} else { // retained mode rendering

		    m[nmesg++].decRefcount();
                    
                    if (canvas.isFatalError()) {
                        continue;
                    }

		    ImageComponent2DRetained offBufRetained = null;
		    
		    if (renderType == J3dMessage.RENDER_OFFSCREEN) {
                        // Issue 131: set offScreenRendering flag here, since it
                        // otherwise won't be set for auto-off-screen rendering
                        // (which doesn't use renderOffScreenBuffer)
                        canvas.offScreenRendering = true;
                        if (canvas.drawable == null || !canvas.active) {
                            canvas.offScreenRendering = false;
                            continue;
			} else {
			    offBufRetained = (ImageComponent2DRetained)
				canvas.offScreenBuffer.retained;
			    
			    if (offBufRetained.isByReference()) {
                    	        offBufRetained.geomLock.getLock();
                                offBufRetained.evaluateExtensions(canvas);
			    }
			}
                        
                    } else if (!canvas.active) {
			continue;
		    }

                    // Issue 78 - need to get the drawingSurface info every
                    // frame; this is necessary since the HDC (window ID)
                    // on Windows can become invalidated without our
                    // being notified!
                    if (!canvas.offScreen) {
                        canvas.drawingSurfaceObject.getDrawingSurfaceObjectInfo();
                    }

		    renderBin = canvas.view.renderBin;

	            // setup rendering context 

	            // We need to catch NullPointerException when the dsi
  	            // gets yanked from us during a remove.

                    if (canvas.useSharedCtx) {

                        if (sharedCtx == null) {
                            sharedCtxDisplay = canvas.screen.display;
                            sharedCtxDrawable = canvas.drawable;

			    // Always lock for context create
			    if (!canvas.drawingSurfaceObject.renderLock()) {
				if ((offBufRetained != null) &&
				    offBufRetained.isByReference()) {
				    offBufRetained.geomLock.unLock();
				}
                                canvas.offScreenRendering = false;
				break doneRender;
			    }

			    synchronized (VirtualUniverse.mc.contextCreationLock) {
                                sharedCtx = null;
				try { 
                                    sharedCtx = canvas.createNewContext(null, true);
                                } catch (RuntimeException ex) {
                                    ex.printStackTrace();
                                }

				if (sharedCtx == null) {
				    canvas.drawingSurfaceObject.unLock();
				    if ((offBufRetained != null) &&
					offBufRetained.isByReference()) {
					offBufRetained.geomLock.unLock();
				    }
                                    canvas.offScreenRendering = false;

                                    // Issue 260 : indicate fatal error and notify error listeners
                                    canvas.setFatalError();
                                    RenderingError err =
                                            new RenderingError(RenderingError.CONTEXT_CREATION_ERROR,
                                                J3dI18N.getString("Renderer7"));
                                    err.setCanvas3D(canvas);
                                    err.setGraphicsDevice(canvas.graphicsConfiguration.getDevice());
                                    notifyErrorListeners(err);

				    break doneRender;
				}
				sharedCtxTimeStamp = 
				    VirtualUniverse.mc.getContextTimeStamp();

				needToRebuildDisplayList = true;
			    }

			    canvas.drawingSurfaceObject.unLock();
                       }
                    }

            	    if (canvas.ctx == null) {

			// Always lock for context create			
			if (!canvas.drawingSurfaceObject.renderLock()) {
			    if ((offBufRetained != null) &&
				offBufRetained.isByReference()) {
				offBufRetained.geomLock.unLock();
			    }
                            canvas.offScreenRendering = false;
			    break doneRender;
			}

			synchronized (VirtualUniverse.mc.contextCreationLock) {
                            canvas.ctx = null;
			    try {
                                canvas.ctx = canvas.createNewContext(sharedCtx, false);
                            } catch (RuntimeException ex) {
                                ex.printStackTrace();
                            }

                            if (canvas.ctx == null) {
				canvas.drawingSurfaceObject.unLock();			    
				if ((offBufRetained != null) &&
				    offBufRetained.isByReference()) {
				    offBufRetained.geomLock.unLock();
				}
                                canvas.offScreenRendering = false;

                                // Issue 260 : indicate fatal error and notify error listeners
                                canvas.setFatalError();
                                RenderingError err =
                                        new RenderingError(RenderingError.CONTEXT_CREATION_ERROR,
                                            J3dI18N.getString("Renderer7"));
                                err.setCanvas3D(canvas);
                                err.setGraphicsDevice(canvas.graphicsConfiguration.getDevice());
                                notifyErrorListeners(err);

                                break doneRender;
			    }

			    if (canvas.graphics2D != null) {
				canvas.graphics2D.init();
			    }

			    canvas.ctxTimeStamp = 
				    VirtualUniverse.mc.getContextTimeStamp();
			    listOfCtxs.add(canvas.ctx);
			    listOfCanvases.add(canvas);

			    if (renderBin.nodeComponentList.size() > 0) {
				for (i = 0; i < renderBin.nodeComponentList.size(); i++) {
				    NodeComponentRetained nc = (NodeComponentRetained)renderBin.nodeComponentList.get(i);
                                    if(nc instanceof ImageComponent2DRetained) {    
                                        ((ImageComponent2DRetained)nc).evaluateExtensions(canvas);
                                    }
				}
                            }

                            // enable separate specular color
			    canvas.enableSeparateSpecularColor();
			}

                        // create the cache texture state in canvas
                        // for state download checking purpose
                        if (canvas.texUnitState == null) {
                            canvas.createTexUnitState();
                        }

                        canvas.resetImmediateRendering(Canvas3D.NOCHANGE);
                        canvas.drawingSurfaceObject.contextValidated();

                        if (!canvas.useSharedCtx) {
                            canvas.needToRebuildDisplayList = true;
                        }
			canvas.drawingSurfaceObject.unLock();			    
            	    } else {

			if (canvas.isRunning) {
                    	    canvas.makeCtxCurrent();
			}
            	    }


	            if (renderBin != null) {
			if ((VirtualUniverse.mc.doDsiRenderLock) &&
			    (!canvas.drawingSurfaceObject.renderLock())) {
			    if ((offBufRetained != null) &&
				offBufRetained.isByReference()) {
				offBufRetained.geomLock.unLock();
			    }
                            canvas.offScreenRendering = false;
			    break doneRender;
			}

			if (needToResendTextureDown) {
			    VirtualUniverse.mc.resendTexTimestamp++;
			    needToResendTextureDown = false;
			}
		        // handle free resource
			if (canvas.useSharedCtx) {
			    freeResourcesInFreeList(canvas);
			} else {
			    canvas.freeResourcesInFreeList(canvas.ctx);
			}

			if (VirtualUniverse.mc.doDsiRenderLock) {
			    canvas.drawingSurfaceObject.unLock();
			}

                        // Issue 109 : removed copyOfCvCache now that we have
                        // a separate canvasViewCache for computing view frustum
                        CanvasViewCache cvCache = canvas.canvasViewCache;

			// Deadlock if we include updateViewCache in
			// drawingSurfaceObject sync.
			canvas.updateViewCache(false, null, null,
					       renderBin.geometryBackground != null);

			if ((VirtualUniverse.mc.doDsiRenderLock) &&
			    (!canvas.drawingSurfaceObject.renderLock())) {
			    if ((offBufRetained != null) &&
				offBufRetained.isByReference()) {
				offBufRetained.geomLock.unLock();
			    }
                            canvas.offScreenRendering = false;
			    break doneRender;
			}
								
                        // setup viewport
                        canvas.setViewport(canvas.ctx, 0, 0,
                           cvCache.getCanvasWidth(),
                           cvCache.getCanvasHeight());



                        // rebuild the display list of all dirty renderMolecules.
                        if (canvas.useSharedCtx) {
			    if (needToRebuildDisplayList) {
				renderBin.updateAllRenderMolecule(
							this, canvas);
				needToRebuildDisplayList = false;
			    }

			    if (dirtyDisplayList) {
                                renderBin.updateDirtyDisplayLists(canvas,
					dirtyRenderMoleculeList,
					dirtyDlistPerRinfoList,
					dirtyRenderAtomList,true);
				dirtyDisplayList = false;
			    }

			    // for shared context, download textures upfront
			    // to minimize the context switching overhead
			    int sz = textureReloadList.size();

			    if (sz > 0) {
				for (j = sz-1; j>=0; j--) {
				    ((TextureRetained)textureReloadList.get(j)).
					reloadTextureSharedContext(canvas);
				}
				textureReloadList.clear();
			    }
				
                        } else {
                            // update each canvas
			    if (canvas.needToRebuildDisplayList) {
				renderBin.updateAllRenderMolecule(canvas);
				canvas.needToRebuildDisplayList = false;
			    }
			    if (canvas.dirtyDisplayList) {
                                renderBin.updateDirtyDisplayLists(canvas,
                                        canvas.dirtyRenderMoleculeList,
					canvas.dirtyDlistPerRinfoList,
					canvas.dirtyRenderAtomList, false);
				canvas.dirtyDisplayList = false;
                            }
                        }

		        // lighting setup
                        if (canvas.view.localEyeLightingEnable !=
                                                canvas.ctxEyeLightingEnable) {
                            canvas.ctxUpdateEyeLightingEnable(canvas.ctx,
					      canvas.view.localEyeLightingEnable);
                            canvas.ctxEyeLightingEnable =
                                canvas.view.localEyeLightingEnable;
                        }


		        // stereo setup
                        boolean useStereo = cvCache.getUseStereo();
                        if (useStereo) {
                            num_stereo_passes = 2;
                            stereo_mode = Canvas3D.FIELD_LEFT;

                            sharedStereoZBuffer =
                                VirtualUniverse.mc.sharedStereoZBuffer;
                        } else {
                            num_stereo_passes = 1;
                            stereo_mode = Canvas3D.FIELD_ALL;

			    // just in case user set flag - 
			    // disable since we are not in stereo
			    sharedStereoZBuffer = false;
                        }

		        // full screen anti-aliasing setup
			if (canvas.view.getSceneAntialiasingEnable() &&
			    canvas.sceneAntialiasingAvailable) {
			    
			    if (!VirtualUniverse.mc.isD3D() && 
				((canvas.extensionsSupported & Canvas3D.MULTISAMPLE) == 0) || 
				!canvas.sceneAntialiasingMultiSamplesAvailable) {
				doAccum = true;
				num_accum_passes = NUM_ACCUMULATION_SAMPLES;

				System.arraycopy(
						 cvCache.getLeftProjection().mat,
                                0, accumLeftProj.mat, 0, 16);


                                accumDxFactor = (
                             	    canvas.canvasViewCache.getPhysicalWindowWidth() /
                                    canvas.canvasViewCache.getCanvasWidth())*canvas.view.fieldOfView;

                                accumDyFactor = (
                                    canvas.canvasViewCache.getPhysicalWindowHeight() /
                                    canvas.canvasViewCache.getCanvasHeight())*canvas.view.fieldOfView;
				

			        accumLeftX = accumLeftProj.mat[3];
			        accumLeftY = accumLeftProj.mat[7];

				if (useStereo) {
				    System.arraycopy(
					cvCache.getRightProjection().mat,
					0, accumRightProj.mat, 0, 16);
				    accumRightX = accumRightProj.mat[3];
				    accumRightY = accumRightProj.mat[7];
				}

				if (renderBin.geometryBackground != null) {
				    System.arraycopy(
					cvCache.getInfLeftProjection().mat,
					0, accumInfLeftProj.mat, 0, 16);
				    accumInfLeftX = accumInfLeftProj.mat[3];
				    accumInfLeftY = accumInfLeftProj.mat[7];
				    if (useStereo) {
					System.arraycopy(
					    cvCache.getInfRightProjection().mat,
					    0, accumInfRightProj.mat, 0, 16);
				        accumInfRightX = accumInfRightProj.mat[3];
				        accumInfRightY = accumInfRightProj.mat[7];
				    }
				}				
			    } else {

				if (!canvas.antialiasingSet) {
				    // System.out.println("Renderer : Enable FullSceneAntialiasing");

				    canvas.setFullSceneAntialiasing(canvas.ctx, true);
				    canvas.antialiasingSet = true;
				}
			    }
		        } else {

			    if (canvas.antialiasingSet) {
				// System.out.println("Renderer : Disable SceneAntialiasing");
				canvas.setFullSceneAntialiasing(canvas.ctx, false);
				canvas.antialiasingSet = false;
			    }
			}

		        // background geometry setup
	    	        if (renderBin.geometryBackground != null) {
			    renderBin.updateInfVworldToVpc();
		        }

		        // setup default render mode - render to both eyes
                        canvas.setRenderMode(canvas.ctx,
					     Canvas3D.FIELD_ALL,
					     canvas.useDoubleBuffer);

			canvas.beginScene();

			// this is if the background image resizes with the canvas
			int winWidth = cvCache.getCanvasWidth();
			int winHeight = cvCache.getCanvasHeight();


		        // clear background if not full screen antialiasing
                        // and not in stereo mode
                        if (!doAccum && !sharedStereoZBuffer) {
			    BackgroundRetained bg = renderBin.background;
                            
                            canvas.clear(bg, winWidth, winHeight);
                            
                        }

		        // handle preRender callback
			if (VirtualUniverse.mc.doDsiRenderLock) {
			    canvas.drawingSurfaceObject.unLock();
			}
                        canvas.view.inCanvasCallback = true;

                        try {
                            canvas.preRender();
                        } catch (RuntimeException e) {
                            System.err.println("Exception occurred during Canvas3D callback:");
                            e.printStackTrace();
                         } catch (Error e) {
                             // Issue 264 - catch Error so Renderer doesn't die
                             System.err.println("Error occurred during Canvas3D callback:");
                             e.printStackTrace();
                        }
                        canvas.view.inCanvasCallback = false;

			if ((VirtualUniverse.mc.doDsiRenderLock) &&
			    (!canvas.drawingSurfaceObject.renderLock())) {
			    if ((offBufRetained != null) &&
				offBufRetained.isByReference()) {
				offBufRetained.geomLock.unLock();
			    }
                            canvas.offScreenRendering = false;
                            break doneRender;
                        }

		        // render loop
		        for (pass = 0; pass < num_stereo_passes; pass++) {
                            if (doAccum) {
                                canvas.clearAccum(canvas.ctx);
                            }
                            canvas.setRenderMode(canvas.ctx, stereo_mode,
                                                  canvas.useDoubleBuffer);

			
			
                            for (apass = 0; apass < num_accum_passes; apass++) {

			        // jitter projection matrix and clear background
			        // for full screen anti-aliasing rendering
			        if (doAccum) {
                                    accumDx = ACCUM_SAMPLES_X[apass] *
						accumDxFactor;
                                    accumDy = ACCUM_SAMPLES_Y[apass] *
						accumDyFactor;

				    accumLeftProj.mat[3] = accumLeftX +
					accumLeftProj.mat[0] * accumDx +
					accumLeftProj.mat[1] * accumDy;

				    accumLeftProj.mat[7] = accumLeftY +
					accumLeftProj.mat[4] * accumDx +
					accumLeftProj.mat[5] * accumDy;

				    if (useStereo) {
                                        accumRightProj.mat[3] = accumRightX +
					    accumRightProj.mat[0] * accumDx +
					    accumRightProj.mat[1] * accumDy;

                                        accumRightProj.mat[7] = accumRightY +
					    accumRightProj.mat[4] * accumDx +
					    accumRightProj.mat[5] * accumDy;
				    }

				    if (renderBin.geometryBackground != null) {
                                        accumInfLeftProj.mat[3] = accumInfLeftX +
					    accumInfLeftProj.mat[0] * accumDx +
					    accumInfLeftProj.mat[1] * accumDy;

                                        accumInfLeftProj.mat[7] = accumInfLeftY +
					    accumInfLeftProj.mat[4] * accumDx +
					    accumInfLeftProj.mat[5] * accumDy;

				        if (useStereo) {
                                            accumInfRightProj.mat[3] = 
					      accumInfRightX +
					      accumInfRightProj.mat[0] * accumDx +
					      accumInfRightProj.mat[1] * accumDy;

                                            accumInfRightProj.mat[7] = 
					      accumInfRightY +
					      accumInfRightProj.mat[4] * accumDx +
					      accumInfRightProj.mat[5] * accumDy;
				        }
                                    }
			        }

                                // clear background for stereo and
                                //  accumulation buffer cases
                                if (doAccum || sharedStereoZBuffer) {
				    BackgroundRetained bg = renderBin.background;

                                    canvas.clear(bg, winWidth, winHeight);

                                }

			        // render background geometry
	    	                if (renderBin.geometryBackground != null) {

				    // setup rendering matrices
				    if (pass == 0) {
                                        canvas.vpcToEc = 
					    cvCache.getInfLeftVpcToEc();
	    	                        if (doAccum) {
                                            canvas.setProjectionMatrix(
						canvas.ctx, accumInfLeftProj);
				        } else {
                                            canvas.setProjectionMatrix(
						canvas.ctx,
					       	cvCache.getInfLeftProjection());
				        }
				    } else {
                                        canvas.vpcToEc = 
					    cvCache.getInfRightVpcToEc();
	    	                        if (doAccum) {
                                            canvas.setProjectionMatrix(
						canvas.ctx, accumInfRightProj);
				        } else {
                                            canvas.setProjectionMatrix(
						canvas.ctx,
					       cvCache.getInfRightProjection());
				        }
                                    }
                                    canvas.vworldToEc.mul(canvas.vpcToEc,
                                        cvCache.getInfVworldToVpc());

				    // render background geometry
				    renderBin.renderBackground(canvas);
			        }

			        // setup rendering matrices
                                if (pass == 0) {
                            	    canvas.vpcToEc = cvCache.getLeftVpcToEc();
			            if (doAccum) {
                                        canvas.setProjectionMatrix(canvas.ctx, accumLeftProj);
                                    } else {
                                        canvas.setProjectionMatrix(canvas.ctx,
					cvCache.getLeftProjection());
				    }
			        } else {
                            	    canvas.vpcToEc = cvCache.getRightVpcToEc();
			            if (doAccum) {
                                        canvas.setProjectionMatrix(
						canvas.ctx, accumRightProj);
                                    } else {
                                        canvas.setProjectionMatrix(canvas.ctx,
						cvCache.getRightProjection());
				    }
			        } 
                                canvas.vworldToEc.mul(canvas.vpcToEc,
                                        cvCache.getVworldToVpc());


                                synchronized (cvCache) {
                                 if (pass == 0) {
                                     canvas.setFrustumPlanes(cvCache.getLeftFrustumPlanesInVworld());
                                 } else {
                                     canvas.setFrustumPlanes(cvCache.getRightFrustumPlanesInVworld());
                                 }
                                }

				// Force view matrix dirty for each eye.
				if (useStereo) {
				    canvas.canvasDirty |= Canvas3D.VIEW_MATRIX_DIRTY;
				}
				
			        // render opaque geometry
                                renderBin.renderOpaque(canvas);

			        // render ordered geometry
                                renderBin.renderOrdered(canvas);

			        // handle renderField callback
				if (VirtualUniverse.mc.doDsiRenderLock) {
				    canvas.drawingSurfaceObject.unLock();
				}
                                canvas.view.inCanvasCallback = true;
                                try {
                                    canvas.renderField(stereo_mode);
                                } catch (RuntimeException e) {
                                    System.err.println("Exception occurred during Canvas3D callback:");
                                    e.printStackTrace();
                                } catch (Error e) {
                                    // Issue 264 - catch Error so Renderer doesn't die
                                    System.err.println("Error occurred during Canvas3D callback:");
                                    e.printStackTrace();
                                }
                                canvas.view.inCanvasCallback = false;
				if ((VirtualUniverse.mc.doDsiRenderLock) &&
				    (!canvas.drawingSurfaceObject.renderLock())) {
				    if ((offBufRetained != null) &&
					offBufRetained.isByReference()) {
					offBufRetained.geomLock.unLock();
				    }
                                    canvas.offScreenRendering = false;
                                    break doneRender;
                                }

			        // render transparent geometry
                                renderBin.renderTransparent(canvas);

                                if (doAccum)
                                    canvas.accum(canvas.ctx, accumValue);
 			    }

                            if (doAccum)
                                canvas.accumReturn(canvas.ctx);
                            if (useStereo) {
                                stereo_mode = Canvas3D.FIELD_RIGHT;
				canvas.rightStereoPass = true;
			    }
		        }
			canvas.imageReady = true;
			canvas.rightStereoPass = false;

		        // reset renderMode
                        canvas.setRenderMode(canvas.ctx,
					     Canvas3D.FIELD_ALL,
					     canvas.useDoubleBuffer);

		        // handle postRender callback
			if (VirtualUniverse.mc.doDsiRenderLock) {
			    canvas.drawingSurfaceObject.unLock();
			}
                        canvas.view.inCanvasCallback = true;

                        try {
                            canvas.postRender();
                        } catch (RuntimeException e) {
                            System.err.println("Exception occurred during Canvas3D callback:");
                            e.printStackTrace();
                        } catch (Error e) {
                            // Issue 264 - catch Error so Renderer doesn't die
                            System.err.println("Error occurred during Canvas3D callback:");
                            e.printStackTrace();
                        }
                        canvas.view.inCanvasCallback = false;

                        // end offscreen rendering
                        if (canvas.offScreenRendering) {

			    canvas.syncRender(canvas.ctx, true);
                            canvas.endOffScreenRendering();
                            canvas.offScreenRendering = false;

                            // do the postSwap for offscreen here
                            canvas.view.inCanvasCallback = true;
                            try {
                                canvas.postSwap();
                            } catch (RuntimeException e) {
                                System.err.println("Exception occurred during Canvas 3D callback:");
                                e.printStackTrace();
                            } catch (Error e) {
                                // Issue 264 - catch Error so Renderer doesn't die
                                System.err.println("Error occurred during Canvas3D callback:");
                                e.printStackTrace();
                            }

			    if (offBufRetained.isByReference()) {
                    	        offBufRetained.geomLock.unLock();
			    }

                            canvas.view.inCanvasCallback = false;

                            canvas.releaseCtx();
                        }

			canvas.endScene();

                        if (doTiming) {
                            numframes += 1.0f;
                            if (numframes >= 20.0f) {
                                currtime = J3dClock.currentTimeMillis();
                                System.err.println(
				    numframes/((currtime-lasttime)/1000.0f) +
						" frames per second");
                                numframes = 0.0f;
                                lasttime = currtime;

				// For taking memory footprint of the entire scene.
				/*
				long totalMem, freeMem, usedMem;
				for(int ii=0; ii<5;ii++) {
				    totalMem = Runtime.getRuntime().totalMemory();
				    freeMem = Runtime.getRuntime().freeMemory();
				    usedMem = totalMem - freeMem;
				    System.out.print("mem used - before: " + usedMem + "bytes ");
				    //System.out.print("mem used - before: " + usedMem + " ");
				    System.runFinalization();
				    System.gc();
				    System.runFinalization();
				    totalMem = Runtime.getRuntime().totalMemory();
				    freeMem = Runtime.getRuntime().freeMemory();
				    usedMem = totalMem - freeMem;
				    System.out.println("after: " + usedMem + "bytes ");
				    //System.out.println("after: " + usedMem + " ");
				    try {
					Thread.sleep(100);
				    }
				    catch (InterruptedException e) { }
			      
				    }
				*/
				
			    }
			}
		    } else { // if (renderBin != null)
			if ((offBufRetained != null) &&
			    offBufRetained.isByReference()) {
			    offBufRetained.geomLock.unLock();
			}	
		    }
		}
	    }

	    // clear array to prevent memory leaks
	    if (opArg == RENDER) {
		m[0] = null;
	    } else {
		Arrays.fill(m, 0, totalMessages, null);
	    }
	}
      } catch (NullPointerException ne) {
           // Print NPE, but otherwise ignore it
	    ne.printStackTrace();
	    if (canvas != null) {
		if (canvas.ctx != null) {
		    canvas.endScene();
		}
		// drawingSurfaceObject will safely ignore
		// this request if this is not lock before
		canvas.drawingSurfaceObject.unLock();

	    }
      } catch (RuntimeException ex) {
            ex.printStackTrace();

	    if (canvas != null) {
		if (canvas.ctx != null) {
		    canvas.endScene();
		}
		// drawingSurfaceObject will safely ignore
		// this request if this is not lock before
		canvas.drawingSurfaceObject.unLock();
	    }

            // Issue 260 : indicate fatal error and notify error listeners
            canvas.setFatalError();
            RenderingError err =
                    new RenderingError(RenderingError.UNEXPECTED_RENDERING_ERROR,
                        J3dI18N.getString("Renderer8"));
            err.setCanvas3D(canvas);
            if (canvas != null) {
                err.setGraphicsDevice(canvas.graphicsConfiguration.getDevice());
            }
            notifyErrorListeners(err);
      }
    }

    // resource clean up
    void shutdown() {
	removeAllCtxs();
        Pipeline.getPipeline().cleanupRenderer();
    }

    void cleanup() {
	super.cleanup();
        renderMessage = new J3dMessage[1];
	rendererStructure = new RendererStructure();	
	bgVworldToVpc = new Transform3D();
	numframes = 0.0f;
	sharedCtx = null;	 
	sharedCtxTimeStamp = 0;
	sharedCtxDisplay = 0;
        sharedCtxDrawable = null;
	dirtyRenderMoleculeList.clear();
	dirtyRenderAtomList.clear();
	dirtyDlistPerRinfoList.clear();
	textureIdResourceFreeList.clear();
	displayListResourceFreeList.clear();
	onScreen = null;
	offScreen = null;
	m = null;
	nmesg = 0;
	lasttime = 0;
	currtime = 0;
    }


    // This is only invoked from removeCtx()/removeAllCtxs()
    // with drawingSurface already lock
    final void makeCtxCurrent(Context sharedCtx, long display, Drawable drawable) {
        if (sharedCtx != currentCtx || drawable != currentDrawable) {
	    Canvas3D.useCtx(sharedCtx, display, drawable);
	    /*
            if(!Canvas3D.useCtx(sharedCtx, display, drawable)) {
                Thread.dumpStack();
                System.err.println("useCtx Fail");
            }
            */
            currentCtx = sharedCtx;
            currentDrawable = drawable;
        }
    }

    // No need to free graphics2d and background if it is from
    // Canvas3D postRequest() offScreen rendering since the
    // user thread will not wait for it. Also we can just
    // reuse it as Canvas3D did not destroy.
    private void removeCtx(Canvas3D cv, long display, Drawable drawable, Context ctx,
			   boolean resetCtx, boolean freeBackground, 
			   boolean destroyOffScreenBuffer) {

	synchronized (VirtualUniverse.mc.contextCreationLock) {
	    if (ctx != null) {
		int idx = listOfCtxs.indexOf(ctx);
		if (idx >= 0) {
		    listOfCtxs.remove(idx);
		    listOfCanvases.remove(idx);
		    // Issue 326 : don't check display variable here
		    if ((drawable != null) && cv.added) {
			// cv.ctx may reset to -1 here so we
			// always use the ctx pass in.
			if (cv.drawingSurfaceObject.renderLock()) {
			    // if it is the last one, free shared resources
			    if (sharedCtx != null) {
				if (listOfCtxs.isEmpty()) {
				    makeCtxCurrent(sharedCtx, sharedCtxDisplay, sharedCtxDrawable);
				    freeResourcesInFreeList(null);
				    freeContextResources();
				    Canvas3D.destroyContext(sharedCtxDisplay, sharedCtxDrawable, sharedCtx);
				    currentCtx = null;
                                    currentDrawable = null;
				} else {
				    freeResourcesInFreeList(cv);
				}
				cv.makeCtxCurrent(ctx, display, drawable);
			    } else {
				cv.makeCtxCurrent(ctx, display, drawable);
				cv.freeResourcesInFreeList(ctx);
			    }
			    cv.freeContextResources(this, freeBackground, ctx);
			    Canvas3D.destroyContext(display, drawable, ctx);
			    currentCtx = null;
                            currentDrawable = null;
			    cv.drawingSurfaceObject.unLock();
			}
		    }
		}

		if (resetCtx) {
		    cv.ctx = null;
		}

		if ((sharedCtx != null) && listOfCtxs.isEmpty()) {
		    sharedCtx = null;
		    sharedCtxTimeStamp = 0;
		}
		cv.ctxTimeStamp = 0;
	    }

	    // Fix for issue 18.
	    // Since we are now the renderer thread, 
	    // we can safely execute destroyOffScreenBuffer.
	    if(destroyOffScreenBuffer) {
		cv.destroyOffScreenBuffer(ctx, display, cv.fbConfig, drawable);
		cv.offScreenBufferPending = false;
	    }
	}
    }

    void removeAllCtxs() {
	Canvas3D cv;

	synchronized (VirtualUniverse.mc.contextCreationLock) {

	    for (int i=listOfCanvases.size()-1; i >=0; i--) {
		cv = (Canvas3D) listOfCanvases.get(i);

		if ((cv.screen != null) && (cv.ctx != null)) {
                    // Issue 326 : don't check display variable here
		    if ((cv.drawable != null) && cv.added) {
			if (cv.drawingSurfaceObject.renderLock()) {
			    // We need to free sharedCtx resource
			    // first before last non-sharedCtx to
			    // workaround Nvidia driver bug under Linux
			    // that crash on freeTexture ID:4685156
			    if ((i == 0) && (sharedCtx != null)) {
				makeCtxCurrent(sharedCtx, sharedCtxDisplay, sharedCtxDrawable);
				freeResourcesInFreeList(null);
				freeContextResources();
				Canvas3D.destroyContext(sharedCtxDisplay, sharedCtxDrawable, sharedCtx);
				currentCtx = null;
                                currentDrawable = null;
			    }
			    cv.makeCtxCurrent();
			    cv.freeResourcesInFreeList(cv.ctx);
			    cv.freeContextResources(this, true, cv.ctx);
			    Canvas3D.destroyContext(cv.screen.display,
						    cv.drawable,
						    cv.ctx);
			    currentCtx = null;
                            currentDrawable = null;
			    cv.drawingSurfaceObject.unLock();
			}
		    }
		}

		cv.ctx = null;
		cv.ctxTimeStamp = 0;
	    }
	    
	    if (sharedCtx != null) {
		sharedCtx = null;
		sharedCtxTimeStamp = 0;
	    }
	    listOfCanvases.clear();
	    listOfCtxs.clear();
	}
    }

    void freeTextureID(int texId, String texture) {
	Canvas3D currentCanvas = null;
	
	// get the current canvas
	for (int i=listOfCtxs.size()-1; i >= 0; i--) {
	    Canvas3D c = (Canvas3D) listOfCanvases.get(i);
	    if (c.ctx == currentCtx) {
		currentCanvas = c;
		break;
	    }
	}

	if (currentCanvas == null) {
	    return;
	}

	synchronized (VirtualUniverse.mc.contextCreationLock) {
	    if (sharedCtx != null) {
		currentCanvas.makeCtxCurrent(sharedCtx);
		// OGL share context is used
		Canvas3D.freeTexture(sharedCtx, texId);
	    } else {
		for (int i=listOfCtxs.size()-1; i >= 0; i--) {
		    Canvas3D c = (Canvas3D) listOfCanvases.get(i);
		    c.makeCtxCurrent();
		    Canvas3D.freeTexture(c.ctx, texId);
		}
	    }
	    // restore current context
	    currentCanvas.makeCtxCurrent();
	}
        // Issue 162: TEMPORARY FIX -- don't free the texture ID, since it will
        // be freed once per canvas / screen and will subsequently cause the ID
        // to be used for multiple textures.
//	if (texture.equals("2D")){
//	    VirtualUniverse.mc.freeTexture2DId(texId);
//	}
//	else if(texture.equals("3D")){
//	    VirtualUniverse.mc.freeTexture3DId(texId);
//	}
    }


    // handle free resource in the FreeList
    void freeResourcesInFreeList(Canvas3D cv) {
	Iterator it;
	boolean isFreeTex = (textureIdResourceFreeList.size() > 0);
	boolean isFreeDL = (displayListResourceFreeList.size() > 0);
	ArrayList list;
	int i, val;
	GeometryArrayRetained geo;

	if (isFreeTex || isFreeDL) {
	    if (cv != null) {
		cv.makeCtxCurrent(sharedCtx);
	    }
	    
	    if (isFreeDL) {
		for (it = displayListResourceFreeList.iterator(); it.hasNext();) {
		    val = ((Integer) it.next()).intValue();
		    if (val <= 0) {
			continue;
		    }
		    Canvas3D.freeDisplayList(sharedCtx, val);
		}
		displayListResourceFreeList.clear();
	    }
	    if (isFreeTex) {
		for (it = textureIdResourceFreeList.iterator(); it.hasNext();) {
		    val = ((Integer) it.next()).intValue();
		    if (val <= 0) {
			continue;
		    }
		    if (val >= textureIDResourceTable.size()) {
			System.out.println("Error in freeResourcesInFreeList : ResourceIDTableSize = " + 
					   textureIDResourceTable.size() + 
					   " val = " + val);
		    } else {
			textureIDResourceTable.set(val, null);
		    }
		    Canvas3D.freeTexture(sharedCtx, val);
		}		    
		textureIdResourceFreeList.clear();
	    }
	    if (cv != null) {
		cv.makeCtxCurrent(cv.ctx);
	    }
	}
    }

    final void addTextureResource(int id, Object obj) {
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

    void freeContextResources() {
	Object obj;
	TextureRetained tex;

	for (int id = textureIDResourceTable.size()-1; id > 0; id--) {
	    obj = textureIDResourceTable.get(id);
	    if (obj == null) {
		continue;
	    }
	    Canvas3D.freeTexture(sharedCtx, id);
	    if (obj instanceof TextureRetained) {
		tex = (TextureRetained) obj;
		synchronized (tex.resourceLock) {
		    tex.resourceCreationMask &= ~rendererBit;
		    if (tex.resourceCreationMask == 0) {
			tex.freeTextureId(id);
		    }
		}
	    }
	}
	textureIDResourceTable.clear();	
	
	// displayList is free in Canvas.freeContextResources()
    }
    
    /**
     * Send a message to the notification thread, which will call the
     * shader error listeners.
     */
    static void notifyErrorListeners(RenderingError err) {
        J3dNotification notification = new J3dNotification();
        notification.type = J3dNotification.RENDERING_ERROR;
        notification.universe = null;//cv.view.universe;
        notification.args[0] = err;
        VirtualUniverse.mc.sendNotification(notification);
    }


    // Default rendering error listener class
    private static RenderingErrorListener defaultErrorListener = null;

    synchronized static RenderingErrorListener getDefaultErrorListener() {
        if (defaultErrorListener == null) {
            defaultErrorListener = new DefaultErrorListener();
        }

        return defaultErrorListener;
    }

    static class DefaultErrorListener implements RenderingErrorListener {
        public void errorOccurred(RenderingError error) {
            System.err.println();
            System.err.println("DefaultRenderingErrorListener.errorOccurred:");
            error.printVerbose();
            System.exit(1);
        }
    }

}
