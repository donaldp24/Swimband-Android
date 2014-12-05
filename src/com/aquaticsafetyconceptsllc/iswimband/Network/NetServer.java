package com.aquaticsafetyconceptsllc.iswimband.Network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;

public class NetServer {

    public static final String TAG = "NetServer";

    private Context mContext;

    private ServerSocket mServerSocket = null;
    private Thread mThread = null;

    protected String _serverName;
    protected NsdServiceInfo _service;

    public NetServerDelegate delegate = null;

    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener mRegistrationListener;


    public NetServer(Context context) {
        mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);


    }

    protected NsdManager.RegistrationListener _createRegistrationListener() {

        mRegistrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                Logger.log("NetServer.mRegistrationListener.onServiceRegistered : %s", serviceInfo.getServiceName());
                if (delegate != null)
                    delegate.netServerDidPublish(NetServer.this);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Logger.log("NetServer.mRegistrationListener.onRegistrationFailed : %s", serviceInfo.getServiceName());
                if (delegate != null)
                    delegate.failedPublishWithError(NetServer.this, errorCode);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                Logger.log("NetServer.mRegistrationListener.onServiceUnregistered : %s", serviceInfo.getServiceName());
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Logger.log("NetServer.mRegistrationListener.onUnregistrationFailed : %s", serviceInfo.getServiceName());
            }
        };
        return mRegistrationListener;
    }

    public String serverName() {
        return _serverName;
    }

    public boolean startServerWithName(String serverName) {
        Logger.l(TAG, "NetServer startServerWithName() : %s", serverName);
        if (_service != null)
            return false;

        try {
            _serverName = serverName;
            // Since discovery will happen via Nsd, we don't need to care which port is
            // used.  Just grab an available one  and advertise it via Nsd.
            mServerSocket = new ServerSocket(0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        Logger.l(TAG, "NetServer startServerWithName() : %s, socket created", serverName);

        mThread = new Thread(new ServerThread());
        mThread.start();

        _service = new NsdServiceInfo();
        _service.setServiceName(_serverName);
        _service.setPort(mServerSocket.getLocalPort());
        _service.setServiceType(NetServerBrowser.SERVICE_TYPE);

        Logger.l(TAG, "NetServer startServerWithName() : %s, NsdServiceInfo created", serverName);

        try {
            _createRegistrationListener();
            mNsdManager.registerService(_service, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        Logger.l(TAG, "NetServer : server (%s) started successfully", _serverName);

        return true;
    }

    public void stopServer() {
        mThread.interrupt();
        try {
            mServerSocket.close();
        }
        catch (IOException ioe) {
            Logger.logError(TAG, "Error when closing server socket.");
        }
        finally {
            if (_service != null) {
                mNsdManager.unregisterService(mRegistrationListener);
            }
        }
    }

    class ServerThread implements Runnable {

        @Override
        public void run() {
            Logger.l(TAG, "ServerThread : %s", _serverName);
            while (!Thread.currentThread().isInterrupted()) {
                Logger.l(TAG, "ServerSocket Created, awaiting connection");
                try {
                    Socket socket = mServerSocket.accept();
                    Logger.l(TAG, "accepted.");
                    if (delegate != null)
                        delegate.acceptSocket(NetServer.this, socket);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            Logger.l(TAG, "ServerThread stopped.");
        }
    }
}
