/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
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
	    
	    for(i=0; i<nnuIdArr1.length; i++) {
		index = equals(nnuIdArr0, nnuIdArr1[i], 0, nnuIdArr0.length);
		
		if(index >= 0) {
		    found = true;		
		    if(index == curStart) {
			curStart++;
		    }
		    else {
			len = index - curStart;
			System.arraycopy(nnuIdArr0, curStart,
					 newNnuIdArr, newStart, len);
		    
			curStart = index+1;
			newStart = newStart + len;
		    }
		}
		else {
		    found = false;
		    System.out.println("Can't Find matching nnuId.");
		    System.out.println("We're in TROUBLE!!!");
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
	    System.out.println("Can't Find matching nnuId.");
	    System.out.println("We're in TROUBLE!!!");
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
	    System.out.println("[" + i +"] is " + nnuIdArr[i].getId());
	}

    }

}
