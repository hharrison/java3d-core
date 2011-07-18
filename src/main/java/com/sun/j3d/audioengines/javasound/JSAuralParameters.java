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

import java.lang.String;
import java.io.*;

/**
 * The AudioDevice dependent sound node and aural attribute node parameters.
 * These are explicitly maintained for HaeSoundMixer
 */

public class JSAuralParameters extends com.sun.j3d.audioengines.AuralParameters {

    /**
     * Reverb Parameters
     *   
     * dirty flag checked and cleared by render()
     */  
    static int REFLECTION_COEFF_CHANGED =  1;
    static int REVERB_DELAY_CHANGED     =  2;
    static int REVERB_ORDER_CHANGED     =  4;
 
    int      reverbDirty = 0xFFFF;
    int      lastReverbSpeed = 0;  // TODO: NOT used yet
    boolean  reverbFlag = false;  // previously refered to as reverbOn
    int      reverbType = 1;  // Reverb type 1 equals NONE in JavaSound engine


    JSAuralParameters() {
        super();
        reverbDirty = 0xFFFF;
        lastReverbSpeed = 0;
        reverbType = 1;
        reverbFlag = false;
    }
}
