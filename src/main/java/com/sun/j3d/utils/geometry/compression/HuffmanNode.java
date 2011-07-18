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

package com.sun.j3d.utils.geometry.compression;

import java.util.Collection;
import java.util.Comparator;

/**
 * Instances of this class are used as the nodes of binary trees representing
 * mappings of tags to compression stream elements.  Tags are descriptors
 * inserted into the compression command stream that specify the encoding of
 * immediately succeeding data elements.<p>
 *
 * The tag assignments in such a tree are computed from the paths taken from
 * the root to the leaf nodes.  Each leaf node represents the particular way
 * one or more compression stream elements wound up being encoded with respect
 * to various combinations of data lengths, shifts, and absolute/relative
 * status.<p>
 *
 * Huffman's algorithm for constructing binary trees with minimal weighted
 * path lengths can be used to optimize the bit lengths of the tags with
 * respect to the frequency of occurrence of their associated data encodings
 * in the compression stream.  The weighted path length is the sum of the
 * frequencies of all the leaf nodes times their path lengths to the root of
 * the tree.<p>
 *
 * The length of the longest tag determines the size of the table mapping tags
 * to data representations.  The geometry compression specification limits the
 * size of the table to 64 entries, so tags cannot be longer than 6 bits.  The
 * depth of the tree is reduced through a process of increasing the data
 * lengths of less frequently occuring nodes so they can be merged with other
 * more frequent nodes.
 */
class HuffmanNode {
    int tag, tagLength ;
    int shift, dataLength ;
    boolean absolute ;

    private int frequency ;
    private HuffmanNode child0, child1, mergeNode ;
    private boolean merged, unmergeable, cleared ;

    void clear() {
	tag = -1 ;
	tagLength = -1 ;

	shift = -1 ;
	dataLength = -1 ;
	absolute = false ;

	child0 = null ;
	child1 = null ;
	mergeNode = null ;

	frequency = 0 ;
	merged = false ;
	unmergeable = false ;
	cleared = true ;
    }

    HuffmanNode() {
	clear() ;
    }

    HuffmanNode(int length, int shift, boolean absolute) {
	this() ;
	set(length, shift, absolute) ;
    }

    final void set(int length, int shift, boolean absolute) {
	this.dataLength = length ;
	this.shift = shift ;
	this.absolute = absolute ;
	this.cleared = false ;
    }

    final boolean cleared() {
	return cleared ;
    }

    final void addCount() {
	frequency++ ;
    }

    final boolean hasCount() {
	return frequency > 0 ;
    }

    final boolean tokenEquals(HuffmanNode node) {
	return
	    this.absolute == node.absolute &&
	    this.dataLength == node.dataLength &&
	    this.shift == node.shift ;
    }

    void addChildren(HuffmanNode child0, HuffmanNode child1) {
	this.child0 = child0 ;
	this.child1 = child1 ;
	this.frequency = child0.frequency + child1.frequency ;
    }

    void collectLeaves(int tag, int tagLength, Collection collection) {
	if (child0 == null) {
	    this.tag = tag ;
	    this.tagLength = tagLength ;
	    collection.add(this) ;
	} else {
	    child0.collectLeaves((tag << 1) | 0, tagLength + 1, collection) ;
	    child1.collectLeaves((tag << 1) | 1, tagLength + 1, collection) ;
	}
    }

    boolean mergeInto(HuffmanNode node) {
	if (this.absolute == node.absolute) {
	    if (this.dataLength > node.dataLength)
		node.dataLength = this.dataLength ;

	    if (this.shift < node.shift)
		node.shift = this.shift ;

	    node.frequency += this.frequency ;
	    this.mergeNode = node ;
	    this.merged = true ;
	    return true ;

	} else
	    return false ;
    }
    
    int incrementLength() {
	if (shift > 0)
	    shift-- ;
	else
	    dataLength++ ;

	return dataLength - shift ;
    }

    final boolean merged() {
	return merged ;
    }

    final HuffmanNode getMergeNode() {
	return mergeNode ;
    }

    void setUnmergeable() {
	unmergeable = true ;
    }

    final boolean unmergeable() {
	return unmergeable ;
    }

    public String toString() {
	return
	    "shift " + shift + " data length " + dataLength +
	    (absolute? " absolute " : " relative ") +
	    "\ntag 0x" + Integer.toHexString(tag) + " tag length " + tagLength +
	    "\nfrequency: " + frequency ;
    }

    /**
     * Sorts nodes in ascending order by frequency.
     */
    static class FrequencyComparator implements Comparator {
	public final int compare(Object o1, Object o2) {
	    return ((HuffmanNode)o1).frequency - ((HuffmanNode)o2).frequency ;
	}
    }

    /**
     * Sorts nodes in descending order by tag bit length.
     */
    static class TagLengthComparator implements Comparator {
	public final int compare(Object o1, Object o2) {
	    return ((HuffmanNode)o2).tagLength - ((HuffmanNode)o1).tagLength ;
	}
    }

    static FrequencyComparator frequencyComparator = new FrequencyComparator() ;
    static TagLengthComparator tagLengthComparator = new TagLengthComparator() ;
}
