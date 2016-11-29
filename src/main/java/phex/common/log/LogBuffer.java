/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2011 Phex Development Group
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
 *  $Id: LogBuffer.java 4524 2011-06-27 10:04:38Z gregork $
 */
package phex.common.log;

import java.util.Collection;

import org.apache.commons.collections.buffer.UnboundedFifoBuffer;
import org.apache.commons.collections.map.MultiValueMap;

/**
 *
 */
public class LogBuffer
{
    private UnboundedFifoBuffer buffer;
    private MultiValueMap ownerMap;
    private int totalSize;
    private int maxSize;
    
    
    public LogBuffer( int maxSize )
    {
        this.maxSize = maxSize;
        totalSize = 0;
        buffer = new UnboundedFifoBuffer();
        ownerMap = new MultiValueMap();
    }
    
    public void addLogRecord( LogRecord record )
    {
        int size = record.getSize();
        buffer.add(record);
        ownerMap.put( record.getOwner(), record );
        totalSize += size;
        validateBufferSize();
    }
    
    public Collection<LogRecord> getLogRecords( Object owner )
    {
        return ownerMap.getCollection( owner );
    }
    
    private void validateBufferSize()
    {
        while ( totalSize > maxSize )
        {
            LogRecord record = (LogRecord) buffer.remove();
            ownerMap.remove( record.getOwner(), record);
            totalSize -= record.getSize();
        }
    }

    /**
     * @return
     */
    public int getFillSize()
    {
        return totalSize;
    }

    /**
     * @return
     */
    public int getElementCount()
    {
        return buffer.size();
    }
}
