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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.media.j3d.Appearance;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Material;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;

import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.utils.image.ImageException;
import com.sun.j3d.utils.image.TextureLoader;


class ObjectFileMaterials implements ImageObserver {
    // DEBUG
    // 1 = Name of materials
    // 16 = Tokens
    private static final int DEBUG = 0;

    private String curName = null;
    private ObjectFileMaterial cur = null;

    private HashMap materials;			// key=String name of material
						// value=ObjectFileMaterial

    private String basePath;
    private boolean fromUrl;

    private class ObjectFileMaterial {

	public Color3f Ka;
	public Color3f Kd;
	public Color3f Ks;
	public int illum;
	public float Ns;
	public Texture2D t;
	public boolean transparent;
	public float transparencyLevel;
        
    }


    void assignMaterial(String matName, Shape3D shape) {
	ObjectFileMaterial p = null;

	if ((DEBUG & 1) != 0) System.out.println("Color " + matName);
    
	Material m = new Material();
	p = (ObjectFileMaterial)materials.get(matName);
	Appearance a = new Appearance();

	if (p != null) {
	    // Set ambient & diffuse color
	    if (p.Ka != null) m.setAmbientColor(p.Ka);
	    if (p.Kd != null) m.setDiffuseColor(p.Kd);

	    // Set specular color
	    if ((p.Ks != null) && (p.illum != 1)) m.setSpecularColor(p.Ks);
	    else if (p.illum == 1) m.setSpecularColor(0.0f, 0.0f, 0.0f);

	    if (p.illum >= 1) m.setLightingEnable(true);
	    else if (p.illum == 0) m.setLightingEnable(false);

	    if (p.Ns != -1.0f) m.setShininess(p.Ns);

	    if (p.t != null) {
		a.setTexture(p.t);
		// Create Texture Coordinates if not already present
		if ((((GeometryArray)shape.getGeometry()).getVertexFormat() &
		     GeometryArray.TEXTURE_COORDINATE_2) == 0) {
		    TexCoordGeneration tcg = new TexCoordGeneration();
		    a.setTexCoordGeneration(tcg);
		}
	    }

	    if (p.transparent) 
		a.setTransparencyAttributes(
		    new TransparencyAttributes(TransparencyAttributes.NICEST,
					       p.transparencyLevel));
	}
	a.setMaterial(m);
	if ((DEBUG & 1) != 0) System.out.println(m);
	shape.setAppearance(a);
    } // End of assignMaterial


    private void readName(ObjectFileParser st) throws ParsingErrorException {
	st.getToken();

	if (st.ttype == ObjectFileParser.TT_WORD) {

	    if (curName != null) materials.put(curName, cur);
	    curName = new String(st.sval);
	    cur = new ObjectFileMaterial();
	}
	st.skipToNextLine();
    } // End of readName


    private void readAmbient(ObjectFileParser st) throws ParsingErrorException {
	Color3f p = new Color3f();

	st.getNumber();
	p.x = (float)st.nval;
	st.getNumber();
	p.y = (float)st.nval;
	st.getNumber();
	p.z = (float)st.nval;

	cur.Ka = p;

	st.skipToNextLine();
    } // End of readAmbient


    private void readDiffuse(ObjectFileParser st) throws ParsingErrorException {
	Color3f p = new Color3f();

	st.getNumber();
	p.x = (float)st.nval;
	st.getNumber();
	p.y = (float)st.nval;
	st.getNumber();
	p.z = (float)st.nval;

	cur.Kd = p;

	st.skipToNextLine();
    } // End of readDiffuse


    private void readSpecular(ObjectFileParser st) throws ParsingErrorException {
	Color3f p = new Color3f();

	st.getNumber();
	p.x = (float)st.nval;
	st.getNumber();
	p.y = (float)st.nval;
	st.getNumber();
	p.z = (float)st.nval;

	cur.Ks = p;

	st.skipToNextLine();
    } // End of readSpecular


    private void readIllum(ObjectFileParser st) throws ParsingErrorException {

	st.getNumber();
	cur.illum = (int)st.nval;

	st.skipToNextLine();
    } // End of readSpecular

    private void readTransparency(ObjectFileParser st) throws ParsingErrorException {

	st.getNumber();
	cur.transparencyLevel = (float)st.nval;
	if ( cur.transparencyLevel < 1.0f ){
		cur.transparent = true;
	}
	st.skipToNextLine();
    } // End of readTransparency

    private void readShininess(ObjectFileParser st) throws ParsingErrorException {
	float f;

	st.getNumber();
	cur.Ns = (float)st.nval;
	if (cur.Ns < 1.0f) cur.Ns = 1.0f;
	else if (cur.Ns > 128.0f) cur.Ns = 128.0f;

	st.skipToNextLine();
    } // End of readSpecular


    public void readMapKd(ObjectFileParser st) {
	// Filenames are case sensitive
	st.lowerCaseMode(false);

	// Get name of texture file (skip path)
	String tFile = null;
	do {
	    st.getToken();
	    if (st.ttype == ObjectFileParser.TT_WORD) tFile = st.sval;
	} while (st.ttype != ObjectFileParser.TT_EOL);

	st.lowerCaseMode(true);

	if (tFile != null) {
	    // Check for filename with no extension
	    if (tFile.lastIndexOf('.') != -1) {
		try {
		    // Convert filename to lower case for extension comparisons
		    String suffix =
			tFile.substring(tFile.lastIndexOf('.') + 1).toLowerCase();

		    TextureLoader t = null;

		    if ((suffix.equals("int")) || (suffix.equals("inta")) ||
			(suffix.equals("rgb")) || (suffix.equals("rgba")) ||
			(suffix.equals("bw")) || (suffix.equals("sgi"))) {
			RgbFile f;
			if (fromUrl) {
			    f = new RgbFile(new URL(basePath + tFile).openStream());
			} else {
			    f = new RgbFile(new FileInputStream(basePath + tFile));
			}
			BufferedImage bi = f.getImage();

			boolean luminance = suffix.equals("int") || suffix.equals("inta");
			boolean alpha = suffix.equals("inta") || suffix.equals("rgba");
			cur.transparent = alpha;

			String s = null;
			if (luminance && alpha) s = "LUM8_ALPHA8";
			else if (luminance) s = "LUMINANCE";
			else if (alpha) s = "RGBA";
			else s = "RGB";

			t = new TextureLoader(bi, s, TextureLoader.GENERATE_MIPMAP);
		    } else {
			// For all other file types, use the TextureLoader
			if (fromUrl) {
			    t = new TextureLoader(new URL(basePath + tFile), "RGB",
						  TextureLoader.GENERATE_MIPMAP, null);
			} else {
			    t = new TextureLoader(basePath + tFile, "RGB",
						  TextureLoader.GENERATE_MIPMAP, null);
			}
		    }
		    Texture2D texture = (Texture2D)t.getTexture();
		    if (texture != null) cur.t = texture;
		}
		catch (FileNotFoundException e) {
		    // Texture won't get loaded if file can't be found
		}
		catch (MalformedURLException e) {
		    // Texture won't get loaded if file can't be found
		}
		catch (IOException e) {
		    // Texture won't get loaded if file can't be found
		} // mcneillk: issue 639
		catch (ImageException iex) {
		    // Texture won't get loaded if other problem
		}
	    }
	}
	st.skipToNextLine();
    } // End of readMapKd


    private void readFile(ObjectFileParser st) throws ParsingErrorException {
	int t;
	st.getToken();
	while (st.ttype != ObjectFileParser.TT_EOF) {

	    // Print out one token for each line
	    if ((DEBUG & 16) != 0) {
		System.out.print("Token ");
		if (st.ttype == ObjectFileParser.TT_EOL) System.out.println("EOL");
		else if (st.ttype == ObjectFileParser.TT_WORD)
		    System.out.println(st.sval);
		else System.out.println((char)st.ttype);
	    }

	    if (st.ttype == ObjectFileParser.TT_WORD) {
		if (st.sval.equals("newmtl")) {
		    readName(st);
		} else if (st.sval.equals("ka")) {
		    readAmbient(st);
		} else if (st.sval.equals("kd")) {
		    readDiffuse(st);
		} else if (st.sval.equals("ks")) {
		    readSpecular(st);
		} else if (st.sval.equals("illum")) {
		    readIllum(st);
		} else if (st.sval.equals("d")) {
		    readTransparency(st);
		} else if (st.sval.equals("ns")) {
		    readShininess(st);
		} else if (st.sval.equals("tf")) {
		    st.skipToNextLine();
		} else if (st.sval.equals("sharpness")) {
		    st.skipToNextLine();
		} else if (st.sval.equals("map_kd")) {
		    readMapKd(st);
		} else if (st.sval.equals("map_ka")) {
		    st.skipToNextLine();
		} else if (st.sval.equals("map_ks")) {
		    st.skipToNextLine();
		} else if (st.sval.equals("map_ns")) {
		    st.skipToNextLine();
		} else if (st.sval.equals("bump")) {
		    st.skipToNextLine();
		}
	    }

	    st.skipToNextLine();

	    // Get next token
	    st.getToken();
	}
	if (curName != null) materials.put(curName, cur);
    } // End of readFile


    void readMaterialFile(boolean fromUrl, String basePath, String fileName)
		throws ParsingErrorException {

	Reader reader;

	this.basePath = basePath;
	this.fromUrl = fromUrl;

	try {
	    if (fromUrl) {
		reader = (Reader)
		    (new InputStreamReader(
			new BufferedInputStream(
			    (new URL(basePath + fileName).openStream()))));
	    } else {
		reader = new BufferedReader(new FileReader(basePath + fileName));
	    }
	}
	catch (IOException e) {
	    // couldn't find it - ignore mtllib
	    return;
	}
	if ((DEBUG & 1) != 0)
	    System.out.println("Material file: " + basePath + fileName);

	ObjectFileParser st = new ObjectFileParser(reader);
	readFile(st);
    }  // End of readMaterialFile


    ObjectFileMaterials() throws ParsingErrorException {
	Reader reader = new StringReader(DefaultMaterials.materials);

	ObjectFileParser st = new ObjectFileParser(reader);
	materials = new HashMap(50);
	readFile(st);
    } // End of ObjectFileMaterials


    /**
     * Implement the ImageObserver interface.  Needed to load jpeg and gif
     * files using the Toolkit.
     */
    public boolean imageUpdate(Image img, int flags,
			       int x, int y, int w, int h) {

	return (flags & (ALLBITS | ABORT)) == 0;
    } // End of imageUpdate

} // End of class ObjectFileMaterials

// End of file ObjectFileMaterials.java
