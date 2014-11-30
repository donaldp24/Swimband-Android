package com.aquaticsafetyconceptsllc.iswimband.band;

import android.content.Context;
import android.os.Handler;
import com.aquaticsafetyconceptsllc.iswimband.Ble.BleManager;
import com.aquaticsafetyconceptsllc.iswimband.Ble.BlePeripheral;
import com.aquaticsafetyconceptsllc.iswimband.Event.SEvent;
import com.aquaticsafetyconceptsllc.iswimband.Utils.Logger;
import de.greenrobot.event.EventBus;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class WahoooBandManager {

	public static final int ADVERTISE_TIME_OUT = 20;
	public static final String UUID_BANDSERVICE = "EBEA0000-473C-48F7-AEBA-3C9CB39C1A31";

	public static final String kBandManagerAdvertisingBandsChangedNotification = "kBandManagerAdvertisingBandsChangedNotification";
	public static final String kBandManagerConnectedBandsChangedNotification = "kBandManagerConnectedBandsChangedNotification";
	public static final String kCBCentralRestorationId = "b07f160c-cff9-454d-b54f-c0a464075823";

	public static final String kWahoooBandDataUpdatedNotification = "kWahoooBandDataUpdatedNotification";

	public static final String kWahoooBandAlertNotification = "kWahoooBandAlertNotification";
	public static final String kWahoooBandKey = "kWahoooBandKey";

	protected static WahoooBandManager _instance = null;
	private BleManager _bleManager;
	protected Context mContext;

	public ArrayList<WahoooBand> connectedBands;
	public ArrayList<WahoooBand> advertisingBands;
	public ArrayList<WahoooBand> remoteBands;
	public ArrayList<WahoooBand> connectedBandsDisplay;
	public ArrayList<WahoooBand> connectingBands;

	static final int BANDCONNECTEDSOUND_INTERVAL = 600;
	protected Handler timerBandConnectedSoundHandler = null;
	protected Runnable timerBandConnectedSoundRunnable = new Runnable() {
		@Override
		public void run() {
			playBandConnectedSoundNotification();

			timerBandConnectedSoundHandler.postDelayed(this, TimeUnit.SECONDS.toMillis(BANDCONNECTEDSOUND_INTERVAL));
		}
	};

	protected Handler purgeHandler = null;
	protected Runnable purgeRunnable = new Runnable() {
		@Override
		public void run() {
			_bleManager.purgeAdvertisingDevices(ADVERTISE_TIME_OUT);

			purgeHandler.postDelayed(this, TimeUnit.SECONDS.toMillis(1));
		}
	};

	public static WahoooBandManager initialize(Context context) {
		if (_instance == null)
			_instance = new WahoooBandManager(context);
		return _instance;
	}

	public static WahoooBandManager sharedManager() {
		return _instance;
	}

	private WahoooBandManager(Context context) {
		connectedBands = new ArrayList<WahoooBand>();
		advertisingBands = new ArrayList<WahoooBand>();
		remoteBands = new ArrayList<WahoooBand>();
		connectedBands = new ArrayList<WahoooBand>();
		connectedBandsDisplay = new ArrayList<WahoooBand>();
		connectingBands = new ArrayList<WahoooBand>();

		mContext = context;
		_bleManager = BleManager.initialize(mContext);

		EventBus.getDefault().register(this);

		purgeHandler = new Handler();
		purgeHandler.postDelayed(purgeRunnable, TimeUnit.SECONDS.toMillis(1));
	}


	public void startScan() {
		Logger.log("WahoooBandManager - startScan() ");

		ArrayList<UUID> uuidArray = new ArrayList<UUID>();
		uuidArray.add(UUID.fromString(UUID_BANDSERVICE));

		advertisingBands.clear();

		// start scan
		_bleManager.scanForPeripheralsWithServices(uuidArray, true);

		EventBus.getDefault().post(new SEvent(kBandManagerAdvertisingBandsChangedNotification, this));
	}

	public void stopScan() {
		Logger.log("WahoooBandManager - stopScan() ");
		// stop scan
		_bleManager.stopScan();
	}

	public void connect(WahoooBand band)  {
		if ( band.type() != WahoooBand.WahoooBandType.kWahoooBand_Peripheral ) {
			Logger.log("WahoooBandManager - connect : band is not peripheral band");
			return;
		}

		PeripheralBand prphBand = (PeripheralBand )band;
		Logger.log("WahoooBandManager - connect(%s)", prphBand.address());
		boolean ret = _bleManager.connectPeripheral(prphBand.peripheral());
		if (!ret) {
			Logger.log("WahoooBandManager - connectPeripheral(%s) - returned false", prphBand.address());
			return;
		}

		if (!connectingBands.contains(band)) {
			Logger.log("WahoooBandManager - connectingBands.add(%s)", prphBand.address());
			connectingBands.add(band);
		}

		Logger.log("WahoooBandManager - advertisingBands.remove(%s)", prphBand.address());
		advertisingBands.remove(band);

		//[[NSNotificationCenter defaultCenter] postNotificationName:kBandManagerAdvertisingBandsChangedNotification object:self];
		EventBus.getDefault().post(new SEvent(kBandManagerAdvertisingBandsChangedNotification, this));
	}

	public void disconnect(WahoooBand band) {
		Logger.log("WahoooBandManager - disconnect()");
		if (band.type() != WahoooBand.WahoooBandType.kWahoooBand_Peripheral) {
			Logger.log("WahoooBandManager - disconnect : band is not peripheral band");
			return;
		}

		connectedBands.remove(band);

		PeripheralBand prphBand = (PeripheralBand) band;

		band.disconnect();

		_bleManager.disconnectPeripheral(prphBand.peripheral());

		buildConnectedBandsDisplay();

		//[[NSNotificationCenter defaultCenter] postNotificationName:kBandManagerConnectedBandsChangedNotification object:self];
		EventBus.getDefault().post(new SEvent(kBandManagerConnectedBandsChangedNotification, this));

		// Req#12: Delete the timer if there is no connected band remaining
		if (connectedBands == null || connectedBands.size() == 0) {
			timerBandConnectedSoundHandler.removeCallbacks(timerBandConnectedSoundRunnable);
			timerBandConnectedSoundHandler = null;
		}
	}

	public void buildConnectedBandsDisplay() {
		ArrayList<WahoooBand> combined = new ArrayList<WahoooBand>();
		combined.addAll(connectedBands);
		combined.addAll(remoteBands);

		Collections.sort(combined, new Comparator<WahoooBand>() {
			@Override
			public int compare(WahoooBand lhs, WahoooBand rhs) {
				int nameResult = lhs.displayName().compareToIgnoreCase(rhs.displayName());
				int stateValue1 = WahoooBand.sortValueForBandState(lhs.bandState());
				int stateValue2 = WahoooBand.sortValueForBandState(rhs.bandState());
				if (stateValue1 < stateValue2)
					return -1;
				if (stateValue1 > stateValue2)
					return 1;
				return nameResult;
			}
		});

		connectedBandsDisplay = combined;
		//Logger.log("WahoooBandManager - connectedBandsDisplay.size() : %d", connectedBandsDisplay.size());
	}

	public void addRemoteBand(WahoooBand band) {
		if (!remoteBands.contains(band)) {
			remoteBands.add(band);

			buildConnectedBandsDisplay();
			//[[NSNotificationCenter defaultCenter] postNotificationName:kBandManagerConnectedBandsChangedNotification object:self];

			EventBus.getDefault().post(new SEvent(kBandManagerConnectedBandsChangedNotification, this));
		}
	}

	public void removeRemoteBand(WahoooBand band) {
		if (remoteBands.contains(band))  {
			remoteBands.remove(band);

			buildConnectedBandsDisplay();
			//[[NSNotificationCenter defaultCenter] postNotificationName:kBandManagerConnectedBandsChangedNotification object:self];

			EventBus.getDefault().post(new SEvent(kBandManagerConnectedBandsChangedNotification, this));
		}
	}

	public void clearRemoteBands() {
		remoteBands.clear();

		//[[NSNotificationCenter defaultCenter] postNotificationName:kBandManagerConnectedBandsChangedNotification object:self];
		EventBus.getDefault().post(new SEvent(kBandManagerConnectedBandsChangedNotification, this));
	}


	public void onEventMainThread(SEvent e) {
		if (BleManager.kBLEManagerDiscoveredPeripheralNotification.equalsIgnoreCase(e.name)) {
			_discoveredPeripheral((BlePeripheral)e.object);
		} else if (BleManager.kBLEManagerUndiscoveredPeripheralNotification.equalsIgnoreCase(e.name)) {
			_undiscoveredPeripheral((BlePeripheral)e.object);
		} else if (BleManager.kBLEManagerConnectedPeripheralNotification.equalsIgnoreCase(e.name)) {
			BlePeripheral peripheral = (BlePeripheral)e.object;
			_connectedPeripheral(peripheral);
		} else if (BleManager.kBLEManagerDisconnectedPeripheralNotification.equalsIgnoreCase(e.name)) {
			BlePeripheral peripheral = (BlePeripheral)e.object;
			_disconnectedPeripheral(peripheral);
		} else if (BleManager.kBLEManagerStateChanged.equalsIgnoreCase(e.name)) {
			Integer state = (Integer)e.object;
			_bleManagerStateChanged(state.intValue());
		} else if (kWahoooBandDataUpdatedNotification.equalsIgnoreCase(e.name)) {
			WahoooBand band = (WahoooBand)e.object;
			_bandDataUpdated(band);
		}
	}

	protected void _discoveredPeripheral(BlePeripheral peripheral)  {
		Logger.log("WahoooBandManager - _discoveredPeripheral : " + peripheral.name() + " : " + peripheral.address());

		PeripheralBand connectedBand = _findConnectedBandForPeripheral(peripheral);

		// If the band is in the connected list, reestablish connection
		if (connectedBand != null) {
			// Logger.log("peripheral(%s) is in connectedBands - calling connect() function", connectedBand.address());
			// connect(connectedBand);
			Logger.log("peripheral(%s) is in connectedBands", connectedBand.address());
			if (peripheral.connectionState() == BlePeripheral.STATE_DISCONNECTED) {
				Logger.log("peripheral(%s) is in connectedBands, but disconnected - try to connect()", connectedBand.address());
				connect(connectedBand);
			}
			return;
		}

		for (WahoooBand band : connectingBands) {
			PeripheralBand peripheralBand = (PeripheralBand) band;
			if (peripheralBand.peripheral().address().equalsIgnoreCase(peripheral.address())) {
				//Logger.log("band(%s) already exist in connectingBands - calling connect() function", peripheral.address());
				//connect(peripheralBand);
				Logger.log("band(%s) already exist in connectingBands", peripheral.address());
				if (peripheral.connectionState() == BlePeripheral.STATE_DISCONNECTED) {
					Logger.log("peripheral(%s) is in connectingBands, but disconnected - try to connect()", connectedBand.address());
					connect(connectedBand);
				}
				return;
			}
		}

		for (WahoooBand band : advertisingBands) {
			PeripheralBand peripheralBand = (PeripheralBand) band;
			if (peripheralBand.peripheral().address().equalsIgnoreCase(peripheral.address())) {
				Logger.log("WahoooBandManager - _discoveredPeripheral : band(%s) already exist in advertisingBands", peripheral.address());
				return;
			}
		}

		String name = peripheral.name();

		//if ( name && [name hasPrefix:@"Wahooo"])
		{

			PeripheralBand newBand = new PeripheralBand();
			newBand.setPeripheral(peripheral);

			advertisingBands.add(newBand);

			//[[NSNotificationCenter defaultCenter] postNotificationName:kBandManagerAdvertisingBandsChangedNotification object:self];
			EventBus.getDefault().post(new SEvent(kBandManagerAdvertisingBandsChangedNotification, this));
		}
	}

	protected void _undiscoveredPeripheral(BlePeripheral peripheral) {

		ArrayList<WahoooBand> removingList = new ArrayList<WahoooBand>();
		for (WahoooBand band : advertisingBands) {
			PeripheralBand peripheralBand = (PeripheralBand) band;
			if (peripheralBand.peripheral().address().equalsIgnoreCase(peripheral.address())) {
				removingList.add(band);
			}
		}

		for (WahoooBand band : removingList) {
			advertisingBands.remove(band);
			//[[NSNotificationCenter defaultCenter] postNotificationName:kBandManagerAdvertisingBandsChangedNotification object:self];
			EventBus.getDefault().post(new SEvent(kBandManagerAdvertisingBandsChangedNotification, this));
		}
	}

	protected void _connectedPeripheral(BlePeripheral peripheral)  {

		Logger.log("_connectedPeripheral (%s) (%s)", peripheral.address(), peripheral.name());

		PeripheralBand connectedBand = null;

		for( WahoooBand band : connectingBands )
		{
			PeripheralBand peripheralBand = (PeripheralBand)band;
			if( peripheralBand.peripheral().address().equalsIgnoreCase(peripheral.address()) )
			{
				connectedBand = peripheralBand;
				break;
			}
		}

		if ( connectedBand != null )
		{
			Logger.log("_connectedPeripheral (%s) (%s) is in connectingBands - remove it from connectingBands", peripheral.address(), peripheral.name());
			connectingBands.remove(connectedBand);
		}
		else
		{
			Logger.log("_connectedPeripheral (%s) (%s) is not in connectingBands", peripheral.address(), peripheral.name());
			for( WahoooBand band : connectedBands )
			{
				PeripheralBand peripheralBand = (PeripheralBand)band;
				if( peripheralBand.peripheral().address().equalsIgnoreCase(peripheral.address()) )
				{
					Logger.log("_connectedPeripheral (%s) (%s) is in connectedBands - return without any processing", peripheral.address(), peripheral.name());
					return;
				}
			}

			Logger.log("_connectedPeripheral (%s) (%s) - create new PeripheralBand (not in connectedBands & connectingBands)", peripheral.address(), peripheral.name());
			connectedBand = new PeripheralBand();
			connectedBand.setPeripheral(peripheral);
		}

		if ( !connectedBands.contains(connectedBand) )
		{
			// Req#12: Create timer to play sound every 10 mins until even one band is connected
			// Timer created only once when first band is connected
			if(connectedBands.size() == 0 && timerBandConnectedSoundHandler == null) {
				timerBandConnectedSoundHandler = new Handler();
				timerBandConnectedSoundHandler.postDelayed(timerBandConnectedSoundRunnable, TimeUnit.SECONDS.toMillis(BANDCONNECTEDSOUND_INTERVAL));
			}
			Logger.log("_connectedPeripheral (%s) (%s) - add connectedBand to connectedBands", peripheral.address(), peripheral.name());
			connectedBands.add(connectedBand);
		}

		buildConnectedBandsDisplay();

		//[[NSNotificationCenter defaultCenter] postNotificationName:kBandManagerConnectedBandsChangedNotification object:self];
		EventBus.getDefault().post(new SEvent(kBandManagerConnectedBandsChangedNotification, this));

	}

	protected void _disconnectedPeripheral(BlePeripheral peripheral) {

		Logger.log("_disconnectedPeripheral (%s) (%s)", peripheral.address(), peripheral.name());
		if ( peripheral != null )
		{
			PeripheralBand connectingBand = null;
			for( WahoooBand band : connectingBands )
			{
				PeripheralBand peripheralBand = (PeripheralBand)band;
				if ( peripheralBand.peripheral().address().equalsIgnoreCase(peripheral.address()) )
				{
					Logger.log("_disconnectedPeripheral (%s) (%s) : is in connectingBands", peripheral.address(), peripheral.name());
					connectingBand = peripheralBand;
					break;
				}
			}

			if ( connectingBand != null )
			{
				connectingBands.remove(connectingBand);
			}

			PeripheralBand connectedBand = _findConnectedBandForPeripheral(peripheral);

			// We're going to want to reconnect asap if in connected list
			if ( connectedBand != null )
			{
				Logger.log("_disconnectedPeripheral (%s) (%s) : is in connectedBands - try to connect again", peripheral.address(), peripheral.name());
				_bleManager.connectPeripheral(connectedBand.peripheral());
			}

			//[self _retrieveConnectedBands];
		}
	}


	protected PeripheralBand _findConnectedBandForPeripheral(BlePeripheral peripheral)  {
		for(WahoooBand band : connectedBands) {
			PeripheralBand peripheralBand = (PeripheralBand)band;
			if( peripheralBand.peripheral().address().equalsIgnoreCase(peripheral.address())) {
				return peripheralBand;
			}
		}

		return null;
	}

	protected void _bleManagerStateChanged(int state)  {
		if (_bleManager.isBleAvailable()) {
			startScan();
		} else {
			stopScan();
		}
	}


	protected void _bandDataUpdated(WahoooBand band) {
		if (connectedBands.contains(band) ||
		remoteBands.contains(band)) {
			buildConnectedBandsDisplay();

			//[[NSNotificationCenter defaultCenter] postNotificationName:kBandManagerConnectedBandsChangedNotification object:self];
			EventBus.getDefault().post(new SEvent(kBandManagerConnectedBandsChangedNotification, this));
		}
	}


	public void playBandConnectedSoundNotification() {
		/*
		SystemSoundID audioEffect;
		NSString *path = [[NSBundle mainBundle] pathForResource:@"SONAR1" ofType:@"WAV"];

		if ([[NSFileManager defaultManager] fileExistsAtPath:path]) {
		NSURL *pathURL = [NSURL fileURLWithPath:path];
		AudioServicesCreateSystemSoundID((__bridge CFURLRef) pathURL, &audioEffect);
		AudioServicesPlaySystemSound(audioEffect);
		}
			else {
			NSLog(@"ERROR: Sound file not found: %@", path);
		}
		*/
	}

}
