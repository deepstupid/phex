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
package phex.common;

import junit.framework.TestCase;
import phex.common.address.AddressUtils;
import phex.common.address.DefaultDestAddress;
import phex.common.address.DestAddress;
import phex.prefs.core.PhexCorePrefs;
import phex.security.PhexSecurityManager;
import phex.servent.Servent;

import java.util.Collection;

public class AltLocContainerTest extends TestCase
{
    private static final DestAddress localAddress = new DefaultDestAddress( "127.0.0.1", 6347 );
    private static final PhexSecurityManager securityService = new PhexSecurityManager();
    private static final String DEFAULT_URN = "urn:sha1:PLSTHIPQGSSATS5FJUPAKUZWUGYQYPFB";
    private static final String DEFAULT_ALTLOC = "http://1.1.1.1:6347/uri-res/N2R?"+DEFAULT_URN+" 2002-04-30T08:30:00Z";
    private static final String ALT1_ALTLOC = "http://2.2.2.2:6347/uri-res/N2R?"+DEFAULT_URN+" 2002-04-30T08:30:00Z";
    
    private int nextAltLocCounter;
    private AltLocContainer testContainer;
    
    @Override
    public void setUp()
    {
        Phex.initialize();
        /// should not depend on these
        PhexCorePrefs.init();
//        PhexGuiPrefs.init();
//        Localizer.initialize( InterfacePrefs.LocaleName.get() );
        Servent servent = Servent.getInstance();
        
        
        nextAltLocCounter = 0;
        
        testContainer = new AltLocContainer( new URN( DEFAULT_URN ) );
        assertEquals( 0, testContainer.getSize() );
    }

    public void testAddContainer()
    {
        for ( int i = 0; i < 10; i++ )
        {
            String altLocStr = "http://1.1.1." + i
                + ":6347/uri-res/N2R?"+DEFAULT_URN+" 2002-04-30T08:30:"+ i % 60 + "Z";
            AlternateLocation loc = AlternateLocation.parseUriResAltLoc( altLocStr,
                securityService );
            assertNotNull( loc );
            testContainer.addAlternateLocation( loc );
        }
        assertEquals( 10, testContainer.getSize() );
        
        AltLocContainer destContainer = new AltLocContainer( new URN( DEFAULT_URN ) );
        destContainer.addContainer( testContainer );
        assertEquals( 10, destContainer.getSize() );
        
        Collection<DestAddress> testAltLocs = testContainer.getAltLocsForExport( localAddress );
        Collection<DestAddress> destAltLocs = destContainer.getAltLocsForExport( localAddress );
        
        assertTrue( testAltLocs.containsAll( destAltLocs ) );
        assertTrue( destAltLocs.containsAll( testAltLocs ) );
    }

    public void testAddAlternateLocation()
    {
        for ( int i = 0; i < 10; i++ )
        {
            String altLocStr = "http://1.1.1." + i
                + ":6347/uri-res/N2R?"+DEFAULT_URN+" 2002-04-30T08:30:"+ i % 60 + "Z";
            AlternateLocation loc = AlternateLocation.parseUriResAltLoc( altLocStr,
                securityService );
            assertNotNull( loc );
            testContainer.addAlternateLocation( loc );
            assertEquals( Math.min( i + 1, AltLocContainer.MAX_ALT_LOC_COUNT ), 
                testContainer.getSize() );
        }
    }
    
    public void testDoubleAddAlternateLocation()
    {
        AlternateLocation loc = AlternateLocation.parseUriResAltLoc( DEFAULT_ALTLOC, 
            securityService );
        assertNotNull( loc );
        testContainer.addAlternateLocation( loc );

        assertEquals( 1, testContainer.getSize() );
        
        loc = AlternateLocation.parseUriResAltLoc( DEFAULT_ALTLOC, 
            securityService );
        assertNotNull( loc );
        testContainer.addAlternateLocation( loc );

        assertEquals( 1, testContainer.getSize() );
    }
    
    public void testLRUAddAlternateLocation()
    {
        AlternateLocation persistentLoc = AlternateLocation.parseUriResAltLoc( ALT1_ALTLOC,
            securityService );
        assertNotNull( persistentLoc );
        testContainer.addAlternateLocation( persistentLoc );
        
        AlternateLocation loc;
        for ( int i = 0; i < AltLocContainer.MAX_ALT_LOC_COUNT - 1; i++ )
        {
            loc = generateNextUniqueAltLoc();
            assertNotNull( loc );
            testContainer.addAlternateLocation( loc );
        }
        Collection<DestAddress> containingSet = testContainer.getAltLocsForExport( localAddress );
        assertTrue( containingSet.contains( persistentLoc.getHostAddress() ) );
        testContainer.addAlternateLocation( persistentLoc );
        for ( int i = 0; i < AltLocContainer.MAX_ALT_LOC_COUNT - 1; i++ )
        {
            loc = generateNextUniqueAltLoc();
            assertNotNull( loc );
            testContainer.addAlternateLocation( loc );
        }
        containingSet = testContainer.getAltLocsForExport( localAddress );
        assertTrue( containingSet.contains( persistentLoc.getHostAddress() ) );
        
        for ( int i = 0; i < AltLocContainer.MAX_ALT_LOC_COUNT * 2; i++ )
        {
            loc = generateNextUniqueAltLoc();
            assertNotNull( loc );
            testContainer.addAlternateLocation( loc );
        }
        Collection<DestAddress> droppedSet = testContainer.getAltLocsForExport( localAddress );
        assertFalse( droppedSet.contains( persistentLoc.getHostAddress() ) );
        
    }

    public void testRemoveAlternateLocation()
    {
        AlternateLocation loc = AlternateLocation.parseUriResAltLoc( DEFAULT_ALTLOC,
            securityService );
        assertNotNull( loc );
        testContainer.addAlternateLocation( loc );
        assertEquals( 1, testContainer.getSize() );
        
        AlternateLocation loc2 = AlternateLocation.parseUriResAltLoc( DEFAULT_ALTLOC,
            securityService );
        assertNotNull( loc2 );
        testContainer.removeAlternateLocation( loc2 );
        assertEquals( 0, testContainer.getSize() );
    }

    public void testIsEmpty()
    {
        assertTrue( testContainer.isEmpty() );
        
        AlternateLocation loc = AlternateLocation.parseUriResAltLoc( DEFAULT_ALTLOC,
            securityService );
        assertNotNull( loc );
        testContainer.addAlternateLocation( loc );
        assertFalse( testContainer.isEmpty() );
    }

//    public void testParseCompactIpAltLocFromHeaders()
//    {
//        Set altLocSet = new HashSet();
//        HTTPHeader header = testContainer.getAltLocHTTPHeaderForAddress(
//            GnutellaHeaderNames.X_ALT,
//            new DefaultDestAddress( "1.1.1.10", 6347 ), altLocSet );
//        assertNotNull( header );
//        assertEquals( 10, AltLocContainer.parseCompactIpAltLocFromHeaders(
//            new HTTPHeader[] { header }, new URN( DEFAULT_URN ), securityService ).size() );
//        assertEquals( 10, altLocSet.size() );
//        
//        altLocSet.clear();
//        header = testContainer.getAltLocHTTPHeaderForAddress(
//            GnutellaHeaderNames.X_ALT,
//            new DefaultDestAddress( "2.1.1.1", 6347 ), altLocSet );
//        assertNotNull( header );
//        assertEquals( 10, AltLocContainer.parseCompactIpAltLocFromHeaders(
//            new HTTPHeader[]
//            {
//                header
//            }, new URN( DEFAULT_URN ), securityService ).size() );
//        assertEquals( 10, altLocSet.size() );
//        
//        // check that only one alt loc is returned when only one is available.
//        // and check if alt locs for a defined HostAddress are not returned.
//        testContainer = new AltLocContainer( new URN( DEFAULT_URN ) );
//        AlternateLocation loc = AlternateLocation
//            .parseUriResAltLoc( DEFAULT_URN, securityService );
//        assertNotNull( loc );
//        testContainer.addAlternateLocation( loc );
//
//        altLocSet.clear();
//        header = testContainer.getAltLocHTTPHeaderForAddress(
//            GnutellaHeaderNames.X_ALT,
//            new DefaultDestAddress( "1.1.1.1", 6347 ), altLocSet );
//        assertNull( header );
//
//        altLocSet.clear();
//        header = testContainer.getAltLocHTTPHeaderForAddress(
//            GnutellaHeaderNames.X_ALT,
//            new DefaultDestAddress( "2.1.1.1", 6347 ), altLocSet );
//        assertEquals( 1, AltLocContainer.parseCompactIpAltLocFromHeaders(
//            new HTTPHeader[]
//            {
//                header
//            }, new URN( DEFAULT_URN ), securityService ).size() );
//        assertEquals( 1, altLocSet.size() );
//    }

//    public void testAlternateLocationContainer() throws Exception
//    {
//        AltLocContainer container = new AltLocContainer( new URN( urnStr ) );
//
//
//        // check if requesting alt loc for same candidate again will not return
//        // same alt locs...
//        header = container.getAltLocHTTPHeaderForAddress(
//            GnutellaHeaderNames.X_ALT,
//            new DefaultDestAddress( "2.1.1.1", 6347 ), altLocSet );
//        assertNull( header );
//        assertEquals( 1, altLocSet.size() );
//    }

    private AlternateLocation generateNextUniqueAltLoc()
    {
        nextAltLocCounter++;
        String ip = AddressUtils.ip2string( nextAltLocCounter );
        AlternateLocation loc = AlternateLocation.parseUriResAltLoc( "http://"+ip+":6347/uri-res/N2R?"+DEFAULT_URN,
            securityService );
        return loc;
    }
}
