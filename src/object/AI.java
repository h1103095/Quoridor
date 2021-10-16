package object;

import java.awt.Point;

/*
 * 인공지능
 */

public class AI extends Player{
	public AI(int i, GameManager gm, int wallNum, String playerName) {
		super(i, gm, wallNum, playerName);
		
	}
	
	public String AIBehavior() {
		int wallOrMove = (int)(Math.random()*3);	// 1/3확률로 벽 생성, 2/3확률로 이동
		int putwallNum = 0;
		
		if(wallNum <= 0)	// 생성 가능한 벽이 없을 시 항상 이동
		{
			wallOrMove = 1;
		}
		
		if(wallOrMove == 0)	// 벽 생성
		{
			while(true) {
				boolean isVertical;
				putwallNum = (int)(Math.random()*2) + 1;				// 가로, 세로 결정
				putwallNum = putwallNum*10 + (int)(Math.random()*8);	// 가로축 결정
				putwallNum = putwallNum*10 + (int)(Math.random()*8);	// 세로축 결정
				
				int vWallNum = putwallNum%10;
				int hWallNum = (putwallNum/10)%10;
				boolean wallPoint[][][] = GM.GetWallPoint();
				
				if(putwallNum / 100 == 1)
					isVertical = true;		// 가로
				else isVertical = false;	// 세로
				
				vWallNum = ((vWallNum > 7) ? 7 : vWallNum);
				hWallNum = ((hWallNum > 7) ? 7 : hWallNum);
				for(int i = 0; i < 9; i++) {
					for(int j = 0; j < 9; j++) {
						//System.out.println(putwallNum);
						if(isVertical)
						{
							if(j == vWallNum && i == hWallNum && !wallPoint[0][j][i] &&
									!wallPoint[0][(j-1) < 0 ? 0 : (j-1)][i] && !wallPoint[0][(j+1) > 7 ? 7 : (j+1)][i] && // 양 옆으로 겹치게 세울 수 없는 벽
									!wallPoint[1][j][i]) // 세로로 겹치게 세울 수 없는 벽
							{
								return "Wall " + Integer.toString(putwallNum);			// 기록할 내용
								//break;
							}
						}
						else
						{
							if(j == vWallNum && i == hWallNum && !wallPoint[1][j][i] &&
									!wallPoint[1][j][(i-1) < 0 ? 0 : (i-1)] && !wallPoint[1][j][(i+1) > 7 ? 7 : (i+1)] && // 위아래로 겹치게 세울 수 없는 벽
									!wallPoint[0][j][i]) // 가로로 겹치게 세울 수 없는 벽
							{
								return "Wall " + Integer.toString(putwallNum);			// 기록할 내용
								//break;
							}
						}
					}
				}
			}

		}
		else {				// 이동
			String prevPosition = Integer.toString(getPoint().x) + " " + Integer.toString(getPoint().y);	// 이전 위치 얻음
			
			Point[] availablePoints = GM.GetAvailablePoint();
			
			int MoveP = GetDirection();
			while(availablePoints[MoveP].x == 9 && availablePoints[MoveP].y == 9)
				MoveP = GetDirection();
			
			return "Move " + Integer.toString(availablePoints[MoveP].x) + " " + Integer.toString(availablePoints[MoveP].y) + " from " + prevPosition;
		}
	}
	
	private int GetDirection() {
		int randomDirection = (int)(Math.random()*10);
		int MoveP = 0; // 0~3까지 오른쪽, 왼쪽, 아래쪽, 위쪽
		switch(randomDirection) {
		case 0:
		case 1:
		case 2:
		case 3:
			MoveP = 2;	// 4/10 확률로 아래
			break;
		case 4:
		case 5:
		case 6:
			MoveP = 0;	// 3/10 확률로 오른쪽
			break;
		case 7:
		case 8:
			MoveP = 1;	// 2/10 확률로 왼쪽
			break;
		case 9:
			MoveP = 3;	// 1/10 확률로 위쪽
			break;
		}
		System.out.println(MoveP);
		return MoveP;
	}

}
