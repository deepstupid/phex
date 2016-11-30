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
 */
package phex.gui.renderer;

import phex.common.TransferDataProvider;

import javax.swing.*;
import java.awt.*;

public class CellColorHandler
{
    private static final Color green = new Color( 0, 128, 0);

    public static void applyCellColor( TransferDataProvider provider, JComponent component )
    {
        switch ( provider.getDataTransferStatus() )
        {
            case TransferDataProvider.TRANSFER_COMPLETED:
                component.setForeground( green );
                break;
            case TransferDataProvider.TRANSFER_ERROR:
                component.setForeground(Color.gray );
                break;
            case TransferDataProvider.TRANSFER_NOT_RUNNING:
                component.setForeground(Color.black );
                break;
            case TransferDataProvider.TRANSFER_RUNNING:
                component.setForeground(Color.blue );
                break;
        }
    }
}