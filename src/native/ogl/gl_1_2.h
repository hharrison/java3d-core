/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

#ifndef __gl_1_2_h_
#define __gl_1_2_h_

#ifdef __cplusplus
extern "C" {
#endif
    
/*
 * Portions of this code were derived from work done by the Blackdown
 * group (www.blackdown.org), who did the initial Linux implementation
 * of the Java 3D API.
 */

#ifndef GL_VERSION_1_2

#ifndef GL_BGR    
#define GL_BGR                            0x80E0
#endif

#ifndef GL_BGRA
#define GL_BGRA                           0x80E1
#endif

    
#ifndef GL_LIGHT_MODEL_COLOR_CONTROL
#define GL_LIGHT_MODEL_COLOR_CONTROL      0x81F8
#endif

#ifndef GL_SEPARATE_SPECULAR_COLOR
#define GL_SEPARATE_SPECULAR_COLOR        0x81FA
#endif

#ifndef GL_SINGLE_COLOR
#define GL_SINGLE_COLOR                   0x81F9
#endif

#endif /* GL_VERSION_1_2 */


#ifndef GL_ARB_texture_env_combine
#define GL_COMBINE_ARB                    0x8570
#define GL_COMBINE_RGB_ARB                0x8571
#define GL_COMBINE_ALPHA_ARB              0x8572
#define GL_RGB_SCALE_ARB                  0x8573
#define GL_ADD_SIGNED_ARB                 0x8574
#define GL_INTERPOLATE_ARB                0x8575
#define GL_SUBTRACT_ARB			  0x84E7
#define GL_CONSTANT_ARB                   0x8576
#define GL_PRIMARY_COLOR_ARB              0x8577
#define GL_PREVIOUS_ARB                   0x8578
#define GL_SOURCE0_RGB_ARB                0x8580
#define GL_SOURCE1_RGB_ARB                0x8581
#define GL_SOURCE2_RGB_ARB                0x8582
#define GL_SOURCE0_ALPHA_ARB              0x8588
#define GL_SOURCE1_ALPHA_ARB              0x8589
#define GL_SOURCE2_ALPHA_ARB              0x858A
#define GL_OPERAND0_RGB_ARB               0x8590
#define GL_OPERAND1_RGB_ARB               0x8591
#define GL_OPERAND2_RGB_ARB               0x8592
#define GL_OPERAND0_ALPHA_ARB             0x8598
#define GL_OPERAND1_ALPHA_ARB             0x8599
#define GL_OPERAND2_ALPHA_ARB             0x859A
#endif /* GL_ARB_texture_env_combine */


#ifndef GL_ARB_texture_env_dot3
#define GL_DOT3_RGB_ARB			  0x86AE
#define GL_DOT3_RGBA_ARB		  0x86AF
#endif /* GL_ARB_texture_env_dot3 */


#ifndef GL_EXT_texture_env_dot3
#define GL_DOT3_RGB_EXT			  0x8740
#define GL_DOT3_RGBA_EXT		  0x8741
#endif /* GL_EXT_texture_env_dot3 */

#ifndef GL_EXT_texture_edge_clamp
#define GL_CLAMP_TO_EDGE_EXT             0x812F
#endif

#ifndef GL_ARB_texture_border_clamp
#define GL_CLAMP_TO_BORDER_ARB            0x812D
#endif

#ifndef GL_ARB_multisample
#if defined(SOLARIS) || defined(__linux__)
#define GLX_SAMPLE_BUFFERS_ARB            100000
#define GLX_SAMPLES_ARB                   100001
#endif
#ifdef  WIN32    
#define WGL_SAMPLE_BUFFERS_ARB            0x2041
#define WGL_SAMPLES_ARB                   0x2042
#endif
#define MULTISAMPLE_ARB                   0x809D
#define SAMPLE_ALPHA_TO_COVERATE_ARB      0x809E
#define SAMPLE_ALPHA_TO_ONE_ARB           0x809F
#define SAMPLE_COVERAGE_ARB               0x80A0
#define MULTISAMPLE_BIT_ARB               0x20000000
#define SAMPLE_BUFFERS_ARB                0x80A8
#define SAMPLES_ARB                       0x80A9
#define SAMPLE_COVERAGE_VALUE_ARB         0x80AA
#define SAMPLE_COVERAGE_INVERT_ARB        0x80AB   
#endif /* GL_ARB_multisample */

#ifndef MULTISAMPLE_ARB
#define MULTISAMPLE_ARB                   0x809D
#endif
    
#ifdef WIN32

/* define those EXT_pixel_format enums from 3DLabs register developer web site */
#ifndef WGL_SUPPORT_OPENGL_EXT
#define WGL_NUMBER_PIXEL_FORMATS_EXT                  0x2000
#define WGL_DRAW_TO_WINDOW_EXT                        0x2001
#define WGL_DRAW_TO_BITMAP_EXT                        0x2002
#define WGL_ACCELERATION_EXT                          0x2003
#define WGL_NEED_PALETTE_EXT                          0x2004
#define WGL_NEED_SYSTEM_PALETTE_EXT                   0x2005
#define WGL_SWAP_LAYER_BUFFERS_EXT                    0x2006
#define WGL_SWAP_METHOD_EXT                           0x2007
#define WGL_NUMBER_OVERLAYS_EXT                       0x2008
#define WGL_NUMBER_UNDERLAYS_EXT                      0x2009
#define WGL_TRANSPARENT_EXT                           0x200A
#define WGL_TRANSPARENT_VALUE_EXT                     0x200B
#define WGL_SHARE_DEPTH_EXT                           0x200C
#define WGL_SHARE_STENCIL_EXT                         0x200D
#define WGL_SHARE_ACCUM_EXT                           0x200E
#define WGL_SUPPORT_GDI_EXT                           0x200F
#define WGL_SUPPORT_OPENGL_EXT                        0x2010
#define WGL_DOUBLE_BUFFER_EXT                         0x2011
#define WGL_STEREO_EXT                                0x2012
#define WGL_PIXEL_TYPE_EXT                            0x2013
#define WGL_COLOR_BITS_EXT                            0x2014
#define WGL_RED_BITS_EXT                              0x2015
#define WGL_RED_SHIFT_EXT                             0x2016
#define WGL_GREEN_BITS_EXT                            0x2017
#define WGL_GREEN_SHIFT_EXT                           0x2018
#define WGL_BLUE_BITS_EXT                             0x2019
#define WGL_BLUE_SHIFT_EXT                            0x201A
#define WGL_ALPHA_BITS_EXT                            0x201B
#define WGL_ALPHA_SHIFT_EXT                           0x201C
#define WGL_ACCUM_BITS_EXT                            0x201D
#define WGL_ACCUM_RED_BITS_EXT                        0x201E
#define WGL_ACCUM_GREEN_BITS_EXT                      0x201F
#define WGL_ACCUM_BLUE_BITS_EXT                       0x2020
#define WGL_ACCUM_ALPHA_BITS_EXT                      0x2021
#define WGL_DEPTH_BITS_EXT                            0x2022
#define WGL_STENCIL_BITS_EXT                          0x2023
#define WGL_AUX_BUFFERS_EXT                           0x2024
#define WGL_NO_ACCELERATION_EXT                       0x2025
#define WGL_GENERIC_ACCELERATION_EXT                  0x2026
#define WGL_FULL_ACCELERATION_EXT                     0x2027
#define WGL_SWAP_EXCHANGE_EXT                         0x2028
#define WGL_SWAP_COPY_EXT                             0x2029
#define WGL_SWAP_UNDEFINED_EXT                        0x202A
#define WGL_TYPE_RGBA_EXT                             0x202B
#define WGL_TYPE_COLORINDEX_EXT                       0x202C
#endif /*  WGL_SUPPORT_OPENGL_EXT */

/* define those ARB_pixel_format enums */
#ifndef WGL_SUPPORT_OPENGL_ARB
#define WGL_NUMBER_PIXEL_FORMATS_ARB    0x2000
#define WGL_SUPPORT_OPENGL_ARB          0x2010
#define WGL_DRAW_TO_WINDOW_ARB          0x2001
#define WGL_DOUBLE_BUFFER_ARB           0x2011
#define WGL_STEREO_ARB                  0x2012
#define WGL_RED_BITS_ARB                0x2015
#define WGL_GREEN_BITS_ARB              0x2017
#define WGL_BLUE_BITS_ARB               0x2019
#define WGL_DEPTH_BITS_ARB              0x2022
#endif /*  WGL_SUPPORT_OPENGL_ARB */

#endif /* WIN32 */

#ifdef __cplusplus
}
#endif
    
#endif
