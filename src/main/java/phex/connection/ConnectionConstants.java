/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2007 Phex Development Group
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
 *  $Id: ConnectionConstants.java 3853 2007-06-30 15:02:22Z gregork $
 */
package phex.connection;

public interface ConnectionConstants {
    String PROTOCOL_06 = "0.6";
    String GNUTELLA_06 = "GNUTELLA/0.6";
    String GNUTELLA_06_503 = "GNUTELLA/0.6 503";
    String GNUTELLA_OK_06 = "GNUTELLA/0.6 200 OK";

    String GNUTELLA_CONNECT = "GNUTELLA CONNECT";
    String GNUTELLA_PHEX_CONNECT = "GNUTELLA PCONNECT";

    int STATUS_CODE_OK = 200;
    int STATUS_CODE_REJECTED = 503;

    String STATUS_MESSAGE_OK = "OK";
    String STATUS_MESSAGE_BUSY = "I am busy.";
    String STATUS_MESSAGE_SHIELDED_LEAF =
            "I am a shielded leaf node";
    String STATUS_MESSAGE_ACCEPT_ONLY_UP =
            "I accept only Ultrapeers";
}