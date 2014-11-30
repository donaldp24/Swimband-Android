package com.aquaticsafetyconceptsllc.iswimband.Network;


import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import com.aquaticsafetyconceptsllc.iswimband.Network.NetConnection.ConnectionState;
import com.aquaticsafetyconceptsllc.iswimband.Network.Packets.BandDataPacket;
import com.aquaticsafetyconceptsllc.iswimband.Network.Packets.ClientConnectRequestPacket;
import com.aquaticsafetyconceptsllc.iswimband.Network.Packets.NetPacket;
import com.aquaticsafetyconceptsllc.iswimband.Network.Packets.NetPacket.IFPacketAllocator;
import com.aquaticsafetyconceptsllc.iswimband.Network.Packets.NetPacket.PacketType;
import com.aquaticsafetyconceptsllc.iswimband.Network.Packets.NetPacketException;
import com.aquaticsafetyconceptsllc.iswimband.Network.Packets.ServerConnectResponsePacket;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Settings;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooDeviceManager;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;

public class NetManager implements NetConnectionDelegate, NetServerDelegate, NetServerBrowserDelegate {
	
	private static NetManager _instance = null;

	private NetServer mServer;
	private NsdHelper mNsdHelper;
	
	private ArrayList<NetConnection> _clientConnections;
	private ArrayList<NetConnection> _serverConnections;
	private ArrayList<NsdServiceInfo> _availableServices;
	
	private String _serviceName;
	private String _serviceID;

	public String serviceName() { return _serviceName; }
	public void setServiceName(String name) { _serviceName = name; }
	public String serviceID() { return _serviceID; }
	public void setServiceID(String serviceID) { _serviceID = serviceID; }
	
	private Boolean _sharing;

	public static NetManager initialize(Context context) {
		if (_instance == null)
			_instance = new NetManager(context);
		return _instance;
	}
	
	public static NetManager sharedManager() {
		return _instance;
	}

	private NetManager(Context context) {
		
		_clientConnections = new ArrayList<NetConnection>();
		_serverConnections = new ArrayList<NetConnection>();
		_availableServices = new ArrayList<NsdServiceInfo>();
		
		_serviceName = Settings.sharedSettings().netServiceName();
		_serviceID = Settings.sharedSettings().netServiceID();

	    mServer = new NetServer();
	
	    mNsdHelper = new NsdHelper(context);
	    mNsdHelper.initializeNsd();
	    
	    //[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(_didEnterBackground:) name:UIApplicationDidEnterBackgroundNotification object:nil];
        //[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(_willEnterForeground:) name:UIApplicationWillEnterForegroundNotification object:nil];
	}
	
	public void startServer() {
		if(mServer.getLocalPort() > -1) {
            mNsdHelper.registerService(mServer.getLocalPort(), _serviceName);
        } else {
            Logger.logDebug("ServerSocket isn't bound.");
        }
	}
	
	public void stopServer() {
		mServer.tearDown();
		mNsdHelper.tearDown();
		
	    ArrayList<NetConnection> clientConnections = (ArrayList<NetConnection>) _clientConnections.clone();
	    _clientConnections.clear();
	    
	    for (NetConnection connection : clientConnections )
	    	connection.tearDown();
	    
	    _sharing = false;
	    
	    _fireUpdateNotification();
	}
	
	public void startBrowser() {
		mNsdHelper.discoverServices();
	}
	
	public void stopBrowser() {
		mNsdHelper.stopDiscovery();
	}


	public void stopSharing() {
		NetPacket packet = new NetPacket(PacketType.kPacketType_ServerStoppedSharing);
		byte[] packetData = packet.pack();

		ArrayList<NetConnection> serverConnections = new ArrayList<NetConnection>();
		serverConnections.addAll(_serverConnections);

		for ( NetConnection connection : serverConnections ) {
			connection.writeData(packetData);
			closeConnection(connection);
		}

		packet = new NetPacket(PacketType.kPacketType_ClientStoppedObserving);
		packetData = packet.pack();

		ArrayList<NetConnection> clientConnections = new ArrayList<NetConnection>();
		serverConnections.addAll(_clientConnections);

		for( NetConnection connection : clientConnections ) {
			connection.writeData(packetData);

			closeConnection(connection);
		}

		stopBrowser();
		stopServer();

		//[[WahoooDeviceManager sharedManager] clearDevices];

	}
	
	public Boolean connectToService(NsdServiceInfo service) {
		if (service != null) {
            Logger.logDebug("Connecting.");
            
            NetConnection connection;
			try {
				connection = new NetConnection(new Socket(service.getHost(), service.getPort()));
				connection.state = ConnectionState.kClientState_Connecting;
				connection.delegate = this;
				connection.connectToServer();
				
				_clientConnections.add(connection);
				
				_availableServices.remove(service);
		        
		        connection.state = ConnectionState.kClientState_WaitingForApproval;
		        
		        ClientConnectRequestPacket packet = new ClientConnectRequestPacket();
		        packet.setClientName(_serviceName);
		        packet.setClientID(_serviceID);
		        
		        connection.writeData(packet.pack());
		        
		        _fireUpdateNotification();

				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}            
            
        } else {
            Logger.logDebug("No service to connect to!");
            return false;
        }
	}

	public boolean openConnectionToService(String serviceName) {
		NsdServiceInfo service = null;

		ArrayList<NsdServiceInfo> services = mNsdHelper._services;

		for( NsdServiceInfo current : services ) {
			if ( current.getServiceName().equals(serviceName) ) {
				service = current;
				break;
			}
		}

		if ( service == null ) {
			return false;
		}

		for( NetConnection currentConnection : _clientConnections )
		{
			if ( currentConnection.serviceName().equals(serviceName) ) {
				_availableServices.remove(service);
				return false;
			}
		}

		NetConnection connection = new NetConnection(service);
		connection.state = ConnectionState.kClientState_Connecting;

		if ( connection.connectToServer())
		{
			connection.delegate = this;

			_clientConnections.add(connection);
			_availableServices.remove(service);
			connection.state = ConnectionState.kClientState_WaitingForApproval;

			ClientConnectRequestPacket packet = new ClientConnectRequestPacket();
			packet.setClientName(_serviceName);
			packet.setClientID(_serviceID);

			connection.writeData(packet.pack());

			_fireUpdateNotification();

			return true;
		}

		return false;
	}
	
	public void closeConnection(NetConnection connection) {
		if (_serverConnections.contains(connection)) {
			NetPacket packet = new NetPacket(PacketType.kPacketType_ServerStoppedSharing);
			byte[] packetData = packet.pack();
			connection.writeData(packetData);
		} else if (_clientConnections.contains(connection)) {
			NetPacket packet = new NetPacket(PacketType.kPacketType_ClientStoppedObserving);
			byte[] packetData = packet.pack();
			connection.writeData(packetData);
		}
		connection.tearDown();

		//[[WahoooDeviceManager sharedManager] removeConnection:connection];
	}
	
	public int numberOfAvailableServices() {
	    return _availableServices.size();
	}
	
	public String nameOfAvailableServiceAtIndex(int index) {

	    String name = "";
	    
	    if( index < _availableServices.size() )
	    {
	        NsdServiceInfo service = _availableServices.get(index);
	        name = service.getServiceName();	        
	    }
	    return name;
	}
	
	public NsdServiceInfo availableServiceAtIndex(int index) {
	    if ( index < _availableServices.size())
	        return _availableServices.get(index);
	    return null;
	}

	public int numberOfClientConnections() {
	    return _clientConnections.size();
	}

	public String nameOfClientConnectionAtIndex(int index) {
	    String name = "";
	    if ( index < _clientConnections.size() ) {
	        NetConnection connection = _clientConnections.get(index);
	        name = connection.serviceName();	        
	    }
	    return name;
	}

	public NetConnection clientConnectionAtIndex(int index) {
	    if (index < _clientConnections.size())
	        return _clientConnections.get(index);
	    return null;
	}

	public int numberOfServerConnections() {
	    return _serverConnections.size();
	}

	public String nameOfServerConnectionAtIndex(int index) {
	    String name = "";
	    
	    if ( index < _serverConnections.size() ) {
	        NetConnection connect = _serverConnections.get(index);
	        name = connect.serviceName();
	    }
	    return name;
	}

	public NetConnection serverConnectionAtIndex(int index) {
	    if (index < _serverConnections.size())
	        return _serverConnections.get(index);
	    return null;
	}

	private void _fireUpdateNotification() {
	    //[[NSNotificationCenter defaultCenter] postNotificationName:kNetConnectionManagerConnectionsUpdated object:self];
	}

	@Override
	public boolean shouldAllowService(NsdServiceInfo service) {
		/*
		WahoooDevice host = WahoooDeviceManager.sharedManager().findHost(service.getServiceName(), "");
	    
	    if ( host )
	    {
	        //Auto-connect to host that already exists
	        [self performSelector:@selector(openConnectionToService:) withObject:host.name afterDelay:0.5f];
	    }
	    else if ( ![_availableServices containsObject:service] )
	    {
	        // Req# 5.In the Network Tab, remove the local device from being advertised as being available to connect to itself. Filter out local
	        // device so you can connect to your own device.
	        // Added condition to check if service being added is not the local device service
	        if(![_serviceName isEqualToString:service.name])
	        {
	            [_availableServices addObject:service];
	            
	            [self _fireUpdateNotification];
	        }
	    }
	    */
		return true;
	}

	@Override
	public void lostService(NsdServiceInfo service) {
		// TODO Auto-generated method stub
		_availableServices.remove(service);
	    _fireUpdateNotification();
	}

	@Override
	public void acceptSocket(NetServer netServer, Socket socket) {
		// TODO Auto-generated method stub
		NetConnection connection = new NetConnection(socket);
	    connection.state = ConnectionState.kServerState_Connecting;
	    connection.delegate = this;
	    
	    connection.connectToServer(); 
        _serverConnections.add(connection);
        _fireUpdateNotification();
	}

	@Override
	public void netConnectionReceivedData(NetConnection connection) {
		// TODO Auto-generated method stub
		int len = connection.readBuffer.length();
		byte[] readData = new byte[len];
		len = connection.readBuffer.get(readData, len);
	    
	    while( readData.length > 0 )
	    {
	        NetPacket packet;
			try {
				packet = NetPacket.unpack(readData, createPacketOfType);
				if ( packet != null ) {
		        	_processPacket(packet, connection);
		        }
		        else{
		            break;
		        }
			} catch (NetPacketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
	     }
	}

	@Override
	public void netConnectionDisconnected(NetConnection connection) {
		// TODO Auto-generated method stub
		
	}
	
	private void _processPacket(NetPacket packet, NetConnection connection) {
		/*
	    swith(packet.type) {
	        case kPacketType_ClientConnectRequest:
	        {
	            if ( connection.state == ConnectionState.kServerState_Connecting ) {
	                ClientConnectRequestPacket ccrp = (ClientConnectRequestPacket)packet;
	                
	                connection.setServiceName(ccrp.clientName());
	                connection.setServiceID(ccrp.clientID());
	                
	                WahoooDevice clientDevice = [[WahoooDeviceManager sharedManager] findClient:connection.serviceName andID:connection.serviceID];
	                
	                if ( clientDevice )
	                {
	                    [self _serverAcceptedConnection:connection];
	                }
	                else
	                {
	                    [self _showConnectionRequestAlert:connection];
	                    
	                    [self _fireUpdateNotification];
	                }
	            }
	        }
	            break;
	            
	        case kPacketType_ServerConnectResponse:
	            if ( connection.state == kClientState_WaitingForApproval )
	            {
	                ServerConnectResponsePacket* serverPacket = (ServerConnectResponsePacket*)packet;
	                
	                if ( serverPacket.approved )
	                {
	                    connection.state = kClientState_Connected;
	                    connection.serviceID = serverPacket.serverID;
	                    connection.serviceName = serverPacket.serverName;
	                    
	                    [[WahoooDeviceManager sharedManager] connectedToHost:connection];
	                }
	                else
	                {
	                    UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Connection Denied" message:[NSString stringWithFormat:@"Your request was denied by %@", serverPacket.serverName] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles: nil];
	                    
	                    [alertView show];
	                
	                    [connection close];
	                }
	            }
	            break;
	            
	        case kPacketType_KeepAlive:
	            //MRN: just ignore
	            break;
	            
	        default:
	            [[WahoooDeviceManager sharedManager] processPacket:packet fromConnection:connection];
	            break;
	    }
	    */
	}
	
	public IFPacketAllocator createPacketOfType = new IFPacketAllocator() {
		
		@Override
		public NetPacket alloc(short packetType) {
			// TODO Auto-generated method stub
			NetPacket packet = null;
			if (packetType == PacketType.kPacketType_ClientConnectRequest.getValue()) {
		        packet = new ClientConnectRequestPacket();
			} else if (packetType == PacketType.kPacketType_ServerConnectResponse.getValue()) {    
	            packet = new ServerConnectResponsePacket();
			} else if (packetType == PacketType.kPacketType_BandData.getValue()) {
	            packet = new BandDataPacket();
			} else if (packetType == PacketType.kPacketType_ServerStoppedSharing.getValue() ||
					packetType == PacketType.kPacketType_ClientStoppedObserving.getValue() ||
					packetType == PacketType.kPacketType_KeepAlive.getValue()) {
	            packet = new NetPacket(PacketType.fromValue(packetType));
			} else {
	            Logger.logError("NetConnectionManager::_createPacketOfType: UNKNOWN PACKET TYPE %d", packetType);
		            
		    }
		    return packet;
		}
	};
}
