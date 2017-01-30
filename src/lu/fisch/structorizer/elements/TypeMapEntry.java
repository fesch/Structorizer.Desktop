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

import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

/**
 * @author kay
 *
 */
public class TypeMapEntry {
	
	// Declaration list node
	class VarDeclaration {
		public String typeDescriptor;
		public Element definingElement;
		public int lineNo;
		public int[] indexRange = null;
		public String elementType = null;
		public boolean isArray = false;
		public boolean isCStyle = false;
		
		public VarDeclaration(String _descriptor, Element _element, int _lineNo, boolean _cStyle)
		{
			typeDescriptor = _descriptor;
			definingElement = _element;
			lineNo = _lineNo;
			isArray = (typeDescriptor.matches(".+\\[.*\\].*") || typeDescriptor.matches("(^|\\W.*)" + BString.breakup("array") + "($|\\W.*)"));
			if (isArray) {
				this.setElementType();
				this.setIndexRange();
			}
			isCStyle = _cStyle;
		}		

		private void setElementType()
		{
			if (this.isArray) {
				if (this.typeDescriptor.endsWith("]")) {
					this.elementType = this.typeDescriptor.substring(0, this.typeDescriptor.indexOf('['));
				}
				else if (this.typeDescriptor.toLowerCase().matches("array.*? of .*")) {
					int posCut = this.typeDescriptor.toLowerCase().indexOf(" of ") + 4;
					this.elementType = this.typeDescriptor.substring(posCut);
				}
				else {
					this.elementType = "???";
				}
			}
		}

		private void setIndexRange()
		{
			if (this.isArray) {
				this.indexRange = new int[]{0, 0};
				if (this.typeDescriptor.matches(".*\\[[0-9]*\\].*")) {
					String countStr = this.typeDescriptor.replaceAll(".*\\[([0-9]*)\\].*", "$1");
					try {
						this.indexRange[1] = Integer.parseInt(countStr)-1;
					}
					catch (NumberFormatException ex) {}
				}
				else if (this.typeDescriptor.matches(".*\\[[0-9]*[.][.]+[0-9]*\\].*")) {
					String countStr = this.typeDescriptor.replaceAll(".*\\[([0-9]*)[.][.]+([0-9]*)\\].*", "$1,$2");
					try {
						String[] limits = countStr.split(",");
						this.indexRange[0] = Integer.parseInt(limits[0]);
						this.indexRange[1] = Integer.parseInt(limits[1]);
					}
					catch (NumberFormatException ex) {}
				}
			}
		}
	}
	public LinkedList<VarDeclaration> declarations = new LinkedList<VarDeclaration>();
	// Set of accessor ids
	public Set<Element> modifiers = new HashSet<Element>();
	public Set<Element> references = new HashSet<Element>();
	
	public TypeMapEntry(String _descriptor, Element _element, int _lineNo, boolean _cStyle, boolean _initialized)
	{
		declarations.add(new VarDeclaration(_descriptor, _element, _lineNo, _cStyle));
		if (_initialized) {
			modifiers.add(_element);
		}
	}
	
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
	
	public StringList getTypes()
	{
		StringList types = new StringList();
		for (VarDeclaration currDecl: declarations) {
			String type = currDecl.typeDescriptor;
			if (currDecl.isArray) {
				type = "@" + currDecl.elementType;
			}
			types.addIfNew(type);
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
	
	public boolean isArray()
	{
		return !this.declarations.isEmpty() && this.declarations.element().isArray;
	}
	
	public int getMaxIndex()
	{
		int ix = -1;
		for (VarDeclaration decl: this.declarations) {
			if (decl.isArray && decl.indexRange[1] > ix) {
				ix = decl.indexRange[1];
			}
		}
		return ix;
	}
	
	public int getMinIndex()
	{
		int ix = -1;
		for (VarDeclaration decl: this.declarations) {
			if (decl.isArray && decl.indexRange[0] > ix) {
				ix = decl.indexRange[0];
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
		String selfDescr = getClass().getSimpleName() + "(";
		String separator = "";
		for (VarDeclaration currDecl: declarations) {
			selfDescr += separator + currDecl.definingElement + ": " + currDecl.typeDescriptor + (currDecl.isArray ? " (array)" : "");
			separator = " | ";
		}
		return selfDescr + ")";
    }
	
}
