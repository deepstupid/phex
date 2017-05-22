/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2005 Phex Development Group
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
 *  Created on 17.08.2006
 *  --- CVS Information ---
 *  $Id: peer.prefs.java 4562 2012-11-21 11:14:19Z gregork $
 */
package phex;

import phex.prefs.Preferences;
import phex.prefs.Setting;
import phex.util.SystemUtils;

import java.io.File;
import java.util.List;

public class NetworkPrefs extends Preferences {
    /**
     * Settings which have to be defined, before you can define your own ones...
     * DON'T CHANGE THESE!
     * ...
     * ...
     * DON'T CHANGE THESE AGAIN! :)
     */
    public final static String GENERAL_GNUTELLA_NETWORK = "<General Gnutella Network>";
    /**
     * The GUID of the servent.
     */
    public final  Setting<String> ServentGuid;
    /**
     * The listening port the server binds to.
     */
    public final  Setting<Integer> ListeningPort;
    /**
     * The name of the used gnutella network.
     */
    public final  Setting<String> CurrentNetwork;
    /**
     * The history of networks.
     */
    public final  Setting<List<String>> NetworkHistory;
    /**
     * Indicates if the node is connected to a Local Area Network (LAN). This
     * is used to indicate if there is a chance to access a local IP when
     * firewalled.
     */
    public final  Setting<Boolean> ConnectedToLAN;
    /**
     * The create socket default connect timeout.
     */
    public final  Setting<Integer> TcpConnectTimeout;
    /**
     * The sockets default read/write timeout.
     */
    public final  Setting<Integer> TcpRWTimeout;
    /**
     * The number of maximum concurrent connection attempts allowed.
     * (XP limits this to 10)
     */
    public final  Setting<Integer> MaxConcurrentConnectAttempts;
    /**
     * The max number of host that should be hold in HostCache.
     */
    public final  Setting<Integer> MaxHostInHostCache;
    /**
     * Automatically removes bad hosts from the connection container.
     */
    public final  Setting<Boolean> AutoRemoveBadHosts;
    /**
     * Indicates if the chat feature is enabled.
     */
    public final  Setting<Boolean> AllowChatConnection;
    /**
     * The default number of maximum concurrent connection attempts allowed on
     * a XP system. (XP limits this to 10, leave 2 for other process)
     */
    private final  int DEFAULT_MAX_CONCURRENT_CONNECT_ATTEMPTS_XP = 8;
    /**
     * The default number of maximum concurrent connection attempts allowed on
     * a other systems then XP. (XP limits this to 10, leave 2 for other process)
     */
    private final  int DEFAULT_MAX_CONCURRENT_CONNECT_ATTEMPTS_OTHERS = 15;

    public NetworkPrefs(String network, File file) {
        super(file);

        ServentGuid = createStringSetting(
                "Network.ServentGuid", "");

        ListeningPort = createListeningPortSetting(
                "Network.ListeningPort");

        CurrentNetwork = createStringSetting(
                "Network.CurrentNetwork",
                network);

        NetworkHistory = createListSetting(
                "Network.NetworkHistory");

        ConnectedToLAN = createBoolSetting(
                "Network.ConnectedToLAN", true);

        TcpConnectTimeout = createIntSetting(
                "Network.TcpConnectTimeout", 30 * 1000);

        TcpRWTimeout = createIntSetting(
                "Network.TcpRWTimeout", 60 * 1000);

        int maxConcConnectAtt;
        if (SystemUtils.IS_OS_WINDOWS_XP) {
            maxConcConnectAtt = DEFAULT_MAX_CONCURRENT_CONNECT_ATTEMPTS_XP;
        } else {
            maxConcConnectAtt = DEFAULT_MAX_CONCURRENT_CONNECT_ATTEMPTS_OTHERS;
        }
        MaxConcurrentConnectAttempts = createIntSetting(
                "Network.MaxConcurrentConnectAttempts", maxConcConnectAtt);

        MaxHostInHostCache = createIntSetting(
                "Network.MaxHostInHostCache", 2000);

        AutoRemoveBadHosts = createBoolSetting(
                "Network.AutoRemoveBadHosts", true);

        AllowChatConnection = createBoolSetting(
                "Network.AllowChatConnection", true);
    }
}
