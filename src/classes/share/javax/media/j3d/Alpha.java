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

package javax.media.j3d;


/**
 * The alpha NodeComponent object provides common methods for
 * converting a time value into an alpha value (a value in the range 0
 * to 1).  The Alpha object is effectively a function of time that
 * generates alpha values in the range [0,1] when sampled: f(t) =
 * [0,1].  A primary use of the Alpha object is to provide alpha
 * values for Interpolator behaviors.  The function f(t) and the
 * characteristics of the Alpha object are determined by
 * user-definable parameters:
 *
 * <p>
 * <ul>
 *
 * <code>loopCount</code> -- This is the number of times to run this
 * Alpha; a value of -1 specifies that the Alpha loops
 * indefinitely.<p>
 *
 * <code>triggerTime</code> -- This is the time in milliseconds since
 * the start time that this object first triggers.  If (startTime +
 * triggerTime >= currentTime) then the Alpha object starts running.<p>
 *
 * <code>phaseDelayDuration</code> -- This is an additional number of
 * milliseconds to wait after triggerTime before actually starting
 * this Alpha.<p>
 *
 * <code>mode</code> -- This can be set to INCREASING_ENABLE,
 * DECREASING_ENABLE, or the Or'ed value of the two.
 * INCREASING_ENABLE activates the increasing Alpha parameters listed
 * below; DECREASING_ENABLE activates the decreasing Alpha parameters
 * listed below.<p>
 *
 * </ul> Increasing Alpha parameters:<p> <ul>
 *
 * <code>increasingAlphaDuration</code> -- This is the period of time
 * during which Alpha goes from zero to one. <p>
 *
 * <code>increasingAlphaRampDuration</code> -- This is the period of
 * time during which the Alpha step size increases at the beginning of
 * the increasingAlphaDuration and, correspondingly, decreases at the
 * end of the increasingAlphaDuration.  This parameter is clamped to
 * half of increasingAlphaDuration.  When this parameter is non-zero,
 * one gets constant acceleration while it is in effect; constant
 * positive acceleration at the beginning of the ramp and constant
 * negative acceleration at the end of the ramp.  If this parameter is
 * zero, then the effective velocity of the Alpha value is constant
 * and the acceleration is zero (ie, a linearly increasing alpha
 * ramp).<p>
 *
 * <code>alphaAtOneDuration</code> -- This is the period of time that
 * Alpha stays at one.<p> </ul> Decreasing Alpha parameters:<p> <ul>
 *
 * <code>decreasingAlphaDuration</code> -- This is the period of time
 * during which Alpha goes from one to zero.<p>
 *
 * <code>decreasingAlphaRampDuration</code> -- This is the period of
 * time during which the Alpha step size increases at the beginning of
 * the decreasingAlphaDuration and, correspondingly, decreases at the
 * end of the decreasingAlphaDuration.  This parameter is clamped to
 * half of decreasingAlphaDuration.  When this parameter is non-zero,
 * one gets constant acceleration while it is in effect; constant
 * positive acceleration at the beginning of the ramp and constant
 * negative acceleration at the end of the ramp.  If this parameter is
 * zero, the effective velocity of the Alpha value is constant and the
 * acceleration is zero (i.e., a linearly-decreasing alpha ramp).<p>
 *
 * <code>alphaAtZeroDuration</code> -- This is the period of time that
 * Alpha stays at zero.
 *
 * </ul>
 *
 * @see Interpolator
 */

public class Alpha extends NodeComponent {

    // loopCount <  -1 --> reserved
    // loopCount == -1 --> repeat forever
    // loopCount >=  0 --> repeat count
    private int loopCount;

    /**
     * Specifies that the increasing component of the alpha is used.
     */
    public static final int INCREASING_ENABLE = 1;

    /**
     * Specifies that the decreasing component of the alpha is used
     */
    public static final int DECREASING_ENABLE = 2;

    /**
     * This alpha's mode, specifies whether to process
     * increasing and decreasing alphas.
     */
    private int mode;

    private float triggerTime;
    private float phaseDelay;
    private float increasingAlpha;
    private long increasingAlphaRamp;
    private float incAlphaRampInternal;
    private float alphaAtOne;
    private float decreasingAlpha;
    private long decreasingAlphaRamp;
    private float decAlphaRampInternal;
    private float alphaAtZero;

    // For pausing and resuming Alpha
    private long pauseTime = 0L;
    private boolean paused = false;

    // Stop time gets used only for loopCount > 0
    private float stopTime;

    // Start time in milliseconds
    private long startTime = MasterControl.systemStartTime;

    /**
     * Constructs an Alpha object with default parameters.  The default
     * values are as follows:
     * <ul>
     * loopCount			: -1<br>
     * mode				: INCREASING_ENABLE<br>
     * startTime			: system start time<br>
     * triggerTime			: 0<br>
     * phaseDelayDuration		: 0<br>
     * increasingAlphaDuration		: 1000<br>
     * increasingAlphaRampDuration	: 0<br>
     * alphaAtOneDuration		: 0<br>
     * decreasingAlphaDuration		: 0<br>
     * decreasingAlphaRampDuration	: 0<br>
     * alphaAtZeroDuration		: 0<br>
     * isPaused				: false<br>
     * pauseTime			: 0<br>
     * </ul>
     */
    public Alpha() {
        loopCount = -1;
        mode = INCREASING_ENABLE;
        increasingAlpha = 1.0f;          // converted to seconds internally 
	/*
	// Java initialize them to zero by default
        triggerTime = 0L;
        phaseDelay = 0.0f;
        increasingAlphaRamp = 0.0f;
        alphaAtOne = 0.0f;
        decreasingAlpha = 0.0f;
        decreasingAlphaRamp = 0.0f;
        alphaAtZero = 0.0f;
	*/
    }


    /**
     * This constructor takes all of the Alpha user-definable parameters.
     * @param loopCount number of times to run this alpha; a value
     * of -1 specifies that the alpha loops indefinitely
     * @param mode indicates whether the increasing alpha parameters or
     * the decreasing alpha parameters or both are active.  This parameter
     * accepts the following values, INCREASING_ENABLE or
     * DECREASING_ENABLE, which may be ORed together to specify
     * that both are active.
     * The increasing alpha parameters are increasingAlphaDuration,
     * increasingAlphaRampDuration, and alphaAtOneDuration.
     * The decreasing alpha parameters are decreasingAlphaDuration,
     * decreasingAlphaRampDuration, and alphaAtZeroDuration.
     * @param triggerTime time in milliseconds since the start time
     * that this object first triggers
     * @param phaseDelayDuration number of milliseconds to wait after
     * triggerTime before actually starting this alpha
     * @param increasingAlphaDuration period of time during which alpha goes
     * from zero to one
     * @param increasingAlphaRampDuration period of time during which
     * the alpha step size increases at the beginning of the
     * increasingAlphaDuration and, correspondingly, decreases at the end
     * of the increasingAlphaDuration. This value is clamped to half of
     * increasingAlphaDuration. NOTE: a value of zero means that the alpha
     * step size remains constant during the entire increasingAlphaDuration.
     * @param alphaAtOneDuration period of time that alpha stays at one
     * @param decreasingAlphaDuration period of time during which alpha goes
     * from one to zero
     * @param decreasingAlphaRampDuration period of time during which
     * the alpha step size increases at the beginning of the
     * decreasingAlphaDuration and, correspondingly, decreases at the end
     * of the decreasingAlphaDuration. This value is clamped to half of
     * decreasingAlphaDuration. NOTE: a value of zero means that the alpha
     * step size remains constant during the entire decreasingAlphaDuration.
     * @param alphaAtZeroDuration period of time that alpha stays at zero
     */
    public Alpha(int loopCount, int mode,
		 long triggerTime, long phaseDelayDuration,
		 long increasingAlphaDuration, 
		 long increasingAlphaRampDuration,
		 long alphaAtOneDuration,
		 long decreasingAlphaDuration, 
		 long decreasingAlphaRampDuration,
		 long alphaAtZeroDuration) {

	this.loopCount = loopCount;
	this.mode = mode;
	this.triggerTime = (float) triggerTime * .001f;
	phaseDelay = (float) phaseDelayDuration * .001f;

	increasingAlpha = (float) increasingAlphaDuration * .001f;
	alphaAtOne = (float)alphaAtOneDuration * .001f;
	increasingAlphaRamp = increasingAlphaRampDuration;
	incAlphaRampInternal = increasingAlphaRampDuration * .001f;
	if (incAlphaRampInternal > (0.5f * increasingAlpha)) {
	    incAlphaRampInternal = 0.5f * increasingAlpha;
	}

	decreasingAlpha = (float)decreasingAlphaDuration * .001f;
	alphaAtZero = (float)alphaAtZeroDuration * .001f;
	decreasingAlphaRamp = decreasingAlphaRampDuration;
	decAlphaRampInternal = decreasingAlphaRampDuration * .001f;
	if (decAlphaRampInternal > (0.5f * decreasingAlpha)) {
	    decAlphaRampInternal = 0.5f * decreasingAlpha;
	}
	computeStopTime();
    }


    /**
     * Constructs a new Alpha object that assumes that the mode is 
     * INCREASING_ENABLE.  
     *
     * @param loopCount number of times to run this alpha; a value
     * of -1 specifies that the alpha loops indefinitely.
     * @param triggerTime time in milliseconds since the start time
     * that this object first triggers
     * @param phaseDelayDuration number of milliseconds to wait after
     * triggerTime before actually starting this alpha
     * @param increasingAlphaDuration period of time during which alpha goes
     * from zero to one
     * @param increasingAlphaRampDuration period of time during which
     * the alpha step size increases at the beginning of the
     * increasingAlphaDuration and, correspondingly, decreases at the end
     * of the increasingAlphaDuration. This value is clamped to half of
     * increasingAlphaDuration. NOTE: a value of zero means that the alpha
     * step size remains constant during the entire increasingAlphaDuration.
     * @param alphaAtOneDuration period of time that alpha stays at one
     */

    public Alpha(int loopCount,
		 long triggerTime, long phaseDelayDuration,
		 long increasingAlphaDuration,
		 long increasingAlphaRampDuration,
		 long alphaAtOneDuration) {
	this(loopCount, INCREASING_ENABLE, 
	     triggerTime, phaseDelayDuration,
	     increasingAlphaDuration, increasingAlphaRampDuration,
	     alphaAtOneDuration, 0, 0, 0);
    }


    /**
      *  This constructor takes only the loopCount and increasingAlphaDuration
      *  as parameters and assigns the default values to all of the other
      *  parameters.  
      * @param loopCount number of times to run this alpha; a value
      * of -1 specifies that the alpha loops indefinitely
      * @param increasingAlphaDuration period of time during which alpha goes
      * from zero to one
      */ 
    public Alpha(int loopCount, long increasingAlphaDuration) {
        // defaults
        mode = INCREASING_ENABLE;
        increasingAlpha = (float) increasingAlphaDuration * .001f;
        this.loopCount = loopCount;

	if (loopCount >= 0) {
	    stopTime = loopCount*increasingAlpha;
	}
    }


    /**
     * Pauses this alpha object.  The current system time when this
     * method is called will be used in place of the actual current
     * time when calculating subsequent alpha values.  This has the
     * effect of freezing the interpolator at the time the method is
     * called.
     *
     * @since Java 3D 1.3
     */
    public void pause() {
	pause(J3dClock.currentTimeMillis());
    }

    /**
     * Pauses this alpha object as of the specified time.  The specified
     * time will be used in place of the actual current time when
     * calculating subsequent alpha values.  This has the effect of freezing
     * the interpolator at the specified time.  Note that specifying a
     * time in the future (that is, a time greater than
     * System.currentTimeMillis()) will cause the alpha to immediately
     * advance to that point before pausing.  Similarly, specifying a
     * time in the past (that is, a time less than
     * System.currentTimeMillis()) will cause the alpha to immediately
     * revert to that point before pausing.
     *
     * @param time the time at which to pause the alpha
     *
     * @exception IllegalArgumentException if time <= 0
     *
     * @since Java 3D 1.3
     */
    public void pause(long time) {
	if (time <= 0L) {
	    throw new IllegalArgumentException(J3dI18N.getString("Alpha0"));
	}

	paused = true;
	pauseTime = time;
	VirtualUniverse.mc.sendRunMessage(J3dThread.RENDER_THREAD);
    }

    /**
     * Resumes this alpha object.  If the alpha
     * object was paused, the difference between the current
     * time and the pause time will be used to adjust the startTime of
     * this alpha.  The equation is as follows:
     *
     * <ul>
     * <code>startTime += System.currentTimeMillis() - pauseTime</code>
     * </ul>
     *
     * Since the alpha object is no longer paused, this has the effect
     * of resuming the interpolator as of the current time.  If the
     * alpha object is not paused when this method is called, then this
     * method does nothing--the start time is not adjusted in this case.
     *
     * @since Java 3D 1.3
     */
    public void resume() {
	resume(J3dClock.currentTimeMillis());
    }

    /**
     * Resumes this alpha object as of the specified time.  If the alpha
     * object was paused, the difference between the specified
     * time and the pause time will be used to adjust the startTime of
     * this alpha.  The equation is as follows:
     *
     * <ul><code>startTime += time - pauseTime</code></ul>
     *
     * Since the alpha object is no longer paused, this has the effect
     * of resuming the interpolator as of the specified time.  If the
     * alpha object is not paused when this method is called, then this
     * method does nothing--the start time is not adjusted in this case.
     *
     * @param time the time at which to resume the alpha
     *
     * @exception IllegalArgumentException if time <= 0
     *
     * @since Java 3D 1.3
     */
    public void resume(long time) {
	if (time <= 0L) {
	    throw new IllegalArgumentException(J3dI18N.getString("Alpha0"));
	}

	if (paused) {
	    long newStartTime = startTime + time - pauseTime;
	    paused = false;
	    pauseTime = 0L;
	    setStartTime(newStartTime);
	}
    }

    /**
     * Returns true if this alpha object is paused.
     * @return true if this alpha object is paused, false otherwise
     *
     * @since Java 3D 1.3
     */
    public boolean isPaused() {
	return paused;
    }

    /**
     * Returns the time at which this alpha was paused.
     * @return the pause time; returns 0 if this alpha is not paused
     *
     * @since Java 3D 1.3
     */
    public long getPauseTime() {
	return pauseTime;
    }


    /**
     * This method returns a value between 0.0 and 1.0 inclusive,
     * based on the current time and the time-to-alpha parameters
     * established for this alpha.  If this alpha object is paused,
     * the value will be based on the pause time rather than the
     * current time.
     * This method will return the starting alpha value if the alpha
     * has not yet started (that is, if the current time is less
     * than startTime + triggerTime + phaseDelayDuration). This
     * method will return the ending alpha value if the alpha has
     * finished (that is, if the loop count has expired).
     *
     * @return a value between 0.0 and 1.0 based on the current time
     */
    public float value() {
	long currentTime = paused ? pauseTime : J3dClock.currentTimeMillis();
	return this.value(currentTime);
    }

    /**
     * This method returns a value between 0.0 and 1.0 inclusive,
     * based on the specified time and the time-to-alpha parameters
     * established for this alpha.
     * This method will return the starting alpha value if the alpha
     * has not yet started (that is, if the specified time is less
     * than startTime + triggerTime + phaseDelayDuration). This
     * method will return the ending alpha value if the alpha has
     * finished (that is, if the loop count has expired).
     *
     * @param atTime The time for which we wish to compute alpha
     * @return a value between 0.0 and 1.0 based on the specified time
     */
    public float value(long atTime) {
	float interpolatorTime
	  = (float)(atTime  - startTime) * .001f; // startTime is in millisec
	float alpha, a1, a2, dt, alphaRampDuration;

	//	System.out.println("alpha mode: " + mode);

	// If non-looping and before start
	//	if ((loopCount != -1) &&
	//	    interpolatorTime <= ( triggerTime +  phaseDelay)) {
	//	
	//	    if (( mode & INCREASING_ENABLE ) == 0 &&
	//		( mode & DECREASING_ENABLE) != 0)
	//		alpha = 1.0f;
	//	    else
	//		alpha = 0.0f;
	//	    return alpha;
	//	}
	
	
	//  Case of {constantly} moving forward, snap back, forward again
	if (( mode & INCREASING_ENABLE ) != 0 &&
	    ( mode & DECREASING_ENABLE) == 0) {

               if(interpolatorTime <= (triggerTime + phaseDelay))
                      return 0.0f;

               if((loopCount != -1) && (interpolatorTime >= stopTime))
                      return 1.0f;

	    //  Constant velocity case
	    if (incAlphaRampInternal == 0.0f) {

		alpha = mfmod((interpolatorTime -  triggerTime -  phaseDelay) +
			      6.0f*( increasingAlpha +  alphaAtOne),
			      (increasingAlpha + alphaAtOne))/ increasingAlpha;

		if ( alpha > 1.0f)  alpha = 1.0f;
		return alpha;
	    }
	
	    //  Ramped velocity case
	    alphaRampDuration =  incAlphaRampInternal;

	    dt = mfmod((interpolatorTime -  triggerTime -  phaseDelay) +
		       6.0f*( increasingAlpha +  alphaAtOne),
		       ( increasingAlpha +  alphaAtOne));
	    if (dt >=  increasingAlpha) {  alpha = 1.0f; return alpha; }
	
		// Original equation kept to help understand
		// computation logic - simplification saves
 		// a multiply and an add
		// a1 = 1.0f/(alphaRampDuration*alphaRampDuration +
		//	   ( increasingAlpha - 2*alphaRampDuration)*
		//	   alphaRampDuration);

		a1 = 1.0f/(increasingAlpha * alphaRampDuration -
			alphaRampDuration * alphaRampDuration);
	
	    if (dt < alphaRampDuration) {
		alpha = 0.5f*a1*dt*dt;
	    } else if (dt <  increasingAlpha - alphaRampDuration) {
		alpha = 0.5f*a1*alphaRampDuration*
		    alphaRampDuration +
		    (dt - alphaRampDuration)*a1*
		    alphaRampDuration;
	    } else {
		alpha = a1*alphaRampDuration*alphaRampDuration +
		    ( increasingAlpha - 2.0f*alphaRampDuration)*a1*
		    alphaRampDuration -
		    0.5f*a1*( increasingAlpha - dt)*
		    ( increasingAlpha - dt);
	    }
	    return alpha;
	
	} else
	
	
	    // Case of {constantly} moving backward, snap forward, backward
	    // again
	    if (( mode & INCREASING_ENABLE ) == 0 &&
		( mode & DECREASING_ENABLE) != 0) {
	
		// If non-looping and past end
		//		if ((loopCount != -1)
		//		    && (interpolatorTime 
		//			>= (triggerTime + phaseDelay + decreasingAlpha))) {
		//		    alpha = 0.0f;
		//		    return alpha;
		//		}

                if(interpolatorTime <= (triggerTime + phaseDelay))
                      return 1.0f; 

                if((loopCount != -1) && (interpolatorTime >= stopTime) )
                      return 0.0f; 


	
		//  Constant velocity case
		if (decAlphaRampInternal == 0.0f) {
		    alpha = mfmod((interpolatorTime -  triggerTime -
				   phaseDelay) +
				  6.0f*( decreasingAlpha + alphaAtZero),
				  (decreasingAlpha + alphaAtZero))/ decreasingAlpha;
		    if ( alpha > 1.0f) {  alpha = 0.0f; return alpha; }
		    alpha = 1.0f -  alpha;
		    return alpha;
		}
	
		//  Ramped velocity case
		alphaRampDuration =  decAlphaRampInternal;
	
		dt = mfmod((interpolatorTime -  triggerTime -  phaseDelay) +
			   6.0f*( decreasingAlpha +  alphaAtZero),
			   ( decreasingAlpha +  alphaAtZero));
		if (dt >=  decreasingAlpha) {  alpha = 0.0f; return alpha; }
	
		// Original equation kept to help understand
		// computation logic - simplification saves
 		// a multiply and an add
		// a1 = 1.0f/(alphaRampDuration*alphaRampDuration +
		//	   ( decreasingAlpha - 2*alphaRampDuration)*
		//	   alphaRampDuration);

		a1 = 1.0f/(decreasingAlpha * alphaRampDuration -
			alphaRampDuration * alphaRampDuration);
	
		if (dt < alphaRampDuration) {
		    alpha = 0.5f*a1*dt*dt;
		} else if (dt <  decreasingAlpha - alphaRampDuration) {
		    alpha = 0.5f*a1*alphaRampDuration*
			alphaRampDuration +
			(dt - alphaRampDuration)*a1*
			alphaRampDuration;
		} else {
		    alpha = a1*alphaRampDuration*alphaRampDuration +
			( decreasingAlpha - 2.0f*alphaRampDuration)*a1*
			alphaRampDuration -
			0.5f*a1*( decreasingAlpha - dt)*
			( decreasingAlpha - dt);
		}
		alpha = 1.0f -  alpha;
		return alpha;
	
	    } else
	
	
		//  Case of {osscilating} increasing and decreasing alpha
		if (( mode & INCREASING_ENABLE) != 0 &&
		    ( mode & DECREASING_ENABLE) != 0) {
	
		    // If non-looping and past end
		  //		    if ((loopCount != -1) &&
		  //			(interpolatorTime >= 
		  //			 (triggerTime +  phaseDelay +  increasingAlpha +
		  //			  alphaAtOne +  decreasingAlpha))) {
		  //			alpha = 0.0f;
		  //			return alpha;
		  //  }


		    // If non-looping and past end, we always end up at zero since
		    // decreasing alpha has been requested.
		    if(interpolatorTime <= (triggerTime + phaseDelay))
                         return 0.0f;

		    if( (loopCount != -1) && (interpolatorTime >= stopTime))	
                         return 0.0f;

		    //  Constant velocity case
		    if (incAlphaRampInternal == 0.0f
			&& decAlphaRampInternal == 0.0f) {
			dt = mfmod(interpolatorTime - triggerTime - phaseDelay +
				   6.0f*(increasingAlpha + alphaAtOne +
					 decreasingAlpha + alphaAtZero),
				   increasingAlpha + alphaAtOne +
				   decreasingAlpha + alphaAtZero);
			alpha = dt / increasingAlpha;
			if ( alpha < 1.0f) return alpha;
			// sub all increasing alpha time
			dt -=  increasingAlpha;
			if (dt <  alphaAtOne) {  alpha = 1.0f; return alpha; }
			// sub out alpha @ 1 time
			dt -=  alphaAtOne;
			alpha = dt/ decreasingAlpha;
			if ( alpha < 1.0f)  alpha = 1.0f -  alpha;
			else  alpha = 0.0f;
			return alpha;
		    }
	
		    //  Ramped velocity case
		    alphaRampDuration =  incAlphaRampInternal;

                    // work around for bug 4308308
                    if (alphaRampDuration == 0.0f)
                        alphaRampDuration = .00001f;

		    dt = mfmod(interpolatorTime -  triggerTime -  phaseDelay +
			       6.0f*( increasingAlpha +  alphaAtOne +
				      decreasingAlpha +  alphaAtZero),
			       increasingAlpha +  alphaAtOne +
			       decreasingAlpha +  alphaAtZero);
		    if (dt <=  increasingAlpha) {

			// Original equation kept to help understand
			// computation logic - simplification saves
 			// a multiply and an add
			// a1 = 1.0f/(alphaRampDuration*alphaRampDuration +
			//	   ( increasingAlpha - 2*alphaRampDuration)*
			//	   alphaRampDuration);

			a1 = 1.0f/(increasingAlpha * alphaRampDuration -
				alphaRampDuration * alphaRampDuration);
	
			if (dt < alphaRampDuration) {
			    alpha = 0.5f*a1*dt*dt;
			} else if (dt <  increasingAlpha - alphaRampDuration) {
			    alpha = 0.5f*a1*alphaRampDuration*
				alphaRampDuration +
				(dt - alphaRampDuration)*a1*
				alphaRampDuration;
			} else {
			    alpha = a1*alphaRampDuration*alphaRampDuration+
				( increasingAlpha - 2.0f*alphaRampDuration)*a1*
				alphaRampDuration -
				0.5f*a1*( increasingAlpha - dt)*
				( increasingAlpha - dt);
			}
			return alpha;
		    }
		    else if (dt <=  increasingAlpha +  alphaAtOne) {
			alpha = 1.0f; return alpha;
		    }
		    else if (dt >=  increasingAlpha +  alphaAtOne +  decreasingAlpha) {
			alpha = 0.0f; return alpha;
		    }
		    else {
			dt -=  increasingAlpha + alphaAtOne;

			alphaRampDuration =  decAlphaRampInternal;

                        // work around for bug 4308308
                        if (alphaRampDuration == 0.0f)
                            alphaRampDuration = .00001f;

			// Original equation kept to help understand
			// computation logic - simplification saves
			// a multiply and an add
			// a1 = 1.0f/(alphaRampDuration*alphaRampDuration +
			//	   ( decreasingAlpha - 2*alphaRampDuration)*
			//	   alphaRampDuration);
	
			a1 = 1.0f/(decreasingAlpha * alphaRampDuration -
				alphaRampDuration * alphaRampDuration);

			if (dt < alphaRampDuration) {
			    alpha = 0.5f*a1*dt*dt;
			} else if (dt <  decreasingAlpha - alphaRampDuration) {
			    alpha = 0.5f*a1*alphaRampDuration*
				alphaRampDuration +
				(dt - alphaRampDuration)*a1*
				alphaRampDuration;
			} else {
			    alpha =
				a1*alphaRampDuration*alphaRampDuration +
				(decreasingAlpha - 2.0f*alphaRampDuration)*a1*
				alphaRampDuration -
				0.5f*a1*( decreasingAlpha - dt)*
				(decreasingAlpha - dt);
			}
			alpha = 1.0f -  alpha;
			return alpha;
		    }
	
		}
	return 0.0f;
    }

    float mfmod(float a, float b) {
	float fm, ta = (a), tb = (b);
	int fmint;
	if (tb < 0.0f) tb = -tb;
	if (ta < 0.0f) ta = -ta;

	fmint =(int)( ta/tb);
	fm = ta - (float)fmint * tb;	

	if ((a) < 0.0f) return ((b) - fm);
	else return fm;
    }

    /**
      * Retrieves this alpha's startTime, the base
      * for all relative time specifications; the default value
      * for startTime is the system start time.
      * @return this alpha's startTime.
      */
    public long getStartTime() {
	return this.startTime;
    }

    /**
     * Sets this alpha's startTime to that specified in the argument; 
     * startTime sets the base (or zero) for all relative time
     * computations; the default value for startTime is the system
     * start time.
     * @param startTime the new startTime value
     */
    public void setStartTime(long startTime) {
	this.startTime = startTime;
	// This is used for passive wakeupOnElapsedFrame in
	// Interpolator to restart behavior after alpha.finished()
	VirtualUniverse.mc.sendRunMessage(J3dThread.RENDER_THREAD);
    }

    /**
      * Retrieves this alpha's loopCount.
      * @return this alpha's loopCount.
      */
    public int getLoopCount() {
	return this.loopCount;
    }

    /**
      * Set this alpha's loopCount to that specified in the argument.
      * @param loopCount the new loopCount value
      */
    public void setLoopCount(int loopCount) {
	this.loopCount = loopCount;
	computeStopTime();
	VirtualUniverse.mc.sendRunMessage(J3dThread.RENDER_THREAD);
    }

    /**
      * Retrieves this alpha's mode.
      * @return this alpha's mode: any combination of
      * INCREASING_ENABLE and DECREASING_ENABLE
      */
    public int getMode() {
	return this.mode;
    }

    /**
      * Set this alpha's mode to that specified in the argument.
     * @param mode indicates whether the increasing alpha parameters or
     * the decreasing alpha parameters or both are active.  This parameter
     * accepts the following values, INCREASING_ENABLE or
     * DECREASING_ENABLE, which may be ORed together to specify
     * that both are active.
     * The increasing alpha parameters are increasingAlphaDuration,
     * increasingAlphaRampDuration, and alphaAtOneDuration.
     * The decreasing alpha parameters are decreasingAlphaDuration,
     * decreasingAlphaRampDuration, and alphaAtZeroDuration.
      */
    public void setMode(int mode) {
	this.mode = mode;
	computeStopTime();
	VirtualUniverse.mc.sendRunMessage(J3dThread.RENDER_THREAD);
    }

    /**
      * Retrieves this alpha's triggerTime.
      * @return this alpha's triggerTime.
      */
    public long getTriggerTime() {
	return (long) (this.triggerTime * 1000f);
    }

    /**
      * Set this alpha's triggerTime to that specified in the argument.
      * @param triggerTime  the new triggerTime
      */
    public void setTriggerTime(long triggerTime) {
	this.triggerTime = (float) triggerTime * .001f;
	computeStopTime();
	VirtualUniverse.mc.sendRunMessage(J3dThread.RENDER_THREAD);
    }

    /**
      * Retrieves this alpha's phaseDelayDuration.
      * @return this alpha's phaseDelayDuration.
      */
    public long getPhaseDelayDuration() {
	return (long)(this.phaseDelay * 1000f);
    }

    /**
      * Set this alpha's phaseDelayDuration to that specified in 
      * the argument.
      * @param phaseDelayDuration  the new phaseDelayDuration
      */
    public void setPhaseDelayDuration(long phaseDelayDuration) {
	this.phaseDelay = (float) phaseDelayDuration * .001f;
	computeStopTime();
	VirtualUniverse.mc.sendRunMessage(J3dThread.RENDER_THREAD);
    }

    /**
      * Retrieves this alpha's increasingAlphaDuration.
      * @return this alpha's increasingAlphaDuration.
      */
    public long getIncreasingAlphaDuration() {
	return (long)(this.increasingAlpha * 1000f);
    }

    /**
      * Set this alpha's increasingAlphaDuration to that specified in 
      * the argument.
      * @param increasingAlphaDuration  the new increasingAlphaDuration
      */
    public void setIncreasingAlphaDuration(long increasingAlphaDuration) {
	this.increasingAlpha = (float) increasingAlphaDuration * .001f;
	computeStopTime();
	VirtualUniverse.mc.sendRunMessage(J3dThread.RENDER_THREAD);
    }

    /**
      * Retrieves this alpha's increasingAlphaRampDuration.
      * @return this alpha's increasingAlphaRampDuration.
      */
    public long getIncreasingAlphaRampDuration() {
	return increasingAlphaRamp;
    }

    /**
      * Set this alpha's increasingAlphaRampDuration to that specified 
      * in the argument.
      * @param increasingAlphaRampDuration  the new increasingAlphaRampDuration
      */
    public void setIncreasingAlphaRampDuration(long increasingAlphaRampDuration) {
	increasingAlphaRamp = increasingAlphaRampDuration;
	incAlphaRampInternal = (float) increasingAlphaRampDuration * .001f;
	if (incAlphaRampInternal > (0.5f * increasingAlpha)) {
	    incAlphaRampInternal = 0.5f * increasingAlpha;
	}
	VirtualUniverse.mc.sendRunMessage(J3dThread.RENDER_THREAD);
    }

    /**
      * Retrieves this alpha's alphaAtOneDuration.
      * @return this alpha's alphaAtOneDuration.
      */
    public long getAlphaAtOneDuration() {
	return (long)(this.alphaAtOne * 1000f);
    }

    /**
     * Set this alpha object's alphaAtOneDuration to the specified 
     * value.
     * @param alphaAtOneDuration  the new alphaAtOneDuration
     */
    public void setAlphaAtOneDuration(long alphaAtOneDuration) {
	this.alphaAtOne = (float) alphaAtOneDuration * .001f;
	computeStopTime();
	VirtualUniverse.mc.sendRunMessage(J3dThread.RENDER_THREAD);
    }

    /**
      * Retrieves this alpha's decreasingAlphaDuration.
      * @return this alpha's decreasingAlphaDuration.
      */
    public long getDecreasingAlphaDuration() {
	return (long)(this.decreasingAlpha * 1000f);
    }

    /**
      * Set this alpha's decreasingAlphaDuration to that specified in 
      * the argument.
      * @param decreasingAlphaDuration  the new decreasingAlphaDuration
      */
    public void setDecreasingAlphaDuration(long decreasingAlphaDuration) {
	this.decreasingAlpha = (float) decreasingAlphaDuration * .001f;
	computeStopTime();
	VirtualUniverse.mc.sendRunMessage(J3dThread.RENDER_THREAD);
    }

    /**
      * Retrieves this alpha's decreasingAlphaRampDuration.
      * @return this alpha's decreasingAlphaRampDuration.
      */
    public long getDecreasingAlphaRampDuration() {
	return decreasingAlphaRamp;
    }

    /**
      * Set this alpha's decreasingAlphaRampDuration to that specified 
      * in the argument.
      * @param decreasingAlphaRampDuration  the new decreasingAlphaRampDuration
      */
    public void setDecreasingAlphaRampDuration(long decreasingAlphaRampDuration) {
	decreasingAlphaRamp = decreasingAlphaRampDuration;
	decAlphaRampInternal = (float) decreasingAlphaRampDuration * .001f;
	if (decAlphaRampInternal > (0.5f * decreasingAlpha)) {
	    decAlphaRampInternal = 0.5f * decreasingAlpha;
	}
	VirtualUniverse.mc.sendRunMessage(J3dThread.RENDER_THREAD);
    }

    /**
      * Retrieves this alpha's alphaAtZeroDuration.
      * @return this alpha's alphaAtZeroDuration.
      */
    public long getAlphaAtZeroDuration() {
	return (long)(this.alphaAtZero * 1000f);
    }

    /**
     * Set this alpha object's alphaAtZeroDuration to the specified 
     * value.
     * @param alphaAtZeroDuration  the new alphaAtZeroDuration
     */
    public void setAlphaAtZeroDuration(long alphaAtZeroDuration) {
	this.alphaAtZero = (float) alphaAtZeroDuration * .001f;
	computeStopTime();
	VirtualUniverse.mc.sendRunMessage(J3dThread.RENDER_THREAD);
    }

    /**
     * Query to test if this alpha object is past its activity window,
     * that is, if it has finished looping.
     * @return true if no longer looping, false otherwise
     */
    public boolean finished() {
	long currentTime = paused ? pauseTime : J3dClock.currentTimeMillis();
	return ((loopCount != -1) &&
 	        ((float)(currentTime - startTime) * .001f > stopTime));
    }

    final private void computeStopTime() {
        if (loopCount >= 0) {
	    float sum = 0;
	    if (( mode & INCREASING_ENABLE ) != 0) {
		sum = increasingAlpha+alphaAtOne; 
	    }
	    if ((mode & DECREASING_ENABLE) != 0) {
		sum += decreasingAlpha+alphaAtZero;
	    }
	    stopTime = this.triggerTime + phaseDelay + loopCount*sum;
	} else {
	    stopTime = 0;
	}
    }

    /** 
     * This internal method returns a clone of the Alpha
     *
     * @return a duplicate of this Alpha
     */
    Alpha cloneAlpha() {
      Alpha a = new Alpha();
      a.setStartTime(getStartTime());
      a.setLoopCount(getLoopCount());
      a.setMode(getMode());
      a.setTriggerTime(getTriggerTime());
      a.setPhaseDelayDuration(getPhaseDelayDuration());
      a.setIncreasingAlphaDuration(getIncreasingAlphaDuration());
      a.setIncreasingAlphaRampDuration(getIncreasingAlphaRampDuration());
      a.setAlphaAtOneDuration(getAlphaAtOneDuration());
      a.setDecreasingAlphaDuration(getDecreasingAlphaDuration());
      a.setDecreasingAlphaRampDuration(getDecreasingAlphaRampDuration());
      a.setAlphaAtZeroDuration(getAlphaAtZeroDuration());
      return a;
    }

    static {
        VirtualUniverse.loadLibraries();
    }

}
