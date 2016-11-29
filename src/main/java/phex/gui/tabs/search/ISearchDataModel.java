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
 *  $Id: ISearchDataModel.java 4380 2009-02-21 20:50:52Z gregork $
 */
package phex.gui.tabs.search;


/**
 * 
 */
public interface ISearchDataModel
{
    /**
     * Sets the visualiztion model that needs to be updated on data model
     * changes or null if currently not visible.
     * @param model the new visualization model or null if not visible.
     */
    public void setVisualizationModel( SearchTreeTableModel model );

    /**
     * @param index
     * @return
     */
    public SearchResultElement getSearchElementAt(int index);

    /**
     * @return
     */
    public int getSearchElementCount();

    /**
     * @param i
     * @param isSortedAscending
     */
    public void setSortBy(int i, boolean isSortedAscending);

}
