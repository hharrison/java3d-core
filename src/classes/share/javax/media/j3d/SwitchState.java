/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

class SwitchState {
    // a bitmask to track the composite switchOn state in a nested switch
    // tree. A bit is set if a branch is switched off at the switch
    // level specified by the position of the bit in the bitmask
    // It is an array of long in order to support infinite deep nested
    // switch tree
    long compositeSwitchMask[] = new long[]{0};

    // switchOn state cached in user thread
    boolean cachedSwitchOn = true;

    // switchOn state in current time, is true if compositeSwitchMask == 0
    boolean currentSwitchOn = true;

    // switchOn state in last time, is true if compositeSwitchMask == 0
    boolean lastSwitchOn = true;

    boolean initialized = false;

    CachedTargets cachedTargets = null;

    boolean inSwitch = false;

    public SwitchState(boolean inSwitch) {
        this.inSwitch = inSwitch;
        initialized = !inSwitch;
    }

    void dump() {
	System.out.println(
	                   " MASK " + compositeSwitchMask[0] +
	                   " CACH " + cachedSwitchOn +
	                   " CURR " + currentSwitchOn +
			   " LAST " + lastSwitchOn);
    }

    void updateCompositeSwitchMask(int switchLevel, boolean switchOn) {
        if (switchLevel < 64) {
            if (switchOn) {
                compositeSwitchMask[0] &= ~(1 << switchLevel);
            } else {
                compositeSwitchMask[0] |= (1 << switchLevel);
            }
        } else {
            int i;
            int index = switchLevel/64;
            int offset = switchLevel%64;

            if (index > compositeSwitchMask.length) {
                long newCompositeSwitchMask[] = new long[index+1];
                System.arraycopy(compositeSwitchMask, 0,
                                newCompositeSwitchMask, 0, index);
                compositeSwitchMask = newCompositeSwitchMask;
            }
            if (switchOn) {
                compositeSwitchMask[index] &= ~(1 << offset);
            } else {
                compositeSwitchMask[index] |= (1 << offset);
            }
        }
    }

    void initSwitchOn() {
	boolean switchOn = evalCompositeSwitchOn();
        currentSwitchOn = lastSwitchOn = cachedSwitchOn = switchOn;
        //currentSwitchOn = cachedSwitchOn = switchOn;
	initialized = true;
    }

    void updateCurrentSwitchOn() {
        currentSwitchOn = !currentSwitchOn;
    }

    void updateLastSwitchOn() {
        lastSwitchOn = currentSwitchOn;
    }

    void updateCachedSwitchOn() {
        cachedSwitchOn = !cachedSwitchOn;
    }

    boolean evalCompositeSwitchOn() {
        boolean switchOn;
        if (compositeSwitchMask.length == 1) {
            switchOn = (compositeSwitchMask[0] == 0);
        } else {
            switchOn = true;
            for (int i=0; i<compositeSwitchMask.length; i++) {
                if (compositeSwitchMask[i] != 0) {
                    switchOn = false;
                    break;
                }
            }
        }
	return switchOn;
    }
}

