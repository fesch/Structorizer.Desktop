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
package lu.fisch.structorizer.gui;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Dialog showing the progress of a CodeParser an allowing to abort the parsing.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2018-06-30      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import lu.fisch.structorizer.locales.LangDialog;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.structorizer.locales.Locales;

/**
 * Monitor dialog for a background parsing thread showing the current phase and possibly further progress info
 * @author Kay Gürtzig
 *
 */
@SuppressWarnings("serial")
public class CodeImportMonitor extends LangDialog implements PropertyChangeListener, ActionListener {

//	public static final LangTextHolder[] phaseNames = {
//			new LangTextHolder("Pre-processing file"),
//			new LangTextHolder("Parsing code"),
//			new LangTextHolder("Building digrams"),
//			new LangTextHolder("Post-processing diagrams")
//	};
	private static final int N_PARSER_PHASES = 4 /*phaseNames.length*/;
	
	private SwingWorker<?,?> worker;
	private JLabel[] phaseLabels = new JLabel[N_PARSER_PHASES];
	private JProgressBar[] progressBars = new JProgressBar[N_PARSER_PHASES];
	private JLabel lblRoots;
	private JLabel lblRootCount;
	private JPanel progressPane;
	private JPanel buttonPane;
	private JPanel buttonBar;
	private JButton btnOk, btnCancel;
	private JLabel lblErrors;
	private int phase = -1;
	
	public static final LangTextHolder ttlImporting = new LangTextHolder("Importing % code...");
	public static final LangTextHolder msgInterrupted = new LangTextHolder("Interrupted!");
	
	public CodeImportMonitor(Frame _owner, SwingWorker<?,?> _worker, String _title)
	{
		super(_owner, true);
		this.worker = _worker;

		initComponents();

		Locales.getInstance().setLocale(this);

		this.setTitle(ttlImporting.getText().replace("%", _title));

		this.setLocationRelativeTo(_owner);

		this.setVisible(true);
	}

	private void initComponents()
	{
		this.getContentPane();
		
		JPanel contentPane = new JPanel();
		
		contentPane.setLayout(new BorderLayout());
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		progressPane = new JPanel();
		GridLayout progressLayout = new GridLayout(0,2);
		progressLayout.setVgap(5);
		progressLayout.setHgap(5);
		progressPane.setLayout(progressLayout);
		
		for (int i = 0; i < N_PARSER_PHASES; i++) {
			phaseLabels[i] = new JLabel("Phase " + (i+1) /*phaseNames[i].getText()*/);	// Default label, to be overridden by the Locale's text
			JProgressBar progBar = progressBars[i] = new JProgressBar(0, 100);
			progBar.setValue(0);
			progBar.setStringPainted(true);
			progBar.setString("");
			progressPane.add(phaseLabels[i]);
			progressPane.add(progressBars[i]);
		}
		
		lblRoots = new JLabel("Created diagrams:");
		lblRootCount = new JLabel("");
		progressPane.add(lblRoots);
		progressPane.add(lblRootCount);
		
		contentPane.add(progressPane, BorderLayout.NORTH);
		
		buttonBar = new JPanel();
		buttonBar.setBorder(new EmptyBorder(10, 0, 0, 0));
		GridLayout buttonLayout = new GridLayout(0, 2);
		buttonLayout.setHgap(5);
		buttonBar.setLayout(buttonLayout);
		
		buttonPane = new JPanel();
		buttonPane.setLayout(new GridLayout(0, 2));
		
		btnOk = new JButton("OK");
		btnOk.addActionListener(this);
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(this);
		
		buttonPane.add(btnCancel);
		buttonPane.add(btnOk);
		
		lblErrors = new JLabel("Errors occurred!");
		
		buttonBar.add(buttonPane);
		buttonBar.add(lblErrors);
		lblErrors.setVisible(false);
		
		contentPane.add(buttonBar, BorderLayout.SOUTH);
		
		this.getContentPane().add(contentPane);
		
		GUIScaler.rescaleComponents(this);

		this.pack();

		btnOk.setEnabled(false);
		this.worker.addPropertyChangeListener(this);
		this.worker.execute();
		
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == this.worker) {
			String aspect = evt.getPropertyName();
			if (aspect.equals("state")) {
				//System.out.println("*** PropertyChange state: " + evt.getNewValue());
				if (this.worker.isDone()) {
					btnCancel.setEnabled(false);
					if (this.phase < 0) {
						this.phase = 0;
					}
					if (this.phase < this.progressBars.length && this.progressBars[this.phase].isIndeterminate()) {
						JProgressBar progBar = this.progressBars[this.phase];
						progBar.setIndeterminate(false);
						progBar.setValue(50);
						progBar.setString(msgInterrupted.getText());
					}
					btnOk.setEnabled(true);
				}
			}
			else if (aspect.equals("progress")) {
				//System.out.println("*** PropertyChange progress: " + evt.getNewValue() + " (" + this.phase + ")");
				if (this.phase < 0) {
					this.phase = 0;
				}
				this.progressBars[this.phase].setIndeterminate(false);
				this.progressBars[this.phase].setValue((Integer)evt.getNewValue());
				this.progressBars[this.phase].setString(null);
			}
			else if (aspect.equals("phase_start")) {
				//System.out.println("*** PropertyChange phase_start: " + evt.getNewValue());
				this.phase = (Integer)evt.getNewValue();
				for (int i = 0; i < this.phase && i < this.progressBars.length; i++) {
					//this.progressBars[i].setValue(100);
					this.progressBars[i].setIndeterminate(false);
					if (this.progressBars[i].getValue() == 0) {
						this.progressBars[i].setString("???");
					}
				}
				this.progressBars[this.phase].setIndeterminate(true);
			}
			else if (aspect.equals("root_count")) {
				this.lblRootCount.setText(((Integer)evt.getNewValue()).toString());
			}
			else if (aspect.equals("error")) {
				this.lblErrors.setVisible(true);
				this.pack();
			}
			else {
				System.out.println("*** Unknown PropertyChange aspect \"" + aspect + "\": " + evt.getNewValue());				
			}
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == btnCancel) {
			this.worker.cancel(true);
		}
		else if (evt.getSource() == btnOk) {
			this.dispose();
		}
	}
	
	

}
