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

package org.jogamp.java3d;

import java.awt.AWTException;
import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.ImageCapabilities;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;

import com.jogamp.nativewindow.awt.AWTGraphicsDevice;
import com.jogamp.opengl.GLCapabilities;

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
  private AWTGraphicsDevice awtGraphicsDevice;
  // Needed for Screen3D
  private int width;
  private int height;

  JoglGraphicsConfiguration(GLCapabilities caps, int chosenIndex, GraphicsDevice device) {
    super();
    this.caps = caps;
    this.chosenIndex = chosenIndex;
    this.device = device;
    //FIXME unit id?
    this.awtGraphicsDevice = new AWTGraphicsDevice(this.device, 0);
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

  @Override
  public BufferedImage createCompatibleImage(int width, int height) {
    throw new RuntimeException("Unimplemented");
  }

  @Override
  public BufferedImage createCompatibleImage(int width, int height,
                                             int transparency) {
    throw new RuntimeException("Unimplemented");
  }

  @Override
  public VolatileImage createCompatibleVolatileImage(int width, int height) {
    throw new RuntimeException("Unimplemented");
  }

  @Override
  public VolatileImage createCompatibleVolatileImage(int width, int height, int transparency) {
    throw new RuntimeException("Unimplemented");
  }

  @Override
  public VolatileImage createCompatibleVolatileImage(int width, int height,
                                                     ImageCapabilities caps) throws AWTException {
    throw new RuntimeException("Unimplemented");
  }

  @Override
  public VolatileImage createCompatibleVolatileImage(int width, int height,
                                                     ImageCapabilities caps, int transparency) throws AWTException {
    throw new RuntimeException("Unimplemented");
  }

  @Override
  public Rectangle getBounds() {
    return new Rectangle(0, 0, width, height);
  }

  @Override
  public ColorModel getColorModel() {
    throw new RuntimeException("Unimplemented");
  }

  @Override
  public ColorModel getColorModel(int transparency) {
    throw new RuntimeException("Unimplemented");
  }

  @Override
  public AffineTransform getDefaultTransform() {
    throw new RuntimeException("Unimplemented");
  }

  @Override
  public GraphicsDevice getDevice() {
    return device;
  }

  public AWTGraphicsDevice getAwtGraphicsDevice() {
    return awtGraphicsDevice;
  }

  @Override
  public AffineTransform getNormalizingTransform() {
    throw new RuntimeException("Unimplemented");
  }
}
