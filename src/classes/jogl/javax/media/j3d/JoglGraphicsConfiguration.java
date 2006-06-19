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

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.media.opengl.*;

/**
 * Class implementing the GraphicsConfiguration API, but not a "real"
 * GraphicsConfiguration object. Wraps a GLCapabilities object and
 * supports either immediate or deferred pixel format / visual
 * selection depending on which platform we are running.
 */

class JoglGraphicsConfiguration extends GraphicsConfiguration {
  private GLCapabilities caps;
  private int chosenIndex;
  private GraphicsDevice device;
  // Needed for Screen3D
  private int width;
  private int height;

  JoglGraphicsConfiguration(GLCapabilities caps, int chosenIndex, GraphicsDevice device) {
    super();
    this.caps = caps;
    this.chosenIndex = chosenIndex;
    this.device = device;
    DisplayMode m = device.getDisplayMode();
    width = m.getWidth();
    height = m.getHeight();
  }

  GLCapabilities getGLCapabilities() {
    return caps;
  }

  int getChosenIndex() {
    return chosenIndex;
  }

  public BufferedImage createCompatibleImage(int width, int height) {
    throw new RuntimeException("Unimplemented");
  }

  public BufferedImage createCompatibleImage(int width, int height,
                                             int transparency) {
    throw new RuntimeException("Unimplemented");
  }

  public VolatileImage createCompatibleVolatileImage(int width, int height) {
    throw new RuntimeException("Unimplemented");
  }

  public VolatileImage createCompatibleVolatileImage(int width, int height, int transparency) {
    throw new RuntimeException("Unimplemented");
  }

  public VolatileImage createCompatibleVolatileImage(int width, int height,
                                                     ImageCapabilities caps) throws AWTException {
    throw new RuntimeException("Unimplemented");
  }

  public VolatileImage createCompatibleVolatileImage(int width, int height,
                                                     ImageCapabilities caps, int transparency) throws AWTException {
    throw new RuntimeException("Unimplemented");
  }

  public Rectangle getBounds() {
    return new Rectangle(0, 0, width, height);
  }

  public ColorModel getColorModel() {
    throw new RuntimeException("Unimplemented");
  }

  public ColorModel getColorModel(int transparency) {
    throw new RuntimeException("Unimplemented");
  }

  public AffineTransform getDefaultTransform() {
    throw new RuntimeException("Unimplemented");
  }

  public GraphicsDevice getDevice() {
    return device;
  }

  public AffineTransform getNormalizingTransform() {
    throw new RuntimeException("Unimplemented");
  }
}
