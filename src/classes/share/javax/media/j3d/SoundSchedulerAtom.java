/*
 * $RCSfile$
 *
 * Copyright 2000-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;


/**
 * A SoundSchedulerAtom is the smallest object representing a Sound within
 * SoundScheduler.  This class contains View-Depedent fields.  Some of these
 * fields may appear to over lap fields in the Sound Node classes, but
 * remember that the Sound Node fields are universal, user-defined fields
 * and do not take into account specific Audio Device view-dependent
 * conditions.
 */

class SoundSchedulerAtom extends Object {

    /**
     * The mirror sound node component of this sound scheduler atom
     */
    SoundRetained sound = null;

    /**
     * MediaContainer currently loaded for this atom
     */
    MediaContainer soundData = null;

    // Maintain continuously playing silent sound sources.
    long      startTime = 0;
    long      endTime = 0;

    long      sampleLength = 0;
    long      loopStartOffset = 0; // for most this will be 0
    long      loopLength = 0;      // for most this is end sample - sampleLength
    long      attackLength = 0;    // portion of sample before loop section
    long      releaseLength = 0;   // portion of sample after loop section

    int       loadStatus = SoundRetained.LOAD_NULL;
    boolean   playing = false;
    int       numberChannels = 0;

    /**
     *  Is this sound in an active scheduling region
     */
    boolean   activated = false;

    /**
     *  Switch for turning sound on or off while the sound is "active"
     */
    static final int OFF         = 0;
    static final int ON          = 1;
    static final int PENDING_ON  = 2;
    static final int PENDING_OFF = 3;
    int       enabled = OFF;

    /**
     *  Switch for muting and unmuting sound while it is playing
     */
    static final int UNMUTED        = 0;
    static final int MUTED          = 1;
    static final int PENDING_UNMUTE = 2;
    static final int PENDING_MUTE   = 3;
    int       muted = UNMUTED;

    /**
     *  Switch for pausing and unpausing sound while it is playing
     */
    static final int UNPAUSED        = 0;  // or resumed
    static final int PAUSED          = 1;
    static final int PENDING_UNPAUSE = 2;  // or pending resume
    static final int PENDING_PAUSE   = 3;
    int       paused = UNPAUSED;


    /**
     * Pending action for this sound determined by the SoundScheduler
     */
    static final int DO_NOTHING      =  0;
    static final int LEAVE_OFF       =  1;
    static final int LEAVE_SILENT    =  2;
    static final int LEAVE_AUDIBLE   =  3;
    static final int LEAVE_PAUSED    =  4;

    static final int RESTART_AUDIBLE =  5;
    static final int START_AUDIBLE   =  6;
    static final int RESTART_SILENT  =  7;
    static final int START_SILENT    =  8;

    static final int MAKE_AUDIBLE    = 11;
    static final int MAKE_SILENT     = 12;
    static final int PAUSE_AUDIBLE   = 13;
    static final int PAUSE_SILENT    = 14;
    static final int RESUME_AUDIBLE  = 15;
    static final int RESUME_SILENT   = 16;
    static final int TURN_OFF        = 17;
    static final int UPDATE          = 18;
    static final int COMPLETE        = 19;
    int       schedulingAction = DO_NOTHING;

    /**
     * This status flag is used for sound scheduling
     */
    static final int SOUND_OFF      = 0; // The sound is not playing
    static final int SOUND_AUDIBLE  = 1; // The sound is potentially audible
    static final int SOUND_SILENT   = 2; // The sound is playing silently
    static final int SOUND_PAUSED   = 3; // The sound is playing silently
    static final int SOUND_COMPLETE = 4; // The sound is finished playing
    int       status = SOUND_OFF;

    // Sound atoms have two dirty flags: attribsDirty for sound node fields
    // and stateDirty for changes to sound state not reflected by sound fields.
    // When the field/parameter associated with the dirty bit has been:
    // passed to all SoundSchedulers to update sound rendering or 'run' state
    // the bit for that field is cleared by the SoundStructure thread.

    /**
     * attribsDirty bit field
     * This bitmask is set when sound node attribute is changed by the user.
     */
    int       attribsDirty = 0x0000;

    /**
     * stateDirty bit field
     * This bitmask is set when scene graph state is changed.
     */
    int       stateDirty   = 0x0000;

    // Load Sound Data Status maintained in SoundRetained class

    /**
     *  Identifiers of sample associated with sound source
     */
    int       sampleId = SoundRetained.NULL_SOUND;

    /**
     *  reference to Sound Scheduler this atom is associated with
     */
    SoundScheduler  soundScheduler = null;


    /**
     * Calculate absolute time at which sample completes
     * Checks playing flag denoting if sound is started already or not:
     *    false - calcalutes endTime in relation to startTime
     *    true  - re-calculates endTime based on current position in
     *            loop portion of sample plus release length
     */
    synchronized void calculateEndTime() {
        SoundRetained sgSound = sound.sgSound;
        int loops = sgSound.loopCount;
        if (debugFlag)
            debugPrint("calculateEndTime: loop count = " + loops);
        // test lengths for <= 0; this includes DURATION_UNKNOWN
        if ( (sampleLength <= 0  || loopLength <= 0  || loops < 0 )
// QUESTION: removed? but what was this trying to avoid
//              changing endTime when that is already set?
//              but what happens when user changes LoopCount AFTER
//              sound is started - should be able to do this
//            && (enabled == OFF  || enabled == PENDING_OFF)
           ) {
            endTime = -1;
            if (debugFlag)
                debugPrint("calculateEndTime: set to -1");
        }
        else {
// QUESTION: if "&& playing" is in above test; won't we have to test for
//          length unknown and loop = -1??
            if (playing && (startTime > 0)) {
                endTime = startTime + attackLength +
                          (loopLength * (loops+1)) + releaseLength; if (debugFlag)
                    debugPrint("calculateEndTime: isPlaying so = " + endTime); }
            else  {
		// Called when release flag is true
		// Determine where within the loop portion sample the
		//   sound is currently playing, then set endTime to
		//   play remaining portion of loop portion plus the
		//   release portion.
                long currentTime = J3dClock.currentTimeMillis();
                endTime = currentTime + ( (loopLength -
                    ((currentTime - startTime - attackLength) % loopLength)) +
                        releaseLength );
                if (debugFlag)
                    debugPrint("calculateEndTime: NOT Playing so = " + endTime);
            }
        }
    }


    void enable(boolean enabled) {
        if (enabled) {
                    setEnableState(PENDING_ON);
                    if (debugFlag)
                        debugPrint(" enableSound calls soundAtom " +
                               this + " setEnableState PENDING_ON");
        }
        else {
                    setEnableState(PENDING_OFF);
                    if (debugFlag)
                        debugPrint(" enableSound calls soundAtom " +
                               this + " setEnableState PENDING_OFF");
        }
    }


    void mute(boolean muted) {
        if (muted) {
            setMuteState(PENDING_MUTE);
            if (debugFlag)
                        debugPrint(" muteSound() calls soundAtom " +
                               this + " setMuteState PENDING_ON");
        }
        else {
            setMuteState(PENDING_UNMUTE);
            if (debugFlag)
                        debugPrint(" muteSound() calls soundAtom " +
                               this + " setMuteState PENDING_UNMUTE");
        }
    }

    void pause(boolean paused) {
        if (paused) {
            setPauseState(PENDING_PAUSE);
            if (debugFlag)
                debugPrint(this + ".pause calls setPauseState(PENDING_PAUSE)");
        }
        else {
            setPauseState(PENDING_UNPAUSE);
            if (debugFlag)
                debugPrint(this +".pause calls setPauseState(PENDING_UNPAUSE)");
        }
    }


// XXXX: remove this
// just set the state after debug no longer needed
    void setEnableState(int state) {
        enabled = state;
        switch (state) {
            case PENDING_ON:
                if (debugFlag)
                    debugPrint("set enabled to PENDING_ON");
                break;
            case ON:
                if (debugFlag)
                    debugPrint("set enabled to ON");
                break;
            case PENDING_OFF:
                if (debugFlag)
                    debugPrint("set enabled to PENDING_OFF");
                break;
            case OFF:
                if (debugFlag)
                    debugPrint("set enabled to OFF");
                break;
            default:
                if (debugFlag)
                    debugPrint("state = " + state);
                break;
        }
    }

// XXXX: remove this
// just set the state after debug no longer needed
    void setMuteState(int state) {
        muted = state;
        switch (state) {
            case PENDING_MUTE:
                if (debugFlag)
                    debugPrint("set mute to PENDING_MUTE");
                break;
            case MUTED:
                if (debugFlag)
                    debugPrint("set mute to MUTE");
                break;
            case PENDING_UNMUTE:
                if (debugFlag)
                    debugPrint("set mute to PENDING_UNMUTE");
                break;
            case UNMUTED:
                if (debugFlag)
                    debugPrint("set mute to UNMUTE");
                break;
            default:
                if (debugFlag)
                    debugPrint("state = " + state);
                break;
        }
    }

// XXXX: remove this
// just set the state after debug no longer needed
    void setPauseState(int state) {
        paused = state;
        switch (state) {
            case PENDING_PAUSE:
                if (debugFlag)
                    debugPrint("set pause to PENDING_PAUSE");
                break;
            case PAUSED:
                if (debugFlag)
                    debugPrint("set pause to PAUSE");
                break;
            case PENDING_UNPAUSE:
                if (debugFlag)
                    debugPrint("set pause to PENDING_UNPAUSE");
                break;
            case UNPAUSED:
                if (debugFlag)
                    debugPrint("set pause to UNPAUSE");
                break;
            default:
                if (debugFlag)
                    debugPrint("state = " + state);
                break;
        }
    }


    /**
     * calcActiveSchedAction()
     * Calculate Sound Scheduler Action for Active sound (it's region
     * intersects the viewPlatform).
     *
     * A big switch testing various SoundRetained fields to determine
     * what SoundScheduler action to perform when sound is Active
     *     set sound active flag true
     *     switch on enable value, to set pending scheduling action
     *         depending on continuous and release flags and sound status
     */
    synchronized int calcActiveSchedAction() {
        SoundRetained sgSound = sound.sgSound;
        int action = DO_NOTHING;
        activated = true;
        switch (enabled) {
            case PENDING_ON:
                setEnableState(ON);
                if (debugFlag)
                     debugPrint(" calcActiveSchedAction: PENDING_ON");
                if (status == SOUND_OFF ||
                    status == SOUND_PAUSED)
                    action = START_AUDIBLE;
                else
                    action = RESTART_AUDIBLE;
                break;
            case ON:
                if (debugFlag)
                    debugPrint(" calcActiveSchedAction: ON");
                if (status == SOUND_OFF)
                    // should NOT see this, but if we do...
                    action = START_AUDIBLE;
		else if (status == SOUND_SILENT)
                    action = MAKE_AUDIBLE;
		else // status == SOUND_AUDIBLE
                    action = LEAVE_AUDIBLE;
                break;
            case PENDING_OFF:
                setEnableState(OFF);
                if (debugFlag)
                    debugPrint("enable = " + enabled +
                               "enabled set to OFF");
                // fail thru
            case OFF:
		// QUESTION: Why would enable status ever be OFF yet
		// status SOUND_AUDIBLE or _SILENT?
	        if (status == SOUND_AUDIBLE) {
                    if (sgSound.release) {
                        if (debugFlag)
                            debugPrint("enable = " + enabled +
                                    ", AUDIBLE, released, " +
                                    "action <- LEAVE_AUDIBLE");
                        if (enabled == PENDING_OFF) {
                            // re-calculate EndTime
                            calculateEndTime();
                        }
                        action = LEAVE_AUDIBLE;
                    }
                    else {
                        if (debugFlag)
                            debugPrint("enable = " + enabled +
                                    ", AUDIBLE, not released, "+
                                    "action <- TURN_OFF");
                        action = TURN_OFF;
                    }
                }
		else if (status == SOUND_SILENT) {
                    if (sgSound.release) {
                        if (debugFlag)
                            debugPrint("enable = " + enabled +
                                    ", SILENT, released, " +
                                    "action <- MAKE_AUDIBLE");
                        // re-calculate EndTime
                        calculateEndTime();
                        action = MAKE_AUDIBLE;
                    }
                    else   {
                        if (debugFlag)
                            debugPrint("enable = " + enabled +
                                    ", SILENT, not released, " +
                                    "action <- TURN_OFF");
                        action = TURN_OFF;
                    }
                }
		else { //  status == SOUND_OFF
                    action = LEAVE_OFF;
                }
                break;
         } // switch on enabled flag

         // if sounds pause state is PENDING_PAUSE modify action to perform.
         if (paused == PENDING_PAUSE) {
             // if this pause state is set to PAUSE then assume the sound is
             // already paused, so any incoming action that leave the state
             // as it already is, leaves the sound paused.
            if (debugFlag)
                debugPrint("    PENDING_PAUSE");
            switch (action) {
                case MAKE_AUDIBLE:
                case LEAVE_AUDIBLE:
                case RESUME_AUDIBLE:
                    action = PAUSE_AUDIBLE;
                    break;
                case MAKE_SILENT:
                case LEAVE_SILENT:
                case RESUME_SILENT:
                    action = PAUSE_SILENT;
                    break;
                default:
                    // don't change action for any other cases
                    break;
            }
	 }
         // if sounds pause state is PENDING_UNPAUSE modify action
	 else if (paused == PENDING_UNPAUSE) {
	     debugPrint("    PENDING_UNPAUSE");
	     switch (action) {
                // When restart (audible or silent) pause flag is checked and
                // explicitly set in SoundScheduler
                case MAKE_AUDIBLE:
                case LEAVE_AUDIBLE:
                case PAUSE_AUDIBLE:
                    action = RESUME_AUDIBLE;
                    break;
                case MAKE_SILENT:
                case LEAVE_SILENT:
                case PAUSE_SILENT:
                    action = RESUME_SILENT;
                    break;
                default:
                    // don't change action for any other cases
                    break;
            }
        }
        return(action);
    } // end of calcActiveSchedAction


    /**
     * calcInactiveSchedAction()
     * Calculate Sound Scheduler action for Inactive sound
     *
     * A big switch testing various SoundRetained fields to determine
     * what SoundScheduler action to perform when sound is inactive.
     *     set sound active flag false
     *     switch on enable value, to set pending scheduling action
     *         depending on continuous and release flags and sound status
     */
    synchronized int calcInactiveSchedAction() {
        int action  = DO_NOTHING;
        SoundRetained sgSound = sound.sgSound;

	// Sound is Inactive
	// Generally, sound is OFF unless continuous flag true
	// then sound is silently playing if on.
	activated = false;

        switch (enabled) {
            case PENDING_ON:
                if (debugFlag)
                debugPrint("    calcInactiveSchedAction: PENDING_ON ");
                setEnableState(ON);
                if (sgSound.continuous) {
                    if (status == SOUND_OFF)
                        action = START_SILENT;
                    else // status == SOUND_AUDIBLE or SOUND_SILENT
                        action = RESTART_SILENT;
                }
                else { // sound is not continuous
                    if (status == SOUND_OFF)
                        action = LEAVE_OFF;
                    else // status == SOUND_SILENT || SOUND_AUDIBLE
                        action = TURN_OFF;
                }
                break;
            case ON:
                if (debugFlag)
                    debugPrint("    calcInactiveSchedActio: ON ");
                if (sgSound.continuous) {
                    if (status == SOUND_AUDIBLE)
                        action = MAKE_SILENT;
                    else if (status == SOUND_OFF)
                        action = START_SILENT;
                    else // status == SOUND_SILENT
                        action = LEAVE_SILENT;
                }
                else { // sound is not continuous
                         // nothing to do if already off
                    if (status == SOUND_OFF)
                         action = LEAVE_OFF;
                    else // status == SOUND_SILENT or SOUND_AUDIBLE
                         action = TURN_OFF;
                }
                break;
            case PENDING_OFF:
                setEnableState(OFF);
                if (debugFlag)
                    debugPrint("Enable = " + enabled +
                                    "enabled set to OFF");
                // fall thru

            case OFF:
                if (sgSound.release && sgSound.continuous) {
                     if (enabled == PENDING_OFF) {
                         // re-calculate EndTime
                         calculateEndTime();
                     }
		     if (status == SOUND_AUDIBLE) {
                         if (debugFlag)
                             debugPrint("Enable = " + enabled +
                                      ", AUDIBLE, released & continuous - " +
                                      "action <- MAKE_SILENT");
                         action = MAKE_SILENT;
                     }
		     else if (status == SOUND_SILENT) {
                         if (debugFlag)
                             debugPrint("Enable = " + enabled +
                                     ", SILENT, released & continuous - " +
                                     "action <- TURN_OFF");
                         action = LEAVE_SILENT;
                     }
                     else {
                         if (debugFlag)
                             debugPrint("Enable = " + enabled +
                                     ", already OFF, action <- LEAVE_OFF");
                         action = LEAVE_OFF;
                     }
                }
                else  { // continuous and release flag not both true
                     if (status == SOUND_OFF) {
                         if (debugFlag)
                             debugPrint("Enable = " + enabled +
                                     ", already OFF, action <- LEAVE_OFF");
                         action = LEAVE_OFF;
                     }
                     else {
                         if (debugFlag)
                             debugPrint("Enable = " + enabled +
                                     ", not already OFF, action <- TURN_OFF");
                         action = TURN_OFF;
                     }
                }
                break;
            default:
                break;
         } // switch

         // if sounds pause state is PENDING_PAUSE modify action to perform.
         if (paused == PENDING_PAUSE) {
             // if this pause state is set to PAUSE then assume the sound is
             // already paused, so any incoming action that leave the state
             // as it already is, leaves the sound paused.
            switch (action) {
                case MAKE_SILENT:
                case LEAVE_SILENT:
                case RESUME_SILENT:
                    action = PAUSE_SILENT;
                    break;
                default:
                    // don't change action for any other cases
                    break;
            }
        }
        // if sounds pause state is PENDING_UNPAUSE modify action
        else if (paused == PENDING_UNPAUSE) {
            switch (action) {
                case LEAVE_SILENT:
                    action = RESUME_SILENT;
                    break;
                default:
                    // don't change action for any other cases
                    break;
            }
        }
        return (action);
    } // end of calcInactiveSchedAction

// XXXX: isPLaying
// XXXX: setLoadingState

    // Debug print mechanism for Sound nodes
    static final boolean debugFlag = false;
    static final boolean internalErrors = false;

    void debugPrint(String message) {
        if (debugFlag) {
            System.err.println(message);
	}
    }


    /**
     * Set bit(s) in soundDirty field
     * @param binary flag denotes bits to set ON
     */
    void setAttribsDirtyFlag(int bitFlag) {
        attribsDirty |= bitFlag;
        if (debugFlag)
            debugPrint("setAttribsDirtyFlag = " + bitFlag);
        return ;
    }
    void setStateDirtyFlag(int bitFlag) {
        stateDirty |= bitFlag;
        if (debugFlag)
            debugPrint("setStateDirtyFlag = " + bitFlag);
        return ;
    }

    /**
     * Clear sound's dirty flag bit value.
     * @param binary flag denotes bits to set OFF
     */
    void clearAttribsDirtyFlag(int bitFlag) {
        if (debugFlag)
            debugPrint("clearAttribsDirtyFlag = " + bitFlag);
        attribsDirty &= ~bitFlag;
        return ;
    }
    void clearAttribsDirtyFlag() {
        // clear all bits
        if (debugFlag)
            debugPrint("clearAttribsDirtyFlag = ALL");
        attribsDirty = 0x0;
        return ;
    }
    void clearStateDirtyFlag(int bitFlag) {
        if (debugFlag)
            debugPrint("clearStateDirtyFlag = " + bitFlag);
        stateDirty &= ~bitFlag;
        return ;
    }
    void clearStateDirtyFlag() {
        if (debugFlag)
            debugPrint("clearStateDirtyFlag = ALL");
        stateDirty = 0x0;
        return ;
    }


    /**
     * Test sound's dirty flag bit(s)
     * @param field denotes which bitmask to set into
     * @param binary flag denotes bits to set Test
     * @return true if bit(s) in bitFlag are set dirty (on)
     */
    boolean testDirtyFlag(int field, int bitFlag) {
        if ((field & bitFlag) > 0)
            return true;
        else
            return false;
    }

    /**
     * Test sound's dirty flags for ANY bits on
     * @return true if any bit in bitFlag is flipped on
     */
    boolean testDirtyFlags() {
        if ((attribsDirty & 0xFFFF) > 0)
            return true;
        else if ((stateDirty & 0xFFFF) > 0)
            return true;
        else
            return false;
    }

}
