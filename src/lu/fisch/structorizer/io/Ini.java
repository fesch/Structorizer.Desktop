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

package lu.fisch.structorizer.io;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class manages entries in the INI-file
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author              Date            Description
 *      ------              ----            -----------
 *      Bob Fisch           2008-05-02      First Issue
 *      Gennaro Donnarumma  2014-02-02      Ini in JAR support
 *      Kay Gürtzig         2016-04-26      Jar path updated
 *      Kay Gürtzig         2016-07-22      Bugfix #200: save() method now immediately closes the file
 *      Kay Gürtzig         2016-09-28      First comment line modified (KGU#264)
 *      Kay Gürtzig         2017-03-13      Method getIniDirectory() added to support issue #372
 *      Kay Gürtzig         2017-11-05      Issue #452: Method wasFirstStart() added.
 *      Kay Gürtzig         2018-03-21      Issue #463 Logger introduced, two file reading sequences extracted to method readTextFile()
 *      Kay Gürtzig         2018-10-28      Flag to detect unsaved changes introduced (+ public method)
 *      Kay Gürtzig         2019-08-02      Issue #733 New strategy for a central ini file in the installation dir
 *      Kay Gürtzig         2019-08-03      Issue #733 Selective property export mechanism implemented.
 *      Kay Gürtzig         2019-08-05      Enh. #737: Additional method variant load(String, boolan)
 *                                          Issue #733: ...alternate... renamed in ...antecedent...
 *      Kay Gürtzig         2019-08-06      Enh. #740: Backup support
 *      Kay Gürtzig         2019-08-07      Enh. #741: Mechanisms to redirect the ini path via command line
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2019-08-02 - Kay Gürtzig 
 *      - The new strategy is that we save preferences only in the regular ini directory. In the
 *        installation directory, however, there may a (restricted) alternative ini file that
 *        contains certain subset of the preferences always to be imposed on starting a session. 
 *      - 
 *
 ******************************************************************************************************///

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import lu.fisch.structorizer.elements.Element;

/**
 * This class manages product settings as properties, saves and loads them
 * to/from the INI-file, adhering to the Singleton pattern.
 * 
 * @author Bob Fisch
 * @see #getInstance()
 */
public class Ini {

	private static Ini ini = null;
	private static boolean useAppData = false;
	// START KGU#48 2018-03-21: Issue #463
	private static final Logger logger = Logger.getLogger(Ini.class.getName());
	// END KGU 2018-03-21
	// START KGU722 2019-08-07: Enh. #741
	private static String favouredFilename = null;
	// END KGU#722 2019-08-07
	// START KGU#456 2017-11-05: Issue #452
	/** remembers whether the ini file had to be created. */
	private boolean iniFileCreated = false;
	// END KGU#456 2017-11-05
	// START KGU#721 2019-08-06 Enh. #740
	private boolean backedUp = false;
	// END KGU#721 2019-08-06

	/**
	 * @return the path of an OS-specific application data subdirectory for
	 *         Structorizer as string
	 * @see #getIniDirectory()
	 */
	public static String getAppDataDirname() {
		String os_name = System.getProperty("os.name").toLowerCase();
		String home = System.getProperty("user.home");
		// mac
		if (os_name.indexOf("mac") >= 0) {
			return home + "/Library/Application Support/Structorizer";
		}
		// windows
		else if (os_name.indexOf("win") >= 0) {
			String appData = System.getenv("APPDATA");
			if (appData != null && !appData.isEmpty()) {
				return appData + "\\Structorizer";
			}
			return home + "\\Application Data\\Structorizer";
		} else {
			return home + System.getProperty("file.separator") + ".unimozer";
		}
	}

	/**
	 * Returns the singleton instance of this class. In case there hadn't been an
	 * instance before, creates it and may also create the "structorizer.ini" file
	 * if there hasn't been any in either the home or the installation directory.
	 * Will not reload the file otherwise.
	 * 
	 * @return the instance
	 * @see #load()
	 * @see #load(String)
	 */
	public static Ini getInstance() {
		if (ini == null) {
			try {
				ini = new Ini();
			} catch (FileNotFoundException ex) {
				logger.severe(ex.getMessage());
			} catch (IOException ex) {
				logger.severe(ex.getMessage());
			}
		}
		return ini;
	}

	/**
	 * @return the userAppData
	 */
	public static boolean isUsingAppData() {
		return useAppData;
	}

	/**
	 * @param userAppData the userAppData to set
	 */
	public static void setUseAppData(boolean puseAppData) {
		useAppData = puseAppData;
	}

	// START KGU#722 2019-08-07: Enh. #741
	public static boolean setIniPath(String filePath) {
		boolean done = false;
		if (ini == null) {
			favouredFilename = filePath;
			done = true;
		}
		else {
			try {
				ini.save();
				File file = new File(filePath);
				if (file.exists() || file.createNewFile()) {
					done = ini.redirect(filePath, false);
				}
			} catch (IOException e) {
				logger.severe(e.getMessage());
			}
		}
		return done;
	}
	// END KGU#722 2019-08-07

	// START KGU#363 2017-03-13: Enh. #372 We use the ini directory for the licenses
	// as well
	/**
	 * @return the path of the directory for the ini file as string
	 */
	public static File getIniDirectory() {
		File iniDir = null;
		if (ini == null || ini.filename == null || ini.filename.isEmpty() || !(new File(ini.filename)).exists()) {
			try {
				String dirName = System.getProperty("user.home") + System.getProperty("file.separator")
						+ ".structorizer";
				if (useAppData) {
					dirName = Ini.getAppDataDirname();
				}
				iniDir = new File(dirName);

			} catch (Error e) {
				logger.severe(e.getMessage());
			} catch (Exception e) {
				logger.severe(e.getMessage());
			}
		} else {
			iniDir = (new File(ini.filename)).getParentFile();
		}
		return iniDir;
	}
	// END KGU#363 2017-03-13
	
	// START KGU#722 2019-08-07: Enh. #741 - we need the complete ini file name
	public File getIniFile()
	{
		return new File(filename);
	}
	// END KGU#722 2019-08-07

	// START KGU#466 2019-08-01: Enh. #733: New ini retrieval mechanism
	/**
	 * @return the installation directory (may be the one where Structorizer.jar or
	 *         the exe resides.
	 */
	public static File getInstallDirectory() {
		File instDir = null;
		URL mySource = Ini.class.getProtectionDomain().getCodeSource().getLocation();
		String myPath = mySource.getPath();
		try {
			myPath = URLDecoder.decode(myPath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.warning("Identifying the installation dir... " + e.getMessage());
		}
		instDir = new File(myPath);
		if ("Structorizer.jar".equals(instDir.getName())) {
			instDir = instDir.getParentFile();
		}
		return instDir;
	}
	// END KGU#466 2019-08-01

	private String filename = "";

	private String filename2 = "";

	private final String ininame = "structorizer.ini";

	private Properties p = new Properties();

	// START KGU#603 2018-10-28: We should be able to tell whether there are unsaved
	// changes
	private boolean wasChanged = false;

	public boolean hasUnsavedChanges() {
		return wasChanged;
	}
	// END KGU#603 2018-10-28

	// METHOD MODIFIED BY GENNARO DONNARUMMA

	private Ini() throws FileNotFoundException, IOException {
		boolean regularExists = false;
		boolean antecedentExists = false;
		File dir = null;
		File file = null;

		String dirname = "";
		String dirname2 = "";

		// regular INI file
		try {
			// START KGU#722 2019-08-07: Enh. #741 - A non-standard ini path may have been set, e.g. via -s option
			//dirname = getIniDirectory().getPath();
			//filename = dirname + System.getProperty("file.separator") + ininame;
			if (favouredFilename != null) {
				file = new File(favouredFilename);
				dir = file.getParentFile();
				if (dir.exists() || dir.mkdirs()) {
					dirname = dir.getPath();
					filename = file.getAbsolutePath();
				}
			}
			if (dirname.isEmpty() || filename.isEmpty()) {
				dirname = getIniDirectory().getPath();
				filename = dirname + System.getProperty("file.separator") + ininame;
			}
			// END KGU#722 2019-08-07
		} catch (Exception e) {
			// START KGU#722 2019-08-07: Enh. #741 - A non-standard ini path may have been set, e.g. via -s option
			//logger.warning("probing regular ini directory " + e.getMessage());
			if (favouredFilename != null) {
				logger.warning("probing externally specified ini file \"" + filename + "\": " + e.getMessage());
			}
			else {
				logger.warning("probing regular ini directory " + e.getMessage());
			}

			// END KGU#722 2019-08-07
		}

		// does the regular file exist?
		try {
			file = new File(filename);
			regularExists = file.exists();
		} catch (Exception e) {
			logger.warning("testing existence of the regular file... " + e.getMessage());
		}

		// template INI file
		try {
			File sourceFile = getInstallDirectory();
			dirname2 = sourceFile.getAbsolutePath();

			// MODIFIED BY GENNARO DONNARUMMA, ADDED SUPPORT SETTINGSFILE IN
			// JAR-PATH OR STRUCTORIZER.exe-PATH
			// START KGU#466 2019-08-01: Issue #733 The separator had been missing here
			// filename2 = dirname2 + "structorizer.ini";
			filename2 = dirname2 + File.separator + ininame;
			// END KGU#466 2019-08-01

			File fileInJarPath = new File(filename2);
			// JOptionPane.showMessageDialog(null, filename2);

			if (!fileInJarPath.exists()) {
				// START KGU#466 2019-08-01: Issue #733 - this was redundant
				// filename2 = dirname2 + System.getProperty("file.separator")
				// + ininame;
				// filename2 = filename2.replaceFirst("Structorizer.jar\\"
				// + System.getProperty("file.separator"), "");
				// END KGU#466 2019-08-01
				filename2 = filename2.replaceFirst("\\" + System.getProperty("file.separator") + "Contents\\"
				// START KGU 2016-04-26: New jar path!
				// + System.getProperty("file.separator")
				// + "Resources\\"
				// END KGU 2016-04-26
						+ System.getProperty("file.separator") + "Java", "");
				// filename2 = filename2.replaceFirst("\\\\Structorizer.app",
				// "");
				// filename2 = URLDecoder.decode(filename2, "UTF-8");
			}
		} catch (Exception e) {
			logger.warning("probing for an alternative ini file... " + e.getMessage());
		}

		// does the alternative file exist?
		try {
			file = new File(filename2);
			antecedentExists = file.exists();
		} catch (Exception e) {
			logger.warning("looking for alternative ini " + e.getMessage());
		}

		// JOptionPane.showMessageDialog(null, filename+" ==> "+regularExists);
		// JOptionPane.showMessageDialog(null,
		// filename2+" ==> "+antecedentExists);

		// if no file has been found
		if (!regularExists & !antecedentExists) {
			// create the regular one
			try {
				dir = new File(dirname);
				file = new File(filename);

				if (!dir.exists()) {
					dir.mkdir();
				}

				if (!file.exists()) {
					try {
						// setProperty("dummy","dummy");
						saveRegular();
						// START KGU#456 2017-11-05: Issue #452
						this.iniFileCreated = true;
						// END KGU#456 2017-11-05
					} catch (Exception e) {
						logger.log(Level.WARNING, "creating the regular file ", e);
					}
				}

				regularExists = true;
			} catch (Exception e) {
				logger.severe(e.getMessage());
			}
		} else if (antecedentExists) {
			// START KGU#466 2019-08-02: Issue #733 - New strategy w.r.t. to central default
			// ini file
			// This means: the antecedent path has preference before the regular one!
			// filename = filename2;
			// antecedentExists = false;
			/*
			 * Now we want that the first time preferences are loaded they be obtained from
			 * the central ini file if it exists. The contents are to be saved to the
			 * regular ini file and further on only the regular file is to be consulted
			 * within the session.
			 */
			if (regularExists) {
				try {
					// Load all existing individual preferences
					loadRegular();
					// Override the set of central preferences
					loadAntecedent();
					// Save the combination
					saveRegular();
				} catch (Exception e) {
					logger.log(Level.WARNING, "combining antecedent with regular file ", e);
				}
			} else {
				try {
					dir = new File(dirname);
					if (!dir.exists()) {
						dir.mkdir();
					}
					// Get the central preferences
					loadAntecedent();
					// and save them as start for the regular in file
					saveRegular();
					this.iniFileCreated = true;
				} catch (Exception e) {
					logger.log(Level.WARNING, "creating a regular file with central presets", e);
				}
			}
			// END KGU#466 2019-08-02
		}

		// load the file once!
		// loadRegular();

		/*
		 * // antecedent INI file try { URL mySource =
		 * Ini.class.getProtectionDomain().getCodeSource().getLocation(); File
		 * sourceFile = new File(mySource.getPath());
		 * dirname2=sourceFile.getAbsolutePath(); filename2 =
		 * dirname2+System.getProperty("file.separator")+ininame; filename2 =
		 * filename2.replaceFirst("Structorizer.jar/", ""); } catch(Error e) {
		 * System.out.println(e.getMessage()); } catch(Exception e) {
		 * System.out.println(e.getMessage()); }
		 * 
		 * // regular INI file try { dirname = System.getProperty("user.home")+System
		 * .getProperty("file.separator")+".structorizer"; filename =
		 * dirname+System.getProperty("file.separator")+ininame; } catch(Error e) {
		 * System.out.println(e.getMessage()); } catch(Exception e) {
		 * System.out.println(e.getMessage()); }
		 * 
		 * // regular INI file try { dir = new File(dirname); file = new File(filename);
		 * 
		 * if(!dir.exists()) { dir.mkdir(); }
		 * 
		 * if(!file.exists()) { try { //setProperty("dummy","dummy"); saveRegular(); }
		 * catch (Exception e) { e.printStackTrace();
		 * System.out.println(e.getMessage()); } } } catch(Error e) {
		 * System.out.println(e.getMessage()); } catch(Exception e) {
		 * System.out.println(e.getMessage()); }
		 * 
		 * // antecedent INI file try { dir2 = new File(dirname2); file2 = new
		 * File(filename2);
		 * 
		 * if(!file2.exists()) { try { //setProperty("dummy","dummy"); saveAntecedent();
		 * } catch (Exception e) { e.printStackTrace();
		 * System.out.println(e.getMessage()); } } } catch(Error e) {
		 * System.out.println(e.getMessage()); } catch(Exception e) {
		 * System.out.println(e.getMessage()); }
		 */
	}

	public String getProperty(String _name, String _default) {
		String value = p.getProperty(_name);
		if (value == null) {
			return _default;
		}
		return value;
	}

	public Set<Object> keySet() {
		return (Set<Object>) p.keySet();
	}

	/**
	 * Loads the properties from the regular ini file as established by the
	 * constructor (according to the product-specific ini file location rules).
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @see #load(String)
	 * @see #loadAntecedent()
	 */
	public void load() throws FileNotFoundException, IOException {
		// if(regularExists) loadRegular();
		// if(antecedentExists) loadAntecedent();
		loadRegular();
	}

	/**
	 * Loads the properties from file {@code _filename}. Previously held properties
	 * not overridden by those from the file remain preserved.
	 * 
	 * @param _filename    - name of the file to be loaded
	 * @param _clearBefore - whether the recent properties should be discarded
	 *                     before
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @see #load()
	 * @see #load(String, boolean)
	 */
	public void load(String _filename) throws FileNotFoundException, IOException
	// START KGU#720 2019-08-05: Enh. #737 It should be possible clear all
	// properties before loading
	{
		load(_filename, false);
	}

	/**
	 * Loads the properties from file {@code _filename}. Clears the previously
	 * existing properties if the file can be opened.
	 * 
	 * @param _filename    - name of the file to be loaded
	 * @param _clearBefore - whether the recent properties should be discarded
	 *                     before
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void load(String _filename, boolean _clearBefore) throws FileNotFoundException, IOException
	// END KGU#720 2019-08-05
	{
		File f = new File(_filename);
		if (f.length() != 0) {
			// START KGU#210 2016-07-22: Bugfix #200 ensure the file gets closed
			// p.load(new FileInputStream(_filename));
			FileInputStream fis = new FileInputStream(_filename);
			// START KGU#720 2019-08-05: Enh. #737
			if (_clearBefore) {
				p.clear();
			}
			// END KGU#720 2019-08-05
			p.load(fis);
			fis.close();
			// END KGU#210 2016-07-22
			this.wasChanged = false;
		}
	}

	public void loadAntecedent() throws FileNotFoundException, IOException {
		// System.out.println("Trying to load INI file: "+filename);
		// START KGU#210 2016-07-22: Bugfix #200 ensure the file gets closed
//		File f = new File(filename2);
//		if (f.length() == 0)
//		{
//			System.out.println("File is empty!");
//		} else
//		{
//			// p.loadFromXML(new FileInputStream(filename));
//			p.load(new FileInputStream(filename2));
//			// System.out.println(p.toString());
//		}
		load(filename2);
		// END KGU#210 2016-07-22
	}

	public void loadRegular() throws FileNotFoundException, IOException {
		// System.out.println("Trying to load INI file: "+filename);
		// JOptionPane.showMessageDialog(null, "Loading from => "+filename);
		// START KGU#210 2016-07-22: Bugfix #200 ensure the file gets closed
//		File f = new File(filename);
//		if (f.length() == 0)
//		{
//			System.out.println("File is empty!");
//		} else
//		{
//			// p.loadFromXML(new FileInputStream(filename));
//			p.load(new FileInputStream(filename));
//			// System.out.println(p.toString());
//		}
		load(filename);
		// END KGU#210 2016-07-22
	}

	/**
	 * Saves all properties to the regular INI file.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void save() throws FileNotFoundException, IOException {
		saveRegular();
	}

	/**
	 * Saves all preferences to the file with path {@code _filename}
	 * 
	 * @param _filename - target file path
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @see #save(String, Set)
	 */
	public void save(String _filename) throws FileNotFoundException, IOException
	// START KGU#466 2019-08-02: Enh. #733
	{
		save(_filename, p);
	}

	/**
	 * Saves the preferences specified by {@code preferenceKeys} to the file with
	 * path {@code _filename}
	 * 
	 * @param _filename      - target file path
	 * @param preferenceKeys - a set of property names and possibly start sequences
	 *                       of property names (the latter ones have to end with an
	 *                       asterisk interpreted as wildcard; note that internal
	 *                       wildcards or even regular expressions will not be
	 *                       recognised as such).
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @see #save()
	 */
	public void save(String _filename, Set<String> preferenceKeys) throws FileNotFoundException, IOException {
		Properties p1 = new Properties();
		ArrayList<String> starts = new ArrayList<String>();
		for (String pattern : preferenceKeys) {
			if (!pattern.endsWith("*")) {
				String value = p.getProperty(pattern);
				if (value != null) {
					p1.setProperty(pattern, value);
				}
			} else {
				starts.add(pattern.substring(0, pattern.length() - 1));
			}
		}
		if (!starts.isEmpty()) {
			// This is awfully slow but it's hard to thing of a better version
			for (Map.Entry<Object, Object> entry : p.entrySet()) {
				String key = ((String) entry.getKey());
				for (String start : starts) {
					if (key.startsWith(start)) {
						p1.setProperty(key, (String) entry.getValue());
					}
				}
			}

		}
		save(_filename, p1);
	}

	// START KGU#721 2019-08-06 Enh. #740
	/**
	 * Makes a backup of the current settings and sets a flag that can be consulted
	 * with
	 * 
	 * @return true if the backup succeeded (might e.g. fail if the directory or a
	 *         former backup is read-only).
	 */
	public boolean backup() {
		boolean done = false;
		try {
			this.save(filename + ".bak");
			done = true;
			this.backedUp = true;
		} catch (IOException ex) {
			logger.severe(ex.getMessage());
		}
		return done;
	}

	/**
	 * Restores the preferences from the last backup created by {@link #backup()},
	 * will clear all preferences not being in the backup and override all being in
	 * it.
	 * 
	 * @return true if a backup had existed and could be loaded successfully
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public boolean restore() throws FileNotFoundException, IOException {
		boolean done = this.backedUp;
		if (done) {
			this.load(filename + ".bak", true);
			this.backedUp = false;
		}
		return done;
	}

	/**
	 * Indicates whether a backup INI file had been produced via {@link #backup()}
	 * recently in this session. A successful call of {@link #restore()} will clear
	 * this flag.
	 * 
	 * @return true if a backup INI file is available for {@link #restore()}.
	 */
	public boolean hasBackup() {
		return this.backedUp && (this.backedUp = (new File(filename + ".bak").exists()));
	}
	// END KGU#721 2019-08-06

	private void save(String _filename, Properties props) throws FileNotFoundException, IOException
	// END KGU#466 2019-08-02
	{
		// START KGU#210 2016-07-22: Bugfix #200 ensure the file gets closed
//		p.store(new FileOutputStream(_filename), "last updated "
//				+ new java.util.Date());
		FileOutputStream fos = new FileOutputStream(_filename);
		// START KGU#264 2016-09-28: The date was redundant (next comment is the date,
		// anyway), so better write the version
		// p.store(fos, "last updated " + new java.util.Date());
		// START KGU#466 2019-08-03: Issue #733 - indicate a property selection
		// p.store(fos, "version " + Element.E_VERSION);
		props.store(fos, "Structorizer version " + Element.E_VERSION + (p != props ? "\n(Preferences subset)" : ""));
		// END KGU#466 2019-08-03
		// END KGU#264 2016-09-28
		fos.close();
		// END KGU#210 2016-07-22
		this.wasChanged = false;
	}

	// START KGU#722 2019-08-06: Enh. #741, bugfix #733
	/**
	 * This tries to redeirect the INI file path to the given _filename. If
	 * {@code _readOnly} is true or the given file cannot be written then all
	 * updates etc. go to a temprary file instead.
	 * 
	 * @param _filename
	 * @param _readOnly
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public boolean redirect(String _filename, boolean _readOnly) throws FileNotFoundException, IOException {
		// First replace all properties by those from the specified file (may crash)
		load(_filename, true);
		// Now check if the file is to be readonly or cannot be written
		File target = new File(_filename);
		if (_readOnly || target.exists() && !target.canWrite()) {
			// If readonly then create a temporary file for the rest of the session and use
			// this instead
			try {
				File tempFile = File.createTempFile("structorizer", ".ini");
				this.save(tempFile.getAbsolutePath());
				_filename = tempFile.getAbsolutePath();
			} catch (IOException ex) {
				logger.severe(ex.getMessage());
			}
		}
		this.filename = _filename;
		return true;
	}
	// END KGU#722 2019-08-06

	// START KGU#466 2019-08-01: Issue #733 - Not only superfluous, but even
	// dangerous
	// private void saveAntecedent() throws FileNotFoundException, IOException
	// {
	// // START KGU#210 2016-07-22: Bugfix #200 ensure the file gets closed
	// //p.store(new FileOutputStream(filename2), "last updated "
	// // + new java.util.Date());
	// this.save(filename2);
	// // END KGU#210 2016-07-22
	// // JOptionPane.showMessageDialog(null, "antecedent saved => "+filename2);
	// }
	// END KGU#466 2019-08-01

	private void saveRegular() throws FileNotFoundException, IOException {
		// START KGU#210 2016-07-22: Bugfix #200 ensure the file gets closed
		// p.store(new FileOutputStream(filename), "last updated "
		// + new java.util.Date());
		this.save(filename);
		// END KGU#210 2016-07-22
		// JOptionPane.showMessageDialog(null, "Regular saved");
	}

	public void setProperty(String _name, String _value) {
		p.setProperty(_name, _value);
		this.wasChanged = true;
	}

	// START KGU#456 2017-11-05: Issue #452
	/**
	 * Returns true if there are indicators for a first use.
	 * 
	 * @return true if the INI file had to be created.
	 */
	public boolean wasFirstStart() {
		return this.iniFileCreated;
	}
	// END KGU#456 2017-11-05

}
