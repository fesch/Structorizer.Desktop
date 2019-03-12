/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

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
package lu.fisch.structorizer.archivar;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Class for packing / unpacking of NSD file collections
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-03-10      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2019-03-09 Kay Gürtzig
 *      - Initiated by issue #698
 *
 ******************************************************************************************************///

import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Updater;
import lu.fisch.structorizer.generators.XmlGenerator;
import lu.fisch.structorizer.io.ArrFilter;
import lu.fisch.structorizer.parsers.NSDParser;
import lu.fisch.utils.StringList;

/**
 * Light-weight class providing methods to compress and extract collections of NSD files or {@link Root}s
 * @author Kay Gürtzig
 */
public class Archivar {

	/** Increment value of x and y coordinate for diagonal arrangement */
	private static final int COORD_INCREMENT = 10;
	/** Default name for a temporary archive */
	public static final String TEMP_ARCHIVE_NAME = "tmpArchivar.zip";
	
	private Logger logger;
	
	public class ArchiveIndexEntry {
		public ArchiveRecord record;	// the root itself (or null) and the point
		public String path;				// the true file path
		public String virtPath = null;	// the virtual path (if inside an archive)
		public String name = null;		// diagram name
		public int minArgs = -1;		// minimum number of routine arguments, or -1 for a program, or -2 for an includable
		public int maxArgs = -1;		// maximum number of routine arguments, or -1 for a program, or -2 for an includable
		
		/** Derives an entry from an {@link ArchiveRecord} */
		public ArchiveIndexEntry(ArchiveRecord archiveRecord)
		{
			this.record = archiveRecord;
			this.path = archiveRecord.root.shadowFilepath;
			if (this.path == null) {
				this.path = archiveRecord.root.getPath();
			}
			else {
				this.virtPath = archiveRecord.root.getPath();
			}
			this.name = archiveRecord.root.getMethodName();
			if (archiveRecord.root.isInclude()) {
				this.minArgs = this.maxArgs = -2;
			}
			else if (archiveRecord.root.isSubroutine()) {
				this.minArgs = archiveRecord.root.getMinParameterCount();
				this.maxArgs = archiveRecord.root.getParameterNames().count();
			}
		}
		
		/** Derives an entry from a {@link Root} and its graphical location {@code point}. */
		public ArchiveIndexEntry(Root root, Point point)
		{
			this(new ArchiveRecord(root, point));
		}
		
		/** Builds an entry from explicitly given values. */
		public ArchiveIndexEntry(Point _point, String _path, String _virtualPath, String _diagramName, int _minArgs, int _maxArgs)
		{
			this.record = new ArchiveRecord(null, _point);
			this.path = _path;
			this.virtPath = _virtualPath;
			this.name = _diagramName;
			this.minArgs = _minArgs;
			this.maxArgs = _maxArgs;
		}
		
		/** Derives an entry from an arranger list file line. */
		public ArchiveIndexEntry(String arrangementLine, File _fromArchive, File _extractDir)
		{
			StringList fields = StringList.explode(arrangementLine, ",");	// FIXME what if a path or the name contains a comma?
			if (fields.count() >= 3)
			{
				Point point = new Point(Integer.parseInt(fields.get(0)), Integer.parseInt(fields.get(1)));
				this.record = new ArchiveRecord(null, point);
				String nsdFileName = fields.get(2);
				if (nsdFileName.startsWith("\""))
					nsdFileName = nsdFileName.substring(1);
				if (nsdFileName.endsWith("\""))
					nsdFileName = nsdFileName.substring(0, nsdFileName.length() - 1);
				File nsdFile = new File(nsdFileName);
				if (!nsdFile.isAbsolute()) {
					if (_extractDir != null) {
						this.path = _extractDir.getAbsolutePath() + File.separator + nsdFileName;
					}
					if (_fromArchive != null) {
						this.virtPath = _fromArchive.getAbsolutePath() + File.separator + nsdFileName;
					}
				}
			}
			if (fields.count() >= 6) {
				this.name = fields.get(3).trim();
				if (this.name.length() >=3 && this.name.startsWith("\"") && this.name.endsWith("\"")) {
					this.name = this.name.substring(1, this.name.length()-1);
				}
				this.minArgs = Integer.parseInt(fields.get(4));
				this.maxArgs = Integer.parseInt(fields.get(5));
			}
		}
		
		public String getPath()
		{
			if (this.path == null) {
				return this.virtPath;
			}
			return this.path;
		}
		
		public File getFile()
		{
			File file = null;
			String path = getPath();
			if (path == null) {
				file = new File(path);
			}
			return file;
		}
		
		public String getSignature()
		{
			String signature = null;
			if (this.name != null) {
				signature = this.name;
				if (this.minArgs >= 0 && this.maxArgs >= 0) {
					signature += "(" + this.minArgs + "-" + this.maxArgs + ")";
				}
			}
			else if (this.record.root != null) {
				signature = this.record.root.getSignatureString(false);
			}
			return signature;
		}
		
		@Override
		public String toString()
		{
			String signature = this.name;
			if (this.minArgs >= 0 && this.maxArgs >= 0) {
				signature += "(" + this.minArgs + "-" + this.maxArgs + ")";
			}
			return getClass().getSimpleName() + "(" + this.record.toString() + ", " + signature + ": " + this.getPath() + ")";
		}
	}
	
	public class ArchiveIndex
	{
		public File arrFile;
		public List<ArchiveIndexEntry> entries;
		
		public ArchiveIndex(File _arrFile, List<ArchiveIndexEntry> _entries)
		{
			arrFile = _arrFile;
			entries = _entries;
			if (_entries == null) {
				entries = new LinkedList<ArchiveIndexEntry>();
				Scanner in;
				try {
					in = new Scanner(_arrFile, "UTF8");
					while (in.hasNextLine())
					{
						entries.add(new ArchiveIndexEntry(in.nextLine(), null, null));
					}
					in.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return this.getClass().getName() + "(" + this.arrFile + ": " + this.entries.size() + ")";
		}
		
	}
	
	/**
	 * Creates an instance
	 */
	public Archivar() {
		logger = Logger.getLogger(this.getClass().getName());
	}
	
	/**
	 * Finds a directory for temporary files (trying different OS standard environment variables)
	 * @return path of a temp directory
	 */
	public static File findTempDir()
	{
		String[] EnvVariablesToCheck = { "TEMP", "TMP", "TMPDIR" };
		String tempDir = "";
		for (int i = 0; (tempDir == null || tempDir.isEmpty()) && i < EnvVariablesToCheck.length; i++)
		{
			tempDir = System.getenv(EnvVariablesToCheck[i]);
		}
		if (tempDir == null || tempDir.isEmpty() || !(new File(tempDir).isDirectory())) {
			try {
				File tempFile = File.createTempFile("arr", null);
				tempDir = tempFile.getParent();
				tempFile.delete();
			} catch (IOException e) {
				EnvVariablesToCheck = new String[]{ "HOME", "HOMEPATH" };
				for (int i = 0; (tempDir == null || tempDir.isEmpty()) && i < EnvVariablesToCheck.length; i++)
				{
					tempDir = System.getenv(EnvVariablesToCheck[i]);
				}
			}
			
		}
		return new File(tempDir);
	}
	
	/**
	 * Creates or provids a directory with name {@link _dirName} in the standard temp directory
	 * @param _dirName - name of the temporary directory to be created or found (no path!).
	 * @return the {@link File} object for the requested directory
	 */
	public static File makeTempDir(String _dirName)
	{
		File tmpDir = findTempDir();
		File tempDir = new File(tmpDir.getAbsolutePath() + File.separator + _dirName);
		if (!tempDir.exists() && !tempDir.isDirectory()) {
			tempDir.delete();
		}
		if (!tempDir.exists()) {
			tempDir.mkdir();
		}
		return tempDir;
	}

	/**
	 * Ensures all {@link Root} objects given as {@code _roots} get saved and will be packed into
	 * a new arrz file {@code _targetFile}.
	 * @param _archive - the target file
	 * @param _roots - the diagrams to be archived
	 * @param _arrangeDiagonally - if true then coordinates with equally growing x and y will be given
	 * to the diagrams in the arr file, otherwise all will be given a dummy location of (-1, -1), indicating
	 * a dynamic arrangement on loading by Arranger.
	 * @param _troubles - a {@link StringList} the description of possible problems are appended to, or null. 
	 * @return a temporary archive path if the intended {@code _archive} file had already existed, otherwise null
	 * @throws ArchivarException if some problem occurred and {@code _troubles} was null
	 */
	public String zipArrangement(File _archive, Collection<Root> _roots, boolean _arrangeDiagonally, StringList _troubles) throws ArchivarException
	{
		Collection<ArchiveRecord> items = new LinkedList<ArchiveRecord>();
		int x = 0, y = 0;
		for (Root root: _roots) {
			if (_arrangeDiagonally) {
				items.add(new ArchiveRecord(root, new Point(x += COORD_INCREMENT, y += COORD_INCREMENT)));
			}
			else {
				items.add(new ArchiveRecord(root));
			}
		}
		return saveArrangement(items, findTempDir().getAbsolutePath() + File.separator + _archive.getName().replace(".arrz", ".arr"), _archive, null, null, _troubles);
	}

	/**
	 * Creates an arrangement archive {@code _targetFile} from the arrangement list file given by {@code _arrFile} 
	 * @param _targetFile - {@link File} object holding the path for the arrangement archive
	 * @param _arrFile - {@link File} object associated with an arrangement list file to be used for archiving
	 * @param _troubles - a {@link StringList} the ddescription of possible problems are appended to or null. 
	 * @return the path of the created temporary file if the target file `zipFilename´ had
	 * existed (otherwise null)
	 * @throws ArchivarException 
	 */
	public String zipArrangement(File _targetFile, File _arrFile, StringList _troubles) throws ArchivarException
	{
		// We will first have to read the arrangement list and to compose the list of File objects
		// TODO
		if (_targetFile != null) {
			throw new ArchivarException("Still not implemented");
		}
		return null;
	}

	/**
	 * Creates an arrangement list with path {@code _arrFilePath}, and possibly an archive {@code _targetFile},
	 * from the {@link ArchiveRecord}s given in {@code _items}.<br/>
	 * If the creation of an archive is intended (i.e. {@code _archive} is given then the arranger
	 * list file will only contain the pure file names (without absolute path) of the nsd files
	 * of the archive items.
	 * @param _items - collection of @ArchiveRecord items to form the arrangement list from it
	 * @param _arrFilePath - path for the arrangement list file to be created
	 * @param _archive - {@link File} object holding the path for the arrangement archive or null
	 * @param _virginTargetDir - a target directory where to save new (virgin) diagrams (if null they will be skipped)
	 * @param _offset - either null or some positive coordinate offset to be subtracted from all locations
	 * @param _troubles - a {@link StringList} to collect error messages. If null, then ArchivarException will be raised
	 * @return the path of the created temporary file if the target file `zipFilename´ had existed
	 * (otherwise null)
	 * @throws ArchivarException if {@code _troubles} is null
	 */
	public String saveArrangement(Collection<ArchiveRecord> _items, String _arrFilePath, File _archive, File _virginTargetDir, Point _offset, StringList _troubles) throws ArchivarException
	{
		String tmpArchive = null;
		int offsetX = 0, offsetY = 0;
		if (_offset != null) {
			offsetX = Math.max(_offset.x, 0);
			offsetY = Math.max(_offset.y, 0);
		}
		/////////////////////////////////////////////////////////////////
		// First we must gather the Root info and create the arr file
		/////////////////////////////////////////////////////////////////
		StringList filePaths = new StringList();
		Writer out = null;
		try {
			FileOutputStream fos = new FileOutputStream(_arrFilePath);
			out = new OutputStreamWriter(fos, "UTF8");
			for (ArchiveRecord item: _items) {
				String path = item.root.getPath();
				try {
					if (path.isEmpty()) {
						if (_virginTargetDir == null || !saveVirginNSD(item.root, _virginTargetDir)) {
							continue;
						}
					}
					if (item.point != null) {
						out.write(Integer.toString(Math.max(item.point.x - offsetX, 0)) + ",");
						out.write(Integer.toString(Math.max(item.point.y - offsetY, 0)) + ",");
					}
					else {
						out.write("-1,-1,");
					}
					StringList entry = new StringList();
					if (_archive != null)
					{
						File nsdFile = new File(path);
						path = nsdFile.getName();	// Only last part of path
					}
					entry.add(path);
					// The following entries are new for enh. #696
					entry.add(item.root.getMethodName());
					int minArgs = -1, maxArgs = -1;
					if (item.root.isInclude()) {
						minArgs = maxArgs = -2;
					}
					else if (item.root.isSubroutine()) {
						minArgs = item.root.getMinParameterCount();
						maxArgs = item.root.getParameterNames().count();
					}
					out.write(entry.getCommaText() + ',' + minArgs + ',' + maxArgs + '\n');
					
					if ((path = item.root.shadowFilepath) == null) {
						path = item.root.getPath();
					};
					filePaths.add(path);
				}
				catch (IOException ex) {
					if (_troubles != null) {
						_troubles.add(item.root.getSignatureString(false) + ": " + ex.toString());
					}
					else {
						throw new ArchivarException(item.root.getSignatureString(false), ex);
					}
				}
			}
		}
		catch (IOException ex) {
			String message = "Failed to write \"" + _arrFilePath + "\"";
			if (_troubles != null) {
				_troubles.add(message + ": " + ex.toString());
			}
			else {
				throw new ArchivarException(message, ex);
			}
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (IOException e) {
					if (_troubles != null) {
						_troubles.add(e.toString());
					}
					else {
						throw new ArchivarException("Failed to close arr file \"" + _arrFilePath + "\"" , e);
					}
				}
			}
		}
		/////////////////////////////////////////////////////////////////
		// Now we may produce the arrangement archive if specified so
		/////////////////////////////////////////////////////////////////		
		if ((_troubles == null || _troubles.isEmpty()) && _archive != null) {
			filePaths.add(_arrFilePath);
			try {
				tmpArchive = compressFiles(_archive, filePaths);
			}
			catch (IOException ex) {
				if (_troubles != null) {
					_troubles.add(_archive.getAbsolutePath() + ": " + ex.toString());
				}
				else {
					throw new ArchivarException("Failed to compress files in " + _archive.getAbsolutePath(), ex);
				}
			}
		}
		
		return tmpArchive;
	}
	

	/**
	 * Saves "virgin" (i.e. unsaved) {@link Root} to an NSD file with proposed name into directory
	 * {@code _targetDir}.<br/>
	 * Will notify registered {@link Updater}s after successful writing
	 * attempt and set file path in {@code _root}.
	 * @param _root - the {@link Root} to be saved (assumed to be the first time).
	 * @param _targetDir - the target folder for saving {@code _root}
	 * @return true iff the saving was successful.
	 */
	private boolean saveVirginNSD(Root _root, File _targetDir) throws IOException
	{
		boolean done = false;
		String filename = _targetDir.getAbsolutePath() + File.separator + _root.proposeFileName();
		File file = new File(filename + ".nsd");
		int count = 1;
		while (file.exists()) {
			file = new File(filename + "_" + count++ + ".nsd");
		}
		filename = file.getAbsolutePath();
		Writer out = null;
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			out = new OutputStreamWriter(fos, "UTF-8");
			XmlGenerator xmlgen = new XmlGenerator();
			out.write(xmlgen.generateCode(_root, "\t"));
			_root.filename = filename;
			_root.rememberSaved();
			done = true;
		}
		finally {
			if (out != null) {
				out.close();
			}
		}
		return done;
	}
	
	/**
	 * Compresses the files listed in {@code _filePaths} into {@code _archive}. If an archive with this
	 * name had existed, however, then we will create the archive as "tmpArchivar.zip" in a standard
	 * temp directory and return the actual path.
	 * @param _archive - the target file
	 * @param _filePaths - {@link StringList} of the absolute paths of the files to be compressed 
	 * @return path of the temporary archive if the specified target file {@link _archive} had existed
	 * @throws IOException in case some IO operation went wrong.
	 */
	private String compressFiles(File _archive, StringList _filePaths) throws IOException
	{
		final int BUFSIZE = 2048;
		String zipFilePath = _archive.getAbsolutePath();
		boolean fileExisted = _archive.exists(); 
		if (fileExisted)
		{
			// don't overwrite the original file but write to a temporary location instead
			// e prefer a recognizable path rather than a unique path, such that the user may identify the file
			File tmpDir = findTempDir();
			zipFilePath = tmpDir.getAbsolutePath() + File.separator + TEMP_ARCHIVE_NAME;
		}
		BufferedInputStream origin = null;
		FileOutputStream dest = new FileOutputStream(zipFilePath);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		try	{
			byte buffer[] = new byte[BUFSIZE];

			int count = 0;
			for (int i = 0; i < _filePaths.count(); i++) {
				File file = new File(_filePaths.get(i));
				FileInputStream fis = new FileInputStream(file);
				origin = new BufferedInputStream(fis, BUFSIZE);
				ZipEntry entry = new ZipEntry(file.getName());
				// START KGU 2018-09-12: Preserve time attributes if possible
				Path srcPath = file.toPath();
				try {
					BasicFileAttributes attrs = Files.readAttributes(srcPath, BasicFileAttributes.class);
					FileTime modTime = attrs.lastModifiedTime();
					FileTime creTime = attrs.creationTime();
					if (modTime.toMillis() > 0) {
						entry.setLastModifiedTime(modTime);
					}
					if (creTime.toMillis() > 0) {
						entry.setCreationTime(creTime);
					}
				} catch (IOException e) {
					logger.log(Level.WARNING, "Failed to set attributes of " + file.getName() + " in " + zipFilePath, e);
					// Let's go on...
				}
				// END KGU 2018-09-12
				out.putNextEntry(entry);
				while((count = origin.read(buffer, 0, BUFSIZE)) != -1)
				{
					out.write(buffer, 0, count);
				}
				origin.close();
			}
		}
		finally {
			out.close();
		}
		if (!fileExisted) {
			// If we could use the intended _targetFile then we should not return anything
			zipFilePath = null;
		}
		return zipFilePath;
	}
	
	/**
	 * 
	 * @param _arrFile
	 * @param _fromArchive
	 * @param _tempDir
	 * @param _troubles
	 * @return
	 */
	public List<ArchiveRecord> loadArrangement(File _arrFile, File _fromArchive, File _tempDir, StringList _troubles)
	{
		LinkedList<ArchiveRecord> items = null;
		Scanner in;
		try {
			in = new Scanner(_arrFile, "UTF8");
			items = new LinkedList<ArchiveRecord>();
			while (in.hasNextLine())
			{
				String line = in.nextLine();
				StringList fields = StringList.explode(line, ",");
				if (fields.count() >= 3)
				{
					Root root = null;
					Point point = new Point();
					point.x = Integer.parseInt(fields.get(0));
					point.y = Integer.parseInt(fields.get(1));
					String nsdFileName = fields.get(2);
					if (nsdFileName.startsWith("\""))
						nsdFileName = nsdFileName.substring(1);
					if (nsdFileName.endsWith("\""))
						nsdFileName = nsdFileName.substring(0, nsdFileName.length() - 1);
					File nsd = new File(nsdFileName);
					if (nsd.exists()) {
						root = loadNSDFile(nsd, _fromArchive, _troubles);
					}
					else if (!nsd.isAbsolute() && _tempDir != null) {
						nsd = new File(_tempDir.getAbsolutePath() + File.separator + nsdFileName);
						root = loadNSDFile(nsd, _fromArchive, _troubles);
					}
					// It might be that the arr file refers to a virtual arrz path (#656)
					else if (_fromArchive == null && nsdFileName.contains(".arrz")) {
						File arrzFile = nsd.getParentFile();
						String pureName = nsd.getName();
						if (arrzFile.exists()) {
							root = extractNSDFrom(arrzFile, pureName, null, _troubles);
						}
					}
					if (root != null) {
						items.add(new ArchiveRecord(root, point));
					}
				}
			}

			in.close();
		} catch (FileNotFoundException e) {
			_troubles.add(_arrFile.getAbsolutePath() + ": " + e.toString());
			logger.log(Level.SEVERE, "Missing arrangement file: " + _arrFile.getAbsolutePath(), e);
		}

		return items;
	}

	/**
	 * Loads the {@link Root} from the given file {@code _nsdFile}. If the file had been
	 * exracted from an arrangement archive then the archive file should be provided as
	 * {@code _fromArchive} to allow attribute inference and the proper setting of the
	 * virtual and shadow path. 
	 * @param _nsdFile - the NSD file to load
	 * @param _fromArchive - the archive file {@code _nsdFile} was extracted from, or null.
	 * @param _troubles - a {@link StringList} error messages may be added to.
	 * @return the loaded {@link Root} or null (in case loading failed for some reason)
	 * @see #extractNSDFrom(File, String, File, StringList)
	 * @see #unzipArrangement(File, File)
	 */
	private Root loadNSDFile(File _nsdFile, File _fromArchive, StringList _troubles) {
		Root root = null;
		// open an existing file
		NSDParser parser = new NSDParser();
		try {
			// The second argument improves attribute information for temporary legacy nsd files extracted from arrz files (KGU#363)
			root = parser.parse(_nsdFile, _fromArchive);

			root.filename = _nsdFile.getAbsolutePath();
			// Enh. #318 Allow nsd files to "reside" in arrz files
			if (_fromArchive != null) {
				root.filename = _fromArchive.getAbsolutePath() + File.separator + _nsdFile.getName();
				root.shadowFilepath = _nsdFile.getAbsolutePath();
			}
			root.getVarNames();	// Initialise the variable table, otherwise the highlighting won't work
		}
		catch (Exception ex) {
			String errorMessage = ex.getLocalizedMessage();
			if (errorMessage == null && (errorMessage = ex.getMessage()) == null) {
				errorMessage = ex.toString();
			}
			_troubles.add(_nsdFile.getAbsolutePath() + ": " + errorMessage);
		}
		return root;
	}

	/**
	 * Selectively extracts a single NSD file with name {@code _nsdName} (no path!) from the
	 * arrangement archive {@code _arrzFile} into target directory {@code _targetDir} and loads
	 * it, returning the resulting {@link Root} if all went well.
	 * @param _arrzFile - the arrangement archive
	 * @param _nsdName - the pure file name (without path)
	 * @param _targetDir - the extraction target folder (if null, a temp folder will be used)
	 * @param _troubles - a {@link StringList} collecting possible error messages
	 * @return - the extracted {@link Root} object, or null if something went wrong.
	 */
	public Root extractNSDFrom(File _arrzFile, String _nsdName, File _targetDir, StringList _troubles) {
		final int BUFSIZE = 2048;
		if (_targetDir == null) {
			_targetDir = findTempDir();
		}
		Root root = null;
		ZipFile zipfile = null;
		try {
			BufferedOutputStream dest = null;
			BufferedInputStream bistr = null;
			zipfile = new ZipFile(_arrzFile);
			Enumeration<? extends ZipEntry> entries = zipfile.entries();
			while(entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (_nsdName.equals(entry.getName())) {
					File targetFile = new File(_targetDir.getAbsolutePath() + File.separator + _nsdName);
					bistr = new BufferedInputStream
							(zipfile.getInputStream(entry));
					int count;
					byte buffer[] = new byte[BUFSIZE];
					FileOutputStream fostr = new FileOutputStream(targetFile);
					dest = new BufferedOutputStream(fostr, BUFSIZE);
					while ((count = bistr.read(buffer, 0, BUFSIZE))	!= -1)
					{
						dest.write(buffer, 0, count);
					}
					dest.flush();
					dest.close();
					bistr.close();
					// Preserve at least the modification time if possible
					Path destPath = (targetFile).toPath();
					try {
						Files.setLastModifiedTime(destPath, entry.getLastModifiedTime());
					} catch (IOException e) {}
					if (targetFile.exists()) {
						root = this.loadNSDFile(targetFile, _arrzFile, _troubles);
					}
					break;
				}
			}
		} catch(Exception ex) {
			logger.log(Level.WARNING, "Failed to extract the NSD file " + _nsdName, ex);
			_troubles.add(_arrzFile + File.separator + _nsdName + ": " + ex.toString());
		}
		finally {
			if (zipfile != null) {
				try {
					zipfile.close();
				} catch (IOException ex) {
					logger.log(Level.WARNING, "Archive file " + _arrzFile.getAbsolutePath() + " couldn't be closed.", ex);
				}
			}
		}
		return root;
	}

	/**
	 * Extracts all files contained in the archive file given by {@code _arrzFile} into the
	 * directory {@code _targetDir} or a temporary directory (if not given).
	 * @param _arrzFile - path of the arrangement archive file
	 * @param _targetDir - target directory path for the extraction (may be null)
	 * @return an {@link ArchiveIndex} object containing the arranger list file (*.arr) and
	 * its content as found in the extracted archive (or otherwise null). 
	 */
	public ArchiveIndex unzipArrangementArchive(File _arrzFile, File _targetDir)
	{
		ArchiveIndex archiveIndex = null;
		final int BUFSIZE = 2048;
		String arrFilename = null;
		if (_targetDir == null)
		{
			String dirName = _arrzFile.getName().toLowerCase();
			if (dirName.endsWith(".arrz")) {
				dirName = dirName.substring(0, dirName.length() - 5);
			}
			_targetDir = makeTempDir(dirName + ".unzip");
		}
		try {
			BufferedOutputStream dest = null;
			BufferedInputStream bistr = null;
			ZipEntry entry;
			ZipFile zipfile = new ZipFile(_arrzFile);
			Enumeration<? extends ZipEntry> entries = zipfile.entries();
			while(entries.hasMoreElements()) {
				entry = (ZipEntry) entries.nextElement();
				String targetName = _targetDir + File.separator + entry.getName();
				bistr = new BufferedInputStream
						(zipfile.getInputStream(entry));
				int count;
				byte buffer[] = new byte[BUFSIZE];
				FileOutputStream fostr = new FileOutputStream(targetName);
				dest = new BufferedOutputStream(fostr, BUFSIZE);
				while ((count = bistr.read(buffer, 0, BUFSIZE))	!= -1)
				{
					dest.write(buffer, 0, count);
				}
				dest.flush();
				dest.close();
				bistr.close();
				// Preserve at least the modification time if possible
				Path destPath = (new File(targetName)).toPath();
				try {
					Files.setLastModifiedTime(destPath, entry.getLastModifiedTime());
				} catch (IOException e) {}
				if (ArrFilter.isArr(entry.getName()))
				{
					arrFilename = targetName;
				}
			}
			zipfile.close();
		} catch(Exception ex) {
			logger.log(Level.WARNING, "Failed to unzip the arrangement archive " + _arrzFile, ex);
		}
		if (arrFilename != null) {
			archiveIndex = new ArchiveIndex(new File(arrFilename), null);
		}
		return archiveIndex;
	}

	/**
	 * Inspects the content of the arrangement archive specified by {@code _arrzFile} and 
	 * derives an {@link ArchiveIndex} of it without extracting the files themselves unless
	 * the arrangement is of legacy format without signature and {@code _targetDir} is given,
	 * in which case the {@link Root}s will be extracted in order to get the signature info
	 * of the contained diagrams.
	 * @param _arrzFile - the archive file to be inspected
	 * @param _targetDir - an extraction directory: if not null and the arranger list doesn't
	 * contain the signaturs then arranger list file and the NSD files will be extracted there.
	 * @return the content overview
	 */
	public ArchiveIndex getArrangementArchiveContent(File _arrzFile, File _targetDir)
	{
		ArchiveIndex archiveIndex = null;
		String arrFileName = null;
		final int BUFSIZE = 2048;
		ZipFile zipfile = null;
		StringList arrContents = null;
		try {
			BufferedInputStream bistr = null;
			zipfile = new ZipFile(_arrzFile);
			Enumeration<? extends ZipEntry> entries = zipfile.entries();
			while(entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				StringBuilder strb = new StringBuilder();
				if (ArrFilter.isArr(entry.getName())) {
					arrFileName = _arrzFile.getAbsolutePath() + File.separator + entry.getName();
					bistr = new BufferedInputStream
							(zipfile.getInputStream(entry));
					try {
					int count;
					byte buffer[] = new byte[BUFSIZE];
					while ((count = bistr.read(buffer, 0, BUFSIZE))	!= -1)
					{
						strb.append((new String(buffer, "UTF-8")).substring(0, count));
					}
					}
					finally {
						if (bistr != null) bistr.close();
					}
					arrContents = StringList.explode(strb.toString(), "\n");
					break;
				}
			}
		} catch(Exception ex) {
			logger.log(Level.WARNING, "Failed to inspect the arr file of " + _arrzFile.getAbsolutePath(), ex);
		}
		finally {
			if (zipfile != null) {
				try {
					zipfile.close();
				} catch (IOException ex) {
					logger.log(Level.WARNING, "Archive file " + _arrzFile.getAbsolutePath() + " couldn't be closed.", ex);
				}
			}
		}
		if (arrContents != null) {
			List<ArchiveIndexEntry> entries = new LinkedList<ArchiveIndexEntry>();
			for (int i = 0; i < arrContents.count(); i++) {
				ArchiveIndexEntry entry = new ArchiveIndexEntry(arrContents.get(i), _arrzFile, null);
				if (entry.getSignature() == null) {
					StringList troubles = new StringList();
					Root root = extractNSDFrom(_arrzFile, entry.getPath(), _targetDir, troubles);
					if (root != null) {
						entry = new ArchiveIndexEntry(root, entry.record.point);
					}
				}
				entries.add(new ArchiveIndexEntry(arrContents.get(i), _arrzFile, null));
			}
			archiveIndex = new ArchiveIndex(new File(arrFileName), entries);
		}
		return archiveIndex;
	}
}
