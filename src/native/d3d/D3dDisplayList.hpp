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
