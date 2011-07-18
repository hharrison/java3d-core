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
 * Java Sound Sample object
 *
 * IMPLEMENTATION NOTE: The JavaSoundMixer is incomplete and really needs
 * to be rewritten.
 */

package com.sun.j3d.audioengines.javasound;

import java.net.URL;
import java.io.InputStream;
import javax.media.j3d.*;
import javax.sound.sampled.*;
import com.sun.j3d.audioengines.*;

/**
 * The Sample Class extended for Java Sound Mixer specific audio device.
 */

class JSSample extends com.sun.j3d.audioengines.Sample
{
    /*
     * NOTE: for this device type there is exactly one sample associated
     *       with each sound.
     */

    /** 
     *  Sound Data Types
     *  
     *  Samples can be processed as streaming or buffered data.
     *  Fully spatializing sound sources may require data to be buffered.
     * 
     *  Sound data specified as Streaming is not copied by the AudioDevice
     *  driver implementation.  It is up the application to ensure that
     *  this data is continuously accessible during sound rendering.
     *  Futhermore, full sound spatialization may not be possible, for
     *  all AudioDevice implementations on unbuffered sound data.
     */ 
    static final int STREAMING_AUDIO_DATA = 1;
    /** 
     *  Sound data specified as Buffered is copied by the AudioDevice
     *  driver implementation.
     */ 
    static final int BUFFERED_AUDIO_DATA = 2;
    /** 
     *  MIDI data 
     *  TODO: differentiate between STREAMING and BUFFERED MIDI data
     *      right now all MIDI data is buffered
     */ 
    static final int STREAMING_MIDI_DATA = 3;
    static final int BUFFERED_MIDI_DATA = 3;
    static final int UNSUPPORTED_DATA_TYPE = -1;

    static final int NULL_SAMPLE = -1;

    /**
     *  sound data types: BUFFERED (cached) or STREAMING (non-cached)
     */  
    int   dataType = BUFFERED_AUDIO_DATA;

    JSChannel channel = null;

    /**
     *  Offset pointer within currently playing sample data
     */  
    long      dataOffset = 0;

    /*
     * Maintain continuously playing silent sound sources.
     */  
    long      timeDeactivated = 0;
    long      positionDeactivated   = 0;

    long      sampleLength = 0;
    long      loopStartOffset = 0; // for most this will be 0
    long      loopLength = 0;      // for most this is end sample - sampleLength
    long      attackLength = 0;    // portion of sample before loop section
    long      releaseLength = 0;   // portion of sample after loop section

    float     rateRatio = 1.0f;
    float     currentRateRatio = -1.0f; // last actual rate ratio send to device
    float     targetRateRatio = -1.0f;
    boolean   rampRateFlag = false;

    public JSSample() {
        super();
        if (debugFlag) 
            debugPrintln("JSSample constructor");
    }

    // the only public methods are those declared in the audioengines
    // package as public

    /*
     * This excutes code necessary to set current fields to their current
     * correct values before JavaSoundMixer either start or updates the
     * sample thru calls to JSThread.
     */
    public void render(int dirtyFlags, View view, AuralParameters attribs) {
        if (debugFlag)
            debugPrint("JSSample.render ");
        // if this is starting set gain, delay (for Pos), freq rate ...
        // TODO: NOT SURE - leaving this in for now
        float freqScaleFactor = attribs.frequencyScaleFactor;
        if (attribs != null) {
            if (freqScaleFactor <= 0.0f) {
                // TODO: Pause Sample
            }
            else
                rateRatio = currentRateRatio * freqScaleFactor;
        }
        else 
            rateRatio = currentRateRatio; 
    }

    /**
     * Clears/re-initialize fields associated with sample data for
     * this sound,
     * and frees any device specific data associated with this sample.
     */  
    public void clear() {
        super.clear();
        if (debugFlag)
            debugPrintln("JSSample.clear() entered"); 
        // TODO: unload sound data at device
//     null out samples element that points to this?
//     would this cause samples list size to shrink?
//     if sample elements are never freed then does this mean
//     a have a memory leak?
        dataType = UNSUPPORTED_DATA_TYPE;
        dataOffset = 0;
        timeDeactivated = 0;
        positionDeactivated = 0;
        sampleLength = 0;
        loopStartOffset = 0;
        loopLength = 0;
        attackLength = 0;
        releaseLength = 0;
        rateRatio = 1.0f;
        channel = null;
        if (debugFlag)
            debugPrintln("JSSample.clear() exited"); 
    }

    // @return error true if error occurred
    boolean load(MediaContainer soundData) {
        /**
         * Get the AudioInputStream first.
         * MediaContiner passed to method assumed to be a clone of the
         * application node with the query capability bits set on.
         */
        String path = soundData.getURLString();
        URL url = soundData.getURLObject();
        InputStream inputStream = soundData.getInputStream();
        boolean cacheFlag = soundData.getCacheEnable();
        AudioInputStream ais = null;
        DataLine dataLine = null;

        // TODO: How do we determine if the file is a MIDI file???
        //     for now set dataType to BUFFERED_ or STREAMING_AUDIO_DATA
        // used to test for ais instanceof AudioMidiInputStream ||
        //                  ais instanceof AudioRmfInputStream ) 
        //     then set dataType = JSSample.BUFFERED_MIDI_DATA;
        // QUESTION: can non-cached MIDI files ever be supported ?
        /****************
        // TODO: when we have a way to determine data type use code below 
        if (dataType==UNSUPPORTED_DATA_TYPE OR error_occurred)
            clearSound(index);
            if (debugFlag)
                debugPrintln("JavaSoundMixer.prepareSound get dataType failed");
            return true;
        }
        *****************/
        // ...for now just check cacheFlag
        if (cacheFlag)
            dataType = BUFFERED_AUDIO_DATA;
        else
            dataType = STREAMING_AUDIO_DATA;

        if ((url == null) && (inputStream == null) && (path == null)) {
            if (debugFlag) 
                debugPrint("JavaSoundMixer.loadSound null data - return error");
           return true;
        } 

        // get ais
        if (path != null)  {
            // generate url from string, and pass url to driver
            if (debugFlag) {
                debugPrint("JavaSoundMixer.loadSound with path = " + path);
            }
            try  {
                url = new URL(path);
            }
            catch (Exception e) {
                // do not throw an exception while rendering
                return true;
            }
        }

        // get DataLine channel based on data type
        if (dataType == BUFFERED_AUDIO_DATA) {
            if (debugFlag) 
                debugPrintln("JSSample.load dataType = BUFFERED ");
            channel = new JSClip();
            if (debugFlag) 
                debugPrintln(" calls JSClip.initAudioInputStream");
            if (url != null)
                ais = channel.initAudioInputStream(url, cacheFlag);
            else if (inputStream != null)
                ais = channel.initAudioInputStream(inputStream, cacheFlag);
            if (ais == null) {
                if (debugFlag)
                    debugPrintln("JavaSoundMixer.prepareSound " +
                             "initAudioInputStream() failed");
                return true;
            }
            if (debugFlag) 
                debugPrintln(" calls JSClip.initDataLine");
            dataLine = channel.initDataLine(ais);
        }
        else if (dataType == STREAMING_AUDIO_DATA) {
            if (debugFlag) 
                debugPrintln("JSSample.load dataType = STREAMING ");
            channel = new JSStream();
            if (debugFlag) 
                debugPrintln(" calls JSStream.initAudioInputStream");
            if (url != null)
                ais = channel.initAudioInputStream(url, cacheFlag);
            else if (inputStream != null)
                ais = channel.initAudioInputStream(inputStream, cacheFlag);
            if (ais == null) {
                if (debugFlag)
                    debugPrintln("JavaSoundMixer.prepareSound " +
                             "initAudioInputStream() failed");
                return true;
            }
            if (debugFlag) 
                debugPrintln(" calls JSStream.initDataLine");
            dataLine = channel.initDataLine(ais);
        }
        else {
            if (debugFlag)
                debugPrintln("JSSample.load doesn't support MIDI yet"); 
        } 
        if (dataLine == null) {
            if (debugFlag)
                debugPrint("JSSample.load initDataLine failed ");
            channel = null;
            return true;
        }
        duration = channel.getDuration();
        if (debugFlag)
            debugPrint("JSSample.load channel duration = " + duration);
        /*
         * Since no error occurred while loading, save all the characteristics
         * for the sound in the sample.
         */
        setDirtyFlags(0xFFFF);
        setSoundType(soundType);
        setSoundData(soundData);

        if (debugFlag)
            debugPrintln("JSSample.load returned without error");
        return false;
    }

    void reset() {
        if (debugFlag)
            debugPrint("JSSample.reset() exit");
        rateRatio = 1.0f;
    }

// TODO: NEED methods for any field accessed by both JSThread and 
//          JavaSoundMixer so that we can make these MT safe?? 
    /*
     * Process request for Filtering fields
     */  
    boolean  getFilterFlag() { 
        return false;
    }
    float  getFilterFreq() { 
        return -1.0f;
    }

    void  setCurrentRateRatio(float ratio) { 
        currentRateRatio = ratio;
    }

    float  getCurrentRateRatio() { 
        return currentRateRatio;
    }

    void  setTargetRateRatio(float ratio) { 
        targetRateRatio = ratio;
    }

    float  getTargetRateRatio() { 
        return targetRateRatio;
    }

    void  setRampRateFlag(boolean flag) { 
        rampRateFlag = flag;
    }

    boolean  getRampRateFlag() { 
        return rampRateFlag;
    }

    void  setDataType(int type) { 
        dataType = type;
    }

    int  getDataType() { 
        return dataType;
    }

}
