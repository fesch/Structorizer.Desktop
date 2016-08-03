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

package lu.fisch.structorizer.locales;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import lu.fisch.utils.StringList;

/**
 *
 * @author robertfisch
 */
public class Translator extends javax.swing.JFrame {
    
    private final Locales locales = new Locales();
    private final HashMap<String,JTable> tables = new HashMap<String,JTable>();
    private String loadedLocale;

    /**
     * Creates new form MainFrame
     */
    public Translator() {
        initComponents();
        
        // disable some buttons
        button_save.setEnabled(false);
        tabs.setEnabled(false);
        
        // initialise the header text
        headerText.setText("Please load a language!");

        // loop through all sections
        ArrayList<String> sectioNames = locales.getSectionNames();
        for (int i = 0; i < sectioNames.size(); i++) {
            // get the name
            String sectionName = sectioNames.get(i);
            
            // create a new tab
            Tab tab = new Tab();
            
            // add it to the panel
            tabs.add(sectionName, tab);
            
            // store a reference
            JTable table = tab.getTable();
            tables.put(sectionName, table);
            
            // set the name
            table.getColumnModel().getColumn(1).setHeaderValue(Locales.DEFAULT_LOCALE);
            table.getTableHeader().repaint();
            
            // fill the table with the keys and the values
            // from the default locale
            DefaultTableModel model = ((DefaultTableModel)table.getModel());
            ArrayList<String> keys = locales.getDefaultLocale().getKeyValues(sectionName);
            for (int j = 0; j < keys.size(); j++) {
                String key = keys.get(j);
                StringList parts = StringList.explode(key.trim(),"=");
                model.addRow(parts.toArray());
            }
        }
    }
    
    public void loadLocale(String localeName)
    {
        headerText.setText(locales.getLocale(localeName).getHeader().getText());
        
        // loop through all sections
        ArrayList<String> sectioNames = locales.getSectionNames();
        for (int i = 0; i < sectioNames.size(); i++) {
            // get the name of the section
            String sectionName = sectioNames.get(i);
            
            // fetch the corresponding table
            JTable table = tables.get(sectionName);
            
            // put the label on the column
            table.getColumnModel().getColumn(2).setHeaderValue(localeName);
            table.getTableHeader().repaint();
            
            // get a reference to the model
            DefaultTableModel model = ((DefaultTableModel)table.getModel());
            
            // get the needed locale and the corresponding section
            Locale locale = locales.getLocale(localeName);
            
            // get the strings and put them into the right row
            for (int r = 0; r < model.getRowCount(); r++) {
                // get the key
                String key = ((String) model.getValueAt(r, 0)).trim();
                // put the value
                model.setValueAt(locale.getValue(sectionName, key), r, 2);
            }
            
        }

        // enable the buttons
        button_save.setEnabled(true);
        tabs.setEnabled(true);  
        
        // remeber the loaded localename
        loadedLocale = localeName;
    }
    
    /*
    private void getMissingStrings()
    {
        // get a list of the english strings only
        StringList baseList = new StringList();
        for (int i = 0; i < enLines.count(); i++) {
            String get = enLines.get(i);
            StringList parts = StringList.explode(enLines.get(i).trim(),"=");
            if(enLines.get(i).trim().contains("=") && parts.get(0).contains(".") && !parts.get(0).startsWith("//"))
            {
                baseList.add(parts.get(0));
            }
        }
        System.out.println("Base: "+baseList.count());
        
        // load strings of all other languages
        String[] list = {"chs","cht","cz","de","en","es","fr","it","lu","nl","pl","pt_br","ru"};
        StringList others = new StringList();
        for (int i = 0; i < list.length; i++) 
        {
            StringList myList = loadLang(list[i]+".txt");
            for (int j = 0; j < myList.count(); j++) {
                String get = myList.get(j);
                StringList parts = StringList.explode(myList.get(j).trim(),"=");
                if(myList.get(j).trim().contains("=") && parts.get(0).contains(".") && !parts.get(0).startsWith("//"))
                {
                    if(!others.contains(parts.get(0)))
                        others.add(parts.get(0));
                }
            }
        }
        System.out.println("Others: "+others.count());
        
        // now get the delta
        StringList delta = new StringList();
        for (int i = 0; i < others.count(); i++) 
        {
            String key = others.get(i);
            if(!baseList.contains(key) && !key.startsWith("//"))
                delta.add(key+"=");
        }
        
        System.out.println("The EN lang is missing these keys:");
        System.out.println(delta.getText());
        
        if(delta.count()>0)
        {
            JOptionPane.showMessageDialog(this, "The reference language file (en.txt) misses strings that have been found in another language file.\n"+
                        "Translator will terminate immediately in order to prevent data loss ...", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
*/
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        button_fr = new javax.swing.JButton();
        button_nl = new javax.swing.JButton();
        button_lu = new javax.swing.JButton();
        button_de = new javax.swing.JButton();
        button_es = new javax.swing.JButton();
        button_pt_br = new javax.swing.JButton();
        button_it = new javax.swing.JButton();
        button_chs = new javax.swing.JButton();
        button_cz = new javax.swing.JButton();
        button_ru = new javax.swing.JButton();
        button_pl = new javax.swing.JButton();
        button_cht = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        button_save = new javax.swing.JButton();
        button_en = new javax.swing.JButton();
        tabs = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        headerText = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(900, 500));

        jPanel1.setBackground(new java.awt.Color(255, 255, 204));
        jPanel1.setPreferredSize(new java.awt.Dimension(655, 48));

        button_fr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/045_fr.png"))); // NOI18N
        button_fr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_frActionPerformed(evt);
            }
        });

        button_nl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/051_nl.png"))); // NOI18N
        button_nl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_nlActionPerformed(evt);
            }
        });

        button_lu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/075_lu.png"))); // NOI18N
        button_lu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_luActionPerformed(evt);
            }
        });

        button_de.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/080_de.png"))); // NOI18N
        button_de.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_deActionPerformed(evt);
            }
        });

        button_es.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/084_es.png"))); // NOI18N
        button_es.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_esActionPerformed(evt);
            }
        });

        button_pt_br.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/085_pt_br.png"))); // NOI18N
        button_pt_br.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_pt_brActionPerformed(evt);
            }
        });

        button_it.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/086_it.png"))); // NOI18N
        button_it.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_itActionPerformed(evt);
            }
        });

        button_chs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/087_cn.png"))); // NOI18N
        button_chs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_chsActionPerformed(evt);
            }
        });

        button_cz.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/088_cz.png"))); // NOI18N
        button_cz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_czActionPerformed(evt);
            }
        });

        button_ru.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/092_ru.png"))); // NOI18N
        button_ru.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_ruActionPerformed(evt);
            }
        });

        button_pl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/093_pl.png"))); // NOI18N
        button_pl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_plActionPerformed(evt);
            }
        });

        button_cht.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/094_tw.png"))); // NOI18N
        button_cht.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_chtActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        jLabel1.setText("Load");

        button_save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/003_Save.png"))); // NOI18N
        button_save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_saveActionPerformed(evt);
            }
        });

        button_en.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/046_uk.png"))); // NOI18N
        button_en.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_enActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_en)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_fr)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_nl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_lu)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_de)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_es)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_pt_br)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_it)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_chs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_cz)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_ru)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_pl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_cht)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 368, Short.MAX_VALUE)
                .addComponent(button_save)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(button_en, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_save, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_it, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(button_cht, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_pl, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_ru, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_cz, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_chs, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_pt_br, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_es, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_de, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_lu, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_nl, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_fr, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        jPanel2.setLayout(new java.awt.BorderLayout());

        headerText.setFont(new java.awt.Font("Monospaced", 0, 10)); // NOI18N
        jScrollPane2.setViewportView(headerText);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        tabs.addTab("Header", jPanel2);

        getContentPane().add(tabs, java.awt.BorderLayout.CENTER);
        tabs.getAccessibleContext().setAccessibleName("Strings");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void button_frActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_frActionPerformed
        loadLocale("fr");
    }//GEN-LAST:event_button_frActionPerformed

    private void button_nlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_nlActionPerformed
        loadLocale("nl");
    }//GEN-LAST:event_button_nlActionPerformed

    private void button_luActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_luActionPerformed
        loadLocale("lu");
    }//GEN-LAST:event_button_luActionPerformed

    private void button_deActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_deActionPerformed
        loadLocale("de");
    }//GEN-LAST:event_button_deActionPerformed

    private void button_esActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_esActionPerformed
        loadLocale("es");
    }//GEN-LAST:event_button_esActionPerformed

    private void button_pt_brActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_pt_brActionPerformed
        loadLocale("pt_br");
    }//GEN-LAST:event_button_pt_brActionPerformed

    private void button_itActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_itActionPerformed
        loadLocale("it");
    }//GEN-LAST:event_button_itActionPerformed

    private void button_chsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_chsActionPerformed
        loadLocale("chs");
    }//GEN-LAST:event_button_chsActionPerformed

    private void button_czActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_czActionPerformed
        loadLocale("cz");
    }//GEN-LAST:event_button_czActionPerformed

    private void button_ruActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_ruActionPerformed
        loadLocale("ru");
    }//GEN-LAST:event_button_ruActionPerformed

    private void button_plActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_plActionPerformed
        loadLocale("pl");
    }//GEN-LAST:event_button_plActionPerformed

    private void button_chtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_chtActionPerformed
        loadLocale("cht");
    }//GEN-LAST:event_button_chtActionPerformed

    private void button_saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_saveActionPerformed
        // load a copy of the default locale
        Locale locale = locales.getDefaultLocale().loadCopyFromFile();
        
        // put the header the locale to save
        locale.setHeader(StringList.explode(headerText.getText(), "\n"));
        
        // loop through all sections in order to merge the values
        ArrayList<String> sectioNames = locales.getSectionNames();
        for (int i = 0; i < sectioNames.size(); i++) {
            // get the name of the section
            String sectionName = sectioNames.get(i);
            
            // fetch the corresponding table
            JTable table = tables.get(sectionName);

            // get a reference to the model
            DefaultTableModel model = ((DefaultTableModel)table.getModel());
            
            // get the strings and put them into locale
            for (int r = 0; r < model.getRowCount(); r++) {
                // get the key
                String key = ((String) model.getValueAt(r, 0)).trim();
                // get the value
                String value = ((String) model.getValueAt(r, 2)).trim();
                // put the value
                locale.setValue(sectionName, key, value);
            }
        }
        
        // now ask where to save the data
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as");   
        fileChooser.setSelectedFile(new File(loadedLocale+".txt"));
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) 
        {
            File fileToSave = fileChooser.getSelectedFile();
            try
            {
                FileOutputStream fos = new FileOutputStream(fileToSave);
                Writer out = new OutputStreamWriter(fos, "UTF8");
                out.write(locale.getText());
                out.close();
            }
            catch (IOException e)
            {
                JOptionPane.showMessageDialog(this, "Error while saving language file\n"+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }     
    }//GEN-LAST:event_button_saveActionPerformed

    private void button_enActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_enActionPerformed
        loadLocale("en");
    }//GEN-LAST:event_button_enActionPerformed

    public static void launch()
    {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Translator translater = new Translator();
                translater.setVisible(true);
                translater.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            }
        });
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Translator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Translator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Translator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Translator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Translator().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_chs;
    private javax.swing.JButton button_cht;
    private javax.swing.JButton button_cz;
    private javax.swing.JButton button_de;
    private javax.swing.JButton button_en;
    private javax.swing.JButton button_es;
    private javax.swing.JButton button_fr;
    private javax.swing.JButton button_it;
    private javax.swing.JButton button_lu;
    private javax.swing.JButton button_nl;
    private javax.swing.JButton button_pl;
    private javax.swing.JButton button_pt_br;
    private javax.swing.JButton button_ru;
    private javax.swing.JButton button_save;
    private javax.swing.JTextPane headerText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane tabs;
    // End of variables declaration//GEN-END:variables
}
