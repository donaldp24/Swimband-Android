package com.aquaticsafetyconceptsllc.iswimband.band;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.aquaticsafetyconceptsllc.iswimband.Network.NetConnection;
import com.aquaticsafetyconceptsllc.iswimband.Network.Packets.NetPacket;

public class WahoooDeviceManager {
	private static WahoooDeviceManager _instance = null;
	
	private HashMap<String, WahoooDevice>    _hostDevices;
    private HashMap<String, WahoooDevice>    _clientDevices;
    public ArrayList<Object> devices() {
    	return null;
    }
    
    private WahoooDeviceManager() {
    	super();
    	
    	_hostDevices = new HashMap<String, WahoooDevice>();
    	_clientDevices = new HashMap<String, WahoooDevice>();
    }
    
    public WahoooDeviceManager sharedManager() {
    	if (_instance == null)
    		_instance = new WahoooDeviceManager();
    	return _instance;
    }

    public WahoooDevice addHost(String name, String deviceID) {
        return _addDeviceWithName(name, deviceID, _hostDevices);
    }

    public WahoooDevice addClient(String name, String deviceID) {
        return _addDeviceWithName(name, deviceID, _clientDevices);
    }

    public WahoooDevice findHost(String name, String deviceID) {
        WahoooDevice device = _hostDevices.get(deviceID);
        
        if ( device == null )
        {
            Collection<WahoooDevice> hosts = _hostDevices.values();
            
            for( WahoooDevice host : hosts ) {
                if ( host.name() == name ) {
                    return host;
                }
            }
        }
        
        return device;
    }

    public WahoooDevice findClient(String name, String deviceID) {
        WahoooDevice device = _clientDevices.get(deviceID);
        return device;
    }

    public WahoooDevice findDeviceForID(String deviceID) {
        WahoooDevice device = _hostDevices.get(deviceID);
        
        if (device == null ) {
            device = _clientDevices.get(deviceID);
        }
        
        return device;
    }

    public void lostConnection(NetConnection connection) {
        /*
        WahoooDevice device = _hostDevices.get(connection.serviceID);
        
        if ( device == null ) {
            device = _clientDevices.get(connection.serviceID);
        }
        
        if ( device ) {
            device.lostConnection();
        }
        */
    }

    public void connectedToHost(NetConnection connection) {
        /*
        WahoooDevice device = addHost(connection.serviceName, connection.serviceID);

        device.connection = connection;
        */
    }

    public void connectedToClient(NetConnection connection) {
        /*
        WahoooDevice device = addClient(connection.serviceName, connection.serviceID);
        
        device.connection = connection;
        */
    }

    public void removeConnection(NetConnection connection) {
        /*
        WahoooDevice device = findClient(connection.serviceName, connection.serviceID);
        
        if ( device != null ) {
            device.disconnect();
            _clientDevices.remove(connection.serviceID);
            
            return;
        }
        
        device = findHost(connection.serviceName, connection.serviceID);
        
        if ( device != null ) {
            device.disconnect();
            
            _hostDevices.remove(connection.serviceID);
            
        }
        */
    }

    public void processPacket(NetPacket packet, NetConnection connection) {
        /*
        WahoooDevice device = null;
        
        switch ( packet.type ) {
            case PacketType.kPacketType_BandData:
                device = findHost(connection.serviceName, connection.serviceID);
                break;
                
            case PacketType.kPacketType_ServerStoppedSharing:
                device = findHost(connection.serviceName, connection.serviceID);
                
                if ( device != null ) {
                    device.disconnect();
                    _hostDevices.remove(connection.serviceID);
                }
                
                return;
                
            case PacketType.kPacketType_ClientStoppedObserving:
                device = findClient(connection.serviceName, connection.serviceID);
                
                if ( device != null ) {
                    device.disconnect();
                    
                    _clientDevices.remove(connection.serviceID);
                }
                
                return;
                
            default:
                Logger.log("Unknown Packet Type %x", packet.type);
                break;
        }
        
        if ( device != null )
        {
            device.processPacket(packet);
        }
        */
    }

    public void clearDevices() {
        _hostDevices.clear();
        _clientDevices.clear();
        
        WahoooBandManager.sharedManager().clearRemoteBands();
    }

    public WahoooDevice _addDeviceWithName(String name, String deviceID, HashMap<String, WahoooDevice> dict) {
        WahoooDevice device = dict.get(deviceID);
        
        if ( device == null ) {
            device = new WahoooDevice(name, deviceID);
            
            dict.put(deviceID, device);
        }
        
        device.setName(name);
        
        return device;
    }
}
