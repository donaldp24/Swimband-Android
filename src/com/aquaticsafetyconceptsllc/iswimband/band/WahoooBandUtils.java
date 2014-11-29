package com.aquaticsafetyconceptsllc.iswimband.band;

import java.util.UUID;

public class WahoooBandUtils {
	
	public enum CBCentralManagerState {
		CBCentralManagerStateUnknown(0),
		CBCentralManagerStateResetting(1),
		CBCentralManagerStateUnsupported(2),
		CBCentralManagerStateUnauthorized(3),
		CBCentralManagerStatePoweredOff(4),
		CBCentralManagerStatePoweredOn(5);
		
		private int value;
		CBCentralManagerState(int value) {
			this.value = value;
		}
		public int getValue() {
			return this.value;
		}
	};

	
	public static String centralManagerStateToString(int state) {
		/*
	    switch(state) {
	        case CBCentralManagerState.CBCentralManagerStateUnknown.getValue():
	            return "State unknown (CBCentralManagerStateUnknown)";
	        case CBCentralManagerState.CBCentralManagerStateResetting.getValue():
	            return "State resetting (CBCentralManagerStateUnknown)";
	        case CBCentralManagerState.CBCentralManagerStateUnsupported.getValue():
	            return "State BLE unsupported (CBCentralManagerStateResetting)";
	        case CBCentralManagerState.CBCentralManagerStateUnauthorized.getValue():
	            return "State unauthorized (CBCentralManagerStateUnauthorized)";
	        case CBCentralManagerState.CBCentralManagerStatePoweredOff.getValue():
	            return "State BLE powered off (CBCentralManagerStatePoweredOff)";
	        case CBCentralManagerState.CBCentralManagerStatePoweredOn.getValue():
	            return "State powered up and ready (CBCentralManagerStatePoweredOn)";
	        default:
	            return "State unknown";
	    }*/
	    return "Unknown state";
	}

	public static String UUIDToString(UUID uuid) {
	    if (uuid == null)
	    	return "NULL";
	    String s = uuid.toString();
	    return s;
	}

	public static int compareUUID(UUID UUID1, UUID UUID2) {
	    if (UUID1.equals(UUID2))
	    	return 1;
	    return 0;
	}

	/*
	+ (CBService *)findServiceFromUUID:(CBUUID *)UUID p:(CBPeripheral *)p {
	    for(int i = 0; i < p.services.count; i++) {
	        CBService *s = [p.services objectAtIndex:i];
	        if ([self compareCBUUID:s.UUID UUID2:UUID]) return s;
	    }
	    return nil; //Service not found on this peripheral
	}

	+ (CBCharacteristic *)findCharacteristicFromUUID:(CBUUID *)UUID service:(CBService*)service {
	    for(int i=0; i < service.characteristics.count; i++) {
	        CBCharacteristic *c = [service.characteristics objectAtIndex:i];
	        if ([WahoooBandUtils compareCBUUID:c.UUID UUID2:UUID]) return c;
	    }
	    return nil; //Characteristic not found on this service
	}
	*/

	public static short swap(short s) {
	    short temp = (short) (s << 8);
	    temp |= (s >> 8);
	    return temp;
	}

	public static String generateGUID() {
	    UUID uuid = UUID.randomUUID();
	    return uuid.toString();
	}
}
