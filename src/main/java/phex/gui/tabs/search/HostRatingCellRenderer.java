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
 *  $Id$
 */
package phex.gui.tabs.search;

import phex.download.RemoteFile;
import phex.gui.renderer.FWTableCellRenderer;
import phex.query.QHDFlag;
import phex.query.QueryHitHost;
import phex.utils.Localizer;

import javax.swing.*;
import java.awt.*;

/**
 * 
 */
public class HostRatingCellRenderer extends FWTableCellRenderer
{
    public HostRatingCellRenderer()
    {
        super();
        setHorizontalAlignment( JLabel.RIGHT );
    }

    public Component getTableCellRendererComponent( JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column )
    {
        super.getTableCellRendererComponent( table, value, isSelected, hasFocus, 
            row, column );
        
        QueryHitHost queryHitHost = null;
        if ( value instanceof SearchResultElement)
        {
            SearchResultElement result = ((SearchResultElement)value);            
            if ( result.getRemoteFileListCount() != 0 )
            {
                setText( String.valueOf( result.getBestRatedFile()
                    .getQueryHitHost().getHostRating() ) );
                setToolTipText( null );
                return this;
            }
            
            queryHitHost = result.getSingleRemoteFile().getQueryHitHost();
        }
        else if ( value instanceof RemoteFile )
        {
            queryHitHost = ((RemoteFile) value).getQueryHitHost();
        }
        if ( queryHitHost == null )
        {
            setText( "" );
            setToolTipText( null );
            return this;
        }
        setText( String.valueOf( queryHitHost.getHostRating() ) );
        
        StringBuilder builder = new StringBuilder( "<html>");
        handleQHDFlag( queryHitHost.getPushNeededFlag(),
            "SearchTab_HostRating_FirewalledTrue",
            "SearchTab_HostRating_FirewalledFalse",
            "SearchTab_HostRating_FirewalledUnknown",
            builder );
        builder.append( "<br>" );
        handleQHDFlag( queryHitHost.getServerBusyFlag(),
            "SearchTab_HostRating_ServerBusyTrue",
            "SearchTab_HostRating_ServerBusyFalse",
            "SearchTab_HostRating_ServerBusyUnknown",
            builder );
        builder.append( "<br>" );
        handleQHDFlag( queryHitHost.getHasUploadedFlag(),
            "SearchTab_HostRating_HasUploadedTrue",
            "SearchTab_HostRating_HasUploadedFalse",
            "SearchTab_HostRating_HasUploadedUnknown",
            builder );
        builder.append( "<br>" );
        if ( queryHitHost.isBrowseHostSupported() )
        {
            builder.append( Localizer.getString( "SearchTab_HostRating_SupportsBrowseHost" ) );
            builder.append( "<br>" );
        }
        if ( queryHitHost.isChatSupported() )
        {
            builder.append( Localizer.getString( "SearchTab_HostRating_SupportsChat" ) );
            builder.append( "<br>" );
        }
        if ( queryHitHost.isUdpHost() )
        {
            builder.append( Localizer.getString( "SearchTab_HostRating_ReceivedViaUdp" ) );
            builder.append( "<br>" );
        }
        setToolTipText( builder.toString() );
        
        return this;
    }

    private void handleQHDFlag( QHDFlag flag, String trueStr,
        String falseStr, String unknownStr, StringBuilder builder )
    {
        switch (flag)
        {
        case QHD_TRUE_FLAG:
            builder.append( Localizer.getString( trueStr ) );
            break;
        case QHD_FALSE_FLAG:
            builder.append( Localizer.getString( falseStr ) );
            break;
        case QHD_UNKNOWN_FLAG:
            builder.append( Localizer.getString( unknownStr ) );
            break;
        }
    }
}