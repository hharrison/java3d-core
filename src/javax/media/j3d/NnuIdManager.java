/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

class NnuIdManager {
    static int nnuId = 0;
    
    final static int getId() {
	if(nnuId == Integer.MAX_VALUE) {
	    nnuId = 0;
	}
	
	return nnuId++;
    }

    final static int equals(NnuId nnuIdArr[], NnuId key, int start, int end) {
 	int mid;
	
 	mid = start +((end - start)/ 2);
 	if(nnuIdArr[mid] != null) {
	    int test = key.equal(nnuIdArr[mid]);

 	    if((test < 0) && (start != mid))
 		return equals(nnuIdArr, key, start, mid);
 	    else if((test > 0) && (start != mid))
		return equals(nnuIdArr, key, mid, end);
 	    else if(test == 0) {
		// Since id is not necessary unique, we've to do
		// some extra check.
		
		// check for equal reference.
		if(key == nnuIdArr[mid]) {
		    return mid;	     
		}
		
		int temp = mid - 1;
		// Look to the left.
		while((temp >= start) && (key.equal(nnuIdArr[temp]) == 0)) {
		    if(key == nnuIdArr[temp]) {
			return temp;	     
		    }
		    temp--;
		}
		
		// Look to the right.
		temp = mid + 1;
		while((temp < end) && (key.equal(nnuIdArr[temp]) == 0)) {
		    if(key == nnuIdArr[temp]) {
			return temp;	     
		    }
		    temp++;
		}
		
		// Fail equal reference check. 
		return -1;
	    }
 	    else
 		return -1;
	}
 	// A null NnuId object encountered.
 	return  -2;
    }

    final static boolean equals(NnuId nnuIdArr[], NnuId key, int[] index,
				int start, int end) {

 	int mid;
	
 	mid = start +((end - start)/ 2);

	if(nnuIdArr[mid] != null) {
 	    int test = key.equal(nnuIdArr[mid]);
	    
 	    if(start != mid) {
 		if(test < 0) {
		    return equals(nnuIdArr, key, index, start, mid);
		}
 		else if(test > 0) {
		    return equals(nnuIdArr, key, index, mid, end);
 		}
 	    }
 	    else { // (start == mid)
 		if(test < 0) {
 		    index[0] = mid;
 		    return false;
 		}
 		else if(test > 0) { 
 		    index[0] = mid+1;
 		    return false;
 		}
 	    }
	    
 	    // (test == 0)
	    // Since id is not necessary unique, we've to do
	    // some extra check.
	    
	    // check for equal reference.
	    if(key == nnuIdArr[mid]) {
		index[0] = mid;
		return true;
	    }
		
	    int temp = mid - 1;
	    // Look to the left.
	    while((temp >= start) && (key.equal(nnuIdArr[temp]) == 0)) {
		if(key == nnuIdArr[temp]) {
		    index[0] = temp;
		    return true;
		}
		temp--;
	    }
	    
	    // Look to the right.
	    temp = mid + 1;
	    while((temp < end) && (key.equal(nnuIdArr[temp]) == 0)) {
		if(key == nnuIdArr[temp]) {
		    index[0] = temp;
		    return true;
		}
		temp++;
	    }
	    
	    // Fail equal reference check. 
	    index[0] = temp;
	    return false;
	    
 	}
 	// A null entry encountered.
 	// But we still want to return the index where we encounter it.
 	index[0] = mid;
 	return  false;
    }

    final static void sort(NnuId nnuIdArr[]) {
	if (nnuIdArr.length < 20) {
	    insertSort(nnuIdArr);
	} else {
	    quicksort(nnuIdArr, 0, nnuIdArr.length-1);
	}
    }
    
    // Insertion sort on smaller array
    final static void insertSort(NnuId nnuIdArr[]) {

	
	for (int i=0; i<nnuIdArr.length; i++) {    
	    for (int j=i; j>0 && 
		     (nnuIdArr[j-1].getId() > nnuIdArr[j].getId()); j--) {
		NnuId temp = nnuIdArr[j];
		nnuIdArr[j] = nnuIdArr[j-1];
		nnuIdArr[j-1] = temp;
	    }
	}
    }

    final static void quicksort( NnuId nnuIdArr[], int l, int r ) {
	int i = l;
	int j = r;
	int k = nnuIdArr[(l+r) / 2].getId();
	
	do {
	    while (nnuIdArr[i].getId() < k) i++;
	    while (k < nnuIdArr[j].getId()) j--;
	    if (i<=j) {
		NnuId tmp = nnuIdArr[i];
		nnuIdArr[i] = nnuIdArr[j];
		nnuIdArr[j] = tmp;
		
		i++;
		j--;
	    }
	} while (i<=j);
	
	if (l<j) quicksort(nnuIdArr, l,j);
	if (l<r) quicksort(nnuIdArr, i,r);
    }
    


    // This method assumes that nnuIdArr0 and nnuIdArr1 are sorted.
    final static NnuId[] delete( NnuId nnuIdArr0[], NnuId nnuIdArr1[] ) {
	
	int i, index, len;
	int curStart =0, newStart =0;
	boolean found = false;

	int size = nnuIdArr0.length - nnuIdArr1.length;

	if(size > 0) {
	    NnuId newNnuIdArr[] = new NnuId[size];
	    
            for (i = 0; i < nnuIdArr1.length; i++) {
                index = equals(nnuIdArr0, nnuIdArr1[i], 0, nnuIdArr0.length);

                if (index >= 0) {
                    found = true;
                    if ((i < (nnuIdArr1.length - 1)) && nnuIdArr1[i].getId() == nnuIdArr1[i + 1].getId()) {
                        // Remove element from original array
                        NnuId[] tmpNnuIdArr0 = new NnuId[nnuIdArr0.length - 1];
                        System.arraycopy(nnuIdArr0, 0, tmpNnuIdArr0, 0, index);
                        System.arraycopy(nnuIdArr0, index + 1,
                                    tmpNnuIdArr0, index, nnuIdArr0.length - index - 1);
                        nnuIdArr0 = tmpNnuIdArr0;
                    } else {
                        // Copy elements from original array to new array up to
                        // but not including the element we are removing
                        if (index == curStart) {
                            curStart++;
                        } else {
                            len = index - curStart;
                            System.arraycopy(nnuIdArr0, curStart,
                                    newNnuIdArr, newStart, len);

                            curStart = index + 1;
                            newStart = newStart + len;
                        }
                    }
                } else {
                    found = false;
                    MasterControl.getCoreLogger().severe("Can't Find matching nnuId.");
                }
            }

	    if((found == true) && (curStart < nnuIdArr0.length)) {
		len = nnuIdArr0.length - curStart;
		System.arraycopy(nnuIdArr0, curStart, newNnuIdArr, newStart, len);
	    }

	    return newNnuIdArr;
	}
	else if( size == 0) {
	    // Remove all.
	}
	else {
	    // We are in trouble !!!
            MasterControl.getCoreLogger().severe("Attempt to remove more elements than are present");
	}

	return null;

    }

    
    // This method assumes that nnuIdArr0 and nnuIdArr1 are sorted.
    final static NnuId[] merge( NnuId nnuIdArr0[], NnuId nnuIdArr1[] ) {

	int index[] = new int[1];
	int indexPlus1, blkSize, i, j;
	
	int size = nnuIdArr0.length + nnuIdArr1.length;
	
	NnuId newNnuIdArr[] = new NnuId[size];
	
	// Copy the nnuIdArr0 data into the newly created newNnuIdArr.
	System.arraycopy(nnuIdArr0, 0, newNnuIdArr, 0, nnuIdArr0.length);
	
	for(i=nnuIdArr0.length, j=0; i<size; i++, j++) {
	    // True or false, it doesn't matter.
	    equals((NnuId[])newNnuIdArr, nnuIdArr1[j], index, 0, i);
	    
	    if(index[0] == i) { // Append to last.
		newNnuIdArr[i] = nnuIdArr1[j];
	    }
	    else { // Insert in between array elements.
		indexPlus1 = index[0] + 1;
		blkSize = i - index[0];

		// Shift the later portion of array elements by one position.
		// This is the make room for the new data entry.
		System.arraycopy(newNnuIdArr, index[0], newNnuIdArr,
				 indexPlus1, blkSize);
		
		newNnuIdArr[index[0]] = nnuIdArr1[j];
	    }
	    
	}

	return newNnuIdArr;
	
    }

    
    final static void replace(NnuId oldObj, NnuId newObj, NnuId nnuIdArr[]) {


	int[] index = new int[1];
	int lenLess1 = nnuIdArr.length - 1;
	int blkSize;

	// delete old from nnuIdArr.
	index[0] = equals(nnuIdArr, oldObj, 0, nnuIdArr.length);
	if(index[0] == lenLess1) {
	    nnuIdArr[index[0]] = null;
	}
	else if(index[0] >= 0) {
	    blkSize = lenLess1 - index[0];
	    System.arraycopy(nnuIdArr, index[0]+1,
			     nnuIdArr, index[0], blkSize);	
	    nnuIdArr[lenLess1] = null;
	}
	else {
	    MasterControl.getCoreLogger().severe("Can't Find matching nnuId.");
	}

	// insert new to nnuIdArr.
	equals(nnuIdArr, newObj, index, 0, lenLess1);
	    
	if(index[0] == lenLess1) { // Append to last.
	    nnuIdArr[index[0]] = newObj;
	}
	else { // Insert in between array elements.
	    blkSize = lenLess1 - index[0];

	    // Shift the later portion of array elements by one position.
	    // This is the make room for the new data entry.
	    System.arraycopy(nnuIdArr, index[0], nnuIdArr,
			     index[0]+1, blkSize);
	    
	    nnuIdArr[index[0]] = newObj;
	}
	

    }

    
    final static void printIds(NnuId nnuIdArr[]) {
	for(int i=0; i<nnuIdArr.length; i++) {
	    System.err.println("[" + i +"] is " + nnuIdArr[i].getId());
	}

    }

}
