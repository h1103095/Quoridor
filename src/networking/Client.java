package networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Scanner;
import javax.swing.*;

/*
 * ��Ʈ��ũ ���ӿ� �����ϴ� �Խ�Ʈ
 */

public class Client extends NetWorkSocket{
	private Socket socket;			// ����� ���� ����
	private byte[] bytes = new byte[20];			// �����͸� �ޱ� ���� ����
	private String receiveMessage;	// ���� ������
	private String sendMessage;		// ���� ������
	private OutputStream os;		// �����͸� ������ ���� ��Ʈ��
	private InputStream is;			// �����͸� �ޱ� ���� ��Ʈ��
	private InetAddress hostIP;		// ȣ��Ʈ IP


	public Client() {
		socket = new Socket();
	}
	
	public boolean JoinServer(String HostIP, JTextArea connectLogTA, JButton closeBT) {
		JTextArea connectLog = connectLogTA;
		try {
			Scanner sc = new Scanner(System.in);
			hostIP = InetAddress.getByName(HostIP);
			bytes = new byte[20];
			
			connectLog.append(HostIP + "�� ���� ��û...\n");
			socket.connect(new InetSocketAddress(hostIP, 8888)); // ���� ����
			connectLog.append("���� ����!\n");

			// ������ ������
			os = socket.getOutputStream();
			sendMessage = "Message From Client";
			bytes = sendMessage.getBytes("UTF-8");

			os.write(bytes, 0, bytes.length);
			os.flush();
			System.out.println("������ ������ ����");
			connectLog.append("������ ������ ����\n");

			// ������ �ޱ�
			is = socket.getInputStream();

			int readByteCount = is.read(bytes);
			receiveMessage = new String(bytes, 0 , readByteCount, "UTF-8");
			System.out.println("������ �ޱ� ����: " + receiveMessage);
			connectLog.append("������ �ޱ� ����\n");
			sc.close();
			
			return true;
			
		} catch (UnknownHostException e) {
			System.out.println("���� ����. ip�ּҸ� Ȯ���� �ּ���.");
			connectLog.append("���� ����. ip�ּҸ� Ȯ���� �ּ���.\n");
			closeBT.setEnabled(true);
			CloseSocket();
			return false;
		} catch (ConnectException e) {
			System.out.println("���� ����. ip�ּҸ� Ȯ���� �ּ���.");
			connectLog.append("���� ����. ip�ּҸ� Ȯ���� �ּ���.\n");
			closeBT.setEnabled(true);
			CloseSocket();
			return false;
		} catch (SocketException e) {
			System.out.println("���� ���� �߻�");
			connectLog.append("���� ���� �߻�\n");
			closeBT.setEnabled(true);
			CloseSocket();
		} catch (IOException e) {
			System.out.println("������ ����� ���� �߻�");
			connectLog.append("������ ����� ���� �߻�\n");
			closeBT.setEnabled(true);
			e.printStackTrace();
			CloseSocket();
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
			if (!socket.isClosed()) {
				socket.close();
				is.close();
				os.close();
				System.out.println("��Ʈ��ũ ������ �����ϴ�...");
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


