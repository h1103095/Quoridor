package com.quoridor.networking;

import java.io.*;
import java.net.*;

import com.quoridor.enums.NETWORK_MSG_TYPE;
import com.quoridor.logger.MyLogger;

public class NetworkObject {
	protected ServerSocket serverSocket;	// 서버 소켓
	protected Socket socket;
	protected InetAddress inetAddress;
	protected OutputStream outputStream;			// 데이터를 보내기 위한 스트림
	protected InputStream inputStream;			// 데이터를 받기 위한 스트림
	protected ObjectOutputStream objectOutputStream;
	protected ObjectInputStream objectInputStream;
	protected DataOutputStream dataOutputStream;
	protected DataInputStream dataInputStream;
	protected int timeout = 30;

	public NetworkObject() {
		try {
			inetAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	// ip주소를 반환
	public String getIP() {
		return inetAddress.getHostAddress();
	}

	public void setStream(Socket socket) throws IOException {
		outputStream = socket.getOutputStream();
		inputStream = socket.getInputStream();
		objectOutputStream = new ObjectOutputStream(outputStream);
		objectInputStream = new ObjectInputStream(inputStream);
		dataOutputStream = new DataOutputStream(outputStream);
		dataInputStream = new DataInputStream(inputStream);
	}

	public synchronized void sendData(NETWORK_MSG_TYPE msgType, String msg) {
		try {
			String str = msgType.toString() + " " + msg + "/";
			outputStream.write(str.getBytes("UTF-8"));
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized String receiveData() {
		try {
			byte[] byteArr = new byte[100];
			int readByteCount = inputStream.read(byteArr);
			if(readByteCount == -1) { throw new IOException(); }
			String data = new String(byteArr, 0, readByteCount, "UTF-8");
			return data;
		} catch (IOException e) {
			MyLogger.getInstance().warning("네트워크 데이터를 받는 과정에서 에러 발생.");
			this.closeSocket();
		}
		return null;
	}

	// 소켓 닫기
	public void closeSocket() {
		try {
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			if (socket != null && !socket.isClosed()) {
				socket.close();
				objectInputStream.close();
				objectOutputStream.close();
				dataInputStream.close();
				dataOutputStream.close();
				inputStream.close();
				outputStream.close();
				MyLogger.getInstance().info("네트워크 연결을 끊습니다...");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


