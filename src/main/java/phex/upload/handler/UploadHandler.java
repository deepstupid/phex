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
 *  --- SVN Information ---
 *  $Id: UploadHandler.java 4103 2008-01-05 21:55:36Z gregork $
 */
package phex.upload.handler;

import phex.http.HTTPRequest;
import phex.upload.UploadManager;
import phex.upload.UploadState;
import phex.upload.response.UploadResponse;

import java.io.IOException;

public interface UploadHandler {
    boolean isPersistentConnection();

    boolean isQueued();

    /**
     * Returns the earliest timestamp the connection is allowed to come back with the
     * next request attempt.
     */
    long getQueueMinNextPollTime();

    /**
     * Returns he maximum time in millis the connection can wait with the next
     * request before it times out.
     */
    int getQueueMaxNextPollTime();

    UploadResponse determineUploadResponse(HTTPRequest httpRequest, UploadState uploadState,
                                           UploadManager uploadMgr)
            throws IOException;
}
