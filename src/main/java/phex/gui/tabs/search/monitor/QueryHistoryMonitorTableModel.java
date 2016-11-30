/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2008 Phex Development Group
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
 *  $Id: QueryHistoryMonitorTableModel.java 4377 2009-02-21 20:46:52Z gregork $
 */
package phex.gui.tabs.search.monitor;

import org.bushe.swing.event.annotation.EventTopicSubscriber;
import phex.common.Phex;
import phex.event.PhexEventTopics;
import phex.host.Host;
import phex.msg.MsgHeader;
import phex.query.QueryHistoryMonitor;
import phex.query.QueryHistoryMonitor.HistoryEntry;
import phex.utils.Localizer;

import javax.swing.table.AbstractTableModel;
import java.awt.*;

public class QueryHistoryMonitorTableModel extends AbstractTableModel
{
    private static final int numColRoutedFrom = 0;
    private static final int numColSearchText = 1;
    private static final int numColHopsTtl = 2;

    private static final String[] tableColumns =
    {
    	Localizer.getString( "SearchMonitorTab_RoutedFrom" ),
    	Localizer.getString( "SearchMonitorTab_SearchText" ),
    	Localizer.getString( "SearchMonitorTab_HopsTtl" )
    };

    private QueryHistoryMonitor history;

    public QueryHistoryMonitorTableModel( QueryHistoryMonitor history )
    {
        this.history = history;
        Phex.getEventService().processAnnotations( this );
    }

    @Override
    public String getColumnName(int col)
    {
        return tableColumns[ col ];
    }

    public int getColumnCount()
    {
        return tableColumns.length;
    }

    public int getRowCount()
    {
        return history.getHistorySize();
    }

    public Object getValueAt(int row, int col)
    {
        HistoryEntry histEntry = history.getSearchQueryAt( row );
        if ( histEntry == null )
        {
            fireTableRowsDeleted( row, row );
            return null;
        }

        MsgHeader header = histEntry.getQueryMsg().getHeader();
        switch (col)
        {
            case numColRoutedFrom:
                Host fromHost = histEntry.getSourceHost();
                if ( fromHost == null )
                {
                    return "<Unknown>";
                }
                return fromHost.getHostAddress().getFullHostName();

            case numColSearchText:
                return histEntry.getQueryMsg().getSearchString();

            case numColHopsTtl:
                int hops = header.getHopsTaken();
                // remaining ttl + hops
                int ttl = hops + header.getTTL();
                return hops + " / " + ttl;
        }
        return "";
    }

    @Override
    public Class<?> getColumnClass(int col)
    {
        return String.class;
    }
    
    @EventTopicSubscriber(topic=PhexEventTopics.Query_Monitor)
    public void onQueryMonitorEvent( String topic, Object event )
    {
        EventQueue.invokeLater( new Runnable() {
            public void run()
            {
                fireTableDataChanged();
            }
        } );
    }
}