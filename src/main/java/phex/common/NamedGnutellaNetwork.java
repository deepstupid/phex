/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2008 Phex Development Group
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
 *  $Id: NamedGnutellaNetwork.java 4254 2008-09-05 21:50:12Z gregork $
 */
package phex.common;

import phex.connection.ConnectionConstants;
import phex.peer.Peer;
import phex.util.FileUtils;

import java.io.File;

/**
 * The representation of a custom named Gnutella network.
 */
public class NamedGnutellaNetwork extends GnutellaNetwork {

    public final String networkName;

    public NamedGnutellaNetwork(Peer peer, String name) {
        super(peer);
        networkName = name;
    }

    public String getName() {
        return networkName;
    }

    /**
     * @see phex.common.GnutellaNetwork#getHostsFile()
     */
    public File getHostsFile() {
        String filename = "phex_" + networkName + ".hosts";
        filename = FileUtils.convertToLocalSystemFilename(filename);
        return peer.file(filename);
    }

    /**
     * @see phex.common.GnutellaNetwork#getFavoritesFile()
     */
    public File getFavoritesFile() {
        String filename = networkName + '_' + Peer.XML_FAVORITES_FILE_NAME;
        filename = FileUtils.convertToLocalSystemFilename(filename);
        return peer.file(filename);
    }

    /**
     *
     */
    public File getSearchFilterFile() {
        String filename = networkName + '_'
                + Peer.XML_SEARCH_FILTER_FILE_NAME;
        filename = FileUtils.convertToLocalSystemFilename(filename);
        return peer.file(filename);
    }

    /**
     * @see phex.common.GnutellaNetwork#getGWebCacheFile()
     */
    public File getGWebCacheFile() {
        String filename = networkName + '_' + Peer.G_WEB_CACHE_FILE_NAME;
        filename = FileUtils.convertToLocalSystemFilename(filename);
        return peer.file(filename);
    }

    /**
     * @see phex.common.GnutellaNetwork#getUdpHostCacheFile()
     */
    public File getUdpHostCacheFile() {
        String filename = networkName + '_' + Peer.UDP_HOST_CACHE_FILE_NAME;
        filename = FileUtils.convertToLocalSystemFilename(filename);
        return peer.file(filename);
    }

    public String getNetworkGreeting() {
        return ConnectionConstants.GNUTELLA_PHEX_CONNECT + ' ' + networkName;
    }
}
