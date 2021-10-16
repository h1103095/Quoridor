package scene;

import java.awt.*;
import java.awt.event.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.io.*;

import object.GameManager;
import enums.GAME_MODE;


/*
 * 복기가 진행되는 프레임과 그 구성요소
 */

// 복기 화면 구성
public class Replay extends JFrame{
	private GameManager GM;
	private GamePanel gamePanel;
	private ButtonPanel buttonPanel;
	private InfoPanel infoPanel;
	private GameSound move_sound = new GameSound("sounds/move.wav");
	private GameSound wall_sound = new GameSound("sounds/wall.wav");

	
	public Replay(GameManager gm) {
		this.GM = gm;
		
		/* 화면 구성 */
		setTitle("Replay");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//setContentPane();
		Container contentPane = getContentPane();
		buttonPanel = new ButtonPanel(GM, this);
		infoPanel = new InfoPanel(GM);
		gamePanel = new GamePanel(GM, infoPanel, buttonPanel, this);
		
		contentPane.setLayout(null);
		contentPane.add(gamePanel);
		
		// 패널들 합치기
		contentPane.setLayout(new BorderLayout());
		contentPane.setBackground(new Color(150, 50, 0));
		
		contentPane.add(buttonPanel, BorderLayout.NORTH);
		contentPane.add(gamePanel, BorderLayout.CENTER);
		contentPane.add(infoPanel, BorderLayout.SOUTH);
		
		setSize(40*9, 40*10+100);
		setVisible(true);
	}
	
	class GameSound{
		File file;
		GameSound(String root) { 
			file = new File(root);
		}
		public void play() {
			Thread st = new Thread(new SoundThread());
			st.start();
		}
		
		class SoundThread implements Runnable {
			public void run() {
				try {
					AudioInputStream inputStream = AudioSystem.getAudioInputStream(file);
					Clip clip = AudioSystem.getClip();
					clip.open(inputStream);
					FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			        float volume = (float)((GM.GetVolume()/2) + 10) / 20;					// 설정 볼륨
			        float range = control.getMaximum() - control.getMinimum();	// 범위 계산
			        float result = (range * volume) + control.getMinimum();		// 최종 볼륨값
			        control.setValue(result);									// 볼륨 설정
					clip.start();												// 소리 재생
				} catch(Exception e)
				{
					System.out.println("사운드 출력 오류");
				}
			}
		}
			
	}
	
	class ButtonPanel extends JPanel {
		JButton[] menuButtons = new JButton[3];
		GameManager GM;
		GAME_MODE gameMode;
		private Frame Game; // 창을 닫기 위한 변수
		
		ButtonPanel(GameManager gm, Frame game) {
			this.Game = game;
			this.GM = gm;
			this.gameMode = GM.GetGameMode();
			setBackground(new Color(150, 50, 0));
			
			// 메뉴 패널 버튼 배치
			setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			String[] menubtStrings = {"앞으로", "중단", "뒤로"};
			
			BtnListener listener = new BtnListener();
			
			for(int i = 0; i < 3; i++) {
				menuButtons[i] = new JButton(menubtStrings[i]);
				menuButtons[i].addActionListener(listener);
				this.add(menuButtons[i]); 
			}
			
			System.out.println(GM.GetTurnCount());
			System.out.println(GM.GetSelectTurn());
			
			if(GM.GetTurnCount() == GM.GetSelectTurn())
			{
				menuButtons[0].setEnabled(false);
				menuButtons[2].setEnabled(false);
			}

			checkbtAvailable();
		}
		
		class BtnListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == menuButtons[0]) {			// 앞으로
					String msg = GM.GetNextTurn();
					GM.ChangeTurn();
					gamePanel.moveTurn(msg, false);
				}else if(e.getSource() == menuButtons[1]) {		// 재생 || 중단 || 종료
					if(menuButtons[1].getText().equals("재생"))		// 재생
					{
						gamePanel.th = new Thread(new ReplayThread());
						gamePanel.th.start();
						menuButtons[1].setText("종료");
					}
					else if(menuButtons[1].getText().equals("중단"))	// 중단
					{
						gamePanel.th.interrupt();
						menuButtons[1].setText("재생");
					}
					else											// 종료
					{
						gamePanel.th.interrupt();
						new Menu();
						Game.dispose();
					}
					
				}else if(e.getSource() == menuButtons[2]) {		// 뒤로
					String msg = GM.GetPrevTurn();
					gamePanel.moveTurn(msg, true);
					GM.ChangeTurn();
				}
			}
		}

		// 버튼이 사용 가능한지를 체크하는 함수
		public void checkbtAvailable() {
			if(GM.GetSelectTurn() == 1)
				menuButtons[2].setEnabled(false);
			else
				menuButtons[2].setEnabled(true);
			if(GM.GetSelectTurn() == GM.GetTurnCount())
			{
				menuButtons[0].setEnabled(false);
				menuButtons[1].setText("재생");
				gamePanel.th.interrupt();
			}
			else
				menuButtons[0].setEnabled(true);
		}
	}
	
	// 리플레이를 위한 스레드
	class ReplayThread implements Runnable {
		public void run() {
			while(true) {
				try {
					Thread.sleep(1000);
					if(GM.GetTurnCount() == GM.GetSelectTurn())
					{
						buttonPanel.menuButtons[0].setEnabled(false);
						break;
					}
					GM.ChangeTurn();
					String msg = GM.GetNextTurn();
					gamePanel.moveTurn(msg, false);
				} catch(InterruptedException e) {
					return;
				}
			}
		}
	}

	// 게임이 진행되는 패널
	class GamePanel extends JPanel{
		GameManager GM;
		InfoPanel infoPanel;
		ButtonPanel buttonPanel;
		private Frame Game;
		boolean turn;
		int wallNum = -1; // 범위 (100 ~ 288) 십의 자리 수와 일의 자리 수는 좌표상의 위치 나타냄, 8보다 크지 않음. 8일 시 7로 바꾸어 계산
		Thread th;
		
		GamePanel(GameManager gm, InfoPanel infoPanel, ButtonPanel buttonPanel, Frame game) {
			this.GM = gm; // 게임 메니저 불러옴
			this.infoPanel = infoPanel; // 정보 패널 불러옴
			this.buttonPanel = buttonPanel;
			this.Game = game;
			th = new Thread(new ReplayThread());
			th.start();
			turn = GM.GetTurn();
		}
		

		
		public void moveTurn(String turnInfo, boolean moveToPrev) {
			if(moveToPrev == true)
			{
				if(turnInfo.substring(6, 10).equals("Wall"))
				{
					int wallNum = Integer.valueOf(turnInfo.substring(11, 14));
					if(turnInfo.substring(0, 5).equals("Black"))
						GM.DeleteWallNum(wallNum, true);
					else
						GM.DeleteWallNum(wallNum, false);
					infoPanel.UpdateWallLabel();
					wall_sound.play();
				}
				else if(turnInfo.substring(6, 10).equals("Move"))
				{
					int moveX = Integer.valueOf(turnInfo.substring(11,12));
					int moveY = Integer.valueOf(turnInfo.substring(13,14));
					int fromX = Integer.valueOf(turnInfo.substring(20,21));
					int fromY = Integer.valueOf(turnInfo.substring(22,23));
					if(turnInfo.substring(0, 5).equals("Black"))
						GM.p1.movePlayer(new Point(fromX, fromY));
					else
						GM.p2.movePlayer(new Point(fromX, fromY));
					move_sound.play();
				}
			}
			else
			{
				if(turnInfo.substring(6, 10).equals("Wall"))
				{
					int wallNum = Integer.valueOf(turnInfo.substring(11, 14));
					if(turnInfo.substring(0, 5).equals("Black"))
						GM.PutWallNum(wallNum, true);
					else
						GM.PutWallNum(wallNum, false);
					infoPanel.UpdateWallLabel();
					wall_sound.play();
				}
				else if(turnInfo.substring(6, 10).equals("Move"))
				{
					int moveX = Integer.valueOf(turnInfo.substring(11,12));
					int moveY = Integer.valueOf(turnInfo.substring(13,14));
					int fromX = Integer.valueOf(turnInfo.substring(20,21));
					int fromY = Integer.valueOf(turnInfo.substring(22,23));
					if(turnInfo.substring(0, 5).equals("Black"))
						GM.p1.movePlayer(new Point(moveX, moveY));
					else
						GM.p2.movePlayer(new Point(moveX, moveY));
					move_sound.play();
				}
			}
			GM.ChangeTurn();
			buttonPanel.checkbtAvailable();
			infoPanel.UpdateTurnLabel();
			infoPanel.UpdateWallLabel();
			turn = GM.GetTurn();
			System.out.println(turn);
			repaint();
		}
		
		// 게임 판 그리기
		public void paintComponent(Graphics g) {
			setBackground(new Color(150, 50, 0));
			super.paintComponent(g);
			
			// 선 그리기
			g.setColor(Color.BLACK);
			for(int i = 1; i < 10; i++) {
				g.drawLine(0, 38*i, 38*9, 38*i); // 가로선
			}
			for(int i = 1; i < 9; i++) {
				g.drawLine(38*i, 0, 38*i, 38*9); // 세로선
			}
			
			// 플레이어 그리기
			drawPlayer(g, 1);
			drawPlayer(g, 2);

			// 벽 그리기
			drawWall(g);
		}
		
		// 플레이어 그리기
		public void drawPlayer(Graphics g, int playerNum) {
			// 흰색 플레이어
			if(playerNum == 1) {
				g.setColor(Color.BLACK);
				g.fillOval(GM.p1.getPoint().x*38, GM.p1.getPoint().y*38, 38, 38);
			}
			// 검은색 플레이어
			else if(playerNum == 2) {
				g.setColor(Color.WHITE);
				g.fillOval(GM.p2.getPoint().x*38, GM.p2.getPoint().y*38, 38, 38);
			}
			
		}
		
		// 벽 그리기
		public void drawWall(Graphics g) {
			g.setColor(Color.YELLOW);
			
			boolean[][][] WallPoints = GM.GetWallPoint();
			for(int i = 0; i < 8; i++) {
				for(int j = 0; j < 8; j++) {
					if(WallPoints[0][j][i])
					{
						g.fillRect(38*j, 38*(i+1) - 2, 76, 3); // 가로벽
					}
					if(WallPoints[1][j][i])
					{
						g.fillRect(38*(j+1) - 2, 38*i, 3, 76); // 세로벽
					}
				}
			}
		}
	}

	// 게임 하단에 정보를 나타내주는 패널
	class InfoPanel extends JPanel {
		private GameManager GM;
		JLabel wallLabelp1;
		JLabel wallLabelp2;
		JLabel turnLabel;
		InfoPanel(GameManager gm) {
			setBackground(new Color(150, 50, 0));
			
			this.GM = gm; 
			setLayout(new BorderLayout());
			
			// 검은색 Walls 라벨
			wallLabelp1 = new JLabel("Walls: " + GM.p1.getWallNum());
			wallLabelp1.setFont(new Font("돋움", Font.ITALIC, 15));
			wallLabelp1.setForeground(Color.BLACK);
			wallLabelp1.setSize(100, 30);

			// 하얀색 Walls 라벨
			wallLabelp2 = new JLabel("Walls: " + GM.p2.getWallNum());
			wallLabelp2.setFont(new Font("돋움", Font.ITALIC, 15));
			wallLabelp2.setForeground(Color.WHITE);
			wallLabelp2.setSize(100, 30);
			
			// 현재 턴 라벨
			if(GM.GetTurn() == true) {
				turnLabel = new JLabel("BLACK");
				turnLabel.setForeground(Color.BLACK);
			}
			else {
				turnLabel = new JLabel("WHITE");
				turnLabel.setForeground(Color.WHITE);
			}
			turnLabel.setFont(new Font("돋움", Font.ITALIC | Font.BOLD, 25));
			turnLabel.setSize(160, 50);
			turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			// 정보 패널 배치
			this.add(wallLabelp1, BorderLayout.WEST);
			this.add(turnLabel, BorderLayout.CENTER);
			this.add(wallLabelp2, BorderLayout.EAST);
		}
		public void UpdateTurnLabel() {
			if(GM.GetTurn() == true) {
				turnLabel.setText("BLACK");
				turnLabel.setForeground(Color.BLACK);
			}
			else {
				turnLabel.setText("WHITE");
				turnLabel.setForeground(Color.WHITE);
			}
		}
		public void UpdateWallLabel() {
			wallLabelp1.setText("Walls: " + GM.p1.getWallNum());
			wallLabelp2.setText("Walls: " + GM.p2.getWallNum());
		}
	}
}

