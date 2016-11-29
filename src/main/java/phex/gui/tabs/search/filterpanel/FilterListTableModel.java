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
 *  $Id: FilterListTableModel.java 4420 2009-03-28 16:21:30Z gregork $
 */
package phex.gui.tabs.search.filterpanel;

import javax.swing.table.AbstractTableModel;

import phex.gui.tabs.search.SearchResultsDataModel;
import phex.rules.Rule;
import phex.rules.SearchFilterRules;
import phex.utils.Localizer;

/**
 * Table model that displays search filter rules.
 */
public class FilterListTableModel extends AbstractTableModel
{
    private SearchFilterRules rules;
    private SearchResultsDataModel displayedDataModel;
    
    public FilterListTableModel( SearchFilterRules rules )
    {
        this.rules = rules;
    }
    
    public void setDisplayedSearch( SearchResultsDataModel dataModel )
    {
        // otherwise no need to update...
        if ( displayedDataModel != dataModel )
        {
            displayedDataModel = dataModel;
            fireTableDataChanged( );
        }
    }

    public int getColumnCount()
    {
        return 2;
    }

    public int getRowCount()
    {
        return rules.getCount();
    }

    public Object getValueAt( int rowIndex, int columnIndex )
    {
        Rule rowRule = rules.getRuleAt(rowIndex);
        switch ( columnIndex )
        {
        case 0:
            if ( displayedDataModel == null )
            {
                return Boolean.FALSE;
            }
            return displayedDataModel.isRuleActive( rowRule ) ? Boolean.TRUE : Boolean.FALSE;
        case 1:
            return rules.getRuleAt(rowIndex);
        default:
            return "";
        }
    }

    public String getColumnName( int column )
    {
        switch ( column )
        {
        case 1:
            return Localizer.getString("SearchTab_SelectRulesToActivate");
        default:
            return "";    
        }
    }

    public Class<?> getColumnClass( int col )
    {
        switch ( col )
        {
        case 0:
            return Boolean.class;
        case 1:
            return Rule.class;
        default:
            return Object.class;
        }
    }

    public boolean isCellEditable( int row, int col )
    {
        return (col == 0) && displayedDataModel != null;
    }

    public void setValueAt( Object aValue, int row, int column )
    {
        Boolean boolVal = (Boolean)aValue;
        Rule rowRule = rules.getRuleAt( row );
        if ( boolVal.booleanValue() )
        {
            displayedDataModel.activateRule( rowRule );
        }
        else
        {
            displayedDataModel.deactivateRule( rowRule );
        }
    }
}
