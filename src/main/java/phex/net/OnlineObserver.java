/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2011 Phex Development Group
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
 *  $Id: OnlineObserver.java 4523 2011-06-22 09:27:23Z gregork $
 */
package phex.net;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.common.Environment;
import phex.connection.ConnectionStatusEvent;
import phex.connection.ConnectionStatusEvent.Status;
import phex.event.ChangeEvent;
import phex.host.HostFetchingStrategy;
import phex.host.HostFetchingStrategy.FetchingReason;
import phex.host.NetworkHostsContainer;
import phex.prefs.core.ConnectionPrefs;
import phex.servent.OnlineStatus;
import phex.servent.Servent;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class tries to observers the online status of a connection.
 * If a certain amount of connection fail due to socket connection 
 * failure the online observer assumes a missing online connection 
 * and disconnects from network.
 */
public class OnlineObserver
{
    private static final Logger logger = LoggerFactory.getLogger( OnlineObserver.class );
    
    /**
     * The number of failed connections in a row.
     */
    private final AtomicInteger failedConnections;
    private final Servent servent;
    private final HostFetchingStrategy fetchingStrategy;
    private AutoReconnectTimer autoReconnectTimer;
    private long lastOfflineTime;
    
    /**
     * Delay to let host fetch operation take effect.
     */
    private long lastHostFetchTime;
    
    public OnlineObserver( Servent servent, HostFetchingStrategy fetchingStrategy )
    {
        this.fetchingStrategy = fetchingStrategy;
        this.servent = servent;
        failedConnections = new AtomicInteger(0);

    }
    
    //@EventTopicSubscriber(topic=PhexEventTopics.Net_ConnectionStatus)
    public void onConnectionStatusEvent( String topic, ConnectionStatusEvent event )
    {
        if ( event.getStatus() == Status.CONNECTION_FAILED )
        {
            // only count if there are no active connections in the network
            NetworkHostsContainer networkHostsContainer = 
                servent.getHostService().getNetworkHostsContainer();
            if ( networkHostsContainer.getTotalConnectionCount() > 0 )
            {
                failedConnections.set( 0 );
                return;
            }
            
            int fc = failedConnections.incrementAndGet();
            if ( logger.isDebugEnabled( ) && fc % 5 == 0 )
            {
                logger.debug( "Observed " + failedConnections + " failed connections.");
            }
            
            // if we have 15 failed connections trigger host fetch operation, honor a delay between
            // host fetching to let last operation take effect.
            if( fc % 15 == 0 && System.currentTimeMillis() - lastHostFetchTime > 15*DateUtils.MILLIS_PER_SECOND )
            {
                logger.info( "Started fetching new hosts due to increasing failed connections");
                lastHostFetchTime = System.currentTimeMillis();
            	fetchingStrategy.fetchNewHosts( FetchingReason.UpdateHosts );
            }
            
            if ( fc >= ConnectionPrefs.OfflineConnectionFailureCount.get())
            {
                logger.debug( "Too many connections failed.. disconnecting network.");
                // trigger timer to attempt to reconnect after some time...
                triggerReconnectTimer( );
            }
        }
        else
        {
            // for online status we don't care if handshake failed or not...
            resetAutoReconnect();
        }
    }
    
    private synchronized void triggerReconnectTimer()
    {
        OnlineStatus oldStatus = servent.getOnlineStatus();
        servent.setOnlineStatus( OnlineStatus.OFFLINE );
        if ( autoReconnectTimer != null )
        {
            autoReconnectTimer.setOfflineTime( lastOfflineTime );
            return;
        }
        
        autoReconnectTimer = new AutoReconnectTimer();
        autoReconnectTimer.setReconnectStatus( oldStatus );
        autoReconnectTimer.setOfflineTime( lastOfflineTime );
        Environment.getInstance().scheduleTimerTask( autoReconnectTimer, 
            1*DateUtils.MILLIS_PER_MINUTE,  2*DateUtils.MILLIS_PER_MINUTE );
    }
    
    private synchronized void autoReconnectTry( OnlineStatus status )
    {
        logger.debug( "Triggering auto-reconnect" );
        failedConnections.set( 0 );
        servent.setOnlineStatus( status );
    }
    
    private synchronized void resetAutoReconnect()
    {
        if ( autoReconnectTimer != null )
        {
            logger.debug( "Reset auto-reconnect" );
            autoReconnectTimer.cancel();
            autoReconnectTimer = null;
        }
        failedConnections.set( 0 );
    }
    
    /**
     * Reacts on online status changes to reset failed connection counter.
     */
    //@EventTopicSubscriber(topic=PhexEventTopics.Servent_OnlineStatus)
    public void onOnlineStatusEvent( String topic, ChangeEvent event )
    {
        OnlineStatus oldStatus = (OnlineStatus) event.getOldValue();
        OnlineStatus newStatus = (OnlineStatus) event.getNewValue();
        if ( oldStatus == OnlineStatus.OFFLINE && 
             newStatus != OnlineStatus.OFFLINE )
        {// switch from offline to any online status
            failedConnections.set( 0 );
        }
        else if ( oldStatus != OnlineStatus.OFFLINE && 
             newStatus == OnlineStatus.OFFLINE )
        {
            // monitor last offline switch time to detect user interaction
            lastOfflineTime = System.currentTimeMillis();
        }
    }
    
    public class AutoReconnectTimer extends TimerTask
    {
        private OnlineStatus reconnectStatus;
        private long offlineTime;
        
        @Override
        public void run()
        {
            try
            {
                if ( lastOfflineTime != offlineTime )
                {
                    cancel();
                    return;
                }
                if ( !servent.getOnlineStatus().isNetworkOnline() )
                {
                    autoReconnectTry( reconnectStatus );
                }
            }
            catch ( Throwable th )
            {
                logger.error( th.toString(), th );
            }
        }

        public void setReconnectStatus(OnlineStatus reconnectStatus)
        {
            this.reconnectStatus = reconnectStatus;
        }

        public void setOfflineTime(long offlineTime)
        {
            this.offlineTime = offlineTime;
        }

        public long getOfflineTime()
        {
            return offlineTime;
        }
    }
}