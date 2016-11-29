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
 *  $Id: TestQueryRoutingTable.java 4381 2009-02-21 23:21:40Z gregork $
 */
package phex.test;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import phex.common.Phex;
import phex.common.QueryRoutingTable;
import phex.gui.prefs.InterfacePrefs;
import phex.gui.prefs.PhexGuiPrefs;
import phex.msg.GUID;
import phex.msg.InvalidMessageException;
import phex.msg.QRPatchTableMsg;
import phex.msg.QRResetTableMsg;
import phex.msg.QueryMsg;
import phex.msg.RouteTableUpdateMsg;
import phex.prefs.core.PhexCorePrefs;
import phex.servent.Servent;
import phex.share.ShareFile;
import phex.utils.AccessUtils;
import phex.utils.Localizer;


public class TestQueryRoutingTable extends TestCase
{
    private QueryRoutingTable qrTable;

    public TestQueryRoutingTable(String s)
    {
        super(s);
    }

    protected void setUp()
    {
        Phex.initialize();
        /// should not depend on these
        PhexCorePrefs.init();
        PhexGuiPrefs.init();
        Localizer.initialize( InterfacePrefs.LocaleName.get() );
        Servent servent = Servent.getInstance();
        
        qrTable = new QueryRoutingTable();
        try
        {
            // words added once with not more then 5 chars...
            AccessUtils.invokeMethod( qrTable, "addWord", "phex" );
            AccessUtils.invokeMethod( qrTable, "addWord", "java" );
            AccessUtils.invokeMethod( qrTable, "add", "test query route table" );
    
            // words added more then once... causes to be (length - 5) but
            // max 6 times.
            AccessUtils.invokeMethod( qrTable, "add", "ExtendedWord" );
            
            AccessUtils.invokeMethod( qrTable, "add", "javafo" );
        }
        catch ( Throwable exp )
        {
            fail();
        }

    }

    protected void tearDown()
    {
    }

    public void testElementCount()
    {
       Integer entryCount = (Integer)AccessUtils.getFieldValue( qrTable, "entryCount" );
       assertEquals( 12, entryCount.intValue() );
    }

    public void testContainsQueryString()
    {
        String[] trueSearchStrings =
        {
            "phex",
            "java",
            "route",
            "test query table",
            "ExtendedWord",
            "Extended",
            "ExtendedWor",
            "ExtendedWo phex"
        };
        for ( int i = 0; i < trueSearchStrings.length; i++ )
        {
            QueryMsg query = createDummyMsg( trueSearchStrings[i] );
            assertTrue( qrTable.containsQuery( query ) );
        }

        String[] falseSearchStrings =
        {
            "wrong",
            "test query table wrong",
        };
        for ( int i = 0; i < falseSearchStrings.length; i++ )
        {
            QueryMsg query = createDummyMsg( falseSearchStrings[i] );
            assertFalse( qrTable.containsQuery( query ) );
        }
    }

    public void testQRTInitMessages()
    {
        Iterator iterator = QueryRoutingTable.buildRouteTableUpdateMsgIterator(
            qrTable, null );

        Object obj = iterator.next();
        assertTrue( obj instanceof QRResetTableMsg );
        obj = iterator.next();
        assertTrue( obj instanceof QRPatchTableMsg );
    }

    public void testBuildAndUpdate()
        throws InvalidMessageException
    {
        Iterator iterator = QueryRoutingTable.buildRouteTableUpdateMsgIterator(
            qrTable, null );

        QueryRoutingTable newTable = new QueryRoutingTable();
        while( iterator.hasNext() )
        {
            RouteTableUpdateMsg msg = (RouteTableUpdateMsg)iterator.next();
            newTable.updateRouteTable( msg, null );
        }
        BitSet set = (BitSet)AccessUtils.getFieldValue( qrTable, "qrTable" );
        BitSet newSet = (BitSet) AccessUtils.getFieldValue( newTable, "qrTable" );
        assertTrue( "Found:\n" + newSet + "\nExpected:\n" + set, newSet.equals( set ) );
    }
    
    
    public void testLookupTimes() throws Throwable
    {
        boolean suc;
        QueryRoutingTable qrTable = createRandomFilled( 16*1024 );
        long start = System.currentTimeMillis();
        for ( int i=0; i<10000; i++ )
        {
            qrTable.containsQuery( createDummyMsg( new GUID().toString() ) );
        }
        long end = System.currentTimeMillis();
        long took = end-start;
        System.out.println( took );
        
        
        qrTable = createRandomFilled( 64*1024 );
        start = System.currentTimeMillis();
        for ( int i=0; i<10000; i++ )
        {
            qrTable.containsQuery( createDummyMsg( new GUID().toString() ) );
        }
        end = System.currentTimeMillis();
        took = end-start;
        System.out.println( took );
        
        
        qrTable = createRandomFilled( 256*1024 );
        start = System.currentTimeMillis();
        for ( int i=0; i<10000; i++ )
        {
            qrTable.containsQuery( createDummyMsg( new GUID().toString() ) );
        }
        end = System.currentTimeMillis();
        took = end-start;
        System.out.println( took );
        
        
        qrTable = createRandomFilled( 1024*1024 );
        start = System.currentTimeMillis();
        for ( int i=0; i<10000; i++ )
        {
            qrTable.containsQuery( createDummyMsg( new GUID().toString() ) );
        }
        end = System.currentTimeMillis();
        took = end-start;
        System.out.println( took );
        
        qrTable = createRandomFilled( 16*1024 );
        start = System.currentTimeMillis();
        for ( int i=0; i<10000; i++ )
        {
            qrTable.containsQuery( createDummyMsg( new GUID().toString() ) );
        }
        end = System.currentTimeMillis();
        took = end-start;
        System.out.println( took );
    }
    
    public void testLargeQueryRoutingTable()
    {
        List<ShareFile> list = new ArrayList<ShareFile>( 10000 );
        for ( int i=0; i<10000; i++ )
        {
            list.add( new ShareFile(new File( new GUID().toString()+"/"+new GUID().toString()
                +"/"+new GUID().toString()+"/"+new GUID().toString()+"/"+new GUID().toString()  ) ) );
        }

        long start = System.currentTimeMillis();
        QueryRoutingTable routingTable = QueryRoutingTable.createLocalQueryRoutingTable( list );
        System.out.println( routingTable.getTableSize() );
        System.out.println( routingTable.getFillRatio() );
        long end = System.currentTimeMillis();
        System.out.println( (end-start) );
    }
    
    private QueryRoutingTable createRandomFilled( int size ) throws Throwable
    {
        QueryRoutingTable qrTable = new QueryRoutingTable( size );
        while( qrTable.getFillRatio() < 4 )
        {
            AccessUtils.invokeMethod( qrTable, "add", new GUID().toString() );
        }
        return qrTable;
    }
    
    private QueryMsg createDummyMsg( String searchStr )
    {
        return new QueryMsg( new GUID(), (byte)7, searchStr, null, false, 
            false, false, QueryMsg.NO_FEATURE_QUERY_SELECTOR );
    }
}