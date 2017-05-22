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
 *  $Id$
 */
package phex.upload;

import junit.framework.TestCase;
import phex.common.address.DefaultDestAddress;
import phex.servent.Peer;
import phex.share.ShareFile;

import java.util.ArrayList;
import java.util.List;

public class HrrnQueueComparatorTest extends TestCase
{
    private UploadManager uploadManager;

    public void setUp()
    {
        //        PhexGuiPrefs.init();
//        Localizer.initialize( InterfacePrefs.LocaleName.get() );

        uploadManager = new UploadManager( new Peer() );
    }
    
    public void testOrder() throws InterruptedException
    {
        List<UploadQueueState> testList = new ArrayList<UploadQueueState>();
        
        for ( int i = 20; i > 0; i-- )
        {
            UploadQueueState uqs = createUploadQueueState( (i + 1) * 1000000L );
            testList.add( uqs );
            Thread.sleep( 10 );
        }
        Thread.sleep( 1000 );
        long sortTime = System.currentTimeMillis();
        testList.sort(new HrrnQueueComparator(sortTime));
        
        for ( UploadQueueState uqs : testList )
        {
            System.out.println( uqs.getFirstQueueTime() + " - " 
                + uqs.getLastRequestedFile().getFileSize() + " - " 
                + HrrnQueueComparator.calcRatio( uqs, sortTime ) );
        }
        
//        int i = 0;
//        for ( UploadQueueState uqs : testList )
//        {
//            assertEquals( (i + 1) * 1000L, uqs.getLastRequestedFile().getFileSize() );
//            i++;
//        }
    }
    
    private UploadQueueState createUploadQueueState( long fileSize )
    {
        UploadState us = new UploadState( new DefaultDestAddress( "", 0 ), "", uploadManager );
        ShareFile shareFile = new DummyShareFile( fileSize );
        UploadQueueState uqs = new UploadQueueState( us, shareFile );
        return uqs;
    }
    
    private static class DummyShareFile extends ShareFile
    {
        public DummyShareFile( long fileSize )
        {
            super( fileSize );
        }        
    }
}
