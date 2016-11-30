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
 *  $Id: LibraryZone.java 4426 2009-04-04 20:27:42Z tianna0370 $
 */
package phex.gui.common.statusbar;

import phex.common.format.NumberFormatUtils;
import phex.gui.common.GUIRegistry;
import phex.gui.common.IconPack;
import phex.gui.common.MainFrame;
import phex.share.SharedFilesService;
import phex.utils.Localizer;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Contains library information displayed on the status bar.
 */
public class LibraryZone extends JPanel 
{
    private final JLabel sharedFilesLabel;

    public LibraryZone() 
    {
        super();
        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        sharedFilesLabel = new JLabel();
        add(sharedFilesLabel);
        
        sharedFilesLabel.addMouseListener(new MouseAdapter() { 
            public void mouseClicked(MouseEvent me) { 
                MainFrame mainFrame = GUIRegistry.getInstance().getMainFrame();
                mainFrame.showLibraryTabByStatusBar();
            }
        });

        updateZone();

        layout.putConstraint(SpringLayout.WEST, sharedFilesLabel, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, sharedFilesLabel, 3, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.EAST, this, 5, SpringLayout.EAST, sharedFilesLabel);

        setupIcons();
    }

    private void setupIcons()
    {
        IconPack factory = GUIRegistry.getInstance().getPlafIconPack();
        sharedFilesLabel.setIcon( factory.getIcon( "StatusBar.Library" ) );
    }

    public void updateZone() 
    {
        SharedFilesService filesService = GUIRegistry.getInstance().
            getServent().getSharedFilesService();

        String label = Localizer.getFormatedString("StatusBar_Library", 
            NumberFormatUtils.formatNumber(filesService.getFileCount()),
            NumberFormatUtils.formatSignificantByteSize(
                filesService.getTotalFileSizeInKb() * 1024L) );
        sharedFilesLabel.setText(label);
        sharedFilesLabel.setToolTipText( Localizer.getString( "StatusBar_TTTLibrary" ) );
        
        validate();
    }
}
