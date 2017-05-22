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
 *  $Id: ThexUploadHandler.java 4046 2007-11-19 17:13:59Z gregork $
 */
package phex.upload.handler;

import phex.common.URN;
import phex.http.GnutellaHeaderNames;
import phex.http.HTTPHeader;
import phex.http.HTTPHeaderNames;
import phex.http.HTTPRequest;
import phex.share.ShareFile;
import phex.share.SharedFilesService;
import phex.upload.UploadState;
import phex.upload.response.ThexUploadResponse;
import phex.upload.response.UploadResponse;

import java.io.IOException;

public class ThexUploadHandler extends AbstractUploadHandler {

    public ThexUploadHandler(SharedFilesService sharedFilesService) {
        super(sharedFilesService);
    }

    protected UploadResponse determineFailFastResponse(HTTPRequest httpRequest,
                                                       UploadState uploadState, ShareFile requestedFile) {
        return null;
    }


    public UploadResponse finalizeUploadResponse(HTTPRequest httpRequest,
                                                 UploadState uploadState, ShareFile requestedFile)
            throws IOException {
        uploadState.setFileName(requestedFile.getFileName());

        // form ok response...

        ThexUploadResponse response = new ThexUploadResponse(requestedFile,
                sharing);

        response.addHttpHeader(new HTTPHeader(HTTPHeaderNames.CONTENT_TYPE,
                "application/dime"));

        response.addHttpHeader(new HTTPHeader(HTTPHeaderNames.CONTENT_LENGTH,
                String.valueOf(response.remainingBody())));

        URN sharedFileURN = requestedFile.getURN();
        if (sharedFileURN != null) {
            response.addHttpHeader(new HTTPHeader(
                    GnutellaHeaderNames.X_GNUTELLA_CONTENT_URN, sharedFileURN
                    .getAsString()));
        }

        return response;
    }
}
