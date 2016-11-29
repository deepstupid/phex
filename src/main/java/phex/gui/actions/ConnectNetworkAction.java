/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2007 Phex Development Group
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
 *  $Id: ConnectNetworkAction.java 4377 2009-02-21 20:46:52Z gregork $
 */
package phex.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import org.bushe.swing.event.annotation.EventTopicSubscriber;

import phex.common.Phex;
import phex.event.ChangeEvent;
import phex.event.PhexEventTopics;
import phex.gui.common.GUIRegistry;
import phex.servent.OnlineStatus;
import phex.servent.Servent;
import phex.utils.Localizer;

public class ConnectNetworkAction extends FWAction
{
    public ConnectNetworkAction()
    {
        super( Localizer.getString( "Connect" ),
            GUIRegistry.getInstance().getPlafIconPack().getIcon( "Network.Connect" ),
            Localizer.getString( "TTTConnect" ), Integer.valueOf(
            Localizer.getChar( "ConnectMnemonic" ) ),
            KeyStroke.getKeyStroke( Localizer.getString( "ConnectAccelerator" ) ) );

        Servent servent = GUIRegistry.getInstance().getServent();
        setEnabled( servent.getOnlineStatus() == OnlineStatus.OFFLINE );
        Phex.getEventService().processAnnotations( this );
    }

    public void actionPerformed(ActionEvent e)
    {
        Servent servent = GUIRegistry.getInstance().getServent();
        servent.setOnlineStatus( OnlineStatus.ONLINE );
    }

    public void refreshActionState()
    {// global actions are not refreshed
        //setEnabled( ServiceManager.getNetworkManager().isNetworkJoined() );
    }
    
    /**
     * Reacts on online status changes.
     */
    @EventTopicSubscriber(topic = PhexEventTopics.Servent_OnlineStatus)
    public void onOnlineStatusEvent(String topic, ChangeEvent event)
    {
        OnlineStatus oldStatus = (OnlineStatus) event.getOldValue();
        OnlineStatus newStatus = (OnlineStatus) event.getNewValue();
        if (oldStatus == OnlineStatus.OFFLINE
            && newStatus != OnlineStatus.OFFLINE)
        {// switch from offline to any online status
            setEnabled( false );
        }
        else if (newStatus == OnlineStatus.OFFLINE
            && oldStatus != OnlineStatus.OFFLINE)
        {// switch from any online to offline status
            setEnabled( true );
        }
    }
}
