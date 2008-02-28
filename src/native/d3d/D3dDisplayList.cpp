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

#include "StdAfx.h"
#include "D3dDisplayList.hpp"

D3dDisplayList::D3dDisplayList()
{
}

D3dDisplayList::~D3dDisplayList()
{
	for (ITER_LPD3DVERTEXBUFFER p = vBufferVec.begin();
	 p != vBufferVec.end(); p++) {
	 SafeDelete(*p);
    }
    vBufferVec.empty();
}

VOID D3dDisplayList::render(D3dCtx *d3dCtx)
{
    for (ITER_LPD3DVERTEXBUFFER p = vBufferVec.begin();
	 p != vBufferVec.end(); p++) {
	(*p)->render(d3dCtx);
    }
}

VOID D3dDisplayList::add(LPD3DVERTEXBUFFER vbuffer)
{
    vBufferVec.push_back(vbuffer);
}


BOOL D3dDisplayList::isQuad(LPD3DVERTEXBUFFER p)
{
    return (!p->isIndexPrimitive && (p->indexBuffer != NULL));
}

VOID D3dDisplayList::optimize(D3dCtx *d3dCtx)
{

    D3dVertexBufferVector vCloneBufferVec;
    D3dVertexBuffer **r = &(*vBufferVec.begin());

    for (; r != &(*vBufferVec.end()); r++) {
	vCloneBufferVec.push_back(*r);

    }

    vBufferVec.erase(vBufferVec.begin(),  vBufferVec.end());

    D3dVertexBuffer **vbegin = &(*vCloneBufferVec.begin());
    D3dVertexBuffer **vend = &(*vCloneBufferVec.end());
    D3dVertexBuffer **q = vbegin;
    D3dVertexBuffer **p;
    int primitiveType, vcounts, climit;
    int indexCounts = 0;
    BOOL merge;
    LPD3DVERTEXBUFFER mergedVB;
    BOOL isPointFlagUsed;
    DWORD vertexFormat;
    BOOL isIndexPrimitive;
    BOOL quadFlag;

    while (q != vend) {
	primitiveType = (*q)->primitiveType;
	climit = (*q)->maxVertexLimit;
	vcounts = (*q)->vcount;
	isPointFlagUsed = (*q)->isPointFlagUsed;
	vertexFormat = (*q)->vertexFormat;
	isIndexPrimitive = (*q)->isIndexPrimitive;
	quadFlag = isQuad(*q);

	if ((*q)->indexBuffer != NULL) {
	    indexCounts = (*q)->indexCount;
	}
	merge = false;
	p = q + 1;

	while (p != vend) {
	    if (((*p)->primitiveType == primitiveType) &&
		((*p)->vertexFormat == vertexFormat) &&
		((*p)->isIndexPrimitive == isIndexPrimitive) &&
		(isQuad(*p) == quadFlag) &&
		((*p)->isPointFlagUsed == isPointFlagUsed) &&
		// This means Mutliple VBs already use
		((*p)->totalVertexCount == (*p)->vcount)) {
		vcounts += (*p)->totalVertexCount;
		if ((*p)->indexBuffer != NULL) {
		    indexCounts += (*p)->totalIndexCount;
		}
		if ((vcounts > climit) || (indexCounts > climit)) {
		    break;
		}
		p++;
		merge = true;
	    } else {
		break;
	    }
	}

	if (merge) {
	    mergedVB = createMergedVB(d3dCtx, q, p, vcounts, indexCounts);
	    if (mergedVB != NULL) {
		for (r = q; r != p; r++) {
		    SafeDelete(*r);
		}
		vBufferVec.push_back(mergedVB);
	    } else {
		for (r = q; r != p; r++) {
		    vBufferVec.push_back(*r);
		}
	    }
	} else {
	    vBufferVec.push_back(*q);
	}
	q = p;
    }

    vCloneBufferVec.erase(vCloneBufferVec.begin(),  vCloneBufferVec.end());
}




LPD3DVERTEXBUFFER D3dDisplayList::createMergedVB(D3dCtx *d3dCtx,
						 D3dVertexBuffer **vstart,
						 D3dVertexBuffer **vend,
						 DWORD vcount,
						 DWORD indexCount)
{
    LPDIRECT3DDEVICE9 device = d3dCtx->pDevice;
    D3dVertexBuffer **r;
    UINT i;
    HRESULT hr;
    LPD3DVERTEXBUFFER vb = new D3dVertexBuffer();

    vb->primitiveType = (*vstart)->primitiveType;
    vb->isIndexPrimitive = (*vstart)->isIndexPrimitive;
    vb->isPointFlagUsed = (*vstart)->isPointFlagUsed;
    vb->vertexFormat = (*vstart)->vertexFormat;
    vb->stride = (*vstart)->stride;
    vb->ctx = (*vstart)->ctx;
    vb->vcount = vb->totalVertexCount = vcount;
    vb->indexCount = vb->totalIndexCount = indexCount;
    vb->maxVertexLimit = (*vstart)->maxVertexLimit;

    if (!vb->isPointFlagUsed) {
	hr = device->CreateVertexBuffer(vb->stride*vcount,
					D3DUSAGE_WRITEONLY,
					vb->vertexFormat,
					D3DPOOL_DEFAULT,
					&vb->buffer,
					NULL);
    } else {
	hr = device->CreateVertexBuffer(vb->stride*vcount,
					D3DUSAGE_WRITEONLY|D3DUSAGE_POINTS,
					vb->vertexFormat,
					D3DPOOL_DEFAULT,
					&vb->buffer,
					NULL);
    }

    if (FAILED(hr)) {
	return NULL;
    }
   	BYTE *bdst = NULL;
	WORD *wdst = NULL;
	UINT *idst = NULL;

    hr = vb->buffer->Lock(0, 0,(VOID**) &bdst , 0);
    if (FAILED(hr)) {
	SafeRelease(vb->buffer);
	return NULL;
    }

    if (indexCount > 0) {
	if (indexCount < 0xffff) {
	    hr = device->CreateIndexBuffer(indexCount*sizeof(WORD),
					   D3DUSAGE_WRITEONLY,
					   D3DFMT_INDEX16,
					   D3DPOOL_DEFAULT,
					   &vb->indexBuffer,
					   NULL);

	} else {
	    hr = device->CreateIndexBuffer(indexCount*sizeof(UINT),
					   D3DUSAGE_WRITEONLY,
					   D3DFMT_INDEX32,
					   D3DPOOL_DEFAULT,
					   &vb->indexBuffer,
					   NULL);
	}
	if (FAILED(hr)) {
	    vb->buffer->Unlock();
	    SafeRelease(vb->buffer);
	    return NULL;
	}
	if (indexCount <= 0xffff) {
	    hr = vb->indexBuffer->Lock(0, 0,(VOID**)  &wdst, 0);
	} else {
	    hr = vb->indexBuffer->Lock(0, 0,(VOID**)  &idst, 0);
	}
	if (FAILED(hr)) {
	    vb->buffer->Unlock();
	    SafeRelease(vb->buffer);
	    SafeRelease(vb->indexBuffer);
	    return NULL;
	}
    }

   	BYTE *bsrc = NULL;
	WORD *wsrc = NULL;
	UINT *isrc = NULL;
	UINT offset = 0;
    DWORD len;
    BOOL stripType = true;

    if ((vb->primitiveType == D3DPT_POINTLIST) ||
	(vb->primitiveType == D3DPT_LINELIST) ||
	(vb->primitiveType == D3DPT_TRIANGLELIST)) {
	vb->numVertices = new USHORT[1];
	if (indexCount <= 0) {
	    vb->numVertices[0] = vcount;
	} else {
	    vb->numVertices[0] = indexCount;
	}
	vb->numVerticesLen = 1;
	vb->stripLen = 1;
	stripType = false;
    }

    for (r = vstart; r != vend; r++) {
	hr = (*r)->buffer->Lock(0, 0,(VOID**) &bsrc, 0);

	if (FAILED(hr)) {
	    vb->buffer->Unlock();
	    if (indexCount > 0) {
		vb->indexBuffer->Unlock();
	    }
	    SafeRelease(vb->buffer);
	    SafeRelease(vb->indexBuffer);
	    return NULL;
	}

	if (indexCount > 0) {
	    if (indexCount <= 0xffff) {
		hr = (*r)->indexBuffer->Lock(0, 0, (VOID**)&wsrc, 0);
	    } else {
		hr = (*r)->indexBuffer->Lock(0, 0,(VOID**) &isrc, 0);
	    }
	    if (FAILED(hr)) {
		(*r)->buffer->Unlock();
		vb->buffer->Unlock();
		SafeRelease(vb->buffer);
		SafeRelease(vb->indexBuffer);
		return NULL;
	    }
	}
	len = (*r)->vcount*(*r)->stride;
	CopyMemory(bdst, bsrc, len);
	if (stripType) {
	    vb->appendStrides((*r)->stripLen, (*r)->numVertices);
	}
	if (indexCount > 0) {
	    if (wdst != NULL) {
		if (wsrc != NULL) {
		    for (i=0; i < (*r)->indexCount; i++) {
			*wdst++ = offset + *wsrc++;
		    }
		} else {
		    // should not happen
		    printf("[Java3D] Error in merging index vertex buffer\n");
		}

	    } else {
		if (wsrc != NULL) {
		    for (i=0; i < (*r)->indexCount; i++) {
			*idst++ = offset + *wsrc++;
		    }
		} else {
		    for (i=0; i < (*r)->indexCount; i++) {
			*idst++ = offset + *isrc++;
		    }
		}
	    }
	    offset += (*r)->vcount;
	}
	bdst += len;
	(*r)->buffer->Unlock();
	if (indexCount > 0) {
	    (*r)->indexBuffer->Unlock();
	    wsrc = NULL;
	    isrc = NULL;
	}
    }


    vb->buffer->Unlock();
    if (indexCount > 0) {
	vb->indexBuffer->Unlock();
    }

    if (vb->isIndexPrimitive && (indexCount <= 0)) {
	// QUAD is used, adjust size of index
	createQuadIndices(d3dCtx, vcount);
    }

    for (i=0; i < D3DDP_MAXTEXCOORD; i++) {
	vb->texCoordPosition[i] = -9999;
    }


    if (debug) {
	int n = 0;
	for (r = vstart; r != vend; r++) {
	    n++;
	}
	printf("Merge %d VB with primitiveType %d, vcount %d, indexCount %d\n",
	       n, vb->primitiveType, vcount, indexCount);
    }
    return vb;

}

