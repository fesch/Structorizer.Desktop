/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package lu.fisch.structorizer.gui;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class launches the analyse process and shows items
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2008-04-18      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:		
 *
 ******************************************************************************************************///

import lu.fisch.structorizer.elements.*;

import java.util.*;


import javax.swing.*;

public class Analyser extends Thread
{
	private Root root = null;
	private JList<DetectedError> errorlist = null;
	private DefaultListModel<DetectedError> errors = null;
	
	private static boolean running = false;
	
	public Analyser(Root _root, JList<DetectedError> _errorlist)
	{
		super();
		
		//System.out.println("Setup ...");
		root = _root;
		errorlist = _errorlist;
		errors = (DefaultListModel<DetectedError>) _errorlist.getModel();
	}

	public void run() 
	{
		// make sure the analyser is not yet running
		if(running==false)
		{
			running=true;
			//System.out.println("Working ...");
			Vector<DetectedError> vec = root.analyse();
			errors.clear();
			
			for(int i=0; i<vec.size(); i++)
			{
				errors.addElement(vec.get(i));
			}
			
			errorlist.repaint();
			errorlist.validate();
			running=false;
		}
		//else System.out.println("RUNNING");
	}
/*	
	public void mouseClicked(MouseEvent e) 
	{
		if (e.getClickCount() == 1)
		{
			// alors?
		}
		else if (e.getClickCount() == 2)
		{
		}
	}	

    public void mouseReleased(MouseEvent e) 
	{
	}
	
    public void mouseEntered(MouseEvent e) 
	{
	}
	
    public void mouseExited(MouseEvent e) 
	{
	}
	
	public void mousePressed(MouseEvent e) 
	{
	}
*/
}
