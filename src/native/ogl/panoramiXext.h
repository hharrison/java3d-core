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

/* $TOG: panoramiXext.h /main/3 1998/02/13 13:08:51 kaleb $ */
/*****************************************************************

Copyright (c) 1991, 1997 Digital Equipment Corporation, Maynard, Massachusetts.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software.

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
DIGITAL EQUIPMENT CORPORATION BE LIABLE FOR ANY CLAIM, DAMAGES, INCLUDING, 
BUT NOT LIMITED TO CONSEQUENTIAL OR INCIDENTAL DAMAGES, OR OTHER LIABILITY, 
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR 
IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of Digital Equipment Corporation 
shall not be used in advertising or otherwise to promote the sale, use or other
dealings in this Software without prior written authorization from Digital 
Equipment Corporation.

******************************************************************/
/*  
 *	PanoramiX definitions
 */

/* THIS IS NOT AN X PROJECT TEAM SPECIFICATION */

#define PANORAMIX_MAJOR_VERSION         1       /* current version number */
#define PANORAMIX_MINOR_VERSION         0

typedef struct {
    Window  window;         /* PanoramiX window - may not exist */
    int	    screen;
    int     State;          /* PanroamiXOff, PanoramiXOn */
    int	    width;	    /* width of this screen */
    int     height;	    /* height of this screen */
    int     ScreenCount;    /* real physical number of screens */
    XID     eventMask;      /* selected events for this client */
} XPanoramiXInfo;    

extern XPanoramiXInfo *XPanoramiXAllocInfo (
#if NeedFunctionPrototypes
    void
#endif
);        

#define XINERAMA_PLACE_TOP	1
#define XINERAMA_PLACE_BOTTOM	2
#define XINERAMA_PLACE_RIGHT	4
#define XINERAMA_PLACE_LEFT	8

#ifndef _XINERAMAINFO_
#define _XINERAMAINFO_

#define XinID	int
#define MAXSCREEN 16
#define DELTA	int
#define POINT	int

typedef struct subwid
{
	XinID	wid;	/* sub window id */
	DELTA	dx,dy;	/* delta in screen co-ord from virtual zero */
	POINT	x,y;	/* location of window in screen co-ord */
	DELTA 	wdx,wdy;/* size of window in screen co-ord */
}SubWID, *pSubWID;

typedef struct xineramainfo
{
	XinID 	wid;	/* Window ID of requested virtual window */
	SubWID	subs[MAXSCREEN];	/* there will be 16 slots */
}XineramaInfo, *pXineramaInfo;
#endif

#ifndef NO_PROTO_HERE
Bool XDgaGetXineramaInfo(
#if NeedFunctionPrototypes
/* this brakes the server
Display *, int, XID, XineramaInfo *
*/
#endif
);
#endif
