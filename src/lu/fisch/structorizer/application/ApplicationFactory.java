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
 *      Description:    Factory class providing a wrapper Application object that handles OS-specific
 *                      Application configuration as e.g. with com.apple.eawt.Application, which is
 *                      not necessarily an installed package.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2018-09-14      First Issue
 *      Kay Gürtzig     2019-03-13      Deprecated stuff replaced
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      This class was introduced to allow a costumizable build that may or may not make use of optional
 *      packages, e.g. com.apple.eawt.Application. 
 *
 ******************************************************************************************************///

/**
 * Factory class providing OS-specific StructorizerApplication objects (dummy or not),
 * which may be configured given a {@link Mainform} instance.
 * @author Kay Gürtzig
 */
public class ApplicationFactory {
	
	// The registration mechanism relied on the idea the existing StructorizerApplication subclasses
	// register themselves on startup but static code of a class isn't executed before a first
	// initialization, so the mechanism doesn't work. We could of course have Structorizer.java
	// register all StructorizerApplication classes but then Structorizer.java may directly specify
	// the class name rightaway rather than some fancy OS key previously associated. 
//	/** The application register statically to be filled by the existing classes themselves */
//	private static final HashMap<String, String> appRegister = new HashMap<String, String>();
	
//	/** Singleton instance, lazily initialized */
//	private static ApplicationFactory instance = null;
//	
//	/** External use prevented - as a singleton, getInstance() is to be used instead. */
//	private ApplicationFactory() {}
//	
//	/**
//	 * Returns the singleton instance of this factory class (lazy initialization)
//	 * @return the singleton instance
//	 */
//	public static ApplicationFactory getInstance()
//	{
//		if (instance == null) {
//			instance = new ApplicationFactory();
//		}
//		return instance;
//	}

	// Registry approach doesn't work as intended, so it didn't make sense anymore
//	/**
//	 * Registers the given class path {@code className} as configurable Application
//	 * @param os_key
//	 * @param className
//	 */
//	public static void registerApplicationClass(String os_key, String className)
//	{
//		appRegister.put(os_key, className);
//	}
//	
//	public static void unregisterApplicationClass(String os_key)
//	{
//		appRegister.remove(os_key);
//	}
	
	/**
	 * Returns an application configuration helper of the specified class, which
	 * mus be a subclass of {@link StructorizerApplication}, or (if this class isn't
	 * available) a dummy {@link StructorizerApplication}
	 * @param className
	 * @return either an instance of class {@code className} or of {@link StructorizerApplication}
	 */
	//public static StructorizerApplication getApplication(String os_key)
	public static StructorizerApplication getApplication(String className)
	{
		StructorizerApplication appl = null;
		
		//String className = appRegister.getOrDefault(os_key, "lu.fisch.structorizer.application.StructorizerApplication");

		try {
			Class<?> applClass = Class.forName(className);
			
			Object appObj = applClass.getDeclaredConstructor().newInstance();
			if (appObj instanceof StructorizerApplication) {
				appl = (StructorizerApplication)appObj;
			}
			else {
				System.err.println("*** Requested configurable application class " + className + " is unsuited.");
				appl = null;
			}
			
		} catch (ClassNotFoundException ex) {
			// The intended application class is not available, so return a dummy
			System.err.println("*** Configurable application class " + className + " not available. Using dummy.");
		} catch (InstantiationException | IllegalAccessException ex) {
			System.err.println("*** Configurable application class " + className + " cannot be instantiated:\n" + ex);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (appl == null) {
			appl = new StructorizerApplication();
		}
		
		return appl;
		
	}
}
