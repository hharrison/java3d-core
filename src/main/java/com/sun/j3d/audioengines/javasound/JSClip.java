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

import java.applet.*;
import java.util.*;
import java.lang.String;
import java.net.*;
import java.io.*;
import java.io.InputStream;
import javax.sound.sampled.*;
 
/**
 * The JSClip Class defines an audio output methods that call JavaSound
 * Hae mixer methods.
 */

class JSClip extends JSChannel {

    Clip line;

// TODO: separate left and right channel required until I write into
// stereo buffer!
    Clip    otherChannel = null;

// TODO: Reverb channel that is centered and not delayed is maintained separately
//     until a way to set stereo reverb send (panned and attenuated to give
//     the same affect) is implemented
    Clip    reverbChannel = null;


    /**
     * Create data line for outputting audio input stream.
     * for a stream that is a sourceDataline
     * @return true is successful in initiallizing DataLine
     */
    DataLine initDataLine(AudioInputStream ais) {
         if (debugFlag)
             debugPrintln("JSClip: initDataLine(" + ais + ")");

         try {
             if (debugFlag) 
                 debugPrintln("JSClip: loadSample - try getting new line ");
             /*
              * From the AudioInputStream fetch information about the format
              * of the audio data - including sampling frequency, number of
              * channels, size of samples,...
              */
             audioFormat = ais.getFormat();

	     /*
	      * we can't yet open the device for ALAW/ULAW playback,
	      * convert ALAW/ULAW to PCM
	      */
	     if ((audioFormat.getEncoding() == AudioFormat.Encoding.ULAW) ||
		 (audioFormat.getEncoding() == AudioFormat.Encoding.ALAW)) {

		 AudioFormat tmp =
		     new AudioFormat(
				     AudioFormat.Encoding.PCM_SIGNED, 
				     audioFormat.getSampleRate(),
				     audioFormat.getSampleSizeInBits() * 2,
				     audioFormat.getChannels(),
				     audioFormat.getFrameSize() * 2,
				     audioFormat.getFrameRate(),
				     true);
		 ais = AudioSystem.getAudioInputStream(tmp, ais);
		 audioFormat = tmp;
	     }

             /* 
              * ask JavaSound for outline with a format suitable for our
              * AudioInputStream.  In order to ask for a line, a Info object
              * with the desired properties must be constructed.
              * Clip is used for outputing buffered data.
              * We have to pass the line the AudioFormat object so it knows
              * format will be.
	      *
              * TODO: we could give JavaSound a hint about how big the
              * internal buffer for the line should be, rather than use the
              * default.
              */
             DataLine.Info info = new DataLine.Info(Clip.class,
                     audioFormat);
	     line = (Clip)AudioSystem.getLine(info);
/*****
// TODO: JSClip can't be a listener (do we need to do this in the thread?)
             if (debugFlag)
                 debugPrintln("JSClip: addLineListener for clip");
             line.addLineListener(this);  
******/

             if (debugFlag)
                 debugPrintln("JSClip: open sound Clip");

	     // Make line ready to receive data.
             line.open(ais);

	     // Line can now receive data but still needs to be
	     // activated (opened) so it will pass data on to the
	     // audio device. This is done at "startSample" time.
         }
         catch (Exception e) {
             if (debugFlag) {
                 debugPrint("JSClip: Internal Error loadSample ");
                 debugPrintln("get stream failed");
             }
	     e.printStackTrace();
             // TODO: clean up vector elements that were set up for
	     // failed sample
             return null;
         }
         return (DataLine)line;
     }  // initDataLine

     /**
      * Start TWO Samples
      *
      * used when two samples are associated with a single Point or Cone
      * sound.  This method handles starting both samples, rather than
      * forcing the caller to make two calls to startSample, so that the
      * actual Java Sound start methods called are as immediate (without
      * delay between as possible.
      */
     boolean startSamples(int loopCount, float leftGain, float rightGain, 
                              int leftDelay, int rightDelay) {
         // loop count is ignored for Stream and MIDI
         // TODO: loop count isn't implemented for MIDI yet

         // left and rightDelay parameters are in terms of Samples
         if (debugFlag) {
             debugPrint("JSClip: startSamples ");
             debugPrintln("start stream for Left called with ");
             debugPrintln("       gain = " + leftGain + 
                          " delay = " + leftDelay);
             debugPrintln("start stream for Right called with ");
             debugPrintln("       gain = " + rightGain + 
                          " delay = " + rightDelay);
         }

         // This is called assuming that the Stream is allocated for a
         // Positional sample, but if it is not then fall back to
         // starting the single sample associated with this Stream
         if (otherChannel == null || reverbChannel == null)
             startSample(loopCount, leftGain, leftDelay);

         /*
          * ais for Left and Right streams should be same so just get ais
          * left stream
          */
         if (ais == null) {
             if (debugFlag) {
                 debugPrint("JSClip: Internal Error startSamples: ");
                 debugPrintln("either left or right ais is null");
             }
             return false;
         }
         Clip leftLine;
         Clip rightLine;
         leftLine = line;
         rightLine = otherChannel;
// left line only for background sounds...
// TODO:
/***********
for now just care about the left
         if (leftLine == null || rightLine == null) {
             if (debugFlag) {
                 debugPrint("JSClip: startSamples Internal Error: ");
                 debugPrintln("either left or right line null");
             }
             return false;
         }
************/
 
         // we know that were processing TWO channels
         double ZERO_EPS = 0.0039;  // approx 1/256 - twice MIDI precision
         double leftVolume = (double)leftGain;
         double rightVolume = (double)rightGain;

// TODO: if not reading/writing done for Clips then I can't do
//     stereo trick (reading mono file and write to stereo buffer)
         // Save time sound started, only in left 
         startTime = System.currentTimeMillis();
         if (debugFlag)
             debugPrintln("*****start Stream with new start time " + 
                         startTime);
         try {
             // QUESTION: Offset clip is done how???
/*******
// TODO:
offset delayed sound
set volume
set pan??
set reverb
         boolean reverbLeft = false; // off; reverb has it own channel
         boolean reverbRight = reverbLeft;

                 if (leftDelay < rightDelay) { 
XXXX                 audioLeftStream.start(leftVolume, panLeft, reverbLeft);
XXXX                 audioRightStream.start(rightVolume, panRight, reverbRight);
                 }
                 else {
XXXX                 audioRightStream.start(rightVolume, panRight, reverbRight);
XXXX                 audioLeftStream.start(leftVolume, panLeft, reverbLeft);
                 }
******/
	     line.setLoopPoints(0, -1);	// Loop the entire sound sample
             line.loop(loopCount);	// plays clip loopCount + 1 times
	     line.start();		// start the sound
         }
         catch (Exception e) {
	     if (debugFlag) {
		 debugPrint("JSClip: startSamples ");
		 debugPrintln("audioInputStream.read failed");
	     }
	     e.printStackTrace();
	     startTime = 0;
	     return false;
         }

         if (debugFlag) 
             debugPrintln("JSClip: startSamples returns");
         return true;
     }  // end of startSamples


     /*
      * This method is called specifically for BackgroundSounds.
      * There is exactly ONE sample (mono or stereo) associated with
      * this type of sound.  Consequently, delay is not applicable.
      * Since the sound has no auralAttributes applied to it reverb 
      * is not applied to the sample.
      */
     boolean  startSample(int loopCount, float gain, int delay) {
	 /*
         if (debugFlag) {
             debugPrint("JSClip: startSample ");
             debugPrintln("start stream called with ");
             debugPrintln("       gain = " + gain + ", delay is zero");
         }

	 // Since only one sample is processed in startSample, just call
	 // this more general method passing duplicate information
	 // We don't really want to do this in the long term.
         return startSamples(loopCount, gain, gain, 0, 0);
	 */

	 // TODO: The following is temporary until we have fully
	 // functional startSample and startSamples methods
	 if (debugFlag)
	     debugPrintln("JSClip.startSample(): starting sound Clip");
	 line.setFramePosition(0); // Start playing from the beginning
	 line.setLoopPoints(0, -1); // Loop the entire sound sample
	 line.loop(loopCount);
	 line.start();
	 return true;
     }  // end of start (single) Sample

     int   stopSample() {
         // This will tell thread to stop reading and writing
         // reload with old URL - reloadSample()???

	 if (debugFlag)
	     debugPrintln("JSClip.stopSample(): stopping sound Clip");
         line.stop();

         startTime = 0;
         return 0;
     }

     int   stopSamples() {
         // This will tell thread to stop reading and writing
         // TODO: For muting, stop sound but don't clear startTime...
         // QUESTION: what does it mean for replaying that .stop "frees memory"

 //        reloadSample
	 // QUESTION: set stop state WHERE??!!

	 if (debugFlag)
	     debugPrintln("JSClip.stopSample(): stopping sound Clip");
         line.stop();

         startTime = 0;
         return 0;
     }

     /*
      * called by LineListener class
      */
     public void update(LineEvent event) {
         if (event.getType().equals(LineEvent.Type.STOP)) {
             line.close(); // really a stop??
         }
         else if (event.getType().equals(LineEvent.Type.CLOSE)) {
             // this forces a system exit in example code
             // TODO: what should be done to close line
             if (debugFlag)
                 debugPrint("JSClip.update(CLOSE) entered ");
         }
     }

}
