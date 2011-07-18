/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.utils.behaviors.interpolators;

import javax.media.j3d.*;
import java.util.*;
import javax.vecmath.*;


/**
 * The CubicSplineSegment class creates the representation of a 
 * TCB (Kochanek-Bartels Spline).  This class takes 4 key frames as 
 * its input (using TCBKeyFrame). If interpolating between the i<sup>th</sup>
 * and (i+1)<sup>th</sup> key frame then the four key frames that need to 
 * be specified are the (i-1)<sup>th</sup>, i<sup>th</sup>, (i+1)<sup>th</sup> 
 * and (i+2)<sup>th</sup>  keyframes in order. The CubicSegmentClass
 * then pre-computes the hermite interpolation basis coefficients if the 
 * (i+1)<sup>th</sup> frame has the linear flag set to zero. These are used to 
 * calculate the interpolated position, scale and quaternions when they 
 * requested by the user using the getInterpolated* methods. If the the 
 * (i+1)<sup>th</sup> frame's linear flag is set to 1 then the class uses 
 * linear interpolation to calculate the interpolated position, sccale and 
 * quaternions it returns through the getInterpolated* methods. 
 *
 * @since Java3D 1.1
 */

public class CubicSplineSegment {

    // Legendre polynomial information for Gaussian quadrature of speed
    // for the domain [0,u], 0 <= u <= 1.

    // Legendre roots mapped to (root+1)/2
    static final double modRoot[] =
      {
       0.046910077,
       0.230765345,
       0.5,
       0.769234655,
       0.953089922
      };

    // original coefficients divided by 2
    static final double modCoeff[] =
      {
       0.118463442,
       0.239314335,
       0.284444444,
       0.239314335,
       0.118463442
      };

    // Key Frames
    TCBKeyFrame[] keyFrame = new TCBKeyFrame[4];
    
    // H.C 
    Point3f  c0, c1, c2, c3; // coefficients for position
    Point3f  e0, e1, e2, e3; // coefficients for scale 

    // variables for destination derivative
    float    one_minus_t_in; 
    float    one_minus_c_in;
    float    one_minus_b_in;
    float    one_plus_c_in;
    float    one_plus_b_in;
    float    ddb;
    float    dda;

    // variables for source derivative
    float    one_minus_t_out;
    float    one_minus_c_out;
    float    one_minus_b_out;
    float    one_plus_c_out; 
    float    one_plus_b_out;
    float    dsb;
    float    dsa;

    // Length of the spline segment
    float    length;

    // interpolation type
    int      linear;
    
    /**
     * Default constructor 
     */
    CubicSplineSegment () {

        length = 0;

    }

    /**
     * Creates a cubic spline segment between two key frames using the 
     * key frames provided. If creating a spline between the ith frame and 
     * the (i+1)<sup>th</sup> frame then send down the (i - 1)<sup>th</sup>, 
     * i<sup>th</sup> , (i+1)<sup>th</sup> and the (i+2)<sup>th</sup> key 
     * frames.  
     * 
     * @param kf0 (i - 1)<sup>th</sup> Key Frame
     * @param kf1 i<sup>th</sup> Key Frame 
     * @param kf2 (i + 1)<sup>th</sup> Key Frame
     * @param kf3 (i + 2)<sup>th</sup> Key Frame 
     */

    CubicSplineSegment (TCBKeyFrame kf0,  TCBKeyFrame kf1, TCBKeyFrame kf2,
                                                           TCBKeyFrame kf3) {

        // Copy KeyFrame information
        keyFrame[0] = new TCBKeyFrame(kf0); 
        keyFrame[1] = new TCBKeyFrame(kf1); 
        keyFrame[2] = new TCBKeyFrame(kf2); 
        keyFrame[3] = new TCBKeyFrame(kf3); 

        // if linear interpolation is requested then just set linear flag
        // if spline interpolation is needed then compute spline coefficients
        if (kf2.linear == 1) {
            this.linear = 1;
        } else {
            this.linear = 0;
            computeCommonCoefficients (kf0, kf1, kf2, kf3);
            computeHermiteCoefficients (kf0, kf1, kf2, kf3);
        }

        length = computeLength (1.0f);
        // System.out.println ("Segment length = " + length);

    }

    // compute the common coefficients
    private void computeCommonCoefficients (TCBKeyFrame kf0, 
                                            TCBKeyFrame kf1,
                                            TCBKeyFrame kf2, 
                                            TCBKeyFrame kf3) {

        // variables for destination derivative
        float  one_minus_t_in = 1.0f - kf1.tension;
        float  one_minus_c_in = 1.0f - kf1.continuity; 
        float  one_minus_b_in = 1.0f - kf1.bias;
        float  one_plus_c_in  = 1.0f + kf1.continuity;
        float  one_plus_b_in  = 1.0f + kf1.bias;

        // coefficients for the incoming Tangent
        ddb = one_minus_t_in * one_minus_c_in * one_minus_b_in; 
        dda = one_minus_t_in * one_plus_c_in * one_plus_b_in; 

        // variables for source derivative
        float  one_minus_t_out = 1.0f - kf2.tension; 
        float  one_minus_c_out = 1.0f - kf2.continuity; 
        float  one_minus_b_out = 1.0f - kf2.bias;
        float  one_plus_c_out  = 1.0f + kf2.continuity;
        float  one_plus_b_out  = 1.0f + kf2.bias;
                  
        // coefficients for the outgoing Tangent
        dsb = one_minus_t_in * one_plus_c_in * one_minus_b_in; 
        dsa = one_minus_t_in * one_minus_c_in * one_plus_b_in; 
    }


    // compute the hermite interpolation basis coefficients
    private void computeHermiteCoefficients (TCBKeyFrame kf0,
                                             TCBKeyFrame kf1,
                                             TCBKeyFrame kf2,
                                             TCBKeyFrame kf3) {


        Point3f deltaP = new Point3f();
        Point3f deltaS = new Point3f();

        // Find the difference in position and scale 
        deltaP.x = kf2.position.x - kf1.position.x;
        deltaP.y = kf2.position.y - kf1.position.y;
        deltaP.z = kf2.position.z - kf1.position.z;

        deltaS.x = kf2.scale.x - kf1.scale.x;
        deltaS.y = kf2.scale.y - kf1.scale.y;
        deltaS.z = kf2.scale.z - kf1.scale.z;
         
        // Incoming Tangent
        Point3f dd_pos    = new Point3f();
        Point3f dd_scale  = new Point3f();

        // If this is the first keyframe of the animation 
        if (kf0.knot == kf1.knot) {

           float ddab = 0.5f * (dda + ddb);

           // Position
           dd_pos.x = ddab * deltaP.x;
           dd_pos.y = ddab * deltaP.y;
           dd_pos.z = ddab * deltaP.z;

           // Scale 
           dd_scale.x = ddab * deltaS.x;
           dd_scale.y = ddab * deltaS.y;
           dd_scale.z = ddab * deltaS.z;

        } else {

           float adj0 = (kf1.knot - kf0.knot)/(kf2.knot - kf0.knot);

           // Position
           dd_pos.x = adj0 * 
              ((ddb * deltaP.x) + (dda * (kf1.position.x - kf0.position.x)));
           dd_pos.y = adj0 *
              ((ddb * deltaP.y) + (dda * (kf1.position.y - kf0.position.y)));
           dd_pos.z = adj0 * 
              ((ddb * deltaP.z) + (dda * (kf1.position.z - kf0.position.z)));

           // Scale 
           dd_scale.x = adj0 * 
              ((ddb * deltaS.x) + (dda * (kf1.scale.x - kf0.scale.x)));
           dd_scale.y = adj0 * 
              ((ddb * deltaS.y) + (dda * (kf1.scale.y - kf0.scale.y)));
           dd_scale.z = adj0 * 
              ((ddb * deltaS.z) + (dda * (kf1.scale.z - kf0.scale.z)));
        }
       
        // Outgoing Tangent
        Point3f ds_pos   = new Point3f();
        Point3f ds_scale = new Point3f();

        // If this is the last keyframe of the animation 
        if (kf2.knot == kf3.knot) {

           float dsab = 0.5f * (dsa + dsb);

           // Position
           ds_pos.x = dsab * deltaP.x;
           ds_pos.y = dsab * deltaP.y;
           ds_pos.z = dsab * deltaP.z;
           
           // Scale
           ds_scale.x = dsab * deltaS.x;
           ds_scale.y = dsab * deltaS.y;
           ds_scale.z = dsab * deltaS.z;

        } else {

           float adj1 = (kf2.knot - kf1.knot)/(kf3.knot - kf1.knot);

           // Position
           ds_pos.x = adj1 * 
             ((dsb * (kf3.position.x - kf2.position.x)) + (dsa * deltaP.x));
           ds_pos.y = adj1 * 
             ((dsb * (kf3.position.y - kf2.position.y)) + (dsa * deltaP.y));
           ds_pos.z = adj1 * 
             ((dsb * (kf3.position.z - kf2.position.z)) + (dsa * deltaP.z));

           // Scale
           ds_scale.x = adj1 * 
             ((dsb * (kf3.scale.x - kf2.scale.x)) + (dsa * deltaS.x));
           ds_scale.y = adj1 * 
             ((dsb * (kf3.scale.y - kf2.scale.y)) + (dsa * deltaS.y));
           ds_scale.z = adj1 * 
             ((dsb * (kf3.scale.z - kf2.scale.z)) + (dsa * deltaS.z));
        }

        // Calculate the coefficients of the polynomial for position
        c0 = new Point3f();
        c0.x = kf1.position.x;
        c0.y = kf1.position.y;
        c0.z = kf1.position.z;

        c1 = new Point3f();
        c1.x = dd_pos.x;
        c1.y = dd_pos.y;
        c1.z = dd_pos.z;

        c2 = new Point3f();
        c2.x = 3*deltaP.x - 2*dd_pos.x - ds_pos.x;
        c2.y = 3*deltaP.y - 2*dd_pos.y - ds_pos.y;
        c2.z = 3*deltaP.z - 2*dd_pos.z - ds_pos.z;

        c3 = new Point3f();
        c3.x = -2*deltaP.x + dd_pos.x + ds_pos.x;
        c3.y = -2*deltaP.y + dd_pos.y + ds_pos.y;
        c3.z = -2*deltaP.z + dd_pos.z + ds_pos.z;

        // Calculate the coefficients of the polynomial for scale 
        e0 = new Point3f();
        e0.x = kf1.scale.x;
        e0.y = kf1.scale.y;
        e0.z = kf1.scale.z;

        e1 = new Point3f();
        e1.x = dd_scale.x;
        e1.y = dd_scale.y;
        e1.z = dd_scale.z;

        e2 = new Point3f();
        e2.x = 3*deltaS.x - 2*dd_scale.x - ds_scale.x;
        e2.y = 3*deltaS.y - 2*dd_scale.y - ds_scale.y;
        e2.z = 3*deltaS.z - 2*dd_scale.z - ds_scale.z;

        e3 = new Point3f();
        e3.x = -2*deltaS.x + dd_scale.x + ds_scale.x;
        e3.y = -2*deltaS.y + dd_scale.y + ds_scale.y;
        e3.z = -2*deltaS.z + dd_scale.z + ds_scale.z;
    }


    /**
     * Computes the length of the curve at a given point between
     * key frames.
     * @param u specifies the point between keyframes where 0 <= u <= 1. 
     */

    public float computeLength (float u) {

        float result = 0f;

        // if linear interpolation
        if (linear == 1) {
            result = u*keyFrame[2].position.distance(keyFrame[1].position);
        } else {
            // Need to transform domain [0,u] to [-1,1].  If 0 <= x <= u
            // and -1 <= t <= 1, then x = u*(t+1)/2.
            int degree = 5;
            for (int i = 0; i < degree; i++)
                result += (float)modCoeff[i]*computeSpeed(u*(float)modRoot[i]);
            result *= u;
        }

        return result;
    }

    // Velocity along curve
    private float computeSpeed (float u) {
        Point3f v = new Point3f();

        v.x = c1.x + u * (2 * c2.x + 3 * u * c3.x);
        v.y = c1.y + u * (2 * c2.y + 3 * u * c3.y);
        v.z = c1.z + u * (2 * c2.z + 3 * u * c3.z);

        return (float)(Math.sqrt(v.x*v.x + v.y*v.y + v.z*v.z)); 
    }


    /**
     * Computes the interpolated quaternion along the curve at 
     * a given point between key frames. This routine uses linear
     * interpolation if the (i+1)<sup>th</sup> key frame's linear 
     * value is equal to 1. 
     * 
     * @param u specifies the point between keyframes where 0 <= u <= 1. 
     * @param newQuat returns the value of the interpolated quaternion 
     */

    public void getInterpolatedQuaternion (float u, Quat4f newQuat) {

        // if linear interpolation
        if (this.linear == 1) {
            double quatDot;

            quatDot = keyFrame[1].quat.x * keyFrame[2].quat.x + 
                      keyFrame[1].quat.y * keyFrame[2].quat.y +
                      keyFrame[1].quat.z * keyFrame[2].quat.z + 
                      keyFrame[1].quat.w * keyFrame[2].quat.w;

            if (quatDot < 0) {
                 newQuat.x = keyFrame[1].quat.x + 
                             (-keyFrame[2].quat.x - keyFrame[1].quat.x) * u;
                 newQuat.y = keyFrame[1].quat.y + 
                             (-keyFrame[2].quat.y - keyFrame[1].quat.y) * u;
                 newQuat.z = keyFrame[1].quat.z + 
                             (-keyFrame[2].quat.z - keyFrame[1].quat.z) * u;
                 newQuat.w = keyFrame[1].quat.w + 
                             (-keyFrame[2].quat.w - keyFrame[1].quat.w) * u;
            } else {
                 newQuat.x = keyFrame[1].quat.x + 
                             (keyFrame[2].quat.x - keyFrame[1].quat.x) * u;
                 newQuat.y = keyFrame[1].quat.y + 
                             (keyFrame[2].quat.y - keyFrame[1].quat.y) * u;
                 newQuat.z = keyFrame[1].quat.z + 
                             (keyFrame[2].quat.z - keyFrame[1].quat.z) * u;
                 newQuat.w = keyFrame[1].quat.w + 
                             (keyFrame[2].quat.w - keyFrame[1].quat.w) * u;
            } 

        } else {

            // TODO:
            // Currently we just use the great circle spherical interpolation
            // for quaternions irrespective of the linear flag. Eventually
            // we might want to do cubic interpolation of quaternions
            newQuat.interpolate (keyFrame[1].quat, keyFrame[2].quat, u);
        }
 
   }



    /**
     * Computes the interpolated scale along the curve at a given point
     * between key frames and returns a Point3f with the interpolated 
     * x, y, and z scale components. This routine uses linear
     * interpolation if the (i+1)<sup>th</sup> key frame's linear 
     * value is equal to 1. 
     * 
     * @param u specifies the point between keyframes where 0 <= u <= 1. 
     * @param newScale returns the interpolated x,y,z scale value in a Point3f
     */

    public void getInterpolatedScale (float u, Point3f newScale) {
        
        // if linear interpolation
        if (this.linear == 1) {

            newScale.x = keyFrame[1].scale.x + 
                      ((keyFrame[2].scale.x - keyFrame[1].scale.x) * u);
            newScale.y = keyFrame[1].scale.y + 
                      ((keyFrame[2].scale.y - keyFrame[1].scale.y) * u);
            newScale.z = keyFrame[1].scale.z + 
                      ((keyFrame[2].scale.z - keyFrame[1].scale.z) * u);

        } else {

            newScale.x = e0.x + u * (e1.x + u * (e2.x + u * e3.x));
            newScale.y = e0.y + u * (e1.y + u * (e2.y + u * e3.y));
            newScale.z = e0.z + u * (e1.z + u * (e2.z + u * e3.z));

        }
    }


    /**
     * Computes the interpolated position along the curve at a given point
     * between key frames and returns a Point3f with the interpolated 
     * x, y, and z scale components. This routine uses linear
     * interpolation if the (i+1)<sup>th</sup> key frame's linear
     * value is equal to 1.
     *
     * @param u specifies the point between keyframes where 0 <= u <= 1.
     * @param newPos returns the interpolated x,y,z position in a Point3f
     */

    public void getInterpolatedPosition (float u, Point3f newPos) {
        
        // if linear interpolation
        if (this.linear == 1) {
            newPos.x = keyFrame[1].position.x + 
                      ((keyFrame[2].position.x - keyFrame[1].position.x) * u);
            newPos.y = keyFrame[1].position.y + 
                      ((keyFrame[2].position.y - keyFrame[1].position.y) * u);
            newPos.z = keyFrame[1].position.z + 
                      ((keyFrame[2].position.z - keyFrame[1].position.z) * u);
        } else {

            newPos.x = c0.x + u * (c1.x + u * (c2.x + u * c3.x));
            newPos.y = c0.y + u * (c1.y + u * (c2.y + u * c3.y));
            newPos.z = c0.z + u * (c1.z + u * (c2.z + u * c3.z));

        }
    }


    /**
     * Computes the interpolated position along the curve at a given point
     * between key frames and returns a Vector3f with the interpolated 
     * x, y, and z scale components. This routine uses linear
     * interpolation if the (i+1)<sup>th</sup> key frame's linear
     * value is equal to 1.
     *
     * @param u specifies the point between keyframes where 0 <= u <= 1.
     * @param newPos returns the interpolated x,y,z position in a Vector3f. 
     */

    public void getInterpolatedPositionVector (float u, Vector3f newPos) {
        // if linear interpolation
        if (this.linear == 1) {
            newPos.x = keyFrame[1].position.x + 
                      ((keyFrame[2].position.x - keyFrame[1].position.x) * u);
            newPos.y = keyFrame[1].position.y + 
                      ((keyFrame[2].position.y - keyFrame[1].position.y) * u);
            newPos.z = keyFrame[1].position.z + 
                      ((keyFrame[2].position.z - keyFrame[1].position.z) * u);
        } else {

            newPos.x = c0.x + u * (c1.x + u * (c2.x + u * c3.x));
            newPos.y = c0.y + u * (c1.y + u * (c2.y + u * c3.y));
            newPos.z = c0.z + u * (c1.z + u * (c2.z + u * c3.z));

        }
    }

    /**
     * Computes the ratio of the length of the spline from the i<sup>th</sup>
     * key frame to the position specified by u to the length of the entire
     * spline segment from the i<sup>th</sup> key frame to the (i+1)
     * <sup>th</sup> key frame. When the (i+1)<sup>th</sup> key frame's linear
     * value is equal to 1, this is meaninful otherwise it should return u. 
     *
     * @param u specifies the point between keyframes where 0 <= u <= 1.
     * @return the interpolated ratio 
     */

    public float getInterpolatedValue (float u) {
        return (computeLength(u)/this.length);
    }
}
