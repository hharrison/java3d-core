/*
 * $RCSfile$
 *
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


/**
 * The EventCatcher class is used to track events on a Canvas3D using the
 * 1.1 event model.  Most events are sent to the canvas for processing.
 */
class EventCatcher extends Object implements ComponentListener, FocusListener,
		KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, WindowListener {

    // The canvas associated with this event catcher
    private Canvas3D canvas;
    private static final boolean DEBUG = false;
    private boolean stopped = false;

    /**
     * flags for event listeners
     */
    private boolean focusEvents = false;
    private boolean keyEvents = false;
    private boolean mouseEvents = false;
    private boolean mouseMotionEvents = false;
    private boolean mouseWheelEvents = false;
    private boolean mouseListenerAdded = false;

    EventCatcher(Canvas3D c) {
	canvas = c;

	if (VirtualUniverse.mc.isD3D()) {
	    enableKeyEvents();
	}
    }

    void enableFocusEvents() {
	if (!focusEvents) {
	    canvas.addFocusListener(this);
	    focusEvents = true;
	}
    }


    void disableFocusEvents() {
	if (focusEvents) {
	    canvas.removeFocusListener(this);
	    focusEvents = false;
	}
    }

    void enableKeyEvents() {
	if (!keyEvents) {
	    canvas.addKeyListener(this);
	    keyEvents = true;
	    // listen for mouseEntered events for keyboard focusing
            if (!mouseListenerAdded) {
                canvas.addMouseListener(this);
                mouseListenerAdded = true;
            }
	}
    }

    void disableKeyEvents() {
	if (keyEvents) {
	    canvas.removeKeyListener(this);
	    keyEvents = false;
	    // listen for mouseEntered events for keyboard focusing
	    if (!mouseEvents) {
		if (mouseListenerAdded) {
		    canvas.removeMouseListener(this);
		    mouseListenerAdded = false;
		}
	    }
	}
    }



    void enableMouseEvents() {
	if (!mouseEvents) {
	    mouseEvents = true;
            if (!mouseListenerAdded) {
	        canvas.addMouseListener(this);
                mouseListenerAdded = true;
            }
	}
    }

    void disableMouseEvents() {
	if (mouseEvents) {
	    mouseEvents = false;
	    if (!keyEvents) {
		if (mouseListenerAdded) {
		    canvas.removeMouseListener(this);
		    mouseListenerAdded = false;
		}
	    }
	}
    }

    void enableMouseMotionEvents() {
	if (!mouseMotionEvents) {
	    canvas.addMouseMotionListener(this);
	    mouseMotionEvents = true;
	}
    }


    void disableMouseMotionEvents() {
	if (mouseMotionEvents) {
	    canvas.removeMouseMotionListener(this);
	    mouseMotionEvents = false;
	}
    }

    void enableMouseWheelEvents() {
	if (!mouseWheelEvents) {
	    canvas.addMouseWheelListener(this);
	    mouseWheelEvents = true;
	}
    }


    void disableMouseWheelEvents() {
	if (mouseWheelEvents) {
	    canvas.removeMouseWheelListener(this);
	    mouseWheelEvents = false;
	}
    }


    public void componentResized(ComponentEvent e) {
	if (e.getSource() == canvas) {
	    if (DEBUG) {
		System.err.println(e);
	    }
	    canvas.sendEventToBehaviorScheduler(e);
	    if (VirtualUniverse.mc.isD3D()) {
		canvas.notifyD3DPeer(Canvas3D.RESIZE);
	    }
	    canvas.evaluateVisiblilty();
            canvas.redraw();
	}
    }

    public void componentMoved(ComponentEvent e) {
	if (e.getSource() == canvas) {
            if (DEBUG) {
                System.err.println(e);
            }
            canvas.sendEventToBehaviorScheduler(e);

            // Issue 458 - the following is not needed for a move
//            if (VirtualUniverse.mc.isD3D()) {
//                canvas.notifyD3DPeer(Canvas3D.RESIZE);
//            }
//            canvas.evaluateVisiblilty(true);
        }
    }

    public void componentHidden(ComponentEvent e) {
	if (DEBUG) {
	    System.err.println(e);
	}
	if (e.getSource() == canvas) {
            canvas.sendEventToBehaviorScheduler(e);
        }
	canvas.evaluateVisiblilty();
    }

    public void componentShown(ComponentEvent e) {
	if (DEBUG) {
	    System.err.println(e);
	}
	if (e.getSource() == canvas) {
            canvas.sendEventToBehaviorScheduler(e);
        }
	canvas.evaluateVisiblilty();
    }

    public void focusGained(FocusEvent e) {
	canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.err.println(e);
	}
    }

    public void focusLost(FocusEvent e) {
	canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.err.println(e);
	}
    }

    public void keyTyped(KeyEvent e) {
	canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.err.println(e);
	}
    }

    public void keyPressed(KeyEvent e) {
	canvas.sendEventToBehaviorScheduler(e);

	if (VirtualUniverse.mc.isD3D() &&
	    e.isAltDown() &&
	    (e.getKeyCode() == KeyEvent.VK_ENTER)) {
	    canvas.notifyD3DPeer(Canvas3D.TOGGLEFULLSCREEN);
	}

	if (DEBUG) {
	    System.err.println(e);
	}
    }

    public void keyReleased(KeyEvent e) {
	canvas.sendEventToBehaviorScheduler(e);
	if (stopped) {
	    stopped = false;
	} else {
	    stopped = true;
	}
	if (DEBUG) {
	    System.err.println(e);
	}
    }

    public void mouseClicked(MouseEvent e) {
//	if (keyEvents &&
//            (VirtualUniverse.mc.getRenderingAPI() !=
//	     MasterControl.RENDER_OPENGL_SOLARIS)) {
//	     // bug 4362074
//           canvas.requestFocus();
//	}

        if (mouseEvents) {
	    canvas.sendEventToBehaviorScheduler(e);
	}
	if (DEBUG) {
	    System.err.println(e);
	}
    }

    public void mouseEntered(MouseEvent e) {
//	if (keyEvents &&
//           (VirtualUniverse.mc.getRenderingAPI() ==
//	     MasterControl.RENDER_OPENGL_SOLARIS)) {
//	     // bug 4362074
//           canvas.requestFocus();
//	}
        if (mouseEvents) {
	    canvas.sendEventToBehaviorScheduler(e);
	}
	if (DEBUG) {
	    System.err.println(e);
	}
    }

    public void mouseExited(MouseEvent e) {
        if (mouseEvents)
	    canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.err.println(e);
	}
    }

    public void mousePressed(MouseEvent e) {
        if (mouseEvents)
	    canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.err.println(e);
	}
    }

    public void mouseReleased(MouseEvent e) {
        if (mouseEvents)
	    canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.err.println(e);
	}
    }

    public void mouseDragged(MouseEvent e) {
	// Note : We don't have to test for mouseMotionEvent here because
	// this routine will never be called unless mouseMotionEvent is enabled.
	canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.err.println(e);
	}
    }

    public void mouseMoved(MouseEvent e) {
	// Note : We don't have to test for mouseMotionEvent here because
	// this routine will never be called unless mouseMotionEvent is enabled.
	canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.err.println(e);
	}
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
	// Note : We don't have to test for mouseWheelEvent here because
	// this routine will never be called unless mouseWheelEvent is enabled.
	canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.err.println(e);
	}
    }

    /*
     * WindowListener methods
     */
    public void windowClosed(WindowEvent e) {
	if (DEBUG) {
	    System.err.println(e);
	}
	canvas.sendEventToBehaviorScheduler(e);
       // Issue 458 - Don't set canvas visible to false
    }

    public void windowClosing(WindowEvent e) {
	if (DEBUG) {
	    System.err.println(e);
	}
	canvas.sendEventToBehaviorScheduler(e);
        // Issue 458 - Don't set canvas.visible to false
    }

    public void windowActivated(WindowEvent e) {
	if (DEBUG) {
	    System.err.println(e);
	}
	canvas.sendEventToBehaviorScheduler(e);
    }

    public void windowDeactivated(WindowEvent e) {
	if (DEBUG) {
	    System.err.println(e);
	}
	canvas.sendEventToBehaviorScheduler(e);
    }

    public void windowDeiconified(WindowEvent e) {
	if (DEBUG) {
	    System.err.println(e);
	}
	canvas.sendEventToBehaviorScheduler(e);
        if (canvas.view != null) {
            canvas.view.sendEventToSoundScheduler(e);
        }
        canvas.evaluateVisiblilty();
    }

    public void windowIconified(WindowEvent e) {
	if (DEBUG) {
	    System.err.println(e);
	}
	canvas.sendEventToBehaviorScheduler(e);
        if (canvas.view != null) {
            canvas.view.sendEventToSoundScheduler(e);
        }
	canvas.evaluateVisiblilty();
    }

    public void windowOpened(WindowEvent e) {
	if (DEBUG) {
	    System.err.println(e);
	}
	canvas.sendEventToBehaviorScheduler(e);
	canvas.evaluateVisiblilty();
    }

    void reset() {
	focusEvents = false;
	keyEvents = false;
	mouseEvents = false;
	mouseMotionEvents = false;
	mouseWheelEvents = false;
	mouseListenerAdded = false;
	stopped = false;
    }

}

