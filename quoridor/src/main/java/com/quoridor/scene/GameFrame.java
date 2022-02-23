package com.quoridor.scene;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import javax.swing.*;

import com.quoridor.enums.GAME_MODE;
import com.quoridor.game.manager.GameManager;
import com.quoridor.game.object.GameAction;
import com.quoridor.game.object.GameState;
import com.quoridor.game.object.GameThread;
import com.quoridor.game.object.ReplayThread;


/*
 * 게임이 진행되는 프레임과 그 구성요소
 */

/*
 * buttonPanel
 * gamePanel
 * InfoPanel
 */

// 게임 화면 구성
public class GameFrame extends JFrame{
	private GameManager gameManager;								// 게임을 종합적으로 관리하는 게임 매니저
	private GamePanel gamePanel;									// 게임 화면을 출력하는 패널
	private ButtonPanel buttonPanel;								// 버튼을 출력하는 패널
	private InfoPanel infoPanel;									// 턴 정보를 출력하는 패널
	
	public GameFrame(GameManager gm, BlockingQueue<GameAction> queue) {
		this.gameManager = gm;
		
		/* 화면 구성 */
		setTitle("game");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		buttonPanel = new ButtonPanel(gameManager, this);
		infoPanel = new InfoPanel(gameManager);
		gamePanel = new GamePanel(this, gameManager, queue);
		
		// 패널들 합치기
		contentPane.setLayout(new BorderLayout());
		contentPane.setBackground(new Color(150, 50, 0));
		
		contentPane.add(buttonPanel, BorderLayout.NORTH);
		contentPane.add(gamePanel, BorderLayout.CENTER);
		contentPane.add(infoPanel, BorderLayout.SOUTH);
		
		setSize(40*9, 40*10+100);
		setVisible(true);
	}

	public void runThread() {
		Thread gamePanelThread = new Thread(gamePanel);
		gamePanelThread.setDaemon(true);
		gamePanelThread.start();
	}

	public void OpenMenuFrame() {
		new MenuFrame();
		this.dispose();
	}
	
	// 게임 화면에서 상단의 버튼 패널
	class ButtonPanel extends JPanel {
		private JButton wallButton = new JButton("장애물");
		private JButton prevButton = new JButton("이전");
		private JButton nextButton = new JButton("다음");
		private JButton saveButton = new JButton("저장");
		private JButton giveupButton = new JButton("기권");

		// 리플레이용 버튼
		private JButton playButton = new JButton("중단");
		private JButton exitButton = new JButton("종료");

		private JButton[] menuButtons = new JButton[5];
		private GameManager gameManager;
		private GameThread gameThread;
		private GameState gameState;
		private GAME_MODE gameMode;
		private GameFrame gameFrame;
		
		
		ButtonPanel(GameManager gameManager, GameFrame gameFrame) {
			this.gameFrame = gameFrame;
			this.gameManager = gameManager;
			this.gameThread = gameManager.getGameThread();
			this.gameState = gameManager.getGameState();
			this.gameMode = gameState.getGameMode();
			setBackground(new Color(150, 50, 0));
			
			// 메뉴 패널 버튼 배치
			setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			
			int numButtons = 0;
			BtnListener listener = new BtnListener();
			
			if(gameMode == GAME_MODE.SINGLE) {
				numButtons = 5;

				menuButtons[0] = wallButton;
				menuButtons[1] = prevButton;
				menuButtons[2] = nextButton;
				menuButtons[3] = saveButton;
				menuButtons[4] = giveupButton;
			} else if (gameMode == GAME_MODE.MULTY) {
				numButtons = 3;

				menuButtons[0] = wallButton;
				menuButtons[1] = saveButton;
				menuButtons[2] = giveupButton;
			} else if (gameMode == GAME_MODE.NETWORK_HOST || gameMode == GAME_MODE.NETWORK_GUEST) {
				numButtons = 2;

				menuButtons[0] = wallButton;
				menuButtons[1] = giveupButton;
			} else if(gameMode == GAME_MODE.REPLAY) {
				numButtons = 4;

				menuButtons[0] = prevButton;
				menuButtons[1] = nextButton;
				menuButtons[2] = playButton;
				menuButtons[3] = exitButton;
			}
			
			for(int i = 0; i < numButtons; i++) {
				menuButtons[i].addActionListener(listener);
				this.add(menuButtons[i]); 
			}

			checkbtAvailable();
		}
		
		class BtnListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				Object clickedObject = e.getSource();
				if(clickedObject == wallButton) {			// 장애물
					gameState.ChangeActionMode();
				} else if(clickedObject == prevButton) {		// prev
					gameState.MoveToPrevTurn();
				} else if(clickedObject == nextButton) {		// next
					gameState.MoveToNextTurn();
				} else if(clickedObject == saveButton) {		// 저장
					String defaultFileName;
					defaultFileName = gameManager.getSaveFileName();
					String fileName= JOptionPane.showInputDialog("저장할 파일명을 입력하세요.", defaultFileName);
					if(fileName != null) {
						gameManager.Save(fileName, false);
						gameManager.getGameThread().interrupt();
						gameFrame.OpenMenuFrame();
					}
				} else if(clickedObject == giveupButton) {		// 기권
					int give_up= JOptionPane.showConfirmDialog(null, "정말로 기권하시겠습니까?", "알림", JOptionPane.YES_NO_OPTION);
					if(give_up == JOptionPane.YES_OPTION) {
						gameState.GiveUp();
						gameManager.getGameThread().GiveUp();
					}
				} else if(clickedObject == playButton) {
					if(((ReplayThread)gameThread).isPause()) {
						((ReplayThread)gameThread).Play();
						playButton.setText("중단");
					} else {
						((ReplayThread)gameThread).Pause();
						playButton.setText("재생");
					}
				} else if(clickedObject == exitButton) {
					gameManager.getGameThread().interrupt();
					gameFrame.OpenMenuFrame();
				}
			}
		}
		
		public void checkbtAvailable() {
			if(gameMode == GAME_MODE.NETWORK_HOST || gameMode == GAME_MODE.NETWORK_GUEST) {	// 네트워크 호스트일 때
				if(gameManager.isLocalPlayerTurn()) { // 본인의 차례일 때
					if(gameState.getCurrentPlayer().getNumRemainWalls() == 0) {			// 설치 가능한 벽의 수가 0일 때
						wallButton.setEnabled(false);	// 장애물 버튼 비활성화
					} else {
						wallButton.setEnabled(true);	// 장애물 버튼 활성화
					}
					giveupButton.setEnabled(true);		// 기권 버튼 활성화
				} else { // 상대방의 차례일 때
					wallButton.setEnabled(false);
					giveupButton.setEnabled(false);
				}
			} else {								// 1인 또는 2인 모드, REPLAY 모드(장애물 버튼, 항복 버튼 없음)
				if(gameState.getCurrentPlayer().isWallRemains()) {
					wallButton.setEnabled(true);
				} else {
					wallButton.setEnabled(false);
				}
				giveupButton.setEnabled(true);

				if(gameState.CanMoveToPrev()) {
					prevButton.setEnabled(true);
				} else {
					prevButton.setEnabled(false);
				}
	
				if(gameState.CanMoveToNext()) {
					nextButton.setEnabled(true);
				} else {
					nextButton.setEnabled(false);
				}
			}
		}
	}

	// 게임이 진행되는 패널
	class GamePanel extends JPanel implements Runnable{
		private GameFrame gameFrame;

		private GameState gameState;

		private Point wallPoint = new Point(0, 0);
		private boolean verticalWall = false;

		private BlockingQueue<GameAction> queue;
		
		GamePanel(GameFrame gameFrame, GameManager gameManager, BlockingQueue<GameAction> queue) {
			this.gameFrame = gameFrame;
			this.gameState = gameManager.getGameState();
			this.queue = queue;
			
			MyMouseListener listener = new MyMouseListener();
			addMouseListener(listener);
			addMouseMotionListener(listener);
		}
	
		public void run() {
			while(!gameState.gameOver) {
				updatePanel();
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			updatePanel();
			ShowGameOverPane();
			gameManager.Save(gameManager.getSaveFileName(), true);
			gameFrame.OpenMenuFrame();
		}

		public void ShowGameOverPane() {
			String textToShow = gameState.getOpponentPlayer().getPlayerName() + "(" + gameState.getOpponentPlayer().getPlayerColor().toString() + ") 승리";
			String paneTitle = "게임 결과";
			JOptionPane.showMessageDialog(null, textToShow, paneTitle, JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
		}

		class MyMouseListener implements MouseListener, MouseMotionListener {
			// 마우스가 눌렸을 때
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				
				if(queue != null) {
					queue.clear();
					if(gameState.getActionMode().isWallMode()) {
						queue.add(new GameAction(new Point(wallPoint), verticalWall));
					} else {
						queue.add(new GameAction(new Point(p.x / 38, p.y / 38)));
					}
				}
			}
			
			// 마우스가 움직였을 때
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();

				if(gameState.getActionMode().isWallMode())
				{
					wallPoint.y = (p.y - 11) / 40;
					wallPoint.x = (p.x - 11) / 40;

					if((p.y+20) % 40 >= 20 && (p.x+20) % 40 <= 20) {
						verticalWall = true;
					} else {
						verticalWall = false;
					}
				}
			}
			public void mouseReleased(MouseEvent e) { }
			public void mouseEntered(MouseEvent e) { }
			public void mouseExited(MouseEvent e) { }
			public void mouseClicked(MouseEvent e) { }
			public void mouseDragged(MouseEvent e) { }
		}

		public void updatePanel() {
			buttonPanel.checkbtAvailable();
			infoPanel.UpdateTurnLabel();
			infoPanel.UpdateWallLabel();
			repaint();
		}
		
		// 게임 판 그리기
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			drawBoard(g);
			drawPlayer(g);
			drawWall(g);
			
			// 이동 가능한 위치 그리기
			if(gameState.getActionMode().isWallMode()) {
				drawAvailableWall(g);
			} else {
				drawAvailablePoint(g);
			}
		}

		public void drawBoard(Graphics g) {
			setBackground(new Color(150, 50, 0));
			// 선 그리기
			g.setColor(Color.BLACK);
			for(int i = 1; i < 10; i++) {
				g.drawLine(0, 38*i, 38*9, 38*i); // 가로선
			}
			for(int i = 1; i < 9; i++) {
				g.drawLine(38*i, 0, 38*i, 38*9); // 세로선
			}
		}
		
		// 플레이어 그리기
		public void drawPlayer(Graphics g) {
			g.setColor(Color.BLACK);
			g.fillOval(gameState.getBlackPlayer().getPoint().x*38, gameState.getBlackPlayer().getPoint().y*38, 38, 38);
			g.setColor(Color.WHITE);
			g.fillOval(gameState.getWhitePlayer().getPoint().x*38, gameState.getWhitePlayer().getPoint().y*38, 38, 38);
		}
		
		// 벽 그리기
		public void drawWall(Graphics g) {
			g.setColor(Color.YELLOW);
			
			boolean[][][] WallPoints = gameState.getWalls();
			for(int i = 0; i < 8; i++) {
				for(int j = 0; j < 8; j++) {
					if(WallPoints[1][i][j]) {
						g.fillRect(38*(j+1) - 2, 38*i, 3, 76); // 세로벽
					}
					if(WallPoints[0][i][j]) {
						g.fillRect(38*j, 38*(i+1) - 2, 76, 3); // 가로벽
					}
				}
			}
		}
		
		// 이동 가능한 위치를 표시해주는 함수
		public void drawAvailablePoint(Graphics g) {
			g.setColor(Color.LIGHT_GRAY);
			Vector<Point> availablePoints = gameState.getAvailableMoves();
			
			for(int i=0; i < availablePoints.size(); i++) {
				Point point = availablePoints.get(i);
				g.fillOval(point.x * 38, point.y * 38, 38, 38);
			}
		}
		
		// 설치 가능한 벽을 표시해주는 함수
		public void drawAvailableWall(Graphics g) {
			g.setColor(new Color(0, 0, 255, 100));
			
			// 현재 마우스 위치에 해당하는 벽의 번호를 불러와 저장
			int yPos = wallPoint.y;
			int xPos = wallPoint.x;

			if(yPos >= 0 && yPos <= 7 && xPos >= 0 && xPos <= 7) {
				if(gameState.isAvailableWall(new GameAction(wallPoint, verticalWall))) {
					if(verticalWall) {
						g.fillRect(38 * (xPos + 1) - 2, 38 * yPos, 5, 76);
					} else {
						g.fillRect(38 * xPos, 38 * (yPos + 1) - 2, 76, 5);
					}
				}
			}
		}
	}

	// 게임 하단에 정보를 나타내주는 패널
	class InfoPanel extends JPanel {
		private GameState gameState;
		JLabel wallLabelBlackPlayer;
		JLabel wallLabelWhitePlayer;
		JLabel turnLabel;
		InfoPanel(GameManager GM) {
			setBackground(new Color(150, 50, 0));
			
			this.gameState = GM.getGameState(); 
			setLayout(new BorderLayout());
			
			// 검은색 Walls 라벨
			wallLabelBlackPlayer = new JLabel("Walls: " + gameState.getBlackPlayer().getNumRemainWalls());
			wallLabelBlackPlayer.setFont(new Font("돋움", Font.ITALIC, 15));
			wallLabelBlackPlayer.setForeground(Color.BLACK);
			wallLabelBlackPlayer.setSize(100, 30);

			// 하얀색 Walls 라벨
			wallLabelWhitePlayer = new JLabel("Walls: " + gameState.getWhitePlayer().getNumRemainWalls());
			wallLabelWhitePlayer.setFont(new Font("돋움", Font.ITALIC, 15));
			wallLabelWhitePlayer.setForeground(Color.WHITE);
			wallLabelWhitePlayer.setSize(100, 30);
			
			// 현재 턴 라벨
			turnLabel = new JLabel(gameState.getCurrentPlayer().getPlayerName());
			turnLabel.setForeground(gameState.getCurrentPlayer().getPlayerColor().getColor());

			turnLabel.setFont(new Font("돋움", Font.ITALIC | Font.BOLD, 25));
			turnLabel.setSize(160, 50);
			turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			// 정보 패널 배치
			this.add(wallLabelBlackPlayer, BorderLayout.WEST);
			this.add(turnLabel, BorderLayout.CENTER);
			this.add(wallLabelWhitePlayer, BorderLayout.EAST);
		}

		public void UpdateTurnLabel() {
			turnLabel.setText(gameState.getCurrentPlayer().getPlayerName());
			turnLabel.setForeground(gameState.getCurrentPlayer().getPlayerColor().getColor());
		}

		public void UpdateWallLabel() {
			wallLabelBlackPlayer.setText("Walls: " + gameState.getBlackPlayer().getNumRemainWalls());
			wallLabelWhitePlayer.setText("Walls: " + gameState.getWhitePlayer().getNumRemainWalls());
		}
	}
}

