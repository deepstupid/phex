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
 *  $Id: ConnectionEngine.java 4490 2009-09-22 07:42:24Z gregork $
 */
package phex.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.common.address.*;
import phex.connection.handshake.HandshakeHandler;
import phex.connection.handshake.HandshakeStatus;
import phex.host.CaughtHostsContainer;
import phex.host.Host;
import phex.host.HostStatus;
import phex.http.*;
import phex.io.buffer.ByteBuffer;
import phex.msg.InvalidMessageException;
import phex.msg.Message;
import phex.msg.MessageProcessor;
import phex.msg.MsgHeader;
import phex.msg.vendor.CapabilitiesVMsg;
import phex.msg.vendor.MessagesSupportedVMsg;
import phex.msghandling.MessageService;
import phex.net.connection.Connection;
import phex.net.repres.PresentationManager;
import phex.MessagePrefs;
import phex.query.DynamicQueryConstants;
import phex.security.AccessType;
import phex.security.PhexSecurityManager;
import phex.peer.Peer;
import phex.util.Localizer;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * <p>A worker that handles the communication between this and another gnutella
 * node.</p>
 * <p>
 * <p>The remote node is represented as a Host object. Depending on whether the
 * host is in incoming or outgoing mode, this will perform the relevant
 * handshake negotiations. If this was an
 * outgoing connection, or during negotiations it becomes clear that the Host
 * wishes to partake in a Gnutella network, then enter a message handling loop
 * to forward all messages as necessary. This will usualy result in a message
 * being read, some bookkeeping to keep track of the message, discarding bad
 * messages and finally queuing a request with the Message Manager to pass on any
 * messages that must be generated in response.</p>
 */
public class ConnectionEngine implements ConnectionConstants {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionEngine.class);

    private final Peer peer;
    private final MessageService messageService;
    private final PhexSecurityManager securityService;
    private final Host connectedHost;
    private final Connection connection;
    /**
     * pre-allocated buffer for repeated uses.
     */
    private byte[] headerBuffer;
    private HTTPHeaderGroup headersRead;
    private HTTPHeaderGroup headersSend;

    public ConnectionEngine(Peer peer, Host connectedHost) {
        this.peer = peer;
        this.messageService = peer.getMessageService();
        this.securityService = peer.getSecurityService();
        this.connectedHost = connectedHost;
        this.connection = connectedHost.getConnection();
    }

    public void processIncomingData()
            throws IOException {
        headerBuffer = new byte[MsgHeader.DATA_LENGTH];
        try {
            while (true) {
                MsgHeader header = readHeader();

                if (header.getPayload() == 2) {
                    // http://rfc-gnutella.sourceforge.net/developer/stable/index.html#t3-2-1
                    /*
                    3.2.7. Bye (0x02) Extension Descriptor Payload

                    Fields	Optional Bye Data
                    Byte offset	0...L-1
                    Optional Bye Data
                    This is an optional field consisting in bytes of variable length, it is reserved for extensions of the current version of the protocol, to specify filters about expected Pong replies. Its maximum length is bounded by the Payload Length field of the header.
                    When used, this field SHOULD be small and agreed upon with other Gnutella servent implementors, as this field MAY be specified in a further specification of the protocol.
                    This descriptor was not specified in the original 0.4 protocol. Implementing it in servents is optional. Servents MAY safely ignore this descriptor, as it is completely compatible with all non Bye-aware 0.4 servents.

                    However a Bye-aware servent MUST set TTL=1 and Hops=0 when sending this descriptor, then it SHOULD NOT send or forward any other descriptor on the same connection path; instead it MAY wait for about 30 seconds that the connection closes (if timeout elapses, it SHOULD close the connection). During that period, the servent MAY ignore all other incoming descriptors coming from the same connection path (with the exception of another incoming Bye Descriptor which MAY be interpreted). The semantic of an sending a Bye descriptor with Hops<>0 is unknown and not defined in this document.

                    On reception, a Bye-aware servent MUST NOT forward this message; it MAY interpret the Payload to take further actions, but it SHOULD disconnect immediately from the servent which sent this descriptor. The content of the Payload is not specified in this version of the protocol (it will typically contain a NUL terminated status line that gives the reason why a servent will be disconnected, and other Optional Bye Data extensions).
                     */
                    continue;
                }

                byte[] body = MessageProcessor.readMessageBody(connection,
                        header.getDataLength());

                connectedHost.incReceivedCount();


                int ttl = header.getTTL();
                int hops = header.getHopsTaken();
                // verify valid ttl and hops data
                if (ttl < 0 || hops < 0) {
                    messageService.dropMessage(header, body,
                            "TTL or hops below 0", connectedHost);
                    continue;
                }
                // if message traveled too far already... drop it.
                int MAX_TTL = MessagePrefs.MaxNetworkTTL.get();
                if (hops > MAX_TTL) {
                    messageService.dropMessage(header, body,
                            "Hops larger then maxNetworkTTL", connectedHost);
                    continue;
                }
                // limit TTL if too high!
                if (ttl >= MAX_TTL) {
                    header.setTTL((byte) (MAX_TTL - hops));
                }

                Message message;
                try {
                    message = MessageProcessor.createMessageFromBody(
                            header, body, securityService);
                    if (message == null) { // unknown message type...
                        messageService.dropMessage(header, body,
                                "Unknown message type", connectedHost);
                        continue;
                    }
                } catch (InvalidMessageException exp) {
                    messageService.dropMessage(header, body,
                            "Invalid message: " + exp.getMessage(), connectedHost);
                    logger.warn("drop {}", exp.getMessage()); //exp.toString(), exp);
                    continue;
                }

                // count the hop and decrement TTL...
                header.countHop();

                messageService.dispatchMessage(message, connectedHost);
            }
        } catch (IOException exp) {
            logger.debug(exp.toString(), exp);
            if (connectedHost.isConnected()) {
                connectedHost.setStatus(HostStatus.ERROR, exp.getMessage());
                connectedHost.disconnect();
            }
            throw exp;
        } catch (Exception exp) {
            logger.warn(exp.toString(), exp);
            if (connectedHost.isConnected()) {
                connectedHost.setStatus(HostStatus.ERROR, exp.getMessage());
                connectedHost.disconnect();
            }
            throw new IOException("Exception occured: " + exp.getMessage());
        }
    }

    private MsgHeader readHeader()
            throws IOException {
        MsgHeader header = MessageProcessor.parseMessageHeader(connection,
                headerBuffer);
        if (header == null) {
            throw new ConnectionClosedException("Connection closed by remote host");
        }

        int length = header.getDataLength();
        if (length < 0) {
            throw new IOException("Negative body size. Disconnecting the remote host.");
        } else if (length > MessagePrefs.MaxLength.get()) {
            // Packet looks suspiciously too big.  Disconnect them.
            if (logger.isWarnEnabled()) {
                // max 256KB when over 64KB max message length
                /*byte[] body = MessageProcessor.readMessageBody(
                        connection, 262144);*/
                //String hexBody = HexConverter.toHexString(body);
                logger.warn("{} Body too big: {}", connectedHost, header);
                        //" - Body too big. Header: " + header + "\nBody(256KB): " + hexBody);
            }

            throw new IOException("Packet too big. Disconnecting the remote host.");
        }
        header.setArrivalTime(System.currentTimeMillis());

        return header;
    }

    //////////////////// Connection initialization //////////////////////////////

    public void initHostHandshake() throws IOException {
        try {
            if (connectedHost.isIncomming()) {
                initializeIncomingWith06();
            } else {
                initializeOutgoingWith06();
            }
            configureConnectionType(headersSend, headersRead);
            postHandshakeConfiguration(headersSend, headersRead);
        } finally {
            if (headersRead != null) {
                // use the connection header whether connection was ok or not
                handleXTryHeaders(headersRead);
                // give free to gc
                headersRead = null;
                headersSend = null;
            }
        }

        // Connection to remote gnutella host is completed at this point.
        connectedHost.setStatus(HostStatus.CONNECTED);
        peer.getHostService().addConnectedHost(connectedHost);

        // send UDP ping as soon as we have recognized host
        peer.getMessageService().sendUdpPing(connectedHost.getHostAddress());

        // queue first Ping msg to send.
        // add ping routing to local host to track my initial pings...
        peer.getMessageService().pingHost(connectedHost,
                MessagePrefs.TTL.get().byteValue());

        // after initial handshake ping send message supported VM.
        if (connectedHost.isVendorMessageSupported()) {
            MessagesSupportedVMsg vMsg = MessagesSupportedVMsg.getMyMsgSupported();
            connectedHost.queueMessageToSend(vMsg);

            CapabilitiesVMsg capVMsg = CapabilitiesVMsg.getMyCapabilitiesVMsg();
            connectedHost.queueMessageToSend(capVMsg);
        }
    }

    private void initializeIncomingWith06()
            throws IOException {
        // read connect headers
        headersRead = HTTPProcessor.parseHTTPHeaders(connection);
        /*if (logger.isDebugEnabled()) {
            logger.debug("{} - Connect headers: {}",
                    connectedHost, headersRead.buildHTTPHeaderString());
        }*/
        configureRemoteHost(headersRead);

        // create appropriate handshake handler that takes care about headers
        // and logic...
        HandshakeHandler handshakeHandler = HandshakeHandler.createHandshakeHandler(
                peer, connectedHost);
        HandshakeStatus myResponse = handshakeHandler.createHandshakeResponse(
                new HandshakeStatus(headersRead), false);
        headersSend = myResponse.getResponseHeaders();

        // send answer to host...
        sendStringToHost(GNUTELLA_06 + ' ' + myResponse.getStatusCode() + ' ' +
                myResponse.getStatusMessage() + "\r\n");
        String httpHeaderString = myResponse.getResponseHeaders().buildHTTPHeaderString();
        sendStringToHost(httpHeaderString);
        sendStringToHost("\r\n");

        if (myResponse.getStatusCode() != STATUS_CODE_OK) {
            throw new IOException("Connection not accepted: " +
                    myResponse.getStatusCode() + ' ' + myResponse.getStatusMessage());
        }

        HandshakeStatus inResponse = HandshakeStatus.parseHandshakeResponse(
                connection);

        /*if (logger.isDebugEnabled()) {
            logger.debug(connectedHost + " - Response Code: '"
                    + inResponse.getStatusCode() + "'.");
            logger.debug(connectedHost + " - Response Message: '"
                    + inResponse.getStatusMessage() + "'.");
            logger.debug(connectedHost + " - Response Headers: "
                    + inResponse.getResponseHeaders().buildHTTPHeaderString());
        }*/

        if (inResponse.getStatusCode() != STATUS_CODE_OK) {
            throw new IOException("Host rejected connection: " +
                    inResponse.getStatusCode() + ' ' +
                    inResponse.getStatusMessage());
        }
        headersRead.replaceHeaders(inResponse.getResponseHeaders());
    }

    private void initializeOutgoingWith06()
            throws IOException {
        connectedHost.setStatus(HostStatus.CONNECTING,
                Localizer.getString("Negotiate0_6Handshake"));

        // Send the first handshake greeting to the remote host.
        String greeting = peer.getGnutellaNetwork().getNetworkGreeting();

        String requestLine = greeting + '/' + PROTOCOL_06 + "\r\n";
        StringBuffer requestBuffer = new StringBuffer(100);
        requestBuffer.append(requestLine);

        // create appropriate handshake handler that takes care about headers
        // and logic...
        HandshakeHandler handshakeHandler = HandshakeHandler.createHandshakeHandler(
                peer, connectedHost);

        HTTPHeaderGroup handshakeHeaders =
                handshakeHandler.createOutgoingHandshakeHeaders();
        requestBuffer.append(handshakeHeaders.buildHTTPHeaderString());
        requestBuffer.append("\r\n");
        headersSend = handshakeHeaders;

        String requestStr = requestBuffer.toString();
        sendStringToHost(requestStr);

        HandshakeStatus handshakeResponse = HandshakeStatus.parseHandshakeResponse(
                connection);
        headersRead = handshakeResponse.getResponseHeaders();

        /*if (logger.isDebugEnabled()) {
            logger.debug(connectedHost + " - Response Code: '"
                    + handshakeResponse.getStatusCode() + "'.");
            logger.debug(connectedHost + " - Response Message: '"
                    + handshakeResponse.getStatusMessage() + "'.");
            logger.debug(connectedHost + " - Response Headers: "
                    + headersRead.buildHTTPHeaderString());
        }*/

        if (handshakeResponse.getStatusCode() != STATUS_CODE_OK) {
            if (handshakeResponse.getStatusCode() == STATUS_CODE_REJECTED) {
                throw new ConnectionRejectedException(
                        handshakeResponse.getStatusCode() + " "
                                + handshakeResponse.getStatusMessage());
            } else {
                throw new ConnectionRejectedException(
                        "Gnutella 0.6 connection rejected. Status: " +
                                handshakeResponse.getStatusCode() + " - " +
                                handshakeResponse.getStatusMessage());
            }
        }

        configureRemoteHost(headersRead);

        HandshakeStatus myResponse = handshakeHandler.createHandshakeResponse(
                handshakeResponse, true);
        HTTPHeaderGroup myResponseHeaders = myResponse.getResponseHeaders();
        headersSend.replaceHeaders(myResponseHeaders);
        // send answer to host...
        sendStringToHost(GNUTELLA_06 + ' ' + myResponse.getStatusCode() + ' ' +
                myResponse.getStatusMessage() + "\r\n");
        String httpHeaderString = myResponseHeaders.buildHTTPHeaderString();
        sendStringToHost(httpHeaderString);
        sendStringToHost("\r\n");

        if (myResponse.getStatusCode() != STATUS_CODE_OK) {
            throw new ConnectionRejectedException("Connection not accepted: " +
                    myResponse.getStatusCode() + ' ' + myResponse.getStatusMessage());
        }
    }

    private void configureConnectionType(HTTPHeaderGroup myHeadersSend,
                                         HTTPHeaderGroup theirHeadersRead) {
        HTTPHeader myUPHeader = myHeadersSend.getHeader(
                GnutellaHeaderNames.X_ULTRAPEER);
        HTTPHeader theirUPHeader = theirHeadersRead.getHeader(
                GnutellaHeaderNames.X_ULTRAPEER);
        if (myUPHeader == null || theirUPHeader == null) {
            connectedHost.setConnectionType(Host.CONNECTION_NORMAL);
        } else if (myUPHeader.booleanValue()) {
            if (theirUPHeader.booleanValue()) {
                connectedHost.setConnectionType(Host.CONNECTION_UP_UP);
            } else {
                connectedHost.setConnectionType(Host.CONNECTION_UP_LEAF);
            }
        } else // !myUPHeader.booleanValue()
        {
            if (theirUPHeader.booleanValue()) {
                connectedHost.setConnectionType(Host.CONNECTION_LEAF_UP);
            } else {
                connectedHost.setConnectionType(Host.CONNECTION_NORMAL);
            }
        }
    }

    private void handleXTryHeaders(HTTPHeaderGroup headers) {
        // X-Try header is not used by most servents anymore... (2003-02-25)
        // we read still read it a while though...
        // http://groups.yahoo.com/group/the_gdf/message/14316
        HTTPHeader[] hostAddresses = headers.getHeaders(
                GnutellaHeaderNames.X_TRY);
        if (hostAddresses != null) {
            handleXTryHosts(hostAddresses, true);
        }
        // for us ultrapeers have low priority other high.. since we can't connect to UP..
        hostAddresses = headers.getHeaders(
                GnutellaHeaderNames.X_TRY_ULTRAPEERS);
        if (hostAddresses != null) {
            handleXTryHosts(hostAddresses, false);
        }
    }

    private void handleXTryHosts(HTTPHeader[] xtryHostAdresses, boolean isUltrapeerList) {
        short priority;
        if (isUltrapeerList) {
            priority = CaughtHostsContainer.HIGH_PRIORITY;
        } else {
            priority = CaughtHostsContainer.NORMAL_PRIORITY;
        }
        CaughtHostsContainer hostContainer = peer.getHostService().getCaughtHostsContainer();

        for (HTTPHeader xtryHostAdress : xtryHostAdresses) {
            StringTokenizer tokenizer = new StringTokenizer(
                    xtryHostAdress.getValue(), ",");
            while (tokenizer.hasMoreTokens()) {
                String hostAddressStr = tokenizer.nextToken().trim();
                try {
                    DestAddress address = PresentationManager.getInstance()
                            .createHostAddress(hostAddressStr, DefaultDestAddress.DEFAULT_PORT);
                    AccessType access = securityService.controlHostAddressAccess(address);
                    switch (access) {
                        case ACCESS_DENIED:
                        case ACCESS_STRONGLY_DENIED:
                            // skip host address...
                            continue;
                    }
                    IpAddress ipAddress = address.getIpAddress();
                    if (!isUltrapeerList && ipAddress != null && ipAddress.isSiteLocalIP()) { // private IP have low priority except for ultrapeers.
                        priority = CaughtHostsContainer.LOW_PRIORITY;
                    }
                    hostContainer.addCaughtHost(address, priority);
                } catch (MalformedDestAddressException exp) {
                }
            }
        }
    }

    /**
     * This method uses the header fields to set attributes of the remote host
     * accordingly.
     */
    private void configureRemoteHost(HTTPHeaderGroup headers) {
        HTTPHeader header = headers.getHeader(HTTPHeaderNames.USER_AGENT);
        if (header != null) {
            if (!connectedHost.setVendor(header.getValue())) {
                return;
            }
        }

        if (connectedHost.isIncomming()) {
            header = headers.getHeader(GnutellaHeaderNames.LISTEN_IP);
            if (header == null) {
                header = headers.getHeader(GnutellaHeaderNames.X_MY_ADDRESS);
            }
            if (header != null) {
                DestAddress addi = connectedHost.getHostAddress();
                // parse port
                int port = AddressUtils.parsePort(header.getValue());
                if (port > 0) {
                    connectedHost.setHostAddress(new DefaultDestAddress(
                            addi.getIpAddress(), port));
                }
            }
        }

        header = headers.getHeader(GnutellaHeaderNames.REMOTE_IP);
        if (header != null) {
            byte[] remoteIP = AddressUtils.parseIP(header.getValue());
            if (remoteIP != null) {
                IpAddress ip = new IpAddress(remoteIP);
                DestAddress address = PresentationManager.getInstance().createHostAddress(ip, -1);
                peer.updateLocalAddress(address);
            }
        }

        header = headers.getHeader(GnutellaHeaderNames.X_QUERY_ROUTING);
        if (header != null) {
            try {
                float version = Float.parseFloat(header.getValue());
                if (version >= 0.1f) {
                    connectedHost.setQueryRoutingSupported(true);
                }
            } catch (NumberFormatException e) { // no qr supported... don't care
            }
        }

        header = headers.getHeader(GnutellaHeaderNames.X_UP_QUERY_ROUTING);
        if (header != null) {
            try {
                float version = Float.parseFloat(header.getValue());
                if (version >= 0.1f) {
                    connectedHost.setUPQueryRoutingSupported(true);
                }
            } catch (NumberFormatException e) { // no qr supported... don't care
            }
        }

        header = headers.getHeader(GnutellaHeaderNames.X_DYNAMIC_QUERY);
        if (header != null) {
            try {
                float version = header.floatValue();
                if (version >= 0.1f) {
                    connectedHost.setDynamicQuerySupported(true);
                }
            } catch (NumberFormatException e) {// no dynamiy query supported... don't care
            }
        }

        byte maxTTL = headers.getByteHeaderValue(GnutellaHeaderNames.X_MAX_TTL,
                DynamicQueryConstants.DEFAULT_MAX_TTL);
        connectedHost.setMaxTTL(maxTTL);

        int degree = headers.getIntHeaderValue(GnutellaHeaderNames.X_DEGREE,
                DynamicQueryConstants.NON_DYNAMIC_QUERY_DEGREE);
        connectedHost.setUltrapeerDegree(degree);
    }

    private void postHandshakeConfiguration(HTTPHeaderGroup myHeadersSend,
                                            HTTPHeaderGroup theirHeadersRead)
            throws IOException

    {
        if (myHeadersSend.isHeaderValueContaining(HTTPHeaderNames.ACCEPT_ENCODING,
                "deflate") && theirHeadersRead.isHeaderValueContaining(
                HTTPHeaderNames.CONTENT_ENCODING, "deflate")) {
            connectedHost.activateInputInflation();
        }
        if (theirHeadersRead.isHeaderValueContaining(HTTPHeaderNames.ACCEPT_ENCODING,
                "deflate") && myHeadersSend.isHeaderValueContaining(
                HTTPHeaderNames.CONTENT_ENCODING, "deflate")) {
            connectedHost.activateOutputDeflation();
        }


        HTTPHeader header = theirHeadersRead.getHeader(
                GnutellaHeaderNames.VENDOR_MESSAGE);
        if (header != null && !header.getValue().isEmpty()) {
            connectedHost.setVendorMessageSupported(true);
        }

        header = theirHeadersRead.getHeader(
                GnutellaHeaderNames.GGEP);
        if (header != null && !header.getValue().isEmpty()) {
            connectedHost.setGgepSupported(true);
        } else {
            connectedHost.setGgepSupported(false);
        }
    }

    private void sendStringToHost(String str)
            throws IOException {
        //logger.debug("{} - Send: {}", connectedHost, str);
        byte[] bytes = str.getBytes("ISO8859-1");
        connection.write(ByteBuffer.wrap(bytes));
        connection.flush();
    }
}
