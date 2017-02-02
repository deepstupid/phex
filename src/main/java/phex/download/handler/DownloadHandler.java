package phex.download.handler;

import phex.host.UnusableHostException;
import phex.http.HTTPMessageException;

import java.io.IOException;

public interface DownloadHandler {
    /**
     * Performs download pre process operations.
     *
     * @throws IOException
     */
    void preProcess() throws DownloadHandlerException;

    /**
     * Handles the handshake for the download.
     */
    void processHandshake()
            throws IOException, UnusableHostException, HTTPMessageException;

    /**
     * Process the actual download data transfer.
     *
     * @throws IOException
     */
    void processDownload() throws IOException;

    /**
     * Performs download post process operations.
     */
    void postProcess();

    /**
     * Stops the download.
     */
    void stopDownload();

    /**
     * Indicates whether the connection is keept alive and the next request can
     * be send.
     *
     * @return true if the next request can be send on this connection
     */
    boolean isAcceptingNextRequest();
}
