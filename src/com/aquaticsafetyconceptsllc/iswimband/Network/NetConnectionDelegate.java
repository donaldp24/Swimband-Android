package com.aquaticsafetyconceptsllc.iswimband.Network;

public interface NetConnectionDelegate {
	public void netConnectionReceivedData(NetConnection connection);
	public void netConnectionDisconnected(NetConnection connection);
}
