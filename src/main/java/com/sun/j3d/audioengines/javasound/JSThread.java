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

package com.sun.j3d.audioengines.javasound;

/*
 * JavaSound engine Thread
 *
 * IMPLEMENTATION NOTE: The JavaSoundMixer is incomplete and really needs
 * to be rewritten.  When this is done, we may or may not need this class.
 */

import javax.media.j3d.*;
import com.sun.j3d.audioengines.*;

/**
 * The Thread Class extended for JavaSound Mixer specific audio device
 * calls that dynamically, in 'real-time" change engine parameters
 * such as volume/gain and sample-rate/frequency(pitch).
 */

class JSThread extends com.sun.j3d.audioengines.AudioEngineThread {

    /**
     * The thread data for this thread
     */
    int totalChannels = 0;
    /**
     * flags denoting if dynamic gain or rate interpolation is to be performed 
     */
    boolean rampGain = false;

    // global thread flat rampRate set true only when setTargetRate called
    // for any sample. but it is cleared only by doWork when no sample
    // has a need for the rate to be ramped any further.
    boolean rampRate = false;

/*** TODO:
     *
     * scalefactors applied to current sample rate to determine delta changes
     * in rate (in Hz)
     *
    float   currentGain = 1.0f;
    float   targetGain = 1.0f;
***********/

    // reference to engine that created this thread
    AudioEngine3D audioEngine = null;

    /**
     * This constructor simply assigns the given id.
     */
    JSThread(ThreadGroup t, AudioEngine3DL2 engine) {
	super(t, "J3D-JavaSoundThread");
        audioEngine = engine;
        // TODO: really get total JavaSound channels
        totalChannels = 32;
        if (debugFlag)
            debugPrint("JSThread.constructor("+t+")");
    }



    /**
     * This method performs one iteration of pending work to do
     * 
     * Wildly "garbled" sounds was caused by unequal changes in delta
     * time verses delta distances (resulting in jumps in rate factors
     * calculated for Doppler.  This work thread is meant to smoothly
     * increment/decrement changes in rate (and other future parameters)
     * until the target value is reached.
     */
    synchronized public void doWork() {
        if (debugFlag)
            debugPrint("JSThread.doWork()");
/*******
        while (rampRate || rampGain) {
*********/
/****** DESIGN
// Loop while sound is playing, reget attributes and gains/reverb,... params
// update lowlevel params then read modify then copy to line(s)

can keep my own loop count for streams??? not really

*******/
            // QUESTION: will size ever get smaller after get performed???
            int numSamples = audioEngine.getSampleListSize();
            JSSample sample = null;
            int numRateRamps = 0;            
            for (int index = 0; index < numSamples; index++) {
                // loop thru samples looking for ones needing rate incremented
                sample = (JSSample)audioEngine.getSample(index);
                if (sample == null)
                    continue;
                if (sample.getRampRateFlag()) {
                    if (debugFlag)
                        debugPrint("    rampRate true");
                    boolean endOfRampReached = adjustRate(sample);
                    sample.setRampRateFlag(!endOfRampReached);
                    if (!endOfRampReached)
                        numRateRamps++;
                }
                // TODO: support changes in gain this way as well
            }
            if (numRateRamps > 0) {
                rampRate = true;
runMonitor(RUN, 0, null);
            }
            else
                rampRate = false;
/*********
            try {
                Thread.sleep(4);
            } catch (InterruptedException e){}
*********/
/********
        } // while
*********/
        // otherwise do nothing
    }

    int getTotalChannels() {
        return (totalChannels);
    }

    /**
     * Gradually change rate scale factor
     * 
     * If the rate change is too great suddenly, it sounds like a
     * jump, so we need to change gradually over time.
     * Since an octive delta change up is 2.0 but down is 0.5, forced
     * "max" rate of change is different for both.
     * @return true if target rate value was reached
     */
    boolean adjustRate(JSSample sample) {
        // QUESTION: what should max delta rate changes be
        // Using 1/32 of a half-step (1/12 of an octive)???
        double maxRateChangeDown = 0.00130213;
        double maxRateChangeUp   = 0.00260417;

        double lastActualRateRatio = sample.getCurrentRateRatio();
        double requestedRateRatio = sample.getTargetRateRatio();
        boolean endOfRamp = false; // flag denotes if target rate reached
        if ( lastActualRateRatio > 0 ) {
            double sampleRateRatio = requestedRateRatio; // in case diff = 0
            double diff = 0.0;
            if (debugFlag) {
                debugPrint("JSThread.adjustRate: between " +
                        lastActualRateRatio + " & " + requestedRateRatio);
            }
            diff = requestedRateRatio - lastActualRateRatio;
            if (diff > 0.0) { // direction of movement is towards listener
                // inch up towards the requested target rateRatio
                if (diff >= maxRateChangeUp) {
                    sampleRateRatio = lastActualRateRatio + maxRateChangeUp;
                    if (debugFlag) {
                        debugPrint("         adjustRate: " +
                                "diff >= maxRateChangeUp so ");
                        debugPrint("         adjustRate: " +
                                "    sampleRateRatio incremented up by max");
                    }
                    endOfRamp = false; // target value not reached
                }
                /* 
                 * otherwise delta change is within tolerance
                 *    so use requested RateRatio as calculated w/out change
                 */
                else {
                    sampleRateRatio = requestedRateRatio;
                    if (debugFlag) {
                        debugPrint("         adjustRate: " +
                                "    requestedRateRatio reached");
                    }
                    endOfRamp = true; // reached
                }
            }
            else if (diff < 0.0) { // movement is away from listener
                // inch down towards the requested target rateRatio
                if ((-diff) >= maxRateChangeDown) {
                    sampleRateRatio = lastActualRateRatio - maxRateChangeDown;
                    if (debugFlag) {
                        debugPrint("         adjustRate: " +
                                "-(diff) >= maxRateChangeUp so ");
                        debugPrint("         adjustRate: " +
                                "    sampleRateRatio incremented down by max ");
                    }
                    endOfRamp = false; // target value not reached
                }
                /*
                 * otherwise negitive delta change is within tolerance so
                 * use sampleRateRatio as calculated w/out change
                 */
                else {
                    sampleRateRatio = requestedRateRatio;
                    if (debugFlag) {
                        debugPrint("         adjustRate: " +
                                "    requestedRateRatio reached");
                    }
                    endOfRamp = true; // reached
                }
            }
            else // there is no difference between last set and requested rates
                return true;

            this.setSampleRate(sample, (float)sampleRateRatio);
        }
        else {
            // this is the first time thru with a rate change
            if (debugFlag) {
                debugPrint("                   adjustRate: " +
                        "last requested rateRatio not set yet " +
                        "so sampleRateRatio left unchanged");
            }
            this.setSampleRate(sample, (float)requestedRateRatio);
            endOfRamp = false; // target value not reached
        }
        return endOfRamp;
    } // adjustRate

    void setSampleRate(JSSample sample, JSAuralParameters attribs) {
// TODO:
    }

    // gain set at start sample time as well
    void setSampleGain(JSSample sample, JSAuralParameters attribs) {
/*******
        // take fields as already set in sample and updates gain
        // called after sample.render performed
        if (debugFlag)
            debugPrint("JSThread.setSampleGain()");
leftGain, rightGain
            if (debugFlag) {
                debugPrint("                           " +
                        "StereoGain during update " + leftGain +
                        ", " + rightGain);
                debugPrint("                           " +
                        "StereoDelay during update " + leftDelay +
                        ", " + rightDelay);
            }
        int   dataType = sample.getDataType();
        int   soundType = sample.getSoundType();
        boolean muted = sample.getMuteFlag();

        if (debugFlag)
            debugPrint("setStereoGain for sample "+sample+" " + leftGain +
                        ", " + rightGain);
        if (dataType == JSAuralParameters.STREAMING_AUDIO_DATA ||
            dataType == JSAuralParameters.BUFFERED_AUDIO_DATA  ) {
            thread.setSampleGain(sample, leftGain);
            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                thread.setSampleGain(
                      ((JSPositionalSample)sample).getSecondIndex(), rightGain);                thread.setSampleGain(
                        ((JSPositionalSample)sample).getReverbIndex(), reverbGain);
            }
        }
        // TODO: JavaSound does not support MIDI song panning yet
        else if (dataType == JSAuralParameters.STREAMING_MIDI_DATA ||

                 dataType == JSAuralParameters.BUFFERED_MIDI_DATA) {
            // Stereo samples not used for Midi Song playback
            thread.setSampleGain(sample, (leftGain+rightGain) );
     ******
            // -1.0 far left, 0.0 center, 1.0 far right
            position = (leftGain - rightGain) / (leftGain + rightGain);
            JSMidi.setSamplePan(sample, position);

            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                JSMidi.setSampleGain(
                      ((JSPositionalSample)sample).getSecondIndex(), rightGain);                JSMidi.setSampleGain(
                        ((JSPositionalSample)sample).getReverbIndex(), reverbGain);
            }
     ******
        }
        else {
            if (debugFlag)
                debugPrint( "JSThread: Internal Error setSampleGain dataType " +
                      dataType + " invalid");
            return;
        }
  *****
        // force specific gain
        // go ahead and set gain immediately
        this.setSampleGain(sample, scaleFactor);
        rampGain = false;  // disable ramping of gain
******/
    }

    void setSampleDelay(JSSample sample, JSAuralParameters attribs) { 
/******
        // take fields as already set in sample and updates delay
        // called after sample.render performed
        // adjust by attrib rolloff
                float delayTime = attribs.reverbDelay * attribs.rolloff;

            leftDelay = (int)(sample.leftDelay * attribs.rolloff);
            rightDelay = (int)(sample.rightDelay * attribs.rolloff);
leftDelay, rightDelay
        int   dataType = sample.getDataType();
        int   soundType = sample.getSoundType();
        if (debugFlag)
            debugPrint("setStereoDelay for sample "+sample+" " + leftDelay +
                        ", " + rightDelay);
        if (dataType == JSAuralParameters.STREAMING_AUDIO_DATA) {
            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                JSStream.setSampleDelay(
                         sample, leftDelay);
                JSStream.setSampleDelay(
                         ((JSPositionalSample)sample).getSecondIndex(), rightDelay);
            }
            else
               JSStream.setSampleDelay(sample, 0);
        }
        else if (dataType == JSAuralParameters.BUFFERED_AUDIO_DATA) {
            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                JSClip.setSampleDelay(
                         sample, leftDelay);
                JSClip.setSampleDelay(
                         ((JSPositionalSample)sample).getSecondIndex(), rightDelay);
            }
            else
                JSClip.setSampleDelay(sample, 0);
        }
        else if (dataType == JSAuralParameters.STREAMING_MIDI_DATA ||

                 dataType == JSAuralParameters.BUFFERED_MIDI_DATA) {
    ********
            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                JSMidi.setSampleDelay(
                         sample, leftDelay);
                JSMidi.setSampleDelay(
                         ((JSPositionalSample)sample).getSecondIndex(), rightDelay);
            }
            else
    ********
               JSMidi.setSampleDelay(sample, 0);
        }
        else {
            if (debugFlag)
                debugPrint( "JSThread: Internal Error setSampleDelay dataType " +
                  dataType + " invalid");
            return;
        }
******/
    }

    void setTargetGain(JSSample sample, float scaleFactor) {
/**********
// TODO: implement this
        // current gain is used as starting scalefactor for ramp
// TEMPORARY: for now just set gain
        this.setSampleGain(sample, scaleFactor);
        rampGain = false;
        rampGain = true;
        targetGain = scaleFactor;
        runMonitor(RUN, 0, null);
**********/
    }

    void setRate(JSSample sample, float rateScaleFactor) {
        // force specific rate
        // go ahead and set rate immediately
        // take fields as already set in sample and updates rate
        // called after sample.render performed
        this.setSampleRate(sample, rateScaleFactor);
        // disables rate from being gradually increased or decreased 
        // don't set global thread flat rampRate false just because
        // one sample's rate is set to a specific value.
        sample.setRampRateFlag(false);
    }

    void setTargetRate(JSSample sample, float rateScaleFactor) {
        // make gradual change in rate factors up or down to target rate
        sample.setRampRateFlag(true);
        sample.setTargetRateRatio(rateScaleFactor);
        rampRate = true;
        runMonitor(RUN, 0, null);
    }

// TODO: should have methods for delay and pan as well

    void setSampleGain(JSSample sample, float gain) {
/***********
// QUESTION: What needs to be synchronized???
        if (debugFlag)
            debugPrint("JSThread.setSampleGain for sample "+sample+" " + gain );
        int   dataType = sample.getDataType();
        int   soundType = sample.getSoundType();
        boolean muted = sample.getMuteFlag();
// TODO:
        if (dataType == JSAuralParameters.STREAMING_AUDIO_DATA)
{
            com.sun.j3d.audio.J3DHaeStream.setSampleGain(index, gain);
        }
        else if (dataType == JSAuralParameters.BUFFERED_AUDIO_DATA) {
            com.sun.j3d.audio.J3DHaeClip.setSampleGain(index, gain);
        }
        else {
            // dataType==JSAuralParameters.STREAMING_MIDI_DATA 
            // dataType==JSAuralParameters.BUFFERED_MIDI_DATA
            com.sun.j3d.audio.J3DHaeMidi.setSampleGain(index, gain);
        }
***************/
    }

    void  setSampleRate(JSSample sample, float scaleFactor) {
/*********
// QUESTION: What needs to be synchronized???
        // TODO: use sample.rateRatio??
        if (debugFlag)
            debugPrint("JSThread.setSampleRate sample " +
                sample + ", scale factor = " + scaleFactor);
        int   dataType = sample.getDataType();
        int   soundType = sample.getSoundType();

// TODO:
        if (dataType == JSAuralParameters.STREAMING_AUDIO_DATA) {
            com.sun.j3d.audio.J3DHaeStream.scaleSampleRate(index, scaleFactor);
            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                com.sun.j3d.audio.J3DHaeStream.scaleSampleRate(
                      ((JSPositionalSample)sample).getSecondIndex(),
                      scaleFactor);
                com.sun.j3d.audio.J3DHaeStream.scaleSampleRate(
                      ((JSPositionalSample)sample).getReverbIndex(),
                      scaleFactor);
            }
        }
        else if (dataType == JSAuralParameters.BUFFERED_AUDIO_DATA) {
            com.sun.j3d.audio.J3DHaeClip.scaleSampleRate(index, scaleFactor);
            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                com.sun.j3d.audio.J3DHaeClip.scaleSampleRate(
                      ((JSPositionalSample)sample).getSecondIndex(),
                      scaleFactor);
                com.sun.j3d.audio.J3DHaeClip.scaleSampleRate(
                      ((JSPositionalSample)sample).getReverbIndex(),
                      scaleFactor);
            }
        }
        else if (dataType == JSAuralParameters.STREAMING_MIDI_DATA ||
                 dataType == JSAuralParameters.BUFFERED_MIDI_DATA) {
            com.sun.j3d.audio.J3DHaeMidi.scaleSampleRate(index, scaleFactor);
            // TODO: MIDI only supported for Background sounds
        }
***********/
        sample.setCurrentRateRatio(scaleFactor);
    }    

    boolean startSample(JSSample sample) {
/**********
// QUESTION: should this have a return values - error - or not??

        int returnValue = 0;
        AuralParameters attribs = audioEngine.getAuralParameters();
        int   soundType = sample.getSoundType();
        boolean muted = sample.getMuteFlag();
        int   dataType = sample.getDataType();
        int   loopCount = sample.getLoopCount();
        float leftGain = sample.leftGain;
        float rightGain = sample.rightGain;
        int   leftDelay = (int)(sample.leftDelay * attribs.rolloff);
        int   rightDelay = (int)(sample.rightDelay * attribs.rolloff);
        if (dataType == JSAuralParameters.STREAMING_AUDIO_DATA) {
            if (soundType == AudioDevice3D.BACKGROUND_SOUND) {
                returnValue = JSStream.startSample(sample,
                    loopCount, leftGain);
                if (debugFlag)
                    debugPrint("JSThread                    " +
                      "start stream backgroundSound with gain " + leftGain);
            }
            else { // soundType is POINT_SOUND or CONE_SOUND
                // start up main left and right channels for spatial rendered sound
                returnValue = JSStream.startSamples(sample,
                       ((JSPositionalSample)sample).getSecondIndex(),
                       loopCount, leftGain, rightGain, leftDelay, rightDelay);
                //
                // start up reverb channel w/out delay even if reverb not on now                //
                float reverbGain = 0.0f;
                if (!muted && auralParams.reverbFlag) {
                    reverbGain = sample.getGain() *
                           attribs.reflectionCoefficient;
                }
                int reverbRtrnVal = JSStream.startSample(
                       ((JSPositionalSample)sample).getReverbIndex(), loopCount,                               reverbGain);
                if (debugFlag)
                    debugPrint("JSThread                    " +
                      "start stream positionalSound with gain "+ leftGain +
                      ", " + rightGain);
            }
        }

        else if (dataType == JSAuralParameters.BUFFERED_AUDIO_DATA) {
            if (soundType == AudioDevice3D.BACKGROUND_SOUND) {
                returnValue = JSClip.startSample(sample,
                    loopCount, leftGain );
                if (debugFlag)
                    debugPrint("JSThread                    " +
                      "start buffer backgroundSound with gain " + leftGain);
            }
            else { // soundType is POINT_SOUND or CONE_SOUND
                // start up main left and right channels for spatial rendered sound
                returnValue = JSClip.startSamples(sample,
                        ((JSPositionalSample)sample).getSecondIndex(),
                        loopCount, leftGain, rightGain, leftDelay, rightDelay);
                //
                // start up reverb channel w/out delay even if reverb not on now                //
                float reverbGain = 0.0f;
                if (!muted && auralParams.reverbFlag) {
                    reverbGain = sample.getGain() *
                           attribs.reflectionCoefficient;
                }
                int reverbRtrnVal = JSClip.startSample(
                        ((JSPositionalSample)sample).getReverbIndex(),
                        loopCount, reverbGain);
 
                if (debugFlag)
                    debugPrint("JSThread                    " +
                      "start stream positionalSound with gain " + leftGain
                      + ", " + rightGain);
            }
        }
        else if (dataType == JSAuralParameters.STREAMING_MIDI_DATA ||
                 dataType == JSAuralParameters.BUFFERED_MIDI_DATA) {
            if (soundType == AudioDevice3D.BACKGROUND_SOUND) {
                returnValue = JSMidi.startSample(sample,
                    loopCount, leftGain);
                if (debugFlag)
                    debugPrint("JSThread                    " +
                      "start Midi backgroundSound with gain " + leftGain);
            }
            else { // soundType is POINT_SOUND or CONE_SOUND
                // start up main left and right channels for spatial rendered sound
                returnValue = JSMidi.startSamples(sample,
                       ((JSPositionalSample)sample).getSecondIndex(),
                       loopCount, leftGain, rightGain, leftDelay, rightDelay);
                *******
                // TODO: positional MIDI sounds not supported.
                //     The above startSamples really just start on sample
                //     Don't bother with reverb channel for now.
 
                //
                // start up reverb channel w/out delay even if reverb not on now                //
                float reverbGain = 0.0f;
                if (!muted && auralParams.reverbFlag) {
                    reverbGain = sample.getGain() *
                           attribs.reflectionCoefficient;
                }
                int reverbRtrnVal = JSMidi.startSample(
                       ((JSPositionalSample)sample).getReverbIndex(), loopCount,                               reverbGain);
                *******
                if (debugFlag)
                    debugPrint("JSThread                    " +
                      "start Midi positionalSound with gain "+ leftGain +
                      ", " + rightGain);
            }
        }

        else {
            if (debugFlag)
                debugPrint(
                    "JSThread: Internal Error startSample dataType " +
                    dataType + " invalid");
            return false;
        }
        // TODO: have to look at return values and conditionally return 'success'
**********/
        return true;
    }

    boolean stopSample(JSSample sample) {
/***********
// QUESTION: should this have a return values - error - or not??
        int   dataType = sample.getDataType();
        int   soundType = sample.getSoundType();

        int returnValue = 0;
        if (dataType == JSAuralParameters.STREAMING_AUDIO_DATA) {
            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                returnValue = JSStream.stopSamples(sample,
                         ((JSPositionalSample)sample).getSecondIndex());
                returnValue = JSStream.stopSample(
                         ((JSPositionalSample)sample).getReverbIndex());
            }
            else
                returnValue = JSStream.stopSample(sample);
        }
        else if (dataType == JSAuralParameters.BUFFERED_AUDIO_DATA) {
            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                returnValue = JSClip.stopSamples(sample,
                         ((JSPositionalSample)sample).getSecondIndex());
                returnValue = JSClip.stopSample(
                         ((JSPositionalSample)sample).getReverbIndex());
            }
            else
                returnValue = JSClip.stopSample(sample);
        }
        else if (dataType == JSAuralParameters.STREAMING_MIDI_DATA ||
                 dataType == JSAuralParameters.BUFFERED_MIDI_DATA) {

          *****
            // TODO: positional sounds NOT supported yet
            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                returnValue = JSMidi.stopSamples(sample,
                         ((JSPositionalSample)sample).getSecondIndex());
                returnValue = JSMidi.stopSample(
                         ((JSPositionalSample)sample).getReverbIndex());
            }
            else
          *****
                returnValue = JSMidi.stopSample(sample);
        }
        else {
            if (debugFlag)
                debugPrint( "JSThread: Internal Error stopSample dataType " +
                  dataType + " invalid");
            return -1;
        }

************/
        return true;
    }
  

    void pauseSample(JSSample sample) {
/**********
        int   dataType = sample.getDataType();
        int   soundType = sample.getSoundType();
        int returnValue = 0;
        if (dataType == JSAuralParameters.STREAMING_AUDIO_DATA) {
            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                returnValue = JSStream.pauseSamples(sample,
                         ((JSPositionalSample)sample).getSecondIndex());
                returnValue = JSStream.pauseSample(
                         ((JSPositionalSample)sample).getReverbIndex());
            }
            else
                returnValue = JSStream.pauseSample(sample);
        }
        else if (dataType == JSAuralParameters.BUFFERED_AUDIO_DATA) {
            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                returnValue = JSClip.pauseSamples(sample,
                         ((JSPositionalSample)sample).getSecondIndex());
                returnValue = JSClip.pauseSample(
                         ((JSPositionalSample)sample).getReverbIndex());
            }
            else
                returnValue = JSClip.pauseSample(sample);
        }
        else if (dataType == JSAuralParameters.STREAMING_MIDI_DATA ||

                 dataType == JSAuralParameters.BUFFERED_MIDI_DATA) {
            *******
            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                returnValue = JSMidi.pauseSamples(sample,
                         ((JSPositionalSample)sample).getSecondIndex());
                returnValue = JSMidi.pauseSample(
                         ((JSPositionalSample)sample).getReverbIndex());
            }
            else
            *****
                returnValue = JSMidi.pauseSample(sample);
        }
        else {
            if (debugFlag)
                debugPrint(
                  "JSThread: Internal Error pauseSample dataType " +
                  dataType + " invalid");
        }
        if (returnValue < 0) {
            if (debugFlag)
                debugPrint( "JSThread: Internal Error pauseSample " +
                    "for sample " + sample + " failed");
        }
// QUESTION: return value or not???
        return;
*************/
    }

    void unpauseSample(JSSample sample) {
/**************
        int   dataType = sample.getDataType();
        int   soundType = sample.getSoundType();
        int returnValue = 0;
        if (dataType == JSAuralParameters.STREAMING_AUDIO_DATA) {
            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                returnValue = JSStream.unpauseSamples(sample,
                      ((JSPositionalSample)sample).getSecondIndex());
                returnValue = JSStream.unpauseSample(
                      ((JSPositionalSample)sample).getReverbIndex());
            }   
            else
                returnValue = JSStream.unpauseSample(sample);
        }
        else if (dataType == JSAuralParameters.BUFFERED_AUDIO_DATA) {
            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                returnValue = JSClip.unpauseSamples(sample,
                      ((JSPositionalSample)sample).getSecondIndex());
                returnValue = JSClip.unpauseSample(
                      ((JSPositionalSample)sample).getReverbIndex());
            }   
            else
                returnValue = JSClip.unpauseSample(sample);
        }
        else if (dataType == JSAuralParameters.STREAMING_MIDI_DATA ||

                 dataType == JSAuralParameters.BUFFERED_MIDI_DATA) {
            *********
            // TODO: positional Midi sounds
            if ( (soundType == AudioDevice3D.CONE_SOUND) ||
                 (soundType == AudioDevice3D.POINT_SOUND) ) {
                returnValue = JSMidi.unpauseSamples(sample,
                      ((JSPositionalSample)sample).getSecondIndex());
                returnValue = JSMidi.unpauseSample(
                      ((JSPositionalSample)sample).getReverbIndex());
            }
            else
            *********
                returnValue = JSMidi.unpauseSample(sample);
        }
        else {
            if (debugFlag)
                debugPrint(
                      "JSThread: Internal Error unpauseSample dataType " +                      dataType + " invalid");
        }
        if (returnValue < 0) {
            if (debugFlag)
                debugPrint( "JSThread: Internal Error unpauseSample " +
                   "for sample " + sample + " failed");
 
        }
// QUESTION: return value or not???
        return;
*************/
    }

// TODO: 
    void muteSample(JSSample sample) {
        // is this already muted? if so don't do anytning

        // This determines if mute is done as a zero gain or
        // as a stop, advance restart...
    }

// TODO: 
    void unmuteSample(JSSample sample) {
        if (debugFlag)
            debugPrint( "JSThread.unmuteSample not implemented");
    }

    int startStreams() {
// QUESTION: return value or not???
        return 0;
    }
    int startStream() {
// QUESTION: return value or not???
        return 0;
    }
    int startClips() {
// QUESTION: return value or not???
        return 0;
    }
    int startClip() {
// QUESTION: return value or not???
        return 0;
    }

    /**
     * This initializes this thread.  Once this method returns, the thread is
     * ready to do work.
     */
    public void initialize() {
        super.initialize();
        // this.setPriority(Thread.MAX_PRIORITY);
        // TODO: init values of fields???
        if (debugFlag)
            debugPrint("JSThread.initialize()");
        // TODO: doesn't do anything yet
    }

    /**
     * Code to close the device
     * @return flag: true is closed sucessfully, false if error
     */  
    boolean close() {
        // TODO: for now do nothing
        return false;
    }

    public void shutdown() {
    }




    // default resource clean up method
    public void cleanup() {
        super.cleanup();
        if (debugFlag)
            debugPrint("JSThread.cleanup()");
    }
}
