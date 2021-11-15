package scene;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import networking.SocketThread;
import networking.Client;

/*
 * 네트워크 게임 참가 시 나타나는 프레임
 * 클라이언트 클래스를 포함하고 있음
 */

// 게임 참여 시 생기는 창
public class ClientFrame extends JFrame{
	JTextField ipTF; 	// IP주소를 입력하는 텍스트 필드
	JButton connectBT;	// 접속 버튼
	JButton closeBT;	// 돌아가기 버튼
	JTextArea connectLogTA; // 통신 로그 창
	SocketThread client;
	Frame clientFrame;	// 창 종료를 위한 객체
	ClientFrame() {
		clientFrame = this;
		setTitle("네트워크 게임 참여");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		
		contentPane.setLayout(new FlowLayout());
		
		ipTF = new JTextField(12);
		connectBT = new JButton("접속");
		connectBT.addActionListener(new BtnListener());
		closeBT = new JButton("취소");
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
			if(b.getText() == "접속") {
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
