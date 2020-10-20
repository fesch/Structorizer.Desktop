/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

    Copyright (C) 2009, 2020  Bob Fisch

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
 *      Description:    Class Download monitor, e.g. for User Guide download (#801) in background
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2020-10-20      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2020-10-20 Kay Gürtzig
 *        Until version 3.30-11, the User Guide download was inexpertly done in the event dispatch
 *        thread, thus making the GUI unresponsive for even several minutes (because the download takes
 *        an unreasonable time in comparison with a browser download of the same file - why?). So it
 *        had to be put in a background thread. Possibly it would be the best just to start it in the
 *        background and not mention it anymore - whatever time it takes.
 *        But well, I did the opposite: This little window contains just a indeterminate progress bar
 *        (no more information than a sand glass, but unfortunately we don't know the final size of the
 *        file in advance) and a cancel button. It places itself at the upper left screen corner and
 *        flickers dully along such that users may get inclined to cancel it.
 *        So may be it will not be used in the event unless we find a way to accelerate the download
 *        dramatically.
 *
 ******************************************************************************************************///

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import lu.fisch.structorizer.locales.LangDialog;


/**
 * Floating temporary dialog showing a progress bar during the download of a large file
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class DownloadMonitor extends LangDialog implements PropertyChangeListener {

	private SwingWorker<?,?> worker;
	private JProgressBar progrBar;
	private JButton btnCancel;

	public DownloadMonitor(Frame owner, SwingWorker<?,?> downloadWorker, String caption)
	{
		worker = downloadWorker;
		
		initComponents();
		
		setTitle(caption);
		
		this.setLocation(0, 0);
		this.setModalityType(ModalityType.MODELESS);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) 
			{
				btnCancel.doClick();
//				worker.cancel(true);
//				progrBar.setIndeterminate(false);
//				progrBar.setValue(30);				
//				JOptionPane.showMessageDialog(getOwner(),
//						Menu.msgDownloadFailed.getText().replace("%", Menu.lblCancel.getText()),
//						getTitle(),
//						JOptionPane.WARNING_MESSAGE);
//				dispose();				
			}
		});
		this.pack();
		this.setVisible(true);
	}

	/**
	 * 
	 */
	private void initComponents() {

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		
		progrBar = new JProgressBar(0, 100);
		progrBar.setValue(0);
		progrBar.setStringPainted(true);
		progrBar.setString("");
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				worker.cancel(true);
				progrBar.setIndeterminate(false);
				progrBar.setValue(30);
				btnCancel.setEnabled(false);
				JOptionPane.showMessageDialog(getOwner(),
						Menu.msgDownloadFailed.getText().replace("%", Menu.lblCancel.getText()),
						getTitle(),
						JOptionPane.WARNING_MESSAGE);
				setVisible(false);
				dispose();
			}});
		
		contentPane.add(progrBar);
		contentPane.add(btnCancel);
		
		this.getContentPane().add(contentPane);

		this.worker.addPropertyChangeListener(this);
		this.worker.execute();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == worker && "state".equals(evt.getPropertyName())) {
			System.out.println("Property Change received: "
					+ evt.getPropertyName() + "-->" + evt.getNewValue().toString());
			if (SwingWorker.StateValue.STARTED == evt.getNewValue()) {
				progrBar.setIndeterminate(true);
			}
			else if (SwingWorker.StateValue.DONE == evt.getNewValue()
					&& !worker.isCancelled()) {
				progrBar.setIndeterminate(false);
				progrBar.setValue(100);
				JOptionPane.showMessageDialog(getOwner(),
						Menu.msgDownloadComplete.getText(),
						getTitle(),
						JOptionPane.INFORMATION_MESSAGE);
				setVisible(false);
				dispose();
			}
		}
	}
}
