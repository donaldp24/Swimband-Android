package com.aquaticsafetyconceptsllc.iswimband.Network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;

public class NetServer {
	private ServerSocket mServerSocket = null;
    private Thread mThread = null;
    private int mPort = -1;
    public NetServerDelegate delegate = null;

    public NetServer() {
        mThread = new Thread(new ServerThread());
        mThread.start();
    }
    
    public int getLocalPort() {
        return mPort;
    }
    
    public void setLocalPort(int port) {
        mPort = port;
    }

    public void tearDown() {
        mThread.interrupt();
        try {
            mServerSocket.close();
        } catch (IOException ioe) {
            Logger.logError("Error when closing server socket.");
        }
    }

    class ServerThread implements Runnable {

        @Override
        public void run() {

            try {
                // Since discovery will happen via Nsd, we don't need to care which port is
                // used.  Just grab an available one  and advertise it via Nsd.
                mServerSocket = new ServerSocket(0);
                setLocalPort(mServerSocket.getLocalPort());
                
                while (!Thread.currentThread().isInterrupted()) {
                    Logger.logDebug("ServerSocket Created, awaiting connection");
                    Socket socket = mServerSocket.accept();
                    Logger.logDebug("accepted.");
                    if (delegate != null)
                    	delegate.acceptSocket(NetServer.this, socket);
                }
            } catch (IOException e) {
                Logger.logError("Error creating ServerSocket: ", e);
                e.printStackTrace();
            }
        }
    }
}
