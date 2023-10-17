package com.quoridor.scene;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import com.quoridor.networking.Server;

/*
 * 네트워크 게임 생성 버튼을 누를 시 나타나는 프레임
 */

public class ServerFrame extends JFrame{
	private Frame hostFrame;
	private Container contentPane;

	private JButton openServerButton;
	private JButton cancelButton;
	private JLabel portLabel;
	private JTextField portTextField;
	private JTextArea connectLogTextField; // 통신 로그 창

	private String defaultPort = "9876";
	private int numJObjects = 5;
	private Server server;

	ServerFrame() {
		hostFrame = this;
		setTitle("네트워크 게임 생성");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		GridBagConstraints[] gridBagConstraints = new GridBagConstraints[numJObjects];
		GridBagLayout gridBagLayout = new GridBagLayout();
		contentPane = getContentPane();
		contentPane.setLayout(gridBagLayout);

		for(int i=0; i < numJObjects; i++) {
			gridBagConstraints[i] = new GridBagConstraints();
		}

		portLabel = new JLabel("포트 번호");
		portTextField = new JTextField(12);
		portTextField.setText(defaultPort);

		openServerButton = new JButton("게임 생성");
		cancelButton = new JButton("취소");

		BtnListener buttonListener = new BtnListener();
		openServerButton.addActionListener(buttonListener);
		cancelButton.addActionListener(buttonListener);
		connectLogTextField = new JTextArea(12, 20);

		gridBagConstraints[0].gridy = 0;
		gridBagConstraints[0].gridx = 0;
		contentPane.add(portLabel, gridBagConstraints[0]);

		gridBagConstraints[1].gridy = 1;
		gridBagConstraints[1].gridx = 0;
		contentPane.add(portTextField, gridBagConstraints[1]);

		gridBagConstraints[2].gridy = 0;
		gridBagConstraints[2].gridx = 1;
		contentPane.add(openServerButton, gridBagConstraints[2]);

		gridBagConstraints[3].gridy = 1;
		gridBagConstraints[3].gridx = 1;
		contentPane.add(cancelButton, gridBagConstraints[3]);

		gridBagConstraints[4].gridy = 2;
		gridBagConstraints[4].gridx = 0;
		gridBagConstraints[4].gridwidth = 2;
		gridBagConstraints[4].fill = GridBagConstraints.BOTH;
		contentPane.add(new JScrollPane(connectLogTextField), gridBagConstraints[4]);
		
		setSize(400, 400);
		setVisible(true);
	}

	private void openServer() {
		try {
			server = new Server();
			int port = Integer.parseInt(portTextField.getText());
			String ipAddress = server.getIP();

			connectLogTextField.append("호스트 ip주소: " + ipAddress + '\n');
			connectLogTextField.append("다른 플레이어를 기다리는 중입니다...\n");

			server.openServer(port, this);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(
				null,
				"연결 실패. 포트 번호에 문자열이 들어갈 수 없습니다.",
				"오류",
				JOptionPane.INFORMATION_MESSAGE | JOptionPane.OK_OPTION
				);
			openServerButton.setEnabled(true);
		}
	}

	public void setOpenServerButtonEnabled(boolean enable) {
		openServerButton.setEnabled(enable);
	}

	public void appendStringToTextArea(String str) {
		connectLogTextField.append(str);
	}

	class BtnListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object clickedObject = e.getSource();
			if(clickedObject == openServerButton) {
				openServer();
				openServerButton.setEnabled(false);
			} else if(clickedObject == cancelButton) {
				// 메뉴 화면으로 이동
				if(server != null) {
					server.closeSocket();
				}
				new MenuFrame();
				hostFrame.dispose();
			}
		}
	}
}
