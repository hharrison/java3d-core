/*
 * Copyright 2001-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
	System.err.println(
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

