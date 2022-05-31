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
 *      Kay Gürtzig     2019-03-14      Enh. #697: New method deriveArrangementList()
 *      Kay Gürtzig     2019-03-26      Enh. #697: Bugfixes in zipArrangement(), saveArrangement()
 *      Kay Gürtzig     2019-07-31      Bugfix #731 (also comprising #526): new static methods renameTo, copyFile
 *      Kay Gürtzig     2019-10-14      Bugfix #763: Missing references files now add to the problem list on loading
 *      Kay Gürtzig     2020-04-23      Bugfix #860: ArchiveIndexEntry did not set path field with absolute nsd file paths
 *      Kay Gürtzig     2020-04-24      Bugfix #862/3: Ensure correct update of ArchiveIndexEntry on attaching the Root
 *      Kay Gürtzig     2020-10-19      Issue #875: Modifications to enable diagram insertion to archives
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
import java.util.Iterator;
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
	
	/**
	 * Archive index entry class, extending {@link ArchiveRecord} by path, name, and argument number information
	 * 
	 * @author Kay Gürtzig
	 */
	public class ArchiveIndexEntry extends ArchiveRecord {
		public String path;					// the true file path
		public String virtPath = null;		// the virtual path (if inside an archive)
		public String name = null;			// diagram name
		public int minArgs = -1;			// minimum number of routine arguments, or -1 for a program, or -2 for an includable
		public int maxArgs = -1;			// maximum number of routine arguments, or -1 for a program, or -2 for an includable
		
		/**
		 * Derives an entry from an {@link ArchiveRecord}
		 * 
		 * @param archiveRecord - the source {@link ArchiveRecord} (Note: The {@link ArchiveRecord#root} component
		 *     <b>must not be {@code null}!</b>)
		 */
		public ArchiveIndexEntry(ArchiveRecord archiveRecord)
		{
			super(archiveRecord);
			setRoot(this.root);
		}
		
		/**
		 * Derives an entry from a {@link Root} and its graphical location {@code point}.
		 * 
		 * @param root - The diagram (must not be null!)
		 * @param point - the graphical location (may be null)
		 */
		public ArchiveIndexEntry(Root root, Point point)
		{
			this(new ArchiveRecord(root, point));
		}
		
		/**
		 * Builds an entry from explicitly given values.<br/>
		 * Important: At least one of {@code _path} or {@code _virtulalPath} must be given!
		 * 
		 * @param _point - graphical location (may be {@code null})
		 * @param _path - actual file path of the nsd file
		 * @param _virtPath - virtual file path inside the source archive
		 * @param _diagramName - name of the diagram (signature part)
		 * @param _minArgs - minimum argument number (-1 for main, -2 for includable)
		 * @param _minArgs - maximum argument number (-1 for main, -2 for includable)
		 */
		public ArchiveIndexEntry(Point _point, String _path, String _virtualPath, String _diagramName, int _minArgs, int _maxArgs)
		{
			super(null, _point);
			this.path = _path;
			this.virtPath = _virtualPath;
			this.name = _diagramName;
			this.minArgs = _minArgs;
			this.maxArgs = _maxArgs;
		}
		
		/** Derives an entry from an arranger list file line.
		 * 
		 * @param arrangementLine - line of an arrangement file
		 * @param _fromArchive - originating arrangement archive
		 * @param _extractDir - extraction directory (for signature retrieval if info isn't part of the line)
		 */
		public ArchiveIndexEntry(String arrangementLine, File _fromArchive, File _extractDir)
		{
			super(null, null);
			StringList fields = StringList.explode(arrangementLine, ",");	// FIXME what if a path or the name contains a comma?
			if (fields.count() >= 3)
			{
				this.point = new Point(Integer.parseInt(fields.get(0)), Integer.parseInt(fields.get(1)));
				String nsdFileName = fields.get(2);
				if (nsdFileName.startsWith("\""))
					nsdFileName = nsdFileName.substring(1);
				if (nsdFileName.endsWith("\""))
					nsdFileName = nsdFileName.substring(0, nsdFileName.length() - 1);
				File nsdFile = new File(nsdFileName);
				// START KGU#858 2020-04-23: Bugfix #860 We forgot to set the path in case it IS absolute
				this.path = nsdFileName;
				// END KGU#858 2020-04-23
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

		/**
		 * @return the stored {@link Root}.
		 * 
		 * @see #getRoot(Archivar)
		 * @see #getFile()
		 */
		public Root getRoot() {
			return this.root;
		}

		/** 
		 * Returns the stored {@link Root}. If the {@link Root} hadn't been stored and an
		 * {@code archivar} is given then the diagram is retrieved and, if successful, 
		 * cached in this entry.
		 * 
		 * @param archivar - optionally an {@link Archivar} for {@link Root} retrieval
		 * @return the {@link Root} object if already loaded or after having been retrieved.
		 * 
		 * @throws Exception if something goes wrong on retrieval
		 * 
		 * @see #getRoot()
		 * @see #getFile()
		 */
		public Root getRoot(Archivar archivar) throws Exception
		{
			if (this.root == null && archivar != null) {
				if (this.path != null) {
					File arrzFile = null;
					if (this.virtPath != null) {
						arrzFile = (new File(this.virtPath)).getParentFile();
					}
					this.setRoot(archivar.loadNSDFile(this.getFile(), arrzFile, null));
				}
			}
			return this.root;
		}
		
		/**
		 * Sets the given {@link Root} object and then updates all related information
		 * 
		 * @param root - the diagram to be set
		 * @return true if the {@code root} was accepted
		 */
		protected boolean setRoot(Root root)
		{
			// START KGU#861 2020-04-24: Bugfix #862/3 - was nonsense, see comment below
			//if (root == null || this.root != null) {
			/* This method is either called in getRoot() if this.root IS null or in the
			 * constructor after this.root hs already been set and hardly anything else.
			 * So we MUST update all information here (except perhaps another Root had
			 * been associated. But the result hasn't been asked for nowhere by now
			 */
			if (root == null || this.root != null && this.root != root) {
			// END KGU#861 2020-04-24
				return false;
			}
			this.root = root;
			this.path = root.shadowFilepath;
			if (this.path == null) {
				this.path = root.getPath();
			}
			else {
				this.virtPath = root.getPath();
			}
			this.name = root.getMethodName();
			if (root.isInclude()) {
				this.minArgs = this.maxArgs = -2;
			}
			else if (root.isSubroutine()) {
				this.minArgs = root.getMinParameterCount();
				this.maxArgs = root.getParameterNames().count();
			}
			return true;
		}
		
		/**
		 * @return either the actual nsd file path or the virtual path within the source archive
		 * 
		 * @see #getFile()
		 */
		public String getPath()
		{
			if (this.path == null) {
				return this.virtPath;
			}
			return this.path;
		}
		
		/**
		 * @return the source file - may be an actual nsd file or a virtual file path inside an archive
		 * 
		 * @see #getPath()
		 */
		public File getFile()
		{
			File file = null;
			String path = getPath();
			if (path != null) {
				file = new File(path);
			}
			return file;
		}
		
		/**
		 * @return the routine signature string as {@code name(minArgs-maxArgs)} or just the diagram name (or null)
		 */
		public String getSignature()
		{
			String signature = null;
			if (this.name != null) {
				signature = this.name;
				if (this.minArgs >= 0 && this.maxArgs >= 0) {
					
					signature += "(" + this.minArgs + (this.maxArgs > this.minArgs ? "-" + this.maxArgs : "") + ")";
				}
			}
			else if (this.root != null) {
				signature = this.root.getSignatureString(false, false);
			}
			return signature;
		}
		
		/**
		 * Compares this entry with other and checks whether the {@link Root} objects or
		 * the files are equivalent. Signatures are not significant here.
		 * 
		 * @param other - another index entry
		 * @return {@code true} iff (= if and only if) both entries are equivalent
		 */
		public boolean equals(ArchiveIndexEntry other)
		{
			boolean equivalent = false;
			File file = this.getFile();
			if (this.root != null && this.root == other.root) {
				equivalent = true;
			}
			else if (file != null && file.equals(other.getFile())) {
				equivalent = true;
			}
			return equivalent;
		}
		
		@Override
		public String toString()
		{
			String signature = this.name;
			if (this.minArgs >= 0 && this.maxArgs >= 0) {
				signature += "(" + this.minArgs + "-" + this.maxArgs + ")";
			}
			return getClass().getSimpleName() + "(" + super.toString() + ", " + signature + ": " + this.getPath() + ")";
		}

	}
	
	/**
	 * Effectively a list of {@link ArchiveIndexEntry} objects, also holding the
	 * corresponding arrangement list file if the index hasn't been merged with another.
	 * 
	 * @author Kay Gürtzig
	 */
	public class ArchiveIndex
	{
		/** The extracted arrangement list */
		public File arrFile;
		public List<ArchiveIndexEntry> entries;
		
		private ArchiveIndex() {
			this.arrFile = null;
			this.entries = new LinkedList<ArchiveIndexEntry>();
		}

		/**
		 * Constructs an ArchiveIndex object from the given {@link ArchiveIndexEntry} list {@code _entries}
		 * or the specified arrangement list file {@code _arrFile} if {@code _engtries} is {@code null}.
		 * 
		 * @param _arrFile - the arrangement list file from which the entries are to be extracted if
		 *     {@code _entries} is {@code null}
		 * @param _entries - the list of {@link ArchiveIndexEntry} objects defining the archive content
		 * @param _arrzFile - optionally an associated arrangement archive file (path information)
		 */
		public ArchiveIndex(File _arrFile, List<ArchiveIndexEntry> _entries, File _arrzFile)
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
						entries.add(new ArchiveIndexEntry(in.nextLine(), _arrzFile, _arrFile.getParentFile()));
					}
					in.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
		/**
		 * Appends the given {@code entry} to this index, if {@code checkDuplicate}
		 * is {@code false} or there is no equivalent entry.
		 * 
		 * @param entry - the {@link ArchiveIndexEntry} to be added
		 * @param checkDuplicate - if {@code true} then root object or path is compared
		 * @return {@code true} if anything changed (i.e. {@code entry} has been added) 
		 */
		public boolean add(ArchiveIndexEntry entry, boolean checkDuplicate)
		{
			boolean added = false;
			if (!checkDuplicate || !this.entries.contains(entry)) {
				added = this.entries.add(entry);
			}
			return added;
		}
		
		/**
		 * Appends all of the entries in the specified collection to the end of this list,
		 * in the order that they are returned by the specified collection's iterator.
		 * 
		 * @param entries - collection of {@link ArchiveIndexEntry}
		 * @param checkDuplicates - if {@code true} then root objects or paths are compared
		 * @return {@code true} if anything changed (i.e. {@code entries} wasn't empty) 
		 */
		public boolean addAll(Collection<? extends ArchiveIndexEntry> entries, boolean checkDuplicates)
		{
			boolean added = false;
			if (checkDuplicates) {
				for (ArchiveIndexEntry entry: entries) {
					if (!this.entries.contains(entry)) {
						added = this.entries.add(entry) || added;
					}
				}
			}
			else {
				added = this.entries.addAll(entries);
			}
			return added;
		}
		
		/**
		 * Appends all of the entries from the other ArchiveIndex which had no equivalent
		 * in this archive index. If both arrFiles differ then arrFile entry gets cleared.
		 * 
		 * @param other - the source {@link ArchiveIndex}
		 * @return  {@code true} if anything changed 
		 */
		public boolean addAll(ArchiveIndex other)
		{
			boolean changed = this.addAll(other.entries, true);
			if (changed && this.arrFile != null && !this.arrFile.equals(other.arrFile)) {
				this.arrFile = null;
			}
			return changed;
		}
		
		/**
		 * Adds a new {@link ArchiveIndexEntry} for the given {@link Root} {@code root}
		 * and {@code point}.
		 * 
		 * @param root - The diagram to be added, should ideally have a file representation
		 * @param point - graphical location (arrangement), may be {@code null}
		 * @return true if the addition worked
		 */
		public boolean addEntryFor(Root root, Point point)
		{
			boolean added = false;
			ArchiveIndexEntry entry = new ArchiveIndexEntry(root, point);
			if (!entries.contains(entry)) {
				added = this.entries.add(entry);
			}
			return added;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return this.getClass().getName() + "(" + this.arrFile + ": " + this.entries.size() + ")";
		}

		/** @return true if there is no entry in this index */
		public boolean isEmpty() {
			return this.entries == null || this.entries.isEmpty();
		}
		
		/** @return the number of entries in this archive index */
		public int size()
		{
			int size = 0;
			if (this.entries != null) {
				size = this.entries.size();
			}
			return size;
		}
		
		/**
		 * @return an iterator for the contained {@link ArchiveIndexEntry} elements, or possibly null!
		 */
		public Iterator<ArchiveIndexEntry> iterator()
		{
			return this.entries.iterator();
		}
		
		/**
		 * Composes the lines of an arrangement file reflecting the cotents of this ArchiveIndex.
		 * @param preferArchivePaths - whether virtual paths of the contained diagrams are to
		 * preferred (or otherwise the "real" or shadow paths)
		 * @return a {@link StringList} of the lines the corresponding arrangement list file would contain
		 */
		public StringList deriveArrangementList(boolean preferArchivePaths)
		{
			StringList arr = new StringList();
			for (ArchiveIndexEntry entry: entries) {
				StringList line = new StringList();
				if (entry.point != null) {
					line.add(Integer.toString(entry.point.x));
					line.add(Integer.toString(entry.point.y));
				}
				else {
					line.add("-1");
					line.add("-1");
				}
				// The columns of path and name are generated together
				StringList pathName = new StringList();
				// Derive the file path
				if ((preferArchivePaths || entry.path == null) && entry.virtPath != null) {
					pathName.add(entry.virtPath);
				}
				else if (entry.path != null) {
					pathName.add(entry.path);
				}
				else if (entry.root != null) {
					pathName.add(entry.root.getPath());
				}
				else {
					pathName.add("");
				}
				// Now add the diagram name
				if (entry.name != null) {
					pathName.add(entry.name);
				}
				else if (entry.root != null) {
					pathName.add(entry.root.getMethodName());
				}
				else {
					pathName.add("");
				}
				line.add(pathName.getCommaText());
				line.add(Integer.toString(entry.minArgs));
				line.add(Integer.toString(entry.maxArgs));
				arr.add(line.concatenate(","));
			}
			return arr;
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
	 * 
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
	 * Creates or provides a directory with name {@link _dirName} in the standard temp directory
	 * 
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
	 * 
	 * @param _archive - the target file
	 * @param _roots - the diagrams to be archived
	 * @param _arrangeDiagonally - if true then coordinates with equally growing x and y will be given
	 * to the diagrams in the arr file, otherwise all will be given a dummy location of (-1, -1), indicating
	 * a dynamic arrangement on loading by Arranger.
	 * @param _troubles - a {@link StringList} the description of possible problems are appended to, or null. 
	 * @return a temporary archive path if the intended {@code _archive} file had already existed, otherwise null
	 * 
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
		// START KGU#678 2019-03-26: Enh. #697 we need that virgin diagrams get saved
		//return saveArrangement(items, findTempDir().getAbsolutePath() + File.separator + _archive.getName().replace(".arrz", ".arr"), _archive, null, null, _troubles);
		File tempDir = findTempDir();
		return saveArrangement(items, tempDir.getAbsolutePath() + File.separator + _archive.getName().replace(".arrz", ".arr"),
				_archive, tempDir, null, _troubles);
		// END KGU#678 2019-03-26
	}

	/**
	 * Creates an arrangement archive {@code _targetFile} from the arrangement list file given by {@code _arrFile}
	 *  
	 * @param _targetFile - {@link File} object holding the path for the arrangement archive
	 * @param _arrFile - {@link File} object associated with an arrangement list file to be used for archiving
	 * @param _troubles - a {@link StringList} the ddescription of possible problems are appended to or null. 
	 * @return the path of the created temporary file if the target file `zipFilename´ had
	 * existed (otherwise null)
	 * 
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
	 * If the creation of an archive is intended (i.e. {@code _archive} is given) then the arranger
	 * list file will only contain the pure file names (without absolute path) of the nsd files
	 * of the archive items.
	 * 
	 * @param _items - collection of @ArchiveRecord items to form the arrangement list from it
	 * @param _arrFilePath - path for the arrangement list file to be created
	 * @param _archive - {@link File} object holding the path for the arrangement archive or null
	 * @param _virginTargetDir - a target directory where to save new (virgin) diagrams (if null they will be skipped)
	 * @param _offset - either {@code null} or some positive coordinate offset to be subtracted from all locations
	 * @param _troubles - a {@link StringList} to collect error messages. If {@code null}, then ArchivarException will be raised
	 * @return the path of the created temporary file if the target file `zipFilename´ had existed
	 *     (otherwise {@code null})
	 * 
	 * @throws ArchivarException if {@code _troubles} is {@code null}
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
		// START KGU#874 2020-10-19: Issue #875 Allow etry names to differ from temp. file names
		StringList itemPaths = new StringList();
		// END KGU#874 2020-10-19
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
						// START KGU#678 2019-03-26: Bugfix on occasion of enh. #697
						path = item.root.getPath();
						// END KGU#678 2019-03-26
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
					// START KGU#874 2020-10-19: Issue #875 - make sure the name in archive is this
					itemPaths.add(path);
					// END KGU#874 2020-10-19
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
						_troubles.add(item.root.getSignatureString(false, false) + ": " + ex.toString());
					}
					else {
						throw new ArchivarException(item.root.getSignatureString(false, false), ex);
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
			// START KGU#874 2020-10-19: Issue #875
			itemPaths.add((new File(_arrFilePath)).getName());
			// END KGU#874 2020-10-19
			try {
				// START KGU#874 2020-10-19: Issue #875
				//tmpArchive = compressFiles(_archive, filePaths);
				tmpArchive = compressFiles(_archive, filePaths, itemPaths);
				// END KGU#874 2020-10-19
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
	 * 
	 * @param _root - the {@link Root} to be saved (assumed to be the first time).
	 * @param _targetDir - the target folder for saving {@code _root}
	 * @return {@code true} iff the saving was successful.
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
			out.write(xmlgen.generateCode(_root, "\t", true));
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
	 * 
	 * @param _archive - the target file
	 * @param _filePaths - {@link StringList} of the absolute paths of the files to be compressed 
	 * @param _itemNames - {@link StringList} of the internal names for the files to be compressed
	 * @return path of the temporary archive if the specified target file {@link _archive} had existed
	 * 
	 * @throws IOException in case some IO operation went wrong.
	 */
	// START KGU#874 2020-10-19: Issue #875 - The item names are not necessarily equal to the file names
	//private String compressFiles(File _archive, StringList _filePaths) throws IOException
	private String compressFiles(File _archive, StringList _filePaths, StringList _itemNames) throws IOException
	// END KGU#874 2020-10-19
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
				// START KGU#874 2020-10-19: Issue #875
				//ZipEntry entry = new ZipEntry(file.getName());
				ZipEntry entry = new ZipEntry(_itemNames.get(i));
				// END KGU#874 2020-10-19
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
	 * Loads the arrangement described in {@code _arrFile} and returns the diagrams and their locations
	 * at least. If necessary and either {@code _arrFile} is given or the listed path is a virtual path
	 * into an archive tries to extract the files, either into {@code _tempDir} if given or into a new
	 * temporary folder.
	 * 
	 * @param _arrFile - the arrangement list file containing the names or paths of the diagram files
	 * @param _fromArchive - the arrangement archive file if the arrangement originates in the archive (for extraction)
	 * @param _tempDir - the temporary directory where to read the diagram files from if the paths aren't absolute.
	 * @param _troubles - {@link StringList} to which occurring error messages will be added
	 * @return a list of {@link ArchiveRecord}s containing a {@link Root} and an arrangement location at least
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
					boolean fileMissing = false;
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
						// START KGU#749 2019-10-14: Bugfix #763 - we must inform about missing files
						else {
							fileMissing = true;
						}
						// END KGU#749 2019-10-14
					}
					// START KGU#749 2019-10-14: Bugfix #763 - we must inform about missing files
					else {
						fileMissing = true;
					}
					// END KGU#749 2019-10-14
					if (root != null) {
						items.add(new ArchiveRecord(root, point));
					}
					// START KGU#749 2019-10-14: Bugfix #763 - we must inform about missing files
					else if (fileMissing) {
						_troubles.add(_arrFile.getName() + ": \"" + nsd.getAbsolutePath() + "\" MISSING!");
					}
					// END KGU#749 2019-10-14
				}
			}

			in.close();
		} catch (FileNotFoundException e) {
			_troubles.add(_arrFile.getAbsolutePath() + ": " + e.toString());
			logger.log(Level.SEVERE, "Missing arrangement file: " + _arrFile.getAbsolutePath(), e);
		} catch (Exception ex) {
			_troubles.add(_arrFile.getAbsolutePath() + ": " + ex.toString());
			logger.log(Level.WARNING, "Trouble on loading arrangement: " + _arrFile.getAbsolutePath(), ex);
		}

		return items;
	}

	/**
	 * Loads the {@link Root} from the given file {@code _nsdFile}. If the file had been
	 * exracted from an arrangement archive then the archive file should be provided as
	 * {@code _fromArchive} to allow attribute inference and the proper setting of the
	 * virtual and shadow path.
	 * 
	 * @param _nsdFile - the NSD file to load
	 * @param _fromArchive - the archive file {@code _nsdFile} was extracted from, or {@code null}.
	 * @param _troubles - a {@link StringList} error messages may be added to.
	 * @return the loaded {@link Root} or null (in case loading failed for some reason)
	 * 
	 * @throws Exception if some problem occurs and {@code _troubles} is {@code null}
	 * 
	 * @see #extractNSDFrom(File, String, File, StringList)
	 * @see #unzipArrangement(File, File)
	 */
	private Root loadNSDFile(File _nsdFile, File _fromArchive, StringList _troubles) throws Exception {
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
			root.retrieveVarNames();	// Initialise the variable table, otherwise the highlighting won't work
		}
		catch (Exception ex) {
			String errorMessage = ex.getLocalizedMessage();
			if (errorMessage == null && (errorMessage = ex.getMessage()) == null) {
				errorMessage = ex.toString();
			}
			if (_troubles != null) {
				_troubles.add(_nsdFile.getAbsolutePath() + ": " + errorMessage);
			}
			else {
				throw ex;
			}
		}
		return root;
	}

	/**
	 * Selectively extracts a single NSD file with name {@code _nsdName} (no path!) from the
	 * arrangement archive {@code _arrzFile} into target directory {@code _targetDir} and loads
	 * it, returning the resulting {@link Root} if all went well.
	 * 
	 * @param _arrzFile - the arrangement archive
	 * @param _nsdName - the pure file name (without path)
	 * @param _targetDir - the extraction target folder (if {@code null}, a temp folder will be used)
	 * @param _troubles - a {@link StringList} collecting possible error messages
	 * @return - the extracted {@link Root} object, or {@code null} if something went wrong.
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
	 * 
	 * @param _arrzFile - path of the arrangement archive file
	 * @param _targetDir - target directory path for the extraction (may be {@code null})
	 * @return an {@link ArchiveIndex} object containing the arranger list file (*.arr) and
	 *     its content as found in the extracted archive (or otherwise {@code null}). 
	 */
	public ArchiveIndex unzipArrangementArchive(File _arrzFile, File _targetDir)
	{
		ArchiveIndex archiveIndex = null;
		final int BUFSIZE = 2048;
		File arrFile = null;
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
				File targetFile = new File(_targetDir + File.separator + entry.getName());
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
				if (ArrFilter.isArr(entry.getName()))
				{
					arrFile = targetFile;
				}
			}
			zipfile.close();
		} catch(Exception ex) {
			logger.log(Level.WARNING, "Failed to unzip the arrangement archive " + _arrzFile, ex);
		}
		if (arrFile != null) {
			archiveIndex = new ArchiveIndex(arrFile, null, _arrzFile);
		}
		return archiveIndex;
	}

	/**
	 * Inspects the content of the arrangement archive specified by {@code _arrzFile} and 
	 * derives an {@link ArchiveIndex} of it without extracting the files themselves unless
	 * the arrangement is of legacy format without signature and {@code _targetDir} is given,
	 * in which case the {@link Root}s will be extracted in order to get the signature info
	 * of the contained diagrams.
	 * 
	 * @param _arrzFile - the archive file to be inspected
	 * @param _targetDir - an extraction directory: if not {@code null} and the arranger list
	 *     doesn't contain the signatures then the arranger list file and the NSD files will
	 *     be extracted there.
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
					entry.root = extractNSDFrom(_arrzFile, entry.getPath(), _targetDir, troubles);
				}
				entries.add(new ArchiveIndexEntry(arrContents.get(i), _arrzFile, null));
			}
			archiveIndex = new ArchiveIndex(new File(arrFileName), entries, null);
		}
		return archiveIndex;
	}
	
	/**
	 * Creates a new index from the specified arrangement list file
	 * 
	 * @param _arrFile - the file containing the arrangement list
	 * @return the constructed ArchiveIndex object
	 */
	public ArchiveIndex makeNewIndexFor(File _arrFile)
	{
		return new ArchiveIndex(_arrFile, null, null);
	}
	
	/**
	 * Creates a new empty index.
	 * 
	 * @return the constructed empty ArchiveIndex object
	 */
	public ArchiveIndex makeEmptyIndex() {
		return new ArchiveIndex();
	}
	
	// START KGU#509/KGU#717 2019-07-31: Bugfix #526, #731 - workarounds for a failing File.renameTo() operation
	/**
	 * Moves/renames file {@code f1} to file {@code f2} in a operating-system-independent way.
	 * This is a replacement for {@link File#renameTo(File)} and a wrapper for
	 * {@link Files#move(Path, Path, java.nio.file.CopyOption...)}.
	 * @param f1 - source {@link File}
	 * @param f2 - {@link File} representing the target path.
	 * @return true if and only if no error occurred, the target file exists and the source file
	 * no longer exists at its original place.
	 * @see #copyFile(File, File, boolean)
	 */
	public static boolean renameTo(File f1, File f2)
	{
		boolean done = false;
		Path p1 = f1.toPath();
		Path p2 = f2.toPath();
		try {
			Files.move(p1, p2, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			// Let's be a little paranoid...
			done =  f2.exists() && (!f1.exists() || f1.getAbsolutePath().equals(f2.getAbsolutePath()));
		}
		catch (Exception ex) {
			Logger.getLogger(Archivar.class.getName()).log(Level.WARNING, "Failed to move \"" + f1 + "\" to \"" + f2 + "\"", ex);
		}
		return done;
	}
	
	/**
	 * Performs a bytewise copy of {@code sourceFile} to {@code targetFile} as workaround
	 * for Linux where {@link File#renameTo(File)} may fail among file systems. If the
	 * target file exists after the copy the source file will be removed.<br/>
	 * Note: Consider {@link #renameTo(File, File)} instead if {@code removeSource is true.}
	 * @param sourceFile
	 * @param targetFile
	 * @param removeSource - whether the {@code sourceFile} is to be removed after a successful
	 * copy
	 * @return in case of errors, a string describing them.
	 * @see #renameTo(File, File)
	 */
	public static String copyFile(File sourceFile, File targetFile, boolean removeSource) {
		String problems = "";
		final int BLOCKSIZE = 512;
		byte[] buffer = new byte[BLOCKSIZE];
		FileOutputStream fos = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(sourceFile.getAbsolutePath());
			fos = new FileOutputStream(targetFile.getAbsolutePath());
			int readBytes = 0;
			do {
				readBytes = fis.read(buffer);
				if (readBytes > 0) {
					fos.write(buffer, 0, readBytes);
				}
			} while (readBytes > 0);
		} catch (FileNotFoundException e) {
			problems += e + "\n";
		} catch (IOException e) {
			problems += e + "\n";
		}
		finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {}
			}
			if (fis != null) {
				try {
					fis.close();
					if (removeSource && targetFile.exists()) {
						sourceFile.delete();
					}
				} catch (IOException e) {}
			}
		}
		return problems;
	}
	// END KGU#509/KGU#717 2019-07-31

}
