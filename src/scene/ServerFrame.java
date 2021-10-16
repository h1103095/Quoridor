package scene;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import networking.SocketThread;
import networking.Server;

/*
 * ��Ʈ��ũ ���� ���� �� ��Ÿ���� ������
 */

// ��Ʈ��ũ ���� ���� ��ư Ŭ���� ����� â
public class ServerFrame extends JFrame{
	private SocketThread server;
	private Frame hostFrame;
	ServerFrame() {
		hostFrame = this;
		setTitle("��Ʈ��ũ ���� ����");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		
		contentPane.setLayout(new FlowLayout());
		
		server = new SocketThread(new Server(), hostFrame);
		
		String ipNum = server.getSocket().GetHostIP();
		contentPane.add(new JLabel("ȣ��Ʈ ip�ּ� : " + ipNum));
		contentPane.add(new JLabel("���� ������ ��ٸ��� ���Դϴ�..."));
		server.start();
		JButton bt = new JButton("���");
		bt.addActionListener(new BtnListener());
		contentPane.add(bt);
		
		setSize(40*9, 200);
		setVisible(true);
	}


	class BtnListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(((JButton)e.getSource()).getText() == "���") {
				server.interrupt();	// ������ ����
				new Menu();
				hostFrame.dispose();
			}
		}

	}

}
