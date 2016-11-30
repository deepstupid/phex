package phex.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;
import org.teleal.cling.support.igd.callback.PortMappingAdd;
import org.teleal.cling.support.igd.callback.PortMappingDelete;
import org.teleal.cling.support.model.PortMapping;
import phex.servent.Servent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UPnPMapper
{
    private static final Logger logger = LoggerFactory.getLogger( UPnPMapper.class );
    
    private UpnpService upnpService;
    private final Map<Service, PortMapping> activePortMappings = new HashMap<Service, PortMapping>();
    
    public void initialize()
    {
        activePortMappings.clear();

        UpnpService upnpService;
        try {
            upnpService = new UpnpServiceImpl(new PhexRegistryListener());
            upnpService.getControlPoint().search();
        } catch  (Throwable e) {
            e.printStackTrace();
            upnpService = null;
        }
        this.upnpService = upnpService;
    }
    
    public void shutdown()
    {
        if ( upnpService != null )
        {
            upnpService.shutdown();
            upnpService = null;
        }
    }
    
    
    private static final DeviceType IGD_DEVICE_TYPE = new UDADeviceType("InternetGatewayDevice", 1);
    private static final DeviceType CONNECTION_DEVICE_TYPE = new UDADeviceType("WANConnectionDevice", 1);
    private static final ServiceType IP_SERVICE_TYPE = new UDAServiceType("WANIPConnection", 1);
    private static final ServiceType PPP_SERVICE_TYPE = new UDAServiceType("WANPPPConnection", 1);

    private Service discoverConnectionService(Device device)
    {
        if (!device.getType().equals(IGD_DEVICE_TYPE))
        {
            return null;
        }

        Device[] connectionDevices = device.findDevices(CONNECTION_DEVICE_TYPE);
        if (connectionDevices.length == 0)
        {
            logger.debug("IGD doesn't support '" + CONNECTION_DEVICE_TYPE + "': "
                + device);
            return null;
        }

        Device connectionDevice = connectionDevices[0];
        logger.debug("Using first discovered WAN connection device: "
            + connectionDevice);

        Service ipConnectionService = connectionDevice
            .findService(IP_SERVICE_TYPE);
        Service pppConnectionService = connectionDevice
            .findService(PPP_SERVICE_TYPE);

        if (ipConnectionService == null && pppConnectionService == null)
        {
            logger.debug("IGD doesn't support IP or PPP WAN connection service: "
                + device);
        }

        return ipConnectionService != null ? ipConnectionService
            : pppConnectionService;
    }
    
    RegistryListener listener = new PhexRegistryListener();
    
    private final class PhexRegistryListener extends DefaultRegistryListener
    {
        @Override
        public void deviceAdded(Registry registry, Device device)
        {
            final Service wanService = discoverConnectionService(device);
            if ( wanService == null )
            {
                return;
            }
            String address;
            if (wanService.getDevice() instanceof RemoteDevice) 
            {
                address = ((RemoteDevice) wanService.getDevice()).getIdentity().getDiscoveredOnLocalAddress().getHostAddress();
            } 
            else 
            {
                address = upnpService.getRouter().getNetworkAddressFactory().getBindAddresses()[0].getHostAddress();
            }
            int port = Servent.getInstance().getLocalAddress().getPort();
            final PortMapping desiredMapping = new PortMapping( port, address, 
                PortMapping.Protocol.TCP, "Phex NAT" );
            
            upnpService.getControlPoint().execute(
                new PortMappingAdd(wanService, desiredMapping)
                {

                    @Override
                    public void success(ActionInvocation invocation)
                    {
                        logger.debug("Port mapping added: {}", desiredMapping);
                        activePortMappings.put(wanService, desiredMapping);
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                        UpnpResponse operation, String defaultMsg)
                    {
                        logger.warn("Failed to add port mapping: {}", desiredMapping);
                        logger.warn("Reason: {}", defaultMsg);
                    }
                });
        }
        
        @Override
        synchronized public void deviceRemoved(Registry registry, Device device) 
        {
            for (Service service : device.findServices()) 
            {
                Iterator<Map.Entry<Service, PortMapping>> it = activePortMappings.entrySet().iterator();
                while (it.hasNext()) 
                {
                    Map.Entry<Service, PortMapping> activeEntry = it.next();
                    if (!activeEntry.getKey().equals(service)) continue;
                    logger.warn("Device disappeared, couldn't delete port mappings: " + activeEntry.getValue());
                    it.remove();
                }
            }
        }
        
        @Override
        synchronized public void beforeShutdown(Registry registry)
        {
            for (Map.Entry<Service, PortMapping> activeEntry : activePortMappings
                .entrySet())
            {
                final PortMapping pm = activeEntry.getValue();
                logger.debug("Trying to delete port mapping on IGD: " + pm);
                new PortMappingDelete(activeEntry.getKey(), registry
                    .getUpnpService().getControlPoint(), pm)
                {
                    @Override
                    public void success(ActionInvocation invocation)
                    {
                        logger.debug("Port mapping deleted: " + pm);
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                        UpnpResponse operation, String defaultMsg)
                    {
                        logger.warn("Failed to delete port mapping: " + pm);
                        logger.warn("Reason: " + defaultMsg);
                    }

                }.run(); // Synchronous!
            }
        }
    }
}
