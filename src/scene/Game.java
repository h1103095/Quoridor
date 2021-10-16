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
 * ������ ����Ǵ� �����Ӱ� �� �������
 */

/*
 * buttonPanel
 * gamePanel
 * InfoPanel
 * �� ������
 */

// ���� ȭ�� ����
public class Game extends JFrame{
	private GameManager GM;												// ������ ���������� �����ϴ� ���� �Ŵ���
	private GamePanel gamePanel;										// ���� ȭ���� ����ϴ� �г�
	private ButtonPanel buttonPanel;									// ��ư�� ����ϴ� �г�
	private InfoPanel infoPanel;										// �� ������ ����ϴ� �г�
	private GameSound move_sound = new GameSound("sounds/move.wav");	// �̵� ����
	private GameSound wall_sound = new GameSound("sounds/wall.wav");	// �� ����
	
	public Game(GameManager gm) {
		this.GM = gm;
		
		/* ȭ�� ���� */
		setTitle("game");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		buttonPanel = new ButtonPanel(GM, this);
		infoPanel = new InfoPanel(GM);
		gamePanel = new GamePanel(GM, this);
		
		// �гε� ��ġ��
		contentPane.setLayout(new BorderLayout());
		contentPane.setBackground(new Color(150, 50, 0));
		
		contentPane.add(buttonPanel, BorderLayout.NORTH);
		contentPane.add(gamePanel, BorderLayout.CENTER);
		contentPane.add(infoPanel, BorderLayout.SOUTH);
		
		setSize(40*9, 40*10+100);
		setVisible(true);
	}
	
	// ���� �� ����
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
			        float volume = (float)((GM.GetVolume()/2) + 10) / 20;					// ���� ����
			        float range = control.getMaximum() - control.getMinimum();	// ���� ���
			        float result = (range * volume) + control.getMinimum();		// ���� ������
			        control.setValue(result);									// ���� ����
					clip.start();												// �Ҹ� ���
				} catch(Exception e)
				{
					System.out.println("���� ��� ����");
				}
			}
		}
			
	}
	
	// ���� ȭ�鿡�� ����� ��ư �г�
	class ButtonPanel extends JPanel {
		JButton[] menuButtons = new JButton[5];
		GameManager GM;
		GAME_MODE gameMode;
		private Frame Game; // â�� �ݱ� ���� ����
		
		ButtonPanel(GameManager gm, Frame game) {
			this.Game = game;
			this.GM = gm;
			this.gameMode = GM.GetGameMode();
			setBackground(new Color(150, 50, 0));
			
			// �޴� �г� ��ư ��ġ
			setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			String[] menubtStrings = {"��ֹ�", "Back", "Next", "����", "���" };
			
			BtnListener listener = new BtnListener();
			
			for(int i = 0; i < 5; i++) {
				menuButtons[i] = new JButton(menubtStrings[i]);
				menuButtons[i].addActionListener(listener);
				this.add(menuButtons[i]); 
			}
			
			// ��Ʈ��ũ ������ ��
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
				if(e.getSource() == menuButtons[0]) {			// ��ֹ�
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
				}else if(e.getSource() == menuButtons[3]) {		// ����
					String defaultFileName;
					defaultFileName = GM.GetFileName();
					String fileName= JOptionPane.showInputDialog("������ ���ϸ��� �Է��ϼ���.", defaultFileName);
					if(fileName != null)
					{
						GM.Save(fileName, false);
						new Menu();
						Game.dispose();
					}
				}else if(e.getSource() == menuButtons[4]) {		// ���
					int give_up= JOptionPane.showConfirmDialog(null, "������ ����Ͻðڽ��ϱ�?", "���", JOptionPane.YES_NO_OPTION);
					if(give_up == JOptionPane.YES_OPTION)
					{
						if(gameMode == GAME_MODE.NETWORKHOST || gameMode == GAME_MODE.NETWORKGUEST)
							GM.SendData("Give up");
						if(GM.GetTurn() == false)
						{
							JOptionPane.showMessageDialog(null, GM.p1.getPlayerName() + " �¸�", "���� ���", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
							new Menu();
							GM.Save(GM.GetDate(), true);
							Game.dispose();
						}
						else
						{
							JOptionPane.showMessageDialog(null, GM.p2.getPlayerName() + " �¸�", "���� ���", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
							new Menu();
							GM.Save(GM.GetDate(), true);
							Game.dispose();
						}
					}
				}
			}
		}

		
		public void checkbtAvailable() {
			if(GM.GetTurn() == true)						// ���� ��
			{
				if(gameMode == GAME_MODE.NETWORKHOST)		// ��Ʈ��ũ ȣ��Ʈ�� ��
				{
					if(GM.p1.getWallNum() == 0)				// ��ġ ������ ���� ���� 0�� ��
						menuButtons[0].setEnabled(false);	// ��ֹ� ��ư ��Ȱ��ȭ
					else
						menuButtons[0].setEnabled(true);	// ��ֹ� ��ư Ȱ��ȭ
					menuButtons[4].setEnabled(true);		// ��� ��ư Ȱ��ȭ
				}
				else if(gameMode == GAME_MODE.NETWORKGUEST)
				{
					menuButtons[0].setEnabled(false);		// ��ֹ� ��ư ��Ȱ��ȭ
					menuButtons[4].setEnabled(false);		// ��� ��ư ��Ȱ��ȭ
				}
				
				else										// 1�� �Ǵ� 2�� ���
				{
					if(GM.p1.getWallNum() == 0)				
						menuButtons[0].setEnabled(false);
					else
						menuButtons[0].setEnabled(true);
					if(gameMode == GAME_MODE.SINGLE)
					{
						if(GM.GetSelectTurn() <= 2)
							menuButtons[1].setEnabled(false);	// ���� ��ư ��Ȱ��ȭ
						else
							menuButtons[1].setEnabled(true);	// ���� ��ư Ȱ��ȭ
						if(GM.GetTurnCount() > GM.GetSelectTurn())
							menuButtons[2].setEnabled(true);	// ���� ��ư Ȱ��ȭ
						else
							menuButtons[2].setEnabled(false);	// ���� ��ư ��Ȱ��ȭ
					}
				}
			}
			else											// �Ͼ� ��
			{
				if(gameMode == GAME_MODE.NETWORKGUEST)		// ��Ʈ��ũ ����� ��
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
					if(GM.p2.getWallNum() == 0)				// 1�� �Ǵ� 2�� ���
						menuButtons[0].setEnabled(false);
					else
						menuButtons[0].setEnabled(true);
					menuButtons[1].setEnabled(false);		// ���� ��ư ��Ȱ��ȭ
					menuButtons[2].setEnabled(false);		// ���� ��ư ��Ȱ��ȭ
				}
			}
		}
	}

	// ������ ����Ǵ� �г�
	class GamePanel extends JPanel{
		GameManager GM;
		NetWorkThread netThread;
		GAME_MODE gameMode;
		Point[] availablePoints;
		private Frame Game;
		boolean turn;
		int wallNum = -1; // ���� (100 ~ 288) ���� �ڸ� ���� ���� �ڸ� ���� ��ǥ���� ��ġ ��Ÿ��, 8���� ũ�� ����. 8�� �� 7�� �ٲپ� ���
		
		GamePanel(GameManager gm,  Frame game) {
			this.GM = gm; // ���� �޴��� �ҷ���
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
			else if(gameMode == GAME_MODE.NETWORKHOST || gameMode == GAME_MODE.NETWORKGUEST) // ��Ʈ��ũ ������ ��
			{
				netThread = new NetWorkThread(GM.GetSocket());							// ��� ���� ������ ����
				if((gameMode == GAME_MODE.NETWORKHOST && GM.GetTurn() == false) || (gameMode == GAME_MODE.NETWORKGUEST && GM.GetTurn() == true))
				{																			// ��Ʈ��ũ ���ӿ��� ���� �� �ڽ��� ���� �ƴ� ��
					netThread.start();														// ���κ��� ����� �ൿ �޾ƿ�
					availablePoints = GM.GetAvailablePoint();								// �̵� ������ ��ġ ǥ��. �� ���¿����� �̵� ������ ��ġ�� ǥ�õ��� �ʰ� �ϱ� ����
					buttonPanel.checkbtAvailable();											// ��ư�� ���� ���� ����
				}
			}
		}
		
		// �����͸� �ޱ� ���� ������
		// �����带 �������� ���� �� ���α׷��� ��� ��� ���·� ������
		class NetWorkThread extends Thread {
			NetWorkSocket socket;							// ���� �Ǵ� Ŭ���̾�Ʈ
			NetWorkThread(NetWorkSocket socket) {
				this.socket = socket;
			}
			public void run() {
				System.out.println("!");
				String turnInfo = socket.ReceiveData();		// �������κ��� ������ �޾ƿ�
				if(turnInfo.length() <= 7)					// ù ���� Ŭ���̾�Ʈ�� �� �������� �� ������ ������ �޾����� �ʴ� ���� ����
				{											// �� ������ �ذ��ϱ� ���� �ڵ�
					turnInfo += socket.ReceiveData();		// �� ������ ������ �޾����� ���� �� ���� ������ �̾ ����
				}
				updateTurn(turnInfo);						// �� ������ �� ������Ʈ. ������ �ൿ�� ���� ���� ��Ȳ�� �����ϱ� ����
				return;
			}
		}
		
		
		class MyMouseListener implements MouseListener, MouseMotionListener {

			MyMouseListener() {
				availablePoints = GM.GetAvailablePoint();	// �̵� ������ ��ġ �޾ƿ�
				turn = GM.GetTurn();						// ���� �� �޾ƿ�
			}
			
			// ���콺�� ������ ��
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
				{	// ��Ʈ��ũ �����̸鼭 ���� ���� �ƴ� ��
					// �ƹ��͵� ����
				}
				else	// �ٸ� ��� ���
				{
					availablePoints = GM.GetAvailablePoint();	// �̵� ������ ��ġ �޾ƿ�
					
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
						// �̵� ������ ��ġ���� Ȯ��
						for(int i = 0; i < 4; i++)
						{
							// �̵� �Ұ����� ��ġ�� (9, 9)�� ����Ǿ� ����
							if(availablePoints[i].x != 9 && availablePoints[i].y != 9)
							{
								// ���콺�� ĭ �ȿ� ������� ��
								if(p.x >= availablePoints[i].x*38 && p.x <= (availablePoints[i].x + 1) *38 &&
										p.y >= availablePoints[i].y*38 && p.y <= (availablePoints[i].y + 1) *38)
								{
									// �÷��̾� ��ġ ���� ����
									if(turn == true)
										GM.p1.movePlayer(new Point(availablePoints[i].x, availablePoints[i].y));
									else
										GM.p2.movePlayer(new Point(availablePoints[i].x, availablePoints[i].y));
									
									// ȭ�� �ٽ� �׸���
									repaint();
									
									// �� ��ȣ ���� �� ���� ��� �Ϻ� ����
									GM.SetTurnCount();
									
									// ���� ������ �ѱ�
									GM.NextTurn();
									buttonPanel.checkbtAvailable();
									
									turn = GM.GetTurn();
									infoPanel.UpdateTurnLabel();
									msg = "Move " + availablePoints[i].x + " " + availablePoints[i].y + " from " + prevPosition;
									availablePoints = GM.GetAvailablePoint();
									
									move_sound.play();
									
									// �̵��� �ѹ��� �ϹǷ� �� �ݺ��� �ʿ� ����
									break;
								}
							}
						}
					}
					//�¸� ���� �˻�				
					if(GM.GameOver())
					{
						GM.Record(msg);	// ���� ����
						if(gameMode == GAME_MODE.NETWORKHOST || gameMode == GAME_MODE.NETWORKGUEST)
						{
							GM.SendData(msg);				// ������ ������
							GM.GetSocket().CloseSocket();	// ���� �ݱ�
							game_over = true;
						}
						GM.Save(GM.GetDate(), true);
						game_over = true;
						if(turn == true)
						{
							JOptionPane.showMessageDialog(null, GM.p2.getPlayerName() + " �¸�", "���� ���", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
							new Menu();
							Game.dispose();
						}
						else
						{
							JOptionPane.showMessageDialog(null, GM.p1.getPlayerName() + " �¸�", "���� ���", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
							new Menu();
							Game.dispose();
						}
						
					}
					if(!msg.isEmpty())
					{
						if(gameMode == GAME_MODE.SINGLE)
						{
							if(turn == false && game_over == false) // AI ���̰� ������ ������ �ʾ��� ���
							{										// == ���� ���� �ΰ� �� ��
								GM.Record(msg);						// �� ���� ���
								msg = GM.p2.AIBehavior();			// AI�� �ൿ �޾ƿ�
								updateTurn(msg);					// AI�� �ൿ ����
								GM.Record(msg);						// AI�� �ൿ ���
													// �� ���� ���� �α׸� ������Ʈ
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
			
			
			// ���콺�� �������� ��
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
		
		// �� �̵��� ȣ��Ǵ� �Լ�
		public void moveTurn(String turnInfo, boolean moveToPrev) {
			if(moveToPrev == true)
			{
				if(turnInfo.substring(6, 10).equals("Wall"))	// �� ����
				{
					int wallNum = Integer.valueOf(turnInfo.substring(11, 14));
					if(turnInfo.substring(0, 5).equals("Black"))
						GM.DeleteWallNum(wallNum, true);
					else
						GM.DeleteWallNum(wallNum, false);
					infoPanel.UpdateWallLabel();
				}
				else if(turnInfo.substring(6, 10).equals("Move"))	// �̵�
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
		
		// NetWorkThread���� ȣ��, ������ �� ������ �޾ƿͼ� ���� ������Ʈ
		public void updateTurn(String turnInfo) {
			if(turnInfo.substring(0,4).equals("Wall"))		// �� ����
			{
				int wallNum = Integer.valueOf(turnInfo.substring(5, 8));
				GM.PutWallNum(wallNum, turn);
				infoPanel.UpdateWallLabel();
			}
			else if(turnInfo.substring(0,4).equals("Move"))	// �̵�
			{
				int moveX = Integer.valueOf(turnInfo.substring(5,6));
				int moveY = Integer.valueOf(turnInfo.substring(7,8));
				if(gameMode == GAME_MODE.NETWORKGUEST)
					GM.p1.movePlayer(new Point(moveX, moveY));
				else
					GM.p2.movePlayer(new Point(moveX, moveY));
			}
			
			if(GM.GameOver())	// ������ �������� �˻�
			{
				repaint();
				if(turn == true)
				{
					JOptionPane.showMessageDialog(null, GM.p1.getPlayerName() + " �¸�", "���� ���", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
					GM.Save(GM.GetDate(), true);
					new Menu();
					Game.dispose();
				}
				else
				{
					JOptionPane.showMessageDialog(null, GM.p2.getPlayerName() + " �¸�", "���� ���", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
					GM.Save(GM.GetDate(), true);
					new Menu();
					Game.dispose();	
				}
				if(gameMode == GAME_MODE.NETWORKGUEST || gameMode == GAME_MODE.NETWORKHOST)
					GM.GetSocket().CloseSocket();
			}
			else if(turnInfo.equals("Give up"))	// ��밡 �׺��ߴ��� �˻�
			{
				if(turn == false)
				{
					JOptionPane.showMessageDialog(null, GM.p1.getPlayerName() + " �¸�", "���� ���", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
					GM.Save(GM.GetDate(), true);
					new Menu();
					Game.dispose();
				}
				else
				{
					JOptionPane.showMessageDialog(null, GM.p2.getPlayerName() + " �¸�", "���� ���", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
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
		
		// ���� �� �׸���
		public void paintComponent(Graphics g) {
			setBackground(new Color(150, 50, 0));
			super.paintComponent(g);
			
			// �� �׸���
			g.setColor(Color.BLACK);
			for(int i = 1; i < 10; i++) {
				g.drawLine(0, 38*i, 38*9, 38*i); // ���μ�
			}
			for(int i = 1; i < 9; i++) {
				g.drawLine(38*i, 0, 38*i, 38*9); // ���μ�
			}
			
			// �÷��̾� �׸���
			drawPlayer(g, 1);
			drawPlayer(g, 2);

			// �� �׸���
			drawWall(g);
			
			// �̵� ������ ��ġ �׸���
			if(GM.GetPutMode() == false)
			{
				drawAvailablePoint(g);
			}
			else
			{
				drawAvailableWall(g);
			}
		}
		
		// �÷��̾� �׸���
		public void drawPlayer(Graphics g, int playerNum) {
			// ��� �÷��̾�
			if(playerNum == 1) {
				g.setColor(Color.BLACK);
				g.fillOval(GM.p1.getPoint().x*38, GM.p1.getPoint().y*38, 38, 38);
			}
			// ������ �÷��̾�
			else if(playerNum == 2) {
				g.setColor(Color.WHITE);
				g.fillOval(GM.p2.getPoint().x*38, GM.p2.getPoint().y*38, 38, 38);
			}
			
		}
		
		// �� �׸���
		public void drawWall(Graphics g) {
			g.setColor(Color.YELLOW);
			
			boolean[][][] WallPoints = GM.GetWallPoint();
			for(int i = 0; i < 8; i++) {
				for(int j = 0; j < 8; j++) {
					if(WallPoints[0][j][i])
					{
						g.fillRect(38*j, 38*(i+1) - 2, 76, 3); // ���κ�
					}
					if(WallPoints[1][j][i])
					{
						g.fillRect(38*(j+1) - 2, 38*i, 3, 76); // ���κ�
					}
				}
			}
		}
		
		// �̵� ������ ��ġ�� ǥ�����ִ� �Լ�
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
		
		// ��ġ ������ ���� ǥ�����ִ� �Լ�
		public void drawAvailableWall(Graphics g) {
			g.setColor(new Color(0, 0, 255, 100));
			
			// ���� ���콺 ��ġ�� �ش��ϴ� ���� ��ȣ�� �ҷ��� ����
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
									!wallPoint[0][(j-1) < 0 ? 0 : (j-1)][i] && !wallPoint[0][(j+1) > 7 ? 7 : (j+1)][i] && // �� ������ ��ġ�� ���� �� ���� ��
									!wallPoint[1][j][i]) // ���η� ��ġ�� ���� �� ���� ��
							{
								g.fillRect(38*j, 38*(i+1) - 2, 76, 5); // ���κ�
								break;
							}
						}
						else
						{
							if(j == vWallNum && i == hWallNum && !wallPoint[1][j][i] &&
									!wallPoint[1][j][(i-1) < 0 ? 0 : (i-1)] && !wallPoint[1][j][(i+1) > 7 ? 7 : (i+1)] && // ���Ʒ��� ��ġ�� ���� �� ���� ��
									!wallPoint[0][j][i]) // ���η� ��ġ�� ���� �� ���� ��
							{
								g.fillRect(38*(j+1) - 2, 38*i, 5, 76); // ���κ�
								break;
							}
						}
					}
				}
			}
		}
	}

	// ���� �ϴܿ� ������ ��Ÿ���ִ� �г�
	class InfoPanel extends JPanel {
		private GameManager GM;
		JLabel wallLabelp1;
		JLabel wallLabelp2;
		JLabel turnLabel;
		InfoPanel(GameManager gm) {
			setBackground(new Color(150, 50, 0));
			
			this.GM = gm; 
			setLayout(new BorderLayout());
			
			// ������ Walls ��
			wallLabelp1 = new JLabel("Walls: " + GM.p1.getWallNum());
			wallLabelp1.setFont(new Font("����", Font.ITALIC, 15));
			wallLabelp1.setForeground(Color.BLACK);
			wallLabelp1.setSize(100, 30);

			// �Ͼ�� Walls ��
			wallLabelp2 = new JLabel("Walls: " + GM.p2.getWallNum());
			wallLabelp2.setFont(new Font("����", Font.ITALIC, 15));
			wallLabelp2.setForeground(Color.WHITE);
			wallLabelp2.setSize(100, 30);
			
			// ���� �� ��
			if(GM.GetTurn() == true) {
				turnLabel = new JLabel("BLACK");
				turnLabel.setForeground(Color.BLACK);
			}
			else {
				turnLabel = new JLabel("WHITE");
				turnLabel.setForeground(Color.WHITE);
			}
			turnLabel.setFont(new Font("����", Font.ITALIC | Font.BOLD, 25));
			turnLabel.setSize(160, 50);
			turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			// ���� �г� ��ġ
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

