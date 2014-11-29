package com.aquaticsafetyconceptsllc.iswimband.Network;

import java.net.Socket;

public interface NetServerDelegate {
	public void acceptSocket(NetServer netServer, Socket socket);
}
