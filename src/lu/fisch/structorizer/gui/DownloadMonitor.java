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
 *      Kay Gürtzig     2020-10-20      First Issue (for #801 improvement)
 *      Kay Gürtzig     2020-10-22      Sensible progress bar
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
 *        But well, I did the opposite and showed a progress bar in this little window placed at the
 *        upper left corner.
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
	private long size = 0;

	public DownloadMonitor(Frame owner, SwingWorker<?,?> downloadWorker, String caption, long expectedSize)
	{
		super(owner);
		worker = downloadWorker;
		size = expectedSize;
		
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
		progrBar.setIndeterminate(true);
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				worker.cancel(true);
				progrBar.setIndeterminate(false);
				//progrBar.setValue(30);
				btnCancel.setEnabled(false);
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
		if (evt.getSource() == worker) {
			if ("state".equals(evt.getPropertyName())) {
				//System.out.println("Property Change received: "
				//		+ evt.getPropertyName() + "-->" + evt.getNewValue().toString());
				if (SwingWorker.StateValue.STARTED == evt.getNewValue()) {
					progrBar.setIndeterminate(false);
					progrBar.setString("0 %");
				}
				else if (SwingWorker.StateValue.DONE == evt.getNewValue()
						&& !worker.isCancelled()) {
//					progrBar.setIndeterminate(false);
//					progrBar.setValue(100);
					progrBar.setString("100 %");
					btnCancel.setEnabled(false);
					setVisible(false);
					dispose();
				}
			}
			else if ("progress".equals(evt.getPropertyName()) && size > 0) {
				Object value = evt.getNewValue();
				if (value instanceof Long) {
					int percentage = (int)Math.min((100 * ((Long)value).intValue() / size), 100);
					progrBar.setValue(percentage);
					// We better avoid the misconception that the transfer is ready
					if (percentage < 100) {
						progrBar.setString(percentage + " %");
					}
				}
			}
		}
	}
}
