package com.aquaticsafetyconceptsllc.iswimband.Network;


import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.widget.Toast;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
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
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooDevice;
import com.aquaticsafetyconceptsllc.iswimband.band.WahoooDeviceManager;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import de.greenrobot.event.EventBus;

public class NetConnectionManager implements NetConnectionDelegate, NetServerDelegate, NetServerBrowserDelegate {

	public static final String TAG = "NetConnectionManager";

	private static NetConnectionManager _instance = null;
	private Context mContext;

	private NetServer _server;
	private NetServerBrowser _serviceBrowser;
	
	private ArrayList<NetConnection> _clientConnections;
	private ArrayList<NetConnection> _serverConnections;
	private ArrayList<NsdServiceInfo> _availableServices;
	
	private String _serviceName;
	private String _serviceID;

	public static final String kNetConnectionManagerConnectionsUpdated = "kNetConnectionManagerConnectionsUpdated";
	public static final String kNetConnectionManagerServerPublishFailed = "kNetConnectionManagerServerPublishFailed";
	public static final String kNetConnectionManagerServerPublishNameConflict = "kNetConnectionManagerServerPublishNameConflict";

	private Handler mHandler;

	public String serviceName() { return _serviceName; }
	public void setServiceName(String name) { _serviceName = name; }
	public String serviceID() { return _serviceID; }
	public void setServiceID(String serviceID) { _serviceID = serviceID; }
	
	private boolean _sharing;
	private Timer _keepAliveTimer;

	public static NetConnectionManager initialize(Context context) {
		if (_instance == null)
			_instance = new NetConnectionManager(context);
		return _instance;
	}
	
	public static NetConnectionManager sharedManager() {
		return _instance;
	}

	private NetConnectionManager(Context context) {

		mContext = context;

		_server = new NetServer(mContext);
		_serviceBrowser = new NetServerBrowser(mContext);

		_server.delegate = this;
		_serviceBrowser.delegate = this;
		
		_clientConnections = new ArrayList<NetConnection>();
		_serverConnections = new ArrayList<NetConnection>();
		_availableServices = new ArrayList<NsdServiceInfo>();

		_serviceID = Settings.sharedSettings().netServiceID();
		_serviceName = Settings.sharedSettings().netServiceName();

		_serviceBrowser.setLocalServerName(_serviceName);

		mHandler = new Handler(mContext.getMainLooper());
	    
	    //[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(_didEnterBackground:) name:UIApplicationDidEnterBackgroundNotification object:nil];
        //[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(_willEnterForeground:) name:UIApplicationWillEnterForegroundNotification object:nil];
	}
	
	public boolean startServer() {

		_serviceBrowser.setLocalServerName(_serviceName);

		if ( _serviceName != null && _serviceName.length() > 0) {
			if ( _keepAliveTimer == null ) {
				_keepAliveTimer = new Timer();
				_keepAliveTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						_sendKeepAlivePacket();
					}
				}, 1000, 1000);
			}
			return _server.startServerWithName(_serviceName);
		}

		return false;
	}
	
	public void stopServer() {
		if ( _keepAliveTimer != null ) {
			_keepAliveTimer.cancel(); _keepAliveTimer.purge();
			_keepAliveTimer = null;
		}

		_server.stopServer();

		ArrayList<NetConnection> clientConnections = new ArrayList<NetConnection>();
		clientConnections.addAll(_clientConnections);
		_clientConnections.clear();
		for (NetConnection connection : clientConnections ) {
			connection.close();
		}

		_sharing = false;

		_fireUpdateNotification();
	}
	
	public void startBrowser() {
		_serviceBrowser.startBrowser();
	}
	
	public void stopBrowser() {
		_serviceBrowser.stopBrowser();

		_availableServices.clear();

		ArrayList<NetConnection> serverConnections = new ArrayList<NetConnection>();
		serverConnections.addAll(_serverConnections);

		_serverConnections.clear();

		for ( NetConnection connection : serverConnections ) {
			connection.close();
		}

		_fireUpdateNotification();
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

		WahoooDeviceManager.sharedManager().clearDevices();
	}

	public boolean openConnectionToService(String serviceName) {

		Logger.l(TAG, "openConnectionToService(%s)", serviceName);

		NsdServiceInfo service = null;

		ArrayList<NsdServiceInfo> services = _serviceBrowser.getServices();

		for (NsdServiceInfo current : services) {
			if ( current.getServiceName().equals(serviceName) ) {
				service = current;
				break;
			}
		}

		if ( service == null ) {
			Logger.l(TAG, "openConnectionToService(%s), cannot find this serviceinfo, return false", serviceName);
			return false;
		}

		for (NetConnection currentConnection : _clientConnections) {
			if ( currentConnection.serviceName().equals(serviceName) ) {

				Logger.l(TAG, "openConnectionToService : %s, already exist in _clientConnections, return false", serviceName);

				_availableServices.remove(service);
				return false;
			}
		}

		NetConnection connection = new NetConnection();

		connection.state = ConnectionState.kClientState_Connecting;

		if ( connection.openConnectionWithNetService(service) ) {

			Logger.l(TAG, "openConnectionToService : %s, connection.openConnectionWithNetService() success", serviceName);

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
		else {
			Logger.l(TAG, "openConnectionToService : %s, connection.openConnectionWithNetService() failed, return false", serviceName);
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
		connection.close();

		WahoooDeviceManager.sharedManager().removeConnection(connection);
	}
	
	public int numberOfAvailableServices() {
	    return _availableServices.size();
	}
	
	public String nameOfAvailableServiceAtIndex(int index) {

	    String name = "";

	    if( index < _availableServices.size() ) {
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

	protected void _fireUpdateNotification() {
	    //[[NSNotificationCenter defaultCenter] postNotificationName:kNetConnectionManagerConnectionsUpdated object:self];
		EventBus.getDefault().post(new SEvent(kNetConnectionManagerConnectionsUpdated, this));
	}

	protected void _showConnectionRequestAlert(NetConnection connection) {

		String title = "Connection Request";

		String messageFormat = "%s is requesting permisison to connect.";

		final NetConnection final_connection = connection;

		new AlertDialog.Builder(mContext)
				.setTitle(title)
				.setMessage(messageFormat)
				.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// continue with delete
						_serverAcceptedConnection(final_connection);
					}
				})
				.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// do nothing
						if ( final_connection.isOpen() )
						{
							ServerConnectResponsePacket serverResponse = new ServerConnectResponsePacket();
							serverResponse.serverName = _serviceName;
							serverResponse.serverID = _serviceID;

							serverResponse.approved = false;
							final_connection.writeData(serverResponse.pack());
							final_connection.close();
						}
					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
	}

	protected void _didEnterBackground() {
		boolean sharing = _sharing;

		stopServer();
		stopBrowser();

		// restore sharing flag for when we resume
		_sharing = sharing;
	}

	protected void _willEnterForeground() {
		if ( _sharing ) {
			startServer();
			startBrowser();
		}
	}

	protected void _serverAcceptedConnection(NetConnection connection) {
		Logger.l(TAG, "_serverAcceptedConnection() : %s", connection.serviceName());

		ServerConnectResponsePacket serverResponse = new ServerConnectResponsePacket();
		serverResponse.serverName = _serviceName;
		serverResponse.serverID = _serviceID;

		if ( connection.isOpen() ) {
			serverResponse.approved = true;
			connection.writeData(serverResponse.pack());
			connection.state = ConnectionState.kServerState_Connected;
			WahoooDeviceManager.sharedManager().connectedToClient(connection);
		}
		else {
			Logger.l(TAG, "_serverAcceptedConnection : %s, not opened", connection.serviceName());
		}

	}

	protected void _sendKeepAlivePacket() {

		NetPacket packet = createPacketOfType.alloc(PacketType.kPacketType_KeepAlive.getValue());
		byte[] packetData = packet.pack();

		ArrayList<NetConnection> connections = new ArrayList<NetConnection>();
		connections.addAll(_serverConnections);

		long tooOld = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - 5;

		for (NetConnection connection : connections) {
			if ( connection.lastDataReceivedTime() < tooOld ) {
				connection.close();
			}
			else {
				connection.writeData(packetData);
			}
		}

		connections.clear();
		connections.addAll(_clientConnections);

		for (NetConnection connection : connections) {
			if ( connection.lastDataReceivedTime() < tooOld ) {
				connection.close();
			}
			else {
				connection.writeData(packetData);
			}
		}
	}

	//===================================================================
	// NetServerDelegate
	//===================================================================

	@Override
	public void acceptSocket(NetServer netServer, Socket socket) {

		Logger.l(TAG, "acceptSocket() : %s", netServer.serverName());

		NetConnection connection = new NetConnection();
		connection.state = ConnectionState.kServerState_Connecting;
		if (connection.openConnectionWithNativeSocket(socket)) {
			connection.delegate = this;
			_serverConnections.add(connection);
			_fireUpdateNotification();
		}
		else {
			Logger.l(TAG, "acceptSocket() : %s, connection.openConnectionWithNativeSocket() failed", netServer.serverName());
		}
	}

	@Override
	public void failedPublishWithError(NetServer netServer, int errorCode) {
		_sharing = false;

		/*
		switch( errorCode )
		{
			case NSNetServicesCollisionError:
				[[NSNotificationCenter defaultCenter] postNotificationName:kNetConnectionManagerServerPublishNameConflict object:self];
				break;

			default:
				[[NSNotificationCenter defaultCenter] postNotificationName:kNetConnectionManagerServerPublishFailed object:self];
				break;
		}
		*/
		EventBus.getDefault().post(new SEvent(kNetConnectionManagerServerPublishFailed, this));
	}

	@Override
	public void netServerDidPublish(NetServer netServer) {

		_sharing = true;

		Settings.sharedSettings().setNetServiceName(_serviceName);
	}


	//===================================================================
	//NetServerBrowserDelegate
	//===================================================================

	@Override
	public boolean shouldAllowService(NsdServiceInfo service) {

		WahoooDevice host = WahoooDeviceManager.sharedManager().findHost(service.getServiceName(), "");
	    
	    if ( host != null ) {
	        //Auto-connect to host that already exists
	        /*

			 final String hostName = host.name();
			 mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					openConnectionToService(hostName);
				}
			}, 500);

			*/
	    }
	    else if ( !_availableServices.contains(service) ) {
	        // Req# 5.In the Network Tab, remove the local device from being advertised as being available to connect to itself. Filter out local
	        // device so you can connect to your own device.
	        // Added condition to check if service being added is not the local device service
	        if(!_serviceName.equals(service.getServiceName())) {

				for (NsdServiceInfo existInfo : _availableServices) {
					if (existInfo.getServiceName().equals(service.getServiceName())) {
						// equal service
						Logger.l(TAG, "shouldAllowService, service(%s) already exist in _avaiableServices", service.getServiceName());
						return false;
					}
				}

	            // _availableServices.add(service);

				// _fireUpdateNotification();
	        }
	    }

		return true;
	}

	@Override
	public void resolvedService(NsdServiceInfo service) {
		WahoooDevice host = WahoooDeviceManager.sharedManager().findHost(service.getServiceName(), "");

		if ( host != null ) {
			//Auto-connect to host that already exists
			//[self performSelector:@selector(openConnectionToService:) withObject:host.name afterDelay:0.5f];
			final String hostName = host.name();
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					openConnectionToService(hostName);
				}
			}, 500);
		}
		else if ( !_availableServices.contains(service) ) {
			// Req# 5.In the Network Tab, remove the local device from being advertised as being available to connect to itself. Filter out local
			// device so you can connect to your own device.
			// Added condition to check if service being added is not the local device service
			if(!_serviceName.equals(service.getServiceName())) {

				for (NsdServiceInfo existInfo : _availableServices) {
					if (existInfo.getServiceName().equals(service.getServiceName())) {
						// equal service
						Logger.l(TAG, "shouldAllowService, service(%s) already exist in _avaiableServices", service.getServiceName());
						return;
					}
				}

				_availableServices.add(service);

				_fireUpdateNotification();
			}
		}
	}

	@Override
	public void lostService(NsdServiceInfo service) {
		// TODO Auto-generated method stub
		_availableServices.remove(service);
	    _fireUpdateNotification();
	}


	//************************************************************************************************************************
	// NetConnectionDelegate
	//************************************************************************************************************************
	@Override
	public void netConnectionDisconnected(NetConnection connection) {

		Logger.l(TAG, "netConnectionDisconnected() : %s", connection.serviceName());

		if ( _serverConnections.contains(connection)) {
			_serverConnections.remove(connection);

			_fireUpdateNotification();
		}
		else if (_clientConnections.contains(connection)) {
			WahoooDeviceManager.sharedManager().lostConnection(connection);

			_clientConnections.remove(connection);

			NsdServiceInfo service = _serviceBrowser.serviceWithName(connection.serviceName());

			if ( service != null ) {
				if ( !_availableServices.contains(service) ) {
					// Req# 5.In the Network Tab, remove the local device from being advertised as being available to connect to itself. Filter out local
					// device so you can connect to your own device.
					// Added condition to check if service being added is not the local device service
					if(!_serviceName.equals(service.getServiceName())) {
						_availableServices.add(service);
					}
				}
			}

			_fireUpdateNotification();
		}
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
				Logger.logError(TAG, "_createPacketOfType: UNKNOWN PACKET TYPE %d", packetType);

			}
			return packet;
		}
	};

	@Override
	public void netConnectionReceivedData(NetConnection connection) {

		Logger.l(TAG, "netConnectionReceivedData() : %s", connection.serviceName());

		int len = connection.readBuffer.length();
		byte[] readData = new byte[len];
		len = connection.readBuffer.get(readData, len);
	    
	    while (readData.length > 0) {
	        NetPacket packet;
			try {
				packet = NetPacket.unpack(readData, createPacketOfType);
				if ( packet != null ) {
		        	_processPacket(packet, connection);
		        }
		        else{
					Logger.l(TAG, "netConnectionReceivedData, NetPacket.unpack() is null! error!");
		            break;
		        }
			} catch (NetPacketException e) {
				e.printStackTrace();
				break;
			}

			len = connection.readBuffer.length();
			readData = new byte[len];
			len = connection.readBuffer.get(readData, len);
	     }
	}


	
	private void _processPacket(NetPacket packet, NetConnection connection) {
	    switch(packet.type) {
	        case kPacketType_ClientConnectRequest:
	        {
	            if ( connection.state == ConnectionState.kServerState_Connecting ) {
	                ClientConnectRequestPacket ccrp = (ClientConnectRequestPacket)packet;
	                
	                connection.setServiceName(ccrp.clientName());
	                connection.setServiceID(ccrp.clientID());
	                
	                WahoooDevice clientDevice = WahoooDeviceManager.sharedManager().findClient(connection.serviceName(), connection.serviceID());
	                
	                if ( clientDevice != null) {
	                    _serverAcceptedConnection(connection);
	                }
	                else {
	                    _showConnectionRequestAlert(connection);
	                    _fireUpdateNotification();
	                }
	            }
	        }
	            break;
	            
	        case kPacketType_ServerConnectResponse:
	            if ( connection.state == ConnectionState.kClientState_WaitingForApproval ) {
	                ServerConnectResponsePacket serverPacket = (ServerConnectResponsePacket)packet;
	                
	                if ( serverPacket.approved ) {
	                    connection.state = ConnectionState.kClientState_Connected;
	                    connection.setServiceID(serverPacket.serverID);
	                    connection.setServiceName(serverPacket.serverName);

						WahoooDeviceManager.sharedManager().connectedToHost(connection);
	                }
	                else {

						String message = String.format("Your request was denied by %s", serverPacket.serverName);
						new AlertDialog.Builder(mContext)
								.setTitle("Connection Denied")
								.setMessage(message)
								.setPositiveButton("OK", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
									}
								})
								.setIcon(android.R.drawable.ic_dialog_alert)
								.show();
	                    connection.close();
	                }
	            }
	            break;
	            
	        case kPacketType_KeepAlive:
	            //MRN: just ignore
	            break;
	            
	        default:
				WahoooDeviceManager.sharedManager().processPacket(packet, connection);
	            break;
	    }
	}
	

}
