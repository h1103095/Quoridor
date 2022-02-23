package com.quoridor.scene;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import com.quoridor.enums.GAME_OPTION;
import com.quoridor.game.manager.OptionManager;
import com.quoridor.logger.MyLogger;
import com.quoridor.utils.Utils;

/*
 * 게임 옵션을 변경하는 창
 */

public class OptionFrame extends JFrame{
	
	OptionFrame() {
		setTitle("환경설정");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(null);
		
		Point appSize = new Point(1000, 600);
		Point menuSize = new Point(400, 200);
		
		OptionPanel op = new OptionPanel(this);
		op.setLayout(new GridLayout(7, 2, 0, 3));
		op.setLocation((appSize.x - menuSize.x)/2, (appSize.y - menuSize.y)/2);
		op.setSize(menuSize.x, menuSize.y);
		
		BackgroundPanel bp = new BackgroundPanel();
		bp.setSize(appSize.x, appSize.y);
		bp.setLayout(null);
		
		bp.add(op);
		contentPane.add(bp);
		
		setSize(appSize.x, appSize.y);
		setVisible(true);
	}
	
	class OptionPanel extends JPanel {
		private OptionManager optionManager = OptionManager.getInstance();

		private JLabel[] optionLabels = new JLabel[optionManager.GetNumConfig()];
		private JTextField[] optionTexts = new JTextField[optionManager.GetNumConfig()];
		private JButton ClearCompletedSaveFiles = new JButton("완료된 게임 기록 삭제");
		private JButton ClearIncompletedSaveFiles = new JButton("진행중인 게임 기록 삭제");
		private JButton OKbutton = new JButton("적용");
		private JButton CANCELbutton = new JButton("닫기");
		private GAME_OPTION gameOptions[] = GAME_OPTION.values();
		
		private Frame optionFrame;
		
		public OptionPanel(Frame optionFrame) {
			this.optionFrame = optionFrame;
			BtnListener listener = new BtnListener();

			for (int i=0; i < gameOptions.length; i++) {
				optionLabels[i] = new JLabel(gameOptions[i].toString());
				optionTexts[i] = new JTextField(optionManager.GetConfig(gameOptions[i]), 20);
				add(optionLabels[i]);
				add(optionTexts[i]);
			}

			ClearCompletedSaveFiles.addActionListener(listener);
			ClearIncompletedSaveFiles.addActionListener(listener);
			OKbutton.addActionListener(listener);
			CANCELbutton.addActionListener(listener);
			add(ClearCompletedSaveFiles);
			add(ClearIncompletedSaveFiles);
			add(OKbutton);
			add(CANCELbutton);
		}
		
		class BtnListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == ClearCompletedSaveFiles) {
					int result = JOptionPane.showConfirmDialog(
						null,
						"세이브 파일을 삭제하시겠습니까?",
						"Confirm",
						JOptionPane.YES_NO_OPTION
					);
					if(result == JOptionPane.YES_OPTION) {
						ClearSaveFiles(true);
					}
				}
				else if(e.getSource() == ClearIncompletedSaveFiles) {
					int result = JOptionPane.showConfirmDialog(
						null,
						"세이브 파일을 삭제하시겠습니까?",
						"Confirm",
						JOptionPane.YES_NO_OPTION
					);
					if(result == JOptionPane.YES_OPTION) {
						ClearSaveFiles(false);
					}
				}
				else if(e.getSource() == OKbutton) {
					try {
						int volume = Integer.parseInt(optionTexts[3].getText());
						int numWalls = Integer.parseInt(optionTexts[4].getText());
						if(volume >= 0 && volume <= optionManager.MAX_VOLUME) {
							if(numWalls >= 0 && numWalls <= optionManager.MAX_NUM_WALLS) {
								SaveProperties();			// 옵션 저장
							} else {
								JOptionPane.showMessageDialog(
									null,
									String.format("장애물 개수를 0에서 %d 사이로 맞춰주세요.", optionManager.MAX_NUM_WALLS),
									"범위 오류",
									JOptionPane.INFORMATION_MESSAGE | JOptionPane.OK_OPTION
								);
							}
						} else {
							JOptionPane.showMessageDialog(
								null,
								String.format("볼륨값을 0에서 %d 사이로 맞춰주세요.", optionManager.MAX_VOLUME),
								"범위 오류",
								JOptionPane.INFORMATION_MESSAGE | JOptionPane.OK_OPTION
							);
						}
					} catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(
							null,
							"볼륨과 장애물 개수에 숫자를 입력해 주세요.",
							"오류",
							JOptionPane.INFORMATION_MESSAGE | JOptionPane.OK_OPTION
						);
					}
				}
				else if(e.getSource() == CANCELbutton) {
					new MenuFrame();
					optionFrame.dispose();	// 옵션 나가기
				}
			}
		}
		
		// 옵션 저장
		public void SaveProperties() {
			for(int i = 0; i < optionManager.GetNumConfig(); i++) {
				optionManager.SetConfig(gameOptions[i], optionTexts[i].getText());
			}
			
			if(optionManager.SaveConfig(Utils.configFilePath)) {
				JOptionPane.showMessageDialog(
					null,
					"적용되었습니다.",
					"알림",
					JOptionPane.INFORMATION_MESSAGE | JOptionPane.OK_OPTION
				);
			} else {
				JOptionPane.showMessageDialog(
					null,
					"저장하는 과정에서 문제가 발생하였습니다.",
					"저장 오류",
					JOptionPane.OK_OPTION
				);
			}
		}

		public void ClearSaveFiles(boolean completed) {
			File[] subFiles;
			String saveFolder = "";
			if(completed == true) {
				saveFolder = optionTexts[1].getText();
			} else {
				saveFolder = optionTexts[2].getText();
			}

			File folder = new File(saveFolder);
			if(!folder.exists()) {
				// 파일이 존재하지 않으면
				folder.mkdir();
			} else if(!folder.isDirectory()) {
				// 디렉토리가 아니면
				JOptionPane.showMessageDialog(
					null,
					"경로가 잘못되었습니다.",
					"오류",
					JOptionPane.OK_OPTION
				);
			} else {
				subFiles = new File(saveFolder).listFiles();
				for(int i = 0; i < subFiles.length; i++) {
					File f = subFiles[i];
					f.delete();
					MyLogger.getInstance().info("저장된 파일을 제거하였습니다. 파일 경로: " + f.getPath());
				}

				JOptionPane.showMessageDialog(
					null,
					"삭제가 완료되었습니다.",
					"알림",
					JOptionPane.INFORMATION_MESSAGE | JOptionPane.OK_OPTION
				);
			}
		}
	}
}
