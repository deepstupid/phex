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
 *  Created on 18.09.2006
 *  --- CVS Information ---
 *  $Id: ProxyPrefs.java 3807 2007-05-19 17:06:46Z gregork $
 */
package phex;

import phex.prefs.Preferences;
import phex.prefs.Setting;

import java.io.File;

public class ProxyPrefs extends Preferences {
    public final int DEFAULT_HTTP_PORT = 80;
    public final int DEFAULT_SOCKS5_PORT = 1080;

    public final Setting<String> ForcedIp;

    public final Setting<Boolean> UseSocks5;
    public final Setting<String> Socks5Host;
    public final Setting<Integer> Socks5Port;
    public final Setting<Boolean> Socks5Authentication;
    public final Setting<String> Socks5User;
    public final Setting<String> Socks5Password;

    /**
     * Defines if a http proxy is used for HTTP connections (not Gnutella
     * connections.
     */
    public final Setting<Boolean> UseHttp;

    /**
     * Defines the name of the http proxy host.
     */
    public final Setting<String> HttpHost;

    /**
     * Defines the port of the http proxy host.
     */
    public final Setting<Integer> HttpPort;

    public ProxyPrefs(File file) { super(file);
        ForcedIp = createStringSetting(
                "Proxy.ForcedIp", "");
        UseSocks5 = createBoolSetting(
                "Proxy.UseSocks5", false);
        Socks5Host = createStringSetting(
                "Proxy.Socks5Host", "");
        Socks5Port = createIntSetting(
                "Proxy.Socks5Port", DEFAULT_SOCKS5_PORT);
        Socks5Authentication = createBoolSetting(
                "Proxy.Socks5Authentication", false);
        Socks5User = createStringSetting(
                "Proxy.Socks5User", "");
        Socks5Password = createStringSetting(
                "Proxy.Socks5Password", "");

        UseHttp = createBoolSetting(
                "Proxy.UseHttp", false);
        HttpHost = createStringSetting(
                "Proxy.HttpHost", "");
        HttpPort = createIntSetting(
                "Proxy.HttpPort", DEFAULT_HTTP_PORT);
    }
}
