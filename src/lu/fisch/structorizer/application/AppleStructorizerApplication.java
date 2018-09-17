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
 *      Author:         kay
 *
 *      Description:    Optional Apple-specific StructizerApplication subclass
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2018.09.14      First Issue to address #537 in a way allowing easy configuration management
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 *      This class is OPTIONAL and must be EXCLUDED from the project configuration and classpath if
 *      package com.apple.eawt is not available!
 *      !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 *
 ******************************************************************************************************///

import lu.fisch.structorizer.gui.Mainform;

/**
 * Optional Apple-specific StructizerApplication subclass
 * @author Kay Gürtzig
 */
public class AppleStructorizerApplication extends StructorizerApplication
{
	
	// Unfortunately this static code isn't executed before a first instance is requested - so it doesn't help
//	static {
//		ApplicationFactory.registerApplicationClass("apple", AppleStructorizerApplication.class.getName());
//	}

	@Override
	public void configureFor(Mainform mainform)
	{
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("apple.awt.graphics.UseQuartz", "true");

		com.apple.eawt.Application application = com.apple.eawt.Application.getApplication();

		try
		{
			application.setEnabledPreferencesMenu(true);
			application.addApplicationListener(new com.apple.eawt.ApplicationAdapter() {
				public void handleAbout(com.apple.eawt.ApplicationEvent evt) {
					mainform.diagram.aboutNSD();
					evt.setHandled(true);
				}
				public void handleOpenApplication(com.apple.eawt.ApplicationEvent evt) {
				}
				public void handleOpenFile(com.apple.eawt.ApplicationEvent evt) {
					if(evt.getFilename()!=null)
					{
						mainform.diagram.openNSD(evt.getFilename());
					}
				}
				public void handlePreferences(com.apple.eawt.ApplicationEvent evt) {
					mainform.diagram.preferencesNSD();
				}
				public void handlePrintFile(com.apple.eawt.ApplicationEvent evt) {
					mainform.diagram.printNSD();
				}
				public void handleQuit(com.apple.eawt.ApplicationEvent evt) {
					mainform.saveToINI();
					mainform.dispose();
				}
			});
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
}
