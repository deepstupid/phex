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
 *  $Id: GeneralGnutellaNetwork.java 4420 2009-03-28 16:21:30Z gregork $
 */
package phex.common;

import phex.connection.ConnectionConstants;
import phex.peer.Peer;

import java.io.File;

/**
 * The representation of the general Gnutella network.
 */
public class GeneralGnutellaNetwork extends GnutellaNetwork {

    public GeneralGnutellaNetwork(Peer peer) {
        super(peer);
    }

    @Override
    public String getName() {
        return peer.netPrefs.GENERAL_GNUTELLA_NETWORK;
    }

    /**
     * @see phex.common.GnutellaNetwork#getHostsFile()
     */
    @Override
    public File getHostsFile() {
        return peer.file(
                Peer.HOSTS_FILE_NAME);
    }

    /**
     * @see phex.common.GnutellaNetwork#getBookmarkedHostsFile()
     */
    @Override
    public File getFavoritesFile() {
        return peer.file(
                Peer.XML_FAVORITES_FILE_NAME);
    }

    /**
     *
     */
    @Override
    public File getSearchFilterFile() {
        return peer.file(
                Peer.XML_SEARCH_FILTER_FILE_NAME);
    }

    /**
     * @see phex.common.GnutellaNetwork#getGWebCacheFile()
     */
    @Override
    public File getGWebCacheFile() {
        return peer.file(
                Peer.G_WEB_CACHE_FILE_NAME);
    }

    /**
     * @see phex.common.GnutellaNetwork#getUdpHostCacheFile()
     */
    @Override
    public File getUdpHostCacheFile() {
        return peer.file(
                Peer.UDP_HOST_CACHE_FILE_NAME);
    }

    @Override
    public String getNetworkGreeting() {
        return ConnectionConstants.GNUTELLA_CONNECT;
    }
}
