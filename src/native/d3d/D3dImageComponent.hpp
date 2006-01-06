/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

#if !defined(D3DIMAGECOMPONENT_H)
#define D3DIMAGECOMPONENT_H

#include "StdAfx.h"

class D3dImageComponent {
public:

    LPDIRECT3DTEXTURE9  surf;
    D3dCtx               *ctx;
    int                 hashCode;
    D3dImageComponent    *next;

    D3dImageComponent();

    D3dImageComponent(D3dCtx *ctx, int hashCode,
		      LPDIRECT3DTEXTURE9 surf);

    ~D3dImageComponent();

    VOID init();

    static D3dImageComponent* find(D3dImageComponent *list,
				   D3dCtx *ctx, int hashCode);

    static D3dImageComponent* add(D3dImageComponent *list,
				  D3dCtx *ctx, int hashCode,
				  LPDIRECT3DTEXTURE9 surf);

    static VOID remove(D3dImageComponent *list, D3dCtx *ctx, int hashCode);
    static VOID remove(D3dImageComponent *list, D3dCtx *ctx); 
    static VOID remove(D3dImageComponent *list, int hashCode); 
    static VOID remove(D3dImageComponent *list, D3dImageComponent *ic);
    static VOID removeAll(D3dImageComponent *list);
};

extern D3dImageComponent RasterList;
extern D3dImageComponent BackgroundImageList;

#endif
