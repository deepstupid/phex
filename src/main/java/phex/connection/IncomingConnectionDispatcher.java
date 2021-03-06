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
 *  $Id: IncomingConnectionDispatcher.java 4379 2009-02-21 20:49:22Z gregork $
 */
package phex.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.common.address.DestAddress;
import phex.common.bandwidth.BandwidthController;
import phex.download.PushHandler;
import phex.host.Host;
import phex.host.HostStatus;
import phex.host.NetworkHostsContainer;
import phex.http.HTTPMessageException;
import phex.http.HTTPProcessor;
import phex.http.HTTPRequest;
import phex.io.buffer.BufferCache;
import phex.msg.GUID;
import phex.net.connection.Connection;
import phex.net.repres.SocketFacade;
import phex.peer.Peer;
import phex.share.HttpRequestDispatcher;
import phex.util.GnutellaInputStream;
import phex.util.IOUtil;
import phex.util.URLCodecUtils;

import java.io.IOException;

/**
 * If during negotiation it is clear that the remote
 * host has connected to obtain data via a GET request or to deliver data in
 * response to a push, then the worker delegates this on.
 */
public class IncomingConnectionDispatcher implements Runnable {
    public static final String GET_REQUEST_PREFIX = "GET ";
    public static final String HEAD_REQUEST_PREFIX = "HEAD ";
    public static final String GIV_REQUEST_PREFIX = "GIV ";
    public static final String CHAT_REQUEST_PREFIX = "CHAT ";
    public static final String URI_DOWNLOAD_PREFIX = "PHEX_URI ";
    public static final String MAGMA_DOWNLOAD_PREFIX = "PHEX_MAGMA ";
    public static final String RSS_DOWNLOAD_PREFIX = "PHEX_RSS ";
    private static final Logger logger = LoggerFactory.getLogger(
            IncomingConnectionDispatcher.class);
    private final Peer peer;
    private final SocketFacade socket;

    public IncomingConnectionDispatcher(SocketFacade socket, Peer peer) {
        this.socket = socket;
        this.peer = peer;
    }

    public void run() {
        GnutellaInputStream gInStream = null;
        try {
            socket.setSoTimeout(peer.netPrefs.TcpRWTimeout.get());
            BandwidthController bwController = peer.getBandwidthService()
                    .getNetworkBandwidthController();
            Connection connection = new Connection(socket, bwController);


//          ByteBuffer buffer = ByteBuffer.allocate( BufferSize._2K );
//          connection.read( buffer );
//          buffer.flip();
//          StringBuilder requestLineBuilder = new StringBuilder( 128 );
//          boolean hasLine = buffer.readLine( requestLineBuilder );
//          if ( !hasLine )
//          {
//              throw new IOException( "Disconnected from remote host during handshake" );
//          }
//          
//          String requestLine = requestLineBuilder.toString();

            String requestLine = connection.readLine();
            if (requestLine == null) {
                throw new IOException("Disconnected from remote host during handshake");
            }
            logger.debug("ConnectionRequest {}", requestLine);

            DestAddress localAddress = peer.getLocalAddress();
            String greeting = peer.getGnutellaNetwork().getNetworkGreeting();
            if (requestLine.startsWith(greeting + '/')) {
                handleGnutellaRequest(connection);
            }
            // used from PushWorker
            else if (requestLine.startsWith(GET_REQUEST_PREFIX)
                    || requestLine.startsWith(HEAD_REQUEST_PREFIX)) {
                if (!peer.getOnlineStatus().isTransfersOnline()
                        && !socket.getRemoteAddress().isLocalHost(localAddress)) {
                    throw new IOException("Transfers not connected.");
                }
                // requestLine = GET /get/1/foo doo.txt HTTP/1.1
                // browse host request = GET / HTTP/1.1
                // URN requestLine = GET /uri-res/N2R?urn:sha1:PLSTHIPQGSSZTS5FJUPAKUZWUGYQYPFB HTTP/1.0
                HTTPRequest httpRequest = HTTPProcessor.parseHTTPRequest(requestLine);
                HTTPProcessor.parseHTTPHeaders(httpRequest, connection);
                logger.debug("{} Request: {}", httpRequest.getRequestMethod(), httpRequest.buildHTTPRequestString());
                if (httpRequest.isGnutellaRequest()) {
                    // file upload request
                    peer.getUploadService().handleUploadRequest(
                            connection, httpRequest);
                } else {
                    // other requests like browse host..
                    new HttpRequestDispatcher(peer).httpRequestHandler(
                            connection, httpRequest);
                }
            }
            // used when requesting push transfer
            else if (requestLine.startsWith(GIV_REQUEST_PREFIX)) {
                if (!peer.getOnlineStatus().isTransfersOnline()
                        && !socket.getRemoteAddress().isLocalHost(localAddress)) {
                    throw new IOException("Transfers not connected.");
                }
                handleIncommingGIV(requestLine);
            }
            // used when requesting chat connection
            else if (requestLine.startsWith(CHAT_REQUEST_PREFIX)) {
                if (!peer.getOnlineStatus().isNetworkOnline()
                        && !socket.getRemoteAddress().isLocalHost(localAddress)) {
                    throw new IOException("Network not connected.");
                }
                DestAddress address = socket.getRemoteAddress();
                logger.debug("Chat request from: {}", address);
                peer.getChatService().acceptChat(connection);
            } else if (requestLine.startsWith(URI_DOWNLOAD_PREFIX)) {
                handleIncommingUriDownload(requestLine);
            } else if (requestLine.startsWith(MAGMA_DOWNLOAD_PREFIX)) {
                handleIncommingMagmaDownload(requestLine);
            } else if (requestLine.startsWith(RSS_DOWNLOAD_PREFIX)) {
                handleIncommingRSSDownload(requestLine);
            } else {
                throw new IOException("Unknown connection request: "
                        + requestLine);
            }
        } catch (HTTPMessageException | IOException exp) {
            logger.debug(exp.toString(), exp);
            IOUtil.closeQuietly(gInStream);
            IOUtil.closeQuietly(socket);
        } catch (Exception exp) {// catch all thats left...
            logger.error(exp.toString(), exp);
            IOUtil.closeQuietly(gInStream);
            IOUtil.closeQuietly(socket);
        }
    }

    private void handleGnutellaRequest(Connection connection)
            throws IOException {
        DestAddress localAddress = peer.getLocalAddress();
        if (!peer.getOnlineStatus().isNetworkOnline()
                && !socket.getRemoteAddress().isLocalHost(localAddress)) {
            throw new IOException("Network not connected.");
        }
        DestAddress address = socket.getRemoteAddress();

        NetworkHostsContainer netHostsCont = peer.getHostService().getNetworkHostsContainer();
        Host host = netHostsCont.createIncomingHost(address, connection);
        host.setStatus(HostStatus.ACCEPTING, "");
        try {
            ConnectionEngine engine = new ConnectionEngine(peer, host);
            engine.initHostHandshake();
            engine.processIncomingData();
        } catch (IOException exp) {
            if (host.isConnected()) {
                host.setStatus(HostStatus.ERROR, exp.getMessage());
                host.disconnect();
            }
            throw exp;
        } finally {
            if (host.isConnected()) {
                host.setStatus(HostStatus.DISCONNECTED, "Unknown");
                host.disconnect();
            }
        }
    }

    /**
     * @param requestLine
     * @throws IOException
     */
    private void handleIncommingUriDownload(String requestLine) throws IOException {
        try {
            DestAddress localAddress = peer.getLocalAddress();
            // this must be a request from local host
            if (!socket.getRemoteAddress().isLocalHost(localAddress)) {
                return;
            }
            socket.getChannel().write(BufferCache.OK_BUFFER);
        } finally {
            IOUtil.closeQuietly(socket);
        }
        String uriToken = requestLine.substring(URI_DOWNLOAD_PREFIX.length() + 1);


    }

    /**
     * @param requestLine
     * @throws IOException
     */
    private void handleIncommingMagmaDownload(String requestLine) throws IOException {
        try {
            DestAddress localAddress = peer.getLocalAddress();
            // this must be a request from local host
            if (!socket.getRemoteAddress().isLocalHost(localAddress)) {
                return;
            }
            socket.getChannel().write(BufferCache.OK_BUFFER);
        } finally {
            IOUtil.closeQuietly(socket);
        }
        String fileNameToken = requestLine.substring(MAGMA_DOWNLOAD_PREFIX.length() + 1);


    }

    private void handleIncommingRSSDownload(String requestLine) throws IOException {
        try {
            DestAddress localAddress = peer.getLocalAddress();
            // this must be a request from local host
            if (!socket.getRemoteAddress().isLocalHost(localAddress)) {
                return;
            }
            socket.getChannel().write(BufferCache.OK_BUFFER);
        } finally {
            IOUtil.closeQuietly(socket);
        }
        String fileNameToken = requestLine.substring(RSS_DOWNLOAD_PREFIX.length() + 1);


    }

    private void handleIncommingGIV(String requestLine) {
        // A correct request line should line should be:
        // GIV <file-ref-num>:<ClientID GUID in hexdec>/<filename>\n\n
        String remainder = requestLine.substring(4); // skip GIV

        try {
            // get file number index position
            int fileNumIdx = remainder.indexOf(':');
            // this would extract file index, but we don't use it anymore
            //String fileIndex = remainder.substring(0, fileNumIdx);

            // get GUID end index position.
            int guidIdx = remainder.indexOf('/', fileNumIdx);
            // extract GUID...
            String guidStr = remainder.substring(fileNumIdx + 1, guidIdx);

            // extract file name
            String givenFileName = remainder.substring(guidIdx + 1);
            givenFileName = URLCodecUtils.decodeURL(givenFileName);

            GUID givenGUID = new GUID(guidStr);
            PushHandler.handleIncommingGIV(socket, givenGUID, givenFileName);
        } catch (IndexOutOfBoundsException exp) {
            // handle possible out of bounds exception for better logging...
            logger.error("Failed to parse GIV: {}", requestLine, exp);
        }
    }
}