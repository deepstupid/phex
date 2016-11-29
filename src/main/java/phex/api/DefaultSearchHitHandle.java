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

public class DefaultSearchHitHandle implements ISearchHitHandle
{
    private IPhexDriver _phexDriver = null;

    private long _id = -1;

    DefaultSearchHitHandle(long id)
    {
        _id = id;

        _phexDriver = DefaultPhexDriver.getInstance();
    }

    public short getStatus()
    {
        if (_phexDriver != null)
        {
            return _phexDriver.getSearchHitStatus(_id);
        }
        else
        {
            return ISearchHitHandle.STATUS_UNDEF;
        }
    }

    public String getRemoteFilename()
    {
        if (_phexDriver != null)
        {
            return _phexDriver.getSearchHitFilename(_id);
        }
        else
        {
            return null;
        }
    }

    public boolean startDownload()
    {
        if (_phexDriver != null)
        {
            return _phexDriver.startDownload(_id);
        }
        else
        {
            return false;
        }
    }

    public boolean stopDownload()
    {
        if (_phexDriver != null)
        {
            return _phexDriver.stopDownload(_id);
        }
        else
        {
            return false;
        }
    }

    public boolean resumeDownload()
    {
        if (_phexDriver != null)
        {
            return _phexDriver.resumeDownload(_id);
        }
        else
        {
            return false;
        }
    }

    public boolean cancelDownload()
    {
        if (_phexDriver != null)
        {
            return _phexDriver.cancelDownload(_id);
        }
        else
        {
            return false;
        }
    }

    public boolean deleteHit()
    {
        return cancelDownload();
    }

    public boolean saveDownloadedFile(String localFilepath)
    {
        if (_phexDriver != null)
        {
            return _phexDriver.saveDownloadedFile(_id, localFilepath);
        }
        else
        {
            return false;
        }
    }
}