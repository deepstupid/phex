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
 *  $Id: SubscriptionDownloader.java 4523 2011-06-22 09:27:23Z gregork $
 */
package phex.util;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.common.Environment;
import phex.SubscriptionPrefs;
import phex.peer.Peer;
import phex.share.FileRescanRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class SubscriptionDownloader extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionDownloader.class);
    private final Peer peer;

    public SubscriptionDownloader(Peer peer) {
        this.peer = peer;
        Environment.getInstance().scheduleTimerTask(this, 0, 14 * 24 * 3600 * 1000);
    }

    private List<String> loadSubscriptionList() {
        String name = "/subscription.list";
        InputStream stream = SubscriptionDownloader.class
                .getResourceAsStream(name);

        // TODO verify the code inside this if{}... is this really correct what
        // happens here?? looks strange...
        if (stream == null) {
            List<String> subscriptionMagnets = peer.subPrefs.SubscriptionMagnets.get();
            List<String> list = peer.subPrefs.SubscriptionMagnets.get();
            if (subscriptionMagnets != null
                    && peer.subPrefs.default_subscriptionMagnets != null) {
                subscriptionMagnets.add(peer.subPrefs.default_subscriptionMagnets);
            }
            return list;
        }
        try {
            // make sure it is buffered
            InputStreamReader input = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(input);
            List<String> list = new ArrayList<String>();
            String line = reader.readLine();
            while (line != null) {
                list.add(line);
                line = reader.readLine();
            }
            List<String> oldSubscriptionMagnets = peer.subPrefs.SubscriptionMagnets.get();
            list.addAll(oldSubscriptionMagnets);
            return list;
        } catch (IOException exp) {
            logger.warn(exp.toString(), exp);
        } finally {
            IOUtil.closeQuietly(stream);
        }
        return Collections.emptyList();
    }

    public void createDownload(String uriStr) throws URIException {
        if (uriStr.length() == 0) {
            return;
        }
        URI uri = new URI(uriStr, true);
        peer.getDownloadService().addFileToDownload(uri, true);
    }

    @Override
    public void run() {
        try {
            List<String> subscriptionMagnets = loadSubscriptionList();
            String uriStr;
            Iterator<String> iterator = subscriptionMagnets.iterator();

            // Sync subscription operation with a possible rescan process, this
            // prevents downloads of files already existing but not yet scanned.
            FileRescanRunner.sync();

            while (iterator.hasNext()) {
                uriStr = iterator.next();
                if (peer.subPrefs.DownloadSilently.get().booleanValue()) {
                    try {
                        createDownload(uriStr);
                    } //This donwloads the magma via magnet silently in the background.
                    catch (URIException exp) {
                        logger.error(exp.getMessage(), exp);
                    }
                } else {


                }
            }
        } catch (Throwable th) {
            logger.error(th.toString(), th);
        }
    }
}
