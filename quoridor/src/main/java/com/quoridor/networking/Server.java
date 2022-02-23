package com.quoridor.networking;

import java.net.*;

import com.quoridor.enums.GAME_MODE;
import com.quoridor.game.manager.GameManager;
import com.quoridor.scene.ServerFrame;

public class Server extends NetworkObject{
	private NetworkObject server;
	private ServerFrame serverFrame;

	public Server() {
		server = this;
	}
	
	// 서버 열기
	public void OpenServer(int port, ServerFrame serverF) {
		this.serverFrame = serverF;
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					serverFrame.appendStringToTextArea("연결 기다리는 중...");
					socket = serverSocket.accept();
					setStream(socket);
					serverFrame.appendStringToTextArea("연결 수락!");
					new GameManager(GAME_MODE.NETWORK_HOST, server);
					serverFrame.dispose();
				} catch(Exception e) {
					e.printStackTrace();
					CloseSocket();
					serverFrame.setOpenServerButtonEnabled(true);
					serverFrame.appendStringToTextArea("에러 발생");
				}
			}
		};

		try {
			serverSocket = new ServerSocket(port);
			Thread openServerThread = new Thread(runnable);
			openServerThread.start();
		} catch(Exception e) {
			if(socket != null) {
				CloseSocket();
			}
			serverFrame.setOpenServerButtonEnabled(true);
			serverFrame.appendStringToTextArea("에러 발생");
			return;
		}
	}
}

