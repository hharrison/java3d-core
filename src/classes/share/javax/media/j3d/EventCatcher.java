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
import java.awt.event.*;


/**
 * The EventCatcher class is used to track events on a Canvas3D using the
 * 1.1 event model.  Most events are sent to the canvas for processing.
 */
class EventCatcher extends Object implements ComponentListener, FocusListener,
		KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, WindowListener {

    // The canvas associated with this event catcher
    private Canvas3D canvas;
    static final boolean DEBUG = false;
    private boolean stopped = false;

    /**
     * flags for event listeners
     */
    private boolean componentEvents = false;
    private boolean focusEvents = false;
    private boolean keyEvents = false;
    private boolean mouseEvents = false;
    private boolean mouseMotionEvents = false;
    private boolean mouseWheelEvents = false;
    private boolean mouseListenerAdded = false;

    EventCatcher(Canvas3D c) {
	canvas = c;

	if (VirtualUniverse.mc.isD3D()) {
	    enableComponentEvents();
	    enableKeyEvents();
	}
    }


    void enableComponentEvents() {

	if (!componentEvents) {
	    canvas.addComponentListener(this);
	    componentEvents = true;
	}
    }

    /*
    void disableComponentEvents() {
	if (componentEvents) {
	    canvas.removeComponentListener(this);
	    componentEvents = false;
	}
    }
    */

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
	    canvas.sendEventToBehaviorScheduler(e);
	    canvas.visible = true;
	    if (VirtualUniverse.mc.isD3D()) {
		canvas.notifyD3DPeer(Canvas3D.RESIZE);
	    }
	    canvas.evaluateActive();
	    repaint();
	    if (DEBUG) {
		System.out.println(e);
	    }
	}
    }

    public void componentHidden(ComponentEvent e) {
	canvas.sendEventToBehaviorScheduler(e);
	canvas.visible = false;
	if (DEBUG) {
	    System.out.println(e);
	}
    }

    public void componentMoved(ComponentEvent e) {
	canvas.sendEventToBehaviorScheduler(e);
	if (VirtualUniverse.mc.isD3D()) {
	    canvas.notifyD3DPeer(Canvas3D.RESIZE);
	}
	repaint();
	if (DEBUG) {
	    System.out.println(e);
	}
    }

    public void componentShown(ComponentEvent e) {
	canvas.sendEventToBehaviorScheduler(e);
	canvas.visible = true;
	canvas.evaluateActive();
	repaint();
	if (DEBUG) {
	    System.out.println(e);
	}
    }

    public void focusGained(FocusEvent e) {
	canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.out.println(e);
	}
    }

    public void focusLost(FocusEvent e) {
	canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.out.println(e);
	}
    }

    public void keyTyped(KeyEvent e) {
	canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.out.println(e);
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
	    System.out.println(e);
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
	    System.out.println(e);
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
	    System.out.println(e);
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
	    System.out.println(e);
	}
    }

    public void mouseExited(MouseEvent e) {
        if (mouseEvents) 
	    canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.out.println(e);
	}
    }

    public void mousePressed(MouseEvent e) {
        if (mouseEvents)
	    canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.out.println(e);
	}
    }

    public void mouseReleased(MouseEvent e) {
        if (mouseEvents)
	    canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.out.println(e);
	}
    }

    public void mouseDragged(MouseEvent e) {
	// Note : We don't have to test for mouseMotionEvent here because 
	// this routine will never be called unless mouseMotionEvent is enabled.
	canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.out.println(e);
	}
    }

    public void mouseMoved(MouseEvent e) {
	// Note : We don't have to test for mouseMotionEvent here because 
	// this routine will never be called unless mouseMotionEvent is enabled.
	canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.out.println(e);
	}
    }
    
    public void mouseWheelMoved(MouseWheelEvent e) {
	// Note : We don't have to test for mouseWheelEvent here because 
	// this routine will never be called unless mouseWheelEvent is enabled.
	canvas.sendEventToBehaviorScheduler(e);
	if (DEBUG) {
	    System.out.println(e);
	}
    }
    

    public void windowActivated(WindowEvent e) {
	windowOpened(e);
    }

    public void windowClosed(WindowEvent e) {
	if (DEBUG) {
	    System.out.println(e);
	}
	canvas.sendEventToBehaviorScheduler(e);
	canvas.visible = false;
	canvas.evaluateActive();
    }

    public void windowClosing(WindowEvent e) {
	if (DEBUG) {
	    System.out.println(e);
	}
	canvas.sendEventToBehaviorScheduler(e);
	canvas.visible = false;
	canvas.evaluateActive();
    }

    public void windowDeactivated(WindowEvent e) {
	if (DEBUG) {
	    System.out.println(e);
	}
	canvas.sendEventToBehaviorScheduler(e);
    }

    public void windowDeiconified(WindowEvent e) {
	if (DEBUG) {
	    System.out.println(e);
	}
	canvas.sendEventToBehaviorScheduler(e);
	canvas.visible = true;
        if (canvas.view != null)
            canvas.view.sendEventToSoundScheduler(e);
	canvas.evaluateActive();
	repaint();
    }

    public void windowIconified(WindowEvent e) {
	if (DEBUG) {
	    System.out.println(e);
	}
	canvas.sendEventToBehaviorScheduler(e);
	canvas.visible = false;
        if (canvas.view != null)
            canvas.view.sendEventToSoundScheduler(e);
	canvas.evaluateActive();
    }

    public void windowOpened(WindowEvent e) {
	if (DEBUG) {
	    System.out.println(e);
	}
	canvas.sendEventToBehaviorScheduler(e);
	canvas.visible = true;
	canvas.evaluateActive();
	repaint();
    }

    void repaint() {
	if (canvas.view != null) {
	    canvas.view.repaint();
	}
    }

    void reset() {
	focusEvents = false;
	keyEvents = false;
	componentEvents = false;
	mouseEvents = false;
	mouseMotionEvents = false;
	mouseWheelEvents = false;
	mouseListenerAdded = false;	
	stopped = false;
    }
		
}

