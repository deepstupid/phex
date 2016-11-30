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
import phex.query.Search;

import java.util.ArrayList;
import java.util.List;

public class SearchItem
{
    public static final short STATUS_UNDEFINED    = -1;

    public static final short STATUS_RUNNING      = 0;

    public static final short STATUS_FINISHED     = 1;

    public static final short STATUS_NON_EXISTING = 2;

    public static final int DEFAULT_MAXIMUM_RESULTS = 200;

    private Search _search = null;

    private long _id = -1;

    private String _searchString = null;

    private List<SearchResultItem> _results = null;

    private int _maximuResults = DEFAULT_MAXIMUM_RESULTS;

    public SearchItem(Search search)
    {
        _search  = search;

        _results = new ArrayList<SearchResultItem>();

        _maximuResults = DEFAULT_MAXIMUM_RESULTS;
    }

    public SearchItem(Search search, int maximuResults)
    {
        _search  = search;

        _results = new ArrayList<SearchResultItem>();

        _maximuResults = maximuResults;
    }

    public SearchItem()
    {
    }

    public short getStatus()
    {
        if (_search == null)
        {
            return STATUS_NON_EXISTING;
        }
        else if (_search.isSearchFinished())
        {
            return STATUS_FINISHED;
        }
        else
        {
            return STATUS_RUNNING;
        }
    }

    public void setSearch(Search search)
    {
        _search = search;
    }

    public Search getSearch()
    {
        return _search;
    }

    public void setResults(List<SearchResultItem> results)
    {
        synchronized(_results)
        {
            _results = results;
        }
    }

    public List<SearchResultItem> getResults()
    {
        return _results;
    }

    public SearchResultItem addResult(RemoteFile remoteFile)
    {
        if (remoteFile == null)
        {
            return null;
        }

        if (_results == null)
        {
            _results = new ArrayList<SearchResultItem>();
        }

        synchronized(_results)
        {
            if (_results.size() >= _maximuResults)
            {
                return null;
            }

            for (SearchResultItem item : _results)
            {
                if (item == null)
                {
                    continue;
                }

                // Do not add a file twice.
                if (item.getRemoteFile() == remoteFile)
                {
                    return null;
                }
            }

            SearchResultItem resultItem = new SearchResultItem(remoteFile);
            resultItem.setSearchItem(this);
            _results.add(resultItem);

//          System.out.println("Found " + _results.size() + " results.");
            return resultItem;
        }
    }

    public List<SearchResultItem> addResults(RemoteFile [] remoteFiles)
    {
        List<SearchResultItem> resultList = new ArrayList<SearchResultItem>();
        if (remoteFiles == null)
        {
            return resultList;
        }

        for (RemoteFile remoteFile : remoteFiles)
        {
            SearchResultItem resultItem = addResult(remoteFile);

            if (resultItem != null)
            {
                resultList.add(resultItem);
            }
        }

        return resultList;
    }

    public void removeResult(SearchResultItem searchResultItem)
    {
        if (searchResultItem == null)
        {
            return;
        }

        if (_results == null)
        {
            return;
        }

        synchronized(_results)
        {
            searchResultItem.setSearchItem(null);

            _results.remove(searchResultItem);
        }
    }

    public void setId(long id)
    {
        _id = id;
    }

    public long getId()
    {
        return _id;
    }

    public void setSearchString(String searchString)
    {
        _searchString = searchString;
    }

    public String getSearchString()
    {
        return _searchString;
    }

    public void setMaximuResults(int maximuResults)
    {
        this._maximuResults = maximuResults;
    }

    public int getMaximuResults()
    {
        return _maximuResults;
    }
}