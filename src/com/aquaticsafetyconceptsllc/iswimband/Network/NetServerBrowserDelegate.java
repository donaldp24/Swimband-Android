package com.aquaticsafetyconceptsllc.iswimband.Network;

import android.net.nsd.NsdServiceInfo;

public interface NetServerBrowserDelegate {
	// Primarily use this to prevent the local host from browsing their own service
	public boolean shouldAllowService(NsdServiceInfo service);
	public void resolvedService(NsdServiceInfo service);
	public void lostService(NsdServiceInfo service);
}
