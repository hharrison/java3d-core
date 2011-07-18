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
 * DirectionalSample object
 *
 * IMPLEMENTATION NOTE: The JavaSoundMixer is incomplete and really needs
 * to be rewritten.
 */

package com.sun.j3d.audioengines.javasound;

import javax.media.j3d.*;
import com.sun.j3d.audioengines.*;
import javax.vecmath.*;

/**
 * The PostionalSample Class defines the data and methods associated with a 
 * PointSound sample played through the AudioDevice.
 */

class JSDirectionalSample extends JSPositionalSample
{
    // The transformed direction of this sound
    Vector3f xformDirection = new Vector3f(0.0f, 0.0f, 1.0f);

    public JSDirectionalSample() {
        super();
        if (debugFlag) 
            debugPrintln("JSDirectionalSample constructor");
    }

    void setXformedDirection() {
        if (debugFlag)
            debugPrint("*** setXformedDirection");
        if (!getVWrldXfrmFlag()) {
            if (debugFlag)
                debugPrint("    Transform NOT set yet, so dir => xformDir");
            xformDirection.set(direction);
        }
        else {
            if (debugFlag)
                debugPrint("    Transform dir => xformDir");
            vworldXfrm.transform(direction, xformDirection);
        }
        if (debugFlag)
            debugPrint("           xform(sound)Direction <= "+xformDirection.x+
                       ", " + xformDirection.y + ", " + xformDirection.z);
    }


    /* ***********************************
     *   
     *  Intersect ray to head with Ellipse
     *   
     * ***********************************/
    /*
     * An ellipse is defined using:
     *    (1) the ConeSound's direction vector as the major axis of the ellipse;
     *    (2) the max parameter (a front distance attenuation value) along the
     *        cone's position axis; and 
     *    (3) the min parameter (a back distance attenuation value) along the
     *        cone's negative axis
     * This method calculates the distance from the sound source to the 
     * Intersection of the Ellipse with the ray from the sound source to the
     * listener's head.
     * This method returns the resulting distance. 
     * If an error occurs, -1.0 is returned.
     *
     * A calculation are done in 'Cone' space:
     *    The origin is defined as being the sound source position.
     *    The ConeSound source axis is the X-axis of this Cone's space.
     *    Since this ConeSound source defines a prolate spheroid (obtained
     * by revolving an ellipsoid about the major axis) we can define the
     * Y-axis of this Cone space as being in the same plane as the X-axis
     * and the vector from the origin to the head.
     *    All calculations in Cone space can be generalized in this two-
     * dimensional space without loss of precision.
     *    Location of the head, H, in Cone space can then be defined as:
     *         H'(x,y) = (cos @, sin @) * | H |
     * where @ is the angle between the X-axis and the ray to H.
     * Using the equation of the line thru the origin and H', and the
     * equation of ellipse defined with min and max, find the 
     * intersection by solving for x and then y.
     *
     *    (I) The equation of the line thru the origin and H', and the
     *                    | H'(y) - S(y) |
     *         y - S(y) = | -----------  | * [x - S(x)]
     *                    | H'(x) - S(x) |
     * and since S(x,y) is the origin of ConeSpace:
     *                    | H'(y) |
     *               y  = | ----- | x
     *                    | H'(x) |
     *
     *    (II) The equation of ellipse:
     *         x**2   y**2
     *         ---- + ---- = 1
     *         a**2   b**2
     * given a is length from origin to ellipse along major, X-axis, and
     * b is length from origin to ellipse along minor, Y-axis;
     * where a**2 = [(max+min)/2]**2 , since 2a = min+max;
     * where b**2 = min*max , since the triangle abc is made is defined by the
     * the points: S(x,y), origin, and (0,b),
     * thus b**2 = a**2 - S(x,y) = a**2 - ((a-min)**2) = 2a*min - min**2
     *      b**2 = ((min+max)*min) - min**2 = min*max.
     * so the equation of the ellipse becomes:
     *            x**2          y**2
     *      ---------------- + ------- = 1
     *      [(max+min)/2]**2   min*max
     *
     * Substuting for y from Eq.(I) into Eq.(II) gives
     *            x**2         [(H'(y)/H'(x))*x]**2
     *      ---------------- + -------------------- = 1
     *      [(max+min)/2]**2          min*max
     *
     * issolating x**2 gives
     *           |         1          [H'(y)/H'(x)]**2 |
     *      x**2 | ---------------- + ---------------- | = 1
     *           | [(max+min)/2]**2       min*max      |
     *
     *
     *           |       4          [(sin @ * |H|)/(cos @ * |H|)]**2 |
     *      x**2 | -------------- + -------------------------------- | = 1
     *           | [(max+min)]**2               min*max              |
     *
     *              |                                         | 
     *              |                   1                     |
     *              |                                         | 
     *      x**2 =  | --------------------------------------- |
     *              |  |       4          [sin @/cos @]**2 |  |
     *              |  | -------------- + ---------------- |  |
     *              |  | [(max+min)]**2       min*max      |  |
     *
     * substitute tan @ for [sin @/cos @], and take the square root and you have
     * the equation for x as calculated below.
     *
     * Then solve for y by plugging x into Eq.(I).
     *
     * Return the distance from the origin in Cone space to this intersection 
     * point: square_root(x**2 + y**2).
     *
     */
    double intersectEllipse(double max, double min ) {

         if (debugFlag)
             debugPrint("        intersectEllipse entered with min/max = " + min + "/" + max);
        /*
         * First find angle '@' between the X-axis ('A') and the ray to Head ('H').
         * In local coordinates, use Dot Product of those two vectors to get cos @:
         *               A(u)*H(u) + A(v)*H(v) + A(w)*H(v)
         *       cos @ = --------------------------------
         *                      |A|*|H|
         * then since domain of @ is { 0 <= @ <= PI }, arccos can be used to get @.
         */
         Vector3f xAxis = this.direction;  // axis is sound direction vector
         // Get the already calculated vector from sound source position to head
         Vector3f sourceToHead = this.sourceToCenterEar;
         // error check vectors not empty
         if (xAxis == null || sourceToHead == null) {
             if (debugFlag)
                 debugPrint( "           one or both of the vectors are null" );
             return (-1.0f);  // denotes an error occurred
         }

         // Dot Product
         double dotProduct = (double)( (sourceToHead.dot(xAxis)) /
                    (sourceToHead.length() * xAxis.length()));
         if (debugFlag)
             debugPrint( "           dot product = " + dotProduct );
         // since theta angle is in the range between 0 and PI, arccos can be used
         double theta = (float)(Math.acos(dotProduct));
         if (debugFlag)
             debugPrint( "           theta = " + theta );

         /*
          * Solve for X using Eq.s (I) and (II) from above.
          */
         double minPlusMax = (double)(min + max);
         double tangent = Math.tan(theta);
         double xSquared = 1.0 /
                           ( ( 4.0 / (minPlusMax * minPlusMax) ) +
                             ( (tangent * tangent) / (min * max) ) );
         double x = Math.sqrt(xSquared);
         if (debugFlag)
             debugPrint( "           X = " + x );
         /*
          * Solve for y, given the result for x:
          *          | H'(y) |       | sin @ | 
          *     y  = | ----- | x  =  | ----- | x
          *          | H'(x) |       | cos @ | 
          */
         double y = tangent * x;
         if (debugFlag)
             debugPrint( "           Y = " + y );
         double ySquared = y * y;

         /*
          * Now return distance from origin to intersection point (x,y)
          */
         float distance = (float)(Math.sqrt(xSquared + ySquared));
         if (debugFlag)
             debugPrint( "           distance to intersection = " + distance );
         return (distance);
    }

    /* *****************
     *   
     *  Find Factor
     *   
     * *****************/
    /*
     *  Interpolates the correct attenuation scale factor given a 'distance'
     *  value.  This version used both front and back attenuation distance 
     *  and scale factor arrays (if non-null) in its calculation of the
     *  the distance attenuation.
     *  If the back attenuation arrays are null then this executes the
     *  PointSoundRetained version of this method.
     *  This method finds the intesection of the ray from the sound source
     *  to the center-ear, with the ellipses defined by the two sets (front
     *  and back) of distance attenuation arrays.
     *  This method looks at pairs of intersection distance values to find 
     *  which pair the input distance argument is between:
     *     [intersectionDistance[index] and intersectionDistance[index+1]
     *  The index is used to get factorArray[index] and factorArray[index+1].
     *  Then the ratio of the 'distance' between this pair of intersection
     *  values is used to scale the two found factorArray values proportionally.
     */  
    float findFactor(double distanceToHead,
                    double[] maxDistanceArray, float[] maxFactorArray,
                    double[] minDistanceArray, float[] minFactorArray) {
        int     index, lowIndex, highIndex, indexMid;
	double	returnValue;

        if (debugFlag) {
            debugPrint("JSDirectionalSample.findFactor entered:");
            debugPrint("      distance to head = " + distanceToHead);
        }

        if (minDistanceArray == null || minFactorArray == null) {
            /*
             * Execute the PointSoundRetained version of this method.
             * Assume it will check for other error conditions.
             */
            return ( this.findFactor(distanceToHead,
                         maxDistanceArray, maxFactorArray) );
        }

        /*
         * Error checking
         */
        if (maxDistanceArray == null || maxFactorArray == null) {
            if (debugFlag)
                debugPrint("    findFactor: arrays null");
            return -1.0f;
        }
        // Assuming length > 1 already tested in set attenuation arrays methods
        int arrayLength = maxDistanceArray.length;
        if (arrayLength < 2) {
            if (debugFlag)
                debugPrint("    findFactor: arrays length < 2");
            return -1.0f;
        }
        int largestIndex = arrayLength - 1;
        /*
         * Calculate distanceGain scale factor
         */
        /*
         * distanceToHead is larger than greatest distance in maxDistanceArray
         * so head is beyond the outer-most ellipse.
         */
        if (distanceToHead >= maxDistanceArray[largestIndex]) {
            if (debugFlag)
                debugPrint("    findFactor: distance > " + 
                                  maxDistanceArray[largestIndex]);
            if (debugFlag)
                debugPrint("    maxDistanceArray length = " + 
                                  maxDistanceArray.length);
            if (debugFlag)   
                debugPrint("    findFactor returns ****** " +
                               maxFactorArray[largestIndex] + " ******");
            return maxFactorArray[largestIndex];
        }

        /*
         * distanceToHead is smaller than least distance in minDistanceArray
         * so head is inside the inner-most ellipse.
         */
        if (distanceToHead <= minDistanceArray[0]) {
            if (debugFlag)
                debugPrint("    findFactor: distance < " +
                                    maxDistanceArray[0]);
            if (debugFlag)   
                debugPrint("    findFactor returns ****** " +
                               minFactorArray[0] + " ******");
            return minFactorArray[0];
        }
 
        /*
         * distanceToHead is between points within attenuation arrays.
         * Use binary halfing of distance attenuation arrays.
         */
        {
            double[] distanceArray = new double[arrayLength];
            float[] factorArray = new float[arrayLength];
            boolean[] intersectionCalculated = new boolean[arrayLength];
            // initialize intersection calculated array flags to false
            for (int i=0; i<arrayLength; i++)
                intersectionCalculated[i] = false;
            boolean intersectionOnEllipse = false;
            int factorIndex = -1;

            /* 
             * Using binary halving to find the two index values in the
             * front and back distance arrays that the distanceToHead 
             * parameter (from sound source position to head) fails between.
             * Changing the the current low and high index values 
             * calculate the intesection of ellipses (defined by this
             * min/max distance values) with the ray (sound source to
             * head).  Put the resulting value into the distanceArray.
             */
             /*
              * initialize the lowIndex to first index of distance arrays.
              * initialize the highIndex to last index of distance arrays.
              */
            lowIndex = 0;
            highIndex = largestIndex;

            if (debugFlag)
                debugPrint("    while loop to find index that's closest: ");
            while (lowIndex < (highIndex-1)) {
                if (debugFlag)
                    debugPrint("        lowIndex " + lowIndex +
                       ", highIndex " + highIndex);
                /*
                 * Calculate the Intersection of Ellipses (defined by this
                 * min/max values) with the ray from the sound source to the
                 * head.  Put the resulting value into the distanceArray.
                 */
                if (!intersectionCalculated[lowIndex]) {
                    distanceArray[lowIndex] = this.intersectEllipse(
                        maxDistanceArray[lowIndex], minDistanceArray[lowIndex]);
                    // If return intersection distance is < 0 an error occurred.
                    if (distanceArray[lowIndex] >= 0.0)
                        intersectionCalculated[lowIndex] = true;
                    else {
                        /*
                         * Error in ellipse intersection calculation.  Use
                         * average of max/min difference for intersection value.
                         */
                        distanceArray[lowIndex] = (minDistanceArray[lowIndex] + 
                                   maxDistanceArray[lowIndex])*0.5;
                        if (internalErrors)
                            debugPrint(
                               "Internal Error in intersectEllipse; use " +
                               distanceArray[lowIndex] + 
                               " for intersection value " );
                        // Rather than aborting, just use average and go on...
                        intersectionCalculated[lowIndex] = true;
                    }
                } // end of if intersection w/ lowIndex not already calculated

                if (!intersectionCalculated[highIndex]) {
                    distanceArray[highIndex] = this.intersectEllipse(
                        maxDistanceArray[highIndex],minDistanceArray[highIndex]);
                    // If return intersection distance is < 0 an error occurred.
                    if (distanceArray[highIndex] >= 0.0f)
                        intersectionCalculated[highIndex] = true;
                    else {
                        /*
                         * Error in ellipse intersection calculation.  Use
                         * average of max/min difference for intersection value.
                         */
                        distanceArray[highIndex] = (minDistanceArray[highIndex]+
                                  maxDistanceArray[highIndex])*0.5f;
                        if (internalErrors)
                            debugPrint(
                               "Internal Error in intersectEllipse; use " +
                               distanceArray[highIndex] +  
                               " for intersection value " );
                        // Rather than aborting, just use average and go on...
                        intersectionCalculated[highIndex] = true;
                    }
                } // end of if intersection w/ highIndex not already calculated

                /*
                 * Test for intersection points being the same as head position
                 * distanceArray[lowIndex] and distanceArray[highIndex], if so
                 * return factor value directly from array
                 */
                if (distanceArray[lowIndex] >= distanceToHead) {
                    if ((lowIndex != 0) && 
                            (distanceToHead < distanceArray[lowIndex])) {
                        if (internalErrors)
                            debugPrint( 
                                "Internal Error: binary halving in " +
                                "findFactor failed; distance < low " +
                                "index value");
                    }
                    if (debugFlag) {
                        debugPrint("        distanceArray[lowIndex] >= " +
                           "distanceToHead" );
                        debugPrint( "        factorIndex = " + lowIndex);
                    }
                    intersectionOnEllipse = true;
                    factorIndex = lowIndex;
                    break;
                }
                else if (distanceArray[highIndex] <= distanceToHead) {
                    if ((highIndex != largestIndex) && 
                             (distanceToHead > distanceArray[highIndex])) {
                        if (internalErrors)
                            debugPrint(
                                "Internal Error: binary halving in " +
                                "findFactor failed; distance > high " +
                                "index value");
                    }
                    if (debugFlag) {
                        debugPrint("        distanceArray[highIndex] >= " +
                           "distanceToHead" );
                        debugPrint( "        factorIndex = " + highIndex);
                    }
                    intersectionOnEllipse = true;
                    factorIndex = highIndex;
                    break;
                }

                if (distanceToHead > distanceArray[lowIndex] &&
                    distanceToHead < distanceArray[highIndex] ) {
                    indexMid = lowIndex + ((highIndex - lowIndex) / 2);
                    if (distanceToHead <= distanceArray[indexMid])
                        // value of distance in lower "half" of list
                        highIndex = indexMid;
                    else // value if distance in upper "half" of list
                        lowIndex = indexMid;
                }
            } /* of while */

            /*
             * First check to see if distanceToHead is beyond min or max
             * ellipses, or on an ellipse.
             * If so, factor is calculated using the distance Ratio 
             *    (distanceToHead - min) / (max-min)
             * where max = maxDistanceArray[factorIndex], and 
             *       min = minDistanceArray[factorIndex]
             */
            if (intersectionOnEllipse  && factorIndex >= 0) {
                if (debugFlag) { 
                    debugPrint( "    ratio calculated using factorIndex " +
                        factorIndex);
                    debugPrint( "    d.A. max pair for factorIndex " +
                        maxDistanceArray[factorIndex] + ", " + 
                        maxFactorArray[factorIndex]);
                    debugPrint( "    d.A. min pair for lowIndex " +
                        minDistanceArray[factorIndex] + ", " + 
                        minFactorArray[factorIndex]);
                }
                returnValue =  (
                    ( (distanceArray[factorIndex] - 
                              minDistanceArray[factorIndex]) /
                          (maxDistanceArray[factorIndex] - 
                              minDistanceArray[factorIndex]) ) *
                      (maxFactorArray[factorIndex] - 
                              minFactorArray[factorIndex]) ) +
                    minFactorArray[factorIndex] ;
                if (debugFlag)   
                    debugPrint("    findFactor returns ****** " +
                               returnValue + " ******");
                return (float)returnValue;
            }

            /* Otherwise, for distanceToHead between distance intersection
             * values, we need to calculate two factors - one for the
             * ellipse defined by lowIndex min/max factor arrays, and
             * the other by highIndex min/max factor arrays.  Then the
             * distance Ratio (defined above) is applied, using these
             * two factor values, to get the final return value.
             */ 
	    double highFactorValue = 1.0;
            double lowFactorValue  = 0.0;
            highFactorValue = 
                ( ((distanceArray[highIndex] - minDistanceArray[highIndex]) /
                   (maxDistanceArray[highIndex]-minDistanceArray[highIndex])) *
                  (maxFactorArray[highIndex] - minFactorArray[highIndex]) ) +
                minFactorArray[highIndex] ;
            if (debugFlag) { 
                debugPrint( "    highFactorValue calculated w/ highIndex " +
                        highIndex);
                debugPrint( "    d.A. max pair for highIndex " +
                        maxDistanceArray[highIndex] + ", " + 
                        maxFactorArray[highIndex]);
                debugPrint( "    d.A. min pair for lowIndex " +
                        minDistanceArray[highIndex] + ", " + 
                        minFactorArray[highIndex]);
                debugPrint( "    highFactorValue " + highFactorValue);
            }
            lowFactorValue =
                ( ((distanceArray[lowIndex] - minDistanceArray[lowIndex]) /
                   (maxDistanceArray[lowIndex] - minDistanceArray[lowIndex])) *
                  (maxFactorArray[lowIndex] - minFactorArray[lowIndex]) ) +
                minFactorArray[lowIndex] ;
            if (debugFlag) { 
                debugPrint( "    lowFactorValue calculated w/ lowIndex " +
                        lowIndex);
                debugPrint( "    d.A. max pair for lowIndex " +
                        maxDistanceArray[lowIndex] + ", " + 
                        maxFactorArray[lowIndex]);
                debugPrint( "    d.A. min pair for lowIndex " +
                        minDistanceArray[lowIndex] + ", " + 
                        minFactorArray[lowIndex]);
                debugPrint( "    lowFactorValue " + lowFactorValue);
            }
            /*
             * calculate gain scale factor based on the ratio distance
             * between ellipses the distanceToHead lies between.
             */
            /*
             * ratio: distance from listener to sound source
             *        between lowIndex and highIndex times
             *        attenuation value between lowIndex and highIndex
             * gives linearly interpolationed attenuation value
             */
            if (debugFlag) { 
                debugPrint( "    ratio calculated using distanceArray" +
                       lowIndex + ", highIndex " + highIndex);
                debugPrint( "    calculated pair for lowIndex " +
                        distanceArray[lowIndex]+", "+ lowFactorValue);
                debugPrint( "    calculated pair for highIndex " +
                        distanceArray[highIndex]+", "+ highFactorValue );
            }
 
            returnValue =
                ( ( (distanceToHead - distanceArray[lowIndex]) /
                    (distanceArray[highIndex] - distanceArray[lowIndex]) ) *
                  (highFactorValue - lowFactorValue) ) +
                factorArray[lowIndex] ;
            if (debugFlag)   
                debugPrint("    findFactor returns ******" + 
                           returnValue + " ******");
            return (float)returnValue;
        } 

    }

    /**
     * CalculateDistanceAttenuation  
     * 
     * Simply calls ConeSound specific 'findFactor()' with 
     * both front and back attenuation linear distance and gain scale factor
     * arrays. 
     */  
    float calculateDistanceAttenuation(float distance) {
        float factor = findFactor(distance, this.attenuationDistance,
                       this.attenuationGain, this.backAttenuationDistance,
                       this.backAttenuationGain);
        if (factor < 0.0f)
            return 1.0f;
        else
            return factor;
    }
    /**
     * CalculateAngularGain  
     *   
     * Simply calls generic (for PointSound) 'findFactor()' with 
     * a single set of angular attenuation distance and gain scalefactor arrays.
     */  
    float calculateAngularGain() {
        float angle = findAngularOffset();
        float factor = findFactor(angle, this.angularDistance, this.angularGain);
        if (factor < 0.0f)
            return 1.0f;
        else
            return factor;
    } 

     /* *****************
     *   
     *  Find Angular Offset
     *   
     * *****************/
    /*   
     *  Calculates the angle from the sound's direction axis and the ray from
     *  the sound origin to the listener'center ear.
     *  For Cone Sounds this value is the arc cosine of dot-product between
     *  the sound direction vector and the vector (sound position,centerEar)
     *  all in Virtual World coordinates space.
     *  Center ear position is in Virtual World coordinates.
     *  Assumes that calculation done in VWorld Space...
     *  Assumes that xformPosition is already calculated...
     */  
    float findAngularOffset() {
        Vector3f unitToEar = new Vector3f();
        Vector3f unitDirection = new Vector3f();
        Point3f  xformPosition = positions[currentIndex];
        Point3f  xformCenterEar = centerEars[currentIndex];
        float   dotProduct;
        float   angle;
        /* 
         * TODO: (Question) is assumption that xformed values available O.K.
         * TODO: (Performance) save this angular offset and only recalculate
         *          if centerEar or sound position have changed. 
         */
        unitToEar.x = xformCenterEar.x - xformPosition.x;
        unitToEar.y = xformCenterEar.y - xformPosition.y;
        unitToEar.z = xformCenterEar.z - xformPosition.z;
        unitToEar.normalize();
        unitDirection.normalize(this.direction);
        dotProduct = unitToEar.dot(unitDirection);
        angle = (float)(Math.acos((double)dotProduct));
        if (debugFlag)
            debugPrint("           angle from cone direction = " + angle);
        return(angle);
    }

     /************
     *   
     *  Calculate Filter
     *   
     * *****************/
    /*   
     *  Calculates the low-pass cutoff frequency filter value applied to the
     *  a sound based on both:
     *      Distance Filter (from Aural Attributes) based on distance
     *         between the sound and the listeners position
     *      Angular Filter (for Directional Sounds) based on the angle
     *         between a sound's projected direction and the
     *         vector between the sounds position and center ear.
     *  The lowest of these two filter is used.
     *  This filter value is stored into the sample's filterFreq field.
     */  
    void calculateFilter(float distance, AuralParameters attribs) {
        // setting filter cutoff freq to 44.1kHz which, in this
        // implementation, is the same as not performing filtering
        float   distanceFilter = 44100.0f;
        float   angularFilter  = 44100.0f;
        int arrayLength = attribs.getDistanceFilterLength();
        int filterType = attribs.getDistanceFilterType();

        boolean distanceFilterFound = false;
        boolean angularFilterFound = false;
        if ((filterType == AuralParameters.NO_FILTERING) && arrayLength > 0) {
            double[] distanceArray = new double[arrayLength];
            float[]  cutoffArray = new float[arrayLength];
            attribs.getDistanceFilter(distanceArray, cutoffArray);

            if (debugFlag) {
                debugPrint("distanceArray    cutoffArray");
                for (int i=0; i<arrayLength; i++)
                    debugPrint((float)distanceArray[i] + ", " + cutoffArray[i]);
            }

            // Calculate angle from direction axis towards listener
            float angle = findAngularOffset(); 
            distanceFilter = findFactor((double)angle,
                   angularDistance, angularFilterCutoff);
            if (distanceFilter < 0.0f)
                distanceFilterFound = false;
            else
                distanceFilterFound = true;
        }
        else {
            distanceFilterFound = false;
            distanceFilter = -1.0f;
        }
 
        if (debugFlag)
            debugPrint("    calculateFilter arrayLength = " + arrayLength);
 
        // Angular filter of directional sound sources.
        arrayLength = angularDistance.length;
        filterType = angularFilterType;
        if ((filterType != AuralParameters.NO_FILTERING) && arrayLength > 0) {
            angularFilter = findFactor((double)distance,
                   angularDistance, angularFilterCutoff);
            if (angularFilter < 0.0f)
                angularFilterFound = false;
            else
                angularFilterFound = true;
        }
        else  {
            angularFilterFound = false;
            angularFilter = -1.0f;
        } 

        filterFlag = distanceFilterFound || angularFilterFound;
        if (distanceFilter < 0.0f)
            filterFreq = angularFilter;
        else if (angularFilter < 0.0f)
            filterFreq = distanceFilter;
        else // both filter frequencies are > 0
            filterFreq = Math.min(distanceFilter, angularFilter);

        if (debugFlag)
            debugPrint("    calculateFilter flag,freq = " + filterFlag +
           "," + filterFreq );
    }

}
