package com.quoridor.networking;

import java.net.*;

public class Client extends NetworkObject{
	public void joinServer(String hostIP, int port) throws Exception{
		socket = new Socket(hostIP, port);
		setStream(socket);
	}
}