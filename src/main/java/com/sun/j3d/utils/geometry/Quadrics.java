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

package com.sun.j3d.utils.geometry;

import com.sun.j3d.utils.geometry.*;
import java.io.*;
import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.math.*;

class Quadrics extends Object {
    
    Quadrics(){ }
    
    // new disk code to remove transforms in the primitive code
    GeomBuffer disk(double r, int xdiv, double y, boolean outside, boolean texCoordYUp) {
        
        double theta, dtheta, sign, sinTheta, cosTheta;
        
        if (outside) sign = 1.0;
        else sign = -1.0;
        
        dtheta = 2.0*Math.PI / xdiv;
        
        GeomBuffer gbuf = new GeomBuffer(xdiv+2);
        
        gbuf.begin(GeomBuffer.TRIANGLE_FAN);
        gbuf.normal3d(0.0, 1.0*sign, 0.0);
        gbuf.texCoord2d(0.5, 0.5);
        gbuf.vertex3d(0.0, y, 0.0);
        
        // create the disk by evaluating points along the unit circle.
        // theta is the angle around the y-axis. Then we obtain
        // (cos(theta), sin(theta)) = (x,z) sample points.  The y value
        // was passed in as a parameter.
        // texture coordinates are obtain from the unit circle centered at
        // (.5, .5) in s, t space.  thus portions of the texture are not used.
        
        if (!outside) {
            for (int i = 0; i <= xdiv; i++) {
                theta = i * dtheta;
                // add 90 degrees to theta so lines up wtih the body
                sinTheta = Math.sin(theta - Math.PI/2.0);
                cosTheta = Math.cos(theta - Math.PI/2.0);
                gbuf.normal3d(0.0, 1.0*sign, 0.0);
                if (texCoordYUp) {
                    gbuf.texCoord2d(0.5+cosTheta*0.5, 1.0 - (0.5+sinTheta*0.5));                    
                }
                else {
                    gbuf.texCoord2d(0.5+cosTheta*0.5, 0.5+sinTheta*0.5);
                }
                gbuf.vertex3d(r*cosTheta, y, r*sinTheta);
            }
        } else {
            for (int i = xdiv; i >= 0; i--) {
                theta = i * dtheta;
                // add 90 degrees to theta so lines up with the body
                sinTheta = Math.sin(theta - Math.PI/2.0);
                cosTheta = Math.cos(theta - Math.PI/2.0);
                gbuf.normal3d(0.0, 1.0*sign, 0.0);
                if (texCoordYUp) {
                    gbuf.texCoord2d(0.5+cosTheta*0.5, 1.0 - (0.5-sinTheta*0.5));                    
                }
                else {
                    gbuf.texCoord2d(0.5+cosTheta*0.5, 0.5-sinTheta*0.5);
                }
                gbuf.vertex3d(cosTheta*r, y, sinTheta*r);
            }
        }
        
        gbuf.end();
        return gbuf;
    }
    
    
    // new cylinder to remove transforms in the cylinder code and to optimize
    // by using triangle strip
    GeomBuffer cylinder(double height, double radius,
            int xdiv, int ydiv, boolean outside, boolean texCoordYUp) {
        
        double sign;
        
        if (outside) sign = 1.0;
        else sign = -1.0;
        
        // compute the deltas
        double dtheta = 2.0*Math.PI / (double)xdiv;
        double dy = height / (double)ydiv;
        double du = 1.0/(double)xdiv;
        double dv = 1.0/(double)ydiv;
        
        GeomBuffer gbuf = new GeomBuffer(ydiv*2*(xdiv+1));
        
        double s = 0.0, t = 0.0;
        double px, pz, qx, qz;
        double py = -height/2.0;
        double qy;
        
        gbuf.begin(GeomBuffer.QUAD_STRIP);
        
        for (int i = 0; i < ydiv; i++) {
            qy = py+dy;
            if (outside) {
                px = Math.cos(xdiv*dtheta - Math.PI/2.0);
                pz = Math.sin(xdiv*dtheta - Math.PI/2.0);
                qx = Math.cos((xdiv-1)*dtheta - Math.PI/2.0);
                qz = Math.sin((xdiv-1)*dtheta - Math.PI/2.0);
                
                // vert 2
                gbuf.normal3d(px*sign, 0.0, pz*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(s, 1.0 - (t + dv));                    
                }
                else {
                    gbuf.texCoord2d(s, t+dv);
                }
                gbuf.vertex3d(px*radius, qy, pz*radius);
                
                // vert 1
                gbuf.normal3d(px*sign, 0.0, pz*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(s, 1.0 - t);
                } else {
                    gbuf.texCoord2d(s, t);
                }
                gbuf.vertex3d(px*radius, py, pz*radius);
                
                // vert 4
                gbuf.normal3d(qx*sign, 0.0, qz*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(s+du, 1.0 - (t + dv));                    
                }
                else {
                    gbuf.texCoord2d(s+du, t+dv);
                }
                gbuf.vertex3d(qx*radius, qy, qz*radius);
                
                // vert 3
                gbuf.normal3d(qx*sign, 0.0, qz*sign);
                if (texCoordYUp) {                
                    gbuf.texCoord2d(s+du, 1.0 - t);
                }
                else {
                    gbuf.texCoord2d(s+du, t);                    
                }
                gbuf.vertex3d(qx*radius, py, qz*radius);
                
                s += (du*2.0);
                
                for (int j = xdiv-2; j >=0; j--) {
                    px = Math.cos(j*dtheta - Math.PI/2.0);
                    pz = Math.sin(j*dtheta - Math.PI/2.0);
                    
                    // vert 6
                    gbuf.normal3d(px*sign, 0.0, pz*sign);
                    if (texCoordYUp) {
                        gbuf.texCoord2d(s, 1.0 - (t + dv));                        
                    }
                    else {
                        gbuf.texCoord2d(s, t+dv);
                    }
                    gbuf.vertex3d(px*radius, qy, pz*radius);
                    
                    // vert 5
                    gbuf.normal3d(px*sign, 0.0, pz*sign);
                    if (texCoordYUp) {
                        gbuf.texCoord2d(s, 1.0 - t);                        
                    }
                    else {
                        gbuf.texCoord2d(s, t);
                    }
                    gbuf.vertex3d(px*radius, py, pz*radius);
                    
                    s += du;
                }
                
            } else {
// 		c = 0;
                px = Math.cos(-Math.PI/2.0);
                pz = Math.sin(-Math.PI/2.0);
                qx = Math.cos(dtheta - Math.PI/2.0);
                qz = Math.sin(dtheta - Math.PI/2.0);
                
                gbuf.normal3d(px*sign, 0.0, pz*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(s, 1.0 - (t + dv));                    
                }
                else {
                    gbuf.texCoord2d(s, t+dv);
                }
                gbuf.vertex3d(px*radius, qy, pz*radius);
                
                // vert 1
                gbuf.normal3d(px*sign, 0.0, pz*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(s, 1.0 - t);
                }
                else {
                    gbuf.texCoord2d(s, t);
                }
                gbuf.vertex3d(px*radius, py, pz*radius);
                
                gbuf.normal3d(qx*sign, 0.0, qz*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(s+du, 1.0 - (t + dv));                    
                }
                else {
                    gbuf.texCoord2d(s+du, t+dv);
                }
                gbuf.vertex3d(qx*radius, qy, qz*radius);
                
                gbuf.normal3d(qx*sign, 0.0, qz*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(s+du, 1.0 - t);                    
                }
                else {
                    gbuf.texCoord2d(s+du, t);
                }
                gbuf.vertex3d(qx*radius, py, qz*radius);
                
                s += (du*2.0);
                
                for (int j = 2; j <= xdiv; j++) {
                    px = Math.cos(j*dtheta - Math.PI/2.0);
                    pz = Math.sin(j*dtheta - Math.PI/2.0);
                    
                    gbuf.normal3d(px*sign, 0.0, pz*sign);
                    if (texCoordYUp) {
                        gbuf.texCoord2d(s, 1.0 - (t + dv));                        
                    }
                    else {
                        gbuf.texCoord2d(s, t+dv);
                    }
                    gbuf.vertex3d(px*radius, qy, pz*radius);
                    
                    gbuf.normal3d(px*sign, 0.0, pz*sign);
                    if (texCoordYUp) {
                        gbuf.texCoord2d(s, 1.0 - t);                        
                    }
                    else {
                        gbuf.texCoord2d(s, t);
                    }
                    gbuf.vertex3d(px*radius, py, pz*radius);
                    
                    s += du;
                }
                
            }
            s = 0.0;
            t += dv;
            py += dy;
        }
        
        gbuf.end();
        
        return gbuf;
    }
    
    // new coneBody method to remove transform in the Cone primitive
    // and to optimize by using triangle strip
    GeomBuffer coneBody(double bottom, double top, double bottomR, double topR,
            int xdiv, int ydiv, double dv, boolean outside, boolean texCoordYUp) {
        
        double r, sign;
        
        if (outside) sign = 1.0;
        else sign = -1.0;
        
        // compute the deltas
        double dtheta = 2.0*Math.PI/(double)xdiv;
        double dr = (topR-bottomR)/(double)ydiv;
        double height = top-bottom;
        double dy = height/(double)ydiv;
        double ynormal = (bottomR-topR)/height;
        double du = 1.0/(double)xdiv;
// 	double dv = 1.0/(double)(ydiv+1);
        
        GeomBuffer gbuf = new GeomBuffer(ydiv*2*(xdiv+1));
        
        double s = 0.0, t = 0.0;
        double px, pz, qx, qz;
        double py = bottom;
        double qy;
        r = bottomR;
        
        gbuf.begin(GeomBuffer.QUAD_STRIP);
        
        for (int i = 0; i < ydiv; i++) {
            qy = py+dy;
            if (outside) {
                px = Math.cos(xdiv*dtheta - Math.PI/2.0);
                pz = Math.sin(xdiv*dtheta - Math.PI/2.0);
                qx = Math.cos((xdiv-1)*dtheta - Math.PI/2.0);
                qz = Math.sin((xdiv-1)*dtheta - Math.PI/2.0);
                
                // vert2
                gbuf.normal3d(px*sign, ynormal*sign, pz*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(s, 1.0 - (t + dv));                    
                }
                else {
                    gbuf.texCoord2d(s, t+dv);
                }
                gbuf.vertex3d(px*(r+dr), qy, pz*(r+dr));
                
                // vert1
                gbuf.normal3d(px*sign, ynormal*sign, pz*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(s, 1.0 - t);                
                }
                else {
                    gbuf.texCoord2d(s, t);
                }
                gbuf.vertex3d(px*r, py, pz*r);
                
                // vert4
                gbuf.normal3d(qx*sign, ynormal*sign, qz*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(s+du, 1.0 - (t + dv));                    
                }
                else {
                    gbuf.texCoord2d(s+du, t+dv);
                }
                gbuf.vertex3d(qx*(r+dr), qy, qz*(r+dr));
                
                // vert3
                gbuf.normal3d(qx*sign, ynormal*sign, qz*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(s+du, 1.0 - t);
                }
                else {
                    gbuf.texCoord2d(s+du, t);
                }
                gbuf.vertex3d(qx*r, py, qz*r);
                
                s += (du*2.0);
                
                for (int j = xdiv-2; j >= 0; j--) {
                    px = Math.cos(j*dtheta - Math.PI/2.0);
                    pz = Math.sin(j*dtheta - Math.PI/2.0);
                    
                    // vert 6
                    gbuf.normal3d(px*sign, ynormal*sign, pz*sign);
                    if (texCoordYUp) {
                        gbuf.texCoord2d(s, 1.0 - (t + dv));
                    } else {
                        gbuf.texCoord2d(s, t+dv);
                    }
                    gbuf.vertex3d(px*(r+dr), qy, pz*(r+dr));
                    
                    // vert 5
                    gbuf.normal3d(px*sign, ynormal*sign, pz*sign);
                    if (texCoordYUp) {
                        gbuf.texCoord2d(s, 1.0 - t);                        
                    }
                    else {
                        gbuf.texCoord2d(s, t);
                    }
                    gbuf.vertex3d(px*r, py, pz*r);
                    
                    s += du;
                }
            } else {
                px = Math.cos(-Math.PI/2.0);
                pz = Math.sin(-Math.PI/2.0);
                qx = Math.cos(dtheta - Math.PI/2.0);
                qz = Math.sin(dtheta - Math.PI/2.0);
                
                // vert1
                gbuf.normal3d(px*sign, ynormal*sign, pz*sign);
                if (texCoordYUp) {
                   gbuf.texCoord2d(s, 1.0 - (t + dv));                    
                }
                else {
                    gbuf.texCoord2d(s, t+dv);
                }
                gbuf.vertex3d(px*(r+dr), qy, pz*(r+dr));
                
                gbuf.normal3d(px*sign, ynormal*sign, pz*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(s, 1.0 - t);                    
                }
                else {
                    gbuf.texCoord2d(s, t);
                }
                gbuf.vertex3d(px*r, py, pz*r);
                
                gbuf.normal3d(qx*sign, ynormal*sign, qz*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(s+du, 1.0 - (t + dv));
                }
                else {
                    gbuf.texCoord2d(s+du, t+dv);
                }
                gbuf.vertex3d(qx*(r+dr), qy, qz*(r+dr));
                
                gbuf.normal3d(qx*sign, ynormal*sign, qz*sign);
                if (texCoordYUp) {                
                    gbuf.texCoord2d(s+du, 1.0 - t);
                }
                else {
                    gbuf.texCoord2d(s+du, t);                    
                }
                gbuf.vertex3d(qx*r, py, qz*r);
                
                s += (du*2.0);
                
                for (int j = 2; j <= xdiv; j++) {
                    px = Math.cos(j*dtheta - Math.PI/2.0);
                    pz = Math.sin(j*dtheta - Math.PI/2.0);
                    
                    gbuf.normal3d(px*sign, ynormal*sign, pz*sign);
                    if (texCoordYUp) {
                        gbuf.texCoord2d(s, 1.0 - (t + dv));                        
                    }
                    else {
                        gbuf.texCoord2d(s, t+dv);
                    }
                    gbuf.vertex3d(px*(r+dr), qy, pz*(r+dr));
                    
                    gbuf.normal3d(px*sign, ynormal*sign, pz*sign);
                    if (texCoordYUp) {
                        gbuf.texCoord2d(s, 1.0 - t);
                    }
                    else {
                        gbuf.texCoord2d(s, t);
                    }
                    gbuf.vertex3d(px*r, py, pz*r);
                    
                    s += du;
                }
            }
            s = 0.0;
            t += dv;
            py += dy;
            r += dr;
        }
        gbuf.end();
        
        return gbuf;
    }
    
    // new coneTop method to remove transforms in the cone code
    GeomBuffer coneTop(double bottom, double radius, double height,
            int xdiv,double t, boolean outside, boolean texCoordYUp) {
        
        double sign;
        
        if (outside) sign = 1.0;
        else sign = -1.0;
        
        // compute the deltas
        double dtheta = 2.0*Math.PI/(double)xdiv;
        double ynormal = radius/height;
        double du = 1.0/(double)xdiv;
        double top = bottom + height;
        
        // initialize the geometry buffer
        GeomBuffer gbuf = new GeomBuffer(xdiv + 2);
        gbuf.begin(GeomBuffer.TRIANGLE_FAN);
        
        // add the tip, which is the center of the fan
        gbuf.normal3d(0.0, ynormal*sign, 0.0);
        if (texCoordYUp) {
            gbuf.texCoord2d(.5, 0.0);            
        }
        else {
            gbuf.texCoord2d(.5, 1.0);
        }
        gbuf.vertex3d(0.0, top, 0.0);
        
        // go around the circle and add the rest of the fan
        double s = 0.0;
        double px, pz;
        if (outside) {
            for (int i = xdiv; i >= 0; i--) {
                px = Math.cos(i*dtheta - Math.PI/2.0);
                pz = Math.sin(i*dtheta - Math.PI/2.0);
                gbuf.normal3d(px*sign, ynormal*sign, pz*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(s, 1.0 - t);                    
                } else {
                    gbuf.texCoord2d(s, t);
                }
                gbuf.vertex3d(px*radius, bottom, pz*radius);
                
                s += du;
            }
        } else {
            for (int i = 0; i <= xdiv; i++) {
                px = Math.cos(i*dtheta - Math.PI/2.0);
                pz = Math.sin(i*dtheta - Math.PI/2.0);
                gbuf.normal3d(px*sign, ynormal*sign, pz*sign);
                if (texCoordYUp) {
                    gbuf.texCoord2d(s, 1.0 - t);
                } else {
                    gbuf.texCoord2d(s, t);
                }
                gbuf.vertex3d(px*radius, bottom, pz*radius);
                s += du;
            }
        }
        gbuf.end();
        return gbuf;
    }
}

