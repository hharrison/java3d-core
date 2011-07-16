/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * J3dNotification is used to hold data for asynchronous error notification.
 */

class J3dNotification extends Object {
    /**
     * The various notification types.
     */
    static final int INVALID_TYPE       = -1;
    static final int SHADER_ERROR       =  0;
    static final int RENDERING_ERROR    =  1;

    /**
     * This holds the type of this message
     */
    int type = INVALID_TYPE;

    /**
     * The universe that this message originated from
     */
    VirtualUniverse universe;

    /**
     * The arguements for a message, 6 for now
     */
    static final int MAX_ARGS = 6;

    Object[] args = new Object[MAX_ARGS];

}
