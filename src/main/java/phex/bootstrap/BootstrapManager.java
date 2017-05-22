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
 *  $Id: BootstrapManager.java 4523 2011-06-22 09:27:23Z gregork $
 */
package phex.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.common.Environment;
import phex.common.address.DestAddress;
import phex.common.address.IpAddress;
import phex.host.NetworkHostsContainer;
import phex.peer.Peer;

import java.util.TimerTask;

/**
 *
 */
public class BootstrapManager {
    private static final Logger logger = LoggerFactory.getLogger(BootstrapManager.class);
    private final Peer peer;
    private final GWebCacheContainer gWebCacheContainer;

    /**
     * Lock to make sure not more then one thread request is running in parallel
     * otherwise it could happen that we create thread after thread while each
     * one takes a long time to come back.
     */
    private volatile boolean isThreadRequestRunning = false;

    public BootstrapManager(Peer peer) {
        this.peer = peer;
        gWebCacheContainer = new GWebCacheContainer(peer);
    }

    /**
     * Starts a query for more hosts in an extra thread.
     *
     * @param preferPhex indicates if a Phex GWebCache should be preferred
     */
    public synchronized void invokeQueryMoreHostsRequest(boolean preferPhex) {
        // we don't want multiple thread request to run at once. If one thread
        // request is running others are blocked.
        if (isThreadRequestRunning) {
            return;
        }
        isThreadRequestRunning = true;
        Runnable runner = new QueryHostsRunner(preferPhex);
        Environment.getInstance().executeOnThreadPool(runner,
                "GWebCacheQuery-" + Integer.toHexString(runner.hashCode()));
    }

    /**
     * Starts a update of GWebCaches in an extra thread.
     *
     * @param preferPhex indicates if a Phex GWebCache should be preferred
     */
    public void invokeUpdateRemoteGWebCache(final DestAddress myHostAddress, boolean preferPhex) {
        Runnable runner = new UpdateGWebCacheRunner(myHostAddress, preferPhex);
        Environment.getInstance().executeOnThreadPool(runner,
                "GWebCacheQuery-" + Integer.toHexString(runner.hashCode()));
    }

    /**
     * Starts a query for more GWebCaches in an extra thread.
     *
     * @param preferPhex indicates if a Phex GWebCache should be preferred
     */
    public synchronized void invokeQueryMoreGWebCachesRequest(boolean preferPhex) {
        // we don't want multiple thread request to run at once. If one thread
        // request is running others are blocked.
        if (isThreadRequestRunning) {
            return;
        }
        isThreadRequestRunning = true;
        Runnable runner = new QueryGWebCachesRunner(preferPhex);
        Environment.getInstance().executeOnThreadPool(runner,
                "GWebCacheQuery-" + Integer.toHexString(runner.hashCode()));
    }

    // temporary workaround method for post manager initialization
    public void postManagerInitRoutine() {
        NetworkHostsContainer netCont = peer.getHostService().getNetworkHostsContainer();
//        Environment.getInstance().scheduleTimerTask( 
//            new QueryGWebCacheTimer( netCont ), 0,
//            QueryGWebCacheTimer.TIMER_PERIOD );
        Environment.getInstance().scheduleTimerTask(
                new UpdateGWebCacheTimer(netCont), UpdateGWebCacheTimer.TIMER_PERIOD,
                UpdateGWebCacheTimer.TIMER_PERIOD);
    }

    ////////////////////////////////////////////////////////////////////////////
    /// Inner classes
    ////////////////////////////////////////////////////////////////////////////
    private final class QueryGWebCachesRunner implements Runnable {
        private final boolean preferPhex;

        /**
         * @param preferPhex
         */
        QueryGWebCachesRunner(boolean preferPhex) {
            this.preferPhex = preferPhex;
        }

        public void run() {
            try {
                gWebCacheContainer.queryMoreGWebCaches();
            } finally {
                isThreadRequestRunning = false;
            }
        }
    }

    private final class UpdateGWebCacheRunner implements Runnable {
        private final DestAddress myHostAddress;
        private final boolean preferPhex;

        /**
         * @param preferPhex
         */
        private UpdateGWebCacheRunner(DestAddress myHostAddress, boolean preferPhex) {
            this.myHostAddress = myHostAddress;
            this.preferPhex = preferPhex;
        }

        public void run() {
            gWebCacheContainer.updateRemoteGWebCache(myHostAddress);
        }
    }

    private final class QueryHostsRunner implements Runnable {
        private final boolean preferPhex;

        /**
         * @param preferPhex
         */
        QueryHostsRunner(boolean preferPhex) {
            this.preferPhex = preferPhex;
        }

        public void run() {
            try {
                gWebCacheContainer.queryMoreHosts();
            } finally {
                isThreadRequestRunning = false;
            }
        }
    }

// Never auto query GWebCache anymore in case we are connected...
// let HostFetchingStrategy do the job
//    private final class QueryGWebCacheTimer extends TimerTask
//    {
//        // every 180 minutes
//        public static final long TIMER_PERIOD = 1000 * 60 * 180;
//        
//        private final NetworkHostsContainer netHostsContainer;
//        
//        QueryGWebCacheTimer( NetworkHostsContainer netHostsContainer )
//        {
//            this.netHostsContainer = netHostsContainer;
//        }
//
//        @Override
//        public void run()
//        {
//            try
//            {
//                // no gwebcache actions if we have no auto connect and are
//                // not connected to any host
//                if ( servent.getOnlineStatus().isNetworkOnline() ||
//                    netHostsContainer.getTotalConnectionCount() > 0 )
//                {
//                    invokeQueryMoreHostsRequest( true );
//                }
//            }
//            catch ( Throwable th)
//            {
//                NLogger.error( GWebCacheManager.class, th, th );
//            }
//        }
//    }

    private final class UpdateGWebCacheTimer extends TimerTask {
        // once per 90 minutes
        public static final long TIMER_PERIOD = 1000L * 60 * 90;

        private final NetworkHostsContainer netHostsContainer;

        UpdateGWebCacheTimer(NetworkHostsContainer netHostsContainer) {
            this.netHostsContainer = netHostsContainer;
        }

        @Override
        public void run() {
            try {
                // no gwebcache actions if we have no auto connect and are
                // not connected to any host
                if (peer.getOnlineStatus().isNetworkOnline() ||
                        netHostsContainer.getTotalConnectionCount() > 0) {
                    DestAddress localAddress = null;
                    if (!peer.isFirewalled()) {
                        localAddress = peer.getLocalAddress();
                        IpAddress localIp = localAddress.getIpAddress();
                        if (localIp != null && localIp.isSiteLocalIP()) {
                            localAddress = null;
                        }
                    }
                    // even when localAddress is null update a GWebCache with
                    // a new GWebCache URL.
                    invokeUpdateRemoteGWebCache(localAddress, true);

                    //invokeQueryMoreGWebCachesRequest( false );
                }
            } catch (Throwable th) {
                logger.error(th.toString(), th);
            }
        }
    }
}
