/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2006 Phex Development Group
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Created on 04.12.2005
 *  --- CVS Information ---
 *  $Id: RulePropertiesPanel.java 4382 2009-03-12 17:19:53Z m_gar $
 */
package phex.gui.dialogs.filter.wizard;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import phex.rules.Rule;
import phex.utils.Localizer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class RulePropertiesPanel extends JPanel
{
    private final FilterWizardDialog parent;
    private JTextField nameTF;
    private JCheckBox permanentActiveRule;
    
    private JTextField notes;
    

    public RulePropertiesPanel( FilterWizardDialog parent )
    {
        this.parent = parent;
        prepareComponent();
    }
    
    private void prepareComponent()
    {
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout("fill:d:grow", // columns
            "p, 4dlu, p, 1dlu, p, 4dlu, p, 1dlu, p, 4dlu, p, 1dlu,p" );
        PanelBuilder contentPB = new PanelBuilder(layout, this);
        //int columnCount = layout.getColumnCount();
        //int rowCount = layout.getRowCount();
        
        JLabel label = new JLabel( 
            Localizer.getString("RuleWizard_FinalizeRule") );
        contentPB.add( label, cc.xywh(1, 1, 1, 1));
        
        label = new JLabel( Localizer.getString("RuleWizard_RuleName") );
        contentPB.add( label, cc.xywh(1, 3, 1, 1));
        
        nameTF = new JTextField( 20 );
        nameTF.getDocument().addDocumentListener( new NameDocumentListener() );
        nameTF.setToolTipText( Localizer.getString("RuleWizard_TTTRuleName") );
        contentPB.add( nameTF, cc.xywh(1, 5, 1, 1));
        
        
     
        label = new JLabel( Localizer.getString("RuleWizard_RuleNotes") );
        contentPB.add( label, cc.xywh(1, 7, 1, 1));
            
        notes = new JTextField( 20 );
        notes.getDocument().addDocumentListener( new NotesDocumentListener() );
        notes.setToolTipText( Localizer.getString("RuleWizard_TTTRuleNotes") );
        notes.addFocusListener(new FocusListener(){

			public void focusGained(FocusEvent e) {
				notes.setCaretPosition(0);
			}

			public void focusLost(FocusEvent e) {
				notes.setCaretPosition(0);
				
			}
		});
        contentPB.add( notes, cc.xywh(1, 9, 1, 1));
        
        
     
        
        label = new JLabel( Localizer.getString("RuleWizard_RuleOptions") );
        contentPB.add( label, cc.xywh(1, 11, 1, 1));
        
        permanentActiveRule = new JCheckBox( Localizer.getString( 
            "RuleWizard_PermanentlyEnableRule") );
        permanentActiveRule.setToolTipText( Localizer.getString( 
            "RuleWizard_TTTPermanentlyEnableRule") );
        permanentActiveRule.addActionListener( 
            new PermanentActiveActionListener() );
        contentPB.add( permanentActiveRule, cc.xywh(1, 13, 1, 1));
    }
    
    public void addNotify()
    {
        super.addNotify();
        nameTF.requestFocusInWindow();        
    }
    
    public void updateRuleData()
    {
        Rule rule = parent.getEditRule();
        nameTF.setText( rule.getName() );
        notes.setText( rule.getNotes() );
        permanentActiveRule.setSelected(rule.isPermanentlyEnabled());
        
        nameTF.setEnabled( !rule.isDefaultRule() );
        notes.setEnabled( !rule.isDefaultRule() );
        notes.setCaretPosition(0);
    }
    
    private final class PermanentActiveActionListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            Rule rule = parent.getEditRule();
            rule.setPermanentlyEnabled( permanentActiveRule.isSelected() );
        }
    }

    public class NameDocumentListener implements DocumentListener
    {
        public void changedUpdate( DocumentEvent e )
        {
            validateContent(e);
        }

        public void insertUpdate( DocumentEvent e )
        {
            validateContent(e);
        }

        public void removeUpdate( DocumentEvent e )
        {
            validateContent(e);
        }

        private void validateContent(DocumentEvent e)
        {
            String ruleName = nameTF.getText().trim();
            parent.getEditRule().setName(ruleName);
            parent.setFinishBtnEnabled( ruleName.length() > 0 );
        }
    }

    private class NotesDocumentListener implements DocumentListener
    {
        public void changedUpdate( DocumentEvent e )
        {
            validateContent(e);
        }

        public void insertUpdate( DocumentEvent e )
        {
            validateContent(e);
        }

        public void removeUpdate( DocumentEvent e )
        {
            validateContent(e);
        }

        private void validateContent(DocumentEvent e)
        {
            parent.getEditRule().setNotes(notes.getText().trim());
        }
    }
    
    
}
