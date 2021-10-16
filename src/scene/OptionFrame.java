package scene;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

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
		String[] optionString = {"이름 변경", "저장 디렉토리 변경", "복기용 게임 디렉토리 변경", "효과음 크기", "장애물 갯수"};
		JLabel[] optionLabels = new JLabel[5];
		String[] defaultOptions = {"Player", "complete_save/", "incomplete_save/", "10", "10"};
				
		JTextField[] optionTexts = new JTextField[6];
		JButton ClearCompletedSaveFiles = new JButton("완료된 게임 기록 삭제");
		JButton ClearIncompletedSaveFiles = new JButton("진행중인 게임 기록 삭제");
		JButton OKbutton = new JButton("적용");
		JButton CANCELbutton = new JButton("닫기");
		
		private Frame optionFrame;
		
		OptionPanel(Frame optionFrame) {
			this.optionFrame = optionFrame;
			BtnListener listener = new BtnListener();
			for(int i = 0; i < 5; i++) {
				LoadSetting();
				optionLabels[i] = new JLabel(optionString[i]);
				add(optionLabels[i]);
				optionTexts[i] = new JTextField(defaultOptions[i], 20);
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
					int result = JOptionPane.showConfirmDialog(null, "정말 세이브 파일을 삭제하시겠습니까?", "Confirm", JOptionPane.YES_NO_OPTION);
					if(result == JOptionPane.YES_OPTION) {
						ClearSaveFiles(true);
						JOptionPane.showMessageDialog(null, "삭제되었습니다.", "삭제 완료", JOptionPane.INFORMATION_MESSAGE | JOptionPane.OK_OPTION);
					}
				}
				else if(e.getSource() == ClearIncompletedSaveFiles) {
					int result = JOptionPane.showConfirmDialog(null, "정말 세이브 파일을 삭제하시겠습니까?", "Confirm", JOptionPane.YES_NO_OPTION);
					if(result == JOptionPane.YES_OPTION) {
						ClearSaveFiles(false);
						JOptionPane.showMessageDialog(null, "삭제되었습니다.", "삭제 완료", JOptionPane.INFORMATION_MESSAGE | JOptionPane.OK_OPTION);
					}
				}
				else if(e.getSource() == OKbutton) {
					if(Integer.parseInt(optionTexts[3].getText()) >= 0 && Integer.parseInt(optionTexts[3].getText()) <= 20) 
						SaveSetting();			// 옵션 저장
					else
						JOptionPane.showMessageDialog(null, "볼륨값을 0에서 20 사이로 맞춰주세요.", "범위 오류", JOptionPane.INFORMATION_MESSAGE | JOptionPane.OK_OPTION);
				}
				else if(e.getSource() == CANCELbutton) {
					new Menu();
					optionFrame.dispose();	// 옵션 나가기
				}
			}
		}
		
		// 옵션 저장
		public void SaveSetting() {
			FileWriter fout = null;
			
			for(int i = 0; i < 5; i++)
			{
				defaultOptions[i] = optionTexts[i].getText();
			}
			
			try {
				fout = new FileWriter("option/setting.txt");
		
				for(int i = 0; i < 5; i++)
					fout.write(defaultOptions[i] + "\r\n");
				JOptionPane.showMessageDialog(null, "적용되었습니다.", "알림", JOptionPane.INFORMATION_MESSAGE | JOptionPane.OK_OPTION);
				fout.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "저장하는 과정에서 문제가 발생하였습니다.", "저장 오류", JOptionPane.OK_OPTION);
			}
		}
		
		public void LoadSetting() {	// 저장된 옵션을 불러오는 함수
			try {
				FileReader fin = new FileReader("option/setting.txt");
				
				int c, i = 0;
				String[] options = new String[5];
				String temp = "";
				while((c = fin.read()) != -1)
				{
					if(c == '\r')
					{
						c = fin.read();
						c = fin.read();
						options[i] = temp;
						i++;
						temp = "";
					}
					temp += (char)c;
				}
				
				defaultOptions[0] = options[0];	// 플레이어 이름
				defaultOptions[1] = options[1];	// 저장 디렉토리
				defaultOptions[2] = options[2];	// 복기용 저장 디렉토리
				defaultOptions[3] = options[3];	// 효과음 크기
				defaultOptions[4] = options[4];	// 벽의 개수
				fin.close();
			}
			catch (IOException e) {
				
			}
		}
		public void ClearSaveFiles(boolean isCompleted) {
			File[] subFiles;
			if(isCompleted == true)
				subFiles = new File(defaultOptions[1]).listFiles();
			else
				subFiles = new File(defaultOptions[2]).listFiles();
			
			for(int i = 0; i < subFiles.length; i++) {
				File f = subFiles[i];
				String s = f.getName();
				int index = s.lastIndexOf(".txt");
				if(index != -1)
				{
					System.out.println(f.getPath() + " 삭제");
					f.delete();
				}
			}
		}
	}
}
