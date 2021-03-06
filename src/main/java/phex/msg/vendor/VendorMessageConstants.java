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
 *  $Id: VendorMessageConstants.java 4318 2008-11-30 22:50:47Z gregork $
 */
package phex.msg.vendor;

/**
 *
 */
public interface VendorMessageConstants {
    // VENDOR NULL
    int SUBSELECTOR_MESSAGES_SUPPORTED = 0;
    int SUBSELECTOR_CAPABILITIES = 10;

    // VENDOR BEAR
    int SUBSELECTOR_HOPS_FLOW = 4;
    int SUBSELECTOR_HORIZON_PING = 5;
    int SUBSELECTOR_TCP_CONNECT_BACK = 7;

    //  VENDOR LIME
    int SUBSELECTOR_MESSAGE_ACK = 11;
    int SUBSELECTOR_OOB_REPLY_COUNT = 12;
    int SUBSELECTOR_PUSH_PROXY_REQUEST = 21;
    int SUBSELECTOR_PUSH_PROXY_ACKNOWLEDGEMENT = 22;
    int SUBSELECTOR_UDP_HEAD_PING = 23;


    byte[] VENDORID_NULL = {0, 0, 0, 0};
    byte[] VENDORID_BEAR = {'B', 'E', 'A', 'R'};
    byte[] VENDORID_LIME = {'L', 'I', 'M', 'E'};
}
