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
 *
 ******************************************************************************************************
 *
 *      Comment: Entry contains a single-linked list of possibly conflicting specifications as well as
 *      sets of element numbers with assigning and referencing use.
 *      
 *
 ******************************************************************************************************///

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

/**
 * @author kay
 *
 */
public class TypeMapEntry {
	
	/**
	 * Internal declaration list node of TypeMapEntry. Don't manipulate this directly
	 * @author Kay Gürtzig
	 */
	class VarDeclaration {
		private static final String arrayPattern1 = "^[Aa][Rr][Rr][Aa][Yy]\\s*?[Oo][Ff](\\s.*)";
		private static final String arrayPattern1o = "^[Aa][Rr][Rr][Aa][Yy]\\s*?(\\d*)\\s*?[Oo][Ff](\\s.*)";	// Oberon style
		private static final String arrayPattern2 = "^[Aa][Rr][Rr][Aa][Yy]\\s*?\\[(.*)\\]\\s*?[Oo][Ff](\\s.*)";
		private static final String arrayPattern3 = "(.*?)\\[(.*?)\\](.*)";
		private static final String arrayPattern4 = "^[Aa][Rr][Rr][Aa][Yy](\\W.*?\\W|\\s*?)[Oo][Ff](\\s.*)";
		private static final String arrayPattern5 = "(.*?)\\[.*?\\]$";

		public String typeDescriptor;
		public Element definingElement;
		public int lineNo;
		public Vector<int[]> indexRanges = null;
		public String elementType = null;
		public boolean isCStyle = false;
		
		public VarDeclaration(String _descriptor, Element _element, int _lineNo, boolean _cStyle)
		{
			typeDescriptor = _descriptor;
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
		 * Indicates whether this declaration bear some evidence of an array structure
		 * @return true if this refes to an indexed type.
		 */
		public boolean isArray()
		{
			return this.indexRanges != null;
		}
		
		/**
		 * Returns a type string with canonicalized structure information, i.e.
		 * the original element type name, prefixed with as many '&#64;' characters
		 * as there are index levels if it is an array type.
		 * @see TypeMapEntry#getTypes()
		 * @return type string, possibly prefixed with one or more '&#64;' characters. 
		 */
		public String getCanonicalType()
		{
			String type = this.typeDescriptor;
			if (this.isArray()) {
				type = "@" + this.elementType;
				for (int i = 1; i < this.indexRanges.size(); i++) {
					type = "@" + type;
				}
			}
			return type;
		}

		private void setElementType()
		{
			// Possible cases we have to cope with might e.g. be:
			// array of unsigned int[12]
			// array[1...6] of ARRAY 9 OF BOOLEAN
			// double[5][8]
			// array [2...9, columns]of array of char
			String typeDescr = this.typeDescriptor;
			while (typeDescr.matches(arrayPattern4)) {
				typeDescr = typeDescr.replaceAll(arrayPattern4, "$2").trim();
			}
			while (typeDescr.matches(arrayPattern5)) {
				typeDescr = typeDescr.replaceAll(arrayPattern5, "$1").trim();
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
			while (typeDescr.matches(arrayPattern1) || typeDescr.matches(arrayPattern1o) || typeDescr.matches(arrayPattern2)) {
				if (typeDescr.matches(arrayPattern1)) {
					typeDescr = typeDescr.replaceFirst(arrayPattern1, "$1").trim();
					this.indexRanges.add(new int[]{0, -1});
				}
				else if (typeDescr.matches(arrayPattern1o)) {
					String dimensions = typeDescr.replaceFirst(arrayPattern1o, "$1").trim();
					typeDescr = typeDescr.replaceFirst(arrayPattern1o, "$2").trim();
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
					String dimens = typeDescr.replaceFirst(arrayPattern2, "$1").trim();
					typeDescr = typeDescr.replaceFirst(arrayPattern2, "$2").trim();
					StringList ranges = StringList.explode(dimens, "\\]\\[");
					addIndexRanges(StringList.explode(ranges, ","));
				}
			};
			while (typeDescr.endsWith("]") && typeDescr.matches(arrayPattern3)) {
				String dimens = typeDescr.replaceFirst(arrayPattern3, "$2").trim();
				typeDescr = typeDescr.replaceFirst(arrayPattern3, "$1$3").trim();
				addIndexRanges(StringList.explode(dimens, ","));
			}
			
		}
		
		private void addIndexRanges(StringList ranges)
		{
			final String rangePattern = "^([0-9]+)[.][.][.]?([0-9]+)$";

			for (int i = 0; i < ranges.count(); i++) {
				int[] indexRange = new int[]{0, -1};
				String range = ranges.get(i).trim();
				if (range.matches(rangePattern)) {
					indexRange[0] = Integer.parseInt(range.replaceAll(rangePattern, "$1"));
					indexRange[1] = Integer.parseInt(range.replaceAll(rangePattern, "$2"));
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
	public LinkedList<VarDeclaration> declarations = new LinkedList<VarDeclaration>();
	// Set of accessor ids
	public Set<Element> modifiers = new HashSet<Element>();
	public Set<Element> references = new HashSet<Element>();
	
	/**
	 * Analyses the given declaration information and creates a corrsponding
	 * entry (with a single declaration record).
	 * @see #addDeclaration(String _descriptor, Element _element, int _lineNo, boolean _cStyle, boolean _initialized) 
	 * @param _descriptor - the found type-describing or -specifying string
	 * @param _element - the originating Structorizer element
	 * @param _lineNo - the line number within the element text
	 * @param _cStyle - whether it's a C-style declaration or initialization
	 * @param _initialized - whether the variable is initialized or assigned here
	 */
	public TypeMapEntry(String _descriptor, Element _element, int _lineNo, boolean _cStyle, boolean _initialized)
	{
		declarations.add(new VarDeclaration(_descriptor, _element, _lineNo, _cStyle));
		if (_initialized) {
			modifiers.add(_element);
		}
	}
	
	/**
	 * Analyses the given declaration information and adds a corresponding
	 * declaration info to this entry.
	 * @param _descriptor - the found type-describing or -specifying string
	 * @param _element - the originating Structorizer element
	 * @param _lineNo - the line number within the element text
	 * @param _cStyle - whether it's a C-style declaration or initialization
	 * @param _initialized - whether the variable is initialized or assigned here
	 * @return indicates whether the new declaration substantially differs from previous ones
	 */
	public boolean addDeclaration(String _descriptor, Element _element, int _lineNo, boolean _cStyle, boolean _initialized)
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
	 * for one array dimension level). 
	 * @return StringList of differing canonicalized type descriptions
	 */
	public StringList getTypes()
	{
		StringList types = new StringList();
		for (VarDeclaration currDecl: declarations) {
			types.addIfNew(currDecl.getCanonicalType());
		}
		return types;
	}
	
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
		return getClass().getSimpleName() + "(" + this.getTypes().concatenate(" | ") + ")";
    }
	
}
