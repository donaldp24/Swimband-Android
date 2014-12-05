/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aquaticsafetyconceptsllc.iswimband.Network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import android.net.nsd.NsdServiceInfo;

import com.aquaticsafetyconceptsllc.iswimband.Utils.Buffer;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;

public class NetConnection {

    public static final String TAG = "NetConnection";

	public static enum ConnectionState
	{
	    kConnectionState_None,
	    
	    kClientState_Connecting,
	    kClientState_WaitingForApproval,
	    kClientState_Connected,
	    
	    kServerState_Connecting,
	    kServerState_Connected
	};
	
	public ConnectionState state;
	
    private ChatClient mChatClient;

    private Socket mSocket;
    private int mPort = -1;
    
    private NsdServiceInfo service;
    private String _serviceName;
    private String _serviceID;
    private long _lastDataReceivedTime;
    
    public Buffer readBuffer;
    
    public NetConnectionDelegate delegate = null;

    private boolean _isOpen;

    public NetConnection() {
        _isOpen = false;
        _serviceName = null;
        _serviceID = null;

        initBuffer();
    }
    
    public boolean openConnectionWithNativeSocket(Socket socket) {
    	close();

        initBuffer();
        _lastDataReceivedTime = TimeUnit.MICROSECONDS.toSeconds(System.currentTimeMillis());
        boolean ret = _setSocket(socket);

        if (ret == false)
            return false;

        _connectToServer();

        return true;
    }
    
    public boolean openConnectionWithNetService(NsdServiceInfo service) {

        close();

        _lastDataReceivedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        _serviceName = service.getServiceName();

    	this.service = service;

    	initBuffer();

    	Socket socket = null;
		try {
			socket = new Socket(service.getHost(), service.getPort());
			boolean ret = _setSocket(socket);

            if (ret == false)
                return false;

            _connectToServer();

            return true;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            return false;
		}
    }

    public boolean isOpen() {
        return _isOpen;
    }

    public long lastDataReceivedTime() {
        return _lastDataReceivedTime;
    }
    
    private void initBuffer() {
    	readBuffer = new Buffer(1024 * 1024);
    }

    public void close() {
        if (mChatClient != null)
            mChatClient.close();
        _isOpen = false;

        if (delegate != null)
            delegate.netConnectionDisconnected(this);
    }

    protected boolean _connectToServer() {
        if (mSocket == null)
            return false;
        mChatClient = new ChatClient();
        _isOpen = true;
        return true;
    }

    public void writeData(byte[] data) {
        if (mChatClient != null)
            mChatClient.addSendMessage(data);
    }


    public int getLocalPort() {
        return mPort;
    }
    
    public void setLocalPort(int port) {
        mPort = port;
    }
    
    public String serviceName() {
    	if (_serviceName != null)
    		return _serviceName;
    	if (service != null)
    		return service.getServiceName();
    	return "Connecting...";
    }
    
    public void setServiceName(String val) {
    	_serviceName = val;
    }
    public String serviceID() {
    	return _serviceID;
    }
    public void setServiceID(String val) {
    	_serviceID = val;
    }


    protected synchronized void _receivedMessage(byte[] msg) {
        Logger.log("message received ");
        
        readBuffer.put(msg);
        
        if (delegate != null)
        	delegate.netConnectionReceivedData(this);
    }

    protected synchronized boolean _setSocket(Socket socket) {
        Logger.log("setSocket being called.");
        if (socket == null) {
            Logger.log("Setting a null socket.");
            return false;
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    // TODO(alexlucas): Auto-generated catch block
                    e.printStackTrace();
                    return false;
                }
            }
        }
        mSocket = socket;
        return true;
    }

    protected Socket _getSocket() {
        return mSocket;
    }

    private class ChatClient {

        private Thread mSendThread;
        private Thread mRecThread;

        BlockingQueue<byte[]> mMessageQueue;
        private int QUEUE_CAPACITY = 10;

        public ChatClient() {

            Logger.log("Creating chatClient");

            mMessageQueue = new ArrayBlockingQueue<byte[]>(QUEUE_CAPACITY);

            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }

        public void addSendMessage(byte[] msg) {
            mMessageQueue.add(msg);
        }

        class SendingThread implements Runnable {
            public SendingThread() {

            }

            @Override
            public void run() {

                if (_getSocket() == null) {
                    Logger.log("Client-side socket not initialized. cannot run it");
                    return;
                } else {
                    Logger.log("Socket already initialized. skipping!");
                }

                mRecThread = new Thread(new ReceivingThread());
                mRecThread.start();


                while (true) {
                    try {
                        byte[] msg = mMessageQueue.take();
                        boolean ret = _sendMessage(msg);
                        if (!ret) {
                            Logger.log("_sendMessage failed --- break SendingThread");
                            break;
                        }
                    } catch (InterruptedException ie) {
                        Logger.log("Message sending loop interrupted, exit");
                        break;
                    }
                }
            }


        }

        class ReceivingThread implements Runnable {

            @Override
            public void run() {

                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(
                            mSocket.getInputStream()));
                    while (!Thread.currentThread().isInterrupted()) {
                    	char[] readBuffer = new char[1024];
                    	int readLen = input.read(readBuffer, 0, 1024);
                        if (readLen < 0) {
                            Logger.logError(TAG, "error : readLen of input.read < 0, close connection!");
                            break;
                        }
                        if (readLen != 0) {
                            Logger.l(TAG, "Read from the stream");
                            byte[] buffer = new byte[readLen];
                            for (int i = 0; i < readLen; i++)
                            	buffer[i] = (byte)readBuffer[i];
                            //_receivedMessage(buffer);
                        } else {
                            Logger.logError(TAG, "The nulls! The nulls!");
                            break;
                        }
                    }
                    input.close();

                } catch (IOException e) {
                    Logger.logError(TAG, "Server loop error: %s", e);
                }
            }
        }

        public void close() {
            try {
                _getSocket().close();
            } catch (IOException ioe) {
                Logger.log("Error when closing server socket.");
            }
        }

        protected boolean _sendMessage(byte[] msg) {
            try {
                Socket socket = _getSocket();
                if (socket == null) {
                    Logger.log("Socket is null, wtf?");
                    return false;
                } else if (socket.getOutputStream() == null) {
                    Logger.log("Socket output stream is null, wtf?");
                    return false;
                }

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(_getSocket().getOutputStream())), true);
                
                char[] writeBuffer = new char[msg.length];
                for (int i = 0; i < msg.length; i++)
                	writeBuffer[i] = (char)msg[i];
                out.print(writeBuffer);
                out.flush();
            } catch (UnknownHostException e) {
                Logger.log("Unknown Host %s", e);
                return false;
            } catch (IOException e) {
                Logger.log("I/O Exception %s", e);
                return false;
            } catch (Exception e) {
                Logger.log("Error3 %s", e);
                return false;
            }
            Logger.log("Client sent message: ");
            return true;
        }
    }
}
