/*
 * Copyright 1999-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * Implementation class for J3DGraphics2D
 */

final class J3DGraphics2DImpl extends J3DGraphics2D {
    private boolean hasBeenDisposed = false;
    private Graphics2D offScreenGraphics2D;
    private BufferedImage g3dImage = null;
    private byte[] data = null;
    private boolean isFlushed = true;
    private Canvas3D canvas3d;
    private int width, height;
    private int texWidth, texHeight;
    private int xmin, ymin, xmax, ymax;
    private Object extentLock = new Object();
    private boolean abgr;
    private boolean initTexMap = false;
    private boolean strokeSet=false;
    private Point2D.Float ptSrc = new Point2D.Float();
    private Point2D.Float ptDst1 = new Point2D.Float();
    private Point2D.Float ptDst2 = new Point2D.Float();
    private Color xOrModeColor = null;
    private volatile boolean initCtx = false;
    private volatile boolean threadWaiting = false;
    static final Color blackTransparent = new Color(0,0,0,0);
    int objectId = -1;

    // Package scope contructor
    J3DGraphics2DImpl(Canvas3D c) {
	canvas3d = c;

	synchronized (VirtualUniverse.mc.contextCreationLock) {
	    if (c.ctx == null) {
		// create a dummy bufferImage
		width = 1;
		height = 1;
		g3dImage = new BufferedImage(width, height,
					     BufferedImage.TYPE_INT_ARGB);
		offScreenGraphics2D = g3dImage.createGraphics();
	    } else {
		init();
	    }
	}

    }

    // This is invoke from Renderer callback when the first
    // time createContext() finish which set
    // canvas3d.extensionSupported correctly.
    void init() {
	// if ABGR extension is supported, we want to use
	// TYPE_4BYTE_ABGR to make a fast copy
	if (!initCtx) {
	    abgr = ((canvas3d.extensionsSupported & Canvas3D.EXT_ABGR) != 0);

	    width = canvas3d.getWidth();
	    height = canvas3d.getHeight();
	    initTexMap = false;

	    if (width <= 0) {
		width = 1;
	    }
	    if (height <= 0) {
		height = 1;
	    }

	    synchronized (extentLock) {
	    xmax = width;
	    ymax = height;
	    xmin = 0;
	    ymin = 0;
	    }
	    g3dImage = new BufferedImage(width, height,
					 (abgr ? BufferedImage.TYPE_4BYTE_ABGR:
					  BufferedImage.TYPE_INT_ARGB));
	    offScreenGraphics2D = g3dImage.createGraphics();
	    clearOffScreen();
	    if (!abgr) {
		data = new byte[width*height*4];
	    }

	    // should be the last flag to set
	    initCtx = true;
	}
    }

    /**
     * Flushes all previously executed rendering operations to the
     * drawing buffer for this 2D graphics object.
     *
     * @param wait flag indicating whether or not to wait for the
     * rendering to be complete before returning from this call.
     */
    public void flush(boolean waiting) {

        if (hasBeenDisposed) {
            throw new IllegalStateException(J3dI18N.getString("J3DGraphics2D0"));
        }

	if (!isFlushed) {
	    // Composite g3dImage into Canvas3D
            if (Thread.currentThread() == canvas3d.screen.renderer) {
		if (!initCtx) {
		    return;
		}
		doFlush();
            } else {
		if (!initCtx) {
		    if (waiting &&
			(canvas3d.pendingView != null) &&
			canvas3d.pendingView.activeStatus) {
			// wait until Renderer init() this context

			while (!initCtx) {
			    MasterControl.threadYield();
			}
		    } else {
			return;
		    }
		}
		// Behavior Scheduler or other threads
		// XXXX: may not be legal for behaviorScheduler
		// May cause deadlock if it is in behaviorScheduler
		// and we wait for Renderer to finish
		boolean renderRun = (Thread.currentThread() !=
				     canvas3d.view.universe.behaviorScheduler);
		// This must put before sendRenderMessage()
		threadWaiting = true;
		sendRenderMessage(renderRun, GraphicsContext3D.FLUSH2D, null,
				  null, null);
		if (waiting) {
		    // It is possible that thread got notify BEFORE
		    // the following runMonitor invoke.
		    runMonitor(J3dThread.WAIT);
		}
	    }
	    isFlushed = true;

 	}
    }

    // copy the data into a byte buffer that will be passed to opengl
    void doFlush() {
        assert !hasBeenDisposed;

	// clip to offscreen buffer size
	if (canvas3d.ctx == null) {
	    canvas3d.getGraphicsContext3D().doClear();
	}

	synchronized (extentLock) {
	if (xmin < 0) {
	    xmin = 0;
	}
	if (xmax > width) {
	    xmax = width;
	}
	if (ymin < 0) {
	    ymin = 0;
	}
	if (ymax > height) {
	    ymax = height;
	}

	if ((xmax - xmin > 0) &&
	    (ymax - ymin > 0)) {
	    if (abgr) {
		data = ((DataBufferByte)g3dImage.getRaster().getDataBuffer()).getData();
	    } else {
		copyImage(g3dImage, data, width, height, xmin, ymin, xmax, ymax);
	    }
	    copyDataToCanvas(0, 0, xmin, ymin, xmax, ymax, width, height);
	} else {

	    runMonitor(J3dThread.NOTIFY);
	}
	// this define an empty region
	xmax = 0;
	ymax = 0;
	xmin = width;
	ymin = height;
	}

    }


    // borrowed from ImageComponentRetained since ImageComponent2D
    // seems to do stuff we don't need to
    final void copyImage(BufferedImage bi, byte[] image,
			 int width, int height,
			 int x1, int y1, int x2, int y2) {

        assert !hasBeenDisposed;

        int biType = bi.getType();
	int w, h, i, j;
	int row, rowBegin, rowInc, dstBegin;

	dstBegin = 0;
	rowInc = 1;
	rowBegin = 0;

	// convert format to RGBA for underlying OGL use
	if ((biType == BufferedImage.TYPE_INT_ARGB) ||
	    (biType == BufferedImage.TYPE_INT_RGB)) {
	    // optimized cases
	    rowBegin = y1;

	    int colBegin = x1;

	    int[] intData =
		((DataBufferInt)bi.getRaster().getDataBuffer()).getData();
	    int rowOffset = rowInc * width;
	    int intPixel;

	    rowBegin = rowBegin*width + colBegin;
	    dstBegin = rowBegin*4;

	    if (biType == BufferedImage.TYPE_INT_ARGB) {
		for (h = y1; h < y2; h++) {
		    i = rowBegin;
		    j = dstBegin;
		    for (w = x1; w < x2; w++, i++) {
			intPixel = intData[i];
			image[j++] = (byte)((intPixel >> 16) & 0xff);
			image[j++] = (byte)((intPixel >>  8) & 0xff);
			image[j++] = (byte)(intPixel & 0xff);
			image[j++] = (byte)((intPixel >> 24) & 0xff);
		    }
		    rowBegin += rowOffset;
		    dstBegin += (rowOffset*4);
		}
	    } else {
		for (h = y1; h < y2; h++) {
		    i = rowBegin;
		    j = dstBegin;
		    for (w = x1; w < x2; w++, i++) {
			intPixel = intData[i];
			image[j++] = (byte)((intPixel >> 16) & 0xff);
			image[j++] = (byte)((intPixel >>  8) & 0xff);
			image[j++] = (byte)(intPixel & 0xff);
			image[j++] = (byte)255;
		    }
		    rowBegin += rowOffset;
		    dstBegin += (rowOffset*4);
		}
	    }
	} else {
	    // non-optimized cases
	    WritableRaster ras = bi.getRaster();
	    ColorModel cm = bi.getColorModel();
	    Object pixel = ImageComponentRetained.getDataElementBuffer(ras);

	    j = (y1*width + x1)*4;
	    for (h = y1; h < y2; h++) {
		i = j;
		for (w = x1; w < x2; w++) {
		    ras.getDataElements(w, h, pixel);
		    image[j++] = (byte)cm.getRed(pixel);
		    image[j++] = (byte)cm.getGreen(pixel);
		    image[j++] = (byte)cm.getBlue(pixel);
		    image[j++] = (byte)cm.getAlpha(pixel);

		}
		j = i+ width*4;
	    }
	}
    }

    void sendRenderMessage(boolean renderRun, int command,
			   Object arg1, Object arg2, Object arg3) {
        // send a message to the request renderer
        J3dMessage renderMessage = new J3dMessage();
        renderMessage.threads = J3dThread.RENDER_THREAD;
        renderMessage.type = J3dMessage.RENDER_IMMEDIATE;
        renderMessage.universe = null;
        renderMessage.view = null;
        renderMessage.args[0] = canvas3d;
        renderMessage.args[1] = new Integer(command);
        renderMessage.args[2] = arg1;
        renderMessage.args[3] = arg2;
        renderMessage.args[4] = arg3;

        while (!canvas3d.view.inRenderThreadData) {
            // wait until the renderer thread data in added in
            // MC:RenderThreadData array ready to receive message
	    MasterControl.threadYield();
        }

        canvas3d.screen.renderer.rendererStructure.addMessage(renderMessage);

        if (renderRun) {
            // notify mc that there is work to do
	    VirtualUniverse.mc.sendRunMessage(canvas3d.view, J3dThread.RENDER_THREAD);
        } else {
            // notify mc that there is work for the request renderer
            VirtualUniverse.mc.setWorkForRequestRenderer();
        }
    }

    final void validate() {
	validate(0, 0, width, height);
    }

    void validate(float x1, float y1, float x2, float y2,
		  AffineTransform xform) {
	float t;

	if (xform == null) {
	    validate(x1, y1, x2, y2);
	} else {
	    ptSrc.x = x1;
	    ptSrc.y = y1;
	    xform.transform(ptSrc, ptDst1);
	    ptSrc.x = x2;
	    ptSrc.y = y2;
	    xform.transform(ptSrc, ptDst2);

	    if (ptDst1.x > ptDst2.x) {
		t = ptDst1.x;
		ptDst1.x = ptDst2.x;
		ptDst2.x = t;
	    }
	    if (ptDst1.y > ptDst2.y) {
		t = ptDst1.y;
		ptDst1.y = ptDst2.y;
		ptDst2.y = t;
	    }
	    // take care of numerical error by adding 1
	    validate(ptDst1.x-1, ptDst1.y-1, ptDst2.x+1, ptDst2.y+1);
	}
    }

    void validate(float x1, float y1, float x2, float y2) {
	boolean doResize = false;
	isFlushed = false;

	synchronized(canvas3d) {
	    if (initCtx && canvas3d.resizeGraphics2D) {
	        doResize = true;
	        canvas3d.resizeGraphics2D = false;
	    }
	}
	if (doResize)  {
	    synchronized (VirtualUniverse.mc.contextCreationLock) {
		Graphics2D oldOffScreenGraphics2D = offScreenGraphics2D;
		initCtx = false;
		init();
		copyGraphics2D(oldOffScreenGraphics2D);
	    }
	} else {
	    AffineTransform tr = getTransform();
	    ptSrc.x = x1;
	    ptSrc.y = y1;
	    tr.transform(ptSrc, ptDst1);
	    ptSrc.x = x2;
	    ptSrc.y = y2;
	    tr.transform(ptSrc, ptDst2);

	    synchronized (extentLock) {
	    if (ptDst1.x < xmin) {
		xmin = (int) ptDst1.x;
	    }
	    if (ptDst1.y < ymin) {
		ymin = (int) ptDst1.y;
	    }
	    if (ptDst2.x > xmax) {
		xmax = (int) ptDst2.x;
	    }
	    if (ptDst2.y > ymax) {
		ymax = (int) ptDst2.y;
	    }
	    }
	}
    }

    void copyGraphics2D(Graphics2D oldg) {
	// restore the original setting of Graphics2D when resizing the windows
	setColor(oldg.getColor());
	setFont(oldg.getFont());
	setClip(oldg.getClip());
	setComposite(oldg.getComposite());
	setTransform(oldg.getTransform());
	setPaint(oldg.getPaint());
	setStroke(oldg.getStroke());
	if (xOrModeColor != null) {
	    setXORMode(xOrModeColor);
	}

    }

    // Implementation of Graphics2D methods
    public final void clip(Shape s) {
	offScreenGraphics2D.clip(s);
    }

    public FontMetrics getFontMetrics() {
	return offScreenGraphics2D.getFontMetrics();
    }

    public Rectangle getClipBounds(Rectangle r) {
	return offScreenGraphics2D.getClipBounds(r);
    }

    public Rectangle getClipRect() {
	return offScreenGraphics2D.getClipRect();
    }

    public String toString() {
	return offScreenGraphics2D.toString();

    }

    public final AffineTransform getTransform() {
	return offScreenGraphics2D.getTransform();
    }

    public final Color getColor() {
	return offScreenGraphics2D.getColor();
    }

    public final Composite getComposite() {
	return offScreenGraphics2D.getComposite();
    }

    public final Font getFont() {
	return offScreenGraphics2D.getFont();
    }

    public final FontMetrics getFontMetrics(Font f) {
	return offScreenGraphics2D.getFontMetrics(f);
    }

    public final FontRenderContext getFontRenderContext() {
	return offScreenGraphics2D.getFontRenderContext();
    }

    public final GraphicsConfiguration getDeviceConfiguration() {
	return offScreenGraphics2D.getDeviceConfiguration();
    }

    public final Object getRenderingHint(Key hintKey) {
	return offScreenGraphics2D.getRenderingHint(hintKey);
    }

    public final Paint getPaint() {
	return offScreenGraphics2D.getPaint();
    }

    public final Rectangle getClipBounds() {
	return offScreenGraphics2D.getClipBounds();
    }

    public final RenderingHints getRenderingHints() {
	return offScreenGraphics2D.getRenderingHints();
    }

    public final Shape getClip() {
	return offScreenGraphics2D.getClip();
    }

    public final Stroke getStroke() {
	return offScreenGraphics2D.getStroke();
    }

    public final boolean drawImage(Image img, AffineTransform xform,
				   ImageObserver obs) {

	validate(0, 0, img.getWidth(obs), img.getHeight(obs), xform);
	return offScreenGraphics2D.drawImage(img, xform, obs);
    }

    public final void drawImage(BufferedImage img, BufferedImageOp op,
				int x, int y) {
	if (op != null) {
	    img = op.filter(img, null);
	}
	validate(x, y, x+img.getWidth(), y+img.getHeight());
	offScreenGraphics2D.drawImage(img, null, x, y);
    }

    public final boolean drawImage(Image img,
				   int x, int y,
				   ImageObserver observer) {

	validate(x, y,
		 x + img.getWidth(observer),
		 y + img.getWidth(observer));
	return offScreenGraphics2D.drawImage(img, x, y, observer);
    }

    public final boolean drawImage(Image img, int x, int y,
				   int width, int height,
				   ImageObserver observer) {
	validate(x, y, x+width, y+height);
	return offScreenGraphics2D.drawImage(img, x, y, width, height,
					     observer);
    }

    public final boolean drawImage(Image img, int x, int y,
				   int width, int height,
				   Color bgcolor,
				   ImageObserver observer) {
	validate(x, y, x+width, y+height);
	return offScreenGraphics2D.drawImage(img, x, y, width, height, bgcolor,
					     observer);
    }

    public final void drawImage(BufferedImage img,
				int dx1, int dy1, int dx2, int dy2,
				int sx1, int sy1, int sx2, int sy2,
				ImageObserver observer) {
	validate(dx1, dy1, dx2, dy2);
	offScreenGraphics2D.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1,
				      sx2, sy2, observer);
    }

    public final boolean drawImage(Image img,
				   int dx1, int dy1, int dx2, int dy2,
				   int sx1, int sy1, int sx2, int sy2,
				   ImageObserver observer) {
	validate(dx1, dy1, dx2, dy2);
	return offScreenGraphics2D.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1,
					     sx2, sy2, observer);
    }

    public final boolean drawImage(Image img,
				   int dx1, int dy1, int dx2, int dy2,
				   int sx1, int sy1, int sx2, int sy2,
				   Color bgcolor,
				   ImageObserver observer) {
	validate(dx1, dy1, dx2, dy2);
	return offScreenGraphics2D.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1,
					     sx2, sy2, bgcolor, observer);
    }

    public final boolean drawImage(Image img, int x, int y,
				   Color bgcolor,
				   ImageObserver observer) {
	validate(x, y, x+img.getWidth(observer), y+img.getHeight(observer));
	return offScreenGraphics2D.drawImage(img, x, y, bgcolor, observer);
    }

    public final boolean hit(Rectangle rect, Shape s, boolean onStroke) {
	return offScreenGraphics2D.hit(rect, s, onStroke);
    }

    public final void addRenderingHints(Map hints) {
	offScreenGraphics2D.addRenderingHints(hints);
    }

    public final void clipRect(int x, int y, int width, int height) {
	offScreenGraphics2D.clipRect(x, y, width, height);
    }

    public final void copyArea(int x, int y, int width, int height,
			       int dx, int dy) {
	validate(x+dx, y+dy, x+dx+width, y+dy+height);
	offScreenGraphics2D.copyArea(x, y, width, height, dx, dy);
    }

    public final void draw(Shape s) {
	Rectangle rect = s.getBounds();
	validate(rect.x, rect.y,
		 rect.x + rect.width,
		 rect.y + rect.height);
	offScreenGraphics2D.draw(s);
    }

    public final void drawArc(int x, int y, int width, int height,
			      int startAngle, int arcAngle) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    public final void drawGlyphVector(GlyphVector g, float x, float y) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.drawGlyphVector(g, x, y);
    }

    public final void drawLine(int x1, int y1, int x2, int y2) {
	int minx, miny, maxx, maxy;
	if (!strokeSet) {
	    if (x1 > x2) {
		minx = x2;
		maxx = x1;
	    } else {
		minx = x1;
		maxx = x2;
	    }
	    if (y1 > y2) {
		miny = y2;
		maxy = y1;
	    } else {
		miny = y1;
		maxy = y2;
	    }
	    validate(minx, miny, maxx, maxy);
	} else {
	    // XXXX: call validate with bounding box of primitive
	    // XXXX: Need to consider Stroke width
	    validate();
	}
	offScreenGraphics2D.drawLine(x1, y1, x2, y2);
    }

    public final void drawOval(int x, int y, int width, int height) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.drawOval(x, y, width, height);
    }

    public final void drawPolygon(int xPoints[], int yPoints[],
				  int nPoints) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.drawPolygon(xPoints,  yPoints, nPoints);
    }

    public final void drawPolyline(int xPoints[], int yPoints[],
				   int nPoints) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.drawPolyline(xPoints,  yPoints, nPoints);
    }

    public final void drawRenderableImage(RenderableImage img,
					  AffineTransform xform) {

	validate(0, 0, img.getWidth(), img.getHeight(), xform);
	offScreenGraphics2D.drawRenderableImage(img, xform);
    }

    public final void drawRenderedImage(RenderedImage img,
					AffineTransform xform) {
	validate(0, 0, img.getWidth(), img.getHeight(), xform);
	offScreenGraphics2D.drawRenderedImage(img, xform);
    }

    public final void drawRoundRect(int x, int y, int width, int height,
				    int arcWidth, int arcHeight) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.drawRoundRect(x, y, width, height, arcWidth,
				      arcHeight);
    }

    public final void drawString(AttributedCharacterIterator iterator,
				 int x, int y) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.drawString(iterator, x, y);
    }

    public final void drawString(AttributedCharacterIterator iterator,
				 float x, float y) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.drawString(iterator, x, y);
    }

    public final void drawString(String s, float x, float y) {
       TextLayout layout = new TextLayout(s, getFont(),
					  getFontRenderContext());
       Rectangle2D bounds = layout.getBounds();
       float x1 = (float) bounds.getX();
       float y1 = (float) bounds.getY();
       validate(x1+x, y1+y,
		x1 + x + (float) bounds.getWidth(),
		y1 + y + (float) bounds.getHeight());
       offScreenGraphics2D.drawString(s, x, y);

    }

    public final void drawString(String s, int x, int y) {
	drawString(s, (float) x, (float) y);
    }

    public final void fill(Shape s) {
	Rectangle rect = s.getBounds();
	validate(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
	offScreenGraphics2D.fill(s);
    }

    public final void fillArc(int x, int y, int width, int height,
			      int startAngle, int arcAngle) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    public final void fillOval(int x, int y, int width, int height) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.fillOval(x, y, width, height);
    }

    public final void fillRoundRect(int x, int y, int width, int height,
				    int arcWidth, int arcHeight) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.fillRoundRect(x, y, width, height, arcWidth,
				      arcHeight);
    }

    public final void rotate(double theta) {
	offScreenGraphics2D.rotate(theta);
    }

    public final void rotate(double theta, double x, double y) {
	offScreenGraphics2D.rotate(theta, x, y);
    }

    public final void scale(double sx, double sy) {
	offScreenGraphics2D.scale(sx, sy);
    }

    public final void setClip(Shape clip) {
	offScreenGraphics2D.setClip(clip);
    }


    public final void setClip(int x, int y, int width, int height) {
	offScreenGraphics2D.setClip(x, y, width, height);
    }

    public final void setColor(Color c) {
	offScreenGraphics2D.setColor(c);
    }

    public final void setComposite(Composite comp) {
	offScreenGraphics2D.setComposite(comp);
    }

    public final void setFont(Font font) {
	offScreenGraphics2D.setFont(font);
    }

    public final void setPaint( Paint paint ) {
	offScreenGraphics2D.setPaint(paint);
    }

    public final void setPaintMode() {
	xOrModeColor = null;
	offScreenGraphics2D.setPaintMode();
    }

    public final void setRenderingHint(Key hintKey, Object hintValue) {
	offScreenGraphics2D.setRenderingHint(hintKey, hintValue);
    }

    public final void setRenderingHints(Map hints) {
	offScreenGraphics2D.setRenderingHints(hints);
    }

    public final void setStroke(Stroke s) {
	strokeSet = (s != null);
	offScreenGraphics2D.setStroke(s);
    }

    public final void setTransform(AffineTransform Tx) {
	offScreenGraphics2D.setTransform(Tx);
    }

    public final void setXORMode(Color c1) {
	xOrModeColor = c1;
	offScreenGraphics2D.setXORMode(c1);
    }

    public final void shear(double shx, double shy) {
	offScreenGraphics2D.shear(shx, shy);
    }

    public final void transform(AffineTransform Tx) {
	offScreenGraphics2D.transform(Tx);
    }

    public final void translate(double tx, double ty) {
	offScreenGraphics2D.translate(tx, ty);
    }

    public final void translate(int x, int y) {
	offScreenGraphics2D.translate(x, y);
    }
    public boolean hitClip(int x, int y, int width, int height) {
	return offScreenGraphics2D.hitClip(x, y, width, height);
    }

    public void draw3DRect(int x, int y, int width, int height,
                           boolean raised) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.draw3DRect(x, y, width, height, raised);
    }

    public void drawBytes(byte data[], int offset, int length, int x, int y) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.drawBytes(data,  offset, length, x, y);
    }

    public void drawChars(char data[], int offset, int length, int x, int y) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.drawChars(data,  offset, length, x, y);
    }

    public void drawPolygon(Polygon p) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.drawPolygon(p);
    }

    public void drawRect(int x, int y, int width, int height) {
	// XXXX: call validate with bounding box of primitive
	// XXXX: need to consider Stroke width
	validate();
	offScreenGraphics2D.drawRect(x, y, width, height);
    }

    public void fill3DRect(int x, int y, int width, int height,
                           boolean raised) {
	// XXXX: call validate with bounding box of primitive
	// XXXX: need to consider Stroke width
	validate();
	offScreenGraphics2D.fill3DRect(x, y, width, height, raised);
    }

    public void fillPolygon(Polygon p) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.fillPolygon(p);
    }

    public final void fillPolygon(int xPoints[], int yPoints[],
                                     int nPoints) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.fillPolygon(xPoints, yPoints, nPoints);
    }

    public final void fillRect(int x, int y, int width, int height) {
	// XXXX: call validate with bounding box of primitive
	validate();
	offScreenGraphics2D.fillRect(x, y, width, height);
    }

    // Issue 121 - release all resources, mark as disposed
    public void dispose() {

        // Issue 583 - do nothing if graphics has already been disposed
        if (hasBeenDisposed) {
            return;
        }

        if (Thread.currentThread() == canvas3d.screen.renderer) {
            doDispose();
        } else {
            // Behavior Scheduler or other threads
            // XXXX: may not be legal for behaviorScheduler
            // May cause deadlock if it is in behaviorScheduler
            // and we wait for Renderer to finish
            boolean renderRun = (Thread.currentThread() !=
                    canvas3d.view.universe.behaviorScheduler);
            sendRenderMessage(renderRun, GraphicsContext3D.DISPOSE2D,
                    null, null, null);
        }


    }

    public void doDispose() {

        if (hasBeenDisposed) {
            return;
        }

        if (objectId != -1) {
        	Canvas3D.freeTexture(canvas3d.ctx, objectId);
            VirtualUniverse.mc.freeTexture2DId(objectId);
            objectId = -1;
        }

        // Dispose of the underlying Graphics2D
        offScreenGraphics2D.dispose();

        // Mark as disposed
        hasBeenDisposed = true;
        // Issue 583 - set graphics2D field to null so it will get recreated
        canvas3d.graphics2D = null;
    }

    public void drawAndFlushImage(BufferedImage img, int x, int y,
				  ImageObserver observer) {

        if (hasBeenDisposed) {
            throw new IllegalStateException(J3dI18N.getString("J3DGraphics2D0"));
        }

        if (!(initCtx && abgr &&
	      (img.getType() == BufferedImage.TYPE_4BYTE_ABGR))) {
	    drawImage(img, x, y, observer);
	    flush(false);
	    return;
	}

	if (Thread.currentThread() == canvas3d.screen.renderer) {
	    doDrawAndFlushImage(img, x, y, observer);
	} else {
	    // Behavior Scheduler or other threads
	    // XXXX: may not be legal for behaviorScheduler
	    // May cause deadlock if it is in behaviorScheduler
	    // and we wait for Renderer to finish
	    boolean renderRun = (Thread.currentThread() !=
				 canvas3d.view.universe.behaviorScheduler);
	    sendRenderMessage(renderRun, GraphicsContext3D.DRAWANDFLUSH2D,
			      img, new Point(x, y), observer);
	}
    }

    void doDrawAndFlushImage(BufferedImage img, int x, int y,
			     ImageObserver observer) {

        assert !hasBeenDisposed;

        int imgWidth = img.getWidth(observer);
 	int imgHeight = img.getHeight(observer);
	int px, py, x1, y1, x2, y2;

	if (canvas3d.ctx == null) {
	    canvas3d.getGraphicsContext3D().doClear();
	}

	// format needs to be 4BYTE_ABGR and abgr needs to be supported
	// also must be in canvas callback
	data = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();

	// Transform the affine transform,
	// note we do not handle scale/rotate etc.
	AffineTransform tr = getTransform();
	ptSrc.x = x;
	ptSrc.y = y;
	tr.transform(ptSrc, ptDst1);
	px = (int) ptDst1.x;
	py = (int) ptDst1.y;

	// clip to offscreen buffer size

	if (px + imgWidth > width) {
	    x2 = width - px;
	} else {
	    x2 = imgWidth;
	}

	if (px < 0) {
	    x1 = -px;
	    px = 0;
	} else {
	    x1 = 0;
	}

	if (py + imgHeight > height) {
	    y2 = height - py;
	} else {
	    y2 = imgHeight;
	}

	if (py < 0) {
	    y1 = -py;
	    py = 0;
	} else {
	    y1 = 0;
	}

	if ((y2 - y1 > 0) && (x2 - x1 > 0)) {
	    copyDataToCanvas(px, py, x1,y1, x2, y2,imgWidth, imgHeight);
	}

    }


    void copyDataToCanvas(int px, int py, int x1, int y1,
			  int x2, int y2, int w, int h) {
	try {
	    if (!canvas3d.drawingSurfaceObject.renderLock()) {
		return;
	    }

            if (!initTexMap) {
                if (objectId == -1) {
                    objectId = VirtualUniverse.mc.getTexture2DId();
                }
                texWidth = getGreaterPowerOf2(w);
                texHeight = getGreaterPowerOf2(h);

                // Canvas got resize, need to init texture map again
                // in Renderer thread
                if (!canvas3d.initTexturemapping(canvas3d.ctx,
                        texWidth, texHeight,
                        objectId)) {
                    // Fail to get the texture surface, most likely
                    // there is not enough texture memory
                    initTexMap = false;
                    VirtualUniverse.mc.freeTexture2DId(objectId);
                    objectId = -1;
                    // TODO : Need to find a better way to report no resource problem --- Chien.
                    System.err.println("J3DGraphics2DImpl.copyDataToCanvas() : Fail to get texture resources ...");

                } else {
                    initTexMap = true;
                }
            }
            if (initTexMap) {
                canvas3d.texturemapping(canvas3d.ctx, px, py,
                        x1, y1, x2, y2,
                        texWidth, texHeight, w,
                        (abgr ? ImageComponentRetained.TYPE_BYTE_ABGR:
                            ImageComponentRetained.TYPE_BYTE_RGBA),
                        objectId, data, width, height);
            }

	    canvas3d.drawingSurfaceObject.unLock();
	} catch (NullPointerException ne) {
	    canvas3d.drawingSurfaceObject.unLock();
	    throw ne;
	}

	clearOffScreen();
	runMonitor(J3dThread.NOTIFY);
    }

    void clearOffScreen() {
	Composite comp = offScreenGraphics2D.getComposite();
	Color c = offScreenGraphics2D.getColor();
	offScreenGraphics2D.setComposite(AlphaComposite.Src);
	offScreenGraphics2D.setColor(blackTransparent);
	offScreenGraphics2D.fillRect(xmin, ymin, (xmax-xmin), (ymax-ymin));
	offScreenGraphics2D.setComposite(comp);
	offScreenGraphics2D.setColor(c);
    }

    /**
     * Return an integer of power 2 greater than x
     */
    static int getGreaterPowerOf2(int x) {
	int i = -1;
	if (x >= 0) {
	    for (i = 1; i < x; i <<= 1);
	}
	return i;
    }

    /**
     * MC may not scheduler Renderer thread or Renderer thread
     * may not process message FLUSH. This will hang user
     * thread.
     */
    synchronized void runMonitor(int action) {
        if (action == J3dThread.WAIT) {
            // Issue 279 - loop until ready
	    while (threadWaiting) {
		try {
		    wait();
		} catch (InterruptedException e){}
	    }
	} else if (action == J3dThread.NOTIFY) {
	    notify();
	    threadWaiting = false;
	}
    }
}
