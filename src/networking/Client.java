package networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Scanner;
import javax.swing.*;

/*
 * 네트워크 게임에 참여하는 게스트
 */

public class Client extends NetWorkSocket{
	private Socket socket;			// 통신을 위한 소켓
	private byte[] bytes = new byte[20];			// 데이터를 받기 위한 변수
	private String receiveMessage;	// 받은 데이터
	private String sendMessage;		// 보낼 데이터
	private OutputStream os;		// 데이터를 보내기 위한 스트림
	private InputStream is;			// 데이터를 받기 위한 스트림
	private InetAddress hostIP;		// 호스트 IP


	public Client() {
		socket = new Socket();
	}
	
	public boolean JoinServer(String HostIP, JTextArea connectLogTA, JButton closeBT) {
		JTextArea connectLog = connectLogTA;
		try {
			Scanner sc = new Scanner(System.in);
			hostIP = InetAddress.getByName(HostIP);
			bytes = new byte[20];
			
			connectLog.append(HostIP + "에 연결 요청...\n");
			socket.connect(new InetSocketAddress(hostIP, 8888)); // 서버 연결
			connectLog.append("연결 성공!\n");

			// 데이터 보내기
			os = socket.getOutputStream();
			sendMessage = "Message From Client";
			bytes = sendMessage.getBytes("UTF-8");

			os.write(bytes, 0, bytes.length);
			os.flush();
			System.out.println("데이터 보내기 성공");
			connectLog.append("데이터 보내기 성공\n");

			// 데이터 받기
			is = socket.getInputStream();

			int readByteCount = is.read(bytes);
			receiveMessage = new String(bytes, 0 , readByteCount, "UTF-8");
			System.out.println("데이터 받기 성공: " + receiveMessage);
			connectLog.append("데이터 받기 성공\n");
			sc.close();
			
			return true;
			
		} catch (UnknownHostException e) {
			System.out.println("연결 실패. ip주소를 확인해 주세요.");
			connectLog.append("연결 실패. ip주소를 확인해 주세요.\n");
			closeBT.setEnabled(true);
			CloseSocket();
			return false;
		} catch (ConnectException e) {
			System.out.println("연결 실패. ip주소를 확인해 주세요.");
			connectLog.append("연결 실패. ip주소를 확인해 주세요.\n");
			closeBT.setEnabled(true);
			CloseSocket();
			return false;
		} catch (SocketException e) {
			System.out.println("소켓 오류 발생");
			connectLog.append("소켓 오류 발생\n");
			closeBT.setEnabled(true);
			CloseSocket();
		} catch (IOException e) {
			System.out.println("데이터 입출력 문제 발생");
			connectLog.append("데이터 입출력 문제 발생\n");
			closeBT.setEnabled(true);
			e.printStackTrace();
			CloseSocket();
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
			if (!socket.isClosed()) {
				socket.close();
				is.close();
				os.close();
				System.out.println("네트워크 연결을 끊습니다...");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			
		}
	}
	
	public String toString() {
		return "Client";
	}

	@Override
	public boolean OpenServer() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String GetHostIP() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}


