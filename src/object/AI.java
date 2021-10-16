package object;

import java.awt.Point;

/*
 * �ΰ�����
 */

public class AI extends Player{
	public AI(int i, GameManager gm, int wallNum, String playerName) {
		super(i, gm, wallNum, playerName);
		
	}
	
	public String AIBehavior() {
		int wallOrMove = (int)(Math.random()*3);	// 1/3Ȯ���� �� ����, 2/3Ȯ���� �̵�
		int putwallNum = 0;
		
		if(wallNum <= 0)	// ���� ������ ���� ���� �� �׻� �̵�
		{
			wallOrMove = 1;
		}
		
		if(wallOrMove == 0)	// �� ����
		{
			while(true) {
				boolean isVertical;
				putwallNum = (int)(Math.random()*2) + 1;				// ����, ���� ����
				putwallNum = putwallNum*10 + (int)(Math.random()*8);	// ������ ����
				putwallNum = putwallNum*10 + (int)(Math.random()*8);	// ������ ����
				
				int vWallNum = putwallNum%10;
				int hWallNum = (putwallNum/10)%10;
				boolean wallPoint[][][] = GM.GetWallPoint();
				
				if(putwallNum / 100 == 1)
					isVertical = true;		// ����
				else isVertical = false;	// ����
				
				vWallNum = ((vWallNum > 7) ? 7 : vWallNum);
				hWallNum = ((hWallNum > 7) ? 7 : hWallNum);
				for(int i = 0; i < 9; i++) {
					for(int j = 0; j < 9; j++) {
						//System.out.println(putwallNum);
						if(isVertical)
						{
							if(j == vWallNum && i == hWallNum && !wallPoint[0][j][i] &&
									!wallPoint[0][(j-1) < 0 ? 0 : (j-1)][i] && !wallPoint[0][(j+1) > 7 ? 7 : (j+1)][i] && // �� ������ ��ġ�� ���� �� ���� ��
									!wallPoint[1][j][i]) // ���η� ��ġ�� ���� �� ���� ��
							{
								return "Wall " + Integer.toString(putwallNum);			// ����� ����
								//break;
							}
						}
						else
						{
							if(j == vWallNum && i == hWallNum && !wallPoint[1][j][i] &&
									!wallPoint[1][j][(i-1) < 0 ? 0 : (i-1)] && !wallPoint[1][j][(i+1) > 7 ? 7 : (i+1)] && // ���Ʒ��� ��ġ�� ���� �� ���� ��
									!wallPoint[0][j][i]) // ���η� ��ġ�� ���� �� ���� ��
							{
								return "Wall " + Integer.toString(putwallNum);			// ����� ����
								//break;
							}
						}
					}
				}
			}

		}
		else {				// �̵�
			String prevPosition = Integer.toString(getPoint().x) + " " + Integer.toString(getPoint().y);	// ���� ��ġ ����
			
			Point[] availablePoints = GM.GetAvailablePoint();
			
			int MoveP = GetDirection();
			while(availablePoints[MoveP].x == 9 && availablePoints[MoveP].y == 9)
				MoveP = GetDirection();
			
			return "Move " + Integer.toString(availablePoints[MoveP].x) + " " + Integer.toString(availablePoints[MoveP].y) + " from " + prevPosition;
		}
	}
	
	private int GetDirection() {
		int randomDirection = (int)(Math.random()*10);
		int MoveP = 0; // 0~3���� ������, ����, �Ʒ���, ����
		switch(randomDirection) {
		case 0:
		case 1:
		case 2:
		case 3:
			MoveP = 2;	// 4/10 Ȯ���� �Ʒ�
			break;
		case 4:
		case 5:
		case 6:
			MoveP = 0;	// 3/10 Ȯ���� ������
			break;
		case 7:
		case 8:
			MoveP = 1;	// 2/10 Ȯ���� ����
			break;
		case 9:
			MoveP = 3;	// 1/10 Ȯ���� ����
			break;
		}
		System.out.println(MoveP);
		return MoveP;
	}

}
