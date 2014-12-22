package com.aquaticsafetyconceptsllc.iswimband.band;

import java.util.*;
import java.util.concurrent.TimeUnit;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import com.aquaticsafetyconceptsllc.iswimband.Ble.BleManager;
import com.aquaticsafetyconceptsllc.iswimband.Ble.BlePeripheral;
import com.aquaticsafetyconceptsllc.iswimband.CoreData.CoreDataManager;
import com.aquaticsafetyconceptsllc.iswimband.CoreData.SwimbandData;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;
import com.aquaticsafetyconceptsllc.iswimband.Utils.OSDate;

import com.aquaticsafetyconceptsllc.iswimband.Utils.ScheduleNotification;
import com.aquaticsafetyconceptsllc.iswimband.Utils.ScheduleNotificationManager;
import de.greenrobot.event.EventBus;

public class PeripheralBand extends WahoooBand {

    public static final String TAG = "PeripheralBand";


	public static final int SERVICE_LOCALID = 0xEBEA0000;

	public static final int UNPAIR_BANDSTATE = 1;
	
	public static final float UPDATE_DELAY = 0.5f;

	// If disconnected for more than an hour, ask if they want to continue using current settings
	public static final int LONG_DISCONNECT_TIME = 3600;

	public static final int REQUIRE_APP_UPDATE = 0;

	public static final int SKIP_SERIAL_NUMBER = 0;

	public static final int kAuthSize = 16;

    private static final int READCHARACTERISTIC_INTERVAL = 100;

    public static final int RSSI_THRESHOLD = -90;
    public static final int RSSI_TOLERANCE = 35;


	public static final String kPeripheralBandRequestingAuthenticationNotification = "kPeripheralBandRequestingAuthenticationNotification";
	public static final String kPeripheralBandAuthenticationFailedNotification = "kPeripheralBandAuthenticationFailedNotification";
	public static final String kPeripheralBandFirmwareUpdateRequiredNotification = "kPeripheralBandFirmwareUpdateRequiredNotification";
	public static final String kPeripheralBandAppUpdateRequiredNotification = "kPeripheralBandAppUpdateRequiredNotification";
	public static final String kPeripheralBandFirstTimeSetupNotification = "kPeripheralBandFirstTimeSetupNotification";
	public static final String kPeripheralBandConfirmSettingsNotification = "kPeripheralBandConfirmSettingsNotification";
	public static final String kPeripheralBandKey = "kPeripheralBandKey";

    public static final String kDeviceInformationReadSerialNumber = "device information read serial number";
    public static final String kBatteryServiceReadBatteryLevel = "battery service read battery level";
    public static final String kDeviceServiceReadFirmwareVersion = "kDeviceServiceReadFirmwareVersion";

	public static final String kPeripheralCurrentFirmwareVersion = "1";
	public static final String kPeripheralFirmwareHexName = "iswimband_1_0";
	public static final int kPeripheralFirmware_1_0_0_Offset = 0x6c0;
	public static final int kPeripheralFirmwareOffset = kPeripheralFirmware_1_0_0_Offset;
	
	public static class DeviceInformationService {
		public static UUID deviceInformationServiceID() {
			return UUID.fromString(BleManager.getLongUuidFromShortUuid("180a"));
		}
	
		public static UUID manufacturerNameID() {
			return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A29"));
		}
	
		public static UUID modelNumberID() {
			return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A24"));
		}
	
		public static UUID serialNumberID() {
			return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A25"));
		}
	
		public static UUID hardwareRevisionID() {
			return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A27"));
		}
	
		public static UUID firmwareRevisionID() {
			return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A26"));
		}
	
		public static UUID softwareRevisionID() {
			return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A28"));
		}
	
		public static UUID systemIDID() {
			return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A23"));
		}
		
		public String manufacturerName;

		public String modelNumber;

		public String serialNumber;

		public String hardwareRevision;

		public String firmwareRevision;

		public String softwareRevision;

        private BluetoothGattService _service;
        private BlePeripheral _peripheral;
        public void setService(BlePeripheral peripheral, BluetoothGattService service) {
            this._service = service;
            this._peripheral = peripheral;

            // read whole characteristics
            Iterator ci = service.getCharacteristics().iterator();
            while(ci.hasNext()) {
                BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic)ci.next();
                if (ch.getUuid().equals(serialNumberID())) {
                    peripheral.readCharacteristic(ch);
                    try {
                        Thread.sleep(READCHARACTERISTIC_INTERVAL);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void readCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
            if (characteristic.getUuid().equals(serialNumberID())) {
                Logger.log("read serial number --------------- ");
                try {
                    String key = new String(value, "utf-8");
                    Logger.log("read serial number : %s", key);

                    String remains = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                    String cleanedkey = "";
                    for (int i = 0; i < key.length(); i++) {
                        if (remains.contains(key.charAt(i) + ""))
                            cleanedkey = cleanedkey + key.charAt(i);
                    }

                    serialNumber = cleanedkey;
                    serialNumber = serialNumber.toUpperCase();
                    Logger.log("read serial number : %s", serialNumber);

                    _peripheral.setSerialNo(serialNumber);


                    // start validation - it is called in authenticationCompleteForiDevicesService on iOS,
                    // android version have no authenticating process!
                    // _startKeyValidation();

                    EventBus.getDefault().post(new SEvent(kDeviceInformationReadSerialNumber, _peripheral));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
	}

    public static class BatteryService {
        public static UUID batteryServiceID() {
            return UUID.fromString(BleManager.getLongUuidFromShortUuid("180f"));
            //return UUID.fromString("EBEA0000-473C-48F7-AEBA-3C9CB39C1A31");
        }

        public static UUID batteryLevelID() {
            return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A19"));
        }

        private BluetoothGattService _service;
        private BlePeripheral _peripheral;


        public int batteryLevel;


        public void setService(BlePeripheral peripheral, BluetoothGattService service) {
            this._peripheral = peripheral;
            this._service = service;

            // read whole characteristics
            Iterator ci = service.getCharacteristics().iterator();
            while(ci.hasNext()) {
                BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic)ci.next();
                peripheral.readCharacteristic(ch);
                try {
                    Thread.sleep(READCHARACTERISTIC_INTERVAL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void readCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
            if (characteristic.getService() == _service) {
                Logger.log("readCharacteristic, batteryService");
                if (characteristic.getUuid().equals(BatteryService.batteryLevelID())) {
                    Logger.log("readCharacteristic, battery_level characteristic");
                    if (value.length >= 1) {
                        batteryLevel = (int) value[0];
                        Logger.log("readCharacteristic, battery_level characteristic - %d", batteryLevel);

                        EventBus.getDefault().post(new SEvent(kBatteryServiceReadBatteryLevel, _peripheral));
                    }
                }
            }
        }
    }

    public static class iDevicesService {

        public static UUID iDevicesServiceId() {
            return UUID.fromString("64AC0000-4A4B-4B58-9F37-94D3C52FFDF7");
        }

        public static UUID firmwareVersionID() {
            return UUID.fromString("64AC0001-4A4B-4B58-9F37-94D3C52FFDF7");
        }

        private BluetoothGattService _service;
        private BlePeripheral _peripheral;

        public String firmwareVersion;

        public void setService(BlePeripheral peripheral, BluetoothGattService service) {
            this._peripheral = peripheral;
            this._service = service;
            this.firmwareVersion = "1.0";

            // read whole characteristics
            Iterator ci = service.getCharacteristics().iterator();
            while(ci.hasNext()) {
                BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic)ci.next();
                if (ch.getUuid().equals(firmwareVersionID())) {
                    peripheral.readCharacteristic(ch);
                    try {
                        Thread.sleep(READCHARACTERISTIC_INTERVAL);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void beginAuthentication() {
            //
        }

        public void readCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
            if (characteristic.getUuid().equals(firmwareVersionID())) {
                Logger.log("read firmware version --------------- ");
                try {
                    String key = new String(value, "utf-8");
                    Logger.log("read firmware version : %s", key);

                    String remains = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.";
                    String cleanedkey = "";
                    for (int i = 0; i < key.length(); i++) {
                        if (remains.contains(key.charAt(i) + ""))
                            cleanedkey = cleanedkey + key.charAt(i);
                    }

                    firmwareVersion = cleanedkey;
                    Logger.log("read firmware version : %s", firmwareVersion);

                    EventBus.getDefault().post(new SEvent(kDeviceServiceReadFirmwareVersion, _peripheral));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

	
	public static class RSSIEntry {
		int value;
		long timeStamp;
	}

	public enum PeripheralBandState
	{
	    kPeripheralBandState_Connecting(0),
	    kPeripheralBandState_Authenticating(1),
	    kPeripheralBandState_ValidatingKey(2),
	    kPeripheralBandState_ValidatingFirmware(3),
	    kPeripheralBandState_Ready(4),
	    kPeripheralBandState_Disconnected(5);

        int value;
        PeripheralBandState(int value) {
            this.value = value;
        }

        int getValue() {
            return value;
        }
	}
	
	public static int kPeripheralBandState_Total = 6;
	
	BlePeripheral        _peripheral;
	
	DeviceInformationService _deviceInfoService;
    BatteryService          _batteryService;
    iDevicesService         _iDevicesService;
    
    Timer                _alertTimer;
    Timer                _warningTimer;
    Timer                _updateSignalTimer;
    ScheduleNotification _alertNotification;
    long				_disconnectTime;
    
    
    ArrayList<RSSIEntry>    _rssiHistory;
    
    SwimbandData           _bandData;
    
    PeripheralBandState     _perphState;
    
    byte[]				_appChallenge = new byte[kAuthSize];
    
    boolean				_restoredFromPeripheral;

    private         Handler validationHandler = new Handler();

    public SwimbandData bandData() {
        return _bandData;
    }

    public void setBandState(WahoooBandState_t state) {

        _bandState = state;

        if( state == WahoooBandState_t.kWahoooBandState_Connected ) {
            cancelPanicAlert();
        }

        EventBus.getDefault().post(new SEvent(kWahoooBandDataUpdatedNotification, this));
    }

    public boolean setAuthenticationKey(String key) {

        Logger.log("setAuthenticationKey (%s)", key);

        String remains = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String cleanedkey = "";
        for (int i = 0; i < key.length(); i++) {
            if (remains.contains(key.charAt(i) + ""))
                cleanedkey = cleanedkey + key.charAt(i);
        }

        //TODO: send key to authentication characteristic
        if (!_deviceInfoService.serialNumber.equalsIgnoreCase(cleanedkey)) {
            Logger.log("setAuthenticationKey (%s, %s) is not equal %s, return false", address(), key, cleanedkey);
            return false;
        }

        _bandData.authKey = cleanedkey;

        CoreDataManager.sharedInstance().saveSwimbandData(_bandData);

        _startFirmwareValidation();

        return true;
    }

    public WahoooBandType type() {
        return WahoooBandType.kWahoooBand_Peripheral;
    }

    public String address() {
        return _peripheral.address();
    }

    public String displayName() {
        if ( _peripheral != null && (_name == null || _name.length() == 0) ) {
            return _peripheral.name();
        }

        return _name;
    }

    public String defaultName() {
        if ( _peripheral != null) {
            return _peripheral.name();
        }

        return _name;
    }

    public void setFwVersion(String fwVersion) {

    }

    public String fwVersion() {
        if ( _iDevicesService != null ) {
           return _iDevicesService.firmwareVersion;
        }

        return null;
    }


    public PeripheralBand() {
        super();

        _batteryService = new BatteryService();
        //_batteryService.delegate = self;
        //[_batteryService enableBatterLevelNotify:YES];


        _deviceInfoService = new DeviceInformationService();

        _iDevicesService = new iDevicesService();
        //_iDevicesService.delegate = self;

        //[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(_blePeripheralConnected:) name:kBLEManagerConnectedPeripheralNotification object:nil];

        //[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(_blePeripheralDisconnected:) name:kBLEManagerDisconnectedPeripheralNotification object:nil];

        //[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(_simPeripheralConnected:) name:kSimManagerConnectedPeripheralNotification object:nil];

        //[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(_simPeripheralDisconnected:) name:kSimManagerDisconnectedPeripheralNotification object:nil];

        _rssiHistory = new ArrayList<RSSIEntry>();

        //_profileObject = [[ProfileObject alloc] initWithFile:@"ProfileData"];

        _restoredFromPeripheral = false;

        EventBus.getDefault().register(this);

    }

    public void onEventMainThread(SEvent e) {
        if (BleManager.kBLEManagerConnectedPeripheralNotification.equalsIgnoreCase(e.name)) {
            _blePeripheralConnected((BlePeripheral)e.object);
        } else if (BleManager.kBLEManagerDisconnectedPeripheralNotification.equalsIgnoreCase(e.name)) {
            _blePeripheralDisconnected((BlePeripheral)e.object);
        } else if (BleManager.kBLEManagerPeripheralServiceDiscovered.equalsIgnoreCase(e.name)) {
            _retrieveCharacteristics((BlePeripheral)e.object);
        } else if (BleManager.kBLEManagerPeripheralDataAvailable.equalsIgnoreCase(e.name)) {
            BleManager.CharacteristicData data = (BleManager.CharacteristicData)e.object;
            _readCharacteristic(data.peripheral, data.characteristic, data.value);
        } else if (BleManager.kBLEManagerPeripheralRssiUpdated.equalsIgnoreCase(e.name)) {
            _rssiUpdated((BlePeripheral)e.object);
        } else if (kDeviceInformationReadSerialNumber.equalsIgnoreCase(e.name)) {
            _readDeviceSerialNumber((BlePeripheral)e.object);
        } else if (kBatteryServiceReadBatteryLevel.equalsIgnoreCase(e.name)) {
            _updatedBatteryLevel((BlePeripheral)e.object);
        } else if (kDeviceServiceReadFirmwareVersion.equalsIgnoreCase(e.name)) {
            _readFirmwareVersion((BlePeripheral)e.object);
        }
    }

    public BlePeripheral peripheral() {
        return _peripheral;
    }

    public void setPeripheral(BlePeripheral peripheral) {
        _peripheral = peripheral;

        //Store these so we know what the source type was if the
        // peripheral source gets disconnected
        // Also because peripheral is a weak reference and will become nil if the
        // instance is released
        if ( peripheral != null ) {
            if ( _peripheral.address() != null ) {
                _bandID = _peripheral.address();

                _loadStoredBandData();
            }

            if ( peripheral.connectionState() == BlePeripheral.STATE_CONNECTED) {
                if ( _bandState == WahoooBandState_t.kWahoooBandState_NotConnected ) {
                    _peripheralConnected(peripheral);
                }
            }
        }
    }

    public void changeName(String newName) {

        Logger.log("PeripheralBand.changedName - newName : %s", newName);

        //String stripped = [newName stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
        String stripped = newName;

        //MRN: Don't allow substring of default name to be stored as the name
        if ( _nameIsSubStringOfDefault(stripped) ) {
            stripped = "";
            Logger.log("PeripheralBand.changedName - _nameIsSubStringOfDefault");
        }

        _bandData.name = stripped;

        setName(stripped);

        CoreDataManager.sharedInstance().saveSwimbandData(_bandData);
    }


    public void disconnect() {

        super.disconnect();

        if ( _alertTimer != null ) {
            _alertTimer.cancel(); _alertTimer.purge();

            _alertTimer = null;
        }

        if ( _warningTimer != null ) {
            _warningTimer.cancel(); _warningTimer.purge();

            _warningTimer = null;
        }


        if ( _alertNotification != null ) {
            ScheduleNotificationManager.sharedInstance().cancelNotification(_alertNotification);

            _alertNotification = null;
        }


        if ( _updateSignalTimer != null ) {
            _updateSignalTimer.cancel(); _updateSignalTimer.purge();

            _updateSignalTimer = null;
        }

        _bandData.disconnectTime = (double)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        CoreDataManager.sharedInstance().saveSwimbandData(_bandData);

        EventBus.getDefault().unregister(this);
    }

    public void panicAlert() {
        if ( _perphState != PeripheralBandState.kPeripheralBandState_Ready ) {
            Logger.l(TAG, "panicAler : _perphStat is Ready!");
            super.panicAlert();
        }
        else {
            Logger.l(TAG, "panicAler : but _perphStat is not Ready, return");
            return;
        }
    }

    public void cancelPanicAlert() {
        super.cancelPanicAlert();

        if ( _alertTimer != null) {
            _alertTimer.cancel(); _alertTimer.purge();

            _alertTimer = null;
        }

        if ( _warningTimer != null) {
            _warningTimer.cancel(); _warningTimer.purge();

            _warningTimer = null;
        }


        if ( _alertNotification != null ) {
            ScheduleNotificationManager.sharedInstance().cancelNotification(_alertNotification);
            _alertNotification = null;
        }

    }

    public void startUpgrade() {
        setBandState(WahoooBandState_t.kWahoooBandState_OTAUpgrade);

        //[_iDevicesService startFirmwareUpdate:[[NSBundle mainBundle] pathForResource:kPeripheralFirmwareHexName ofType:@"hex"] withOffset:kPeripheralFirmwareOffset];

    }

    public int upgradeProgress() {
        return _updateProgress;
    }

    protected void _updateValues() {
        if ( _peripheral != null) {
            //_profileObject.delegate = self;
            //_profileObject.peripheral = _peripheral;
        }
    }

    protected void _peripheralConnected(BlePeripheral peripheral) {

        Logger.log("PeripheralBand._peripheralConnected - peripheral(%s)(%s)", peripheral.address(), peripheral.name());

        if ( peripheral == _peripheral ) {
            _restoredFromPeripheral = false;

            _fwVersion = null;

            if( _bandState == WahoooBandState_t.kWahoooBandState_NotConnected ) {
                _bandState = WahoooBandState_t.kWahoooBandState_Connecting;
            }

            _perphState = PeripheralBandState.kPeripheralBandState_Connecting;

            _bandID = _peripheral.address();

            _loadStoredBandData();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    _updateValues();
                }
            }, (int)(UPDATE_DELAY * TimeUnit.SECONDS.toMillis(1)));

            //[[NSNotificationCenter defaultCenter] postNotificationName:kWahoooBandDataUpdatedNotification object:self];
            EventBus.getDefault().post(new SEvent(kWahoooBandDataUpdatedNotification, this));

            if( _updateSignalTimer != null )
            {
                _updateSignalTimer.cancel(); _updateSignalTimer.purge();

                _updateSignalTimer = null;
            }


            _updateSignalTimer = new Timer();
            _updateSignalTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    _rssiTimer();
                }
            }, 100, 1000);


        }
        else {
            Logger.log("PeripheralBand._peripheralConnected - peripheral(%s)(%s) is not equal band.peripheral", peripheral.address(), peripheral.name());
        }
    }

    protected void _peripheralDisconnected(BlePeripheral peripheral) {
        if ( peripheral == _peripheral ) {
            _restoredFromPeripheral = false;
            _disconnectTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

            _bandData.disconnectTime = (double)_disconnectTime;
            CoreDataManager.sharedInstance().saveSwimbandData(_bandData);

            int alertDelay = _getAlertDelay();
            int warningDelay = _getWarningDelay();

            Logger.l(TAG, "PeripheralBand._peripheralDisconnected : alertDelay : %d, warningDelay : %d", alertDelay, warningDelay);

            //_iDevicesService.peripheral = nil;
            _perphState = PeripheralBandState.kPeripheralBandState_Disconnected;

            if ( _bandState == WahoooBandState_t.kWahoooBandState_Connecting &&
                    _perphState.getValue() <= PeripheralBandState.kPeripheralBandState_Authenticating.getValue() ) {
                Logger.log("PeripheralBand._peripheralDisconnected - status < authenticating, so disconnect it & return");
                WahoooBandManager.sharedManager().disconnect(this);
                return;
            }
            else if ( _bandState == WahoooBandState_t.kWahoooBandState_Connected )
            {
                {
                    if ( alertDelay > 0 )
                    {
                        if ( warningDelay > 0 )
                        {

                        }
                        else
                        {
                            _bandState = WahoooBandState_t.kWahoooBandState_Caution;
                        }

                        _setupAlertTimer();
                        _scheduleLocalNotification();
                    }
                    else
                    {
                        _bandState = WahoooBandState_t.kWahoooBandState_Alarm;
                        panicAlert();
                    }
                }

            }
            else if ( _bandState == WahoooBandState_t.kWahoooBandState_OTAUpgrade)
            {
                _bandState = WahoooBandState_t.kWahoooBandState_NotConnected;
            }

            if ( _updateSignalTimer != null )
            {
                _updateSignalTimer.cancel(); _updateSignalTimer.purge();

                _updateSignalTimer = null;
            }

            //[[NSNotificationCenter defaultCenter] postNotificationName:kWahoooBandDataUpdatedNotification object:self];
            EventBus.getDefault().post(new SEvent(kWahoooBandDataUpdatedNotification, this));
        }
        else {
            Logger.log("_peripheralDisconnected , _peripheral(%s) is not equal (%s)", _peripheral.address(), peripheral.address());
        }
    }

    protected void _blePeripheralConnected(BlePeripheral peripheral) {
        _peripheralConnected(peripheral);
    }

    protected void _blePeripheralDisconnected(BlePeripheral peripheral) {
        _peripheralDisconnected(peripheral);
    }

    protected void _retrieveCharacteristics(BlePeripheral peripheral) {
        if (_peripheral != peripheral) {
            //Logger.log("_retrieveCharacteristics , _peripheral(%s) is not equal (%s)", _peripheral.address(), peripheral.address());
            return;
        }

        Logger.log("_retrieveCharacteristics , _peripheral(%s)", _peripheral.address(), peripheral.address());

        List<BluetoothGattService> serviceList = peripheral.getSupportedGattServices();
        Iterator i = serviceList.iterator(); // Must be in synchronized block
        while (i.hasNext()) {
            BluetoothGattService service = (BluetoothGattService)i.next();
            if (service.getUuid().equals(BatteryService.batteryServiceID())) {
                Logger.log("_retrieveCharacteristics , _peripheral(%s), batteryService", _peripheral.address());
                _batteryService.setService(peripheral, service);
            }
            else if (service.getUuid().equals(DeviceInformationService.deviceInformationServiceID())) {
                Logger.log("_retrieveCharacteristics , _peripheral(%s), deviceInfoService", _peripheral.address());
                _deviceInfoService.setService(peripheral, service);
            }
            else if (service.getUuid().equals(iDevicesService.iDevicesServiceId())) {
                Logger.log("_retrieveCharacteristics , _peripheral(%s), iDevicesService", _peripheral.address());
                _iDevicesService.setService(peripheral, service);
                _iDevicesService.beginAuthentication();
            }
        }
    }

    protected void _readCharacteristic(BlePeripheral peripheral, BluetoothGattCharacteristic characteristic, byte[] value) {
        if (peripheral != _peripheral) {
            //Logger.log("_readCharacteristic , _peripheral(%s), is not equal peripheral(%s)", _peripheral.address(), peripheral.address());
            return;
        }
        // read value for this characteristic
        if (characteristic.getService() == _deviceInfoService._service) {
            Logger.log("_readCharacteristic , _peripheral(%s), _deviceInfoService", _peripheral.address());
            _deviceInfoService.readCharacteristic(characteristic, value);
        }
        else if (characteristic.getService() == _batteryService._service) {
            Logger.log("_readCharacteristic , _peripheral(%s), _batteryService", _peripheral.address());
            _batteryService.readCharacteristic(characteristic, value);
        }
        else if (characteristic.getService() == _iDevicesService._service) {
            Logger.log("_readCharacteristic, _peripheral(%s), _iDevicesService", _peripheral.address());
            _iDevicesService.readCharacteristic(characteristic, value);
        }
    }

    protected void _setupAlertTimer() {
        if ( _warningTimer != null ) {
            _warningTimer.cancel(); _warningTimer.purge();

            _warningTimer = null;
        }

        if ( _alertTimer != null ) {
            _alertTimer.cancel(); _alertTimer.purge();

            _alertTimer = null;
        }

        int alertDelay = _getAlertDelay();
        int warningDelay = _getWarningDelay();

        int now = (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        int duration = ((int)_disconnectTime + alertDelay - now);
        if (duration < 0)
            duration = 0;

        Logger.l(TAG, "duration for alert : %d", duration);


        if (warningDelay > 0) {

            int interval = (int)_disconnectTime + warningDelay - now;
            if (interval < 0)
                interval = 0;

            Logger.l(TAG, "interval for warning alert : %d", interval);

            OSDate dt = new OSDate().offsetSecond(interval);

            _warningTimer = new Timer();
            _warningTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    _setWarningState();
                }
            }, dt);
        }

        OSDate dtAlert = new OSDate().offsetSecond(duration);
        _alertTimer = new Timer();
        _alertTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                panicAlert();
            }
        }, dtAlert);
    }

    protected int _getAlertDelay() {
        int alertTime = _alertTime;

        if ( _alertType == WahoooAlertType.kWahoooAlertType_NonSwimmer ) {
            alertTime = WAHOOBAND_NONSWIMMER_ALERT_TIME;
        }

        return alertTime;
    }

    protected int _getWarningDelay() {
        int alertTime = _getAlertDelay();
        int warningTime = _warningTime;

        if ( _alertType == WahoooAlertType.kWahoooAlertType_NonSwimmer ) {
            Logger.l(TAG, "type is no swimmer : warningTime = 0");
            warningTime = 0;
        }
        else {
            warningTime = alertTime - warningTime;
            if (warningTime < 0)
                warningTime = 0;
        }

        return warningTime;

    }

    protected void _setWarningState() {
        if ( _bandState == WahoooBandState_t.kWahoooBandState_Connected ) {
            Logger.log("PeripheralBand._setWarningState() - set _bandState = Caution");
            _bandState = WahoooBandState_t.kWahoooBandState_Caution;

            //[[NSNotificationCenter defaultCenter] postNotificationName:kWahoooBandDataUpdatedNotification object:self];
            EventBus.getDefault().post(new SEvent(kWahoooBandDataUpdatedNotification, this));
        }

        _warningTimer = null;
    }

    protected void _scheduleLocalNotification() {
        int fireTime = (int)_disconnectTime + _alertTime;

        if ( _alertType == WahoooAlertType.kWahoooAlertType_NonSwimmer ) {
            fireTime = (int)_disconnectTime + WAHOOBAND_NONSWIMMER_ALERT_TIME;
        }


        if ( _alertNotification != null )
        {
            ScheduleNotificationManager.sharedInstance().cancelNotification(_alertNotification);

            _alertNotification = null;
        }

        _alertNotification = new ScheduleNotification();
        _alertNotification.setTitle("Attention!");
        _alertNotification.setAlertBody("An iSwimband requires your attention!");

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(TimeUnit.SECONDS.toMillis(fireTime));
        Date fireDate = cal.getTime();

        _alertNotification.setFireDate(fireDate);
        ScheduleNotificationManager.sharedInstance().scheduleNotification(_alertNotification);
    }

    protected void _readDeviceSerialNumber(BlePeripheral peripheral) {
        if (peripheral == _peripheral) {
            Logger.log("_readDevicesSerialNumber (%s)", peripheral.address());
            _startAuthentication();
            _startKeyValidation();
        }
    }

    protected void _updatedBatteryLevel(BlePeripheral peripheral) {
        if (peripheral == _peripheral) {
            _batteryLevel = _batteryService.batteryLevel;

            EventBus.getDefault().post(new SEvent(kWahoooBandDataUpdatedNotification, this));
        }
    }

    protected void _readFirmwareVersion(BlePeripheral peripheral) {
        if (peripheral == _peripheral) {

            _fwVersion = _iDevicesService.firmwareVersion;

            EventBus.getDefault().post(new SEvent(kWahoooBandDataUpdatedNotification, this));
        }
    }

    protected void _rssiUpdated(BlePeripheral peripheral) {
        if (peripheral == _peripheral) {
            RSSIEntry entry = new RSSIEntry();

            entry.value = peripheral.rssi();
            Logger.l(TAG, "rssi : %d", entry.value);
            entry.timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

            _rssiHistory.add(0, entry);
            if (entry.value < RSSI_THRESHOLD &&
                    _rssiHistory.size() > 2 &&
                    _rssiHistory.get(1).value < RSSI_THRESHOLD &&
                    _rssiHistory.get(2).value < RSSI_THRESHOLD) {

                    Logger.l(TAG, "rssi(%d) is low (3 times), forcely disconnecting....", entry.value);
                    // this is disconnected state, forcely close connection
                    _peripheral.disconnect();
            }
            else {
                //
            }

            _calcuateRSSIVelocity();

            //[[NSNotificationCenter defaultCenter] postNotificationName:kWahoooBandDataUpdatedNotification object:self];

            EventBus.getDefault().post(new SEvent(kWahoooBandDataUpdatedNotification, this));
        }
    }

    protected void _calcuateRSSIVelocity() {
        int sum = 0;
        int time = 0;
        int maxTime = 10;
        // average out the last 10 seconds
        _rssiVelocity = 0;
        int maxEntries = kMedianRSSIEntryCount;

        if ( _rssiHistory.size() > 1 ) {
            if  ( !kUseMedianRSSI ) {
                RSSIEntry current = _rssiHistory.get(0);
                RSSIEntry first = null;

                int i = 0;
                int count = 0;
                for ( i = 0; (i < _rssiHistory.size()); i++ ) {

                    if ( i >= maxEntries )
                        break;

                    count++;

                    first = _rssiHistory.get(i);

                    sum += first.value;
                }

                //Weight the most recent more
                sum += current.value;
                count++;

                if ( count > 0 )
                    _rssi  = sum / count;
            }
            else {
                ArrayList<RSSIEntry> sorted = new ArrayList<RSSIEntry>();
                int length = (maxEntries < _rssiHistory.size()) ? maxEntries : _rssiHistory.size();
                for (int j = 0; j < length; j++) {
                    sorted.add(_rssiHistory.get(j));
                }

                Collections.sort(sorted, new Comparator<RSSIEntry>() {
                    @Override
                    public int compare(RSSIEntry lhs, RSSIEntry rhs) {
                        if (lhs.value < rhs.value)
                            return -1;
                        if (lhs.value > rhs.value)
                            return 1;
                        return 0;
                    }
                });

                int idx = sorted.size() / 2;

                RSSIEntry median = sorted.get(idx);

                _rssi = median.value;
            }

            // Delete remaining entries

            if ( _rssiHistory.size() > maxEntries )
            {
                int size = _rssiHistory.size();
                for (int j = 0; j < size - maxEntries; j++)
                    _rssiHistory.remove(_rssiHistory.size() - 1);
            }
        }
        else if (_rssiHistory.size() == 1) {
            RSSIEntry firstEntry = _rssiHistory.get(0);
            _rssi = firstEntry.value;
        }
        else {
            _rssi = 0;
        }


        {
            float diff = _rssi - kBaseRSSIValue;
            float exp = diff / 6.f;
            float factor = (float)Math.pow(0.5, exp);

            int bars = 5;

            if (_rssi < -87)
            {
                bars = 0;
            }
            else if (_rssi < -82)
            {
                bars = 1;
            }
            else if (_rssi < -77)
            {
                bars = 2;
            }
            else if (_rssi < -72)
            {
                bars = 3;
            }
            else if (_rssi < -67)
            {
                bars = 4;
            }

            //_range = kBaseDistance * factor;
            _range = kBaseDistance * ((5 - bars) / 5.0f);
        }
    }

    protected void _loadStoredBandData() {
        if ( _bandData == null ) {
            _bandData = CoreDataManager.sharedInstance().getSwimbandData(_bandID);
            if (_bandData == null)
                _bandData = SwimbandData.createSwimbandData();

            if ( _bandData.bandId == null || _bandData.bandId.length() == 0) {
                _bandData.bandId = _bandID;
            }

            if ( _bandData.alarmTime == 0 ) {
                _bandData.alarmTime = WAHOOBAND_DEFAULT_ALERT_TIME;
            }

            if ( _bandData.warningTime == 0 ) {
                _bandData.warningTime = WAHOOBAND_DEFAULT_WARNING_TIME;
            }

            if ( _bandData.alarmType == 0 ) {
                _bandData.alarmType = WahoooAlertType.kWahoooAlertType_NonSwimmer.getValue();
            }

            if ( _bandData.authKey == null || _bandData.authKey.equals("")) {
                // Push authentication window
            }

            if ( _bandData.name == null || _bandData.name.equals("") ) {
                _bandData.name = name();
            }
            else if (_nameIsSubStringOfDefault(_bandData.name)) {
                //MRN: Fix stored names that might be substrings of the default band name which
                // is used to confirm ownership of the band because screw security.
                setName("");
            }
            else {
                setName(_bandData.name);
            }

            //[[WahoooBandManager sharedManager] saveBandData];
            CoreDataManager.sharedInstance().saveSwimbandData(_bandData);

        }

        _alertTime = _bandData.alarmTime;
        _warningTime = _bandData.warningTime;
        if (_bandData.alarmType == 0)
            _alertType = WahoooAlertType.kWahoooAlertType_NonSwimmer;
        else
            _alertType = WahoooAlertType.kWahoooAlertType_Swimmer;
    }

    protected void _startAuthentication() {
        Logger.log("_startAuthentication (%s)", address());
        if(_perphState == PeripheralBandState.kPeripheralBandState_Connecting) {
            _perphState = PeripheralBandState.kPeripheralBandState_Authenticating;
            //[_iDevicesService beginAuthentication];
        } else
            Logger.log("_startAuthentication (%s), error, state is not _connecting", address());
    }

    /**
     * this function is called in authenticationCompleteForiDevicesService on iOS
     * android version has no authentication process, so this is called after getting SN
     */
    protected void _startKeyValidation() {
        /*
        #if SKIP_SERIAL_NUMBER

            _perphState = kPeripheralBandState_ValidatingKey;

        [self _startFirmwareValidation];

        #else
        if (
        #if !UNPAIR_BANDSTATE
        self.bandState == kWahoooBandState_Connecting &&
        #endif
        _perphState == kPeripheralBandState_Authenticating )
        {
            //TODO: Check to see if value already stored, otherwise prompt
            _perphState = kPeripheralBandState_ValidatingKey;

            [self _validateKey];
        }
        #endif
        */
        if (_perphState == PeripheralBandState.kPeripheralBandState_Authenticating ) {
            Logger.log("_startKeyValidation - authenticating => validatingkey");
            _perphState = PeripheralBandState.kPeripheralBandState_ValidatingKey;
            _validateKey();
        } else {
            Logger.log("_startKeyValidation - peripheral state is not authentication");
        }
    }

    protected void _validateKey() {
        Logger.log("_validateKey - --- ---");
        if (_perphState == PeripheralBandState.kPeripheralBandState_ValidatingKey) {
            Logger.log("_validateKey - peripheral state is in kPeripheralBandState_ValidatingKey");
            if ( _deviceInfoService.serialNumber != null ) {
                Logger.log("_validateKey - _deviceInfoService.serialNumber != null");
                if ( _bandData.authKey == null ||
                        !_bandData.authKey.equalsIgnoreCase(_deviceInfoService.serialNumber) ) {
                    Logger.log("_validateKey - _bandData.authKey == null || !_bandData.authKey.equalsIgnoreCase(_deviceInfoService.serialNumber)");
                    //[[NSNotificationCenter defaultCenter] postNotificationName:kPeripheralBandRequestingAuthenticationNotification object:self userInfo:[NSDictionary dictionaryWithObjectsAndKeys:self, kPeripheralBandKey, nil]];
                    EventBus.getDefault().post(new SEvent(kPeripheralBandRequestingAuthenticationNotification, this));
                }
                else {
                    Logger.log("_validateKey - calling _startFirmwareValidation");
                    _startFirmwareValidation();
                }

            }
            else if ( _peripheral.connectionState() == BlePeripheral.STATE_CONNECTED ) {
                Logger.log("_validateKey - _peripheral(%s) is connected, _validateKey() after 1 second", _peripheral.address());
                //[NSTimer scheduledTimerWithTimeInterval:1.0 target:self selector:@selector(_validateKey) userInfo:nil repeats:NO];
                Timer timer = new Timer();
                OSDate dt = new OSDate().offsetSecond(1);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        _validateKey();
                    }
                }, dt);
            }
            else {
                Logger.log("_validateKey - no case");
            }
        }
        else {
            Logger.log("_validateKey - error - _perphState is not kPeripheralBandState_ValidatingKey");
        }
    }

    protected void _startFirmwareValidation() {
        Logger.log("_startFirmwareValidation : (%s)", address());
        if (_perphState == PeripheralBandState.kPeripheralBandState_ValidatingKey ) {
            _perphState = PeripheralBandState.kPeripheralBandState_ValidatingFirmware;
            validationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    _validateFirmware();
                }
            }, TimeUnit.SECONDS.toMillis(1));
        }
        else {
            Logger.log("_startFirmwareValidation : (%s), error ! condition is not true for _validateFirmware", address());
        }
    }

    protected void _validateFirmware() {
        /*
        if (
        #if !UNPAIR_BANDSTATE
        self.bandState == kWahoooBandState_Connecting &&
        #endif
        _perphState == kPeripheralBandState_ValidatingFirmware )
        {

            if ( self.fwVersion != nil && self.fwVersion.length > 0 )
            {
                _perphState = kPeripheralBandState_ValidatingFirmware;
                NSDictionary* userInfo = [NSDictionary dictionaryWithObjectsAndKeys:self, kPeripheralBandKey, nil];
                NSString* extraInfoDelim=@"-";
                NSString* delimiter = @".";
                NSArray* currentFw = [kPeripheralCurrentFirmwareVersion componentsSeparatedByString:extraInfoDelim];
                NSArray* deviceFw = [self.fwVersion componentsSeparatedByString:extraInfoDelim];

                currentFw = [[currentFw firstObject] componentsSeparatedByString:delimiter];
                deviceFw = [[deviceFw firstObject] componentsSeparatedByString:delimiter];

                int currentIndex = 0;
                int deviceIndex = 0;

                while( currentIndex < currentFw.count && deviceIndex < deviceFw.count )
                {
                    NSInteger currentInt = [[currentFw objectAtIndex:currentIndex] integerValue];
                    NSInteger deviceInt = [[deviceFw objectAtIndex:deviceIndex] integerValue];

                    if ( currentInt < deviceInt )
                    {
                        #if REQUIRE_APP_UPDATE
                            // Update App
                            [[NSNotificationCenter defaultCenter] postNotificationName:kPeripheralBandAppUpdateRequiredNotification object:self userInfo:userInfo];
                        return;
                        #else
                        break;
                        #endif
                    }
                    else if ( currentInt > deviceInt )
                    {
                        // Update firmware
                        [[NSNotificationCenter defaultCenter] postNotificationName:kPeripheralBandFirmwareUpdateRequiredNotification object:self userInfo:userInfo];
                        return;
                    }


                    currentIndex++;
                    deviceIndex++;
                }

                // If we got this far, make sure the versions have the same number of entries



                if ( currentFw.count > deviceFw.count )
                {
                    while( currentIndex < currentFw.count )
                    {
                        NSInteger currentInt = [[currentFw objectAtIndex:currentIndex] integerValue];
                        //If the remaining digits are all 0, ignore
                        if ( currentInt > 0 )
                        {
                            // Update Firmware
                            [[NSNotificationCenter defaultCenter] postNotificationName:kPeripheralBandFirmwareUpdateRequiredNotification object:self userInfo:userInfo];

                            return;
                        }

                        currentIndex++;
                    }
                }
                else if ( currentFw.count < deviceFw.count )
                {
                    #if REQUIRE_APP_UPDATE
                    // Update App

                    while( deviceIndex < deviceFw.count )
                    {
                        NSInteger deviceInt = [[deviceFw objectAtIndex:deviceIndex] integerValue];
                        //If the remaining digits are all 0, ignore
                        if ( deviceInt > 0 )
                        {
                            [[NSNotificationCenter defaultCenter] postNotificationName:kPeripheralBandAppUpdateRequiredNotification object:self userInfo:userInfo];
                            return;
                        }

                        deviceIndex++;
                    }
                    #endif


                }

                // If we got this far, firmware passed test
                {
                    // Firmware matches
                    [self setBandState:kWahoooBandState_Connected];

                    _perphState = kPeripheralBandState_Ready;

                    //[self _checkFirstTimeSetup];
                    [self performSelector:@selector(_checkFirstTimeSetup) withObject:nil afterDelay:0.5];
                }

            }
        }
        */

        // If we got this far, firmware passed test
        {
            // Firmware matches
            Logger.log("peripheralband._validateFirmware (%s) : state => kWahoooBandState_Connected", address());
            setBandState(WahoooBandState_t.kWahoooBandState_Connected);

            Logger.log("peripheralband._validateFirmware (%s) : peripheral state => kPeripheralBandState_Ready", address());
            _perphState = PeripheralBandState.kPeripheralBandState_Ready;

            //[self _checkFirstTimeSetup];
            //[self performSelector:@selector(_checkFirstTimeSetup) withObject:nil afterDelay:0.5];
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    _checkFirstTimeSetup();
                }
            }, 500);
        }
    }

    protected boolean _longTimeSinceDisconnect() {
        if( _bandData.disconnectTime > 0 ) {
            int disconnectTime = _bandData.disconnectTime.intValue();
            int now = (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

            if( (now - disconnectTime) > LONG_DISCONNECT_TIME  ) {
                return true;
            }
        }

        return false;
    }

    protected void _checkFirstTimeSetup() {
        Logger.log("_checkFirstTimeSetup (%s)", address());
        if( bandState() == WahoooBandState_t.kWahoooBandState_Connected ) {
            //NSDictionary* userInfo = [NSDictionary dictionaryWithObjectsAndKeys:self, kPeripheralBandKey, nil];

            if(_bandData.firstTime) {
                //[[NSNotificationCenter defaultCenter] postNotificationName:kPeripheralBandFirstTimeSetupNotification object:self userInfo:userInfo];
                EventBus.getDefault().post(new SEvent(kPeripheralBandFirstTimeSetupNotification, this));

                _bandData.firstTime = false;

                //[[WahoooBandManager sharedManager] saveBandData];
                CoreDataManager.sharedInstance().saveSwimbandData(_bandData);
            }
            else if ( _longTimeSinceDisconnect() ) {
                //[[NSNotificationCenter defaultCenter] postNotificationName:kPeripheralBandConfirmSettingsNotification object:self userInfo:userInfo];
                EventBus.getDefault().post(new SEvent(kPeripheralBandConfirmSettingsNotification, this));
            }
        } else
            Logger.log("_checkFirstTimeSetup (%s), error, bandstate != _connected", address());
    }

    public void setAlertTime(int alertTime) {
        super.setAlertTime(alertTime);
        if ( _bandData != null ) {
            _bandData.alarmTime = alertTime;
        }
    }

    public void setAlertType(WahoooAlertType alertType) {
        super.setAlertType(alertType);
        if ( _bandData != null ) {
            _bandData.alarmType = _alertType.getValue();
        }
    }

    public void setWarningTime(int warningTime) {
        super.setWarningTime(warningTime);
        if ( _bandData != null ) {
            _bandData.warningTime = warningTime;
        }
    }

    protected void _rssiTimer(/* NSTimer *timer */) {
        if ( _peripheral != null ) {
            _peripheral.updateRSSI();
        }
    }

    protected boolean _nameIsSubStringOfDefault(String name) {
        return defaultName().contains(name) && name.length() > 10;
        //NSRange subRange = [self.defaultName rangeOfString:name];

        //return (subRange.location != NSNotFound && subRange.length > 10 );
    }

    /*
    #pragma mark - BatteryServiceDelegate

    -(void) batteryService:(BatteryService *)service batteryLevelUpdated:(NSInteger)level
    {
        if ( service == _batteryService )
        {
            [_peripheral updateRSSI:@selector(_rssiUpdated:) target:self];

            //NSLog(@"Battery Level Update");
            self.batteryLevel = level;
        }
    }

    #pragma end

    #pragma mark - ProfileObjectDelegate

    -(void) profileObject:(ProfileObject*)profileObject discoveredService:(ServiceProxy*)service
    {
        //NSLog(@"I found a service!  %@", service.UUID);

        [self _retrieveCharacteristics:service];
    }

    -(void) profileObject:(ProfileObject *)profileObject discoveredCharacteristic:(CharacteristicProxy*)characteristic
    {
        [self _readCharacteristic:characteristic];
        //[self performSelector:@selector(_readCharacteristic:) withObject:characteristic afterDelay:2];
    }

    #pragma end

    #pragma mark - iDevicesServiceDelegate

    -(NSData*) getAppChallengeDataForiDevicesService:(iDevicesService*)service
    {
        memset( _appChallenge, 0, sizeof(_appChallenge));

        SecRandomCopyBytes(kSecRandomDefault, sizeof(_appChallenge) >> 1, _appChallenge);


        return [NSData dataWithBytes:_appChallenge length:sizeof(_appChallenge)];
    }

    -(NSData*) iDevicesService:(iDevicesService*)service deviceChallengeReceived:(NSData*)challenge
    {
        uint8_t key[kAuthSize] = {0x9e, 0x08, 0xca, 0x8f, 0x03, 0x34, 0x41, 0xab, 0x88, 0x88, 0x7e, 0x93, 0xe5, 0x2e, 0x19, 0xcb};
        uint8_t responseBuffer[kAuthSize];

        NSData* keyData = [NSData dataWithBytes:key length:sizeof(key)];

        NSData* decrypted = [challenge AES128DecryptWithKey:keyData];

        if ( memcmp(_appChallenge, decrypted.bytes, sizeof(_appChallenge) >> 1) != 0)
        {
            return nil;
        }

        memcpy(responseBuffer, decrypted.bytes, sizeof(responseBuffer));

        memset(responseBuffer, 0, sizeof(responseBuffer) >> 1);

        return [[NSData dataWithBytes:responseBuffer length:sizeof(responseBuffer)] AES128EncryptWithKey:keyData];
    }

    -(void) authenticationCompleteForiDevicesService:(iDevicesService*)service
    {
        if ( _perphState == kPeripheralBandState_Authenticating )
        {


            [self _startKeyValidation];
        }
    }

    -(void) firmwareUpgradeCompleteForiDevicesService:(iDevicesService*)service
    {

    }

    -(void) firmwareUpgradeProgressUpdate:(NSInteger)progress
    {
        _updateProgress = progress;
        [[NSNotificationCenter defaultCenter] postNotificationName:kWahoooBandDataUpdatedNotification object:self];

    }

    -(void) iDevicesService:(iDevicesService*)service error:(NSError*)error
    {
        NSLog(@"PeripheralBand iDevices Servie Error: %@", error);
    }



    #pragma end

    */
}
