package phex.upload;

import junit.framework.Assert;
import junit.framework.TestCase;
import phex.common.address.DefaultDestAddress;
import phex.http.GnutellaHeaderNames;
import phex.http.GnutellaRequest;
import phex.http.HTTPHeader;
import phex.http.HTTPRequest;
import phex.UploadPrefs;
import phex.peer.Peer;
import phex.share.ShareFile;
import phex.share.SharedFilesService;
import phex.upload.handler.AbstractUploadHandler;
import phex.upload.handler.UploadHandler;
import phex.upload.response.UploadResponse;

import java.io.File;
import java.io.IOException;

public class UploadHandlerTest extends TestCase {
    private UploadManager uploadManager;

    public void setUp() {
        //        PhexGuiPrefs.init();
//        Localizer.initialize( InterfacePrefs.LocaleName.get() );
        Peer peer = new Peer();
        uploadManager = new UploadManager(peer);
    }

    public void testIfStateRemainsQueuedIfBusy() throws IOException {
        HTTPRequest request = new HTTPRequest("GET",
                "/get/1/dummy", false);
        request.addHeader(new HTTPHeader(GnutellaHeaderNames.X_QUEUE,
                "0.1"));
        UploadState state = new UploadState(
                new DefaultDestAddress("1.1.1.1", 80), "test",
                uploadManager);
        UploadHandler h = new UploadHandlerMock();

        // ensure manager is busy...
        UploadPrefs.MaxParallelUploads.set(1);
        UploadState busy = new UploadState(
                new DefaultDestAddress("2.2.2.2", 80), "busy",
                uploadManager);
        busy.setStatus(UploadStatus.UPLOADING_DATA);
        uploadManager.addUploadState(busy);

        h.determineUploadResponse(request, state, uploadManager);
        Assert.assertTrue(h.isQueued());

        h.determineUploadResponse(request, state, uploadManager);
        Assert.assertTrue("not queued", h.isQueued());

        h.determineUploadResponse(request, state, uploadManager);
        Assert.assertTrue(h.isQueued());

        // release busy state..
        uploadManager.removeUploadState(busy);

        h.determineUploadResponse(request, state, uploadManager);
        Assert.assertFalse(h.isQueued());
    }

    private static class UploadHandlerMock extends AbstractUploadHandler {

        protected UploadHandlerMock() {
            super(new SharedFilesService(new Peer()));
        }

        @Override
        protected UploadResponse determineFailFastResponse(
                HTTPRequest httpRequest, UploadState uploadState,
                ShareFile requestedFile) {
            return null;
        }

        @Override
        protected UploadResponse finalizeUploadResponse(
                HTTPRequest httpRequest, UploadState uploadState,
                ShareFile requestedFile) throws IOException {
            return null;
        }

        @Override
        protected ShareFile findShareFile(GnutellaRequest request,
                                          UploadState uploadState, UploadManager uploadMgr) {
            return new ShareFile(new File("dummy"));
        }
    }
}
