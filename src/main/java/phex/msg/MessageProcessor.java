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
 *  --- CVS Information ---
 *  $Id: MessageProcessor.java 4479 2009-07-26 13:37:12Z gregork $
 */
package phex.msg;

import phex.common.log.NLogger;
import phex.msg.vendor.VendorMsg;
import phex.net.connection.Connection;
import phex.MessagePrefs;
import phex.peer.Peer;
import phex.security.PhexSecurityManager;
import phex.util.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


public class MessageProcessor {

    private final Peer peer;

    private MessageProcessor(Peer peer) {
        this.peer = peer;
    }

    public Message parseMessage(Connection connection, PhexSecurityManager securityService)
            throws IOException, InvalidMessageException {
        MsgHeader header = parseMessageHeader(connection, new byte[MsgHeader.DATA_LENGTH]);
        if (header == null) {
            throw new IOException("Connection closed by remote host");
        }
        int length = header.getDataLength();
        if (length < 0) {
            throw new IOException("Negative body size. Drop.");
        } else if (length > peer.messagePrefs.MaxLength.get()) {
            throw new IOException("Packet too big (" + length + "). Drop.");
        }
        return parseMessage(header, connection, securityService);
    }

    public Message parseMessage(MsgHeader header, Connection connection,
                                       PhexSecurityManager securityService)
            throws IOException, InvalidMessageException {
        return parseMessage(header, connection.getInputStream(), securityService);
    }

    public Message parseMessage(MsgHeader header, InputStream inStream,
                                       PhexSecurityManager securityService)
            throws IOException, InvalidMessageException {
        byte[] body = readMessageBody(inStream, header.getDataLength());

        Message message = createMessageFromBody(header, body, securityService);

        return message;
    }

    public Message createMessageFromBody(MsgHeader header, byte[] body,
                                                PhexSecurityManager securityService)
            throws InvalidMessageException {
        switch (header.getPayload()) {
            case MsgHeader.PING_PAYLOAD:
                return new PingMsg(header, body);
            case MsgHeader.PONG_PAYLOAD:
                return new PongMsg(header, body, securityService);
            case MsgHeader.PUSH_PAYLOAD:
                return new PushRequestMsg(header, body);
            case MsgHeader.QUERY_HIT_PAYLOAD:
                return new QueryResponseMsg(header, body, securityService);
            case MsgHeader.QUERY_PAYLOAD:
                return new QueryMsg(peer, header, body);
            case MsgHeader.ROUTE_TABLE_UPDATE_PAYLOAD:
                return RouteTableUpdateMsg.parseMessage(header, body);
            case MsgHeader.VENDOR_MESSAGE_PAYLOAD:
            case MsgHeader.STANDARD_VENDOR_MESSAGE_PAYLOAD:
                return VendorMsg.parseMessage(header, body);
            default:
                // unknown message type return null...
        }
        return null;
    }

    public static byte[] readMessageBody(Connection connection, int dataLength)
            throws IOException {
        return readMessageBody(connection.getInputStream(), dataLength);
    }

    public static byte[] readMessageBody(InputStream inStream, int dataLength)
            throws IOException {
        final byte[] body = new byte[dataLength];
        inStream.read(body);
        return body;
//        byte[] body = new byte[ dataLength ];
//
//        int dataRead = 0;
//        int len;
//        int readSize;
//        while ( dataRead < dataLength )
//        {
//            readSize = Math.min( dataLength - dataRead, 1024 );
//            len = inStream.read( body, dataRead, readSize );
//            if ( len == -1 )
//            {
//                throw new IOException( "Connection closed by remote host" );
//            }
//            dataRead += len;
//        }
//
//        return body;
    }

    public static MsgHeader parseMessageHeader(Connection connection,
                                               byte[] buffer)
            throws IOException {
        return parseMessageHeader(connection.getInputStream(), buffer);
    }

    public static MsgHeader parseMessageHeader(InputStream inStream,
                                               byte[] buffer)
            throws IOException {
        inStream.read(buffer, 0, MsgHeader.DATA_LENGTH);
        return parseMessageHeader(ByteBuffer.wrap(buffer));

//        int lenRead = 0;
//        int len;
//        while ( lenRead < MsgHeader.DATA_LENGTH )
//        {
//            len = inStream.read( buffer, lenRead, MsgHeader.DATA_LENGTH - lenRead);
//            if ( len == -1 )
//            {
//                return null;
//            }
//            lenRead += len;
//        }
//        return parseMessageHeader( ByteBuffer.wrap( buffer ) );
    }

    public static MsgHeader parseMessageHeader(ByteBuffer buffer) {
        byte[] guidArr = new byte[GUID.DATA_LENGTH];
        buffer.get(guidArr);

        byte payload = buffer.get();
        byte ttl = buffer.get();
        byte hops = buffer.get();

        byte[] lenArr = new byte[4];
        buffer.get(lenArr);
        int dataLength = IOUtil.deserializeIntLE(lenArr, 0);

        MsgHeader header = new MsgHeader(new GUID(guidArr), payload, ttl, hops,
                dataLength);

        return header;
    }

    /**
     * creates a body for a message from a byte array given its header
     *
     * @param MsgHeader
     * @param data      byte array
     * @param offset
     * @return body in byte array or null on failure
     * @author Madhu
     */
    public static byte[] createBody(MsgHeader MsgHdr, byte data[], int offset) {
        int bodyLength = MsgHdr.getDataLength();
        byte[] body = new byte[bodyLength];

        if (bodyLength > (data.length - offset)) {
            NLogger.warn(MessageProcessor.class, " Message Data length greater" +
                    " then that of given byte array " + new String(data)
            );
            return null;
        }

        System.arraycopy(data, offset, body, 0, bodyLength);
        return body;
    }
}