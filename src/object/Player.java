package object;

import java.awt.*;

/*
 * �÷��̾��� ����
 */

public class Player {
	private Point p; // player point
	private int playernum; // �÷��̾� ��ȣ
	protected int wallNum; // ���� ����
	private String playerName;	// �÷��̾��
	protected GameManager GM;	// ���� �Ŵ���
	
	Player(int pnum, GameManager gm, int wallNum, String playerName) {
		this.playernum = pnum;
		this.GM = gm;
		this.wallNum = wallNum;
		this.playerName = playerName;
		
		// �ʱ� ��ġ ����
		this.p = new Point(4, 0);
		if(playernum == 1) {
			this.p.y = 8;
		}
		else {
			this.p.y = 0;
		}
		
	}
	public int getPlayerNum() { return playernum; }		// �÷��̾� ��ȣ ��ȯ. 1���� ����, 2���� �Ͼ�
	public Point getPoint() { return p; }				// �÷��̾� ��ġ ��ȯ
	public int getWallNum() { return wallNum; }			// ���� ������ ���� �� ��ȯ
	public void minusWallNum() { wallNum--; }			// ���� �� ����
	public void plusWallNum() { wallNum++; }			// ���� �� ����
	public void movePlayer(Point p) { this.p = p; } 	// �÷��̾� �̵�
	public void setPlayerName(String playerName) { this.playerName = playerName; }	// �÷��̾� �̸� ����
	public String getPlayerName() { return playerName; }							// �÷��̾� �̸� ��ȯ
	
	public String AIBehavior() { return ""; } // AI�� �ൿ�� �����ϰ� String���� ��ȯ�ϴ� �Լ�
	
	// �̵� ������ ��ġ�� ��ȯ�ϴ� �Լ�
	public Point[] updateAvailablePlace(boolean[][][] wallPoints, Point opponentP) { // ���� ��ġ�� ��� ��ġ�� �޾ƿ�
		
		Point[] availablePoints = new Point[4];
		
		// �÷��̾� ��ġ�� 0�϶��� 8�϶� ���� ����ϱ� ���� �ϱ� ���� ����
		int pymin = 0;
		int pymax = 8;
		int pxmin = 0;
		int pxmax = 8;
		
		int pnum = 0; // �̵� ������ ��ġ ����
		
		if(p.y <= 0) pymin = 0; else pymin = p.y - 1;
		if(p.y >= 8) pymax = 7; else pymax = p.y;
		if(p.x <= 0) pxmin = 0; else pxmin = p.x - 1;
		if(p.x >= 8) pxmax = 7; else pxmax = p.x;
		
		// ������ �̵� ���� ����
		if(p.x < 8) { 
			if(wallPoints[1][p.x][pymin] == false && wallPoints[1][p.x][pymax] == false) // �����ʿ� ���� ���� ��
			{
				if(opponentP.x == p.x + 1 && opponentP.y == p.y && p.x < 8) // ���� �پ� �����鼭 �ǳ� �� �� �ִ� ��Ȳ�� ��
				{
					if(wallPoints[1][(p.x + 1 > 7)? 7 : p.x + 1][pymin] == false && wallPoints[1][(p.x + 1 > 7)? 7 : p.x + 1][pymax] == false)
					{
						if(p.x + 2 <= 8)	// �پ�Ѿ��� �� ���� ����� �ʴ� ���
							availablePoints[pnum++] = new Point(p.x + 2, p.y);
						else
							availablePoints[pnum++] = new Point(9, 9); // ���� ����� ��� (9, 9)����
					}
					else availablePoints[pnum++] = new Point(9, 9); // �̵��� �� ���ٴ� �ǹ̷� (9, 9)�� ����
				}	
				else
					availablePoints[pnum++] = new Point(p.x + 1, p.y);
			}
			else
				availablePoints[pnum++] = new Point(9, 9); // �̵��� �� ���ٴ� �ǹ̷� (9, 9)�� ����
				// availablePlace[p.x + 1][p.y] = true;
		}
		else
			availablePoints[pnum++] = new Point(9, 9);
		
		// ���� �̵� ���� ����
		if(p.x > 0) {
			if(wallPoints[1][p.x - 1][pymin] == false && wallPoints[1][p.x - 1][pymax] == false)
			{
				if(opponentP.x == p.x - 1 && opponentP.y == p.y && p.x > 0)
				{
					if(wallPoints[1][(p.x - 2 < 0 )? 0 : p.x - 2][pymin] == false && wallPoints[1][(p.x - 2 < 0)? 0 : p.x - 2][pymax] == false)
					{
						if(p.x - 2 >= 0)
							availablePoints[pnum++] = new Point(p.x - 2, p.y);
						else
							availablePoints[pnum++] = new Point(9, 9);
					}
					else availablePoints[pnum++] = new Point(9, 9); // �̵��� �� ���ٴ� �ǹ̷� (9, 9)�� ����
				}
				else
					availablePoints[pnum++] = new Point(p.x - 1, p.y);
			}
			else
				availablePoints[pnum++] = new Point(9, 9);
				// availablePlace[p.x - 1][p.y] = true;
		}
		else
			availablePoints[pnum++] = new Point(9, 9);
		
		// �Ʒ��� �̵� ���� ����
		if(p.y < 8) {
			if(wallPoints[0][pxmin][p.y] == false && wallPoints[0][pxmax][p.y] == false)
			{
				if(opponentP.x == p.x && opponentP.y == p.y + 1 && p.y < 8)
				{
					if(wallPoints[0][pxmin][(p.y + 1 > 7)? 7 : p.y + 1] == false && wallPoints[0][pxmax][(p.y + 1 > 7)? 7 : p.y + 1] == false)
					{
						if(p.y + 2 <= 8)
							availablePoints[pnum++] = new Point(p.x, p.y + 2);
						else
							availablePoints[pnum++] = new Point(9, 9);
					}
					else availablePoints[pnum++] = new Point(9, 9); // �̵��� �� ���ٴ� �ǹ̷� (9, 9)�� ����
				}
				else
					availablePoints[pnum++] = new Point(p.x, p.y + 1);
			}
			else
				availablePoints[pnum++] = new Point(9, 9);
				// availablePlace[p.x][p.y + 1] = true;
		}
		else
			availablePoints[pnum++] = new Point(9, 9);
		
		// ���� �̵� ���� ����
		if(p.y > 0) {
			if(wallPoints[0][pxmin][p.y - 1] == false && wallPoints[0][pxmax][p.y - 1] == false)
			{
				if(opponentP.x == p.x && opponentP.y == p.y - 1 && p.y > 0)
				{
					if(wallPoints[0][pxmin][(p.y - 2 < 0)? 0 : p.y - 2] == false && wallPoints[0][pxmax][(p.y - 2 < 0)? 0 : p.y - 2] == false)
					{
						if(p.y - 2 >= 0)
							availablePoints[pnum++] = new Point(p.x, p.y - 2);
						else
							availablePoints[pnum++] = new Point(9, 9);
					}
					else availablePoints[pnum++] = new Point(9, 9); // �̵��� �� ���ٴ� �ǹ̷� (9, 9)�� ����
				}
				else
				availablePoints[pnum++] = new Point(p.x, p.y - 1);
			}
			else
				availablePoints[pnum++] = new Point(9, 9);
				// availablePlace[p.x][p.y - 1] = true;
		}
		else
			availablePoints[pnum++] = new Point(9, 9);

		return availablePoints;
	}
}