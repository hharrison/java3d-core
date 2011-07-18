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
 * IMPLEMENTATION NOTE: The JavaSoundMixer is incomplete and really needs
 * to be rewritten.
 */

package com.sun.j3d.audioengines.javasound;

// import java.applet.*;
import java.util.*;
import java.lang.String;
import java.net.*;
import java.io.*;
import java.io.InputStream;
// import sun.applet.*;
import javax.sound.sampled.*;
import com.sun.j3d.audioengines.*;
// import javax.media.j3d.*;

/**
 * The JSChannel Class defines an audio output methods that call JavaSound
 * API methods common for all data line types: streams, clip and MIDI lines.
 */

class JSChannel {

    AudioInputStream  ais   = null;
    long              startTime = 0;
    URL               url = null;
    InputStream       inputStream = null;
    AudioFormat       audioFormat = null;
    // WORKAROUND for (possibly old) bug in JavaSound
    //     JavaSound has left and right flipped
    // TODO: verify whether this is still true
    static double panLeft      =  1.0;
    static double panRight     = -1.0;
    float rateInHz = 0.0f;

    /**
     * Debug print mechanism for Sound nodes
     */ 
    static final boolean debugFlag = false;

    static void debugPrint(String message) {
        if (debugFlag)
            System.out.print(message);
    }

    static void debugPrintln(String message) {
        if (debugFlag)
            System.out.println(message);
    }


    /**
     * Code to initialize the device
     * @return flag: true is initialized sucessfully, false if error
     */
    boolean initialize() {
        // for now do nothing
        return true;
    }

    /**
     * @return reference to newly created AudioInputStream
     */
    AudioInputStream initAudioInputStream(InputStream inputStream, boolean cacheFlag) {
         ais = null;
         if (inputStream == null) {
             if (debugFlag) {
                 debugPrint("JSChannel: Internal Error initAudioInputStream ");
                 debugPrintln("input stream given is null");
             }   
             this.inputStream = null;
             return null;
         }
         try {
             if (debugFlag)
                 debugPrintln("JSChannel: initAudioInputStream - try getting stream ");
             // open the sound data as an 'audio input stream'
             // and read the header information at the start of the data.
             ais = AudioSystem.getAudioInputStream(inputStream);
             // add this new stream to vector list of streams
         }
         catch (Exception e) {
             if (debugFlag) {
                 debugPrint("JSChannel: Internal Error initAudioInputStream ");
                 debugPrintln("get stream failed");
             }
	     e.printStackTrace();
             this.inputStream = null;
             return null;
         }
         // success, so save new inputStream and nullify url field
         this.inputStream = inputStream;
         url = null;
/******
// QUESTION: HOW do I figure out the data type of the file/url/inputStream????
         if (ais instanceof AudioMidiInputStream ||
             ais instanceof AudioRmfInputStream ) 
             // QUESTION: can non-cached MIDI files ever be supported ?
*******/
         return ais;
    }  // initAudioInputStream


    /**
     * @return reference to newly created AudioInputStream
     */
    AudioInputStream initAudioInputStream(URL path, boolean cacheFlag) {
         ais = null;
         if (path == null) {
             if (debugFlag) {
                 debugPrint("JSChannel: Internal Error initAudioInputStream ");
                 debugPrintln("URL given is null");
             }   
             this.url = null;
             return null;
         }
         try {
             if (debugFlag)
                 debugPrintln("JSChannel: initAudioInputStream - try getting stream ");
             ais = AudioSystem.getAudioInputStream(path.openStream());
         }
         catch (Exception e) {
             if (debugFlag) {
                 debugPrint("JSChannel: Internal Error initAudioInputStream ");
                 debugPrintln("get stream failed");
             }
	     e.printStackTrace();
             this.url = null;
             return null;
         }
         // success, so save new url path and nullify input stream field
         this.url = path;
         inputStream = null;
         return ais;
     }  // initAudioInputStream
 

    AudioInputStream reinitAudioInputStream(URL path) {
/*****
         if (path == null) {
             if (debugFlag) {
                 debugPrint("JSChannel: Internal Error reinitAudioInputStream ");
                 debugPrintln("URL given is null");
             }   
             return null;
         }
         try {
             if (debugFlag)
                 debugPrintln("JSChannel: reinitAudioInputStream - try getting stream ");
             ais = AudioSystem.getAudioInputStream(path.openStream());
         }
         catch (Exception e) {
             if (debugFlag) {
                 debugPrint("JSChannel: Internal Error reinitAudioInputStream ");
                 debugPrintln("get stream failed");
             }   
	     e.printStackTrace();
             return null;
         }
         // Parametes stay the same except for start time which is changed later
         return ais;
******/
         return null; // TODO: implement this

     }  // reinitAudioInputStream

    AudioInputStream reinitAudioInputStream(InputStream inputStream) {
/******
         AudioInputStream ais;
         if (inputStream == null) {
             if (debugFlag) {
                 debugPrint("JSChannel: Internal Error reinitAudioInputStream ");
                 debugPrintln("InputStream given is null");
             }   
             return null;
         }
         try {
// Couldn't get this method to work!!!
             if (debugFlag)
                 debugPrintln("JSChannel: reintAudioContainer - try closing stream ");
             inputStream.close();

             if (debugFlag)
                 debugPrintln("JSChannel: reinitAudioInputStream - try getting stream ");
             ais = AudioSystem.getAudioInputStream(inputStream);
         }
         catch (Exception e) {
             if (debugFlag) {
                 debugPrint("JSChannel: Internal Error reinitAudioInputStream ");
                 debugPrintln("get stream failed");
             }   
	     e.printStackTrace();
             return null;
         }
         // Parametes stay the same except for start time which is changed later
         return ais;  // >=0 if everythings OK
**************/
         return null;  // TODO: implement this

     }  // reinitAudioInputStream


     DataLine initDataLine(AudioInputStream ais) {
         if (debugFlag) {
             debugPrintln("JSChannel: initDataLine(" + ais + ")");
             debugPrintln("           must be overridden");
         }
         return null;
     }

     long  getDuration() {
         // TODO: how should this really be done??
         if (debugFlag)
             debugPrintln("JSChannel:getDuration");

         if (ais == null || audioFormat == null ) { 
             if (debugFlag) 
                 debugPrintln("JSChannel: Internal Error getDuration"); 
             return (long)Sample.DURATION_UNKNOWN;
         } 
         // Otherwise we'll assume that we can calculate this duration

         // get "duration" of audio stream (wave file)
         // TODO: For True STREAMing audio the size is unknown...
         long numFrames = ais.getFrameLength();
         if (debugFlag)
             debugPrintln("           frame length = " + numFrames);
         if (numFrames <= 0)
             return (long)Sample.DURATION_UNKNOWN;

         float rateInFrames = audioFormat.getFrameRate();
         rateInHz = audioFormat.getSampleRate();
         if (debugFlag)
             debugPrintln("           rate in Frames = " + rateInFrames);
         if (numFrames <= 0)
             return (long)Sample.DURATION_UNKNOWN;
         long duration = (long)((float)numFrames/rateInFrames); 
         if (debugFlag)
             debugPrintln("           duration(based on ais) = " + duration);
         return duration;
     }

     /**
      * Start TWO Samples
      */
     boolean  startSamples(int loopCount, float leftGain, float rightGain, 
                              int leftDelay, int rightDelay) {
         if (debugFlag)
             debugPrint("JSChannel: startSamples must be overridden");
         return false;
     } // end of start Samples

     /*
      * Starts a Sample
      */
     boolean  startSample(int loopCount, float gain, int delay) {
         if (debugFlag)
             debugPrint("JSChannel: startSample must be overridden");
         return false;
     }  // end of start (single) Sample

     int   stopSample() {
// This will tell thread to stop reading and writing
         // reload with old URL
         // reloadSample
         if (debugFlag)
             debugPrint("JSChannel: stopSample must be overridden");
         startTime = 0;
         return 0;
     }

     int   stopSamples() {
// This will tell thread to stop reading and writing
         // TODO: For muting, stop sound but don't clear startTime...
         // QUESTION: what does it mean for replaying that .stop "frees memory"
         if (debugFlag)
             debugPrint("JSChannel: stopSample must be overridden");
//        reloadSample

         startTime = 0;
         return 0;
     }

     void  setSampleGain(float gain) {
// TODO: Must be done in thread
         if (debugFlag)
             debugPrint("JSChannel: setSampleGain must be overridden");
     }

     void  setSampleDelay(int delay) {
         if (debugFlag)
             debugPrint("JSChannel: setSampleDelay must be overridden");
         /*
          * null method
          */
         // dynamic changes to sample delay while playing is not implemented
     }

     void  setSampleReverb(int type, boolean on) {
         if (debugFlag)
             debugPrint("JSChannel: setSampleReverb must be overridden");
     }

     void  setSampleRate() {
         if (debugFlag)
             debugPrint("JSChannel: setSampleRate must be overridden");
     }
     void  scaleSampleRate(float scaleFactor) {
         /**
          * Change rate for Doppler affect or pitch shifting.
          * Engine maximum sample rate is 48kHz so clamp to that 
          * max value.
          */
         if (debugFlag)  
             debugPrintln("JSChannel: scaleSampleRate");
         if (ais == null) {
             if (debugFlag) {
                 debugPrint("JSChannel: Internal Error scaleSampleRate: ");
                 debugPrintln("ais is null");
             }
             return;
         }
 
         AudioFormat audioFormat = ais.getFormat();
         float rate = audioFormat.getSampleRate();

         double newRate = rate * scaleFactor;
         if (newRate > 48000.0)  // clamp to 48K max
             newRate = 48000.0;
/****
// NOTE: This doesn't work...
///          audioStream.setSampleRate(newRate);

// need to set FloatControl.Type(SAMPLE_RATE) to new value somehow...

         if (debugFlag) {
             debugPrintln("JSChannel: scaleSampleRate: new rate = " + 
                     rate * scaleFactor);
             debugPrintln("              >>>>>>>>>>>>>>>  using scaleFactor = " +
                     scaleFactor);
         }
****/
     }

     int  pauseSamples() {
         /**
          * Pause playing samples
          */
// TODO: Notify thread
         return 0;
     }

     int  pauseSample() {
         /** 
          * Pause playing a sample
          */ 
// TODO: Notify thread
         return 0;
     }   

     int  unpauseSamples() { 
         /**
          * Resume playing samples
          */
// TODO: Notify thread
         return 0;
     }

     int  unpauseSample() { 
         /** 
          * Resume playing a sample
          */ 
// TODO: Notify thread
         return 0;
     }

     void  setSampleFiltering(boolean filterFlag, float cutoffFreq) {
         /**
          * Set or clear low-pass filtering
          */
/****
// QUESTION: how will this be done if data is written out one channel/sample at
       a time??
****/
         // QUESTION: should filtering of Midi data be performed?
//          ais.setFiltering(filterFlag, cutoffFreq);
     }

}
