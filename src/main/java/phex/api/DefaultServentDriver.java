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

import phex.servent.OnlineStatus;

public class DefaultServentDriver implements IServentDriver
{
    private IPhexDriver _phexDriver = null;

    private static IServentDriver _singleton = null;

    public static IServentDriver getInstance()
    {
        if (_singleton == null)
        {
            _singleton = new DefaultServentDriver();
        }

        return _singleton;
    }

    private DefaultServentDriver()
    {
        _phexDriver = DefaultPhexDriver.getInstance();
    }

    public boolean startServent()
    {
        if (_phexDriver != null)
        {
            return _phexDriver.startServent();
        }
        else
        {
            return false;
        }
    }

    public boolean stopServent()
    {
        if (_phexDriver != null)
        {
            return _phexDriver.stopServent();
        }
        else
        {
            return false;
        }
    }

    public OnlineStatus getServentStatus()
    {
        if (_phexDriver != null)
        {
            return _phexDriver.getServentStatus();
        }
        else
        {
            return OnlineStatus.OFFLINE;
        }
    }

    public ISearchHandle startSearch(String searchString)
    {
        if (_phexDriver != null)
        {
            long handle = _phexDriver.startSearch(searchString);
            if (handle == -1)
            {
                return null;
            }
            else
            {
                return new DefaultSearchHandle(handle);
            }
        }
        else
        {
            return null;
        }
    }

    public ISearchHandle[] getAllSearches()
    {
        if (_phexDriver != null)
        {
            return convertSearchHandles(_phexDriver.getAllSearches());
        }
        else
        {
            return null;
        }
    }

    private ISearchHandle[] convertSearchHandles(long [] handles)
    {
        if (handles == null)
        {
            return null;
        }

        final int size = handles.length;
        ISearchHandle [] result = new ISearchHandle[size];
        for (int index = 0; index < size; index++)
        {
            result[index] = new DefaultSearchHandle(handles[index]);
        }

        return result;
    }

    public boolean publishFile(String localFilePath)
    {
        if (_phexDriver != null)
        {
            return _phexDriver.publishFile(localFilePath);
        }
        else
        {
            return false;
        }
    }
}