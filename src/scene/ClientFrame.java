package scene;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import networking.SocketThread;
import networking.Client;

/*
 * ��Ʈ��ũ ���� ���� �� ��Ÿ���� ������
 * Ŭ���̾�Ʈ Ŭ������ �����ϰ� ����
 */

// ���� ���� �� ����� â
public class ClientFrame extends JFrame{
	JTextField ipTF; 	// IP�ּҸ� �Է��ϴ� �ؽ�Ʈ �ʵ�
	JButton connectBT;	// ���� ��ư
	JButton closeBT;	// ���ư��� ��ư
	JTextArea connectLogTA; // ��� �α� â
	SocketThread client;
	Frame clientFrame;	// â ���Ḧ ���� ��ü
	ClientFrame() {
		clientFrame = this;
		setTitle("��Ʈ��ũ ���� ����");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		
		contentPane.setLayout(new FlowLayout());
		
		ipTF = new JTextField(12);
		connectBT = new JButton("����");
		connectBT.addActionListener(new BtnListener());
		closeBT = new JButton("���");
		closeBT.addActionListener(new BtnListener());
		connectLogTA = new JTextArea(12, 20); 
		
		contentPane.add(ipTF);
		contentPane.add(connectBT);
		contentPane.add(new JScrollPane(connectLogTA));
		contentPane.add(closeBT);
		
		setSize(40*9, 400);
		setVisible(true);
	}
	
	class BtnListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JButton b = (JButton)e.getSource();
			if(b.getText() == "����") {
				String ip = ipTF.getText();
				if(!ip.isEmpty())
				{
					closeBT.setEnabled(false);
					client = new SocketThread(new Client(), clientFrame, ip, connectLogTA, closeBT);
					client.start();
				}
			}
			else {
				clientFrame.dispose();
			}
		}
	}
}
