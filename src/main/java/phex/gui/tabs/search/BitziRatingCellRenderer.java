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

import phex.gui.renderer.FWTableCellRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 
 */
public class BitziRatingCellRenderer extends FWTableCellRenderer
{
    public BitziRatingCellRenderer()
    {
        super();
        setHorizontalAlignment( JLabel.RIGHT );
        addMouseListener( new MouseAction() );
    }

    public Component getTableCellRendererComponent( JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column )
    {
        super.getTableCellRendererComponent( table, value, isSelected, hasFocus, 
            row, column );
        
        Float rating = (Float) value;
        if ( rating.isNaN() )
        {
            setText( "-" );
        }
        if ( rating.isInfinite() )
        {
            setText( "?" );
        }
        
        setText( String.valueOf( rating ) );
        
        return this;
    }
    
    private static class MouseAction extends MouseAdapter
    {

        /**
         * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseClicked(MouseEvent e)
        {
            System.out.println("PUUUUUUUUUUUUUUUP");
            super.mouseClicked( e );
        }
        
    }
}