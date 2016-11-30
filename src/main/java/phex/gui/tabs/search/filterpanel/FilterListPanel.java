/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2009 Phex Development Group
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
 *  --- SVN Information ---
 *  $Id: FilterListPanel.java 4420 2009-03-28 16:21:30Z gregork $
 */
package phex.gui.tabs.search.filterpanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import phex.gui.actions.FWAction;
import phex.gui.common.table.FWTable;
import phex.gui.dialogs.filter.AdvSearchRulesDialog;
import phex.gui.tabs.search.SearchResultsDataModel;
import phex.rules.Rule;
import phex.rules.SearchFilterRules;
import phex.utils.Localizer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;


/**
 * A panel showing the list of search filter rules. 
 */
public class FilterListPanel extends JPanel
{
    private final SearchFilterRules filterRules;
    private FilterListTableModel filterListTableModel;
    private FWTable searchRuleTable;
    
    public FilterListPanel( SearchFilterRules rules )
    {
        super();
        this.filterRules = rules;
        initializeComponent( );
        updateUI();
    }

    /**
     * 
     */
    private void initializeComponent( )
    {
        setOpaque(false);
        
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout(
            "18dlu, fill:d:grow, 2dlu, d, 6dlu", // columns
            "6dlu, p, 6dlu," ); // rows
        PanelBuilder panelBuilder = new PanelBuilder( layout, this );
        
        filterListTableModel = new FilterListTableModel( filterRules );
        searchRuleTable = new FWTable( filterListTableModel );
        searchRuleTable.setVisibleRowCount( 3 );
        searchRuleTable.setShowVerticalLines(false);
        JTableHeader tableHeader = searchRuleTable.getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        
        TableColumn column = searchRuleTable.getColumnModel().getColumn(0);
        
        // adjust column width of checkbox
        TableCellRenderer renderer = searchRuleTable.getDefaultRenderer(Boolean.class);
        if ( renderer instanceof JComponent )
        {
            JComponent component = (JComponent) renderer;
            column.setMaxWidth( component.getPreferredSize().width+2 );
            column.setMinWidth( component.getPreferredSize().width+2 );
        }
        
        
        TableCellRenderer headerRenderer = searchRuleTable.getTableHeader().getDefaultRenderer();
        if ( headerRenderer instanceof JLabel )
        {
            ((JLabel)headerRenderer).setHorizontalAlignment(JLabel.LEFT);
        }
        column = searchRuleTable.getColumnModel().getColumn(1);
        column.setCellRenderer( new RuleCellRenderer() );
        
        JScrollPane scrollPane = FWTable.createFWTableScrollPane( searchRuleTable );
        panelBuilder.add( scrollPane, cc.xy(2, 2));
        
        //ButtonActionHandler btnActionHandler = new ButtonActionHandler();
        
        JButton btn = new JButton( new AdvancedFilterAction() );
        panelBuilder.add( btn, cc.xy(4, 2) );        
    }
    
    public void setDisplayedSearch( SearchResultsDataModel searchResultsDataModel )
    {
        if ( searchResultsDataModel == null )
        {
            searchRuleTable.setEnabled( false );
            searchRuleTable.clearSelection();
        }
        else
        {
            searchRuleTable.setEnabled( true );
        }
        
        filterListTableModel.setDisplayedSearch( searchResultsDataModel );
    }
    
    /**
     * Cell renderer of the search filter rules table.
     */
    private class RuleCellRenderer extends DefaultTableCellRenderer
    {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
        {
            super.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column );
            if ( value instanceof Rule )
            {
                Rule rule = (Rule)value;
                setText( rule.getName() );
                setToolTipText( rule.getNotes() );
            }
            return this;
        }
    }
    
    /**
     * Action to open the advanced search rules dialog.
     */
    private class AdvancedFilterAction extends FWAction
    {
        public AdvancedFilterAction()
        {
            super( Localizer.getString( "SearchTab_EditFilterRules" ), null,
                Localizer.getString( "SearchTab_TTTEditFilterRules" ) );
            refreshActionState();
        }

        public void actionPerformed( ActionEvent e )
        {
            new AdvSearchRulesDialog( filterRules ).setVisible( true );
        }

        @Override
        public void refreshActionState()
        {
            setEnabled( true );
        }
    }
}
