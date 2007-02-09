/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

class HashKey extends Object {

    /**
     * The value is used for character storage.
     */
    char value[];

    /**
     * The count is the number of characters in the buffer.
     */
    int count = 0;

    HashKey() {
	this(16);
    }

    HashKey(int length) {
	value = new char[length];
    }

    HashKey(HashKey hashkey) {
	this.set(hashkey);
    }

    HashKey(String str) {
        this(str.length() + 16);
        append(str);
    }

    void set(HashKey hashkey) {
	int i;

	if (this.count < hashkey.count) {
	    this.value = new char[hashkey.count];
	}

	for (i=0; i<hashkey.count; i++) {
	    this.value[i] = hashkey.value[i];
	}
	this.count = hashkey.count;
    }

    void reset() {
	count = 0;
    }

    void ensureCapacity(int minimumCapacity) {
        int maxCapacity = value.length;

        if (minimumCapacity > maxCapacity) {
            int newCapacity = (maxCapacity + 1) * 2;
            if (minimumCapacity > newCapacity) {
                newCapacity = minimumCapacity;
            }

            char newValue[] = new char[newCapacity];
            System.arraycopy(value, 0, newValue, 0, count);
            value = newValue;
        }
    }

    HashKey append(String str) {
	int len = 0;

        if (str == null)
	    return this;

        len = str.length();
        ensureCapacity(count + len);
        str.getChars(0, len, value, count);
        count += len;
        return this;
    }

    public int hashCode() {
        int h = 0;
        int off = 0;
        char val[] = value;
        int len = count;

        if (len < 16) {
            for (int i = len ; i > 0; i--) {
                h = (h * 37) + val[off++];
            }
        } else {
            // only sample some characters
            int skip = len / 8;
            for (int i = len ; i > 0; i -= skip, off += skip) {
                h = (h * 39) + val[off];
            }
        }
        return h;
    }

    public boolean equals(Object anObject) {
        if ((anObject != null) && (anObject instanceof HashKey)) {
            HashKey anotherHashKey = (HashKey)anObject;
            int n = count;
            if (n == anotherHashKey.count) {
                char v1[] = value;
                char v2[] = anotherHashKey.value;;
                int i = 0;
                int j = 0;
                while (n-- != 0) {
                    if (v1[i++] != v2[j++]) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    
    /* For internal use only. */
    private int equals(HashKey hk) {
	int index = 0;
	
	while((index < count) && (index < hk.count)) {
	    if(value[index] < hk.value[index])
		return -1;
	    else if(value[index] > hk.value[index])
		return 1;
	    index++;
	}
	
	if(count == hk.count)
	    // Found it!
	    return 0;
	else if(count < hk.count)
	    return -1;
	else
	    return 1;
	
    }

    
    /* For package use only. */
    int equals(HashKey localToVworldKeys[], int start, int end) {
	int mid;

	mid = start +((end - start)/ 2);
	if(localToVworldKeys[mid] != null) {
	    int test = equals(localToVworldKeys[mid]);

	    if((test < 0) && (start != mid))
		return equals(localToVworldKeys, start, mid);
	    else if((test > 0) && (start != mid))
		return equals(localToVworldKeys, mid, end);
	    else if(test == 0)
		return mid;	     
	    else
		return -1;
	}
	// A null haskey encountered.
	return  -2;
    }

    /* For package use only. */
    boolean equals(HashKey localToVworldKeys[], int[] index,
		   int start, int end) {

	int mid;
	
	mid = start +((end - start)/ 2);
	if(localToVworldKeys[mid] != null) {
	    int test = equals(localToVworldKeys[mid]);
	    
	    if(start != mid) {
		if(test < 0) {
		    return equals(localToVworldKeys, index, start, mid);
		}
		else if(test > 0) {
		    return equals(localToVworldKeys, index, mid, end);
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
	    index[0] = mid;
	    return true;
	    
	}
	// A null haskey encountered.
	// But we still want to return the index where we encounter it.
	index[0] = mid;
	return  false;
    }
    
    public String toString() {
	return new String(value, 0, count);
    }
 
    String getLastNodeId() {
      int i, j, temp;
      
      for(i=(count-1); i>0; i--) 
	if(value[i] == '+')
	  break;
     
      if(i>0) {
	value[i++] = '\0';
	temp = count-i;
	char v1[] = new char[temp];
	for(j=0; j<temp; j++, i++) {
	  v1[j] = value[i];
	  value[i] = '\0';
	}
	count = count - (temp+1);	
	return new String(v1);
      }
      
      return new String(value, 0, count);
    }

}
