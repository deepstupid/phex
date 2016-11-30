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
 *  $Id: AltLocContainer.java 4403 2009-03-18 22:19:54Z gregork $
 */
package phex.common;

import org.apache.commons.collections.map.LinkedMap;
import phex.common.address.DestAddress;
import phex.common.log.NLogger;
import phex.http.HTTPHeader;
import phex.security.PhexSecurityManager;
import phex.xml.sax.share.DAlternateLocation;

import java.util.*;

/**
 * A AlternateLocationContainer that helps holder to add, sort and access
 * AlternateLocations.
 */
public class AltLocContainer extends LinkedMap {
    public static final int MAX_ALT_LOC_COUNT = 100;
    public static final int MAX_ALT_LOC_FOR_QUERY_COUNT = 10;
    
    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    /**
     * The urn each alternate location must match to be accepted.
     */
    private URN urn;

    public AltLocContainer( URN urn ) {
        super();

        if( urn == null )
        {
            throw new NullPointerException( "URN must be provided" );
        }
        this.urn = urn;
    }

    public void addContainer( AltLocContainer cont )
    {
        if ( cont.urn == null || !cont.urn.equals( urn ) )
        {
            // dont add container with wrong or not existing sha1.
            // This is a implementation error.
            throw new IllegalArgumentException(
                "Trying to add container with not matching urns" );

        }

        // this check prevents a NullPointerException during putAll();

        synchronized(this)
        {
            for( Object tempAltLoc : cont.keySet() )
            {
                addAlternateLocation( (AlternateLocation) tempAltLoc);
            }
        }
    }

    public void addAlternateLocation( AlternateLocation altLoc )
    {
        URN altLocURN = altLoc.getURN();
        if ( altLocURN == null || !altLocURN.equals( urn ) )
        {// dont add alt location with wrong or not existing sha1
            NLogger.warn( AltLocContainer.class,
                "Cant add alt-location with not matching URN to container." );
            assert false : "Cant add alt-location with not matching URN to container.";
            return;
        }

        synchronized(this)
        {
            // thanks to AlternateLocation hashCode implementation the map ensures
            // that each alt loc is only once present. The remove operation of a 
            // possible duplicate alt loc is required here to update the internal 
            // order of elements. This ensures that alt loc not seen (put) for a 
            // long time get removed from the map.
            this.remove( altLoc );
            this.put( altLoc, PRESENT );
            // make sure we have not more then MAX_ALT_LOC_COUNT alt locations.
            if ( this.size() > MAX_ALT_LOC_COUNT )
            {// drop last element
                Object firstKey = this.firstKey();
                super.remove( firstKey );
            }
        }
    }
    
    public void removeAlternateLocation( AlternateLocation altLoc )
    {
        URN altLocURN = altLoc.getURN();
        if ( altLocURN == null || !altLocURN.equals( urn ) )
        {// dont remove alt location with wrong or not existing sha1
            NLogger.warn( AltLocContainer.class,
                "Cant remove alt-location with not matching URN from container." );
            assert false : "Cant remove alt-location with not matching URN from container.";
            return;
        }
        synchronized(this)
        {
            super.remove( altLoc );
        }
    }

    //TODO return a List-wrapping set since the keys are already unique
    public Collection<DestAddress> getAltLocsForExport( DestAddress localAddress )
    {
        if ( isEmpty() )
        {
            return Collections.emptySet();
        }
        Set s = this.keySet();
        Iterator<AlternateLocation> iterator = s.iterator();
        Set<DestAddress> result = new HashSet<>();
        while( iterator.hasNext() )
        {
            AlternateLocation altLoc = iterator.next();
            DestAddress destAddress = altLoc.getHostAddress();
            if ( !destAddress.isIpHostName() )
            {
                continue;
            }
            if ( destAddress.isLocalHost( localAddress ) )
            {
                continue;
            }
            result.add(destAddress);
        }
        return result;
    }
    
    /**
     * Returns a DestAddress array of alt locs for the query response record.
     * 
     * @return a DestAddress array of alt locs
     */
    public Set<DestAddress> getAltLocForQueryResponseRecord( DestAddress localAddress )
    {
        if ( isEmpty() )
        {
            return Collections.emptySet();
        }
        synchronized (this)
        {
            Iterator<AlternateLocation> iterator = this.keySet().iterator();
            Set<DestAddress> result = new HashSet<>();
            while( iterator.hasNext() && result.size() < MAX_ALT_LOC_FOR_QUERY_COUNT )
            {
                AlternateLocation altLoc = iterator.next();
                DestAddress destAddress = altLoc.getHostAddress();
                if ( !destAddress.isIpHostName() )
                {
                    continue;
                }
                if ( destAddress.isLocalHost( localAddress ) )
                {
                    continue;
                }
                result.add(destAddress);
            }
            return result;
        }
    }

    /**
     * Returns a HTTPHeader array for this host address. This will include all
     * alternate locations except the one that equals the host address and the
     * one contained in the sendAltLocList. The newly send alt locs are added
     * to the sendAltLocList inside this call.
     * 
     * @param headerName the name of the http header to generate, use X-ALT or X-NAlt.
     * @param hostAddress the host address that gets the alternate locations.
     * @param sendAltLocSet a list of already send alt locs.
     * @return a HTTPHeader array for this host address
     */
    public HTTPHeader getAltLocHTTPHeaderForAddress( String headerName,
        DestAddress hostAddress, Set<AlternateLocation> sendAltLocSet )     {
        if ( isEmpty() )
        {
            return null;
        }
        int count = 0;
        StringBuffer headerValue = new StringBuffer();
        synchronized (this)
        {
            Iterator<AlternateLocation> iterator = keySet().iterator();
            while( iterator.hasNext() )
            {
                AlternateLocation altLoc = iterator.next();
                
                // filter out alt locations to given host address..
                if ( hostAddress.getHostName().equals( altLoc.getHostAddress().getHostName() ) )
                {
                    continue;
                }
                // filter out already send alt locs...
                if ( sendAltLocSet.contains( altLoc ) )
                {
                    continue;
                }
                
                if ( count > 0 )
                {
                    headerValue.append(',');
                }
                headerValue.append( altLoc.getHTTPString() );
                sendAltLocSet.add( altLoc );
                count ++;
                if ( count == 10 )
                {
                    break;
                }
            }
        }
        if ( headerValue.length() > 0 )
        {
            HTTPHeader altLocHeader = new HTTPHeader( headerName,
                headerValue.toString() );
            return altLocHeader;
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns the number of alternate locations in the container.
     * @return the number of alternate locations in the container.
     */
    public synchronized int getSize()
    {

            return size();

    }



    public void createDAlternateLocationList( List<DAlternateLocation> list )
    {

        synchronized(this) {
            Iterator<AlternateLocation> iterator = this.keySet().iterator();
            while (iterator.hasNext()) {
                AlternateLocation altLoc = iterator.next();
                DAlternateLocation dAltLoc = new DAlternateLocation();
                dAltLoc.setHostAddress(altLoc.getHostAddress().getFullHostName());
                dAltLoc.setUrn(altLoc.getURN().getAsString());
                list.add(dAltLoc);
            }
        }
    }

    /**
     * Returns a string representation of the object.
     * @return a string representation of the object.
     */
    @Override
    public synchronized String toString()
    {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Alt-Locations(SHA1: ");
        stringBuffer.append( urn.getAsString() );
        stringBuffer.append(")=[ ");
        if ( this != null )
        {
            Iterator<AlternateLocation> iterator = this.keySet().iterator();
            AlternateLocation altLoc;
            while( iterator.hasNext() )
            {
                altLoc = iterator.next();
                stringBuffer.append( altLoc.toString() );
                stringBuffer.append(", ");
            }
        }
        stringBuffer.append( " ]" );
        return stringBuffer.toString();
    }


    
    /**
     * Parses AlternateLocations from the values of the given http headers and
     * returns a List containing the results. 
     * @param headers HTTPHeaderGroup to parse the AlternateLocations from
     * @return List containing all AlternateLocations found.
     */
    public static List<AlternateLocation> parseUriResAltLocFromHeaders( HTTPHeader[] headers, 
        PhexSecurityManager securityService )
    {
        if ( headers.length == 0 )
        {
            return Collections.emptyList();
        }
        List<AlternateLocation> altLocList = new ArrayList<>();
        StringTokenizer tokenizer;
        for ( HTTPHeader header : headers )
        {
            tokenizer = new StringTokenizer( header.getValue(), ",");

            while( tokenizer.hasMoreTokens() )
            {
                try
                {
                    String altLocationStr = tokenizer.nextToken().trim();
                    AlternateLocation altLocation = AlternateLocation.parseUriResAltLoc(
                        altLocationStr, securityService );
                    if ( altLocation == null )
                    {
                        continue;
                    }
                    altLocList.add( altLocation );
                }
                // TODO filter this out
                // this is currently just to make sure we have no untested error cases.
                // and the download will not fail because of this.
                catch ( Exception exp )
                {
                    NLogger.error( AltLocContainer.class, exp, exp );
                }
            }
        }
        return altLocList;
    }
    
    /**
     * Parses AlternateLocations from the values of the given http headers and
     * returns a List containing the results. 
     * @param headers HTTPHeaderGroup to parse the AlternateLocations from
     * @return List containing all AlternateLocations found.
     */
    public static List<AlternateLocation> parseCompactIpAltLocFromHeaders( HTTPHeader[] headers, URN urn,
        PhexSecurityManager securityService )
    {
        if ( headers.length == 0 )
        {
            return Collections.emptyList();
        }
        List<AlternateLocation> altLocList = new ArrayList<>();
        StringTokenizer tokenizer;
        for ( HTTPHeader header : headers )
        {
            tokenizer = new StringTokenizer( header.getValue(), ",");

            while( tokenizer.hasMoreTokens() )
            {
                try
                {
                    String altLocationStr = tokenizer.nextToken().trim();
                    if ( altLocationStr.startsWith( "tls=" ) )
                    {// skip tls marking.
                        continue;
                    }
                    AlternateLocation altLocation = AlternateLocation.parseCompactIpAltLoc(
                        altLocationStr, urn, securityService );
                    if ( altLocation == null )
                    {
                        continue;
                    }
                    altLocList.add( altLocation );
                }
                // TODO filter this out
                // this is currently just to make sure we have no untested error cases.
                // and the download will not fail because of this.
                catch ( Exception exp )
                {
                    NLogger.error( AltLocContainer.class, exp, exp );
                }
            }
        }
        return altLocList;
    }
}