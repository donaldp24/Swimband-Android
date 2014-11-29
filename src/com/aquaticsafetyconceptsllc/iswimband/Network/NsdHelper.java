package com.aquaticsafetyconceptsllc.iswimband.Network;

import java.util.ArrayList;

import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager;
import android.util.Log;

public class NsdHelper {

    private Context mContext;

    private NsdManager mNsdManager;
    private NsdManager.ResolveListener mResolveListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.RegistrationListener mRegistrationListener;

    public static final String SERVICE_TYPE = "_http._tcp.";
    
    public NetServerBrowserDelegate delegate;
    public ArrayList<NsdServiceInfo> _services;

    public NsdHelper(Context context) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        _services = new ArrayList<NsdServiceInfo>();
    }

    public void initializeNsd() {
        initializeResolveListener();
        initializeDiscoveryListener();
        initializeRegistrationListener();
    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Logger.logDebug("Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Logger.logDebug("Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Logger.logDebug("Unknown Service Type: " + service.getServiceType());
                } else /*if (service.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains(mServiceName)) */{
                	if (!_services.contains(service)) {
	                	Boolean add = true;
	                	if (delegate != null) {
	                		add = delegate.shouldAllowService(service);
	                	}
	                	if (add) {
	                		_services.add(service);
	                		mNsdManager.resolveService(service, mResolveListener);
	                	}
                	}
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Logger.logDebug("service lost" + service);
                if (delegate != null)
                	delegate.lostService(service);
            }
            
            @Override
            public void onDiscoveryStopped(String serviceType) {
                Logger.logInfo("Discovery stopped: " + serviceType);        
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Logger.logError("Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Logger.logError("Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Logger.logError("Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Logger.logInfo("Resolve Succeeded. " + serviceInfo);
            }
        };
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                
            }
            
            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            }
            
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }
            
        };
    }

    public void registerService(int port, String name) {
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(name);
        serviceInfo.setServiceType(SERVICE_TYPE);
        
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        
    }

    public void discoverServices() {
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }
    
    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public void tearDown() {
        mNsdManager.unregisterService(mRegistrationListener);
    }
}
