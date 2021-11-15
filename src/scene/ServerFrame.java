package scene;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import networking.SocketThread;
import networking.Server;

/*
 * 네트워크 게임 생성 시 나타나는 프레임
 */

// 네트워크 게임 생성 버튼 클릭시 생기는 창
public class ServerFrame extends JFrame{
	private SocketThread server;
	private Frame hostFrame;
	ServerFrame() {
		hostFrame = this;
		setTitle("네트워크 게임 생성");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		
		contentPane.setLayout(new FlowLayout());
		
		server = new SocketThread(new Server(), hostFrame);
		
		String ipNum = server.getSocket().GetHostIP();
		contentPane.add(new JLabel("호스트 ip주소 : " + ipNum));
		contentPane.add(new JLabel("게임 참여를 기다리는 중입니다..."));
		server.start();
		JButton bt = new JButton("취소");
		bt.addActionListener(new BtnListener());
		contentPane.add(bt);
		
		setSize(40*9, 200);
		setVisible(true);
	}


	class BtnListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(((JButton)e.getSource()).getText() == "취소") {
				server.interrupt();	// 스레드 종료
				new Menu();
				hostFrame.dispose();
			}
		}

	}

}
