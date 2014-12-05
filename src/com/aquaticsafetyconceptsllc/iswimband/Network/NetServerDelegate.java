package com.aquaticsafetyconceptsllc.iswimband.Network;

import java.net.Socket;

public interface NetServerDelegate {
	public void acceptSocket(NetServer netServer, Socket socket);
	public void netServerDidPublish(NetServer netServer);
	public void failedPublishWithError(NetServer netServer, int error);

}
