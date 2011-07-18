/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.exp.swing;

import com.sun.j3d.exp.swing.impl.AutoOffScreenCanvas3D;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.swing.JPanel;
import javax.swing.event.AncestorListener;


/**
 * This class provides a lightweight capability to Java 3D. The component
 * handles bidirectional messaging between swing and Java 3D so that repaint
 * ordonned by swing are sent to the universe if necessary and refreshes from
 * the universe are painted accordingly. In order to get responsive interfaces
 * during layout changes, the canvas has a feature (disabled by default) that
 * lets true resizes occur only after a timer expires. Images between real
 * resizes can eventually be slightly wrong and pixelated, but their display
 * will be stutterless.<br>
 * Lightweight canvas also handles redirection to heavyweight canvas for the
 * following events:<br>
 * - InputMethodEvent<br>
 * - KeyEvent<br>
 * - FocusEvent<br>
 * - ComponentKeyEvent<br>
 * - MouseWheelEvent<br>
 * - MouseEvent<br>
 * - MouseMotionEvent<br>
 * <br>
 * <br>
 * When Swing is waiting for a canvas to be retrieved and that canvas is in
 * rendering stage,a loop takes place, which includes small calls to wait().
 * The canvas status is tested for readiness before and after the wait(). If
 * the canvas is not ready to be retrieved after the wait(), counter is
 * decremented and control is given back to awt thread, which will repaint old
 * buffer. If the loop goes over a certain amount of iterations, the canvas is
 * declared 'crashed' and won't be updated anymore. This was done so that a
 * crashed canvas/universe does not remove control over your GUI and does not
 * leave you with a frozen application. In current implementation, the delay
 * before a canvas is declared crashed is of :<br>
 * <code>30  Math.max(20.0, getView().getMinimumFrameCycleTime() )</code>
 *
 * @author Frederic 'pepe' Barachant
 *
 * @see getLightweightComponent()
 * @see setResizeValidationDelay()
 * @see setResizeMode()
 *
 * @since Java 3D 1.5
 */
public class JCanvas3D extends JPanel implements AncestorListener {
    /**
     * Resizing the canvas or component will be done immediately. This
     * operation might take some time and make the application look sluggish.
     *
     * @see setResizeMode()
     */
    public final static int RESIZE_IMMEDIATELY = 0;

    /**
     * Resizing the canvas or component will be done if no resizing
     * occurs after expiration of a certain delay. Rendering will be
     * eventually stretched or deformed. It can be useful on certain
     * applications where smooth update of UI during layout is needed or
     * desired.
     *
     * @see setResizeMode()
     */
    public final static int RESIZE_DELAYED = 1;

    //TODO: FBA: this had been taken from javax.media.j3d.Screen3D. When/IF proper dpi handling comes one day, that part will have to be changed also for consistency
    /** size of a pixel */
    private static double METERS_PER_PIXEL = 0.0254 / 90.0;

    /** the template to be used for this canvas */
    private GraphicsConfigTemplate3D template;
    
    /** the graphics configuration used for this canvas */
    private GraphicsConfiguration graphicsConfig;

    /** The canvas that is linked to the component. */
    private InternalCanvas3D canvas;
    
    /** flag indicating that the JCanvas3D has been added to a container */
    private boolean hasBeenAdded = false;

    /** The resize mode currently being used. */
    int resizeMode;

    /**
     * the idle delay that will trigger a real resize. ('idle' being
     * the lack of resizing action from the user)
     */
    int resizeValidationDelay;

    /** the device to be used by this canvas */
    private GraphicsDevice device;

    //TODO: FBA: the constructor below should be callable. Code should be changed so that it is possible, in order for the canvas to be useable into netbeans.
    //TODO: FBA: create a netbeans module that installs J3D as a library and the JCanvas3D as a new item in a new J3D category of the swing palette (take from the java.net swash project)

    /**
     * Constructs and initializes a new JCanvas3D object that Java 3D
     * can render into. The screen device is obtained from
     * <code>GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()</code>,
     * which might not be the one you should use if you are in a multiscreen environment.
     * The JCanvas3D is constructed using the following default parameters:<br>
     * resize mode : RESIZE_IMMEDIATELY<br>
     * validation delay : 100ms<br>
     * double buffer enable : false<br>
     * stereo enable : false<br>
     */
    public JCanvas3D() {
        this(null, GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice());
    }

    /**
     * Constructs and initializes a new Canvas3D object that Java 3D
     * can render into, using the specified graphics device.
     *
     * @param device the screen graphics device that will be used to construct
     *        a GraphicsConfiguration.
     */
    public JCanvas3D(GraphicsDevice device) {
        this(null, device);
    }

    /**
     * Constructs and initializes a new Canvas3D object that Java 3D
     * can render into, using the specified template.
     * The screen device is obtained from
     * <code>GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()</code>,
     * which might not be the one you should use if you are
     * in a multiscreen environment.
     *
     * @param template The template that will be used to construct a
     *        GraphicsConfiguration. The stereo and doublebuffer properties
     *        are forced to UNNECESSARY.
     */
    public JCanvas3D(GraphicsConfigTemplate3D template) {
        this(template, GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice());
    }

    /**
     * Constructs and initializes a new Canvas3D object that Java 3D
     * can render into, using the specified template and graphics device.
     *
     * @param template The template that will be used to construct a
     *        GraphicsConfiguration. The stereo and doublebuffer properties
     *        are forced to UNNECESSARY.
     * @param device the screen graphics device that will be used to construct
     *        a GraphicsConfiguration in conjunction with the template.
     */
    public JCanvas3D(GraphicsConfigTemplate3D template, GraphicsDevice device) {
        this.device = device;
        this.template = new GraphicsConfigTemplate3D();

        if (template != null) {
            // Clone template (it would be easier if GCT3D were cloneable)
            this.template.setRedSize(template.getRedSize());
            this.template.setGreenSize(template.getGreenSize());
            this.template.setBlueSize(template.getBlueSize());
            this.template.setDepthSize(template.getDepthSize());
            this.template.setSceneAntialiasing(template.getSceneAntialiasing());
            this.template.setStencilSize(template.getStencilSize());
//            this.template.setDoubleBuffer(template.getDoubleBuffer());
//            this.template.setStereo(template.getStereo());
        }

        // Force double-buffer and stereo to UNNECESSARY
        this.template.setStereo(GraphicsConfigTemplate.UNNECESSARY);
        this.template.setDoubleBuffer(GraphicsConfigTemplate.UNNECESSARY);

        graphicsConfig = this.device.getBestConfiguration(this.template);

        addAncestorListener(this);
        setDoubleBuffered(false);
        setResizeMode(RESIZE_IMMEDIATELY);
        setResizeValidationDelay(100);

        // so that key events and such can be received.
        setFocusable(true);
    }

    /**
     * {@inheritDoc}
     *
     * @param event {@inheritDoc}
     */
    public void ancestorAdded(javax.swing.event.AncestorEvent event) {
        //        if ( true == isVisible(  ) ) // check if the component itself is visible.
        {
            Dimension sz = getSize();

            if (0 == sz.width) {
                sz.width = 100;
            }

            if (0 == sz.height) {
                sz.height = 100;
            }

            createCanvas(sz.width, sz.height);
            canvas.addNotifyFlag = true; // make it so that i can call addNotify() without being rejected.
            canvas.addNotify();
            hasBeenAdded = true;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param event {@inheritDoc}
     */
    public void ancestorMoved(javax.swing.event.AncestorEvent event) {
    }

    /**
     * {@inheritDoc}
     *
     * @param event {@inheritDoc}
     */
    public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
        hasBeenAdded = false;
        canvas.removeNotify();
    }

    /**
     * Computes the physical dimensions of the screen in space.
     */
    private void computePhysicalDimensions() {
        // Fix to Issue : 433 - JCanvas3D crashed when using jogl pipe.
        Rectangle screenRect = this.graphicsConfig.getBounds();
        int screenWidth = (int) screenRect.getWidth();
        int screenHeight = (int) screenRect.getHeight();
        canvas.getScreen3D().setSize(screenWidth, screenHeight);
        canvas.getScreen3D()
              .setPhysicalScreenWidth(((double) screenWidth) * METERS_PER_PIXEL);
        canvas.getScreen3D()
              .setPhysicalScreenHeight(((double) screenHeight) * METERS_PER_PIXEL);
    }

    /**
     * Creates a heavyweight canvas and initializes it, or changes the
     * size of the current one if present. Current heavyweight canvas is
     * changed only if size is different from the actual one. No canvas is
     * created if this component has no parent, that is, was not added to a
     * container.
     *
     * @param width the width of the canvas to create.
     * @param height the height of the canvas to create.
     */
    void createCanvas(int width, int height) {
        if (getParent() == null) {
            return;
        }

        if (null != canvas) {
            // i had a canvas, i need to check if i really need to change it
            if ((width != canvas.getWidth()) || (height != canvas.getHeight())) {
                if ((null != canvas.getOffScreenBuffer()) &&
                        (null != canvas.getOffScreenBuffer().getImage())) {
                    canvas.getOffScreenBuffer().getImage().flush(); // flushing so that eventual resources are freed.
                }
            } else {
                return;
            }
        } else {
            // no canvas, i have to create it.
            canvas = new InternalCanvas3D(this.graphicsConfig, this);
        }

        createOffScreenBuffer(width, height); // whatever happened right above, i need to create the offscreen buffer.
    }

    /**
     * Creates an offscreen buffer to be attached to the heavyweight
     * buffer. Buffer is created 'byreference'
     *
     * @param width the width of the buffer.
     * @param height the height of the buffer.
     */
    private void createOffScreenBuffer(int width, int height) {
        computePhysicalDimensions();

        //        this.canvas.setDoubleBufferEnable( false );
        java.awt.image.BufferedImage bImage = new java.awt.image.BufferedImage(width,
                height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        javax.media.j3d.ImageComponent2D image = new javax.media.j3d.ImageComponent2D(javax.media.j3d.ImageComponent2D.FORMAT_RGBA8,
                bImage, true, false );
        image.setCapability(image.ALLOW_IMAGE_READ);
        image.setCapability(image.ALLOW_IMAGE_WRITE);

        this.canvas.stopRenderer();

        // offscreenrendering might occur even if the renderer is stopped. For that reason, i'm waiting for an hypothetical offscreen render to finish before setting offscreen rendering.
        // Otherwise, rendering will stop with an exception.
        this.canvas.waitForOffScreenRendering();

        this.canvas.setOffScreenBuffer(image);
        this.canvas.startRenderer();
    }

    /**
     * Returns the offscreen heavyweight canvas of that lightweight
     * component.
     *
     * @return the heavyweight canvas that lies in the deepness of this
     *         Component.
     */
    public Canvas3D getOffscreenCanvas3D() {
        if (null == this.canvas) {
            createCanvas(getWidth(), getHeight());
        }

        return this.canvas;
    }

    /**
     * Retrieves the resize mode for that component.
     * 
     * @return the resize mode, which can be one of RESIZE_IMMEDIATELY or
     *         RESIZE_DELAYED
     */
    public int getResizeMode() {
        return resizeMode;
    }

    /**
     * Retrieves the validation delay for that canvas, whatever the
     * resize mode is set to.
     *
     * @return the validation delay.
     */
    public int getResizeValidationDelay() {
        return resizeValidationDelay;
    }

    /**
     * Paints the result of the rendering. If the rendered buffer is
     * not useable (render thread being between [code]postRender()[/code] and
     * [code]postSwap()[/code]), it will wait for it to be ready. Otherwise it
     * will directly paint the previous buffer.
     *
     * @param g {@inheritDoc}
     */
    public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g); //paint background

        // Wait for and display image if JCanvas3D was added to an ancestor
        if (hasBeenAdded) {
            if ((false == canvas.canvasCrashed) &&
                    (true == canvas.isRendererRunning())) {
                //            System.err.println("paintComponentWaitforSwap");
                canvas.waitForSwap();

                //            System.err.println("wait is over");
            }

            if (null != canvas.bi) {
                // can eventually be null if the canvas did not send the result in the desired timeframe
                // for first render. In that case, we don't paint and keep the background as-is.
                g.drawImage(canvas.bi, 0, 0, getWidth(), getHeight(), null);
            }
        }
    }

    /**
     * Redirects event to canvas and to superclass.
     *
     * @param e {@inheritDoc}
     */
    protected void processComponentKeyEvent(java.awt.event.KeyEvent e) {
        super.processComponentKeyEvent(e);

        Object src = e.getSource();
        e.setSource(canvas);
        canvas.processComponentEvent(e);
        e.setSource(src);
    }

    /**
     * Redirects event to canvas and to superclass.
     *
     * @param e {@inheritDoc}
     */
    protected void processFocusEvent(java.awt.event.FocusEvent e) {
        super.processFocusEvent(e);

        Object src = e.getSource();
        e.setSource(canvas);
        canvas.processFocusEvent(e);
        e.setSource(src);
    }

    /**
     * Redirects event to canvas and to superclass.
     *
     * @param e {@inheritDoc}
     */
    protected void processInputMethodEvent(java.awt.event.InputMethodEvent e) {
        super.processInputMethodEvent(e);

        Object src = e.getSource();
        e.setSource(canvas);
        canvas.processInputMethodEvent(e);
        e.setSource(src);
    }

    /**
     * Redirects event to canvas and to superclass.
     *
     * @param e {@inheritDoc}
     */
    protected void processKeyEvent(java.awt.event.KeyEvent e) {
        super.processKeyEvent(e);

        Object src = e.getSource();
        e.setSource(canvas);
        canvas.processKeyEvent(e);
        e.setSource(src);
    }

    /**
     * Redirects event to canvas and to superclass.
     *
     * @param e {@inheritDoc}
     */
    protected void processMouseEvent(java.awt.event.MouseEvent e) {
        super.processMouseEvent(e);

        Object src = e.getSource();
        e.setSource(canvas);
        canvas.processMouseEvent(e);
        e.setSource(src);
    }

    /**
     * Redirects event to canvas and to superclass.
     *
     * @param e {@inheritDoc}
     */
    protected void processMouseMotionEvent(java.awt.event.MouseEvent e) {
        super.processMouseMotionEvent(e);

        Object src = e.getSource();
        e.setSource(canvas);
        canvas.processMouseMotionEvent(e);
        e.setSource(src);
    }

    /**
     * Redirects event to canvas and to superclass.
     *
     * @param e {@inheritDoc}
     */
    protected void processMouseWheelEvent(java.awt.event.MouseWheelEvent e) {
        super.processMouseWheelEvent(e);

        Object src = e.getSource();
        e.setSource(canvas);
        canvas.processMouseWheelEvent(e);
        e.setSource(src);
    }

    /**
     * {@inheritDoc}
     *
     * @param x {@inheritDoc}
     * @param y {@inheritDoc}
     * @param width {@inheritDoc}
     * @param height {@inheritDoc}
     */
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        if ((null == canvas) || (null == canvas.getOffScreenBuffer()) ||
                (JCanvas3D.RESIZE_IMMEDIATELY == getResizeMode())) //whatever the resize mode, i create on first setbounds(). (not doing so would create a deadlock in DELAYED mode when trying to do the first paint
         {
            createCanvas(width, height);
        } else if ((JCanvas3D.RESIZE_DELAYED == getResizeMode()) &&
                ((null != canvas.getParent()) &&
                (true == canvas.getParent().isVisible()))) {
            if ((null == canvas.resizeThread) ||
                    (false == canvas.resizeThread.isAlive())) {
                canvas.resizeThread = new ResizeThread(width, height,
                        getResizeValidationDelay(), this);
                canvas.resizeThread.start();
            } else {
                canvas.resizeThread.setWidth(width);
                canvas.resizeThread.setHeight(height);
            }
        }
    }

    /**
     * Sets resize mode to be used on this component. Resize mode
     * permits to have smoother canvas resizes. The time taken by a canvas to
     * be resized can be pretty long: renderer has to stop, current render has
     * to end, everything has to be initialized again, and after all that has
     * been done, renderer is started again, then the image is displayed once
     * rendered. Resize mode uses a timer to make those steps only after the
     * last refresh request occured. 'Latest refresh' is determined by the
     * amount of time between now and the last time you asked for a size
     * change. If that time expires, a real resize is done. In between, the
     * same size is rendered, but the drawn image is scaled down/up. This has
     * some drawbacks, as the image can appear blocked, imprecise, distorted,
     * incomplete for that while, but most of the time only some of the
     * drawbacks will be users will see nothing. Default delay is set to
     * 100ms, which is low enough for common human not to be able to really
     * see that the rendered image is scaled.
     * 
     * @param resizeMode can be one of RESIZE_IMMEDIATELY or RESIZE_DELAYED
     * @see #RESIZE_IMMEDIATELY
     * @see #RESIZE_DELAYED
     */
    public void setResizeMode(int resizeMode) {
        this.resizeMode = resizeMode;
    }

    /**
     * Sets the validation delay for the component. The validation
     * delay is the maximum time allowed for the canvas resizing to occur
     * using rendered buffer scaling. Once that delay expired, the canvas is
     * resized at the lowest level possible, thus in the rendering pipeline.
     * Note: Changing this field is only useful if resize mode is set to
     * RESIZE_IMMEDIATELY or RESIZE_DELAYED
     * 
     * @param resizeValidationDelay the delay before a real resize would occur.
     * @see #RESIZE_IMMEDIATELY
     * @see #RESIZE_DELAYED
     */
    public void setResizeValidationDelay(int resizeValidationDelay) {
        this.resizeValidationDelay = resizeValidationDelay;
    }

    /**
     * This class is the internal Canvas3D that is used and sent to
     * Java 3D. It is remote controlled through JCanvas3D and is modified to be
     * able to tell the lightweight component when refreshes are needed.
     */
    static class InternalCanvas3D extends Canvas3D
            implements AutoOffScreenCanvas3D {

        // These two constants define the maximum amount of time
        // to wait for the readback of the off-screen buffer to complete.
        // The total time is MAX_WAIT_LOOPS * MAX_WAIT_TIME msec.
        private static final int MAX_WAIT_LOOPS = 5;
        private static final long MAX_WAIT_TIME = 100;

        /**
         * the bufferedImage that will be displayed as the result
         * of the computations.
         */
        BufferedImage bi = null;

        /**
         * This is the lightweight canvas that is linked to that
         * offscreen canvas.
         */
        JCanvas3D lwCanvas;

        /**
         * If delayed resizing is selected, a thread handling
         * resising will be started.
         */
        ResizeThread resizeThread;

        /**
         * flag used to sort a call to addnotify() from user and
         * from the lightweight component. Lightweight component calls
         * addNotify() so that the rendering begins and uses normal routines,
         * but this is a method that user must not call.
         */
        boolean addNotifyFlag;

        /**
         * flag indicating that the canvas crashed in a way or an
         * other, making swing to wait for the swap for much too long.
         */
        protected boolean canvasCrashed;

        /**
         * flag used to know when image can be painted or not. This
         * is to avoid component potentially displaying a buffer with an
         * unfinished blit. There is already a flag (imageReady) in Canvas3D
         * that does this but it can't be used because of restrictions. This
         * flag is not really fine grained, being set from end of postRender()
         * to end of postSwap()
         */
        boolean imageReadyBis;

        /**
         * Flag to indicate that the component is waiting for the
         * canvas to acomplish its swap, and that the component has to be
         * notified when done.
         */
        boolean waitingForSwap;

        /**
         * Creates a new instance of JCanvas3D. Resize mode is set
         * to RESIZE_IMMEDIATELY and validation delay to 100ms.
         * 
         * @param graphicsConfiguration The graphics configuration to be used.
         * @param lwCanvas the lightweight canvas that is linked to that
         *        heavyweight canvas.
         */
        public InternalCanvas3D(GraphicsConfiguration graphicsConfiguration,
            JCanvas3D lwCanvas) {
            super(graphicsConfiguration, true);
            this.lwCanvas = lwCanvas;
            imageReadyBis = false;
            waitingForSwap = false;
            addNotifyFlag = false;
        }

        /**
         * {@inheritDoc}
         */
        public void addNotify() {
            if (false == addNotifyFlag) {
                throw new UnsupportedOperationException("CHANGE ME");
            } else {
                addNotifyFlag = false;
                super.addNotify();
            }
        }

        /**
         * Normally, returns the parent of that component. As the
         * canvas ought to never be added to any component, it has no parent.
         * Java 3D expects it to have a parent for some operations, so we in
         * fact cheat it by returning the parent of the lightweight component.
         *
         * @return the parent of the lightweight component, if any. Returns
         *         null if the component is not created or if it has no
         *         parent.
         */
        public java.awt.Container getParent() {
            if (null == this.lwCanvas) {
                return null;
            }

            return this.lwCanvas.getParent();
        }

        /**
         * Blocks the retrieval of the render buffer.
         */
        public void postRender() {
            imageReadyBis = false;
        }

        /**
         * Retrieves the buffer from canvas, if possible, and
         * calls/notifies component to be repainted, if necessary.
         */
        synchronized public void postSwap() {
            if (true == isRendererRunning()) { // as weird as it can look, there can be postswaps without rendered running. (?!!) Anyway, in that case we should not refresh.
                bi = getOffScreenBuffer().getImage();
                imageReadyBis = true;

                if (false == waitingForSwap) {
                    //                    System.err.println("repaint " + System.currentTimeMillis());
                    this.lwCanvas.repaint();
                } else {
                    notify();
                }
            } else {
                //                System.err.println("SWAP WITHOUT RENDERER RUNNING");
            }
        }

        /**
         * Overriden so that the JComponent can access it.
         *
         * @param e {@inheritDoc}
         */
        protected void processComponentEvent(java.awt.event.ComponentEvent e) {
            super.processComponentEvent(e);
        }

        /**
         * Overriden so that the JComponent can access it.
         *
         * @param e {@inheritDoc}
         */
        protected void processFocusEvent(java.awt.event.FocusEvent e) {
            super.processFocusEvent(e);
        }

        /**
         * Overriden so that the JComponent can access it.
         *
         * @param e {@inheritDoc}
         */
        protected void processInputMethodEvent(
            java.awt.event.InputMethodEvent e) {
            super.processInputMethodEvent(e);
        }

        /**
         * Overriden so that the JComponent can access it.
         *
         * @param e {@inheritDoc}
         */
        protected void processKeyEvent(java.awt.event.KeyEvent e) {
            super.processKeyEvent(e);
        }

        /**
         * Overriden so that the JComponent can access it.
         *
         * @param e {@inheritDoc}
         */
        protected void processMouseEvent(java.awt.event.MouseEvent e) {
            super.processMouseEvent(e);
        }

        /**
         * Overriden so that the JComponent can access it.
         *
         * @param e {@inheritDoc}
         */
        protected void processMouseMotionEvent(java.awt.event.MouseEvent e) {
            super.processMouseMotionEvent(e);
        }

        /**
         * Overriden so that the JComponent can access it.
         *
         * @param e {@inheritDoc}
         */
        protected void processMouseWheelEvent(java.awt.event.MouseWheelEvent e) {
            super.processMouseWheelEvent(e);
        }

        /**
         * If the Canvas is in a state that forbids the retrieving
         * of the buffer, wait a bit before trying again.
         */
        synchronized void waitForSwap() {
            int counter = MAX_WAIT_LOOPS;
            while (false == imageReadyBis) {
                try {
                    waitingForSwap = true;
                    wait(MAX_WAIT_TIME);
                    waitingForSwap = false;

                    if (!imageReadyBis && --counter <= 0) {
                        //if i've waited too long for the canvas to be there, let us declare it crashed.
                        System.err.println("CANVAS CRASHED!!!");
                        canvasCrashed = true;
                        return;
                    }
                } catch (InterruptedException ex) {
                    System.err.println(ex);
                }
            }
        }

    }
    
    /**
     * This Runnable is the class used when the canvas has to be
     * resized.
     */
    static class ResizeSwingRunnable implements Runnable {
        /** The component that is displaying the canvas */
        JCanvas3D canvas;

        /** latest height that was requested */
        int height;

        /** latest width that was requested */
        int width;

        /**
         * Creates a new ResizeSwingRunnable object.
         */
        private ResizeSwingRunnable() {
        }

        /**
         * Creates a new ResizeSwingRunnable object.
         *
         * @param canvas the canvas to check
         * @param width the width that is requested
         * @param height the height that is requested
         */
        public ResizeSwingRunnable(JCanvas3D canvas, int width, int height) {
            this.canvas = canvas;
            this.width = width;
            this.height = height;
        }

        /**
         * {@inheritDoc}
         */
        public void run() {
            canvas.createCanvas(width, height);
        }
    }

    /**
     * This Thread handles the resizing changes and handles the timer
     * up to the moment when the resizing has to really occur.
     */
    static class ResizeThread extends Thread {
        //TODO: refactor so that it can handle a list of canvases, delays and start delay date, and change to a singleton. Actually, each JCanvas3D that would have to resize would spawn its own thread, which ought to be seen as "a bad thing"
        /** the canvas that has to be checked */
        JCanvas3D canvas;

        /** A flag indicating that since last check, size got changed again and the delay has to be reset */
        boolean sizeChanged;

        /** the delay that has to occur between last size change and real resize */
        int delay;

        /** latest height that was requested */
        int height;

        /** latest width that was requested */
        int width;

        /**
         * Creates a new ResizeThread object.
         */
        private ResizeThread() {
        }

        /**
         * Creates a new ResizeThread object.
         *
         * @param width initial width change
         * @param height initial height change
         * @param delay delay to be used
         * @param canvas the canvas that has to be checked
         */
        public ResizeThread(int width, int height, int delay, JCanvas3D canvas) {
            this.width = width;
            this.height = height;
            this.delay = delay;
            this.sizeChanged = true;
            this.canvas = canvas;
        }

        /**
         * returns the latest height that is being requested for change
         *
         * @return latest height requested
         */
        public int getHeight() {
            return height;
        }

        /**
         * returns the latest width that is being requested for change
         *
         * @return latest width requested
         */
        public int getWidth() {
            return width;
        }

        /**
         * {@inheritDoc}
         */
        public void run() {
            try {
                while (true == sizeChanged) // the double loop is made so that if a change of size arrives while the canvas is already resizing, the same thread can keep up with subsequent resizes.
                 { // the effect of the double loop is to simplify some subtle race conditions at higher level.

                    while (true == sizeChanged) {
                        sizeChanged = false;
                        Thread.sleep(delay); // while the thread sleeps, value can change. if value changes, flag will be true, and i'll have to wait again. if size does not change during the sleep, thread will quit and size will change.
                                             //TODO: should i force a resize after a definite delay occured, so it does not stay zoomed too long ?
                    }

                    try {
                        EventQueue.invokeAndWait(new ResizeSwingRunnable(
                                canvas, width, height));
                    } catch (InterruptedException ie) {
                    } catch (InvocationTargetException ite) {
                    }
                }
            } catch (InterruptedException ie) {
                //if i get interrupted, this is not important, i'll quit method.
            }
        }

        /**
         * sets height. this has the effect of resetting the timeout.
         *
         * @param height the new height.
         *
         * @throws RuntimeException DOCUMENT ME!
         */
        public void setHeight(int height) {
            if (isAlive()) {
                this.height = height;
                sizeChanged = true;
            } else {
                throw new RuntimeException(
                    "Resizing order arrived to a dead resizing thread. Spawn a new one.");
            }
        }

        /**
         * Sets width. This has the effect of resetting the timeout.
         *
         * @param width the new width.
         *
         * @throws RuntimeException DOCUMENT ME!
         */
        public void setWidth(int width) {
            if (isAlive()) {
                this.width = width;
                sizeChanged = true;
            } else {
                throw new RuntimeException(
                    "Resizing order arrived to a dead resizing thread. Spawn a new one.");
            }
        }
    }
}
