package networking;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;

/*
 * 네트워크 게임을 생성하는 호스트
 */

public class Server extends NetWorkSocket{
	private ServerSocket serverSocket;	// 서버 소켓
	private Socket socket;				// 통신을 위한 소켓
	private byte[] bytes = new byte[20];				// 데이터를 받기 위한 변수
	private String receiveMessage;		// 받은 데이터
	private String sendMessage;			// 보낼 데이터
	private OutputStream os;			// 데이터를 보내기 위한 스트림
	private InputStream is;				// 데이터를 받기 위한 스트림
	private InetAddress ia;				// 자신의 주소를 얻기 위한 변수
	private Frame LoadingFrame;			// 창 종료를 위한 객체

	// 호스트의 ip주소를 반환
	public String GetHostIP() {
		String ipNum;
		try {
			ipNum = InetAddress.getLocalHost().getHostAddress();
		} catch (IOException e) {
			e.printStackTrace();
			return "오류로 인해 호스트의 주소를 출력할 수 없습니다.";
		}
		return ipNum;
	}
	
	// 서버 열기
	public boolean OpenServer() {
		try {
			ia = InetAddress.getLocalHost();
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(ia, 8888)); // 서버 오픈
			bytes = new byte[20];

			System.out.println("연결 기다리는 중...");
			System.out.println(ia);
			socket = serverSocket.accept();
			InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
			System.out.println("연결  수락!" + isa.getHostName() + "/" + isa.getAddress());

			// 데이터 받기
			is = socket.getInputStream();
			
			int readByteCount = is.read(bytes);		// 클라이언트로부터 확인 데이터 받음
			receiveMessage = new String(bytes, 0, readByteCount, "UTF-8");
			System.out.println("데이터 받기 성공: " + receiveMessage);

			// 데이터 보내기
			os = socket.getOutputStream();
			sendMessage = new String("Message From Host");
			bytes = sendMessage.getBytes("UTF-8");

			os.write(bytes, 0, bytes.length);		// 클라이언트로 확인 데이터 보냄
			os.flush();
			System.out.println("데이터 보내기 성공" + bytes);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return false;
	}
	
	// 데이터 보내기
	public synchronized void SendData(String msg) {
		try {
			System.out.println("Send: " + msg);
			os = socket.getOutputStream();
			bytes = msg.getBytes("UTF-8");
	
			os.write(bytes, 0, bytes.length);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 데이터 받기
	public synchronized String ReceiveData() {
		try {
			is = socket.getInputStream();
			int readByteCount = is.read(bytes);
			receiveMessage = new String(bytes, 0 , readByteCount, "UTF-8");
			System.out.println("Received: " + receiveMessage);
			return receiveMessage;
		} catch (IOException e) {
			e.printStackTrace();
			return "Failed NetWorking";
		}
	}
	
	// 소켓 닫기
	public void CloseSocket() {
		try {
			if (!serverSocket.isClosed()) {
				serverSocket.close();
				socket.close();
				is.close();
				os.close();
				System.out.println("서버를 닫습니다.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			
		}
	}
	
	public String toString() {
		return "Host";
	}

	@Override
	public boolean JoinServer(String HostIP, JTextArea connectLogTA, JButton closeBT) {
		// TODO Auto-generated method stub
		return false;
	}

}

