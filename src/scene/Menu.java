package scene;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.*;

import object.GameManager;
import enums.GAME_MODE;

/*
 * 맨 처음 게임 실행 시 나타나는 프레임과 그 구성요소
 */



// 메뉴 프레임
public class Menu extends JFrame{
	public Menu() {
		Point appSize = new Point(1000, 600);	// 창 사이즈
		Point menuSize = new Point(400, 400);	// 버튼 부분 사이즈
		
		setTitle("Quoridor");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		
		BackgroundPanel bp = new BackgroundPanel();
		bp.setSize(appSize.x, appSize.y);
		bp.setLayout(null);
		
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
		String[] menuLabels = {
				"1인용 게임", "2인용 게임", "2인용 네트워크 게임 (게임생성)", "2인용 네트워크 게임 (게임참여)",
				"이전게임 Load", "이전게임 복기", "환경설정", "종료"
		};
		ImageIcon[] buttonIcons = {
				new ImageIcon("images/SINGLE.png"), new ImageIcon("images/MULTY.png"), new ImageIcon("images/NetWork.jpg"),
				new ImageIcon("images/NetWork.jpg"), new ImageIcon("images/Load.jpg"), new ImageIcon("images/Replay.png"),
				new ImageIcon("images/Option.jpg"), new ImageIcon("images/Exit.png")
		};
		JButton[] menuButtons = new JButton[8];
		GameManager GM;
		String defaultCompletedSaveDirectory = "complete_save/";
		String defaultIncompletedSaveDirectory = "incomplete_save/";
		private Frame Menu;
		public MenuPanel(Frame menu) {
			this.Menu = menu; 
			BtnListener listener = new BtnListener();
			for(int i = 0; i < 8; i++) {
				menuButtons[i] = new JButton(menuLabels[i], buttonIcons[i]);
				menuButtons[i].addActionListener(listener);
				add(menuButtons[i]);
			}
		}
		
		class BtnListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == menuButtons[0]) { // 1인용 게임
					GM = new GameManager(GAME_MODE.SINGLE);
					new Game(GM);
					Menu.dispose(); // 메뉴 닫음
				}
				else if(e.getSource() == menuButtons[1]) { // 2인용 게임
					GM = new GameManager(GAME_MODE.MULTY);
					new Game(GM);
					Menu.dispose();
				}else if(e.getSource()==menuButtons[2]) { // 네트워크 호스트
					new ServerFrame();
					Menu.dispose();
				}
				else if(e.getSource()==menuButtons[3]) { // 네트워크 게스트
					new ClientFrame();
					Menu.dispose();
				}
				else if(e.getSource()==menuButtons[4]) { // 게임 로드
					JFileChooser fc = new JFileChooser();
					File file;
					LoadSetting();
					fc.setCurrentDirectory(new File(defaultIncompletedSaveDirectory));	// 세이브 경로
					fc.setFileFilter(new FileNameExtensionFilter("TXT File", "txt"));	// 텍스트 파일만 보이도록 설정
					
					int returnVal = fc.showOpenDialog(Menu.this);						// 파일 여는 창 생성
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						file = fc.getSelectedFile();
						GM = new GameManager(GAME_MODE.LOADGAME, file);
						new Game(GM);
						System.out.println("Opening: " + file.getName() + ".");
						Menu.dispose();
					}
				}
				else if(e.getSource()==menuButtons[5]) { // 리플레이
					JFileChooser fc = new JFileChooser();
					LoadSetting();
					fc.setCurrentDirectory(new File(defaultCompletedSaveDirectory));	// 종료된 게임 세이브 경로
					fc.setFileFilter(new FileNameExtensionFilter("TXT File", "txt"));
					
					int returnVal = fc.showOpenDialog(Menu.this);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						GM = new GameManager(GAME_MODE.REPLAY, file);
						new Replay(GM);
						System.out.println("Opening: " + file.getName() + ".");
						Menu.dispose();
					}
				}
				else if(e.getSource()==menuButtons[6]) { // 환경설정
					new OptionFrame();
					Menu.dispose();
				}
				else if(e.getSource()==menuButtons[7]) { // 종료
					System.exit(0);
				}
			}
		}
		public void LoadSetting() {
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
				defaultCompletedSaveDirectory = options[1];
				defaultIncompletedSaveDirectory = options[2];		
				fin.close();
			}
			catch (IOException e) {
				
			}
		}
	}
}