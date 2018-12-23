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
package lu.fisch.structorizer.application;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Basic dummy application for OS-specific embedding of Structorizer.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2018.09.14      First Issue, addressing issue #537
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      This class was introduced to allow a customizable build that may or may not make use of optional
 *      packages, e.g. com.apple.eawt.Application. It is intended to be subclass but also serves as dummy
 *      fallback in case the specified subclass is not available.
 *
 ******************************************************************************************************///

import lu.fisch.structorizer.gui.Mainform;

/**
 * Basic dummy application class for OS-specific customization of Structorizer embedding.
 * Responds to {@link #configureFor(Mainform)}.<br/>
 * Subclasses should be lightweight and override {@link #configureFor(Mainform)} in a sensible
 * way, using the OS-specific extension package.
 * <!--- NOTE: All subclasses must statically register with a suitedOS-specific key on {@link ApplicationFactory}
 * in the following way (let the subclass be named CPMApplication and "CP/M" the corresponding
 * os key):<br/>
 * {@code static} { {@code ApplicationFactory.registerAplication("CP/M", CPMAplication.class.getName());} } -->
 * @author Kay Gürtzig
 */
public class StructorizerApplication {

	public StructorizerApplication() {}
	
	/**
	 * Does all application-specific configuration based on the given {@link Mainform} instance.<br/>
	 * This method is to be overridden in a sensible way by the subclasses.
	 * @param mainform - the fully initialized(!) Structorizer {@link Mainform} instance
	 */
	public void configureFor(Mainform mainform)
	{
		//System.err.println("*** Dummy application!");
	}
	
}
