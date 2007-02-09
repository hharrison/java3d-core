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

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * A class that enumerates all wakeup criteria in a wakeup condition
 */

class WakeupCriteriaEnumerator implements Enumeration {

   // An array used for the current criteria in this object
   WakeupCriterion[] criterion = null;

   // A pointer to the current criteria
   int currentIndex = 0;

   // The number of valid criteria in the array, may be less than criterion.length
   int length = 0;

   WakeupCriteriaEnumerator(WakeupCondition cond, int type) {
	this.reset(cond, type);
   }

   void reset(WakeupCondition cond, int type) {
	int i, j;

	currentIndex = 0;
	length = 0;
	if (cond instanceof WakeupCriterion) {
	   WakeupCriterion crit = (WakeupCriterion)cond;

	   if (criterion == null || criterion.length < 1) {
	      criterion = new WakeupCriterion[1];
	   }
	   if (crit.triggered || type == WakeupCondition.ALL_ELEMENTS) {
	      criterion[0] = crit;
	      length = 1;
	   }
        } else {
	   if (cond instanceof WakeupAnd) {
	      WakeupAnd condAnd = (WakeupAnd)cond;

	      if (criterion == null || criterion.length < condAnd.conditions.length) {
		 criterion = new WakeupCriterion[condAnd.conditions.length];
	      }
	      for (i=0; i<condAnd.conditions.length; i++) {
		 if (condAnd.conditions[i].triggered || type == WakeupCondition.ALL_ELEMENTS) {
		    criterion[length++] = condAnd.conditions[i];
		 }
	      }
	   } else if (cond instanceof WakeupOr) {
	      WakeupOr condOr = (WakeupOr)cond;

	      if (criterion == null || criterion.length < condOr.conditions.length) {
		 criterion = new WakeupCriterion[condOr.conditions.length];
	      }
	      for (i=0; i<condOr.conditions.length; i++) {
		 if (condOr.conditions[i].triggered || type == WakeupCondition.ALL_ELEMENTS) {
		    criterion[length++] = condOr.conditions[i];
		 }
	      }
	   } else if (cond instanceof WakeupOrOfAnds) {
	      WakeupOrOfAnds condOrOfAnds = (WakeupOrOfAnds)cond;
	      int lengthNeeded = 0;

	      for (i=0; i<condOrOfAnds.conditions.length; i++) {
		 lengthNeeded += condOrOfAnds.conditions[i].conditions.length;
	      }

	      if (criterion == null || criterion.length < lengthNeeded) {
		 criterion = new WakeupCriterion[lengthNeeded];
	      }

	      for (i=0; i<condOrOfAnds.conditions.length; i++) {
		 for (j=0; j<condOrOfAnds.conditions[i].conditions.length; j++) {
		     if (condOrOfAnds.conditions[i].conditions[j].triggered || 
			 type == WakeupCondition.ALL_ELEMENTS) {
			criterion[length++] = condOrOfAnds.conditions[i].conditions[j];
		     }
		 }
	      }
	   } else {
	      WakeupAndOfOrs condAndOfOrs = (WakeupAndOfOrs)cond;
	      int lengthNeeded = 0;

	      for (i=0; i<condAndOfOrs.conditions.length; i++) {
		 lengthNeeded += condAndOfOrs.conditions[i].conditions.length;
	      }

	      if (criterion == null || criterion.length < lengthNeeded) {
		 criterion = new WakeupCriterion[lengthNeeded];
	      }

	      for (i=0; i<condAndOfOrs.conditions.length; i++) {
		 for (j=0; j<condAndOfOrs.conditions[i].conditions.length; j++) {
		     if (condAndOfOrs.conditions[i].conditions[j].triggered || 
			 type == WakeupCondition.ALL_ELEMENTS) {
			criterion[length++] = condAndOfOrs.conditions[i].conditions[j];
		     }
		 }
	      }
	   }
        }
   }

   public boolean hasMoreElements() {
        if (currentIndex == length) {
	   return false;
	}
	return true;
   }

   public Object nextElement() {
	if (currentIndex < length) {
	   return ((Object)criterion[currentIndex++]);
	} else {
	   throw new NoSuchElementException(J3dI18N.getString("WakeupCriteriaEnumerator0"));
	}
   }
}
