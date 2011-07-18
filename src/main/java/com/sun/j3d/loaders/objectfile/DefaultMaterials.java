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

package com.sun.j3d.loaders.objectfile;

/**
 * This class provides default materials for the object file loader
 */
class DefaultMaterials {
  /**
   * String that describes the default materials.
   */
  static final String materials = 
    "newmtl amber\n" +
    "Ka 0.0531 0.0531 0.0531\n" +
    "Kd 0.5755 0.2678 0.0000\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl amber_trans\n" +
    "Ka 0.0531 0.0531 0.0531\n" +
    "Kd 0.5755 0.2678 0.0000\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "d 0.8400\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl charcoal\n" +
    "Ka 0.0082 0.0082 0.0082\n" +
    "Kd 0.0041 0.0041 0.0041\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl lavendar\n" +
    "Ka 0.1281 0.0857 0.2122\n" +
    "Kd 0.2187 0.0906 0.3469\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl navy_blue\n" +
    "Ka 0.0000 0.0000 0.0490\n" +
    "Kd 0.0000 0.0000 0.0531\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl pale_green\n" +
    "Ka 0.0444 0.0898 0.0447\n" +
    "Kd 0.0712 0.3796 0.0490\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl pale_pink\n" +
    "Ka 0.0898 0.0444 0.0444\n" +
    "Kd 0.6531 0.2053 0.4160\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl pale_yellow\n" +
    "Ka 0.3606 0.3755 0.0935\n" +
    "Kd 0.6898 0.6211 0.1999\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl peach\n" +
    "Ka 0.3143 0.1187 0.0167\n" +
    "Kd 0.6367 0.1829 0.0156\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl periwinkle\n" +
    "Ka 0.0000 0.0000 0.1184\n" +
    "Kd 0.0000 0.0396 0.8286\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl redwood\n" +
    "Ka 0.0204 0.0027 0.0000\n" +
    "Kd 0.2571 0.0330 0.0000\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl smoked_glass\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.0041 0.0041 0.0041\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "d 0.9800\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl aqua_filter\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.3743 0.6694 0.5791\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "d 0.9800\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl yellow_green\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.1875 0.4082 0.0017\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl bluetint\n" +
    "Ka 0.1100 0.4238 0.5388\n" +
    "Kd 0.0468 0.7115 0.9551\n" +
    "Ks 0.3184 0.3184 0.3184\n" +
    "illum 9\n" +
    "d 0.5700\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl plasma\n" +
    "Ka 0.4082 0.0816 0.2129\n" +
    "Kd 1.0000 0.0776 0.4478\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 9\n" +
    "d 0.7500\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl emerald\n" +
    "Ka 0.0470 1.0000 0.0000\n" +
    "Kd 0.0470 1.0000 0.0000\n" +
    "Ks 0.2000 0.2000 0.2000\n" +
    "illum 9\n" +
    "d 0.7500\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl ruby\n" +
    "Ka 1.0000 0.0000 0.0000\n" +
    "Kd 1.0000 0.0000 0.0000\n" +
    "Ks 0.2000 0.2000 0.2000\n" +
    "illum 9\n" +
    "d 0.7500\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl sapphire\n" +
    "Ka 0.0235 0.0000 1.0000\n" +
    "Kd 0.0235 0.0000 1.0000\n" +
    "Ks 0.2000 0.2000 0.2000\n" +
    "illum 9\n" +
    "d 0.7500\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl white\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 1.0000 1.0000 1.0000\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl red\n" +
    "Ka 0.4449 0.0000 0.0000\n" +
    "Kd 0.7714 0.0000 0.0000\n" +
    "Ks 0.8857 0.0000 0.0000\n" +
    "illum 2\n" +
    "Ns 136.4300\n" +
    "\n" +
    "newmtl blue_pure\n" +
    "Ka 0.0000 0.0000 0.5000\n" +
    "Kd 0.0000 0.0000 1.0000\n" +
    "Ks 0.0000 0.0000 0.5000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl lime\n" +
    "Ka 0.0000 0.5000 0.0000\n" +
    "Kd 0.0000 1.0000 0.0000\n" +
    "Ks 0.0000 0.5000 0.0000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl green\n" +
    "Ka 0.0000 0.2500 0.0000\n" +
    "Kd 0.0000 0.2500 0.0000\n" +
    "Ks 0.0000 0.2500 0.0000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl yellow\n" +
    "Ka 1.0000 0.6667 0.0000\n" +
    "Kd 1.0000 0.6667 0.0000\n" +
    "Ks 1.0000 0.6667 0.0000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl purple\n" +
    "Ka 0.5000 0.0000 1.0000\n" +
    "Kd 0.5000 0.0000 1.0000\n" +
    "Ks 0.5000 0.0000 1.0000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl orange\n" +
    "Ka 1.0000 0.1667 0.0000\n" +
    "Kd 1.0000 0.1667 0.0000\n" +
    "Ks 1.0000 0.1667 0.0000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl grey\n" +
    "Ka 0.5000 0.5000 0.5000\n" +
    "Kd 0.1837 0.1837 0.1837\n" +
    "Ks 0.5000 0.5000 0.5000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl rubber\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.0100 0.0100 0.0100\n" +
    "Ks 0.1000 0.1000 0.1000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl flaqua\n" +
    "Ka 0.0000 0.4000 0.4000\n" +
    "Kd 0.0000 0.5000 0.5000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flblack\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.0041 0.0041 0.0041\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flblue_pure\n" +
    "Ka 0.0000 0.0000 0.5592\n" +
    "Kd 0.0000 0.0000 0.7102\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flgrey\n" +
    "Ka 0.2163 0.2163 0.2163\n" +
    "Kd 0.5000 0.5000 0.5000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl fllime\n" +
    "Ka 0.0000 0.3673 0.0000\n" +
    "Kd 0.0000 1.0000 0.0000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl florange\n" +
    "Ka 0.6857 0.1143 0.0000\n" +
    "Kd 1.0000 0.1667 0.0000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flpurple\n" +
    "Ka 0.2368 0.0000 0.4735\n" +
    "Kd 0.3755 0.0000 0.7510\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flred\n" +
    "Ka 0.4000 0.0000 0.0000\n" +
    "Kd 1.0000 0.0000 0.0000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flyellow\n" +
    "Ka 0.7388 0.4925 0.0000\n" +
    "Kd 1.0000 0.6667 0.0000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl pink\n" +
    "Ka 0.9469 0.0078 0.2845\n" +
    "Kd 0.9878 0.1695 0.6702\n" +
    "Ks 0.7429 0.2972 0.2972\n" +
    "illum 2\n" +
    "Ns 106.2000\n" +
    "\n" +
    "newmtl flbrown\n" +
    "Ka 0.0571 0.0066 0.0011\n" +
    "Kd 0.1102 0.0120 0.0013\n" +
    "illum 1\n" +
    "\n" +
    "newmtl brown\n" +
    "Ka 0.1020 0.0185 0.0013\n" +
    "Kd 0.0857 0.0147 0.0000\n" +
    "Ks 0.1633 0.0240 0.0000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl glass\n" +
    "Ka 1.0000 1.0000 1.0000\n" +
    "Kd 0.4873 0.4919 0.5306\n" +
    "Ks 0.6406 0.6939 0.9020\n" +
    "illum 2\n" +
    "Ns 200.0000\n" +
    "\n" +
    "newmtl flesh\n" +
    "Ka 0.4612 0.3638 0.2993\n" +
    "Kd 0.5265 0.4127 0.3374\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl aqua\n" +
    "Ka 0.0000 0.4000 0.4000\n" +
    "Kd 0.0000 0.5000 0.5000\n" +
    "Ks 0.5673 0.5673 0.5673\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl black\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.0020 0.0020 0.0020\n" +
    "Ks 0.5184 0.5184 0.5184\n" +
    "illum 2\n" +
    "Ns 157.3600\n" +
    "\n" +
    "newmtl silver\n" +
    "Ka 0.9551 0.9551 0.9551\n" +
    "Kd 0.6163 0.6163 0.6163\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl dkblue_pure\n" +
    "Ka 0.0000 0.0000 0.0449\n" +
    "Kd 0.0000 0.0000 0.1347\n" +
    "Ks 0.0000 0.0000 0.5673\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl fldkblue_pure\n" +
    "Ka 0.0000 0.0000 0.0449\n" +
    "Kd 0.0000 0.0000 0.1347\n" +
    "illum 1\n" +
    "\n" +
    "newmtl dkgreen\n" +
    "Ka 0.0000 0.0122 0.0000\n" +
    "Kd 0.0058 0.0245 0.0000\n" +
    "Ks 0.0000 0.0490 0.0000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl dkgrey\n" +
    "Ka 0.0490 0.0490 0.0490\n" +
    "Kd 0.0490 0.0490 0.0490\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl ltbrown\n" +
    "Ka 0.1306 0.0538 0.0250\n" +
    "Kd 0.2776 0.1143 0.0531\n" +
    "Ks 0.3000 0.1235 0.0574\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl fldkgreen\n" +
    "Ka 0.0000 0.0122 0.0000\n" +
    "Kd 0.0058 0.0245 0.0000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flltbrown\n" +
    "Ka 0.1306 0.0538 0.0250\n" +
    "Kd 0.2776 0.1143 0.0531\n" +
    "illum 1\n" +
    "\n" +
    "newmtl tan\n" +
    "Ka 0.4000 0.3121 0.1202\n" +
    "Kd 0.6612 0.5221 0.2186\n" +
    "Ks 0.5020 0.4118 0.2152\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl fltan\n" +
    "Ka 0.4000 0.3121 0.1202\n" +
    "Kd 0.6612 0.4567 0.1295\n" +
    "illum 1\n" +
    "\n" +
    "newmtl brzskin\n" +
    "Ka 0.4408 0.2694 0.1592\n" +
    "Kd 0.3796 0.2898 0.2122\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 25.0000\n" +
    "\n" +
    "newmtl lips\n" +
    "Ka 0.4408 0.2694 0.1592\n" +
    "Kd 0.9265 0.2612 0.2898\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 25.0000\n" +
    "\n" +
    "newmtl redorange\n" +
    "Ka 0.3918 0.0576 0.0000\n" +
    "Kd 0.7551 0.0185 0.0000\n" +
    "Ks 0.4694 0.3224 0.1667\n" +
    "illum 2\n" +
    "Ns 132.5600\n" +
    "\n" +
    "newmtl blutan\n" +
    "Ka 0.4408 0.2694 0.1592\n" +
    "Kd 0.0776 0.2571 0.2041\n" +
    "Ks 0.1467 0.1469 0.0965\n" +
    "illum 2\n" +
    "Ns 25.0000\n" +
    "\n" +
    "newmtl bluteal\n" +
    "Ka 0.0041 0.1123 0.1224\n" +
    "Kd 0.0776 0.2571 0.2041\n" +
    "Ks 0.1467 0.1469 0.0965\n" +
    "illum 2\n" +
    "Ns 25.0000\n" +
    "\n" +
    "newmtl pinktan\n" +
    "Ka 0.4408 0.2694 0.1592\n" +
    "Kd 0.6857 0.2571 0.2163\n" +
    "Ks 0.1467 0.1469 0.0965\n" +
    "illum 2\n" +
    "Ns 25.0000\n" +
    "\n" +
    "newmtl brnhair\n" +
    "Ka 0.0612 0.0174 0.0066\n" +
    "Kd 0.0898 0.0302 0.0110\n" +
    "Ks 0.1306 0.0819 0.0352\n" +
    "illum 2\n" +
    "Ns 60.4700\n" +
    "\n" +
    "newmtl blondhair\n" +
    "Ka 0.4449 0.2632 0.0509\n" +
    "Kd 0.5714 0.3283 0.0443\n" +
    "Ks 0.7755 0.4602 0.0918\n" +
    "illum 2\n" +
    "Ns 4.6500\n" +
    "\n" +
    "newmtl flblonde\n" +
    "Ka 0.4449 0.2632 0.0509\n" +
    "Kd 0.5714 0.3283 0.0443\n" +
    "illum 1\n" +
    "\n" +
    "newmtl yelloworng\n" +
    "Ka 0.5837 0.1715 0.0000\n" +
    "Kd 0.8857 0.2490 0.0000\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl bone\n" +
    "Ka 0.3061 0.1654 0.0650\n" +
    "Kd 0.9000 0.7626 0.4261\n" +
    "Ks 0.8939 0.7609 0.5509\n" +
    "illum 2\n" +
    "Ns 200.0000\n" +
    "\n" +
    "newmtl teeth\n" +
    "Ka 0.6408 0.5554 0.3845\n" +
    "Kd 0.9837 0.7959 0.4694\n" +
    "illum 1\n" +
    "\n" +
    "newmtl brass\n" +
    "Ka 0.2490 0.1102 0.0000\n" +
    "Kd 0.4776 0.1959 0.0000\n" +
    "Ks 0.5796 0.5796 0.5796\n" +
    "illum 2\n" +
    "Ns 134.8800\n" +
    "\n" +
    "newmtl dkred\n" +
    "Ka 0.0939 0.0000 0.0000\n" +
    "Kd 0.2286 0.0000 0.0000\n" +
    "Ks 0.2490 0.0000 0.0000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl taupe\n" +
    "Ka 0.1061 0.0709 0.0637\n" +
    "Kd 0.2041 0.1227 0.1058\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 84.5000\n" +
    "\n" +
    "newmtl dkteal\n" +
    "Ka 0.0000 0.0245 0.0163\n" +
    "Kd 0.0000 0.0653 0.0449\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 55.0400\n" +
    "\n" +
    "newmtl dkdkgrey\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.0122 0.0122 0.0122\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl dkblue\n" +
    "Ka 0.0000 0.0029 0.0408\n" +
    "Kd 0.0000 0.0041 0.0571\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl gold\n" +
    "Ka 0.7224 0.1416 0.0000\n" +
    "Kd 1.0000 0.4898 0.0000\n" +
    "Ks 0.7184 0.3695 0.3695\n" +
    "illum 2\n" +
    "Ns 123.2600\n" +
    "\n" +
    "newmtl redbrick\n" +
    "Ka 0.1102 0.0067 0.0067\n" +
    "Kd 0.3306 0.0398 0.0081\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flmustard\n" +
    "Ka 0.4245 0.2508 0.0000\n" +
    "Kd 0.8898 0.3531 0.0073\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flpinegreen\n" +
    "Ka 0.0367 0.0612 0.0204\n" +
    "Kd 0.1061 0.2163 0.0857\n" +
    "illum 1\n" +
    "\n" +
    "newmtl fldkred\n" +
    "Ka 0.0939 0.0000 0.0000\n" +
    "Kd 0.2286 0.0082 0.0082\n" +
    "illum 1\n" +
    "\n" +
    "newmtl fldkgreen2\n" +
    "Ka 0.0025 0.0122 0.0014\n" +
    "Kd 0.0245 0.0694 0.0041\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flmintgreen\n" +
    "Ka 0.0408 0.1429 0.0571\n" +
    "Kd 0.1306 0.2898 0.1673\n" +
    "illum 1\n" +
    "\n" +
    "newmtl olivegreen\n" +
    "Ka 0.0167 0.0245 0.0000\n" +
    "Kd 0.0250 0.0367 0.0000\n" +
    "Ks 0.2257 0.2776 0.1167\n" +
    "illum 2\n" +
    "Ns 97.6700\n" +
    "\n" +
    "newmtl skin\n" +
    "Ka 0.2286 0.0187 0.0187\n" +
    "Kd 0.1102 0.0328 0.0139\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 17.8300\n" +
    "\n" +
    "newmtl redbrown\n" +
    "Ka 0.1469 0.0031 0.0000\n" +
    "Kd 0.2816 0.0060 0.0000\n" +
    "Ks 0.3714 0.3714 0.3714\n" +
    "illum 2\n" +
    "Ns 141.0900\n" +
    "\n" +
    "newmtl deepgreen\n" +
    "Ka 0.0000 0.0050 0.0000\n" +
    "Kd 0.0000 0.0204 0.0050\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 113.1800\n" +
    "\n" +
    "newmtl flltolivegreen\n" +
    "Ka 0.0167 0.0245 0.0000\n" +
    "Kd 0.0393 0.0531 0.0100\n" +
    "illum 1\n" +
    "\n" +
    "newmtl jetflame\n" +
    "Ka 0.7714 0.0000 0.0000\n" +
    "Kd 0.9510 0.4939 0.0980\n" +
    "Ks 0.8531 0.5222 0.0000\n" +
    "illum 2\n" +
    "Ns 132.5600\n" +
    "\n" +
    "newmtl brownskn\n" +
    "Ka 0.0122 0.0041 0.0000\n" +
    "Kd 0.0204 0.0082 0.0000\n" +
    "Ks 0.0735 0.0508 0.0321\n" +
    "illum 2\n" +
    "Ns 20.1600\n" +
    "\n" +
    "newmtl greenskn\n" +
    "Ka 0.0816 0.0449 0.0000\n" +
    "Kd 0.0000 0.0735 0.0000\n" +
    "Ks 0.0490 0.1224 0.0898\n" +
    "illum 3\n" +
    "Ns 46.5100\n" +
    "sharpness 146.5100\n" +
    "\n" +
    "newmtl ltgrey\n" +
    "Ka 0.5000 0.5000 0.5000\n" +
    "Kd 0.3837 0.3837 0.3837\n" +
    "Ks 0.5000 0.5000 0.5000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl bronze\n" +
    "Ka 0.0449 0.0204 0.0000\n" +
    "Kd 0.0653 0.0367 0.0122\n" +
    "Ks 0.0776 0.0408 0.0000\n" +
    "illum 3\n" +
    "Ns 137.2100\n" +
    "sharpness 125.5800\n" +
    "\n" +
    "newmtl bone1\n" +
    "Ka 0.6408 0.5554 0.3845\n" +
    "Kd 0.9837 0.7959 0.4694\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flwhite1\n" +
    "Ka 0.9306 0.9306 0.9306\n" +
    "Kd 1.0000 1.0000 1.0000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flwhite\n" +
    "Ka 0.6449 0.6116 0.5447\n" +
    "Kd 0.9837 0.9309 0.8392\n" +
    "Ks 0.8082 0.7290 0.5708\n" +
    "illum 2\n" +
    "Ns 200.0000\n" +
    "\n" +
    "newmtl shadow\n" +
    "Kd 0.0350 0.0248 0.0194\n" +
    "illum 0\n" +
    "d 0.7500\n" +
    "\n" +
    "newmtl fldkolivegreen\n" +
    "Ka 0.0056 0.0082 0.0000\n" +
    "Kd 0.0151 0.0204 0.0038\n" +
    "illum 1\n" +
    "\n" +
    "newmtl fldkdkgrey\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.0122 0.0122 0.0122\n" +
    "illum 1\n" +
    "\n" +
    "newmtl lcdgreen\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 0.5878 1.0000 0.5061\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl brownlips\n" +
    "Ka 0.1143 0.0694 0.0245\n" +
    "Kd 0.1429 0.0653 0.0408\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 25.0000\n" +
    "\n" +
    "newmtl muscle\n" +
    "Ka 0.2122 0.0077 0.0154\n" +
    "Kd 0.4204 0.0721 0.0856\n" +
    "Ks 0.1184 0.1184 0.1184\n" +
    "illum 2\n" +
    "Ns 25.5800\n" +
    "\n" +
    "newmtl flltgrey\n" +
    "Ka 0.5224 0.5224 0.5224\n" +
    "Kd 0.8245 0.8245 0.8245\n" +
    "illum 1\n" +
    "\n" +
    "newmtl offwhite.warm\n" +
    "Ka 0.5184 0.4501 0.3703\n" +
    "Kd 0.8367 0.6898 0.4490\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl offwhite.cool\n" +
    "Ka 0.5184 0.4501 0.3703\n" +
    "Kd 0.8367 0.6812 0.5703\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl yellowbrt\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 1.0000 0.7837 0.0000\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl chappie\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 0.5837 0.1796 0.0367\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl archwhite\n" +
    "Ka 0.2816 0.2816 0.2816\n" +
    "Kd 0.9959 0.9959 0.9959\n" +
    "illum 1\n" +
    "\n" +
    "newmtl archwhite2\n" +
    "Ka 0.2816 0.2816 0.2816\n" +
    "Kd 0.8408 0.8408 0.8408\n" +
    "illum 1\n" +
    "\n" +
    "newmtl lighttan\n" +
    "Ka 0.0980 0.0536 0.0220\n" +
    "Kd 0.7020 0.4210 0.2206\n" +
    "Ks 0.8286 0.8057 0.5851\n" +
    "illum 2\n" +
    "Ns 177.5200\n" +
    "\n" +
    "newmtl lighttan2\n" +
    "Ka 0.0980 0.0492 0.0144\n" +
    "Kd 0.3143 0.1870 0.0962\n" +
    "Ks 0.8286 0.8057 0.5851\n" +
    "illum 2\n" +
    "Ns 177.5200\n" +
    "\n" +
    "newmtl lighttan3\n" +
    "Ka 0.0980 0.0492 0.0144\n" +
    "Kd 0.1796 0.0829 0.0139\n" +
    "Ks 0.8286 0.8057 0.5851\n" +
    "illum 2\n" +
    "Ns 177.5200\n" +
    "\n" +
    "newmtl lightyellow\n" +
    "Ka 0.5061 0.1983 0.0000\n" +
    "Kd 1.0000 0.9542 0.3388\n" +
    "Ks 1.0000 0.9060 0.0000\n" +
    "illum 2\n" +
    "Ns 177.5200\n" +
    "\n" +
    "newmtl lighttannew\n" +
    "Ka 0.0980 0.0492 0.0144\n" +
    "Kd 0.7878 0.6070 0.3216\n" +
    "Ks 0.8286 0.8057 0.5851\n" +
    "illum 2\n" +
    "Ns 177.5200\n" +
    "\n" +
    "newmtl default\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 0.7102 0.7020 0.6531\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 128.0000\n" +
    "\n" +
    "newmtl ship2\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 1.0000 1.0000 1.0000\n" +
    "Ks 0.1143 0.1143 0.1143\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl dkpurple\n" +
    "Ka 0.0082 0.0000 0.0163\n" +
    "Kd 0.0245 0.0000 0.0490\n" +
    "Ks 0.1266 0.0000 0.2531\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl dkorange\n" +
    "Ka 0.4041 0.0123 0.0000\n" +
    "Kd 0.7143 0.0350 0.0000\n" +
    "Ks 0.7102 0.0870 0.0000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl mintgrn\n" +
    "Ka 0.0101 0.1959 0.0335\n" +
    "Kd 0.0245 0.4776 0.0816\n" +
    "Ks 0.0245 0.4776 0.0816\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl fgreen\n" +
    "Ka 0.0000 0.0449 0.0000\n" +
    "Kd 0.0000 0.0449 0.0004\n" +
    "Ks 0.0062 0.0694 0.0000\n" +
    "illum 2\n" +
    "Ns 106.2000\n" +
    "\n" +
    "newmtl glassblutint\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 0.5551 0.8000 0.7730\n" +
    "Ks 0.7969 0.9714 0.9223\n" +
    "illum 4\n" +
    "d 0.3300\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl bflesh\n" +
    "Ka 0.0122 0.0122 0.0122\n" +
    "Kd 0.0245 0.0081 0.0021\n" +
    "Ks 0.0531 0.0460 0.0153\n" +
    "illum 2\n" +
    "Ns 20.1600\n" +
    "\n" +
    "newmtl meh\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 0.5551 0.8000 0.7730\n" +
    "Ks 0.7969 0.9714 0.9223\n" +
    "illum 4\n" +
    "d 0.7500\n" +
    "Ns 183.7200\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl violet\n" +
    "Ka 0.0083 0.0000 0.1265\n" +
    "Kd 0.0287 0.0269 0.1347\n" +
    "Ks 0.2267 0.4537 0.6612\n" +
    "illum 2\n" +
    "Ns 96.9000\n" +
    "\n" +
    "newmtl iris\n" +
    "Ka 0.3061 0.0556 0.0037\n" +
    "Kd 0.0000 0.0572 0.3184\n" +
    "Ks 0.8041 0.6782 0.1477\n" +
    "illum 2\n" +
    "Ns 188.3700\n" +
    "\n" +
    "newmtl blugrn\n" +
    "Ka 0.4408 0.4144 0.1592\n" +
    "Kd 0.0811 0.6408 0.2775\n" +
    "Ks 0.1467 0.1469 0.0965\n" +
    "illum 2\n" +
    "Ns 25.0000\n" +
    "\n" +
    "newmtl glasstransparent\n" +
    "Ka 0.2163 0.2163 0.2163\n" +
    "Kd 0.4694 0.4694 0.4694\n" +
    "Ks 0.6082 0.6082 0.6082\n" +
    "illum 4\n" +
    "d 0.7500\n" +
    "Ns 200.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl fleshtransparent\n" +
    "Ka 0.4000 0.2253 0.2253\n" +
    "Kd 0.6898 0.2942 0.1295\n" +
    "Ks 0.7388 0.4614 0.4614\n" +
    "illum 4\n" +
    "d 0.7500\n" +
    "Ns 6.2000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl fldkgrey\n" +
    "Ka 0.0449 0.0449 0.0449\n" +
    "Kd 0.0939 0.0939 0.0939\n" +
    "illum 1\n" +
    "\n" +
    "newmtl sky_blue\n" +
    "Ka 0.1363 0.2264 0.4122\n" +
    "Kd 0.1241 0.5931 0.8000\n" +
    "Ks 0.0490 0.0490 0.0490\n" +
    "illum 2\n" +
    "Ns 13.9500\n" +
    "\n" +
    "newmtl fldkpurple\n" +
    "Ka 0.0443 0.0257 0.0776\n" +
    "Kd 0.1612 0.0000 0.3347\n" +
    "Ks 0.0000 0.0000 0.0000\n" +
    "illum 2\n" +
    "Ns 13.9500\n" +
    "\n" +
    "newmtl dkbrown\n" +
    "Ka 0.0143 0.0062 0.0027\n" +
    "Kd 0.0087 0.0038 0.0016\n" +
    "Ks 0.2370 0.2147 0.1821\n" +
    "illum 3\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl bone2\n" +
    "Ka 0.6408 0.5388 0.3348\n" +
    "Kd 0.9837 0.8620 0.6504\n" +
    "illum 1\n" +
    "\n" +
    "newmtl bluegrey\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 0.1881 0.2786 0.2898\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 14.7300\n" +
    "\n" +
    "newmtl metal\n" +
    "Ka 0.9102 0.8956 0.1932\n" +
    "Kd 0.9000 0.7626 0.4261\n" +
    "Ks 0.8939 0.8840 0.8683\n" +
    "illum 2\n" +
    "Ns 200.0000\n" +
    "\n" +
    "newmtl sand_stone\n" +
    "Ka 0.1299 0.1177 0.0998\n" +
    "Kd 0.1256 0.1138 0.0965\n" +
    "Ks 0.2370 0.2147 0.1821\n" +
    "illum 3\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl hair\n" +
    "Ka 0.0013 0.0012 0.0010\n" +
    "Kd 0.0008 0.0007 0.0006\n" +
    "Ks 0.0000 0.0000 0.0000\n" +
    "illum 3\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n";
}
