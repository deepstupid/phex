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
 *  $Id: ConnectionObserver.java 4354 2009-01-14 22:07:40Z gregork $
 */
package phex.connection;


import phex.common.ThreadTracking;
import phex.common.log.NLogger;
import phex.host.Host;
import phex.host.HostStatus;
import phex.host.NetworkHostsContainer;
import phex.msghandling.MessageService;
import phex.util.Localizer;

import java.util.ArrayList;
import java.util.List;


/**
 * Observes the connected hosts to find sleeping connections. It verifies if
 * these connection still respond to incoming ping. In case they don't respond
 * the connections are dropped.
 */
public class ConnectionObserver implements Runnable {
    private static final long SLEEP_TIME = 1000;
    private static final long PING_WAIT_TIME = 2000;
    private final NetworkHostsContainer networkHostsContainer;
    private final MessageService messageService;
    private List<ConnectionSnapshoot> snapshootList;
    private List<Host> quiteList;

    public ConnectionObserver(NetworkHostsContainer networkHostsContainer,
                              MessageService messageService) {
        this.networkHostsContainer = networkHostsContainer;
        this.messageService = messageService;
    }

    public void start() {
        Thread thread = new Thread(ThreadTracking.rootThreadGroup, this,
                "ConnectionObserver-" + Integer.toHexString(hashCode()));
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    public void run() {
        snapshootList = new ArrayList<>();
        quiteList = new ArrayList<>();
        while (true) {
            snapshootList.clear();
            quiteList.clear();

            Host[] hosts = networkHostsContainer.getUltrapeerConnections();
            createSnapshoots(hosts);
            hosts = networkHostsContainer.getLeafConnections();
            createSnapshoots(hosts);

            //Sleep some time...
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
            }

            // Find connections that where quite in the meantime
            hosts = networkHostsContainer.getUltrapeerConnections();
            findQuiteHosts(hosts);
            hosts = networkHostsContainer.getLeafConnections();
            findQuiteHosts(hosts);

            if (quiteList.size() > 0) {
                // send a ping to each connection in the hope they respond to it.
                int size = quiteList.size();
                for (Host host : quiteList) {
                    NLogger.debug(ConnectionObserver.class,
                            host + " - Sending keep alive ping. ");
                    // first ping all hosts in the quite host list.
                    messageService.pingHost(host);
                }
                //Wait some time for the pinged hosts to respond...
                try {
                    Thread.sleep(PING_WAIT_TIME);
                } catch (InterruptedException e) {
                }
                for (Host host : quiteList) {
                    // Disconnect connection if it was still quite in the meantime
                    ConnectionSnapshoot shoot = findSnapshoot(host);
                    if (shoot.hasBeenQuiet()) {
                        host.setStatus(HostStatus.ERROR,
                                Localizer.getString("HostNotResponding"));
                        NLogger.debug(ConnectionObserver.class,
                                host + " - Host not responding, disconnecting..");
                        host.disconnect();
                    }
                }
            }
        }
    }

    private void findQuiteHosts(Host[] hosts) {
        for (Host host : hosts) {
            ConnectionSnapshoot shoot = findSnapshoot(host);
            if (shoot == null) {// this is a new host...
                continue;
            }

            if (shoot.hasBeenQuiet()) {
                quiteList.add(host);
            }
        }
    }

    private void createSnapshoots(Host[] hosts) {
        for (Host host : hosts) {
            snapshootList.add(new ConnectionSnapshoot(host));
        }
    }

    private ConnectionSnapshoot findSnapshoot(Host host) {
        int size = snapshootList.size();
        for (ConnectionSnapshoot shoot : snapshootList) {
            if (shoot.host == host) {
                return shoot;
            }
        }
        return null;
    }

    private static class ConnectionSnapshoot {
        final Host host;
        final int receivedCount;
        final int sentCount;
        final int sendDropCount;

        ConnectionSnapshoot(Host host) {
            this.host = host;
            receivedCount = host.getReceivedCount();
            sentCount = host.getSentCount();
            sendDropCount = host.getSentDropCount();
        }

        /**
         * Returns true if the data has not changed. This means we have received
         * no new data, or all sent packages have been dropped.
         *
         * @return true if the host has been quite, false others.
         */
        public boolean hasBeenQuiet() {
            int receivedDiff = host.getReceivedCount() - receivedCount;
            int sentDiff = host.getSentCount() - sentCount;
            int sendDropDiff = host.getSentDropCount() - sendDropCount;

            if (receivedDiff == 0) {
                return true;
            }

            return sentDiff == sendDropDiff && sentDiff != 0;

        }
    }
}