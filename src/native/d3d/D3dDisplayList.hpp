/*
 * $RCSfile$
 *
 * Copyright 2000-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

#if !defined(D3DDISPLAYLIST_H)
#define D3DDISPLAYLIST_H

#include "StdAfx.h"
#include "D3dVertexBuffer.hpp"


class D3dDisplayList {
public:
    D3dDisplayList();
    ~D3dDisplayList();
    VOID add(LPD3DVERTEXBUFFER vBuffer);
    VOID render(D3dCtx *d3dCtx);
    VOID optimize(D3dCtx *d3dCtx);
    BOOL isQuad(LPD3DVERTEXBUFFER p);
    LPD3DVERTEXBUFFER createMergedVB(D3dCtx *d3dCtx,
				     D3dVertexBuffer **p,
				     D3dVertexBuffer **q,
				     DWORD vcount,
				     DWORD indexCount);

private:
    D3dVertexBufferVector vBufferVec;
};
typedef D3dDisplayList* LPD3DDISPLAYLIST;
#endif
