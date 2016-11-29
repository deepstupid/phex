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
 *  $Id: MainStatusBar.java 4426 2009-04-04 20:27:42Z tianna0370 $
 */
package phex.gui.common.statusbar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import phex.common.bandwidth.BandwidthManager;
import phex.download.swarming.SwarmingManager;
import phex.gui.common.GUIRegistry;
import phex.servent.Servent;
import phex.gui.dialogs.options.OptionsDialog;
import phex.gui.common.MainFrame;

public class MainStatusBar extends StatusBar
{   
    private final UpdateAction updateAction;

    public MainStatusBar()
    {
        super();
        Servent servent = GUIRegistry.getInstance().getServent();
        BandwidthManager bandwidthService = servent.getBandwidthService();
        SwarmingManager downloadService = servent.getDownloadService();
        updateAction = new UpdateAction();
        
        ConnectionsZone connectionsZone= new ConnectionsZone();
        NodeStatusZone nodeStatusZone= new NodeStatusZone();
        LibraryZone libraryZone= new LibraryZone();
        DownloadZone downloadZone= new DownloadZone(downloadService, bandwidthService);
        UploadZone uploadZone= new UploadZone(bandwidthService);
        
        addZone("ConnectionsZone", connectionsZone, "");
        addZone("NodeStatusZone", nodeStatusZone, "*");
        addZone("LibraryZone", libraryZone, "");
        addZone("DownloadZone", downloadZone, "");
        addZone("UploadZone", uploadZone, "");
        
        connectionsZone.addMouseListener(new MouseAdapter() { 
            public void mouseClicked(MouseEvent me) { 
                OptionsDialog optionsDialog=new OptionsDialog();
                optionsDialog.setModal(true);
                optionsDialog.changeSelectRow(optionsDialog.OptionsPaneSelector(OptionsDialog.OptionsPaneIndex.NETWORK));
                optionsDialog.show();
            }
        });
        		
        nodeStatusZone.addMouseListener(new MouseAdapter() { 
            public void mouseClicked(MouseEvent me) { 
                OptionsDialog optionsDialog=new OptionsDialog();
                optionsDialog.setModal(true);
                optionsDialog.changeSelectRow(optionsDialog.OptionsPaneSelector(OptionsDialog.OptionsPaneIndex.NETWORK));
                optionsDialog.show();
            }
        }); 

        libraryZone.addMouseListener(new MouseAdapter() { 
            public void mouseClicked(MouseEvent me) { 
                MainFrame mainFrame = GUIRegistry.getInstance().getMainFrame();
                mainFrame.showLibraryTabByStatusBar();
            }
        });
    }
    
    @Override
    public void removeNotify()
    {
        super.removeNotify();
        GUIRegistry.getInstance().getGuiUpdateTimer().removeActionListener( 
            updateAction );
    }
    
    @Override
    public void addNotify()
    {
        super.addNotify();
        GUIRegistry.getInstance().getGuiUpdateTimer().addActionListener( 
            updateAction );
    }
    
    private final class UpdateAction implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {            
            ((ConnectionsZone)getZone("ConnectionsZone")).updateZone();
            ((NodeStatusZone)getZone("NodeStatusZone")).updateZone();
            ((DownloadZone)getZone("DownloadZone")).updateZone();
            ((UploadZone)getZone("UploadZone")).updateZone();
            ((LibraryZone)getZone("LibraryZone")).updateZone();
        }
    }
}
