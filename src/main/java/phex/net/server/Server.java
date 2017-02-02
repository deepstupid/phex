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
 *  $Id: Server.java 4560 2012-01-23 15:28:53Z gregork $
 */
package phex.net.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.common.Environment;
import phex.common.address.AddressUtils;
import phex.common.address.DestAddress;
import phex.common.address.IpAddress;
import phex.common.address.LocalServentAddress;

import phex.event.ChangeEvent;
import phex.host.NetworkHostsContainer;
import phex.msghandling.MessageService;
import phex.net.UPnPMapper;
import phex.prefs.core.ConnectionPrefs;
import phex.prefs.core.NetworkPrefs;
import phex.prefs.core.ProxyPrefs;
import phex.servent.Servent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.TimerTask;

/**
 *
 */
public abstract class Server implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    /**
     * The servent this server acts for.
     */
    protected final Servent servent;
    /**
     * The local address of this server.
     */
    protected final LocalServentAddress localAddress;
    private final UPnPMapper upnpMapper;
    protected ServerSocket serverSocket;
    protected volatile boolean isRunning;
    /**
     * Indicates if a incoming connection was seen
     */
    protected boolean hasConnectedIncomming;
    /**
     * The time the last incoming connection was seen.
     */
    protected long lastInConnectionTime;
    private FirewallCheckTimer firewallCheckTimer;


    public Server(Servent servent) {
        this.servent = servent;
        hasConnectedIncomming = ConnectionPrefs.HasConnectedIncomming.get();
        lastInConnectionTime = -1;
        isRunning = false;

        localAddress = new LocalServentAddress(this);
        if (ProxyPrefs.ForcedIp.get().length() > 0) {
            IpAddress ip = new IpAddress(
                    AddressUtils.parseIP(ProxyPrefs.ForcedIp.get()));
            localAddress.setForcedHostIP(ip);
        }

        upnpMapper = new UPnPMapper();
    }

    public synchronized void startup() throws IOException {
        if (isRunning) {
            return;
        }
        logger.debug("Starting listener");
        isRunning = true;

        firewallCheckTimer = new FirewallCheckTimer(
                servent.getHostService().getNetworkHostsContainer(),
                servent.getMessageService());
        Environment.getInstance().scheduleTimerTask(firewallCheckTimer,
                FirewallCheckTimer.TIMER_PERIOD,
                FirewallCheckTimer.TIMER_PERIOD);

        bind(NetworkPrefs.ListeningPort.get());

        try {
            upnpMapper.initialize();
        } catch (Exception e) {
            logger.error("PnP: {}", e);
        }

        Environment.getInstance().executeOnThreadPool(this,
                "IncommingListener-" + Integer.toHexString(hashCode()));
    }

    protected abstract void bind(int initialPort) throws IOException;

    protected abstract void closeServer();

    public synchronized void restart() throws IOException {
        shutdown(true);
        startup();
    }

    public synchronized void shutdown(boolean waitForCompleted) {
        // not running, already dead or been requested to die.
        if (!isRunning) {
            return;
        }
        logger.debug("Shutting down listener");

        upnpMapper.shutdown();

        firewallCheckTimer.cancel();
        firewallCheckTimer = null;

        ConnectionPrefs.HasConnectedIncomming.set(hasConnectedIncomming);

        closeServer();

        if (waitForCompleted) {
            // Wait until the thread is dead.
            while (isRunning) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Indicates if this server has received an incoming connection from a
     * remote host.
     *
     * @return true if it has connected incoming, false otherwise.
     */
    public boolean hasConnectedIncoming() {
        return hasConnectedIncomming;
    }

    public IpAddress resolveLocalHostIP() {
        byte[] ip = null;
        InetAddress addr = serverSocket.getInetAddress();
        ip = addr.getAddress();
        IpAddress ipAddress;
        if (ip[0] == 0 && ip[1] == 0 && ip[2] == 0 && ip[3] == 0) {
            ipAddress = IpAddress.LOCAL_HOST_IP;
        } else {
            ipAddress = new IpAddress(ip);
        }
        return ipAddress;
    }

    public LocalServentAddress getLocalAddress() {
        return localAddress;
    }

    public void updateLocalAddress(DestAddress newAddress) {
        localAddress.updateLocalAddress(newAddress);
    }

    public int getListeningLocalPort() {
        if (serverSocket != null) {
            return serverSocket.getLocalPort();
        } else {
            return NetworkPrefs.ListeningPort.get();
        }
    }

    public class FirewallCheckTimer extends TimerTask {
        // once per 5 minutes
        public static final long TIMER_PERIOD = 1000L * 60 * 5;

        // but only when at least 30 minutes have passed since last good known
        // status.
        private static final long CHECK_TIME = 1000L * 60 * 30;

        private final NetworkHostsContainer netHostsContainer;
        private final MessageService messageService;

        /**
         * The last time a TCP connect back request check was sent.
         */
        private long lastFirewallCheckTime;

        FirewallCheckTimer(NetworkHostsContainer netHostsContainer, MessageService messageService) {
            this.netHostsContainer = netHostsContainer;
            this.messageService = messageService;

        }

        @Override
        public void run() {
            try {
                long now = System.currentTimeMillis();

                if ((hasConnectedIncomming && now - lastInConnectionTime > CHECK_TIME)
                        || (!hasConnectedIncomming && now - lastFirewallCheckTime > CHECK_TIME)) {
                    if (netHostsContainer.getUltrapeerConnectionCount() <= 2) {
                        return;
                    }

                    boolean isRequestSent = messageService.requestTCPConnectBack();
                    // in case no request was sent we just assume the last good
                    // known status.
                    if (isRequestSent) {
                        lastFirewallCheckTime = now;
                        Environment.getInstance().scheduleTimerTask(
                                new IncommingCheckRunner(),
                                IncommingCheckRunner.TIMER_PERIOD);
                    }
                }
            } catch (Throwable th) {
                logger.error(th.toString(), th);
            }
        }

        /**
         * Reacts on local address changes..
         */
        //@EventTopicSubscriber(topic=PhexEventTopics.Servent_LocalAddress)
        public void onLocaleAddressEvent(String topic, ChangeEvent event) {
            lastFirewallCheckTime = 0;
        }
    }

    private class IncommingCheckRunner extends TimerTask {
        // after 60 sec.
        public static final long TIMER_PERIOD = 1000 * 60;

        @Override
        public void run() {
            try {
                long now = System.currentTimeMillis();
                if (now - lastInConnectionTime > TIMER_PERIOD) {
                    hasConnectedIncomming = false;
                }
            } catch (Throwable th) {
                logger.error(th.toString(), th);
            }
        }
    }
}
