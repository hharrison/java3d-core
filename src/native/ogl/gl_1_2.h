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
    
#if defined(SOLARIS) || defined(__linux__)
#ifndef GL_ARB_multisample
#define GLX_SAMPLE_BUFFERS_ARB            100000
#define GLX_SAMPLES_ARB                   100001

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
#endif /* SOLARIS */

#ifndef MULTISAMPLE_ARB
#define MULTISAMPLE_ARB                   0x809D
#endif


#ifdef __cplusplus
}
#endif
    
#endif
