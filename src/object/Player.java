package object;

import java.awt.*;

/*
 * 플레이어의 정보
 */

public class Player {
	private Point p; // player point
	private int playernum; // 플레이어 번호
	protected int wallNum; // 벽의 개수
	private String playerName;	// 플레이어명
	protected GameManager GM;	// 게임 매니저
	
	Player(int pnum, GameManager gm, int wallNum, String playerName) {
		this.playernum = pnum;
		this.GM = gm;
		this.wallNum = wallNum;
		this.playerName = playerName;
		
		// 초기 위치 설정
		this.p = new Point(4, 0);
		if(playernum == 1) {
			this.p.y = 8;
		}
		else {
			this.p.y = 0;
		}
		
	}
	public int getPlayerNum() { return playernum; }		// 플레이어 번호 반환. 1번이 검정, 2번이 하양
	public Point getPoint() { return p; }				// 플레이어 위치 반환
	public int getWallNum() { return wallNum; }			// 생성 가능한 벽의 수 반환
	public void minusWallNum() { wallNum--; }			// 벽의 수 감소
	public void plusWallNum() { wallNum++; }			// 벽의 수 증가
	public void movePlayer(Point p) { this.p = p; } 	// 플레이어 이동
	public void setPlayerName(String playerName) { this.playerName = playerName; }	// 플레이어 이름 설정
	public String getPlayerName() { return playerName; }							// 플레이어 이름 반환
	
	public String AIBehavior() { return ""; } // AI의 행동을 결정하고 String으로 반환하는 함수
	
	// 이동 가능한 위치를 반환하는 함수
	public Point[] updateAvailablePlace(boolean[][][] wallPoints, Point opponentP) { // 벽의 위치와 상대 위치를 받아옴
		
		Point[] availablePoints = new Point[4];
		
		// 플레이어 위치가 0일때와 8일때 벽을 계산하기 쉽게 하기 위한 변수
		int pymin = 0;
		int pymax = 8;
		int pxmin = 0;
		int pxmax = 8;
		
		int pnum = 0; // 이동 가능한 위치 개수
		
		if(p.y <= 0) pymin = 0; else pymin = p.y - 1;
		if(p.y >= 8) pymax = 7; else pymax = p.y;
		if(p.x <= 0) pxmin = 0; else pxmin = p.x - 1;
		if(p.x >= 8) pxmax = 7; else pxmax = p.x;
		
		// 오른쪽 이동 가능 여부
		if(p.x < 8) { 
			if(wallPoints[1][p.x][pymin] == false && wallPoints[1][p.x][pymax] == false) // 오른쪽에 벽이 없을 때
			{
				if(opponentP.x == p.x + 1 && opponentP.y == p.y && p.x < 8) // 상대랑 붙어 있으면서 건너 뛸 수 있는 상황일 때
				{
					if(wallPoints[1][(p.x + 1 > 7)? 7 : p.x + 1][pymin] == false && wallPoints[1][(p.x + 1 > 7)? 7 : p.x + 1][pymax] == false)
					{
						if(p.x + 2 <= 8)	// 뛰어넘었을 때 맵을 벗어나지 않는 경우
							availablePoints[pnum++] = new Point(p.x + 2, p.y);
						else
							availablePoints[pnum++] = new Point(9, 9); // 맵을 벗어나는 경우 (9, 9)대입
					}
					else availablePoints[pnum++] = new Point(9, 9); // 이동할 수 없다는 의미로 (9, 9)를 대입
				}	
				else
					availablePoints[pnum++] = new Point(p.x + 1, p.y);
			}
			else
				availablePoints[pnum++] = new Point(9, 9); // 이동할 수 없다는 의미로 (9, 9)를 대입
				// availablePlace[p.x + 1][p.y] = true;
		}
		else
			availablePoints[pnum++] = new Point(9, 9);
		
		// 왼쪽 이동 가능 여부
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
					else availablePoints[pnum++] = new Point(9, 9); // 이동할 수 없다는 의미로 (9, 9)를 대입
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
		
		// 아래쪽 이동 가능 여부
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
					else availablePoints[pnum++] = new Point(9, 9); // 이동할 수 없다는 의미로 (9, 9)를 대입
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
		
		// 위쪽 이동 가능 여부
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
					else availablePoints[pnum++] = new Point(9, 9); // 이동할 수 없다는 의미로 (9, 9)를 대입
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