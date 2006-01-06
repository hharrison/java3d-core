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

#include <stdio.h>
#include <jni.h>

#include "gldefs.h"

#ifdef DEBUG
/* Uncomment the following for VERBOSE debug messages */
/* #define VERBOSE */
#endif /* DEBUG */


extern void throwAssert(JNIEnv *env, char *str);


JNIEXPORT
void JNICALL Java_javax_media_j3d_RasterRetained_execute(JNIEnv *env, 
		jobject obj, jlong ctxInfo, jobject geo, 
                jboolean updateAlpha, jfloat alpha,
                jint type, jint w_raster, jint h_raster,
                jint x_offset, jint y_offset, jfloat x, jfloat y, jfloat z, jbyteArray imageYdown)


{
    jclass geo_class;
    JNIEnv table;

    jfieldID w_field, h_field;
    int width, height;

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;

    table = *env;

    geo_class =  (jclass) (*(table->GetObjectClass))(env, geo);

#ifdef VERBOSE
    fprintf(stderr, 
       "x %g ,y %g ,z %g ,w_raster %d,h_raster %d, x_offset %d, y_offset %d\n",
       x,y,z,w_raster,h_raster,x_offset,y_offset);
#endif

    if ((type == javax_media_j3d_Raster_RASTER_COLOR) ||
	(type == javax_media_j3d_Raster_RASTER_COLOR_DEPTH)) {
	jobject image;
	jclass image_class;
	jfieldID image_field, format_field; 
	jbyte *byteData;
	int format;
	int glformat;

	image_field = (jfieldID)(*(table->GetFieldID))(env, geo_class,
		    "image","Ljavax/media/j3d/ImageComponent2DRetained;");
	image = (jobject) (*(table->GetObjectField))(env, geo, image_field);

	if (image == NULL) {
	  return;
	}
	image_class = (jclass) (*(table->GetObjectClass))(env, image);

	format_field = (jfieldID) (*(table->GetFieldID))(env, image_class,
		    "storedYdownFormat", "I");
	format = (jint)(*(table->GetIntField))(env, image, format_field);
	w_field = (jfieldID) (*(table->GetFieldID))(env, image_class,
		    "width", "I");
	width = (jint)(*(table->GetIntField))(env, image, w_field);
	h_field = (jfieldID)(*(table->GetFieldID))(env, image_class,
		    "height", "I");
	height = (jint)(*(table->GetIntField))(env, image, h_field);


	/* 
	 * raster position is upper left corner, default for Java3D 
	 * ImageComponent currently has the data reverse in Y
	 */
	glPixelZoom(1.0, -1.0);
	glPixelStorei(GL_UNPACK_ROW_LENGTH, width);
	if (x_offset >= 0) {
	    glPixelStorei(GL_UNPACK_SKIP_PIXELS, x_offset);
	    if (x_offset + w_raster > width) {
		w_raster = width - x_offset;
	    }
	} else {
	    w_raster += x_offset;
	    if (w_raster > width) {
		w_raster  = width;
	    }
	}
	if (y_offset >= 0) {
	    glPixelStorei(GL_UNPACK_SKIP_ROWS, y_offset);
	    if (y_offset + h_raster > height) {
		h_raster = height - y_offset;
	    }
	} else {
	    h_raster += y_offset;
	    if (h_raster > height) {
		h_raster = height;
	    }
	}



	glRasterPos3f(x, y, z);

        byteData = (jbyte *)(*(table->GetPrimitiveArrayCritical))(env,
                    imageYdown, NULL);
		
/*
	{
        int i, j, *intData;

	fprintf(stderr, "format = %d, w_raster = %d, h_raster = %d\n\n", format, w_raster, h_raster);
	intData = (int*)byteData;
	for (i = 0; i < w_raster; i++) {
	    for (j = 0; j < h_raster; j++, intData++) {
		fprintf(stderr, " 0x%x", *intData);
	    }
	    fprintf(stderr, "\n");
	}
	}
	*/
        switch (format) {
        case FORMAT_BYTE_RGBA:
            glformat = GL_RGBA;
            break;
        case FORMAT_BYTE_RGB:
            glformat = GL_RGB;
            break;

        case FORMAT_BYTE_ABGR:         
	    if (ctxProperties->abgr_ext) { /* If its zero, should never come here! */
		glformat = GL_ABGR_EXT;
	    }
	    break;
        case FORMAT_BYTE_BGR:         
	    if (ctxProperties->bgr_ext) { /* If its zero, should never come here! */
		glformat = ctxProperties->bgr_ext_enum;
	    }
	    break;
        case FORMAT_BYTE_LA:
            glformat = GL_LUMINANCE_ALPHA;
            break;

        case FORMAT_BYTE_GRAY:
        case FORMAT_USHORT_GRAY:
	default:
	    throwAssert(env, "illegal format");
            break;
        }
	glDrawPixels(w_raster, h_raster, glformat, GL_UNSIGNED_BYTE, 
		     byteData);	

	(*(table->ReleasePrimitiveArrayCritical))(env, 
			imageYdown, byteData, 0);
    }

    if ((type == javax_media_j3d_Raster_RASTER_DEPTH) ||
	(type == javax_media_j3d_Raster_RASTER_COLOR_DEPTH)) {
	GLint draw_buf;
	jobject depth;
	jclass depth_class;
	jfieldID depth_field, depth_type_field;
	int depth_type;

	depth_field = (jfieldID)(*(table->GetFieldID))(env, geo_class,
		"depthComponent","Ljavax/media/j3d/DepthComponentRetained;");

	depth = (jobject) (*(table->GetObjectField))(env, geo, depth_field);
	if (depth == NULL) {
	  return;
	}
	depth_class = (jclass) (*(table->GetObjectClass))(env, depth);

	depth_type_field = (jfieldID) (*(table->GetFieldID))(env,
	depth_class, "type", "I");
	depth_type = (jint)(*(table->GetIntField))(env, depth,
	depth_type_field);

	w_field = (jfieldID) (*(table->GetFieldID))(env, depth_class,
		"width", "I");
	width = (jint)(*(table->GetIntField))(env, depth, w_field);
	h_field = (jfieldID)(*(table->GetFieldID))(env, depth_class,
		"height", "I");
	height = (jint)(*(table->GetIntField))(env, depth, h_field);


	glGetIntegerv(GL_DRAW_BUFFER, &draw_buf);
	/* disable draw buffer */
	glDrawBuffer(GL_NONE);
	/* glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE); */

	/* 
	 * raster position is upper left corner, default for Java3D 
	 * ImageComponent currently has the data reverse in Y
	 */
	glPixelStorei(GL_UNPACK_ROW_LENGTH, width);
	if (x_offset >= 0) {
	    glPixelStorei(GL_UNPACK_SKIP_PIXELS, x_offset);
	    if (x_offset + w_raster > width) {
		w_raster = width - x_offset;
	    }	    
	} else {
	    w_raster += x_offset;
	    if (w_raster > width) {
		w_raster  = width;
	    }	    
	}
	if (y_offset >= 0) {
	    glPixelStorei(GL_UNPACK_SKIP_ROWS, y_offset);
	    if (y_offset + h_raster > height) {
		h_raster = height - y_offset;
	    }
	} else {
	    h_raster += y_offset;
	    if (h_raster > height) {
		h_raster = height;
	    }
	}


	if (depth_type ==  javax_media_j3d_DepthComponentRetained_DEPTH_COMPONENT_TYPE_INT) { 
	    jfieldID intData_field;
	    jintArray intData_array;
	    jint *intData;

	    intData_field = (jfieldID)(*(table->GetFieldID))(env,
	    depth_class, "depthData","[I");

	    intData_array = (jintArray)(*(table->GetObjectField))(env, depth,
			intData_field);
	    intData = (jint *)(*(table->GetPrimitiveArrayCritical))(env,
	    intData_array, NULL);
	    glDrawPixels(w_raster, h_raster, GL_DEPTH_COMPONENT,
			GL_UNSIGNED_INT, intData);
	    (*(table->ReleasePrimitiveArrayCritical))(env, intData_array,
			intData, 0);
	} else { /* javax_media_j3d_DepthComponentRetained_DEPTH_COMPONENT_TYPE_FLOAT */
	    jfieldID floatData_field;
	    jfloatArray floatData_array;
	    jfloat *floatData;

	    floatData_field = (jfieldID)(*(table->GetFieldID))(env,
		    depth_class, "depthData","[F");
	    floatData_array = (jfloatArray)(*(table->GetObjectField))(env, depth,
		    floatData_field);
	    floatData = (jfloat *)(*(table->GetPrimitiveArrayCritical))(env,
		    floatData_array, NULL);
	    glDrawPixels(w_raster, h_raster, GL_DEPTH_COMPONENT,
		    GL_FLOAT, floatData);
	    (*(table->ReleasePrimitiveArrayCritical))(env, floatData_array,
		    floatData, 0);
	}

	/* re-enable draw buffer */
	glDrawBuffer(draw_buf);

    }
    glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
    glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
    glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);

}


#if 0

JNIEXPORT
void JNICALL Java_javax_media_j3d_RasterRetained_executeTiled(JNIEnv *env, 
		jobject obj, jlong ctxInfo, jobject geo, 
                jint format, jint w_raster, jint h_raster,
                jint x_offset, jint y_offset, jint deltaw, jint deltah, jfloat x, jfloat y, jfloat z, jbyteArray tile)


{
    jclass geo_class;
    JNIEnv table;
    jint j;
    int alphaChanged = 0;  	/* used so we can get alpha data from */
				/* JNI before using it so we can use  */
				/* GetPrimitiveArrayCritical */
    jobject image;
    jclass image_class;
    jfieldID byteData_field, image_field, format_field; 
    jbyteArray byteData_array;
    jbyte *byteData;
    int glformat;
    float rasterPos[3];
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;

    table = *env;


#ifdef VERBOSE
    fprintf(stderr, 
       "x %g ,y %g ,z %g ,w_raster %d,h_raster %d, x_offset %d, y_offset %d\n",
       x,y,z,w_raster,h_raster,x_offset,y_offset);
#endif


	/* 
	 * raster position is upper left corner, default for Java3D 
	 * ImageComponent currently has the data reverse in Y
	 */
	glPixelZoom(1.0, -1.0);
	/*	glPixelStorei(GL_UNPACK_ROW_LENGTH, width);*/
	if (x_offset >= 0) {
	    glPixelStorei(GL_UNPACK_SKIP_PIXELS, x_offset);
	    if (x_offset + w_raster > width) {
		w_raster = width - x_offset;
	    }	    
	} else {
	    w_raster += x_offset;
	    if (w_raster > width) {
		w_raster  = width;
	    }	    
	}
	if (y_offset >= 0) {
	    glPixelStorei(GL_UNPACK_SKIP_ROWS, y_offset);
	    if (y_offset + h_raster > height) {
		h_raster = height - y_offset;
	    }	    
	} else {
	    h_raster += y_offset;
	    if (h_raster > height) {
		h_raster = height;
	    }	    
	}
	
	if (deltaw == 0 && deltah == 0) {
	    glRasterPos3f(x, y, z);
	}
	else {
	    glGetFloatv(GL_CURRENT_RASTER_POSITION,rasterPos);
	    rasterPos[0] += (float)deltaw;
	    rasterPos[1] += (float)deltah;
	    glRasterPos3f(rasterPos[0], rasterPos[1], rasterPos[2]);
	}
		
	

        byteData = (jbyte *)(*(table->GetPrimitiveArrayCritical))(env,
                    tile, NULL);
        switch (format) {
        case FORMAT_BYTE_RGBA:
            glformat = GL_RGBA;
            break;
        case FORMAT_BYTE_RGB:
            glformat = GL_RGB;
            break;

        case FORMAT_BYTE_ABGR:         
	    if (ctxProperties->abgr_ext) { /* If its zero, should never come here! */
		glformat = GL_ABGR_EXT;
	    }
	    break;


        case FORMAT_BYTE_BGR:         
	    if (ctxProperties->bgr_ext) { /* If its zero, should never come here! */
		glformat = ctxProperties->bgr_ext_enum;
	    }
	    break;

        case FORMAT_BYTE_LA:
            glformat = GL_LUMINANCE_ALPHA;
            break;
        case FORMAT_BYTE_GRAY:
        case FORMAT_USHORT_GRAY:	    
	default:
	    throwAssert(env, "illegal format");
            break;
        }
	fprintf(stderr, "w_raster = %d, h_raster = %d, glformat = %d\n",w_raster, h_raster, glformat);
	glDrawPixels(w_raster, h_raster, glformat, GL_UNSIGNED_BYTE, 
		     byteData);	

	(*(table->ReleasePrimitiveArrayCritical))(env, 
			tile, byteData, 0);

	/* 	glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);*/
    glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
    glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);

}
#endif
