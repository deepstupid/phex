package phex.net;

import org.xsocket.IDataSource;
import phex.common.address.DestAddress;

public interface UdpDataHandler {
    void handleUdpData(IDataSource dataSource, DestAddress orgin);
}
