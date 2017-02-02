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
 *  $Id:$
 */
package phex.api;

import phex.download.RemoteFile;
import phex.download.swarming.SWDownloadFile;

public class SearchResultItem {
    public static final short STATUS_UNDEF = -1;

    public static final short STATUS_TRANSFER_NOT_RUNNING = SWDownloadFile.TRANSFER_NOT_RUNNING;

    public static final short STATUS_TRANSFER_RUNNING = SWDownloadFile.TRANSFER_RUNNING;

    public static final short STATUS_TRANSFER_ERROR = SWDownloadFile.TRANSFER_ERROR;

    public static final short STATUS_TRANSFER_COMPLETED = SWDownloadFile.TRANSFER_COMPLETED;
    SWDownloadFile _swDownloadFile = null;
    private String _localFilepath = null;
    private RemoteFile _remoteFile = null;
    private long _id = -1;
    private SearchItem _searchItem = null;

    public SearchResultItem() {
    }

    public SearchResultItem(RemoteFile remoteFile) {
        _remoteFile = remoteFile;

        _localFilepath = null;
    }

    public String getLocalFilepath() {
        return _localFilepath;
    }

    public void setLocalFilepath(String localFilepath) {
        _localFilepath = localFilepath;
    }

    public short getStatus() {
        if (_swDownloadFile != null) {
            synchronized (_swDownloadFile) {
                return _swDownloadFile.getDataTransferStatus();
            }
        } else {
            return STATUS_TRANSFER_NOT_RUNNING;
        }
    }

    public RemoteFile getRemoteFile() {
        return _remoteFile;
    }

    public void setRemoteFile(RemoteFile remoteFile) {
        _remoteFile = remoteFile;
    }

    public long getId() {
        return _id;
    }

    public void setId(long id) {
        _id = id;
    }

    public SWDownloadFile getSWDownloadFile() {
        return _swDownloadFile;
    }

    public void setSWDownloadFile(SWDownloadFile swDownloadFile) {
        _swDownloadFile = swDownloadFile;
    }

    public SearchItem getSearchItem() {
        return _searchItem;
    }

    public void setSearchItem(SearchItem searchItem) {
        _searchItem = searchItem;
    }
}