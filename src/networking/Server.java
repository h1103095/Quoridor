package networking;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;

/*
 * ��Ʈ��ũ ������ �����ϴ� ȣ��Ʈ
 */

public class Server extends NetWorkSocket{
	private ServerSocket serverSocket;	// ���� ����
	private Socket socket;				// ����� ���� ����
	private byte[] bytes = new byte[20];				// �����͸� �ޱ� ���� ����
	private String receiveMessage;		// ���� ������
	private String sendMessage;			// ���� ������
	private OutputStream os;			// �����͸� ������ ���� ��Ʈ��
	private InputStream is;				// �����͸� �ޱ� ���� ��Ʈ��
	private InetAddress ia;				// �ڽ��� �ּҸ� ��� ���� ����
	private Frame LoadingFrame;			// â ���Ḧ ���� ��ü

	// ȣ��Ʈ�� ip�ּҸ� ��ȯ
	public String GetHostIP() {
		String ipNum;
		try {
			ipNum = InetAddress.getLocalHost().getHostAddress();
		} catch (IOException e) {
			e.printStackTrace();
			return "������ ���� ȣ��Ʈ�� �ּҸ� ����� �� �����ϴ�.";
		}
		return ipNum;
	}
	
	// ���� ����
	public boolean OpenServer() {
		try {
			ia = InetAddress.getLocalHost();
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(ia, 8888)); // ���� ����
			bytes = new byte[20];

			System.out.println("���� ��ٸ��� ��...");
			System.out.println(ia);
			socket = serverSocket.accept();
			InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
			System.out.println("����  ����!" + isa.getHostName() + "/" + isa.getAddress());

			// ������ �ޱ�
			is = socket.getInputStream();
			
			int readByteCount = is.read(bytes);		// Ŭ���̾�Ʈ�κ��� Ȯ�� ������ ����
			receiveMessage = new String(bytes, 0, readByteCount, "UTF-8");
			System.out.println("������ �ޱ� ����: " + receiveMessage);

			// ������ ������
			os = socket.getOutputStream();
			sendMessage = new String("Message From Host");
			bytes = sendMessage.getBytes("UTF-8");

			os.write(bytes, 0, bytes.length);		// Ŭ���̾�Ʈ�� Ȯ�� ������ ����
			os.flush();
			System.out.println("������ ������ ����" + bytes);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return false;
	}
	
	// ������ ������
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
	
	// ������ �ޱ�
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
	
	// ���� �ݱ�
	public void CloseSocket() {
		try {
			if (!serverSocket.isClosed()) {
				serverSocket.close();
				socket.close();
				is.close();
				os.close();
				System.out.println("������ �ݽ��ϴ�.");
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

