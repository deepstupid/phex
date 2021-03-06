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
 *  $Id: MessageService.java 4523 2011-06-22 09:27:23Z gregork $
 */
package phex.msghandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.MessagePrefs;
import phex.bootstrap.UdpHostCacheContainer;
import phex.common.AbstractLifeCycle;
import phex.common.Environment;
import phex.common.PongCache;
import phex.common.QueryRoutingTable;
import phex.common.address.DestAddress;
import phex.host.CaughtHostsContainer;
import phex.host.Host;
import phex.host.NetworkHostsContainer;
import phex.msg.*;
import phex.msg.vendor.*;
import phex.security.PhexSecurityManager;
import phex.peer.Peer;
import phex.share.SharedFilesService;

import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

public class MessageService extends AbstractLifeCycle {
    private static final Logger logger = LoggerFactory.getLogger(
            MessageService.class);

    private final Peer peer;
    private final MessageRouting messageRouting;
    private final MessageDispatcher messageDispatcher;
    private final QueryMsgRoutingHandler queryMsgRoutingHandler;
    private final PongFactory pongFactory;

    private UdpMessageDataHandler udpHandler;

    // TODO PongCache might also be moved to MessageDispatcher, but there is 
    // still a dependency to UdpMessageEngine.
    private PongCache pongCache;

    /**
     * Timer responsible to trigger QRT updates.
     */
    private QRPUpdateTimer qrpUpdateTimer;

    /**
     * The last sent query routing table, used for dynamic query
     * for a fast check for matches.
     */
    private QueryRoutingTable lastSentQueryRoutingTable;

    /**
     * The number of TCP redirects sent during the last time frame.
     */
    private int numberOfTCPRedirectsSent;
    public MessagePrefs prefs;

    public MessageService(NetworkHostsContainer netHostsContainer,
                          CaughtHostsContainer caughtHostsContainer, UdpHostCacheContainer uhcContainer,
                          PhexSecurityManager securityService, Peer peer) {

        this.peer = peer;
        pongCache = new PongCache(peer);

        this.prefs = new MessagePrefs(peer.file(Peer.MESSAGE_PREFS_FILE));

        messageRouting = new MessageRouting();

        pongFactory = new PongFactory(netHostsContainer, caughtHostsContainer,
                securityService);
        messageDispatcher = new MessageDispatcher(peer, messageRouting,
                pongFactory);

        queryMsgRoutingHandler = new QueryMsgRoutingHandler(peer, messageRouting);
        messageDispatcher.addMessageSubscriber(QueryMsg.class, queryMsgRoutingHandler);

    }

    ////////////////////////////////////////////////////////////////////////////
    // LifeCycle Methods
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public void doStart() {
        messageDispatcher.initStats(peer.getStatisticsService());

        udpHandler = new UdpMessageDataHandler(peer, peer.getStatisticsService(),
                peer.getSharedFilesService(), pongFactory,
                peer.getSecurityService(), peer.getHostService(),
                this, peer.getUdpService());
        peer.getUdpService().setUdpDataHandler(udpHandler);

        qrpUpdateTimer = new QRPUpdateTimer(peer.getSharedFilesService());
        Environment.getInstance().scheduleTimerTask(
                qrpUpdateTimer, QRPUpdateTimer.TIMER_PERIOD,
                QRPUpdateTimer.TIMER_PERIOD);

        Environment.getInstance().scheduleTimerTask(
                new ResetTCPRedirectCounter(), ResetTCPRedirectCounter.TIMER_PERIOD,
                ResetTCPRedirectCounter.TIMER_PERIOD);

        Environment.getInstance().scheduleTimerTask(
                new HopsFlowTimer(), HopsFlowTimer.TIMER_DELAY,
                HopsFlowTimer.TIMER_PERIOD);
    }

    //@EventTopicSubscriber(topic=PhexEventTopics.Host_Disconnect)
    public void onHostDisconnectEvent(String topic, Host host) {
        // remove routings of obsolete host.
        messageRouting.removeRoutings(host);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Message Dispatching
    ////////////////////////////////////////////////////////////////////////////

    public <T extends Message> void addMessageSubscriber(Class<T> clazz,
                                                         MessageSubscriber<T> subscriber) {
        messageDispatcher.addMessageSubscriber(clazz, subscriber);
    }

    public <T extends Message> void removeMessageSubscriber(Class<T> clazz,
                                                            MessageSubscriber<T> subscriber) {
        messageDispatcher.removeMessageSubscriber(clazz, subscriber);
    }

    public <T extends Message> void addUdpMessageSubscriber(Class<? extends Message> clazz,
                                                            UdpMessageSubscriber<T> subscriber) {
        messageDispatcher.addUdpMessageSubscriber(clazz, subscriber);
    }

    public <T extends Message> void removeUdpMessageSubscriber(Class<T> clazz,
                                                               UdpMessageSubscriber<T> subscriber) {
        messageDispatcher.removeUdpMessageSubscriber(clazz, subscriber);
    }

    protected void dispatchToUdpSubscribers(Message message, DestAddress sourceAddress)
            throws InvalidMessageException {
        messageDispatcher.dispatchToUdpSubscribers(message, sourceAddress);
    }

    public void dispatchMessage(Message message, Host sourceHost) {
        //Logger.logMessage( Logger.FINEST, Logger.NETWORK,
        //    "Received Header function: " + header.getPayload() );
        MsgHeader header = message.getHeader();
        switch (header.getPayload()) {
            case MsgHeader.PING_PAYLOAD:
                messageDispatcher.handlePing((PingMsg) message, sourceHost);
                break;

            case MsgHeader.PONG_PAYLOAD:
                messageDispatcher.handlePong((PongMsg) message, sourceHost);
                break;

            case MsgHeader.PUSH_PAYLOAD:
                messageDispatcher.handlePushRequest((PushRequestMsg) message, sourceHost);
                break;

            case MsgHeader.QUERY_PAYLOAD:
                messageDispatcher.handleQuery((QueryMsg) message, sourceHost);
                break;

            case MsgHeader.QUERY_HIT_PAYLOAD:
                messageDispatcher.handleQueryResponse((QueryResponseMsg) message, sourceHost);
                break;
            case MsgHeader.ROUTE_TABLE_UPDATE_PAYLOAD:
                messageDispatcher.handleRouteTableUpdate((RouteTableUpdateMsg) message,
                        sourceHost);
                break;
            case MsgHeader.VENDOR_MESSAGE_PAYLOAD:
            case MsgHeader.STANDARD_VENDOR_MESSAGE_PAYLOAD:
                messageDispatcher.handleVendorMessage((VendorMsg) message, sourceHost);
                break;
        }

    }

    public void dropMessage(MsgHeader header, byte[] body, String reason, Host sourceHost) {

        messageDispatcher.dropMessage(header, body, reason, sourceHost);
    }


    ////////////////////////////////////////////////////////////////////////////
    // Query Messages
    ////////////////////////////////////////////////////////////////////////////

    public void updateMyQueryRouting(QueryMsg queryMsg) {
        // add my own query to seen list.
        messageRouting.checkAndAddToQueryRoutingTable(queryMsg.getHeader().getMsgID(),
                Host.LOCAL_HOST);
    }

    public void forwardMyQueryToUltrapeers(QueryMsg msg) {
        queryMsgRoutingHandler.forwardQueryToUltrapeers(msg, null);
    }

    public void forwardQueryToLeaves(QueryMsg msg, Host fromHost) {
        queryMsgRoutingHandler.forwardQueryToLeaves(msg, fromHost);
    }


    ////////////////////////////////////////////////////////////////////////////
    // Push Messages
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Routes the {@link PushRequestMsg} to a host we forwarded a {@link QueryResponseMsg}
     * for or is known from a {@link PushProxyRequestVMsg}.
     *
     * @param message the message to route.
     * @return true if the routing was successful, false otherwise.
     */
    public boolean routePushMessage(PushRequestMsg message) {
        GUID clientGUID = message.getClientGUID();
        Host host = messageRouting.getPushRouting(clientGUID);
        if (host == null) {
            // no push route...
            logger.debug("No PUSH route for {}.", clientGUID);
            return false;
        }
        logger.debug("Push route for {} is {}", clientGUID, host);
        host.queueMessageToSend(message);
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Ping Messages
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Ping a host
     */
    public void pingHost(Host host) {
        pingHost(host, (byte) 1);
    }

    /**
     * Ping a host
     */
    public void pingHost(Host host, byte ttl) {
        // Send ping msg.
        PingMsg pingMsg = new PingMsg(ttl);
        messageRouting.checkAndAddToPingRoutingTable(pingMsg.getHeader().getMsgID(),
                Host.LOCAL_HOST);
        if (logger.isDebugEnabled()) {
            logger.debug("Queueing Ping: {} - {} - Host: {}", pingMsg, pingMsg.getHeader(), host);
        }
        host.queueMessageToSend(pingMsg);
    }

    /**
     * Sends a ping message with the given TTL to the given hosts.
     *
     * @param ttl   the TTL to use.
     * @param hosts the hosts to ping.
     */
    public void pingHosts(byte ttl, Host[] hosts) {
        PingMsg pingMsg = new PingMsg(ttl);
        messageRouting.checkAndAddToPingRoutingTable(pingMsg.getHeader().getMsgID(),
                Host.LOCAL_HOST);
        forwardPing(pingMsg, Host.LOCAL_HOST, hosts);
    }

    /**
     * Forwards a ping to the given hosts but never to the from Host.
     *
     * @param msg      the ping to forward
     * @param fromHost the host the ping came from.
     * @param hosts    the hosts to forward to.
     */
    private static void forwardPing(PingMsg msg, Host fromHost, Host[] hosts) {
        for (int i = 0; i < hosts.length; i++) {
            if (hosts[i] == fromHost) {
                continue;
            }
            hosts[i].queueMessageToSend(msg);
        }
    }

    /**
     * <p>Called to forward a Ping to all connected neighbors. This is only
     * done under special conditions.<br>
     * When we are in Leaf mode we hold connections to Ultrapeers (we are there
     * leaf) and usual peers therefore we:<br>
     * - Never broadcast a message coming from a Ultrapper.
     * - Never broadcast a message to a ultrapeer.<br>
     * This strategy is used to separate the broadcast traffic of the peer
     * network from the Ultrapeer/Leaf network and is essential for a correct
     * Ultrapeer proposal support.</p>
     *
     * @param msg the MsgPing to forward
     * @param fromHost the Host that originated this message
     *
     * @deprecated only not delete for documentation purposes how pongs are
     *             handled without PongCache
     */
// only not delete for documentation purposes how pongs are handled without PongCache
//    @Deprecated
//    public void forwardPing( PingMsg msg, Host fromHost, boolean isShieldedLeaf )
//    {
//        // Never broadcast a message coming from a ultrapeer when in leaf mode!
//        if ( isShieldedLeaf && fromHost != null
//            && fromHost.isLeafUltrapeerConnection() )
//        {
//            return;
//        }
//
//        Host[] hosts;
//
//        if ( !isShieldedLeaf )
//        {   // only forward to ultrapeers if I'm not a leaf.
//            hosts = hostsContainer.getUltrapeerConnections();
//            forwardPing(msg, fromHost, hosts);
//
//            // only forward to leafs if I'm not a leaf itself.
//            hosts = hostsContainer.getLeafConnections();
//            forwardPing(msg, fromHost, hosts);
//        }
//
//        // forward to usual peers.
//        hosts = hostsContainer.getPeerConnections();
//        forwardPing(msg, fromHost, hosts);
//    }

    ////////////////////////////////////////////////////////////////////////////
    // Pong Messages
    ////////////////////////////////////////////////////////////////////////////

    /**
     * @param pong
     * @see phex.common.PongCache#addPong(phex.msg.PongMsg)
     */
    public void addPongToCache(PongMsg pong) {
        pongCache.addPong(pong);
    }

    /**
     * @see phex.common.PongCache#getPongs()
     */
    public List<PongMsg> getCachedPongs() {
        return pongCache.getPongs();
    }

    ////////////////////////////////////////////////////////////////////////////
    // TCP Connect Back Messages
    ////////////////////////////////////////////////////////////////////////////


    public boolean requestTCPConnectBack() {
        DestAddress localAddress = peer.getLocalAddress();
        VendorMsg tcpConnectBack = new TCPConnectBackVMsg(localAddress.getPort());
        Host[] hosts = peer.getHostService().getUltrapeerConnections();
        int sentCount = 0;
        for (int i = 0; sentCount <= 5 && i < hosts.length; i++) {
            if (hosts[i].isTCPConnectBackSupported()) {
                hosts[i].queueMessageToSend(tcpConnectBack);
                sentCount++;
            }
        }
        return sentCount > 0;
    }

    public boolean isTCPRedirectAllowed() {
        return numberOfTCPRedirectsSent <= 3;
    }

    public void incNumberOfTCPRedirectsSent() {
        numberOfTCPRedirectsSent++;
    }

    /**
     * Returns the last sent query routing table that contains
     * my and my leafs entries.
     *
     * @return the last sent query routing table.
     */
    public QueryRoutingTable getLastSentQueryRoutingTable() {
        return lastSentQueryRoutingTable;
    }

    ////////////////////////////////////////////////////////////////////////////
    // HopsFlow Vendor Messages
    ////////////////////////////////////////////////////////////////////////////

    public void triggerQueryRoutingTableUpdate() {
        Environment.getInstance().executeOnThreadPool(qrpUpdateTimer,
                "TriggerQueryRoutingTableUpdate");
    }

    ////////////////////////////////////////////////////////////////////////////
    // QRT handling
    ////////////////////////////////////////////////////////////////////////////

    /**
     * This method sends a udp ping to the given host address
     */
    public void sendUdpPing(DestAddress address) {
        PingMsg udpPing = PingMsg.createUdpPingMsg(peer.isUltrapeer());
        udpHandler.sendUdpPing(udpPing, address);
//        logger.debug("Sent Udp Ping to" + address + " : " + udpPing + " with Scp Byte : " +
//                udpPing.getScpByte() != null ? String.valueOf(udpPing.getScpByte()[0]) : "null"
//        );
    }

    public void sendUdpMessageAcknowledgementVMsg(GUID id, int resultCount, byte[] securityToken,
                                                  DestAddress destination) {
        MessageAcknowledgementVMsg respMsg = new MessageAcknowledgementVMsg(
                id, resultCount, securityToken);
        udpHandler.sendMessageAcknowledgementVMsg(respMsg, destination);
    }

    private class ResetTCPRedirectCounter extends TimerTask {
        private static final long TIMER_PERIOD = 1000 * 60 * 15;

        @Override
        public void run() {
            try {
                numberOfTCPRedirectsSent = 0;
            } catch (Throwable th) {
                logger.error("ResetTCPRedirectCounter {}", th);
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    // UDP Messages
    ////////////////////////////////////////////////////////////////////////////

    private class HopsFlowTimer extends TimerTask {
        private static final long TIMER_DELAY = 1000 * 60 * 2;
        private static final long TIMER_PERIOD = 1000 * 15;

        private boolean lastBusyState = false;

        @Override
        public void run() {
            try {
                if (!peer.getHostService().isShieldedLeafNode()) {
                    return;
                }
                Host[] ultrapeers = peer.getHostService().getUltrapeerConnections();
                boolean isHostBusy = peer.isUploadLimitReached();

                byte hopsFlowLimit;
                if (isHostBusy) {
                    hopsFlowLimit = 0;
                } else {
                    hopsFlowLimit = 5;
                }
                HopsFlowVMsg msg = new HopsFlowVMsg(hopsFlowLimit);
                for (int i = 0; i < ultrapeers.length; i++) {
                    if (ultrapeers[i].isHopsFlowSupported()
                            && (isHostBusy != lastBusyState
                            || ultrapeers[i].getConnectionUpTime() < TIMER_PERIOD * 1.1)) {
                        ultrapeers[i].queueMessageToSend(msg);
                    }
                }
                lastBusyState = isHostBusy;
            } catch (Throwable th) {
                logger.error(th.toString(), th);
            }
        }
    }

    private class QRPUpdateTimer extends TimerTask {
        private static final long TIMER_PERIOD = 1000 * 10;
        private final SharedFilesService sharedFilesService;

        public QRPUpdateTimer(SharedFilesService sharedFilesService) {
            this.sharedFilesService = sharedFilesService;
        }

        @Override
        public void run() {
            try {
                sendQueryRoutingTable();
            } catch (Throwable th) {
                logger.error(th.toString(), th);
            }
        }

        /**
         * Sends the query routing table to all network connections that haven't
         * been updated for a while.
         */
        private void sendQueryRoutingTable() {
            boolean isUltrapeer = peer.isUltrapeer();
            // check if we are a shielded leaf node or a Ultrapeer.
            // Forwarding QRT is not wanted otherwise.
            if (!(peer.isShieldedLeafNode() || isUltrapeer)) {
                return;
            }

            Host[] hosts = peer.getHostService().getUltrapeerConnections();

            QueryRoutingTable shareQRT = sharedFilesService.getLocalRoutingTable();

            QueryRoutingTable currentTable = null;
            QueryRoutingTable lastSentTable;
            for (int i = 0; i < hosts.length; i++) {
                // first check if we are a UP or leaf supports QRP
                if (isUltrapeer) {
                    if (!hosts[i].isUPQueryRoutingSupported()) {
                        continue;
                    }
                } else {
                    if (!hosts[i].isQueryRoutingSupported()) {
                        continue;
                    }
                }

                if (!hosts[i].isQRTableUpdateRequired()) {
                    continue;
                }

                logger.debug("Updating QRTable for: {}", hosts[i]);
                if (currentTable == null) {// lazy initialize
                    currentTable = new QueryRoutingTable(shareQRT.getTableSize());
                    currentTable.aggregateToRouteTable(shareQRT);
                    QueryRoutingTable.fillQRTWithLeaves(currentTable, peer);
                    lastSentQueryRoutingTable = currentTable;
                }
                lastSentTable = hosts[i].getLastSentRoutingTable();

                Iterator<RouteTableUpdateMsg> msgIterator =
                        QueryRoutingTable.buildRouteTableUpdateMsgIterator(
                                currentTable, lastSentTable);
                RouteTableUpdateMsg msg;
                while (msgIterator.hasNext()) {
                    msg = msgIterator.next();
                    hosts[i].queueMessageToSend(msg);
                }
                // when setting the last sent routing table the next routing
                // table update time is set.
                hosts[i].setLastSentRoutingTable(currentTable);
            }
        }
    }
}