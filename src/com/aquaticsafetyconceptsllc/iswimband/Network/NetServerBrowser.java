package com.aquaticsafetyconceptsllc.iswimband.Network;

import java.util.ArrayList;

import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager;
import android.util.Log;

public class NetServerBrowser {

    public static final String TAG = "NetServerBrowser";

    private Context mContext;

    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;

    public static final String SERVICE_TYPE = "_Wahooo_SwimBand._tcp.";
    
    public NetServerBrowserDelegate delegate;


    protected ArrayList<NsdServiceInfo> _services;

    private String mLocalServerName;

    public NetServerBrowser(Context context) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        _services = new ArrayList<NsdServiceInfo>();

        _initializeBrowser();
    }

    public void setLocalServerName(String serverName) {
        mLocalServerName = serverName;
    }

    protected void _initializeBrowser() {
        _initializeDiscoveryListener();
    }

    protected void _initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Logger.log("Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Logger.log("Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Logger.log("Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mLocalServerName)) {
                    Logger.log("Local machine: " + mLocalServerName);
                } else /*if (service.getServiceName().contains(mServiceName)) */{
                	if (!_services.contains(service)) {

                        boolean add = true;
                        for (NsdServiceInfo exist : _services) {
                            if (exist.getServiceName().equals(service.getServiceName())) {
                                add = false;
                                break;
                            }
                        }

                        if (add && delegate != null) {
                            add = delegate.shouldAllowService(service);
                        }
                        if (add) {
                            mNsdManager.resolveService(service, _createResolveListener());
                        }
                	}
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Logger.log("service lost" + service);
                if (delegate != null)
                	delegate.lostService(service);

                _services.remove(service);
                ArrayList<NsdServiceInfo> removings = new ArrayList<NsdServiceInfo>();
                for (NsdServiceInfo exist : _services) {
                    if (exist.getServiceName().equals(service.getServiceName())) {
                        removings.add(exist);
                    }
                }

                for (NsdServiceInfo remo : removings) {
                    _services.remove(remo);
                }
            }
            
            @Override
            public void onDiscoveryStopped(String serviceType) {
                Logger.l(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Logger.logError(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Logger.logError(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    protected NsdManager.ResolveListener _createResolveListener() {
        return new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Logger.logError(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Logger.l(TAG, "Resolve Succeeded. " + serviceInfo);
                _services.add(serviceInfo);
                if (delegate != null)
                    delegate.resolvedService(serviceInfo);
            }
        };
    }


    public void startBrowser() {
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }
    
    public void stopBrowser() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public ArrayList<NsdServiceInfo> getServices() {
        return _services;
    }

    public int serviceCount() {
        return _services.size();
    }

    public NsdServiceInfo serviceWithName(String serviceName) {
        if (serviceName == null)
            return null;

        for (NsdServiceInfo serviceInfo : _services) {
            if (serviceName.equals(serviceInfo.getServiceName()))
                return serviceInfo;
        }
        return null;
    }

}
