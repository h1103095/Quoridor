package com.quoridor.scene;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.*;

import com.quoridor.enums.GAME_MODE;
import com.quoridor.enums.GAME_OPTION;
import com.quoridor.game.manager.GameManager;
import com.quoridor.game.manager.OptionManager;
import com.quoridor.logger.MyLogger;

/*
 * 맨 처음 게임 실행 시 나타나는 프레임과 그 구성요소
 */

// 메뉴 프레임
public class MenuFrame extends JFrame{
	public MenuFrame() {
		Point appSize = new Point(1000, 600);	// 창 사이즈
		Point menuSize = new Point(400, 400);	// 버튼 부분 사이즈
		String title = "Quoridor";
		
		setTitle(title);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		
		// 배경 설정
		BackgroundPanel bp = new BackgroundPanel();
		bp.setSize(appSize.x, appSize.y);
		bp.setLayout(null);
		
		// 메뉴(버튼들) 설정
		MenuPanel mp = new MenuPanel(this);
		mp.setLayout(new GridLayout(8, 1, 0, 3));
		mp.setLocation((appSize.x - menuSize.x)/2, (appSize.y - menuSize.y)/2);	// 버튼들 위치를 창의 중앙으로 설정
		mp.setSize(menuSize.x, menuSize.y);
		
		contentPane.setLayout(null);
		bp.add(mp);
		contentPane.add(bp);

		setSize(appSize.x, appSize.y);
		setVisible(true);
		
		mp.requestFocus();
	}


	// 버튼 패널
	class MenuPanel extends JPanel {
		private OptionManager optionManager = OptionManager.getInstance();
		private Frame Menu;
		
		public MenuPanel(Frame menu) {
			this.Menu = menu;

			JButton onePlayerButton = new JButton("1인용 게임", new ImageIcon("quoridor\\src\\main\\java\\resources\\QuoridorResources\\images\\Single.png"));
			JButton twoPlayerButton = new JButton("2인용 게임", new ImageIcon("quoridor\\src\\main\\java\\resources\\QuoridorResources\\images\\Multy.png"));
			JButton serverButton = new JButton("2인용 네트워크 게임(게임생성)", new ImageIcon("quoridor\\src\\main\\java\\resources\\QuoridorResources\\images\\Network.jpg"));
			JButton clientButton = new JButton("2인용 네트워크 게임(게임참여)", new ImageIcon("quoridor\\src\\main\\java\\resources\\QuoridorResources\\images\\Network.jpg"));
			JButton loadButton = new JButton("이전게임 Load", new ImageIcon("quoridor\\src\\main\\java\\resources\\QuoridorResources\\images\\Load.jpg"));
			JButton replayButton = new JButton("이전게임 리플레이", new ImageIcon("quoridor\\src\\main\\java\\resources\\QuoridorResources\\images\\Replay.jpg"));
			JButton optionButton = new JButton("환경설정", new ImageIcon("quoridor\\src\\main\\java\\resources\\QuoridorResources\\images\\Option.jpg"));
			JButton exitButton = new JButton("종료", new ImageIcon("quoridor\\src\\main\\java\\resources\\QuoridorResources\\images\\Exit.png"));
	
			onePlayerButton.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new GameManager(GAME_MODE.SINGLE);
						Menu.dispose(); // 메뉴 닫음
					}
				}
			);

			twoPlayerButton.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new GameManager(GAME_MODE.MULTY);
						Menu.dispose();
					}
				}
			);

			serverButton.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new ServerFrame();
						Menu.dispose();
					}
				}
			);

			clientButton.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new ClientFrame();
						Menu.dispose();
					}
				}
			);

			loadButton.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JFileChooser fc = new JFileChooser();
						File file;
						fc.setCurrentDirectory(new File(optionManager.getConfig(GAME_OPTION.INCOMPLETED_GAME_SAVE_DIRECTORY)));	// 세이브 경로
						fc.setFileFilter(new FileNameExtensionFilter("DATA File", "data"));	// 텍스트 파일만 보이도록 설정
						
						int returnVal = fc.showOpenDialog(MenuFrame.this);	// 파일 여는 창 생성
						
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							file = fc.getSelectedFile();
							new GameManager(GAME_MODE.LOAD_GAME, file);
							MyLogger.getInstance().info("저장된 게임을 불러옵니다. 파일 경로: " + file.getName());
							Menu.dispose();
						}
					}
				}
			);

			replayButton.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JFileChooser fc = new JFileChooser();
						fc.setCurrentDirectory(new File(optionManager.getConfig(GAME_OPTION.COMPLETED_GAME_SAVE_DIRECTORY)));	// 종료된 게임 세이브 경로
						fc.setFileFilter(new FileNameExtensionFilter("DATA File", "data"));
						
						int returnVal = fc.showOpenDialog(MenuFrame.this);
						
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							new GameManager(GAME_MODE.REPLAY, file);
							MyLogger.getInstance().info("저장된 게임을 복기합니다. 파일 경로: " + file.getName());
							Menu.dispose();
						}
					}
				}
			);

			optionButton.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new OptionFrame();
						Menu.dispose();
					}
				}
			);

			exitButton.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					}
				}
			);
	
			JButton[] menuButtons = {
				onePlayerButton,
				twoPlayerButton,
				serverButton,
				clientButton,
				loadButton,
				replayButton,
				optionButton,
				exitButton
			};

			for(int i = 0; i < 8; i++) {
				this.add(menuButtons[i]);
			}
		}
	}
}