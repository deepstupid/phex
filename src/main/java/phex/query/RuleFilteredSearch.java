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
 *  $Id: RuleFilteredSearch.java 4318 2008-11-30 22:50:47Z gregork $
 */
package phex.query;

import phex.download.RemoteFile;
import phex.rules.Rule;
import phex.peer.Peer;

import java.util.ArrayList;

public class RuleFilteredSearch {
    private final Peer peer;



    private final Search search;

    /**
     * Associated class that is able to hold search results. Access to this
     * should be locked by holding 'this'.
     */
    private final SearchResultHolder displayedSearchResults;

    /**
     * Associated class that is able to hold search hidden results. Access to this
     * should be locked by holding 'this'.
     */
    private final SearchResultHolder hiddenSearchResults;

    private final Rule[] searchFilterRules;

    private DefaultSearchProgress searchProgress;

    public RuleFilteredSearch(Search search, Rule ruleFilter, Peer peer) {
        this(search, new Rule[]{ruleFilter}, peer);
    }

    public RuleFilteredSearch(Search search, Rule[] ruleFilters, Peer peer) {
        super();
        this.peer = peer;

        displayedSearchResults = new SearchResultHolder();
        hiddenSearchResults = new SearchResultHolder();
        this.search = search;
        this.searchFilterRules = ruleFilters;

    }

    public int getResultCount() {
        return displayedSearchResults.getQueryHitCount();
    }

    public int getHiddenCount() {
        return hiddenSearchResults.getQueryHitCount();
    }

    /**
     * Delegates to inner search
     */
    public int getProgress() {
        return search.getProgress();
    }

    /**
     * Delegates to inner search
     */
    public boolean isSearchFinished() {
        return search.isSearchFinished();
    }

    public void startSearching(DefaultSearchProgress searchProgress) {
        this.searchProgress = searchProgress;
        search.startSearching(searchProgress);
    }

    /**
     * Delegates to inner search
     */
    public void stopSearching() {
        search.stopSearching();
    }

    private void processRules(RemoteFile[] remoteFiles) {
        for (int i = 0; i < searchFilterRules.length; i++) {
            searchFilterRules[i].process(search, remoteFiles, peer);
        }

        ArrayList<RemoteFile> newHitList = new ArrayList<RemoteFile>(remoteFiles.length);
        for (int j = 0; j < remoteFiles.length; j++) {
            if (remoteFiles[j].isFilteredRemoved()) {
                continue;
            } else if (remoteFiles[j].isFilteredHidden()) {
                hiddenSearchResults.addQueryHit(remoteFiles[j]);
            } else {

                displayedSearchResults.addQueryHit(remoteFiles[j]);
                newHitList.add(remoteFiles[j]);
            }
        }
        // if something was added...
        if (newHitList.size() > 0) {
            searchProgress.incReceivedResultsCount(newHitList.size());
            RemoteFile[] newHits = new RemoteFile[newHitList.size()];
            newHitList.toArray(newHits);
            fireSearchHitsAdded(newHits);
        }
    }

    //@EventTopicSubscriber(topic=PhexEventTopics.Search_Data)
    public void onSearchDataEvent(String topic, SearchDataEvent event) {
        if (search != event.getSource()) {
            return;
        }
        int type = event.getType();
        switch (type) {
            case SearchDataEvent.SEARCH_HITS_ADDED:
                processRules(event.getSearchData());
                break;

            default:
                // all other events are simply forwarded..
                forwardSearchChangeEvent(event);
                break;
        }
    }

    ///////////////////// START event handling methods ////////////////////////

    protected void fireSearchStarted() {
        SearchDataEvent searchChangeEvent =
                new SearchDataEvent(this, SearchDataEvent.SEARCH_STARTED);
        fireSearchChangeEvent(searchChangeEvent);
    }

    protected void fireSearchStoped() {
        SearchDataEvent searchChangeEvent =
                new SearchDataEvent(this, SearchDataEvent.SEARCH_STOPED);
        fireSearchChangeEvent(searchChangeEvent);
    }

    protected void fireSearchFiltered() {
        SearchDataEvent searchChangeEvent =
                new SearchDataEvent(this, SearchDataEvent.SEARCH_FILTERED);
        fireSearchChangeEvent(searchChangeEvent);
    }

    public void fireSearchChanged() {
        SearchDataEvent searchChangeEvent =
                new SearchDataEvent(this, SearchDataEvent.SEARCH_CHANGED);
        fireSearchChangeEvent(searchChangeEvent);
    }

    protected void fireSearchHitsAdded(RemoteFile[] newHits) {
        SearchDataEvent searchChangeEvent = new SearchDataEvent(this,
                SearchDataEvent.SEARCH_HITS_ADDED, newHits);
        fireSearchChangeEvent(searchChangeEvent);
    }

    private void forwardSearchChangeEvent(final SearchDataEvent searchChangeEvent) {
        SearchDataEvent event = new SearchDataEvent(this, searchChangeEvent.getType(),
                searchChangeEvent.getSearchData());

    }

    private void fireSearchChangeEvent(final SearchDataEvent searchChangeEvent) {

    }

    ///////////////////// END event handling methods ////////////////////////
}