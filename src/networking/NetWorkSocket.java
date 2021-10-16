package networking;

import javax.swing.*;

/* 
 * ȣ��Ʈ�� Ŭ���̾�Ʈ�� ���� Ŭ����
 */
public abstract class NetWorkSocket {
	public abstract void SendData(String msg);		// ������ ������
	public abstract String ReceiveData();			// ������ �ޱ�
	public abstract void CloseSocket();				// ���� �ݱ�
	public abstract boolean OpenServer();			// ���� ����
	public abstract boolean JoinServer(String HostIP, JTextArea connectLogTA, JButton closeBT);	// ������ ����
	public abstract String GetHostIP();				// ȣ��Ʈ IP ���
}
