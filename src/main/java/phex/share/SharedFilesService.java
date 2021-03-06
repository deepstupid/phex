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
 *  $Id: SharedFilesService.java 4436 2009-05-13 18:52:01Z tianna0370 $
 */
package phex.share;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.common.*;
import phex.common.collections.IntSet;
import phex.common.collections.StringTrie;
import phex.common.file.FileManager;
import phex.common.file.ManagedFile;
import phex.common.file.ManagedFileException;
import phex.event.UserMessageListener;
import phex.msg.QueryMsg;
import phex.LibraryPrefs;
import phex.peer.Peer;
import phex.thex.FileHashCalculationHandler;
import phex.thex.ThexCalculationWorker;
import phex.util.FileUtils;
import phex.util.StringUtils;
import phex.xml.sax.DPhex;
import phex.xml.sax.XMLBuilder;
import phex.xml.sax.share.DSharedFile;
import phex.xml.sax.share.DSharedLibrary;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 */
public class SharedFilesService extends AbstractLifeCycle
        implements FileHashCalculationHandler {
    private static final Logger logger = LoggerFactory.getLogger(SharedFilesService.class);
    /**
     * Lock object to lock saving of shared file lists.
     */
    private static final Object saveSharedFilesLock = new Object();

    private final ReentrantReadWriteLock rwLock;
    /**
     * The search engine used to query for shared files.
     */
    private final QueryResultSearchEngine searchEngine;
    /**
     * This HashMap maps native File objects to its shared counter part in the
     * phex system.
     */
    private final HashMap<File, SharedDirectory> directoryShareMap;
    /**
     * A list of shared directories.
     */
    private final ArrayList<SharedDirectory> sharedDirectories;
    /**
     * A maps that maps URNs to the file they belong to. This is for performant
     * searching by urn.
     * When accessing this object locking via the rwLock object is required.
     */
    private final HashMap<URN, ShareFile> urnToFileMap;
    /**
     * This map contains all absolute file paths as keys for the ShareFile
     * behind it.
     * When accessing this object locking via the rwLock object is required.
     */
    private final HashMap<String, ShareFile> nameToFileMap;
    /**
     * A map that contains the network creation time and a Set of ShareFile
     * the time belongs to.
     * When accessing this object locking via the rwLock object is required.
     */
    private final Map<Long, Set<ShareFile>> timeToFileMap;
    /**
     * This lists holds all shared files at there current index position.
     * When files are un-shared during runtime a null is placed at the index
     * position of the removed file. Access via the file index is done using
     * the method getFileByIndex( fileIndex ).
     * When accessing this object locking via the rwLock object is required.
     */
    private final ArrayList<ShareFile> indexedSharedFiles;
    /**
     * This list contains the shared files without gaps. It is used for direct
     * and straight access via the getFileAt( position ). Also it is used for
     * getFileCount().
     * When accessing this object locking via the rwLock object is required.
     */
    private final ArrayList<ShareFile> sharedFiles;
    /**
     * The string trie containing the key words and an IntSet with matching
     * file indices.
     */
    private final StringTrie<IntSet> keywordTrie;
    /**
     * A instance of a background runner queue to calculate
     * urns.
     */
    private final RunnerQueueWorker urnThexCalculationRunner;
    public final Peer peer;
    /**
     * The total size of the shared files.
     */
    private int totalFileSizeKb;
    /**
     * Local query routing table. Contains all shared files.
     */
    private QueryRoutingTable localRoutingTable;
    /**
     * Indicates if the local qrt needs to be rebuild i.e. after shared files
     * have changed.
     */
    private boolean localQRTNeedsUpdate;
    /**
     * Object that holds the save job instance while a save job is running. The
     * reference is null if the job is not running.
     */
    private SaveSharedFilesJob saveSharedFilesJob;

    public SharedFilesService(Peer peer) {

        this.peer = peer;
        rwLock = new ReentrantReadWriteLock();
        urnThexCalculationRunner = new RunnerQueueWorker(Thread.NORM_PRIORITY - 1);

        Environment.getInstance().scheduleTimerTask(
                new FileRescanTimer(), FileRescanTimer.TIMER_PERIOD,
                FileRescanTimer.TIMER_PERIOD);

        searchEngine = new QueryResultSearchEngine(peer, this);

        directoryShareMap = new HashMap<File, SharedDirectory>();
        sharedDirectories = new ArrayList<SharedDirectory>();
        urnToFileMap = new HashMap<URN, ShareFile>();
        nameToFileMap = new HashMap<String, ShareFile>();
        timeToFileMap = new TreeMap<Long, Set<ShareFile>>(Collections.reverseOrder());
        indexedSharedFiles = new ArrayList<ShareFile>();
        sharedFiles = new ArrayList<ShareFile>();
        keywordTrie = new StringTrie<IntSet>(true);
        totalFileSizeKb = 0;
        localQRTNeedsUpdate = true;
    }

    public DSharedLibrary loadSharedLibrary() {
        logger.debug("Load shared library configuration file.");

        File file = peer.file(Peer.XML_SHARED_LIBRARY_FILE_NAME);

        DPhex dPhex;
        try {
            ManagedFile managedFile = peer.files.getReadWriteManagedFile(file);
            dPhex = XMLBuilder.loadDPhexFromFile(managedFile);
            if (dPhex == null) {
                logger.debug("No shared library configuration file found.");
                return null;
            }
        } catch (InterruptedIOException exp) {
            // no error... just plain interruption.
            return null;
        } catch (IOException exp) {
            logger.error(exp.toString(), exp);
            Environment.getInstance().fireDisplayUserMessage(
                    UserMessageListener.SharedFilesLoadFailed,
                    new String[]{exp.toString()});
            // likely loading of shared files failed... again... lets try to find
            // the cause and create a spare copy of the failed file
            try {
                FileUtils.copyFile(file, new File(file.getAbsolutePath() + ".failed"));
            } catch (IOException e) {
                logger.error("Failed to store failed file copy: {}", e);
            }
            return null;
        } catch (ManagedFileException exp) {
            logger.error(exp.toString(), exp);
            Environment.getInstance().fireDisplayUserMessage(
                    UserMessageListener.SharedFilesLoadFailed,
                    new String[]{exp.toString()});
            return null;
        }

        // update old download list
        DSharedLibrary sharedLibrary = dPhex.getSharedLibrary();
        return sharedLibrary;
    }

    @Override
    protected void doStart() throws Exception {
        // TODO can we ensure that this is called as the last start or after
        // all other life cycle have been started?
        FileRescanRunner.rescan(this, true, false);
    }

    @Override
    protected void doStop() throws Exception {
        triggerSaveSharedFiles();
    }

    public List<ShareFile> handleQuery(QueryMsg queryMsg) {
        return searchEngine.handleQuery(queryMsg);
    }

    public QueryRoutingTable getLocalRoutingTable() {
        if (localQRTNeedsUpdate) {
            localRoutingTable = QueryRoutingTable.createLocalQueryRoutingTable(
                    getSharedFiles());
        }
        return localRoutingTable;
    }

    /**
     * Returns the part of the file path that is shared.
     *
     * @param file the file to determine the shared part of the path for.
     * @return the part of the file path that is shared.
     */
    private String getSharedFilePath(File file) {
        rwLock.readLock().lock();
        try {
            File highestDir = file.getParentFile();

            // copy shared directories set to prevent concurrent modification.
            // TODO this could still cause ConcurrentModificationException during the toArray()
            // operation, but it occurs very rarely.
            ArrayList<String> sharedDirectoriesCopy = new ArrayList<String>(
                    LibraryPrefs.SharedDirectoriesSet.get());
            for (String dirStr : sharedDirectoriesCopy) {
                // TODO this call will create many File instances, can't they be cached?
                File dir = new File(dirStr);
                if (FileUtils.isChildOfDir(file, dir)
                        && FileUtils.isChildOfDir(highestDir, dir)) {
                    highestDir = dir;
                }
            }
            // also share the shared dir itself.
            File highestParent = highestDir.getParentFile();
            if (highestParent != null) {
                highestDir = highestParent;
            }
            String pathStr = highestDir.getAbsolutePath();
            int length = pathStr.length();
            if (!pathStr.endsWith(File.separator)) {
                length++;
            }
            return file.getAbsolutePath().substring(length);
        } finally {
            rwLock.readLock().unlock();
//            try{ rwLock.readUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }
        }
    }

    /**
     * Returns the a shared file by name. If the given name is null or a
     * file with this name is not found then null is returned.
     */
    public ShareFile getFileByName(String name) {
        rwLock.readLock().lock();
        try {
            return nameToFileMap.get(name);
        } finally {
            rwLock.readLock().unlock();
//            try{ rwLock.readUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }

        }
    }

    /**
     * Returns the a shared file by file. If the given file is null or a
     * file is not found then null is returned.
     */
    public ShareFile getShareFileByFile(File file) {
        return getFileByName(file.getAbsolutePath());
    }

    /**
     * Gets the file at the given index in the shared file list.
     * To access via the file index use the method getFileByIndex( fileIndex )
     */
    public ShareFile getFileAt(int index) {
        rwLock.readLock().lock();
        try {
            if (index >= sharedFiles.size()) {
                return null;
            }
            return sharedFiles.get(index);
        } finally {
            rwLock.readLock().unlock();
//            try{ rwLock.readUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }
        }
    }

    public Iterator<IntSet> getIndexIteratorForPrefixTerm(String searchTerm,
                                                          int startOffset, int stopOffset) {
        return keywordTrie.getPrefixedBy(searchTerm, startOffset, stopOffset);
    }

    /**
     * Creates a list containing all SharedFiles.
     *
     * @return a list with all shared files.
     */
    public List<ShareFile> getSharedFiles() {
        rwLock.readLock().lock();
        try {
            return new ArrayList<ShareFile>(sharedFiles);
        } finally {
            rwLock.readLock().unlock();
//            try{ rwLock.readUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }
        }
    }

    /**
     * Returns the shared files count.
     */
    public int getFileCount() {
        rwLock.readLock().lock();
        try {
            return sharedFiles.size();
        } finally {
            rwLock.readLock().unlock();
            //try{ rwLock.readUnlock(); }
            //catch (IllegalAccessException exp )
            //{ logger.error( exp.toString(), exp ); }
        }
    }

    /**
     * Returns the total size of all shared files in KB.
     */
    public int getTotalFileSizeInKb() {
        rwLock.readLock().lock();
        try {
            return totalFileSizeKb;
        } finally {
            rwLock.readLock().unlock();
//            try{ rwLock.readUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }
        }
    }

    /**
     * Returns a shared file by its index number.
     */
    public ShareFile getFileByIndex(int fileIndex)
            throws IndexOutOfBoundsException {
        rwLock.readLock().lock();
        try {
            if (fileIndex >= indexedSharedFiles.size()) {
                return null;
            }
            return indexedSharedFiles.get(fileIndex);
        } finally {
            rwLock.readLock().unlock();

//            try{ rwLock.readUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }
        }
    }

    /**
     * Returns a shared file by its urn. If the given urn is null or a
     * file with this URN is not found then null is returned.
     */
    public ShareFile getFileByURN(URN fileURN)
            throws IndexOutOfBoundsException {
        rwLock.readLock().lock();
        try {
            if (fileURN == null) {
                return null;
            }
            return urnToFileMap.get(fileURN);
        } finally {
            rwLock.readLock().unlock();
//            try{ rwLock.readUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }
        }
    }

    //clear counters
    public void clearLibrarySearchCounters() {
        int index = 0;
        rwLock.readLock().lock();
        try {
            while (index < sharedFiles.size()) {
                sharedFiles.get(index).clearSearchCounters();
                index++;
            }
        } finally {
            rwLock.readLock().unlock();
//            try{ rwLock.readUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }

        }
    }

    public void clearLibraryUploadCounters() {
        int index = 0;
        rwLock.readLock().lock();
        try {
            while (index < sharedFiles.size()) {
                sharedFiles.get(index).clearUploadCounters();
                index++;
            }
        } finally {
            rwLock.readLock().unlock();
//            try{ rwLock.readUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }

        }
    }

    /**
     * Returns whether a file with the given URN is shared or not.
     *
     * @return true when a file with the given URN is shared, false otherwise.
     */
    public boolean isURNShared(URN fileURN)
            throws IndexOutOfBoundsException {
        rwLock.readLock().lock();
        try {
            if (fileURN == null) {
                return false;
            }
            return urnToFileMap.containsKey(fileURN);
        } finally {
            rwLock.readLock().unlock();
//            try{ rwLock.readUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }

        }
    }

    public List<ShareFile> getFilesByURNs(URN[] urns) {
        rwLock.readLock().lock();
        try {
            List<ShareFile> results = new ArrayList<ShareFile>(urns.length);
            for (int i = 0; i < urns.length; i++) {
                ShareFile file = urnToFileMap.get(urns[i]);
                if (file != null) {
                    results.add(file);
                }
            }
            return results;
        } finally {
            rwLock.readLock().unlock();
//            try{ rwLock.readUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }

        }
    }

    public List<ShareFile> getNewestFiles(int count) {
        List<ShareFile> fileList = new ArrayList<ShareFile>();
        Iterator<Entry<Long, Set<ShareFile>>> iterator = timeToFileMap.entrySet().iterator();
        while (iterator.hasNext() && fileList.size() < count) {
            Set<ShareFile> fileSet = iterator.next().getValue();
            Iterator<ShareFile> setIterator = fileSet.iterator();
            while (setIterator.hasNext() && fileList.size() < count) {
                ShareFile shareFile = setIterator.next();
                fileList.add(shareFile);
            }
        }
        return fileList;
    }

    /**
     * Adds a shared file if its not already shared.
     * Its important that the file owns a valid urn when being added.
     */
    public void addSharedFile(ShareFile shareFile) {
        File file = shareFile.getSystemFile();
        // check if file is already there
        if (getFileByName(file.getAbsolutePath()) != null) {
            return;
        }

        rwLock.writeLock().lock();
        int position;
        try {
            position = indexedSharedFiles.size();
            shareFile.setFileIndex(position);
            indexedSharedFiles.add(shareFile);
            sharedFiles.add(shareFile);

            // don't add to urn map yet since urns get calculated in background.
            nameToFileMap.put(file.getAbsolutePath(), shareFile);

            // fill search trie...
            String keywordsString = getSharedFilePath(shareFile.getSystemFile()).toLowerCase();
            String[] keywords = StringUtils.split(keywordsString, StringUtils.FILE_DELIMITERS);
            for (int i = 0; i < keywords.length; i++) {
                IntSet indices = keywordTrie.get(keywords[i]);
                if (indices == null) {// lazy initialize
                    indices = new IntSet();
                    keywordTrie.add(keywords[i], indices);
                }
                indices.add(position);
            }

            totalFileSizeKb += file.length() / 1024;
            localQRTNeedsUpdate = true;
        } finally {
            rwLock.writeLock().unlock();
//            try{ rwLock.writeUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }
        }
        //fireSharedFileAdded( position );
    }

    private void addTimeToFile(ShareFile shareFile) {
//        try{ rwLock.assertWriteLock(); }
//        catch (IllegalAccessException exp )
//        { logger.error( exp.toString(), exp ); }

        Long time = shareFile.getNetworkCreateTime();
        Set<ShareFile> shareFileSet = timeToFileMap.computeIfAbsent(time, k -> new HashSet<ShareFile>());
        shareFileSet.add(shareFile);
    }

    /**
     * Removed a shared file if its shared.
     */
    public void removeSharedFile(ShareFile shareFile) {
        rwLock.writeLock().lock();
        int position;
        try {
            // clear index position...
            int fileIndex = shareFile.getFileIndex();
            indexedSharedFiles.set(fileIndex, null);

            // clear from trie...
            String keywordsString = getSharedFilePath(shareFile.getSystemFile()).toLowerCase();
            String[] keywords = StringUtils.split(keywordsString, StringUtils.FILE_DELIMITERS);
            for (int i = 0; i < keywords.length; i++) {
                IntSet indices = keywordTrie.get(keywords[i]);
                if (indices != null) {
                    indices.remove(fileIndex);
                    if (indices.size() == 0) {
                        keywordTrie.remove(keywords[i]);
                    }
                }
            }

            // remove name to file map
            File file = shareFile.getSystemFile();
            urnToFileMap.remove(shareFile.getURN());
            nameToFileMap.remove(file.getAbsolutePath());
            removeTimeToFile(shareFile);

            // try to find shareFile in access list
            position = sharedFiles.indexOf(shareFile);
            if (position != -1) {// if removed update data
                sharedFiles.remove(position);
                totalFileSizeKb -= shareFile.getFileSize() / 1024;
                localQRTNeedsUpdate = true;
            }
        } finally {
            rwLock.writeLock().unlock();
//            try{ rwLock.writeUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }

        }
        //if ( position != -1 )
        //{// if removed fire events
        //    fireSharedFileRemoved( position );
        //}
    }

    /**
     * When calling lock must be owned!
     *
     * @param shareFile
     */
    private void removeTimeToFile(ShareFile shareFile) {
//        try{ rwLock.assertWriteLock(); }
//        catch (IllegalAccessException exp )
//        { logger.error( exp.toString(), exp ); }

        Long time = shareFile.getNetworkCreateTime();
        Set<ShareFile> shareFileSet = timeToFileMap.get(time);
        if (shareFileSet == null) {
            return;
        }
        shareFileSet.remove(shareFile);
        if (shareFileSet.size() == 0) {
            timeToFileMap.remove(time);
        }
    }

    /**
     * Adds a shared file if its not already shared.
     */
    public void updateSharedDirecotries(
            HashMap<File, SharedDirectory> sharedDirectoryMap,
            HashSet<SharedDirectory> sharedDirectoryList) {
        rwLock.writeLock().lock();
        try {
            directoryShareMap.clear();
            directoryShareMap.putAll(sharedDirectoryMap);
            sharedDirectories.clear();
            sharedDirectories.addAll(sharedDirectoryList);
            sharedDirectoriesChanged();
        } finally {
            rwLock.writeLock().unlock();
//            try{ rwLock.writeUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }

        }
    }

    /**
     * Do not modify!
     */
    public SharedDirectory[] getSharedDirectories() {
        rwLock.readLock().lock();
        try {
            SharedDirectory[] array = new SharedDirectory[sharedDirectories.size()];
            array = sharedDirectories.toArray(array);
            return array;
        } finally {
            rwLock.readLock().unlock();
//            try{ rwLock.readUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }
        }
    }

    /**
     *
     */
    public SharedDirectory getSharedDirectory(File file) {
        if (!file.isDirectory()) {
            return null;
        }
        rwLock.readLock().lock();
        try {
            SharedResource resource = directoryShareMap.get(file);
            if (resource instanceof SharedDirectory) {
                return (SharedDirectory) resource;
            }
            return null;
        } finally {
            rwLock.readLock().unlock();
//            try{ rwLock.readUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }
        }
    }

    /**
     * Adds a urn to file mapping for this ShareFile. When calling make sure
     * the urn is already set.
     *
     * @param shareFile
     */
    public void addUrn2FileMapping(ShareFile shareFile) {
        rwLock.writeLock().lock();
        try {
            assert (shareFile.getURN() != null);
            urnToFileMap.put(shareFile.getURN(), shareFile);
            // only add time once we have a valid URN...
            // it makes no sense to return whats new files without urn.
            addTimeToFile(shareFile);
        } finally {
            rwLock.writeLock().unlock();
//            try{ rwLock.writeUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }

        }
    }

    /**
     * Queues a ShareFile for calculating THEX.
     *
     * @param shareFile the share file to calculate the
     *                  thex hash for.
     */
    public void queueUrnCalculation(ShareFile shareFile) {
        UrnCalculationWorker worker = new UrnCalculationWorker(
                shareFile, this);
        urnThexCalculationRunner.add(worker);
    }

    /**
     * Queues a ShareFile for calculating its URN.
     *
     * @param shareFile the share file to calculate the
     *                  urn hash for.
     */
    public void queueThexCalculation(ShareFile shareFile) {
        ThexCalculationWorker worker = new ThexCalculationWorker(
                shareFile);
        urnThexCalculationRunner.add(worker);
    }

    public void setCalculationRunnerPause(boolean state) {
        urnThexCalculationRunner.setPause(state);
    }

    public int getCalculationRunnerQueueSize() {
        return urnThexCalculationRunner.getQueueSize();
    }

    /**
     * Clears the complete shared file list. Without making information
     * persistent.
     */
    public void clearSharedFiles() {
        rwLock.writeLock().lock();
        try {
            urnThexCalculationRunner.stopAndClear();
            sharedFiles.clear();
            indexedSharedFiles.clear();
            urnToFileMap.clear();
            nameToFileMap.clear();
            timeToFileMap.clear();
            totalFileSizeKb = 0;
            localQRTNeedsUpdate = true;
        } finally {
            rwLock.writeLock().unlock();
//            try{ rwLock.writeUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }

        }
        //fireAllSharedFilesChanged();
    }

    /**
     * Triggers a save of the download list. The call is not blocking and returns
     * directly, the save process is running in parallel.
     */
    public void triggerSaveSharedFiles() {
        logger.debug("Trigger save shared files...");
        synchronized (saveSharedFilesLock) {
            if (saveSharedFilesJob != null) {
                // save shared files is already in progress. we rerequest a save.
                saveSharedFilesJob.triggerFollowUpSave();
            } else {
                saveSharedFilesJob = new SaveSharedFilesJob(peer.files);
                saveSharedFilesJob.start();
            }
        }
    }

    ///////////////////// START event handling methods ////////////////////////
    private void sharedDirectoriesChanged() {

    }
    ///////////////////// END event handling methods /////////////////////////

    ///////////////////// START inner classes        /////////////////////////

    private class FileRescanTimer extends TimerTask {
        // once per minute
        public static final long TIMER_PERIOD = 1000 * 60;

        @Override
        public void run() {
            try {
                FileRescanRunner.rescan(SharedFilesService.this, false, false);
            } catch (Throwable th) {
                logger.error(th.toString(), th);
            }
        }
    }

    private class SaveSharedFilesJob extends Thread {
        private final FileManager files;
        private volatile boolean isFollowUpSaveTriggered;

        public SaveSharedFilesJob(FileManager files) {
            super(ThreadTracking.rootThreadGroup, "SaveSharedFilesJob");
            this.files = files;
            setPriority(Thread.MIN_PRIORITY);
        }

        public void triggerFollowUpSave() {
            isFollowUpSaveTriggered = true;
        }

        /**
         * Saving of the shared file list is done asynchronously to make sure that there
         * will be no deadlocks happening
         */
        @Override
        public void run() {
            File libraryFile = peer.file(
                    Peer.XML_SHARED_LIBRARY_FILE_NAME);
            File tmpFile = peer.file(
                    Peer.XML_SHARED_LIBRARY_FILE_NAME
                            + ".tmp");
            do {
                logger.debug("Saving shared library.");
                isFollowUpSaveTriggered = false;
                rwLock.readLock().lock();
                try {
                    DPhex dPhex = new DPhex();
                    dPhex.setPhexVersion(PhexVersion.getFullVersion());

                    DSharedLibrary dLibrary = createDSharedLibrary();
                    dPhex.setSharedLibrary(dLibrary);

                    // first save into temporary file...
                    ManagedFile tmpMgFile = files.getReadWriteManagedFile(tmpFile);
                    XMLBuilder.saveToFile(tmpMgFile, dPhex);

                    // after saving copy temporary file to real file.
                    ManagedFile libraryMgFile = files.getReadWriteManagedFile(libraryFile);
                    // lock library file.
                    try {
                        libraryMgFile.acquireFileLock();
                        FileUtils.copyFile(tmpFile, libraryFile);
                    } finally {
                        libraryMgFile.releaseFileLock();
                    }

                    //File zipFile = Environment.getInstance().getPhexConfigFile(
                    //    EnvironmentConstants.XML_SHARED_LIBRARY_FILE_NAME + ".def" );
                    //OutputStream out = new DeflaterOutputStream( new FileOutputStream( zipFile ) );
                    //FileInputStream inStream = new FileInputStream( libraryFile );
                    //int c;
                    //byte[] buffer = new byte[16*1024];
                    //while ( (c = inStream.read( buffer )) != -1 )
                    //{
                    //    out.write(buffer, 0, c);
                    //}
                } catch (ManagedFileException exp) {
                    if (exp.getCause() instanceof InterruptedException) { // the thread was interrupted and requested to stop, most likely
                        // by user request.
                        logger.debug(exp.toString());
                    } else {
                        // TODO during close this message is never displayed since application
                        // will exit too fast. A solution to delay exit process in case 
                        // SlideInWindows are open needs to be found.
                        logger.error(exp.toString(), exp);
                        Environment.getInstance().fireDisplayUserMessage(
                                UserMessageListener.SharedFilesSaveFailed,
                                new String[]{exp.toString()});
                    }
                } catch (IOException exp) {
                    logger.error(exp.toString(), exp);
                    Environment.getInstance().fireDisplayUserMessage(
                            UserMessageListener.SharedFilesSaveFailed, new String[]
                                    {exp.toString()});
                } finally {
                    rwLock.readLock().unlock();
//            try{ rwLock.readUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }
                }
            }
            while (isFollowUpSaveTriggered);
            logger.debug("Finished saving download list...");

            synchronized (saveSharedFilesLock) {
                // give created instance free once we are finished..
                saveSharedFilesJob = null;
            }
        }

        private DSharedLibrary createDSharedLibrary() {
            DSharedLibrary library = new DSharedLibrary();
            rwLock.readLock().lock();
            try {
                List<DSharedFile> sharedFileList = library.getSubElementList();
                for (ShareFile file : sharedFiles) {
                    try {
                        if (file.getURN() == null) {
                            continue;
                        }
                        DSharedFile dFile = file.createDSharedFile();
                        sharedFileList.add(dFile);
                    } catch (Exception exp) {
                        logger.error("SharedFile skipped due to error.", exp);
                    }
                }
            } finally {
                rwLock.readLock().unlock();
//            try{ rwLock.readUnlock(); }
//            catch (IllegalAccessException exp )
//            { logger.error( exp.toString(), exp ); }

            }
            return library;
        }
    }

    ///////////////////// END inner classes   ////////////////////////
}