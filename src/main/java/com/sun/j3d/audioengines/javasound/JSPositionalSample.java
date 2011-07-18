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
 * Java Sound PositionalSample object
 *
 * IMPLEMENTATION NOTE: The JavaSoundMixer is incomplete and really needs
 * to be rewritten.
 */

package com.sun.j3d.audioengines.javasound;

import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.audioengines.*;

/**
 * The PostionalSample Class defines the data and methods associated with a 
 * PointSound sample played thru the AudioDevice.
 */

class JSPositionalSample extends JSSample
{ 

    // maintain fields for stereo channel rendering
    float     leftGain    = 1.0f;  // scale factor
    float     rightGain   = 1.0f;  // scale factor
    int       leftDelay   = 0;  // left  InterauralTimeDifference in millisec
    int       rightDelay  = 0;  // right ITD in millisec
    // fields for reverb channel

    // debug flag for the verbose Doppler calculation methods
    static final
    protected boolean  dopplerFlag = true;

    /**
     * For positional and directional sounds, TWO Hae streams or clips
     * are allocated, one each for the left and right channels, played at
     * a different (delayed) time and with a different gain value.
     */
    int     secondIndex = NULL_SAMPLE; 
    /**
     * A third sample for control of reverb of the stream/clip is openned
     * and maintained for all directional/positional sounds.
     * For now, even if no aural attributes (using reverb) are active, 
     * a reverb channel is always started with the other two. A sound could 
     * be started without reverb and then reverb added later, but since there
     * is no way to offset properly into all sounds (considering non-cached
     * and nconsistent rate-changes during playing) this third sound is
     * always allocated and started.
     */
    int     reverbIndex = NULL_SAMPLE; 

    /**
     * Save ear positions transformed into VirtualWorld coords from Head coords
     * These default positions are used when the real values cannot queried
     */
    Point3f  xformLeftEar   = new Point3f(-0.09f, -0.03f, 0.095f);
    Point3f  xformRightEar  = new Point3f(0.09f, -0.03f, 0.095f);
    // Z axis in head space - looking into the screen
    Vector3f xformHeadZAxis = new Vector3f(0.0f, 0.0f, -1.0f); // Va

    /**
     * Save vectors from source source position to transformed ear parameters
     */
    Vector3f sourceToCenterEar = new Vector3f();   // Vh
    Vector3f sourceToRightEar = new Vector3f();    // Vf or Vc
    Vector3f sourceToLeftEar = new Vector3f();     // Vf or Vc

    boolean  averageDistances = false;
    long     deltaTime = 0;
    double   sourcePositionChange = -1.0;
    double   headPositionChange = -1.0;

    /*
     * Maintain the last locations of sound and head as well as time the
     * sound was last processed.
     * Process delta distance and time as part of Doppler calculations.
     */
    static    int    MAX_DISTANCES = 4; 
    int       numDistances = 0; 
// TODO: time is based on changes to position!!! only
// TODO: must shap shot when either Position OR ear changes!!!
// TODO: must grab all changes to VIEW parameters (could change ear)!!!
//          not just when pointer to View changes!!
    long[]    times = new long[MAX_DISTANCES]; 
    Point3f[] positions = new Point3f[MAX_DISTANCES];  // xformed sound source positions
    Point3f[] centerEars = new Point3f[MAX_DISTANCES];  // xformed center ear positions
    /*
     * a set of indices (first, last, and current) are maintained to point
     * into the above arrays
     */
    int firstIndex = 0;
    int lastIndex  = 0;
    int currentIndex  = 0;
 
    /*
     * Allow changes in Doppler rate only small incremental values otherwise
     * you hear skips in the pitch of a sound during playback.
     * When playback is faster, allow delta changes:
     *   (diff in Factor for octave (1.0))/(12 1/2-steps))*(1/4) of half-step 
     * When playback is slower, allow delta changes:
     *   (diff in Factor for octave (0.5))/(12 1/2-steps))*(1/4) of half-step 
     */
    double   lastRequestedDopplerRateRatio = -1.0f;
    double   lastActualDopplerRateRatio    = -1.0f;
    static   double maxRatio                 = 256.0f;   // 8 times higher/lower
    /*
     * denotes movement of sound away or towards listener
     */
    static   int    TOWARDS   = 1;
    static   int    NO_CHANGE = 0;
    static   int    AWAY      = -1;

    /*
     * Process request for Filtering fields
     */  
    boolean   filterFlag = false;
    float     filterFreq = -1.0f;

    /*
     * Construct a new audio device Sample object
     */  
    public JSPositionalSample() {
        super();
        if (debugFlag)
            debugPrint("JSPositionalSample constructor");
        // initiallize circular buffer for averaging distance values
        for (int i=0; i<MAX_DISTANCES; i++) {
            positions[i] = new Point3f();
            centerEars[i] = new Point3f(0.09f, -0.03f, 0.095f);
        }
        clear();
    }

    // TODO: get/set secondChannel to JSStream/Clip/MIDI
    // TODO: get/set reverbChannel to JSStream/Clip/MIDI
    /*
     * Process request for Filtering fields
     */  
    boolean  getFilterFlag() {
        return filterFlag;
    }
    float  getFilterFreq() {
        return filterFreq;
    }

    
    /**
     * Clears the fields associated with sample data for this sound, and 
     * frees any device specific data associated with this sample.
     */  
    public void clear() {
        if (debugFlag)
            debugPrint("JSPositionalSample.clear() enter");
        super.clear();
        leftGain = 1.0f;
        rightGain = 1.0f;
        leftDelay = 0;
        rightDelay = 0;
        xformLeftEar.set(-0.09f, -0.03f, 0.095f);
        xformRightEar.set(0.09f, -0.03f, 0.095f);
        // Z axis in head space - looking into the screen
        xformHeadZAxis.set(0.0f, 0.0f, -1.0f); // Va
        sourceToCenterEar.set(0.0f, 0.0f, 0.0f);   // Vh
        sourceToRightEar.set(0.0f, 0.0f, 0.0f);    // Vf or Vc
        sourceToLeftEar.set(0.0f, 0.0f, 0.0f);     // Vf or Vc
        reset();
        if (debugFlag)
            debugPrint("JSPositionalSample.clear() exit");
    }

    /**
     * Reset time and count based fields associated with sample data 
     * for this sound
     */  
    void reset() {
        if (debugFlag)
            debugPrint("JSPositionalSample.reset() enter");
        super.reset();
        averageDistances = false;  // denotes not previously processed
        deltaTime = 0;
        sourcePositionChange = -1.0;
        headPositionChange = -1.0;
        rateRatio = 1.0f;
        numDistances = 0; 
        averageDistances = false;
        if (debugFlag)
            debugPrint("JSPositionalSample.reset() exit");
    }
    // increments index counters and bumps index numbers if the end of
    // the circular buffer is reached
    void incrementIndices() {
        int maxIndex = MAX_DISTANCES - 1;
        if (numDistances < maxIndex) {
            averageDistances = false;
            currentIndex  = numDistances;
            lastIndex = currentIndex - 1;
            firstIndex = 0;
            numDistances++;
        }
        else if (numDistances == maxIndex) {
            // we filled the data buffers completely and are ready to
            // calculate averages
            averageDistances = true;
            currentIndex  = maxIndex;
            lastIndex = currentIndex - 1;
            firstIndex = 0;
            numDistances++;
        }
        else if (numDistances > maxIndex) {
            // increment each counter and loop around
            averageDistances = true;
            currentIndex++;
            lastIndex++;
            firstIndex++;
            currentIndex %= MAX_DISTANCES;
            lastIndex %= MAX_DISTANCES;
            firstIndex %= MAX_DISTANCES;
        } 
    }

    // Not only do we transform position but delta time is calculated and
    // old transformed position is saved
    // Average the last MAX_DISTANCES delta time and change in position using
    // an array for both and circlularly storing the time and distance values
    // into this array.
    // Current transformed position and time in stored into maxIndex of their
    // respective arrays.
    void setXformedPosition() {
        Point3f newPosition = new Point3f();
        if (debugFlag)
            debugPrint("*** setXformedPosition");
        // xform Position 
        if (getVWrldXfrmFlag()) {
            if (debugFlag)
                debugPrint("    Transform set so transform pos");
	    vworldXfrm.transform(position, newPosition);
        }
        else {
            if (debugFlag)
               debugPrint("    Transform NOT set so pos => xformPos");
	    newPosition.set(position);
        }
        // store position and increment indices ONLY if theres an actual change
        if (newPosition.x == positions[currentIndex].x &&
            newPosition.y == positions[currentIndex].y &&
            newPosition.z == positions[currentIndex].z ) {
            if (debugFlag)
                debugPrint("    No change in pos, so don't reset");
            return;
        }

        incrementIndices();
        // store new transformed position
        times[currentIndex] = System.currentTimeMillis(); 
	positions[currentIndex].set(newPosition);
        if (debugFlag)
            debugPrint("           xform(sound)Position -" +
                      " positions[" + currentIndex + "] = (" +
                        positions[currentIndex].x + ", " + 
                        positions[currentIndex].y + ", " +
                        positions[currentIndex].z + ")");
      
        // since this is a change to the sound position and not the
        // head save the last head position into the current element
        if (numDistances > 1)
            centerEars[currentIndex].set(centerEars[lastIndex]);

    } 

    /**
     * Set Doppler effect Rate 
     *
     * Calculate the rate of change in for the head and sound
     * between the two time stamps (last two times position or
     * VirtualWorld transform was updated).
     * First determine if the head and sound source are moving 
     * towards each other (distance between them is decreasing), 
     * moving away from each other (distance between them is
     * increasing), or no change (distance is the same, not moving
     * or moving the same speed/direction).
     * The following equation is used for determining the change in frequency -
     *     If there has been a change in the distance between the head and sound:
     *
     *         f' =  f * frequencyScaleFactor * velocityRatio
     *
     *     For no change in the distance bewteen head and sound, velocityRatio is 1:
     *
     *         f' =  f
     *
     *     For head and sound moving towards each other, velocityRatio (> 1.0) is:
     *
     *         |  speedOfSound*rollOff  +   velocityOfHead*velocityScaleFactor  |
     *         |  ------------------------------------------------------------- |
     *         |  speedOfSound*rollOff  -  velocityOfSource*velocityScaleFactor |
     * 
     *     For head and sound moving away from each other, velocityRatio (< 1.0) is:
     * 
     *         |  speedOfSound*rollOff  -   velocityOfHead*velocityScaleFactor  |
     *         |  ------------------------------------------------------------- |
     *         |  speedOfSound*rollOff  +  velocityOfSource*velocityScaleFactor |
     *
     * where frequencyScaleFactor, rollOff, velocityScaleFactor all come from
     * the active AuralAttributes parameters.
     * The following special cases must be test for AuralAttribute parameters:
     *   rolloff  
     *       Value MUST be > zero for any sound to be heard!
     *       If value is zero, all sounds affected by AuralAttribute region are silent.
     *   velocityScaleFactor 
     *      Value MUST be > zero for any sound to be heard!
     *      If value is zero, all sounds affected by AuralAttribute region are paused.
     *   frequencyScaleFactor 
     *      Value of zero disables Doppler calculations:
     *         Sfreq' = Sfreq * frequencyScaleFactor
     *
     * This rate is passed to device drive as a change to playback sample
     * rate, in this case the frequency need not be known.
     *
     * Return value of zero denotes no change
     * Return value of -1 denotes ERROR
     */
    float calculateDoppler(AuralParameters attribs) {
        double sampleRateRatio = 1.0;
        double headVelocity = 0.0;              // in milliseconds
        double soundVelocity = 0.0;             // in milliseconds
        double distanceSourceToHead = 0.0;      // in meters
        double lastDistanceSourceToHead = 0.0;  // in meters
        float  speedOfSound = attribs.SPEED_OF_SOUND;
        double numerator = 1.0;
        double denominator = 1.0;
        int    direction = NO_CHANGE; // sound movement away or towards listener

        Point3f  lastXformPosition;
        Point3f  lastXformCenterEar;
        Point3f  xformPosition;
        Point3f  xformCenterEar;
        float    averagedSoundDistances = 0.0f;
        float    averagedEarsDistances = 0.0f;
    
        /*
         *  Average the differences between the last MAX_DISTANCE
         *  sound positions and head positions
         */
        if (!averageDistances) {
            // TODO: Use some EPSilion to do 'equals' test against
            if (dopplerFlag)
                debugPrint("JSPositionalSample.calculateDoppler - " +
                           "not enough distance data collected, " +
                           "dopplerRatio set to zero");
            // can't calculate change in direction
            return 0.0f; // sample rate ratio is zero
        }

        lastXformPosition = positions[lastIndex];
        lastXformCenterEar = centerEars[lastIndex];
        xformPosition = positions[currentIndex];
        xformCenterEar = centerEars[currentIndex];
        distanceSourceToHead = xformPosition.distance(xformCenterEar);
        lastDistanceSourceToHead = lastXformPosition.distance(lastXformCenterEar);
        if (dopplerFlag) {
                debugPrint("JSPositionalSample.calculateDoppler - distances: " +
                           "current,last = " + distanceSourceToHead + ", " +
                           lastDistanceSourceToHead );
                debugPrint("                                      " +
                    "current position = " +
                    xformPosition.x + ", " + xformPosition.y +
                    ", " + xformPosition.z);
                debugPrint("                                      " +
                    "current ear = " +
                    xformCenterEar.x + ", " + xformCenterEar.y +
                    ", " + xformCenterEar.z);
                debugPrint("                                      " +
                    "last position = " +
                    lastXformPosition.x + ", " + lastXformPosition.y +
                    ", " + lastXformPosition.z);
                debugPrint("                                      " +
                    "last ear = " +
                    lastXformCenterEar.x + ", " + lastXformCenterEar.y +
                    ", " + lastXformCenterEar.z);
        }
        if (distanceSourceToHead == lastDistanceSourceToHead) {
            // TODO: Use some EPSilion to do 'equals' test against
            if (dopplerFlag)
                debugPrint("JSPositionalSample.calculateDoppler - " +
                           "distance diff = 0, dopplerRatio set to zero");
            // can't calculate change in direction
            return 0.0f; // sample rate ratio is zero
        }
           
        deltaTime = times[currentIndex] - times[firstIndex];
        for (int i=0; i<(MAX_DISTANCES-1); i++) {
                averagedSoundDistances += positions[i+1].distance(positions[i]);
                averagedEarsDistances += centerEars[i+1].distance(centerEars[i]);
        }
        averagedSoundDistances /= (MAX_DISTANCES-1);
        averagedEarsDistances /= (MAX_DISTANCES-1);
        soundVelocity = averagedSoundDistances/deltaTime;
        headVelocity = averagedEarsDistances/deltaTime;
        if (dopplerFlag) {
                debugPrint("                                      " +
                    "delta time = " + deltaTime );
                debugPrint("                                      " +
                    "soundPosition delta = " + 
                    xformPosition.distance(lastXformPosition));
                debugPrint("                                      " +
                    "soundVelocity = " + soundVelocity);
                debugPrint("                                      " +
                    "headPosition delta = " + 
                    xformCenterEar.distance(lastXformCenterEar));
                debugPrint("                                      " +
                    "headVelocity = " + headVelocity);
        }
        if (attribs != null) {

                float rolloff = attribs.rolloff;
                float velocityScaleFactor = attribs.velocityScaleFactor;
                if (rolloff != 1.0f) {
                    speedOfSound *=  rolloff;
                    if (dopplerFlag)
                        debugPrint("                                      " +
                            "attrib rollof = " + rolloff);
                }
                if (velocityScaleFactor != 1.0f) {
                    soundVelocity *= velocityScaleFactor;
                    headVelocity *= velocityScaleFactor;
                    if (dopplerFlag) {
                        debugPrint("                                      " +
                            "attrib velocity scale factor = " + 
                             velocityScaleFactor );
                        debugPrint("                                      " +
                            "new soundVelocity = " + soundVelocity);
                        debugPrint("                                      " +
                            "new headVelocity = " + headVelocity);
                    }
                }
        }
        if (distanceSourceToHead < lastDistanceSourceToHead) {
                // sound and head moving towards each other
                if (dopplerFlag)
                    debugPrint("                                      " +
                     "moving towards...");
                direction = TOWARDS;
                numerator = speedOfSound + headVelocity;
                denominator = speedOfSound - soundVelocity;
        }
        else { 
                // sound and head moving away from each other
                //    note: no change in distance case covered above
                if (dopplerFlag)
                    debugPrint("                                      " +
                     "moving away...");
                direction = AWAY;
                numerator = speedOfSound - headVelocity;
                denominator = speedOfSound + soundVelocity;
        }
        if (numerator <= 0.0) {
                if (dopplerFlag)
                    debugPrint("JSPositionalSample.calculateDoppler: " +
                               "BOOM!! - velocity of head > speed of sound");
                return -1.0f;
        }
        else if (denominator <= 0.0) {
                if (dopplerFlag)
                    debugPrint("JSPositionalSample.calculateDoppler: " + 
                               "BOOM!! - velocity of sound source negative");
                return -1.0f;
        }
        else {
                if (dopplerFlag)
                    debugPrint("JSPositionalSample.calculateDoppler: " +
                               "numerator = " + numerator +
                               ", denominator = " + denominator );
                sampleRateRatio = numerator / denominator;
        }

/********
  IF direction WERE important to calling method...
     * Return value greater than 0 denotes direction of sound source is
     *        towards the listener
     * Return value less than 0 denotes direction of sound source is
     *        away from the listener
        if (direction == AWAY)
            return -((float)sampleRateRatio);
        else
            return (float)sampleRateRatio;
*********/
        return (float)sampleRateRatio;
    }

    void updateEar(int dirtyFlags, View view) {
        if (debugFlag)
            debugPrint("*** updateEar fields");
        // xform Ear
        Point3f  xformCenterEar = new Point3f();
        if (!calculateNewEar(dirtyFlags, view, xformCenterEar))  {
            if (debugFlag)
                debugPrint("calculateNewEar returned false");
            return;
        }
        // store ear and increment indices ONLY if there is an actual change
        if (xformCenterEar.x == centerEars[currentIndex].x &&
            xformCenterEar.y == centerEars[currentIndex].y &&
            xformCenterEar.z == centerEars[currentIndex].z ) {
            if (debugFlag)
                debugPrint("    No change in ear, so don't reset");
            return;
        }
        // store xform Ear 
        incrementIndices();
        times[currentIndex] = System.currentTimeMillis();
        centerEars[currentIndex].set(xformCenterEar);
        // since this is a change to the head position and not the sound
        // position save the last sound position into the current element
        if (numDistances > 1)
            positions[currentIndex].set(positions[lastIndex]);
    }

    boolean calculateNewEar(int dirtyFlags, View view, Point3f xformCenterEar) {
        /*
         * Transform ear position (from Head) into Virtual World Coord space
         */
        Point3d  earPosition = new Point3d();   // temporary double Point

        // TODO: check dirty flags coming in
        //     For now, recalculate ear positions by forcing earsXformed false
        boolean  earsXformed = false;  
        if (!earsXformed) { 
              if (view != null) {
                PhysicalBody body = view.getPhysicalBody();
                if (body != null) {

                    // Get Head Coord. to Virtual World transform
		    // TODO: re-enable this when userHeadToVworld is 
                    //     implemented correctly!!!
                    Transform3D headToVwrld = new Transform3D();
                    view.getUserHeadToVworld(headToVwrld);
                    if (debugFlag) {
                        debugPrint("user head to Vwrld colum-major:");
                        double[] matrix = new double[16];
                        headToVwrld.get(matrix);
                        debugPrint("JSPosSample    " + matrix[0]+", " +
                             matrix[1]+", "+matrix[2]+", "+matrix[3]);
                        debugPrint("JSPosSample    " + matrix[4]+", " +
                             matrix[5]+", "+matrix[6]+", "+matrix[7]);
                        debugPrint("JSPosSample    " + matrix[8]+", " +
                             matrix[9]+", "+matrix[10]+", "+matrix[11]);
                        debugPrint("JSPosSample    " + matrix[12]+", " +
                             matrix[13]+", "+matrix[14]+", "+matrix[15]);
                    }

                    // Get left and right ear positions in Head Coord.s
                    // Transforms left and right ears to Virtual World coord.s
                    body.getLeftEarPosition(earPosition);
                    xformLeftEar.x = (float)earPosition.x;
                    xformLeftEar.y = (float)earPosition.y;
                    xformLeftEar.z = (float)earPosition.z;
                    body.getRightEarPosition(earPosition);
                    xformRightEar.x = (float)earPosition.x;
                    xformRightEar.y = (float)earPosition.y;
                    xformRightEar.z = (float)earPosition.z;
                    headToVwrld.transform(xformRightEar);
                    headToVwrld.transform(xformLeftEar);
                    // Transform head viewing (Z) axis to Virtual World coord.s
                    xformHeadZAxis.set(0.0f, 0.0f, -1.0f); // Va
                    headToVwrld.transform(xformHeadZAxis);

                    // calculate the new (current) mid-point between the ears
                    // find the mid point between left and right ear positions
                    xformCenterEar.x = xformLeftEar.x + 
                         ((xformRightEar.x - xformLeftEar.x)*0.5f);
                    xformCenterEar.y = xformLeftEar.y + 
                         ((xformRightEar.y - xformLeftEar.y)*0.5f);
                    xformCenterEar.z = xformLeftEar.z + 
                         ((xformRightEar.z - xformLeftEar.z)*0.5f);
                    // TODO: when head changes earDirty should be set!
                    // earDirty = false;
                    if (debugFlag) {
                        debugPrint("           earXformed CALCULATED");
                        debugPrint("           xformCenterEar = " + 
                                    xformCenterEar.x + " " +
                                    xformCenterEar.y + " " +
                                    xformCenterEar.z ); 
                    }
                    earsXformed = true;
                } // end of body NOT null
              } // end of view NOT null
        } // end of earsDirty
        else {
            // TODO:  use existing transformed ear positions
        }

        if (!earsXformed) {
            // uses the default head position of (0.0, -0.03, 0.095)
            if (debugFlag)
                debugPrint("           earXformed NOT calculated");
        } 
        return earsXformed;
    }

    /**  
     * Render this sample
     *
     * Calculate the audiodevice parameters necessary to spatially play this
     * sound.
     */  
    public void render(int dirtyFlags, View view, AuralParameters attribs) {
        if (debugFlag)
            debugPrint("JSPositionalSample.render"); 
        updateEar(dirtyFlags, view);

        /*
         * Time to check velocities and change the playback rate if necessary...
         *
         * Rolloff value MUST be > zero for any sound to be heard!
         *     If rolloff is zero, all sounds affected by AuralAttribute region
         *     are silent.
         * FrequencyScaleFactor value MUST be > zero for any sound to be heard!
         *     since Sfreq' = Sfreq * frequencyScaleFactor.
         *     If FrequencyScaleFactor is zero, all sounds affected by 
         *     AuralAttribute region are paused.
         * VelocityScaleFactor value of zero disables Doppler calculations.
         *
         * Scale 'Doppler' rate (or lack of Doppler) by frequencyScaleFactor.
         */
        float dopplerRatio = 1.0f;
        if (attribs != null) {
            float rolloff = attribs.rolloff;
            float frequencyScaleFactor = attribs.frequencyScaleFactor;
            float velocityScaleFactor = attribs.velocityScaleFactor;
            if (debugFlag || dopplerFlag)
                debugPrint("JSPositionalSample: attribs NOT null");
            if (rolloff <= 0.0f) {
                if (debugFlag)
                    debugPrint("    rolloff = " + rolloff + " <= 0.0" );
                // TODO: Make sound silent
                // return ???
            }
            else if (frequencyScaleFactor <= 0.0f) {
                if (debugFlag)
                    debugPrint("    freqScaleFactor = " + frequencyScaleFactor +
                               " <= 0.0" );
                // TODO: Pause sound silent
                // return ???
            }
            else if (velocityScaleFactor > 0.0f) {
                if (debugFlag || dopplerFlag)
                    debugPrint("    velocityScaleFactor = " +
                                       velocityScaleFactor);
/*******
                if (deltaTime > 0) {
*******/
                    // Doppler can be calculated after the second time
                    // updateXformParams() is executed
                    dopplerRatio = calculateDoppler(attribs);

                    if (dopplerRatio == 0.0f) {
                        // dopplerRatio zeroo denotes no changed
                        // TODO: But what if frequencyScaleFactor has changed
                        if (debugFlag) {
                            debugPrint("JSPositionalSample: render: " +
                                       "dopplerRatio returned zero; no change");
                        }
                    }
                    else if (dopplerRatio == -1.0f) {
                        // error returned by calculateDoppler
                        if (debugFlag) {
                            debugPrint("JSPositionalSample: render: " +
                                       "dopplerRatio returned = " + 
                                       dopplerRatio + "< 0");
                        }
                        // TODO: Make sound silent
                        // return ???
                    }
                    else if (dopplerRatio > 0.0f) {
                        // rate could be changed
                        rateRatio = dopplerRatio * frequencyScaleFactor *
                                    getRateScaleFactor();
                        if (debugFlag) {
                            debugPrint("    scaled by frequencyScaleFactor = " +
                                    frequencyScaleFactor );
                        }
                    }
/******
                }
                else  {
                    if (debugFlag)
                        debugPrint("deltaTime <= 0 - skip Doppler calc");
                }
******/
            }
            else  { // auralAttributes not null but velocityFactor <= 0
              // Doppler is disabled
              rateRatio = frequencyScaleFactor * getRateScaleFactor(); 
            }
        }
        /* 
         * since aural attributes undefined, default values are used,
         *   thus no Doppler calculated
         */
        else {
            if (debugFlag || dopplerFlag)
                debugPrint("JSPositionalSample: attribs null");
            rateRatio = 1.0f;
        }

        this.panSample(attribs);
    }    

    /* *****************
     *   
     *  Calculate Angular Gain
     *   
     * *****************/
    /*   
     *  Calculates the Gain scale factor applied to the overall gain for
     *  a sound based on angle between a sound's projected direction and the
     *  vector between the sounds position and center ear.
     *
     *  For Point Sounds this value is always 1.0f.
     */  
    float calculateAngularGain() {
        return(1.0f);
    }

    /* *****************
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
        if ((filterType != AuralParameters.NO_FILTERING) && arrayLength > 0) {
            double[] distanceArray = new double[arrayLength];
            float[]  cutoffArray = new float[arrayLength];
            attribs.getDistanceFilter(distanceArray, cutoffArray);

            if (debugFlag) {
                debugPrint("distanceArray    cutoffArray");
                for (int i=0; i<arrayLength; i++)
                    debugPrint((float)(distanceArray[i]) + ", " + cutoffArray[i]);
            }
            distanceFilter = findFactor((double)distance, 
                   distanceArray, cutoffArray);
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

        // Angular filter only applies to directional sound sources.
        angularFilterFound = false;
        angularFilter = -1.0f;

        filterFlag = distanceFilterFound || angularFilterFound;
        filterFreq = distanceFilter;
        if (debugFlag) 
            debugPrint("    calculateFilter flag,freq = " + filterFlag +
           "," + filterFreq );
    }

    /* *****************
     *   
     *  Find Factor
     *   
     * *****************/
    /*
     *  Interpolates the correct output factor given a 'distance' value
     *  and references to the distance array and factor array used in
     *  the calculation.  These array parameters could be either linear or
     *  angular distance arrays, or filter arrays.
     *  The values in the distance array are monotonically increasing.
     *  This method looks at pairs of distance array values to find which
     *  pair the input distance argument is between distanceArray[index] and
     *  distanceArray[index+1].
     *  The index is used to get factorArray[index] and factorArray[index+1].
     *  Then the ratio of the 'distance' between this pair of distanceArray 
     *  values is used to scale the two found factorArray values proportionally.
     *  The resulting factor is returned, unless there is an error, then -1.0
     *  is returned.
     */  
    float findFactor(double distance, 
                     double[] distanceArray, float[] factorArray) {
        int     index, lowIndex, highIndex, indexMid;

        if (debugFlag)
            debugPrint("JSPositionalSample.findFactor entered");

        /*
         * Error checking
         */
        if (distanceArray == null || factorArray == null) {
            if (debugFlag)
                debugPrint("   findFactor: arrays null");
            return -1.0f;  // no value
        }
        int arrayLength = distanceArray.length;
        if (arrayLength < 2) {
            if (debugFlag)
                debugPrint("   findFactor: arrays length < 2");
            return -1.0f; // no value
        }
        int largestIndex = arrayLength - 1;

        /*
         * Calculate distanceGain scale factor
         */
        if (distance >= distanceArray[largestIndex]) {
            if (debugFlag) {
                debugPrint("   findFactor: distance > " + 
                                  distanceArray[largestIndex]);
                debugPrint("   distanceArray length = "+ arrayLength);
            }
            return factorArray[largestIndex];
        }
        else if (distance <= distanceArray[0]) {
            if (debugFlag)
                debugPrint("   findFactor: distance < " +
                                    distanceArray[0]);
            return factorArray[0];
        } 
        /*
         * Distance between points within attenuation array.
         * Use binary halfing of distance array
         */
        else {
            lowIndex = 0;
            highIndex = largestIndex;
            if (debugFlag)
                debugPrint("   while loop to find index: ");
            while (lowIndex < (highIndex-1)) {
                if (debugFlag) {
                    debugPrint("       lowIndex " + lowIndex + 
                       ", highIndex " + highIndex);
                    debugPrint("       d.A. pair for lowIndex " + 
                       distanceArray[lowIndex] +  ", " + factorArray[lowIndex] );
                    debugPrint("       d.A. pair for highIndex " + 
                       distanceArray[highIndex] +  ", " + factorArray[highIndex] );
                }
                /*
                 * we can assume distance is between distance atttenuation vals
                 * distanceArray[lowIndex] and distanceArray[highIndex]
                 * calculate gain scale factor based on distance
                 */
                if (distanceArray[lowIndex] >= distance) {
                    if (distance < distanceArray[lowIndex]) {
                        if (internalErrors)
                            debugPrint("Internal Error: binary halving in " +
                            " findFactor failed; distance < index value");
                    }
                    if (debugFlag) {
                        debugPrint( "       index == distanceGain " +
                           lowIndex);
                        debugPrint("        findFactor returns [LOW=" + 
                           lowIndex + "] " + factorArray[lowIndex]);
                    }
                    // take value of scale factor directly from factorArray
                    return factorArray[lowIndex];
                }
                else if (distanceArray[highIndex] <= distance) {
                    if (distance > distanceArray[highIndex]) {
                        if (internalErrors)
                            debugPrint("Internal Error: binary halving in " +
                            " findFactor failed; distance > index value");
                    }
                    if (debugFlag) {
                        debugPrint( "       index == distanceGain " +
                           highIndex);
                        debugPrint("        findFactor returns [HIGH=" + 
                           highIndex + "] " + factorArray[highIndex]);
                    }
                    // take value of scale factor directly from factorArray
                    return factorArray[highIndex];
                }
                if (distance > distanceArray[lowIndex] && 
                    distance < distanceArray[highIndex] ) {
                    indexMid = lowIndex + ((highIndex - lowIndex) / 2);
                    if (distance <= distanceArray[indexMid])
                        // value of distance in lower "half" of list
                        highIndex = indexMid;
                    else // value if distance in upper "half" of list
                        lowIndex = indexMid;
                }
            } /* of while */

            /*
             * ratio: distance from listener to sound source 
             *        between lowIndex and highIndex times
             *        attenuation value between lowIndex and highIndex
             * gives linearly interpolationed attenuation value
             */
            if (debugFlag) {
                debugPrint( "   ratio calculated using lowIndex " +
                       lowIndex + ", highIndex " + highIndex);
                debugPrint( "   d.A. pair for lowIndex " + 
                        distanceArray[lowIndex]+", "+factorArray[lowIndex] );
                debugPrint( "   d.A. pair for highIndex " + 
                        distanceArray[highIndex]+", "+factorArray[highIndex] );
            }

            float outputFactor = 
                   ((float)(((distance - distanceArray[lowIndex])/
                    (distanceArray[highIndex] - distanceArray[lowIndex]) ) ) *
                    (factorArray[highIndex] - factorArray[lowIndex]) ) +
                   factorArray[lowIndex] ; 
            if (debugFlag)
                debugPrint("    findFactor returns " + outputFactor);
            return outputFactor;
        }  
    }

    /**
     * CalculateDistanceAttenuation
     *
     * Simply calls generic (for PointSound) 'findFactor()' with 
     * a single set of attenuation distance and gain scale factor arrays.
     */
    float calculateDistanceAttenuation(float distance) {
        float  factor = 1.0f;
        factor = findFactor((double)distance, this.attenuationDistance,
                           this.attenuationGain);
        if (factor >= 0.0)
            return (factor);
        else
            return (1.0f);
    }

    /* ******************
     *   
     *  Pan Sample
     *   
     * ******************/
    /*  
     *  Sets pan and delay for a single sample associated with this Sound.
     *  Front and Back quadrants are treated the same.
     */
    void panSample(AuralParameters attribs) {
        int     quadrant = 1;
        float   intensityHigh = 1.0f;
        float   intensityLow = 0.125f;
        float   intensityDifference = intensityHigh - intensityLow;

        //TODO: time around "average" default head
        // int     delayHigh = 32;  // 32.15 samples = .731 ms
        // int     delayLow = 0;

        float   intensityOffset; // 0.0 -> 1.0 then 1.0 -> 0.0 for full rotation
        float   halfX;
        int     id;
        int     err;

        float   nearZero = 0.000001f;
        float   nearOne  = 0.999999f;
        float   nearNegativeOne  = -nearOne;
        float   halfPi = (float)Math.PI * 0.5f;
        /*
         * Parameters used for IID and ITD equations.
         * Name of parameters (as used in Guide, E.3) are denoted in comments.
         */
        float    distanceSourceToCenterEar = 0.0f;     // Dh
        float    lastDistanceSourceToCenterEar = 0.0f;
        float    distanceSourceToRightEar = 0.0f;      // Ef or Ec
        float    distanceSourceToLeftEar = 0.0f;       // Ef or Ec
        float    distanceBetweenEars = 0.18f;          // De
        float    radiusOfHead = 0.0f;                  // De/2
        float    radiusOverDistanceToSource = 0.0f;    // De/2 * 1/Dh

        float    alpha = 0.0f;                         // 'alpha'
        float    sinAlpha = 0.0f;                      // sin(alpha);
        float    gamma = 0.0f;                         // 'gamma'

        // Speed of Sound (unaffected by rolloff) in millisec/meters
        float    speedOfSound = attribs.SPEED_OF_SOUND;
        float    invSpeedOfSound = 1.0f / attribs.SPEED_OF_SOUND;

        float    sampleRate = 44.1f;                   // 44 samples/millisec

        boolean  rightEarClosest = false;
        boolean  soundFromBehind = false;

        float    distanceGain = 1.0f;
        float    allGains = this.gain; // product of gain scale factors
  
        Point3f  workingPosition = new Point3f();
        Point3f  workingCenterEar = new Point3f();

        // Asuumes that head and ear positions can be retrieved from universe

        Vector3f mixScale = new Vector3f();  // for mix*Samples code

        // Use transformed position of this sound
        workingPosition.set(positions[currentIndex]);
        workingCenterEar.set(centerEars[currentIndex]);
        if (debugFlag) {
            debugPrint("panSample:workingPosition from" +
                 " positions["+currentIndex+"] -> " +
                 workingPosition.x + ", " + workingPosition.y + ", " +
                 workingPosition.z + " for pointSound " + this);
            debugPrint("panSample:workingCenterEar " +
                 workingCenterEar.x + " " + workingCenterEar.y + " " +
                 workingCenterEar.z);
            debugPrint("panSample:xformLeftEar " +
                 xformLeftEar.x + " " + xformLeftEar.y + " " +
                 xformLeftEar.z);
            debugPrint("panSample:xformRightEar " +
                 xformRightEar.x + " " + xformRightEar.y + " " +
                 xformRightEar.z);
        }

        // Create the vectors from the sound source to head positions
        sourceToCenterEar.x = workingCenterEar.x - workingPosition.x;
        sourceToCenterEar.y = workingCenterEar.y - workingPosition.y;
        sourceToCenterEar.z = workingCenterEar.z - workingPosition.z;
        sourceToRightEar.x = xformRightEar.x - workingPosition.x;
        sourceToRightEar.y = xformRightEar.y - workingPosition.y;
        sourceToRightEar.z = xformRightEar.z - workingPosition.z;
        sourceToLeftEar.x = xformLeftEar.x - workingPosition.x;
        sourceToLeftEar.y = xformLeftEar.y - workingPosition.y;
        sourceToLeftEar.z = xformLeftEar.z - workingPosition.z;

        /*
         * get distances from SoundSource to 
         *    (i)   head origin
         *    (ii)  right ear
         *    (iii) left ear
         */
        distanceSourceToCenterEar = workingPosition.distance(workingCenterEar);
        distanceSourceToRightEar = workingPosition.distance(xformRightEar);
        distanceSourceToLeftEar = workingPosition.distance(xformLeftEar);
        distanceBetweenEars = xformRightEar.distance(xformLeftEar);
        if (debugFlag)
            debugPrint("           distance from left,right ears to source: = (" +
                  distanceSourceToLeftEar + ", " + distanceSourceToRightEar + ")");

        radiusOfHead = distanceBetweenEars * 0.5f;
        if (debugFlag)
            debugPrint("           radius of head = " + radiusOfHead );
        radiusOverDistanceToSource =    // De/2 * 1/Dh
                    radiusOfHead/distanceSourceToCenterEar;
        if (debugFlag)
            debugPrint("           radius over distance = " + radiusOverDistanceToSource );
        if (debugFlag) {
            debugPrint("panSample:source to center ear " +
                 sourceToCenterEar.x + " " + sourceToCenterEar.y + " " +
                 sourceToCenterEar.z );
            debugPrint("panSample:xform'd Head ZAxis " +
                 xformHeadZAxis.x + " " + xformHeadZAxis.y + " " +
                 xformHeadZAxis.z );
            debugPrint("panSample:length of sourceToCenterEar " +
                 sourceToCenterEar.length());
            debugPrint("panSample:length of xformHeadZAxis " +
                 xformHeadZAxis.length());
        }

        // Dot Product 
        double dotProduct = (double)(
                    (sourceToCenterEar.dot(xformHeadZAxis))/
                    (sourceToCenterEar.length() * xformHeadZAxis.length()));
        if (debugFlag)
            debugPrint( "           dot product = " + dotProduct );
        alpha = (float)(Math.acos(dotProduct));
        if (debugFlag)
            debugPrint( "           alpha = " + alpha );

        if (alpha > halfPi) {
            if (debugFlag)
                debugPrint("           sound from behind");
            soundFromBehind = true;
            alpha = (float)Math.PI - alpha;
            if (debugFlag)
                debugPrint( "           PI minus alpha =>" + alpha );
        }
        else {
            soundFromBehind = false;
            if (debugFlag)
                debugPrint("           sound from in front");
        }

        gamma = (float)(Math.acos(radiusOverDistanceToSource));
        if (debugFlag)
            debugPrint( "           gamma " + gamma );

        rightEarClosest = 
            (distanceSourceToRightEar>distanceSourceToLeftEar) ? false : true ;
        /*
         * Determine the quadrant sound is in 
         */
        if (rightEarClosest) {
            if (debugFlag)
                debugPrint( "           right ear closest");
            if (soundFromBehind)
                quadrant = 4;
            else
                quadrant = 1;
        }
        else  {
            if (debugFlag)
                debugPrint( "           left ear closest");
            if (soundFromBehind)
                quadrant = 3;
            else
                quadrant = 2;
        }
        sinAlpha = (float)(Math.sin((double)alpha));
        if (sinAlpha < 0.0) sinAlpha = -sinAlpha;
        if (debugFlag)
            debugPrint( "           sin(alpha) " + sinAlpha );

        /*
         * The path from sound source to the farthest ear is always indirect
         * (it wraps around part of the head).
         * Calculate distance wrapped around the head for farthest ear
         */
        float DISTANCE = (float)Math.sqrt((double)
                distanceSourceToCenterEar * distanceSourceToCenterEar +
                radiusOfHead * radiusOfHead);
        if (debugFlag)
            debugPrint( "           partial distance from edge of head to source = "
                      + distanceSourceToCenterEar);
        if (rightEarClosest) {
            distanceSourceToLeftEar = 
                DISTANCE + radiusOfHead * (halfPi+alpha-gamma);
            if (debugFlag)
                debugPrint("           new distance from left ear to source = "
                      + distanceSourceToLeftEar);
        } 
        else {
            distanceSourceToRightEar = 
                DISTANCE + radiusOfHead * (halfPi+alpha-gamma);
            if (debugFlag)
                debugPrint("           new distance from right ear to source = "
                      + distanceSourceToRightEar);
        }
        /*
         * The path from the source source to the closest ear could either
         * be direct or indirect (wraps around part of the head).
         * if sinAlpha >= radiusOverDistance path of sound to closest ear
         * is direct, otherwise it is indirect
         */
        if (sinAlpha < radiusOverDistanceToSource) {
            if (debugFlag)
                debugPrint("           closest path is also indirect ");
            // Path of sound to closest ear is indirect
 
            if (rightEarClosest) {
                distanceSourceToRightEar = 
                    DISTANCE + radiusOfHead * (halfPi-alpha-gamma);
                if (debugFlag)
                    debugPrint("           new distance from right ear to source = "
                      + distanceSourceToRightEar);
            }
            else {
                distanceSourceToLeftEar = 
                    DISTANCE + radiusOfHead * (halfPi-alpha-gamma);
                if (debugFlag)
                    debugPrint("           new distance from left ear to source = "
                      + distanceSourceToLeftEar);
            }
        }
        else {
            if (debugFlag)
                debugPrint("           closest path is direct ");
            if (rightEarClosest) {
                if (debugFlag)
                    debugPrint("           direct distance from right ear to source = "
                      + distanceSourceToRightEar);
            }
            else {
                if (debugFlag)
                    debugPrint("           direct distance from left ear to source = "
                      + distanceSourceToLeftEar);
            }
        }

        /**
         * Short-cut taken.  Rather than using actual delays from source
         * (where the overall distances would be taken into account in 
         * determining delay) the difference in the left and right delay
         * are applied.
         * This approach will be preceptibly wrong for sound sources that
         * are very far away from the listener so both ears would have 
         * large delay.
         */
        sampleRate = channel.rateInHz * (0.001f); // rate in milliseconds
        if (rightEarClosest) {
            rightDelay = 0;
            leftDelay = (int)((distanceSourceToLeftEar - distanceSourceToRightEar) *
                    invSpeedOfSound * sampleRate);
        }
        else {
            leftDelay = 0;
            rightDelay = (int)((distanceSourceToRightEar - distanceSourceToLeftEar) *
                    invSpeedOfSound * sampleRate);
        }

        if (debugFlag) {
            debugPrint("           using inverted SoS = " + invSpeedOfSound);
            debugPrint("           and sample rate = " + sampleRate);
            debugPrint("           left and right delay = ("
                      + leftDelay + ", " + rightDelay + ")");
        }

        // What should the gain be for the different ears???
        // TODO: now using a hack that sets gain based on a unit circle!!!
        workingPosition.sub(workingCenterEar); // offset sound pos. by head origin
        // normalize; put Sound on unit sphere around head origin
        workingPosition.scale(1.0f/distanceSourceToCenterEar);
        if (debugFlag)
            debugPrint("           workingPosition after unitization " +
              workingPosition.x+" "+workingPosition.y+" "+workingPosition.z );

        /*
         * Get the correct distance gain scale factor from attenuation arrays.
         * This requires that sourceToCenterEar vector has been calculated.
         */
        // TODO: now using distance from center ear to source
        //    Using distances from each ear to source would be more accurate
        distanceGain = calculateDistanceAttenuation(distanceSourceToCenterEar);

        allGains *= distanceGain;

        /*
         * Add angular gain (for Cone sound)
         */
        if (debugFlag)
            debugPrint("           all Gains (without angular gain) " + allGains);
        // assume that transfromed Position is already calculated
        allGains *= this.calculateAngularGain();
        if (debugFlag)
            debugPrint("                     (incl. angular gain)  " + allGains);

        halfX = workingPosition.x/2.0f;
        if (halfX >= 0)
            intensityOffset = (intensityDifference * (0.5f - halfX));
        else
            intensityOffset = (intensityDifference * (0.5f + halfX));

        /*
         * For now have delay constant for front back sound for now
         */
        if (debugFlag)
            debugPrint("panSample:                 quadrant " + quadrant);
        switch (quadrant) {
           case 1: 
              // Sound from front, right of center of head
           case 4:
              // Sound from back, right of center of head
              rightGain = allGains * (intensityHigh - intensityOffset);
              leftGain =  allGains * (intensityLow + intensityOffset);
              break;

           case 2:
              // Sound from front, left of center of head
           case 3:
              // Sound from back, right of center of head
              leftGain = allGains * (intensityHigh - intensityOffset);
              rightGain = allGains * (intensityLow + intensityOffset);
              break;
        }  /* switch */
        if (debugFlag)
            debugPrint("panSample:                 left/rightGain " + leftGain +
                        ", " + rightGain);

        // Combines distance and angular filter to set this sample's current
        // frequency cutoff value
        calculateFilter(distanceSourceToCenterEar, attribs); 

    }  /* panSample() */

// NOTE: setGain in audioengines.Sample is used to set/get user suppled factor
//     this class uses this single gain value to calculate the left and
//     right gain values
}
