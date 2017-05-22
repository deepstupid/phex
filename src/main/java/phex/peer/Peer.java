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
 *  $Id: Servent.java 4377 2009-02-21 20:46:52Z gregork $
 */
package phex.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.*;
import phex.chat.ChatService;
import phex.common.*;
import phex.common.address.DestAddress;
import phex.common.bandwidth.BandwidthManager;
import phex.common.file.FileManager;
import phex.download.swarming.SwarmingManager;
import phex.host.DefaultHostFetchingStrategy;
import phex.host.HostFetchingStrategy;
import phex.host.HostManager;
import phex.host.UltrapeerCapabilityChecker;
import phex.msg.GUID;
import phex.msghandling.MessageService;
import phex.net.OnlineObserver;
import phex.net.UdpService;
import phex.net.server.OIOServer;
import phex.net.server.Server;
import phex.query.QueryManager;
import phex.security.PhexSecurityManager;
import phex.share.SharedFilesService;
import phex.statistic.StatisticsManager;
import phex.upload.UploadManager;
import phex.util.StringUtils;
import phex.util.SystemProperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Peer extends AbstractLifeCycle implements PeerInfo {

    public static final String ERROR_LOG_FILE_NAME = "phex.error.log";
    public static final String AUTOCONNECT_HOSTS_FILE_NAME = "autoconnecthosts.cfg";
    public static final String G_WEB_CACHE_FILE_NAME = "gwebcache.cfg";
    public static final String UDP_HOST_CACHE_FILE_NAME = "udphostcache.cfg";
    public static final String HOSTS_FILE_NAME = "phex.hosts";
    public static final String XML_DOWNLOAD_FILE_NAME = "phexdownload.xml";
    public static final String XML_RESEARCH_SERVICE_FILE_NAME = "phexresearch.xml";
    public static final String XML_GUI_SETTINGS_FILE_NAME = "phexgui.xml";
    public static final String XML_SECURITY_FILE_NAME = "security.xml";
    public static final String XML_SHARED_LIBRARY_FILE_NAME = "sharedlibrary.xml";
    public static final String XML_FILTER_LIST_FILE_NAME = "filterlist.xml";
    public static final String XML_FAVORITES_FILE_NAME = "favorites.xml";
    public static final String XML_SEARCH_FILTER_FILE_NAME = "searchfilters.xml";
    public static final String UPLOAD_PREFS_FILE_NAME = "upload.cfg";

    public static final String NETWORK_PREFERENCES_FILE_NAME = "phexCorePrefs.properties";
    public static final String MESSAGE_PREFS_FILE = "message.cfg";

    public static final String GUI_PREFERENCES_FILE_NAME = "phexGuiPrefs.properties";
    public static final String LOG_FILE_NAME = "phex.log";

    private static final Logger logger = LoggerFactory.getLogger(Peer.class);
    private static final String CONNECTION_PREFS_FILE = "connection.cfg";
    private static final String DOWNLOAD_PREFS_FILE = "download.cfg";


    private final List<LifeCycle> dependentLifeCycles;
    private final Server server;
    private final UdpService udpService;
    // hold reference to not loose to GC.
    @SuppressWarnings("unused")
    private final OnlineObserver onlineObserver;
    private final HostFetchingStrategy hostFetchingStrategy;
    private final ChatService chatService;
    private final MessageService messageService;
    private final UploadManager uploadService;
    private final QueryManager queryService;
    private final HostManager hostService;
    private final PhexSecurityManager securityService;
    private final SharedFilesService sharedFilesService;
    private final BandwidthManager bandwidthService;
    private final StatisticsManager statisticsService;
    private final SwarmingManager downloadService;

    private final File home;


    /**
     * The GUID of the servent. Derived from prefs.ServentGuid
     */
    private GUID serventGuid;
    /**
     * The Gnutella Network configuration and settings separation class.
     */
    private GnutellaNetwork gnutellaNetwork;
    /**
     * The online status of the servent.
     */
    private OnlineStatus onlineStatus;
    private UltrapeerCapabilityChecker upChecker;
    public FileManager files;

    public final NetworkPrefs netPrefs;
    public final ConnectionPrefs connectionPrefs;
    public final DownloadPrefs downloadPrefs;
    public final MessagePrefs messagePrefs;
    public final BandwidthPrefs bandwidthPrefs;
    public final ProxyPrefs proxyPrefs;
    public final LibraryPrefs libPrefs;
    public final SubscriptionPrefs subPrefs;
    public final StatisticPrefs statPrefs;
    public final UploadPrefs uploadPrefs;

    public Peer() {
        this(NetworkPrefs.GENERAL_GNUTELLA_NETWORK, SystemProperties.getPhexConfigRoot());
    }

    public Peer(String network, File home) {
        this.home = home;
        
        this.netPrefs = new NetworkPrefs(network, file(NETWORK_PREFERENCES_FILE_NAME));
        this.connectionPrefs = new ConnectionPrefs(file(CONNECTION_PREFS_FILE));
        this.downloadPrefs = new DownloadPrefs(file(DOWNLOAD_PREFS_FILE));
        this.messagePrefs = new MessagePrefs(file("message.cfg"));
        this.bandwidthPrefs = new BandwidthPrefs(file("bandwidth.cfg"));
        this.proxyPrefs  = new ProxyPrefs(file("proxy.cfg"));
        this.libPrefs  = new LibraryPrefs(file("lib.cfg"));
        this.subPrefs  = new SubscriptionPrefs(file("sub.cfg"));
        this.statPrefs  = new StatisticPrefs(file("stat.cfg"));
        this.uploadPrefs = new UploadPrefs(file("upload.cfg"));

        this.files = new FileManager();
        dependentLifeCycles = new ArrayList<LifeCycle>();

        String serventGuidStr = netPrefs.ServentGuid.get();
        if (StringUtils.isEmpty(serventGuidStr)) {
            serventGuid = new GUID();
            netPrefs.ServentGuid.set(serventGuid.toHexString());
        } else {
            try {
                serventGuid = new GUID(serventGuidStr);
            } catch (Exception exp) {
                logger.warn("{}", exp);
                serventGuid = new GUID();
                netPrefs.ServentGuid.set(serventGuid.toHexString());
            }
        }


        // TODO find a better way to apply servent settings...

        String networkName = netPrefs.CurrentNetwork.get();
        gnutellaNetwork = GnutellaNetwork.getGnutellaNetworkFromString(this, networkName);

        if (connectionPrefs.AutoConnectOnStartup.get().booleanValue()) {
            setOnlineStatus(OnlineStatus.ONLINE);
        } else {
            setOnlineStatus(OnlineStatus.OFFLINE);
        }

        securityService = new PhexSecurityManager();
        dependentLifeCycles.add(securityService);

        sharedFilesService = new SharedFilesService(this);
        dependentLifeCycles.add(sharedFilesService);

        downloadService = new SwarmingManager(this, sharedFilesService);
        dependentLifeCycles.add(downloadService);

        bandwidthService = new BandwidthManager(this);

        chatService = new ChatService(this);

        uploadService = new UploadManager(this);

        statisticsService = new StatisticsManager(this);
        dependentLifeCycles.add(statisticsService);

        hostService = new HostManager(this, true);
        dependentLifeCycles.add(hostService);

        messageService = new MessageService(hostService.getNetworkHostsContainer(),
                hostService.getCaughtHostsContainer(), hostService.getUhcContainer(),
                securityService, this);
        dependentLifeCycles.add(messageService);

        queryService = new QueryManager(messageService, this);
        dependentLifeCycles.add(queryService);

        hostFetchingStrategy = new DefaultHostFetchingStrategy(this,
                hostService.getUhcContainer());

        onlineObserver = new OnlineObserver(this, hostFetchingStrategy);

        server = new OIOServer(this);//new JettyServer();

        udpService = new UdpService(netPrefs.ListeningPort.get().intValue());
    }


    public File file(String file) {
        return Environment.getPhexConfigFile(home, file);
    }

    @Override
    protected void doStart() throws Exception {
        MultipleException multiExp = new MultipleException();

        hostFetchingStrategy.postManagerInitRoutine();

        try {
            server.startup();
        } catch (IOException exp) {
            logger.error("{}", exp);
        }

        try {
            udpService.startup();
        } catch (IOException exp) {
            logger.error("{}", exp);
        }

        upChecker = new UltrapeerCapabilityChecker(this, statisticsService);

        if (!dependentLifeCycles.isEmpty()) {
            // start dependent life cycles from end to start..
            ListIterator<LifeCycle> iterator = dependentLifeCycles.listIterator(dependentLifeCycles.size());
            while (iterator.hasPrevious()) {
                try {
                    iterator.previous().start();
                } catch (Throwable e) {
                    multiExp.add(e);
                }
            }
        }

        multiExp.throwPossibleExp();
    }

    @Override
    protected void doStop() throws Exception {
        MultipleException multiExp = new MultipleException();

        server.shutdown(false);
        udpService.shutdown();

        if (!dependentLifeCycles.isEmpty()) {
            // stop dependent life cycles from end to start..
            ListIterator<LifeCycle> iterator = dependentLifeCycles.listIterator(dependentLifeCycles.size());
            while (iterator.hasPrevious()) {
                try {
                    iterator.previous().stop();
                } catch (Throwable e) {
                    multiExp.add(e);
                }
            }
        }

        multiExp.throwPossibleExp();
    }

    /**
     * @return the udpService
     */
    public UdpService getUdpService() {
        return udpService;
    }

    /**
     * Returns the chat service of this servent.
     *
     * @return the chat service.
     */
    public ChatService getChatService() {
        return chatService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public HostManager getHostService() {
        return hostService;
    }

    public UploadManager getUploadService() {
        return uploadService;
    }

    public QueryManager getQueryService() {
        return queryService;
    }

    public SharedFilesService getSharedFilesService() {
        return sharedFilesService;
    }

    public PhexSecurityManager getSecurityService() {
        return securityService;
    }

    /**
     * @return the bandwidthService
     */
    public BandwidthManager getBandwidthService() {
        return bandwidthService;
    }

    /**
     * Returns the StatisticsService that provides access to the
     * Phex statistics system.
     *
     * @return the statistics service.
     */
    public StatisticsManager getStatisticsService() {
        return statisticsService;
    }

    /**
     * Returns the DownloadService that provides access to the
     * Phex download system.
     *
     * @return the download service.
     */
    public SwarmingManager getDownloadService() {
        return downloadService;
    }

    /**
     * Returns true if this node is currently a ultrapeer, false otherwise.
     * This node is currently a ultrapeer if it is forced to be a ultrapeer or
     * has leaf connections.
     *
     * @return true if the node is currently a ultrapeer, false otherwise.
     */
    public boolean isUltrapeer() {
        return hostService.isUltrapeer();
    }

    /**
     * Returns true if the local servent is a shielded leaf node ( has a connection
     * to a ultrapeer as a leaf).
     */
    public boolean isShieldedLeafNode() {
        return hostService.isShieldedLeafNode();
    }

    /**
     * Returns true if this node is allowed to become a ultrapeer, false otherwise.
     *
     * @return true if this node is allowed to become a ultrapeer, false otherwise.
     */
    public boolean isAbleToBecomeUltrapeer() {
        // when we already are a ultrapeer we must be able to become one..
        if (isUltrapeer()) {
            return true;
        }
        return !isShieldedLeafNode() && (connectionPrefs.AllowToBecomeUP.get().booleanValue() &&
                upChecker.isUltrapeerCapable());
    }

    /**
     * The method checks if we are able to go into leaf state. This is
     * necessary to react accordingly to the "X-UltrapeerNeeded: false" header.
     *
     * @return true if we are able to switch to Leaf state, false otherwise.
     */
    public boolean allowDowngradeToLeaf() {
        return !upChecker.isPerfectUltrapeer() && hostService.allowDowngradeToLeaf();
    }

    public void upgradeToUltrapeer() {
        if (!isShieldedLeafNode()) {
            return;
        }
        logger.debug("Upgrading to ultrapeer..");
        OnlineStatus oldStatus = onlineStatus;
        setOnlineStatus(OnlineStatus.OFFLINE);
        setOnlineStatus(oldStatus);
    }

    /**
     * Indicates if this server is currently firewalled or assumed to be firewalled.
     * To determine this the server is asked if it has received incoming
     * connections yet (e.g. from TCPConnectBack)
     *
     * @return true if it has connected incoming, false otherwise.
     */
    public boolean isFirewalled() {
        return !server.hasConnectedIncoming();
    }

    /**
     * Indicates if we are a udp host cache.
     */
    public static boolean isUdpHostCache() {
        // TODO implement logic to determine if we are udp host cache capable.
        return false;
    }

    /**
     * Indicates if this servent has reached its upload limit, all
     * upload slots are full.
     *
     * @return true if the upload limit is reached, false otherwise.
     */
    public boolean isUploadLimitReached() {
        return uploadService.isHostBusy();
    }

    /**
     * Returns the current local address. This will be the forced address
     * in case a forced address is set.
     *
     * @return the current determined local address or the user set forced address.
     */
    public DestAddress getLocalAddress() {
        return server.getLocalAddress();
    }

    /**
     * Updates the local address of the servent. In case a forced address is
     * set any call of this method will be ignored.
     * A PhexEventTopics.Servent_LocalAddress event topic will be fired in
     * case the address has changed.
     *
     * @param newAddress the new address.
     */
    public void updateLocalAddress(DestAddress newAddress) {
        server.updateLocalAddress(newAddress);
    }

    /**
     * Triggers a restart of the servents server.
     *
     * @throws IOException
     */
    public void restartServer() throws IOException {
        server.restart();
    }

    /**
     * Returns the GUID of the servent.
     *
     * @return the GUID of the servent.
     */
    public GUID getServentGuid() {
        return serventGuid;
    }

    public OnlineStatus getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(OnlineStatus newStatus) {
        if (newStatus == onlineStatus) {
            return;
        }
        //OnlineStatus oldStatus = onlineStatus;
        onlineStatus = newStatus;
//        Phex.getEventService().publish( PhexEventTopics.Servent_OnlineStatus,
//            new ChangeEvent( this, oldStatus, newStatus ) );
    }

    /**
     * Returns the current network.
     *
     * @return the current network.
     */
    public GnutellaNetwork getGnutellaNetwork() {
        return gnutellaNetwork;
    }

    /**
     * Switching the GnutellaNetwork causes the servent to go into OFFLINE
     * status before switching the network and back to the former status,
     * after switching the network.
     *
     * @param network the new GnutellaNetwork.
     */
    public void setGnutellaNetwork(GnutellaNetwork network) {
        OnlineStatus oldStatus = onlineStatus;
        setOnlineStatus(OnlineStatus.OFFLINE);

        GnutellaNetwork oldNetwork = gnutellaNetwork;
        gnutellaNetwork = network;

        setOnlineStatus(oldStatus);
    }

    /**
     * Returns the HostFetchingStrategy this servent uses to
     * find more hosts.
     *
     * @return the HostFetchingStrategy of this servent.
     */
    public HostFetchingStrategy getHostFetchingStrategy() {
        return hostFetchingStrategy;
    }


}