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

import android.net.nsd.NsdServiceInfo;

import com.aquaticsafetyconceptsllc.iswimband.Utils.Buffer;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;

public class NetConnection {
	
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
    
    public Buffer readBuffer;
    
    public NetConnectionDelegate delegate = null;
    
    
    public NetConnection(Socket socket) {
    	initBuffer();
        setSocket(socket);
    }
    
    public NetConnection(NsdServiceInfo service) {
    	this.service = service;
    	initBuffer();
    	Socket socket = null;
		try {
			socket = new Socket(service.getHost(), service.getPort());
			setSocket(socket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void initBuffer() {
    	readBuffer = new Buffer(1024 * 1024);
    }

    public void tearDown() {
        mChatClient.tearDown();
    }

    public void connectToServer() {
        mChatClient = new ChatClient();
    }

    public void sendMessage(byte[] msg) {
        if (mChatClient != null) {
            mChatClient.sendMessage(msg);
        }
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
    	return "";
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
    

    public synchronized void receivedMessage(byte[] msg) {
        Logger.logDebug("message received ");
        
        readBuffer.put(msg);
        
        if (delegate != null)
        	delegate.netConnectionReceivedData(this);
    }

    private synchronized void setSocket(Socket socket) {
        Logger.logDebug("setSocket being called.");
        if (socket == null) {
            Logger.logDebug("Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    // TODO(alexlucas): Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        mSocket = socket;
    }

    private Socket getSocket() {
        return mSocket;
    }

    private class ChatClient {

        private Thread mSendThread;
        private Thread mRecThread;

        public ChatClient() {

            Logger.logDebug("Creating chatClient");

            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }

        class SendingThread implements Runnable {

            BlockingQueue<byte[]> mMessageQueue;
            private int QUEUE_CAPACITY = 10;

            public SendingThread() {
                mMessageQueue = new ArrayBlockingQueue<byte[]>(QUEUE_CAPACITY);
            }

            @Override
            public void run() {
       
                if (getSocket() == null) {
                    Logger.logDebug("Client-side socket not initialized. cannot run it");
                    return;
                } else {
                    Logger.logDebug("Socket already initialized. skipping!");
                }

                mRecThread = new Thread(new ReceivingThread());
                mRecThread.start();


                while (true) {
                    try {
                        byte[] msg = mMessageQueue.take();
                        sendMessage(msg);
                    } catch (InterruptedException ie) {
                        Logger.logDebug("Message sending loop interrupted, exiting");
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
                        if (readLen != 0) {
                            Logger.logDebug("Read from the stream");
                            byte[] buffer = new byte[readLen];
                            for (int i = 0; i < readLen; i++)
                            	buffer[i] = (byte)readBuffer[i];
                            receivedMessage(buffer);
                        } else {
                            Logger.logDebug("The nulls! The nulls!");
                            break;
                        }
                    }
                    input.close();

                } catch (IOException e) {
                    Logger.logError("Server loop error: %s", e);
                }
            }
        }

        public void tearDown() {
            try {
                getSocket().close();
            } catch (IOException ioe) {
                Logger.logDebug("Error when closing server socket.");
            }
        }

        public void sendMessage(byte[] msg) {
            try {
                Socket socket = getSocket();
                if (socket == null) {
                    Logger.logDebug("Socket is null, wtf?");
                } else if (socket.getOutputStream() == null) {
                    Logger.logDebug("Socket output stream is null, wtf?");
                }

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(getSocket().getOutputStream())), true);
                
                char[] writeBuffer = new char[msg.length];
                for (int i = 0; i < msg.length; i++)
                	writeBuffer[i] = (char)msg[i];
                out.print(writeBuffer);
                out.flush();
            } catch (UnknownHostException e) {
                Logger.logDebug("Unknown Host %s", e);
            } catch (IOException e) {
                Logger.logDebug("I/O Exception %s", e);
            } catch (Exception e) {
                Logger.logDebug("Error3 %s", e);
            }
            Logger.logDebug("Client sent message: ");
        }
    }
}
