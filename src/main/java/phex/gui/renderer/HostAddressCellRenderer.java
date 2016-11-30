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
 *  $Id: HostAddressCellRenderer.java 4318 2008-11-30 22:50:47Z gregork $
 */
package phex.gui.renderer;

import phex.common.address.DestAddress;
import phex.gui.common.GUIRegistry;
import phex.gui.common.IconPack;
import phex.utils.Localizer;

import javax.swing.*;

/**
 * 
 */
public class HostAddressCellRenderer extends FWTableCellRenderer
{
    private final IconPack iconPack;
    
    public HostAddressCellRenderer()
    {
        iconPack = GUIRegistry.getInstance().getCountryIconPack();
    }
    
    /**
     * Sets the string for the cell being rendered to <code>value</code>.
     *
     * @param value  the string value for this cell; if value is
     *      <code>null</code> it sets the text value to an empty string
     * @see JLabel#setText
     *
     */
    protected void setValue( Object value )
    {
        if ( value instanceof DestAddress )
        {
            DestAddress address = (DestAddress)value;
            setText( address.getFullHostName() );
                        
            String countryCode = address.getCountryCode();
            Icon icon = null;
            String countraName = null;
            if ( countryCode != null && countryCode.length() > 0 )
            {
                icon = iconPack.getIcon( countryCode );
                countraName = Localizer.getCountryName( countryCode );
            }
            setIcon( icon );
            setToolTipText( countraName );
        }
        else
        {
            setText( value != null ? value.toString() : "" );
            setIcon( null );
            setToolTipText( null );
        }
    }
}
