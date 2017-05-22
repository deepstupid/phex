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
package phex;


import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.api.*;
import phex.common.Environment;
import phex.common.PhexVersion;
import phex.common.URN;
import phex.common.file.FileManager;
import phex.common.log.NLogger;
import phex.download.RemoteFile;
import phex.download.swarming.SWDownloadFile;
import phex.download.swarming.SwarmingManager;
import phex.prefs.Setting;
import phex.query.*;
import phex.peer.OnlineStatus;
import phex.peer.Peer;
import phex.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Phex Peer
 */
public class Phex extends Peer implements IPhexDriver {

    static final Logger logger = LoggerFactory.getLogger(Phex.class);

    static {
        boolean DEBUG = true;
        if (!DEBUG) {
            ((ch.qos.logback.classic.Logger) (LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)))
                    .setLevel(Level.INFO);
        }
    }

    public static void main(String args[]) throws Exception {
        new Phex().start();
    }

    public final FileManager files;


    public Phex() {
        this(null);
    }

    public Phex(String configPath) {
        super();
        files = new FileManager();
    }


    // Query time out in milliseconds.
    private static final long DEFAULT_QUERY_TIMEOUT = DefaultSearchProgress.DEFAULT_QUERY_TIMEOUT;

    // Maximum number of results (hits).
    private static final int DESIRED_RESULTS = DefaultSearchProgress.DESIRED_RESULTS;


    private long _nextSearchId = 0;
    private long _nextHitId = 0;
    private HashMap<Long, SearchItem> _searches = null;
    private KeyAllocator<SearchItem> _searchIdAllocator = new KeyAllocator<SearchItem>();
    private HashMap<Long, SearchResultItem> _hits = null;
    private KeyAllocator<SearchResultItem> _hitIdAllocator = new KeyAllocator<SearchResultItem>();
    private QueryManager queries = null;
    private SwarmingManager downloads;


    /**
     * Returns the Phex full vendor string including the version.
     *
     * @return full vendor string including version.
     */
    public static String getAgent() {
        return "Phex " + PrivateNetworkConstants.PRIVATE_BUILD_ID + PhexVersion.getFullVersion();
    }

    public synchronized boolean start() {

        try {
            if (!super.start()) {
                return false;
            }
        } catch (Exception e) {
            logger.error("start: {}", e);
            return false;
        }

        queries = this.getQueryService();
        downloads = this.getDownloadService();


        return true;
    }

    public OnlineStatus getServentStatus() {
        if (this != null) {
            return this.getOnlineStatus();
        } else {
            return OnlineStatus.OFFLINE;
        }
    }

    public synchronized boolean stop() {

        try {
            return super.stop();
        } catch (Exception ex) {
            return false;
        }

    }

    public long startSearch(String searchString) {
        SearchItem item = createSearch(searchString);

        return item == null ? -1 : item.id;
    }

    public String getSearchString(long searchId) {
        SearchItem item = searchForId(searchId);

        return item == null ? null : item.getSearchString();
    }

    public short getSearchStatus(long searchId) {
        SearchItem item = searchForId(searchId);

        return item == null ? SearchItem.STATUS_NON_EXISTING : item.getStatus();
    }

    public boolean stopSearch(long searchId) {
        SearchItem item = searchForId(searchId);
        if (item == null) {
            return false;
        }

        Search search = item.getSearch();
        if (search == null) {
            return false;
        }

        search.stopSearching();

        return true;
    }

    public boolean resumeSearch(long searchId) {
        SearchItem item = searchForId(searchId);
        if (item == null) {
            return false;
        }

        Search search = item.getSearch();
        if (search == null) {
            return false;
        }

        DefaultSearchProgress progress = DefaultSearchProgress.createStandardProgress(DEFAULT_QUERY_TIMEOUT, DESIRED_RESULTS);
        search.startSearching(progress);

        return true;
    }

    public boolean deleteSearch(long searchId) {
        final boolean stopOK = stopSearch(searchId);
        if (stopOK) {
            return removeSearch(searchId);
        }

        return false;
    }

    public long[] getAllSearches() {
        Collection<SearchItem> searches = getAllSearchItems();
        List<Long> ids = new ArrayList<Long>();

        for (SearchItem search : searches) {
            // Defensive programming: skip null.
            if (search != null) {
                ids.add(new Long(search.id));
            }
        }

        return longListToArray(ids);
    }

    public long[] getSearchHits(long searchId) {
        SearchItem item = searchForId(searchId);

        if (item == null) {
            return null;
        } else {
            List<SearchResultItem> hits = item.getResults();
            List<Long> ids = new ArrayList<Long>();
            for (SearchResultItem hit : hits) {
                if (hit != null) {
                    ids.add(new Long(hit.getId()));
                }
            }

            return longListToArray(ids);
        }
    }

    public String getSearchHitFilename(long hitId) {
        SearchResultItem item = hitForId(hitId);
        if (item == null) {
            return null;
        }

        RemoteFile remoteFile = item.getRemoteFile();
        if (remoteFile == null) {
            return null;
        } else {
            return remoteFile.getFilename();
        }
    }

    public short getSearchHitStatus(long hitId) {
        SearchResultItem item = hitForId(hitId);

        return item == null ? SearchResultItem.STATUS_UNDEF : item.getStatus();
    }

    public boolean startDownload(long hitId) {
        SearchResultItem item = hitForId(hitId);
        if (item == null) {
            return false;
        }

        item.setLocalFilepath(null);

        SearchResultItem[] items = new SearchResultItem[1];
        items[0] = item;

        performStartDownload(items);

        return true;
    }

    public boolean saveDownloadedFile(long hitId, String localFilepath) {
        SearchResultItem item = hitForId(hitId);
        if (item == null) {
            return false;
        }

        final short status = item.getStatus();
        if (status != SearchResultItem.STATUS_TRANSFER_COMPLETED) {
            return false;
        }

        item.setLocalFilepath(localFilepath);

        return performMoveDownloadedFile(item);
    }

    public boolean stopDownload(long hitId) {
        SearchResultItem item = hitForId(hitId);
        if (item == null) {
            return false;
        }

        SWDownloadFile swFile = item.getSWDownloadFile();
        if (swFile == null) {
            return false;
        }

        if (swFile.isDownloadInProgress()) {
            swFile.stopDownload();

            return true;
        } else {
            return false;
        }
    }

    public boolean resumeDownload(long hitId) {
        SearchResultItem item = hitForId(hitId);
        if (item == null) {
            return false;
        }

        SWDownloadFile swFile = item.getSWDownloadFile();
        if (swFile == null) {
            return false;
        }

        if (swFile.isDownloadStopped()) {
            swFile.startDownload();

            return true;
        } else {
            return false;
        }
    }

    public boolean cancelDownload(long hitId) {
        SearchResultItem item = hitForId(hitId);
        if (item == null) {
            return false;
        }

        final short downloadStatus = item.getStatus();
        if (downloadStatus == SearchResultItem.STATUS_TRANSFER_RUNNING ||
                downloadStatus == SearchResultItem.STATUS_TRANSFER_COMPLETED) {
            SWDownloadFile file = item.getSWDownloadFile();
            if (file != null && downloads != null) {
                downloads.removeDownloadFile(file);
            }
        }

        Long key = item.getId();
        _hits.remove(key);
        SearchItem searchItem = item.getSearchItem();
        if (searchItem != null) {
            searchItem.removeResult(item);
        }

        return true;
    }

    public boolean deleteHit(long hitId) {
        return cancelDownload(hitId);
    }

    public boolean publishFile(String localFilePath) {
        if (localFilePath == null) {
            return false;
        }

        Setting<Set<String>> sds = LibraryPrefs.SharedDirectoriesSet;
        if (sds == null) {
            return false;
        }

        Set<String> dirs = sds.get();
        if (dirs == null) {
            return false;
        }

        String publicFolderPath = null;
        for (String string : dirs) {
            publicFolderPath = string;

            if (publicFolderPath != null) {
                break;
            }
        }

        if (publicFolderPath == null) {
            return false;
        }

        return copyFileToFolder(localFilePath, publicFolderPath);
    }

    private static boolean copyFileToFolder(String inputFilePath, String outputFolderPath) {
        if (inputFilePath == null || outputFolderPath == null) {
            return false;
        }

        try {
            File inputFile = new File(inputFilePath);
            File outputFolder = new File(outputFolderPath);

            if (!inputFile.isFile()) {
                return false;
            }

            if (!outputFolder.isDirectory()) {
                return false;
            }

            final String inputFileCanonicalPath = inputFile.getCanonicalPath();
            final String inputFileName = inputFile.getName();
            final String outputFilePath = outputFolderPath + File.separator + inputFileName;
            File outputFile = new File(outputFilePath);

            // We are not allowed to write a file to itself.
            if (inputFileCanonicalPath.equals(outputFile.getCanonicalPath())) {
                return false;
            }

            return Util.copyFile(inputFilePath, outputFilePath);
        } catch (Throwable t) {
            return false;
        }
    }

    private Collection<SearchItem> getAllSearchItems() {
        return _searches.values();
    }

    private SearchItem createSearch(String searchString) {
        // For simplicity, we do not resuse ids. 
        if (_nextSearchId >= Long.MAX_VALUE) {
            return null;
        }

        if (searchString != null && queries != null) {
            SearchContainer searchContainer = queries.getSearchContainer();
            Search newSearch = searchContainer.createSearch(searchString);
            DefaultSearchProgress progress = DefaultSearchProgress.createStandardProgress(DEFAULT_QUERY_TIMEOUT, DESIRED_RESULTS);
            newSearch.startSearching(progress);

            NLogger.info(Phex.class, "Submitted query with search string = " + searchString);

            return storeSearch(newSearch);
        } else {
            return null;
        }
    }

    private SearchItem storeSearch(Search search) {
        if (_searches == null) {
            _searches = new HashMap<Long, SearchItem>();
        }

        synchronized (_searches) {
            for (SearchItem item : _searches.values()) {
                if (item == null) {
                    continue;
                }

                // If the search is already in the list, do nothing.
                if (item.getSearch() == search) {
                    return null;
                }
            }

            List<Long> keys = _searchIdAllocator.allocateKeys(_searches, 1, _nextSearchId);
            if (keys == null) {
                return null;
            }

            Long key = keys.get(0);
            SearchItem item = new SearchItem(search, key.longValue());

            _searches.put(key, item);

            _nextSearchId = _nextSearchId + 1 < Long.MAX_VALUE ? _nextSearchId + 1 : 0;

            return item;
        }
    }

    private boolean removeSearch(long searchHandle) {
        if (_searches == null) {
            return true;
        }

        synchronized (_searches) {
            Long key = new Long(searchHandle);

            SearchItem item = _searches.get(key);
            if (item != null) {
                _searches.remove(key);

                return true;
            } else {
                return false;
            }
        }
    }

    private SearchItem searchForId(long id) {
        if (_searches == null) {
            return null;
        }

        synchronized (_searches) {
            return _searches.get(new Long(id));
        }
    }

    private SearchResultItem hitForId(long id) {
        if (_hits == null) {
            _hits = new HashMap<Long, SearchResultItem>();
        }

        synchronized (_hits) {
            return _hits.get(new Long(id));
        }
    }

    //@EventTopicSubscriber(topic=PhexEventTopics.Search_Data)
    public void onSearchDataEvent(String topic, SearchDataEvent event) {
        SearchItem item = findSearchForEvent(event);
        if (item == null) {
            return;
        }

        if (event.getType() == SearchDataEvent.SEARCH_HITS_ADDED) {
            RemoteFile[] newSearchResults = event.getSearchData();

            List<SearchResultItem> results = item.addResults(newSearchResults);
            final boolean hashResultsOK = hashResults(results);
            if (hashResultsOK) {
                NLogger.info(Phex.class, "Added search results to hash.");
            } else {
                NLogger.error(Phex.class, "Cannot add search results to hash.");

                // TODO: delete results here.
            }
        }
    }

    private SearchItem findSearchForEvent(SearchDataEvent event) {
        if (event == null) {
            return null;
        }

        if (_searches == null) {
            return null;
        }

        synchronized (_searches) {
            for (SearchItem item : _searches.values()) {
                if (item == null) {
                    continue;
                }

                if (item.getSearch() == event.getSource()) {
                    return item;
                }
            }
        }

        return null;
    }

    private static long[] longListToArray(List<Long> list) {
        if (list == null) {
            return new long[0];
        }

        final int sz = list.size();

        long[] longArray = new long[sz];

        for (int index = 0; index < sz; index++) {
            Long el = list.get(index);

            longArray[index] = el == null ? 0 : el.longValue();
        }

        return longArray;
    }

    private boolean hashResults(List<SearchResultItem> results) {
        if (_hits == null) {
            _hits = new HashMap<Long, SearchResultItem>();
        }

        if (results == null) {
            return false;
        }

        final int resultsSize = results.size();

        synchronized (_hits) {
            List<Long> keys = _hitIdAllocator.allocateKeys(_hits, resultsSize, _nextHitId);
            if (keys == null) {
                return false;
            }

            final int keysSize = keys.size();
            if (keysSize != resultsSize) {
                return false;
            }

            final Long lastKey = keys.get(keysSize - 1);
            final long lastKeyLong = lastKey.longValue();
            _nextHitId = lastKeyLong + 1 < Long.MAX_VALUE ? lastKeyLong + 1 : 0;
            for (int index = 0; index < keysSize; index++) {
                Long key = keys.get(index);
                SearchResultItem item = results.get(index);

                item.setId(key.longValue());
                _hits.put(key, item);

                NLogger.info(Phex.class, "Added result with key " + key + '.');
            }

            return true;
        }
    }

    private void performStartDownload(SearchResultItem[] searchResultItems) {
        // We need a final variable to be passed to the runnable object.
        final SearchResultItem[] hits = searchResultItems;

        Runnable runner = () -> {
            try {
                final int sz = hits.length;
                RemoteFile[] rfiles = new RemoteFile[sz];

                for (int i = 0; i < sz; i++) {
                    rfiles[i] = hits[i].getRemoteFile();
                }

                for (int i = 0; i < sz; i++) {
                    rfiles[i].setInDownloadQueue(true);

                    final long fileSz = rfiles[i].getFileSize();
                    final URN urn = rfiles[i].getURN();
                    SWDownloadFile downloadFile = downloads.getDownloadFile(fileSz, urn);

                    if (downloadFile != null) {
                        downloadFile.addDownloadCandidate(rfiles[i]);
                        hits[i].setSWDownloadFile(downloadFile);
                    } else {
                        RemoteFile dfile = new RemoteFile(rfiles[i]);
                        String searchTerm = StringUtils.createNaturalSearchTerm(dfile.getFilename());
                        final String fileName = dfile.getFilename();

                        downloadFile = downloads.addFileToDownload(dfile, fileName, searchTerm);
                        hits[i].setSWDownloadFile(downloadFile);
                    }

                    final String message = "Started download of file " + rfiles[i].getFilename();
                    NLogger.info(Phex.class, message);
                }
            } catch (Throwable th) {
                NLogger.error(Phex.class, th, th);
            }
        };

        Environment.getInstance().executeOnThreadPool(runner, "QuickDownloadAction");
    }

    private static boolean performMoveDownloadedFile(SearchResultItem item) {
        if (item == null) {
            return false;
        }

        SWDownloadFile swFile = item.getSWDownloadFile();
        if (swFile == null) {
            return false;
        }

        try {
            File inputFile = swFile.getDestinationFile();
            final String inputFilePath = inputFile.getCanonicalPath();
            final String outputFilePath = item.getLocalFilepath();

            return Util.moveFile(inputFilePath, outputFilePath);
        } catch (IOException ex) {
            return false;
        }
    }
}