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

#include "StdAfx.h"

D3dImageComponent::D3dImageComponent()
{
    init();
}


D3dImageComponent::D3dImageComponent(D3dCtx *_ctx, 
				     int _hashCode,
				     LPDIRECT3DTEXTURE8 _surf)
{
    ctx = _ctx;
    hashCode = _hashCode;
    surf = _surf;
    next = NULL;
}

D3dImageComponent::~D3dImageComponent()
{
    if ((ctx != NULL) && (surf != NULL)) {
	ctx->freeResource(surf);
    }
}

VOID D3dImageComponent::init()
{
    next = NULL;
    surf = NULL;
    hashCode = 0;
    ctx = NULL;
}

D3dImageComponent* D3dImageComponent::add(D3dImageComponent *list,
					  D3dCtx *ctx, 
					  int hashCode,
					  LPDIRECT3DTEXTURE8 surf)
{
    D3dImageComponent *p = list->next;
 
    D3dImageComponent *ic = new D3dImageComponent(ctx, hashCode, surf);

    if (ic == NULL) {
	return NULL;
    }
    list->next = ic;
    ic->next = p;
    return ic;
}

D3dImageComponent* D3dImageComponent::find(D3dImageComponent *list,
					   D3dCtx *ctx, 
					   int hashCode)
{
    // skip the first dummy node
    D3dImageComponent *p = list->next;
    while (p != NULL) {
	if ((p->ctx == ctx) &&
	    (p->hashCode == hashCode)) {
	    return p;
	}
	p = p->next;
    }
    return NULL;
}


VOID D3dImageComponent::remove(D3dImageComponent *list,
			       D3dCtx *ctx, int hashCode)
{
    // skip the first dummy node
    D3dImageComponent *p = list->next;
    D3dImageComponent *q = list;

    while (p != NULL) {
	if ((p->ctx == ctx) &&
	    (p->hashCode == hashCode)) {
	    q->next = p->next;
	    delete p;
	    break;
	}
	q = p;
	p = p->next;
	
    }
}


VOID D3dImageComponent::remove(D3dImageComponent *list,
			       int hashCode)
{
    // skip the first dummy node
    D3dImageComponent *p = list->next;
    D3dImageComponent *q = list;

    while (p != NULL) {
	if (p->hashCode == hashCode) {
	    q->next = p->next;
	    delete p;
	    // continue for image in another ctx
	    p = q->next;
	} else {
	    q = p;
	    p = p->next;
	}
    }
}

VOID D3dImageComponent::remove(D3dImageComponent *list,
			       D3dCtx *ctx)
{
    // skip the first dummy node
    D3dImageComponent *p = list->next;
    D3dImageComponent *q = list;

    while (p != NULL) {
	if (p->ctx == ctx) {
	    q->next = p->next;
	    delete p;
	    p = q->next;
	    // continue for other images
	} else {
	    q = p;
	    p = p->next;
	}
    }
}

VOID D3dImageComponent::remove(D3dImageComponent *list,
			       D3dImageComponent *ic)
{
    // skip the first dummy node
    D3dImageComponent *p = list->next;
    D3dImageComponent *q = list;

    while (p != NULL) {
	if (p == ic) {
	    q->next = p->next;
	    delete p;
	    break;
	} 
	q = p;
	p = p->next;
    }
}

VOID D3dImageComponent::removeAll(D3dImageComponent *list)
{
    // skip the first dummy node
    D3dImageComponent *q, *p = list->next;

    list->next = NULL;

    while (p != NULL) {
	q = p->next;
	delete p;
	p = q;
    }
}
