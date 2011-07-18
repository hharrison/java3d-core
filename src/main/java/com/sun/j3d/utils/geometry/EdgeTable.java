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

package com.sun.j3d.utils.geometry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import com.sun.j3d.utils.geometry.Edge;

class EdgeTable {

  private HashMap edgeTable;
  private static final int DEBUG = 0;



  Integer get(int a, int b)
  {
    return (Integer)edgeTable.get(new Edge(a, b));
  } // End of get()


  Integer get(Edge e)
  {
    return (Integer)edgeTable.get(e);
  } // End of get()



  // This function creates a table used to connect the triangles.
  // Here's how it works.  If a triangle is made of indices 12,
  // 40, and 51, then edge(12, 40) gets 51, edge(40, 51) gets 12,
  // and edge(51, 12) gets 40.  This lets us quickly move from
  // triangle to triangle without saving a lot of extra data.
  EdgeTable(int triangleIndices[])
  {
    // We'll have one edge for each vertex
    edgeTable = new HashMap(triangleIndices.length * 2);

    // Fill in table
    Edge e;
    for (int t = 0 ; t < triangleIndices.length ; t += 3) {
      // Put all 3 edges of triangle into table
      for (int v = 0 ; v < 3 ; v++) {
        e = new Edge(triangleIndices[t + v],
                     triangleIndices[t + ((v + 1) % 3)]);

	if (edgeTable.get(e) != null) {
	  if ((DEBUG & 1) != 0) {
	    System.out.println("EdgeTable Error: duplicate edge (" +
	    triangleIndices[t + v] + ", " +
	    triangleIndices[t + ((v + 1) % 3)] + ").");
	  }
	} else {
	  // Store index of 3rd vertex (across from edge)
	  edgeTable.put(e, new Integer(t + ((v + 2) % 3)));
	}
      }
    }

    if ((DEBUG & 1) != 0) {
      System.out.println("Edge Table:");
      Iterator list = edgeTable.keySet().iterator();
      while (list.hasNext()) {
        Edge edge = (Edge)list.next();
        System.out.println("  (" + edge.v1 + ", " + edge.v2 + ") = " + 
          get(edge.v1, edge.v2));
      }
    }
  } // End of constructor EdgeTable

} // End of class EdgeTable

// End of file EdgeTable.java
