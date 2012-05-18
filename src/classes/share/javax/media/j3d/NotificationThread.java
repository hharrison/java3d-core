/*
 * Copyright 2005-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.LinkedList;

/**
 * The NotificationThread class is used for asynchronous error notification,
 * such as notifying ShaderError listeners.
 */
class NotificationThread extends Thread {
    // action flag for runMonitor
    private static final int WAIT   = 0;
    private static final int NOTIFY = 1;
    private static final int STOP   = 2;

    private volatile boolean running = true;
    private boolean waiting = false;
    private boolean ready = false;

    private LinkedList notificationQueue = new LinkedList();

    /**
     * Creates a new instance of NotificationThread
     */
    NotificationThread(ThreadGroup t) {
        // Only one notification thread for the entire system
	super(t, "J3D-NotificationThread");
    }

    /**
     * Adds a notification message to the queue
     */
    synchronized void addNotification(J3dNotification n) {
        notificationQueue.add(n);
        runMonitor(NOTIFY);
    }

    /**
     * Gets the list of queued notification messages
     */
    private synchronized J3dNotification[] getNotifications() {
        J3dNotification[] notifications = (J3dNotification[])notificationQueue.toArray(new J3dNotification[0]);
        notificationQueue.clear();
        return notifications;
    }

    /**
     * Processes all pending notification messages
     */
    private void processNotifications() {
        J3dNotification[] notifications = getNotifications();

        for (int i = 0; i < notifications.length; i++) {
            J3dNotification n = notifications[i];
            switch (n.type) {
            case J3dNotification.SHADER_ERROR:
                n.universe.notifyShaderErrorListeners((ShaderError)n.args[0]);
                break;
            case J3dNotification.RENDERING_ERROR:
                VirtualUniverse.notifyRenderingErrorListeners((RenderingError)n.args[0]);
                break;
            default:
                System.err.println("J3dNotification.processNotifications: unrecognized type = " + n.type);
            }
        }
    }

    // Called from MasterControlThread
    void finish() {
	runMonitor(STOP);
    }

    public void run() {
	while (running) {
	    runMonitor(WAIT);

            processNotifications();
	}
//        System.err.println("Notification thread finished");
    }


    private synchronized void runMonitor(int action) {
        switch (action) {
        case WAIT:
            while (running && !ready) {
                waiting = true;
                try {
                    wait();
                } catch (InterruptedException e) {
                }
                waiting = false;
            }
            ready = false;
            break;
        case NOTIFY:
            ready = true;
            if (waiting) {
                notify();
            }
            break;
        case STOP:
            running = false;
            notify();
            break;
        default:
            // Should never get here...
            assert(false);
        }
    }

}
