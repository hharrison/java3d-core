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

D3dImageComponent RasterList;
D3dImageComponent BackgroundImageList;

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_RasterRetained_execute(JNIEnv *env, 
		jobject obj, jlong ctx, jobject geo, 
                jboolean updateAlpha, jfloat alpha,
                jint type, jint w_raster, jint h_raster,
                jint x_offset, jint y_offset, jfloat x, jfloat y, jfloat z,
	        jbyteArray imageYdown)
{
    jfieldID id;
    int width, height;
    int hashCode;
    D3DVERTEX worldCoord;
    D3DTLVERTEX screenCoord;
    
    GetDevice();

    int startx = x_offset;
    int starty = y_offset;
    int endx = x_offset + w_raster;
    int endy = y_offset + h_raster;

    jclass geo_class =  env->GetObjectClass(geo);

    if ((type == javax_media_j3d_Raster_RASTER_COLOR) || 
	(type == javax_media_j3d_Raster_RASTER_COLOR_DEPTH)) {

	int alphaChanged = 0;  	// used so we can get alpha data from
				// JNI before using it so we can use
				// GetPrimitiveArrayCritical

	id = env->GetFieldID(geo_class, "image",
			     "Ljavax/media/j3d/ImageComponent2DRetained;");
	jobject image = env->GetObjectField(geo, id);


	jclass image_class = env->GetObjectClass(image);

	if (image_class == NULL) {
	    return;
	}
	/*	
	id = env->GetFieldID(image_class, "surfaceDirty", "I");
	if (env->GetIntField(image, id) == NOTLIVE) {
	    return;
	}
	*/
	id = env->GetFieldID(image_class, "width", "I");
	width = env->GetIntField(image, id);
	id = env->GetFieldID(image_class, "height", "I");
	height = env->GetIntField(image, id);

	id = env->GetFieldID(image_class, "hashId", "I");
	hashCode = env->GetIntField(image, id);

	// clipping
	if (startx > width) {
	    startx = width;
	} else if (startx < 0) {
	    startx = 0;
	}
	if (starty > height) {
	    starty = height;
	} else if (starty < 0) {
	    starty = 0;
	}
	if (endx > width) {
	    endx = width;
	} else if (endx < 0) {
	    endx = 0;
	}
	if (endy > height) {
	    endy = height;
	} else if (endy < 0) {
	    endy = 0;
	}

	// raster position is upper left corner, default for Java3D 
	// ImageComponent currently has the data reverse in Y
	worldCoord.x = x;
	worldCoord.y = y;
	worldCoord.z = z;

	lockImage();

	D3dImageComponent* d3dImage = 
	    D3dImageComponent::find(&RasterList, d3dCtx, hashCode);

	LPDIRECT3DTEXTURE8 surf = NULL ;

	if ((d3dImage == NULL) || (d3dImage->surf == NULL)) {

	    surf = createSurfaceFromImage(env, image, ctx,
					  width, height, imageYdown);

	    if (surf == NULL) {
		if (d3dImage != NULL) {
		    D3dImageComponent::remove(&RasterList, d3dImage);
		}
		unlockImage();
		return;
	    }
	    if (d3dImage == NULL) {
		d3dImage = D3dImageComponent::add(&RasterList, d3dCtx, hashCode, surf);

		if (d3dImage == NULL) {
		    return;
		}

	    } else {
		d3dImage->surf = surf;
	    }
	} 

	d3dCtx->transform(&worldCoord, &screenCoord);
	if ((screenCoord.sz >= 0) && (screenCoord.sz <= 1)) {
	    screenCoord.sx -= 0.5f;
	    screenCoord.sy -= 0.5f;
	    drawTextureRect(d3dCtx, device, d3dImage->surf, screenCoord,
			    startx, starty, endx, endy,
			    endx - startx, endy - starty, false); 
	}
	unlockImage();

    } 

    if ((type == javax_media_j3d_Raster_RASTER_DEPTH) || 
	(type == javax_media_j3d_Raster_RASTER_COLOR_DEPTH)) {
	id = env->GetFieldID(geo_class,	"depthComponent",
			     "Ljavax/media/j3d/DepthComponentRetained;");

	jobject depth = env->GetObjectField(geo, id);
	jclass depth_class = env->GetObjectClass(depth);

	if (depth_class == NULL) {
	    return;
	}
	id = env->GetFieldID(depth_class, "type", "I");
	int depth_type = env->GetIntField(depth, id);
	id = env->GetFieldID(depth_class, "width", "I");
	width = env->GetIntField(depth, id);
	id = env->GetFieldID(depth_class, "height", "I");
	height = env->GetIntField(depth, id);


    
	// clipping
	if (startx > width) {
	    startx = width;
	} else if (startx < 0) {
	    startx = 0;
	}
	if (starty > height) {
	    starty = height;
	} else if (starty < 0) {
	    starty = 0;
	}
	if (endx > width) {
	    endx = width;
	} else if (endx < 0) {
	    endx = 0;
	}
	if (endy > height) {
	    endy = height;
	} else if (endy < 0) {
	    endy = 0;
	}
	
	int h = endy - starty;
	int w = endx - startx;

	// raster position is upper left corner, default for Java3D 
	// ImageComponent currently has the data reverse in Y
	if ((h > 0) && (w > 0)) {
	    worldCoord.x = x;
	    worldCoord.y = y;
	    worldCoord.z = z;

	    d3dCtx->transform(&worldCoord, &screenCoord);

	    if (d3dCtx->depthStencilSurface == NULL) {
		HRESULT hr =
		    device->GetDepthStencilSurface(&d3dCtx->depthStencilSurface);
		if (FAILED(hr)) {
		    if (debug) {
			printf("[Java3D] Fail to get depth stencil surface %s\n",
			       DXGetErrorString8(hr));
		    }
		    return;
		}
  	    }

	    if (depth_type == javax_media_j3d_DepthComponentRetained_DEPTH_COMPONENT_TYPE_INT) { 
		id = env->GetFieldID(depth_class, "depthData","[I");
		jintArray intData_array = (jintArray) env->GetObjectField(depth, id);
		jint * intData = (jint *) env->GetPrimitiveArrayCritical(
							 intData_array,  NULL);
		copyDepthToSurface(d3dCtx,
				   device, 
				   screenCoord.sx, 
				   screenCoord.sy, 
				   x_offset, y_offset,
				   w, h,width, height,
				   intData, d3dCtx->depthStencilSurface);
		env->ReleasePrimitiveArrayCritical(intData_array,
						   intData, 0);
	    } else { // javax_media_j3d_DepthComponentRetained_DEPTH_COMPONENT_TYPE_FLOAT

		id = env->GetFieldID(depth_class, "depthData","[F");
		jfloatArray floatData_array = (jfloatArray)
		    env->GetObjectField(depth, id);
		
		jfloat *floatData = (jfloat *) env->GetPrimitiveArrayCritical(
							      floatData_array, NULL);
		copyDepthToSurface(d3dCtx,
				   device, 
				   screenCoord.sx, 
				   screenCoord.sy, 
				   x_offset, y_offset, 
				   w, h, width, height,
				   floatData, d3dCtx->depthStencilSurface);
		env->ReleasePrimitiveArrayCritical(floatData_array,
						   floatData, 0);
	    }
	}
    }
}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_RasterRetained_executeTiled(JNIEnv *env, 
		jobject obj, jlong ctx, jobject geo, 
                jint format, jint w_raster, jint h_raster,
                jint x_offset, jint y_offset, jint deltaw, jint deltah,
  	        jfloat x, jfloat y, jfloat z, jbyteArray tile)
{
    // This is is not used by both OGL and D3D
}
