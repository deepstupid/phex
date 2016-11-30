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
import phex.prefs.core.NetworkPrefs;

import java.io.File;

/**
 * The representation of the general Gnutella network.
 */
public class GeneralGnutellaNetwork extends GnutellaNetwork
{    
    @Override
    public String getName()
    {
        return NetworkPrefs.GENERAL_GNUTELLA_NETWORK;
    }
    
    /**
     * @see phex.common.GnutellaNetwork#getHostsFile()
     */
    @Override
    public File getHostsFile()
    {
        return Environment.getPhexConfigFile(
            EnvironmentConstants.HOSTS_FILE_NAME );
    }
    
    /**
     * @see phex.common.GnutellaNetwork#getBookmarkedHostsFile()
     */
    @Override
    public File getFavoritesFile()
    {
        return Environment.getPhexConfigFile(
            EnvironmentConstants.XML_FAVORITES_FILE_NAME );
    }
    
    /**
     *
     */
    @Override
    public File getSearchFilterFile()
    {
        return Environment.getPhexConfigFile(
            EnvironmentConstants.XML_SEARCH_FILTER_FILE_NAME );
    }

    /**
     * @see phex.common.GnutellaNetwork#getGWebCacheFile()
     */
    @Override
    public File getGWebCacheFile()
    {
        return Environment.getPhexConfigFile(
            EnvironmentConstants.G_WEB_CACHE_FILE_NAME );
    }
    
    /**
     * @see phex.common.GnutellaNetwork#getUdpHostCacheFile()
     */
    @Override
    public File getUdpHostCacheFile()
    {
        return Environment.getPhexConfigFile(
            EnvironmentConstants.UDP_HOST_CACHE_FILE_NAME );
    }
    
    @Override
    public String getNetworkGreeting()
    {
        return ConnectionConstants.GNUTELLA_CONNECT;
    }
}
