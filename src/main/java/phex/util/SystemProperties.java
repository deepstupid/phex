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
 *  $Id: SystemProperties.java 4509 2011-04-01 12:51:40Z gregork $
 */
package phex.util;

import phex.api.Phex;
import phex.prefs.core.ProxyPrefs;

import java.io.File;
import java.io.IOException;
import java.security.Security;

public class SystemProperties {

    public static final String PHEX_CONFIG_PATH_SYSPROP = "phex.config.path";
    public static final String PHEX_DOWNLOAD_PATH_SYSPROP = "phex.download.path";
    private volatile static File phexConfigRoot;
    private volatile static File phexDownloadsRoot;

    public static String HOME() {
        return System.getProperty("user.home");
    }

    /**
     * For a HTTPURLConnection java uses configured proxy settings.
     */
    public static void updateProxyProperties() {
        System.setProperty("http.agent", Phex.getFullPhexVendor());
        if (ProxyPrefs.UseHttp.get().booleanValue()) {
            System.setProperty("http.proxyHost", ProxyPrefs.HttpHost.get());
            System.setProperty("http.proxyPort", ProxyPrefs.HttpPort.get().toString());
        } else {
            System.setProperty("http.proxyHost", "");
            System.setProperty("http.proxyPort", "");
        }

        // cache DNS name lookups for only 30 minutes
        System.setProperty("networkaddress.cache.ttl", "1800");
        Security.setProperty("networkaddress.cache.ttl", "1800");
    }

    /**
     * Sets the directory into which Phex adds its configuration files. When
     * configRoot is null the directory is set to:<br>
     * {user.home}/phex on windows systems and<br>
     * {user.home}/.phex on unix and mac systems.
     *
     * @deprecated since Phex 3.0, drop support once 2.x is not in use anymore
     */
    @Deprecated
    private static File getOldPhexConfigRoot() {
        StringBuffer path = new StringBuffer(20);
        path.append(HOME());
        path.append(File.separator);

        //phex config files are hidden on all UNIX systems (also MacOSX. Since
        //there are many UNIX like operation systems with Java support out there,
        //we can not recognize the OS through it's name. Thus we check if the
        //root of the filesystem starts with "/" since only UNIX uses such
        //filesystem conventions
        if (File.separatorChar == '/') {
            path.append('.');
        }
        path.append("phex");
        File configRoot = new File(path.toString());
        return configRoot;
    }


    /**
     * Returns the full path to the Phex directory of the user's home. This
     * directory is plattform dependent.
     * - Unix: ~/.phex/
     * - OSX: ~/Library/Application Support/Phex/
     * - Windows: ..../Documents and Settings/username/Application Data/Phex/
     *
     * @throws RuntimeException in case creating a possible missing Phex config root fails
     *                          or the phexConfigRoot is not a directory.
     */
    public static File getPhexConfigRoot() {
        if (phexConfigRoot == null) {
            phexConfigRoot = initPhexConfigRoot();
        }
        if (phexConfigRoot.exists()) {
            if (!phexConfigRoot.isDirectory()) {
                throw new RuntimeException("Config location is not a directory: " + phexConfigRoot.getAbsolutePath());
            }
        } else {
            try {
                FileUtils.forceMkdir(phexConfigRoot);
            } catch (IOException exp) {
                throw new RuntimeException("Failed creating config directory: " + phexConfigRoot.getAbsolutePath());
            }
        }
        return phexConfigRoot;
    }

    private static File initPhexConfigRoot() {
        // to prevent problems wait with assigning userPath...
        String tmpUserPath = System.getProperty(PHEX_CONFIG_PATH_SYSPROP);
        if (StringUtils.isEmpty(tmpUserPath)) {
            if (SystemUtils.IS_OS_WINDOWS) {
                String appDataEnv = System.getenv("APPDATA");
                if (StringUtils.isEmpty(appDataEnv)) {
                    tmpUserPath = HOME() + File.separator + "Application Data";
                }
                tmpUserPath = appDataEnv + File.separator
                        + "Phex" + File.separator;
            } else if (SystemUtils.IS_OS_MAC_OSX) {
                tmpUserPath = HOME() + File.separator
                        + "Library" + File.separator + "Application Support";
                tmpUserPath = tmpUserPath + File.separator + "Phex" + File.separator;
            } else {
                tmpUserPath = HOME() + File.separator
                        + ".phex" + File.separator;
            }
        } else {
            if (!tmpUserPath.endsWith(File.separator)) {
                tmpUserPath = tmpUserPath + File.separator;
            }
        }
        File dir = new File(tmpUserPath);
        return dir;
    }


    /**
     * Returns the full path to the Phex directory of the user's home. This
     * directory is plattform dependent.
     * - Unix: ~/phex/
     * - OSX: ~/Library/Application Support/Phex/
     * - Windows: ..../Documents and Settings/username/Application Data/Phex/
     *
     * @throws RuntimeException in case creating a possible missing Phex config root fails
     *                          or the phexConfigRoot is not a directory.
     */
    public static File getPhexDownloadsRoot() {
        if (phexDownloadsRoot == null) {
            phexDownloadsRoot = initPhexDownloadsRoot();
        }
        if (phexDownloadsRoot.exists()) {
            if (!phexDownloadsRoot.isDirectory()) {
                throw new RuntimeException("Config location is not a directory: " + phexDownloadsRoot.getAbsolutePath());
            }
        }
        // Don't create directory here... it might be different in user configuration.
        return phexDownloadsRoot;
    }

    private static File initPhexDownloadsRoot() {
        // to prevent problems wait with assigning userPath...
        String tmpUserPath = System.getProperty(PHEX_DOWNLOAD_PATH_SYSPROP);
        if (StringUtils.isEmpty(tmpUserPath)) {
            if (SystemUtils.IS_OS_WINDOWS) {
                tmpUserPath = HOME() + File.separator + "Downloads" + File.separator + "Phex";
            } else if (SystemUtils.IS_OS_MAC_OSX) {
                tmpUserPath = HOME() + File.separator
                        + "Library" + File.separator + "Application Support";
                tmpUserPath = tmpUserPath + File.separator + "Phex" + File.separator;
            } else {
                tmpUserPath = HOME() + File.separator
                        + "phex" + File.separator;
            }
        } else {
            if (!tmpUserPath.endsWith(File.separator)) {
                tmpUserPath = tmpUserPath + File.separator;
            }
        }
        File dir = new File(tmpUserPath);
        return dir;
    }


}
