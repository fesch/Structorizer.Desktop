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
package lu.fisch.structorizer.elements;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Entry structure of the variable type map held by Root
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.01.19      First Issue (enh. #259)
 *      Kay Gürtzig     2017.03.05      Method isArray(boolean) added and JavaDoc updated
 *      Kay Gürtzig     2017.04.14      Issue #394: Method variants of getCanonicalType() and getTypes(),
 *                                      argument mixup fixed, type assimilation support 
 *      Kay Gürtzig     2017.07.04      Issue #423: Structure changes to support record types and named
 *                                      type definitions
 *      Kay Gürtzig     2017.09.18      Enh. #423: dummy singleton introduced (impacts poorly tested!)
 *      Kay Gürtzig     2017.09.22      Bugfix #428 Defective replacement pattern for "short" in canonicalizeType(String)
 *      Kay Gürtzig     2017.09.29      Regex stuff revised (final Strings -> final Patterns)
 *      Kay Gürtzig     2018.07.12      Canonicalisation of type name "unsigned short" added.
 *
 ******************************************************************************************************
 *
 *      Comment: Entry contains a single-linked list of possibly conflicting specifications as well as
 *      references to elements with assigning and referencing use.
 *      
 *      FIXME: Redesign plan (Enh. #423): Replace the multi-level index range architecture in VarDeclaration
 *      by a single-level index range where the elementType would be a TypeMapEntry reference instead of
 *      a mere String. This way, a fully recursive type structure would be possible (e.g. records of arrays of
 *      arrays of records etc.). Maybe the multi-level index ranges could evene be preserved if the intermediate
 *      levels are of no individual meaning, though this obviously complicates type comparability.
 *      
 *      Further on, the use of the same structure for type definitions which may be shard among variables
 *      provoked an inconsistency: the sets of defining and modifying elements are misleading fpr shared type
 *      entries.
 *      In the event there should be a (recursive) type table withiut references to modifying elements
 *      and a separate map from variable names to entries, which collect data over the live cycle of the
 *      variable where one of the data items is a type link (or a list of type links, wth a declaring element
 *      each). The type entry itself will only need a link to the (first) defining element. 
 *
 ******************************************************************************************************///

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

/**
 * Recursive data record of the central type map for variables and named types.
 * @author Kay Gürtzig
 */
public class TypeMapEntry {
	
	// START KGU#388 2017-09-18: Enh. 423
	// A shared empty entry for undefined types
	private static TypeMapEntry dummy = null;
	// END KGU#388 2017-09-18
	// "Canonical" scalar type names
	private static final String[] canonicalNumericTypes = {
			"byte",
			"short",
			"int",
			"long",
			"float",
			"double"
	};
	private static final Pattern ARRAY_PATTERN1 = Pattern.compile("^[Aa][Rr][Rr][Aa][Yy]\\s*?[Oo][Ff](\\s.*)");
	private static final Pattern ARRAY_PATTERN1o = Pattern.compile("^[Aa][Rr][Rr][Aa][Yy]\\s*?(\\d*)\\s*?[Oo][Ff](\\s.*)");	// Oberon style
	private static final Pattern ARRAY_PATTERN2 = Pattern.compile("^[Aa][Rr][Rr][Aa][Yy]\\s*?\\[(.*)\\]\\s*?[Oo][Ff](\\s.*)");
	private static final Pattern ARRAY_PATTERN3 = Pattern.compile("(.*?)\\[(.*?)\\](.*)");
	private static final Pattern ARRAY_PATTERN4 = Pattern.compile("^[Aa][Rr][Rr][Aa][Yy](\\W.*?\\W|\\s*?)[Oo][Ff](\\s.*)");
	private static final Pattern ARRAY_PATTERN5 = Pattern.compile("(.*?)\\[.*?\\]$");
	//private static final Pattern RANGE_PATTERN = Pattern.compile("^([0-9]+)[.][.][.]?([0-9]+)$");
	private static final Pattern RANGE_PATTERN = Pattern.compile("^([0-9]+)\\s*?[.][.][.]?\\s*?([0-9]+)$");
	
	/**
	 * Internal declaration list node of TypeMapEntry. Don't manipulate this directly
	 * @author Kay Gürtzig
	 */
	class VarDeclaration {

		public String typeDescriptor;
		public Element definingElement;
		public int lineNo;
		public Vector<int[]> indexRanges = null;
		public String elementType = null;
		// START KGU#388 2017-07-04: Enh. #423 New structure for record types  
		public LinkedHashMap<String, TypeMapEntry> components = null;
		// END KGU#388 2017-07-04
		public boolean isCStyle = false;
		
		public VarDeclaration(String _descriptor, Element _element, int _lineNo, boolean _cStyle)
		{
			typeDescriptor = _descriptor.trim();
			definingElement = _element;
			lineNo = _lineNo;
			boolean isArray = (typeDescriptor.matches(".+\\[.*\\].*") || typeDescriptor.matches("(^|\\W.*)" + BString.breakup("array") + "($|\\W.*)"));
			if (isArray) {
				this.setElementType();
				this.setIndexRanges();
			}
			isCStyle = _cStyle;
		}
		
		/**
		 * Creates a new Type definition for a record type
		 * @param _descriptor - textual description of the type
		 * @param _element - originating element (should be an Instruction with a type definition)
		 * @param _lineNo - line no within the element
		 * @param _components - the ordered map of declared components
		 */
		public VarDeclaration(String _descriptor, Element _element, int _lineNo,
				LinkedHashMap<String, TypeMapEntry> _components)
		{
			typeDescriptor = _descriptor.trim();
			definingElement = _element;
			lineNo = _lineNo;
			components = _components; 
		}
		
		/**
		 * Indicates whether this declaration bears some evidence of an array structure
		 * @return true if this refers to an indexed type.
		 */
		public boolean isArray()
		{
			return this.indexRanges != null;
		}
		
		// START KGU#388 201707-04: Enh. #423
		/**
		 * Indicates whether this declaration represents a record structure
		 * @return true if this refers to a structured type.
		 */
		public boolean isRecord()
		{
			return this.components != null;
		}
		// END KGU#388 2017-07-04
		
		/**
		 * Returns a type string with canonicalized structure information, i.e.
		 * the original element type name, prefixed with as many '&#64;' characters
		 * as there are index levels if it is an array type.
		 * @see TypeMapEntry#getTypes()
		 * @return type string, possibly prefixed with one or more '&#64;' characters. 
		 */
		public String getCanonicalType()
		{
			return getCanonicalType(false);
		}
		
		// START KGU#380 2017-04-14: Issue #394: Improved type comparison opportunities
		/**
		 * Returns a type string with canonicalized structure information and - if
		 * {@code canonicalizeTypeNames} is true - type identifiers (as far as
		 * possible, i.e. type names like "integer", "real" etc. apparently designating
		 * standard types will be replaced by corresponding Java type names), all
		 * prefixed with as many '&#64;' characters as there are index levels if it
		 * is an array type.
		 * @param canonicalizeTypeNames - specifies whether type names are to be unified, too
		 * @see TypeMapEntry#getTypes()
		 * @return type string, possibly prefixed with one or more '&#64;' characters. 
		 */
		public String getCanonicalType(boolean canonicalizeTypeNames)
		{
			String type = this.typeDescriptor;
			if (this.isArray()) {
				type = "@" + this.elementType;
				for (int i = 1; i < this.indexRanges.size(); i++) {
					type = "@" + type;
				}
			}
			// START KGU#388 2017-07-04: Enh. #423
			else if (this.isRecord()) {
				StringList compDescr = new StringList();
				for (Entry<String,TypeMapEntry> entry: this.components.entrySet()) {
					compDescr.add(entry.getKey() + ":" + entry.getValue().getCanonicalType(canonicalizeTypeNames, true));
				}
				type = "${" + compDescr.concatenate(";") + "}";
			}
			// END KGU#388 2017-07-04
			if (canonicalizeTypeNames) {
				type = canonicalizeType(type);
			}
			return type;
		}
		// END KGU#380 2017-04-14
		
		private void setElementType()
		{
			// Possible cases we have to cope with might e.g. be:
			// array of unsigned int[12]
			// array[1..6] of ARRAY 9 OF BOOLEAN
			// double[5][8]
			// array [2..9, columns] of array of char
			String typeDescr = this.typeDescriptor;
			Matcher matcher;
			while ((matcher = ARRAY_PATTERN4.matcher(typeDescr)).matches()) {
				typeDescr = matcher.replaceAll( "$2").trim();
			}
			while ((matcher = ARRAY_PATTERN5.matcher(typeDescr)).matches()) {
				typeDescr = matcher.replaceAll( "$1").trim();
			}
			if (typeDescr.isEmpty()) {
				typeDescr = "???";
			}
			this.elementType = typeDescr;
		}

		private void setIndexRanges()
		{
			//final String arrayPattern1 = "^" + BString.breakup("array") + "\\s*?" +  BString.breakup("of") + "(\\s.*)";
			//final String arrayPattern1o = "^" + BString.breakup("array") + "\\s*?(\\d*)\\s*?" +  BString.breakup("of") + "(\\s.*)";	// Oberon style
			//final String arrayPattern2 = "^" + BString.breakup("array") + "\\s*?\\[(.*)\\]\\s*?" +  BString.breakup("of") + "(\\s.*)";
			//final String arrayPattern3 = "(.*?)\\[(.*?)\\](.*)";
			String typeDescr = this.typeDescriptor;
			this.indexRanges = new Vector<int[]>();
			Matcher matcher1, matcher1o = null, matcher2 = null;
			boolean matches1 = false, matches1o = false;
			while ((matches1 = (matcher1 = ARRAY_PATTERN1.matcher(typeDescr)).matches())
					|| (matches1o = (matcher1o = ARRAY_PATTERN1o.matcher(typeDescr)).matches())
					|| (matcher2 = ARRAY_PATTERN2.matcher(typeDescr)).matches()) {
				if (matches1) {
					typeDescr = matcher1.replaceFirst("$1").trim();
					this.indexRanges.add(new int[]{0, -1});
				}
				else if (matches1o) {
					String dimensions = matcher1o.replaceFirst("$1").trim();
					typeDescr = matcher1o.replaceFirst("$2").trim();
					StringList counts = StringList.explode(dimensions, ",");
					for (int i = 0; i < counts.count(); i++) {
						try {
							int count = Integer.parseInt(counts.get(i));
							this.indexRanges.add(new int[]{0, count-1});
						}
						catch (NumberFormatException ex) {
							this.indexRanges.add(new int[]{0, -1});							
						}
					}
				}
				else {
					String dimens = matcher2.replaceFirst("$1").trim();
					typeDescr = matcher2.replaceFirst("$2").trim();
					StringList ranges = StringList.explode(dimens, "\\]\\s*\\[");
					addIndexRanges(StringList.explode(ranges, ","));
				}
				matches1 = matches1o = false;
			};
			Matcher matcher3 = ARRAY_PATTERN3.matcher(typeDescr);
			while (typeDescr.endsWith("]") && matcher3.matches()) {
				String dimens = matcher3.replaceFirst("$2").trim();
				typeDescr = matcher3.replaceFirst("$1$3").trim();
				addIndexRanges(StringList.explode(dimens, ","));
				matcher3.reset(typeDescr);
			}
			
		}
		
		private void addIndexRanges(StringList ranges)
		{
			Matcher matcher = RANGE_PATTERN.matcher("");

			for (int i = 0; i < ranges.count(); i++) {
				int[] indexRange = new int[]{0, -1};
				String range = ranges.get(i).trim();
				matcher.reset(range);
				if (matcher.matches()) {
					indexRange[0] = Integer.parseInt(matcher.replaceAll("$1"));
					indexRange[1] = Integer.parseInt(matcher.replaceAll("$2"));
				}
				else {
					try {
						indexRange[1] = Integer.parseInt(range) - 1;
					}
					catch (NumberFormatException ex) {}
				}
				this.indexRanges.add(indexRange);
			}
			
		}
			
	}
	// START KGU#388 2017-07-04: Enh. #423 New structure for record types  
	public String typeName = null;			// to distinguish and identify named types
	// END KGU#388 2017-07-04
	public LinkedList<VarDeclaration> declarations = new LinkedList<VarDeclaration>();
	// Set of accessor ids
	public Set<Element> modifiers = new HashSet<Element>();
	public Set<Element> references = new HashSet<Element>();
	// START KGU#388 2017-09-14: Enh. #423 we now distinguish between declared and guessed types
	/** Distinguishes between declared type (true) and guessed or derived type (false) */
	public boolean isDeclared = false;
	// END KGU#388 2017-09-14
	
	private TypeMapEntry()
	{
	}
	
	/**
	 * Analyses the given declaration information and creates a corresponding
	 * entry (with a single declaration record).
	 * @see #addDeclaration(String _descriptor, Element _element, int _lineNo, boolean _initialized, boolean _cStyle) 
	 * @param _descriptor - the found type-describing or -specifying string
	 * @param _typeName - the type name if this is a type definition, null otherwise (enh. #423, 2017-07-12)
	 * @param _element - the originating Structorizer element
	 * @param _lineNo - the line number within the element text
	 * @param _initialized - whether the variable is initialized or assigned here
	 * @param _explicit - whether this is an explicit variable declaration (or just derived from value)
	 * @param _cStyle - whether it's a C-style declaration or initialization
	 */
	public TypeMapEntry(String _descriptor, String _typeName, Element _element, int _lineNo, boolean _initialized, boolean _explicit, boolean _cStyle)
	{
		// START KGU#388 2017-07-12: Enh. #423
		this.typeName = _typeName;
		// END KGU#388 2017-07-12
		declarations.add(new VarDeclaration(_descriptor, _element, _lineNo, _cStyle));
		if (_initialized) {
			modifiers.add(_element);
		}
		isDeclared = _explicit;
	}
	
	// START KGU#388 2017-07-12: Enh. #423
	/**
	 * Creates a record type entry as explicitly declared (with a single declaration record).
	 * @see #addDeclaration(String _descriptor, Element _element, int _lineNo, boolean _initialized, boolean _cStyle) 
	 * @param _descriptor - the found type-describing or -specifying string
	 * @param _typeName - the type name if this is a type definition (mandatory!)
	 * @param _components - the component type map
	 * @param _element - the originating Structorizer element
	 * @param _lineNo - the line number within the element text
	 */
	public TypeMapEntry(String _descriptor, String _typeName, LinkedHashMap<String, TypeMapEntry> _components,
			Element _element, int _lineNo)
	{
		this.typeName = _typeName;
		declarations.add(new VarDeclaration(_descriptor, _element, _lineNo, _components));
		this.isDeclared = true;
	}
	
	public static TypeMapEntry getDummy()
	{
		if (dummy == null) {
			dummy = new TypeMapEntry();
		}
		return dummy;
	}
	// END KGU#388 2017-07-12
	
	/**
	 * Returns a type string with canonicalized structure information and - if
	 * {@code canonicalizeTypeNames} is true - canonicalized type identifiers (as far as
	 * possible, i.e. type names like "integer", "real" etc. apparently designating
	 * standard types will be replaced by corresponding Java type names), all
	 * prefixed with as many '&#64;' characters as there are index levels if it
	 * is an array type or embedded in a "${...}" if it is a record/struct type.
	 * If the type information is too ambiguous then an empty string is returned.
	 * @param _canonicalizeTypeNames - if contained element types are to be canonicalized, too.
	 * @param _asName - set this true if in case of a named type the name is to be returned (otherwise
	 * the structural description would be returned)
	 * @return name or structural description
	 */
	public String getCanonicalType(boolean _canonicalizeTypeNames, boolean _asName) {
		String type = "";
		if (_asName && this.isNamed()) {
			type = this.typeName;
		}
		else {
			StringList types = this.getTypes(_canonicalizeTypeNames);
			if (types.count() == 1) {
				type = types.get(0);
			}
			else if (_canonicalizeTypeNames && this.isArray(true)) {
				type = "@???";
			}
		}
		return type;
	}

	/**
	 * Tries to map the given type name to a unified type name for easier comparison.
	 * The unified type names are similar to elementary C or Java type names but
	 * strictly lower-case
	 * @param type - a type-designating string
	 * @return a replacement type name or the original type name (if no match was found)
	 */
	public static String canonicalizeType(String type) {
		// (copied from JavaGenerator.transformType()
		type = type.replaceAll("(^|.*\\W)(" + BString.breakup("short int") + ")($|\\W.*)", "$1short$3");
		type = type.replaceAll("(^|.*\\W)(" + BString.breakup("long int") + ")($|\\W.*)", "$1long$3");
		type = type.replaceAll("(^|.*\\W)(" + BString.breakup("long long") + ")($|\\W.*)", "$1long$3");
		type = type.replaceAll("(^|.*\\W)(S" + BString.breakup("hort") + ")($|\\W.*)", "$1short$3");
		type = type.replaceAll("(^|.*\\W)(" + BString.breakup("unsigned int") + ")($|\\W.*)", "$1int$3");
		type = type.replaceAll("(^|.*\\W)(" + BString.breakup("unsigned long") + ")($|\\W.*)", "$1long$3");
		// START KGU 2018-07-12
		type = type.replaceAll("(^|.*\\W)(" + BString.breakup("unsigned short") + ")($|\\W.*)", "$1short$3");
		// END KGU 2018-07-12
		type = type.replaceAll("(^|.*\\W)(" + BString.breakup("unsigned char") + ")($|\\W.*)", "$1byte$3");
		type = type.replaceAll("(^|.*\\W)(" + BString.breakup("signed char") + ")($|\\W.*)", "$1byte$3");
		type = type.replaceAll("(^|.*\\W)(" + BString.breakup("unsigned") + ")($|\\W.*)", "$1int$3");
		type = type.replaceAll("(^|.*\\W)(I" + BString.breakup("nt") + ")($|\\W.*)", "$1int$3");
		type = type.replaceAll("(^|.*\\W)(" + BString.breakup("integer") + ")($|\\W.*)", "$1int$3");
		type = type.replaceAll("(^|.*\\W)(L" + BString.breakup("ong") + ")($|\\W.*)", "$1long$3");
		type = type.replaceAll("(^|.*\\W)(" + BString.breakup("longint") + ")($|\\W.*)", "$1long$3");
		type = type.replaceAll("(^|.*\\W)(D" + BString.breakup("ouble") + ")($|\\W.*)", "$1double$3");
		type = type.replaceAll("(^|.*\\W)(" + BString.breakup("longreal") + ")($|\\W.*)", "$1double$3");
		type = type.replaceAll("(^|.*\\W)(" + BString.breakup("real") + ")($|\\W.*)", "$1double$3");
		type = type.replaceAll("(^|.*\\W)(F" + BString.breakup("loat") + ")($|\\W.*)", "$1float$3");
		type = type.replaceAll("(^|.*\\W)(C" + BString.breakup("har") + ")($|\\W.*)", "$1char$3");
		type = type.replaceAll("(^|.*\\W)(" + BString.breakup("character") + ")($|\\W.*)", "$1char$3");
		type = type.replaceAll("(^|.*\\W)(B" + BString.breakup("oolean") + ")($|\\W.*)", "$1boolean$3");
		if (type.matches("(^|.*\\W)(" + BString.breakup("bool") + "[eE]?)(\\W.*|$)")) {
			type = type.replaceAll("(^|.*\\W)(" + BString.breakup("bool") + "[eE]?)(\\W.*|$)", "$1boolean$3");
		}
		type = type.replaceAll("(^|.*\\W)(" + BString.breakup("string") + ")($|\\W.*)", "$1string$3");
		return type;
	}
	
	/**
	 * Checks if the canonicalized type description {@code typeName} is among the
	 * canonical names of standard types.
	 * @param typeName - a typ description (should be a name)
	 * @return true if the type is detected.
	 */
	public static boolean isStandardType(String typeName)
	{
		String canonicalType = canonicalizeType(typeName);
		if (canonicalType.equals("string") || canonicalType.equals("char") || canonicalType.equals("boolean")) {
			return true;
		}
		for (String canon: canonicalNumericTypes) {
			if (canonicalType.equals(canon)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Analyses the given declaration information and adds a corresponding
	 * declaration info to this entry if it differs.
	 * It will even be added if this entry is marked as (explicitly) declared
	 * such that analysis may find out potential conflicts
	 * @param _descriptor - the found type-describing or -specifying string
	 * @param _element - the originating Structorizer element
	 * @param _lineNo - the line number within the element text
	 * @param _initialized - whether the variable is initialized or assigned here
	 * @param _cStyle - whether it's a C-style declaration or initialization
	 * @return indicates whether the new declaration substantially differs from previous ones
	 */
	public boolean addDeclaration(String _descriptor, Element _element, int _lineNo, boolean _initialized, boolean _cStyle)
	{
		boolean differs = false;
		boolean isNew = true;
		_descriptor = _descriptor.trim();
		for (VarDeclaration currDecl: declarations) {
			boolean equals = currDecl.typeDescriptor.equalsIgnoreCase(_descriptor);
			if (!equals) {
				differs = true;
			}
			if (currDecl.definingElement == _element && equals) {
				isNew = false;
			}
		}
		if (isNew) {
			declarations.addLast(new VarDeclaration(_descriptor, _element, _lineNo, _cStyle));
		}
		if (_initialized) {
			modifiers.add(_element);
		}
		return differs;
	}
	
	/**
	 * Checks whether there is a unique type specification or if the determined
	 * type strings are equal (case-indifferently).
	 * @return true if the gathered type information is consistent, else otherwise 
	 */
	public boolean isConflictFree()
	{
		String typeDescr = null;
		for (VarDeclaration currDecl: declarations) {
			if (typeDescr != null && !currDecl.typeDescriptor.equalsIgnoreCase(typeDescr)) {
				return false;
			}
		}		
		return true;
	}
	
	/**
	 * Returns a StringList containing the type specifications of all detected
	 * declarations or assignments in canonicalized form (a prefix "&#64;" stands
	 * for one array dimension level, a prefix "$" symbolizes a record/struct).
	 * Type names are preserved as declared.
	 * @return StringList of differing canonicalized type descriptions
	 */
	public StringList getTypes()
	{
		return getTypes(false);
	}
	
	// START KGU#380 2017-04-14: Issue #394: Improved type comparison opportunities
	/**
	 * Returns a StringList containing the type specifications of all detected
	 * declarations or assignments in canonicalized form (a prefix "&#64;" stands
	 * for one array dimension level, a prefix "$" symbolizes a record/struct).<br/>
	 * If {@code canonicalizeTypeNames} is true then type identifiers apparently
	 * designating standard types (like "integer", "real" etc.) will be replaced
	 * by corresponding Java type names. 
	 * @param canonicalizeTypeNames - specifies whether type names are to be unified, too
	 * @return StringList of differing canonicalized type descriptions
	 */
	public StringList getTypes(boolean canonicalizeTypeNames)
	{
		StringList types = new StringList();
		for (VarDeclaration currDecl: declarations) {
			types.addIfNew(currDecl.getCanonicalType(canonicalizeTypeNames));
		}
		return types;
	}
	// END KGU#380 2017-04-14
	
// Was wrong concept
//	// START KGU#388 2017-09-13: Enh. #423
//	/**
//	 * Returns the original definition type entry for the referred record type (may be
//	 * this entry itself). 
//	 * @param typeMap - the current type map for nme retrieval
//	 * @return the defining record type entry if available, otherwise null.
//	 */
//	public TypeMapEntry getRecordType(HashMap<String, TypeMapEntry> typeMap)
//	{
//		TypeMapEntry recordType = null;
//		if (this.isRecord()) {
//			// Is this entry the type definition? Then return this directly
//			if (this.isNamed()) {
//				return this;
//			}
//			// Otherwise look for the first reference with record structure
//			for (VarDeclaration varDecl: this.declarations) {
//				if (varDecl.isRecord()) {
//					recordType = typeMap.get(varDecl.typeDescriptor);
//					if (recordType != null) {
//						return recordType.getRecordType(typeMap);
//					}
//				} //if (varDecl.isRecord())
//			} // for (VarDeclaration varDecl: this.declarations)
//		} // if (this.isRecord())
//		return recordType;
//	}
	
	@SuppressWarnings("unchecked")
	/**
	 * If this is a defined record type, returns the component-type map
	 * @param _merge - whether concurring definitions are to be merged.
	 * @return an ordered table mapping component names to defining TypeMapEntries
	 */
	public LinkedHashMap<String, TypeMapEntry> getComponentInfo(boolean _merge)
	{
		LinkedHashMap<String, TypeMapEntry> componentInfo = null;
		if (this.isRecord()) {
			for (VarDeclaration varDecl: this.declarations) {
				if (varDecl.isRecord()) {
					if (componentInfo == null) {
						componentInfo = varDecl.components;
						if (!_merge) {
							break;
						}
						componentInfo = (LinkedHashMap<String, TypeMapEntry>)componentInfo.clone();
					}
					else {
						for (Entry<String, TypeMapEntry> entry: varDecl.components.entrySet()) {
							componentInfo.putIfAbsent(entry.getKey(), entry.getValue());
						}
					} // if (componentInfo == null)
				} //if (varDecl.isRecord())
			} // for (VarDeclaration varDecl: this.declarations)
		} // if (this.isRecord())
		return componentInfo;
	}
	// END KGU#388 2017-09-13

	/**
	 * Checks if there is a C-style declaration for this variable in _element
	 * or in the first declaring element (if _element = null)
	 * @param _element - the interesting element or null
	 * @return true if the first matching declaration uses C syntax 
	 */
	public boolean isCStyleDeclaredAt(Element _element)
	{
		for (VarDeclaration currDecl: declarations) {
			if (_element == null || currDecl.definingElement == _element) {
				return currDecl.isCStyle;
			}			
		}
		return false;
	}
	
	/**
	 * Indicates whether at least the first declaration states that the type
	 * is an array (this may be in conflict with other declarations, though)
	 * @see #isArray(boolean)
	 * @see #isRecord()
	 * @see #isRecord(boolean)
	 * @return true if there is some evidence of an array structure
	 */
	public boolean isArray()
	{
		return !this.declarations.isEmpty() && this.declarations.element().isArray();
	}
	
	/**
	 * Indicates whether there is some evidence for an array structure. If
	 * _allDeclarations is true then the result will only be true if all of
	 * the found declarations support the array property, otherwise it would be
	 * enough that one of the declarations shows array structure. 
	 * @see #isArray()
	 * @see #isRecord()
	 * @see #isRecord(boolean)
	 * @_allDeclarations - whether all declarations must support the assumption 
	 * @return true if there is enough evidence of an array structure
	 */
	public boolean isArray(boolean _allDeclarations)
	{
		for (VarDeclaration decl: this.declarations) {
			if (_allDeclarations && !decl.isArray()) {
				return false;
			}
			else if (!_allDeclarations && decl.isArray()) {
				return true;
			}
		}
		return !_allDeclarations;
	}
	
	// START KGU#388 2017-07-11: Enh. #423
	/**
	 * Indicates whether at least the first declaration states that the type
	 * is a record (this may be in conflict with other declarations, though)
	 * @see #isRecord(boolean)
	 * @return true if there is some evidence of an array structure
	 */
	public boolean isRecord()
	{
		return !this.declarations.isEmpty() && this.declarations.element().isRecord();
	}
	
	/**
	 * Indicates whether there is some evidence for a record structure. If
	 * _allDeclarations is true then the result will only be true if all of
	 * the found declarations support the record property, otherwise it would be
	 * enough that one of the declarations shows array structure. 
	 * @see #isArray()
	 * @see #isRecord()
	 * @see #isArray(boolean)
	 * @_allDeclarations - whether all declarations must support the assumption 
	 * @return true if there is enough evidence of an array structure
	 */
	public boolean isRecord(boolean _allDeclarations)
	{
		for (VarDeclaration decl: this.declarations) {
			if (_allDeclarations && !decl.isRecord()) {
				return false;
			}
			else if (!_allDeclarations && decl.isRecord()) {
				return true;
			}
		}
		return !_allDeclarations;
	}
	
	/**
	 * Indicates whether this type entry refers to a named type definition
	 * @return true if this has a type name.
	 */
	public boolean isNamed()
	{
		return this.typeName != null;
	}
	// END KGU#388 2017-07011

	/**
	 * Returns the maximum index of array dimension 'level' found over all
	 * declarations or -1 if there is no substantial range information.
	 * @param level - dimension index (0 for outermost)
	 * @return maximum index found among array declarations of the given level
	 */
	public int getMaxIndex(int level)
	{
		int ix = -1;
		for (VarDeclaration decl: this.declarations) {
			if (level >= 0 && decl.isArray() && level < decl.indexRanges.size() && decl.indexRanges.get(level)[1] > ix) {
				ix = decl.indexRanges.get(level)[1];
			}
		}
		return ix;
	}
	
	/**
	 * Returns the minimum index of array dimension 'level' found over all
	 * declarations or -1 if there is no substantial range information.
	 * @param level - dimension index (0 for outermost)
	 * @return maximum index found among array declarations of the given level
	 */
	public int getMinIndex(int level)
	{
		int ix = -1;
		for (VarDeclaration decl: this.declarations) {
			if (level >= 0 && decl.isArray() && level < decl.indexRanges.size() && decl.indexRanges.get(level)[0] > ix) {
				ix = decl.indexRanges.get(level)[0];
			}
		}
		return ix;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
    public String toString()
    {
		String name = typeName == null ? "" : typeName + "=";
		return getClass().getSimpleName() + "(" + name + this.getTypes().concatenate(" | ") + ")";
    }

	/**
	 * Tries to find a common compatible canonical type for {@code type1} and {@code type2}
	 * by aligning to the respective larger type, e.g. short -&gt; int -&gt; long -&gt; float
	 * or char -&gt; String.   
	 * @param type1 - a type-designating string
	 * @param type2 - a type-designating string
	 * @param canonicalized - set this true if both type names have already been canonicalized
	 * (or if they must not be canonicalized). 
	 * @return either common type name or "???" if both are incompatible
	 */
	public static String combineTypes(String type1, String type2, boolean canonicalized) {
		if (!canonicalized) {
			type1 = canonicalizeType(type1);
			type2 = canonicalizeType(type2);
		}
		if (!type1.equals(type2)) {
			int typeIx1 = -1, typeIx2 = -1;
			for (int i = 0; i < canonicalNumericTypes.length && (typeIx1 < 0 || typeIx2 < 0); i++) {
				if (typeIx1 < 0 && type1.equals(canonicalNumericTypes[i])) {
					typeIx1 = i;
				}
				if (typeIx2 < 0 && type2.equals(canonicalNumericTypes[i])) {
					typeIx2 = i;
				}
			}
			if (typeIx1 >= 0 && typeIx2 >= 0) {
				type1 = canonicalNumericTypes[Math.max(typeIx1, typeIx2)];
			}
			else if (type1.equals("char") && type2.equals("string")
					|| type1.equals("string") && type2.equals("char")) {
				// We try an automatic conversion
				type1 = "string";
			}
			else {
				type1 = "???";
			}
		}
		return type1;
	}

	// START KGU#388 2017-09-19: Enh. #423 for code generator support
	/**
	 * Checks whether there is a declaration of this variable or type within the
	 * given diagram {@code _root}.
	 * @param _root - the suspected source diagram or null
	 * @return true if there is a declaration within {@code _root} or if {@code _root}
	 * is null and this is declared at all. 
	 */
	public boolean isDeclaredWithin(Root _root)
	{
		if (_root == null) {
			return this.isDeclared;
		}
		for (VarDeclaration decl: declarations) {
			if (decl.definingElement != null && Element.getRoot(decl.definingElement) == _root) {
				return true;
			}
		}
		return false;
	}
	// END KGU#388 2017-09-19
	
	// START KGU#506 2018-03-14: Issue #522
	/** @return the {@link Element} where this variable or type is declared (if any). */
	public Element getDeclaringElement()
	{
		for (VarDeclaration decl: declarations) {
			if (decl.definingElement != null) {
				return decl.definingElement;
			}
		}
		return null;
	}
	// END KGU#506 2018-03-14
	
}
