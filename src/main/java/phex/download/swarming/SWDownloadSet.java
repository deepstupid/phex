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
 *  $Id: SWDownloadSet.java 4430 2009-04-18 10:22:57Z gregork $
 */
package phex.download.swarming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.download.DownloadScope;
import phex.peer.Peer;

/**
 * The download set is used to hold everything that is needed for a doing a swarm
 * download. This is the download file, the download segment and the download
 * candidate.
 */
public class SWDownloadSet {
    private static final Logger logger = LoggerFactory.getLogger(SWDownloadSet.class);

    public final Peer peer;
    public final SWDownloadFile downloadFile;
    public final SWDownloadCandidate downloadCandidate;
    private DownloadScope downloadScope;
    private SWDownloadSegment downloadSegment;


    public SWDownloadSet(Peer peer, SWDownloadFile aDownloadFile,
                         SWDownloadCandidate aDownloadCandidate) {
        this.peer = peer;
        downloadFile = aDownloadFile;
        downloadCandidate = aDownloadCandidate;
        downloadScope = null;
    }

    public SWDownloadSegment allocateSegment() {
        logger.debug("Allocate segment on set {}", this);
        if (downloadScope == null) {
            downloadScope = downloadFile.allocateDownloadScope(downloadCandidate);

            if (downloadScope != null) {
                if (downloadScope.getEnd() == Long.MAX_VALUE) {
                    downloadSegment = new SWDownloadSegment(downloadFile,
                            downloadScope.getStart(), SWDownloadConstants.UNKNOWN_FILE_SIZE);
                } else {
                    downloadSegment = new SWDownloadSegment(downloadFile,
                            downloadScope.getStart(), downloadScope.getLength());
                }
                downloadCandidate.associateDownloadSegment(downloadSegment);
            }
            // sanity check to make sure!
            logger.debug("Allocated segment: {} on set {}", downloadSegment, this);
        }
        if (downloadSegment == null) {
            logger.debug("No segment found to allocate");
            return null;
        }
        downloadCandidate.addToCandidateLog("Allocated segment: " + downloadSegment + " - " + downloadScope);
        return downloadSegment;
    }

    public SWDownloadSegment getDownloadSegment() {
        return downloadSegment;
    }

    /**
     * Releases a allocated download segment.
     */
    public void releaseDownloadSegment() {
        if (downloadSegment != null) {
            logger.debug("Release file download segment: {} on set {}", downloadSegment, this);
            downloadFile.releaseDownloadScope(downloadScope,
                    downloadSegment.getTransferredDataSize(), downloadCandidate);
            downloadCandidate.addToCandidateLog("Release segment: " + downloadSegment
                    + " - " + downloadScope);
            downloadSegment = null;
            downloadScope = null;
        }
        logger.debug("Release candidate download segment on set {}", this);
        downloadCandidate.releaseDownloadSegment();
    }

    /**
     * Releases a allocated download set.
     */
    public void releaseDownloadSet() {
        logger.debug("Release download set on set {}", this);
        releaseDownloadSegment();
        downloadFile.releaseDownloadCandidate(downloadCandidate);
        downloadFile.decrementWorkerCount();
    }

    @Override
    public String toString() {
        return "[DownloadSet@" + Integer.toHexString(hashCode()) + ": (Segment: " + downloadSegment + " - Candidate: "
                + downloadCandidate + " - File: " + downloadFile + ")]";
    }
}