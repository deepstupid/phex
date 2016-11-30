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
 *  $Id: NodeStatusZone.java 4490 2009-09-22 07:42:24Z gregork $
 */
package phex.gui.common.statusbar;

import phex.gui.common.GUIRegistry;
import phex.gui.common.IconPack;
import phex.gui.dialogs.options.OptionsDialog;
import phex.servent.Servent;
import phex.utils.Localizer;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NodeStatusZone extends JPanel
{
    private final Servent servent;
    
    private Icon ultrapeerIcon;
    private Icon leafModeIcon;
    private final JLabel nodeStatusLabel;

    public NodeStatusZone()
    {
        super(  );
        SpringLayout layout = new SpringLayout();
        setLayout( layout );
        
        servent = GUIRegistry.getInstance().getServent();
        
        nodeStatusLabel = new JLabel();
        add( nodeStatusLabel );
        
        nodeStatusLabel.addMouseListener(new MouseAdapter() { 
            public void mouseClicked(MouseEvent me) { 
                OptionsDialog optionsDialog=new OptionsDialog();
                optionsDialog.setModal(true);
                optionsDialog.changeSelectRow(optionsDialog.OptionsPaneSelector(OptionsDialog.OptionsPaneIndex.NETWORK));
                optionsDialog.show();
            }
        });
       
        updateZone();
        
        layout.putConstraint(SpringLayout.NORTH, nodeStatusLabel, 3, SpringLayout.NORTH, this );
        layout.putConstraint(SpringLayout.WEST, nodeStatusLabel, 5, SpringLayout.WEST, this );
        layout.putConstraint(SpringLayout.EAST, this, 5, SpringLayout.EAST, nodeStatusLabel );
        layout.putConstraint(SpringLayout.SOUTH, this, 3, SpringLayout.SOUTH, nodeStatusLabel );
        
        setupIcons();
        updateZone();
    }
    
    private void setupIcons()
    {
        IconPack factory = GUIRegistry.getInstance().getPlafIconPack();        
        ultrapeerIcon = factory.getIcon( "StatusBar.UltraPeer" );
        leafModeIcon = factory.getIcon( "StatusBar.LeafMode" );        
    }

    public void updateZone()
    {
        if ( servent.isUltrapeer() )
        {
            nodeStatusLabel.setIcon( ultrapeerIcon );
            nodeStatusLabel.setToolTipText( Localizer.getString( "StatusBar_TTTUltrapeer" ) );           
        }
        else
        {
            nodeStatusLabel.setIcon( leafModeIcon );                
            nodeStatusLabel.setToolTipText( Localizer.getString( "StatusBar_TTTLeaf" ) );
        }
        validate();
    }
}
