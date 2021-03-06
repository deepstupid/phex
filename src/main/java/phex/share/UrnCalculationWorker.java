/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2007 Phex Development Group
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
 *  Created on 13.12.2004
 *  --- CVS Information ---
 *  $Id: UrnCalculationWorker.java 4226 2008-07-13 10:03:45Z gregork $
 */
package phex.share;

import phex.common.AltLocContainer;
import phex.common.URN;
import phex.common.log.NLogger;
import phex.download.swarming.SWDownloadFile;
import phex.download.swarming.SwarmingManager;
import phex.LibraryPrefs;
import phex.util.IOUtil;
import phex.util.bitzi.Base32;
import phex.util.bitzi.SHA1;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

class UrnCalculationWorker implements Runnable {
    private final SwarmingManager downloadService;
    private final SharedFilesService sharing;
    private final ShareFile shareFile;

    UrnCalculationWorker(ShareFile shareFile, SharedFilesService sharing) {
        this.shareFile = shareFile;
        this.sharing = sharing;
        this.downloadService = sharing.peer.getDownloadService();
    }

    public void run() {
        boolean succ = calculateURN();
        // if calculation succeed
        if (succ) {
            // add the urn to the map to share by urn
            sharing.addUrn2FileMapping(shareFile);
            sharing.triggerSaveSharedFiles();
        }
    }

    /**
     * Calculates the URN of the file for HUGE support. This method can take
     * some time for large files. For URN calculation a SHA-1 digest is created
     * over the complete file and the SHA-1 digest is translated into a Base32
     * representation.
     */
    private boolean calculateURN() {
        int urnCalculationMode = sharing.peer.libPrefs.UrnCalculationMode.get().intValue();
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(shareFile.getSystemFile());
            MessageDigest messageDigest =
                    //new SHA1();
                    SHA1.get();

            byte[] buffer = new byte[64 * 1024];
            int length;
            long start = System.currentTimeMillis();
            long start2 = System.currentTimeMillis();
            while ((length = inStream.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, length);
                long end2 = System.currentTimeMillis();
                try {
                    Thread.sleep((end2 - start2) * urnCalculationMode);
                } catch (InterruptedException exp) {
                    // reset interrupted flag
                    Thread.currentThread().interrupt();
                    return false;
                }
                start2 = System.currentTimeMillis();
            }
            inStream.close();
            byte[] shaDigest = messageDigest.digest();
            long end = System.currentTimeMillis();
            URN urn = new URN("urn:sha1:" + Base32.encode(shaDigest));
            shareFile.setURN(urn);
            if (NLogger.isDebugEnabled(UrnCalculationWorker.class)) {
                NLogger.debug(UrnCalculationWorker.class, "SHA1 time: "
                        + (end - start) + " size: " + shareFile.getSystemFile().length());
            }

            // check if we find a download with the same urn and capture alt locs
            // from it
            SWDownloadFile file = downloadService.getDownloadFileByURN(urn);
            if (file != null) {
                AltLocContainer altCont = file.getGoodAltLocContainer();
                shareFile.getAltLocContainer().addContainer(altCont);
            }

            return true;
        } catch (IOException exp) {// dont care... no urn could be calculated...
            NLogger.debug(UrnCalculationWorker.class, exp, exp);
            return false;
        } finally {
            IOUtil.closeQuietly(inStream);
        }
    }
}