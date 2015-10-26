package goldengine.java;

import java.util.*;

/*
 * Licensed Material - Property of Matthew Hawkins (hawkini@barclays.net)
 *
 * GOLDParser - code ported from VB - Author Devin Cook. All rights reserved.
 *
 * No modifications to this code are allowed without the permission of the author.
 */
/**-------------------------------------------------------------------------------------------<br>
 *
 *      Source File:    FAState.java<br>
 *
 *      Author:         Devin Cook, Matthew Hawkins<br>
 *
 *      Description:    Represents a DFA state.<br>
 *
 *
 *-------------------------------------------------------------------------------------------<br>
 *
 *      Revision List<br>
 *<pre>
 *      Author          Version         Description
 *      ------          -------         -----------
 *      MPH             1.0             First Issue</pre><br>
 *
 *-------------------------------------------------------------------------------------------<br>
 *
 *      IMPORT: java.util <br>
 *
 *-------------------------------------------------------------------------------------------<br>
 */
public class FAState
{
	private Vector edges = new Vector();
	public int acceptSymbol;

    /***************************************************************
 	 *
 	 * addEdge
 	 *
 	 * This method will add an edge to the FAState. It will create a
     * new edge if there are no chars (the lambda edge), otherwise
     * it will find the index from the target passed in, and create
     * a new edge with the target index of the target passed in, and
     * the characters passed in.<br>
     * If the target is not found, it will produce an error.
 	 * @param chars The set of characters associated with the edge to
     *              be created.
     * @param target The target index in this state.
 	 ***************************************************************/
    public void addEdge(String chars, int target)
    {
        FAEdge edge;
        int n, index;

        if(chars.equals("")) // lambda edge - always add
        {
            edge = new FAEdge();
            edge.setChars("");
            edge.setTargetIndex(target);
            edges.addElement(edge);
        }
        else
        {
            index = -1;
            n = 0;

            while((n < edges.size()) & (index == -1))
            {
                FAEdge tmpE = (FAEdge)edges.elementAt(n);
                if(tmpE.getTargetIndex() == target)
                {
                    index = n;
                }
                n++;
            }

            if(index == -1)
            {
                edge = new FAEdge();
                edge.setChars(chars);
                edge.setTargetIndex(target);
                edges.addElement(edge);
            }
            else
            {
                FAEdge tmpEE = (FAEdge)edges.elementAt(index);
                tmpEE.setChars(tmpEE.getChars() + chars);
            }
        }
    }

    /***************************************************************
 	 *
 	 * edge
 	 *
 	 * This method will return the edge at the specified index for
     * this state.
 	 * @param index The index of the edge that will be returned.
 	 * @return The FAEdge at the specified index.
 	 ***************************************************************/
	public FAEdge edge(int index)
    {
        if((index >= 0) & (index < edges.size()))
        {
            return (FAEdge)edges.elementAt(index);
        }

        return null;
    }

    /***************************************************************
 	 *
 	 * edgeCount
 	 *
 	 * The number of edges in this FAState.
 	 * @return The number of edges in this FAState.
 	 ***************************************************************/
    public int edgeCount()
    {
        return edges.size();
    }
}