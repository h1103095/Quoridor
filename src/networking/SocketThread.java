package networking;

import java.awt.*;
import javax.swing.*;

import object.GameManager;
import enums.GAME_MODE;
import scene.Game;

/*
 * ������ Ŭ���̾�Ʈ�� ��� ������
 */

public class SocketThread extends Thread {
	private NetWorkSocket socket;	// ���� �Ǵ� Ŭ���̾�Ʈ
	private Frame frame;			// â ���Ḧ ���� ��ü
	private String hostIP;			// ȣ��Ʈ IP
	private JTextArea connectLog;	// ��� ���
	private JButton closeBT;		// ��� ��ư
	public SocketThread(NetWorkSocket socket, Frame frame) {
		this.socket = socket;
		this.frame = frame;
	}
	public SocketThread(NetWorkSocket socket, Frame frame, String HostIP, JTextArea connectLogTA, JButton closeBT) {
		this.socket = socket;
		this.frame = frame;
		this.hostIP = HostIP;
		this.connectLog = connectLogTA;
		this.closeBT = closeBT;
	}
	public void run() {
		// ȣ��Ʈ�� ��
		if(socket.toString() == "Host" && socket.OpenServer())
		{
			GameManager GM = new GameManager(GAME_MODE.NETWORKHOST, getSocket());
			new Game(GM);		// ��Ʈ��ũ ȣ��Ʈ�� ���� ����
			frame.dispose();	// ���� â �ݱ�
		}
		// Ŭ���̾�Ʈ�� ��
		else if(socket.toString() == "Client" && socket.JoinServer(hostIP, connectLog, closeBT))
		{
			GameManager GM = new GameManager(GAME_MODE.NETWORKGUEST, getSocket());
			new Game(GM);		// ��Ʈ��ũ �Խ�Ʈ�� ���� ����
			frame.dispose();	// ���� â �ݱ�
		}
	}
	// ���� ��ȯ
	public NetWorkSocket getSocket() { return socket; }
}