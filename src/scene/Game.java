package scene;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.sound.sampled.*;

import object.GameManager;
import networking.NetWorkSocket;
import enums.GAME_MODE;


/*
 * 게임이 진행되는 프레임과 그 구성요소
 */

/*
 * buttonPanel
 * gamePanel
 * InfoPanel
 * 로 구성됨
 */

// 게임 화면 구성
public class Game extends JFrame{
	private GameManager GM;												// 게임을 종합적으로 관리하는 게임 매니저
	private GamePanel gamePanel;										// 게임 화면을 출력하는 패널
	private ButtonPanel buttonPanel;									// 버튼을 출력하는 패널
	private InfoPanel infoPanel;										// 턴 정보를 출력하는 패널
	private GameSound move_sound = new GameSound("sounds/move.wav");	// 이동 사운드
	private GameSound wall_sound = new GameSound("sounds/wall.wav");	// 벽 사운드
	
	public Game(GameManager gm) {
		this.GM = gm;
		
		/* 화면 구성 */
		setTitle("game");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		buttonPanel = new ButtonPanel(GM, this);
		infoPanel = new InfoPanel(GM);
		gamePanel = new GamePanel(GM, this);
		
		// 패널들 합치기
		contentPane.setLayout(new BorderLayout());
		contentPane.setBackground(new Color(150, 50, 0));
		
		contentPane.add(buttonPanel, BorderLayout.NORTH);
		contentPane.add(gamePanel, BorderLayout.CENTER);
		contentPane.add(infoPanel, BorderLayout.SOUTH);
		
		setSize(40*9, 40*10+100);
		setVisible(true);
	}
	
	// 게임 내 사운드
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
	
	// 게임 화면에서 상단의 버튼 패널
	class ButtonPanel extends JPanel {
		JButton[] menuButtons = new JButton[5];
		GameManager GM;
		GAME_MODE gameMode;
		private Frame Game; // 창을 닫기 위한 변수
		
		ButtonPanel(GameManager gm, Frame game) {
			this.Game = game;
			this.GM = gm;
			this.gameMode = GM.GetGameMode();
			setBackground(new Color(150, 50, 0));
			
			// 메뉴 패널 버튼 배치
			setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			String[] menubtStrings = {"장애물", "Back", "Next", "저장", "기권" };
			
			BtnListener listener = new BtnListener();
			
			for(int i = 0; i < 5; i++) {
				menuButtons[i] = new JButton(menubtStrings[i]);
				menuButtons[i].addActionListener(listener);
				this.add(menuButtons[i]); 
			}
			
			// 네트워크 게임일 시
			if(gameMode == GAME_MODE.NETWORKHOST || gameMode == GAME_MODE.NETWORKGUEST)
			{
				menuButtons[1].setEnabled(false);
				menuButtons[2].setEnabled(false);
				menuButtons[3].setEnabled(false);
			}
			else if(gameMode == GAME_MODE.MULTY)
			{
				menuButtons[1].setEnabled(false);
				menuButtons[2].setEnabled(false);
			}
			checkbtAvailable();
		}
		
		class BtnListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == menuButtons[0]) {			// 장애물
					GM.ChangePutMode();
					Game.repaint();
				}else if(e.getSource() == menuButtons[1]) {		// prev
					String msg = GM.GetPrevTurn();
					gamePanel.moveTurn(msg, true);
					msg = GM.GetPrevTurn();
					gamePanel.moveTurn(msg, true);
				}else if(e.getSource() == menuButtons[2]) {		// next
					String msg = GM.GetNextTurn();
					gamePanel.moveTurn(msg, false);
					msg = GM.GetNextTurn();
					gamePanel.moveTurn(msg, false);
				}else if(e.getSource() == menuButtons[3]) {		// 저장
					String defaultFileName;
					defaultFileName = GM.GetFileName();
					String fileName= JOptionPane.showInputDialog("저장할 파일명을 입력하세요.", defaultFileName);
					if(fileName != null)
					{
						GM.Save(fileName, false);
						new Menu();
						Game.dispose();
					}
				}else if(e.getSource() == menuButtons[4]) {		// 기권
					int give_up= JOptionPane.showConfirmDialog(null, "정말로 기권하시겠습니까?", "잠깐", JOptionPane.YES_NO_OPTION);
					if(give_up == JOptionPane.YES_OPTION)
					{
						if(gameMode == GAME_MODE.NETWORKHOST || gameMode == GAME_MODE.NETWORKGUEST)
							GM.SendData("Give up");
						if(GM.GetTurn() == false)
						{
							JOptionPane.showMessageDialog(null, GM.p1.getPlayerName() + " 승리", "게임 결과", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
							new Menu();
							GM.Save(GM.GetDate(), true);
							Game.dispose();
						}
						else
						{
							JOptionPane.showMessageDialog(null, GM.p2.getPlayerName() + " 승리", "게임 결과", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
							new Menu();
							GM.Save(GM.GetDate(), true);
							Game.dispose();
						}
					}
				}
			}
		}

		
		public void checkbtAvailable() {
			if(GM.GetTurn() == true)						// 검정 턴
			{
				if(gameMode == GAME_MODE.NETWORKHOST)		// 네트워크 호스트일 때
				{
					if(GM.p1.getWallNum() == 0)				// 설치 가능한 벽의 수가 0일 때
						menuButtons[0].setEnabled(false);	// 장애물 버튼 비활성화
					else
						menuButtons[0].setEnabled(true);	// 장애물 버튼 활성화
					menuButtons[4].setEnabled(true);		// 기권 버튼 활성화
				}
				else if(gameMode == GAME_MODE.NETWORKGUEST)
				{
					menuButtons[0].setEnabled(false);		// 장애물 버튼 비활성화
					menuButtons[4].setEnabled(false);		// 기권 버튼 비활성화
				}
				
				else										// 1인 또는 2인 모드
				{
					if(GM.p1.getWallNum() == 0)				
						menuButtons[0].setEnabled(false);
					else
						menuButtons[0].setEnabled(true);
					if(gameMode == GAME_MODE.SINGLE)
					{
						if(GM.GetSelectTurn() <= 2)
							menuButtons[1].setEnabled(false);	// 이전 버튼 비활성화
						else
							menuButtons[1].setEnabled(true);	// 이전 버튼 활성화
						if(GM.GetTurnCount() > GM.GetSelectTurn())
							menuButtons[2].setEnabled(true);	// 다음 버튼 활성화
						else
							menuButtons[2].setEnabled(false);	// 다음 버튼 비활성화
					}
				}
			}
			else											// 하양 턴
			{
				if(gameMode == GAME_MODE.NETWORKGUEST)		// 네트워크 모드일 떄
				{
					if(GM.p2.getWallNum() == 0)
						menuButtons[0].setEnabled(false);
					else
						menuButtons[0].setEnabled(true);
					menuButtons[4].setEnabled(true);
				}
				else if(gameMode == GAME_MODE.NETWORKHOST)
				{
					menuButtons[0].setEnabled(false);
					menuButtons[4].setEnabled(false);
				}
				else
				{
					if(GM.p2.getWallNum() == 0)				// 1인 또는 2인 모드
						menuButtons[0].setEnabled(false);
					else
						menuButtons[0].setEnabled(true);
					menuButtons[1].setEnabled(false);		// 이전 버튼 비활성화
					menuButtons[2].setEnabled(false);		// 다음 버튼 비활성화
				}
			}
		}
	}

	// 게임이 진행되는 패널
	class GamePanel extends JPanel{
		GameManager GM;
		NetWorkThread netThread;
		GAME_MODE gameMode;
		Point[] availablePoints;
		private Frame Game;
		boolean turn;
		int wallNum = -1; // 범위 (100 ~ 288) 십의 자리 수와 일의 자리 수는 좌표상의 위치 나타냄, 8보다 크지 않음. 8일 시 7로 바꾸어 계산
		
		GamePanel(GameManager gm,  Frame game) {
			this.GM = gm; // 게임 메니저 불러옴
			this.gameMode = GM.GetGameMode();
			this.Game = game;
			
			turn = GM.GetTurn();
			
			MyMouseListener listener = new MyMouseListener();
			addMouseListener(listener);
			addMouseMotionListener(listener);
			
			if(gameMode == GAME_MODE.SINGLE)
			{
				if(turn == false)
				{
					String msg = GM.p2.AIBehavior();
					updateTurn(msg);
					GM.Record(msg);
				}
			}
			else if(gameMode == GAME_MODE.NETWORKHOST || gameMode == GAME_MODE.NETWORKGUEST) // 네트워크 게임일 시
			{
				netThread = new NetWorkThread(GM.GetSocket());							// 통신 위한 쓰레드 생성
				if((gameMode == GAME_MODE.NETWORKHOST && GM.GetTurn() == false) || (gameMode == GAME_MODE.NETWORKGUEST && GM.GetTurn() == true))
				{																			// 네트워크 게임에서 시작 시 자신의 턴이 아닐 시
					netThread.start();														// 상대로부터 상대의 행동 받아옴
					availablePoints = GM.GetAvailablePoint();								// 이동 가능한 위치 표시. 이 상태에서는 이동 가능한 위치가 표시되지 않게 하기 위함
					buttonPanel.checkbtAvailable();											// 버튼의 가능 여부 설정
				}
			}
		}
		
		// 데이터를 받기 위한 쓰레드
		// 쓰레드를 생성하지 않을 시 프로그램이 계속 대기 상태로 유지됨
		class NetWorkThread extends Thread {
			NetWorkSocket socket;							// 서버 또는 클라이언트
			NetWorkThread(NetWorkSocket socket) {
				this.socket = socket;
			}
			public void run() {
				System.out.println("!");
				String turnInfo = socket.ReceiveData();		// 소켓으로부터 데이터 받아옴
				if(turnInfo.length() <= 7)					// 첫 턴이 클라이언트일 시 서버에서 턴 정보가 완전히 받아지지 않는 오류 존재
				{											// 그 오류를 해결하기 위한 코드
					turnInfo += socket.ReceiveData();		// 턴 정보가 완전히 받아지지 않을 시 남은 정보를 이어서 받음
				}
				updateTurn(turnInfo);						// 턴 정보로 턴 업데이트. 상대방의 행동에 의한 게임 상황을 적용하기 위함
				return;
			}
		}
		
		
		class MyMouseListener implements MouseListener, MouseMotionListener {

			MyMouseListener() {
				availablePoints = GM.GetAvailablePoint();	// 이동 가능한 위치 받아옴
				turn = GM.GetTurn();						// 현재 턴 받아옴
			}
			
			// 마우스가 눌렸을 때
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				turn = GM.GetTurn();
				String msg = "";
				String prevPosition;
				boolean game_over = false;
				
				if(GM.GetTurn() == true)
					prevPosition = Integer.toString(GM.p1.getPoint().x) + " " + Integer.toString(GM.p1.getPoint().y);
				else
					prevPosition = Integer.toString(GM.p2.getPoint().x) + " " + Integer.toString(GM.p2.getPoint().y);
				if((gameMode == GAME_MODE.NETWORKHOST && turn == false) || (gameMode == GAME_MODE.NETWORKGUEST && turn == true))
				{	// 네트워크 게임이면서 나의 턴이 아닐 시
					// 아무것도 안함
				}
				else	// 다른 모든 경우
				{
					availablePoints = GM.GetAvailablePoint();	// 이동 가능한 위치 받아옴
					
					if(GM.GetPutMode() == true)
					{
						if(GM.PutWallNum(wallNum, turn))
						{
							GM.SetTurnCount();
							GM.NextTurn();
							turn = GM.GetTurn();
							buttonPanel.checkbtAvailable();
							infoPanel.UpdateWallLabel();
							infoPanel.UpdateTurnLabel();
							repaint();
							msg = "Wall " + wallNum + " ";
							wallNum = -1;
							wall_sound.play();
						}
					}
					else
					{
						// 이동 가능한 위치인지 확인
						for(int i = 0; i < 4; i++)
						{
							// 이동 불가능한 위치는 (9, 9)로 저장되어 있음
							if(availablePoints[i].x != 9 && availablePoints[i].y != 9)
							{
								// 마우스가 칸 안에 들어있을 때
								if(p.x >= availablePoints[i].x*38 && p.x <= (availablePoints[i].x + 1) *38 &&
										p.y >= availablePoints[i].y*38 && p.y <= (availablePoints[i].y + 1) *38)
								{
									// 플레이어 위치 정보 변경
									if(turn == true)
										GM.p1.movePlayer(new Point(availablePoints[i].x, availablePoints[i].y));
									else
										GM.p2.movePlayer(new Point(availablePoints[i].x, availablePoints[i].y));
									
									// 화면 다시 그리기
									repaint();
									
									// 턴 번호 설정 및 게임 기록 일부 삭제
									GM.SetTurnCount();
									
									// 다음 턴으로 넘김
									GM.NextTurn();
									buttonPanel.checkbtAvailable();
									
									turn = GM.GetTurn();
									infoPanel.UpdateTurnLabel();
									msg = "Move " + availablePoints[i].x + " " + availablePoints[i].y + " from " + prevPosition;
									availablePoints = GM.GetAvailablePoint();
									
									move_sound.play();
									
									// 이동은 한번만 하므로 또 반복할 필요 없음
									break;
								}
							}
						}
					}
					//승리 여부 검사				
					if(GM.GameOver())
					{
						GM.Record(msg);	// 정보 저장
						if(gameMode == GAME_MODE.NETWORKHOST || gameMode == GAME_MODE.NETWORKGUEST)
						{
							GM.SendData(msg);				// 데이터 보내기
							GM.GetSocket().CloseSocket();	// 소켓 닫기
							game_over = true;
						}
						GM.Save(GM.GetDate(), true);
						game_over = true;
						if(turn == true)
						{
							JOptionPane.showMessageDialog(null, GM.p2.getPlayerName() + " 승리", "게임 결과", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
							new Menu();
							Game.dispose();
						}
						else
						{
							JOptionPane.showMessageDialog(null, GM.p1.getPlayerName() + " 승리", "게임 결과", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
							new Menu();
							Game.dispose();
						}
						
					}
					if(!msg.isEmpty())
					{
						if(gameMode == GAME_MODE.SINGLE)
						{
							if(turn == false && game_over == false) // AI 턴이고 게임이 끝나지 않았을 경우
							{										// == 내가 돌을 두고 난 후
								GM.Record(msg);						// 턴 정보 기록
								msg = GM.p2.AIBehavior();			// AI의 행동 받아옴
								updateTurn(msg);					// AI의 행동 실행
								GM.Record(msg);						// AI의 행동 기록
													// 턴 수와 게임 로그를 업데이트
							}
						}
						else if((gameMode == GAME_MODE.NETWORKHOST || gameMode == GAME_MODE.NETWORKGUEST) && !game_over)
						{
							GM.SendData(msg.substring(0, 9));
							netThread = new NetWorkThread(GM.GetSocket());
							netThread.start();
							GM.Record(msg);
						}
						else
						{
							GM.Record(msg);
						}
					}
				}
			}
			
			
			// 마우스가 움직였을 때
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();

				if(GM.GetPutMode() == true)
				{
					wallNum = 100 + (p.y - 11)/40*10 + (p.x - 11)/40;
					if((p.x+20) % 40 <= 20 && (p.y+20) % 40 >= 20) wallNum += 100;
					
					repaint();
				}
			}
			public void mouseReleased(MouseEvent e) { }
			public void mouseEntered(MouseEvent e) { }
			public void mouseExited(MouseEvent e) { }
			public void mouseClicked(MouseEvent e) { }
			public void mouseDragged(MouseEvent e) { }
		}
		
		// 턴 이동시 호출되는 함수
		public void moveTurn(String turnInfo, boolean moveToPrev) {
			if(moveToPrev == true)
			{
				if(turnInfo.substring(6, 10).equals("Wall"))	// 벽 생성
				{
					int wallNum = Integer.valueOf(turnInfo.substring(11, 14));
					if(turnInfo.substring(0, 5).equals("Black"))
						GM.DeleteWallNum(wallNum, true);
					else
						GM.DeleteWallNum(wallNum, false);
					infoPanel.UpdateWallLabel();
				}
				else if(turnInfo.substring(6, 10).equals("Move"))	// 이동
				{
					int moveX = Integer.valueOf(turnInfo.substring(11,12));
					int moveY = Integer.valueOf(turnInfo.substring(13,14));
					int fromX = Integer.valueOf(turnInfo.substring(20,21));
					int fromY = Integer.valueOf(turnInfo.substring(22,23));
					if(turnInfo.substring(0, 5).equals("Black"))
						GM.p1.movePlayer(new Point(fromX, fromY));
					else
						GM.p2.movePlayer(new Point(fromX, fromY));
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
				}
			}
			GM.ChangeTurn();
			buttonPanel.checkbtAvailable();
			infoPanel.UpdateTurnLabel();
			infoPanel.UpdateWallLabel();
			availablePoints = GM.GetAvailablePoint();
			turn = GM.GetTurn();
			repaint();
		}
		
		// NetWorkThread에서 호출, 상대방의 턴 정보를 받아와서 게임 업데이트
		public void updateTurn(String turnInfo) {
			if(turnInfo.substring(0,4).equals("Wall"))		// 벽 생성
			{
				int wallNum = Integer.valueOf(turnInfo.substring(5, 8));
				GM.PutWallNum(wallNum, turn);
				infoPanel.UpdateWallLabel();
			}
			else if(turnInfo.substring(0,4).equals("Move"))	// 이동
			{
				int moveX = Integer.valueOf(turnInfo.substring(5,6));
				int moveY = Integer.valueOf(turnInfo.substring(7,8));
				if(gameMode == GAME_MODE.NETWORKGUEST)
					GM.p1.movePlayer(new Point(moveX, moveY));
				else
					GM.p2.movePlayer(new Point(moveX, moveY));
			}
			
			if(GM.GameOver())	// 게임이 끝났는지 검사
			{
				repaint();
				if(turn == true)
				{
					JOptionPane.showMessageDialog(null, GM.p1.getPlayerName() + " 승리", "게임 결과", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
					GM.Save(GM.GetDate(), true);
					new Menu();
					Game.dispose();
				}
				else
				{
					JOptionPane.showMessageDialog(null, GM.p2.getPlayerName() + " 승리", "게임 결과", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
					GM.Save(GM.GetDate(), true);
					new Menu();
					Game.dispose();	
				}
				if(gameMode == GAME_MODE.NETWORKGUEST || gameMode == GAME_MODE.NETWORKHOST)
					GM.GetSocket().CloseSocket();
			}
			else if(turnInfo.equals("Give up"))	// 상대가 항복했는지 검사
			{
				if(turn == false)
				{
					JOptionPane.showMessageDialog(null, GM.p1.getPlayerName() + " 승리", "게임 결과", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
					GM.Save(GM.GetDate(), true);
					new Menu();
					Game.dispose();
				}
				else
				{
					JOptionPane.showMessageDialog(null, GM.p2.getPlayerName() + " 승리", "게임 결과", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
					GM.Save(GM.GetDate(), true);
					new Menu();
					Game.dispose();
				}
				if(gameMode == GAME_MODE.NETWORKGUEST || gameMode == GAME_MODE.NETWORKHOST)
					GM.GetSocket().CloseSocket();
			}
			
			GM.NextTurn();
			buttonPanel.checkbtAvailable();
			infoPanel.UpdateTurnLabel();
			availablePoints = GM.GetAvailablePoint();
			turn = GM.GetTurn();
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
			
			// 이동 가능한 위치 그리기
			if(GM.GetPutMode() == false)
			{
				drawAvailablePoint(g);
			}
			else
			{
				drawAvailableWall(g);
			}
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
		
		// 이동 가능한 위치를 표시해주는 함수
		public void drawAvailablePoint(Graphics g) {
			g.setColor(Color.LIGHT_GRAY);
			Point[] availablePoints = GM.GetAvailablePoint();
			
			for(int i = 0; i < 9; i++) {
				for(int j = 0; j < 9; j++) {
					for(int k = 0; k < 4; k++) {
						if(availablePoints[k].x != 9 && availablePoints[k].y != 9)
							if(i == availablePoints[k].y && j == availablePoints[k].x)
								g.fillOval(38*j, 38*i, 38, 38);
					}
				}
			}
		}
		
		// 설치 가능한 벽을 표시해주는 함수
		public void drawAvailableWall(Graphics g) {
			g.setColor(new Color(0, 0, 255, 100));
			
			// 현재 마우스 위치에 해당하는 벽의 번호를 불러와 저장
			boolean isVertical;
			int vWallNum = wallNum%10;
			int hWallNum = (wallNum/10)%10;
			boolean wallPoint[][][] = GM.GetWallPoint();
			
			if(wallNum / 100 == 1)
				isVertical = true;
			else isVertical = false;
			
			if(vWallNum == -1 || hWallNum == -1)
				return;
			else
			{
				vWallNum = ((vWallNum > 7) ? 7 : vWallNum);
				hWallNum = ((hWallNum > 7) ? 7 : hWallNum);
				for(int i = 0; i < 9; i++) {
					for(int j = 0; j < 9; j++) {
						if(isVertical)
						{
							if(j == vWallNum && i == hWallNum && !wallPoint[0][j][i] &&
									!wallPoint[0][(j-1) < 0 ? 0 : (j-1)][i] && !wallPoint[0][(j+1) > 7 ? 7 : (j+1)][i] && // 양 옆으로 겹치게 세울 수 없는 벽
									!wallPoint[1][j][i]) // 세로로 겹치게 세울 수 없는 벽
							{
								g.fillRect(38*j, 38*(i+1) - 2, 76, 5); // 가로벽
								break;
							}
						}
						else
						{
							if(j == vWallNum && i == hWallNum && !wallPoint[1][j][i] &&
									!wallPoint[1][j][(i-1) < 0 ? 0 : (i-1)] && !wallPoint[1][j][(i+1) > 7 ? 7 : (i+1)] && // 위아래로 겹치게 세울 수 없는 벽
									!wallPoint[0][j][i]) // 가로로 겹치게 세울 수 없는 벽
							{
								g.fillRect(38*(j+1) - 2, 38*i, 5, 76); // 세로벽
								break;
							}
						}
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

