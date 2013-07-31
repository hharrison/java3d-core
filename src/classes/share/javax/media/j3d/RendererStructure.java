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


/**
 * A renderer structure is an object that organizes messages
 * to the renderer thread.
 */
class RendererStructure extends J3dStructure{

    /**
     * This constructor does nothing
     */
    RendererStructure() {
	super(null, J3dThread.RENDER_THREAD);
    }

    /**
     * Returns all messages in the queue.
     */
    J3dMessage[] getMessages() {
	int sz;

        synchronized (messageList) {
            if ((sz = messageList.size()) > 0) {
		if (msgList.length < sz) {
		    msgList = new J3dMessage[sz];
		}
		messageList.toArrayAndClear(msgList);
            }
        }

	nMessage = sz;
        return msgList;
    }


    @Override
    void processMessages(long referenceTime) {}

    @Override
    void removeNodes(J3dMessage m) {}

    @Override
    void cleanup() {}
}
