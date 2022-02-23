package com.quoridor.agents;

import java.awt.Point;
import java.util.Vector;
import java.util.Random;

import com.quoridor.enums.PLAYER_COLOR;
import com.quoridor.game.object.GameAction;
import com.quoridor.game.object.GameState;

/*
 * 인공지능
 */

public class AI extends Player{
	public AI(PLAYER_COLOR playerColor, int numWalls, String playerName) {
		super(playerColor, numWalls, playerName, null);
	}
	
	public GameAction SelectAction(GameState gameState) {
		GameAction action;
		Random random = new Random();

		int wallOrMove = random.nextInt(3);	// 1/3확률로 벽 생성, 2/3확률로 이동
		
		
		if(numWalls <= 0) {	// 생성 가능한 벽이 없을 시 항상 이동
			wallOrMove = 1;
		}
		
		if(wallOrMove == 0) {	// 벽 생성
			do {
				boolean vertical = random.nextBoolean();
				int pointY = random.nextInt(8);
				int pointX = random.nextInt(8);

				action = new GameAction(new Point(pointX, pointY), vertical);
			} while (!gameState.isAvailableWall(action));
		} else {	// 이동
			Vector<Point> availablePoints = gameState.getAvailableMoves();
			
			int MoveP = GetDirection();
			while(MoveP >= availablePoints.size()) {
				MoveP = GetDirection();
			}

			action = new GameAction(availablePoints.get(MoveP));
		}

		return action;
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
		return MoveP;
	}

}
