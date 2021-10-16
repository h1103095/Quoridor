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
 * ���Ⱑ ����Ǵ� �����Ӱ� �� �������
 */

// ���� ȭ�� ����
public class Replay extends JFrame{
	private GameManager GM;
	private GamePanel gamePanel;
	private ButtonPanel buttonPanel;
	private InfoPanel infoPanel;
	private GameSound move_sound = new GameSound("sounds/move.wav");
	private GameSound wall_sound = new GameSound("sounds/wall.wav");

	
	public Replay(GameManager gm) {
		this.GM = gm;
		
		/* ȭ�� ���� */
		setTitle("Replay");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//setContentPane();
		Container contentPane = getContentPane();
		buttonPanel = new ButtonPanel(GM, this);
		infoPanel = new InfoPanel(GM);
		gamePanel = new GamePanel(GM, infoPanel, buttonPanel, this);
		
		contentPane.setLayout(null);
		contentPane.add(gamePanel);
		
		// �гε� ��ġ��
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
	
	class ButtonPanel extends JPanel {
		JButton[] menuButtons = new JButton[3];
		GameManager GM;
		GAME_MODE gameMode;
		private Frame Game; // â�� �ݱ� ���� ����
		
		ButtonPanel(GameManager gm, Frame game) {
			this.Game = game;
			this.GM = gm;
			this.gameMode = GM.GetGameMode();
			setBackground(new Color(150, 50, 0));
			
			// �޴� �г� ��ư ��ġ
			setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
			String[] menubtStrings = {"������", "�ߴ�", "�ڷ�"};
			
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
				if(e.getSource() == menuButtons[0]) {			// ������
					String msg = GM.GetNextTurn();
					GM.ChangeTurn();
					gamePanel.moveTurn(msg, false);
				}else if(e.getSource() == menuButtons[1]) {		// ��� || �ߴ� || ����
					if(menuButtons[1].getText().equals("���"))		// ���
					{
						gamePanel.th = new Thread(new ReplayThread());
						gamePanel.th.start();
						menuButtons[1].setText("����");
					}
					else if(menuButtons[1].getText().equals("�ߴ�"))	// �ߴ�
					{
						gamePanel.th.interrupt();
						menuButtons[1].setText("���");
					}
					else											// ����
					{
						gamePanel.th.interrupt();
						new Menu();
						Game.dispose();
					}
					
				}else if(e.getSource() == menuButtons[2]) {		// �ڷ�
					String msg = GM.GetPrevTurn();
					gamePanel.moveTurn(msg, true);
					GM.ChangeTurn();
				}
			}
		}

		// ��ư�� ��� ���������� üũ�ϴ� �Լ�
		public void checkbtAvailable() {
			if(GM.GetSelectTurn() == 1)
				menuButtons[2].setEnabled(false);
			else
				menuButtons[2].setEnabled(true);
			if(GM.GetSelectTurn() == GM.GetTurnCount())
			{
				menuButtons[0].setEnabled(false);
				menuButtons[1].setText("���");
				gamePanel.th.interrupt();
			}
			else
				menuButtons[0].setEnabled(true);
		}
	}
	
	// ���÷��̸� ���� ������
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

	// ������ ����Ǵ� �г�
	class GamePanel extends JPanel{
		GameManager GM;
		InfoPanel infoPanel;
		ButtonPanel buttonPanel;
		private Frame Game;
		boolean turn;
		int wallNum = -1; // ���� (100 ~ 288) ���� �ڸ� ���� ���� �ڸ� ���� ��ǥ���� ��ġ ��Ÿ��, 8���� ũ�� ����. 8�� �� 7�� �ٲپ� ���
		Thread th;
		
		GamePanel(GameManager gm, InfoPanel infoPanel, ButtonPanel buttonPanel, Frame game) {
			this.GM = gm; // ���� �޴��� �ҷ���
			this.infoPanel = infoPanel; // ���� �г� �ҷ���
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

