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
package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Generator for PapDesigner diagram files.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2020-02-27      First Issue for Enhancement request #440
 *      Kay Gürtzig     2020-03-07      PapItem and PapElement classes integrated
 *      Kay Gürtzig     2020-04-02      PapParallel fundamentally rewritten, provisional Jump mechanism implemented
 *      Kay Gürtzig     2020-04-25      Bugfix #863/2: Assignment symbols hadn't been transformed in CALLs
 *      Kay Gürtzig     2020-04-28      Issue #864: Parameter lists of calls and routine declarations had to be transformed
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2020-02-27 (Kay Gürtzig)
 *      - This gnenerator is to convert structograms into flowcharts of type "Programmablaufplan" in
 *        the XML format of PAPDesigner
 *      - It needs a completely different strategy: In a first pass it is to create some PAP objects
 *        and a raster extension tuple (height, width, and horizontal excentricity in raster units).
 *        This information can only be retrieved recursively bottom-up (similar to drawing a diagram,
 *        some pre-generate phase). Each PAP object will then be subject to a horizontal and vertical
 *        coordinate translocation, the translation values will always be positive.
 *
 ******************************************************************************************************///

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.elements.Loop;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Parallel;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.Try;
import lu.fisch.structorizer.elements.While;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.parsers.CodeParser;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

/**
 * Generator for PAP graphs in PAPDesigner format
 * @author Kay Gürtzig
 */
public class PapGenerator extends Generator {

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getDialogTitle()
	 */
	@Override
	protected String getDialogTitle() {
		return "Export PAPDesigner ...";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getFileDescription()
	 */
	@Override
	protected String getFileDescription() {
		return "PAPDesigner files (.pap)";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getFileExtensions()
	 */
	@Override
	protected String[] getFileExtensions() {
		return new String[] {"pap"};
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIndent()
	 */
	@Override
	protected String getIndent() {
		return "  ";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#commentSymbolLeft()
	 */
	@Override
	protected String commentSymbolLeft() {
		return "<!--";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#commentSymbolRight()
	 */
	@Override
	protected String commentSymbolRight() {
		return "-->";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getInputReplacer(boolean)
	 */
	@Override
	protected String getInputReplacer(boolean withPrompt) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getOutputReplacer()
	 */
	@Override
	protected String getOutputReplacer() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#breakMatchesCase()
	 */
	@Override
	protected boolean breakMatchesCase() {
		return false;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIncludePattern()
	 */
	@Override
	protected String getIncludePattern() {
		return null;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getOverloadingLevel()
	 */
	@Override
	protected OverloadingLevel getOverloadingLevel() {
		// Just leave all as is...
		return OverloadingLevel.OL_DEFAULT_ARGUMENTS;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getTryCatchLevel()
	 */
	@Override
	protected TryCatchSupportLevel getTryCatchLevel() {
		return TryCatchSupportLevel.TC_NO_TRY;
	}

	/************ Code Generation **************/
		
	/** Counter for the exported diagrams (serves as ID in the pap file) */
	private int currentNo = 0;
	
	/** Specifies whether the flowcharts are to created according to the DIN 66001 version from
	 * 1982 (which introduced specific loop delimiters and bans parallelograms for I/O), otherwise
	 * the DIN version from 1962 will be used, which composes loops with condition elements and
	 * "jumps".
	 */
	protected boolean din66001_1982 = true;
	
	/** PapDesigner-conform date format */
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		tokens.replaceAll("<-", ":=");
		return tokens.concatenate();
	}
	
	// ============= Internal Classes for PAP element representation ===============

	/**
	 * Abstract graph-theoretical base element of a PapDesigner diagram for
	 * PAP export, common inheritance parent for {@link PapFigure} (vertex) and
	 * {@link PapConnection} (link).
	 * @author Kay Gürtzig
	 */
	private static abstract class PapItem {
		
		private static long nextId = 0;
		
		private long id = -1;
		public double format = 1.0;
		public String text = "";
		
		/**
		 * Creates some unspecific PAP item labeled with the given {@code _text}.
		 * @param _text - label for the item
		 */
		public PapItem(String _text)
		{
			id = nextId++;
			text = _text;
		}

		/**
		 * Creates some unspecific PAP item labeled with the given {@code _text} and the
		 * given format value.
		 * @param _text - label for the item
		 * @param _format - some scale value (not actually used)
		 */
		public PapItem(String _text, double _format)
		{
			id = nextId++;
			text = _text;
			format = _format;
		}
		
		/**
		 * @return the diagram-unique item id (as used in the pap file)
		 */
		public long getId()
		{
			return id;
		}
		
		/**
		 * Reset the id counter (to prepare the creation of a new PAP).
		 */
		public static void resetNextId()
		{
			nextId = 0;
		}
		
		/**
		 * Is to append the XML code for this item to the {@link StringList} {@code _code}.
		 * @param _code - the file content to be produced.
		 * @param _indent - the current indentation string
		 * @param _indentUnit - the indentation per level to be used
		 */
		public abstract void generateCode(StringList _code, String _indent, String _indentUnit);
	}

	/**
	 * Vertex class in PAP graphs, used to compose the graphs on PAP export
	 * @author Kay Gürtzig
	 */
	private static class PapFigure extends PapItem {
		
		public enum Type {PapTitle,					// Root
			PapStart, PapEnd, PapConnector,		// Connectors
			PapActivity, PapInput, PapOutput,	// Instruction
			PapModule,							// Call
			PapCondition,						// Alternative
			PapLoopStart, PapLoopEnd,			// Compound Loop
			PapComment};
			
		Type type;
		long idAssociate = -1;
		boolean anchor = false;
		int row, col;

		/**
		 * Creates a new PapFigure element, i.e. a node in the flowchart. Exports itself Via
		 * {@link #generateCode(StringList, String, String)} into a .pap file text.
		 * rows and columns, can be translated via # 
		 * @param _row - the vertical coordinate
		 * @param _column - the horizontal coordinate
		 * @param _figureType - the {@link Type} of the figure
		 * @param _text - the text content for the node (may contain newlines)
		 * @param _assocWith - possibly another PapFigure as association partner (to be used among
		 * {@link Type.PapLoopStart} and {@link Type.PapLoopEnd} figures. Automatically establishes
		 * a symmetric relation if not {@code null}.
		 */
		public PapFigure(int _row, int _column, Type _figureType, String _text, PapFigure _assocWith)
		{
			super(_text);
			row = _row;
			col = _column;
			type = _figureType;
			anchor = (_figureType == Type.PapTitle);
			if (_assocWith != null) {
				_assocWith.idAssociate = this.getId();
				this.idAssociate = _assocWith.getId();
			}
		}

		@Override
		public void generateCode(StringList _code, String _indent, String _indentUnit)
		{
			String indent1 = _indent + _indentUnit;
			String indent2 = indent1 + _indentUnit;
			_code.add(_indent + "<ENTRY COLUMN=\"" + this.col + "\" ROW=\"" + this.row + "\""
					+ (this.anchor ? " ANCHOR=\"True\"" : "") + ">");
			_code.add(indent1 + "<FIGURE SUBTYPE=\"" + this.type.toString() + "\" FORMAT=\"" + String.format(Locale.US, "%.2f", this.format) + "\" ID=\""
					+ this.getId() + (this.idAssociate >= 0 ? "\" ASSOCIATE=\"" + this.idAssociate : "") + "\">");
			_code.add(indent2 + "<TEXT><![CDATA[" + this.text + "]]></TEXT>");
			_code.add(indent1 + "</FIGURE>");
			_code.add(_indent + "</ENTRY>");
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return this.getClass().getSimpleName() + "(" + this.getId() + ", " + this.type + ", [" + row + "," + col + "])";
		}

	}

	/**
	 * Link class in PAP graphs, used to compose the graph on PAP export
	 * @author Kay Gürtzig
	 */
	private static class PapConnection extends PapItem {

		// References to source and target figures (vertices)
		private long from, to;
		
		/**
		 * Create an unlabeled link between the nodes with ids {@code _fromId} and {@code _toId}.
		 * @param _fromId - id of the source node ({@link PapFigure}
		 * @param _toId - id of the target node ({@link PapFigure}
		 */
		public PapConnection(long _fromId, long _toId) {
			this(_fromId, _toId, "");
		}

		/**
		 * Create a labeled link between the nodes with ids {@code _fromId} and {@code _toId}.
		 * @param _fromId - id of the source node ({@link PapFigure}
		 * @param _toId - id of the target node ({@link PapFigure}
		 * @param _text - the label of the link
		 */
		public PapConnection(long _fromId, long _toId, String _text) {
			this(_fromId, _toId, _text, 1.00);
		}

		/**
		 * Create a labeled link between the nodes with ids {@code _fromId} and {@code _toId}.
		 * @param _fromId - id of the source node ({@link PapFigure}
		 * @param _toId - id of the target node ({@link PapFigure}
		 * @param _text - the label of the link
		 * @param _format - format (scale) parameter (not atually used)
		 */
		public PapConnection(long _fromId, long _toId, String _text, double _format) {
			super(_text, _format);
			from = _fromId;
			to = _toId;
		}
		
		/* (non-Javadoc)
		 * @see lu.fisch.structorizer.generators.PapItem#generateCode(lu.fisch.utils.StringList, java.lang.String, java.lang.String)
		 */
		@Override
		public void generateCode(StringList _code, String _indent, String _indentUnit)
		{
			_code.add(_indent + "<CONNECTION FORMAT=\"" + String.format(Locale.US, "%.2f", this.format) + "\" ID=\"" + this.getId()
			+ "\" FROM=\"" + this.from + "\" TO=\"" + this.to + "\" TEXT=\"" + this.text + "\" />");
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return this.getClass().getSimpleName() + "(" + from + " --> " + to + ")";
		}
	}
	
	// ============= Internal Helper Classes for Structorizer elements ===============
	
	/**
	 * Base element conversion helper class, handles Instruction offsprings directly and
	 * serves as inheritance root for more specific Element subclasses.
	 * @author Kay Gürtzig
	 */
	private class PapElement {
		
		/** Horizontal and vertical extensions (width = left + right + 1) */
		protected int left = 0, right = 0, height = 1;
		/** Top row and axis column */
		protected int row0 = 0, column0 = 0;
		
		protected Element element;
		
		public PapElement(Element el)
		{
			element = el;
			if (!element.getComment().getLongString().trim().isEmpty()) {
				right++;
			}
			// START KGU#863 2020-04-28: Issue #864 In case of a muti-line we must decompose it
			if (element instanceof Call) {
				StringList lines = element.getUnbrokenText();
				if (lines.count() > 1) {
					for (int i = 0; i < lines.count(); i++) {
						if (!lines.get(i).trim().isEmpty()) {
							height++;
						}
					}
					// Subtract the standard height since we counted all (non-empty) lines
					if (height > 1) {
						height--;
					}
				}
			}
			// END KGU#863 2020-04-28
			// START KGU#396 2020-04-02: Issue #440 Makeshift approach for Jump elements
			else if (element instanceof Jump) {
				if (!(((Jump) element).isReturn() && this.isLastElementOfRoot())) {
					height++;
				}
			}
			// END KGU396 2020-04-02
			else if (element.getClass().getSimpleName().equals("Instruction")) {
				// Input and output lines are to be separated and hence increase height
				StringList lines = element.getUnbrokenText();
				boolean isIO = false;
				for (int i = 0; i < lines.count(); i++) {
					String line = lines.get(i);
					if (Instruction.isInput(line) || Instruction.isOutput(line)) {
						if (i > 0) height++;
						isIO = true;
					}
					else {
						if (isIO) height++;
						isIO = false;
					}
				}
			}
		}
		
		/** @return the number of occupied raster columns */
		public int getWidth() {
			return left + right + 1;
		}
		/** @return the number of occupied raster rows */
		public int getHeight()
		{
			return this.height;
		}
		
		/** Sets the top "central" reference raster coordinates (translates the element) 
		 * @return the next row coordinate below this element
		 */
		public int setAxisTop(int row, int column)
		{
			this.row0 = row;
			this.column0 = column;
			return row + height;
		}
		
		/**
		 * Adds {@link PapFigure}s and {@link PapConnection}s as required to
		 * {@code figures} and {@code connections}, providing them with
		 * incremental ids starting from {@code startId}.
		 * @param figures - a list of {@link PapFigure} objects to be filled
		 * @param connections - a list of {@link PapConnection} objects to be filled
		 * @param startId - the next id to be used within 
		 * @return the next unused consecutive id free for 
		 * */
		public long[] generateItems(List<PapFigure> figures, List<PapConnection> connections)
		{
			int row = row0;
			long firstId = -1;
			long prevId = -1;
			PapFigure.Type papType = PapFigure.Type.PapActivity;
			StringList text = element.getUnbrokenText();
			String comment = element.getComment().getText().trim();
			if (!comment.isEmpty()) {
				figures.add(new PapFigure(row, column0+1, PapFigure.Type.PapComment, comment, null));
			}
			if (element instanceof Call) {
				papType = PapFigure.Type.PapModule;
				Root owningRoot = Element.getRoot(element);
				// START KGU#862 2020-04-25: Bugfix #863/2 - the assignment symbol must be transformed
				for (int i = 0; i < text.count(); i++) {
					String line = text.get(i).trim();
					if (line.isEmpty()) {
						continue;
					}
					// START KGU#863 2020-04-28: issues #385, #864/2 Support for declared optional arguments
					Function call = ((Call)element).getCalledRoutine(i);
					if (call != null && (routinePool != null) && line.endsWith(")")) {
						java.util.Vector<Root> callCandidates = routinePool.findRoutinesBySignature(call.getName(), call.paramCount(), owningRoot, false);
						if (!callCandidates.isEmpty()) {
							// FIXME We'll just fetch the very first one for now...
							Root called = callCandidates.get(0);
							StringList defaults = new StringList();
							called.collectParameters(null, null, defaults);
							if (defaults.count() > call.paramCount()) {
								// We just insert the list of default values for the missing arguments
								line = line.substring(0, line.length()-1) + (call.paramCount() > 0 ? ", " : "") + 
										defaults.subSequence(call.paramCount(), defaults.count()).concatenate(", ") + ")";
							}
						}
					}
					PapFigure lastFigure = new PapFigure(row++, column0, papType, transform(line), null); 
					figures.add(lastFigure);
					if (prevId >= 0) {
						connections.add(new PapConnection(prevId, lastFigure.getId()));
					}
					prevId = lastFigure.getId();
					if (firstId < 0) {
						firstId = prevId;
					}
					// END KGU#863 2020-04-28
				}
				// All done in general, avert the creation of another figure ...
				text.clear();
				// ... unless the call has not produced any figure (?)
				if (prevId < 0) {
					// Obviously there was no non-empty line, have a dummy node created
					text.add("");
				}
				// END KGU#862 2020-04-25
			}
			else if (element instanceof Jump) {
				boolean isRegularReturn = false;
				// FIXME: EXIT node found - Prepare a routing from this figure to the target figure!
				// START KGU#496 2020-04-02: Enh. #440 Provisional approach
				// As a first shot, we just build a dead end, i.e. a blind connector without exit link (ironically)
				String label = CodeParser.getKeywordOrDefault("preLeave", "break");
				if (!text.isEmpty()) {
					// Not a default leave, check whether the line contains arguments
					if (((Jump) element).isReturn()) {
						label = CodeParser.getKeywordOrDefault("preReturn", "return");
						if (isLastElementOfRoot()) {
							isRegularReturn = true;
						}
					}
					else if (((Jump) element).isExit()) {
						label = CodeParser.getKeywordOrDefault("preExit", "exit");
					}
					else if (((Jump) element).isThrow()) {
						label = CodeParser.getKeywordOrDefault("preThrow", "throw");
					}
				}
				PapFigure lastFigure = new PapFigure(row++, column0, PapFigure.Type.PapActivity, text.getText(), null);
				firstId = lastFigure.getId();
				figures.add(lastFigure);
				if (isRegularReturn) {
					prevId = firstId;
				}
				else {
					lastFigure = new PapFigure(row, column0, PapFigure.Type.PapConnector, "", null);
					figures.add(lastFigure);
					connections.add(new PapConnection(firstId, lastFigure.getId(), label));
				}
				return new long[]{firstId, prevId};
				// END KGU#496 2020-04-02
			}
			else if (element instanceof Instruction) {
				StringList lines = element.getUnbrokenText();
				text.clear();	// No collected lines so far
				PapFigure lastFigure = null;
				for (int i = 0; i < lines.count(); i++) {
					String line = lines.get(i);
					boolean isInput = Instruction.isInput(line);
					if (isInput || Instruction.isOutput(line)) {
						if (!text.isEmpty()) {
							// First accomplish the begun instruction line sequence
							figures.add(lastFigure = new PapFigure(row++, column0, PapFigure.Type.PapActivity, text.getText(), null));
							if (prevId >= 0) {
								connections.add(new PapConnection(prevId, lastFigure.getId()));
							}
							prevId = lastFigure.getId();
							if (firstId < 0) {
								firstId = prevId;
							}
						}
						if (!din66001_1982) {
							papType = isInput ? PapFigure.Type.PapInput : PapFigure.Type.PapOutput;
						}
						figures.add(lastFigure = new PapFigure(row++, column0, papType, line, null));
						if (prevId >= 0) {
							connections.add(new PapConnection(prevId, lastFigure.getId()));
						}
						prevId = lastFigure.getId();
						if (firstId < 0) {
							firstId = prevId;
						}
						text.clear();
					}
					else {
						// Collect the line such that we may produce a multi-line element
						papType = PapFigure.Type.PapActivity;
						text.add(transform(line, false));
					}
				}
			}
			if (!text.isEmpty()) {
				PapFigure lastFigure = new PapFigure(row, column0, papType, text.getText(), null); 
				figures.add(lastFigure);
				if (row > row0) {
					connections.add(new PapConnection(prevId, lastFigure.getId()));
				}
				prevId = lastFigure.getId();
				if (firstId < 0) {
					firstId = prevId;
				}
			}
			return new long[]{firstId, prevId};
		}

		/**
		 * @return true if the incorporated {@link #element} is the last element of
		 * the main sequence of the owing {@link Root}.
		 */
		protected boolean isLastElementOfRoot()
		{
			Element parent = element.parent;
			return (parent != null && parent instanceof Subqueue && parent.parent instanceof Root
					&& ((Subqueue)parent).getElement(((Subqueue)parent).getSize()-1) == element);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return this.getClass().getSimpleName() + "(" + element + ")";
		}
	}
	
	/**
	 * Represents an NS diagram for PAP export 
	 * @author Kay Gürtzig
	 */
	private class PapRoot extends PapElement {

		PapSequence sequ = null;
		
		/**
		 * @param root
		 */
		public PapRoot(Root root) {
			super(root); // This reserves the row for the title
			// Generate the items for the body
			sequ = new PapSequence(root.children);
			// Reserve the rows for start and end nodes
			height += 2 + sequ.getHeight();
			left = sequ.left;
			right = Math.max(right, sequ.right);
		}

		@Override
		public int setAxisTop(int row, int column)
		{
			super.setAxisTop(row, column);
			this.sequ.setAxisTop(row + 2, column);
			return row + this.sequ.getHeight() + 3;
		}
		
		@Override
		public long[] generateItems(List<PapFigure> figures, List<PapConnection> connections)
		{
			int row = this.row0;
			PapItem.resetNextId();
			// Generate the title figure
			// START KGU#863 2020-04-28: Issue #864/1: PapDesigner does not recognise semicolons
			//figures.add(new PapFigure(row, column0, PapFigure.Type.PapTitle, element.getText().getLongString(), null));
			String header = transformHeader((Root)element);
			figures.add(new PapFigure(row, column0, PapFigure.Type.PapTitle, header, null));
			// END KGU#863 2020-04-28
			// Generate the comment if necessary
			String comment = element.getComment().getText().trim();
			if (!comment.isEmpty()) {
				figures.add(new PapFigure(row, column0+1, PapFigure.Type.PapComment, comment, null));
			}
			row++;
			// Generate the start node
			PapFigure lastFigure = new PapFigure(row++, column0, PapFigure.Type.PapStart, "Start", null);
			figures.add(lastFigure);
			long prevId = lastFigure.getId();
			long[] idPair = this.sequ.generateItems(figures, connections);
			if (idPair[0] >= 0) {
				connections.add(new PapConnection(prevId, idPair[0]));
			}
			else {
				idPair[1] = prevId;
			}
			row += this.sequ.getHeight();
			// Generate the start node
			lastFigure = new PapFigure(row++, column0, PapFigure.Type.PapEnd, "End", null);
			figures.add(lastFigure);
			if (idPair[1] >= 0) {
				connections.add(new PapConnection(idPair[1], lastFigure.getId()));
			}
			
			return new long[]{prevId, lastFigure.getId()};
		}
		
	}

	/**
	 * Represents a linear sequence of PapElements
	 * @author Kay Gürtzig
	 */
	private class PapSequence extends PapElement {
		protected List<PapElement> elements = new LinkedList<PapElement>();
		
		public PapSequence(Subqueue sq)
		{
			super(sq);
			height = 0;
			for (int i = 0; i < sq.getSize(); i++) {
				Element el = sq.getElement(i);
				// FIXME: differentiate by Element type
				if (el instanceof Loop) {
					addElement(new PapLoop((Loop)el));
				}
				else if (el instanceof Alternative) {
					addElement(new PapAlternative((Alternative)el));
				}
				else if (el instanceof Case) {
					addElement(new PapCase((Case)el));
				}
				else if (el instanceof Parallel) {
					addElement(new PapParallel((Parallel)el));
				}
				else if (el instanceof Try) {
					addElement(new PapTry((Try)el));
				}
				else {
					addElement(new PapElement(el));
				}
			}
		}
		
		@Override
		public int setAxisTop(int row, int column)
		{
			super.setAxisTop(row, column);
			for (PapElement elem: elements) {
				elem.setAxisTop(row, column);
				row += elem.getHeight();
			}
			return row;
		}
		
		@Override
		public long[] generateItems(List<PapFigure> figures, List<PapConnection> connections)
		{
			long firstId = -1, lastId = -1;
			for (PapElement elem: elements) {
				long[] elemIds =  elem.generateItems(figures, connections);
				if (firstId < 0) {
					firstId = elemIds[0];
				}
				if (elemIds[0] >= 0 && lastId >= 0) {
					connections.add(new PapConnection(lastId, elemIds[0]));
				}
				lastId = elemIds[1];
			}
			return new long[] {firstId, lastId};
		}

		/**
		 * Inserts the given PapElement {@code el} a the row {@code pos}
		 * @param el - the {@link PapElement} to be inserted (or appended)
		 * @param pos - the relative row index before which {@code el} is to be inserted
		 * (pos < 0m and pos > n/o elements are tolerated and aligned to 0 or
		 * n/o elements, respectively)
		 */
		public void addElement(PapElement el)
		{
			elements.add(el);
			height += el.getHeight();
			if (left < el.left) {
				left = el.left;
			}
			if (right < el.right) {
				right = el.right;
			}
		}

	}

	/**
	 * Represents an {@link Alternative for PAP generation}
	 * @author Kay Gürtzig
	 */
	private class PapAlternative extends PapElement {

		private PapSequence sequTrue = null;
		private PapSequence sequFalse = null;
		/** Default displacements of the axes of left and right branch */
		private int colTrue = -1, colFalse = +1;
		
		/**
		 * Creates a PapAlternative object from the Structorizer {@link Alternative} {@code alt}
		 * @param alt
		 */
		public PapAlternative(Alternative alt) {
			super(alt);
			height += 1;
			int heightT = 0, leftT = 0, rightT = 0;
			int heightF = 0, leftF = 0, rightF = 0;
			if (alt.qTrue.getSize() > 0) {
				this.sequTrue = new PapSequence(alt.qTrue);
				heightT = this.sequTrue.getHeight();
				leftT = this.sequTrue.left;
				rightT = this.sequTrue.right;
				if (alt.qFalse.getSize() > 0) {
					colTrue -= rightT;
				}
				else {
					colTrue = 0;
					colFalse = rightT + 1;
				}
			}
			if (alt.qFalse.getSize() > 0) {
				this.sequFalse = new PapSequence(alt.qFalse);
				heightF = this.sequFalse.getHeight();
				leftF = this.sequFalse.left;
				rightF = this.sequFalse.right;
				if (alt.qTrue.getSize() > 0) {
					colFalse += leftF;
				}
				else {
					colFalse = 0;
					colTrue = - (leftF + 1);
				}
			}
			left = Math.max(-colTrue + leftT, -colFalse + leftF);
			right = Math.max(colTrue + rightT, colFalse + rightF);
			if (!element.getComment().getText().trim().isEmpty() && rightF < 1) {
				right++; // We need an extra column for the condition comment
			}
			height += Math.max(heightT, heightF);
		}
		
		@Override
		public int setAxisTop(int row, int column)
		{
			super.setAxisTop(row, column);
			if (this.sequTrue != null) {
				// colTrue is negative or 0
				this.sequTrue.setAxisTop(row + 1, column + colTrue);
			}
			if (this.sequFalse != null) {
				// colFalse is positive or 0
				this.sequFalse.setAxisTop(row + 1, column + colFalse);
			}
			return row + height;
		}

		@Override
		public long[] generateItems(List<PapFigure> figures, List<PapConnection> connections)
		{
			int row = row0;
			PapFigure lastFigure = new PapFigure(row, column0, PapFigure.Type.PapCondition, element.getBrokenText().getText(), null);
			figures.add(lastFigure);
			long firstId = lastFigure.getId();
			long prevId = firstId;
			// Handle the IF comment
			String comment = element.getComment().getText().trim();
			if (!comment.isEmpty()) {
				figures.add(new PapFigure(row, column0 + colFalse + 1, PapFigure.Type.PapComment, comment, null));
			}
			// Prepare branch generation
			long idsTrue[] = null, idsFalse[] = null;
			int rowT = row + 1, rowF = row + 1;
			// Handle the TRUE side
			if (colTrue < 0) {
				// Set a bend above the TRUE branch
				figures.add(lastFigure = new PapFigure(row, column0 + colTrue, PapFigure.Type.PapConnector, "", null));
				prevId = lastFigure.getId();
				// Connect the condition with the left top bend
				connections.add(new PapConnection(firstId, prevId, "true"));
			}
			if (this.sequTrue != null) {
				// Generate the TRUE (left) branch sequence
				idsTrue = this.sequTrue.generateItems(figures, connections);
				if (idsTrue[0] >= 0) {
					// Connect the left top bend or the connection with the first branch node
					connections.add(new PapConnection(prevId, idsTrue[0], colTrue == 0 ? "true" : ""));
				}
				else {
					// branch was empty, so prepare the direct connection from left top to bottom bend
					idsTrue[1] = prevId;
				}
				rowT += this.sequTrue.getHeight();
			}
			else {
				// There was no TRUE branch, so prepare a direct connection from left top to bottom bend
				idsTrue = new long[] {-1, prevId};
			}
			// Handle the FALSE side
			prevId = firstId;
			if (colFalse > 0) {
				// Set a bend above the FALSE branch
				figures.add(lastFigure = new PapFigure(row, column0 + colFalse, PapFigure.Type.PapConnector, "", null));
				prevId = lastFigure.getId();
				// Connect the condition with the right top bend
				connections.add(new PapConnection(firstId, prevId, "false"));
			}
			if (this.sequFalse != null) {
				// Generate the FALSE (right) branch sequence
				idsFalse = this.sequFalse.generateItems(figures, connections);
				if (idsFalse[0] >= 0) {
					// Connect the right top bend or the connection with the first branch node
					connections.add(new PapConnection(prevId, idsFalse[0], colFalse == 0 ? "false" : ""));
				}
				else {
					// branch was empty, so prepare the direct connection from right top to bottom bend
					idsFalse[1] = prevId;
				}
				rowF += this.sequFalse.getHeight();
			}
			else {
				// There was no FALSE branch, so prepare a direct connection from right top to bottom bend
				idsFalse = new long[] {-1, prevId};
			}
			row = Math.max(rowT, rowF);
			// This will be the joining node
			PapFigure joinFigure = new PapFigure(row, column0, PapFigure.Type.PapConnector, "", null);
			figures.add(joinFigure);
			long lastId = joinFigure.getId();
			if (colTrue < 0 && idsTrue[1] >= 0) {
				// Set a bend below the TRUE branch if the branch did not exit
				figures.add(lastFigure = new PapFigure(row, column0 + colTrue, PapFigure.Type.PapConnector, "", null));
				// Connect the lowest TRUE branch node with the lower left bend
				connections.add(new PapConnection(idsTrue[1], lastFigure.getId()));
				idsTrue[1] = lastFigure.getId();	// Cache the lower left bend id
			}
			if (colFalse > 0 && idsFalse[1] >= 0) {
				// Set a bend below the FALSE branch if the branch did not exit
				figures.add(lastFigure = new PapFigure(row, column0 + colFalse, PapFigure.Type.PapConnector, "", null));
				// Connect the lowest FALSE branch node with the lower right bend
				connections.add(new PapConnection(idsFalse[1], lastFigure.getId()));
				idsFalse[1] = lastFigure.getId();	// Cache the lower right bend id
			}
			if (idsTrue[1] >= 0) {
				connections.add(new PapConnection(idsTrue[1], lastId, colTrue == 0 && sequTrue == null ? "true" : ""));
			}
			if (idsFalse[1] >= 0) {
				connections.add(new PapConnection(idsFalse[1], lastId, colFalse == 0 && sequFalse == null ? "false" : ""));
			}
			return new long[]{firstId, lastId};
		}
		
	}

	/**
	 * Represents a {@link Case} structure for PAP export as cascade of alternatives
	 * @author Kay Gürtzig
	 */
	private class PapCase extends PapElement {

		private ArrayList<PapSequence> branches = new ArrayList<PapSequence>();
		private int[] colBranches = null;
		private int rowSelection = 0;
		private String discrVar = null;
		
		/**
		 * @param _case
		 */
		public PapCase(Case _case) {
			super(_case);
			// The comment will be placed above the selection
			boolean hasComment = right > 0;
			// The head will be replaced by a horizontal series of conditions in one row
			StringList text = _case.getUnbrokenText();
			int nBranches = text.count() - 1;
			// FIXME: We will always have to construct a default branch here!
			boolean hasDefault = !text.get(nBranches).equals("%");
			colBranches = new int[nBranches];
			Root root = Element.getRoot(_case);
			if (!root.getVarNames().contains(text.get(0))) {
				discrVar = "discr" + Integer.toHexString(_case.hashCode());
			}
			if (hasComment || discrVar != null) {
				rowSelection++;
				height++;
			}
			int maxBranchHeight = 0;
			for (int i = 0; i < nBranches; i++) {
				Subqueue sq = _case.qs.get(i);
				PapSequence sequ = null;
				if (i < nBranches - 1 || hasDefault) {
					sequ = new PapSequence(sq);
					maxBranchHeight = Math.max(maxBranchHeight, sequ.getHeight());
				}
				branches.add(sequ);
				if (i == 0) {
					colBranches[i] = column0;
					left = sequ.left;
					right = sequ.right;
				}
				else if (sequ != null) {
					colBranches[i] = right + sequ.left + 1;
					right += sequ.getWidth();
				}
				else {
					colBranches[i] = ++right;
				}
			}
			height += 1 + maxBranchHeight;
		}

		@Override
		public int setAxisTop(int row, int column)
		{
			int bottom = super.setAxisTop(row, column);
			for (int i = 0; i < branches.size(); i++) {
				PapSequence branch = branches.get(i);
				if (branch != null) {
					branch.setAxisTop(row + rowSelection + 1, column+ colBranches[i]);
				}
			}
			return bottom;
		}

		@Override
		public long[] generateItems(List<PapFigure> figures, List<PapConnection> connections)
		{
			int row = row0;
			long firstId = -1, lastId = -1;
			StringList text = element.getUnbrokenText();
			String comment = element.getComment().getText().trim();
			PapFigure firstFigure = null, lastFigure = null;
			String discr = text.get(0);
			
			if (!comment.isEmpty()) {
				figures.add(new PapFigure(row, column0+1, PapFigure.Type.PapComment, comment, null));
			}
			
			if (discrVar != null) {
				// Create an initializer element assigning the generic discriminator variable
				firstFigure = new PapFigure(row, column0, PapFigure.Type.PapActivity, discrVar + " := " + discr, null);
				figures.add(firstFigure);
				firstId = firstFigure.getId();
				discr = discrVar;
			}

			// Prepare the confluence connector at end
			figures.add(lastFigure = new PapFigure(row0 + height - 1, column0, PapFigure.Type.PapConnector, "", null));
			lastId = lastFigure.getId();
			
			row = row0 + rowSelection;
			
			long prevIdTop = -1;
			for (int i = 0; i < branches.size(); i++) {
				String selector = text.get(i + 1);
				boolean descendTrue = true;
				if (i+2 < text.count()) {
					String operator = " = ";
					if (Element.splitExpressionList(selector, ",", true).count() > 2) {
						selector = "{" + selector + "}";
						operator = " in ";
					}
					firstFigure = new PapFigure(row, column0 + colBranches[i], PapFigure.Type.PapCondition, discr + operator + selector, null);
				}
				else {
					firstFigure = new PapFigure(row, column0 + colBranches[i], PapFigure.Type.PapConnector, "", null);
					descendTrue = false;
				}
				figures.add(firstFigure);
				if (i == 0) {
					if (firstId < 0) {
						// First condition, no initializer
						firstId = firstFigure.getId();
					}
					else {
						// Connect the initializer with the first condition
						connections.add(new PapConnection(firstId, firstFigure.getId()));
					}
				}
				else {
					connections.add(new PapConnection(prevIdTop, firstFigure.getId(), "false"));
				}
				prevIdTop = firstFigure.getId();
				
				long branchIds[];
				if (branches.get(i) != null) {
					branchIds = branches.get(i).generateItems(figures, connections);
				}
				else {
					branchIds = new long[] {-1, -1};
				}
				
				if (branchIds[0] >= 0) {
					connections.add(new PapConnection(firstFigure.getId(), branchIds[0], descendTrue ? "true" : ""));
				}
				else {
					// subqueue was empty, so bridge it
					branchIds[1] = firstFigure.getId();
				}
				if (i > 0) {
					if (branchIds[1] >= 0) {
						// place a join connector beneath the branch
						firstFigure = new PapFigure(row0+height-1 , column0 + colBranches[i], PapFigure.Type.PapConnector, "", null);
						figures.add(firstFigure);
						// The branch did not exit at end, so connect its end with the join connector
						connections.add(new PapConnection(branchIds[1], firstFigure.getId()));
						// Connect the join connector with its neighbour to the left
						connections.add(new PapConnection(firstFigure.getId(), lastFigure.getId()));
						lastFigure = firstFigure;
					}
				}
				else if (branchIds[1] >= 0) {
					// Connect the first branch with the confluence connector if it did not jump off
					connections.add(new PapConnection(branchIds[1], lastId));
				}
			}
			
			return new long[] {firstId, lastId};
		}

	}

	/**
	 * Represents some {@link While}, {@link Repeat}, {@link For}, or {@link Forever} element
	 * for PAP export.
	 * @author Kay Gürtzig
	 *
	 */
	private class PapLoop extends PapElement {

		/** Column offset for the left loop bypass in the discrete form (DIN 66001 from 1966) */
		private int colBypassLeft = 0;
		/** Column offset for the right loop bypass in the discrete form (DIN 66001 from 1966) */
		private int colBypassRight = 0;
		/** Row offset for the loop body */
		private int rowBody = 1;
		
		private PapSequence body = null;
		
		/**
		 * Creates a new PapLoop object from the given {@link Loop} implementor class instance
		 * {@code loop}.
		 * @param loop
		 */
		public PapLoop(Loop loop) {
			super(loop);
			body = new PapSequence(loop.getBody());
			// FIXME we must handle exits (leave commands) here
			int extraRows = 2;
			if (din66001_1982 || loop instanceof For && ((For)loop).isForInLoop()) {
				left = Math.max(left, body.left);
				right = Math.max(right, body.right);
			}
			else {
				colBypassLeft = -Math.max(left, body.left + 1);
				colBypassRight = Math.max(1, body.right + (loop instanceof Forever ? 0 : 1));
				// START KGU 2020-04-01 A comment induces right > 0, not right > 1
				//if (right > 1) {
				if (right > 0) {
				// END KGU 2020-04-01
					// A comment has to be placed right of the condition
					right = colBypassRight + 1; 
				}
				else {
					right = colBypassRight;
				}
				if (loop instanceof While) {
					extraRows = 4;
					rowBody = 2;
				}
				else if (loop instanceof Repeat) {
					// extraRow is okay but we don't need a left bypass
					colBypassLeft++;
				}
				else if (loop instanceof For) {
					/* FIXME: This applies only for counting loops,
					 * we have no idea how to transform a traversing loop (FOR IN)
					 */
					extraRows = 6;
					rowBody = 3;
				}
				else if (loop instanceof Forever) {
					// TODO We will re-introduce an (isolated) end connector for later leave mechanism
					//extraRows++;
				}
				left = -colBypassLeft;
			}
			height = body.getHeight() + extraRows;
			
		}
		
		@Override
		public int setAxisTop(int row, int column)
		{
			super.setAxisTop(row, column);
			body.setAxisTop(row + rowBody, column);
			
			return row + height;
		}

		@Override
		public long[] generateItems(List<PapFigure> figures, List<PapConnection> connections)
		{
			// FIXME we must handle exits (leave commands / breaks) here
			long firstId = -1;
			long lastId = -1;
			int row = row0;
			PapFigure firstFigure = null, lastFigure = null;
			String text = transform(element.getBrokenText().getText().trim());
			String comment = element.getComment().getText().trim();
			if (din66001_1982 || element instanceof For && ((For)element).isForInLoop()) {
				// Generate the opening loop item
				firstFigure = new PapFigure(row, column0, PapFigure.Type.PapLoopStart,
						(!(element instanceof Repeat) ? text : ""), null);
				figures.add(firstFigure);
				firstId = firstFigure.getId();
				
				// Place the comment at top if it's not a REPEAT loop 
				if (!comment.isEmpty() && !(element instanceof Repeat)) {
					figures.add(new PapFigure(row, column0 + 1, PapFigure.Type.PapComment, comment, null));
				}
				
				row++;
				// Generate the body items
				long[] idsBody = body.generateItems(figures, connections);

				row += body.getHeight();
				// Generate the closing loop item
				lastFigure = new PapFigure(row, column0, PapFigure.Type.PapLoopEnd,
						(element instanceof Repeat ? text : ""), firstFigure);
				figures.add(lastFigure);
				lastId = lastFigure.getId();
				
				// Place the comment at bottom if it IS a REPEAT loop 
				if (!comment.isEmpty() && (element instanceof Repeat)) {
					figures.add(new PapFigure(row, column0 + 1, 
							PapFigure.Type.PapComment, comment, null));
				}
				
				// Connect the loop control items with the loop body or bridge it if empty.
				if (idsBody[0] >= 0) {
					connections.add(new PapConnection(firstId, idsBody[0]));
					// Connect the body's bottom with the loop end node if the body did not exit
					if (idsBody[1] >= 0) {
						connections.add(new PapConnection(idsBody[1], lastId));
					}
				}
				else {
					connections.add(new PapConnection(firstId, lastId));
				}
				// TODO Modify this when we implement a leave connection
				if (element instanceof Forever) {
					lastId = -1;	// Avert the connection with following elements
				}
			}
			// DIN 660001 1966 - decomposed loops
			else if (element instanceof Repeat) {
				// At top there is a confluence connector
				firstFigure = new PapFigure(row++, column0, PapFigure.Type.PapConnector, "", null);
				figures.add(firstFigure);
				firstId = firstFigure.getId();
				
				// Generate the body items
				long[] idsBody = body.generateItems(figures, connections);
				if (idsBody[0] >= 0) {
					connections.add(new PapConnection(firstId, idsBody[0]));
				}
				else {
					idsBody[1] = firstId;
				}
				
				// Add the exit condition
				row += body.getHeight();
				lastFigure = new PapFigure(row, column0, PapFigure.Type.PapCondition, text, null);
				figures.add(lastFigure);
				lastId = lastFigure.getId();
				// Connect the body's bottom with the connector if the body did not jump off
				if (idsBody[1] >= 0) {
					connections.add(new PapConnection(idsBody[1], lastId));
				}
				
				// Place the comment if necessary
				if (!comment.isEmpty()) {
					figures.add(new PapFigure(row, column0 + colBypassRight + 1,
							PapFigure.Type.PapComment, comment, null));
				}
				
				// Create the backlink
				firstFigure = new PapFigure(row, column0 + colBypassRight, PapFigure.Type.PapConnector, "", null);
				figures.add(firstFigure);
				connections.add(new PapConnection(lastId, firstFigure.getId(), "false"));
				
				lastFigure = new PapFigure(row0, column0 + colBypassRight, PapFigure.Type.PapConnector, "", null);
				figures.add(lastFigure);
				connections.add(new PapConnection(firstFigure.getId(), lastFigure.getId()));
				connections.add(new PapConnection(lastFigure.getId(), firstId));
				
			}
			else if (element instanceof While) {
				// At top there is a confluence connector
				firstFigure = new PapFigure(row++, column0, PapFigure.Type.PapConnector, "", null);
				figures.add(firstFigure);
				firstId = firstFigure.getId();
				
				// Add the entry condition
				firstFigure = new PapFigure(row, column0, PapFigure.Type.PapCondition, text, null);
				figures.add(firstFigure);
				connections.add(new PapConnection(firstId, firstFigure.getId()));

				// Create part of the forward link
				lastFigure = new PapFigure(row, column0 + colBypassRight, PapFigure.Type.PapConnector, "", null);
				figures.add(lastFigure);
				connections.add(new PapConnection(firstFigure.getId(), lastFigure.getId(), "false"));

				// Place the comment if necessary
				if (!comment.isEmpty()) {
					figures.add(new PapFigure(row, column0 + colBypassRight + 1,
							PapFigure.Type.PapComment, comment, null));
				}
				row++;
				
				// Generate the body items
				long[] idsBody = body.generateItems(figures, connections);
				if (idsBody[0] >= 0) {
					connections.add(new PapConnection(firstFigure.getId(), idsBody[0], "true"));
				}
				else {
					idsBody[1] = firstFigure.getId();
				}

				row += body.getHeight();
				// Create the backlink
				firstFigure = new PapFigure(row, column0, PapFigure.Type.PapConnector, "", null);	// below body
				figures.add(firstFigure);
				// Connect the body's bottom with the connector if the body did not jump off
				if (idsBody[1] >= 0) {
					connections.add(new PapConnection(idsBody[1], firstFigure.getId()));
				}
				idsBody[1] = firstFigure.getId();
				firstFigure = new PapFigure(row, column0 + colBypassLeft, PapFigure.Type.PapConnector, "", null);	// bottom left
				figures.add(firstFigure);
				connections.add(new PapConnection(idsBody[1], firstFigure.getId()));
				idsBody[1] = firstFigure.getId();
				firstFigure = new PapFigure(row0, column0 + colBypassLeft, PapFigure.Type.PapConnector, "", null);	// top left
				figures.add(firstFigure);
				connections.add(new PapConnection(idsBody[1], firstFigure.getId()));
				connections.add(new PapConnection(firstFigure.getId(), firstId));

				// Create the forward link
				row++;
				firstFigure = new PapFigure(row, column0 + colBypassRight, PapFigure.Type.PapConnector, "", null);	// below body
				figures.add(firstFigure);
				connections.add(new PapConnection(lastFigure.getId(), firstFigure.getId()));
				lastFigure = new PapFigure(row, column0, PapFigure.Type.PapConnector, "", null);
				figures.add(lastFigure);
				lastId = lastFigure.getId();
				connections.add(new PapConnection(firstFigure.getId(), lastId));
			}
			else if (element instanceof For) {
				// This should be a counting loop now.
				// Start with the initializer activity
				String initialization = ((For)element).getCounterVar() + " := " + ((For)element).getStartValue();
				int step = ((For)element).getStepConst();
				String condition = ((For)element).getCounterVar() + (step >= 0 ? " <= " : " >= ")
						+ ((For)element).getEndValue();
				String increment = ((For)element).getCounterVar() + " := "
						+ ((For)element).getCounterVar() + (step >= 0 ? " + " : " - ") + Math.abs(step);
				firstFigure = new PapFigure(row++, column0, PapFigure.Type.PapActivity, initialization, null);
				figures.add(firstFigure);
				firstId = firstFigure.getId();
				
				// Between initialization and condition there is a confluence connector
				lastFigure = new PapFigure(row++, column0, PapFigure.Type.PapConnector, "", null);
				figures.add(lastFigure);
				long conflId = lastFigure.getId();
				connections.add(new PapConnection(firstId, conflId));
				
				// Add the loop control
				firstFigure = new PapFigure(row, column0, PapFigure.Type.PapCondition, condition, null);
				figures.add(firstFigure);
				connections.add(new PapConnection(conflId, firstFigure.getId()));

				// Create part of the forward link
				lastFigure = new PapFigure(row, column0 + colBypassRight, PapFigure.Type.PapConnector, "", null);
				figures.add(lastFigure);
				connections.add(new PapConnection(firstFigure.getId(), lastFigure.getId(), "false"));

				// Place the comment if necessary
				if (!comment.isEmpty()) {
					figures.add(new PapFigure(row, column0 + colBypassRight + 1,
							PapFigure.Type.PapComment, comment, null));
				}
				row++;
				
				// Generate the body items
				long[] idsBody = body.generateItems(figures, connections);
				if (idsBody[0] >= 0) {
					connections.add(new PapConnection(firstFigure.getId(), idsBody[0], "true"));
				}
				else {
					idsBody[1] = firstFigure.getId();
				}

				row += body.getHeight();
				
				// Add the incrementation
				firstFigure = new PapFigure(row++, column0, PapFigure.Type.PapActivity, increment, null);	// below body
				figures.add(firstFigure);
				// Connect the body's bottom with the connector if the body did not jump off
				if (idsBody[1] >= 0) {
					connections.add(new PapConnection(idsBody[1], firstFigure.getId()));
				}
				idsBody[1] = firstFigure.getId();
				
				// Create the backlink
				firstFigure = new PapFigure(row, column0, PapFigure.Type.PapConnector, "", null);	// below increment
				figures.add(firstFigure);
				connections.add(new PapConnection(idsBody[1], firstFigure.getId()));
				idsBody[1] = firstFigure.getId();
				firstFigure = new PapFigure(row, column0 + colBypassLeft, PapFigure.Type.PapConnector, "", null);	// bottom left
				figures.add(firstFigure);
				connections.add(new PapConnection(idsBody[1], firstFigure.getId()));
				idsBody[1] = firstFigure.getId();
				firstFigure = new PapFigure(row0+1, column0 + colBypassLeft, PapFigure.Type.PapConnector, "", null);	// top left
				figures.add(firstFigure);
				connections.add(new PapConnection(idsBody[1], firstFigure.getId()));
				connections.add(new PapConnection(firstFigure.getId(), conflId));

				// Create the forward link
				row++;
				firstFigure = new PapFigure(row, column0 + colBypassRight, PapFigure.Type.PapConnector, "", null);	// below body
				figures.add(firstFigure);
				connections.add(new PapConnection(lastFigure.getId(), firstFigure.getId()));
				lastFigure = new PapFigure(row, column0, PapFigure.Type.PapConnector, "", null);
				figures.add(lastFigure);
				lastId = lastFigure.getId();
				connections.add(new PapConnection(firstFigure.getId(), lastId));
				
			}
			else if (element instanceof Forever) {
				// At top there is a confluence connector
				firstFigure = new PapFigure(row, column0, PapFigure.Type.PapConnector, "", null);
				figures.add(firstFigure);
				firstId = firstFigure.getId();
				
				// Place the comment if necessary
				if (!comment.isEmpty()) {
					figures.add(new PapFigure(row, column0 + 1,
							PapFigure.Type.PapComment, comment, null));
				}
				row++;

				// Generate the body items
				long[] idsBody = body.generateItems(figures, connections);
				if (idsBody[0] >= 0) {
					connections.add(new PapConnection(firstId, idsBody[0]));
				}
				else {
					idsBody[1] = firstId;
				}
				
				row += body.getHeight();
				// Create the backlink
				firstFigure = new PapFigure(row, column0, PapFigure.Type.PapConnector, "", null);	// below body
				figures.add(firstFigure);
				// Connect the body's bottom with the connector if the body did not jump off
				if (idsBody[1] >= 0) {
					connections.add(new PapConnection(idsBody[1], firstFigure.getId()));
				}
				idsBody[1] = firstFigure.getId();
				firstFigure = new PapFigure(row, column0 + colBypassLeft, PapFigure.Type.PapConnector, "", null);	// bottom left
				figures.add(firstFigure);
				connections.add(new PapConnection(idsBody[1], firstFigure.getId()));
				idsBody[1] = firstFigure.getId();
				firstFigure = new PapFigure(row0, column0 + colBypassLeft, PapFigure.Type.PapConnector, "", null);	// top left
				figures.add(firstFigure);
				connections.add(new PapConnection(idsBody[1], firstFigure.getId()));
				connections.add(new PapConnection(firstFigure.getId(), firstId));
				
				// TODO Produce an unreachable node for the successor element (or a possible leave)
				//lastFigure = new PapFigure(row+1, column0, PapFigure.Type.PapConnector, "", null);	// below body
				//figures.add(lastFigure);
				//lastId = lastFigure.getId();
			}
			return new long[] {firstId, lastId};
		}
	}
	
	/**
	 * Represents a {@link Parallel} element construction for PAP generation.
	 * @author Kay Gürtzig
	 */
	private class PapParallel extends PapElement {

		private ArrayList<PapSequence> branches = new ArrayList<PapSequence>();
		private int[] branchCols = null;	// relative column displacements of the branch anchors

		/**
		 * Creates a new PapParallel object from the given {@link Parallel} element {@code para}.
		 * @param para
		 */
		public PapParallel(Parallel para) {
			
			// FIXME: For the time being there is no support for Parallel element in PapDesigner
			super(para);
			/* We will place the branches side by side and create a distributor connector above them
			 * since the import interface obviously does not reject it though it can't be produced
			 * manually via the GUI.
			 * So the total width will be the sum of the widths of the branches.
			 * We will then need three extra rows
			 * 1. for the horizontal distribution to the branches;
			 * 2. for rejoining the branches
			 * 3. for the loop end figure
			 */
			height++;	// This is for the loop end figure
			branchCols = new int[para.qs.size()];
			int maxBranchHeight = 0;
			int totalWidth = 0;
			for (int i = 0; i < para.qs.size(); i++) {
				PapSequence sequ = new PapSequence(para.qs.get(i));
				branches.add(sequ);
				branchCols[i] = totalWidth + sequ.left;
				totalWidth += sequ.getWidth();
				maxBranchHeight = Math.max(maxBranchHeight, sequ.getHeight());
			}
			if (branchCols.length > 1) {
				left = (totalWidth-1) / 2;
				height += maxBranchHeight + 2;
			}
			else {
				left = branches.get(0).left;
			}
			right = Math.max(right, totalWidth - left - 1);
			for (int i = 0; i < branchCols.length; i++) {
				branchCols[i] -= left;
			}
		}

		@Override
		public int setAxisTop(int row, int column)
		{
			int bottom = super.setAxisTop(row, column);
			row ++;
			if (branchCols.length > 1) {
				row ++;
			}
			for (int i = 0; i < branches.size(); i++) {
				branches.get(i).setAxisTop(row, column + branchCols[i]);
			}
			return bottom;
		}
		
		@Override
		public long[] generateItems(List<PapFigure> figures, List<PapConnection> connections)
		{
			// FIXME: We do something here that is not supported by the PapDesigner GUI!!
			int distRow = row0 + 1;
			int joinRow = row0 + height - 2;
			boolean moreThan1 = branchCols.length > 1;
			long[] distIds = new long[branchCols.length];
			long[] joinIds = new long[branchCols.length];
			
			String comment = element.getComment().getText().trim();

			PapFigure figure = new PapFigure(row0, column0, PapFigure.Type.PapLoopStart,
					("(=== START PARALLEL SECTION ===)"), null);
			figures.add(figure);
			PapFigure firstFigure = figure;
			long firstId = figure.getId();
			figure = new PapFigure(row0 + height-1, column0, PapFigure.Type.PapLoopEnd,
					"true (=== END PARALLEL SECTION ===)", firstFigure);
			figures.add(figure);
			long lastId = figure.getId();
			
			// Place the comment at top 
			if (!comment.isEmpty()) {
				figures.add(new PapFigure(row0, column0 + 1, PapFigure.Type.PapComment, comment, null));
			}
			
			// Generate the distribution and junction networks
			if (moreThan1) {
				// Distributor node
				figures.add(figure = new PapFigure(distRow, column0, PapFigure.Type.PapConnector, "", null));
				long distId = figure.getId();
				connections.add(new PapConnection(firstId, distId));
				// Join node
				figures.add(figure = new PapFigure(joinRow, column0, PapFigure.Type.PapConnector, "", null));
				long joinId = figure.getId();
				connections.add(new PapConnection(joinId, lastId));
				
				for (int i = 0; i < branchCols.length; i++) {
					int colOffset = branchCols[i];
					if (colOffset == 0) {
						distIds[i] = distId;
						joinIds[i] = joinId;
					}
					else {
						figures.add(figure = new PapFigure(distRow, column0 + colOffset, PapFigure.Type.PapConnector, "", null));
						distIds[i] = figure.getId();
						figures.add(figure = new PapFigure(joinRow, column0 + colOffset, PapFigure.Type.PapConnector, "", null));
						joinIds[i] = figure.getId();
					}
					if (i > 0) {
						if (colOffset > 0 && branchCols[i-1] >= 0) {
							connections.add(new PapConnection(distIds[i-1], distIds[i]));
							connections.add(new PapConnection(joinIds[i], joinIds[i-1]));
						}
						else if (branchCols[i-1] < 0 && colOffset <= 0) {
							connections.add(new PapConnection(distIds[i], distIds[i-1]));
							connections.add(new PapConnection(joinIds[i-1], joinIds[i]));
						}
						else {
							// the neighbouring branches are at either side of the central axis
							connections.add(new PapConnection(distId, distIds[i-1]));
							connections.add(new PapConnection(distId, distIds[i]));
							connections.add(new PapConnection(joinIds[i-1], joinId));
							connections.add(new PapConnection(joinIds[i], joinId));
						}
					}
				}
			}
			else if (branchCols.length == 1) {
				distIds[0] = firstId;
				joinIds[0] = lastId;
			}
			
			// Generate the branches
			for (int i = 0; i < branches.size(); i++) {
				PapSequence branch = branches.get(i);
				long[] idsBranch = branch.generateItems(figures, connections);

				if (idsBranch[0] >= 0) {
					connections.add(new PapConnection(distIds[i], idsBranch[0]));
					if (idsBranch[1] >= 0) {
						connections.add(new PapConnection(idsBranch[1], joinIds[i]));
					}
				}
				else {
					connections.add(new PapConnection(distIds[i], joinIds[i]));
				}
				
			}

			return new long[] {firstId, lastId};
			
		}

	}
	
	/**
	 * Represents a TRY element for PAP export
	 * @author Kay Gürtzig
	 */
	private class PapTry extends PapElement {

		private PapSequence[] sections = new PapSequence[3];
		
		/** Relative column offset of the "catch branch" */
		private int colCatch = 0;
		
		/**
		 * @param _try - the {@link Try} element to be exported
		 */
		public PapTry(Try _try) {
			super(_try);
			height += 3;	// 2 for the catch bypass, 1 for the loop frame bottom
			sections[0] = new PapSequence(_try.qTry);
			sections[1] = new PapSequence(_try.qCatch);
			sections[2] = new PapSequence(_try.qFinally);
			for (int i = 0; i < sections.length; i++) {
				PapSequence section = sections[i];
				height += section.getHeight();
				if (i == 1) {
					right = Math.max(right, section.getWidth());
					colCatch = 1 + section.left;
				}
				else {
					left = Math.max(left, section.left);
					right = Math.max(right, section.right);
				}
			}
		}

		@Override
		public int setAxisTop(int row, int column)
		{
			super.setAxisTop(row, column);
			for (int i = 0; i < sections.length; i++) {
				PapSequence section = sections[i];
				row = section.setAxisTop(row + 1, (i == 1 ? column + colCatch : column));
			}
			return row + 1;
		}

		@Override
		public long[] generateItems(List<PapFigure> figures, List<PapConnection> connections)
		{
			// FIXME we must handle exits (leave commands / breaks), in particular throws, here
			long firstId = -1, lastId = -1;
			int row = row0;
			PapFigure firstFigure = new PapFigure(row++, column0, PapFigure.Type.PapLoopStart, "(=== START TRY ===)", null);
			figures.add(firstFigure);
			firstId = firstFigure.getId();
			// Try section
			long[] sectionIds = sections[0].generateItems(figures, connections);
			if (sectionIds[0] >= 0) {
				connections.add(new PapConnection(firstId, sectionIds[0]));
			}
			else {
				sectionIds[1] = firstId;
			}
			row += sections[0].getHeight();
			
			// Condition figure to symbolize the catch
			PapFigure lastFigure = new PapFigure(row, column0, PapFigure.Type.PapCondition,
					"Exception\n" + element.getUnbrokenText().getLongString() + "\ncaught?", null);
			figures.add(lastFigure);
			lastId = lastFigure.getId();
			if (sectionIds[1] >= 0) {
				connections.add(new PapConnection(sectionIds[1], lastId));
			}
			// Connector above the catch branch
			PapFigure connector = new PapFigure(row++, column0 + colCatch, PapFigure.Type.PapConnector, "", null);
			figures.add(connector);
			connections.add(new PapConnection(lastId, connector.getId(), "true"));
			// Catch section
			sectionIds = sections[1].generateItems(figures, connections);
			if (sectionIds[0] >= 0) {
				connections.add(new PapConnection(connector.getId(), sectionIds[0]));
			}
			else {
				sectionIds[1] = connector.getId();
			}
			row += sections[1].getHeight();
			// Connector below the catch branch
			figures.add(connector = new PapFigure(row, column0 + colCatch, PapFigure.Type.PapConnector, "", null));
			if (sectionIds[1] >= 0) {
				connections.add(new PapConnection(sectionIds[1], connector.getId()));
			}
			// Confluence connector
			figures.add(lastFigure = new PapFigure(row++, column0, PapFigure.Type.PapConnector, "", null));
			connections.add(new PapConnection(connector.getId(), lastFigure.getId()));
			connections.add(new PapConnection(lastId, lastFigure.getId(), "false"));
			lastId = lastFigure.getId();
			
			// Finally section
			sectionIds = sections[2].generateItems(figures, connections);
			if (sectionIds[0] >= 0) {
				connections.add(new PapConnection(lastId, sectionIds[0]));
			}
			else {
				sectionIds[1] = lastId;
			}
			row += sections[2].getHeight();
			lastFigure = new PapFigure(row, column0, PapFigure.Type.PapLoopEnd, "true (=== END TRY ===)", firstFigure);
			figures.add(lastFigure);
			if (sectionIds[1] >= 0) {
				connections.add(new PapConnection(sectionIds[1], lastFigure.getId()));
			}
			
			return new long[] {firstId, lastFigure.getId()};
		}	
		
	}

	// ============= Generation code for Entire Diagrams ===============

	@Override
	public String generateCode(Root _root, String _indent, boolean _public)
	{
		varNames = _root.getVarNames();
		
		String indent1 = getIndent();
		String indent2 = indent1 + getIndent();
		String indent3 = indent2 + getIndent();
		String indent4 = indent3 + getIndent();
		String indent5 = indent4 + getIndent();
		
		String pp_attributes = "";
		String date_attributes = "";
		// We must always place author, created, and modified attributes
		pp_attributes += " AUTHOR=\"" + BString.encodeToHtml(_root.getAuthor() == null ? System.getProperty("user.name") : _root.getAuthor()) + "\"";
		Date nextDate = _root.getCreated();
		if (nextDate == null) {
			nextDate = new Date();
		}
		date_attributes += " CREATED=\"" + dateFormat.format(nextDate) + "\"";
		if (_root.getModified() != null) {
			nextDate = _root.getModified();
		}
		date_attributes += " MODIFIED=\"" + dateFormat.format(nextDate) + "\"";
		if (topLevel) {
			din66001_1982 = !this.getPluginOption("din66001_1982", true).equals(false);
			
			code.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			appendComment("Generated by Structorizer " + Element.E_VERSION + " on " + dateFormat.format(new Date()), "");
			// The GUID is a constant prescribed by the PapDesigner developer F. Folkmann
			code.add("<FRAME GUID=\"2FB25471-B62C-4EE6-BD43-F819C095ACF8\" FORMAT=\"0000\" APP_VERSION=\"2.2.0.8\" CHECKSUM=\"UNSIGNED\">");
			// FIXME name should be of the group or file or so
			code.add(indent1 + "<PROJECT FORMAT=\"1.00\" NAME=\"" + BString.encodeToHtml(makeProjectName(_root)) + "\""
					+ pp_attributes + date_attributes + ">");
			code.add(indent2 + "<DIAGRAMS>");
		}
		
		// START KGU#863 2020-04-28: Issue #864: Transform parameter list
		//code.add(indent3 + "<DIAGRAM FORMAT=\"1.00\" ID=\"" + (currentNo++) + "\" NAME=\"" + BString.encodeToHtml(_root.getText().getLongString()) + "\"" + date_attributes + " >");
		code.add(indent3 + "<DIAGRAM FORMAT=\"1.00\" ID=\"" + (currentNo++) + "\" NAME=\"" + BString.encodeToHtml(transformHeader(_root)) + "\"" + date_attributes + " >");
		// END KGU#863 2020-04-28
		
		PapRoot rootNode = new PapRoot(_root);
		int columns = rootNode.getWidth();
		int rows = rootNode.getHeight();
		rootNode.setAxisTop(0, rootNode.left);
		code.add(indent4 + "<LAYOUT FORMAT=\"1.00\" COLUMNS=\"" + columns + "\" ROWS=\"" + rows + "\">");

		List<PapFigure> figures = new LinkedList<PapFigure>();
		List<PapConnection> connections = new LinkedList<PapConnection>();
		rootNode.generateItems(figures, connections);

		code.add(indent5 + "<ENTRIES>");
		for (PapFigure figure: figures) {
			figure.generateCode(code, indent5 + this.getIndent(), this.getIndent());
		}
		code.add(indent5 + "</ENTRIES>");
		code.add(indent4 + "</LAYOUT>");

		code.add(indent4 + "<CONNECTIONS>");
		for (PapConnection conn: connections) {
			conn.generateCode(code, indent5, this.getIndent());
		}
		code.add(indent4 + "</CONNECTIONS>");

		code.add(indent3 + "</DIAGRAM>");

		if (topLevel) {
			this.subroutineInsertionLine = code.count();
			this.libraryInsertionLine = code.count();
			code.add(indent2 + "</DIAGRAMS>");
			code.add(indent1 + "</PROJECT>");
			code.add("</FRAME>");
		}
		
		return code.getText();
	}

	// START KGU#396 2020-03-06: Issue #440 Common helper for PasGenerator and PapGenerator
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getModuleName()
	 */
	@Override
	protected String getModuleName()
	{
		String unitName = "";
		for (int i = 0; i < pureFilename.length(); i++)
		{
			char ch = pureFilename.charAt(i);
			if (!Character.isAlphabetic(ch) && !Character.isDigit(ch))
			{
				ch = '_';
			}
			unitName += ch;
		}
		return unitName;
	}
	
	/**
	 * Derives a project name for a bundle of exported diagrams from
	 * the proposed filename or (as default) from the name of the given
	 * {@code _root}
	 * @param _root - the (top-level) {@link Root} to be exported
	 * @return the proposed UNIT name
	 */
	protected String makeProjectName(Root _root) {
		String projName = this.getModuleName();
		if (projName.isEmpty()) {
			projName = _root.proposeFileName().toUpperCase();
			if (projName.contains("-")) {
				projName = projName.substring(0, projName.indexOf('-'));
			}
		}
		return projName;
	}
	// END KGU#396 2020-03-06

	// START KGU#815 2020-04-01: Enh. #828
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatesClass()
	 */
	@Override
	protected boolean allowsMixedModule()
	{
		return true;
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#max1MainPerModule()
	 */
	@Override
	protected boolean max1MainPerModule()
	{
		return false;
	}
	// END KGU#815 2020-04-01

	// START KGU#863 2020-04-28: Issue #864/1
	/**
	 * Provides a (possibly adapted) Root header that meets the requirements
	 * for the PapDesigner mapping between Calls and routines.
	 * (For PapDesigner v2.2.08.06, semicolons must be substituted.)
	 * @param root - the {@link Root} the header for which is needed
	 * @return the transformed routine header
	 */
	private String transformHeader(Root root) {
		String header = root.getText().getLongString();
		if (header.contains(";")) {
			// We might convert it to C or to some pseudo Pascal syntax with commas
			header = root.getMethodName();
			String resType = root.getResultType();
			StringList paramNames = new StringList();
			StringList paramTypes = new StringList();
			if (root.collectParameters(paramNames, paramTypes, null)) {
				header += "(";
				for (int i = 0; i < paramNames.count(); i++) {
					if (i > 0) {
						header += ", ";
					}
					String type = paramTypes.get(i);
					header += paramNames.get(i);
					if (type != null) {
						header += ":" + type;
					}
				}
				header += ")";
			}
			if (resType != null) {
				header += ": " + resType;
			}
		}
		return header;
	}
	// END KGU#863 2020-04-28
}
