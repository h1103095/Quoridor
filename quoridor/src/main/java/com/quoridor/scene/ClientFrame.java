package com.quoridor.scene;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

import com.quoridor.enums.GAME_MODE;
import com.quoridor.game.manager.GameManager;
import com.quoridor.networking.Client;

/*
 * 네트워크 게임 참가 시 나타나는 프레임
 * 클라이언트 클래스를 포함하고 있음
 */

// 게임 참여 시 생기는 창
public class ClientFrame extends JFrame{
	private Frame clientFrame;
	private JTextField hostIPTextField; 	// IP주소를 입력하는 텍스트 필드
	private JTextField portTextField;
	private JButton connectButton;	// 접속 버튼
	private JButton cancelButton;	// 돌아가기 버튼
	private JTextArea connectLogTextField; // 통신 로그 창
	private JLabel hostIPLabel;
	private JLabel portLabel;
	private String defaultPort = "9876";
	private Client client;

	private int numJObjects = 7;

	ClientFrame() {
		clientFrame = this;
		setTitle("네트워크 게임 참여");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Layout 설정
		Container contentPane = getContentPane();
		GridBagConstraints[] gridBagConstraints = new GridBagConstraints[numJObjects];
		GridBagLayout gridBagLayout = new GridBagLayout();
		contentPane.setLayout(gridBagLayout);

		for(int i=0; i < numJObjects; i++) {
			gridBagConstraints[i] = new GridBagConstraints();
		}
		
		hostIPLabel = new JLabel("호스트 ip주소");
		portLabel = new JLabel("포트 번호");
		hostIPTextField = new JTextField(12);
		portTextField = new JTextField(12);
		portTextField.setText(defaultPort);

		connectButton = new JButton("접속");
		cancelButton = new JButton("취소");
		connectButton.addActionListener(new BtnListener());
		cancelButton.addActionListener(new BtnListener());
		connectLogTextField = new JTextArea(12, 20);
		
		gridBagConstraints[0].gridy = 0;
		gridBagConstraints[0].gridx = 0;
		contentPane.add(hostIPLabel, gridBagConstraints[0]);

		gridBagConstraints[1].gridy = 0;
		gridBagConstraints[1].gridx = 1;
		contentPane.add(hostIPTextField, gridBagConstraints[1]);

		gridBagConstraints[2].gridy = 0;
		gridBagConstraints[2].gridx = 2;
		contentPane.add(connectButton, gridBagConstraints[2]);

		gridBagConstraints[3].gridy = 1;
		gridBagConstraints[3].gridx = 0;
		contentPane.add(portLabel, gridBagConstraints[3]);

		gridBagConstraints[4].gridy = 1;
		gridBagConstraints[4].gridx = 1;
		contentPane.add(portTextField, gridBagConstraints[4]);

		gridBagConstraints[5].gridy = 1;
		gridBagConstraints[5].gridx = 2;
		contentPane.add(cancelButton, gridBagConstraints[5]);

		gridBagConstraints[6].gridy = 2;
		gridBagConstraints[6].gridx = 0;
		gridBagConstraints[6].gridwidth = 3;
		gridBagConstraints[6].fill = GridBagConstraints.BOTH;
		contentPane.add(new JScrollPane(connectLogTextField), gridBagConstraints[6]);
		
		setSize(40*9, 400);
		setVisible(true);
	}

	private void JoinServer(String hostIP) {
		try {
			int port = Integer.decode(portTextField.getText()).intValue();

			client = new Client();
			client.JoinServer(hostIP, port);
			new GameManager(GAME_MODE.NETWORK_GUEST, client);
			this.dispose();
		} catch (UnknownHostException e) {
			connectLogTextField.append("연결 실패. ip주소를 확인해 주세요.\n");
			cancelButton.setEnabled(true);
		} catch (ConnectException e) {
			connectLogTextField.append("연결 실패. ip주소를 확인해 주세요.\n");
			cancelButton.setEnabled(true);
		} catch (SocketException e) {
			connectLogTextField.append("소켓 오류 발생\n");
			cancelButton.setEnabled(true);
		} catch (IOException e) {
			connectLogTextField.append("데이터 입출력 문제 발생\n");
			cancelButton.setEnabled(true);
		} catch (NumberFormatException e) {
			connectLogTextField.append("연결 실패. 포트 번호에 문자열이 들어갈 수 없습니다.\n");
			cancelButton.setEnabled(true);
		} catch (Exception e) {
			connectLogTextField.append("연결 실패.\n");
			cancelButton.setEnabled(true);
			e.printStackTrace();
		}
	}
	
	class BtnListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object clickedObject = e.getSource();
			if(clickedObject == connectButton) {
				String hostIP = hostIPTextField.getText();
				if(!hostIP.isEmpty()) {
					cancelButton.setEnabled(false);
					JoinServer(hostIP);
				} else {
					connectLogTextField.append("ip주소를 입력해 주세요.\n");
				}
			} else if(clickedObject == cancelButton) {
				// 메뉴 화면으로 이동
				new MenuFrame();
				if(client != null) {
					client.CloseSocket();
				}
				clientFrame.dispose();
			}
		}
	}
}
