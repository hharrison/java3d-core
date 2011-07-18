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

 /*
  *  Sound Distance Attenuation utilities
  *
  *  Various methods to create PointSound and ConeSound distance attenuation
  *  arrays.
  */

package com.sun.j3d.utils.audio;

import java.io.* ;
import javax.vecmath.* ;
import java.lang.String; 
import javax.media.j3d.*;
import com.sun.j3d.internal.J3dUtilsI18N;

public class DistanceAttenuation
{
  // private fields

  // public fields
  /**
   * Equation types
   */
  static final int DOUBLE_DISTANCE_HALF_GAIN = 1;

  // methods
  /**
   * Fill a Distance Attenuation array
   *
   * recommend that the distance attenuation Point2f array is defined to
   * be allocated to be 10 for DOUBLE_DISTANCE_HALF_GAIN - since 1/(2^10)
   * exceeds 1/1000 scale that is agreed to be affective zero gain
   * 
   * First method assumes that:
   *    type is half gain for every double of distance
   *    inner radius is 0.0 but region between 0th and 1st elements is constant
   *         since gains for these two elements are the same
   *    min gain approches zero.
   */
  public void fillDistanceAttenuation(
                         float unitDistance, float unitGain,
                         Point2f[] distanceAttenuation ) {
      if (distanceAttenuation == null)
          throw new SoundException(J3dUtilsI18N.getString("DistanceAttenuation0"));

      int length = distanceAttenuation.length;
      distanceAttenuation[0].x = 0.0f;
      distanceAttenuation[0].y = unitGain;
      float nextDistance = unitDistance;
      float nextGain     = unitGain;

      for (int i=1; i<length; i++) {
          distanceAttenuation[i].x = nextDistance;
          distanceAttenuation[i].y = nextGain;
          nextDistance *= 2.0f;
          nextGain *= 0.5f;
      } 
  }

  public void fillDistanceAttenuation(
                         float innerRadius, float maxConstantGain,
                         float unitDistance, float unitGain,
                         int curveType, Point2f[] distanceAttenuation ) {
      if (distanceAttenuation == null)
          throw new SoundException(J3dUtilsI18N.getString("DistanceAttenuation0"));

      int length = distanceAttenuation.length;
      distanceAttenuation[0].x = innerRadius;
      distanceAttenuation[0].y = maxConstantGain;
      // Danger if mzxConstanceGain is less than greater than unitGain
      // then your modeling attenuation that's physically improbable!
      float nextDistance = unitDistance;
      float nextGain     = unitGain;

      for (int i=1; i<length; i++) {
          distanceAttenuation[i].x = innerRadius + nextDistance;
          distanceAttenuation[i].y = nextGain;
          nextDistance *= 2.0f;
          nextGain *= 0.5f;
      } 
  }

  public void fillDistanceAttenuation(
                         float innerRadius, float maxConstantGain,
                         float unitDistance, float unitGain,
                         float outerRadius, float minConstantGain,
                         int curveType, Point2f[] distanceAttenuation ) {
  }
}

