/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2006 Phex Development Group
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
 *  Created on 12-Nov-2003
 *  --- CVS Information ---
 *  $Id: Thex.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.thex;

import phex.download.swarming.SWDownloadSegment;

/**
 * @author Laura Requena
 *         <p>
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Thex {

    private String root;

    private String serialization;

    private byte[] serializationByte;

    private int hashSize;

    //private final Vector leavesHashes;

    private int nodes10serSegment;

    private int firstBlock;

    private int levelsLeft;

    private SWDownloadSegment segment;

    public Thex() {
        //leavesHashes = new Vector();
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public SWDownloadSegment getSegment() {
        return segment;
    }

    public void setSegment(SWDownloadSegment sg) {
        segment = sg;
    }

    public int getNodes10serSegment() {
        return nodes10serSegment;
    }

    public void setNodes10serSegment(int nodes10ser) {
        nodes10serSegment = nodes10ser;
    }

    public int getFirstBlock() {
        return firstBlock;
    }

    public void setFirstBlock(int firstB) {
        firstBlock = firstB;
    }

    public int getLevelsLeft() {
        return levelsLeft;
    }

//    public Vector getLeavesHashes()
//    {
//        return leavesHashes;
//    }

//    public void addLeafHash(byte[] hash)
//    {
//        leavesHashes.add(hash);
//    }

    public void setLevelsLeft(int levelsL) {
        levelsLeft = levelsL;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }

    public byte[] getSerializationByte() {
        return serializationByte;
    }

    public void setSerializationByte(byte[] serializationByte) {
        this.serializationByte = serializationByte;
    }

    public int getHashSize() {
        return hashSize;
    }

    public void setHashSize(int hashSize) {
        this.hashSize = hashSize;
    }

}
